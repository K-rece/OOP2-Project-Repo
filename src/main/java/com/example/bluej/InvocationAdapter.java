package com.example.bluej;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;

import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

public class InvocationAdapter implements InvocationFinishedListener {
  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;

  public InvocationAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

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