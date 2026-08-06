package com.broadcastsim.core.engineering.constants;

/**
 * Defines codec compression, relative-resource, formula-weight, and buffer constants from ECS
 * sections 3 and 11.
 */
public final class CodecConstants {

  public static final double RAW_COMPRESSION_RATIO = 1.0;
  public static final double JPEG_2000_COMPRESSION_RATIO = 6.0;
  public static final double MPEG_2_COMPRESSION_RATIO = 20.0;
  public static final double H264_COMPRESSION_RATIO = 60.0;
  public static final double H265_COMPRESSION_RATIO = 120.0;

  public static final double RAW_CPU_RELATIVE_WEIGHT = 0.5;
  public static final double JPEG_2000_CPU_RELATIVE_WEIGHT = 1.2;
  public static final double MPEG_2_CPU_RELATIVE_WEIGHT = 1.5;
  public static final double H264_CPU_RELATIVE_WEIGHT = 2.0;
  public static final double H265_CPU_RELATIVE_WEIGHT = 3.0;

  public static final double RAW_MEMORY_RELATIVE_WEIGHT = 0.5;
  public static final double JPEG_2000_MEMORY_RELATIVE_WEIGHT = 1.1;
  public static final double MPEG_2_MEMORY_RELATIVE_WEIGHT = 1.3;
  public static final double H264_MEMORY_RELATIVE_WEIGHT = 1.8;
  public static final double H265_MEMORY_RELATIVE_WEIGHT = 2.5;

  public static final int RAW_FORMULA_WEIGHT = 1;
  public static final int JPEG_2000_FORMULA_WEIGHT = 2;
  public static final int MPEG_2_FORMULA_WEIGHT = 3;
  public static final int H264_FORMULA_WEIGHT = 5;
  public static final int H265_FORMULA_WEIGHT = 8;

  public static final int RAW_BUFFER_MEGABYTES = 32;
  public static final int JPEG_2000_BUFFER_MEGABYTES = 64;
  public static final int MPEG_2_BUFFER_MEGABYTES = 96;
  public static final int H264_BUFFER_MEGABYTES = 128;
  public static final int H265_BUFFER_MEGABYTES = 160;

  private CodecConstants() {}
}
