package GUI.JavaFX;

import Bus.CashLedgerBUS;
import DTO.CashLedgerDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** JavaFX replacement for GUI.Panel.lichSuDonHangPanel. */
public final class TransactionHistoryView {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CashLedgerBUS cashLedgerBUS = new CashLedgerBUS();
    private final BorderPane root = new BorderPane();
    private final TableView<CashLedgerDTO> table = new TableView<>();
    private final ObservableList<CashLedgerDTO> allTransactions = FXCollections.observableArrayList();
    private final ObservableList<CashLedgerDTO> filteredTransactions = FXCollections.observableArrayList();
    private final ObservableList<CashLedgerDTO> pageItems = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final Label saleTotalLabel = new Label("0 VNĐ");
    private final Label purchaseTotalLabel = new Label("0 VNĐ");
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private Button previousButton;
    private Button nextButton;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private int currentPage = 1;
    private int pageSize = 10;

    public TransactionHistoryView() {
        buildView();
        loadData();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("history-root");
        root.setPadding(new Insets(20));
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createPagination());
        BorderPane.setMargin(root.getCenter(), new Insets(14, 0, 0, 0));
    }

    private VBox createHeader() {
        Label eyebrow = new Label("CASH LEDGER");
        eyebrow.getStyleClass().add("history-eyebrow");
        Label title = new Label("LỊCH SỬ GIAO DỊCH");
        title.getStyleClass().add("history-title");
        Label subtitle = new Label("Theo dõi các giao dịch bán hàng và nhập hàng đã ghi nhận trong hệ thống.");
        subtitle.getStyleClass().add("history-subtitle");

        VBox headingText = new VBox(2, eyebrow, title, subtitle);
        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);
        Button refreshButton = button("Làm mới", "secondary-button", this::loadData);
        HBox heading = new HBox(12, headingText, headingSpacer, refreshButton);
        heading.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(12,
                createStatCard("TỔNG BÁN", saleTotalLabel, "sale-stat"),
                createStatCard("TỔNG NHẬP", purchaseTotalLabel, "purchase-stat"));
        HBox.setHgrow(stats.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(stats.getChildren().get(1), Priority.ALWAYS);

        configureDatePicker(fromDatePicker);
        configureDatePicker(toDatePicker);

        searchField.setPromptText("ID, loại, tham chiếu, số tiền, mô tả...");
        searchField.setPrefWidth(290);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter(false);
        });

        Button filterButton = button("Lọc", "primary-button", () -> {
            currentPage = 1;
            applyFilter(true);
        });
        Button clearButton = button("Xóa lọc", "secondary-button", this::clearFilter);
        Button detailButton = button("Xem chi tiết", "success-button", this::showSelectedDetail);

        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("history-field-label");
        Label fromLabel = new Label("Từ ngày");
        fromLabel.getStyleClass().add("history-field-label");
        Label toLabel = new Label("Đến ngày");
        toLabel.getStyleClass().add("history-field-label");
        resultLabel.getStyleClass().add("history-result-label");

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        HBox filters = new HBox(9,
                searchLabel, searchField,
                fromLabel, fromDatePicker,
                toLabel, toDatePicker,
                filterButton, clearButton, detailButton,
                filterSpacer, resultLabel);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("history-searchbar");

        return new VBox(14, heading, stats, filters);
    }

    private VBox createStatCard(String caption, Label valueLabel, String accentClass) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("history-stat-caption");
        valueLabel.getStyleClass().add("history-stat-value");
        VBox card = new VBox(5, captionLabel, valueLabel);
        card.setPadding(new Insets(15, 18, 15, 18));
        card.getStyleClass().addAll("history-stat-card", accentClass);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox createTableCard() {
        configureTable();
        Label hint = new Label("Double-click giao dịch SALE/PURCHASE để xem chứng từ tham chiếu.");
        hint.getStyleClass().add("history-hint");
        VBox card = new VBox(9, hint, table);
        card.getStyleClass().add("history-card");
        card.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có giao dịch phù hợp."));
        table.getStyleClass().add("history-table");

        TableColumn<CashLedgerDTO, Number> sttCol = new TableColumn<>("STT");
        sttCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                pageItems.indexOf(data.getValue()) + 1 + (currentPage - 1) * pageSize));
        sttCol.setPrefWidth(55);

        TableColumn<CashLedgerDTO, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOf(data.getValue().getId())));
        idCol.setPrefWidth(80);

        TableColumn<CashLedgerDTO, String> typeCol = new TableColumn<>("Loại");
        typeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(displayType(data.getValue().getType())));
        typeCol.setPrefWidth(120);

        TableColumn<CashLedgerDTO, String> referenceCol = new TableColumn<>("Tham chiếu");
        referenceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOf(data.getValue().getReferenceId())));
        referenceCol.setPrefWidth(105);

        TableColumn<CashLedgerDTO, String> amountCol = new TableColumn<>("Số tiền");
        amountCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().getAmount()) + " VNĐ"));
        amountCol.setPrefWidth(145);

        TableColumn<CashLedgerDTO, String> descriptionCol = new TableColumn<>("Mô tả");
        descriptionCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getDescription())));
        descriptionCol.setPrefWidth(300);

        TableColumn<CashLedgerDTO, String> createdAtCol = new TableColumn<>("Thời gian");
        createdAtCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getCreatedAt() == null ? "N/A" : data.getValue().getCreatedAt().format(DATE_TIME_FORMAT)));
        createdAtCol.setPrefWidth(165);

        table.getColumns().setAll(sttCol, idCol, typeCol, referenceCol, amountCol, descriptionCol, createdAtCol);
        table.setRowFactory(tv -> {
            TableRow<CashLedgerDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(CashLedgerDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().removeAll("sale-row", "purchase-row", "capital-row");
                    if (!empty && item != null) {
                        String type = safe(item.getType()).toUpperCase(Locale.ROOT);
                        if ("SALE".equals(type)) getStyleClass().add("sale-row");
                        else if ("PURCHASE".equals(type)) getStyleClass().add("purchase-row");
                        else getStyleClass().add("capital-row");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showDetail(row.getItem());
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
        pageLabel.getStyleClass().add("history-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 2, 0, 2));
        return footer;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("history-button", styleClass);
        button.setOnAction(e -> action.run());
        return button;
    }

    public void loadData() {
        try {
            List<CashLedgerDTO> rows = cashLedgerBUS.getTodayTransactions();
            allTransactions.setAll(rows == null ? List.of() : rows);
            currentPage = 1;
            applyFilter(false);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Không thể tải lịch sử giao dịch", safe(ex.getMessage()));
        }
    }

    private void applyFilter(boolean warnInvalidDate) {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from != null && to != null && from.isAfter(to)) {
            if (warnInvalidDate) {
                showAlert(Alert.AlertType.ERROR, "Ngày không hợp lệ", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
            }
            return;
        }

        String keyword = safe(searchField.getText()).trim().toLowerCase(Locale.ROOT);
        filteredTransactions.clear();

        for (CashLedgerDTO dto : allTransactions) {
            if (dto == null) continue;
            LocalDate rowDate = dto.getCreatedAt() == null ? null : dto.getCreatedAt().toLocalDate();
            boolean matchDate = true;
            if (from != null) matchDate = rowDate != null && !rowDate.isBefore(from);
            if (matchDate && to != null) matchDate = rowDate != null && !rowDate.isAfter(to);

            boolean matchText = keyword.isEmpty()
                    || valueOf(dto.getId()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(dto.getType()).toLowerCase(Locale.ROOT).contains(keyword)
                    || displayType(dto.getType()).toLowerCase(Locale.ROOT).contains(keyword)
                    || valueOf(dto.getReferenceId()).toLowerCase(Locale.ROOT).contains(keyword)
                    || formatMoney(dto.getAmount()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(dto.getDescription()).toLowerCase(Locale.ROOT).contains(keyword);

            if (matchDate && matchText) filteredTransactions.add(dto);
        }

        if (currentPage > totalPages()) currentPage = totalPages();
        if (currentPage < 1) currentPage = 1;
        updateStatistics();
        renderPage();
    }

    private void clearFilter() {
        searchField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        currentPage = 1;
        applyFilter(false);
    }

    private void updateStatistics() {
        BigDecimal totalSale = BigDecimal.ZERO;
        BigDecimal totalPurchase = BigDecimal.ZERO;
        for (CashLedgerDTO dto : filteredTransactions) {
            if (dto == null || dto.getAmount() == null) continue;
            if ("SALE".equalsIgnoreCase(dto.getType())) totalSale = totalSale.add(dto.getAmount());
            else if ("PURCHASE".equalsIgnoreCase(dto.getType())) totalPurchase = totalPurchase.add(dto.getAmount());
        }
        saleTotalLabel.setText(formatMoney(totalSale) + " VNĐ");
        purchaseTotalLabel.setText(formatMoney(totalPurchase) + " VNĐ");
    }

    private void renderPage() {
        int total = filteredTransactions.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredTransactions.subList(start, end));
        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages + "  •  " + total + " giao dịch");
        resultLabel.setText(total + " kết quả");
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        table.refresh();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredTransactions.size() / (double) pageSize));
    }

    private void showSelectedDetail() {
        CashLedgerDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn giao dịch", "Vui lòng chọn một giao dịch để xem chi tiết.");
            return;
        }
        showDetail(selected);
    }

    private void showDetail(CashLedgerDTO dto) {
        if (dto == null || dto.getReferenceId() == null) {
            showAlert(Alert.AlertType.INFORMATION, "Không có chứng từ", "Giao dịch này không có mã chứng từ tham chiếu.");
            return;
        }
        String type = safe(dto.getType()).toUpperCase(Locale.ROOT);
        if ("SALE".equals(type)) {
            new OrderDetailDialogFx(ownerWindow(), dto.getReferenceId()).showAndWait();
        } else if ("PURCHASE".equals(type)) {
            new ReceiptDetailDialogFx(ownerWindow(), dto.getReferenceId(), this::loadData).showAndWait();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Chưa có màn hình chi tiết",
                    "Loại giao dịch " + displayType(dto.getType()) + " không có chứng từ chi tiết trong module hiện tại.");
        }
    }

    private void configureDatePicker(DatePicker picker) {
        picker.setPromptText("dd/MM/yyyy");
        picker.setPrefWidth(125);
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE_FORMAT.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) return null;
                try {
                    return LocalDate.parse(text.trim(), DATE_FORMAT);
                } catch (DateTimeParseException ex) {
                    return picker.getValue();
                }
            }
        });
    }

    private Window ownerWindow() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private String displayType(String type) {
        if (type == null) return "N/A";
        switch (type.toUpperCase(Locale.ROOT)) {
            case "SALE": return "Bán hàng (SALE)";
            case "PURCHASE": return "Nhập hàng (PURCHASE)";
            case "CAPITAL_IN": return "Vốn vào";
            case "CAPITAL_OUT": return "Vốn ra";
            default: return type;
        }
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? title : message);
        if (ownerWindow() != null) alert.initOwner(ownerWindow());
        alert.showAndWait();
    }
}
