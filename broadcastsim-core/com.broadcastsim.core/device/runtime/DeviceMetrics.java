package com.broadcastsim.core.device.runtime;

import lombok.Builder;
import lombok.Value;

/** Immutable collection of the current engineering metrics for a device. */
@Value
@Builder
public class DeviceMetrics {

  double cpuUsagePercentage;
  double memoryUsageMb;
  double temperatureCelsius;
  double powerConsumptionWatts;
  double bandwidthMegabitsPerSecond;
}
