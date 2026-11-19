package com.example.bluej;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;
import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

/**
 * @author Krece Hayes
 * Adapter: converts BlueJ's compile events into {@link com.example.EventType#COMPILE_SUCCESS} and
 * {@link com.example.EventType#COMPILE_ERROR} events. Only compiles the student starts are
 * recorded; BlueJ's automatic background error checks are ignored, so the counts match what the
 * student did.
 */
public class CompileAdapter implements CompileListener {

  /** This is where the converted events are published. */
  private final TelemetryCollector collector;
  /** Creates the plugin's events. */
  private final TelemetryEventFactory factory;

  /**
   * Creates the adapter.
   *
   * @param collector where compile events are published
   * @param factory creates the events
   */
  public CompileAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  /**
   * Called by BlueJ when there is a compile error, sends a COMPILE_ERROR with the file, line
   * number and message, unless the compile was BlueJ's own background check.
   *
   * @param e BlueJ's description of the error
   */
  @Override
  public void compileError(CompileEvent e) {
    if (!e.isUserGeneratedCompilation()) {
      return;
    }
    collector.publish(factory.compileError(e.getFiles().length > 0 ? e.getFiles()[0].getName() : "unknown", e.getErrorLineNumber(), e.getErrorMessage()));
  }

  /**
   * Called by BlueJ when a compile succeeds, sends a COMPILE_SUCCESS, unless the compile was
   * BlueJ's own background check.
   *
   * @param e BlueJ's description of the compile
   */
  @Override
  public void compileSucceeded(CompileEvent e) {
    if (!e.isUserGeneratedCompilation()) {
      return;
    }
    collector.publish(factory.compileSuccess());
  }

  /**
   * Not recorded: a failed compile is already recorded through {@link
   * #compileError(CompileEvent)}, so recording it here would count it twice.
   *
   * @param arg0 BlueJ's description of the compile
   */
  @Override
  public void compileFailed(CompileEvent arg0) {

  }

  /**
   * Not recorded: attempts are counted from their outcome (success or error).
   *
   * @param arg0 BlueJ's description of the compile
   */
  @Override
  public void compileStarted(CompileEvent arg0) {

  }

  /**
   * Not recorded: warnings arent apart of the report.
   *
   * @param arg0 BlueJ's description of the warning
   */
  @Override
  public void compileWarning(CompileEvent arg0) {
    
  }
}
