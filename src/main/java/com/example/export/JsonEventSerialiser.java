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

/**
 * Writes a session as a JSON document with three parts: {@code session}
 * (details and times), {@code summary} (compile counts and timing) and {@code events} (every event
 * in time order, each with an ISO-8601 UTC timestamp).
 */
public class JsonEventSerialiser implements EventSerialiser {

  /** Version of the JSON layout */
  static final String SCHEMA_VERSION = "1.1";
  /**
   * Gson instance set up for readable output (indented, without escaping characters like {@code
   * '}).
   */
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  /** Creates a JSON serialiser. */
  public JsonEventSerialiser() {
  }

  /**
   * Converts a session into pretty-printed JSON. Events are sorted by time and events with the same time keep their original order.
   *
   * @param session the session's details
   * @param events the events recorded during the session
   * @return JSON text
   */
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

  /**
   * Builds the {@code session} part: session ID, student ID, project name, start and end time, and
   * duration.
   *
   * @param session the session's details
   * @return the session as a JSON object
   */
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

  /**
   * Builds the {@code summary} part: event count, compilation attempts, successes, failures and
   * the average time between compiles.
   *
   * @param stats the session's calculated statistics
   * @return the summary as a JSON object
   */
  private JsonObject summaryToJson(SessionStatistics stats) {

    JsonObject obj = new JsonObject();
    obj.addProperty("eventCount", stats.getEventCount());
    obj.addProperty("compilationAttempts", stats.getCompilationAttempts());
    obj.addProperty("successfulCompilations", stats.getSuccessfulCompilations());
    obj.addProperty("failedCompilations", stats.getFailedCompilations());
    obj.addProperty("averageSecondsBetweenCompiles", stats.getAverageSecondsBetweenCompiles());
    return obj;
  }

  /**
   * Builds the {@code events} part. Each event's data keys are written in alphabetical order so
   * the output is the same every time.
   *
   * @param events the events, already sorted by time
   * @return the events as a JSON array
   */
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

  /**
   * Formats a time as ISO-8601 in UTC
   *
   * @param millis milliseconds since 1 January 1970 (UTC)
   * @return the formatted time
   */
  private static String isoTime(long millis) {
    return Instant.ofEpochMilli(millis).toString();
  }
}