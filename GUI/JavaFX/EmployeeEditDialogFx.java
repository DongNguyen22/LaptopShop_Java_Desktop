package GUI.JavaFX;

import Bus.EmployeeBUS;
import DTO.EmployeeDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import valiateData.validationResult;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.EmployeeDialog. Supports add and edit. */
public final class EmployeeEditDialogFx {

    private final EmployeeDTO employee;
    private final EmployeeBUS employeeBUS;
    private final boolean editMode;
    private boolean saved;

    public EmployeeEditDialogFx(EmployeeDTO employee, EmployeeBUS employeeBUS) {
        this.employee = employee;
        this.employeeBUS = employeeBUS;
        this.editMode = employee != null && employee.getEmployeeId() != null;
    }

    public boolean showAndWait() {
        saved = false;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Sửa nhân viên" : "Thêm nhân viên");
        dialog.setHeaderText(editMode
                ? "Cập nhật thông tin nhân viên #" + employee.getEmployeeId()
                : "Nhập thông tin nhân viên mới");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("employee-edit-dialog");
        pane.setPrefWidth(460);

        URL css = EmployeeEditDialogFx.class.getResource("/GUI/JavaFX/employee.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        ButtonType saveType = new ButtonType(editMode ? "Lưu thay đổi" : "Thêm nhân viên",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelType, saveType);

        TextField nameField = new TextField(editMode ? safe(employee.getEmployeeName()) : "");
        nameField.setPromptText("Nhập họ và tên");

        TextField phoneField = new TextField(editMode ? safe(employee.getPhoneNumber()) : "");
        phoneField.setPromptText("Ví dụ: 0901234567");

        TextField salaryField = new TextField(editMode && employee.getSalary() != null
                ? employee.getSalary().toPlainString()
                : "");
        salaryField.setPromptText("Nhập lương");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ACTIVE", "INACTIVE");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setValue(editMode && employee.getStatus() != null ? employee.getStatus().toUpperCase() : "ACTIVE");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);
        form.setPadding(new Insets(8, 0, 8, 0));

        addField(form, 0, "Họ & Tên", nameField);
        addField(form, 2, "Số điện thoại", phoneField);
        addField(form, 4, "Lương", salaryField);
        addField(form, 6, "Trạng thái", statusBox);

        nameField.setPrefWidth(380);
        phoneField.setPrefWidth(380);
        salaryField.setPrefWidth(380);
        pane.setContent(form);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            clearInvalid(nameField, phoneField, salaryField);

            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String salaryText = salaryField.getText().trim();
            String status = statusBox.getValue();

            BigDecimal salary;
            try {
                salary = new BigDecimal(salaryText);
            } catch (NumberFormatException ex) {
                markInvalid(salaryField);
                showValidationError("Lương không hợp lệ. Vui lòng nhập số.");
                salaryField.requestFocus();
                event.consume();
                return;
            }

            EmployeeDTO candidate = new EmployeeDTO();
            if (editMode) {
                candidate.setEmployeeId(employee.getEmployeeId());
                candidate.setCreatedAt(employee.getCreatedAt());
                candidate.setUpdatedAt(employee.getUpdatedAt());
            }
            candidate.setEmployeeName(name);
            candidate.setPhoneNumber(phone);
            candidate.setSalary(salary);
            candidate.setStatus(status);

            validationResult validation = employeeBUS.validate(candidate);
            if (!validation.isValid()) {
                String code = validation.getMessage();
                if ("name".equals(code)) {
                    markInvalid(nameField);
                    nameField.requestFocus();
                    showValidationError("Tên nhân viên không hợp lệ.");
                } else if ("phone".equals(code)) {
                    markInvalid(phoneField);
                    phoneField.requestFocus();
                    showValidationError("Số điện thoại không hợp lệ.");
                } else if ("salary".equals(code)) {
                    markInvalid(salaryField);
                    salaryField.requestFocus();
                    showValidationError("Lương không hợp lệ.");
                } else {
                    showValidationError(code == null || code.isBlank() ? "Dữ liệu không hợp lệ." : code);
                }
                event.consume();
                return;
            }

            boolean success = editMode
                    ? employeeBUS.updateEmployee(candidate)
                    : employeeBUS.addEmployee(candidate);

            if (!success) {
                showValidationError(editMode
                        ? "Cập nhật nhân viên thất bại."
                        : "Thêm nhân viên thất bại.");
                event.consume();
                return;
            }

            saved = true;
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return saved && result.isPresent() && result.get() == saveType;
    }

    private void addField(GridPane form, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("dialog-field-label");
        form.add(label, 0, row);
        form.add(field, 0, row + 1);
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

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
