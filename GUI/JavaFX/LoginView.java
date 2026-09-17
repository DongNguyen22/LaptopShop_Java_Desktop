package GUI.JavaFX;

import Bus.AccountBUS;
import DTO.AccountDTO;
import DTO.PasswordUtil;
import DTO.UserSession;
import GUI.dashBoard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.SwingUtilities;
import java.net.URL;

/**
 * JavaFX replacement for the old Swing LoginDialog.
 *
 * This is intentionally the first screen migrated to JavaFX. After a
 * successful login it still opens the existing Swing dashboard so the rest of
 * the project can be migrated screen-by-screen without breaking current
 * business logic.
 */
public class LoginView {

    private final AccountBUS accountBUS = new AccountBUS();
    private final String rememberedUsername;
    private final String rememberedPassword;

    private Stage stage;
    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtPasswordVisible;
    private Label lblError;
    private CheckBox chkRemember;
    private boolean passwordVisible;
    private boolean loginSuccess;

    public LoginView(String rememberedUsername, String rememberedPassword) {
        this.rememberedUsername = rememberedUsername == null ? "No" : rememberedUsername;
        this.rememberedPassword = rememberedPassword == null ? "No" : rememberedPassword;
    }

    /** Show login from the JavaFX Application start method. */
    public void show(Stage primaryStage) {
        this.stage = primaryStage;
        buildAndShow();
    }

    /**
     * Show the JavaFX login again from a Swing screen (for example Logout).
     * JavaFX is kept alive by APP.App with Platform.setImplicitExit(false).
     */
    public static void showFromSwing(String username, String password) {
        Platform.runLater(() -> {
            Stage loginStage = new Stage();
            new LoginView(username, password).show(loginStage);
        });
    }

    private void buildAndShow() {
        stage.setTitle("Đăng nhập hệ thống");
        stage.setMinWidth(820);
        stage.setMinHeight(520);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("login-root");

        VBox form = createLoginForm();
        StackPane imagePane = createImagePane();

        root.setLeft(form);
        root.setCenter(imagePane);

        Scene scene = new Scene(root, 920, 560);
        URL css = getClass().getResource("/GUI/JavaFX/login.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setScene(scene);
        stage.centerOnScreen();

        // If the user closes the login window instead of logging in, allow the
        // application to terminate even though JavaFX implicit exit is disabled.
        stage.setOnHidden(event -> {
            if (!loginSuccess) {
                Platform.exit();
            }
        });

        stage.show();
    }

    private VBox createLoginForm() {
        VBox form = new VBox(12);
        form.setPrefWidth(410);
        form.setMinWidth(360);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(48, 48, 48, 48));
        form.getStyleClass().add("login-form");

        Label lblTitle = new Label("ĐĂNG NHẬP");
        lblTitle.getStyleClass().add("login-title");

        Label lblSub = new Label("Vui lòng đăng nhập để tiếp tục");
        lblSub.getStyleClass().add("login-subtitle");

        Label lblUsername = new Label("Tên đăng nhập");
        lblUsername.getStyleClass().add("field-label");

        txtUsername = new TextField();
        txtUsername.setPromptText("Nhập tên đăng nhập");
        txtUsername.getStyleClass().add("input-field");

        Label lblPassword = new Label("Mật khẩu");
        lblPassword.getStyleClass().add("field-label");

        StackPane passwordPane = createPasswordField();

        chkRemember = new CheckBox("Ghi nhớ đăng nhập");
        chkRemember.getStyleClass().add("remember-check");

        lblError = new Label(" ");
        lblError.setWrapText(true);
        lblError.getStyleClass().add("error-label");

        Button btnLogin = new Button("Đăng nhập");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.getStyleClass().add("login-button");
        btnLogin.setDefaultButton(true);
        btnLogin.setOnAction(event -> handleLogin());

        form.getChildren().addAll(
                lblTitle,
                lblSub,
                spacer(12),
                lblUsername,
                txtUsername,
                spacer(4),
                lblPassword,
                passwordPane,
                chkRemember,
                lblError,
                btnLogin);

        rememberAccount();
        return form;
    }

