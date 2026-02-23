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
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class EditProfileController {

    @FXML private TextField emailField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextArea bioArea;
    @FXML private Label imagePathLabel;

    private String selectedImagePath = "";
    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = Main.getSession().getUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            birthdayPicker.setValue(currentUser.getBirthday());
            bioArea.setText(currentUser.getBio());
            selectedImagePath = currentUser.getProfileImage();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                imagePathLabel.setText(new File(selectedImagePath).getName());
            }
        }
    }

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
    private void handleUpdate(ActionEvent event) {
        String newEmail = emailField.getText();
        LocalDate newBirthday = birthdayPicker.getValue();
        String newBio = bioArea.getText();

        if (validateInput(newEmail, newBirthday, newBio)) {
            try {
                boolean success = true;
                
                if (!newEmail.equals(currentUser.getEmail())) {
                    success &= userService.updateEmail(currentUser.getId(), newEmail);
                }
                
                if (!newBirthday.equals(currentUser.getBirthday())) {
                    success &= userService.updateBirthday(currentUser.getId(), newBirthday);
                }
                
                if (newBio != null && !newBio.equals(currentUser.getBio())) {
                    success &= userService.updateBio(currentUser.getId(), newBio);
                }
                
                if (selectedImagePath != null && !selectedImagePath.equals(currentUser.getProfileImage())) {
                    success &= userService.updateProfileImage(currentUser.getId(), selectedImagePath);
                }

                if (success) {
                    // Refresh session user data
                    Main.getSession().setUser(userService.getUserById(currentUser.getId()));
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile Updated", "Your profile changes have been saved.");
                    handleCancel(event);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage());
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

    private boolean validateInput(String email, LocalDate birthday, String bio) {
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Email", "Please enter a valid email format.");
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
