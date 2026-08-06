package com.broadcastsim.core.valueobject;

import com.broadcastsim.core.common.enums.DeviceType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Immutable value object that uniquely identifies a simulated device. */
@Getter
@EqualsAndHashCode
public final class DeviceId {

  private static final String ID_FORMAT = "%s-%03d";
  private static final Map<DeviceType, AtomicInteger> COUNTERS = createCounters();

  private final String value;

  private DeviceId(String value) {
    this.value = value;
  }

  /**
   * Generates the next identifier for the supplied device type.
   *
   * @param deviceType the device type for which to generate an identifier
   * @return the next type-specific identifier
   */
  public static DeviceId generate(DeviceType deviceType) {
    DeviceType requiredDeviceType =
        Objects.requireNonNull(deviceType, "device type must not be null");
    AtomicInteger counter = COUNTERS.get(requiredDeviceType);
    return new DeviceId(
        ID_FORMAT.formatted(requiredDeviceType.getIdentifierPrefix(), counter.incrementAndGet()));
  }

  /**
   * Returns the serialized device identifier value.
   *
   * @return the identifier value
   */
  @Override
  public String toString() {
    return value;
  }

  private static Map<DeviceType, AtomicInteger> createCounters() {
    Map<DeviceType, AtomicInteger> counters = new EnumMap<>(DeviceType.class);
    for (DeviceType deviceType : DeviceType.values()) {
      counters.put(deviceType, new AtomicInteger());
    }
    return counters;
  }
}
