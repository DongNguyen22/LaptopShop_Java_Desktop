package GUI.JavaFX;

import DAO.ChiTietPhieuNhapDAO;
import DAO.EmployeeDAO;
import DAO.PhieuNhapDAO;
import DAO.SerialDAO;
import DTO.UserSession;
import DTO.chiTietPhieuNhap;
import DTO.phieuNhap;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.ChiTietPhieuNhapDialog. */
public final class ReceiptDetailDialogFx {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final Stage stage = new Stage();
    private final long receiptId;
    private final Runnable onChanged;
    private final PhieuNhapDAO receiptDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO detailDAO = new ChiTietPhieuNhapDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final SerialDAO serialDAO = new SerialDAO();
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final TableView<chiTietPhieuNhap> table = new TableView<>();
    private final ObservableList<chiTietPhieuNhap> detailItems = FXCollections.observableArrayList();
    private phieuNhap receipt;
    private VBox rootContent;

    public ReceiptDetailDialogFx(Window owner, long receiptId, Runnable onChanged) {
        this.receiptId = receiptId;
        this.onChanged = onChanged;
        configureStage(owner);
        loadReceipt();
        if (receipt != null) buildView();
    }

    public void showAndWait() {
        if (receipt != null) stage.showAndWait();
    }

    private void configureStage(Window owner) {
        stage.setTitle("Chi tiết phiếu nhập #" + receiptId);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setWidth(980);
        stage.setHeight(650);
        stage.setMinWidth(820);
        stage.setMinHeight(520);
    }

