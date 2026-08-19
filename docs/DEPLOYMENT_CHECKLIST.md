# SmartTravel Platform — Production Deployment & Release Checklist

> **Release**: v1.0.0 | **Commit**: 905a170 | **Tagged**: `git tag v1.0.0` pushed to origin

---

## 1. Architecture & Deployment Targets

```
                                  [ Internet / CDN ]
                                          │
                   ┌──────────────────────┴──────────────────────┐
                   │                                             │
                   ▼                                             ▼
        [ Frontend SPA — Vercel ]               [ Backend API — Render / Docker ]
        Runtime: Static HTML/JS/CSS              Runtime: OpenJDK 21 (Temurin)
        Config: frontend/vercel.json             Config: render.yaml + backend/Dockerfile
        URL: Assigned by Vercel on deploy        URL: Assigned by Render on deploy
                   │                                             │
                   └──────────────────────┬──────────────────────┘
                                          │
                                          ▼
                               [ MongoDB Atlas — AWS ap-south-1 ]
                               Engine: MongoDB 6.0+
                               Topology: 3-node Replica Set (atlas-8fdw7o-shard-0)
                               Region: AWS AP_SOUTH_1
```

---

## 2. Release Artifacts

| Artifact | Status | Location |
|---|---|---|
| Backend JAR | ✅ Built | `backend/target/smarttravel-backend-1.0.0.jar` (44 MB) |
| Frontend Dist | ✅ Built | `frontend/dist/` (gzipped main: 82.55 kB) |
| Backend Dockerfile | ✅ Created | `backend/Dockerfile` |
| Frontend Dockerfile | ✅ Created | `frontend/Dockerfile` |
| Nginx Config (SPA) | ✅ Created | `frontend/nginx.conf` |
| Vercel Config | ✅ Created | `frontend/vercel.json` |
| Render Blueprint | ✅ Created | `render.yaml` |
| Prod Profile | ✅ Created | `backend/src/main/resources/application-prod.yml` |
| Git Tag | ✅ Pushed | `v1.0.0` → `origin/main` |

---

## 3. Build Commands

### Backend (requires Java 21)
```bash
# Run full test suite first
./mvnw clean test

# Build production JAR
./mvnw clean package -DskipTests

# Generated JAR
java -Dspring.profiles.active=prod -jar target/smarttravel-backend-1.0.0.jar
```

### Frontend
```bash
# Type check
npx tsc --noEmit

# Production build
npm run build
# Output: dist/
```

---

## 4. Environment Variables Checklist

> [!IMPORTANT]
> Never commit actual secret values into git. Supply all variables via the host environment manager.

### Frontend — Vercel Environment Variables (Project Settings → Environment Variables)

| Variable | Required | Description |
|---|---|---|
| `VITE_API_BASE_URL` | **Yes** | `https://<your-render-backend>.onrender.com` |
| `VITE_WS_BASE_URL` | **Yes** | `wss://<your-render-backend>.onrender.com/ws` |
| `VITE_RAZORPAY_KEY_ID` | **Yes** | Your Razorpay `rzp_live_...` Key ID (public, safe in frontend) |
| `VITE_SWAGGER_URL` | No | `https://<backend-url>/swagger-ui.html` (if enabled) |

### Backend — Render Service Environment Variables

| Variable | Required | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | **Yes** | `prod` |
| `MONGODB_URI` | **Yes** | Full Atlas connection string |
| `MONGODB_DATABASE` | No | `smarttravel` |
| `JWT_SECRET` | **Yes** | Base64-encoded 512-bit key (`openssl rand -base64 64`) |
| `JWT_EXPIRATION_MS` | No | `86400000` |
| `CORS_ALLOWED_ORIGINS` | **Yes** | Your Vercel frontend URL e.g. `https://smarttravel.vercel.app` |
| `RAZORPAY_ENABLED` | **Yes** | `true` |
| `RAZORPAY_KEY_ID` | **Yes** | Razorpay Key ID |
| `RAZORPAY_KEY_SECRET` | **Yes** | Razorpay Key Secret |
| `RAZORPAY_WEBHOOK_SECRET` | **Yes** | Razorpay Webhook HMAC Secret |
| `RAZORPAY_CURRENCY` | No | `INR` |
| `SWAGGER_UI_ENABLED` | No | `false` (recommended) |
| `SWAGGER_API_DOCS_ENABLED` | No | `false` (recommended) |

---

## 5. Deployment Procedure

### Step 1 — MongoDB Atlas (Pre-existing)
- ✅ Atlas cluster `atlas-8fdw7o-shard-0` connected and verified (3-node replica set, AWS ap-south-1)
- ✅ Connection tested by all 404 integration tests
- Add Render backend server IP to MongoDB Atlas IP Access List before go-live

