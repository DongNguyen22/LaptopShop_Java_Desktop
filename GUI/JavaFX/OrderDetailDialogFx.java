package GUI.JavaFX;

import Bus.OrderBUS;
import DTO.OrderDetailDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

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

/** JavaFX replacement for GUI.Dialog.thongTinHoaDonDialog. */
public final class OrderDetailDialogFx {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private final Stage stage = new Stage();
    private final Long orderId;
    private final OrderBUS orderBUS = new OrderBUS();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private final ObservableList<OrderDetailDTO> items = FXCollections.observableArrayList();
    private final TableView<OrderDetailDTO> table = new TableView<>();
    private OrderDetailDTO first;

    public OrderDetailDialogFx(Window owner, Long orderId) {
        this.orderId = orderId;
        stage.setTitle("Chi tiết hóa đơn #" + orderId);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setWidth(980);
        stage.setHeight(650);
        stage.setMinWidth(820);
        stage.setMinHeight(520);
        loadData();
        if (first != null) buildView();
    }

    public void showAndWait() {
        if (first != null) stage.showAndWait();
    }

    private void loadData() {
        ArrayList<OrderDetailDTO> list = orderBUS.getOrderDetailByOrderId(orderId);
        if (list == null || list.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Không tìm thấy chi tiết hóa đơn #" + orderId);
            return;
        }
        items.setAll(list);
        first = list.get(0);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("order-dialog-root");
        root.setPadding(new Insets(18));
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        var css = getClass().getResource("/GUI/JavaFX/order.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }

    private VBox createHeader() {
        Label eyebrow = new Label("ORDER DETAIL");
        eyebrow.getStyleClass().add("order-eyebrow");
        Label title = new Label("Hóa đơn #" + orderId);
        title.getStyleClass().add("order-dialog-title");

        GridPane grid = new GridPane();
        grid.setHgap(26);
        grid.setVgap(7);
        addInfo(grid, 0, 0, "Mã hóa đơn", String.valueOf(first.getOrderId()));
        addInfo(grid, 2, 0, "Nhân viên", safe(first.getEmployeeName()));
        addInfo(grid, 0, 2, "Mã khách hàng", String.valueOf(first.getCustomerCode()));
        addInfo(grid, 2, 2, "Khách hàng", safe(first.getCustomerName()));
        addInfo(grid, 0, 4, "Ngày lập", first.getCreatedAt() == null ? "N/A" : first.getCreatedAt().format(DATE_FORMAT));
        addInfo(grid, 2, 4, "Khuyến mãi", first.getPromotionName() == null ? "Không áp dụng" : first.getPromotionName());
        addInfo(grid, 0, 6, "Thanh toán", safe(first.getPaymentMethod()));
        addInfo(grid, 2, 6, "Tổng tiền", formatMoney(first.getTotal()) + " VNĐ");

        VBox card = new VBox(10, eyebrow, title, grid);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(14));
        BorderPane.setMargin(card, new Insets(0, 0, 12, 0));
        return card;
    }

    private void addInfo(GridPane grid, int col, int row, String label, String value) {
        Label key = new Label(label);
        key.getStyleClass().add("order-info-key");
        Label val = new Label(value == null || value.isBlank() ? "N/A" : value);
        val.getStyleClass().add("order-info-value");
        grid.add(key, col, row);
        grid.add(val, col, row + 1);
    }

    private VBox createTableCard() {
        configureTable();
        Label title = new Label("Sản phẩm trong hóa đơn");
        title.getStyleClass().add("dialog-section-title");
        VBox card = new VBox(8, title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(12));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Hóa đơn chưa có sản phẩm."));

        TableColumn<OrderDetailDTO, Number> sttCol = new TableColumn<>("STT");
        sttCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(items.indexOf(data.getValue()) + 1));
        sttCol.setPrefWidth(55);
        TableColumn<OrderDetailDTO, String> idCol = new TableColumn<>("Mã SP");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getProductCode())));
        idCol.setPrefWidth(80);
        TableColumn<OrderDetailDTO, String> productCol = new TableColumn<>("Tên sản phẩm");
        productCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getProductName())));
        productCol.setPrefWidth(220);
        TableColumn<OrderDetailDTO, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getSerial())));
        serialCol.setPrefWidth(145);
        TableColumn<OrderDetailDTO, String> warrantyCol = new TableColumn<>("Hạn bảo hành");
        warrantyCol.setCellValueFactory(data -> {
            LocalDateTime expiry = warrantyExpiry(data.getValue().getCreatedAt(), data.getValue().getWarrantyTime());
            return new ReadOnlyStringWrapper(expiry == null ? "Không bảo hành" : expiry.format(DATE_FORMAT));
        });
        warrantyCol.setPrefWidth(160);
        TableColumn<OrderDetailDTO, String> originalCol = new TableColumn<>("Giá gốc");
        originalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().getOriginalPrice())));
        originalCol.setPrefWidth(120);
        TableColumn<OrderDetailDTO, String> priceCol = new TableColumn<>("Thành tiền");
        priceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().getUnitPrice())));
        priceCol.setPrefWidth(120);
        table.getColumns().setAll(sttCol, idCol, productCol, serialCol, warrantyCol, originalCol, priceCol);
    }

    private HBox createFooter() {
        Button export = button("Xuất PDF", "secondary-button", this::exportPdf);
        Button close = button("Đóng", "primary-button", stage::close);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, export, spacer, close);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(14, 0, 0, 0));
        return footer;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().addAll("order-button", styleClass);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void exportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu hóa đơn");
        chooser.setInitialFileName("HoaDon_" + orderId + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(stage);
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
            addCell(pdfTable, "STT", headerFont, true);
            addCell(pdfTable, "Sản phẩm", headerFont, true);
            addCell(pdfTable, "Serial", headerFont, true);
            addCell(pdfTable, "Giá gốc", headerFont, true);
            addCell(pdfTable, "Thành tiền", headerFont, true);
            addCell(pdfTable, "Hạn bảo hành", headerFont, true);
            int stt = 1;
            for (OrderDetailDTO item : items) {
                addCell(pdfTable, String.valueOf(stt++), normalFont, false);
                addCell(pdfTable, safe(item.getProductName()), normalFont, false);
                addCell(pdfTable, safe(item.getSerial()), normalFont, false);
                addCell(pdfTable, formatMoney(item.getOriginalPrice()), normalFont, false);
                addCell(pdfTable, formatMoney(item.getUnitPrice()), normalFont, false);
                LocalDateTime expiry = warrantyExpiry(item.getCreatedAt(), item.getWarrantyTime());
                addCell(pdfTable, expiry == null ? "Không bảo hành" : expiry.format(DATE_FORMAT), normalFont, false);
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

    private void addCell(PdfPTable table, String text, com.itextpdf.text.Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        if (header) cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private LocalDateTime warrantyExpiry(LocalDateTime createdAt, Integer warrantyMonths) {
        return createdAt == null || warrantyMonths == null ? null : createdAt.plusMonths(warrantyMonths);
    }

    private String formatMoney(BigDecimal value) { return moneyFormat.format(value == null ? BigDecimal.ZERO : value); }
    private String safe(String value) { return value == null ? "" : value; }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? title : message);
        alert.showAndWait();
    }
}
