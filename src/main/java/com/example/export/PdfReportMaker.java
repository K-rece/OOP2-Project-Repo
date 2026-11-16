package com.example.export;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import com.example.EventType;
import com.example.TelemetryEvent;


public class PdfReportMaker {

  private static final PDFont BOLD = PDType1Font.TIMES_BOLD;
  private static final PDFont REGULAR = PDType1Font.TIMES_ROMAN;
  private static final float MARGIN = 50;
  private static final float ROW = 18;
  private static final float BAR_HEIGHT = 10;
  private static final float PAD = 5;


  private final DateTimeFormatter dateTime;
  private final DateTimeFormatter timeOnly;

  public PdfReportMaker() {
    this(ZoneId.systemDefault());
  }

  public PdfReportMaker(ZoneId zone) {
    this.dateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).withZone(zone);
    this.timeOnly = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH).withZone(zone);
  }

  public void write(ReportData data, File file) throws IOException {
    try (PDDocument document = new PDDocument()) {
      Canvas canvas = new Canvas(document);
      try {
        writeHeader(canvas, data);
        writeSummary(canvas, data);
        writeActivity(canvas, data);
        writeErrors(canvas, data);
        writeTimeline(canvas, data);
      } finally {
        canvas.close();
      }
      document.save(file);
    }
  }

  private void writeHeader(Canvas c, ReportData data) throws IOException {
    SessionInfo s = data.getSession();
    c.text(BOLD, 20, MARGIN, "BlueJ Telemetry Report");
    c.down(ROW * 1.8f);
    labelValue(c, "Project", s.getProjectName());
    labelValue(c, "Student ID", s.getStudentId());
    labelValue(c, "Session ID", s.getSessionId());
    labelValue(c, "Started", dateTime.format(Instant.ofEpochMilli(s.getStartMillis())));
    labelValue(c, "Ended", dateTime.format(Instant.ofEpochMilli(s.getEndMillis())));
    labelValue(c, "Session Duration", formatDuration(s.getDurationMillis()));
  }

  private void writeSummary(Canvas c, ReportData data) throws IOException {
    SessionStatistics st = data.getStatistics();
    heading(c, "Session Summary");
    labelValue(c, "Total events recorded", String.valueOf(st.getEventCount()));
    labelValue(c, "Compilation attempts", String.valueOf(st.getCompilationAttempts()));
    labelValue(c, "Successful compilations", String.valueOf(st.getSuccessfulCompilations()));
    labelValue(c, "Failed compilations", String.valueOf(st.getFailedCompilations()));
    labelValue(c, "Compile success rate", String.format(Locale.ENGLISH, "%.1f%%", data.getCompileSuccessRate()));
    labelValue(c, "Average time between compiles",
        String.format(Locale.ENGLISH, "%.1f s", st.getAverageSecondsBetweenCompiles()));
  }

  private void writeActivity(Canvas c, ReportData data) throws IOException {
    heading(c, "Active Coding Time vs Idle Time");
    long total = data.getSession().getDurationMillis();
    if (total == 0) {
      c.text(REGULAR, 11, MARGIN, "No session time recorded.");
      c.down(ROW);
      return;
    }
    long active = data.getActiveMillis();
    long idle = data.getIdleMillis();
    labelValue(c, "Active", formatDuration(active) + "  (" + percent(active, total) + ")");
    labelValue(c, "Idle", formatDuration(idle) + "  (" + percent(idle, total) + ")");

    float width = c.contentWidth();
    float activeWidth = width * active / total;
    c.ensureSpace(ROW);
    c.bar(MARGIN, activeWidth, 0.30f, 0.65f, 0.35f);
    c.bar(MARGIN + activeWidth, width - activeWidth, 0.75f, 0.75f, 0.75f);
    c.down(ROW);
  }

  private void writeErrors(Canvas c, ReportData data) throws IOException {
    heading(c, "Compilation Errors by Frequency");
    Map<String, Integer> errors = data.getErrorFrequency();
    if (errors.isEmpty()) {
      c.text(REGULAR, 11, MARGIN, "No compilation errors recorded.");
      c.down(ROW);
      return;
    }
    float right = MARGIN + c.contentWidth();
    float countX = MARGIN + c.contentWidth() * 0.62f;
    float barX = countX + 50;
    float[] cols = {MARGIN, countX, barX};
    float maxBar = right - barX - 2 * PAD;
    int highest = errors.values().iterator().next();

    tableHeader(c, cols, right, "Error message", "Count", "Frequency");
    for (Map.Entry<String, Integer> entry : errors.entrySet()) {
      c.ensureSpace(ROW);
      c.tableRow(cols, right);
      c.text(REGULAR, 10, cols[0] + PAD, fit(entry.getKey(), REGULAR, 10, countX - MARGIN - 2 * PAD));
      c.text(REGULAR, 10, cols[1] + PAD, String.valueOf(entry.getValue()));
      c.bar(barX + PAD, maxBar * entry.getValue() / highest, 0.85f, 0.35f, 0.30f);
      c.down(ROW);
    }
  }

  private void writeTimeline(Canvas c, ReportData data) throws IOException {
    heading(c, "Class Creation and Method Definition Timeline");
    if (data.getClassTimeline().isEmpty()) {
      c.text(REGULAR, 11, MARGIN, "No class changes recorded.");
      c.down(ROW);
      return;
    }
    float right = MARGIN + c.contentWidth();
    float[] cols = {MARGIN, MARGIN + 80, MARGIN + 170};

    tableHeader(c, cols, right, "Time", "Event", "Class");
    for (TelemetryEvent event : data.getClassTimeline()) {
      c.ensureSpace(ROW);
      c.tableRow(cols, right);

      String action;
      String name = String.valueOf(event.getData().get("className"));

      if (event.getEventType() == EventType.METHOD_ADDED) {
        action = "Method added";
        name = name + "." + event.getData().get("method");
      } else if (event.getEventType() == EventType.CLASS_CREATED) {
        action = "Class created";
      } else {
        action = "Class deleted";
      }

      c.text(REGULAR, 10, cols[0] + PAD, timeOnly.format(Instant.ofEpochMilli(event.getTimestamp())));
      c.text(REGULAR, 10, cols[1] + PAD, action);
      c.text(REGULAR, 10, cols[2] + PAD, fit(name, REGULAR, 10, right - cols[2] - 2 * PAD));
      c.down(ROW);
    }
  }

  private void heading(Canvas c, String title) throws IOException {
    c.down(ROW);
    c.ensureSpace(ROW * 3);
    c.text(BOLD, 14, MARGIN, title);
    c.down(ROW * 1.3f);
  }

  private void labelValue(Canvas c, String label, String value) throws IOException {
    c.ensureSpace(ROW);
    c.text(BOLD, 11, MARGIN, label + ":");
    c.text(REGULAR, 11, MARGIN + 190, fit(value, REGULAR, 11, c.contentWidth() - 190));
    c.down(ROW);
  }

  private void tableHeader(Canvas c, float[] cols, float right, String... titles) throws IOException {
    c.ensureSpace(ROW * 2);
    c.tableRow(cols, right);
    for (int i = 0; i < titles.length; i++) {
      c.text(BOLD, 10, cols[i] + PAD, titles[i]);
    }
    c.down(ROW);
  }

  static String formatDuration(long millis) {
    long seconds = millis / 1000;
    long h = seconds / 3600;
    long m = (seconds % 3600) / 60;
    long s = seconds % 60;
    return h > 0
        ? String.format(Locale.ENGLISH, "%dh %02dm %02ds", h, m, s)
        : String.format(Locale.ENGLISH, "%dm %02ds", m, s);
  }

  private static String percent(long part, long total) {
    return String.format(Locale.ENGLISH, "%.1f%%", 100.0 * part / total);
  }

  private static String fit(String text, PDFont font, float size, float maxWidth) throws IOException {
    String safe = clean(text);
    if (width(safe, font, size) <= maxWidth) {
      return safe;
    }
    while (!safe.isEmpty() && width(safe + "...", font, size) > maxWidth) {
      safe = safe.substring(0, safe.length() - 1);
    }
    return safe + "...";
  }

  private static float width(String text, PDFont font, float size) throws IOException {
    return font.getStringWidth(text) / 1000 * size;
  }

  static String clean(String text) {
    StringBuilder sb = new StringBuilder();
    for (char ch : text.toCharArray()) {
      if (ch >= 32 && ch <= 126) {
        sb.append(ch);
      } else if (Character.isWhitespace(ch)) {
        sb.append(' ');
      } else {
        sb.append('?');
      }
    }
    return sb.toString();
  }

  private static class Canvas {
    private final PDDocument document;
    private PDPageContentStream stream;
    private float y;
    private float pageWidth;

    Canvas(PDDocument document) throws IOException {
      this.document = document;
      newPage();
    }

    private void newPage() throws IOException {
      if (stream != null) {
        stream.close();
      }
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);
      stream = new PDPageContentStream(document, page);
      pageWidth = page.getMediaBox().getWidth();
      y = page.getMediaBox().getHeight() - MARGIN;
    }

    float contentWidth() {
      return pageWidth - 2 * MARGIN;
    }

    void ensureSpace(float needed) throws IOException {
      if (y - needed < MARGIN) {
        newPage();
      }
    }

    void down(float amount) throws IOException {
      y -= amount;
      if (y < MARGIN) {
        newPage();
      }
    }

    void text(PDFont font, float size, float x, String text) throws IOException {
      stream.beginText();
      stream.setFont(font, size);
      stream.newLineAtOffset(x, y);
      stream.showText(clean(text));
      stream.endText();
    }

    void tableRow(float[] columnStarts, float tableRight) throws IOException {
      float bottom = y - 5;
      stream.setStrokingColor(0.4f, 0.4f, 0.4f);
      stream.setLineWidth(0.6f);
      stream.addRect(columnStarts[0], bottom, tableRight - columnStarts[0], ROW);
      for (int i = 1; i < columnStarts.length; i++) {
        stream.moveTo(columnStarts[i], bottom);
        stream.lineTo(columnStarts[i], bottom + ROW);
      }
      stream.stroke();
    }

    void bar(float x, float width, float r, float g, float b) throws IOException {
      if (width <= 0) {
        return;
      }
      stream.setNonStrokingColor(r, g, b);
      stream.addRect(x, y - 3, width, BAR_HEIGHT + 2);
      stream.fill();
      stream.setNonStrokingColor(0f, 0f, 0f);
    }

    void close() throws IOException {
      stream.close();
    }
  }
}

