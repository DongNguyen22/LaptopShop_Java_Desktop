package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for supplier management during incremental migration. */
public final class SupplierFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public SupplierFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            SupplierView view = new SupplierView();
            Scene scene = new Scene(view.getRoot());

            URL css = SupplierFxPanel.class.getResource("/GUI/JavaFX/supplier.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/supplier.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
