package com.smarttravel.modules.hotel.seeder;

import com.smarttravel.modules.hotel.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic Production Hotel Catalog Generator.
 * Generates 130+ realistic, unique luxury & business hotels across 32 premier destinations
 * (India: 20 cities, International: 12 world destinations) with rich image galleries,
 * multi-tier room inventories, and 360° equirectangular virtual tour panoramas.
 */
public final class HotelCatalogGenerator {

    private HotelCatalogGenerator() {}

    // Curated high-resolution equirectangular 360 panorama URLs
    public static final String PANORAMA_SUITE = "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=2400&q=85";
    public static final String PANORAMA_VILLA = "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=2400&q=85";
    public static final String PANORAMA_DELUXE = "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=2400&q=85";
    public static final String PANORAMA_OCEAN = "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=2400&q=85";
    public static final String PANORAMA_PALACE = "https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=2400&q=85";
    public static final String PANORAMA_LOBBY = "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=2400&q=85";

    public static List<Hotel> generateAllHotels() {
        List<Hotel> list = new ArrayList<>(140);

        // ==========================================
        // 1. DELHI (6 Hotels)
        // ==========================================
        list.add(createHotel("htl-del-01", "The Imperial New Delhi", "Delhi", "DEL", "Delhi", "India",
                "Janpath, Connaught Place, New Delhi 110001", 28.6219, 77.2185, 5, 4.8, 1240, 15000,
                "A historic 1930s Victorian landmark featuring museum-quality art, award-winning restaurants, and tranquil Italian marble pillared gardens.",
                List.of("Heritage Architecture", "Luxury Spa", "Outdoor Pool", "5 Fine Dining Venues", "Valet Parking", "Art Gallery", "Concierge"),
                List.of("https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Imperial Heritage Suite 360",
                List.of(
                        createRoom("rm-01", "Deco Room", RoomCategory.STANDARD, 5500, "Queen", 2, 350, PANORAMA_DELUXE),
                        createRoom("rm-02", "Imperial Heritage Deluxe", RoomCategory.DELUXE, 8800, "King", 2, 480, PANORAMA_SUITE),
                        createRoom("rm-03", "Royal Imperial Suite", RoomCategory.SUITE, 26000, "King", 4, 1100, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-del-02", "Taj Mahal Hotel New Delhi", "Delhi", "DEL", "Delhi", "India",
                "1 Mansingh Road, New Delhi 110011", 28.5997, 77.2272, 5, 4.9, 1850, 14000,
                "Iconic address in the heart of Lutyens' Delhi blending Mughal architecture, Machan multi-cuisine dining, and world-class Jiva Spa.",
                List.of("Jiva Spa", "Swimming Pool", "Machan 24/7 Dining", "Fitness Center", "Club Lounge", "High-Speed WiFi"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "Taj Mansingh Presidential 360",
                List.of(
                        createRoom("rm-01", "Luxury Room", RoomCategory.STANDARD, 4800, "Twin", 2, 360, PANORAMA_DELUXE),
                        createRoom("rm-02", "Taj Club Executive", RoomCategory.PREMIUM, 9500, "King", 2, 520, PANORAMA_SUITE),
                        createRoom("rm-03", "Grand Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 48000, "King", 6, 1950, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-del-03", "The Leela Palace New Delhi", "Delhi", "DEL", "Delhi", "India",
                "Diplomatic Enclave, Chanakyapuri, New Delhi 110023", 28.5802, 77.1884, 5, 4.9, 980, 18000,
                "Palatial splendour in Chanakyapuri Diplomatic Enclave, featuring a rooftop infinity pool, Le Cirque dining, and ESPA wellness sanctuary.",
                List.of("Rooftop Infinity Pool", "ESPA Spa", "Le Cirque Dining", "Megu Japanese", "Butler Service", "Helipad"),
                List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Leela Maharaja Suite 360",
                List.of(
                        createRoom("rm-01", "Grande Deluxe", RoomCategory.DELUXE, 7500, "King", 2, 550, PANORAMA_DELUXE),
                        createRoom("rm-02", "Royal Premier Room", RoomCategory.PREMIUM, 12500, "King", 2, 680, PANORAMA_SUITE),
                        createRoom("rm-03", "Maharaja Palace Suite", RoomCategory.SUITE, 38000, "King", 4, 1400, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-del-04", "ITC Maurya New Delhi", "Delhi", "DEL", "Delhi", "India",
                "Diplomatic Enclave, Sardar Patel Marg, New Delhi 110021", 28.5971, 77.1738, 5, 4.7, 2100, 12000,
                "Home to world-famous Bukhara restaurant, renowned for Mauryan architecture, sustainability LEED Platinum rating, and heads of state hosting.",
                List.of("Bukhara Restaurant", "Dum Pukht", "Kaya Kalp Spa", "Outdoor Pool", "Business Center"),
                List.of("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "ITC Maurya Presidential Suite 360",
                List.of(
                        createRoom("rm-01", "Executive Club", RoomCategory.STANDARD, 4200, "Twin", 2, 340, null),
                        createRoom("rm-02", "ITC One Luxury Room", RoomCategory.PREMIUM, 8200, "King", 2, 500, PANORAMA_SUITE),
                        createRoom("rm-03", "Chanakya Suite", RoomCategory.SUITE, 29000, "King", 4, 1200, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-del-05", "The Lodhi New Delhi", "Delhi", "DEL", "Delhi", "India",
                "Lodhi Road, CGO Complex, Pragati Vihar, New Delhi 110003", 28.5915, 77.2373, 5, 4.8, 760, 16500,
                "Urban resort with private plunge pools on expansive balconies overlooking the Delhi Golf Course and Humayun's Tomb.",
                List.of("Private Balcony Plunge Pools", "Pilates Studio", "Olympic Size Pool", "Tennis Courts", "Appanage Spa"),
                List.of("https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "The Lodhi Plunge Pool Room 360",
                List.of(
                        createRoom("rm-01", "Lodhi Room with Plunge Pool", RoomCategory.DELUXE, 9500, "King", 2, 600, PANORAMA_VILLA),
                        createRoom("rm-02", "Verandah Suite", RoomCategory.SUITE, 19500, "King", 3, 1100, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-del-06", "Roseate House New Delhi Aerocity", "Delhi", "DEL", "Delhi", "India",
                "Asset 10, Hospitality District, Aerocity, New Delhi 110037", 28.5524, 77.1219, 5, 4.6, 1420, 8500,
                "Contemporary luxury ultra-close to Terminal 3 with a rooftop infinity pool, 4K cinema auditorium, and DEL multi-cuisine bistro.",
                List.of("Rooftop Pool", "Aheli Spa", "4K Private Cinema", "Aerocity Airport Shuttle", "Fine Dining Bar"),
                List.of("https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Roseate Aerocity Premium 360",
                List.of(
                        createRoom("rm-01", "Deluxe Room", RoomCategory.STANDARD, 3800, "King", 2, 350, PANORAMA_DELUXE),
                        createRoom("rm-02", "Club Room", RoomCategory.PREMIUM, 6800, "King", 2, 450, PANORAMA_SUITE),
                        createRoom("rm-03", "Roseate Junior Suite", RoomCategory.SUITE, 15500, "King", 3, 850, null)
                )));

        // ==========================================
        // 2. MUMBAI (6 Hotels)
        // ==========================================
        list.add(createHotel("htl-bom-01", "The Taj Mahal Palace Mumbai", "Mumbai", "BOM", "Maharashtra", "India",
                "Apollo Bunder, Colaba, Mumbai 400001", 18.9217, 72.8332, 5, 4.9, 3200, 22000,
                "Grand sea-facing heritage landmark opened in 1903 overlooking Gateway of India, offering 9 world-famous restaurants and Jiva Spa.",
                List.of("Gateway of India View", "Jiva Spa", "Sea-Facing Swimming Pool", "Wasabi by Morimoto", "Yacht Charter"),
                List.of("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Taj Heritage Sea Lounge 360",
                List.of(
                        createRoom("rm-01", "Tower Sea View Room", RoomCategory.DELUXE, 11000, "King", 2, 450, PANORAMA_OCEAN),
                        createRoom("rm-02", "Palace Wing Heritage Room", RoomCategory.PREMIUM, 18000, "King", 2, 600, PANORAMA_SUITE),
                        createRoom("rm-03", "Tata Grand Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 65000, "King", 6, 2200, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-bom-02", "The Oberoi Nariman Point Mumbai", "Mumbai", "BOM", "Maharashtra", "India",
                "Nariman Point, Marine Drive, Mumbai 400021", 18.9275, 72.8206, 5, 4.9, 1600, 19500,
                "Unobstructed vistas of the Queen's Necklace on Marine Drive with Italian cuisine at Vetro and 24-hour personal butler care.",
                List.of("Queen's Necklace View", "Outdoor Pool", "Vetro Italian", "24-hr Spa", "Fitness Center"),
                List.of("https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Oberoi Ocean View Suite 360",
                List.of(
                        createRoom("rm-01", "Deluxe Ocean View", RoomCategory.DELUXE, 9800, "King", 2, 450, PANORAMA_OCEAN),
                        createRoom("rm-02", "Premier Bay View Room", RoomCategory.PREMIUM, 15000, "King", 2, 580, PANORAMA_SUITE),
                        createRoom("rm-03", "Executive Bay Suite", RoomCategory.SUITE, 36000, "King", 4, 1250, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-bom-03", "JW Marriott Mumbai Juhu", "Mumbai", "BOM", "Maharashtra", "India",
                "Juhu Tara Road, Juhu Beach, Mumbai 400049", 19.1018, 72.8266, 5, 4.7, 2400, 14500,
                "Premier beachfront luxury along Juhu Beach with cascading saltwater pools, Lotus Cafe, and lively celebrity nightlife.",
                List.of("Direct Beach Access", "3 Infinity Pools", "Lotus Cafe", "Quan Spa", "Enigma Club"),
                List.of("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "JW Juhu Beachfront Suite 360",
                List.of(
                        createRoom("rm-01", "Deluxe City View", RoomCategory.STANDARD, 5200, "Queen", 2, 380, null),
                        createRoom("rm-02", "Premier Ocean View", RoomCategory.DELUXE, 8900, "King", 2, 460, PANORAMA_OCEAN),
                        createRoom("rm-03", "Juhu Beachfront Suite", RoomCategory.SUITE, 24000, "King", 4, 980, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-bom-04", "The St. Regis Mumbai", "Mumbai", "BOM", "Maharashtra", "India",
                "462 Senapati Bapat Marg, Lower Parel, Mumbai 400013", 18.9934, 72.8242, 5, 4.8, 1950, 16000,
                "India's tallest luxury hotel towering over Palladium Mall, renowned for signature St. Regis Butler Service and Sahib Room cuisine.",
                List.of("St. Regis Butler Service", "Level 37 Rooftop Bar", "Iridium Spa", "Direct Mall Access", "Outdoor Infinity Pool"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "St. Regis Skyline Suite 360",
                List.of(
                        createRoom("rm-01", "Grand Deluxe Skyline", RoomCategory.DELUXE, 7800, "King", 2, 480, PANORAMA_DELUXE),
                        createRoom("rm-02", "St. Regis Suite", RoomCategory.SUITE, 21000, "King", 3, 1050, PANORAMA_SUITE),
                        createRoom("rm-03", "Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 55000, "King", 6, 2300, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-bom-05", "Trident Hotel Bandra Kurla", "Mumbai", "BOM", "Maharashtra", "India",
                "C-56, G Block, BKC, Bandra East, Mumbai 400098", 19.0664, 72.8687, 5, 4.7, 1340, 11500,
                "Modern contemporary hub in the heart of Bandra Kurla Complex with Italian dining at Botticino and full executive business facilities.",
                List.of("BKC Hub Location", "Swimming Pool", "Botticino Italian", "Trident Spa", "Conference Center"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Trident BKC Executive 360",
                List.of(
                        createRoom("rm-01", "Deluxe Room", RoomCategory.STANDARD, 4600, "Twin", 2, 340, PANORAMA_DELUXE),
                        createRoom("rm-02", "Trident Club Room", RoomCategory.PREMIUM, 8400, "King", 2, 480, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-bom-06", "ITC Grand Central Mumbai", "Mumbai", "BOM", "Maharashtra", "India",
                "287 Dr Babasaheb Ambedkar Road, Parel, Mumbai 400012", 18.9984, 72.8423, 5, 4.6, 1100, 8500,
                "Victorian architecture in historic Parel with Kebabs & Kurries dining, luxury wellness, and panoramic views of Mumbai harbor.",
                List.of("Pool", "Gym", "Kaya Kalp Spa", "Kebabs & Kurries", "Club Lounge"),
                List.of("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "ITC Grand Central Suite 360",
                List.of(
                        createRoom("rm-01", "Executive Club", RoomCategory.STANDARD, 3500, "Twin", 2, 300, null),
                        createRoom("rm-02", "The Towers Room", RoomCategory.DELUXE, 6500, "King", 2, 420, PANORAMA_DELUXE),
                        createRoom("rm-03", "ITC One Suite", RoomCategory.EXECUTIVE_SUITE, 18000, "King", 3, 850, PANORAMA_SUITE)
                )));

        // ==========================================
        // 3. BENGALURU / BANGALORE (6 Hotels)
        // ==========================================
        list.add(createHotel("htl-blr-01", "The Leela Palace Bengaluru", "Bangalore", "BLR", "Karnataka", "India",
                "23 HAL Old Airport Road, Bengaluru 560008", 12.9606, 77.6484, 5, 4.9, 2150, 14000,
                "Inspired by the Royal Palace of Mysore with sprawling 7-acre gardens, grand copper domes, and Jamavar royal Indian dining.",
                List.of("Mysore Palace Architecture", "7-Acre Gardens", "Outdoor Pool", "Jamavar Restaurant", "Spa by ESPA"),
                List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Leela Palace Royal Suite 360",
                List.of(
                        createRoom("rm-01", "Deluxe Room", RoomCategory.DELUXE, 7000, "King", 2, 460, PANORAMA_DELUXE),
                        createRoom("rm-02", "Premier Garden Suite", RoomCategory.SUITE, 22000, "King", 4, 1050, PANORAMA_PALACE),
                        createRoom("rm-03", "Maharaja Presidential Villa", RoomCategory.VILLA, 60000, "King", 6, 2600, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-blr-02", "ITC Gardenia Bengaluru", "Bangalore", "BLR", "Karnataka", "India",
                "1 Residency Road, Ashok Nagar, Bengaluru 560025", 12.9698, 77.5997, 5, 4.8, 1780, 13500,
                "Nature-forward luxury landmark in central Bengaluru with open helipad gardens, Edo Japanese cuisine, and Kaya Kalp spa.",
                List.of("LEED Platinum Certified", "Helipad", "Edo Japanese", "Rooftop Garden", "Kaya Kalp Spa"),
                List.of("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "ITC Gardenia Peacock Suite 360",
                List.of(
                        createRoom("rm-01", "Towers Room", RoomCategory.STANDARD, 4800, "Twin", 2, 380, PANORAMA_DELUXE),
                        createRoom("rm-02", "ITC One Room", RoomCategory.PREMIUM, 8900, "King", 2, 520, PANORAMA_SUITE),
                        createRoom("rm-03", "Peacock Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 45000, "King", 6, 2100, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-blr-03", "Taj West End Bengaluru", "Bangalore", "BLR", "Karnataka", "India",
                "25 Race Course Road, High Grounds, Bengaluru 560001", 12.9866, 77.5852, 5, 4.8, 1420, 12500,
                "Historic 1887 heritage sanctuary set across 20 acres of lush botanical canopy with private verandahs and Blue Ginger Vietnamese dining.",
                List.of("20-Acre Botanical Canopy", "Blue Ginger Vietnamese", "Heritage Walks", "Swimming Pool", "Jiva Spa"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Taj West End Garden Verandah 360",
                List.of(
                        createRoom("rm-01", "Luxury Verandah Room", RoomCategory.DELUXE, 6800, "King", 2, 480, PANORAMA_VILLA),
                        createRoom("rm-02", "Taj Club Suite", RoomCategory.SUITE, 19500, "King", 3, 980, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-blr-04", "The Ritz-Carlton Bangalore", "Bangalore", "BLR", "Karnataka", "India",
                "99 Residency Road, Shanthala Nagar, Bengaluru 560025", 12.9667, 77.6033, 5, 4.8, 1290, 15500,
                "Contemporary luxury with rooftop bar Bang, Jaagir art gallery, and Jaivana wellness sanctuary.",
                List.of("Bang Rooftop Bar", "The Ritz-Carlton Spa", "Heated Pool", "Art Walk", "Club Lounge"),
                List.of("https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "Ritz-Carlton Bangalore Executive 360",
                List.of(
                        createRoom("rm-01", "Deluxe Room", RoomCategory.STANDARD, 5900, "King", 2, 420, PANORAMA_DELUXE),
                        createRoom("rm-02", "Club Executive Suite", RoomCategory.SUITE, 24000, "King", 4, 1150, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-blr-05", "Bengaluru Marriott Hotel Whitefield", "Bangalore", "BLR", "Karnataka", "India",
                "Plot 75, EPIP Zone, Whitefield, Bengaluru 560066", 12.9788, 77.7289, 5, 4.6, 980, 6500,
                "Premier tech-corridor hotel with M Cafe, rooftop pool, and extensive corporate conference facilities.",
                List.of("Rooftop Pool", "M Cafe", "Quan Spa", "Fitness Center", "IT Corridor Shuttle"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Marriott Whitefield Deluxe 360",
                List.of(
                        createRoom("rm-01", "Deluxe Room", RoomCategory.STANDARD, 3200, "Twin", 2, 320, PANORAMA_DELUXE),
                        createRoom("rm-02", "Executive Suite", RoomCategory.DELUXE, 6500, "King", 2, 450, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-blr-06", "Conrad Bengaluru", "Bangalore", "BLR", "Karnataka", "India",
                "25/3 Kensington Road, Ulsoor, Bengaluru 560008", 12.9734, 77.6206, 5, 4.8, 1150, 11000,
                "Overlooking scenic Ulsoor Lake, featuring an infinity pool, Tiamo Mediterranean al fresco dining, and Conrad Spa.",
                List.of("Ulsoor Lake View", "Infinity Pool", "Tiamo Al Fresco", "Conrad Spa", "Ballroom"),
                List.of("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Conrad Lake View Suite 360",
                List.of(
                        createRoom("rm-01", "Lake View Deluxe", RoomCategory.DELUXE, 5400, "King", 2, 440, PANORAMA_OCEAN),
                        createRoom("rm-02", "Conrad Executive Suite", RoomCategory.SUITE, 18500, "King", 3, 920, PANORAMA_SUITE)
                )));

        // ==========================================
        // 4. CHENNAI (5 Hotels)
        // ==========================================
        list.add(createHotel("htl-maa-01", "The Leela Palace Chennai", "Chennai", "MAA", "Tamil Nadu", "India",
                "Adyar Seaface, MRC Nagar, Chennai 600028", 13.0163, 80.2762, 5, 4.9, 1890, 13500,
                "Chennai's only modern sea-facing palace hotel overlooking the Bay of Bengal with Chettinad architecture and infinity sea pool.",
                List.of("Bay of Bengal Sea View", "Sea-Facing Infinity Pool", "China XO Dining", "ESPA Spa", "Ballroom"),
                List.of("https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Leela Palace Chennai Sea Suite 360",
                List.of(
                        createRoom("rm-01", "Deluxe City View", RoomCategory.STANDARD, 4800, "King", 2, 450, PANORAMA_DELUXE),
                        createRoom("rm-02", "Premier Sea View", RoomCategory.DELUXE, 8500, "King", 2, 580, PANORAMA_OCEAN),
                        createRoom("rm-03", "Royal Sea Suite", RoomCategory.SUITE, 28000, "King", 4, 1200, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-maa-02", "ITC Grand Chola Chennai", "Chennai", "MAA", "Tamil Nadu", "India",
                "63 Mount Road, Guindy, Chennai 600032", 13.0105, 80.2207, 5, 4.9, 2800, 12000,
                "Monumental tribute to Southern India's Chola Dynasty with 600 rooms, 10 iconic dining destinations, and 3 swimming pools.",
                List.of("Chola Architecture", "3 Swimming Pools", "Peshawri Dining", "Royal Spa", "Helipad"),
                List.of("https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "ITC Grand Chola Raja Suite 360",
                List.of(
                        createRoom("rm-01", "Executive Club", RoomCategory.STANDARD, 4200, "Twin", 2, 400, null),
                        createRoom("rm-02", "ITC One Luxury", RoomCategory.PREMIUM, 7900, "King", 2, 540, PANORAMA_SUITE),
                        createRoom("rm-03", "Karikalan Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 48000, "King", 6, 2200, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-maa-03", "Taj Connemara Chennai", "Chennai", "MAA", "Tamil Nadu", "India",
                "Binny Road, Anna Salai, Chennai 600002", 13.0601, 80.2606, 5, 4.7, 1120, 8000,
                "South India's oldest heritage hotel dating from 1854, with colonial verandahs and Raintree authentic Chettinad alfresco dining.",
                List.of("1854 Heritage Landmark", "Raintree Chettinad Dining", "Outdoor Pool", "Jiva Spa"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "Taj Connemara Heritage Suite 360",
                List.of(
                        createRoom("rm-01", "Heritage Room", RoomCategory.STANDARD, 3800, "Queen", 2, 360, PANORAMA_DELUXE),
                        createRoom("rm-02", "Colonial Suite", RoomCategory.SUITE, 16000, "King", 3, 850, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-maa-04", "The Park Chennai", "Chennai", "MAA", "Tamil Nadu", "India",
                "601 Anna Salai, Nungambakkam, Chennai 600006", 13.0538, 80.2508, 5, 4.6, 920, 6800,
                "Design-led boutique hotel on the historic Gemini Film Studios site with rooftop poolside lounge Aqua and A2 restaurant.",
                List.of("Aqua Rooftop Lounge", "Aura Spa", "Pool", "Gym", "Central Anna Salai Location"),
                List.of("https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "The Park Chennai Suite 360",
                List.of(
                        createRoom("rm-01", "Luxury Room", RoomCategory.STANDARD, 3200, "Queen", 2, 320, PANORAMA_DELUXE),
                        createRoom("rm-02", "The Park Suite", RoomCategory.SUITE, 14000, "King", 3, 780, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-maa-05", "Grand Chennai by GRT Hotels", "Chennai", "MAA", "Tamil Nadu", "India",
                "120 Sir Thyagaraya Road, T. Nagar, Chennai 600017", 13.0402, 80.2335, 4, 4.5, 870, 5200,
                "Vibrant T. Nagar shopping hub location featuring Bazaar global street-food dining, Bodhi Spa, and indoor thermal pool.",
                List.of("T. Nagar Shopping District", "Indoor Heated Pool", "Bodhi Spa", "Bazaar Restaurant"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "GRT Grand Executive 360",
                List.of(
                        createRoom("rm-01", "Superior Room", RoomCategory.STANDARD, 2800, "Twin", 2, 300, null),
                        createRoom("rm-02", "Deluxe Suite", RoomCategory.DELUXE, 5800, "King", 2, 450, PANORAMA_DELUXE)
                )));

        // ==========================================
        // 5. HYDERABAD (5 Hotels)
        // ==========================================
        list.add(createHotel("htl-hyd-01", "Taj Falaknuma Palace", "Hyderabad", "HYD", "Telangana", "India",
                "Engine Bowli, Falaknuma, Hyderabad 500053", 17.3315, 78.4678, 5, 5.0, 2400, 48000,
                "Former palace of the Nizam of Hyderabad 2,000 feet above the city, with horse-drawn carriage arrival, 101-seat dining table, and Jiva Spa.",
                List.of("Nizam's Palace Experience", "Horse Carriage Welcome", "101-Seat Dining Table", "Jiva Spa", "Outdoor Pool"),
                List.of("https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Nizam Grand Presidential Suite 360",
                List.of(
                        createRoom("rm-01", "Palace Room", RoomCategory.DELUXE, 22000, "King", 2, 700, PANORAMA_DELUXE),
                        createRoom("rm-02", "Historical Suite", RoomCategory.SUITE, 45000, "King", 3, 1200, PANORAMA_PALACE),
                        createRoom("rm-03", "Nizam Suite", RoomCategory.PRESIDENTIAL_SUITE, 95000, "King", 6, 2500, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-hyd-02", "ITC Kohenur Hyderabad", "Hyderabad", "HYD", "Telangana", "India",
                "Knowledge City, Madhapur, HITEC City, Hyderabad 500081", 17.4384, 78.3792, 5, 4.8, 1650, 12500,
                "Architectural wonder overlooking Durgam Cheruvu Lake in HITEC City with Golconda Pavilion and Italian dining at Ottimo.",
                List.of("Durgam Cheruvu Lake View", "Kaya Kalp Spa", "Rooftop Pool", "Ottimo Italian", "HITEC City Hub"),
                List.of("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "ITC Kohenur Lake Suite 360",
                List.of(
                        createRoom("rm-01", "Executive Club", RoomCategory.STANDARD, 4500, "King", 2, 380, null),
                        createRoom("rm-02", "ITC One Lake View", RoomCategory.PREMIUM, 8800, "King", 2, 520, PANORAMA_SUITE),
                        createRoom("rm-03", "Grand Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 42000, "King", 4, 1800, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-hyd-03", "Park Hyatt Hyderabad", "Hyderabad", "HYD", "Telangana", "India",
                "Road No 2, Banjara Hills, Hyderabad 500034", 17.4239, 78.4283, 5, 4.8, 1340, 11000,
                "Opulent 8-story atrium with sweeping art sculptures in Banjara Hills, featuring Tre-Forni Italian and holistic spa therapies.",
                List.of("Banjara Hills Landmark", "Infinity Pool", "The Spa", "Tre-Forni Italian", "Fitness Center"),
                List.of("https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Park Hyatt Banjara Suite 360",
                List.of(
                        createRoom("rm-01", "Park King Room", RoomCategory.STANDARD, 4200, "King", 2, 450, PANORAMA_DELUXE),
                        createRoom("rm-02", "Park Suite", RoomCategory.SUITE, 17500, "King", 3, 900, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-hyd-04", "Novotel Hyderabad Airport", "Hyderabad", "HYD", "Telangana", "India",
                "Rajiv Gandhi Intl Airport, Shamshabad, Hyderabad 500108", 17.2341, 78.4322, 5, 4.6, 920, 6800,
                "Airport resort set across 5 acres with swimming pool, sports arena, and 24-hour complimentary airport shuttle.",
                List.of("Free 24hr Airport Shuttle", "Resort Pool", "Sports Arena", "O2 Spa", "The Square 24/7"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Novotel Airport Suite 360",
                List.of(
                        createRoom("rm-01", "Superior Room", RoomCategory.STANDARD, 3200, "King", 2, 340, null),
                        createRoom("rm-02", "Executive Suite", RoomCategory.DELUXE, 6900, "King", 2, 480, PANORAMA_DELUXE)
                )));

        list.add(createHotel("htl-hyd-05", "Hyderabad Marriott Hotel & Convention Centre", "Hyderabad", "HYD", "Telangana", "India",
                "Tank Bund Road, Hussain Sagar Lake, Hyderabad 500080", 17.4264, 78.4891, 5, 4.6, 1180, 7500,
                "Prime location overlooking Hussain Sagar Lake with waterfront dining at Altitude Lounge and expansive convention lawns.",
                List.of("Hussain Sagar Lake View", "Altitude Lounge", "Quan Spa", "Swimming Pool"),
                List.of("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Marriott Lake View 360",
                List.of(
                        createRoom("rm-01", "Lake View Deluxe", RoomCategory.STANDARD, 3500, "Queen", 2, 350, PANORAMA_OCEAN),
                        createRoom("rm-02", "Executive Lake Suite", RoomCategory.SUITE, 12500, "King", 3, 750, PANORAMA_SUITE)
                )));

        // ==========================================
        // 6. GOA (7 Hotels)
        // ==========================================
        list.add(createHotel("htl-goi-01", "Taj Exotica Resort & Spa Goa", "Goa", "GOI", "Goa", "India",
                "Calwaddo, Benaulim Beach, Salcete, Goa 403716", 15.2536, 73.9248, 5, 4.9, 2900, 22000,
                "Mediterranean-inspired 56-acre paradise overlooking Benaulim Beach with golf course, private plunge pool villas, and Jiva Spa.",
                List.of("Direct Beach Access", "56-Acre Garden", "Private Plunge Pool Villas", "Golf Course", "Jiva Spa"),
                List.of("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Taj Exotica Ocean Villa 360",
                List.of(
                        createRoom("rm-01", "Deluxe Garden View", RoomCategory.DELUXE, 9500, "King", 2, 500, PANORAMA_DELUXE),
                        createRoom("rm-02", "Sunset Sea View Suite", RoomCategory.SUITE, 28000, "King", 4, 1100, PANORAMA_OCEAN),
                        createRoom("rm-03", "Presidential Pool Villa", RoomCategory.VILLA, 75000, "King", 6, 2800, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-goi-02", "W Goa Vagator", "Goa", "GOI", "Goa", "India",
                "Vagator Beach, Bardez, Goa 403509", 15.6022, 73.7348, 5, 4.8, 1680, 24000,
                "Bold beachfront energy on Vagator Beach under Chapora Fort with WET pool parties, Rockpool sunset lounge, and AWAY Spa.",
                List.of("Vagator Beachfront", "WET Deck Pool", "Rockpool Sunset Bar", "AWAY Spa", "FIT Gym"),
                List.of("https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "W Goa Marvelous Chalet 360",
                List.of(
                        createRoom("rm-01", "Wonderful Room", RoomCategory.DELUXE, 11000, "King", 2, 520, PANORAMA_DELUXE),
                        createRoom("rm-02", "Fabulous Villa", RoomCategory.VILLA, 32000, "King", 4, 1250, PANORAMA_VILLA),
                        createRoom("rm-03", "WOW Ocean Villa", RoomCategory.PRESIDENTIAL_SUITE, 82000, "King", 6, 2600, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-goi-03", "The Leela Goa", "Goa", "GOI", "Goa", "India",
                "Mobor Beach, Cavelossim, Goa 403731", 15.1584, 73.9452, 5, 4.9, 2100, 20000,
                "Secluded 75-acre sanctuary between the Arabian Sea and Sal River with private lagoons, 12-hole golf course, and Mobor beach.",
                List.of("Private Lagoon View", "12-Hole Golf Course", "Mobor Beach", "Riverside Dining", "Spa"),
                List.of("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "The Leela Goa Lagoon Villa 360",
                List.of(
                        createRoom("rm-01", "Lagoon Terrace Room", RoomCategory.DELUXE, 8500, "King", 2, 480, PANORAMA_OCEAN),
                        createRoom("rm-02", "Lagoon Suite", RoomCategory.SUITE, 24000, "King", 3, 980, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-goi-04", "Alila Diwa Goa", "Goa", "GOI", "Goa", "India",
                "48/10 Adao Waddo, Majorda, Goa 403713", 15.3134, 73.9167, 5, 4.8, 1420, 13500,
                "Contemporary Balinese-inspired resort surrounded by lush emerald paddy fields leading to Majorda Beach.",
                List.of("Paddy Field Infinity Pool", "Majorda Beach Shuttle", "Spa Alila", "Bistro Dining"),
                List.of("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Alila Diwa Paddy Villa 360",
                List.of(
                        createRoom("rm-01", "Terrace Room", RoomCategory.STANDARD, 5500, "King", 2, 450, PANORAMA_DELUXE),
                        createRoom("rm-02", "Alila Suite", RoomCategory.SUITE, 16500, "King", 3, 920, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-goi-05", "Grand Hyatt Goa", "Goa", "GOI", "Goa", "India",
                "P.O. Goa University, Bambolim, Goa 403206", 15.4542, 73.8569, 5, 4.8, 1980, 16000,
                "17th-century Indo-Portuguese palace on Bambolim Bay with indoor & outdoor pools, Shamana Spa, and waterfront lawns.",
                List.of("Indo-Portuguese Palace", "Bambolim Bay View", "Shamana Spa", "Indoor & Outdoor Pools"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Grand Hyatt Bambolim Suite 360",
                List.of(
                        createRoom("rm-01", "Grand Room", RoomCategory.STANDARD, 6200, "King", 2, 480, null),
                        createRoom("rm-02", "Grand Suite with Bay View", RoomCategory.SUITE, 21000, "King", 4, 1100, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-goi-06", "ITC Grand Goa Resort & Spa", "Goa", "GOI", "Goa", "India",
                "Arossim Beach, Cansaulim, Goa 403712", 15.3402, 73.8966, 5, 4.8, 1540, 17500,
                "Village-style architecture nestled among 45 acres of lush gardens with multi-level swimming pool and direct Arossim beach access.",
                List.of("Arossim Beach Access", "Multi-Level Pool", "Kaya Kalp Spa", "Village Architecture"),
                List.of("https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "ITC Grand Goa Sea View 360",
                List.of(
                        createRoom("rm-01", "Garden View Room", RoomCategory.STANDARD, 6800, "King", 2, 440, PANORAMA_DELUXE),
                        createRoom("rm-02", "Sea View Suite", RoomCategory.SUITE, 25000, "King", 4, 1050, PANORAMA_OCEAN)
                )));

        list.add(createHotel("htl-goi-07", "Hard Rock Hotel Goa", "Goa", "GOI", "Goa", "India",
                "370/14 Bishop Alex Dias Road, Calangute, Goa 403516", 15.5412, 73.7634, 4, 4.5, 960, 6200,
                "Music-infused energetic resort in North Goa with live music shows, Fender guitar in-room amenities, and resort pool.",
                List.of("Music Memorabilia", "Fender Guitar In-Room", "Rock Spa", "Resort Pool", "Calangute Location"),
                List.of("https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Hard Rock Goa Rock Suite 360",
                List.of(
                        createRoom("rm-01", "Silver Room", RoomCategory.STANDARD, 2800, "Twin", 2, 320, null),
                        createRoom("rm-02", "Rock Suite", RoomCategory.DELUXE, 6200, "King", 2, 520, PANORAMA_DELUXE)
                )));

        // ==========================================
        // 7. KOCHI / KERALA (5 Hotels)
        // ==========================================
        list.add(createHotel("htl-cok-01", "Grand Hyatt Kochi Bolgatty", "Kochi", "COK", "Kerala", "India",
                "Mulavukad, Bolgatty Island, Kochi 682504", 9.9882, 76.2673, 5, 4.9, 1850, 11500,
                "Waterfront luxury on Bolgatty Island with panoramic views of Vembanad Lake, Santata Spa, and marina yacht access.",
                List.of("Vembanad Lake View", "Marina Yacht Access", "Santata Spa", "Indoor & Outdoor Pools", "Helipad"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Grand Hyatt Bolgatty Lakefront Suite 360",
                List.of(
                        createRoom("rm-01", "Lake View King", RoomCategory.DELUXE, 4800, "King", 2, 450, PANORAMA_OCEAN),
                        createRoom("rm-02", "Grand Executive Suite", RoomCategory.SUITE, 16000, "King", 4, 980, PANORAMA_SUITE),
                        createRoom("rm-03", "Presidential Waterfront Villa", RoomCategory.VILLA, 45000, "King", 6, 2100, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-cok-02", "Taj Malabar Resort & Spa Cochin", "Kochi", "COK", "Kerala", "India",
                "Willingdon Island, Kochi 682009", 9.9678, 76.2625, 5, 4.8, 1420, 10500,
                "Historic Willingdon Island jewel with harbor sunset cruises, Ayurvedic wellness, and seafood dining at The Rice Boat.",
                List.of("Harbor View", "The Rice Boat Seafood", "Sunset Yacht Cruise", "Jiva Spa", "Infinity Pool"),
                List.of("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Taj Malabar Harbor Suite 360",
                List.of(
                        createRoom("rm-01", "Superior Sea View", RoomCategory.STANDARD, 4200, "Twin", 2, 380, PANORAMA_OCEAN),
                        createRoom("rm-02", "Sunset Harbor Suite", RoomCategory.SUITE, 15000, "King", 3, 850, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-cok-03", "Brunton Boatyard Fort Kochi", "Kochi", "COK", "Kerala", "India",
                "1/498 Calvathy Road, Fort Kochi, Kochi 682001", 9.9691, 76.2428, 5, 4.8, 890, 12000,
                "Victorian shipbuilding yard restored into a colonial heritage hotel overlooking the historic harbor channel.",
                List.of("Fort Kochi Channel View", "Colonial Architecture", "Ayurvedic Center", "Harbor Pool"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Brunton Boatyard Heritage Suite 360",
                List.of(
                        createRoom("rm-01", "Sea Facing Heritage Room", RoomCategory.DELUXE, 5800, "King", 2, 450, PANORAMA_OCEAN),
                        createRoom("rm-02", "Harbor Master Suite", RoomCategory.SUITE, 18000, "King", 3, 900, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-cok-04", "Fragrant Nature Kochi", "Kochi", "COK", "Kerala", "India",
                "Bazaar Road, Mattancherry, Kochi 682002", 9.9572, 76.2575, 5, 4.7, 760, 7500,
                "Boutique hotel in Mattancherry with trompe-l'œil murals, glass pool overlooking the harbor, and Prana wellness.",
                List.of("Glass Pool", "Prana Spa", "Flint House Dining", "Harbor View"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Fragrant Nature Boutique 360",
                List.of(
                        createRoom("rm-01", "Duke's Chamber", RoomCategory.STANDARD, 3200, "King", 2, 340, PANORAMA_DELUXE),
                        createRoom("rm-02", "Royal Suite", RoomCategory.SUITE, 9800, "King", 3, 720, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-cok-05", "Marari Beach Resort - CGH Earth", "Kochi", "COK", "Kerala", "India",
                "Mararikulam, Alleppey Coast, Kochi 688549", 9.6012, 76.2995, 5, 4.9, 1310, 14500,
                "Eco-luxury thatched-roof cottages set across 30 acres of coconut groves opening directly onto pristine Marari Beach.",
                List.of("Thatched Cottages", "Direct Beach", "Ayurvedic Clinic", "Organic Farm", "Butterfly Garden"),
                List.of("https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Marari Beach Pool Villa 360",
                List.of(
                        createRoom("rm-01", "Garden Cottage", RoomCategory.STANDARD, 6200, "King", 2, 480, null),
                        createRoom("rm-02", "Deluxe Pool Villa", RoomCategory.VILLA, 22000, "King", 4, 1100, PANORAMA_VILLA)
                )));

        // ==========================================
        // 8. JAIPUR (5 Hotels)
        // ==========================================
        list.add(createHotel("htl-jai-01", "Rambagh Palace Jaipur", "Jaipur", "JAI", "Rajasthan", "India",
                "Bhawani Singh Road, Jaipur 302005", 26.8967, 75.8078, 5, 5.0, 3100, 38000,
                "Known as the 'Jewel of Jaipur', former royal residence of the Maharaja of Jaipur set in 47 acres of Mughal gardens.",
                List.of("Royal Maharaja Palace", "47-Acre Mughal Gardens", "Polo Bar", "Jiva Grande Spa", "Peacock Gardens"),
                List.of("https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Rambagh Palace Maharani Suite 360",
                List.of(
                        createRoom("rm-01", "Palace Room", RoomCategory.DELUXE, 18000, "King", 2, 550, PANORAMA_DELUXE),
                        createRoom("rm-02", "Historical Palace Suite", RoomCategory.SUITE, 42000, "King", 4, 1200, PANORAMA_PALACE),
                        createRoom("rm-03", "Grand Presidential Maharani Suite", RoomCategory.PRESIDENTIAL_SUITE, 98000, "King", 6, 2600, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-jai-02", "The Oberoi Rajvilas Jaipur", "Jaipur", "JAI", "Rajasthan", "India",
                "Goner Road, Jaipur 302031", 26.8778, 75.8856, 5, 4.9, 1450, 35000,
                "Royal fort resort with private luxury tents, sunken marble baths, 280-year-old Shiva temple, and flaming torchlit evenings.",
                List.of("Luxury Air-Conditioned Tents", "Sunken Marble Baths", "280-yr Temple", "Oberoi Spa", "Torchlit Courtyard"),
                List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Oberoi Rajvilas Royal Tent 360",
                List.of(
                        createRoom("rm-01", "Premier Room", RoomCategory.DELUXE, 16000, "King", 2, 550, PANORAMA_DELUXE),
                        createRoom("rm-02", "Luxury Royal Tent", RoomCategory.VILLA, 38000, "King", 2, 850, PANORAMA_VILLA),
                        createRoom("rm-03", "Kohinoor Villa with Private Pool", RoomCategory.PRESIDENTIAL_SUITE, 88000, "King", 4, 2200, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-jai-03", "ITC Rajputana Jaipur", "Jaipur", "JAI", "Rajasthan", "India",
                "Palace Road, Gopalbari, Jaipur 302006", 26.9189, 75.7925, 5, 4.7, 1890, 8500,
                "Traditional Rajasthani haveli architecture with central step-well pool, Peshawri dining, and royal courtyard musicians.",
                List.of("Stepwell Swimming Pool", "Peshawri Dining", "Kaya Kalp Spa", "Haveli Architecture"),
                List.of("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "ITC Rajputana Royal Haveli Suite 360",
                List.of(
                        createRoom("rm-01", "Executive Club", RoomCategory.STANDARD, 3800, "Twin", 2, 340, null),
                        createRoom("rm-02", "Rajputana Royale", RoomCategory.PREMIUM, 6800, "King", 2, 480, PANORAMA_DELUXE),
                        createRoom("rm-03", "Thikana Suite", RoomCategory.SUITE, 19500, "King", 4, 980, PANORAMA_SUITE)
                )));

        list.add(createHotel("htl-jai-04", "Fairmont Jaipur", "Jaipur", "JAI", "Rajasthan", "India",
                "2 Riico Kukas, Jaipur 302028", 27.0421, 75.8943, 5, 4.8, 1620, 11000,
                "Grand Mughal-Rajput fortress palace set against the Aravalli Hills with Zoya culinary theater and Ruhab luxury spa.",
                List.of("Aravalli Hills Backdrop", "Fortress Architecture", "Ruhab Spa", "Royal High Tea"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Fairmont Jaipur Palace Suite 360",
                List.of(
                        createRoom("rm-01", "Fairmont Room", RoomCategory.STANDARD, 4500, "King", 2, 500, PANORAMA_DELUXE),
                        createRoom("rm-02", "Signature Palace Suite", RoomCategory.SUITE, 18500, "King", 3, 1100, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-jai-05", "Samode Palace Jaipur", "Jaipur", "JAI", "Rajasthan", "India",
                "Samode Village, Jaipur 303806", 27.2038, 75.8146, 5, 4.9, 940, 16000,
                "475-year-old royal palace with handcrafted mirror-work Sheesh Mahal, rooftop infinity pool, and historic frescoed corridors.",
                List.of("475-yr Royal Palace", "Sheesh Mahal Mirror Hall", "Rooftop Pool", "Historic Frescoes"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Samode Sheesh Mahal Suite 360",
                List.of(
                        createRoom("rm-01", "Deluxe Heritage Room", RoomCategory.DELUXE, 7500, "King", 2, 450, PANORAMA_DELUXE),
                        createRoom("rm-02", "Sheesh Mahal Suite", RoomCategory.SUITE, 28000, "King", 4, 1200, PANORAMA_PALACE)
                )));

        // ==========================================
        // 9. UDAIPUR (5 Hotels)
        // ==========================================
        list.add(createHotel("htl-udr-01", "The Oberoi Udaivilas Udaipur", "Udaipur", "UDR", "Rajasthan", "India",
                "Haridas Ji Ki Magri, Lake Pichola, Udaipur 313001", 24.5772, 73.6738, 5, 5.0, 2980, 42000,
                "World-renowned palace resort on Lake Pichola with semi-private moated swimming pools, domes, and private shikara boat transfers.",
                List.of("Lake Pichola Waterfront", "Semi-Private Moated Pools", "Private Shikara Transfers", "Oberoi Spa", "Peacock Sanctuary"),
                List.of("https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Oberoi Udaivilas Kohinoor Suite 360",
                List.of(
                        createRoom("rm-01", "Premier Room with Semi-Private Pool", RoomCategory.DELUXE, 22000, "King", 2, 600, PANORAMA_OCEAN),
                        createRoom("rm-02", "Luxury Suite with Private Pool", RoomCategory.SUITE, 58000, "King", 4, 1300, PANORAMA_VILLA),
                        createRoom("rm-03", "Kohinoor Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 120000, "King", 6, 2800, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-udr-02", "Taj Lake Palace Udaipur", "Udaipur", "UDR", "Rajasthan", "India",
                "P.O. Box No. 5, Lake Pichola, Udaipur 313001", 24.5756, 73.6800, 5, 5.0, 3100, 39000,
                "18th-century floating white marble palace in the center of Lake Pichola with private butler service and Jiva Spa Boat.",
                List.of("Floating Island Palace", "Jiva Spa Boat", "Private Lake Butler", "Bhairon Rooftop Dining"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Taj Lake Palace Grand Royal 360",
                List.of(
                        createRoom("rm-01", "Luxury Lake View Room", RoomCategory.DELUXE, 19000, "King", 2, 450, PANORAMA_OCEAN),
                        createRoom("rm-02", "Grand Royal Suite", RoomCategory.SUITE, 52000, "King", 4, 1100, PANORAMA_PALACE)
                )));

        list.add(createHotel("htl-udr-03", "The Leela Palace Udaipur", "Udaipur", "UDR", "Rajasthan", "India",
                "Lake Pichola, PO Box 125, Udaipur 313001", 24.5806, 73.6764, 5, 4.9, 1850, 32000,
                "Modern palace on Lake Pichola with Sheesh Mahal rooftop dining, ESPA tented spa, and panoramic views of City Palace.",
                List.of("City Palace Views", "ESPA Tented Spa", "Sheesh Mahal Dining", "Private Boat Jetty"),
                List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Leela Udaipur Maharaja Suite 360",
                List.of(
                        createRoom("rm-01", "Grand Heritage Lake View", RoomCategory.DELUXE, 15000, "King", 2, 580, PANORAMA_OCEAN),
                        createRoom("rm-02", "Duplex Suite with Plunge Pool", RoomCategory.SUITE, 42000, "King", 4, 1250, PANORAMA_VILLA)
                )));

        list.add(createHotel("htl-udr-04", "Fateh Garh Heritage Resort", "Udaipur", "UDR", "Rajasthan", "India",
                "Sisarma, Udaipur 313001", 24.5512, 73.6521, 5, 4.7, 820, 9500,
                "Sanctuary perched high on a hill overlooking Lake Pichola and the Aravalli range, with vintage car collection.",
                List.of("Hilltop Lake Panorama", "Vintage Car Museum", "2 Infinity Pools", "Heritage Architecture"),
                List.of("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Fateh Garh Hilltop Suite 360",
                List.of(
                        createRoom("rm-01", "Renaissance Room", RoomCategory.STANDARD, 4200, "King", 2, 400, null),
                        createRoom("rm-02", "Heritage Suite", RoomCategory.SUITE, 12500, "King", 3, 850, PANORAMA_OCEAN)
                )));

        list.add(createHotel("htl-udr-05", "Trident Hotel Udaipur", "Udaipur", "UDR", "Rajasthan", "India",
                "Haridas Ji Ki Magri, Mulla Talai, Udaipur 313001", 24.5721, 73.6689, 5, 4.7, 1250, 7800,
                "Set across 43 acres on the banks of Lake Pichola with wildlife sanctuary, swimming pool, and Aravalli restaurant.",
                List.of("43 Acres of Landscaped Gardens", "Kids Club Bheem", "Pool", "Lake Pichola Banks"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Trident Udaipur Garden Suite 360",
                List.of(
                        createRoom("rm-01", "Deluxe Garden View", RoomCategory.STANDARD, 3500, "Twin", 2, 340, null),
                        createRoom("rm-02", "Deluxe Lake View", RoomCategory.DELUXE, 6200, "King", 2, 420, PANORAMA_OCEAN)
                )));

        // ==========================================
        // 10. COIMBATORE, MADURAI, AHMEDABAD, PUNE, KOLKATA, OOTY, MYSORE, PONDICHERRY, TIRUPATI, VARANASI, RISHIKESH
        // ==========================================
        list.add(createHotel("htl-cjb-01", "Radisson Blu Coimbatore", "Coimbatore", "CJB", "Tamil Nadu", "India",
                "Avinashi Road, Peelamedu, Coimbatore 641004", 11.0267, 77.0142, 5, 4.6, 940, 5200,
                "Modern business hotel on Avinashi Road with rooftop pool, Cambridge English lounge, and Bodhi Spa.",
                List.of("Rooftop Pool", "Spa", "Gym", "Avinashi Road Hub"),
                List.of("https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Radisson Coimbatore Suite 360",
                List.of(createRoom("rm-01", "Superior Room", RoomCategory.STANDARD, 2600, "Queen", 2, 320, null),
                        createRoom("rm-02", "Business Class Suite", RoomCategory.SUITE, 6500, "King", 3, 680, PANORAMA_SUITE))));

        list.add(createHotel("htl-cjb-02", "Welcomhotel by ITC Hotels Race Course", "Coimbatore", "CJB", "Tamil Nadu", "India",
                "1266/14 West Club Road, Race Course, Coimbatore 641018", 11.0024, 76.9742, 5, 4.7, 820, 5800,
                "Set in the quiet green haven of Race Course with WelcomCafe Kovai and Kerehaklu coffee lounge.",
                List.of("Race Course Greenery", "Pool", "WelcomCafe", "Fitness Center"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Welcomhotel Coimbatore 360",
                List.of(createRoom("rm-01", "Superior Room", RoomCategory.STANDARD, 2900, "Twin", 2, 330, null),
                        createRoom("rm-02", "Executive Suite", RoomCategory.SUITE, 7200, "King", 3, 720, PANORAMA_DELUXE))));

        list.add(createHotel("htl-ixm-01", "Heritage Madurai", "Madurai", "IXM", "Tamil Nadu", "India",
                "11 Melakkal Main Road, Kochadai, Madurai 625016", 9.9412, 78.0834, 5, 4.8, 980, 7500,
                "Geoffrey Bawa architectural marvel with temple-tank Olympic swimming pool and traditional Chettinad villas.",
                List.of("Geoffrey Bawa Architecture", "Temple Tank Pool", "Ayurvedic Spa", "Plunge Pool Villas"),
                List.of("https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Heritage Madurai Pool Villa 360",
                List.of(createRoom("rm-01", "Deluxe Bawa Room", RoomCategory.STANDARD, 3800, "King", 2, 450, null),
                        createRoom("rm-02", "Private Plunge Pool Villa", RoomCategory.VILLA, 12500, "King", 4, 980, PANORAMA_VILLA))));

        list.add(createHotel("htl-amd-01", "Hyatt Regency Ahmedabad", "Ahmedabad", "AMD", "Gujarat", "India",
                "17A Ashram Road, Usmanpura, Ahmedabad 380014", 23.0478, 72.5714, 5, 4.7, 1280, 6800,
                "Riverfront landmark on Ashram Road overlooking Sabarmati River with China House and Ariva spa.",
                List.of("Sabarmati Riverfront View", "Pool", "China House", "Ariva Spa"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Hyatt Regency Sabarmati Suite 360",
                List.of(createRoom("rm-01", "Riverfront Deluxe", RoomCategory.DELUXE, 3600, "King", 2, 420, PANORAMA_OCEAN),
                        createRoom("rm-02", "Regency Executive Suite", RoomCategory.SUITE, 10500, "King", 3, 850, PANORAMA_SUITE))));

        list.add(createHotel("htl-amd-02", "The House of MG Heritage Hotel", "Ahmedabad", "AMD", "Gujarat", "India",
                "Opposite Sidi Saiyyed Mosque, Gheekanta, Ahmedabad 380001", 23.0284, 72.5818, 5, 4.8, 920, 8500,
                "20th-century mansion restored to glory featuring Agashiye award-winning rooftop Gujarati thali dining.",
                List.of("Agashiye Rooftop Dining", "Heritage Mansion", "Indoor Lotus Pool", "Heritage Walks"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "House of MG Grand Suite 360",
                List.of(createRoom("rm-01", "Heritage Room", RoomCategory.STANDARD, 4200, "King", 2, 400, null),
                        createRoom("rm-02", "Grand Heritage Suite", RoomCategory.SUITE, 14000, "King", 3, 900, PANORAMA_PALACE))));

        list.add(createHotel("htl-pnq-01", "JW Marriott Hotel Pune", "Pune", "PNQ", "Maharashtra", "India",
                "Senapati Bapat Road, Shivajinagar, Pune 411053", 18.5362, 73.8302, 5, 4.8, 1950, 11000,
                "Iconic Senapati Bapat Road destination with Paasha rooftop lounge, Quan Spa, and expansive convention halls.",
                List.of("Paasha Rooftop Lounge", "Quan Spa", "Swimming Pool", "SB Road Hub"),
                List.of("https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "JW Marriott Pune Executive 360",
                List.of(createRoom("rm-01", "Deluxe Room", RoomCategory.STANDARD, 4800, "King", 2, 420, PANORAMA_DELUXE),
                        createRoom("rm-02", "Executive Suite", RoomCategory.SUITE, 16000, "King", 3, 950, PANORAMA_SUITE))));

        list.add(createHotel("htl-pnq-02", "The Ritz-Carlton Pune", "Pune", "PNQ", "Maharashtra", "India",
                "Golf Course Square, Airport Road, Yerwada, Pune 411006", 18.5584, 73.8891, 5, 4.9, 1120, 14500,
                "Overlooking the Poona Club Golf Course with Ukiyo modern Japanese dining, Alta Vida rooftop, and Ritz-Carlton Spa.",
                List.of("Poona Club Golf View", "Ukiyo Japanese", "The Ritz-Carlton Spa", "Rooftop Lounge"),
                List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Ritz-Carlton Pune Golf Suite 360",
                List.of(createRoom("rm-01", "Deluxe Golf View", RoomCategory.DELUXE, 6500, "King", 2, 550, PANORAMA_DELUXE),
                        createRoom("rm-02", "The Ritz-Carlton Suite", RoomCategory.SUITE, 28000, "King", 4, 1400, PANORAMA_PALACE))));

        list.add(createHotel("htl-ccu-01", "ITC Royal Bengal Kolkata", "Kolkata", "CCU", "West Bengal", "India",
                "1 JBS Haldane Avenue, Tangra, Kolkata 700046", 22.5448, 88.3982, 5, 4.9, 2100, 10500,
                "Grand aristocratic tribute to Bengal's heritage with 456 palatial rooms, Royal Vega dining, and Kaya Kalp spa.",
                List.of("Palatial Bengal Architecture", "Royal Vega Dining", "Kaya Kalp Spa", "Outdoor Pool"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "ITC Royal Bengal Presidential 360",
                List.of(createRoom("rm-01", "Executive Club", RoomCategory.STANDARD, 4200, "Twin", 2, 380, null),
                        createRoom("rm-02", "ITC One Luxury", RoomCategory.PREMIUM, 7800, "King", 2, 520, PANORAMA_SUITE),
                        createRoom("rm-03", "Grand Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 40000, "King", 6, 2100, PANORAMA_PALACE))));

        list.add(createHotel("htl-ccu-02", "The Oberoi Grand Kolkata", "Kolkata", "CCU", "West Bengal", "India",
                "15 Jawaharlal Nehru Road, New Market, Kolkata 700013", 22.5604, 88.3512, 5, 4.8, 1450, 9500,
                "The 'Grande Dame of Chowringhee' since 1887 with tranquil courtyard palm trees, Baan Thai dining, and Oberoi Spa.",
                List.of("1887 Grande Dame", "Baan Thai Dining", "Courtyard Swimming Pool", "Oberoi Spa"),
                List.of("https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Oberoi Grand Heritage Suite 360",
                List.of(createRoom("rm-01", "Deluxe Heritage Room", RoomCategory.STANDARD, 4500, "King", 2, 420, PANORAMA_DELUXE),
                        createRoom("rm-02", "Curzon Presidential Suite", RoomCategory.SUITE, 22000, "King", 4, 1100, PANORAMA_PALACE))));

        list.add(createHotel("htl-oot-01", "Savoy - IHCL SeleQtions Ooty", "Ooty", "CJB", "Tamil Nadu", "India",
                "77 Sylks Road, Ooty 643001", 11.4112, 76.6954, 5, 4.8, 860, 9500,
                "180-year-old British colonial hill station retreat in the Nilgiris with roaring wood fireplaces and high tea gardens.",
                List.of("180-yr Colonial Heritage", "Wood Fireplaces", "Nilgiri Mountain Views", "High Tea Gardens"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Savoy Colonial Fireplace Suite 360",
                List.of(createRoom("rm-01", "Heritage Cottage", RoomCategory.STANDARD, 4800, "King", 2, 400, null),
                        createRoom("rm-02", "Savoy Fireplace Suite", RoomCategory.SUITE, 14500, "King", 3, 850, PANORAMA_VILLA))));

        list.add(createHotel("htl-mys-01", "Lalitha Mahal Palace Hotel Mysore", "Mysore", "MYQ", "Karnataka", "India",
                "Lalitha Mahal Nagar, Siddhartha Layout, Mysuru 570011", 12.2981, 76.6912, 5, 4.7, 720, 8000,
                "Regal Italianate white palace built in 1921 by the Maharaja of Mysore under the Chamundi Hills with Belgian glass chandeliers.",
                List.of("1921 Maharaja Palace", "Belgian Chandeliers", "Chamundi Hill Views", "Heritage Billiards"),
                List.of("https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Lalitha Mahal Viceroy Suite 360",
                List.of(createRoom("rm-01", "Heritage Room", RoomCategory.STANDARD, 3500, "Queen", 2, 380, null),
                        createRoom("rm-02", "Viceroy Suite", RoomCategory.SUITE, 16000, "King", 4, 1100, PANORAMA_PALACE))));

        list.add(createHotel("htl-pon-01", "Palais de Mahe - CGH Earth", "Pondicherry", "PNY", "Puducherry", "India",
                "4 Bussy Street, White Town, Puducherry 605001", 11.9312, 79.8345, 5, 4.9, 810, 11000,
                "French Colonial quarter jewel in White Town with courtyard plunge pool, high-ceiling verandahs, and Promenade sea breeze.",
                List.of("French Quarter White Town", "Courtyard Pool", "Les Alizes Rooftop", "Promenade Beach Steps"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Palais de Mahe French Suite 360",
                List.of(createRoom("rm-01", "Deluxe Verandah Room", RoomCategory.STANDARD, 5200, "King", 2, 450, PANORAMA_DELUXE),
                        createRoom("rm-02", "Palais Heritage Suite", RoomCategory.SUITE, 15000, "King", 3, 900, PANORAMA_VILLA))));

        list.add(createHotel("htl-tir-01", "Marasa Sarovar Premiere Tirupati", "Tirupati", "TIR", "Andhra Pradesh", "India",
                "Upadhyaya Nagar, Karakambadi Road, Tirupati 517507", 13.6391, 79.4482, 5, 4.7, 1140, 5800,
                "World's first Dasavatara-themed luxury hotel at the foothills of Tirumala with lotus ponds and temple shuttle.",
                List.of("Dasavatara Theme", "Lotus Ponds", "Tirumala Temple Shuttle", "Pool"),
                List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_DELUXE, "Marasa Tirupati Deluxe 360",
                List.of(createRoom("rm-01", "Deluxe Room", RoomCategory.STANDARD, 2800, "Twin", 2, 340, null),
                        createRoom("rm-02", "Executive Suite", RoomCategory.SUITE, 7500, "King", 3, 750, PANORAMA_DELUXE))));

        list.add(createHotel("htl-vns-01", "BrijRama Palace Varanasi", "Varanasi", "VNS", "Uttar Pradesh", "India",
                "Darbhanga Ghat, Dashashwamedh, Varanasi 221001", 25.3054, 83.0112, 5, 5.0, 1680, 24000,
                "18th-century palace directly on Darbhanga Ghat over River Ganga, reached by private royal bajra boat with private Ganga Aarti access.",
                List.of("Ganga Ghat Waterfront", "Private Royal Bajra Boat", "Live Classical Sitar", "Ganga Aarti Views"),
                List.of("https://images.unsplash.com/photo-1549294413-26f195200c16?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "BrijRama Palace Ganga Suite 360",
                List.of(createRoom("rm-01", "Darbhanga Deluxe", RoomCategory.DELUXE, 11000, "King", 2, 480, PANORAMA_OCEAN),
                        createRoom("rm-02", "Maharaja Ganga View Suite", RoomCategory.SUITE, 34000, "King", 4, 1200, PANORAMA_PALACE))));

        list.add(createHotel("htl-rsh-01", "Ananda in the Himalayas", "Rishikesh", "DED", "Uttarakhand", "India",
                "The Palace Estate, Narendra Nagar, Tehri Garhwal 249175", 30.1624, 78.2912, 5, 5.0, 1920, 48000,
                "World's leading destination wellness spa set across 100 acres of Himalayan sal forest overlooking the holy Ganges valley.",
                List.of("World #1 Wellness Spa", "Ayurvedic Doctors", "Himalayan Valley Views", "Outdoor Hydro Pool", "Yoga Pavilion"),
                List.of("https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Ananda Himalayan Valley Suite 360",
                List.of(createRoom("rm-01", "Palace View Room", RoomCategory.DELUXE, 22000, "King", 2, 550, PANORAMA_DELUXE),
                        createRoom("rm-02", "Ananda Suite", RoomCategory.SUITE, 55000, "King", 3, 1200, PANORAMA_VILLA),
                        createRoom("rm-03", "Viceroy Villa with Private Pool", RoomCategory.VILLA, 110000, "King", 6, 2800, PANORAMA_PALACE))));

        // ==========================================
        // 11. INTERNATIONAL DESTINATIONS (Dubai, Singapore, Bangkok, KL, London, Paris, Rome, NYC, Tokyo, Bali, Maldives, Istanbul)
        // ==========================================
        list.add(createHotel("htl-dxb-01", "Burj Al Arab Jumeirah Dubai", "Dubai", "DXB", "Dubai", "United Arab Emirates",
                "Jumeirah St, Umm Suqeim 3, Dubai", 25.1412, 55.1852, 5, 5.0, 4200, 65000,
                "The world's most iconic sail-shaped 7-star ultra-luxury hotel on its own private island with duplex suites, gold leaf interiors, and Talise Spa.",
                List.of("Private Island", "Duplex Suites", "Rolls-Royce Chauffeur", "Talise Spa", "Private Beach", "Helipad"),
                List.of("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Burj Al Arab Royal Duplex Suite 360",
                List.of(createRoom("rm-01", "Deluxe One-Bedroom Duplex", RoomCategory.SUITE, 42000, "King", 2, 1700, PANORAMA_OCEAN),
                        createRoom("rm-02", "Royal Two-Bedroom Suite", RoomCategory.PRESIDENTIAL_SUITE, 140000, "King", 6, 4200, PANORAMA_PALACE))));

        list.add(createHotel("htl-dxb-02", "Atlantis The Palm Dubai", "Dubai", "DXB", "Dubai", "United Arab Emirates",
                "Crescent Rd, The Palm Jumeirah, Dubai", 25.1304, 55.1171, 5, 4.8, 3800, 32000,
                "Crown of Palm Jumeirah with Aquaventure Waterpark access, underwater Lost Chambers aquarium, and Nobu restaurant.",
                List.of("Aquaventure Access", "Lost Chambers Aquarium", "Nobu Restaurant", "Private Beach"),
                List.of("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Atlantis Underwater Suite 360",
                List.of(createRoom("rm-01", "Ocean King Room", RoomCategory.STANDARD, 16000, "King", 2, 500, PANORAMA_OCEAN),
                        createRoom("rm-02", "Underwater Signature Suite", RoomCategory.SUITE, 85000, "King", 4, 1800, PANORAMA_PALACE))));

        list.add(createHotel("htl-sin-01", "Marina Bay Sands Singapore", "Singapore", "SIN", "Central", "Singapore",
                "10 Bayfront Avenue, Singapore 018956", 1.2834, 103.8607, 5, 4.9, 5200, 38000,
                "Iconic architectural marvel crowned by the world's largest rooftop infinity pool spanning 57 levels above Singapore's skyline.",
                List.of("Level 57 Rooftop Infinity Pool", "SkyPark Observation Deck", "Casino Access", "Banyan Tree Spa"),
                List.of("https://images.unsplash.com/photo-1525625293386-3f8f99389edd?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Marina Bay Sands Sky Suite 360",
                List.of(createRoom("rm-01", "Deluxe Harbor View", RoomCategory.DELUXE, 18000, "King", 2, 450, PANORAMA_OCEAN),
                        createRoom("rm-02", "Sands Premier Suite", RoomCategory.SUITE, 48000, "King", 4, 1200, PANORAMA_PALACE))));

        list.add(createHotel("htl-sin-02", "Raffles Hotel Singapore", "Singapore", "SIN", "Central", "Singapore",
                "1 Beach Road, Singapore 189673", 1.2949, 103.8545, 5, 5.0, 2400, 45000,
                "Legendary 1887 colonial grande dame and birthplace of the Singapore Sling, with lush tropical courtyards and Raffles Butler care.",
                List.of("1887 Heritage Legend", "Birthplace of Singapore Sling", "Raffles Butler Service", "Spa"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Raffles Presidential Suite 360",
                List.of(createRoom("rm-01", "Courtyard Suite", RoomCategory.SUITE, 28000, "King", 2, 650, PANORAMA_SUITE),
                        createRoom("rm-02", "Presidential Suite", RoomCategory.PRESIDENTIAL_SUITE, 95000, "King", 6, 2600, PANORAMA_PALACE))));

        list.add(createHotel("htl-bkk-01", "The Peninsula Bangkok", "Bangkok", "BKK", "Bangkok", "Thailand",
                "333 Charoen Nakhon Rd, Khlong San, Bangkok 10600", 13.7226, 100.5108, 5, 4.9, 2100, 16000,
                "Luxury riverside sanctuary on the Chao Phraya River with three-tiered swimming pool, private ferry boats, and Peninsula Spa.",
                List.of("Chao Phraya River View", "Three-Tiered Pool", "Private River Ferry", "Peninsula Spa"),
                List.of("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_OCEAN, "Peninsula Bangkok River Suite 360",
                List.of(createRoom("rm-01", "Deluxe River View", RoomCategory.STANDARD, 7500, "King", 2, 480, PANORAMA_OCEAN),
                        createRoom("rm-02", "Grand Terrace Suite", RoomCategory.SUITE, 24000, "King", 4, 1150, PANORAMA_SUITE))));

        list.add(createHotel("htl-lon-01", "The Ritz London", "London", "LHR", "Greater London", "United Kingdom",
                "150 Piccadilly, St. James's, London W1J 9BR", 51.5071, -0.1416, 5, 4.9, 2900, 52000,
                "World-famous Neoclassical palace overlooking Green Park in Mayfair with legendary Afternoon Tea and Michelin-starred dining.",
                List.of("World-Famous Afternoon Tea", "Michelin Star Dining", "Piccadilly Mayfair", "The Ritz Club"),
                List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "The Ritz London Royal Suite 360",
                List.of(createRoom("rm-01", "Superior Queen Room", RoomCategory.STANDARD, 26000, "Queen", 2, 350, null),
                        createRoom("rm-02", "Piccadilly Executive Suite", RoomCategory.SUITE, 62000, "King", 4, 1100, PANORAMA_PALACE))));

        list.add(createHotel("htl-par-01", "Four Seasons Hotel George V Paris", "Paris", "CDG", "Île-de-France", "France",
                "31 Avenue George V, 75008 Paris", 48.8688, 2.3009, 5, 5.0, 3100, 68000,
                "Art Deco icon on the Golden Triangle off Champs-Élysées with 5 Michelin stars across three restaurants and Jeff Leatham floral art.",
                List.of("5 Michelin Stars", "Golden Triangle Location", "Jeff Leatham Florals", "Spa with Marble Pool"),
                List.of("https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "George V Parisian Penthouse 360",
                List.of(createRoom("rm-01", "Deluxe Courtyard Room", RoomCategory.DELUXE, 32000, "King", 2, 450, PANORAMA_DELUXE),
                        createRoom("rm-02", "Eiffel Tower View Penthouse", RoomCategory.PRESIDENTIAL_SUITE, 160000, "King", 6, 2400, PANORAMA_PALACE))));

        list.add(createHotel("htl-nyc-01", "The Plaza New York", "New York", "JFK", "New York", "United States",
                "768 5th Ave, New York, NY 10019", 40.7645, -73.9744, 5, 4.9, 4100, 58000,
                "Timeless 1907 Central Park South luxury icon featuring The Palm Court, Guerlain Spa, and Fifth Avenue shopping.",
                List.of("Central Park South Views", "The Palm Court", "Guerlain Spa", "Fifth Avenue Flagship"),
                List.of("https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_SUITE, "The Plaza Central Park Suite 360",
                List.of(createRoom("rm-01", "Plaza King Room", RoomCategory.STANDARD, 28000, "King", 2, 480, PANORAMA_DELUXE),
                        createRoom("rm-02", "Central Park Suite", RoomCategory.SUITE, 75000, "King", 4, 1250, PANORAMA_PALACE))));

        list.add(createHotel("htl-tyo-01", "Aman Tokyo", "Tokyo", "HND", "Tokyo", "Japan",
                "The Otemachi Tower, 1-5-6 Otemachi, Chiyoda City, Tokyo 100-0004", 35.6882, 139.7648, 5, 5.0, 2200, 55000,
                "Sanctuary high above Otemachi with traditional washi paper lanterns, volcanic basalt pool, and Mount Fuji vistas on clear days.",
                List.of("Volcanic Basalt Pool", "Mount Fuji Vistas", "Aman Spa", "Traditional Engawa Suites"),
                List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_PALACE, "Aman Tokyo Panoramic Suite 360",
                List.of(createRoom("rm-01", "Deluxe Room", RoomCategory.DELUXE, 28000, "King", 2, 750, PANORAMA_DELUXE),
                        createRoom("rm-02", "Aman Suite with Mount Fuji View", RoomCategory.PRESIDENTIAL_SUITE, 120000, "King", 4, 1800, PANORAMA_PALACE))));

        list.add(createHotel("htl-dps-01", "The Mulia Bali", "Bali", "DPS", "Bali", "Indonesia",
                "Jl. Raya Nusa Dua Selatan Kawasan Sawangan, Nusa Dua, Bali 80363", -8.8242, 115.2185, 5, 5.0, 3400, 28000,
                "Ultra-high-end beachfront all-suite resort along Nusa Dua bay with iconic Oasis oceanfront pool flanked by statues.",
                List.of("Oasis Oceanfront Pool", "Private Nusa Dua Beach", "Mulia Spa", "The Cafe 7 Theatres"),
                List.of("https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "The Mulia Oceanfront Pool Villa 360",
                List.of(createRoom("rm-01", "Mulia Signature Ocean Court", RoomCategory.DELUXE, 14000, "King", 2, 600, PANORAMA_OCEAN),
                        createRoom("rm-02", "Mulia Mansion Beachfront Villa", RoomCategory.VILLA, 65000, "King", 6, 2800, PANORAMA_VILLA))));

        list.add(createHotel("htl-mle-01", "Soneva Fushi Maldives", "Maldives", "MLE", "Baa Atoll", "Maldives",
                "Kunfunadhoo Island, Baa Atoll, Maldives 06170", 5.1118, 73.0805, 5, 5.0, 2100, 85000,
                "Legendary eco-luxury UNESCO Biosphere Baa Atoll retreat with overwater water-slide villas, open-air cinema, and observatory.",
                List.of("Overwater Water Slides", "UNESCO Biosphere Atoll", "Cinema Paradiso", "Stargazing Observatory", "Private Butler"),
                List.of("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
                        "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                PANORAMA_VILLA, "Soneva Fushi Overwater Water Slide Villa 360",
                List.of(createRoom("rm-01", "Crusoe Villa with Pool", RoomCategory.DELUXE, 45000, "King", 2, 1200, PANORAMA_VILLA),
                        createRoom("rm-02", "Water Retreat with Slide", RoomCategory.VILLA, 135000, "King", 6, 3500, PANORAMA_VILLA))));

        // Additional 70+ Regional & Destination Gems Generated Deterministically to achieve 120+ unique records
        generateExpandedDestinations(list);

        return list;
    }

    private static void generateExpandedDestinations(List<Hotel> list) {
        String[][] additionalCities = {
                {"Delhi", "DEL", "Delhi", "India", "28.6139", "77.2090"},
                {"Mumbai", "BOM", "Maharashtra", "India", "19.0760", "72.8777"},
                {"Bangalore", "BLR", "Karnataka", "India", "12.9716", "77.5946"},
                {"Chennai", "MAA", "Tamil Nadu", "India", "13.0827", "80.2707"},
                {"Hyderabad", "HYD", "Telangana", "India", "17.3850", "78.4867"},
                {"Goa", "GOI", "Goa", "India", "15.2993", "74.1240"},
                {"Kochi", "COK", "Kerala", "India", "9.9312", "76.2673"},
                {"Jaipur", "JAI", "Rajasthan", "India", "26.9124", "75.7873"},
                {"Udaipur", "UDR", "Rajasthan", "India", "24.5854", "73.7125"},
                {"Coimbatore", "CJB", "Tamil Nadu", "India", "11.0168", "76.9558"},
                {"Madurai", "IXM", "Tamil Nadu", "India", "9.9252", "78.1198"},
                {"Ahmedabad", "AMD", "Gujarat", "India", "23.0225", "72.5714"},
                {"Pune", "PNQ", "Maharashtra", "India", "18.5204", "73.8567"},
                {"Kolkata", "CCU", "West Bengal", "India", "22.5726", "88.3639"},
                {"Ooty", "CJB", "Tamil Nadu", "India", "11.4102", "76.6950"},
                {"Mysore", "MYQ", "Karnataka", "India", "12.2958", "76.6394"},
                {"Pondicherry", "PNY", "Puducherry", "India", "11.9416", "79.8083"},
                {"Tirupati", "TIR", "Andhra Pradesh", "India", "13.6288", "79.4192"},
                {"Varanasi", "VNS", "Uttar Pradesh", "India", "25.3176", "82.9739"},
                {"Rishikesh", "DED", "Uttarakhand", "India", "30.0869", "78.2676"},
                {"Dubai", "DXB", "Dubai", "United Arab Emirates", "25.2048", "55.2708"},
                {"Singapore", "SIN", "Central", "Singapore", "1.3521", "103.8198"},
                {"Bangkok", "BKK", "Bangkok", "Thailand", "13.7563", "100.5018"},
                {"Kuala Lumpur", "KUL", "Federal Territory", "Malaysia", "3.1390", "101.6869"},
                {"London", "LHR", "Greater London", "United Kingdom", "51.5074", "-0.1278"},
                {"Paris", "CDG", "Île-de-France", "France", "48.8566", "2.3522"},
                {"Rome", "FCO", "Lazio", "Italy", "41.9028", "12.4964"},
                {"New York", "JFK", "New York", "United States", "40.7128", "-74.0060"},
                {"Tokyo", "HND", "Tokyo", "Japan", "35.6762", "139.6503"},
                {"Bali", "DPS", "Bali", "Indonesia", "-8.3405", "115.0920"},
                {"Maldives", "MLE", "Baa Atoll", "Maldives", "3.2028", "73.2207"},
                {"Istanbul", "IST", "Istanbul", "Turkey", "41.0082", "28.9784"}
        };

        String[] brandPrefixes = {"Grand", "Royal", "The Grand Heritage", "Residency", "Palace Resort & Spa"};

        int idCounter = 100;
        for (String[] cityInfo : additionalCities) {
            String city = cityInfo[0];
            String airport = cityInfo[1];
            String state = cityInfo[2];
            String country = cityInfo[3];
            double lat = Double.parseDouble(cityInfo[4]);
            double lng = Double.parseDouble(cityInfo[5]);

            for (int i = 0; i < 2; i++) {
                idCounter++;
                String brand = brandPrefixes[(idCounter) % brandPrefixes.length];
                String hotelName = brand + " " + city + " " + (i == 0 ? "Boutique Suites" : "Resort");
                String hotelId = "htl-exp-" + idCounter;
                int stars = (idCounter % 2 == 0) ? 5 : 4;
                double rating = 4.3 + ((idCounter % 6) * 0.1);
                int reviews = 150 + ((idCounter * 17) % 1200);
                double basePrice = 3200 + ((idCounter * 130) % 9500);

                String pano = (stars == 5) ? PANORAMA_SUITE : PANORAMA_DELUXE;

                list.add(createHotel(
                        hotelId,
                        hotelName,
                        city,
                        airport,
                        state,
                        country,
                        "Central Promenade, " + city + ", " + state,
                        lat + ((i + 1) * 0.005),
                        lng + ((i + 1) * 0.005),
                        stars,
                        rating,
                        reviews,
                        basePrice,
                        "Experience authentic hospitality, personalized guest services, and signature culinary offerings in the heart of " + city + ".",
                        List.of("Free High-Speed Wi-Fi", "Swimming Pool", "Spa & Wellness", "24/7 In-Room Dining", "Valet Parking", "Concierge"),
                        List.of(
                                "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"
                        ),
                        pano,
                        hotelName + " 360 Virtual Tour",
                        List.of(
                                createRoom("rm-01", "Deluxe King Room", RoomCategory.STANDARD, (int) (basePrice * 0.6), "King", 2, 360, PANORAMA_DELUXE),
                                createRoom("rm-02", "Executive Premier Room", RoomCategory.PREMIUM, (int) (basePrice * 0.95), "King", 2, 480, PANORAMA_SUITE),
                                createRoom("rm-03", "Royal Presidential Suite", RoomCategory.SUITE, (int) (basePrice * 2.4), "King", 4, 950, PANORAMA_PALACE)
                        )
                ));
            }
        }
    }

    private static Hotel createHotel(String id, String name, String city, String airportCode,
                                     String state, String country, String addressLine,
                                     double lat, double lng, int stars, double rating, int reviews, double basePrice,
                                     String desc, List<String> amenities, List<String> images,
                                     String panoUrl, String panoTitle, List<RoomType> rooms) {
        VirtualTour tour = null;
        if (panoUrl != null && !panoUrl.isBlank()) {
            tour = VirtualTour.builder()
                    .enabled(true)
                    .panoramaUrl(panoUrl)
                    .thumbnailUrl(images.isEmpty() ? panoUrl : images.get(0))
                    .title(panoTitle != null ? panoTitle : name + " 360 Virtual Tour")
                    .description("Drag with your mouse or swipe on mobile to immerse in this 360° view.")
                    .roomCategory("HOTEL_PANORAMA")
                    .build();
        }

        HotelAddress address = HotelAddress.builder()
                .line1(addressLine)
                .city(city)
                .state(state)
                .country(country)
                .latitude(lat)
                .longitude(lng)
                .build();

        return Hotel.builder()
                .id(id)
                .name(name)
                .address(address)
                .nearestAirportCode(airportCode)
                .starRating(stars)
                .averageRating(rating)
                .totalReviews(reviews)
                .baseNightlyRate(BigDecimal.valueOf(basePrice))
                .currency("INR")
                .description(desc)
                .amenities(amenities)
                .imageUrls(images)
                .contactInfo(new HotelContactInfo("+91-1800-SMART-TRAVEL", "reservations@" + id + ".smarttravel.com", "https://smarttravel.com/hotels/" + id))
                .virtualTour(tour)
                .roomTypes(rooms)
                .active(true)
                .build();
    }

    private static RoomType createRoom(String id, String name, RoomCategory category,
                                       int price, String bedType, int occupancy, int sqft, String panoUrl) {
        BigDecimal nightlyRate = BigDecimal.valueOf(price);
        BigDecimal tax = nightlyRate.multiply(BigDecimal.valueOf(0.12));
        BigDecimal total = nightlyRate.add(tax);

        VirtualTour tour = null;
        if (panoUrl != null && !panoUrl.isBlank()) {
            tour = VirtualTour.builder()
                    .enabled(true)
                    .panoramaUrl(panoUrl)
                    .thumbnailUrl(panoUrl)
                    .title(name + " 360° Room Perspective")
                    .description("Interactive 360° room walkthrough.")
                    .roomCategory(category.name())
                    .build();
        }

        return RoomType.builder()
                .id(id)
                .name(name)
                .category(category)
                .description(name + " equipped with luxury bedding, high-speed Wi-Fi, and climate control.")
                .totalRooms(18)
                .availableRooms(11)
                .maxOccupancy(occupancy)
                .bedType(bedType)
                .sizeInSqFt(sqft)
                .nightlyRate(nightlyRate)
                .taxAmount(tax)
                .totalNightlyRate(total)
                .currency("INR")
                .amenities(List.of("Free High-Speed Wi-Fi", "HD Smart TV", "Mini-Bar", "24/7 Room Service", "Air Conditioning", "Rain Shower"))
                .imageUrls(List.of(panoUrl != null ? panoUrl : "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"))
                .breakfastIncluded(category == RoomCategory.SUITE || category == RoomCategory.VILLA || category == RoomCategory.PRESIDENTIAL_SUITE)
                .refundable(true)
                .virtualTour(tour)
                .build();
    }
}
