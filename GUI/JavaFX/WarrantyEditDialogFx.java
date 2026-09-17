package GUI.JavaFX;

import DAO.EmployeeDAO;
import DAO.warrantyCardDAO;
import DTO.UserSession;
import DTO.WarrantyTempDTO;
import DTO.warrantyCardDTO;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** JavaFX replacement for GUI.Dialog.ThemPhieuBaoHanhDialog. */
public final class WarrantyEditDialogFx {

    private final Stage stage = new Stage();
    private final warrantyCardDAO dao = new warrantyCardDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final Runnable onChanged;
    private final boolean editMode;
    private final warrantyCardDTO currentDTO;

    private final TextField serialField = new TextField();
    private final TextField customerField = new TextField();
    private final TextField orderField = new TextField();
    private final TableView<ProductRow> productTable = new TableView<>();
    private final ObservableList<ProductRow> productRows = FXCollections.observableArrayList();
    private final Label warrantyRemainingLabel = new Label("N/A");
    private final TextArea detailsArea = new TextArea();
    private final Label wordCountLabel = new Label("0 / 500 từ");
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final DatePicker returnDatePicker = new DatePicker();

    private long selectedOrderId = -1;
    private long selectedProductId = -1;
    private String selectedSerial = "";
    private ProductRow selectedRow;

    public WarrantyEditDialogFx(Window owner, warrantyCardDTO dto, Runnable onChanged) {
        this.currentDTO = dto == null ? new warrantyCardDTO() : dto;
        this.editMode = dto != null && dto.getWarrantyId() > 0;
        this.onChanged = onChanged;

        if (owner != null) stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(editMode ? "Chỉnh sửa phiếu bảo hành" : "Tạo phiếu bảo hành");
        stage.setMinWidth(920);
        stage.setMinHeight(680);

        BorderPane root = buildView();
        Scene scene = new Scene(root, 1060, 760);
        URL css = WarrantyEditDialogFx.class.getResource("/GUI/JavaFX/warranty.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);

        if (editMode) loadEditData();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private BorderPane buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("warranty-dialog-root");
        root.setPadding(new Insets(18));

        Label title = new Label(editMode ? "Chỉnh sửa phiếu bảo hành" : "Tạo phiếu bảo hành");
        title.getStyleClass().add("warranty-dialog-title");
        Label subtitle = new Label(editMode
                ? "Cập nhật ngày dự kiến trả, trạng thái và nội dung xử lý."
                : "Tra cứu serial đã bán, kiểm tra thời hạn và tiếp nhận bảo hành.");
        subtitle.getStyleClass().add("warranty-subtitle");
        VBox header = new VBox(4, title, subtitle);
        root.setTop(header);

        VBox content = new VBox(12, createLookupCard(), createProductCard(), createDetailsCard());
        VBox.setVgrow(productTable, Priority.ALWAYS);
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(14, 0, 14, 0));
        root.setBottom(createButtons());
        return root;
    }

    private VBox createLookupCard() {
        Label section = sectionTitle("Khách hàng & đơn hàng");
        serialField.setPromptText("Nhập serial sản phẩm đã bán");
        serialField.setOnAction(event -> searchBySerial());

        Button searchButton = button("Tra cứu", "primary-button", this::searchBySerial);
        HBox serialBox = new HBox(8, serialField, searchButton);
        HBox.setHgrow(serialField, Priority.ALWAYS);

        customerField.setEditable(false);
        orderField.setEditable(false);
        customerField.setPromptText("Khách hàng");
        orderField.setPromptText("Đơn hàng");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Serial:"), 0, 0);
        grid.add(serialBox, 1, 0);
        grid.add(new Label("Tên KH:"), 0, 1);
        grid.add(customerField, 1, 1);
        grid.add(new Label("Đơn hàng:"), 0, 2);
        grid.add(orderField, 1, 2);
        GridPane.setHgrow(serialBox, Priority.ALWAYS);
        GridPane.setHgrow(customerField, Priority.ALWAYS);
        GridPane.setHgrow(orderField, Priority.ALWAYS);

        VBox card = new VBox(10, section, grid);
        card.getStyleClass().add("warranty-card");
        card.setPadding(new Insets(14));
        return card;
    }

