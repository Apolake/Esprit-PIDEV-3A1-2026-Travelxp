package com.travelxp.utils;

import com.travelxp.models.Trip;

public class EmailTemplates {

    public static String tripConfirmationHtml(String fullName, Trip trip, double totalCost) {
        return """
            <div style="font-family:Segoe UI,Arial,sans-serif;max-width:600px;margin:auto;padding:18px">
              <h2 style="margin:0;color:#111827">TravelXP - Participation Confirmed ✅</h2>
              <p style="color:#374151">Hello %s,</p>
              <p style="color:#374151">
                Your participation in the trip has been confirmed successfully.
              </p>

              <div style="background:#f3f4f6;border:1px solid #e5e7eb;border-radius:12px;padding:14px">
                <p style="margin:6px 0"><b>Trip:</b> %s</p>
                <p style="margin:6px 0"><b>Route:</b> %s → %s</p>
                <p style="margin:6px 0"><b>Dates:</b> %s to %s</p>
                <p style="margin:6px 0"><b>Total Paid:</b> $%.2f</p>
              </div>

              <p style="margin-top:16px;color:#6b7280;font-size:12px">
                If you did not make this action, please contact support immediately.
              </p>
            </div>
        """.formatted(
                safe(fullName),
                safe(trip.getTripName()),
                safe(trip.getOrigin()),
                safe(trip.getDestination()),
                trip.getStartDate() != null ? trip.getStartDate() : "-",
                trip.getEndDate() != null ? trip.getEndDate() : "-",
                totalCost
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}