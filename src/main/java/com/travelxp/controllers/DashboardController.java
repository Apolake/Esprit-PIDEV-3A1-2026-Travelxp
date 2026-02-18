package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Gamification;
import com.travelxp.models.User;
import com.travelxp.services.GamificationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ImageView profileImageView;
    @FXML private Label emailLabel;
    @FXML private Label birthdayLabel;
    @FXML private Label bioLabel;
    @FXML private Label titleLabel;
    @FXML private Label levelLabel;
    @FXML private Label xpLabel;
    @FXML private ProgressBar xpProgressBar;

    private final GamificationService gamificationService = new GamificationService();

    @FXML
    public void initialize() {
        // Apply circular clip
        Circle clip = new Circle(60, 60, 60); // Center X, Y, Radius
        profileImageView.setClip(clip);
        
        User user = Main.getSession().getUser();
        if (user != null) {
            updateProfileUI(user);
            updateGamificationUI(user.getId());
        }
    }

    private void updateProfileUI(User user) {
        welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
        emailLabel.setText(user.getEmail());
        birthdayLabel.setText(user.getBirthday().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        bioLabel.setText(user.getBio() != null ? user.getBio() : "No bio yet.");
        
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                File file = new File(user.getProfileImage());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    profileImageView.setImage(image);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void updateGamificationUI(int userId) {
        try {
            Gamification gamification = gamificationService.getGamificationByUserId(userId);
            if (gamification != null) {
                titleLabel.setText(gamification.getTitle());
                levelLabel.setText(String.valueOf(gamification.getLevel()));
                
                int currentLevel = gamification.getLevel();
                int currentXp = gamification.getXp();
                int nextLevelXp = gamificationService.getXpForNextLevel(currentLevel);
                // Calculate previous level threshold to show progress within current level
                int prevLevelXp = (currentLevel > 1) ? gamificationService.getXpForNextLevel(currentLevel - 1) : 0;
                
                // Progress is (currentXp - prevLevelXp) / (nextLevelXp - prevLevelXp)
                // But simplified: just show total XP or progress to next level?
                // The requirements say "XP: 120 / 200". Let's assume total XP vs threshold.
                
                xpLabel.setText(currentXp + " / " + nextLevelXp + " XP");
                
                double progress;
                if (currentLevel == 1) {
                    progress = (double) currentXp / nextLevelXp;
                } else {
                    double range = nextLevelXp - prevLevelXp;
                    double gainedInLevel = currentXp - prevLevelXp;
                    progress = gainedInLevel / range;
                }
                
                xpProgressBar.setProgress(progress);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTasks(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/tasks.fxml");
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
        com.travelxp.utils.ThemeManager.toggleTheme(stage.getScene());
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
