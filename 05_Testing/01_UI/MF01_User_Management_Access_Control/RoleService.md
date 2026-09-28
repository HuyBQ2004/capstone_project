# Unit Test Case — RoleService (MF01 User Management & Access Control)

---

## Function: `RoleService.delete` / `RoleService.update`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-004` | | Function Name | `RoleService.delete` / `RoleService.update` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `181` | | Lack of test cases | `0` |
| Test requirement | `Verify role guards (BR-008, UC-098): none of the 7 default system roles can be deleted or renamed, a custom role with assigned users cannot be deleted and MSG-106 renders the real assigned count including INACTIVE users, a custom role becomes deletable once its users are reassigned, and role names stay unique.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `15` | `0` | `0` | `4` | `10` | `1` | `15` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 | UTCID14 | UTCID15 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | | | | |
| Target role | `ROLE-01` System Admin (`system = true`) | O | | | | | | | | | | | | | | |
| | `ROLE-02` School Administration (`system = true`) | | O | | | | | | | | | | | O | | O |
| | `ROLE-03` Medical Staff (`system = true`) | | | O | | | | | | | | | | | | |
| | `ROLE-04` Service Caterers (`system = true`) | | | | O | | | | | | | | | | | |
| | `ROLE-05` Teacher (`system = true`) | | | | | O | | | | | | | | | | |
| | `ROLE-06` Parent (`system = true`) | | | | | | O | | | | | | | | | |
| | `ROLE-07` Raw Food Supplier (`system = true`) | | | | | | | O | | | | | | | | |
| | `ROLE-08` Kitchen Auditor (custom, 0 assigned user) | | | | | | | | O | | | | | | O | |
| | `ROLE-09` Ad-hoc Auditor (custom, 3 assigned users) | | | | | | | | | O | O | O | | | | |
| Assigned users | 3 users assigned: 2 ACTIVE and 1 INACTIVE | | | | | | | | | O | | | | | | |
| | All 3 assigned users are INACTIVE | | | | | | | | | | O | | | | | |
| | All 3 users reassigned to `ROLE-05` before the call | | | | | | | | | | | O | | | | |
| Operation | Delete role (`delete`) | O | O | O | O | O | O | O | O | O | O | O | | | | |
| | Update role name to `BGH` | | | | | | | | | | | | O | | | |
| | Update role name to `Teacher` (already used by `ROLE-05`) | | | | | | | | | | | | | | O | |
| | Update permissions list (`PERM-*`) | | | | | | | | | | | | | O | | |
| | Update description only, name unchanged | | | | | | | | | | | | | | | O |
| **Confirm** | | | | | | | | | | | | | | | | |
| Return | Operation succeeds | | | | | | | | O | | | O | | O | | O |
| | `ApiException` thrown, operation refused | O | O | O | O | O | O | O | | O | O | | O | | O | |
| Exception | `IAM-004`: Access denied / system role protected (HTTP 403) | O | O | O | O | O | O | O | | | | | O | | | |
| | `IAM-009`: Cannot delete role with assigned users (HTTP 409) | | | | | | | | | O | O | | | | | |
| | HTTP 409 Conflict on the duplicate role name | | | | | | | | | | | | | | O | |
| Log message | `MSG-005`: Access denied: You do not have permission to access this resource. | O | O | O | O | O | O | O | | | | | O | | | |
| | `MSG-106` rendered with `[Number]` = 3 | | | | | | | | | O | O | | | | | |
| State Verification | Role row removed from DB and a `DELETE` audit row written | | | | | | | | O | | | O | | | | |
| | Row count of `roles` unchanged (nothing deleted) | O | O | O | O | O | O | O | | O | O | | | | | |
| | Role name unchanged after the refused update | | | | | | | | | | | | O | | | |
| | Permissions persisted exactly as the submitted `PERM-*` list | | | | | | | | | | | | | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 | UTCID14 | UTCID15 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | A | A | A | A | N | A | B | N | A | N | A | N |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | | | | |
