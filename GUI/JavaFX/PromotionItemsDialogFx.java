package GUI.JavaFX;

import Bus.promotionsBUS;
import DTO.promotionItemsDTO;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.PromotionItemsDialog. */
public final class PromotionItemsDialogFx {

    private final int promotionId;
    private final String promotionName;
    private final promotionsBUS promotionBUS;
    private final Stage stage = new Stage();
    private final TableView<promotionItemsDTO> table = new TableView<>();
    private final ObservableList<promotionItemsDTO> items = FXCollections.observableArrayList();
    private final Label countLabel = new Label();

    public PromotionItemsDialogFx(Window owner, int promotionId, String promotionName, promotionsBUS promotionBUS) {
        this.promotionId = promotionId;
        this.promotionName = safe(promotionName);
        this.promotionBUS = promotionBUS;
        configureStage(owner);
        buildView();
        loadData();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private void configureStage(Window owner) {
        stage.setTitle("Quản lý sản phẩm khuyến mãi: " + promotionName);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setWidth(820);
        stage.setHeight(530);
        stage.setMinWidth(700);
        stage.setMinHeight(460);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("promotion-items-root");
        root.setTop(createHeader());
        root.setCenter(createTableCard());
        root.setBottom(createFooter());

        Scene scene = new Scene(root);
        URL css = getClass().getResource("/GUI/JavaFX/promotion.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setScene(scene);
    }

    private VBox createHeader() {
        Label eyebrow = new Label("PROMOTION ITEMS");
        eyebrow.getStyleClass().add("promotion-items-eyebrow");
        Label title = new Label("Sản phẩm áp dụng khuyến mãi");
        title.getStyleClass().add("promotion-items-title");
        Label name = new Label(promotionName);
        name.getStyleClass().add("promotion-items-name");

        VBox copy = new VBox(2, eyebrow, title, name);
        HBox.setHgrow(copy, Priority.ALWAYS);
        countLabel.getStyleClass().add("promotion-items-count");

        HBox row = new HBox(16, copy, countLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(row);
        header.setPadding(new Insets(18, 22, 16, 22));
        header.getStyleClass().add("promotion-items-header");
        return header;
    }

    private VBox createTableCard() {
        configureTable();
        Label hint = new Label("PERCENT: nhập % giảm cho sản phẩm • GIFT: chọn sản phẩm tặng kèm.");
        hint.getStyleClass().add("promotion-items-hint");
        VBox card = new VBox(9, hint, table);
        card.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getStyleClass().add("promotion-items-card");
        BorderPane.setMargin(card, new Insets(16, 22, 12, 22));
        return card;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label("Chưa có sản phẩm nào trong khuyến mãi."));
        table.getStyleClass().add("promotion-items-table");

        TableColumn<promotionItemsDTO, Number> idColumn = new TableColumn<>("Mã SP");
        idColumn.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getProduct_id()));

        TableColumn<promotionItemsDTO, String> nameColumn = new TableColumn<>("Tên sản phẩm");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getProduct_name())));
        nameColumn.setPrefWidth(240);

        TableColumn<promotionItemsDTO, Number> discountColumn = new TableColumn<>("Giảm giá (%)");
        discountColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getDiscount_percent()));

        TableColumn<promotionItemsDTO, String> giftColumn = new TableColumn<>("Sản phẩm tặng kèm");
        giftColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getGifted_product_name())));
        giftColumn.setPrefWidth(240);

        table.getColumns().setAll(idColumn, nameColumn, discountColumn, giftColumn);
        table.setRowFactory(tv -> {
            TableRow<promotionItemsDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editItem(row.getItem());
                }
            });
            return row;
        });
    }

    private HBox createFooter() {
        Button addButton = createButton("Thêm sản phẩm", "success-button", this::addItem);
        Button editButton = createButton("Sửa", "primary-button", this::editSelectedItem);
        Button deleteButton = createButton("Xóa", "danger-button", this::deleteSelectedItem);
        editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeButton = createButton("Đóng", "secondary-button", stage::close);

        HBox footer = new HBox(9, addButton, editButton, deleteButton, spacer, closeButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 22, 18, 22));
        return footer;
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("promotion-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void loadData() {
        try {
            ArrayList<promotionItemsDTO> list = promotionBUS.getPromotionItems(promotionId);
            items.setAll(list == null ? new ArrayList<>() : list);
            countLabel.setText(items.size() + " sản phẩm");
        } catch (RuntimeException ex) {
            items.clear();
            countLabel.setText("0 sản phẩm");
            showError("Không thể tải sản phẩm khuyến mãi.\n" + safe(ex.getMessage()));
        }
    }

    private void addItem() {
        PromotionItemEditDialogFx dialog = new PromotionItemEditDialogFx(stage, promotionId, null, promotionBUS);
        if (dialog.showAndWait()) {
            loadData();
        }
    }

    private void editSelectedItem() {
        promotionItemsDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn sản phẩm cần sửa.");
            return;
        }
        editItem(selected);
    }

    private void editItem(promotionItemsDTO selected) {
        if (selected == null) {
            return;
        }
        PromotionItemEditDialogFx dialog = new PromotionItemEditDialogFx(stage, promotionId, selected, promotionBUS);
        if (dialog.showAndWait()) {
            loadData();
        }
    }

    private void deleteSelectedItem() {
        promotionItemsDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn sản phẩm cần xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm khỏi khuyến mãi?");
        confirm.setContentText("Sản phẩm: " + safe(selected.getProduct_name()));
        Optional<ButtonType> result = confirm.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            if (promotionBUS.deletePromotionItem(promotionId, selected.getProduct_id())) {
                loadData();
            } else {
                showError("Xóa sản phẩm khỏi khuyến mãi thất bại.");
            }
        } catch (RuntimeException ex) {
            showError("Không thể xóa sản phẩm khỏi khuyến mãi.\n" + safe(ex.getMessage()));
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(stage);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
