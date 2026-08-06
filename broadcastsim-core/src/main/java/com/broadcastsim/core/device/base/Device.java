package com.broadcastsim.core.device.base;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.Optional;

/** Defines the lifecycle contract shared by all simulated broadcast devices. */
public interface Device {

  /** Initializes the device with its profile defaults. */
  void initialize();

  /** Updates the device for one simulation tick. */
  void update();

  /** Recalculates dependent device values. */
  void calculate();

  /**
   * Receives an upstream logical signal.
   *
   * @param signal the received signal
   */
  void receiveSignal(Signal signal);

  /**
   * Generates the device's current logical output signal, when one is available.
   *
   * @return the generated signal, if available
   */
  Optional<Signal> generateSignal();

  /** Applies an event when the event model is introduced. */
  void applyEvent();

  /**
   * Returns the unique identifier of this device.
   *
   * @return the device identifier
   */
  DeviceId getDeviceId();

  /**
   * Returns the device category.
   *
   * @return the device type
   */
  DeviceType getDeviceType();

  /**
   * Returns the device lifecycle state.
   *
   * @return the device state
   */
  DeviceState getDeviceState();
}
