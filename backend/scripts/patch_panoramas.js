/**
 * MongoDB Atlas Panorama Patch Script
 * Run with: mongosh "mongodb+srv://..." patch_panoramas.js
 * 
 * Replaces regular Unsplash photo URLs with real equirectangular
 * 360° panoramas from Pannellum CDN (public domain, CORS-open, 2:1 ratio)
 */

const PANO_URLS = [
  "https://pannellum.org/images/cerro-toco-0.jpg",
  "https://pannellum.org/images/alma-0.jpg",
  "https://pannellum.org/images/robber-s-roost-1.jpg"
];

const db = db.getSiblingDB("smarttravel");
const hotels = db.hotels.find({}).toArray();

print(`Found ${hotels.length} hotels to patch...`);

let hotelsPatched = 0;
let roomsPatched = 0;

hotels.forEach((hotel, i) => {
  const hotelPano = PANO_URLS[i % PANO_URLS.length];
  let modified = false;

  // Patch hotel-level virtualTour
  if (hotel.virtualTour) {
    hotel.virtualTour.panoramaUrl = hotelPano;
    hotel.virtualTour.thumbnailUrl = hotelPano;
    hotel.virtualTour.enabled = true;
    modified = true;
  }

  // Patch each room's virtualTour
  if (hotel.roomTypes && hotel.roomTypes.length > 0) {
    hotel.roomTypes.forEach((room, j) => {
      if (room.virtualTour) {
        const roomPano = PANO_URLS[(i + j + 1) % PANO_URLS.length];
        room.virtualTour.panoramaUrl = roomPano;
        room.virtualTour.thumbnailUrl = roomPano;
        room.virtualTour.enabled = true;
        roomsPatched++;
        modified = true;
      }
    });
  }

  if (modified) {
    db.hotels.replaceOne({ _id: hotel._id }, hotel);
    hotelsPatched++;
  }
});

print(`Done! Patched ${hotelsPatched} hotels and ${roomsPatched} rooms.`);
print("All hotel/room panorama URLs now use real equirectangular 360° images.");
