package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;

/** Performs the deterministic power calculation defined by EFS formulas 6.4, 7.3, and 8.4. */
public final class PowerCalculator {

  private PowerCalculator() {}

  /**
   * Calculates power consumption from CPU utilization and the device power limits.
   *
   * @param idlePowerWatts idle power consumption
   * @param cpuUsagePercentage current CPU utilization
   * @param maximumPowerWatts maximum power consumption
   * @return power consumption in watts
   */
  public static double calculatePowerConsumption(
      double idlePowerWatts, double cpuUsagePercentage, double maximumPowerWatts) {
    requireNonNegative(idlePowerWatts, "idlePowerWatts");
    requirePercentage(cpuUsagePercentage, "cpuUsagePercentage");
    requireNonNegative(maximumPowerWatts, "maximumPowerWatts");
    if (maximumPowerWatts < idlePowerWatts) {
      throw new IllegalArgumentException("maximumPowerWatts must not be less than idlePowerWatts");
    }
    return idlePowerWatts
        + (cpuUsagePercentage / PowerConstants.CPU_PERCENTAGE_DIVISOR)
            * (maximumPowerWatts - idlePowerWatts);
  }

  /**
   * Calculates power utilization relative to the maximum available power.
   *
   * @param powerConsumptionWatts current power consumption
   * @param maximumPowerWatts maximum available power
   * @return power utilization percentage
   */
  public static double calculatePowerUtilization(
      double powerConsumptionWatts, double maximumPowerWatts) {
    requireNonNegative(powerConsumptionWatts, "powerConsumptionWatts");
    if (!Double.isFinite(maximumPowerWatts)
        || maximumPowerWatts <= CpuConstants.MINIMUM_CPU_PERCENTAGE) {
      throw new IllegalArgumentException("maximumPowerWatts must be finite and greater than zero");
    }
    return (powerConsumptionWatts / maximumPowerWatts) * PowerConstants.CPU_PERCENTAGE_DIVISOR;
  }

  private static void requireNonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < CpuConstants.MINIMUM_CPU_PERCENTAGE) {
      throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
  }

  private static void requirePercentage(double value, String name) {
    if (!Double.isFinite(value)
        || value < CpuConstants.MINIMUM_CPU_PERCENTAGE
        || value > CpuConstants.MAXIMUM_CPU_PERCENTAGE) {
      throw new IllegalArgumentException(name + " must be between zero and one hundred");
    }
  }
}
