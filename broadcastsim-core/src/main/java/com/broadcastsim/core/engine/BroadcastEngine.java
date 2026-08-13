package com.broadcastsim.core.engine;

import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.SimulationState;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.device.runtime.DeviceSnapshot;
import com.broadcastsim.core.engineering.calculators.HealthCalculator;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.NetworkConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;
import com.broadcastsim.core.rule.RuleEngine;
import com.broadcastsim.core.rule.RuleExecutionReport;
import com.broadcastsim.core.signal.SignalGraph;
import com.broadcastsim.core.signal.SignalPropagationEngine;
import com.broadcastsim.core.signal.SignalPropagationResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Owns the lifecycle state of a broadcast simulation. */
public final class BroadcastEngine {

  private final SimulationContext context;
  private final RuleEngine ruleEngine;
  private final SignalPropagationEngine signalPropagationEngine;
  private SimulationState simulationState = SimulationState.STOPPED;

  /**
   * Creates an engine backed by the supplied shared context.
   *
   * @param context the engine infrastructure context
   */
  public BroadcastEngine(SimulationContext context) {
    this(context, new SignalGraph());
  }

  /**
   * Creates an engine using the supplied signal topology.
   *
   * @param context the engine infrastructure context
   * @param signalGraph the topology used during signal propagation
   */
  public BroadcastEngine(SimulationContext context, SignalGraph signalGraph) {
    this.context = Objects.requireNonNull(context, "simulation context must not be null");
    SignalGraph requiredSignalGraph =
        Objects.requireNonNull(signalGraph, "signal graph must not be null");
    this.ruleEngine = new RuleEngine(context.getDeviceRegistry());
    this.signalPropagationEngine =
        new SignalPropagationEngine(context.getDeviceRegistry(), requiredSignalGraph);
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
   * Executes one deterministic simulation tick while the simulation is running.
   *
   * @return the observable outcome of the tick
   * @throws IllegalStateException if the simulation is not running
   */
  public SimulationTickResult tick() {
    requireState(SimulationState.RUNNING);
    long tick = context.getSimulationClock().advance();
    Instant timestamp = simulationTimestamp(tick);
    RuleExecutionReport ruleExecutionReport = ruleEngine.execute(timestamp);
    SignalPropagationResult signalPropagationResult = signalPropagationEngine.propagate();
    evaluateHealth();
    return new SimulationTickResult(
        tick, timestamp, ruleExecutionReport, signalPropagationResult, captureSnapshots(timestamp));
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

  private Instant simulationTimestamp(long tick) {
    return Instant.EPOCH.plus(context.getSimulationClock().getTickInterval().multipliedBy(tick));
  }

  private void evaluateHealth() {
    for (Device device : context.getDeviceRegistry().getAll()) {
      if (device instanceof AbstractDevice abstractDevice) {
        DeviceRuntime runtime = abstractDevice.getDeviceRuntime();
        runtime.updateHealthStatus(
            HealthCalculator.evaluateOperationalHealth(runtime.getOperationalFailures()));
        if (runtime.getHealthStatus() == HealthStatus.FAILED) {
          continue;
        }
        runtime.updateHealthStatus(
            HealthCalculator.evaluateOverallHealth(
                HealthCalculator.evaluateCpuUsage(runtime.getMetrics().getCpuUsagePercentage()),
                HealthCalculator.evaluateMemoryUsage(
                    MemoryConstants.MINIMUM_MEMORY_UTILIZATION_PERCENTAGE),
                HealthCalculator.evaluateTemperature(runtime.getMetrics().getTemperatureCelsius()),
                HealthCalculator.evaluatePowerUtilization(
                    runtime.getMetrics().getPowerConsumptionWatts()
                        / maximumPowerWatts(device)
                        * PowerConstants.CPU_PERCENTAGE_DIVISOR),
                HealthCalculator.evaluateSignalQuality(NetworkConstants.MAXIMUM_PERCENTAGE)));
      }
    }
  }

  private double maximumPowerWatts(Device device) {
    return switch (device.getDeviceType()) {
      case CAMERA -> PowerConstants.CAMERA_MAXIMUM_POWER_WATTS;
      case ROUTER -> PowerConstants.ROUTER_MAXIMUM_POWER_WATTS;
      case ENCODER -> PowerConstants.ENCODER_MAXIMUM_POWER_WATTS;
      default ->
          throw new IllegalArgumentException("unsupported device type for health evaluation");
    };
  }

  private List<DeviceSnapshot> captureSnapshots(Instant timestamp) {
    List<DeviceSnapshot> snapshots = new ArrayList<>();
    for (Device device : context.getDeviceRegistry().getAll()) {
      if (device instanceof AbstractDevice abstractDevice) {
        DeviceRuntime runtime = abstractDevice.getDeviceRuntime();
        snapshots.add(
            DeviceSnapshot.builder()
                .deviceId(device.getDeviceId())
                .timestamp(timestamp)
                .deviceState(runtime.getDeviceState())
                .metrics(runtime.getMetrics())
                .build());
      }
    }
    return snapshots;
  }

  private void requireState(SimulationState expectedState) {
    if (simulationState != expectedState) {
      throw new IllegalStateException("simulation state does not allow this operation");
    }
  }
}
