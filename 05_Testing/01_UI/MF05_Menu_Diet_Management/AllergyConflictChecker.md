# Unit Test Case — AllergyConflictChecker (MF05 Menu & Diet Management)

---

## Function: `check` / `hasBlockingConflict`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-002` | | Function Name | `AllergyConflictChecker.check` / `hasBlockingConflict` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `95` | | Lack of test cases | `0` |
| Test requirement | `Detect allergy conflicts between menu dishes and verified student allergy declarations of the same grade (BR-063), count affected students grouped by allergen, and treat a dish with a covering substitute as non-blocking (BR-063 + BR-065).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `2` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `HS-3A-001` (`FX-006`): allergies = [Tôm, Cua], `healthStatus=VERIFIED`, severity=Severe | O | O |
| | `HS-3A-004` (`FX-007`): allergy = Tôm, `healthStatus=VERIFIED` | O | O |
| | Menu `DRAFT`, `grade="Khối 3"`, contains a dish with allergen "Tôm" | O | O |
| | Classes `3A`/`3B` belong to Khối 3 (`FX-018`) | O | O |
| Dish substitute Input | Dish has **no** `substitutes` for the conflicting allergen | O | |
| | Dish has a `MenuDishSubstitute` (`ca-basa-sot-ca-chua` → `dau-phu-sot-ca-chua`, `FX-008`/`FX-009`) covering exactly the "Tôm" allergen | | O |
| **Confirm** | | | |
| Return | `check()` returns 1 `AllergyConflict`, `allergen="Tôm"`, `affectedStudentCount == 2` (only `VERIFIED` records counted, grouped by allergen) | O | O |
| | `hasBlockingConflict() == true` | O | |
| | `hasBlockingConflict() == false`; conflict still returned with `blocking == false` (kept as warning for School Admin) | | O |
| | `submit()` succeeds → `PENDING_APPROVAL` | | O |
| Exception | `AllergyConflictException`, `errorCode == "MENU-051"`, HTTP 409; `status` stays `DRAFT` | O | |
| Log message | `MSG-051`: Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students. (`[Number]=2`, no student identity/name/code exposed — PDPD, cross-checked in `MF05-TC-SEC-009`) | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

> UTCID mapping: UTCID01 = `MF05-TC-003`, UTCID02 = `MF05-TC-004`.
