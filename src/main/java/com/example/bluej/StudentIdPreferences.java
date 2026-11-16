package com.example.bluej;

import bluej.extensions2.BlueJ;
import bluej.extensions2.PreferenceGenerator;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


public class StudentIdPreferences implements PreferenceGenerator {

  private static final String PROPERTY_KEY = "Telemetry-Student-ID";

  private final BlueJ blueJ;
  private final Pane pane = new Pane();
  private final TextField studentIdField = new TextField();

  public StudentIdPreferences(BlueJ blueJ) {
    this.blueJ = blueJ;
    HBox row = new HBox(8); 
    row.getChildren().add(new Label("Telemetry student ID:"));
    row.getChildren().add(studentIdField);
    pane.getChildren().add(row);
    loadValues();
  }

  public String getStudentId() {
    return blueJ.getExtensionPropertyString(PROPERTY_KEY, "");
  }

  @Override
  public Pane getWindow() {
    return pane;
  }

  @Override
  public void saveValues() {
    blueJ.setExtensionPropertyString(PROPERTY_KEY, studentIdField.getText().trim());
  }

  @Override
  public void loadValues() {
    studentIdField.setText(getStudentId());
  }
}
