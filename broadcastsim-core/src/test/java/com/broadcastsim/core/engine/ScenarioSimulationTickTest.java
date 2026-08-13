package com.broadcastsim.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PortType;
import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.ValueType;
import com.broadcastsim.core.connection.ConnectionRegistry;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.event.EventQueue;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.scenario.Scenario;
import com.broadcastsim.core.scenario.ScenarioEvent;
import com.broadcastsim.core.scenario.ScenarioEventStatus;
import com.broadcastsim.core.signal.ChromaSampling;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.signal.SignalGraph;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies that due scenario changes are applied before engineering-rule execution. */
class ScenarioSimulationTickTest {

  private static final Duration TICK_INTERVAL = Duration.ofSeconds(10);

  @Test
  void appliesScheduledFpsBeforeRuleEngineCalculatesRuntimeMetrics() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    ScenarioEvent scenarioEvent =
        ScenarioEvent.setProperty(
            Instant.ofEpochSecond(10), camera.getDeviceId(), PropertyKey.FPS, 60.0);
    Scenario scenario = new Scenario();
    scenario.schedule(scenarioEvent);
    BroadcastEngine engine =
        new BroadcastEngine(
            new SimulationContext(
                registry,
                new ConnectionRegistry(),
                new SimulationClock(TICK_INTERVAL),
                new EventQueue()),
            new SignalGraph(),
            scenario);
    engine.start();

    SimulationTickResult result = engine.tick();

    assertEquals(60.0, camera.getFramesPerSecond());
    assertEquals(1244.16, camera.getDeviceRuntime().getMetrics().getBandwidthMegabitsPerSecond());
    assertEquals(ScenarioEventStatus.EXECUTED, scenarioEvent.getExecutionStatus());
    assertEquals(1, result.scenarioEvents().size());
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
