package com.smarttravel.modules.booking.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * High-entropy PNR (Passenger Name Record) / Booking Reference generator.
 * Generates unambiguous 8-character alphanumeric references (e.g., ST8K4P2Q).
 */
@Component
public class PnrGenerator {

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PNR_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new 8-character cryptographic booking reference.
     */
    public String generatePnr() {
        StringBuilder sb = new StringBuilder(PNR_LENGTH);
        for (int i = 0; i < PNR_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }
}
