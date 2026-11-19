package com.example.bluej;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;
import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

/**
 * @author Krece Hayes
 * Adapter: converts BlueJ's "method call finished" events (when the student runs a method from
 * BlueJ's interface) into {@link com.example.EventType#RUN_FINISHED} or {@link
 * com.example.EventType#RUN_EXCEPTION} events.
 */
public class InvocationAdapter implements InvocationFinishedListener {
  /** This is where the converted events are published. */
  private final TelemetryCollector collector;
  /** Creates the plugin's events. */
  private final TelemetryEventFactory factory;

  /**
   * Creates the adapter.
   *
   * @param collector where run events are published
   * @param factory creates the events
   */
  public InvocationAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  /**
   * Called by BlueJ when a method call finishes, and puublishes RUN_EXCEPTION if it ended with an exception, and RUN_FINISHED otherwise.
   *
   * @param event BlueJ's description of the finished call
   */
  @Override
  public void invocationFinished(InvocationFinishedEvent event) {
    try {
      String cls = String.valueOf(event.getClassName());
      String method = String.valueOf(event.getMethodName());
      String type = String.valueOf(event.getEventType());

      if (type.contains("EXCEPTION")) {
        collector.publish(factory.runException(cls, method, String.valueOf(event.getResult())));
      } else {
        collector.publish(factory.runFinished(cls, method));
      }
    } catch (Exception e) {
      System.out.println("Telemetry: invocation event error: " + e.getMessage());
    }
  }
}