package com.travelxp.repositories;

import com.travelxp.models.Feedback;
import com.travelxp.utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRepository {

    private Connection getConnection() {
        return MyDB.getInstance().getConnection();
    }

    // Check if user exists
    public boolean userExists(int userId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Check if duplicate feedback exists for same user and content
    public boolean duplicateFeedbackExists(int userId, String content) throws SQLException {
        String sql = "SELECT 1 FROM feedback WHERE user_id = ? AND fcontent = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, content);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Create new feedback
    public void createFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (title, fcontent, image_url, user_id, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, feedback.getTitle());
            pstmt.setString(2, feedback.getContent());
            pstmt.setString(3, feedback.getImageUrl());
            pstmt.setInt(4, feedback.getUserId());
            pstmt.setTimestamp(5, Timestamp.valueOf(feedback.getCreatedAt()));
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Get all feedbacks (no filters)
    public List<Feedback> getAllFeedback() throws SQLException {
        return searchFeedback(null, null, "date", false);
    }

    /**
     * Retrieve feedback with optional username/title filters and sorting.
     * For likes/dislikes sorting, highest values appear first (descending).
     * For date sorting, newest appear first (descending) when asc=false, oldest first when asc=true.
     *
     * @param usernameFilter substring to match against users.username (case insensitive)
     * @param titleFilter substring to match feedback.title (case insensitive)
     * @param sortBy one of "date","likes","dislikes"
     * @param asc whether to sort ascending (false=descending) - only affects date sorting
     */
    public List<Feedback> searchFeedback(String usernameFilter, String titleFilter, String sortBy, boolean asc) throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT f.id, f.title, f.fcontent, f.image_url, f.user_id, f.created_at, f.likes, f.dislikes, f.sentiment, f.status, f.favorite_count, u.username ");
        sql.append("FROM feedback f JOIN users u ON f.user_id = u.id ");

        boolean whereAdded = false;
        if (usernameFilter != null && !usernameFilter.trim().isEmpty()) {
            sql.append("WHERE u.username LIKE ? ");
            whereAdded = true;
        }
        if (titleFilter != null && !titleFilter.trim().isEmpty()) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("f.title LIKE ? ");
            whereAdded = true;
        }

        sql.append("ORDER BY ");
        switch (sortBy != null ? sortBy : "date") {
            case "likes":
                sql.append("f.likes DESC, f.created_at DESC");
                break;
            case "dislikes":
                sql.append("f.dislikes DESC, f.created_at DESC");
                break;
            case "date":
            default:
                sql.append("f.created_at ").append(asc ? "ASC" : "DESC");
                break;
        }

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (usernameFilter != null && !usernameFilter.trim().isEmpty()) {
                pstmt.setString(idx++, "%" + usernameFilter + "%");
            }
            if (titleFilter != null && !titleFilter.trim().isEmpty()) {
                pstmt.setString(idx++, "%" + titleFilter + "%");
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String content = rs.getString("fcontent");
                    String imageUrl = rs.getString("image_url");
                    int userId = rs.getInt("user_id");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    int likes = rs.getInt("likes");
                    int dislikes = rs.getInt("dislikes");
                    String sentiment = rs.getString("sentiment");
                    String status = rs.getString("status");
                    int favoriteCount = rs.getInt("favorite_count");
                    String username = rs.getString("username");

                    Feedback fb = new Feedback(id, title, content, userId, createdAt, imageUrl, likes, dislikes, sentiment, status);
                    fb.setUsername(username);
                    fb.setFavoriteCount(favoriteCount);
                    // compute dynamic status based on age
                    String dyn = computeStatus(createdAt);
                    fb.setStatus(dyn);
                    feedbacks.add(fb);
                }
            }
        }
        return feedbacks;
    }

    // Get feedback by ID
    public Feedback getFeedbackById(int id) throws SQLException {
        String sql = "SELECT f.id, f.title, f.fcontent, f.image_url, f.user_id, f.created_at, f.likes, f.dislikes, f.sentiment, f.status, f.favorite_count, u.username"
                   + " FROM feedback f JOIN users u ON f.user_id=u.id WHERE f.id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String content = rs.getString("fcontent");
                    String imageUrl = rs.getString("image_url");
                    int userId = rs.getInt("user_id");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    int likes = rs.getInt("likes");
                    int dislikes = rs.getInt("dislikes");
                    String sentiment = rs.getString("sentiment");
                    String status = rs.getString("status");
                    int favoriteCount = rs.getInt("favorite_count");
                    String username = rs.getString("username");

                    Feedback fb = new Feedback(id, title, content, userId, createdAt, imageUrl, likes, dislikes, sentiment, status);
                    fb.setUsername(username);
                    fb.setFavoriteCount(favoriteCount);
                    fb.setStatus(computeStatus(createdAt));
                    return fb;
                }
            }
        }
        return null;
    }

    // Update a feedback
    public void updateFeedback(Feedback feedback) throws SQLException {
        String sql = "UPDATE feedback SET title = ?, fcontent = ?, image_url = ?, sentiment = ?, status = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setString(1, feedback.getTitle());
            pstmt.setString(2, feedback.getContent());
            pstmt.setString(3, feedback.getImageUrl());
            pstmt.setString(4, feedback.getSentiment());
            pstmt.setString(5, feedback.getStatus());
            pstmt.setInt(6, feedback.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Feedback not found with id: " + feedback.getId());
            }
        }
    }

    // Delete feedback by ID
    public void deleteFeedback(int id) throws SQLException {
        String sql = "DELETE FROM feedback WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Feedback not found with id: " + id);
            }
        }
    }

    // compute status helper
    private String computeStatus(LocalDateTime createdAt) {
        if (createdAt == null) return "NEW";
        long days = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        if (days >= 30) return "OLD";
        if (days >= 3) return "RECENT";
        return "NEW";
    }

    // Toggle favorite status for a feedback by a user
    public void toggleFavorite(int feedbackId, int userId) throws SQLException {
        Connection conn = getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // Check if favorite already exists
            String selectSql = "SELECT 1 FROM favorites WHERE feedback_id = ? AND user_id = ?";
            boolean exists = false;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, feedbackId);
                selectStmt.setInt(2, userId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                // Remove favorite
                String deleteSql = "DELETE FROM favorites WHERE feedback_id = ? AND user_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, feedbackId);
                    deleteStmt.setInt(2, userId);
                    deleteStmt.executeUpdate();
                }

                // Decrement favorite count
                String decSql = "UPDATE feedback SET favorite_count = GREATEST(COALESCE(favorite_count,0) - 1, 0) WHERE id = ?";
                try (PreparedStatement decStmt = conn.prepareStatement(decSql)) {
                    decStmt.setInt(1, feedbackId);
                    decStmt.executeUpdate();
                }
            } else {
                // Add favorite
                String insertSql = "INSERT INTO favorites (feedback_id, user_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, feedbackId);
                    insertStmt.setInt(2, userId);
                    insertStmt.executeUpdate();
                }

                // Increment favorite count
                String incSql = "UPDATE feedback SET favorite_count = COALESCE(favorite_count,0) + 1 WHERE id = ?";
                try (PreparedStatement incStmt = conn.prepareStatement(incSql)) {
                    incStmt.setInt(1, feedbackId);
                    incStmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException e) { /* ignore */ }
            throw ex;
        } finally {
            try { conn.setAutoCommit(previousAutoCommit); } catch (SQLException e) { /* ignore */ }
        }
    }

    // Check if feedback is favorited by user
    public boolean isFavorited(int feedbackId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM favorites WHERE feedback_id = ? AND user_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, feedbackId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Get favorited feedbacks for a user
    public List<Feedback> getFavoritedFeedbacks(int userId, String sortBy, boolean asc) throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT f.id, f.title, f.fcontent, f.image_url, f.user_id, f.created_at, f.likes, f.dislikes, f.sentiment, f.status, f.favorite_count, u.username ")
           .append("FROM feedback f ")
           .append("INNER JOIN favorites fav ON f.id = fav.feedback_id ")
           .append("INNER JOIN users u ON f.user_id = u.id ")
           .append("WHERE fav.user_id = ? ");

        sql.append("ORDER BY ");
        switch (sortBy != null ? sortBy : "date") {
            case "likes":
                sql.append("f.likes DESC, f.created_at DESC");
                break;
            case "dislikes":
                sql.append("f.dislikes DESC, f.created_at DESC");
                break;
            case "favorites":
                sql.append("f.favorite_count DESC, f.created_at DESC");
                break;
            case "date":
            default:
                sql.append("f.created_at ").append(asc ? "ASC" : "DESC");
                break;
        }

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql.toString())) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String content = rs.getString("fcontent");
                    String imageUrl = rs.getString("image_url");
                    int fbUserId = rs.getInt("user_id");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    int likes = rs.getInt("likes");
                    int dislikes = rs.getInt("dislikes");
                    String sentiment = rs.getString("sentiment");
                    String status = rs.getString("status");
                    int favoriteCount = rs.getInt("favorite_count");
                    String username = rs.getString("username");

                    Feedback fb = new Feedback(id, title, content, fbUserId, createdAt, imageUrl, likes, dislikes, sentiment, status);
                    fb.setUsername(username);
                    fb.setFavoriteCount(favoriteCount);
                    fb.setStatus(computeStatus(createdAt));
                    feedbacks.add(fb);
                }
            }
        }
        return feedbacks;
    }

    // Update feedback sentiment
    public void updateFeedbackSentiment(int feedbackId, String sentiment) throws SQLException {
        String sql = "UPDATE feedback SET sentiment = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, sentiment);
            stmt.setInt(2, feedbackId);
            stmt.executeUpdate();
        }
    }

    // Toggle or insert a reaction (LIKE / DISLIKE) for a feedback by a user
    public void toggleFeedbackReaction(int feedbackId, int userId, String reaction) throws SQLException {
        if (reaction == null) throw new SQLException("Reaction must be provided");
        String normalized = reaction.trim().toUpperCase();
        if (!normalized.equals("LIKE") && !normalized.equals("DISLIKE")) {
            throw new SQLException("Invalid reaction: " + reaction);
        }

        Connection conn = getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            String selectSql = "SELECT reaction FROM feedback_reactions WHERE feedback_id = ? AND user_id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, feedbackId);
                selectStmt.setInt(2, userId);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        // no previous reaction -> insert and increment counter
                        String insertSql = "INSERT INTO feedback_reactions (feedback_id, user_id, reaction, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP())";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, feedbackId);
                            insertStmt.setInt(2, userId);
                            insertStmt.setString(3, normalized);
                            insertStmt.executeUpdate();
                        }

                        String updateSql = normalized.equals("LIKE") ? "UPDATE feedback SET likes = COALESCE(likes,0) + 1 WHERE id = ?" : "UPDATE feedback SET dislikes = COALESCE(dislikes,0) + 1 WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, feedbackId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        String existing = rs.getString("reaction");
                        if (existing == null) existing = "";
                        existing = existing.toUpperCase();
                        if (existing.equals(normalized)) {
                            // same reaction -> remove and decrement
                            String deleteSql = "DELETE FROM feedback_reactions WHERE feedback_id = ? AND user_id = ?";
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                                deleteStmt.setInt(1, feedbackId);
                                deleteStmt.setInt(2, userId);
                                deleteStmt.executeUpdate();
                            }

                            String decSql = normalized.equals("LIKE") ? "UPDATE feedback SET likes = GREATEST(COALESCE(likes,0) - 1, 0) WHERE id = ?" : "UPDATE feedback SET dislikes = GREATEST(COALESCE(dislikes,0) - 1, 0) WHERE id = ?";
                            try (PreparedStatement decStmt = conn.prepareStatement(decSql)) {
                                decStmt.setInt(1, feedbackId);
                                decStmt.executeUpdate();
                            }
                        } else {
                            // opposite reaction exists -> update reaction and adjust counters
                            String updateReactionSql = "UPDATE feedback_reactions SET reaction = ?, created_at = CURRENT_TIMESTAMP() WHERE feedback_id = ? AND user_id = ?";
                            try (PreparedStatement urs = conn.prepareStatement(updateReactionSql)) {
                                urs.setString(1, normalized);
                                urs.setInt(2, feedbackId);
                                urs.setInt(3, userId);
                                urs.executeUpdate();
                            }

                            // decrement previous, increment new
                            String decPrev = existing.equals("LIKE") ? "UPDATE feedback SET likes = GREATEST(COALESCE(likes,0) - 1, 0) WHERE id = ?" : "UPDATE feedback SET dislikes = GREATEST(COALESCE(dislikes,0) - 1, 0) WHERE id = ?";
                            String incNew = normalized.equals("LIKE") ? "UPDATE feedback SET likes = COALESCE(likes,0) + 1 WHERE id = ?" : "UPDATE feedback SET dislikes = COALESCE(dislikes,0) + 1 WHERE id = ?";

                            try (PreparedStatement decStmt = conn.prepareStatement(decPrev)) {
                                decStmt.setInt(1, feedbackId);
                                decStmt.executeUpdate();
                            }
                            try (PreparedStatement incStmt = conn.prepareStatement(incNew)) {
                                incStmt.setInt(1, feedbackId);
                                incStmt.executeUpdate();
                            }
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException e) { /* ignore */ }
            throw ex;
        } finally {
            try { conn.setAutoCommit(previousAutoCommit); } catch (SQLException e) { /* ignore */ }
        }
    }
}
