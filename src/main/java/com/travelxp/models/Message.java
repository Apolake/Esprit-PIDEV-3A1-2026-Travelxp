package com.travelxp.models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String senderUsername; // For display purposes

    // Constructor without ID (for new messages)
    public Message(int conversationId, int senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Constructor with ID (for messages from DB)
    public Message(int id, int conversationId, int senderId, String content, LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", isRead=" + isRead +
                '}';
    }
}
