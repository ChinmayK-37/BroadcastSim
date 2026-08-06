package com.broadcastsim.core.common.enums;

/** Defines the operational failure conditions specified by EFS formula 11.4. */
public enum OperationalFailureType {
  POWER_UNAVAILABLE,
  THERMAL_PROTECTION_SHUTDOWN,
  RUNTIME_WATCHDOG_TERMINATION,
  MEMORY_ALLOCATION_FAILURE,
  REQUIRED_INPUT_SIGNAL_UNAVAILABLE,
  EXPLICIT_DEVICE_FAULT_INJECTION
}
