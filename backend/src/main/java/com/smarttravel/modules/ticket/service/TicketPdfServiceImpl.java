package com.smarttravel.modules.ticket.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Production implementation of PDF E-Ticket generation using OpenPDF.
 * Renders structured, deterministic, and professionally styled electronic flight tickets.
 */
@Service
public class TicketPdfServiceImpl implements TicketPdfService {

    private static final Logger log = LoggerFactory.getLogger(TicketPdfServiceImpl.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm 'UTC'")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    // Corporate Color Palette
    private static final Color BRAND_NAVY = new Color(24, 43, 73);
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color BORDER_GRAY = new Color(226, 232, 240);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color STATUS_GREEN = new Color(22, 101, 52);
    private static final Color STATUS_RED = new Color(153, 27, 27);

    @Override
    public byte[] generateTicketPdf(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null for PDF generation");
        }

        log.debug("Generating PDF E-Ticket for ticket number: {} (PNR: {})", ticket.getTicketNumber(), ticket.getBookingReference());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Set document metadata
            document.addTitle("SmartTravel E-Ticket - " + ticket.getTicketNumber());
            document.addSubject("Flight Booking Confirmation PNR " + ticket.getBookingReference());
            document.addAuthor("SmartTravel Platform");
            document.addCreator("SmartTravel PDF Engine");

            // Fonts
            Font headerTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND_NAVY);
            Font headerSubtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLUE);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_NAVY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, TEXT_MUTED);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);

            // 1. BRAND HEADER TABLE
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            Paragraph brandP = new Paragraph("SMARTTRAVEL", headerTitleFont);
            Paragraph subBrandP = new Paragraph("ELECTRONIC TICKET & PASSENGER RECEIPT", headerSubtitleFont);
            logoCell.addElement(brandP);
            logoCell.addElement(subBrandP);
            headerTable.addCell(logoCell);

            PdfPCell statusCell = new PdfPCell();
            statusCell.setBorder(Rectangle.NO_BORDER);
            statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Color statusColor = "CANCELLED".equalsIgnoreCase(String.valueOf(ticket.getStatus())) ? STATUS_RED : STATUS_GREEN;
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, statusColor);
            Paragraph pnrP = new Paragraph("BOOKING PNR: " + ticket.getBookingReference(), valueFont);
            pnrP.setAlignment(Element.ALIGN_RIGHT);
            Paragraph tktP = new Paragraph("TICKET NO: " + ticket.getTicketNumber(), smallFont);
            tktP.setAlignment(Element.ALIGN_RIGHT);
            Paragraph statusP = new Paragraph("STATUS: " + ticket.getStatus(), statusFont);
            statusP.setAlignment(Element.ALIGN_RIGHT);

            statusCell.addElement(pnrP);
            statusCell.addElement(tktP);
            statusCell.addElement(statusP);
            headerTable.addCell(statusCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // 2. FLIGHT ITINERARY SECTION
            PdfPTable itineraryTitleTable = createSectionHeader("1. FLIGHT ITINERARY", sectionHeaderFont);
            document.add(itineraryTitleTable);

            PdfPTable flightTable = new PdfPTable(3);
            flightTable.setWidthPercentage(100);
            flightTable.setWidths(new float[]{40, 20, 40});

            // Departure Cell
            AirportInfo dep = ticket.getDepartureAirport();
            String depCode = dep != null ? dep.getCode() : "N/A";
            String depName = dep != null ? dep.getName() : "Departure Airport";
            String depCity = dep != null ? (dep.getCity() + ", " + dep.getCountry()) : "";
            String depTime = ticket.getDepartureTime() != null ? DATE_TIME_FORMATTER.format(ticket.getDepartureTime()) : "TBD";

            PdfPCell depCell = createBoxCell();
            depCell.addElement(new Paragraph("DEPARTURE", labelFont));
            depCell.addElement(new Paragraph(depCode + " - " + depCity, valueFont));
            depCell.addElement(new Paragraph(depName, smallFont));
            depCell.addElement(new Paragraph(depTime, valueFont));
            if (dep != null && dep.getTerminal() != null && !dep.getTerminal().isBlank()) {
                depCell.addElement(new Paragraph("Terminal: " + dep.getTerminal(), smallFont));
            }
            flightTable.addCell(depCell);

            // Flight Middle info Cell
            PdfPCell midCell = createBoxCell();
            midCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph flightNoP = new Paragraph(ticket.getAirlineCode() + "-" + ticket.getFlightNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_BLUE));
            flightNoP.setAlignment(Element.ALIGN_CENTER);
            Paragraph airlineP = new Paragraph(ticket.getAirline(), smallFont);
            airlineP.setAlignment(Element.ALIGN_CENTER);
            Paragraph durP = new Paragraph((ticket.getDurationMinutes() != null ? ticket.getDurationMinutes() : 0) + " Mins | Direct", smallFont);
            durP.setAlignment(Element.ALIGN_CENTER);
            Paragraph cabinP = new Paragraph("Class: " + ticket.getCabinClass(), labelFont);
            cabinP.setAlignment(Element.ALIGN_CENTER);

            midCell.addElement(flightNoP);
            midCell.addElement(airlineP);
            midCell.addElement(durP);
            midCell.addElement(cabinP);
            flightTable.addCell(midCell);

            // Arrival Cell
            AirportInfo arr = ticket.getArrivalAirport();
            String arrCode = arr != null ? arr.getCode() : "N/A";
            String arrName = arr != null ? arr.getName() : "Arrival Airport";
            String arrCity = arr != null ? (arr.getCity() + ", " + arr.getCountry()) : "";
            String arrTime = ticket.getArrivalTime() != null ? DATE_TIME_FORMATTER.format(ticket.getArrivalTime()) : "TBD";

            PdfPCell arrCell = createBoxCell();
            arrCell.addElement(new Paragraph("ARRIVAL", labelFont));
            arrCell.addElement(new Paragraph(arrCode + " - " + arrCity, valueFont));
            arrCell.addElement(new Paragraph(arrName, smallFont));
            arrCell.addElement(new Paragraph(arrTime, valueFont));
            if (arr != null && arr.getTerminal() != null && !arr.getTerminal().isBlank()) {
                arrCell.addElement(new Paragraph("Terminal: " + arr.getTerminal(), smallFont));
            }
            flightTable.addCell(arrCell);

            document.add(flightTable);
            document.add(new Paragraph(" "));

            // 3. PASSENGERS SECTION
            PdfPTable passengerTitleTable = createSectionHeader("2. PASSENGER DETAILS", sectionHeaderFont);
            document.add(passengerTitleTable);

            PdfPTable passTable = new PdfPTable(6);
            passTable.setWidthPercentage(100);
            passTable.setWidths(new float[]{6, 34, 15, 15, 10, 20});

            // Table Header
            addTableHeader(passTable, "#", labelFont);
            addTableHeader(passTable, "Passenger Name", labelFont);
            addTableHeader(passTable, "Gender", labelFont);
            addTableHeader(passTable, "Nationality", labelFont);
            addTableHeader(passTable, "Seat", labelFont);
            addTableHeader(passTable, "E-Ticket No.", labelFont);

            List<PassengerTicketInfo> passengers = ticket.getPassengers();
            if (passengers != null && !passengers.isEmpty()) {
                for (int i = 0; i < passengers.size(); i++) {
                    PassengerTicketInfo p = passengers.get(i);
                    String name = String.format("%s %s %s",
                            p.getTitle() != null ? p.getTitle() : "",
                            p.getFirstName() != null ? p.getFirstName() : "",
                            p.getLastName() != null ? p.getLastName() : "").trim();
                    String gender = p.getGender() != null ? p.getGender() : "N/A";
                    String nat = p.getNationality() != null ? p.getNationality() : "Indian";
                    String seat = p.getSeatNumber() != null ? p.getSeatNumber() : "Assigned at Check-in";
                    String etkt = p.getETicketNumber() != null ? p.getETicketNumber() : ticket.getTicketNumber();

                    addTableCell(passTable, String.valueOf(i + 1), normalFont);
                    addTableCell(passTable, name, valueFont);
                    addTableCell(passTable, gender, normalFont);
                    addTableCell(passTable, nat, normalFont);
                    addTableCell(passTable, seat, normalFont);
                    addTableCell(passTable, etkt, smallFont);
                }
            }
            document.add(passTable);
            document.add(new Paragraph(" "));

            // 4. FARE BREAKDOWN & PAYMENT SUMMARY
            PdfPTable fareTitleTable = createSectionHeader("3. FARE & PAYMENT SUMMARY", sectionHeaderFont);
            document.add(fareTitleTable);

            PdfPTable fareTable = new PdfPTable(2);
            fareTable.setWidthPercentage(100);
            fareTable.setWidths(new float[]{70, 30});

            String currency = ticket.getCurrency() != null ? ticket.getCurrency() : "INR";
            BigDecimal total = ticket.getTotalAmount() != null ? ticket.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal baseFare = ticket.getFareBreakdown() != null && ticket.getFareBreakdown().getBaseFare() != null
                    ? ticket.getFareBreakdown().getBaseFare() : BigDecimal.ZERO;
            BigDecimal taxes = ticket.getFareBreakdown() != null && ticket.getFareBreakdown().getTaxes() != null
                    ? ticket.getFareBreakdown().getTaxes() : BigDecimal.ZERO;
            BigDecimal fees = ticket.getFareBreakdown() != null && ticket.getFareBreakdown().getFees() != null
                    ? ticket.getFareBreakdown().getFees() : BigDecimal.ZERO;

            addFareRow(fareTable, "Base Airfare", currency + " " + baseFare.toPlainString(), normalFont);
            addFareRow(fareTable, "Government Airport Taxes & Surcharges", currency + " " + taxes.toPlainString(), normalFont);
            addFareRow(fareTable, "Platform & Airline Convenience Fees", currency + " " + fees.toPlainString(), normalFont);
            addFareRow(fareTable, "Total Amount Paid (Payment Verified)", currency + " " + total.toPlainString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_NAVY));

            document.add(fareTable);
            document.add(new Paragraph(" "));

            // 5. IMPORTANT NOTICES & LEGAL FOOTER
            PdfPTable noticesTable = new PdfPTable(1);
            noticesTable.setWidthPercentage(100);

            PdfPCell noticeCell = createBoxCell();
            noticeCell.addElement(new Paragraph("IMPORTANT TRAVEL NOTICES", labelFont));
            noticeCell.addElement(new Paragraph("1. Check-in counters close 60 minutes prior to domestic departures and 75 minutes prior to international flights.", smallFont));
            noticeCell.addElement(new Paragraph("2. Please present a valid Government-issued photo identification (Aadhaar, Passport, Voter ID) matching the ticket name at airport security.", smallFont));
            noticeCell.addElement(new Paragraph("3. Standard cabin baggage allowance is 7kg. Checked baggage rules apply as per airline policy.", smallFont));
            String issuedAtStr = ticket.getIssuedAt() != null ? DATE_TIME_FORMATTER.format(ticket.getIssuedAt()) : "N/A";
            noticeCell.addElement(new Paragraph("Issued by SmartTravel Platform on " + issuedAtStr + ". This is a computer-generated electronic receipt.", smallFont));
            noticesTable.addCell(noticeCell);

            document.add(noticesTable);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Failed to generate PDF document for ticket: {}", ticket.getTicketNumber(), ex);
            throw new com.smarttravel.common.exception.BusinessRuleException("Failed to generate PDF E-Ticket document: " + ex.getMessage());
        }
    }

    private PdfPTable createSectionHeader(String title, Font font) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(4);

        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(BG_LIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(6);
        table.addCell(cell);
        return table;
    }

    private PdfPCell createBoxCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BG_LIGHT);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1);
        cell.setPadding(8);
        return cell;
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BG_LIGHT);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addFareRow(PdfPTable table, String label, String amount, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorderColor(BORDER_GRAY);
        labelCell.setPadding(6);

        PdfPCell amtCell = new PdfPCell(new Phrase(amount, font));
        amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amtCell.setBorderColor(BORDER_GRAY);
        amtCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(amtCell);
    }
}
