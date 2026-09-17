package APP;

import javafx.application.Application;

/**
 * Plain Java launcher for VS Code / IDE Run Java.
 *
 * IMPORTANT:
 * Do not make this class extend javafx.application.Application.
 * Running a JavaFX Application subclass directly with the standard Java
 * launcher can produce: "JavaFX runtime components are missing" when
 * JavaFX is supplied through Maven's classpath rather than the module path.
 *
 * 
 * Keep APP.App as the class you run. It delegates to FxApplication.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Application.launch(FxApplication.class, args);
    }
}
