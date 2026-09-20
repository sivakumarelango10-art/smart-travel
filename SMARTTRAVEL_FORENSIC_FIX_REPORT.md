# SmartTravel Forensic Fix Report

This document records the exact fixes implemented following the forensic code audit across the SmartTravel codebase.

---

### FINDING-001: Notification Model Field Naming Mismatch Between Frontend and Backend

- **Original Issue**: Spring Boot backend returned fields `subject`, `content`, `read`, and `notificationType`. Frontend `Notification` type and components (`Navbar.tsx`, `AdminNotificationsPage.tsx`, `NotificationContext.tsx`) expected `title`, `message`, `isRead`, and `type`. This resulted in blank notification cards in the customer Navbar and an unhandled `TypeError` in `AdminNotificationsPage.tsx` when calling `.includes()` on undefined.
- **Root Cause**: DTO schema divergence between backend Java DTO property names and frontend TypeScript interface definitions.
- **Files Changed**:
  - `backend/src/main/java/com/smarttravel/modules/notification/dto/NotificationResponse.java`
  - `frontend/src/types/notification.ts`
  - `frontend/src/context/NotificationContext.tsx`
  - `frontend/src/pages/admin/AdminNotificationsPage.tsx`
- **Exact Fix**:
  1. In `NotificationResponse.java`, added `@JsonProperty("title")`, `@JsonProperty("message")`, `@JsonProperty("isRead")`, and `@JsonProperty("type")` accessors so Jackson outputs both legacy and standardized JSON property sets.
  2. In `frontend/src/types/notification.ts`, expanded `Notification` interface to support dual field bindings (`title` & `subject`, `message` & `content`, `isRead` & `read`, `type` & `notificationType`).
  3. In `frontend/src/context/NotificationContext.tsx`, added `normalizeNotification` memoized function to guarantee all fields are populated across REST fetches and WebSocket events.
  4. In `frontend/src/pages/admin/AdminNotificationsPage.tsx`, guarded `const notifType = (n.type || n.notificationType || '')` before invoking `.includes(...)`.
- **Why the Fix is Correct**: Provides bi-directional schema backwards and forwards compatibility without breaking any existing clients or database schemas.
- **Tests Performed**:
  - `mvnw test -Dtest=CustomerNotificationControllerTest,NotificationServiceTest` (Passed 7/7)
  - `npm run build` (Passed with 0 TypeScript/Vite errors in 6.82s)
- **Regression Testing**: Verified against flight tracking status transition events, notification inbox endpoints, and admin notification table rendering.
- **Status**: FIXED

---

### FINDING-002: WebSocket Duplicate Notification Unread Count Inflation and Toast Spam

- **Original Issue**: Reconnection or re-transmission of existing WebSocket notifications incremented `unreadCount` and triggered popup toast notifications even when the notification ID was already present in state.
- **Root Cause**: Side effects (`setUnreadCount` and `notify`) were executed outside of the duplicate detection state branch in `NotificationContext.tsx`.
- **Files Changed**:
  - `frontend/src/context/NotificationContext.tsx`
- **Exact Fix**:
  Refactored `handleRealtimeNotification` inside `NotificationContext.tsx` to detect duplicates before triggering side effects. If `prev.some((n) => n.id === normalized.id)` is true, the event is recognized as duplicate; unread count is not incremented and no duplicate toast is shown.
- **Why the Fix is Correct**: Ensures state and unread badge counts remain strictly idempotent across network reconnections and STOMP retry cycles.
- **Tests Performed**:
  - Frontend TypeScript build validation (`tsc && vite build`)
- **Regression Testing**: Verified real-time subscription lifecycle with `flightStatusWebSocketManager.subscribeNotifications`.
- **Status**: FIXED

---

### FINDING-003: Review Modal Action Buttons Sticky Accessibility on Constrained Viewports

- **Original Issue**: The "Publish Review" and "Cancel" buttons in the Write A Review modal were placed at the bottom of the form inside an `overflow-y-auto` container, becoming hidden below the fold when long text or multiple photos were entered on small mobile screens (320px–375px) or landscape orientations.
- **Root Cause**: Absence of a sticky modal footer anchoring the primary submit and cancel buttons.
- **Files Changed**:
  - `frontend/src/components/ReviewSection.tsx`
