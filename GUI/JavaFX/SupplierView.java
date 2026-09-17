package GUI.JavaFX;

import Bus.nhaCungCapBUS;
import DTO.nhaCungCapDTO;
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
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import valiateData.PermissionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX replacement for GUI.Panel.nhaCungCapPanel.
 * Supplier BUS/DAO/DTO remain unchanged.
 */
public final class SupplierView {

    private final BorderPane root = new BorderPane();
    private final nhaCungCapBUS supplierBUS = new nhaCungCapBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final TableView<nhaCungCapDTO> table = new TableView<>();
    private final ObservableList<nhaCungCapDTO> pageItems = FXCollections.observableArrayList();
    private final List<nhaCungCapDTO> currentList = new ArrayList<>();

    private final TextField searchField = new TextField();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private final Label recordLabel = new Label();
    private final TextField pageField = new TextField("1");
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();
    private final Button firstButton = new Button("<<");
    private final Button prevButton = new Button("<");
    private final Button nextButton = new Button(">");
    private final Button lastButton = new Button(">>");

    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPages = 1;

    public SupplierView() {
        buildView();
        loadSuppliers();
    }

    public Parent getRoot() {
        return root;
    }

    public void refresh() {
        loadSuppliers();
    }

    private void buildView() {
        root.getStyleClass().add("supplier-root");
        root.setTop(createHeader());
        root.setCenter(createContent());
    }

