package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Comment;
import com.travelxp.models.Feedback;
import com.travelxp.services.FeedbackService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class CommentsViewController {

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label feedbackContentLabel;

    private Feedback currentFeedback;
    private FeedbackService feedbackService;
    private FeedbackController feedbackController;

    @FXML
    public void initialize() {
        feedbackService = new FeedbackService();
    }

    public void setFeedback(Feedback feedback) {
        this.currentFeedback = feedback;
        feedbackContentLabel.setText(feedback.getContent());
    }

    public void setFeedbackController(FeedbackController feedbackController) {
        this.feedbackController = feedbackController;
    }

    public void loadComments() {
        commentsContainer.getChildren().clear();

        try {
            var comments = feedbackService.getCommentsByFeedback(currentFeedback.getId());

            if (comments.isEmpty()) {
                Label emptyLabel = new Label("No comments yet. Add one!");
                emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
                commentsContainer.getChildren().add(emptyLabel);
            } else {
                for (Comment comment : comments) {
                    commentsContainer.getChildren().add(createCommentCard(comment));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load comments: " + e.getMessage());
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox();
        card.getStyleClass().add("comment-card");
        card.setSpacing(8);

        // Comment content
        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("content-label");

        // Metadata
        Label metaLabel = new Label("User ID: " + comment.getUserId() + " | Created: " + comment.getCreatedAt());
        metaLabel.getStyleClass().add("meta-label");

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(8);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button updateBtn = new Button("Edit");
        updateBtn.getStyleClass().addAll("button", "secondary-button");
        updateBtn.setOnAction(e -> handleUpdateComment(comment));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("button", "danger-button");
        deleteBtn.setOnAction(e -> handleDeleteComment(comment));

        // Only show update/delete if it's the user's comment or if user is admin
        int currentUserId = Main.getSession().getUser().getId();
        String role = Main.getSession().getUser().getRole();
        if (comment.getUserId() == currentUserId || "ADMIN".equals(role)) {
            buttonBox.getChildren().addAll(updateBtn, deleteBtn);
        }

        card.getChildren().addAll(contentLabel, metaLabel, buttonBox);

        return card;
    }

    @FXML
    private void handleAddComment() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Comment");
        dialog.setHeaderText("Write a comment");

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefRowCount(4);
        textArea.setPromptText("Enter comment text...");

        VBox content = new VBox(new Label("Comment:"), textArea);
        content.setPadding(new Insets(10));
        content.setSpacing(10);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(commentText -> {
            if (!commentText.trim().isEmpty()) {
                try {
                    Comment newComment = new Comment(
                        currentFeedback.getId(),
                        Main.getSession().getUser().getId(),
                        commentText,
                        LocalDateTime.now()
                    );
                    feedbackService.addComment(newComment);
                    loadComments();
                    showInfo("Comment added successfully!");
                } catch (SQLException e) {
                    showError("Failed to add comment: " + e.getMessage());
                }
            }
        });
    }

    private void handleUpdateComment(Comment comment) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Comment");
        dialog.setHeaderText("Edit comment");

        TextArea textArea = new TextArea();
        textArea.setText(comment.getContent());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(4);

        VBox content = new VBox(new Label("Comment:"), textArea);
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
                    comment.setContent(newContent);
                    feedbackService.updateComment(comment);
                    loadComments();
                    showInfo("Comment updated successfully!");
                } catch (SQLException e) {
                    showError("Failed to update comment: " + e.getMessage());
                }
            }
        });
    }

    private void handleDeleteComment(Comment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Comment");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This comment will be permanently deleted.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                feedbackService.deleteComment(comment.getId());
                loadComments();
                showInfo("Comment deleted successfully!");
            } catch (SQLException e) {
                showError("Failed to delete comment: " + e.getMessage());
            }
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
}
