package com.travelxp.ai;

import com.travelxp.models.Trip;

public class TripAIService {

    /**
     * Generates the automatic first message
     */
    public String generateFirstMessage(TripAIContext context) {

        Trip trip = context.getTrip();

        String name = safe(trip.getTripName());
        String origin = safe(trip.getOrigin());
        String destination = safe(trip.getDestination());
        String start = trip.getStartDate() != null ? trip.getStartDate().toString() : "Not specified";
        String end = trip.getEndDate() != null ? trip.getEndDate().toString() : "Not specified";
        String budget = trip.getBudgetAmount() != null
                ? "$" + trip.getBudgetAmount()
                : "Not specified";

        return """
👋 Hello! I'm your TravelXP AI Assistant.

✈️ Trip Overview (from your trip data):

• Trip: %s
• Route: %s → %s
• Start Date: %s
• End Date: %s
• Budget: %s

I can help you plan, optimize, and better understand this trip.

Here are some things you can ask me:

• Can you help me improve this trip plan?
• Is my budget realistic?
• What should I prepare for this destination?
• How can I optimize my schedule?
• What travel tips should I know?

Ask me anything about THIS trip 🙂
"""
                .formatted(name, origin, destination, start, end, budget);
    }

    private String safe(String v) {
        return v == null || v.isBlank() ? "Not specified" : v;
    }
}