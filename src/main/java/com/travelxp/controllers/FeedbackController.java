package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Feedback;
import com.travelxp.services.FeedbackService;
import com.travelxp.utils.ThemeManager;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class FeedbackController {

    @FXML private VBox feedbackContainer;
    @FXML private Pane animatedBg;

    private FeedbackService feedbackService;
    private final Random random = new Random();

    public void initialize() {
        feedbackService = new FeedbackService();
        loadFeedbacks();
        Platform.runLater(this::startBackgroundAnimation);
    }

    private void startBackgroundAnimation() {
        if (animatedBg == null) return;
        for (int i = 0; i < 10; i++) {
            Circle circle = createCircle();
            animatedBg.getChildren().add(circle);
            animateCircle(circle);
        }
    }

    private Circle createCircle() {
        double radius = 30 + random.nextDouble() * 120;
        Circle circle = new Circle(radius);
        circle.setCenterX(random.nextDouble() * 1200);
        circle.setCenterY(random.nextDouble() * 900);
        double opacity = 0.03 + random.nextDouble() * 0.05;
        boolean isDark = ThemeManager.isDark();
        String color = isDark ? "#D4AF37" : "#002b5c";
        circle.setFill(Color.web(color, opacity));
        circle.setStroke(Color.web(color, opacity * 1.5));
        circle.setStrokeWidth(1.5);
        circle.setEffect(new javafx.scene.effect.BoxBlur(10, 10, 2));
        return circle;
    }

    private void animateCircle(Circle circle) {
        double duration = 6 + random.nextDouble() * 6;
        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), circle);
        tt.setByX(random.nextDouble() * 500 - 250);
        tt.setByY(random.nextDouble() * 500 - 250);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        tt.play();
    }

    private void loadFeedbacks() {
        feedbackContainer.getChildren().clear();
        try {
            var feedbacks = feedbackService.getAllFeedback();
            if (feedbacks.isEmpty()) {
                Label emptyLabel = new Label("No feedbacks yet. Create one!");
                emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
                feedbackContainer.getChildren().add(emptyLabel);
            } else {
                for (Feedback feedback : feedbacks) {
                    feedbackContainer.getChildren().add(createFeedbackCard(feedback));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load feedbacks: " + e.getMessage());
        }
    }

    private VBox createFeedbackCard(Feedback feedback) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setSpacing(10);

        Label contentLabel = new Label(feedback.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("content-label");

        Label metaLabel = new Label("User ID: " + feedback.getUserId() + " | Created: " + feedback.getCreatedAt());
        metaLabel.getStyleClass().add("meta-label");

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().addAll("button", "secondary-button");
        updateBtn.setOnAction(e -> handleUpdate(feedback));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("button", "danger-button");
        deleteBtn.setOnAction(e -> handleDelete(feedback));

        Button commentsBtn = new Button("Comments");
        commentsBtn.getStyleClass().addAll("button", "primary-button");
        commentsBtn.setOnAction(e -> openCommentsView(feedback));

        int currentUserId = Main.getSession().getUser().getId();
        String role = Main.getSession().getUser().getRole();
        if (feedback.getUserId() == currentUserId || "ADMIN".equals(role)) {
            buttonBox.getChildren().addAll(commentsBtn, updateBtn, deleteBtn);
        } else {
            buttonBox.getChildren().addAll(commentsBtn);
        }

        card.getChildren().addAll(contentLabel, metaLabel, buttonBox);
        return card;
    }

    @FXML
    private void handleAddFeedback() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Feedback");
        dialog.setHeaderText("Create a new feedback");
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        textArea.setPromptText("Enter feedback content...");
        VBox content = new VBox(new Label("Feedback Content:"), textArea);
        content.setPadding(new Insets(10));
        content.setSpacing(10);
        dialog.getDialogPane().setContent(content);
        ButtonType okButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) return textArea.getText();
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(content_text -> {
            if (!content_text.trim().isEmpty()) {
                try {
                    Feedback newFeedback = new Feedback(content_text, Main.getSession().getUser().getId(), LocalDateTime.now());
                    feedbackService.createFeedback(newFeedback);
                    loadFeedbacks();
                } catch (SQLException e) {
                    showError("Failed to create feedback: " + e.getMessage());
                }
            }
        });
    }

    private void handleUpdate(Feedback feedback) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Feedback");
        dialog.setHeaderText("Edit feedback content");
        TextArea textArea = new TextArea();
        textArea.setText(feedback.getContent());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        VBox content = new VBox(new Label("Feedback Content:"), textArea);
        content.setPadding(new Insets(10));
        content.setSpacing(10);
        dialog.getDialogPane().setContent(content);
        ButtonType okButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) return textArea.getText();
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try {
                    feedback.setContent(newContent);
                    feedbackService.updateFeedback(feedback);
                    loadFeedbacks();
                } catch (SQLException e) {
                    showError("Failed to update feedback: " + e.getMessage());
                }
            }
        });
    }

    private void handleDelete(Feedback feedback) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Feedback");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will delete the feedback and all its comments.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    feedbackService.deleteFeedback(feedback.getId());
                    loadFeedbacks();
                } catch (SQLException e) {
                    showError("Failed to delete feedback: " + e.getMessage());
                }
            }
        });
    }

    private void openCommentsView(Feedback feedback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/comments-view.fxml"));
            BorderPane root = loader.load();
            CommentsViewController controller = loader.getController();
            controller.setFeedback(feedback);
            controller.setFeedbackController(this);
            controller.loadComments();
            Stage stage = new Stage();
            stage.setTitle("Comments - Feedback #" + feedback.getId());
            stage.setScene(new Scene(root, 700, 500));
            ThemeManager.applyTheme(stage.getScene());
            stage.show();
        } catch (IOException e) {
            showError("Failed to open comments view: " + e.getMessage());
        }
    }

    @FXML private void handleTasks(ActionEvent event) { changeScene(event, "/com/travelxp/views/tasks.fxml"); }
    @FXML private void handleBrowseProperties(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-property-view.fxml" : "/com/travelxp/views/property-view.fxml");
    }
    @FXML private void handleBrowseTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(false);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void handleMyBookings(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-booking-view.fxml" : "/com/travelxp/views/booking-view.fxml");
    }
    @FXML private void handleMyTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(true);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void handleEditProfile(ActionEvent event) { changeScene(event, "/com/travelxp/views/edit_profile.fxml"); }
    @FXML private void handleChangePassword(ActionEvent event) { changeScene(event, "/com/travelxp/views/change_password.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { Main.setSession(null); changeScene(event, "/com/travelxp/views/login.fxml"); }
    @FXML private void handleBack(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin_dashboard.fxml" : "/com/travelxp/views/dashboard.fxml");
    }
    @FXML private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
    }
    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