    private VBox createProductCard() {
        Label section = sectionTitle("Sản phẩm cần bảo hành");
        configureProductTable();
        productTable.setPrefHeight(260);

        Label caption = new Label("Hạn BH còn lại:");
        warrantyRemainingLabel.getStyleClass().add("warranty-remaining");
        HBox remaining = new HBox(7, caption, warrantyRemainingLabel);
        remaining.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, section, productTable, remaining);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        card.getStyleClass().add("warranty-card");
        card.setPadding(new Insets(14));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureProductTable() {
        productTable.setItems(productRows);
        productTable.getStyleClass().add("warranty-table");
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setPlaceholder(new Label("Nhập serial và bấm Tra cứu để tải sản phẩm trong đơn hàng."));

        TableColumn<ProductRow, ProductRow> chooseCol = new TableColumn<>("Chọn");
        chooseCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        chooseCol.setPrefWidth(58);
        chooseCol.setCellFactory(column -> new TableCell<ProductRow, ProductRow>() {
            private final CheckBox checkBox = new CheckBox();
            {
                setAlignment(Pos.CENTER);
                checkBox.setOnAction(event -> {
                    ProductRow row = getItem();
                    if (row == null) return;
                    if (row.disabled || editMode) {
                        checkBox.setSelected(row == selectedRow);
                        return;
                    }
                    if (checkBox.isSelected()) selectProduct(row);
                    else if (selectedRow == row) clearProductSelection();
                    productTable.refresh();
                });
            }

            @Override
            protected void updateItem(ProductRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                } else {
                    checkBox.setDisable(row.disabled || editMode);
                    checkBox.setSelected(row == selectedRow);
                    setGraphic(checkBox);
                }
            }
        });

