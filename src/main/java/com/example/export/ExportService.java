package com.example.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportService {
    
  private final List<ReportExporter> exporters;

  public ExportService(List<ReportExporter> exporters) {
    this.exporters = List.copyOf(exporters);
  }

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

  public void exportInBackground(RecordedSession session, File folder) {
    Thread worker = new Thread(() -> exportNow(session, folder), "telemetry-export");
    worker.start();
  }
}

