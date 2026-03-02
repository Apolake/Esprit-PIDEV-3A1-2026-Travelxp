package com.travelxp.models;

import java.time.LocalDateTime;

public class Feedback {

    private int id;
    private String title;
    private String content;
    private int userId;
    private LocalDateTime createdAt;
    private String imageUrl;
    private int likes;
    private int dislikes;
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private String status; // NEW, RECENT, OLD
    private int favoriteCount;
    private boolean isFavorited; // Whether the current user has favorited this feedback
    // username of the feedback author (not stored in table)
    private String username;

    // Constructor without ID (for new feedback before DB assignment)
    public Feedback(String title, String content, int userId, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
        this.imageUrl = null;
        this.likes = 0;
        this.dislikes = 0;
        this.sentiment = null;
        this.status = "NEW";
    }

    // Constructor with ID (for feedback loaded from DB)
    public Feedback(int id, String title, String content, int userId, LocalDateTime createdAt,
                    String imageUrl, int likes, int dislikes, String sentiment, String status) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.dislikes = dislikes;
        this.sentiment = sentiment;
        this.status = status;
        this.username = null;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() { return title; }

    public String getImageUrl() { return imageUrl; }

    public int getLikes() { return likes; }

    public int getDislikes() { return dislikes; }

    public String getSentiment() { return sentiment; }

    public String getStatus() { return status; }

    public String getUsername() { return username; }

    public int getFavoriteCount() { return favoriteCount; }

    public boolean isFavorited() { return isFavorited; }

    public int getUserId() {
        return userId;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setTitle(String title) { this.title = title; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setLikes(int likes) { this.likes = likes; }

    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public void setStatus(String status) { this.status = status; }

    public void setUsername(String username) { this.username = username; }

    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }

    public void setFavorited(boolean favorited) { this.isFavorited = favorited; }

    // Calculate sentiment based on likes/dislikes ratio
    public String calculateSentiment() {
        if (likes > dislikes) {
            return "POSITIVE";
        } else if (dislikes > likes) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }

    // Update sentiment based on current likes/dislikes
    public void updateSentiment() {
        this.sentiment = calculateSentiment();
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", sentiment='" + sentiment + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
