package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.User;
import com.travelxp.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label birthdayLabel;
    @FXML private Label bioLabel;
    @FXML private Label joinedLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        User user = Main.getSession().getUser();
        if (user != null) {
            updateUI(user);
        }
    }

    private void updateUI(User user) {
        usernameLabel.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
        birthdayLabel.setText(user.getBirthday().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        bioLabel.setText(user.getBio() != null ? user.getBio() : "No bio yet.");
        joinedLabel.setText(user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                java.io.File file = new java.io.File(user.getProfileImage());
                if (file.exists()) {
                    profileImageView.setImage(new Image(file.toURI().toString()));
                }
            } catch (Exception e) {
                // Fallback or log error
            }
        }
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
    private void handleDeleteAccount(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Account");
        dialog.setHeaderText("Are you sure you want to delete your account?");
        dialog.setContentText("Please enter your password to confirm:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String password = result.get();
            try {
                if (userService.deleteUser(Main.getSession().getUser().getId(), password)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Account Deleted", "Your account has been successfully deleted.");
                    handleLogout(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to Delete", "Incorrect password.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Database Error", e.getMessage());
            }
        }
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
