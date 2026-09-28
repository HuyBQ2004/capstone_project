# Unit Test Case — IncidentInvestigationService (MF11 Incident & Emergency Management)

---

## Function: `assessAndStart` — role guard

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IIS-FUNC-001` | | Function Name | `IncidentInvestigationService.assessAndStart()` (role guard) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `95` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-147 verbatim: *"Medical Staff is responsible for classifying the Severity of the incident."* Decision Table (7 roles × 1 endpoint, `POST /incidents/{code}/assess`) per TDS §16. Non-permitted roles → `IEM-005`/MSG-005/HTTP 403, **not** 404 (`school-admin` can view the incident but not classify it). Positive branch fires event `IncidentSeverityAssigned` and returns MSG-159 (TDS §10.2, supersedes the earlier MSG-029 assignment). Covers `MF11-TC-007`. Condition Ref `COND-06` · Constraint Ref `C14` · `AP-M11-05`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `1` | `3` | `0` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Incident exists, `severity` currently unset (`SEVERITY_PENDING`) | O | O | O | O |
| Actor | JWT — role `school-admin` (can view the incident per TDS §16, but is not permitted to classify it) | O | | | |
| | JWT — role `service-caterers` | | O | | |
| | JWT — role `teacher` / `parent` / `supplier` / `system-admin` (remaining 4 non-permitted roles; all behave identically to UTCID01/UTCID02 per the 7-role × 1-endpoint decision table) | | | O | |
| | JWT — role `medical-staff` | | | | O |
| Input | `assessAndStart(code, {severity:"HIGH"}, school-admin)` | O | | | |
| | `assessAndStart(code, {severity:"HIGH"}, service-caterers)` | | O | | |
| | `assessAndStart(code, {severity:"HIGH"}, <one of teacher/parent/supplier/system-admin>)` | | | O | |
| | `assessAndStart(code, {severity:"HIGH"}, medical-staff)` | | | | O |
| **Confirm** | | | | | |
| Return | `200`, `severity` set, event `IncidentSeverityAssigned` published exactly once, `message` = MSG-159 *"Incident severity assigned successfully. Investigation is now in progress."* | | | | O |
| Exception | `ForbiddenException IEM-005` (HTTP 403), `message` = MSG-005 verbatim *"Access denied: You do not have permission to access this resource."* — **not** 404, even though `school-admin` can see the incident | O | O | O | |
| Log message | `MSG-005`: Access denied: You do not have permission to access this resource. | O | O | O | |
| Side-effect | `severity` in DB unchanged (still `SEVERITY_PENDING`) | O | O | O | |
| | Event `IncidentSeverityAssigned` **not** published | O | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | A | N |
| Passed/Failed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

> Note: `school-admin` (UTCID01) is asserted separately from the other 5 non-permitted roles because BR-147/TDS §16 explicitly calls out that School Admin can *view* the incident record — the test must confirm the guard still returns 403 (not 404) for this role specifically. UTCID03 is a representative case; per the source's decision table (7 roles × 1 endpoint) `teacher`, `parent`, `supplier`, and `system-admin` are asserted to behave identically to `service-caterers`.

---

## Function: `assessAndStart` — severity enum validation

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IIS-FUNC-002` | | Function Name | `IncidentInvestigationService.assessAndStart()` (DTO enum binding) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `40` | | Lack of test cases | `0` |
| Test requirement | BR-147 (3 levels: LOW/MEDIUM/HIGH only) + §5.2 `CHECK (severity IN ('LOW','MEDIUM','HIGH'))` + §10 error table → `IEM-147`/HTTP 400 + D6 (SCREAMING_SNAKE enum convention) + CF-M11-12/L22. DB invariant `ck_inc_severity_set` ensures `severity IS NOT NULL` once the record leaves `SEVERITY_PENDING`. Covers `MF11-TC-008`. Condition Ref `COND-06` · Constraint Ref `C12` / `D6`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `8` | `3` | `5` | `0` | `8` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| Precondition | Incident exists, `severity` currently `SEVERITY_PENDING` | O | O | O | O | O | O | O | O |
| Actor | JWT — role `medical-staff` (guard already passed) | O | O | O | O | O | O | O | O |
| Input | `severity = "CRITICAL"` (not in enum) | O | | | | | | | |
| | `severity = "Cao"` (Vietnamese, not accepted) | | O | | | | | | |
| | `severity = "high"` (lowercase, D6 requires SCREAMING_SNAKE) | | | O | | | | | |
| | `severity = ""` (empty) | | | | O | | | | |
| | `severity = "PENDING"` (display value for NULL, rejected as input) | | | | | O | | | |
| | `severity = "LOW"` | | | | | | O | | |
| | `severity = "MEDIUM"` | | | | | | | O | |
| | `severity = "HIGH"` | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | `400`, `error.code = IEM-147`, `details[0].field = "severity"`, `message` = MSG-150 verbatim *"Incident severity must be one of LOW, MEDIUM or HIGH."* | O | O | O | O | O | | | |
| | `200`, `severity` accepted and persisted as submitted value | | | | | | O | O | O |
| Exception | `ValidationException IEM-147` (HTTP 400) | O | O | O | O | O | | | |
| Log message | `MSG-150`: Incident severity must be one of LOW, MEDIUM or HIGH. | O | O | O | O | O | | | |
| Side-effect | `ck_inc_severity_set` — record stays in `SEVERITY_PENDING`, `severity` remains `NULL` in DB | O | O | O | O | O | | | |
| | `ck_inc_severity_set` satisfied — `severity IS NOT NULL` after leaving `SEVERITY_PENDING` | | | | | | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | A | A | N | N | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | |

