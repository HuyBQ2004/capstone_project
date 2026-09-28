# Integration Test Case — Feedback Response & Anonymized Dashboard (MF10 Feedback & Announcement Management)

---

## Feature: Feedback Response (BR-143) & Anonymized Dashboard (BR-141/BR-145)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Feedback Response (BR-143) & Anonymized Dashboard (BR-141/BR-145) |
| Test requirement | Verify `POST /api/v1/feedbacks/{code}/response` closes the loop end-to-end (caterer writes, parent reads, notified) per UC-163/BR-143, and that `GET /api/v1/feedbacks/dashboard` aggregates feedback anonymously down to class level (BR-145) while the weekly digest job (BR-141) notifies both School Administration and the catering manager. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 2 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/feedbacks/{code}/response — Reply End-to-End and Notify the Submitter** | | | | | | | | | | | | | | |
| MF10-TC-INT-006 | Test that a Service Caterers reply is saved, the responder identity comes from the JWT (not the body), the original submitter is notified and can read the reply, the "not responded" filter reflects it, and multiple replies append. | 1. `service-caterers` `POST /feedbacks/FB-8809/response {content:"Đã điều chỉnh thời gian giữ nóng…"}`.<br>2. Read DB `feedback_responses`.<br>3. Read DB `notifications`.<br>4. `parent` (owner) `GET /feedbacks/FB-8809`.<br>5. `service-caterers` `GET /feedbacks?not:responded=true`.<br>6. `POST` a second reply. | 1. HTTP 201, `lateResponse` correct for the elapsed time.<br>2. 1 row, `responder_user_id` = actor from JWT.<br>3. 1 notification row sent to the feedback owner (`parent`).<br>4. `responses[]` has 1 item with `content`/`respondedAt`/`responder`.<br>5. `FB-8809` no longer appears (negative filter `not:` per §2b-2 works).<br>6. `responses[]` now has 2 items (append, multiple replies allowed — TDS §6.6). | `FX-004` (`FB-8809`, `rating=2`) seeded. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | BR-143, UC-163 Postconditions, UC-046 step 4 |
| **GET /api/v1/feedbacks/dashboard — Anonymized to Class Level; Weekly Digest (BR-141/BR-145)** | | | | | | | | | | | | | | |
| MF10-TC-INT-007 | Test that the School Administration dashboard aggregates without leaking any student identity, and that the weekly digest job notifies both School Administration and Service Caterers. | 1. **Assertion of premise:** confirm DB has ≥3 feedback rows with non-null `student_id`.<br>2. `school-admin` `GET /feedbacks/dashboard?weekStart=2026-06-29`.<br>3. Inspect response fields.<br>4. Serialize the whole response to a string and search for identity leaks.<br>5. Inspect `byClass`.<br>6. Run `compileAndSendWeeklyDigest(schoolId)`.<br>7. Confirm a soft-deleted feedback is excluded. | 1. Premise holds.<br>2. HTTP 200.<br>3. Has `totalFeedbacks`, `averageRating`, `ratingDistribution`, `negativeCount`, `illnessFlaggedCount`, `topTags`, `byClass`.<br>4. `doesNotContain("studentId", "student", "HS-3A-", "Nguyễn Minh Khang")`.<br>5. No array element in `byClass` carries an individual identity.<br>6. A digest notification is generated for BOTH `school-admin` AND `service-caterers`.<br>7. A retracted feedback (from `TC-INT-005`) does not count toward the totals. | ≥3 feedback rows with `student_id`; at least one soft-deleted row from `TC-INT-005`. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | BR-141 ("catering manager AND school leadership"), BR-145, UC-164; DPO-relevant |
