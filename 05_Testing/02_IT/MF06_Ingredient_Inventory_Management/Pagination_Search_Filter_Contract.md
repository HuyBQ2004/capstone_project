# Integration Test Case — Pagination, Search & Filter Contract (MF06 Ingredient & Inventory Management)

---

## Feature: Pagination, Search & Filter Contract

### 1. General Information

| Field | Value |
|---|---|
| Feature | Pagination, Search & Filter Contract |
| Test requirement | Verify the module-wide 5-field pagination shape (`items`/`total`/`page`/`pageSize`/`pageCount`) is invariant across all 7 MF06 list endpoints, including empty-result-set → 200 (never 404), `page`/`pageSize` clamping, the `not:field` negation filter operator, the `SEARCH_FIELDS` allow-list (case-insensitive `ILIKE`, never a full-column scan), that all 6 MF06-owned resource tables use `id UUID` PK + `code VARCHAR(32)` + `UNIQUE(school_id, code)` (D1/L1), that the third `shelfStatus` value is `IN_STOCK` (not the stale `ACTIVE`, L2), and that a date-range filter still returns results after the seed's `dd/MM/yyyy` → ISO-8601 conversion (the D4 silent-empty-result trap). |
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
| **Pagination Shape & Negative Filter (`not:`)** | | | | | | | | | | | | | | |
| MF06-TC-INT-025 | Test that an empty result set returns HTTP 200 with the full 5-field shape (never 404/wrapper), and that the `not:` negation operator correctly excludes matching rows using SCREAMING_SNAKE values. | 1. `GET /api/v1/inventory?status=KHONG_TON_TAI` (a status value with no matches).<br>2. `GET /api/v1/inventory?not:storageZone=DRY`. | 1. Step 1 → HTTP 200, body exactly `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — no 404, no `data`/`result` wrapper.<br>2. Step 2 → excludes exactly the `DRY` batches; the filter value is SCREAMING_SNAKE (`DRY`), not the legacy `"Dry"` shown in the old TDS §13 example IT-M06-25. | Seeded inventory across multiple `storageZone` values. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | §2b-1 (invariant shape); §2b-6 (empty set → 200, `pageCount ≥ 1`); §2b-2 (`not:` negation); D6 |
| MF06-TC-INT-026 | Test that a date-range filter still returns matching rows after the mock's `dd/MM/yyyy` seed values were converted to ISO-8601 — the known "silent empty result" trap of D4. | 1. `GET /api/v1/inventory-transactions?dateFrom=2026-07-06&dateTo=2026-07-06`. | 1. Only rows with `service_date=2026-07-06` are returned.<br>2. `total > 0` (seed has `TXN-20260706-06/07/08`) — if the filter still parses dates as `dd/MM/yyyy` after the ISO conversion, the result is silently empty with no error; `total > 0` is the only assertion that catches this. | Seed `TXN-20260706-06/07/08` present. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | §2b-5 (`inventory-transactions` → filters on `service_date`); catches the D4 conversion trap noted in TDS-05 |
| **Identity Contract (D1) & shelfStatus Enum Fix (L2)** | | | | | | | | | | | | | | |
| MF06-TC-INT-027 | Test that every MF06-owned table uses `id UUID` as the internal PK plus a separate business `code VARCHAR(32)` with a per-school uniqueness constraint. | 1. Query `information_schema` for all 7 MF06 tables (`inventory_batches`, `receiving_sessions`, `purchase_requests`, `deliveries`, `supplier_batches`, `preparation_records`, `issue_slips`). | 1. Every table has `id uuid` PK.<br>2. Every table has `code varchar(32) NOT NULL`.<br>3. Every table has `UNIQUE(school_id, code)`. | Full MF06 schema applied. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | D1; "Việc phải làm ở từng TDS" table (MF05/MF06/MF07/MF12 must convert PK `VARCHAR` → `UUID` + split out `code`); L1 |
| MF06-TC-INT-028 | Test that `shelfStatus` uses the corrected 3-value enum (`IN_STOCK`, not the stale `ACTIVE`), and that no `status` column exists on `inventory_batches` (the value is always derived). | 1. `GET /api/v1/inventory`.<br>2. Inspect `information_schema.columns` for `inventory_batches`. | 1. A batch with >2 days until expiry returns `$.items[?].status="IN_STOCK"`.<br>2. A batch with ≤2 days returns `"EXPIRING_SOON"`; an expired batch returns `"EXPIRED"`.<br>3. No item ever returns `"ACTIVE"`.<br>4. `inventory_batches` has no `status` column (it is derived, not stored). | Batches at each shelf-status boundary. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | ADR-M06-2 (fixed 2026-07-29, review I4); CF-M06-11 (no stored `status` column); L2; AP-AI-009/AP-AI-012 |
| **Pagination Contract Across All 7 Resources & Case-Insensitive Search** | | | | | | | | | | | | | | |
| MF06-TC-INT-029 | Test that ALL 7 MF06 list endpoints share the identical pagination contract, date format, and case-insensitive search behavior. | 1. For each of the 7 `GET` list endpoints: check the 5-field shape; `?page=99` clamps to `pageCount`; `?pageSize=0` clamps to `1`.<br>2. Scan every field in the full JSON tree for `dd/MM/yyyy`-shaped strings.<br>3. `?search=THỊT LỢN` on `/inventory`. | 1. (a) exactly the 5 pagination fields present on every resource.<br>2. (b) `page` clamped correctly; (c) `pageSize=0` → clamped to `1`.<br>3. (d) no field anywhere matches `^\d{2}/\d{2}/\d{4}`.<br>4. (e) search is case-insensitive `ILIKE`: `?search=THỊT LỢN` matches the batch labeled `"Thịt lợn nạc vai"`. | All 7 resources seeded. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | §2b-1/§2b-6; §2b-7 checklist; D4; TDS §8.2 `SEARCH_FIELDS` (`inventory` → `id, ingredient_label, supplier_label` — real columns, CF-M06-9). Note: accent-insensitive search (`"thit lon"` matching `"Thịt lợn"`) is explicitly OUT of scope — no `unaccent` extension in the §2b contract |
| **Server-Authoritative Timestamp (BR-073)** | | | | | | | | | | | | | | |
| MF06-TC-INT-030 | Test that `receivedAt` and device info are always server-recorded, never accepted from the client, even when the client supplies plausible-looking values. | 1. `POST /receiving-sessions` with body `receivedAt: "2026-07-01T00:00:00Z"` (a past date) and a fabricated `deviceInfo`. | 1. DB `received_at` equals the server clock value (`FX-CLOCK` = `2026-07-06T03:00:00Z`), never the client-supplied value. | `FX-CLOCK` fixed at `2026-07-06T03:00:00Z`. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | BR-073 verbatim ("the system automatically records GPS coordinates, device info, and timestamp… an anti-fraud measure"); ADR-M06-7; AP-AI-006 |
