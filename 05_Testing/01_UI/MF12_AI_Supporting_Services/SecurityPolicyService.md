# Unit Test Case — SecurityPolicyService (MF12 AI & Supporting Services)

> Group (B) — System Administration. Layer `com.sfms.mf12.policy.*`. The `security_policies` table + seed are owned by MF01 (L10/CF-6); MF12 owns the read/update endpoint (UC-184). Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-015`, `MF12-TC-016`.

---

## Function: `SecurityPolicyService.update()` — field range validation

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-017` | | Function Name | `SecurityPolicyService.update` — validation |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 45 | | Lack of test cases | 0 |
| Test requirement | Security-policy fields outside their allowed range must be rejected with `IAM-184` (HTTP 422) and `$.error.details` listing the allowed range per invalid field (D2 envelope); a fully valid update must succeed with MSG-029 and emit `SecurityPolicyUpdated` (UC-184 step 4). (source: MF12-TC-015) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 1 | 3 | 2 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | `FX-011` `SEC-DEFAULT` seed loaded | O | O | O | O | O | O |
| Field under test | `maxLoginAttempts = 2` | O | | | | | |
| | `maxLoginAttempts = 3` (lower bound, valid) | | O | | | | |
| | `maxLoginAttempts = 10` (upper bound, valid) | | | O | | | |
| | `maxLoginAttempts = 20` | | | | O | | |
| | `passwordMinLength = 6` (BR-006 min is 8) | | | | | O | |
| | All 4 fields valid: `5/15/30/8` | | | | | | O |
| **Confirm** | | | | | | | |
| Exception | `$.error.code == "IAM-184"`, HTTP 422, `$.error.details[0].allowed == "3–10"` | O | | | O | | |
| | `$.error.code == "IAM-184"`, HTTP 422 (password length range) | | | | | O | |
| Return | HTTP 200 (valid value accepted) | | O | O | | | O |
| | HTTP 200, message = MSG-029 verbatim ("Profile updated successfully."), `SecurityPolicyUpdated` event emitted | | | | | | O |
| | On any 422, DB is unchanged for ALL fields (reject whole request, no partial write) | O | | | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | A | B | B | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: the allowed range for `sessionTimeoutMinutes`, `passwordMinLength`, and the 2 remaining policy fields is not yet codified in BR-002/004/006 — only `maxLoginAttempts: "3–10"` is stated explicitly in the TestSpec (`UT-12-15`). Confirm the remaining ranges with Huy before hardcoding assertions.

---

## Function: `SecurityPolicyService.update()` — PUT partial merge (PATCH semantics)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-018` | | Function Name | `SecurityPolicyService.update` — partial merge |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | `PUT /security-policies` is a partial merge — an omitted field must keep its previous value (never silently reset to `false`/`null`), while an explicitly-sent `null` for a required boolean is rejected (`TDS §17.1 C10`, L18/CF-16). (source: MF12-TC-016) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 1 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-011`: `requireMixedCase=true`, `noReuseLast3=true` | O | O | O |
| Request body | 7 of 8 fields sent, `requireMixedCase` OMITTED | O | | |
| | `{"requireMixedCase": false}` only (1 field) | | O | |
| | `{"requireMixedCase": null}` (explicit null) | | | O |
| **Confirm** | | | | |
| Return | HTTP 200; `requireMixedCase` STILL `true` (not reset); other 7 fields updated correctly | O | | |
| | HTTP 200; `requireMixedCase = false`; `noReuseLast3` and other fields unchanged | | O | |
| Exception | HTTP 422 — explicit `null` is rejected for a required boolean (distinguishes "not sent" from "sent as null") | | | O |
| Return | `updatedBy`/`updatedAt` derived from JWT + server clock, never trusts client-sent values | O | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: implementation must use `Optional<Boolean>`/`JsonNullable<Boolean>` in the DTO; a plain record field cannot distinguish "field absent" from "field sent as null", which is exactly how bug L18 (password policy silently loosened) arose.
