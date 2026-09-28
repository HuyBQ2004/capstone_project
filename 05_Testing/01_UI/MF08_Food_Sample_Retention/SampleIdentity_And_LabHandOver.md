# Unit Test Cases — MF08 Food Sample Retention
## Scope: Actor identity from JWT (BR-114), lab hand-over evidence (BR-212), retrieval eligibility (BR-113), and the sample status FSM

> Source module: `com.sfms.mf08.service` (`FoodSampleService`, `SampleLabRequestService`), `com.sfms.mf08.domain.FoodSampleStatus`
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-013`…`MF08-TC-016`

---

## Function: Actor identity — `register()` / `dispose()` / `handOver()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-013` | | Function Name | Identity fields (`collectedBy`, `disposedBy`, `requestedBy`, `deliveredBy`) are always read from the JWT |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-114 (amended 2026-07-12) changed `authorizedBy`-type fields from free-text entry to automatic, read-only capture from the logged-in account, to guarantee a tamper-proof audit trail. CWE-807. Source: `MF08-TC-013`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 4 | 0 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Operation | `register()` | O | | | |
| | `dispose()` | | O | | |
| | `handOver()` — `requestedBy` | | | O | |
| | `handOver()` — `deliveredBy` | | | | O |
| Body sends forged name | `"Nguyễn Văn X"` / `"Trần Văn Hùng"` / `"ai đó"` / `"Trần Văn Hùng (bếp)"` | O | O | O | O |
| JWT actor name | `"Lê Thị Mai"` / `"Lê Thị Mai"` / `"Ngô Thị Hà (Y tế)"` / `"Ngô Thị Hà (Y tế)"` | O | O | O | O |
| **Confirm** | | | | | |
| Saved value | Matches JWT name in every case, forged body value discarded | O | O | O | O |
| DTO | Request DTOs have no setter (or `@JsonIgnore`) for these identity fields | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** `deliveredBy` is the field most likely to be mis-implemented: the legacy mock let caterer staff enter it by hand. Since BR-215 was deprecated (2026-07-26), Medical Staff physically carries the sample to the lab themselves — `deliveredBy` must equal that same actor, with no manual-entry path at all. Allowing a caller to inject another person's name into `collectedBy`/`disposedBy`/`requestedBy`/`deliveredBy` reproduces exactly the sample-swap risk the 2026-07-26 BR-113 amendment was written to close, and voids columns 11/12 of Mẫu số 5 as legal evidence.

---

## Function: `SampleLabRequestService.handOver()` — Hand-over evidence is a hard gate (BR-212)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-014` | | Function Name | `testCriteria` required for LAB, `authorityId` required for AUTHORITY, ≥1 hand-over record required |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-212 / TT 14/2011/TT-BYT Điều 5.2 requires test criteria (mandatory when handing to a lab) and a photo/scan of the receiving lab's acknowledgment (mandatory), with an optional lab representative name. The system never auto-generates a hand-over record. Source: `MF08-TC-014`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 6 | 3 | 3 | 0 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| `handoverType` | `LAB` | O | O | O | | | O |
| | `AUTHORITY` | | | | O | O | |
| `testCriteria` | present | O | | O | | | O |
| | empty | | O | | O | O | |
| `authorityId` | present | O | O | O | O | | O |
| | null | | | | | O | |
| `HANDOVER_RECORD` file | 1 file attached | O | O | | O | O | O (`labRepresentative` = null) |
| | 0 files | | | O | | | |
| **Confirm** | | | | | | | |
| Return | Request created, sample(s) → `SENT_TO_LAB` | O | | | O | | O |
| Exception | `SMP-212`, HTTP 422, no request created, no status change | | O | O | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | N | A | B |
| Passed/Failed | | | | | | |
| Executed Date | | | | | | |
| Defect ID | | | | | | |

**Note:** `SENT_TO_LAB` is a terminal, non-reversible status (ADR-M08-2). If a request were allowed to succeed without a hand-over record, there would be no way to prove the sample actually reached the receiving party — the TDS classifies this invariant as 100% non-negotiable. The system itself never fabricates a "hand-over minutes" document; only user-uploaded evidence counts.

---

## Function: `SampleLabRequestService.handOver()` — Only `RETAINED` samples are eligible (BR-113)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-015` | | Function Name | Batch hand-over rejects any sample not currently `RETAINED` |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | BR-113 / UC-076 precondition: Medical Staff may only designate samples that are currently retained (not yet disposed and not already sent to a lab). Source: `MF08-TC-015`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| `sampleIds` batch | Mix of `RETAINED` + one `SENT_TO_LAB` | O | | | |
| | Mix of `RETAINED` + one `DISPOSED` | | O | | |
| | All `RETAINED` | | | O | |
| | Empty `[]` | | | | O |
| **Confirm** | | | | | |
| Exception | `SMP-113`, HTTP 409, names the offending sample code(s) in `details` | O | O | | |
| | `ValidationException`, HTTP 422 | | | | O |
| Return | Hand-over succeeds | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | N | A |
| Passed/Failed | | | | |
| Executed Date | | | | |
| Defect ID | | | | |

**Note:** Allowing a second hand-over on an already-dispatched or already-disposed sample would create two lab requests pointing at the same physical box — the exact defect the `semantic-audit.ts` rule `MAU_KHONG_GAN_NGUOC_VE_PHIEU` is designed to catch. `details` must name the violating sample code(s) so the caterer/medical UI can highlight the correct row.

---

## Function: `FoodSampleStatus` — Retained-sample state machine (ADR-M08-2)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF08-UT-016` | | Function Name | State transition guard for `RETAINED` / `SENT_TO_LAB` / `DISPOSED` (+ 2 legacy read-only values) |
| Created By | Nguyễn Đức Mạnh | | Executed By | `<TesterName>` |
| Lines of code | `<LOC>` | | Lack of test cases | `<LOC/100*KLOC - TotalTestCases>` |
| Test requirement | Per ADR-M08-2 and TDS §6.4, `SENT_TO_LAB` is a terminal state with no reversal, and disposal metadata linking (UC-076 AF1) must not change a `DISPOSED` sample's status. Source: `MF08-TC-016`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `<#>` | `<#>` | 8 | 4 | 4 | 0 | 8 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| From | `RETAINED` | O | O | O | | | | | |
| | `SENT_TO_LAB` | | | | O | O | | | |
| | `DISPOSED` | | | | | | O | O | O |
| To | `SENT_TO_LAB` | O | | | | O | O | | |
| | `DISPOSED` | | O | | | | | O | O (with `incidentId` metadata) |
| | `RETAINED` | | | O | O | | | O | |
| **Confirm** | | | | | | | | | |
| Return | Transition allowed | O | O | O | | | | | O |
| Exception | `IllegalStateException`/`ConflictException` — transition rejected | | | | O | O | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | A | A | A | A | B |
| Passed/Failed | | | | | | | | |
| Executed Date | | | | | | | | |
| Defect ID | | | | | | | | |

**Note:** `UTCID08` (`DISPOSED → DISPOSED` + `incidentId`) is the UC-076 AF1 metadata-link path — the only mutation a `DISPOSED` sample can undergo, and it must never change `status`. The two legacy values (`RETRIEVAL_REQUESTED`, `PENDING_LAB_DELIVERY`) must remain reachable only from historical data — no transition may produce them, or the deprecated BR-215 process would silently come back to life.
