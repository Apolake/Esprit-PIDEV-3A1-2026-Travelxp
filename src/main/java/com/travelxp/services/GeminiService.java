package com.travelxp.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class GeminiService {

    private static final String MODEL = "gemini-3-flash-preview";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String apiKey;

    public GeminiService() {
        this.apiKey = firstNonBlank(
                System.getenv("GEMINI_API_KEY"),
                System.getProperty("GEMINI_API_KEY")
        );

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not set (env or -DGEMINI_API_KEY).");
        }

        System.out.println("✅ Gemini key loaded, first 8: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    // role لازم يكون: "user" أو "model"
    public String chat(String systemPrompt, List<Message> history) throws IOException {
        JsonObject body = new JsonObject();

        // system_instruction
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            JsonObject sys = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject p = new JsonObject();
            p.addProperty("text", systemPrompt);
            parts.add(p);
            sys.add("parts", parts);
            body.add("system_instruction", sys);
        }

        // contents
        JsonArray contents = new JsonArray();
        for (Message m : history) {
            JsonObject c = new JsonObject();
            c.addProperty("role", m.role()); // user / model

            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", m.content());
            parts.add(part);

            c.add("parts", parts);
            contents.add(c);
        }
        body.add("contents", contents);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(gson.toJson(body), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String raw = (response.body() != null) ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Gemini error: " + response.code() + " - " + raw);
            }

            JsonObject root = gson.fromJson(raw, JsonObject.class);

            return root.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
        }
    }

    public record Message(String role, String content) {}
}