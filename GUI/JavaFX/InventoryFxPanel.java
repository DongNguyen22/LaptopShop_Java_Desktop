package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for the inventory/warehouse screen during incremental migration. */
public final class InventoryFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public InventoryFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(this::createScene);
    }

    private void createScene() {
        InventoryView view = new InventoryView();
        Scene scene = new Scene(view.getRoot());
        URL css = InventoryFxPanel.class.getResource("/GUI/JavaFX/inventory.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/inventory.css");
        }
        fxPanel.setScene(scene);
    }
}
