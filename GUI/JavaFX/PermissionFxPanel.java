package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for role/permission management during incremental migration. */
public final class PermissionFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public PermissionFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            PermissionView view = new PermissionView();
            Scene scene = new Scene(view.getRoot());

            URL css = PermissionFxPanel.class.getResource("/GUI/JavaFX/permission.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/permission.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
