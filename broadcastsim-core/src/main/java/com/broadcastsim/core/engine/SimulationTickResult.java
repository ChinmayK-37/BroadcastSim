package com.broadcastsim.core.engine;

import com.broadcastsim.core.alarm.Alarm;
import com.broadcastsim.core.device.runtime.DeviceSnapshot;
import com.broadcastsim.core.rule.RuleExecutionReport;
import com.broadcastsim.core.scenario.ScenarioEvent;
import com.broadcastsim.core.signal.SignalPropagationResult;
import com.broadcastsim.core.timeline.SimulationSnapshot;
import java.time.Instant;
import java.util.List;

/** Captures the observable outcomes of one deterministic simulation tick. */
public record SimulationTickResult(
    long tick,
    Instant timestamp,
    RuleExecutionReport ruleExecutionReport,
    SignalPropagationResult signalPropagationResult,
    List<ScenarioEvent> scenarioEvents,
    List<Alarm> alarms,
    List<DeviceSnapshot> snapshots,
    SimulationSnapshot simulationSnapshot) {

  /**
   * Creates an immutable simulation tick result.
   *
   * @param tick the advanced simulation tick number
   * @param timestamp the deterministic simulation timestamp
   * @param ruleExecutionReport aggregate device rule execution results
   * @param signalPropagationResult signal propagation outcome
   * @param scenarioEvents events applied before rule execution
   * @param alarms alarm states evaluated during the tick
   * @param snapshots immutable runtime snapshots captured at the end of the tick
   * @param simulationSnapshot immutable timeline observation created from the completed tick
   */
  public SimulationTickResult {
    scenarioEvents = List.copyOf(scenarioEvents);
    alarms = List.copyOf(alarms);
    snapshots = List.copyOf(snapshots);
  }
}
