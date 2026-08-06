package com.broadcastsim.core.device.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.OperationalFailureType;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies device runtime state, health evaluation, snapshots, and lifecycle transitions. */
class DeviceRuntimeModelTest {

  @Test
  void evaluatesDocumentedCpuAndTemperatureThresholds() {
    HealthEvaluator healthEvaluator = new HealthEvaluator();

    assertEquals(DeviceState.ONLINE, healthEvaluator.evaluate(metrics(79.0, 69.0)));
    assertEquals(DeviceState.WARNING, healthEvaluator.evaluate(metrics(80.0, 69.0)));
    assertEquals(DeviceState.WARNING, healthEvaluator.evaluate(metrics(79.0, 70.0)));
    assertEquals(DeviceState.FAILED, healthEvaluator.evaluate(metrics(95.0, 69.0)));
    assertEquals(DeviceState.FAILED, healthEvaluator.evaluate(metrics(79.0, 85.0)));
  }

  @Test
  void updatesRuntimeMetricsHealthTimestampAndFlags() {
    DeviceRuntime deviceRuntime = runtime(DeviceState.CREATED);
    Instant updatedAt = Instant.parse("2026-08-05T00:00:01Z");

    deviceRuntime.updateMetrics(metrics(42.0, 45.0));
    deviceRuntime.updateHealthPercentage(90.0);
    deviceRuntime.updateHealthStatus(HealthStatus.WARNING);
    deviceRuntime.updateLastUpdated(updatedAt);
    deviceRuntime.addRuntimeFlag(DeviceRuntimeFlag.SIGNAL_AVAILABLE);
    deviceRuntime.removeRuntimeFlag(DeviceRuntimeFlag.CONNECTED);
    deviceRuntime.addOperationalFailure(OperationalFailureType.POWER_UNAVAILABLE);

    assertEquals(42.0, deviceRuntime.getMetrics().getCpuUsagePercentage());
    assertEquals(90.0, deviceRuntime.getHealthPercentage());
    assertEquals(HealthStatus.WARNING, deviceRuntime.getHealthStatus());
    assertEquals(updatedAt, deviceRuntime.getLastUpdated());
    assertTrue(deviceRuntime.getRuntimeFlags().contains(DeviceRuntimeFlag.SIGNAL_AVAILABLE));
    assertFalse(deviceRuntime.getRuntimeFlags().contains(DeviceRuntimeFlag.CONNECTED));
    assertTrue(
        deviceRuntime.getOperationalFailures().contains(OperationalFailureType.POWER_UNAVAILABLE));
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            deviceRuntime
                .getOperationalFailures()
                .add(OperationalFailureType.MEMORY_ALLOCATION_FAILURE));
  }

  @Test
  void capturesAnImmutableDeviceSnapshot() {
    DeviceMetrics deviceMetrics = metrics(42.0, 45.0);
    DeviceSnapshot deviceSnapshot =
        DeviceSnapshot.builder()
            .deviceId(DeviceId.generate(DeviceType.CAMERA))
            .timestamp(Instant.parse("2026-08-05T00:00:01Z"))
            .deviceState(DeviceState.ONLINE)
            .metrics(deviceMetrics)
            .build();

    assertEquals(DeviceState.ONLINE, deviceSnapshot.getDeviceState());
    assertEquals(deviceMetrics, deviceSnapshot.getMetrics());
  }

  @Test
  void permitsOnlyDocumentedLifecycleTransitions() {
    DeviceLifecycleManager lifecycleManager = new DeviceLifecycleManager();
    DeviceRuntime deviceRuntime = runtime(DeviceState.CREATED);

    lifecycleManager.transition(deviceRuntime, DeviceState.ONLINE);
    lifecycleManager.transition(deviceRuntime, DeviceState.WARNING);
    lifecycleManager.transition(deviceRuntime, DeviceState.FAILED);
    lifecycleManager.transition(deviceRuntime, DeviceState.RECOVERING);
    lifecycleManager.transition(deviceRuntime, DeviceState.ONLINE);

    assertEquals(DeviceState.ONLINE, deviceRuntime.getDeviceState());
    assertThrows(
        IllegalStateException.class,
        () -> lifecycleManager.transition(deviceRuntime, DeviceState.RECOVERING));
  }

  private DeviceRuntime runtime(DeviceState deviceState) {
    return new DeviceRuntime(
        deviceState,
        metrics(30.0, 40.0),
        100.0,
        Instant.parse("2026-08-05T00:00:00Z"),
        Set.of(DeviceRuntimeFlag.POWERED, DeviceRuntimeFlag.CONNECTED));
  }

  private DeviceMetrics metrics(double cpuUsagePercentage, double temperatureCelsius) {
    return DeviceMetrics.builder()
        .cpuUsagePercentage(cpuUsagePercentage)
        .memoryUsageMb(40.0)
        .temperatureCelsius(temperatureCelsius)
        .powerConsumptionWatts(12.0)
        .bandwidthMegabitsPerSecond(8.0)
        .build();
  }
}
