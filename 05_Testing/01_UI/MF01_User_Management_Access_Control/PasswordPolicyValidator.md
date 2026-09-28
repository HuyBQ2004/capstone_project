# Unit Test Case — PasswordPolicyValidator (MF01 User Management & Access Control)

---

## Function: `PasswordPolicyValidator.validate`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `IAM-FUNC-009` | | Function Name | `PasswordPolicyValidator.validate` |
| Created By | `Nguyen Van Nam` | | Executed By | `Nam` |
| Lines of code | `51` | | Lack of test cases | `0` |
| Test requirement | `Verify the dynamic password complexity policy (BR-006): each of the five criteria (minimum length, uppercase, lowercase, digit, special character) is enforced independently and rejects with IAM-006policy plus the verbatim MSG-104, the minimum length is read from the SecurityPolicy row rather than hardcoded, and disabling a policy flag stops the corresponding criterion from being enforced.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `8` | `0` | `0` | `2` | `5` | `1` | `8` |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | | |
| Security policy | `SEC-DEFAULT` — min length 8, uppercase / lowercase / digit / special all required | O | O | O | O | O | O | O | |
| | `SEC-DEFAULT` with `passwordMinLength` raised to 12 | | | | | | | O | |
| | `SEC-DEFAULT` with one flag turned off at a time (special / number / mixed case) | | | | | | | | O |
| Password input | `P@ssw0rd123` — satisfies every criterion, 11 characters | O | | | | | | O | |
| | `password` — no uppercase, no digit, no special character | | O | | | | | | |
| | `P@ss1` — 5 characters, below the minimum length | | | O | | | | | |
| | `P@SSWORD1` — no lowercase letter | | | | O | | | | |
| | `Password1` — no special character | | | | | O | | | |
| | `P@ssword` — no digit | | | | | | O | | |
| | `Password1` / `P@ssword` / `p@ssw0rd` against the matching disabled flag | | | | | | | | O |
| **Confirm** | | | | | | | | | |
| Return | Validation passes, no exception thrown | O | | | | | | | O |
| | `ApiException` thrown, password refused | | O | O | O | O | O | O | |
| Exception | `IAM-006policy`: Password does not meet the security policy (HTTP 422) | | O | O | O | O | O | O | |
| Log message | `MSG-104`: Password does not meet the security policy. It must contain uppercase, lowercase, a number, and a special character. | | O | O | O | O | O | | |
| | Message is never `MSG-063` (a warning, never a blocking oracle) | | O | O | O | O | O | | |
| State Verification | The same 11-character password passes with min length 8 and fails with min length 12 | | | | | | | O | |
| | Each disabled flag stops only its own criterion from being enforced | | | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 | UTCID08 |
|---|---|---|---|---|---|---|---|---|
| Type (N/A/B) | N | A | B | A | A | A | A | N |
| Passed/Failed | P | P | P | P | P | P | P | P |
| Executed Date | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 | 2026-08-14 |
| Defect ID | | | | | | | | |
