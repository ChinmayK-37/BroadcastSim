package com.broadcastsim.core.engineering.calculators;

import com.broadcastsim.core.engineering.constants.TemperatureConstants;

/** Performs deterministic thermal calculations defined by EFS formulas 6.5, 7.4, 8.5, and 11.2. */
public final class TemperatureCalculator {

  private TemperatureCalculator() {}

  /**
   * Calculates the next temperature from generated heat and cooling.
   *
   * @param currentTemperatureCelsius current device temperature
   * @param generatedHeatCelsius generated heat for the current tick
   * @param coolingRateCelsius cooling for the current tick
   * @return next temperature in degrees Celsius
   */
  public static double calculateNextTemperature(
      double currentTemperatureCelsius, double generatedHeatCelsius, double coolingRateCelsius) {
    requireFinite(currentTemperatureCelsius, "currentTemperatureCelsius");
    requireNonNegative(generatedHeatCelsius, "generatedHeatCelsius");
    requireNonNegative(coolingRateCelsius, "coolingRateCelsius");
    return currentTemperatureCelsius + generatedHeatCelsius - coolingRateCelsius;
  }

  /** Calculates the next temperature without allowing it to fall below ambient temperature. */
  public static double calculateNextTemperature(
      double ambientTemperatureCelsius,
      double currentTemperatureCelsius,
      double generatedHeatCelsius,
      double coolingRateCelsius) {
    requireFinite(ambientTemperatureCelsius, "ambientTemperatureCelsius");
    return Math.max(
        ambientTemperatureCelsius,
        calculateNextTemperature(
            currentTemperatureCelsius, generatedHeatCelsius, coolingRateCelsius));
  }

  /** Calculates power-derived temperature without allowing it to fall below ambient temperature. */
  public static double calculateNextTemperatureFromPower(
      double ambientTemperatureCelsius,
      double currentTemperatureCelsius,
      double powerWatts,
      double heatCoefficientCelsiusPerWatt,
      double coolingRateCelsius) {
    requireFinite(ambientTemperatureCelsius, "ambientTemperatureCelsius");
    return Math.max(
        ambientTemperatureCelsius,
        calculateNextTemperatureFromPower(
            currentTemperatureCelsius,
            powerWatts,
            heatCoefficientCelsiusPerWatt,
            coolingRateCelsius));
  }

  /**
   * Calculates the next temperature from power, heat coefficient, and cooling.
   *
   * @param currentTemperatureCelsius current device temperature
   * @param powerWatts current power consumption
   * @param heatCoefficientCelsiusPerWatt heat generated per watt during a tick
   * @param coolingRateCelsius cooling for the current tick
   * @return next temperature in degrees Celsius
   */
  public static double calculateNextTemperatureFromPower(
      double currentTemperatureCelsius,
      double powerWatts,
      double heatCoefficientCelsiusPerWatt,
      double coolingRateCelsius) {
    requireFinite(currentTemperatureCelsius, "currentTemperatureCelsius");
    requireNonNegative(powerWatts, "powerWatts");
    requireNonNegative(heatCoefficientCelsiusPerWatt, "heatCoefficientCelsiusPerWatt");
    requireNonNegative(coolingRateCelsius, "coolingRateCelsius");
    return currentTemperatureCelsius
        + (powerWatts * heatCoefficientCelsiusPerWatt)
        - coolingRateCelsius;
  }

  /**
   * Calculates cooling without allowing the device to drop below ambient temperature.
   *
   * @param ambientTemperatureCelsius ambient temperature
   * @param currentTemperatureCelsius current device temperature
   * @param coolingRateCelsius cooling for the current tick
   * @return next temperature in degrees Celsius
   */
  public static double calculateCooledTemperature(
      double ambientTemperatureCelsius,
      double currentTemperatureCelsius,
      double coolingRateCelsius) {
    requireFinite(ambientTemperatureCelsius, "ambientTemperatureCelsius");
    requireFinite(currentTemperatureCelsius, "currentTemperatureCelsius");
    requireNonNegative(coolingRateCelsius, "coolingRateCelsius");
    return Math.max(ambientTemperatureCelsius, currentTemperatureCelsius - coolingRateCelsius);
  }

  private static void requireFinite(double value, String name) {
    if (!Double.isFinite(value)) {
      throw new IllegalArgumentException(name + " must be finite");
    }
  }

  private static void requireNonNegative(double value, String name) {
    if (!Double.isFinite(value)
        || value < TemperatureConstants.MINIMUM_COOLING_RATE_CELSIUS_PER_TICK) {
      throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
  }
}
