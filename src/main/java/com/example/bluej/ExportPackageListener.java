package com.example.bluej;

import com.example.export.ExportService;
import com.example.export.SessionRecorder;
import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

/**
 * @author Ubayd Razack
 * Starts a session when a project opens, and saves the session's JSON and PDF into the project's folder when it closes.
 */
public class ExportPackageListener implements PackageListener {
  
  /** This holds the events of the current session. */
  private final SessionRecorder recorder;
  /** Writes the JSON and PDF files. */
  private final ExportService exportService;

  /**
   * Creates the listener.
   *
   * @param recorder holds the current session's events
   * @param exportService writes the report files
   */
  public ExportPackageListener(SessionRecorder recorder, ExportService exportService) {
    this.recorder = recorder;
    this.exportService = exportService;
  }

  /**
   * Called by BlueJ when a project's package opens, and starts a new session for the project.
   *
   * @param event BlueJ's description of the package
   */
  @Override
  public void packageOpened(PackageEvent event) {
    try {
      recorder.start(event.getPackage().getProject().getName());
    } catch (ExtensionException e) {
      System.err.println("Telemetry: could not start session: " + e.getMessage());
    }
  }

  /**
   * Called by BlueJ just before a project's package closes, then ends the session and saves its files.
   * This runs straight away so the files are not lost if BlueJ is quitting.
   *
   * @param event BlueJ's description of the package
   */
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