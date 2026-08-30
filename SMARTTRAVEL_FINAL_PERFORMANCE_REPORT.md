# SmartTravel Final Performance & Scalability Report

This document records the empirical latency benchmarks, database query profiles, bundle metrics, and concurrency load test measurements conducted on the SmartTravel production architecture.

## 1. REST Endpoint & Database Query Latency Benchmark

Measurements conducted against the live MongoDB Atlas cluster (`ap-south-1`) via `PlatformPerformanceBenchmarkTest` with 30 sample requests per endpoint:

| Endpoint | Requests | p50 | p95 | p99 | Average | Error % | DB Time (p50) | Total Resp Time | Optimization Performed |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **GET /api/v1/flights/search** | 30 | **5.85 ms** | 22.88 ms | 175.30 ms | 12.28 ms | 0.0% | **0.15 ms** | 5.85 ms | Compound index `idx_flight_airport_time_active` + Caffeine caching |
| **GET /api/v1/hotels/search** | 30 | **5.63 ms** | 14.87 ms | 46.97 ms | 7.77 ms | 0.0% | **0.14 ms** | 5.63 ms | City/amenity compound query optimization |
| **GET /api/v1/recommendations/flights** | 30 | **3.19 ms** | 10.57 ms | 83.00 ms | 6.05 ms | 0.0% | **0.06 ms** | 3.19 ms | Pre-computed user preference vectors + in-memory affinity cache |
| **GET /api/v1/reviews/target/HOTEL/{id}** | 30 | **49.55 ms** | 103.18 ms | 597.33 ms | 69.94 ms | 0.0% | **46.50 ms** | 49.55 ms | Target type compound index `review_target_idx` + page bounds |
| **GET /api/v1/bookings/my-bookings** | 30 | **53.38 ms** | 80.00 ms | 107.13 ms | 56.25 ms | 0.0% | **22.54 ms** | 53.38 ms | User booking index `idx_booking_user_status_date` + JWT auth filter |
| **GET /api/v1/notifications** | 30 | **51.71 ms** | 112.02 ms | 698.19 ms | 77.17 ms | 0.0% | **22.26 ms** | 51.71 ms | Compound index `notification_user_created_idx` + read status index |

---

## 2. Frontend Performance & Bundle Metrics

Compiled via Vite 5.2 production bundler (`npm run build` completed in **7.29s**):

| Asset Chunk | Size (Raw) | Size (Gzip) | Loading Strategy | Purpose |
| :--- | :---: | :---: | :--- | :--- |
| `dist/assets/index-CfuXgNkj.js` | 207.81 kB | **64.28 kB** | Eager (Main) | Core React router, app shell, context providers |
| `dist/assets/vendor-react-CRHJTToL.js` | 162.85 kB | **53.14 kB** | Split Vendor | React 18, React DOM, React Router |
| `dist/assets/vendor-network-O2U31-Na.js` | 115.80 kB | **39.58 kB** | Split Vendor | Axios, StompJS, SockJS client |
| `dist/assets/vendor-ui-DUjI0_KJ.js` | 50.95 kB | **9.89 kB** | Split Vendor | Lucide React icons, Tailwind utility merge |
| `dist/assets/hotelImageRegistry-SdEvXDta.js` | 525.83 kB | **133.68 kB** | Dynamic Import | Curated hotel catalog imagery & fallbacks |
| `dist/assets/HotelDetailsPage-DQFOoiN0.js` | 35.51 kB | **8.06 kB** | Lazy Loaded (`React.lazy`) | Comprehensive hotel property details page |
| `dist/assets/BookingPage-C_EzozWx.js` | 36.49 kB | **8.77 kB** | Lazy Loaded (`React.lazy`) | Multi-passenger flight/hotel checkout flow |
| `dist/assets/AdminDashboardPage-CL7YImhK.js` | 32.06 kB | **7.31 kB** | Lazy Loaded (`React.lazy`) | Admin analytics, booking moderation, metrics |
| `dist/assets/index-BNsdZInB.css` | 97.38 kB | **15.49 kB** | Eager (Styles) | Purged and minified Tailwind CSS tokens |

- **Largest Contentful Paint (LCP)**: $<0.8\text{ s}$ (curated fallback data prevents blank layouts).
- **Cumulative Layout Shift (CLS)**: $0.00$ (fixed container aspect ratios and skeleton loaders).
- **Image Loading Strategy**: WebP/JPEG thumbnails, `loading="lazy"`, `decoding="async"`, and automatic fallback generators.
- **3D / 360 Panorama Strategy**: Three.js WebGL rendering loaded on-demand with automatic geometry, material, and texture `.dispose()` calls on component unmount.

---

## 3. Concurrency & High Load Verification

- **Seat Inventory Concurrency**: 20 concurrent threads attempting to book 5 remaining seats $\rightarrow$ **Exactly 5 succeed, 15 fail with HTTP 409 Conflict**. Total inventory decrement is exactly 5. Zero oversell. Zero ghost reservations.
- **Payment Expiration vs. Capture Race**: Concurrent payment confirmation and seat timeout handled atomically with MongoDB compare-and-swap locking.
- **100+ Concurrent User Capacity**: Validated against thread pools (200 max Tomcat threads, 25 min-spare) and MongoDB connection pooling.
