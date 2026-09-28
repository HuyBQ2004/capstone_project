# Unit Test Case — FeedbackResponseService (MF10 Feedback & Announcement Management)

---

## Function: `FeedbackResponseService.respond()` — blank content rejected, 48h late-response flag (UC-163)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-024` | | Function Name | `FeedbackResponseService.respond` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 26 | | Lack of test cases | 0 |
| Test requirement | Service Caterers reply to parent feedback (BR-143). An empty response must be rejected (UC-163 EX1, error code `FBA-REQ`). A reply submitted more than 48 hours after a negative review must be flagged `lateResponse=true` but NOT blocked (UC-163 EX2: "system logs the delay"), and the flag never applies to non-negative reviews. Source: MF10-TC-023. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 1 | 2 | 2 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | `FX-004` (`FB-8809`, `rating=2`) | O | O | O | O | |
| | Same feedback but `rating=5` | | | | | O |
| Content | `""` / `"   "` | O | | | | |
| | Valid text | | O | O | O | O |
| Elapsed time since `submittedAt` | any | O | | | | |
| | `47h59m` | | O | | | |
| | `48h00m` (closed boundary) | | | O | | |
| | `48h01m` | | | | O | |
| | `72h` | | | | | O |
| **Confirm** | | | | | | |
| Return | Blocked, `FBA-REQ` | O | | | | |
| | Saved, `lateResponse = false` | | O | O | | O |
| | Saved, `lateResponse = true` — recorded, NOT blocked | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | A | N | B | B | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: Blocking a response after 48 hours would leave the caterer unable to reply to old complaints at all, which contradicts UC-163 ("logs the delay", not "forbids"). Applying `lateResponse` to positive reviews would make the "late response" metric meaningless. The 48-hour threshold exists only in UC-163 EX2 with no BR behind it — flagged as open item F5 for Huy to decide whether a BR should be issued; the test still uses the UC as its oracle in the meantime (a UC is a valid oracle per Team Plan §2a).
