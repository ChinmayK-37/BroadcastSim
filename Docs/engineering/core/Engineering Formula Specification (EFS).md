Engineering Formula Specification (EFS)
1. Engineering Formula Specification (EFS)

Replace the current formula section with the following.

Formula 6.1 – Pixels Per Second
Purpose

Calculate the number of pixels generated every second.

Inputs
Width
Height
FPS
Equation
PixelsPerSecond = Width × Height × FPS
Output
PixelsPerSecond (pixels/sec)
Formula 6.2 – Raw Bitrate
Purpose

Calculate the uncompressed video bitrate.

Inputs
PixelsPerSecond
BitsPerPixel
Equation
RawBitrate = PixelsPerSecond × BitsPerPixel
Output
RawBitrate (bits/sec)
Formula 6.3 – Compressed Bitrate
Purpose

Estimate encoder output bitrate.

Inputs
RawBitrate
CompressionRatio
Equation
CompressedBitrate = RawBitrate / CompressionRatio
Output
CompressedBitrate (bits/sec)
Formula 6.4 – Camera Power
Purpose

Estimate camera power consumption.

Inputs
CPUUsage
IdlePower
MaximumPower
Equation
Power = IdlePower + (CPUUsage / 100) × (MaximumPower - IdlePower)
Output
Power (W)
Formula 6.5 – Camera Temperature
Purpose

Update camera operating temperature.

Inputs
CurrentTemperature
GeneratedHeat
CoolingRate
Equation
TemperatureNext = max(AmbientTemperature, CurrentTemperature + GeneratedHeat - CoolingRate)
Output
TemperatureNext (°C)
7. Encoder Engineering Formulas
Formula 7.1 – CPU Usage
Purpose

Estimate encoder CPU utilization.

Equation
CPUUsage = BaseCPU + ResolutionWeight + FPSWeight + CodecWeight + ((StreamCount - 1) × StreamWeight)
Formula 7.2 – Memory Usage
Purpose

Estimate encoder runtime memory usage.

Equation
MemoryUsage = BaseMemory + (FrameBuffer × StreamCount) + CodecBuffer
Formula 7.3 – Power
Purpose

Estimate encoder power consumption.

Equation
Power = IdlePower + (CPUUsage / 100) × (MaximumPower - IdlePower)
Formula 7.4 – Temperature
Purpose

Estimate encoder operating temperature.

Equation
TemperatureNext = max(AmbientTemperature, CurrentTemperature + (Power × HeatCoefficient) - CoolingRate)
8. Router Engineering Formulas
Formula 8.1 – Throughput
Purpose

Calculate total routed throughput.

Equation
Throughput = Σ(InputSignalBitrate)
Formula 8.2 – CPU Usage
Purpose

Estimate router CPU utilization.

Equation
CPUUsage = BaseCPU + (ActiveRoutes × RouteWeight) + (LinkUtilization × UtilizationWeight)
Formula 8.3 – Link Utilization
Purpose

Calculate link utilization.

Equation
LinkUtilization = (CurrentBitrate / LinkCapacity) × 100
Formula 8.4 – Power
Purpose

Estimate router power consumption.

Equation
Power = IdlePower + (CPUUsage / 100) × (MaximumPower - IdlePower)
Formula 8.5 – Temperature
Purpose

Estimate router operating temperature.

Equation
TemperatureNext = max(AmbientTemperature, CurrentTemperature + (Power × HeatCoefficient) - CoolingRate)

Formula 8.6 – Router Memory Usage
Purpose

Calculate router runtime memory usage.

Equation
MemoryUsage = BaseMemory
9. Signal Engineering Formulas
Formula 9.1 – Signal Quality
Purpose

Calculate overall signal quality.

Equation
SignalQuality = max(0, min(100, 100 - PacketLossPenalty - LatencyPenalty))
Formula 9.2 – Packet Loss Penalty
Purpose

Calculate quality degradation caused by packet loss.

Equation
PacketLossPenalty = PacketLoss × LossWeight
Formula 9.3 – Latency Penalty
Purpose

