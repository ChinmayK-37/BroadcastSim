package com.broadcastsim.web.dto;

import com.broadcastsim.core.common.enums.HealthStatus;

/** Presents an existing timeline observation using dashboard-ready display values. */
public record TimelineSnapshotResponse(
    String elapsedSimulationTime,
    int deviceCount,
    HealthStatus healthStatus,
    int activeAlarmCount) {}
