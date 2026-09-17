package GUI.JavaFX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import valiateData.PermissionManager;

/**
 * Sidebar JavaFX nhúng trong Dashboard Swing.
 *
 * Không chứa nghiệp vụ điều hướng cụ thể: sidebar chỉ phát ra kind qua callback.
 * Vì vậy mapping module vẫn do chuyenManHinhController quản lý.
 */
public final class SidebarView extends BorderPane {

    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private static final PseudoClass GROUP_ACTIVE = PseudoClass.getPseudoClass("group-active");
    private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass COLLAPSED = PseudoClass.getPseudoClass("collapsed");

    private final IntConsumer onNavigate;
    private final Runnable onLogout;
    private final Runnable onToggleCollapsed;

    private final VBox menuBox = new VBox(5);
    private final ScrollPane scrollPane = new ScrollPane(menuBox);
    private final Label brandLabel = new Label("META STORE");
    private final Label brandSubLabel = new Label("Quản lý bán hàng");
    private final Label navigationSection = createSectionLabel("ĐIỀU HƯỚNG");
    private final Label managementSection = createSectionLabel("QUẢN LÝ");
    private final Label userNameLabel;
    private final Label roleLabel;
    private final Label logoutLabel = new Label("Đăng Xuất");
    private final Label collapseHintLabel = new Label("Thu gọn");
    private final SVGPath collapseArrow = createSvg("collapse");
    private final StackPane headerCollapseButton = new StackPane(collapseArrow);

    private final HBox brandTextBox = new HBox();
    private final VBox brandText = new VBox(1);
    private final HBox logoutItem = new HBox(12);
    private final VBox userInfoBox = new VBox(2);
    private final HBox collapseItem = new HBox(10);

    private final Map<Integer, NavItem> navItems = new HashMap<>();
    private final Map<Integer, MenuGroup> groupsByKind = new HashMap<>();
    private final List<MenuGroup> groups = new ArrayList<>();
    private final List<NavItem> allNavItems = new ArrayList<>();

    private boolean collapsed;
    private int activeKind = 0;

    public SidebarView(
            String userName,
            String role,
            PermissionManager permissions,
            IntConsumer onNavigate,
            Runnable onLogout,
            Runnable onToggleCollapsed) {
        this.onNavigate = onNavigate;
        this.onLogout = onLogout;
        this.onToggleCollapsed = onToggleCollapsed;
        this.userNameLabel = new Label(normalize(userName, "User"));
        this.roleLabel = new Label(normalize(role, ""));

        getStyleClass().add("sidebar-root");
        setPadding(new Insets(0));
        collapseArrow.getStyleClass().add("nav-svg");

        setTop(buildHeader());
        setCenter(buildMenu(permissions));
        setBottom(buildFooter());

        setActiveKind(0);
    }

