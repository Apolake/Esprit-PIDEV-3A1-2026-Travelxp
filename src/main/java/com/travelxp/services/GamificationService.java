package com.travelxp.services;

import com.travelxp.models.Gamification;
import com.travelxp.utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GamificationService {
    private final Connection connection;

    public GamificationService() {
        this.connection = MyDB.getInstance().getConnection();
    }

    public void createGamification(int userId) throws SQLException {
        String sql = "INSERT INTO gamification (user_id, xp, level, title) VALUES (?, 0, 1, 'Novice')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public void addXp(int userId, int points) throws SQLException {
        Gamification gamification = getGamificationByUserId(userId);
        if (gamification == null) return;

        int newXp = gamification.getXp() + points;
        int newLevel = calculateLevel(newXp);
        String newTitle = getTitle(newLevel);

        String sql = "UPDATE gamification SET xp = ?, level = ?, title = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newXp);
            pstmt.setInt(2, newLevel);
            pstmt.setString(3, newTitle);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
        }
    }

    public Gamification getGamificationByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM gamification WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Gamification(
                            rs.getInt("user_id"),
                            rs.getInt("xp"),
                            rs.getInt("level"),
                            rs.getString("title")
                    );
                }
            }
        }
        return null;
    }

    public void updateGamification(int userId, int xp, int level) throws SQLException {
        String newTitle = getTitle(level);
        String sql = "UPDATE gamification SET xp = ?, level = ?, title = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, xp);
            pstmt.setInt(2, level);
            pstmt.setString(3, newTitle);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
        }
    }

    public int calculateLevel(int xp) {
        if (xp < 50) return 1;
        if (xp < 120) return 2;
        if (xp < 200) return 3;
        if (xp < 300) return 4;
        return 5 + (xp - 300) / 100;
    }

    public String getTitle(int level) {
        if (level <= 2) return "Novice";
        if (level <= 4) return "Explorer";
        if (level <= 6) return "Traveler";
        if (level <= 8) return "Globetrotter";
        if (level <= 10) return "Adventurer";
        if (level <= 12) return "Voyager";
        if (level <= 14) return "Wanderer";
        if (level <= 16) return "Nomad";
        if (level <= 18) return "Pioneer";
        if (level <= 20) return "Trailblazer";
        if (level <= 22) return "Pathfinder";
        if (level <= 24) return "Seeker";
        if (level <= 26) return "Globetrotter";
        if (level <= 28) return "Explorer";
        if (level <= 30) return "Great Adventurer";
        return "Beyond Limits";
    }

    public int getXpForNextLevel(int currentLevel) {
        if (currentLevel == 1) return 50;
        if (currentLevel == 2) return 120;
        if (currentLevel == 3) return 200;
        if (currentLevel == 4) return 300;
        return 300 + (currentLevel - 4) * 250; 
    }
}
