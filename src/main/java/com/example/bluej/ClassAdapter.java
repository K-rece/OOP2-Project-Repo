package com.example.bluej;

import java.util.HashSet;
import java.util.Set;
import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;
import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;

/**
 * Adapter: converts BlueJ's class events into the plugin's class events. The first time a class is
 * seen it is reported as {@link com.example.EventType#CLASS_CREATED}; after that, changes are
 * {@link com.example.EventType#CLASS_MODIFIED}, and removal is {@link
 * com.example.EventType#CLASS_DELETED}.
 */
public class ClassAdapter implements ClassListener {
  
  /** This is where the converted events are published. */
  private final TelemetryCollector collector;
  /** Creates the plugin's events. */
  private final TelemetryEventFactory factory;
  /** Names of the classes already seen, used to tell a new class from a changed one. */
  private final Set<String> seenClasses = new HashSet<>();

  /**
   * Creates the adapter.
   *
   * @param collector where class events are published
   * @param factory creates the events
   */
  public ClassAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  /**
   * Called by BlueJ when a class's compile state changes (created, edited or compiled). Publishes
   * CLASS_CREATED the first time a class is seen, otherwise CLASS_MODIFIED.
   *
   * @param event BlueJ's description of the change
   */
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

  /**
   * Called by BlueJ when a class is renamed. Publishes CLASS_MODIFIED.
   *
   * @param event BlueJ's description of the change
   */
  @Override
  public void classNameChanged(ClassEvent event) {
    try {
      collector.publish(factory.classModified(event.getBClass().getName()));
    } catch (Exception e) {
      System.out.println("Telemetry: class event error: " + e.getMessage());
    }
  }

  /**
   * Called by BlueJ when a class is removed. Publishes CLASS_DELETED.
   *
   * @param event BlueJ's description of the change
   */
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