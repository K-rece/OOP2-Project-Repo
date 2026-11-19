package com.example.export;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.EventType;
import com.example.TelemetryEvent;


/**
 * @author Ubayd Razack
 * Works out everything the PDF report shows. The PDF maker only draws the values, so all the calculations can be unit tested here.
 */
public class ReportData {

  /** The session's details. */
  private final SessionInfo session;
  /** Compile counts and timing for the session. */
  private final SessionStatistics statistics;
  /** Total idle time in the session, in milliseconds. */
  private final long idleMillis;
  /** Each compiler error message and how many times it occurred, most frequent first. */
  private final Map<String, Integer> errorFrequency;
  /** Class creations, deletions and new methods, in time order. */
  private final List<TelemetryEvent> classTimeline;

  /**
   * Calculates the report's values for a session.
   *
   * @param session the session's details
   * @param events the session's events, in any order
   */
  public ReportData(SessionInfo session, List<TelemetryEvent> events) {
    List<TelemetryEvent> ordered = new ArrayList<>(events);
    ordered.sort(Comparator.comparingLong(TelemetryEvent::getTimestamp));

    this.session = session;
    this.statistics = new SessionStatistics(ordered);
    this.idleMillis = calculateIdleMillis(ordered, session);
    this.errorFrequency = countErrors(ordered);
    this.classTimeline = filterClassEvents(ordered);
  }

  /**
   * Adds up the time between each {@code IDLE_START} and the following {@code IDLE_END}. An idle
   * period still open at the end of the session counts up to the session's end. The total is never
   * more than the session's length.
   *
   * @param events the session's events, sorted by time
   * @param session the session's details
   * @return the total idle time in milliseconds
   */
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

  /**
   * Counts how many times each compiler error message occurred.
   *
   * @param events the session's events
   * @return each message and its count, most frequent first, ties in alphabetical order
   */
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

  /**
   * Picks out the events for the progression timeline: classes created and deleted, and methods added.
   *
   * @param events the session's events, sorted by time
   * @return the timeline events, in time order
   */
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

  /**
   * Returns the session's details.
   *
   * @return the session's details
   */
  public SessionInfo getSession() { return session; }
  /**
   * Returns the session's compile counts and timing.
   *
   * @return the session's compile counts and timing
   */
  public SessionStatistics getStatistics() { return statistics; }
  /**
   * Returns the total idle time in milliseconds.
   *
   * @return the total idle time in milliseconds
   */
  public long getIdleMillis() { return idleMillis; }
  /**
   * Returns the session length minus idle time, in milliseconds.
   *
   * @return the session length minus idle time, in milliseconds
   */
  public long getActiveMillis() { return session.getDurationMillis() - idleMillis; }
  /**
   * Returns a copy of each error message and its count, most frequent first.
   *
   * @return a copy of each error message and its count, most frequent first
   */
  public Map<String, Integer> getErrorFrequency() { return new LinkedHashMap<>(errorFrequency); }
  /**
   * Returns an unmodifiable list of class and method events, in time order.
   *
   * @return an unmodifiable list of class and method events, in time order
   */
  public List<TelemetryEvent> getClassTimeline() { return List.copyOf(classTimeline); }

  /**
   * Returns the percentage of compiles that succeeded.
   *
   * @return a percentage from 0 to 100, or 0 if there were no compiles
   */
  public double getCompileSuccessRate() {
    int attempts = statistics.getCompilationAttempts();
    return attempts == 0 ? 0 : 100.0 * statistics.getSuccessfulCompilations() / attempts;
  }
}

