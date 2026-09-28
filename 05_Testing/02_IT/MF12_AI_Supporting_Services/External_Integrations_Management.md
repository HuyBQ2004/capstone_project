# Integration Test Case — External Integrations Management (MF12 AI & Supporting Services)

---

## Feature: External Integrations Management

### 1. General Information

| Field | Value |
|---|---|
| Feature | External Integrations Management |
| Test requirement | Verify the full UC-040 flow — `test-connection` → `PUT /integrations/{code}` → `GET /system/status` — enforces a single source of truth for connection state (`connected` is derived from `last_test_passed`, never trusted from the client, per C11/L14/CF-10) and that credentials are stored as ciphertext only (BR-163/BR-164). |
| Number of TCs | 1 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 1 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 1 | 0 |
| Round 3 | 0 | 0 | 1 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/integrations/{code}/test-connection → PUT /api/v1/integrations/{code} → GET /api/v1/system/status** | | | | | | | | | | | | | | |
| MF12-TC-INT-016 | Test the complete UC-040 flow: `test-connection` then `PUT`, and that `GET /system/status` reports the exact same Gemini connection state as `GET /integrations` (single source of truth, L14/CF-10). | 1. `POST /integrations/gemini/test-connection` with `FX-022` (fake OK) → assert.<br>2. `PUT /integrations/gemini` with `apiKey` → assert.<br>3. Inspect DB: cipher vs plaintext via `SELECT count(*) FROM external_integrations WHERE credential_cipher LIKE 'AIza%'`.<br>4. `GET /integrations` → assert `connected`.<br>5. `GET /system/status` → assert the SAME Gemini state as step 4. | 1. Step 1 → HTTP 200, `last_test_passed=true`, `last_tested_at=now`.<br>2. Step 2 → HTTP 200, `connected: true` — derived from `last_test_passed`, never from the client body (C11).<br>3. `credential_cipher` is ciphertext; the plaintext-prefix query returns 0 rows.<br>4. `GET /integrations` → `credentialMasked` present, `apiKey` never present (cross-ref `MF12-TC-SEC-006`/`MF12-TC-009`).<br>5. `GET /system/status` reads the SAME table — Gemini status matches `GET /integrations` 100%, no independently-derived status (L14/CF-10).<br>6. §11.3 smoke-test step 7 is green. | External integration seed `FX-INT-001` for `gemini` (`connected=false`, `last_test_passed=false`). | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | CRITICAL; BR-163, BR-164; L14/CF-10 — reproduces the historical bug where "External Integrations & AI" showed "Not connected" while "System Status" showed "running" |
