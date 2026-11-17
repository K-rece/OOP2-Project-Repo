package com.example.bluej;

import com.example.export.ExportService;
import com.example.export.SessionRecorder;
import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

public class ExportPackageListener implements PackageListener {
  
  private final SessionRecorder recorder;
  private final ExportService exportService;

  public ExportPackageListener(SessionRecorder recorder, ExportService exportService) {
    this.recorder = recorder;
    this.exportService = exportService;
  }

  @Override
  public void packageOpened(PackageEvent event) {
    try {
      recorder.start(event.getPackage().getProject().getName());
    } catch (ExtensionException e) {
      System.err.println("Telemetry: could not start session: " + e.getMessage());
    }
  }

  @Override
  public void packageClosing(PackageEvent event) {
    if (!recorder.isActive()) {
      return;
    }
    try {
      exportService.exportNow(recorder.finish(), event.getPackage().getProject().getDir());
    } catch (ExtensionException e) {
      System.err.println("Telemetry: could not export session: " + e.getMessage());
    }
  }
}