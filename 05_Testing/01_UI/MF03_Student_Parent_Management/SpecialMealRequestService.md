# Unit Test Case — SpecialMealRequestService (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-005`, `MF03-TC-006`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/service/SpecialMealRequestServiceTest.java`.

---

## Function: `approve()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-007` | | Function Name | `SpecialMealRequestService.approve()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `218` | | Lack of test cases | `0` |
| Test requirement | BR-220: a `dietaryType` outside the 5 standard values (`StandardDietCatalog`) is neither rejected nor auto-approved: it transitions to `NEEDS_INFO` (`STU-066` / `MSG-144`: "This dietary type is outside the standard list and requires medical justification before approval.") pending Medical Staff justification. A value inside the standard list approves directly; a second `approve()` call with a `reviewNote` after `NEEDS_INFO` must reach `APPROVED` — proving BR-220 is a review gate, not a rejection gate. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `3` | `0` | `0` | `2` | `1` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `SMR-2026-0001` (`dietaryType="Keto"`, PENDING) | O | | |
| | `SMR-2026-0001` (`dietaryType="Lactose-Free"`, PENDING) | | O | |
| | `SMR-2026-0001` (`dietaryType="Keto"`, `status=NEEDS_INFO`) | | | O |
| Operation | `approve(code, reviewNote=null, medicalStaff)` — 1st call | O | | |
| | `approve(code, reviewNote=null, medicalStaff)` | | O | |
| | `approve(code, reviewNote="Có chỉ định dinh dưỡng của bác sĩ nội tiết", medicalStaff)` — 2nd call | | | O |
| **Confirm** | | | | |
| Exception | `ApiException` `code = "STU-066"`, `status = 422`, message = `MSG-144` | O | | |
| Return | `response.status() = "NEEDS_INFO"` (not `PENDING`, not `REJECTED`) | O | | |
| | `response.status() = "APPROVED"` | | O | O |
| | `smr.getReviewedByUserId() != null` | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | N | N |
| Passed/Failed | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | |

Note: `MSG-114` is retired as of `error_message.md` v1.9 — must **not** reappear anywhere for this path. `StandardDietCatalog` supplies the standard-diet list as configuration, not a hardcoded literal in the test.

---

## Function: `reject()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-010` | | Function Name | `SpecialMealRequestService.reject()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `218` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-031 review-gate exception (UC-078) shared pattern: `reject()` must reject a blank `reason` with `STU-032` / `MSG-113` ("A reason is required for this action."). | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `1` | `0` | `0` | `0` | `1` | `0` | `1` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Target | `SMR-2026-9999` (no repository lookup needed — reason validated first) | O |
| `reason` value | `""` (empty) | O |
| **Confirm** | | |
| Exception | `ApiException` `code = "STU-032"`, message = `MSG-113` | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | P |
| Executed Date | 2026-08-14 |
| Defect ID | |

Note: this is the same `STU-032`/`MSG-113` gate as `HealthReviewService.requestAllergyInfo()`, exercised here for `SpecialMealRequestService.reject()`. Cross-referenced with `MF03-TC-006`.
