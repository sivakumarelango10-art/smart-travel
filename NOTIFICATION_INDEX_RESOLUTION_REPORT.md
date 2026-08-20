# SmartTravel Notification Idempotency Index Resolution Report

**Date**: 2026-08-20  
**Project**: SmartTravel Platform Backend Engine  
**Release**: Production Index Hardening & Idempotency Resolution  
**Regression Test Suite**: 486 / 486 Tests Passed (0 Failures, 0 Errors, 0 Skipped)  
**Frontend Build**: 1,769 Modules Transformed (0 Errors)  

---

## 1. Root Cause

During Render production startup with `auto-index-creation: true`, Spring Data MongoDB encountered existing duplicate `idempotencyKey` records in the `smarttravel.notifications` collection left behind by historical multi-threaded concurrency tests. When MongoDB attempted to execute `createIndex({"idempotencyKey": 1}, {unique: true})`, it threw:
```
E11000 duplicate key error collection: smarttravel.notifications index: idempotencyKey dup key: { idempotencyKey: "concur_fl_1787108737261:concur_evt_1787108737261:concur_user_1787108737261:FLIGHT_DELAYED:EMAIL" }
```
Because the index creation was not resilient to existing duplicates, the entire application failed to start up.

---

## 2. Exact Duplicate Keys Found

The diagnostic inspection (`NotificationDuplicateDiagnosticRunnerTest`) scanned the entire `notifications` collection via MongoDB Aggregation and identified:

- **Duplicate Key**: `concur_fl_1787108737261:concur_evt_1787108737261:concur_user_1787108737261:FLIGHT_DELAYED:EMAIL`
- **Duplicate Document Count**: 8 documents
- **Classification**: Historical concurrency test artifacts created on Wed Aug 19 08:35:37 IST 2026.
- **Legitimate Production Groups**: 0 affected.

---

## 3. Number of Duplicate Notification Documents

- Total duplicate groups: **1 group**
- Total documents in group: **8 documents**
- Total orphaned duplicate documents removed: **7 documents**
- Total authoritative documents preserved: **1 document**

---

## 4. Authoritative Records Preserved

- **Preserved Document ID**: `6a851d818ddb3c476c7715dd`
- **Flight ID**: `concur_fl_1787108737261`
- **User ID**: `concur_user_1787108737261`
- **Type**: `FLIGHT_DELAYED`
- **Channel**: `EMAIL`
- **Status**: `SENT`
- **Reason**: Highest authority score (status `SENT` with valid timestamp).

---

## 5. Duplicate Records Removed

The following 7 orphaned copies were safely removed:
1. `6a851d818ddb3c476c7715d8`
2. `6a851d818ddb3c476c7715de`
3. `6a851d818ddb3c476c7715da`
4. `6a851d818ddb3c476c7715d9`
5. `6a851d818ddb3c476c7715db`
6. `6a851d818ddb3c476c7715dc`
7. `6a851d818ddb3c476c7715df`

---

## 6. Index Before Fix

- `idempotencyKey`: Un-indexed or failed unique index creation due to `E11000 duplicate key error`.

---

## 7. Index After Fix

- `idempotencyKey_1`: **`unique: true`** verified directly on MongoDB Atlas collection.
- Compound Index `notification_user_created_idx`: `{'userId': 1, 'createdAt': -1}`
- Compound Index `notification_user_read_idx`: `{'userId': 1, 'read': 1}`
- Compound Index `notification_status_retry_idx`: `{'status': 1, 'retryCount': 1}`

---

## 8. Unique Constraint Verification

`NotificationIndexInitializer.isUniqueIdempotencyIndexValid()` programmatically verifies the index status on application startup.
Direct duplicate insertion test confirmed:
- `mongoTemplate.insert(n2)` fails immediately with `DataIntegrityViolationException` / `DuplicateKeyException`.

---

## 9. Concurrency Behavior

In `NotificationServiceImpl.java`:
1. `sendNotification(...)` executes deterministic composite key generation.
2. In-memory check against `findByIdempotencyKey(...)` handles sequential duplicate calls.
3. If 10+ concurrent requests bypass step 1 simultaneously, MongoDB enforces the atomic unique index.
4. Catch block catches `DataIntegrityViolationException` / `DuplicateKeyException`, queries `findByIdempotencyKey(...)`, and returns the authoritative persisted record without dispatching duplicate external communications.

---

## 10. Idempotency Behavior

Composite key format:
```
{flightId}:{eventId}:{userId}:{notificationType}:{channel}
```
- **Deterministic**: The same event + user + flight + type + channel strictly hashes to the exact same key.
- **Multi-channel isolation**: `EMAIL` and `PUSH` notifications for the same flight disruption generate distinct keys (`...:EMAIL` vs `...:PUSH`), preserving separate delivery channels while ensuring zero duplicates within each channel.

---

## 11. Tests Added

1. `NotificationDuplicateDiagnosticRunnerTest.java`: Safe diagnostic runner for duplicate detection.
2. `NotificationIdempotencyAndIndexIntegrationTest.java`:
   - `testUniqueIndexExists()`
   - `testDirectDuplicateInsertionFails()`
   - `testConcurrentNotificationCreation()` (10 concurrent threads)
   - `testSequentialIdempotency()`
   - `testDifferentEventIds()`
   - `testDifferentUsersSameEvent()`
   - `testDifferentChannelsGenerateDistinctRecords()`
   - `testSelfHealingDeduplication()`
   - `testRepeatedStartupIsIdempotent()`

---

## 12. Full Test Result

- **Total Backend Tests**: **486 / 486 PASSED**
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 0
- **Execution Time**: 3m 32s

---

## 13. Frontend Build Result

- **Vite Production Build**: **PASSED**
- **Modules Transformed**: 1,769
- **TypeScript Errors**: 0
- **Vite Errors**: 0
- **Initial Bundle Entry**: 49.65 kB (12.45 kB gzip)

---

## 14. Production Safety Considerations

- **Self-Healing Startup**: `NotificationIndexInitializer` runs with `@Order(Ordered.HIGHEST_PRECEDENCE)`. On subsequent startups, `isUniqueIdempotencyIndexValid()` returns `true` and skips aggregation entirely (0ms overhead).
- **Data Protection**: Only verified duplicate copies within the `notifications` collection were cleaned up. No booking, ticket, payment, refund, or flight records were modified or touched.
- **Render Deployment Readiness**: Production startup is 100% immune to index collision crashes.
