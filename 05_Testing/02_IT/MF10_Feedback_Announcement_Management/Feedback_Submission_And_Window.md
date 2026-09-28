# Integration Test Case — Feedback Submission, 24h Window & Duplicate/Retract (MF10 Feedback & Announcement Management)

---

## Feature: Feedback Submission — End-to-End, 24h Window, Duplicate Prevention & Soft-Delete

### 1. General Information

| Field | Value |
|---|---|
| Feature | Feedback Submission — End-to-End, 24h Window, Duplicate Prevention & Soft-Delete |
| Test requirement | Verify `POST /api/v1/feedbacks` end-to-end (UC-085 happy path, BR-139/140/142, MSG-046), that the 24-hour window (BR-139) is enforced at the API layer (not just the FE) for submit/edit/delete, that BR-140's low-rating-needs-tag rule is enforced at the DB layer too, that one person can submit only once per (meal session × dish × student) even under concurrent requests, and that retracting feedback is a soft delete that preserves data for audits/investigations. |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 4 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/feedbacks — Submit End-to-End (UC-085 happy path)** | | | | | | | | | | | | | | |
| MF10-TC-INT-001 | Test that a low-rating feedback containing an illness keyword saves correctly and dispatches both MSG-057 and MSG-058 notifications, even though the client lies about `illnessKeyword`. | 1. `POST /api/v1/feedbacks` with `rating=2`, `tags=["Món ăn nguội"]`, `comment="Con về kêu đau bụng nhẹ"`, `illnessKeyword:false` (client lies — L9).<br>2. Read HTTP response.<br>3. Read DB `feedbacks` row.<br>4. Read DB `notifications` rows.<br>5. Read audit log. | 1. HTTP 201; body `id` matches `FB-…`, `editableUntil = servedAt + 24h` (ISO).<br>2. DB: 1 new row, `illness_keyword = true` (server recomputed), `submitted_at` = server clock, `meal_session_id` not null.<br>3. DB `notifications`: 3 new rows — `FeedbackSubmitted` (caterer), MSG-057 (`rating<3`), MSG-058 (`ALLERGY_ALERT`, roles include `medical-staff`).<br>4. Audit log has `CREATE Feedback FB-…`. | Flyway `V18__mf10_feedback_announcement.sql` + `V19__seed_mf10.sql` applied; `ReferenceResolver` stub `FX-002`; clock `FX-001`; actor `FX-017` (parent). | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-085, BR-139/140/142, MSG-046; L9/H3 |
| **POST /api/v1/feedbacks — 24-Hour Window Enforced at API Layer (MSG-022)** | | | | | | | | | | | | | | |
| MF10-TC-INT-002 | Test that submitting feedback for a meal served more than 24 hours ago is blocked at the API, not only hidden by the FE. | 1. `POST /feedbacks {mealSessionId:"MS-20260630-L"}` (served ~34h ago).<br>2. Read response body.<br>3. Query DB for a new row.<br>4. `POST` with `mealSessionId:"MS-9999"` (non-existent). | 1. HTTP 409.<br>2. `error.code == "FBA-022"`, `error.message` verbatim `"Feedback submission blocked: Outside the 24-hour limit of meal serving."`, `error.details` is an array (possibly empty, never `null`).<br>3. No new DB row.<br>4. HTTP 404 `FBA-404` (distinct from the 409 case). | Seed feedback session `MS-20260630-L` served ~34h before the fixed clock. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | BR-139, MSG-022, UC-085 AF2; H4 |
| **DB CHECK Constraint — BR-140 Enforced Below the Service Layer** | | | | | | | | | | | | | | |
| MF10-TC-INT-003 | Test that `ck_fb_low_rating_needs_tag` blocks a Parent low-rating row with no tags even when inserted directly via JDBC, bypassing the service. | 1. `INSERT` via JDBC: `submitter_role='PARENT', rating=2, tags='[]'`.<br>2. `INSERT`: `submitter_role='TEACHER', rating=2, tags='[]'`.<br>3. `INSERT`: `rating=0`.<br>4. `UPDATE feedbacks SET tags='[]' WHERE rating=2 AND submitter_role='PARENT'`. | 1. `DataIntegrityViolationException`, message references `ck_fb_low_rating_needs_tag`.<br>2. Succeeds (constraint has a `submitter_role <> 'PARENT'` branch — CF-M10-3).<br>3. `CHECK (rating BETWEEN 1 AND 5)` violation.<br>4. Same constraint blocks the UPDATE path too, not just INSERT. | Testcontainers PostgreSQL 16, Flyway applied. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | BR-140, C4 |
| **POST /api/v1/feedbacks — One Submission Per (User × Meal Session × Dish × Student), Including Concurrency** | | | | | | | | | | | | | | |
| MF10-TC-INT-004 | Test that a duplicate submission for the same key is rejected, distinct dish/student combinations succeed, and a race of two concurrent identical requests never produces two rows. | 1. `POST` once.<br>2. `POST` again with the same key.<br>3. `POST` same session, different dish.<br>4. `POST` same session/dish, different student (parent with 2 children).<br>5. Fire 2 threads simultaneously with the identical key. | 1. HTTP 201.<br>2. HTTP 409 `FBA-DUP`.<br>3. HTTP 201.<br>4. HTTP 201.<br>5. Exactly 1 thread succeeds, 1 receives 409; DB has exactly 1 row for that key. | `uq_fb_once (submitter_user_id, meal_session_id, dish_id, student_id)` migrated. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | TDS §5.2 `uq_fb_once`; `student_id` NULL handling for Teacher scope=CLASS |
| **DELETE /api/v1/feedbacks/{code} — Retract Is Soft-Delete** | | | | | | | | | | | | | | |
| MF10-TC-INT-005 | Test that retracting feedback within the window sets `deleted_at` without a hard delete, disappears from reads/dashboard but remains in the DB, and cannot be deleted twice; retracting outside the window is still blocked by BR-139. | 1. `DELETE /feedbacks/FB-8812` (inside window).<br>2. Read DB row.<br>3. `GET /feedbacks` (owner).<br>4. `GET /feedbacks/FB-8812`.<br>5. `GET /feedbacks/dashboard`.<br>6. `DELETE` a second time.<br>7. `DELETE /feedbacks/FB-8809` (outside window). | 1. HTTP 204.<br>2. Row still exists, `deleted_at` not null.<br>3. `FB-8812` absent from the list.<br>4. HTTP 404.<br>5. `totalFeedbacks` decreased by 1.<br>6. HTTP 404 (not deleted twice).<br>7. HTTP 409 `FBA-022`. | `FX-003` (`FB-8812`, inside window), `FX-004` (`FB-8809`, outside window) seeded. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-046 AF2, TDS §4.2, BR-141/142 (soft delete preserves audit trail) |
