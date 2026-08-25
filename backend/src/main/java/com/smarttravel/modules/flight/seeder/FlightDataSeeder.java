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
 * covering 40+ premier destinations (Bali, Maldives, Dubai, London, New York, Singapore,
 * Paris, Tokyo, Zurich, Bangkok, Goa, Kashmir, Kerala, Jaipur, etc.),
 * authentic airline fleets, dynamic rolling schedules through 2027, and multi-cabin inventories.
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
            long existingCount = flightRepository.count();
            if (existingCount >= 180) {
                log.info("MongoDB flight collection verified with {} active routes.", existingCount);
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
                flightRepository.saveAll(toInsert);
                log.info("Successfully seeded {} comprehensive global flights into MongoDB.", toInsert.size());
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
        // Domestic Metros
        fleet.add(buildFlight("AI-101", "Air India", "AI", "DEL", "BOM", "Boeing 787-8 Dreamliner", now.minus(45, ChronoUnit.MINUTES), 130, 4500, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("6E-204", "IndiGo", "6E", "BOM", "BLR", "Airbus A321neo", now.minus(25, ChronoUnit.MINUTES), 105, 3800, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("UK-955", "Vistara", "UK", "DEL", "BOM", "Boeing 787-9", now.plus(20, ChronoUnit.MINUTES), 130, 5600, FlightStatus.BOARDING, null, null));
        fleet.add(buildFlight("6E-551", "IndiGo", "6E", "DEL", "HYD", "Airbus A320neo", now.plus(1, ChronoUnit.HOURS), 135, 4200, FlightStatus.DELAYED, 40, "Air traffic congestion at New Delhi"));
        fleet.add(buildFlight("AI-504", "Air India", "AI", "DEL", "BLR", "Airbus A321neo", now.plus(2, ChronoUnit.HOURS), 165, 5200, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("6E-678", "IndiGo", "6E", "DEL", "MAA", "Airbus A321neo", now.plus(3, ChronoUnit.HOURS), 170, 4900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("SG-303", "SpiceJet", "SG", "DEL", "CCU", "Boeing 737-800", now.plus(1, ChronoUnit.HOURS), 135, 3900, FlightStatus.DELAYED, 25, "Incoming aircraft turn-around"));
        fleet.add(buildFlight("QP-1102", "Akasa Air", "QP", "BOM", "BLR", "Boeing 737 MAX 8", now.plus(4, ChronoUnit.HOURS), 105, 3400, FlightStatus.ON_TIME, null, null));

        // Indian Vacation & Leisure Routes
        fleet.add(buildFlight("6E-101", "IndiGo", "6E", "DEL", "GOI", "Airbus A320neo", now.plus(2, ChronoUnit.HOURS), 150, 5400, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-812", "Air India", "AI", "BOM", "GOI", "Airbus A320neo", now.plus(1, ChronoUnit.HOURS), 75, 3200, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("6E-344", "IndiGo", "6E", "DEL", "SXR", "Airbus A320neo", now.plus(3, ChronoUnit.HOURS), 85, 4600, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-409", "Air India", "AI", "DEL", "COK", "Airbus A321neo", now.plus(4, ChronoUnit.HOURS), 190, 6200, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-722", "IndiGo", "6E", "BOM", "COK", "Airbus A320neo", now.plus(5, ChronoUnit.HOURS), 115, 3900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-491", "Air India", "AI", "DEL", "JAI", "Airbus A319", now.plus(2, ChronoUnit.HOURS), 55, 2900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-289", "IndiGo", "6E", "BOM", "UDR", "Airbus A320neo", now.plus(3, ChronoUnit.HOURS), 80, 3600, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-406", "Air India", "AI", "DEL", "VNS", "Airbus A320neo", now.plus(4, ChronoUnit.HOURS), 80, 3400, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-885", "IndiGo", "6E", "DEL", "IXZ", "Airbus A321neo", now.plus(6, ChronoUnit.HOURS), 310, 8900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-018", "Air India", "AI", "DEL", "AMD", "Airbus A320neo", now.plus(3, ChronoUnit.HOURS), 90, 3500, FlightStatus.SCHEDULED, null, null));

        // Tropical Vacations: Bali, Maldives, Phuket, Bangkok
        fleet.add(buildFlight("GA-850", "Garuda Indonesia", "GA", "DEL", "DPS", "Airbus A330-300", now.minus(90, ChronoUnit.MINUTES), 540, 16999, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("AI-214", "Air India", "AI", "BOM", "DPS", "Boeing 787-8", now.plus(7, ChronoUnit.HOURS), 525, 17499, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-1121", "IndiGo", "6E", "BOM", "MLE", "Airbus A321neo", now.minus(30, ChronoUnit.MINUTES), 165, 11999, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("AI-263", "Air India", "AI", "DEL", "MLE", "Airbus A320neo", now.plus(5, ChronoUnit.HOURS), 240, 14200, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("TG-316", "Thai Airways", "TG", "DEL", "BKK", "Boeing 777-300ER", now.plus(4, ChronoUnit.HOURS), 260, 13800, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-1073", "IndiGo", "6E", "BOM", "HKT", "Airbus A321neo", now.plus(6, ChronoUnit.HOURS), 285, 12900, FlightStatus.SCHEDULED, null, null));

        // Middle East Flagships
        fleet.add(buildFlight("EK-500", "Emirates", "EK", "BOM", "DXB", "Boeing 777-300ER", now.minus(50, ChronoUnit.MINUTES), 195, 21000, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("EK-512", "Emirates", "EK", "DEL", "DXB", "Airbus A380-800", now.plus(8, ChronoUnit.HOURS), 230, 24000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("EY-205", "Etihad Airways", "EY", "BOM", "AUH", "Boeing 787-9", now.plus(6, ChronoUnit.HOURS), 210, 22500, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("QR-571", "Qatar Airways", "QR", "DEL", "DOH", "Boeing 777-300ER", now.plus(5, ChronoUnit.HOURS), 250, 28000, FlightStatus.SCHEDULED, null, null));

        // Southeast Asia & Far East
        fleet.add(buildFlight("SQ-402", "Singapore Airlines", "SQ", "DEL", "SIN", "Airbus A380-800", now.plus(9, ChronoUnit.HOURS), 335, 32000, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("SQ-423", "Singapore Airlines", "SQ", "BOM", "SIN", "Boeing 787-10", now.minus(80, ChronoUnit.MINUTES), 320, 29500, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("UK-115", "Vistara", "UK", "BLR", "SIN", "Airbus A321neo", now.plus(4, ChronoUnit.HOURS), 275, 14299, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("MH-191", "Malaysia Airlines", "MH", "DEL", "KUL", "Airbus A330-300", now.plus(7, ChronoUnit.HOURS), 330, 18500, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("JL-740", "Japan Airlines", "JL", "DEL", "HND", "Boeing 787-9", now.minus(120, ChronoUnit.MINUTES), 485, 46000, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("NH-838", "All Nippon Airways", "NH", "BOM", "NRT", "Boeing 787-8", now.plus(11, ChronoUnit.HOURS), 510, 48500, FlightStatus.SCHEDULED, null, null));

        // Europe & UK
        fleet.add(buildFlight("BA-112", "British Airways", "BA", "DEL", "LHR", "Boeing 787-9", now.minus(110, ChronoUnit.MINUTES), 555, 52000, FlightStatus.DEPARTED, null, null));
        fleet.add(buildFlight("AI-112", "Air India", "AI", "BOM", "LHR", "Boeing 777-300ER", now.plus(10, ChronoUnit.HOURS), 565, 48000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AF-225", "Air France", "AF", "DEL", "CDG", "Boeing 777-300ER", now.plus(8, ChronoUnit.HOURS), 550, 54000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("LH-760", "Lufthansa", "LH", "DEL", "FRA", "Boeing 747-8", now.plus(9, ChronoUnit.HOURS), 520, 51000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("KL-872", "KLM Royal Dutch Airlines", "KL", "DEL", "AMS", "Boeing 787-10", now.plus(11, ChronoUnit.HOURS), 535, 49500, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("LX-155", "Swiss International", "LX", "BOM", "ZRH", "Airbus A330-300", now.plus(12, ChronoUnit.HOURS), 540, 53000, FlightStatus.SCHEDULED, null, null));

        // Americas & Australia
        fleet.add(buildFlight("AI-105", "Air India", "AI", "DEL", "JFK", "Boeing 777-300ER", now.plus(14, ChronoUnit.HOURS), 940, 72000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-173", "Air India", "AI", "DEL", "SFO", "Boeing 777-200LR", now.plus(16, ChronoUnit.HOURS), 970, 78000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AC-043", "Air Canada", "AC", "DEL", "YYZ", "Boeing 777-200LR", now.plus(15, ChronoUnit.HOURS), 935, 75000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("QF-068", "Qantas", "QF", "DEL", "SYD", "Airbus A330-200", now.plus(13, ChronoUnit.HOURS), 740, 68000, FlightStatus.SCHEDULED, null, null));

        // =========================================================================
        // 2. COMPREHENSIVE RECURRENT SCHEDULES ACROSS UPCOMING MONTHS (2026-2027)
        // =========================================================================
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate endDate = today.plusDays(90);

        // Core Route Template Catalog for Daily & Multi-Weekly Generation
        List<RouteTemplate> templates = getRouteTemplates();

        for (LocalDate cur = today.plusDays(1); !cur.isAfter(endDate); cur = cur.plusDays(2)) {
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

    private static class RouteTemplate {
        String airline;
        String airlineCode;
        int flightCodeNumber;
        String orig;
        String dest;
        String aircraft;
        int hour;
        int minute;
        int durationMins;
        double basePrice;

        RouteTemplate(String airline, String airlineCode, int flightCodeNumber,
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

    private List<RouteTemplate> getRouteTemplates() {
        List<RouteTemplate> list = new ArrayList<>();

        // Domestic Metros
        list.add(new RouteTemplate("Air India", "AI", 101, "DEL", "BOM", "Boeing 787-8", 8, 0, 130, 4650));
        list.add(new RouteTemplate("Air India", "AI", 102, "BOM", "DEL", "Airbus A350-900", 14, 30, 135, 4850));
        list.add(new RouteTemplate("IndiGo", "6E", 204, "BOM", "BLR", "Airbus A321neo", 9, 15, 105, 3850));
        list.add(new RouteTemplate("IndiGo", "6E", 205, "BLR", "BOM", "Airbus A320neo", 17, 45, 110, 3950));
        list.add(new RouteTemplate("Vistara", "UK", 955, "DEL", "BOM", "Boeing 787-9", 11, 0, 130, 5650));
        list.add(new RouteTemplate("Air India", "AI", 504, "DEL", "BLR", "Airbus A321neo", 6, 30, 165, 5200));
        list.add(new RouteTemplate("IndiGo", "6E", 551, "DEL", "HYD", "Airbus A320neo", 7, 15, 135, 4250));
        list.add(new RouteTemplate("IndiGo", "6E", 678, "DEL", "MAA", "Airbus A321neo", 10, 0, 170, 4950));
        list.add(new RouteTemplate("SpiceJet", "SG", 303, "DEL", "CCU", "Boeing 737-800", 15, 20, 135, 3950));

        // Domestic Leisure & Top Hotspots
        list.add(new RouteTemplate("IndiGo", "6E", 101, "DEL", "GOI", "Airbus A320neo", 10, 30, 150, 5450));
        list.add(new RouteTemplate("Air India", "AI", 812, "BOM", "GOI", "Airbus A320neo", 12, 0, 75, 3250));
        list.add(new RouteTemplate("IndiGo", "6E", 344, "DEL", "SXR", "Airbus A320neo", 8, 45, 85, 4650));
        list.add(new RouteTemplate("Air India", "AI", 409, "DEL", "COK", "Airbus A321neo", 13, 15, 190, 6250));
        list.add(new RouteTemplate("IndiGo", "6E", 722, "BOM", "COK", "Airbus A320neo", 16, 0, 115, 3950));
        list.add(new RouteTemplate("Air India", "AI", 491, "DEL", "JAI", "Airbus A319", 9, 30, 55, 2950));
        list.add(new RouteTemplate("IndiGo", "6E", 289, "BOM", "UDR", "Airbus A320neo", 11, 45, 80, 3650));
        list.add(new RouteTemplate("Air India", "AI", 406, "DEL", "VNS", "Airbus A320neo", 14, 0, 80, 3450));
        list.add(new RouteTemplate("IndiGo", "6E", 885, "DEL", "IXZ", "Airbus A321neo", 5, 45, 310, 8950));
        list.add(new RouteTemplate("Air India", "AI", 018, "DEL", "AMD", "Airbus A320neo", 18, 30, 90, 3550));

        // Tropical Vacations (Bali, Maldives, Phuket, Bangkok)
        list.add(new RouteTemplate("Garuda Indonesia", "GA", 850, "DEL", "DPS", "Airbus A330-300", 23, 30, 540, 16999));
        list.add(new RouteTemplate("Air India", "AI", 214, "BOM", "DPS", "Boeing 787-8", 22, 15, 525, 17499));
        list.add(new RouteTemplate("IndiGo", "6E", 1121, "BOM", "MLE", "Airbus A321neo", 10, 15, 165, 11999));
        list.add(new RouteTemplate("Air India", "AI", 263, "DEL", "MLE", "Airbus A320neo", 11, 30, 240, 14299));
        list.add(new RouteTemplate("Thai Airways", "TG", 316, "DEL", "BKK", "Boeing 777-300ER", 23, 55, 260, 13850));
        list.add(new RouteTemplate("IndiGo", "6E", 1073, "BOM", "HKT", "Airbus A321neo", 1, 30, 285, 12950));

        // Middle East Hubs
        list.add(new RouteTemplate("Emirates", "EK", 500, "BOM", "DXB", "Boeing 777-300ER", 19, 30, 195, 21500));
        list.add(new RouteTemplate("Emirates", "EK", 512, "DEL", "DXB", "Airbus A380-800", 21, 50, 230, 24500));
        list.add(new RouteTemplate("Etihad Airways", "EY", 205, "BOM", "AUH", "Boeing 787-9", 20, 15, 210, 22800));
        list.add(new RouteTemplate("Qatar Airways", "QR", 571, "DEL", "DOH", "Boeing 777-300ER", 3, 50, 250, 28500));

        // Southeast Asia & Far East
        list.add(new RouteTemplate("Singapore Airlines", "SQ", 402, "DEL", "SIN", "Airbus A380-800", 21, 55, 335, 32500));
        list.add(new RouteTemplate("Singapore Airlines", "SQ", 423, "BOM", "SIN", "Boeing 787-10", 23, 40, 320, 29800));
        list.add(new RouteTemplate("Vistara", "UK", 115, "BLR", "SIN", "Airbus A321neo", 23, 10, 275, 14299));
        list.add(new RouteTemplate("Japan Airlines", "JL", 740, "DEL", "HND", "Boeing 787-9", 20, 0, 485, 46500));

        // Europe, Americas & Australia
        list.add(new RouteTemplate("British Airways", "BA", 112, "DEL", "LHR", "Boeing 787-9", 3, 45, 555, 52500));
        list.add(new RouteTemplate("Air India", "AI", 112, "BOM", "LHR", "Boeing 777-300ER", 6, 45, 565, 48500));
        list.add(new RouteTemplate("Air France", "AF", 225, "DEL", "CDG", "Boeing 777-300ER", 0, 35, 550, 54500));
        list.add(new RouteTemplate("Lufthansa", "LH", 760, "DEL", "FRA", "Boeing 747-8", 2, 50, 520, 51500));
        list.add(new RouteTemplate("Swiss International", "LX", 155, "BOM", "ZRH", "Airbus A330-300", 1, 15, 540, 53500));
        list.add(new RouteTemplate("Air India", "AI", 105, "DEL", "JFK", "Boeing 777-300ER", 2, 0, 940, 72500));
        list.add(new RouteTemplate("Air India", "AI", 173, "DEL", "SFO", "Boeing 777-200LR", 4, 0, 970, 78500));
        list.add(new RouteTemplate("Qantas", "QF", 068, "DEL", "SYD", "Airbus A330-200", 18, 10, 740, 68500));

        return list;
    }

    private Flight buildFlight(String flightNumber, String airline, String airlineCode,
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

    private AirportInfo buildAirportInfo(String code, boolean isDeparture) {
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
