package GUI.JavaFX;

import Bus.promotionsBUS;
import DTO.promotionsDTO;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import valiateData.PermissionManager;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX replacement for GUI.Panel.khuyenMaiPanel.
 * Reuses promotionsBUS/DAO/DTO unchanged.
 */
public final class PromotionView {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private final BorderPane root = new BorderPane();
    private final promotionsBUS promotionBUS = new promotionsBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final TableView<promotionsDTO> table = new TableView<>();
    private final ObservableList<promotionsDTO> pageItems = FXCollections.observableArrayList();
    private final List<promotionsDTO> currentList = new ArrayList<>();

    private final TextField searchField = new TextField();
    private final DatePicker fromDatePicker = new DatePicker(LocalDate.now());
    private final DatePicker toDatePicker = new DatePicker(LocalDate.now());
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Label currentPromoLabel = new Label();
    private final Label filterStatusLabel = new Label();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();

    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button manageItemsButton;
    private Button previousButton;
    private Button nextButton;

    private int currentPage = 1;
    private int pageSize = 20;

    public PromotionView() {
        buildView();
        configureFilters();
        loadPromotions();
    }

    public Parent getRoot() {
        return root;
    }

    public void refresh() {
        loadPromotions();
    }

    private void buildView() {
        root.getStyleClass().add("promotion-root");
        root.setTop(createHeader());

        VBox content = new VBox(12,
                createSearchAndActionBar(),
                createFilterBar(),
                createTableCard(),
                createPaginationBar());
        content.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(content.getChildren().get(2), Priority.ALWAYS);
        root.setCenter(content);
    }

    private VBox createHeader() {
        Label eyebrow = new Label("PROMOTION MANAGEMENT");
        eyebrow.getStyleClass().add("promotion-eyebrow");

        Label title = new Label("Quản lý khuyến mãi");
        title.getStyleClass().add("promotion-title");

        Label subtitle = new Label("Theo dõi chương trình, thời gian áp dụng và sản phẩm được khuyến mãi.");
        subtitle.getStyleClass().add("promotion-subtitle");

        currentPromoLabel.getStyleClass().add("promotion-current-status");
        currentPromoLabel.setWrapText(true);

        VBox copy = new VBox(3, eyebrow, title, subtitle, currentPromoLabel);
        HBox.setHgrow(copy, Priority.ALWAYS);

        resultLabel.getStyleClass().add("promotion-count");

        HBox row = new HBox(18, copy, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.setPadding(new Insets(18, 22, 18, 22));
        header.getStyleClass().add("promotion-header");
        return header;
    }

    private HBox createSearchAndActionBar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Tên khuyến mãi...");
        searchField.setPrefWidth(280);
        searchField.getStyleClass().add("promotion-search-field");
        searchField.setTooltip(new Tooltip("Tìm theo tên chương trình khuyến mãi"));
        searchField.setOnAction(event -> searchPromotions());

        Button searchButton = createButton("Tìm", "secondary-button", this::searchPromotions);
        Button refreshButton = createButton("Làm mới", "secondary-button", this::clearFilters);

        addButton = createButton("Thêm", "success-button", this::addPromotion);
        setPermissionVisibility(addButton, "PROMOTION", "ADD");

        editButton = createButton("Sửa", "primary-button", this::editSelectedPromotion);
        setPermissionVisibility(editButton, "PROMOTION", "UPDATE");

        deleteButton = createButton("Xóa", "danger-button", this::deleteSelectedPromotion);
        setPermissionVisibility(deleteButton, "PROMOTION", "DELETE");

        manageItemsButton = createButton("Quản lý SP", "secondary-button", this::managePromotionItems);
        setPermissionVisibility(manageItemsButton, "PROMOTION", "UPDATE");
        manageItemsButton.setTooltip(new Tooltip("Quản lý sản phẩm áp dụng cho khuyến mãi PERCENT hoặc GIFT"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(9,
                searchLabel, searchField, searchButton,
                spacer,
                refreshButton, addButton, editButton, deleteButton, manageItemsButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12));
        bar.getStyleClass().add("promotion-toolbar");
        return bar;
    }

    private HBox createFilterBar() {
        fromDatePicker.setPrefWidth(135);
        toDatePicker.setPrefWidth(135);
        fromDatePicker.getStyleClass().add("promotion-date-picker");
        toDatePicker.getStyleClass().add("promotion-date-picker");

        Button filterDateButton = createButton("Lọc ngày", "secondary-button", this::filterByDate);

        statusBox.getItems().setAll("Tất cả", "Đang chạy", "Chưa bắt đầu", "Đã kết thúc", "Đã tắt");
        statusBox.setValue("Tất cả");
        statusBox.setPrefWidth(145);
        statusBox.getStyleClass().add("promotion-filter-box");

        Button clearButton = createButton("Xóa lọc", "secondary-button", this::clearFilters);

        filterStatusLabel.getStyleClass().add("promotion-filter-status");
        HBox.setHgrow(filterStatusLabel, Priority.ALWAYS);

        HBox bar = new HBox(9,
                new Label("Từ ngày"), fromDatePicker,
                new Label("Đến ngày"), toDatePicker,
                filterDateButton,
                new Label("Trạng thái"), statusBox,
                clearButton,
                filterStatusLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 12, 10, 12));
        bar.getStyleClass().add("promotion-filterbar");
        return bar;
    }

