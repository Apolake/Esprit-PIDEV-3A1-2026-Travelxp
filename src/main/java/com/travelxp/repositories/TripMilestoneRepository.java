package com.travelxp.repositories;

import com.travelxp.models.TripMilestone;
import com.travelxp.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripMilestoneRepository {

    public List<TripMilestone> findAll() throws SQLException {
        String sql = "SELECT * FROM trip_milestones ORDER BY id DESC";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<TripMilestone> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        }
    }

    public List<TripMilestone> findByTripId(Long tripId) throws SQLException {
        String sql = "SELECT * FROM trip_milestones WHERE trip_id = ? ORDER BY milestone_date ASC";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TripMilestone> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    public void insert(TripMilestone m) throws SQLException {
        String sql = """
            INSERT INTO trip_milestones (trip_id, title, description, milestone_date, status, xp_earned)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillStatement(ps, m, false);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getLong(1));
            }
        }
    }

    public void update(TripMilestone m) throws SQLException {
        String sql = """
            UPDATE trip_milestones SET
              trip_id=?,
              title=?,
              description=?,
              milestone_date=?,
              status=?,
              xp_earned=?
            WHERE id=?
        """;

        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            fillStatement(ps, m, true);
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM trip_milestones WHERE id=?";
        Connection cnx = MyDB.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, TripMilestone m, boolean isUpdate) throws SQLException {
        ps.setLong(1, m.getTripId());
        ps.setString(2, m.getTitle());

        if (m.getDescription() == null) ps.setNull(3, Types.VARCHAR);
        else ps.setString(3, m.getDescription());

        if (m.getMilestoneDate() == null) ps.setNull(4, Types.DATE);
        else ps.setDate(4, Date.valueOf(m.getMilestoneDate()));

        if (m.getStatus() == null) ps.setNull(5, Types.VARCHAR);
        else ps.setString(5, m.getStatus());

        if (m.getXpEarned() == null) ps.setNull(6, Types.INTEGER);
        else ps.setInt(6, m.getXpEarned());

        if (isUpdate) {
            ps.setLong(7, m.getId());
        }
    }

    private TripMilestone mapRow(ResultSet rs) throws SQLException {
        TripMilestone m = new TripMilestone();
        m.setId(rs.getLong("id"));
        m.setTripId(rs.getLong("trip_id"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));

        Date d = rs.getDate("milestone_date");
        m.setMilestoneDate(d == null ? null : d.toLocalDate());

        m.setStatus(rs.getString("status"));

        int xp = rs.getInt("xp_earned");
        m.setXpEarned(rs.wasNull() ? null : xp);

        Timestamp c = null;
        Timestamp u = null;
        try { c = rs.getTimestamp("created_at"); } catch (Exception ignored) {}
        try { u = rs.getTimestamp("updated_at"); } catch (Exception ignored) {}

        if (c != null) m.setCreatedAt(c.toLocalDateTime());
        if (u != null) m.setUpdatedAt(u.toLocalDateTime());

        return m;
    }
}
