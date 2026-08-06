package com.broadcastsim.core.signal;

import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.List;
import java.util.Set;
import lombok.Getter;

/** Captures the observable outcome of one logical signal propagation request. */
@Getter
public final class SignalPropagationResult {

  private final Set<DeviceId> visitedDevices;
  private final List<Signal> propagatedSignals;
  private final List<ConnectionId> failedPropagations;
  private final List<ConnectionId> disconnectedLinks;
  private final boolean cycleDetected;

  /**
   * Creates an immutable propagation result.
   *
   * @param visitedDevices devices reached during propagation
   * @param propagatedSignals signals delivered over connected links
   * @param failedPropagations links whose propagation failed
   * @param disconnectedLinks links skipped because they are disconnected
   * @param cycleDetected whether the graph was cyclic
   */
  public SignalPropagationResult(
      Set<DeviceId> visitedDevices,
      List<Signal> propagatedSignals,
      List<ConnectionId> failedPropagations,
      List<ConnectionId> disconnectedLinks,
      boolean cycleDetected) {
    this.visitedDevices = Set.copyOf(visitedDevices);
    this.propagatedSignals = List.copyOf(propagatedSignals);
    this.failedPropagations = List.copyOf(failedPropagations);
    this.disconnectedLinks = List.copyOf(disconnectedLinks);
    this.cycleDetected = cycleDetected;
  }
}
