package com.broadcastsim.core.device.base;

import com.broadcastsim.core.profile.DeviceProfile;

/** Defines the extension point for creating devices from reusable profiles. */
public interface DeviceFactory {

  /**
   * Creates a device from a compatible profile.
   *
   * @param profile the profile to apply to the device
   * @return the created device
   */
  Device create(DeviceProfile profile);
}
