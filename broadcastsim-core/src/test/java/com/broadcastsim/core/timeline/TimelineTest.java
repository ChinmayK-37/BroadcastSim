package com.broadcastsim.core.timeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceSnapshot;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies in-memory ordering, retrieval, and immutability of simulation history. */
class TimelineTest {

  private static final Instant FIRST_TIME = Instant.ofEpochSecond(10);
  private static final Instant SECOND_TIME = Instant.ofEpochSecond(20);

  @Test
  void startsEmptyAndStoresSnapshotsInTimeAndInsertionOrder() {
    Timeline timeline = new Timeline();
    SimulationSnapshot second = snapshot(SECOND_TIME, deviceSnapshot(DeviceType.ENCODER, 60.0));
    SimulationSnapshot first = snapshot(FIRST_TIME, deviceSnapshot(DeviceType.CAMERA, 30.0));
    SimulationSnapshot sameTime = snapshot(FIRST_TIME, deviceSnapshot(DeviceType.ROUTER, 40.0));

    assertTrue(timeline.getAllSnapshots().isEmpty());
    timeline.addSnapshot(second);
    timeline.addSnapshot(first);
    timeline.addSnapshot(sameTime);

    assertEquals(List.of(first, sameTime, second), timeline.getAllSnapshots());
  }

  @Test
  void retrievesNearestAndRangeSnapshotsAndCanClearHistory() {
    Timeline timeline = new Timeline();
    SimulationSnapshot first = snapshot(FIRST_TIME, deviceSnapshot(DeviceType.CAMERA, 30.0));
    SimulationSnapshot second = snapshot(SECOND_TIME, deviceSnapshot(DeviceType.ENCODER, 60.0));
    timeline.addSnapshot(first);
    timeline.addSnapshot(second);

    assertEquals(first, timeline.getSnapshotAtOrNearest(Instant.ofEpochSecond(15)).orElseThrow());
    assertEquals(List.of(first, second), timeline.getSnapshotsWithin(FIRST_TIME, SECOND_TIME));
    assertThrows(
        IllegalArgumentException.class, () -> timeline.getSnapshotsWithin(SECOND_TIME, FIRST_TIME));
    timeline.clear();

    assertTrue(timeline.getAllSnapshots().isEmpty());
  }

  @Test
  void createsImmutableSnapshotsForMultipleDeviceObservations() {
    DeviceSnapshot camera = deviceSnapshot(DeviceType.CAMERA, 30.0);
    DeviceSnapshot encoder = deviceSnapshot(DeviceType.ENCODER, 60.0);
    SimulationSnapshot simulationSnapshot = snapshot(FIRST_TIME, camera, encoder);

    assertEquals(2, simulationSnapshot.getDeviceSnapshots().size());
    assertEquals(
        HealthStatus.NORMAL, simulationSnapshot.getDeviceSnapshots().getFirst().getHealthStatus());
    assertThrows(
        UnsupportedOperationException.class,
        () -> simulationSnapshot.getDeviceSnapshots().add(deviceSnapshot(DeviceType.ROUTER, 40.0)));
  }

  private SimulationSnapshot snapshot(Instant simulationTime, DeviceSnapshot... deviceSnapshots) {
    return SimulationSnapshot.builder()
        .simulationTime(simulationTime)
        .deviceSnapshots(List.of(deviceSnapshots))
        .activeAlarms(List.of())
        .build();
  }

  private DeviceSnapshot deviceSnapshot(DeviceType deviceType, double cpuUsage) {
    return DeviceSnapshot.builder()
        .deviceId(DeviceId.generate(deviceType))
        .timestamp(FIRST_TIME)
        .deviceState(DeviceState.ONLINE)
        .healthStatus(HealthStatus.NORMAL)
        .metrics(
            DeviceMetrics.builder()
                .cpuUsagePercentage(cpuUsage)
                .memoryUsageMb(256.0)
                .temperatureCelsius(42.0)
                .powerConsumptionWatts(20.0)
                .bandwidthMegabitsPerSecond(100.0)
                .build())
        .build();
  }
}
