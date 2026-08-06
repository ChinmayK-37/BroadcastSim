package com.broadcastsim.core.registry;

import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.valueobject.DeviceId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Provides indexed access to devices participating in a simulation. */
public final class DeviceRegistry {

  private final Map<DeviceId, Device> devices = new LinkedHashMap<>();

  /**
   * Registers a device under its unique identifier.
   *
   * @param device the device to register
   * @throws IllegalArgumentException if the identifier is already registered
   */
  public void register(Device device) {
    Objects.requireNonNull(device, "device must not be null");
    if (devices.putIfAbsent(device.getDeviceId(), device) != null) {
      throw new IllegalArgumentException("device id is already registered");
    }
  }

  /**
   * Unregisters the device associated with an identifier.
   *
   * @param deviceId the identifier to remove
   * @return the removed device, if it was registered
   */
  public Optional<Device> unregister(DeviceId deviceId) {
    return Optional.ofNullable(
        devices.remove(Objects.requireNonNull(deviceId, "device id must not be null")));
  }

  /**
   * Finds a device by identifier.
   *
   * @param deviceId the device identifier
   * @return the device, if registered
   */
  public Optional<Device> get(DeviceId deviceId) {
    return Optional.ofNullable(
        devices.get(Objects.requireNonNull(deviceId, "device id must not be null")));
  }

  /**
   * Returns an immutable snapshot of all registered devices.
   *
   * @return all devices in registration order
   */
  public Collection<Device> getAll() {
    return List.copyOf(devices.values());
  }

  /**
   * Returns whether an identifier is currently registered.
   *
   * @param deviceId the device identifier
   * @return {@code true} if a device is registered for the identifier
   */
  public boolean contains(DeviceId deviceId) {
    return devices.containsKey(Objects.requireNonNull(deviceId, "device id must not be null"));
  }

  /**
   * Returns an immutable snapshot of devices belonging to one type.
   *
   * @param deviceType the device type to find
   * @return all devices of the requested type
   */
  public Collection<Device> getByType(DeviceType deviceType) {
    Objects.requireNonNull(deviceType, "device type must not be null");
    List<Device> matchingDevices = new ArrayList<>();
    for (Device device : devices.values()) {
      if (device.getDeviceType() == deviceType) {
        matchingDevices.add(device);
      }
    }
    return List.copyOf(matchingDevices);
  }
}
