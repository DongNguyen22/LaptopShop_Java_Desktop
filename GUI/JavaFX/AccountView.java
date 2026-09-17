package GUI.JavaFX;

import Bus.AccountBUS;
import Bus.EmployeeBUS;
import Bus.RoleBUS;
import DTO.AccountDTO;
import DTO.EmployeeDTO;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** JavaFX replacement for GUI.Panel.taiKhoanPanel. */
public final class AccountView {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AccountBUS accountBUS = new AccountBUS();
    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final RoleBUS roleBUS = new RoleBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final ObservableList<AccountDTO> accounts = FXCollections.observableArrayList();
    private final ObservableList<AccountDTO> filteredAccounts = FXCollections.observableArrayList();
    private final ObservableList<AccountDTO> pageItems = FXCollections.observableArrayList();
    private final Map<Long, String> employeeNames = new HashMap<>();
    private final Map<Integer, String> roleNames = new HashMap<>();

    private final BorderPane root = new BorderPane();
    private final TextField searchField = new TextField();
    private final TableView<AccountDTO> table = new TableView<>();
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

    public AccountView() {
        buildView();
        configureSearch();
        loadAccounts();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("account-root");

        VBox header = createHeader();
        VBox content = new VBox(14, createToolbar(), createTableCard());
        content.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(content.getChildren().get(1), Priority.ALWAYS);

        root.setTop(header);
        root.setCenter(content);
        root.setBottom(createPagination());
    }

    private VBox createHeader() {
        Label eyebrow = new Label("HỆ THỐNG");
        eyebrow.getStyleClass().add("account-eyebrow");

        Label title = new Label("Quản lý tài khoản");
        title.getStyleClass().add("account-title");

        Label subtitle = new Label("Quản lý tài khoản đăng nhập, nhân viên phụ trách và vai trò truy cập.");
        subtitle.getStyleClass().add("account-subtitle");

        VBox textBox = new VBox(2, eyebrow, title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        resultLabel.getStyleClass().add("account-count");

        HBox row = new HBox(16, textBox, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.getStyleClass().add("account-header");
        header.setPadding(new Insets(18, 22, 18, 22));
        return header;
    }

    private HBox createToolbar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Username, nhân viên, ID, vai trò, ngày cập nhật...");
        searchField.getStyleClass().add("account-search-field");
        searchField.setPrefWidth(330);

        Button refreshButton = createButton("Làm mới", "secondary-button");
        refreshButton.setOnAction(event -> loadAccounts());

        addButton = createButton("Thêm", "success-button");
        addButton.setVisible(permissionManager.hasPermission("ACCOUNT", "ADD"));
        addButton.setManaged(addButton.isVisible());
        addButton.setOnAction(event -> addAccount());

        editButton = createButton("Sửa", "primary-button");
        editButton.setVisible(permissionManager.hasPermission("ACCOUNT", "UPDATE"));
        editButton.setManaged(editButton.isVisible());
        editButton.setDisable(true);
        editButton.setOnAction(event -> editSelectedAccount());

        deleteButton = createButton("Xóa", "danger-button");
        deleteButton.setVisible(permissionManager.hasPermission("ACCOUNT", "DELETE"));
        deleteButton.setManaged(deleteButton.isVisible());
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> deleteSelectedAccount());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                searchLabel, searchField, refreshButton,
                spacer, addButton, editButton, deleteButton);
        toolbar.getStyleClass().add("account-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12));
        return toolbar;
    }

