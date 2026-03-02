package com.travelxp.services;

import com.travelxp.models.Feedback;
import com.travelxp.models.Comment;
import com.travelxp.repositories.FeedbackRepository;
import com.travelxp.repositories.CommentRepository;

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

    public Feedback getFeedbackById(int id) throws SQLException {
        return feedbackRepo.getFeedbackById(id);
    }

    public void updateFeedback(Feedback fb) throws SQLException {
        // Validation: Check content is not empty
        if (fb.getContent() == null || fb.getContent().trim().isEmpty()) {
            throw new SQLException("Feedback content cannot be empty");
        }

        feedbackRepo.updateFeedback(fb);
    }

    public void deleteFeedback(int id) throws SQLException {
        feedbackRepo.deleteFeedback(id);
    }

    // --- Comment methods ---
    public void addComment(Comment c) throws SQLException {
        // Validation: Check content is not empty
        if (c.getContent() == null || c.getContent().trim().isEmpty()) {
            throw new SQLException("Comment cannot be empty");
        }

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

    public void updateComment(Comment c) throws SQLException {
        // Validation: Check content is not empty
        if (c.getContent() == null || c.getContent().trim().isEmpty()) {
            throw new SQLException("Comment cannot be empty");
        }

        commentRepo.updateComment(c);
    }

    public void deleteComment(int id) throws SQLException {
        commentRepo.deleteComment(id);
    }
}
