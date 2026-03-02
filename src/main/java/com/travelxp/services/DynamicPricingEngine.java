package com.travelxp.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import com.travelxp.models.Service;
import com.travelxp.utils.MyDB;

/**
 * Dynamic Pricing Engine
 *
 * Automatically adjusts booking price based on:
 * 1. Season (summer peak, winter holidays, shoulder, off-peak)
 * 2. Demand (number of existing bookings for the property)
 * 3. Number of guests
 * 4. Added extra services
 * 5. Length of stay (discount for longer bookings)
 */
public class DynamicPricingEngine {

    // ---------- Season multipliers ----------
    private static final double PEAK_SUMMER_MULTIPLIER    = 1.30;  // Jun-Aug
    private static final double WINTER_HOLIDAY_MULTIPLIER = 1.25;  // Dec-Jan
    private static final double SHOULDER_MULTIPLIER       = 1.10;  // Mar-May, Sep
    private static final double OFF_PEAK_MULTIPLIER       = 0.90;  // Feb, Oct-Nov

    // ---------- Demand thresholds ----------
    private static final int    HIGH_DEMAND_THRESHOLD     = 10;
    private static final int    MEDIUM_DEMAND_THRESHOLD   = 5;
    private static final double HIGH_DEMAND_MULTIPLIER    = 1.20;
    private static final double MEDIUM_DEMAND_MULTIPLIER  = 1.10;

    // ---------- Guest pricing ----------
    private static final double EXTRA_GUEST_CHARGE        = 15.0; // per extra guest per night

    // ---------- Length-of-stay discounts ----------
    private static final int    WEEKLY_THRESHOLD          = 7;
    private static final int    MONTHLY_THRESHOLD         = 28;
    private static final double WEEKLY_DISCOUNT           = 0.90;  // 10% off
    private static final double MONTHLY_DISCOUNT          = 0.80;  // 20% off

    private Connection getConnection() {
        return MyDB.getInstance().getConnection();
    }

    /**
     * Calculate the dynamic total price for a booking.
     *
     * @param basePrice      the property's base price per night
     * @param bookingDate    the date the stay starts
     * @param duration       number of nights
     * @param numGuests      number of guests (0 or 1 means single/couple – no surcharge)
     * @param propertyId     used to query current demand
     * @param extraServices  list of extra services the user selected
     * @return PricingBreakdown with the final price and explanation
     */
    public PricingBreakdown calculatePrice(double basePrice, LocalDate bookingDate, int duration,
                                           int numGuests, Long propertyId,
                                           List<Service> extraServices) {
        StringBuilder explanation = new StringBuilder();
        double nightlyRate = basePrice;

        // --- 1. Season ---
        double seasonMultiplier = getSeasonMultiplier(bookingDate);
        nightlyRate *= seasonMultiplier;
        explanation.append(String.format("Season (%s): x%.2f\n", getSeasonName(bookingDate), seasonMultiplier));

        // --- 2. Demand ---
        int currentBookings = countActiveBookingsForProperty(propertyId);
        double demandMultiplier = getDemandMultiplier(currentBookings);
        nightlyRate *= demandMultiplier;
        explanation.append(String.format("Demand (%d active bookings): x%.2f\n", currentBookings, demandMultiplier));

        // --- 3. Extra guests ---
        double guestSurcharge = 0;
        if (numGuests > 2) {
            guestSurcharge = (numGuests - 2) * EXTRA_GUEST_CHARGE;
            explanation.append(String.format("Extra guests (%d): +$%.2f/night\n", numGuests - 2, guestSurcharge));
        }

        double adjustedNightly = nightlyRate + guestSurcharge;

        // --- 4. Duration subtotal ---
        double subtotal = adjustedNightly * duration;
        explanation.append(String.format("Subtotal (%d nights x $%.2f): $%.2f\n", duration, adjustedNightly, subtotal));

        // --- 5. Length-of-stay discount ---
        double stayMultiplier = getStayMultiplier(duration);
        subtotal *= stayMultiplier;
        if (stayMultiplier < 1.0) {
            explanation.append(String.format("Length-of-stay discount: x%.2f\n", stayMultiplier));
        }

        // --- 6. Extra services ---
        double servicesTotal = 0;
        if (extraServices != null && !extraServices.isEmpty()) {
            for (Service s : extraServices) {
                servicesTotal += s.getPrice();
            }
            explanation.append(String.format("Extra services total: +$%.2f\n", servicesTotal));
        }

        double finalPrice = subtotal + servicesTotal;
        explanation.append(String.format("TOTAL: $%.2f", finalPrice));

        return new PricingBreakdown(finalPrice, seasonMultiplier, demandMultiplier,
                stayMultiplier, guestSurcharge * duration, servicesTotal, explanation.toString());
    }

