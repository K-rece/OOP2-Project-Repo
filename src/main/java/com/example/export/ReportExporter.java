package com.example.export;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.example.TelemetryEvent;

public abstract class ReportExporter {

  private final DateTimeFormatter stamp;

  protected ReportExporter(ZoneId zone) {
    this.stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(zone);
  }

  public final File export(SessionInfo session, List<TelemetryEvent> events, File folder)
      throws IOException {
    File file = new File(folder, fileName(session));
    write(session, events, file);
    return file;
  }

  String fileName(SessionInfo session) {
    return safeName(session.getProjectName()) + "_"
        + stamp.format(Instant.ofEpochMilli(session.getStartMillis()))
        + "_telemetry." + extension();
  }

  static String safeName(String name) {
    String safe = name == null ? "" : name.replaceAll("[^A-Za-z0-9_-]", "_");
    return safe.isEmpty() ? "project" : safe;
  }

  protected abstract String extension();

  protected abstract void write(SessionInfo session, List<TelemetryEvent> events, File file)
      throws IOException;
}

