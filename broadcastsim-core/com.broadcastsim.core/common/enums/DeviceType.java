package com.broadcastsim.core.common.enums;

/** Identifies the broadcast device categories supported by the simulator. */
public enum DeviceType {
  CAMERA("CAM"),
  ROUTER("RTR"),
  ENCODER("ENC"),
  DECODER("DEC"),
  MEDIA_SERVER("MSV"),
  VIEWER("VWR");

  private final String identifierPrefix;

  DeviceType(String identifierPrefix) {
    this.identifierPrefix = identifierPrefix;
  }

  /**
   * Returns the prefix used for generated device identifiers.
   *
   * @return the device identifier prefix
   */
  public String getIdentifierPrefix() {
    return identifierPrefix;
  }
}
