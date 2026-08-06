package com.broadcastsim.core.engineering.constants;

/** Defines thermal-model constants and limits from ECS sections 4, 5, 6, 9, 10, and 11. */
public final class TemperatureConstants {

  public static final double AMBIENT_TEMPERATURE_CELSIUS = 25.0;
  public static final double CAMERA_COOLING_RATE_CELSIUS_PER_TICK = 0.50;
  public static final double CAMERA_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK = 0.04;
  public static final double ENCODER_COOLING_RATE_CELSIUS_PER_TICK = 0.80;
  public static final double ENCODER_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK = 0.06;
  public static final double ROUTER_COOLING_RATE_CELSIUS_PER_TICK = 0.60;
  public static final double FORMULA_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK = 0.04;
  public static final double MINIMUM_HEAT_CELSIUS_PER_TICK = 0.0;
  public static final double MINIMUM_COOLING_RATE_CELSIUS_PER_TICK = 0.0;
  public static final double MINIMUM_TEMPERATURE_CELSIUS = AMBIENT_TEMPERATURE_CELSIUS;
  public static final double MAXIMUM_TEMPERATURE_CELSIUS = 120.0;
  public static final double WARNING_TEMPERATURE_CELSIUS = 70.0;
  public static final double CRITICAL_TEMPERATURE_CELSIUS = 85.0;

  private TemperatureConstants() {}
}
