package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for product category management. */
public final class CategoryFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public CategoryFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            CategoryView view = new CategoryView();
            Scene scene = new Scene(view.getRoot());

            URL css = CategoryFxPanel.class.getResource("/GUI/JavaFX/category.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/category.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
