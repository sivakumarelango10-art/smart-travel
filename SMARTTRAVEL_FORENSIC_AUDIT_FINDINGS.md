# SmartTravel Forensic Audit Findings Report

Generated as part of the Forensic, Line-by-Line, File-by-File Audit of the SmartTravel Platform.

---

## FINDING-001

Severity: HIGH  
Category: BUG / API / UI  
File: `backend/src/main/java/com/smarttravel/modules/notification/dto/NotificationResponse.java` and `frontend/src/types/notification.ts`  
Line: `backend/.../NotificationResponse.java:31-41`, `frontend/.../notification.ts:15-25`, `frontend/.../AdminNotificationsPage.tsx:56`, `frontend/.../Navbar.tsx:219-230`  
Function: `NotificationResponse` DTO serialization & `NotificationContext` / `Navbar` / `AdminNotificationsPage`  

### Evidence
The Spring Boot backend serializes notification responses with fields:
- `subject` (String)
- `content` (String)
- `read` (boolean)
- `notificationType` (NotificationType enum)

The frontend React TypeScript interface (`Notification`) and components expect:
- `title` (string)
- `message` (string)
- `isRead` (boolean)
- `type` (NotificationType)

In `Navbar.tsx` (lines 225-230):
```tsx
<span className="font-bold text-white text-xs">{notif.title}</span>
<p className="text-[11px] text-slate-300 mt-1 leading-snug">{notif.message}</p>
```
Both `notif.title` and `notif.message` evaluate to `undefined` (rendered blank), and `notif.isRead` evaluates to `undefined`.
In `AdminNotificationsPage.tsx` (line 56):
```tsx
if (n.type.includes('CANCEL') || n.priority === 'URGENT') ...
```
Calling `.includes()` on `undefined` throws an unhandled runtime `TypeError: Cannot read properties of undefined (reading 'includes')`.

### Root Cause
Schema divergence between backend Java DTO property names and frontend TypeScript interface definitions.

### Impact
1. Travelers see blank notification cards in the Navbar dropdown.
2. Read state styling fails to apply accurately.
3. The Admin Notifications management page crashes when rendering icons for notifications that lack `n.type`.

### Reproduction
1. Log into the application and trigger or receive any flight, booking, or gate update notification.
2. Click the notification bell in the Navbar: notification cards have blank headers and empty text.
3. Navigate to `/admin/notifications`: runtime exception is triggered on icon type evaluation.

### Recommended Fix
1. In `NotificationResponse.java`, add JSON property accessors for `title`, `message`, `isRead`, and `type`.
2. In `types/notification.ts`, expand interface with dual-field compatibility (`title` & `subject`, `message` & `content`, `isRead` & `read`, `type` & `notificationType`).
3. In `NotificationContext.tsx`, implement defensive normalization on all incoming and fetched notifications.
4. In `AdminNotificationsPage.tsx`, guard against undefined types using `(n.type || n.notificationType || '').includes(...)`.

Status: OPEN

---

## FINDING-002

Severity: MEDIUM  
Category: BUG / WEBSOCKET  
File: `frontend/src/context/NotificationContext.tsx`  
Line: 80-86  
Function: `handleRealtimeNotification`  

### Evidence
```tsx
const handleRealtimeNotification = (incoming: any) => {
  if (!incoming) return;
  setNotifications((prev) => {
    if (prev.some((n) => n.id === incoming.id)) return prev;
    return [incoming, ...prev];
  });
  setUnreadCount((prev) => prev + 1);
  notify(incoming.subject || 'New Notification', incoming.content || '', 'INFO');
};
```

### Root Cause
`setUnreadCount` and `notify` are invoked outside the state updater check where duplicate detection happens.

### Impact
Whenever a WebSocket reconnects or re-broadcasts an existing notification ID, `unreadCount` falsely increments and a duplicate toast notification is popped up to the traveler.

### Reproduction
1. Connect to the WebSocket notification topic for user `user-1`.
2. Send two consecutive messages with the exact same notification ID.
3. The list maintains 1 item, but the unread count badge increments by 2 and two identical toasts appear.

### Recommended Fix
Perform duplicate detection before executing side effects, or verify within state updater so only genuinely new unread notifications increment the unread count and trigger toasts.

Status: OPEN

---

## FINDING-003

