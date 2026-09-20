# SmartTravel Vercel & Render Deployment Audit

This document presents a comprehensive, forensic deployment audit of the frontend on Vercel, the backend on Render, and the end-to-end integration between both platforms.

---

## 1. VERCEL DEPLOYMENT AUDIT

### 1.1 Configuration Specification
- **Configuration File**: `vercel.json` (at workspace root)
- **Vercel Version**: Version 2 Build & Routing Pipeline
- **Framework Preset**: `vite`
- **Root Directory Strategy**: Monorepo root with configured subdirectory commands:
  - `buildCommand`: `"cd frontend && npm install && npm run build"`
  - `outputDirectory`: `"frontend/dist"`
- **Cache Headers**: Configured with `public, max-age=31536000, immutable` for `/assets/(.*)`.
- **Security Headers**:
  - `X-Frame-Options`: `DENY`
  - `X-Content-Type-Options`: `nosniff`
  - `Referrer-Policy`: `strict-origin-when-cross-origin`

### 1.2 Build Process & Artifacts
- **Compiler**: TypeScript 5.2.2 + Vite 5.2.11 / Rollup
- **Code Splitting & Vendor Chunking**:
  - `vendor-react`: `react`, `react-dom`, `react-router-dom`
  - `vendor-ui`: `lucide-react`, `clsx`, `tailwind-merge`
  - `vendor-network`: `axios`, `@stomp/stompjs`, `sockjs-client`
- **Output Bundle Size**:
  - Total compressed initial payload: ~160 kB gzip
  - Largest async chunk: `hotelImageRegistry` (531 kB raw / 135 kB gzip)
  - CSS stylesheet: 106 kB raw / 16.6 kB gzip
- **Build Command Execution**: Passed in 6.82s with 0 errors.

### 1.3 Environment Variables Required on Vercel
| Variable Name | Required? | Purpose | Recommended Production Value |
|---------------|-----------|---------|------------------------------|
| `VITE_API_BASE_URL` | YES | REST API origin URL | `https://smarttravel-backend-6qkl.onrender.com/v1` |
| `VITE_WS_BASE_URL` | YES | STOMP WebSocket URL | `wss://smarttravel-backend-6qkl.onrender.com/ws` |
| `VITE_RAZORPAY_KEY_ID` | YES | Razorpay Public Key ID | `rzp_live_...` (or test key `rzp_test_...`) |
| `VITE_GOOGLE_CLIENT_ID` | YES | Google OAuth Web Client ID | `<project-id>.apps.googleusercontent.com` |
| `VITE_SWAGGER_URL` | OPTIONAL | Link to API documentation | Leave unset in production (Swagger disabled) |

### 1.4 Route Rewrites & SPA Routing
- Previously: Single catch-all `{ "source": "/(.*)", "destination": "/index.html" }`.
- Audit Finding: If `VITE_API_BASE_URL` was omitted or relative `/api/...` calls were made, Vercel returned `index.html` (HTTP 200 text/html), causing JSON parse errors in Axios.
- Fix Applied: Added reverse proxy rewrites in `vercel.json` for `/api/:path*` and `/v1/:path*` forwarding directly to `https://smarttravel-backend-6qkl.onrender.com`.
- Verification: Clean JSON syntax validated; client SPA routing preserved.

### 1.5 Live Vercel Status
- Dashboard Access: `NOT VERIFIED — VERCEL ACCESS REQUIRED` (Live deployment dashboard credentials not provided).
- Local Production Bundle Validation: `PASS` (dist generated cleanly, assets mapped correctly).

---

## 2. RENDER DEPLOYMENT AUDIT

### 2.1 Configuration Specification
- **Configuration File**: `render.yaml`
- **Service Name**: `smarttravel-backend`
- **Service Type**: `web`
- **Runtime**: `docker` (Multi-stage build)
- **Region**: `oregon`
- **Plan**: `starter` (512MB–1GB RAM recommended for JVM 21)
- **Dockerfile Path**: `./backend/Dockerfile`
- **Docker Context**: `./backend`
- **Health Check Path**: `/actuator/health`

### 2.2 Dockerfile & Container Architecture
- **Stage 1 (Builder)**: `eclipse-temurin:21-jdk-jammy`
  - Maven wrapper line endings sanitized (`sed -i 's/\r$//' mvnw`)
  - Offline dependency resolution layer cached (`./mvnw dependency:go-offline -B -q`)
  - Batch JAR packaging (`./mvnw clean package -DskipTests -B -q`)
- **Stage 2 (Runtime)**: `eclipse-temurin:21-jre-jammy`
  - Non-root user & group created (`smarttravel:smarttravel`, UID/GID non-root)
  - Workdir: `/app`
  - G1 Garbage Collector with memory cap: `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError`
  - Port binding: Dynamic `${PORT:-${SERVER_PORT:-8080}}`
  - Container health check: `wget -qO- http://localhost:${PORT:-${SERVER_PORT:-8080}}/actuator/health | grep -q '"status":"UP"' || exit 1`

