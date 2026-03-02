package com.travelxp.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SentimentAnalyzer {
    private static final Set<String> positive = new HashSet<>(Arrays.asList(
            "good", "great", "awesome", "fantastic", "love", "nice", "excellent", "happy", "pleasant"
    ));
    private static final Set<String> negative = new HashSet<>(Arrays.asList(
            "bad", "terrible", "horrible", "hate", "awful", "worst", "sad", "angry", "poor"
    ));

    public static String analyze(String text) {
        if (text == null || text.isEmpty()) return "NEUTRAL";
        String[] words = text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
        int score = 0;
        for (String w : words) {
            if (positive.contains(w)) score++;
            if (negative.contains(w)) score--;
        }
        if (score > 0) return "POSITIVE";
        if (score < 0) return "NEGATIVE";
        return "NEUTRAL";
    }
}
