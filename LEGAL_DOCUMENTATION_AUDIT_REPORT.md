# SmartTravel — Legal Documentation & Privacy Audit Report

**Platform Version:** 1.0.0  
**Document Generation Date:** August 24, 2026  
**Applicable Legal Framework:** Digital Personal Data Protection Act, 2023 (DPDP Act, 2023), Digital Personal Data Protection Rules, 2025, and the Information Technology Act, 2000 (India).

---

## 1. Executive Summary & Policy Status
This audit report presents a comprehensive verification of the legal documentation, personal data collections, storage mechanisms, payment flows, dynamic pricing disclosures, cancellation refund rules, and user controls implemented across the SmartTravel codebase.

| Document / Feature | Route | Status | Notes |
| :--- | :--- | :--- | :--- |
| **Privacy Policy** | `/privacy-policy` (alias: `/privacy`) | **Production-Ready** | Full DPDP Act 2023 alignment, itemized database collection disclosures, and zero-deception data principal rights. |
| **Terms & Conditions** | `/terms-and-conditions` (alias: `/terms`) | **Production-Ready** | Detailed platform mediation terms, 30-min price freeze terms, exact time-tiered refund rules, and simulated radar disclaimer. |
| **Cookie & Storage Policy** | `/cookie-policy` (alias: `/cookies`) | **Production-Ready** | Itemized client-side `localStorage` / `sessionStorage` token audit with explicit zero-tracking cookie declaration. |
| **Registration Consent** | `/register` | **Integrated** | Non-deceptive, unbundled legal acknowledgement notice linking to Terms and Privacy Policy. |
| **Pre-Payment Consent** | `/book/:flightId` (Step 3) | **Integrated** | Explicit pre-checkout confirmation linking to Terms, Cancellation Tiers, and Privacy Policy. |
| **Global Footer Navigation** | Global (`Footer.tsx`) | **Integrated** | Accessible across desktop, tablet, and mobile with direct links in sitemap and bottom copyright bar. |

---

## 2. Personal Data & Database Model Audit
Every collection in the MongoDB database containing personal, financial, or itinerary data was audited against actual source code:

| Collection Name | Document Class | Fields Stored | Processing Purpose | Access & Security | Retention / Deletion |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `users` | `com.smarttravel.modules.user.model.User` | Full Name, Email (normalized), `BCrypt` password hash, Phone, Roles (`ROLE_CUSTOMER`, `ROLE_ADMIN`), `UserPreferences`, Verification status, `lastLoginAt`. | Authentication, account profile management, saved travel preferences. | Authenticated user (`userId`), `ROLE_ADMIN`. | Retained during active account lifecycle. Deleted immediately upon `DELETE /v1/auth/me`. |
| `bookings` | `com.smarttravel.modules.booking.model.Booking` | PNR reference, User ID, User Email, Flight details, `Passenger` list (Name, Title, Gender, DOB, Nationality, Passport, Seat Number), Fare breakdown, Total (INR), Status (`CONFIRMED`, `CANCELLED`, `EXPIRED`), Expiration. | Flight seat reservations, e-ticket generation, airport passenger manifests. | User owner and staff/admin. | Active until flight completion or cancellation. Financial invoice references archived for tax compliance. |
| `payments` | `com.smarttravel.modules.payment.model.Payment` | Booking ID, PNR, User ID, User Email, Razorpay Order ID, Razorpay Payment ID, Signature hash, Amount (paise/INR), Currency, Status (`CREATED`, `VERIFIED`, `FAILED`), failure reason. | Payment reconciliation, fraud prevention, transaction verification. | User owner, Admin, Payment webhook. | Archived as required under statutory financial record-keeping laws. |
| `refunds` | `com.smarttravel.modules.payment.refund.model.Refund` | Refund Number, Payment ID, Razorpay Payment ID, Booking ID, User ID, Amount (paise/INR), Reason (`CUSTOMER_CANCELLATION`, `FLIGHT_CANCELLED`, `ADMIN_OVERRIDE`), Status (`REQUESTED`, `PROCESSING`, `COMPLETED`), Razorpay Refund ID. | Policy-based refund execution, banking settlement tracking. | User owner and Admin. | Maintained for payment audit trail and customer dispute resolution. |
| `price_freezes` | `com.smarttravel.modules.pricing.model.PriceFreeze` | User ID, Flight ID, Cabin Class, Passenger count, Locked Price per passenger, Total locked price, Base price snapshot, Status (`ACTIVE`, `USED`, `EXPIRED`, `CANCELLED`), 30-min expiration timestamp. | Temporary 30-minute fare locking against dynamic price surges. | User owner. | Auto-expired after 30 minutes via scheduled background cleanup task. |
| `tracked_flights` | `com.smarttravel.modules.flight.tracking.model.TrackedFlight` | User ID, Flight ID, Flight Number, Last known status, Last known ETA, Active flag. | Airspace tracking subscriptions and schedule notifications. | User owner. | Maintained until untracked or flight arrival. |
| `notifications` | `com.smarttravel.modules.notification.model.Notification` | User ID, Title, Message, Notification Type, Channel (`IN_APP`, `PUSH`, `EMAIL`), Read status, Timestamps. | Critical travel updates, gate alerts, check-in reminders. | User owner. | Auto-archived or managed via notification center. |
| `push_subscriptions` | `com.smarttravel.modules.notification.model.PushSubscription` | User ID, Browser Push Endpoint URL, `p256dh` public encryption key, `auth` token secret. | W3C Web Push notification delivery. | System Push Engine. | Revoked upon browser permission change or account deletion. |
| `hotels` & `room_types` | `com.smarttravel.modules.hotel.model.Hotel` | Property details, amenities, room categories, base prices, 15-min temporary checkout holds. | Hotel discovery, room category selection, stay booking. | Public search, authenticated booking. | Property catalog data. |

