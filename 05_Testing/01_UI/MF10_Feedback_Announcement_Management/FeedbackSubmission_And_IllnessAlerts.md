# Unit Test Case — IllnessKeywordDetector / FeedbackService.submit / getDetail (MF10 Feedback & Announcement Management)

---

## Function: `IllnessKeywordDetector.detect()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-005` | | Function Name | `IllnessKeywordDetector.detect` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 25 | | Lack of test cases | 0 |
| Test requirement | The system must automatically scan feedback content for illness-related keywords sourced from the `illness_keywords` table (BR-142), case-insensitive and diacritic-insensitive (ADR-M10-6, `FX-011` — 10 keywords from `mock-data.ts:2716`). Source: MF10-TC-005. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 3 | 3 | 0 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| `comment` | `"Con về kêu đau bụng nhẹ buổi chiều."` (exact match) | O | | | | | |
| | `"be bi NON sau khi an"` (no diacritics + uppercase) | | O | | | | |
| | `"Cháu bị Tiêu Chảy từ tối qua"` (mixed case) | | | O | | | |
| | `"Con ăn hết suất, khen ngon."` (no match) | | | | O | | |
| | `""` / `null` (empty — must not NPE) | | | | | O | |
| | `"món có sốt cà chua"` — trap: `"sốt"` is a keyword but appears inside "sốt cà chua" (sauce), not as a symptom | | | | | | O |
| **Confirm** | | | | | | | |
| Return | `true`, matched keyword returned | O | O | O | | | |
| | `false` | | | | O | O | O |
| Repository call | `repository.findByActiveTrue()` invoked (no hardcoded array in the class) | O | O | O | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | N | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note (UTCID06): This is a business decision without a documented source — BR-142 says nothing about keyword context. The TestSpec flags it with a warning and item **F6** (open question for Huy/Medical Staff: accept the false positive as safer, or filter by context?). If undecided at execution time, mark this case `@Disabled` rather than choosing an answer — the current implementation (`IllnessKeywordDetector.detect()`, plain substring match, no context filter) will actually return `true` here, so this UTCID is expected to conflict with the documented expectation until F6 is resolved.

---

## Function: `FeedbackService.submit()` — server recomputes `illnessKeyword`, ignores client value

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-006` | | Function Name | `FeedbackService.submit` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | `illnessKeyword` is always recomputed by the server; the client-sent value must have zero effect in either direction (CWE-602, BR-142, `review.md` H3 — blocker: "editing the payload turns off the medical alert"). Source: MF10-TC-006. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Request | `submit({comment:"con đau bụng", illnessKeyword:false})` — client tries to suppress the alert | O | | | |
| | `submit({comment:"con ăn ngon", illnessKeyword:true})` — client tries to force a false alert | | O | | |
| | `submit({comment:"con đau bụng"})` — field omitted entirely | | | O | |
| | Request DTO structure | | | | O |
| **Confirm** | | | | | |
| Return | Saved record has `illnessKeyword=true`, `NotificationPort.publish()` called with `type=ALLERGY_ALERT` | O | | O | |
| | Saved record has `illnessKeyword=false`, no medical alert published | | O | | |
| Contract | `SubmitFeedbackRequest` has no public setter for `illnessKeyword` (or it is `@JsonIgnore`ed) — verified via Jackson mapping | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | A | A | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: UTCID01 failing means any caller can silence a food-poisoning alert; UTCID02 failing means Medical Staff gets spammed with false alerts and starts ignoring the channel, including real ones. The mock FE currently does both wrong things at once (`hasIllnessKeyword()` client-side + `feedback/meal/page.tsx:98-111` calls `notify()` itself) — both move entirely to the server; this test does not wait on the FE fix.

---

## Function: `FeedbackService.submit()` — two independent notification branches (MSG-057 / MSG-058)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-007` | | Function Name | `FeedbackService.submit` (notification dispatch) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 22 | | Lack of test cases | 0 |
| Test requirement | `rating<3` triggers MSG-057 to `service-caterers`/`school-admin`; illness keyword detection triggers MSG-058 (`ALLERGY_ALERT`, critical) to `medical-staff`/`school-admin`. The two branches are independent, not `if/else` (BR-140, BR-142, CF-M10-9). Source: MF10-TC-007. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| `rating` | `5` | O | | | |
| | `2` | | O | | |
| | `5` | | | O | |
| | `2` | | | | O |
| Illness keyword present? | No | O | O | | |
| | Yes | | | O | O |
| **Confirm** | | | | | |
| Notification count | 1 — `FeedbackSubmitted` to `service-caterers` only | O | | | |
| | 2 — adds MSG-057 to `["service-caterers","school-admin"]` | | O | | |
| | 2 — adds MSG-058 (`ALLERGY_ALERT`, critical) to `["medical-staff","school-admin"]` | | | O | |
| | 3 — both MSG-057 and MSG-058 | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: UTCID04 is the most dangerous cell — a rating-2 feedback that also contains an illness keyword must alert both the caterer AND Medical Staff. Collapsing the two branches into a single `if/else` silently drops one notification exactly when both parties need to act. MSG-057 is a backend addition not yet reflected on the caterer's mock screen (CF-M10-9).

---

## Function: `FeedbackService.getDetail()` — Medical Staff identity exception + audit (BR-142/BR-145)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-008` | | Function Name | `FeedbackService.getDetail` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 32 | | Lack of test cases | 0 |
| Test requirement | Medical Staff may see submitter identity ONLY when `illnessKeyword=true`, and every such access must generate an audit record (BR-142 "notify Medical Staff for review" — impossible on anonymized data; BR-145 default anonymization; ADR-M10-5 "controlled exception"). Source: MF10-TC-009. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 2 | 3 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Actor | `medical-staff` | O | O | | | |
| | `school-admin` | | | O | | |
| | `service-caterers` | | | | O | |
| | Owner (`FX-017`, the submitting parent) | | | | | O |
| `illnessKeyword` on the record | `true` (`FX-004`) | O | | O | O | any |
| | `false` (`FX-003`) | | O | | | any |
| **Confirm** | | | | | | |
| Return | Full detail with `studentId`/`studentName`, exactly 1 audit record `READ FeedbackIdentity` | O | | | | |
| | Anonymized detail (no exception) | | O | O | O | |
| | Full detail (owner is not subject to the exception rule) | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: The exception must live in the service layer, not the controller, since the controller cannot know `illnessKeyword` before the record is read (avoids Layer Violation AP-AI-004). Opening the exception for every record would let Medical Staff read every child's name off the feedback list, including praise-only feedback; skipping the audit write removes accountability when there is a complaint about who viewed PII.
