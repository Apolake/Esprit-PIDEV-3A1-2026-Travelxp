-- Drop old versions if they exist to ensure clean state
DROP TABLE IF EXISTS trip_activity_participants;
DROP TABLE IF EXISTS trip_participants;

-- Record join between user and template trip
CREATE TABLE trip_participants (
    trip_id BIGINT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (trip_id, user_id),
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Record specific template activities joined by user
CREATE TABLE trip_activity_participants (
    activity_id BIGINT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (activity_id, user_id),
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
