package GUI.JavaFX;

import Bus.EmployeeBUS;
import DTO.EmployeeDTO;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import valiateData.PermissionManager;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

/** JavaFX replacement for GUI.Panel.EmployeePanel. */
public final class EmployeeView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private static final DecimalFormat SALARY_FORMAT = new DecimalFormat("#,##0");

    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final ObservableList<EmployeeDTO> employees = FXCollections.observableArrayList();
    private final ObservableList<EmployeeDTO> filteredEmployees = FXCollections.observableArrayList();
    private final ObservableList<EmployeeDTO> pageItems = FXCollections.observableArrayList();

    private final BorderPane root = new BorderPane();
    private final TextField searchField = new TextField();
    private final TableView<EmployeeDTO> table = new TableView<>();
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

    public EmployeeView() {
        buildView();
        configureSearch();
        loadEmployees();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("employee-root");

        VBox header = createHeader();
        VBox content = new VBox(14, createToolbar(), createTableCard());
        content.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(content.getChildren().get(1), Priority.ALWAYS);

        root.setTop(header);
        root.setCenter(content);
        root.setBottom(createPagination());
    }

    private VBox createHeader() {
        Label eyebrow = new Label("NHÂN SỰ");
        eyebrow.getStyleClass().add("employee-eyebrow");

        Label title = new Label("Quản lý thông tin nhân viên");
        title.getStyleClass().add("employee-title");

        Label subtitle = new Label("Theo dõi hồ sơ, lương và trạng thái làm việc của nhân viên.");
        subtitle.getStyleClass().add("employee-subtitle");

        VBox textBox = new VBox(2, eyebrow, title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        resultLabel.getStyleClass().add("employee-count");

        HBox row = new HBox(16, textBox, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.getStyleClass().add("employee-header");
        header.setPadding(new Insets(18, 22, 18, 22));
        return header;
    }

    private HBox createToolbar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Tên, mã NV, SĐT, lương, trạng thái...");
        searchField.getStyleClass().add("employee-search-field");
        searchField.setPrefWidth(300);

        Button refreshButton = createButton("Làm mới", "secondary-button");
        refreshButton.setOnAction(event -> loadEmployees());

        addButton = createButton("Thêm", "success-button");
        addButton.setVisible(permissionManager.hasPermission("EMPLOYEE", "ADD"));
        addButton.setManaged(addButton.isVisible());
        addButton.setOnAction(event -> addEmployee());

        editButton = createButton("Sửa", "primary-button");
        editButton.setVisible(permissionManager.hasPermission("EMPLOYEE", "UPDATE"));
        editButton.setManaged(editButton.isVisible());
        editButton.setDisable(true);
        editButton.setOnAction(event -> editSelectedEmployee());

        deleteButton = createButton("Xóa", "danger-button");
        deleteButton.setVisible(permissionManager.hasPermission("EMPLOYEE", "DELETE"));
        deleteButton.setManaged(deleteButton.isVisible());
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> deleteSelectedEmployee());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                searchLabel, searchField, refreshButton,
                spacer, addButton, editButton, deleteButton);
        toolbar.getStyleClass().add("employee-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12));
        return toolbar;
    }

    private VBox createTableCard() {
        configureTable();
        VBox card = new VBox(table);
        card.getStyleClass().add("employee-table-card");
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

        pageLabel.getStyleClass().add("employee-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.getStyleClass().add("employee-pagination");
        return footer;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.getStyleClass().add("employee-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu nhân viên"));
        table.setItems(pageItems);

        TableColumn<EmployeeDTO, Void> indexCol = new TableColumn<>("STT");
        indexCol.setPrefWidth(58);
        indexCol.setMaxWidth(70);
        indexCol.setCellFactory(column -> new TableCell<EmployeeDTO, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1 + (currentPage - 1) * pageSize));
            }
        });

        TableColumn<EmployeeDTO, Long> idCol = new TableColumn<>("Mã NV");
        idCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        idCol.setPrefWidth(80);

        TableColumn<EmployeeDTO, String> nameCol = new TableColumn<>("Họ & Tên");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        nameCol.setPrefWidth(180);

        TableColumn<EmployeeDTO, String> phoneCol = new TableColumn<>("SĐT");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);

        TableColumn<EmployeeDTO, BigDecimal> salaryCol = new TableColumn<>("Lương");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        salaryCol.setPrefWidth(115);
        salaryCol.setCellFactory(column -> new TableCell<EmployeeDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : SALARY_FORMAT.format(value));
            }
        });

        TableColumn<EmployeeDTO, java.sql.Timestamp> createdCol = new TableColumn<>("Ngày tạo");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdCol.setPrefWidth(155);
        createdCol.setCellFactory(column -> timestampCell());

        TableColumn<EmployeeDTO, java.sql.Timestamp> updatedCol = new TableColumn<>("Cập nhật mới nhất");
        updatedCol.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        updatedCol.setPrefWidth(165);
        updatedCol.setCellFactory(column -> timestampCell());

        TableColumn<EmployeeDTO, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        table.getColumns().setAll(indexCol, idCol, nameCol, phoneCol, salaryCol, createdCol, updatedCol, statusCol);

        table.setRowFactory(tv -> {
            TableRow<EmployeeDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(EmployeeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().remove("inactive-row");
                    if (!empty && item != null && "INACTIVE".equalsIgnoreCase(safe(item.getStatus()))) {
                        getStyleClass().add("inactive-row");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()
                        && permissionManager.hasPermission("EMPLOYEE", "UPDATE")) {
                    table.getSelectionModel().select(row.getItem());
                    editSelectedEmployee();
                }
            });
            return row;
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (editButton != null) {
                editButton.setDisable(newValue == null);
            }
            if (deleteButton != null) {
                deleteButton.setDisable(newValue == null);
            }
        });
    }

    private TableCell<EmployeeDTO, java.sql.Timestamp> timestampCell() {
        return new TableCell<EmployeeDTO, java.sql.Timestamp>() {
            @Override
            protected void updateItem(java.sql.Timestamp value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.toLocalDateTime().format(DATE_TIME_FORMATTER));
            }
        };
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("employee-button", styleClass);
        return button;
    }

    private void configureSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            applyFilter(newValue);
        });
    }

    private void applyFilter(String keyword) {
        String normalized = safe(keyword).trim().toLowerCase(Locale.ROOT);

        filteredEmployees.clear();
        for (EmployeeDTO employee : employees) {
            if (employee == null) continue;
            boolean matches = normalized.isEmpty()
                    || String.valueOf(employee.getEmployeeId()).toLowerCase(Locale.ROOT).contains(normalized)
                    || safe(employee.getEmployeeName()).toLowerCase(Locale.ROOT).contains(normalized)
                    || safe(employee.getPhoneNumber()).toLowerCase(Locale.ROOT).contains(normalized)
                    || formatSalary(employee.getSalary()).toLowerCase(Locale.ROOT).contains(normalized)
                    || formatTimestamp(employee.getCreatedAt()).toLowerCase(Locale.ROOT).contains(normalized)
                    || formatTimestamp(employee.getUpdatedAt()).toLowerCase(Locale.ROOT).contains(normalized)
                    || safe(employee.getStatus()).toLowerCase(Locale.ROOT).contains(normalized);
            if (matches) filteredEmployees.add(employee);
        }

        if (currentPage > totalPages()) currentPage = totalPages();
        if (currentPage < 1) currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int total = filteredEmployees.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredEmployees.subList(start, end));
        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages);
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        updateResultCount();
        table.refresh();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredEmployees.size() / (double) pageSize));
    }

    private void loadEmployees() {
        try {
            ArrayList<EmployeeDTO> list = employeeBUS.getAllEmployees();
            employees.setAll(list == null ? new ArrayList<>() : list);
            currentPage = 1;
            applyFilter(searchField.getText());
        } catch (RuntimeException ex) {
            employees.clear();
            currentPage = 1;
            applyFilter(searchField.getText());
            showAlert(Alert.AlertType.ERROR,
                    "Không thể tải dữ liệu",
                    "Không thể tải danh sách nhân viên.\n" + safe(ex.getMessage()));
        }
    }

    private void addEmployee() {
        if (!permissionManager.hasPermission("EMPLOYEE", "ADD")) {
            showPermissionWarning("thêm nhân viên");
            return;
        }

        EmployeeEditDialogFx dialog = new EmployeeEditDialogFx(null, employeeBUS);
        if (dialog.showAndWait()) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm nhân viên thành công.");
            loadEmployees();
        }
    }

    private void editSelectedEmployee() {
        if (!permissionManager.hasPermission("EMPLOYEE", "UPDATE")) {
            showPermissionWarning("sửa nhân viên");
            return;
        }

        EmployeeDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Chưa chọn nhân viên",
                    "Vui lòng chọn nhân viên cần sửa.");
            return;
        }

        EmployeeDTO latest = employeeBUS.getEmployeeById(selected.getEmployeeId());
        if (latest == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Không tìm thấy nhân viên",
                    "Dữ liệu nhân viên có thể đã thay đổi. Hãy làm mới danh sách.");
            return;
        }

        EmployeeEditDialogFx dialog = new EmployeeEditDialogFx(latest, employeeBUS);
        if (dialog.showAndWait()) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật nhân viên thành công.");
            loadEmployees();
        }
    }

    private void deleteSelectedEmployee() {
        if (!permissionManager.hasPermission("EMPLOYEE", "DELETE")) {
            showPermissionWarning("xóa nhân viên");
            return;
        }

        EmployeeDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Chưa chọn nhân viên",
                    "Vui lòng chọn nhân viên cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa nhân viên " + safe(selected.getEmployeeName()) + "?");
        confirm.setContentText("Thao tác hiện tại của hệ thống sẽ chuyển nhân viên sang trạng thái INACTIVE.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean success = employeeBUS.deleteEmployee(selected.getEmployeeId());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa nhân viên thành công.");
            loadEmployees();
        } else {
            showAlert(Alert.AlertType.ERROR, "Không thể xóa", "Xóa nhân viên thất bại.");
        }
    }

    private void updateResultCount() {
        int visible = filteredEmployees.size();
        int total = employees.size();
        resultLabel.setText(visible == total
                ? total + " nhân viên"
                : visible + " / " + total + " nhân viên");
    }

    private void showPermissionWarning(String action) {
        showAlert(Alert.AlertType.WARNING,
                "Không có quyền truy cập",
                "Tài khoản hiện tại không có quyền " + action + ".");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String formatSalary(BigDecimal value) {
        return value == null ? "" : SALARY_FORMAT.format(value);
    }

    private static String formatTimestamp(java.sql.Timestamp value) {
        return value == null ? "" : value.toLocalDateTime().format(DATE_TIME_FORMATTER);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}