package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.UserSession;
import com.travelxp.models.User;
import com.travelxp.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Empty fields", "Please fill in all fields.");
            return;
        }

        try {
            User user = userService.login(email, password);
            if (user != null) {
                Main.setSession(new com.travelxp.UserSession(user));
                if ("ADMIN".equals(user.getRole())) {
                    changeScene(event, "/com/travelxp/views/admin_dashboard.fxml");
                } else {
                    changeScene(event, "/com/travelxp/views/dashboard.fxml");
                }
            } else {
                showAlert("Error", "Login failed", "Invalid email or password.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database Error", "Failed to login: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterRedirect(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/register.fxml");
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        com.travelxp.utils.ThemeManager.toggleTheme(stage.getScene());
    }

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Scene Error", "Failed to load view: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
