package GUI_Controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import GUI.JavaFX.AccountFxPanel;
import GUI.JavaFX.CustomerFxPanel;
import GUI.JavaFX.CategoryFxPanel;
import GUI.JavaFX.EmployeeFxPanel;
import GUI.JavaFX.HomeFxPanel;
import GUI.JavaFX.InventoryFxPanel;
import GUI.JavaFX.OrderFxPanel;
import GUI.JavaFX.PermissionFxPanel;
import GUI.JavaFX.ProductFxPanel;
import GUI.JavaFX.PromotionFxPanel;
import GUI.JavaFX.ReceiptFxPanel;
import GUI.JavaFX.StatisticsFxPanel;
import GUI.JavaFX.SupplierFxPanel;
import GUI.JavaFX.TransactionHistoryFxPanel;
import GUI.JavaFX.WarrantyFxPanel;
import danhMucBean.danhMuc;

public class chuyenManHinhController {
    private final JPanel root;
    private ArrayList<danhMuc> listItem;
    private JPanel currentActiveItem = null;
    private IntConsumer onScreenChanged = kind -> { };

    // Đồng bộ palette với sidebar mới trong dashBoard.
    private final Color parentColor = new Color(17, 24, 39);
    private final Color childColor = new Color(24, 34, 53);
    private final Color hoverColor = new Color(31, 41, 55);
    private final Color activeColor = new Color(30, 64, 175);
    private final Color borderActive = new Color(96, 165, 250);

    public chuyenManHinhController(JPanel root) {
        this.root = root;
    }

    public void setOnScreenChanged(IntConsumer listener) {
        onScreenChanged = listener == null ? kind -> { } : listener;
    }

    public void setInitialActive(JPanel selectedItem) {
        if (selectedItem != null) {
            setActive(selectedItem);
        }
    }

    private boolean isChildPanel(JPanel panel) {
        Object value = panel.getClientProperty("isChild");
        return value instanceof Boolean && (Boolean) value;
    }

    private void setNormalStyle(JPanel panel) {
        boolean isChild = isChildPanel(panel);
        panel.setBackground(isChild ? childColor : parentColor);
        panel.setBorder(BorderFactory.createEmptyBorder(
                8,
                isChild ? 30 : 14,
                8,
                12));
    }

    private void setActive(JPanel selectedItem) {
        if (currentActiveItem != null && currentActiveItem != selectedItem) {
            setNormalStyle(currentActiveItem);
        }

        boolean isChild = isChildPanel(selectedItem);
        selectedItem.setBackground(activeColor);
        selectedItem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, borderActive),
                BorderFactory.createEmptyBorder(
                        8,
                        isChild ? 26 : 10,
                        8,
                        12)));

        currentActiveItem = selectedItem;
    }

    public void setView() {
        showScreen(0);
    }

    /**
     * Điều hướng tới màn hình theo kind. Phương thức public này được dùng bởi
     * cả sidebar Swing cũ và sidebar JavaFX mới để giữ nguyên mapping module.
     */
    public void showScreen(int kind) {
        JPanel node;
        int resolvedKind = kind;
        switch (kind) {
            case 0:
                node = new HomeFxPanel();
                break;
            case 1:
                node = new CategoryFxPanel();
                break;
            case 2:
                node = new SupplierFxPanel();
                break;
            case 3:
                node = new PromotionFxPanel();
                break;
            case 4:
                node = new CustomerFxPanel();
                break;
            case 5:
                node = new EmployeeFxPanel();
                break;
            case 6:
                node = new WarrantyFxPanel();
                break;
            case 7:
                node = new ProductFxPanel();
                break;
            case 8:
                node = new InventoryFxPanel();
                break;
            case 9:
                node = new ReceiptFxPanel();
                break;
            case 10:
                node = new OrderFxPanel();
                break;
            case 11:
                node = new TransactionHistoryFxPanel();
                break;
            case 12:
                node = new AccountFxPanel();
                break;
            case 13:
                node = new PermissionFxPanel();
                break;
            case 14:
                node = new StatisticsFxPanel();
                break;
            default:
                node = new HomeFxPanel();
                resolvedKind = 0;
                break;
        }

        root.removeAll();
        root.setLayout(new BorderLayout());
        root.add(node, BorderLayout.CENTER);
        root.revalidate();
        root.repaint();
        onScreenChanged.accept(resolvedKind);
    }

    public void setEvent(ArrayList<danhMuc> listItem) {
        this.listItem = listItem;
        for (danhMuc item : listItem) {
            item.getjPanel().addMouseListener(new PanelEvent(item.getjPanel(), item.getKind()));
        }
    }

    private class PanelEvent implements MouseListener {
        private final JPanel jpnItem;
        private final int kind;

        PanelEvent(JPanel jpnItem, int kind) {
            this.jpnItem = jpnItem;
            this.kind = kind;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            setActive(jpnItem);
            showScreen(kind);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (jpnItem != currentActiveItem) {
                jpnItem.setBackground(hoverColor);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (jpnItem != currentActiveItem) {
                boolean isChild = isChildPanel(jpnItem);
                jpnItem.setBackground(isChild ? childColor : parentColor);
            }
        }
    }
}
