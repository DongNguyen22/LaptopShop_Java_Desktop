package GUI.JavaFX;

import Connector.JDBCUTIL;
import DAO.SanPhamDAO;
import DTO.sanPham;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import valiateData.PermissionManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * JavaFX replacement for GUI.Dialog.ChiTietSanPhamDialog.
 *
 * The existing DAO/DTO/database behavior is intentionally reused so this class
 * only replaces the presentation layer during the incremental Swing -> JavaFX
 * migration.
 */
public final class ProductDetailDialogFx {

    private static final int MAX_IMAGES = 5;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final String productId;
    private final Runnable onProductChanged;
    private final SanPhamDAO productDAO = new SanPhamDAO();
    private final PermissionManager permissionManager = new PermissionManager();
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final List<String> images = new ArrayList<>();
    private final ObservableList<SerialRow> serialRows = FXCollections.observableArrayList();

    private int currentImageIndex;
    private Stage stage;

    private final Label codeValue = valueLabel();
    private final Label nameValue = valueLabel();
    private final Label categoryValue = valueLabel();
    private final Label profitValue = valueLabel();
    private final Label averageCostValue = valueLabel();
    private final Label unitPriceValue = valueLabel();
    private final Label sellPriceValue = valueLabel();
    private final Label createdAtValue = valueLabel();
    private final Label updatedAtValue = valueLabel();
    private final Label statusValue = valueLabel();

    private final Label imageSectionTitle = new Label();
    private final ImageView imageView = new ImageView();
    private final Label imageMessage = new Label();
    private final Label imageIndexLabel = new Label("0/0");
    private Button previousImageButton;
    private Button nextImageButton;
    private Button deleteImageButton;
    private Button addImageButton;

    private final TableView<SerialRow> serialTable = new TableView<>();
    private final Label promotionLabel = new Label();

    public ProductDetailDialogFx(String productId) {
        this(productId, null);
    }

    public ProductDetailDialogFx(String productId, Runnable onProductChanged) {
        this.productId = productId;
        this.onProductChanged = onProductChanged;
    }

    public void showAndWait(Window owner) {
        sanPham product = productDAO.getById(productId);
        if (product == null) {
            showError("Không tìm thấy sản phẩm", "Không thể tải thông tin sản phẩm có ID " + safe(productId) + ".");
            return;
        }

        stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Chi tiết sản phẩm - " + safe(product.getCode()));
        stage.setMinWidth(760);
        stage.setMinHeight(620);

        BorderPane root = buildView();
        Scene scene = new Scene(root, 920, 760);
        addStylesheet(scene, "/GUI/JavaFX/product.css");
        addStylesheet(scene, "/GUI/JavaFX/product-detail.css");
        stage.setScene(scene);

        refreshAll();
        stage.centerOnScreen();
        stage.showAndWait();
    }

    private BorderPane buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("product-detail-root");

