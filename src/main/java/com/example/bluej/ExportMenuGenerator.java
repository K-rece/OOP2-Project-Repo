package com.example.bluej;

import com.example.export.ExportService;
import com.example.export.SessionRecorder;
import bluej.extensions2.BPackage;
import bluej.extensions2.ExtensionException;
import bluej.extensions2.MenuGenerator;
import javafx.scene.control.MenuItem;

public class ExportMenuGenerator extends MenuGenerator {
  
  private final SessionRecorder recorder;
  private final ExportService exportService;

  public ExportMenuGenerator(SessionRecorder recorder, ExportService exportService) {
    this.recorder = recorder;
    this.exportService = exportService;
  }

  @Override
  public MenuItem getToolsMenuItem(BPackage bPackage) {
    MenuItem item = new MenuItem("Export Telemetry Report");
    item.setOnAction(event -> exportCurrentSession(bPackage));
    return item;
  }

  private void exportCurrentSession(BPackage bPackage) {
    if (!recorder.isActive()) {
      System.out.println("Telemetry: no session to export yet");
      return;
    }
    try {
      exportService.exportInBackground(recorder.snapshot(), bPackage.getProject().getDir());
    } catch (ExtensionException e) {
      System.err.println("Telemetry: manual export failed: " + e.getMessage());
    }
  }
}