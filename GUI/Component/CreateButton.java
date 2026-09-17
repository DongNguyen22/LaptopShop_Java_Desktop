package GUI.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CreateButton extends JButton {

    private Color bg = new Color(66, 90, 115);

    public CreateButton(String text ) {
        super(text);

        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setBackground(bg);
        setForeground(Color.WHITE);

        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(bg.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(bg);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(bg.darker().darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(bg.darker());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Không vẽ border
    }
}