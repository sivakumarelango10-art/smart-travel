# SmartTravel Platform — Database Design & MongoDB Schema Specification

> **Database:** MongoDB 7.x  
> **ORM / Driver:** Spring Data MongoDB 4.3.x  
> **Schema Pattern:** Document-oriented with embedded sub-documents for fast queries and discrete collections for high-cardinality entities.

---

## 1. Collections Overview

| Collection Name | Purpose | Primary Access Pattern |
|---|---|---|
| `users` | Traveler & Staff identities, credentials, role claims, and saved preferences. | Indexed by `email` (unique) and `_id`. |
| `flights` | Static flight catalog, schedules, airline info, aircraft configuration, and base fares. | Search by `departureAirport.code`, `arrivalAirport.code`, `departureTime`. |
| `flight_statuses` | Real-time telemetry, live delay status, revised departure/arrival ETAs, gate/terminal. | Polled and streamed by `flightId` and `flightNumber`. |
| `flight_trackings` | User flight subscriptions for in-app and push alert routing. | Compound index on `{ userId: 1, flightId: 1 }`. |
| `seats` | Aircraft seat layouts, seat classification, pricing addons, and atomic lock states. | Query by `flightId`, updated atomically on selection. |
| `hotels` | Hotel catalog, geo-coordinates, star ratings, amenities, and image galleries. | Geospatial & faceted search by `city`, `starRating`, `location`. |
| `rooms` | Hotel room categories, capacity, pricing per night, and bed configuration. | Query by `hotelId` and `roomType`. |
| `price_rules` | Configurable dynamic pricing rules, surge factors, and seasonal multipliers. | Evaluated during pricing calculations by `targetType` and priority. |
| `price_histories` | Time-series historical price recordings for trend charts and auditability. | Query by `{ productId: 1, recordedAt: -1 }`. |
| `price_freezes` | 30-minute guaranteed price locks with cryptographic tokens and TTL expiration. | Query by `freezeToken`, TTL index on `expiresAt`. |
| `bookings` | Core transactional booking records, passenger info, itemized pricing snapshots. | Query by `bookingReference` (unique) and `userId`. |
| `payments` | Settlement records, transaction hashes, and mock/external gateway responses. | Query by `bookingId` and `transactionRef`. |
| `cancellation_policies` | Configurable refund rules and time-based penalty tiers for flights and hotels. | Evaluated during cancellation preview and refund execution. |
| `refunds` | Refund lifecycle tracking, fee deductions, payout amounts, and audit timelines. | Query by `bookingId` and `userId`. |
| `reviews` | 1-5 star ratings, feedback text, photo URLs, verified booking flag, and helpful votes. | Query by `{ targetType: 1, targetId: 1, status: 1 }`. |
| `review_replies` | Official replies to reviews by hotel owners or airline representatives. | Query by `reviewId`. |
| `review_reports` | Community flags for offensive/inappropriate reviews in moderation queues. | Query by `reviewId` and `status`. |
| `user_preferences` | Reusable traveler defaults (preferred seat type, room class, diet). | Query by `userId`. |
| `search_histories` | Recent search logs used as signals for personalized recommendations. | Query by `userId` sorted by `timestamp` desc. |
| `recommendations` | Generated recommendations with transparent "Why this" rationales and user feedback. | Query by `userId` sorted by `score` desc. |
| `notifications` | In-app notification alerts for delay updates, refund completions, and freeze warnings. | Query by `{ userId: 1, isRead: 1 }`. |

---

## 2. Collection Schemas & Example Documents

### 2.1 `users`
```json
{
  "_id": "66c1e101f1a2b3c4d5e6f701",
  "email": "traveler@example.com",
  "passwordHash": "$2a$12$e8Y7z9V8...BCryptHash...",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+919876543210",
  "roles": ["ROLE_USER"],
  "preferences": {
    "preferredSeatType": "WINDOW",
    "preferredRoomType": "DELUXE",
    "favoriteDestinations": ["Goa", "Bali", "Dubai"]
  },
  "createdAt": "2026-08-18T10:00:00Z",
  "updatedAt": "2026-08-18T10:00:00Z"
}
```