        TableColumn<ProductRow, Number> idCol = new TableColumn<>("Mã SP");
        idCol.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().item.getProductId()));
        idCol.setPrefWidth(70);

        TableColumn<ProductRow, String> nameCol = new TableColumn<>("Tên sản phẩm");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().item.getProductName())));
        nameCol.setPrefWidth(220);

        TableColumn<ProductRow, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().item.getProductSerial())));
        serialCol.setPrefWidth(145);

        TableColumn<ProductRow, String> monthsCol = new TableColumn<>("Hạn BH");
        monthsCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().item.getWarrantyMonths() > 0
                        ? data.getValue().item.getWarrantyMonths() + " tháng" : "Không BH"));
        monthsCol.setPrefWidth(92);

        TableColumn<ProductRow, String> remainingCol = new TableColumn<>("Hạn BH còn lại");
        remainingCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().remainingText));
        remainingCol.setPrefWidth(120);

        productTable.getColumns().setAll(chooseCol, idCol, nameCol, serialCol, monthsCol, remainingCol);
        productTable.setRowFactory(tv -> new TableRow<ProductRow>() {
            @Override
            protected void updateItem(ProductRow row, boolean empty) {
                super.updateItem(row, empty);
                getStyleClass().remove("warranty-row-disabled");
                if (!empty && row != null && row.disabled) getStyleClass().add("warranty-row-disabled");
            }
        });
    }

    private VBox createDetailsCard() {
        Label section = sectionTitle("Thông tin bảo hành");

        statusBox.getItems().setAll("Đã tiếp nhận", "Từ chối BH", "Đang bảo hành", "Hoàn thành");
        statusBox.setValue("Đã tiếp nhận");

        returnDatePicker.setPromptText("dd/MM/yyyy");
        returnDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        detailsArea.setPromptText("Mô tả tình trạng, nội dung tiếp nhận hoặc kết quả xử lý...");
        detailsArea.setWrapText(true);
        detailsArea.setPrefRowCount(4);
        detailsArea.textProperty().addListener((obs, oldValue, newValue) -> {
            int count = wordCount(newValue);
            if (count > 500) {
                detailsArea.setText(oldValue);
                detailsArea.positionCaret(oldValue == null ? 0 : oldValue.length());
            } else {
                wordCountLabel.setText(count + " / 500 từ");
            }
        });
        wordCountLabel.getStyleClass().add("warranty-record-label");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);
        grid.add(new Label("Trạng thái:"), 0, 0);
        grid.add(statusBox, 1, 0);
        grid.add(new Label("Ngày dự kiến trả:"), 0, 1);
        grid.add(returnDatePicker, 1, 1);
        grid.add(new Label("Mô tả:"), 0, 2);
        grid.add(detailsArea, 1, 2);
        grid.add(wordCountLabel, 1, 3);
        GridPane.setHgrow(statusBox, Priority.ALWAYS);
        GridPane.setHgrow(returnDatePicker, Priority.ALWAYS);
        GridPane.setHgrow(detailsArea, Priority.ALWAYS);

        VBox card = new VBox(10, section, grid);
        card.getStyleClass().add("warranty-card");
        card.setPadding(new Insets(14));
        return card;
    }

    private HBox createButtons() {
        Button cancel = button("Hủy", "secondary-button", stage::close);
        Button save = button(editMode ? "Lưu thay đổi" : "Tạo phiếu", "success-button", this::save);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(8, spacer, cancel, save);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    private void searchBySerial() {
        if (editMode) return;
        String serial = safe(serialField.getText()).trim();
        if (serial.isEmpty()) {
            error("Vui lòng nhập serial cần tra cứu.");
            return;
        }

        try {
            WarrantyTempDTO temp = dao.getOld(serial);
            if (temp == null) {
                customerField.setText("Không tìm thấy KH");
                orderField.setText("Không tìm thấy thông tin");
                productRows.clear();
                clearProductSelection();
                selectedOrderId = -1;
                error("Không tìm thấy thông tin bán hàng của serial này.");
                return;
            }

            customerField.setText(safe(temp.getCustomerName()));
            orderField.setText(safe(temp.getDisplay()));
            loadOrderProducts(temp.getOrderID());
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Không thể tra cứu serial: " + safe(ex.getMessage()));
        }
    }

    private void loadOrderProducts(long orderId) {
        selectedOrderId = orderId;
        List<warrantyCardDTO> items = dao.getOrderItemsByOrderId(orderId);
        List<warrantyCardDTO> existing = dao.getByOrderId(orderId);
        List<ProductRow> rows = new ArrayList<>();

        if (items != null) {
            for (warrantyCardDTO item : items) {
                boolean beingWarranted = false;
                if (existing != null) {
                    for (warrantyCardDTO wc : existing) {
                        if (safe(wc.getProductSerial()).equals(safe(item.getProductSerial()))
                                && wc.getStatusCode() >= 1 && wc.getStatusCode() <= 3) {
                            beingWarranted = true;
                            break;
                        }
                    }
                }

                String remaining = getWarrantyRemainingText(item);
                if (beingWarranted) remaining = "Đang có BH";
                boolean disabled = beingWarranted || remaining.contains("Hết hạn");

                if (editMode && safe(currentDTO.getProductSerial()).equals(safe(item.getProductSerial()))) {
                    disabled = false;
                }
                rows.add(new ProductRow(item, remaining, disabled));
            }
        }
        productRows.setAll(rows);
    }

    private String getWarrantyRemainingText(warrantyCardDTO item) {
        if (item.getCreatedAt() == null || item.getWarrantyMonths() <= 0) return "Không xác định";
        LocalDateTime expiry = item.getCreatedAt().plusMonths(item.getWarrantyMonths());
        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), expiry);
        if (daysLeft > 30) return (daysLeft / 30) + " tháng";
        if (daysLeft > 0) return daysLeft + " ngày";
        if (daysLeft == 0) return "Hết hạn";
        return "Hết hạn " + Math.abs(daysLeft) + " ngày";
    }

    private void selectProduct(ProductRow row) {
        selectedRow = row;
        selectedProductId = row.item.getProductId();
        selectedSerial = safe(row.item.getProductSerial());
        warrantyRemainingLabel.setText(row.remainingText);
        productTable.getSelectionModel().select(row);
    }

    private void clearProductSelection() {
        selectedRow = null;
        selectedProductId = -1;
        selectedSerial = "";
        warrantyRemainingLabel.setText("N/A");
        productTable.getSelectionModel().clearSelection();
    }

    private void loadEditData() {
        serialField.setText(safe(currentDTO.getProductSerial()));
        serialField.setEditable(false);
        customerField.setText(safe(currentDTO.getCustomerName()));
        orderField.setText("#" + currentDTO.getOrderId());
        selectedOrderId = currentDTO.getOrderId();

        try {
            WarrantyTempDTO temp = dao.getOld(currentDTO.getProductSerial());
            if (temp != null) orderField.setText(safe(temp.getDisplay()));
        } catch (Exception ignored) {
        }

        loadOrderProducts(currentDTO.getOrderId());
        for (ProductRow row : productRows) {
            if (safe(row.item.getProductSerial()).equals(safe(currentDTO.getProductSerial()))) {
                selectProduct(row);
                break;
            }
        }
        productTable.setDisable(true);
        if (currentDTO.getReturnDate() != null) returnDatePicker.setValue(currentDTO.getReturnDate());
        statusBox.setValue(currentDTO.getStatusName());
        detailsArea.setText(safe(currentDTO.getDetails()));
    }

    private void save() {
        LocalDate returnDate = returnDatePicker.getValue();
        if (returnDate == null) {
            error("Vui lòng chọn ngày dự kiến trả.");
            return;
        }

        if (editMode) {
            currentDTO.setReturnDate(returnDate);
            currentDTO.setDetails(safe(detailsArea.getText()));
            currentDTO.setStatusCode(warrantyCardDTO.getStatusCodeByName(statusBox.getValue()));
            try {
                if (dao.update(currentDTO)) {
                    info("Cập nhật phiếu bảo hành thành công.");
                    if (onChanged != null) onChanged.run();
                    stage.close();
                } else {
                    error("Không thể cập nhật phiếu bảo hành.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error("Lỗi khi cập nhật: " + safe(ex.getMessage()));
            }
            return;
        }

        if (selectedOrderId <= 0) {
            error("Vui lòng tra cứu serial để xác định đơn hàng.");
            return;
        }
        if (selectedProductId <= 0 || selectedSerial.isEmpty()) {
            error("Vui lòng chọn sản phẩm/serial cần bảo hành.");
            return;
        }

        currentDTO.setOrderId(selectedOrderId);
        currentDTO.setProductId(selectedProductId);
        currentDTO.setProductSerial(selectedSerial);
        currentDTO.setReturnDate(returnDate);
        currentDTO.setDetails(safe(detailsArea.getText()));
        currentDTO.setStatusCode(warrantyCardDTO.getStatusCodeByName(statusBox.getValue()));

        try {
            long employeeId = UserSession.getInstance().getAccount().getEmployeeId();
            currentDTO.setCreatedUser(employeeId);
            currentDTO.setCreatedUserName(employeeDAO.getEmployeeNameById((int) employeeId));
            if (dao.insert(currentDTO)) {
                info("Tạo phiếu bảo hành thành công.");
                if (onChanged != null) onChanged.run();
                stage.close();
            } else {
                error("Không thể tạo phiếu bảo hành.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Lỗi khi tạo phiếu: " + safe(ex.getMessage()));
        }
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("warranty-section-title");
        return label;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("warranty-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void info(String message) {
        alert(Alert.AlertType.INFORMATION, "Thông báo", message);
    }

    private void error(String message) {
        alert(Alert.AlertType.ERROR, "Lỗi", message);
    }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private static int wordCount(String text) {
        String value = safe(text).trim();
        return value.isEmpty() ? 0 : value.split("\\s+").length;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class ProductRow {
        private final warrantyCardDTO item;
        private final String remainingText;
        private final boolean disabled;

        private ProductRow(warrantyCardDTO item, String remainingText, boolean disabled) {
            this.item = item;
            this.remainingText = remainingText;
            this.disabled = disabled;
        }
    }
}
