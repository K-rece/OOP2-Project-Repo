package com.example.bluej;

import java.util.HashSet;
import java.util.Set;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;

import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;

public class ClassAdapter implements ClassListener {
  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;
  // names of classes we have already seen
  private final Set<String> seenClasses = new HashSet<>();

  public ClassAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  @Override
  public void classStateChanged(ClassEvent event) {
    try {
      String name = event.getBClass().getName();
      if (seenClasses.add(name)) {
        collector.publish(factory.classCreated(name));
      } else {
        collector.publish(factory.classModified(name));
      }
    } catch (Exception e) {
      System.out.println("Telemetry: class event error: " + e.getMessage());
    }
  }

  @Override
  public void classNameChanged(ClassEvent event) {
    try {
      collector.publish(factory.classModified(event.getBClass().getName()));
    } catch (Exception e) {
      System.out.println("Telemetry: class event error: " + e.getMessage());
    }
  }

  @Override
  public void classRemoved(ClassEvent event) {
    try {
      String name = event.getBClass().getName();
      seenClasses.remove(name);
      collector.publish(factory.classDeleted(name));
    } catch (Exception e) {
      System.out.println("Telemetry: class event error: " + e.getMessage());
    }
  }
}