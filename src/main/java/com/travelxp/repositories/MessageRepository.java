package com.travelxp.repositories;

import com.travelxp.models.Message;
import com.travelxp.utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    /**
     * Insert a new message into the messages table
     */
    public void sendMessage(Message message) throws SQLException {
        String sql = "INSERT INTO messages (conversation_id, sender_id, content) VALUES (?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, message.getConversationId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());
            stmt.executeUpdate();
        }
    }

    /**
     * Get all messages in a conversation, ordered by creation time (oldest first)
     */
    public List<Message> getMessagesByConversation(int conversationId) throws SQLException {
        String sql = "SELECT m.*, u.username FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE m.conversation_id = ? " +
                     "ORDER BY m.created_at ASC";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message msg = new Message(
                            rs.getInt("id"),
                            rs.getInt("conversation_id"),
                            rs.getInt("sender_id"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    );
                    msg.setSenderUsername(rs.getString("username"));
                    messages.add(msg);
                }
            }
        }
        return messages;
    }

    /**
     * Mark a message as read
     */
    public void markMessageAsRead(int messageId) throws SQLException {
        String sql = "UPDATE messages SET is_read = TRUE WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            stmt.executeUpdate();
        }
    }

    /**
     * Mark all messages in a conversation as read for a specific user (receiver)
     */
    public void markConversationAsRead(int conversationId, int userId) throws SQLException {
        String sql = "UPDATE messages SET is_read = TRUE " +
                     "WHERE conversation_id = ? AND sender_id != ? AND is_read = FALSE";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get count of unread messages in a conversation for a specific user
     */
    public int getUnreadCount(int conversationId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM messages " +
                     "WHERE conversation_id = ? AND sender_id != ? AND is_read = FALSE";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get the last message in a conversation
     */
    public Message getLastMessage(int conversationId) throws SQLException {
        String sql = "SELECT m.*, u.username FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE m.conversation_id = ? " +
                     "ORDER BY m.created_at DESC LIMIT 1";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Message msg = new Message(
                            rs.getInt("id"),
                            rs.getInt("conversation_id"),
                            rs.getInt("sender_id"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    );
                    msg.setSenderUsername(rs.getString("username"));
                    return msg;
                }
            }
        }
        return null;
    }

    /**
     * Delete a message
     */
    public void deleteMessage(int messageId) throws SQLException {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            stmt.executeUpdate();
        }
    }
}
