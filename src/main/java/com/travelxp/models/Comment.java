package com.travelxp.models;

import java.time.LocalDateTime;

public class Comment {

    private int id;
    private int feedbackId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private int likes;
    private int dislikes;
    private String timezone;

    // Constructor without ID (for new comments before DB assignment)
    public Comment(int feedbackId, int userId, String content, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.likes = 0;
        this.dislikes = 0;
        this.timezone = null;
    }

    // Full constructor with ID
    public Comment(int id, int feedbackId, int userId, String content, LocalDateTime createdAt, int likes, int dislikes, String timezone) {
        this.id = id;
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
        this.dislikes = dislikes;
        this.timezone = timezone;
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

    public int getLikes() { return likes; }

    public int getDislikes() { return dislikes; }

    public String getTimezone() { return timezone; }

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

    public void setLikes(int likes) { this.likes = likes; }

    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public void setTimezone(String timezone) { this.timezone = timezone; }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", feedbackId=" + feedbackId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
