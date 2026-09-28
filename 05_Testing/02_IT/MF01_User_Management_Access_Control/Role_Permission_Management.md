# Integration Test Case — Role & Permission Management (MF01 User Management & Access Control)

---

## Feature: `Role & Permission Management`

### 1. General Information

| Field | Value |
|---|---|
| Feature | Role & Permission Management |
| Test requirement | Verify the role endpoints end to end: default system roles cannot be deleted, a custom role with assigned users is refused with a rendered count, an empty custom role is deletable and audited, path parameters are business codes rather than UUIDs, only the System Admin may manage roles, and the `users` count is derived at runtime with no such column in the `roles` table. |
| Number of TCs | 11 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 11 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 11 | 0 |
| Round 3 | 0 | 0 | 11 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **DELETE /api/v1/roles/{code}** | | | | | | | | | | | | | | |
| MF01-TC-INT-012a | Test that a default system role cannot be deleted. | 1. Log in as System Admin.<br>2. Send DELETE to `/api/v1/roles/ROLE-01`.<br>3. Count the rows in `roles` where `system = true`. | 1. HTTP 403 Forbidden with `error.code = IAM-004` and `error.message` = MSG-005.<br>2. All 7 system roles are still present. | An authenticated System Admin session; the 7 default roles seeded. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.deleteSystemRoleBlocked` |
| MF01-TC-INT-012b | Test that a custom role with assigned users cannot be deleted and the count is rendered. | 1. Log in as System Admin.<br>2. Send DELETE to `/api/v1/roles/ROLE-09`, a custom role holding 3 users of whom 1 is INACTIVE. | 1. HTTP 409 Conflict with `error.code = IAM-009`.<br>2. `error.message` is MSG-106 rendered with the number 3, counting the INACTIVE user as well.<br>3. The role row is not deleted. | `ROLE-09` seeded with 3 assigned users. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.deleteRoleWithUsersReturns409` |
| MF01-TC-INT-012c | Test that a custom role with no assigned users is deleted and audited. | 1. Log in as System Admin.<br>2. Send DELETE to `/api/v1/roles/ROLE-08`, a custom role with 0 assigned users.<br>3. Query `roles` and `audit_logs`. | 1. HTTP 200 OK.<br>2. The `ROLE-08` row is removed from the database.<br>3. One new audit row records the deletion. | `ROLE-08` seeded with 0 assigned users. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.deleteEmptyCustomRole` |
| MF01-TC-INT-012d | Test the response when deleting a role that no longer exists. | 1. Delete `ROLE-08` successfully.<br>2. Send DELETE to `/api/v1/roles/ROLE-08` a second time. | 1. HTTP 404 Not Found.<br>2. `error.message` = MSG-137. | `ROLE-08` already deleted in the same session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.deleteTwiceReturns404` |
| MF01-TC-INT-012e | Test that a School Admin cannot delete a role. | 1. Log in as School Admin `U-0001`.<br>2. Send DELETE to `/api/v1/roles/ROLE-09`. | 1. HTTP 403 Forbidden.<br>2. Only the System Admin manages roles per the authorization matrix. | An authenticated School Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.schoolAdminCannotDeleteRole` |
| MF01-TC-INT-012f | Test that the path parameter is the business code and not a UUID. | 1. Log in as System Admin.<br>2. Send DELETE to `/api/v1/roles/ROLE-01` and observe that the code is resolved.<br>3. Send the same request using the internal UUID of the role. | 1. The business code `ROLE-01` is accepted and routed to the correct role.<br>2. The UUID form is not a valid public identifier. | An authenticated System Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.pathParamIsCodeNotUuid` |
| **GET /api/v1/roles** | | | | | | | | | | | | | | |
| MF01-TC-INT-013a | Test the role list response shape. | 1. Log in as System Admin.<br>2. Send GET to `/api/v1/roles`.<br>3. Inspect the envelope and one item. | 1. HTTP 200 OK with the 5 pagination keys `items`, `total`, `page`, `pageSize`, `pageCount`.<br>2. Each item exposes `id`, `name`, `description`, `system`, `users` and `permissions`. | An authenticated System Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.rolesResponseShape` |
| MF01-TC-INT-013b | Test that the `users` count is derived at runtime. | 1. Send GET to `/api/v1/roles` and note the `users` value for `ROLE-05` (Teacher).<br>2. Create a new user with the `teacher` role.<br>3. Send GET to `/api/v1/roles` again. | 1. The `users` value for `ROLE-05` increases by exactly 1.<br>2. The value is computed by a SQL count at request time. | An authenticated System Admin session. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.usersFieldIsRuntimeDerived` |
| MF01-TC-INT-013c | Test that deactivating a user does not reduce the role user count. | 1. Note the `users` value of a role.<br>2. Deactivate one of the users assigned to it.<br>3. Send GET to `/api/v1/roles` again. | 1. The `users` value is unchanged.<br>2. BR-008 counts INACTIVE users as still assigned. | An authenticated System Admin session and a role with assigned users. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.inactiveUserStillCountedInRoleUsers` |
| MF01-TC-INT-013d | Test that the `roles` table has no persisted `users` column. | 1. Query `information_schema.columns` for the `roles` table. | 1. No column named `users` exists.<br>2. The value in the API response is purely derived. | Flyway migration applied. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.databaseHasNoUsersColumn` |
| MF01-TC-INT-013e | Test the seeded assignment count of the System Admin role. | 1. Log in as System Admin.<br>2. Send GET to `/api/v1/roles`.<br>3. Read the `users` value of `ROLE-01`. | 1. `ROLE-01` reports exactly 2 assigned users, matching the seeded `U-9000` and `U-9001`. | Seed data with both System Admin accounts loaded. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `RoleControllerIT.systemAdminRoleHasTwoAssignedUsers` |
