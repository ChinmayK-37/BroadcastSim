package com.broadcastsim.core.engineering.rules;

import com.broadcastsim.core.common.enums.OperationalFailureType;
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.calculators.CpuCalculator;
import com.broadcastsim.core.engineering.calculators.MemoryCalculator;
import com.broadcastsim.core.engineering.calculators.PowerCalculator;
import com.broadcastsim.core.engineering.calculators.TemperatureCalculator;
import com.broadcastsim.core.engineering.constants.CodecConstants;
import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;
import com.broadcastsim.core.engineering.constants.ResolutionConstants;
import com.broadcastsim.core.engineering.constants.TemperatureConstants;
import com.broadcastsim.core.signal.Codec;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.signal.Signal;
import java.time.Instant;
import java.util.Objects;

/** Calculates encoder runtime metrics using the reusable engineering calculators. */
public final class EncoderRuleModel {

  /** Executes the model using the runtime timestamp as a deterministic execution timestamp. */
  public RuleExecutionResult execute(Encoder encoder) {
    Objects.requireNonNull(encoder, "encoder must not be null");
    return execute(encoder, encoder.getDeviceRuntime().getLastUpdated());
  }

  /** Executes the encoder engineering model at the supplied simulation timestamp. */
  public RuleExecutionResult execute(Encoder encoder, Instant executionTimestamp) {
    Encoder requiredEncoder = Objects.requireNonNull(encoder, "encoder must not be null");
    Instant requiredTimestamp =
        Objects.requireNonNull(executionTimestamp, "execution timestamp must not be null");
    DeviceRuntime runtime = requiredEncoder.getDeviceRuntime();
    try {
      Signal signal = validateInputs(requiredEncoder);
      double cpu =
          CpuCalculator.calculateEncoderCpuUsage(
              CpuConstants.ENCODER_BASE_CPU_PERCENTAGE,
              resolutionWeight(signal.getResolution()),
              frameRateWeight(signal.getFramesPerSecond()),
              codecWeight(requiredEncoder.getCodec()),
              CpuConstants.INITIAL_STREAM_COUNT,
              CpuConstants.STREAM_WEIGHT);
      double memory =
          MemoryCalculator.calculateEncoderMemoryUsage(
              MemoryConstants.ENCODER_BASE_MEMORY_MEGABYTES,
              MemoryConstants.FRAME_BUFFER_MEGABYTES,
              CpuConstants.INITIAL_STREAM_COUNT,
              codecBuffer(requiredEncoder.getCodec()));
      double power =
          PowerCalculator.calculatePowerConsumption(
              PowerConstants.ENCODER_IDLE_POWER_WATTS,
              cpu,
              PowerConstants.ENCODER_MAXIMUM_POWER_WATTS);
      DeviceMetrics metrics =
          DeviceMetrics.builder()
              .cpuUsagePercentage(cpu)
              .memoryUsageMb(memory)
              .temperatureCelsius(
                  TemperatureCalculator.calculateNextTemperatureFromPower(
                      TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
                      runtime.getMetrics().getTemperatureCelsius(),
                      power,
                      TemperatureConstants.ENCODER_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK,
                      TemperatureConstants.ENCODER_COOLING_RATE_CELSIUS_PER_TICK))
              .powerConsumptionWatts(power)
              .bandwidthMegabitsPerSecond(requiredEncoder.getTargetBitrateMegabitsPerSecond())
              .build();
      runtime.updateMetrics(metrics);
      runtime.updateLastUpdated(requiredTimestamp);
      recordThermalOperatingLimitFailure(runtime, metrics);
      validateOutputs(metrics);
      return result(
          requiredEncoder,
          runtime,
          ExecutionStatus.SUCCESS,
          ValidationStatus.VALID,
          requiredTimestamp);
    } catch (IllegalArgumentException exception) {
      return result(
          requiredEncoder,
          runtime,
          ExecutionStatus.FAILURE,
          ValidationStatus.INVALID,
          requiredTimestamp);
    }
  }

  public Signal validateInputs(Encoder encoder) {
    Objects.requireNonNull(encoder, "encoder must not be null");
    encoder.getCodec();
    encoder.getTargetBitrateMegabitsPerSecond();
    return encoder
        .getCurrentInputSignal()
        .orElseThrow(() -> new IllegalArgumentException("encoder input signal is required"));
  }

  public void validateOutputs(DeviceMetrics metrics) {
    RuntimeMetricValidator.validate(
        metrics,
        TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
        PowerConstants.ENCODER_IDLE_POWER_WATTS,
        PowerConstants.ENCODER_MAXIMUM_POWER_WATTS);
  }

  private void recordThermalOperatingLimitFailure(DeviceRuntime runtime, DeviceMetrics metrics) {
    if (metrics.getTemperatureCelsius() > TemperatureConstants.MAXIMUM_TEMPERATURE_CELSIUS) {
      runtime.addOperationalFailure(OperationalFailureType.THERMAL_PROTECTION_SHUTDOWN);
    }
  }

  private double resolutionWeight(Resolution resolution) {
    return switch (resolution) {
      case SD_480P -> ResolutionConstants.SD_WEIGHT;
      case HD_720P -> ResolutionConstants.HD_WEIGHT;
      case FULL_HD_1080P -> ResolutionConstants.FULL_HD_WEIGHT;
      case UHD_4K -> ResolutionConstants.UHD_4K_WEIGHT;
      case UHD_8K -> ResolutionConstants.UHD_8K_WEIGHT;
    };
  }

  private double frameRateWeight(double framesPerSecond) {
    return ResolutionConstants.frameRateCpuWeight(framesPerSecond);
  }

  private double codecWeight(Codec codec) {
    return switch (codec) {
      case RAW -> CodecConstants.RAW_FORMULA_WEIGHT;
      case H264 -> CodecConstants.H264_FORMULA_WEIGHT;
      case H265 -> CodecConstants.H265_FORMULA_WEIGHT;
    };
  }

  private double codecBuffer(Codec codec) {
    return switch (codec) {
      case RAW -> CodecConstants.RAW_BUFFER_MEGABYTES;
      case H264 -> CodecConstants.H264_BUFFER_MEGABYTES;
      case H265 -> CodecConstants.H265_BUFFER_MEGABYTES;
    };
  }

  private RuleExecutionResult result(
      Encoder encoder,
      DeviceRuntime runtime,
      ExecutionStatus executionStatus,
      ValidationStatus validationStatus,
      Instant timestamp) {
    return RuleExecutionResult.builder()
        .deviceId(encoder.getDeviceId())
        .ruleModelName(RuleModelName.ENCODER)
        .executionStatus(executionStatus)
        .updatedDeviceRuntime(runtime)
        .validationStatus(validationStatus)
        .executionTimestamp(timestamp)
        .build();
  }
}
