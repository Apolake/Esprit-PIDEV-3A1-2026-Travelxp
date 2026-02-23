package com.travelxp.models;

import java.sql.Date;

public class Booking {

    private int bookingId;
    private int userId;
    private int tripId;
    private int serviceId;
    private Date bookingDate;
    private String bookingStatus;
    private int duration; // In days
    private double totalPrice;

    public Booking() {}

   public Booking(int userId, int tripId, int serviceId, Date bookingDate, String bookingStatus, int duration, double totalPrice) {
    this.userId = userId;
    this.tripId = tripId;
    this.serviceId = serviceId;
    this.bookingDate = bookingDate;
    this.bookingStatus = bookingStatus;
    this.duration = duration;
    this.totalPrice = totalPrice;
}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    
    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }
    
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
