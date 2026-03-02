package com.travelxp.controllers;

import java.io.IOException;
import java.util.Random;

import com.travelxp.Main;
import com.travelxp.services.GeminiService;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Gemini AI chat assistant page.
 */
public class GeminiChatController {

    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Pane animatedBg;

    private final GeminiService geminiService = new GeminiService();
    private final Random random = new Random();

    @FXML
    public void initialize() {
        Platform.runLater(this::startBackgroundAnimation);

        if (!geminiService.isConfigured()) {
            addAssistantMessage("Welcome! It looks like the Gemini API key is not configured yet. " +
                    "Please add your API key to db.properties as 'gemini.api.key=YOUR_KEY' to enable AI assistance.\n\n" +
                    "You can get a free API key from https://aistudio.google.com/apikey");
        } else {
            addAssistantMessage("Hi there! I'm the TravelXP Assistant, powered by Gemini. " +
                    "I know everything about this app and can help you navigate it.\n\n" +
                    "Try asking me things like:\n" +
                    "  - \"How do I book a property?\"\n" +
                    "  - \"How do I set up Face ID?\"\n" +
                    "  - \"What is the gamification system?\"\n" +
                    "  - \"How do I recharge my wallet?\"");
        }

        // Auto-scroll when new content is added
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                chatScrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        if (!geminiService.isConfigured()) {
            addAssistantMessage("The Gemini API key is not configured. Please add 'gemini.api.key=YOUR_KEY' to db.properties.");
            return;
        }

        // Add user message bubble
        addUserMessage(message);
        messageField.clear();

        // Disable input while waiting
        messageField.setDisable(true);
        sendButton.setDisable(true);

        // Show typing indicator
        HBox typingIndicator = createTypingIndicator();
        chatContainer.getChildren().add(typingIndicator);

        // Send to Gemini on background thread
        new Thread(() -> {
            try {
                String response = geminiService.chat(message);
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(typingIndicator);
                    addAssistantMessage(response);
                    messageField.setDisable(false);
                    sendButton.setDisable(false);
                    messageField.requestFocus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(typingIndicator);
                    addAssistantMessage("Sorry, I encountered an error: " + e.getMessage());
                    messageField.setDisable(false);
                    sendButton.setDisable(false);
                    messageField.requestFocus();
                });
            }
        }).start();
    }

    @FXML
    private void handleClearChat(ActionEvent event) {
        geminiService.clearHistory();
        chatContainer.getChildren().clear();
        addAssistantMessage("Chat cleared! How can I help you?");
    }

    /**
     * Add a user message bubble to the chat.
     */
    private void addUserMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.setStyle(
                "-fx-background-color: -fx-accent-color;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 18;" +
                "-fx-background-radius: 18 18 4 18;" +
                "-fx-font-size: 13px;"
        );

        HBox bubble = new HBox(label);
        bubble.setAlignment(Pos.CENTER_RIGHT);
        bubble.setPadding(new Insets(2, 0, 2, 80));

        chatContainer.getChildren().add(bubble);
    }

    /**
     * Add an assistant message bubble to the chat with markdown-style formatting.
     * Supports: **bold**, *italic*, `code`, # headers, - list items
     */
    private void addAssistantMessage(String text) {
        TextFlow textFlow = parseMarkdown(text);
        textFlow.setMaxWidth(550);
        textFlow.setStyle(
                "-fx-background-color: -fx-secondary-bg;" +
                "-fx-padding: 12 18;" +
                "-fx-background-radius: 18 18 18 4;" +
                "-fx-border-color: derive(-fx-secondary-bg, 20%);" +
                "-fx-border-radius: 18 18 18 4;" +
                "-fx-border-width: 1;"
        );

        HBox bubble = new HBox(textFlow);
        bubble.setAlignment(Pos.CENTER_LEFT);
        bubble.setPadding(new Insets(2, 80, 2, 0));

        chatContainer.getChildren().add(bubble);
    }

    /**
     * Parse markdown-style text into a styled TextFlow.
     * Supports **bold**, *italic*, ***bold italic***, `inline code`, ## headers, and - list items.
     */
    private TextFlow parseMarkdown(String markdown) {
        TextFlow flow = new TextFlow();
        flow.setLineSpacing(4);

        String[] lines = markdown.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Headers: # text, ## text, ### text
            if (line.matches("^#{1,3}\\s+.*")) {
                int level = 0;
                while (level < line.length() && line.charAt(level) == '#') level++;
                String headerText = line.substring(level).trim();
                Text header = new Text(headerText);
                double size = level == 1 ? 18 : level == 2 ? 16 : 14;
                header.setFont(Font.font("Segoe UI", FontWeight.BOLD, size));
                header.setStyle("-fx-fill: -fx-text-base-color;");
                flow.getChildren().add(header);
            }
            // List items: - text or * text (at start of line, but not bold marker)
            else if (line.matches("^\\s*[-]\\s+.*")) {
                String itemText = line.replaceFirst("^\\s*[-]\\s+", "");
                Text bullet = new Text("  \u2022 ");
                bullet.setStyle("-fx-fill: -fx-accent-color;");
                bullet.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                flow.getChildren().add(bullet);
                parseInlineFormatting(itemText, flow);
            }
            else {
                parseInlineFormatting(line, flow);
            }

            // Add newline between lines (except the last)
            if (i < lines.length - 1) {
                flow.getChildren().add(new Text("\n"));
            }
        }

        return flow;
    }

    /**
     * Parse inline markdown formatting: **bold**, *italic*, ***bold italic***, `code`.
     */
    private void parseInlineFormatting(String text, TextFlow flow) {
        // Regex to find: ***bold italic***, **bold**, *italic*, `code`
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(\\*{3})(.+?)\\*{3}" +        // ***bold italic***
                "|(\\*{2})(.+?)\\*{2}" +        // **bold**
                "|(\\*)(.+?)\\*" +              // *italic*
                "|`([^`]+)`"                       // `code`
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // Add plain text before this match
            if (matcher.start() > lastEnd) {
                Text plain = new Text(text.substring(lastEnd, matcher.start()));
                plain.setFont(Font.font("Segoe UI", 13));
                plain.setStyle("-fx-fill: -fx-text-base-color;");
                flow.getChildren().add(plain);
            }

            if (matcher.group(1) != null) {
                // ***bold italic***
                Text t = new Text(matcher.group(2));
                t.setFont(Font.font("Segoe UI", FontWeight.BOLD, FontPosture.ITALIC, 13));
                t.setStyle("-fx-fill: -fx-text-base-color;");
                flow.getChildren().add(t);
            } else if (matcher.group(3) != null) {
                // **bold**
                Text t = new Text(matcher.group(4));
                t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                t.setStyle("-fx-fill: -fx-text-base-color;");
                flow.getChildren().add(t);
            } else if (matcher.group(5) != null) {
                // *italic*
                Text t = new Text(matcher.group(6));
                t.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 13));
                t.setStyle("-fx-fill: -fx-text-base-color;");
                flow.getChildren().add(t);
            } else if (matcher.group(7) != null) {
                // `code`
                Text t = new Text(matcher.group(7));
                t.setFont(Font.font("Consolas", 12));
                t.setStyle("-fx-fill: -fx-accent-color;");
                flow.getChildren().add(t);
            }

            lastEnd = matcher.end();
        }

        // Add remaining plain text
        if (lastEnd < text.length()) {
            Text plain = new Text(text.substring(lastEnd));
            plain.setFont(Font.font("Segoe UI", 13));
            plain.setStyle("-fx-fill: -fx-text-base-color;");
            flow.getChildren().add(plain);
        }
    }

    /**
     * Create a typing indicator (animated dots).
     */
    private HBox createTypingIndicator() {
        Label dots = new Label("Thinking...");
        dots.setStyle(
                "-fx-background-color: -fx-secondary-bg;" +
                "-fx-text-fill: -fx-text-base-color;" +
                "-fx-padding: 10 16;" +
                "-fx-background-radius: 18 18 18 4;" +
                "-fx-font-size: 12px;" +
                "-fx-font-style: italic;" +
                "-fx-opacity: 0.7;"
        );

        HBox bubble = new HBox(dots);
        bubble.setAlignment(Pos.CENTER_LEFT);
        bubble.setPadding(new Insets(2, 80, 2, 0));
        return bubble;
    }

    // ========== Background Animation (same pattern as DashboardController) ==========

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
        boolean isDark = com.travelxp.utils.ThemeManager.isDark();
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

    // ========== Navigation handlers ==========

    @FXML private void handleDashboard(ActionEvent event) { changeScene(event, "/com/travelxp/views/dashboard.fxml"); }
    @FXML private void handleTasks(ActionEvent event) { changeScene(event, "/com/travelxp/views/tasks.fxml"); }
    @FXML private void handleEditProfile(ActionEvent event) { changeScene(event, "/com/travelxp/views/edit_profile.fxml"); }
    @FXML private void handleChangePassword(ActionEvent event) { changeScene(event, "/com/travelxp/views/change_password.fxml"); }
    @FXML private void handleFeedback(ActionEvent event) { changeScene(event, "/com/travelxp/views/feedback-view.fxml"); }

    @FXML
    private void handleBrowseProperties(ActionEvent event) {
        String fxml = Main.getSession().getUser().getRole().equals("ADMIN")
                ? "/com/travelxp/views/admin-property-view.fxml"
                : "/com/travelxp/views/property-view.fxml";
        changeScene(event, fxml);
    }

    @FXML
    private void handleMyBookings(ActionEvent event) {
        String fxml = Main.getSession().getUser().getRole().equals("ADMIN")
                ? "/com/travelxp/views/admin-booking-view.fxml"
                : "/com/travelxp/views/booking-view.fxml";
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

    @FXML private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        com.travelxp.utils.ThemeManager.toggleTheme(stage.getScene());
    }

    @FXML private void handleLogout(ActionEvent event) {
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
        }
    }
}
