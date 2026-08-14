package com.broadcastsim.core.engineering.constants;

/** Defines CPU-model constants and valid limits from ECS sections 5, 6, 9, 10, and 11. */
public final class CpuConstants {

  public static final double CAMERA_BASE_CPU_PERCENTAGE = 5.0;

  public static final double ENCODER_BASE_CPU_PERCENTAGE = 10.0;
  public static final double ENCODER_IDLE_CPU_PERCENTAGE = 5.0;

  public static final double ROUTER_BASE_CPU_PERCENTAGE = 2.0;
  public static final double ROUTER_IDLE_CPU_PERCENTAGE = 2.0;

  public static final double STREAM_WEIGHT = 5.0;
  public static final double ROUTE_WEIGHT = 2.0;
  public static final double UTILIZATION_WEIGHT = 0.10;

  public static final int MINIMUM_ACTIVE_ROUTE_COUNT = 0;
  public static final int INITIAL_STREAM_COUNT = 1;

  public static final double MINIMUM_CPU_PERCENTAGE = 0.0;
  public static final double MAXIMUM_CPU_PERCENTAGE = 100.0;
  
  public static final double WARNING_CPU_PERCENTAGE = 80.0;
  public static final double CRITICAL_CPU_PERCENTAGE = 95.0;

  private CpuConstants() {}
}
