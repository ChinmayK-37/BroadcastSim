package com.broadcastsim.core.device.runtime;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.OperationalFailureType;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/** Holds the mutable operational state of a simulated device. */
@Getter
public final class DeviceRuntime {

  private DeviceState deviceState;
  private DeviceMetrics metrics;
  private HealthStatus healthStatus;
  private double healthPercentage;
  private Instant lastUpdated;
  private final Set<DeviceRuntimeFlag> runtimeFlags;
  private final Set<OperationalFailureType> operationalFailures;

  /**
   * Creates runtime state from the supplied initial operational values.
   *
   * @param deviceState the current lifecycle state
   * @param metrics the current device metrics
   * @param healthPercentage the current health percentage
   * @param lastUpdated the last runtime update timestamp
   * @param runtimeFlags the active runtime flags
   */
  public DeviceRuntime(
      DeviceState deviceState,
      DeviceMetrics metrics,
      double healthPercentage,
      Instant lastUpdated,
      Set<DeviceRuntimeFlag> runtimeFlags) {
    this(
        deviceState,
        metrics,
        HealthStatus.NORMAL,
        healthPercentage,
        lastUpdated,
        runtimeFlags,
        Set.of());
  }

  /**
   * Creates runtime state from the supplied initial operational values and health details.
   *
   * @param deviceState the current lifecycle state
   * @param metrics the current device metrics
   * @param healthStatus the current engineering health status
   * @param healthPercentage the current health percentage
   * @param lastUpdated the last runtime update timestamp
   * @param runtimeFlags the active runtime flags
   * @param operationalFailures the active operational failure conditions
   */
  public DeviceRuntime(
      DeviceState deviceState,
      DeviceMetrics metrics,
      HealthStatus healthStatus,
      double healthPercentage,
      Instant lastUpdated,
      Set<DeviceRuntimeFlag> runtimeFlags,
      Set<OperationalFailureType> operationalFailures) {
    this.deviceState = Objects.requireNonNull(deviceState, "device state must not be null");
    this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    this.healthStatus = Objects.requireNonNull(healthStatus, "health status must not be null");
    this.healthPercentage = healthPercentage;
    this.lastUpdated = Objects.requireNonNull(lastUpdated, "last updated must not be null");
    this.runtimeFlags =
        runtimeFlags.isEmpty()
            ? EnumSet.noneOf(DeviceRuntimeFlag.class)
            : EnumSet.copyOf(runtimeFlags);
    this.operationalFailures =
        operationalFailures.isEmpty()
            ? EnumSet.noneOf(OperationalFailureType.class)
            : EnumSet.copyOf(operationalFailures);
  }

  /**
   * Replaces the current device metrics.
   *
   * @param metrics the latest device metrics
   */
  public void updateMetrics(DeviceMetrics metrics) {
    this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
  }

  /**
   * Replaces the current health percentage.
   *
   * @param healthPercentage the latest health percentage
   */
  public void updateHealthPercentage(double healthPercentage) {
    this.healthPercentage = healthPercentage;
  }

  /**
   * Replaces the current engineering health status.
   *
   * @param healthStatus the latest engineering health status
   */
  public void updateHealthStatus(HealthStatus healthStatus) {
    this.healthStatus = Objects.requireNonNull(healthStatus, "health status must not be null");
  }

  /**
   * Updates the timestamp representing the last runtime change.
   *
   * @param lastUpdated the latest runtime update timestamp
   */
  public void updateLastUpdated(Instant lastUpdated) {
    this.lastUpdated = Objects.requireNonNull(lastUpdated, "last updated must not be null");
  }

  /**
   * Activates a runtime condition flag.
   *
   * @param runtimeFlag the flag to activate
   */
  public void addRuntimeFlag(DeviceRuntimeFlag runtimeFlag) {
    runtimeFlags.add(Objects.requireNonNull(runtimeFlag, "runtime flag must not be null"));
  }

  /**
   * Clears a runtime condition flag.
   *
   * @param runtimeFlag the flag to clear
   */
  public void removeRuntimeFlag(DeviceRuntimeFlag runtimeFlag) {
    runtimeFlags.remove(Objects.requireNonNull(runtimeFlag, "runtime flag must not be null"));
  }

  /**
   * Returns an immutable snapshot of active runtime condition flags.
   *
   * @return active runtime flags
   */
  public Set<DeviceRuntimeFlag> getRuntimeFlags() {
    return Set.copyOf(runtimeFlags);
  }

  /**
   * Activates an operational failure condition.
   *
   * @param operationalFailure the failure condition to activate
   */
  public void addOperationalFailure(OperationalFailureType operationalFailure) {
    operationalFailures.add(
        Objects.requireNonNull(operationalFailure, "operational failure must not be null"));
  }

  /**
   * Clears an operational failure condition.
   *
   * @param operationalFailure the failure condition to clear
   */
  public void removeOperationalFailure(OperationalFailureType operationalFailure) {
    operationalFailures.remove(
        Objects.requireNonNull(operationalFailure, "operational failure must not be null"));
  }

  /**
   * Returns an immutable snapshot of active operational failure conditions.
   *
   * @return active operational failure conditions
   */
  public Set<OperationalFailureType> getOperationalFailures() {
    return Set.copyOf(operationalFailures);
  }

  void updateDeviceState(DeviceState deviceState) {
    this.deviceState = Objects.requireNonNull(deviceState, "device state must not be null");
  }
}
