package com.broadcastsim.core.property;

import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.ValueType;
import lombok.Builder;
import lombok.Getter;

/**
 * Immutable metadata that defines how a simulation property is represented and constrained.
 *
 * @param <T> the Java type of the property's value
 */
@Getter
@Builder
public final class PropertyDefinition<T> {

  private final PropertyKey key;
  private final String displayName;
  private final Class<T> valueClass;
  private final ValueType valueType;
  private final PropertyCategory category;
  private final PropertyUnit unit;
  private final PropertyAccess access;
  private final T defaultValue;
  private final T minimum;
  private final T maximum;
  private final T warningThreshold;
  private final T criticalThreshold;
  private final String description;
}
