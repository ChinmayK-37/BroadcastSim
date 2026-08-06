package com.broadcastsim.core.common.enums;

/** Identifies every supported device property in the simulation domain. */
public enum PropertyKey {

  // Video
  RESOLUTION,
  FPS,
  BIT_DEPTH,
  CHROMA_SAMPLING,
  CODEC,
  BITRATE,

  // Signal
  SIGNAL_STATUS,
  SIGNAL_QUALITY,
  LATENCY,
  PACKET_LOSS,

  // Device Metrics
  CPU_USAGE,
  MEMORY_USAGE,
  TEMPERATURE,
  POWER_CONSUMPTION,

  // Router
  INPUT_COUNT,
  OUTPUT_COUNT,
  ACTIVE_INPUT,
  ACTIVE_OUTPUT,
  BANDWIDTH,
  LINK_CAPACITY,

  // Media Server
  STORAGE_CAPACITY,
  STORAGE_USED,

  // Viewer
  BUFFERING,
  PLAYBACK_STATUS,

  // Generic
  STATUS,
  HEALTH
}