    // ==================== helpers ====================

    private double getSeasonMultiplier(LocalDate date) {
        if (date == null) return 1.0;
        Month month = date.getMonth();
        switch (month) {
            case JUNE: case JULY: case AUGUST:
                return PEAK_SUMMER_MULTIPLIER;
            case DECEMBER: case JANUARY:
                return WINTER_HOLIDAY_MULTIPLIER;
            case MARCH: case APRIL: case MAY: case SEPTEMBER:
                return SHOULDER_MULTIPLIER;
            default:
                return OFF_PEAK_MULTIPLIER;
        }
    }

    private String getSeasonName(LocalDate date) {
        if (date == null) return "Unknown";
        Month month = date.getMonth();
        switch (month) {
            case JUNE: case JULY: case AUGUST: return "Peak Summer";
            case DECEMBER: case JANUARY: return "Winter Holiday";
            case MARCH: case APRIL: case MAY: case SEPTEMBER: return "Shoulder";
            default: return "Off-Peak";
        }
    }

    private double getDemandMultiplier(int activeBookings) {
        if (activeBookings >= HIGH_DEMAND_THRESHOLD) return HIGH_DEMAND_MULTIPLIER;
        if (activeBookings >= MEDIUM_DEMAND_THRESHOLD) return MEDIUM_DEMAND_MULTIPLIER;
        return 1.0;
    }

    private double getStayMultiplier(int duration) {
        if (duration >= MONTHLY_THRESHOLD) return MONTHLY_DISCOUNT;
        if (duration >= WEEKLY_THRESHOLD)  return WEEKLY_DISCOUNT;
        return 1.0;
    }

    private int countActiveBookingsForProperty(Long propertyId) {
        if (propertyId == null) return 0;
        String sql = "SELECT COUNT(*) FROM booking WHERE property_id = ? AND booking_status IN ('CONFIRMED','PENDING')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, propertyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ==================== Result holder ====================

    public static class PricingBreakdown {
        private final double totalPrice;
        private final double seasonMultiplier;
        private final double demandMultiplier;
        private final double stayDiscount;
        private final double guestSurcharge;
        private final double servicesTotal;
        private final String explanation;

        public PricingBreakdown(double totalPrice, double seasonMultiplier, double demandMultiplier,
                                double stayDiscount, double guestSurcharge, double servicesTotal,
                                String explanation) {
            this.totalPrice = totalPrice;
            this.seasonMultiplier = seasonMultiplier;
            this.demandMultiplier = demandMultiplier;
            this.stayDiscount = stayDiscount;
            this.guestSurcharge = guestSurcharge;
            this.servicesTotal = servicesTotal;
            this.explanation = explanation;
        }

        public double getTotalPrice() { return totalPrice; }
        public double getSeasonMultiplier() { return seasonMultiplier; }
        public double getDemandMultiplier() { return demandMultiplier; }
        public double getStayDiscount() { return stayDiscount; }
        public double getGuestSurcharge() { return guestSurcharge; }
        public double getServicesTotal() { return servicesTotal; }
        public String getExplanation() { return explanation; }
    }
}
