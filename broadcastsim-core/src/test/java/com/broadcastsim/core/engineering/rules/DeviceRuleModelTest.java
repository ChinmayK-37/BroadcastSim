package com.broadcastsim.core.engineering.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
import com.broadcastsim.core.valueobject.SignalId;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies deterministic device rule-model execution. */
class DeviceRuleModelTest {

  private static final Instant EXECUTION_TIMESTAMP = Instant.parse("2026-08-06T00:00:01Z");

  @Test
  void cameraRuleUpdatesRuntimeMetricsAndReturnsSuccess() {
    Camera camera =
        new Camera(DeviceId.generate(DeviceType.CAMERA), cameraProfile(), runtime(), outputPort());
    camera.initialize();

    RuleExecutionResult result = new CameraRuleModel().execute(camera, EXECUTION_TIMESTAMP);

    assertEquals(ExecutionStatus.SUCCESS, result.getExecutionStatus());
    assertEquals(ValidationStatus.VALID, result.getValidationStatus());
    assertEquals(RuleModelName.CAMERA, result.getRuleModelName());
    assertEquals(EXECUTION_TIMESTAMP, camera.getDeviceRuntime().getLastUpdated());
    assertEquals(5.0, camera.getDeviceRuntime().getMetrics().getCpuUsagePercentage());
    assertEquals(128.0, camera.getDeviceRuntime().getMetrics().getMemoryUsageMb());
    assertEquals(1244.16, camera.getDeviceRuntime().getMetrics().getBandwidthMegabitsPerSecond());
  }

  @Test
  void encoderRuleCalculatesRuntimeFromInputSignalAndIsDeterministic() {
    Encoder firstEncoder = encoder();
    Encoder secondEncoder = encoder();
    Signal firstSignal = signal();
    Signal secondSignal = signal();
    firstEncoder.receiveSignal(firstSignal);
    secondEncoder.receiveSignal(secondSignal);

    RuleExecutionResult first = new EncoderRuleModel().execute(firstEncoder, EXECUTION_TIMESTAMP);
    RuleExecutionResult second = new EncoderRuleModel().execute(secondEncoder, EXECUTION_TIMESTAMP);

    assertEquals(ExecutionStatus.SUCCESS, first.getExecutionStatus());
    assertEquals(23.0, firstEncoder.getDeviceRuntime().getMetrics().getCpuUsagePercentage());
    assertEquals(704.0, firstEncoder.getDeviceRuntime().getMetrics().getMemoryUsageMb());
    assertEquals(8.0, firstEncoder.getDeviceRuntime().getMetrics().getBandwidthMegabitsPerSecond());
    assertEquals(
        firstEncoder.getDeviceRuntime().getMetrics(),
        secondEncoder.getDeviceRuntime().getMetrics());
  }

  @Test
  void encoderRuleAcceptsAllFrameRatesDefinedByEcs() {
    double[] supportedFrameRates = {
      ResolutionConstants.FRAME_RATE_23_976,
      ResolutionConstants.FRAME_RATE_24,
      ResolutionConstants.FRAME_RATE_25,
      ResolutionConstants.FRAME_RATE_29_97,
      ResolutionConstants.FRAME_RATE_30,
      ResolutionConstants.FRAME_RATE_50,
      ResolutionConstants.FRAME_RATE_59_94,
      ResolutionConstants.FRAME_RATE_60,
      ResolutionConstants.FRAME_RATE_120
    };

    for (double frameRate : supportedFrameRates) {
      Encoder encoder = encoder();
      encoder.receiveSignal(signal(frameRate));

      RuleExecutionResult result = new EncoderRuleModel().execute(encoder, EXECUTION_TIMESTAMP);

      assertEquals(ExecutionStatus.SUCCESS, result.getExecutionStatus());
    }
  }

  @Test
  void encoderRuleReportsInvalidInputWithoutChangingRuntime() {
    Encoder encoder = encoder();
    DeviceRuntime runtime = encoder.getDeviceRuntime();
    DeviceMetrics originalMetrics = runtime.getMetrics();

    RuleExecutionResult result = new EncoderRuleModel().execute(encoder, EXECUTION_TIMESTAMP);

    assertEquals(ExecutionStatus.FAILURE, result.getExecutionStatus());
    assertEquals(ValidationStatus.INVALID, result.getValidationStatus());
    assertSame(originalMetrics, runtime.getMetrics());
  }

