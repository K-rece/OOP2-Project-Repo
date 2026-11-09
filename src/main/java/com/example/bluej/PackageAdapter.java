package com.example.bluej;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;

import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

public class PackageAdapter implements PackageListener {
  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;

  public PackageAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  @Override
  public void packageOpened(PackageEvent ev) {
    collector.publish(factory.projectOpened(projectName(ev)));
  }

  @Override
  public void packageClosing(PackageEvent ev) {
    collector.publish(factory.projectClosed(projectName(ev)));
  }

  private String projectName(PackageEvent ev) {
    try {
      return ev.getPackage().getProject().getName();
    } catch (ExtensionException e) {
      return "unknown";
    }
  }
}
