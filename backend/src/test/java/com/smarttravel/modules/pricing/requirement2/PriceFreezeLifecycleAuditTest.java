package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.PriceFreeze;
import com.smarttravel.modules.pricing.model.PriceFreezeStatus;
import com.smarttravel.modules.pricing.repository.PriceFreezeRepository;
import com.smarttravel.modules.pricing.service.DynamicPricingService;
import com.smarttravel.modules.pricing.service.PriceFreezeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Requirement #2 - Test Group 3: Price Freeze Lifecycle
 * Verifies freeze creation, authoritative locked price, expiration calculation, IDOR protection, conflict handling, and cleanup.
 */
@ExtendWith(MockitoExtension.class)
class PriceFreezeLifecycleAuditTest {

    @Mock
    private PriceFreezeRepository priceFreezeRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private DynamicPricingService dynamicPricingService;

    @InjectMocks
    private PriceFreezeServiceImpl priceFreezeService;

    private Flight flight;
    private CabinInventory cabinInventory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(priceFreezeService, "defaultFreezeDurationMinutes", 30);

        cabinInventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(50)
                .basePrice(new BigDecimal("5000.00"))
                .build();

        flight = Flight.builder()
                .id("fl-freeze-01")
                .flightNumber("UK-955")
                .cabinInventories(List.of(cabinInventory))
                .build();
    }

    @Test
    @DisplayName("21. Freeze creation creates active record with 30-minute expiration")
    void testCreateFreeze_Success() {
        when(flightRepository.findById("fl-freeze-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus("user-alice", "fl-freeze-01", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        DynamicPriceBreakdown breakdown = DynamicPriceBreakdown.builder()
                .flightId("fl-freeze-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(2)
                .baseFare(new BigDecimal("5000.00"))
                .totalPerPassenger(new BigDecimal("6000.00"))
                .grandTotal(new BigDecimal("12000.00"))
                .demandAdjustmentPercent(10.0)
                .build();

        when(dynamicPricingService.calculateDynamicPrice(flight, cabinInventory, 2)).thenReturn(breakdown);
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenAnswer(inv -> inv.getArgument(0));

        PriceFreeze freeze = priceFreezeService.createFreeze("user-alice", "fl-freeze-01", CabinClass.ECONOMY, 2);

        assertThat(freeze).isNotNull();
        assertThat(freeze.getUserId()).isEqualTo("user-alice");
        assertThat(freeze.getFlightId()).isEqualTo("fl-freeze-01");
        assertThat(freeze.getStatus()).isEqualTo(PriceFreezeStatus.ACTIVE);
        assertThat(freeze.getExpiresAt()).isAfter(Instant.now().plus(25, ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("22. Frozen price equals backend authoritative calculated price")
    void testFrozenPriceEqualsAuthoritativePrice() {
        when(flightRepository.findById("fl-freeze-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus("user-alice", "fl-freeze-01", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        DynamicPriceBreakdown breakdown = DynamicPriceBreakdown.builder()
                .flightId("fl-freeze-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .baseFare(new BigDecimal("5000.00"))
                .totalPerPassenger(new BigDecimal("5850.00"))
                .grandTotal(new BigDecimal("5850.00"))
                .build();

        when(dynamicPricingService.calculateDynamicPrice(flight, cabinInventory, 1)).thenReturn(breakdown);
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenAnswer(inv -> inv.getArgument(0));

        PriceFreeze freeze = priceFreezeService.createFreeze("user-alice", "fl-freeze-01", CabinClass.ECONOMY, 1);

        assertThat(freeze.getLockedPricePerPassenger()).isEqualByComparingTo("5850.00");
        assertThat(freeze.getLockedTotalPrice()).isEqualByComparingTo("5850.00");
    }

    @Test
    @DisplayName("23. Expiry date is strictly computed as now + 30 minutes")
    void testExpiryWindowCalculation() {
        when(flightRepository.findById("fl-freeze-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus("user-alice", "fl-freeze-01", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        DynamicPriceBreakdown breakdown = DynamicPriceBreakdown.builder()
                .totalPerPassenger(new BigDecimal("5000.00"))
                .grandTotal(new BigDecimal("5000.00"))
                .build();

        when(dynamicPricingService.calculateDynamicPrice(flight, cabinInventory, 1)).thenReturn(breakdown);
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now().plus(29, ChronoUnit.MINUTES);
        PriceFreeze freeze = priceFreezeService.createFreeze("user-alice", "fl-freeze-01", CabinClass.ECONOMY, 1);
        Instant after = Instant.now().plus(31, ChronoUnit.MINUTES);

        assertThat(freeze.getExpiresAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("24. Valid freeze is marked as USED upon booking completion")
    void testMarkAsUsed_Success() {
        PriceFreeze freeze = PriceFreeze.builder()
                .id("freeze-valid")
                .userId("user-alice")
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
                .build();

        when(priceFreezeRepository.findById("freeze-valid")).thenReturn(Optional.of(freeze));
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenAnswer(inv -> inv.getArgument(0));

        PriceFreeze used = priceFreezeService.markAsUsed("freeze-valid", "booking-99", "user-alice");

        assertThat(used.getStatus()).isEqualTo(PriceFreezeStatus.USED);
        assertThat(used.getBookingId()).isEqualTo("booking-99");
    }

    @Test
    @DisplayName("25. Expired freeze is rejected when attempting to mark as used")
    void testExpiredFreezeRejectedInMarkAsUsed() {
        PriceFreeze expired = PriceFreeze.builder()
                .id("freeze-expired")
                .userId("user-alice")
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .build();

        when(priceFreezeRepository.findById("freeze-expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> priceFreezeService.markAsUsed("freeze-expired", "booking-99", "user-alice"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("26. IDOR Protection: User cannot access or use another user's freeze")
    void testIdorProtection_RejectsDifferentUser() {
        PriceFreeze freeze = PriceFreeze.builder()
                .id("freeze-alice")
                .userId("user-alice")
                .status(PriceFreezeStatus.ACTIVE)
                .build();

        when(priceFreezeRepository.findById("freeze-alice")).thenReturn(Optional.of(freeze));

        assertThatThrownBy(() -> priceFreezeService.getFreezeById("freeze-alice", "user-attacker"))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> priceFreezeService.markAsUsed("freeze-alice", "booking-100", "user-attacker"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("27. Insufficient seats triggers BadRequestException on freeze creation")
    void testInsufficientSeats_RejectsFreeze() {
        CabinInventory fullInv = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(2)
                .build();
        Flight fullFlight = Flight.builder().id("fl-full").cabinInventories(List.of(fullInv)).build();

        when(flightRepository.findById("fl-full")).thenReturn(Optional.of(fullFlight));

        assertThatThrownBy(() -> priceFreezeService.createFreeze("user-alice", "fl-full", CabinClass.ECONOMY, 5))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient seats");
    }

    @Test
    @DisplayName("28. Duplicate active freeze on same flight throws ConflictException")
    void testDuplicateActiveFreezeThrowsConflict() {
        PriceFreeze active = PriceFreeze.builder()
                .id("freeze-dup")
                .userId("user-alice")
                .flightId("fl-freeze-01")
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        when(flightRepository.findById("fl-freeze-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus("user-alice", "fl-freeze-01", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> priceFreezeService.createFreeze("user-alice", "fl-freeze-01", CabinClass.ECONOMY, 1))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("29. Background cleanup task auto-expires stale price freezes")
    void testExpireStaleFreezeJob() {
        PriceFreeze stale1 = PriceFreeze.builder().id("stale1").status(PriceFreezeStatus.ACTIVE).expiresAt(Instant.now().minus(10, ChronoUnit.MINUTES)).build();
        PriceFreeze stale2 = PriceFreeze.builder().id("stale2").status(PriceFreezeStatus.ACTIVE).expiresAt(Instant.now().minus(20, ChronoUnit.MINUTES)).build();

        when(priceFreezeRepository.findByStatusAndExpiresAtBefore(eq(PriceFreezeStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of(stale1, stale2));

        priceFreezeService.expireStaleFreeze();

        assertThat(stale1.getStatus()).isEqualTo(PriceFreezeStatus.EXPIRED);
        assertThat(stale2.getStatus()).isEqualTo(PriceFreezeStatus.EXPIRED);
        verify(priceFreezeRepository).saveAll(List.of(stale1, stale2));
    }

    @Test
    @DisplayName("30. Frozen price remains immutable while freeze is valid")
    void testFrozenPriceImmutability() {
        PriceFreeze freeze = PriceFreeze.builder()
                .id("freeze-locked")
                .userId("user-alice")
                .lockedPricePerPassenger(new BigDecimal("5200.00"))
                .lockedTotalPrice(new BigDecimal("10400.00"))
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
                .build();

        when(priceFreezeRepository.findById("freeze-locked")).thenReturn(Optional.of(freeze));

        PriceFreeze fetched = priceFreezeService.getFreezeById("freeze-locked", "user-alice");

        assertThat(fetched.getLockedPricePerPassenger()).isEqualByComparingTo("5200.00");
        assertThat(fetched.getLockedTotalPrice()).isEqualByComparingTo("10400.00");
    }
}
