# SmartTravel Platform — Production Deployment & Release Checklist

This document details the exact procedures, environment variable requirements, and validation gates for deploying the SmartTravel platform to staging and production environments.

---

## 1. Architecture & Deployment Targets

```
                                  [ Internet / CDN ]
                                          │
                   ┌──────────────────────┴──────────────────────┐
                   │                                             │
                   ▼                                             ▼
        [ Frontend (SPA) ]                            [ Backend (Spring Boot) ]
        Hosting: Vercel / Netlify / AWS S3+CloudFront Hosting: AWS ECS / Railway / Render / DigitalOcean
        Runtime: Static HTML/JS/CSS                   Runtime: OpenJDK 21 (Temurin / Corretto)
        Port: 80 / 443                                Port: 8080 (Configurable via SERVER_PORT)
                   │                                             │
                   └──────────────────────┬──────────────────────┘
                                          │
                                          ▼
                               [ MongoDB Atlas Cluster ]
                               Engine: MongoDB 6.0+ (M10+ recommended for Prod)
                               Topology: Replica Set (3-node)
```

---

## 2. Environment Variables Checklist

> [!IMPORTANT]
> Never commit actual secret values into git. Supply all variables via the host environment manager (e.g., AWS Secrets Manager, GitHub Actions Secrets, Vercel Environment Variables).

### Frontend Environment Variables (`.env.production`)

| Variable Name | Required | Description | Example / Format |
|---|---|---|---|
| `VITE_API_BASE_URL` | Yes | Fully qualified HTTPS URL of the production backend API | `https://api.smarttravel.com/api/v1` |
| `VITE_WS_BASE_URL` | Yes | Fully qualified WSS URL of the WebSocket endpoint | `wss://api.smarttravel.com/ws` |
| `VITE_SWAGGER_URL` | No | Public URL for API documentation (if enabled) | `https://api.smarttravel.com/swagger-ui.html` |

### Backend Environment Variables (`application-prod.yml`)

| Variable Name | Required | Description | Recommendation |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Yes | Active Spring profile | Must be `prod` |
| `SERVER_PORT` | No | HTTP listening port | Default: `8080` |
| `MONGODB_URI` | Yes | MongoDB Atlas replica-set connection string | `mongodb+srv://<user>:<password>@<cluster>.mongodb.net/smarttravel?retryWrites=true&w=majority` |
| `MONGODB_DATABASE` | No | Database name | `smarttravel` |
| `JWT_SECRET` | Yes | Base64-encoded 512-bit HMAC-SHA secret key | Generate with `openssl rand -base64 64` |
| `JWT_EXPIRATION_MS` | No | Access token TTL in milliseconds | Default: `86400000` (24 hours) |
| `JWT_REFRESH_EXPIRATION_MS` | No | Refresh token TTL in milliseconds | Default: `604800000` (7 days) |
| `CORS_ALLOWED_ORIGINS` | Yes | Comma-separated list of allowed frontend origins | `https://smarttravel.com,https://admin.smarttravel.com` |
| `RAZORPAY_ENABLED` | Yes | Enable live payment gateway processing | `true` |
| `RAZORPAY_KEY_ID` | Yes | Razorpay Public Key ID | `rzp_live_...` |
| `RAZORPAY_KEY_SECRET` | Yes | Razorpay Secret Key | From Razorpay Dashboard |
| `RAZORPAY_WEBHOOK_SECRET` | Yes | Razorpay Webhook HMAC-SHA256 signature secret | Configured in Razorpay Webhooks |
| `RAZORPAY_CURRENCY` | No | Transaction currency code | `INR` |
| `SWAGGER_UI_ENABLED` | No | Enable/disable public Swagger UI in production | `false` (recommended) |
| `SWAGGER_API_DOCS_ENABLED` | No | Enable/disable `/v3/api-docs` endpoint | `false` (recommended) |

---

## 3. Pre-Deployment Verification Gates

Execute all validation gates locally or in CI/CD pipeline prior to promotion:

```bash
# 1. Full Backend Test Suite (Target: 404 tests passing, 0 failures, 0 errors)
cd backend
mvn clean test

# 2. Frontend TypeScript Verification (Target: Exit code 0)
cd ../frontend
npx tsc --noEmit

# 3. Frontend Production Build (Target: Clean bundle generation)
npm run build

# 4. Secret Scan Check (Target: 0 matches)
git grep "rzp_live_"
git grep "mongodb+srv://"
git ls-files backend/.env
```

---

## 4. Step-by-Step Production Deployment Procedure

### Step 1: Database Provisioning (MongoDB Atlas)
1. Ensure IP Access List allows backend server IP range (or AWS VPC Peering / PrivateLink).
2. Verify dedicated database user with `readWrite` role on `smarttravel` database only.
3. Confirm automated daily backups and point-in-time recovery (PITR) are active.

### Step 2: Backend Service Deployment
1. Build the production executable JAR:
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```
2. Containerize or deploy JAR:
   ```bash
   java -Dspring.profiles.active=prod -jar target/smarttravel-backend-1.0.0.jar
   ```
3. Verify backend health probe returns HTTP 200:
   ```bash
   curl -i https://api.smarttravel.com/actuator/health
   ```

### Step 3: Frontend Deployment
1. Set production environment variables in build environment (`VITE_API_BASE_URL`, `VITE_WS_BASE_URL`).
2. Build SPA:
   ```bash
   cd frontend
   npm run build
   ```
3. Deploy `dist/` directory to static hosting/CDN.
4. Configure SPA routing fallback: rewrite all non-file routes (`/*`) to `/index.html`.

### Step 4: Webhook Configuration
1. In the Razorpay Dashboard → Settings → Webhooks:
   - Webhook URL: `https://api.smarttravel.com/api/v1/payments/webhook`
   - Secret: Matches `RAZORPAY_WEBHOOK_SECRET`
   - Active Events:
     - `payment.captured`
     - `payment.failed`
     - `refund.processed`
     - `refund.failed`

---

## 5. Post-Deployment Smoke Test Checklist

- [ ] `GET /actuator/health` returns `{"status":"UP"}`
- [ ] `GET /api/v1/health` returns operational status without exposing database credentials
- [ ] User Registration & JWT Login flow executes successfully
- [ ] Flight search returns live flights with correct cabin pricing
- [ ] Booking creation atomically reserves inventory
- [ ] Payment order initialization returns valid Razorpay Order ID
- [ ] Ticket issuance and PDF download render properly
- [ ] Check-in generates boarding passes
- [ ] Customer cancellation releases seats and initiates refund record
- [ ] Admin user login accesses `/admin` analytics dashboard
- [ ] Non-admin user receives HTTP 403 when attempting `/api/v1/admin/*` access

---

## 6. Rollback Procedures

If critical defects are identified post-deployment:
1. **Frontend Rollback**: Revert CDN deployment to previous release tag (instant static swap).
2. **Backend Rollback**: Redeploy previous Docker container image tag or JAR version.
3. **Database Safeguard**: Backward-compatible schema design ensures MongoDB documents remain valid across minor version rollbacks.
