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
        String sql = "INSERT INTO feedback (fcontent, user_id, created_at) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, feedback.getContent());
            pstmt.setInt(2, feedback.getUserId());
            pstmt.setTimestamp(3, Timestamp.valueOf(feedback.getCreatedAt()));
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Get all feedbacks
    public List<Feedback> getAllFeedback() throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT id, fcontent, user_id, created_at FROM feedback ORDER BY created_at DESC";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("fcontent");
                int userId = rs.getInt("user_id");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                feedbacks.add(new Feedback(id, content, userId, createdAt));
            }
        }
        return feedbacks;
    }

    // Get feedback by ID
    public Feedback getFeedbackById(int id) throws SQLException {
        String sql = "SELECT id, fcontent, user_id, created_at FROM feedback WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String content = rs.getString("fcontent");
                    int userId = rs.getInt("user_id");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    
                    return new Feedback(id, content, userId, createdAt);
                }
            }
        }
        return null;
    }

    // Update a feedback
    public void updateFeedback(Feedback feedback) throws SQLException {
        String sql = "UPDATE feedback SET fcontent = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            
            pstmt.setString(1, feedback.getContent());
            pstmt.setInt(2, feedback.getId());
            
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
}
