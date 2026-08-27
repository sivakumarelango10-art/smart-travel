package com.smarttravel.modules.hotel.seeder;

import com.smarttravel.modules.hotel.model.*;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.model.UserPreferences;
import com.smarttravel.modules.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seeds demo hotels with rich photo galleries, default pricing rules, and initial system admin on application startup.
 */
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;

import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Component
public class HotelAndPricingDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HotelAndPricingDataSeeder.class);

    private final HotelRepository hotelRepository;
    private final DynamicPricingRuleRepository pricingRuleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FlightRepository flightRepository;
    private final FlightPriceHistoryRepository priceHistoryRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public HotelAndPricingDataSeeder(HotelRepository hotelRepository,
                                     DynamicPricingRuleRepository pricingRuleRepository,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     @org.springframework.beans.factory.annotation.Autowired(required = false) FlightRepository flightRepository,
                                     @org.springframework.beans.factory.annotation.Autowired(required = false) FlightPriceHistoryRepository priceHistoryRepository) {
        this.hotelRepository = hotelRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.flightRepository = flightRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdminUser();
        seedHotels();
        seedPricingRules();
        seedPriceHistory();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@smarttravel.com";
        String normalizedEmail = "admin@smarttravel.com";
        if (userRepository.findByEmail(adminEmail).isEmpty() && userRepository.findByNormalizedEmail(normalizedEmail).isEmpty()) {
            log.info("Seeding default Administrator account: {}", adminEmail);
            Set<Role> roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
            roles.add(Role.ROLE_ADMIN);

            User admin = User.builder()
                    .fullName("SmartTravel Administrator")
                    .firstName("SmartTravel")
                    .lastName("Admin")
                    .email(adminEmail)
                    .normalizedEmail(normalizedEmail)
                    .phoneNumber("+919876543210")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .roles(roles)
                    .accountStatus(AccountStatus.ACTIVE)
                    .active(true)
                    .emailVerified(true)
                    .preferences(new UserPreferences())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            userRepository.save(admin);
            log.info("Default Administrator account successfully created: {} / Admin@123", adminEmail);
        }
    }

    private void seedHotels() {
        log.info("Checking and seeding comprehensive hotel catalog with 360° virtual tours and multi-cabin room types...");

        List<Hotel> catalog = HotelCatalogGenerator.generateAllHotels();
        List<Hotel> toInsert = new ArrayList<>();

        for (Hotel hotel : catalog) {
            if (!hotelRepository.existsById(hotel.getId())) {
                toInsert.add(hotel);
            }
        }

        if (!toInsert.isEmpty()) {
            hotelRepository.saveAll(toInsert);
            log.info("Successfully seeded {} unique hotels into MongoDB (Total catalog size: {})", toInsert.size(), hotelRepository.count());
        } else {
            log.info("Hotel database catalog verified with {} active properties.", hotelRepository.count());
        }
    }

    private void seedPricingRules() {
        if (pricingRuleRepository.count() > 0) {
            // Self-healing: if existing rules had fractional values (< 1.0), normalize to whole percentages (e.g. 0.20 -> 20.0)
            List<DynamicPricingRule> existingRules = pricingRuleRepository.findAll();
            boolean updated = false;
            for (DynamicPricingRule r : existingRules) {
                if (r.getPercentageAdjustment() > 0 && r.getPercentageAdjustment() < 1.0) {
                    r.setPercentageAdjustment(r.getPercentageAdjustment() * 100.0);
                    updated = true;
                } else if (r.getPercentageAdjustment() < 0 && r.getPercentageAdjustment() > -1.0) {
                    r.setPercentageAdjustment(r.getPercentageAdjustment() * 100.0);
                    updated = true;
                }
            }
            if (updated) {
                pricingRuleRepository.saveAll(existingRules);
                log.info("Normalized {} dynamic pricing rules to whole percentage values", existingRules.size());
            }
            return;
        }

        log.info("Seeding default dynamic pricing rules...");

        List<DynamicPricingRule> rules = List.of(
                // Demand/Occupancy Rules
                buildDemandRule("High Occupancy Surge (>80%)", 1, 20.0, 0.80, 0.90, "20% surge when flight/hotel reaches 80% occupancy"),
                buildDemandRule("Extreme Occupancy Surge (>90%)", 2, 35.0, 0.90, 1.00, "35% surge when flight/hotel reaches 90% occupancy"),
                buildDemandRule("Low Demand Discount (<30%)", 3, -10.0, 0.00, 0.30, "10% discount when occupancy is below 30% to stimulate bookings"),

                // Holiday Peak Rules (explicitly 20% Holiday Surge)
                buildTimeBoundRule("Independence Day & Long Weekend Surge", DynamicPricingRuleType.HOLIDAY, 5, 20.0,
                        LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 20), "20% holiday peak surge"),
                buildTimeBoundRule("Diwali Festival High Season (Oct-Nov)", DynamicPricingRuleType.HOLIDAY, 15, 30.0,
                        LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 15), "30% Diwali festive demand peak"),
                buildTimeBoundRule("Year End & New Year Surge (Dec-Jan)", DynamicPricingRuleType.HOLIDAY, 20, 40.0,
                        LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 5), "40% New Year holiday travel surge"),

                // Seasonal Rules
                buildTimeBoundRule("Summer Vacation Peak (May-Jun)", DynamicPricingRuleType.SEASONAL, 10, 15.0,
                        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30), "15% summer holiday surge")
        );

        pricingRuleRepository.saveAll(rules);
        log.info("Seeded {} dynamic pricing rules", rules.size());
    }

    private void seedPriceHistory() {
        if (priceHistoryRepository == null || flightRepository == null) return;
        if (priceHistoryRepository.count() > 0) {
            log.debug("Flight price history collection non-empty, skipping seed");
            return;
        }

        log.info("Seeding realistic historical price points for key flights...");
        List<Flight> sampleFlights = flightRepository.findAll().stream()
                .filter(f -> f.getFlightNumber() != null &&
                        (f.getFlightNumber().equals("AI-101") ||
                         f.getFlightNumber().equals("6E-204") ||
                         f.getFlightNumber().equals("UK-955") ||
                         f.getFlightNumber().equals("EK-500") ||
                         f.getFlightNumber().equals("SG-8169") ||
                         f.getFlightNumber().equals("BA-112") ||
                         f.getFlightNumber().equals("SQ-402")))
                .limit(10)
                .toList();

        List<FlightPriceHistory> histories = new ArrayList<>();
        Instant now = Instant.now();

        for (Flight flight : sampleFlights) {
            if (flight.getCabinInventories() == null || flight.getCabinInventories().isEmpty()) continue;
            CabinInventory eco = flight.getCabinInventories().get(0);
            BigDecimal base = eco.getBasePrice() != null ? eco.getBasePrice() : new BigDecimal("5000.00");

            double[] occupancyPcts = { 0.25, 0.38, 0.45, 0.55, 0.68, 0.76, 0.85, 0.92 };
            double[] demandSurges = { 0.0, 0.0, 5.0, 5.0, 10.0, 10.0, 20.0, 30.0 };
            String[] reasons = {
                "Standard base fare (25% booked)",
                "Early bird discount window",
                "Moderate demand uptick (+5%)",
                "Steady weekday bookings (+5%)",
                "Weekend peak demand surge (+10%)",
                "High occupancy surge (+10%)",
                "Last-minute high demand (+20%)",
                "High demand (92% seats filled) (+30%)"
            };

            for (int i = 0; i < occupancyPcts.length; i++) {
                Instant pointTime = now.minus(14 - (i * 2), ChronoUnit.DAYS).plus(i * 3, ChronoUnit.HOURS);
                double demandPct = demandSurges[i];
                BigDecimal demandAdj = base.multiply(BigDecimal.valueOf(demandPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal adjustedBase = base.add(demandAdj).setScale(2, RoundingMode.HALF_UP);
                BigDecimal tax = adjustedBase.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal fee = new BigDecimal("150.00");
                BigDecimal finalPrice = adjustedBase.add(tax).add(fee).setScale(2, RoundingMode.HALF_UP);

                histories.add(FlightPriceHistory.builder()
                        .flightId(flight.getId())
                        .flightNumber(flight.getFlightNumber())
                        .cabinClass(eco.getCabinClass())
                        .basePrice(base)
                        .demandAdjustmentPercent(demandPct)
                        .seasonalAdjustmentPercent(0.0)
                        .holidayAdjustmentPercent(0.0)
                        .dynamicAdjustmentAmount(demandAdj)
                        .taxAmount(tax)
                        .feeAmount(fee)
                        .finalPrice(finalPrice)
                        .occupancyRatio(occupancyPcts[i])
                        .reason(reasons[i])
                        .capturedAt(pointTime)
                        .build());
            }
        }

        if (!histories.isEmpty()) {
            priceHistoryRepository.saveAll(histories);
            log.info("Seeded {} historical price snapshots across {} flights", histories.size(), sampleFlights.size());
        }
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private Hotel buildHotel(String name, String city, String airportCode, int stars,
                              BigDecimal baseRate, String addressLine1, String state,
                              List<String> imageUrls,
                              List<String> amenities, List<RoomType> roomTypes) {
        return Hotel.builder()
                .name(name)
                .address(HotelAddress.builder()
                        .line1(addressLine1)
                        .city(city)
                        .state(state)
                        .country("India")
                        .build())
                .nearestAirportCode(airportCode)
                .starRating(stars)
                .description("Luxury " + stars + "-star hotel in " + city + " offering world-class amenities, award-winning culinary dining, and exceptional personalized service.")
                .baseNightlyRate(baseRate)
                .currency("INR")
                .imageUrls(imageUrls)
                .amenities(amenities)
                .contactInfo(new HotelContactInfo("+91-11-12345678", "reservations@hotel.com", null))
                .averageRating(4.3 + (stars - 4) * 0.3)
                .totalReviews(140 + stars * 45)
                .active(true)
                .roomTypes(roomTypes)
                .build();
    }

    private RoomType buildRoom(String id, String name, RoomCategory category,
                                int nightlyRate, String bedType, int maxOccupancy, int sizeInSqFt) {
        BigDecimal rate = BigDecimal.valueOf(nightlyRate);
        BigDecimal tax = rate.multiply(new BigDecimal("0.12")).setScale(2, java.math.RoundingMode.HALF_UP);
        return RoomType.builder()
                .id(id)
                .name(name)
                .category(category)
                .description("Elegant " + name + " with modern amenities and premium furnishings.")
                .totalRooms(15)
                .availableRooms(12)
                .maxOccupancy(maxOccupancy)
                .bedType(bedType)
                .sizeInSqFt(sizeInSqFt)
                .nightlyRate(rate)
                .taxAmount(tax)
                .totalNightlyRate(rate.add(tax))
                .currency("INR")
                .amenities(List.of("Free WiFi", "Mini Bar", "Smart TV", "In-room Safe", "Coffee Maker"))
                .breakfastIncluded(category == RoomCategory.SUITE || category == RoomCategory.PRESIDENTIAL_SUITE)
                .refundable(true)
                .build();
    }

    private DynamicPricingRule buildDemandRule(String name, int priority,
                                                double pct, Double minOccupancy, Double maxOccupancy,
                                                String description) {
        return DynamicPricingRule.builder()
                .name(name)
                .type(DynamicPricingRuleType.DEMAND)
                .enabled(true)
                .priority(priority)
                .percentageAdjustment(pct)
                .minOccupancyThreshold(minOccupancy)
                .maxOccupancyThreshold(maxOccupancy)
                .description(description)
                .createdBy("SYSTEM")
                .build();
    }

    private DynamicPricingRule buildTimeBoundRule(String name, DynamicPricingRuleType type,
                                                   int priority, double pct,
                                                   LocalDate start, LocalDate end, String description) {
        return DynamicPricingRule.builder()
                .name(name)
                .type(type)
                .enabled(true)
                .priority(priority)
                .percentageAdjustment(pct)
                .startDate(start.atStartOfDay().toInstant(ZoneOffset.UTC))
                .endDate(end.atTime(23, 59, 59).toInstant(ZoneOffset.UTC))
                .description(description)
                .createdBy("SYSTEM")
                .build();
    }
}
