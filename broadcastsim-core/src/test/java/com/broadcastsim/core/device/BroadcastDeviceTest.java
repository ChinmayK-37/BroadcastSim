package com.broadcastsim.core.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.router.VideoRouter;
import com.broadcastsim.core.device.runtime.DeviceMetrics;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.constants.ResolutionConstants;
import com.broadcastsim.core.profile.DeviceProfile;
import com.broadcastsim.core.property.PropertyDefinition;
import com.broadcastsim.core.signal.ChromaSampling;
import com.broadcastsim.core.signal.Codec;
import com.broadcastsim.core.signal.Resolution;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies initial camera, video router, and encoder behavior. */
class BroadcastDeviceTest {

  @Test
  void cameraGeneratesRawSignalFromConfiguredProperties() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA),
            cameraProfile(),
            runtime(),
            outputPort("CAM_OUT"));
    camera.initialize();

    Signal signal = camera.generateSignal().orElseThrow();

    assertEquals(Resolution.FULL_HD_1080P, signal.getResolution());
    assertEquals(60.0, signal.getFramesPerSecond());
    assertEquals(Codec.RAW, signal.getCodec());
    assertEquals(95.0, signal.getQualityPercentage());
    assertEquals(Resolution.FULL_HD_1080P, camera.getResolution());
    assertEquals(60.0, camera.getFramesPerSecond());
    assertEquals(10, camera.getBitDepth());
    assertEquals(ChromaSampling.CHROMA_422, camera.getChromaSampling());
    assertEquals(camera.getDeviceId(), signal.getSourceDeviceId());
    assertThrows(UnsupportedOperationException.class, () -> camera.receiveSignal(signal));
  }

  @Test
  void cameraPreservesFractionalFrameRateInGeneratedSignal() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA),
            cameraProfile(ResolutionConstants.FRAME_RATE_23_976),
            runtime(),
            outputPort("CAM_FRACTIONAL_OUT"));
    camera.initialize();

    Signal signal = camera.generateSignal().orElseThrow();

    assertEquals(ResolutionConstants.FRAME_RATE_23_976, camera.getFramesPerSecond());
    assertEquals(ResolutionConstants.FRAME_RATE_23_976, signal.getFramesPerSecond());
  }

  @Test
  void videoRouterForwardsOnlySignalsFromTheSelectedInput() {
    VideoRouter videoRouter =
        new VideoRouter(DeviceId.generate(DeviceType.ROUTER), routerProfile(), runtime());
    Port inputOne = inputPort("RTR_IN1");
    Port inputTwo = inputPort("RTR_IN2");
    Port output = outputPort("RTR_OUT1");
    videoRouter.addInput(inputOne);
    videoRouter.addInput(inputTwo);
    videoRouter.addOutput(output);
    videoRouter.initialize();

    Signal signal = cameraSignal();
    videoRouter.receiveSignal(inputOne, signal);
    videoRouter.route(inputOne, output);

    assertEquals(signal, videoRouter.forwardSignal(output).orElseThrow());
    assertEquals(1, videoRouter.getActiveRouteCount());
    assertEquals(
        signal.getBitrateMegabitsPerSecond(), videoRouter.getCurrentThroughputMegabitsPerSecond());
    assertEquals(1485.0, videoRouter.getLinkCapacityMegabitsPerSecond());
    assertTrue(videoRouter.generateSignal().isEmpty());
    assertThrows(IllegalArgumentException.class, () -> videoRouter.route(inputTwo, inputOne));
    assertThrows(UnsupportedOperationException.class, () -> videoRouter.receiveSignal(signal));
  }

  @Test
  void encoderUpdatesCodecAndBitrateMetadataOnly() {
    Encoder encoder =
        new Encoder(
            DeviceId.generate(DeviceType.ENCODER),
            encoderProfile(),
            runtime(),
            inputPort("ENC_IN1"),
            outputPort("ENC_OUT1"));
    encoder.initialize();
    Signal signal = cameraSignal();

    assertTrue(encoder.generateSignal().isEmpty());

    encoder.receiveSignal(signal);
    assertEquals(Codec.H264, encoder.getCodec());
    assertEquals(8.0, encoder.getTargetBitrateMegabitsPerSecond());
    assertEquals(signal, encoder.getCurrentInputSignal().orElseThrow());
    Signal encodedSignal = encoder.generateSignal().orElseThrow();

    assertEquals(signal, encodedSignal);
    assertEquals(Codec.H264, encodedSignal.getCodec());
    assertEquals(8.0, encodedSignal.getBitrateMegabitsPerSecond());
    assertEquals(Resolution.FULL_HD_1080P, encodedSignal.getResolution());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Encoder(
                DeviceId.generate(DeviceType.ENCODER),
                cameraProfile(),
                runtime(),
                inputPort("INVALID_IN"),
                outputPort("INVALID_OUT")));
  }

  private DeviceProfile cameraProfile() {
    return cameraProfile(60.0);
  }

  private DeviceProfile cameraProfile(double framesPerSecond) {
    return DeviceProfile.builder()
        .name("Camera Profile")
        .supportedDeviceType(DeviceType.CAMERA)
        .defaultProperties(
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
                        framesPerSecond,
                        ValueType.DOUBLE,
                        PropertyUnit.FRAMES_PER_SECOND),
                PropertyKey.BIT_DEPTH,
                    property(
                        Integer.class,
                        PropertyKey.BIT_DEPTH,
                        10,
                        ValueType.INTEGER,
                        PropertyUnit.BITS),
                PropertyKey.CHROMA_SAMPLING,
                    property(
                        ChromaSampling.class,
                        PropertyKey.CHROMA_SAMPLING,
                        ChromaSampling.CHROMA_422,
                        ValueType.ENUM,
                        PropertyUnit.RATIO),
                PropertyKey.SIGNAL_QUALITY,
                    property(
                        Double.class,
                        PropertyKey.SIGNAL_QUALITY,
                        95.0,
                        ValueType.DOUBLE,
                        PropertyUnit.PERCENTAGE)))
        .build();
  }

  private DeviceProfile routerProfile() {
    return DeviceProfile.builder()
        .name("Router Profile")
        .supportedDeviceType(DeviceType.ROUTER)
        .defaultProperties(
            Map.of(
                PropertyKey.LINK_CAPACITY,
                property(
                    Double.class,
                    PropertyKey.LINK_CAPACITY,
                    1485.0,
                    ValueType.DOUBLE,
                    PropertyUnit.MEGABITS_PER_SECOND)))
        .build();
  }

  private DeviceProfile encoderProfile() {
    return DeviceProfile.builder()
        .name("Encoder Profile")
        .supportedDeviceType(DeviceType.ENCODER)
        .defaultProperties(
            Map.of(
                PropertyKey.CODEC,
                    property(
                        Codec.class,
                        PropertyKey.CODEC,
                        Codec.H264,
                        ValueType.ENUM,
                        PropertyUnit.NONE),
                PropertyKey.BITRATE,
                    property(
                        Double.class,
                        PropertyKey.BITRATE,
                        8.0,
                        ValueType.DOUBLE,
                        PropertyUnit.MEGABITS_PER_SECOND)))
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

  private Signal cameraSignal() {
    Camera camera =
        new Camera(
            DeviceId.generate(DeviceType.CAMERA),
            cameraProfile(),
            runtime(),
            outputPort("SOURCE_OUT"));
    camera.initialize();
    return camera.generateSignal().orElseThrow();
  }

  private Port inputPort(String name) {
    return new Port(name, PortType.VIDEO, PortDirection.INPUT);
  }

  private Port outputPort(String name) {
    return new Port(name, PortType.VIDEO, PortDirection.OUTPUT);
  }
}
