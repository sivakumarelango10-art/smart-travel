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
 * Seeds demo hotels, default pricing rules, and initial system admin on application startup.
 */
@Component
public class HotelAndPricingDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HotelAndPricingDataSeeder.class);

    private final HotelRepository hotelRepository;
    private final DynamicPricingRuleRepository pricingRuleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public HotelAndPricingDataSeeder(HotelRepository hotelRepository,
                                     DynamicPricingRuleRepository pricingRuleRepository,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        this.hotelRepository = hotelRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdminUser();
        seedHotels();
        seedPricingRules();
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
        if (hotelRepository.count() > 0) {
            log.debug("Hotels collection non-empty, skipping seed");
            return;
        }

        log.info("Seeding demo hotel data...");

        List<Hotel> hotels = List.of(
                // Delhi Hotels
                buildHotel("The Imperial New Delhi", "Delhi", "DEL", 5,
                        new BigDecimal("15000"),
                        "Janpath, New Delhi, 110001", "Delhi",
                        List.of("Pool", "Spa", "Fine Dining", "Business Center", "Valet Parking", "Concierge"),
                        List.of(
                                buildRoom("rm-01", "Superior Room", RoomCategory.STANDARD, 5000, "Queen", 2, 350),
                                buildRoom("rm-02", "Deluxe Room", RoomCategory.DELUXE, 8500, "King", 2, 450),
                                buildRoom("rm-03", "Royal Suite", RoomCategory.SUITE, 25000, "King", 4, 900)
                        )),
                buildHotel("Taj Hotel New Delhi", "Delhi", "DEL", 5,
                        new BigDecimal("12000"),
                        "1 Mansingh Road, New Delhi", "Delhi",
                        List.of("Pool", "Spa", "24hr Room Service", "Gym", "Business Lounge"),
                        List.of(
                                buildRoom("rm-01", "Standard Room", RoomCategory.STANDARD, 4500, "Twin", 2, 320),
                                buildRoom("rm-02", "Premium Room", RoomCategory.PREMIUM, 9000, "King", 2, 480),
                                buildRoom("rm-03", "Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 45000, "King", 6, 1800)
                        )),

                // Mumbai Hotels
                buildHotel("The Oberoi Mumbai", "Mumbai", "BOM", 5,
                        new BigDecimal("18000"),
                        "Nariman Point, Mumbai 400021", "Maharashtra",
                        List.of("Ocean View", "Pool", "Spa", "4 Restaurants", "Helicopter Service"),
                        List.of(
                                buildRoom("rm-01", "Luxury Room", RoomCategory.DELUXE, 9000, "King", 2, 420),
                                buildRoom("rm-02", "Premier Room", RoomCategory.PREMIUM, 14000, "King", 2, 550),
                                buildRoom("rm-03", "Ambassador Suite", RoomCategory.SUITE, 35000, "King", 4, 1200)
                        )),
                buildHotel("ITC Grand Central Mumbai", "Mumbai", "BOM", 5,
                        new BigDecimal("8500"),
                        "Dr Babasaheb Ambedkar Road, Mumbai", "Maharashtra",
                        List.of("Pool", "Gym", "Spa", "Multi-cuisine Restaurant", "Club Lounge"),
                        List.of(
                                buildRoom("rm-01", "Classic Room", RoomCategory.STANDARD, 3500, "Twin", 2, 300),
                                buildRoom("rm-02", "Superior Room", RoomCategory.DELUXE, 6500, "King", 2, 400),
                                buildRoom("rm-03", "Executive Suite", RoomCategory.EXECUTIVE_SUITE, 18000, "King", 3, 800)
                        )),

                // Bangalore Hotels
                buildHotel("The Leela Palace Bengaluru", "Bangalore", "BLR", 5,
                        new BigDecimal("14000"),
                        "23 Airport Road, Bengaluru 560008", "Karnataka",
                        List.of("Pool", "Spa", "Rooftop Bar", "Business Center", "24hr Dining"),
                        List.of(
                                buildRoom("rm-01", "Deluxe Room", RoomCategory.DELUXE, 7000, "King", 2, 450),
                                buildRoom("rm-02", "Premier Suite", RoomCategory.SUITE, 22000, "King", 4, 1000),
                                buildRoom("rm-03", "Royal Villa", RoomCategory.VILLA, 60000, "King", 6, 2500)
                        )),
                buildHotel("Marriott Whitefield Bengaluru", "Bangalore", "BLR", 4,
                        new BigDecimal("6500"),
                        "8 EPIP Zone, Whitefield, Bengaluru", "Karnataka",
                        List.of("Pool", "Gym", "Restaurant", "Business Lounge"),
                        List.of(
                                buildRoom("rm-01", "Standard Room", RoomCategory.STANDARD, 3200, "Twin", 2, 300),
                                buildRoom("rm-02", "Deluxe Room", RoomCategory.DELUXE, 5500, "King", 2, 380)
                        )),

                // Chennai Hotels
                buildHotel("The Park Chennai", "Chennai", "MAA", 5,
                        new BigDecimal("9000"),
                        "601 Anna Salai, Chennai 600006", "Tamil Nadu",
                        List.of("Rooftop Pool", "Spa", "Bar", "Fine Dining", "Business Center"),
                        List.of(
                                buildRoom("rm-01", "Classic Room", RoomCategory.STANDARD, 4000, "Queen", 2, 320),
                                buildRoom("rm-02", "Premium Room", RoomCategory.PREMIUM, 7500, "King", 2, 460),
                                buildRoom("rm-03", "The Suite", RoomCategory.SUITE, 20000, "King", 4, 900)
                        )),

                // Hyderabad Hotels
                buildHotel("Taj Falaknuma Palace", "Hyderabad", "HYD", 5,
                        new BigDecimal("50000"),
                        "Engine Bowli, Falaknuma, Hyderabad", "Telangana",
                        List.of("Palace Experience", "Pool", "Spa", "Horse Carriage", "Butler Service"),
                        List.of(
                                buildRoom("rm-01", "Palace Room", RoomCategory.DELUXE, 25000, "King", 2, 800),
                                buildRoom("rm-02", "Grand Suite", RoomCategory.SUITE, 75000, "King", 4, 2000)
                        )),

                // Goa Hotels
                buildHotel("Taj Exotica Goa", "Goa", "GOI", 5,
                        new BigDecimal("22000"),
                        "Calwaddo, Salcete, Goa 403712", "Goa",
                        List.of("Private Beach", "Pool", "Spa", "Water Sports", "Tennis Court"),
                        List.of(
                                buildRoom("rm-01", "Luxury Room", RoomCategory.DELUXE, 10000, "King", 2, 500),
                                buildRoom("rm-02", "Sea View Suite", RoomCategory.SUITE, 30000, "King", 4, 1200),
                                buildRoom("rm-03", "Beach Villa", RoomCategory.VILLA, 80000, "King", 6, 3000)
                        )),

                // Kolkata Hotels
                buildHotel("ITC Royal Bengal Kolkata", "Kolkata", "CCU", 5,
                        new BigDecimal("10000"),
                        "1, J. B. S. Haldane Avenue, Kolkata", "West Bengal",
                        List.of("Pool", "Spa", "Golf Course View", "24hr Dining", "Business Center"),
                        List.of(
                                buildRoom("rm-01", "Welcome Room", RoomCategory.STANDARD, 4200, "Twin", 2, 350),
                                buildRoom("rm-02", "Executive Room", RoomCategory.DELUXE, 7800, "King", 2, 480),
                                buildRoom("rm-03", "Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 40000, "King", 6, 2000)
                        ))
        );

        hotelRepository.saveAll(hotels);
        log.info("Seeded {} hotels", hotels.size());
    }

    private void seedPricingRules() {
        if (pricingRuleRepository.count() > 0) {
            log.debug("Pricing rules collection non-empty, skipping seed");
            return;
        }

        log.info("Seeding default dynamic pricing rules...");

        List<DynamicPricingRule> rules = List.of(
                // Demand bands
                buildDemandRule("Low Demand (0-40%)", 1, 0.0, 0.0, 0.40, "Standard pricing — low demand"),
                buildDemandRule("Moderate Demand (40-60%)", 2, 5.0, 0.40, 0.60, "Moderate demand — slight surcharge"),
                buildDemandRule("High Demand (60-80%)", 3, 10.0, 0.60, 0.80, "High demand — seat availability limited"),
                buildDemandRule("Very High Demand (80-90%)", 4, 20.0, 0.80, 0.90, "Very high demand — limited seats"),
                buildDemandRule("Near Sold Out (90-100%)", 5, 30.0, 0.90, 1.0, "Near capacity — last few seats"),

                // Seasonal — Summer (April to June)
                buildTimeBoundRule("Summer Season Surge", DynamicPricingRuleType.SEASONAL, 10, 15.0,
                        LocalDate.of(LocalDate.now().getYear(), 4, 1),
                        LocalDate.of(LocalDate.now().getYear(), 6, 30),
                        "Summer travel season surcharge"),

                // Seasonal — New Year
                buildTimeBoundRule("New Year Surge", DynamicPricingRuleType.SEASONAL, 11, 20.0,
                        LocalDate.of(LocalDate.now().getYear(), 12, 26),
                        LocalDate.of(LocalDate.now().getYear() + 1, 1, 5),
                        "New Year travel peak"),

                // Holiday surcharge examples (Diwali 2026)
                buildTimeBoundRule("Diwali 2026", DynamicPricingRuleType.HOLIDAY, 20, 15.0,
                        LocalDate.of(2026, 10, 18),
                        LocalDate.of(2026, 10, 25),
                        "Diwali festival holiday surcharge")
        );

        pricingRuleRepository.saveAll(rules);
        log.info("Seeded {} dynamic pricing rules", rules.size());
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private Hotel buildHotel(String name, String city, String airportCode, int stars,
                              BigDecimal baseRate, String addressLine1, String state,
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
                .description("Luxury " + stars + "-star hotel in " + city + " offering world-class amenities and exceptional service.")
                .baseNightlyRate(baseRate)
                .currency("INR")
                .amenities(amenities)
                .contactInfo(new HotelContactInfo("+91-11-12345678", "reservations@hotel.com", null))
                .averageRating(4.2 + (stars - 4) * 0.2)
                .totalReviews(120 + stars * 30)
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
                .amenities(List.of("Free WiFi", "Mini Bar", "Smart TV", "In-room Safe"))
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
