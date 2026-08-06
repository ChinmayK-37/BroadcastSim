package com.broadcastsim.core.property;

import com.broadcastsim.core.common.enums.PropertyKey;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Owns the runtime properties of a single simulation domain object. */
public final class PropertyContainer {

  private final Map<PropertyKey, Property<?>> properties = new EnumMap<>(PropertyKey.class);

  /**
   * Adds a property under the key declared by its definition.
   *
   * @param property the property to add
   * @throws IllegalArgumentException if the property key is already present
   */
  public void addProperty(Property<?> property) {
    Objects.requireNonNull(property, "property must not be null");
    if (properties.putIfAbsent(property.getDefinition().getKey(), property) != null) {
      throw new IllegalArgumentException("property key is already present");
    }
  }

  /**
   * Finds a runtime property by its key.
   *
   * @param key the property key
   * @return the property, if present
   */
  public Optional<Property<?>> getProperty(PropertyKey key) {
    return Optional.ofNullable(properties.get(Objects.requireNonNull(key, "key must not be null")));
  }

  /**
   * Updates a registered property through its controlled mutation method.
   *
   * @param key the property key
   * @param value the new runtime value
   * @throws IllegalArgumentException if no property is registered for the key
   */
  public void updateProperty(PropertyKey key, Object value) {
    Property<?> property =
        getProperty(key)
            .orElseThrow(() -> new IllegalArgumentException("property key is not registered"));
    updateValue(property, value);
  }

  /**
   * Returns whether a property is present for the supplied key.
   *
   * @param key the property key
   * @return {@code true} when the property is present
   */
  public boolean containsProperty(PropertyKey key) {
    return properties.containsKey(Objects.requireNonNull(key, "key must not be null"));
  }

  /**
   * Returns an immutable view of all runtime properties.
   *
   * @return properties keyed by {@link PropertyKey}
   */
  public Map<PropertyKey, Property<?>> getAllProperties() {
    return Collections.unmodifiableMap(properties);
  }

  @SuppressWarnings("unchecked")
  private <T> void updateValue(Property<?> property, Object value) {
    ((Property<T>) property).updateValue((T) value);
  }
}
