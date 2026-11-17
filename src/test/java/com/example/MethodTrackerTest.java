package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;


class MethodTrackerTest {
  private final MethodTracker tracker = new MethodTracker();

  @Test
  void firstCompileReportsEveryMethodInOrder() {
    assertEquals(List.of("bark()", "getName()"),
        tracker.findNewMethods("Dog", List.of("getName()", "bark()")));
  }

  @Test
  void laterCompileReportsOnlyTheNewMethods() {
    tracker.findNewMethods("Dog", List.of("bark()"));

    assertEquals(List.of("sit()"), tracker.findNewMethods("Dog", List.of("bark()", "sit()")));
  }

  @Test
  void nothingNewGivesEmptyList() {
    tracker.findNewMethods("Dog", List.of("bark()"));

    assertEquals(List.of(), tracker.findNewMethods("Dog", List.of("bark()")));
  }

  @Test
  void classesAreTrackedSeparately() {
    tracker.findNewMethods("Dog", List.of("speak()"));

    assertEquals(List.of("speak()"), tracker.findNewMethods("Cat", List.of("speak()")));
  }

  @Test
  void aRemovedMethodCountsAsNewIfItComesBack() {
    tracker.findNewMethods("Dog", List.of("bark()"));
    tracker.findNewMethods("Dog", List.of());

    assertEquals(List.of("bark()"), tracker.findNewMethods("Dog", List.of("bark()")));
  }

  @Test
  void overloadsAreDifferentMethods() {
    tracker.findNewMethods("Dog", List.of("bark()"));

    assertEquals(List.of("bark(int)"), tracker.findNewMethods("Dog", List.of("bark()", "bark(int)")));
  }
}