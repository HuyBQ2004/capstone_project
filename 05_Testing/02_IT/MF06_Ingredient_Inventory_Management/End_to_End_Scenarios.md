# Integration Test Case — End-to-End Scenarios (MF06 Ingredient & Inventory Management)

---

## Feature: End-to-End Traceability & Incident Investigation

### 1. General Information

| Field | Value |
|---|---|
| Feature | End-to-End Traceability & Incident Investigation |
| Test requirement | Verify the full procurement-to-plate backbone scenario (order → quote → accept price → schedule → deliver → receive → store → issue → prepare → trace-back) with every link joined by KEY (never by label, per constraint C6), and the cross-module MF11↔MF06 incident/quarantine investigation scenario in which Medical Staff quarantines a batch implicated in a food-safety incident, confirms it is excluded from the FEFO candidate pool and from further adjustment, then safely releases it once the incident is closed. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 2 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Procurement-to-Plate Backbone Scenario** | | | | | | | | | | | | | | |
| MF06-TC-E2E-001 | Test the full backbone scenario: purchase order → supplier quote → price acceptance → schedule confirmation → delivery → receiving → storage → FEFO issuance → preparation → trace-back — with every link verified by foreign key, never by display label. | 1. `POST /purchase-requests` (UC-021).<br>2. `POST /purchase-requests/{code}/quote` (supplier quotes).<br>3. `POST .../accept-price`.<br>4. `POST .../confirm-schedule` (UC-015).<br>5. `POST /deliveries` (UC-083).<br>6. `PATCH /deliveries/{code}/status` (UC-092).<br>7. `POST /receiving-sessions` (UC-060).<br>8. `PATCH /inventory/{code}/storage-zone` (UC-061).<br>9. `GET /inventory/issue-plan`.<br>10. `POST /inventory/issue-slips` (UC-045).<br>11. `POST /preparation-records` (UC-066).<br>12. `GET /traceability/dish-batches?dishId=…&serviceDate=…&mealType=LUNCH` (trace-back). | 1. Every step in the chain succeeds with the expected status transition per TDS §6.4/§6.5.<br>2. The final trace-back query returns the correct batch(es) and the correct supplier.<br>3. Every link in the chain is verified by KEY, not label: `ingredient_id`, `dish_id`, `menu_id`, `issue_slip_id`, `receiving_session_id`, `supplier_id` all match end-to-end.<br>4. If any link in the chain is broken, `GET /traceability/dish-batches` returns empty or an incomplete result — this is the exact failure this test exists to catch. | Full happy-path fixture set: `FX-SUP-APPROVED`, `FX-SCHOOL-GEO`, published menu, sufficient stock. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | BR-076 verbatim ("full traceability from source"); constraint C6 (joins by ID, never by name); Team Plan §3; TDS §6.4/§6.5 lifecycle; CF-M06-5 (single-round price approval, no $5,000 dual-authorization gate) |
| **Cross-Module Incident Investigation (MF11 ↔ MF06)** | | | | | | | | | | | | | | |
| MF06-TC-E2E-002 | Test the incident-investigation scenario spanning MF11 (incident management) and MF06 (quarantine): a suspect batch is identified via trace-back, quarantined, excluded from FEFO and adjustment while under quarantine, then correctly released once the incident is closed — without ever letting an expired batch slip back into the candidate pool. | 1. MF11 opens an incident for the suspect dish (`"Gà rang gừng"`, meal date `2026-06-30`).<br>2. Medical Staff `GET /traceability/dish-batches` → identifies `BATCH-MP-8820`.<br>3. `PATCH /inventory/BATCH-MP-8820/quarantine` with `incidentId`.<br>4. `GET /inventory/issue-plan` → confirm the batch is no longer selectable.<br>5. `POST /inventory-transactions/adjustments` on the quarantined batch.<br>6. Close the incident.<br>7. `DELETE /inventory/{code}/quarantine`.<br>8. Run the `DONG_SU_CO_MA_CON_LO_KHOA` invariant check. | 1. Step 2 → `BATCH-MP-8820` is correctly surfaced by the trace-back query.<br>2. Step 3 → quarantine set successfully with the incident reference.<br>3. Step 4 → the batch is excluded from the FEFO candidate pool (two independent lock axes: expiry and quarantine).<br>4. Step 5 → HTTP 409, `error.code="ING-149"`.<br>5. Steps 6–7 → after the incident is closed and quarantine is explicitly lifted, the batch returns to the candidate pool IF it is still within its shelf life.<br>6. Step 8 → `violations` is empty — no batch remains locked after its incident is closed. | `FX-PREP-FAILSTEP` (`PREP-20260630-01`, the real historical link for `INC-2026-014`); `FX-BATCH-QUAR-INC`. | Pending | 2026-08-05 | Huy | Pending | | | Pending | | | BR-149 verbatim; rules `DONG_SU_CO_MA_CON_LO_KHOA` + `CACH_LY_KHONG_RO_SU_CO` + `CACH_LY_LO_KHONG_LIEN_QUAN_BUA`; ADR-M06-2 (two independent lock axes — expiry × quarantine); expected failure mode: closing the incident while the batch stays locked forever with no screen to surface it (verbatim script §9b note), or releasing quarantine incorrectly re-admits an already-expired batch (would mean quarantine had overwritten `shelfStatus`, the exact bug ADR-M06-2 was designed to eliminate) |
