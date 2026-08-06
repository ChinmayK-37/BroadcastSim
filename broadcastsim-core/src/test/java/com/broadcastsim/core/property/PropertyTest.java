package com.broadcastsim.core.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.broadcastsim.core.common.enums.PropertyAccess;
import com.broadcastsim.core.common.enums.PropertyCategory;
import com.broadcastsim.core.common.enums.PropertyKey;
import com.broadcastsim.core.common.enums.PropertyUnit;
import com.broadcastsim.core.common.enums.ValueType;
import org.junit.jupiter.api.Test;

/** Verifies property validation and registry behavior. */
class PropertyTest {

  @Test
  void updatesAReadWritePropertyWithinItsConfiguredBounds() {
    Property<Double> property = Property.fromDefinition(fpsDefinition(PropertyAccess.READ_WRITE));

    property.updateValue(60.0);

    assertEquals(60.0, property.getValue());
  }

  @Test
  void rejectsValuesOutsideConfiguredBounds() {
    Property<Double> property = Property.fromDefinition(fpsDefinition(PropertyAccess.READ_WRITE));

    assertThrows(IllegalArgumentException.class, () -> property.updateValue(0.0));
    assertThrows(IllegalArgumentException.class, () -> property.updateValue(241.0));
  }

  @Test
  void rejectsUpdatesToReadOnlyProperties() {
    Property<Double> property = Property.fromDefinition(fpsDefinition(PropertyAccess.READ_ONLY));

    assertThrows(IllegalStateException.class, () -> property.updateValue(60.0));
  }

  @Test
  void registersAndFindsDefinitionsByKey() {
    PropertyRegistry registry = new PropertyRegistry();
    PropertyDefinition<Double> definition = fpsDefinition(PropertyAccess.READ_WRITE);

    registry.register(definition);

    assertTrue(registry.contains(PropertyKey.FPS));
    assertEquals(definition, registry.find(PropertyKey.FPS).orElseThrow());
    assertFalse(registry.find(PropertyKey.BITRATE).isPresent());
  }

  @Test
  void rejectsDuplicatePropertyKeys() {
    PropertyRegistry registry = new PropertyRegistry();
    registry.register(fpsDefinition(PropertyAccess.READ_WRITE));

    assertThrows(
        IllegalArgumentException.class,
        () -> registry.register(fpsDefinition(PropertyAccess.READ_WRITE)));
  }

  private PropertyDefinition<Double> fpsDefinition(PropertyAccess access) {
    return PropertyDefinition.<Double>builder()
        .key(PropertyKey.FPS)
        .displayName("FPS")
        .valueClass(Double.class)
        .valueType(ValueType.DOUBLE)
        .category(PropertyCategory.CONFIGURATION)
        .unit(PropertyUnit.FRAMES_PER_SECOND)
        .access(access)
        .defaultValue(30.0)
        .minimum(1.0)
        .maximum(240.0)
        .warningThreshold(120.0)
        .criticalThreshold(180.0)
        .description("Frames per second")
        .build();
  }
}
