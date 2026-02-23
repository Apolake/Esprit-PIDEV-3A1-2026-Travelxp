package com.travelxp.models;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Trip {

    private Long id;
    private Long userId;
    private String tripName;

    private String origin;
    private String destination;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;

    private Double budgetAmount;
    private String currency;
    private Double totalExpenses;

    private Integer totalXpEarned;

    private String notes;
    private String coverImageUrl;
    private Long parentId;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Trip() {
    }

    public Trip(Long id, Long userId, String tripName, String origin, String destination, String description,
                LocalDate startDate, LocalDate endDate, String status,
                Double budgetAmount, String currency, Double totalExpenses,
                Integer totalXpEarned, String notes, String coverImageUrl,
                Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.tripName = tripName;
        this.origin = origin;
        this.destination = destination;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.budgetAmount = budgetAmount;
        this.currency = currency;
        this.totalExpenses = totalExpenses;
        this.totalXpEarned = totalXpEarned;
        this.notes = notes;
        this.coverImageUrl = coverImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---------- Getters & Setters ----------

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTripName() { return tripName; }
    public void setTripName(String tripName) { this.tripName = tripName; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }

    public Integer getTotalXpEarned() { return totalXpEarned; }
    public void setTotalXpEarned(Integer totalXpEarned) { this.totalXpEarned = totalXpEarned; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return tripName + " (" + destination + ")";
    }
}
