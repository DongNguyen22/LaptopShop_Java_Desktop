package GUI.JavaFX;

import DAO.ChiTietPhieuNhapDAO;
import DAO.EmployeeDAO;
import DAO.PhieuNhapDAO;
import DAO.SerialDAO;
import DTO.UserSession;
import DTO.chiTietPhieuNhap;
import DTO.phieuNhap;
import Utils.ExcelUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** JavaFX replacement for GUI.Panel.phieuNhapPanel. */
public final class ReceiptView {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final BorderPane root = new BorderPane();
    private final PhieuNhapDAO receiptDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO detailDAO = new ChiTietPhieuNhapDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final PermissionManager permissionManager = new PermissionManager();
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final TableView<phieuNhap> table = new TableView<>();
    private final ObservableList<phieuNhap> pageItems = FXCollections.observableArrayList();
    private final List<phieuNhap> fullList = new ArrayList<>();
    private final List<phieuNhap> filteredList = new ArrayList<>();
    private final Map<Long, String> employeeNameCache = new HashMap<>();

    private final TextField searchField = new TextField();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private Button previousButton;
    private Button nextButton;

    private int currentPage = 1;
    private int pageSize = 20;

    public ReceiptView() {
        buildView();
        loadData();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("receipt-root");
        root.setPadding(new Insets(18));
        root.setTop(createTop());
        root.setCenter(createTableCard());
        root.setBottom(createPagination());
    }

    private VBox createTop() {
        Label eyebrow = new Label("INVENTORY / RECEIPTS");
        eyebrow.getStyleClass().add("receipt-eyebrow");

        Label title = new Label("Quản lý phiếu nhập");
        title.getStyleClass().add("receipt-title");

        Label subtitle = new Label("Theo dõi phiếu nhập, serial, nhà cung cấp và trạng thái xác nhận.");
        subtitle.getStyleClass().add("receipt-subtitle");

        VBox heading = new VBox(2, eyebrow, title, subtitle);

        Button addButton = createButton("+ Thêm phiếu nhập", "success-button", this::addReceipt);
        addButton.setVisible(permissionManager.hasPermission("RECEIPT", "ADD"));
        addButton.setManaged(addButton.isVisible());

        Button importButton = createButton("Import Excel", "secondary-button", this::importFromExcel);
        Button exportButton = createButton("Export Excel", "secondary-button", this::exportToExcel);
        Button templateButton = createButton("Tải Template", "secondary-button", this::downloadTemplate);
        Button refreshButton = createButton("Làm mới", "secondary-button", this::loadData);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, addButton, importButton, exportButton, templateButton, spacer, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("Tìm kiếm (Mã/NCC)");
        searchLabel.getStyleClass().add("receipt-field-label");
        searchField.setPromptText("Nhập mã phiếu hoặc nhà cung cấp...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilterAndPagination();
        });

        resultLabel.getStyleClass().add("receipt-result-label");
        Region searchSpacer = new Region();
        HBox.setHgrow(searchSpacer, Priority.ALWAYS);
        HBox searchBar = new HBox(10, searchLabel, searchField, searchSpacer, resultLabel);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("receipt-searchbar");

