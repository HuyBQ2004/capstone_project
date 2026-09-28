# Integration Test Case — Preparation Records / Cook Log Invariants (MF06 Ingredient & Inventory Management)

---

## Feature: Preparation Records (Cook Log) Invariants

### 1. General Information

| Field | Value |
|---|---|
| Feature | Preparation Records (Cook Log) Invariants |
| Test requirement | Verify the 4 `semantic-audit.ts` rules under script section §8 ("Sổ chế biến" / cook log) that TDS §13.0 had originally omitted (L7 — 19 rules documented vs. 24 actually in the script): a preparation record must never use an expired batch (BR-205), every batch used must trace back to a real issue slip (BR-076), every batch referenced must exist (FK, C6), and a preparation record's date must match its menu's date (BR-076). These resources are owned by MF06 even though the Step-1/Step-2 inspection forms themselves belong to MF07 (CF-M06-1/CF-M06-2). |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 5 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Preparation Record (Cook Log) Invariants — §8 semantic-audit.ts rules missed by TDS §13.0 (L7)** | | | | | | | | | | | | | | |
| MF06-TC-INT-018 | Test the `semantic-audit.ts` rule `CHE_BIEN_DUNG_LO_HET_HAN`: no preparation record used a batch that was already expired on its cook date, and the write path blocks this even when called directly. | 1. Given `FX-PREP-OK` (`PREP-20260706-01`, `date=2026-07-06`, `batchesUsed=[BATCH-MP-8842, …]`), run the invariant query joining `preparation_records`/`preparation_batches` to `inventory_batches` on `expiry_date < prep.date`.<br>2. `POST /api/v1/preparation-records` with `batchesUsed` containing a batch already expired at `date`. | 1. Step 1 → `rowsChecked > 0` and `violations` is empty.<br>2. Step 2 → HTTP 409, `error.code="ING-205"`. | `FX-PREP-OK`. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | Rule `CHE_BIEN_DUNG_LO_HET_HAN` (§8, verbatim from script); BR-205 verbatim (covers the second half of the rule — "prevents selection for issuance OR use during preparation" — that TDS §13 had not covered); ⚠️ ORACLE GAP (L6) |
| MF06-TC-INT-019 | Test the `semantic-audit.ts` rule `LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT`: a batch used in a preparation record should have a corresponding `ISSUE` transaction for the same date, UNLESS explicitly flagged as an out-of-band addition. | 1. Given a preparation record using batch `X` on date `D`, run the invariant query checking for an `ISSUE` row `(batch_id=X, service_date=D)`.<br>2. `POST /preparation-records` with a batch that has no corresponding issue slip. | 1. Step 1 → `rowsChecked > 0`; `violations` counted only over rows where `preparation_batches.extra = false`.<br>2. Step 2 → either HTTP 422 with `details` naming the batch, OR HTTP 201 with `preparation_batches.extra = true` — **two candidate oracles, not yet reconciled (see Note)**. | Preparation record with a batch lacking an issue slip. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | Rule `LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT` (§8); BR-076; §5.2 (`preparation_batches.extra` column, meant to hold exactly this case for audit reconciliation). ⚠️ Two oracles conflict and neither is authoritative yet — pending decision from Huy + Thảo on whether "extra" batches are allowed at all; do not assume either resolution when implementing |
| MF06-TC-INT-020 | Test the `semantic-audit.ts` rule `SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI`: `preparation_batches.batch_id` must reference an existing batch (FK), and the write path returns a proper not-found error. | 1. Direct `INSERT INTO preparation_batches` with a non-existent `batch_id`.<br>2. `POST /api/v1/preparation-records` with `batchesUsed: ["BATCH-KHONG-CO"]`. | 1. Step 1 → foreign-key violation.<br>2. Step 2 → HTTP 404, message = MSG-137 verbatim: "Resource not found." | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | Rule `SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI` (§8); BR-076, C6; MSG-137 (shared code across all modules → assert verbatim) |
| MF06-TC-INT-020b | Test the `semantic-audit.ts` rule `LO_KHONG_CO_PHIEU_NHAN`: every batch in inventory traces back to a receiving session (no orphan batches that would break the traceability chain at the warehouse boundary). | 1. Run the invariant query: batches with `receiving_session_id IS NULL`. | 1. `rowsChecked > 0`.<br>2. `violations` is empty. | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | Rule `LO_KHONG_CO_PHIEU_NHAN` (§10b); BR-076 ("traceability chain stops at the warehouse: no way to know who delivered it or on which slip") |
| MF06-TC-INT-021 | Test the `semantic-audit.ts` rule `CHE_BIEN_SAI_NGAY_THUC_DON`: a preparation record's date must equal its linked menu's `menu_date`. | 1. Given a preparation record with a `menu_id`, run the invariant query checking `prep.date <> menu.menu_date`. | 1. `rowsChecked > 0`.<br>2. `violations` is empty. | — | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | Rule `CHE_BIEN_SAI_NGAY_THUC_DON` (§8); BR-076 (`PreparationRecord` must anchor to the correct production run) |
