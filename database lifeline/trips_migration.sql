CREATE TABLE IF NOT EXISTS trips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    trip_name VARCHAR(255) NOT NULL,
    origin VARCHAR(255),
    destination VARCHAR(255),
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'PLANNED',
    budget_amount DOUBLE DEFAULT 0.0,
    currency VARCHAR(10) DEFAULT 'USD',
    total_expenses DOUBLE DEFAULT 0.0,
    total_xp_earned INT DEFAULT 0,
    notes TEXT,
    cover_image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    description TEXT,
    activity_date DATE,
    start_time TIME,
    end_time TIME,
    location_name VARCHAR(255),
    transport_type VARCHAR(100),
    cost_amount DOUBLE DEFAULT 0.0,
    currency VARCHAR(10) DEFAULT 'USD',
    xp_earned INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PLANNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trip_milestones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    milestone_date DATE,
    status VARCHAR(50) DEFAULT 'PLANNED',
    xp_earned INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);