Calculate quality degradation caused by latency.

Equation
LatencyPenalty = Latency × LatencyWeight
Formula 9.4 – Signal Delay
Purpose

Calculate end-to-end signal latency.

Equation
Latency = BaseLatency + SwitchDelay + QueueDelay
10. Health Evaluation
Formula 10.1 – Overall Device Health

The HealthCalculator shall evaluate operational failures before runtime metrics.

An active OperationalFailureType produces FAILED. Runtime metric thresholds produce only WARNING or CRITICAL.

When no OperationalFailureType is active, the overall device health shall be determined by evaluating the highest severity among the following runtime metrics:

CPU Usage
Memory Usage
Temperature
Power Utilization
Signal Quality
Evaluation Priority
FAILED > CRITICAL > WARNING > NORMAL
Evaluation Algorithm

Step 1 â€“ Operational Failure Evaluation

IF any OperationalFailureType is present
    OverallHealth = FAILED

ELSE
    Continue to runtime metric evaluation

Step 2 â€“ Runtime Metric Evaluation

IF any runtime metric == CRITICAL
    OverallHealth = CRITICAL

ELSE IF any metric == WARNING
    OverallHealth = WARNING

ELSE
    OverallHealth = NORMAL
11. Validation Rules
Formula 11.1 – Link Capacity Validation
Equation
CurrentBitrate ≤ LinkCapacity

If the condition is false:

SignalCongestion = TRUE
Formula 11.2 – Cooling Model
Equation
TemperatureNext = max(AmbientTemperature, CurrentTemperature - CoolingRate)
Formula 11.3 – Runtime Metric Validation

Every calculated runtime metric shall satisfy the following constraints:

Metric	Validation Rule
CPU Usage	0 ≤ CPU ≤ 100
Memory Usage	Memory ≥ 0
Power Consumption	IdlePower ≤ Power ≤ MaximumPower
Temperature	AmbientTemperature ≤ Temperature ≤ ECS MaximumTemperature operating limit. The temperature calculation remains unclamped; a value above the ECS maximum is retained as the calculated observation and is an invalid operating condition.
Signal Quality	0 ≤ SignalQuality ≤ 100
Link Utilization	LinkUtilization ≥ 0
Formula 11.4 – Device Failure Conditions

A device shall transition to the FAILED state only when an OperationalFailureType is present.

OperationalFailureType is the implementation representation of an active operational failure condition used by HealthCalculator.

The following conditions constitute a failure:

Device power unavailable
Thermal protection shutdown
Runtime watchdog termination
Memory allocation failure
Required input signal unavailable
Explicit device fault injection

Exceeding a runtime metric threshold (CPU, Memory, Temperature, Power Utilization, or Signal Quality) shall not directly produce the FAILED state. Threshold violations result in WARNING or CRITICAL states; FAILED represents loss of operational capability.
Formula 11.5 – Power Utilization

Purpose

Calculate current power utilization as a percentage of maximum power.

Equation
PowerUtilization = (PowerConsumption / MaximumPower) × 100

1. Document Information
Field	Value
Document	Engineering Formula Specification
Abbreviation	EFS
Version	1.0
Project	BroadcastSim
Author	Chinmay Kulkarni
Status	Draft
2. Purpose
Objective

The Engineering Formula Specification (EFS) defines the mathematical relationships, dependency rules, and engineering models used by BroadcastSim.

It serves as the single source of truth for all runtime calculations performed during simulation.

This document intentionally separates relationships from constants.

Formula definitions are maintained in this document.
Numerical values and coefficients are maintained in the Engineering Constants Specification (ECS).

This separation ensures that engineering models can be recalibrated without modifying the simulation logic.

3. Scope

The EFS governs the calculation of:

Video signal properties
Device runtime metrics
Device health
Power consumption
Thermal behavior
Signal quality
Network characteristics
Rule dependencies

The document does not define:

UI behavior
REST APIs
Database schema
Device configuration storage
User interactions
4. Engineering Philosophy

