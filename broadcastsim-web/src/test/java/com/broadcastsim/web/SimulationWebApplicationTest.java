package com.broadcastsim.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/** Verifies the minimal HTTP controls that delegate to the existing simulation core. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SimulationWebApplicationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void startsApplicationContextAndReturnsDashboard() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("dashboard"));
  }

  @Test
  void returnsCurrentSimulationStatus() throws Exception {
    mockMvc
        .perform(get("/api/simulation/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.simulationState").value("STOPPED"))
        .andExpect(jsonPath("$.currentTick").value(0))
        .andExpect(jsonPath("$.devices.length()").value(3));
  }

  @Test
  void startsPausesResumesAndStopsSimulation() throws Exception {
    mockMvc
        .perform(post("/api/simulation/start"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.simulationState").value("RUNNING"));
    mockMvc
        .perform(post("/api/simulation/pause"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.simulationState").value("PAUSED"));
    mockMvc
        .perform(post("/api/simulation/resume"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.simulationState").value("RUNNING"));
    mockMvc
        .perform(post("/api/simulation/stop"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.simulationState").value("STOPPED"));
  }

  @Test
  void advancesManualTickAndExposesUpdatedStatus() throws Exception {
    mockMvc.perform(post("/api/simulation/start")).andExpect(status().isOk());

    mockMvc
        .perform(post("/api/simulation/tick"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentTick").value(1))
        .andExpect(jsonPath("$.latestTick.tick").value(1));
  }

  @Test
  void returnsTimelineSnapshotsCreatedByManualTicks() throws Exception {
    mockMvc.perform(post("/api/simulation/start")).andExpect(status().isOk());
    mockMvc.perform(post("/api/simulation/tick")).andExpect(status().isOk());

    mockMvc
        .perform(get("/api/simulation/timeline"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].deviceSnapshots.length()").value(3));
  }
}
