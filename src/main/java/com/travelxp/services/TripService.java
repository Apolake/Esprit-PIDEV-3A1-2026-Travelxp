package com.travelxp.services;

import com.travelxp.models.Trip;
import com.travelxp.repositories.TripRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TripService {

    private final TripRepository repo = new TripRepository();

    public List<Trip> getAllTrips() throws SQLException {
        return repo.findAll();
    }

    public List<Trip> getTripsByUserId(int userId) throws SQLException {
        return repo.findByUserId(userId);
    }

    public List<Trip> getParticipantsByTripId(long parentId) throws SQLException {
        return repo.findByParentId(parentId);
    }

    public boolean isUserParticipating(long templateId, int userId) throws SQLException {
        return repo.existsByParentAndUser(templateId, userId);
    }

    public void addTripParticipant(long templateId, int userId) throws SQLException {
        repo.addTripParticipant(templateId, userId);
    }

    public void addActivityParticipant(long templateActivityId, int userId) throws SQLException {
        repo.addActivityParticipant(templateActivityId, userId);
    }

    public void removeTripParticipant(long templateId, int userId) throws SQLException {
        repo.removeTripParticipant(templateId, userId);
    }

    public void removeActivityParticipant(long templateActivityId, int userId) throws SQLException {
        repo.removeActivityParticipant(templateActivityId, userId);
    }

    public void addTrip(Trip t) throws SQLException {
        validate(t);

        boolean exists = repo.existsByNameAndDates(t.getTripName(), t.getStartDate(), t.getEndDate(), t.getUserId());
        if (exists) {
            throw new IllegalArgumentException("You already have a trip with this name and dates.");
        }

        if (t.getStatus() == null || t.getStatus().trim().isEmpty()) t.setStatus("PLANNED");
        if (t.getTotalXpEarned() == null) t.setTotalXpEarned(0);
        if (t.getTotalExpenses() == null) t.setTotalExpenses(0.0);

        repo.insert(t);
    }

    public void updateTrip(Trip t) throws SQLException {
        if (t.getId() == null) throw new IllegalArgumentException("Trip id is required for update.");
        validate(t);

        if (t.getStatus() == null || t.getStatus().trim().isEmpty()) t.setStatus("PLANNED");
        if (t.getTotalXpEarned() == null) t.setTotalXpEarned(0);
        if (t.getTotalExpenses() == null) t.setTotalExpenses(0.0);

        repo.update(t);
    }

    public void deleteTrip(long id) throws SQLException {
        repo.deleteById(id);
    }

    private void validate(Trip t) {
        if (t.getTripName() == null || t.getTripName().trim().isEmpty()) {
            throw new IllegalArgumentException("Trip name is required.");
        }

        LocalDate start = t.getStartDate();
        LocalDate end = t.getEndDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        if (t.getBudgetAmount() != null && t.getBudgetAmount() < 0) {
            throw new IllegalArgumentException("Budget amount cannot be negative.");
        }

        if (t.getTotalExpenses() != null && t.getTotalExpenses() < 0) {
            throw new IllegalArgumentException("Total expenses cannot be negative.");
        }

        if (t.getTotalXpEarned() != null && t.getTotalXpEarned() < 0) {
            throw new IllegalArgumentException("Total XP cannot be negative.");
        }
    }
}
