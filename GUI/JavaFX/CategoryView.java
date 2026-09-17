package GUI.JavaFX;

import Bus.CategoryBUS;
import DTO.CategoryDTO;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import valiateData.PermissionManager;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JavaFX screen for product category management. */
public final class CategoryView {

    private static final String ALL = "Tất cả";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final CategoryBUS categoryBUS = new CategoryBUS();
    private final PermissionManager permissionManager = new PermissionManager();

    private final BorderPane root = new BorderPane();
    private final ObservableList<CategoryDTO> filteredItems = FXCollections.observableArrayList();
    private final ObservableList<CategoryDTO> pageItems = FXCollections.observableArrayList();
    private final List<CategoryDTO> categoryList = new ArrayList<>();
    private final TableView<CategoryDTO> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    private final Label pageLabel = new Label();
    private final ComboBox<Integer> pageSizeBox = new ComboBox<>();

    private Button editButton;
    private Button deleteButton;
    private Button previousButton;
    private Button nextButton;

    private int currentPage = 1;
    private int pageSize = 10;

    public CategoryView() {
        buildView();
        configureEvents();
        loadCategories();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildView() {
        root.getStyleClass().add("category-root");
        root.setTop(createHeader());
        root.setCenter(createContent());
        root.setBottom(createPagination());
    }

    private VBox createHeader() {
        Label eyebrow = new Label("PRODUCT CATEGORIES");
        eyebrow.getStyleClass().add("category-eyebrow");

        Label title = new Label("Phân loại sản phẩm");
        title.getStyleClass().add("category-title");

        Label subtitle = new Label("Quản lý mã và tên danh mục dùng để phân loại sản phẩm trong hệ thống.");
        subtitle.getStyleClass().add("category-subtitle");

        VBox text = new VBox(3, eyebrow, title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resultLabel.getStyleClass().add("category-count");
        HBox row = new HBox(18, text, spacer, resultLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(row);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.getStyleClass().add("category-header");
        return header;
    }

    private VBox createContent() {
        HBox toolbar = createToolbar();
        configureTable();

        VBox tableCard = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getStyleClass().add("category-table-card");

        VBox content = new VBox(16, toolbar, tableCard);
        content.setPadding(new Insets(22, 28, 28, 28));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return content;
    }

    private HBox createToolbar() {
        Label searchLabel = new Label("Tìm kiếm");
        searchLabel.getStyleClass().add("toolbar-label");

        searchField.setPromptText("Nhập tên phân loại...");
        searchField.setPrefWidth(280);
        searchField.getStyleClass().add("category-search-field");
        searchField.setTooltip(new Tooltip("Tìm kiếm theo tên phân loại"));

        categoryBox.setPrefWidth(185);
        categoryBox.getStyleClass().add("category-filter-box");
        categoryBox.setTooltip(new Tooltip("Lọc theo một phân loại cụ thể"));

        Button searchButton = createButton("Tìm", "secondary-button", this::applySearch);
        Button refreshButton = createButton("Làm mới", "secondary-button", this::resetFilters);

        Button addButton = createButton("Thêm", "success-button", this::addCategory);
        setPermissionVisibility(addButton, "CATEGORY", "ADD");

        editButton = createButton("Sửa", "primary-button", this::editSelectedCategory);
        setPermissionVisibility(editButton, "CATEGORY", "UPDATE");
        editButton.setDisable(true);

        deleteButton = createButton("Xóa", "danger-button", this::deleteSelectedCategory);
        setPermissionVisibility(deleteButton, "CATEGORY", "DELETE");
        deleteButton.setDisable(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                searchLabel, searchField, searchButton, categoryBox,
                spacer, refreshButton, addButton, editButton, deleteButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14, 16, 14, 16));
        toolbar.getStyleClass().add("category-toolbar");
        return toolbar;
    }

    private HBox createPagination() {
        previousButton = createButton("‹ Trước", "secondary-button", () -> {
            if (currentPage > 1) {
                currentPage--;
                renderPage();
            }
        });

        nextButton = createButton("Sau ›", "secondary-button", () -> {
            if (currentPage < totalPages()) {
                currentPage++;
                renderPage();
            }
        });

        pageSizeBox.getItems().setAll(10, 20, 30, 50, 100);
        pageSizeBox.setValue(pageSize);
        pageSizeBox.setPrefWidth(85);
        pageSizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 1;
                renderPage();
            }
        });

