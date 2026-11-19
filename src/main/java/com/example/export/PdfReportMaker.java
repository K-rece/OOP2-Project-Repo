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


/**
 * Draws a {@link ReportData} as a PDF report using Apache PDFBox {@link ReportData}.
 */
public class PdfReportMaker {

  /** Fonts and design specifications for pdf. */
  private static final PDFont BOLD = PDType1Font.TIMES_BOLD;
  private static final PDFont REGULAR = PDType1Font.TIMES_ROMAN;
  private static final float MARGIN = 50;
  private static final float ROW = 18;
  private static final float BAR_HEIGHT = 10;
  private static final float PAD = 5;


  /** Formats full dates and times */
  private final DateTimeFormatter dateTime;
  /** Formats times of day for the timeline */
  private final DateTimeFormatter timeOnly;

  /** Creates a report maker that shows times in the computer's own time zone */
  public PdfReportMaker() {
    this(ZoneId.systemDefault());
  }

  /**
   * Creates a report maker that shows times in the given time zone (tests use UTC).
   *
   * @param zone the time zone for dates and times in the report
   */
  public PdfReportMaker(ZoneId zone) {
    this.dateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).withZone(zone);
    this.timeOnly = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH).withZone(zone);
  }

  /**
   * Makes the full report and saves the pdf
   *
   * @param data the values to show
   * @param file where to save the pdf
   * @throws IOException if the file cannot be written
   */
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

  /**
   * Makes the title and the session details such as project, student ID, session ID, times, duration.
   *
   * @param c the page being drawn on
   * @param data the report values
   * @throws IOException if drawing fails
   */
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

  /**
   * Makes the session summary: event count, compile counts, success rate and average time between
   * compiles.
   *
   * @param c the page being drawn on
   * @param data the report values
   * @throws IOException if drawing fails
   */
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

  /**
   * Makes active and idle time as figures and percentages, plus a bar split green (active) and
   * grey (idle).
   *
   * @param c the page being drawn on
   * @param data the report values
   * @throws IOException if drawing fails
   */
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

  /**
   * Makes the compilation errors table
   *
   * @param c the page being drawn on
   * @param data the report values
   * @throws IOException if drawing fails
   */
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

  /**
   * Makes the progression timeline table
   *
   * @param c the page being drawn on
   * @param data the report values
   * @throws IOException if drawing fails
   */
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

  /**
   * Makes a section heading
   *
   * @param c the page being drawn on
   * @param title the heading text
   * @throws IOException if drawing fails
   */
  private void heading(Canvas c, String title) throws IOException {
    c.down(ROW);
    c.ensureSpace(ROW * 3);
    c.text(BOLD, 14, MARGIN, title);
    c.down(ROW * 1.3f);
  }

  /**
   * Makes one "label: value" line.
   *
   * @param c the page being drawn on
   * @param label the bold label
   * @param value the value, shortened with "..." if too long
   * @throws IOException if drawing fails
   */
  private void labelValue(Canvas c, String label, String value) throws IOException {
    c.ensureSpace(ROW);
    c.text(BOLD, 11, MARGIN, label + ":");
    c.text(REGULAR, 11, MARGIN + 190, fit(value, REGULAR, 11, c.contentWidth() - 190));
    c.down(ROW);
  }

  /**
   * Makes a table's header row with borders and bold titles.
   *
   * @param c the page being drawn on
   * @param cols the x position where each column starts
   * @param right the x position of the table's right edge
   * @param titles the column titles
   * @throws IOException if drawing fails
   */
  private void tableHeader(Canvas c, float[] cols, float right, String... titles) throws IOException {
    c.ensureSpace(ROW * 2);
    c.tableRow(cols, right);
    for (int i = 0; i < titles.length; i++) {
      c.text(BOLD, 10, cols[i] + PAD, titles[i]);
    }
    c.down(ROW);
  }

  /**
   * Formats the duration
   *
   * @param millis the duration in milliseconds
   * @return the formatted duration
   */
  static String formatDuration(long millis) {
    long seconds = millis / 1000;
    long h = seconds / 3600;
    long m = (seconds % 3600) / 60;
    long s = seconds % 60;
    return h > 0
        ? String.format(Locale.ENGLISH, "%dh %02dm %02ds", h, m, s)
        : String.format(Locale.ENGLISH, "%dm %02ds", m, s);
  }

  /**
   * Formats one value as a percentage of another
   *
   * @param part the part
   * @param total the whole (must not be 0)
   * @return the percentage to one decimal place
   */
  private static String percent(long part, long total) {
    return String.format(Locale.ENGLISH, "%.1f%%", 100.0 * part / total);
  }

  /**
   * Shortens text with "..." so it fits in the given width.
   *
   * @param text the text
   * @param font the font it will be drawn in
   * @param size the font size
   * @param maxWidth the available width in points
   * @return the text, shortened if needed
   * @throws IOException if the font cannot measure the text
   */
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

  /**
   * Measures how wide text will be when drawn.
   *
   * @param text the text
   * @param font the font
   * @param size the font size
   * @return the width in points
   * @throws IOException if the font cannot measure the text
   */
  private static float width(String text, PDFont font, float size) throws IOException {
    return font.getStringWidth(text) / 1000 * size;
  }

  /**
   * Replaces characters the built-in PDF fonts cannot draw: whitespace becomes a space and any
   * other non-basic character becomes {@code ?}.
   *
   * @param text the text
   * @return the text with only printable basic characters
   */
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

  /**
   * Keeps track of the current page and how far down it has been written, and starts a new page
   * automatically when there is no room left.
   */
  private static class Canvas {
    /** The PDF being built. */
    private final PDDocument document;
    /** Draws on the current page. */
    private PDPageContentStream stream;
    /** Current vertical position, measured up from the bottom of the page. */
    private float y;
    /** Width of the page, in points. */
    private float pageWidth;

    /**
     * Creates the canvas and its first page.
     *
     * @param document the PDF to draw into
     * @throws IOException if the page cannot be created
     */
    Canvas(PDDocument document) throws IOException {
      this.document = document;
      newPage();
    }

    /**
     * Finishes the current page and starts a new A4 page at the top margin.
     *
     * @throws IOException if the page cannot be created
     */
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

    /**
     * Returns the usable width between the margins, in points.
     *
     * @return the usable width between the margins, in points
     */
    float contentWidth() {
      return pageWidth - 2 * MARGIN;
    }

    /**
     * Starts a new page if there isn't enough room left on this one.
     *
     * @param needed the height needed, in points
     * @throws IOException if a new page cannot be created
     */
    void ensureSpace(float needed) throws IOException {
      if (y - needed < MARGIN) {
        newPage();
      }
    }

    /**
     * Moves down the page, starting a new page at the bottom margin.
     *
     * @param amount how far to move, in points
     * @throws IOException if a new page cannot be created
     */
    void down(float amount) throws IOException {
      y -= amount;
      if (y < MARGIN) {
        newPage();
      }
    }

    /**
     * Makes text at the current height.
     *
     * @param font the font
     * @param size the font size
     * @param x the horizontal position
     * @param text the text, cleaned of unsupported characters
     * @throws IOException if drawing fails
     */
    void text(PDFont font, float size, float x, String text) throws IOException {
      stream.beginText();
      stream.setFont(font, size);
      stream.newLineAtOffset(x, y);
      stream.showText(clean(text));
      stream.endText();
    }

    /**
     * Makes one table row's borders: an outline plus a vertical line at each column's left edge.
     *
     * @param columnStarts the x position where each column starts
     * @param tableRight the x position of the table's right edge
     * @throws IOException if drawing fails
     */
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

    /**
     * Makes a filled bar at the current height. Nothing is drawn if the width is zero or less.
     *
     * @param x the left edge
     * @param width the bar's width
     * @param r red, from 0 to 1
     * @param g green, from 0 to 1
     * @param b blue, from 0 to 1
     * @throws IOException if drawing fails
     */
    void bar(float x, float width, float r, float g, float b) throws IOException {
      if (width <= 0) {
        return;
      }
      stream.setNonStrokingColor(r, g, b);
      stream.addRect(x, y - 3, width, BAR_HEIGHT + 2);
      stream.fill();
      stream.setNonStrokingColor(0f, 0f, 0f);
    }

    /**
     * Finishes the last page.
     *
     * @throws IOException if the page cannot be finished
     */
    void close() throws IOException {
      stream.close();
    }
  }
}

