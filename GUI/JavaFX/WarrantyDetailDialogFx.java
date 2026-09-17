package GUI.JavaFX;

import DAO.warrantyCardDAO;
import DTO.warrantyCardDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
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
import valiateData.PermissionManager;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** JavaFX replacement for GUI.Dialog.ChiTietPhieuBaoHanhDialog. */
public final class WarrantyDetailDialogFx {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Stage stage = new Stage();
    private final warrantyCardDAO dao = new warrantyCardDAO();
    private final PermissionManager permissionManager = new PermissionManager();
    private final long warrantyId;
    private final Runnable onChanged;
    private warrantyCardDTO dto;

    public WarrantyDetailDialogFx(Window owner, long warrantyId, Runnable onChanged) {
        this.warrantyId = warrantyId;
        this.onChanged = onChanged;
        if (owner != null) stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Chi tiết phiếu bảo hành");
        stage.setMinWidth(760);
        stage.setMinHeight(600);
        loadData();
    }

    public void showAndWait() {
        if (dto != null) stage.showAndWait();
    }

    private void loadData() {
        try {
            dto = dao.getById(warrantyId);
            if (dto == null) {
                error("Không tìm thấy phiếu bảo hành #" + warrantyId + ".");
                return;
            }
            if (dto.getWarrantyMonths() == 0) {
                dto.setWarrantyMonths(dao.getWarrantyMonthsFromReceipt(dto.getOrderId()));
            }
            Scene scene = new Scene(buildView(), 860, 700);
            URL css = WarrantyDetailDialogFx.class.getResource("/GUI/JavaFX/warranty.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
            dto = null;
            error("Không thể tải chi tiết phiếu bảo hành: " + safe(ex.getMessage()));
        }
    }

    private BorderPane buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("warranty-dialog-root");
        root.setPadding(new Insets(18));

        Label title = new Label("Phiếu bảo hành #" + dto.getWarrantyId());
        title.getStyleClass().add("warranty-dialog-title");
        Label status = new Label(dto.getStatusName());
        status.getStyleClass().addAll("warranty-status-badge", statusClass(dto.getStatusCode()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, title, spacer, status);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("warranty-detail-header");
        header.setPadding(new Insets(14));
        root.setTop(header);

        VBox cards = new VBox(12,
                infoCard("Thông tin sản phẩm", new String[][] {
                        {"Mã sản phẩm", String.valueOf(dto.getProductId())},
                        {"Tên sản phẩm", safe(dto.getProductName())},
                        {"Serial", safe(dto.getProductSerial())},
                        {"Hạn bảo hành", dto.getWarrantyMonths() > 0 ? dto.getWarrantyMonths() + " tháng" : "Không xác định"}
                }),
                infoCard("Thông tin khách hàng", new String[][] {
                        {"Tên khách hàng", safe(dto.getCustomerName())},
                        {"SĐT", safe(dto.getCustomerPhone())},
                        {"Mã đơn hàng", String.valueOf(dto.getOrderId())}
                }),
                warrantyCard(),
                infoCard("Thông tin quản lý", auditRows()));

        ScrollPane scroll = new ScrollPane(cards);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("warranty-scroll");
        root.setCenter(scroll);
        BorderPane.setMargin(scroll, new Insets(12, 0, 12, 0));
        root.setBottom(createButtons());
        return root;
    }

    private VBox warrantyCard() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(9);
        addInfoRow(grid, 0, "Ngày tiếp nhận", formatDateTime(dto.getReceiveTime()));
        addInfoRow(grid, 1, "Ngày dự kiến trả", dto.getReturnDate() == null ? "N/A" : DATE.format(dto.getReturnDate()));
        addInfoRow(grid, 2, "Trạng thái", dto.getStatusName());

        TextArea description = new TextArea(safe(dto.getDetails()));
        description.setEditable(false);
        description.setWrapText(true);
        description.setPrefRowCount(4);
        grid.add(labelKey("Mô tả"), 0, 3);
        grid.add(description, 1, 3);
        GridPane.setHgrow(description, Priority.ALWAYS);

        VBox card = new VBox(10, sectionTitle("Thông tin bảo hành"), grid);
        card.getStyleClass().add("warranty-card");
        card.setPadding(new Insets(14));
        return card;
    }

    private String[][] auditRows() {
        if (dto.getUpdatedAt() != null && !dto.getUpdatedAt().equals(dto.getCreatedAt())) {
            return new String[][] {
                    {"Người tạo", safe(dto.getCreatedUserName())},
                    {"Thời gian tạo", formatDateTime(dto.getCreatedAt())},
                    {"Thời gian sửa", formatDateTime(dto.getUpdatedAt())}
            };
        }
        return new String[][] {
                {"Người tạo", safe(dto.getCreatedUserName())},
                {"Thời gian tạo", formatDateTime(dto.getCreatedAt())}
        };
    }

