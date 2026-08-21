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
        log.info("Checking and seeding demo hotel data with distinct high-resolution luxury imagery...");

        List<Hotel> hotels = List.of(
                // Delhi Hotels
                buildHotel("The Imperial New Delhi", "Delhi", "DEL", 5,
                        new BigDecimal("15000"),
                        "Janpath, New Delhi, 110001", "Delhi",
                        List.of(
                                "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
                        ),
                        List.of("Pool", "Spa", "Fine Dining", "Business Center", "Valet Parking", "Concierge"),
                        List.of(
                                buildRoom("rm-01", "Superior Room", RoomCategory.STANDARD, 5000, "Queen", 2, 350),
                                buildRoom("rm-02", "Deluxe Room", RoomCategory.DELUXE, 8500, "King", 2, 450),
                                buildRoom("rm-03", "Royal Suite", RoomCategory.SUITE, 25000, "King", 4, 900)
                        )),
                buildHotel("Taj Hotel New Delhi", "Delhi", "DEL", 5,
                        new BigDecimal("12000"),
                        "1 Mansingh Road, New Delhi", "Delhi",
                        List.of(
                                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80"
                        ),
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
                        List.of(
                                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"
                        ),
                        List.of("Ocean View", "Pool", "Spa", "4 Restaurants", "Helicopter Service"),
                        List.of(
                                buildRoom("rm-01", "Luxury Room", RoomCategory.DELUXE, 9000, "King", 2, 420),
                                buildRoom("rm-02", "Premier Room", RoomCategory.PREMIUM, 14000, "King", 2, 550),
                                buildRoom("rm-03", "Ambassador Suite", RoomCategory.SUITE, 35000, "King", 4, 1200)
                        )),
                buildHotel("ITC Grand Central Mumbai", "Mumbai", "BOM", 5,
                        new BigDecimal("8500"),
                        "Dr Babasaheb Ambedkar Road, Mumbai", "Maharashtra",
                        List.of(
                                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=1200&q=80"
                        ),
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
                        List.of(
                                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1200&q=80"
                        ),
                        List.of("Pool", "Spa", "Rooftop Bar", "Business Center", "24hr Dining"),
                        List.of(
                                buildRoom("rm-01", "Deluxe Room", RoomCategory.DELUXE, 7000, "King", 2, 450),
                                buildRoom("rm-02", "Premier Suite", RoomCategory.SUITE, 22000, "King", 4, 1000),
                                buildRoom("rm-03", "Royal Villa", RoomCategory.VILLA, 60000, "King", 6, 2500)
                        )),
                buildHotel("Marriott Whitefield Bengaluru", "Bangalore", "BLR", 4,
                        new BigDecimal("6500"),
                        "8 EPIP Zone, Whitefield, Bengaluru", "Karnataka",
                        List.of(
                                "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"
                        ),
                        List.of("Pool", "Gym", "Restaurant", "Business Lounge"),
                        List.of(
                                buildRoom("rm-01", "Standard Room", RoomCategory.STANDARD, 3200, "Twin", 2, 300),
                                buildRoom("rm-02", "Deluxe Room", RoomCategory.DELUXE, 5500, "King", 2, 380)
                        )),

                // Chennai Hotels
                buildHotel("The Park Chennai", "Chennai", "MAA", 5,
                        new BigDecimal("9000"),
                        "601 Anna Salai, Chennai 600006", "Tamil Nadu",
                        List.of(
                                "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1200&q=80"
                        ),
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
                        List.of(
                                "https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"
                        ),
                        List.of("Palace Experience", "Pool", "Spa", "Horse Carriage", "Butler Service"),
                        List.of(
                                buildRoom("rm-01", "Palace Room", RoomCategory.DELUXE, 25000, "King", 2, 800),
                                buildRoom("rm-02", "Grand Suite", RoomCategory.SUITE, 75000, "King", 4, 2000)
                        )),

                // Goa Hotels
                buildHotel("Taj Exotica Goa", "Goa", "GOI", 5,
                        new BigDecimal("22000"),
                        "Calwaddo, Salcete, Goa 403712", "Goa",
                        List.of(
                                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"
                        ),
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
                        List.of(
                                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"
                        ),
                        List.of("Pool", "Spa", "Golf Course View", "24hr Dining", "Business Center"),
                        List.of(
                                buildRoom("rm-01", "Welcome Room", RoomCategory.STANDARD, 4200, "Twin", 2, 350),
                                buildRoom("rm-02", "Executive Room", RoomCategory.DELUXE, 7800, "King", 2, 480),
                                buildRoom("rm-03", "Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 40000, "King", 6, 2000)
                        ))
        );

        if (hotelRepository.count() == 0) {
            hotelRepository.saveAll(hotels);
            log.info("Seeded {} hotels with rich photo galleries", hotels.size());
        } else {
            // Update existing hotels with distinct photo galleries if needed
            for (Hotel hotel : hotels) {
                hotelRepository.findByName(hotel.getName()).ifPresent(existing -> {
                    existing.setImageUrls(hotel.getImageUrls());
                    hotelRepository.save(existing);
                });
            }
            log.info("Refreshed distinct photo galleries for all existing hotel properties");
        }
    }

    private void seedPricingRules() {
        if (pricingRuleRepository.count() > 0) {
            log.debug("Pricing rules collection non-empty, skipping seed");
            return;
        }

        log.info("Seeding default dynamic pricing rules...");

        List<DynamicPricingRule> rules = List.of(
                // Demand/Occupancy Rules
                buildDemandRule("High Occupancy Surge (>80%)", 1, 0.20, 0.80, 1.00, "20% surge when flight/hotel reaches 80% occupancy"),
                buildDemandRule("Extreme Occupancy Surge (>90%)", 2, 0.35, 0.90, 1.00, "35% surge when flight/hotel reaches 90% occupancy"),
                buildDemandRule("Low Demand Discount (<30%)", 3, -0.10, 0.00, 0.30, "10% discount when occupancy is below 30% to stimulate bookings"),

                // Seasonal Rules
                buildTimeBoundRule("Summer Vacation Peak (May-Jun)", DynamicPricingRuleType.SEASONAL, 10, 0.15,
                        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30), "15% summer holiday surge"),
                buildTimeBoundRule("Diwali Festival High Season (Oct-Nov)", DynamicPricingRuleType.HOLIDAY, 15, 0.30,
                        LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 15), "30% Diwali festive demand peak"),
                buildTimeBoundRule("Year End & New Year Surge (Dec-Jan)", DynamicPricingRuleType.HOLIDAY, 20, 0.40,
                        LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 5), "40% New Year holiday travel surge")
        );

        pricingRuleRepository.saveAll(rules);
        log.info("Seeded {} dynamic pricing rules", rules.size());
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
