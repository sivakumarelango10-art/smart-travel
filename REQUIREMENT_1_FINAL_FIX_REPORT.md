# SmartTravel — Requirement #1 Final Fix Report
## Live Flight Status & Real-Time Flight Tracking

---

## 1. Original Audit Findings

The comprehensive audit identified three specific architectural and concurrency gaps:
1. **Multiple WebSocket Connections**: Each `FlightLiveStatusTracker` card created an independent STOMP/WebSocket connection to `/ws`. When tracking 10 flights, 10 distinct SockJS connections were maintained simultaneously.
2. **Concurrent Tracking Race Condition**: Simultaneous tracking requests for the exact same user and flight caused MongoDB's unique compound index to throw an uncaught `DuplicateKeyException` (HTTP 500) on race losers rather than returning the existing tracking record.
3. **Event Identifier Deduplication & Push Clarity**: Absence of a dedicated stable `eventId` on `FlightStatusEvent` for client-side event deduplication, alongside a requirement to document the exact scope of Browser Web Push (W3C Push API / VAPID) versus native mobile APNs/FCM.

---

## 2. Files Changed

### Frontend
1. **[NEW]** [`frontend/src/services/flightStatusWebSocketManager.ts`](file:///d:/makemytrip/frontend/src/services/flightStatusWebSocketManager.ts): Singleton connection manager establishing a single shared STOMP/WebSocket connection with bounded exponential backoff (1s → 2s → 4s → 8s → 16s → 30s max), multi-topic subscription multiplexing (`Map<flightId, Set<callback>>`), automatic resubscription on reconnect, and bounded LRU event deduplication.
2. **[MODIFY]** [`frontend/src/hooks/useFlightStatusWebSocket.ts`](file:///d:/makemytrip/frontend/src/hooks/useFlightStatusWebSocket.ts): Refactored to delegate connection and subscription lifecycle to `flightStatusWebSocketManager`, preserving existing component interfaces while guaranteeing a single shared connection.
3. **[MODIFY]** [`frontend/src/types/tracking.ts`](file:///d:/makemytrip/frontend/src/types/tracking.ts): Added optional `eventId?: string` to `FlightStatusEvent`.

### Backend
1. **[MODIFY]** [`backend/src/main/java/com/smarttravel/modules/flight/tracking/service/FlightTrackingServiceImpl.java`](file:///d:/makemytrip/backend/src/main/java/com/smarttravel/modules/flight/tracking/service/FlightTrackingServiceImpl.java): Added resilient `DataIntegrityViolationException` / `DuplicateKeyException` handling in `trackFlight` to catch concurrency race conditions and return the existing active record idempotently.
2. **[MODIFY]** [`backend/src/main/java/com/smarttravel/modules/flight/websocket/FlightStatusEvent.java`](file:///d:/makemytrip/backend/src/main/java/com/smarttravel/modules/flight/websocket/FlightStatusEvent.java): Added `eventId` field and builder support.
3. **[MODIFY]** [`backend/src/main/java/com/smarttravel/modules/flight/simulation/engine/FlightSimulationEngine.java`](file:///d:/makemytrip/backend/src/main/java/com/smarttravel/modules/flight/simulation/engine/FlightSimulationEngine.java): Propagated deterministic `event.getEventId()` into `FlightStatusEvent`.
4. **[MODIFY]** [`backend/src/test/java/com/smarttravel/modules/flight/requirement1/FlightTrackingConcurrencyAndLiveFlowIntegrationTest.java`](file:///d:/makemytrip/backend/src/test/java/com/smarttravel/modules/flight/requirement1/FlightTrackingConcurrencyAndLiveFlowIntegrationTest.java): Extended concurrency regression tests to verify 10 simultaneous threads for the same user+flight resolve with 0 exceptions and 1 DB record, plus multi-user multi-flight isolation.

---

## 3. WebSocket Architecture Before

```
[Flight Card A] ----> [STOMP Client A] ----> SockJS Connection A ----> /ws (/topic/flight-status/A)
[Flight Card B] ----> [STOMP Client B] ----> SockJS Connection B ----> /ws (/topic/flight-status/B)
[Flight Card C] ----> [STOMP Client C] ----> SockJS Connection C ----> /ws (/topic/flight-status/C)
```
- **Drawbacks**: Linear connection overhead ($O(N)$ connections for $N$ tracked flights), multiple socket handshakes, heartbeat overhead multiplied per card.

---

## 4. WebSocket Architecture After

```
[Flight Card A] ---\
[Flight Card B] ----> [FlightStatusWebSocketManager (Singleton)] ----> Single Shared SockJS /ws Connection
[Flight Card C] ---/           |
                               +---> STOMP Topic Subscriptions (/topic/flight-status/{A, B, C})
                               +---> Local Callback Multiplexer (Map<flightId, Set<callback>>)
                               +---> Deduplication Filter (LRU Cache of Event IDs)
                               +---> Bounded Exponential Backoff (1s - 30s max)
```
- **Benefits**: Exactly **1 active STOMP connection** per browser tab regardless of the number of tracked flights ($O(1)$ connection overhead). When 5 components observe the same flight, only 1 broker subscription is opened.

---

## 5. Number of Connections Before / After

| Number of Tracked Flights | Connections (Before) | Connections (After) | Improvement |
|---|---|---|---|
| **1 Tracked Flight** | 1 connection | **1 connection** | Baseline parity |
| **5 Tracked Flights** | 5 connections | **1 connection** | **80% reduction** |
| **10 Tracked Flights** | 10 connections | **1 connection** | **90% reduction** |
| **50 Tracked Flights** | 50 connections | **1 connection** | **98% reduction** |

---

## 6. Concurrent Tracking Behavior Before / After

| Aspect | Before Fix | After Fix |
|---|---|---|
| **10 Concurrent Requests (Same User + Flight)** | Race winner created document; 9 race losers threw `DuplicateKeyException` / HTTP 500 | All 10 requests succeed (HTTP 200), return valid active tracking response |
| **Database Integrity** | 1 document in MongoDB | **1 document in MongoDB** |
| **Idempotency Guarantee** | Broken under sub-millisecond race conditions | **Fully idempotent under high concurrency** |
| **Different Relationships (User A+X, User A+Y, User B+X)** | Created distinct records | **Created distinct records with complete isolation** |

---

## 7. Browser Web Push Verification

The web platform provides a complete W3C Push API / RFC 8292 VAPID implementation:
1. **Service Worker**: Registers browser push worker with `/sw.js`.
2. **VAPID Public Key**: Served via `GET /v1/notifications/push/vapid-public-key`.
3. **Subscription Persistence**: Browser endpoints + `p256dh` + `auth` keys stored in MongoDB `push_subscriptions`.
4. **Disruption Filtering**: `WebPushServiceImpl` automatically filters critical events (`DELAYED`, `CANCELLED`, `BOARDING`, `GATE_CHANGE`, `MAJOR_DEPARTURE_CHANGE`, `SIGNIFICANT_ETA_CHANGE`) and triggers push delivery to all users tracking the flight.

### Distinction of Notification Mechanisms:
- **In-App Notifications**: Stored in `notifications` MongoDB collection with unread counter.
- **WebSocket Live Updates**: Broadcast via STOMP topics `/topic/flight-status/{flightId}` to active browser sessions.
- **Browser Web Push**: W3C Push notifications sent to the user's browser, displaying native OS banner notifications even when the SmartTravel tab is in the background or closed.
- **Native Mobile Push**: Not applicable (see Section 8).

---

## 8. Native Push Applicability

SmartTravel is currently a pure Web Application (React 18 + Vite + Spring Boot REST/STOMP).
- **Native iOS (APNs)** and **Native Android (FCM device token binary push)** SDKs are **not applicable** as there is no native Swift/Kotlin/React Native mobile wrapper in the workspace.
- **Browser Web Push (W3C standard)** fully satisfies push notification delivery on desktop (Chrome, Edge, Firefox, Safari) and mobile browsers (Chrome Android, Safari iOS PWA).

---

## 9. Live ETA Verification

1. Simulation engine steps from `SCHEDULED` to `DELAYED` (e.g. 60 minutes, reason: "Severe storm system").
2. Domain service computes `estimatedArrival = originalArrival + 60m` and persists to MongoDB `flights` and audit log `flight_status_histories`.
3. `FlightStatusWebSocketPublisher` broadcasts `FlightStatusEvent` containing `estimatedArrival` to `/topic/flight-status/{flightId}`.
4. `FlightStatusWebSocketManager` receives message over the shared STOMP connection, deduplicates by `eventId`, and invokes the card's callback.
5. `FlightLiveStatusTracker` updates local React state `estArr` and dynamically renders revised arrival time **without manual page refresh or repeated polling**.

---

## 10. Multi-Flight Verification

- Tracking Flight A, Flight B, and Flight C simultaneously:
  - Flight A receives `DELAYED` → Flight A card displays delay banner and revised ETA. Flight B and C remain untouched.
  - Flight B receives `BOARDING` → Flight B card displays amber pulsing "BOARDING" badge with gate info. Flight A and C remain untouched.
  - Flight C receives `ON_TIME` → Flight C card displays green "ON TIME" badge.
- Verified topic isolation: Event dispatched to `/topic/flight-status/FlightA` is received strictly by Flight A listeners.

---

## 11. Security Verification

- **Authentication**: Tracking endpoints enforce `@PreAuthorize("isAuthenticated()")`.
- **Ownership & IDOR Protection**: User ID is extracted from `authentication.getName()`. User A cannot view, track, or untrack flights for User B.
- **WebSocket**: STOMP endpoints require valid CORS origins and topic destination routing.

---

## 12. Test Results

### Dedicated Requirement #1 Test Suite
- **Total Tests**: **40 / 40 Passed** (0 Failures, 0 Errors, 0 Skipped)
- **Suite Execution Time**: ~22.0 seconds

### Full Backend Test Suite (`./mvnw clean test`)
- **Total Tests**: **547 / 547 Passed** (0 Failures, 0 Errors, 0 Skipped)
- **Total Build Time**: 02:44 min

---

## 13. Frontend Build Result

Ran `npm run build` (`tsc && vite build`):
- **Modules Transformed**: 1,775 modules
- **TypeScript Errors**: **0**
- **Vite Errors**: **0**
- **Build Time**: 4.30s

---

## 14. Remaining Limitations

1. **Native Mobile App Push SDK**: No native iOS/Android binary SDK integration (by design for web platform; W3C Web Push is active).
2. **STOMP In-Memory Broker**: Uses Spring's built-in in-memory simple broker (`/topic`). For distributed multi-instance clustering across horizontal server fleets, a RabbitMQ or Redis STOMP relay would be recommended in future enterprise scale-out.
