package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;


/**
 * Turns snapshots of the open editors into {@link EventType#EDIT}, {@link EventType#IDLE_START}
 * and {@link EventType#IDLE_END} events.
 *
 */
public class ActivityMonitor implements TelemetryObserver {

  /** How long without any activity before the student counts as idle: 60 seconds. */
  public static final long DEFAULT_IDLE_THRESHOLD_MILLIS = 60_000;

  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;
  private final LongSupplier clock;
  private final long idleThresholdMillis;
  private final Map<String, Integer> lastLengths = new HashMap<>();
  private long lastActivityMillis;
  private boolean idle;

  /**
   * Creates a monitor using the system clock and the default 60-second idle threshold.
   *
   * @param collector where edit and idle events are published
   * @param factory creates the events
   */
  public ActivityMonitor(TelemetryCollector collector, TelemetryEventFactory factory) {
    this(collector, factory, System::currentTimeMillis, DEFAULT_IDLE_THRESHOLD_MILLIS);
  }

  /**
   * Creates a monitor with a given clock and idle threshold (used by tests).
   *
   * @param collector where edit and idle events are published
   * @param factory creates the events
   * @param clock supplies the current time in milliseconds
   * @param idleThresholdMillis milliseconds without activity before the student counts as idle
   */
  public ActivityMonitor(TelemetryCollector collector, TelemetryEventFactory factory,
      LongSupplier clock, long idleThresholdMillis) {
    this.collector = collector;
    this.factory = factory;
    this.clock = clock;
    this.idleThresholdMillis = idleThresholdMillis;
    this.lastActivityMillis = clock.getAsLong();
  }

  /**
   * Records any event other than an idle event as activity, ending an idle period if one is open.
   *
   * @param event the event that was just published
   */
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

  /**
   * Checks the open editors for changes and for idleness. Publishes an {@link EventType#EDIT} for
   * each editor whose length changed since the last poll, and an {@link EventType#IDLE_START} once the idle threshold has passed. 
   * An editor seen for the first time only sets a starting point.
   *
   * @param textLengths each open editor's current text length, by class name
   */
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