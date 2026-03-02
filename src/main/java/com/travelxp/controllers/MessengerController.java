package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Conversation;
import com.travelxp.models.Message;
import com.travelxp.services.MessagingService;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class MessengerController {

    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button closeButton;
    @FXML private Button floatingActionButton;
    @FXML private Label conversationTitle;
    @FXML private Label onlineStatus;
    @FXML private Label userInitials;
    @FXML private Label typingIndicator;
    @FXML private Label messageCount;
    @FXML private Label lastSeen;
    @FXML private ScrollPane messagesScrollPane;

    private MessagingService messagingService;
    private Conversation currentConversation;
    private int currentUserId;
    private String otherUsername;
    private final Random random = new Random();

    public void initialize() {
        messagingService = new MessagingService();
        currentUserId = Main.getSession().getUser().getId();

        // Set up send button
        sendButton.setOnAction(e -> handleSendMessage());
        
        // Set up close button
        closeButton.setOnAction(e -> handleClose());
        
        // Set up floating action button
        floatingActionButton.setOnAction(e -> handleFloatingAction());

        // Set up text area behavior
        messageInput.setWrapText(true);
        messageInput.setPrefRowCount(3);
        
        // Handle Enter key press to send message
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                handleSendMessage();
            }
        });

        // Auto-scroll to bottom when new messages arrive
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue((Double) newVal);
        });

        Platform.runLater(() -> updateConversationHeader());
    }

    /**
     * Set the conversation to display
     */
    public void setConversation(Conversation conversation, String otherUsername) {
        this.currentConversation = conversation;
        this.otherUsername = otherUsername;
        this.conversationTitle.setText("Chat with " + otherUsername);

        try {
            // Mark all messages as read
            messagingService.markConversationAsRead(conversation.getId(), currentUserId);

            // Load and display messages
            loadMessages();
        } catch (SQLException e) {
            showError("Failed to load conversation: " + e.getMessage());
        }
    }

    /**
     * Load messages from the database and display them
     */
    private void loadMessages() {
        try {
            List<Message> messages = messagingService.getConversationMessages(currentConversation.getId());
            messagesContainer.getChildren().clear();

            if (messages.isEmpty()) {
                Label emptyLabel = new Label("No messages yet. Start the conversation!");
                emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
                messagesContainer.getChildren().add(emptyLabel);
            } else {
                for (Message message : messages) {
                    messagesContainer.getChildren().add(createMessageBubble(message));
                }
            }

            // Scroll to bottom
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        } catch (SQLException e) {
            showError("Failed to load messages: " + e.getMessage());
        }
    }

    /**
     * Create a message bubble for display
     */
    private HBox createMessageBubble(Message message) {
        HBox bubbleBox = new HBox();
        bubbleBox.setSpacing(10);
        bubbleBox.setPadding(new Insets(8, 15, 8, 15));

        boolean isCurrentUser = message.getSenderId() == currentUserId;

        // Create message bubble
        VBox bubble = new VBox();
        bubble.setSpacing(6);
        bubble.setPadding(new Insets(12, 14, 12, 14));
        bubble.setStyle(isCurrentUser ?
                "-fx-background-color: #007bff; -fx-border-radius: 12; -fx-background-radius: 12;" :
                "-fx-background-color: #e9ecef; -fx-border-radius: 12; -fx-background-radius: 12;");
        bubble.setMaxWidth(500);

        // Sender name
        Label senderLabel = new Label(message.getSenderUsername());
        senderLabel.setStyle(isCurrentUser ?
                "-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.85); -fx-font-weight: bold;" :
                "-fx-font-size: 12; -fx-text-fill: #495057; -fx-font-weight: bold;");

        // Message content - make it wrap properly
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle(isCurrentUser ?
                "-fx-font-size: 14; -fx-text-fill: white; -fx-line-spacing: 2;" :
                "-fx-font-size: 14; -fx-text-fill: #212529; -fx-line-spacing: 2;");

        // Timestamp
        Label timeLabel = new Label(formatTime(message.getCreatedAt().toString()));
        timeLabel.setStyle(isCurrentUser ?
                "-fx-font-size: 11; -fx-text-fill: rgba(255,255,255,0.6);" :
                "-fx-font-size: 11; -fx-text-fill: #6c757d;");

        bubble.getChildren().addAll(senderLabel, contentLabel, timeLabel);

        // Alignment - right for current user, left for other
        if (isCurrentUser) {
            bubbleBox.setStyle("-fx-alignment: center-right;");
            bubbleBox.getChildren().add(bubble);
        } else {
            bubbleBox.setStyle("-fx-alignment: center-left;");
            bubbleBox.getChildren().add(bubble);
        }

        return bubbleBox;
    }

    /**
     * Handle sending a message
     */
    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            showError("Message cannot be empty");
            return;
        }

        try {
            messagingService.sendMessage(currentConversation.getId(), currentUserId, content);
            messageInput.clear();
            loadMessages();
        } catch (SQLException e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Close the messenger window
     */
    @FXML
    private void handleClose() {
        // Close the stage
        Stage stage = (Stage) messageInput.getScene().getWindow();
        stage.close();
    }

    /**
     * Format timestamp for display
     */
    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            // Expected format: "YYYY-MM-DD HH:mm:ss"
            String[] parts = timestamp.split(" ");
            if (parts.length >= 2) {
                String timePart = parts[1];
                // Extract HH:mm from HH:mm:ss
                return timePart.length() >= 5 ? timePart.substring(0, 5) : timePart;
            }
            return timestamp;
        } catch (Exception e) {
            System.err.println("Error formatting time: " + e.getMessage());
            return timestamp;
        }
    }

    /**
     * Update the conversation header with user information
     */
    private void updateConversationHeader() {
        if (otherUsername != null && !otherUsername.isEmpty()) {
            // Set user initials
            String[] nameParts = otherUsername.split(" ");
            String initials = "";
            if (nameParts.length > 0) {
                initials += nameParts[0].charAt(0);
            }
            if (nameParts.length > 1) {
                initials += nameParts[nameParts.length - 1].charAt(0);
            }
            userInitials.setText(initials.toUpperCase());
            
            // Set online status
            onlineStatus.setText("Online");
            onlineStatus.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12;");
            
            // Set last seen
            lastSeen.setText("Last seen: Just now");
        }
    }

    /**
     * Handle floating action button click
     */
    private void handleFloatingAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quick Actions");
        alert.setHeaderText("Available Actions");
        alert.setContentText("• Start video call\n• Share location\n• Send file\n• Create group");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