    private VBox createTableCard() {
        configureTable();
        VBox card = new VBox(table);
        card.getStyleClass().add("account-table-card");
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

        pageLabel.getStyleClass().add("account-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.getStyleClass().add("account-pagination");
        return footer;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.getStyleClass().add("account-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu tài khoản"));
        table.setItems(pageItems);

        TableColumn<AccountDTO, Number> indexCol = new TableColumn<>("STT");
        indexCol.setPrefWidth(55);
        indexCol.setMaxWidth(70);
        indexCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                pageItems.indexOf(data.getValue()) + 1 + (currentPage - 1) * pageSize));

        TableColumn<AccountDTO, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        idCol.setPrefWidth(70);

        TableColumn<AccountDTO, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getUsername())));
        usernameCol.setPrefWidth(150);

        TableColumn<AccountDTO, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(data -> new ReadOnlyStringWrapper("••••••"));
        passwordCol.setPrefWidth(95);

        TableColumn<AccountDTO, String> employeeCol = new TableColumn<>("Nhân viên");
        employeeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                employeeNames.getOrDefault(data.getValue().getEmployeeId(), "NV #" + data.getValue().getEmployeeId())));
        employeeCol.setPrefWidth(185);

        TableColumn<AccountDTO, Long> employeeIdCol = new TableColumn<>("ID NV");
        employeeIdCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEmployeeId()));
        employeeIdCol.setPrefWidth(78);

        TableColumn<AccountDTO, String> roleCol = new TableColumn<>("Vai trò");
        roleCol.setCellValueFactory(data -> {
            int roleId = data.getValue().getRole_id();
            String name = roleNames.get(roleId);
            return new ReadOnlyStringWrapper(name == null ? "Role #" + roleId : name + " (#" + roleId + ")");
        });
        roleCol.setPrefWidth(150);

        TableColumn<AccountDTO, String> updatedCol = new TableColumn<>("Cập nhật");
        updatedCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDate(data.getValue().getUpdateAt())));
        updatedCol.setPrefWidth(110);

        TableColumn<AccountDTO, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(statusText(data.getValue().getStatus())));
        statusCol.setPrefWidth(105);

        table.getColumns().setAll(indexCol, idCol, usernameCol, passwordCol, employeeCol,
                employeeIdCol, roleCol, updatedCol, statusCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            boolean none = selected == null;
            editButton.setDisable(none);
            deleteButton.setDisable(none);
        });

        table.setRowFactory(tv -> {
            TableRow<AccountDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()
                        && permissionManager.hasPermission("ACCOUNT", "UPDATE")) {
                    table.getSelectionModel().select(row.getItem());
                    editSelectedAccount();
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

    private void applyFilter(String keyword) {
        final String text = safe(keyword).trim().toLowerCase(Locale.ROOT);
        filteredAccounts.clear();
        for (AccountDTO account : accounts) {
            if (account == null) continue;
            if (text.isEmpty() || matches(account, text)) {
                filteredAccounts.add(account);
            }
        }
        if (currentPage > totalPages()) currentPage = totalPages();
        if (currentPage < 1) currentPage = 1;
        renderPage();
    }

    private boolean matches(AccountDTO account, String keyword) {
        if (account == null) {
            return false;
        }
        String employeeName = employeeNames.getOrDefault(account.getEmployeeId(), "");
        String roleName = roleNames.getOrDefault(account.getRole_id(), "");
        return contains(String.valueOf(account.getId()), keyword)
                || contains(account.getUsername(), keyword)
                || contains(employeeName, keyword)
                || contains(String.valueOf(account.getEmployeeId()), keyword)
                || contains(String.valueOf(account.getRole_id()), keyword)
                || contains(roleName, keyword)
                || contains(formatDate(account.getUpdateAt()), keyword)
                || contains(statusText(account.getStatus()), keyword)
                || contains(account.getStatus(), keyword);
    }

    private void loadAccounts() {
        try {
            rebuildLookups();
            accounts.setAll(accountBUS.getAll());
            currentPage = 1;
            applyFilter(searchField.getText());
        } catch (RuntimeException ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách tài khoản.\n" + safe(ex.getMessage()));
        }
    }

    private void rebuildLookups() {
        employeeNames.clear();
        for (EmployeeDTO employee : employeeBUS.getAllEmployees()) {
            if (employee != null && employee.getEmployeeId() != null) {
                employeeNames.put(employee.getEmployeeId(), safe(employee.getEmployeeName()));
            }
        }

        roleNames.clear();
        for (RoleDTO role : roleBUS.getAllRoles()) {
            if (role != null) {
                roleNames.put(role.getRoleId(), safe(role.getName()));
            }
        }
    }

    private void renderPage() {
        int total = filteredAccounts.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredAccounts.subList(start, end));
        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages);
        resultLabel.setText(total + " tài khoản");
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        table.refresh();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredAccounts.size() / (double) pageSize));
    }

    private void addAccount() {
        if (!permissionManager.hasPermission("ACCOUNT", "ADD")) {
            showPermissionDenied();
            return;
        }

        AccountEditDialogFx dialog = new AccountEditDialogFx(
                root.getScene() == null ? null : root.getScene().getWindow(),
                null, accountBUS, employeeBUS, roleBUS);
        if (dialog.showAndWait()) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm tài khoản thành công.");
            loadAccounts();
        }
    }

    private void editSelectedAccount() {
        if (!permissionManager.hasPermission("ACCOUNT", "UPDATE")) {
            showPermissionDenied();
            return;
        }

        AccountDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn tài khoản", "Vui lòng chọn tài khoản cần sửa.");
            return;
        }

        AccountDTO current = accountBUS.getById(selected.getId());
        if (current == null) {
            showAlert(Alert.AlertType.ERROR, "Không tìm thấy", "Tài khoản không còn tồn tại hoặc đã bị xóa.");
            loadAccounts();
            return;
        }

        AccountEditDialogFx dialog = new AccountEditDialogFx(
                root.getScene() == null ? null : root.getScene().getWindow(),
                current, accountBUS, employeeBUS, roleBUS);
        if (dialog.showAndWait()) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật tài khoản thành công.");
            loadAccounts();
        }
    }

    private void deleteSelectedAccount() {
        if (!permissionManager.hasPermission("ACCOUNT", "DELETE")) {
            showPermissionDenied();
            return;
        }

        AccountDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn tài khoản", "Vui lòng chọn tài khoản cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa tài khoản " + safe(selected.getUsername()) + "?");
        confirm.setContentText("Tài khoản sẽ được chuyển sang trạng thái không hoạt động.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean success = accountBUS.delete(selected.getId());
        if (!success) {
            showAlert(Alert.AlertType.ERROR, "Thất bại", "Xóa tài khoản thất bại.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa tài khoản thành công.");
        loadAccounts();
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("account-button", styleClass);
        return button;
    }

    private void showPermissionDenied() {
        showAlert(Alert.AlertType.WARNING, "Không có quyền", "Bạn không có quyền thực hiện thao tác này.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_FORMATTER);
    }

    private static String statusText(String status) {
        if ("1".equalsIgnoreCase(safe(status)) || "ACTIVE".equalsIgnoreCase(safe(status))) {
            return "Hoạt động";
        }
        if ("0".equalsIgnoreCase(safe(status)) || "INACTIVE".equalsIgnoreCase(safe(status))) {
            return "Ngừng hoạt động";
        }
        return safe(status);
    }

    private static boolean contains(String value, String keyword) {
        return safe(value).toLowerCase(Locale.ROOT).contains(keyword);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}