        VBox header = createHeader();
        VBox content = new VBox(14,
                createInfoCard(),
                createImageCard(),
                createSerialCard(),
                createPromotionCard());
        content.setPadding(new Insets(16, 18, 18, 18));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("product-detail-scroll");

        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(createFooter());
        return root;
    }

    private VBox createHeader() {
        Label eyebrow = new Label("SẢN PHẨM");
        eyebrow.getStyleClass().add("detail-eyebrow");

        Label title = new Label("Chi tiết sản phẩm");
        title.getStyleClass().add("detail-title");

        Label subtitle = new Label("Thông tin sản phẩm, hình ảnh, serial và khuyến mãi đang áp dụng.");
        subtitle.getStyleClass().add("detail-subtitle");

        VBox box = new VBox(2, eyebrow, title, subtitle);
        box.getStyleClass().add("detail-header");
        box.setPadding(new Insets(16, 20, 16, 20));
        return box;
    }

    private VBox createInfoCard() {
        Label title = sectionTitle("Thông tin sản phẩm");

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(10);
        grid.getStyleClass().add("detail-info-grid");

        addInfo(grid, 0, 0, "Mã sản phẩm", codeValue);
        addInfo(grid, 1, 0, "Tên sản phẩm", nameValue);
        addInfo(grid, 0, 1, "Danh mục", categoryValue);
        addInfo(grid, 1, 1, "Trạng thái", statusValue);
        addInfo(grid, 0, 2, "Lợi nhuận", profitValue);
        addInfo(grid, 1, 2, "Vốn trung bình", averageCostValue);
        addInfo(grid, 0, 3, "Đơn giá", unitPriceValue);
        addInfo(grid, 1, 3, "Giá bán", sellPriceValue);
        addInfo(grid, 0, 4, "Ngày tạo", createdAtValue);
        addInfo(grid, 1, 4, "Ngày cập nhật", updatedAtValue);

        VBox card = new VBox(12, title, new Separator(), grid);
        card.getStyleClass().add("detail-card");
        return card;
    }

    private VBox createImageCard() {
        imageSectionTitle.getStyleClass().add("detail-section-title");

        imageView.setFitWidth(560);
        imageView.setFitHeight(340);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageMessage.getStyleClass().add("detail-image-message");
        imageMessage.setAlignment(Pos.CENTER);
        imageMessage.setMaxWidth(Double.MAX_VALUE);

        VBox imageHolder = new VBox(imageView, imageMessage);
        imageHolder.setAlignment(Pos.CENTER);
        imageHolder.setMinHeight(350);
        imageHolder.getStyleClass().add("detail-image-holder");

        addImageButton = actionButton("Thêm ảnh", "success-button");
        deleteImageButton = actionButton("Xóa ảnh này", "danger-button");
        previousImageButton = actionButton("◄ Trước", "secondary-button");
        nextImageButton = actionButton("Sau ►", "secondary-button");

        addImageButton.setOnAction(event -> addNewImage());
        deleteImageButton.setOnAction(event -> deleteCurrentImage());
        previousImageButton.setOnAction(event -> previousImage());
        nextImageButton.setOnAction(event -> nextImage());

        boolean canUpdate = permissionManager.hasPermission("STORE", "UPDATE");
        addImageButton.setVisible(canUpdate);
        addImageButton.setManaged(canUpdate);
        deleteImageButton.setVisible(canUpdate);
        deleteImageButton.setManaged(canUpdate);

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);
        HBox imageActions = new HBox(8, addImageButton, deleteImageButton, actionSpacer,
                previousImageButton, imageIndexLabel, nextImageButton);
        imageActions.setAlignment(Pos.CENTER_LEFT);
        imageIndexLabel.getStyleClass().add("detail-image-index");

        VBox card = new VBox(12, imageSectionTitle, new Separator(), imageHolder, imageActions);
        card.getStyleClass().add("detail-card");
        return card;
    }

    private VBox createSerialCard() {
        Label title = sectionTitle("Danh sách Serial");

        TableColumn<SerialRow, String> serialColumn = new TableColumn<>("Serial");
        serialColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSerial()));
        serialColumn.setPrefWidth(420);

        TableColumn<SerialRow, String> warrantyColumn = new TableColumn<>("Hạn bảo hành");
        warrantyColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getWarranty()));
        warrantyColumn.setPrefWidth(180);

        serialTable.getColumns().setAll(serialColumn, warrantyColumn);
        serialTable.setItems(serialRows);
        serialTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serialTable.setPrefHeight(180);
        serialTable.setPlaceholder(new Label("Sản phẩm chưa có serial."));
        serialTable.getStyleClass().add("detail-serial-table");

        VBox card = new VBox(12, title, new Separator(), serialTable);
        card.getStyleClass().add("detail-card");
        return card;
    }

    private VBox createPromotionCard() {
        Label title = sectionTitle("Khuyến mãi đang áp dụng");
        promotionLabel.setWrapText(true);
        promotionLabel.getStyleClass().add("detail-promotion-text");

        VBox card = new VBox(12, title, new Separator(), promotionLabel);
        card.getStyleClass().add("detail-card");
        return card;
    }

    private HBox createFooter() {
        Button editButton = actionButton("Sửa sản phẩm", "primary-button");
        boolean canUpdate = permissionManager.hasPermission("STORE", "UPDATE");
        editButton.setVisible(canUpdate);
        editButton.setManaged(canUpdate);
        editButton.setOnAction(event -> editProduct());

        Button closeButton = actionButton("Đóng", "secondary-button");
        closeButton.setOnAction(event -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, spacer, editButton, closeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 18, 12, 18));
        footer.getStyleClass().add("detail-footer");
        return footer;
    }

    private void refreshAll() {
        refreshProductInfo();
        reloadImages(false);
        loadSerialInfo();
        loadPromotionInfo();
    }

    private void refreshProductInfo() {
        sanPham product = productDAO.getById(productId);
        if (product == null) {
            showError("Không tìm thấy sản phẩm", "Sản phẩm không còn tồn tại trong dữ liệu.");
            if (stage != null) {
                stage.close();
            }
            return;
        }

        codeValue.setText(display(product.getCode()));
        nameValue.setText(display(product.getTenSP()));
        categoryValue.setText(display(product.getTenDanhmuc()));
        profitValue.setText(formatProfit(product.getLoiNhuan()));
        averageCostValue.setText(formatMoney(product.getGiaNhapTb()));
        unitPriceValue.setText(formatMoney(product.getDonGia()));
        sellPriceValue.setText(formatMoney(product.getSellPrice()));
        statusValue.setText(product.isActive() ? "Bật" : "Tắt");
        createdAtValue.setText(product.getNgayTao() == null ? "N/A" : product.getNgayTao().format(DATE_TIME_FORMAT));
        updatedAtValue.setText(product.getNgayCapNhat() == null ? "N/A" : product.getNgayCapNhat().format(DATE_TIME_FORMAT));

        if (stage != null) {
            stage.setTitle("Chi tiết sản phẩm - " + display(product.getCode()));
        }
    }

    private void reloadImages(boolean keepCurrentIndex) {
        int previousIndex = currentImageIndex;
        images.clear();
        List<String> data = productDAO.getProductImages(productId);
        if (data != null) {
            images.addAll(data);
        }

        if (images.isEmpty()) {
            currentImageIndex = 0;
        } else if (keepCurrentIndex) {
            currentImageIndex = Math.min(previousIndex, images.size() - 1);
        } else {
            currentImageIndex = 0;
        }
        updateCurrentImage();
    }

    private void updateCurrentImage() {
        imageSectionTitle.setText("Danh sách ảnh (" + images.size() + "/" + MAX_IMAGES + ")");

        if (images.isEmpty()) {
            imageView.setImage(null);
            imageMessage.setText("Chưa có ảnh");
            imageIndexLabel.setText("0/0");
            previousImageButton.setDisable(true);
            nextImageButton.setDisable(true);
            deleteImageButton.setDisable(true);
            addImageButton.setDisable(images.size() >= MAX_IMAGES);
            return;
        }

        String path = images.get(currentImageIndex);
        Image image = loadImage(path);
        imageView.setImage(image);
        imageMessage.setText(image == null ? "Không thể tải ảnh" : "");
        imageIndexLabel.setText((currentImageIndex + 1) + "/" + images.size());
        previousImageButton.setDisable(currentImageIndex <= 0);
        nextImageButton.setDisable(currentImageIndex >= images.size() - 1);
        deleteImageButton.setDisable(false);
        addImageButton.setDisable(images.size() >= MAX_IMAGES);
    }

    private Image loadImage(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        try {
            String source = path.trim();
            if (!(source.startsWith("http://") || source.startsWith("https://") || source.startsWith("file:"))) {
                File file = new File(source);
                if (!file.exists()) {
                    return null;
                }
                source = file.toURI().toString();
            }
            Image image = new Image(source, 560, 340, true, true, false);
            return image.isError() ? null : image;
        } catch (Exception ex) {
            return null;
        }
    }

    private void previousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            updateCurrentImage();
        }
    }

    private void nextImage() {
        if (currentImageIndex < images.size() - 1) {
            currentImageIndex++;
            updateCurrentImage();
        }
    }

    private void addNewImage() {
        if (images.size() >= MAX_IMAGES) {
            showWarning("Giới hạn ảnh", "Mỗi sản phẩm chỉ được tối đa " + MAX_IMAGES + " ảnh.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh sản phẩm");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Hình ảnh", "*.jpg", "*.jpeg", "*.png"));
        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        try {
            File uploadDirectory = new File(System.getProperty("user.dir"), "uploads/products");
            if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
                throw new IllegalStateException("Không thể tạo thư mục uploads/products.");
            }

            String extension = fileExtension(selectedFile.getName());
            String fileName = "prod_" + productId + "_" + System.currentTimeMillis() + extension;
            File destination = new File(uploadDirectory, fileName);
            Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (!productDAO.addProductImage(productId, destination.getAbsolutePath())) {
                Files.deleteIfExists(destination.toPath());
                throw new IllegalStateException("Không thể lưu đường dẫn ảnh vào cơ sở dữ liệu.");
            }

            images.clear();
            List<String> data = productDAO.getProductImages(productId);
            if (data != null) {
                images.addAll(data);
            }
            currentImageIndex = Math.max(0, images.size() - 1);
            updateCurrentImage();
            runProductChangedCallback();
            showInfo("Thêm ảnh thành công", "Đã thêm ảnh sản phẩm (" + images.size() + "/" + MAX_IMAGES + ").");
        } catch (Exception ex) {
            showError("Lỗi thêm ảnh", ex.getMessage());
        }
    }

    private void deleteCurrentImage() {
        if (images.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Xác nhận xóa ảnh");
        confirm.setHeaderText("Xóa ảnh hiện tại?");
        confirm.setContentText("Ảnh sẽ bị xóa khỏi sản phẩm.");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (!answer.isPresent() || answer.get() != ButtonType.OK) {
            return;
        }

        String imagePath = images.get(currentImageIndex);
        deleteLocalFileIfPossible(imagePath);
        productDAO.deleteProductImage(productId, imagePath);
        reloadImages(true);
        runProductChangedCallback();
        showInfo("Đã xóa ảnh", "Sản phẩm hiện còn " + images.size() + "/" + MAX_IMAGES + " ảnh.");
    }

    private void loadSerialInfo() {
        serialRows.clear();
        String sql = "SELECT DISTINCT se.serial, ri.warranty_time "
                + "FROM receipt_items ri JOIN item_serials se ON ri.receipt_item_id = se.receipt_item_id "
                + "WHERE ri.product_id = ? AND se.serial IS NOT NULL ORDER BY se.serial";

        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId);
            var rs = statement.executeQuery();
            while (rs.next()) {
                String serial = rs.getString("serial");
                int warranty = rs.getInt("warranty_time");
                serialRows.add(new SerialRow(serial, warranty > 0 ? warranty + " tháng" : "N/A"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            serialTable.setPlaceholder(new Label("Không thể tải danh sách serial."));
        }
    }

    private void loadPromotionInfo() {
        String sql = "SELECT p.promotion_id, p.name, pi.discount_percent, p.start_at, p.end_at "
                + "FROM promotion_items pi JOIN promotions p ON pi.promotion_id = p.promotion_id "
                + "WHERE pi.product_id = ? AND p.status = 1";

        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId);
            var rs = statement.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                double discount = rs.getDouble("discount_percent");
                java.sql.Date startDate = rs.getDate("start_at");
                java.sql.Date endDate = rs.getDate("end_at");
                promotionLabel.setText("Tên: " + safe(name)
                        + "  •  Giảm: " + discount + "%"
                        + "  •  Từ: " + startDate + " → " + endDate);
            } else {
                promotionLabel.setText("Không có khuyến mãi");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            promotionLabel.setText("Không thể tải thông tin khuyến mãi.");
        }
    }

    private void editProduct() {
        sanPham product = productDAO.getById(productId);
        if (product == null) {
            showError("Không tìm thấy sản phẩm", "Không thể tải sản phẩm để chỉnh sửa.");
            return;
        }

        ProductEditDialogFx dialog = new ProductEditDialogFx(product);
        if (dialog.showAndWait(stage)) {
            refreshProductInfo();
            loadPromotionInfo();
            runProductChangedCallback();
            showInfo("Thành công", "Đã cập nhật thông tin sản phẩm.");
        }
    }

    private void runProductChangedCallback() {
        if (onProductChanged != null) {
            onProductChanged.run();
        }
    }

    private void deleteLocalFileIfPossible(String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }
        String value = path.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return;
        }
        try {
            File file = value.startsWith("file:") ? new File(java.net.URI.create(value)) : new File(value);
            Files.deleteIfExists(file.toPath());
        } catch (Exception ignored) {
            // Keep the database deletion behavior even when the physical file is already missing.
        }
    }

    private static String fileExtension(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return ".png";
        }
        if (lower.endsWith(".jpeg")) {
            return ".jpeg";
        }
        return ".jpg";
    }

    private void addInfo(GridPane grid, int column, int row, String labelText, Label value) {
        VBox block = new VBox(3);
        Label label = new Label(labelText);
        label.getStyleClass().add("detail-field-label");
        value.setWrapText(true);
        block.getChildren().addAll(label, value);
        GridPane.setHgrow(block, Priority.ALWAYS);
        block.setMaxWidth(Double.MAX_VALUE);
        grid.add(block, column, row);
    }

    private Button actionButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("product-button", styleClass);
        return button;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-section-title");
        return label;
    }

    private static Label valueLabel() {
        Label label = new Label("-");
        label.getStyleClass().add("detail-field-value");
        return label;
    }

    private String formatMoney(double value) {
        return numberFormat.format(value) + " VND";
    }

    private String formatProfit(double value) {
        return numberFormat.format(value);
    }

    private static String display(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private void addStylesheet(Scene scene, String resourcePath) {
        java.net.URL css = ProductDetailDialogFx.class.getResource(resourcePath);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (stage != null && stage.isShowing()) {
            alert.initOwner(stage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        if (stage != null && stage.isShowing()) {
            alert.initOwner(stage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (stage != null && stage.isShowing()) {
            alert.initOwner(stage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private static final class SerialRow {
        private final String serial;
        private final String warranty;

        private SerialRow(String serial, String warranty) {
            this.serial = serial == null ? "" : serial;
            this.warranty = warranty == null ? "" : warranty;
        }

        private String getSerial() {
            return serial;
        }

        private String getWarranty() {
            return warranty;
        }
    }
}
