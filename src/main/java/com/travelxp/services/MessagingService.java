package com.travelxp.services;

import com.travelxp.models.Conversation;
import com.travelxp.models.Message;
import com.travelxp.repositories.ConversationRepository;
import com.travelxp.repositories.MessageRepository;

import java.sql.SQLException;
import java.util.List;

public class MessagingService {
    private final MessageRepository messageRepository = new MessageRepository();
    private final ConversationRepository conversationRepository = new ConversationRepository();

    /**
     * Send a message to a conversation
     */
    public void sendMessage(int conversationId, int senderId, String content) throws SQLException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        Message message = new Message(conversationId, senderId, content.trim());
        messageRepository.sendMessage(message);

        // Update conversation timestamp
        conversationRepository.updateConversationTimestamp(conversationId);
    }

    /**
     * Get or create a conversation between two users
     */
    public Conversation getOrCreateConversation(int user1Id, int user2Id, Integer feedbackId) throws SQLException {
        if (user1Id == user2Id) {
            throw new IllegalArgumentException("Cannot create conversation with yourself");
        }
        return conversationRepository.getOrCreateConversation(user1Id, user2Id, feedbackId);
    }

    /**
     * Get all messages in a conversation
     */
    public List<Message> getConversationMessages(int conversationId) throws SQLException {
        return messageRepository.getMessagesByConversation(conversationId);
    }

    /**
     * Mark all messages as read in a conversation for the current user
     */
    public void markConversationAsRead(int conversationId, int userId) throws SQLException {
        messageRepository.markConversationAsRead(conversationId, userId);
    }

    /**
     * Get unread count for a conversation
     */
    public int getUnreadCount(int conversationId, int userId) throws SQLException {
        return messageRepository.getUnreadCount(conversationId, userId);
    }

    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations(int userId) throws SQLException {
        List<Conversation> conversations = conversationRepository.getConversationsForUser(userId);
        
        // Fetch and set unread count for each conversation
        for (Conversation conv : conversations) {
            int unreadCount = getUnreadCount(conv.getId(), userId);
            conv.setUnreadCount(unreadCount);
        }
        
        return conversations;
    }

    /**
     * Get a specific conversation by ID
     */
    public Conversation getConversation(int conversationId) throws SQLException {
        return conversationRepository.getConversationById(conversationId);
    }

    /**
     * Delete a conversation (with all its messages)
     */
    public void deleteConversation(int conversationId) throws SQLException {
        conversationRepository.deleteConversation(conversationId);
    }

    /**
     * Check if conversation exists between two users
     */
    public boolean conversationExists(int user1Id, int user2Id) throws SQLException {
        return conversationRepository.conversationExists(user1Id, user2Id);
    }
}
