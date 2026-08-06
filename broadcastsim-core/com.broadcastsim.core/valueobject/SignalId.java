package com.broadcastsim.core.valueobject;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Immutable value object that uniquely identifies a logical broadcast signal. */
@Getter
@EqualsAndHashCode
public final class SignalId {

  private static final String ID_FORMAT = "SIG-%03d";
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private final String value;

  private SignalId(String value) {
    this.value = value;
  }

  /**
   * Generates the next logical signal identifier.
   *
   * @return the next signal identifier
   */
  public static SignalId generate() {
    return new SignalId(ID_FORMAT.formatted(COUNTER.incrementAndGet()));
  }

  /**
   * Returns the serialized signal identifier value.
   *
   * @return the identifier value
   */
  @Override
  public String toString() {
    return value;
  }
}
