package com.travelxp.utils;

import java.util.*;

/**
 * Keeps independent chat history per Trip ID.
 * In-memory store (simple). You can persist it later in DB if you want.
 */
public class TripChatSessionStore {

    private static final Map<Long, List<ChatMessage>> store = new HashMap<>();

    public static List<ChatMessage> getOrCreate(Long tripId) {
        if (tripId == null) tripId = -1L;
        return store.computeIfAbsent(tripId, id -> new ArrayList<>());
    }

    public static void clearTrip(Long tripId) {
        if (tripId == null) return;
        store.remove(tripId);
    }

    // Simple message model
    public static class ChatMessage {
        private final String role;    // "system" | "user" | "assistant"
        private final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}