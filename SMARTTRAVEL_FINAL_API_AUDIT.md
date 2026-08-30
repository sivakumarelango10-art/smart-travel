# SmartTravel Final API Contract & Security Audit

This document records the complete endpoint-by-endpoint audit across all REST APIs in the SmartTravel platform.

## API Endpoint Audit Matrix

| HTTP Method & Path | Auth Required | Role | Input Validation (`@Valid`) | Response Format | Status Codes | Rate Limit / Cache | Audit Status |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `POST /api/v1/auth/register` | Public | None | Email format, Password strength | `ApiResponse<AuthResponse>` | 201, 400, 409 | Rate Limited (Brute Force Guard) | **VERIFIED** |
| `POST /api/v1/auth/login` | Public | None | Email, Password | `ApiResponse<AuthResponse>` | 200, 401 | Rate Limited (Brute Force Guard) | **VERIFIED** |
| `POST /api/v1/auth/google` | Public | None | Google ID Token | `ApiResponse<AuthResponse>` | 200, 401 | Rate Limited | **VERIFIED** |
| `GET /api/v1/flights/search` | Public | None | Origin, Dest, Date, Class | `ApiResponse<Page<FlightResponse>>` | 200, 400 | Caffeine Cached (`CACHE_FLIGHTS`) | **VERIFIED** |
| `GET /api/v1/flights/{id}` | Public | None | Flight ID | `ApiResponse<FlightResponse>` | 200, 404 | Caffeine Cached | **VERIFIED** |
| `GET /api/v1/flights/{id}/seats` | Public | None | Flight ID | `ApiResponse<List<SeatResponse>>` | 200, 404 | Short TTL Cache | **VERIFIED** |
| `GET /api/v1/hotels/search` | Public | None | City, Star, Price Range | `ApiResponse<Page<HotelResponse>>` | 200, 400 | Caffeine Cached (`CACHE_HOTELS`) | **VERIFIED** |
| `GET /api/v1/hotels/{id}` | Public | None | Hotel ID | `ApiResponse<HotelResponse>` | 200, 404 | Caffeine Cached | **VERIFIED** |
| `GET /api/v1/pricing/flights/{id}/history`| Public | None | Flight ID | `ApiResponse<FlightPriceHistoryResponse>`| 200, 404 | Cached | **VERIFIED** |
| `POST /api/v1/pricing/flights/{id}/freeze`| Required | User | Flight ID, Duration | `ApiResponse<PriceFreezeResponse>` | 201, 400, 401 | Server-side TTL Lock | **VERIFIED** |
| `POST /api/v1/bookings` | Required | User | Passengers, Seats, PriceFreezeId | `ApiResponse<BookingResponse>` | 201, 400, 409 | Atomic Inventory Decrement | **VERIFIED** |
| `GET /api/v1/bookings/my-bookings` | Required | User | Pageable | `ApiResponse<Page<BookingResponse>>` | 200, 401 | User ID Scoped (IDOR Safe) | **VERIFIED** |
| `GET /api/v1/bookings/{id}` | Required | User | Booking ID | `ApiResponse<BookingResponse>` | 200, 403, 404 | Ownership Check | **VERIFIED** |
| `POST /api/v1/bookings/{id}/cancel` | Required | User | Reason, Note | `ApiResponse<BookingResponse>` | 200, 400, 403, 409 | Idempotent State Machine | **VERIFIED** |
| `POST /api/v1/payments/create-order` | Required | User | Booking ID, Gateway | `ApiResponse<PaymentOrderResponse>` | 200, 400, 401 | Server-authoritative Recalc | **VERIFIED** |
| `POST /api/v1/payments/verify` | Required | User | Razorpay Order/Payment/Signature | `ApiResponse<PaymentResponse>` | 200, 400, 401 | HMAC-SHA256 Signature Check | **VERIFIED** |
| `POST /api/v1/payments/webhook` | Public (Signed)| Gateway | Razorpay Webhook Payload | `ApiResponse<Void>` | 200, 400, 401 | HMAC Secret Verified | **VERIFIED** |
| `GET /api/v1/reviews/target/{type}/{id}` | Public | None | Target Type, Target ID | `ApiResponse<Page<ReviewResponse>>` | 200, 400 | Moderated Status Filtered | **VERIFIED** |
| `POST /api/v1/reviews` | Required | User | Rating, Comment, Target | `ApiResponse<ReviewResponse>` | 201, 400, 401 | Verified Stay Linked | **VERIFIED** |
| `POST /api/v1/reviews/{id}/photo` | Required | User | MultipartFile ($\le 5\text{MB}$) | `ApiResponse<ReviewResponse>` | 200, 400, 403 | MIME & Extension Validated | **VERIFIED** |
| `GET /api/v1/recommendations/flights` | Optional | None | Limit | `ApiResponse<List<RecommendationResponse>>`| 200 | Cold-start Fallback Supported | **VERIFIED** |
| `POST /api/v1/recommendations/feedback` | Required | User | Rec ID, FeedbackType | `ApiResponse<Void>` | 200, 400, 401 | User Affinity Updated | **VERIFIED** |
| `GET /api/v1/admin/**` | Required | Admin | Admin Parameters | `ApiResponse<T>` | 200, 403 | Role-Based Access Enforced | **VERIFIED** |
| `GET /actuator/health` | Public | None | None | `HealthStatus` | 200, 503 | Spring Boot Health Probe | **VERIFIED** |
