package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Comment;
import com.travelxp.models.Feedback;
import com.travelxp.services.FeedbackService;
import com.travelxp.utils.ThemeManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class CommentsViewController {

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label feedbackContentLabel;

    @FXML
    private ComboBox<String> commentSortCombo;

    private Feedback currentFeedback;
    private FeedbackService feedbackService;
    private FeedbackController feedbackController;

    @FXML
    public void initialize() {
        feedbackService = new FeedbackService();
        if (commentSortCombo != null) {
            commentSortCombo.getItems().addAll("Date","Likes","Dislikes");
            commentSortCombo.setValue("Date");
        }
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
            String sortBy = "date";
            boolean asc = false;
            if (commentSortCombo != null) {
                String sel = commentSortCombo.getValue();
                if ("Date".equals(sel)) {
                    sortBy = "date";
                    asc = false; // newest first (original behavior)
                } else if ("Likes".equals(sel)) {
                    sortBy = "likes";
                    asc = false; // highest likes first
                } else if ("Dislikes".equals(sel)) {
                    sortBy = "dislikes";
                    asc = false; // highest dislikes first
                }
            }
            var comments = feedbackService.getCommentsByFeedback(currentFeedback.getId(), sortBy, asc);

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

        // Metadata - fetch username and make it bold
        String username = "Unknown User";
        try {
            String fetchedUsername = feedbackService.getCommentUsername(comment.getUserId());
            if (fetchedUsername != null && !fetchedUsername.trim().isEmpty()) {
                username = fetchedUsername;
            }
        } catch (SQLException e) {
            // Keep default username if fetch fails
        }
        
        Label metaLabel = new Label("User: " + username + " | Created: " + comment.getCreatedAt() + " | TZ: " + comment.getTimezone());
        metaLabel.getStyleClass().add("comment-meta-label");

        // Reactions
        HBox reactionBox = new HBox();
        reactionBox.setSpacing(6);
        Label likesLabel = new Label("👍 " + comment.getLikes());
        Label dislikesLabel = new Label("👎 " + comment.getDislikes());
        likesLabel.setStyle("-fx-cursor: hand;");
        dislikesLabel.setStyle("-fx-cursor: hand;");
        likesLabel.setOnMouseClicked(evt -> {
            try {
                feedbackService.toggleCommentReaction(comment.getId(), Main.getSession().getUser().getId(), "LIKE");
                loadComments();
            } catch (SQLException ex) {
                showError("Could not toggle like: " + ex.getMessage());
            }
        });
        dislikesLabel.setOnMouseClicked(evt -> {
            try {
                feedbackService.toggleCommentReaction(comment.getId(), Main.getSession().getUser().getId(), "DISLIKE");
                loadComments();
            } catch (SQLException ex) {
                showError("Could not toggle dislike: " + ex.getMessage());
            }
        });
        reactionBox.getChildren().addAll(likesLabel, dislikesLabel);

        // Translate button
        Button translateBtn = new Button("Translate");
        translateBtn.getStyleClass().addAll("button", "secondary-button");
        translateBtn.setOnAction(e -> showTranslateDialog(comment, contentLabel));

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

        card.getChildren().addAll(contentLabel, metaLabel, reactionBox, buttonBox);

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
        addLanguageToolButtons(content, textArea);
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
                    newComment.setTimezone(java.time.ZoneId.systemDefault().toString());
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
        addLanguageToolButtons(content, textArea);
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
    @FXML
    private void handleCommentReset() {
        if (commentSortCombo != null) commentSortCombo.setValue("Date");
        loadComments();
    }
    
    @FXML
    private void handleCommentSort() {
        loadComments();
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // helper to add grammar/translation buttons below a comment textarea
    private void addLanguageToolButtons(VBox container, TextArea textArea) {
        HBox toolBox = new HBox(5);
        Button grammarBtn = new Button("Check Grammar");
        Button translateBtn = new Button("Translate");
        toolBox.getChildren().addAll(grammarBtn, translateBtn);
        container.getChildren().add(toolBox);

        grammarBtn.setOnAction(e -> {
            String text = textArea.getText();
            try {
                String corrected = com.travelxp.utils.LanguageToolClient.checkGrammar(text);
                if (!corrected.equals(text)) {
                    textArea.setText(corrected);
                    showInfo("Grammar corrected.");
                } else {
                    showInfo("No grammar issues found.");
                }
            } catch (Exception ex) {
                showError("Grammar check failed: " + ex.getMessage());
            }
        });

        translateBtn.setOnAction(e -> {
            TextInputDialog langDialog = new TextInputDialog("en");
            langDialog.setTitle("Translate");
            langDialog.setHeaderText("Enter target language code (e.g. en, es, fr)");
            Optional<String> lang = langDialog.showAndWait();
            lang.ifPresent(l -> {
                try {
                    String translated = com.travelxp.utils.LibreTranslateClient.translate(textArea.getText(), l.trim());
                    textArea.setText(translated);
                    showInfo("Translation applied.");
                } catch (Exception ex) {
                    showError("Translation failed: " + ex.getMessage());
                }
            });
        });
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Translate dialog for comment content
    private void showTranslateDialog(Comment comment, Label contentLabel) {
        // Create dialog with language selection
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Translate Comment");
        dialog.setHeaderText("Select target language for translation");
        
        // Language options
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label instructionLabel = new Label("Choose a language to translate this comment to:");
        instructionLabel.setWrapText(true);
        
        // Language selection list
        ListView<String> languageList = new ListView<>();
        languageList.setPrefHeight(200);
        
        // Common languages with display names
        String[][] languages = {
            {"en", "English"},
            {"es", "Spanish"},
            {"fr", "French"},
            {"de", "German"},
            {"it", "Italian"},
            {"pt", "Portuguese"},
            {"ru", "Russian"},
            {"ja", "Japanese"},
            {"ko", "Korean"},
            {"zh", "Chinese"},
            {"ar", "Arabic"},
            {"hi", "Hindi"},
            {"nl", "Dutch"},
            {"sv", "Swedish"},
            {"da", "Danish"},
            {"no", "Norwegian"},
            {"fi", "Finnish"},
            {"pl", "Polish"},
            {"tr", "Turkish"},
            {"th", "Thai"}
        };
        
        for (String[] lang : languages) {
            languageList.getItems().add(lang[1] + " (" + lang[0] + ")");
        }
        
        languageList.getSelectionModel().select(0); // Default to English
        
        content.getChildren().addAll(instructionLabel, languageList);
        dialog.getDialogPane().setContent(content);
        
        ButtonType translateButton = new ButtonType("Translate", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(translateButton, cancelButton);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == translateButton) {
                String selected = languageList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Extract language code from "Name (code)" format
                    int start = selected.indexOf("(");
                    int end = selected.indexOf(")");
                    if (start != -1 && end != -1 && end > start) {
                        return selected.substring(start + 1, end);
                    }
                }
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetLang -> {
            try {
                String originalContent = comment.getContent();
                String translated = com.travelxp.utils.LibreTranslateClient.translate(originalContent, targetLang);
                
                // Create a separate window/panel for the translation
                Stage translationStage = new Stage();
                translationStage.setTitle("Translated Comment - " + getLanguageName(targetLang));
                translationStage.setResizable(false);
                
                VBox translationContent = new VBox(15);
                translationContent.setPadding(new Insets(20));
                translationContent.setSpacing(10);
                
                Label titleLabel = new Label("Translation Result");
                titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
                
                Label originalLabel = new Label("Original Text:");
                originalLabel.setStyle("-fx-font-weight: bold;");
                TextArea originalText = new TextArea(originalContent);
                originalText.setWrapText(true);
                originalText.setEditable(false);
                originalText.setPrefHeight(120);
                originalText.setPrefWidth(400);
                
                Label translatedLabel = new Label("Translated Text (" + getLanguageName(targetLang) + "):");
                translatedLabel.setStyle("-fx-font-weight: bold;");
                TextArea translatedText = new TextArea(translated);
                translatedText.setWrapText(true);
                translatedText.setEditable(false);
                translatedText.setPrefHeight(120);
                translatedText.setPrefWidth(400);
                
                Button exitBtn = new Button("Exit");
                exitBtn.getStyleClass().addAll("button", "primary-button");
                exitBtn.setOnAction(e -> translationStage.close());
                
                translationContent.getChildren().addAll(
                    titleLabel,
                    originalLabel, originalText, 
                    translatedLabel, translatedText,
                    exitBtn
                );
                
                Scene scene = new Scene(translationContent, 450, 400);
                ThemeManager.applyTheme(scene);
                translationStage.setScene(scene);
                translationStage.show();
                
            } catch (Exception ex) {
                showError("Translation failed: " + ex.getMessage());
            }
        });
    }
    
    // Helper method to get language display name
    private String getLanguageName(String code) {
        switch (code) {
            case "en": return "English";
            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "ru": return "Russian";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            case "zh": return "Chinese";
            case "ar": return "Arabic";
            case "hi": return "Hindi";
            case "nl": return "Dutch";
            case "sv": return "Swedish";
            case "da": return "Danish";
            case "no": return "Norwegian";
            case "fi": return "Finnish";
            case "pl": return "Polish";
            case "tr": return "Turkish";
            case "th": return "Thai";
            default: return code;
        }
    }
}
