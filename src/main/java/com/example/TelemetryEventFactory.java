package com.example;

import java.util.Map;
import java.util.function.LongSupplier;

public class TelemetryEventFactory {
  private final LongSupplier clock;

  public TelemetryEventFactory(LongSupplier clock) {
    this.clock = clock;
  }

  public TelemetryEventFactory() {
    this(System::currentTimeMillis);
  }

  public TelemetryEvent compileError(String file, int line, String message) {
    return new TelemetryEvent(EventType.COMPILE_ERROR, clock.getAsLong(),
        Map.of("file", file, "line", line, "message", message));
  }
  // Make more meathod for each event

  public TelemetryEvent compileSuccess() {
    return new TelemetryEvent(EventType.COMPILE_SUCCESS, clock.getAsLong(), Map.of());
  }

  public TelemetryEvent classCreated(String className) {
    return new TelemetryEvent(EventType.CLASS_CREATED, clock.getAsLong(),
        Map.of("className", className));
  }

  public TelemetryEvent projectOpened(String projectName) {
    return new TelemetryEvent(EventType.PROJECT_OPENED, clock.getAsLong(),
        Map.of("projectName", projectName));
  }

  public TelemetryEvent projectClosed(String projectName) {
    return new TelemetryEvent(EventType.PROJECT_CLOSED, clock.getAsLong(),
        Map.of("projectName", projectName));
  }

  public TelemetryEvent classModified(String className) {
    return new TelemetryEvent(EventType.CLASS_MODIFIED, clock.getAsLong(),
        Map.of("className", className));
  }

  public TelemetryEvent classDeleted(String className) {
    return new TelemetryEvent(EventType.CLASS_DELETED, clock.getAsLong(),
        Map.of("className", className));
  }

  public TelemetryEvent runFinished(String className, String methodName) {
    return new TelemetryEvent(EventType.RUN_FINISHED, clock.getAsLong(),
        Map.of("className", className, "method", methodName));
  }

  public TelemetryEvent runException(String className, String methodName, String exception) {
    return new TelemetryEvent(EventType.RUN_EXCEPTION, clock.getAsLong(),
        Map.of("className", className, "method", methodName,
            "exception", exception));
  }

}