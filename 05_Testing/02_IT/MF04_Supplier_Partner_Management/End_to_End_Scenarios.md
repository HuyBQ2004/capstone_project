# Integration Test Case — Cross-Module Semantic Integrity Rules (MF04 Supplier & Partner Management)

---

## Feature: `partners` as the Single Source of Truth — 5 Rules Ported from `semantic-audit.ts`

### 1. General Information

| Field | Value |
|---|---|
| Feature | `partners` as the Single Source of Truth — 5 Rules Ported from `semantic-audit.ts` |
| Test requirement | Port the 5 `semantic-audit.ts` rules that touch MF04 into executable integration tests (`00_Team_Work_Assignment_Plan.md` §2a): purchasing/receiving from a supplier whose food-safety certificate has expired must be blocked (BR-046/BR-070, cross-module with MF06); no table outside `partners` may store a copy of the partner's status (`ADR-M04-7`, constraint C6 of `data_model.puml`); a `CorrectiveAction` cannot reference a nonexistent supplier; and no MF04 table may join to a partner by its name string instead of its key. Scenarios 3 and 5 are self-contained within MF04's own schema and do not require MF06/MF07 to be present. |
| Number of TCs | 1 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 1 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 1 | 0 |
| Round 3 | 0 | 0 | 1 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`partners` Single Source of Truth — 5 Semantic Rules** | | | | | | | | | | | | | | |
| MF04-TC-INT-015 | Test the 5 `semantic-audit.ts` scenarios that constrain MF04's data model: expired-certificate purchasing/receiving blocks, no duplicated partner-status columns anywhere else in the schema, rejection of a `CorrectiveAction` referencing a nonexistent supplier, and no join-by-name anywhere in MF04. | 1. **MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN**: `SUP-004`'s food-safety certificate expired `2026-06-22`; MF06 creates a `PurchaseRequest(supplierId=SUP-004, deliveryDate=2026-06-30)`.<br>2. **NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN**: MF06 attempts to mark a receiving session `RECEIVED` when its `deliveryDate` is after the certificate's expiry.<br>3. **PHIEU_GHI_SAI_TRANG_THAI_NCC**: query `information_schema.columns` for `column_name IN ('supplier_status','partner_status') AND table_name <> 'partners'`.<br>4. **CAPA_GIAO_NCC_KHONG_TON_TAI**: a `CorrectiveAction` is created with `assigneeSupplierId="SUP-999"` (does not exist).<br>5. **NCC_LO_KHAC_NCC_PHIEU_NHAN**: scan every MF04 table's foreign-key definitions for a text-based join to a partner. | 1. `PartnerLookupPort.hasValidFoodSafetyCert("SUP-004")` returns `false`; the purchase request is blocked (BR-046/BR-070).<br>2. Blocked by the same port method.<br>3. **0 rows** — `partners` is the only table holding partner status (ADR-M04-7).<br>4. Rejected — by a foreign-key constraint or `PartnerLookupPort.findById()` returning empty.<br>5. **No** MF04 table stores the partner's name as a join key — every reference goes through `partner_id` (UUID FK in the DB) or `code` (in JSON). | Testcontainers PostgreSQL 16, Flyway migrated. Scenarios 1/2/4 depend on MF06/MF07 being present; if either module is not yet implemented, run scenarios 3 and 5 as pure schema/port-contract checks and defer the rest to the cross-module test phase. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` lines 254-258/310-328/341-358, ADR-M04-7, constraint C6; scenario 3 reproduces the exact defect class the rule was written for — the right key pointing at data copied from a stale source |
