package com.broadcastsim.core.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.router.VideoRouter;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.rules.ExecutionStatus;
import com.broadcastsim.core.engineering.rules.RuleModelName;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.signal.ChromaSampling;
import com.broadcastsim.core.signal.Codec;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import com.broadcastsim.core.valueobject.SignalId;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies deterministic RuleEngine orchestration of MVP device rule models. */
class RuleEngineTest {

  private static final Instant EXECUTION_TIMESTAMP = Instant.parse("2026-08-13T00:00:00Z");

  @Test
  void executesAnEmptyRegistryWithoutResults() {
    RuleExecutionReport report = new RuleEngine(new DeviceRegistry()).execute(EXECUTION_TIMESTAMP);

    assertTrue(report.results().isEmpty());
    assertTrue(report.unsupportedDeviceIds().isEmpty());
    assertTrue(report.isSuccessful());
  }

  @Test
  void executesSingleCameraAndPreservesConfiguration() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    double configuredFrameRate = camera.getFramesPerSecond();
    registry.register(camera);

    RuleExecutionReport report = new RuleEngine(registry).execute(EXECUTION_TIMESTAMP);

    assertEquals(1, report.results().size());
    assertEquals(RuleModelName.CAMERA, report.results().getFirst().getRuleModelName());
    assertEquals(ExecutionStatus.SUCCESS, report.results().getFirst().getExecutionStatus());
    assertEquals(configuredFrameRate, camera.getFramesPerSecond());
    assertEquals(EXECUTION_TIMESTAMP, camera.getDeviceRuntime().getLastUpdated());
  }

  @Test
  void executesCameraRouterAndEncoderInEngineeringOrder() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    VideoRouter router = router();
    Encoder encoder = encoder();
    encoder.receiveSignal(signal());
    registry.register(encoder);
    registry.register(router);
    registry.register(camera);

    RuleExecutionReport report = new RuleEngine(registry).execute(EXECUTION_TIMESTAMP);

    assertEquals(
        List.of(RuleModelName.CAMERA, RuleModelName.VIDEO_ROUTER, RuleModelName.ENCODER),
        report.results().stream().map(result -> result.getRuleModelName()).toList());
    assertTrue(report.isSuccessful());
  }

  @Test
  void resolvesMvpRuleModelsAndReportsUnsupportedDevices() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    UnsupportedDevice unsupportedDevice =
        new UnsupportedDevice(DeviceId.generate(DeviceType.DECODER));
    registry.register(camera);
    registry.register(unsupportedDevice);
    RuleEngine ruleEngine = new RuleEngine(registry);

    RuleExecutionReport report = ruleEngine.execute(EXECUTION_TIMESTAMP);

    assertEquals(Optional.of(RuleModelName.CAMERA), ruleEngine.resolveRuleModel(camera));
    assertTrue(ruleEngine.resolveRuleModel(unsupportedDevice).isEmpty());
    assertEquals(List.of(unsupportedDevice.getDeviceId()), report.unsupportedDeviceIds());
    assertEquals(1, report.results().size());
  }

  @Test
  void isolatesRuleExecutionFailuresBetweenDevices() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    Encoder encoderWithoutInput = encoder();
    registry.register(camera);
    registry.register(encoderWithoutInput);

    RuleExecutionReport report = new RuleEngine(registry).execute(EXECUTION_TIMESTAMP);

    assertEquals(ExecutionStatus.SUCCESS, report.results().getFirst().getExecutionStatus());
    assertEquals(ExecutionStatus.FAILURE, report.results().get(1).getExecutionStatus());
    assertFalse(report.isSuccessful());
    assertEquals(EXECUTION_TIMESTAMP, camera.getDeviceRuntime().getLastUpdated());
  }

  private Camera camera() {
    Camera camera =
        new Camera(DeviceId.generate(DeviceType.CAMERA), cameraProfile(), runtime(), outputPort());
    camera.initialize();
    return camera;
  }

  private Encoder encoder() {
    Encoder encoder =
        new Encoder(
            DeviceId.generate(DeviceType.ENCODER),
            encoderProfile(),
            runtime(),
            inputPort(),
            outputPort());
    encoder.initialize();
    return encoder;
  }

  private VideoRouter router() {
    VideoRouter router =
        new VideoRouter(DeviceId.generate(DeviceType.ROUTER), routerProfile(), runtime());
    router.initialize();
    return router;
  }

  private DeviceProfile cameraProfile() {
    return profile(
        DeviceType.CAMERA,
        Map.of(
            PropertyKey.RESOLUTION,
                definition(Resolution.class, PropertyKey.RESOLUTION, Resolution.FULL_HD_1080P),
            PropertyKey.FPS, definition(Double.class, PropertyKey.FPS, 60.0),
            PropertyKey.BIT_DEPTH, definition(Integer.class, PropertyKey.BIT_DEPTH, 10),
            PropertyKey.CHROMA_SAMPLING,
                definition(
                    ChromaSampling.class, PropertyKey.CHROMA_SAMPLING, ChromaSampling.CHROMA_422),
            PropertyKey.SIGNAL_QUALITY,
                definition(Double.class, PropertyKey.SIGNAL_QUALITY, 95.0)));
  }

  private DeviceProfile encoderProfile() {
    return profile(
        DeviceType.ENCODER,
        Map.of(
            PropertyKey.CODEC, definition(Codec.class, PropertyKey.CODEC, Codec.H264),
            PropertyKey.BITRATE, definition(Double.class, PropertyKey.BITRATE, 8.0)));
  }

  private DeviceProfile routerProfile() {
    return profile(
        DeviceType.ROUTER,
        Map.of(
            PropertyKey.LINK_CAPACITY, definition(Double.class, PropertyKey.LINK_CAPACITY, 100.0)));
  }

  private DeviceProfile profile(
      DeviceType deviceType, Map<PropertyKey, PropertyDefinition<?>> properties) {
    return DeviceProfile.builder()
        .name(deviceType.name())
        .supportedDeviceType(deviceType)
        .defaultProperties(properties)
        .build();
  }

  private <T> PropertyDefinition<T> definition(Class<T> type, PropertyKey key, T value) {
    return PropertyDefinition.<T>builder()
        .key(key)
        .displayName(key.name())
        .valueClass(type)
        .valueType(valueType(value))
        .category(PropertyCategory.CONFIGURATION)
        .unit(PropertyUnit.NONE)
        .access(PropertyAccess.READ_WRITE)
        .defaultValue(value)
        .description(key.name())
        .build();
  }

  private ValueType valueType(Object value) {
    return value instanceof Integer
        ? ValueType.INTEGER
        : value instanceof Double ? ValueType.DOUBLE : ValueType.ENUM;
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

  private Signal signal() {
    return Signal.builder()
        .signalId(SignalId.generate())
        .sourceDeviceId(DeviceId.generate(DeviceType.CAMERA))
        .resolution(Resolution.FULL_HD_1080P)
        .framesPerSecond(60.0)
        .codec(Codec.RAW)
        .bitrateMegabitsPerSecond(8.0)
        .qualityPercentage(95.0)
        .latency(Duration.ZERO)
        .build();
  }

  private Port inputPort() {
    return new Port("IN", PortType.VIDEO, PortDirection.INPUT);
  }

  private Port outputPort() {
    return new Port("OUT", PortType.VIDEO, PortDirection.OUTPUT);
  }

  private static final class UnsupportedDevice implements Device {

    private final DeviceId deviceId;

    private UnsupportedDevice(DeviceId deviceId) {
      this.deviceId = deviceId;
    }

    @Override
    public void initialize() {}

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

    @Override
    public DeviceId getDeviceId() {
      return deviceId;
    }

    @Override
    public DeviceType getDeviceType() {
      return DeviceType.DECODER;
    }

    @Override
    public DeviceState getDeviceState() {
      return DeviceState.CREATED;
    }
  }
}
