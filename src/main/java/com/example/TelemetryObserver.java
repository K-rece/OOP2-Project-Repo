package com.example;

public interface TelemetryObserver {
  /**
   * Called by the collector each time an event is published.
   *
   * @param event the event that was just recorded
   */
  void onEvent(TelemetryEvent event);
}
