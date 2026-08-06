Engineering Data Dictionary (EDD)
Document Information
Field	Value
Document	Engineering Data Dictionary
Abbreviation	EDD
Version	1.0
Project	BroadcastSim
Status	Draft
1. Purpose

The Engineering Data Dictionary (EDD) defines every engineering term used throughout BroadcastSim.

Its objectives are:

Remove ambiguity
Standardize terminology
Define units
Define symbols
Specify valid ranges
Specify applicable devices

Every engineering document shall reference these definitions.

2. Property Definition Standard

Every engineering property shall define:

Name
Symbol
Unit
Data Type
Category
Description
Applicable Devices
Typical Range

3. Video Properties
Resolution
Field	Value
Symbol	R
Unit	pixels
Type	Configuration
Data Type	Resolution
Description	Width × Height of the video frame
Devices	Camera, Encoder, Decoder
Frame Rate
Field	Value
Symbol	FPS
Unit	frames/sec
Type	Configuration
Data Type	Double
Description	Number of frames produced every second
Devices	Camera, Encoder
Bit Depth
Field	Value
Symbol	BD
Unit	bits
Type	Configuration
Description	Number of bits used per color sample
Devices	Camera
Chroma Sampling
Field	Value
Symbol	CS
Unit	Ratio
Type	Configuration
Description	Color subsampling format (4:2:0, 4:2:2, 4:4:4)
Devices	Camera, Encoder
Codec
Field	Value
Symbol	C
Unit	N/A
Type	Configuration
Description	Compression algorithm
Devices	Encoder, Decoder

4. Signal Properties
Bitrate
Field	Value
Symbol	BR
Unit	Mbps / Gbps
Type	Calculated
Description	Amount of data transmitted per second
Devices	Camera, Encoder, Router
Bandwidth
Field	Value
Symbol	BW
Unit	Mbps / Gbps
Type	Calculated
Description	Required transmission capacity
Devices	Camera, Router
Signal Quality
Field	Value
Symbol	SQ
Unit	%
Type	Runtime
Description	Overall quality score of a signal
Devices	All
Latency
Field	Value
Symbol	L
Unit	ms
Type	Runtime
Description	Time taken for signal delivery
Devices	Router, Encoder
Packet Loss
Field	Value
Symbol	PL
Unit	%
Type	Runtime
Description	Percentage of packets lost during transmission
Devices	Router

5. Performance Properties
CPU Usage
Field	Value
Symbol	CPU
Unit	%
Type	Calculated
Description	Processor utilization
Devices	Encoder, Router
Memory Usage
Field	Value
Symbol	MEM
Unit	MB
Type	Calculated
Description	Runtime memory consumption
Devices	Encoder, Router
Power Consumption
Field	Value
Symbol	P
Unit	Watt
Type	Calculated
Description	Instantaneous electrical power usage
Devices	All
Temperature
Field	Value
Symbol	T
Unit	°C
Type	Calculated
Description	Device operating temperature
Devices	All

6. Network Properties
Link Capacity

Maximum throughput supported by a connection.

Unit

Gbps
Link Utilization

Percentage of current bandwidth compared to link capacity.

Unit

%
Active Routes

Number of active routes inside a router.

Unit

count

7. Runtime Properties
Device State

Lifecycle state.

Examples

CREATED
ONLINE
WARNING
FAILED
RECOVERING
OFFLINE
Runtime Flag

Temporary execution state.

Examples

OVERHEATED
DISCONNECTED
LOW_POWER
OperationalFailureType

Active operational failure condition evaluated by HealthCalculator before runtime metric thresholds.

Examples

POWER_UNAVAILABLE
THERMAL_PROTECTION_SHUTDOWN
RUNTIME_WATCHDOG_TERMINATION
MEMORY_ALLOCATION_FAILURE
REQUIRED_INPUT_SIGNAL_UNAVAILABLE
EXPLICIT_DEVICE_FAULT_INJECTION
Health

Overall device health.

Examples

NORMAL
WARNING
CRITICAL
FAILED

8. Naming Conventions
Property	Convention
Class	PascalCase
Variable	camelCase
Enum	UPPER_CASE
Units	SI Units wherever possible
IDs	Value Objects

9. Unit Standards
Quantity	Unit
Temperature	°C
Power	W
CPU	%
Memory	MB
Bandwidth	Mbps / Gbps
Latency	ms
Frame Rate	fps
Resolution	pixels

10. References

All future engineering documents shall use these definitions.

No document may redefine an existing engineering property.
