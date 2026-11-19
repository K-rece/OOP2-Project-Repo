package com.example;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Receives every telemetry event and passes it on to all subscribed {@link TelemetryObserver}s. The BlueJ adapters publish here; the session recorder and
 * activity monitor listen here. Neither side knows about the other.
 */

public class TelemetryCollector {

  private final List<TelemetryObserver> observers = new CopyOnWriteArrayList<>();

  /** Creates a collector with no observers. */
  public TelemetryCollector() {
  }

  /**
   * Registers an observer to receive every future event.
   *
   * @param o the observer to add
   */
  public void subscribe(TelemetryObserver o) {
    observers.add(o);
  }

  /**
   * Stops an observer from receiving events.
   *
   * @param o the observer to remove
   */
  public void unsubscribe(TelemetryObserver o) {
    observers.remove(o);
  }

  /**
   * Sends an event to every subscribed observer
   *
   * @param e the event to publish
   */
  public void publish(TelemetryEvent e) {
    for (TelemetryObserver o : observers)
      o.onEvent((e));
  }
}
