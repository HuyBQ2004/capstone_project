# Unit Test Case — NotificationDispatcher / QuietHoursPolicy / DeliveryRetryScheduler (MF10 Feedback & Announcement Management)

---

## Function: `NotificationDispatcher.dispatch()` — urgent alerts bypass quiet hours and user opt-out (BR-138)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-015` | | Function Name | `NotificationDispatcher.dispatch` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 34 | | Lack of test cases | 0 |
| Test requirement | Critical food-safety alerts (recall, incident, allergy) must be dispatched immediately, ignoring both quiet hours and per-user type opt-out (BR-138, BR-158 "urgent alerts are not affected by quiet hours"). `critical` is derived from `type ∈ {INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT}` (D6), never taken from the request body. Source: MF10-TC-014. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 2 | 3 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Precondition | Clock = 23:30 VN (inside quiet hours) | O | O | O | O | O |
| `type` / `typeEnabled[type]` | `FOOD_RECALL`, `typeEnabled=false` (user disabled it) | O | | | | |
| | `INCIDENT_ALERT`, `typeEnabled=false` | | O | | | |
| | `ALLERGY_ALERT`, `typeEnabled=true` | | | O | | |
| | `MENU_PUBLISHED`, `typeEnabled=true` | | | | O | |
| | `EXPIRY_ALERT`, `typeEnabled=false` | | | | | O |
| **Confirm** | | | | | | |
| Return | Sent immediately — critical wins over both quiet hours and opt-out | O | O | O | | |
| | `NotificationDelivery(status=DEFERRED)` until `quietEnd` | | | | O | |
| | Not sent to the gateway, but still persisted for the notification bell (UC-136) | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | A | A | N | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: If a critical alert is blocked by quiet hours, a food recall notice sent at 23:30 waits until 06:00 — by then the recalled batch may already be used at breakfast. `critical` must never come from the request body, or a caller could self-declare "critical" to bypass quiet hours.

---

## Function: `QuietHoursPolicy.shouldDefer()` — default 22:00–06:00 window (BR-158)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-016` | | Function Name | `QuietHoursPolicy.shouldDefer` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | Non-urgent push notifications must be deferred by default between 22:00–06:00 local time (`Asia/Ho_Chi_Minh`), including the case where the window wraps past midnight (BR-158). Source: MF10-TC-015. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 7 | 3 | 0 | 4 | 7 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 |
|---|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | | |
| Precondition | `critical=false` | O | O | O | O | O | O | O |
| Local time (VN) | `21:59:59` (just before) | O | | | | | | |
| | `22:00:00` (closed lower boundary) | | O | | | | | |
| | `23:30:00` (mid-window, before midnight) | | | O | | | | |
| | `00:30:00` — wraps past midnight (`start > end`) ⭐ | | | | O | | | |
| | `05:59:59` (just before the upper boundary) | | | | | O | | |
| | `06:00:00` (open upper boundary) | | | | | | O | |
| | `12:00:00` (well outside the window) | | | | | | | O |
| **Confirm** | | | | | | | | |
| Return | Sent | O | | | | | O | O |
| | Deferred, `nextRetryAt` = 06:00 same day (if before midnight) or next day (if after midnight) | | O | O | O | O | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 | UTCID07 |
|---|---|---|---|---|---|---|---|
| Type (N/A/B) | B | B | N | N | B | B | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | | |

Note: A naive `now.isAfter(start) && now.isBefore(end)` implementation classifies `00:30` as **outside** quiet hours (since `00:30 < 22:00`), silently disabling protection for the entire midnight–06:00 half of the window — exactly the half BR-158 exists to protect. UTCID04 (`00:30`) is mandatory and must never be dropped from the suite. The comparison must be `now >= start || now <= end` when `start > end`, computed on `LocalTime` in `Asia/Ho_Chi_Minh`, never on raw UTC `Instant`.

---

## Function: `QuietHoursPolicy` + `NotificationPreference` — per-user override (BR-158b)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-017` | | Function Name | `QuietHoursPolicy.shouldDefer` (personalized window) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 18 | | Lack of test cases | 0 |
| Test requirement | Users may configure their own quiet-hours window, which overrides the system default (BR-158b, UC-088, `FX-012`: `U-TEACHER-001` set to `21:00–07:00`). Users without a saved preference fall back to the default without error. Source: MF10-TC-016. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 3 | 1 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Clock / user | `21:30` VN, default user (`22:00–06:00`) | O | | | |
| | `21:30` VN, `U-TEACHER-001` (`21:00–07:00`) | | O | | |
| | `06:30` VN, default user | | | O | |
| | User with no preference record at all | | | | O |
| **Confirm** | | | | | |
| Return | Sent | O | | O | O |
| | Deferred | | O | | |
| Behavior | Falls back to the BR-158 default, no `NullPointerException` | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | N | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: The same `now` value must produce two different outcomes depending on whose preference is looked up (UTCID01 vs UTCID02). If defaults are read from a global constant instead, BR-158b becomes meaningless — the settings screen saves data that never reaches the code path that uses it.

---

## Function: `DeliveryRetryScheduler` / `RetryBackoffPolicy` — retry then email fallback (BR-159)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-018` | | Function Name | `DeliveryRetryScheduler.runOnce` |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Failed push deliveries must retry automatically up to `MAX_ATTEMPTS = 3`, then fall back to email if `emailFallback=true`, or stop at `FAILED` if `emailFallback=false` (BR-159, `DeliveryStatus{QUEUED,DEFERRED,SENT,FAILED,FALLBACK_EMAIL}`, `FX-015`). Source: MF10-TC-017. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Scenario (`FX-015`) | `U-ADMIN-001` — succeeds immediately | O | | | |
| | `U-CATERER-001` — fails 3 times then succeeds | | O | | |
| | `U-TEACHER-001` — always fails, `emailFallback=false` | | | O | |
| | `U-ADMIN-001` — always fails, `emailFallback=true` | | | | O |
| **Confirm** | | | | | |
| Status sequence | `QUEUED → SENT` | O | | | |
| | `QUEUED → FAILED(1) → FAILED(2) → FAILED(3) → SENT` | | O | | |
| | `QUEUED → FAILED(1..3) → FAILED` (stops, no email) | | | O | |
| | `QUEUED → FAILED(1..3) → FALLBACK_EMAIL`, exactly 1 email sent | | | | O |
| Bound | `attempt` never exceeds 3 | | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: BR-159 specifies no exact retry count — `3` is chosen per TDS §6.4/C8 and centralized as `RetryBackoffPolicy.MAX_ATTEMPTS`; if this number changes, only the constant should move, boundary tests should still assert an absolute number to avoid a tautology. Missing the `attempt <= 3` bound turns the scheduler into an infinite retry loop against a device the user has already uninstalled the app from; sending the fallback email before retries are exhausted would double up push + email for the same event.
