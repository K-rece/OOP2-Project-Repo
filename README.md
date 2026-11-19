# OOP2-Project-Repo

COMP 3607 - OOP2 Project: A BlueJ data analysis extension plugin for collecting, formatting and analysing telemetry data produced using the BlueJ coding editor.

## Prerequisites

- JDK 17 or newer, with JAVA_HOME set to the JDK folder
- Apache Maven 3.9 or newer, with the bin folder on your PATH

## Setup 

1. Clone the repository and open a terminal in its folder:
```
git clone https://github.com/K-rece/OOP2-Project-Repo.git
cd OOP2-Project-Repo
```

2. Install BlueJ
3. From the repository folder, run:

On Windows: 
```
mvn install:install-file "-Dfile=C:\Program Files\BlueJ\lib\bluej.jar" "-DgroupId=org.bluej" "-DartifactId=bluej" "-Dversion=5.0" "-Dpackaging=jar"
```

On macOS:
```
mvn install:install-file "-Dfile=/Applications/BlueJ.app/Contents/Resources/Java/bluej.jar" "-DgroupId=org.bluej" "-DartifactId=bluej" "-Dversion=5.0" "-Dpackaging=jar"
```

(If BlueJ is installed somewhere other than the location in this command, change the path to that version's lib\bluej.jar)

4. If you use VS Code: press Ctrl+Shift+P and run Java: Clean Java Language Server Workspace, then restart.

## Production

1. To test the functionality of the code, run: 
```
mvn test
```

2. To build the plugin, run:

```
mvn package
```

3. After building the plugin, copy **target/oop2-bluej-project-1.0.0.jar** into BlueJ's extensions folder which is located in:

- Windows: **C:\Program Files\BlueJ\lib\extensions2**
- macOS: **BlueJ.app/Contents/Resources/Java/extensions2**

4. Then, open or make a new project within BlueJ and begin your session. Recording of the session happens automatically.

5. At any point during recording, export by going from **Tools -> Export Telemetry Report.**

### Notes
- You can optionally enter your Student ID by navigating to the Extensions page via **Tools -> Preferences -> Extensions**
- To uninstall plugin, delete the jar file from the extensions2 folder.
- While it's installed, every project opened in BlueJ gets telemetry files.
- You can check if the extension is being seen by BlueJ via **Help -> Installed Extensions**.
