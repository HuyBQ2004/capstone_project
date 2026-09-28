# Unit Test Cases — MF08 Food Sample Retention
## Scope: `RetentionPolicy`, `FoodSampleMapper` derived fields, and `FoodSampleService.dispose()` (BR-111, BR-112)

> Source module: `com.sfms.mf08.domain.RetentionPolicy`, `com.sfms.mf08.mapper`, `com.sfms.mf08.service.FoodSampleService`
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-008`…`MF08-TC-012`
> ⚠️ BR-111 was amended on 2026-07-26: the minimum retention period is **24 hours**, not the legacy 72 hours still referenced by the mock frontend and some UC text. All test oracles below use 24h.

---

## Function: `RetentionPolicy` — Retention deadline is server-computed at 24h

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-008` | | Function Name | `retentionDeadline` snapshot = `collectedAt + 24h`, server-side, ignores client input |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-111 (QĐ 1246/QĐ-BYT Điều 8.2) requires a 24-hour countdown from registration. The server must snapshot `retentionDeadline = collectedAt + 24h` regardless of what the client sends, including the mock frontend's legacy free-text `"72 giờ kể từ..."` payload. Source: `MF08-TC-008`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 1 | 0 | 1 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | Clock fixed at `2026-07-07T01:00:00Z`; sample registered with `collectedAt = 2026-07-06T03:15:00Z` | O |
| Request body | Client sends stale/garbage fields: `retentionDeadline="72 giờ kể từ..."`, `hoursRemaining=72` | O |
| **Confirm** | | |
| Return | `retentionDeadline == collectedAt + 24h` (`2026-07-07T03:15:00Z`), ISO-8601, client-supplied string fully ignored | O |
| | `hoursRemaining` in response = `2` (rounded down from 2h15 remaining at fixed clock) | O |
| | No `72`-hour constant found anywhere in the retention calculation source | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | |
| Executed Date | |
| Defect ID | |

**Note:** Keep the constant in exactly one place (`RetentionPolicy.MIN_RETENTION_HOURS = 24`). `retentionDeadline` is still persisted (a snapshot of the rule in effect at collection time — ADR-M08-3), whereas `hoursRemaining` is a derived, non-persisted field (see next function). Accepting the client's free-text deadline would silently corrupt every downstream comparison (disposal gate, deadline reminders).

---

## Function: `FoodSampleMapper.toResponse()` — `hoursRemaining` is computed at read time

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-009` | | Function Name | `hoursRemaining` is a runtime-derived field, never stored |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | Per ADR-M08-3, derived fields such as `hoursRemaining` must never be stored as a column — they are computed from `retentionDeadline` and the current clock each time the entity is mapped to a response. Source: `MF08-TC-009`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 3 | 2 | 0 | 1 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Same DB entity (`retentionDeadline = 2026-07-07T03:15Z`), mapped 3 times with different injected clocks | O | O | O |
| `Clock` at mapping time | `2026-07-06T03:20Z` (5 min after collection) | O | | |
| | `2026-07-07T01:00Z` (fixed clock, 2h15 remaining) | | O | |
| | `2026-07-07T05:00Z` (past deadline) | | | O |
| **Confirm** | | | | |
| Return | `hoursRemaining = 23` (rounded down) | O | | |
| | `hoursRemaining = 2` | | O | |
| | `hoursRemaining = 0`, never negative | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | B |
| Passed/Failed | | | |
| Executed Date | | | |
| Defect ID | | | |

**Note:** All three values come from the same DB row — proving `hoursRemaining` cannot be a stored column. `information_schema.columns` must have no `hours_remaining` column (cross-checked at integration level in `MF08-TC-INT-012`). A stored/stale value would make the retention log show "24h left" for a sample collected days ago, exactly the defect present in the current mock seed data.

---

## Function: `FoodSampleService.dispose()` — Cannot dispose before minimum retention elapses (BR-111)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-010` | | Function Name | Disposal blocked before the 24-hour retention deadline |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-111 / QĐ 1246 Điều 8.2 forbids disposal before the 24-hour minimum retention elapses. Source: `MF08-TC-010`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 3 | 1 | 0 | 2 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `retentionDeadline = 2026-07-07T03:15:00Z`, no linked incident | O | O | O |
| Current clock | `03:14:59Z` (1 second before deadline) | O | | |
| | `03:15:00Z` (exactly at deadline) | | O | |
| | `03:15:01Z` (1 second after deadline) | | | O |
| **Confirm** | | | | |
| Return | Sample transitions to `DISPOSED` | | O | O |
| Exception | `BusinessException` code `SMP-111`, HTTP 409, sample stays `RETAINED` | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | B | B | N |
| Passed/Failed | | | |
| Executed Date | | | |
| Defect ID | | | |

**Note:** The comparison must be `!now.isBefore(retentionDeadline)` (inclusive of the exact boundary), matching the `ck_smp_dispose_after_deadline` CHECK constraint which uses `>=`. If the service used `isAfter` while the DB used `>=`, the boundary second would behave inconsistently between layers. Allowing early disposal would delete evidence before food-poisoning symptoms typically manifest — defeating the purpose of retention.

---

## Function: `FoodSampleService.dispose()` — Blocked while linked to an open incident (BR-112)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-011` | | Function Name | Mandatory exception: disposal blocked while an active incident is linked |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-112 requires the system to block disposal — even after the 24h minimum has elapsed — while a food safety incident/suspected poisoning is linked to the batch, until that incident is closed. Source: `MF08-TC-011`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Retention deadline elapsed? | Yes | O | O | O | |
| | No | | | | O |
| Linked incident | `INC-2026-014` OPEN | O | | | O |
| | `INC-2026-012` CLOSED | | O | | |
| | none | | | O | |
| **Confirm** | | | | | |
| Exception | `SMP-112`, HTTP 409, stays `RETAINED` | O | | | |
| | `SMP-111`, HTTP 409 (deadline check runs first — reports the more immediate reason) | | | | O |
| Return | Disposal succeeds | | O | O | |
| Port call | `incidentPort.hasOpenIncidentLinked()` NOT called (already blocked by the deadline check) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** Disposing a sample tied to an under-investigation incident is unrecoverable — evidence for an active food-safety case would be permanently destroyed. This is flagged in the TDS as a rollback-worthy / DPO-notify condition if it ever occurs in production.

---

## Function: `DisposeSampleRequest` validation — mandatory disposal fields (BR-112)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-012` | | Function Name | Disposal must record time, disposer, witness, and note (Mẫu số 5 columns 9/10/12) |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-112 / UC-030 requires disposal to capture the disposal date/time, method, disposer (signed off from JWT), and a **witness name** in a dedicated column — not concatenated into the note text. Source: `MF08-TC-012`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Sample eligible for disposal (BR-111/BR-112 gates passed) | O | O | O | O |
| Missing field | `disposedAt` | O | | | |
| | `witness` (not currently sent by the mock frontend) | | O | | |
| | `note` | | | O | |
| | none (all fields present) | | | | O |
| **Confirm** | | | | | |
| Exception | `ValidationException` code `SMP-030`, HTTP 422, sample stays `RETAINED` | O | O | O | |
| Return | Saved; `disposal_witness` stored in its **own column**, `disposal_note` does NOT contain a `"— nhân chứng:"` concatenation; `disposedBy` = JWT full name, ignoring any body value | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | A | N |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** Expected 🔴 RED until the frontend adds a dedicated `disposalWitness` field (it currently concatenates the witness name into the free-text note). Losing the witness as queryable, structured data means the audit trail cannot prove who witnessed a disposal — exactly the gap flagged against seed record `AUD-98994`.
