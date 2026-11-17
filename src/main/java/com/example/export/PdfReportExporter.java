package com.example.export;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import com.example.TelemetryEvent;

public class PdfReportExporter extends ReportExporter {
  
  private final PdfReportMaker maker;

  public PdfReportExporter(PdfReportMaker maker) {
    this(maker, ZoneId.systemDefault());
  }

  public PdfReportExporter(PdfReportMaker maker, ZoneId zone) {
    super(zone);
    this.maker = maker;
  }

  @Override
  protected String extension() {
    return "pdf";
  }

  @Override
  protected void write(SessionInfo session, List<TelemetryEvent> events, File file) throws IOException {
    maker.write(new ReportData(session, events), file);
  }
}

