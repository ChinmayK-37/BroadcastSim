package com.broadcastsim.core.alarm;

import com.broadcastsim.core.common.enums.AlarmSeverity;
import com.broadcastsim.core.common.enums.AlarmState;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.valueobject.AlarmId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Converts calculated device health conditions into deterministic alarm lifecycle state. */
public final class AlarmEngine {

  private final Map<DeviceId, Alarm> alarmsByDeviceId = new LinkedHashMap<>();

  /**
   * Evaluates the supplied devices and updates their alarm states.
   *
   * @param devices the devices whose health states will be translated into alarms
   * @param timestamp the deterministic simulation timestamp
   * @return all known alarms, including alarms cleared during this evaluation
   */
  public List<Alarm> evaluate(Collection<Device> devices, Instant timestamp) {
    Objects.requireNonNull(devices, "devices must not be null");
    Instant requiredTimestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    for (Device device : devices) {
      if (device instanceof AbstractDevice abstractDevice) {
        evaluateDevice(abstractDevice, requiredTimestamp);
      }
    }
    return List.copyOf(alarmsByDeviceId.values());
  }

  private void evaluateDevice(AbstractDevice device, Instant timestamp) {
    DeviceRuntime runtime = device.getDeviceRuntime();
    if (runtime.getHealthStatus() == HealthStatus.NORMAL) {
      clearAlarm(device.getDeviceId(), timestamp);
      return;
    }
    raiseOrUpdateAlarm(device.getDeviceId(), runtime, timestamp);
  }

  private void raiseOrUpdateAlarm(DeviceId deviceId, DeviceRuntime runtime, Instant timestamp) {
    Alarm existingAlarm = alarmsByDeviceId.get(deviceId);
    AlarmId alarmId = existingAlarm == null ? AlarmId.generate() : existingAlarm.getAlarmId();
    Instant raisedAt = existingAlarm == null ? timestamp : existingAlarm.getRaisedAt();
    alarmsByDeviceId.put(
        deviceId,
        Alarm.builder()
            .alarmId(alarmId)
            .deviceId(deviceId)
            .severity(severityFor(runtime.getHealthStatus()))
            .state(AlarmState.RAISED)
            .healthStatus(runtime.getHealthStatus())
            .operationalFailures(runtime.getOperationalFailures())
            .raisedAt(raisedAt)
            .build());
  }

  private void clearAlarm(DeviceId deviceId, Instant timestamp) {
    Alarm existingAlarm = alarmsByDeviceId.get(deviceId);
    if (existingAlarm == null || existingAlarm.getState() == AlarmState.CLEARED) {
      return;
    }
    alarmsByDeviceId.put(
        deviceId,
        Alarm.builder()
            .alarmId(existingAlarm.getAlarmId())
            .deviceId(existingAlarm.getDeviceId())
            .severity(existingAlarm.getSeverity())
            .state(AlarmState.CLEARED)
            .healthStatus(HealthStatus.NORMAL)
            .operationalFailures(Set.of())
            .raisedAt(existingAlarm.getRaisedAt())
            .clearedAt(timestamp)
            .build());
  }

  private AlarmSeverity severityFor(HealthStatus healthStatus) {
    return switch (healthStatus) {
      case WARNING -> AlarmSeverity.WARNING;
      case CRITICAL, FAILED -> AlarmSeverity.CRITICAL;
      case NORMAL -> throw new IllegalArgumentException("normal health does not produce an alarm");
    };
  }
}
