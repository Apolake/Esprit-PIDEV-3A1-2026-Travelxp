USE travelxp;

CREATE TABLE IF NOT EXISTS booking_services (
    booking_id INT,
    service_id INT,
    PRIMARY KEY (booking_id, service_id),
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE CASCADE
);
