package APP;

import GUI.JavaFX.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Real JavaFX Application class.
 * APP.App is only a plain launcher so VS Code can run the project normally.
 */
public class FxApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Keep JavaFX alive while the legacy Swing dashboard is open so that
        // Logout can reopen the JavaFX login screen.
        Platform.setImplicitExit(false);
        new LoginView("No", "No").show(primaryStage);
    }
}
