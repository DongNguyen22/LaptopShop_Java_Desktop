package GUI.JavaFX;

import Bus.SanPhamBUS;
import Bus.nhaCungCapBUS;
import Bus.supplierProductsBUS;
import DAO.ChiTietPhieuNhapDAO;
import DAO.PhieuNhapDAO;
import DAO.SerialDAO;
import DTO.SerialSanPhamDTO;
import DTO.UserSession;
import DTO.chiTietPhieuNhap;
import DTO.nhaCungCapDTO;
import DTO.phieuNhap;
import DTO.sanPham;
import DTO.supplierProductsDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * JavaFX replacement for GUI.Dialog.ThemPhieuNhapDialogV2. Supports add and
 * edit.
 */
public final class ReceiptEditDialogFx {

    private final Stage stage = new Stage();
    private final Long editReceiptId;
    private final Runnable onSaveCallback;
    private final nhaCungCapBUS supplierBUS = new nhaCungCapBUS();
    private final supplierProductsBUS supplierProductsBUS = new supplierProductsBUS();
    private final SanPhamBUS productBUS = new SanPhamBUS();
    private final PhieuNhapDAO receiptDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO detailDAO = new ChiTietPhieuNhapDAO();
    private final SerialDAO serialDAO = new SerialDAO();
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final ComboBox<SupplierOption> supplierBox = new ComboBox<>();
    private final TableView<ProductOption> supplierProductsTable = new TableView<>();
    private final ObservableList<ProductOption> supplierProducts = FXCollections.observableArrayList();
    private final TableView<ReceiptLine> selectedTable = new TableView<>();
    private final ObservableList<ReceiptLine> selectedLines = FXCollections.observableArrayList();
    private final Label totalLabel = new Label("0 VND");
    private final Set<String> originalSerialsForEdit = new HashSet<>();

    public ReceiptEditDialogFx(Window owner, Long editReceiptId, Runnable onSaveCallback) {
        this.editReceiptId = editReceiptId;
        this.onSaveCallback = onSaveCallback;
        configureStage(owner);
        buildView();
        loadSuppliers();
        if (editReceiptId != null)
            loadEditData();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private void configureStage(Window owner) {
        stage.setTitle(editReceiptId == null ? "Thêm phiếu nhập" : "Sửa phiếu nhập #" + editReceiptId);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null)
            stage.initOwner(owner);
        stage.setWidth(1180);
        stage.setHeight(760);
        stage.setMinWidth(940);
        stage.setMinHeight(640);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("receipt-dialog-root");
        root.setPadding(new Insets(18));
        root.setTop(createHeader());
        root.setCenter(createContent());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        var css = getClass().getResource("/GUI/JavaFX/receipt.css");
        if (css != null)
            scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }

