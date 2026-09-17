package GUI.JavaFX;

import Bus.CustomerBUS;
import Bus.OrderBUS;
import Bus.ProductSellBUS;
import Bus.SerialBUS;
import Bus.promotionsBUS;
import DTO.CustomerDTO;
import DTO.OrderDTO;
import DTO.OrderItemDTO;
import DTO.ProductSellDTO;
import DTO.SerialDTO;
import DTO.UserSession;
import DTO.promotionItemsDTO;
import DTO.promotionsDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import valiateData.validationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** JavaFX replacement for GUI.Dialog.themHoaDonDialog. */
public final class OrderCreateDialogFx {
    private final Stage stage = new Stage();
    private final CustomerBUS customerBUS = new CustomerBUS();
    private final OrderBUS orderBUS = new OrderBUS();
    private final ProductSellBUS productSellBUS = new ProductSellBUS();
    private final SerialBUS serialBUS = new SerialBUS();
    private final promotionsBUS promotionBUS = new promotionsBUS();
    private final promotionsDTO noPromotionOption = createNoPromotionOption();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");

    private final ObservableList<ProductSellDTO> productRows = FXCollections.observableArrayList();
    private final ObservableList<CartLine> cartRows = FXCollections.observableArrayList();
    private final ArrayList<ProductSellDTO> allProducts = new ArrayList<>();
    private final ArrayList<SerialDTO> availableSerials = new ArrayList<>();
    private final ArrayList<SerialDTO> serialsInUse = new ArrayList<>();
    private final ArrayList<OrderItemDTO> soldItems = new ArrayList<>();
    private final ArrayList<OrderItemDTO> giftedItems = new ArrayList<>();
    private final ArrayList<promotionsDTO> allPromotions = new ArrayList<>();

    private final TableView<ProductSellDTO> productTable = new TableView<>();
    private final TableView<CartLine> cartTable = new TableView<>();
    private final TextField productSearch = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField customerNameField = new TextField();
    private final Label customerIdLabel = new Label("-");
    private final ComboBox<String> paymentBox = new ComboBox<>();
    private final ComboBox<promotionsDTO> promotionBox = new ComboBox<>();
    private final Label subtotalLabel = new Label("0 VNĐ");
    private final Label discountLabel = new Label("0 VNĐ");
    private final Label finalTotalLabel = new Label("0 VNĐ");

    private final Long orderId;
    private Long tempCustomerId;
    private CustomerDTO existingCustomer;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal promotionDiscount = BigDecimal.ZERO;
    private boolean saved;
    private boolean changingPromotion;

    public OrderCreateDialogFx(Window owner) {
        this.orderId = orderBUS.getOrderID();
        configureStage(owner);
        loadReferenceData();
        buildView();
    }

    public void showAndWait() {
        if (orderId == null) {
            showAlert(Alert.AlertType.ERROR, "Không thể tạo hóa đơn", "Không tạo được mã hóa đơn tạm.");
            return;
        }
        stage.showAndWait();
    }

    public boolean isSaved() { return saved; }
    public Long getOrderId() { return orderId; }
    public Long getTempCustomerId() { return tempCustomerId; }

    private void configureStage(Window owner) {
        stage.setTitle("Tạo hóa đơn");
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setWidth(1320);
        stage.setHeight(800);
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
    }

    private void loadReferenceData() {
        List<ProductSellDTO> products = productSellBUS.getAllAvailableProducts();
        if (products != null) allProducts.addAll(products);
        productRows.setAll(allProducts);

        List<SerialDTO> serials = serialBUS.getAllSerialByProduct();
        if (serials != null) availableSerials.addAll(serials);

        List<promotionsDTO> promotions = promotionBUS.getAllPromotions();
        if (promotions != null) allPromotions.addAll(promotions);
    }

    private void buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("order-dialog-root");
        root.setPadding(new Insets(14));

