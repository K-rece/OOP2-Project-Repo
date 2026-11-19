package com.example.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;


 /** @author Justin Craigwell */
class SessionInfoTest {

  @Test
  void durationIsEndMinusStart() {
    SessionInfo session = new SessionInfo("s-1", "STUDENT_ID", "Shapes", 1000, 61_000);

    assertEquals(60_000, session.getDurationMillis());
  }

  @Test
  void zeroLengthSessionIsAllowed() {
    SessionInfo session = new SessionInfo("s-1", "STUDENT_ID", "Shapes", 5000, 5000);

    assertEquals(0, session.getDurationMillis());
  }

  @Test
  void endBeforeStartIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new SessionInfo("s-1", "STUDENT_ID", "Shapes", 5000, 1000));
  }
}
