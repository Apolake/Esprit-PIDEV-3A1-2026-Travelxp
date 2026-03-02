package com.travelxp.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.travelxp.models.Offer;
import com.travelxp.models.Property;
import com.travelxp.utils.MyDB;

/**
 * Advanced Feature #2 – Property Recommendation Engine.
 * 
 * Recommends properties that currently have active offers with a discount
 * percentage of 30 % or more. Properties are ranked by the highest discount
 * available, so the best deals appear first.
 */
public class PropertyRecommendationService {

    private final Connection cnx;
    private final PropertyService propertyService;
    private final OfferService offerService;

    /** Minimum discount percentage to qualify as a "recommended" deal. */
    private static final BigDecimal MIN_DISCOUNT = new BigDecimal("30");

    public PropertyRecommendationService() {
        this.cnx = MyDB.getInstance().getConnection();
        this.propertyService = new PropertyService();
        this.offerService = new OfferService();
    }

    // ── public API ──────────────────────────────────────────

    /**
     * Returns properties with at least one active offer ≥ 30 % discount,
     * ordered by highest discount descending.
     */
    public List<Property> getRecommendedProperties() throws SQLException {
        String sql =
                "SELECT DISTINCT p.*, MAX(o.discount_percentage) AS max_discount " +
                "FROM property p " +
                "JOIN offer o ON o.property_id = p.id " +
                "WHERE o.is_active = true " +
                "  AND o.start_date <= ? " +
                "  AND o.end_date   >= ? " +
                "  AND o.discount_percentage >= ? " +
                "GROUP BY p.id " +
                "ORDER BY max_discount DESC";

        List<Property> recommended = new ArrayList<>();
        PreparedStatement ps = cnx.prepareStatement(sql);
        Date today = Date.valueOf(LocalDate.now());
        ps.setDate(1, today);
        ps.setDate(2, today);
        ps.setBigDecimal(3, MIN_DISCOUNT);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            recommended.add(mapProperty(rs));
        }
        return recommended;
    }

    /**
     * Returns the best active offer (highest discount ≥ 30 %) for a given property,
     * or null if none qualifies.
     */
    public Offer getBestOffer(Long propertyId) throws SQLException {
        List<Offer> activeOffers = offerService.getActiveOffersByPropertyId(propertyId);
        Offer best = null;
        for (Offer o : activeOffers) {
            if (o.getDiscountPercentage().compareTo(MIN_DISCOUNT) >= 0) {
                if (best == null || o.getDiscountPercentage().compareTo(best.getDiscountPercentage()) > 0) {
                    best = o;
                }
            }
        }
        return best;
    }

    /**
     * Returns a map of Property → best Offer for all recommended properties.
     * Useful for displaying deal badges on cards.
     */
    public Map<Property, Offer> getRecommendationsWithOffers() throws SQLException {
        List<Property> recommended = getRecommendedProperties();
        Map<Property, Offer> map = new LinkedHashMap<>();
        for (Property p : recommended) {
            Offer best = getBestOffer(p.getId());
            if (best != null) {
                map.put(p, best);
            }
        }
        return map;
    }

    // ── private helpers ─────────────────────────────────────

    private Property mapProperty(ResultSet rs) throws SQLException {
        Property p = new Property(
            rs.getLong("id"),
            rs.getLong("owner_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("property_type"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("country"),
            rs.getInt("bedrooms"),
            rs.getInt("bathrooms"),
            rs.getInt("max_guests"),
            rs.getBigDecimal("price_per_night"),
            rs.getString("images"),
            rs.getBoolean("is_active")
        );
        double lat = rs.getDouble("latitude");
        if (!rs.wasNull()) p.setLatitude(lat);
        double lng = rs.getDouble("longitude");
        if (!rs.wasNull()) p.setLongitude(lng);
        return p;
    }
}
