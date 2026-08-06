package com.broadcastsim.core.engine;

import com.broadcastsim.core.common.enums.SimulationState;
import java.util.Objects;

/** Owns the lifecycle state of a broadcast simulation. */
public final class BroadcastEngine {

  private final SimulationContext context;
  private SimulationState simulationState = SimulationState.STOPPED;

  /**
   * Creates an engine backed by the supplied shared context.
   *
   * @param context the engine infrastructure context
   */
  public BroadcastEngine(SimulationContext context) {
    this.context = Objects.requireNonNull(context, "simulation context must not be null");
  }

  /**
   * Starts a stopped simulation.
   *
   * @throws IllegalStateException if the simulation is not stopped
   */
  public void start() {
    requireState(SimulationState.STOPPED);
    simulationState = SimulationState.RUNNING;
  }

  /** Stops a running or paused simulation. */
  public void stop() {
    simulationState = SimulationState.STOPPED;
  }

  /**
   * Pauses a running simulation.
   *
   * @throws IllegalStateException if the simulation is not running
   */
  public void pause() {
    requireState(SimulationState.RUNNING);
    simulationState = SimulationState.PAUSED;
  }

  /**
   * Resumes a paused simulation.
   *
   * @throws IllegalStateException if the simulation is not paused
   */
  public void resume() {
    requireState(SimulationState.PAUSED);
    simulationState = SimulationState.RUNNING;
  }

  /**
   * Returns the current simulation lifecycle state.
   *
   * @return the simulation state
   */
  public SimulationState getSimulationState() {
    return simulationState;
  }

  /**
   * Returns the engine's shared infrastructure context.
   *
   * @return the simulation context
   */
  public SimulationContext getContext() {
    return context;
  }

  private void requireState(SimulationState expectedState) {
    if (simulationState != expectedState) {
      throw new IllegalStateException("simulation state does not allow this operation");
    }
  }
}
