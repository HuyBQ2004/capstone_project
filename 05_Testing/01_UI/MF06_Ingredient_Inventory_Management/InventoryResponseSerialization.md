# Unit Test Case — InventoryResponseSerialization (MF06 Ingredient & Inventory Management)

---

## Function: `InventoryBatchMapper` / DTO Jackson serialization (`@JsonTest`)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `MF06-FUNC-009` | | Function Name | `InventoryBatchMapper` (MapStruct, `@JsonProperty("id")`) + response DTO Jackson config |
| Created By | `Bui Quang Huy` | | Executed By | `Huy` |
| Lines of code | `TBD` | | Lack of test cases | `TBD` |
| Test requirement | `The public id in JSON and in path params must always be the business code (e.g. "BATCH-MP-8842"), never the internal UUID PK (D1) — this includes foreign-key-shaped fields such as supplierId, which must resolve to the target table's code, not its UUID, since leaking a supplier UUID risks cross-supplier data exposure (D1 reason 3). All date/time fields must serialize as ISO-8601 ("2026-07-05" for LocalDate, "2026-07-02T06:00:00Z" for Instant) with zero @JsonFormat annotations left in package com.sfms.mf06 (D4, overriding ADR-M11-5). All enums must serialize/deserialize as SCREAMING_SNAKE values exactly as declared in the DB CHECK constraints (TransactionType, ReasonCode, StorageZone, MealType) — display labels never leak into the wire format (D6) — and an invalid enum literal on deserialize must reject with 400 MSG-138.` | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| `0` | `0` | `3` | `2` | `1` | `0` | `3` |

### 3. Condition / Confirmation Matrix

Each column is one **UTCID** (one test case). Mark `O` in the cell where the condition/expected value applies to that UTCID.

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `InventoryBatch{ id=<UUID>, code="BATCH-MP-8842", supplierId=<UUID>, supplierCode="SUP-001" }` | O | | |
| | DTO with `receivedDate: LocalDate`, `receivedAt: Instant`, `serviceDate: LocalDate` | | O | |
| | `TransactionType.ADJUSTMENT`, `ReasonCode.SPOILAGE`, `StorageZone.CHILLED`, `MealType.LUNCH`; also invalid input `"Chilled"` / `"spoilage"` to deserialize | | | O |
| Operation Input | Serialize the batch entity to JSON | O | | |
| | Serialize the DTO; grep package `com.sfms.mf06` for `@JsonFormat` | | O | |
| | Serialize each enum; then deserialize the invalid lower/mixed-case literals | | | O |
| **Confirm** | | | | |
| Return | `$.id = "BATCH-MP-8842"`; `$.supplierId = "SUP-001"`; no field in the JSON tree matches the UUID regex; existing business codes preserved unchanged (`BATCH-MP-8842`, `RCV-20260702-02`, `SUP-001`, `PR-2026-0815`, `MENU-2026-27` — D1: keep a module's existing code shape) | O | | |
| | `$.receivedDate="2026-07-05"`, `$.receivedAt="2026-07-02T06:00:00Z"`; no field matches `^\d{2}/\d{2}/\d{4}`; grep for `@JsonFormat` returns 0 results | | O | |
| | `"ADJUSTMENT"`, `"SPOILAGE"`, `"CHILLED"`, `"LUNCH"` (never `"Dieu chinh"`/`"spoilage"`/`"Chilled"`) | | | O |
| Exception | — | | | |
| | — | | | |
| | Deserializing `"Chilled"` / `"spoilage"` → `400 MSG-138`: *"Validation failed: one or more required fields are missing or invalid."* (asserted literally — MSG-138 is a shared v1.5 code, no ORACLE GAP) | | | O |
| Log message | — | | | |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

> UTCID mapping: UTCID01 = `M06-TC-017`, UTCID02 = `M06-TC-018`, UTCID03 = `M06-TC-019`.
