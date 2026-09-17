package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for promotion management during incremental migration. */
public final class PromotionFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public PromotionFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            PromotionView view = new PromotionView();
            Scene scene = new Scene(view.getRoot());

            URL css = PromotionFxPanel.class.getResource("/GUI/JavaFX/promotion.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/promotion.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
