# Unit Test Case — RefreshTokenService (MF01 User Management & Access Control)

---

## Function: `RefreshTokenService.rotate` / `issueForNewSession`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-003` | | Function Name | `RefreshTokenService.rotate` / `issueForNewSession` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `146` | | Lack of test cases | `0` |
| Test requirement | `Verify refresh token rotation with sliding expiration of 14 days (BR-004): a token is accepted up to the exact expiry second and rejected one second later, the old record is revoked before the new one is written, only the SHA-256 hash is persisted, an already revoked token is refused, the account status and lockout guards also apply on the refresh path (BR-002, BR-010), and issueForNewSession revokes every previous session before inserting the new one (BR-005).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `12` | `0` | `0` | `5` | `4` | `3` | `12` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | |
| Operation | Rotate an existing refresh token (`rotate`) | O | O | O | O | O | O | O | O | O | O | | |
| | Issue a session at login (`issueForNewSession`) | | | | | | | | | | | O | O |
| Owner account | `U-0001` ACTIVE, email verified, not locked | O | O | O | O | O | O | O | | | O | O | O |
| | Account switched to `INACTIVE` in the middle of the session | | | | | | | | O | | | | |
| | Account currently locked out (`lockedUntil > now`) | | | | | | | | | O | | | |
| Token state | Present in DB with `revoked_at IS NULL` | O | O | O | O | O | O | | O | O | O | | |
| | Already revoked (`revoked_at IS NOT NULL`) | | | | | | | O | | | | | |
| Token expiration | 1 day left before `expires_at` | O | | | O | O | O | | | | | | |
| | 1 second before `expires_at` | | O | | | | | | | | | | |
| | 1 second past `expires_at` | | | O | | | | | | | O | | |
| **Confirm** | | | | | | | | | | | | | |
| Return | New token pair issued, no exception thrown | O | O | | O | O | O | | | | | O | O |
| | `ApiException` thrown, no token pair issued | | | O | | | | O | O | O | O | | |
| Exception | `IAM-008`: Session expired / invalid token (HTTP 401) | | | O | | | | O | O | O | O | | |
| Log message | `MSG-004`: Session expired. Please log in again. | | | O | | | | O | O | O | O | | |
| State Verification | New `expires_at` = rotation time + 14 days | O | | | | | | | | | | | |
| | Old record has `revoked_at` set after the rotation | | | | O | | | | | | | | |
| | The revoke of the old record happens before the new record is saved | | | | | O | | | | | | | |
| | `token_hash` differs from the returned token; no column holds the raw token | | | | | | O | | | | | | |
| | Every session of the user revoked as well | | | | | | | | O | | | | |
| | No new access token issued when the rotation fails | | | | | | | | | | O | | |
| | `revokeAllForUser` invoked before the new token is inserted (BR-005) | | | | | | | | | | | O | |
| | New `expires_at` = now + 14 days; raw token returned, hash stored | | | | | | | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | N | N | N | A | A | A | A | N | B |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | |
