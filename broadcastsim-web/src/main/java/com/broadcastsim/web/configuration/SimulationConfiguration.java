package com.broadcastsim.web.configuration;

import com.broadcastsim.core.common.enums.ConnectionStatus;
import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PortType;
import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.ValueType;
import com.broadcastsim.core.connection.Connection;
import com.broadcastsim.core.connection.ConnectionRegistry;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.router.VideoRouter;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engine.BroadcastEngine;
import com.broadcastsim.core.engine.SimulationClock;
import com.broadcastsim.core.engine.SimulationContext;
import com.broadcastsim.core.event.EventQueue;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.scenario.Scenario;
import com.broadcastsim.core.scenario.ScenarioEvent;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Creates the small in-memory device topology used by the web MVP dashboard. */
@Configuration
public class SimulationConfiguration {

  private static final Duration TICK_INTERVAL = Duration.ofSeconds(1);
  private static final Instant FPS_CHANGE_TIME = Instant.ofEpochSecond(10);

  /**
   * Creates the reusable broadcast engine used by all web requests.
   *
   * @return configured in-memory broadcast engine
   */
  @Bean
  public BroadcastEngine broadcastEngine() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    Camera camera = camera();
    VideoRouter videoRouter = videoRouter();
    Encoder encoder = encoder();
    deviceRegistry.register(camera);
    deviceRegistry.register(videoRouter);
    deviceRegistry.register(encoder);
    SignalGraph signalGraph = signalGraph(camera, videoRouter, encoder);
    Scenario scenario = new Scenario();
    scenario.schedule(
        ScenarioEvent.setProperty(FPS_CHANGE_TIME, camera.getDeviceId(), PropertyKey.FPS, 60.0));
    return new BroadcastEngine(
        new SimulationContext(
            deviceRegistry,
            new ConnectionRegistry(),
            new SimulationClock(TICK_INTERVAL),
            new EventQueue()),
        signalGraph,
        scenario);
  }

  private Camera camera() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA),
            profile(
                DeviceType.CAMERA,
                Map.of(
                    PropertyKey.RESOLUTION,
                    definition(Resolution.class, PropertyKey.RESOLUTION, Resolution.FULL_HD_1080P),
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
                    definition(Double.class, PropertyKey.SIGNAL_QUALITY, 95.0))),
            runtime(),
            outputPort("CAM_OUT"));
    camera.initialize();
    return camera;
  }

  private VideoRouter videoRouter() {
    VideoRouter videoRouter =
        new VideoRouter(
            DeviceId.generate(DeviceType.ROUTER),
            profile(
                DeviceType.ROUTER,
                Map.of(
                    PropertyKey.LINK_CAPACITY,
                    definition(Double.class, PropertyKey.LINK_CAPACITY, 10000.0))),
            runtime());
    videoRouter.addInput(inputPort("RTR_IN"));
    videoRouter.addOutput(outputPort("RTR_OUT"));
    videoRouter.route(
        videoRouter.getInputPorts().getFirst(), videoRouter.getOutputPorts().getFirst());
    videoRouter.initialize();
    return videoRouter;
  }

  private Encoder encoder() {
    Encoder encoder =
        new Encoder(
            DeviceId.generate(DeviceType.ENCODER),
            profile(
                DeviceType.ENCODER,
                Map.of(
                    PropertyKey.CODEC, definition(Codec.class, PropertyKey.CODEC, Codec.H264),
                    PropertyKey.BITRATE, definition(Double.class, PropertyKey.BITRATE, 8.0))),
            runtime(),
            inputPort("ENC_IN"),
            outputPort("ENC_OUT"));
    encoder.initialize();
    return encoder;
  }

  private SignalGraph signalGraph(Camera camera, VideoRouter videoRouter, Encoder encoder) {
    SignalGraph signalGraph = new SignalGraph();
    signalGraph.addConnection(
        connection(
            camera.getDeviceId(),
            camera.getOutputPorts().getFirst(),
            videoRouter.getDeviceId(),
            videoRouter.getInputPorts().getFirst()));
    signalGraph.addConnection(
        connection(
            videoRouter.getDeviceId(),
            videoRouter.getOutputPorts().getFirst(),
            encoder.getDeviceId(),
            encoder.getInputPorts().getFirst()));
    return signalGraph;
  }

  private Connection connection(
      DeviceId sourceDeviceId, Port sourcePort, DeviceId targetDeviceId, Port targetPort) {
    return Connection.builder()
        .connectionId(ConnectionId.generate())
        .sourceDeviceId(sourceDeviceId)
        .sourcePort(sourcePort)
        .targetDeviceId(targetDeviceId)
        .targetPort(targetPort)
        .status(ConnectionStatus.CONNECTED)
        .build();
  }

  private DeviceProfile profile(
      DeviceType deviceType, Map<PropertyKey, PropertyDefinition<?>> defaultProperties) {
    return DeviceProfile.builder()
        .name(deviceType.name())
        .supportedDeviceType(deviceType)
        .defaultProperties(defaultProperties)
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
