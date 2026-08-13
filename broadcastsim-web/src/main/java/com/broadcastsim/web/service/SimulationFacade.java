package com.broadcastsim.web.service;

import com.broadcastsim.core.alarm.Alarm;
import com.broadcastsim.core.common.enums.AlarmState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.engine.BroadcastEngine;
import com.broadcastsim.core.engine.SimulationTickResult;
import com.broadcastsim.core.scenario.ScenarioEvent;
import com.broadcastsim.core.timeline.SimulationSnapshot;
import com.broadcastsim.web.dto.DeviceStatusResponse;
import com.broadcastsim.web.dto.SimulationStatusResponse;
import com.broadcastsim.web.dto.TimelineSnapshotResponse;
import java.time.Duration;
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

  /**
   * Returns the ten most recent timeline snapshots in simulation-time order.
   *
   * @return recent simulation snapshots
   */
  public synchronized List<TimelineSnapshotResponse> recentTimeline() {
    List<SimulationSnapshot> snapshots = timeline();
    return snapshots.subList(Math.max(0, snapshots.size() - 10), snapshots.size()).stream()
        .map(this::timelineSnapshot)
        .toList();
  }

  /**
   * Returns elapsed simulation time derived from the existing clock.
   *
   * @return elapsed simulation time in {@code HH:mm:ss} format
   */
  public synchronized String elapsedSimulationTime() {
    return formatElapsedTime(
        broadcastEngine
            .getContext()
            .getSimulationClock()
            .getTickInterval()
            .multipliedBy(broadcastEngine.getContext().getSimulationClock().getCurrentTick()));
  }

  /**
   * Schedules a camera frame-rate update for the next simulation tick.
   *
   * @param framesPerSecond requested camera frame rate
   * @return current simulation status
   */
  public synchronized SimulationStatusResponse scheduleCameraFramesPerSecond(
      double framesPerSecond) {
    AbstractDevice camera =
        broadcastEngine.getContext().getDeviceRegistry().getByType(DeviceType.CAMERA).stream()
            .filter(AbstractDevice.class::isInstance)
            .map(AbstractDevice.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("configured camera is not available"));
    broadcastEngine
        .getScenario()
        .schedule(
            ScenarioEvent.setProperty(
                nextSimulationTimestamp(), camera.getDeviceId(), PropertyKey.FPS, framesPerSecond));
    return status();
  }

  private Instant simulationTime() {
    return latestTick == null ? Instant.EPOCH : latestTick.timestamp();
  }

  private Instant nextSimulationTimestamp() {
    long nextTick = broadcastEngine.getContext().getSimulationClock().getCurrentTick() + 1;
    return Instant.EPOCH.plus(
        broadcastEngine.getContext().getSimulationClock().getTickInterval().multipliedBy(nextTick));
  }

  private TimelineSnapshotResponse timelineSnapshot(SimulationSnapshot simulationSnapshot) {
    return new TimelineSnapshotResponse(
        formatElapsedTime(Duration.between(Instant.EPOCH, simulationSnapshot.getSimulationTime())),
        simulationSnapshot.getDeviceSnapshots().size(),
        simulationSnapshot.getDeviceSnapshots().isEmpty()
            ? null
            : simulationSnapshot.getDeviceSnapshots().getFirst().getHealthStatus(),
        simulationSnapshot.getActiveAlarms().size());
  }

  private String formatElapsedTime(Duration elapsedTime) {
    long totalSeconds = elapsedTime.toSeconds();
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
