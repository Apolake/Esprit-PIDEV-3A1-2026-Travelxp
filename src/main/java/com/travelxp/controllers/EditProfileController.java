package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.User;
import com.travelxp.services.UserService;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;
import java.util.regex.Pattern;

public class EditProfileController {

    @FXML private TextField emailField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextArea bioArea;
    @FXML private Label imagePathLabel;
    @FXML private Pane animatedBg;

    private String selectedImagePath = "";
    private final UserService userService = new UserService();
    private User currentUser;
    private final Random random = new Random();

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
        Platform.runLater(this::startBackgroundAnimation);
    }

    private void startBackgroundAnimation() {
        if (animatedBg == null) return;
        
        // Fewer, more intentional circles for a cleaner look
        for (int i = 0; i < 10; i++) {
            Circle circle = createCircle();
            animatedBg.getChildren().add(circle);
            animateCircle(circle);
        }
    }

    private Circle createCircle() {
        // Varied sizes for depth
        double radius = 30 + random.nextDouble() * 120;
        Circle circle = new Circle(radius);
        
        circle.setCenterX(random.nextDouble() * 1200);
        circle.setCenterY(random.nextDouble() * 900);
        
        // Dynamic opacity for professional layering
        double opacity = 0.03 + random.nextDouble() * 0.05;
        
        // Check current theme state at creation time
        boolean isDark = com.travelxp.utils.ThemeManager.isDark();
        String color = isDark ? "#D4AF37" : "#002b5c";
        
        circle.setFill(Color.web(color, opacity));
        circle.setStroke(Color.web(color, opacity * 1.5));
        circle.setStrokeWidth(1.5);
        
        // Add a slight blur to the circles themselves
        circle.setEffect(new javafx.scene.effect.BoxBlur(10, 10, 2));
        
        return circle;
    }

    private void animateCircle(Circle circle) {
        // Faster duration: 6 to 12 seconds
        double duration = 6 + random.nextDouble() * 6;
        
        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), circle);
        tt.setByX(random.nextDouble() * 500 - 250);
        tt.setByY(random.nextDouble() * 500 - 250);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        // Smooth easing is key for professionalism
        tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            String relativePath = com.travelxp.utils.ImageUtil.saveImage(selectedFile);
            if (relativePath != null) {
                selectedImagePath = relativePath;
                imagePathLabel.setText(new File(selectedImagePath).getName());
            }
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
        String fxml = "/com/travelxp/views/dashboard.fxml";
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            fxml = "/com/travelxp/views/admin_dashboard.fxml";
        }
        changeScene(event, fxml);
    }

    @FXML
    private void handleTasks(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/tasks.fxml");
    }

    @FXML
    private void handleBrowseProperties(ActionEvent event) {
        String fxml = "/com/travelxp/views/property-view.fxml";
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            fxml = "/com/travelxp/views/admin-property-view.fxml";
        }
        changeScene(event, fxml);
    }

    @FXML
    private void handleMyBookings(ActionEvent event) {
        String fxml = "/com/travelxp/views/booking-view.fxml";
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            fxml = "/com/travelxp/views/admin-booking-view.fxml";
        }
        changeScene(event, fxml);
    }

    @FXML
    private void handleBrowseTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(false);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(true);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActivities(ActionEvent event) {
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/change_password.fxml");
    }

    @FXML
    private void handleFeedback(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/feedback-view.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Main.setSession(null);
        changeScene(event, "/com/travelxp/views/login.fxml");
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
