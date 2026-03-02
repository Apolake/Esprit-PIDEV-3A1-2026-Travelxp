package com.travelxp.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.scene.image.Image;

/**
 * TOTP (Time-based One-Time Password) service implementing RFC 6238.
 * Generates secrets, validates codes, and produces QR codes for authenticator apps.
 */
public class TotpService {

    private static final int SECRET_LENGTH = 20; // 160 bits
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int WINDOW = 1; // Allow 1 step before/after for clock drift
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final String ISSUER = "TravelXP";

    // ==================== Secret Key Generation ====================

    /**
     * Generate a new random TOTP secret key.
     * @return Base32-encoded secret string.
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_LENGTH];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    // ==================== Code Generation & Validation ====================

    /**
     * Generate the current TOTP code for the given secret.
     * @param secret Base32-encoded secret.
     * @return The current 6-digit TOTP code as a string (zero-padded).
     */
    public String generateCode(String secret) {
        long timeStep = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        return generateCodeForStep(secret, timeStep);
    }

    /**
     * Validate a TOTP code against the given secret.
     * Allows a window of +/- 1 time step for clock drift tolerance.
     * @param secret Base32-encoded secret.
     * @param code   The 6-digit code provided by the user.
     * @return true if code is valid within the time window.
     */
    public boolean validateCode(String secret, String code) {
        if (secret == null || code == null || code.length() != CODE_DIGITS) {
            return false;
        }

        long currentStep = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;

        for (int i = -WINDOW; i <= WINDOW; i++) {
            String expected = generateCodeForStep(secret, currentStep + i);
            if (expected.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCodeForStep(String secret, long timeStep) {
        try {
            byte[] key = base32Decode(secret);
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                       | ((hash[offset + 1] & 0xFF) << 16)
                       | ((hash[offset + 2] & 0xFF) << 8)
                       | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP code generation failed", e);
        }
    }

    // ==================== QR Code Generation ====================

    /**
     * Build the otpauth:// URI for authenticator apps.
     * @param secret Base32-encoded secret.
     * @param email  The user's email (used as the account name).
     * @return The otpauth URI string.
     */
    public String buildOtpAuthUri(String secret, String email) {
        String encodedIssuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                encodedIssuer, encodedEmail, secret, encodedIssuer, CODE_DIGITS, TIME_STEP_SECONDS);
    }

    /**
     * Generate a JavaFX Image containing the QR code for the given secret and email.
     * The QR code encodes the otpauth:// URI for scanning with Google Authenticator, Authy, etc.
     * @param secret Base32-encoded secret.
     * @param email  The user's email.
     * @return A JavaFX Image of the QR code.
     */
    public Image generateQrCodeImage(String secret, String email) throws WriterException, IOException {
        String uri = buildOtpAuthUri(secret, email);
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(uri, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        return new Image(new ByteArrayInputStream(out.toByteArray()));
    }

    // ==================== Base32 Encoding/Decoding ====================

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * Encode bytes to Base32 (RFC 4648) without padding.
     */
    public static String base32Encode(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            result.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return result.toString();
    }

    /**
     * Decode a Base32 (RFC 4648) string to bytes.
     */
    public static byte[] base32Decode(String encoded) {
        encoded = encoded.toUpperCase().replaceAll("[^A-Z2-7]", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0;
        int bitsLeft = 0;

        for (char c : encoded.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                out.write((buffer >> bitsLeft) & 0xFF);
            }
        }
        return out.toByteArray();
    }
}
