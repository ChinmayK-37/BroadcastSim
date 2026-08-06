package com.broadcastsim.core.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.broadcastsim.core.common.enums.ValueType;
import com.broadcastsim.core.connection.Connection;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.router.VideoRouter;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies logical propagation through camera, router, and encoder topologies. */
class SignalPropagationTest {

  @Test
  void propagatesFromCameraToRouter() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    SignalGraph signalGraph = new SignalGraph();
    Camera camera = camera();
    VideoRouter router = router();
    Port routerInput = input("RTR_IN1");
    router.addInput(routerInput);
    router.initialize();
    register(deviceRegistry, camera, router);
    signalGraph.addConnection(
        connection(camera, output("CAM_OUT"), router, routerInput, ConnectionStatus.CONNECTED));

    SignalPropagationResult result =
        new SignalPropagationEngine(deviceRegistry, signalGraph).propagate();

    assertEquals(Set.of(camera.getDeviceId(), router.getDeviceId()), result.getVisitedDevices());
    assertEquals(1, result.getPropagatedSignals().size());
    assertTrue(result.getFailedPropagations().isEmpty());
  }

  @Test
  void propagatesFromCameraThroughRouterToEncoder() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    SignalGraph signalGraph = new SignalGraph();
    Camera camera = camera();
    VideoRouter router = router();
    Encoder encoder = encoder();
    Port routerInput = input("RTR_IN1");
    Port routerOutput = output("RTR_OUT1");
    router.addInput(routerInput);
    router.addOutput(routerOutput);
    router.route(routerInput, routerOutput);
    router.initialize();
    register(deviceRegistry, camera, router, encoder);
    signalGraph.addConnection(
        connection(camera, output("CAM_OUT"), router, routerInput, ConnectionStatus.CONNECTED));
    signalGraph.addConnection(
        connection(router, routerOutput, encoder, input("ENC_IN1"), ConnectionStatus.CONNECTED));

    SignalPropagationResult result =
        new SignalPropagationEngine(deviceRegistry, signalGraph).propagate();

    assertEquals(3, result.getVisitedDevices().size());
    assertEquals(2, result.getPropagatedSignals().size());
    assertEquals(Codec.H264, encoder.generateSignal().orElseThrow().getCodec());
  }

  @Test
  void recordsDisconnectedLinksWithoutDelivery() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    SignalGraph signalGraph = new SignalGraph();
    Camera camera = camera();
    VideoRouter router = router();
    Port routerInput = input("RTR_IN1");
    router.addInput(routerInput);
    router.initialize();
    register(deviceRegistry, camera, router);
    Connection disconnected =
        connection(camera, output("CAM_OUT"), router, routerInput, ConnectionStatus.DISCONNECTED);
    signalGraph.addConnection(disconnected);

    SignalPropagationResult result =
        new SignalPropagationEngine(deviceRegistry, signalGraph).propagate();

    assertEquals(Set.of(disconnected.getConnectionId()), Set.copyOf(result.getDisconnectedLinks()));
    assertTrue(result.getPropagatedSignals().isEmpty());
  }

  @Test
  void recordsFailedPropagationWhenDownstreamDeviceIsMissing() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    SignalGraph signalGraph = new SignalGraph();
    Camera camera = camera();
    deviceRegistry.register(camera);
    Connection missingTarget =
        connection(
            camera.getDeviceId(),
            output("CAM_OUT"),
            DeviceId.generate(DeviceType.VIEWER),
            input("VIEWER_IN"),
            ConnectionStatus.CONNECTED);
    signalGraph.addConnection(missingTarget);

    SignalPropagationResult result =
        new SignalPropagationEngine(deviceRegistry, signalGraph).propagate();

    assertEquals(
        Set.of(missingTarget.getConnectionId()), Set.copyOf(result.getFailedPropagations()));
    assertTrue(result.getPropagatedSignals().isEmpty());
  }

  @Test
  void deliversCameraSignalsToMultipleDownstreamDevices() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    SignalGraph signalGraph = new SignalGraph();
    Camera camera = camera();
    Encoder firstEncoder = encoder();
    Encoder secondEncoder = encoder();
    register(deviceRegistry, camera, firstEncoder, secondEncoder);
    signalGraph.addConnection(
        connection(
            camera, output("CAM_OUT"), firstEncoder, input("ENC1_IN"), ConnectionStatus.CONNECTED));
    signalGraph.addConnection(
        connection(
            camera,
            output("CAM_OUT"),
            secondEncoder,
            input("ENC2_IN"),
            ConnectionStatus.CONNECTED));

    SignalPropagationResult result =
        new SignalPropagationEngine(deviceRegistry, signalGraph).propagate();

    assertEquals(2, result.getPropagatedSignals().size());
    assertEquals(3, result.getVisitedDevices().size());
  }

  @Test
  void reportsCyclesWithoutTraversingTheTopology() {
    DeviceRegistry deviceRegistry = new DeviceRegistry();
    SignalGraph signalGraph = new SignalGraph();
    Camera camera = camera();
    VideoRouter router = router();
    Port routerInput = input("RTR_IN1");
    Port routerOutput = output("RTR_OUT1");
    router.addInput(routerInput);
    router.addOutput(routerOutput);
    router.route(routerInput, routerOutput);
    router.initialize();
    register(deviceRegistry, camera, router);
    signalGraph.addConnection(
        connection(camera, output("CAM_OUT"), router, routerInput, ConnectionStatus.CONNECTED));
    signalGraph.addConnection(
        connection(router, routerOutput, camera, input("CAM_IN"), ConnectionStatus.CONNECTED));

    SignalPropagationResult result =
        new SignalPropagationEngine(deviceRegistry, signalGraph).propagate();

    assertTrue(result.isCycleDetected());
    assertFalse(result.getVisitedDevices().iterator().hasNext());
  }

  private Camera camera() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA), cameraProfile(), runtime(), output("CAM_OUT"));
    camera.initialize();
    return camera;
  }

  private VideoRouter router() {
    return new VideoRouter(DeviceId.generate(DeviceType.ROUTER), routerProfile(), runtime());
  }

  private Encoder encoder() {
    Encoder encoder =
        new Encoder(
            DeviceId.generate(DeviceType.ENCODER),
            encoderProfile(),
            runtime(),
            input("ENC_IN1"),
            output("ENC_OUT1"));
    encoder.initialize();
    return encoder;
  }

  private void register(DeviceRegistry deviceRegistry, Device... devices) {
    for (Device device : devices) {
      deviceRegistry.register(device);
    }
  }

  private Connection connection(
      Device source, Port sourcePort, Device target, Port targetPort, ConnectionStatus status) {
    return connection(source.getDeviceId(), sourcePort, target.getDeviceId(), targetPort, status);
  }

  private Connection connection(
      DeviceId sourceDeviceId,
      Port sourcePort,
      DeviceId targetDeviceId,
      Port targetPort,
      ConnectionStatus status) {
    return Connection.builder()
        .connectionId(ConnectionId.generate())
        .sourceDeviceId(sourceDeviceId)
        .sourcePort(sourcePort)
        .targetDeviceId(targetDeviceId)
        .targetPort(targetPort)
        .status(status)
        .build();
  }

  private DeviceProfile cameraProfile() {
    return profile(
        DeviceType.CAMERA,
        Map.of(
            PropertyKey.RESOLUTION,
                property(
                    Resolution.class,
                    PropertyKey.RESOLUTION,
                    Resolution.FULL_HD_1080P,
                    ValueType.ENUM,
                    PropertyUnit.PIXELS),
            PropertyKey.FPS,
                property(
                    Double.class,
                    PropertyKey.FPS,
                    60.0,
                    ValueType.DOUBLE,
                    PropertyUnit.FRAMES_PER_SECOND),
            PropertyKey.SIGNAL_QUALITY,
                property(
                    Double.class,
                    PropertyKey.SIGNAL_QUALITY,
                    95.0,
                    ValueType.DOUBLE,
                    PropertyUnit.PERCENTAGE)));
  }

  private DeviceProfile routerProfile() {
    return profile(DeviceType.ROUTER, Map.of());
  }

  private DeviceProfile encoderProfile() {
    return profile(
        DeviceType.ENCODER,
        Map.of(
            PropertyKey.CODEC,
                property(
                    Codec.class, PropertyKey.CODEC, Codec.H264, ValueType.ENUM, PropertyUnit.NONE),
            PropertyKey.BITRATE,
                property(
                    Double.class,
                    PropertyKey.BITRATE,
                    8.0,
                    ValueType.DOUBLE,
                    PropertyUnit.MEGABITS_PER_SECOND)));
  }

  private DeviceProfile profile(
      DeviceType deviceType, Map<PropertyKey, PropertyDefinition<?>> properties) {
    return DeviceProfile.builder()
        .name(deviceType.name())
        .supportedDeviceType(deviceType)
        .defaultProperties(properties)
        .build();
  }

  private <T> PropertyDefinition<T> property(
      Class<T> valueClass, PropertyKey key, T value, ValueType valueType, PropertyUnit unit) {
    return PropertyDefinition.<T>builder()
        .key(key)
        .displayName(key.name())
        .valueClass(valueClass)
        .valueType(valueType)
        .category(PropertyCategory.CONFIGURATION)
        .unit(unit)
        .access(PropertyAccess.READ_WRITE)
        .defaultValue(value)
        .description(key.name())
        .build();
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

  private Port input(String name) {
    return new Port(name, PortType.VIDEO, PortDirection.INPUT);
  }

  private Port output(String name) {
    return new Port(name, PortType.VIDEO, PortDirection.OUTPUT);
  }
}