    private VBox infoCard(String title, String[][] rows) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(9);
        for (int i = 0; i < rows.length; i++) addInfoRow(grid, i, rows[i][0], rows[i][1]);
        VBox card = new VBox(10, sectionTitle(title), grid);
        card.getStyleClass().add("warranty-card");
        card.setPadding(new Insets(14));
        return card;
    }

    private void addInfoRow(GridPane grid, int row, String key, String value) {
        grid.add(labelKey(key), 0, row);
        Label valueLabel = new Label(value == null || value.isBlank() ? "N/A" : value);
        valueLabel.setWrapText(true);
        valueLabel.getStyleClass().add("warranty-info-value");
        grid.add(valueLabel, 1, row);
        GridPane.setHgrow(valueLabel, Priority.ALWAYS);
    }

    private Label labelKey(String text) {
        Label label = new Label(text + ":");
        label.getStyleClass().add("warranty-info-key");
        return label;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("warranty-section-title");
        return label;
    }

    private HBox createButtons() {
        Button export = button("Xuất PDF", "success-button", this::exportPdf);
        Button edit = button("Chỉnh sửa", "primary-button", this::editWarranty);
        boolean canEdit = permissionManager.hasPermission("WARRANTY", "UPDATE");
        edit.setVisible(canEdit);
        edit.setManaged(canEdit);
        Button close = button("Đóng", "secondary-button", stage::close);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(8, export, spacer, edit, close);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    private void editWarranty() {
        if (!permissionManager.hasPermission("WARRANTY", "UPDATE")) {
            warning("Bạn không có quyền chỉnh sửa phiếu bảo hành.");
            return;
        }
        stage.close();
        Window owner = stage.getOwner();
        new WarrantyEditDialogFx(owner, dto, onChanged).showAndWait();
    }

    private void exportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Xuất PDF phiếu bảo hành");
        chooser.setInitialFileName("PhieuBaoHanh_" + dto.getWarrantyId() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }

        try {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file.getAbsolutePath());
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

            String fontPath = "./resources/fonts/SVN-Arial 2.ttf";
            String fontBoldPath = "./resources/fonts/SVN-Arial 2 bold.ttf";
            com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                    fontPath, com.itextpdf.io.font.PdfEncodings.IDENTITY_H, pdf);
            com.itextpdf.kernel.font.PdfFont bold = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                    fontBoldPath, com.itextpdf.io.font.PdfEncodings.IDENTITY_H, pdf);

            document.add(new com.itextpdf.layout.element.Paragraph("PHIẾU BẢO HÀNH")
                    .setFont(bold).setFontSize(20)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new com.itextpdf.layout.element.Paragraph("Mã phiếu: " + dto.getWarrantyId()).setFont(bold));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            com.itextpdf.layout.element.Table customerTable = pdfTable();
            addPdfRow(customerTable, font, bold, "Tên khách hàng", safe(dto.getCustomerName()));
            addPdfRow(customerTable, font, bold, "Số điện thoại", safe(dto.getCustomerPhone()));
            addPdfRow(customerTable, font, bold, "Mã đơn hàng", String.valueOf(dto.getOrderId()));
            document.add(new com.itextpdf.layout.element.Paragraph("THÔNG TIN KHÁCH HÀNG").setFont(bold).setFontSize(14));
            document.add(customerTable);

            com.itextpdf.layout.element.Table productTable = pdfTable();
            addPdfRow(productTable, font, bold, "Mã sản phẩm", String.valueOf(dto.getProductId()));
            addPdfRow(productTable, font, bold, "Tên sản phẩm", safe(dto.getProductName()));
            addPdfRow(productTable, font, bold, "Serial", safe(dto.getProductSerial()));
            addPdfRow(productTable, font, bold, "Hạn bảo hành", dto.getWarrantyMonths() + " tháng");
            document.add(new com.itextpdf.layout.element.Paragraph("THÔNG TIN SẢN PHẨM").setFont(bold).setFontSize(14));
            document.add(productTable);

            com.itextpdf.layout.element.Table warrantyTable = pdfTable();
            addPdfRow(warrantyTable, font, bold, "Ngày tiếp nhận", formatDateTime(receiveOrCreated()));
            addPdfRow(warrantyTable, font, bold, "Ngày dự kiến trả",
                    dto.getReturnDate() == null ? "N/A" : DATE.format(dto.getReturnDate()));
            addPdfRow(warrantyTable, font, bold, "Trạng thái", dto.getStatusName());
            addPdfRow(warrantyTable, font, bold, "Mô tả",
                    safe(dto.getDetails()).isBlank() ? "Không có mô tả" : dto.getDetails());
            document.add(new com.itextpdf.layout.element.Paragraph("THÔNG TIN BẢO HÀNH").setFont(bold).setFontSize(14));
            document.add(warrantyTable);

            document.add(new com.itextpdf.layout.element.Paragraph("\nNgày in: " + DATE_TIME.format(LocalDateTime.now()))
                    .setFont(font).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            document.add(new com.itextpdf.layout.element.Paragraph("Người tạo phiếu: " + safe(dto.getCreatedUserName()))
                    .setFont(font).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            document.close();
            info("Đã xuất PDF thành công!\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Lỗi khi xuất PDF: " + safe(ex.getMessage()));
        }
    }

    private com.itextpdf.layout.element.Table pdfTable() {
        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(new float[] {3, 7});
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
        return table;
    }

    private void addPdfRow(com.itextpdf.layout.element.Table table,
                           com.itextpdf.kernel.font.PdfFont font,
                           com.itextpdf.kernel.font.PdfFont bold,
                           String label,
                           String value) {
        table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(label).setFont(bold)));
        table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(value == null ? "N/A" : value).setFont(font)));
    }

    private LocalDateTime receiveOrCreated() {
        return dto.getReceiveTime() != null ? dto.getReceiveTime() : dto.getCreatedAt();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "N/A" : DATE_TIME.format(value);
    }

    private String statusClass(int code) {
        switch (code) {
            case 1: return "status-received";
            case 2: return "status-rejected";
            case 3: return "status-processing";
            case 4: return "status-completed";
            default: return "status-received";
        }
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

    private void warning(String message) {
        alert(Alert.AlertType.WARNING, "Cảnh báo", message);
    }

    private void error(String message) {
        alert(Alert.AlertType.ERROR, "Lỗi", message);
    }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        if (stage.isShowing()) alert.initOwner(stage);
        else if (stage.getOwner() != null) alert.initOwner(stage.getOwner());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
