package com.travelxp.controllers;

import com.travelxp.models.Trip;
import com.travelxp.services.GeminiService;
import com.travelxp.services.GeminiService.Message;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TripAIPanelController {

    @FXML private Label tripSummaryLabel;
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatBox;
    @FXML private TextField userInputField;

    // (اختياري) لو عندك زر Send في FXML ضيفه
    @FXML private Button sendBtn;

    private Trip currentTrip;

    // ✅ history
    private final List<Message> history = new ArrayList<>();

    // ✅ Gemini Service
    private GeminiService aiService;

    // ✅ نخلي prompt ثابت لكل رحلة
    private String systemPrompt;

    // --------------- INIT ---------------
    public void initWithTrip(Trip trip) {
        this.currentTrip = trip;

        tripSummaryLabel.setText(buildTripSummary(trip));
        chatBox.getChildren().clear();
        history.clear();

        // Auto-scroll أقوى: أي تغيير في ارتفاع chatBox ينزل تلقائيًا
        hookAutoScroll();

        // أنشئ الخدمة مرة واحدة
        try {
            aiService = new GeminiService();
        } catch (Exception e) {
            addAiMessage("❌ AI init failed: " + e.getMessage());
            return;
        }

        // system prompt مبني على الرحلة
        systemPrompt = buildSystemPrompt();

        // رسالة البداية (Gemini role = model)
        String hello = "Hello 👋 Ask me about your trip: budget, schedule, packing, activities, or risks.";
        addAiMessage(hello);
        history.add(new Message("model", hello));

        focusInput();
    }

    // --------------- SEND ---------------
    @FXML
    private void handleSend() {
        if (aiService == null) return;

        String msg = userInputField.getText();
        if (msg == null || msg.trim().isEmpty()) return;

        msg = msg.trim();

        addUserMessage(msg);
        history.add(new Message("user", msg));
        userInputField.clear();
        setSending(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Gemini يأخذ systemPrompt + history
                return aiService.chat(systemPrompt, history);
            }
        };

        task.setOnSucceeded(ev -> {
            String reply = task.getValue();
            if (reply == null || reply.isBlank()) reply = "I couldn't generate a response. Try again.";

            addAiMessage(reply);
            history.add(new Message("model", reply));

            setSending(false);
            focusInput();
        });

        task.setOnFailed(ev -> {
            String err = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
            addAiMessage("❌ AI error: " + err);

            setSending(false);
            focusInput();
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    // --------------- PROMPTS ---------------
    private String buildSystemPrompt() {
        String tripInfo =
                "Trip info:\n" +
                        "- Name: " + safe(currentTrip != null ? currentTrip.getTripName() : null) + "\n" +
                        "- Origin: " + safe(currentTrip != null ? currentTrip.getOrigin() : null) + "\n" +
                        "- Destination: " + safe(currentTrip != null ? currentTrip.getDestination() : null) + "\n" +
                        "- Start date: " + (currentTrip != null && currentTrip.getStartDate() != null ? currentTrip.getStartDate() : "-") + "\n" +
                        "- End date: " + (currentTrip != null && currentTrip.getEndDate() != null ? currentTrip.getEndDate() : "-") + "\n" +
                        "- Budget: " + (currentTrip != null && currentTrip.getBudgetAmount() != null ? currentTrip.getBudgetAmount() : "-") + "\n";

        String behavior =
                "You are a smart travel assistant.\n" +
                        "Use only the trip info above.\n" +
                        "- Reply in the same language as the user.\n" +
                        "- Be practical and specific.\n" +
                        "- Ask short follow-up questions when needed.\n" +
                        "- Do not repeat the same sentence every time.\n";

        return behavior + "\n" + tripInfo;
    }

    private String buildTripSummary(Trip t) {
        if (t == null) return "Trip Summary: -";
        return "Trip: " + safe(t.getTripName()) + "\n"
                + "Route: " + safe(t.getOrigin()) + " -> " + safe(t.getDestination()) + "\n"
                + "Dates: " + (t.getStartDate() != null ? t.getStartDate() : "-")
                + " to " + (t.getEndDate() != null ? t.getEndDate() : "-");
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // --------------- UI HELPERS ---------------
    private void addUserMessage(String text) {
        Label l = new Label("You: " + text);
        l.getStyleClass().add("chat-user");
        l.setWrapText(true);
        chatBox.getChildren().add(l);
    }

    private void addAiMessage(String text) {
        Label l = new Label("AI: " + text);
        l.getStyleClass().add("chat-ai");
        l.setWrapText(true);
        chatBox.getChildren().add(l);
    }

    // ✅ Auto scroll أقوى (أفضل من setVvalue وحدها)
    private void hookAutoScroll() {
        if (chatScroll == null || chatBox == null) return;

        chatBox.heightProperty().addListener((obs, oldV, newV) -> {
            Platform.runLater(() -> {
                chatScroll.applyCss();
                chatScroll.layout();
                chatScroll.setVvalue(1.0);
            });
        });
    }

    private void setSending(boolean sending) {
        if (sendBtn != null) sendBtn.setDisable(sending);
        userInputField.setDisable(sending);
    }

    private void focusInput() {
        Platform.runLater(() -> userInputField.requestFocus());
    }
}