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
        String sql = "INSERT INTO comments (feedback_id, user_id, comment_text, created_at) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, comment.getFeedbackId());
            pstmt.setInt(2, comment.getUserId());
            pstmt.setString(3, comment.getContent());
            pstmt.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Get all comments for a specific feedback
    public List<Comment> getCommentsByFeedbackId(int feedbackId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, feedback_id, user_id, comment_text, created_at FROM comments WHERE feedback_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setInt(1, feedbackId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    String content = rs.getString("comment_text");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    
                    comments.add(new Comment(id, feedbackId, userId, content, createdAt));
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
}
