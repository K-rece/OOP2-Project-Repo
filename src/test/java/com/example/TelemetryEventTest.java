package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

 /** @author Justin Craigwell */
class TelemetryEventTest {

  @Test
  void storesTypeTimestampAndData() {
    TelemetryEvent event = new TelemetryEvent(EventType.EDIT, 42L, Map.of("className", "Dog"));

    assertEquals(EventType.EDIT, event.getEventType());
    assertEquals(42L, event.getTimestamp());
    assertEquals(Map.of("className", "Dog"), event.getData());
  }

  @Test
  void laterChangesToTheOriginalMapDoNotAffectTheEvent() {
    Map<String, Object> data = new HashMap<>();
    data.put("className", "Dog");
    TelemetryEvent event = new TelemetryEvent(EventType.EDIT, 42L, data);

    data.put("className", "Cat");

    assertEquals("Dog", event.getData().get("className"));
  }

  @Test
  void dataCannotBeChangedThroughTheGetter() {
    TelemetryEvent event = new TelemetryEvent(EventType.EDIT, 42L, Map.of("className", "Dog"));

    assertThrows(UnsupportedOperationException.class, () -> event.getData().put("x", 1));
  }
}