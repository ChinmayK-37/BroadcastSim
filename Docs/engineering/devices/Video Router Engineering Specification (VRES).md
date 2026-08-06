Video Router Engineering Specification (VRES)
Document Information
Field	Value
Document	Video Router Engineering Specification
Abbreviation	VRES
Version	1.0
Project	BroadcastSim
Status	Draft
1. Purpose

The Video Router is responsible for directing logical video signals from one or more input ports to one or more output ports.

The router does not modify video content.

Its responsibility is only signal switching and routing.

2. Responsibilities

The Video Router shall

Receive logical video signals
Maintain routing table
Connect input ports to output ports
Forward signals
Calculate runtime metrics
Report health status

The Video Router shall not

Encode signals
Decode signals
Generate new signals
Store signals
3. Device Classification
Property	Value
Category	Routing Device
Signal Role	Forwarder
Input Ports	Configurable
Output Ports	Configurable
Rule Model	RouterRuleModel
4. Configuration Properties
Property	Unit
Number of Input Ports	count
Number of Output Ports	count
Routing Mode	Enum
Default Route	Route
Routing Modes

Version 1

Manual

Future

Automatic
Priority
Load Balanced
Redundant
5. Runtime Properties
Property	Unit
Active Routes	count
CPU Usage	%
Memory Usage	MB
Power Consumption	W
Temperature	°C
Total Throughput	Mbps / Gbps
Device Health	Enum
6. Port Model

Each port contains

Port ID
Direction
Connection Status
Current Signal
Connected Device

Ports may be

Connected
Disconnected
Disabled
7. Routing Table

The router maintains a routing table.

Example

Input	Output
IN1	OUT1
IN2	OUT3
IN3	OUT2

Version 1 supports one input to one output mapping.

Future versions may support multicast.

8. Signal Forwarding Pipeline
Input Signal
      │
      ▼
Input Port
      │
      ▼
Routing Table Lookup
      │
      ▼
Output Port
      │
      ▼
Forward Signal

The router does not alter

Resolution
FPS
Codec
Bitrate

Only the signal path changes.

9. Engineering Model

The router calculates

Connected Ports
        │
        ▼
Active Routes
        │
        ▼
Total Throughput
        │
        ▼
CPU Usage
        │
        ▼
Memory Usage
        │
        ▼
Power
        │
        ▼
Temperature
        │
        ▼
Health
10. Formula References

The router shall use

Throughput Formula
CPU Formula
Memory Formula
Power Formula
Temperature Formula

Defined in the Engineering Formula Specification (EFS).

11. Constants Used

The router references

Router Constants
Power Constants
CPU Constants
Temperature Constants
Network Constants

from the Engineering Constants Specification (ECS).

12. Runtime Behaviour
Adding Routes

Increasing active routes increases

CPU Usage
Throughput
Power Consumption
Temperature
Removing Routes

Removing routes decreases

CPU Usage
Throughput
Power Consumption

Temperature decreases gradually according to the cooling model.

Disconnected Port

If an output port becomes disconnected

Signal forwarding stops
Routing table remains intact
Throughput decreases
13. Signal Handling

The router

Receives

Logical video signals

Produces

Forwarded logical video signals

The router never modifies

Resolution
FPS
Codec
Bitrate
Signal Quality

Only propagation path changes.

14. Health and Failure Conditions

CPU and temperature threshold violations produce WARNING or CRITICAL according to ECS.

No active power and an injected internal fault are represented by OperationalFailureType and produce FAILED through HealthCalculator.

Future routing failures shall define an OperationalFailureType before they can produce FAILED.
15. Recovery

Recovery sequence

FAILED
    │
    ▼
RECOVERING
    │
    ▼
ONLINE

Runtime metrics recover gradually.

Routing configuration is preserved unless explicitly reset.

16. Performance Expectations

Example

Router

32 Inputs

32 Outputs

16 Active Routes

Expected

Moderate CPU utilization
Moderate memory usage
Stable throughput
Stable operating temperature

Exact values depend on ECS constants.

17. Future Enhancements

Future versions may support

Crosspoint matrix simulation
Multicast routing
Redundant paths
Automatic failover
Route priorities
Signal groups
SDI matrix routing
SMPTE ST 2110 flow routing
NMOS IS-04 / IS-05 inspired routing concepts
18. Version History
Version	Changes
1.0	Initial Video Router Engineering Specification
