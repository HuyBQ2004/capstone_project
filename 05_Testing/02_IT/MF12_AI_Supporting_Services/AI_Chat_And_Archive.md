# Integration Test Case — AI Chat & Conversation Archive (MF12 AI & Supporting Services)

---

## Feature: AI Chat, Conversation Archive & Shared AI Gateway

### 1. General Information

| Field | Value |
|---|---|
| Feature | AI Chat, Conversation Archive & Shared AI Gateway |
| Test requirement | Verify the full round-trip of `POST /api/v1/ai/chat` (UC-012) against `ai_conversations`/`ai_requests`, and verify `AiAssistPort` — the single shared AI gateway (`ADR-M12-01`) used by other modules such as MF05's menu AI-suggest (UC-119, BR-157) — archives every AI call regardless of caller, and that MF12's `GeminiAiAssistAdapter` bean removes MF05's `503 MENU-073` fallback (`M12-D09`/L33). |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 3 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/ai/chat (×2, same conversationId)** | | | | | | | | | | | | | | |
| MF12-TC-INT-001 | Test a full 2-turn chat round trip: same `conversationId` across two calls creates exactly one conversation with two archived requests. | 1. `POST /ai/chat` without `conversationId`.<br>2. Extract `conversationId` from the response.<br>3. `POST /ai/chat` again with the same `conversationId`.<br>4. Assert DB state via `SELECT count(*) FROM ai_requests r JOIN ai_conversations c ON r.conversation_id = c.id WHERE c.code = ?` (D1: `WHERE code`).<br>5. `GET /ai/conversations/{code}`. | 1. Both calls → HTTP 200.<br>2. Exactly **1** row in `ai_conversations`, **2** rows in `ai_requests` pointing to the same conversation.<br>3. `last_message_at` equals the timestamp of turn 2, ISO-8601 (D4).<br>4. `ai_requests.prompt` is already redacted (cross-ref `MF12-TC-004`).<br>5. `GET /ai/conversations/{code}` returns 2 turns in chronological order. | `@SpringBootTest` + Testcontainers PostgreSQL; MF12 schema `V22`/`V23` applied. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | BR-157; blocked by L6 until AI chat is wired end-to-end |
| **AiAssistPort — Shared AI Gateway Used by Other Modules** | | | | | | | | | | | | | | |
| MF12-TC-INT-002 | Test that MF05's `POST /api/v1/menus/{code}/ai-suggest` routes through MF12's shared `AiAssistPort`, producing an `ai_requests` row with `module='MF05_MENU'`, and that MF05 never calls Gemini directly. | 1. Call MF05's `POST /api/v1/menus/{code}/ai-suggest`.<br>2. Inspect `ai_requests` in MF12.<br>3. Static check: `grep -rniE "generativelanguage\|GeminiClient\|GEMINI_API_KEY" src/main/java/com/sfms/menu/`. | 1. Exactly 1 `ai_requests` row with `module = 'MF05_MENU'`, `prompt` redacted, `status=SUCCESS`.<br>2. Static grep → no output (`ADR-M05-03` + `ADR-M12-01`); MF05 never imports the Gemini SDK.<br>3. MF05's prompt is redacted by the shared port, not by MF05 itself.<br>4. When AI fails, a `status=TIMEOUT` row is still written (cross-ref `MF12-TC-003`), and MF05 still allows manual entry (cross-ref `MF05-TC-014`). | Both MF05 and MF12 deployed together; `FX-022` fake Gemini client. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | CRITICAL; BR-157, BR-164; boundary MF12↔MF05, cross-ref MF05-TC-014/015; blocked by L6 + MF05 CF-13 |
| MF12-TC-INT-022 | Test that `com.sfms.mf12.service.GeminiAiAssistAdapter` is the single Spring bean implementing MF05's `AiAssistPort` interface, removing MF05's previous `503 MENU-073` fallback, and that archive failure rolls back the whole AI-suggest call (no ungoverned AI response). | 1. `applicationContext.getBeanProvider(AiAssistPort.class)` → assert exactly 1 bean.<br>2. `POST /api/v1/menus/{code}/ai-suggest` with a real seeded account.<br>3. Count `ai_requests` before/after.<br>4. Read the newly written `ai_requests` row.<br>5. Simulate the adapter throwing during archive write → repeat step 2. | 1. Exactly **1** bean of type `GeminiAiAssistAdapter`, in package `com.sfms.mf12` (not `mf05` — MF05 only declares the interface).<br>2. `POST .../ai-suggest` no longer returns `503 MENU-073`; returns HTTP 200 with `suggestions[]` matching `AiSuggestionResponse` schema.<br>3. `ai_requests` count increases by exactly **1** (C1: every AI call across every module produces exactly one row).<br>4. New row has `module = "MF05_MENU"`, `code` matching `AIQ-<year>-<seq>` (`M12-D14`), `redacted = true`, `status = SUCCESS`, `response_text IS NOT NULL` (constraint `ck_ai_req_success_has_response`).<br>5. On archive failure → the whole call rolls back: error returned, `ai_requests` count unchanged, and MF05 returns no suggestion (C1 — no unarchived AI response is ever surfaced). | `AiAssistPort` interface published by MF05; MF12's Gemini client in stub mode (`ADR-000`); ≥1 MF05 menu seeded. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | HIGH; C1, BR-156/BR-157; ADR-M05-03, ADR-M12-01, M12-D09/L33 |
