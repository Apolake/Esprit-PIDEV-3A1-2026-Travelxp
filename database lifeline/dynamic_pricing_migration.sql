-- Migration: Add num_guests column to booking table for dynamic pricing
-- Run this against your travelxp database

ALTER TABLE `booking` ADD COLUMN `num_guests` INT NOT NULL DEFAULT 1 AFTER `total_price`;
