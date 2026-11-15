package com.example.export;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;
import com.example.EventType;
import com.example.TelemetryEvent;
import com.example.TelemetryObserver;


public class SessionRecorder implements TelemetryObserver {
    
  private final LongSupplier clock;
  private final List<TelemetryEvent> events = new ArrayList<>();
  private boolean active;
  private String sessionId;
  private String projectName;
  private long startMillis;

  public SessionRecorder() {
    this(System::currentTimeMillis);
  }

  public SessionRecorder(LongSupplier clock) {
    this.clock = clock;
  }

  public synchronized void start(String projectName) {
    begin(projectName, clock.getAsLong());
  }

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

  @Override
  public synchronized void onEvent(TelemetryEvent event) {

    if (event.getEventType() == EventType.PROJECT_OPENED) {
      begin(String.valueOf(event.getData().get("projectName")), event.getTimestamp());
    }
    if (active) {
      events.add(event);
    }
  }

  public synchronized boolean isActive() {
    return active;
  }

  public synchronized RecordedSession snapshot() {
    if (!active) {
      throw new IllegalStateException("No session is running");
    }
    long end = Math.max(clock.getAsLong(), startMillis);
    SessionInfo info = new SessionInfo(sessionId, SessionInfo.STUDENT_ID_PLACEHOLDER, projectName, startMillis, end);
    return new RecordedSession(info, events);
  }

  public synchronized RecordedSession finish() {
    RecordedSession session = snapshot();
    active = false;
    events.clear();
    return session;
  }
}

