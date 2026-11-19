package com.example.bluej;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;

import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

/**
 * Adapter: converts BlueJ's package events into {@link com.example.EventType#PROJECT_OPENED} and
 * {@link com.example.EventType#PROJECT_CLOSED} events.
 */
public class PackageAdapter implements PackageListener {
  /** Where the converted events are published. */
  private final TelemetryCollector collector;
  /** Creates the plugin's events. */
  private final TelemetryEventFactory factory;

  /**
   * Creates the adapter.
   *
   * @param collector where project events are published
   * @param factory creates the events
   */
  public PackageAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  /**
   * Called by BlueJ when a project's package opens, and then it publishes PROJECT_OPENED.
   *
   * @param ev BlueJ's description of the package
   */
  @Override
  public void packageOpened(PackageEvent ev) {
    collector.publish(factory.projectOpened(projectName(ev)));
  }

  /**
   * Called by BlueJ just before a project's package closes, and then it publishes PROJECT_CLOSED.
   *
   * @param ev BlueJ's description of the package
   */
  @Override
  public void packageClosing(PackageEvent ev) {
    collector.publish(factory.projectClosed(projectName(ev)));
  }

  /**
   * Gets the project's name from a package event.
   *
   * @param ev BlueJ's description of the package
   * @return the project name, or {@code "unknown"} if BlueJ cannot provide it
   */
  private String projectName(PackageEvent ev) {
    try {
      return ev.getPackage().getProject().getName();
    } catch (ExtensionException e) {
      return "unknown";
    }
  }
}
