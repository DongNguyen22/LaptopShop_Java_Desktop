package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for account management during incremental migration. */
public final class AccountFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public AccountFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            AccountView view = new AccountView();
            Scene scene = new Scene(view.getRoot());

            URL css = AccountFxPanel.class.getResource("/GUI/JavaFX/account.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/account.css");
            }

            fxPanel.setScene(scene);
        });
    }
}
