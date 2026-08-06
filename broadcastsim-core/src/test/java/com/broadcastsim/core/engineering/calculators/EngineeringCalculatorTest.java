package com.broadcastsim.core.engineering.calculators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.HealthStatus;
import com.broadcastsim.core.common.enums.OperationalFailureType;
import com.broadcastsim.core.engineering.constants.CodecConstants;
import com.broadcastsim.core.engineering.constants.CpuConstants;
import com.broadcastsim.core.engineering.constants.MemoryConstants;
import com.broadcastsim.core.engineering.constants.NetworkConstants;
import com.broadcastsim.core.engineering.constants.PowerConstants;
import com.broadcastsim.core.engineering.constants.ResolutionConstants;
import com.broadcastsim.core.engineering.constants.TemperatureConstants;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies the engineering calculations specified by EFS and ECS. */
class EngineeringCalculatorTest {

  @Test
  void calculatesVideoBandwidthAndValidatesLinkCapacity() {
    double pixelsPerSecond =
        BandwidthCalculator.calculatePixelsPerSecond(
            ResolutionConstants.FULL_HD_WIDTH_PIXELS,
            ResolutionConstants.FULL_HD_HEIGHT_PIXELS,
            ResolutionConstants.FRAME_RATE_30);
    double rawBitrate =
        BandwidthCalculator.calculateRawBitrate(
            pixelsPerSecond, ResolutionConstants.CHROMA_422_BITS_PER_PIXEL);

    assertEquals(62208000.0, pixelsPerSecond);
    assertEquals(995328000.0, rawBitrate);
    assertEquals(
        16588800.0,
        BandwidthCalculator.calculateCompressedBitrate(
            rawBitrate, CodecConstants.H264_COMPRESSION_RATIO));
    assertEquals(30.0, BandwidthCalculator.calculateThroughput(List.of(10.0, 20.0)));
    assertEquals(
        50.0,
        BandwidthCalculator.calculateLinkUtilization(
            NetworkConstants.HD_SDI_CAPACITY_MEGABITS_PER_SECOND / 2,
            NetworkConstants.HD_SDI_CAPACITY_MEGABITS_PER_SECOND));
    assertTrue(
        BandwidthCalculator.isWithinLinkCapacity(
            NetworkConstants.HD_SDI_CAPACITY_MEGABITS_PER_SECOND,
            NetworkConstants.HD_SDI_CAPACITY_MEGABITS_PER_SECOND));
    assertFalse(
        BandwidthCalculator.isWithinLinkCapacity(
            NetworkConstants.HD_SDI_CAPACITY_MEGABITS_PER_SECOND + 1,
            NetworkConstants.HD_SDI_CAPACITY_MEGABITS_PER_SECOND));
  }

