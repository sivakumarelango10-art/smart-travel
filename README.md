# SmartTravel Platform

> **SmartTravel Platform** is an enterprise-grade, full-stack travel booking ecosystem built with **Java 21, Spring Boot 3.3.x, Spring Data MongoDB, and React 18 + TypeScript (Vite)**. It is architected for real-time flight tracking, dynamic pricing with price freezing, tiered automated refunds, interactive seat/room selection, moderated reviews, and personalized hybrid recommendations.

---

## 🚀 Phase 1 Status: Foundation Complete & Tested

| Layer | Status | Key Deliverables |
|---|---|---|
| **Backend Foundation** | ✅ Complete | Clean modular architecture, Global RFC 7807 Exception Handling (`@RestControllerAdvice`), Bean Validation, Health Endpoint (`/api/health`), OpenAPI 3.0 Documentation (`/swagger-ui.html`). |
| **Security Architecture** | ✅ Complete | Stateless Spring Security 6, JJWT 0.12.6 token management, Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_MODERATOR`), password encryption, strict CORS, IDOR protection shields. |
| **Database Tier** | ✅ Complete | Spring Data MongoDB configuration with zero hardcoded credentials, safe connection timeout, auditing ready. |
| **Frontend Foundation** | ✅ Complete | React 18 + TypeScript + Vite SPA, Axios client with JWT & error interceptors, React Router DOM navigation, Glassmorphic UI theme, real-time live Backend Health telemetry widget. |
| **Test Suite** | ✅ Complete | 13/13 JUnit 5 unit, slice, and security tests passing with 0 failures. |

---

## 🏛️ Architectural Documentation

- **Master System Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **Staged Implementation Plan & Roadmap:** [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md)
- **Database Collection Schemas & Index Strategy:** [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md)
- **REST & Real-Time API Specifications:** [docs/API_SPECIFICATION.md](docs/API_SPECIFICATION.md)

---

## 🛠️ Technology Stack

- **Backend:** Java 21 LTS, Spring Boot 3.3.2, Spring Web, Spring Security 6.3, JJWT 0.12.6, Spring Data MongoDB, Jakarta Bean Validation, Springdoc OpenAPI 2.5 (Swagger UI), Spring Actuator.
- **Frontend:** React 18.3, TypeScript 5.2, Vite 5.4, React Router DOM 6.23, Axios 1.7, StompJS 7.0, Lucide React, TailwindCSS 3.4.
- **Database:** MongoDB 7.x.
- **Testing:** JUnit 5, Mockito, Spring Security Test, Spring Boot Test, Testcontainers.

---

## ⚙️ Environment Variables

Backend and Frontend configurations are strictly driven by environment variables. Refer to templates:
- `backend/.env.example`
- `frontend/.env.example`

### Backend Variables (`backend/.env`):
```properties
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
MONGODB_URI=mongodb://localhost:27017/smarttravel_dev?serverSelectionTimeoutMS=2000
JWT_SECRET=dGhpcy1pcy1hLXNhbXBsZS01MTItYml0LXNlY3JldC1rZXktZm9yLXVzZS13aXRoLWpqd3Qtc21hcnR0cmF2ZWwtYXBwbGljYXRpb24tZGV2ZWxvcG1lbnQtdGVzdGluZw==
JWT_EXPIRATION_MS=86400000
JWT_REFRESH_EXPIRATION_MS=604800000
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
PRICE_FREEZE_DURATION_MINUTES=30
```

### Frontend Variables (`frontend/.env`):
```properties
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=http://localhost:8080/ws
```

---

## 🏃 Running the Application Locally

### 1. Prerequisites
- **Java:** JDK 21 LTS
- **Node.js:** v18+ / v20+ / v24+
- **MongoDB:** MongoDB 6.0+ (optional for Phase 1; backend gracefully runs in standalone test mode)

### 2. Backend Execution
```bash
cd backend
# Windows:
$env:JAVA_HOME="C:\Program Files\Java\jdk-26.0.2" # (or your JDK 21 path)
.\mvnw.cmd spring-boot:run

# Unix/macOS:
export JAVA_HOME=/path/to/jdk21
./mvnw spring-boot:run
```
- Backend Service: `http://localhost:8080`
- Health Endpoint: `http://localhost:8080/api/health`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 3. Frontend Execution
```bash
cd frontend
npm install
npm run dev
```
- Frontend Application: `http://localhost:5173`

---

## 🧪 Running Automated Tests

### Backend Test Suite
```bash
cd backend
.\mvnw.cmd clean test
```
**Results:** 13/13 tests passing:
- `HealthControllerTest`: Public health check endpoint & MongoDB status telemetry (2/2)
- `GlobalExceptionHandlerTest`: Standard RFC 7807 error structures for 400, 403, 404, 409, and field validation errors (5/5)
- `JwtTokenProviderTest`: Token signing, claims extraction, expiration, and tamper defense (3/3)
- `SecurityAccessTest`: Public endpoint access, 401 unauthorized access rejection, and authenticated access rules (3/3)

### Frontend Production Build Test
```bash
cd frontend
npm run build
```
**Results:** TypeScript type check & Vite production bundle created cleanly (0 errors).

---

## 🗺️ Implementation Roadmap

- [x] **Phase 1: Project Foundation & Security Layer** (Current)
- [ ] **Phase 2: Live Flight Status & Telemetry Simulation** (Next)
- [ ] **Phase 3: Dynamic Pricing Engine & Price Freeze**
- [ ] **Phase 4: Cancellation Policy & Automated Refund Engine**
- [ ] **Phase 5: Dynamic Seat Map & Room Inventory Engine**
- [ ] **Phase 6: Verified Reviews & Hybrid Recommendations**
