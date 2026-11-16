package com.example.export;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.EventType;
import com.example.TelemetryEvent;


public class ReportData {

  private final SessionInfo session;
  private final SessionStatistics statistics;
  private final long idleMillis;
  private final Map<String, Integer> errorFrequency;
  private final List<TelemetryEvent> classTimeline;

  public ReportData(SessionInfo session, List<TelemetryEvent> events) {
    List<TelemetryEvent> ordered = new ArrayList<>(events);
    ordered.sort(Comparator.comparingLong(TelemetryEvent::getTimestamp));

    this.session = session;
    this.statistics = new SessionStatistics(ordered);
    this.idleMillis = calculateIdleMillis(ordered, session);
    this.errorFrequency = countErrors(ordered);
    this.classTimeline = filterClassEvents(ordered);
  }

  private static long calculateIdleMillis(List<TelemetryEvent> events, SessionInfo session) {
    long total = 0;
    Long idleSince = null;

    for (TelemetryEvent event : events) {
      if (event.getEventType() == EventType.IDLE_START && idleSince == null) {
        idleSince = event.getTimestamp();
      } else if (event.getEventType() == EventType.IDLE_END && idleSince != null) {
        total += event.getTimestamp() - idleSince;
        idleSince = null;
      }
    }
    if (idleSince != null) {
      total += Math.max(0, session.getEndMillis() - idleSince);
    }
    return Math.min(total, session.getDurationMillis()); 
  }

  private static Map<String, Integer> countErrors(List<TelemetryEvent> events) {
    Map<String, Integer> counts = new HashMap<>();
    for (TelemetryEvent event : events) {
      if (event.getEventType() == EventType.COMPILE_ERROR) {
        String message = String.valueOf(event.getData().get("message"));
        counts.put(message, counts.getOrDefault(message, 0) + 1);
      }
    }

    List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
    entries.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
        .thenComparing(Map.Entry.comparingByKey()));

    Map<String, Integer> sorted = new LinkedHashMap<>(); 
    for (Map.Entry<String, Integer> entry : entries) {
      sorted.put(entry.getKey(), entry.getValue());
    }
    return sorted;
  }

  private static List<TelemetryEvent> filterClassEvents(List<TelemetryEvent> events) {
    List<TelemetryEvent> timeline = new ArrayList<>();
    for (TelemetryEvent event : events) {
      EventType type = event.getEventType();
      if (type == EventType.CLASS_CREATED || type == EventType.CLASS_DELETED || type == EventType.METHOD_ADDED) {
        timeline.add(event);
      }
    }
    return timeline;
  }

  public SessionInfo getSession() { return session; }
  public SessionStatistics getStatistics() { return statistics; }
  public long getIdleMillis() { return idleMillis; }
  public long getActiveMillis() { return session.getDurationMillis() - idleMillis; }
  public Map<String, Integer> getErrorFrequency() { return new LinkedHashMap<>(errorFrequency); }
  public List<TelemetryEvent> getClassTimeline() { return List.copyOf(classTimeline); }

  public double getCompileSuccessRate() {
    int attempts = statistics.getCompilationAttempts();
    return attempts == 0 ? 0 : 100.0 * statistics.getSuccessfulCompilations() / attempts;
  }
}

