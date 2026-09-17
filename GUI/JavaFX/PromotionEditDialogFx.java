package GUI.JavaFX;

import Bus.promotionsBUS;
import DTO.promotionsDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/** JavaFX add/edit dialog for promotions. BUS/DAO/DTO are reused unchanged. */
public final class PromotionEditDialogFx {

    private final promotionsDTO promotion;
    private final promotionsBUS promotionBUS;
    private final boolean editMode;
    private boolean saved;

    public PromotionEditDialogFx(promotionsDTO promotion, promotionsBUS promotionBUS) {
        this.promotion = promotion;
        this.promotionBUS = promotionBUS;
        this.editMode = promotion != null && promotion.getPromotion_id() > 0;
    }

    public boolean showAndWait() {
        saved = false;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Sửa khuyến mãi" : "Thêm khuyến mãi");
        dialog.setHeaderText(editMode
                ? "Cập nhật chương trình khuyến mãi #" + promotion.getPromotion_id()
                : "Tạo chương trình khuyến mãi mới");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("promotion-edit-dialog");
        pane.setPrefWidth(610);

        URL css = PromotionEditDialogFx.class.getResource("/GUI/JavaFX/promotion.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        ButtonType saveType = new ButtonType(editMode ? "Lưu thay đổi" : "Thêm khuyến mãi",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelType, saveType);

        TextField idField = new TextField(editMode ? String.valueOf(promotion.getPromotion_id()) : "");
        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");

        TextField nameField = new TextField(editMode ? safe(promotion.getName()) : "");
        nameField.setPromptText("Tên chương trình khuyến mãi");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Giảm hóa đơn", "Giảm giá %", "Tặng sản phẩm");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setValue(editMode ? typeToDisplay(promotion.getPromotion_type()) : "Giảm hóa đơn");

        TextField minPaidField = new TextField(editMode ? numberText(promotion.getMin_paid()) : "0");
        minPaidField.setPromptText("0");

        TextField discountField = new TextField(editMode ? String.valueOf(promotion.getDiscount_on_bill()) : "0");
        discountField.setPromptText("0");

        CheckBox autoCheck = new CheckBox("Tự động áp dụng");
        autoCheck.setSelected(editMode && promotion.isIs_auto());

        CheckBox activeCheck = new CheckBox(editMode ? "Kích hoạt" : "Kích hoạt ngay");
        activeCheck.setSelected(editMode && promotion.isStatus());

        DatePicker startPicker = new DatePicker(editMode && promotion.getStart_at() != null
                ? promotion.getStart_at().toLocalDate()
                : LocalDate.now());
        DatePicker endPicker = new DatePicker(editMode && promotion.getEnd_at() != null
                ? promotion.getEnd_at().toLocalDate()
                : LocalDate.now().plusDays(7));

        Runnable adjustTypeFields = () -> {
            boolean bill = "Giảm hóa đơn".equals(typeBox.getValue());
            minPaidField.setDisable(!bill);
            discountField.setDisable(!bill);
            if (!bill) {
                minPaidField.setText("0");
                discountField.setText("0");
            }
        };
        typeBox.valueProperty().addListener((obs, oldValue, newValue) -> adjustTypeFields.run());
        adjustTypeFields.run();

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(9);
        form.setPadding(new Insets(8, 0, 8, 0));

        int row = 0;
        if (editMode) {
            addField(form, row++, "Mã khuyến mãi", idField);
        }
        addField(form, row++, "Tên khuyến mãi (*)", nameField);
        addField(form, row++, "Loại khuyến mãi", typeBox);
        addField(form, row++, "Giá trị tối thiểu", minPaidField);
        addField(form, row++, "Giảm giá hóa đơn (%)", discountField);

        HBox checkRow = new HBox(24, autoCheck, activeCheck);
        checkRow.getStyleClass().add("promotion-checkbox-row");
        addField(form, row++, "Thiết lập", checkRow);

        HBox dateRow = new HBox(10, new Label("Từ"), startPicker, new Label("Đến"), endPicker);
        dateRow.setFillHeight(true);
        addField(form, row, "Thời gian khuyến mãi", dateRow);

        pane.setContent(form);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-save-button");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String displayType = typeBox.getValue();
            String rawType = displayToType(displayType);
            LocalDate startDate = startPicker.getValue();
            LocalDate endDate = endPicker.getValue();

            if (name.isEmpty()) {
                reject(event, nameField, "Vui lòng nhập tên khuyến mãi.");
                return;
            }
            if (startDate == null || endDate == null) {
                showError("Vui lòng chọn đầy đủ thời gian khuyến mãi.");
                event.consume();
                return;
            }
            if (startDate.isAfter(endDate)) {
                showError("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
                event.consume();
                return;
            }

            double minPaid = 0;
            int billDiscount = 0;
            if ("BILL".equals(rawType)) {
                try {
                    minPaid = Double.parseDouble(minPaidField.getText().trim());
                    billDiscount = Integer.parseInt(discountField.getText().trim());
                } catch (NumberFormatException ex) {
                    showError("Giá trị tối thiểu và giảm giá phải là số.");
                    event.consume();
                    return;
                }
                if (minPaid < 0) {
                    reject(event, minPaidField, "Điều kiện tối thiểu phải từ 0đ trở lên.");
                    return;
                }
                int minDiscount = editMode ? 0 : 1;
                if (billDiscount < minDiscount || billDiscount > 100) {
                    reject(event, discountField,
                            editMode ? "Giảm giá phải từ 0% đến 100%." : "Giảm giá phải từ 1% đến 100%.");
                    return;
                }
            }

            LocalDateTime startAt = startDate.atStartOfDay().with(LocalTime.MIN);
            LocalDateTime endAt = endDate.atTime(LocalTime.MAX);

            promotionsDTO candidate = new promotionsDTO();
            if (editMode) {
                candidate.setPromotion_id(promotion.getPromotion_id());
                candidate.setAvailable_time(promotion.getAvailable_time());
                candidate.setCreated_at(promotion.getCreated_at());
            } else {
                candidate.setCreated_at(LocalDateTime.now());
            }
            candidate.setName(name);
            candidate.setPromotion_type(rawType);
            candidate.setMin_paid(minPaid);
            candidate.setDiscount_on_bill(billDiscount);
            candidate.setIs_auto(autoCheck.isSelected());
            candidate.setStatus(activeCheck.isSelected());
            candidate.setStart_at(startAt);
            candidate.setEnd_at(endAt);
            candidate.setUpdated_at(LocalDateTime.now());

            try {
                boolean success = editMode
                        ? promotionBUS.updatePromotion(candidate)
                        : promotionBUS.addPromotion(candidate);
                if (!success) {
                    showError(editMode ? "Cập nhật khuyến mãi thất bại." : "Thêm khuyến mãi thất bại.");
                    event.consume();
                    return;
                }
                saved = true;
            } catch (RuntimeException ex) {
                showError("Không thể lưu khuyến mãi.\n" + safe(ex.getMessage()));
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
        form.add(field, 1, row);
        GridPane.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);
        if (field instanceof javafx.scene.control.Control) {
            ((javafx.scene.control.Control) field).setMaxWidth(Double.MAX_VALUE);
        }
    }

    private void reject(ActionEvent event, TextField field, String message) {
        field.requestFocus();
        showError(message);
        event.consume();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String displayToType(String display) {
        if ("Giảm giá %".equals(display)) {
            return "PERCENT";
        }
        if ("Tặng sản phẩm".equals(display)) {
            return "GIFT";
        }
        return "BILL";
    }

    private static String typeToDisplay(String type) {
        if ("PERCENT".equals(type)) {
            return "Giảm giá %";
        }
        if ("GIFT".equals(type)) {
            return "Tặng sản phẩm";
        }
        return "Giảm hóa đơn";
    }

    private static String numberText(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
