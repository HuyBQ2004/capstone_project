# Unit Test Case — AuthService Lockout (MF01 User Management & Access Control)


---

## Function: `AuthService.login` — failed-attempt lockout (BR-002)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-001-L` | | Function Name | `AuthService.login` (lockout branch) |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `211` | | Lack of test cases | `0` |
| Test requirement | `Verify the account lockout rule (BR-002): the failed-login counter increments on every wrong password, the account locks when the counter reaches SecurityPolicy.maxLoginAttempts, the lockout lasts SecurityPolicy.lockoutMinutes and releases exactly on the boundary, MSG-076 renders the minutes value from the DB policy (never hardcoded), a lockout revokes the refresh token, and the block is audited.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `9` | `0` | `0` | `1` | `4` | `4` | `9` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 |
|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | |
| Precondition | User `U-0001` ACTIVE, email verified, `failedLoginCount = 0` | O | O | O | O | O | | | | |
| | User `U-0001` already locked, `lockedUntil = 2026-01-10T02:15:00Z` | | | | | | O | O | O | |
| Security policy | `FX-POLICY` — SEC-DEFAULT: 5 attempts / 15 min lockout | O | O | O | O | O | O | O | O | |
| | `FX-POLICY-ALT` — 3 attempts / 20 min lockout | | | | | | | | | O |
| Password input | Wrong password, attempts 1 to 4 | O | | | | | | | | |
| | Wrong password, attempt 5 (equals `maxLoginAttempts`) | | O | | O | O | | | | |
| | Wrong password, attempt 3 (equals ALT `maxLoginAttempts`) | | | | | | | | | O |
| | Correct password, attempt 6 (while still locked) | | | O | | | | | | |
| | Correct password | | | | | | O | O | O | |
| Clock at login | `02:14:59Z` — 1 s before the lockout expires | | | | | | O | | | |
| | `02:15:00Z` — exactly at the lockout expiry boundary | | | | | | | O | | |
| | `02:15:01Z` — 1 s after the lockout expires | | | | | | | | O | |
| **Confirm** | | | | | | | | | | |
| Return | `ApiException` — no `LoginResult`, no token issued | O | O | O | O | O | O | | | O |
| | `LoginResult` issued successfully | | | | | | | O | O | |
| Exception | `IAM-002` — Invalid credentials (HTTP 401) | O | | | | | | | | |
| | `IAM-007` — Account temporarily locked (HTTP 423) | | O | O | O | O | O | | | O |
| Log message | `MSG-002`: Invalid credentials. Please check your email and password. | O | | | | | | | | |
| | `MSG-076` rendered with `15` minutes (value read from DB policy) | | O | O | | | O | | | |
| | `MSG-076` rendered with `20` minutes (proves the value is not hardcoded) | | | | | | | | | O |
| State Verification | `failedLoginCount` = 1, 2, 3, 4 respectively; `lockedUntil` stays NULL | O | | | | | | | | |
| | `failedLoginCount` = 5 and `lockedUntil` = now + 15 min | | O | | | | | | | |
| | Counter NOT reset by the correct password while locked | | | O | | | | | | |
| | `failedLoginCount` reset to 0 and `lockedUntil` cleared to NULL | | | | | | | O | O | |
| | `audit_logs` row `LOGIN` with `result = BLOCKED` and the client IP | | | | O | | | | | |
| | `refreshTokenService.revokeAllForUser` invoked on lockout (BR-004) | | | | | O | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 |
|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | B | A | A | A | B | B | B | N |
| Passed/Failed | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | |
