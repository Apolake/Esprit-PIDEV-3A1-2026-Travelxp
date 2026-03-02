-- Record join between user and template trip
CREATE TABLE IF NOT EXISTS trip_participants (
    template_id BIGINT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (template_id, user_id),
    FOREIGN KEY (template_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Record specific activities joined by user (links to template activity)
CREATE TABLE IF NOT EXISTS trip_activity_participants (
    template_activity_id BIGINT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (template_activity_id, user_id),
    FOREIGN KEY (template_activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
