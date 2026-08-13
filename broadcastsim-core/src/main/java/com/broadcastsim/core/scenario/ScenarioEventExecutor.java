package com.broadcastsim.core.scenario;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.runtime.DeviceLifecycleManager;
import com.broadcastsim.core.property.Property;
import com.broadcastsim.core.registry.DeviceRegistry;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Applies due scenario events to registered device configuration and lifecycle state. */
public final class ScenarioEventExecutor {

  private final DeviceRegistry deviceRegistry;
  private final DeviceLifecycleManager lifecycleManager;

  /**
   * Creates an executor that resolves event targets from the supplied device registry.
   *
   * @param deviceRegistry the registry containing scenario event targets
   */
  public ScenarioEventExecutor(DeviceRegistry deviceRegistry) {
    this.deviceRegistry =
        Objects.requireNonNull(deviceRegistry, "device registry must not be null");
    this.lifecycleManager = new DeviceLifecycleManager();
  }

  /**
   * Executes each pending event due at or before the supplied simulation timestamp.
   *
   * <p>Events with the same timestamp retain their scenario insertion order.
   *
   * @param scenario the scenario containing scheduled events
   * @param simulationTimestamp the current simulation time
   * @return events evaluated during this execution, in deterministic order
   */
  public List<ScenarioEvent> executeDueEvents(Scenario scenario, Instant simulationTimestamp) {
    Objects.requireNonNull(scenario, "scenario must not be null");
    Instant requiredTimestamp =
        Objects.requireNonNull(simulationTimestamp, "simulation timestamp must not be null");
    List<ScenarioEvent> dueEvents =
        scenario.getEvents().stream()
            .filter(this::isPending)
            .filter(event -> !event.getScheduledTimestamp().isAfter(requiredTimestamp))
            .sorted(Comparator.comparing(ScenarioEvent::getScheduledTimestamp))
            .toList();
    dueEvents.forEach(this::execute);
    return dueEvents;
  }

  private boolean isPending(ScenarioEvent scenarioEvent) {
    return scenarioEvent.getExecutionStatus() == ScenarioEventStatus.PENDING;
  }

  private void execute(ScenarioEvent scenarioEvent) {
    try {
      AbstractDevice device = targetDevice(scenarioEvent);
      switch (scenarioEvent.getEventType()) {
        case SET_PROPERTY -> setProperty(device, scenarioEvent);
        case DEVICE_ENABLE ->
            lifecycleManager.transition(device.getDeviceRuntime(), DeviceState.ONLINE);
        case DEVICE_DISABLE ->
            lifecycleManager.transition(device.getDeviceRuntime(), DeviceState.OFFLINE);
      }
      scenarioEvent.markExecuted();
    } catch (IllegalArgumentException | IllegalStateException exception) {
      scenarioEvent.markFailed();
    }
  }

  private AbstractDevice targetDevice(ScenarioEvent scenarioEvent) {
    Device device =
        deviceRegistry
            .get(scenarioEvent.getTargetDeviceId())
            .orElseThrow(
                () -> new IllegalArgumentException("scenario event target does not exist"));
    if (!(device instanceof AbstractDevice abstractDevice)) {
      throw new IllegalArgumentException("scenario event target does not support configuration");
    }
    return abstractDevice;
  }

  private void setProperty(AbstractDevice device, ScenarioEvent scenarioEvent) {
    Property<?> property =
        device
            .getPropertyContainer()
            .getProperty(scenarioEvent.getPropertyKey())
            .orElseThrow(
                () -> new IllegalArgumentException("scenario event property does not exist"));
    if (property.getDefinition().getCategory() != PropertyCategory.CONFIGURATION
        || property.getDefinition().getAccess() != PropertyAccess.READ_WRITE) {
      throw new IllegalArgumentException("scenario event property is not writable configuration");
    }
    device
        .getPropertyContainer()
        .updateProperty(scenarioEvent.getPropertyKey(), scenarioEvent.getPropertyValue());
  }
}
