package com.broadcastsim.core.engine;

import com.broadcastsim.core.connection.ConnectionRegistry;
import com.broadcastsim.core.event.EventQueue;
import com.broadcastsim.core.registry.DeviceRegistry;
import java.util.Objects;

/** Groups the infrastructure components shared by the broadcast engine. */
public final class SimulationContext {

  private final DeviceRegistry deviceRegistry;
  private final ConnectionRegistry connectionRegistry;
  private final SimulationClock simulationClock;
  private final EventQueue eventQueue;

  /**
   * Creates an engine context from its required infrastructure components.
   *
   * @param deviceRegistry the device registry
   * @param connectionRegistry the connection registry
   * @param simulationClock the simulation clock
   * @param eventQueue the pending event queue
   */
  public SimulationContext(
      DeviceRegistry deviceRegistry,
      ConnectionRegistry connectionRegistry,
      SimulationClock simulationClock,
      EventQueue eventQueue) {
    this.deviceRegistry =
        Objects.requireNonNull(deviceRegistry, "device registry must not be null");
    this.connectionRegistry =
        Objects.requireNonNull(connectionRegistry, "connection registry must not be null");
    this.simulationClock =
        Objects.requireNonNull(simulationClock, "simulation clock must not be null");
    this.eventQueue = Objects.requireNonNull(eventQueue, "event queue must not be null");
  }

  /**
   * Returns the registered devices.
   *
   * @return the device registry
   */
  public DeviceRegistry getDeviceRegistry() {
    return deviceRegistry;
  }

  /**
   * Returns the connection registry integration point.
   *
   * @return the connection registry
   */
  public ConnectionRegistry getConnectionRegistry() {
    return connectionRegistry;
  }

  /**
   * Returns the simulation clock.
   *
   * @return the simulation clock
   */
  public SimulationClock getSimulationClock() {
    return simulationClock;
  }

  /**
   * Returns the pending event queue.
   *
   * @return the event queue
   */
  public EventQueue getEventQueue() {
    return eventQueue;
  }
}
