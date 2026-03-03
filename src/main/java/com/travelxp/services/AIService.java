package com.travelxp.services;

import com.travelxp.utils.TripChatSessionStore.ChatMessage;

import java.util.List;

/**
 * AI service abstraction.
 * Replace the implementation inside callModel(...) with your real API call later.
 */
public class AIService {

    public String generateReply(List<ChatMessage> messages) throws Exception {
        // TODO: Replace with real API call (HTTP request)
        return callModel(messages);
    }

    private String callModel(List<ChatMessage> messages) {
        // TEMP demo response (so you can test UI + context + sessions)
        // You will replace this with an HTTP call to your AI provider.
        ChatMessage lastUser = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equalsIgnoreCase(messages.get(i).getRole())) {
                lastUser = messages.get(i);
                break;
            }
        }

        if (lastUser == null) {
            return "Hello! I’m your TravelXP assistant. I’m ready whenever you are.";
        }

        return "I received your message: \"" + lastUser.getContent() + "\"\n\n"
                + "If you connect me to an AI API, I will answer with real trip planning help based on your Trip data.";
    }
}