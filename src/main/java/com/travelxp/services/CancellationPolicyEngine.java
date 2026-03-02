package com.travelxp.services;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.travelxp.models.Booking;

/**
 * Cancellation Policy Engine
 * Rules:
 *   - Free cancellation (100% refund) if cancelled within 24 hours of booking creation
 *   - 50% refund if cancelled more than 3 days before the booking date
 *   - No refund if cancelled 1 day (or less) before the booking date
 */
public class CancellationPolicyEngine {

    public static class CancellationResult {
        private final double refundAmount;
        private final double refundPercentage;
        private final String policyApplied;

        public CancellationResult(double refundAmount, double refundPercentage, String policyApplied) {
            this.refundAmount = refundAmount;
            this.refundPercentage = refundPercentage;
            this.policyApplied = policyApplied;
        }

        public double getRefundAmount() { return refundAmount; }
        public double getRefundPercentage() { return refundPercentage; }
        public String getPolicyApplied() { return policyApplied; }
    }

    /**
     * Calculate the refund for a booking cancellation.
     *
     * @param booking      the booking being cancelled
     * @param createdDate  the date/time the booking was originally created (from DB created_at)
     * @return CancellationResult with refund details
     */
    public CancellationResult calculateRefund(Booking booking, java.sql.Timestamp createdDate) {
        LocalDate today = LocalDate.now();
        double totalPrice = booking.getTotalPrice();

        // Rule 1: Free cancellation within 24 hours of booking creation
        if (createdDate != null) {
            long hoursSinceCreation = ChronoUnit.HOURS.between(createdDate.toLocalDateTime(), java.time.LocalDateTime.now());
            if (hoursSinceCreation <= 24) {
                return new CancellationResult(totalPrice, 100.0,
                        "Free cancellation – within 24 hours of booking creation (100% refund)");
            }
        }

        // Rule 2 & 3: Based on days before the booking date
        Date bookingDate = booking.getBookingDate();
        if (bookingDate != null) {
            long daysBeforeBooking = ChronoUnit.DAYS.between(today, bookingDate.toLocalDate());

            if (daysBeforeBooking > 3) {
                // 50% refund – cancelled more than 3 days before booking date
                double refund = totalPrice * 0.50;
                return new CancellationResult(refund, 50.0,
                        "50% refund – cancelled more than 3 days before booking date");
            } else if (daysBeforeBooking <= 1) {
                // No refund – cancelled 1 day or less before booking date
                return new CancellationResult(0, 0.0,
                        "No refund – cancelled 1 day or less before booking date");
            } else {
                // Between 1-3 days: 50% refund
                double refund = totalPrice * 0.50;
                return new CancellationResult(refund, 50.0,
                        "50% refund – cancelled 2-3 days before booking date");
            }
        }

        // Fallback: full refund if no booking date set
        return new CancellationResult(totalPrice, 100.0, "Full refund – no booking date specified");
    }

    /**
     * Convenience overload when no created_at timestamp is available.
     * Falls back to date-based rules only.
     */
    public CancellationResult calculateRefund(Booking booking) {
        return calculateRefund(booking, null);
    }
}
