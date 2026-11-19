package com.example;

import java.net.URL;
import java.util.List;
import com.example.bluej.ClassAdapter;
import com.example.bluej.CompileAdapter;
import com.example.bluej.EditorActivityPoller;
import com.example.bluej.ExportMenuGenerator;
import com.example.bluej.ExportPackageListener;
import com.example.bluej.InvocationAdapter;
import com.example.bluej.MethodAdapter;
import com.example.bluej.PackageAdapter;
import com.example.bluej.StudentIdPreferences;
import com.example.export.ExportService;
import com.example.export.JsonEventSerialiser;
import com.example.export.JsonReportExporter;
import com.example.export.PdfReportExporter;
import com.example.export.PdfReportMaker;
import com.example.export.SessionRecorder;
import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;

/**
 * @author Krece Hayes
 * @author Ubayd Razack
 *  BlueJ creates this class when it starts and calls {@link #startup(BlueJ)}, which builds every part of the plugin and connects them
 */
public class TelemetryExtension extends Extension {

  /** Creates the extension. BlueJ calls this when it loads the plugin, before {@link #startup(BlueJ)}. */
  public TelemetryExtension() {
  }

  /**
   * Builds and connects all parts of the plugin. The {@link ActivityMonitor} subscribes before the
   * {@link com.example.export.SessionRecorder} so an open idle period is closed before a session
   * ends.
   *
   * @param blueJ BlueJ's extension interface, used to register listeners, the menu and preferences
   */
  @Override
  public void startup(BlueJ blueJ) {
    TelemetryCollector collector = new TelemetryCollector();
    TelemetryEventFactory factory = new TelemetryEventFactory();


    ActivityMonitor activityMonitor = new ActivityMonitor(collector, factory);
    collector.subscribe(activityMonitor);
    new EditorActivityPoller(blueJ, activityMonitor).start();


    StudentIdPreferences studentIdPreferences = new StudentIdPreferences(blueJ);
    blueJ.setPreferenceGenerator(studentIdPreferences);
    SessionRecorder recorder = new SessionRecorder(System::currentTimeMillis,
        studentIdPreferences::getStudentId);
    collector.subscribe(recorder);
    ExportService exportService = new ExportService(List.of(
        new JsonReportExporter(new JsonEventSerialiser()),
        new PdfReportExporter(new PdfReportMaker())));

    blueJ.addCompileListener(new CompileAdapter(collector, factory));
    blueJ.addCompileListener(new MethodAdapter(blueJ, collector, factory, new MethodTracker()));
    blueJ.addPackageListener(new PackageAdapter(collector, factory));
    blueJ.addPackageListener(new ExportPackageListener(recorder, exportService));
    blueJ.addClassListener(new ClassAdapter(collector, factory));
    blueJ.addInvocationFinishedListener(new InvocationAdapter(collector, factory));
    blueJ.setMenuGenerator(new ExportMenuGenerator(recorder, exportService));
  }

  /**
   * Checks the plugin can run on this BlueJ version.
   *
   * @return true if BlueJ's extensions API is version 3 or later
   */
  @Override
  public boolean isCompatible() {
    return getExtensionsAPIVersionMajor() >= 3;
  }

  /**
   * Returns the plugin's version
   *
   * @return the version number
   */
  @Override
  public String getVersion() {
    return "1.0.0";
  }

  /**
   * Returns the plugin's name
   *
   * @return the plugin name
   */
  @Override
  public String getName() {
    return "BlueJ Telemetry";
  }

  /**
   * Returns a short description
   *
   * @return the description
   */
  @Override
  public String getDescription() {
    return "Collects coding telemetry and exports JSON/PDF";
  }

  /**
   * Returns a web page for the plugin
   *
   * @return null
   */
  @Override
  public URL getURL() {
    return null;
  }
}