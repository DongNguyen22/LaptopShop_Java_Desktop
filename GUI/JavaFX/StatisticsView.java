package GUI.JavaFX;

import Bus.ThongKeBUS;
import DTO.EmployeeDTO;
import DTO.OrderDTO;
import DTO.OrderItemDTO;
import DTO.ThongKePhuongThucThanhToanDTO;
import DTO.chiTietPhieuNhap;
import DTO.phieuNhap;
import DTO.sanPham;
import Utils.ExcelUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** JavaFX replacement for GUI.Panel.thongKePanel. */
public final class StatisticsView {

    private static final String TYPE_ORDERS = "ĐƠN HÀNG (orders)";
    private static final String TYPE_RECEIPTS = "PHIẾU NHẬP (receipts)";
    private static final String CRITERION_EMPLOYEE = "Nhân viên";
    private static final String CRITERION_PRODUCT = "Sản phẩm";
    private static final String CRITERION_MONEY = "Tiền";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThongKeBUS bus = new ThongKeBUS();
    private final BorderPane root = new BorderPane();
    private final TableView<ReportRow> table = new TableView<>();
    private final ObservableList<ReportRow> pageItems = FXCollections.observableArrayList();

    private final ComboBox<String> typeBox = new ComboBox<>();
    private final ComboBox<String> criterionBox = new ComboBox<>();
    private final ComboBox<EmployeeDTO> employeeBox = new ComboBox<>();
    private final ComboBox<sanPham> productBox = new ComboBox<>();
    private final DatePicker fromDate = new DatePicker(LocalDate.now());
    private final DatePicker toDate = new DatePicker(LocalDate.now());

    private final Label mainSummaryCaption = new Label("TỔNG HỢP");
    private final Label mainSummaryValue = new Label("Chưa có dữ liệu");
    private final Label recordSummaryValue = new Label("0 bản ghi");
    private final Label paymentSummary = new Label();
    private final PieChart paymentChart = new PieChart();
    private final VBox paymentCard = new VBox();
    private final StackPane extraSelectorHolder = new StackPane();

    private final Label pageLabel = new Label();
    private final Label recordLabel = new Label();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private Button firstButton;
    private Button previousButton;
    private Button nextButton;
    private Button lastButton;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalRecords = 0;
    private List<String> currentHeaders = new ArrayList<>();

    public StatisticsView() {
        buildView();
        configureSelectors();
        updateExtraSelector();
        updateSummaryVisibility();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("statistics-root");
        root.setPadding(new Insets(20));
        root.setTop(createHeaderAndFilters());
        root.setCenter(createContent());
        root.setBottom(createPagination());
        BorderPane.setMargin(root.getCenter(), new Insets(14, 0, 0, 0));
    }

