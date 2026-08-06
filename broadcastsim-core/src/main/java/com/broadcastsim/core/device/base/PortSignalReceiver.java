package com.broadcastsim.core.device.base;

import com.broadcastsim.core.signal.Signal;

/** Defines a device that accepts logical signals on identified input ports. */
public interface PortSignalReceiver {

  /**
   * Receives a logical signal on a specific input port.
   *
   * @param inputPort the receiving input port
   * @param signal the received signal
   */
  void receiveSignal(Port inputPort, Signal signal);
}
