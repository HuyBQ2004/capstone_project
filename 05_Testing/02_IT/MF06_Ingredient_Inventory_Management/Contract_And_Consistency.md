# Integration Test Case — Contract & Cross-Module Consistency (MF06 Ingredient & Inventory Management)

---

## Feature: API Contract & Schema Consistency (OpenAPI D8)

### 1. General Information

| Field | Value |
|---|---|
| Feature | API Contract & Schema Consistency (OpenAPI D8) |
| Test requirement | Verify the `GET /v3/api-docs` OpenAPI spec — MF06's source of truth for FE type generation (`openapi-typescript`) — never re-introduces a D1/D2/D4/D6 drift through an auto-generated channel: no `id`/`*Id` field declared as `format: uuid` and every business-code example matches the code pattern (D1); every 4xx/5xx response uses the shared `ErrorResponse` schema (D2); every enum matches the D6 SCREAMING_SNAKE catalog, notably `ShelfStatus` containing `IN_STOCK` not the stale `ACTIVE`; every date/datetime field is ISO-8601 typed with a matching example (D4); every list endpoint's `200` schema is `PagedResponse` with exactly the 5 pagination fields (S7); and every controller/operation carries the required D8 annotations (`@Tag`, UC+BR citation in the description, a single `bearerAuth` security scheme, public `/v3/api-docs` access). This whole group is written ahead of the annotations existing (Red Gate/TDD) and is expected to FAIL until TDS §9.2 and the controllers add them. |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 6 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Identity Contract — No UUIDs in the Public Spec (D1)** | | | | | | | | | | | | | | |
| MF06-TC-CON-001 | Test that no `id`/`*Id` property in the OpenAPI spec is typed/formatted as a UUID, and that every business-id example matches the business-code pattern. | 1. Fetch `GET /v3/api-docs`.<br>2. Scan `components.schemas.*.properties` for any property named `id`/`*Id` with `format: "uuid"`.<br>3. Check every `id`/`*Id` field's `example` against `^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$`.<br>4. Scan the whole spec for any `example` value that is a UUID string. | 1. Zero properties have `format: "uuid"`.<br>2. Every `id`/`*Id` example matches the business-code pattern (`BATCH-MP-8842`, `SUP-001`, `RCV-20260702-02`, `MENU-2026-27`).<br>3. No `example` anywhere in the spec is a bare UUID string. | Full MF06 controller/schema set deployed with OpenAPI generation active. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | S6; D1; Team Plan §2c-3b(a) |
| **Error Envelope Contract (D2)** | | | | | | | | | | | | | | |
| MF06-TC-CON-002 | Test that every 4xx/5xx response across every MF06 operation references the shared `ErrorResponse` schema with the exact D2 envelope shape. | 1. Fetch `GET /v3/api-docs`.<br>2. For every operation and every `4xx`/`5xx` response code, inspect `content["application/json"].schema.$ref`. | 1. Every 4xx/5xx response's schema `$ref` is `"#/components/schemas/ErrorResponse"`.<br>2. `ErrorResponse` has exactly 3 fields: `code`, `message`, `details`, with `details` declared as a non-nullable array. | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | S3; D2; Team Plan §2c-3b(b) |
| **Enum Contract — SCREAMING_SNAKE (D6)** | | | | | | | | | | | | | | |
| MF06-TC-CON-003 | Test that every enum in the spec matches the D6 catalog exactly, with special attention to MF06's own enums and the corrected `ShelfStatus` value. | 1. Fetch `GET /v3/api-docs`.<br>2. Inspect every `enum` array: `MealType`, `PartnerType`, `RoleKey`, `NotificationType`, `ConsumptionStatus`, `BiometricStatus`, `AllergySeverity`, `HealthHistoryEventType`, plus MF06-owned enums `StorageZone`, `TransactionType`, `ReasonCode`, `ReceivingStatus`, `IssueSlipStatus`, `ShelfStatus`.<br>3. Grep the source for `@Schema(allowableValues`. | 1. `MealType = [BREAKFAST, LUNCH, SNACK]` exactly — no `"Bữa chiều"`.<br>2. All 6 MF06-owned enums are entirely SCREAMING_SNAKE.<br>3. `ShelfStatus` contains `IN_STOCK`, never `ACTIVE` (L2).<br>4. `@Schema(allowableValues` returns 0 matches (enums must come from a real Java `enum` type, not hardcoded string lists). | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | S5; D6; Team Plan §2c-3b(c); L2/L4 |
| **Date/Time Format Contract (D4)** | | | | | | | | | | | | | | |
| MF06-TC-CON-004 | Test that every date/datetime-shaped property in the spec declares the correct OpenAPI type/format and an ISO-8601 example. | 1. Fetch `GET /v3/api-docs`.<br>2. Scan property names matching `.*(Date\|At\|date_.*\|.*_date\|.*_at)$`. | 1. Every matching property has `type: "string"` and `format` ∈ {`date`, `date-time`}.<br>2. Every such property's `example` is a valid ISO-8601 string.<br>3. No `example` anywhere matches `^\d{2}/\d{2}/\d{4}`. | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | S4; D4; Team Plan §2c-3b(d); L3 |
| **Pagination Shape Contract (§2b-1)** | | | | | | | | | | | | | | |
| MF06-TC-CON-005 | Test that every list `GET` operation across the 7 MF06 resources declares the shared, single, non-duplicated `PagedResponse` schema with exactly 5 fields. | 1. Fetch `GET /v3/api-docs`.<br>2. For all 7 list operations, inspect the `200` response schema.<br>3. Inspect `components.schemas` for duplicate inline `PagedResponse`-shaped schemas. | 1. Every list operation's `200` schema is `PagedResponse` with exactly 5 properties: `items`, `total`, `page`, `pageSize`, `pageCount` — no `data`/`result` wrapper.<br>2. `PagedResponse` is declared exactly ONCE in `components.schemas`, not duplicated inline per endpoint. | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | S7; Team Plan §2b-1 |
| **Operation & Security Annotations (S1/S2/S8)** | | | | | | | | | | | | | | |
| MF06-TC-CON-006 | Test that every controller and operation carries the required D8 annotations: correct tag grouping, UC+BR traceability in the description, a single bearer-auth security scheme, and public docs access. | 1. Fetch `GET /v3/api-docs`.<br>2. Inspect `@Tag` on every MF06 controller.<br>3. Inspect every operation's `description` for `UC-\d{3}` and `BR-\d{3}` patterns.<br>4. Inspect `components.securitySchemes`.<br>5. `GET /v3/api-docs` and `GET /swagger-ui/index.html` with NO Authorization header. | 1. Every MF06 controller is tagged `"MF06 — Ingredient & Inventory Management"` — one consistent group in swagger-ui.<br>2. Every operation's `description` contains ≥1 UC id AND ≥1 BR id (except UC-139, which has no BR in §1.5).<br>3. Exactly one `securitySchemes.bearerAuth` entry (`http`/`bearer`/`JWT`).<br>4. Both endpoints return HTTP 200 without a token (`permitAll`) — required for `openapi-typescript` to fetch the spec. | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | S1, S2, S8; D8 infrastructure note (without `permitAll`, the FE's `openapi-typescript` generation step cannot fetch the spec at all); ⚠️ this entire test group is written in RED/TDD state — as of TestSpec v1.1 no controller yet carries `@Operation`/`@Schema`/`@ApiResponse`/`@Tag` annotations, so all 6 cases are expected to FAIL until the annotations are added (this is the Red Gate working correctly, not a defective test); if D8 is ultimately not approved, this group is marked `@Disabled("D8 rejected")` rather than deleted |
