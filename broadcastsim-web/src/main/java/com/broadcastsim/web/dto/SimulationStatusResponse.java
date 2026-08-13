package com.broadcastsim.web.dto;

import com.broadcastsim.core.alarm.Alarm;
import com.broadcastsim.core.common.enums.SimulationState;
import com.broadcastsim.core.engine.SimulationTickResult;
import java.time.Instant;
import java.util.List;

/** Presents current simulation state read from the existing simulation core. */
public record SimulationStatusResponse(
    SimulationState simulationState,
    Instant simulationTime,
    long currentTick,
    SimulationTickResult latestTick,
    List<DeviceStatusResponse> devices,
    List<Alarm> activeAlarms) {

  /** Creates an immutable status response. */
  public SimulationStatusResponse {
    devices = List.copyOf(devices);
    activeAlarms = List.copyOf(activeAlarms);
  }
}
