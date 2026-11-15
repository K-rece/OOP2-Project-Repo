package com.example;

import java.net.URL;
import java.util.List;

import com.example.bluej.ClassAdapter;
import com.example.bluej.CompileAdapter;
import com.example.bluej.ExportMenuGenerator;
import com.example.bluej.ExportPackageListener;
import com.example.bluej.InvocationAdapter;
import com.example.bluej.PackageAdapter;
import com.example.export.ExportService;
import com.example.export.JsonEventSerialiser;
import com.example.export.JsonReportExporter;
import com.example.export.PdfReportExporter;
import com.example.export.PdfReportMaker;
import com.example.export.SessionRecorder;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;

public class TelemetryExtension extends Extension {

  @Override
  public void startup(BlueJ blueJ) {
    TelemetryCollector collector = new TelemetryCollector();
    EventStore store = new EventStore();
    collector.subscribe(store);
    TelemetryEventFactory factory = new TelemetryEventFactory();

    SessionRecorder recorder = new SessionRecorder();
    collector.subscribe(recorder);
    ExportService exportService = new ExportService(List.of(
        new JsonReportExporter(new JsonEventSerialiser()),
        new PdfReportExporter(new PdfReportMaker())));

    blueJ.addCompileListener(new CompileAdapter(collector, factory));
    blueJ.addPackageListener(new PackageAdapter(collector, factory));
    blueJ.addPackageListener(new ExportPackageListener(recorder, exportService));
    blueJ.addClassListener(new ClassAdapter(collector, factory));
    blueJ.addInvocationFinishedListener(new InvocationAdapter(collector, factory));
    blueJ.setMenuGenerator(new ExportMenuGenerator(recorder, exportService));
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