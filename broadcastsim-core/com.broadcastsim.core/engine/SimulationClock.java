package com.broadcastsim.core.engine;

import java.time.Duration;
import java.util.Objects;

/** Tracks discrete simulation time independently from real time. */
public final class SimulationClock {

  private final Duration tickInterval;
  private long currentTick;

  /**
   * Creates a clock with the specified simulation tick interval.
   *
   * @param tickInterval the duration represented by one tick
   */
  public SimulationClock(Duration tickInterval) {
    this.tickInterval = Objects.requireNonNull(tickInterval, "tick interval must not be null");
  }

  /**
   * Returns the current simulation tick.
   *
   * @return the current tick number
   */
  public long getCurrentTick() {
    return currentTick;
  }

  /**
   * Returns the duration represented by one simulation tick.
   *
   * @return the tick interval
   */
  public Duration getTickInterval() {
    return tickInterval;
  }

  /**
   * Advances the clock by one simulation tick.
   *
   * @return the advanced tick number
   */
  public long advance() {
    return ++currentTick;
  }
}
