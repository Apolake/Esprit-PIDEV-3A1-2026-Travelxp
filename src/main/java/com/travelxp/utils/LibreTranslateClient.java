package com.travelxp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LibreTranslateClient {
    // Use a simple, direct approach with a reliable free API
    private static final String TRANSLATE_API = "https://api.mymemory.translated.net/get";
    private static final int TIMEOUT_MS = 30000; // 30 second timeout

    public static String translate(String text, String targetLang) throws Exception {
        if (text == null || text.isEmpty()) return text;
        if (targetLang == null || targetLang.trim().isEmpty()) targetLang = "en";
        targetLang = targetLang.trim().toLowerCase();

        return translateWithMyMemory(text, targetLang);
    }
    
    private static String translateWithMyMemory(String text, String targetLang) throws Exception {
        // URL encode the text
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        
        // Build the MyMemory API URL
        // MyMemory doesn't support "auto" source language, so we'll use "en" as default
        // or try to detect if the text looks like English
        String sourceLang = detectLanguage(text);
        String urlStr = TRANSLATE_API + 
                       "?q=" + encodedText +
                       "&langpair=" + sourceLang + "|" + targetLang;
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP Error: " + responseCode + " - " + conn.getResponseMessage());
        }
        
        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        // Parse the response to extract translated text
        return parseMyMemoryResponse(response.toString());
    }
    
    private static String detectLanguage(String text) {
        // Simple language detection based on common characters
        if (text == null || text.isEmpty()) return "en";
        
        // Check for common non-English characters
        if (text.matches(".*[àâäéèêëîïôöùûüÿçÀÂÄÉÈÊËÎÏÔÖÙÛÜŸÇ].*")) {
            return "fr"; // French
        }
        if (text.matches(".*[ñáéíóúüÑÁÉÍÓÚÜ].*")) {
            return "es"; // Spanish
        }
        if (text.matches(".*[àèéìíîòóùúÀÈÉÌÍÎÒÓÙÚ].*")) {
            return "it"; // Italian
        }
        if (text.matches(".*[äöüßÄÖÜ].*")) {
            return "de"; // German
        }
        if (text.matches(".*[ãõÃÕ].*")) {
            return "pt"; // Portuguese
        }
        if (text.matches(".*[абвгдежзийклмнопрстуфхцчшщъыьэюяАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ].*")) {
            return "ru"; // Russian
        }
        if (text.matches(".*[あいうえお][ぁぃぅぇぉ].*")) {
            return "ja"; // Japanese
        }
        if (text.matches(".*[가나다라마바사아자차카타파하].*")) {
            return "ko"; // Korean
        }
        if (text.matches(".*[的一是不了人我在有他这为].*")) {
            return "zh"; // Chinese
        }
        
        // Default to English for English-like text or unknown
        return "en";
    }
    
    private static String parseMyMemoryResponse(String response) throws Exception {
        try {
            // MyMemory returns JSON like: {"responseData":{"translatedText":"Hello"},"responseStatus":200}
            // Look for "translatedText":"content"
            int start = response.indexOf("\"translatedText\":\"");
            if (start != -1) {
                start += 18; // Length of "\"translatedText\":\""
                int end = response.indexOf("\"", start);
                if (end != -1) {
                    String translated = response.substring(start, end);
                    // Unescape common escape sequences including Unicode
                    return unescapeTranslation(translated);
                }
            }
            
            // Alternative: look for responseData.translatedText
            start = response.indexOf("\"translatedText\":");
            if (start != -1) {
                start = response.indexOf("\"", start + 17); // Find the quote after translatedText
                if (start != -1) {
                    start += 1;
                    int end = response.indexOf("\"", start);
                    if (end != -1) {
                        String translated = response.substring(start, end);
                        return unescapeTranslation(translated);
                    }
                }
            }
            
            throw new Exception("Could not parse translation response format");
            
        } catch (Exception e) {
            throw new Exception("Failed to parse translation response: " + e.getMessage());
        }
    }
    
    private static String unescapeTranslation(String text) {
        if (text == null) return null;
        
        // Handle Unicode escape sequences like \u00a0
        text = text.replaceAll("\\\\u00a0", " "); // Non-breaking space to regular space
        text = text.replaceAll("\\\\u0020", " "); // Space
        text = text.replaceAll("\\\\u0021", "!"); // Exclamation mark
        text = text.replaceAll("\\\\u0022", "\""); // Double quote
        text = text.replaceAll("\\\\u0027", "'"); // Single quote
        text = text.replaceAll("\\\\u002c", ","); // Comma
        text = text.replaceAll("\\\\u002e", "."); // Period
        text = text.replaceAll("\\\\u003f", "?"); // Question mark
        text = text.replaceAll("\\\\u0021", "!"); // Exclamation mark
        text = text.replaceAll("\\\\u0028", "("); // Left parenthesis
        text = text.replaceAll("\\\\u0029", ")"); // Right parenthesis
        text = text.replaceAll("\\\\u005b", "["); // Left bracket
        text = text.replaceAll("\\\\u005d", "]"); // Right bracket
        text = text.replaceAll("\\\\u007b", "{"); // Left brace
        text = text.replaceAll("\\\\u007d", "}"); // Right brace
        text = text.replaceAll("\\\\u003a", ":"); // Colon
        text = text.replaceAll("\\\\u003b", ";"); // Semicolon
        text = text.replaceAll("\\\\u003c", "<"); // Less than
        text = text.replaceAll("\\\\u003e", ">"); // Greater than
        text = text.replaceAll("\\\\u002f", "/"); // Forward slash
        text = text.replaceAll("\\\\u005c", "\\"); // Backslash
        
        // Handle standard escape sequences
        text = text.replace("\\n", "\n")
                  .replace("\\t", "\t")
                  .replace("\\r", "\r")
                  .replace("\\\"", "\"")
                  .replace("\\\\", "\\");
        
        return text;
    }
}
