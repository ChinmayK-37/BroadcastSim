package com.broadcastsim.core.engineering.rules;

import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.NetworkConstants;
import com.broadcastsim.core.engineering.constants.TemperatureConstants;
import java.util.Objects;

/** Validates calculated runtime metrics against caller-supplied engineering limits. */
final class RuntimeMetricValidator {

  private RuntimeMetricValidator() {}

  static void validate(
      DeviceMetrics metrics,
      double ambientTemperatureCelsius,
      double idlePowerWatts,
      double maximumPowerWatts) {
    DeviceMetrics requiredMetrics = Objects.requireNonNull(metrics, "metrics must not be null");
    if (!Double.isFinite(requiredMetrics.getCpuUsagePercentage())
        || requiredMetrics.getCpuUsagePercentage() < CpuConstants.MINIMUM_CPU_PERCENTAGE
        || requiredMetrics.getCpuUsagePercentage() > CpuConstants.MAXIMUM_CPU_PERCENTAGE
        || !Double.isFinite(requiredMetrics.getMemoryUsageMb())
        || requiredMetrics.getMemoryUsageMb() < MemoryConstants.MINIMUM_MEMORY_MEGABYTES
        || !Double.isFinite(requiredMetrics.getTemperatureCelsius())
        || requiredMetrics.getTemperatureCelsius() < ambientTemperatureCelsius
        || requiredMetrics.getTemperatureCelsius()
            > TemperatureConstants.MAXIMUM_TEMPERATURE_CELSIUS
        || !Double.isFinite(requiredMetrics.getPowerConsumptionWatts())
        || requiredMetrics.getPowerConsumptionWatts() < idlePowerWatts
        || requiredMetrics.getPowerConsumptionWatts() > maximumPowerWatts
        || !Double.isFinite(requiredMetrics.getBandwidthMegabitsPerSecond())
        || requiredMetrics.getBandwidthMegabitsPerSecond() < NetworkConstants.MINIMUM_PERCENTAGE) {
      throw new IllegalArgumentException(
          "calculated runtime metrics are outside engineering limits");
    }
  }
}
