package com.example;

import java.util.Map;

/**
 * @author Krece Hayes
 * One recorded student action: what happened ({@link EventType}), when, and any details. Events
 * are immutable which means that once created they cannot be changed. Create them with {@link
 * TelemetryEventFactory} rather than this constructor.
 */
public class TelemetryEvent {
  /** What kind of action this event records. */
  private final EventType type;
  /** When the action happened, in milliseconds since 1 January 1970 (UTC). */
  private final long timestampMillis;
  /** Extra details for this event type */
  private final Map<String, Object> data;

  /**
   * Creates an event. Package-private so events are only made by {@link TelemetryEventFactory}.
   *
   * @param type what kind of action this is
   * @param ts when it happened, in milliseconds since 1 January 1970 (UTC)
   * @param data details of the action; copied, so later changes to the map have no effect
   */
  TelemetryEvent(EventType type, long ts, Map<String, Object> data) {
    this.type = type;
    this.timestampMillis = ts;
    this.data = Map.copyOf(data);
  }

  /**
   * Returns the kind of action this event records.
   *
   * @return the event type
   */
  public EventType getEventType() {
    return type;
  }

  /**
   * Returns when the action happened.
   *
   * @return milliseconds since 1 January 1970 (UTC)
   */
  public long getTimestamp() {
    return timestampMillis;
  }

  /**
   * Returns the details of this event. The keys depend on the event type (see {@link EventType}).
   *
   * @return an unmodifiable copy of the event's data
   */
  public Map<String, Object> getData() {
    return Map.copyOf(data);
  }
}
