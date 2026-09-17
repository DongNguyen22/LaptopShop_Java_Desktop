package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Bus.AccountBUS;
import DTO.AccountDTO;
import DTO.UserSession;
import GUI.JavaFX.LoginView;
import GUI.JavaFX.HomeFxPanel;
import GUI.JavaFX.SidebarFxPanel;
import danhMucBean.danhMuc;
import valiateData.PermissionManager;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Map;
import GUI_Controller.chuyenManHinhController;

public class dashBoard extends JFrame {

    private JPanel jpnTitle, jpnMenu, jpnContent, jpnBody, jpnSidebar, jpnMain;
    private JPanel jpnIconMenu, jpnTitleLeft, jpnTitleRight;
    private JLabel lblMenuIcon, lblMenuTitle;
    private JLabel lblHello, lblUserName, lblRole;
    private JPanel jpnHome, jpnQlyChung, jpnQlySanPham, jpnQlyDonHang, jpnQlyTaiKhoan, jpnDoanhThu;
    private JPanel jpnSanPhamSplit, jpnNCC, jpnKhuyenMai, jpnDvt;
    private JPanel jpnsanPhamStore, jpnSanPhamStock, jpnPhieuNhap;
    private JPanel jpnDonHang, jpnLichSuDonHang;
    private JPanel jpnTaiKhoan, jpnPhanQuyen;
    private JPanel jpnThongKe, jpnBaoHanh;
    private JPanel jpnNhanVien;
    private JPanel jpnExit;
    private AccountDTO account;
    // Modern sidebar palette
    private final Color sidebarColor = new Color(17, 24, 39);      // #111827
    private final Color parentColor = new Color(17, 24, 39);
    private final Color childColor = new Color(24, 34, 53);
    private final Color hoverColor = new Color(31, 41, 55);
    private final Color activeColor = new Color(30, 64, 175);      // #1E40AF
    private final Color accentColor = new Color(96, 165, 250);     // #60A5FA
    private final Color textMuted = new Color(156, 163, 175);
    private final Color bgContent = new Color(243, 244, 246);
    private final Color borderItem = new Color(38, 50, 71);
    private final Dimension menuSize = new Dimension(280, 52);
    private final int SIDEBAR_WIDTH = 280;
    private Timer timer;
    private boolean isVisible = true;
    private PermissionManager pm = new PermissionManager();
    private String rememberUsername, rememberPassword;
    private JPanel pnlQlyChung, pnlQlySanPham, pnlQlyDonHang, pnlQlyTaiKhoan, pnlQlyDoanhThu;
    private SidebarFxPanel fxSidebar;
    private chuyenManHinhController navigationController;

    public dashBoard(String rememberPassword, String rememberUsername) {
        this.account = UserSession.getInstance().getAccount();
        this.rememberPassword = rememberPassword;
        this.rememberUsername = rememberUsername;
        init();
        initEventController();
        this.setVisible(true);
    }