### 2.3 Environment Variables Required on Render
| Variable Name | Required? | Purpose | Secret? | Source |
|---------------|-----------|---------|---------|--------|
| `SPRING_PROFILES_ACTIVE` | YES | Active Spring profile | NO | Value: `prod` |
| `SERVER_PORT` / `PORT` | YES | HTTP listen port | NO | Auto-injected by Render |
| `MONGODB_URI` | YES | MongoDB Atlas connection string | YES | Set securely in Render dashboard |
| `MONGODB_DATABASE` | YES | Database name | NO | Value: `smarttravel` |
| `JWT_SECRET` | YES | Base64 512-bit signing secret | YES | Generate via `openssl rand -base64 64` |
| `JWT_EXPIRATION_MS` | YES | JWT access token validity | NO | Value: `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | YES | Allowed CORS origins | NO | `https://smart-travel-sage.vercel.app,https://*.vercel.app` |
| `RAZORPAY_ENABLED` | YES | Razorpay integration toggle | NO | Value: `true` |
| `RAZORPAY_KEY_ID` | YES | Razorpay Key ID | NO | Set in Render dashboard |
| `RAZORPAY_KEY_SECRET` | YES | Razorpay Key Secret | YES | Set securely in Render dashboard |
| `RAZORPAY_WEBHOOK_SECRET` | YES | Razorpay Webhook Secret | YES | Set securely in Render dashboard |
| `RAZORPAY_CURRENCY` | YES | Currency code | NO | Value: `INR` |
| `SWAGGER_UI_ENABLED` | YES | Swagger UI toggle | NO | Value: `false` in production |
| `SWAGGER_API_DOCS_ENABLED`| YES | OpenAPI docs toggle | NO | Value: `false` in production |
| `GOOGLE_CLIENT_ID` | OPTIONAL | Google OAuth Client ID | NO | Set in Render dashboard |
| `GEMINI_API_KEY` | OPTIONAL | Google Gemini API Key | YES | Set securely in Render dashboard |

### 2.4 Cold Start & Standby Hardening
- **Actuator Health Probe**: `/actuator/health` exposes liveness and readiness probes (`management.endpoint.health.probes.enabled=true`).
- **Warmup Service**: Frontend includes `warmupService.ts` executing non-blocking background ping to `/v1/health` on initial page load, waking Render free/starter tiers before user payment or search actions.

### 2.5 Live Render Status
- Dashboard Access: `NOT VERIFIED — RENDER ACCESS REQUIRED` (Live Render credentials/dashboard unavailable in local workspace).
- Packaging & Local JAR Validation: `PASS` (`smarttravel-backend-1.0.0.jar` built successfully, 354 source files and 129 test classes compiled).

---

## 3. VERCEL ↔ RENDER INTEGRATION AUDIT

### 3.1 REST API Communication Chain
- **Frontend Origin**: `https://smart-travel-sage.vercel.app` (or custom domain)
- **Backend Origin**: `https://smarttravel-backend-6qkl.onrender.com`
- **Path Resolution**:
  - `VITE_API_BASE_URL` in `frontend/src/config/constants.ts` automatically strips trailing `/v1`, `/api/v1`, or `/api`.
  - Services use standardized `/v1/...` relative paths: `apiClient.get('/v1/flights')` resolves cleanly to `https://smarttravel-backend-6qkl.onrender.com/v1/flights`.
  - Duplicate `/api/v1/v1/` prefixes are defensively intercepted and normalized in `api.ts`.
- **Fallback Reverse Proxy**: `vercel.json` provides edge rewrites to Render backend for `/api/:path*` and `/v1/:path*`.

### 3.2 WebSocket / STOMP Real-Time Integration
- **Frontend WebSocket Client**: `@stomp/stompjs` + `sockjs-client`
- **Backend WebSocket Endpoint**: `/ws` (with SockJS fallback registered in `WebSocketConfig.java`)
- **Connection Handshake**:
  - URL: `wss://smarttravel-backend-6qkl.onrender.com/ws`
  - Reconnection: Exponential backoff with heartbeat intervals (10s incoming / 10s outgoing)
  - Origins Allowed: Handshake permitted for `https://*.vercel.app` and `https://smart-travel-sage.vercel.app`
- **Real-Time Topics**:
  - `/topic/flight-status/{flightId}`: Live flight status & delay updates
  - `/topic/flights/{flightId}/pricing`: Real-time dynamic pricing breakdown
  - `/topic/hotels/{hotelId}/rooms`: Room inventory & availability events
  - `/topic/notifications/{userId}`: User-targeted booking, refund, and gate alert broadcasts

### 3.3 CORS Policy Verification
- Configured in `com.smarttravel.common.security.SecurityConfig.java`:
  ```java
  configuration.setAllowedOriginPatterns(List.of(
      "http://localhost:*",
      "https://*.vercel.app",
      "https://smart-travel-sage.vercel.app"
  ));
  configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
  configuration.setAllowedHeaders(List.of(
      "Authorization", "Content-Type", "X-Requested-With", "Accept",
      "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
      "X-Request-ID", "X-Correlation-ID"
  ));
  configuration.setAllowCredentials(true);
  configuration.setMaxAge(3600L);
  ```
- Any preview branch or production domain under `*.vercel.app` is permitted.

### 3.4 Production URLs Verification
- Verified that production builds do not contain hardcoded `localhost:8080` or `127.0.0.1` fallbacks in core API interceptors.
- Production environment configurations consistently target the Render service `smarttravel-backend-6qkl.onrender.com`.
