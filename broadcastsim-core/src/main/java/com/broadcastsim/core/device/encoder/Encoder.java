package com.broadcastsim.core.device.encoder;

import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.signal.Codec;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.Objects;
import java.util.Optional;

/** Applies configured codec and bitrate metadata to an input logical signal. */
public final class Encoder extends AbstractDevice {

  private Signal inputSignal;

  /**
   * Creates an encoder with one input port and one output port.
   *
   * @param deviceId the encoder identifier
   * @param deviceProfile the encoder profile
   * @param deviceRuntime the encoder runtime state
   * @param inputPort the encoder input port
   * @param outputPort the encoder output port
   */
  public Encoder(
      DeviceId deviceId,
      DeviceProfile deviceProfile,
      DeviceRuntime deviceRuntime,
      Port inputPort,
      Port outputPort) {
    super(deviceId, deviceProfile, deviceRuntime);
    if (getDeviceType() != DeviceType.ENCODER) {
      throw new IllegalArgumentException("encoder requires an encoder profile");
    }
    addInputPort(Objects.requireNonNull(inputPort, "input port must not be null"));
    addOutputPort(Objects.requireNonNull(outputPort, "output port must not be null"));
  }

  /**
   * Stores the current input signal for metadata encoding.
   *
   * @param signal the signal to encode
   */
  @Override
  public void receiveSignal(Signal signal) {
    inputSignal = Objects.requireNonNull(signal, "signal must not be null");
  }

  /**
   * Returns the configured output codec.
   *
   * @return configured codec
   */
  public Codec getCodec() {
    return getPropertyValue(PropertyKey.CODEC, Codec.class);
  }

  /**
   * Returns the configured target bitrate.
   *
   * @return target bitrate in megabits per second
   */
  public double getTargetBitrateMegabitsPerSecond() {
    return getPropertyValue(PropertyKey.BITRATE, Double.class);
  }

  /**
   * Returns the current input signal without exposing mutable device state.
   *
   * @return current input signal, if available
   */
  public Optional<Signal> getCurrentInputSignal() {
    return Optional.ofNullable(inputSignal);
  }

  /**
   * Applies configured codec and bitrate values to the current input signal.
   *
   * @return the metadata-modified signal, if an input signal is available
   */
  @Override
  public Optional<Signal> generateSignal() {
    if (inputSignal == null) {
      return Optional.empty();
    }
    inputSignal.setCodec(getCodec());
    inputSignal.setBitrateMegabitsPerSecond(getTargetBitrateMegabitsPerSecond());
    return Optional.of(inputSignal);
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
