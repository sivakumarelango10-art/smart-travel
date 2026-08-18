package com.smarttravel.modules.flight.service;

import com.smarttravel.modules.flight.dto.CabinSelectionResponse;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FareCalculationServiceTest {

    private FareCalculationService fareCalculationService;

    @BeforeEach
    void setUp() {
        fareCalculationService = new FareCalculationServiceImpl();
    }

    @Test
    @DisplayName("Should correctly calculate single passenger fare breakdown with explicit taxes and fees")
    void testCalculateSinglePassengerFare_Explicit() {
        CabinInventory inventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(180)
                .availableSeats(100)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();

        FareBreakdownDto result = fareCalculationService.calculateSinglePassengerFare(inventory);

        assertThat(result).isNotNull();
        assertThat(result.getBaseFare()).isEqualByComparingTo("5000.00");
        assertThat(result.getTaxes()).isEqualByComparingTo("600.00");
        assertThat(result.getFees()).isEqualByComparingTo("150.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("5750.00");
        assertThat(result.getCurrency()).isEqualTo("INR");
        assertThat(result.getPassengerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should scale fare breakdown correctly for multiple passengers")
    void testCalculateTotalFare_MultiplePassengers() {
        CabinInventory inventory = CabinInventory.builder()
                .cabinClass(CabinClass.BUSINESS)
                .totalSeats(24)
                .availableSeats(10)
                .basePrice(new BigDecimal("15000.00"))
                .taxAmount(new BigDecimal("1800.00"))
                .feeAmount(new BigDecimal("300.00"))
                .totalPrice(new BigDecimal("17100.00"))
                .build();

        FareBreakdownDto result = fareCalculationService.calculateTotalFare(inventory, 3);

        assertThat(result).isNotNull();
        assertThat(result.getBaseFare()).isEqualByComparingTo("45000.00");
        assertThat(result.getTaxes()).isEqualByComparingTo("5400.00");
        assertThat(result.getFees()).isEqualByComparingTo("900.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("51300.00");
        assertThat(result.getPassengerCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should build full CabinSelectionResponse with both single and total fares")
    void testBuildCabinSelectionResponse() {
        CabinInventory inventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(180)
                .availableSeats(42)
                .basePrice(new BigDecimal("4000.00"))
                .taxAmount(new BigDecimal("480.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("4630.00"))
                .build();

        CabinSelectionResponse selection = fareCalculationService.buildCabinSelectionResponse(inventory, 2);

        assertThat(selection).isNotNull();
        assertThat(selection.getCabinClass()).isEqualTo(CabinClass.ECONOMY);
        assertThat(selection.getAvailableSeats()).isEqualTo(42);
        assertThat(selection.getSinglePassengerFare().getTotalAmount()).isEqualByComparingTo("4630.00");
        assertThat(selection.getTotalFare().getTotalAmount()).isEqualByComparingTo("9260.00");
        assertThat(selection.getTotalFare().getPassengerCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should generate default multi-tier cabin inventories for legacy flight")
    void testGenerateDefaultCabinInventories() {
        List<CabinInventory> inventories = fareCalculationService.generateDefaultCabinInventories(
                new BigDecimal("6000.00"), 200, 150, Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS)
        );

        assertThat(inventories).hasSize(2);
        assertThat(inventories).anyMatch(i -> i.getCabinClass() == CabinClass.ECONOMY && i.getBasePrice().compareTo(new BigDecimal("6000.00")) == 0);
        assertThat(inventories).anyMatch(i -> i.getCabinClass() == CabinClass.BUSINESS && i.getBasePrice().compareTo(new BigDecimal("18000.00")) == 0);
    }
}
