package com.broadcastsim.core.timeline;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Stores immutable simulation snapshots in deterministic simulation-time order. */
public final class Timeline {

  private final List<SimulationSnapshot> snapshots = new ArrayList<>();

  /**
   * Stores a snapshot by simulation time, preserving insertion order for equal timestamps.
   *
   * @param simulationSnapshot the observation to store
   */
  public void addSnapshot(SimulationSnapshot simulationSnapshot) {
    SimulationSnapshot requiredSnapshot =
        Objects.requireNonNull(simulationSnapshot, "simulation snapshot must not be null");
    int insertionIndex = snapshots.size();
    while (insertionIndex > 0
        && snapshots
            .get(insertionIndex - 1)
            .getSimulationTime()
            .isAfter(requiredSnapshot.getSimulationTime())) {
      insertionIndex--;
    }
    snapshots.add(insertionIndex, requiredSnapshot);
  }

  /**
   * Returns all stored snapshots in deterministic simulation-time order.
   *
   * @return all simulation snapshots
   */
  public List<SimulationSnapshot> getAllSnapshots() {
    return List.copyOf(snapshots);
  }

  /**
   * Finds the snapshot at or nearest to a simulation time, preferring the earlier snapshot on ties.
   *
   * @param simulationTime the requested simulation time
   * @return the nearest snapshot, if one has been stored
   */
  public Optional<SimulationSnapshot> getSnapshotAtOrNearest(Instant simulationTime) {
    Instant requiredTime =
        Objects.requireNonNull(simulationTime, "simulation time must not be null");
    SimulationSnapshot nearestSnapshot = null;
    Duration nearestDistance = null;
    for (SimulationSnapshot snapshot : snapshots) {
      Duration distance = Duration.between(requiredTime, snapshot.getSimulationTime()).abs();
      if (nearestDistance == null || distance.compareTo(nearestDistance) < 0) {
        nearestSnapshot = snapshot;
        nearestDistance = distance;
      }
    }
    return Optional.ofNullable(nearestSnapshot);
  }

  /**
   * Returns every snapshot whose simulation time is within the inclusive range.
   *
   * @param startTime the inclusive start time
   * @param endTime the inclusive end time
   * @return ordered snapshots within the time range
   */
  public List<SimulationSnapshot> getSnapshotsWithin(Instant startTime, Instant endTime) {
    Instant requiredStartTime = Objects.requireNonNull(startTime, "start time must not be null");
    Instant requiredEndTime = Objects.requireNonNull(endTime, "end time must not be null");
    if (requiredStartTime.isAfter(requiredEndTime)) {
      throw new IllegalArgumentException("start time must not be after end time");
    }
    return snapshots.stream()
        .filter(snapshot -> !snapshot.getSimulationTime().isBefore(requiredStartTime))
        .filter(snapshot -> !snapshot.getSimulationTime().isAfter(requiredEndTime))
        .toList();
  }

  /** Removes all stored snapshots. */
  public void clear() {
    snapshots.clear();
  }
}