    private VBox createTableCard() {
        configureTable();
        VBox card = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("promotion-table-card");
        return card;
    }

    private HBox createPaginationBar() {
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
        pageSizeBox.setPrefWidth(82);
        pageSizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 1;
                renderPage();
            }
        });

        pageLabel.getStyleClass().add("pagination-label");
        Label sizeLabel = new Label("Dòng/trang");
        sizeLabel.getStyleClass().add("pagination-muted");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, pageLabel, spacer, sizeLabel, pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 12, 8, 12));
        footer.getStyleClass().add("promotion-pagination");
        return footer;
    }

    private void configureFilters() {
        statusBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                filterByStatus();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label("Không có khuyến mãi phù hợp."));
        table.getStyleClass().add("promotion-table");

        TableColumn<promotionsDTO, Number> sttColumn = new TableColumn<>("STT");
        sttColumn.setMinWidth(48);
        sttColumn.setMaxWidth(60);
        sttColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(0));
        sttColumn.setCellFactory(column -> new TableCell<promotionsDTO, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf((currentPage - 1) * pageSize + getIndex() + 1));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<promotionsDTO, Number> idColumn = new TableColumn<>("Mã KM");
        idColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getPromotion_id()));
        idColumn.setVisible(false);

        TableColumn<promotionsDTO, String> nameColumn = new TableColumn<>("Tên khuyến mãi");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getName())));
        nameColumn.setPrefWidth(180);

        TableColumn<promotionsDTO, String> typeColumn = new TableColumn<>("Loại");
        typeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(typeToDisplay(data.getValue().getPromotion_type())));

        TableColumn<promotionsDTO, String> minColumn = new TableColumn<>("Giá trị tối thiểu");
        minColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(MONEY_FORMAT.format(data.getValue().getMin_paid())));

        TableColumn<promotionsDTO, Number> discountColumn = new TableColumn<>("Giảm giá (%)");
        discountColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getDiscount_on_bill()));

        TableColumn<promotionsDTO, String> startColumn = new TableColumn<>("Bắt đầu");
        startColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDateTime(data.getValue().getStart_at())));
        startColumn.setPrefWidth(135);

        TableColumn<promotionsDTO, String> endColumn = new TableColumn<>("Kết thúc");
        endColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDateTime(data.getValue().getEnd_at())));
        endColumn.setPrefWidth(135);

        TableColumn<promotionsDTO, String> statusColumn = new TableColumn<>("Trạng thái");
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(statusOf(data.getValue())));
        statusColumn.setCellFactory(column -> new TableCell<promotionsDTO, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-running", "status-upcoming", "status-ended", "status-disabled");
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status);
                    setAlignment(Pos.CENTER);
                    if ("Đang chạy".equals(status)) {
                        getStyleClass().add("status-running");
                    } else if ("Chưa bắt đầu".equals(status)) {
                        getStyleClass().add("status-upcoming");
                    } else if ("Đã kết thúc".equals(status)) {
                        getStyleClass().add("status-ended");
                    } else if ("Đã tắt".equals(status)) {
                        getStyleClass().add("status-disabled");
                    }
                }
            }
        });

        TableColumn<promotionsDTO, Number> itemCountColumn = new TableColumn<>("Số SP");
        itemCountColumn.setCellValueFactory(data ->
                new ReadOnlyIntegerWrapper(promotionBUS.countPromotionItems(data.getValue().getPromotion_id())));
        itemCountColumn.setMinWidth(60);
        itemCountColumn.setMaxWidth(80);

        table.getColumns().setAll(sttColumn, idColumn, nameColumn, typeColumn, minColumn, discountColumn,
                startColumn, endColumn, statusColumn, itemCountColumn);

        table.setRowFactory(tv -> {
            TableRow<promotionsDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()
                        && permissionManager.hasPermission("PROMOTION", "UPDATE")) {
                    editPromotion(row.getItem());
                }
            });
            return row;
        });
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("promotion-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void setPermissionVisibility(Button button, String module, String action) {
        boolean visible = permissionManager.hasPermission(module, action);
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void loadPromotions() {
        try {
            searchField.clear();
            currentList.clear();
            ArrayList<promotionsDTO> list = promotionBUS.getAll_2();
            if (list != null) {
                currentList.addAll(list);
            }
            currentPage = 1;
            renderPage();
            updateCurrentPromotion();
            filterStatusLabel.setText("");
        } catch (RuntimeException ex) {
            showError("Không thể tải danh sách khuyến mãi.\n" + safe(ex.getMessage()));
        }
    }

    private void searchPromotions() {
        String keyword = searchField.getText().trim();
        try {
            ArrayList<promotionsDTO> list = promotionBUS.searchPromotions(keyword);
            replaceCurrentList(list);
            filterStatusLabel.setText("Đang tìm: '" + keyword + "' (" + currentList.size() + " kết quả)");
        } catch (RuntimeException ex) {
            showError("Không thể tìm khuyến mãi.\n" + safe(ex.getMessage()));
        }
    }

    private void filterByDate() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        if (fromDate == null || toDate == null) {
            showWarning("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.");
            return;
        }
        if (fromDate.isAfter(toDate)) {
            showWarning("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            return;
        }

        LocalDateTime from = fromDate.atStartOfDay().with(LocalTime.MIN);
        LocalDateTime to = toDate.atTime(LocalTime.MAX);
        try {
            ArrayList<promotionsDTO> list = promotionBUS.filterByDate(from, to);
            replaceCurrentList(list);
            filterStatusLabel.setText("Đang lọc: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " - " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " (" + currentList.size() + " kết quả)");
        } catch (RuntimeException ex) {
            showError("Không thể lọc khuyến mãi theo ngày.\n" + safe(ex.getMessage()));
        }
    }

    private void filterByStatus() {
        String selectedStatus = statusBox.getValue();
        if (selectedStatus == null) {
            return;
        }
        if ("Tất cả".equals(selectedStatus)) {
            try {
                replaceCurrentList(promotionBUS.getAll_2());
                filterStatusLabel.setText("");
            } catch (RuntimeException ex) {
                showError("Không thể tải danh sách khuyến mãi.\n" + safe(ex.getMessage()));
            }
            return;
        }

        try {
            ArrayList<promotionsDTO> all = promotionBUS.getAll_2();
            ArrayList<promotionsDTO> filtered = new ArrayList<>();
            if (all != null) {
                for (promotionsDTO promotion : all) {
                    if (selectedStatus.equals(statusOf(promotion))) {
                        filtered.add(promotion);
                    }
                }
            }
            replaceCurrentList(filtered);
            filterStatusLabel.setText("Đang lọc: Trạng thái '" + selectedStatus + "' ("
                    + currentList.size() + " kết quả)");
        } catch (RuntimeException ex) {
            showError("Không thể lọc theo trạng thái.\n" + safe(ex.getMessage()));
        }
    }

    private void clearFilters() {
        searchField.clear();
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now());
        if (!"Tất cả".equals(statusBox.getValue())) {
            statusBox.setValue("Tất cả");
        } else {
            loadPromotions();
        }
    }

    private void replaceCurrentList(List<promotionsDTO> list) {
        currentList.clear();
        if (list != null) {
            currentList.addAll(list);
        }
        currentPage = 1;
        renderPage();
        updateCurrentPromotion();
    }

    private void renderPage() {
        int totalPages = totalPages();
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        int fromIndex = Math.min((currentPage - 1) * pageSize, currentList.size());
        int toIndex = Math.min(fromIndex + pageSize, currentList.size());
        pageItems.setAll(currentList.subList(fromIndex, toIndex));

        resultLabel.setText(currentList.size() + " chương trình");
        pageLabel.setText("Trang " + currentPage + "/" + totalPages + " • " + currentList.size() + " bản ghi");
        if (previousButton != null) {
            previousButton.setDisable(currentPage <= 1);
        }
        if (nextButton != null) {
            nextButton.setDisable(currentPage >= totalPages);
        }
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(currentList.size() / (double) pageSize));
    }

    private void updateCurrentPromotion() {
        try {
            ArrayList<promotionsDTO> active = promotionBUS.getActivePromotions();
            int count = active == null ? 0 : active.size();
            currentPromoLabel.getStyleClass().removeAll("current-active", "current-empty");
            if (count > 0) {
                StringBuilder builder = new StringBuilder("Đang có ").append(count).append(" khuyến mãi đang chạy: ");
                for (int i = 0; i < Math.min(count, 3); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append(active.get(i).getName());
                }
                if (count > 3) {
                    builder.append("...");
                }
                currentPromoLabel.setText(builder.toString());
                currentPromoLabel.getStyleClass().add("current-active");
            } else {
                currentPromoLabel.setText("Hiện không có khuyến mãi nào đang chạy");
                currentPromoLabel.getStyleClass().add("current-empty");
            }
        } catch (RuntimeException ex) {
            currentPromoLabel.setText("Không thể tải trạng thái khuyến mãi hiện tại");
            currentPromoLabel.getStyleClass().add("current-empty");
        }
    }

    private void addPromotion() {
        if (!permissionManager.hasPermission("PROMOTION", "ADD")) {
            showWarning("Bạn không có quyền thêm khuyến mãi.");
            return;
        }
        PromotionEditDialogFx dialog = new PromotionEditDialogFx(null, promotionBUS);
        if (dialog.showAndWait()) {
            loadPromotions();
            showInfo("Thêm khuyến mãi thành công.");
        }
    }

    private void editSelectedPromotion() {
        if (!permissionManager.hasPermission("PROMOTION", "UPDATE")) {
            showWarning("Bạn không có quyền sửa khuyến mãi.");
            return;
        }
        promotionsDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn khuyến mãi cần sửa.");
            return;
        }
        editPromotion(selected);
    }

    private void editPromotion(promotionsDTO selected) {
        if (selected == null) {
            return;
        }
        promotionsDTO fresh = promotionBUS.getPromotionById(selected.getPromotion_id());
        if (fresh == null) {
            showError("Không tìm thấy thông tin khuyến mãi.");
            return;
        }
        PromotionEditDialogFx dialog = new PromotionEditDialogFx(fresh, promotionBUS);
        if (dialog.showAndWait()) {
            loadPromotions();
            showInfo("Cập nhật khuyến mãi thành công.");
        }
    }

    private void deleteSelectedPromotion() {
        if (!permissionManager.hasPermission("PROMOTION", "DELETE")) {
            showWarning("Bạn không có quyền xóa khuyến mãi.");
            return;
        }
        promotionsDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn khuyến mãi cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa khuyến mãi \"" + safe(selected.getName()) + "\"?");
        confirm.setContentText("Tất cả sản phẩm đang gán cho khuyến mãi này cũng sẽ bị xóa khỏi chương trình khuyến mãi.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            if (promotionBUS.deletePromotion(selected.getPromotion_id())) {
                loadPromotions();
                showInfo("Xóa khuyến mãi thành công.");
            } else {
                showError("Xóa khuyến mãi thất bại.");
            }
        } catch (RuntimeException ex) {
            showError("Không thể xóa khuyến mãi.\n" + safe(ex.getMessage()));
        }
    }

    private void managePromotionItems() {
        if (!permissionManager.hasPermission("PROMOTION", "UPDATE")) {
            showWarning("Bạn không có quyền quản lý sản phẩm khuyến mãi.");
            return;
        }
        promotionsDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn khuyến mãi cần quản lý sản phẩm.");
            return;
        }

        promotionsDTO fresh = promotionBUS.getPromotionById(selected.getPromotion_id());
        if (fresh == null) {
            showError("Không tìm thấy khuyến mãi.");
            return;
        }
        String type = fresh.getPromotion_type();
        if (!"PERCENT".equals(type) && !"GIFT".equals(type)) {
            showInfo("Loại khuyến mãi này không hỗ trợ quản lý sản phẩm.\n"
                    + "Chỉ khuyến mãi Giảm giá % hoặc Tặng sản phẩm sử dụng danh sách sản phẩm.");
            return;
        }

        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        PromotionItemsDialogFx dialog = new PromotionItemsDialogFx(
                owner, fresh.getPromotion_id(), fresh.getName(), promotionBUS);
        dialog.showAndWait();
        loadPromotions();
    }

    private String statusOf(promotionsDTO promotion) {
        if (promotion == null || promotion.getStart_at() == null || promotion.getEnd_at() == null) {
            return "";
        }
        return promotionBUS.getPromotionStatus(
                promotion.getStart_at(), promotion.getEnd_at(), promotion.isStatus());
    }

    private static String typeToDisplay(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "BILL":
                return "Giảm hóa đơn";
            case "PERCENT":
                return "Giảm giá %";
            case "GIFT":
                return "Tặng sản phẩm";
            default:
                return type;
        }
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMAT);
    }

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", message);
    }

    private void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", message);
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Lỗi", message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
