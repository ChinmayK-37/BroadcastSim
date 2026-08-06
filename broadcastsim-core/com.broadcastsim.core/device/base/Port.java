package com.broadcastsim.core.device.base;

import com.broadcastsim.core.common.enums.PortDirection;
import com.broadcastsim.core.common.enums.PortType;
import lombok.Value;

/** Immutable description of a device input or output port. */
@Value
public class Port {

  String name;
  PortType type;
  PortDirection direction;
}
