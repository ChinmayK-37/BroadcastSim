package com.broadcastsim.core.engineering.constants;

/** Defines memory-model constants and limits from ECS sections 5, 6, 10, and 11. */
public final class MemoryConstants {

  public static final double CAMERA_BASE_MEMORY_MEGABYTES = 128.0;
  public static final double ENCODER_BASE_MEMORY_MEGABYTES = 512.0;
  public static final double ENCODER_IDLE_MEMORY_MEGABYTES = 512.0;
  public static final double ROUTER_BASE_MEMORY_MEGABYTES = 256.0;
  public static final double ROUTER_IDLE_MEMORY_MEGABYTES = 256.0;
  public static final double FRAME_BUFFER_MEGABYTES = 64.0;
  public static final double MINIMUM_MEMORY_MEGABYTES = 0.0;
  public static final double MINIMUM_MEMORY_UTILIZATION_PERCENTAGE = 0.0;
  public static final double MAXIMUM_MEMORY_UTILIZATION_PERCENTAGE = 100.0;
  public static final double WARNING_MEMORY_UTILIZATION_PERCENTAGE = 80.0;
  public static final double CRITICAL_MEMORY_UTILIZATION_PERCENTAGE = 95.0;

  private MemoryConstants() {}
}
