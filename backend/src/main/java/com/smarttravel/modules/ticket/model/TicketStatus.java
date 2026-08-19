package com.smarttravel.modules.ticket.model;

/**
 * Lifecycle states of an issued electronic flight ticket.
 */
public enum TicketStatus {
    /**
     * Ticket has been successfully issued after confirmed booking and verified payment.
     */
    ISSUED,

    /**
     * Ticket has been cancelled in accordance with booking cancellation.
     */
    CANCELLED,

    /**
     * Ticket has expired without being used or flight departed and completed.
     */
    EXPIRED
}
