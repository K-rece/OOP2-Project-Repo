package com.example.bluej;

import bluej.extensions2.BlueJ;
import bluej.extensions2.PreferenceGenerator;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


/**
 * Adds a "Telemetry student ID" text input box to BlueJ's Tools -> Preferences -> Extensions panel.
 */
public class StudentIdPreferences implements PreferenceGenerator {

  /** Student ID is stored in this. */
  private static final String PROPERTY_KEY = "Telemetry-Student-ID";

  /** BlueJ's extension interface, used to load and save the setting. */
  private final BlueJ blueJ;
  /** The panel shown in BlueJ's preferences window. */
  private final Pane pane = new Pane();
  /** The input box the student types their ID into. */
  private final TextField studentIdField = new TextField();

  /**
   * Builds the panel and fills in the saved student ID.
   *
   * @param blueJ BlueJ's extension interface
   */
  public StudentIdPreferences(BlueJ blueJ) {
    this.blueJ = blueJ;
    HBox row = new HBox(8); 
    row.getChildren().add(new Label("Telemetry student ID:"));
    row.getChildren().add(studentIdField);
    pane.getChildren().add(row);
    loadValues();
  }

  /**
   * Returns the saved student ID.
   *
   * @return the student ID, or an empty string if none has been entered
   */
  public String getStudentId() {
    return blueJ.getExtensionPropertyString(PROPERTY_KEY, "");
  }

  /**
   * Called by BlueJ to get the panel to show in the preferences window.
   *
   * @return the student ID panel
   */
  @Override
  public Pane getWindow() {
    return pane;
  }

  /**
   * Called by BlueJ when the student clicks OK. Saves the entered ID, without surrounding spaces.
   */
  @Override
  public void saveValues() {
    blueJ.setExtensionPropertyString(PROPERTY_KEY, studentIdField.getText().trim());
  }

  /** Called by BlueJ when the preferences window opens. Shows the saved ID in the box. */
  @Override
  public void loadValues() {
    studentIdField.setText(getStudentId());
  }
}
