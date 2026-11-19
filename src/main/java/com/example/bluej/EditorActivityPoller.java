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

/**
 * @author Ubayd Razack
 * Every few seconds, reads the text length of each open editor and passes the numbers to the
 * {@link ActivityMonitor}, which decides what counts as an edit or idle time. Uses a JavaFX {@link
 * Timeline} so the editors are only read on BlueJ's own user-interface thread.
 */
public class EditorActivityPoller {
    
  /** How often the editors are checked (seconds) */
  private static final double POLL_SECONDS = 5;

  /** BlueJ's extension interface, which is used to find the open editors. */
  private final BlueJ blueJ;
  /** Receives the editor lengths and then decides what they mean. */
  private final ActivityMonitor monitor;

  /**
   * Creates the poller. Call {@link #start()} to begin checking.
   *
   * @param blueJ BlueJ's extension interface
   * @param monitor receives each check's editor lengths
   */
  public EditorActivityPoller(BlueJ blueJ, ActivityMonitor monitor) {
    this.blueJ = blueJ;
    this.monitor = monitor;
  }

  /**
   * Starts checking the open editors every {@value #POLL_SECONDS} seconds, for as long as BlueJ
   * runs.
   */
  public void start() {
    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(POLL_SECONDS), event -> pollOnce()));
    timeline.setCycleCount(Animation.INDEFINITE);
    Platform.runLater(timeline::play);
  }

  /**
   * Reads the editors once and passes the lengths to the monitor. The errors are then logged. 
   */
  private void pollOnce() {
    try {
      monitor.poll(readTextLengths());
    } catch (Exception e) {
      System.out.println("Telemetry: editor poll error: " + e.getMessage());
    }
  }

  /**
   * Gets the text length of every open, visible editor in every open project.
   *
   * @return each open editor's text length, by class name
   * @throws Exception if BlueJ cannot list the projects, packages or classes
   */
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