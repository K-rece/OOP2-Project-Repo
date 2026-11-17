package com.example.export;

public class SessionInfo {

  public static final String STUDENT_ID_PLACEHOLDER = "STUDENT_ID";

  private final String sessionId;
  private final String studentId;
  private final String projectName;
  private final long startMillis;
  private final long endMillis;

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

  public String getSessionId() { return sessionId; }
  public String getStudentId() { return studentId; }
  public String getProjectName() { return projectName; }
  public long getStartMillis() { return startMillis; }
  public long getEndMillis() { return endMillis; }

  public long getDurationMillis() {
    return endMillis - startMillis;
  }
  
}
