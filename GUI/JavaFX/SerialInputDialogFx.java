package GUI.JavaFX;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** JavaFX replacement for GUI.Dialog.SerialInputDialog. */
public final class SerialInputDialogFx {

    private final Stage stage = new Stage();
    private final TableView<SerialRow> table = new TableView<>();
    private final ObservableList<SerialRow> rows = FXCollections.observableArrayList();
    private final List<String> originalSerials;
    private List<String> resultSerials;

    private final TextField formatField = new TextField("SN-@@@");
    private final TextField startField = new TextField("001");
    private final TextField endField = new TextField("010");
    private final Spinner<Integer> stepSpinner = new Spinner<>();

    public SerialInputDialogFx(Window owner, List<String> existingSerials) {
        this.originalSerials = new ArrayList<>(existingSerials == null ? List.of() : existingSerials);
        this.resultSerials = new ArrayList<>(originalSerials);
        configureStage(owner);
        buildView();
        populateRows();
    }

    public List<String> showAndWait() {
        stage.showAndWait();
        return new ArrayList<>(resultSerials);
    }

    private void configureStage(Window owner) {
        stage.setTitle("Nhập Serial");
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setWidth(650);
        stage.setHeight(610);
        stage.setMinWidth(560);
        stage.setMinHeight(480);
        stage.setOnCloseRequest(event -> resultSerials = new ArrayList<>(originalSerials));
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("receipt-dialog-root");
        root.setPadding(new Insets(18));
        root.setTop(createBulkPanel());
        root.setCenter(createTableCard());
        root.setBottom(createButtons());

        Scene scene = new Scene(root);
        var css = getClass().getResource("/GUI/JavaFX/receipt.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
    }

    private VBox createBulkPanel() {
        Label title = new Label("Tạo serial hàng loạt");
        title.getStyleClass().add("dialog-section-title");

        formatField.setPromptText("Ví dụ: SN-@@@");
        startField.setPromptText("001");
        endField.setPromptText("010");
        stepSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        stepSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Định dạng (@ = phần số)"), 0, 0);
        grid.add(formatField, 1, 0, 3, 1);
        grid.add(new Label("Số bắt đầu"), 0, 1);
        grid.add(startField, 1, 1);
        grid.add(new Label("Số kết thúc"), 2, 1);
        grid.add(endField, 3, 1);
        grid.add(new Label("Bước nhảy"), 0, 2);
        grid.add(stepSpinner, 1, 2);

        Button generateButton = button("Tạo serial hàng loạt", "primary-button", this::generateBulkSerials);
        HBox action = new HBox(generateButton);
        action.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, title, grid, action);
        box.getStyleClass().add("receipt-card");
        box.setPadding(new Insets(14));
        BorderPane.setMargin(box, new Insets(0, 0, 12, 0));
        return box;
    }

    private VBox createTableCard() {
        configureTable();
        Button addRowButton = button("+ Thêm dòng", "secondary-button", this::addRow);
        Button deleteRowButton = button("Xóa dòng", "danger-button", this::deleteRow);
        HBox bar = new HBox(8, addRowButton, deleteRowButton);
        VBox box = new VBox(8, bar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        box.getStyleClass().add("receipt-card");
        box.setPadding(new Insets(12));
        return box;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(rows);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Chưa có serial."));

        TableColumn<SerialRow, Number> indexCol = new TableColumn<>("STT");
        indexCol.setCellValueFactory(data -> data.getValue().indexProperty());
        indexCol.setPrefWidth(70);
        indexCol.setEditable(false);

        TableColumn<SerialRow, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> data.getValue().serialProperty());
        serialCol.setCellFactory(TextFieldTableCell.forTableColumn());
        serialCol.setOnEditCommit(event -> event.getRowValue().setSerial(event.getNewValue()));
        serialCol.setPrefWidth(480);

        table.getColumns().setAll(indexCol, serialCol);
    }

