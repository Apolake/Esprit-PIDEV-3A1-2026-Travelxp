package com.travelxp.services;

import com.travelxp.entities.Activity;
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

    public Activity getById(Long id) throws SQLException {
        return repo.findById(id);
    }

    public void addActivity(Activity a) throws SQLException {
        repo.insert(a);
    }

    public void updateActivity(Activity a) throws SQLException {
        repo.update(a);
    }

    public void deleteActivity(Long id) throws SQLException {
        repo.delete(id);
    }
}