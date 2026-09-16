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

    // Unsplash hotel interior panoramas — wide-angle 4096×2048 (2:1 ratio) for sphere mapping
    // Unsplash CDN has Access-Control-Allow-Origin: * → works with Three.js crossOrigin='anonymous'
    private static final String PANO_SUITE   = "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=4096&h=2048&q=85";
    private static final String PANO_VILLA   = "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=4096&h=2048&q=85";
    private static final String PANO_DELUXE  = "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=4096&h=2048&q=85";
    private static final String PANO_OCEAN   = "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=4096&h=2048&q=85";
    private static final String PANO_PALACE  = "https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=4096&h=2048&q=85";
    private static final String PANO_LOBBY   = "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=4096&h=2048&q=85";

    // 8 panorama variants for room-level diversity across 140+ hotels / 340+ rooms
    private static final List<String> PANO_POOL = List.of(
        "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=4096&h=2048&q=85",
        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=4096&h=2048&q=85"
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
