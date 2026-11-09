package com.example.bluej;

import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

public class CompileAdapter implements CompileListener {
  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;

  public CompileAdapter(TelemetryCollector collector, TelemetryEventFactory factory) {
    this.collector = collector;
    this.factory = factory;
  }

  @Override
  public void compileError(CompileEvent e) {
    collector.publish(factory.compileError(
        e.getFiles().length > 0 ? e.getFiles()[0].getName() : "unknown",
        e.getErrorLineNumber(), e.getErrorMessage()));
  }

  @Override
  public void compileSucceeded(CompileEvent e) {
    collector.publish(factory.compileSuccess());
  }

  @Override
  public void compileFailed(CompileEvent arg0) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'compileFailed'");
  }

  @Override
  public void compileStarted(CompileEvent arg0) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'compileStarted'");
  }

  @Override
  public void compileWarning(CompileEvent arg0) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'compileWarning'");
  }
}
