package com.smarttravel.modules.hotel.service;

import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for hotel search, retrieval, and room selection.
 */
public interface HotelService {

    /**
     * Search hotels by city with optional filters.
     */
    Page<Hotel> searchHotels(String city, String airportCode, Integer minStars,
                              BigDecimal maxPrice, Pageable pageable);

    /**
     * Get a single hotel by ID.
     */
    Hotel getHotelById(String hotelId);

    /**
     * Get available room types for a hotel.
     */
    List<RoomType> getRoomTypes(String hotelId);

    /**
     * Get a specific room type.
     */
    RoomType getRoomType(String hotelId, String roomTypeId);

    /**
     * Hold a room (decrement availableRooms atomically).
     *
     * @return The updated room type
     */
    RoomType holdRoom(String hotelId, String roomTypeId, int roomCount);

    /**
     * Release a held room (increment availableRooms atomically).
     */
    void releaseRoom(String hotelId, String roomTypeId, int roomCount);

    /**
     * Get all hotels (paginated).
     */
    Page<Hotel> getAllHotels(Pageable pageable);

    /**
     * Create or update a hotel (admin).
     */
    Hotel saveHotel(Hotel hotel);

    /**
     * Delete a hotel (admin, soft delete).
     */
    void deleteHotel(String hotelId);
}
