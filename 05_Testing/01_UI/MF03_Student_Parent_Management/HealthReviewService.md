# Unit Test Case — HealthReviewService (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-003`, `MF03-TC-006`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/service/HealthReviewServiceTest.java`.

---

## Function: `verifyAllergy()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-003` | | Function Name | `HealthReviewService.verifyAllergy()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `204` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-031: a `SEVERE` allergy declaration cannot be verified without supporting medical documentation (`proofFileId`) — rejected with `STU-031` / `MSG-112`: "Severe allergy declaration requires supporting medical documentation." `MODERATE` declarations verify successfully with or without a document; `SEVERE` verifies successfully once a `proofFileId` is present. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `3` | `0` | `0` | `2` | `0` | `1` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `ALG-901`-style — SEVERE, `proofFileId = null`, PENDING | O | | |
| | `ALG-005`-style — MODERATE, `proofFileId = null`, PENDING | | O | |
| | SEVERE, `proofFileId = <random UUID>`, PENDING | | | O |
| Operation | `verifyAllergy(code, medicalStaff)` | O | O | O |
| **Confirm** | | | | |
| Exception | `ApiException` `code = "STU-031"`, `status = 422`, message = `MSG-112`; declaration `status` remains `PENDING` (not mutated before validation) | O | | |
| Return | `response.status() = "VERIFIED"` | | O | O |
| | `decl.getVerifiedAt() != null` | | O | |
| | `decl.getVerifiedByUserId() != null` | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | B | N | N |
| Passed/Failed | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | |

Note: the service must check **before** flipping `status`, so `STU-031`/`MSG-112` is returned as 422; DB `CHECK ck_severe_needs_proof` is only the last-resort backstop (if relied upon alone, the client would see a 500, not 422).

---

## Function: `requestAllergyInfo()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-004` | | Function Name | `HealthReviewService.requestAllergyInfo()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `204` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-031 review-gate exception (UC-077): `requestAllergyInfo()` must reject a missing/blank `reason` with `STU-032` / `MSG-113` ("A reason is required for this action."). A non-blank reason must transition the declaration to `NEEDS_INFO` and persist `reviewNote` verbatim. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `3` | `0` | `0` | `1` | `2` | `0` | `3` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Target | `ALG-2026-9999` (no repository lookup needed — reason validated first) | O | O | |
| | `ALG-004`-style declaration (`MODERATE`, `proofFileId = null`, PENDING) | | | O |
| `reason` value | `null` | O | | |
| | `"   "` (whitespace only) | | O | |
| | `"Thiếu đơn bác sĩ xác nhận mức độ dị ứng"` | | | O |
| **Confirm** | | | | |
| Exception | `ApiException` `code = "STU-032"`, message = `MSG-113` | O | O | |
| Return | `response.status() = "NEEDS_INFO"`; `decl.getReviewNote()` persisted verbatim | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | |

Note: DTO validation uses blank-string semantics (not just `null`) so whitespace-only reasons are also rejected. The paired `reject()` blank-reason case (`STU-032`) lives in `SpecialMealRequestService.md` since it is exercised through `SpecialMealRequestServiceTest`, not this class.
