# Unit Test Case — PartnerApprovalService (MF04 Supplier & Partner Management)

---

## Function: `PartnerApprovalService.reject()` — rejection without a reason is blocked (BR-048)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-005` | | Function Name | `PartnerApprovalService.reject()` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 14 | | Lack of test cases | 0 |
| Test requirement | Rejecting a partner without a reason must be blocked with `SUP-048`/MSG-113 (*"A reason is required for this action."*), and the partner status must not change (BR-048). Source: MF04-TC-005, TC-COND-005. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 3 | 0 | 0 | 0 | 3 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| `reject("SUP-005", reason, actorSchoolAdmin)` | `reason = null` | O | | |
| | `reason = ""` | | O | |
| | `reason = "   "` (whitespace only) | | | O |
| **Confirm** | | | | |
| Exception | `ValidationException` `code = "SUP-048"` | O | O | O |
| Return | `partner.status` still `PENDING` (FX-002 baseline) | O | O | O |
| | `eventPublisher.publish(PartnerRejected)` never invoked | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | A |
| Passed/Failed | Passed | Passed | Passed |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: If a partner is allowed to move to `REJECTED` with `reject_reason = null`, the rejection email carries no explanation of what needs to change — and the whole "Update/Resubmit Application" screen (UC-170 Normal Flow step 2) reads that same field to pre-fill the correction guidance for the supplier. A silent-reason rejection leaves the supplier stuck with no way to know what to fix before resubmitting.

---

## Function: `PartnerApprovalService.reject()` — valid rejection records the reason verbatim and notifies (BR-048)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `SUP-FUNC-006` | | Function Name | `PartnerApprovalService.reject()` (happy path) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | A rejection with a valid reason must move the partner to `REJECTED`, store the reason verbatim (Vietnamese diacritics preserved, not truncated), publish exactly one `PartnerRejected` event, and write the same text into `application.comment` so UC-170/UC-158 can redisplay it. Source: MF04-TC-006, BR-048, UC-007 Normal Flow step 8. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 1 | 0 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Request | `reject("SUP-005", "Giấy chứng nhận ATTP đính kèm đã hết hạn — bổ sung bản còn hiệu lực trước khi nộp lại.", actorSchoolAdmin)` | O |
| **Confirm** | | |
| Return | `partner.status == REJECTED` | O |
| | `partner.rejectReason` == the exact input string, diacritics intact | O |
| | Exactly 1 `PartnerRejected` event published, carrying `reason` | O |
| | `application.status == REJECTED` and `application.comment == reason` | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | N |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: If `application.comment` is left unset while only `partner.rejectReason` is written, the resubmission screen (UC-170) shows a dash instead of the actual feedback the supplier needs to act on — the same defect class as `CF-M04-9`, where the application record and the partner record silently drift apart because a single business action only touched one of the two tables it should have.
