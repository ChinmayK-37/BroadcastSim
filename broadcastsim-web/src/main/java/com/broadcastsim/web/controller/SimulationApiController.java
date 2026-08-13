package com.broadcastsim.web.controller;

import com.broadcastsim.core.timeline.SimulationSnapshot;
import com.broadcastsim.web.dto.SimulationStatusResponse;
import com.broadcastsim.web.service.SimulationFacade;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes manual simulation lifecycle controls through the MVP REST API. */
@RestController
@RequestMapping("/api/simulation")
public class SimulationApiController {

  private final SimulationFacade simulationFacade;

  /**
   * Creates the API controller.
   *
   * @param simulationFacade web facade for the existing simulation core
   */
  public SimulationApiController(SimulationFacade simulationFacade) {
    this.simulationFacade = simulationFacade;
  }

  /**
   * Returns current simulation status.
   *
   * @return current simulation status
   */
  @GetMapping("/status")
  public SimulationStatusResponse status() {
    return simulationFacade.status();
  }

  /**
   * Starts the simulation.
   *
   * @return current simulation status
   */
  @PostMapping("/start")
  public SimulationStatusResponse start() {
    return simulationFacade.start();
  }

  /**
   * Pauses the simulation.
   *
   * @return current simulation status
   */
  @PostMapping("/pause")
  public SimulationStatusResponse pause() {
    return simulationFacade.pause();
  }

  /**
   * Resumes the simulation.
   *
   * @return current simulation status
   */
  @PostMapping("/resume")
  public SimulationStatusResponse resume() {
    return simulationFacade.resume();
  }

  /**
   * Stops the simulation.
   *
   * @return current simulation status
   */
  @PostMapping("/stop")
  public SimulationStatusResponse stop() {
    return simulationFacade.stop();
  }

  /**
   * Advances the simulation by one manual tick.
   *
   * @return current simulation status after processing the tick
   */
  @PostMapping("/tick")
  public SimulationStatusResponse tick() {
    return simulationFacade.tick();
  }

  /**
   * Returns in-memory observations from completed simulation ticks.
   *
   * @return ordered simulation timeline snapshots
   */
  @GetMapping("/timeline")
  public List<SimulationSnapshot> timeline() {
    return simulationFacade.timeline();
  }
}
