package com.smarttravel.modules.ticket.service;

import com.smarttravel.modules.ticket.model.Ticket;

/**
 * Service for generating deterministic, professional PDF E-Tickets from ticket entity snapshots.
 */
public interface TicketPdfService {

    /**
     * Generates a binary PDF document for the given ticket snapshot.
     *
     * @param ticket Ticket historical snapshot
     * @return Raw PDF document byte array
     */
    byte[] generateTicketPdf(Ticket ticket);
}
