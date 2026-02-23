package com.travelxp.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TripMilestone {

    private Long id;
    private Long tripId;

    private String title;
    private String description;

    private LocalDate milestoneDate;

    private String status;     // PLANNED / DONE / SKIPPED
    private Integer xpEarned;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TripMilestone() {}

    public TripMilestone(Long id, Long tripId, String title, String description,
                         LocalDate milestoneDate, String status, Integer xpEarned,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tripId = tripId;
        this.title = title;
        this.description = description;
        this.milestoneDate = milestoneDate;
        this.status = status;
        this.xpEarned = xpEarned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getMilestoneDate() { return milestoneDate; }
    public void setMilestoneDate(LocalDate milestoneDate) { this.milestoneDate = milestoneDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getXpEarned() { return xpEarned; }
    public void setXpEarned(Integer xpEarned) { this.xpEarned = xpEarned; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
