package com.broadcastsim.core.engineering.rules;

import com.broadcastsim.core.device.router.VideoRouter;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.calculators.BandwidthCalculator;
import com.broadcastsim.core.engineering.calculators.CpuCalculator;
import com.broadcastsim.core.engineering.calculators.MemoryCalculator;
import com.broadcastsim.core.engineering.calculators.PowerCalculator;
import com.broadcastsim.core.engineering.calculators.TemperatureCalculator;
import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;
import com.broadcastsim.core.engineering.constants.TemperatureConstants;
import java.time.Instant;
import java.util.Objects;

/** Calculates video-router runtime metrics using the reusable engineering calculators. */
public final class VideoRouterRuleModel {

  /** Executes the model using the runtime timestamp as a deterministic execution timestamp. */
  public RuleExecutionResult execute(VideoRouter videoRouter) {
    Objects.requireNonNull(videoRouter, "video router must not be null");
    return execute(videoRouter, videoRouter.getDeviceRuntime().getLastUpdated());
  }

  /** Executes the video-router engineering model at the supplied simulation timestamp. */
  public RuleExecutionResult execute(VideoRouter videoRouter, Instant executionTimestamp) {
    VideoRouter requiredRouter =
        Objects.requireNonNull(videoRouter, "video router must not be null");
    Instant requiredTimestamp =
        Objects.requireNonNull(executionTimestamp, "execution timestamp must not be null");
    DeviceRuntime runtime = requiredRouter.getDeviceRuntime();
    try {
      validateInputs(requiredRouter);
      double throughput = requiredRouter.getCurrentThroughputMegabitsPerSecond();
      double utilization =
          BandwidthCalculator.calculateLinkUtilization(
              throughput, requiredRouter.getLinkCapacityMegabitsPerSecond());
      double cpu =
          CpuCalculator.calculateRouterCpuUsage(
              CpuConstants.ROUTER_BASE_CPU_PERCENTAGE,
              requiredRouter.getActiveRouteCount(),
              CpuConstants.ROUTE_WEIGHT,
              utilization,
              CpuConstants.UTILIZATION_WEIGHT);
      double power =
          PowerCalculator.calculatePowerConsumption(
              PowerConstants.ROUTER_IDLE_POWER_WATTS,
              cpu,
              PowerConstants.ROUTER_MAXIMUM_POWER_WATTS);
      DeviceMetrics metrics =
          DeviceMetrics.builder()
              .cpuUsagePercentage(cpu)
              .memoryUsageMb(
                  MemoryCalculator.calculateRouterMemoryUsage(
                      MemoryConstants.ROUTER_BASE_MEMORY_MEGABYTES))
              .temperatureCelsius(
                  TemperatureCalculator.calculateNextTemperatureFromPower(
                      TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
                      runtime.getMetrics().getTemperatureCelsius(),
                      power,
                      TemperatureConstants.FORMULA_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK,
                      TemperatureConstants.ROUTER_COOLING_RATE_CELSIUS_PER_TICK))
              .powerConsumptionWatts(power)
              .bandwidthMegabitsPerSecond(throughput)
              .build();
      validateOutputs(metrics);
      runtime.updateMetrics(metrics);
      runtime.updateLastUpdated(requiredTimestamp);
      return result(
          requiredRouter,
          runtime,
          ExecutionStatus.SUCCESS,
          ValidationStatus.VALID,
          requiredTimestamp);
    } catch (IllegalArgumentException exception) {
      return result(
          requiredRouter,
          runtime,
          ExecutionStatus.FAILURE,
          ValidationStatus.INVALID,
          requiredTimestamp);
    }
  }

  public void validateInputs(VideoRouter videoRouter) {
    Objects.requireNonNull(videoRouter, "video router must not be null");
    videoRouter.getLinkCapacityMegabitsPerSecond();
  }

  public void validateOutputs(DeviceMetrics metrics) {
    RuntimeMetricValidator.validate(
        metrics,
        TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
        PowerConstants.ROUTER_IDLE_POWER_WATTS,
        PowerConstants.ROUTER_MAXIMUM_POWER_WATTS);
  }

  private RuleExecutionResult result(
      VideoRouter videoRouter,
      DeviceRuntime runtime,
      ExecutionStatus executionStatus,
      ValidationStatus validationStatus,
      Instant timestamp) {
    return RuleExecutionResult.builder()
        .deviceId(videoRouter.getDeviceId())
        .ruleModelName(RuleModelName.VIDEO_ROUTER)
        .executionStatus(executionStatus)
        .updatedDeviceRuntime(runtime)
        .validationStatus(validationStatus)
        .executionTimestamp(timestamp)
        .build();
  }
}
