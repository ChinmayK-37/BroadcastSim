package com.broadcastsim.core.rule;

import com.broadcastsim.core.engineering.rules.ExecutionStatus;
import com.broadcastsim.core.engineering.rules.RuleExecutionResult;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.List;

/** Aggregates the outcomes of one deterministic RuleEngine execution cycle. */
public record RuleExecutionReport(
    Instant executionTimestamp,
    List<RuleExecutionResult> results,
    List<DeviceId> unsupportedDeviceIds) {

  /**
   * Creates an immutable report.
   *
   * @param executionTimestamp the timestamp supplied for this execution cycle
   * @param results rule-model results in deterministic device processing order
   * @param unsupportedDeviceIds registered devices without an MVP rule model
   */
  public RuleExecutionReport {
    results = List.copyOf(results);
    unsupportedDeviceIds = List.copyOf(unsupportedDeviceIds);
  }

  /**
   * Returns whether every supported-device rule execution succeeded.
   *
   * @return {@code true} when no execution result reports failure
   */
  public boolean isSuccessful() {
    return results.stream()
        .allMatch(result -> result.getExecutionStatus() == ExecutionStatus.SUCCESS);
  }
}
