package GUI.JavaFX;

import Bus.CustomerBUS;
import DTO.CustomerDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * JavaFX replacement for GUI.Panel.CustomerPanel.
 *
 * The BUS/DAO/DTO layer is intentionally reused so this migration only changes
 * the presentation layer.
 */
public final class CustomerView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final BorderPane root = new BorderPane();
    private final CustomerBUS customerBUS = new CustomerBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final ObservableList<CustomerDTO> customers = FXCollections.observableArrayList();
    private final FilteredList<CustomerDTO> filteredCustomers =
            new FilteredList<>(customers, customer -> true);

    private final TableView<CustomerDTO> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final Label resultLabel = new Label();
    private final Button editButton = new Button("Sửa khách hàng");

    public CustomerView() {
        buildView();
        configureSearch();
        loadCustomers();
    }

    public Parent getRoot() {
        return root;
    }

    public void refresh() {
        loadCustomers();
    }

    private void buildView() {
        root.getStyleClass().add("customer-root");
        root.setTop(createHeader());
        root.setCenter(createContent());
    }

    private VBox createHeader() {
        Label eyebrow = new Label("CUSTOMER MANAGEMENT");
        eyebrow.getStyleClass().add("customer-eyebrow");

        Label title = new Label("Quản lý khách hàng");
        title.getStyleClass().add("customer-title");

        Label subtitle = new Label("Tra cứu thông tin và cập nhật khách hàng trong hệ thống.");
        subtitle.getStyleClass().add("customer-subtitle");

        VBox copy = new VBox(3, eyebrow, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resultLabel.getStyleClass().add("customer-count");

        HBox row = new HBox(18, copy, spacer, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.getStyleClass().add("customer-header");
        return header;
    }

    private VBox createContent() {
        HBox toolbar = createToolbar();
        configureTable();

        VBox tableCard = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getStyleClass().add("customer-table-card");

        VBox content = new VBox(16, toolbar, tableCard);
        content.setPadding(new Insets(22, 28, 28, 28));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private HBox createToolbar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Tên khách hàng, số điện thoại hoặc mã khách hàng...");
        searchField.setPrefWidth(360);
        searchField.getStyleClass().add("customer-search-field");
        searchField.setTooltip(new Tooltip("Tìm khách hàng theo ID, tên hoặc số điện thoại"));

        Button searchButton = new Button("Tìm");
        searchButton.getStyleClass().addAll("customer-button", "secondary-button");
        searchButton.setOnAction(event -> applyFilter(searchField.getText()));

        Button refreshButton = new Button("Làm mới");
        refreshButton.getStyleClass().addAll("customer-button", "secondary-button");
        refreshButton.setOnAction(event -> {
            searchField.clear();
            loadCustomers();
        });

        editButton.getStyleClass().addAll("customer-button", "primary-button");
        editButton.setVisible(permissionManager.hasPermission("CUSTOMER", "UPDATE"));
        editButton.setManaged(editButton.isVisible());
        editButton.setOnAction(event -> editSelectedCustomer());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                searchLabel,
                searchField,
                searchButton,
                spacer,
                refreshButton,
                editButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14, 16, 14, 16));
        toolbar.getStyleClass().add("customer-toolbar");
        return toolbar;
    }

    private void configureTable() {
        table.setItems(filteredCustomers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label("Không có khách hàng phù hợp."));
        table.getStyleClass().add("customer-table");

        TableColumn<CustomerDTO, Integer> sttColumn = new TableColumn<>("STT");
        sttColumn.setPrefWidth(55);
        sttColumn.setMinWidth(48);
        sttColumn.setMaxWidth(70);
        sttColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(0));
        sttColumn.setCellFactory(column -> new TableCell<CustomerDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<CustomerDTO, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getCustomerId()));

        TableColumn<CustomerDTO, String> nameColumn = new TableColumn<>("Tên khách hàng");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getCustomerName())));

        TableColumn<CustomerDTO, String> phoneColumn = new TableColumn<>("Số điện thoại");
        phoneColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getPhoneNumber())));

        TableColumn<CustomerDTO, String> createdColumn = new TableColumn<>("Ngày tạo");
        createdColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatDateTime(data.getValue().getCreatedAt())));

        TableColumn<CustomerDTO, String> updatedColumn = new TableColumn<>("Ngày cập nhật");
        updatedColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatDateTime(data.getValue().getUpdatedAt())));

        table.getColumns().setAll(
                sttColumn,
                idColumn,
                nameColumn,
                phoneColumn,
                createdColumn,
                updatedColumn);

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null
                    && permissionManager.hasPermission("CUSTOMER", "UPDATE")) {
                editSelectedCustomer();
            }
        });
    }

    private void configureSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter(newValue));
    }

    private void applyFilter(String keyword) {
        final String normalized = safe(keyword).trim().toLowerCase(Locale.ROOT);

        filteredCustomers.setPredicate(customer -> {
            if (normalized.isEmpty()) {
                return true;
            }

            return String.valueOf(customer.getCustomerId()).toLowerCase(Locale.ROOT).contains(normalized)
                    || safe(customer.getCustomerName()).toLowerCase(Locale.ROOT).contains(normalized)
                    || safe(customer.getPhoneNumber()).toLowerCase(Locale.ROOT).contains(normalized)
                    || formatDateTime(customer.getCreatedAt()).toLowerCase(Locale.ROOT).contains(normalized)
                    || formatDateTime(customer.getUpdatedAt()).toLowerCase(Locale.ROOT).contains(normalized);
        });

        updateResultCount();
    }

    private void loadCustomers() {
        try {
            ArrayList<CustomerDTO> list = customerBUS.getAll();
            customers.setAll(list == null ? new ArrayList<>() : list);
            applyFilter(searchField.getText());
        } catch (RuntimeException ex) {
            customers.clear();
            updateResultCount();
            showAlert(Alert.AlertType.ERROR,
                    "Không thể tải dữ liệu",
                    "Không thể tải danh sách khách hàng.\n" + safe(ex.getMessage()));
        }
    }

    private void editSelectedCustomer() {
        if (!permissionManager.hasPermission("CUSTOMER", "UPDATE")) {
            showAlert(Alert.AlertType.WARNING,
                    "Không có quyền truy cập",
                    "Tài khoản hiện tại không có quyền sửa khách hàng.");
            return;
        }

        CustomerDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Chưa chọn khách hàng",
                    "Vui lòng chọn một khách hàng trong bảng trước khi sửa.");
            return;
        }

        CustomerEditDialogFx dialog = new CustomerEditDialogFx(selected, customerBUS);
        boolean updated = dialog.showAndWait();
        if (updated) {
            // CustomerDTO is updated by the dialog. Refresh renders the new values
            // while keeping the DAO/BUS implementation unchanged.
            table.refresh();
            applyFilter(searchField.getText());
        }
    }

    private void updateResultCount() {
        int visible = filteredCustomers.size();
        int total = customers.size();
        resultLabel.setText(visible == total
                ? total + " khách hàng"
                : visible + " / " + total + " khách hàng");
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "—" : value.format(DATE_TIME_FORMATTER);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
