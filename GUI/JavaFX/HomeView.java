package GUI.JavaFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * JavaFX version of the old Swing GUI.Panel.trangChuPanel.
 *
 * Phase 2 intentionally keeps this screen presentation-only because the old
 * Swing home cards did not contain navigation/business actions either.
 */
public final class HomeView {

    private final BorderPane root;

    public HomeView() {
        root = new BorderPane();
        root.getStyleClass().add("home-root");
        root.setTop(createHeader());
        root.setCenter(createDashboard());
        root.setBottom(createFooter());
    }

    public Parent getRoot() {
        return root;
    }

    private VBox createHeader() {
        Label eyebrow = new Label("TỔNG QUAN HỆ THỐNG");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Hệ thống quản lý cửa hàng điện tử");
        title.getStyleClass().add("home-title");
        title.setWrapText(true);

        Label welcome = new Label("Xin chào! Chúc bạn một ngày làm việc hiệu quả.");
        welcome.getStyleClass().add("home-subtitle");
        welcome.setWrapText(true);

        VBox texts = new VBox(7, eyebrow, title, welcome);
        texts.setAlignment(Pos.CENTER_LEFT);

        Label statusDot = new Label("●");
        statusDot.getStyleClass().add("status-dot");
        Label statusText = new Label("Hệ thống đang hoạt động");
        statusText.getStyleClass().add("status-text");
        HBox status = new HBox(7, statusDot, statusText);
        status.setAlignment(Pos.CENTER);
        status.getStyleClass().add("status-pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(18, texts, spacer, status);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("header-row");

        VBox header = new VBox(row);
        header.getStyleClass().add("home-header");
        return header;
    }

    private VBox createDashboard() {
        Label sectionTitle = new Label("Truy cập nhanh");
        sectionTitle.getStyleClass().add("section-title");

        Label sectionHint = new Label("Các nhóm chức năng chính của cửa hàng");
        sectionHint.getStyleClass().add("section-hint");

        VBox sectionHeading = new VBox(3, sectionTitle, sectionHint);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.getStyleClass().add("home-grid");

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.333);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }

        grid.add(createCard("⌘", "Quản lý chung",
                "Khách hàng, nhân viên, nhà cung cấp và các danh mục dùng chung."), 0, 0);
        grid.add(createCard("▣", "Quản lý sản phẩm",
                "Theo dõi sản phẩm cửa hàng, tồn kho và phiếu nhập."), 1, 0);
        grid.add(createCard("▤", "Quản lý đơn hàng",
                "Xử lý đơn hàng và tra cứu lịch sử giao dịch."), 2, 0);
        grid.add(createCard("♙", "Quản lý tài khoản",
                "Quản lý tài khoản người dùng trong hệ thống."), 0, 1);
        grid.add(createCard("◆", "Phân quyền hệ thống",
                "Kiểm soát vai trò và quyền truy cập theo chức năng."), 1, 1);
        grid.add(createCard("▥", "Báo cáo thống kê",
                "Theo dõi số liệu và các báo cáo phục vụ quản lý."), 2, 1);

        VBox body = new VBox(18, sectionHeading, grid);
        body.setPadding(new Insets(26, 30, 30, 30));
        VBox.setVgrow(grid, Priority.ALWAYS);
        return body;
    }

    private StackPane createCard(String iconText, String titleText, String descriptionText) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("card-icon");

        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("card-icon-box");
        iconBox.setMinSize(48, 48);
        iconBox.setPrefSize(48, 48);
        iconBox.setMaxSize(48, 48);

        Label title = new Label(titleText);
        title.getStyleClass().add("card-title");

        Label description = new Label(descriptionText);
        description.getStyleClass().add("card-description");
        description.setWrapText(true);

        Label tag = new Label("MODULE");
        tag.getStyleClass().add("card-tag");

        VBox copy = new VBox(7, tag, title, description);
        copy.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(copy, Priority.ALWAYS);

        HBox content = new HBox(16, iconBox, copy);
        content.setAlignment(Pos.TOP_LEFT);

        StackPane card = new StackPane(content);
        card.setPadding(new Insets(22));
        card.setMinHeight(150);
        card.getStyleClass().add("home-card");
        GridPane.setHgrow(card, Priority.ALWAYS);
        GridPane.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox createFooter() {
        Label product = new Label("Electronic Store Management System");
        product.getStyleClass().add("footer-primary");

        Label divider = new Label("•");
        divider.getStyleClass().add("footer-divider");

        Label version = new Label("Version 1.0");
        version.getStyleClass().add("footer-secondary");

        HBox footer = new HBox(8, product, divider, version);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(13, 30, 13, 30));
        footer.getStyleClass().add("home-footer");
        return footer;
    }
}