        pageLabel.getStyleClass().add("category-page-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(8, pageLabel, spacer, new Label("Dòng/trang"), pageSizeBox, previousButton, nextButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 28, 18, 28));
        footer.getStyleClass().add("category-pagination");
        return footer;
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("category-button", styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void setPermissionVisibility(Button button, String module, String action) {
        boolean visible = permissionManager.hasPermission(module, action);
        button.setVisible(visible);
        button.setManaged(visible);
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label("Không có phân loại phù hợp."));
        table.getStyleClass().add("category-table");

        TableColumn<CategoryDTO, Number> sttColumn = new TableColumn<>("STT");
        sttColumn.setPrefWidth(58);
        sttColumn.setMinWidth(52);
        sttColumn.setMaxWidth(72);
        sttColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                pageItems.indexOf(data.getValue()) + 1 + (currentPage - 1) * pageSize));
        sttColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CategoryDTO, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getCategory_id()));
        idColumn.setVisible(false);

        TableColumn<CategoryDTO, String> codeColumn = new TableColumn<>("Mã Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getCode())));
        codeColumn.setPrefWidth(150);

        TableColumn<CategoryDTO, String> nameColumn = new TableColumn<>("Tên phân loại");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getName())));
        nameColumn.setPrefWidth(260);

        TableColumn<CategoryDTO, String> createdColumn = new TableColumn<>("Ngày tạo");
        createdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getCreated_at() == null ? "" : data.getValue().getCreated_at().format(DATE_FORMATTER)));
        createdColumn.setPrefWidth(185);

        TableColumn<CategoryDTO, String> updatedColumn = new TableColumn<>("Ngày cập nhật");
        updatedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getUpdated_at() == null ? "" : data.getValue().getUpdated_at().format(DATE_FORMATTER)));
        updatedColumn.setPrefWidth(185);

        table.getColumns().setAll(sttColumn, idColumn, codeColumn, nameColumn, createdColumn, updatedColumn);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            boolean none = selected == null;
            editButton.setDisable(none);
            deleteButton.setDisable(none);
        });

        table.setRowFactory(tv -> {
            TableRow<CategoryDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()
                        && permissionManager.hasPermission("CATEGORY", "UPDATE")) {
                    editCategory(row.getItem());
                }
            });
            return row;
        });
    }

    private void configureEvents() {
        searchField.setOnAction(event -> applySearch());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 1;
            applySearch();
        });
        categoryBox.setOnAction(event -> {
            currentPage = 1;
            applySearch();
        });
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(categoryBUS.getAll());

        String oldSelection = categoryBox.getValue();
        categoryBox.getItems().clear();
        categoryBox.getItems().add(ALL);
        for (CategoryDTO category : categoryList) {
            categoryBox.getItems().add(safe(category.getName()));
        }

        if (oldSelection != null && categoryBox.getItems().contains(oldSelection)) {
            categoryBox.setValue(oldSelection);
        } else {
            categoryBox.setValue(ALL);
        }

        currentPage = 1;
        applySearch();
    }

    private void applySearch() {
        if (categoryBox.getItems().isEmpty()) {
            return;
        }

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        int selectedIndex = categoryBox.getSelectionModel().getSelectedIndex();
        ArrayList<CategoryDTO> result;

        if (selectedIndex > 0 && selectedIndex - 1 < categoryList.size()) {
            int categoryId = categoryList.get(selectedIndex - 1).getCategory_id();
            if (!keyword.isEmpty()) {
                result = categoryBUS.searchByCategoryAndKeyword(categoryId, keyword);
            } else {
                result = new ArrayList<>();
                CategoryDTO category = categoryBUS.getByID(categoryId);
                if (category != null) {
                    result.add(category);
                }
            }
        } else if (!keyword.isEmpty()) {
            result = categoryBUS.search(keyword);
        } else {
            result = categoryBUS.getAll();
        }

        filteredItems.setAll(result);
        if (currentPage > totalPages()) currentPage = totalPages();
        if (currentPage < 1) currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int total = filteredItems.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);
        pageItems.setAll(filteredItems.subList(start, end));
        int pages = totalPages();
        pageLabel.setText("Trang " + currentPage + "/" + pages);
        resultLabel.setText(total + " phân loại");
        previousButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= pages);
        table.refresh();
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(filteredItems.size() / (double) pageSize));
    }

    private void resetFilters() {
        searchField.clear();
        loadCategories();
        categoryBox.setValue(ALL);
        currentPage = 1;
        applySearch();
    }

    private void addCategory() {
        if (!permissionManager.hasPermission("CATEGORY", "ADD")) {
            showWarning("Bạn không có quyền thêm phân loại.");
            return;
        }
        if (CategoryEditDialogFx.show(ownerWindow(), null, categoryBUS)) {
            loadCategories();
            showInformation("Thêm phân loại thành công.");
        }
    }

    private void editSelectedCategory() {
        CategoryDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn phân loại cần sửa.");
            return;
        }
        editCategory(selected);
    }

    private void editCategory(CategoryDTO category) {
        if (!permissionManager.hasPermission("CATEGORY", "UPDATE")) {
            showWarning("Bạn không có quyền sửa phân loại.");
            return;
        }
        if (CategoryEditDialogFx.show(ownerWindow(), category, categoryBUS)) {
            loadCategories();
            showInformation("Cập nhật phân loại thành công.");
        }
    }

    private void deleteSelectedCategory() {
        if (!permissionManager.hasPermission("CATEGORY", "DELETE")) {
            showWarning("Bạn không có quyền xóa phân loại.");
            return;
        }

        CategoryDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn phân loại cần xóa.");
            return;
        }

        if (categoryBUS.isCategoryInUse(selected.getCategory_id())) {
            showWarning("Không thể xóa phân loại này vì có sản phẩm đang sử dụng.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa phân loại sản phẩm?");
        confirm.setContentText("Bạn có chắc chắn muốn xóa phân loại: " + safe(selected.getName()) + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (categoryBUS.delete(selected.getCategory_id())) {
            loadCategories();
            showInformation("Xóa phân loại thành công.");
        } else {
            showError("Xóa phân loại thất bại.");
        }
    }

    private Window ownerWindow() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private void showInformation(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", message);
    }

    private void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", message);
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Lỗi", message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}