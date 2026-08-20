package com.smarttravel.modules.hotel.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for hotel search and room selection.
 */
@RestController
@RequestMapping("/v1/hotels")
@Tag(name = "Hotels", description = "Hotel catalog, search, and room selection")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @Operation(summary = "Search hotels by city, airport, stars, and/or price")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Hotel>>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String airportCode,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Hotel> hotels = hotelService.searchHotels(city, airportCode, minStars, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success("Hotels retrieved", hotels));
    }

    @Operation(summary = "Get hotel details by ID")
    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<Hotel>> getHotel(@PathVariable String hotelId) {
        Hotel hotel = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(ApiResponse.success("Hotel retrieved", hotel));
    }

    @Operation(summary = "Get all room types for a hotel")
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomType>>> getRoomTypes(@PathVariable String hotelId) {
        List<RoomType> rooms = hotelService.getRoomTypes(hotelId);
        return ResponseEntity.ok(ApiResponse.success("Room types retrieved", rooms));
    }

    @Operation(summary = "Get a specific room type")
    @GetMapping("/{hotelId}/rooms/{roomTypeId}")
    public ResponseEntity<ApiResponse<RoomType>> getRoomType(
            @PathVariable String hotelId,
            @PathVariable String roomTypeId) {
        RoomType room = hotelService.getRoomType(hotelId, roomTypeId);
        return ResponseEntity.ok(ApiResponse.success("Room type retrieved", room));
    }

    @Operation(summary = "Hold a room (reserve temporarily)")
    @PostMapping("/{hotelId}/rooms/{roomTypeId}/hold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RoomType>> holdRoom(
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @RequestParam(defaultValue = "1") int roomCount,
            Authentication authentication) {
        RoomType held = hotelService.holdRoom(hotelId, roomTypeId, roomCount);
        return ResponseEntity.ok(ApiResponse.success("Room held successfully", held));
    }

    @Operation(summary = "Release a held room")
    @PostMapping("/{hotelId}/rooms/{roomTypeId}/release")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> releaseRoom(
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @RequestParam(defaultValue = "1") int roomCount,
            Authentication authentication) {
        hotelService.releaseRoom(hotelId, roomTypeId, roomCount);
        return ResponseEntity.ok(ApiResponse.success("Room released successfully"));
    }
}
