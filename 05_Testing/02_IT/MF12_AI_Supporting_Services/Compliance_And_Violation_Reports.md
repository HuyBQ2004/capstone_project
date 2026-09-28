# Integration Test Case — Compliance & Violation Report Sending (MF12 AI & Supporting Services)

---

## Feature: Compliance & Violation Report Sending (UC-185 / UC-188 / UC-189 / UC-190)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Compliance & Violation Report Sending |
| Test requirement | Verify `POST /api/v1/sent-reports/compliance` persists a `sent_reports` row and publishes a `ComplianceReportSent` event to MF10 (BR-209, MSG-091), `GET /sent-reports/{code}/download` produces PDF+XLSX files with all 4 BR-178 components, `GET /report-center` only surfaces the 2 currently-active report types (BR-213), and `sent_reports`' single-table JSONB shape (`ADR-M12-05`) preserves the mock's field union — including legacy fields on historical rows. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/sent-reports/compliance** | | | | | | | | | | | | | | |
| MF12-TC-INT-012 | Test that sending a compliance report persists a `sent_reports` row with server-recomputed data and publishes exactly one `ComplianceReportSent` event to MF10, without MF12 calling `/notifications` directly. | 1. `POST /sent-reports/compliance` for a period with data.<br>2. Inspect `sent_reports` + payload.<br>3. Inspect the published event.<br>4. Inspect generated files. | 1. HTTP 200, message MSG-091.<br>2. 1 `sent_reports` row: `report_type='COMPLIANCE'`, `school_id='SCH-001'` (from JWT — C12), `sent_by` from JWT (never trusted from client — cross-ref `MF12-TC-SEC-016` case g).<br>3. `payload.avgScore == 90`, recomputed from `FX-016`, matching `FX-015`.<br>4. Event `ComplianceReportSent` published exactly once with `recipient='School Administration'`.<br>5. `filePdfUrl` + `fileXlsxUrl` both non-null (BR-178 — cross-ref `MF12-TC-INT-015`).<br>6. MF12 never calls `POST /notifications` directly — notification dispatch is MF10's job via the event. | `FX-016` (5 sessions, avg 90); `FX-030` Medical Staff JWT for `SCH-001`. | Pending | | | Pending | | | Pending | | | HIGH; BR-209, MSG-091, BR-158/159; boundary L28/CF-29 — retry/quiet-hours logic tested in MF10, not here |
| **GET /api/v1/sent-reports/{code}/download?format=pdf\|xlsx** | | | | | | | | | | | | | | |
| MF12-TC-INT-015 | Test that downloaded PDF and XLSX report files both contain all 4 required BR-178 components, the Confidential label when applicable, and no student codes; unsupported formats are rejected. | 1. Send a report (cross-ref `MF12-TC-INT-012`) → `GET .../download?format=pdf`.<br>2. `?format=xlsx`.<br>3. `?format=csv` (unsupported).<br>4. Extract content → check the 4 components.<br>5. Inspect the audit row for the download. | 1. Step 1 → HTTP 200, `Content-Type: application/pdf`, body starts with `%PDF-`.<br>2. Step 2 → HTTP 200, `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, body starts with `PK` (zip).<br>3. Step 3 → HTTP 400 (only 2 formats per BR-178).<br>4. Step 4 → BOTH files contain: report title, data period range (`rangeLabel` = "Monday 06/07/2026 – Sunday 12/07/2026"), school name, export timestamp.<br>5. Step 4 → "Confidential" label present when `confidential=true` (BR-181).<br>6. Step 4 → no student code present (cross-ref `MF12-TC-SEC-008`).<br>7. Step 5 → 1 audit row for the download (BR-177).<br>8. Non-authorized role → HTTP 403 (§16 row `/download`). | A sent report exists (from `MF12-TC-INT-012`). | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | HIGH; BR-178, BR-181; new endpoint L25/CF-23 |
| **GET /api/v1/report-center** | | | | | | | | | | | | | | |
| MF12-TC-INT-019 | Test that the report-center hub only lists `COMPLIANCE` and `VIOLATION_SUMMARY` as sendable report types (BR-213), while deprecated `INCIDENT_SUMMARY` remains visible in history only. | 1. `GET /report-center` with `FX-030` → inspect the list of report types.<br>2. Non-Medical → assert 403. | 1. Returns exactly 2 sendable types: `COMPLIANCE` (UC-185) and `VIOLATION_SUMMARY` (UC-189).<br>2. `INCIDENT_SUMMARY` is NOT in the "sendable" list (deprecated — cross-ref `MF12-TC-023`).<br>3. The history section DOES include old `INCIDENT_SUMMARY` reports (reading history still works — BR-177).<br>4. Non-Medical → HTTP 403 (§16: `GET /report-center` is Medical Staff only, including System Admin being denied). | Historical `sent_reports` including at least one `INCIDENT_SUMMARY` row. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | MEDIUM; BR-213, BR-175; new endpoint §9.2 #28; L8/CF-4 |
| **sent_reports — JSONB Field-Union Shape (ADR-M12-05)** | | | | | | | | | | | | | | |
| MF12-TC-INT-020 | Test that `sent_reports`' single-table JSONB response preserves the mock's field union at the JSON root (not nested under `payload`), and that historical rows with removed fields (e.g. `latestPestControl`) still read correctly. | 1. Seed `FX-015` (`COMPLIANCE` with `avgScore`, `threeStepSummary`, `staffHealthValid`) + 1 `VIOLATION_SUMMARY` (with `counts`) + 1 historical `INCIDENT_SUMMARY` (with the removed field `latestPestControl`).<br>2. `GET /sent-reports?reportType=COMPLIANCE` → inspect shape.<br>3. `GET /sent-reports` (all) → inspect sort.<br>4. `GET /sent-reports/{code of old INCIDENT_SUMMARY}` → inspect the legacy field.<br>5. Send a NEW report → confirm `latestPestControl` is absent. | 1. Step 2 → the `COMPLIANCE` item has `avgScore`, `threeStepSummary`, `failedSessions`, `staffHealthValid` at the JSON ROOT — not wrapped in `payload` (preserves the mock's field union, `ADR-M12-05`).<br>2. Step 3 → sorted `sentAt DESC`; all 3 types share one list, each with its own fields.<br>3. Step 4 → the old record STILL returns `latestPestControl` (JSONB is tolerant — L26/CF-12).<br>4. Step 5 → new reports do NOT generate `latestPestControl` (removed per BR-209's 27/07 revision).<br>5. `counts.partnerCount` (a field outside the declared interface) is tolerated, does not cause a failure (L26/CF-28). | Seeded rows across all 3 report types, including one legacy row. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | HIGH; ADR-M12-05; L15/CF-11, L26/CF-28+CF-12 — wrapping fields in `payload` would break 16 FE files |
