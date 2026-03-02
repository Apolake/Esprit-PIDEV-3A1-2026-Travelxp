package com.travelxp.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class LanguageToolClient {
    private static final String API = "https://api.languagetool.org/v2/check";

    public static String check(String text) throws Exception {
        if (text == null || text.isEmpty()) return text;
        String params = "language=en-US&text=" + java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
        byte[] postData = params.getBytes(StandardCharsets.UTF_8);

        URL url = new URL(API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        JSONObject json = new JSONObject(sb.toString());
        JSONArray matches = json.getJSONArray("matches");
        StringBuilder result = new StringBuilder(text);
        // apply replacements from end to start
        for (int i = matches.length() - 1; i >= 0; i--) {
            JSONObject match = matches.getJSONObject(i);
            int offset = match.getInt("offset");
            int length = match.getInt("length");
            JSONArray reps = match.getJSONArray("replacements");
            if (reps.length() > 0) {
                String rep = reps.getJSONObject(0).getString("value");
                result.replace(offset, offset + length, rep);
            }
        }
        return result.toString();
    }

    public static String checkGrammar(String text) throws Exception {
        return check(text);
    }
}
