package com.broadcastsim.core.signal;

import com.broadcastsim.core.connection.Connection;
import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Maintains the directed topology of logical broadcast device connections. */
public final class SignalGraph {

  private final Map<ConnectionId, Connection> connections = new HashMap<>();
  private final Map<DeviceId, Set<DeviceId>> downstreamAdjacency = new HashMap<>();
  private final Map<DeviceId, Set<DeviceId>> upstreamAdjacency = new HashMap<>();
  private final Map<DeviceId, Set<Connection>> outgoingConnections = new HashMap<>();

  /**
   * Adds a connection to the directed topology.
   *
   * @param connection the connection to add
   * @throws IllegalArgumentException if the connection identifier is already present
   */
  public void addConnection(Connection connection) {
    Objects.requireNonNull(connection, "connection must not be null");
    if (connections.putIfAbsent(connection.getConnectionId(), connection) != null) {
      throw new IllegalArgumentException("connection id is already present");
    }
    downstreamAdjacency
        .computeIfAbsent(connection.getSourceDeviceId(), ignored -> new HashSet<>())
        .add(connection.getTargetDeviceId());
    upstreamAdjacency
        .computeIfAbsent(connection.getTargetDeviceId(), ignored -> new HashSet<>())
        .add(connection.getSourceDeviceId());
    outgoingConnections
        .computeIfAbsent(connection.getSourceDeviceId(), ignored -> new HashSet<>())
        .add(connection);
  }

  /**
   * Removes a connection from the directed topology.
   *
   * @param connectionId the connection identifier to remove
   * @return {@code true} if the connection was present
   */
  public boolean removeConnection(ConnectionId connectionId) {
    Connection removedConnection =
        connections.remove(Objects.requireNonNull(connectionId, "connection id must not be null"));
    if (removedConnection == null) {
      return false;
    }
    removeAdjacencyIfUnused(removedConnection);
    removeOutgoingConnection(removedConnection);
    return true;
  }

  /**
   * Returns directly downstream devices for a device.
   *
   * @param deviceId the source device identifier
   * @return an immutable set of downstream device identifiers
   */
  public Set<DeviceId> getDownstream(DeviceId deviceId) {
    return Set.copyOf(
        downstreamAdjacency.getOrDefault(
            Objects.requireNonNull(deviceId, "device id must not be null"), Set.of()));
  }

  /**
   * Returns directly upstream devices for a device.
   *
   * @param deviceId the target device identifier
   * @return an immutable set of upstream device identifiers
   */
  public Set<DeviceId> getUpstream(DeviceId deviceId) {
    return Set.copyOf(
        upstreamAdjacency.getOrDefault(
            Objects.requireNonNull(deviceId, "device id must not be null"), Set.of()));
  }

  /**
   * Returns logical connections emitted by a source device.
   *
   * @param deviceId the source device identifier
   * @return an immutable set of outgoing connections
   */
  public Set<Connection> getOutgoingConnections(DeviceId deviceId) {
    return Set.copyOf(
        outgoingConnections.getOrDefault(
            Objects.requireNonNull(deviceId, "device id must not be null"), Set.of()));
  }

  /**
   * Returns whether the topology contains the supplied device.
   *
   * @param deviceId the device identifier
   * @return {@code true} if the device participates in a connection
   */
  public boolean contains(DeviceId deviceId) {
    DeviceId requiredDeviceId = Objects.requireNonNull(deviceId, "device id must not be null");
    return downstreamAdjacency.containsKey(requiredDeviceId)
        || upstreamAdjacency.containsKey(requiredDeviceId);
  }

  /**
   * Detects whether the directed topology contains a cycle.
   *
   * @return {@code true} if a directed cycle exists
   */
  public boolean detectCycles() {
    Set<DeviceId> visited = new HashSet<>();
    Set<DeviceId> activePath = new HashSet<>();
    for (DeviceId deviceId : downstreamAdjacency.keySet()) {
      if (hasCycle(deviceId, visited, activePath)) {
        return true;
      }
    }
    return false;
  }

  private void removeAdjacencyIfUnused(Connection connection) {
    if (hasConnection(connection.getSourceDeviceId(), connection.getTargetDeviceId())) {
      return;
    }
    removeNeighbor(
        downstreamAdjacency, connection.getSourceDeviceId(), connection.getTargetDeviceId());
    removeNeighbor(
        upstreamAdjacency, connection.getTargetDeviceId(), connection.getSourceDeviceId());
  }

  private boolean hasConnection(DeviceId sourceDeviceId, DeviceId targetDeviceId) {
    return connections.values().stream()
        .anyMatch(
            connection ->
                connection.getSourceDeviceId().equals(sourceDeviceId)
                    && connection.getTargetDeviceId().equals(targetDeviceId));
  }

  private void removeNeighbor(
      Map<DeviceId, Set<DeviceId>> adjacency, DeviceId deviceId, DeviceId neighborId) {
    Set<DeviceId> neighbors = adjacency.get(deviceId);
    if (neighbors == null) {
      return;
    }
    neighbors.remove(neighborId);
    if (neighbors.isEmpty()) {
      adjacency.remove(deviceId);
    }
  }

  private void removeOutgoingConnection(Connection connection) {
    Set<Connection> deviceConnections = outgoingConnections.get(connection.getSourceDeviceId());
    if (deviceConnections == null) {
      return;
    }
    deviceConnections.remove(connection);
    if (deviceConnections.isEmpty()) {
      outgoingConnections.remove(connection.getSourceDeviceId());
    }
  }

  private boolean hasCycle(DeviceId deviceId, Set<DeviceId> visited, Set<DeviceId> activePath) {
    if (activePath.contains(deviceId)) {
      return true;
    }
    if (!visited.add(deviceId)) {
      return false;
    }
    activePath.add(deviceId);
    for (DeviceId downstreamDeviceId : downstreamAdjacency.getOrDefault(deviceId, Set.of())) {
      if (hasCycle(downstreamDeviceId, visited, activePath)) {
        return true;
      }
    }
    activePath.remove(deviceId);
    return false;
  }
}
