package com.travelxp.services;

import com.travelxp.models.TripMilestone;
import com.travelxp.repositories.TripMilestoneRepository;

import java.sql.SQLException;
import java.util.List;

public class TripMilestoneService {

    private final TripMilestoneRepository repo = new TripMilestoneRepository();

    public List<TripMilestone> getAllMilestones() throws SQLException {
        return repo.findAll();
    }

    public List<TripMilestone> getMilestonesByTripId(Long tripId) throws SQLException {
        return repo.findByTripId(tripId);
    }

    public void addMilestone(TripMilestone m) throws SQLException {
        if (m.getTitle() == null || m.getTitle().isBlank()) {
            throw new IllegalArgumentException("Milestone title is required.");
        }
        repo.insert(m);
    }

    public void updateMilestone(TripMilestone m) throws SQLException {
        if (m.getId() == null) throw new IllegalArgumentException("Milestone ID is required for update.");
        repo.update(m);
    }

    public void deleteMilestone(Long id) throws SQLException {
        repo.delete(id);
    }
}
