package com.broadcastsim.core.signal;

import com.broadcastsim.core.common.enums.ConnectionStatus;
import com.broadcastsim.core.connection.Connection;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.base.PortSignalProducer;
import com.broadcastsim.core.device.base.PortSignalReceiver;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Propagates logical signals through a directed signal graph without simulation scheduling. */
public final class SignalPropagationEngine {

  private final DeviceRegistry deviceRegistry;
  private final SignalGraph signalGraph;

  /**
   * Creates an engine using the supplied device registry and topology graph.
   *
   * @param deviceRegistry the registered devices
   * @param signalGraph the directed broadcast topology
   */
  public SignalPropagationEngine(DeviceRegistry deviceRegistry, SignalGraph signalGraph) {
    this.deviceRegistry = deviceRegistry;
    this.signalGraph = signalGraph;
  }

  /**
   * Propagates currently generated logical signals across the topology graph.
   *
   * @return the observable outcome of the propagation request
   */
  public SignalPropagationResult propagate() {
    Set<DeviceId> visitedDevices = new HashSet<>();
    List<Signal> propagatedSignals = new ArrayList<>();
    List<ConnectionId> failedPropagations = new ArrayList<>();
    List<ConnectionId> disconnectedLinks = new ArrayList<>();

    if (signalGraph.detectCycles()) {
      return new SignalPropagationResult(
          visitedDevices, propagatedSignals, failedPropagations, disconnectedLinks, true);
    }

    Deque<DeviceId> devicesToVisit = new ArrayDeque<>();
    for (Device device : deviceRegistry.getAll()) {
      if (device.generateSignal().isPresent()) {
        devicesToVisit.addLast(device.getDeviceId());
      }
    }

    while (!devicesToVisit.isEmpty()) {
      DeviceId sourceDeviceId = devicesToVisit.removeFirst();
      if (!visitedDevices.add(sourceDeviceId)) {
        continue;
      }
      deviceRegistry
          .get(sourceDeviceId)
          .ifPresent(
              sourceDevice ->
                  propagateOutgoingConnections(
                      sourceDevice,
                      devicesToVisit,
                      propagatedSignals,
                      failedPropagations,
                      disconnectedLinks));
    }

    return new SignalPropagationResult(
        visitedDevices, propagatedSignals, failedPropagations, disconnectedLinks, false);
  }

  private void propagateOutgoingConnections(
      Device sourceDevice,
      Deque<DeviceId> devicesToVisit,
      List<Signal> propagatedSignals,
      List<ConnectionId> failedPropagations,
      List<ConnectionId> disconnectedLinks) {
    for (Connection connection : signalGraph.getOutgoingConnections(sourceDevice.getDeviceId())) {
      if (connection.getStatus() != ConnectionStatus.CONNECTED) {
        disconnectedLinks.add(connection.getConnectionId());
        continue;
      }
      Optional<Signal> signal = generateSignal(sourceDevice, connection);
      if (signal.isEmpty()) {
        failedPropagations.add(connection.getConnectionId());
        continue;
      }
      Optional<Device> targetDevice = deviceRegistry.get(connection.getTargetDeviceId());
      if (targetDevice.isEmpty()) {
        failedPropagations.add(connection.getConnectionId());
        continue;
      }
      Signal propagatedSignal = signal.orElseThrow();
      deliverSignal(targetDevice.orElseThrow(), connection, propagatedSignal);
      propagatedSignals.add(propagatedSignal);
      devicesToVisit.addLast(connection.getTargetDeviceId());
    }
  }

  private Optional<Signal> generateSignal(Device device, Connection connection) {
    if (device instanceof PortSignalProducer portSignalProducer) {
      return portSignalProducer.generateSignal(connection.getSourcePort());
    }
    return device.generateSignal();
  }

  private void deliverSignal(Device targetDevice, Connection connection, Signal signal) {
    if (targetDevice instanceof PortSignalReceiver portSignalReceiver) {
      portSignalReceiver.receiveSignal(connection.getTargetPort(), signal);
      return;
    }
    targetDevice.receiveSignal(signal);
  }
}
