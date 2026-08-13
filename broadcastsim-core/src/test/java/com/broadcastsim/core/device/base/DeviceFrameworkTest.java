package com.broadcastsim.core.device.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.ValueType;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies common device framework behavior. */
class DeviceFrameworkTest {

  @Test
  void generatesSeparateIdentifierSequencesForEachDeviceType() {
    DeviceId decoderId = DeviceId.generate(DeviceType.DECODER);
    DeviceId encoderId = DeviceId.generate(DeviceType.ENCODER);
    DeviceId nextDecoderId = DeviceId.generate(DeviceType.DECODER);

    assertTrue(decoderId.toString().startsWith("DEC-"));
    assertTrue(encoderId.toString().startsWith("ENC-"));
    assertEquals(identifierSequence(decoderId) + 1, identifierSequence(nextDecoderId));
  }

  @Test
  void initializesProfilePropertiesAndTransitionsToOnline() {
    DeviceProfile profile =
        DeviceProfile.builder()
            .name("Generic Camera")
            .supportedDeviceType(DeviceType.CAMERA)
            .defaultProperties(Map.of(PropertyKey.FPS, fpsDefinition()))
            .build();
    TestDevice device = new TestDevice(DeviceId.generate(DeviceType.CAMERA), profile, runtime());

    device.initialize();

    assertEquals(DeviceState.ONLINE, device.getDeviceState());
    assertTrue(device.getPropertyContainer().containsProperty(PropertyKey.FPS));
  }

  private PropertyDefinition<Double> fpsDefinition() {
    return PropertyDefinition.<Double>builder()
        .key(PropertyKey.FPS)
        .displayName("FPS")
        .valueClass(Double.class)
        .valueType(ValueType.DOUBLE)
        .category(PropertyCategory.CONFIGURATION)
        .unit(PropertyUnit.FRAMES_PER_SECOND)
        .access(PropertyAccess.READ_WRITE)
        .defaultValue(30.0)
        .minimum(1.0)
        .maximum(240.0)
        .warningThreshold(120.0)
        .criticalThreshold(180.0)
        .description("Frames per second")
        .build();
  }

  private int identifierSequence(DeviceId deviceId) {
    return Integer.parseInt(
        deviceId.toString().substring(deviceId.toString().lastIndexOf('-') + 1));
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
    public void receiveSignal(Signal signal) {}

    @Override
    public Optional<Signal> generateSignal() {
      return Optional.empty();
    }

    @Override
    public void applyEvent() {}
  }

  private DeviceRuntime runtime() {
    return new DeviceRuntime(
        DeviceState.CREATED,
        DeviceMetrics.builder()
            .cpuUsagePercentage(0.0)
            .memoryUsageMb(0.0)
            .temperatureCelsius(0.0)
            .powerConsumptionWatts(0.0)
            .bandwidthMegabitsPerSecond(0.0)
            .build(),
        100.0,
        Instant.EPOCH,
        Set.of());
  }
}
