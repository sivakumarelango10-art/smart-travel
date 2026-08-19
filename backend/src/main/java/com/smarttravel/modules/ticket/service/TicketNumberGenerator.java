package com.smarttravel.modules.ticket.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Cryptographically secure Ticket Number Generator.
 * Generates unique, collision-resistant public ticket identifiers formatted as "ST-XXXXXXXXXXXX".
 */
@Component
public class TicketNumberGenerator {

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TICKET_CODE_LENGTH = 12;
    private static final String PREFIX = "ST-";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new 15-character public ticket number (e.g., ST-8K4P2Q7X9Y1Z).
     */
    public String generateTicketNumber() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < TICKET_CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Generates an individual e-ticket number for a specific passenger in a booking.
     *
     * @param primaryTicketNumber Master ticket number
     * @param passengerIndex 1-based passenger index
     * @return Formatted passenger e-ticket number (e.g., ST-8K4P2Q7X9Y1Z-01)
     */
    public String generatePassengerETicketNumber(String primaryTicketNumber, int passengerIndex) {
        return String.format("%s-%02d", primaryTicketNumber, passengerIndex);
    }

    public String generatePassengerTicketNumber(String primaryTicketNumber, int passengerIndex) {
        return generatePassengerETicketNumber(primaryTicketNumber, passengerIndex);
    }
}
