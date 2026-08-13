package com.broadcastsim.core.timeline;

import com.broadcastsim.core.alarm.Alarm;
import com.broadcastsim.core.device.runtime.DeviceSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;

/** Immutable observation of all relevant simulation state at one simulation time. */
@Value
public class SimulationSnapshot {

  Instant simulationTime;
  List<DeviceSnapshot> deviceSnapshots;
  List<Alarm> activeAlarms;

  /**
   * Creates an immutable simulation snapshot from already calculated runtime state.
   *
   * @param simulationTime the simulation time represented by this snapshot
   * @param deviceSnapshots immutable device observations at the simulation time
   * @param activeAlarms current alarms observed at the simulation time
   */
  @Builder
  public SimulationSnapshot(
      Instant simulationTime, List<DeviceSnapshot> deviceSnapshots, List<Alarm> activeAlarms) {
    this.simulationTime =
        Objects.requireNonNull(simulationTime, "simulation time must not be null");
    this.deviceSnapshots = List.copyOf(deviceSnapshots);
    this.activeAlarms = List.copyOf(activeAlarms);
  }
}
