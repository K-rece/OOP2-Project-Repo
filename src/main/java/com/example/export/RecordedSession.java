package com.example.export;

import java.util.List;
import com.example.TelemetryEvent;

/**
 * A finished ession: its details plus the events recorded during it.
 */
public class RecordedSession {
    
  /** The session's details. */
  private final SessionInfo info;
  /** The events recorded during the session. Unmodifiable. */
  private final List<TelemetryEvent> events;

  /**
   * Creates a recorded session.
   *
   * @param info the session's details
   * @param events the events recorded(copied) so later changes to the list have no effect
   */
  public RecordedSession(SessionInfo info, List<TelemetryEvent> events) {
    this.info = info;
    this.events = List.copyOf(events);
  }

  /**
   * Returns the session's details.
   *
   * @return the session's details
   */
  public SessionInfo getInfo() { return info; }
  /**
   * Returns an unmodifiable list of the session's events.
   *
   * @return an unmodifiable list of the session's events
   */
  public List<TelemetryEvent> getEvents() { return events; }
}

