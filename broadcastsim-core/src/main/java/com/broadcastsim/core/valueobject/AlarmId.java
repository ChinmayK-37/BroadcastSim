package com.broadcastsim.core.valueobject;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Immutable value object that uniquely identifies a device alarm. */
@Getter
@EqualsAndHashCode
public final class AlarmId {

  private static final String ID_FORMAT = "ALM-%03d";
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private final String value;

  private AlarmId(String value) {
    this.value = value;
  }

  /**
   * Generates the next logical alarm identifier.
   *
   * @return the next alarm identifier
   */
  public static AlarmId generate() {
    return new AlarmId(ID_FORMAT.formatted(COUNTER.incrementAndGet()));
  }

  /**
   * Returns the serialized alarm identifier value.
   *
   * @return the identifier value
   */
  @Override
  public String toString() {
    return value;
  }
}
