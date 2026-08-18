# SmartTravel Platform — REST & Real-Time API Specification

> **Base URL:** `/api/v1`  
> **WebSocket URL:** `/ws` (STOMP Protocol over SockJS)  
> **Auth Protocol:** Bearer Token (`Authorization: Bearer <JWT_ACCESS_TOKEN>`)

---

## 1. Authentication & User Profile APIs

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register new user account |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user & issue JWT |
| `POST` | `/api/v1/auth/refresh-token` | Public | Exchange refresh token for new access token |
| `GET` | `/api/v1/user/profile` | `ROLE_USER` | Fetch current user profile & travel preferences |
| `PUT` | `/api/v1/user/preferences` | `ROLE_USER` | Update seat & room preferences |

---

## 2. Flight & Live Flight Status APIs (Feature 1)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/flights` | Public | Search flights by origin, destination, date, cabin class |
| `GET` | `/api/v1/flights/{id}` | Public | Get flight details & schedule |
| `GET` | `/api/v1/flights/{id}/status` | Public | Get real-time flight telemetry (delay, revised ETA, gate) |
| `POST` | `/api/v1/flights/{id}/track` | `ROLE_USER` | Subscribe user dashboard to flight status updates |
| `DELETE` | `/api/v1/flights/{id}/track` | `ROLE_USER` | Unsubscribe from flight updates |
| `GET` | `/api/v1/flights/tracked` | `ROLE_USER` | List all flights currently tracked by user |
| `POST` | `/api/v1/admin/flights/{id}/status-simulation` | `ROLE_ADMIN` | Manually inject a simulated flight delay/status shift |

### WebSocket Real-time Topics:
- **Broadcast:** `/topic/flights/{flightNumber}/status` — Real-time payload with status, revised departure, delay reason, ETA.
- **User Alerts:** `/user/queue/notifications` — Direct alert notifications for tracked flights.

---

## 3. Dynamic Pricing & Price Freeze APIs (Feature 2)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/pricing/{productType}/{productId}` | Public | Calculate real-time dynamic price with transparent factor breakdown |
| `GET` | `/api/v1/pricing/{productType}/{productId}/history` | Public | Get historical price trend data points for charting |
| `POST` | `/api/v1/pricing/freeze` | `ROLE_USER` | Lock in current price for 30 minutes (returns `freezeToken`) |
| `GET` | `/api/v1/pricing/freeze/{freezeToken}` | `ROLE_USER` | Validate active freeze token & remaining TTL countdown |
| `GET` | `/api/v1/admin/pricing/rules` | `ROLE_ADMIN` | List all dynamic pricing factor rules |
| `POST` | `/api/v1/admin/pricing/rules` | `ROLE_ADMIN` | Create/update pricing factor multiplier rule |

---

## 4. Cancellation & Refund APIs (Feature 3)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/bookings/{id}/cancellation-preview` | `ROLE_USER` | Preview refund amount & cancellation charges before confirming |
| `POST` | `/api/v1/bookings/{id}/cancel` | `ROLE_USER` | Execute booking cancellation & initiate refund state machine |
| `GET` | `/api/v1/refunds/{id}` | `ROLE_USER` | Get refund details, current state, and audit timeline |
| `GET` | `/api/v1/user/refunds` | `ROLE_USER` | List all refunds requested by current user |
| `GET` | `/api/v1/admin/refunds` | `ROLE_ADMIN` | List all platform refund requests with filter by status |
| `POST` | `/api/v1/admin/refunds/{id}/retry` | `ROLE_ADMIN` | Manually retry failed refund transaction |

---

## 5. Seat & Room Selection APIs (Feature 4)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/flights/{id}/seats` | Public | Get dynamic aircraft seat map layout with prices & statuses |
| `POST` | `/api/v1/flights/{id}/seats/lock` | `ROLE_USER` | Acquire temporary 10-min lock on selected seat |
| `DELETE` | `/api/v1/flights/{id}/seats/lock/{seatNumber}` | `ROLE_USER` | Release locked seat |
| `GET` | `/api/v1/hotels` | Public | Search hotels with filters (city, stars, price, amenities) |
| `GET` | `/api/v1/hotels/{id}` | Public | Get hotel details and image gallery |
| `GET` | `/api/v1/hotels/{id}/rooms` | Public | List room types with live availability and pricing |

---

## 6. Reviews & Ratings APIs (Feature 5)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/reviews` | Public | Get reviews filtered by target (`FLIGHT`/`HOTEL`), rating, sort |
| `POST` | `/api/v1/reviews` | `ROLE_USER` | Submit review (requires verified completed booking) |
| `PUT` | `/api/v1/reviews/{id}` | `ROLE_USER` | Edit user's own review |
| `DELETE` | `/api/v1/reviews/{id}` | `ROLE_USER` | Delete user's own review |
| `POST` | `/api/v1/reviews/{id}/helpful` | `ROLE_USER` | Upvote review as helpful |
| `POST` | `/api/v1/reviews/{id}/reply` | `ROLE_USER` | Reply to an existing review |
| `POST` | `/api/v1/reviews/{id}/report` | `ROLE_USER` | Flag review for moderation review |
| `GET` | `/api/v1/admin/moderation/reviews` | `ROLE_MODERATOR` | Fetch flagged review moderation queue |
| `PUT` | `/api/v1/admin/moderation/reviews/{id}/decision` | `ROLE_MODERATOR` | Approve or Reject flagged review |

---

## 7. Personalized Recommendation APIs (Feature 6)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/recommendations` | `ROLE_USER` | Get personalized recommendations with "Why this recommendation?" explanations |
| `POST` | `/api/v1/recommendations/{id}/feedback` | `ROLE_USER` | Submit explicit feedback (`HELPFUL` or `IRRELEVANT`) |

---

## 8. Bookings & Checkout APIs

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/bookings` | `ROLE_USER` | Create booking from frozen rate or live calculated price |
| `GET` | `/api/v1/bookings/{id}` | `ROLE_USER` | Get booking summary & itinerary |
| `GET` | `/api/v1/user/bookings` | `ROLE_USER` | List all bookings for authenticated user |
| `POST` | `/api/v1/bookings/{id}/pay` | `ROLE_USER` | Submit mock/gateway payment settlement |

---

## 9. Centralized Error Responses

All API errors return RFC 7807 compatible JSON structure:

```json
{
  "timestamp": "2026-08-18T16:20:00.000Z",
  "status": 409,
  "error": "SEAT_ALREADY_LOCKED",
  "message": "Seat 12A has already been locked or booked by another traveler.",
  "path": "/api/v1/flights/66c1e101f1a2b3c4d5e6f702/seats/lock"
}
```

---
*End of REST & Real-Time API Specification.*
