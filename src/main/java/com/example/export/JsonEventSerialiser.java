package com.example.export;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import com.example.TelemetryEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonEventSerialiser implements EventSerialiser {

  static final String SCHEMA_VERSION = "1.1";
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  @Override
  public String serialise(SessionInfo session, List<TelemetryEvent> events) {

    List<TelemetryEvent> ordered = new ArrayList<>(events);
    ordered.sort(Comparator.comparingLong(TelemetryEvent::getTimestamp));

    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", SCHEMA_VERSION);
    root.add("session", sessionToJson(session));
    root.add("summary", summaryToJson(new SessionStatistics(ordered)));
    root.add("events", eventsToJson(ordered));
    return gson.toJson(root);
  }

  private JsonObject sessionToJson(SessionInfo session) {

    JsonObject obj = new JsonObject();
    obj.addProperty("sessionId", session.getSessionId());
    obj.addProperty("studentId", session.getStudentId());
    obj.addProperty("projectName", session.getProjectName());
    obj.addProperty("startTime", isoTime(session.getStartMillis()));
    obj.addProperty("endTime", isoTime(session.getEndMillis()));
    obj.addProperty("durationSeconds", session.getDurationMillis() / 1000);
    return obj;
  }

  private JsonObject summaryToJson(SessionStatistics stats) {

    JsonObject obj = new JsonObject();
    obj.addProperty("eventCount", stats.getEventCount());
    obj.addProperty("compilationAttempts", stats.getCompilationAttempts());
    obj.addProperty("successfulCompilations", stats.getSuccessfulCompilations());
    obj.addProperty("failedCompilations", stats.getFailedCompilations());
    obj.addProperty("averageSecondsBetweenCompiles", stats.getAverageSecondsBetweenCompiles());
    return obj;
  }

  private JsonArray eventsToJson(List<TelemetryEvent> events) {

    JsonArray array = new JsonArray();

    for (TelemetryEvent event : events) {
      JsonObject obj = new JsonObject();
      obj.addProperty("type", event.getEventType().name());
      obj.addProperty("timestamp", isoTime(event.getTimestamp()));
      obj.add("data", gson.toJsonTree(new TreeMap<>(event.getData())));
      array.add(obj);
    }
    return array;
  }

  private static String isoTime(long millis) {
    return Instant.ofEpochMilli(millis).toString();
  }
}