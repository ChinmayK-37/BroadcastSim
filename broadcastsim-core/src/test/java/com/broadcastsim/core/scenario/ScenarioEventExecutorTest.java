package com.broadcastsim.core.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PortType;
import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.ValueType;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.signal.ChromaSampling;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies deterministic application and validation of minimum viable scenario events. */
class ScenarioEventExecutorTest {

  private static final Instant EVENT_TIMESTAMP = Instant.ofEpochSecond(10);

  @Test
  void leavesAnEmptyScenarioUnchanged() {
    ScenarioEventExecutor executor = new ScenarioEventExecutor(new DeviceRegistry());

    assertTrue(executor.executeDueEvents(new Scenario(), Instant.EPOCH).isEmpty());
  }

  @Test
  void doesNotExecuteFutureEventsEarlyAndExecutesEachEventOnlyOnce() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    ScenarioEvent scenarioEvent =
        ScenarioEvent.setProperty(EVENT_TIMESTAMP, camera.getDeviceId(), PropertyKey.FPS, 60.0);
    Scenario scenario = scenario(scenarioEvent);
    ScenarioEventExecutor executor = new ScenarioEventExecutor(registry);

    assertTrue(executor.executeDueEvents(scenario, Instant.EPOCH).isEmpty());
    assertEquals(30.0, camera.getFramesPerSecond());
    assertEquals(List.of(scenarioEvent), executor.executeDueEvents(scenario, EVENT_TIMESTAMP));
    assertEquals(60.0, camera.getFramesPerSecond());
    assertEquals(ScenarioEventStatus.EXECUTED, scenarioEvent.getExecutionStatus());
    assertTrue(executor.executeDueEvents(scenario, EVENT_TIMESTAMP.plusSeconds(1)).isEmpty());
  }

  @Test
  void enablesAndDisablesDevicesThroughTheExistingLifecycleModel() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    Scenario scenario =
        scenario(
            ScenarioEvent.disableDevice(Instant.EPOCH, camera.getDeviceId()),
            ScenarioEvent.enableDevice(EVENT_TIMESTAMP, camera.getDeviceId()));
    ScenarioEventExecutor executor = new ScenarioEventExecutor(registry);

    executor.executeDueEvents(scenario, Instant.EPOCH);
    assertEquals(DeviceState.OFFLINE, camera.getDeviceState());
    executor.executeDueEvents(scenario, EVENT_TIMESTAMP);

    assertEquals(DeviceState.ONLINE, camera.getDeviceState());
  }

  @Test
  void rejectsInvalidTargetsPropertiesAndValues() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    ScenarioEvent invalidDevice =
        ScenarioEvent.setProperty(
            Instant.EPOCH, DeviceId.generate(DeviceType.VIEWER), PropertyKey.FPS, 60.0);
    ScenarioEvent invalidProperty =
        ScenarioEvent.setProperty(Instant.EPOCH, camera.getDeviceId(), PropertyKey.BITRATE, 8.0);
    ScenarioEvent invalidValue =
        ScenarioEvent.setProperty(Instant.EPOCH, camera.getDeviceId(), PropertyKey.FPS, "invalid");

    new ScenarioEventExecutor(registry)
        .executeDueEvents(scenario(invalidDevice, invalidProperty, invalidValue), Instant.EPOCH);

    assertEquals(ScenarioEventStatus.FAILED, invalidDevice.getExecutionStatus());
    assertEquals(ScenarioEventStatus.FAILED, invalidProperty.getExecutionStatus());
    assertEquals(ScenarioEventStatus.FAILED, invalidValue.getExecutionStatus());
  }

  @Test
  void executesEventsAtTheSameTimestampInScenarioInsertionOrder() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    ScenarioEvent first =
        ScenarioEvent.setProperty(EVENT_TIMESTAMP, camera.getDeviceId(), PropertyKey.FPS, 50.0);
    ScenarioEvent second =
        ScenarioEvent.setProperty(EVENT_TIMESTAMP, camera.getDeviceId(), PropertyKey.FPS, 60.0);

    List<ScenarioEvent> executed =
        new ScenarioEventExecutor(registry)
            .executeDueEvents(scenario(first, second), EVENT_TIMESTAMP);

    assertEquals(List.of(first, second), executed);
    assertEquals(60.0, camera.getFramesPerSecond());
  }

  private Scenario scenario(ScenarioEvent... scenarioEvents) {
    Scenario scenario = new Scenario();
    for (ScenarioEvent scenarioEvent : scenarioEvents) {
      scenario.schedule(scenarioEvent);
    }
    return scenario;
  }

  private Camera camera() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA),
            DeviceProfile.builder()
                .name("Camera")
                .supportedDeviceType(DeviceType.CAMERA)
                .defaultProperties(
                    Map.of(
                        PropertyKey.RESOLUTION,
                        definition(
                            Resolution.class, PropertyKey.RESOLUTION, Resolution.FULL_HD_1080P),
                        PropertyKey.FPS,
                        definition(Double.class, PropertyKey.FPS, 30.0),
                        PropertyKey.BIT_DEPTH,
                        definition(Integer.class, PropertyKey.BIT_DEPTH, 10),
                        PropertyKey.CHROMA_SAMPLING,
                        definition(
                            ChromaSampling.class,
                            PropertyKey.CHROMA_SAMPLING,
                            ChromaSampling.CHROMA_422),
                        PropertyKey.SIGNAL_QUALITY,
                        definition(Double.class, PropertyKey.SIGNAL_QUALITY, 95.0)))
                .build(),
            runtime(),
            new Port("CAM_OUT", PortType.VIDEO, PortDirection.OUTPUT));
    camera.initialize();
    return camera;
  }

  private <T> PropertyDefinition<T> definition(Class<T> type, PropertyKey key, T value) {
    return PropertyDefinition.<T>builder()
        .key(key)
        .displayName(key.name())
        .valueClass(type)
        .valueType(value instanceof Double ? ValueType.DOUBLE : ValueType.ENUM)
        .category(PropertyCategory.CONFIGURATION)
        .unit(PropertyUnit.NONE)
        .access(PropertyAccess.READ_WRITE)
        .defaultValue(value)
        .minimum(value instanceof Double ? type.cast(1.0) : null)
        .maximum(value instanceof Double ? type.cast(120.0) : null)
        .description(key.name())
        .build();
  }

  private DeviceRuntime runtime() {
    return new DeviceRuntime(
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
        Set.of());
  }
}