  @Test
  void rejectsInvalidBandwidthInputs() {
    assertThrows(
        IllegalArgumentException.class,
        () -> BandwidthCalculator.calculatePixelsPerSecond(-1, 1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> BandwidthCalculator.calculateRawBitrate(1, -1));
    assertThrows(
        IllegalArgumentException.class, () -> BandwidthCalculator.calculateCompressedBitrate(1, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> BandwidthCalculator.calculateThroughput(Arrays.asList(1.0, null)));
    assertThrows(NullPointerException.class, () -> BandwidthCalculator.calculateThroughput(null));
    assertThrows(
        IllegalArgumentException.class, () -> BandwidthCalculator.calculateLinkUtilization(1, 0));
    assertThrows(
        IllegalArgumentException.class, () -> BandwidthCalculator.isWithinLinkCapacity(-1, 1));
  }

  @Test
  void calculatesEncoderAndRouterCpuUsage() {
    assertEquals(
        33.0,
        CpuCalculator.calculateEncoderCpuUsage(
            CpuConstants.ENCODER_BASE_CPU_PERCENTAGE,
            ResolutionConstants.FULL_HD_WEIGHT,
            ResolutionConstants.FRAME_RATE_60_WEIGHT,
            CodecConstants.H264_FORMULA_WEIGHT,
            3,
            CpuConstants.STREAM_WEIGHT));
    assertEquals(
        16.0,
        CpuCalculator.calculateRouterCpuUsage(
            CpuConstants.ROUTER_BASE_CPU_PERCENTAGE,
            2,
            CpuConstants.ROUTE_WEIGHT,
            100.0,
            CpuConstants.UTILIZATION_WEIGHT));
  }

  @Test
  void rejectsInvalidCpuInputs() {
    assertThrows(
        IllegalArgumentException.class,
        () -> CpuCalculator.calculateEncoderCpuUsage(-1, 1, 1, 1, 1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> CpuCalculator.calculateEncoderCpuUsage(1, 1, 1, 1, 0, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> CpuCalculator.calculateRouterCpuUsage(1, -1, 1, 1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> CpuCalculator.calculateRouterCpuUsage(1, 1, Double.NaN, 1, 1));
  }

  @Test
  void calculatesEncoderMemoryUsageAndRejectsInvalidInputs() {
    assertEquals(
        832.0,
        MemoryCalculator.calculateEncoderMemoryUsage(
            MemoryConstants.ENCODER_BASE_MEMORY_MEGABYTES,
            MemoryConstants.FRAME_BUFFER_MEGABYTES,
            3,
            CodecConstants.H264_BUFFER_MEGABYTES));
    assertEquals(
        MemoryConstants.ROUTER_BASE_MEMORY_MEGABYTES,
        MemoryCalculator.calculateRouterMemoryUsage(MemoryConstants.ROUTER_BASE_MEMORY_MEGABYTES));
    assertThrows(
        IllegalArgumentException.class,
        () -> MemoryCalculator.calculateEncoderMemoryUsage(-1, 1, 1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> MemoryCalculator.calculateEncoderMemoryUsage(1, 1, 0, 1));
    assertThrows(
        IllegalArgumentException.class, () -> MemoryCalculator.calculateRouterMemoryUsage(-1));
  }

  @Test
  void calculatesPowerAndRejectsOutOfRangeInputs() {
    assertEquals(
        50.0,
        PowerCalculator.calculatePowerConsumption(
            PowerConstants.ENCODER_IDLE_POWER_WATTS,
            50.0,
            PowerConstants.ENCODER_MAXIMUM_POWER_WATTS));
    assertEquals(
        50.0,
        PowerCalculator.calculatePowerUtilization(
            PowerConstants.ENCODER_MAXIMUM_POWER_WATTS / 2,
            PowerConstants.ENCODER_MAXIMUM_POWER_WATTS));
    assertThrows(
        IllegalArgumentException.class, () -> PowerCalculator.calculatePowerConsumption(1, 101, 2));
    assertThrows(
        IllegalArgumentException.class, () -> PowerCalculator.calculatePowerConsumption(2, 1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> PowerCalculator.calculatePowerUtilization(1, 0));
  }

  @Test
  void calculatesTemperatureAndPreventsOvercoolingBelowAmbient() {
    assertEquals(
        30.1,
        TemperatureCalculator.calculateNextTemperature(
            30.0,
            TemperatureConstants.CAMERA_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK
                * PowerConstants.CAMERA_MAXIMUM_POWER_WATTS,
            TemperatureConstants.CAMERA_COOLING_RATE_CELSIUS_PER_TICK));
    assertEquals(
        34.0,
        TemperatureCalculator.calculateNextTemperatureFromPower(
            30.0,
            PowerConstants.ENCODER_MAXIMUM_POWER_WATTS,
            TemperatureConstants.ENCODER_HEAT_COEFFICIENT_CELSIUS_PER_WATT_PER_TICK,
            TemperatureConstants.ENCODER_COOLING_RATE_CELSIUS_PER_TICK));
    assertEquals(
        TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
        TemperatureCalculator.calculateCooledTemperature(
            TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
            TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS,
            TemperatureConstants.CAMERA_COOLING_RATE_CELSIUS_PER_TICK));
    assertThrows(
        IllegalArgumentException.class,
        () -> TemperatureCalculator.calculateNextTemperature(Double.NaN, 1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> TemperatureCalculator.calculateCooledTemperature(1, 1, -1));
  }

  @Test
  void calculatesSignalQualityDelayAndClampsAtBoundaries() {
    double packetLossPenalty =
        SignalQualityCalculator.calculatePacketLossPenalty(10.0, NetworkConstants.LOSS_WEIGHT);
    double latencyPenalty =
        SignalQualityCalculator.calculateLatencyPenalty(50.0, NetworkConstants.LATENCY_WEIGHT);

    assertEquals(20.0, packetLossPenalty);
    assertEquals(5.0, latencyPenalty);
    assertEquals(
        75.0, SignalQualityCalculator.calculateSignalQuality(packetLossPenalty, latencyPenalty));
    assertEquals(0.0, SignalQualityCalculator.calculateSignalQuality(100.0, 1.0));
    assertEquals(
        0.32,
        SignalQualityCalculator.calculateSignalDelay(
            NetworkConstants.BASE_LATENCY_MILLISECONDS,
            NetworkConstants.SWITCH_DELAY_MILLISECONDS,
            NetworkConstants.QUEUE_DELAY_MILLISECONDS),
        0.000001);
    assertThrows(
        IllegalArgumentException.class,
        () -> SignalQualityCalculator.calculatePacketLossPenalty(-1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> SignalQualityCalculator.calculateLatencyPenalty(1, -1));
  }

  @Test
  void evaluatesHealthThresholdsAndHighestSeverity() {
    assertEquals(
        HealthStatus.WARNING,
        HealthCalculator.evaluateCpuUsage(CpuConstants.WARNING_CPU_PERCENTAGE));
    assertEquals(
        HealthStatus.WARNING,
        HealthCalculator.evaluateCpuUsage(CpuConstants.WARNING_CPU_PERCENTAGE + 1));
    assertEquals(
        HealthStatus.CRITICAL,
        HealthCalculator.evaluateCpuUsage(CpuConstants.CRITICAL_CPU_PERCENTAGE + 1));
    assertEquals(
        HealthStatus.WARNING,
        HealthCalculator.evaluateTemperature(TemperatureConstants.WARNING_TEMPERATURE_CELSIUS));
    assertEquals(
        HealthStatus.WARNING,
        HealthCalculator.evaluateTemperature(TemperatureConstants.WARNING_TEMPERATURE_CELSIUS + 1));
    assertEquals(
        HealthStatus.CRITICAL,
        HealthCalculator.evaluateTemperature(
            TemperatureConstants.CRITICAL_TEMPERATURE_CELSIUS + 1));
    assertEquals(
        HealthStatus.CRITICAL,
        HealthCalculator.evaluateMemoryUsage(
            MemoryConstants.CRITICAL_MEMORY_UTILIZATION_PERCENTAGE + 1));
    assertEquals(
        HealthStatus.WARNING,
        HealthCalculator.evaluatePowerUtilization(
            PowerConstants.WARNING_POWER_UTILIZATION_PERCENTAGE));
    assertEquals(
        HealthStatus.CRITICAL,
        HealthCalculator.evaluateSignalQuality(
            NetworkConstants.WARNING_SIGNAL_QUALITY_PERCENTAGE - 1));
    assertEquals(
        HealthStatus.WARNING,
        HealthCalculator.evaluateSignalQuality(
            NetworkConstants.NORMAL_SIGNAL_QUALITY_EXCLUSIVE_PERCENTAGE));
    assertEquals(
        HealthStatus.NORMAL,
        HealthCalculator.evaluateSignalQuality(
            NetworkConstants.NORMAL_SIGNAL_QUALITY_EXCLUSIVE_PERCENTAGE + 1));
    assertEquals(HealthStatus.NORMAL, HealthCalculator.evaluateOperationalHealth(Set.of()));
    assertEquals(
        HealthStatus.FAILED,
        HealthCalculator.evaluateOperationalHealth(
            Set.of(OperationalFailureType.RUNTIME_WATCHDOG_TERMINATION)));
    assertEquals(
        HealthStatus.FAILED,
        HealthCalculator.evaluateOverallHealth(
            HealthStatus.WARNING,
            HealthStatus.NORMAL,
            HealthStatus.CRITICAL,
            HealthStatus.NORMAL,
            HealthStatus.FAILED));
    assertThrows(IllegalArgumentException.class, () -> HealthCalculator.evaluateCpuUsage(-1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            HealthCalculator.evaluateTemperature(
                TemperatureConstants.AMBIENT_TEMPERATURE_CELSIUS - 1));
    assertThrows(
        NullPointerException.class,
        () ->
            HealthCalculator.evaluateOverallHealth(
                null,
                HealthStatus.NORMAL,
                HealthStatus.NORMAL,
                HealthStatus.NORMAL,
                HealthStatus.NORMAL));
    assertThrows(
        NullPointerException.class, () -> HealthCalculator.evaluateOperationalHealth(null));
    assertThrows(
        NullPointerException.class,
        () ->
            HealthCalculator.evaluateOperationalHealth(
                Arrays.asList(OperationalFailureType.POWER_UNAVAILABLE, null)));
  }

  @Test
  void producesDeterministicResultsForIdenticalInputs() {
    double first =
        CpuCalculator.calculateEncoderCpuUsage(
            CpuConstants.ENCODER_BASE_CPU_PERCENTAGE,
            ResolutionConstants.UHD_4K_WEIGHT,
            ResolutionConstants.FRAME_RATE_60_WEIGHT,
            CodecConstants.H265_FORMULA_WEIGHT,
            2,
            CpuConstants.STREAM_WEIGHT);
    double second =
        CpuCalculator.calculateEncoderCpuUsage(
            CpuConstants.ENCODER_BASE_CPU_PERCENTAGE,
            ResolutionConstants.UHD_4K_WEIGHT,
            ResolutionConstants.FRAME_RATE_60_WEIGHT,
            CodecConstants.H265_FORMULA_WEIGHT,
            2,
            CpuConstants.STREAM_WEIGHT);

    assertEquals(first, second);
  }
}
