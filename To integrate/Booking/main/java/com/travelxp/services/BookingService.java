package com.travelxp.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.travelxp.models.Booking;
import com.travelxp.utils.MyDB;

public class BookingService {

    private Connection cnx;

    public BookingService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // CREATE
    public void addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO booking (user_id, trip_id, service_id, booking_date, booking_status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, booking.getUserId());
        ps.setInt(2, booking.getTripId());
        ps.setInt(3, booking.getServiceId());
        ps.setDate(4, booking.getBookingDate());
        ps.setString(5, booking.getBookingStatus());
        ps.executeUpdate();
    }

    // READ
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Booking b = new Booking(
                rs.getInt("user_id"),
                rs.getInt("trip_id"),
                rs.getInt("service_id"),
                rs.getDate("booking_date"),
                rs.getString("booking_status")
            );
            b.setBookingId(rs.getInt("booking_id"));
            bookings.add(b);
        }
        return bookings;
    }

    // UPDATE
    public void updateBookingStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE booking SET booking_status=? WHERE booking_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, bookingId);
        ps.executeUpdate();
    }

    // DELETE
    public void deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM booking WHERE booking_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, bookingId);
        ps.executeUpdate();
    }
}