    private VBox createHeader() {
        Label eyebrow = new Label("SUPPLIER MANAGEMENT");
        eyebrow.getStyleClass().add("supplier-eyebrow");

        Label title = new Label("Quản lý nhà cung cấp");
        title.getStyleClass().add("supplier-title");

        Label subtitle = new Label("Tra cứu, thêm, cập nhật và quản lý nhà cung cấp trong hệ thống.");
        subtitle.getStyleClass().add("supplier-subtitle");

        VBox copy = new VBox(3, eyebrow, title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resultLabel.getStyleClass().add("supplier-count");

        HBox row = new HBox(18, copy, spacer, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.getStyleClass().add("supplier-header");
        return header;
    }

    private VBox createContent() {
        HBox toolbar = createToolbar();
        configureTable();

        VBox tableCard = new VBox(table, createPagination());
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getStyleClass().add("supplier-table-card");

        VBox content = new VBox(16, toolbar, tableCard);
        content.setPadding(new Insets(22, 28, 28, 28));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private HBox createToolbar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Mã, tên, số điện thoại hoặc địa chỉ...");
        searchField.setPrefWidth(330);
        searchField.getStyleClass().add("supplier-search-field");
        searchField.setTooltip(new Tooltip("Tìm nhà cung cấp theo mã, tên, số điện thoại hoặc địa chỉ"));
        searchField.setOnAction(event -> searchSuppliers());

        Button searchButton = createButton("Tìm", "secondary-button", this::searchSuppliers);
        Button refreshButton = createButton("Làm mới", "secondary-button", () -> {
            searchField.clear();
            loadSuppliers();
        });

        Button addButton = createButton("Thêm", "success-button", this::addSupplier);
        setPermissionVisibility(addButton, "SUPPLIER", "ADD");

        Button updateButton = createButton("Sửa", "primary-button", this::editSelectedSupplier);
        setPermissionVisibility(updateButton, "SUPPLIER", "UPDATE");

        Button deleteButton = createButton("Xóa", "danger-button", this::deleteSelectedSupplier);
        setPermissionVisibility(deleteButton, "SUPPLIER", "DELETE");

        Button productsButton = createButton("Quản lý SP", "secondary-button", this::manageProducts);
        setPermissionVisibility(productsButton, "SUPPLIER", "UPDATE");
        productsButton.setTooltip(new Tooltip("Quản lý các sản phẩm được liên kết với nhà cung cấp"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                searchLabel, searchField, searchButton,
                spacer,
                refreshButton, addButton, updateButton, deleteButton, productsButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14, 16, 14, 16));
        toolbar.getStyleClass().add("supplier-toolbar");
        return toolbar;
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("supplier-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void setPermissionVisibility(Button button, String module, String action) {
        boolean visible = permissionManager.hasPermission(module, action);
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label("Không có nhà cung cấp phù hợp."));
        table.getStyleClass().add("supplier-table");

        TableColumn<nhaCungCapDTO, Number> sttColumn = new TableColumn<>("STT");
        sttColumn.setPrefWidth(55);
        sttColumn.setMinWidth(48);
        sttColumn.setMaxWidth(70);
        sttColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(0));
        sttColumn.setCellFactory(column -> new TableCell<nhaCungCapDTO, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    int absoluteIndex = (currentPage - 1) * pageSize + getIndex() + 1;
                    setText(String.valueOf(absoluteIndex));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<nhaCungCapDTO, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getSupplier_id()));
        idColumn.setVisible(false);

        TableColumn<nhaCungCapDTO, String> codeColumn = new TableColumn<>("Mã nhà cung cấp");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getCode())));

        TableColumn<nhaCungCapDTO, String> nameColumn = new TableColumn<>("Tên nhà cung cấp");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getName())));

        TableColumn<nhaCungCapDTO, String> phoneColumn = new TableColumn<>("Số điện thoại");
        phoneColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getPhone_number())));

        TableColumn<nhaCungCapDTO, String> addressColumn = new TableColumn<>("Địa chỉ");
        addressColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getAddress())));

        table.getColumns().setAll(sttColumn, idColumn, codeColumn, nameColumn, phoneColumn, addressColumn);
    }

    private HBox createPagination() {
        configurePaginationButton(firstButton, () -> goToPage(1));
        configurePaginationButton(prevButton, () -> goToPage(currentPage - 1));
        configurePaginationButton(nextButton, () -> goToPage(currentPage + 1));
        configurePaginationButton(lastButton, () -> goToPage(totalPages));

        pageLabel.getStyleClass().add("pagination-label");
        recordLabel.getStyleClass().add("pagination-record-label");

        pageField.setPrefWidth(52);
        pageField.setMaxWidth(52);
        pageField.setAlignment(Pos.CENTER);
        pageField.getStyleClass().add("page-field");
        pageField.setOnAction(event -> {
            try {
                goToPage(Integer.parseInt(pageField.getText().trim()));
            } catch (NumberFormatException ex) {
                pageField.setText(String.valueOf(currentPage));
            }
        });

        pageSizeBox.getItems().addAll(10, 20, 30, 50, 100);
        pageSizeBox.setValue(pageSize);
        pageSizeBox.getStyleClass().add("page-size-box");
        pageSizeBox.setOnAction(event -> {
            Integer selected = pageSizeBox.getValue();
            if (selected != null) {
                pageSize = selected;
                currentPage = 1;
                renderPage();
            }
        });

        Label pageSizeLabel = new Label("Hiển thị:");
        pageSizeLabel.getStyleClass().add("pagination-record-label");

        HBox pagination = new HBox(8,
                firstButton, prevButton, pageLabel, pageField, nextButton, lastButton,
                new Region(), pageSizeLabel, pageSizeBox, recordLabel);
        HBox.setHgrow(pagination.getChildren().get(6), Priority.ALWAYS);
        pagination.setAlignment(Pos.CENTER_LEFT);
        pagination.setPadding(new Insets(12, 14, 12, 14));
        pagination.getStyleClass().add("supplier-pagination");
        return pagination;
    }

    private void loadSuppliers() {
        try {
            ArrayList<nhaCungCapDTO> list = supplierBUS.getAllNhaCungCap();
            currentList.clear();
            if (list != null) {
                currentList.addAll(list);
            }
            currentPage = 1;
            renderPage();
        } catch (RuntimeException ex) {
            currentList.clear();
            currentPage = 1;
            renderPage();
            showAlert(Alert.AlertType.ERROR, "Không thể tải dữ liệu",
                    "Không thể tải danh sách nhà cung cấp.\n" + safe(ex.getMessage()));
        }
    }

    private void searchSuppliers() {
        try {
            String keyword = searchField.getText().trim();
            ArrayList<nhaCungCapDTO> list = supplierBUS.searchNhaCungCap(keyword);
            currentList.clear();
            if (list != null) {
                currentList.addAll(list);
            }
            currentPage = 1;
            renderPage();
        } catch (RuntimeException ex) {
            showAlert(Alert.AlertType.ERROR, "Không thể tìm kiếm",
                    "Tìm kiếm nhà cung cấp thất bại.\n" + safe(ex.getMessage()));
        }
    }

    private void renderPage() {
        int total = currentList.size();
        totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage < 1) {
            currentPage = 1;
        } else if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        pageItems.clear();
        if (start < end) {
            pageItems.addAll(currentList.subList(start, end));
        }

        pageLabel.setText("Trang " + currentPage + " / " + totalPages);
        pageField.setText(String.valueOf(currentPage));

        int displayStart = total == 0 ? 0 : start + 1;
        int displayEnd = total == 0 ? 0 : end;
        recordLabel.setText("Đang hiển thị " + displayStart + " - " + displayEnd + " trên tổng " + total);
        resultLabel.setText(total + " nhà cung cấp");

        updatePaginationButtons();
    }

    private void configurePaginationButton(Button button, Runnable action) {
        button.getStyleClass().addAll("supplier-button", "secondary-button");
        button.setOnAction(event -> action.run());
    }

    private void updatePaginationButtons() {
        boolean atFirstPage = currentPage <= 1;
        boolean atLastPage = currentPage >= totalPages;
        firstButton.setDisable(atFirstPage);
        prevButton.setDisable(atFirstPage);
        nextButton.setDisable(atLastPage);
        lastButton.setDisable(atLastPage);
    }

    private void goToPage(int page) {
        if (page < 1 || page > totalPages) {
            pageField.setText(String.valueOf(currentPage));
            return;
        }
        currentPage = page;
        renderPage();
    }

    private void addSupplier() {
        if (!permissionManager.hasPermission("SUPPLIER", "ADD")) {
            showPermissionWarning("thêm nhà cung cấp");
            return;
        }

        SupplierEditDialogFx dialog = new SupplierEditDialogFx(null, supplierBUS);
        if (dialog.showAndWait()) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm nhà cung cấp thành công.");
            loadSuppliers();
        }
    }

    private void editSelectedSupplier() {
        if (!permissionManager.hasPermission("SUPPLIER", "UPDATE")) {
            showPermissionWarning("sửa nhà cung cấp");
            return;
        }

        nhaCungCapDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn nhà cung cấp",
                    "Vui lòng chọn nhà cung cấp cần sửa.");
            return;
        }

        SupplierEditDialogFx dialog = new SupplierEditDialogFx(selected, supplierBUS);
        if (dialog.showAndWait()) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật nhà cung cấp thành công.");
            reloadCurrentQuery();
        }
    }

    private void deleteSelectedSupplier() {
        if (!permissionManager.hasPermission("SUPPLIER", "DELETE")) {
            showPermissionWarning("xóa nhà cung cấp");
            return;
        }

        nhaCungCapDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn nhà cung cấp",
                    "Vui lòng chọn nhà cung cấp cần xóa.");
            return;
        }

        try {
            if (supplierBUS.isSupplierInUse(selected.getSupplier_id())) {
                showAlert(Alert.AlertType.WARNING, "Không thể xóa",
                        "Không thể xóa nhà cung cấp này vì đã có phiếu nhập hoặc sản phẩm liên quan.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xóa");
            confirm.setHeaderText("Xóa nhà cung cấp " + safe(selected.getName()) + "?");
            confirm.setContentText("Mã nhà cung cấp: " + safe(selected.getCode()));

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            if (supplierBUS.deleteNhaCungCap(selected.getSupplier_id())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa nhà cung cấp thành công.");
                reloadCurrentQuery();
            } else {
                showAlert(Alert.AlertType.ERROR, "Xóa thất bại", "Xóa nhà cung cấp thất bại.");
            }
        } catch (RuntimeException ex) {
            showAlert(Alert.AlertType.ERROR, "Xóa thất bại",
                    "Không thể xóa nhà cung cấp.\n" + safe(ex.getMessage()));
        }
    }

    private void manageProducts() {
        if (!permissionManager.hasPermission("SUPPLIER", "UPDATE")) {
            showPermissionWarning("quản lý sản phẩm của nhà cung cấp");
            return;
        }

        nhaCungCapDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn nhà cung cấp",
                    "Vui lòng chọn nhà cung cấp cần quản lý sản phẩm.");
            return;
        }

        final int supplierId = selected.getSupplier_id();
        final String supplierName = safe(selected.getName());

        javafx.stage.Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        SupplierProductsDialogFx dialog = new SupplierProductsDialogFx(owner, supplierId, supplierName);
        dialog.showAndWait();
    }

    private void reloadCurrentQuery() {
        if (searchField.getText().trim().isEmpty()) {
            loadSuppliers();
        } else {
            searchSuppliers();
        }
    }

    private void showPermissionWarning(String action) {
        showAlert(Alert.AlertType.WARNING, "Không có quyền truy cập",
                "Tài khoản hiện tại không có quyền " + action + ".");
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
