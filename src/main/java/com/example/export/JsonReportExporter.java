package com.example.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.List;
import com.example.TelemetryEvent;

public class JsonReportExporter extends ReportExporter {
  
  private final EventSerialiser serialiser;

  public JsonReportExporter(EventSerialiser serialiser) {
    this(serialiser, ZoneId.systemDefault());
  }

  public JsonReportExporter(EventSerialiser serialiser, ZoneId zone) {
    super(zone);
    this.serialiser = serialiser;
  }

  @Override
  protected String extension() {
    return "json";
  }

  @Override
  protected void write(SessionInfo session, List<TelemetryEvent> events, File file) throws IOException {
    Files.writeString(file.toPath(), serialiser.serialise(session, events), StandardCharsets.UTF_8);
  }
}
