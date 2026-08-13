package com.broadcastsim.core.device.runtime;

import com.broadcastsim.core.common.enums.DeviceState;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Validates and applies lifecycle transitions to device runtime state. */
public final class DeviceLifecycleManager {

  private static final Map<DeviceState, Set<DeviceState>> ALLOWED_TRANSITIONS =
      Map.of(
          DeviceState.CREATED, Set.of(DeviceState.ONLINE),
          DeviceState.ONLINE, Set.of(DeviceState.WARNING, DeviceState.OFFLINE),
          DeviceState.WARNING, Set.of(DeviceState.FAILED),
          DeviceState.FAILED, Set.of(DeviceState.RECOVERING),
          DeviceState.RECOVERING, Set.of(DeviceState.ONLINE),
          DeviceState.OFFLINE, Set.of(DeviceState.ONLINE));

  /**
   * Transitions runtime state to a valid next lifecycle state.
   *
   * @param deviceRuntime the runtime state to update
   * @param targetState the requested next state
   * @throws IllegalStateException if the transition is invalid
   */
  public void transition(DeviceRuntime deviceRuntime, DeviceState targetState) {
    DeviceRuntime requiredRuntime =
        Objects.requireNonNull(deviceRuntime, "device runtime must not be null");
    DeviceState requiredTargetState =
        Objects.requireNonNull(targetState, "target state must not be null");
    if (!isValidTransition(requiredRuntime.getDeviceState(), requiredTargetState)) {
      throw new IllegalStateException("invalid device lifecycle transition");
    }
    requiredRuntime.updateDeviceState(requiredTargetState);
  }

  /**
   * Returns whether a lifecycle transition is permitted.
   *
   * @param currentState the current lifecycle state
   * @param targetState the requested next state
   * @return {@code true} when the transition is valid
   */
  public boolean isValidTransition(DeviceState currentState, DeviceState targetState) {
    DeviceState requiredCurrentState =
        Objects.requireNonNull(currentState, "current state must not be null");
    DeviceState requiredTargetState =
        Objects.requireNonNull(targetState, "target state must not be null");
    return ALLOWED_TRANSITIONS
        .getOrDefault(requiredCurrentState, Set.of())
        .contains(requiredTargetState);
  }
}
