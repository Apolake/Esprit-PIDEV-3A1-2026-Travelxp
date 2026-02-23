USE travelxp;

CREATE TABLE IF NOT EXISTS service (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    provider_name VARCHAR(255) NOT NULL,
    service_type VARCHAR(100),
    price DOUBLE,
    eco_friendly BOOLEAN,
    xp_reward INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
