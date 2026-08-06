package com.broadcastsim.core.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.ConnectionStatus;
import com.broadcastsim.core.common.enums.DeviceType;
import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PortType;
import com.broadcastsim.core.common.enums.SignalStatus;
import com.broadcastsim.core.connection.Connection;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import com.broadcastsim.core.valueobject.SignalId;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies the signal domain objects and directed topology graph. */
class SignalDomainTest {

  @Test
  void updatesMutableSignalMetadata() {
    Signal signal =
        Signal.builder()
            .signalId(SignalId.generate())
            .sourceDeviceId(DeviceId.generate(DeviceType.CAMERA))
            .resolution(Resolution.FULL_HD_1080P)
            .framesPerSecond(30)
            .codec(Codec.RAW)
            .bitrateMegabitsPerSecond(3000.0)
            .qualityPercentage(100.0)
            .latency(Duration.ZERO)
            .status(SignalStatus.ACTIVE)
            .build();

    signal.setCodec(Codec.H264);
    signal.setBitrateMegabitsPerSecond(8.0);
    signal.setStatus(SignalStatus.DEGRADED);

    assertEquals(Codec.H264, signal.getCodec());
    assertEquals(8.0, signal.getBitrateMegabitsPerSecond());
    assertEquals(SignalStatus.DEGRADED, signal.getStatus());
  }

  @Test
  void maintainsTopologyAndDetectsCycles() {
    DeviceId camera = DeviceId.generate(DeviceType.CAMERA);
    DeviceId router = DeviceId.generate(DeviceType.ROUTER);
    DeviceId encoder = DeviceId.generate(DeviceType.ENCODER);
    SignalGraph signalGraph = new SignalGraph();
    Connection cameraToRouter = connection(camera, router);
    Connection routerToEncoder = connection(router, encoder);

    signalGraph.addConnection(cameraToRouter);
    signalGraph.addConnection(routerToEncoder);

    assertTrue(signalGraph.contains(router));
    assertEquals(Set.of(router), signalGraph.getDownstream(camera));
    assertEquals(Set.of(router), signalGraph.getUpstream(encoder));
    assertFalse(signalGraph.detectCycles());

    signalGraph.addConnection(connection(encoder, camera));
    assertTrue(signalGraph.detectCycles());

    assertTrue(signalGraph.removeConnection(cameraToRouter.getConnectionId()));
    assertTrue(signalGraph.getDownstream(camera).isEmpty());
  }

  private Connection connection(DeviceId sourceDeviceId, DeviceId targetDeviceId) {
    return Connection.builder()
        .connectionId(ConnectionId.generate())
        .sourceDeviceId(sourceDeviceId)
        .sourcePort(new Port("OUT1", PortType.VIDEO, PortDirection.OUTPUT))
        .targetDeviceId(targetDeviceId)
        .targetPort(new Port("IN1", PortType.VIDEO, PortDirection.INPUT))
        .status(ConnectionStatus.CONNECTED)
        .build();
  }
}