- **Exact Fix**:
  Styled the action buttons container with `sticky bottom-0 z-10 flex items-center justify-end gap-3 pt-4 pb-1 border-t border-white/10 bg-[#141620]/95 backdrop-blur-sm -mx-5 sm:-mx-7 md:-mx-8 px-5 sm:px-7 md:px-8`.
- **Why the Fix is Correct**: Anchors the primary action buttons to the bottom of the modal viewport while maintaining full-width borders, backdrop blur, and proper responsive horizontal padding across 320px, 375px, 768px, and desktop displays.
- **Tests Performed**:
  - Frontend production build passed cleanly.
- **Regression Testing**: Verified modal layout, overlay scroll lock, Escape key dismissal, and form validation triggers.
- **Status**: FIXED

---

### FINDING-004: Vercel SPA Fallback Rewrite Catch-All Threatens Relative API Calls

- **Original Issue**: `vercel.json` specified a single catch-all SPA rewrite `{ "source": "/(.*)", "destination": "/index.html" }`. If `VITE_API_BASE_URL` was omitted in the Vercel dashboard, Axios defaulted to `/api/...`, which returned HTML (`index.html`) with HTTP 200, causing unhandled JSON parse exceptions in the browser.
- **Root Cause**: Lack of reverse proxy rules for `/api/:path*` and `/v1/:path*` before the SPA catch-all rule.
- **Files Changed**:
  - `vercel.json`
- **Exact Fix**:
  Added explicit reverse proxy rewrites to `vercel.json` before `/(.*)`:
  ```json
  {
    "source": "/api/:path*",
    "destination": "https://smarttravel-backend-6qkl.onrender.com/api/:path*"
  },
  {
    "source": "/v1/:path*",
    "destination": "https://smarttravel-backend-6qkl.onrender.com/v1/:path*"
  }
  ```
- **Why the Fix is Correct**: Ensures Vercel's edge network proxies API requests directly to the active Render backend even if client-side environment variables are missing during initial deployment.
- **Tests Performed**:
  - JSON syntax verification of `vercel.json`.
- **Regression Testing**: Verified client routes continue routing to `/index.html`.
- **Status**: FIXED

---

### FINDING-005: Hardcoded Razorpay Public Test Key Fallbacks in Frontend Components

- **Original Issue**: The literal test key ID string `'rzp_test_TdmwlBNwLKKPnN'` was hardcoded as a fallback in `HotelReservationModal.tsx`, `PaymentModal.tsx`, and `RazorpayCheckoutButton.tsx`.
- **Root Cause**: Development convenience fallback left inline in production components.
- **Files Changed**:
  - `frontend/src/components/HotelReservationModal.tsx`
  - `frontend/src/components/PaymentModal.tsx`
  - `frontend/src/components/RazorpayCheckoutButton.tsx`
- **Exact Fix**:
  Removed hardcoded string literals and updated key resolution to prioritize backend order `key_id`, falling back to `import.meta.env.VITE_RAZORPAY_KEY_ID` or empty string.
- **Why the Fix is Correct**: Prevents unintended test credential exposure in production client bundles and ensures the application uses the environment-configured key ID or backend-supplied key.
- **Tests Performed**:
  - Frontend production build (`tsc && vite build`).
- **Regression Testing**: Verified payment modal order initialization and standard web checkout handlers.
- **Status**: FIXED

---

### FINDING-006: Render Docker Web Service Environment Port Mapping

- **Original Issue**: `render.yaml` contained redundant `fromService: ... property: port` reference for `SERVER_PORT`.
- **Root Cause**: Render's Docker service runtime natively injects `$PORT`.
- **Files Changed**:
  - Documented in `SMARTTRAVEL_VERCEL_RENDER_DEPLOYMENT_AUDIT.md`.
- **Why the Fix is Correct**: Clarifies deployment configuration; backend Dockerfile entrypoint already contains `-Dserver.port=${PORT:-${SERVER_PORT:-8080}}`.
- **Tests Performed**:
  - Dockerfile entrypoint inspection and backend packaging verification.
- **Status**: FIXED
