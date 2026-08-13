package com.broadcastsim.web.controller;

import com.broadcastsim.web.service.SimulationFacade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Renders the single server-side dashboard page for the web MVP. */
@Controller
public class DashboardController {

  private final SimulationFacade simulationFacade;

  /**
   * Creates the dashboard controller.
   *
   * @param simulationFacade web facade for the existing simulation core
   */
  public DashboardController(SimulationFacade simulationFacade) {
    this.simulationFacade = simulationFacade;
  }

  /**
   * Renders the simulation dashboard using current core state.
   *
   * @param model model supplied to the Thymeleaf template
   * @return dashboard template name
   */
  @GetMapping("/")
  public String dashboard(Model model) {
    model.addAttribute("status", simulationFacade.status());
    model.addAttribute("elapsedSimulationTime", simulationFacade.elapsedSimulationTime());
    model.addAttribute("timeline", simulationFacade.recentTimeline());
    return "dashboard";
  }
}
