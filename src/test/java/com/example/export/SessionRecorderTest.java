package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.example.TelemetryEventFactory;

class SessionRecorderTest {

  private static final long T = 1_700_000_000_000L;
  private final long[] now = {T}; // a clock the test can move forward
  private final SessionRecorder recorder = new SessionRecorder(() -> now[0]);
  private final TelemetryEventFactory factory = new TelemetryEventFactory(() -> now[0]);

  @Test
  void ignoresEventsBeforeASessionStarts() {
    recorder.onEvent(factory.compileSuccess());

    assertEquals(false, recorder.isActive());
  }

  @Test
  void projectOpenedEventStartsASession() {
    recorder.onEvent(factory.projectOpened("Shapes"));
    recorder.onEvent(factory.compileSuccess());
    now[0] = T + 60_000;

    RecordedSession session = recorder.finish();
    assertEquals("Shapes", session.getInfo().getProjectName());
    assertEquals(T, session.getInfo().getStartMillis());
    assertEquals(60_000, session.getInfo().getDurationMillis());
    assertEquals(2, session.getEvents().size());
  }

  @Test
  void startingTwiceKeepsTheFirstSession() {
    recorder.start("Shapes");
    now[0] = T + 5000;
    recorder.onEvent(factory.projectOpened("Shapes"));

    assertEquals(T, recorder.snapshot().getInfo().getStartMillis());
  }

  @Test
  void snapshotKeepsRecordingButFinishResets() {
    recorder.start("Shapes");
    recorder.onEvent(factory.compileSuccess());

    assertEquals(1, recorder.snapshot().getEvents().size());
    assertTrue(recorder.isActive());

    recorder.finish();
    assertEquals(false, recorder.isActive());
  }

  @Test
  void eachSessionGetsANewId() {
    recorder.start("Shapes");
    String first = recorder.finish().getInfo().getSessionId();
    recorder.start("Shapes");
    String second = recorder.finish().getInfo().getSessionId();

    assertTrue(!first.equals(second));
  }

  @Test
  void usesThePlaceholderWhenNoStudentIdIsGiven() {
    recorder.start("Shapes");

    assertEquals("STUDENT_ID", recorder.snapshot().getInfo().getStudentId());
  }

  @Test
  void usesTheSuppliedStudentIdTrimmed() {
    SessionRecorder withId = new SessionRecorder(() -> now[0], () -> "  816012345 ");
    withId.start("Shapes");

    assertEquals("816012345", withId.snapshot().getInfo().getStudentId());
  }

  @Test
  void blankStudentIdFallsBackToThePlaceholder() {
    SessionRecorder blank = new SessionRecorder(() -> now[0], () -> "   ");
    blank.start("Shapes");

    assertEquals("STUDENT_ID", blank.snapshot().getInfo().getStudentId());
  }

  @Test
  void studentIdIsReadAtExportTime() {
    String[] id = {""};
    SessionRecorder changing = new SessionRecorder(() -> now[0], () -> id[0]);
    changing.start("Shapes");
    id[0] = "816012345"; // entered in preferences after the session started

    assertEquals("816012345", changing.snapshot().getInfo().getStudentId());
  }

  @Test
  void snapshotWithoutSessionIsRejected() {
    assertThrows(IllegalStateException.class, () -> recorder.snapshot());
  }
}