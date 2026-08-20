package com.smarttravel.modules.pricing.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Price freeze implementation managing fare locking lifecycle.
 * Prevents duplicate active freezes for the same user+flight+cabin.
 */
@Service
public class PriceFreezeServiceImpl implements PriceFreezeService {

    private static final Logger log = LoggerFactory.getLogger(PriceFreezeServiceImpl.class);

    @Value("${app.pricing.default-freeze-duration-minutes:30}")
    private int defaultFreezeDurationMinutes;

    private final PriceFreezeRepository priceFreezeRepository;
    private final FlightRepository flightRepository;
    private final DynamicPricingService dynamicPricingService;

    public PriceFreezeServiceImpl(PriceFreezeRepository priceFreezeRepository,
                                  FlightRepository flightRepository,
                                  DynamicPricingService dynamicPricingService) {
        this.priceFreezeRepository = priceFreezeRepository;
        this.flightRepository = flightRepository;
        this.dynamicPricingService = dynamicPricingService;
    }

    @Override
    public PriceFreeze createFreeze(String userId, String flightId, CabinClass cabinClass, int passengerCount) {
        // Validate flight exists
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Check for existing ACTIVE freeze to prevent duplicates
        Optional<PriceFreeze> existingFreeze =
                priceFreezeRepository.findByUserIdAndFlightIdAndStatus(userId, flightId, PriceFreezeStatus.ACTIVE);
        if (existingFreeze.isPresent()) {
            PriceFreeze existing = existingFreeze.get();
            if (!existing.isExpired()) {
                throw new ConflictException("You already have an active price freeze for this flight (expires " +
                        existing.getExpiresAt() + "). Cancel the existing freeze to create a new one.");
            }
            // Auto-expire if past expiration
            existing.setStatus(PriceFreezeStatus.EXPIRED);
            priceFreezeRepository.save(existing);
        }

        // Find cabin inventory
        CabinInventory inventory = flight.getCabinInventories().stream()
                .filter(ci -> ci.getCabinClass() == cabinClass)
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Cabin class " + cabinClass + " is not available on flight " + flight.getFlightNumber()));

        if (inventory.getAvailableSeats() < passengerCount) {
            throw new BadRequestException("Insufficient seats available in " + cabinClass +
                    " for " + passengerCount + " passenger(s)");
        }

        // Calculate current dynamic price
        DynamicPriceBreakdown breakdown = dynamicPricingService.calculateDynamicPrice(flight, inventory, passengerCount);

        Instant expiresAt = Instant.now().plus(defaultFreezeDurationMinutes, ChronoUnit.MINUTES);

        PriceFreeze freeze = PriceFreeze.builder()
                .userId(userId)
                .flightId(flightId)
                .flightNumber(flight.getFlightNumber())
                .cabinClass(cabinClass)
                .passengerCount(passengerCount)
                .lockedPricePerPassenger(breakdown.getTotalPerPassenger())
                .lockedTotalPrice(breakdown.getGrandTotal())
                .currency("INR")
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(expiresAt)
                .basePriceAtFreeze(breakdown.getBaseFare())
                .demandAdjustmentPercentAtFreeze(breakdown.getDemandAdjustmentPercent())
                .holidayAdjustmentPercentAtFreeze(breakdown.getHolidayAdjustmentPercent())
                .seasonalAdjustmentPercentAtFreeze(breakdown.getSeasonalAdjustmentPercent())
                .build();

        PriceFreeze saved = priceFreezeRepository.save(freeze);
        log.info("Price freeze {} created for user {} on flight {} cabin {} (expires {})",
                saved.getId(), userId, flight.getFlightNumber(), cabinClass, expiresAt);
        return saved;
    }

    @Override
    public List<PriceFreeze> getUserFreezes(String userId) {
        return priceFreezeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public PriceFreeze getFreezeById(String freezeId, String userId) {
        PriceFreeze freeze = priceFreezeRepository.findById(freezeId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceFreeze", "id", freezeId));
        if (!freeze.getUserId().equals(userId)) {
            // IDOR protection
            throw new ResourceNotFoundException("PriceFreeze", "id", freezeId);
        }
        // Auto-expire if needed
        if (freeze.getStatus() == PriceFreezeStatus.ACTIVE && freeze.isExpired()) {
            freeze.setStatus(PriceFreezeStatus.EXPIRED);
            freeze = priceFreezeRepository.save(freeze);
        }
        return freeze;
    }

    @Override
    public PriceFreeze cancelFreeze(String freezeId, String userId) {
        PriceFreeze freeze = getFreezeById(freezeId, userId);
        if (freeze.getStatus() != PriceFreezeStatus.ACTIVE) {
            throw new BadRequestException("Cannot cancel a freeze with status: " + freeze.getStatus());
        }
        freeze.setStatus(PriceFreezeStatus.CANCELLED);
        return priceFreezeRepository.save(freeze);
    }

    @Override
    public PriceFreeze markAsUsed(String freezeId, String bookingId, String userId) {
        PriceFreeze freeze = priceFreezeRepository.findById(freezeId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceFreeze", "id", freezeId));

        if (!freeze.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("PriceFreeze", "id", freezeId);
        }
        if (freeze.getStatus() != PriceFreezeStatus.ACTIVE) {
            throw new BadRequestException("Price freeze is no longer active (status: " + freeze.getStatus() + ")");
        }
        if (freeze.isExpired()) {
            freeze.setStatus(PriceFreezeStatus.EXPIRED);
            priceFreezeRepository.save(freeze);
            throw new BadRequestException("Price freeze expired at " + freeze.getExpiresAt() + ". Please re-calculate fare.");
        }

        freeze.setStatus(PriceFreezeStatus.USED);
        freeze.setBookingId(bookingId);
        return priceFreezeRepository.save(freeze);
    }

    @Override
    @Scheduled(fixedDelayString = "${app.scheduler.price-freeze-cleanup-interval-ms:60000}")
    public void expireStaleFreeze() {
        List<PriceFreeze> expired = priceFreezeRepository
                .findByStatusAndExpiresAtBefore(PriceFreezeStatus.ACTIVE, Instant.now());
        if (!expired.isEmpty()) {
            for (PriceFreeze freeze : expired) {
                freeze.setStatus(PriceFreezeStatus.EXPIRED);
            }
            priceFreezeRepository.saveAll(expired);
            log.info("Expired {} stale price freezes", expired.size());
        }
    }
}
