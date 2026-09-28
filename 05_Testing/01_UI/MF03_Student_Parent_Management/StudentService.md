# Unit Test Case — StudentService (MF03 Student & Parent Management)

> Source: `TestSpec-MF03_Student_Parent_Management.md` §4.1 `MF03-TC-002`. Test file: `04_Development/Backend/src/test/java/com/sfms/mf03/service/StudentServiceTest.java`.

---

## Function: `create()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-002` | | Function Name | `StudentServiceImpl.create()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `437` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-030: every student must be linked to at least 1 parent account at creation time. Creating a student with `parent = null` must be rejected with `STU-030` / `MSG-111` ("Each student must be linked to at least one parent account.") and `details` containing `{field: "parent.email"}`. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `1` | `0` | `0` | `0` | `1` | `0` | `1` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Operation | `create({parentId: null, name, dob, classCode: "3A", parent: null})` | O |
| **Confirm** | | |
| Exception | `ApiException` `code = "STU-030"`, `status = 422`, message = `MSG-111`, `details` contains `{field: "parent.email"}` | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | P |
| Executed Date | 2026-08-14 |
| Defect ID | |

Note: BR-030 cannot be expressed as a single-table `CHECK` (cross-table invariant) — enforced inside `StudentServiceImpl.create()` before any `students` row is persisted, backed by the reconciliation query of TDS §14.1 query 3.

---

## Function: `unlink()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `STU-FUNC-012` | | Function Name | `ParentLinkServiceImpl.unlink()` |
| Created By | Bui Quang Huy | | Executed By | Huy |
| Lines of code | `83` | | Lack of test cases | `0` |
| Test requirement | Enforce BR-030 on removal: unlinking the last remaining parent of a student must be rejected with `STU-030`; unlinking one of two parents must still succeed. Although these two cases live in `StudentServiceTest.java` (paired unit cases for `MF03-TC-002`), both actually exercise `ParentLinkServiceImpl.unlink()`, not `StudentServiceImpl` — `StudentService` has no `unlink()` method of its own. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `2` | `0` | `0` | `1` | `1` | `0` | `2` |

### 3. Condition / Confirmation Matrix

| | | UTCID02 | UTCID03 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | Student has exactly 1 linked parent (`countByStudentId == 1`) | O | |
| | Student has 2 linked parents (`countByStudentId == 2`, `findByStudentId` returns 1 matching link) | | O |
| Operation | `unlink(studentId, parentUserId, schoolAdmin)` — the only/last parent | O | |
| | `unlink(studentId, parentUserId, schoolAdmin)` — one of two parents | | O |
| **Confirm** | | | |
| Exception | `ApiException` `code = "STU-030"` | O | |
| Return | Completes without throwing (`assertThatCode(...).doesNotThrowAnyException()`) | | O |

### 4. Execution Result

| | UTCID02 | UTCID03 |
|---|---|---|
| Type (N/A/B) | A | N |
| Passed/Failed | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 |
| Defect ID | | |

Note: UTCID numbering continues from `create()` above (UTCID01) since both functions are paired unit cases for `MF03-TC-002` in the same test class; total test-class test count is 1 (create) + 2 (unlink) = 3, matching `StudentServiceTest`'s 3 `@Test` methods.
