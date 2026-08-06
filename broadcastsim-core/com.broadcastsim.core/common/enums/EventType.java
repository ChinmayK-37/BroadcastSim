package com.broadcastsim.core.common.enums;

/** Identifies the user-triggered events supported by the simulation domain. */
public enum EventType {
  CONNECT,
  DISCONNECT,
  RESTART,
  POWER_FAILURE,
  SIGNAL_LOSS,
  HIGH_CPU,
  HIGH_TEMPERATURE,
  RECOVER
}
