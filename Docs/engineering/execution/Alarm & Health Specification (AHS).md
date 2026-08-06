Alarm & Health Specification (AHS)
Document Information
Field	Value
Document	Alarm & Health Specification
Abbreviation	AHS
Version	1.0
Project	BroadcastSim
Status	Draft
1. Purpose

This specification defines how BroadcastSim evaluates device health and generates engineering alarms.

The Health Engine evaluates device runtime metrics and assigns a health state.

The Alarm Engine converts abnormal health conditions into alarms for operators.

Health and alarms are separate concepts.

2. Health vs Alarm

Health

Represents the internal operating condition of a device.

Alarm

Represents a notification generated because of a health condition.

Example

CPU = 96%

↓

Health = CRITICAL

↓

Alarm = HIGH_CPU

Health always exists.

Alarm exists only when required.

3. Health States

Every device is always in exactly one health state.

NORMAL

↓

WARNING

↓

CRITICAL

↓

FAILED

Definitions

NORMAL

Device operates within engineering limits.

WARNING

Device exceeds recommended operating limits but still functions.

CRITICAL

Device is operating outside safe engineering limits.

Immediate operator attention is recommended.

FAILED

Device can no longer perform its primary function.

4. Alarm Severity

Version 1

INFO

WARNING

CRITICAL

Future

EMERGENCY
5. Alarm Types

Version 1

Alarm	Trigger
HIGH_CPU	CPU threshold exceeded
HIGH_MEMORY	Memory threshold exceeded
HIGH_TEMPERATURE	Temperature threshold exceeded
HIGH_POWER	Power threshold exceeded
SIGNAL_LOST	No incoming/outgoing signal
DEVICE_DISCONNECTED	Connection removed
ROUTE_FAILURE	Invalid routing
INVALID_CONFIGURATION	Invalid configuration detected
6. Health Evaluation Order

Health shall be evaluated after Rule Engine execution.

Rule Engine

↓

Updated Runtime

↓

Health Evaluation

↓

Alarm Generation
7. Evaluation Priority

HealthCalculator shall first evaluate active OperationalFailureType conditions.

An OperationalFailureType produces FAILED. Runtime metric thresholds do not produce FAILED; they produce WARNING or CRITICAL according to ECS.

When no OperationalFailureType is active, HealthCalculator shall evaluate the following runtime metrics:

CPU Usage

Memory Usage

Temperature

Power Utilization

Signal Quality

The highest severity determines overall health using:

FAILED > CRITICAL > WARNING > NORMAL

Example

Temperature = NORMAL

CPU = WARNING

Signal = CRITICAL

↓

Overall Health = CRITICAL
8. Threshold Sources

All thresholds shall originate from

Engineering Constants Specification (ECS).

No thresholds may be hardcoded.

9. Alarm Lifecycle

Every alarm follows

Not Raised

↓

Raised

↓

Acknowledged

↓

Cleared

↓

Archived

Version 1

Support

Raised
Cleared

Future versions

Acknowledged
Archived
10. Alarm Rules

An alarm is generated only when

threshold is exceeded
device is ONLINE or WARNING
alarm is not already active

Duplicate alarms shall not be generated.

11. Alarm Clearing

An alarm is cleared when

The triggering condition no longer exists.

Example

CPU

96%

↓

70%

↓

HIGH_CPU cleared
12. Health Recovery

Health recovery first requires that no OperationalFailureType is active.

IF any OperationalFailureType is present

→ Device = FAILED

ELSE IF any runtime metric is CRITICAL

→ Device = CRITICAL

ELSE IF any runtime metric is WARNING

→ Device = WARNING

ELSE

→ Device = NORMAL

Runtime metrics evaluated

CPU
Memory
Temperature
Power
Signal Quality

Recovery is gradual.

Instant recovery is not permitted.

13. Alarm Data Model

Each alarm contains

Alarm ID
Device ID
Alarm Type
Severity
Timestamp
Current Value
Threshold Value
Message

Future

Operator Notes
Acknowledgement User
14. Multiple Alarm Handling

A device may have multiple active alarms.

Example

HIGH_CPU

HIGH_TEMPERATURE

SIGNAL_LOST

Overall health is determined by the highest severity.

15. Alarm Propagation

Version 1

Alarms remain local to the device.

Future versions may support

Parent device alarms
Network-wide alarms
Alarm correlation
16. Performance Requirements

Alarm evaluation shall

execute once per simulation tick
be deterministic
avoid duplicate alarm creation
execute in O(n)

where

n = number of devices
17. Future Enhancements

Future versions

Alarm acknowledgement
Alarm suppression
Alarm correlation
Alarm escalation
Alarm history
Alarm dashboard
Email/SMS/Webhook notifications
Root cause analysis
18. Version History
Version	Changes
1.0	Initial Alarm & Health Specification
