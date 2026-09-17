package GUI.JavaFX;

import DAO.ChiTietPhieuNhapDAO;
import DAO.SerialDAO;
import DTO.SerialSanPhamDTO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
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

import java.util.ArrayList;
import java.util.List;

/** JavaFX replacement for GUI.Dialog.ChiTietSerialDialog. */
public final class SerialDetailDialogFx {

    private final Stage stage = new Stage();
    private final long receiptId;
    private final long productId;
    private final String productName;
    private final SerialDAO serialDAO = new SerialDAO();
    private final ChiTietPhieuNhapDAO detailDAO = new ChiTietPhieuNhapDAO();

    private final TableView<SerialSanPhamDTO> table = new TableView<>();
    private final ObservableList<SerialSanPhamDTO> visibleItems = FXCollections.observableArrayList();
    private final List<SerialSanPhamDTO> allSerials = new ArrayList<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final Label countLabel = new Label();

    public SerialDetailDialogFx(Window owner, String productName, long receiptId, long productId) {
        this.productName = productName == null ? "" : productName;
        this.receiptId = receiptId;
        this.productId = productId;
        configureStage(owner);
        buildView();
        loadData();
    }

    public void showAndWait() { stage.showAndWait(); }

    private void configureStage(Window owner) {
        stage.setTitle("Chi tiết Serial - " + productName);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setWidth(760);
        stage.setHeight(610);
        stage.setMinWidth(650);
        stage.setMinHeight(480);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("receipt-dialog-root");
        root.setPadding(new Insets(18));

        Label title = new Label("Serial – " + productName);
        title.getStyleClass().add("receipt-dialog-title");
        searchField.setPromptText("Tìm serial / mã đơn hàng...");
        searchField.setPrefWidth(250);
        statusBox.getItems().setAll("Tất cả", "Còn hàng", "Đã bán");
        statusBox.setValue("Tất cả");
        Button filterButton = button("Lọc", "primary-button", this::applyFilter);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox filters = new HBox(8, new Label("Tìm:"), searchField, new Label("Trạng thái:"), statusBox, filterButton, spacer, countLabel);
        filters.setAlignment(Pos.CENTER_LEFT);
        VBox top = new VBox(10, title, filters);
        top.setPadding(new Insets(0, 0, 12, 0));
        root.setTop(top);

        configureTable();
        VBox card = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("receipt-card");
        card.setPadding(new Insets(10));
        root.setCenter(card);

        Button close = button("Đóng", "secondary-button", stage::close);
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox footer = new HBox(8, footerSpacer, close);
        footer.setPadding(new Insets(12, 0, 0, 0));
        root.setBottom(footer);

        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter());
        statusBox.valueProperty().addListener((obs, oldV, newV) -> applyFilter());

        Scene scene = new Scene(root);
        var css = getClass().getResource("/GUI/JavaFX/receipt.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(visibleItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có serial phù hợp."));

        TableColumn<SerialSanPhamDTO, String> sttCol = new TableColumn<>("STT");
        sttCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(visibleItems.indexOf(data.getValue()) + 1)));
        sttCol.setPrefWidth(65);
        TableColumn<SerialSanPhamDTO, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getSerial())));
        serialCol.setPrefWidth(300);
        TableColumn<SerialSanPhamDTO, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getOrderId() == null ? "Còn hàng" : "Đã bán"));
        statusCol.setPrefWidth(120);
        TableColumn<SerialSanPhamDTO, String> orderCol = new TableColumn<>("Mã đơn hàng");
        orderCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getOrderId() == null ? "Chưa bán" : data.getValue().getOrderId()));
        orderCol.setPrefWidth(170);
        table.getColumns().setAll(sttCol, serialCol, statusCol, orderCol);
    }

    private void loadData() {
        allSerials.clear();
        Long detailId = detailDAO.getIDCTPhieuNhap(receiptId, productId);
        List<SerialSanPhamDTO> data = serialDAO.getSerial(detailId);
        if (data != null) {
            for (SerialSanPhamDTO serial : data) {
                serial.setOrderId(serialDAO.getOrderIdBySerial(serial.getSerial()));
                allSerials.add(serial);
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        String keyword = safe(searchField.getText()).trim().toLowerCase();
        String status = statusBox.getValue() == null ? "Tất cả" : statusBox.getValue();
        visibleItems.clear();
        for (SerialSanPhamDTO serial : allSerials) {
            String orderId = safe(serial.getOrderId());
            String currentStatus = orderId.isEmpty() ? "Còn hàng" : "Đã bán";
            boolean keywordMatch = keyword.isEmpty()
                    || safe(serial.getSerial()).toLowerCase().contains(keyword)
                    || orderId.toLowerCase().contains(keyword);
            boolean statusMatch = "Tất cả".equals(status) || status.equals(currentStatus);
            if (keywordMatch && statusMatch) visibleItems.add(serial);
        }
        countLabel.setText(visibleItems.size() + " serial");
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("receipt-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
