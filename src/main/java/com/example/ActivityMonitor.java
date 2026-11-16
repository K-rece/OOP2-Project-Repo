package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;


public class ActivityMonitor implements TelemetryObserver {

  public static final long DEFAULT_IDLE_THRESHOLD_MILLIS = 60_000;

  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;
  private final LongSupplier clock;
  private final long idleThresholdMillis;
  private final Map<String, Integer> lastLengths = new HashMap<>();
  private long lastActivityMillis;
  private boolean idle;

  public ActivityMonitor(TelemetryCollector collector, TelemetryEventFactory factory) {
    this(collector, factory, System::currentTimeMillis, DEFAULT_IDLE_THRESHOLD_MILLIS);
  }

  public ActivityMonitor(TelemetryCollector collector, TelemetryEventFactory factory,
      LongSupplier clock, long idleThresholdMillis) {
    this.collector = collector;
    this.factory = factory;
    this.clock = clock;
    this.idleThresholdMillis = idleThresholdMillis;
    this.lastActivityMillis = clock.getAsLong();
  }

  @Override
  public synchronized void onEvent(TelemetryEvent event) {
    EventType type = event.getEventType();
    if (type == EventType.IDLE_START || type == EventType.IDLE_END) {
      return;
    }
    lastActivityMillis = clock.getAsLong();
    if (idle) {
      idle = false;
      collector.publish(factory.idleEnded());
    }
  }

  public synchronized void poll(Map<String, Integer> textLengths) {
    for (Map.Entry<String, Integer> entry : textLengths.entrySet()) {
      Integer previous = lastLengths.put(entry.getKey(), entry.getValue());
      if (previous != null && !previous.equals(entry.getValue())) {
        collector.publish(factory.edit(entry.getKey(), entry.getValue() - previous));
      }
    }
    lastLengths.keySet().retainAll(textLengths.keySet());

    if (!idle && clock.getAsLong() - lastActivityMillis >= idleThresholdMillis) {
      idle = true;
      collector.publish(factory.idleStartedAt(lastActivityMillis));
    }
  }
}