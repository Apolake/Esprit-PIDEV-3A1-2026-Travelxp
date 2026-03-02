-- Migration: Add latitude and longitude columns to property table
-- These are used by the Nominatim Geocoding and OSRM Routing APIs

ALTER TABLE property ADD COLUMN latitude DOUBLE DEFAULT NULL;
ALTER TABLE property ADD COLUMN longitude DOUBLE DEFAULT NULL;
