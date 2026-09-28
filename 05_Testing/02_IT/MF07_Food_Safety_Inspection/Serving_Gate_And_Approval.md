# Integration Test Case — Serving Gate & Approval (MF07 Food Safety Inspection & Monitoring)

---

## Feature: Serving Gate — Sole Checkpoint for Meal Distribution (BR-094)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Serving Gate — Sole Checkpoint for Meal Distribution |
| Test requirement | Verify `GET /api/v1/inspections/serving-gate` is the single source of truth MF09 must query before serving a meal — both the allowed path (session passed) and every blocked path with a distinguishable `reasonCode`. Cross-referenced with `MF07-TC-SEC-015` (architectural check that MF09 has no bypass). |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 2 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **GET /api/v1/inspections/serving-gate — Allowed** | | | | | | | | | | | | | | |
| MF07-TC-INT-002 | Test that the serving gate opens (`allowed:true`) once a session has fully passed and been co-signed. | 1. Run `MF07-TC-INT-001` (full passed + co-signed session).<br>2. `GET /inspections/serving-gate?date=2026-08-05&meal=LUNCH`. | 1. HTTP 200 `{"allowed": true}`, includes `sessionCode` so MF09 can record which gate check it performed.<br>2. Rate limit is 600/min — the highest in the module, since MF09 calls it on every serving action.<br>3. Teacher (`FX-024`) can also call this endpoint (§16: `✅ School`) — teachers are the ones who serve meals. | Completed `MF07-TC-INT-001` session (`overall='PASSED'`, co-signed). | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | BR-094, BR-098 |
| **GET /api/v1/inspections/serving-gate — Blocked (4 Distinguishable Reason Codes)** | | | | | | | | | | | | | | |
| MF07-TC-INT-003 | Test that the serving gate blocks meal distribution for 4 distinct DB states, each with its own `reasonCode`, and always responds HTTP 200 (never an error status) as it is a query, not a failure. | Query `GET /serving-gate` for 4 seeded DB states:<br>(a) session `overall='FAILED'` (`FX-002`).<br>(b) session `overall='IN_PROGRESS'` (`FX-003`).<br>(c) no session exists for (date, meal).<br>(d) all 3 steps PASSED but `co_signed_at IS NULL`. | 1. (a) → `{allowed:false, reasonCode:"INSPECTION_FAILED"}`.<br>2. (b) → `{allowed:false, reasonCode:"INSPECTION_INCOMPLETE"}`.<br>3. (c) → `{allowed:false, reasonCode:"NO_SESSION"}`.<br>4. (d) → `{allowed:false, reasonCode:"NOT_CO_SIGNED"}`.<br>5. HTTP 200 in all 4 cases (gate is a query; `allowed:false` is returned in the body, not as an HTTP error).<br>6. A low kitchen-hygiene score never appears in any `reasonCode` (C6 — cross-ref `MF07-TC-INT-009` case b). | Seed data for the 4 DB states above. | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | BR-094; TDS §17.1 C4; cross-ref MF07-TC-SEC-015 |
