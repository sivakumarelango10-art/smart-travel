package com.smarttravel.modules.booking.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smarttravel.modules.booking.model.BoardingPass;
import com.smarttravel.modules.flight.model.AirportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Production implementation of PDF Boarding Pass generator using OpenPDF.
 */
@Service
public class BoardingPassPdfServiceImpl implements BoardingPassPdfService {

    private static final Logger log = LoggerFactory.getLogger(BoardingPassPdfServiceImpl.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'UTC'")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'UTC'")
            .withZone(ZoneId.of("UTC"));

    // Styling Palette
    private static final Color BRAND_NAVY = new Color(15, 23, 42);
    private static final Color BRAND_PRIMARY = new Color(2, 132, 199);
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color BORDER_GRAY = new Color(203, 213, 225);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color ACCENT_SEAT = new Color(220, 38, 38);

    @Override
    public byte[] generateBoardingPassPdf(BoardingPass boardingPass) {
        if (boardingPass == null) {
            throw new IllegalArgumentException("BoardingPass cannot be null for PDF generation");
        }
        return generateMultiBoardingPassPdf(List.of(boardingPass));
    }

    @Override
    public byte[] generateMultiBoardingPassPdf(List<BoardingPass> boardingPasses) {
        if (boardingPasses == null || boardingPasses.isEmpty()) {
            throw new IllegalArgumentException("Boarding pass list cannot be empty for PDF generation");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            document.addTitle("SmartTravel Official Boarding Pass");
            document.addSubject("Flight Boarding Pass");
            document.addAuthor("SmartTravel Airline Operations");
            document.addCreator("SmartTravel PDF Engine");

            // Fonts
            Font headerTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND_NAVY);
            Font headerSubtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND_PRIMARY);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_NAVY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, TEXT_MUTED);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
            Font largeValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND_NAVY);
            Font seatFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, ACCENT_SEAT);

            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7, TEXT_MUTED);
            Font barcodeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 12, BRAND_NAVY);

            for (int i = 0; i < boardingPasses.size(); i++) {
                BoardingPass bp = boardingPasses.get(i);
                if (i > 0) {
                    document.newPage();
                }

                // 1. BRAND HEADER TABLE
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                headerTable.setWidths(new float[]{65, 35});

                PdfPCell logoCell = new PdfPCell();
                logoCell.setBorder(Rectangle.NO_BORDER);
                try {
                    java.net.URL logoUrl = getClass().getResource("/static/logo.png");
                    if (logoUrl != null) {
                        com.lowagie.text.Image logoImg = com.lowagie.text.Image.getInstance(logoUrl);
                        logoImg.scaleToFit(140, 50);
                        logoCell.addElement(logoImg);
                    } else {
                        logoCell.addElement(new Paragraph("SMARTTRAVEL AIRWAYS", headerTitleFont));
                        logoCell.addElement(new Paragraph("OFFICIAL BOARDING PASS", headerSubtitleFont));
                    }
                } catch (Exception imgEx) {
                    logoCell.addElement(new Paragraph("SMARTTRAVEL AIRWAYS", headerTitleFont));
                    logoCell.addElement(new Paragraph("OFFICIAL BOARDING PASS", headerSubtitleFont));
                }
                headerTable.addCell(logoCell);

                PdfPCell metaCell = new PdfPCell();
                metaCell.setBorder(Rectangle.NO_BORDER);
                metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                Paragraph pnrP = new Paragraph();
                pnrP.setAlignment(Element.ALIGN_RIGHT);
                pnrP.add(new Chunk("PNR / BOOKING: ", labelFont));
                pnrP.add(new Chunk(bp.getBookingReference() != null ? bp.getBookingReference() : "N/A", valueFont));
                metaCell.addElement(pnrP);

                Paragraph bpNumP = new Paragraph();
                bpNumP.setAlignment(Element.ALIGN_RIGHT);
                bpNumP.add(new Chunk("PASS NO: ", labelFont));
                bpNumP.add(new Chunk(bp.getBoardingPassNumber() != null ? bp.getBoardingPassNumber() : "N/A", smallFont));
                metaCell.addElement(bpNumP);
                headerTable.addCell(metaCell);

                document.add(headerTable);
                document.add(new Paragraph(" "));

                // 2. MAIN BOARDING PASS CARD
                PdfPTable cardTable = new PdfPTable(4);
                cardTable.setWidthPercentage(100);
                cardTable.setWidths(new float[]{30, 25, 25, 20});

                // Row 1: Passenger Name, Flight, Cabin, Seat
                addCardCell(cardTable, "PASSENGER NAME", bp.getPassengerName(), labelFont, largeValueFont, 1);
                addCardCell(cardTable, "FLIGHT", bp.getFlightNumber() != null ? bp.getFlightNumber() : "N/A", labelFont, largeValueFont, 1);
                addCardCell(cardTable, "CLASS", bp.getCabinClass() != null ? bp.getCabinClass().name() : "ECONOMY", labelFont, largeValueFont, 1);
                addCardCell(cardTable, "SEAT", bp.getSeatNumber() != null ? bp.getSeatNumber() : "TBD", labelFont, seatFont, 1);

                // Row 2: Route Origin & Destination
                AirportInfo dep = bp.getDepartureAirport();
                String originStr = dep != null ? dep.getCity() + " (" + dep.getCode() + ")" : "N/A";
                AirportInfo arr = bp.getArrivalAirport();
                String destStr = arr != null ? arr.getCity() + " (" + arr.getCode() + ")" : "N/A";

                addCardCell(cardTable, "FROM / DEPARTURE AIRPORT", originStr, labelFont, valueFont, 2);
                addCardCell(cardTable, "TO / ARRIVAL AIRPORT", destStr, labelFont, valueFont, 2);

                // Row 3: Departure Time, Boarding Time, Gate, Terminal
                String depTimeStr = bp.getDepartureTime() != null ? DATE_TIME_FORMATTER.format(bp.getDepartureTime()) : "N/A";
                String brdTimeStr = bp.getBoardingTime() != null ? TIME_FORMATTER.format(bp.getBoardingTime())
                        : (bp.getDepartureTime() != null ? TIME_FORMATTER.format(bp.getDepartureTime().minusSeconds(45 * 60)) : "45m before");

                addCardCell(cardTable, "DATE & DEPARTURE TIME", depTimeStr, labelFont, valueFont, 1);
                addCardCell(cardTable, "BOARDING TIME", brdTimeStr, labelFont, largeValueFont, 1);
                addCardCell(cardTable, "GATE", bp.getGate() != null ? bp.getGate() : "Gate 12", labelFont, largeValueFont, 1);
                addCardCell(cardTable, "TERMINAL", bp.getTerminal() != null ? bp.getTerminal() : "T3", labelFont, largeValueFont, 1);

                // Row 4: Boarding Group, E-Ticket, Airline
                addCardCell(cardTable, "BOARDING GROUP", bp.getBoardingGroup() != null ? bp.getBoardingGroup() : "Group 1", labelFont, valueFont, 1);
                addCardCell(cardTable, "E-TICKET NUMBER", bp.getETicketNumber() != null ? bp.getETicketNumber() : (bp.getTicketNumber() != null ? bp.getTicketNumber() : "N/A"), labelFont, smallFont, 2);
                addCardCell(cardTable, "CARRIER", bp.getAirline() != null ? bp.getAirline() : "SmartTravel Airways", labelFont, smallFont, 1);

                document.add(cardTable);
                document.add(new Paragraph(" "));

                // 3. BARCODE & SECURITY NOTICE TABLE
                PdfPTable footerTable = new PdfPTable(2);
                footerTable.setWidthPercentage(100);
                footerTable.setWidths(new float[]{60, 40});

                PdfPCell noticeCell = new PdfPCell();
                noticeCell.setBackgroundColor(BG_LIGHT);
                noticeCell.setBorderColor(BORDER_GRAY);
                noticeCell.setPadding(8);
                noticeCell.addElement(new Paragraph("SECURITY & BOARDING INFORMATION", sectionTitleFont));
                noticeCell.addElement(new Paragraph("• Please be at the boarding gate at least 30 minutes prior to departure.", smallFont));
                noticeCell.addElement(new Paragraph("• Valid government photo ID is mandatory along with this boarding pass.", smallFont));
                noticeCell.addElement(new Paragraph("• Cabin baggage must comply with maximum size and weight dimensions.", smallFont));
                footerTable.addCell(noticeCell);

                PdfPCell barcodeCell = new PdfPCell();
                barcodeCell.setBackgroundColor(BG_LIGHT);
                barcodeCell.setBorderColor(BORDER_GRAY);
                barcodeCell.setPadding(8);
                barcodeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                Paragraph barcodeHeader = new Paragraph("SCAN AT GATE", labelFont);
                barcodeHeader.setAlignment(Element.ALIGN_CENTER);
                Paragraph barcodeP = new Paragraph("||||| ||||||| || |||||||| |||| ||||||", barcodeFont);
                barcodeP.setAlignment(Element.ALIGN_CENTER);
                Paragraph barcodeData = new Paragraph(bp.getBoardingPassNumber() != null ? bp.getBoardingPassNumber() : bp.getBookingReference(), smallFont);
                barcodeData.setAlignment(Element.ALIGN_CENTER);
                barcodeCell.addElement(barcodeHeader);
                barcodeCell.addElement(barcodeP);
                barcodeCell.addElement(barcodeData);
                footerTable.addCell(barcodeCell);

                document.add(footerTable);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Failed to generate boarding pass PDF", ex);
            throw new RuntimeException("Error rendering Boarding Pass PDF: " + ex.getMessage(), ex);
        }
    }

    private void addCardCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont, int colspan) {
        PdfPCell cell = new PdfPCell();
        cell.setColspan(colspan);
        cell.setBackgroundColor(BG_LIGHT);
        cell.setBorderColor(BORDER_GRAY);
        cell.setPadding(6);
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value != null ? value : "N/A", valueFont));
        table.addCell(cell);
    }
}
