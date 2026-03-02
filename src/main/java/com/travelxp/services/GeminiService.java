package com.travelxp.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Service for communicating with the Google Gemini API.
 * Uses the REST API via java.net.http.HttpClient and Gson for JSON.
 * Maintains conversation history for multi-turn chat.
 */
public class GeminiService {

    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String[] MODELS = {
            "gemini-2.5-flash"
    };
    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 2000;

    private final HttpClient httpClient;
    private final Gson gson;
    private String apiKey;
    private int currentModelIndex = 0;
    private final List<JsonObject> conversationHistory;

    /**
     * System prompt that teaches Gemini everything about TravelXP.
     */
    private static final String SYSTEM_PROMPT = """
            You are the TravelXP Assistant, a friendly and knowledgeable AI helper built into the TravelXP desktop travel application. \
            Your job is to help users understand how to use the app, navigate its features, and answer questions about its capabilities.

            Here is a comprehensive description of TravelXP:

            === APP OVERVIEW ===
            TravelXP is a JavaFX desktop travel platform where users can browse/book properties, plan trips, manage activities, \
            earn XP through a gamification system, and interact via feedback/comments. It has a role-based system (regular users and admins).

            === NAVIGATION ===
            The app sidebar (visible on most pages) provides navigation to all major sections:
            - Dashboard: Main hub showing profile overview, wallet balance, gamification rank (XP/level), featured properties, and featured trips.
            - Browse Properties: View available accommodation properties with images, locations, and prices. Book properties directly.
            - My Bookings: View your bookings, cancel them (with balance refund), or edit booking duration (recalculates price).
            - Browse Trips: Explore available public trips with origin/destination details and budgets.
            - My Trips: View trips you've joined or created.
            - Tasks: A navigation hub page.
            - Activities: Manage trip activities (title, type, date, cost, status). Earn XP from completing activities.
            - Feedback: Create, update, and delete feedback posts. View and manage comments on feedback.
            - Edit Profile: Update email, birthday, bio, profile image. Also access Face ID enrollment and TOTP 2FA setup.
            - Security (Change Password): Change your account password.
            - Logout: Log out and return to the login screen.

            === KEY FEATURES ===
            1. **Account & Authentication:**
               - Register with username, email, password, birthday, bio, and profile image.
               - Login with email/password. If TOTP 2FA is enabled, you'll need to enter an authenticator code.
               - Login with Face ID (if enrolled) by clicking "Login with Face ID" on the login page.
               - Change password from the Security page.
               - Delete your account from the Profile page.

            2. **Face ID:**
               - Enroll by going to Edit Profile → "Register / Update Face ID". The webcam captures 5 photos to train a face recognition model.
               - Once enrolled, use "Login with Face ID" on the login page for quick biometric login.

            3. **TOTP Two-Factor Authentication (2FA):**
               - Enable from Edit Profile → "Setup / Manage 2FA (TOTP)".
               - A QR code is displayed to scan with an authenticator app (Google Authenticator, Authy, etc.).
               - Once enabled, login requires entering a 6-digit code from your authenticator app after entering email/password.
               - Disable 2FA from the same page (requires current TOTP code confirmation).

            4. **Wallet & Payments:**
               - Your balance is shown on the Dashboard profile card.
               - Click "Recharge" to add funds. If Stripe is configured, a Stripe Checkout payment page opens in your browser.
               - If Stripe is not configured, funds are added directly (demo mode).
               - Balance is deducted when booking properties and refunded when cancelling bookings.

            5. **Properties:**
               - Browse properties as visual cards showing image, title, location, and price per night.
               - Click "Book Now" to open a booking dialog. Select check-in/check-out dates, see available offers/discounts, and add extra services.
               - The total price is calculated based on duration, discounts, and services. Requires sufficient balance.

            6. **Bookings:**
               - View your bookings from "My Bookings" in the sidebar.
               - Cancel a booking to get a refund to your wallet balance.
               - Edit booking duration to extend or shorten your stay (price recalculated, balance adjusted).

            7. **Trips:**
               - Browse public trips with origin, destination, dates, and budget information.
               - Join or participate in trips.
               - View your trips from "My Trips".
               - Trips have statuses: PLANNED, ONGOING, COMPLETED, CANCELLED.
               - Earn XP by completing trips.

            8. **Activities:**
               - Create and manage activities linked to your trips.
               - Activities have a title, type, date, cost, and status.
               - Earn XP from completing activities.

            9. **Gamification (XP & Levels):**
               - Earn XP from trips, activities, services, and being active on the platform.
               - Your level, title (e.g., Novice, Explorer, etc.), and XP progress are shown on the Dashboard.
               - Higher levels unlock higher titles.

            10. **Feedback & Comments:**
                - Share feedback about the platform from the Feedback page.
                - View other users' feedback and add comments.
                - Edit or delete your own feedback and comments.

            11. **Theme Toggle:**
                - Toggle between dark and light mode using the theme button (available on every page, usually top-right).

            === ADMIN FEATURES ===
            Admin users have additional management pages accessible from their sidebar:
            - Admin Dashboard: View all users, create users, edit user roles/details, reset passwords, delete users.
            - Manage Properties: Full CRUD for properties (add, edit, delete properties with all details).
            - Manage Offers: Create discount offers for properties with percentage/amount, date ranges, and active status.
            - Manage Bookings: View and manage all user bookings.
            - Manage Trips: Full CRUD for trips.
            - Manage Activities: Full CRUD for trip activities.
            - Manage Services: CRUD for travel services (provider, type, price, eco-friendly flag, XP reward).
            - Moderation: Edit or delete any user's feedback and comments.

            === INSTRUCTIONS FOR YOU ===
            - Be helpful, concise, and friendly.
            - When users ask how to do something, give step-by-step instructions referencing the sidebar menu items and button names.
            - If a user asks about a feature that doesn't exist, let them know politely and suggest related features.
            - You can answer general travel questions too, but always relate back to how TravelXP can help.
            - Format your responses clearly. Use short paragraphs. Mention exact button/menu names in quotes.
            - If the user seems confused, offer to explain the main navigation structure.
            """;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
        loadApiKey();
    }

    private void loadApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                apiKey = props.getProperty("gemini.api.key", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("WARNING: gemini.api.key not found in db.properties. Gemini assistant will not work.");
        }
    }

    /**
     * Check if the Gemini API key is configured.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Send a user message and get the assistant's response.
     * Maintains conversation history for context.
     *
     * @param userMessage The user's message
     * @return The assistant's response text
     * @throws IOException          on network error
     * @throws InterruptedException if the request is interrupted
     */
    public String chat(String userMessage) throws IOException, InterruptedException {
        // Add user message to conversation history
        JsonObject userPart = new JsonObject();
        userPart.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", userMessage);
        userParts.add(textPart);
        userPart.add("parts", userParts);
        conversationHistory.add(userPart);

        // Build the request body
        JsonObject requestBody = new JsonObject();

        // System instruction
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemTextPart = new JsonObject();
        systemTextPart.addProperty("text", SYSTEM_PROMPT);
        systemParts.add(systemTextPart);
        systemInstruction.add("parts", systemParts);
        requestBody.add("system_instruction", systemInstruction);

        // Contents (conversation history)
        JsonArray contentsArray = new JsonArray();
        for (JsonObject msg : conversationHistory) {
            contentsArray.add(msg);
        }
        requestBody.add("contents", contentsArray);

        // Generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 1024);
        requestBody.add("generationConfig", generationConfig);

        String jsonBody = gson.toJson(requestBody);

        // Try each model with retries on 429
        IOException lastError = null;
        int startModel = currentModelIndex;

        for (int modelAttempt = 0; modelAttempt < MODELS.length; modelAttempt++) {
            int modelIdx = (startModel + modelAttempt) % MODELS.length;
            String model = MODELS[modelIdx];
            String url = API_BASE + model + ":generateContent?key=" + apiKey;

            for (int retry = 0; retry <= MAX_RETRIES; retry++) {
                if (retry > 0) {
                    Thread.sleep(RETRY_DELAY_MS * retry);
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    currentModelIndex = modelIdx; // remember working model
                    String assistantText = parseSuccessResponse(response.body());

                    // Add assistant response to conversation history
                    JsonObject modelPart = new JsonObject();
                    modelPart.addProperty("role", "model");
                    JsonArray modelParts = new JsonArray();
                    JsonObject modelTextPart = new JsonObject();
                    modelTextPart.addProperty("text", assistantText);
                    modelParts.add(modelTextPart);
                    modelPart.add("parts", modelParts);
                    conversationHistory.add(modelPart);

                    return assistantText;
                }

                if (response.statusCode() == 429) {
                    lastError = new IOException(parseErrorMessage(response.body()));
                    // If last retry for this model, break to try next model
                    if (retry == MAX_RETRIES) break;
                    continue; // retry same model
                }

                // Other errors — don't retry, try next model
                lastError = new IOException(parseErrorMessage(response.body()));
                break;
            }
        }

        // All models/retries failed
        if (conversationHistory.size() > 0) {
            conversationHistory.remove(conversationHistory.size() - 1);
        }
        throw lastError != null ? lastError : new IOException("All Gemini models are unavailable.");
    }

    private String parseSuccessResponse(String body) throws IOException {
        try {
            JsonObject responseJson = gson.fromJson(body, JsonObject.class);
            return responseJson
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini response: " + e.getMessage());
        }
    }

    /**
     * Parse an error response from Gemini to extract a clean, user-friendly message.
     */
    private String parseErrorMessage(String body) {
        try {
            JsonObject errorJson = gson.fromJson(body, JsonObject.class);
            if (errorJson.has("error")) {
                JsonObject error = errorJson.getAsJsonObject("error");
                int code = error.has("code") ? error.get("code").getAsInt() : 0;
                String status = error.has("status") ? error.get("status").getAsString() : "UNKNOWN";
                String message = error.has("message") ? error.get("message").getAsString() : body;

                if (code == 429) {
                    return "Rate limit reached. The AI assistant is temporarily unavailable — please try again in a moment.";
                } else if (code == 403) {
                    return "API key doesn't have permission. Please check your Gemini API key in db.properties.";
                } else if (code == 400) {
                    return "Invalid request. Please try rephrasing your question.";
                }
                return "Gemini error (" + status + "): " + message;
            }
        } catch (Exception ignored) {}
        return "Gemini API error: " + body;
    }

    /**
     * Clear conversation history to start a new chat.
     */
    public void clearHistory() {
        conversationHistory.clear();
    }
}
