package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.engineering.constants.NetworkConstants;

/**
 * Performs deterministic signal-quality and delay calculations defined by EFS formulas 9.1 through
 * 9.4.
 */
public final class SignalQualityCalculator {

  private SignalQualityCalculator() {}

  /**
   * Calculates the quality penalty caused by packet loss.
   *
   * @param packetLossPercentage packet loss percentage
   * @param lossWeight packet-loss coefficient
   * @return signal-quality penalty
   */
  public static double calculatePacketLossPenalty(double packetLossPercentage, double lossWeight) {
    requireNonNegative(packetLossPercentage, "packetLossPercentage");
    requireNonNegative(lossWeight, "lossWeight");
    return packetLossPercentage * lossWeight;
  }

  /**
   * Calculates the quality penalty caused by latency.
   *
   * @param latencyMilliseconds signal latency in milliseconds
   * @param latencyWeight latency coefficient
   * @return signal-quality penalty
   */
  public static double calculateLatencyPenalty(double latencyMilliseconds, double latencyWeight) {
    requireNonNegative(latencyMilliseconds, "latencyMilliseconds");
    requireNonNegative(latencyWeight, "latencyWeight");
    return latencyMilliseconds * latencyWeight;
  }

  /**
   * Calculates and clamps signal quality to the ECS-defined percentage range.
   *
   * @param packetLossPenalty quality penalty caused by packet loss
   * @param latencyPenalty quality penalty caused by latency
   * @return signal quality percentage
   */
  public static double calculateSignalQuality(double packetLossPenalty, double latencyPenalty) {
    requireNonNegative(packetLossPenalty, "packetLossPenalty");
    requireNonNegative(latencyPenalty, "latencyPenalty");
    return Math.clamp(
        NetworkConstants.MAXIMUM_PERCENTAGE - packetLossPenalty - latencyPenalty,
        NetworkConstants.MINIMUM_PERCENTAGE,
        NetworkConstants.MAXIMUM_PERCENTAGE);
  }

  /**
   * Calculates signal latency from its base, switch, and queue delay components.
   *
   * @param baseLatencyMilliseconds base network latency
   * @param switchDelayMilliseconds switch delay
   * @param queueDelayMilliseconds queue delay
   * @return total latency in milliseconds
   */
  public static double calculateSignalDelay(
      double baseLatencyMilliseconds,
      double switchDelayMilliseconds,
      double queueDelayMilliseconds) {
    requireNonNegative(baseLatencyMilliseconds, "baseLatencyMilliseconds");
    requireNonNegative(switchDelayMilliseconds, "switchDelayMilliseconds");
    requireNonNegative(queueDelayMilliseconds, "queueDelayMilliseconds");
    return baseLatencyMilliseconds + switchDelayMilliseconds + queueDelayMilliseconds;
  }

  private static void requireNonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < NetworkConstants.MINIMUM_PERCENTAGE) {
      throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
  }
}
