package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Feedback;
import com.travelxp.services.FeedbackService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class FeedbackController {

    @FXML
    private VBox feedbackContainer;

    private FeedbackService feedbackService;
    private Stage primaryStage;

    public void initialize() {
        feedbackService = new FeedbackService();
        loadFeedbacks();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
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
        card.getStyleClass().add("feedback-card");
        card.setSpacing(10);

        // Content
        Label contentLabel = new Label(feedback.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("content-label");

        // Metadata
        Label metaLabel = new Label("User ID: " + feedback.getUserId() + " | Created: " + feedback.getCreatedAt());
        metaLabel.getStyleClass().add("meta-label");

        // Buttons
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

        // Only show update/delete if it's the user's feedback or if user is admin
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
            if (dialogButton == okButton) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(content_text -> {
            if (!content_text.trim().isEmpty()) {
                try {
                    Feedback newFeedback = new Feedback(content_text, Main.getSession().getUser().getId(), LocalDateTime.now());
                    feedbackService.createFeedback(newFeedback);
                    loadFeedbacks();
                    showInfo("Feedback created successfully!");
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
            if (dialogButton == okButton) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try {
                    feedback.setContent(newContent);
                    feedbackService.updateFeedback(feedback);
                    loadFeedbacks();
                    showInfo("Feedback updated successfully!");
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

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                feedbackService.deleteFeedback(feedback.getId());
                loadFeedbacks();
                showInfo("Feedback deleted successfully!");
            } catch (SQLException e) {
                showError("Failed to delete feedback: " + e.getMessage());
            }
        }
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
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
            stage.show();
        } catch (IOException e) {
            showError("Failed to open comments view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        try {
            String fxml = "/com/travelxp/views/dashboard.fxml";
            if (Main.getSession().getUser().getRole().equals("ADMIN")) {
                fxml = "/com/travelxp/views/admin_dashboard.fxml";
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }
}
