# Unit Test Case — PasswordResetService (MF01 User Management & Access Control)

---

## Function: `PasswordResetService.request` / `reset`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-007` | | Function Name | `PasswordResetService.request` / `reset` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `158` | | Lack of test cases | `0` |
| Test requirement | `Verify the 1-hour reset token window including the exact boundary and single-use invalidation (BR-003), that the reset path still enforces the password policy and the last-3 reuse rule (BR-006 / BR-015), that a successful reset revokes every active session (UC-032), and that the request endpoint is rate limited to 1 email per 15 minutes and leaks nothing for unknown or inactive accounts.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `11` | `0` | `0` | `4` | `3` | `4` | `11` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | |
| Operation | Reset password with token (`reset`) | O | O | O | O | O | O | | | | | |
| | Request reset link (`request`) | | | | | | | O | O | O | O | O |
| Email / user state | Registered ACTIVE user `toanpq@dtd.edu.vn` (U-0001) | O | O | O | O | O | O | O | O | O | | |
| | Email not present in the database | | | | | | | | | | O | |
| | Registered user whose account is `INACTIVE` | | | | | | | | | | | O |
| Token age | 59 minutes, inside the 1-hour window | O | | | O | O | | | | | | |
| | Exactly 60 minutes, the 1-hour boundary | | O | | | | | | | | | |
| | 61 minutes, expired | | | O | | | | | | | | |
| Token state | Token unused | O | O | O | O | O | | | | | | |
| | Token already consumed by a previous reset | | | | | | O | | | | | |
| New password input | Strong and never used before | O | O | O | | O | O | | | | | |
| | Weak password / password inside the last-3 history | | | | O | | | | | | | |
| Request timing | First request at T0 | | | | | | | O | | | | |
| | Second request at T0 + 14'59" (inside the 15-minute window) | | | | | | | | O | | | |
| | Second request at T0 + 15'00" (exactly at the window boundary) | | | | | | | | | O | | |
| **Confirm** | | | | | | | | | | | | |
| Return | Reset succeeds, no exception thrown | O | O | | | O | | | | | | |
| | `ApiException` thrown, password unchanged | | | O | O | | O | | | | | |
| | Request completes with no exception (identical for every email) | | | | | | | O | O | O | O | O |
| Exception | `IAM-008`: Password reset token expired or invalid (HTTP 401) | | | O | | | O | | | | | |
| | `IAM-006policy` for a weak password / `IAM-006reuse` for a reused password (HTTP 422) | | | | O | | | | | | | |
| Log message | `MSG-004`: Session expired. Please log in again. | | | O | | | O | | | | | |
| | `MSG-104` / `MSG-105` respectively | | | | O | | | | | | | |
| State Verification | `password_hash` changed and `used_at` is set | O | O | | | | | | | | | |
| | `password_hash` NOT changed | | | O | O | | O | | | | | |
| | `revokeAll` invoked for the user (UC-032) | | | | | O | | | | | | |
| | 1 email sent and 1 row written to `password_reset_tokens` | | | | | | | O | | O | | |
| | 0 extra email and 0 extra token row (rate limit applied) | | | | | | | | O | | | |
| | 0 email and 0 token row, no exception raised (anti-enumeration) | | | | | | | | | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | B | A | N | A | N | B | B | A | N |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | |
