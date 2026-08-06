package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.OperationalFailureType;
import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.NetworkConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;
import com.broadcastsim.core.engineering.constants.TemperatureConstants;
import java.util.Collection;
import java.util.Objects;

/** Performs deterministic health calculations defined by EFS formula 10.1 and AHS section 7. */
public final class HealthCalculator {

  private HealthCalculator() {}

  /**
   * Classifies CPU utilization using the ECS health thresholds.
   *
   * @param cpuUsagePercentage CPU utilization percentage
   * @return the CPU health status
   */
  public static HealthStatus evaluateCpuUsage(double cpuUsagePercentage) {
    requirePercentage(cpuUsagePercentage, "cpuUsagePercentage");
    if (cpuUsagePercentage > CpuConstants.CRITICAL_CPU_PERCENTAGE) {
      return HealthStatus.CRITICAL;
    }
    if (cpuUsagePercentage >= CpuConstants.WARNING_CPU_PERCENTAGE) {
      return HealthStatus.WARNING;
    }
    return HealthStatus.NORMAL;
  }

  /**
   * Classifies temperature using the ECS health thresholds.
   *
   * @param temperatureCelsius device temperature in degrees Celsius
   * @return the temperature health status
   */
  public static HealthStatus evaluateTemperature(double temperatureCelsius) {
    if (!Double.isFinite(temperatureCelsius)
        || temperatureCelsius < TemperatureConstants.MINIMUM_TEMPERATURE_CELSIUS) {
      throw new IllegalArgumentException(
          "temperatureCelsius must be finite and within the ECS range");
    }
    if (temperatureCelsius > TemperatureConstants.CRITICAL_TEMPERATURE_CELSIUS) {
      return HealthStatus.CRITICAL;
    }
    if (temperatureCelsius >= TemperatureConstants.WARNING_TEMPERATURE_CELSIUS) {
      return HealthStatus.WARNING;
    }
    return HealthStatus.NORMAL;
  }

  /**
   * Classifies memory utilization using the ECS health thresholds.
   *
   * @param memoryUtilizationPercentage memory utilization percentage
   * @return the memory health status
   */
  public static HealthStatus evaluateMemoryUsage(double memoryUtilizationPercentage) {
    return evaluateIncreasingPercentage(
        memoryUtilizationPercentage,
        MemoryConstants.MINIMUM_MEMORY_UTILIZATION_PERCENTAGE,
        MemoryConstants.MAXIMUM_MEMORY_UTILIZATION_PERCENTAGE,
        MemoryConstants.WARNING_MEMORY_UTILIZATION_PERCENTAGE,
        MemoryConstants.CRITICAL_MEMORY_UTILIZATION_PERCENTAGE,
        "memoryUtilizationPercentage");
  }

  /**
   * Classifies power utilization using the ECS health thresholds.
   *
   * @param powerUtilizationPercentage power utilization percentage
   * @return the power health status
   */
  public static HealthStatus evaluatePowerUtilization(double powerUtilizationPercentage) {
    return evaluateIncreasingPercentage(
        powerUtilizationPercentage,
        PowerConstants.MINIMUM_POWER_UTILIZATION_PERCENTAGE,
        PowerConstants.MAXIMUM_POWER_UTILIZATION_PERCENTAGE,
        PowerConstants.WARNING_POWER_UTILIZATION_PERCENTAGE,
        PowerConstants.CRITICAL_POWER_UTILIZATION_PERCENTAGE,
        "powerUtilizationPercentage");
  }

  /**
   * Classifies signal quality using the ECS health threshold summary.
   *
   * @param signalQualityPercentage signal quality percentage
   * @return the signal-quality health status
   */
  public static HealthStatus evaluateSignalQuality(double signalQualityPercentage) {
    if (!Double.isFinite(signalQualityPercentage)
        || signalQualityPercentage < NetworkConstants.MINIMUM_PERCENTAGE
        || signalQualityPercentage > NetworkConstants.MAXIMUM_PERCENTAGE) {
      throw new IllegalArgumentException(
          "signalQualityPercentage must be within the ECS percentage range");
    }
    if (signalQualityPercentage < NetworkConstants.WARNING_SIGNAL_QUALITY_PERCENTAGE) {
      return HealthStatus.CRITICAL;
    }
    if (signalQualityPercentage <= NetworkConstants.NORMAL_SIGNAL_QUALITY_EXCLUSIVE_PERCENTAGE) {
      return HealthStatus.WARNING;
    }
    return HealthStatus.NORMAL;
  }

  /**
   * Evaluates operational failures using the EFS formula 11.4 failure conditions.
   *
   * @param failures active operational failure conditions
   * @return failed when any operational failure is active; otherwise normal
   */
  public static HealthStatus evaluateOperationalHealth(
      Collection<OperationalFailureType> failures) {
    Objects.requireNonNull(failures, "failures must not be null");
    boolean hasOperationalFailure = false;
    for (OperationalFailureType failure : failures) {
      Objects.requireNonNull(failure, "failure must not be null");
      hasOperationalFailure = true;
    }
    return hasOperationalFailure ? HealthStatus.FAILED : HealthStatus.NORMAL;
  }

  /**
   * Returns the highest-severity status among the five health dimensions required by AHS section
   * 12.
   *
   * @param cpuStatus CPU health status
   * @param memoryStatus memory health status
   * @param temperatureStatus temperature health status
   * @param powerStatus power health status
   * @param signalQualityStatus signal-quality health status
   * @return the overall health status
   */
  public static HealthStatus evaluateOverallHealth(
      HealthStatus cpuStatus,
      HealthStatus memoryStatus,
      HealthStatus temperatureStatus,
      HealthStatus powerStatus,
      HealthStatus signalQualityStatus) {
    HealthStatus overall = Objects.requireNonNull(cpuStatus, "cpuStatus must not be null");
    overall =
        moreSevere(overall, Objects.requireNonNull(memoryStatus, "memoryStatus must not be null"));
    overall =
        moreSevere(
            overall,
            Objects.requireNonNull(temperatureStatus, "temperatureStatus must not be null"));
    overall =
        moreSevere(overall, Objects.requireNonNull(powerStatus, "powerStatus must not be null"));
    return moreSevere(
        overall,
        Objects.requireNonNull(signalQualityStatus, "signalQualityStatus must not be null"));
  }

  private static HealthStatus moreSevere(HealthStatus first, HealthStatus second) {
    return first.ordinal() >= second.ordinal() ? first : second;
  }

  private static HealthStatus evaluateIncreasingPercentage(
      double value,
      double minimumPercentage,
      double maximumPercentage,
      double warningPercentage,
      double criticalPercentage,
      String name) {
    if (!Double.isFinite(value) || value < minimumPercentage || value > maximumPercentage) {
      throw new IllegalArgumentException(name + " must be within the ECS percentage range");
    }
    if (value > criticalPercentage) {
      return HealthStatus.CRITICAL;
    }
    if (value >= warningPercentage) {
      return HealthStatus.WARNING;
    }
    return HealthStatus.NORMAL;
  }

  private static void requirePercentage(double value, String name) {
    if (!Double.isFinite(value)
        || value < CpuConstants.MINIMUM_CPU_PERCENTAGE
        || value > CpuConstants.MAXIMUM_CPU_PERCENTAGE) {
      throw new IllegalArgumentException(name + " must be within the ECS percentage range");
    }
  }
}
