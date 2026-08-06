package com.broadcastsim.core.device.base;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.device.runtime.DeviceLifecycleManager;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.Property;
import com.broadcastsim.core.property.PropertyContainer;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/** Provides common identity, lifecycle, property, and port behavior for devices. */
@Getter
public abstract class AbstractDevice implements Device {

  private final DeviceId deviceId;
  private final DeviceType deviceType;
  private final DeviceProfile deviceProfile;
  private final PropertyContainer propertyContainer;
  private final DeviceRuntime deviceRuntime;
  private final List<Port> inputPorts = new ArrayList<>();
  private final List<Port> outputPorts = new ArrayList<>();

  /**
   * Creates a device with its identity and reusable profile.
   *
   * @param deviceId the unique device identifier
   * @param deviceProfile the profile that supplies device defaults
   * @param deviceRuntime the mutable runtime state
   */
  protected AbstractDevice(
      DeviceId deviceId, DeviceProfile deviceProfile, DeviceRuntime deviceRuntime) {
    this.deviceId = Objects.requireNonNull(deviceId, "device id must not be null");
    this.deviceProfile = Objects.requireNonNull(deviceProfile, "device profile must not be null");
    this.deviceType = deviceProfile.getSupportedDeviceType();
    this.propertyContainer = new PropertyContainer();
    this.deviceRuntime = Objects.requireNonNull(deviceRuntime, "device runtime must not be null");
  }

  /** Loads default runtime properties and transitions the device to the online state. */
  @Override
  public void initialize() {
    deviceProfile.getDefaultProperties().values().stream()
        .map(Property::fromDefinition)
        .forEach(propertyContainer::addProperty);
    new DeviceLifecycleManager().transition(deviceRuntime, DeviceState.ONLINE);
  }

  /**
   * Returns an immutable view of the device input ports.
   *
   * @return input ports
   */
  public List<Port> getInputPorts() {
    return List.copyOf(inputPorts);
  }

  /**
   * Returns an immutable view of the device output ports.
   *
   * @return output ports
   */
  public List<Port> getOutputPorts() {
    return List.copyOf(outputPorts);
  }

  /**
   * Adds an input port to the device framework state.
   *
   * @param port the input port
   */
  protected final void addInputPort(Port port) {
    inputPorts.add(Objects.requireNonNull(port, "port must not be null"));
  }

  /**
   * Adds an output port to the device framework state.
   *
   * @param port the output port
   */
  protected final void addOutputPort(Port port) {
    outputPorts.add(Objects.requireNonNull(port, "port must not be null"));
  }

  /**
   * Updates the lifecycle state from shared device behavior.
   *
   * @param deviceState the new lifecycle state
   */
  protected final void transitionTo(DeviceState deviceState) {
    new DeviceLifecycleManager().transition(deviceRuntime, deviceState);
  }

  /**
   * Returns the device lifecycle state stored by its runtime model.
   *
   * @return the device lifecycle state
   */
  @Override
  public final DeviceState getDeviceState() {
    return deviceRuntime.getDeviceState();
  }

  /**
   * Returns a typed runtime property value.
   *
   * @param key the property key
   * @param valueType the expected property value type
   * @param <T> the property value type
   * @return the typed property value
   */
  protected final <T> T getPropertyValue(PropertyKey key, Class<T> valueType) {
    Property<?> property =
        propertyContainer
            .getProperty(key)
            .orElseThrow(() -> new IllegalArgumentException("required property is not present"));
    return valueType.cast(property.getValue());
  }
}
