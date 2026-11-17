package com.example;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TelemetryCollector {
  private final List<TelemetryObserver> observers = new CopyOnWriteArrayList<>();

  public void subscribe(TelemetryObserver o) {
    observers.add(o);
  }

  public void unsubscribe(TelemetryObserver o) {
    observers.remove(o);
  }

  public void publish(TelemetryEvent e) {
    for (TelemetryObserver o : observers)
      o.onEvent((e));
  }
}
