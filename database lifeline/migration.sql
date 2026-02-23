CREATE TABLE IF NOT EXISTS gamification (
    user_id INT PRIMARY KEY,
    xp INT DEFAULT 0,
    level INT DEFAULT 1,
    title VARCHAR(50) DEFAULT 'Novice',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
