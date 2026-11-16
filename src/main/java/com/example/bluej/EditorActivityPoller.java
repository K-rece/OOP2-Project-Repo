package com.example.bluej;

import java.util.HashMap;
import java.util.Map;
import com.example.ActivityMonitor;
import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.editor.JavaEditor;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

public class EditorActivityPoller {
    
  private static final double POLL_SECONDS = 5;

  private final BlueJ blueJ;
  private final ActivityMonitor monitor;

  public EditorActivityPoller(BlueJ blueJ, ActivityMonitor monitor) {
    this.blueJ = blueJ;
    this.monitor = monitor;
  }

  public void start() {
    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(POLL_SECONDS), event -> pollOnce()));
    timeline.setCycleCount(Animation.INDEFINITE);
    Platform.runLater(timeline::play);
  }

  private void pollOnce() {
    try {
      monitor.poll(readTextLengths());
    } catch (Exception e) {
      System.out.println("Telemetry: editor poll error: " + e.getMessage());
    }
  }

  private Map<String, Integer> readTextLengths() throws Exception {
    Map<String, Integer> lengths = new HashMap<>();
    for (BProject project : blueJ.getOpenProjects()) {
      for (BPackage bPackage : project.getPackages()) {
        for (BClass bClass : bPackage.getClasses()) {
          JavaEditor editor = bClass.getJavaEditor();
          if (editor != null && editor.isVisible()) {
            lengths.put(bClass.getName(), editor.getTextLength());
          }
        }
      }
    }
    return lengths;
  }
}