# Unit Test Case — AccountService (MF01 User Management & Access Control)


---

## Function: `AccountService.changePassword`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-002` | | Function Name | `AccountService.changePassword` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `96` | | Lack of test cases | `0` |
| Test requirement | `Verify password storage and reuse rules: the new password is stored as a BCrypt hash with cost 12 and the raw string is never persisted (BR-001); a wrong current password is rejected with IAM-002; the last 3 passwords cannot be reused (BR-006 / BR-015) while the 4th-oldest can, proving the window is exactly 3; the noReuseLast3 policy flag is honoured; and a successful change revokes every other active session (UC-011).` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `8` | `0` | `0` | `4` | `2` | `2` | `8` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| Precondition | User `U-0001` exists and is authenticated | O | O | O | O | O | O | O | O |
| | Security policy `SEC-DEFAULT` loaded from DB (min length 8, all flags true) | O | O | O | O | O | O | | O |
| | `password_history` holds the 3 most recent password hashes | | | | O | O | O | O | O |
| Current password | Matches the stored BCrypt hash | O | O | | O | O | O | O | O |
| | Does NOT match the stored hash | | | O | | | | | |
| New password | Strong and never used before (`N3w@Pass456`) | O | O | | | O | | | O |
| | Equal to the 2nd entry of `password_history` (inside the 3-window) | | | | O | | | O | |
| | Equal to the 4th-oldest password (outside the 3-window) | | | | | | O | | |
| Policy flag | `noReuseLast3 = true` (default) | O | O | O | O | O | O | | O |
| | `noReuseLast3 = false` while the new password IS in history | | | | | | | O | |
| **Confirm** | | | | | | | | | |
| Return | Change succeeds (`void`) | O | O | | | O | O | O | O |
| | `ApiException` thrown, password not changed | | | O | O | | | | |
| Exception | `IAM-002` — Invalid credentials (HTTP 401) | | | O | | | | | |
| | `IAM-006reuse` — Cannot reuse any of the last 3 passwords (HTTP 422) | | | | O | | | | |
| Log message | `MSG-002`: Invalid credentials. Please check your email and password. | | | O | | | | | |
| | `MSG-105`: You cannot reuse any of your last 3 passwords. Please choose a different one. | | | | O | | | | |
| State Verification | Stored hash matches `^\$2[aby]\$\d{2}\$.{53}$` and the cost factor is `12` | O | | | | | | | |
| | The raw password string appears in NO persisted column | | O | | | | | | |
| | `save()` NOT invoked when the reuse rule rejects the change | | | | O | | | | |
| | New hash appended to `password_history` | | | | | O | | | |
| | `revokeAllOtherSessions` invoked after a successful change (UC-011) | | | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A | N | B | B | N |
| Passed/Failed | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | |
