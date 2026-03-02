package com.travelxp.services;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import com.travelxp.models.User;
import com.travelxp.utils.MyDB;
import com.travelxp.utils.PasswordUtil;

public class UserService {
    private final Connection connection;
    private final GamificationService gamificationService;

    public UserService() {
        this.connection = MyDB.getInstance().getConnection();
        this.gamificationService = new GamificationService();
    }

    public boolean registerUser(User user) throws SQLException {
        // Uniqueness check (email + username)
        if (isEmailOrUsernameTaken(user.getEmail(), user.getUsername())) {
            throw new SQLException("Username or Email already exists.");
        }

        String sql = "INSERT INTO users(username, email, password_hash, birthday, bio, profile_image) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, PasswordUtil.hashPassword(user.getPasswordHash())); // Using field as raw password during reg
            pstmt.setDate(4, Date.valueOf(user.getBirthday()));
            pstmt.setString(5, user.getBio());
            pstmt.setString(6, user.getProfileImage());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        gamificationService.createGamification(user.getId()); // Initialize gamification
                    }
                }
                return true;
            }
            return false;
        }
    }

    private boolean isEmailOrUsernameTaken(String email, String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? OR username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        User user = mapResultSetToUser(rs);
                        gamificationService.addXp(user.getId(), 10); // Login XP
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public boolean updateEmail(int userId, String newEmail) throws SQLException {
        // Uniqueness check for new email
        String checkSql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, newEmail);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Email is already taken.");
                }
            }
        }

        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newEmail);
            pstmt.setInt(2, userId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) gamificationService.addXp(userId, 5); // Email Update XP
            return success;
        }
    }

    public boolean updatePassword(int userId, String currentPassword, String newPassword) throws SQLException {
        User user = getUserById(userId);
        if (user != null && PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) {
            String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, PasswordUtil.hashPassword(newPassword));
                pstmt.setInt(2, userId);
                boolean success = pstmt.executeUpdate() > 0;
                if (success) gamificationService.addXp(userId, 5); // Password Update XP
                return success;
            }
        }
        return false;
    }

    public boolean updateBirthday(int userId, LocalDate birthday) throws SQLException {
        if (birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthday cannot be a future date.");
        }
        String sql = "UPDATE users SET birthday = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(birthday));
            pstmt.setInt(2, userId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) gamificationService.addXp(userId, 5); // Birthday Update XP
            return success;
        }
    }

    public boolean updateBio(int userId, String bio) throws SQLException {
        if (bio != null && bio.length() > 500) {
            throw new IllegalArgumentException("Bio cannot exceed 500 characters.");
        }
        String sql = "UPDATE users SET bio = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bio);
            pstmt.setInt(2, userId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) gamificationService.addXp(userId, 5); // Bio Update XP
            return success;
        }
    }

    public boolean updateProfileImage(int userId, String imagePath) throws SQLException {
        String sql = "UPDATE users SET profile_image = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, imagePath);
            pstmt.setInt(2, userId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) gamificationService.addXp(userId, 5); // Image Update XP
            return success;
        }
    }

    public boolean updateBalance(int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public boolean deleteUser(int userId, String password) throws SQLException {
        User user = getUserById(userId);
        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                return pstmt.executeUpdate() > 0;
            }
        }
        return false;
    }

    // Admin Methods
    public java.util.List<User> getAllUsers() throws SQLException {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public boolean updateUserAsAdmin(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, birthday = ?, bio = ?, profile_image = ?, role = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setDate(3, Date.valueOf(user.getBirthday()));
            pstmt.setString(4, user.getBio());
            pstmt.setString(5, user.getProfileImage());
            pstmt.setString(6, user.getRole());
            pstmt.setInt(7, user.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteUserAsAdmin(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean resetPasswordAsAdmin(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.hashPassword(newPassword));
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setBio(rs.getString("bio"));
        user.setProfileImage(rs.getString("profile_image"));
        user.setRole(rs.getString("role"));
        user.setBalance(rs.getDouble("balance"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        try {
            user.setFaceRegistered(rs.getBoolean("face_registered"));
        } catch (SQLException e) {
            user.setFaceRegistered(false);
        }
        try {
            user.setTotpEnabled(rs.getBoolean("totp_enabled"));
            user.setTotpSecret(rs.getString("totp_secret"));
        } catch (SQLException e) {
            user.setTotpEnabled(false);
            user.setTotpSecret(null);
        }
        return user;
    }

    public boolean setFaceRegistered(int userId, boolean registered) throws SQLException {
        String sql = "UPDATE users SET face_registered = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, registered);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean setTotpSecret(int userId, String secret) throws SQLException {
        String sql = "UPDATE users SET totp_secret = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, secret);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean setTotpEnabled(int userId, boolean enabled) throws SQLException {
        String sql = "UPDATE users SET totp_enabled = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, enabled);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean disableTotp(int userId) throws SQLException {
        String sql = "UPDATE users SET totp_enabled = 0, totp_secret = NULL WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
