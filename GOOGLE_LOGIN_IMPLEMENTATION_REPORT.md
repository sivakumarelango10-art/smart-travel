# SmartTravel — Google Sign-In Implementation Report

## 1. Executive Summary
This document provides a comprehensive technical overview of the implementation of **"Continue with Google"** federated authentication for the SmartTravel enterprise travel platform. The implementation seamlessly integrates Google Identity Services (GIS) with the existing Spring Boot stateless JWT security system and React/Vite frontend without replacing existing authentication or breaking normal email/password authentication.

---

## 2. Existing Authentication Architecture Audit
Prior to implementation, a complete audit of the authentication and authorization subsystems was conducted:

| Layer | Component | Description / Function |
|---|---|---|
| **Database Document** | `User.java` (MongoDB `users` collection) | Stores user identifiers, normalized email index, salted BCrypt password hash, roles (`ROLE_USER`, `ROLE_ADMIN`), account status (`ACTIVE`, `INACTIVE`, `SUSPENDED`, `DELETED`), and embedded `UserPreferences`. |
| **Repository** | `UserRepository.java` | Extends `MongoRepository<User, String>`, providing `findByNormalizedEmail`, `findByEmail`, `existsByNormalizedEmail`, `existsByEmail`. |
| **Service Layer** | `AuthService` / `AuthServiceImpl` | Manages registration, credential verification via `PasswordEncoder`, token generation, profile retrieval, preference updating, password changes, and account deletion. |
| **Token Security** | `JwtTokenProvider.java` | Issues and cryptographically validates 512-bit HMAC-SHA512 signed JWT Bearer access tokens (24-hour expiry) and refresh tokens (7-day expiry). |
| **Filter Chain** | `SecurityConfig.java` + `JwtAuthenticationFilter.java` + `RequestIdFilter.java` | Spring Security 6 stateless filter chain enforcing RBAC, correlation request IDs, custom CORS rules, and secure response headers. |
| **Frontend State** | `AuthContext.tsx` + `authService.ts` | React context providing `user`, `isAuthenticated`, `isAdmin`, `login`, `register`, `logout`, and token management across `localStorage` / `sessionStorage`. |

---

## 3. Google Federated Authentication Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      React / Vite Frontend                  │
│   LoginPage / RegisterPage → GoogleSignInButton (GIS)       │
└──────────────────────────────┬──────────────────────────────┘
                               │ 1. User clicks "Continue with Google"
                               │ 2. Google popup/one-tap prompt
                               ▼
┌─────────────────────────────────────────────────────────────┐
│               Google Identity Services (GIS)                │
│    Verifies Google user & returns signed Google ID Token    │
└──────────────────────────────┬──────────────────────────────┘
                               │ 3. ID Token (JWT Credential)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             SmartTravel Spring Boot Backend                 │
│                 POST /v1/auth/google                        │
└──────────────────────────────┬──────────────────────────────┘
                               │ 4. Server-side verification:
                               │    - Signature check via Google public JWKs
                               │    - Issuer: https://accounts.google.com
                               │    - Audience: GOOGLE_CLIENT_ID
                               │    - Expiration & Subject (sub)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│           Account Resolution & Safe Linking Strategy        │
│   1. Lookup user by googleSubject (verified Google sub)     │
│   2. If not found, lookup by verified normalized email      │
│      - Link googleSubject & avatar if found                 │
│      - Create new User entity if not found (ROLE_USER)      │
│   3. Issue SmartTravel JWT Bearer Token (AuthResponse)      │
└──────────────────────────────┬──────────────────────────────┘
                               │ 5. Return SmartTravel JWT + UserSummary
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  Authenticated User Session                 │
│  Immediate access to:                                       │
│  - Dashboard & My Account                                   │
│  - My Bookings & Boarding Passes                            │
│  - Live Flight Radar & Seat Selection                       │
│  - Hotel Bookings & Room Selection                          │
│  - Dynamic Pricing, Freezes & Automated Refunds             │
└─────────────────────────────────────────────────────────────┘
```

The browser **never trusts an unverified Google identity**. The backend performs strict server-side cryptographic validation before authenticating or registering the user.

---

## 4. Google Cloud Configuration & Minimum Scopes

### Minimum Scope Configuration
To protect user privacy and follow data minimization principles (DPDP Act, 2023), only basic identity scopes are requested:
- `openid`
- `email`
- `profile`

*No unnecessary scopes (e.g. Gmail, Drive, Calendar, Contacts) are requested.*

### Authorized Origins & Redirect URIs
In the Google Cloud Console (APIs & Services &rarr; Credentials &rarr; OAuth 2.0 Client IDs):
- **Authorized JavaScript Origins**:
  - Development: `http://localhost:5173`, `http://localhost:3000`
  - Production: `https://smart-travel-sage.vercel.app`
