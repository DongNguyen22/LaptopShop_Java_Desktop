package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for the Product/Store screen during incremental migration. */
public final class ProductFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public ProductFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            ProductView view = new ProductView();
            Scene scene = new Scene(view.getRoot());

            URL css = ProductFxPanel.class.getResource("/GUI/JavaFX/product.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/product.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
