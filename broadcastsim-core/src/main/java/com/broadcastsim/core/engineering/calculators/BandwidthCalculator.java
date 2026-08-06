package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.engineering.constants.NetworkConstants;
import java.util.Collection;
import java.util.Objects;

/**
 * Performs the deterministic bandwidth and link calculations defined by EFS formulas 6.1 through
 * 6.3 and 8.1 through 8.3.
 */
public final class BandwidthCalculator {

  private BandwidthCalculator() {}

  /**
   * Calculates the pixels generated per second.
   *
   * @param widthPixels frame width in pixels
   * @param heightPixels frame height in pixels
   * @param framesPerSecond frame rate in frames per second
   * @return pixels generated per second
   */
  public static double calculatePixelsPerSecond(
      int widthPixels, int heightPixels, double framesPerSecond) {
    requireNonNegative(widthPixels, "widthPixels");
    requireNonNegative(heightPixels, "heightPixels");
    requireNonNegative(framesPerSecond, "framesPerSecond");
    return (double) widthPixels * heightPixels * framesPerSecond;
  }

  /**
   * Calculates an uncompressed video bitrate in bits per second.
   *
   * @param pixelsPerSecond source pixel rate
   * @param bitsPerPixel bits represented by each pixel
   * @return raw bitrate in bits per second
   */
  public static double calculateRawBitrate(double pixelsPerSecond, double bitsPerPixel) {
    requireNonNegative(pixelsPerSecond, "pixelsPerSecond");
    requireNonNegative(bitsPerPixel, "bitsPerPixel");
    return pixelsPerSecond * bitsPerPixel;
  }

  /**
   * Calculates a compressed video bitrate in bits per second.
   *
   * @param rawBitrateBitsPerSecond uncompressed bitrate
   * @param compressionRatio codec compression ratio
   * @return compressed bitrate in bits per second
   */
  public static double calculateCompressedBitrate(
      double rawBitrateBitsPerSecond, double compressionRatio) {
    requireNonNegative(rawBitrateBitsPerSecond, "rawBitrateBitsPerSecond");
    if (!Double.isFinite(compressionRatio)
        || compressionRatio <= NetworkConstants.IDEAL_PACKET_LOSS_PERCENTAGE) {
      throw new IllegalArgumentException("compressionRatio must be finite and greater than zero");
    }
    return rawBitrateBitsPerSecond / compressionRatio;
  }

  /**
   * Sums all input signal bitrates to calculate router throughput.
   *
   * @param inputSignalBitratesMegabitsPerSecond input bitrates in megabits per second
   * @return aggregate throughput in megabits per second
   */
  public static double calculateThroughput(
      Collection<Double> inputSignalBitratesMegabitsPerSecond) {
    Objects.requireNonNull(
        inputSignalBitratesMegabitsPerSecond, "input signal bitrates must not be null");
    double throughput = NetworkConstants.IDEAL_PACKET_LOSS_PERCENTAGE;
    for (Double bitrate : inputSignalBitratesMegabitsPerSecond) {
      if (bitrate == null) {
        throw new IllegalArgumentException("input signal bitrate must not be null");
      }
      requireNonNegative(bitrate, "inputSignalBitrate");
      throughput += bitrate;
    }
    return throughput;
  }

  /**
   * Calculates link utilization as a percentage.
   *
   * @param currentBitrateMegabitsPerSecond current link bitrate
   * @param linkCapacityMegabitsPerSecond available link capacity
   * @return link utilization percentage
   */
  public static double calculateLinkUtilization(
      double currentBitrateMegabitsPerSecond, double linkCapacityMegabitsPerSecond) {
    requireNonNegative(currentBitrateMegabitsPerSecond, "currentBitrateMegabitsPerSecond");
    if (!Double.isFinite(linkCapacityMegabitsPerSecond)
        || linkCapacityMegabitsPerSecond <= NetworkConstants.IDEAL_PACKET_LOSS_PERCENTAGE) {
      throw new IllegalArgumentException(
          "linkCapacityMegabitsPerSecond must be finite and greater than zero");
    }
    return (currentBitrateMegabitsPerSecond / linkCapacityMegabitsPerSecond)
        * NetworkConstants.MAXIMUM_PERCENTAGE;
  }

  /**
   * Validates whether a bitrate fits within a link capacity.
   *
   * @param currentBitrateMegabitsPerSecond current link bitrate
   * @param linkCapacityMegabitsPerSecond available link capacity
   * @return true when the bitrate does not exceed capacity
   */
  public static boolean isWithinLinkCapacity(
      double currentBitrateMegabitsPerSecond, double linkCapacityMegabitsPerSecond) {
    requireNonNegative(currentBitrateMegabitsPerSecond, "currentBitrateMegabitsPerSecond");
    if (!Double.isFinite(linkCapacityMegabitsPerSecond)
        || linkCapacityMegabitsPerSecond <= NetworkConstants.IDEAL_PACKET_LOSS_PERCENTAGE) {
      throw new IllegalArgumentException(
          "linkCapacityMegabitsPerSecond must be finite and greater than zero");
    }
    return currentBitrateMegabitsPerSecond <= linkCapacityMegabitsPerSecond;
  }

  private static void requireNonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < NetworkConstants.IDEAL_PACKET_LOSS_PERCENTAGE) {
      throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
  }

  private static void requireNonNegative(int value, String name) {
    if (value < NetworkConstants.IDEAL_PACKET_LOSS_PERCENTAGE) {
      throw new IllegalArgumentException(name + " must be non-negative");
    }
  }
}
