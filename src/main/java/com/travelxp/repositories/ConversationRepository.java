package com.travelxp.repositories;

import com.travelxp.models.Conversation;
import com.travelxp.utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationRepository {

    /**
     * Get or create a conversation between two users
     * Ensures that the lower user ID is always user1_id
     */
    public Conversation getOrCreateConversation(int user1Id, int user2Id, Integer feedbackId) throws SQLException {
        // Ensure consistent ordering: lower ID is user1_id
        if (user1Id > user2Id) {
            int temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        // Try to get existing conversation
        String selectSql = "SELECT * FROM conversations WHERE user1_id = ? AND user2_id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Conversation(
                            rs.getInt("id"),
                            rs.getInt("user1_id"),
                            rs.getInt("user2_id"),
                            rs.getObject("feedback_id") != null ? rs.getInt("feedback_id") : null,
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        }

        // Create new conversation if it doesn't exist
        String insertSql = "INSERT INTO conversations (user1_id, user2_id, feedback_id) VALUES (?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            if (feedbackId != null) {
                stmt.setInt(3, feedbackId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int conversationId = generatedKeys.getInt(1);
                    return new Conversation(conversationId, user1Id, user2Id, feedbackId,
                            LocalDateTime.now(), LocalDateTime.now());
                }
            }
        }
        throw new SQLException("Failed to create conversation");
    }

    /**
     * Get all conversations for a user, ordered by most recent update
     */
    public List<Conversation> getConversationsForUser(int userId) throws SQLException {
        String sql = "SELECT c.*, " +
                     "CASE WHEN c.user1_id = ? THEN u2.username ELSE u1.username END as other_username " +
                     "FROM conversations c " +
                     "JOIN users u1 ON c.user1_id = u1.id " +
                     "JOIN users u2 ON c.user2_id = u2.id " +
                     "WHERE c.user1_id = ? OR c.user2_id = ? " +
                     "ORDER BY c.updated_at DESC";

        List<Conversation> conversations = new ArrayList<>();
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conv = new Conversation(
                            rs.getInt("id"),
                            rs.getInt("user1_id"),
                            rs.getInt("user2_id"),
                            rs.getObject("feedback_id") != null ? rs.getInt("feedback_id") : null,
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                    conv.setOtherUsername(rs.getString("other_username"));
                    conversations.add(conv);
                }
            }
        }
        return conversations;
    }

    /**
     * Get a specific conversation by ID
     */
    public Conversation getConversationById(int conversationId) throws SQLException {
        String sql = "SELECT c.*, " +
                     "u1.username as user1_username, " +
                     "u2.username as user2_username " +
                     "FROM conversations c " +
                     "JOIN users u1 ON c.user1_id = u1.id " +
                     "JOIN users u2 ON c.user2_id = u2.id " +
                     "WHERE c.id = ?";

        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Conversation(
                            rs.getInt("id"),
                            rs.getInt("user1_id"),
                            rs.getInt("user2_id"),
                            rs.getObject("feedback_id") != null ? rs.getInt("feedback_id") : null,
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    /**
     * Update the updated_at timestamp (called when a message is sent)
     */
    public void updateConversationTimestamp(int conversationId) throws SQLException {
        String sql = "UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a conversation
     */
    public void deleteConversation(int conversationId) throws SQLException {
        String sql = "DELETE FROM conversations WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Check if a conversation exists between two users
     */
    public boolean conversationExists(int user1Id, int user2Id) throws SQLException {
        if (user1Id > user2Id) {
            int temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        String sql = "SELECT 1 FROM conversations WHERE user1_id = ? AND user2_id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
