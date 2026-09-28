# Unit Test Case — NotificationType / MealType / RoleKey / Date format / Public-ID Contracts (MF10 Feedback & Announcement Management)

---

## Function: `NotificationType` enum contract — 7 values, `critical` is derived (L2/H5)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-019` | | Function Name | `NotificationEnumContractTest` (`common/enums/NotificationType`, `NotificationCommand` deserialize) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | `NotificationType` is fixed at exactly the 7 values chosen in D6 (`MENU_PUBLISHED, INSPECTION_ALERT, CAPA_ASSIGNED, EXPIRY_ALERT, INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT`), overriding the 4-value taxonomy (`ALLERGY/FOOD_SAFETY/NEWS/SYSTEM`) still present in TDS-MF10 v1.0 and the mock (`review.md` H5 — blocker: "two taxonomies in the same module"). `critical` must be derived from `type`, not stored. Source: MF10-TC-018. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 6 | 2 | 3 | 1 | 6 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|---|
| **Condition** | | | | | | | |
| Enum values | `NotificationType.values()` | O | | | | | |
| Deserialize | `{"type":"ALLERGY_ALERT"}` | | O | | | | |
| | `{"type":"FOOD_SAFETY"}` / `"NEWS"` / `"SYSTEM"` / `"ALLERGY"` (legacy 4-value taxonomy) | | | O | | | |
| Serialize | Any of the 7 values | | | | O | | |
| `isCritical()` | `INCIDENT_ALERT`, `FOOD_RECALL`, `ALLERGY_ALERT` | | | | | O | |
| | The other 4 types | | | | | | O |
| **Confirm** | | | | | | | |
| Return | `values().length == 7`, exactly matches the D6 list | O | | | | | |
| | Accepted, mapped to `NotificationType.ALLERGY_ALERT` | | O | | | | |
| | Rejected, `HttpMessageNotReadableException` | | | O | | | |
| | Serializes as SCREAMING_SNAKE string, not a Vietnamese label | | | | O | | |
| | `isCritical() == true` | | | | | O | |
| | `isCritical() == false` | | | | | | O |
| Structural | `UserNotification` class has no `critical` field (checked after first confirming it does have a `type` field, to avoid a false green) | O | | | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 | UTCID06 |
|---|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | N | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | | |

Note: This test overrides TDS-MF10 §5.2 by design (L2) — the mapping from the 7 types to 4 badge colors is not yet defined anywhere (F1), and this test intentionally does not assume one. Keeping the two taxonomies side by side means a user who disables `EXPIRY_ALERT` in settings can still receive it (or the dispatcher can wrongly block an allergy alert) because the settings screen and the notification record cannot talk to each other.

---

## Function: `MealType` enum contract — rejects Vietnamese display strings (L4/H1)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-020` | | Function Name | `MealTypeContractTest` (`common/enums/MealType`, `SubmitFeedbackRequest`) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 16 | | Lack of test cases | 0 |
| Test requirement | `MealType` is `BREAKFAST/LUNCH/SNACK`, SCREAMING_SNAKE, sourced from `common/enums` — never declared locally inside `com.sfms.mf10` (D6, `M10-D09`, `review.md` H1 — blocker, three-way conflict across MF05/MF07/MF10). Source: MF10-TC-019. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 2 | 3 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| `mealType` payload | `"LUNCH"` | O | | | | |
| | `"Bữa trưa"` — exactly what the mock currently sends (`feedback/meal/page.tsx:91`) | | O | | | |
| | `"Bữa chiều"` — MF07's value, D6 states "Bữa phụ" ≠ "Bữa chiều" | | | O | | |
| | `"lunch"` (wrong case) | | | | O | |
| Import source | `MealType` class location | | | | | O |
| **Confirm** | | | | | | |
| Return | Accepted; entity stores `LUNCH`; `?mealType=LUNCH` filters correctly | O | | | | |
| Exception | `400` | | O | O | O | |
| Structural | Imported from `com.sfms.common.enums`, not declared inside `com.sfms.mf10` | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: If `common/enums/MealType` does not yet exist, this test must fail at compile time — that is the intended behavior (§6 Suspension: the module must not declare a temporary local enum just to turn the test green). Accepting Vietnamese strings would leave `meal_type` containing a mix of `"Bữa trưa"` and `"LUNCH"` within months, silently splitting the BR-141 weekly statistics by meal.

---

