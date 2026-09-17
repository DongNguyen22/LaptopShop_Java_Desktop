package GUI.JavaFX;

import Bus.SanPhamBUS;
import Connector.JDBCUTIL;
import DAO.SanPhamDAO;
import DTO.sanPham;
import Utils.ExcelUtils;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import valiateData.PermissionManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** JavaFX replacement for GUI.Panel.cuaHangPanel (Product/Store screen). */
public final class ProductView {

    private static final String ALL = "Tất cả";
    private static final String ACTIVE = "Bật";
    private static final String INACTIVE = "Tắt";

    private final SanPhamBUS productBUS = new SanPhamBUS();
    private final SanPhamDAO productDAO = new SanPhamDAO();
    private final PermissionManager permissionManager = new PermissionManager();
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final BorderPane root = new BorderPane();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryBox = new ComboBox<>();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final TableView<sanPham> table = new TableView<>();
    private final ObservableList<sanPham> pageItems = FXCollections.observableArrayList();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();

    private final List<sanPham> fullList = new ArrayList<>();
    private final List<sanPham> filteredList = new ArrayList<>();

    private Button addButton;
    private Button detailButton;
    private Button editButton;
    private Button deleteButton;
    private Button previousButton;
    private Button nextButton;

    private int currentPage = 1;
    private int pageSize = 20;

    public ProductView() {
        buildView();
        loadCategories();
        configureFilters();
        loadProducts();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("product-root");

        VBox header = createHeader();
        VBox content = new VBox(12, createFilterBar(), createActionBar(), createTableCard(), createPaginationBar());
        content.setPadding(new Insets(16, 20, 18, 20));
        VBox.setVgrow(content.getChildren().get(2), Priority.ALWAYS);

        root.setTop(header);
        root.setCenter(content);
    }

    private VBox createHeader() {
        Label eyebrow = new Label("CỬA HÀNG");
        eyebrow.getStyleClass().add("product-eyebrow");

        Label title = new Label("Quản lý sản phẩm");
        title.getStyleClass().add("product-title");

        Label subtitle = new Label("Quản lý danh sách, giá bán, tồn kho và trạng thái sản phẩm.");
        subtitle.getStyleClass().add("product-subtitle");

        VBox textBox = new VBox(2, eyebrow, title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        resultLabel.getStyleClass().add("product-count");

        HBox row = new HBox(16, textBox, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.getStyleClass().add("product-header");
        header.setPadding(new Insets(18, 22, 18, 22));
        return header;
    }

    private HBox createFilterBar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Mã hoặc tên sản phẩm...");
        searchField.getStyleClass().add("product-search-field");
        searchField.setPrefWidth(270);

        categoryBox.setPrefWidth(180);
        statusBox.setPrefWidth(110);
        categoryBox.getStyleClass().add("product-filter-box");
        statusBox.getStyleClass().add("product-filter-box");

        HBox filterBar = new HBox(10,
                searchLabel, searchField,
                new Label("Danh mục"), categoryBox,
                new Label("Trạng thái"), statusBox);
        filterBar.getStyleClass().add("product-toolbar");
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12));
        return filterBar;
    }

    private HBox createActionBar() {
        addButton = createButton("Thêm sản phẩm", "success-button");
        addButton.setVisible(permissionManager.hasPermission("STORE", "ADD"));
        addButton.setManaged(addButton.isVisible());
        addButton.setOnAction(event -> addProduct());

        detailButton = createButton("Chi tiết", "secondary-button");
        detailButton.setDisable(true);
        detailButton.setOnAction(event -> viewSelectedProduct());

        editButton = createButton("Sửa", "primary-button");
        editButton.setVisible(permissionManager.hasPermission("STORE", "UPDATE"));
        editButton.setManaged(editButton.isVisible());
        editButton.setDisable(true);
        editButton.setOnAction(event -> editSelectedProduct());

        deleteButton = createButton("Xóa", "danger-button");
        deleteButton.setVisible(permissionManager.hasPermission("STORE", "DELETE"));
        deleteButton.setManaged(deleteButton.isVisible());
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> deleteSelectedProduct());

        Button importButton = createButton("Import Excel", "secondary-button");
        importButton.setOnAction(event -> importFromExcel());

        Button exportButton = createButton("Export Excel", "secondary-button");
        exportButton.setOnAction(event -> exportToExcel());

        Button templateButton = createButton("Tải Template", "secondary-button");
        templateButton.setOnAction(event -> downloadTemplate());

        Button refreshButton = createButton("Làm mới", "secondary-button");
        refreshButton.setOnAction(event -> loadProducts());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(8,
                addButton, detailButton, editButton, deleteButton,
                spacer, importButton, exportButton, templateButton, refreshButton);
        actionBar.getStyleClass().add("product-actionbar");
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(10, 12, 10, 12));
        return actionBar;
    }

    private VBox createTableCard() {
        configureTable();
        VBox card = new VBox(table);
        card.getStyleClass().add("product-table-card");
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private HBox createPaginationBar() {
        previousButton = createButton("‹ Trước", "secondary-button");
        nextButton = createButton("Sau ›", "secondary-button");
        previousButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                updatePage();
            }
        });
        nextButton.setOnAction(event -> {
            if (currentPage < totalPages()) {
                currentPage++;
                updatePage();
            }
        });

        pageSizeBox.getItems().addAll(10, 20, 50, 100);
        pageSizeBox.setValue(pageSize);
        pageSizeBox.setPrefWidth(82);
        pageSizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 1;
                updatePage();
            }
        });

        pageLabel.getStyleClass().add("pagination-label");
        Label sizeLabel = new Label("Dòng/trang");
        sizeLabel.getStyleClass().add("pagination-muted");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, pageLabel, spacer, sizeLabel, pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("product-pagination");
        footer.setPadding(new Insets(9, 12, 9, 12));
        return footer;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.getStyleClass().add("product-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu sản phẩm"));
        table.setItems(pageItems);

        TableColumn<sanPham, sanPham> imageCol = new TableColumn<>("Ảnh");
        imageCol.setPrefWidth(68);
        imageCol.setMaxWidth(82);
        imageCol.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cell.getValue()));
        imageCol.setCellFactory(column -> new TableCell<sanPham, sanPham>() {
            @Override
            protected void updateItem(sanPham product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                ImageView view = loadThumbnail(product.getMaSP());
                setGraphic(view);
                setText(view == null ? "—" : null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<sanPham, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(safe(cell.getValue().getMaSP())));
        idCol.setPrefWidth(55);

        TableColumn<sanPham, String> codeCol = new TableColumn<>("Mã");
        codeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(safe(cell.getValue().getCode())));
        codeCol.setPrefWidth(90);

        TableColumn<sanPham, String> categoryCol = new TableColumn<>("Danh mục");
        categoryCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(safe(cell.getValue().getTenDanhmuc())));
        categoryCol.setPrefWidth(130);

        TableColumn<sanPham, String> nameCol = new TableColumn<>("Tên sản phẩm");
        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(safe(cell.getValue().getTenSP())));
        nameCol.setPrefWidth(220);

        TableColumn<sanPham, Number> stockCol = new TableColumn<>("Tồn kho");
        stockCol.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getSoLuong()));
        stockCol.setPrefWidth(78);

        TableColumn<sanPham, Number> profitCol = new TableColumn<>("Lợi nhuận");
        profitCol.setCellValueFactory(cell -> new ReadOnlyDoubleWrapper(cell.getValue().getLoiNhuan()));
        profitCol.setPrefWidth(88);
        profitCol.setCellFactory(column -> new TableCell<sanPham, Number>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    double raw = value.doubleValue();
                    setText(raw <= 1 ? String.format(Locale.US, "%.0f%%", raw * 100) : String.format(Locale.US, "%.0f%%", raw));
                }
            }
        });

        TableColumn<sanPham, Number> priceCol = new TableColumn<>("Đơn giá");
        priceCol.setCellValueFactory(cell -> new ReadOnlyDoubleWrapper(cell.getValue().getDonGia()));
        priceCol.setPrefWidth(115);
        priceCol.setCellFactory(column -> moneyCell());

        TableColumn<sanPham, Number> sellPriceCol = new TableColumn<>("Giá bán");
        sellPriceCol.setCellValueFactory(cell -> new ReadOnlyDoubleWrapper(cell.getValue().getSellPrice()));
        sellPriceCol.setPrefWidth(115);
        sellPriceCol.setCellFactory(column -> moneyCell());

        TableColumn<sanPham, Boolean> activeCol = new TableColumn<>("Bật/Tắt");
        activeCol.setCellValueFactory(cell -> new ReadOnlyBooleanWrapper(cell.getValue().isActive()));
        activeCol.setPrefWidth(78);
        activeCol.setCellFactory(column -> new TableCell<sanPham, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private boolean syncing;

            {
                setAlignment(Pos.CENTER);
                checkBox.setOnAction(event -> {
                    if (syncing || getTableRow() == null) {
                        return;
                    }
                    sanPham product = (sanPham) getTableRow().getItem();
                    if (product == null) {
                        return;
                    }
                    if (!permissionManager.hasPermission("STORE", "UPDATE")) {
                        syncing = true;
                        checkBox.setSelected(product.isActive());
                        syncing = false;
                        showPermissionWarning();
                        return;
                    }
                    boolean newValue = checkBox.isSelected();
                    productDAO.updateActive(product.getMaSP(), newValue);
                    product.setActive(newValue);
                    showInfo("Cập nhật trạng thái", "Đã cập nhật sản phẩm sang trạng thái " + (newValue ? "Bật" : "Tắt") + ".");
                });
            }

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    syncing = true;
                    checkBox.setSelected(value);
                    checkBox.setDisable(!permissionManager.hasPermission("STORE", "UPDATE"));
                    syncing = false;
                    setGraphic(checkBox);
                }
            }
        });

        table.getColumns().setAll(imageCol, idCol, codeCol, categoryCol, nameCol,
                stockCol, profitCol, priceCol, sellPriceCol, activeCol);

        table.setRowFactory(tv -> {
            TableRow<sanPham> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getItem());
                    viewSelectedProduct();
                }
            });
            return row;
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (detailButton != null) {
                detailButton.setDisable(newValue == null);
            }
            if (editButton != null) {
                editButton.setDisable(newValue == null);
            }
            if (deleteButton != null) {
                deleteButton.setDisable(newValue == null);
            }
        });
    }

    private TableCell<sanPham, Number> moneyCell() {
        return new TableCell<sanPham, Number>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : moneyFormat.format(value.doubleValue()));
            }
        };
    }

    private ImageView loadThumbnail(String productId) {
        try {
            List<String> images = productDAO.getProductImages(productId);
            if (images.isEmpty()) {
                return null;
            }
            File file = resolveImageFile(images.get(0));
            if (file == null || !file.exists() || !file.isFile()) {
                return null;
            }
            Image image = new Image(file.toURI().toString(), 48, 48, true, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(48);
            imageView.setFitHeight(48);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception ex) {
            return null;
        }
    }

    private File resolveImageFile(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        File direct = new File(path);
        if (direct.exists()) {
            return direct;
        }
        File relative = new File(System.getProperty("user.dir"), path.replace("\\", File.separator));
        return relative.exists() ? relative : direct;
    }

    private void configureFilters() {
        statusBox.getItems().setAll(ALL, ACTIVE, INACTIVE);
        statusBox.setValue(ALL);
        categoryBox.setValue(ALL);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        categoryBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void loadCategories() {
        categoryBox.getItems().clear();
        categoryBox.getItems().add(ALL);
        String sql = "SELECT name FROM categories ORDER BY name";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql);
             var rs = statement.executeQuery()) {
            while (rs.next()) {
                categoryBox.getItems().add(rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadProducts() {
        sanPham selected = table.getSelectionModel().getSelectedItem();
        String selectedId = selected == null ? null : selected.getMaSP();

        fullList.clear();
        List<sanPham> data = productBUS.getAllProducts();
        if (data != null) {
            fullList.addAll(data);
        }
        applyFilters();

        if (selectedId != null) {
            for (sanPham item : pageItems) {
                if (selectedId.equals(item.getMaSP())) {
                    table.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    private void applyFilters() {
        String keyword = safe(searchField.getText()).trim().toLowerCase(Locale.ROOT);
        String category = categoryBox.getValue() == null ? ALL : categoryBox.getValue();
        String status = statusBox.getValue() == null ? ALL : statusBox.getValue();

        filteredList.clear();
        for (sanPham product : fullList) {
            boolean matchSearch = keyword.isEmpty()
                    || safe(product.getCode()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(product.getTenSP()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(product.getMaSP()).toLowerCase(Locale.ROOT).contains(keyword);

            boolean matchCategory = ALL.equals(category)
                    || safe(product.getTenDanhmuc()).equals(category);

            boolean matchStatus = ALL.equals(status)
                    || (ACTIVE.equals(status) && product.isActive())
                    || (INACTIVE.equals(status) && !product.isActive());

            if (matchSearch && matchCategory && matchStatus) {
                filteredList.add(product);
            }
        }

        currentPage = 1;
        updatePage();
    }

    private void updatePage() {
        int pages = totalPages();
        if (currentPage > pages) {
            currentPage = pages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        int start = Math.min((currentPage - 1) * pageSize, filteredList.size());
        int end = Math.min(start + pageSize, filteredList.size());
        pageItems.setAll(filteredList.subList(start, end));

        resultLabel.setText(filteredList.size() + " sản phẩm");
        pageLabel.setText("Trang " + currentPage + "/" + pages + "  •  " + filteredList.size() + " kết quả");
        if (previousButton != null) {
            previousButton.setDisable(currentPage <= 1);
        }
        if (nextButton != null) {
            nextButton.setDisable(currentPage >= pages);
        }
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredList.size() / (double) pageSize));
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("product-button", styleClass);
        return button;
    }

    private void addProduct() {
        if (!permissionManager.hasPermission("STORE", "ADD")) {
            showPermissionWarning();
            return;
        }
        ProductEditDialogFx dialog = new ProductEditDialogFx(null);
        if (dialog.showAndWait(root.getScene() == null ? null : root.getScene().getWindow())) {
            loadProducts();
            showInfo("Thành công", "Đã thêm sản phẩm mới.");
        }
    }

    private void viewSelectedProduct() {
        sanPham selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần xem chi tiết.");
            return;
        }

        ProductDetailDialogFx dialog = new ProductDetailDialogFx(selected.getMaSP(), this::loadProducts);
        dialog.showAndWait(root.getScene() == null ? null : root.getScene().getWindow());
    }

    private void editSelectedProduct() {
        if (!permissionManager.hasPermission("STORE", "UPDATE")) {
            showPermissionWarning();
            return;
        }
        sanPham selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần sửa.");
            return;
        }

        ProductEditDialogFx dialog = new ProductEditDialogFx(selected);
        if (dialog.showAndWait(root.getScene() == null ? null : root.getScene().getWindow())) {
            loadProducts();
            showInfo("Thành công", "Đã cập nhật sản phẩm.");
        }
    }

    private void deleteSelectedProduct() {
        if (!permissionManager.hasPermission("STORE", "DELETE")) {
            showPermissionWarning();
            return;
        }
        sanPham selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm " + safe(selected.getCode()) + "?");
        confirm.setContentText(safe(selected.getTenSP()) + "\n\nSản phẩm đã phát sinh phiếu nhập sẽ không thể xóa.");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        boolean deleted = productDAO.delete(selected.getMaSP());
        if (!deleted) {
            showWarning("Không thể xóa", "Sản phẩm đã phát sinh phiếu nhập hoặc đang được dữ liệu khác sử dụng.");
            return;
        }
        loadProducts();
        showInfo("Đã xóa", "Xóa sản phẩm thành công.");
    }

    private void importFromExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel sản phẩm");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File file = chooser.showOpenDialog(root.getScene() == null ? null : root.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            List<Map<String, Object>> data = ExcelUtils.readExcel(file, 0);
            StringBuilder errors = new StringBuilder();
            int successCount = 0;

            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> row = data.get(i);
                try {
                    String categoryCode = value(row.get("Code danh mục*"));
                    String code = value(row.get("Code sản phẩm*"));
                    String name = value(row.get("Tên sản phẩm*"));
                    double profit = Double.parseDouble(value(row.get("Lợi nhuận*")));
                    double price = Double.parseDouble(value(row.get("Giá bán*")));

                    if (!productDAO.isCategoryExistsByCode(categoryCode)) {
                        errors.append("Dòng ").append(i + 2).append(": Code danh mục '")
                                .append(categoryCode).append("' không tồn tại\n");
                        continue;
                    }
                    if (isProductCodeDuplicate(code)) {
                        errors.append("Dòng ").append(i + 2).append(": Code sản phẩm '")
                                .append(code).append("' đã tồn tại\n");
                        continue;
                    }

                    sanPham product = new sanPham();
                    product.setCode(code);
                    product.setTenSP(name);
                    product.setLoiNhuan(profit);
                    product.setDonGia(price);
                    product.setDanhMucId(productDAO.getCategoryIdByCode(categoryCode));
                    productDAO.insert(product);
                    updateImportedSellPrice(code, price);
                    successCount++;
                } catch (Exception ex) {
                    errors.append("Dòng ").append(i + 2).append(": ").append(ex.getMessage()).append("\n");
                }
            }

            String message = "Import thành công " + successCount + " sản phẩm.";
            if (errors.length() > 0) {
                message += "\n\nCác dòng chưa import:\n" + errors;
            }
            showInfo("Kết quả Import", message);
            loadProducts();
        } catch (Exception ex) {
            showError("Lỗi Import", "Không thể đọc file Excel: " + ex.getMessage());
        }
    }

    private void exportToExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu danh sách sản phẩm");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("SanPham_Export.xlsx");
        File file = chooser.showSaveDialog(root.getScene() == null ? null : root.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try {
            List<String> headers = Arrays.asList("STT", "ID", "Mã", "Danh mục", "Tên", "Tồn kho", "Lợi nhuận", "Đơn giá", "Giá bán", "Trạng thái");
            List<List<Object>> rows = new ArrayList<>();
            for (int i = 0; i < filteredList.size(); i++) {
                sanPham product = filteredList.get(i);
                rows.add(Arrays.asList(
                        i + 1,
                        product.getMaSP(),
                        product.getCode(),
                        product.getTenDanhmuc(),
                        product.getTenSP(),
                        product.getSoLuong(),
                        product.getLoiNhuan(),
                        product.getDonGia(),
                        product.getSellPrice(),
                        product.isActive() ? ACTIVE : INACTIVE));
            }
            ExcelUtils.writeExcel(file, headers, rows);
            showInfo("Export thành công", "Đã lưu file:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showError("Lỗi Export", "Không thể xuất Excel: " + ex.getMessage());
        }
    }

    private void downloadTemplate() {
        try {
            File template = ExcelUtils.createSanPhamTemplate();
            showInfo("Tải Template", "File template đã được tạo tại:\n" + template.getAbsolutePath());
        } catch (Exception ex) {
            showError("Lỗi", "Không thể tạo template: " + ex.getMessage());
        }
    }

    private boolean isProductCodeDuplicate(String code) {
        String sql = "SELECT COUNT(*) AS cnt FROM products WHERE product_code = ?";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            var rs = statement.executeQuery();
            return rs.next() && rs.getInt("cnt") > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void updateImportedSellPrice(String code, double price) {
        String sql = "UPDATE products SET sell_price = ? WHERE product_code = ?";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, price);
            statement.setString(2, code);
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPermissionWarning() {
        showWarning("Không có quyền truy cập", "Tài khoản hiện tại không có quyền thực hiện chức năng này.");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
