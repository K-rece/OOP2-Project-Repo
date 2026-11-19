package com.example;

import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Creates {@link TelemetryEvent}s with the correct type, data keys and current time, so the rest
 * of the plugin never builds events by hand. Takes a clock so tests can fix the time.
 */
public class TelemetryEventFactory {
  /** Supplies the current time in milliseconds */
  private final LongSupplier clock;

  /**
   * Creates a factory that timestamps events using the given clock.
   *
   * @param clock supplies the current time in milliseconds
   */
  public TelemetryEventFactory(LongSupplier clock) {
    this.clock = clock;
  }

  /** Creates a factory that timestamps events with the system clock. */
  public TelemetryEventFactory() {
    this(System::currentTimeMillis);
  }

  /**
   * Creates a {@link EventType#COMPILE_ERROR} event.
   *
   * @param file the name of the file with the error
   * @param line the line number of the error
   * @param message the compiler's error message
   * @return the new event
   */
  public TelemetryEvent compileError(String file, int line, String message) {
    return new TelemetryEvent(EventType.COMPILE_ERROR, clock.getAsLong(), Map.of("file", file, "line", line, "message", message));
  }

  /**
   * Creates a {@link EventType#COMPILE_SUCCESS} event.
   *
   * @return the new event
   */
  public TelemetryEvent compileSuccess() {
    return new TelemetryEvent(EventType.COMPILE_SUCCESS, clock.getAsLong(), Map.of());
  }

  /**
   * Creates a {@link EventType#CLASS_CREATED} event.
   *
   * @param className the class's name
   * @return the new event
   */
  public TelemetryEvent classCreated(String className) {
    return new TelemetryEvent(EventType.CLASS_CREATED, clock.getAsLong(), Map.of("className", className));
  }

  /**
   * Creates a {@link EventType#PROJECT_OPENED} event.
   *
   * @param projectName the project's name
   * @return the new event
   */
  public TelemetryEvent projectOpened(String projectName) {
    return new TelemetryEvent(EventType.PROJECT_OPENED, clock.getAsLong(), Map.of("projectName", projectName));
  }

  /**
   * Creates a {@link EventType#PROJECT_CLOSED} event.
   *
   * @param projectName the project's name
   * @return the new event
   */
  public TelemetryEvent projectClosed(String projectName) {
    return new TelemetryEvent(EventType.PROJECT_CLOSED, clock.getAsLong(), Map.of("projectName", projectName));
  }

  /**
   * Creates a {@link EventType#CLASS_MODIFIED} event.
   *
   * @param className the class's name
   * @return the new event
   */
  public TelemetryEvent classModified(String className) {
    return new TelemetryEvent(EventType.CLASS_MODIFIED, clock.getAsLong(), Map.of("className", className));
  }

  /**
   * Creates a {@link EventType#CLASS_DELETED} event.
   *
   * @param className the class's name
   * @return the new event
   */
  public TelemetryEvent classDeleted(String className) {
    return new TelemetryEvent(EventType.CLASS_DELETED, clock.getAsLong(), Map.of("className", className));
  }

  /**
   * Creates a {@link EventType#RUN_FINISHED} event.
   *
   * @param className the class the method belongs to
   * @param methodName the method that was called
   * @return the new event
   */
  public TelemetryEvent runFinished(String className, String methodName) {
    return new TelemetryEvent(EventType.RUN_FINISHED, clock.getAsLong(), Map.of("className", className, "method", methodName));
  }

  /**
   * Creates a {@link EventType#RUN_EXCEPTION} event.
   *
   * @param className the class the method belongs to
   * @param methodName the method that was called
   * @param exception a description of the exception thrown
   * @return the new event
   */
  public TelemetryEvent runException(String className, String methodName, String exception) {
    return new TelemetryEvent(EventType.RUN_EXCEPTION, clock.getAsLong(), Map.of("className", className, "method", methodName, "exception", exception));
  }

  /**
   * Creates a {@link EventType#IDLE_START} event timestamped now.
   *
   * @return the new event
   */
  public TelemetryEvent idleStarted() {
    return new TelemetryEvent(EventType.IDLE_START, clock.getAsLong(), Map.of());
  }

  /**
   * Creates a {@link EventType#IDLE_END} event.
   *
   * @return the new event
   */
  public TelemetryEvent idleEnded() {
    return new TelemetryEvent(EventType.IDLE_END, clock.getAsLong(), Map.of());
  }

  /**
   * Creates a {@link EventType#IDLE_START} event with an earlier timestamp. Idle time is only
   * noticed after the idle threshold has passed, so the event is backdated to when activity
   * stopped.
   *
   * @param timestampMillis when the idle period actually began
   * @return the new event
   */
  public TelemetryEvent idleStartedAt(long timestampMillis) {
    return new TelemetryEvent(EventType.IDLE_START, timestampMillis, Map.of());
  }

  /**
   * Creates an {@link EventType#EDIT} event.
   *
   * @param className the class whose editor changed
   * @param charsChanged the change in the editor's text length (negative if text was deleted)
   * @return the new event
   */
  public TelemetryEvent edit(String className, int charsChanged) {
    return new TelemetryEvent(EventType.EDIT, clock.getAsLong(), Map.of("className", className, "charsChanged", charsChanged));
  }

  /**
   * Creates a {@link EventType#METHOD_ADDED} event.
   *
   * @param className the class the method belongs to
   * @param method the method's signature
   * @return the new event
   */
  public TelemetryEvent methodAdded(String className, String method) {
    return new TelemetryEvent(EventType.METHOD_ADDED, clock.getAsLong(), Map.of("className", className, "method", method));
  }

}