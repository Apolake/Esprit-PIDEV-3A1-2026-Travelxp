package com.travelxp.controllers;

import com.travelxp.models.User;
import com.travelxp.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextArea bioArea;
    @FXML private Label imagePathLabel;

    private String selectedImagePath = "";
    private final UserService userService = new UserService();

    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            imagePathLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate birthday = birthdayPicker.getValue();
        String bio = bioArea.getText();

        if (validateInput(username, email, password, confirmPassword, birthday, bio)) {
            User user = new User(username, email, password, birthday, bio, selectedImagePath);
            try {
                if (userService.registerUser(user)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful!", "You can now login.");
                    handleBack(event);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Registration failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/login.fxml");
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        com.travelxp.utils.ThemeManager.toggleTheme(stage.getScene());
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword, LocalDate birthday, String bio) {
        if (username.length() < 3 || username.length() > 50) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Username", "Username must be between 3 and 50 characters.");
            return false;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Email", "Please enter a valid email format.");
            return false;
        }

        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Password", "Password must be at least 8 characters long, contain at least one letter and one number.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password Mismatch", "Passwords do not match.");
            return false;
        }

        if (birthday == null || birthday.isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Birthday", "Please select a valid past date for your birthday.");
            return false;
        }

        if (bio != null && bio.length() > 500) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Bio", "Bio must be max 500 characters.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
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
