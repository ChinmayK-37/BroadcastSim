package com.broadcastsim.core.alarm;

import com.broadcastsim.core.common.enums.AlarmSeverity;
import com.broadcastsim.core.common.enums.AlarmState;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.OperationalFailureType;
import com.broadcastsim.core.valueobject.AlarmId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

/** Immutable alarm state for one device health condition. */
@Value
public class Alarm {

  AlarmId alarmId;
  DeviceId deviceId;
  AlarmSeverity severity;
  AlarmState state;
  HealthStatus healthStatus;
  Set<OperationalFailureType> operationalFailures;
  Instant raisedAt;
  Instant clearedAt;

  /**
   * Creates an immutable alarm state.
   *
   * @param alarmId unique alarm identity
   * @param deviceId affected device
   * @param severity current alarm severity
   * @param state current alarm lifecycle state
   * @param healthStatus health condition represented by the alarm
   * @param operationalFailures active operational failures associated with the alarm
   * @param raisedAt simulation time at which the alarm was raised
   * @param clearedAt simulation time at which the alarm was cleared, if applicable
   */
  @Builder
  public Alarm(
      AlarmId alarmId,
      DeviceId deviceId,
      AlarmSeverity severity,
      AlarmState state,
      HealthStatus healthStatus,
      Set<OperationalFailureType> operationalFailures,
      Instant raisedAt,
      Instant clearedAt) {
    this.alarmId = alarmId;
    this.deviceId = deviceId;
    this.severity = severity;
    this.state = state;
    this.healthStatus = healthStatus;
    this.operationalFailures = Set.copyOf(operationalFailures);
    this.raisedAt = raisedAt;
    this.clearedAt = clearedAt;
  }
}
