package com.smarttravel.modules.pricing;

import com.smarttravel.common.exception.ConflictException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceFreezeServiceTest {

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
                .basePrice(new BigDecimal("4500.00"))
                .build();

        flight = Flight.builder()
                .id("flight-01")
                .flightNumber("ST-101")
                .cabinInventories(List.of(cabinInventory))
                .build();
    }

    @Test
    @DisplayName("createFreeze locks price and sets 30-minute expiration window")
    void testCreateFreeze_Success() {
        when(flightRepository.findById("flight-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus("user-1", "flight-01", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        DynamicPriceBreakdown breakdown = DynamicPriceBreakdown.builder()
                .flightId("flight-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .baseFare(new BigDecimal("4500.00"))
                .totalPerPassenger(new BigDecimal("5190.00"))
                .grandTotal(new BigDecimal("5190.00"))
                .demandAdjustmentPercent(10.0)
                .build();

        when(dynamicPricingService.calculateDynamicPrice(flight, cabinInventory, 1)).thenReturn(breakdown);
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenAnswer(inv -> {
            PriceFreeze pf = inv.getArgument(0);
            pf.setId("freeze-01");
            return pf;
        });

        PriceFreeze result = priceFreezeService.createFreeze("user-1", "flight-01", CabinClass.ECONOMY, 1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("freeze-01");
        assertThat(result.getLockedPricePerPassenger()).isEqualByComparingTo("5190.00");
        assertThat(result.getStatus()).isEqualTo(PriceFreezeStatus.ACTIVE);
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("createFreeze throws ConflictException if user already has active freeze for this flight")
    void testCreateFreeze_DuplicateConflict() {
        PriceFreeze existing = PriceFreeze.builder()
                .id("freeze-existing")
                .userId("user-1")
                .flightId("flight-01")
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(600)) // Not expired
                .build();

        when(flightRepository.findById("flight-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus("user-1", "flight-01", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> priceFreezeService.createFreeze("user-1", "flight-01", CabinClass.ECONOMY, 1))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("cancelFreeze transitions active freeze to CANCELLED")
    void testCancelFreeze() {
        PriceFreeze freeze = PriceFreeze.builder()
                .id("freeze-01")
                .userId("user-1")
                .status(PriceFreezeStatus.ACTIVE)
                .build();

        when(priceFreezeRepository.findById("freeze-01")).thenReturn(Optional.of(freeze));
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenReturn(freeze);

        PriceFreeze cancelled = priceFreezeService.cancelFreeze("freeze-01", "user-1");

        assertThat(cancelled.getStatus()).isEqualTo(PriceFreezeStatus.CANCELLED);
    }

    @Test
    @DisplayName("expireStaleFreeze cleanup job marks passed freezes as EXPIRED")
    void testExpireStaleFreeze() {
        PriceFreeze stale = PriceFreeze.builder()
                .id("freeze-stale")
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        when(priceFreezeRepository.findByStatusAndExpiresAtBefore(eq(PriceFreezeStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of(stale));

        priceFreezeService.expireStaleFreeze();

        assertThat(stale.getStatus()).isEqualTo(PriceFreezeStatus.EXPIRED);
        verify(priceFreezeRepository).saveAll(List.of(stale));
    }
}
