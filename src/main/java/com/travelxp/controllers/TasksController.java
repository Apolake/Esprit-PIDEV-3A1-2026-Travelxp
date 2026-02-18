package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.utils.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class TasksController {

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/dashboard.fxml");
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/edit_profile.fxml");
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/change_password.fxml");
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Main.setSession(null);
        changeScene(event, "/com/travelxp/views/login.fxml");
    }

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
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