    private VBox createHeaderAndFilters() {
        Label eyebrow = new Label("REPORTING & ANALYTICS");
        eyebrow.getStyleClass().add("statistics-eyebrow");
        Label title = new Label("THỐNG KÊ");
        title.getStyleClass().add("statistics-title");
        Label subtitle = new Label("Theo dõi doanh thu, chi phí và hoạt động theo nhân viên hoặc sản phẩm.");
        subtitle.getStyleClass().add("statistics-subtitle");
        VBox headingText = new VBox(2, eyebrow, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox heading = new HBox(12, headingText, spacer);
        heading.setAlignment(Pos.CENTER_LEFT);

        typeBox.getItems().setAll(TYPE_ORDERS, TYPE_RECEIPTS);
        typeBox.setValue(TYPE_ORDERS);
        criterionBox.getItems().setAll(CRITERION_EMPLOYEE, CRITERION_PRODUCT, CRITERION_MONEY);
        criterionBox.setValue(CRITERION_EMPLOYEE);

        configureDatePicker(fromDate);
        configureDatePicker(toDate);
        typeBox.setPrefWidth(190);
        criterionBox.setPrefWidth(135);
        employeeBox.setPrefWidth(220);
        productBox.setPrefWidth(240);
        extraSelectorHolder.setPrefWidth(240);
        extraSelectorHolder.setMinWidth(210);

        VBox typeField = field("Loại", typeBox);
        VBox criterionField = field("Tiêu chí", criterionBox);
        VBox extraField = field("Đối tượng", extraSelectorHolder);
        VBox fromField = field("Từ ngày", fromDate);
        VBox toField = field("Đến ngày", toDate);

        Button reportButton = button("Xuất thống kê", "primary-button", this::loadData);
        Button detailButton = button("Xem chi tiết", "success-button", this::showSelectedDetail);
        Button excelButton = button("Xuất Excel", "secondary-button", this::exportExcel);
        Button pdfButton = button("Xuất PDF", "secondary-button", this::exportPdf);

        HBox filters = new HBox(10, typeField, criterionField, extraField, fromField, toField,
                reportButton, detailButton, excelButton, pdfButton);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        filters.getStyleClass().add("statistics-filter-card");
        filters.setPadding(new Insets(13));

        return new VBox(14, heading, filters);
    }

    private VBox field(String labelText, javafx.scene.Node node) {
        Label label = new Label(labelText);
        label.getStyleClass().add("statistics-field-label");
        return new VBox(5, label, node);
    }

    private HBox createContent() {
        configureTable();
        VBox tableCard = new VBox(8);
        Label tableTitle = new Label("DỮ LIỆU CHI TIẾT");
        tableTitle.getStyleClass().add("statistics-section-title");
        Label hint = new Label("Double-click một dòng để mở chứng từ tương ứng.");
        hint.getStyleClass().add("statistics-hint");
        tableCard.getChildren().addAll(new HBox(12, tableTitle, hint), table);
        tableCard.getStyleClass().add("statistics-card");
        tableCard.setPadding(new Insets(12));
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox summary = createSummaryPanel();
        summary.setPrefWidth(310);
        summary.setMinWidth(285);
        HBox content = new HBox(14, tableCard, summary);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private VBox createSummaryPanel() {
        VBox mainCard = statCard(mainSummaryCaption, mainSummaryValue, "main-stat");
        Label recordCaption = new Label("SỐ BẢN GHI");
        VBox countCard = statCard(recordCaption, recordSummaryValue, "count-stat");

        Label paymentTitle = new Label("PHƯƠNG THỨC THANH TOÁN");
        paymentTitle.getStyleClass().add("statistics-stat-caption");
        paymentSummary.getStyleClass().add("statistics-payment-text");
        paymentSummary.setWrapText(true);
        paymentChart.setLegendVisible(true);
        paymentChart.setLabelsVisible(true);
        paymentChart.setPrefHeight(230);
        paymentCard.getChildren().setAll(paymentTitle, paymentChart, paymentSummary);
        paymentCard.setSpacing(7);
        paymentCard.getStyleClass().addAll("statistics-stat-card", "payment-stat");
        paymentCard.setPadding(new Insets(14));
        paymentCard.setVisible(false);
        paymentCard.setManaged(false);

        VBox summary = new VBox(12, mainCard, countCard, paymentCard);
        return summary;
    }

    private VBox statCard(Label caption, Label value, String accentClass) {
        caption.getStyleClass().add("statistics-stat-caption");
        value.getStyleClass().add("statistics-stat-value");
        value.setWrapText(true);
        VBox card = new VBox(7, caption, value);
        card.getStyleClass().addAll("statistics-stat-card", accentClass);
        card.setPadding(new Insets(15));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Chọn bộ lọc rồi bấm Xuất thống kê."));
        table.getStyleClass().add("statistics-table");
        table.setRowFactory(tv -> {
            TableRow<ReportRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showDetail(row.getItem());
            });
            return row;
        });
        rebuildColumns();
    }

