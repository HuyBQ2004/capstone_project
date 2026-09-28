# Integration Test Case — End-to-End Scenarios (MF07 Food Safety Inspection & Monitoring)

---

## Feature: End-to-End Cross-Module Scenarios

### 1. General Information

| Field | Value |
|---|---|
| Feature | End-to-End Cross-Module Scenarios |
| Test requirement | Verify the module's full backbone scenario (inspection outcome gates meal serving via ID-linked references), the complete failed-step-to-locked-violation-record chain feeding MF11, and the FE↔BE contract snapshot that guarantees "change the `baseURL` and it just works" across all 6 MF07 resources. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Backbone Scenario: Inspection Outcome Gates Meal Serving** | | | | | | | | | | | | | | |
| MF07-TC-E2E-001 | Test the system backbone: a caterer's passed 3-step inspection allows a teacher to serve the meal, while a failed inspection for the same day blocks serving for a different meal slot — all links verified by ID, never by name/label. | 1. Login Caterer (`FX-020`) → `POST /inspections` for lunch on day D, grade 3.<br>2. Submit all 3 steps, `coreTemp: 82` → co-sign → `overall=PASSED`.<br>3. Login Teacher (`FX-024`) → `GET /inspections/serving-gate?date=D&meal=LUNCH` → serve.<br>4. **Negative branch:** for the SNACK meal same day D, submit Step 2 with `coreTemp: 68` → `overall=FAILED`.<br>5. Teacher → `serving-gate?date=D&meal=SNACK` → attempt to serve.<br>6. Assert all cross-module links use ID/code, not display name. | 1. (3) → `allowed:true`; serving succeeds.<br>2. (5) → `allowed:false`, `reasonCode="INSPECTION_FAILED"`; serving API is blocked; no `student_distributions` row is `status='SERVED'` for the SNACK meal on day D.<br>3. (6) → every link uses code/ID: `inspection_sessions.menu_id → menus.code`, `.partner_id → partners.code`, `.dish_id → dishes.code`; no join by dish name or Vietnamese meal label anywhere.<br>4. Running the semantic-audit check afterward reports 0 violations of the `PHAT_SUAT_DU_KIEM_THUC_TRUOT` rule. | Clean DB + seeded MF01 (3 users), MF02 (`SCH-001`, class 3A), MF04 (`SUP-001`), MF05 (`MENU-2026-30` PUBLISHED with `dishId`), MF07 (`FX-012` checklist). | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | CRITICAL; BR-094, BR-098; the single most severe failure mode of the whole system if this gate is bypassed |
| **From Failed Step to Locked Violation Record** | | | | | | | | | | | | | | |
| MF07-TC-E2E-002 | Test the complete chain from a failed inspection step, through automatic CAPA creation, to a Medical Staff violation record that is exported as PDF, signed, and locked — stopping at PENDING_APPROVAL (approval itself belongs to MF11). | 1. Caterer submits Step 2 `coreTemp: 68` → `step2=FAILED`.<br>2. Assert CAPA auto-created (no CAPA API called).<br>3. Login Medical Staff → `POST /violations` with `sourceInspectionId` = session code, `caterersRepresentativeId`, description + photo.<br>4. `POST /violations/{code}/export-pdf`.<br>5. `POST /violations/{code}/signed-record`.<br>6. `PUT {"resolution":"PENDING_APPROVAL"}`.<br>7. `GET /inspections/{code}` → assert `violations[]`.<br>8. Attempt to edit the locked record's content. | 1. (2) → exactly 1 CAPA row with `source_inspection_code` set.<br>2. (3) → HTTP 201, MSG-086; no deprecated personal-identity field present (L11).<br>3. (4) → valid server-side PDF matching the `specialreport2173_1.md` template.<br>4. (5) → `locked:true`, `sha256` present.<br>5. (6) → HTTP 200 (signed photo already present; without step 5 this would be HTTP 409 `VIO-095`).<br>6. (7) → the session response includes this violation record (reverse traceability, BR-183).<br>7. (8) → HTTP 409 (append-only); `audit_logs` records the rejected edit attempt.<br>8. Boundary note: the subsequent approval step (`resolution → APPROVED` by School Admin, BR-186) belongs to MF11 UC-182 and is intentionally NOT tested here — this scenario stops at `PENDING_APPROVAL` (L10/CF-6). | Clean caterer + medical staff sessions; inspection session in progress. | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | HIGH; BR-095, BR-183, BR-211; MSG-086, MSG-095 |
| **FE↔BE Contract Snapshot** | | | | | | | | | | | | | | |
| MF07-TC-E2E-003 | Test that the response key shape of all 6 MF07 resources matches the FE mock server's contract exactly, so switching `baseURL` from mock to real backend requires no FE code change. | 1. Call `GET` for `inspections`, `inspections/{code}`, `kitchen-inspections`, `checklist-groups`, `violations`, `safety-signoffs`, `compliance-report`.<br>2. Recursively extract the key set of each response.<br>3. Compare against the locked key snapshot exported from `mock-server.ts` (`src/test/resources/contracts/mf07-mock-keys.json`).<br>4. Verify value formats per D4/D6. | 1. Backend key set is a superset of (or equal to) the mock key set — no missing keys.<br>2. All 17 contracted field names present (C12): `meal`, `step1`, `step2`, `step3`, `overall`, `coreTemp`, `dish`, `evidencePhotos`, `staffNotes`, `formHeader`, `step1Fresh`, `step1Dry`, `step2Rows`, `step3Rows`, `itemFindings`, `itemResults`, `confirmStatus`, `signedRecordFiles`.<br>3. `overall` accepts `IN_PROGRESS` in addition to the base states (CF-23).<br>4. Pagination responses have exactly the 5 contracted keys, no wrapper.<br>5. `date` matches `^\d{4}-\d{2}-\d{2}$`; `occurredAt`/`signedAt`/`pdfGeneratedAt` are ISO instants, never `dd/MM/yyyy` (D4).<br>6. `meal` ∈ `{BREAKFAST, LUNCH, SNACK}` (D6).<br>7. Every path is under `/api/v1` (D3). | Backend running with full MF07 seed data across all 6 resources. | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | HIGH; contract snapshot compares KEYS only, not values; format checks in step 4 are mandatory since D4/D6 change value format without changing keys |