BroadcastSim follows these principles.

Deterministic Simulation

The same inputs shall always produce the same outputs.

Random values are prohibited unless an explicit fault injection module is enabled.

Cause and Effect

Every calculated property must be traceable to one or more input properties.

Example

FPS ↑

↓

Bandwidth ↑

↓

CPU ↑

↓

Power ↑

↓

Temperature ↑

↓

Health ↓

No property changes independently.

Device Independence

Each device owns its own engineering model.

Example

Camera

↓

CameraRuleModel

Encoder

↓

EncoderRuleModel

Router

↓

RouterRuleModel

The Rule Engine coordinates execution but does not contain device-specific formulas.

Separation of Concerns

Configuration values shall never be modified by formulas.

Runtime calculations shall never overwrite configuration.

Configuration

↓

Runtime

↓

Snapshot

↓

Timeline

5. Property Classification

Every property belongs to exactly one category.

Configuration Properties

Defined by the user.

Examples

Resolution
FPS
Codec
Bit Depth
Chroma Sampling
Number of Streams
Calculated Properties

Derived from formulas.

Examples

CPU Usage
Memory Usage
Temperature
Power
Bandwidth
Runtime Properties

Generated during execution.

Examples

Device State
Alarm State
Signal Status
Runtime Flags
6. Dependency Graph

Calculations shall always follow the dependency order below.

Configuration

↓

Video Parameters

↓

Bandwidth

↓

CPU

↓

Memory

↓

Power

↓

Temperature

↓

Device Health

↓

Alarm Generation

No calculation may violate this order.

7. Formula Execution Cycle

During each simulation cycle the Rule Engine shall execute formulas in the following sequence.

Signal calculations

↓

Device calculations

↓

Runtime calculations

↓

Health evaluation

↓

Alarm evaluation

↓

Snapshot generation

Each stage operates only on the output of the previous stage.

8. Formula Requirements

Every engineering formula shall satisfy the following requirements.

FR-01

Must be deterministic.

FR-02

Must not contain hardcoded constants.

All coefficients shall originate from ECS.

FR-03

Must be unit-aware.

Inputs and outputs shall define engineering units.

Example

Power

Watt
Temperature

°C
Bandwidth

Mbps/Gbps
FR-04

Must define valid operating ranges.

FR-05

Must define failure conditions where applicable.

FR-06

Must be reversible where appropriate.

Example

Input Bitrate

↓

Compression

↓

Output Bitrate
9. Formula Categories

BroadcastSim formulas are divided into six categories.

Video Formulas

Resolution

FPS

Bitrate

Compression

Device Performance

CPU

Memory

Power

Temperature

Signal Quality

Latency

Packet Loss

Signal Quality

Network

Bandwidth

Link Utilization

Switch Delay

Health

Health Score

Warning Thresholds

Critical Thresholds

Runtime

Snapshots

State Transitions

Timeline

10. Formula Documentation Standard

Every engineering formula shall include the following sections.

Purpose

Why the formula exists.

Inputs

Required properties.

Outputs

Generated properties.

Dependencies

Properties required before execution.

Equation

Mathematical relationship.

Constants

Reference to ECS.

Validation

Permitted ranges.

Example

Worked numerical example.

11. Formula Versioning

Engineering formulas are versioned independently from application code.

Any change affecting simulation output shall increment the EFS version.

12. Future Extensions

Future versions may introduce formulas for:

HDR
Dolby Vision
SMPTE ST 2110
SDI
NDI
Audio
Genlock
PTP
GPU acceleration
Adaptive cooling
Redundant routing

These additions shall not modify existing formulas without version updates.

13. References

This specification is based on general broadcast engineering concepts and publicly available standards.

Future revisions may reference:

SMPTE Standards
EBU Technical Recommendations
DVB Standards
FFmpeg Documentation
Intel Quick Sync
NVIDIA Video Codec SDK
Cisco Networking Documentation
14. Change Log
Version	Changes
1.0	Initial Engineering Formula Specification



