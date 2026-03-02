package com.travelxp.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.travelxp.models.Service;
import com.travelxp.utils.MyDB;

public class ServiceService {

    private Connection getConnection() {
        return MyDB.getInstance().getConnection();
    }

    // CREATE
    public void addService(Service service) throws SQLException {
        String sql = "INSERT INTO service (provider_name, service_type, price, eco_friendly, xp_reward) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, service.getProviderName());
            ps.setString(2, service.getServiceType());
            ps.setDouble(3, service.getPrice());
            ps.setBoolean(4, service.isEcoFriendly());
            ps.setInt(5, service.getXpReward());
            ps.executeUpdate();
        }
    }

    // READ
    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM service";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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
        return services;
    }

    // UPDATE
    public void updateService(Service service) throws SQLException {
        String sql = "UPDATE service SET provider_name=?, service_type=?, price=?, eco_friendly=?, xp_reward=? WHERE service_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, service.getProviderName());
            ps.setString(2, service.getServiceType());
            ps.setDouble(3, service.getPrice());
            ps.setBoolean(4, service.isEcoFriendly());
            ps.setInt(5, service.getXpReward());
            ps.setInt(6, service.getServiceId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteService(int serviceId) throws SQLException {
        String sql = "DELETE FROM service WHERE service_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.executeUpdate();
        }
    }
}
