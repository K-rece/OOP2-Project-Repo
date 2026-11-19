package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.Test;


 /** @author Justin Craigwell */
class TelemetryEventFactoryTest {

  private static final long T = 1_700_000_000_000L;

  private final TelemetryEventFactory factory = new TelemetryEventFactory(() -> T);

  private void check(TelemetryEvent event, EventType type, Map<String, Object> data) {
    assertEquals(type, event.getEventType());
    assertEquals(T, event.getTimestamp());
    assertEquals(data, event.getData());
  }

  @Test
  void projectEvents() {
    check(factory.projectOpened("Shapes"), EventType.PROJECT_OPENED, Map.of("projectName", "Shapes"));
    check(factory.projectClosed("Shapes"), EventType.PROJECT_CLOSED, Map.of("projectName", "Shapes"));
  }

  @Test
  void classEvents() {
    check(factory.classCreated("Dog"), EventType.CLASS_CREATED, Map.of("className", "Dog"));
    check(factory.classModified("Dog"), EventType.CLASS_MODIFIED, Map.of("className", "Dog"));
    check(factory.classDeleted("Dog"), EventType.CLASS_DELETED, Map.of("className", "Dog"));
  }

  @Test
  void methodAdded() {
    check(factory.methodAdded("Dog", "bark()"), EventType.METHOD_ADDED,
        Map.of("className", "Dog", "method", "bark()"));
  }

  @Test
  void compileEvents() {
    check(factory.compileSuccess(), EventType.COMPILE_SUCCESS, Map.of());
    check(factory.compileError("Dog.java", 12, "';' expected"), EventType.COMPILE_ERROR,
        Map.of("file", "Dog.java", "line", 12, "message", "';' expected"));
  }

  @Test
  void runEvents() {
    check(factory.runFinished("Dog", "bark"), EventType.RUN_FINISHED,
        Map.of("className", "Dog", "method", "bark"));
    check(factory.runException("Dog", "bark", "NullPointerException"), EventType.RUN_EXCEPTION,
        Map.of("className", "Dog", "method", "bark", "exception", "NullPointerException"));
  }

  @Test
  void editAndIdleEvents() {
    check(factory.edit("Dog", -7), EventType.EDIT, Map.of("className", "Dog", "charsChanged", -7));
    check(factory.idleStarted(), EventType.IDLE_START, Map.of());
    check(factory.idleEnded(), EventType.IDLE_END, Map.of());
  }

  @Test
  void idleStartedAtUsesTheGivenTime() {
    TelemetryEvent event = factory.idleStartedAt(T - 60_000);

    assertEquals(EventType.IDLE_START, event.getEventType());
    assertEquals(T - 60_000, event.getTimestamp());
  }

  @Test
  void defaultConstructorUsesTheCurrentTime() {
    long before = System.currentTimeMillis();
    long stamp = new TelemetryEventFactory().compileSuccess().getTimestamp();
    long after = System.currentTimeMillis();

    assertTrue(stamp >= before && stamp <= after);
  }
}