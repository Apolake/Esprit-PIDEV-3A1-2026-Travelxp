ALTER TABLE trips ADD COLUMN parent_id BIGINT NULL;
ALTER TABLE trips ADD CONSTRAINT fk_parent_trip FOREIGN KEY (parent_id) REFERENCES trips(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS trip_participants (
    trip_id BIGINT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (trip_id, user_id),
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