---

## 3. Third-Party Service Providers Audit
Only third-party providers actually integrated in the codebase are included:

1. **Payment Gateway**: **Razorpay Software Private Limited**
   - *Role:* Authoritative payment order generation, 256-bit encrypted checkout modal, HMAC signature verification, webhook notification handling, and automated refund disbursement.
   - *Data Transmitted:* Order amount, PNR, and contact info. No card numbers or CVVs are ever handled or stored by SmartTravel.
2. **Database & Infrastructure**: **MongoDB Atlas / Cloud Hosting**
   - *Role:* Clustered document persistence with encrypted connections (TLS) and compound indexing.
3. **Map & Radar Display**: **Leaflet / OpenStreetMap**
   - *Role:* Public map tiles for airspace radar visualization. Zero passenger PII is sent to map tile servers.
4. **Imagery CDN**: **Unsplash CDN**
   - *Role:* High-resolution travel and destination photography assets.

---

## 4. Cancellation & Refund Policy Audit
The Terms and Privacy Policy accurately describe the exact time-tiered refund calculation implemented in `CancellationRefundPolicy.java` and `RefundServiceImpl.java`:

- **&gt; 168 Hours (&gt; 7 Days) Before Departure:** **100% Full Refund** of original gross fare paid.
- **24 to 168 Hours (1–7 Days) Before Departure:** **50% Partial Refund** calculated via exact paise integer arithmetic (`originalAmountPaise / 2`).
- **&lt; 24 Hours Before Departure:** **0% (No Refund)** due to late cancellation window.
- **After Scheduled Flight Departure:** **0% (No Show / No Refund)**.
- **Airline Disruption / Flight Cancellation:** **100% Full Refund** triggered automatically regardless of window.
- **Processing Timeline:** SmartTravel triggers refund requests instantly; banking settlement cycles reflect credit within **5 to 7 business days**.

---

## 5. Dynamic Pricing & Price Freeze Disclosure Audit
- **Dynamic Pricing Engine:** Fares dynamically adjust based on cabin occupancy demand bands (0–40% seats booked: +0%; 40–60%: +5%; 60–80%: +10%; 80–90%: +20%; 90–100%: +30%), calendar seasonality, and holiday surges. Itemized fare snapshots (Base Fare + GST Taxes + Convenience Fee) are clearly disclosed before checkout.
- **Price Freeze Program:** Locks current fare for **30 minutes**. Users may hold one active freeze per flight cabin. If not used within 30 minutes, the freeze transitions to `EXPIRED`.

---

## 6. Flight Radar Simulation Disclosure Audit
- **Live Flight Radar & Telemetry:** Disclosed transparently in both Terms & Conditions (§16) and Privacy Policy (§8) that flight telemetry (coordinates, altitude, speed, and simulated delays) may be generated by the platform simulation engine for demonstrative, scheduling, and testing purposes, and must not be used for real-world flight navigation.

---

## 7. Client Storage & Cookies Audit
- **Cookies:** SmartTravel does **NOT** use tracking cookies or third-party marketing pixels.
- **`localStorage` / `sessionStorage`:**
  - `smarttravel_access_token`: Cryptographic JWT Bearer token (24-hour expiration).
  - `smarttravel_refresh_token`: Cryptographic refresh token (7-day expiration if "Remember Me" is selected).
  - `smarttravel_user`: Cached non-sensitive profile payload for fast client-side rendering.

---

## 8. User Rights & Account Controls Audit
- **Profile Correction:** Users can edit full name, phone number, address, and travel preferences anytime via `/account`.
- **Self-Service Deletion:** Account deletion is fully implemented via `DELETE /v1/auth/me`, removing user credentials, preferences, and push notification tokens from the database.
- **Notification Opt-Out:** Browser push subscriptions can be revoked directly in browser settings.

---

## 9. Security & Secret Exposure Audit
- All backend JWT signing keys, Razorpay keys, and database secrets are externalized via environment variables in `application.yml`.
- No sensitive credentials, private keys, or passwords are exposed in frontend client bundles or legal documentation.

---

## 10. Legal & Business Information Placeholders for Counsel Review
The following standardized placeholders are clearly designated in the documentation for final corporate legal entity insertion prior to production launch:

1. `[SMARTTRAVEL LEGAL ENTITY NAME]` &rarr; Replace with official registered company name (e.g., *SmartTravel Technologies Private Limited*).
2. `[REGISTERED OFFICE ADDRESS]` &rarr; Replace with registered corporate headquarters address.
3. `[PRIVACY CONTACT EMAIL]` &rarr; Replace with designated privacy mailbox (e.g., `privacy@smarttravel.com`).
4. `[GRIEVANCE OFFICER NAME / DESIGNATION]` &rarr; Replace with formal Data Protection / Grievance Officer name.
5. `[GRIEVANCE EMAIL]` &rarr; Replace with designated grievance redressal email (e.g., `grievance@smarttravel.com`).
6. `[JURISDICTION CITY]` &rarr; Replace with exclusive court jurisdiction city (e.g., *New Delhi, India*).
7. `[EFFECTIVE DATE]` / `[LAST UPDATED DATE]` &rarr; Replace with corporate launch dates.

---

## 11. Disclaimer
*This technical documentation and policy framework was generated from an exhaustive audit of the SmartTravel platform codebase to ensure 100% technical truthfulness. It does not constitute formal legal counsel. Final corporate legal review should be conducted by a qualified attorney prior to live commercial operations.*
