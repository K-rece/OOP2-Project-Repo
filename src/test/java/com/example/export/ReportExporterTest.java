package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import com.example.TelemetryEventFactory;
import com.google.gson.JsonParser;

class ReportExporterTest {
  private static final long T = 1_700_000_000_000L;

  private final SessionInfo session = new SessionInfo("s-1", "STUDENT_ID", "My Shapes!", T, T + 60_000);
  private final ReportExporter json = new JsonReportExporter(new JsonEventSerialiser(), ZoneOffset.UTC);
  private final ReportExporter pdf = new PdfReportExporter(new PdfReportMaker(ZoneOffset.UTC), ZoneOffset.UTC);

  @Test
  void fileNameHasProjectAndStartTime() {
    assertEquals("My_Shapes__2023-11-14_22-13-20_telemetry.json", json.fileName(session));
    assertEquals("My_Shapes__2023-11-14_22-13-20_telemetry.pdf", pdf.fileName(session));
  }

  @Test
  void unsafeOrEmptyProjectNamesAreCleaned() {
    assertEquals("a_b_c", ReportExporter.safeName("a/b:c"));
    assertEquals("project", ReportExporter.safeName(""));
    assertEquals("project", ReportExporter.safeName(null));
  }

  @Test
  void writesValidJsonAndPdfIntoTheFolder() throws IOException {
    File folder = Files.createTempDirectory("export").toFile();
    List<com.example.TelemetryEvent> events =
        List.of(new TelemetryEventFactory(() -> T).compileSuccess());
    try {
      File jsonFile = json.export(session, events, folder);
      File pdfFile = pdf.export(session, events, folder);

      assertEquals(folder, jsonFile.getParentFile());
      String text = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
      assertEquals("My Shapes!", JsonParser.parseString(text).getAsJsonObject()
          .getAsJsonObject("session").get("projectName").getAsString());

      try (PDDocument doc = PDDocument.load(pdfFile)) {
        assertTrue(doc.getNumberOfPages() >= 1);
      }
    } finally {
      for (File f : folder.listFiles()) {
        f.delete();
      }
      folder.delete();
    }
  }
}