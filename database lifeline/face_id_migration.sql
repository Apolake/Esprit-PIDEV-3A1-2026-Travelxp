-- Face ID Migration
-- Adds face_registered column to users table to track which users have enrolled their face for Face ID login.
-- Run this migration before using the Face ID feature.

ALTER TABLE users ADD COLUMN face_registered TINYINT(1) NOT NULL DEFAULT 0;
