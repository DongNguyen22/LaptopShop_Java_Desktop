package GUI.JavaFX;

import DAO.ChiTietPhieuNhapDAO;
import DAO.khoDAO;
import DTO.khoDTO;
import Utils.ExcelUtils;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import valiateData.PermissionManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** JavaFX replacement for GUI.Panel.tonKhoPanel. */
public final class InventoryView {

    private final BorderPane root = new BorderPane();
    private final khoDAO inventoryDAO = new khoDAO();
    private final ChiTietPhieuNhapDAO receiptItemDAO = new ChiTietPhieuNhapDAO();
    private final PermissionManager permissionManager = new PermissionManager();

    private final TableView<khoDTO> table = new TableView<>();
    private final ObservableList<khoDTO> allItems = FXCollections.observableArrayList();
    private final ObservableList<khoDTO> filteredItems = FXCollections.observableArrayList();
    private final ObservableList<khoDTO> pageItems = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private final Label lotCountLabel = new Label("0");
    private final Label importedQtyLabel = new Label("0");
    private final Label stockQtyLabel = new Label("0");
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private Button previousButton;
    private Button nextButton;
    private int currentPage = 1;
    private int pageSize = 20;

    public InventoryView() {
        buildView();
        loadData();
    }

    public Parent getRoot() {
        return root;
    }

    public void refreshData() {
        loadData();
    }

