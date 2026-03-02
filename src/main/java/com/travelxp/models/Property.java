package com.travelxp.models;

import java.math.BigDecimal;

public class Property {
	private Long id;
	private Long ownerId;
	private String title;
	private String description;
	private String propertyType;
	private String address;
	private String city;
	private String country;
	private Integer bedrooms;
	private Integer bathrooms;
	private Integer maxGuests;
	private BigDecimal pricePerNight;
	private String images;
	private Boolean isActive;

	public Property() {}

	public Property(Long ownerId, String title, String description, String propertyType, String address, String city, String country, Integer bedrooms, Integer bathrooms, Integer maxGuests, BigDecimal pricePerNight, String images, Boolean isActive) {
		this.ownerId = ownerId;
		this.title = title;
		this.description = description;
		this.propertyType = propertyType;
		this.address = address;
		this.city = city;
		this.country = country;
		this.bedrooms = bedrooms;
		this.bathrooms = bathrooms;
		this.maxGuests = maxGuests;
		this.pricePerNight = pricePerNight;
		this.images = images;
		this.isActive = isActive;
	}

	public Property(Long id, Long ownerId, String title, String description, String propertyType, String address, String city, String country, Integer bedrooms, Integer bathrooms, Integer maxGuests, BigDecimal pricePerNight, String images, Boolean isActive) {
		this.id = id;
		this.ownerId = ownerId;
		this.title = title;
		this.description = description;
		this.propertyType = propertyType;
		this.address = address;
		this.city = city;
		this.country = country;
		this.bedrooms = bedrooms;
		this.bathrooms = bathrooms;
		this.maxGuests = maxGuests;
		this.pricePerNight = pricePerNight;
		this.images = images;
		this.isActive = isActive;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getOwnerId() { return ownerId; }
	public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getPropertyType() { return propertyType; }
	public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }

	public String getCountry() { return country; }
	public void setCountry(String country) { this.country = country; }

	public Integer getBedrooms() { return bedrooms; }
	public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

	public Integer getBathrooms() { return bathrooms; }
	public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }

	public Integer getMaxGuests() { return maxGuests; }
	public void setMaxGuests(Integer maxGuests) { this.maxGuests = maxGuests; }

	public BigDecimal getPricePerNight() { return pricePerNight; }
	public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

	public String getImages() { return images; }
	public void setImages(String images) { this.images = images; }

	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean isActive) { this.isActive = isActive; }

	@Override
	public String toString() {
		return "Property{" +
				"id=" + id +
				", ownerId=" + ownerId +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", propertyType='" + propertyType + '\'' +
				", address='" + address + '\'' +
				", city='" + city + '\'' +
				", country='" + country + '\'' +
				", bedrooms=" + bedrooms +
				", bathrooms=" + bathrooms +
				", maxGuests=" + maxGuests +
				", pricePerNight=" + pricePerNight +
				", images='" + images + '\'' +
				", isActive=" + isActive +
				'}';
	}
}
