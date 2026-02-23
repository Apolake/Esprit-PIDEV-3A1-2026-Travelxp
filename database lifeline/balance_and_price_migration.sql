USE travelxp;

-- Add balance to users
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS balance DECIMAL(10, 2) DEFAULT 0.00;

-- Add total_price to booking
ALTER TABLE booking 
ADD COLUMN IF NOT EXISTS total_price DECIMAL(10, 2) DEFAULT 0.00;
