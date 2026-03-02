-- TOTP 2FA Migration
-- Adds TOTP (Time-based One-Time Password) two-factor authentication columns to users table.
-- Run this migration before using the TOTP 2FA feature.

ALTER TABLE users ADD COLUMN totp_enabled TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN totp_secret VARCHAR(64) DEFAULT NULL;
