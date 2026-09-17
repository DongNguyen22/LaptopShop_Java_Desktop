package GUI.JavaFX;

import Bus.SanPhamBUS;
import Bus.supplierProductsBUS;
import DTO.sanPham;
import DTO.supplierProductsDTO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JavaFX replacement for GUI.Dialog.SupplierProductsDialog.
 * Uses the existing supplierProductsBUS and SanPhamBUS without changing
 * BUS/DAO/DTO behavior.
 */
public final class SupplierProductsDialogFx {

    private final int supplierId;
    private final String supplierName;
    private final supplierProductsBUS supplierProductsBUS = new supplierProductsBUS();
    private final SanPhamBUS productBUS = new SanPhamBUS();
    private final Stage stage = new Stage();
    private final TableView<sanPham> table = new TableView<>();
    private final ObservableList<sanPham> tableItems = FXCollections.observableArrayList();
    private final Label countLabel = new Label();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.##");

    public SupplierProductsDialogFx(Window owner, int supplierId, String supplierName) {
        this.supplierId = supplierId;
        this.supplierName = safe(supplierName);
        configureStage(owner);
        buildView();
        loadData();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private void configureStage(Window owner) {
        stage.setTitle("Quản lý sản phẩm của nhà cung cấp: " + supplierName);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setMinWidth(720);
        stage.setMinHeight(500);
        stage.setWidth(820);
        stage.setHeight(560);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("supplier-products-root");
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        String css = getClass().getResource("supplier-products.css") == null
                ? null
                : getClass().getResource("supplier-products.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        }
        stage.setScene(scene);
    }

    private VBox createHeader() {
        Label eyebrow = new Label("SUPPLIER PRODUCTS");
        eyebrow.getStyleClass().add("supplier-products-eyebrow");

        Label title = new Label("Sản phẩm của nhà cung cấp");
        title.getStyleClass().add("supplier-products-title");

        Label supplier = new Label(supplierName);
        supplier.getStyleClass().add("supplier-products-supplier-name");

        Label subtitle = new Label("Quản lý các sản phẩm mà nhà cung cấp này có thể cung ứng.");
        subtitle.getStyleClass().add("supplier-products-subtitle");

        VBox copy = new VBox(3, eyebrow, title, supplier, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        countLabel.getStyleClass().add("supplier-products-count");
        HBox row = new HBox(18, copy, spacer, countLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.getStyleClass().add("supplier-products-header");
        return header;
    }

    private VBox createTableCard() {
        configureTable();

        Label hint = new Label("Chọn một sản phẩm trong bảng để xóa liên kết với nhà cung cấp.");
        hint.getStyleClass().add("supplier-products-hint");

        VBox card = new VBox(10, hint, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("supplier-products-card");
        BorderPane.setMargin(card, new Insets(18, 24, 14, 24));
        return card;
    }

    private void configureTable() {
        table.setItems(tableItems);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Nhà cung cấp chưa có sản phẩm nào."));
        table.getStyleClass().add("supplier-products-table");

        TableColumn<sanPham, String> idColumn = new TableColumn<>("Mã SP");
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getMaSP())));
        idColumn.setPrefWidth(120);

        TableColumn<sanPham, String> nameColumn = new TableColumn<>("Tên sản phẩm");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getTenSP())));
        nameColumn.setPrefWidth(360);

