package GUI.JavaFX;

import Connector.JDBCUTIL;
import DAO.SanPhamDAO;
import DTO.sanPham;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import java.sql.ResultSet;
import java.util.Optional;

/** JavaFX replacement for ThemSanPhamDialog and SuaSanPhamDialog. */
public final class ProductEditDialogFx {

    private final SanPhamDAO productDAO = new SanPhamDAO();
    private final sanPham product;
    private final boolean editMode;
    private boolean saved;

    private final TextField idField = new TextField();
    private final ComboBox<CategoryOption> categoryBox = new ComboBox<>();
    private final TextField codeField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField profitField = new TextField();
    private final TextField priceField = new TextField();
    private final Label stockLabel = new Label("0");

    public ProductEditDialogFx(sanPham product) {
        this.product = product;
        this.editMode = product != null;
    }

    public boolean showAndWait(Window owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(editMode ? "Sửa sản phẩm" : "Thêm sản phẩm");
        dialog.setHeaderText(editMode
                ? "Cập nhật thông tin sản phẩm"
                : "Nhập thông tin sản phẩm mới");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("product-edit-dialog");
        dialogPane.setPrefWidth(520);

        ButtonType saveType = new ButtonType(editMode ? "Lưu thay đổi" : "Thêm sản phẩm", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        loadCategories();
        configureFields();
        populateData();

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(7);
        form.setPadding(new Insets(8, 4, 4, 4));
        form.getColumnConstraints().clear();

        addField(form, 0, "ID sản phẩm", idField);
        addField(form, 2, "Danh mục", categoryBox);
        addField(form, 4, "Mã Code sản phẩm", codeField);
        addField(form, 6, "Tên sản phẩm", nameField);
        addField(form, 8, "Lợi nhuận", profitField);
        addField(form, 10, "Đơn giá", priceField);
        addField(form, 12, "Tồn kho", stockLabel);

        dialogPane.setContent(form);

        Node saveButton = dialogPane.lookupButton(saveType);
        if (saveButton instanceof Button) {
            ((Button) saveButton).getStyleClass().add("dialog-save-button");
        }

        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            clearInvalid(codeField, nameField, profitField, priceField);

            String code = codeField.getText() == null ? "" : codeField.getText().trim();
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String profitText = profitField.getText() == null ? "" : profitField.getText().trim();
            String priceText = priceField.getText() == null ? "" : priceField.getText().trim();
            CategoryOption category = categoryBox.getValue();

            if (category == null) {
                showValidationError("Vui lòng chọn danh mục sản phẩm.");
                categoryBox.requestFocus();
                event.consume();
                return;
            }
            if (code.isEmpty()) {
                markInvalid(codeField);
                showValidationError("Vui lòng nhập mã Code sản phẩm.");
                codeField.requestFocus();
                event.consume();
                return;
            }
            if (name.isEmpty()) {
                markInvalid(nameField);
                showValidationError("Vui lòng nhập tên sản phẩm.");
                nameField.requestFocus();
                event.consume();
                return;
            }
            if (isCodeDuplicate(code)) {
                markInvalid(codeField);
                showValidationError("Mã Code sản phẩm đã tồn tại.");
                codeField.requestFocus();
                event.consume();
                return;
            }

            double profit;
            double price;
            try {
                profit = Double.parseDouble(profitText.replace(",", "."));
            } catch (NumberFormatException ex) {
                markInvalid(profitField);
                showValidationError("Lợi nhuận phải là số.");
                profitField.requestFocus();
                event.consume();
                return;
            }
            try {
                price = Double.parseDouble(priceText.replace(",", "."));
            } catch (NumberFormatException ex) {
                markInvalid(priceField);
                showValidationError("Đơn giá phải là số.");
                priceField.requestFocus();
                event.consume();
                return;
            }
            if (profit < 0) {
                markInvalid(profitField);
                showValidationError("Lợi nhuận không được nhỏ hơn 0.");
                event.consume();
                return;
            }
            if (price < 0) {
                markInvalid(priceField);
                showValidationError("Đơn giá không được nhỏ hơn 0.");
                event.consume();
                return;
            }

            sanPham candidate = new sanPham();
            if (editMode) {
                candidate.setMaSP(product.getMaSP());
            } else {
                candidate.setMaSP(idField.getText());
            }
            candidate.setCode(code);
            candidate.setTenSP(name);
            candidate.setDanhMucId(category.id);
            candidate.setLoiNhuan(profit);
            candidate.setDonGia(price);

            try {
                if (editMode) {
                    productDAO.update(candidate);
                    if (!verifyUpdatedProduct(candidate)) {
                        throw new IllegalStateException("Dữ liệu chưa được cập nhật trong cơ sở dữ liệu.");
                    }
                } else {
                    productDAO.insert(candidate);
                    if (!productExistsByCode(code)) {
                        throw new IllegalStateException("Sản phẩm chưa được tạo trong cơ sở dữ liệu.");
                    }
                    // SanPhamDAO.insert intentionally remains unchanged and does not persist sell_price.
                    // Keep the DAO untouched, but preserve the Add Product form's entered price here.
                    updateInitialSellPrice(code, price);
                }
                saved = true;
            } catch (Exception ex) {
                showValidationError("Không thể lưu sản phẩm: " + ex.getMessage());
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return saved && result.isPresent() && result.get() == saveType;
    }

    private void configureFields() {
        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        codeField.setPromptText("VD: IP15PM");
        nameField.setPromptText("Tên sản phẩm");
        profitField.setPromptText("VD: 0.20 hoặc 20");
        priceField.setPromptText("VD: 29000000");
        stockLabel.getStyleClass().add("stock-value");
    }

    private void populateData() {
        if (!editMode) {
            idField.setText(generateProductId());
            stockLabel.setText("0");
            if (!categoryBox.getItems().isEmpty()) {
                categoryBox.getSelectionModel().selectFirst();
            }
            return;
        }

        sanPham fresh = productDAO.getById(product.getMaSP());
        sanPham source = fresh != null ? fresh : product;

        idField.setText(safe(source.getMaSP()));
        codeField.setText(safe(source.getCode()));
        nameField.setText(safe(source.getTenSP()));
        profitField.setText(String.valueOf(source.getLoiNhuan()));
        priceField.setText(String.valueOf(source.getDonGia()));
        stockLabel.setText(String.valueOf(source.getSoLuong()));

        for (CategoryOption option : categoryBox.getItems()) {
            if (option.id == source.getDanhMucId()
                    || option.name.equalsIgnoreCase(safe(source.getTenDanhmuc()))) {
                categoryBox.setValue(option);
                break;
            }
        }
    }

    private void loadCategories() {
        String sql = "SELECT category_id, name FROM categories ORDER BY name";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                categoryBox.getItems().add(new CategoryOption(rs.getInt("category_id"), rs.getString("name")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateProductId() {
        String sql = "SELECT MAX(product_id) AS maxId FROM products";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return String.valueOf(rs.getInt("maxId") + 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Tự động";
    }

    private boolean isCodeDuplicate(String code) {
        String sql = editMode
                ? "SELECT COUNT(*) AS cnt FROM products WHERE product_code = ? AND product_id <> ?"
                : "SELECT COUNT(*) AS cnt FROM products WHERE product_code = ?";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            if (editMode) {
                statement.setString(2, product.getMaSP());
            }
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt("cnt") > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean productExistsByCode(String code) {
        String sql = "SELECT COUNT(*) AS cnt FROM products WHERE product_code = ?";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt("cnt") > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean verifyUpdatedProduct(sanPham expected) {
        sanPham actual = productDAO.getById(expected.getMaSP());
        if (actual == null) {
            return false;
        }
        return safe(actual.getCode()).equals(expected.getCode())
                && safe(actual.getTenSP()).equals(expected.getTenSP())
                && actual.getDanhMucId() == expected.getDanhMucId()
                && Math.abs(actual.getLoiNhuan() - expected.getLoiNhuan()) < 0.000001
                && Math.abs(actual.getDonGia() - expected.getDonGia()) < 0.01;
    }

    private void updateInitialSellPrice(String code, double price) {
        String sql = "UPDATE products SET sell_price = ? WHERE product_code = ?";
        try (var connection = JDBCUTIL.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, price);
            statement.setString(2, code);
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addField(GridPane form, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("dialog-field-label");
        form.add(label, 0, row);
        form.add(field, 0, row + 1);
        GridPane.setHgrow(field, Priority.ALWAYS);
        if (field instanceof TextField) {
            ((TextField) field).setMaxWidth(Double.MAX_VALUE);
        }
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

    private static final class CategoryOption {
        private final int id;
        private final String name;

        private CategoryOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
