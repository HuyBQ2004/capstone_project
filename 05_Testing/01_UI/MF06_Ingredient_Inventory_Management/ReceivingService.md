# Unit Test Case — ReceivingService (MF06 Ingredient & Inventory Management)

---

## Function: `ReceivingService.record(RecordReceivingRequest)` + `PartnerStatusPort.isEligible()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-004` | | Function Name | `ReceivingService.record()` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `record() must enforce three independent hard gates before any row is written: (1) a rejected receiving line must carry both a rejectReason and >=1 rejectPhotoUrls, otherwise 400 ING-078 (BR-078); (2) closing/confirming a receiving session requires photosCount >= 1 — sessions still IN_PROGRESS are exempt via ck_rcv_photos, otherwise 400 ING-072 (BR-072); (3) the supplier must be status=APPROVED at the moment the session opens, verified through PartnerStatusPort.isEligible() by code (not by importing the MF04 entity, constraint C9) — a SUSPENDED supplier is rejected with 409 ING-070 and zero rows written across receiving_sessions/batches/transactions, since the whole session is a single 5-table transaction (ADR-M06-5, BR-070). A supplier that is APPROVED but has certStatus=EXPIRING must still be allowed, since BR-070 only checks status, not certificate validity (CF-M06-7).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `4` | `0` | `4` | `0` | `4` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Receiving line: `rejected=true`, `rejectReason=null`, `rejectPhotoUrls`=1 photo | O | | | |
| | Session being closed/confirmed with `photosCount=0` (session status not `IN_PROGRESS`) | | O | | |
| | Receiving line: `rejected=true`, `rejectReason`="Rau dap nat, co mui la — khong dat cam quan", `rejectPhotoUrls=[]` (empty) | | | O | |
| | `FX-SUP-SUSPENDED` = `SUP-004`, `status=SUSPENDED`, `certExpiry=2026-06-22`; `PartnerStatusPort` mock returns `SUSPENDED` | | | | O |
| | (comparison case, same UTCID04 test) `FX-SUP-EXPIRING` = `SUP-003`, `status=APPROVED`, `certStatus=EXPIRING` — must still be allowed | | | | O |
| Operation Input | `record(request)` with the reject-without-reason line | O | | | |
| | `record(request)` closing the session with zero photos | | O | | |
| | `record(request)` with the reject-with-empty-photos line | | | O | |
| | `record(request)` opening a session for `SUP-004` | | | | O |
| **Confirm** | | | | | |
| Return | — (rejected before write in cases 1-3) | | | | |
| | `SUP-003` case: session opens successfully (BR-070 checks `status` only) | | | | O |
| Exception | `400 ING-078` (⚠️ ORACLE GAP L6) | O | | | |
| | `400 ING-072` (⚠️ ORACLE GAP L6) | | O | | |
| | `400 ING-078` (⚠️ ORACLE GAP L6) | | | O | |
| | `SupplierNotApprovedException` → `409 ING-070` (⚠️ ORACLE GAP L6); zero rows in `receiving_sessions`/`batches`/`transactions` (`verify(..., never()).save(any())` on all three repos) | | | | O |
| Log message | — (⚠️ ORACLE GAP L6 — no literal MSG asserted; only non-blank English message) | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

> UTCID mapping: UTCID01 = `M06-TC-012`, UTCID02 = `M06-TC-012b`, UTCID03 = `M06-TC-012c`, UTCID04 = `M06-TC-013`.
