package com.example;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventStore implements TelemetryObserver {
  private final List<TelemetryEvent> events = new CopyOnWriteArrayList<>();

  @Override
  public void onEvent(TelemetryEvent e) {
    events.add(e);
  }

  public List<TelemetryEvent> getEvents() {
    return List.copyOf(events);
  }
}
