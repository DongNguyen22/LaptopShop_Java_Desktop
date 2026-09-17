package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for warranty management during incremental migration. */
public final class WarrantyFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public WarrantyFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            WarrantyView view = new WarrantyView();
            Scene scene = new Scene(view.getRoot());
            URL css = WarrantyFxPanel.class.getResource("/GUI/JavaFX/warranty.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/warranty.css");
            }
            fxPanel.setScene(scene);
        });
    }
}