> Note (UTCID05): `"PENDING"` is the **display** value shown in the UI when `severity IS NULL`, not a value the API accepts as input — it must be rejected exactly like the other invalid strings, per BR-147/D6.

---

## Function: `assessAndStart` → `EmergencyAlertDispatcher` (outbox)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IIS-FUNC-003` | | Function Name | `IncidentInvestigationService.assessAndStart()` → `EmergencyAlertDispatcher` (outbox) |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `85` | | Lack of test cases | `0` |
| Test requirement | BR-148 verbatim: *"An incident classified as High Severity must immediately trigger an alert to: School Board · Catering Company · Local Health Authority."* Alert dispatch is **asynchronous** via transactional outbox (ADR-M11-9, SLA < 60s, measured at integration level only), must never block or roll back the classification transaction (AP-M11-11). Event `HighSeverityAlertRaised` (§7.1). Covers `MF11-TC-009`. Condition Ref `COND-07` · Constraint Ref `TDS §17.1 C13` · `AP-M11-11`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `1` | `1` | `1` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Incident exists, `severity` currently `SEVERITY_PENDING` | O | O | O |
| | `NotificationPort` behaves normally | O | | |
| | `NotificationPort` throws `RuntimeException` (fixture `FX-019`) | | O | |
| Actor | JWT — role `medical-staff` | O | O | O |
| Input | `assessAndStart(code, {severity:"HIGH"}, medical-staff)` | O | O | |
| | `assessAndStart(code, {severity:"MEDIUM"}, medical-staff)` or `{severity:"LOW"}` | | | O |
| **Confirm** | | | |
| Return | `200`, `severity = HIGH` recorded, response includes `emergencyAlert.dispatched = true` | O | O | |
| | `200`, `severity` recorded (MEDIUM or LOW) | | | O |
| Side-effect | Exactly **1** `HighSeverityAlertRaised` event in the outbox, `recipients = ["SCHOOL_BOARD", "CATERER", "LOCAL_HEALTH_AUTHORITY"]` (all 3 groups per BR-148) | O | | |
| | `verify(mailSender, never())` — MF11 does not call SMTP/SMS synchronously itself | O | | |
| | Classification transaction commits successfully; event remains in outbox for retry — transaction is **not** rolled back despite `NotificationPort` failure | | O | |
| | **0** `HighSeverityAlertRaised` events raised | | | O |
| Log message | — (async dispatch, no synchronous exception surfaced to caller) | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | B |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> Implementation note: SLA < 60s is measured at `TC-INT-005` (integration, transactional outbox), not at unit level — this function only asserts the outbox event/recipients/no-rollback behavior.

---

## Function: `suspectedDishCandidates`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IIS-FUNC-004` | | Function Name | `IncidentInvestigationService.suspectedDishCandidates()` |
| Created By | `Tran Tuan Minh` | | Executed By | `Minh` |
| Lines of code | `60` | | Lack of test cases | `0` |
| Test requirement | BR-149 (amended 2026-07-27): suspected-dish window changed from 72h to **24h**, applied uniformly to both the "suspected dishes" list and the lab-sample list, per QĐ 39/2006/QĐ-BYT. Anchor timestamp fallback order: `occurredAt → mealDate (00:00 VN time) → reportedAt`. Must **not** use the superseded 48h/72h window. Covers `MF11-TC-012`. Condition Ref `COND-09` · Constraint Ref `C5`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `6` | `0` | `1` | `5` | `6` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | `occurredAt = 2026-06-30T04:15Z` → window `[2026-06-29T04:15Z, 2026-06-30T04:15Z]` | O | O | O | | | |
| | `occurredAt` missing, `mealDate` present | | | | O | | |
| | `occurredAt` and `mealDate` both missing, `reportedAt` present | | | | | O | |
| | Dish served 46h before `occurredAt` (well outside 24h window; guards against the superseded 48h/72h window) | | | | | | O |
| Input | Dish served `2026-06-29T04:16Z` (1 minute inside window, lower edge) | O | | | | | |
| | Dish served `2026-06-29T04:14Z` (1 minute outside window, lower edge) | | O | | | | |
| | Dish served `2026-06-29T04:15Z` (exactly at boundary) | | | O | | | |
| | `suspectedDishCandidates(incident)` — anchor resolution when `occurredAt` absent | | | | O | | |
| | `suspectedDishCandidates(incident)` — anchor resolution when both `occurredAt`/`mealDate` absent | | | | | O | |
| | `suspectedDishCandidates(incident)` — dish served 46h before `occurredAt` | | | | | | O |
| **Confirm** | | | | | | |
| Return | Dish **included** in candidates | O | | O | | | |
| | Dish **excluded** from candidates | | O | | | | O |
| | Anchor used = `mealDate` at `00:00` Asia/Ho_Chi_Minh | | | | O | | |
| | Anchor used = `reportedAt` | | | | | O | |
| Log message | — (pure query logic, no exception) | | | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | B | B | B | A |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

> Note (UTCID03): the window's lower boundary (`occurredAt − 24h`, i.e. `2026-06-29T04:15Z`) is **closed** — a dish served exactly at that instant is included, not excluded.