- **Authorized Redirect URIs**:
  - Development: `http://localhost:5173/login`, `http://localhost:5173`
  - Production: `https://smart-travel-sage.vercel.app/login`, `https://smart-travel-sage.vercel.app`

---

## 5. Environment Variables & Production Secrets

| Variable Name | Layer | Target Environments | Secret / Public | Description |
|---|---|---|---|---|
| `VITE_GOOGLE_CLIENT_ID` | Frontend (Vite) | Local `.env` & Vercel Dashboard | Public OAuth Client ID | Google Cloud OAuth 2.0 Client ID for GIS web initialization. |
| `GOOGLE_CLIENT_ID` | Backend (Spring Boot) | Local `.env` & Render Dashboard | Confidential Server Config | Used by `GoogleTokenVerifierImpl` to enforce audience validation. |
| `JWT_SECRET` | Backend (Spring Boot) | Local `.env` & Render Dashboard | Confidential Secret | HMAC-SHA512 signing secret for SmartTravel JWT tokens. |

> **Security Note**: No Google Client Secrets or private credentials are exposed in frontend code, bundles, or Git repositories.

---

## 6. Codebase Modifications Summary

### 6.1 Database & Entity Changes
- **`AuthProvider.java`**: Created enum (`LOCAL`, `GOOGLE`).
- **`User.java`**:
  - Added `authProvider` (defaults to `AuthProvider.LOCAL`).
  - Added `@Indexed(unique = false) private String googleSubject;` for verified Google `sub` identity lookups.
  - Added `avatarUrl` for Google profile pictures.
  - Updated constructors, Builder, and getters/setters.
- **`UserRepository.java`**:
  - Added `Optional<User> findByGoogleSubject(String googleSubject);`.

### 6.2 Backend Security & Authentication Logic
- **`pom.xml`**:
  - Added `com.google.api-client:google-api-client:2.6.0` and `com.google.http-client:google-http-client-gson:1.44.2` for official Google ID Token verification.
- **`GoogleTokenPayload.java`**:
  - Identity record encapsulating `subject`, `email`, `emailVerified`, `name`, `firstName`, `lastName`, and `pictureUrl`.
- **`GoogleLoginRequest.java`**:
  - Request DTO containing `@NotBlank String credential` and `boolean rememberMe`.
- **`GoogleTokenVerifier.java` & `GoogleTokenVerifierImpl.java`**:
  - Server-side component executing Google cryptographic verification, audience checking, issuer validation (`accounts.google.com`), and expiration checking.
- **`AuthService.java` & `AuthServiceImpl.java`**:
  - Implemented `authenticateWithGoogle(GoogleLoginRequest request)`:
    1. Verifies Google ID token via `GoogleTokenVerifier`.
    2. Searches by `googleSubject`.
    3. If not found, searches by verified normalized email and safely links Google identity without creating duplicate records.
    4. If brand new, registers new user with `AuthProvider.GOOGLE`, default `ROLE_USER`, and initialized `UserPreferences`.
    5. Validates account status (`ACTIVE`, not `DELETED`/`SUSPENDED`).
    6. Issues standard SmartTravel JWT access token via `JwtTokenProvider`.
- **`AuthController.java`**:
  - Added `POST /v1/auth/google` endpoint (with aliases `/api/v1/auth/google`, `/api/auth/google`, `/auth/google`).
- **`SecurityConfig.java`**:
  - Added `/api/auth/google/**`, `/v1/auth/google/**`, etc. to public `permitAll()` matchers.

### 6.3 Frontend Components & Authentication State
- **`types/auth.ts`**:
  - Extended `User` with optional `authProvider` and `avatarUrl`.
  - Added `GoogleLoginRequest` interface.
- **`services/authService.ts`**:
  - Added `loginWithGoogle(credential: string, rememberMe?: boolean)` method that dispatches to `POST /v1/auth/google` and syncs token storage.
- **`context/AuthContext.tsx`**:
  - Added `loginWithGoogle` to `AuthContextType` and `AuthProvider`.
- **`components/GoogleSignInButton.tsx`**:
  - Responsive, dark-themed button featuring the official Google vector logo, dynamic script loading (`https://accounts.google.com/gsi/client`), loading states, and error handling.
- **`pages/LoginPage.tsx` & `pages/RegisterPage.tsx`**:
  - Integrated `GoogleSignInButton` with divider and fallback email authentication options.

### 6.4 Privacy Policy Updates
- **`pages/PrivacyPolicyPage.tsx`**:
  - **Section 5 (Account & Traveler Data)**: Documented Google Federated Authentication, data fields received (`sub`, verified email, display name, avatar), purpose limitation, and minimum scope usage.
  - **Section 13 (Third-Party Service Providers)**: Added Google LLC (Google Identity Services) to authorized third-party identity partners.

---

## 7. Verification and Automated Test Results

### 7.1 Backend Test Suite Execution (`.\mvnw.cmd test`)
All **670 unit and integration tests passed cleanly**:

