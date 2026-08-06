package com.broadcastsim.core.profile;

import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.property.PropertyDefinition;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/** Immutable reusable template that provides defaults for one device type. */
@Getter
public final class DeviceProfile {

  private final String name;
  private final DeviceType supportedDeviceType;
  private final Map<PropertyKey, PropertyDefinition<?>> defaultProperties;

  /**
   * Creates an immutable device profile.
   *
   * @param name the reusable profile name
   * @param supportedDeviceType the device type supported by this profile
   * @param defaultProperties the default property definitions
   */
  @Builder
  public DeviceProfile(
      String name,
      DeviceType supportedDeviceType,
      Map<PropertyKey, PropertyDefinition<?>> defaultProperties) {
    this.name = name;
    this.supportedDeviceType = supportedDeviceType;
    this.defaultProperties = Map.copyOf(defaultProperties);
  }
}
