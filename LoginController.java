package com.smartfinance.javafx;

import com.smartfinance.service.FinanceContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX login/register screen.
 */
public class LoginController {

    private final FinanceContext context;
    private final Runnable onSuccess;

    public LoginController(FinanceContext context, Runnable onSuccess) {
        this.context = context;
        this.onSuccess = onSuccess;
    }

    public void show(Stage stage) {
        Label title = new Label("Smart Finance Tracker");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        TextField fullName = new TextField();
        fullName.setPromptText("Full Name (for registration)");

        Button loginBtn = new Button("Login");
        loginBtn.setOnAction(e -> {
            var result = context.getAuthService().login(username.getText(), password.getText());
            if (result.isPresent()) {
                onSuccess.run();
            } else {
                showError("Invalid username or password");
            }
        });

        Button registerBtn = new Button("Register");
        registerBtn.setOnAction(e -> {
            try {
                context.getAuthService().register(username.getText(), password.getText(), fullName.getText());
                onSuccess.run();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        VBox root = new VBox(12, title,
                new Label("Demo login: demo / demo123"),
                username, password, fullName, loginBtn, registerBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f4f6f8;");

        stage.setScene(new Scene(root, 400, 380));
        stage.setTitle("Login - Smart Finance Tracker");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}