    private void buildView() {
        root.getStyleClass().add("inventory-root");
        root.setPadding(new Insets(20));
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createPagination());
        BorderPane.setMargin(root.getCenter(), new Insets(14, 0, 0, 0));
    }

    private VBox createHeader() {
        Label eyebrow = new Label("INVENTORY / WAREHOUSE");
        eyebrow.getStyleClass().add("inventory-eyebrow");
        Label title = new Label("Quản lý tồn kho");
        title.getStyleClass().add("inventory-title");
        Label subtitle = new Label("Theo dõi tồn kho theo từng lô nhập, serial, giá nhập và thời gian bảo hành.");
        subtitle.getStyleClass().add("inventory-subtitle");

        HBox stats = new HBox(10,
                statCard("Số lô đang hiển thị", lotCountLabel),
                statCard("SL nhập", importedQtyLabel),
                statCard("Tồn kho", stockQtyLabel));

        Button exportButton = button("Export Excel", "success-button", this::exportToExcel);
        Button refreshButton = button("Làm mới", "secondary-button", this::loadData);

        searchField.setPromptText("Tên SP / Product ID / Mã phiếu...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter();
        });

        statusBox.getItems().setAll("Tất cả", "Bật", "Tắt");
        statusBox.setValue("Tất cả");
        statusBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox filters = new HBox(8,
                new Label("Tìm kiếm:"), searchField,
                new Label("Trạng thái:"), statusBox,
                spacer, refreshButton, exportButton);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("inventory-filter-card");
        filters.setPadding(new Insets(12));

        VBox header = new VBox(7, eyebrow, title, subtitle, stats, filters);
        header.setPadding(new Insets(0, 0, 2, 0));
        return header;
    }

    private VBox statCard(String caption, Label valueLabel) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("inventory-stat-caption");
        valueLabel.getStyleClass().add("inventory-stat-value");
        VBox card = new VBox(4, captionLabel, valueLabel);
        card.getStyleClass().add("inventory-stat-card");
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setMinWidth(150);
        return card;
    }

    private VBox createTableCard() {
        configureTable();
        resultLabel.getStyleClass().add("inventory-record-label");
        VBox card = new VBox(8, resultLabel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("inventory-card");
        card.setPadding(new Insets(12));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.getStyleClass().add("inventory-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu tồn kho phù hợp."));

        TableColumn<khoDTO, Number> receiptCol = new TableColumn<>("Mã phiếu nhập");
        receiptCol.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getPhieuNhapId()));
        receiptCol.setPrefWidth(115);

        TableColumn<khoDTO, String> nameCol = new TableColumn<>("Tên sản phẩm");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getTenSP())));
        nameCol.setPrefWidth(210);

        TableColumn<khoDTO, khoDTO> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        serialCol.setPrefWidth(85);
        serialCol.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("Xem");
            {
                viewButton.getStyleClass().addAll("inventory-inline-button", "primary-button");
                viewButton.setOnAction(event -> {
                    khoDTO item = getItem();
                    if (item != null) showSerialDetail(item);
                });
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(khoDTO item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : viewButton);
            }
        });

        TableColumn<khoDTO, Number> qtyCol = new TableColumn<>("Số lượng nhập");
        qtyCol.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getSoLuong()));
        qtyCol.setPrefWidth(95);

        TableColumn<khoDTO, Number> priceCol = new TableColumn<>("Giá nhập");
        priceCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getGiaNhap()));
        priceCol.setPrefWidth(110);
        priceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : moneyFormat.format(value.doubleValue()));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        TableColumn<khoDTO, Number> stockCol = new TableColumn<>("Tồn kho");
        stockCol.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getTonKho()));
        stockCol.setPrefWidth(82);

        TableColumn<khoDTO, Number> warrantyCol = new TableColumn<>("BH (tháng)");
        warrantyCol.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getThoiGianBaoHanh()));
        warrantyCol.setPrefWidth(90);

        TableColumn<khoDTO, Boolean> activeCol = new TableColumn<>("Bật/Tắt");
        activeCol.setCellValueFactory(data -> new ReadOnlyBooleanWrapper(data.getValue().isTrangThai()));
        activeCol.setPrefWidth(82);
        activeCol.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private boolean syncing;
            {
                setAlignment(Pos.CENTER);
                checkBox.setOnAction(event -> {
                    if (syncing) return;
                    khoDTO item = getTableRow() == null ? null : (khoDTO) getTableRow().getItem();
                    if (item == null) return;
                    if (!permissionManager.hasPermission("WAREHOUSE", "UPDATE")) {
                        syncing = true;
                        checkBox.setSelected(item.isTrangThai());
                        syncing = false;
                        showWarning("Không có quyền", "Bạn không có quyền cập nhật trạng thái tồn kho.");
                        return;
                    }
                    boolean newValue = checkBox.isSelected();
                    updateReceiptItemActive(item, newValue);
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
                    checkBox.setDisable(!permissionManager.hasPermission("WAREHOUSE", "UPDATE"));
                    syncing = false;
                    setGraphic(checkBox);
                }
            }
        });

        table.getColumns().setAll(receiptCol, nameCol, serialCol, qtyCol, priceCol, stockCol, warrantyCol, activeCol);
    }

    private HBox createPagination() {
        previousButton = button("‹ Trước", "secondary-button", () -> {
            if (currentPage > 1) {
                currentPage--;
                renderPage();
            }
        });
        nextButton = button("Sau ›", "secondary-button", () -> {
            if (currentPage < totalPages()) {
                currentPage++;
                renderPage();
            }
        });

        pageSizeBox.getItems().setAll(10, 20, 30, 50, 100);
        pageSizeBox.setValue(pageSize);
        pageSizeBox.setPrefWidth(85);
        pageSizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 1;
                renderPage();
            }
        });

        pageLabel.getStyleClass().add("inventory-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 2, 0, 2));
        return footer;
    }

    private void loadData() {
        try {
            List<khoDTO> data = inventoryDAO.getAll();
            allItems.setAll(data == null ? List.of() : data);
            currentPage = 1;
            applyFilter();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi tải dữ liệu", "Không thể tải dữ liệu tồn kho: " + safe(ex.getMessage()));
        }
    }

    private void applyFilter() {
        String keyword = safe(searchField.getText()).trim().toLowerCase(Locale.ROOT);
        String status = statusBox.getValue() == null ? "Tất cả" : statusBox.getValue();

        filteredItems.clear();
        for (khoDTO item : allItems) {
            boolean matchSearch = keyword.isEmpty()
                    || safe(item.getTenSP()).toLowerCase(Locale.ROOT).contains(keyword)
                    || String.valueOf(item.getProductId()).contains(keyword)
                    || String.valueOf(item.getPhieuNhapId()).contains(keyword);
            boolean matchStatus = "Tất cả".equals(status)
                    || ("Bật".equals(status) && item.isTrangThai())
                    || ("Tắt".equals(status) && !item.isTrangThai());
            if (matchSearch && matchStatus) filteredItems.add(item);
        }

        int pages = totalPages();
        if (pages == 0) currentPage = 1;
        else if (currentPage > pages) currentPage = pages;
        updateSummary();
        renderPage();
    }

    private void renderPage() {
        pageItems.clear();
        int total = filteredItems.size();
        int start = Math.max(0, (currentPage - 1) * pageSize);
        int end = Math.min(start + pageSize, total);
        if (start < total) pageItems.addAll(filteredItems.subList(start, end));

        int pages = totalPages();
        pageLabel.setText(total == 0
                ? "Không có dữ liệu"
                : "Trang " + currentPage + "/" + pages + " • " + total + " lô");
        resultLabel.setText(total == 0
                ? "Không tìm thấy lô hàng phù hợp."
                : "Hiển thị " + (start + 1) + "–" + end + " trên " + total + " lô hàng");
        previousButton.setDisable(currentPage <= 1 || total == 0);
        nextButton.setDisable(currentPage >= pages || total == 0);
    }

    private int totalPages() {
        if (filteredItems.isEmpty()) return 0;
        return (int) Math.ceil(filteredItems.size() / (double) pageSize);
    }

    private void updateSummary() {
        int imported = 0;
        int stock = 0;
        for (khoDTO item : filteredItems) {
            imported += item.getSoLuong();
            stock += item.getTonKho();
        }
        lotCountLabel.setText(String.valueOf(filteredItems.size()));
        importedQtyLabel.setText(moneyFormat.format(imported));
        stockQtyLabel.setText(moneyFormat.format(stock));
    }

    private void updateReceiptItemActive(khoDTO item, boolean isActive) {
        try {
            inventoryDAO.updateIsActive(item.getPhieuNhapId(), item.getProductId(), isActive);
            receiptItemDAO.calculateAveragePrice(item.getProductId());
            item.setTrangThai(isActive);
            applyFilter();
            showInfo("Cập nhật trạng thái", "Đã cập nhật trạng thái lô hàng: " + (isActive ? "Bật" : "Tắt") + ".");
        } catch (Exception ex) {
            ex.printStackTrace();
            item.setTrangThai(!isActive);
            table.refresh();
            showError("Lỗi cập nhật", "Không thể cập nhật trạng thái lô hàng: " + safe(ex.getMessage()));
        }
    }

    private void showSerialDetail(khoDTO item) {
        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        new SerialDetailDialogFx(owner, item.getTenSP(), item.getPhieuNhapId(), item.getProductId()).showAndWait();
    }

    private void exportToExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu danh sách tồn kho");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("Kho_Export.xlsx");
        File file = chooser.showSaveDialog(root.getScene() == null ? null : root.getScene().getWindow());
        if (file == null) return;
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try {
            List<String> headers = Arrays.asList(
                    "Mã phiếu", "Tên sản phẩm", "Serial", "Số lượng", "Giá nhập", "Tồn kho", "Thời gian BH", "Bật/Tắt");
            List<List<Object>> rows = new ArrayList<>();
            // Giữ hành vi Swing cũ: export các dòng đang hiển thị trên trang hiện tại.
            for (khoDTO item : pageItems) {
                rows.add(Arrays.asList(
                        item.getPhieuNhapId(),
                        item.getTenSP(),
                        "Xem",
                        item.getSoLuong(),
                        item.getGiaNhap(),
                        item.getTonKho(),
                        item.getThoiGianBaoHanh(),
                        item.isTrangThai()));
            }
            ExcelUtils.writeExcel(file, headers, rows);
            showInfo("Export thành công", "Đã lưu file:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi Export", "Không thể xuất Excel: " + safe(ex.getMessage()));
        }
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("inventory-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
