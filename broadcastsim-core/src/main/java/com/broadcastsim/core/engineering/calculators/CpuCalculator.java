package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.engineering.constants.CpuConstants;
import java.util.Objects;

/** Performs deterministic CPU calculations defined by EFS formulas 7.1 and 8.2. */
public final class CpuCalculator {

  private CpuCalculator() {}

  /**
   * Calculates encoder CPU utilization.
   *
   * @param baseCpuPercentage base CPU utilization
   * @param resolutionWeight resolution contribution
   * @param framesPerSecondWeight frame-rate contribution
   * @param codecWeight codec contribution
   * @param streamCount active stream count
   * @param streamWeight additional-stream contribution
   * @return CPU utilization percentage
   */
  public static double calculateEncoderCpuUsage(
      double baseCpuPercentage,
      double resolutionWeight,
      double framesPerSecondWeight,
      double codecWeight,
      int streamCount,
      double streamWeight) {
    requireNonNegative(baseCpuPercentage, "baseCpuPercentage");
    requireNonNegative(resolutionWeight, "resolutionWeight");
    requireNonNegative(framesPerSecondWeight, "framesPerSecondWeight");
    requireNonNegative(codecWeight, "codecWeight");
    requireNonNegative(streamWeight, "streamWeight");
    if (streamCount < CpuConstants.INITIAL_STREAM_COUNT) {
      throw new IllegalArgumentException("streamCount must be at least one");
    }
    return baseCpuPercentage
        + resolutionWeight
        + framesPerSecondWeight
        + codecWeight
        + ((streamCount - CpuConstants.INITIAL_STREAM_COUNT) * streamWeight);
  }

  /**
   * Calculates router CPU utilization.
   *
   * @param baseCpuPercentage base CPU utilization
   * @param activeRoutes number of active routes
   * @param routeWeight per-route CPU contribution
   * @param linkUtilizationPercentage link utilization percentage
   * @param utilizationWeight link-utilization CPU contribution
   * @return CPU utilization percentage
   */
  public static double calculateRouterCpuUsage(
      double baseCpuPercentage,
      int activeRoutes,
      double routeWeight,
      double linkUtilizationPercentage,
      double utilizationWeight) {
    requireNonNegative(baseCpuPercentage, "baseCpuPercentage");
    if (activeRoutes < CpuConstants.MINIMUM_ACTIVE_ROUTE_COUNT) {
      throw new IllegalArgumentException("activeRoutes must be non-negative");
    }
    requireNonNegative(routeWeight, "routeWeight");
    requireNonNegative(linkUtilizationPercentage, "linkUtilizationPercentage");
    requireNonNegative(utilizationWeight, "utilizationWeight");
    return baseCpuPercentage
        + (activeRoutes * routeWeight)
        + (linkUtilizationPercentage * utilizationWeight);
  }

  private static void requireNonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < CpuConstants.MINIMUM_CPU_PERCENTAGE) {
      throw new IllegalArgumentException(
          Objects.requireNonNull(name, "name must not be null")
              + " must be finite and non-negative");
    }
  }
}
