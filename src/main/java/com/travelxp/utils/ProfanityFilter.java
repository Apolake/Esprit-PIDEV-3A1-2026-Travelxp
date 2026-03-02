package com.travelxp.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ProfanityFilter {
    // simple list of words to mask; expand as needed
    private static final Set<String> blacklist = new HashSet<>(Arrays.asList(
            "damn", "hell", "shit", "fuck", "bastard", "bitch"
    ));

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b(\\w+)\\b");

    public static String sanitize(String input) {
        if (input == null) return null;
        return WORD_PATTERN.matcher(input).replaceAll(match -> {
            String w = match.group(1);
            if (blacklist.contains(w.toLowerCase())) {
                if (w.length() <= 2) {
                    return "**";
                }
                StringBuilder stars = new StringBuilder();
                for (int i = 1; i < w.length(); i++) stars.append('*');
                return w.charAt(0) + stars.toString();
            }
            return w;
        });
    }
}
