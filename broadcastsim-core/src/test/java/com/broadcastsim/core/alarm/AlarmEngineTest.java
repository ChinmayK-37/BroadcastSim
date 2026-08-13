package com.broadcastsim.core.alarm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.AlarmSeverity;
import com.broadcastsim.core.common.enums.AlarmState;
import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.OperationalFailureType;
import com.broadcastsim.core.device.base.AbstractDevice;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies deterministic health-to-alarm translation and alarm lifecycle behavior. */
class AlarmEngineTest {

  private static final Instant FIRST_TIMESTAMP = Instant.parse("2026-08-13T00:00:00Z");
  private static final Instant SECOND_TIMESTAMP = Instant.parse("2026-08-13T00:00:01Z");

  @Test
  void normalHealthProducesNoAlarm() {
    TestDevice device = device();

    List<Alarm> alarms = new AlarmEngine().evaluate(List.of(device), FIRST_TIMESTAMP);

    assertTrue(alarms.isEmpty());
  }

  @Test
  void warningAndCriticalHealthProduceMatchingSeverityAlarms() {
    TestDevice warningDevice = device();
    warningDevice.getDeviceRuntime().updateHealthStatus(HealthStatus.WARNING);
    TestDevice criticalDevice = device();
    criticalDevice.getDeviceRuntime().updateHealthStatus(HealthStatus.CRITICAL);

    List<Alarm> alarms =
        new AlarmEngine().evaluate(List.of(warningDevice, criticalDevice), FIRST_TIMESTAMP);

    assertEquals(AlarmSeverity.WARNING, alarms.getFirst().getSeverity());
    assertEquals(AlarmSeverity.CRITICAL, alarms.get(1).getSeverity());
    assertEquals(AlarmState.RAISED, alarms.getFirst().getState());
  }

  @Test
  void failedOperationalFailureProducesCriticalAlarm() {
    TestDevice device = device();
    device.getDeviceRuntime().addOperationalFailure(OperationalFailureType.POWER_UNAVAILABLE);
    device.getDeviceRuntime().updateHealthStatus(HealthStatus.FAILED);

    Alarm alarm = new AlarmEngine().evaluate(List.of(device), FIRST_TIMESTAMP).getFirst();

    assertEquals(HealthStatus.FAILED, alarm.getHealthStatus());
    assertEquals(AlarmSeverity.CRITICAL, alarm.getSeverity());
    assertTrue(alarm.getOperationalFailures().contains(OperationalFailureType.POWER_UNAVAILABLE));
  }

  @Test
  void preservesActiveAlarmThenClearsItWhenHealthReturnsToNormal() {
    TestDevice device = device();
    AlarmEngine alarmEngine = new AlarmEngine();
    device.getDeviceRuntime().updateHealthStatus(HealthStatus.WARNING);

    Alarm raisedAlarm = alarmEngine.evaluate(List.of(device), FIRST_TIMESTAMP).getFirst();
    Alarm activeAlarm = alarmEngine.evaluate(List.of(device), SECOND_TIMESTAMP).getFirst();
    device.getDeviceRuntime().updateHealthStatus(HealthStatus.NORMAL);
    Alarm clearedAlarm = alarmEngine.evaluate(List.of(device), SECOND_TIMESTAMP).getFirst();

    assertEquals(raisedAlarm.getAlarmId(), activeAlarm.getAlarmId());
    assertEquals(FIRST_TIMESTAMP, activeAlarm.getRaisedAt());
    assertEquals(AlarmState.CLEARED, clearedAlarm.getState());
    assertEquals(SECOND_TIMESTAMP, clearedAlarm.getClearedAt());
  }

  @Test
  void maintainsIndependentAlarmStatesForMultipleDevices() {
    TestDevice warningDevice = device();
    warningDevice.getDeviceRuntime().updateHealthStatus(HealthStatus.WARNING);
    TestDevice failedDevice = device();
    failedDevice.getDeviceRuntime().updateHealthStatus(HealthStatus.FAILED);

    List<Alarm> alarms =
        new AlarmEngine().evaluate(List.of(warningDevice, failedDevice), FIRST_TIMESTAMP);

    assertEquals(2, alarms.size());
    assertEquals(warningDevice.getDeviceId(), alarms.getFirst().getDeviceId());
    assertEquals(failedDevice.getDeviceId(), alarms.get(1).getDeviceId());
  }

  private TestDevice device() {
    return new TestDevice(
        DeviceId.generate(DeviceType.CAMERA),
        DeviceProfile.builder()
            .name("Test Device")
            .supportedDeviceType(DeviceType.CAMERA)
            .defaultProperties(Map.of())
            .build(),
        new DeviceRuntime(
            DeviceState.CREATED,
            DeviceMetrics.builder()
                .cpuUsagePercentage(0.0)
                .memoryUsageMb(0.0)
                .temperatureCelsius(25.0)
                .powerConsumptionWatts(0.0)
                .bandwidthMegabitsPerSecond(0.0)
                .build(),
            100.0,
            Instant.EPOCH,
            Set.of()));
  }

  private static final class TestDevice extends AbstractDevice {

    private TestDevice(
        DeviceId deviceId, DeviceProfile deviceProfile, DeviceRuntime deviceRuntime) {
      super(deviceId, deviceProfile, deviceRuntime);
    }

    @Override
    public void update() {}

    @Override
    public void calculate() {}

    @Override
    public void receiveSignal(com.broadcastsim.core.signal.Signal signal) {}

    @Override
    public java.util.Optional<com.broadcastsim.core.signal.Signal> generateSignal() {
      return java.util.Optional.empty();
    }

    @Override
    public void applyEvent() {}
  }
}
