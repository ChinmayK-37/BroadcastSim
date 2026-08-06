package com.broadcastsim.core.device.runtime;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/** Immutable record of one device's runtime state at a point in simulation time. */
@Value
@Builder
public class DeviceSnapshot {

  DeviceId deviceId;
  Instant timestamp;
  DeviceState deviceState;
  DeviceMetrics metrics;
}
