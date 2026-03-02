package com.travelxp.services;

import com.travelxp.models.Feedback;
import com.travelxp.models.Comment;
import com.travelxp.repositories.FeedbackRepository;
import com.travelxp.repositories.CommentRepository;
import com.travelxp.utils.ProfanityFilter;
import com.travelxp.utils.SentimentAnalyzer;

import java.sql.SQLException;
import java.util.List;

public class FeedbackService {

    private FeedbackRepository feedbackRepo;
    private CommentRepository commentRepo;

    // Default constructor
    public FeedbackService() {
        this.feedbackRepo = new FeedbackRepository();
        this.commentRepo = new CommentRepository();
    }

    // Constructor for both repos
    public FeedbackService(FeedbackRepository feedbackRepo, CommentRepository commentRepo) {
        this.feedbackRepo = feedbackRepo;
        this.commentRepo = commentRepo;
    }

    // --- Feedback methods ---
    public void createFeedback(Feedback fb) throws SQLException {
        // Validation: Check content is not empty
        if (fb.getContent() == null || fb.getContent().trim().isEmpty()) {
            throw new SQLException("Feedback content cannot be empty");
        }

        // Profanity filtering
        fb.setContent(ProfanityFilter.sanitize(fb.getContent()));
        fb.setTitle(ProfanityFilter.sanitize(fb.getTitle()));

        // Sentiment analysis
        fb.setSentiment(SentimentAnalyzer.analyze(fb.getContent()));

        // Validation: Check user exists
        if (!feedbackRepo.userExists(fb.getUserId())) {
            throw new SQLException("User ID does not exist");
        }

        // Validation: Check for duplicate feedback content from same user
        if (feedbackRepo.duplicateFeedbackExists(fb.getUserId(), fb.getContent())) {
            throw new SQLException("You have already posted this feedback");
        }

        feedbackRepo.createFeedback(fb);
    }

    public List<Feedback> getAllFeedback() throws SQLException {
        return feedbackRepo.getAllFeedback();
    }

    // search/filter feedbacks
    public List<Feedback> searchFeedback(String usernameFilter, String titleFilter, String sortBy, boolean asc) throws SQLException {
        return feedbackRepo.searchFeedback(usernameFilter, titleFilter, sortBy, asc);
    }

    public Feedback getFeedbackById(int id) throws SQLException {
        return feedbackRepo.getFeedbackById(id);
    }

    public void updateFeedback(Feedback fb) throws SQLException {
        // Validation: Check content is not empty
        if (fb.getContent() == null || fb.getContent().trim().isEmpty()) {
            throw new SQLException("Feedback content cannot be empty");
        }

        // Profanity filtering and sentiment
        fb.setContent(ProfanityFilter.sanitize(fb.getContent()));
        fb.setTitle(ProfanityFilter.sanitize(fb.getTitle()));
        fb.setSentiment(SentimentAnalyzer.analyze(fb.getContent()));

        feedbackRepo.updateFeedback(fb);
    }

    public void deleteFeedback(int id) throws SQLException {
        feedbackRepo.deleteFeedback(id);
    }

    // Reaction wrappers
    public void toggleFeedbackReaction(int feedbackId, int userId, String reaction) throws SQLException {
        feedbackRepo.toggleFeedbackReaction(feedbackId, userId, reaction);
        // Update sentiment based on new likes/dislikes ratio
        Feedback feedback = feedbackRepo.getFeedbackById(feedbackId);
        if (feedback != null) {
            feedback.updateSentiment();
            feedbackRepo.updateFeedbackSentiment(feedbackId, feedback.getSentiment());
        }
    }

    public void toggleCommentReaction(int commentId, int userId, String reaction) throws SQLException {
        commentRepo.toggleCommentReaction(commentId, userId, reaction);
    }

    // --- Comment methods ---
    public void addComment(Comment c) throws SQLException {
        // Validation: Check content is not empty
        if (c.getContent() == null || c.getContent().trim().isEmpty()) {
            throw new SQLException("Comment cannot be empty");
        }

        // Profanity filter
        c.setContent(ProfanityFilter.sanitize(c.getContent()));

        // Validation: Check user exists
        if (!commentRepo.userExists(c.getUserId())) {
            throw new SQLException("User ID does not exist");
        }

        // Validation: Check for duplicate comment from same user on same feedback
        if (commentRepo.duplicateCommentExists(c.getFeedbackId(), c.getUserId(), c.getContent())) {
            throw new SQLException("You have already posted this comment on this feedback");
        }

        commentRepo.addComment(c);
    }

    public List<Comment> getCommentsByFeedback(int feedbackId) throws SQLException {
        return commentRepo.getCommentsByFeedbackId(feedbackId);
    }

    public List<Comment> getCommentsByFeedback(int feedbackId, String sortBy, boolean asc) throws SQLException {
        return commentRepo.getCommentsByFeedbackId(feedbackId, sortBy, asc);
    }

    public void updateComment(Comment c) throws SQLException {
        // Validation: Check content is not empty
        if (c.getContent() == null || c.getContent().trim().isEmpty()) {
            throw new SQLException("Comment cannot be empty");
        }

        // Profanity filter
        c.setContent(ProfanityFilter.sanitize(c.getContent()));

        commentRepo.updateComment(c);
    }

    public void deleteComment(int id) throws SQLException {
        commentRepo.deleteComment(id);
    }

    // Get username by user ID for comments
    public String getCommentUsername(int userId) throws SQLException {
        return commentRepo.getUsernameByUserId(userId);
    }

    // Toggle favorite status for a feedback
    public void toggleFavorite(int feedbackId, int userId) throws SQLException {
        feedbackRepo.toggleFavorite(feedbackId, userId);
    }

    // Check if feedback is favorited by user
    public boolean isFavorited(int feedbackId, int userId) throws SQLException {
        return feedbackRepo.isFavorited(feedbackId, userId);
    }

    // Get favorited feedbacks for a user
    public List<Feedback> getFavoritedFeedbacks(int userId, String sortBy, boolean asc) throws SQLException {
        return feedbackRepo.getFavoritedFeedbacks(userId, sortBy, asc);
    }
}
