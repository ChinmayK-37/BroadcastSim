package com.broadcastsim.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.DeviceState;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.EventType;
import com.broadcastsim.core.common.enums.SimulationState;
import com.broadcastsim.core.connection.ConnectionRegistry;
import com.broadcastsim.core.device.base.Device;
import com.broadcastsim.core.event.EventQueue;
import com.broadcastsim.core.registry.DeviceRegistry;
import com.broadcastsim.core.signal.Signal;
import com.broadcastsim.core.valueobject.DeviceId;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Verifies the broadcast engine infrastructure foundation. */
class BroadcastEngineTest {

  @Test
  void managesTheEngineLifecycle() {
    BroadcastEngine engine = new BroadcastEngine(newContext());

    engine.start();
    assertEquals(SimulationState.RUNNING, engine.getSimulationState());

    engine.pause();
    assertEquals(SimulationState.PAUSED, engine.getSimulationState());

    engine.resume();
    engine.stop();
    assertEquals(SimulationState.STOPPED, engine.getSimulationState());
  }

  @Test
  void rejectsInvalidLifecycleTransitions() {
    BroadcastEngine engine = new BroadcastEngine(newContext());

    assertThrows(IllegalStateException.class, engine::pause);
    assertThrows(IllegalStateException.class, engine::resume);
    engine.start();
    assertThrows(IllegalStateException.class, engine::start);
  }

  @Test
  void advancesSimulationTimeWithoutScheduling() {
    SimulationClock clock = new SimulationClock(Duration.ofMillis(200));

    assertEquals(1, clock.advance());
    assertEquals(1, clock.getCurrentTick());
    assertEquals(Duration.ofMillis(200), clock.getTickInterval());
  }

  @Test
  void registersAndFiltersDevices() {
    DeviceRegistry registry = new DeviceRegistry();
    Device camera = new TestDevice(DeviceId.generate(DeviceType.CAMERA), DeviceType.CAMERA);
    Device router = new TestDevice(DeviceId.generate(DeviceType.ROUTER), DeviceType.ROUTER);
    registry.register(camera);
    registry.register(router);

    assertTrue(registry.contains(camera.getDeviceId()));
    assertEquals(camera, registry.get(camera.getDeviceId()).orElseThrow());
    assertEquals(1, registry.getByType(DeviceType.CAMERA).size());
    assertEquals(router, registry.unregister(router.getDeviceId()).orElseThrow());
  }

  @Test
  void maintainsEventsInFirstInFirstOutOrder() {
    EventQueue eventQueue = new EventQueue();
    eventQueue.enqueue(EventType.CONNECT);
    eventQueue.enqueue(EventType.RESTART);

    assertEquals(EventType.CONNECT, eventQueue.peek().orElseThrow());
    assertEquals(EventType.CONNECT, eventQueue.dequeue().orElseThrow());
    assertEquals(EventType.RESTART, eventQueue.dequeue().orElseThrow());
    assertTrue(eventQueue.isEmpty());

    eventQueue.enqueue(EventType.RECOVER);
    eventQueue.clear();
    assertTrue(eventQueue.isEmpty());
  }

  private SimulationContext newContext() {
    return new SimulationContext(
        new DeviceRegistry(),
        new ConnectionRegistry(),
        new SimulationClock(Duration.ofMillis(200)),
        new EventQueue());
  }

  private record TestDevice(DeviceId deviceId, DeviceType deviceType) implements Device {

    @Override
    public void initialize() {}

    @Override
    public void update() {}

    @Override
    public void calculate() {}

    @Override
    public void receiveSignal(Signal signal) {}

    @Override
    public Optional<Signal> generateSignal() {
      return Optional.empty();
    }

    @Override
    public void applyEvent() {}

    @Override
    public DeviceId getDeviceId() {
      return deviceId;
    }

    @Override
    public DeviceType getDeviceType() {
      return deviceType;
    }

    @Override
    public DeviceState getDeviceState() {
      return DeviceState.CREATED;
    }
  }
}
