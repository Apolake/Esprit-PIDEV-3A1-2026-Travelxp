package com.travelxp.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.travelxp.models.PlaceDTO;

/**
 * Service responsible for fetching nearby places from a third-party API.
 *
 * Supports Google Places (default) and Foursquare (when provider="FOURSQUARE").
 * The service reads API keys from environment variables:
 *  - GOOGLE_PLACES_API_KEY for Google
 *  - FOURSQUARE_API_KEY for Foursquare
 */
public class PlacesService {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final String provider; // "GOOGLE" or "FOURSQUARE"
    private final String apiKey;

    public PlacesService() {
    // simple in-memory cache keyed by request parameters
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private volatile long cacheTtlMillis = 10 * 60 * 1000; // 10 minutes by default
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>(); // simple in-memory cache keyed by request parameters
    private volatile long cacheTtlMillis = 10 * 60 * 1000; // 10 minutes by default
        } else {
            this.apiKey = System.getenv("GOOGLE_PLACES_API_KEY");
        }
        this.client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    }

    /**
     * Fetch nearby places for the supplied categories.
     *
     * @param lat property latitude
     * @param lon property longitude
    /**
     * Backwards-compatible fetch method (returns full list).
     */
    public List<PlaceDTO> fetchNearbyPlaces(double lat, double lon, List<String> categories, int radiusMeters) throws IOException, InterruptedException {
        return fetchNearbyPlaces(lat, lon, categories, radiusMeters, 0, 0);
    }

     * @param categories list of category identifiers (e.g., restaurant, hospital, tourist_attraction, airport)
     * @param radiusMeters search radius in meters
     */
    public List<PlaceDTO> fetchNearbyPlaces(double lat, double lon, List<String> categories, int radiusMeters) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
    /**
     * Fetch nearby places and return a paginated slice.
     * Page index is zero-based. If pageSize <= 0, returns full list.
     */
    public List<PlaceDTO> fetchNearbyPlaces(double lat, double lon, List<String> categories, int radiusMeters, int page, int pageSize) throws IOException, InterruptedException {
        String key = buildCacheKey(lat, lon, categories, radiusMeters);
        List<PlaceDTO> all;
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired(cacheTtlMillis)) {
            all = entry.places;
        } else {
            all = fetchAllNearbyPlaces(lat, lon, categories, radiusMeters);
            cache.put(key, new CacheEntry(all, System.currentTimeMillis()));
        }
        if (pageSize <= 0) return all;
        int from = page * pageSize;
        if (from >= all.size()) return Collections.emptyList();
        int to = Math.min(from + pageSize, all.size());
        return new ArrayList<>(all.subList(from, to));
    }
        }
    private List<PlaceDTO> fetchAllNearbyPlaces(double lat, double lon, List<String> categories, int radiusMeters) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Places API key is not set. Set GOOGLE_PLACES_API_KEY or FOURSQUARE_API_KEY environment variable.");
        }
        List<PlaceDTO> places = new ArrayList<>();

        if ("OSM".equals(provider)) {
            // build Overpass QL query combining all categories
            StringBuilder ov = new StringBuilder("[out:json];(");
            for (String cat : categories) {
                switch (cat.toLowerCase()) {
                    case "restaurant":
                        ov.append(String.format("node(around:%d,%.6f,%.6f)[amenity=restaurant];", radiusMeters, lat, lon));
                        break;
                    case "hospital":
                        ov.append(String.format("node(around:%d,%.6f,%.6f)[amenity=hospital];", radiusMeters, lat, lon));
                        break;
                    case "tourist_attraction":
                        ov.append(String.format("node(around:%d,%.6f,%.6f)[tourism=attraction];", radiusMeters, lat, lon));
                        break;
                    case "airport":
                        ov.append(String.format("node(around:%d,%.6f,%.6f)[aeroway=aerodrome];", radiusMeters, lat, lon));
                        ov.append(String.format("node(around:%d,%.6f,%.6f)[aeroway=airport];", radiusMeters, lat, lon));
                        break;
                    default:
                        // generic amenity match
                        ov.append(String.format("node(around:%d,%.6f,%.6f)[amenity=%s];", radiusMeters, lat, lon, cat));
                }
            }
            ov.append(")\nout center;" );
            String query = ov.toString();
            String url = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(TIMEOUT).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonObject root = gson.fromJson(resp.body(), JsonObject.class);
                JsonArray elems = root.has("elements") ? root.getAsJsonArray("elements") : new JsonArray();
                for (int i = 0; i < elems.size(); i++) {
                    JsonObject el = elems.get(i).getAsJsonObject();
                    String id = el.has("id") ? el.get("id").getAsString() : null;
                    Double plat = el.has("lat") ? el.get("lat").getAsDouble() : (el.has("center") ? el.getAsJsonObject("center").get("lat").getAsDouble() : null);
                    Double plon = el.has("lon") ? el.get("lon").getAsDouble() : (el.has("center") ? el.getAsJsonObject("center").get("lon").getAsDouble() : null);
                    JsonObject tags = el.has("tags") ? el.getAsJsonObject("tags") : new JsonObject();
                    String name = tags.has("name") ? tags.get("name").getAsString() : null;
                    String categoryName = tags.entrySet().stream().map(e -> e.getKey()+"="+e.getValue().getAsString()).collect(Collectors.joining(","));
                    String address = null;
                    if (tags.has("addr:full")) address = tags.get("addr:full").getAsString();
                    Integer distance = null;
                    if (plat != null && plon != null) {
                        distance = (int) Math.round(computeDistanceKm(lat, lon, plat, plon) * 1000.0);
                    }
                    PlaceDTO p = new PlaceDTO(id, name, categoryName, address, plat, plon, distance);
                    places.add(p);
                }
            }
        } else {
            // GOOGLE Places Nearby Search - call once per type/category
            for (String type : categories) {
                String url = String.format(
                        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",
                        lat, lon, radiusMeters, URLEncoder.encode(type, StandardCharsets.UTF_8), apiKey);
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(TIMEOUT).GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200) continue;
                JsonObject root = gson.fromJson(resp.body(), JsonObject.class);
                JsonArray results = root.has("results") ? root.getAsJsonArray("results") : new JsonArray();
                for (int i = 0; i < results.size(); i++) {
                    JsonObject r = results.get(i).getAsJsonObject();
                    String id = r.has("place_id") ? r.get("place_id").getAsString() : null;
                    String name = r.has("name") ? r.get("name").getAsString() : null;
                    String vicinity = r.has("vicinity") ? r.get("vicinity").getAsString() : null;
                    Double plat = null, plon = null;
                    if (r.has("geometry") && r.getAsJsonObject("geometry").has("location")) {
                        JsonObject loc = r.getAsJsonObject("geometry").getAsJsonObject("location");
                        if (loc.has("lat")) plat = loc.get("lat").getAsDouble();
                        if (loc.has("lng")) plon = loc.get("lng").getAsDouble();
                    }
                    Integer distance = null;
                    if (plat != null && plon != null) {
                        distance = (int) Math.round(computeDistanceKm(lat, lon, plat, plon) * 1000.0);
                    }
                    PlaceDTO p = new PlaceDTO(id, name, type, vicinity, plat, plon, distance);
                    places.add(p);
                }
            }
        }

        return places;
    }

    private String buildCacheKey(double lat, double lon, List<String> categories, int radiusMeters) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%.6f,%.6f|%d|", lat, lon, radiusMeters));
        if (categories != null) {
            for (String c : categories) {
                sb.append(c).append(',');
            }
        }
        return sb.toString();
    }

    public void clearCache() { cache.clear(); }

    public void setCacheTtlMillis(long ttlMillis) { this.cacheTtlMillis = ttlMillis; }

    private static class CacheEntry {
        final List<PlaceDTO> places;
        final long fetchedAt;
        CacheEntry(List<PlaceDTO> places, long fetchedAt) { this.places = places == null ? Collections.emptyList() : new ArrayList<>(places); this.fetchedAt = fetchedAt; }
        boolean isExpired(long ttl) { return System.currentTimeMillis() - fetchedAt > ttl; }
    }
     * Weights: restaurants (0.5), attractions (0.3), airport distance (0.2).
     * This produces a score in [0,1].
     */
    public double computeAttractivenessScore(double propLat, double propLon, List<PlaceDTO> places) {
        int restaurants = 0;
        int attractions = 0;
        double nearestAirportKm = Double.POSITIVE_INFINITY;

        for (PlaceDTO p : places) {
            String c = p.getCategory() == null ? "" : p.getCategory().toLowerCase();
            String n = p.getName() == null ? "" : p.getName().toLowerCase();
            if (c.contains("restaurant") || n.contains("restaurant") || c.contains("food") ) restaurants++;
            if (c.contains("tourist") || c.contains("attraction") || n.contains("museum") || n.contains("park")) attractions++;
            if (c.contains("airport") || n.contains("airport")) {
                if (p.getLatitude() != null && p.getLongitude() != null) {
                    double d = computeDistanceKm(propLat, propLon, p.getLatitude(), p.getLongitude());
                    nearestAirportKm = Math.min(nearestAirportKm, d);
                }
            }
        }

        double rScore = Math.min(1.0, restaurants / 10.0); // saturate at 10 restaurants
        double aScore = Math.min(1.0, attractions / 5.0);   // saturate at 5 attractions
        double airportScore = 0.0;
        if (Double.isFinite(nearestAirportKm)) {
            // closer airport -> higher score; 0 km -> 1.0, 100 km -> 0.0
            airportScore = 1.0 - Math.min(1.0, nearestAirportKm / 100.0);
        }

        double score = 0.5 * rScore + 0.3 * aScore + 0.2 * airportScore;
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double computeDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
