package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmNewPassword = confirmNewPasswordField.getText();

        if (validateInput(currentPassword, newPassword, confirmNewPassword)) {
            try {
                if (userService.updatePassword(Main.getSession().getUser().getId(), currentPassword, newPassword)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password Changed", "Your password has been successfully updated.");
                    handleCancel(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Change Failed", "Incorrect current password.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/dashboard.fxml");
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        com.travelxp.utils.ThemeManager.toggleTheme(stage.getScene());
    }

    private boolean validateInput(String current, String newPwd, String confirm) {
        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Empty Fields", "Please fill in all fields.");
            return false;
        }

        if (!isValidPassword(newPwd)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Password", "New password must be at least 8 characters long, contain at least one letter and one number.");
            return false;
        }

        if (!newPwd.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password Mismatch", "New passwords do not match.");
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[a-zA-Z].*") && password.matches(".*[0-9].*");
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
            showAlert(Alert.AlertType.ERROR, "Error", "Scene Error", "Failed to load view: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
