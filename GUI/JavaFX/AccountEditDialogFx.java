package GUI.JavaFX;

import Bus.AccountBUS;
import Bus.EmployeeBUS;
import Bus.RoleBUS;
import DTO.AccountDTO;
import DTO.EmployeeDTO;
import DTO.PasswordUtil;
import DTO.RoleDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.AccountDialog. */
public final class AccountEditDialogFx {

    private final Window owner;
    private final AccountDTO account;
    private final AccountBUS accountBUS;
    private final EmployeeBUS employeeBUS;
    private final RoleBUS roleBUS;
    private final boolean editMode;
    private boolean saved;

    public AccountEditDialogFx(Window owner,
                               AccountDTO account,
                               AccountBUS accountBUS,
                               EmployeeBUS employeeBUS,
                               RoleBUS roleBUS) {
        this.owner = owner;
        this.account = account;
        this.accountBUS = accountBUS;
        this.employeeBUS = employeeBUS;
        this.roleBUS = roleBUS;
        this.editMode = account != null && account.getId() > 0;
    }

    public boolean showAndWait() {
        saved = false;

        Dialog<ButtonType> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(editMode ? "Sửa tài khoản" : "Tạo tài khoản");
        dialog.setHeaderText(editMode
                ? "Cập nhật tài khoản #" + account.getId()
                : "Tạo tài khoản đăng nhập mới");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("account-edit-dialog");
        pane.setPrefWidth(520);

        URL css = AccountEditDialogFx.class.getResource("/GUI/JavaFX/account.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        ButtonType saveType = new ButtonType(editMode ? "Lưu thay đổi" : "Thêm tài khoản",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelType, saveType);

        TextField usernameField = new TextField(editMode ? safe(account.getUsername()) : "");
        usernameField.setPromptText("Tên đăng nhập");
        usernameField.setEditable(!editMode);
        if (editMode) {
            usernameField.getStyleClass().add("readonly-field");
        }

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(editMode ? "Để trống nếu không đổi mật khẩu" : "Mật khẩu");

        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText(passwordField.getPromptText());
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);

        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty());

        CheckBox showPassword = new CheckBox("Hiện mật khẩu");
        showPassword.selectedProperty().addListener((obs, oldValue, visible) -> {
            passwordVisibleField.setVisible(visible);
            passwordVisibleField.setManaged(visible);
            passwordField.setVisible(!visible);
            passwordField.setManaged(!visible);
            if (visible) {
                passwordVisibleField.requestFocus();
                passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
            } else {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }
        });

        HBox passwordBox = new HBox(8, passwordField, passwordVisibleField, showPassword);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(passwordVisibleField, Priority.ALWAYS);

        ComboBox<RoleDTO> roleBox = new ComboBox<>();
        roleBox.setMaxWidth(Double.MAX_VALUE);
        ArrayList<RoleDTO> roles = roleBUS.getAllRoles();
        roleBox.getItems().setAll(roles);

        ComboBox<EmployeeDTO> employeeBox = new ComboBox<>();
        employeeBox.setMaxWidth(Double.MAX_VALUE);
        ArrayList<EmployeeDTO> employees = employeeBUS.getExcept();
        employeeBox.getItems().setAll(employees);

        if (editMode) {
            EmployeeDTO currentEmployee = employeeBUS.getEmployeeById(account.getEmployeeId());
            if (currentEmployee != null && employeeBox.getItems().stream()
                    .noneMatch(item -> sameEmployee(item, currentEmployee))) {
                employeeBox.getItems().add(currentEmployee);
            }
            selectEmployee(employeeBox, account.getEmployeeId());
            selectRole(roleBox, account.getRole_id());
        } else {
            if (!roleBox.getItems().isEmpty()) {
                roleBox.getSelectionModel().selectFirst();
            }
            if (!employeeBox.getItems().isEmpty()) {
                employeeBox.getSelectionModel().selectFirst();
            }
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);
        form.setPadding(new Insets(8, 0, 8, 0));

        addField(form, 0, "Username (*)", usernameField);
        addField(form, 2, editMode ? "Mật khẩu mới" : "Password (*)", passwordBox);
        addField(form, 4, "Vai trò (*)", roleBox);
        addField(form, 6, "Nhân viên (*)", employeeBox);

        usernameField.setPrefWidth(420);
        passwordField.setPrefWidth(285);
        passwordVisibleField.setPrefWidth(285);
        roleBox.setPrefWidth(420);
        employeeBox.setPrefWidth(420);
        pane.setContent(form);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            clearInvalid(usernameField, passwordField, passwordVisibleField);

            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            EmployeeDTO employee = employeeBox.getValue();
            RoleDTO role = roleBox.getValue();

            if (username.isEmpty()) {
                reject(event, usernameField, "Vui lòng nhập username.");
                return;
            }
            if (!editMode && password.isEmpty()) {
                reject(event, passwordField, "Vui lòng nhập mật khẩu.");
                return;
            }
            if (employee == null) {
                showError("Vui lòng chọn nhân viên.");
                employeeBox.requestFocus();
                event.consume();
                return;
            }
            if (role == null) {
                showError("Vui lòng chọn vai trò.");
                roleBox.requestFocus();
                event.consume();
                return;
            }

            if (!editMode) {
                AccountDTO duplicate = accountBUS.findByUserName_2(username);
                if (duplicate != null) {
                    reject(event, usernameField, "Username đã tồn tại. Vui lòng chọn username khác.");
                    return;
                }
            }

            boolean success;
            if (editMode) {
                AccountDTO candidate = new AccountDTO();
                candidate.setId(account.getId());
                candidate.setEmployeeId(employee.getEmployeeId());
                candidate.setRole_id(role.getRoleId());
                candidate.setPassword(password.isEmpty() ? null : PasswordUtil.hashPassword(password));
                candidate.setUpdateAt(LocalDateTime.now());
                success = accountBUS.update(candidate);
            } else {
                AccountDTO candidate = new AccountDTO();
                candidate.setUsername(username);
                candidate.setPassword(PasswordUtil.hashPassword(password));
                candidate.setEmployeeId(employee.getEmployeeId());
                candidate.setRole_id(role.getRoleId());
                candidate.setCreatedAt(LocalDateTime.now());
                candidate.setUpdateAt(LocalDateTime.now());
                candidate.setStatus("1");
                long id = accountBUS.insert(candidate);
                success = id > 0;
            }

            if (!success) {
                showError(editMode ? "Cập nhật tài khoản thất bại." : "Thêm tài khoản thất bại.");
                event.consume();
                return;
            }

            saved = true;
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return saved && result.isPresent() && result.get() == saveType;
    }

    private void addField(GridPane form, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("dialog-field-label");
        form.add(label, 0, row);
        form.add(field, 0, row + 1);
    }

    private void selectEmployee(ComboBox<EmployeeDTO> box, long employeeId) {
        for (EmployeeDTO employee : box.getItems()) {
            if (employee != null && employee.getEmployeeId() != null
                    && employee.getEmployeeId() == employeeId) {
                box.getSelectionModel().select(employee);
                return;
            }
        }
    }

    private void selectRole(ComboBox<RoleDTO> box, int roleId) {
        for (RoleDTO role : box.getItems()) {
            if (role != null && role.getRoleId() == roleId) {
                box.getSelectionModel().select(role);
                return;
            }
        }
    }

    private boolean sameEmployee(EmployeeDTO left, EmployeeDTO right) {
        return left != null && right != null
                && left.getEmployeeId() != null && right.getEmployeeId() != null
                && left.getEmployeeId().equals(right.getEmployeeId());
    }

    private void reject(ActionEvent event, TextField field, String message) {
        markInvalid(field);
        field.requestFocus();
        showError(message);
        event.consume();
    }

    private void clearInvalid(TextField... fields) {
        for (TextField field : fields) {
            field.getStyleClass().remove("invalid-field");
        }
    }

    private void markInvalid(TextField field) {
        if (!field.getStyleClass().contains("invalid-field")) {
            field.getStyleClass().add("invalid-field");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
