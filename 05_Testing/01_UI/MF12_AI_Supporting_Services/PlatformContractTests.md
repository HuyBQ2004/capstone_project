# Unit Test Case — Platform Mapper / Code Policy Contract Tests (MF12 AI & Supporting Services)

> Cross-cutting contract tests that apply across MF12's resources (`audit_logs`, `error_logs`, `sent_reports`, `security_policies`, `external_integrations`, `checklist_groups`). Test classes: `mapper/DateFormatContractTest.java`, `mapper/ReportPeriodContractTest.java`, `platform/CodePolicyTest.java`. Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-026`, `MF12-TC-027`, `MF12-TC-028`.

---

## Function: Date/time JSON contract — ISO-8601 (`00_Common_Conventions.md` D4)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-027` | | Function Name | Mapper of `audit_logs`, `error_logs`, `sent_reports`, `security_policies`, `external_integrations` — date/time fields |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | All 5 MF12 resources must return date/time fields in ISO-8601 (D4), overriding the older `dd/MM/yyyy` convention in `TDS §17.1 C13`/CF-26 (Approved 2026-07-30, L3). Input parsing must still tolerate the legacy locale format the mock currently sends, since the mock seed itself was not updated this round. (source: MF12-TC-026) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 3 | 0 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Request body | `PUT` with `{"lastTestedAt": "02/07/2026 09:41"}` (legacy locale format) | O | | |
| | `PUT` with an ISO-8601 value | | O | |
| Check target | `GET` across all 5 resources — response date fields | | | O |
| **Confirm** | | | | |
| Return | Parses to the correct `Instant` (Asia/Ho_Chi_Minh for the locale form) | O | | |
| | Parses to the same `Instant` as UTCID01 | | O | |
| | Response is ISO-8601 for `timestamp` (audit/error log), `sentAt`, `date`, `updatedAt`, `lastTestedAt` in ALL 5 resources | | | O |
| | No `@JsonFormat` annotation remains in `com.sfms.mf12.*` (`grep -rn "@JsonFormat" src/main/java/com/sfms/mf12/` → no output); DB columns are `timestamptz`/`date`, never `varchar` | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: this converter must live in a module shared by all 13 modules (`common/`, owned by Nam) — `audit_logs` alone is written by 13 modules, so a local MF12-only fix would leave the rest of the system inconsistent.

---

## Function: `SentReport.period` mapper — case-insensitive parse (`ReportPeriod`, outside D6)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-028` | | Function Name | Mapper of `SentReport.period` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | `period` must parse case-insensitively and respond in the capitalized form the FE reads (`"Weekly"`/`"Daily"`) per `TDS §17.1 C13`/CF-27; `ReportPeriod` is explicitly NOT one of the 8 enums covered by D6's SCREAMING_SNAKE convention, so C13 still governs until Nam adds it to D6. (source: MF12-TC-027) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 2 | 1 | 2 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| `period` value sent | `"Weekly"` (matches `FX-015` seed) | O | | | | |
| | `"weekly"` (lowercase) | | O | | | |
| | `"WEEKLY"` (uppercase) | | | O | | |
| | `"Daily"` | | | | O | |
| | `"Monthly"` (unsupported) | | | | | O |
| **Confirm** | | | | | | |
| Return | Parses OK, DB enum `WEEKLY`; response `"Weekly"` (capitalized, FE-readable form) | O | O | O | | |
| | Parses OK, DB enum `DAILY`; response `"Daily"` | | | | O | |
| Exception | HTTP 422 — only Weekly/Daily supported | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: if Nam later adds `ReportPeriod` to D6, this test must be updated FIRST (to assert `"WEEKLY"` SCREAMING_SNAKE) before the implementation is changed to match — do not let code and test drift.

---

## Function: `create()` for `checklist-groups` and `sent-reports` — client-supplied `id` (D5)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-029` | | Function Name | `create()` — client `id` handling |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | If the request body has an `id`, it is used as `code`; a duplicate must return 409; if absent, the server auto-generates a code, and the response always returns `id` (D5/D1). Concurrent creates without an `id` must never collide. (source: MF12-TC-028) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 1 | 1 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Request | `POST /checklist-groups` with `{"id":"CLG-9"}` | O | | | |
| | Same `id="CLG-9"` sent again | | O | | |
| | `POST` with NO `id` | | | O | |
| | 2 concurrent `POST`s, both with NO `id` | | | | O |
| **Confirm** | | | | | |
| Return | HTTP 201, response `"id": "CLG-9"`; DB `code = "CLG-9"`, `id` (UUID) generated by JPA | O | | | |
| Exception | HTTP 409, `$.error.code == "AIS-409"`; no second record created, no overwrite | | O | | |
| Return | HTTP 201 with a server-generated `code`; response always includes `id` | | | O | |
| | The 2 concurrent requests receive 2 DIFFERENT `code`s (no collision) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | N | B |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: uniqueness scope differs by resource — `UNIQUE(school_id, code)` for `checklist_groups`/`sent_reports`, but `UNIQUE(code)` only for `external_integrations` (L11, no `school_id` column there). This function tests the `checklist_groups`/`sent_reports` scope; `external_integrations` uniqueness is covered by `AIS-FUNC-006`'s fixture (`FX-007`, `code="gemini"`).
