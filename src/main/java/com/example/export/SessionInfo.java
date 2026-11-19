package com.example.export;

/** The details that identify one coding session: who, which project, and when. */
public class SessionInfo {

  /** Student ID used when the student hasn;t entered one in BlueJ's preferences. */
  public static final String STUDENT_ID_PLACEHOLDER = "STUDENT_ID";

  /** Unique ID for this session. */
  private final String sessionId;
  /** The student's ID, or {@link #STUDENT_ID_PLACEHOLDER}. */
  private final String studentId;
  /** The BlueJ project worked on. */
  private final String projectName;
  /** When the session started, in milliseconds */
  private final long startMillis;
  /** When the session ended, in milliseconds */
  private final long endMillis;

  /**
   * Creates the session details.
   *
   * @param sessionId unique ID for the session
   * @param studentId the student's ID
   * @param projectName the BlueJ project's name
   * @param startMillis when the session started
   * @param endMillis when the session ended
   * @throws IllegalArgumentException if the end is before the start
   */
  public SessionInfo(String sessionId, String studentId, String projectName, long startMillis, long endMillis) {

    if (endMillis < startMillis) {
      throw new IllegalArgumentException("Session cannot end before it starts");
    }
    
    this.sessionId = sessionId;
    this.studentId = studentId;
    this.projectName = projectName;
    this.startMillis = startMillis;
    this.endMillis = endMillis;
  }

  /**
   * Returns the session's unique ID.
   *
   * @return the session's unique ID
   */
  public String getSessionId() { return sessionId; }
  /**
   * Returns the student's ID, or the placeholder.
   *
   * @return the student's ID, or the placeholder
   */
  public String getStudentId() { return studentId; }
  /**
   * Returns the BlueJ project's name.
   *
   * @return the BlueJ project's name
   */
  public String getProjectName() { return projectName; }
  /**
   * Returns when the session started, in milliseconds since 1 January 1970 (UTC).
   *
   * @return when the session started, in milliseconds since 1 January 1970 (UTC)
   */
  public long getStartMillis() { return startMillis; }
  /**
   * Returns when the session ended, in milliseconds since 1 January 1970 (UTC).
   *
   * @return when the session ended, in milliseconds since 1 January 1970 (UTC)
   */
  public long getEndMillis() { return endMillis; }

  /**
   * Returns how long the session lasted.
   *
   * @return the duration in milliseconds
   */
  public long getDurationMillis() {
    return endMillis - startMillis;
  }
  
}
