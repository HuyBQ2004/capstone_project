# Unit Test Case — AccountActivationService (MF01 User Management & Access Control)

---

## Function: `AccountActivationService.activate` / `resend`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-006` | | Function Name | `AccountActivationService.activate` / `resend` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `154` | | Lack of test cases | `0` |
| Test requirement | `Verify the 48-hour activation window including the exact expiry boundary (BR-011), the PENDING_VERIFICATION to ACTIVE transition with email_verified = true and a BCrypt password hash (BR-010, UC-154), single-use token invalidation, hashed token storage, and the resend flow which invalidates the old token and issues a new one valid for another 48 hours while staying silent for unknown or already-active accounts.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `12` | `0` | `0` | `5` | `4` | `3` | `12` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | |
| Target user | `FX-U-PENDING` STF-009 (lanht@dtd.edu.vn), PENDING_VERIFICATION, `passwordHash = NULL` | O | O | O | O | O | O | | O | O | O | O | |
| | Account already `ACTIVE` (`emailVerified = true`) | | | | | | | O | | | | | O |
| | Email not present in the database | | | | | | | | | | | | O |
| Token age | `expires_at` = T0 + 1 min, i.e. 47h59' old, inside the window | O | | | O | O | | | | | | | |
| | `expires_at` = T0 exactly, the 48h boundary | | O | | | | | | | | | | |
| | `expires_at` = T0 - 60 s, i.e. 48h01' old, expired | | | O | | | | | | | | | |
| Token state | Token unused (`used_at IS NULL`) | O | O | O | O | O | | | O | | | | |
| | Token already consumed by a previous activation | | | | | | O | | | | | | |
| Password input | Strong password `P@ssw0rd123` | O | O | O | O | | O | O | | | | | |
| | Weak password `password` (violates BR-006) | | | | | O | | | | | | | |
| Operation | Activate account (`activate`) | O | O | O | O | O | O | O | O | | | | |
| | Resend activation link (`resend`) | | | | | | | | | O | O | O | O |
| **Confirm** | | | | | | | | | | | | | |
| Return | Activation succeeds, no exception thrown | O | O | | O | | | | | | | | |
| | `ApiException` thrown, activation refused | | | O | | O | O | O | | | | | |
| | Resend completes silently, nothing is sent | | | | | | | | | | | | O |
| Exception | `IAM-008`: Activation token expired or invalid (HTTP 401) | | | O | | | O | O | | | | | |
| | `IAM-006policy`: Password does not meet the security policy (HTTP 422) | | | | | O | | | | | | | |
| Log message | `MSG-004`: Session expired. Please log in again. | | | O | | | | | | | | | |
| | `MSG-104`: Password does not meet the security policy. | | | | | O | | | | | | | |
| State Verification | `status` = ACTIVE, `email_verified` = true, `password_hash` starts with `$2a$12$`, `used_at` = T0 | | | | O | | | | | | | | |
| | `status` stays PENDING_VERIFICATION and `password_hash` stays NULL | | | O | | | | | | | | | |
| | `password_hash` unchanged after the replay attempt | | | | | | O | | | | | | |
| | Token looked up by `sha256(input)`; no column stores the raw token | | | | | | | | O | | | | |
| | New token has `expires_at = T0 + 48h` and `used_at = NULL` | | | | | | | | | O | | | |
| | `invalidateAllForUser` invoked before the new token is issued | | | | | | | | | | O | | |
| | Exactly 1 email sent to `lanht@dtd.edu.vn` | | | | | | | | | | | O | |
| | 0 emails sent and no token created (anti-enumeration) | | | | | | | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | B | N | A | A | A | N | N | N | N | A |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | |
