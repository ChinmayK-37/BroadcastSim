package com.broadcastsim.core.device.camera;

import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.SignalStatus;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.signal.ChromaSampling;
import com.broadcastsim.core.signal.Codec;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import com.broadcastsim.core.valueobject.SignalId;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** Produces logical, unencoded broadcast signals from camera configuration properties. */
public final class Camera extends AbstractDevice {

  private static final double RAW_SIGNAL_BITRATE_MEGABITS_PER_SECOND = 3000.0;

  /**
   * Creates a camera with one configured output port.
   *
   * @param deviceId the camera identifier
   * @param deviceProfile the camera profile
   * @param deviceRuntime the camera runtime state
   * @param outputPort the camera output port
   */
  public Camera(
      DeviceId deviceId,
      DeviceProfile deviceProfile,
      DeviceRuntime deviceRuntime,
      Port outputPort) {
    super(deviceId, deviceProfile, deviceRuntime);
    if (getDeviceType() != DeviceType.CAMERA) {
      throw new IllegalArgumentException("camera requires a camera profile");
    }
    addOutputPort(Objects.requireNonNull(outputPort, "output port must not be null"));
  }

  /**
   * Rejects incoming signals because cameras are signal producers.
   *
   * @param signal the unsupported incoming signal
   */
  @Override
  public void receiveSignal(Signal signal) {
    throw new UnsupportedOperationException("camera does not accept incoming signals");
  }

  /**
   * Returns the configured output resolution.
   *
   * @return configured resolution
   */
  public Resolution getResolution() {
    return getPropertyValue(PropertyKey.RESOLUTION, Resolution.class);
  }

  /**
   * Returns the configured frame rate.
   *
   * @return configured frames per second
   */
  public double getFramesPerSecond() {
    return getPropertyValue(PropertyKey.FPS, Double.class);
  }

  /**
   * Returns the configured bit depth.
   *
   * @return configured bit depth in bits
   */
  public int getBitDepth() {
    return getPropertyValue(PropertyKey.BIT_DEPTH, Integer.class);
  }

  /**
   * Returns the configured chroma-sampling format.
   *
   * @return configured chroma-sampling format
   */
  public ChromaSampling getChromaSampling() {
    return getPropertyValue(PropertyKey.CHROMA_SAMPLING, ChromaSampling.class);
  }

  /**
   * Creates a logical raw signal from the camera's configurable properties.
   *
   * @return the generated signal
   */
  @Override
  public Optional<Signal> generateSignal() {
    return Optional.of(
        Signal.builder()
            .signalId(SignalId.generate())
            .sourceDeviceId(getDeviceId())
            .resolution(getResolution())
            .framesPerSecond(getFramesPerSecond())
            .codec(Codec.RAW)
            .bitrateMegabitsPerSecond(RAW_SIGNAL_BITRATE_MEGABITS_PER_SECOND)
            .qualityPercentage(getPropertyValue(PropertyKey.SIGNAL_QUALITY, Double.class))
            .latency(Duration.ZERO)
            .status(SignalStatus.ACTIVE)
            .build());
  }

  /** Does not handle device events during the initial device behavior milestone. */
  @Override
  public void applyEvent() {}

  /** Does not apply time-based updates during the initial device behavior milestone. */
  @Override
  public void update() {}

  /** Does not calculate rules during the initial device behavior milestone. */
  @Override
  public void calculate() {}
}
