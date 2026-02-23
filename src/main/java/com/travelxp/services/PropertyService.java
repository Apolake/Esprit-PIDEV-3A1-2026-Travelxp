package com.travelxp.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.travelxp.models.Property;
import com.travelxp.utils.MyDB;

public class PropertyService {

    private Connection cnx;

    public PropertyService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // CREATE
    public void addProperty(Property property) throws SQLException {
        String sql = "INSERT INTO property (owner_id, title, description, property_type, address, city, country, bedrooms, bathrooms, max_guests, price_per_night, images, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, property.getOwnerId());
        ps.setString(2, property.getTitle());
        ps.setString(3, property.getDescription());
        ps.setString(4, property.getPropertyType());
        ps.setString(5, property.getAddress());
        ps.setString(6, property.getCity());
        ps.setString(7, property.getCountry());
        ps.setInt(8, property.getBedrooms());
        ps.setInt(9, property.getBathrooms());
        ps.setInt(10, property.getMaxGuests());
        ps.setBigDecimal(11, property.getPricePerNight());
        ps.setString(12, property.getImages());
        ps.setBoolean(13, property.getIsActive());
        ps.executeUpdate();
    }

    // READ
    public List<Property> getAllProperties() throws SQLException {
        List<Property> properties = new ArrayList<>();
        String sql = "SELECT * FROM property";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Property p = mapResultSetToProperty(rs);
            properties.add(p);
        }
        return properties;
    }

    public Property getPropertyById(Long id) throws SQLException {
        String sql = "SELECT * FROM property WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        Property p = null;
        if (rs.next()) {
            p = mapResultSetToProperty(rs);
        }
        return p;
    }

    // UPDATE
    public void updateProperty(Property property) throws SQLException {
        String sql = "UPDATE property SET owner_id=?, title=?, description=?, property_type=?, address=?, city=?, country=?, bedrooms=?, bathrooms=?, max_guests=?, price_per_night=?, images=?, is_active=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, property.getOwnerId());
        ps.setString(2, property.getTitle());
        ps.setString(3, property.getDescription());
        ps.setString(4, property.getPropertyType());
        ps.setString(5, property.getAddress());
        ps.setString(6, property.getCity());
        ps.setString(7, property.getCountry());
        ps.setInt(8, property.getBedrooms());
        ps.setInt(9, property.getBathrooms());
        ps.setInt(10, property.getMaxGuests());
        ps.setBigDecimal(11, property.getPricePerNight());
        ps.setString(12, property.getImages());
        ps.setBoolean(13, property.getIsActive());
        ps.setLong(14, property.getId());
        ps.executeUpdate();
    }

    // DELETE
    public void deleteProperty(Long id) throws SQLException {
        String sql = "DELETE FROM property WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    private Property mapResultSetToProperty(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long ownerId = rs.getLong("owner_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String propertyType = rs.getString("property_type");
        String address = rs.getString("address");
        String city = rs.getString("city");
        String country = rs.getString("country");
        Integer bedrooms = rs.getInt("bedrooms");
        Integer bathrooms = rs.getInt("bathrooms");
        Integer maxGuests = rs.getInt("max_guests");
        BigDecimal pricePerNight = rs.getBigDecimal("price_per_night");
        String images = rs.getString("images");
        Boolean isActive = rs.getBoolean("is_active");
        return new Property(id, ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, pricePerNight, images, isActive);
    }
}
