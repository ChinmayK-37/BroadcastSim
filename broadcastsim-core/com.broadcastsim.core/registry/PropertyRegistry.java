package com.broadcastsim.core.property;

import com.broadcastsim.core.common.enums.PropertyKey;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Maintains reusable property definitions for O(1) lookup by property key. */
public final class PropertyRegistry {

  private final Map<PropertyKey, PropertyDefinition<?>> definitions =
      new EnumMap<>(PropertyKey.class);

  /**
   * Registers a property definition under its domain key.
   *
   * @param definition the definition to register
   * @throws IllegalArgumentException if another definition already uses the same key
   */
  public void register(PropertyDefinition<?> definition) {
    Objects.requireNonNull(definition, "definition must not be null");
    if (definitions.putIfAbsent(definition.getKey(), definition) != null) {
      throw new IllegalArgumentException("property definition key is already registered");
    }
  }

  /**
   * Finds a registered property definition by its domain key.
   *
   * @param key the property key
   * @return the registered definition, if present
   */
  public Optional<PropertyDefinition<?>> find(PropertyKey key) {
    return Optional.ofNullable(
        definitions.get(Objects.requireNonNull(key, "key must not be null")));
  }

  /**
   * Returns whether a property is registered for the supplied key.
   *
   * @param key the property key
   * @return {@code true} when the key is registered
   */
  public boolean contains(PropertyKey key) {
    return definitions.containsKey(Objects.requireNonNull(key, "key must not be null"));
  }

  /**
   * Returns an immutable view of all registered property definitions.
   *
   * @return the registered definitions keyed by {@link PropertyKey}
   */
  public Map<PropertyKey, PropertyDefinition<?>> getAll() {
    return Collections.unmodifiableMap(definitions);
  }
}