        SplitPane splitPane = new SplitPane(createProductPane(), createOrderPane());
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.45);
        root.setCenter(splitPane);

        Scene scene = new Scene(root);
        var css = getClass().getResource("/GUI/JavaFX/order.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN),
                this::clearAllItems);
        stage.setScene(scene);
    }

    private VBox createProductPane() {
        Label eyebrow = new Label("AVAILABLE PRODUCTS");
        eyebrow.getStyleClass().add("order-eyebrow");
        Label title = new Label("Danh sách sản phẩm");
        title.getStyleClass().add("order-dialog-title");
        Label subtitle = new Label("Chọn sản phẩm và serial còn khả dụng để đưa vào hóa đơn.");
        subtitle.getStyleClass().add("order-subtitle");

        productSearch.setPromptText("Tìm theo mã hoặc tên sản phẩm...");
        productSearch.textProperty().addListener((obs, oldValue, newValue) -> filterProducts());
        Button addProduct = button("+ Thêm sản phẩm", "primary-button", this::addSelectedProduct);
        HBox searchRow = new HBox(8, productSearch, addProduct);
        HBox.setHgrow(productSearch, Priority.ALWAYS);

        configureProductTable();
        VBox card = new VBox(10, eyebrow, title, subtitle, searchRow, productTable);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(14));
        return card;
    }

    private VBox createOrderPane() {
        Label eyebrow = new Label("NEW ORDER");
        eyebrow.getStyleClass().add("order-eyebrow");
        Label title = new Label("Thông tin hóa đơn #" + (orderId == null ? "N/A" : orderId));
        title.getStyleClass().add("order-dialog-title");

        GridPane info = new GridPane();
        info.setHgap(12);
        info.setVgap(9);
        info.getColumnConstraints().clear();

        Label employeeValue = new Label(UserSession.getInstance().getAccount() == null
                ? "N/A" : UserSession.getInstance().getAccount().getUsername());
        employeeValue.getStyleClass().add("order-info-value");
        customerIdLabel.getStyleClass().add("order-info-value");

        phoneField.setPromptText("10 chữ số");
        phoneField.textProperty().addListener((obs, oldValue, newValue) -> handleCustomer());
        customerNameField.setPromptText("Tên khách hàng");

        paymentBox.getItems().setAll("Tiền Mặt", "Chuyển Khoản");
        paymentBox.getSelectionModel().selectFirst();

        promotionBox.setCellFactory(list -> promotionCell());
        promotionBox.setButtonCell(promotionCell());
        promotionBox.valueProperty().addListener((obs, oldValue, newValue) -> applyPromotion(newValue));
        refreshPromotionChoices();

        addField(info, 0, 0, "Mã hóa đơn", new Label(String.valueOf(orderId)));
        addField(info, 2, 0, "Nhân viên", employeeValue);
        addField(info, 0, 2, "Số điện thoại", phoneField);
        addField(info, 2, 2, "Mã khách hàng", customerIdLabel);
        addField(info, 0, 4, "Tên khách hàng", customerNameField);
        addField(info, 2, 4, "Thanh toán", paymentBox);
        addField(info, 0, 6, "Khuyến mãi", promotionBox);

        configureCartTable();
        Label cartTitle = new Label("Sản phẩm đã chọn");
        cartTitle.getStyleClass().add("dialog-section-title");
        Label shortcut = new Label("Ctrl + X: xóa toàn bộ chi tiết hóa đơn.");
        shortcut.getStyleClass().add("order-hint");

        VBox totals = createTotals();
        Button remove = button("Xóa sản phẩm", "danger-button", this::removeSelectedItem);
        Button clear = button("Xóa tất cả", "secondary-button", this::clearAllItems);
        Button save = button("Lưu hóa đơn", "success-button", this::saveOrder);
        Button close = button("Đóng", "secondary-button", stage::close);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, remove, clear, spacer, close, save);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, eyebrow, title, info, new Separator(), cartTitle, shortcut, cartTable, totals, actions);
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(14));
        return card;
    }

    private ListCell<promotionsDTO> promotionCell() {
        return new ListCell<>() {
            @Override protected void updateItem(promotionsDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else if (item == noPromotionOption) setText("Không áp dụng");
                else setText(item.getName() + " [" + item.getPromotion_type() + "]");
            }
        };
    }

    private void addField(GridPane grid, int col, int row, String labelText, javafx.scene.Node value) {
        Label label = new Label(labelText);
        label.getStyleClass().add("order-info-key");
        if (value instanceof Label) ((Label) value).getStyleClass().add("order-info-value");
        grid.add(label, col, row);
        grid.add(value, col, row + 1);
        GridPane.setHgrow(value, Priority.ALWAYS);
    }

    @SuppressWarnings("unchecked")
    private void configureProductTable() {
        productTable.setItems(productRows);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setPlaceholder(new Label("Không có sản phẩm còn hàng."));
        TableColumn<ProductSellDTO, String> idCol = new TableColumn<>("Mã SP");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getProduct_id())));
        idCol.setPrefWidth(75);
        TableColumn<ProductSellDTO, String> nameCol = new TableColumn<>("Tên SP");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getProduct_name())));
        nameCol.setPrefWidth(210);
        TableColumn<ProductSellDTO, Number> stockCol = new TableColumn<>("SL");
        stockCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStock()));
        stockCol.setPrefWidth(55);
        TableColumn<ProductSellDTO, String> unitCol = new TableColumn<>("Đơn giá");
        unitCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(BigDecimal.valueOf(data.getValue().getUnit_price()))));
        unitCol.setPrefWidth(105);
        TableColumn<ProductSellDTO, String> sellCol = new TableColumn<>("Giá bán");
        sellCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(BigDecimal.valueOf(data.getValue().getSell_price()))));
        sellCol.setPrefWidth(105);
        productTable.getColumns().setAll(idCol, nameCol, stockCol, unitCol, sellCol);
        productTable.setRowFactory(tv -> {
            TableRow<ProductSellDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) addProduct(row.getItem());
            });
            return row;
        });
    }

    @SuppressWarnings("unchecked")
    private void configureCartTable() {
        cartTable.setItems(cartRows);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.setPlaceholder(new Label("Hóa đơn chưa có sản phẩm."));
        TableColumn<CartLine, String> idCol = new TableColumn<>("Mã SP");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().item.getProductId())));
        idCol.setPrefWidth(70);
        TableColumn<CartLine, String> nameCol = new TableColumn<>("Tên SP");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().productName));
        nameCol.setPrefWidth(160);
        TableColumn<CartLine, String> serialCol = new TableColumn<>("Serial");
        serialCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().item.getProductSerial()));
        serialCol.setPrefWidth(115);
        TableColumn<CartLine, String> typeCol = new TableColumn<>("Ghi chú");
        typeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().type));
        typeCol.setPrefWidth(80);
        TableColumn<CartLine, String> originalCol = new TableColumn<>("Giá bán");
        originalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().originalPrice)));
        originalCol.setPrefWidth(95);
        TableColumn<CartLine, String> discountCol = new TableColumn<>("Giảm");
        discountCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().discount)));
        discountCol.setPrefWidth(85);
        TableColumn<CartLine, String> finalCol = new TableColumn<>("Thành tiền");
        finalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMoney(data.getValue().finalPrice)));
        finalCol.setPrefWidth(105);
        cartTable.getColumns().setAll(idCol, nameCol, serialCol, typeCol, originalCol, discountCol, finalCol);
    }

    private VBox createTotals() {
        subtotalLabel.getStyleClass().add("order-total-value");
        discountLabel.getStyleClass().add("order-total-muted");
        finalTotalLabel.getStyleClass().add("order-grand-total");
        HBox subtotalRow = totalRow("Tạm tính", subtotalLabel);
        HBox discountRow = totalRow("Giảm giá", discountLabel);
        HBox finalRow = totalRow("Tổng tiền", finalTotalLabel);
        VBox totals = new VBox(4, subtotalRow, discountRow, finalRow);
        totals.getStyleClass().add("order-total-box");
        updateTotals();
        return totals;
    }

    private HBox totalRow(String name, Label value) {
        Label label = new Label(name);
        label.getStyleClass().add("order-total-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, label, spacer, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().addAll("order-button", styleClass);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void filterProducts() {
        String keyword = safe(productSearch.getText()).trim().toLowerCase(Locale.ROOT);
        ArrayList<ProductSellDTO> filtered = new ArrayList<>();
        for (ProductSellDTO p : allProducts) {
            if (keyword.isEmpty()
                    || String.valueOf(p.getProduct_id()).contains(keyword)
                    || safe(p.getProduct_name()).toLowerCase(Locale.ROOT).contains(keyword)) {
                filtered.add(p);
            }
        }
        productRows.setAll(filtered);
    }

    private void addSelectedProduct() {
        ProductSellDTO selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần thêm.");
            return;
        }
        addProduct(selected);
    }

    private void addProduct(ProductSellDTO product) {
        if (product == null || orderId == null) return;
        SerialDTO serial = chooseSerial(product.getProduct_id(), "Chọn serial sản phẩm");
        if (serial == null) return;

        BigDecimal original = BigDecimal.valueOf(product.getUnit_price());
        BigDecimal sell = BigDecimal.valueOf(product.getSell_price());
        OrderItemDTO item = new OrderItemDTO(orderId, product.getProduct_id(), original, sell,
                serial.getReceiptId(), serial.getSerial());
        soldItems.add(item);
        serialsInUse.add(serial);
        availableSerials.remove(serial);
        cartRows.add(new CartLine(item, safe(product.getProduct_name()), "Hàng Bán", original,
                original.subtract(sell), sell, null, serial));
        subtotal = subtotal.add(sell);
        clearPromotionEffects();
        refreshPromotionChoices();
        updateTotals();
    }

    private SerialDTO chooseSerial(Long productId, String title) {
        ArrayList<SerialDTO> choices = new ArrayList<>();
        for (SerialDTO s : availableSerials) {
            if (productId != null && productId.equals(s.getProductId())) choices.add(s);
        }
        if (choices.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Hết serial", "Sản phẩm này không còn serial khả dụng.");
            return null;
        }
        ChoiceDialog<SerialDTO> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(title);
        dialog.setHeaderText("Chọn serial còn khả dụng");
        dialog.setContentText("Serial:");
        Optional<SerialDTO> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void refreshPromotionChoices() {
        changingPromotion = true;
        ObservableList<promotionsDTO> choices = FXCollections.observableArrayList();
        choices.add(noPromotionOption);
        for (promotionsDTO p : allPromotions) {
            BigDecimal min = BigDecimal.valueOf(p.getMin_paid());
            if (subtotal.compareTo(min) >= 0) choices.add(p);
        }
        promotionBox.setItems(choices);
        promotionBox.getSelectionModel().select(0);
        changingPromotion = false;
    }

    private void applyPromotion(promotionsDTO promotion) {
        if (changingPromotion) return;
        clearPromotionEffects();
        if (promotion == null || promotion == noPromotionOption || promotion.getPromotion_id() < 0) {
            updateTotals();
            return;
        }

        if ("BILL".equalsIgnoreCase(promotion.getPromotion_type())) {
            promotionDiscount = subtotal
                    .multiply(BigDecimal.valueOf(promotion.getDiscount_on_bill()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            updateTotals();
            return;
        }

        if ("GIFT".equalsIgnoreCase(promotion.getPromotion_type())) {
            ArrayList<promotionItemsDTO> promotionItems = promotionBUS.getPromotionItems(promotion.getPromotion_id());
            if (promotionItems == null) promotionItems = new ArrayList<>();
            ArrayList<CartLine> addedGiftLines = new ArrayList<>();
            for (CartLine source : new ArrayList<>(cartRows)) {
                if (!"Hàng Bán".equals(source.type)) continue;
                for (promotionItemsDTO pi : promotionItems) {
                    if (!source.item.getProductId().equals(pi.getProduct_id()) || pi.getGifted_product_id() == null) continue;
                    SerialDTO giftSerial = chooseSerial(pi.getGifted_product_id(), "Chọn serial hàng tặng");
                    if (giftSerial == null) {
                        rollbackGiftLines(addedGiftLines);
                        changingPromotion = true;
                        promotionBox.getSelectionModel().select(0);
                        changingPromotion = false;
                        showAlert(Alert.AlertType.WARNING, "Chưa áp dụng khuyến mãi",
                                "Không đủ hoặc chưa chọn serial cho hàng tặng.");
                        updateTotals();
                        return;
                    }
                    OrderItemDTO gift = new OrderItemDTO(orderId, pi.getGifted_product_id(), BigDecimal.ZERO,
                            BigDecimal.ZERO, giftSerial.getReceiptId(), giftSerial.getSerial());
                    giftedItems.add(gift);
                    serialsInUse.add(giftSerial);
                    availableSerials.remove(giftSerial);
                    CartLine line = new CartLine(gift, safe(pi.getGifted_product_name()), "Hàng Tặng",
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            source.item.getProductSerial(), giftSerial);
                    cartRows.add(line);
                    addedGiftLines.add(line);
                }
            }
        }
        updateTotals();
    }

    private void clearPromotionEffects() {
        promotionDiscount = BigDecimal.ZERO;
        if (!giftedItems.isEmpty()) {
            ArrayList<CartLine> gifts = new ArrayList<>();
            for (CartLine line : cartRows) if ("Hàng Tặng".equals(line.type)) gifts.add(line);
            rollbackGiftLines(gifts);
        }
    }

    private void rollbackGiftLines(List<CartLine> gifts) {
        for (CartLine line : gifts) {
            cartRows.remove(line);
            giftedItems.remove(line.item);
            if (line.serialRef != null) {
                serialsInUse.remove(line.serialRef);
                if (!availableSerials.contains(line.serialRef)) availableSerials.add(line.serialRef);
            }
        }
    }

    private void removeSelectedItem() {
        CartLine selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần xóa.");
            return;
        }
        if ("Hàng Tặng".equals(selected.type)) {
            showAlert(Alert.AlertType.INFORMATION, "Hàng tặng", "Hãy bỏ khuyến mãi hoặc xóa sản phẩm điều kiện để bỏ hàng tặng.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Xóa sản phẩm khỏi hóa đơn?");
        confirm.setContentText(selected.productName + " - " + selected.item.getProductSerial());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        clearPromotionEffects();
        cartRows.remove(selected);
        soldItems.remove(selected.item);
        subtotal = subtotal.subtract(selected.finalPrice);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) subtotal = BigDecimal.ZERO;
        if (selected.serialRef != null) {
            serialsInUse.remove(selected.serialRef);
            if (!availableSerials.contains(selected.serialRef)) availableSerials.add(selected.serialRef);
        }
        refreshPromotionChoices();
        updateTotals();
    }

    private void clearAllItems() {
        if (soldItems.isEmpty() && giftedItems.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Xóa tất cả chi tiết hóa đơn?");
        confirm.setContentText("Toàn bộ serial đã chọn sẽ được trả lại danh sách khả dụng.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        for (SerialDTO serial : new ArrayList<>(serialsInUse)) {
            if (!availableSerials.contains(serial)) availableSerials.add(serial);
        }
        serialsInUse.clear();
        soldItems.clear();
        giftedItems.clear();
        cartRows.clear();
        subtotal = BigDecimal.ZERO;
        promotionDiscount = BigDecimal.ZERO;
        refreshPromotionChoices();
        updateTotals();
    }

    private void handleCustomer() {
        String phone = safe(phoneField.getText()).trim();
        phoneField.getStyleClass().remove("field-error");
        if (phone.length() < 10) {
            customerIdLabel.setText("-");
            customerNameField.clear();
            customerNameField.setEditable(true);
            existingCustomer = null;
            return;
        }
        if (phone.length() > 10) {
            phoneField.getStyleClass().add("field-error");
            return;
        }

        validationResult validation = customerBUS.validateSDT(phone);
        if (!validation.isValid()) {
            phoneField.getStyleClass().add("field-error");
            return;
        }

        existingCustomer = customerBUS.getKHinfor(phone);
        if (existingCustomer != null) {
            customerIdLabel.setText(String.valueOf(existingCustomer.getCustomerId()));
            customerNameField.setText(safe(existingCustomer.getCustomerName()));
            customerNameField.setEditable(false);
            if (tempCustomerId != null) customerBUS.delete(tempCustomerId);
            tempCustomerId = null;
        } else {
            if (tempCustomerId != null) customerBUS.delete(tempCustomerId);
            tempCustomerId = customerBUS.createCustomerID();
            customerIdLabel.setText(String.valueOf(tempCustomerId));
            customerNameField.clear();
            customerNameField.setEditable(true);
        }
    }

    private void saveOrder() {
        if (orderId == null) {
            showAlert(Alert.AlertType.ERROR, "Không thể lưu", "Mã hóa đơn không hợp lệ.");
            return;
        }
        if (soldItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu sản phẩm", "Hóa đơn chưa có sản phẩm.");
            return;
        }
        String phone = safe(phoneField.getText()).trim();
        validationResult phoneValidation = customerBUS.validateSDT(phone);
        if (phone.isEmpty() || !phoneValidation.isValid()) {
            phoneField.getStyleClass().add("field-error");
            showAlert(Alert.AlertType.WARNING, "Số điện thoại không hợp lệ",
                    phoneValidation.getMessage() == null ? "Vui lòng nhập số điện thoại khách hàng." : phoneValidation.getMessage());
            return;
        }
        String customerName = safe(customerNameField.getText()).trim();
        if (customerName.isEmpty()) {
            customerNameField.getStyleClass().add("field-error");
            showAlert(Alert.AlertType.WARNING, "Thiếu tên khách hàng", "Vui lòng nhập tên khách hàng.");
            return;
        }

        Long customerId;
        try {
            String customerIdText = safe(customerIdLabel.getText()).trim();
            if (customerIdText.isEmpty() || "-".equals(customerIdText)) throw new NumberFormatException();
            customerId = Long.valueOf(customerIdText);
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.WARNING, "Khách hàng chưa hợp lệ", "Vui lòng nhập lại số điện thoại khách hàng.");
            return;
        }

        ArrayList<OrderItemDTO> itemsToSave = new ArrayList<>(soldItems);
        itemsToSave.addAll(giftedItems);
        for (OrderItemDTO item : itemsToSave) {
            if (!serialBUS.isSerialAvailable(item.getProductSerial())) {
                showAlert(Alert.AlertType.WARNING, "Serial đã được sử dụng", item.getProductSerial() + " đã được sử dụng.");
                return;
            }
        }

        promotionsDTO promotion = promotionBox.getValue();
        Integer promotionId = (promotion == null || promotion == noPromotionOption || promotion.getPromotion_id() < 0)
                ? null : promotion.getPromotion_id();
        String payment = paymentBox.getValue() == null ? "Tiền Mặt" : paymentBox.getValue();
        long employeeId = UserSession.getInstance().getAccount().getEmployeeId();
        BigDecimal total = subtotal.subtract(promotionDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        OrderDTO order = new OrderDTO(orderId, customerId, promotionId, total, payment, employeeId, null);
        boolean orderSaved = orderBUS.insert(order, itemsToSave);
        if (!orderSaved) {
            showAlert(Alert.AlertType.ERROR, "Lưu hóa đơn thất bại", "Không thể lưu hóa đơn. Dữ liệu đã được rollback.");
            return;
        }

        boolean customerSaved = true;
        if (existingCustomer == null) {
            CustomerDTO customer = new CustomerDTO();
            customer.setCustomerId(customerId);
            customer.setCustomerName(customerName);
            customer.setPhoneNumber(phone);
            customerSaved = customerBUS.insert(customer);
        }
        if (!customerSaved) {
            showAlert(Alert.AlertType.ERROR, "Lưu khách hàng thất bại", "Hóa đơn đã lưu nhưng không cập nhật được thông tin khách hàng.");
            return;
        }

        saved = true;
        tempCustomerId = null; // placeholder customer is now a real customer and must not be cleaned up by parent
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu hóa đơn #" + orderId + " thành công.");
        stage.close();
    }

    private void updateTotals() {
        subtotalLabel.setText(formatMoney(subtotal) + " VNĐ");
        discountLabel.setText(formatMoney(promotionDiscount) + " VNĐ");
        BigDecimal total = subtotal.subtract(promotionDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        finalTotalLabel.setText(formatMoney(total) + " VNĐ");
        cartTable.refresh();
    }

    private static promotionsDTO createNoPromotionOption() {
        promotionsDTO promotion = new promotionsDTO();
        promotion.setPromotion_id(-1);
        promotion.setName("Không áp dụng");
        promotion.setPromotion_type("NONE");
        return promotion;
    }

    private String formatMoney(BigDecimal value) { return moneyFormat.format(value == null ? BigDecimal.ZERO : value); }
    private String safe(String value) { return value == null ? "" : value; }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? title : message);
        alert.showAndWait();
    }

    private static final class CartLine {
        private final OrderItemDTO item;
        private final String productName;
        private final String type;
        private final BigDecimal originalPrice;
        private final BigDecimal discount;
        private final BigDecimal finalPrice;
        private final String sourceSerial;
        private final SerialDTO serialRef;

        private CartLine(OrderItemDTO item, String productName, String type, BigDecimal originalPrice,
                         BigDecimal discount, BigDecimal finalPrice, String sourceSerial, SerialDTO serialRef) {
            this.item = item;
            this.productName = productName;
            this.type = type;
            this.originalPrice = originalPrice == null ? BigDecimal.ZERO : originalPrice;
            this.discount = discount == null ? BigDecimal.ZERO : discount;
            this.finalPrice = finalPrice == null ? BigDecimal.ZERO : finalPrice;
            this.sourceSerial = sourceSerial;
            this.serialRef = serialRef;
        }
    }
}
