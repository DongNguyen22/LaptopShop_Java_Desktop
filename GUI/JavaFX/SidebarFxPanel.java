package GUI.JavaFX;

import java.awt.Dimension;
import java.util.function.IntConsumer;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import valiateData.PermissionManager;

/**
 * Cầu nối Sidebar JavaFX với Dashboard Swing.
 */
public final class SidebarFxPanel extends JFXPanel {

    public static final int EXPANDED_WIDTH = 280;
    public static final int COLLAPSED_WIDTH = 76;

    private volatile SidebarView view;
    private volatile boolean collapsed;
    private Timer animationTimer;
    private Consumer<Boolean> collapsedListener = value -> { };

    public SidebarFxPanel(
            String userName,
            String role,
            PermissionManager permissions,
            IntConsumer onNavigate,
            Runnable onLogout) {
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setMinimumSize(new Dimension(COLLAPSED_WIDTH, 0));
        setOpaque(false);

        Platform.runLater(() -> {
            view = new SidebarView(
                    userName,
                    role,
                    permissions,
                    onNavigate,
                    onLogout,
                    this::toggleCollapsed);
            Scene scene = new Scene(view, EXPANDED_WIDTH, 700, Color.TRANSPARENT);
            String css = SidebarFxPanel.class.getResource("sidebar.css") == null
                    ? null
                    : SidebarFxPanel.class.getResource("sidebar.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }
            setScene(scene);
        });
    }

    public void toggleCollapsed() {
        setCollapsed(!collapsed);
    }

    public void setCollapsed(boolean value) {
        if (collapsed == value) {
            return;
        }
        collapsed = value;
        Platform.runLater(() -> {
            if (view != null) {
                view.setCollapsed(value);
            }
        });
        animateWidth(value ? COLLAPSED_WIDTH : EXPANDED_WIDTH);
        collapsedListener.accept(value);
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setOnCollapsedChanged(Consumer<Boolean> listener) {
        collapsedListener = listener == null ? value -> { } : listener;
        collapsedListener.accept(collapsed);
    }

    public void setActiveKind(int kind) {
        Platform.runLater(() -> {
            if (view != null) {
                view.setActiveKind(kind);
            }
        });
    }

    private void animateWidth(int target) {
        Runnable start = () -> {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            final int direction = target < getPreferredSize().width ? -1 : 1;
            animationTimer = new Timer(10, e -> {
                int current = getPreferredSize().width;
                int distance = Math.abs(target - current);
                int step = Math.max(10, distance / 4);
                int next = current + direction * step;
                boolean finished = direction < 0 ? next <= target : next >= target;
                if (finished) {
                    next = target;
                    animationTimer.stop();
                }
                setPreferredSize(new Dimension(next, 0));
                if (getParent() instanceof JComponent) {
                    JComponent parent = (JComponent) getParent();
                    parent.revalidate();
                    parent.repaint();
                } else {
                    revalidate();
                    repaint();
                }
            });
            animationTimer.start();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            start.run();
        } else {
            SwingUtilities.invokeLater(start);
        }
    }
}
