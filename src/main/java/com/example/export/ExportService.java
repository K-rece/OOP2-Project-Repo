package com.example.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 
 * @author Ubayd Razack
 * Runs every report exporter for a session. */
public class ExportService {
    
  /** The exporters to run, one per file format. */
  private final List<ReportExporter> exporters;

  /**
   * Creates the service.
   *
   * @param exporters the exporters to run; copied
   */
  public ExportService(List<ReportExporter> exporters) {
    this.exporters = List.copyOf(exporters);
  }

  /**
   * Saves the session in every format straight away and if one format fails, the error is logged and
   * the others are still written.
   *
   * @param session the session to save
   * @param folder the folder to save into
   * @return the files that were written
   */
  public List<File> exportNow(RecordedSession session, File folder) {
    List<File> written = new ArrayList<>();
    for (ReportExporter exporter : exporters) {
      try {
        written.add(exporter.export(session.getInfo(), session.getEvents(), folder));
      } catch (IOException | RuntimeException e) {
        System.err.println("Telemetry: export failed: " + e.getMessage());
      }
    }
    return written;
  }

  /**
   * Saves the session on a separate thread, so BlueJ's window does not freeze while the PDF is
   * written.
   *
   * @param session the session to save
   * @param folder the folder to save into
   */
  public void exportInBackground(RecordedSession session, File folder) {
    Thread worker = new Thread(() -> exportNow(session, folder), "telemetry-export");
    worker.start();
  }
}

