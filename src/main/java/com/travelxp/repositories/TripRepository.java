package com.travelxp.repositories;

import com.travelxp.models.Trip;
import com.travelxp.utils.MyDB;

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
                       created_at, updated_at, parent_id
                FROM trips
                ORDER BY id DESC
                """;

        List<Trip> trips = new ArrayList<>();
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trips.add(mapRow(rs));
            }
        }
        return trips;
    }

    public List<Trip> findByUserId(int userId) throws SQLException {
        String sql = """
                SELECT id, user_id, trip_name, origin, destination, description,
                       start_date, end_date, status,
                       budget_amount, currency, total_expenses,
                       total_xp_earned, notes, cover_image_url,
                       created_at, updated_at, parent_id
                FROM trips WHERE user_id = ? ORDER BY start_date DESC
                """;
        List<Trip> trips = new ArrayList<>();
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(mapRow(rs));
                }
            }
        }
        return trips;
    }

    public List<Trip> findByParentId(long parentId) throws SQLException {
        String sql = "SELECT * FROM trips WHERE parent_id = ?";
        List<Trip> trips = new ArrayList<>();
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(mapRow(rs));
                }
            }
        }
        return trips;
    }

    public boolean existsByNameAndDates(String tripName, LocalDate startDate, LocalDate endDate, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM trips WHERE trip_name = ? AND start_date = ? AND end_date = ? AND (user_id = ? OR (user_id IS NULL AND ? IS NULL))";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, tripName);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            if (userId != null) ps.setLong(4, userId); else ps.setNull(4, Types.INTEGER);
            if (userId != null) ps.setLong(5, userId); else ps.setNull(5, Types.INTEGER);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByParentAndUser(long parentId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM trips WHERE parent_id = ? AND user_id = ?";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, parentId);
            ps.setInt(2, userId);
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
                 total_xp_earned, notes, cover_image_url, parent_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (t.getUserId() != null) ps.setLong(1, t.getUserId());
            else ps.setNull(1, Types.INTEGER);
            
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
            
            if (t.getParentId() != null) ps.setLong(15, t.getParentId());
            else ps.setNull(15, Types.BIGINT);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
        }
    }

    public void addTripParticipant(long tripId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO trip_participants (trip_id, user_id) VALUES (?, ?)";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, tripId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void addActivityParticipant(long activityId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO trip_activity_participants (activity_id, user_id) VALUES (?, ?)";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, activityId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void removeTripParticipant(long tripId, int userId) throws SQLException {
        String sql = "DELETE FROM trip_participants WHERE trip_id = ? AND user_id = ?";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, tripId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void removeActivityParticipant(long activityId, int userId) throws SQLException {
        String sql = "DELETE FROM trip_activity_participants WHERE activity_id = ? AND user_id = ?";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, activityId);
            ps.setInt(2, userId);
            ps.executeUpdate();
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

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (t.getUserId() != null) ps.setLong(1, t.getUserId());
            else ps.setNull(1, Types.INTEGER);
            
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

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Trip mapRow(ResultSet rs) throws SQLException {
        Trip t = new Trip();

        t.setId(rs.getLong("id"));
        t.setUserId(rs.getObject("user_id") != null ? rs.getLong("user_id") : null);
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
        
        try {
            t.setParentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
        } catch (Exception e) {}

        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));

        return t;
    }
}