    private void loadReceipt() {
        receipt = receiptDAO.getById(receiptId);
        if (receipt == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy phiếu nhập #" + receiptId);
            alert.showAndWait();
            return;
        }
        List<chiTietPhieuNhap> details = detailDAO.getAll(receipt);
        detailItems.setAll(details == null ? List.of() : details);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("receipt-dialog-root");
        root.setPadding(new Insets(18));
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        var css = getClass().getResource("/GUI/JavaFX/receipt.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }

    private VBox createHeader() {
        Label eyebrow = new Label("RECEIPT DETAIL");
        eyebrow.getStyleClass().add("receipt-eyebrow");
        Label title = new Label("Phiếu nhập #" + receiptId);
        title.getStyleClass().add("receipt-dialog-title");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);
        addInfo(grid, 0, 0, "Mã phiếu", String.valueOf(receipt.getId()));
        addInfo(grid, 2, 0, "Nhà cung cấp", safe(receipt.getNhaCungCap()));
        addInfo(grid, 0, 2, "Ngày tạo", receipt.getNgayTao() == null ? "N/A" : receipt.getNgayTao().format(DATE_FORMAT));
        addInfo(grid, 2, 2, "Người tạo", getEmployeeName(receipt.getNguoiTao()));
        addInfo(grid, 0, 4, "Tổng tiền", formatMoney(receipt.getTongTien()) + " VND");
        addInfo(grid, 2, 4, "Trạng thái", receipt.getTrangThai());

        VBox card = new VBox(10, eyebrow, title, grid);
        card.getStyleClass().add("receipt-card");
        card.setPadding(new Insets(14));
        BorderPane.setMargin(card, new Insets(0, 0, 12, 0));
        return card;
    }

    private void addInfo(GridPane grid, int col, int row, String label, String value) {
        Label key = new Label(label);
        key.getStyleClass().add("receipt-info-key");
        Label val = new Label(value == null || value.isBlank() ? "N/A" : value);
        val.getStyleClass().add("receipt-info-value");
        grid.add(key, col, row);
        grid.add(val, col, row + 1);
    }

    private VBox createTableCard() {
        configureTable();
        Label title = new Label("Danh sách sản phẩm");
        title.getStyleClass().add("dialog-section-title");
        Label hint = new Label("Double-click một sản phẩm hoặc bấm Xem serial để xem tình trạng từng serial.");
        hint.getStyleClass().add("receipt-hint");
        VBox card = new VBox(8, title, hint, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("receipt-card");
        card.setPadding(new Insets(12));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(detailItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Phiếu nhập chưa có sản phẩm."));

        TableColumn<chiTietPhieuNhap, String> idCol = new TableColumn<>("ID SP");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSanPhamId())));
        idCol.setPrefWidth(80);
        TableColumn<chiTietPhieuNhap, String> nameCol = new TableColumn<>("Sản phẩm");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getTenSP())));
        nameCol.setPrefWidth(230);
        TableColumn<chiTietPhieuNhap, chiTietPhieuNhap> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        serialCol.setPrefWidth(110);
        serialCol.setCellFactory(column -> new TableCell<>() {
            private final Button view = button("Xem serial", "secondary-button", () -> {
                chiTietPhieuNhap detail = getItem();
                if (detail != null) showSerialDetail(detail);
            });
            @Override protected void updateItem(chiTietPhieuNhap item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : view);
            }
        });
        TableColumn<chiTietPhieuNhap, String> qtyCol = new TableColumn<>("Số lượng");
        qtyCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuong())));
        qtyCol.setPrefWidth(80);
        TableColumn<chiTietPhieuNhap, String> priceCol = new TableColumn<>("Đơn giá");
        priceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().getGiaNhap())));
        priceCol.setPrefWidth(125);
        TableColumn<chiTietPhieuNhap, String> warrantyCol = new TableColumn<>("BH (tháng)");
        warrantyCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getThoiGianBaoHanh())));
        warrantyCol.setPrefWidth(95);
        TableColumn<chiTietPhieuNhap, String> totalCol = new TableColumn<>("Thành tiền");
        totalCol.setCellValueFactory(data -> {
            BigDecimal price = data.getValue().getGiaNhap() == null ? BigDecimal.ZERO : data.getValue().getGiaNhap();
            return new ReadOnlyStringWrapper(formatMoney(price.multiply(BigDecimal.valueOf(data.getValue().getSoLuong()))));
        });
        totalCol.setPrefWidth(140);

        table.getColumns().setAll(idCol, nameCol, serialCol, qtyCol, priceCol, warrantyCol, totalCol);
        table.setRowFactory(tv -> {
            TableRow<chiTietPhieuNhap> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showSerialDetail(row.getItem());
            });
            return row;
        });
    }

    private HBox createFooter() {
        Button exportPdf = button("Xuất PDF", "secondary-button", this::exportToPdf);
        Button edit = button("Chỉnh sửa phiếu", "primary-button", this::editReceipt);
        Button complete = button("Hoàn thành phiếu", "success-button", this::changeStatus);
        Button close = button("Đóng", "secondary-button", stage::close);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        if (receipt.getStatus() == 0) footer.getChildren().addAll(edit, complete);
        footer.getChildren().addAll(spacer, exportPdf, close);
        footer.setPadding(new Insets(14, 0, 0, 0));
        return footer;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("receipt-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showSerialDetail(chiTietPhieuNhap detail) {
        new SerialDetailDialogFx(stage, detail.getTenSP(), receiptId, detail.getSanPhamId()).showAndWait();
    }

    private void editReceipt() {
        ReceiptEditDialogFx dialog = new ReceiptEditDialogFx(stage, receiptId, () -> {
            if (onChanged != null) onChanged.run();
            stage.close();
        });
        dialog.showAndWait();
    }

    private void changeStatus() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Sửa trạng thái");
        confirm.setHeaderText("Xác nhận hoàn thành phiếu nhập #" + receiptId + "?");
        confirm.setContentText("Sau khi hoàn thành sẽ cập nhật stock và ghi nợ tiền nhà cung cấp.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            long employeeId = UserSession.getInstance().getAccount().getEmployeeId();
            int updated = receiptDAO.updateStatus(receiptId, employeeId, 1);
            if (updated > 0) {
                info("Đã hoàn thành phiếu nhập!");
                if (onChanged != null) onChanged.run();
                stage.close();
            } else {
                error("Không tìm thấy phiếu nhập hoặc không cập nhật được!");
            }
        } catch (Exception ex) {
            error("Lỗi khi cập nhật trạng thái: " + safe(ex.getMessage()));
        }
    }

    private void exportToPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Xuất PDF phiếu nhập");
        chooser.setInitialFileName("PhieuNhap_" + receiptId + ".pdf");
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
            com.itextpdf.kernel.font.PdfFont fontBold = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                    fontBoldPath, com.itextpdf.io.font.PdfEncodings.IDENTITY_H, pdf);

            document.add(new com.itextpdf.layout.element.Paragraph("PHIẾU NHẬP")
                    .setFont(fontBold).setFontSize(20)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new com.itextpdf.layout.element.Paragraph("Mã phiếu: " + receipt.getId()).setFont(font));
            document.add(new com.itextpdf.layout.element.Paragraph("Nhà cung cấp: " + safe(receipt.getNhaCungCap())).setFont(font));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            float[] widths = {1, 2, 3, 5, 2, 2, 3};
            com.itextpdf.layout.element.Table pdfTable = new com.itextpdf.layout.element.Table(widths);
            pdfTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            addPdfCell(pdfTable, font, "STT", true);
            addPdfCell(pdfTable, font, "Mã SP", true);
            addPdfCell(pdfTable, font, "Tên SP", true);
            addPdfCell(pdfTable, font, "Serial", true);
            addPdfCell(pdfTable, font, "Số lượng", true);
            addPdfCell(pdfTable, font, "Đơn giá", true);
            addPdfCell(pdfTable, font, "Thành tiền", true);

            int index = 1;
            for (chiTietPhieuNhap detail : detailItems) {
                Long detailId = detailDAO.getIDCTPhieuNhap(receiptId, detail.getSanPhamId());
                List<DTO.SerialSanPhamDTO> serials = serialDAO.getSerial(detailId);
                StringBuilder serialText = new StringBuilder();
                if (serials != null) {
                    for (DTO.SerialSanPhamDTO serial : serials) serialText.append(serial.getSerial()).append("\n");
                }
                BigDecimal price = detail.getGiaNhap() == null ? BigDecimal.ZERO : detail.getGiaNhap();
                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(detail.getSoLuong()));
                addPdfCell(pdfTable, font, String.valueOf(index++), false);
                addPdfCell(pdfTable, font, String.valueOf(detail.getSanPhamId()), false);
                addPdfCell(pdfTable, font, safe(detail.getTenSP()), false);
                addPdfCell(pdfTable, font, serialText.toString(), false);
                addPdfCell(pdfTable, font, String.valueOf(detail.getSoLuong()), false);
                addPdfCell(pdfTable, font, formatMoney(price), false);
                addPdfCell(pdfTable, font, formatMoney(lineTotal), false);
            }
            document.add(pdfTable);
            document.add(new com.itextpdf.layout.element.Paragraph("Tổng tiền: " + formatMoney(receipt.getTongTien()) + " VND").setFont(fontBold));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            document.add(new com.itextpdf.layout.element.Paragraph("\nNgày in: " + sdf.format(new java.util.Date()))
                    .setFont(font).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            document.add(new com.itextpdf.layout.element.Paragraph("Người tạo phiếu: " + getEmployeeName(receipt.getNguoiTao()))
                    .setFont(font).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            document.close();

            info("Xuất PDF thành công!\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            error("Lỗi xuất PDF: " + safe(ex.getMessage()));
        }
    }

    private void addPdfCell(com.itextpdf.layout.element.Table table,
                            com.itextpdf.kernel.font.PdfFont font,
                            String text,
                            boolean header) {
        com.itextpdf.layout.element.Paragraph paragraph = new com.itextpdf.layout.element.Paragraph(text == null ? "" : text).setFont(font);
        if (header) paragraph.setBold();
        table.addCell(new com.itextpdf.layout.element.Cell().add(paragraph));
    }

    private String getEmployeeName(long employeeId) {
        try {
            String name = employeeDAO.getEmployeeNameById((int) employeeId);
            return name == null || name.isBlank() ? "N/A" : name;
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private void error(String message) { alert(Alert.AlertType.ERROR, "Lỗi", message); }
    private void info(String message) { alert(Alert.AlertType.INFORMATION, "Thông báo", message); }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
