package com.broadcastsim.core.device.router;

import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.base.PortSignalProducer;
import com.broadcastsim.core.device.base.PortSignalReceiver;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.calculators.BandwidthCalculator;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Routes received logical signals from configured input ports to selected output ports. */
public final class VideoRouter extends AbstractDevice
    implements PortSignalProducer, PortSignalReceiver {

  private final Map<Port, Port> routingTable = new HashMap<>();
  private final Map<Port, Signal> inputSignals = new HashMap<>();

  /**
   * Creates a video router with no ports or routes configured.
   *
   * @param deviceId the router identifier
   * @param deviceProfile the router profile
   * @param deviceRuntime the router runtime state
   */
  public VideoRouter(DeviceId deviceId, DeviceProfile deviceProfile, DeviceRuntime deviceRuntime) {
    super(deviceId, deviceProfile, deviceRuntime);
    if (getDeviceType() != DeviceType.ROUTER) {
      throw new IllegalArgumentException("video router requires a router profile");
    }
  }

  /**
   * Adds an input port that can receive a routed signal.
   *
   * @param inputPort the input port to add
   */
  public void addInput(Port inputPort) {
    requireDirection(inputPort, PortDirection.INPUT);
    addInputPort(inputPort);
  }

  /**
   * Adds an output port that can forward a routed signal.
   *
   * @param outputPort the output port to add
   */
  public void addOutput(Port outputPort) {
    requireDirection(outputPort, PortDirection.OUTPUT);
    addOutputPort(outputPort);
  }

  /**
   * Routes an input port to an output port.
   *
   * @param inputPort the input port that supplies the signal
   * @param outputPort the output port that forwards the signal
   */
  public void route(Port inputPort, Port outputPort) {
    requireRegisteredPort(inputPort, getInputPorts());
    requireRegisteredPort(outputPort, getOutputPorts());
    routingTable.put(outputPort, inputPort);
  }

  /**
   * Receives a logical signal for a particular input port.
   *
   * @param inputPort the input port that received the signal
   * @param signal the received signal
   */
  public void receiveSignal(Port inputPort, Signal signal) {
    requireRegisteredPort(inputPort, getInputPorts());
    inputSignals.put(inputPort, Objects.requireNonNull(signal, "signal must not be null"));
  }

  /**
   * Returns the routed signal for an output port, if the selected input has a signal.
   *
   * @param outputPort the output port to inspect
   * @return the selected input signal, if available
   */
  public Optional<Signal> forwardSignal(Port outputPort) {
    requireRegisteredPort(outputPort, getOutputPorts());
    return Optional.ofNullable(routingTable.get(outputPort)).map(inputSignals::get);
  }

  /**
   * Returns the number of configured routes.
   *
   * @return active route count
   */
  public int getActiveRouteCount() {
    return routingTable.size();
  }

  /**
   * Returns the aggregate bitrate of signals on routed inputs.
   *
   * @return current throughput in megabits per second
   */
  public double getCurrentThroughputMegabitsPerSecond() {
    List<Double> routedBitrates =
        routingTable.values().stream()
            .collect(java.util.stream.Collectors.toCollection(HashSet::new))
            .stream()
            .map(inputSignals::get)
            .filter(Objects::nonNull)
            .map(Signal::getBitrateMegabitsPerSecond)
            .toList();
    return BandwidthCalculator.calculateThroughput(routedBitrates);
  }

  /**
   * Returns the configured capacity of the router link.
   *
   * @return link capacity in megabits per second
   */
  public double getLinkCapacityMegabitsPerSecond() {
    return getPropertyValue(PropertyKey.LINK_CAPACITY, Double.class);
  }

  /**
   * Generates the routed signal for a specific output port.
   *
   * @param outputPort the output port to inspect
   * @return the selected input signal, if available
   */
  @Override
  public Optional<Signal> generateSignal(Port outputPort) {
    return forwardSignal(outputPort);
  }

  /**
   * Rejects port-agnostic input because routers require an explicit input port.
   *
   * @param signal the unsupported port-agnostic signal
   */
  @Override
  public void receiveSignal(Signal signal) {
    throw new UnsupportedOperationException("router input port is required");
  }

  /**
   * Returns no port-agnostic output because routers require an explicit output port.
   *
   * @return an empty result
   */
  @Override
  public Optional<Signal> generateSignal() {
    return Optional.empty();
  }

  /** Does not handle device events during the initial device behavior milestone. */
  @Override
  public void applyEvent() {}

  /** Does not apply time-based updates during the initial device behavior milestone. */
  @Override
  public void update() {}

  /** Does not calculate rules during the initial device behavior milestone. */
  @Override
  public void calculate() {}

  private void requireDirection(Port port, PortDirection expectedDirection) {
    Port requiredPort = Objects.requireNonNull(port, "port must not be null");
    if (requiredPort.getDirection() != expectedDirection) {
      throw new IllegalArgumentException("port direction does not match");
    }
  }

  private void requireRegisteredPort(Port port, Iterable<Port> registeredPorts) {
    if (!containsPort(Objects.requireNonNull(port, "port must not be null"), registeredPorts)) {
      throw new IllegalArgumentException("port is not registered");
    }
  }

  private boolean containsPort(Port port, Iterable<Port> registeredPorts) {
    for (Port registeredPort : registeredPorts) {
      if (registeredPort.equals(port)) {
        return true;
      }
    }
    return false;
  }
}
