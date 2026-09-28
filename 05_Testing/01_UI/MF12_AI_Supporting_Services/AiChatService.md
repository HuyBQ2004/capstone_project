# Unit Test Case — AiChatService (MF12 AI & Supporting Services)

> Group (A) — AI Gateway. Layer `com.sfms.mf12.ai.*`. `GeminiClient` mocked/faked per `ADR-000` §Mock Strategy (fake JSON matching schema contract). Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-001`…`MF12-TC-005`.
> ⚠️ All 5 functions are currently `⚠️ BLOCKED by L6/CF-2` in the TestSpec (the FE mock has no AI chat resource/route yet) — test cases are written and kept `🔴 RED`, not executed against real code until the route/resource is added.

---

## Function: `AiChatService.ask()` + `AiArchiveService.archive()` (same-transaction archive)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-001` | | Function Name | `AiChatService.ask() / AiArchiveService.archive()` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 55 | | Lack of test cases | 0 |
| Test requirement | A successful AI chat MUST produce exactly one `ai_requests` row in the same transaction as the answer, and `AiAssistPort`/`GeminiClient` must be the only call path (BR-157, ADR-M12-01). (source: MF12-TC-001) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 1 | 0 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | `FX-022` fake Gemini returns valid JSON (schema per ADR-000) after 50ms | O |
| | JWT of any role — UC-012 actor is "User, any role" (`FX-029`/`FX-030`) | O |
| Request | `service.ask(new ChatRequest("Suggest a menu for grade 3", null), actor)` | O |
| **Confirm** | | |
| Return | `answer` is not null | O |
| | Exactly **1** `ai_requests` row with `status=SUCCESS`, `response_text` and `prompt` not null, `module` not null | O |
| | `ai_conversations` created (when `conversationId` absent) and `last_message_at` updated | O |
| | Archive happens in the **same transaction** as the call (`TransactionSynchronizationManager.isActualTransactionActive()`) | O |
| | `geminiClient.generate()` invoked exactly once — `AiAssistPort` is the only path | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | N |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

Note: fire-and-forget archiving (writing the row after the response is sent) would pass this test's "answer returned" check but fail its "same transaction" check — see `AIS-FUNC-002` for the negative case this guards against.

---

## Function: `AiArchiveService.archive()` — failure MUST rollback

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-002` | | Function Name | `AiArchiveService.archive` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | If the archive write fails, the whole operation must roll back and the client must receive NO answer — BR-157 allows no exception where a response is served without a trace (error code `AIS-157`, HTTP 500). (source: MF12-TC-002) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 1 | 0 | 1 | 0 | 1 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 |
|---|---|---|
| **Condition** | | |
| Precondition | `FX-022` Gemini succeeds | O |
| | `FX-025` `AiRequestRepository` throws `DataAccessException` on INSERT | O |
| Action | `service.ask(...)` | O |
| **Confirm** | | |
| Exception | `$.error.code == "AIS-157"`, HTTP 500 (⚠️ D7/L4: assert code only, `AIS-157` has no source MSG yet) | O |
| Return | **No** `answer` returned to the client, even though Gemini already produced content | O |
| | Transaction rolled back — `ai_conversations` row (if newly created in this call) is also gone | O |

### 4. Execution Result

| | UTCID01 |
|---|---|
| Type (N/A/B) | A |
| Passed/Failed | Untested |
| Executed Date | 2026-08-05 |
| Defect ID | |

---

## Function: `GeminiClient` timeout (10s) + `AiArchiveService` — TIMEOUT row

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-003` | | Function Name | `AiChatService.ask()` — AI timeout branch |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | AI response taking longer than the 10s timeout must return `AIS-073` (MSG-073) AND still write one `ai_requests` row with `status=TIMEOUT` — distinct from `AIS-FUNC-002` where the row must NOT exist (BR-157, UC-012 AF1). (source: MF12-TC-003) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 0 | 1 | 2 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Timeout configured at 10s (`FX-026`) | O | O | O |
| Fake Gemini delay | `9.9s` (just under the limit) | O | | |
| | `10s` (on-point) | | O | |
| | `11s` (`FX-024`, over the limit) | | | O |
| **Confirm** | | | | |
| Return | Success — row `status=SUCCESS` | O | O | |
| Exception | `$.error.code == "AIS-073"`, `$.error.message` = MSG-073 verbatim ("Gemini AI Service is currently unavailable. Please try again later."), HTTP 503 | | | O |
| Return | Row `ai_requests.status = TIMEOUT` written (1 row, NOT rolled back) | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | B | B | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note (UTCID02): the TestSpec's `@ParameterizedTest` includes the 10s on-point value but its Expected Result section only states outcomes for 9.9s (success) and 11s (timeout). This matrix assumes the on-point value (delay == timeout) still succeeds, consistent with the "not yet exceeded" boundary semantics used elsewhere in MF12 (e.g. `AIS-FUNC-007`). Confirm with dev before locking the assertion.

---

## Function: `PromptRedactor` (in `AiChatService`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-004` | | Function Name | `PromptRedactor` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 25 | | Lack of test cases | 0 |
| Test requirement | Any student code/name in a prompt must be redacted before it is sent to Gemini AND before it is persisted, while preserving business meaning (BR-173, BR-181 — PDPD Law 91/2025). (source: MF12-TC-004) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 1 | 0 | 1 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `FX-028` prompt = "Student HS-3A-001 Nguyen Minh Khang has a shrimp allergy, suggest an alternative dish" | O | |
| | Same prompt, additional student code pattern `HS-5B-007` embedded | | O |
| Action | `service.ask(prompt, actor)`; capture prompt sent via `ArgumentCaptor` on `geminiClient.generate()` | O | O |
| **Confirm** | | | |
| Return | Prompt sent to Gemini does NOT contain `"HS-3A-001"` nor `"Nguyen Minh Khang"` (regex `HS-\d+[A-Z]-\d+` matches none) | O | |
| | Business meaning preserved — phrase "shrimp allergy" still present so AI can still answer meaningfully | O | |
| | `ai_requests.prompt` stored in DB is also the redacted version | O | |
| | Any other student-code shape (e.g. `HS-5B-007`) is also redacted | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | B |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

---

## Function: `AiChatService.history()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-005` | | Function Name | `AiChatService.history` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | A user may only read their OWN AI conversations — no role, including System Admin, may read another user's conversation (§16 `GET /ai/conversations` = "Own" for all 7 roles; `AIS-005`/MSG-005). (source: MF12-TC-005) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-020` conversation owned by `caterer-001` | O | O | |
| | `FX-021` conversation owned by `medical-001` | | O | O |
| Actor / JWT | `caterer-001` lists own history | O | | |
| | `caterer-001` requests `medical-001`'s conversation by code | | O | |
| | `system-admin` requests another user's conversation | | | O |
| **Confirm** | | | | |
| Return | Only conversations belonging to `caterer-001` are returned (filter applied at repository/Specification layer on `user_id`) | O | | |
| Exception | HTTP 403, `$.error.code == "AIS-005"`, message = MSG-005 verbatim | | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | A | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note (UTCID03): §16 explicitly grants "Own" (not "All") to System Admin for this endpoint — there is no admin bypass. This is the case most likely to be loosened incorrectly during implementation.
