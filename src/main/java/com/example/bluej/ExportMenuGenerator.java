package com.example.bluej;

import com.example.export.ExportService;
import com.example.export.SessionRecorder;
import bluej.extensions2.BPackage;
import bluej.extensions2.ExtensionException;
import bluej.extensions2.MenuGenerator;
import javafx.scene.control.MenuItem;

/**
 * Adds the option/button "Export Telemetry Report" to BlueJ's Tools menu, so the student can save the JSON and PDF
 * for the session so far at any time.
 */
public class ExportMenuGenerator extends MenuGenerator {
  
  /** This holds the events of the current session. */
  private final SessionRecorder recorder;
  /** Writes the JSON and PDF files. */
  private final ExportService exportService;

  /**
   * Creates the menu generator.
   *
   * @param recorder holds the current session's events
   * @param exportService writes the report files
   */
  public ExportMenuGenerator(SessionRecorder recorder, ExportService exportService) {
    this.recorder = recorder;
    this.exportService = exportService;
  }

  /**
   * Called by BlueJ to build the Tools menu for an open project.
   *
   * @param bPackage the package whose window the menu belongs to
   * @return the "Export Telemetry Report" menu item
   */
  @Override
  public MenuItem getToolsMenuItem(BPackage bPackage) {
    MenuItem item = new MenuItem("Export Telemetry Report");
    item.setOnAction(event -> exportCurrentSession(bPackage));
    return item;
  }

  /**
   * Saves the session so far into the project's folder without ending it.
   * 
   * @param bPackage the package whose project folder the files are saved in
   */
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