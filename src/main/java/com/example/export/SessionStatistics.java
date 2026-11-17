package com.example.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.EventType;
import com.example.TelemetryEvent;

public class SessionStatistics {

  private final int eventCount;
  private final int successfulCompilations;
  private final int failedCompilations;
  private final double averageSecondsBetweenCompiles;

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

  public int getEventCount() { return eventCount; }
  public int getSuccessfulCompilations() { return successfulCompilations; }
  public int getFailedCompilations() { return failedCompilations; }
  public int getCompilationAttempts() { return successfulCompilations + failedCompilations; }
  public double getAverageSecondsBetweenCompiles() { return averageSecondsBetweenCompiles; }
}