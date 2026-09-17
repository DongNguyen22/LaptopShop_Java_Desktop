package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for the Employee screen during incremental migration. */
public final class EmployeeFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public EmployeeFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            EmployeeView view = new EmployeeView();
            Scene scene = new Scene(view.getRoot());

            URL css = EmployeeFxPanel.class.getResource("/GUI/JavaFX/employee.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/employee.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
