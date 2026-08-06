package com.broadcastsim.core.device.base;

import com.broadcastsim.core.signal.Signal;
import java.util.Optional;

/** Defines a device that produces logical signals on identified output ports. */
public interface PortSignalProducer {

  /**
   * Generates a logical signal for a specific output port.
   *
   * @param outputPort the output port from which to generate the signal
   * @return the generated signal, if available
   */
  Optional<Signal> generateSignal(Port outputPort);
}
