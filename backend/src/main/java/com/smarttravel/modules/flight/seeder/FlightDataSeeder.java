package com.smarttravel.modules.flight.seeder;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic Production Flight Data Seeder.
 * Populates MongoDB with comprehensive domestic and international flight routes,
 * covering 40+ premier destinations with authentic airline fleets, exact aircraft models
 * (Airbus A320neo, Airbus A321neo, Airbus A320ceo, Boeing 737 MAX 8, Boeing 737-800,
 * ATR 72-600, Airbus A350-900, Boeing 787-9 Dreamliner), rolling daily schedules through 2027,
 * and multi-cabin inventories.
 */
@Component
public class FlightDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlightDataSeeder.class);

    private final FlightRepository flightRepository;

    public FlightDataSeeder(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            Instant startWindow = today.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endWindow = today.plusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant();

            long upcomingFlightsCount = flightRepository.countByDepartureTimeBetweenAndActiveTrue(startWindow, endWindow);
            log.info("Current upcoming flights in next 30 days: {}", upcomingFlightsCount);

            if (upcomingFlightsCount >= 300) {
                log.info("MongoDB flight collection verified with active upcoming routes.");
                return;
            }

            List<Flight> seededFlights = generateComprehensiveFleet();
            List<Flight> toInsert = new ArrayList<>();
            for (Flight f : seededFlights) {
                if (!flightRepository.existsByFlightNumber(f.getFlightNumber())) {
                    toInsert.add(f);
                }
            }
            if (!toInsert.isEmpty()) {
                // Batch insert for maximum performance
                int batchSize = 500;
                for (int i = 0; i < toInsert.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toInsert.size());
                    flightRepository.saveAll(toInsert.subList(i, end));
                }
                log.info("Successfully seeded {} comprehensive daily global flights into MongoDB.", toInsert.size());
            }
        } catch (Exception ex) {
            log.warn("Flight seeding encountered non-fatal error during startup: {}", ex.getMessage());
        }
    }

    public List<Flight> generateComprehensiveFleet() {
        List<Flight> fleet = new ArrayList<>();
        Instant now = Instant.now();

        // =========================================================================
        // 1. LIVE FLIGHTS (DEPARTED, BOARDING, DELAYED, ON_TIME) FOR RADAR & STATUS
        // =========================================================================
        fleet.add(buildFlight("AI-101", "Air India", "AI", "DEL", "BOM", "Boeing 787-9 Dreamliner", now.minus(45, ChronoUnit.MINUTES), 130, 4500, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("6E-204", "IndiGo", "6E", "BOM", "BLR", "Airbus A321neo", now.minus(25, ChronoUnit.MINUTES), 105, 3800, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("UK-955", "Vistara", "UK", "DEL", "BOM", "Airbus A321neo", now.plus(20, ChronoUnit.MINUTES), 130, 5600, FlightStatus.BOARDING, null, null));
        fleet.add(buildFlight("6E-551", "IndiGo", "6E", "DEL", "HYD", "Airbus A320neo", now.plus(1, ChronoUnit.HOURS), 135, 4200, FlightStatus.DELAYED, 40, "Air traffic congestion at New Delhi"));
        fleet.add(buildFlight("AI-504", "Air India", "AI", "DEL", "BLR", "Airbus A350-900", now.plus(2, ChronoUnit.HOURS), 165, 5200, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("6E-678", "IndiGo", "6E", "DEL", "MAA", "Airbus A320ceo", now.plus(3, ChronoUnit.HOURS), 170, 4900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("SG-303", "SpiceJet", "SG", "DEL", "CCU", "Boeing 737-800", now.plus(1, ChronoUnit.HOURS), 135, 3900, FlightStatus.DELAYED, 25, "Incoming aircraft turn-around"));
        fleet.add(buildFlight("QP-1102", "Akasa Air", "QP", "BOM", "BLR", "Boeing 737 MAX 8", now.plus(4, ChronoUnit.HOURS), 105, 3400, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("IX-801", "Air India Express", "IX", "DEL", "BOM", "Boeing 737 MAX 8", now.plus(2, ChronoUnit.HOURS), 130, 3900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("9I-501", "Alliance Air", "9I", "DEL", "IXC", "ATR 72-600", now.plus(1, ChronoUnit.HOURS), 55, 2300, FlightStatus.ON_TIME, null, null));

        // Indian Vacation & Leisure Live Routes
        fleet.add(buildFlight("6E-101", "IndiGo", "6E", "DEL", "GOI", "Airbus A320neo", now.plus(2, ChronoUnit.HOURS), 150, 5400, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-812", "Air India", "AI", "BOM", "GOI", "Airbus A320ceo", now.plus(1, ChronoUnit.HOURS), 75, 3200, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("6E-344", "IndiGo", "6E", "DEL", "SXR", "Airbus A321neo", now.plus(3, ChronoUnit.HOURS), 85, 4600, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-409", "Air India", "AI", "DEL", "COK", "Airbus A350-900", now.plus(4, ChronoUnit.HOURS), 190, 6200, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-722", "IndiGo", "6E", "BOM", "COK", "Airbus A320neo", now.plus(5, ChronoUnit.HOURS), 115, 3900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-491", "Air India", "AI", "DEL", "JAI", "Airbus A320ceo", now.plus(2, ChronoUnit.HOURS), 55, 2900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-289", "IndiGo", "6E", "BOM", "UDR", "ATR 72-600", now.plus(3, ChronoUnit.HOURS), 80, 3600, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-406", "Air India", "AI", "DEL", "VNS", "Airbus A320neo", now.plus(4, ChronoUnit.HOURS), 80, 3400, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-885", "IndiGo", "6E", "DEL", "IXZ", "Airbus A321neo", now.plus(6, ChronoUnit.HOURS), 310, 8900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-018", "Air India", "AI", "DEL", "AMD", "Airbus A320ceo", now.plus(3, ChronoUnit.HOURS), 90, 3500, FlightStatus.SCHEDULED, null, null));

        // Tropical Vacations Live
        fleet.add(buildFlight("GA-850", "Garuda Indonesia", "GA", "DEL", "DPS", "Airbus A330-300", now.minus(90, ChronoUnit.MINUTES), 540, 16999, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("AI-214", "Air India", "AI", "BOM", "DPS", "Boeing 787-9 Dreamliner", now.plus(7, ChronoUnit.HOURS), 525, 17499, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-1121", "IndiGo", "6E", "BOM", "MLE", "Airbus A321neo", now.minus(30, ChronoUnit.MINUTES), 165, 11999, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("AI-263", "Air India", "AI", "DEL", "MLE", "Airbus A320neo", now.plus(5, ChronoUnit.HOURS), 240, 14200, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("TG-316", "Thai Airways", "TG", "DEL", "BKK", "Boeing 777-300ER", now.plus(4, ChronoUnit.HOURS), 260, 13800, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-1073", "IndiGo", "6E", "BOM", "HKT", "Airbus A321neo", now.plus(6, ChronoUnit.HOURS), 285, 12900, FlightStatus.SCHEDULED, null, null));

        // Middle East Flagships Live
        fleet.add(buildFlight("EK-500", "Emirates", "EK", "BOM", "DXB", "Boeing 777-300ER", now.minus(50, ChronoUnit.MINUTES), 195, 21000, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("EK-512", "Emirates", "EK", "DEL", "DXB", "Airbus A380-800", now.plus(8, ChronoUnit.HOURS), 230, 24000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("EY-205", "Etihad Airways", "EY", "BOM", "AUH", "Boeing 787-9", now.plus(6, ChronoUnit.HOURS), 210, 22500, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("QR-571", "Qatar Airways", "QR", "DEL", "DOH", "Boeing 777-300ER", now.plus(5, ChronoUnit.HOURS), 250, 28000, FlightStatus.SCHEDULED, null, null));

        // Southeast Asia & Far East Live
        fleet.add(buildFlight("SQ-402", "Singapore Airlines", "SQ", "DEL", "SIN", "Airbus A350-900", now.plus(9, ChronoUnit.HOURS), 335, 32000, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("SQ-423", "Singapore Airlines", "SQ", "BOM", "SIN", "Boeing 787-10", now.minus(80, ChronoUnit.MINUTES), 320, 29500, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("UK-115", "Vistara", "UK", "BLR", "SIN", "Airbus A321neo", now.plus(4, ChronoUnit.HOURS), 275, 14299, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("MH-191", "Malaysia Airlines", "MH", "DEL", "KUL", "Airbus A330-300", now.plus(7, ChronoUnit.HOURS), 330, 18500, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("JL-740", "Japan Airlines", "JL", "DEL", "HND", "Boeing 787-9", now.minus(120, ChronoUnit.MINUTES), 485, 46000, FlightStatus.DEPARTED, null, null));

        // Europe & Americas Live
        fleet.add(buildFlight("BA-112", "British Airways", "BA", "DEL", "LHR", "Boeing 787-9", now.minus(110, ChronoUnit.MINUTES), 555, 52000, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("AI-112", "Air India", "AI", "BOM", "LHR", "Airbus A350-900", now.plus(10, ChronoUnit.HOURS), 565, 48000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AF-225", "Air France", "AF", "DEL", "CDG", "Boeing 777-300ER", now.plus(8, ChronoUnit.HOURS), 550, 54000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("LH-760", "Lufthansa", "LH", "DEL", "FRA", "Boeing 747-8", now.plus(9, ChronoUnit.HOURS), 520, 51000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-105", "Air India", "AI", "DEL", "JFK", "Boeing 787-9 Dreamliner", now.plus(14, ChronoUnit.HOURS), 940, 72000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-173", "Air India", "AI", "DEL", "SFO", "Airbus A350-900", now.plus(16, ChronoUnit.HOURS), 970, 78000, FlightStatus.SCHEDULED, null, null));

        // =========================================================================
        // 2. COMPREHENSIVE DAILY RECURRENT SCHEDULES ACROSS 180 DAYS
        // =========================================================================
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate endDate = today.plusDays(180);

        List<RouteTemplate> templates = getRouteTemplates();

        for (LocalDate cur = today; !cur.isAfter(endDate); cur = cur.plusDays(1)) {
            String dateSuffix = cur.format(DateTimeFormatter.BASIC_ISO_DATE);

            for (RouteTemplate t : templates) {
                Instant dep = cur.atTime(t.hour, t.minute).toInstant(ZoneOffset.UTC);
                String flNum = t.airlineCode + "-" + t.flightCodeNumber + "-" + dateSuffix;
                fleet.add(buildFlight(
                        flNum,
                        t.airline,
                        t.airlineCode,
                        t.orig,
                        t.dest,
                        t.aircraft,
                        dep,
                        t.durationMins,
                        t.basePrice,
                        FlightStatus.SCHEDULED,
                        null,
                        null
                ));
            }
        }

        return fleet;
    }

    public static class RouteTemplate {
        public String airline;
        public String airlineCode;
        public int flightCodeNumber;
        public String orig;
        public String dest;
        public String aircraft;
        public int hour;
        public int minute;
        public int durationMins;
        public double basePrice;

        public RouteTemplate(String airline, String airlineCode, int flightCodeNumber,
                             String orig, String dest, String aircraft,
                             int hour, int minute, int durationMins, double basePrice) {
            this.airline = airline;
            this.airlineCode = airlineCode;
            this.flightCodeNumber = flightCodeNumber;
            this.orig = orig;
            this.dest = dest;
            this.aircraft = aircraft;
            this.hour = hour;
            this.minute = minute;
            this.durationMins = durationMins;
            this.basePrice = basePrice;
        }
    }

    public static List<RouteTemplate> getRouteTemplates() {
        List<RouteTemplate> list = new ArrayList<>();

        // ─────────────────────────────────────────────────────────────────────────
        // 1. HIGH-FREQUENCY DOMESTIC CORRIDORS (Using exact specified aircraft)
        // ─────────────────────────────────────────────────────────────────────────
        
        // DEL <-> BOM (Morning, Afternoon, Evening, Night)
        list.add(new RouteTemplate("Air India", "AI", 101, "DEL", "BOM", "Boeing 787-9 Dreamliner", 6, 30, 130, 4650));
        list.add(new RouteTemplate("IndiGo", "6E", 501, "DEL", "BOM", "Airbus A321neo", 8, 45, 125, 4200));
        list.add(new RouteTemplate("Air India", "AI", 805, "DEL", "BOM", "Airbus A350-900", 11, 15, 130, 4950));
        list.add(new RouteTemplate("SpiceJet", "SG", 123, "DEL", "BOM", "Boeing 737-800", 14, 15, 135, 3850));
        list.add(new RouteTemplate("Akasa Air", "QP", 1301, "DEL", "BOM", "Boeing 737 MAX 8", 17, 30, 130, 3950));
        list.add(new RouteTemplate("Air India Express", "IX", 801, "DEL", "BOM", "Boeing 737 MAX 8", 19, 15, 130, 3900));
        list.add(new RouteTemplate("IndiGo", "6E", 605, "DEL", "BOM", "Airbus A320neo", 21, 0, 125, 4100));
        list.add(new RouteTemplate("Air India", "AI", 103, "DEL", "BOM", "Airbus A320ceo", 22, 45, 130, 4300));

        // BOM <-> DEL (Reciprocal)
        list.add(new RouteTemplate("IndiGo", "6E", 502, "BOM", "DEL", "Airbus A321neo", 7, 0, 130, 4200));
        list.add(new RouteTemplate("Air India", "AI", 102, "BOM", "DEL", "Airbus A350-900", 9, 30, 135, 4850));
        list.add(new RouteTemplate("Air India Express", "IX", 802, "BOM", "DEL", "Boeing 737 MAX 8", 12, 15, 130, 3900));
        list.add(new RouteTemplate("SpiceJet", "SG", 124, "BOM", "DEL", "Boeing 737-800", 15, 45, 135, 3850));
        list.add(new RouteTemplate("Akasa Air", "QP", 1302, "BOM", "DEL", "Boeing 737 MAX 8", 18, 50, 130, 3950));
        list.add(new RouteTemplate("Air India", "AI", 806, "BOM", "DEL", "Boeing 787-9 Dreamliner", 21, 30, 135, 4750));

        // DEL <-> BLR & BLR <-> DEL
        list.add(new RouteTemplate("Air India", "AI", 504, "DEL", "BLR", "Airbus A350-900", 6, 30, 165, 5200));
        list.add(new RouteTemplate("IndiGo", "6E", 201, "DEL", "BLR", "Airbus A321neo", 10, 15, 160, 4800));
        list.add(new RouteTemplate("Air India", "AI", 506, "DEL", "BLR", "Airbus A320neo", 15, 0, 165, 5100));
        list.add(new RouteTemplate("Akasa Air", "QP", 1401, "DEL", "BLR", "Boeing 737 MAX 8", 19, 45, 165, 4600));

        list.add(new RouteTemplate("IndiGo", "6E", 202, "BLR", "DEL", "Airbus A321neo", 7, 45, 165, 4800));
        list.add(new RouteTemplate("Air India", "AI", 505, "BLR", "DEL", "Airbus A350-900", 11, 30, 165, 5200));
        list.add(new RouteTemplate("Air India", "AI", 507, "BLR", "DEL", "Airbus A320ceo", 17, 15, 165, 5100));
        list.add(new RouteTemplate("Akasa Air", "QP", 1402, "BLR", "DEL", "Boeing 737 MAX 8", 21, 0, 165, 4600));

        // BOM <-> BLR & BLR <-> BOM
        list.add(new RouteTemplate("IndiGo", "6E", 204, "BOM", "BLR", "Airbus A321neo", 9, 15, 105, 3850));
        list.add(new RouteTemplate("Akasa Air", "QP", 1102, "BOM", "BLR", "Boeing 737 MAX 8", 14, 30, 105, 3400));
        list.add(new RouteTemplate("Air India Express", "IX", 901, "BOM", "BLR", "Boeing 737-800", 17, 0, 105, 3350));
        list.add(new RouteTemplate("Air India", "AI", 609, "BOM", "BLR", "Airbus A320neo", 19, 0, 110, 4100));

        list.add(new RouteTemplate("IndiGo", "6E", 205, "BLR", "BOM", "Airbus A320ceo", 8, 0, 110, 3950));
        list.add(new RouteTemplate("Akasa Air", "QP", 1103, "BLR", "BOM", "Boeing 737 MAX 8", 16, 15, 105, 3400));
        list.add(new RouteTemplate("Air India", "AI", 610, "BLR", "BOM", "Airbus A320neo", 21, 15, 110, 4100));

        // DEL <-> GOI/GOX & GOI <-> DEL (Goa)
        list.add(new RouteTemplate("IndiGo", "6E", 101, "DEL", "GOI", "Airbus A320neo", 10, 30, 150, 5450));
        list.add(new RouteTemplate("Air India", "AI", 860, "DEL", "GOI", "Airbus A320ceo", 14, 45, 155, 5600));
        list.add(new RouteTemplate("Air India Express", "IX", 331, "DEL", "GOI", "Boeing 737 MAX 8", 18, 15, 150, 5200));

        list.add(new RouteTemplate("IndiGo", "6E", 102, "GOI", "DEL", "Airbus A320neo", 13, 45, 150, 5450));
        list.add(new RouteTemplate("Air India", "AI", 861, "GOI", "DEL", "Airbus A320ceo", 17, 30, 155, 5600));

        // BOM <-> GOI & GOI <-> BOM
        list.add(new RouteTemplate("Air India", "AI", 812, "BOM", "GOI", "Airbus A320neo", 12, 0, 75, 3250));
        list.add(new RouteTemplate("IndiGo", "6E", 442, "BOM", "GOI", "Airbus A320ceo", 16, 30, 70, 2950));
        list.add(new RouteTemplate("Akasa Air", "QP", 1501, "BOM", "GOI", "Boeing 737 MAX 8", 19, 30, 70, 2900));
        list.add(new RouteTemplate("Air India", "AI", 813, "GOI", "BOM", "Airbus A320neo", 14, 15, 75, 3250));
        list.add(new RouteTemplate("IndiGo", "6E", 443, "GOI", "BOM", "Airbus A320ceo", 18, 45, 70, 2950));

        // DEL <-> SXR & SXR <-> DEL (Srinagar Kashmir)
        list.add(new RouteTemplate("IndiGo", "6E", 344, "DEL", "SXR", "Airbus A320neo", 8, 45, 85, 4650));
        list.add(new RouteTemplate("Air India", "AI", 825, "DEL", "SXR", "Airbus A320ceo", 13, 0, 90, 4850));
        list.add(new RouteTemplate("SpiceJet", "SG", 141, "DEL", "SXR", "Boeing 737-800", 16, 20, 85, 4250));

        list.add(new RouteTemplate("IndiGo", "6E", 345, "SXR", "DEL", "Airbus A320neo", 11, 0, 85, 4650));
        list.add(new RouteTemplate("Air India", "AI", 826, "SXR", "DEL", "Airbus A320ceo", 15, 15, 90, 4850));

        // DEL <-> COK & BOM <-> COK (Kochi Kerala)
        list.add(new RouteTemplate("Air India", "AI", 409, "DEL", "COK", "Airbus A321neo", 13, 15, 190, 6250));
        list.add(new RouteTemplate("IndiGo", "6E", 389, "DEL", "COK", "Airbus A321neo", 17, 30, 185, 5950));
        list.add(new RouteTemplate("Air India", "AI", 410, "COK", "DEL", "Airbus A321neo", 17, 15, 190, 6250));
        list.add(new RouteTemplate("IndiGo", "6E", 722, "BOM", "COK", "Airbus A320neo", 16, 0, 115, 3950));
        list.add(new RouteTemplate("IndiGo", "6E", 723, "COK", "BOM", "Airbus A320neo", 19, 0, 115, 3950));

        // Regional & Tier-2 Routes (ATR 72-600, A320neo, A320ceo, B737-800)
        list.add(new RouteTemplate("Alliance Air", "9I", 501, "DEL", "IXC", "ATR 72-600", 7, 0, 55, 2350));
        list.add(new RouteTemplate("Alliance Air", "9I", 502, "IXC", "DEL", "ATR 72-600", 9, 0, 55, 2350));
        list.add(new RouteTemplate("IndiGo", "6E", 289, "BOM", "UDR", "ATR 72-600", 11, 45, 80, 3650));
        list.add(new RouteTemplate("IndiGo", "6E", 290, "UDR", "BOM", "ATR 72-600", 14, 15, 80, 3650));
        list.add(new RouteTemplate("Alliance Air", "9I", 401, "DEL", "JAI", "ATR 72-600", 8, 30, 60, 2450));
        list.add(new RouteTemplate("Alliance Air", "9I", 402, "JAI", "DEL", "ATR 72-600", 10, 30, 60, 2450));
        list.add(new RouteTemplate("Air India", "AI", 491, "DEL", "JAI", "Airbus A320ceo", 14, 0, 55, 2950));

        // Other Major Metros (HYD, MAA, CCU, VNS, IXZ, AMD, PNQ, ATQ, GAU, TRV)
        list.add(new RouteTemplate("IndiGo", "6E", 551, "DEL", "HYD", "Airbus A320neo", 7, 15, 135, 4250));
        list.add(new RouteTemplate("IndiGo", "6E", 552, "HYD", "DEL", "Airbus A320neo", 10, 30, 135, 4250));
        list.add(new RouteTemplate("IndiGo", "6E", 678, "DEL", "MAA", "Airbus A321neo", 10, 0, 170, 4950));
        list.add(new RouteTemplate("IndiGo", "6E", 679, "MAA", "DEL", "Airbus A321neo", 14, 0, 170, 4950));
        list.add(new RouteTemplate("SpiceJet", "SG", 303, "DEL", "CCU", "Boeing 737-800", 15, 20, 135, 3950));
        list.add(new RouteTemplate("SpiceJet", "SG", 304, "CCU", "DEL", "Boeing 737-800", 18, 15, 135, 3950));
        list.add(new RouteTemplate("Air India", "AI", 406, "DEL", "VNS", "Airbus A320neo", 14, 0, 80, 3450));
        list.add(new RouteTemplate("Air India", "AI", 407, "VNS", "DEL", "Airbus A320neo", 16, 30, 80, 3450));
        list.add(new RouteTemplate("IndiGo", "6E", 885, "DEL", "IXZ", "Airbus A321neo", 5, 45, 310, 8950));
        list.add(new RouteTemplate("IndiGo", "6E", 886, "IXZ", "DEL", "Airbus A321neo", 11, 30, 310, 8950));
        list.add(new RouteTemplate("Air India", "AI", 18, "DEL", "AMD", "Airbus A320ceo", 18, 30, 90, 3550));
        list.add(new RouteTemplate("Air India", "AI", 19, "AMD", "DEL", "Airbus A320ceo", 21, 0, 90, 3550));
        list.add(new RouteTemplate("IndiGo", "6E", 331, "DEL", "PNQ", "Airbus A320neo", 8, 30, 125, 3850));
        list.add(new RouteTemplate("IndiGo", "6E", 332, "PNQ", "DEL", "Airbus A320neo", 11, 45, 125, 3850));
        list.add(new RouteTemplate("Air India", "AI", 453, "DEL", "ATQ", "Airbus A320ceo", 7, 0, 65, 2750));
        list.add(new RouteTemplate("Air India", "AI", 454, "ATQ", "DEL", "Airbus A320ceo", 9, 30, 65, 2750));
        list.add(new RouteTemplate("IndiGo", "6E", 611, "DEL", "GAU", "Airbus A321neo", 9, 15, 145, 4650));
        list.add(new RouteTemplate("IndiGo", "6E", 612, "GAU", "DEL", "Airbus A321neo", 12, 45, 145, 4650));
        list.add(new RouteTemplate("Air India", "AI", 511, "DEL", "TRV", "Airbus A320neo", 6, 15, 200, 6450));
        list.add(new RouteTemplate("Air India", "AI", 512, "TRV", "DEL", "Airbus A320neo", 10, 30, 200, 6450));

        // ─────────────────────────────────────────────────────────────────────────
        // 2. INTERNATIONAL FLAGSHIP ROUTES
        // ─────────────────────────────────────────────────────────────────────────

        // Tropical Escapes (Bali, Maldives, Bangkok, Phuket)
        list.add(new RouteTemplate("Garuda Indonesia", "GA", 850, "DEL", "DPS", "Airbus A330-300", 23, 30, 540, 16999));
        list.add(new RouteTemplate("Air India", "AI", 214, "BOM", "DPS", "Boeing 787-9 Dreamliner", 22, 15, 525, 17499));
        list.add(new RouteTemplate("Garuda Indonesia", "GA", 851, "DPS", "DEL", "Airbus A330-300", 11, 0, 540, 16999));
        list.add(new RouteTemplate("Air India", "AI", 215, "DPS", "BOM", "Boeing 787-9 Dreamliner", 9, 30, 525, 17499));

        list.add(new RouteTemplate("IndiGo", "6E", 1121, "BOM", "MLE", "Airbus A321neo", 10, 15, 165, 11999));
        list.add(new RouteTemplate("Air India", "AI", 263, "DEL", "MLE", "Airbus A320neo", 11, 30, 240, 14299));
        list.add(new RouteTemplate("IndiGo", "6E", 1122, "MLE", "BOM", "Airbus A321neo", 14, 30, 165, 11999));
        list.add(new RouteTemplate("Air India", "AI", 264, "MLE", "DEL", "Airbus A320neo", 16, 45, 240, 14299));

        list.add(new RouteTemplate("Thai Airways", "TG", 316, "DEL", "BKK", "Boeing 777-300ER", 23, 55, 260, 13850));
        list.add(new RouteTemplate("Thai Airways", "TG", 317, "BKK", "DEL", "Boeing 777-300ER", 7, 15, 260, 13850));
        list.add(new RouteTemplate("IndiGo", "6E", 1073, "BOM", "HKT", "Airbus A321neo", 1, 30, 285, 12950));
        list.add(new RouteTemplate("IndiGo", "6E", 1074, "HKT", "BOM", "Airbus A321neo", 8, 0, 285, 12950));

        // Middle East Hubs (Dubai, Abu Dhabi, Doha)
        list.add(new RouteTemplate("Emirates", "EK", 500, "BOM", "DXB", "Boeing 777-300ER", 19, 30, 195, 21500));
        list.add(new RouteTemplate("Emirates", "EK", 501, "DXB", "BOM", "Boeing 777-300ER", 2, 45, 195, 21500));
        list.add(new RouteTemplate("Emirates", "EK", 512, "DEL", "DXB", "Airbus A380-800", 21, 50, 230, 24500));
        list.add(new RouteTemplate("Emirates", "EK", 513, "DXB", "DEL", "Airbus A380-800", 4, 15, 230, 24500));
        list.add(new RouteTemplate("Etihad Airways", "EY", 205, "BOM", "AUH", "Boeing 787-9", 20, 15, 210, 22800));
        list.add(new RouteTemplate("Etihad Airways", "EY", 206, "AUH", "BOM", "Boeing 787-9", 3, 0, 210, 22800));
        list.add(new RouteTemplate("Qatar Airways", "QR", 571, "DEL", "DOH", "Boeing 777-300ER", 3, 50, 250, 28500));
        list.add(new RouteTemplate("Qatar Airways", "QR", 572, "DOH", "DEL", "Boeing 777-300ER", 10, 30, 250, 28500));

        // Southeast Asia & Far East (Singapore, KL, Tokyo)
        list.add(new RouteTemplate("Singapore Airlines", "SQ", 402, "DEL", "SIN", "Airbus A350-900", 21, 55, 335, 32500));
        list.add(new RouteTemplate("Singapore Airlines", "SQ", 401, "SIN", "DEL", "Airbus A350-900", 8, 30, 335, 32500));
        list.add(new RouteTemplate("Singapore Airlines", "SQ", 423, "BOM", "SIN", "Boeing 787-10", 23, 40, 320, 29800));
        list.add(new RouteTemplate("Singapore Airlines", "SQ", 424, "SIN", "BOM", "Boeing 787-10", 7, 0, 320, 29800));
        list.add(new RouteTemplate("Air India", "AI", 380, "DEL", "SIN", "Boeing 787-9 Dreamliner", 14, 0, 330, 24500));
        list.add(new RouteTemplate("Air India", "AI", 381, "SIN", "DEL", "Boeing 787-9 Dreamliner", 22, 30, 330, 24500));
        list.add(new RouteTemplate("Japan Airlines", "JL", 740, "DEL", "HND", "Boeing 787-9", 20, 0, 485, 46500));
        list.add(new RouteTemplate("Japan Airlines", "JL", 749, "HND", "DEL", "Boeing 787-9", 10, 0, 485, 46500));

        // Europe, Americas & Australia (London, Paris, Frankfurt, New York, San Francisco, Sydney)
        list.add(new RouteTemplate("British Airways", "BA", 112, "DEL", "LHR", "Boeing 787-9", 3, 45, 555, 52500));
        list.add(new RouteTemplate("British Airways", "BA", 113, "LHR", "DEL", "Boeing 787-9", 14, 0, 555, 52500));
        list.add(new RouteTemplate("Air India", "AI", 112, "BOM", "LHR", "Airbus A350-900", 6, 45, 565, 48500));
        list.add(new RouteTemplate("Air India", "AI", 113, "LHR", "BOM", "Airbus A350-900", 17, 30, 565, 48500));
        list.add(new RouteTemplate("Air France", "AF", 225, "DEL", "CDG", "Boeing 777-300ER", 0, 35, 550, 54500));
        list.add(new RouteTemplate("Air France", "AF", 226, "CDG", "DEL", "Boeing 777-300ER", 10, 30, 550, 54500));
        list.add(new RouteTemplate("Lufthansa", "LH", 760, "DEL", "FRA", "Boeing 747-8", 2, 50, 520, 51500));
        list.add(new RouteTemplate("Lufthansa", "LH", 761, "FRA", "DEL", "Boeing 747-8", 12, 15, 520, 51500));
        list.add(new RouteTemplate("Air India", "AI", 105, "DEL", "JFK", "Boeing 787-9 Dreamliner", 2, 0, 940, 72500));
        list.add(new RouteTemplate("Air India", "AI", 106, "JFK", "DEL", "Boeing 787-9 Dreamliner", 12, 30, 940, 72500));
        list.add(new RouteTemplate("Air India", "AI", 173, "DEL", "SFO", "Airbus A350-900", 4, 0, 970, 78500));
        list.add(new RouteTemplate("Air India", "AI", 174, "SFO", "DEL", "Airbus A350-900", 15, 0, 970, 78500));
        list.add(new RouteTemplate("Qantas", "QF", 68, "DEL", "SYD", "Airbus A330-200", 18, 10, 740, 68500));
        list.add(new RouteTemplate("Qantas", "QF", 69, "SYD", "DEL", "Airbus A330-200", 8, 30, 740, 68500));

        return list;
    }

    public static Flight buildFlight(String flightNumber, String airline, String airlineCode,
                                     String origCode, String destCode, String aircraftModel,
                                     Instant depTime, int durationMins, double basePriceVal,
                                     FlightStatus status, Integer delayMins, String delayReason) {
        Instant arrTime = depTime.plus(durationMins, ChronoUnit.MINUTES);
        BigDecimal basePrice = BigDecimal.valueOf(basePriceVal);

        AirportInfo orig = buildAirportInfo(origCode, true);
        AirportInfo dest = buildAirportInfo(destCode, false);

        int totalSeats = 180;
        int availSeats = 142;

        Set<CabinClass> cabinClasses = new HashSet<>();
        cabinClasses.add(CabinClass.ECONOMY);
        cabinClasses.add(CabinClass.PREMIUM_ECONOMY);
        cabinClasses.add(CabinClass.BUSINESS);

        List<CabinInventory> inventories = new ArrayList<>();
        inventories.add(new CabinInventory(CabinClass.ECONOMY, 140, 115, basePrice, basePrice.multiply(BigDecimal.valueOf(0.05)), basePrice.multiply(BigDecimal.valueOf(0.03)), null));
        inventories.add(new CabinInventory(CabinClass.PREMIUM_ECONOMY, 24, 18, basePrice.multiply(BigDecimal.valueOf(1.5)), basePrice.multiply(BigDecimal.valueOf(0.07)), basePrice.multiply(BigDecimal.valueOf(0.04)), null));
        inventories.add(new CabinInventory(CabinClass.BUSINESS, 16, 9, basePrice.multiply(BigDecimal.valueOf(2.8)), basePrice.multiply(BigDecimal.valueOf(0.12)), basePrice.multiply(BigDecimal.valueOf(0.06)), null));

        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setAirline(airline);
        flight.setAirlineCode(airlineCode);
        flight.setDepartureAirport(orig);
        flight.setArrivalAirport(dest);
        flight.setDepartureTime(depTime);
        flight.setArrivalTime(arrTime);
        flight.setDurationMinutes(durationMins);
        flight.setAircraftModel(aircraftModel);
        flight.setBasePrice(basePrice);
        flight.setTotalSeats(totalSeats);
        flight.setAvailableSeats(availSeats);
        flight.setCabinClasses(cabinClasses);
        flight.setCabinInventories(inventories);
        flight.setStatus(status != null ? status : FlightStatus.SCHEDULED);
        flight.setDelayMinutes(delayMins);
        flight.setDelayReason(delayReason);
        if (delayMins != null && delayMins > 0) {
            flight.setRevisedDepartureTime(depTime.plus(delayMins, ChronoUnit.MINUTES));
            flight.setEstimatedArrival(arrTime.plus(delayMins, ChronoUnit.MINUTES));
        } else {
            flight.setRevisedDepartureTime(depTime);
            flight.setEstimatedArrival(arrTime);
        }
        flight.setLastStatusUpdated(Instant.now());
        flight.setActive(true);

        return flight;
    }

    public static AirportInfo buildAirportInfo(String code, boolean isDeparture) {
        String clean = code != null ? code.toUpperCase().trim() : "DEL";
        return switch (clean) {
            // Indian Major Metros
            case "BOM" -> new AirportInfo("BOM", "Chhatrapati Shivaji Maharaj International Airport", "Mumbai", "India", "T2", isDeparture ? "Gate 42" : "Gate 14");
            case "BLR" -> new AirportInfo("BLR", "Kempegowda International Airport", "Bengaluru", "India", "T2", isDeparture ? "Gate 18" : "Gate 06");
            case "MAA" -> new AirportInfo("MAA", "Chennai International Airport", "Chennai", "India", "T1", isDeparture ? "Gate 08" : "Gate 02");
            case "CCU" -> new AirportInfo("CCU", "Netaji Subhash Chandra Bose International Airport", "Kolkata", "India", "T2", isDeparture ? "Gate 12" : "Gate 05");
            case "HYD" -> new AirportInfo("HYD", "Rajiv Gandhi International Airport", "Hyderabad", "India", "T1", isDeparture ? "Gate 22" : "Gate 09");

            // Indian Leisure & Regional Hotspots
            case "GOI" -> new AirportInfo("GOI", "Dabolim Airport", "Goa", "India", "T1", isDeparture ? "Gate 04" : "Gate 01");
            case "GOX" -> new AirportInfo("GOX", "Manohar International Airport (Mopa)", "Goa", "India", "T1", isDeparture ? "Gate 03" : "Gate 02");
            case "SXR" -> new AirportInfo("SXR", "Sheikh ul-Alam International Airport", "Srinagar (Kashmir)", "India", "T1", isDeparture ? "Gate 02" : "Gate 01");
            case "COK" -> new AirportInfo("COK", "Cochin International Airport", "Kochi (Kerala)", "India", "T3", isDeparture ? "Gate 07" : "Gate 03");
            case "JAI" -> new AirportInfo("JAI", "Jaipur International Airport", "Jaipur", "India", "T2", isDeparture ? "Gate 05" : "Gate 02");
            case "UDR" -> new AirportInfo("UDR", "Maharana Pratap Airport", "Udaipur", "India", "T1", isDeparture ? "Gate 02" : "Gate 01");
            case "VNS" -> new AirportInfo("VNS", "Lal Bahadur Shastri International Airport", "Varanasi", "India", "T1", isDeparture ? "Gate 04" : "Gate 02");
            case "IXZ" -> new AirportInfo("IXZ", "Veer Savarkar International Airport", "Port Blair (Andaman)", "India", "T2", isDeparture ? "Gate 03" : "Gate 01");
            case "AMD" -> new AirportInfo("AMD", "Sardar Vallabhbhai Patel International Airport", "Ahmedabad", "India", "T2", isDeparture ? "Gate 09" : "Gate 04");
            case "PNQ" -> new AirportInfo("PNQ", "Pune International Airport", "Pune", "India", "T1", isDeparture ? "Gate 06" : "Gate 02");
            case "ATQ" -> new AirportInfo("ATQ", "Sri Guru Ram Dass Jee International Airport", "Amritsar", "India", "T1", isDeparture ? "Gate 03" : "Gate 01");
            case "GAU" -> new AirportInfo("GAU", "Lokpriya Gopinath Bordoloi International Airport", "Guwahati", "India", "T1", isDeparture ? "Gate 04" : "Gate 02");
            case "IXC" -> new AirportInfo("IXC", "Shaheed Bhagat Singh International Airport", "Chandigarh", "India", "T1", isDeparture ? "Gate 05" : "Gate 01");
            case "TRV" -> new AirportInfo("TRV", "Thiruvananthapuram International Airport", "Thiruvananthapuram", "India", "T2", isDeparture ? "Gate 03" : "Gate 01");

            // Tropical & Vacation Flagships
            case "DPS" -> new AirportInfo("DPS", "Ngurah Rai International Airport", "Bali", "Indonesia", "International", isDeparture ? "Gate 06" : "Gate 02");
            case "MLE" -> new AirportInfo("MLE", "Velana International Airport", "Malé", "Maldives", "T1", isDeparture ? "Gate 04" : "Gate 01");
            case "BKK" -> new AirportInfo("BKK", "Suvarnabhumi Airport", "Bangkok", "Thailand", "Main", isDeparture ? "Gate E4" : "Gate D2");
            case "HKT" -> new AirportInfo("HKT", "Phuket International Airport", "Phuket", "Thailand", "T2", isDeparture ? "Gate 11" : "Gate 05");

            // Middle East
            case "DXB" -> new AirportInfo("DXB", "Dubai International Airport", "Dubai", "United Arab Emirates", "T3", isDeparture ? "Gate B12" : "Gate A04");
            case "AUH" -> new AirportInfo("AUH", "Zayed International Airport", "Abu Dhabi", "United Arab Emirates", "T1", isDeparture ? "Gate 15" : "Gate 07");
            case "DOH" -> new AirportInfo("DOH", "Hamad International Airport", "Doha", "Qatar", "T1", isDeparture ? "Gate C24" : "Gate D10");

            // Southeast Asia & Far East
            case "SIN" -> new AirportInfo("SIN", "Singapore Changi Airport", "Singapore", "Singapore", "T3", isDeparture ? "Gate A16" : "Gate B08");
            case "KUL" -> new AirportInfo("KUL", "Kuala Lumpur International Airport", "Kuala Lumpur", "Malaysia", "KLIA1", isDeparture ? "Gate C12" : "Gate C02");
            case "HND" -> new AirportInfo("HND", "Tokyo Haneda Airport", "Tokyo", "Japan", "T3", isDeparture ? "Gate 112" : "Gate 105");
            case "NRT" -> new AirportInfo("NRT", "Narita International Airport", "Tokyo", "Japan", "T1", isDeparture ? "Gate 24" : "Gate 18");
            case "ICN" -> new AirportInfo("ICN", "Incheon International Airport", "Seoul", "South Korea", "T2", isDeparture ? "Gate 240" : "Gate 232");

            // Europe & UK
            case "LHR" -> new AirportInfo("LHR", "London Heathrow Airport", "London", "United Kingdom", "T2", isDeparture ? "Gate B36" : "Gate C10");
            case "CDG" -> new AirportInfo("CDG", "Paris Charles de Gaulle Airport", "Paris", "France", "T2E", isDeparture ? "Gate K32" : "Gate L14");
            case "FRA" -> new AirportInfo("FRA", "Frankfurt Airport", "Frankfurt", "Germany", "T1", isDeparture ? "Gate Z25" : "Gate B10");
            case "AMS" -> new AirportInfo("AMS", "Amsterdam Airport Schiphol", "Amsterdam", "Netherlands", "Lounge 3", isDeparture ? "Gate E18" : "Gate F06");
            case "ZRH" -> new AirportInfo("ZRH", "Zurich Airport", "Zurich", "Switzerland", "Airside Center", isDeparture ? "Gate E42" : "Gate B20");

            // Americas & Australia
            case "JFK" -> new AirportInfo("JFK", "John F. Kennedy International Airport", "New York", "United States", "T4", isDeparture ? "Gate B28" : "Gate A06");
            case "SFO" -> new AirportInfo("SFO", "San Francisco International Airport", "San Francisco", "United States", "International", isDeparture ? "Gate G94" : "Gate G90");
            case "YYZ" -> new AirportInfo("YYZ", "Toronto Pearson International Airport", "Toronto", "Canada", "T1", isDeparture ? "Gate E74" : "Gate F60");
            case "SYD" -> new AirportInfo("SYD", "Sydney Kingsford Smith Airport", "Sydney", "Australia", "T1", isDeparture ? "Gate 32" : "Gate 24");

            // Default fallback
            default -> new AirportInfo("DEL", "Indira Gandhi International Airport", "New Delhi", "India", "T3", isDeparture ? "Gate 28" : "Gate 05");
        };
    }
}
