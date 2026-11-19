package com.example.export;

import java.util.List;
import com.example.TelemetryEvent;

/**
 * Turns a session and its events into text in some output format. Code that saves files depends on this interface rather than on JSON, so another format
 * could be added without changing existing classes.
 */
public interface EventSerialiser {
  /**
   * Converts a session and its events into text.
   *
   * @param session the session's details such as ID, project, and times.
   * @param events the events recorded during the session.
   * @return the session as text in this serialiser's format
   */
  String serialise(SessionInfo session, List<TelemetryEvent> events);
}

