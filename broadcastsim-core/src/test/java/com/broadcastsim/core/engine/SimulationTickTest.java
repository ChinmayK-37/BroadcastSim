package com.broadcastsim.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.ConnectionStatus;
import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PortType;
import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.SimulationState;
import com.broadcastsim.core.common.enums.ValueType;
import com.broadcastsim.core.connection.Connection;
import com.broadcastsim.core.connection.ConnectionRegistry;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.rules.ExecutionStatus;
import com.broadcastsim.core.event.EventQueue;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.signal.ChromaSampling;
import com.broadcastsim.core.signal.Codec;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.signal.SignalGraph;
import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies the manual deterministic simulation lifecycle. */
class SimulationTickTest {

  private static final Duration TICK_INTERVAL = Duration.ofMillis(100);

  @Test
  void startsTicksPausesResumesAndStops() {
    BroadcastEngine engine = engine(new DeviceRegistry(), new SignalGraph());

    assertEquals(SimulationState.STOPPED, engine.getSimulationState());
    engine.start();
    assertEquals(1, engine.tick().tick());
    engine.pause();
    assertThrows(IllegalStateException.class, engine::tick);
    assertEquals(1, engine.getContext().getSimulationClock().getCurrentTick());
    engine.resume();
    assertEquals(2, engine.tick().tick());
    engine.stop();

    assertEquals(SimulationState.STOPPED, engine.getSimulationState());
    assertThrows(IllegalStateException.class, engine::tick);
  }

  @Test
  void tickExecutesRulesUpdatesRuntimeAndCapturesSnapshot() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    BroadcastEngine engine = engine(registry, new SignalGraph());
    engine.start();

    SimulationTickResult result = engine.tick();

    assertEquals(1, result.tick());
    assertEquals(Instant.EPOCH.plus(TICK_INTERVAL), result.timestamp());
    assertEquals(
        ExecutionStatus.SUCCESS,
        result.ruleExecutionReport().results().getFirst().getExecutionStatus());
    assertEquals(1244.16, camera.getDeviceRuntime().getMetrics().getBandwidthMegabitsPerSecond());
    assertEquals(1, result.snapshots().size());
    assertEquals(camera.getDeviceId(), result.snapshots().getFirst().getDeviceId());
  }

  @Test
  void tickPropagatesSignalAndAllowsEncoderToExecuteOnNextTick() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    Encoder encoder = encoder();
    registry.register(camera);
    registry.register(encoder);
    SignalGraph signalGraph = new SignalGraph();
    signalGraph.addConnection(connection(camera, encoder));
    BroadcastEngine engine = engine(registry, signalGraph);
    engine.start();

    SimulationTickResult firstTick = engine.tick();
    SimulationTickResult secondTick = engine.tick();

    assertEquals(1, firstTick.signalPropagationResult().getPropagatedSignals().size());
    assertTrue(encoder.getCurrentInputSignal().isPresent());
    assertEquals(
        ExecutionStatus.FAILURE,
        firstTick.ruleExecutionReport().results().get(1).getExecutionStatus());
    assertEquals(
        ExecutionStatus.SUCCESS,
        secondTick.ruleExecutionReport().results().get(1).getExecutionStatus());
  }

  @Test
  void producesDeterministicResultsForEquivalentInitialState() {
    DeviceRegistry firstRegistry = new DeviceRegistry();
    Camera firstCamera = camera();
    firstRegistry.register(firstCamera);
    DeviceRegistry secondRegistry = new DeviceRegistry();
    Camera secondCamera = camera();
    secondRegistry.register(secondCamera);
    BroadcastEngine firstEngine = engine(firstRegistry, new SignalGraph());
    BroadcastEngine secondEngine = engine(secondRegistry, new SignalGraph());
    firstEngine.start();
    secondEngine.start();

    SimulationTickResult firstResult = firstEngine.tick();
    SimulationTickResult secondResult = secondEngine.tick();

    assertEquals(firstResult.timestamp(), secondResult.timestamp());
    assertEquals(
        firstCamera.getDeviceRuntime().getMetrics(), secondCamera.getDeviceRuntime().getMetrics());
    assertEquals(
        firstResult.ruleExecutionReport().results().size(),
        secondResult.ruleExecutionReport().results().size());
  }

  @Test
  void preservesSuccessfulDeviceExecutionWhenAnotherRuleFails() {
    DeviceRegistry registry = new DeviceRegistry();
    Camera camera = camera();
    registry.register(camera);
    registry.register(encoder());
    BroadcastEngine engine = engine(registry, new SignalGraph());
    engine.start();

    SimulationTickResult result = engine.tick();

    assertEquals(
        ExecutionStatus.SUCCESS,
        result.ruleExecutionReport().results().getFirst().getExecutionStatus());
    assertEquals(
        ExecutionStatus.FAILURE,
        result.ruleExecutionReport().results().get(1).getExecutionStatus());
    assertFalse(result.ruleExecutionReport().isSuccessful());
    assertEquals(1244.16, camera.getDeviceRuntime().getMetrics().getBandwidthMegabitsPerSecond());
  }

  private BroadcastEngine engine(DeviceRegistry deviceRegistry, SignalGraph signalGraph) {
    return new BroadcastEngine(
        new SimulationContext(
            deviceRegistry,
            new ConnectionRegistry(),
            new SimulationClock(TICK_INTERVAL),
            new EventQueue()),
        signalGraph);
  }

  private Camera camera() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA),
            cameraProfile(),
            runtime(),
            outputPort("CAM_OUT"));
    camera.initialize();
    return camera;
  }

  private Encoder encoder() {
    Encoder encoder =
        new Encoder(
            DeviceId.generate(DeviceType.ENCODER),
            encoderProfile(),
            runtime(),
            inputPort("ENC_IN"),
            outputPort("ENC_OUT"));
    encoder.initialize();
    return encoder;
  }

  private Connection connection(Camera camera, Encoder encoder) {
    return Connection.builder()
        .connectionId(ConnectionId.generate())
        .sourceDeviceId(camera.getDeviceId())
        .sourcePort(camera.getOutputPorts().getFirst())
        .targetDeviceId(encoder.getDeviceId())
        .targetPort(encoder.getInputPorts().getFirst())
        .status(ConnectionStatus.CONNECTED)
        .build();
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

  private Port inputPort(String name) {
    return new Port(name, PortType.VIDEO, PortDirection.INPUT);
  }

  private Port outputPort(String name) {
    return new Port(name, PortType.VIDEO, PortDirection.OUTPUT);
  }
}
