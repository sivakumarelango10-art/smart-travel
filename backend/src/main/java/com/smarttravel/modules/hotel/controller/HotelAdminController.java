package com.smarttravel.modules.hotel.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoint to patch panorama URLs across all hotel documents in MongoDB.
 * Called once to migrate from regular Unsplash photos to real equirectangular panoramas.
 */
@RestController
@RequestMapping("/api/v1/admin/hotels")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin - Hotels", description = "Admin hotel management and data migration operations")
public class HotelAdminController {

    private static final Logger log = LoggerFactory.getLogger(HotelAdminController.class);

    // Authentic equirectangular 360° panoramas (2:1 ratio, public-domain, CORS-open)
    private static final String PANO_SUITE   = "https://pannellum.org/images/cerro-toco-0.jpg";
    private static final String PANO_VILLA   = "https://pannellum.org/images/alma-0.jpg";
    private static final String PANO_DELUXE  = "https://pannellum.org/images/robber-s-roost-1.jpg";
    private static final String PANO_OCEAN   = "https://pannellum.org/images/cerro-toco-0.jpg";
    private static final String PANO_PALACE  = "https://pannellum.org/images/alma-0.jpg";
    private static final String PANO_LOBBY   = "https://pannellum.org/images/robber-s-roost-1.jpg";

    // Rotation through real panorama URLs for variety across 150 hotels
    private static final List<String> PANO_POOL = List.of(
        PANO_SUITE, PANO_VILLA, PANO_DELUXE, PANO_OCEAN, PANO_PALACE, PANO_LOBBY
    );

    private final MongoTemplate mongoTemplate;

    public HotelAdminController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Patches all hotel and room virtual tour panorama URLs in MongoDB with
     * real equirectangular 360° panoramas from Pannellum's public CDN.
     *
     * <p>This is a one-time migration operation. Safe to re-run.
     */
    @PostMapping("/patch-panoramas")
    @Operation(summary = "Patch hotel panorama URLs",
               description = "Replaces all hotel/room virtualTour panoramaUrls with real equirectangular 360° panoramas. One-time migration.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patchPanoramas() {
        log.info("ADMIN: Starting panorama URL migration for all hotels...");

        List<Hotel> hotels = mongoTemplate.findAll(Hotel.class);
        int hotelsPatched = 0;
        int roomsPatched = 0;

        for (int i = 0; i < hotels.size(); i++) {
            Hotel hotel = hotels.get(i);
            String hotelPano = PANO_POOL.get(i % PANO_POOL.size());
            String roomPano1 = PANO_POOL.get((i + 1) % PANO_POOL.size());
            String roomPano2 = PANO_POOL.get((i + 2) % PANO_POOL.size());
            String roomPano3 = PANO_POOL.get((i + 3) % PANO_POOL.size());

            boolean modified = false;

            // Patch hotel-level virtual tour
            if (hotel.getVirtualTour() != null) {
                hotel.getVirtualTour().setPanoramaUrl(hotelPano);
                hotel.getVirtualTour().setThumbnailUrl(hotelPano);
                hotel.getVirtualTour().setEnabled(true);
                modified = true;
            }

            // Patch each room's virtual tour
            if (hotel.getRoomTypes() != null) {
                String[] roomPanos = { roomPano1, roomPano2, roomPano3 };
                for (int j = 0; j < hotel.getRoomTypes().size(); j++) {
                    RoomType room = hotel.getRoomTypes().get(j);
                    if (room.getVirtualTour() != null) {
                        String roomPano = roomPanos[j % roomPanos.length];
                        room.getVirtualTour().setPanoramaUrl(roomPano);
                        room.getVirtualTour().setThumbnailUrl(roomPano);
                        room.getVirtualTour().setEnabled(true);
                        roomsPatched++;
                        modified = true;
                    }
                }
            }

            if (modified) {
                mongoTemplate.save(hotel);
                hotelsPatched++;
            }
        }

        log.info("ADMIN: Panorama migration complete — {} hotels, {} rooms patched", hotelsPatched, roomsPatched);

        return ResponseEntity.ok(ApiResponse.success(
            "Panorama migration complete",
            Map.of(
                "hotelsPatched", hotelsPatched,
                "roomsPatched", roomsPatched,
                "timestamp", Instant.now().toString()
            )
        ));
    }
}
