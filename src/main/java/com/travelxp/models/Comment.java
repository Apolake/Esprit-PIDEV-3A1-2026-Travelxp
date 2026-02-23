package com.travelxp.models;

import java.time.LocalDateTime;

public class Comment {

    private int id;
    private int feedbackId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;

    // Constructor without ID (for new comments before DB assignment)
    public Comment(int feedbackId, int userId, String content, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Full constructor with ID
    public Comment(int id, int feedbackId, int userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getFeedbackId() {
        return feedbackId;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", feedbackId=" + feedbackId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