    private Node buildHeader() {
        HBox header = new HBox(11);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 12, 13, 12));

        StackPane logo = new StackPane();
        logo.getStyleClass().add("brand-logo");
        logo.setMinSize(42, 42);
        logo.setPrefSize(42, 42);
        logo.setMaxSize(42, 42);
        SVGPath shopIcon = createSvg("store");
        shopIcon.getStyleClass().add("brand-logo-icon");
        logo.getChildren().add(shopIcon);
        Tooltip.install(logo, new Tooltip("Meta Store"));

        brandLabel.getStyleClass().add("brand-title");
        brandSubLabel.getStyleClass().add("brand-subtitle");
        brandText.getChildren().addAll(brandLabel, brandSubLabel);
        brandText.setAlignment(Pos.CENTER_LEFT);
        brandTextBox.getChildren().add(brandText);
        HBox.setHgrow(brandTextBox, Priority.ALWAYS);

        headerCollapseButton.getStyleClass().add("collapse-button");
        headerCollapseButton.setMinSize(34, 34);
        headerCollapseButton.setPrefSize(34, 34);
        headerCollapseButton.setMaxSize(34, 34);
        Tooltip.install(headerCollapseButton, new Tooltip("Thu gọn / mở rộng sidebar"));
        headerCollapseButton.setOnMouseClicked(e -> onToggleCollapsed.run());

        header.getChildren().addAll(logo, brandTextBox, headerCollapseButton);
        return header;
    }

    private Node buildMenu(PermissionManager permissions) {
        menuBox.getStyleClass().add("menu-box");
        menuBox.setPadding(new Insets(8, 8, 10, 8));
        menuBox.setFillWidth(true);

        menuBox.getChildren().add(navigationSection);
        NavItem home = addNavItem(0, "Trang Chủ", "home", false);
        menuBox.getChildren().add(home);
        menuBox.getChildren().add(spacer(7));
        menuBox.getChildren().add(managementSection);

        MenuGroup common = new MenuGroup("Quản Lý Chung", "apps");
        addIfAllowed(common, permissions, "CATEGORY", 1, "Phân Loại", "category");
        addIfAllowed(common, permissions, "SUPPLIER", 2, "Nhà Cung Cấp", "truck");
        addIfAllowed(common, permissions, "PROMOTION", 3, "Khuyến Mãi", "gift");
        addIfAllowed(common, permissions, "CUSTOMER", 4, "Khách Hàng", "person");
        addIfAllowed(common, permissions, "EMPLOYEE", 5, "Nhân Viên", "group");
        addIfAllowed(common, permissions, "WARRANTY", 6, "Bảo Hành", "shield");
        addGroupIfNotEmpty(common);

        MenuGroup product = new MenuGroup("Quản Lý Sản Phẩm", "package");
        addIfAllowed(product, permissions, "STORE", 7, "Cửa Hàng", "store");
        addIfAllowed(product, permissions, "WAREHOUSE", 8, "Tồn Kho", "warehouse");
        addIfAllowed(product, permissions, "RECEIPT", 9, "Phiếu Nhập", "receipt");
        addGroupIfNotEmpty(product);

        MenuGroup order = new MenuGroup("Quản Lý Đơn Hàng", "cart");
        addIfAllowed(order, permissions, "ORDER", 10, "Đơn Hàng", "bag");
        addIfAllowed(order, permissions, "HISTORY", 11, "Lịch Sử Giao Dịch", "history");
        addGroupIfNotEmpty(order);

        MenuGroup account = new MenuGroup("Quản Lý Tài Khoản", "user-settings");
        addIfAllowed(account, permissions, "ACCOUNT", 12, "Tài Khoản", "account");
        addIfAllowed(account, permissions, "PERMISSION", 13, "Phân Quyền", "lock");
        addGroupIfNotEmpty(account);

        MenuGroup report = new MenuGroup("Báo Cáo & Thống Kê", "chart");
        addIfAllowed(report, permissions, "REPORT", 14, "Thống Kê Tổng Hợp", "bar-chart");
        addGroupIfNotEmpty(report);

        scrollPane.getStyleClass().add("sidebar-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        return scrollPane;
    }

    private Node buildFooter() {
        VBox footer = new VBox(8);
        footer.getStyleClass().add("sidebar-footer");
        footer.setPadding(new Insets(9, 8, 10, 8));

        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");

        HBox userCard = new HBox(10);
        userCard.getStyleClass().add("user-card");
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setPadding(new Insets(9, 10, 9, 10));

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("user-avatar");
        avatar.setMinSize(36, 36);
        avatar.setPrefSize(36, 36);
        avatar.setMaxSize(36, 36);
        Label initial = new Label(firstLetter(userNameLabel.getText()));
        initial.getStyleClass().add("user-avatar-text");
        avatar.getChildren().add(initial);
        Tooltip.install(avatar, new Tooltip(userNameLabel.getText() + "\n" + roleLabel.getText()));

        userNameLabel.getStyleClass().add("user-name");
        roleLabel.getStyleClass().add("user-role");
        userInfoBox.getChildren().addAll(userNameLabel, roleLabel);
        HBox.setHgrow(userInfoBox, Priority.ALWAYS);
        userCard.getChildren().addAll(avatar, userInfoBox);

        collapseItem.getStyleClass().add("footer-action");
        collapseItem.setAlignment(Pos.CENTER_LEFT);
        collapseItem.setPadding(new Insets(8, 11, 8, 11));
        SVGPath collapseIcon = createSvg("collapse");
        collapseIcon.getStyleClass().add("nav-svg");
        collapseHintLabel.getStyleClass().add("footer-action-label");
        collapseItem.getChildren().addAll(iconBox(collapseIcon), collapseHintLabel);
        Tooltip.install(collapseItem, new Tooltip("Thu gọn / mở rộng sidebar"));
        collapseItem.setOnMouseClicked(e -> onToggleCollapsed.run());

        logoutItem.getStyleClass().addAll("footer-action", "logout-action");
        logoutItem.setAlignment(Pos.CENTER_LEFT);
        logoutItem.setPadding(new Insets(8, 11, 8, 11));
        SVGPath logoutIcon = createSvg("logout");
        logoutIcon.getStyleClass().add("nav-svg");
        logoutLabel.getStyleClass().add("footer-action-label");
        logoutItem.getChildren().addAll(iconBox(logoutIcon), logoutLabel);
        Tooltip.install(logoutItem, new Tooltip("Đăng Xuất"));
        logoutItem.setOnMouseClicked(e -> onLogout.run());

        footer.getChildren().addAll(separator, userCard, collapseItem, logoutItem);
        return footer;
    }

    private void addIfAllowed(
            MenuGroup group,
            PermissionManager permissions,
            String module,
            int kind,
            String title,
            String icon) {
        if (permissions != null && permissions.hasPermission(module, "VIEW")) {
            NavItem item = addNavItem(kind, title, icon, true);
            group.addItem(item);
            groupsByKind.put(kind, group);
        }
    }

    private NavItem addNavItem(int kind, String title, String icon, boolean child) {
        NavItem item = new NavItem(kind, title, icon, child);
        navItems.put(kind, item);
        allNavItems.add(item);
        return item;
    }

    private void addGroupIfNotEmpty(MenuGroup group) {
        if (group.isEmpty()) {
            return;
        }
        groups.add(group);
        menuBox.getChildren().add(group);
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        region.setPrefHeight(height);
        region.setMaxHeight(height);
        return region;
    }

    public void setActiveKind(int kind) {
        activeKind = kind;
        for (NavItem item : allNavItems) {
            item.setActive(item.kind == kind);
        }
        for (MenuGroup group : groups) {
            group.setGroupActive(false);
        }

        MenuGroup activeGroup = groupsByKind.get(kind);
        if (activeGroup != null) {
            activeGroup.setGroupActive(true);
            if (!collapsed) {
                for (MenuGroup group : groups) {
                    group.setExpanded(group == activeGroup);
                }
            }
        } else if (!collapsed) {
            for (MenuGroup group : groups) {
                group.setExpanded(false);
            }
        }
    }

    public int getActiveKind() {
        return activeKind;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        pseudoClassStateChanged(COLLAPSED, collapsed);

        brandTextBox.setVisible(!collapsed);
        brandTextBox.setManaged(!collapsed);
        headerCollapseButton.setVisible(!collapsed);
        headerCollapseButton.setManaged(!collapsed);
        navigationSection.setVisible(!collapsed);
        navigationSection.setManaged(!collapsed);
        managementSection.setVisible(!collapsed);
        managementSection.setManaged(!collapsed);
        userInfoBox.setVisible(!collapsed);
        userInfoBox.setManaged(!collapsed);
        logoutLabel.setVisible(!collapsed);
        logoutLabel.setManaged(!collapsed);
        collapseHintLabel.setVisible(!collapsed);
        collapseHintLabel.setManaged(!collapsed);
        collapseArrow.setRotate(collapsed ? 180 : 0);

        for (NavItem item : allNavItems) {
            item.setCollapsed(collapsed);
        }
        for (MenuGroup group : groups) {
            group.setCollapsed(collapsed);
        }

        if (!collapsed) {
            MenuGroup activeGroup = groupsByKind.get(activeKind);
            if (activeGroup != null) {
                activeGroup.setExpanded(true);
            }
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    private final class NavItem extends HBox {
        private final int kind;
        private final Label titleLabel;
        private final StackPane iconContainer;

        private NavItem(int kind, String title, String iconName, boolean child) {
            this.kind = kind;
            getStyleClass().add("nav-item");
            if (child) {
                getStyleClass().add("nav-child");
            }
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(11);
            setMinHeight(43);
            setPrefHeight(43);
            setMaxWidth(Double.MAX_VALUE);

            SVGPath icon = createSvg(iconName);
            icon.getStyleClass().add("nav-svg");
            iconContainer = iconBox(icon);

            titleLabel = new Label(title);
            titleLabel.getStyleClass().add("nav-label");
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            getChildren().addAll(iconContainer, titleLabel);
            Tooltip.install(this, new Tooltip(title));

            setOnMouseClicked(e -> {
                setActiveKind(kind);
                onNavigate.accept(kind);
            });
        }

        private void setActive(boolean active) {
            pseudoClassStateChanged(ACTIVE, active);
        }

        private void setCollapsed(boolean value) {
            pseudoClassStateChanged(COLLAPSED, value);
            titleLabel.setVisible(!value);
            titleLabel.setManaged(!value);
            setAlignment(value ? Pos.CENTER : Pos.CENTER_LEFT);
        }
    }

    private final class MenuGroup extends VBox {
        private final HBox header = new HBox(11);
        private final VBox children = new VBox(3);
        private final Label titleLabel;
        private final Label chevron = new Label("›");
        private final List<NavItem> childItems = new ArrayList<>();
        private boolean expanded;
        private boolean compact;

        private MenuGroup(String title, String iconName) {
            getStyleClass().add("menu-group");
            setMaxWidth(Double.MAX_VALUE);

            header.getStyleClass().add("group-header");
            header.setAlignment(Pos.CENTER_LEFT);
            header.setMinHeight(45);
            header.setPrefHeight(45);
            header.setMaxWidth(Double.MAX_VALUE);

            SVGPath icon = createSvg(iconName);
            icon.getStyleClass().add("nav-svg");
            StackPane iconContainer = iconBox(icon);

            titleLabel = new Label(title);
            titleLabel.getStyleClass().add("group-label");
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            chevron.getStyleClass().add("group-chevron");
            header.getChildren().addAll(iconContainer, titleLabel, chevron);
            Tooltip.install(header, new Tooltip(title));

            children.getStyleClass().add("group-children");
            children.setFillWidth(true);
            children.setVisible(false);
            children.setManaged(false);

            header.setOnMouseClicked(e -> {
                if (compact) {
                    setExpanded(true);
                    onToggleCollapsed.run();
                    return;
                }
                boolean next = !expanded;
                if (next) {
                    for (MenuGroup group : groups) {
                        if (group != this) {
                            group.setExpanded(false);
                        }
                    }
                }
                setExpanded(next);
            });

            getChildren().addAll(header, children);
        }

        private void addItem(NavItem item) {
            childItems.add(item);
            children.getChildren().add(item);
        }

        private boolean isEmpty() {
            return childItems.isEmpty();
        }

        private void setExpanded(boolean value) {
            expanded = value;
            pseudoClassStateChanged(EXPANDED, value);
            chevron.setText(value ? "⌄" : "›");
            boolean showChildren = value && !compact;
            children.setVisible(showChildren);
            children.setManaged(showChildren);
        }

        private void setGroupActive(boolean value) {
            header.pseudoClassStateChanged(GROUP_ACTIVE, value);
        }

        private void setCollapsed(boolean value) {
            compact = value;
            header.pseudoClassStateChanged(COLLAPSED, value);
            titleLabel.setVisible(!value);
            titleLabel.setManaged(!value);
            chevron.setVisible(!value);
            chevron.setManaged(!value);
            header.setAlignment(value ? Pos.CENTER : Pos.CENTER_LEFT);
            children.setVisible(!value && expanded);
            children.setManaged(!value && expanded);
        }
    }

    private static Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static StackPane iconBox(SVGPath icon) {
        StackPane box = new StackPane(icon);
        box.getStyleClass().add("nav-icon-box");
        box.setMinSize(28, 28);
        box.setPrefSize(28, 28);
        box.setMaxSize(28, 28);
        return box;
    }

    private static SVGPath createSvg(String name) {
        return FxIconFactory.create(name, 0.82);
    }

    private static String firstLetter(String text) {
        String value = normalize(text, "U").trim();
        return value.isEmpty() ? "U" : value.substring(0, 1).toUpperCase();
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