## Function: `RoleKey` enum contract — `recipientRoles` is kebab-case (L3/H6)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-021` | | Function Name | `RoleKeyContractTest` (`NotificationCommand.recipientRoles`, `UserNotification.recipientRoles`) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 18 | | Lack of test cases | 0 |
| Test requirement | `RoleKey` values are kebab-case (`system-admin, school-admin, medical-staff, service-caterers, teacher, parent, supplier`) since they also serve as URL path segments (D6). `review.md` H6 — blocker, "two role-identifier vocabularies in the same system". Source: MF10-TC-020. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 2 | 3 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Request | `publish(recipientRoles: ["medical-staff","school-admin"])` | O | | | | |
| | `publish(recipientRoles: ["Medical Staff"])` — TDS-MF10 v1.0's current value | | O | | | |
| | `publish(recipientRoles: ["Raw Food Supplier"])` — correct value is `supplier` | | | O | | |
| | `audience=ROLES` with `recipientRoles=[]` | | | | O | |
| | `audience=ALL` with `recipientRoles=null` | | | | | O |
| **Confirm** | | | | | | |
| Return | Accepted, JSON keeps kebab-case | O | | | | O |
| Exception | `400` | | O | O | | |
| | `ck_ntf_roles` CHECK violation | | | | O | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: Mixing the two vocabularies breaks `notification-bell.tsx`, which filters with `recipientRoles.includes("medical-staff")` — a `"Medical Staff"` value never matches, so the bell stays silent for the exact role that needed the medical alert. This is a silent failure: no exception, just nobody sees the notification.

---

## Function: Date format contract — all dates are ISO-8601; `submittedAt` is server-assigned (L14/D4)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-022` | | Function Name | `DateFormatContractTest` (Jackson config + all MF10 DTOs) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 22 | | Lack of test cases | 0 |
| Test requirement | Every date field serializes as ISO-8601 (`LocalDate` for calendar dates, `Instant` for timestamps — D4); the server always assigns `submittedAt`, ignoring any client value. Source: MF10-TC-021. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 1 | 4 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Serialize | `FeedbackDetail`, `AnnouncementDetail`, `NotificationResponse` | O | O | | | |
| Request payload | `{"submittedAt":"01/07/2026 19:30"}` — exact string the mock currently sends via `toLocaleString("vi-VN")` | | | O | | |
| | `{"submittedAt":"2020-01-01T00:00:00Z"}` — client tries to backdate | | | | O | |
| Code scan | `@JsonFormat` usages inside package `com.sfms.mf10` | | | | | O |
| **Confirm** | | | | | | |
| Return | No field matches `\d{2}/\d{2}/\d{4}`; `mealDate` matches `^\d{4}-\d{2}-\d{2}$`; instant fields match ISO-8601 | O | O | | | |
| | Value is ignored; `submittedAt` = server clock (`FX-001`) | | | O | O | |
| Structural | Zero matches — no `@JsonFormat` annotation anywhere in the package | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: If `submittedAt` is accepted from the client, a parent could backdate a submission to slip through the 24-hour window or make content land in the wrong weekly statistics bucket (BR-141). This test is reusable across all 12 other modules under D4 — consider hoisting it to shared `common` test fixtures instead of duplicating it.

---

## Function: Public-ID contract — `id` is always the business code, never a UUID (L1/D1)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `FBA-FUNC-023` | | Function Name | `PublicIdContractTest` (all MF10 DTOs, `FeedbackSpec`, `AnnouncementSpec`) |
| Created By | Nguyễn Đức Mạnh | | Executed By | Manh |
| Lines of code | 24 | | Lack of test cases | 0 |
| Test requirement | The public `id` field is always the business code (`code`); path params and JSON foreign keys use `code`, never the internal UUID PK (D1; `review.md` A1 — blocker, called out `"id": "FB-8809"`/`"id": "ANN-113"` explicitly). `SEARCH_FIELDS` must match the real entity columns (CF-M10-8). Source: MF10-TC-022. | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 3 | 2 | 0 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| Serialize | `FeedbackDetail` | O | | | | |
| | `AnnouncementDetail` | | O | | | |
| Assert on constant | `FeedbackSpec.SEARCH_FIELDS` | | | O | | |
| | `AnnouncementSpec.SEARCH_FIELDS` | | | | O | |
| Column-to-JSON mapping | `dish_name`→`dish`, `class_name`→`class` via `@JsonProperty` | | | | | O |
| **Confirm** | | | | | | |
| Return | `id = "FB-8812"`, `dishId = "DISH-021"`, `mealSessionId = "MS-20260701-L"` — no UUID anywhere | O | | | | |
| | `id = "ANN-112"`; `revisions[].by` is a name, `revisions[].at` is ISO | | O | | | |
| | `== ["comment","dishName","className"]` — NOT the `["title","senderName","content"]` from Team Plan §2b-5 (fields that don't exist on `Feedback`, item F4) | | | O | | |
| | `== ["title","content","audienceLabel"]` | | | | O | |
| | JSON exposes the mapped names correctly without breaking FE's read contract | | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | N | N | A | A | N |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note: Leaking a UUID would make FE navigate to `/parent/feedback/<uuid>` while every other screen shows `FB-8809`; a `search` running against non-existent columns always returns empty with no error, which is much harder to notice than an exception.
