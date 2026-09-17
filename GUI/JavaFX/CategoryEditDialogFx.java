package GUI.JavaFX;

import Bus.CategoryBUS;
import DTO.CategoryDTO;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;

import java.util.List;
import java.util.Optional;

/** JavaFX dialog for adding/editing product categories. */
public final class CategoryEditDialogFx {

    private CategoryEditDialogFx() {
    }

    public static boolean show(Window owner, CategoryDTO source, CategoryBUS categoryBUS) {
        boolean editMode = source != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Sửa phân loại" : "Thêm phân loại");
        dialog.setHeaderText(editMode
                ? "Cập nhật thông tin phân loại sản phẩm"
                : "Tạo phân loại sản phẩm mới");
        dialog.initModality(Modality.APPLICATION_MODAL);
        // JFXPanel dùng EmbeddedWindow, không phải Stage thật; chỉ gán owner khi an toàn.
        if (owner instanceof Stage) {
            dialog.initOwner(owner);
        }

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("category-edit-dialog");
        URL css = CategoryEditDialogFx.class.getResource("/GUI/JavaFX/category.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }
        pane.setPrefWidth(480);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField idField = new TextField(editMode ? String.valueOf(source.getCategory_id()) : "");
        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");

        TextField nameField = new TextField(editMode ? safe(source.getName()) : "");
        nameField.setPromptText("Ví dụ: Laptop Gaming");

        TextField codeField = new TextField(editMode ? safe(source.getCode()) : "");
        codeField.setPromptText("Ví dụ: CAT-001");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);
        form.setPadding(new Insets(8, 0, 8, 0));

        int row = 0;
        if (editMode) {
            addField(form, row, "ID (không sửa)", idField);
            row += 2;
        }
        addField(form, row, "Mã code (*)", codeField);
        row += 2;
        addField(form, row, "Tên phân loại (*)", nameField);

        codeField.setPrefWidth(400);
        nameField.setPrefWidth(400);
        pane.setContent(form);

        final boolean[] saved = { false };
        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            clearInvalid(codeField, nameField);

            String code = codeField.getText() == null ? "" : codeField.getText().trim();
            String name = nameField.getText() == null ? "" : nameField.getText().trim();

            if (code.isEmpty()) {
                reject(event, codeField, "Vui lòng nhập mã code.");
                return;
            }
            if (name.isEmpty()) {
                reject(event, nameField, "Vui lòng nhập tên phân loại.");
                return;
            }

            int ignoreId = editMode ? source.getCategory_id() : -1;
            List<CategoryDTO> all = categoryBUS.getAll();
            for (CategoryDTO category : all) {
                if (category.getCategory_id() == ignoreId) {
                    continue;
                }
                if (safe(category.getCode()).equalsIgnoreCase(code)) {
                    reject(event, codeField, "Mã code này đã tồn tại.");
                    return;
                }
                if (safe(category.getName()).equalsIgnoreCase(name)) {
                    reject(event, nameField, "Tên phân loại này đã tồn tại.");
                    return;
                }
            }

            CategoryDTO candidate = editMode
                    ? new CategoryDTO(source.getCategory_id(), name, code)
                    : new CategoryDTO(0, name, code);

            boolean success = editMode
                    ? categoryBUS.update(candidate)
                    : categoryBUS.add(candidate);

            if (!success) {
                showError(editMode
                        ? "Cập nhật phân loại thất bại. Vui lòng kiểm tra lại dữ liệu."
                        : "Thêm phân loại thất bại. Vui lòng kiểm tra lại dữ liệu.");
                event.consume();
                return;
            }

            saved[0] = true;
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return saved[0] && result.isPresent() && result.get() == saveType;
    }

    private static void addField(GridPane form, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("dialog-field-label");
        form.add(label, 0, row);
        form.add(field, 0, row + 1);
    }

    private static void reject(ActionEvent event, TextField field, String message) {
        if (!field.getStyleClass().contains("invalid-field")) {
            field.getStyleClass().add("invalid-field");
        }
        field.requestFocus();
        showError(message);
        event.consume();
    }

    private static void clearInvalid(TextField... fields) {
        for (TextField field : fields) {
            field.getStyleClass().remove("invalid-field");
        }
    }

    private static void showError(String message) {
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
