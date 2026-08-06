package com.broadcastsim.core.engineering.constants;

/** Defines power-model constants and limits from ECS sections 4, 5, 6, 10, and 11. */
public final class PowerConstants {

  public static final double CAMERA_IDLE_POWER_WATTS = 5.0;
  public static final double CAMERA_MAXIMUM_POWER_WATTS = 15.0;
  public static final double ENCODER_IDLE_POWER_WATTS = 20.0;
  public static final double ENCODER_MAXIMUM_POWER_WATTS = 80.0;
  public static final double ROUTER_IDLE_POWER_WATTS = 15.0;
  public static final double ROUTER_MAXIMUM_POWER_WATTS = 50.0;
  public static final double ROUTER_POWER_PER_ACTIVE_PORT_WATTS = 0.4;
  public static final double CPU_PERCENTAGE_DIVISOR = 100.0;
  public static final double MINIMUM_POWER_UTILIZATION_PERCENTAGE = 0.0;
  public static final double MAXIMUM_POWER_UTILIZATION_PERCENTAGE = 100.0;
  public static final double WARNING_POWER_UTILIZATION_PERCENTAGE = 80.0;
  public static final double CRITICAL_POWER_UTILIZATION_PERCENTAGE = 95.0;

  private PowerConstants() {}
}
