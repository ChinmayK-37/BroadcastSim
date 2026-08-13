package com.broadcastsim.web.service;

import com.broadcastsim.core.alarm.Alarm;
import com.broadcastsim.core.common.enums.AlarmState;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.engine.BroadcastEngine;
import com.broadcastsim.core.engine.SimulationTickResult;
import com.broadcastsim.core.timeline.SimulationSnapshot;
import com.broadcastsim.web.dto.DeviceStatusResponse;
import com.broadcastsim.web.dto.SimulationStatusResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/** Translates web use cases into calls to the existing broadcast simulation core. */
@Service
public class SimulationFacade {

  private final BroadcastEngine broadcastEngine;
  private SimulationTickResult latestTick;

  /**
   * Creates the facade around the configured in-memory simulation engine.
   *
   * @param broadcastEngine the core simulation engine
   */
  public SimulationFacade(BroadcastEngine broadcastEngine) {
    this.broadcastEngine = broadcastEngine;
  }

  /**
   * Starts the simulation.
   *
   * @return current simulation status
   */
  public synchronized SimulationStatusResponse start() {
    broadcastEngine.start();
    return status();
  }

  /**
   * Pauses the simulation.
   *
   * @return current simulation status
   */
  public synchronized SimulationStatusResponse pause() {
    broadcastEngine.pause();
    return status();
  }

  /**
   * Resumes the simulation.
   *
   * @return current simulation status
   */
  public synchronized SimulationStatusResponse resume() {
    broadcastEngine.resume();
    return status();
  }

  /**
   * Stops the simulation.
   *
   * @return current simulation status
   */
  public synchronized SimulationStatusResponse stop() {
    broadcastEngine.stop();
    return status();
  }

  /**
   * Advances the simulation by one manual tick.
   *
   * @return current simulation status after the completed tick
   */
  public synchronized SimulationStatusResponse tick() {
    latestTick = broadcastEngine.tick();
    return status();
  }

  /**
   * Returns current simulation state read directly from the core model.
   *
   * @return current simulation status
   */
  public synchronized SimulationStatusResponse status() {
    return new SimulationStatusResponse(
        broadcastEngine.getSimulationState(),
        simulationTime(),
        broadcastEngine.getContext().getSimulationClock().getCurrentTick(),
        latestTick,
        deviceStatuses(),
        activeAlarms());
  }

  /**
   * Returns the immutable in-memory timeline collected by completed ticks.
   *
   * @return ordered simulation snapshots
   */
  public synchronized List<SimulationSnapshot> timeline() {
    return broadcastEngine.getTimeline().getAllSnapshots();
  }

  private Instant simulationTime() {
    return latestTick == null ? Instant.EPOCH : latestTick.timestamp();
  }

  private List<DeviceStatusResponse> deviceStatuses() {
    return broadcastEngine.getContext().getDeviceRegistry().getAll().stream()
        .filter(AbstractDevice.class::isInstance)
        .map(AbstractDevice.class::cast)
        .map(this::deviceStatus)
        .toList();
  }

  private DeviceStatusResponse deviceStatus(AbstractDevice device) {
    return new DeviceStatusResponse(
        device.getDeviceId().toString(),
        device.getDeviceType(),
        device.getDeviceState(),
        device.getDeviceRuntime().getHealthStatus(),
        device.getDeviceRuntime().getMetrics());
  }

  private List<Alarm> activeAlarms() {
    if (latestTick == null) {
      return List.of();
    }
    return latestTick.alarms().stream()
        .filter(alarm -> alarm.getState() == AlarmState.RAISED)
        .toList();
  }
}
