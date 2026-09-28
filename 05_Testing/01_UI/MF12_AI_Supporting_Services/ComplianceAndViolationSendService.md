# Unit Test Case — ComplianceReportSendService / ViolationReportSendService (MF12 AI & Supporting Services)

> Group (D) — Proactive food-safety reporting. Layer `com.sfms.mf12.report.*`. Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-021`, `MF12-TC-022`, `MF12-TC-023`.

---

## Function: `ComplianceReportSendService.send()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-022` | | Function Name | `ComplianceReportSendService.send` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Sending a compliance report for a period with NO kitchen-inspection sessions must be blocked server-side with `AIS-092` (MSG-092), HTTP 422 - the current FE only disables the button client-side, so a direct API call can bypass it (BR-209, L22/CF-24). A period WITH data must succeed with MSG-091 and produce both PDF and XLSX. (source: MF12-TC-021) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 1 | 1 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `FX-017` - period has NO kitchen-inspection sessions | O | |
| | `FX-016` - 5 sessions in period, avg score 90 | | O |
| Request | `POST /sent-reports/compliance` (direct API call, bypassing the disabled FE button) | O | O |
| **Confirm** | | | |
| Exception | HTTP 422, `$.error.code == "AIS-092"`, message = MSG-092 verbatim ("No kitchen inspection data available for the selected period. Cannot generate compliance report.") | O | |
| Return | `sent_reports` row count NOT increased; no PDF/XLSX generated | O | |
| | HTTP 200, message = MSG-091; `payload.avgScore == 90`; both `filePdfUrl` and `fileXlsxUrl` present (BR-178) | | O |
| | No notification fired when blocked (School Admin is not notified about a report that does not exist) | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

---

## Function: `ViolationReportSendService.send()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-023` | | Function Name | `ViolationReportSendService.send` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | A period with NO violation records must be blocked with `AIS-097` (MSG-097); a period with data must succeed with MSG-099, with `counts.staffCount` always `0` (BR-184 deprecated - must not regenerate personal data), and must never duplicate signed-violation-record content (BR-211/BR-214, L26/CF-28). (source: MF12-TC-022) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 1 | 1 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-018` - period has NO violation records | O | | |
| | `FX-019` - 3 records, `bySeverity={LOW:1,MEDIUM:2}` | | O | O |
| Request | `POST /sent-reports/violations` | O | O | O |
| **Confirm** | | | | |
| Exception | HTTP 422, `$.error.code == "AIS-097"`, message = MSG-097 verbatim; no row created | O | | |
| Return | HTTP 200, message = MSG-099 verbatim; `counts.processCount == 3`, `counts.bySeverity == {LOW:1, MEDIUM:2}` | | O | |
| | `counts.staffCount == 0` - BR-184 is Deprecated, must not regenerate personal data | | O | |
| | Payload contains ONLY rollup counts + navigation link - no `description`/photos/signed-record content duplicated | | | O |
| | Unlisted field `counts.partnerCount` is tolerated in the JSONB payload without failing the request (L26) | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | B |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: Deprecated `INCIDENT_SUMMARY` report guard (both send paths)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-024` | | Function Name | `POST /sent-reports/incidents` + `POST /sent-reports {reportType:'INCIDENT_SUMMARY'}` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | UC-188/BR-213 was deprecated on 2026-07-26 - creating a NEW `INCIDENT_SUMMARY` report must be blocked with `AIS-188` (HTTP 409) on BOTH the new dedicated endpoint AND the legacy generic `POST /sent-reports` path the FE dialog still uses (L8/CF-4, L24/CF-22); reading history of old incident reports must keep working (BR-177). (source: MF12-TC-023) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Request | `POST /sent-reports/incidents` (new dedicated endpoint) | O | | | |
| | `POST /sent-reports` with `{"reportType":"INCIDENT_SUMMARY", ...}` (legacy path still used by the FE dialog) | | O | | |
| | `GET /sent-reports?reportType=INCIDENT_SUMMARY` (read history) | | | O | |
| | `GET /sent-reports/{code}` for a pre-2026-07-26 incident report | | | | O |
| **Confirm** | | | | | |
| Exception | HTTP 409, `$.error.code == "AIS-188"`; no new row created (D7/L4: assert code only) | O | O | | |
| Return | HTTP 200, returns the OLD `INCIDENT_SUMMARY` report(s) - reading history must keep working per C7 | | | O | |
| | HTTP 200, all fields of the old record returned, including deprecated ones (L26) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | N | N |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note (UTCID02): this is the decisive case - blocking only the new dedicated endpoint leaves the deprecation with no real effect, since the FE's incident dialog still posts through the generic `/sent-reports` path. Block at the service layer so both controllers pass through the same gate.
