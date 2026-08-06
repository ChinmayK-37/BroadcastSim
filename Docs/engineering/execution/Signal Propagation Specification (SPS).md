Signal Propagation Specification (SPS)
Document Information
Field	Value
Document	Signal Propagation Specification
Abbreviation	SPS
Version	1.0
Project	BroadcastSim
Status	Draft
1. Purpose

This specification defines how logical broadcast signals travel through the BroadcastSim network.

The specification covers

Signal creation
Signal propagation
Signal transformation
Signal termination
Signal failure
Signal validation

No real audio or video is processed.

Signals represent engineering metadata only.

2. Signal Definition

A Signal represents a logical broadcast stream.

Each signal contains

Signal ID
Source Device
Resolution
Frame Rate
Bit Depth
Chroma Sampling
Codec
Bitrate
Signal Quality
Latency
Timestamp

Signals are mutable because downstream devices may modify metadata.

3. Signal Lifecycle

Every signal follows

CREATED

↓

PROPAGATING

↓

PROCESSED

↓

FORWARDED

↓

TERMINATED

A terminated signal no longer participates in propagation.

4. Signal Producers

Version 1

Device	Action
Camera	Generate Signal

Future

Media Server
Replay Server
Graphics Engine
5. Signal Consumers
Device	Action
Encoder	Consume + Modify
Viewer	Consume
Recorder	Consume
6. Signal Forwarders
Device	Action
Video Router	Forward

The router does not modify signal content.

7. Propagation Order

Signal propagation shall follow the topology defined by SignalGraph.

Example

Camera

↓

Video Router

↓

Encoder

↓

Viewer

Traversal order shall be deterministic.

8. Propagation Algorithm

The propagation engine shall

Identify signal-producing devices.
Generate logical signals.
Traverse SignalGraph.
Deliver signals to downstream devices.
Allow devices to modify or forward signals.
Continue until no downstream devices remain.
9. Device Behaviour
Camera

Generate a new Signal.

Video Router

Forward incoming Signal.

Do not modify

Resolution
FPS
Codec
Bitrate
Encoder

Modify

Codec
Bitrate
Compression Ratio

Preserve

Source Device
Resolution
FPS
Timestamp
10. Signal Modification Rules

Only processing devices may modify signal metadata.

Routing devices shall never modify signal metadata.

Source devices create new signals.

11. Multiple Outputs

A signal may be propagated to multiple downstream devices.

Example

Camera
      │
      ▼
Video Router
   │      │
   ▼      ▼
Encoder  Viewer

Each downstream path receives its own logical signal instance.

12. Connection Failure

If a connection becomes unavailable

Camera

↓

Router

X

Encoder

The propagation engine shall

Stop propagation along the failed path
Continue propagation along healthy paths
Report the failure in SignalPropagationResult
13. Device Failure

If a downstream device is OFFLINE or FAILED

Signal delivery is skipped
Upstream propagation continues
Failure is recorded
14. Cycle Detection

SignalGraph shall detect routing cycles.

Example

Router A

↓

Router B

↓

Router A

Cycles shall be reported.

Infinite propagation is prohibited.

15. Signal Validation

Before propagation

Signal must exist
Required metadata must be valid
Source device must be ONLINE

After propagation

Metadata shall remain valid
Signal Quality shall remain within limits
Bitrate shall be non-negative
16. Signal Quality

Version 1

Signal Quality is affected by

Device failures
Packet loss
High latency

Future versions may include

Bit errors
Noise
Link degradation
Jitter
17. Performance Requirements

Propagation shall

Execute once per simulation tick
Scale linearly with the number of active connections
Avoid duplicate traversal
Avoid infinite loops

Target complexity

O(V + E)

where

V = Devices
E = Connections
18. Future Enhancements

Future versions may support

SMPTE ST 2110 multicast flows
SDI crosspoint routing
Redundant signal paths
Hitless switching
NMOS IS-04 / IS-05 discovery
Audio signal propagation
Ancillary data propagation
Network congestion simulation
19. Version History
Version	Changes
1.0	Initial Signal Propagation Specification