package com.travelxp.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Offer {
	private Long id;
	private Long propertyId;
	private String title;
	private String description;
	private BigDecimal discountPercentage;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean isActive;
	private LocalDateTime createdAt;

	public Offer() {}

	public Offer(Long propertyId, String title, String description, BigDecimal discountPercentage, LocalDate startDate, LocalDate endDate, Boolean isActive, LocalDateTime createdAt) {
		this.propertyId = propertyId;
		this.title = title;
		this.description = description;
		this.discountPercentage = discountPercentage;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isActive = isActive;
		this.createdAt = createdAt;
	}

	public Offer(Long id, Long propertyId, String title, String description, BigDecimal discountPercentage, LocalDate startDate, LocalDate endDate, Boolean isActive, LocalDateTime createdAt) {
		this.id = id;
		this.propertyId = propertyId;
		this.title = title;
		this.description = description;
		this.discountPercentage = discountPercentage;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isActive = isActive;
		this.createdAt = createdAt;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getPropertyId() { return propertyId; }
	public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public BigDecimal getDiscountPercentage() { return discountPercentage; }
	public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

	public LocalDate getStartDate() { return startDate; }
	public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

	public LocalDate getEndDate() { return endDate; }
	public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean isActive) { this.isActive = isActive; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	@Override
	public String toString() {
		return "Offer{" +
				"id=" + id +
				", propertyId=" + propertyId +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", discountPercentage=" + discountPercentage +
				", startDate=" + startDate +
				", endDate=" + endDate +
				", isActive=" + isActive +
				", createdAt=" + createdAt +
				'}';
	}
}