Severity: LOW  
Category: UI / RESPONSIVE  
File: `frontend/src/components/ReviewSection.tsx`  
Line: 888, 1045  
Function: `ReviewSection` Write Your Review Modal  

### Evidence
In the "Write Your Review" modal, the submit action buttons (`Cancel` and `Publish Review`) are positioned at the bottom of the form without sticky positioning inside an `overflow-y-auto` container with `max-h-[90vh]`.

### Root Cause
The action bar is placed at the end of the scrollable document body rather than inside a sticky modal footer.

### Impact
On mobile screens (e.g. 320px–375px width or landscape orientation) where users enter a detailed review and attach up to 5 photos, the action buttons get pushed far down below the fold, forcing the user to scroll repeatedly to find the submit button.

### Reproduction
1. Open the review modal on a 320x568 viewport.
2. Select ratings, type a 4-line review, and select 3 photos.
3. Action buttons scroll completely offscreen.

### Recommended Fix
Add `sticky bottom-0 bg-[#141620] py-3 z-10` to the action button wrapper to ensure action buttons are permanently visible and accessible across all viewport sizes.

Status: OPEN

---

## FINDING-004

Severity: MEDIUM  
Category: DEPLOYMENT / VERCEL  
File: `vercel.json`  
Line: 6-11  
Function: Vercel rewrites configuration  

### Evidence
```json
"rewrites": [
  {
    "source": "/(.*)",
    "destination": "/index.html"
  }
]
```

### Root Cause
A single catch-all SPA rewrite rewrites all paths to `/index.html`. If the Vercel production environment variable `VITE_API_BASE_URL` is omitted, Axios defaults to `/api`, which hits the Vercel domain and is rewritten to `/index.html`, returning HTTP 200 with HTML text.

### Impact
Frontend fails completely with `SyntaxError: Unexpected token '<'` when attempting to parse JSON from API responses.

### Reproduction
1. Deploy to Vercel without configuring `VITE_API_BASE_URL`.
2. Open the application: all API calls return `index.html` instead of JSON.

### Recommended Fix
1. Explicitly document `VITE_API_BASE_URL` as a mandatory Vercel environment variable.
2. Add reverse proxy rules in `vercel.json` for `/api/:path*` and `/v1/:path*` pointing to the active Render backend service as a resilient fallback.

Status: OPEN

---

## FINDING-005

Severity: LOW  
Category: SECURITY / CODE QUALITY  
File: `frontend/src/components/HotelReservationModal.tsx`, `PaymentModal.tsx`, `RazorpayCheckoutButton.tsx`  
Line: `HotelReservationModal.tsx:363,370`, `PaymentModal.tsx:155`, `RazorpayCheckoutButton.tsx:102`  
Function: Razorpay Key ID resolution  

### Evidence
A public test key ID string literal is hardcoded as fallback in multiple frontend components when `import.meta.env.VITE_RAZORPAY_KEY_ID` is undefined.

### Root Cause
Component-level fallback intended for local development was committed in production UI components.

### Impact
Exposes a public test identifier and could mask missing environment variable configuration in production deployments.

### Reproduction
Inspect the compiled frontend bundle or source files for hardcoded key literals.

### Recommended Fix
Prioritize backend-provided `order.key_id` or `activeOrder.keyId`, fall back to `import.meta.env.VITE_RAZORPAY_KEY_ID`, and eliminate hardcoded string literals.

Status: OPEN

---

## FINDING-006

Severity: INFO  
Category: DEPLOYMENT / RENDER  
File: `render.yaml`  
Line: 21-26  
Function: `SERVER_PORT` environment definition  

### Evidence
```yaml
- key: SERVER_PORT
  fromService:
    type: web
    name: smarttravel-backend
    property: port
```

### Root Cause
Render automatically provides `$PORT` to Docker web services at runtime, rendering the blueprint `fromService: ... property: port` self-reference redundant.

### Impact
No application failure occurs because `Dockerfile` entrypoint correctly handles `${PORT:-${SERVER_PORT:-8080}}`, but it creates ambiguity in deployment blueprints.

### Reproduction
Review `render.yaml` vs Dockerfile entrypoint syntax.

### Recommended Fix
Document the Render port injection mechanism in `SMARTTRAVEL_VERCEL_RENDER_DEPLOYMENT_AUDIT.md`.

Status: OPEN
