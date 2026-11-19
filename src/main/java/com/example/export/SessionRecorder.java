package com.example.export;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import com.example.EventType;
import com.example.TelemetryEvent;
import com.example.TelemetryObserver;

/**
 * Observer that keeps the events for the current session only. A session starts when a project
 * opens and ends when it closes; {@link #finish()} hands back everything recorded and resets for
 * the next session. The student ID is read from BlueJ's preferences each time a report is made.
 */
public class SessionRecorder implements TelemetryObserver {

  /** Supplies the current time; replaced by a fixed value in tests. */
  private final LongSupplier clock;
  /** Supplies the student ID entered in BlueJ's preferences, read at export time. */
  private final Supplier<String> studentId;
  /** The events recorded so far in the current session. */
  private final List<TelemetryEvent> events = new ArrayList<>();
  /** Whether a session is currently running. */
  private boolean active;
  /** Unique ID of the current session. */
  private String sessionId;
  /** Project of the current session. */
  private String projectName;
  /** When the current session started. */
  private long startMillis;

  /** Creates a recorder that uses the system clock and the student ID placeholder. */
  public SessionRecorder() {
    this(System::currentTimeMillis);
  }

  /**
   * Creates a recorder that uses the given clock and the student ID placeholder.
   *
   * @param clock supplies the current time in milliseconds
   */
  public SessionRecorder(LongSupplier clock) {
    this(clock, () -> SessionInfo.STUDENT_ID_PLACEHOLDER);
  }

  /**
   * Creates a recorder that uses the given clock and student ID source.
   *
   * @param clock supplies the current time in milliseconds
   * @param studentId supplies the student ID; asked each time a report is made
   */
  public SessionRecorder(LongSupplier clock, Supplier<String> studentId) {
    this.clock = clock;
    this.studentId = studentId;
  }

  /**
   * Starts a new session for a project. Does nothing if a session is already running.
   *
   * @param projectName the project being opened
   */
  public synchronized void start(String projectName) {
    begin(projectName, clock.getAsLong());
  }

  /**
   * Starts a new session with a fresh ID, unless one is already running.
   *
   * @param name the project's name
   * @param startTime when the session starts
   */
  private void begin(String name, long startTime) {
    if (active) {
      return;
    }
    active = true;
    sessionId = UUID.randomUUID().toString();
    projectName = name;
    startMillis = startTime;
    events.clear();
  }

  /**
   * Records an event if a session is running. A {@link EventType#PROJECT_OPENED} event also starts
   * a session, so it does not matter whether this or the BlueJ listener hears about the project
   * first.
   *
   * @param event the event that was just published
   */
  @Override
  public synchronized void onEvent(TelemetryEvent event) {
    if (event.getEventType() == EventType.PROJECT_OPENED) {
      begin(String.valueOf(event.getData().get("projectName")), event.getTimestamp());
    }
    if (active) {
      events.add(event);
    }
  }

  /**
   * Returns true if a session is currently running.
   *
   * @return true if a session is currently running
   */
  public synchronized boolean isActive() {
    return active;
  }

  /**
   * Returns the session so far without ending it (used by manual export).
   *
   * @return the session's details and the events recorded so far
   * @throws IllegalStateException if no session is running
   */
  public synchronized RecordedSession snapshot() {
    if (!active) {
      throw new IllegalStateException("No session is running");
    }
    long end = Math.max(clock.getAsLong(), startMillis);
    SessionInfo info = new SessionInfo(sessionId, currentStudentId(), projectName, startMillis, end);
    return new RecordedSession(info, events);
  }

  /**
   * Gets the student ID from its source.
   *
   * @return the trimmed student ID, or the placeholder if none has been entered
   */
  private String currentStudentId() {
    String id = studentId.get();
    if (id == null || id.trim().isEmpty()) {
      return SessionInfo.STUDENT_ID_PLACEHOLDER;
    }
    return id.trim();
  }

  /**
   * Ends the session and returns everything recorded during it.
   *
   * @return the session's details and all its events
   * @throws IllegalStateException if no session is running
   */
  public synchronized RecordedSession finish() {
    RecordedSession session = snapshot();
    active = false;
    events.clear();
    return session;
  }
}
