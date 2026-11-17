package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.example.EventType;
import com.example.TelemetryEventFactory;

class ReportDataTest {
  private static final long T = 1_700_000_000_000L;
  private static final long TEN_MIN = 600_000;

  private final SessionInfo session = new SessionInfo("s-1", "STUDENT_ID", "Shapes", T, T + TEN_MIN);

  private TelemetryEventFactory at(long offsetMillis) {
    return new TelemetryEventFactory(() -> T + offsetMillis);
  }

  @Test
  void noIdleEventsMeansAllActive() {
    ReportData data = new ReportData(session, List.of(at(0).compileSuccess()));

    assertEquals(0, data.getIdleMillis());
    assertEquals(TEN_MIN, data.getActiveMillis());
  }

  @Test
  void addsUpIdleStretches() {
    ReportData data = new ReportData(session, List.of(
        at(60_000).idleStarted(), at(120_000).idleEnded(),
        at(300_000).idleStarted(), at(420_000).idleEnded()));

    assertEquals(180_000, data.getIdleMillis());
    assertEquals(420_000, data.getActiveMillis());
  }

  @Test
  void unfinishedIdleCountsUntilSessionEnd() {
    ReportData data = new ReportData(session, List.of(at(480_000).idleStarted()));

    assertEquals(120_000, data.getIdleMillis());
  }

  @Test
  void idleEndWithoutStartIsIgnored() {
    ReportData data = new ReportData(session, List.of(at(60_000).idleEnded()));

    assertEquals(0, data.getIdleMillis());
  }

  @Test
  void errorsAreCountedMostFrequentFirst() {
    ReportData data = new ReportData(session, List.of(
        at(0).compileError("A.java", 1, "';' expected"),
        at(1).compileError("A.java", 9, "cannot find symbol"),
        at(2).compileError("B.java", 4, "cannot find symbol"),
        at(3).compileError("B.java", 5, "cannot find symbol")));

    Map<String, Integer> freq = data.getErrorFrequency();
    assertEquals(List.of("cannot find symbol", "';' expected"), List.copyOf(freq.keySet()));
    assertEquals(3, (int) freq.get("cannot find symbol"));
    assertEquals(1, (int) freq.get("';' expected"));
  }

  @Test
  void timelineHasOnlyClassCreationsAndDeletionsInOrder() {
    ReportData data = new ReportData(session, List.of(
        at(3000).classDeleted("Cat"),
        at(1000).classCreated("Dog"),
        at(2000).classModified("Dog"),
        at(1500).compileSuccess()));

    assertEquals(2, data.getClassTimeline().size());
    assertEquals(EventType.CLASS_CREATED, data.getClassTimeline().get(0).getEventType());
    assertEquals(EventType.CLASS_DELETED, data.getClassTimeline().get(1).getEventType());
  }

  @Test
  void timelineIncludesNewMethods() {
    ReportData data = new ReportData(session, List.of(
        at(1000).classCreated("Dog"),
        at(2000).methodAdded("Dog", "bark()")));

    assertEquals(2, data.getClassTimeline().size());
    assertEquals(EventType.METHOD_ADDED, data.getClassTimeline().get(1).getEventType());
  }

  @Test
  void successRateIsPercentOfCompiles() {
    ReportData data = new ReportData(session, List.of(
        at(0).compileError("A.java", 1, "x"),
        at(1).compileSuccess(), at(2).compileSuccess(), at(3).compileSuccess()));

    assertEquals(75.0, data.getCompileSuccessRate(), 0.001);
  }

  @Test
  void successRateIsZeroWithNoCompiles() {
    assertEquals(0.0, new ReportData(session, List.of()).getCompileSuccessRate(), 0.001);
  }
}