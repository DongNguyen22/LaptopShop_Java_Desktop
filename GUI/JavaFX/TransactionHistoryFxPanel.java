package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for transaction/order history during incremental migration. */
public final class TransactionHistoryFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public TransactionHistoryFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(this::createScene);
    }

    private void createScene() {
        TransactionHistoryView view = new TransactionHistoryView();
        Scene scene = new Scene(view.getRoot());
        URL css = TransactionHistoryFxPanel.class.getResource("/GUI/JavaFX/transaction-history.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/transaction-history.css");
        }
        fxPanel.setScene(scene);
    }
}
