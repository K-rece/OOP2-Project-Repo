package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.TelemetryEventFactory;

class SessionStatisticsTest {
  private static final long T = 1_700_000_000_000L;

  private TelemetryEventFactory at(long offsetMillis) {
    return new TelemetryEventFactory(() -> T + offsetMillis);
  }

  @Test
  void countsSuccessfulAndFailedCompiles() {
    SessionStatistics stats = new SessionStatistics(List.of(
        at(0).compileError("A.java", 1, "x"),
        at(1000).compileError("A.java", 2, "y"),
        at(2000).compileSuccess(),
        at(3000).classCreated("B")));

    assertEquals(4, stats.getEventCount());
    assertEquals(1, stats.getSuccessfulCompilations());
    assertEquals(2, stats.getFailedCompilations());
    assertEquals(3, stats.getCompilationAttempts());
  }

  @Test
  void averagesGapBetweenCompiles() {
    // gaps of 10s and 30s -> average 20s
    SessionStatistics stats = new SessionStatistics(List.of(
        at(0).compileSuccess(),
        at(10_000).compileSuccess(),
        at(40_000).compileSuccess()));

    assertEquals(20.0, stats.getAverageSecondsBetweenCompiles(), 0.001);
  }

  @Test
  void averageIgnoresEventOrder() {
    SessionStatistics stats = new SessionStatistics(List.of(
        at(40_000).compileSuccess(),
        at(0).compileSuccess(),
        at(10_000).compileSuccess()));

    assertEquals(20.0, stats.getAverageSecondsBetweenCompiles(), 0.001);
  }

  @Test
  void averageIsZeroWithFewerThanTwoCompiles() {
    assertEquals(0.0, new SessionStatistics(List.of()).getAverageSecondsBetweenCompiles(), 0.001);
    assertEquals(0.0, new SessionStatistics(List.of(at(0).compileSuccess()))
        .getAverageSecondsBetweenCompiles(), 0.001);
  }

  @Test
  void ignoresNonCompileEvents() {
    SessionStatistics stats = new SessionStatistics(List.of(
        at(0).classCreated("A"),
        at(5000).projectOpened("Shapes")));

    assertEquals(0, stats.getCompilationAttempts());
    assertEquals(0.0, stats.getAverageSecondsBetweenCompiles(), 0.001);
  }
}