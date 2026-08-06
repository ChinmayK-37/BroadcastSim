package com.broadcastsim.core.connection;

import com.broadcastsim.core.common.enums.ConnectionStatus;
import com.broadcastsim.core.device.base.Port;
import com.broadcastsim.core.valueobject.ConnectionId;
import com.broadcastsim.core.valueobject.DeviceId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Represents a logical link between an output port and an input port. */
@Getter
public final class Connection {

  private final ConnectionId connectionId;
  private final DeviceId sourceDeviceId;
  private final Port sourcePort;
  private final DeviceId targetDeviceId;
  private final Port targetPort;
  @Setter private ConnectionStatus status;

  /**
   * Creates a logical connection between two device ports.
   *
   * @param connectionId the connection identifier
   * @param sourceDeviceId the source device identifier
   * @param sourcePort the source output port
   * @param targetDeviceId the target device identifier
   * @param targetPort the target input port
   * @param status the connection status
   */
  @Builder
  public Connection(
      ConnectionId connectionId,
      DeviceId sourceDeviceId,
      Port sourcePort,
      DeviceId targetDeviceId,
      Port targetPort,
      ConnectionStatus status) {
    this.connectionId = connectionId;
    this.sourceDeviceId = sourceDeviceId;
    this.sourcePort = sourcePort;
    this.targetDeviceId = targetDeviceId;
    this.targetPort = targetPort;
    this.status = status;
  }
}