    private VBox createHeader() {
        Label eyebrow = new Label(editReceiptId == null ? "NEW RECEIPT" : "EDIT RECEIPT");
        eyebrow.getStyleClass().add("receipt-eyebrow");
        Label title = new Label(editReceiptId == null ? "Thêm phiếu nhập" : "Sửa phiếu nhập #" + editReceiptId);
        title.getStyleClass().add("receipt-dialog-title");

        supplierBox.setPromptText("Chọn nhà cung cấp");
        supplierBox.setPrefWidth(360);
        supplierBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null)
                loadProductsBySupplier(newValue.id);
        });

        totalLabel.getStyleClass().add("receipt-total");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox info = new HBox(10,
                new Label("Nhà cung cấp:"), supplierBox,
                spacer,
                new Label("Tổng tiền:"), totalLabel);
        info.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(6, eyebrow, title, info);
        header.setPadding(new Insets(0, 0, 14, 0));
        return header;
    }

    private VBox createContent() {
        configureSupplierProductsTable();
        configureSelectedTable();

        Label availableTitle = new Label("Sản phẩm của nhà cung cấp");
        availableTitle.getStyleClass().add("dialog-section-title");
        Label availableHint = new Label("Double-click hoặc dùng nút Thêm để đưa sản phẩm vào phiếu.");
        availableHint.getStyleClass().add("receipt-hint");
        VBox availableCard = new VBox(7, availableTitle, availableHint, supplierProductsTable);
        VBox.setVgrow(supplierProductsTable, Priority.ALWAYS);
        availableCard.getStyleClass().add("receipt-card");
        availableCard.setPadding(new Insets(12));
        availableCard.setPrefHeight(400);

        Label selectedTitle = new Label("Danh sách sản phẩm nhập");
        selectedTitle.getStyleClass().add("dialog-section-title");
        Label selectedHint = new Label(
                "Nhập serial cho từng sản phẩm; số lượng tự động bằng số serial. Đơn giá và thời gian bảo hành có thể sửa trực tiếp.");
        selectedHint.getStyleClass().add("receipt-hint");
        VBox selectedCard = new VBox(7, selectedTitle, selectedHint, selectedTable);
        VBox.setVgrow(selectedTable, Priority.ALWAYS);
        selectedCard.getStyleClass().add("receipt-card");
        selectedCard.setPadding(new Insets(12));

        VBox content = new VBox(12, availableCard, selectedCard);
        VBox.setVgrow(selectedCard, Priority.ALWAYS);
        return content;
    }

    private HBox createFooter() {
        Button saveButton = button("Lưu phiếu nhập", "success-button", this::saveReceipt);
        Button cancelButton = button("Hủy", "secondary-button", stage::close);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, spacer, saveButton, cancelButton);
        footer.setPadding(new Insets(14, 0, 0, 0));
        return footer;
    }

    @SuppressWarnings("unchecked")
    private void configureSupplierProductsTable() {
        supplierProductsTable.setItems(supplierProducts);
        supplierProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        supplierProductsTable.setPlaceholder(
                new Label("Nhà cung cấp chưa có sản phẩm đang hoạt động."));

        TableColumn<ProductOption, String> idCol = new TableColumn<>("Mã");
        idCol.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id)));
        idCol.setPrefWidth(100);

        TableColumn<ProductOption, String> nameCol = new TableColumn<>("Tên sản phẩm");
        nameCol.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().name));
        nameCol.setPrefWidth(700);

        TableColumn<ProductOption, ProductOption> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellValueFactory(
                data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionCol.setPrefWidth(110);

        // Căn giữa tiêu đề cột "Thao tác"
        actionCol.setStyle("-fx-alignment: CENTER;");

        actionCol.setCellFactory(column -> new TableCell<ProductOption, ProductOption>() {

            private final Button add = button("Thêm", "primary-button", () -> {
                ProductOption product = getItem();

                if (product != null) {
                    addProduct(product);
                }
            });

            @Override
            protected void updateItem(ProductOption item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(add);

                    // Căn nút "Thêm" chính giữa ô
                    setAlignment(Pos.CENTER);
                }
            }
        });

        supplierProductsTable.getColumns().setAll(
                idCol,
                nameCol,
                actionCol);

        supplierProductsTable.setRowFactory(tv -> {
            TableRow<ProductOption> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    addProduct(row.getItem());
                }
            });

            return row;
        });
    }

    @SuppressWarnings("unchecked")
    private void configureSelectedTable() {
        selectedTable.setItems(selectedLines);
        selectedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        selectedTable.setPlaceholder(new Label("Chưa có sản phẩm nào trong phiếu."));

        TableColumn<ReceiptLine, String> indexCol = new TableColumn<>("STT");
        indexCol.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(String.valueOf(selectedLines.indexOf(data.getValue()) + 1)));
        indexCol.setPrefWidth(55);
        TableColumn<ReceiptLine, String> nameCol = new TableColumn<>("Sản phẩm");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().name));
        nameCol.setPrefWidth(235);
        TableColumn<ReceiptLine, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().serialSummary()));
        serialCol.setPrefWidth(150);
        serialCol.setCellFactory(column -> new TableCell<>() {
            private final Button serialButton = button("Nhập serial", "secondary-button", () -> {
                ReceiptLine line = getTableView().getItems().get(getIndex());
                editSerials(line);
            });

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    serialButton.setText(item == null || item.startsWith("Chưa") ? "Nhập serial" : item);
                    setGraphic(serialButton);
                    setText(null);
                }
            }
        });
        TableColumn<ReceiptLine, String> quantityCol = new TableColumn<>("Số lượng");
        quantityCol
                .setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().serials.size())));
        quantityCol.setPrefWidth(75);

        TableColumn<ReceiptLine, ReceiptLine> priceCol = new TableColumn<>("Đơn giá");
        priceCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        priceCol.setPrefWidth(130);
        priceCol.setCellFactory(column -> numericCell(false));

        TableColumn<ReceiptLine, ReceiptLine> warrantyCol = new TableColumn<>("BH (tháng)");
        warrantyCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        warrantyCol.setPrefWidth(105);
        warrantyCol.setCellFactory(column -> numericCell(true));

        TableColumn<ReceiptLine, String> totalCol = new TableColumn<>("Thành tiền");
        totalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().lineTotal())));
        totalCol.setPrefWidth(140);

        TableColumn<ReceiptLine, ReceiptLine> deleteCol = new TableColumn<>("Xóa");
        deleteCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        deleteCol.setPrefWidth(75);
        deleteCol.setCellFactory(column -> new TableCell<>() {
            private final Button remove = button("Xóa", "danger-button", () -> {
                ReceiptLine line = getItem();
                if (line != null) {
                    selectedLines.remove(line);
                    updateSupplierLock();
                    recalculateTotal();
                    selectedTable.refresh();
                }
            });

            @Override
            protected void updateItem(ReceiptLine item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : remove);
            }
        });

        selectedTable.getColumns().setAll(indexCol, nameCol, serialCol, quantityCol, priceCol, warrantyCol, totalCol,
                deleteCol);
    }

    private TableCell<ReceiptLine, ReceiptLine> numericCell(boolean warranty) {
        return new TableCell<>() {
            private final TextField field = new TextField();
            private boolean updating;
            {
                field.setPrefWidth(warranty ? 80 : 115);
                field.focusedProperty().addListener((obs, wasFocused, focused) -> {
                    if (!focused && !updating)
                        commitField();
                });
                field.setOnAction(event -> commitField());
            }

            private void commitField() {
                ReceiptLine line = getItem();
                if (line == null)
                    return;
                try {
                    if (warranty) {
                        int value = Integer.parseInt(field.getText().trim());
                        if (value < 0)
                            throw new NumberFormatException();
                        line.warrantyMonths = value;
                    } else {
                        BigDecimal value = new BigDecimal(field.getText().trim().replace(",", ""));
                        if (value.compareTo(BigDecimal.ZERO) < 0)
                            throw new NumberFormatException();
                        line.unitPrice = value;
                    }
                    field.getStyleClass().remove("invalid-field");
                    recalculateTotal();
                    selectedTable.refresh();
                } catch (Exception ex) {
                    if (!field.getStyleClass().contains("invalid-field"))
                        field.getStyleClass().add("invalid-field");
                }
            }

            @Override
            protected void updateItem(ReceiptLine item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    updating = true;
                    field.setText(warranty ? String.valueOf(item.warrantyMonths) : item.unitPrice.toPlainString());
                    updating = false;
                    setGraphic(field);
                }
            }
        };
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("receipt-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void loadSuppliers() {
        supplierBox.getItems().clear();
        List<nhaCungCapDTO> suppliers = supplierBUS.getAllNhaCungCap();
        if (suppliers != null) {
            for (nhaCungCapDTO supplier : suppliers) {
                supplierBox.getItems().add(new SupplierOption(supplier.getSupplier_id(), safe(supplier.getName())));
            }
        }
        if (!supplierBox.getItems().isEmpty() && editReceiptId == null)
            supplierBox.getSelectionModel().selectFirst();
    }

    private void loadProductsBySupplier(int supplierId) {
        supplierProducts.clear();
        List<supplierProductsDTO> links = supplierProductsBUS.getProductsBySupplierId(supplierId);
        if (links == null)
            return;
        for (supplierProductsDTO link : links) {
            sanPham product = productBUS.getProductById(String.valueOf(link.getProduct_id()));
            if (product != null && product.isActive()) {
                supplierProducts.add(new ProductOption(link.getProduct_id(), safe(product.getTenSP())));
            }
        }
    }

    private void loadEditData() {
        try {
            phieuNhap receipt = receiptDAO.getById(editReceiptId);
            if (receipt == null) {
                error("Không tìm thấy phiếu nhập #" + editReceiptId);
                stage.close();
                return;
            }
            for (SupplierOption option : supplierBox.getItems()) {
                if (option.id == receipt.getNhaCungCapId()) {
                    supplierBox.setValue(option);
                    break;
                }
            }

            List<chiTietPhieuNhap> details = detailDAO.getAll(receipt);
            selectedLines.clear();
            originalSerialsForEdit.clear();
            if (details != null) {
                for (chiTietPhieuNhap detail : details) {
                    Long detailId = detailDAO.getIDCTPhieuNhap(editReceiptId, detail.getSanPhamId());
                    List<SerialSanPhamDTO> serialDTOs = serialDAO.getSerial(detailId);
                    List<String> serials = new ArrayList<>();
                    if (serialDTOs != null) {
                        for (SerialSanPhamDTO serial : serialDTOs) {
                            serials.add(serial.getSerial());
                            if (serial.getSerial() != null)
                                originalSerialsForEdit.add(serial.getSerial().trim());
                        }
                    }
                    selectedLines.add(new ReceiptLine(
                            detail.getSanPhamId(),
                            safe(detail.getTenSP()),
                            serials,
                            detail.getGiaNhap() == null ? BigDecimal.ZERO : detail.getGiaNhap(),
                            detail.getThoiGianBaoHanh()));
                }
            }
            updateSupplierLock();
            recalculateTotal();
        } catch (Exception ex) {
            error("Lỗi load dữ liệu sửa: " + safe(ex.getMessage()));
        }
    }

    private void addProduct(ProductOption product) {
        if (product == null)
            return;
        for (ReceiptLine line : selectedLines) {
            if (line.productId == product.id) {
                warning("Sản phẩm này đã được thêm!");
                return;
            }
        }
        ReceiptLine line = new ReceiptLine(product.id, product.name, new ArrayList<>(), BigDecimal.ZERO, 0);
        selectedLines.add(line);
        updateSupplierLock();
        selectedTable.getSelectionModel().select(line);
        editSerials(line);
    }

    private void editSerials(ReceiptLine line) {
        if (line == null)
            return;
        SerialInputDialogFx dialog = new SerialInputDialogFx(stage, line.serials);
        List<String> serials = dialog.showAndWait();
        if (serials != null && !serials.isEmpty()) {
            line.serials = new ArrayList<>(serials);
            recalculateTotal();
            selectedTable.refresh();
        }
    }

    private void updateSupplierLock() {
        supplierBox.setDisable(editReceiptId != null || !selectedLines.isEmpty());
    }

    private void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptLine line : selectedLines)
            total = total.add(line.lineTotal());
        totalLabel.setText(formatMoney(total) + " VND");
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptLine line : selectedLines)
            total = total.add(line.lineTotal());
        return total;
    }

    private void saveReceipt() {
        selectedTable.requestFocus();
        if (selectedLines.isEmpty()) {
            error("Chưa có sản phẩm nào!");
            return;
        }
        SupplierOption supplier = supplierBox.getValue();
        if (supplier == null) {
            error("Vui lòng chọn nhà cung cấp.");
            return;
        }

        Set<String> serialsInReceipt = new HashSet<>();
        for (ReceiptLine line : selectedLines) {
            if (line.serials == null || line.serials.isEmpty()) {
                error("Sản phẩm '" + line.name + "' chưa nhập serial!");
                editSerials(line);
                return;
            }
            if (line.unitPrice == null || line.unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                error("Đơn giá của sản phẩm '" + line.name + "' không hợp lệ.");
                return;
            }
            if (line.warrantyMonths < 0) {
                error("Thời gian bảo hành của sản phẩm '" + line.name + "' không hợp lệ.");
                return;
            }
            for (String serial : line.serials) {
                String normalized = serial == null ? "" : serial.trim();
                if (normalized.isEmpty()) {
                    error("Serial của sản phẩm '" + line.name + "' không được để trống.");
                    return;
                }
                if (!serialsInReceipt.add(normalized)) {
                    error("Serial bị trùng trong phiếu nhập: " + normalized);
                    return;
                }
            }
        }

        try {
            // Kiểm tra serial trước khi ghi dữ liệu. Khi sửa, serial cũ của chính phiếu này
            // được phép giữ lại.
            for (String serial : serialsInReceipt) {
                if (serialDAO.isSerialExists(serial)
                        && (editReceiptId == null || !originalSerialsForEdit.contains(serial))) {
                    error("Serial đã tồn tại trong hệ thống: " + serial);
                    return;
                }
            }

            if (editReceiptId != null && detailDAO.hasAnySerialUsedInOrder(editReceiptId)) {
                error("Phiếu nhập này có serial đã bán nên không thể chỉnh sửa chi tiết.");
                return;
            }

            long employeeId = UserSession.getInstance().getAccount().getEmployeeId();
            BigDecimal total = calculateTotal();
            long receiptId;

            if (editReceiptId == null) {
                phieuNhap receipt = new phieuNhap();
                receipt.setNhaCungCapId(supplier.id);
                receipt.setTongTien(total);
                receipt.setNguoiTao(employeeId);
                receiptId = receiptDAO.insert(receipt);
            } else {
                receiptId = editReceiptId;
                detailDAO.deleteAllByReceiptIdSafe(receiptId);
                receiptDAO.updateTongTien(receiptId, total);
            }

            if (receiptId <= 0)
                throw new RuntimeException("Không tạo/sửa được phiếu nhập");

            for (ReceiptLine line : selectedLines) {
                chiTietPhieuNhap detail = new chiTietPhieuNhap();
                detail.setPhieuNhapId(receiptId);
                detail.setSanPhamId(line.productId);
                detail.setSoLuong(line.serials.size());
                detail.setGiaNhap(line.unitPrice);
                detail.setThoiGianBaoHanh(line.warrantyMonths);
                long detailId = detailDAO.insert(detail);
                if (detailId <= 0)
                    throw new RuntimeException("Không lưu được chi tiết sản phẩm: " + line.name);
                detailDAO.calculateAveragePrice(line.productId);

                Long receiptItemId = detailDAO.getIDCTPhieuNhap(receiptId, line.productId);
                for (String serial : line.serials) {
                    SerialSanPhamDTO dto = new SerialSanPhamDTO();
                    dto.setIdCTPhieuNhap(receiptItemId);
                    dto.setSerial(serial.trim());
                    if (!serialDAO.insert(dto)) {
                        throw new RuntimeException("Không lưu được serial: " + serial);
                    }
                }
            }

            info("Lưu phiếu nhập thành công!");
            if (onSaveCallback != null)
                onSaveCallback.run();
            stage.close();
        } catch (Exception ex) {
            error("Lỗi khi lưu: " + safe(ex.getMessage()));
        }
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private void error(String message) {
        alert(Alert.AlertType.ERROR, "Lỗi", message);
    }

    private void warning(String message) {
        alert(Alert.AlertType.WARNING, "Thông báo", message);
    }

    private void info(String message) {
        alert(Alert.AlertType.INFORMATION, "Thành công", message);
    }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class SupplierOption {
        private final int id;
        private final String name;

        private SupplierOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class ProductOption {
        private final long id;
        private final String name;

        private ProductOption(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final class ReceiptLine {
        private final long productId;
        private final String name;
        private List<String> serials;
        private BigDecimal unitPrice;
        private int warrantyMonths;

        private ReceiptLine(long productId, String name, List<String> serials, BigDecimal unitPrice,
                int warrantyMonths) {
            this.productId = productId;
            this.name = name;
            this.serials = serials == null ? new ArrayList<>() : new ArrayList<>(serials);
            this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
            this.warrantyMonths = warrantyMonths;
        }

        private String serialSummary() {
            return serials.isEmpty() ? "Chưa nhập serial" : "Đã nhập " + serials.size() + " serial";
        }

        private BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(serials.size()));
        }
    }
}
