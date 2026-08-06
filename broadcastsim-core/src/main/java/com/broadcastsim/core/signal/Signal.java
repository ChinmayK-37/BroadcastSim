package com.broadcastsim.core.signal;

import com.broadcastsim.core.common.enums.SignalStatus;
import com.broadcastsim.core.valueobject.DeviceId;
import com.broadcastsim.core.valueobject.SignalId;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Represents mutable metadata for one logical broadcast signal in transit. */
@Getter
@Setter
public final class Signal {

  private final SignalId signalId;
  private final DeviceId sourceDeviceId;
  private Resolution resolution;
  private double framesPerSecond;
  private Codec codec;
  private double bitrateMegabitsPerSecond;
  private double qualityPercentage;
  private Duration latency;
  private SignalStatus status;

  /**
   * Creates a mutable logical signal with the supplied metadata.
   *
   * @param signalId the signal identifier
   * @param sourceDeviceId the originating device identifier
   * @param resolution the video resolution
   * @param framesPerSecond the frame rate
   * @param codec the signal codec
   * @param bitrateMegabitsPerSecond the bitrate in megabits per second
   * @param qualityPercentage the quality percentage
   * @param latency the logical signal latency
   * @param status the signal status
   */
  @Builder
  public Signal(
      SignalId signalId,
      DeviceId sourceDeviceId,
      Resolution resolution,
      double framesPerSecond,
      Codec codec,
      double bitrateMegabitsPerSecond,
      double qualityPercentage,
      Duration latency,
      SignalStatus status) {
    this.signalId = signalId;
    this.sourceDeviceId = sourceDeviceId;
    this.resolution = resolution;
    this.framesPerSecond = framesPerSecond;
    this.codec = codec;
    this.bitrateMegabitsPerSecond = bitrateMegabitsPerSecond;
    this.qualityPercentage = qualityPercentage;
    this.latency = latency;
    this.status = status;
  }
}
