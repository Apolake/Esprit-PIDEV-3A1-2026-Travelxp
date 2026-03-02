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
        String sql = "INSERT INTO property (owner_id, title, description, property_type, address, city, country, bedrooms, bathrooms, max_guests, price_per_night, images, is_active, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        if (property.getLatitude() != null) ps.setDouble(14, property.getLatitude()); else ps.setNull(14, java.sql.Types.DOUBLE);
        if (property.getLongitude() != null) ps.setDouble(15, property.getLongitude()); else ps.setNull(15, java.sql.Types.DOUBLE);
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
        String sql = "UPDATE property SET owner_id=?, title=?, description=?, property_type=?, address=?, city=?, country=?, bedrooms=?, bathrooms=?, max_guests=?, price_per_night=?, images=?, is_active=?, latitude=?, longitude=? WHERE id=?";
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
        if (property.getLatitude() != null) ps.setDouble(14, property.getLatitude()); else ps.setNull(14, java.sql.Types.DOUBLE);
        if (property.getLongitude() != null) ps.setDouble(15, property.getLongitude()); else ps.setNull(15, java.sql.Types.DOUBLE);
        ps.setLong(16, property.getId());
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
        Property p = new Property(id, ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, pricePerNight, images, isActive);
        double lat = rs.getDouble("latitude");
        if (!rs.wasNull()) p.setLatitude(lat);
        double lng = rs.getDouble("longitude");
        if (!rs.wasNull()) p.setLongitude(lng);
        return p;
    }

    /**
     * Advanced search: search by name + filter by city/country + sort by price.
     * All rolled into one method.
     *
     * @param nameQuery  search term for title (empty/null = no filter)
     * @param city       filter by city (empty/null = no filter)
     * @param country    filter by country (empty/null = no filter)
     * @param sortByPrice "ASC", "DESC", or null for no sorting
     * @return filtered & sorted list of properties
     */
    public List<Property> searchProperties(String nameQuery, String city, String country, String sortByPrice) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM property WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (nameQuery != null && !nameQuery.isBlank()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + nameQuery.trim() + "%");
        }
        if (city != null && !city.isBlank()) {
            sql.append(" AND city LIKE ?");
            params.add("%" + city.trim() + "%");
        }
        if (country != null && !country.isBlank()) {
            sql.append(" AND country LIKE ?");
            params.add("%" + country.trim() + "%");
        }
        if ("ASC".equalsIgnoreCase(sortByPrice)) {
            sql.append(" ORDER BY price_per_night ASC");
        } else if ("DESC".equalsIgnoreCase(sortByPrice)) {
            sql.append(" ORDER BY price_per_night DESC");
        }

        List<Property> results = new ArrayList<>();
        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            results.add(mapResultSetToProperty(rs));
        }
        return results;
    }

    /**
     * Get all distinct city values for filter dropdowns.
     */
    public List<String> getAllCities() throws SQLException {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM property WHERE city IS NOT NULL AND city != '' ORDER BY city";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) cities.add(rs.getString("city"));
        return cities;
    }

    /**
     * Get all distinct country values for filter dropdowns.
     */
    public List<String> getAllCountries() throws SQLException {
        List<String> countries = new ArrayList<>();
        String sql = "SELECT DISTINCT country FROM property WHERE country IS NOT NULL AND country != '' ORDER BY country";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) countries.add(rs.getString("country"));
        return countries;
    }
}