```
[INFO] Results:
[INFO] 
[INFO] Tests run: 670, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  04:44 min
```

#### Dedicated Google Authentication Test Breakdown:
1. `GoogleTokenVerifierTest.testValidGoogleCredential`: **PASS** (Cryptographic validation, claim extraction).
2. `GoogleTokenVerifierTest.testMissingCredential`: **PASS** (Missing/blank credential rejected with 401).
3. `GoogleTokenVerifierTest.testExpiredGoogleToken`: **PASS** (Expired credential rejected).
4. `GoogleTokenVerifierTest.testWrongAudience`: **PASS** (Audience mismatch rejected).
5. `GoogleTokenVerifierTest.testWrongIssuer`: **PASS** (Invalid issuer rejected).
6. `GoogleTokenVerifierTest.testMissingClaims`: **PASS** (Missing subject or email rejected).
7. `GoogleAuthServiceTest.testExistingGoogleUserLogin`: **PASS** (Recognizes verified `googleSubject`).
8. `GoogleAuthServiceTest.testNewGoogleUserRegistration`: **PASS** (Creates new user with `ROLE_USER` and preferences).
9. `GoogleAuthServiceTest.testDuplicatePrevention`: **PASS** (Repeated logins do not duplicate accounts).
10. `GoogleAuthServiceTest.testExistingAccountLinking`: **PASS** (Safely links `googleSubject` to existing local user).
11. `GoogleAuthServiceTest.testSuspendedUserGoogleLogin`: **PASS** (Suspended accounts rejected with `ForbiddenException`).
12. `GoogleAuthControllerTest.testGoogleLoginSuccess`: **PASS** (`POST /v1/auth/google` returns 200 OK + JWT).
13. `GoogleAuthControllerTest.testGoogleLoginValidationFailure`: **PASS** (Empty payload returns 400 Bad Request).
14. `GoogleAuthControllerTest.testGoogleLoginUnauthorized`: **PASS** (Invalid token returns 401 Unauthorized).
15. `AuthServiceTest` & `AuthControllerTest`: **PASS** (Email/password login regression tests passed).
16. `Phase12SecurityAuditIntegrationTest`: **PASS** (All 10 RBAC and security audit checks passed).
17. `ProductionSecurityEndpointsIntegrationTest`: **PASS** (All 13 production security endpoint checks passed).

### 7.2 Frontend Production Build Verification (`npm run build`)
Clean TypeScript compilation and Vite production bundling:

```
> smarttravel-frontend@1.0.0 build
> tsc && vite build

vite v5.4.21 building for production...
transforming...
✓ 1865 modules transformed.
rendering chunks...
dist/index.html                                         2.07 kB │ gzip:  0.85 kB
dist/assets/GoogleSignInButton-BSR3-QdZ.js              3.68 kB │ gzip:  1.95 kB
dist/assets/LoginPage-fUiw8k1b.js                       4.97 kB │ gzip:  1.77 kB
dist/assets/RegisterPage-C0LWA3-H.js                    8.89 kB │ gzip:  2.33 kB
dist/assets/PrivacyPolicyPage-D-6LtVr4.js              40.52 kB │ gzip:  9.72 kB
✓ built in 21.86s
```

---

## 8. Compliance with Core Requirements

| Internship Requirement | Status | Verification Note |
|---|---|---|
| **Requirement #1 — Live Flight Tracking** | **PASS** | Authenticated Google users retain full access to live radar telemetry, WebSocket subscriptions, and delay alerts. |
| **Requirement #2 — Dynamic Pricing** | **PASS** | Real-time dynamic pricing curves, price freeze creation, and price history tracking function seamlessly for Google users. |
| **Requirement #3 — Cancellation & Refund** | **PASS** | Automated tiered refund calculations and Razorpay refund disbursements remain fully functional. |
| **Requirement #4 — Seat & Room Selection** | **PASS** | Interactive cabin seat map reservations and luxury hotel room grid allocations persist to user profile seamlessly. |

---

## 9. Remaining Manual Google Cloud Console Steps for Production
To activate the live Google OAuth prompt in production:
1. Go to [Google Cloud Console](https://console.cloud.google.com/) &rarr; **APIs & Services** &rarr; **Credentials**.
2. Select your Web OAuth 2.0 Client ID.
3. Under **Authorized JavaScript origins**, ensure both origins are present:
   - `http://localhost:5173`
   - `https://smart-travel-sage.vercel.app`
4. Under **Authorized redirect URIs**, ensure:
   - `http://localhost:5173/login`
   - `https://smart-travel-sage.vercel.app/login`
5. In **Vercel Project Settings** &rarr; **Environment Variables**:
   - Set `VITE_GOOGLE_CLIENT_ID` = `YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com`
6. In **Render Service Environment**:
   - Set `GOOGLE_CLIENT_ID` = `YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com`
