package com.broadcastsim.core.engineering.constants;

import java.util.Map;

/**
 * Defines video-resolution, frame-rate, bit-depth, and chroma constants from ECS sections 2 and 11.
 */
public final class ResolutionConstants {

  public static final int SD_WIDTH_PIXELS = 720;
  public static final int SD_HEIGHT_PIXELS = 576;
  public static final int HD_WIDTH_PIXELS = 1280;
  public static final int HD_HEIGHT_PIXELS = 720;
  public static final int FULL_HD_WIDTH_PIXELS = 1920;
  public static final int FULL_HD_HEIGHT_PIXELS = 1080;
  public static final int UHD_4K_WIDTH_PIXELS = 3840;
  public static final int UHD_4K_HEIGHT_PIXELS = 2160;
  public static final int UHD_8K_WIDTH_PIXELS = 7680;
  public static final int UHD_8K_HEIGHT_PIXELS = 4320;

  public static final double FRAME_RATE_23_976 = 23.976;
  public static final int FRAME_RATE_24 = 24;
  public static final int FRAME_RATE_25 = 25;
  public static final double FRAME_RATE_29_97 = 29.97;
  public static final int FRAME_RATE_30 = 30;
  public static final int FRAME_RATE_50 = 50;
  public static final double FRAME_RATE_59_94 = 59.94;
  public static final int FRAME_RATE_60 = 60;
  public static final int FRAME_RATE_120 = 120;

  public static final int BIT_DEPTH_8 = 8;
  public static final int BIT_DEPTH_10 = 10;
  public static final int BIT_DEPTH_12 = 12;
  public static final int CHROMA_420_BITS_PER_PIXEL = 12;
  public static final int CHROMA_422_BITS_PER_PIXEL = 16;
  public static final int CHROMA_444_BITS_PER_PIXEL = 24;

  public static final int SD_WEIGHT = 1;
  public static final int HD_WEIGHT = 2;
  public static final int FULL_HD_WEIGHT = 4;
  public static final int UHD_4K_WEIGHT = 8;
  public static final int UHD_8K_WEIGHT = 16;
  public static final int FRAME_RATE_24_WEIGHT = 1;
  public static final int FRAME_RATE_23_976_WEIGHT = FRAME_RATE_24_WEIGHT;
  public static final int FRAME_RATE_25_WEIGHT = 2;
  public static final int FRAME_RATE_29_97_WEIGHT = 2;
  public static final int FRAME_RATE_30_WEIGHT = 2;
  public static final int FRAME_RATE_50_WEIGHT = 4;
  public static final int FRAME_RATE_59_94_WEIGHT = 4;
  public static final int FRAME_RATE_60_WEIGHT = 4;
  public static final int FRAME_RATE_120_WEIGHT = 8;

  private static final Map<Double, Integer> FRAME_RATE_CPU_WEIGHTS =
      Map.ofEntries(
          Map.entry(FRAME_RATE_23_976, FRAME_RATE_23_976_WEIGHT),
          Map.entry((double) FRAME_RATE_24, FRAME_RATE_24_WEIGHT),
          Map.entry((double) FRAME_RATE_25, FRAME_RATE_25_WEIGHT),
          Map.entry(FRAME_RATE_29_97, FRAME_RATE_29_97_WEIGHT),
          Map.entry((double) FRAME_RATE_30, FRAME_RATE_30_WEIGHT),
          Map.entry((double) FRAME_RATE_50, FRAME_RATE_50_WEIGHT),
          Map.entry(FRAME_RATE_59_94, FRAME_RATE_59_94_WEIGHT),
          Map.entry((double) FRAME_RATE_60, FRAME_RATE_60_WEIGHT),
          Map.entry((double) FRAME_RATE_120, FRAME_RATE_120_WEIGHT));

  /**
   * Returns the ECS CPU weight for a supported broadcast frame rate.
   *
   * @param framesPerSecond the exact frame rate in frames per second
   * @return the corresponding ECS CPU weight
   * @throws IllegalArgumentException if the frame rate is not supported by ECS
   */
  public static int frameRateCpuWeight(double framesPerSecond) {
    Integer weight = FRAME_RATE_CPU_WEIGHTS.get(framesPerSecond);
    if (weight == null) {
      throw new IllegalArgumentException("frame rate has no ECS-defined CPU weight");
    }
    return weight;
  }

  private ResolutionConstants() {}
}
