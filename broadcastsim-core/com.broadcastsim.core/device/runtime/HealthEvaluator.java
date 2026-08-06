package com.broadcastsim.core.device.runtime;

import com.broadcastsim.core.common.enums.DeviceState;
import java.util.Objects;

/** Determines a device lifecycle state from its measured CPU and temperature values. */
public final class HealthEvaluator {

  private static final double CPU_WARNING_THRESHOLD_PERCENTAGE = 80.0;
  private static final double CPU_FAILURE_THRESHOLD_PERCENTAGE = 95.0;
  private static final double TEMPERATURE_WARNING_THRESHOLD_CELSIUS = 70.0;
  private static final double TEMPERATURE_FAILURE_THRESHOLD_CELSIUS = 85.0;

  /**
   * Evaluates the current state implied by a device's metrics.
   *
   * @param metrics the device metrics to evaluate
   * @return the resulting online, warning, or failed state
   */
  public DeviceState evaluate(DeviceMetrics metrics) {
    DeviceMetrics requiredMetrics = Objects.requireNonNull(metrics, "metrics must not be null");
    if (isFailed(requiredMetrics)) {
      return DeviceState.FAILED;
    }
    if (isWarning(requiredMetrics)) {
      return DeviceState.WARNING;
    }
    return DeviceState.ONLINE;
  }

  private boolean isFailed(DeviceMetrics metrics) {
    return metrics.getCpuUsagePercentage() >= CPU_FAILURE_THRESHOLD_PERCENTAGE
        || metrics.getTemperatureCelsius() >= TEMPERATURE_FAILURE_THRESHOLD_CELSIUS;
  }

  private boolean isWarning(DeviceMetrics metrics) {
    return metrics.getCpuUsagePercentage() >= CPU_WARNING_THRESHOLD_PERCENTAGE
        || metrics.getTemperatureCelsius() >= TEMPERATURE_WARNING_THRESHOLD_CELSIUS;
  }
}
