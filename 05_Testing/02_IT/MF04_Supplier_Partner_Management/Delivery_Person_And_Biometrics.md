# Integration Test Case — Delivery Person & Biometrics End-to-End (MF04 Supplier & Partner Management)

---

## Feature: Face-Data Deletion (UC-028) and National-ID Race Condition (BR-050)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Face-Data Deletion (UC-028) and National-ID Race Condition (BR-050) |
| Test requirement | Verify that `DELETE /delivery-persons/{code}/face` is implemented end-to-end even though the current mock UI has no button for it (`CF-M04-7`) — deletion revokes gate access, is audited, and does not 500 on a second call — and that two concurrent registrations with the same national ID cannot both succeed: the database's unique constraint, not just the service-level check, must be the final backstop (BR-050). |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 1 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Delete Face Data — End to End (UC-028)** | | | | | | | | | | | | | | |
| MF04-TC-INT-011b | Test the full `DELETE /delivery-persons/{code}/face` flow: DB state, audit trail, constraint integrity, and repeat-call behavior. | 1. `DP-01` is `REGISTERED`.<br>2. `DELETE /api/v1/delivery-persons/DP-01/face`.<br>3. Query `delivery_persons` and `audit_logs`.<br>4. Repeat the same `DELETE` call. | 1. Baseline confirmed.<br>2. HTTP 200, MSG-029.<br>3. `biometric_status=NOT_REGISTERED`, `face_id=NULL`, `biometric_registered_at=NULL`; `audit_logs` has an entry with actor + timestamp + the delivery person's code; `ck_dp_face` constraint still holds after the write.<br>4. HTTP 409/404 (nothing left to delete) — **not** a 500. | Testcontainers PostgreSQL 16, Flyway migrated; `DP-01` `REGISTERED` with `faceId="mock-uuid"`. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | UC-028 Postconditions, MSG-029, BR-051; `CF-M04-7` — the mock UI has no delete button yet, but BR-051 ("data may be deleted by the supplier") requires the endpoint to exist regardless |
| **CCCD Uniqueness Under Concurrency (Race Condition)** | | | | | | | | | | | | | | |
| MF04-TC-INT-012 | Test that two simultaneous `POST /delivery-persons` requests with the same national ID cannot both succeed, and that the losing request receives a proper business error, not a raw 500. | 1. Two threads coordinated by a `CountDownLatch`, both `POST /api/v1/delivery-persons` with `nationalId="001077712345"`.<br>2. Release both simultaneously.<br>3. Inspect both HTTP responses.<br>4. Query the database. | 1–2. Both fired concurrently.<br>3. Exactly one `201`, exactly one `409 SUP-050` — the losing request never surfaces a raw `500 DataIntegrityViolationException`.<br>4. `SELECT COUNT(*) FROM delivery_persons WHERE national_id_hash = ?` == 1. | Testcontainers PostgreSQL 16, `ux_dp_nid_hash` unique index applied. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | BR-050 ("unique in the system" is an invariant, not a "usually unique"); service-side check-then-act alone cannot survive concurrent load — the DB constraint is the real guarantee |
