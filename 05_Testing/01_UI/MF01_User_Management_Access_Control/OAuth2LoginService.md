# Unit Test Case — OAuth2LoginService (MF01 User Management & Access Control)

---

## Function: `OAuth2LoginService.linkAndLogin`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-005` | | Function Name | `OAuth2LoginService.linkAndLogin` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `188` | | Lack of test cases | `0` |
| Test requirement | `Verify Google sign-in is link-only with no auto-provisioning (BR-218): an unknown email is refused with IAM-102 and writes nothing to the database, a first-time link stores googleSub and publishes GoogleIdentityLinked exactly once, an existing googleSub is never overwritten, the password rules are skipped for a Google session, and the account status and lockout guards still apply (BR-002, BR-010).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `14` | `0` | `0` | `8` | `5` | `1` | `14` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 | UTCID14 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | | | | | | | |
| Account existence | Email absent from the database (`new.person@gmail.com`) | O | O | O | | | | | | | | | | | |
| | Email present, `googleSub = NULL`, status ACTIVE (first-time link) | | | | O | O | O | O | | | O | O | | | |
| | Email present, `googleSub = google-sub-0009` (U-0009, already linked) | | | | | | | | O | O | | | | | |
| | Email present but the account is `INACTIVE` | | | | | | | | | | | | O | | |
| | Email present but the account is currently locked out | | | | | | | | | | | | | O | |
| | Email present, `emailVerified = false` | | | | | | | | | | | | | | O |
| Google identity | Google `sub` matches the stored `googleSub` | | | | | | | | O | | | | | | |
| | Google `sub` differs from the stored `googleSub` | | | | | | | | | O | | | | | |
| Password state | `passwordHash = NULL` (Google-only user) | | | | | | | | | | O | O | | | |
| **Confirm** | | | | | | | | | | | | | | | |
| Return | `LoginResult` with access and refresh token issued | | | | O | O | O | O | O | | O | O | | | |
| | `ApiException` thrown, no session created | O | O | O | | | | | | O | | | O | O | O |
| | `role` field equals `school-admin` (kebab-case RoleKey, D6) | | | | O | | | | | | | | | | |
| Exception | `IAM-102`: Google account not linked to any SFMS account (HTTP 403) | O | O | O | | | | | | | | | | | |
| | `IAM-003`: Account deactivated / unverified (HTTP 403) | | | | | | | | | | | | O | | |
| | `IAM-007`: Account temporarily locked (HTTP 423) | | | | | | | | | | | | | O | |
| | `IAM-103`: Google authentication failed (HTTP 401) | | | | | | | | | O | | | | | O |
| Log message | `MSG-102`: Google account not linked to any SFMS account. | O | | | | | | | | | | | | | |
| DB Verification | `userRepo.save(...)` never invoked — no auto-provisioning | | O | | | | | | | | | | | | |
| | `googleSub` written with the Google identifier | | | | O | | | | | | | | | | |
| | Existing `googleSub` preserved, not overwritten | | | | | | | | O | O | | | | | |
| State Verification | No access or refresh token issued; 1 audit row with `result = BLOCKED` | | | O | | | | | | | | | | | |
| | `issueForNewSession` invoked (BR-005 revoke of the previous session) | | | | | O | | | | | | | | | |
| | `GoogleIdentityLinked` event published exactly once | | | | | | O | | | | | | | | |
| | No link event published on a repeat login | | | | | | | | O | | | | | | |
| | Audit row `LOGIN_GOOGLE` with `result = SUCCESS` | | | | | | | O | | | | | | | |
| | `passwordEncoder`, `passwordHistory` and `passwordPolicyValidator` never touched | | | | | | | | | | | O | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 | UTCID09 | UTCID10 | UTCID11 | UTCID12 | UTCID13 | UTCID14 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | A | A | N | N | N | N | N | B | N | N | A | A | A |
| Passed/Failed | P | P | P | P | P | P | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | | | | | | | |
