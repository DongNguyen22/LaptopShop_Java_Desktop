package GUI.JavaFX;

import Bus.nhaCungCapBUS;
import DTO.nhaCungCapDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;

/**
 * JavaFX dialog for adding/editing suppliers.
 * Reuses nhaCungCapBUS/DAO/DTO without changing business/data layers.
 */
public final class SupplierEditDialogFx {

    private final nhaCungCapDTO supplier;
    private final nhaCungCapBUS supplierBUS;
    private final boolean editMode;
    private boolean saved;

    public SupplierEditDialogFx(nhaCungCapDTO supplier, nhaCungCapBUS supplierBUS) {
        this.supplier = supplier;
        this.supplierBUS = supplierBUS;
        this.editMode = supplier != null && supplier.getSupplier_id() > 0;
    }

    public boolean showAndWait() {
        saved = false;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Sửa nhà cung cấp" : "Thêm nhà cung cấp");
        dialog.setHeaderText(editMode
                ? "Cập nhật thông tin nhà cung cấp #" + supplier.getSupplier_id()
                : "Nhập thông tin nhà cung cấp mới");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("supplier-edit-dialog");
        pane.setPrefWidth(500);

        URL css = SupplierEditDialogFx.class.getResource("/GUI/JavaFX/supplier.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        ButtonType saveType = new ButtonType(editMode ? "Lưu thay đổi" : "Thêm nhà cung cấp",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelType, saveType);

        TextField idField = new TextField(editMode ? String.valueOf(supplier.getSupplier_id()) : "");
        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");

        TextField codeField = new TextField(editMode ? safe(supplier.getCode()) : "");
        codeField.setPromptText("Ví dụ: NCC-001");

        TextField nameField = new TextField(editMode ? safe(supplier.getName()) : "");
        nameField.setPromptText("Ví dụ: Công ty ABC");

        TextField phoneField = new TextField(editMode ? safe(supplier.getPhone_number()) : "");
        phoneField.setPromptText("Ví dụ: 0901234567");

        TextField addressField = new TextField(editMode ? safe(supplier.getAddress()) : "");
        addressField.setPromptText("Ví dụ: 123 Đường XYZ");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);
        form.setPadding(new Insets(8, 0, 8, 0));

        int row = 0;
        if (editMode) {
            addField(form, row, "ID (không sửa)", idField);
            row += 2;
        }
        addField(form, row, "Mã nhà cung cấp (*)", codeField);
        row += 2;
        addField(form, row, "Tên nhà cung cấp (*)", nameField);
        row += 2;
        addField(form, row, "Số điện thoại (*)", phoneField);
        row += 2;
        addField(form, row, "Địa chỉ", addressField);

        codeField.setPrefWidth(400);
        nameField.setPrefWidth(400);
        phoneField.setPrefWidth(400);
        addressField.setPrefWidth(400);
        pane.setContent(form);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            clearInvalid(codeField, nameField, phoneField, addressField);

            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty()) {
                reject(event, nameField, "Vui lòng nhập tên nhà cung cấp.");
                return;
            }
            if (code.isEmpty()) {
                reject(event, codeField, "Vui lòng nhập mã nhà cung cấp.");
                return;
            }
            if (phone.isEmpty()) {
                reject(event, phoneField, "Vui lòng nhập số điện thoại.");
                return;
            }
            if (!phone.matches("0\\d{9}")) {
                reject(event, phoneField,
                        "Số điện thoại không hợp lệ. Phải gồm 10 chữ số và bắt đầu bằng 0.");
                return;
            }

            String oldCode = editMode ? safe(supplier.getCode()) : "";
            String oldPhone = editMode ? safe(supplier.getPhone_number()) : "";

            try {
                if ((!editMode || !code.equals(oldCode)) && supplierBUS.isSupplierCodeDuplicate(code)) {
                    reject(event, codeField, "Mã nhà cung cấp đã tồn tại. Vui lòng nhập mã khác.");
                    return;
                }

                if ((!editMode || !phone.equals(oldPhone)) && supplierBUS.isPhoneNumberExist(phone)) {
                    reject(event, phoneField, "Số điện thoại đã tồn tại. Vui lòng nhập số khác.");
                    return;
                }

                nhaCungCapDTO candidate = editMode
                        ? new nhaCungCapDTO(supplier.getSupplier_id(), code, name, phone, address)
                        : new nhaCungCapDTO(code, name, phone, address.isEmpty() ? null : address);

                boolean success = editMode
                        ? supplierBUS.updateNhaCungCap(candidate)
                        : supplierBUS.addNhaCungCap(candidate);

                if (!success) {
                    showError(editMode
                            ? "Cập nhật nhà cung cấp thất bại. Vui lòng kiểm tra lại dữ liệu."
                            : "Thêm nhà cung cấp thất bại. Vui lòng kiểm tra lại dữ liệu.");
                    event.consume();
                    return;
                }

                saved = true;
            } catch (RuntimeException ex) {
                showError("Lỗi hệ thống: " + safe(ex.getMessage()));
                event.consume();
            }
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
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
