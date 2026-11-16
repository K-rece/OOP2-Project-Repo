package com.example.export;

import java.util.List;
import com.example.TelemetryEvent;

public class RecordedSession {
    
  private final SessionInfo info;
  private final List<TelemetryEvent> events;

  public RecordedSession(SessionInfo info, List<TelemetryEvent> events) {
    this.info = info;
    this.events = List.copyOf(events);
  }

  public SessionInfo getInfo() { return info; }
  public List<TelemetryEvent> getEvents() { return events; }
}

