Simulation Lifecycle Specification (SLS)
Document Information
Field	Value
Document	Simulation Lifecycle Specification
Abbreviation	SLS
Version	1.0
Project	BroadcastSim
Status	Draft
1. Purpose

The Simulation Lifecycle Specification defines how BroadcastSim executes one simulation cycle (tick).

Every tick shall execute the same sequence of operations.

The execution order is deterministic and identical for every simulation.

2. Simulation States

The simulator supports the following states.

CREATED

↓

INITIALIZED

↓

RUNNING

↓

PAUSED

↓

STOPPED

State transitions are managed by BroadcastEngine.

3. Tick Model

BroadcastSim uses a fixed tick simulation.

Default Tick Interval

100 ms

Future versions may allow configurable intervals between

100–500 ms
4. Simulation Cycle

Each simulation tick follows this sequence.

Tick Start
    │
    ▼
Process Events
    │
    ▼
Update Device Runtime
    │
    ▼
Execute Rule Engine
    │
    ▼
Propagate Signals
    │
    ▼
Evaluate Device Health
    │
    ▼
Generate Alarms
    │
    ▼
Create Device Snapshots
    │
    ▼
Update Timeline
    │
    ▼
Tick End

Every stage completes before the next begins.

5. Stage 1 – Process Events

Input

User events
Scheduled events
Fault injections

Examples

Connect Device
Disconnect Device
Change FPS
Change Codec
Restart Device

Output

Updated device configuration.

6. Stage 2 – Update Runtime

Update

Runtime timers
Internal counters
Cooling
Recovery timers

No engineering formulas are executed.

7. Stage 3 – Execute Rule Engine

Execute

CameraRuleModel

↓

RouterRuleModel

↓

EncoderRuleModel

Each RuleModel updates

CPU
Memory
Power
Temperature
Bandwidth

No signal propagation occurs here.

8. Stage 4 – Propagate Signals

Execute

SignalPropagationEngine

Responsibilities

Generate new signals
Traverse SignalGraph
Deliver signals
Update signal metadata

No runtime calculations occur.

9. Stage 5 – Evaluate Health

Evaluate

OperationalFailureType
CPU
Memory
Temperature
Power Utilization
Signal Quality

Produce

NORMAL

WARNING

CRITICAL

FAILED

HealthCalculator evaluates OperationalFailureType first. When no operational failure is active, it evaluates the runtime metrics using the precedence FAILED > CRITICAL > WARNING > NORMAL.

No alarms generated yet.

10. Stage 6 – Generate Alarms

Generate

INFO
WARNING
CRITICAL

Based on

Device Health
Runtime Metrics
Signal State

Alarm generation never changes device state.

11. Stage 7 – Create Snapshots

Each device creates

DeviceSnapshot

Contains

Runtime metrics
Device state
Signal information
Timestamp

Snapshots are immutable.

12. Stage 8 – Update Timeline

Append snapshots to

Timeline

Timeline is later used for

Replay
Analytics
Graphs
Debugging
13. Error Handling

If one device fails during processing

Log the error
Preserve previous runtime
Continue simulation

The simulation must never stop because of one device failure.

14. Performance Goals

One simulation cycle should

Scale linearly with device count
Avoid unnecessary allocations
Avoid blocking operations
Avoid reflection

Target complexity

O(n)

where

n = number of devices
15. Future Enhancements

Future versions may introduce

Parallel execution
Multi-threaded Rule Engine
Parallel Signal Propagation
Priority event queues
Distributed simulation
16. Version History
Version	Changes
1.0	Initial Simulation Lifecycle Specification
