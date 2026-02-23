package com.travelxp.services;

import com.travelxp.entities.TripMilestone;
import com.travelxp.repositories.TripMilestoneRepository;

import java.sql.SQLException;
import java.util.List;

public class TripMilestoneService {

    private final TripMilestoneRepository repo = new TripMilestoneRepository();

    public List<TripMilestone> getAllMilestones() throws SQLException {
        return repo.findAll();
    }

    public void addMilestone(TripMilestone m) throws SQLException {
        repo.insert(m);
    }

    public void updateMilestone(TripMilestone m) throws SQLException {
        repo.update(m);
    }

    public void deleteMilestone(Long id) throws SQLException {
        repo.delete(id);
    }
}