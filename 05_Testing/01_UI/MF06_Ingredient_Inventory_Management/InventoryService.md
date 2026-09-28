# Unit Test Case — InventoryService (MF06 Ingredient & Inventory Management)

---

## Function: `InventoryService.remaining()` / `adjust(AdjustRequest)` / `storeBatch(batchCode, storageZone)`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-002` | | Function Name | `InventoryService.remaining()` / `InventoryService.adjust()` / `InventoryService.storeBatch()` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `Remaining stock must always be a derived, runtime-computed number — initialQuantity + SUM(delta) over the append-only transaction ledger — never a stored remaining_quantity column (ADR-M06-1, L5: number + separate unit field, no formatted string); an ADJUSTMENT transaction must be blocked with 400 ING-204 whenever either reasonCode or managerSignature is missing/blank, enforced as a decision table over both fields so non-repudiation is guaranteed (BR-204, ⚠️ ORACLE GAP L6 — message content not asserted, only code/status/non-blank English message); storing a batch into a storageZone that does not match the ingredient's required temperature group must be rejected with 422 ING-202 and a details array naming the offending field (BR-202, D2).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `5` | `1` | `4` | `0` | `5` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | Batch `initialQuantity=45kg`; ledger has `RECEIPT +45` (original receipt line), `ISSUE -24.1`, `ISSUE -7.6` | O | | | | |
| | `ADJUSTMENT` on `BATCH-GF-4388`, `delta=-2 liters`, `reasonCode=null`, `managerSignature="Le Thi Mai"` | | O | | | |
| | `ADJUSTMENT` on `BATCH-GF-4388`, `delta=-2 liters`, `reasonCode=SPOILAGE`, `managerSignature=""` (blank) | | | O | | |
| | `ADJUSTMENT` on `BATCH-GF-4388`, `delta=-2 liters`, `reasonCode=null`, `managerSignature=null` | | | | O | |
| | Ingredient `ING-THIT-LON-VAI` requires `storageZone=CHILLED` | | | | | O |
| Operation Input | `remaining("BATCH-GF-4411")` | O | | | | |
| | `adjust(request)` — case b: reason missing | | O | | | |
| | `adjust(request)` — case c: signature blank | | | O | | |
| | `adjust(request)` — case d: both missing | | | | O | |
| | `storeBatch(batchCode, StorageZone.DRY)` | | | | | O |
| **Confirm** | | | | | | |
| Return | `remaining()=13.3` (`BigDecimal`, `isEqualByComparingTo`), `unit="kg"`; returned as a **number**, never `"13.3 kg"` (L5); no `remaining` field read from the entity | O | | | | |
| | — (all abnormal cases blocked before persistence; `verify(txRepo, never()).save(any())`) | | | | | |
| | Matching zone (`CHILLED`) case stores successfully | | | | | O |
| Exception | `MissingReasonOrSignatureException` → `400 ING-204` (⚠️ ORACLE GAP L6 — code + non-blank English message asserted, not literal text) | | O | O | O | |
| | `StorageZoneMismatchException` → `422 ING-202`, `details=[{"field":"storageZone","message":"Thit lon nac vai requires CHILLED"}]` (⚠️ ORACLE GAP L6) | | | | | O |
| Log message | — (⚠️ ORACLE GAP L6 — no literal message asserted for `ING-204`/`ING-202`) | | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

> UTCID mapping: UTCID01 = `M06-TC-006`, UTCID02 = `M06-TC-009`, UTCID03 = `M06-TC-010`, UTCID04 = `M06-TC-010b`, UTCID05 = `M06-TC-011`.

---

## Function: `IssueSlipService.issue()`

> No dedicated `IssueSlipServiceTest.java` is listed in the TDS test-file map (§4.1 "Ban do file test"); this method's unit coverage is grouped here alongside `InventoryService` as the nearest logical peer (ledger / traceability of stock movements).

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-003` | | Function Name | `IssueSlipService.issue()` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `Every BOM line in an issue slip must resolve to a real catalog ingredientId before any ISSUE ledger line is written; a name mismatch between the BOM label and the catalog label (e.g. "Tom su dong lanh" vs. "Tom su") must be treated as unresolved and reject the whole issue() call with 422 ING-076 and unknownIngredient=true rather than silently skipping the line, because a silently-dropped line breaks the traceability chain (BR-076, constraint C6) without raising any error.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `1` | `0` | `1` | `0` | `1` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | BOM line references an ingredient by label `"Tom su dong lanh"`, which does not exactly match the catalog label `"Tom su"` for the intended `ingredientId` | O |
| Operation Input | `issue()` invoked with the mismatched BOM line | O |
| **Confirm** | | |
| Return | — (rejected before any write) | |
| Exception | Request rejected: `422 ING-076`, response field `unknownIngredient=true`; **no** `ISSUE` ledger line written for this line (`verify(txRepo, never()).save(any())` for the unresolved line) | O |
| Log message | — (⚠️ ORACLE GAP L6 — `ING-076` has no MSG in `error_message.md` v1.5; only code/status/non-blank English message asserted) | |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

> UTCID mapping: UTCID01 = `M06-TC-015`.