        TableColumn<sanPham, String> priceColumn = new TableColumn<>("Giá");
        priceColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(priceFormat.format(data.getValue().getDonGia())));
        priceColumn.setPrefWidth(170);

        table.getColumns().setAll(idColumn, nameColumn, priceColumn);
    }

    private HBox createFooter() {
        Button addButton = createButton("Thêm sản phẩm", "success-button", this::addProduct);
        addButton.setTooltip(new Tooltip("Liên kết thêm một sản phẩm với nhà cung cấp"));

        Button deleteButton = createButton("Xóa", "danger-button", this::deleteProduct);
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = createButton("Đóng", "secondary-button", stage::close);

        HBox footer = new HBox(10, addButton, deleteButton, spacer, closeButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 24, 20, 24));
        return footer;
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("supplier-products-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void loadData() {
        tableItems.clear();
        try {
            ArrayList<supplierProductsDTO> links = supplierProductsBUS.getProductsBySupplierId(supplierId);
            if (links != null) {
                for (supplierProductsDTO link : links) {
                    sanPham product = productBUS.getProductById(String.valueOf(link.getProduct_id()));
                    if (product != null) {
                        tableItems.add(product);
                    }
                }
            }
            countLabel.setText(tableItems.size() + " sản phẩm");
        } catch (RuntimeException ex) {
            countLabel.setText("0 sản phẩm");
            showAlert(Alert.AlertType.ERROR, "Không thể tải dữ liệu",
                    "Không thể tải danh sách sản phẩm của nhà cung cấp.\n" + safe(ex.getMessage()));
        }
    }

    private void addProduct() {
        try {
            List<sanPham> allProducts = productBUS.getAllProducts();
            ArrayList<supplierProductsDTO> current = supplierProductsBUS.getProductsBySupplierId(supplierId);

            Set<Integer> linkedIds = new HashSet<>();
            if (current != null) {
                for (supplierProductsDTO link : current) {
                    linkedIds.add(link.getProduct_id());
                }
            }

            List<sanPham> available = new ArrayList<>();
            if (allProducts != null) {
                for (sanPham product : allProducts) {
                    Integer productId = parseProductId(product);
                    if (productId != null && !linkedIds.contains(productId)) {
                        available.add(product);
                    }
                }
            }

            if (available.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Không còn sản phẩm",
                        "Tất cả sản phẩm đã được thêm vào nhà cung cấp này.");
                return;
            }

            Optional<sanPham> selected = showProductPicker(available);
            if (selected.isEmpty()) {
                return;
            }

            Integer productId = parseProductId(selected.get());
            if (productId == null) {
                showAlert(Alert.AlertType.ERROR, "Mã sản phẩm không hợp lệ",
                        "Không thể xác định mã sản phẩm để thêm vào nhà cung cấp.");
                return;
            }

            if (supplierProductsBUS.addProductToSupplier(supplierId, productId)) {
                loadData();
                table.getSelectionModel().select(
                        tableItems.stream()
                                .filter(item -> safe(item.getMaSP()).equals(String.valueOf(productId)))
                                .findFirst()
                                .orElse(null));
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm sản phẩm thành công.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Thêm thất bại",
                        "Không thể thêm sản phẩm. Sản phẩm có thể đã được liên kết với nhà cung cấp.");
            }
        } catch (RuntimeException ex) {
            showAlert(Alert.AlertType.ERROR, "Thêm thất bại",
                    "Không thể thêm sản phẩm vào nhà cung cấp.\n" + safe(ex.getMessage()));
        }
    }

    private Optional<sanPham> showProductPicker(List<sanPham> availableProducts) {
        Dialog<sanPham> dialog = new Dialog<>();
        dialog.setTitle("Chọn sản phẩm");
        dialog.setHeaderText("Chọn sản phẩm để thêm cho " + supplierName);
        if (stage.isShowing()) {
            dialog.initOwner(stage);
        }
        dialog.initModality(Modality.WINDOW_MODAL);

        ButtonType addType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, cancelType);
        dialog.getDialogPane().getStyleClass().add("supplier-products-picker");

        TextField searchField = new TextField();
        searchField.setPromptText("Tìm theo mã hoặc tên sản phẩm...");
        searchField.getStyleClass().add("supplier-products-search");

        ObservableList<sanPham> source = FXCollections.observableArrayList(availableProducts);
        ObservableList<sanPham> filtered = FXCollections.observableArrayList(source);
        ListView<sanPham> listView = new ListView<>(filtered);
        listView.setPrefSize(500, 330);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(sanPham item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(safe(item.getTenSP()) + "  •  Mã: " + safe(item.getMaSP())
                            + "  •  Giá: " + priceFormat.format(item.getDonGia()));
                }
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String keyword = safe(newValue).trim().toLowerCase();
            filtered.setAll(source.filtered(product -> keyword.isEmpty()
                    || safe(product.getMaSP()).toLowerCase().contains(keyword)
                    || safe(product.getTenSP()).toLowerCase().contains(keyword)));
            if (!filtered.isEmpty()) {
                listView.getSelectionModel().selectFirst();
            }
        });

        if (!filtered.isEmpty()) {
            listView.getSelectionModel().selectFirst();
        }

        VBox content = new VBox(10, searchField, listView);
        content.setPadding(new Insets(8, 4, 4, 4));
        dialog.getDialogPane().setContent(content);

        String css = getClass().getResource("supplier-products.css") == null
                ? null
                : getClass().getResource("supplier-products.css").toExternalForm();
        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css);
        }

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (listView.getSelectionModel().getSelectedItem() == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn sản phẩm",
                        "Vui lòng chọn sản phẩm cần thêm.");
                event.consume();
            }
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                addButton.fire();
            }
        });

        dialog.setResultConverter(buttonType ->
                buttonType == addType ? listView.getSelectionModel().getSelectedItem() : null);

        return dialog.showAndWait();
    }

    private void deleteProduct() {
        sanPham selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn sản phẩm",
                    "Vui lòng chọn sản phẩm cần xóa.");
            return;
        }

        Integer productId = parseProductId(selected);
        if (productId == null) {
            showAlert(Alert.AlertType.ERROR, "Mã sản phẩm không hợp lệ",
                    "Không thể xác định mã sản phẩm cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm khỏi nhà cung cấp?");
        confirm.setContentText("Sản phẩm: " + safe(selected.getTenSP())
                + "\nMã: " + safe(selected.getMaSP())
                + "\n\nThao tác này chỉ xóa liên kết giữa sản phẩm và nhà cung cấp.");
        if (stage.isShowing()) {
            confirm.initOwner(stage);
        }

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            if (supplierProductsBUS.deleteProductFromSupplier(supplierId, productId)) {
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa sản phẩm thành công.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Xóa thất bại", "Không thể xóa sản phẩm khỏi nhà cung cấp.");
            }
        } catch (RuntimeException ex) {
            showAlert(Alert.AlertType.ERROR, "Xóa thất bại",
                    "Không thể xóa sản phẩm khỏi nhà cung cấp.\n" + safe(ex.getMessage()));
        }
    }

    private Integer parseProductId(sanPham product) {
        if (product == null || product.getMaSP() == null) {
            return null;
        }
        try {
            return Integer.valueOf(product.getMaSP().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (stage.isShowing()) {
            alert.initOwner(stage);
        }
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
