package com.travelxp.repositories;

import com.travelxp.models.Activity;
import com.travelxp.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityRepository {

    public List<Activity> findAll() throws SQLException {
        String sql = "SELECT * FROM activities ORDER BY id DESC";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Activity> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        }
    }

    public List<Activity> findByTripId(Long tripId) throws SQLException {
        String sql = "SELECT * FROM activities WHERE trip_id = ? ORDER BY id DESC";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setLong(1, tripId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Activity> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    public Activity findById(Long id) throws SQLException {
        String sql = "SELECT * FROM activities WHERE id=?";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    public void insert(Activity a) throws SQLException {
        String sql = """
            INSERT INTO activities
            (trip_id, title, type, description, activity_date, start_time, end_time,
             location_name, transport_type, cost_amount, currency, xp_earned, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillStatement(ps, a, false);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setId(keys.getLong(1));
            }
        }
    }

    public void update(Activity a) throws SQLException {
        String sql = """
            UPDATE activities SET
              trip_id=?,
              title=?,
              type=?,
              description=?,
              activity_date=?,
              start_time=?,
              end_time=?,
              location_name=?,
              transport_type=?,
              cost_amount=?,
              currency=?,
              xp_earned=?,
              status=?
            WHERE id=?
        """;

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            fillStatement(ps, a, true);
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM activities WHERE id=?";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, Activity a, boolean isUpdate) throws SQLException {
        if (a.getTripId() == null) {
            throw new SQLException("trip_id is required.");
        }
        ps.setLong(1, a.getTripId());
        ps.setString(2, a.getTitle());

        if (a.getType() == null || a.getType().isBlank()) ps.setNull(3, Types.VARCHAR);
        else ps.setString(3, a.getType());

        if (a.getDescription() == null || a.getDescription().isBlank()) ps.setNull(4, Types.LONGVARCHAR);
        else ps.setString(4, a.getDescription());

        if (a.getActivityDate() == null) ps.setNull(5, Types.DATE);
        else ps.setDate(5, Date.valueOf(a.getActivityDate()));

        if (a.getStartTime() == null) ps.setNull(6, Types.TIME);
        else ps.setTime(6, Time.valueOf(a.getStartTime()));

        if (a.getEndTime() == null) ps.setNull(7, Types.TIME);
        else ps.setTime(7, Time.valueOf(a.getEndTime()));

        if (a.getLocationName() == null || a.getLocationName().isBlank()) ps.setNull(8, Types.VARCHAR);
        else ps.setString(8, a.getLocationName());

        if (a.getTransportType() == null || a.getTransportType().isBlank()) ps.setNull(9, Types.VARCHAR);
        else ps.setString(9, a.getTransportType());

        if (a.getCostAmount() == null) ps.setNull(10, Types.DOUBLE);
        else ps.setDouble(10, a.getCostAmount());

        if (a.getCurrency() == null || a.getCurrency().isBlank()) ps.setNull(11, Types.VARCHAR);
        else ps.setString(11, a.getCurrency());

        if (a.getXpEarned() == null) ps.setNull(12, Types.INTEGER);
        else ps.setInt(12, a.getXpEarned());

        String st = a.getStatus();
        if (st == null || st.isBlank()) st = "PLANNED";
        ps.setString(13, st);

        if (isUpdate) {
            ps.setLong(14, a.getId());
        }
    }

    private Activity mapRow(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setId(rs.getLong("id"));
        a.setTripId(rs.getLong("trip_id"));
        a.setTitle(rs.getString("title"));
        a.setType(rs.getString("type"));
        a.setDescription(rs.getString("description"));
        Date d = rs.getDate("activity_date");
        a.setActivityDate(d == null ? null : d.toLocalDate());
        Time st = rs.getTime("start_time");
        a.setStartTime(st == null ? null : st.toLocalTime());
        Time et = rs.getTime("end_time");
        a.setEndTime(et == null ? null : et.toLocalTime());
        a.setLocationName(rs.getString("location_name"));
        a.setTransportType(rs.getString("transport_type"));
        double cost = rs.getDouble("cost_amount");
        a.setCostAmount(rs.wasNull() ? null : cost);
        a.setCurrency(rs.getString("currency"));
        int xp = rs.getInt("xp_earned");
        a.setXpEarned(rs.wasNull() ? null : xp);
        a.setStatus(rs.getString("status"));
        Timestamp c = rs.getTimestamp("created_at");
        a.setCreatedAt(c == null ? null : c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at");
        a.setUpdatedAt(u == null ? null : u.toLocalDateTime());
        return a;
    }
}
