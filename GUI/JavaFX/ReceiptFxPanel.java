package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for receipt management during incremental migration. */
public final class ReceiptFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public ReceiptFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            ReceiptView view = new ReceiptView();
            Scene scene = new Scene(view.getRoot());
            URL css = ReceiptFxPanel.class.getResource("/GUI/JavaFX/receipt.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/receipt.css");
            }
            fxPanel.setScene(scene);
        });
    }
}
