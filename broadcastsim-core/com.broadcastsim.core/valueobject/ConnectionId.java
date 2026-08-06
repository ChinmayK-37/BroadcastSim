package com.broadcastsim.core.valueobject;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Immutable value object that uniquely identifies a logical connection. */
@Getter
@EqualsAndHashCode
public final class ConnectionId {

  private static final String ID_FORMAT = "CON-%03d";
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private final String value;

  private ConnectionId(String value) {
    this.value = value;
  }

  /**
   * Generates the next logical connection identifier.
   *
   * @return the next connection identifier
   */
  public static ConnectionId generate() {
    return new ConnectionId(ID_FORMAT.formatted(COUNTER.incrementAndGet()));
  }

  /**
   * Returns the serialized connection identifier value.
   *
   * @return the identifier value
   */
  @Override
  public String toString() {
    return value;
  }
}
