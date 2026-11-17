package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.TelemetryEvent;

class ExportServiceTest {
    
  private static final long T = 1_700_000_000_000L;

  private final RecordedSession session = new RecordedSession(
      new SessionInfo("s-1", "STUDENT_ID", "Shapes", T, T + 1000), List.of());

  private static class BrokenExporter extends ReportExporter {
    BrokenExporter() { super(ZoneOffset.UTC); }
    @Override protected String extension() { return "txt"; }
    @Override protected void write(SessionInfo s, List<TelemetryEvent> e, File f) throws IOException {
      throw new IOException("disk full");
    }
  }

  @Test
  void writesOneFilePerExporter() throws IOException {
    File folder = Files.createTempDirectory("export").toFile();
    ExportService service = new ExportService(List.of(
        new JsonReportExporter(new JsonEventSerialiser(), ZoneOffset.UTC),
        new PdfReportExporter(new PdfReportMaker(ZoneOffset.UTC), ZoneOffset.UTC)));
    try {
      List<File> files = service.exportNow(session, folder);

      assertEquals(2, files.size());
      assertEquals(2, folder.listFiles().length);
    } finally {
      for (File f : folder.listFiles()) {
        f.delete();
      }
      folder.delete();
    }
  }

  @Test
  void oneFailingFormatDoesNotStopTheOthers() throws IOException {
    File folder = Files.createTempDirectory("export").toFile();
    ExportService service = new ExportService(List.of(
        new BrokenExporter(),
        new JsonReportExporter(new JsonEventSerialiser(), ZoneOffset.UTC)));
    try {
      List<File> files = service.exportNow(session, folder);

      assertEquals(1, files.size());
      assertEquals(true, files.get(0).getName().endsWith(".json"));
    } finally {
      for (File f : folder.listFiles()) {
        f.delete();
      }
      folder.delete();
    }
  }
}