package com.travelxp.services;

import java.io.InputStream;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * SMTP Email Service (API #1)
 *
 * Sends transactional emails for booking events:
 *   - Booking confirmation
 *   - Booking cancellation (with refund info)
 *   - Booking modification
 *
 * Configure SMTP credentials in db.properties:
 *   mail.smtp.host, mail.smtp.port, mail.smtp.user, mail.smtp.password, mail.from
 */
public class EmailService {

    private final String smtpHost;
    private final int    smtpPort;
    private final String smtpUser;
    private final String smtpPassword;
    private final String fromAddress;
    private final boolean enabled;

    public EmailService() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.smtpHost     = props.getProperty("mail.smtp.host", "smtp.gmail.com");
        this.smtpPort     = Integer.parseInt(props.getProperty("mail.smtp.port", "587"));
        this.smtpUser     = props.getProperty("mail.smtp.user", "");
        this.smtpPassword = props.getProperty("mail.smtp.password", "");
        this.fromAddress  = props.getProperty("mail.from", smtpUser);
        this.enabled      = !smtpUser.isEmpty() && !smtpPassword.isEmpty();
    }

    /**
     * Send a generic email message.
     */
    public boolean sendEmail(String to, String subject, String htmlBody) {
        if (!enabled) {
            System.out.println("[EmailService] SMTP not configured – email not sent.");
            System.out.println("  To: " + to);
            System.out.println("  Subject: " + subject);
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("[EmailService] Email sent to " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("[EmailService] Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== Convenience booking emails ====================

    public void sendBookingConfirmation(String toEmail, int bookingId, String bookingDate,
                                         int duration, double totalPrice, String currency) {
        String subject = "TravelXP – Booking #" + bookingId + " Confirmed!";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#002b5c;'>Booking Confirmed &#10004;</h2>"
                + "<p>Your booking <strong>#" + bookingId + "</strong> has been confirmed.</p>"
                + "<table style='border-collapse:collapse;width:100%;'>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><strong>Date</strong></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + bookingDate + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><strong>Duration</strong></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + duration + " nights</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><strong>Total Price</strong></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + String.format("%.2f %s", totalPrice, currency) + "</td></tr>"
                + "</table>"
                + "<p style='margin-top:20px;color:#666;'>Thank you for choosing TravelXP!</p>"
                + "</div>";
        sendEmail(toEmail, subject, body);
    }

    public void sendCancellationNotice(String toEmail, int bookingId, double refundAmount,
                                        String policyApplied, String currency) {
        String subject = "TravelXP – Booking #" + bookingId + " Cancelled";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#f44336;'>Booking Cancelled</h2>"
                + "<p>Your booking <strong>#" + bookingId + "</strong> has been cancelled.</p>"
                + "<p><strong>Policy applied:</strong> " + policyApplied + "</p>"
                + "<p><strong>Refund amount:</strong> " + String.format("%.2f %s", refundAmount, currency) + "</p>"
                + "<p style='margin-top:20px;color:#666;'>We hope to see you again on TravelXP!</p>"
                + "</div>";
        sendEmail(toEmail, subject, body);
    }

    public void sendBookingModification(String toEmail, int bookingId, String changeDescription,
                                         double newTotalPrice, String currency) {
        String subject = "TravelXP – Booking #" + bookingId + " Updated";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#D4AF37;'>Booking Updated</h2>"
                + "<p>Your booking <strong>#" + bookingId + "</strong> has been modified.</p>"
                + "<p><strong>Change:</strong> " + changeDescription + "</p>"
                + "<p><strong>New total:</strong> " + String.format("%.2f %s", newTotalPrice, currency) + "</p>"
                + "<p style='margin-top:20px;color:#666;'>Thank you for using TravelXP!</p>"
                + "</div>";
        sendEmail(toEmail, subject, body);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
