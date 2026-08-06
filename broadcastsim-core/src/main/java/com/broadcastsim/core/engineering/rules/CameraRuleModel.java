package com.broadcastsim.core.engineering.rules;

import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.calculators.BandwidthCalculator;
import com.broadcastsim.core.engineering.calculators.PowerCalculator;
import com.broadcastsim.core.engineering.calculators.TemperatureCalculator;
import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.NetworkConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;
import com.broadcastsim.core.engineering.constants.TemperatureConstants;
import java.time.Instant;
import java.util.Objects;

/** Calculates camera runtime metrics using the reusable engineering calculators. */
public final class CameraRuleModel {

  /** Executes the model using the runtime timestamp as a deterministic execution timestamp. */
  public RuleExecutionResult execute(Camera camera) {
    Objects.requireNonNull(camera, "camera must not be null");
    return execute(camera, camera.getDeviceRuntime().getLastUpdated());
  }

  /** Executes the camera engineering model at the supplied simulation timestamp. */
  public RuleExecutionResult execute(Camera camera, Instant executionTimestamp) {
    Camera requiredCamera = Objects.requireNonNull(camera, "camera must not be null");
    Instant requiredTimestamp =
        Objects.requireNonNull(executionTimestamp, "execution timestamp must not be null");
    DeviceRuntime runtime = requiredCamera.getDeviceRuntime();
    try {
      validateInputs(requiredCamera);
      double pixels =
          BandwidthCalculator.calculatePixelsPerSecond(
              requiredCamera.getResolution().getWidth(),
              requiredCamera.getResolution().getHeight(),
              requiredCamera.getFramesPerSecond());
      double rawBitrate =
          BandwidthCalculator.calculateRawBitrate(pixels, requiredCamera.getBitDepth());
      double power =
          PowerCalculator.calculatePowerConsumption(
              PowerConstants.CAMERA_IDLE_POWER_WATTS,
              CpuConstants.CAMERA_BASE_CPU_PERCENTAGE,
              PowerConstants.CAMERA_MAXIMUM_POWER_WATTS);
      DeviceMetrics metrics =
          DeviceMetrics.builder()
              .cpuUsagePercentage(CpuConstants.CAMERA_BASE_CPU_PERCENTAGE)
              .memoryUsageMb(MemoryConstants.CAMERA_BASE_MEMORY_MEGABYTES)
              .temperatureCelsius(
                  TemperatureCalculator.calculateNextTemperature(
                      TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
                      runtime.getMetrics().getTemperatureCelsius(),
                      power
                          * TemperatureConstants.CAMERA_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK,
                      TemperatureConstants.CAMERA_COOLING_RATE_CELSIUS_PER_TICK))
              .powerConsumptionWatts(power)
              .bandwidthMegabitsPerSecond(rawBitrate / NetworkConstants.BITS_PER_MEGABIT)
              .build();
      validateOutputs(metrics);
      runtime.updateMetrics(metrics);
      runtime.updateLastUpdated(requiredTimestamp);
      return result(
          requiredCamera,
          runtime,
          ExecutionStatus.SUCCESS,
          ValidationStatus.VALID,
          requiredTimestamp);
    } catch (IllegalArgumentException exception) {
      return result(
          requiredCamera,
          runtime,
          ExecutionStatus.FAILURE,
          ValidationStatus.INVALID,
          requiredTimestamp);
    }
  }

  public void validateInputs(Camera camera) {
    Objects.requireNonNull(camera, "camera must not be null");
    camera.getResolution();
    camera.getFramesPerSecond();
    camera.getBitDepth();
    camera.getChromaSampling();
  }

  public void validateOutputs(DeviceMetrics metrics) {
    RuntimeMetricValidator.validate(
        metrics,
        TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
        PowerConstants.CAMERA_IDLE_POWER_WATTS,
        PowerConstants.CAMERA_MAXIMUM_POWER_WATTS);
  }

  private RuleExecutionResult result(
      Camera camera,
      DeviceRuntime runtime,
      ExecutionStatus executionStatus,
      ValidationStatus validationStatus,
      Instant timestamp) {
    return RuleExecutionResult.builder()
        .deviceId(camera.getDeviceId())
        .ruleModelName(RuleModelName.CAMERA)
        .executionStatus(executionStatus)
        .updatedDeviceRuntime(runtime)
        .validationStatus(validationStatus)
        .executionTimestamp(timestamp)
        .build();
  }
}
