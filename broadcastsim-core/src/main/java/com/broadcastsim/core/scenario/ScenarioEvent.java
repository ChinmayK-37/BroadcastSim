package com.broadcastsim.core.scenario;

import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.valueobject.DeviceId;
import com.broadcastsim.core.valueobject.EventId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/** Represents one deterministic configuration or device-state change scheduled in a scenario. */
@Getter
public final class ScenarioEvent {

  private final EventId eventId;
  private final Instant scheduledTimestamp;
  private final DeviceId targetDeviceId;
  private final ScenarioEventType eventType;
  private final PropertyKey propertyKey;
  private final Object propertyValue;
  private ScenarioEventStatus executionStatus;

  private ScenarioEvent(
      Instant scheduledTimestamp,
      DeviceId targetDeviceId,
      ScenarioEventType eventType,
      PropertyKey propertyKey,
      Object propertyValue) {
    this.eventId = EventId.generate();
    this.scheduledTimestamp = requireValidTimestamp(scheduledTimestamp);
    this.targetDeviceId =
        Objects.requireNonNull(targetDeviceId, "target device id must not be null");
    this.eventType = Objects.requireNonNull(eventType, "event type must not be null");
    this.propertyKey = propertyKey;
    this.propertyValue = propertyValue;
    this.executionStatus = ScenarioEventStatus.PENDING;
  }

  /**
   * Schedules a writable configuration-property change.
   *
   * @param scheduledTimestamp the simulation time at which to apply the change
   * @param targetDeviceId the device whose property will change
   * @param propertyKey the property to update
   * @param propertyValue the replacement property value
   * @return a pending property-change event
   */
  public static ScenarioEvent setProperty(
      Instant scheduledTimestamp,
      DeviceId targetDeviceId,
      PropertyKey propertyKey,
      Object propertyValue) {
    return new ScenarioEvent(
        scheduledTimestamp,
        targetDeviceId,
        ScenarioEventType.SET_PROPERTY,
        Objects.requireNonNull(propertyKey, "property key must not be null"),
        Objects.requireNonNull(propertyValue, "property value must not be null"));
  }

  /**
   * Schedules transition of a disabled device to the online state.
   *
   * @param scheduledTimestamp the simulation time at which to enable the device
   * @param targetDeviceId the device to enable
   * @return a pending enable event
   */
  public static ScenarioEvent enableDevice(Instant scheduledTimestamp, DeviceId targetDeviceId) {
    return new ScenarioEvent(
        scheduledTimestamp, targetDeviceId, ScenarioEventType.DEVICE_ENABLE, null, null);
  }

  /**
   * Schedules transition of an online device to the offline state.
   *
   * @param scheduledTimestamp the simulation time at which to disable the device
   * @param targetDeviceId the device to disable
   * @return a pending disable event
   */
  public static ScenarioEvent disableDevice(Instant scheduledTimestamp, DeviceId targetDeviceId) {
    return new ScenarioEvent(
        scheduledTimestamp, targetDeviceId, ScenarioEventType.DEVICE_DISABLE, null, null);
  }

  void markExecuted() {
    executionStatus = ScenarioEventStatus.EXECUTED;
  }

  void markFailed() {
    executionStatus = ScenarioEventStatus.FAILED;
  }

  private static Instant requireValidTimestamp(Instant scheduledTimestamp) {
    Instant requiredTimestamp =
        Objects.requireNonNull(scheduledTimestamp, "scheduled timestamp must not be null");
    if (requiredTimestamp.isBefore(Instant.EPOCH)) {
      throw new IllegalArgumentException("scheduled timestamp must not precede simulation start");
    }
    return requiredTimestamp;
  }
}
