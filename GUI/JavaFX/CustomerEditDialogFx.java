package GUI.JavaFX;

import Bus.CustomerBUS;
import DTO.CustomerDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import valiateData.validationResult;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.EditCustomerDialog. */
public final class CustomerEditDialogFx {

    private final CustomerDTO customer;
    private final CustomerBUS customerBUS;
    private boolean updated;

    public CustomerEditDialogFx(CustomerDTO customer, CustomerBUS customerBUS) {
        this.customer = customer;
        this.customerBUS = customerBUS;
    }

    public boolean showAndWait() {
        updated = false;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa khách hàng");
        dialog.setHeaderText("Cập nhật thông tin khách hàng #" + customer.getCustomerId());

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("customer-edit-dialog");
        pane.setPrefWidth(430);

        URL css = CustomerEditDialogFx.class.getResource("/GUI/JavaFX/customer.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        ButtonType saveType = new ButtonType("Lưu thay đổi", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelType, saveType);

        TextField nameField = new TextField(safe(customer.getCustomerName()));
        nameField.setPromptText("Nhập tên khách hàng");

        TextField phoneField = new TextField(safe(customer.getPhoneNumber()));
        phoneField.setPromptText("Nhập số điện thoại");

        Label nameLabel = new Label("Tên khách hàng");
        nameLabel.getStyleClass().add("dialog-field-label");
        Label phoneLabel = new Label("Số điện thoại");
        phoneLabel.getStyleClass().add("dialog-field-label");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);
        form.setPadding(new Insets(8, 0, 8, 0));
        form.add(nameLabel, 0, 0);
        form.add(nameField, 0, 1);
        form.add(phoneLabel, 0, 2);
        form.add(phoneField, 0, 3);
        nameField.setPrefWidth(360);
        phoneField.setPrefWidth(360);
        pane.setContent(form);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty()) {
                showValidationError("Tên khách hàng không được để trống.");
                nameField.requestFocus();
                event.consume();
                return;
            }

            if (phone.isEmpty()) {
                showValidationError("Số điện thoại không được để trống.");
                phoneField.requestFocus();
                event.consume();
                return;
            }

            validationResult validation = customerBUS.validateSDT(phone);
            if (!validation.isValid()) {
                showValidationError(validation.getMessage());
                phoneField.requestFocus();
                event.consume();
                return;
            }

            String oldName = customer.getCustomerName();
            String oldPhone = customer.getPhoneNumber();
            LocalDateTime oldUpdatedAt = customer.getUpdatedAt();

            customer.setCustomerName(name);
            customer.setPhoneNumber(phone);
            customer.setUpdatedAt(LocalDateTime.now());

            boolean success = customerBUS.insert(customer);
            if (!success) {
                // Keep the in-memory object consistent with the database when
                // the existing BUS/DAO reports an update failure.
                customer.setCustomerName(oldName);
                customer.setPhoneNumber(oldPhone);
                customer.setUpdatedAt(oldUpdatedAt);
                showValidationError("Cập nhật khách hàng thất bại.");
                event.consume();
                return;
            }

            updated = true;
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return updated && result.isPresent() && result.get() == saveType;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? "Dữ liệu không hợp lệ." : message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
