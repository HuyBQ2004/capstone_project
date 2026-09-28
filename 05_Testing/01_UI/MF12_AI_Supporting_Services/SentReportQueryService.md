# Unit Test Case — SentReportQueryService / ReportExporter (MF12 AI & Supporting Services)

> Group (D) — Proactive food-safety reporting. Layer `com.sfms.mf12.report.*`. Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-024`, `MF12-TC-025`.

---

## Function: `SentReportQueryService.detail()` — immutable snapshot

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-025` | | Function Name | `SentReportQueryService.detail` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | A sent report is a snapshot at send time — reading it later must return the SAME numbers even if the underlying source data changes afterwards, and the record can never be edited once sent (ADR-M12-05, BR-177). (source: MF12-TC-024) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 1 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-015` (`RPTS-2026-001`, `avgScore=90`) + `FX-016` (5 source sessions, avg 90) | O | O | O |
| Action | `GET /sent-reports/RPTS-2026-001`, record the payload | O | | |
| | Source data changed after send (1 more session with score 50 added; true avg now ~83), then `GET` again | | O | |
| | `PUT /sent-reports/RPTS-2026-001` attempted | | | O |
| **Confirm** | | | | |
| Return | Initial `payload.avgScore == 90` | O | | |
| | `payload.avgScore` STILL `90` (not recalculated to ~83); `threeStepSummary`/`failedSessions`/`staffHealthValid` also unchanged | | O | |
| Exception | HTTP 405/409 — a sent report is immutable, cannot be edited | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `ReportExporter` — student-identity redaction + confidentiality label

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-026` | | Function Name | `ReportExporter` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 45 | | Lack of test cases | 0 |
| Test requirement | Exported reports (JSON payload, PDF, XLSX) must never expose a student's identity (code/name), and any report containing sensitive content must be flagged `confidential=true` with a visible "Confidential" label on the printed file (BR-181, PDPD Law 91/2025). (source: MF12-TC-025) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 0 | 1 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Source data contains a student (`HS-3A-001`) with an allergy conflict in the session | O | O | O |
| Check target | Response JSON payload | O | | |
| | Extracted text of the generated PDF and XLSX files | | O | |
| | `confidential` flag + printed label | | | O |
| **Confirm** | | | | |
| Return | Payload contains only aggregate figures — no `studentId`, student name, or pattern `HS-\d+[A-Z]-\d+` | O | | |
| | Extracted PDF/XLSX text does not match `HS-\d+[A-Z]-\d+` (`pdftotext ... \| grep -E "HS-[0-9]+[A-Z]-[0-9]+"` → no output) | | O | |
| | `confidential = true` and the file carries a printed "Confidential" label when sensitive content is present | | | O |
| | Aggregate counts (e.g. "2 students affected") remain correct — anonymization does not drop the figures | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | B |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |
