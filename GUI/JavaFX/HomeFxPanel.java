package GUI.JavaFX;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

/**
 * Small Swing -> JavaFX bridge used while the application is migrated one
 * screen at a time. Dashboard remains Swing; its content area can host this
 * panel like any other JPanel.
 */
public final class HomeFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public HomeFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        createScene();
    }

    private void createScene() {
        Platform.runLater(() -> {
            HomeView view = new HomeView();
            Scene scene = new Scene(view.getRoot());

            URL css = HomeView.class.getResource("/GUI/JavaFX/home.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/home.css");
            }

            fxPanel.setScene(scene);
        });
    }

    /**
     * Convenience helper if a caller ever needs to create this bridge from a
     * non-Swing thread.
     */
    public static void showIn(JPanel root) {
        SwingUtilities.invokeLater(() -> {
            root.removeAll();
            root.setLayout(new BorderLayout());
            root.add(new HomeFxPanel(), BorderLayout.CENTER);
            root.revalidate();
            root.repaint();
        });
    }
}
