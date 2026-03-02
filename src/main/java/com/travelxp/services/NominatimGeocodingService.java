package com.travelxp.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * API #2 – OpenStreetMap Nominatim Geocoding Service.
 *
 * Converts addresses / place names to latitude/longitude coordinates (forward
 * geocoding) and coordinates back to addresses (reverse geocoding).
 *
 * Uses the free Nominatim API at https://nominatim.openstreetmap.org.
 * Usage policy: max 1 request per second, custom User-Agent header.
 */
public class NominatimGeocodingService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "TravelXP/1.0 (JavaFX Desktop App)";

    // ── Forward Geocoding ───────────────────────────────────

    /**
     * Geocode an address string to coordinates.
     *
     * @param address free-form address string, e.g. "Tunis, Tunisia"
     * @return a GeocodingResult with lat/lng, or null if nothing found
     */
    public GeocodingResult geocode(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String urlStr = BASE_URL + "/search?format=json&limit=1&q=" + encoded;
            String json = httpGet(urlStr);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            if (arr.isEmpty()) return null;

            JsonObject obj = arr.get(0).getAsJsonObject();
            double lat = obj.get("lat").getAsDouble();
            double lon = obj.get("lon").getAsDouble();
            String displayName = obj.has("display_name") ? obj.get("display_name").getAsString() : address;
            return new GeocodingResult(lat, lon, displayName);
        } catch (Exception e) {
            System.err.println("Nominatim geocoding error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Search for multiple results (e.g. for autocomplete).
     */
    public List<GeocodingResult> search(String query, int limit) {
        List<GeocodingResult> results = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlStr = BASE_URL + "/search?format=json&limit=" + Math.min(limit, 10) + "&q=" + encoded;
            String json = httpGet(urlStr);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();
                double lat = obj.get("lat").getAsDouble();
                double lon = obj.get("lon").getAsDouble();
                String displayName = obj.has("display_name") ? obj.get("display_name").getAsString() : "";
                results.add(new GeocodingResult(lat, lon, displayName));
            }
        } catch (Exception e) {
            System.err.println("Nominatim search error: " + e.getMessage());
        }
        return results;
    }

    // ── Reverse Geocoding ───────────────────────────────────

    /**
     * Reverse-geocode coordinates to a human-readable address.
     *
     * @param lat latitude
     * @param lon longitude
     * @return display address, or null on failure
     */
    public String reverseGeocode(double lat, double lon) {
        try {
            String urlStr = BASE_URL + "/reverse?format=json&lat=" + lat + "&lon=" + lon;
            String json = httpGet(urlStr);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.has("display_name") ? obj.get("display_name").getAsString() : null;
        } catch (Exception e) {
            System.err.println("Nominatim reverse geocode error: " + e.getMessage());
            return null;
        }
    }

    // ── HTTP helper ─────────────────────────────────────────

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Nominatim HTTP " + code);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    // ── Result DTO ──────────────────────────────────────────

    public static class GeocodingResult {
        private final double latitude;
        private final double longitude;
        private final String displayName;

        public GeocodingResult(double latitude, double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }

        public double getLatitude()  { return latitude; }
        public double getLongitude() { return longitude; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return displayName + " [" + latitude + ", " + longitude + "]";
        }
    }
}
