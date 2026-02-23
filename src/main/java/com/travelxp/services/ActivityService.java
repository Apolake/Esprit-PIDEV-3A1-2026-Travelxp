package com.travelxp.services;

import com.travelxp.models.Activity;
import com.travelxp.repositories.ActivityRepository;

import java.sql.SQLException;
import java.util.List;

public class ActivityService {

    private final ActivityRepository repo = new ActivityRepository();

    public List<Activity> getAllActivities() throws SQLException {
        return repo.findAll();
    }

    public List<Activity> getActivitiesByTripId(Long tripId) throws SQLException {
        return repo.findByTripId(tripId);
    }

    public void addActivity(Activity a) throws SQLException {
        if (a.getTitle() == null || a.getTitle().isBlank()) {
            throw new IllegalArgumentException("Activity title is required.");
        }
        repo.insert(a);
    }

    public void updateActivity(Activity a) throws SQLException {
        if (a.getId() == null) throw new IllegalArgumentException("Activity ID is required for update.");
        repo.update(a);
    }

    public void deleteActivity(Long id) throws SQLException {
        repo.delete(id);
    }
}
