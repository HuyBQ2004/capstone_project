# Integration Test Cases — MF08 Food Sample Retention
## Feature: Food Sample Registration (UC-069)

> Stack: Testcontainers PostgreSQL 16 + real Flyway migration (8 CHECK constraints from TDS §5.2 must run). Timeout 120s.
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-INT-001`, `MF08-TC-INT-016`, `MF08-TC-INT-017`

---

## Feature: Food Sample Registration

### 1. General Information

| Field | Value |
|---|---|
| Feature | Food Sample Registration (UC-069) |
| Test requirement | End-to-end registration: `POST /api/v1/samples` must pass all validation gates (BR-105/107/108/109/110/203), persist a `RETAINED` sample with a server-computed 24h retention deadline, enforce one sample per dish per inspection session (QĐ 1246 Điều 7.1), and correctly populate the reserved audit columns (`created_by`/`updated_by`, D9). |
| Number of TCs | 8 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | `<#>` | `<#>` | 8 | `<#>` |
| Round 2 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 3 | `<#>` | `<#>` | `<#>` | `<#>` |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Function: POST /api/v1/samples — Happy path** | | | | | | | | | | | | | | |
| `MF08-TC-INT-001a` | Register a food sample end-to-end with a valid multipart payload and evidence photo. | 1. Seed FX-002…FX-008, stub FX-009/010/011/012, fix clock at FX-001.<br>2. `POST /api/v1/samples` multipart: `payload` (valid JSON) + `evidencePhotos=@mau-com-trang.jpg`, as Service Caterers (FX-014). | - HTTP **201**; body has `id` = business code (D1), `status: "RETAINED"`, `retentionDeadline` in ISO-8601 = `collectedAt + 24h`, runtime-computed `hoursRemaining`.<br>- Response carries **MSG-044** ("Food sample retention registered. Cold storage timer initiated."). | Testcontainers PG16 + Flyway migrated; MF07 inspection session `INS-2026-0706-T` PASSED 3/3; geofence stub returns inside-campus. | Pending | | | Pending | | | Pending | | | UC-069 postcondition text still says "72-hour timer" — the correct oracle is BR-111 = 24h (CF-M08-1). |
| `MF08-TC-INT-001b` | Verify the created row in `food_samples` and its evidence attachment directly in the database. | 1. Continue from `INT-001a`.<br>2. `SELECT status, retention_deadline - collected_at AS span, temp_out_of_range FROM food_samples WHERE code = 'SMP-20260706-05'`.<br>3. `SELECT count(*) FROM sample_attachments WHERE owner_type='SAMPLE' AND kind='EVIDENCE_PHOTO'`. | - Row: `RETAINED \| 24:00:00 \| false`.<br>- `school_id` = JWT's school; `inspection_id` = value sent (BR-105/BR-114); `dish_id` not null.<br>- Exactly **1** `EVIDENCE_PHOTO` attachment, `checksum_sha256` matches the uploaded file. | `INT-001a` executed. | Pending | | | Pending | | | Pending | | | If `span` ≠ 24:00:00, a stale 72h constant is still present somewhere. |
| `MF08-TC-INT-001c` | Verify the domain event and audit log entry fire exactly once. | 1. Continue from `INT-001a`.<br>2. Inspect published events and the audit log table. | - `FoodSampleRegistered` event published exactly once.<br>- Audit log has a `CREATE FoodSample SMP-...` entry with actor + IP (matches the shape of mock seed `AUD-99120`). | `INT-001a` executed. | Pending | | | Pending | | | Pending | | | |
| **Function: One sample per dish per inspection session (Điều 7.1)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-016a` | Registering the same dish twice under the same inspection session is rejected. | 1. Register "Cơm trắng" for `INS-2026-0706-T` → expect 201.<br>2. Register "Cơm trắng" again for the **same** `INS-2026-0706-T`. | - Step 1: **201**.<br>- Step 2: **409** — blocked by unique index `uq_smp_inspection_dish(inspection_id, dish_id)`. | `INT-001a` fixtures available. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-016b` | A different dish in the same session, or the same dish in a different session, is allowed. | 1. Register "Canh bí đỏ" for the same session `INS-2026-0706-T`.<br>2. Register "Cơm trắng" for a **different** session. | Both requests return **201**. | `INT-016a` executed. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-016c` | Two concurrent requests for the same (session, dish) pair — only one may succeed. | Fire 2 threads with identical `(inspectionId, dishId)` simultaneously. | Exactly **1** request succeeds (201); the other gets **409**. The guard must be the DB unique index, not a check-then-act race in the service. | `INT-016a` executed. | Pending | | | Pending | | | Pending | | | Duplicate rows would make the Mẫu số 5 register show two entries for one dish, and an incident investigation would not know which box is the real sample. |
| **Function: Audit columns via BaseEntity (D9)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-017a` | `created_by` is populated by `AuditorAware`, never set manually by the service. | 1. `POST /samples` with JWT `sub = "maile@anbinhcatering.vn"`.<br>2. Read `created_by` from the DB row.<br>3. Grep `com.sfms.mf08` source for `setCreatedBy`. | - `created_by` = the actor's email, matching the JWT subject.<br>- Grep for `setCreatedBy` returns **0 results** — the value comes only from `AuditorAware`, not manual service code. | `INT-001a` fixtures available. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-017b` | `updated_by` changes on a later update by a different actor; `created_by` never does. | 1. `PATCH /samples/{code}/storage` using a **different** authenticated account. | - `updated_by` reflects the new actor.<br>- `created_by` is unchanged from registration. | `INT-017a` executed. | Pending | | | Pending | | | Pending | | | 4 distinct "who" concepts coexist on this entity: `collected_by`/`disposed_by`/`delivered_by` are business fields (Mẫu số 5), while `created_by`/`updated_by` are the D9 reserved audit columns — do not conflate them. |

---

## Filling Guide

1. Each `Function` group header corresponds to one production endpoint/mechanism under test; individual rows are the specific scenarios from `MF08-TC-INT-001`, `MF08-TC-INT-016`, `MF08-TC-INT-017` of the TDD test specification.
2. Update **Round 1/2/3** to `Passed`/`Failed` as the suite is actually executed against a live Testcontainers environment; fill in **Test date** and **Tester**.
3. The **Note** column carries the compliance/regression rationale so a failure is immediately actionable without re-reading the full TDD spec.
