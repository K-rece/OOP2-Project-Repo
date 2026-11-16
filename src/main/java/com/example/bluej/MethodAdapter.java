package com.example.bluej;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.MethodTracker;
import com.example.TelemetryCollector;
import com.example.TelemetryEventFactory;
import bluej.extensions2.BClass;
import bluej.extensions2.BMethod;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;


public class MethodAdapter implements CompileListener {
    
  private final BlueJ blueJ;
  private final TelemetryCollector collector;
  private final TelemetryEventFactory factory;
  private final MethodTracker tracker;

  public MethodAdapter(BlueJ blueJ, TelemetryCollector collector, TelemetryEventFactory factory,
      MethodTracker tracker) {
    this.blueJ = blueJ;
    this.collector = collector;
    this.factory = factory;
    this.tracker = tracker;
  }

  @Override
  public void compileSucceeded(CompileEvent event) {
    if (!event.isUserGeneratedCompilation()) {
      return; 
    }
    for (File file : event.getFiles()) {
      try {
        publishNewMethods(file);
      } catch (Exception e) {
        System.out.println("Telemetry: method scan error: " + e.getMessage());
      }
    }
  }

  private void publishNewMethods(File javaFile) throws Exception {
    BClass bClass = findClass(javaFile);
    if (bClass == null) {
      return;
    }
    List<String> signatures = new ArrayList<>();
    for (BMethod method : bClass.getDeclaredMethods()) {
      if (!method.getName().contains("$")) { 
        signatures.add(signature(method));
      }
    }
    String className = bClass.getName();
    for (String added : tracker.findNewMethods(className, signatures)) {
      collector.publish(factory.methodAdded(className, added));
    }
  }

  private BClass findClass(File javaFile) throws Exception {
    String fileName = javaFile.getName();
    if (!fileName.endsWith(".java")) {
      return null;
    }
    String className = fileName.substring(0, fileName.length() - ".java".length());
    BClass sameName = null;
    for (BProject project : blueJ.getOpenProjects()) {
      for (BPackage bPackage : project.getPackages()) {
        BClass candidate = bPackage.getBClass(className);
        if (candidate == null) {
          continue;
        }
        if (bPackage.getDir().equals(javaFile.getParentFile())) {
          return candidate; 
        }
        sameName = candidate;
      }
    }
    return sameName;
  }


  private String signature(BMethod method) {
    StringBuilder sb = new StringBuilder(method.getName()).append("(");
    Object[] parameters = method.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      Object p = parameters[i];
      sb.append(p instanceof Class ? ((Class<?>) p).getSimpleName() : String.valueOf(p));
    }
    return sb.append(")").toString();
  }

  @Override
  public void compileStarted(CompileEvent event) {
  }

  @Override
  public void compileError(CompileEvent event) {
  }

  @Override
  public void compileWarning(CompileEvent event) {
  }

  @Override
  public void compileFailed(CompileEvent event) {
  }
}