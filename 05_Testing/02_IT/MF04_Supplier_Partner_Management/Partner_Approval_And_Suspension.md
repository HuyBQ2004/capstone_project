# Integration Test Case — Partner Approval & Suspension Lifecycle (MF04 Supplier & Partner Management)

---

## Feature: Approve / Reject / Suspend / Reinstate — Single-Transaction Consistency Across 3 Tables

### 1. General Information

| Field | Value |
|---|---|
| Feature | Approve / Reject / Suspend / Reinstate — Single-Transaction Consistency Across 3 Tables |
| Test requirement | Verify that `approve()` updates `partnership_applications`, `supplier_partnerships` and `partners` in one `@Transactional` (closing `CF-M04-9`, where the mock updates only `partners` and leaves the application `PENDING` forever); that approval is blocked with `409 SUP-013` and fully rolled back when the partner still has an `EXPIRED` document; that `suspend()` cascades to lock every user account tied to the partner (`users.partnerId`, ADR-M04-6, C5); and that `reinstate()` is blocked while an urgent/expired document remains. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 4 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Approve — Updates Application, Partnership and Partner in One Transaction** | | | | | | | | | | | | | | |
| MF04-TC-INT-001 | Test that approving an application updates `partnership_applications`, `supplier_partnerships`, and `partners` atomically, with audit and event emitted, and `created_by`/`updated_by` auto-filled by `AuditorAware`. | 1. `POST /api/v1/partners/SUP-005/approve` as School Admin `SCH-HN-0042` (path param is the **business code**, D1).<br>2. Read HTTP response.<br>3. Query `partnership_applications`, `supplier_partnerships`, `partners` directly.<br>4. Query `audit_logs`. | 1. HTTP 201/200 accepted.<br>2. Body `{"id":"SUP-005","status":"APPROVED"}` — `id` is the code, not a UUID.<br>3. `APP-2026-05.status=APPROVED`, `reviewed_by`/`reviewed_at` set; `supplier_partnerships(SUP-005,SCH-HN-0042).status=APPROVED`, `since=2026-07-28`; `partners.SUP-005.status=APPROVED`, `reject_reason=NULL`.<br>4. 1 `audit_logs` row, `resource='Supplier Management'`, `created_by`/`updated_by` = actor's email though the service never sets it manually; exactly 1 `PartnerApproved(partnerCode="SUP-005")` published. | Testcontainers PostgreSQL 16, Flyway migrated; Seed FX-002 (`SUP-005` PENDING, all documents VALID, `APP-2026-05` PENDING with `SCH-HN-0042`); JWT FX-011. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-007 Normal Flow steps 7+9, MSG-037, ADR-M04-2, CF-M04-9, D9/L10 |
| MF04-TC-INT-002 | Test that approval is blocked with `409 SUP-013` when the partner still has an expired food-safety certificate, and that none of the 3 tables change (full rollback). | 1. `POST /api/v1/partners/SUP-004/approve`.<br>2. Read response.<br>3. Re-query all 3 tables. | 1. HTTP 409, envelope `{"error":{"code":"SUP-013","message":"Supplier food safety certificate has expired. Onboarding blocked.","details":[...]}}`.<br>2. Matches step 1.<br>3. `partners.SUP-004.status` still `SUSPENDED`; `partnership_applications` unchanged; `supplier_partnerships` has no new row; no `PartnerApproved` event; no "APPROVE" audit entry. | FX-003 (`SUP-004`, food-safety cert expired `2026-06-22`), has an open application with the actor's school. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | BR-046, UC-007 AF1, MSG-013 ⚠️ (outside §1.4 — CF-M04-2); if this table stays partially updated on failure, transaction boundary does not cover all 3 tables |
| **Suspend / Reinstate — Cascades Account Lockout (ADR-M04-6 / C5)** | | | | | | | | | | | | | | |
| MF04-TC-INT-003 | Test that suspending a partner locks every user account tied to it in the same operation, and that suspension without a reason is rejected. | 1. `POST /api/v1/partners/SUP-004/suspend {"reason":"Lô tôm BATCH-DD-1092 không đạt kiểm tra tiếp nhận 30/06."}`.<br>2. Query `partners` and `users`.<br>3. Query `PartnerLookupPort.isApproved(SUP-004)`.<br>4. Repeat step 1 with no `reason`. | 1. HTTP 200, body `suspendedAccounts >= 1`.<br>2. `partners.SUP-004.status=SUSPENDED`, `suspend_reason` stored verbatim; `users.U-0007.status=SUSPENDED` in the same transaction/event flow (not a client-side loop, unlike the mock).<br>3. Returns `false` — MF06 blocks receiving from this partner (BR-070).<br>4. HTTP 422 `SUP-048`; no account is locked. | FX-003 (`SUP-004` APPROVED); FX-008 (`U-0007`, `partner_id=SUP-004`, ACTIVE). Depends on MF01's `users.partner_id`/JWT claim `partnerId` (L11 — closed per M04-D13). | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | Constraint C5 `data_model.puml`, ADR-M04-6, BR-048; genuine 🔴 if MF01's `partner_id` column is missing — not a test defect |
| MF04-TC-INT-004 | Test that reinstating a suspended partner is blocked while a document is still `URGENT`/`EXPIRED`, and succeeds once a valid replacement is uploaded. | 1. `POST /api/v1/partners/SUP-004/reinstate` while the food-safety cert is still expired.<br>2. Upload a valid replacement certificate (UC-091).<br>3. `POST /api/v1/partners/SUP-004/reinstate` again. | 1. HTTP 409 `SUP-013`; `partners.SUP-004.status` still `SUSPENDED`; `users.U-0007` still `SUSPENDED`.<br>2. Upload succeeds.<br>3. HTTP 200; `status=APPROVED`, `suspend_reason=NULL`; `users.U-0007` back to `ACTIVE`. | Same as MF04-TC-INT-003, run sequentially after it. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | ADR-M04-6, BR-046; mock's "Reinstate" button is `disabled` under the same condition |