    private HBox createButtons() {
        Button saveButton = button("Lưu", "success-button", this::saveSerials);
        Button cancelButton = button("Hủy", "secondary-button", () -> {
            resultSerials = new ArrayList<>(originalSerials);
            stage.close();
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(8, spacer, saveButton, cancelButton);
        bar.setPadding(new Insets(14, 0, 0, 0));
        return bar;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("receipt-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void populateRows() {
        rows.clear();
        for (String serial : originalSerials) {
            if (serial != null && !serial.trim().isEmpty()) {
                rows.add(new SerialRow(rows.size() + 1, serial.trim()));
            }
        }
        if (rows.isEmpty()) addRow();
    }

    private void addRow() {
        rows.add(new SerialRow(rows.size() + 1, ""));
        table.getSelectionModel().selectLast();
        table.scrollTo(rows.size() - 1);
    }

    private void deleteRow() {
        SerialRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        rows.remove(selected);
        reindex();
        if (rows.isEmpty()) addRow();
    }

    private void generateBulkSerials() {
        String format = formatField.getText().trim();
        String startText = startField.getText().trim();
        String endText = endField.getText().trim();
        if (format.isEmpty() || startText.isEmpty() || endText.isEmpty()) {
            error("Vui lòng nhập đầy đủ định dạng, số bắt đầu và kết thúc.");
            return;
        }

        int start;
        int end;
        int step;
        try {
            start = Integer.parseInt(startText);
            end = Integer.parseInt(endText);
            step = stepSpinner.getValue();
        } catch (Exception ex) {
            error("Số bắt đầu / kết thúc phải là số nguyên.");
            return;
        }
        if (start > end || step < 1) {
            error("Số bắt đầu phải nhỏ hơn hoặc bằng số kết thúc và bước nhảy >= 1.");
            return;
        }

        Matcher matcher = Pattern.compile("(@+)").matcher(format);
        if (!matcher.find()) {
            error("Định dạng phải chứa ít nhất một ký tự @.");
            return;
        }
        int digitCount = matcher.group(1).length();
        int atStart = matcher.start();
        if (String.valueOf(start).length() > digitCount || String.valueOf(end).length() > digitCount) {
            error("Số bắt đầu hoặc kết thúc có quá nhiều chữ số so với " + digitCount + " ký tự @.");
            return;
        }

        int count = ((end - start) / step) + 1;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Xác nhận tạo loạt");
        confirm.setHeaderText("Tạo " + count + " serial");
        confirm.setContentText("OK: thay thế danh sách hiện tại. Cancel: giữ nguyên.\nNếu muốn thêm vào danh sách, chọn 'Thêm vào'.");
        ButtonType appendType = new ButtonType("Thêm vào");
        confirm.getButtonTypes().setAll(ButtonType.OK, appendType, ButtonType.CANCEL);
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() == ButtonType.CANCEL) return;
        if (choice.get() == ButtonType.OK) rows.clear();

        int generated = 0;
        for (int num = start; num <= end; num += step) {
            String number = String.format("%0" + digitCount + "d", num);
            String serial = format.substring(0, atStart) + number + format.substring(atStart + digitCount);
            rows.add(new SerialRow(rows.size() + 1, serial));
            generated++;
        }
        info("Đã tạo thành công " + generated + " serial.");
    }

    private void saveSerials() {
        table.edit(-1, null);
        Set<String> duplicateCheck = new HashSet<>();
        List<String> serials = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            String serial = rows.get(i).getSerial() == null ? "" : rows.get(i).getSerial().trim();
            if (serial.isEmpty()) {
                error("Serial không được để trống (dòng " + (i + 1) + ").");
                return;
            }
            if (!duplicateCheck.add(serial)) {
                error("Serial bị trùng: " + serial);
                return;
            }
            serials.add(serial);
        }
        resultSerials = serials;
        stage.close();
    }

    private void reindex() {
        for (int i = 0; i < rows.size(); i++) rows.get(i).setIndex(i + 1);
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final class SerialRow {
        private final SimpleIntegerProperty index = new SimpleIntegerProperty();
        private final SimpleStringProperty serial = new SimpleStringProperty();

        private SerialRow(int index, String serial) {
            this.index.set(index);
            this.serial.set(serial);
        }

        private SimpleIntegerProperty indexProperty() { return index; }
        private SimpleStringProperty serialProperty() { return serial; }
        private String getSerial() { return serial.get(); }
        private void setSerial(String value) { serial.set(value == null ? "" : value); }
        private void setIndex(int value) { index.set(value); }
    }
}
