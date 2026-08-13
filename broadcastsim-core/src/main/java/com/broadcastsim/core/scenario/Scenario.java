package com.broadcastsim.core.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Holds the ordered sequence of deterministic changes to apply during a simulation. */
public final class Scenario {

  private final List<ScenarioEvent> events = new ArrayList<>();

  /**
   * Adds an event in deterministic insertion order.
   *
   * @param scenarioEvent the event to schedule
   */
  public void schedule(ScenarioEvent scenarioEvent) {
    events.add(Objects.requireNonNull(scenarioEvent, "scenario event must not be null"));
  }

  /**
   * Returns a snapshot of scheduled events in insertion order.
   *
   * @return scheduled scenario events
   */
  public List<ScenarioEvent> getEvents() {
    return List.copyOf(events);
  }
}
