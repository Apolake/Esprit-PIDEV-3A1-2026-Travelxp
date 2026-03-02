package com.travelxp.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherInfo getCurrentWeatherByCity(String city) throws IOException, InterruptedException {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Destination city is empty.");
        }

        // 1) Geocoding: city -> lat/lon
        GeoPoint point = geocodeCity(city);

        // 2) Weather: lat/lon -> current weather
        return fetchCurrentWeather(point.latitude, point.longitude, point.nameResolved);
    }

    private GeoPoint geocodeCity(String city) throws IOException, InterruptedException {
        String q = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);

        // Open-Meteo Geocoding API:
        // https://geocoding-api.open-meteo.com/v1/search?name=Berlin&count=1&language=en&format=json
        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + q
                + "&count=1"
                + "&language=en"
                + "&format=json";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("Geocoding failed. HTTP " + res.statusCode());
        }

        JsonNode root = mapper.readTree(res.body());
        JsonNode results = root.get("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw new IOException("No matching location found for: " + city);
        }

        JsonNode first = results.get(0);
        double lat = first.get("latitude").asDouble();
        double lon = first.get("longitude").asDouble();

        String name = first.hasNonNull("name") ? first.get("name").asText() : city;
        String country = first.hasNonNull("country") ? first.get("country").asText() : "";
        String display = country.isBlank() ? name : (name + ", " + country);

        return new GeoPoint(lat, lon, display);
    }

    private WeatherInfo fetchCurrentWeather(double lat, double lon, String displayName)
            throws IOException, InterruptedException {

        // Open-Meteo Forecast API endpoint /v1/forecast with "current" variables
        // Example:
        // https://api.open-meteo.com/v1/forecast?latitude=...&longitude=...&current=temperature_2m,weather_code,wind_speed_10m&timezone=auto
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,weather_code,wind_speed_10m"
                + "&timezone=auto";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("Weather request failed. HTTP " + res.statusCode());
        }

        JsonNode root = mapper.readTree(res.body());
        JsonNode current = root.get("current");
        if (current == null) {
            throw new IOException("Weather response has no 'current' field.");
        }

        double temp = current.hasNonNull("temperature_2m") ? current.get("temperature_2m").asDouble() : Double.NaN;
        double wind = current.hasNonNull("wind_speed_10m") ? current.get("wind_speed_10m").asDouble() : Double.NaN;
        int code = current.hasNonNull("weather_code") ? current.get("weather_code").asInt() : -1;

        String condition = weatherCodeToText(code);

        return new WeatherInfo(displayName, temp, wind, code, condition);
    }

    private String weatherCodeToText(int code) {
        // Minimal mapping (you can expand later)
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }

    private static class GeoPoint {
        final double latitude;
        final double longitude;
        final String nameResolved;

        GeoPoint(double latitude, double longitude, String nameResolved) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.nameResolved = nameResolved;
        }
    }

    public static class WeatherInfo {
        public final String location;
        public final double temperatureC;
        public final double windKmh;
        public final int weatherCode;
        public final String condition;

        public WeatherInfo(String location, double temperatureC, double windKmh, int weatherCode, String condition) {
            this.location = location;
            this.temperatureC = temperatureC;
            this.windKmh = windKmh;
            this.weatherCode = weatherCode;
            this.condition = condition;
        }
    }
}