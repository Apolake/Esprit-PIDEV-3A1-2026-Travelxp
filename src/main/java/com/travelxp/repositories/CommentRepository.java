package com.travelxp.repositories;

import com.travelxp.models.Comment;
import com.travelxp.utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

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

    // Check if duplicate comment exists for same user on same feedback
    public boolean duplicateCommentExists(int feedbackId, int userId, String content) throws SQLException {
        String sql = "SELECT 1 FROM comments WHERE feedback_id = ? AND user_id = ? AND comment_text = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, feedbackId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, content);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Add a new comment
    public void addComment(Comment comment) throws SQLException {
        String sql = "INSERT INTO comments (feedback_id, user_id, comment_text, created_at, likes, dislikes, timezone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, comment.getFeedbackId());
            pstmt.setInt(2, comment.getUserId());
            pstmt.setString(3, comment.getContent());
            pstmt.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            pstmt.setInt(5, comment.getLikes());
            pstmt.setInt(6, comment.getDislikes());
            pstmt.setString(7, comment.getTimezone());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Get all comments for a specific feedback (default sort by date desc)
    public List<Comment> getCommentsByFeedbackId(int feedbackId) throws SQLException {
        return getCommentsByFeedbackId(feedbackId, "date", false);
    }

    /**
     * Retrieve comments with optional sort order.
     * For likes/dislikes sorting, highest values appear first (descending).
     * For date sorting, newest appear first (descending) when asc=false, oldest first when asc=true.
     * sortBy: "date", "likes", "dislikes". asc parameter controls date sort direction.
     */
    public List<Comment> getCommentsByFeedbackId(int feedbackId, String sortBy, boolean asc) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, feedback_id, user_id, comment_text, created_at, likes, dislikes, timezone FROM comments WHERE feedback_id = ? ");
        sql.append("ORDER BY ");
        switch (sortBy != null ? sortBy : "date") {
            case "likes":
                sql.append("likes DESC, created_at DESC");
                break;
            case "dislikes":
                sql.append("dislikes DESC, created_at DESC");
                break;
            case "date":
            default:
                sql.append("created_at ").append(asc ? "ASC" : "DESC");
                break;
        }

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql.toString())) {
            pstmt.setInt(1, feedbackId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    String content = rs.getString("comment_text");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    int likes = rs.getInt("likes");
                    int dislikes = rs.getInt("dislikes");
                    String timezone = rs.getString("timezone");

                    comments.add(new Comment(id, feedbackId, userId, content, createdAt, likes, dislikes, timezone));
                }
            }
        }
        return comments;
    }

    // Update a comment
    public void updateComment(Comment comment) throws SQLException {
        String sql = "UPDATE comments SET comment_text = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Comment not found with id: " + comment.getId());
            }
        }
    }

    // Delete a comment by ID
    public void deleteComment(int id) throws SQLException {
        String sql = "DELETE FROM comments WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Comment not found with id: " + id);
            }
        }
    }

    // Toggle or insert a reaction (LIKE / DISLIKE) for a comment by a user
    public void toggleCommentReaction(int commentId, int userId, String reaction) throws SQLException {
        if (reaction == null) throw new SQLException("Reaction must be provided");
        String normalized = reaction.trim().toUpperCase();
        if (!normalized.equals("LIKE") && !normalized.equals("DISLIKE")) {
            throw new SQLException("Invalid reaction: " + reaction);
        }

        Connection conn = getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            String selectSql = "SELECT reaction FROM comment_reactions WHERE comment_id = ? AND user_id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, commentId);
                selectStmt.setInt(2, userId);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        String insertSql = "INSERT INTO comment_reactions (comment_id, user_id, reaction, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP())";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, commentId);
                            insertStmt.setInt(2, userId);
                            insertStmt.setString(3, normalized);
                            insertStmt.executeUpdate();
                        }

                        String updateSql = normalized.equals("LIKE") ? "UPDATE comments SET likes = COALESCE(likes,0) + 1 WHERE id = ?" : "UPDATE comments SET dislikes = COALESCE(dislikes,0) + 1 WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, commentId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        String existing = rs.getString("reaction");
                        if (existing == null) existing = "";
                        existing = existing.toUpperCase();
                        if (existing.equals(normalized)) {
                            String deleteSql = "DELETE FROM comment_reactions WHERE comment_id = ? AND user_id = ?";
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                                deleteStmt.setInt(1, commentId);
                                deleteStmt.setInt(2, userId);
                                deleteStmt.executeUpdate();
                            }

                            String decSql = normalized.equals("LIKE") ? "UPDATE comments SET likes = GREATEST(COALESCE(likes,0) - 1, 0) WHERE id = ?" : "UPDATE comments SET dislikes = GREATEST(COALESCE(dislikes,0) - 1, 0) WHERE id = ?";
                            try (PreparedStatement decStmt = conn.prepareStatement(decSql)) {
                                decStmt.setInt(1, commentId);
                                decStmt.executeUpdate();
                            }
                        } else {
                            String updateReactionSql = "UPDATE comment_reactions SET reaction = ?, created_at = CURRENT_TIMESTAMP() WHERE comment_id = ? AND user_id = ?";
                            try (PreparedStatement urs = conn.prepareStatement(updateReactionSql)) {
                                urs.setString(1, normalized);
                                urs.setInt(2, commentId);
                                urs.setInt(3, userId);
                                urs.executeUpdate();
                            }

                            String decPrev = existing.equals("LIKE") ? "UPDATE comments SET likes = GREATEST(COALESCE(likes,0) - 1, 0) WHERE id = ?" : "UPDATE comments SET dislikes = GREATEST(COALESCE(dislikes,0) - 1, 0) WHERE id = ?";
                            String incNew = normalized.equals("LIKE") ? "UPDATE comments SET likes = COALESCE(likes,0) + 1 WHERE id = ?" : "UPDATE comments SET dislikes = COALESCE(dislikes,0) + 1 WHERE id = ?";

                            try (PreparedStatement decStmt = conn.prepareStatement(decPrev)) {
                                decStmt.setInt(1, commentId);
                                decStmt.executeUpdate();
                            }
                            try (PreparedStatement incStmt = conn.prepareStatement(incNew)) {
                                incStmt.setInt(1, commentId);
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

    // Get username by user ID
    public String getUsernameByUserId(int userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return null;
    }
}