    private StackPane createPasswordField() {
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Nhập mật khẩu");
        txtPassword.getStyleClass().add("input-field");

        txtPasswordVisible = new TextField();
        txtPasswordVisible.setPromptText("Nhập mật khẩu");
        txtPasswordVisible.getStyleClass().add("input-field");
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);

        // Keep both fields synchronized so switching visibility does not lose text.
        txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());

        Button btnEye = new Button("👁");
        btnEye.setFocusTraversable(false);
        btnEye.getStyleClass().add("eye-button");
        btnEye.setOnAction(event -> togglePassword());

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(txtPassword, Priority.ALWAYS);
        HBox.setHgrow(txtPasswordVisible, Priority.ALWAYS);
        row.getChildren().addAll(txtPassword, txtPasswordVisible, btnEye);
        row.getStyleClass().add("password-row");

        StackPane wrapper = new StackPane(row);
        return wrapper;
    }

    private StackPane createImagePane() {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("image-pane");

        URL imageUrl = getClass().getResource("/images/login.jpg");
        if (imageUrl != null) {
            Image image = new Image(imageUrl.toExternalForm(), true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(false);
            imageView.fitWidthProperty().bind(pane.widthProperty());
            imageView.fitHeightProperty().bind(pane.heightProperty());
            pane.getChildren().add(imageView);
        }

        VBox overlay = new VBox(8);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(36));
        overlay.setMouseTransparent(true);

        Label brand = new Label("META STORE");
        brand.getStyleClass().add("image-brand");
        Label caption = new Label("Hệ thống quản lý bán đồ điện tử");
        caption.getStyleClass().add("image-caption");
        overlay.getChildren().addAll(brand, caption);

        pane.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.BOTTOM_LEFT);
        return pane;
    }

    private StackPane spacer(double height) {
        StackPane spacer = new StackPane();
        spacer.setMinHeight(height);
        spacer.setPrefHeight(height);
        return spacer;
    }

    private void rememberAccount() {
        if (!"No".equals(rememberedPassword)) {
            txtUsername.setText(rememberedUsername);
            txtPassword.setText(rememberedPassword);
            chkRemember.setSelected(true);
        }
    }

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        txtPasswordVisible.setVisible(passwordVisible);
        txtPasswordVisible.setManaged(passwordVisible);
        txtPassword.setVisible(!passwordVisible);
        txtPassword.setManaged(!passwordVisible);

        if (passwordVisible) {
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
        } else {
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getText().length());
        }
    }

    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();

        resetError();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin");
            if (user.isEmpty()) {
                markError(txtUsername);
                txtUsername.requestFocus();
            } else {
                markPasswordError();
                activePasswordField().requestFocus();
            }
            return;
        }

        AccountDTO account = accountBUS.findByUserName(user);
        if (account == null || !PasswordUtil.checkPassword(pass, account.getPassword())) {
            lblError.setText("Sai tên đăng nhập hoặc mật khẩu");
            markError(txtUsername);
            markPasswordError();
            txtUsername.requestFocus();
            txtUsername.selectAll();
            return;
        }

        UserSession.getInstance().setAccount(account);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Đăng nhập");
        alert.setHeaderText(null);
        alert.setContentText("Đăng nhập thành công!");
        alert.showAndWait();

        final boolean remember = chkRemember.isSelected();
        loginSuccess = true;
        stage.close();

        // Phase 1 migration: keep the existing Swing dashboard running.
        SwingUtilities.invokeLater(() -> {
            if (remember) {
                new dashBoard(pass, user);
            } else {
                new dashBoard("No", "No");
            }
        });
    }

    private TextField activePasswordField() {
        return passwordVisible ? txtPasswordVisible : txtPassword;
    }

    private void markPasswordError() {
        markError(txtPassword);
        markError(txtPasswordVisible);
    }

    private void markError(TextField field) {
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
    }

    private void resetError() {
        lblError.setText(" ");
        txtUsername.getStyleClass().remove("input-error");
        txtPassword.getStyleClass().remove("input-error");
        txtPasswordVisible.getStyleClass().remove("input-error");
    }
}
