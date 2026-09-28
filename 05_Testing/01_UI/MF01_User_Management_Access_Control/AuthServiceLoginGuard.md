# Unit Test Case — AuthService Login Guards (MF01 User Management & Access Control)


---

## Function: `AuthService.login` — account status & school status guards (BR-010)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-001-G` | | Function Name | `AuthService.login` / `AuthService.completeLogin` (guard branch) |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `211` | | Lack of test cases | `0` |
| Test requirement | `Verify that only an ACTIVE and email-verified account can log in (BR-010): PENDING_VERIFICATION, INACTIVE and SUSPENDED are all rejected with IAM-003 without incrementing the failed-login counter and without touching BCrypt (a PENDING account has a NULL password hash). Also verify the school-status gate added on 11/08/2026: login is blocked when the linked school is not ACTIVE, while an unknown school status and a System Admin (no linked school) both pass through.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `13` | `0` | `0` | `5` | `7` | `1` | `13` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | | |
| Account fixture | `FX-U-PENDING` — STF-009, PENDING_VERIFICATION, `emailVerified = false`, `passwordHash = NULL` | O | O | O | O | | O | | | | | | | |
| | `FX-U-SCHOOLADMIN` — U-0001, ACTIVE, verified, `schoolId = SCH-HN-0042` | | | | | O | | O | | | O | O | O | |
| | U-0001 forced to `INACTIVE` (still verified) | | | | | | | O | | | | | | |
| | U-0001 forced to `SUSPENDED` (still verified) | | | | | | | | O | | | | | |
| | `FX-U-SUSPENDED` — U-0007, SUSPENDED, `partnerId = SUP-004`, `schoolId = NULL` | | | | | | | | | O | | | | |
| | `FX-U-SYSADMIN` — U-9001, ACTIVE, verified, `schoolId = NULL` | | | | | | | | | | | | | O |
| Password input | Arbitrary wrong password | O | O | O | O | | | | | | | | | |
| | Correct password `P@ssw0rd123` | | | | | O | O | O | O | O | O | O | O | O |
| Repetition | Login attempted 10 consecutive times | | | O | | | | | | | | | | |
| Linked school status | `refs.schoolStatus(SCH-HN-0042)` = `PENDING` (not ACTIVE) | | | | | | | | | | O | | | |
| | `refs.schoolStatus(SCH-HN-0042)` = `ACTIVE` | | | | | | | | | | | O | | |
| | `refs.schoolStatus(...)` not stubbed, returns `NULL` (school row not found) | | | | | | | | | | | | O | O |
| **Confirm** | | | | | | | | | | | | | | |
| Return | `ApiException` thrown, login rejected | O | O | O | O | | O | O | O | O | O | | | |
| | Login succeeds, no exception thrown | | | | | O | | | | | | O | O | O |
| Exception | `IAM-003` — Account deactivated / unverified (HTTP 403) | O | | | | | O | O | O | O | O | | | |
| | Exception is `ApiException`, never `NullPointerException` | | | | O | | | | | | | | | |
| Log message | `MSG-003`: Your account has been deactivated. Please contact your administrator. | O | | | | | O | O | O | | O | | | |
| State Verification | `refreshTokenService.issueForNewSession` and `jwtService.issueAccessToken` never invoked | | O | | | | | | | O | O | | | |
| | `failedLoginCount` stays 0 and `lockedUntil` stays NULL after 10 attempts | | | O | | | | | | | | | | |
| | `failedLoginCount` stays 0 (status rejection must not trigger lockout) | | | | | | O | O | O | | | | | |
| | `partnerId` preserved as `SUP-004` after the rejection | | | | | | | | | O | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | B | N | A | A | A | A | A | N | N | N |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | | |
