package com.travelxp.models;

import java.sql.Date;

public class Booking {

    private int bookingId;
    private int userId;
    private int tripId;
    private int serviceId;
    private Date bookingDate;
    private String bookingStatus;

    public Booking() {}

   public Booking(int userId, int tripId, int serviceId, Date bookingDate, String bookingStatus) {
    this.userId = userId;
    this.tripId = tripId;
    this.serviceId = serviceId;
    this.bookingDate = bookingDate;
    this.bookingStatus = bookingStatus;
}


    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public int getTripId() { return tripId; }
    public int getServiceId() { return serviceId; }
    public Date getBookingDate() { return bookingDate; }
    public String getBookingStatus() { return bookingStatus; }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