    public void init() {
        this.setTitle("Quản Lý Bán Hàng");
        this.setSize(1400, 800);
        this.setMinimumSize(new Dimension(1180, 650));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        /* ACCOUNT INFO + JAVAFX TOPBAR */
        AccountBUS accountBUS = new AccountBUS();
        Map<String, String> mapAccount = accountBUS.getUserInfo(account.getUsername());

        if (mapAccount != null && !mapAccount.isEmpty()) {
            Map.Entry<String, String> entry = mapAccount.entrySet().iterator().next();
            String displayName = entry.getKey();
            String roleName = entry.getValue();

            // Giữ thông tin người dùng cho Sidebar JavaFX. Header/Topbar đã được loại bỏ.
            lblUserName = new JLabel(displayName);
            lblRole = new JLabel("Roles: " + roleName);
            lblHello = new JLabel("Xin chào");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Thông Tin Tài Khoản Không còn hợp lệ Vui Lòng liên hệ admin",
                    "ERRROR",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        /* BODY + CONTENT */
        jpnBody = new JPanel(new BorderLayout());
        jpnBody.setBackground(bgContent);

        // Khu vực bên phải chỉ còn nội dung module; Header/Topbar đã được loại bỏ.
        jpnMain = new JPanel(new BorderLayout());
        jpnMain.setBackground(bgContent);
        JPanel contentShell = new JPanel(new BorderLayout());
        contentShell.setBackground(bgContent);
        contentShell.setBorder(new EmptyBorder(12, 12, 12, 12));

        jpnContent = new JPanel(new BorderLayout());
        jpnContent.setBackground(Color.WHITE);
        jpnContent.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        jpnContent.add(new HomeFxPanel(), BorderLayout.CENTER);

        contentShell.add(jpnContent, BorderLayout.CENTER);
        jpnMain.add(contentShell, BorderLayout.CENTER);
        jpnBody.add(jpnMain, BorderLayout.CENTER);

        this.add(jpnBody, BorderLayout.CENTER);
        this.setBackground(bgContent);
    }

    private void initEventController() {
        navigationController = new chuyenManHinhController(jpnContent);

        String username = lblUserName == null ? "User" : lblUserName.getText();
        String roleText = lblRole == null ? "" : lblRole.getText().replace("Roles: ", "");

        fxSidebar = new SidebarFxPanel(
                username,
                roleText,
                pm,
                kind -> SwingUtilities.invokeLater(() -> navigationController.showScreen(kind)),
                () -> SwingUtilities.invokeLater(this::logOut));

        // Controller là nguồn trạng thái điều hướng chung cho Sidebar.
        navigationController.setOnScreenChanged(kind -> {
            if (fxSidebar != null) {
                fxSidebar.setActiveKind(kind);
            }
        });

        jpnBody.add(fxSidebar, BorderLayout.WEST);
        navigationController.showScreen(0);
        jpnBody.revalidate();
        jpnBody.repaint();
    }

    private void toggleChung(boolean isActive) {
        pnlQlyChung.setVisible(isActive);
        updateChevron(jpnQlyChung, isActive);
        refreshSidebar();
    }

    private void toggleSanPham(boolean isActive) {
        pnlQlySanPham.setVisible(isActive);
        updateChevron(jpnQlySanPham, isActive);
        refreshSidebar();
    }

    private void toggleDonHang(boolean isActive) {
        pnlQlyDonHang.setVisible(isActive);
        updateChevron(jpnQlyDonHang, isActive);
        refreshSidebar();
    }

    private void toggleTaiKhoan(boolean isActive) {
        pnlQlyTaiKhoan.setVisible(isActive);
        updateChevron(jpnQlyTaiKhoan, isActive);
        refreshSidebar();
    }

    private void toggleDoanhThu(boolean isActive) {
        pnlQlyDoanhThu.setVisible(isActive);
        updateChevron(jpnDoanhThu, isActive);
        refreshSidebar();
    }

    private void refreshSidebar() {
        jpnMenu.revalidate();
        jpnMenu.repaint();
    }

    private boolean isAnyChungVisible() {
        return pnlQlyChung.isVisible();
    }

    private boolean isAnySanPhamVisible() {
        return pnlQlySanPham.isVisible();
    }

    private boolean isAnyDonHangVisible() {
        return pnlQlyDonHang.isVisible();
    }

    private boolean isAnyTaiKhoanVisible() {
        return pnlQlyTaiKhoan.isVisible();
    }

    private boolean isAnyDoanhThuVisible() {
        return pnlQlyDoanhThu.isVisible();
    }

    private boolean isAnyChungVisible_1() {
        return jpnSanPhamSplit.isVisible()
                || jpnNCC.isVisible()
                || jpnKhuyenMai.isVisible()
                || jpnDvt.isVisible()
                || jpnNhanVien.isVisible()
                || jpnBaoHanh.isVisible();
    }

    private boolean isAnySanPhamVisible_1() {
        return jpnsanPhamStore.isVisible()
                || jpnSanPhamStock.isVisible()
                || jpnPhieuNhap.isVisible();
    }

    private boolean isAnyDonHangVisible_1() {
        return jpnDonHang.isVisible() || jpnLichSuDonHang.isVisible();
    }

    private boolean isAnyTaiKhoanVisible_1() {
        return jpnTaiKhoan.isVisible() || jpnPhanQuyen.isVisible();
    }

    private boolean isAnyDoanhThuVisible_1() {
        return jpnThongKe.isVisible();
    }

    private JPanel createPanel(String title, String icon, boolean isChild) {
        return createPanel(title, icon, isChild, false);
    }

    private JPanel createPanel(String title, String icon, boolean isChild, boolean showChevron) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(isChild ? childColor : parentColor);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, menuSize.height));
        panel.setPreferredSize(new Dimension(SIDEBAR_WIDTH - 18, menuSize.height));
        panel.setMinimumSize(new Dimension(0, menuSize.height));
        panel.setVisible(!isChild);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.putClientProperty("isChild", isChild);

        int leftPadding = isChild ? 30 : 14;
        panel.setBorder(BorderFactory.createEmptyBorder(8, leftPadding, 8, 12));

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        content.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setPreferredSize(new Dimension(24, 24));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, isChild ? 17 : 18));
        lblIcon.setForeground(isChild ? new Color(209, 213, 219) : Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", isChild ? Font.PLAIN : Font.BOLD, isChild ? 13 : 14));
        lblTitle.setForeground(isChild ? new Color(209, 213, 219) : Color.WHITE);

        content.add(lblIcon);
        content.add(lblTitle);
        panel.add(content, BorderLayout.CENTER);

        if (showChevron) {
            JLabel chevron = new JLabel("›");
            chevron.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
            chevron.setForeground(textMuted);
            chevron.setBorder(new EmptyBorder(0, 4, 0, 2));
            panel.putClientProperty("chevron", chevron);
            panel.add(chevron, BorderLayout.EAST);
        }

        panel.putClientProperty("titleLabel", lblTitle);
        panel.putClientProperty("iconLabel", lblIcon);
        panel.setToolTipText(title);
        addHoverEffect_BoxLayout(panel, isChild);
        return panel;
    }

    private JPanel createSidebarHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(true);
        header.setBackground(sidebarColor);
        header.setBorder(new EmptyBorder(18, 16, 14, 16));

        JLabel logo = new JLabel("M", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(new Color(37, 99, 235));
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setPreferredSize(new Dimension(38, 38));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("META STORE");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel sub = new JLabel("Quản lý bán hàng");
        sub.setForeground(textMuted);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        text.add(brand);
        text.add(Box.createVerticalStrut(2));
        text.add(sub);

        header.add(logo, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private JPanel createSidebarFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(sidebarColor);
        footer.setBorder(new EmptyBorder(10, 8, 10, 8));

        JSeparator separator = new JSeparator();
        separator.setForeground(borderItem);
        separator.setBackground(borderItem);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        footer.add(separator);
        footer.add(Box.createVerticalStrut(10));

        JPanel userCard = new JPanel(new BorderLayout(10, 0));
        userCard.setOpaque(true);
        userCard.setBackground(new Color(24, 34, 53));
        userCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        String username = lblUserName == null ? "User" : lblUserName.getText();
        String initial = username == null || username.trim().isEmpty()
                ? "U"
                : username.trim().substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(37, 99, 235));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(username);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        String roleText = lblRole == null ? "" : lblRole.getText().replace("Roles: ", "");
        JLabel role = new JLabel(roleText);
        role.setForeground(textMuted);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.add(name);
        info.add(Box.createVerticalStrut(2));
        info.add(role);

        userCard.add(avatar, BorderLayout.WEST);
        userCard.add(info, BorderLayout.CENTER);
        footer.add(userCard);
        footer.add(Box.createVerticalStrut(8));
        footer.add(jpnExit);
        return footer;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(107, 114, 128));
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setBorder(new EmptyBorder(4, 12, 4, 8));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return label;
    }

    private void styleSubmenuContainer(JPanel panel) {
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 8, 0, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void updateChevron(JPanel panel, boolean expanded) {
        Object value = panel.getClientProperty("chevron");
        if (value instanceof JLabel) {
            ((JLabel) value).setText(expanded ? "⌄" : "›");
            ((JLabel) value).setForeground(expanded ? accentColor : textMuted);
        }
    }

    private void toggleMenu() {
        if (timer != null && timer.isRunning()) {
            return;
        }

        final int target = isVisible ? 0 : SIDEBAR_WIDTH;
        final int step = isVisible ? -28 : 28;
        timer = new Timer(8, (ActionEvent e) -> {
            int width = jpnSidebar.getPreferredSize().width + step;
            boolean finished = step < 0 ? width <= target : width >= target;
            if (finished) {
                width = target;
                isVisible = !isVisible;
                lblMenuIcon.setText(isVisible ? "☰" : "☷");
                timer.stop();
            }
            jpnSidebar.setPreferredSize(new Dimension(Math.max(0, width), 0));
            jpnSidebar.revalidate();
            jpnBody.revalidate();
            jpnBody.repaint();
        });
        timer.start();
    }

    private void addHoverEffect_BoxLayout(JPanel panel, boolean isChild) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive(panel)) {
                    panel.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive(panel)) {
                    panel.setBackground(isChild ? childColor : parentColor);
                }
            }

            private boolean isActive(JPanel p) {
                return p.getBackground().equals(activeColor);
            }
        });
    }

    private void logOut() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().clear();
            dispose();

            if (!rememberUsername.equals("No")) {
                LoginView.showFromSwing(rememberUsername, rememberPassword);
            } else {
                LoginView.showFromSwing("No", "No");
            }
        }
    }

}