  @Test
  void encoderRuleRejectsFrameRateNotDefinedByEcsWithoutChangingRuntime() {
    Encoder encoder = encoder();
    DeviceRuntime runtime = encoder.getDeviceRuntime();
    DeviceMetrics originalMetrics = runtime.getMetrics();
    encoder.receiveSignal(signal(26.0));

    RuleExecutionResult result = new EncoderRuleModel().execute(encoder, EXECUTION_TIMESTAMP);

    assertEquals(ExecutionStatus.FAILURE, result.getExecutionStatus());
    assertEquals(ValidationStatus.INVALID, result.getValidationStatus());
    assertSame(originalMetrics, runtime.getMetrics());
  }

  @Test
  void videoRouterRuleCalculatesRoutedThroughputAndRuntimeMetrics() {
    VideoRouter router =
        new VideoRouter(DeviceId.generate(DeviceType.ROUTER), routerProfile(), runtime());
    Port input = inputPort();
    Port output = outputPort();
    router.addInput(input);
    router.addOutput(output);
    router.initialize();
    router.receiveSignal(input, signal());
    router.route(input, output);

    RuleExecutionResult result = new VideoRouterRuleModel().execute(router, EXECUTION_TIMESTAMP);

    assertEquals(ExecutionStatus.SUCCESS, result.getExecutionStatus());
    assertEquals(RuleModelName.VIDEO_ROUTER, result.getRuleModelName());
    assertEquals(8.0, router.getDeviceRuntime().getMetrics().getBandwidthMegabitsPerSecond());
    assertEquals(4.8, router.getDeviceRuntime().getMetrics().getCpuUsagePercentage());
    assertEquals(256.0, router.getDeviceRuntime().getMetrics().getMemoryUsageMb());
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

  private DeviceProfile cameraProfile() {
    return profile(
        DeviceType.CAMERA,
        Map.of(
            PropertyKey.RESOLUTION,
                definition(
                    Resolution.class,
                    PropertyKey.RESOLUTION,
                    Resolution.FULL_HD_1080P,
                    PropertyUnit.PIXELS),
            PropertyKey.FPS,
                definition(Double.class, PropertyKey.FPS, 60.0, PropertyUnit.FRAMES_PER_SECOND),
            PropertyKey.BIT_DEPTH,
                definition(Integer.class, PropertyKey.BIT_DEPTH, 10, PropertyUnit.BITS),
            PropertyKey.CHROMA_SAMPLING,
                definition(
                    ChromaSampling.class,
                    PropertyKey.CHROMA_SAMPLING,
                    ChromaSampling.CHROMA_422,
                    PropertyUnit.RATIO),
            PropertyKey.SIGNAL_QUALITY,
                definition(
                    Double.class, PropertyKey.SIGNAL_QUALITY, 96.0, PropertyUnit.PERCENTAGE)));
  }

  private DeviceProfile encoderProfile() {
    return profile(
        DeviceType.ENCODER,
        Map.of(
            PropertyKey.CODEC,
                definition(Codec.class, PropertyKey.CODEC, Codec.H264, PropertyUnit.NONE),
            PropertyKey.BITRATE,
                definition(
                    Double.class, PropertyKey.BITRATE, 8.0, PropertyUnit.MEGABITS_PER_SECOND)));
  }

  private DeviceProfile routerProfile() {
    return profile(
        DeviceType.ROUTER,
        Map.of(
            PropertyKey.LINK_CAPACITY,
            definition(
                Double.class, PropertyKey.LINK_CAPACITY, 100.0, PropertyUnit.MEGABITS_PER_SECOND)));
  }

  private DeviceProfile profile(
      DeviceType deviceType, Map<PropertyKey, PropertyDefinition<?>> properties) {
    return DeviceProfile.builder()
        .name(deviceType.name())
        .supportedDeviceType(deviceType)
        .defaultProperties(properties)
        .build();
  }

  private <T> PropertyDefinition<T> definition(
      Class<T> type, PropertyKey key, T value, PropertyUnit unit) {
    return PropertyDefinition.<T>builder()
        .key(key)
        .displayName(key.name())
        .valueClass(type)
        .valueType(valueType(value))
        .category(PropertyCategory.CONFIGURATION)
        .unit(unit)
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
    return signal(60);
  }

  private Signal signal(double framesPerSecond) {
    return Signal.builder()
        .signalId(SignalId.generate())
        .sourceDeviceId(DeviceId.generate(DeviceType.CAMERA))
        .resolution(Resolution.FULL_HD_1080P)
        .framesPerSecond(framesPerSecond)
        .codec(Codec.RAW)
        .bitrateMegabitsPerSecond(8.0)
        .qualityPercentage(96.0)
        .latency(Duration.ZERO)
        .build();
  }

  private Port inputPort() {
    return new Port("IN", PortType.VIDEO, PortDirection.INPUT);
  }

  private Port outputPort() {
    return new Port("OUT", PortType.VIDEO, PortDirection.OUTPUT);
  }
}