### 2.2 `flights` & `flight_statuses`
```json
// flights
{
  "_id": "66c1e101f1a2b3c4d5e6f702",
  "flightNumber": "ST-101",
  "airline": "SmartAir",
  "airlineCode": "SA",
  "departureAirport": {
    "code": "DEL",
    "name": "Indira Gandhi International Airport",
    "city": "New Delhi",
    "terminal": "T3"
  },
  "arrivalAirport": {
    "code": "BOM",
    "name": "Chhatrapati Shivaji Maharaj International Airport",
    "city": "Mumbai",
    "terminal": "T2"
  },
  "departureTime": "2026-08-20T18:30:00Z",
  "arrivalTime": "2026-08-20T20:45:00Z",
  "durationMinutes": 135,
  "aircraftModel": "Airbus A321neo",
  "basePrice": 5000.0,
  "totalSeats": 180,
  "availableSeats": 42,
  "cabinClasses": ["ECONOMY", "PREMIUM_ECONOMY", "BUSINESS"],
  "status": "ACTIVE"
}

// flight_statuses
{
  "_id": "66c1e101f1a2b3c4d5e6f703",
  "flightId": "66c1e101f1a2b3c4d5e6f702",
  "flightNumber": "ST-101",
  "currentStatus": "DELAYED",
  "originalDeparture": "2026-08-20T18:30:00Z",
  "revisedDeparture": "2026-08-20T19:30:00Z",
  "delayMinutes": 60,
  "delayReason": "Weather conditions and heavy air traffic",
  "originalArrival": "2026-08-20T20:45:00Z",
  "estimatedArrival": "2026-08-20T21:45:00Z",
  "gate": "A12",
  "terminal": "T3",
  "baggageBelt": "4",
  "updatedAt": "2026-08-18T16:00:00Z"
}
```

### 2.3 `seats`
```json
{
  "_id": "66c1e101f1a2b3c4d5e6f704",
  "flightId": "66c1e101f1a2b3c4d5e6f702",
  "seatNumber": "12A",
  "row": 12,
  "column": "A",
  "seatClass": "ECONOMY",
  "seatType": "EXTRA_LEGROOM",
  "basePrice": 5000.0,
  "additionalFee": 850.0,
  "status": "AVAILABLE",
  "lockedByUserId": null,
  "lockExpiresAt": null
}
```

### 2.4 `price_rules` & `price_freezes`
```json
// price_freezes
{
  "_id": "66c1e101f1a2b3c4d5e6f705",
  "userId": "66c1e101f1a2b3c4d5e6f701",
  "productType": "FLIGHT",
  "productId": "66c1e101f1a2b3c4d5e6f702",
  "freezeToken": "frz_89a7f34cb12948e",
  "basePrice": 5000.0,
  "frozenPrice": 6750.0,
  "adjustmentsSnapshot": [
    { "factorName": "Holiday Demand", "amount": 1000.0 },
    { "factorName": "High Booking Velocity", "amount": 750.0 }
  ],
  "frozenAt": "2026-08-18T16:00:00Z",
  "expiresAt": "2026-08-18T16:30:00Z",
  "status": "ACTIVE"
}
```

### 2.5 `bookings`, `cancellation_policies` & `refunds`
```json
// refunds
{
  "_id": "66c1e101f1a2b3c4d5e6f706",
  "bookingId": "66c1e101f1a2b3c4d5e6f707",
  "paymentId": "66c1e101f1a2b3c4d5e6f708",
  "userId": "66c1e101f1a2b3c4d5e6f701",
  "originalBookingAmount": 6750.0,
  "cancellationCharge": 675.0,
  "refundAmount": 6075.0,
  "refundReason": "Personal emergency / Travel plan changed",
  "status": "COMPLETED",
  "timeline": [
    {
      "status": "PENDING",
      "timestamp": "2026-08-18T16:05:00Z",
      "note": "Cancellation requested by traveler",
      "actor": "TRAVELER"
    },
    {
      "status": "PROCESSING",
      "timestamp": "2026-08-18T16:05:05Z",
      "note": "Cancellation policy applied: Tier 1 (>48 hrs, 90% refund)",
      "actor": "POLICY_ENGINE"
    },
    {
      "status": "COMPLETED",
      "timestamp": "2026-08-18T16:05:10Z",
      "note": "Refund of ₹6,075 credited to original payment source (Mock Gateway TX: REF-99482)",
      "actor": "PAYMENT_GATEWAY"
    }
  ],
  "transactionRef": "REF-99482",
  "expectedTimelineDays": 1,
  "createdAt": "2026-08-18T16:05:00Z",
  "updatedAt": "2026-08-18T16:05:10Z"
}
```

---
*End of Database Design Specification.*
