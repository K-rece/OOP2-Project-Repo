package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


  /** @author Justin Craigwell */
class TelemetryCollectorTest {
    
  private final TelemetryCollector collector = new TelemetryCollector();
  private final TelemetryEventFactory factory = new TelemetryEventFactory(() -> 1000L);

  @Test
  void everySubscriberReceivesTheEvent() {
    List<TelemetryEvent> first = new ArrayList<>();
    List<TelemetryEvent> second = new ArrayList<>();
    collector.subscribe(first::add);
    collector.subscribe(second::add);

    TelemetryEvent event = factory.compileSuccess();
    collector.publish(event);

    assertEquals(List.of(event), first);
    assertEquals(List.of(event), second);
  }

  @Test
  void unsubscribedObserverStopsReceiving() {
    List<TelemetryEvent> received = new ArrayList<>();
    TelemetryObserver observer = received::add;
    collector.subscribe(observer);
    collector.publish(factory.compileSuccess());

    collector.unsubscribe(observer);
    collector.publish(factory.compileSuccess());

    assertEquals(1, received.size());
  }

  @Test
  void publishingWithNoSubscribersDoesNothing() {
    collector.publish(factory.compileSuccess()); // must not throw
  }
}