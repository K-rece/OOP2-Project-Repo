package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import com.example.TelemetryEvent;
import com.example.TelemetryEventFactory;

class PdfReportMakerTest {
  private static final long T = 1_700_000_000_000L; // 22:13:20 UTC

  private final SessionInfo session = new SessionInfo("s-1", "STUDENT_ID", "Shapes", T, T + 1_800_000);
  private final PdfReportMaker writer = new PdfReportMaker(ZoneOffset.UTC);

  private TelemetryEventFactory at(long offsetMillis) {
    return new TelemetryEventFactory(() -> T + offsetMillis);
  }

  /** Writes the report to a temp file and returns its text, checking it is a readable PDF. */
  private String writeAndRead(List<TelemetryEvent> events, int[] pageCount) throws IOException {
    File file = Files.createTempFile("report", ".pdf").toFile();
    try {
      writer.write(new ReportData(session, events), file);
      try (PDDocument pdf = PDDocument.load(file)) { // throws if not a valid PDF
        pageCount[0] = pdf.getNumberOfPages();
        return new PDFTextStripper().getText(pdf);
      }
    } finally {
      file.delete();
    }
  }

  @Test
  void reportContainsAllSections() throws IOException {
    String text = writeAndRead(List.of(
        at(0).classCreated("Dog"),
        at(60_000).compileError("Dog.java", 3, "';' expected"),
        at(120_000).compileSuccess(),
        at(300_000).idleStarted(),
        at(600_000).idleEnded()), new int[1]);

    assertTrue(text.contains("BlueJ Telemetry Report"));
    assertTrue(text.contains("Shapes"));
    assertTrue(text.contains("Session Summary"));
    assertTrue(text.contains("Active Coding Time vs Idle Time"));
    assertTrue(text.contains("25m 00s")); // active: 30 min - 5 min idle
    assertTrue(text.contains("';' expected"));
    assertTrue(text.contains("22:13:20")); // Dog created at session start
    assertTrue(text.contains("Dog"));
  }

  @Test
  void emptySessionShowsPlaceholders() throws IOException {
    String text = writeAndRead(List.of(), new int[1]);

    assertTrue(text.contains("No compilation errors recorded."));
    assertTrue(text.contains("No class changes recorded."));
  }

  @Test
  void longTimelineSpillsOntoMorePages() throws IOException {
    List<TelemetryEvent> events = new ArrayList<>();
    for (int i = 0; i < 80; i++) {
      events.add(at(i * 1000L).classCreated("Class" + i));
    }
    int[] pages = new int[1];
    String text = writeAndRead(events, pages);

    assertTrue(pages[0] > 1);
    assertTrue(text.contains("Class79"));
  }

  @Test
  void unsupportedCharactersDoNotCrash() throws IOException {
    String text = writeAndRead(List.of(
        at(0).compileError("A.java", 1, "bad char → here")), new int[1]);

    assertTrue(text.contains("bad char ? here"));
  }

  @Test
  void durationFormatting() {
    assertEquals("0m 45s", PdfReportMaker.formatDuration(45_000));
    assertEquals("25m 00s", PdfReportMaker.formatDuration(1_500_000));
    assertEquals("1h 05m 30s", PdfReportMaker.formatDuration(3_930_000));
  }
}