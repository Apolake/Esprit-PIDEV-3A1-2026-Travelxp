package com.travelxp.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * API #1 – OSRM (Open Source Routing Machine) Distance &amp; Routing Service.
 *
 * Calculates driving distance, estimated travel time, and step-by-step route
 * directions between two points using the free public OSRM demo server at
 * https://router.project-osrm.org.
 *
 * No API key required.
 */
public class OSRMRoutingService {

    private static final String BASE_URL = "https://router.project-osrm.org";

    // ── Route calculation ───────────────────────────────────

    /**
     * Calculate the driving route between two coordinate pairs.
     *
     * @param fromLat origin latitude
     * @param fromLng origin longitude
     * @param toLat   destination latitude
     * @param toLng   destination longitude
     * @return a RouteResult with distance, duration and turn-by-turn steps,
     *         or null if routing failed
     */
    public RouteResult getRoute(double fromLat, double fromLng, double toLat, double toLng) {
        try {
            // OSRM expects coordinates as lng,lat
            String urlStr = BASE_URL + "/route/v1/driving/"
                    + fromLng + "," + fromLat + ";"
                    + toLng + "," + toLat
                    + "?overview=full&steps=true&geometries=geojson";

            String json = httpGet(urlStr);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (!"Ok".equals(root.get("code").getAsString())) {
                System.err.println("OSRM error: " + root.get("code").getAsString());
                return null;
            }

            JsonObject route = root.getAsJsonArray("routes").get(0).getAsJsonObject();
            double distanceMeters = route.get("distance").getAsDouble();
            double durationSeconds = route.get("duration").getAsDouble();

            // Extract turn-by-turn instructions from the first leg
            List<RouteStep> steps = new ArrayList<>();
            JsonArray legs = route.getAsJsonArray("legs");
            if (legs.size() > 0) {
                JsonArray stepsArr = legs.get(0).getAsJsonObject().getAsJsonArray("steps");
                for (int i = 0; i < stepsArr.size(); i++) {
                    JsonObject step = stepsArr.get(i).getAsJsonObject();
                    String instruction = "";
                    if (step.has("maneuver")) {
                        JsonObject maneuver = step.getAsJsonObject("maneuver");
                        String type = maneuver.has("type") ? maneuver.get("type").getAsString() : "";
                        String modifier = maneuver.has("modifier") ? maneuver.get("modifier").getAsString() : "";
                        instruction = type + (modifier.isEmpty() ? "" : " " + modifier);
                    }
                    String name = step.has("name") ? step.get("name").getAsString() : "";
                    double stepDist = step.get("distance").getAsDouble();
                    double stepDur = step.get("duration").getAsDouble();
                    steps.add(new RouteStep(instruction, name, stepDist, stepDur));
                }
            }

            // Extract route geometry (list of [lng, lat] coordinates)
            List<double[]> geometry = new ArrayList<>();
            if (route.has("geometry")) {
                JsonObject geo = route.getAsJsonObject("geometry");
                JsonArray coords = geo.getAsJsonArray("coordinates");
                for (int i = 0; i < coords.size(); i++) {
                    JsonArray coord = coords.get(i).getAsJsonArray();
                    geometry.add(new double[]{ coord.get(1).getAsDouble(), coord.get(0).getAsDouble() });
                }
            }

            return new RouteResult(distanceMeters, durationSeconds, steps, geometry);
        } catch (Exception e) {
            System.err.println("OSRM routing error: " + e.getMessage());
            return null;
        }
    }

    // ── Convenience methods ─────────────────────────────────

    /**
     * Returns a human-readable summary, e.g. "12.5 km – ~18 min driving".
     */
    public String getRouteSummary(double fromLat, double fromLng, double toLat, double toLng) {
        RouteResult r = getRoute(fromLat, fromLng, toLat, toLng);
        if (r == null) return "Route unavailable";
        return r.getFormattedDistance() + " – ~" + r.getFormattedDuration() + " driving";
    }

    // ── HTTP helper ─────────────────────────────────────────

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "TravelXP/1.0 (JavaFX Desktop App)");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("OSRM HTTP " + code);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    // ── DTOs ────────────────────────────────────────────────

    public static class RouteResult {
        private final double distanceMeters;
        private final double durationSeconds;
        private final List<RouteStep> steps;
        private final List<double[]> geometry;

        public RouteResult(double distanceMeters, double durationSeconds, List<RouteStep> steps, List<double[]> geometry) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.steps = steps;
            this.geometry = geometry;
        }

        public double getDistanceMeters()  { return distanceMeters; }
        public double getDurationSeconds() { return durationSeconds; }
        public List<RouteStep> getSteps()  { return steps; }
        public List<double[]> getGeometry() { return geometry; }

        /** e.g. "12.5 km" or "850 m" */
        public String getFormattedDistance() {
            if (distanceMeters >= 1000) {
                return String.format("%.1f km", distanceMeters / 1000);
            }
            return String.format("%.0f m", distanceMeters);
        }

        /** e.g. "1 h 23 min" or "18 min" */
        public String getFormattedDuration() {
            int totalMin = (int) Math.ceil(durationSeconds / 60.0);
            if (totalMin >= 60) {
                return (totalMin / 60) + " h " + (totalMin % 60) + " min";
            }
            return totalMin + " min";
        }

        @Override
        public String toString() {
            return getFormattedDistance() + " – " + getFormattedDuration();
        }
    }

    public static class RouteStep {
        private final String instruction;
        private final String roadName;
        private final double distanceMeters;
        private final double durationSeconds;

        public RouteStep(String instruction, String roadName, double distanceMeters, double durationSeconds) {
            this.instruction = instruction;
            this.roadName = roadName;
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
        }

        public String getInstruction()     { return instruction; }
        public String getRoadName()         { return roadName; }
        public double getDistanceMeters()   { return distanceMeters; }
        public double getDurationSeconds()  { return durationSeconds; }

        @Override
        public String toString() {
            String dist = distanceMeters >= 1000
                    ? String.format("%.1f km", distanceMeters / 1000)
                    : String.format("%.0f m", distanceMeters);
            return instruction + " on " + roadName + " (" + dist + ")";
        }
    }
}
