package com.smarttravel.modules.hotel.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Hotel service implementation with atomic room hold/release operations
 * using MongoTemplate findAndModify to prevent double-booking.
 */
@Service
public class HotelServiceImpl implements HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final MongoTemplate mongoTemplate;
    private final com.smarttravel.modules.hotel.websocket.HotelRoomWebSocketPublisher hotelRoomWebSocketPublisher;

    @org.springframework.beans.factory.annotation.Autowired
    public HotelServiceImpl(HotelRepository hotelRepository,
                            MongoTemplate mongoTemplate,
                            @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.hotel.websocket.HotelRoomWebSocketPublisher hotelRoomWebSocketPublisher) {
        this.hotelRepository = hotelRepository;
        this.mongoTemplate = mongoTemplate;
        this.hotelRoomWebSocketPublisher = hotelRoomWebSocketPublisher;
    }

    public HotelServiceImpl(HotelRepository hotelRepository, MongoTemplate mongoTemplate) {
        this(hotelRepository, mongoTemplate, null);
    }

    @Override
    public Page<Hotel> searchHotels(String city, String airportCode, Integer minStars,
                                     BigDecimal maxPrice, Pageable pageable) {
        if (airportCode != null && !airportCode.isBlank()) {
            return hotelRepository.findByNearestAirportCodeAndActiveTrue(airportCode.toUpperCase().trim(), pageable);
        }
        if (city != null && !city.isBlank()) {
            if (minStars != null) {
                return hotelRepository.searchByCityAndStars(city, minStars, pageable);
            }
            if (maxPrice != null) {
                return hotelRepository.searchByCityAndMaxPrice(city, maxPrice, pageable);
            }
            return hotelRepository.findByCityContainingIgnoreCaseAndActiveTrue(city, pageable);
        }
        return hotelRepository.findByActiveTrueOrderByAverageRatingDesc(pageable);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_HOTEL_STATIC, key = "#hotelId")
    public Hotel getHotelById(String hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));
    }

    @Override
    public List<RoomType> getRoomTypes(String hotelId) {
        Hotel hotel = getHotelById(hotelId);
        return hotel.getRoomTypes();
    }

    @Override
    public RoomType getRoomType(String hotelId, String roomTypeId) {
        Hotel hotel = getHotelById(hotelId);
        return hotel.getRoomTypes().stream()
                .filter(rt -> roomTypeId.equals(rt.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", roomTypeId));
    }

    @Override
    public RoomType holdRoom(String hotelId, String roomTypeId, int roomCount) {
        if (roomCount <= 0) throw new BadRequestException("Room count must be at least 1");

        // Atomic decrement: only update if availableRooms >= roomCount
        Query query = Query.query(
                Criteria.where("_id").is(hotelId)
                        .and("roomTypes.id").is(roomTypeId)
                        .and("roomTypes.availableRooms").gte(roomCount)
        );
        Update update = new Update().inc("roomTypes.$.availableRooms", -roomCount);
        Hotel updated = mongoTemplate.findAndModify(
                query, update,
                FindAndModifyOptions.options().returnNew(true),
                Hotel.class
        );
        if (updated == null) {
            throw new BadRequestException("Insufficient available rooms or room type not found");
        }
        RoomType heldRoom = updated.getRoomTypes().stream()
                .filter(rt -> roomTypeId.equals(rt.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", roomTypeId));

        if (hotelRoomWebSocketPublisher != null) {
            hotelRoomWebSocketPublisher.publishRoomUpdate(
                    com.smarttravel.modules.hotel.websocket.RoomAvailabilityEvent.builder()
                            .hotelId(hotelId)
                            .roomTypeId(roomTypeId)
                            .roomTypeName(heldRoom.getName())
                            .category(heldRoom.getCategory())
                            .availableRooms(heldRoom.getAvailableRooms())
                            .totalRooms(heldRoom.getTotalRooms())
                            .nightlyRate(heldRoom.getNightlyRate())
                            .action("HELD")
                            .build()
            );
        }

        return heldRoom;
    }

    @Override
    public void releaseRoom(String hotelId, String roomTypeId, int roomCount) {
        if (roomCount <= 0) return;

        Query query = Query.query(
                Criteria.where("_id").is(hotelId)
                        .and("roomTypes.id").is(roomTypeId)
        );
        Update update = new Update().inc("roomTypes.$.availableRooms", roomCount);
        Hotel updated = mongoTemplate.findAndModify(
                query, update,
                FindAndModifyOptions.options().returnNew(true),
                Hotel.class
        );
        log.info("Released {} room(s) for hotel {} room type {}", roomCount, hotelId, roomTypeId);

        if (hotelRoomWebSocketPublisher != null && updated != null) {
            updated.getRoomTypes().stream()
                    .filter(rt -> roomTypeId.equals(rt.getId()))
                    .findFirst()
                    .ifPresent(rt -> hotelRoomWebSocketPublisher.publishRoomUpdate(
                            com.smarttravel.modules.hotel.websocket.RoomAvailabilityEvent.builder()
                                    .hotelId(hotelId)
                                    .roomTypeId(roomTypeId)
                                    .roomTypeName(rt.getName())
                                    .category(rt.getCategory())
                                    .availableRooms(rt.getAvailableRooms())
                                    .totalRooms(rt.getTotalRooms())
                                    .nightlyRate(rt.getNightlyRate())
                                    .action("RELEASED")
                                    .build()
                    ));
        }
    }

    @Override
    public Page<Hotel> getAllHotels(Pageable pageable) {
        return hotelRepository.findByActiveTrueOrderByAverageRatingDesc(pageable);
    }

    @Override
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    public void deleteHotel(String hotelId) {
        Hotel hotel = getHotelById(hotelId);
        hotel.setActive(false);
        hotelRepository.save(hotel);
        log.info("Hotel {} soft-deleted", hotelId);
    }
}