        VBox top = new VBox(14, heading, actions, searchBar);
        top.setPadding(new Insets(2, 2, 14, 2));
        return top;
    }

    private VBox createTableCard() {
        configureTable();
        Label hint = new Label("Double-click một phiếu để xem chi tiết. Chỉ phiếu đang Chờ xác nhận mới có thể xóa.");
        hint.getStyleClass().add("receipt-hint");
        VBox card = new VBox(9, hint, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("receipt-card");
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có phiếu nhập phù hợp."));
        table.getStyleClass().add("receipt-table");

        TableColumn<phieuNhap, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(75);

        TableColumn<phieuNhap, String> supplierCol = new TableColumn<>("Nhà cung cấp");
        supplierCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getNhaCungCap())));
        supplierCol.setPrefWidth(220);

        TableColumn<phieuNhap, String> totalCol = new TableColumn<>("Tổng tiền");
        totalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().getTongTien())));
        totalCol.setPrefWidth(145);

        TableColumn<phieuNhap, String> dateCol = new TableColumn<>("Ngày tạo");
        dateCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getNgayTao() == null ? "N/A" : data.getValue().getNgayTao().format(DATE_TIME_FORMAT)));
        dateCol.setPrefWidth(175);

        TableColumn<phieuNhap, String> employeeCol = new TableColumn<>("Người tạo");
        employeeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(getEmployeeName(data.getValue().getNguoiTao())));
        employeeCol.setPrefWidth(155);

        TableColumn<phieuNhap, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTrangThai()));
        statusCol.setPrefWidth(125);
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("status-waiting", "status-done");
                if (!empty && item != null) {
                    getStyleClass().add("Chờ xác nhận".equals(item) ? "status-waiting" : "status-done");
                }
            }
        });

        TableColumn<phieuNhap, phieuNhap> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionCol.setPrefWidth(95);
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Xóa");
            {
                deleteButton.getStyleClass().addAll("table-action-button", "danger-button");
                deleteButton.setOnAction(event -> {
                    phieuNhap receipt = getItem();
                    if (receipt != null) deleteReceipt(receipt);
                });
            }

            @Override
            protected void updateItem(phieuNhap item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        table.getColumns().setAll(idCol, supplierCol, totalCol, dateCol, employeeCol, statusCol, actionCol);
        table.setRowFactory(tv -> {
            TableRow<phieuNhap> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showDetail(row.getItem().getId());
                }
            });
            return row;
        });
    }

    private HBox createPagination() {
        previousButton = createButton("‹ Trước", "secondary-button", () -> {
            if (currentPage > 1) {
                currentPage--;
                renderPage();
            }
        });
        nextButton = createButton("Sau ›", "secondary-button", () -> {
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

        pageLabel.getStyleClass().add("receipt-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 2, 0, 2));
        return footer;
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("receipt-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    public void loadData() {
        try {
            fullList.clear();
            List<phieuNhap> data = receiptDAO.getAll();
            if (data != null) {
                fullList.addAll(data);
            }
            employeeNameCache.clear();
            currentPage = Math.max(1, currentPage);
            applyFilterAndPagination();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Không thể tải phiếu nhập", safe(ex.getMessage()));
        }
    }

    private void applyFilterAndPagination() {
        String keyword = safe(searchField.getText()).trim().toLowerCase();
        filteredList.clear();
        for (phieuNhap receipt : fullList) {
            String supplier = safe(receipt.getNhaCungCap()).toLowerCase();
            if (keyword.isEmpty()
                    || String.valueOf(receipt.getId()).contains(keyword)
                    || supplier.contains(keyword)) {
                filteredList.add(receipt);
            }
        }
        int pages = totalPages();
        if (currentPage > pages) currentPage = pages;
        if (currentPage < 1) currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int total = filteredList.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredList.subList(start, end));

        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages + "  •  " + total + " phiếu");
        resultLabel.setText(total + " kết quả");
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredList.size() / (double) pageSize));
    }

    private void addReceipt() {
        if (!permissionManager.hasPermission("RECEIPT", "ADD")) {
            showAlert(Alert.AlertType.WARNING, "Không có quyền truy cập", "Bạn không có quyền thêm phiếu nhập.");
            return;
        }
        ReceiptEditDialogFx dialog = new ReceiptEditDialogFx(ownerWindow(), null, this::loadData);
        dialog.showAndWait();
    }

    private void showDetail(long receiptId) {
        ReceiptDetailDialogFx dialog = new ReceiptDetailDialogFx(ownerWindow(), receiptId, this::loadData);
        dialog.showAndWait();
    }

    private void deleteReceipt(phieuNhap receipt) {
        if (receipt == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa phiếu nhập #" + receipt.getId() + "?");
        confirm.setContentText("Thao tác này chỉ thực hiện được khi phiếu đang Chờ xác nhận.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        if (!"Chờ xác nhận".equals(receipt.getTrangThai())) {
            showAlert(Alert.AlertType.WARNING, "Không thể xóa", "Chỉ có thể xóa phiếu nhập đang chờ xác nhận!");
            return;
        }

        try {
            receiptDAO.deleteByReceiptId(receipt.getId());
            loadData();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Xóa thất bại", safe(ex.getMessage()));
        }
    }

    private void importFromExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel phiếu nhập");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File file = chooser.showOpenDialog(ownerWindow());
        if (file == null) return;

        try {
            List<Map<String, Object>> data = ExcelUtils.readExcel(file, 0);
            StringBuilder errors = new StringBuilder();
            int successCount = 0;
            Set<String> serialSet = new HashSet<>();
            Map<Long, Long> receiptMap = new HashMap<>();
            SerialDAO serialDAO = new SerialDAO();
            long empId = UserSession.getInstance().getAccount().getEmployeeId();

            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> row = data.get(i);
                try {
                    String supplierCode = getStringSafe(row, "Mã nhà cung cấp*");
                    String productCode = getStringSafe(row, "Mã sản phẩm*");
                    String serialText = getStringSafe(row, "Serial sản phẩm*");
                    double unitPrice = Double.parseDouble(getStringSafe(row, "Đơn giá nhập*"));
                    long warrantyMonths = Long.parseLong(getStringSafe(row, "Thời gian BH (tháng)"));

                    if (isBlank(supplierCode) || isBlank(productCode)) {
                        errors.append("Dòng ").append(i + 2).append(": thiếu dữ liệu\n");
                        continue;
                    }

                    Long supplierId = serialDAO.getSupplierIdByCode(supplierCode);
                    if (supplierId == null) {
                        errors.append("Dòng ").append(i + 2).append(": NCC không tồn tại -> ").append(supplierCode).append("\n");
                        continue;
                    }
                    Long productId = serialDAO.getProductIdByCode(productCode);
                    if (productId == null) {
                        errors.append("Dòng ").append(i + 2).append(": Sản phẩm không tồn tại -> ").append(productCode).append("\n");
                        continue;
                    }
                    if (!serialDAO.isProductForSupplier(productId, supplierId)) {
                        errors.append("Dòng ").append(i + 2).append(": SP không thuộc NCC\n");
                        continue;
                    }

                    List<String> validSerials = new ArrayList<>();
                    if (!serialText.isEmpty()) {
                        for (String part : serialText.split(";")) {
                            String serial = part.trim();
                            if (serial.isEmpty()) continue;
                            if (serialSet.contains(serial)) {
                                errors.append("Dòng ").append(i + 2).append(": Serial trùng file -> ").append(serial).append("\n");
                                continue;
                            }
                            serialSet.add(serial);
                            if (serialDAO.isSerialExists(serial)) {
                                errors.append("Dòng ").append(i + 2).append(": Serial đã tồn tại -> ").append(serial).append("\n");
                                continue;
                            }
                            validSerials.add(serial);
                        }
                    }
                    if (validSerials.isEmpty()) {
                        errors.append("Dòng ").append(i + 2).append(": Không có serial hợp lệ\n");
                        continue;
                    }

                    Long receiptId = receiptMap.get(supplierId);
                    if (receiptId == null) {
                        phieuNhap receipt = new phieuNhap();
                        receipt.setNhaCungCapId(Math.toIntExact(supplierId));
                        receipt.setNguoiTao(empId);
                        receipt.setTongTien(BigDecimal.ZERO);
                        receiptId = receiptDAO.insert(receipt);
                        if (receiptId <= 0) throw new RuntimeException("Không tạo được phiếu nhập");
                        receiptMap.put(supplierId, receiptId);
                    }

                    chiTietPhieuNhap detail = new chiTietPhieuNhap();
                    detail.setPhieuNhapId(receiptId);
                    detail.setSanPhamId(productId);
                    detail.setSoLuong(validSerials.size());
                    detail.setGiaNhap(BigDecimal.valueOf(unitPrice));
                    detail.setThoiGianBaoHanh((int) warrantyMonths);
                    long receiptItemId = detailDAO.insert(detail);
                    if (receiptItemId <= 0) {
                        errors.append("Dòng ").append(i + 2).append(": lỗi insert chi tiết\n");
                        continue;
                    }
                    serialDAO.addSerialNumbers(receiptId, productId, validSerials);
                    successCount++;
                } catch (Exception ex) {
                    errors.append("Dòng ").append(i + 2).append(": ").append(safe(ex.getMessage())).append("\n");
                }
            }

            for (Long receiptId : receiptMap.values()) {
                receiptDAO.updateTotal(receiptId);
            }

            String message = "Import thành công " + successCount + " dòng!";
            if (errors.length() > 0) message += "\n\nLỗi:\n" + errors;
            showAlert(errors.length() > 0 ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION, "Kết quả Import", message);
            loadData();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đọc file", safe(ex.getMessage()));
        }
    }

    private void exportToExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export phiếu nhập");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("PhieuNhap_Export.xlsx");
        File file = chooser.showSaveDialog(ownerWindow());
        if (file == null) return;
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try {
            List<String> headers = List.of("ID", "Nhà cung cấp", "Tổng tiền", "Ngày tạo", "Người tạo");
            List<List<Object>> rows = new ArrayList<>();
            for (phieuNhap receipt : pageItems) {
                rows.add(List.of(
                        receipt.getId(),
                        safe(receipt.getNhaCungCap()),
                        formatMoney(receipt.getTongTien()),
                        receipt.getNgayTao() == null ? "N/A" : receipt.getNgayTao().format(DATE_TIME_FORMAT),
                        getEmployeeName(receipt.getNguoiTao())
                ));
            }
            ExcelUtils.writeExcel(file, headers, rows);
            showAlert(Alert.AlertType.INFORMATION, "Export thành công", file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi export", safe(ex.getMessage()));
        }
    }

    private void downloadTemplate() {
        try {
            File template = ExcelUtils.createPhieuNhapTemplate();
            showAlert(Alert.AlertType.INFORMATION, "Tải Template thành công", "File template: " + template.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải template", safe(ex.getMessage()));
        }
    }

    private Window ownerWindow() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private String getEmployeeName(long id) {
        return employeeNameCache.computeIfAbsent(id, key -> {
            try {
                String name = employeeDAO.getEmployeeNameById((int) key.longValue());
                return isBlank(name) ? "N/A" : name;
            } catch (Exception ex) {
                return "N/A";
            }
        });
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private String getStringSafe(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        Window owner = ownerWindow();
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
