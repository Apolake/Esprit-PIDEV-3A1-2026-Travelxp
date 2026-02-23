package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Gamification;
import com.travelxp.models.Property;
import com.travelxp.models.User;
import com.travelxp.services.GamificationService;
import com.travelxp.services.PropertyService;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    
    @FXML private VBox propertyShowcase;
    @FXML private ScrollPane propertyScrollPane;

    private final GamificationService gamificationService = new GamificationService();
    private final PropertyService propertyService = new PropertyService();
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

        loadPropertyShowcase();
        Platform.runLater(this::startBackgroundAnimation);
    }

    private void loadPropertyShowcase() {
        if (propertyShowcase == null) return;
        propertyShowcase.getChildren().clear();
        try {
            List<Property> properties = propertyService.getAllProperties();
            for (Property p : properties) {
                propertyShowcase.getChildren().add(createPropertyCard(p));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPropertyCard(Property p) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(20));

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Image Preview
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(120, 120);
        imageContainer.getStyleClass().add("profile-image-container");
        ImageView iv = new ImageView();
        iv.setFitHeight(110);
        iv.setFitWidth(110);
        iv.setPreserveRatio(true);
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            File imgFile = new File(p.getImages());
            if (imgFile.exists()) {
                iv.setImage(new Image(imgFile.toURI().toString()));
            }
        }
        imageContainer.getChildren().add(iv);

        VBox textInfo = new VBox(5);
        Label title = new Label(p.getTitle());
        title.getStyleClass().add("title-4");
        title.setWrapText(true);

        Label location = new Label(p.getCity() + ", " + p.getCountry());
        location.getStyleClass().add("text-muted");
        
        textInfo.getChildren().addAll(title, location);
        HBox.setHgrow(textInfo, Priority.ALWAYS);

        VBox priceInfo = new VBox(5);
        priceInfo.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label price = new Label("$" + p.getPricePerNight());
        price.getStyleClass().add("accent");
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        Label night = new Label("/ night");
        night.getStyleClass().add("text-muted");
        priceInfo.getChildren().addAll(price, night);

        mainContent.getChildren().addAll(imageContainer, textInfo, priceInfo);

        Button viewBtn = new Button("Book This Property");
        viewBtn.getStyleClass().add("accent");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> handleBrowseProperties(e));

        card.getChildren().addAll(mainContent, viewBtn);
        return card;
    }

    @FXML
    private void scrollPropertiesUp() {
        propertyScrollPane.setVvalue(Math.max(0, propertyScrollPane.getVvalue() - 0.2));
    }

    @FXML
    private void scrollPropertiesDown() {
        propertyScrollPane.setVvalue(Math.min(1, propertyScrollPane.getVvalue() + 0.2));
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
    private void handleFeedback(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/feedback-view.fxml");
    }

    @FXML
    private void handleBrowseProperties(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/property-view.fxml");
    }

    @FXML
    private void handleMyBookings(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/booking-view.fxml");
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
