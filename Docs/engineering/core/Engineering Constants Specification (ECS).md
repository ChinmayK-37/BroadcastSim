2. Engineering Constants Specification (ECS)

Replace the coefficient section.

11. Engineering Coefficients
Resolution Weight
Resolution	Weight
SD	1
HD	2
Full HD	4
UHD 4K	8
UHD 8K	16
FPS Weight
FPS	Weight
23.976	1
24	1
25	2
29.97	2
30	2
50	4
59.94	4
60	4
120	8
Codec Weight
Codec	Weight
RAW	1
JPEG2000	2
MPEG-2	3
H.264	5
H.265	8
Stream Weight
StreamWeight = 5
Base CPU
Device	Value
Camera	5
Router	2
Encoder	10
Base Memory
Device	Value
Camera	128 MB
Router	256 MB
Encoder	512 MB
Frame Buffer
FrameBuffer = 64 MB
Codec Buffer
Codec	Buffer
RAW	32 MB
JPEG2000	64 MB
MPEG-2	96 MB
H.264	128 MB
H.265	160 MB
Heat Coefficient
HeatCoefficient = 0.04 °C/W/tick
Cooling Rate
CoolingRate = 0.50 °C/tick
Route Weight
RouteWeight = 2
Utilization Weight
UtilizationWeight = 0.10
Loss Weight
LossWeight = 2
Latency Weight
LatencyWeight = 0.10
Queue Delay
QueueDelay = 0.02 ms
Base Latency
BaseLatency = 0.20 ms
Switch Delay
SwitchDelay = 0.10 ms


Health Threshold Summary
Metric	Normal	Warning	Critical
CPU	<80	80–95	>95
Memory	<80	80–95	>95
Temperature	<70°C	70–85°C	>85°C
Power Utilization	<80	80–95	>95
Signal Quality	>95	80–95	<80

Now every calculator and HealthCalculator has one authoritative table.


Engineering Constants Specification (ECS)
Document Information
Field	Value
Document	Engineering Constants Specification
Abbreviation	ECS
Version	1.0
Project	BroadcastSim
Status	Draft
1. Purpose

The Engineering Constants Specification (ECS) defines every configurable engineering constant used by BroadcastSim.

No formula shall contain hardcoded numerical values.

Every coefficient, threshold, lookup table, scaling factor, and engineering limit must originate from this specification.

2. Video Standards

Supported Resolutions

Name	Width	Height
SD	720	576
HD	1280	720
Full HD	1920	1080
UHD 4K	3840	2160
UHD 8K	7680	4320

Supported Frame Rates
FPS
23.976
24
25
29.97
30
50
59.94
60
120

Bit Depth
Format	Bits
8-bit	8
10-bit	10
12-bit	12

Chroma Sampling

Format	Approx. Bits/Pixel
4:2:0	12
4:2:2	16
4:4:4	24

3. Codec Constants
Codec	Compression Ratio	CPU Weight	Memory Weight
RAW	1	0.5	0.5
JPEG2000	6	1.2	1.1
MPEG-2	20	1.5	1.3
H.264	60	2.0	1.8
H.265	120	3.0	2.5

CPU and Memory weights are relative factors, not percentages.

4. Camera Constants
Constant	Value
Idle Power	5 W
Maximum Power	15 W
Ambient Temperature	25°C
Cooling Rate	0.50°C / Tick
Heat Factor	0.04°C / Watt

5. Encoder Constants
Constant	Value
Idle CPU	5%
Idle Memory	512 MB
Idle Power	20 W
Maximum Power	80 W
Cooling Rate	0.80°C / Tick
Heat Factor	0.06°C / Watt

6. Router Constants
Constant	Value
Idle CPU	2%
Idle Memory	256 MB
Idle Power	15 W
Maximum Power	50 W
Maximum Ports	64
Power per Active Port	0.4 W
Cooling Rate	0.60°C / Tick

7. Network Constants
Constant	Value
Base Latency	0.20 ms
Switch Delay	0.10 ms
Queue Delay Factor	0.02 ms
Packet Loss (Ideal)	0%

8. Broadcast Interfaces
Interface	Capacity
SD-SDI	270 Mbps
HD-SDI	1.485 Gbps
3G-SDI	2.97 Gbps
6G-SDI	5.94 Gbps
12G-SDI	11.88 Gbps
25GbE	25 Gbps
40GbE	40 Gbps
100GbE	100 Gbps

9. Health Thresholds
CPU Usage
State	Range
NORMAL	0–80%
WARNING	80–95%
CRITICAL	>95%
Temperature
State	Range
NORMAL	0–70°C
WARNING	70–85°C
CRITICAL	>85°C
Signal Quality
State	Range
EXCELLENT	95–100%
GOOD	80–95%
DEGRADED	60–80%
LOST	<60%

10. Runtime Limits
Property	Min	Max
CPU	0%	100%
Memory	0 MB	Unlimited
Temperature	Ambient	120°C
Power	Idle	Maximum
Signal Quality	0%	100%
Packet Loss	0%	100%
Latency	0 ms	Unlimited

11. Formula Coefficients

The following coefficients shall be referenced by formulas.

FPS CPU Weight

FPS	Weight
23.976	1
24	1
25	2
29.97	2
30	2
50	4
59.94	4
60	4
120	8

FPS CPU Weight is used only by the encoder CPU formula.

Name	Purpose
ResolutionFactor	Resolution scaling
FPSFactor	Frame-rate scaling
CodecFactor	Codec complexity
StreamFactor	Multiple stream scaling
PowerCoefficient	CPU → Power
HeatCoefficient	Power → Temperature
CoolingCoefficient	Temperature reduction
BandwidthFactor	Throughput scaling

The numeric values may be refined in later versions without changing formula definitions.

12. Future Expansion

Reserved for:

HDR
Dolby Vision
SMPTE ST 2110
NDI
Audio streams
PTP
Genlock
GPU acceleration
Multi-channel encoding
Redundant paths

13. Version History
Version	Changes
1.0	Initial engineering constants
