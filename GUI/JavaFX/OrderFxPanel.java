package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for order management during incremental migration. */
public final class OrderFxPanel extends JPanel {
    private final JFXPanel fxPanel;

    public OrderFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(this::createScene);
    }

    private void createScene() {
        OrderView view = new OrderView();
        Scene scene = new Scene(view.getRoot());
        URL css = OrderFxPanel.class.getResource("/GUI/JavaFX/order.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/order.css");
        }
        fxPanel.setScene(scene);
    }
}