    private void rebuildColumns() {
        boolean orders = isOrders();
        TableColumn<ReportRow, Number> sttCol = new TableColumn<>("STT");
        sttCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStt()));
        sttCol.setPrefWidth(55);

        TableColumn<ReportRow, String> idCol = new TableColumn<>(orders ? "Mã đơn" : "Mã phiếu");
        idCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getReferenceId())));
        idCol.setPrefWidth(105);

        TableColumn<ReportRow, String> moneyCol = new TableColumn<>("Tổng tiền");
        moneyCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(formatMoney(d.getValue().getAmount())));
        moneyCol.setPrefWidth(145);

        TableColumn<ReportRow, String> dateCol = new TableColumn<>("Ngày tạo");
        dateCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().getCreatedAt() == null ? "" : d.getValue().getCreatedAt().format(DATE_TIME_FORMAT)));
        dateCol.setPrefWidth(180);

        if (orders) {
            TableColumn<ReportRow, String> paymentCol = new TableColumn<>("PT Thanh toán");
            paymentCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(safe(d.getValue().getPaymentMethod())));
            paymentCol.setPrefWidth(150);
            table.getColumns().setAll(sttCol, idCol, moneyCol, paymentCol, dateCol);
            currentHeaders = Arrays.asList("STT", "Mã đơn", "Tổng tiền", "PT Thanh toán", "Ngày tạo");
        } else {
            table.getColumns().setAll(sttCol, idCol, moneyCol, dateCol);
            currentHeaders = Arrays.asList("STT", "Mã phiếu", "Tổng tiền", "Ngày tạo");
        }
    }

    private HBox createPagination() {
        firstButton = button("≪", "page-button", () -> goToPage(1));
        previousButton = button("‹", "page-button", () -> goToPage(currentPage - 1));
        nextButton = button("›", "page-button", () -> goToPage(currentPage + 1));
        lastButton = button("≫", "page-button", () -> goToPage(totalPages()));

        pageSizeBox.getItems().setAll(10, 20, 30, 50, 100);
        pageSizeBox.setValue(pageSize);
        pageSizeBox.setPrefWidth(82);
        pageSizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 1;
                if (hasValidSelection(false)) {
                    totalRecords = getTotalRecords();
                    loadTablePage();
                } else {
                    updatePaginationLabels();
                }
            }
        });

        pageLabel.getStyleClass().add("statistics-page-label");
        recordLabel.getStyleClass().add("statistics-record-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, pageLabel, recordLabel, spacer, new Label("Dòng/trang"), pageSizeBox,
                firstButton, previousButton, nextButton, lastButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 2, 0, 2));
        updatePaginationLabels();
        return footer;
    }

    private void configureSelectors() {
        employeeBox.setPromptText("-- Chọn nhân viên --");
        productBox.setPromptText("-- Chọn sản phẩm --");
        employeeBox.setConverter(new StringConverter<EmployeeDTO>() {
            @Override public String toString(EmployeeDTO emp) {
                return emp == null ? "" : emp.getEmployeeName() + " - ID: " + emp.getEmployeeId();
            }
            @Override public EmployeeDTO fromString(String string) { return null; }
        });
        productBox.setConverter(new StringConverter<sanPham>() {
            @Override public String toString(sanPham sp) {
                return sp == null ? "" : sp.getMaSP() + " - " + sp.getTenSP();
            }
            @Override public sanPham fromString(String string) { return null; }
        });

        criterionBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateExtraSelector();
            clearReport();
            updateSummaryVisibility();
        });
        typeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            rebuildColumns();
            clearReport();
            updateSummaryVisibility();
        });
    }

    private void updateExtraSelector() {
        String criterion = criterionBox.getValue();
        extraSelectorHolder.getChildren().clear();
        if (CRITERION_EMPLOYEE.equals(criterion)) {
            employeeBox.getItems().setAll(bus.getEmployees());
            extraSelectorHolder.getChildren().add(employeeBox);
        } else if (CRITERION_PRODUCT.equals(criterion)) {
            productBox.getItems().setAll(bus.getProducts());
            extraSelectorHolder.getChildren().add(productBox);
        } else {
            Label allLabel = new Label("Toàn bộ dữ liệu");
            allLabel.getStyleClass().add("statistics-all-label");
            extraSelectorHolder.getChildren().add(allLabel);
        }
    }

    private void loadData() {
        if (!validateDates() || !hasValidSelection(true)) return;
        currentPage = 1;
        loadSummary();
        totalRecords = getTotalRecords();
        recordSummaryValue.setText(totalRecords + " bản ghi");
        loadTablePage();
        updateSummaryVisibility();
    }

    private boolean validateDates() {
        if (fromDate.getValue() == null || toDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu ngày", "Vui lòng chọn Từ ngày và Đến ngày.");
            return false;
        }
        if (fromDate.getValue().isAfter(toDate.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Ngày không hợp lệ", "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày.");
            return false;
        }
        return true;
    }

    private boolean hasValidSelection(boolean showMessage) {
        String criterion = criterionBox.getValue();
        if (CRITERION_EMPLOYEE.equals(criterion) && employeeBox.getValue() == null) {
            if (showMessage) showAlert(Alert.AlertType.WARNING, "Thiếu nhân viên", "Vui lòng chọn nhân viên cần thống kê.");
            return false;
        }
        if (CRITERION_PRODUCT.equals(criterion) && productBox.getValue() == null) {
            if (showMessage) showAlert(Alert.AlertType.WARNING, "Thiếu sản phẩm", "Vui lòng chọn sản phẩm cần thống kê.");
            return false;
        }
        return true;
    }

    private int getTotalRecords() {
        String start = startDateTime();
        String end = endDateTime();
        String type = busType();
        String criterion = criterionBox.getValue();
        try {
            if (CRITERION_EMPLOYEE.equals(criterion)) {
                long id = employeeBox.getValue().getEmployeeId();
                return isOrders() ? bus.countOrdersByEmployee(id, start, end)
                        : bus.countReceiptsByEmployee(id, start, end);
            }
            if (CRITERION_PRODUCT.equals(criterion)) {
                long id = Long.parseLong(productBox.getValue().getMaSP());
                return isOrders() ? bus.countOrdersByProduct(id, start, end)
                        : bus.countReceiptsByProduct(id, start, end);
            }
            return isOrders() ? bus.countAllOrders(start, end) : bus.countAllReceipts(start, end);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi thống kê", "Không thể đếm dữ liệu: " + ex.getMessage());
            return 0;
        }
    }

    private void loadSummary() {
        paymentSummary.setText("");
        paymentChart.getData().clear();
        String start = startDateTime();
        String end = endDateTime();
        String type = busType();
        String criterion = criterionBox.getValue();
        try {
            if (CRITERION_EMPLOYEE.equals(criterion)) {
                double[] summary = bus.getSummaryByEmployee(type, employeeBox.getValue().getEmployeeId(), start, end);
                if (isOrders()) {
                    mainSummaryCaption.setText("DOANH THU THEO NHÂN VIÊN");
                    mainSummaryValue.setText("Tổng số đơn: " + (int) summary[1] + "\n" + formatMoney(summary[0]) + " VNĐ");
                } else {
                    mainSummaryCaption.setText("CHI NHẬP THEO NHÂN VIÊN");
                    mainSummaryValue.setText("Tổng số phiếu: " + (int) summary[1] + "\n" + formatMoney(summary[0]) + " VNĐ");
                }
            } else if (CRITERION_PRODUCT.equals(criterion)) {
                long productId = Long.parseLong(productBox.getValue().getMaSP());
                double[] summary = bus.getSummaryByProduct(type, productId, start, end);
                if (isOrders()) {
                    mainSummaryCaption.setText("DOANH THU THEO SẢN PHẨM");
                    mainSummaryValue.setText("Serial bán: " + (int) summary[0] + "\n" + formatMoney(summary[1]) + " VNĐ");
                } else {
                    mainSummaryCaption.setText("CHI NHẬP THEO SẢN PHẨM");
                    mainSummaryValue.setText("Serial nhập: " + (int) summary[0] + "\n" + formatMoney(summary[1]) + " VNĐ");
                }
            } else {
                if (isOrders()) {
                    ArrayList<OrderDTO> list = bus.getAllOrders(start, end);
                    double total = list.stream().mapToDouble(o -> o.getTotal().doubleValue()).sum();
                    mainSummaryCaption.setText("TỔNG THU");
                    mainSummaryValue.setText(formatMoney(total) + " VNĐ");
                    loadPaymentSummary(start, end);
                } else {
                    ArrayList<phieuNhap> list = bus.getAllReceipts(start, end);
                    double total = list.stream().mapToDouble(p -> p.getTongTien().doubleValue()).sum();
                    mainSummaryCaption.setText("TỔNG CHI");
                    mainSummaryValue.setText(formatMoney(total) + " VNĐ");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi thống kê", "Không thể tải tổng hợp: " + ex.getMessage());
        }
    }

    private void loadPaymentSummary(String start, String end) {
        ArrayList<ThongKePhuongThucThanhToanDTO> payments = bus.getPaymentData(start, end);
        StringBuilder text = new StringBuilder();
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        for (ThongKePhuongThucThanhToanDTO item : payments) {
            String method = safe(item.getPhuongThuc());
            text.append(method).append(": ").append(formatMoney(item.getTongTien())).append(" VNĐ\n");
            if (item.getTongTien() > 0) chartData.add(new PieChart.Data(method, item.getTongTien()));
        }
        paymentSummary.setText(text.toString().trim());
        paymentChart.setData(chartData);
    }

    private void loadTablePage() {
        pageItems.clear();
        if (!hasValidSelection(false) || !validateDatesSilently()) {
            updatePaginationLabels();
            return;
        }
        rebuildColumns();
        int offset = (currentPage - 1) * pageSize;
        String start = startDateTime();
        String end = endDateTime();
        String criterion = criterionBox.getValue();
        int stt = offset + 1;
        try {
            if (CRITERION_EMPLOYEE.equals(criterion)) {
                long id = employeeBox.getValue().getEmployeeId();
                if (isOrders()) {
                    for (OrderDTO dto : bus.getOrdersByEmployeePaginated(id, start, end, offset, pageSize)) {
                        pageItems.add(orderRow(stt++, dto));
                    }
                } else {
                    for (phieuNhap dto : bus.getReceiptsByEmployeePaginated(id, start, end, offset, pageSize)) {
                        pageItems.add(receiptRow(stt++, dto));
                    }
                }
            } else if (CRITERION_PRODUCT.equals(criterion)) {
                long id = Long.parseLong(productBox.getValue().getMaSP());
                if (isOrders()) {
                    for (OrderItemDTO dto : bus.getOrdersByProductPaginated(id, start, end, offset, pageSize)) {
                        pageItems.add(new ReportRow(stt++, dto.getOrderId(), dto.getUnitPrice(),
                                dto.getPaymentMethod(), dto.getCreatedAt()));
                    }
                } else {
                    for (chiTietPhieuNhap dto : bus.getReceiptsByProductPaginated(id, start, end, offset, pageSize)) {
                        pageItems.add(new ReportRow(stt++, dto.getPhieuNhapId(), dto.getGiaNhap(),
                                null, dto.getNgayTao()));
                    }
                }
            } else {
                if (isOrders()) {
                    for (OrderDTO dto : bus.getAllOrdersPaginated(start, end, offset, pageSize)) {
                        pageItems.add(orderRow(stt++, dto));
                    }
                } else {
                    for (phieuNhap dto : bus.getAllReceiptsPaginated(start, end, offset, pageSize)) {
                        pageItems.add(receiptRow(stt++, dto));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi thống kê", "Không thể tải dữ liệu: " + ex.getMessage());
        }
        updatePaginationLabels();
    }

    private ReportRow orderRow(int stt, OrderDTO dto) {
        return new ReportRow(stt, dto.getOrder_id(), dto.getTotal(), dto.getPayment_method(), dto.getCreated_at());
    }

    private ReportRow receiptRow(int stt, phieuNhap dto) {
        return new ReportRow(stt, dto.getId(), dto.getTongTien(), null, dto.getNgayTao());
    }

    private void showSelectedDetail() {
        ReportRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn dữ liệu", "Vui lòng chọn một dòng để xem chi tiết.");
            return;
        }
        showDetail(selected);
    }

    private void showDetail(ReportRow row) {
        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        if (isOrders()) {
            new OrderDetailDialogFx(owner, row.getReferenceId()).showAndWait();
        } else {
            new ReceiptDetailDialogFx(owner, row.getReferenceId(), this::loadData).showAndWait();
        }
    }

    private void exportExcel() {
        if (pageItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Không có dữ liệu ở trang hiện tại để xuất Excel.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu file Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("thongke.xlsx");
        File file = chooser.showSaveDialog(window());
        if (file == null) return;
        file = ensureExtension(file, ".xlsx");
        try {
            List<List<Object>> rows = new ArrayList<>();
            for (ReportRow row : pageItems) rows.add(toExcelRow(row));
            ExcelUtils.writeExcel(file, new ArrayList<>(currentHeaders), rows);
            showAlert(Alert.AlertType.INFORMATION, "Xuất Excel", "Xuất file thành công:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi xuất Excel: " + ex.getMessage());
        }
    }

    private List<Object> toExcelRow(ReportRow row) {
        if (isOrders()) {
            return Arrays.asList(row.getStt(), row.getReferenceId(), formatMoney(row.getAmount()),
                    safe(row.getPaymentMethod()), formatDateTime(row.getCreatedAt()));
        }
        return Arrays.asList(row.getStt(), row.getReferenceId(), formatMoney(row.getAmount()),
                formatDateTime(row.getCreatedAt()));
    }

    private void exportPdf() {
        if (!validateDates() || !hasValidSelection(true)) return;
        List<ReportRow> allRows = getAllRows();
        if (allRows.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Không có dữ liệu để xuất PDF.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu file PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName("thongke.pdf");
        File file = chooser.showSaveDialog(window());
        if (file == null) return;
        file = ensureExtension(file, ".pdf");
        try (FileOutputStream output = new FileOutputStream(file)) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, output);
            document.open();

            BaseFont baseFont;
            try {
                baseFont = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            }
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(baseFont, 11);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(baseFont, 11, com.itextpdf.text.Font.BOLD);

            Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Loại: " + typeBox.getValue(), normalFont));
            document.add(new Paragraph("Tiêu chí: " + criterionBox.getValue(), normalFont));
            if (CRITERION_EMPLOYEE.equals(criterionBox.getValue()) && employeeBox.getValue() != null) {
                document.add(new Paragraph("Nhân viên: " + employeeBox.getValue().getEmployeeName(), normalFont));
            } else if (CRITERION_PRODUCT.equals(criterionBox.getValue()) && productBox.getValue() != null) {
                document.add(new Paragraph("Sản phẩm: " + productBox.getValue().getTenSP(), normalFont));
            }
            document.add(new Paragraph("Từ ngày: " + fromDate.getValue().format(DATE_DISPLAY)
                    + " đến " + toDate.getValue().format(DATE_DISPLAY), normalFont));
            document.add(Chunk.NEWLINE);

            PdfPTable pdfTable = new PdfPTable(currentHeaders.size());
            pdfTable.setWidthPercentage(100);
            for (String header : currentHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(cell);
            }
            for (ReportRow row : allRows) {
                for (Object value : toExcelRow(row)) {
                    PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value.toString(), normalFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfTable.addCell(cell);
                }
            }
            document.add(pdfTable);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(mainSummaryCaption.getText() + ": " + mainSummaryValue.getText().replace("\n", " - "), headerFont));
            if (paymentCard.isVisible() && !paymentSummary.getText().isEmpty()) {
                document.add(new Paragraph("Chi tiết thanh toán:", headerFont));
                document.add(new Paragraph(paymentSummary.getText(), normalFont));
            }
            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Xuất PDF", "Xuất PDF thành công:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi xuất PDF: " + ex.getMessage());
        }
    }

    private List<ReportRow> getAllRows() {
        List<ReportRow> rows = new ArrayList<>();
        String start = startDateTime();
        String end = endDateTime();
        String criterion = criterionBox.getValue();
        int stt = 1;
        try {
            if (CRITERION_EMPLOYEE.equals(criterion)) {
                long id = employeeBox.getValue().getEmployeeId();
                if (isOrders()) {
                    for (OrderDTO dto : bus.getOrdersByEmployee(id, start, end)) rows.add(orderRow(stt++, dto));
                } else {
                    for (phieuNhap dto : bus.getReceiptsByEmployee(id, start, end)) rows.add(receiptRow(stt++, dto));
                }
            } else if (CRITERION_PRODUCT.equals(criterion)) {
                long id = Long.parseLong(productBox.getValue().getMaSP());
                if (isOrders()) {
                    for (OrderItemDTO dto : bus.getOrdersByProduct(id, start, end)) {
                        rows.add(new ReportRow(stt++, dto.getOrderId(), dto.getUnitPrice(), dto.getPaymentMethod(), dto.getCreatedAt()));
                    }
                } else {
                    for (chiTietPhieuNhap dto : bus.getReceiptsByProduct(id, start, end)) {
                        rows.add(new ReportRow(stt++, dto.getPhieuNhapId(), dto.getGiaNhap(), null, dto.getNgayTao()));
                    }
                }
            } else if (isOrders()) {
                for (OrderDTO dto : bus.getAllOrders(start, end)) rows.add(orderRow(stt++, dto));
            } else {
                for (phieuNhap dto : bus.getAllReceipts(start, end)) rows.add(receiptRow(stt++, dto));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi thống kê", "Không thể tải dữ liệu xuất PDF: " + ex.getMessage());
        }
        return rows;
    }

    private void updateSummaryVisibility() {
        boolean showPayment = CRITERION_MONEY.equals(criterionBox.getValue()) && isOrders();
        paymentCard.setVisible(showPayment);
        paymentCard.setManaged(showPayment);
    }

    private void clearReport() {
        pageItems.clear();
        totalRecords = 0;
        currentPage = 1;
        mainSummaryCaption.setText("TỔNG HỢP");
        mainSummaryValue.setText("Chưa có dữ liệu");
        recordSummaryValue.setText("0 bản ghi");
        paymentSummary.setText("");
        paymentChart.getData().clear();
        updatePaginationLabels();
    }

    private void goToPage(int page) {
        if (page < 1 || page > totalPages() || page == currentPage) return;
        currentPage = page;
        loadTablePage();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(totalRecords / (double) pageSize));
    }

    private void updatePaginationLabels() {
        int pages = totalPages();
        if (currentPage > pages) currentPage = pages;
        int start = totalRecords == 0 ? 0 : (currentPage - 1) * pageSize + 1;
        int end = totalRecords == 0 ? 0 : Math.min(currentPage * pageSize, totalRecords);
        pageLabel.setText("Trang " + currentPage + " / " + pages);
        recordLabel.setText("Hiển thị " + start + " - " + end + " / " + totalRecords);
        firstButton.setDisable(currentPage <= 1);
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        lastButton.setDisable(currentPage >= pages);
    }

    private String startDateTime() {
        return fromDate.getValue().toString() + " 00:00:00";
    }

    private String endDateTime() {
        return toDate.getValue().toString() + " 23:59:59";
    }

    private String busType() {
        return isOrders() ? "orders" : "receipts";
    }

    private boolean isOrders() {
        return TYPE_ORDERS.equals(typeBox.getValue());
    }

    private boolean validateDatesSilently() {
        return fromDate.getValue() != null && toDate.getValue() != null && !fromDate.getValue().isAfter(toDate.getValue());
    }

    private void configureDatePicker(DatePicker picker) {
        picker.setPrefWidth(135);
        picker.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate value) {
                return value == null ? "" : value.format(DATE_DISPLAY);
            }
            @Override public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) return null;
                return LocalDate.parse(text.trim(), DATE_DISPLAY);
            }
        });
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("statistics-button", styleClass);
        button.setOnAction(e -> action.run());
        return button;
    }

    private String formatMoney(BigDecimal value) {
        return value == null ? "0" : moneyFormat.format(value.doubleValue());
    }

    private String formatMoney(double value) {
        return moneyFormat.format(value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMAT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Window window() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private File ensureExtension(File file, String extension) {
        if (file.getName().toLowerCase().endsWith(extension)) return file;
        return new File(file.getAbsolutePath() + extension);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (window() != null) alert.initOwner(window());
        alert.showAndWait();
    }

    private static final class ReportRow {
        private final int stt;
        private final long referenceId;
        private final BigDecimal amount;
        private final String paymentMethod;
        private final LocalDateTime createdAt;

        private ReportRow(int stt, long referenceId, BigDecimal amount, String paymentMethod, LocalDateTime createdAt) {
            this.stt = stt;
            this.referenceId = referenceId;
            this.amount = amount == null ? BigDecimal.ZERO : amount;
            this.paymentMethod = paymentMethod;
            this.createdAt = createdAt;
        }

        int getStt() { return stt; }
        long getReferenceId() { return referenceId; }
        BigDecimal getAmount() { return amount; }
        String getPaymentMethod() { return paymentMethod; }
        LocalDateTime getCreatedAt() { return createdAt; }
    }
}
