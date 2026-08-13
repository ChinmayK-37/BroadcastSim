package com.broadcastsim.web.dto;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.device.runtime.DeviceMetrics;

/** Presents existing device runtime state to web clients without recalculation. */
public record DeviceStatusResponse(
    String deviceId,
    DeviceType deviceType,
    DeviceState deviceState,
    HealthStatus healthStatus,
    DeviceMetrics metrics) {}
