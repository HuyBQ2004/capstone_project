# Unit Test Case — FeedbackListItemMapper / FeedbackDetailMapper (MF10 Feedback & Announcement Management)

---

## Function: `FeedbackListItemMapper.toDto()` / `FeedbackDetailMapper.toOwnerDto()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-009` | | Function Name | `FeedbackListItemMapper.toDto` / `FeedbackDetailMapper.toOwnerDto` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | List/summary DTOs must never carry `studentId`, `student`, `submitterName`, `submitterUserId` (BR-145; UC-162 Normal Flow step 3, "hiding the identity of the submitter to protect privacy"; ADR-M10-5). The owner-facing DTO must still contain `studentId` (CWE-359, PDPD scope, DPO Sign-off item). Source: MF10-TC-008. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition (mandatory assertion before the negative check) | `entity.getStudentId() != null` on a freshly built entity — proves the source data actually has PII before asserting it is hidden ⭐ | O | O | O | O |
| Mapper under test | `listItemMapper.toDto(entity)` | O | O | | |
| | `detailMapper.toOwnerDto(entity)` | | | O | O |
| **Confirm** | | | | | |
| Return (positive fields) | Has `className`, `mealDate`, `rating`, `tags`, `comment`, `illnessKeyword` | O | | | |
| Return (absence, serialized JSON) | `!json.has("studentId") && !json.has("student") && !json.has("submitterName") && !json.has("submitterUserId")` | | O | | |
| Return (owner view) | Has `studentId` (owner still sees their own child) | | | O | |
| Isolation | Two mappers produce two different results from the **same** source entity | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: The absence assertion must run against the **serialized JSON**, not the object graph — a `null`-valued field still exposes the field name in the contract and FE code may branch on `"studentId" in item`. Returning the raw entity to School Administration/Caterers would expose a child's name tied to health content (e.g. "abdominal pain") to roughly 26 caterer accounts and 8 school-admin accounts per school — this is the DPO Sign-off item of the TestSpec.
