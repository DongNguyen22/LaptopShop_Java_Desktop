package GUI.JavaFX;

import Bus.promotionsBUS;
import DAO.SanPhamDAO;
import DTO.promotionItemsDTO;
import DTO.promotionsDTO;
import DTO.sanPham;
import javafx.event.ActionEvent;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** JavaFX replacement for GUI.Dialog.AddEditPromotionItemDialog. */
public final class PromotionItemEditDialogFx {

    private final Window owner;
    private final int promotionId;
    private final promotionItemsDTO item;
    private final promotionsBUS promotionBUS;
    private final boolean editMode;
    private final SanPhamDAO productDAO = new SanPhamDAO();

    private String promotionType = "";
    private boolean saved;

    public PromotionItemEditDialogFx(Window owner, int promotionId, promotionItemsDTO item, promotionsBUS promotionBUS) {
        this.owner = owner;
        this.promotionId = promotionId;
        this.item = item;
        this.promotionBUS = promotionBUS;
        this.editMode = item != null;
        loadPromotionType();
    }

    public boolean showAndWait() {
        saved = false;

        Dialog<ButtonType> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(editMode ? "Sửa sản phẩm khuyến mãi" : "Thêm sản phẩm khuyến mãi");
        dialog.setHeaderText(editMode
                ? "Cập nhật điều kiện áp dụng cho sản phẩm"
                : "Chọn sản phẩm áp dụng khuyến mãi");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("promotion-item-edit-dialog");
        pane.setPrefWidth(540);

        URL css = PromotionItemEditDialogFx.class.getResource("/GUI/JavaFX/promotion.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        ButtonType saveType = new ButtonType(editMode ? "Lưu thay đổi" : "Thêm sản phẩm",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelType, saveType);

        ComboBox<sanPham> productBox = new ComboBox<>();
        productBox.setMaxWidth(Double.MAX_VALUE);
        configureProductCellFactory(productBox, "--- Chọn sản phẩm ---");
        loadProducts(productBox);

        TextField discountField = new TextField();
        discountField.setPromptText("1 - 99");

        ComboBox<sanPham> giftBox = new ComboBox<>();
        giftBox.setMaxWidth(Double.MAX_VALUE);
        configureProductCellFactory(giftBox, "--- Không tặng ---");
        loadGiftedProducts(giftBox);

        if (editMode) {
            ensureProductPresent(productBox, item.getProduct_id());
            selectProduct(productBox, item.getProduct_id());
            productBox.setDisable(true);
            discountField.setText(String.valueOf(item.getDiscount_percent()));
            if (item.getGifted_product_id() != null) {
                selectProduct(giftBox, item.getGifted_product_id());
            }
        }

        if ("PERCENT".equals(promotionType)) {
            giftBox.setDisable(true);
            giftBox.setValue(null);
            if (!editMode) {
                discountField.setText("");
            }
        } else if ("GIFT".equals(promotionType)) {
            discountField.setDisable(true);
            discountField.setText("0");
        } else {
            productBox.setDisable(true);
            discountField.setDisable(true);
            giftBox.setDisable(true);
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(8, 0, 8, 0));
        addField(form, 0, "Sản phẩm (*)", productBox);
        addField(form, 1, "Giảm giá (%)", discountField);
        addField(form, 2, "Sản phẩm tặng kèm", giftBox);

        Label typeHint = new Label("Loại khuyến mãi: " + typeToDisplay(promotionType));
        typeHint.getStyleClass().add("promotion-item-type-hint");
        form.add(typeHint, 1, 3);
        pane.setContent(form);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            sanPham selectedProduct = productBox.getValue();
            if (selectedProduct == null) {
                showError("Vui lòng chọn sản phẩm.");
                productBox.requestFocus();
                event.consume();
                return;
            }

            if (!"PERCENT".equals(promotionType) && !"GIFT".equals(promotionType)) {
                showError("Loại khuyến mãi này không hỗ trợ quản lý sản phẩm.");
                event.consume();
                return;
            }

            int discount = 0;
            if ("PERCENT".equals(promotionType)) {
                try {
                    discount = Integer.parseInt(discountField.getText().trim());
                } catch (NumberFormatException ex) {
                    showError("Giảm giá phải là số nguyên từ 1% đến 99%.");
                    discountField.requestFocus();
                    event.consume();
                    return;
                }
                if (discount < 1 || discount > 99) {
                    showError("Giảm giá phải là số nguyên từ 1% đến 99%.");
                    discountField.requestFocus();
                    event.consume();
                    return;
                }
            }

            Long giftedProductId = null;
            if ("GIFT".equals(promotionType)) {
                sanPham gift = giftBox.getValue();
                if (gift == null) {
                    showError("Vui lòng chọn sản phẩm tặng kèm.");
                    giftBox.requestFocus();
                    event.consume();
                    return;
                }
                giftedProductId = parseProductId(gift);
            }

            long productId = parseProductId(selectedProduct);
            if (!editMode && promotionBUS.isProductInActivePromotion(productId, promotionId)) {
                showError("Sản phẩm '" + safe(selectedProduct.getTenSP())
                        + "' đang được áp dụng trong một chương trình khuyến mãi đang chạy khác.\n"
                        + "Một sản phẩm chỉ được áp dụng cho một khuyến mãi đang chạy.");
                event.consume();
                return;
            }

            promotionItemsDTO candidate = new promotionItemsDTO();
            candidate.setPromotion_id(promotionId);
            candidate.setProduct_id(productId);
            candidate.setDiscount_percent(discount);
            candidate.setGifted_product_id(giftedProductId);

            try {
                boolean success = editMode
                        ? promotionBUS.updatePromotionItem(candidate)
                        : promotionBUS.addPromotionItem(candidate);
                if (!success) {
                    showError(editMode
                            ? "Cập nhật sản phẩm khuyến mãi thất bại."
                            : "Thêm sản phẩm thất bại. Có thể sản phẩm đã tồn tại trong khuyến mãi.");
                    event.consume();
                    return;
                }
                saved = true;
            } catch (RuntimeException ex) {
                showError("Không thể lưu sản phẩm khuyến mãi.\n" + safe(ex.getMessage()));
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return saved && result.isPresent() && result.get() == saveType;
    }

    private void loadPromotionType() {
        promotionsDTO promotion = promotionBUS.getPromotionById(promotionId);
        if (promotion != null) {
            promotionType = safe(promotion.getPromotion_type());
        }
    }

    private void loadProducts(ComboBox<sanPham> productBox) {
        List<sanPham> products = productDAO.getActiveProducts();
        Set<Long> existingProductIds = new HashSet<>();
        if (!editMode) {
            ArrayList<promotionItemsDTO> existingItems = promotionBUS.getPromotionItems(promotionId);
            if (existingItems != null) {
                for (promotionItemsDTO existing : existingItems) {
                    existingProductIds.add(existing.getProduct_id());
                }
            }
        }

        if (products != null) {
            for (sanPham product : products) {
                long id = parseProductId(product);
                if (!existingProductIds.contains(id)) {
                    productBox.getItems().add(product);
                }
            }
        }
    }

    private void loadGiftedProducts(ComboBox<sanPham> giftBox) {
        List<sanPham> products = productDAO.getActiveProducts();
        if (products != null) {
            giftBox.getItems().addAll(products);
        }
    }

    private void ensureProductPresent(ComboBox<sanPham> box, long productId) {
        for (sanPham product : box.getItems()) {
            if (product != null && parseProductId(product) == productId) {
                return;
            }
        }
        List<sanPham> products = productDAO.getActiveProducts();
        if (products != null) {
            for (sanPham product : products) {
                if (parseProductId(product) == productId) {
                    box.getItems().add(product);
                    return;
                }
            }
        }
    }

    private void selectProduct(ComboBox<sanPham> box, long productId) {
        for (sanPham product : box.getItems()) {
            if (product != null && parseProductId(product) == productId) {
                box.setValue(product);
                return;
            }
        }
    }

    private void configureProductCellFactory(ComboBox<sanPham> box, String nullText) {
        box.setCellFactory(list -> new ListCell<sanPham>() {
            @Override
            protected void updateItem(sanPham product, boolean empty) {
                super.updateItem(product, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(product == null ? nullText : safe(product.getTenSP()));
                }
            }
        });
        box.setButtonCell(new ListCell<sanPham>() {
            @Override
            protected void updateItem(sanPham product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty || product == null ? nullText : safe(product.getTenSP()));
            }
        });
    }

    private void addField(GridPane form, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("dialog-field-label");
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);
        if (field instanceof javafx.scene.control.Control) {
            ((javafx.scene.control.Control) field).setMaxWidth(Double.MAX_VALUE);
        }
    }

    private long parseProductId(sanPham product) {
        return Long.parseLong(product.getMaSP());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String typeToDisplay(String type) {
        if ("PERCENT".equals(type)) {
            return "Giảm giá %";
        }
        if ("GIFT".equals(type)) {
            return "Tặng sản phẩm";
        }
        if ("BILL".equals(type)) {
            return "Giảm hóa đơn";
        }
        return type;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
