package com.example.export;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.example.TelemetryEvent;

/**
 * @author Ubayd Razack
 * {@link #export(SessionInfo, List, File)} fixes the steps for saving a report
 * (work out the file name, then write the file), and each subclass supplies only the
 * format-specific parts: {@link #extension()} and {@link #write(SessionInfo, List, File)}.
 */
public abstract class ReportExporter {

  /** Formats the session start time for file names */
  private final DateTimeFormatter stamp;

  /**
   * Creates an exporter whose file names use times in the given time zone.
   *
   * @param zone the time zone for the time in file names
   */
  protected ReportExporter(ZoneId zone) {
    this.stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(zone);
  }

  /**
   * The template method: saves the session's report into a folder. It is {@code final} so
   * subclasses cannot change the order of the steps.
   *
   * @param session the session's details
   * @param events the session's events
   * @param folder the folder to save into (the BlueJ project's folder)
   * @return the file that was written
   * @throws IOException if the file cannot be written
   */
  public final File export(SessionInfo session, List<TelemetryEvent> events, File folder)
      throws IOException {
    File file = new File(folder, fileName(session));
    write(session, events, file);
    return file;
  }

  /**
   * Builds the file name from the project name and session start time, so each session gets its own files.
   *
   * @param session the session's details
   * @return the file name
   */
  String fileName(SessionInfo session) {
    return safeName(session.getProjectName()) + "_"
        + stamp.format(Instant.ofEpochMilli(session.getStartMillis()))
        + "_telemetry." + extension();
  }

  /**
   * Replaces characters that are not safe in file names with {@code _}.
   *
   * @param name the project name
   * @return a safe version of the name, or {@code project} if it is empty
   */
  static String safeName(String name) {
    String safe = name == null ? "" : name.replaceAll("[^A-Za-z0-9_-]", "_");
    return safe.isEmpty() ? "project" : safe;
  }

  /**
   * Step for subclasses: the file extension for this format.
   *
   * @return the extension without a dot
   */
  protected abstract String extension();

  /**
   * Step for subclasses: writes the report in this format.
   *
   * @param session the session's details
   * @param events the session's events
   * @param file the file to write
   * @throws IOException if the file cannot be written
   */
  protected abstract void write(SessionInfo session, List<TelemetryEvent> events, File file)
      throws IOException;
}

