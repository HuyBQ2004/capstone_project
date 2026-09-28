# Integration Test Case — Staff Management (MF01 User Management & Access Control)

---

## Feature: `Staff Management & School Scoping`

### 1. General Information

| Field | Value |
|---|---|
| Feature | Staff Management & School Scoping |
| Test requirement | Verify that staff creation is reserved to the School Administration role acting inside its own school (BR-013, UC-024): the school is taken from the JWT and never from the request body, only school-level roles may be assigned, and System Admin and Teacher are both refused on this endpoint per the authorization matrix. |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 6 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/staff** | | | | | | | | | | | | | | |
| MF01-TC-INT-010a | Test that a School Admin can create a teacher inside their own school. | 1. Log in as the School Admin of school A (`SCH-HN-0042`).<br>2. Send POST to `/api/v1/staff` with `{"name":"Staff A","email":"staff.a@dtd.edu.vn","phone":"0912345699","role":"teacher"}`.<br>3. Read the created row from the database. | 1. HTTP 201 Created.<br>2. The new staff row carries `school_id` = `SCH-HN-0042`, taken from the JWT claim.<br>3. The account is created in the pending state awaiting activation. | An authenticated School Admin session for school A. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `StaffCreateIT.schoolAdminCreatesTeacherOwnSchool` |
| MF01-TC-INT-010b | Test that a `schoolId` supplied in the body cannot move the staff to another school. | 1. Log in as the School Admin of school A.<br>2. Send POST to `/api/v1/staff` with a body containing `"schoolId":"SCH-HN-0051"` (school B).<br>3. Read the created row, if any. | 1. The request is either refused with HTTP 403 or accepted with the body value ignored.<br>2. In the accepted case the row still belongs to `SCH-HN-0042`.<br>3. No staff row is ever created under school B. | An authenticated School Admin session for school A; school B exists in the seed data. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `StaffCreateIT.schoolIdInBodyIsIgnoredOrRejected` |
| MF01-TC-INT-010c | Test that a School Admin cannot create a `system-admin` through the staff endpoint. | 1. Log in as the School Admin of school A.<br>2. Send POST to `/api/v1/staff` with `"role":"system-admin"`. | 1. HTTP 403 Forbidden with `error.code = IAM-004`.<br>2. No staff row is created. | An authenticated School Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `StaffCreateIT.systemAdminRoleRejectedOnStaff` |
| MF01-TC-INT-010d | Test that non-school-level roles are refused on the staff endpoint. | 1. Log in as the School Admin of school A.<br>2. Send POST to `/api/v1/staff` once for each of `parent`, `supplier` and `service-caterers`. | 1. Every request returns HTTP 403 Forbidden with `error.code = IAM-004`.<br>2. Only school-level roles are accepted here per UC-024 step 4. | An authenticated School Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `StaffCreateIT.nonSchoolRolesRejected` |
| MF01-TC-INT-010e | Test that a System Admin cannot use the staff endpoint. | 1. Log in as System Admin `U-9001`.<br>2. Send POST to `/api/v1/staff` with a valid teacher payload. | 1. HTTP 403 Forbidden with `error.code = IAM-004`.<br>2. The System Admin must use `POST /api/v1/users` instead, per the authorization matrix. | An authenticated System Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `StaffCreateIT.sysAdminCannotUseStaffEndpoint` |
| MF01-TC-INT-010f | Test that a Teacher cannot create staff. | 1. Log in as teacher `U-0004`.<br>2. Send POST to `/api/v1/staff` with a valid payload. | 1. HTTP 403 Forbidden with `error.code = IAM-004` and `error.message` = MSG-005.<br>2. No staff row is created. | An authenticated Teacher session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `StaffCreateIT.teacherCannotCreateStaff` |
