package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;

/** Performs the deterministic memory calculation defined by EFS formula 7.2. */
public final class MemoryCalculator {

  private MemoryCalculator() {}

  /**
   * Calculates encoder memory consumption.
   *
   * @param baseMemoryMegabytes base memory consumption
   * @param frameBufferMegabytes frame buffer consumed per stream
   * @param streamCount active stream count
   * @param codecBufferMegabytes codec buffer consumption
   * @return memory consumption in megabytes
   */
  public static double calculateEncoderMemoryUsage(
      double baseMemoryMegabytes,
      double frameBufferMegabytes,
      int streamCount,
      double codecBufferMegabytes) {
    requireNonNegative(baseMemoryMegabytes, "baseMemoryMegabytes");
    requireNonNegative(frameBufferMegabytes, "frameBufferMegabytes");
    requireNonNegative(codecBufferMegabytes, "codecBufferMegabytes");
    if (streamCount < CpuConstants.INITIAL_STREAM_COUNT) {
      throw new IllegalArgumentException("streamCount must be at least one");
    }
    return baseMemoryMegabytes + (frameBufferMegabytes * streamCount) + codecBufferMegabytes;
  }

  /**
   * Calculates router memory usage from its ECS-defined base memory.
   *
   * @param baseMemoryMegabytes router base memory
   * @return memory consumption in megabytes
   */
  public static double calculateRouterMemoryUsage(double baseMemoryMegabytes) {
    requireNonNegative(baseMemoryMegabytes, "baseMemoryMegabytes");
    return baseMemoryMegabytes;
  }

  private static void requireNonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < MemoryConstants.MINIMUM_MEMORY_MEGABYTES) {
      throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
  }
}
