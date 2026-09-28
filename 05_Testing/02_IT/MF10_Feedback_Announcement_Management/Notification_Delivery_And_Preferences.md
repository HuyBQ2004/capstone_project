# Integration Test Case — Notification Read State, Preferences & Delivery Pipeline (MF10 Feedback & Announcement Management)

---

## Feature: Notification Read State, Preferences & Delivery Pipeline (BR-138/158/158b/159)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Notification Read State, Preferences & Delivery Pipeline (BR-138/158/158b/159) |
| Test requirement | Verify read state is tracked per-recipient rather than as a global flag (CF-M10-10), that notification preferences persist across restarts and that the 3 CRITICAL types cannot be dispatch-disabled (BR-138/158b), and that the delivery pipeline correctly defers during quiet hours, sends critical alerts immediately, retries up to the configured maximum, and falls back to email via Mailpit (BR-159). |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 2 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Notification Read State — Per-Recipient, Not Global** | | | | | | | | | | | | | | |
| MF10-TC-INT-013 | Test that marking a notification read only affects the acting user, not every recipient, and that the table shape backing it is `notification_reads`, not a `read` column on `notifications`. | 1. Seed `NTF-900` to `["medical-staff","school-admin"]`, unread.<br>2. `U-MEDICAL-001` `GET /notifications`.<br>3. `U-MEDICAL-001` `PATCH /notifications/NTF-900/read`.<br>4. `U-MEDICAL-001` `GET` again.<br>5. `U-ADMIN-001` `GET`.<br>6. Inspect DB.<br>7. `PATCH read` a second time by the same user.<br>8. `U-CATERER-001` (not a recipient) `GET` and `PATCH read`.<br>9. `GET` as any role for `NTF-895` (`audience=ALL`). | 1. Seeded.<br>2. `NTF-900` shows `read=false`.<br>3. HTTP 204.<br>4. `read=true`.<br>5. `NTF-900` still shows `read=false` for the admin ⭐ (the crux of the test).<br>6. `notification_reads` has exactly 1 row; `notifications` table has no `read` column.<br>7. HTTP 204, idempotent, still exactly 1 row (`uq_ntfread`).<br>8. `NTF-900` absent from the list; `PATCH read` → 403/404.<br>9. Every role sees `NTF-895`. | `FX-009` (5 seeded notifications), `FX-010` (`notification_reads` fixture) applied. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | CF-M10-10 |
| **PUT /api/v1/notification-preferences/me — Persisted Config, CRITICAL Types Cannot Be Disabled** | | | | | | | | | | | | | | |
| MF10-TC-INT-014 | Test that preferences persist across an application restart, and that the 3 CRITICAL notification types still dispatch even when a user tries to disable them, while non-critical disabled types still get recorded (visible in the bell) without pushing. | 1. `GET /notification-preferences/me` with no existing record.<br>2. `PUT {typeEnabled:{EXPIRY_ALERT:false}, quietStart:"21:00", quietEnd:"07:00"}`.<br>3. `GET` again.<br>4. Restart the application context (`@DirtiesContext`), `GET` again.<br>5. `PUT {typeEnabled:{FOOD_RECALL:false}}`.<br>6. Dispatch `FOOD_RECALL` to that user at 23:30.<br>7. Dispatch `EXPIRY_ALERT` (disabled in step 2).<br>8. `PUT {quietStart:"25:00"}`. | 1. HTTP 200, BR-158 defaults (`22:00`/`06:00`, all types `true`, `emailFallback=true`) — NOT 404.<br>2. HTTP 200.<br>3. Reflects the values just saved (unlike the mock, which only holds state in `useState`).<br>4. Values survive the restart — truly persisted.<br>5. HTTP 200 accepted; response still lists `criticalTypes:["INCIDENT_ALERT","FOOD_RECALL","ALLERGY_ALERT"]`.<br>6. Still sent (BR-138 overrides personal configuration).<br>7. Not pushed, but a row is still written to `notifications` for history (UC-136).<br>8. HTTP 422. | User with no prior preference record; `FX-012` fixtures available for comparison. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | BR-158b, BR-138, UC-088; CF-M10-7 (mock loses state on reload — backend must not) |
| **Notification Delivery Pipeline — Quiet Hours, Immediate Critical, Retry, Email Fallback** | | | | | | | | | | | | | | |
| MF10-TC-INT-018 | Test the full delivery pipeline end-to-end: non-urgent notifications deferred during quiet hours and sent once the window opens, urgent alerts sent immediately regardless of quiet hours, retry-then-succeed, and retry-exhausted-then-email-fallback via Mailpit; also lock down that only MF10 code writes to the notifications table. | 1. Clock 23:30 VN; `publish(MENU_PUBLISHED → school-admin)`.<br>2. Advance clock to 06:00 and run the scheduler.<br>3. Clock 23:30; `publish(FOOD_RECALL → parent)`.<br>4. `publish(MENU_PUBLISHED → U-CATERER-001)` with the gateway failing 3 times then succeeding (`FX-015`).<br>5. `publish(… → U-ADMIN-001)` with the gateway always failing, `emailFallback=true`.<br>6. `publish(… → U-TEACHER-001)` always failing, `emailFallback=false`.<br>7. ArchUnit scan of the codebase. | 1. `notification_deliveries.status = DEFERRED`, `next_retry_at = 06:00` next day; no email.<br>2. `status = SENT`, gateway called exactly once.<br>3. `status = SENT` immediately (BR-138), bypassing quiet hours.<br>4. Sequence `FAILED(1..3) → SENT`; no email.<br>5. Sequence `FAILED(1..3) → FALLBACK_EMAIL`; Mailpit (`localhost:8025/api/v1/messages`) shows exactly 1 message.<br>6. Stops at `FAILED`; Mailpit count unchanged.<br>7. No class outside `com.sfms.mf10` calls `notificationRepository.save()` directly — every other module goes through `NotificationPort` (ADR-M10-4/C8). | `FX-015` gateway stub, Mailpit running at `:8025`, `MutableClock` injected (no `Thread.sleep`, must run under 120s). | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | BR-138/158/158b/159, ADR-M10-4, ADR-000 |
