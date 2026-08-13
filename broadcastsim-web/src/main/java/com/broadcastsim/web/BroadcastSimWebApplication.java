package com.broadcastsim.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the BroadcastSim web MVP. */
@SpringBootApplication
public class BroadcastSimWebApplication {

  /**
   * Starts the Spring Boot application.
   *
   * @param args application startup arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(BroadcastSimWebApplication.class, args);
  }
}
