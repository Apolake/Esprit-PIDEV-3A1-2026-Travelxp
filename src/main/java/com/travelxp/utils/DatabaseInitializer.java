package com.travelxp.utils;

import java.sql.*;

/**
 * Initializes and creates all necessary database tables and columns for feedback features.
 * This class runs on application startup to ensure the database schema is up-to-date.
 */
public class DatabaseInitializer {

    public static void initialize() {
        try {
            Connection conn = MyDB.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            // Step 1: Add missing columns to feedback table
            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN title VARCHAR(255)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding title column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN image_url VARCHAR(255)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding image_url column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN likes INT DEFAULT 0");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding likes column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN dislikes INT DEFAULT 0");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding dislikes column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN sentiment VARCHAR(50)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding sentiment column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN status VARCHAR(50) DEFAULT 'NEW'");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding status column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN timezone VARCHAR(50)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding timezone column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN username VARCHAR(100)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding username column: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN favorite_count INT DEFAULT 0");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding favorite_count column: " + e.getMessage());
                }
            }

            // Step 2: Add missing columns to comments table
            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN likes INT DEFAULT 0");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding likes to comments: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN dislikes INT DEFAULT 0");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding dislikes to comments: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN timezone VARCHAR(50)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding timezone to comments: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN username VARCHAR(100)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Error adding username to comments: " + e.getMessage());
                }
            }

            // Step 3: Create favorites table if not exists
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS favorites (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "feedback_id INT NOT NULL," +
                        "user_id INT NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uq_feedback_user (feedback_id, user_id)," +
                        "FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "INDEX idx_user_favorites (user_id)," +
                        "INDEX idx_feedback_favorites (feedback_id)" +
                        ")");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Error creating favorites table: " + e.getMessage());
                }
            }

            // Step 4: Create feedback_reactions table
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS feedback_reactions (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "feedback_id INT NOT NULL," +
                        "user_id INT NOT NULL," +
                        "reaction VARCHAR(20) NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "UNIQUE KEY unique_feedback_user (feedback_id, user_id)" +
                        ")");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Error creating feedback_reactions table: " + e.getMessage());
                }
            }

            // Step 5: Create comment_reactions table
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS comment_reactions (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "comment_id INT NOT NULL," +
                        "user_id INT NOT NULL," +
                        "reaction VARCHAR(20) NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "UNIQUE KEY unique_comment_user (comment_id, user_id)" +
                        ")");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Error creating comment_reactions table: " + e.getMessage());
                }
            }

            // Step 6: Populate username fields if they're null
            try {
                stmt.execute("UPDATE feedback f JOIN users u ON f.user_id = u.id SET f.username = u.username WHERE f.username IS NULL");
                stmt.execute("UPDATE comments c JOIN users u ON c.user_id = u.id SET c.username = u.username WHERE c.username IS NULL");
            } catch (SQLException e) {
                System.err.println("Warning: Could not populate username fields: " + e.getMessage());
            }

            // Step 7: Set default timezone if not set
            try {
                stmt.execute("UPDATE feedback SET timezone = 'UTC' WHERE timezone IS NULL");
                stmt.execute("UPDATE comments SET timezone = 'UTC' WHERE timezone IS NULL");
            } catch (SQLException e) {
                System.err.println("Warning: Could not set default timezone: " + e.getMessage());
            }

            // Step 8: Create conversations table for messaging
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS conversations (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "user1_id INT NOT NULL," +
                        "user2_id INT NOT NULL," +
                        "feedback_id INT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE SET NULL," +
                        "UNIQUE KEY unique_conversation (user1_id, user2_id)," +
                        "INDEX idx_user1 (user1_id)," +
                        "INDEX idx_user2 (user2_id)," +
                        "INDEX idx_updated (updated_at)" +
                        ")");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Error creating conversations table: " + e.getMessage());
                }
            }

            // Step 9: Create messages table for messaging
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "conversation_id INT NOT NULL," +
                        "sender_id INT NOT NULL," +
                        "content TEXT NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "is_read BOOLEAN DEFAULT FALSE," +
                        "FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "INDEX idx_conversation (conversation_id)," +
                        "INDEX idx_created (created_at)" +
                        ")");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Error creating messages table: " + e.getMessage());
                }
            }

            stmt.close();
            System.out.println("✅ Database schema initialization completed successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
