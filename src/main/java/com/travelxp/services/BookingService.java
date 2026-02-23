package com.travelxp.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.travelxp.models.Booking;
import com.travelxp.models.Service;
import com.travelxp.utils.MyDB;

public class BookingService {

    private Connection getConnection() {
        return MyDB.getInstance().getConnection();
    }

    // CREATE
    public void addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO booking (user_id, property_id, trip_id, service_id, booking_date, booking_status, duration, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getUserId());
            if (booking.getPropertyId() != null) ps.setLong(2, booking.getPropertyId()); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setInt(3, booking.getTripId());
            ps.setInt(4, booking.getServiceId());
            ps.setDate(5, booking.getBookingDate());
            ps.setString(6, booking.getBookingStatus());
            ps.setInt(7, booking.getDuration());
            ps.setDouble(8, booking.getTotalPrice());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    booking.setBookingId(rs.getInt(1));
                    saveBookingServices(booking);
                }
            }
        }
    }

    private void saveBookingServices(Booking booking) throws SQLException {
        if (booking.getExtraServices() == null || booking.getExtraServices().isEmpty()) return;
        
        String sql = "INSERT INTO booking_services (booking_id, service_id) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (Service s : booking.getExtraServices()) {
                ps.setInt(1, booking.getBookingId());
                ps.setInt(2, s.getServiceId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // READ
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Booking b = mapResultSetToBooking(rs);
                b.setExtraServices(getServicesForBooking(b.getBookingId()));
                bookings.add(b);
            }
        }
        return bookings;
    }

    // READ BY USER
    public List<Booking> getBookingsByUserId(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapResultSetToBooking(rs);
                    b.setExtraServices(getServicesForBooking(b.getBookingId()));
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }

    private List<Service> getServicesForBooking(int bookingId) throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.* FROM service s JOIN booking_services bs ON s.service_id = bs.service_id WHERE bs.booking_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Service s = new Service(
                        rs.getString("provider_name"),
                        rs.getString("service_type"),
                        rs.getDouble("price"),
                        rs.getBoolean("eco_friendly"),
                        rs.getInt("xp_reward")
                    );
                    s.setServiceId(rs.getInt("service_id"));
                    services.add(s);
                }
            }
        }
        return services;
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
            rs.getInt("user_id"),
            rs.getLong("property_id") == 0 ? null : rs.getLong("property_id"),
            rs.getInt("trip_id"),
            rs.getInt("service_id"),
            rs.getDate("booking_date"),
            rs.getString("booking_status"),
            rs.getInt("duration"),
            rs.getDouble("total_price")
        );
        b.setBookingId(rs.getInt("booking_id"));
        return b;
    }

    // UPDATE DURATION
    public void updateBookingDuration(int bookingId, int duration) throws SQLException {
        String sql = "UPDATE booking SET duration=? WHERE booking_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, duration);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    // UPDATE PRICE
    public void updateBookingPrice(int bookingId, double totalPrice) throws SQLException {
        String sql = "UPDATE booking SET total_price=? WHERE booking_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, totalPrice);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    public void updateBookingDate(int bookingId, java.sql.Date newDate) throws SQLException {
        String sql = "UPDATE booking SET booking_date=? WHERE booking_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, newDate);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    // UPDATE STATUS
    public void updateBookingStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE booking SET booking_status=? WHERE booking_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM booking WHERE booking_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }
}
