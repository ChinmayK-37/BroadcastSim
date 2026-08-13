package com.broadcastsim.core.rule;

import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.device.camera.Camera;
import com.broadcastsim.core.device.encoder.Encoder;
import com.broadcastsim.core.device.router.VideoRouter;
import com.broadcastsim.core.device.runtime.DeviceRuntime;
import com.broadcastsim.core.engineering.rules.CameraRuleModel;
import com.broadcastsim.core.engineering.rules.EncoderRuleModel;
import com.broadcastsim.core.engineering.rules.ExecutionStatus;
import com.broadcastsim.core.engineering.rules.RuleExecutionResult;
import com.broadcastsim.core.engineering.rules.RuleModelName;
import com.broadcastsim.core.engineering.rules.ValidationStatus;
import com.broadcastsim.core.engineering.rules.VideoRouterRuleModel;
import com.broadcastsim.core.registry.DeviceRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Coordinates deterministic execution of the available device engineering rule models. */
public final class RuleEngine {

  private final DeviceRegistry deviceRegistry;
  private final CameraRuleModel cameraRuleModel;
  private final VideoRouterRuleModel videoRouterRuleModel;
  private final EncoderRuleModel encoderRuleModel;

  /**
   * Creates an engine using the MVP device rule models.
   *
   * @param deviceRegistry the registry whose devices will be processed
   */
  public RuleEngine(DeviceRegistry deviceRegistry) {
    this(deviceRegistry, new CameraRuleModel(), new VideoRouterRuleModel(), new EncoderRuleModel());
  }

  RuleEngine(
      DeviceRegistry deviceRegistry,
      CameraRuleModel cameraRuleModel,
      VideoRouterRuleModel videoRouterRuleModel,
      EncoderRuleModel encoderRuleModel) {
    this.deviceRegistry =
        Objects.requireNonNull(deviceRegistry, "device registry must not be null");
    this.cameraRuleModel =
        Objects.requireNonNull(cameraRuleModel, "camera rule model must not be null");
    this.videoRouterRuleModel =
        Objects.requireNonNull(videoRouterRuleModel, "video router rule model must not be null");
    this.encoderRuleModel =
        Objects.requireNonNull(encoderRuleModel, "encoder rule model must not be null");
  }

  /**
   * Executes all registered devices at one supplied simulation timestamp.
   *
   * <p>Devices execute in the Version 1 engineering order: camera, video router, then encoder.
   * Unsupported devices are reported without preventing supported devices from executing.
   *
   * @param executionTimestamp the deterministic timestamp for this cycle
   * @return aggregate results for the execution cycle
   */
  public RuleExecutionReport execute(Instant executionTimestamp) {
    Instant requiredTimestamp =
        Objects.requireNonNull(executionTimestamp, "execution timestamp must not be null");
    List<RuleExecutionResult> results = new ArrayList<>();
    List<com.broadcastsim.core.valueobject.DeviceId> unsupportedDeviceIds = new ArrayList<>();

    executeCameras(requiredTimestamp, results);
    executeVideoRouters(requiredTimestamp, results);
    executeEncoders(requiredTimestamp, results);
    collectUnsupportedDevices(unsupportedDeviceIds);

    return new RuleExecutionReport(requiredTimestamp, results, unsupportedDeviceIds);
  }

  /**
   * Resolves the rule model name for an MVP device.
   *
   * @param device the device to resolve
   * @return the associated rule model, or empty when the device is unsupported
   */
  public Optional<RuleModelName> resolveRuleModel(Device device) {
    Objects.requireNonNull(device, "device must not be null");
    if (device instanceof Camera) {
      return Optional.of(RuleModelName.CAMERA);
    }
    if (device instanceof VideoRouter) {
      return Optional.of(RuleModelName.VIDEO_ROUTER);
    }
    if (device instanceof Encoder) {
      return Optional.of(RuleModelName.ENCODER);
    }
    return Optional.empty();
  }

  private void executeCameras(Instant timestamp, List<RuleExecutionResult> results) {
    for (Device device : deviceRegistry.getAll()) {
      if (device instanceof Camera camera) {
        results.add(executeCamera(camera, timestamp));
      }
    }
  }

  private void executeVideoRouters(Instant timestamp, List<RuleExecutionResult> results) {
    for (Device device : deviceRegistry.getAll()) {
      if (device instanceof VideoRouter videoRouter) {
        results.add(executeVideoRouter(videoRouter, timestamp));
      }
    }
  }

  private void executeEncoders(Instant timestamp, List<RuleExecutionResult> results) {
    for (Device device : deviceRegistry.getAll()) {
      if (device instanceof Encoder encoder) {
        results.add(executeEncoder(encoder, timestamp));
      }
    }
  }

  private RuleExecutionResult executeCamera(Camera camera, Instant timestamp) {
    try {
      return cameraRuleModel.execute(camera, timestamp);
    } catch (RuntimeException exception) {
      return failedResult(camera, camera.getDeviceRuntime(), RuleModelName.CAMERA, timestamp);
    }
  }

  private RuleExecutionResult executeVideoRouter(VideoRouter videoRouter, Instant timestamp) {
    try {
      return videoRouterRuleModel.execute(videoRouter, timestamp);
    } catch (RuntimeException exception) {
      return failedResult(
          videoRouter, videoRouter.getDeviceRuntime(), RuleModelName.VIDEO_ROUTER, timestamp);
    }
  }

  private RuleExecutionResult executeEncoder(Encoder encoder, Instant timestamp) {
    try {
      return encoderRuleModel.execute(encoder, timestamp);
    } catch (RuntimeException exception) {
      return failedResult(encoder, encoder.getDeviceRuntime(), RuleModelName.ENCODER, timestamp);
    }
  }

  private RuleExecutionResult failedResult(
      Device device,
      DeviceRuntime deviceRuntime,
      RuleModelName ruleModelName,
      Instant executionTimestamp) {
    return RuleExecutionResult.builder()
        .deviceId(device.getDeviceId())
        .ruleModelName(ruleModelName)
        .executionStatus(ExecutionStatus.FAILURE)
        .updatedDeviceRuntime(deviceRuntime)
        .validationStatus(ValidationStatus.INVALID)
        .executionTimestamp(executionTimestamp)
        .build();
  }

  private void collectUnsupportedDevices(
      List<com.broadcastsim.core.valueobject.DeviceId> unsupportedDeviceIds) {
    for (Device device : deviceRegistry.getAll()) {
      if (resolveRuleModel(device).isEmpty()) {
        unsupportedDeviceIds.add(device.getDeviceId());
      }
    }
  }
}
