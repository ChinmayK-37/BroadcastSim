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
@Builder
public class Alarm {

  AlarmId alarmId;
  DeviceId deviceId;
  AlarmSeverity severity;
  AlarmState state;
  HealthStatus healthStatus;
  Set<OperationalFailureType> operationalFailures;
  Instant raisedAt;
  Instant clearedAt;
}
