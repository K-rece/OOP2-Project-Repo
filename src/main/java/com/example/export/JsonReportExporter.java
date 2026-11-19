package com.example.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.List;
import com.example.TelemetryEvent;

/**
 * Saves a session as a {@code .json} file, using any {@link EventSerialiser} to produce the text.
 */
public class JsonReportExporter extends ReportExporter {
  
  /** Produces the file's text */
  private final EventSerialiser serialiser;

  /**
   * Creates an exporter that names files using the computer's own time zone
   *
   * @param serialiser produces the file's text
   */
  public JsonReportExporter(EventSerialiser serialiser) {
    this(serialiser, ZoneId.systemDefault());
  }

  /**
   * Creates an exporter that names files using the given time zone.
   *
   * @param serialiser produces the file's text
   * @param zone the time zone for the time in file names
   */
  public JsonReportExporter(EventSerialiser serialiser, ZoneId zone) {
    super(zone);
    this.serialiser = serialiser;
  }

  /**
   * Returns {@code json}.
   *
   * @return {@code json}
   */
  @Override
  protected String extension() {
    return "json";
  }

  /**
   * Writes the serialised session to the file as UTF-8 text
   *
   * @param session the session's details
   * @param events the session's events
   * @param file the file to write
   * @throws IOException if the file cannot be written
   */
  @Override
  protected void write(SessionInfo session, List<TelemetryEvent> events, File file) throws IOException {
    Files.writeString(file.toPath(), serialiser.serialise(session, events), StandardCharsets.UTF_8);
  }
}
