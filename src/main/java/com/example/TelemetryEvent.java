package com.example;

import java.util.Map;

public class TelemetryEvent {
  private final EventType type;
  private final long timestampMillis;
  private final Map<String, Object> data;

  TelemetryEvent(EventType type, long ts, Map<String, Object> data) {
    this.type = type;
    this.timestampMillis = ts;
    this.data = Map.copyOf(data);
  }

  public EventType getEventType() {
    return type;
  }

  public long getTimestamp() {
    return timestampMillis;
  }

  public Map<String, Object> getData() {
    return Map.copyOf(data);
  }
}
