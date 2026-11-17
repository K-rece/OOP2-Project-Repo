package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ActivityMonitorTest {
  private static final long T = 1_700_000_000_000L;
  private static final long THRESHOLD = 60_000;

  private final long[] now = {T}; // a clock the test can move forward
  private final TelemetryCollector collector = new TelemetryCollector();
  private final TelemetryEventFactory factory = new TelemetryEventFactory(() -> now[0]);
  private final ActivityMonitor monitor = new ActivityMonitor(collector, factory, () -> now[0], THRESHOLD);
  private final List<TelemetryEvent> published = new ArrayList<>();

  ActivityMonitorTest() {
    collector.subscribe(monitor);
    collector.subscribe(published::add);
  }

  private List<EventType> types() {
    List<EventType> types = new ArrayList<>();
    for (TelemetryEvent event : published) {
      types.add(event.getEventType());
    }
    return types;
  }

  @Test
  void firstSightOfAnEditorIsNotAnEdit() {
    monitor.poll(Map.of("Dog", 100));

    assertEquals(List.of(), published);
  }

  @Test
  void changedLengthPublishesAnEditWithTheDifference() {
    monitor.poll(Map.of("Dog", 100));
    monitor.poll(Map.of("Dog", 112));
    monitor.poll(Map.of("Dog", 105));

    assertEquals(List.of(EventType.EDIT, EventType.EDIT), types());
    assertEquals("Dog", published.get(0).getData().get("className"));
    assertEquals(12, published.get(0).getData().get("charsChanged"));
    assertEquals(-7, published.get(1).getData().get("charsChanged"));
  }

  @Test
  void unchangedLengthPublishesNothing() {
    monitor.poll(Map.of("Dog", 100));
    monitor.poll(Map.of("Dog", 100));

    assertEquals(List.of(), published);
  }

  @Test
  void goesIdleAfterTheThresholdBackdatedToLastActivity() {
    now[0] = T + THRESHOLD - 1;
    monitor.poll(Map.of());
    assertEquals(List.of(), published); // not quite long enough

    now[0] = T + THRESHOLD;
    monitor.poll(Map.of());
    monitor.poll(Map.of()); // still idle: no second IDLE_START

    assertEquals(List.of(EventType.IDLE_START), types());
    assertEquals(T, published.get(0).getTimestamp());
  }

  @Test
  void anEditEndsIdle() {
    monitor.poll(Map.of("Dog", 100));
    now[0] = T + THRESHOLD;
    monitor.poll(Map.of("Dog", 100));
    now[0] = T + THRESHOLD + 5000;
    monitor.poll(Map.of("Dog", 101));

    assertEquals(List.of(EventType.IDLE_START, EventType.IDLE_END, EventType.EDIT), types());
    assertEquals(T + THRESHOLD + 5000, published.get(1).getTimestamp());
  }

  @Test
  void otherEventsCountAsActivity() {
    now[0] = T + THRESHOLD - 1000;
    collector.publish(factory.compileSuccess()); // activity just before the threshold
    now[0] = T + THRESHOLD + 1000;
    monitor.poll(Map.of());

    assertEquals(List.of(EventType.COMPILE_SUCCESS), types()); // so: not idle yet
  }

  @Test
  void aCompileEndsIdle() {
    now[0] = T + THRESHOLD;
    monitor.poll(Map.of());
    collector.publish(factory.compileSuccess());

    assertEquals(List.of(EventType.IDLE_START, EventType.IDLE_END, EventType.COMPILE_SUCCESS), types());
  }

  @Test
  void aReopenedEditorStartsFromAFreshBaseline() {
    monitor.poll(Map.of("Dog", 100));
    monitor.poll(Map.of());           // editor closed
    monitor.poll(Map.of("Dog", 150)); // reopened with different length

    assertEquals(List.of(), published);
  }
}