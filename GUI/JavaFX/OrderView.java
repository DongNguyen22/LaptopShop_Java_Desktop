package GUI.JavaFX;

import Bus.CustomerBUS;
import Bus.EmployeeBUS;
import Bus.OrderBUS;
import DTO.EmployeeDTO;
import DTO.NewOrderDTO;
import DTO.OrderDetailDTO;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import valiateData.PermissionManager;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** JavaFX replacement for GUI.Panel.donHangPanel. */
public final class OrderView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private final BorderPane root = new BorderPane();
    private final OrderBUS orderBUS = new OrderBUS();
    private final CustomerBUS customerBUS = new CustomerBUS();
    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final PermissionManager permissionManager = new PermissionManager();
    private final EmployeeDTO allEmployeesOption = createAllEmployeesOption();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");

    private final TableView<NewOrderDTO> table = new TableView<>();
    private final ObservableList<NewOrderDTO> pageItems = FXCollections.observableArrayList();
    private final List<NewOrderDTO> allOrders = new ArrayList<>();
    private final List<NewOrderDTO> filteredOrders = new ArrayList<>();
    private final TextField searchField = new TextField();
    private final ComboBox<EmployeeDTO> employeeBox = new ComboBox<>();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private Button previousButton;
    private Button nextButton;
    private int currentPage = 1;
    private int pageSize = 20;

    public OrderView() {
        buildView();
        loadEmployees();
        loadData();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("order-root");
        root.setPadding(new Insets(18));
        root.setTop(createTop());
        root.setCenter(createTableCard());
        root.setBottom(createPagination());
    }

    private VBox createTop() {
        Label eyebrow = new Label("SALES / ORDERS");
        eyebrow.getStyleClass().add("order-eyebrow");
        Label title = new Label("Quản lý đơn hàng");
        title.getStyleClass().add("order-title");
        Label subtitle = new Label("Theo dõi, tra cứu, xuất dữ liệu và tạo hóa đơn bán hàng.");
        subtitle.getStyleClass().add("order-subtitle");
        VBox heading = new VBox(2, eyebrow, title, subtitle);

        Button addButton = button("+ Thêm hóa đơn", "success-button", this::addOrder);
        addButton.setVisible(permissionManager.hasPermission("ORDER", "ADD"));
        addButton.setManaged(addButton.isVisible());
        Button detailButton = button("Chi tiết", "primary-button", this::showSelectedDetail);
        Button excelButton = button("Export Excel", "secondary-button", this::exportExcel);
        Button pdfButton = button("Export PDF", "secondary-button", this::exportSelectedPdf);
        Button refreshButton = button("Làm mới", "secondary-button", this::loadData);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, addButton, detailButton, excelButton, pdfButton, spacer, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("order-field-label");
        searchField.setPromptText("Mã hóa đơn, khách hàng, thanh toán...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter();
        });

        Label employeeLabel = new Label("Nhân viên");
        employeeLabel.getStyleClass().add("order-field-label");
        employeeBox.setPrefWidth(250);
        employeeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter();
        });
        employeeBox.setCellFactory(list -> employeeCell());
        employeeBox.setButtonCell(employeeCell());

        resultLabel.getStyleClass().add("order-result-label");
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        HBox filters = new HBox(10, searchLabel, searchField, employeeLabel, employeeBox, filterSpacer, resultLabel);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("order-searchbar");

        VBox top = new VBox(14, heading, actions, filters);
        top.setPadding(new Insets(2, 2, 14, 2));
        return top;
    }

    private ListCell<EmployeeDTO> employeeCell() {
        return new ListCell<>() {
            @Override protected void updateItem(EmployeeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else if (item == allEmployeesOption) setText("Tất cả nhân viên");
                else setText(item.getEmployeeName() + " - ID: " + item.getEmployeeId());
            }
        };
    }

    private VBox createTableCard() {
        configureTable();
        Label hint = new Label("Double-click một hóa đơn để xem chi tiết.");
        hint.getStyleClass().add("order-hint");
        VBox card = new VBox(9, hint, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("order-card");
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có hóa đơn phù hợp."));
        table.getStyleClass().add("order-table");

        TableColumn<NewOrderDTO, Number> sttCol = new TableColumn<>("STT");
        sttCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(pageItems.indexOf(data.getValue()) + 1 + (currentPage - 1) * pageSize));
        sttCol.setPrefWidth(55);

        TableColumn<NewOrderDTO, String> idCol = new TableColumn<>("Mã hóa đơn");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getOrderId())));
        idCol.setPrefWidth(95);
        TableColumn<NewOrderDTO, String> customerCol = new TableColumn<>("Tên khách hàng");
        customerCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getCustomerName())));
        customerCol.setPrefWidth(170);
        TableColumn<NewOrderDTO, String> promoCol = new TableColumn<>("Khuyến mãi");
        promoCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getPromotionName())));
        promoCol.setPrefWidth(150);
        TableColumn<NewOrderDTO, String> totalCol = new TableColumn<>("Tổng tiền");
        totalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().getTotal()) + " VNĐ"));
        totalCol.setPrefWidth(130);
        TableColumn<NewOrderDTO, String> paymentCol = new TableColumn<>("Thanh toán");
        paymentCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getPaymentMethod())));
        paymentCol.setPrefWidth(120);
        TableColumn<NewOrderDTO, String> employeeCol = new TableColumn<>("Người lập");
        employeeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getEmployeeName())));
        employeeCol.setPrefWidth(145);
        TableColumn<NewOrderDTO, String> dateCol = new TableColumn<>("Ngày lập");
        dateCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getCreatedAt() == null ? "N/A" : data.getValue().getCreatedAt().format(DATE_FORMAT)));
        dateCol.setPrefWidth(165);

        table.getColumns().setAll(sttCol, idCol, customerCol, promoCol, totalCol, paymentCol, employeeCol, dateCol);
        table.setRowFactory(tv -> {
            TableRow<NewOrderDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showDetail(row.getItem().getOrderId());
            });
            return row;
        });
    }

    private HBox createPagination() {
        previousButton = button("‹ Trước", "secondary-button", () -> {
            if (currentPage > 1) { currentPage--; renderPage(); }
        });
        nextButton = button("Sau ›", "secondary-button", () -> {
            if (currentPage < totalPages()) { currentPage++; renderPage(); }
        });
        pageSizeBox.getItems().setAll(10, 20, 30, 50, 100);
        pageSizeBox.setValue(pageSize);
        pageSizeBox.setPrefWidth(85);
        pageSizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) { pageSize = newValue; currentPage = 1; renderPage(); }
        });
        pageLabel.getStyleClass().add("order-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 2, 0, 2));
        return footer;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().addAll("order-button", styleClass);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void loadEmployees() {
        List<EmployeeDTO> employees = employeeBUS.getAllEmployees();
        ObservableList<EmployeeDTO> items = FXCollections.observableArrayList();
        items.add(allEmployeesOption);
        if (employees != null) items.addAll(employees);
        employeeBox.setItems(items);
        employeeBox.getSelectionModel().select(0);
    }

    public void loadData() {
        try {
            allOrders.clear();
            List<NewOrderDTO> orders = orderBUS.getAllNew();
            if (orders != null) allOrders.addAll(orders);
            currentPage = Math.max(1, currentPage);
            applyFilter();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Không thể tải hóa đơn", safe(ex.getMessage()));
        }
    }

    private void applyFilter() {
        String keyword = safe(searchField.getText()).trim().toLowerCase(Locale.ROOT);
        EmployeeDTO emp = employeeBox.getValue();
        filteredOrders.clear();
        for (NewOrderDTO order : allOrders) {
            boolean matchSearch = keyword.isEmpty()
                    || String.valueOf(order.getOrderId()).contains(keyword)
                    || safe(order.getCustomerName()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(order.getPaymentMethod()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(order.getPromotionName()).toLowerCase(Locale.ROOT).contains(keyword);
            boolean matchEmployee = emp == null || emp == allEmployeesOption
                    || (order.getEmployee_id() != null && order.getEmployee_id().equals(emp.getEmployeeId()));
            if (matchSearch && matchEmployee) filteredOrders.add(order);
        }
        int pages = totalPages();
        if (currentPage > pages) currentPage = pages;
        if (currentPage < 1) currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int total = filteredOrders.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredOrders.subList(start, end));
        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages + "  •  " + total + " hóa đơn");
        resultLabel.setText(total + " kết quả");
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        table.refresh();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredOrders.size() / (double) pageSize));
    }

    private void addOrder() {
        if (!permissionManager.hasPermission("ORDER", "ADD")) {
            showAlert(Alert.AlertType.WARNING, "Không có quyền truy cập", "Bạn không có quyền thêm hóa đơn.");
            return;
        }
        try {
            OrderCreateDialogFx dialog = new OrderCreateDialogFx(ownerWindow());
            dialog.showAndWait();
            if (dialog.isSaved()) {
                loadData();
            } else {
                Long id = dialog.getOrderId();
                if (id != null) {
                    orderBUS.deleteOrder(id);
                    orderBUS.setAutoIncrement(id);
                }
                Long customerId = dialog.getTempCustomerId();
                if (customerId != null) customerBUS.delete(customerId);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Không thể tạo hóa đơn", safe(ex.getMessage()));
        }
    }

    private void showSelectedDetail() {
        NewOrderDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn", "Vui lòng chọn một hóa đơn.");
            return;
        }
        showDetail(selected.getOrderId());
    }

    private void showDetail(Long orderId) {
        if (orderId == null) return;
        new OrderDetailDialogFx(ownerWindow(), orderId).showAndWait();
    }

    private void exportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu file Excel");
        chooser.setInitialFileName("HOA_DON.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        File file = chooser.showSaveDialog(ownerWindow());
        if (file == null) return;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            XSSFSheet sheet = workbook.createSheet("Danh Sách Hóa Đơn");
            XSSFRow header = sheet.createRow(0);
            String[] headers = {"STT", "Mã Hóa Đơn", "Mã Nhân Viên", "Mã Khách Hàng", "Mã Khuyến Mãi", "Tổng Tiền", "Phương Thức Thanh Toán", "Ngày Lập"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int rowIndex = 1;
            for (NewOrderDTO o : filteredOrders) {
                XSSFRow row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(rowIndex++);
                row.createCell(1).setCellValue(o.getOrderId() == null ? 0 : o.getOrderId());
                row.createCell(2).setCellValue(o.getEmployee_id() == null ? 0 : o.getEmployee_id());
                row.createCell(3).setCellValue(o.getCustomer_id() == null ? 0 : o.getCustomer_id());
                if (o.getPromotionID() == null) row.createCell(4).setCellValue("Không có");
                else row.createCell(4).setCellValue(o.getPromotionID());
                row.createCell(5).setCellValue(o.getTotal() == null ? 0 : o.getTotal().doubleValue());
                row.createCell(6).setCellValue(safe(o.getPaymentMethod()));
                row.createCell(7).setCellValue(o.getCreatedAt() == null ? "" : o.getCreatedAt().format(DATE_FORMAT));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            showAlert(Alert.AlertType.INFORMATION, "Xuất Excel thành công", "Đã lưu: " + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Xuất Excel thất bại", safe(ex.getMessage()));
        }
    }

    private void exportSelectedPdf() {
        NewOrderDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn", "Vui lòng chọn hóa đơn cần xuất PDF.");
            return;
        }
        exportPdf(selected.getOrderId());
    }

    private void exportPdf(Long orderId) {
        ArrayList<OrderDetailDTO> details = orderBUS.getOrderDetailByOrderId(orderId);
        if (details == null || details.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Hóa đơn không có chi tiết để xuất.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu hóa đơn");
        chooser.setInitialFileName("HoaDon_" + orderId + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(ownerWindow());
        if (file == null) return;

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            BaseFont normalBase = loadFont("./resources/fonts/SVN-Arial 2.ttf");
            BaseFont boldBase = loadFont("./resources/fonts/SVN-Arial 2 bold.ttf");
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(boldBase, 20);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(normalBase, 11);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(boldBase, 11);

            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            OrderDetailDTO first = details.get(0);
            document.add(new Paragraph("Mã hóa đơn: " + first.getOrderId(), normalFont));
            document.add(new Paragraph("Khách hàng: " + safe(first.getCustomerName()), normalFont));
            document.add(new Paragraph("Nhân viên: " + safe(first.getEmployeeName()), normalFont));
            document.add(new Paragraph("Ngày lập: " + (first.getCreatedAt() == null ? "N/A" : first.getCreatedAt().format(DATE_FORMAT)), normalFont));
            document.add(new Paragraph("Thanh toán: " + safe(first.getPaymentMethod()), normalFont));
            document.add(new Paragraph("Khuyến mãi: " + (first.getPromotionName() == null ? "Không áp dụng" : first.getPromotionName()), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(6);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{1, 4, 2.5f, 2.2f, 2.2f, 3.2f});
            addPdfCell(pdfTable, "STT", headerFont, true);
            addPdfCell(pdfTable, "Sản phẩm", headerFont, true);
            addPdfCell(pdfTable, "Serial", headerFont, true);
            addPdfCell(pdfTable, "Giá gốc", headerFont, true);
            addPdfCell(pdfTable, "Thành tiền", headerFont, true);
            addPdfCell(pdfTable, "Hạn bảo hành", headerFont, true);
            int stt = 1;
            for (OrderDetailDTO item : details) {
                addPdfCell(pdfTable, String.valueOf(stt++), normalFont, false);
                addPdfCell(pdfTable, safe(item.getProductName()), normalFont, false);
                addPdfCell(pdfTable, safe(item.getSerial()), normalFont, false);
                addPdfCell(pdfTable, formatMoney(item.getOriginalPrice()), normalFont, false);
                addPdfCell(pdfTable, formatMoney(item.getUnitPrice()), normalFont, false);
                LocalDateTime expiry = warrantyExpiry(item.getCreatedAt(), item.getWarrantyTime());
                addPdfCell(pdfTable, expiry == null ? "Không bảo hành" : expiry.format(DATE_FORMAT), normalFont, false);
            }
            document.add(pdfTable);
            document.add(new Paragraph(" "));
            Paragraph total = new Paragraph("Tổng tiền: " + formatMoney(first.getTotal()) + " VNĐ", headerFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);
            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Xuất PDF thành công", "Đã lưu: " + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Xuất PDF thất bại", safe(ex.getMessage()));
        }
    }

    private BaseFont loadFont(String preferred) throws Exception {
        File f = new File(preferred);
        if (f.exists()) return BaseFont.createFont(f.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        File windowsArial = new File("C:/Windows/Fonts/arial.ttf");
        if (windowsArial.exists()) return BaseFont.createFont(windowsArial.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    private void addPdfCell(PdfPTable table, String text, com.itextpdf.text.Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        if (header) cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static EmployeeDTO createAllEmployeesOption() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmployeeId(-1L);
        employee.setEmployeeName("Tất cả nhân viên");
        return employee;
    }

    private Window ownerWindow() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private LocalDateTime warrantyExpiry(LocalDateTime createdAt, Integer warrantyMonths) {
        return createdAt == null || warrantyMonths == null ? null : createdAt.plusMonths(warrantyMonths);
    }

    private String safe(String value) { return value == null ? "" : value; }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? title : message);
        alert.showAndWait();
    }
}
