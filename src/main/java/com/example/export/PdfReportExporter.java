package com.example.export;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import com.example.TelemetryEvent;

/** Saves a session as a {@code .pdf} report, using {@link PdfReportMaker}. */
public class PdfReportExporter extends ReportExporter {
  
  /** makes the pdf. */
  private final PdfReportMaker maker;

  /**
   * Creates an exporter that names files using the computer's own time zone
   *
   * @param maker draws the PDF
   */
  public PdfReportExporter(PdfReportMaker maker) {
    this(maker, ZoneId.systemDefault());
  }

  /**
   * Creates an exporter that names files using the given time zone.
   *
   * @param maker make the pdf
   * @param zone the time zone for the time in file names
   */
  public PdfReportExporter(PdfReportMaker maker, ZoneId zone) {
    super(zone);
    this.maker = maker;
  }

  /**
   * Returns {@code pdf}.
   *
   * @return {@code pdf}
   */
  @Override
  protected String extension() {
    return "pdf";
  }

  /**
   * Calculates the report values and draws them as a pdf
   *
   * @param session the session's details
   * @param events the session's events
   * @param file the file to write
   * @throws IOException if the file cannot be written
   */
  @Override
  protected void write(SessionInfo session, List<TelemetryEvent> events, File file) throws IOException {
    maker.write(new ReportData(session, events), file);
  }
}

