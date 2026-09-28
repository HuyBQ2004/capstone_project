# Unit Test Case — AuthService Session (MF01 User Management & Access Control)


---

## Function: `AuthService.login` (session issue) / `AuthService.logout`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-001-S` | | Function Name | `AuthService.login` (session issue) / `AuthService.logout` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `211` | | Lack of test cases | `0` |
| Test requirement | `Verify single-session enforcement (BR-005): every successful login delegates to issueForNewSession exactly once — which revokes the previous session and inserts the new one atomically — and this happens BEFORE the LOGIN SUCCESS audit row is written. Verify that logout revokes every session of the user, writes an audit row (UC-035), and is idempotent on a second call.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `4` | `0` | `0` | `2` | `0` | `2` | `4` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | User `U-0001` ACTIVE, email verified, correct password supplied | O | O | | |
| | User `U-0001` has an active session (already logged in) | O | | O | O |
| Operation | Second `login` from another device | O | O | | |
| | `logout` called once | | | O | |
| | `logout` called twice with the same session | | | | O |
| **Confirm** | | | | | |
| Return | New `LoginResult` issued | O | O | | |
| | Logout completes, no exception thrown | | | O | O |
| State Verification | `refreshTokenService.issueForNewSession` invoked exactly once (revoke + insert are inside it) | O | | | |
| | Session issue happens BEFORE the `LOGIN` / `SUCCESS` audit row is written | | O | | |
| | `revokeAllForUser` invoked and a `LOGOUT` audit row written | | | O | |
| | Second logout raises no exception and produces no duplicate revoke | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | B | N | B |
| Passed/Failed | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | |
