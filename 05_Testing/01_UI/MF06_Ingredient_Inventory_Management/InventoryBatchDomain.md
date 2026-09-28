# Unit Test Case — InventoryBatch Domain (MF06 Ingredient & Inventory Management)

---

## Function: `InventoryBatch.shelfStatus(LocalDate today)` / `InventoryBatch.isLocked(LocalDate today)`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-006` | | Function Name | `InventoryBatch.shelfStatus()` / `InventoryBatch.isLocked()` (pure domain, `@Transient`) |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `shelfStatus(today) must derive one of exactly 3 SCREAMING_SNAKE values from expiryDate with no stored status column read (CF-M06-11): EXPIRED when expiryDate is before today; EXPIRING_SOON when expiryDate is on or up to 2 days after today (inclusive upper boundary, ADR-M06-2); IN_STOCK otherwise — the third label is IN_STOCK, not ACTIVE, because ACTIVE is already the FE STATUS_BADGE key for partner status (L2). isLocked(today) must be the logical OR of the two independent axes — shelfStatus==EXPIRED or quarantine!=null (ADR-M06-2, unifying BR-205 + BR-149) — and removing quarantine must never overwrite an EXPIRED shelfStatus.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `0` | `1` | `2` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `FX-CLOCK`: `today = 2026-07-06`. No DB, no Spring context | O | O |
| | `@ParameterizedTest` 5 boundary dates: `expiryDate` = `2026-07-05` (yesterday), `2026-07-06` (today), `2026-07-07` (today+1), `2026-07-08` (today+2, upper boundary), `2026-07-09` (today+3) | O | |
| | Decision table, 4 combinations of `(shelfStatus, quarantine)`: (a) `IN_STOCK`/`null` = `FX-BATCH-OK`; (b) `EXPIRED`/`null` = `FX-BATCH-EXPIRED`; (c) `IN_STOCK`/non-null = `FX-BATCH-QUAR-NOINC`; (d) `EXPIRED`/non-null = `FX-BATCH-QUAR-INC` | | O |
| | Cell (d), quarantine subsequently removed | | O |
| Operation Input | `shelfStatus(today)` called for each of the 5 dates | O | |
| | `isLocked(today)` called for each of the 4 cells, then re-called for cell (d) after quarantine removal | | O |
| **Confirm** | | | |
| Return | `EXPIRED`, `EXPIRING_SOON`, `EXPIRING_SOON`, `EXPIRING_SOON`, `IN_STOCK` respectively for the 5 boundary dates; no read of any `status` column | O | |
| | (a) `false`, (b) `true`, (c) `true`, (d) `true`; cell (d) after quarantine removal: still `true` (shelfStatus is not overwritten by un-quarantining) | | O |
| Exception | — | | |
| Log message | — | | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | B | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> UTCID mapping: UTCID01 = `M06-TC-004`, UTCID02 = `M06-TC-005`.