### Step 2 — Backend on Render
1. Go to [https://render.com](https://render.com) → "New Web Service"
2. Connect GitHub repository: `sivakumarelango10-art/smart-travel`
3. Set **Root Directory**: `backend`
4. Set **Dockerfile path**: `./Dockerfile`
5. Set **Docker Build Context**: `./` (auto-detected from render.yaml)
6. Set **Health Check Path**: `/actuator/health`
7. Add all backend environment variables (see table above) in "Environment" tab
8. Deploy

### Step 3 — Frontend on Vercel
1. Go to [https://vercel.com](https://vercel.com) → "New Project"
2. Import GitHub repository: `sivakumarelango10-art/smart-travel`
3. Set **Root Directory**: `frontend`
4. Framework Preset: **Vite**
5. Build Command: `npm run build`
6. Output Directory: `dist`
7. Add all frontend environment variables (see table above)
8. Deploy

> [!IMPORTANT]
> Vercel's SPA routing fallback is configured via `frontend/vercel.json` — all unknown routes are rewritten to `/index.html`. No additional manual configuration needed.

### Step 4 — CORS Update
After Vercel assigns your frontend URL (e.g. `https://smarttravel.vercel.app`):
- Update Render backend environment variable `CORS_ALLOWED_ORIGINS` to the exact Vercel URL
- Redeploy backend if already running

### Step 5 — Razorpay Webhook
In Razorpay Dashboard → Settings → Webhooks:
- **Webhook URL**: `https://<your-render-backend>.onrender.com/api/v1/payments/webhook`
- **Secret**: Must match `RAZORPAY_WEBHOOK_SECRET` environment variable
- **Active Events**: `payment.captured`, `payment.failed`, `refund.processed`, `refund.failed`

---

## 6. Health Check URLs (post-deployment)

```bash
# Backend operational health
GET https://<backend>.onrender.com/api/v1/health

# Spring Actuator liveness
GET https://<backend>.onrender.com/actuator/health

# Frontend home
GET https://<your-app>.vercel.app/
```

Expected responses:
- Backend: `HTTP 200`, body contains database status UP
- Actuator: `HTTP 200`, `{"status":"UP"}`
- Frontend: `HTTP 200`, React SPA loads

---

## 7. Post-Deployment Smoke Test Checklist

> [!NOTE]
> Use Razorpay **test mode** credentials for initial verification (not live production keys).

- [ ] Frontend loads on Vercel URL without errors
- [ ] Register a new customer account
- [ ] Login and receive JWT
- [ ] Search flights — results displayed
- [ ] Open flight detail and seat map
- [ ] Create a booking (inventory atomically decremented)
- [ ] Generate Razorpay payment order
- [ ] Simulate Razorpay test payment
- [ ] Verify webhook received and processed (booking status → CONFIRMED)
- [ ] Issue and download ticket PDF
- [ ] Complete online check-in
- [ ] Download boarding pass PDF
- [ ] View My Bookings
- [ ] Cancel a booking (seats released, refund initiated)
- [ ] View notifications
- [ ] Log out
- [ ] Admin login with `admin.*` email
- [ ] Access Admin Dashboard and Analytics
- [ ] Verify customer JWT denied access to `/api/v1/admin/**` (HTTP 403)

---

## 8. Security Post-Deployment Verification

- [ ] All endpoints served over HTTPS
- [ ] `X-Frame-Options: DENY` present on responses
- [ ] `X-Content-Type-Options: nosniff` present
- [ ] No stack traces in API error responses
- [ ] No MongoDB URI or credentials in API responses
- [ ] Swagger UI disabled (`/swagger-ui.html` returns 404 or 403)
- [ ] `/actuator/env` not exposed (returns 404)
- [ ] Admin endpoints return 403 for non-admin JWT
- [ ] Razorpay webhook rejects invalid HMAC signatures
- [ ] CORS rejects cross-origin requests from non-allowlisted domains

---

## 9. Performance Baseline (Local Dev)

| Endpoint | Avg (ms) | Min (ms) | Max (ms) | Status |
|---|---|---|---|---|
| `GET /api/v1/health` | 108 | 69 | 131 | 200 OK |
| `GET /api/v1/flights` | 162 | 107 | 198 | 200 OK |
| `GET /actuator/health` | 208 | 138 | 317 | 200 OK |
| Admin Analytics (authed) | 14 | 11 | 18 | 200 OK |

> Production measurements must be taken from the actual deployed Render URL and updated here.

---

## 10. Rollback Procedure

```bash
# Step 1 — Roll back to the previous stable commit
git log --oneline             # Find target commit SHA
git checkout e02a6d6          # Detach to the stable pre-deploy commit

# Step 2 — Backend rollback
# On Render: Settings → Manual Deploy → Deploy previous commit

# Step 3 — Frontend rollback
# On Vercel: Deployments → find previous deployment → Redeploy

# No database rollback is required:
# All schema changes are backward-compatible (additive-only MongoDB documents)
```

**Previous stable commit**: `e02a6d6` — `chore(release): finalize production readiness audit`

---

## 11. Monitoring Checklist

- [ ] Render health check alerts configured (HTTP on `/actuator/health`)
- [ ] MongoDB Atlas monitoring alerts set (CPU > 80%, connections > threshold)
- [ ] Log streaming enabled on Render (stdout/stderr)
- [ ] Razorpay Dashboard → Payments monitored for failures
- [ ] Consider uptime monitoring (e.g. UptimeRobot) on `/api/v1/health`
