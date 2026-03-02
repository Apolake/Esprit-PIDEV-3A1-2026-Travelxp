package com.travelxp.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Currency Exchange Service (API #2)
 *
 * Uses the free ExchangeRate-API (https://open.er-api.com) – no key required.
 * Fetches live exchange rates and converts amounts between currencies.
 */
public class CurrencyExchangeService {

    private static final String API_BASE = "https://open.er-api.com/v6/latest/";

    // Cache to avoid repeated API calls within the same session
    private final Map<String, Map<String, Double>> rateCache = new HashMap<>();

    /**
     * Convert an amount from one currency to another.
     *
     * @param amount       the amount in the source currency
     * @param fromCurrency ISO-4217 code (e.g. "USD")
     * @param toCurrency   ISO-4217 code (e.g. "EUR")
     * @return converted amount, or -1 if conversion fails
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) return amount;

        double rate = getRate(fromCurrency.toUpperCase(), toCurrency.toUpperCase());
        if (rate < 0) return -1;
        return Math.round(amount * rate * 100.0) / 100.0;
    }

    /**
     * Get the exchange rate from fromCurrency to toCurrency.
     *
     * @return rate, or -1 on error
     */
    public double getRate(String fromCurrency, String toCurrency) {
        Map<String, Double> rates = getRates(fromCurrency);
        if (rates == null) return -1;
        Double rate = rates.get(toCurrency);
        return rate != null ? rate : -1;
    }

    /**
     * Get all rates for a given base currency (cached per session).
     */
    public Map<String, Double> getRates(String baseCurrency) {
        baseCurrency = baseCurrency.toUpperCase();
        if (rateCache.containsKey(baseCurrency)) {
            return rateCache.get(baseCurrency);
        }

        try {
            String urlStr = API_BASE + baseCurrency;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("[CurrencyExchangeService] API returned status " + status);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            if (!"success".equals(json.get("result").getAsString())) {
                System.err.println("[CurrencyExchangeService] API result: " + json.get("result"));
                return null;
            }

            JsonObject ratesObj = json.getAsJsonObject("rates");
            Map<String, Double> ratesMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : ratesObj.entrySet()) {
                ratesMap.put(entry.getKey(), entry.getValue().getAsDouble());
            }

            rateCache.put(baseCurrency, ratesMap);
            return ratesMap;
        } catch (Exception e) {
            System.err.println("[CurrencyExchangeService] Error fetching rates: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a list of supported currency codes.
     */
    public Set<String> getSupportedCurrencies() {
        Map<String, Double> rates = getRates("USD");
        return rates != null ? rates.keySet() : Set.of("USD", "EUR", "GBP", "TND", "JPY", "CAD", "AUD", "CHF");
    }

    /**
     * Clear the cached rates (useful if the user wants to refresh).
     */
    public void clearCache() {
        rateCache.clear();
    }
}
