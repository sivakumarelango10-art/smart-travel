package com.smarttravel.modules.hotel.repository;

import com.smarttravel.modules.hotel.model.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


/**
 * Repository for hotel catalog queries.
 */
public interface HotelRepository extends MongoRepository<Hotel, String> {

    Page<Hotel> findByActiveTrueOrderByAverageRatingDesc(Pageable pageable);

    @Query("{'address.city': {$regex: ?0, $options: 'i'}, 'active': true}")
    Page<Hotel> findByCityContainingIgnoreCaseAndActiveTrue(String city, Pageable pageable);

    Page<Hotel> findByNearestAirportCodeAndActiveTrue(String airportCode, Pageable pageable);

    Page<Hotel> findByStarRatingGreaterThanEqualAndActiveTrue(int minStars, Pageable pageable);

    @Query("{'address.city': {$regex: ?0, $options: 'i'}, 'starRating': {$gte: ?1}, 'active': true}")
    Page<Hotel> searchByCityAndStars(String city, int minStars, Pageable pageable);

    @Query("{'address.city': {$regex: ?0, $options: 'i'}, 'baseNightlyRate': {$lte: ?1}, 'active': true}")
    Page<Hotel> searchByCityAndMaxPrice(String city, java.math.BigDecimal maxPrice, Pageable pageable);

    long countByActiveTrue();
}
