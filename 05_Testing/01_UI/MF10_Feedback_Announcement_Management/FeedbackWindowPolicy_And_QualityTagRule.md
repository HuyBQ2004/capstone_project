# Unit Test Case — FeedbackWindowPolicy / QualityTagRule (MF10 Feedback & Announcement Management)

---

## Function: `FeedbackWindowPolicy.assertOpen()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-001` | | Function Name | `FeedbackWindowPolicy.assertOpen` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 28 | | Lack of test cases | 0 |
| Test requirement | The 24-hour feedback window is computed server-side from `servedAt` (via `ReferenceResolver.servedAtOf()`, `M10-D06`), never from client-supplied `mealDate` (BR-139, MSG-022, source: MF10-TC-001). Must distinguish "outside window" (`FBA-022`), "session not found" (`FBA-404`) and "meal not yet served" (`FBA-REQ`) as three different failure codes. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 1 | 3 | 2 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Precondition | `Clock` = `FX-001` (`2026-07-01T14:00:00Z`), `ReferenceResolver` stub = `FX-002` | O | O | O | O | O | O |
| `servedAt` vs `now` | `2026-06-30T14:00:01Z` — 23h59m59s (inside) | O | | | | | |
| | `2026-06-30T14:00:00Z` — exactly 24h00m00s (closed boundary) | | O | | | | |
| | `2026-06-30T13:59:59Z` — 24h00m01s (just outside) | | | O | | | |
| | `2026-06-30T03:50:00Z` (`MS-20260630-L`) — ~34h (well outside) | | | | O | | |
| | `Optional.empty()` (`MS-9999`) — session does not exist | | | | | O | |
| | `2026-07-01T15:00:00Z` — future serve time (meal not yet served) | | | | | | O |
| **Confirm** | | | | | | | |
| Return | Passes the gate, `referenceResolver.servedAtOf(...)` invoked exactly once, proceeds to BR-140 check | O | O | | | | |
| Exception | `FeedbackWindowClosedException` code `FBA-022`, `repository.save()` never called | | | O | O | | |
| | `FBA-404` — meal session not found (NOT `FBA-022`) | | | | | O | |
| | `FBA-REQ` — cannot submit for a meal not yet served | | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | B | B | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: If the policy reads `req.getMealDate()` instead of the resolved `servedAt`, a parent can re-open the window indefinitely by editing a single request field — this is exactly the gap `review.md` H4 documents. If `Optional.empty()` also returns `FBA-022`, the user sees "outside 24h" when the real cause is a non-existent session (BR-139, L10/CF-M10-11).

---

## Function: `FeedbackService.update() / retract()` — window guard on edit/delete

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-002` | | Function Name | `FeedbackService.update` / `FeedbackService.retract` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 34 | | Lack of test cases | 0 |
| Test requirement | BR-139's 24-hour window applies to `PATCH` and `DELETE`, not only `POST` (UC-046 AF1/AF2, EX1: "if an edit is attempted via API"). Source: MF10-TC-002. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 2 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | `FX-003` (`FB-8812`, session still inside the window) | O | O | | |
| | `FX-004` (`FB-8809`, session already past 24h) | | | O | O |
| Operation | `update("FB-8812", {rating:4}, parent)` | O | | | |
| | `retract("FB-8812", parent)` | | O | | |
| | `update("FB-8809", {rating:4}, parent)` | | | O | |
| | `retract("FB-8809", parent)` | | | | O |
| **Confirm** | | | | | |
| Return | Saved successfully, `updatedAt != null` | O | | | |
| | `deletedAt != null` | | O | | |
| Exception | `FeedbackWindowClosedException` (`FBA-022`), no DB write | | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: If the gate only fires on `POST`, a parent could alter the content of a feedback entry after it has already fed into the weekly summary (BR-141) or after Medical Staff opened an investigation off an illness keyword (BR-142) — UC-046 EX1 names this attack path explicitly ("via API"), so this is not a theoretical risk. The FE currently has no Edit/Delete button (H10); this test proves the gate lives server-side regardless.

---

## Function: `QualityTagRule` — mandatory tag below 3 stars

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-003` | | Function Name | `QualityTagRule.validate` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 26 | | Lack of test cases | 0 |
| Test requirement | When a Parent rates below 3 stars, the system must require at least one non-blank quality-concern tag (BR-140, error code `FBA-140`). Source: MF10-TC-003. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 7 | 3 | 3 | 1 | 7 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 |
|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | |
| Precondition | `submitterRole = PARENT` | O | O | O | O | O | O | O |
| `rating` / `tags` | `rating=1, tags=[]` | O | | | | | | |
| | `rating=2, tags=[]` (upper boundary of the forbidden zone) | | O | | | | | |
| | `rating=2, tags=["Món ăn nguội"]` | | | O | | | | |
| | `rating=3, tags=[]` (lower boundary of the allowed zone — BR-140 says "below 3", not "3 or below") | | | | O | | | |
| | `rating=5, tags=[]` | | | | | O | | |
| | `rating=0` or `rating=6` (outside the 1–5 scale) | | | | | | O | |
| | `rating=2, tags=["  "]` (whitespace-only tag) | | | | | | | O |
| **Confirm** | | | | | | | | |
| Return | Valid, request proceeds to `save()` | | | O | O | O | | |
| Exception | `FBA-140` — quality tag required, no `save()` | O | O | | | | | O |
| | `FBA-REQ` — rating out of the 1–5 scale | | | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 |
|---|---|---|---|---|---|---|---|
| Type (N/A/B) | A | B | N | B | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | |

Note: The invariant is also enforced at DB level via CHECK `ck_fb_low_rating_needs_tag` (C4) — the service check must not be the only line of defense, since it is bypassed when a record is inserted directly (see `MF10-TC-INT-003`).

---

## Function: `QualityTagRule` — exemption for Teacher submissions (CF-M10-3)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-004` | | Function Name | `QualityTagRule.validate` (role branch) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 12 | | Lack of test cases | 0 |
| Test requirement | BR-140 applies only to Parent submissions — traceability §1.10 lists only BR-139 against UC-086 (Teacher). Traceability is the authoritative source over the UC file (CF-M10-3, BR-140 literal text: "when a **parent** rates below 3 stars..."). Source: MF10-TC-004. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 1 | 1 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| `submit(rating=2, tags=[], submitterRole=TEACHER, scope=CLASS)` | | O | |
| `submit(rating=2, tags=[], submitterRole=PARENT)` (control case, same run) | | | O |
| **Confirm** | | | |
| Return | Valid — Teacher is not required to supply a tag | O | |
| Exception | `FBA-140` — Parent is still blocked | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | A |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | | |

Note: This test is deliberately written as one case with both role branches to prevent anyone from "fixing for consistency" in only one direction. This is the single most likely test to be edited incorrectly — if a reviewer later decides UC-086 should also carry BR-140, `traceability_matrix.md` §1.10 must be updated first, and only then this test.
