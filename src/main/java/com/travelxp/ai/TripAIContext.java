package com.travelxp.ai;

import com.travelxp.models.Trip;

public class TripAIContext {

    private final Trip trip;

    public TripAIContext(Trip trip) {
        this.trip = trip;
    }

    public Trip getTrip() {
        return trip;
    }
}