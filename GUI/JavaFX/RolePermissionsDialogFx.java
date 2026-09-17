package GUI.JavaFX;

import Bus.RoleBUS;
import Bus.RolePermissionsBUS;
import DTO.RoleDTO;
import DTO.RolePermissionsDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.RolesDIALOG. */
public final class RolePermissionsDialogFx {

    private final RolePermissionsBUS rolePermissionsBUS = new RolePermissionsBUS();
    private final RoleBUS roleBUS = new RoleBUS();

    private final boolean newRole;
    private final RoleDTO role;
    private final ObservableList<RolePermissionsDTO> permissions = FXCollections.observableArrayList();

    private final Stage stage = new Stage();
    private final TextField roleNameField = new TextField();
    private final TableView<RolePermissionsDTO> table = new TableView<>();
    private final Label statusLabel = new Label();

    private Button createRoleButton;
    private Button saveButton;
    private int roleId = -1;

    public RolePermissionsDialogFx(RoleDTO role) {
        this.role = role;
        this.newRole = role == null;
        if (!newRole) {
            roleId = role.getRoleId();
        }
        buildStage();
        if (!newRole) {
            loadPermissions();
        }
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private void buildStage() {
        stage.setTitle(newRole ? "Thêm Role" : "Sửa Role");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setMinWidth(760);
        stage.setMinHeight(570);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("role-dialog-root");
        root.setTop(createHeader());
        root.setCenter(createCenter());
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 900, 670);
        URL css = RolePermissionsDialogFx.class.getResource("/GUI/JavaFX/permission.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        installShortcut(scene);

        stage.setScene(scene);
    }

    private VBox createHeader() {
        Label eyebrow = new Label(newRole ? "ROLE MỚI" : "MA TRẬN QUYỀN");
        eyebrow.getStyleClass().add("dialog-eyebrow");

        Label title = new Label(newRole ? "Tạo role và thiết lập quyền" : "Phân quyền: " + safe(role.getName()));
        title.getStyleClass().add("dialog-title");

        Label hint = new Label(newRole
                ? "Tạo role trước, sau đó bật các quyền cần thiết và bấm Lưu."
                : "Đánh dấu quyền theo từng module. Tên role được giữ nguyên như màn hình Swing cũ.");
        hint.getStyleClass().add("dialog-subtitle");

        VBox box = new VBox(2, eyebrow, title, hint);
        box.getStyleClass().add("role-dialog-header");
        box.setPadding(new Insets(18, 22, 16, 22));
        return box;
    }

    private VBox createCenter() {
        HBox roleRow = createRoleRow();
        VBox tableCard = createPermissionTableCard();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        VBox center = new VBox(14, roleRow, tableCard);
        center.setPadding(new Insets(16, 20, 12, 20));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return center;
    }

    private HBox createRoleRow() {
        Label label = new Label("Tên Role");
        label.getStyleClass().add("dialog-field-label");

        roleNameField.setPromptText("Ví dụ: Nhân viên kho");
        roleNameField.getStyleClass().add("role-name-field");
        HBox.setHgrow(roleNameField, Priority.ALWAYS);

        createRoleButton = createButton("Tạo Role", "success-button");
        createRoleButton.setOnAction(event -> createRole());

        if (newRole) {
            roleNameField.setEditable(true);
            createRoleButton.setDisable(false);
        } else {
            roleNameField.setText(safe(role.getName()));
            roleNameField.setEditable(false);
            roleNameField.getStyleClass().add("readonly-field");
            createRoleButton.setDisable(true);
            createRoleButton.setManaged(false);
            createRoleButton.setVisible(false);
        }

        HBox row = new HBox(10, label, roleNameField, createRoleButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("role-name-row");
        row.setPadding(new Insets(12));
        return row;
    }

    private VBox createPermissionTableCard() {
        configureTable();

        statusLabel.getStyleClass().add("permission-hint");
        updateStatusLabel();

        Label shortcut = new Label("Mẹo: bấm tiêu đề VIEW/ADD/UPDATE/DELETE để chọn cả cột · Ctrl+Shift+X để bật/tắt toàn bộ.");
        shortcut.getStyleClass().add("permission-shortcut");

        VBox card = new VBox(8, statusLabel, table, shortcut);
        card.getStyleClass().add("permission-matrix-card");
        card.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.getStyleClass().add("permission-matrix-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(newRole
                ? "Hãy tạo role trước để tải danh sách module và quyền."
                : "Không có dữ liệu quyền cho role này."));
        table.setItems(permissions);
        table.setEditable(true);

        TableColumn<RolePermissionsDTO, String> moduleCol = new TableColumn<>("Module");
        moduleCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(
                safe(data.getValue().getModuleName())));
        moduleCol.setPrefWidth(300);

        TableColumn<RolePermissionsDTO, Boolean> viewCol = createPermissionColumn(
                "VIEW", RolePermissionsDTO::isView, RolePermissionsDTO::setView,
                RolePermissionsDTO::getViewId, "VIEW");
        TableColumn<RolePermissionsDTO, Boolean> addCol = createPermissionColumn(
                "ADD", RolePermissionsDTO::isAdd, RolePermissionsDTO::setAdd,
                RolePermissionsDTO::getAddId, "ADD");
        TableColumn<RolePermissionsDTO, Boolean> updateCol = createPermissionColumn(
                "UPDATE", RolePermissionsDTO::isEdit, RolePermissionsDTO::setEdit,
                RolePermissionsDTO::getEditId, "UPDATE");
        TableColumn<RolePermissionsDTO, Boolean> deleteCol = createPermissionColumn(
                "DELETE", RolePermissionsDTO::isDelete, RolePermissionsDTO::setDelete,
                RolePermissionsDTO::getDeleteId, "DELETE");

        table.getColumns().setAll(moduleCol, viewCol, addCol, updateCol, deleteCol);
    }

