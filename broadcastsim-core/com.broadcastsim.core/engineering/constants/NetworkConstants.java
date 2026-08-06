package com.broadcastsim.core.engineering.constants;

/**
 * Defines network, signal-quality, and interface-capacity constants from ECS sections 7 through 11.
 */
public final class NetworkConstants {

  public static final double BASE_LATENCY_MILLISECONDS = 0.20;
  public static final double BITS_PER_MEGABIT = 1000000.0;
  public static final double SWITCH_DELAY_MILLISECONDS = 0.10;
  public static final double QUEUE_DELAY_MILLISECONDS = 0.02;
  public static final double IDEAL_PACKET_LOSS_PERCENTAGE = 0.0;
  public static final double LOSS_WEIGHT = 2.0;
  public static final double LATENCY_WEIGHT = 0.10;
  public static final double MINIMUM_PERCENTAGE = 0.0;
  public static final double MAXIMUM_PERCENTAGE = 100.0;
  public static final double EXCELLENT_SIGNAL_QUALITY_PERCENTAGE = 95.0;
  public static final double GOOD_SIGNAL_QUALITY_PERCENTAGE = 80.0;
  public static final double DEGRADED_SIGNAL_QUALITY_PERCENTAGE = 60.0;
  public static final double WARNING_SIGNAL_QUALITY_PERCENTAGE = 80.0;
  public static final double NORMAL_SIGNAL_QUALITY_EXCLUSIVE_PERCENTAGE = 95.0;
  public static final double SD_SDI_CAPACITY_MEGABITS_PER_SECOND = 270.0;
  public static final double HD_SDI_CAPACITY_MEGABITS_PER_SECOND = 1485.0;
  public static final double THREE_G_SDI_CAPACITY_MEGABITS_PER_SECOND = 2970.0;
  public static final double SIX_G_SDI_CAPACITY_MEGABITS_PER_SECOND = 5940.0;
  public static final double TWELVE_G_SDI_CAPACITY_MEGABITS_PER_SECOND = 11880.0;
  public static final double TWENTY_FIVE_GBE_CAPACITY_MEGABITS_PER_SECOND = 25000.0;
  public static final double FORTY_GBE_CAPACITY_MEGABITS_PER_SECOND = 40000.0;
  public static final double ONE_HUNDRED_GBE_CAPACITY_MEGABITS_PER_SECOND = 100000.0;

  private NetworkConstants() {}
}
