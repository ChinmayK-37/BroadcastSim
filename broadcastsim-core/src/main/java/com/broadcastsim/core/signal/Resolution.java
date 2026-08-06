package com.broadcastsim.core.signal;

/** Defines the supported logical video resolutions for broadcast signals. */
public enum Resolution {
  SD_480P(720, 480),
  HD_720P(1280, 720),
  FULL_HD_1080P(1920, 1080),
  UHD_4K(3840, 2160),
  UHD_8K(7680, 4320);

  private final int width;
  private final int height;

  Resolution(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Returns the horizontal pixel count.
   *
   * @return the width in pixels
   */
  public int getWidth() {
    return width;
  }

  /**
   * Returns the vertical pixel count.
   *
   * @return the height in pixels
   */
  public int getHeight() {
    return height;
  }
}
