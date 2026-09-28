# Unit Test Case — HealthStatusCalculator (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-004`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/mapper/HealthStatusCalculatorTest.java`.

---

## Function: `compute()` — derived `healthStatus` decision table (ADR-M03-3)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-005` | | Function Name | `HealthStatusCalculator.compute()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `69` | | Lack of test cases | `0` |
| Test requirement | `healthStatus` is a derived field, never a stored column (AP-AI-009). Formula per ADR-M03-3: `NEEDS_INFO` if ≥1 declaration is `NEEDS_INFO`; else `PENDING` if ≥1 is `PENDING`; else `VERIFIED` if ≥1 is `VERIFIED`; if no declaration exists at all → `VERIFIED`. The formula must scan **both** `allergy_declarations` and `chronic_condition_declarations` lists passed in. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `6` | `0` | `0` | `2` | `0` | `4` | `6` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| `allergyStatuses` | `[]` | O | | | | | |
| | `[VERIFIED, VERIFIED]` | | O | | | | |
| | `[VERIFIED, PENDING]` | | | O | | | |
| | `[VERIFIED, NEEDS_INFO]` | | | | O | | |
| | `[PENDING, NEEDS_INFO]` | | | | | O | |
| | `[PENDING]` | | | | | | O |
| `chronicStatuses` | `[]` | O | O | O | O | O | |
| | `[NEEDS_INFO]` | | | | | | O |
| **Confirm** | | | | | | | |
| Return `compute(...)` | `VERIFIED` (no-declaration case defaults to VERIFIED) | O | O | | | | |
| | `PENDING` | | | O | | | |
| | `NEEDS_INFO` (priority `NEEDS_INFO` > `PENDING` > `VERIFIED`) | | | | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | B | N | N | B | B | B |
| Passed/Failed | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | |

Note: UTCID06 is the case most likely to be implemented wrong (formula must scan the chronic-condition table too, not only allergies). This decision table exercises `compute()` only; the label-vs-declaration semantics of `computeLabel()` are covered by the second function block below.

---

## Function: `computeLabel()` — "no declaration" vs "verified" label (added 2026-08-08)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-011` | | Function Name | `HealthStatusCalculator.computeLabel()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `69` | | Lack of test cases | `0` |
| Test requirement | `compute()` returns `VERIFIED` for an empty declaration set (a rule inherited from legacy mock seed data), which caused a real UI defect: a just-created student with zero declarations displayed as "Đã xác minh" (Verified) on the Admin's Students screen even though nobody had reviewed anything yet. `computeLabel()` was added to separate "not declared" from "verified": it returns the sentinel `NO_DECLARATION` for empty/`null` input, and otherwise falls back to `compute()`'s result name. `compute()` itself is unchanged — only the API-facing label changed. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `3` | `0` | `0` | `1` | `0` | `2` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID07 | UTCID08 | UTCID09 |
|---|---|---|---|---|
| **Condition** | | | | |
| `allergyStatuses` / `chronicStatuses` | `[]` / `[]` | O | | |
| | `null` / `null` | | O | |
| | `[VERIFIED]` / `[]`, `[PENDING]` / `[]`, `[]` / `[NEEDS_INFO]` (3 sub-assertions in 1 method) | | | O |
| **Confirm** | | | | |
| Return `computeLabel(...)` | `== NO_DECLARATION`, `!= "VERIFIED"` | O | | |
| | `== NO_DECLARATION` | | O | |
| | Equals `compute(...)` result name for each sub-case (`VERIFIED`, `PENDING`, `NEEDS_INFO`) | | | O |

### 4. Execution Result

| | UTCID07 | UTCID08 | UTCID09 |
|---|---|---|---|
| Type (N/A/B) | B | B | N |
| Passed/Failed | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | |

Note: UTCID09 (`withDeclarationsFallsBackToCompute`) asserts 3 separate `computeLabel()` calls in one `@Test` method (not `@ParameterizedTest`), so it is counted here as a single UTCID/test case per Bước 5's "one @Test method = one UTCID" rule, while its 3 internal assertions are recorded together in the Confirm row.