    private TableColumn<RolePermissionsDTO, Boolean> createPermissionColumn(
            String title,
            BooleanGetter getter,
            BooleanSetter setter,
            IdGetter idGetter,
            String type) {

        TableColumn<RolePermissionsDTO, Boolean> column = new TableColumn<>();
        column.setPrefWidth(120);
        column.setSortable(false);

        Label header = new Label(title);
        header.getStyleClass().add("clickable-column-header");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        header.setOnMouseClicked(event -> toggleColumn(type));
        column.setGraphic(header);

        column.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(getter.get(data.getValue())));
        column.setCellFactory(col -> new TableCell<RolePermissionsDTO, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                setAlignment(Pos.CENTER);
                checkBox.setOnAction(event -> {
                    RolePermissionsDTO item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        setter.set(item, checkBox.isSelected());
                    }
                });
            }

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                RolePermissionsDTO item = getTableRow().getItem();
                checkBox.setSelected(getter.get(item));
                checkBox.setDisable(roleId == -1 || idGetter.get(item) <= 0);
                setGraphic(checkBox);
            }
        });
        return column;
    }

    private HBox createFooter() {
        saveButton = createButton("Lưu quyền", "primary-button");
        saveButton.setDisable(roleId == -1);
        saveButton.setOnAction(event -> savePermissions());

        Button cancelButton = createButton("Hủy", "secondary-button");
        cancelButton.setOnAction(event -> stage.close());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10, spacer, cancelButton, saveButton);
        footer.getStyleClass().add("role-dialog-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        return footer;
    }

    private void createRole() {
        String roleName = safe(roleNameField.getText()).trim();
        if (roleName.isEmpty()) {
            markInvalid(roleNameField, true);
            showWarning("Thiếu tên role", "Tên role không được để trống.");
            return;
        }
        markInvalid(roleNameField, false);

        try {
            int createdId = roleBUS.addRole(roleName);
            if (createdId == -1) {
                showWarning("Không thể tạo role", "Tên role đã tồn tại hoặc role không thể được tạo.");
                return;
            }

            roleId = createdId;
            roleNameField.setEditable(false);
            roleNameField.getStyleClass().add("readonly-field");
            createRoleButton.setDisable(true);
            saveButton.setDisable(false);
            loadPermissions();
            showInfo("Thành công", "Tạo role thành công. Bạn có thể thiết lập quyền cho role mới.");
        } catch (RuntimeException ex) {
            showError("Không thể tạo role", messageOf(ex));
        }
    }

    private void loadPermissions() {
        try {
            ArrayList<RolePermissionsDTO> data = rolePermissionsBUS.getAll(roleId);
            permissions.setAll(data);
            updateStatusLabel();
            table.refresh();
        } catch (RuntimeException ex) {
            showError("Không thể tải quyền", messageOf(ex));
        }
    }

    private void savePermissions() {
        if (roleId == -1) {
            showWarning("Chưa tạo role", "Bạn cần tạo role trước khi lưu quyền.");
            return;
        }

        boolean success = true;
        try {
            for (RolePermissionsDTO permission : permissions) {
                success &= updatePermission(permission.getViewId(), permission.isView());
                success &= updatePermission(permission.getAddId(), permission.isAdd());
                success &= updatePermission(permission.getEditId(), permission.isEdit());
                success &= updatePermission(permission.getDeleteId(), permission.isDelete());
            }
        } catch (RuntimeException ex) {
            showError("Không thể lưu quyền", messageOf(ex));
            return;
        }

        if (!success) {
            showWarning("Lưu chưa hoàn tất",
                    "Một số quyền không cập nhật được. Vui lòng kiểm tra kết nối dữ liệu và thử lại.");
            return;
        }

        showInfo("Thành công", "Cập nhật quyền thành công.");
        stage.close();
    }

    private boolean updatePermission(int moduleActionId, boolean value) {
        if (moduleActionId <= 0) {
            return true;
        }
        return rolePermissionsBUS.update(roleId, moduleActionId, value);
    }

    private void toggleColumn(String type) {
        if (roleId == -1 || permissions.isEmpty()) {
            return;
        }

        boolean target = hasAnyUnchecked(type);
        for (RolePermissionsDTO item : permissions) {
            switch (type) {
                case "VIEW":
                    if (item.getViewId() > 0) item.setView(target);
                    break;
                case "ADD":
                    if (item.getAddId() > 0) item.setAdd(target);
                    break;
                case "UPDATE":
                    if (item.getEditId() > 0) item.setEdit(target);
                    break;
                case "DELETE":
                    if (item.getDeleteId() > 0) item.setDelete(target);
                    break;
                default:
                    break;
            }
        }
        table.refresh();
    }

    private boolean hasAnyUnchecked(String type) {
        for (RolePermissionsDTO item : permissions) {
            switch (type) {
                case "VIEW":
                    if (item.getViewId() > 0 && !item.isView()) return true;
                    break;
                case "ADD":
                    if (item.getAddId() > 0 && !item.isAdd()) return true;
                    break;
                case "UPDATE":
                    if (item.getEditId() > 0 && !item.isEdit()) return true;
                    break;
                case "DELETE":
                    if (item.getDeleteId() > 0 && !item.isDelete()) return true;
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void installShortcut(Scene scene) {
        KeyCombination toggleAll = new KeyCodeCombination(
                KeyCode.X,
                KeyCombination.CONTROL_DOWN,
                KeyCombination.SHIFT_DOWN);

        scene.getAccelerators().put(toggleAll, () -> {
            if (roleId == -1 || permissions.isEmpty()) {
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText("Thay đổi toàn bộ quyền?");
            confirm.setContentText("Tất cả quyền sẽ được bật nếu còn quyền chưa chọn; nếu đã chọn hết thì sẽ bỏ chọn toàn bộ.");

            Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
            if (!result.isPresent() || result.get() != javafx.scene.control.ButtonType.OK) {
                return;
            }

            boolean allChecked = true;
            for (RolePermissionsDTO item : permissions) {
                if ((item.getViewId() > 0 && !item.isView())
                        || (item.getAddId() > 0 && !item.isAdd())
                        || (item.getEditId() > 0 && !item.isEdit())
                        || (item.getDeleteId() > 0 && !item.isDelete())) {
                    allChecked = false;
                    break;
                }
            }

            boolean target = !allChecked;
            setAllPermissions(target);
        });
    }

    private void setAllPermissions(boolean value) {
        for (RolePermissionsDTO item : permissions) {
            if (item.getViewId() > 0) item.setView(value);
            if (item.getAddId() > 0) item.setAdd(value);
            if (item.getEditId() > 0) item.setEdit(value);
            if (item.getDeleteId() > 0) item.setDelete(value);
        }
        table.refresh();
    }

    private void updateStatusLabel() {
        if (roleId == -1) {
            statusLabel.setText("Chưa tạo role · Chưa có ma trận quyền");
            return;
        }
        statusLabel.setText("Role ID: " + roleId + " · " + permissions.size() + " module");
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("permission-button", styleClass);
        return button;
    }

    private void markInvalid(TextField field, boolean invalid) {
        if (invalid) {
            if (!field.getStyleClass().contains("invalid-field")) {
                field.getStyleClass().add("invalid-field");
            }
        } else {
            field.getStyleClass().remove("invalid-field");
        }
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
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface BooleanGetter {
        boolean get(RolePermissionsDTO item);
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(RolePermissionsDTO item, boolean value);
    }

    @FunctionalInterface
    private interface IdGetter {
        int get(RolePermissionsDTO item);
    }
}
