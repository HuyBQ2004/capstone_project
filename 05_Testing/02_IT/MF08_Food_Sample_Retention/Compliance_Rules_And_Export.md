# Integration Test Cases — MF08 Food Sample Retention
## Feature: Semantic Compliance Rules, Schema Contract, Mẫu số 5 Export, Retention Scheduler

> Stack: Testcontainers PostgreSQL 16 + real Flyway migration. Timeout 120s.
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-INT-011`…`MF08-TC-INT-014`

---

## Feature: Semantic Compliance Rules, Schema Contract, Mẫu số 5 Export, Retention Scheduler

### 1. General Information

| Field | Value |
|---|---|
| Feature | Cross-cutting compliance guarantees: the 10 `semantic-audit.ts` rules touching MF08, database schema shape (no derived columns, all `review.md` §F columns present), the Mẫu số 5 register export (UC-118), and the retention-deadline scheduler (BR-110/BR-111) |
| Test requirement | These are regression guardrails, not feature happy paths — most assert an invariant that must hold across the whole module rather than one endpoint. |
| Number of TCs | 13 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | `<#>` | `<#>` | 13 | `<#>` |
| Round 2 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 3 | `<#>` | `<#>` | `<#>` | `<#>` |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Function: semantic-audit.ts rules ported to MF08 (10 scenarios)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-011a` | `MAU_SAI_BUA` — the meal label's date must match the sample's collection date. | `POST /samples` with `mealShift` text dated 02/07 but `collectedAt = 2026-07-06`. | **422**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-011b` | `HAN_LUU_KHONG_DU_24H` — retention span is always exactly 24h. | Register any sample; query `retention_deadline - collected_at` for all rows. | Every row has a span of exactly 24 hours; no row has a span shorter than 24h. | — | Pending | | | Pending | | | Pending | | | The `semantic-audit.ts` rule name still says `_72H` — the correct amended value is 24h (BR-111, CF-M08-1). |
| `MF08-TC-INT-011c` | `GUI_LAB_TRUOC_KHI_LAY` — cannot send to lab before the sample was collected. | Attempt to write `sent_to_lab_at < collected_at`. | Blocked by CHECK `ck_smp_sent_after_collect`. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-011d` | `HUY_MAU_TRUOC_HAN_24H` — cannot dispose before the 24h deadline. | Attempt to write `disposed_at < retention_deadline`. | **409 `SMP-111`** at the service layer, and blocked by CHECK `ck_smp_dispose_after_deadline` at the DB layer. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-011e` | `MAU_NGOAI_KHUNG_SU_CO` — only samples within the incident's retrieval window are retrievable. | `GET /sample-lab-requests/retrievable?incidentId=INC-2026-014` where `INC-2026-014.occurredAt = 2026-06-30T04:00Z`. | Only samples with `collected_at` within the 24h window before `occurredAt` are returned; a same-dish sample collected 02/07 does **not** appear. | Seed samples inside and outside the window. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-011f` | `PHIEU_TRO_MAU_KHONG_TON_TAI` — cannot hand over a non-existent sample code. | `POST /sample-lab-requests` with `sampleIds` containing `"SMP-KHONG-CO"`. | **404 `SMP-404`**; no request created. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-011g` | `BAN_GIAO_TRUOC_KHI_LAY_MAU` — cannot hand over before the sample was collected. | A hand-over request with `requestedAt` earlier than one batch member's `collected_at`. | **422**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-011h` | `PHIEU_KHAC_SU_CO_VOI_MAU` — cannot re-attach a sample to a different incident's request. | With `SMP-A` already linked to `INC-2026-014`, hand it over under a request for a different incident. | **409 `SMP-113`**; `SMP-A.incident_id` unchanged. | — | Pending | | | Pending | | | Pending | | | Duplicated in `Lab_HandOver_And_Incident_Link.md` `INT-004b` — kept here too as part of the full 10-rule audit sweep. |
| `MF08-TC-INT-011i` | `MAU_KHONG_GAN_NGUOC_VE_PHIEU` — every sample in a successful batch links back to the request. | After a successful 3-sample hand-over, check `lab_request_id` on all 3. | All 3 samples' `lab_request_id` equal the created request's id — the relationship holds in both directions. | — | Pending | | | Pending | | | Pending | | | Structurally guaranteed by the single-transaction hand-over (ADR-M08-1) — this scenario asserts that guarantee holds. |
| `MF08-TC-INT-011j` | `PHIEU_THU_GIU_THIEU_CO_QUAN` — an authority hand-over must name the receiving authority. | `POST /sample-lab-requests` with `handoverType=AUTHORITY` but no `authorityId`. | **422**; blocked by CHECK `ck_labreq_authority`. | — | Pending | | | Pending | | | Pending | | | |
| **Function: Database schema contract** | | | | | | | | | | | | | | |
| `MF08-TC-INT-012a` | No derived columns exist on `food_samples`. | After Flyway migrate: `SELECT column_name FROM information_schema.columns WHERE table_name='food_samples' AND column_name IN ('hours_remaining','batch_ids')`. | **0 rows**. | Flyway migrated. | Pending | | | Pending | | | Pending | | | Confirm the `food_samples` table exists before asserting a column is absent — an empty schema also returns 0 rows and would falsely "pass." |
| `MF08-TC-INT-012b` | Every column `review.md` §F requires is present. | `SELECT column_name FROM information_schema.columns WHERE table_name='food_samples' AND column_name IN ('code','sample_amount','sample_unit','inspection_id','disposal_witness','temp_out_of_range','gps_lat','gps_lng')`. | **8 rows** — all present. | — | Pending | | | Pending | | | Pending | | | Missing `sample_amount`/`sample_unit` means BR-203 cannot be enforced server-side (finding F2, blocker); missing `inspection_id` means BR-105 has no data to gate on (F3); missing `disposal_witness` means legally-required witness data is lost (F5). |
| `MF08-TC-INT-012c` | `code` is unique per school. | `SELECT indexdef FROM pg_indexes WHERE tablename='food_samples' AND indexdef LIKE '%school_id%code%'`. | ≥1 matching index. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-012d` | All 8 CHECK constraints from TDS §5.2 exist. | `SELECT conname FROM pg_constraint WHERE conrelid='food_samples'::regclass AND contype='c'` (plus the same for `sample_lab_requests`). | Includes `ck_smp_amount`, `ck_smp_container_state`, `ck_smp_dispose_after_deadline`, `ck_smp_dispose_complete`, `ck_smp_sent_has_request`, `ck_smp_sent_after_collect`, `ck_labreq_criteria`, `ck_labreq_authority`. | — | Pending | | | Pending | | | Pending | | | |
| **Function: GET /api/v1/samples/export — Mẫu số 5 register export (UC-118)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-013a` | XLSX export has exactly the 12 Appendix 2 columns, in the mandated order. | `GET /samples/export?format=xlsx&dateFrom=2026-06-28&dateTo=2026-07-06`. | Columns in order: (1) No. · (2) Sample name · (3) Meal (time) · (4) Servings · (5) Weight/Volume · (6) Container · (7) Storage temperature · (8) Collection time · (9) Disposal time · (10) Note · (11) Collected by · (12) Disposed by. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-013b` | Export respects the date filter — it does not dump the entire register. | Same call as above, compare row count against samples actually collected within `[dateFrom, dateTo]`. | Only rows within range appear. | — | Pending | | | Pending | | | Pending | | | The mock UI currently only shows a toast for export, so this filter has never actually been exercised. |
| `MF08-TC-INT-013c` | Column (5) displays combined amount+unit; column (10) merges the disposal witness into the note for the printed form. | Inspect the generated file's column 5 and 10 values. | Column 5 shows `"120 g"` / `"180 ml"` (presentation-layer join of `amount`+`unit`; DB still stores them separately — see D12). Column 10 merges the witness into the note text for legal-form purposes, while the DB still holds `disposal_witness` in its own column. | — | Pending | | | Pending | | | Pending | | | This presentation-layer date format (`dd/MM/yyyy HH:mm`) is intentional and does not conflict with the JSON API's ISO-8601 rule (D4) — it only applies to the printed/exported file. |
| `MF08-TC-INT-013d` | PDF export produces the same row count and a PDF content type. | `GET /samples/export?format=pdf` with the same filters. | Correct `Content-Type`; row count matches the XLSX export for the same filter. | — | Pending | | | Pending | | | Pending | | | |
| **Function: RetentionDeadlineScheduler (ADR-M08-6)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-014a` | Expired, unlocked samples raise a retention-expired event; locked/terminal samples do not. | Advance the clock past FX-002/FX-003's deadlines and run the scheduler. | `FoodSampleRetentionExpired` fires for FX-002 and FX-003. It does **not** fire for FX-004 (linked to an open incident — or fires with `disposalLocked=true`) and does not fire for any `SENT_TO_LAB`/`DISPOSED` sample. | Seed FX-002…FX-006. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-014b` | Running the scheduler twice does not duplicate events. | Run the job a second time without advancing the clock further. | No duplicate `FoodSampleRetentionExpired` events for the same `sampleId` (idempotent). | `INT-014a` executed. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-014c` | No fridge-temperature monitoring job exists. | Scan all `@Scheduled` beans in `com.sfms.mf08`. | Exactly **1** scheduled bean (the retention scheduler); none related to `fridgeTemp`, a 30-minute poll, or MSG-059. | — | Pending | | | Pending | | | Pending | | | BR-110's automatic temperature-alert mechanism was removed 2026-07-11; reintroducing a polling job would resurrect a mechanism the business rule explicitly deleted, and there is no sensor feed to support it. |

---

## Filling Guide

1. This file groups every **module-wide invariant** check that isn't tied to a single endpoint's happy path — schema shape, cross-cutting business rules, the legally-mandated export, and the background scheduler.
2. The 10-scenario `semantic-audit.ts` sweep is the closest thing MF08 has to a regression contract against the original mock's known defects — do not remove scenarios from this list even if they duplicate an assertion made elsewhere (e.g. `INT-011h` vs `Lab_HandOver_And_Incident_Link.md INT-004b`); the sweep's value is in running all 10 together.
3. Update Round columns as the suite executes.
