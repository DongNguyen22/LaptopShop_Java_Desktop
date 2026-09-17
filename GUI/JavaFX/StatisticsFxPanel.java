package GUI.JavaFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

/** Swing -> JavaFX bridge for statistics during incremental migration. */
public final class StatisticsFxPanel extends JPanel {

    private final JFXPanel fxPanel;

    public StatisticsFxPanel() {
        super(new BorderLayout());
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(this::createScene);
    }

    private void createScene() {
        StatisticsView view = new StatisticsView();
        Scene scene = new Scene(view.getRoot());
        URL css = StatisticsFxPanel.class.getResource("/GUI/JavaFX/statistics.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("Không tìm thấy CSS JavaFX: /GUI/JavaFX/statistics.css");
        }
        fxPanel.setScene(scene);
    }
}
