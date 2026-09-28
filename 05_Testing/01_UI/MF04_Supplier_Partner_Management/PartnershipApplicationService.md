# Unit Test Case — PartnershipApplicationService (MF04 Supplier & Partner Management)

---

## Function: `PartnershipApplicationService.portalState()` — 3-branch application state (UC-155/159)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-014` | | Function Name | `PartnershipApplicationService.portalState()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 18 | | Lack of test cases | 0 |
| Test requirement | The supplier/caterer portal must resolve to exactly one of three states depending on the latest application: `NO_APPLICATION` (`["SUBMIT"]`), `TRACKING` (`PENDING`/`APPROVED`, `["TRACK"]`), or `CAN_RESUBMIT` (`REJECTED`/`ACTION_REQUIRED`, `["RESUBMIT","TRACK"]`) — matching UC-155/UC-159 Normal Flow step 3 verbatim. `applicationId` is the business code (D1), `null` only when no application exists. Source: MF04-TC-014, TC-COND-012. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 5 | 0 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Existing application | none | O | | | | |
| | `PENDING` | | O | | | |
| | `APPROVED` | | | O | | |
| | `REJECTED` | | | | O | |
| | `ACTION_REQUIRED` | | | | | O |
| **Confirm** | | | | | | |
| `state` | `NO_APPLICATION`, `applicationId == null` | O | | | | |
| | `TRACKING`, `applicationId` = the application's business code | | O | O | | |
| | `CAN_RESUBMIT`, `applicationId` = the application's business code | | | | O | O |
| `availableActions` | `["SUBMIT"]` | O | | | | |
| | `["TRACK"]` | | O | O | | |
| | `["RESUBMIT","TRACK"]` | | | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | N | N | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: If `APPROVED` is grouped under `CAN_RESUBMIT` (an easy copy-paste mistake with `REJECTED`), a supplier that already has a live partnership would see a "Resubmit" button and could overwrite the active application record that MF06 relies on to authorize deliveries — corrupting an in-force relationship instead of simply tracking its status.

---

## Function: `SubmitApplicationRequest` — tax ID format validation (MSG-007 ⚠️ outside §1.4)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-016` | | Function Name | `SubmitApplicationRequest` (Jakarta Bean Validation) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 12 | | Lack of test cases | 0 |
| Test requirement | `taxId` must match Vietnam's tax ID pattern `^\d{10}(-\d{3})?$` (10 digits, optionally followed by a 3-digit branch suffix). A violation raises `SUP-007`/MSG-007 ⚠️ *(content confirmed by `error_message.md`, not yet linked to an MF04 UC in traceability §1.4 — pending Huy's decision, CF-M04-2)*, HTTP 422. Fields other than `taxId` fall under the generic `SUP-138`/MSG-138 instead. Source: MF04-TC-016, TC-COND-014, UC-152 Normal Flow step 5. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 2 | 4 | 0 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| `taxId` | `"0109988776"` (10 digits) | O | | | | | |
| | `"0109988776-001"` (10 + 3-digit branch) | | O | | | | |
| | `"010998877"` (9 digits) | | | O | | | |
| | `"01099887766"` (11 digits) | | | | O | | |
| | `"0109988776-01"` (2-digit branch suffix) | | | | | O | |
| | `"ABC0109988"` (non-numeric) | | | | | | O |
| **Confirm** | | | | | | | |
| Return | No violation | O | O | | | | |
| Exception | `ConstraintViolation`; API → `422 SUP-007` | | | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | B | B | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: A garbage tax ID that passes validation becomes permanently unverifiable against the tax authority — the very document a food-safety inspector or auditor would first ask to cross-check. Because MSG-007 is not yet formally linked to an MF04 use case in the traceability matrix (CF-M04-2), this test intentionally asserts on the `error.code` and the exact message text sourced from `error_message.md`, not on an assumed UC/BR pairing.
