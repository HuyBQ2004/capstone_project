# Unit Test Case — AuthService (MF01 User Management & Access Control)


---

## Function: `AuthService.login` / `AuthService.acceptTerms`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-001` | | Function Name | `AuthService.login` / `AuthService.acceptTerms` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `211` | | Lack of test cases | `0` |
| Test requirement | `Verify the terms-of-service acceptance flag on the login result (BR-009): a user who has never accepted the terms is still issued tokens but flagged needTermsAccept = true; acceptTerms is idempotent and stores an ISO-8601 instant (D4); the role returned in the login result is the kebab-case RoleKey (D6).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `5` | `0` | `0` | `4` | `0` | `1` | `5` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | User `U-0001` exists, status = ACTIVE, email verified | O | O | O | O | O |
| | Correct password `P@ssw0rd123` supplied | O | O | O | O | O |
| `termsAcceptedAt` | `NULL` (user has never accepted the terms) | O | | O | O | |
| | `2026-01-10T02:00:00Z` (already accepted) | | O | | | O |
| Operation | `login` only | O | O | | | O |
| | `acceptTerms` then `login` again | | | O | | |
| | `acceptTerms` called twice in a row | | | | O | |
| **Confirm** | | | | | | |
| Return | `LoginResult` issued, `needTermsAccept = true` | O | | | | |
| | `LoginResult` issued, `needTermsAccept = false` | | O | O | | |
| | `role` field equals `school-admin` (kebab-case RoleKey, D6) | | | | | O |
| State Verification | Access token AND refresh token still issued despite pending terms | O | | | | |
| | `termsAcceptedAt` is a non-null `Instant` after acceptance | | | O | | |
| | Second `acceptTerms` call does NOT move the stored timestamp | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | B | N |
| Passed/Failed | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | |
