package com.example.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.EventType;
import com.example.TelemetryEvent;

/**
 * Calculates the summary numbers for a session from its events: event count, successful and failed
 * compilations, and the average time between compiles. Used by both the JSON and the PDF.
 */
public class SessionStatistics {

  /** Total number of events in the session. */
  private final int eventCount;
  /** Number of compiles that succeeded. */
  private final int successfulCompilations;
  /** Number of compiles that reported an error. */
  private final int failedCompilations;
  /** Average gap between consecutive compiles, in seconds to one decimal place. */
  private final double averageSecondsBetweenCompiles;

  /**
   * Calculates the statistics for a list of events.
   *
   * @param events the session's events, in any order
   */
  public SessionStatistics(List<TelemetryEvent> events) {

    int succeeded = 0;
    int failed = 0;
    List<Long> compileTimes = new ArrayList<>();

    for (TelemetryEvent event : events) {
      if (event.getEventType() == EventType.COMPILE_SUCCESS) {
        succeeded++;
        compileTimes.add(event.getTimestamp());
      } else if (event.getEventType() == EventType.COMPILE_ERROR) {
        failed++;
        compileTimes.add(event.getTimestamp());
      }
    }

    this.eventCount = events.size();
    this.successfulCompilations = succeeded;
    this.failedCompilations = failed;
    this.averageSecondsBetweenCompiles = averageGapSeconds(compileTimes);
  }

  /**
   * Calculates the average gap between consecutive compile times. The gaps add up to the time
   * between the first and last compile, so the average is that span divided by the number of gaps.
   *
   * @param times the times of every compile; sorted by this method
   * @return the average gap in seconds to one decimal place, or 0 if there are fewer than two
   */
  private static double averageGapSeconds(List<Long> times) {

    if (times.size() < 2) {
      return 0;
    }
    Collections.sort(times);
    long first = times.get(0);
    long last = times.get(times.size() - 1);
    double averageMillis = (double) (last - first) / (times.size() - 1);
    return Math.round(averageMillis / 100.0) / 10.0;
  }

  /**
   * Returns the total number of events.
   *
   * @return the total number of events
   */
  public int getEventCount() { return eventCount; }
  /**
   * Returns the number of compiles that succeeded.
   *
   * @return the number of compiles that succeeded
   */
  public int getSuccessfulCompilations() { return successfulCompilations; }
  /**
   * Returns the number of compiles that reported an error.
   *
   * @return the number of compiles that reported an error
   */
  public int getFailedCompilations() { return failedCompilations; }
  /**
   * Returns the total number of compiles (successful plus failed).
   *
   * @return the total number of compiles (successful plus failed)
   */
  public int getCompilationAttempts() { return successfulCompilations + failedCompilations; }
  /**
   * Returns the average gap between compiles in seconds, or 0 if there were fewer than two.
   *
   * @return the average gap between compiles in seconds, or 0 if there were fewer than two
   */
  public double getAverageSecondsBetweenCompiles() { return averageSecondsBetweenCompiles; }
}