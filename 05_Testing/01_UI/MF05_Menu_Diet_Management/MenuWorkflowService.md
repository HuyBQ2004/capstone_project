# Unit Test Case — MenuWorkflowService (MF05 Menu & Diet Management)

---

## Function: `submit` / `approve` / `reject` / state machine transitions

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MENU-FUNC-001` | | Function Name | `MenuWorkflowService.submit` / `approve` / `reject` |
| Created By | `Nguyễn Phương Thảo` | | Executed By | `Thao` |
| Lines of code | `210` | | Lack of test cases | `0` |
| Test requirement | `Validate submission lead-time >= 3 days (BR-061), enforce School-Administration-only approve/reject (BR-068), require approvalSignature/rejectReason (UC-006 step 5 / AF1), require approvedBy on APPROVED/PUBLISHED (semantic rule DA_DUYET_MA_THIEU_NGUOI_DUYET), and enforce the MenuStatus state machine (BR-068).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `9` | `1` | `5` | `3` | `9` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 |
|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | |
| Precondition | Menu `DRAFT`, all dishes have full BOM, no blocking allergy conflict | O | O | | | | | | | |
| | Menu `PENDING_APPROVAL` (`FX-001`) | | | O | O | O | | O | | |
| | Menu at various source states (`DRAFT`, `PENDING_APPROVAL`, `APPROVED`, `PUBLISHED`, `REJECTED`) | | | | | | | | O | O |
| | Actor JWT role ≠ `SCHOOL_ADMIN` (6 roles: SYS_ADMIN, SERVICE_CATERERS, MEDICAL_STAFF, TEACHER, PARENT, SUPPLIER) | | | O | | | | | | |
| | Actor JWT role = `SCHOOL_ADMIN` (`FX-021`) | | | O | O | O | | O | | |
| `menuDate` lead-time Input | `menuDate = today + 3` (on-point boundary) | O | | | | | | | | |
| | `menuDate = today + 2` (off-point, below boundary) | | O | | | | | | | |
| | `menuDate = today − 1` (already past the serving date) | | | | | | O | | | |
| `approvalSignature` Input | `null` / `""` / `"   "` | | | | O | | | | | |
| | `"SIG-2630"` (valid) | | | | O | | | O | | |
| `rejectReason` Input | `null` / `""` / `"   "` | | | | | O | | | | |
| | `"Thiếu suất riêng cho nhóm ăn chay Khối 3."` (valid) | | | | | O | | | | |
| `approvedBy` Input | Set `status = APPROVED`/`PUBLISHED` while `approvedBy` unset | | | | | | | O | | |
| State transition Input | Transition matrix 5×5 (`DRAFT/PENDING_APPROVAL/APPROVED/PUBLISHED/REJECTED`), including invalid edges `DRAFT→PUBLISHED`, `PUBLISHED→DRAFT` | | | | | | | | O | |
| | Full BR-068 lifecycle: `submit()`→`approve()`→`publish()` in order | | | | | | | | | O |
| **Confirm** | | | | | | | | | | |
| Return | `status == PENDING_APPROVAL`, `submittedAt` set, message `MSG-039` | O | | | | | | | | |
| | `status == APPROVED`, `approvedAt`/`approvedBy` set, message `MSG-040` | | | | O | | | O | | O |
| | `status == REJECTED`, `rejectReason` kept, message `MSG-074` | | | | | O | | | | |
| | Invalid-role attempt: `SCHOOL_ADMIN` succeeds, all 6 other roles denied (incl. `SYS_ADMIN`) | | | O | | | | | | |
| | Valid transitions succeed for every ✅ cell; `PENDING_APPROVAL→PUBLISHED` allowed in phase 1 with auto `approvedAt` + WARN log | | | | | | | | O | |
| | `status == PUBLISHED` after `publish()`, reached via 3 ordered transitions with audit log per step | | | | | | | | | O |
| Exception | `LeadTimeViolationException`, `MENU-061`, HTTP 422; `menuRepository.save()` never called | | O | | | | O | | | |
| | `AccessDeniedException`, `MENU-005`, HTTP 403 | | | O | | | | | | |
| | `ValidationException`, HTTP 422 (missing signature) | | | | O | | | | | |
| | `ValidationException`, HTTP 422 (missing reject reason) | | | | | O | | | | |
| | `DataIntegrityViolationException` (CHECK constraint) / service HTTP 422 (missing `approvedBy`) | | | | | | | O | | |
| | `IllegalStateTransitionException`, HTTP 409, status unchanged for every ❌ cell | | | | | | | | O | |
| State verification | No DB row has `submitted_at > menu_date` after full suite run | | | | | | O | | | |
| | `approve()` never sets `PUBLISHED` directly; distinct `submit()`→`approve()`→`publish()` steps | | | | | | | | | O |
| Log message | `MSG-039`: Menu draft submitted for approval successfully. | O | | | | | | | | |
| | `MSG-040`: Menu approved and published successfully. Notifications sent to parents. | | | | O | | | O | | O |
| | `MSG-074`: Menu updated successfully. | | | | | O | | | | |
| | `MSG-005`: Access denied: You do not have permission to access this resource. | | | O | | | | | | |
| | WARN log `"CF-8: FE bỏ qua trạng thái APPROVED"` on `PENDING_APPROVAL→PUBLISHED` (phase-1 allowance) | | | | | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 |
|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | A | A | A | B | A | B | N |
| Passed/Failed | Passed | Passed | Passed | Passed | Passed | Passed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | | | |

> UTCID mapping: UTCID01 = `MF05-TC-001`, UTCID02 = `MF05-TC-002`, UTCID03 = `MF05-TC-007`, UTCID04 = `MF05-TC-008`, UTCID05 = `MF05-TC-009`, UTCID06 = `MF05-TC-016`, UTCID07 = `MF05-TC-019`, UTCID08 = `MF05-TC-021`, UTCID09 = `MF05-TC-022`.
