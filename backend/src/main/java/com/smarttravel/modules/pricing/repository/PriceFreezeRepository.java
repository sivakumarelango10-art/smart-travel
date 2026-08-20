package com.smarttravel.modules.pricing.repository;

import com.smarttravel.modules.pricing.model.PriceFreeze;
import com.smarttravel.modules.pricing.model.PriceFreezeStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for price freeze records.
 */
public interface PriceFreezeRepository extends MongoRepository<PriceFreeze, String> {

    List<PriceFreeze> findByUserIdOrderByCreatedAtDesc(String userId);

    List<PriceFreeze> findByUserIdAndStatus(String userId, PriceFreezeStatus status);

    Optional<PriceFreeze> findByUserIdAndFlightIdAndStatus(
            String userId, String flightId, PriceFreezeStatus status);

    /** Find expired active freezes for cleanup */
    List<PriceFreeze> findByStatusAndExpiresAtBefore(PriceFreezeStatus status, Instant now);
}
