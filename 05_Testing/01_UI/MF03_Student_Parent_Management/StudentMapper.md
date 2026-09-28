# Unit Test Case — StudentMapper (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-008`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/mapper/StudentMapperTest.java`.

---

## Function: `toResponse()` — pass-through of pre-computed `specialDiet` / `parents`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-006` | | Function Name | `StudentMapper.toResponse()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `38` | | Lack of test cases | `0` |
| Test requirement | `StudentMapper` is a MapStruct pass-through interface with no computed logic of its own (ADR-M03-3 / CF-M03-11). `specialDiet` (the most recently `APPROVED` `SpecialMealRequest`, chosen by `SpecialMealRequestRepository.findFirstByStudentIdAndStatusOrderByRequestedAtDesc`) and `parents` are computed by the **service/repository** layer and merely assigned into `StudentResponse` by the mapper — this unit test verifies only that pass-through, not the "latest APPROVED" selection logic itself (that cross-table `ORDER BY` behavior is verified by `web.StudentSpecialDietIT`, since it depends on PostgreSQL ordering and cannot be simulated at the unit level). The test also asserts the `Student` entity carries no `specialDiet` column (CF-M03-11 — derived, not stored). | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `5` | `0` | `0` | `3` | `0` | `2` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| `specialDiet` param passed in | `"Gluten-Free"`, `hasSpecialDiet="yes"` | O | | | | |
| | `null`, `hasSpecialDiet="no"` (service computed null — only a PENDING SMR exists) | | O | | | |
| | `null`, `hasSpecialDiet="no"` (service computed null — no SMR exists at all) | | | O | | |
| Entity/table `Student` schema | `Student.class.getDeclaredFields()` | | | | O | |
| `parents` param passed in | `[ParentSummary("U-9905", "Nguyễn Văn Bình (synthetic)", "Bố", true)]` | | | | | O |
| **Confirm** | | | | | | |
| Return | `response.specialDiet() == "Gluten-Free"`, `response.hasSpecialDiet() == "yes"` | O | | | | |
| | `response.specialDiet() == null`, `response.hasSpecialDiet() == "no"` | | O | O | | |
| Schema Assertion | No declared field named `specialDiet` on `Student` | | | | O | |
| Return | `response.parents()` has size 1; `parents().get(0).parentId() == "U-9905"`; `isPrimary() == true` | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | B | B |
| Passed/Failed | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | |

Note: **Lệch giữa TestSpec và code thật** — the TestSpec's "Feature Under Test" for `MF03-TC-008` names `StudentMapper.toResponse()` as the component that *selects* the latest-APPROVED special meal request; reading the real code shows `StudentMapper` is a pure MapStruct pass-through with no such logic — selection is a repository derived query. This doc file, and the test it maps to, follow the real code (source of truth), not the original TestSpec wording. UTCID02 and UTCID03 are two different upstream service states that both produce `specialDiet=null` at the mapper boundary; the mapper cannot and does not distinguish them, which is correct.
