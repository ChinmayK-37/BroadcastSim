package com.broadcastsim.core.valueobject;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Immutable value object that uniquely identifies a scenario event. */
@Getter
@EqualsAndHashCode
public final class EventId {

  private static final String ID_FORMAT = "EVT-%03d";
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private final String value;

  private EventId(String value) {
    this.value = value;
  }

  /**
   * Generates the next scenario event identifier.
   *
   * @return a generated event identifier
   */
  public static EventId generate() {
    return new EventId(ID_FORMAT.formatted(COUNTER.incrementAndGet()));
  }

  /**
   * Returns the serialized event identifier.
   *
   * @return the event identifier value
   */
  @Override
  public String toString() {
    return value;
  }
}
