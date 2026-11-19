package com.example;

/**
 *  @author Krece Hayes
 * The kinds of student activity the plugin records.
 * Each {@link TelemetryEvent} has exactly one of these types.
 */
public enum EventType {
  /** A BlueJ project was opened. Data: {@code projectName}. */
  PROJECT_OPENED,
  /** A BlueJ project is closing. Data: {@code projectName}. */
  PROJECT_CLOSED,
  /** A class was seen for the first time in this BlueJ run. Data: {@code className}. */
  CLASS_CREATED,
  /** A class's compile state changed (edited, compiled, or renamed). Data: {@code className}. */
  CLASS_MODIFIED,
  /** A class was removed from the project. Data: {@code className}. */
  CLASS_DELETED,
  /** A method appeared in a class after a successful compile. Data: {@code className}, {@code method}. */
  METHOD_ADDED,
  /** An editor's text length changed since the last check. Data: {@code className}, {@code charsChanged}. */
  EDIT,
  /** The student became idle (no activity for the idle threshold). No data. */
  IDLE_START,
  /** The student became active again after being idle. No data. */
  IDLE_END,
  /** A compile started by the student succeeded. No data. */
  COMPILE_SUCCESS,
  /** A compile started by the student reported an error. Data: {@code file}, {@code line}, {@code message}. */
  COMPILE_ERROR,
  /** A method call made from BlueJ threw an exception. Data: {@code className}, {@code method}, {@code exception}. */
  RUN_EXCEPTION,
  /** A method call made from BlueJ finished normally. Data: {@code className}, {@code method}. */
  RUN_FINISHED
}
