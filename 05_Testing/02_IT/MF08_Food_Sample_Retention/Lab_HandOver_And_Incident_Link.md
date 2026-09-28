# Integration Test Cases — MF08 Food Sample Retention
## Feature: Lab Hand-Over, Disposal Lock, and Incident Linking (UC-030, UC-076)

> Stack: Testcontainers PostgreSQL 16 + real Flyway migration. Timeout 120s.
> Traceability: `TestSpec-MF08_Food_Sample_Retention.md` §4 — `MF08-TC-INT-002`…`MF08-TC-INT-006`, `MF08-TC-INT-015`

---

## Feature: Lab Hand-Over, Disposal Lock, and Incident Linking

### 1. General Information

| Field | Value |
|---|---|
| Feature | Sample hand-over to lab, disposal lock/unlock by incident lifecycle, and disposed-sample metadata linking (UC-030, UC-076) |
| Test requirement | A batch hand-over must be atomic (ADR-M08-1): the request record, the N sample status transitions, and the attachments all succeed or all roll back together. Disposal must stay locked while an incident is open and unlock the moment it closes (BR-112). Concurrent hand-over attempts on the same sample must not both succeed (ADR-M08-2, terminal state). |
| Number of TCs | 12 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | `<#>` | `<#>` | 12 | `<#>` |
| Round 2 | `<#>` | `<#>` | `<#>` | `<#>` |
| Round 3 | `<#>` | `<#>` | `<#>` | `<#>` |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Function: POST /api/v1/sample-lab-requests — Atomic batch hand-over (ADR-M08-1)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-002a` | Happy path: hand over 3 `RETAINED` samples of the same open incident in one request. | `POST /sample-lab-requests` with `sampleIds=[3 samples of INC-2026-014]`, `handoverRecords=@bien-ban-giao-nhan.pdf`, as Medical Staff. | HTTP **201**; `sample_lab_requests` +1 row `status=SENT_TO_LAB`; all 3 samples now `status=SENT_TO_LAB`, `lab_request_id` points to the new request, `sent_to_lab_at` = request's `requestedAt`, `incident_id = INC-2026-014`; +1 `HANDOVER_RECORD` attachment. | Seed FX-004 (2 samples, incident OPEN) + 1 more `RETAINED` sample of the same incident. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-002b` | Rollback path: same payload but 0 `HANDOVER_RECORD` files attached. | `POST /sample-lab-requests` with `sampleIds` valid but no hand-over evidence file. | **422 `SMP-212`**; **0 new rows** in `sample_lab_requests`, `food_samples` status, and `sample_attachments` — full rollback. | `INT-002a` fixtures. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-002c` | Rollback path: one sample in the batch is already `SENT_TO_LAB`. | `POST /sample-lab-requests` where sample #3 of the batch is `FX-005` (already `SENT_TO_LAB`). | **409 `SMP-113`**; **0 new rows** anywhere; the 2 otherwise-eligible samples remain `RETAINED` (no partial state change). | `INT-002a` fixtures + FX-005. | Pending | | | Pending | | | Pending | | | A partial success here would mean the register and the hand-over request disagree about the same physical box — the `MAU_KHONG_GAN_NGUOC_VE_PHIEU` semantic rule. |
| **Function: POST /api/v1/sample-lab-requests — Incident validity (BR-114)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-003a` | Hand-over against an open incident succeeds. | `POST /sample-lab-requests` with `incidentId=INC-2026-014` (OPEN). | **201**. | FX-009 stub: `INC-2026-014` OPEN. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-003b` | Hand-over against a closed incident is rejected. | `POST /sample-lab-requests` with `incidentId=INC-2026-012` (CLOSED). | **409 `SMP-023`**; no request created, no sample status change. Assert only `error.code`, **not** the literal MSG-023 text (see note). | FX-009 stub: `INC-2026-012` CLOSED. | Pending | | | Pending | | | Pending | | | `SMP-023` currently borrows MSG-023's text, which describes a different scenario (incomplete incident report) — this is a known mapping gap (L12/CF-M08-6) pending a dedicated message code from the traceability owner. |
| `MF08-TC-INT-003c` | Hand-over against a non-existent incident is rejected. | `POST /sample-lab-requests` with `incidentId=INC-9999`. | **409 `SMP-023`**. | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-003d` | `incidentId` is a mandatory field. | `POST /sample-lab-requests` with `incidentId=null`. | **422** (required field). | — | Pending | | | Pending | | | Pending | | | |
| **Function: No second hand-over of the same sample (BR-113, ADR-M08-2)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-004a` | A sample already sent to a lab cannot be handed over again for the same incident. | 1. Hand over `[SMP-A, SMP-B]` → `LABREQ-X`.<br>2. Hand over `SMP-A` again into a **new** request for the same incident. | Step 2: **409 `SMP-113`**; `SMP-A` keeps `lab_request_id = LABREQ-X` and its original `incident_id` (not overwritten). | — | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-004b` | A sample already sent to a lab cannot be claimed by a different incident's request. | Hand over `SMP-A` (already `SENT_TO_LAB`) into a request for a **different** open incident. | **409 `SMP-113`**; `incident_id` unchanged. | `INT-004a` executed. | Pending | | | Pending | | | Pending | | | Overwriting `incident_id` here is the `PHIEU_KHAC_SU_CO_VOI_MAU` rule made real — the original incident would silently lose its sample. |
| **Function: Disposal lock/unlock over an incident's lifecycle (BR-112)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-005a` | Disposal is blocked while the linked incident is open, even past the retention deadline. | `POST /samples/{code}/dispose` for `FX-004` (past 24h, linked to `INC-2026-014` OPEN). | **409 `SMP-112`**. | Seed FX-004. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-005b` | Disposal unlocks the moment the linked incident closes. | 1. Publish `IncidentClosed(INC-2026-014)`.<br>2. Retry `POST /samples/{code}/dispose` for the same sample. | **200**; `status = DISPOSED`; DB row has all 4 mandatory fields (`disposed_at`, `disposed_by`, `disposal_witness`, `disposal_note`) — CHECK `ck_smp_dispose_complete` cannot be bypassed. | `INT-005a` executed. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-005c` | A sample that hasn't reached its retention deadline yet is blocked for the deadline reason, not the incident reason. | `POST /samples/{code}/dispose` for `FX-002` (not yet 24h, no linked incident). | **409 `SMP-111`**. | Seed FX-002. | Pending | | | Pending | | | Pending | | | Unlocking disposal before an open incident closes would destroy evidence for an active investigation — flagged in the TDD spec as a DPO-notify condition. |
| **Function: POST /api/v1/samples/{code}/incident-link — Metadata link on a disposed sample (UC-076 AF1)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-006a` | Linking a disposed sample to an open incident records metadata only. | `POST /samples/{code}/incident-link` for `FX-006` (DISPOSED) + `INC-2026-014` (OPEN). | **200**; `incident_id` recorded; `status` **stays** `DISPOSED`; `lab_request_id` remains null; no attachment created; `DisposedSampleLinkedToIncident` event published once. | Seed FX-006. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-006b` | Linking against a closed incident is rejected. | Same sample, `incidentId=INC-2026-012` (CLOSED). | **409 `SMP-023`**. | `INT-006a` fixtures. | Pending | | | Pending | | | Pending | | | |
| `MF08-TC-INT-006c` | A still-`RETAINED` sample cannot use this metadata-link shortcut. | `POST /samples/{code}/incident-link` for a `RETAINED` sample. | **409** — a retained sample must go through the real hand-over (`POST /sample-lab-requests`), not the metadata-link endpoint. | — | Pending | | | Pending | | | Pending | | | Allowing a disposed sample's `status` to change here would make the record claim the sample is "being sent to lab" days after it was physically destroyed. |
| **Function: Concurrent hand-over race (ADR-M08-1, ADR-M08-2)** | | | | | | | | | | | | | | |
| `MF08-TC-INT-015a` | Two simultaneous hand-over requests for the same sample to different recipients — only one may win. | 2 threads `POST /sample-lab-requests` concurrently, both with `sampleIds=[SMP-A]`, one targeting CDC and one NIFC. | Exactly **1** request returns 201; the other returns **409 `SMP-113`**. `sample_lab_requests` count increases by exactly 1; `SMP-A.lab_request_id` points to only the winning request. | `SMP-A` is `RETAINED`. | Pending | | | Pending | | | Pending | | | Because `SENT_TO_LAB` is terminal (ADR-M08-2), a lost race here has **no repair path** — the row-level lock (`SELECT ... FOR UPDATE`) must be verified under real concurrency, not just check-then-act logic. |

---

## Filling Guide

1. Each `Function` group covers one API operation or lifecycle mechanism; rows map 1:1 to scenarios inside `MF08-TC-INT-002` through `MF08-TC-INT-006` and `MF08-TC-INT-015` of the TDD test specification.
2. **DB Assertion** style checks (row counts, FK values) referenced in the Test Case Procedure/Expected Results should be run directly against the Testcontainers database, not inferred from the HTTP response alone.
3. Update Round columns as the suite executes; keep the **Note** column intact when re-filling — it carries the compliance reasoning behind each oracle.
