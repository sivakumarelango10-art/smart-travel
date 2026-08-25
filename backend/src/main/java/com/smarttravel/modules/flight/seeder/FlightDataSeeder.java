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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic Production Flight Data Seeder.
 * Populates MongoDB with rich, authentic domestic and international flight routes,
 * realistic airline fleets (Air India, IndiGo, Vistara, Emirates, Singapore Airlines, British Airways, etc.),
 * dynamic rolling schedules, and full multi-cabin inventories.
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
            if (flightRepository.count() >= 25) {
                log.info("MongoDB flight collection verified with all flagship flight schedules active.");
                return;
            }
            List<Flight> seededFlights = generateInitialFleet();
            List<Flight> toInsert = new ArrayList<>();
            for (Flight f : seededFlights) {
                if (!flightRepository.existsByFlightNumber(f.getFlightNumber())) {
                    toInsert.add(f);
                }
            }
            if (!toInsert.isEmpty()) {
                flightRepository.saveAll(toInsert);
                log.info("Successfully seeded {} flagship flights into MongoDB.", toInsert.size());
            }
        } catch (Exception ex) {
            log.warn("Flight seeding encountered non-fatal error during startup: {}", ex.getMessage());
        }
    }

    public List<Flight> generateInitialFleet() {
        List<Flight> fleet = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Air India Flagship Routes
        fleet.add(buildFlight("AI-101", "Air India", "AI", "DEL", "BOM", "Boeing 787-8 Dreamliner", now.plus(4, ChronoUnit.HOURS), 130, 4500, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("AI-102", "Air India", "AI", "BOM", "DEL", "Airbus A350-900", now.plus(6, ChronoUnit.HOURS), 135, 4800, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-504", "Air India", "AI", "DEL", "BLR", "Airbus A321neo", now.plus(2, ChronoUnit.HOURS), 165, 5200, FlightStatus.BOARDING, null, null));
        fleet.add(buildFlight("AI-505", "Air India", "AI", "BLR", "DEL", "Airbus A320neo", now.plus(8, ChronoUnit.HOURS), 160, 5100, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-112", "Air India", "AI", "DEL", "LHR", "Boeing 777-300ER", now.plus(12, ChronoUnit.HOURS), 540, 48000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("AI-995", "Air India", "AI", "DEL", "DXB", "Boeing 787-8", now.plus(5, ChronoUnit.HOURS), 240, 18500, FlightStatus.SCHEDULED, null, null));

        // 2. IndiGo High-Frequency Domestic Routes
        fleet.add(buildFlight("6E-204", "IndiGo", "6E", "BOM", "BLR", "Airbus A321neo", now.plus(3, ChronoUnit.HOURS), 105, 3800, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("6E-205", "IndiGo", "6E", "BLR", "BOM", "Airbus A320neo", now.plus(7, ChronoUnit.HOURS), 110, 3900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-551", "IndiGo", "6E", "DEL", "HYD", "Airbus A320neo", now.plus(1, ChronoUnit.HOURS), 135, 4200, FlightStatus.DELAYED, 35, "Air traffic congestion at New Delhi"));
        fleet.add(buildFlight("6E-552", "IndiGo", "6E", "HYD", "DEL", "Airbus A321neo", now.plus(9, ChronoUnit.HOURS), 130, 4100, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-678", "IndiGo", "6E", "DEL", "MAA", "Airbus A321neo", now.plus(5, ChronoUnit.HOURS), 170, 4900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-679", "IndiGo", "6E", "MAA", "DEL", "Airbus A320neo", now.plus(10, ChronoUnit.HOURS), 175, 4800, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-101", "IndiGo", "6E", "DEL", "GOI", "Airbus A320neo", now.plus(4, ChronoUnit.HOURS), 150, 5400, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("6E-102", "IndiGo", "6E", "BOM", "GOI", "Airbus A320neo", now.plus(2, ChronoUnit.HOURS), 75, 3200, FlightStatus.ON_TIME, null, null));

        // 3. Vistara Premium Routes
        fleet.add(buildFlight("UK-955", "Vistara", "UK", "DEL", "BOM", "Boeing 787-9", now.plus(3, ChronoUnit.HOURS), 130, 5600, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("UK-956", "Vistara", "UK", "BOM", "DEL", "Airbus A321neo", now.plus(8, ChronoUnit.HOURS), 135, 5800, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("UK-811", "Vistara", "UK", "DEL", "BLR", "Airbus A320neo", now.plus(6, ChronoUnit.HOURS), 160, 5900, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("UK-115", "Vistara", "UK", "DEL", "SIN", "Boeing 787-9", now.plus(14, ChronoUnit.HOURS), 345, 26000, FlightStatus.SCHEDULED, null, null));

        // 4. SpiceJet & Akasa Air
        fleet.add(buildFlight("SG-8169", "SpiceJet", "SG", "DEL", "BOM", "Boeing 737 MAX 8", now.plus(5, ChronoUnit.HOURS), 135, 3600, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("QP-1102", "Akasa Air", "QP", "BOM", "BLR", "Boeing 737 MAX 8", now.plus(4, ChronoUnit.HOURS), 105, 3400, FlightStatus.ON_TIME, null, null));
        fleet.add(buildFlight("QP-1354", "Akasa Air", "QP", "DEL", "GOI", "Boeing 737 MAX 8", now.plus(7, ChronoUnit.HOURS), 150, 4600, FlightStatus.SCHEDULED, null, null));

        // 5. Major International Hubs
        fleet.add(buildFlight("EK-500", "Emirates", "EK", "BOM", "DXB", "Boeing 777-300ER", now.plus(6, ChronoUnit.HOURS), 195, 21000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("EK-512", "Emirates", "EK", "DEL", "DXB", "Airbus A380-800", now.plus(11, ChronoUnit.HOURS), 230, 24000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("SQ-402", "Singapore Airlines", "SQ", "DEL", "SIN", "Airbus A380-800", now.plus(13, ChronoUnit.HOURS), 335, 32000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("SQ-423", "Singapore Airlines", "SQ", "BOM", "SIN", "Boeing 787-10", now.plus(15, ChronoUnit.HOURS), 320, 29500, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("BA-112", "British Airways", "BA", "DEL", "LHR", "Boeing 787-9", now.plus(16, ChronoUnit.HOURS), 555, 52000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("QR-571", "Qatar Airways", "QR", "DEL", "DOH", "Boeing 777-300ER", now.plus(8, ChronoUnit.HOURS), 250, 28000, FlightStatus.SCHEDULED, null, null));
        fleet.add(buildFlight("EY-205", "Etihad Airways", "EY", "BOM", "AUH", "Boeing 787-9", now.plus(9, ChronoUnit.HOURS), 210, 22500, FlightStatus.SCHEDULED, null, null));

        // 6. Comprehensive Future Schedule: Seed Daily / Weekly Schedules through January 31, 2027
        // Ensures full search availability across all dates (Diwali, Thanksgiving, Christmas, New Year, and January)
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
        java.time.LocalDate endDate = java.time.LocalDate.of(today.getYear() + (today.getMonthValue() >= 2 ? 1 : 0), 1, 31);
        if (endDate.isBefore(today.plusDays(30))) {
            endDate = java.time.LocalDate.of(today.getYear() + 1, 1, 31);
        }

        // Seed every 2 days for rich route density without overloading MongoDB collection size
        for (java.time.LocalDate cur = today.plusDays(1); !cur.isAfter(endDate); cur = cur.plusDays(2)) {
            String dateSuffix = cur.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

            // Morning DEL -> BOM
            Instant delBomDep = cur.atTime(8, 0).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("AI-101-" + dateSuffix, "Air India", "AI", "DEL", "BOM", "Boeing 787-8 Dreamliner", delBomDep, 130, 4650, FlightStatus.SCHEDULED, null, null));

            // Afternoon BOM -> DEL
            Instant bomDelDep = cur.atTime(14, 30).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("AI-102-" + dateSuffix, "Air India", "AI", "BOM", "DEL", "Airbus A350-900", bomDelDep, 135, 4850, FlightStatus.SCHEDULED, null, null));

            // Morning BOM -> BLR
            Instant bomBlrDep = cur.atTime(9, 15).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("6E-204-" + dateSuffix, "IndiGo", "6E", "BOM", "BLR", "Airbus A321neo", bomBlrDep, 105, 3850, FlightStatus.SCHEDULED, null, null));

            // Evening BLR -> BOM
            Instant blrBomDep = cur.atTime(17, 45).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("6E-205-" + dateSuffix, "IndiGo", "6E", "BLR", "BOM", "Airbus A320neo", blrBomDep, 110, 3950, FlightStatus.SCHEDULED, null, null));

            // Premium DEL -> BOM
            Instant vistaraDep = cur.atTime(11, 0).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("UK-955-" + dateSuffix, "Vistara", "UK", "DEL", "BOM", "Boeing 787-9", vistaraDep, 130, 5650, FlightStatus.SCHEDULED, null, null));

            // Vacation DEL -> GOI
            Instant goaDep = cur.atTime(10, 30).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("6E-101-" + dateSuffix, "IndiGo", "6E", "DEL", "GOI", "Airbus A320neo", goaDep, 150, 5450, FlightStatus.SCHEDULED, null, null));

            // International BOM -> DXB
            Instant dxbDep = cur.atTime(19, 30).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("EK-500-" + dateSuffix, "Emirates", "EK", "BOM", "DXB", "Boeing 777-300ER", dxbDep, 195, 21500, FlightStatus.SCHEDULED, null, null));

            // International DEL -> LHR
            Instant lhrDep = cur.atTime(3, 45).toInstant(java.time.ZoneOffset.UTC);
            fleet.add(buildFlight("BA-112-" + dateSuffix, "British Airways", "BA", "DEL", "LHR", "Boeing 787-9", lhrDep, 555, 52500, FlightStatus.SCHEDULED, null, null));
        }

        return fleet;
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
            case "BOM" -> new AirportInfo("BOM", "Chhatrapati Shivaji Maharaj International Airport", "Mumbai", "India", "T2", isDeparture ? "Gate 42" : "Gate 14");
            case "BLR" -> new AirportInfo("BLR", "Kempegowda International Airport", "Bengaluru", "India", "T2", isDeparture ? "Gate 18" : "Gate 06");
            case "MAA" -> new AirportInfo("MAA", "Chennai International Airport", "Chennai", "India", "T1", isDeparture ? "Gate 08" : "Gate 02");
            case "CCU" -> new AirportInfo("CCU", "Netaji Subhash Chandra Bose International Airport", "Kolkata", "India", "T2", isDeparture ? "Gate 12" : "Gate 05");
            case "HYD" -> new AirportInfo("HYD", "Rajiv Gandhi International Airport", "Hyderabad", "India", "T1", isDeparture ? "Gate 22" : "Gate 09");
            case "GOI" -> new AirportInfo("GOI", "Dabolim Airport", "Goa", "India", "T1", isDeparture ? "Gate 04" : "Gate 01");
            case "DXB" -> new AirportInfo("DXB", "Dubai International Airport", "Dubai", "United Arab Emirates", "T3", isDeparture ? "Gate B12" : "Gate A04");
            case "LHR" -> new AirportInfo("LHR", "London Heathrow Airport", "London", "United Kingdom", "T2", isDeparture ? "Gate B36" : "Gate C10");
            case "SIN" -> new AirportInfo("SIN", "Singapore Changi Airport", "Singapore", "Singapore", "T3", isDeparture ? "Gate A16" : "Gate B08");
            case "DOH" -> new AirportInfo("DOH", "Hamad International Airport", "Doha", "Qatar", "T1", isDeparture ? "Gate C24" : "Gate D10");
            case "AUH" -> new AirportInfo("AUH", "Zayed International Airport", "Abu Dhabi", "United Arab Emirates", "T1", isDeparture ? "Gate 15" : "Gate 07");
            default -> new AirportInfo("DEL", "Indira Gandhi International Airport", "New Delhi", "India", "T3", isDeparture ? "Gate 28" : "Gate 05");
        };
    }
}
