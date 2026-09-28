# Unit Test Case — BomQuantityCalculatorService (MF06 Ingredient & Inventory Management)

---

## Function: `BomQuantityCalculator.toPurchaseQuantity(perServing, servings, ingredient)`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-007` | | Function Name | `BomQuantityCalculator.toPurchaseQuantity()` |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `For weight/volume units (kg, g, lit, ml), purchase quantity = edible quantity / (1 - refuseFactor), scale 3, HALF_UP, matching NUMERIC(12,3) (constraint C7). refuseFactor=0.0 must divide cleanly (no ArithmeticException, no special-casing) and refuseFactor=null (field not declared in catalog) must be treated as 0.0 without NPE. For count units (qua, hop, cai, bo), the refuse-factor division must NOT be applied even when the catalog does declare a non-zero refuseFactor for that ingredient (e.g. ING-TRUNG-CUT=0.18) — classification must be by unit, never by refuseFactor==null, since count units can carry a real refuseFactor value that simply does not apply to purchase-quantity math (C7, second half of the rule).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `0` | `1` | `2` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `ING-TOM-SU`: `refuseFactor=0.45`, unit=`kg` | O | |
| | `ING-CA-BASA`: `refuseFactor=0.0`, unit=`kg` (comparison case, same UTCID01 test) | O | |
| | `ING-GAO-TAM` / `ING-PHO-MAI`: `refuseFactor` not declared (`null`), unit=`kg` (comparison case, same UTCID01 test) | O | |
| | `FX-BATCH-COUNT-UNIT` / `FX-CATALOG`: `ING-TRUNG-CUT`, unit=`"qua"` (count), `refuseFactor=0.18` (declared and must be ignored) | | O |
| Operation Input | `toPurchaseQuantity(perServing=40g, servings=380, ING-TOM-SU)` | O | |
| | `toPurchaseQuantity(...)` for `ING-CA-BASA` and for `ING-GAO-TAM`/`ING-PHO-MAI` (same UTCID) | O | |
| | `toPurchaseQuantity(perServing=3 qua, servings=380, ING-TRUNG-CUT)` | | O |
| **Confirm** | | | |
| Return | `27.636 kg` (`15.2 / (1-0.45)`, scale 3, HALF_UP) | O | |
| | `ING-CA-BASA` case: result = edible quantity unchanged, no special-case branch, no `ArithmeticException`; `null`-refuseFactor case: treated as `0.0`, no `NullPointerException` | O | |
| | `1140 qua` (`3 x 380`) — **not** divided by `(1-0.18)`; rule applies to the full count-unit set: `qua`, `hop`, `cai`, `bo` | | O |
| Exception | — (none expected in either case; absence of `ArithmeticException`/`NullPointerException` is itself an assertion) | | |
| Log message | — | | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | B | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> UTCID mapping: UTCID01 = `M06-TC-007`, UTCID02 = `M06-TC-008`.
