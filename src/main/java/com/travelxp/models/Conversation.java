package com.travelxp.models;

import java.time.LocalDateTime;

public class Conversation {
    private int id;
    private int user1Id;
    private int user2Id;
    private Integer feedbackId; // nullable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String otherUsername; // For display - username of the other user
    private int unreadCount; // Number of unread messages

    // Constructor without ID (for new conversations)
    public Conversation(int user1Id, int user2Id, Integer feedbackId) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.feedbackId = feedbackId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.unreadCount = 0;
    }

    // Constructor with ID (for conversations from DB)
    public Conversation(int id, int user1Id, int user2Id, Integer feedbackId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.feedbackId = feedbackId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.unreadCount = 0;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUser1Id() {
        return user1Id;
    }

    public int getUser2Id() {
        return user2Id;
    }

    public Integer getFeedbackId() {
        return feedbackId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get the ID of the other user in this conversation
     */
    public int getOtherUserId(int currentUserId) {
        return currentUserId == user1Id ? user2Id : user1Id;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                ", feedbackId=" + feedbackId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", unreadCount=" + unreadCount +
                '}';
    }
}
