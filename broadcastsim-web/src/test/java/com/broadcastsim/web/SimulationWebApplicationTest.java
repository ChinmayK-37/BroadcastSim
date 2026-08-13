package com.broadcastsim.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("dashboard"))
        .andExpect(content().string(containsString("dashboard-main")))
        .andExpect(content().string(containsString("id=\"simulation-time\"")))
        .andExpect(content().string(containsString("refreshTimeline")))
        .andExpect(content().string(containsString("Simulation")))
        .andExpect(content().string(containsString("Broadcast Pipeline")))
        .andExpect(content().string(containsString("Device Metrics")))
        .andExpect(content().string(containsString("Active Alarms")))
        .andExpect(content().string(containsString("Recent Timeline")))
        .andExpect(content().string(containsString("CAMERA")))
        .andExpect(content().string(containsString("NORMAL")))
        .andExpect(content().string(containsString("00:00:00")))
        .andExpect(content().string(containsString("disabled=\"disabled\"")));
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
  void dashboardReflectsRunningPausedAndStoppedControlStates() throws Exception {
    mockMvc.perform(post("/api/simulation/start")).andExpect(status().isOk());
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("RUNNING")));
    mockMvc.perform(post("/api/simulation/pause")).andExpect(status().isOk());
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PAUSED")));
    mockMvc.perform(post("/api/simulation/stop")).andExpect(status().isOk());
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("STOPPED")));
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
  void schedulesCameraFpsThroughTheExistingScenarioMechanism() throws Exception {
    mockMvc.perform(post("/api/simulation/start")).andExpect(status().isOk());
    mockMvc
        .perform(post("/api/simulation/camera/fps").param("framesPerSecond", "30"))
        .andExpect(status().isOk());

    mockMvc
        .perform(post("/api/simulation/tick"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latestTick.scenarioEvents[0].eventType").value("SET_PROPERTY"));
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

  @Test
  void dashboardDisplaysOnlyTheTenMostRecentTimelineSnapshots() throws Exception {
    mockMvc.perform(post("/api/simulation/start")).andExpect(status().isOk());
    for (int tick = 0; tick < 11; tick++) {
      mockMvc.perform(post("/api/simulation/tick")).andExpect(status().isOk());
    }

    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("timeline", hasSize(10)))
        .andExpect(content().string(containsString("1244.16")));
  }

  @Test
  void dashboardDisplaysElapsedTimeForSixtyFiveCompletedTicks() throws Exception {
    advanceSimulation(65);

    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("00:01:05")));
  }

  @Test
  void dashboardDisplaysElapsedTimeForOneHundredCompletedTicks() throws Exception {
    advanceSimulation(100);

    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("00:01:40")));
  }

  private void advanceSimulation(int tickCount) throws Exception {
    mockMvc.perform(post("/api/simulation/start")).andExpect(status().isOk());
    for (int tick = 0; tick < tickCount; tick++) {
      mockMvc.perform(post("/api/simulation/tick")).andExpect(status().isOk());
    }
  }
}
