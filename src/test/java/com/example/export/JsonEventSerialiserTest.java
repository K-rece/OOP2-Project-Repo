package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import com.example.TelemetryEventFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

 /** @author Justin Craigwell */
 class JsonEventSerialiserTest {
  private static final long T = 1_700_000_000_000L; // 2023-11-14T22:13:20Z

  private final EventSerialiser serialiser = new JsonEventSerialiser();
  private final SessionInfo session = new SessionInfo("s-1", SessionInfo.STUDENT_ID_PLACEHOLDER, "Shapes", T, T + 1_800_000);

  private JsonObject parse(String json) {
    return JsonParser.parseString(json).getAsJsonObject(); // throws if JSON is invalid
  }

  @Test
  void serialisesTypeTimestampAndData() {
    TelemetryEventFactory factory = new TelemetryEventFactory(() -> T);

    JsonObject root = parse(serialiser.serialise(session,
        List.of(factory.compileError("Dog.java", 12, "';' expected"))));

    assertEquals("1.1", root.get("schemaVersion").getAsString());

    JsonObject event = root.getAsJsonArray("events").get(0).getAsJsonObject();
    assertEquals("COMPILE_ERROR", event.get("type").getAsString());
    assertEquals("2023-11-14T22:13:20Z", event.get("timestamp").getAsString());

    JsonObject data = event.getAsJsonObject("data");
    assertEquals("Dog.java", data.get("file").getAsString());
    assertEquals(12, data.get("line").getAsInt());
    assertEquals("';' expected", data.get("message").getAsString());
  }

  @Test
  void includesSessionMetadata() {
    JsonObject s = parse(serialiser.serialise(session, List.of())).getAsJsonObject("session");

    assertEquals("s-1", s.get("sessionId").getAsString());
    assertEquals("STUDENT_ID", s.get("studentId").getAsString());
    assertEquals("Shapes", s.get("projectName").getAsString());
    assertEquals("2023-11-14T22:13:20Z", s.get("startTime").getAsString());
    assertEquals("2023-11-14T22:43:20Z", s.get("endTime").getAsString());
    assertEquals(1800, s.get("durationSeconds").getAsLong());
  }

  @Test
  void includesCompileSummary() {
    TelemetryEventFactory first = new TelemetryEventFactory(() -> T);
    TelemetryEventFactory second = new TelemetryEventFactory(() -> T + 30_000);

    JsonObject summary = parse(serialiser.serialise(session, List.of(
        first.compileError("Dog.java", 3, "missing ;"),
        second.compileSuccess()))).getAsJsonObject("summary");

    assertEquals(2, summary.get("eventCount").getAsInt());
    assertEquals(2, summary.get("compilationAttempts").getAsInt());
    assertEquals(1, summary.get("successfulCompilations").getAsInt());
    assertEquals(1, summary.get("failedCompilations").getAsInt());
    assertEquals(30.0, summary.get("averageSecondsBetweenCompiles").getAsDouble(), 0.001);
  }

  @Test
  void emptySessionGivesZeroCountsAndEmptyEvents() {
    JsonObject root = parse(serialiser.serialise(session, List.of()));

    assertEquals(0, root.getAsJsonObject("summary").get("eventCount").getAsInt());
    assertEquals(0, root.getAsJsonArray("events").size());
  }

  @Test
  void eventsAreOrderedByTimestamp() {
    TelemetryEventFactory later = new TelemetryEventFactory(() -> T + 5000);
    TelemetryEventFactory earlier = new TelemetryEventFactory(() -> T);

    JsonArray events = parse(serialiser.serialise(session, List.of(
        later.compileSuccess(),
        earlier.classCreated("Dog")))).getAsJsonArray("events");

    assertEquals("CLASS_CREATED", events.get(0).getAsJsonObject().get("type").getAsString());
    assertEquals("COMPILE_SUCCESS", events.get(1).getAsJsonObject().get("type").getAsString());
  }
}