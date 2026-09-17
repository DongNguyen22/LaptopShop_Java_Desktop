package GUI.JavaFX;

import Bus.RoleBUS;
import DTO.RoleDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import valiateData.PermissionManager;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/** JavaFX replacement for GUI.Panel.phanQuyenPanel. */
public final class PermissionView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RoleBUS roleBUS = new RoleBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final ObservableList<RoleDTO> roles = FXCollections.observableArrayList();
    private final ObservableList<RoleDTO> filteredRoles = FXCollections.observableArrayList();
    private final ObservableList<RoleDTO> pageItems = FXCollections.observableArrayList();

    private final BorderPane root = new BorderPane();
    private final TextField searchField = new TextField();
    private final TableView<RoleDTO> table = new TableView<>();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();

    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button previousButton;
    private Button nextButton;

    private int currentPage = 1;
    private int pageSize = 10;

    public PermissionView() {
        buildView();
        configureSearch();
        loadRoles();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("permission-root");

        VBox content = new VBox(14, createToolbar(), createTableCard());
        content.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(content.getChildren().get(1), Priority.ALWAYS);

        root.setTop(createHeader());
        root.setCenter(content);
        root.setBottom(createPagination());
    }

    private VBox createHeader() {
        Label eyebrow = new Label("HỆ THỐNG");
        eyebrow.getStyleClass().add("permission-eyebrow");

        Label title = new Label("Quản lý phân quyền");
        title.getStyleClass().add("permission-title");

        Label subtitle = new Label("Quản lý vai trò và ma trận quyền truy cập theo từng module.");
        subtitle.getStyleClass().add("permission-subtitle");

        VBox textBox = new VBox(2, eyebrow, title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        resultLabel.getStyleClass().add("permission-count");

        HBox row = new HBox(16, textBox, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.getStyleClass().add("permission-header");
        header.setPadding(new Insets(18, 22, 18, 22));
        return header;
    }

    private HBox createToolbar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("ID hoặc tên role...");
        searchField.getStyleClass().add("permission-search-field");
        searchField.setPrefWidth(300);

        Button searchButton = createButton("Tìm", "secondary-button");
        searchButton.setOnAction(event -> {
            currentPage = 1;
            applyFilter(searchField.getText());
        });

        Button refreshButton = createButton("Làm mới", "secondary-button");
        refreshButton.setOnAction(event -> {
            searchField.clear();
            loadRoles();
        });

        addButton = createButton("Thêm role", "success-button");
        addButton.setVisible(permissionManager.hasPermission("PERMISSION", "ADD"));
        addButton.setManaged(addButton.isVisible());
        addButton.setOnAction(event -> addRole());

        editButton = createButton("Sửa quyền", "primary-button");
        editButton.setVisible(permissionManager.hasPermission("PERMISSION", "UPDATE"));
        editButton.setManaged(editButton.isVisible());
        editButton.setDisable(true);
        editButton.setOnAction(event -> editSelectedRole());

        deleteButton = createButton("Xóa", "danger-button");
        deleteButton.setVisible(permissionManager.hasPermission("PERMISSION", "DELETE"));
        deleteButton.setManaged(deleteButton.isVisible());
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> deleteSelectedRole());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                searchLabel, searchField, searchButton, refreshButton,
                spacer, addButton, editButton, deleteButton);
        toolbar.getStyleClass().add("permission-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12));
        return toolbar;
    }

    private VBox createTableCard() {
        configureTable();
        VBox card = new VBox(table);
        card.getStyleClass().add("permission-table-card");
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private HBox createPagination() {
        previousButton = createButton("‹ Trước", "secondary-button");
        previousButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderPage();
            }
        });

        nextButton = createButton("Sau ›", "secondary-button");
        nextButton.setOnAction(e -> {
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

        pageLabel.getStyleClass().add("permission-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.getStyleClass().add("permission-pagination");
        return footer;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.getStyleClass().add("permission-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu role"));
        table.setItems(pageItems);

        TableColumn<RoleDTO, Number> indexCol = new TableColumn<>("STT");
        indexCol.setPrefWidth(55);
        indexCol.setMaxWidth(70);
        indexCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                pageItems.indexOf(data.getValue()) + 1 + (currentPage - 1) * pageSize));

        TableColumn<RoleDTO, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoleId()));
        idCol.setPrefWidth(80);

        TableColumn<RoleDTO, String> nameCol = new TableColumn<>("Tên quyền");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getName())));
        nameCol.setPrefWidth(240);

        TableColumn<RoleDTO, String> createdCol = new TableColumn<>("Ngày tạo");
        createdCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatTimestamp(data.getValue().getCreatedAt())));
        createdCol.setPrefWidth(160);

        TableColumn<RoleDTO, String> updatedCol = new TableColumn<>("Ngày cập nhật");
        updatedCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatTimestamp(data.getValue().getUpdatedAt())));
        updatedCol.setPrefWidth(160);

        table.getColumns().setAll(indexCol, idCol, nameCol, createdCol, updatedCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            boolean none = selected == null;
            editButton.setDisable(none);
            deleteButton.setDisable(none);
        });

        table.setRowFactory(tv -> {
            TableRow<RoleDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()
                        && permissionManager.hasPermission("PERMISSION", "UPDATE")) {
                    table.getSelectionModel().select(row.getItem());
                    editSelectedRole();
                }
            });
            return row;
        });
    }

    private void configureSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter(newValue);
        });
    }

    private void loadRoles() {
        try {
            roles.setAll(roleBUS.getAllRoles());
            currentPage = 1;
            applyFilter(searchField.getText());
        } catch (RuntimeException ex) {
            showError("Không thể tải danh sách role", messageOf(ex));
        }
    }

    private void applyFilter(String keyword) {
        String text = safe(keyword).trim().toLowerCase(Locale.ROOT);
        filteredRoles.clear();
        for (RoleDTO role : roles) {
            if (role == null) continue;
            boolean matches = text.isEmpty()
                    || String.valueOf(role.getRoleId()).contains(text)
                    || safe(role.getName()).toLowerCase(Locale.ROOT).contains(text)
                    || formatTimestamp(role.getCreatedAt()).toLowerCase(Locale.ROOT).contains(text)
                    || formatTimestamp(role.getUpdatedAt()).toLowerCase(Locale.ROOT).contains(text);
            if (matches) filteredRoles.add(role);
        }
        if (currentPage > totalPages()) currentPage = totalPages();
        if (currentPage < 1) currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int total = filteredRoles.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredRoles.subList(start, end));
        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages);
        resultLabel.setText(total + " role");
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        table.refresh();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredRoles.size() / (double) pageSize));
    }

    private void addRole() {
        if (!permissionManager.hasPermission("PERMISSION", "ADD")) {
            showWarning("Không có quyền truy cập", "Tài khoản hiện tại không có quyền thêm role.");
            return;
        }

        RolePermissionsDialogFx dialog = new RolePermissionsDialogFx(null);
        dialog.showAndWait();
        loadRoles();
    }

    private void editSelectedRole() {
        if (!permissionManager.hasPermission("PERMISSION", "UPDATE")) {
            showWarning("Không có quyền truy cập", "Tài khoản hiện tại không có quyền cập nhật role.");
            return;
        }

        RoleDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn role", "Vui lòng chọn một role để sửa quyền.");
            return;
        }

        RoleDTO latest = roleBUS.getById(selected.getRoleId());
        if (latest == null) {
            showWarning("Không tìm thấy role", "Role này có thể đã bị xóa hoặc thay đổi.");
            loadRoles();
            return;
        }

        RolePermissionsDialogFx dialog = new RolePermissionsDialogFx(latest);
        dialog.showAndWait();
        loadRoles();
    }

    private void deleteSelectedRole() {
        if (!permissionManager.hasPermission("PERMISSION", "DELETE")) {
            showWarning("Không có quyền truy cập", "Tài khoản hiện tại không có quyền xóa role.");
            return;
        }

        RoleDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn role", "Vui lòng chọn role cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa role \"" + safe(selected.getName()) + "\"?");
        confirm.setContentText("Role đang được tài khoản sử dụng sẽ không thể xóa.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            if (!roleBUS.deleteRole(selected.getRoleId())) {
                showWarning("Không thể xóa role",
                        "Role đang được sử dụng bởi ít nhất một tài khoản.");
                return;
            }
            showInfo("Thành công", "Xóa role thành công.");
            loadRoles();
        } catch (RuntimeException ex) {
            showError("Không thể xóa role", messageOf(ex));
        }
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("permission-button", styleClass);
        return button;
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "—";
        }
        return timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String messageOf(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.trim().isEmpty()
                ? throwable.getClass().getSimpleName()
                : message;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}