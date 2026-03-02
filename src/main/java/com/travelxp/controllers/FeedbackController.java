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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class FeedbackController {

    @FXML private VBox feedbackContainer;
    @FXML private Pane animatedBg;
    @FXML private TextField usernameFilterField;
    @FXML private TextField titleFilterField;
    @FXML private ComboBox<String> sortCombo;

    private FeedbackService feedbackService;
    private final Random random = new Random();

    public void initialize() {
        feedbackService = new FeedbackService();
        // init sort options
        if (sortCombo != null) {
            sortCombo.getItems().addAll("Date", "Likes", "Dislikes");
            sortCombo.setValue("Date");
        }
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
            String uname = usernameFilterField != null ? usernameFilterField.getText().trim() : null;
            String title = titleFilterField != null ? titleFilterField.getText().trim() : null;
            String sortBy = "date";
            boolean asc = false;
            if (sortCombo != null) {
                String sel = sortCombo.getValue();
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
            var feedbacks = feedbackService.searchFeedback(uname, title, sortBy, asc);
            
            // Check favorite status for current user
            int currentUserId = Main.getSession().getUser().getId();
            for (Feedback feedback : feedbacks) {
                try {
                    boolean isFavorited = feedbackService.isFavorited(feedback.getId(), currentUserId);
                    feedback.setFavorited(isFavorited);
                } catch (SQLException ex) {
                    // If checking fails, assume not favorited
                    feedback.setFavorited(false);
                }
            }
            
            if (feedbacks.isEmpty()) {
                Label emptyLabel = new Label("No feedbacks yet. Create one!");
                emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
                feedbackContainer.getChildren().add(emptyLabel);
            } else {
                for (Feedback feedback : feedbacks) {
                    // Calculate sentiment based on current likes/dislikes ratio
                    feedback.updateSentiment();
                    feedbackContainer.getChildren().add(createFeedbackCard(feedback));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load feedbacks: " + e.getMessage());
        }
    }

    private VBox createFeedbackCard(Feedback feedback) {
        try {
            VBox card = new VBox();
            card.getStyleClass().add("card");
            card.setSpacing(10);
            Label titleLabel = new Label(feedback.getTitle() != null && !feedback.getTitle().isEmpty() ? feedback.getTitle() : "(no title)");
            titleLabel.getStyleClass().add("feedback-title");

            Label contentLabel = new Label(feedback.getContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("content-label");

        // optional image
        if (feedback.getImageUrl() != null && !feedback.getImageUrl().isEmpty()) {
            try {
                // Construct the full path to the uploaded image
                String imagePath = "uploads/" + feedback.getImageUrl();
                System.out.println("Attempting to load image: " + imagePath);
                
                // Check if file exists first
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    System.err.println("Image file does not exist: " + imagePath);
                    // Continue without adding the image
                } else {
                    // Load image with proper URL format
                    String imageUrl = imageFile.toURI().toString();
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imageUrl);
                    ImageView img = new ImageView(image);
                    img.setFitWidth(200);
                    img.setPreserveRatio(true);
                    img.setSmooth(true); // Better image quality
                    img.setCache(true);  // Better performance
                    
                    // Check if image loaded successfully
                    if (image.getException() == null && image.getWidth() > 0 && image.getHeight() > 0) {
                        card.getChildren().add(img);
                        System.out.println("Successfully loaded image for feedback " + feedback.getId());
                    } else {
                        System.err.println("Failed to load image: " + imagePath + " - " + (image.getException() != null ? image.getException().getMessage() : "Invalid dimensions"));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image for feedback " + feedback.getId() + ": " + e.getMessage());
                e.printStackTrace();
                // Continue without adding the image
            }
        }

            String userInfo = feedback.getUsername() != null ? feedback.getUsername() : "ID:"+feedback.getUserId();
            Label metaLabel = new Label("By " + userInfo + " | Created: " + feedback.getCreatedAt() + " | Status: " + feedback.getStatus() + " | Favorites: " + feedback.getFavoriteCount());
            metaLabel.getStyleClass().add("feedback-meta-label");

            // Sentiment label
            Label sentimentLabel = new Label(feedback.getSentiment() != null ? feedback.getSentiment() : "NEUTRAL");
            sentimentLabel.getStyleClass().add("sentiment-label");
            
            // Set color based on sentiment
            String sentiment = feedback.getSentiment() != null ? feedback.getSentiment() : "NEUTRAL";
            if ("POSITIVE".equals(sentiment)) {
                sentimentLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-background-color: #d4edda; -fx-padding: 4px 8px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            } else if ("NEGATIVE".equals(sentiment)) {
                sentimentLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-background-color: #f8d7da; -fx-padding: 4px 8px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            } else {
                sentimentLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold; -fx-background-color: #e9ecef; -fx-padding: 4px 8px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            }

            HBox reactionBox = new HBox();
            reactionBox.setSpacing(10);
            Label likesLabel = new Label("👍 " + feedback.getLikes());
            Label dislikesLabel = new Label("👎 " + feedback.getDislikes());
            Label favoriteLabel = new Label("⭐ " + feedback.getFavoriteCount());
            
            // make them clickable for reactions
            likesLabel.setStyle("-fx-cursor: hand;");
            dislikesLabel.setStyle("-fx-cursor: hand;");
            favoriteLabel.setStyle("-fx-cursor: hand;");
            
            likesLabel.setOnMouseClicked(evt -> {
                try {
                    feedbackService.toggleFeedbackReaction(feedback.getId(), Main.getSession().getUser().getId(), "LIKE");
                    loadFeedbacks();
                } catch (SQLException ex) {
                    showError("Could not toggle like: " + ex.getMessage());
                }
            });
            dislikesLabel.setOnMouseClicked(evt -> {
                try {
                    feedbackService.toggleFeedbackReaction(feedback.getId(), Main.getSession().getUser().getId(), "DISLIKE");
                    loadFeedbacks();
                } catch (SQLException ex) {
                    showError("Could not toggle dislike: " + ex.getMessage());
                }
            });
            
            // Favorite toggle
            favoriteLabel.setOnMouseClicked(evt -> {
                try {
                    feedbackService.toggleFavorite(feedback.getId(), Main.getSession().getUser().getId());
                    loadFeedbacks();
                } catch (SQLException ex) {
                    showError("Could not toggle favorite: " + ex.getMessage());
                }
            });

            // Add enhanced features buttons
            Button grammarBtn = new Button("🔍 Grammar Check");
            grammarBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 4 8; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11;");
            grammarBtn.setOnAction(e -> checkGrammarOnCard(feedback));
            
            Button translateBtn = new Button("🌐 Translate");
            translateBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 4 8; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11;");
            translateBtn.setOnAction(e -> showTranslationDialog(feedback));
            
            Button pdfBtn = new Button("📄 Download PDF");
            pdfBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 4 8; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11;");
            pdfBtn.setOnAction(e -> downloadFeedbackAsPDF(feedback));

            reactionBox.getChildren().addAll(likesLabel, dislikesLabel, favoriteLabel, grammarBtn, translateBtn, pdfBtn);
            
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
            
            buttonBox.getChildren().add(commentsBtn);
            
            if (feedback.getUserId() == currentUserId || "ADMIN".equals(role)) {
                buttonBox.getChildren().addAll(updateBtn, deleteBtn);
            }

            card.getChildren().addAll(titleLabel, contentLabel, metaLabel, sentimentLabel, reactionBox, buttonBox);
            return card;
        } catch (Exception e) {
            // Fallback to basic card if error
            VBox card = new VBox();
            card.getStyleClass().add("card");
            card.setSpacing(10);
            Label contentLabel = new Label(feedback.getContent());
            contentLabel.setWrapText(true);
            card.getChildren().add(contentLabel);
            return card;
        }
    }

    @FXML
    private void handleSearch() {
        loadFeedbacks();
    }

    @FXML
    private void handleReset() {
        if (usernameFilterField != null) usernameFilterField.clear();
        if (titleFilterField != null) titleFilterField.clear();
        if (sortCombo != null) sortCombo.setValue("Date");
        loadFeedbacks();
    }

    @FXML private void handleFavorites() {
        try {
            String sortBy = "date";
            boolean asc = false;
            if (sortCombo != null) {
                String sel = sortCombo.getValue();
                if ("Date".equals(sel)) {
                    sortBy = "date";
                    asc = false; // newest first
                } else if ("Likes".equals(sel)) {
                    sortBy = "likes";
                    asc = false; // highest likes first
                } else if ("Dislikes".equals(sel)) {
                    sortBy = "dislikes";
                    asc = false; // highest dislikes first
                }
            }
            var favoritedFeedbacks = feedbackService.getFavoritedFeedbacks(Main.getSession().getUser().getId(), sortBy, asc);
            
            feedbackContainer.getChildren().clear();
            if (favoritedFeedbacks.isEmpty()) {
                Label emptyLabel = new Label("No favorited feedbacks yet. Start favoriting feedbacks!");
                emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
                feedbackContainer.getChildren().add(emptyLabel);
            } else {
                for (Feedback feedback : favoritedFeedbacks) {
                    feedbackContainer.getChildren().add(createFeedbackCard(feedback));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load favorited feedbacks: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddFeedback() {
        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Add New Feedback");
        dialog.setHeaderText("Create a new feedback");
        
        TextField titleField = new TextField();
        titleField.setPromptText("Enter feedback title...");
        
        // Image upload components
        HBox imageBox = new HBox(10);
        imageBox.setStyle("-fx-alignment: center-left;");
        Label imageLabel = new Label("Image:");
        Button uploadBtn = new Button("Upload Image");
        Label filePathLabel = new Label("No image selected");
        filePathLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        String[] selectedImagePath = {null};
        
        imageBox.getChildren().addAll(imageLabel, uploadBtn, filePathLabel);
        
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        textArea.setPromptText("Enter feedback content...");
        
        VBox content = new VBox(
                new Label("Title:"), titleField,
                imageBox,
                new Label("Feedback Content:"), textArea);
        content.setPadding(new Insets(10));
        content.setSpacing(10);
        addLanguageToolButtons(content, textArea);
        dialog.getDialogPane().setContent(content);
        
        ButtonType okButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
        
        // Image upload functionality
        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                try {
                    String fileName = generateFileName(selectedFile.getName());
                    File uploadsDir = new File("uploads");
                    if (!uploadsDir.exists()) {
                        uploadsDir.mkdirs();
                    }
                    File destFile = new File(uploadsDir, fileName);
                    
                    java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), 
                                           java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    selectedImagePath[0] = fileName;
                    filePathLabel.setText("Selected: " + fileName);
                    filePathLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                } catch (Exception ex) {
                    showError("Failed to upload image: " + ex.getMessage());
                }
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                String title = titleField.getText().trim();
                String feedbackContent = textArea.getText().trim();
                if (feedbackContent.isEmpty()) {
                    showError("Feedback content cannot be empty");
                    return null;
                }
                Feedback feedback = new Feedback(title, feedbackContent, Main.getSession().getUser().getId(), LocalDateTime.now());
                feedback.setImageUrl(selectedImagePath[0]);
                return feedback;
            }
            return null;
        });
        
        Optional<Feedback> result = dialog.showAndWait();
        result.ifPresent(newFeedback -> {
            try {
                feedbackService.createFeedback(newFeedback);
                loadFeedbacks();
                showInfo("Feedback created successfully!");
            } catch (SQLException e) {
                showError("Failed to create feedback: " + e.getMessage());
            }
        });
    }

    // Helper method to generate unique filename
    private String generateFileName(String originalName) {
        String extension = "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalName.substring(lastDot);
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "image_" + timestamp + extension;
    }

    private void handleUpdate(Feedback feedback) {
        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Update Feedback");
        dialog.setHeaderText("Edit feedback content");
        
        TextField titleField = new TextField();
        titleField.setText(feedback.getTitle());
        
        // Image upload components for update
        HBox imageBox = new HBox(10);
        imageBox.setStyle("-fx-alignment: center-left;");
        Label imageLabel = new Label("Image:");
        Button uploadBtn = new Button("Upload Image");
        Label filePathLabel = new Label(feedback.getImageUrl() != null && !feedback.getImageUrl().isEmpty() 
            ? "Current: " + feedback.getImageUrl() 
            : "No image selected");
        filePathLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        
        imageBox.getChildren().addAll(imageLabel, uploadBtn, filePathLabel);
        
        TextArea textArea = new TextArea();
        textArea.setText(feedback.getContent());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        
        VBox dialogContent = new VBox(new Label("Title:"), titleField, imageBox,
                                new Label("Feedback Content:"), textArea);
        dialogContent.setPadding(new Insets(10));
        dialogContent.setSpacing(10);
        addLanguageToolButtons(dialogContent, textArea);
        dialog.getDialogPane().setContent(dialogContent);
        
        ButtonType okButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
        
        // Image upload functionality for update
        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                try {
                    String fileName = generateFileName(selectedFile.getName());
                    File uploadsDir = new File("uploads");
                    if (!uploadsDir.exists()) {
                        uploadsDir.mkdirs();
                    }
                    File destFile = new File(uploadsDir, fileName);
                    
                    java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), 
                                           java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    filePathLabel.setText("Selected: " + fileName);
                    filePathLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                } catch (Exception ex) {
                    showError("Failed to upload image: " + ex.getMessage());
                }
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                String title = titleField.getText().trim();
                String feedbackContent = textArea.getText().trim();
                String imageUrl = feedback.getImageUrl(); // Keep existing image by default
                
                // Extract filename from the label text if a new image was uploaded
                String labelText = filePathLabel.getText();
                if (labelText != null && labelText.startsWith("Selected: ")) {
                    imageUrl = labelText.substring(10); // Remove "Selected: " prefix
                }
                
                if (feedbackContent.isEmpty()) {
                    showError("Feedback content cannot be empty");
                    return null;
                }
                
                feedback.setTitle(title);
                feedback.setImageUrl(imageUrl);
                feedback.setContent(feedbackContent);
                return feedback;
            }
            return null;
        });
        
        Optional<Feedback> result = dialog.showAndWait();
        result.ifPresent(updatedFeedback -> {
            try {
                feedbackService.updateFeedback(updatedFeedback);
                loadFeedbacks();
                showInfo("Feedback updated successfully!");
            } catch (SQLException e) {
                showError("Failed to update feedback: " + e.getMessage());
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


    private void addLanguageToolButtons(VBox vbox, TextArea textArea) {
        HBox toolsBox = new HBox(10);
        toolsBox.setStyle("-fx-alignment: center-left;");
        
        // Grammar Check Button
        Button grammarBtn = new Button("🔍 Check Grammar");
        grammarBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5 10; -fx-border-radius: 4; -fx-background-radius: 4;");
        grammarBtn.setOnAction(e -> {
            try {
                String originalText = textArea.getText();
                String correctedText = com.travelxp.utils.LanguageToolClient.checkGrammar(originalText);
                if (!correctedText.equals(originalText)) {
                    textArea.setText(correctedText);
                    showInfo("Grammar check completed. Text has been corrected.");
                } else {
                    showInfo("No grammar issues found.");
                }
            } catch (Exception ex) {
                showError("Grammar check failed: " + ex.getMessage());
            }
        });
        
        // Translate Button
        Button translateBtn = new Button("🌐 Translate to French");
        translateBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 5 10; -fx-border-radius: 4; -fx-background-radius: 4;");
        translateBtn.setOnAction(e -> {
            try {
                String originalText = textArea.getText();
                String translatedText = translateText(originalText, "fr");
                if (translatedText != null && !translatedText.equals(originalText)) {
                    // Show translation in a dialog
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Translated Text");
                    dialog.setHeaderText("Translation to French");
                    
                    TextArea translatedArea = new TextArea(translatedText);
                    translatedArea.setWrapText(true);
                    translatedArea.setEditable(false);
                    translatedArea.setPrefRowCount(10);
                    translatedArea.setPrefColumnCount(50);
                    
                    VBox content = new VBox(new Label("Original:"), textArea, new Label("Translated:"), translatedArea);
                    content.setSpacing(10);
                    content.setPadding(new Insets(10));
                    
                    dialog.getDialogPane().setContent(content);
                    ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().add(okButton);
                    
                    dialog.showAndWait();
                } else {
                    showInfo("Translation failed or text is already in French.");
                }
            } catch (Exception ex) {
                showError("Translation failed: " + ex.getMessage());
            }
        });
        
        // PDF Download Button
        Button pdfBtn = new Button("📄 Download as PDF");
        pdfBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5 10; -fx-border-radius: 4; -fx-background-radius: 4;");
        pdfBtn.setOnAction(e -> {
            try {
                String content = textArea.getText();
                String title = "Feedback Content";
                downloadAsPDF(title, content);
            } catch (Exception ex) {
                showError("PDF generation failed: " + ex.getMessage());
            }
        });
        
        toolsBox.getChildren().addAll(grammarBtn, translateBtn, pdfBtn);
        vbox.getChildren().add(toolsBox);
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
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Translate text using a simple HTTP API call with improved error handling
     */
    private String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Using a free translation API (LibreTranslate)
            String apiUrl = "https://libretranslate.de/translate";
            
            // Create JSON payload
            String jsonPayload = String.format(
                "{\"q\":\"%s\",\"source\":\"en\",\"target\":\"%s\",\"format\":\"text\"}",
                text.replace("\"", "\\\"").replace("\\", "\\\\"),
                targetLanguage
            );
            
            // Make HTTP request
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 second timeout
            conn.setReadTimeout(10000);    // 10 second timeout
            
            // Write JSON payload
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Check response code
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                // Try alternative API if main one fails
                return translateTextAlternative(text, targetLanguage);
            }
            
            // Read response
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
            // Parse JSON response with better error handling
            try {
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                if (jsonResponse.has("translatedText")) {
                    String translatedText = jsonResponse.getString("translatedText");
                    if (translatedText != null && !translatedText.equals(text)) {
                        return translatedText;
                    }
                }
            } catch (org.json.JSONException e) {
                System.err.println("JSON parsing error: " + e.getMessage());
                return translateTextAlternative(text, targetLanguage);
            }
            
        } catch (Exception e) {
            System.err.println("Translation API error: " + e.getMessage());
            return translateTextAlternative(text, targetLanguage);
        }
        
        return null;
    }
    
    /**
     * Alternative translation method using a different approach
     */
    private String translateTextAlternative(String text, String targetLanguage) {
        try {
            // Try using Google Translate API via a different endpoint
            String apiUrl = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=" + 
                           targetLanguage + "&dt=t&q=" + java.net.URLEncoder.encode(text, "UTF-8");
            
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                
                // Parse Google Translate response (simplified)
                String responseStr = response.toString();
                if (responseStr.contains("\"")) {
                    // Extract text between quotes
                    int start = responseStr.indexOf("\"") + 1;
                    int end = responseStr.indexOf("\"", start);
                    if (start > 0 && end > start) {
                        String translated = responseStr.substring(start, end);
                        translated = translated.replace("\\u0026", "&")
                                              .replace("\\u003c", "<")
                                              .replace("\\u003e", ">")
                                              .replace("\\u0027", "'")
                                              .replace("\\u0022", "\"")
                                              .replace("\\n", "\n")
                                              .replace("\\r", "\r");
                        if (!translated.equals(text)) {
                            return translated;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Alternative translation failed: " + e.getMessage());
        }
        
        // Final fallback: simple word replacement for common phrases
        return translateTextFallback(text, targetLanguage);
    }
    
    /**
     * Fallback translation using simple word replacement
     */
    private String translateTextFallback(String text, String targetLanguage) {
        // Simple word replacement for demonstration
        String lowerText = text.toLowerCase();
        
        switch (targetLanguage) {
            case "fr": // French
                if (lowerText.contains("i hate")) return text.replace("I hate", "Je déteste");
                if (lowerText.contains("hello")) return text.replace("Hello", "Bonjour");
                if (lowerText.contains("goodbye")) return text.replace("Goodbye", "Au revoir");
                if (lowerText.contains("thank you")) return text.replace("Thank you", "Merci");
                break;
            case "es": // Spanish
                if (lowerText.contains("i hate")) return text.replace("I hate", "Odio");
                if (lowerText.contains("hello")) return text.replace("Hello", "Hola");
                if (lowerText.contains("goodbye")) return text.replace("Goodbye", "Adiós");
                if (lowerText.contains("thank you")) return text.replace("Thank you", "Gracias");
                break;
            case "de": // German
                if (lowerText.contains("i hate")) return text.replace("I hate", "Ich hasse");
                if (lowerText.contains("hello")) return text.replace("Hello", "Hallo");
                if (lowerText.contains("goodbye")) return text.replace("Goodbye", "Auf Wiedersehen");
                if (lowerText.contains("thank you")) return text.replace("Thank you", "Danke");
                break;
            case "it": // Italian
                if (lowerText.contains("i hate")) return text.replace("I hate", "Odio");
                if (lowerText.contains("hello")) return text.replace("Hello", "Ciao");
                if (lowerText.contains("goodbye")) return text.replace("Goodbye", "Arrivederci");
                if (lowerText.contains("thank you")) return text.replace("Thank you", "Grazie");
                break;
            case "pt": // Portuguese
                if (lowerText.contains("i hate")) return text.replace("I hate", "Eu odeio");
                if (lowerText.contains("hello")) return text.replace("Hello", "Olá");
                if (lowerText.contains("goodbye")) return text.replace("Goodbye", "Adeus");
                if (lowerText.contains("thank you")) return text.replace("Thank you", "Obrigado");
                break;
        }
        
        // If no specific translation found, return a simple message
        String languageName = getLanguageName(targetLanguage);
        return text + " [Translated to " + languageName + " - API unavailable]";
    }
    
    /**
     * Get language name for display
     */
    private String getLanguageName(String code) {
        switch (code) {
            case "fr": return "French";
            case "es": return "Spanish";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            default: return "Selected Language";
        }
    }
    
    /**
     * Download feedback content as PDF or Text file
     */
    private void downloadAsPDF(String title, String content) {
        try {
            // Create a simple text file with PDF-like formatting
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Document");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName(title.replace(" ", "_") + ".txt");
            
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                String fileExtension = "";
                String fileName = selectedFile.getName();
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
                }
                
                if ("pdf".equals(fileExtension)) {
                    // Create a simple PDF-like text file with basic formatting
                    createPDFLikeFile(selectedFile, title, content);
                } else {
                    // Create a simple text file
                    createTextFile(selectedFile, title, content);
                }
                
                showInfo("Document downloaded successfully to: " + selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Failed to download document: " + e.getMessage());
        }
    }
    
    /**
     * Create a PDF-like text file with basic formatting
     */
    private void createPDFLikeFile(File file, String title, String content) throws Exception {
        StringBuilder pdfContent = new StringBuilder();
        
        // Add PDF-like header
        pdfContent.append("=".repeat(80)).append("\n");
        pdfContent.append("                        ").append(title.toUpperCase()).append("\n");
        pdfContent.append("=".repeat(80)).append("\n\n");
        
        // Add metadata
        pdfContent.append("Generated on: ").append(java.time.LocalDateTime.now()).append("\n");
        pdfContent.append("Author: ").append(Main.getSession().getUser().getUsername()).append("\n");
        pdfContent.append("Document Type: Feedback Content\n\n");
        
        // Add content with proper formatting
        pdfContent.append("-".repeat(80)).append("\n");
        pdfContent.append("CONTENT:\n");
        pdfContent.append("-".repeat(80)).append("\n\n");
        
        // Format content with line wrapping
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.length() > 80) {
                // Simple word wrapping
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    if (currentLine.length() + word.length() + 1 > 80) {
                        pdfContent.append(currentLine.toString().trim()).append("\n");
                        currentLine = new StringBuilder(word + " ");
                    } else {
                        currentLine.append(word).append(" ");
                    }
                }
                if (currentLine.length() > 0) {
                    pdfContent.append(currentLine.toString().trim()).append("\n");
                }
            } else {
                pdfContent.append(line).append("\n");
            }
        }
        
        pdfContent.append("\n").append("=".repeat(80)).append("\n");
        pdfContent.append("END OF DOCUMENT").append("\n");
        pdfContent.append("=".repeat(80)).append("\n");
        
        java.nio.file.Files.write(file.toPath(), pdfContent.toString().getBytes());
    }
    
    /**
     * Create a simple text file
     */
    private void createTextFile(File file, String title, String content) throws Exception {
        java.nio.file.Files.write(file.toPath(), 
            (title + "\n" + "=".repeat(title.length()) + "\n\n" + content).getBytes());
    }
    
    /**
     * Check grammar on feedback card
     */
    private void checkGrammarOnCard(Feedback feedback) {
        try {
            String originalText = feedback.getContent();
            String correctedText = com.travelxp.utils.LanguageToolClient.checkGrammar(originalText);
            if (!correctedText.equals(originalText)) {
                // Show dialog with corrected text
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Grammar Check Results");
                dialog.setHeaderText("Grammar corrections applied");
                
                TextArea originalArea = new TextArea(originalText);
                originalArea.setWrapText(true);
                originalArea.setEditable(false);
                originalArea.setPrefRowCount(8);
                
                TextArea correctedArea = new TextArea(correctedText);
                correctedArea.setWrapText(true);
                correctedArea.setEditable(true);
                correctedArea.setPrefRowCount(8);
                
                VBox content = new VBox(
                    new Label("Original Text:"), originalArea,
                    new Label("Corrected Text:"), correctedArea
                );
                content.setSpacing(10);
                content.setPadding(new Insets(10));
                
                dialog.getDialogPane().setContent(content);
                ButtonType applyButton = new ButtonType("Apply Changes", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().addAll(applyButton, cancelButton);
                
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    // Update feedback content
                    feedback.setContent(correctedText);
                    try {
                        feedbackService.updateFeedback(feedback);
                        loadFeedbacks();
                        showInfo("Grammar corrections applied successfully!");
                    } catch (SQLException ex) {
                        showError("Failed to update feedback: " + ex.getMessage());
                    }
                }
            } else {
                showInfo("No grammar issues found in this feedback.");
            }
        } catch (Exception ex) {
            showError("Grammar check failed: " + ex.getMessage());
        }
    }
    
    /**
     * Show translation dialog with language selection
     */
    private void showTranslationDialog(Feedback feedback) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Translate Feedback");
        dialog.setHeaderText("Select language for translation");
        
        // Language selection dropdown
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(
            "French (fr)",
            "Spanish (es)", 
            "German (de)",
            "Italian (it)",
            "Portuguese (pt)"
        );
        languageCombo.setValue("French (fr)");
        
        VBox content = new VBox(new Label("Select target language:"), languageCombo);
        content.setSpacing(10);
        content.setPadding(new Insets(10));
        
        dialog.getDialogPane().setContent(content);
        ButtonType translateButton = new ButtonType("Translate", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(translateButton, cancelButton);
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selectedLanguage = languageCombo.getValue();
            String languageCode = selectedLanguage.substring(selectedLanguage.length() - 3, selectedLanguage.length() - 1);
            
            try {
                String originalText = feedback.getContent();
                String translatedText = translateText(originalText, languageCode);
                
                if (translatedText != null && !translatedText.equals(originalText)) {
                    // Show translation comparison dialog
                    showTranslationComparisonDialog(feedback, originalText, translatedText, selectedLanguage);
                } else {
                    showInfo("Translation failed or text is already in " + selectedLanguage.split(" ")[0] + ".");
                }
            } catch (Exception ex) {
                showError("Translation failed: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Show translation comparison dialog
     */
    private void showTranslationComparisonDialog(Feedback feedback, String originalText, String translatedText, String language) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Translation Comparison");
        dialog.setHeaderText("Original vs " + language.split(" ")[0] + " Translation");
        
        TextArea originalArea = new TextArea(originalText);
        originalArea.setWrapText(true);
        originalArea.setEditable(false);
        originalArea.setPrefRowCount(12);
        originalArea.setPrefColumnCount(60);
        
        TextArea translatedArea = new TextArea(translatedText);
        translatedArea.setWrapText(true);
        translatedArea.setEditable(false);
        translatedArea.setPrefRowCount(12);
        translatedArea.setPrefColumnCount(60);
        
        // Add copy button for translated text
        Button copyBtn = new Button("📋 Copy Translation");
        copyBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5 10;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(translatedText);
            clipboard.setContent(content);
            showInfo("Translation copied to clipboard!");
        });
        
        VBox dialogContent = new VBox(
            new Label("Original Text (English):"), originalArea,
            new Label(language.split(" ")[0] + " Translation:"), translatedArea,
            copyBtn
        );
        dialogContent.setSpacing(10);
        dialogContent.setPadding(new Insets(10));
        
        dialog.getDialogPane().setContent(dialogContent);
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);
        
        dialog.showAndWait();
    }
    
    /**
     * Download specific feedback as PDF
     */
    private void downloadFeedbackAsPDF(Feedback feedback) {
        try {
            String content = feedback.getContent();
            String title = feedback.getTitle() != null && !feedback.getTitle().isEmpty() ? feedback.getTitle() : "Feedback_" + feedback.getId();
            
            // Create a proper PDF file with PDF content
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Feedback Document");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            fileChooser.setInitialFileName(title.replace(" ", "_") + ".pdf");
            
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                String fileExtension = "";
                String fileName = selectedFile.getName();
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
                }
                
                if ("pdf".equals(fileExtension)) {
                    // Create a real PDF file with PDF content
                    createFeedbackPDFFile(selectedFile, feedback, content);
                } else {
                    // Create a simple text file
                    createFeedbackTextFile(selectedFile, feedback, content);
                }
                
                showInfo("Feedback document downloaded successfully to: " + selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Failed to download feedback document: " + e.getMessage());
        }
    }
    
    /**
     * Create a real PDF file for specific feedback with image support
     */
    private void createFeedbackPDFFile(File file, Feedback feedback, String content) throws Exception {
        // Create PDF content with proper PDF structure
        StringBuilder pdfContent = new StringBuilder();
        
        // PDF header
        pdfContent.append("%PDF-1.4\n");
        
        // PDF objects
        String title = feedback.getTitle() != null ? feedback.getTitle() : "FEEDBACK";
        String author = feedback.getUsername() != null ? feedback.getUsername() : "User ID: " + feedback.getUserId();
        
        // Check if feedback has an image
        boolean hasImage = feedback.getImageUrl() != null && !feedback.getImageUrl().isEmpty();
        int imageObjectId = 0;
        
        // Object 1: Catalog
        pdfContent.append("1 0 obj\n");
        pdfContent.append("<< /Type /Catalog /Pages 2 0 R >>\n");
        pdfContent.append("endobj\n\n");
        
        // Object 2: Pages
        pdfContent.append("2 0 obj\n");
        pdfContent.append("<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n");
        pdfContent.append("endobj\n\n");
        
        // Object 3: Page
        pdfContent.append("3 0 obj\n");
        if (hasImage) {
            // Include image in resources if image exists
            pdfContent.append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> /XObject << /Im1 7 0 R >> >> >>\n");
            imageObjectId = 7;
        } else {
            pdfContent.append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\n");
        }
        pdfContent.append("endobj\n\n");
        
        // Object 4: Content stream
        pdfContent.append("4 0 obj\n");
        pdfContent.append("<< /Length 6 0 R >>\n");
        pdfContent.append("stream\n");
        
        // PDF content stream with text
        pdfContent.append("BT\n");
        pdfContent.append("/F1 24 Tf\n");
        pdfContent.append("72 720 Td\n");
        pdfContent.append("(").append(escapePDFString(title)).append(") Tj\n");
        pdfContent.append("ET\n\n");
        
        pdfContent.append("BT\n");
        pdfContent.append("/F1 12 Tf\n");
        pdfContent.append("72 690 Td\n");
        pdfContent.append("(Author: ").append(escapePDFString(author)).append(") Tj\n");
        pdfContent.append("ET\n\n");
        
        // Add image if present
        if (hasImage) {
            try {
                // Load and embed image
                String imagePath = "uploads/" + feedback.getImageUrl();
                File imageFile = new File(imagePath);
                
                if (imageFile.exists()) {
                    // Read image data
                    byte[] imageData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    
                    // Get image dimensions for proper scaling
                    int imageWidth = 400; // Default width
                    int imageHeight = 300; // Default height
                    String colorSpace = "/DeviceRGB";
                    
                    try {
                        // Try to get actual image dimensions using Java's image reading
                        javafx.scene.image.Image img = new javafx.scene.image.Image(imageFile.toURI().toString());
                        if (img.getWidth() > 0 && img.getHeight() > 0) {
                            imageWidth = (int) img.getWidth();
                            imageHeight = (int) img.getHeight();
                        }
                        // Check if image is grayscale
                        if (img.getPixelReader() != null) {
                            // For now, assume RGB. Could be enhanced to detect grayscale
                            colorSpace = "/DeviceRGB";
                        }
                    } catch (Exception ex) {
                        // Use default dimensions if image reading fails
                        System.err.println("Could not read image dimensions: " + ex.getMessage());
                    }
                    
                    // Calculate scaled dimensions to fit in PDF (max 400x300)
                    double scale = Math.min(400.0 / imageWidth, 300.0 / imageHeight);
                    if (scale < 1.0) {
                        imageWidth = (int) (imageWidth * scale);
                        imageHeight = (int) (imageHeight * scale);
                    }
                    
                    // Center the image horizontally
                    int imageX = (612 - imageWidth) / 2;
                    int imageY = 450; // Starting Y position
                    
                    // Add image to content stream with proper transformation
                    pdfContent.append("q\n");
                    pdfContent.append(imageWidth).append(" 0 0 ").append(imageHeight).append(" ").append(imageX).append(" ").append(imageY).append(" cm\n");
                    pdfContent.append("/Im1 Do\n");
                    pdfContent.append("Q\n\n");
                    
                    // Object 7: Image (if image exists)
                    pdfContent.append(imageObjectId).append(" 0 obj\n");
                    pdfContent.append("<< /Type /XObject /Subtype /Image /Width ").append(imageWidth).append(" /Height ").append(imageHeight).append(" /ColorSpace ").append(colorSpace).append(" /BitsPerComponent 8 /Length ").append(imageData.length).append(" /Filter /FlateDecode >>\n");
                    pdfContent.append("stream\n");
                    
                    // Compress and add image data
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    java.util.zip.Deflater deflater = new java.util.zip.Deflater();
                    deflater.setInput(imageData);
                    deflater.finish();
                    byte[] buffer = new byte[1024];
                    while (!deflater.finished()) {
                        int count = deflater.deflate(buffer);
                        baos.write(buffer, 0, count);
                    }
                    baos.close();
                    byte[] compressedData = baos.toByteArray();
                    
                    // Convert to PDF-safe string (handle binary data properly)
                    StringBuilder imageStream = new StringBuilder();
                    for (byte b : compressedData) {
                        imageStream.append((char) (b & 0xFF));
                    }
                    
                    pdfContent.append(imageStream.toString());
                    pdfContent.append("\nendstream\n");
                    pdfContent.append("endobj\n\n");
                }
            } catch (Exception e) {
                System.err.println("Failed to embed image in PDF: " + e.getMessage());
                e.printStackTrace();
                // Continue without image if embedding fails
            }
        }
        
        // Add content with line wrapping
        int startYPosition = hasImage ? 380 : 560; // Adjust starting position if image is present
        pdfContent.append("BT\n");
        pdfContent.append("/F1 12 Tf\n");
        pdfContent.append("72 ").append(startYPosition).append(" Td\n");
        
        String[] lines = content.split("\n");
        int yPosition = startYPosition;
        for (String line : lines) {
            if (line.length() > 80) {
                // Simple word wrapping for PDF
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    if (currentLine.length() + word.length() + 1 > 80) {
                        pdfContent.append("(").append(escapePDFString(currentLine.toString().trim())).append(") Tj\n");
                        yPosition -= 15;
                        pdfContent.append("ET\nBT\n/F1 12 Tf\n72 ").append(yPosition).append(" Td\n");
                        currentLine = new StringBuilder(word + " ");
                    } else {
                        currentLine.append(word).append(" ");
                    }
                }
                if (currentLine.length() > 0) {
                    pdfContent.append("(").append(escapePDFString(currentLine.toString().trim())).append(") Tj\n");
                }
            } else {
                pdfContent.append("(").append(escapePDFString(line)).append(") Tj\n");
            }
            yPosition -= 15;
            if (yPosition < 50) break; // Prevent going off page
            pdfContent.append("ET\nBT\n/F1 12 Tf\n72 ").append(yPosition).append(" Td\n");
        }
        
        pdfContent.append("ET\n");
        pdfContent.append("endstream\n");
        pdfContent.append("endobj\n\n");
        
        // Object 5: Font
        pdfContent.append("5 0 obj\n");
        pdfContent.append("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\n");
        pdfContent.append("endobj\n\n");
        
        // Object 6: Content length (placeholder, will be calculated)
        int contentLength = pdfContent.length();
        pdfContent.append("6 0 obj\n");
        pdfContent.append(contentLength).append("\n");
        pdfContent.append("endobj\n\n");
        
        // Cross-reference table
        long xrefPosition = pdfContent.length();
        int totalObjects = hasImage ? 8 : 7; // Add 1 if image object was created
        pdfContent.append("xref\n");
        pdfContent.append("0 ").append(totalObjects).append("\n");
        pdfContent.append("0000000000 65535 f \n");
        pdfContent.append(String.format("%010d 00000 n \n", 9)); // Object 1 position
        pdfContent.append(String.format("%010d 00000 n \n", 9 + 25)); // Object 2 position
        pdfContent.append(String.format("%010d 00000 n \n", 9 + 25 + 25)); // Object 3 position
        pdfContent.append(String.format("%010d 00000 n \n", 9 + 25 + 25 + 25)); // Object 4 position
        pdfContent.append(String.format("%010d 00000 n \n", 9 + 25 + 25 + 25 + 25)); // Object 5 position
        pdfContent.append(String.format("%010d 00000 n \n", 9 + 25 + 25 + 25 + 25 + 25)); // Object 6 position
        if (hasImage) {
            pdfContent.append(String.format("%010d 00000 n \n", 9 + 25 + 25 + 25 + 25 + 25 + 25)); // Object 7 (image) position
        }
        
        // Trailer
        pdfContent.append("trailer\n");
        pdfContent.append("<< /Size ").append(totalObjects).append(" /Root 1 0 R >>\n");
        pdfContent.append("startxref\n");
        pdfContent.append(xrefPosition).append("\n");
        pdfContent.append("%%EOF\n");
        
        java.nio.file.Files.write(file.toPath(), pdfContent.toString().getBytes());
    }
    
    /**
     * Escape special characters for PDF
     */
    private String escapePDFString(String text) {
        return text.replace("\\", "\\\\")
                  .replace("(", "\\(")
                  .replace(")", "\\)")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Create a simple text file for specific feedback
     */
    private void createFeedbackTextFile(File file, Feedback feedback, String content) throws Exception {
        String title = feedback.getTitle() != null && !feedback.getTitle().isEmpty() ? feedback.getTitle() : "Feedback_" + feedback.getId();
        String header = title + "\n" + "=".repeat(title.length()) + "\n\n";
        String metadata = "Author: " + (feedback.getUsername() != null ? feedback.getUsername() : "User ID: " + feedback.getUserId()) + "\n";
        metadata += "Created: " + feedback.getCreatedAt() + "\n";
        metadata += "Status: " + feedback.getStatus() + "\n";
        metadata += "Sentiment: " + (feedback.getSentiment() != null ? feedback.getSentiment() : "NEUTRAL") + "\n\n";
        
        java.nio.file.Files.write(file.toPath(), (header + metadata + content).getBytes());
    }
    
}
