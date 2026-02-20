package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Gamification;
import com.travelxp.models.User;
import com.travelxp.services.GamificationService;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Random;

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
    @FXML private Pane animatedBg;

    private final GamificationService gamificationService = new GamificationService();
    private final Random random = new Random();

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
