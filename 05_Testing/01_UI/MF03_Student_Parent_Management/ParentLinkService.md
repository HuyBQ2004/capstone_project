# Unit Test Case — ParentLinkService (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-001`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/service/ParentLinkServiceTest.java`.

---

## Function: `linkOrCreateParent()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-001` | | Function Name | `ParentLinkServiceImpl.linkOrCreateParent()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `83` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-201: a parent account may be linked to a maximum of 5 student profiles. At the 6th link attempt the operation must be blocked with `STU-201` / `MSG-110` (verbatim: "This parent account is already linked to 5 students. School Administration verification is required.") and no `student_parents` row may be written; the 5th link (4→5) must still succeed. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `2` | `0` | `0` | `0` | `0` | `2` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Parent `U-0901` already linked to exactly 5 students (`FX-003`) | O | |
| | Parent `U-0902` already linked to exactly 4 students (`FX-004`) | | O |
| `parentUserId` | `U-0901` | O | |
| | `U-0902` | | O |
| New student | `HS-9Z-002` (fresh, unlinked) | O | O |
| **Confirm** | | | |
| Return | Link succeeds; `countLinkedStudents(parentUserId) == 5` | | O |
| Exception | `ApiException` `code = "STU-201"`, `status = 409` | O | |
| DB Verification | `repository.save()` on `student_parents` **never** invoked (BVA 5→6 blocked) | O | |
| | Exactly 1 new `student_parents` row written (BVA 4→5 allowed) | | O |
| Log message | `MSG-110`: "This parent account is already linked to 5 students. School Administration verification is required." | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | B | B |
| Passed/Failed | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 |
| Defect ID | | |

Note: service-level check is the first gate; the final backstop is DB `TRIGGER check_parent_link_limit` (§5.2), covered at integration level by `MF03-TC-INT-007` scenario 5 (bulk-import rollback). Count must use `SELECT … FOR UPDATE` to avoid a race between two concurrent link requests.
