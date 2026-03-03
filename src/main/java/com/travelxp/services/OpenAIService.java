package com.travelxp.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class OpenAIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String apiKey;

    public OpenAIService() {
        this.apiKey = firstNonBlank(
                System.getenv("OPENROUTER_API_KEY"),
                System.getenv("OPENAI_API_KEY"),
                System.getProperty("OPENROUTER_API_KEY"),
                System.getProperty("OPENAI_API_KEY")
        );

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key not found. Set OPENROUTER_API_KEY or OPENAI_API_KEY.");
        }

        System.out.println("✅ Key loaded, first 8: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    public String chat(String systemPrompt, List<Message> history) throws IOException {

        JsonObject body = new JsonObject();
        body.addProperty("model", "google/gemma-3n-e2b-it:free");
        body.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", systemPrompt);
        messages.add(sys);

        for (Message m : history) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.role());
            msg.addProperty("content", m.content());
            messages.add(msg);
        }

        body.add("messages", messages);

        System.out.println("ENV OPENROUTER_API_KEY = " + System.getenv("OPENROUTER_API_KEY"));
        System.out.println("PROP OPENROUTER_API_KEY = " + System.getProperty("OPENROUTER_API_KEY"));
        System.out.println("Using apiKey length = " + (apiKey == null ? "null" : apiKey.length()));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("HTTP-Referer", "http://localhost")
                .addHeader("X-Title", "TravelXP")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(gson.toJson(body), MediaType.parse("application/json")))
                .build();

        // ✅ أهم سطر تشخيص:
        System.out.println("Request headers: " + request.headers());

        try (Response response = client.newCall(request).execute()) {
            String raw = (response.body() != null) ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("OpenRouter error: " + response.code() + " - " + raw);
            }

            JsonObject root = gson.fromJson(raw, JsonObject.class);
            return root.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString().trim();
        }
    }

    public record Message(String role, String content) {}
}