package com.smarttravel.modules.ticket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TicketNumberGenerator Unit Tests")
class TicketNumberGeneratorTest {

    private TicketNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new TicketNumberGenerator();
    }

    @Test
    @DisplayName("Should generate valid ticket number matching format ST-XXXXXXXXXXXX")
    void shouldGenerateValidTicketNumberFormat() {
        String ticketNumber = generator.generateTicketNumber();

        assertThat(ticketNumber).isNotNull();
        assertThat(ticketNumber).startsWith("ST-");
        assertThat(ticketNumber.length()).isEqualTo(15);
        assertThat(ticketNumber).matches("^ST-[A-Z0-9]{12}$");
    }

    @Test
    @DisplayName("Should generate collision-free unique ticket numbers across high volume")
    void shouldGenerateUniqueTicketNumbers() {
        int count = 1000;
        Set<String> ticketNumbers = new HashSet<>();

        for (int i = 0; i < count; i++) {
            ticketNumbers.add(generator.generateTicketNumber());
        }

        assertThat(ticketNumbers).hasSize(count);
    }

    @Test
    @DisplayName("Should generate formatted passenger e-ticket number with index")
    void shouldGeneratePassengerETicketNumber() {
        String masterTicket = "ST-ABC123XYZ456";

        String p1 = generator.generatePassengerETicketNumber(masterTicket, 1);
        String p2 = generator.generatePassengerETicketNumber(masterTicket, 2);

        assertThat(p1).isEqualTo("ST-ABC123XYZ456-01");
        assertThat(p2).isEqualTo("ST-ABC123XYZ456-02");
    }
}
