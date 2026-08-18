package com.smarttravel.modules.flight.service;

import com.smarttravel.modules.flight.dto.CabinSelectionResponse;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FareCalculationServiceImpl implements FareCalculationService {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.12"); // 12% Aviation GST
    private static final BigDecimal DEFAULT_ECONOMY_FEE = new BigDecimal("150.00");
    private static final BigDecimal DEFAULT_PREMIUM_ECONOMY_FEE = new BigDecimal("200.00");
    private static final BigDecimal DEFAULT_BUSINESS_FEE = new BigDecimal("300.00");
    private static final BigDecimal DEFAULT_FIRST_FEE = new BigDecimal("500.00");

    @Override
    public FareBreakdownDto calculateSinglePassengerFare(CabinInventory inventory) {
        if (inventory == null) {
            return new FareBreakdownDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "INR", 1);
        }

        BigDecimal base = inventory.getBasePrice() != null ? inventory.getBasePrice().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal tax = inventory.getTaxAmount() != null && inventory.getTaxAmount().compareTo(BigDecimal.ZERO) > 0
                ? inventory.getTaxAmount().setScale(2, RoundingMode.HALF_UP)
                : base.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = inventory.getFeeAmount() != null && inventory.getFeeAmount().compareTo(BigDecimal.ZERO) > 0
                ? inventory.getFeeAmount().setScale(2, RoundingMode.HALF_UP)
                : getDefaultFeeForCabin(inventory.getCabinClass());

        BigDecimal total = inventory.getTotalPrice() != null && inventory.getTotalPrice().compareTo(BigDecimal.ZERO) > 0
                ? inventory.getTotalPrice().setScale(2, RoundingMode.HALF_UP)
                : base.add(tax).add(fee).setScale(2, RoundingMode.HALF_UP);

        return FareBreakdownDto.builder()
                .baseFare(base)
                .taxes(tax)
                .fees(fee)
                .totalAmount(total)
                .currency("INR")
                .passengerCount(1)
                .build();
    }

    @Override
    public FareBreakdownDto calculateTotalFare(CabinInventory inventory, int passengerCount) {
        int count = Math.max(1, passengerCount);
        FareBreakdownDto singleFare = calculateSinglePassengerFare(inventory);

        BigDecimal countMultiplier = BigDecimal.valueOf(count);
        BigDecimal totalBase = singleFare.getBaseFare().multiply(countMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTaxes = singleFare.getTaxes().multiply(countMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalFees = singleFare.getFees().multiply(countMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = singleFare.getTotalAmount().multiply(countMultiplier).setScale(2, RoundingMode.HALF_UP);

        return FareBreakdownDto.builder()
                .baseFare(totalBase)
                .taxes(totalTaxes)
                .fees(totalFees)
                .totalAmount(totalAmount)
                .currency("INR")
                .passengerCount(count)
                .build();
    }

    @Override
    public CabinSelectionResponse buildCabinSelectionResponse(CabinInventory inventory, int passengerCount) {
        if (inventory == null) {
            return null;
        }

        int count = Math.max(1, passengerCount);
        FareBreakdownDto single = calculateSinglePassengerFare(inventory);
        FareBreakdownDto total = calculateTotalFare(inventory, count);

        return CabinSelectionResponse.builder()
                .cabinClass(inventory.getCabinClass())
                .availableSeats(inventory.getAvailableSeats())
                .singlePassengerFare(single)
                .totalFare(total)
                .build();
    }

    @Override
    public FareBreakdownDto calculateFare(BigDecimal basePrice, CabinClass cabinClass, int passengerCount) {
        BigDecimal base = basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0 ? basePrice : BigDecimal.ZERO;
        CabinClass cabin = cabinClass != null ? cabinClass : CabinClass.ECONOMY;
        BigDecimal tax = base.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = getDefaultFeeForCabin(cabin);
        BigDecimal total = base.add(tax).add(fee).setScale(2, RoundingMode.HALF_UP);

        CabinInventory tempInventory = CabinInventory.builder()
                .cabinClass(cabin)
                .basePrice(base)
                .taxAmount(tax)
                .feeAmount(fee)
                .totalPrice(total)
                .build();

        return calculateTotalFare(tempInventory, passengerCount);
    }

    @Override
    public List<CabinInventory> generateDefaultCabinInventories(BigDecimal basePrice, int totalSeats, int availableSeats, Set<CabinClass> cabinClasses) {
        List<CabinInventory> inventories = new ArrayList<>();
        BigDecimal base = basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0 ? basePrice : new BigDecimal("5000.00");
        int total = Math.max(1, totalSeats);
        int available = Math.max(0, availableSeats);

        Set<CabinClass> classes = (cabinClasses != null && !cabinClasses.isEmpty()) ? cabinClasses : Set.of(CabinClass.ECONOMY);

        if (classes.size() == 1 && classes.contains(CabinClass.ECONOMY)) {
            BigDecimal tax = base.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fee = DEFAULT_ECONOMY_FEE;
            BigDecimal totalPrice = base.add(tax).add(fee);

            inventories.add(CabinInventory.builder()
                    .cabinClass(CabinClass.ECONOMY)
                    .totalSeats(total)
                    .availableSeats(available)
                    .basePrice(base)
                    .taxAmount(tax)
                    .feeAmount(fee)
                    .totalPrice(totalPrice)
                    .build());
            return inventories;
        }

        // Multi-tier distribution
        for (CabinClass cabinClass : classes) {
            double capacityRatio;
            double priceMultiplier;
            BigDecimal fee;

            switch (cabinClass) {
                case BUSINESS -> {
                    capacityRatio = 0.15;
                    priceMultiplier = 3.0;
                    fee = DEFAULT_BUSINESS_FEE;
                }
                case PREMIUM_ECONOMY -> {
                    capacityRatio = 0.20;
                    priceMultiplier = 1.5;
                    fee = DEFAULT_PREMIUM_ECONOMY_FEE;
                }
                case FIRST -> {
                    capacityRatio = 0.05;
                    priceMultiplier = 5.0;
                    fee = DEFAULT_FIRST_FEE;
                }
                default -> { // ECONOMY
                    capacityRatio = 0.60;
                    priceMultiplier = 1.0;
                    fee = DEFAULT_ECONOMY_FEE;
                }
            }

            int cabinTotal = Math.max(1, (int) Math.round(total * capacityRatio));
            int cabinAvailable = Math.min(cabinTotal, Math.max(0, (int) Math.round(available * capacityRatio)));

            BigDecimal cabinBase = base.multiply(BigDecimal.valueOf(priceMultiplier)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cabinTax = cabinBase.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cabinTotalPrice = cabinBase.add(cabinTax).add(fee).setScale(2, RoundingMode.HALF_UP);

            inventories.add(CabinInventory.builder()
                    .cabinClass(cabinClass)
                    .totalSeats(cabinTotal)
                    .availableSeats(cabinAvailable)
                    .basePrice(cabinBase)
                    .taxAmount(cabinTax)
                    .feeAmount(fee)
                    .totalPrice(cabinTotalPrice)
                    .build());
        }

        return inventories;
    }

    private BigDecimal getDefaultFeeForCabin(CabinClass cabinClass) {
        if (cabinClass == null) {
            return DEFAULT_ECONOMY_FEE;
        }
        return switch (cabinClass) {
            case PREMIUM_ECONOMY -> DEFAULT_PREMIUM_ECONOMY_FEE;
            case BUSINESS -> DEFAULT_BUSINESS_FEE;
            case FIRST -> DEFAULT_FIRST_FEE;
            default -> DEFAULT_ECONOMY_FEE;
        };
    }
}
