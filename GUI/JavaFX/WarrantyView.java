package GUI.JavaFX;

import DAO.warrantyCardDAO;
import DTO.warrantyCardDTO;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import valiateData.PermissionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** JavaFX replacement for GUI.Panel.phieuBaoHanhPanel. */
public final class WarrantyView {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BorderPane root = new BorderPane();
    private final warrantyCardDAO dao = new warrantyCardDAO();
    private final PermissionManager permissionManager = new PermissionManager();

    private final TableView<warrantyCardDTO> table = new TableView<>();
    private final ObservableList<warrantyCardDTO> allItems = FXCollections.observableArrayList();
    private final ObservableList<warrantyCardDTO> filteredItems = FXCollections.observableArrayList();
    private final ObservableList<warrantyCardDTO> pageItems = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private final Label totalLabel = new Label("0");
    private final Label processingLabel = new Label("0");
    private final Label completedLabel = new Label("0");

    private Button previousButton;
    private Button nextButton;
    private int currentPage = 1;
    private int pageSize = 20;

    public WarrantyView() {
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
        root.getStyleClass().add("warranty-root");
        root.setPadding(new Insets(20));
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createPagination());
        BorderPane.setMargin(root.getCenter(), new Insets(14, 0, 0, 0));
    }

    private VBox createHeader() {
        Label eyebrow = new Label("WARRANTY / AFTER-SALES");
        eyebrow.getStyleClass().add("warranty-eyebrow");

        Label title = new Label("Quản lý phiếu bảo hành");
        title.getStyleClass().add("warranty-title");

        Label subtitle = new Label("Tiếp nhận, xử lý và theo dõi tiến độ bảo hành sản phẩm theo serial.");
        subtitle.getStyleClass().add("warranty-subtitle");

        HBox stats = new HBox(10,
                statCard("Tổng phiếu", totalLabel),
                statCard("Đang xử lý", processingLabel),
                statCard("Hoàn thành", completedLabel));

        searchField.setPromptText("Mã phiếu / sản phẩm / serial / khách hàng / SĐT...");
        searchField.setPrefWidth(340);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter();
        });

        Button addButton = button("+ Thêm phiếu", "primary-button", this::createWarranty);
        addButton.setVisible(permissionManager.hasPermission("WARRANTY", "ADD"));
        addButton.setManaged(addButton.isVisible());

        Button detailButton = button("Chi tiết", "secondary-button", this::viewSelected);
        Button refreshButton = button("Làm mới", "secondary-button", this::loadData);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(8,
                new Label("Tìm kiếm:"), searchField,
                spacer, refreshButton, detailButton, addButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("warranty-filter-card");
        toolbar.setPadding(new Insets(12));

        VBox box = new VBox(7, eyebrow, title, subtitle, stats, toolbar);
        return box;
    }

    private VBox statCard(String caption, Label value) {
        Label label = new Label(caption);
        label.getStyleClass().add("warranty-stat-caption");
        value.getStyleClass().add("warranty-stat-value");
        VBox card = new VBox(4, label, value);
        card.getStyleClass().add("warranty-stat-card");
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setMinWidth(150);
        return card;
    }

    private VBox createTableCard() {
        configureTable();
        resultLabel.getStyleClass().add("warranty-record-label");
        VBox card = new VBox(8, resultLabel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("warranty-card");
        card.setPadding(new Insets(12));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.getStyleClass().add("warranty-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có phiếu bảo hành phù hợp."));

        TableColumn<warrantyCardDTO, Number> indexCol = new TableColumn<>("STT");
        indexCol.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(pageItems.indexOf(data.getValue()) + 1));
        indexCol.setPrefWidth(52);

        TableColumn<warrantyCardDTO, Number> idCol = new TableColumn<>("Mã");
        idCol.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getWarrantyId()));
        idCol.setPrefWidth(65);

        TableColumn<warrantyCardDTO, String> productCol = new TableColumn<>("Sản phẩm");
        productCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getProductName())));
        productCol.setPrefWidth(180);

        TableColumn<warrantyCardDTO, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getProductSerial())));
        serialCol.setPrefWidth(115);

        TableColumn<warrantyCardDTO, String> customerCol = new TableColumn<>("Khách hàng");
        customerCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getCustomerName())));
        customerCol.setPrefWidth(140);

        TableColumn<warrantyCardDTO, String> phoneCol = new TableColumn<>("SĐT");
        phoneCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getCustomerPhone())));
        phoneCol.setPrefWidth(100);

        TableColumn<warrantyCardDTO, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatusName()));
        statusCol.setPrefWidth(115);
        statusCol.setCellFactory(column -> new TableCell<warrantyCardDTO, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-received", "status-rejected", "status-processing", "status-completed");
                    return;
                }
                setText(value);
                getStyleClass().removeAll("status-received", "status-rejected", "status-processing", "status-completed");
                if ("Đã tiếp nhận".equals(value)) getStyleClass().add("status-received");
                else if ("Từ chối BH".equals(value)) getStyleClass().add("status-rejected");
                else if ("Đang bảo hành".equals(value)) getStyleClass().add("status-processing");
                else if ("Hoàn thành".equals(value)) getStyleClass().add("status-completed");
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<warrantyCardDTO, String> receiveCol = new TableColumn<>("Ngày tiếp nhận");
        receiveCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getReceiveTime() == null ? "" : DATE_TIME.format(data.getValue().getReceiveTime())));
        receiveCol.setPrefWidth(145);

        TableColumn<warrantyCardDTO, String> returnCol = new TableColumn<>("Ngày dự kiến trả");
        returnCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getReturnDate() == null ? "" : DATE.format(data.getValue().getReturnDate())));
        returnCol.setPrefWidth(125);

        table.getColumns().setAll(indexCol, idCol, productCol, serialCol, customerCol, phoneCol,
                statusCol, receiveCol, returnCol);

        table.setRowFactory(tv -> {
            TableRow<warrantyCardDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    viewDetails(row.getItem());
                }
            });
            return row;
        });
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

        pageLabel.getStyleClass().add("warranty-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, pageLabel, spacer,
                new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 2, 0, 2));
        return footer;
    }

    private void loadData() {
        try {
            List<warrantyCardDTO> data = dao.getAll();
            allItems.setAll(data == null ? List.of() : data);
            currentPage = 1;
            applyFilter();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi tải dữ liệu", "Không thể tải danh sách phiếu bảo hành: " + safe(ex.getMessage()));
        }
    }

    private void applyFilter() {
        String keyword = safe(searchField.getText()).trim().toLowerCase(Locale.ROOT);
        filteredItems.clear();
        for (warrantyCardDTO dto : allItems) {
            boolean match = keyword.isEmpty()
                    || safe(dto.getProductName()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(dto.getProductSerial()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(dto.getCustomerName()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(dto.getCustomerPhone()).toLowerCase(Locale.ROOT).contains(keyword)
                    || dto.getStatusName().toLowerCase(Locale.ROOT).contains(keyword)
                    || String.valueOf(dto.getWarrantyId()).contains(keyword)
                    || String.valueOf(dto.getOrderId()).contains(keyword);
            if (match) filteredItems.add(dto);
        }
        int pages = totalPages();
        if (pages == 0) currentPage = 1;
        else if (currentPage > pages) currentPage = pages;
        updateSummary();
        renderPage();
    }

    private void updateSummary() {
        int processing = 0;
        int completed = 0;
        for (warrantyCardDTO dto : filteredItems) {
            if (dto.getStatusCode() == 1 || dto.getStatusCode() == 3) processing++;
            if (dto.getStatusCode() == 4) completed++;
        }
        totalLabel.setText(String.valueOf(filteredItems.size()));
        processingLabel.setText(String.valueOf(processing));
        completedLabel.setText(String.valueOf(completed));
    }

    private void renderPage() {
        pageItems.clear();
        int total = filteredItems.size();
        int start = Math.max(0, (currentPage - 1) * pageSize);
        int end = Math.min(start + pageSize, total);
        if (start < total) pageItems.addAll(filteredItems.subList(start, end));

        int pages = totalPages();
        pageLabel.setText(total == 0 ? "Không có dữ liệu" : "Trang " + currentPage + "/" + pages + " • " + total + " phiếu");
        resultLabel.setText(total == 0
                ? "Không tìm thấy phiếu bảo hành phù hợp."
                : "Hiển thị " + (start + 1) + "–" + end + " trên " + total + " phiếu bảo hành");
        previousButton.setDisable(currentPage <= 1 || total == 0);
        nextButton.setDisable(currentPage >= pages || total == 0);
    }

    private int totalPages() {
        if (filteredItems.isEmpty()) return 0;
        return (int) Math.ceil(filteredItems.size() / (double) pageSize);
    }

    private void createWarranty() {
        if (!permissionManager.hasPermission("WARRANTY", "ADD")) {
            showWarning("Không có quyền", "Bạn không có quyền tạo phiếu bảo hành.");
            return;
        }
        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        new WarrantyEditDialogFx(owner, null, this::loadData).showAndWait();
    }

    private void viewSelected() {
        warrantyCardDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn phiếu", "Vui lòng chọn một phiếu bảo hành để xem chi tiết.");
            return;
        }
        viewDetails(selected);
    }

    private void viewDetails(warrantyCardDTO selected) {
        if (selected == null) return;
        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        new WarrantyDetailDialogFx(owner, selected.getWarrantyId(), this::loadData).showAndWait();
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("warranty-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
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
        alert.setContentText(message == null ? "" : message);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
