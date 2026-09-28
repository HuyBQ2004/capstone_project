# Integration Test Case — Violation Record, DB Constraints & Compliance Reporting (MF07 Food Safety Inspection & Monitoring)

---

## Feature: Violation Record Lifecycle, Database Constraints & Compliance Reporting

### 1. General Information

| Field | Value |
|---|---|
| Feature | Violation Record Lifecycle, Database Constraints & Compliance Reporting |
| Test requirement | Verify the violation record lifecycle (create → export PDF → sign → lock, append-only per ADR-M07-05), CHECK constraints enforced at the DB layer as a second line of defense (temperature thresholds, evidence, enum values), the deliberately-not-yet-enabled `ck_insp_holding_temp` constraint, the schema guard against deprecated `fridge_temp_logs`, the 5-slug compliance report shape, and audit log completeness for every write operation. |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Violation Lifecycle: Create → Export PDF → Signed Record → Lock** | | | | | | | | | | | | | | |
| MF07-TC-INT-011 | Test the full violation record lifecycle: create, block premature approval submission, export a server-side PDF, upload the signed record, and enforce append-only lock afterward. | 1. `POST /violations` complying with BR-183 (`caterersRepresentativeId`, description, photo) → assert HTTP 201 + MSG-086.<br>2. `PUT {"resolution":"PENDING_APPROVAL"}` BEFORE a signed photo exists → assert.<br>3. `POST .../export-pdf` → assert PDF.<br>4. `POST .../signed-record` (upload signed photo) → assert.<br>5. `PUT {"description":"changed content"}` after lock → assert.<br>6. Inspect `audit_logs`. | 1. Step 1 → HTTP 201, `resolution='PENDING'`, message MSG-086.<br>2. Step 2 → HTTP 409 `VIO-095` (server-side gate — not just a disabled UI button).<br>3. Step 3 → HTTP 200; real PDF file (`%PDF-` header), `pdf_exported=true`, `pdf_generated_at` set.<br>4. Step 4 → HTTP 200, `locked: true`; `signed_record_photo_url` + `signed_record_sha256` set.<br>5. Step 5 → HTTP 409 (append-only, ADR-M07-05); `description` column unchanged in DB.<br>6. Every post-lock change appends a NEW `audit_logs` row (BR-188), never an in-place update.<br>7. After step 4, `PUT {"resolution":"PENDING_APPROVAL"}` → HTTP 200 (signed photo now present). | Violation record with description, photo, and `caterersRepresentativeId` per BR-183. | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | **Disabled — moved to MF11 (M07-D07), not counted in MF07 exit criteria.** BR-211, MSG-086, MSG-095, ADR-M07-05 |
| **CHECK Constraints — DB-Layer Second Line of Defense** | | | | | | | | | | | | | | |
| MF07-TC-INT-012 | Test that critical CHECK constraints on temperature thresholds, evidence requirements, and enum values are enforced at the DB layer even when the service layer is bypassed via direct JDBC writes. | Direct `INSERT`/`UPDATE` via `JdbcTemplate` (bypassing the service):<br>1. `step2_rows` with `core_temp = 68` and `result='PASSED'`.<br>2. `step1_fresh_rows` with `sensory_pass=false`, `photo_count=0`.<br>3. `checklist_item_results` with `passed=false`, `reason=NULL`.<br>4. `inspection_sessions` with `overall='DONE'` (out-of-enum value).<br>5. A valid row. | 1. Cases 1-4 all raise `DataIntegrityViolationException`; the constraint name appears in the message.<br>2. `overall` CHECK accepts exactly 4 values: `{PENDING, PASSED, FAILED, IN_PROGRESS}` (CF-23 — `IN_PROGRESS` added); the 3 `step*` columns accept exactly 3 values.<br>3. Case 5 inserts successfully.<br>4. Constraints `ck_s1f_evidence`, `ck_cir_evidence`, `ck_insp_overall` all physically exist in `pg_constraint`. | Testcontainers PostgreSQL with `V12__mf07_inspection.sql` applied. | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | BR-090, BR-091, BR-083; TDS §17.1 C2/C5 |
| **CHECK Constraint `ck_insp_holding_temp` — Deliberately Not Yet Enabled** | | | | | | | | | | | | | | |
| MF07-TC-INT-013 | Test that `ck_insp_holding_temp` is intentionally not enabled yet, so existing sessions with `holding_temp = NULL` are not blocked, since the FE has no input field for it (L19/CF-24). | 1. Query `pg_constraint` for `ck_insp_holding_temp`.<br>2. `INSERT` a session with `holding_temp = NULL` (current FE state). | 1. Constraint either does not exist yet or exists as `NOT VALID`; the choice must be documented consistently.<br>2. Insert succeeds (not blocked).<br>3. A comment/TODO in `V12` references CF-24 explaining why it is not enabled. | `V12` migration applied. | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | L19/CF-24 — protects against premature implementation; enable via a NEW migration (V15+) once FE adds the input field |
| **Schema Guard — `fridge_temp_logs` Must Not Exist** | | | | | | | | | | | | | | |
| MF07-TC-INT-015 | Test that the deprecated `fridge_temp_logs` and `pest_control_logs` tables are never created, while `pest_control_valid` remains on `kitchen_inspections`. | Query `information_schema.tables` for `fridge_temp_logs` and `pest_control_logs`. | 1. 0 rows returned for both table names.<br>2. Column `pest_control_valid` still exists on `kitchen_inspections` (still read by the School Admin screen — L9). | `V12` migration applied. | Passed | 2026-08-05 | Thao | Pending | | | Pending | | | L8/CF-4 (BR-089 deprecated), L9/CF-5 — regression guard |
| **GET /api/v1/compliance-report** | | | | | | | | | | | | | | |
| MF07-TC-INT-017 | Test that the compliance report exposes exactly the 5 contracted slugs with correct data sourcing, and is inaccessible to Service Caterers. | `GET /compliance-report` using `FX-022` (Medical) and `FX-023` (School Admin); extract the sorted `slug` list. | 1. Exactly 5 slugs, sorted: `["hygiene","receiving","sample-retention","supplier-certificates","three-step"]`.<br>2. `hygiene` score is sourced from `kitchen_inspections.score` (BR-104) — matches `FX-009`/`FX-010`.<br>3. `three-step` reflects the pass ratio of sessions with `overall='PASSED'`.<br>4. Caterer role → HTTP 403 (§16: `GET /compliance-report` = ❌ for SERVICE CATERERS). | Kitchen inspection and session seed data available. | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | BR-104; UC-115/113/117 |
| **Audit Log Completeness (BR-170/BR-188)** | | | | | | | | | | | | | | |
| MF07-TC-INT-018 | Test that every sensitive write operation across the inspection and violation lifecycle produces an audit log entry with actor and IP address, appended (never updated) after lock. | Run `MF07-TC-INT-001` (open session + 3 steps + co-sign) then `MF07-TC-INT-011` (violation lifecycle) → query `audit_logs`. | 1. At least 5 rows for the session chain: `CREATE`, `SUBMIT_STEP` ×3, `CO_SIGN`.<br>2. Every row has non-null `actor`, non-null `ip_address`, and monotonically increasing `occurred_at`.<br>3. The `actor` of `CO_SIGN` is the co-signer, not the session creator.<br>4. The violation chain additionally logs `EXPORT_PDF`, `UPLOAD_SIGNED_RECORD`, `LOCK`.<br>5. After lock, further changes append a NEW row rather than updating an existing one (append-only). | `MF07-TC-INT-001` and `MF07-TC-INT-011` executed beforehand. | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | TDS §14.2 (BR-170/BR-188 — oracle only, not MF07 BR of record) |
