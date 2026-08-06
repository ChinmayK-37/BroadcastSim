package com.broadcastsim.core.engineering.rules;

import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/** Immutable outcome of a deterministic device rule-model execution. */
@Value
@Builder
public class RuleExecutionResult {

  DeviceId deviceId;
  RuleModelName ruleModelName;
  ExecutionStatus executionStatus;
  DeviceRuntime updatedDeviceRuntime;
  ValidationStatus validationStatus;
  Instant executionTimestamp;
}
