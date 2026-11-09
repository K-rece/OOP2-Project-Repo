package com.example;

import java.net.URL;

import com.example.bluej.ClassAdapter;
import com.example.bluej.CompileAdapter;
import com.example.bluej.InvocationAdapter;
import com.example.bluej.PackageAdapter;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;

public class TelemetryExtension extends Extension {

  @Override
  public void startup(BlueJ blueJ) {
    TelemetryCollector collector = new TelemetryCollector();
    EventStore store = new EventStore();
    collector.subscribe(store);
    TelemetryEventFactory factory = new TelemetryEventFactory();

    blueJ.addCompileListener(new CompileAdapter(collector, factory));
    blueJ.addPackageListener(new PackageAdapter(collector, factory));
    blueJ.addClassListener(new ClassAdapter(collector, factory));
    blueJ.addInvocationFinishedListener(new InvocationAdapter(collector, factory));
  }

  @Override
  public boolean isCompatible() {
    return getExtensionsAPIVersionMajor() >= 3;
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getName() {
    return "BlueJ Telemetry";
  }

  @Override
  public String getDescription() {
    return "Collects coding telemetry and exports JSON/PDF";
  }

  @Override
  public URL getURL() {
    return null;
  }
}