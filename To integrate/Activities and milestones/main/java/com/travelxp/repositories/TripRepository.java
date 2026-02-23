package com.travelxp.repositories;

import com.travelxp.entities.Trip;
import com.travelxp.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {

    public List<Trip> findAll() throws SQLException {
        String sql = """
                SELECT id, user_id, trip_name, origin, destination, description,
                       start_date, end_date, status,
                       budget_amount, currency, total_expenses,
                       total_xp_earned, notes, cover_image_url,
                       created_at, updated_at
                FROM trips
                ORDER BY id DESC
                """;

        List<Trip> trips = new ArrayList<>();

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trips.add(mapRow(rs));
            }
        }
        return trips;
    }

    public boolean existsByNameAndDates(String tripName, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM trips WHERE trip_name = ? AND start_date = ? AND end_date = ?";

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, tripName);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void insert(Trip t) throws SQLException {
        String sql = """
                INSERT INTO trips
                (user_id, trip_name, origin, destination, description,
                 start_date, end_date, status,
                 budget_amount, currency, total_expenses,
                 total_xp_earned, notes, cover_image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, t.getUserId()); // ممكن null
            ps.setString(2, t.getTripName());
            ps.setString(3, t.getOrigin());
            ps.setString(4, t.getDestination());
            ps.setString(5, t.getDescription());
            ps.setDate(6, Date.valueOf(t.getStartDate()));
            ps.setDate(7, Date.valueOf(t.getEndDate()));
            ps.setString(8, t.getStatus());

            ps.setObject(9, t.getBudgetAmount());
            ps.setString(10, t.getCurrency());
            ps.setObject(11, t.getTotalExpenses());

            ps.setObject(12, t.getTotalXpEarned());
            ps.setString(13, t.getNotes());
            ps.setString(14, t.getCoverImageUrl());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
        }
    }

    public void update(Trip t) throws SQLException {
        String sql = """
                UPDATE trips SET
                    user_id = ?,
                    trip_name = ?,
                    origin = ?,
                    destination = ?,
                    description = ?,
                    start_date = ?,
                    end_date = ?,
                    status = ?,
                    budget_amount = ?,
                    currency = ?,
                    total_expenses = ?,
                    total_xp_earned = ?,
                    notes = ?,
                    cover_image_url = ?
                WHERE id = ?
                """;

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setObject(1, t.getUserId());
            ps.setString(2, t.getTripName());
            ps.setString(3, t.getOrigin());
            ps.setString(4, t.getDestination());
            ps.setString(5, t.getDescription());
            ps.setDate(6, Date.valueOf(t.getStartDate()));
            ps.setDate(7, Date.valueOf(t.getEndDate()));
            ps.setString(8, t.getStatus());

            ps.setObject(9, t.getBudgetAmount());
            ps.setString(10, t.getCurrency());
            ps.setObject(11, t.getTotalExpenses());

            ps.setObject(12, t.getTotalXpEarned());
            ps.setString(13, t.getNotes());
            ps.setString(14, t.getCoverImageUrl());

            ps.setLong(15, t.getId());

            ps.executeUpdate();
        }
    }

    public void deleteById(long id) throws SQLException {
        String sql = "DELETE FROM trips WHERE id = ?";

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Trip mapRow(ResultSet rs) throws SQLException {
        Trip t = new Trip();

        t.setId(rs.getLong("id"));
        t.setUserId((Long) rs.getObject("user_id"));
        t.setTripName(rs.getString("trip_name"));

        t.setOrigin(rs.getString("origin"));
        t.setDestination(rs.getString("destination"));
        t.setDescription(rs.getString("description"));

        Date sd = rs.getDate("start_date");
        Date ed = rs.getDate("end_date");
        t.setStartDate(sd != null ? sd.toLocalDate() : null);
        t.setEndDate(ed != null ? ed.toLocalDate() : null);

        t.setStatus(rs.getString("status"));

        t.setBudgetAmount((Double) rs.getObject("budget_amount"));
        t.setCurrency(rs.getString("currency"));
        t.setTotalExpenses((Double) rs.getObject("total_expenses"));

        t.setTotalXpEarned((Integer) rs.getObject("total_xp_earned"));

        t.setNotes(rs.getString("notes"));
        t.setCoverImageUrl(rs.getString("cover_image_url"));

        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));

        return t;
    }
}