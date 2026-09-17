package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for the Customer screen during incremental migration. */
public final class CustomerFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public CustomerFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            CustomerView view = new CustomerView();
            Scene scene = new Scene(view.getRoot());

            URL css = CustomerFxPanel.class.getResource("/GUI/JavaFX/customer.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/customer.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
