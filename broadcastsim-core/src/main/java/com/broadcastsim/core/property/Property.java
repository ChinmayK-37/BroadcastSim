package com.broadcastsim.core.property;

import com.broadcastsim.core.common.enums.PropertyAccess;
import java.util.Objects;
import lombok.Getter;

/**
 * Holds the mutable value of a property together with its immutable definition.
 *
 * @param <T> the Java type of the property's value
 */
@Getter
public final class Property<T> {

  private final PropertyDefinition<T> definition;
  private T value;

  private Property(PropertyDefinition<T> definition, T value) {
    this.definition = definition;
    this.value = value;
  }

  /**
   * Creates a property initialized with the default value from its definition.
   *
   * @param definition property metadata
   * @param <T> the Java type of the property's value
   * @return a property initialized to its default value
   */
  public static <T> Property<T> fromDefinition(PropertyDefinition<T> definition) {
    Objects.requireNonNull(definition, "definition must not be null");
    return new Property<>(definition, definition.getDefaultValue());
  }

  /**
   * Updates the property value after enforcing its definition constraints.
   *
   * @param value the new property value
   * @throws IllegalStateException if the property is read-only
   * @throws IllegalArgumentException if the value does not satisfy the definition
   */
  public void updateValue(T value) {
    if (definition.getAccess() == PropertyAccess.READ_ONLY) {
      throw new IllegalStateException("property is read-only");
    }

    validateValue(value);
    this.value = value;
  }

  private void validateValue(T candidate) {
    if (candidate == null) {
      throw new IllegalArgumentException("property value must not be null");
    }
    if (!definition.getValueClass().isInstance(candidate)) {
      throw new IllegalArgumentException("property value has an invalid type");
    }
    validateMinimum(candidate);
    validateMaximum(candidate);
  }

  private void validateMinimum(T candidate) {
    if (definition.getMinimum() != null && compare(candidate, definition.getMinimum()) < 0) {
      throw new IllegalArgumentException("property value is below its minimum");
    }
  }

  private void validateMaximum(T candidate) {
    if (definition.getMaximum() != null && compare(candidate, definition.getMaximum()) > 0) {
      throw new IllegalArgumentException("property value exceeds its maximum");
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private int compare(T candidate, T boundary) {
    if (!(candidate instanceof Comparable comparable)) {
      throw new IllegalArgumentException("property value does not support bounds");
    }
    return comparable.compareTo(boundary);
  }
}
