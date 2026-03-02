package com.travelxp.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.travelxp.models.Offer;
import com.travelxp.utils.MyDB;

public class OfferService {

    private Connection getConnection() {
        return MyDB.getInstance().getConnection();
    }

	// CREATE
	public void addOffer(Offer offer) throws SQLException {
		String sql = "INSERT INTO offer (property_id, title, description, discount_percentage, start_date, end_date, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, offer.getPropertyId());
            ps.setString(2, offer.getTitle());
            ps.setString(3, offer.getDescription());
            ps.setBigDecimal(4, offer.getDiscountPercentage());
            ps.setDate(5, Date.valueOf(offer.getStartDate()));
            ps.setDate(6, Date.valueOf(offer.getEndDate()));
            ps.setBoolean(7, offer.getIsActive());
            ps.setTimestamp(8, offer.getCreatedAt() != null ? Timestamp.valueOf(offer.getCreatedAt()) : null);
            ps.executeUpdate();
        }
	}

	// READ
	public List<Offer> getAllOffers() throws SQLException {
		List<Offer> offers = new ArrayList<>();
		String sql = "SELECT * FROM offer";
		try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                offers.add(mapResultSetToOffer(rs));
            }
        }
		return offers;
	}

	public Offer getOfferById(Long id) throws SQLException {
		String sql = "SELECT * FROM offer WHERE id=?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToOffer(rs);
            }
        }
		return null;
	}

	public List<Offer> getActiveOffersByPropertyId(Long propertyId) throws SQLException {
		List<Offer> offers = new ArrayList<>();
		String sql = "SELECT * FROM offer WHERE property_id=? AND is_active=true AND start_date <= ? AND end_date >= ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, propertyId);
            Date today = Date.valueOf(LocalDate.now());
            ps.setDate(2, today);
            ps.setDate(3, today);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offers.add(mapResultSetToOffer(rs));
                }
            }
        }
		return offers;
	}

	public List<Offer> getAllActiveOffersForProperty(Long propertyId) throws SQLException {
		List<Offer> offers = new ArrayList<>();
		String sql = "SELECT * FROM offer WHERE property_id=? AND is_active=true";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, propertyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offers.add(mapResultSetToOffer(rs));
                }
            }
        }
		return offers;
	}

	// UPDATE
	public void updateOffer(Offer offer) throws SQLException {
		String sql = "UPDATE offer SET property_id=?, title=?, description=?, discount_percentage=?, start_date=?, end_date=?, is_active=?, created_at=? WHERE id=?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, offer.getPropertyId());
            ps.setString(2, offer.getTitle());
            ps.setString(3, offer.getDescription());
            ps.setBigDecimal(4, offer.getDiscountPercentage());
            ps.setDate(5, Date.valueOf(offer.getStartDate()));
            ps.setDate(6, Date.valueOf(offer.getEndDate()));
            ps.setBoolean(7, offer.getIsActive());
            ps.setTimestamp(8, offer.getCreatedAt() != null ? Timestamp.valueOf(offer.getCreatedAt()) : null);
            ps.setLong(9, offer.getId());
            ps.executeUpdate();
        }
	}

	// DELETE
	public void deleteOffer(Long id) throws SQLException {
		String sql = "DELETE FROM offer WHERE id=?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
	}

	private Offer mapResultSetToOffer(ResultSet rs) throws SQLException {
		Long id = rs.getLong("id");
		Long propertyId = rs.getLong("property_id");
		String title = rs.getString("title");
		String description = rs.getString("description");
		BigDecimal discountPercentage = rs.getBigDecimal("discount_percentage");
		LocalDate startDate = rs.getDate("start_date").toLocalDate();
		LocalDate endDate = rs.getDate("end_date").toLocalDate();
		Boolean isActive = rs.getBoolean("is_active");
		Timestamp createdAtTs = rs.getTimestamp("created_at");
		LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;
		return new Offer(id, propertyId, title, description, discountPercentage, startDate, endDate, isActive, createdAt);
	}
}
