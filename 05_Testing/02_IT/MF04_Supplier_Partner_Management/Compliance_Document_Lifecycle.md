# Integration Test Case — Compliance Document Lifecycle (MF04 Supplier & Partner Management)

---

## Feature: Document Update Versioning and BR-047 Expiry Scan

### 1. General Information

| Field | Value |
|---|---|
| Feature | Document Update Versioning and BR-047 Expiry Scan |
| Test requirement | Verify that updating a compliance document creates a new version and keeps the superseded one as evidence (`superseded_by_document_id`, never a physical delete — TDS §4.2, audit trail for food-safety inspections), and that the scheduled BR-047 scan sends `MSG-053` only for documents inside the 180/30-day windows, skips `NULL`-expiry and superseded documents, notifies both the owning supplier and the school it partners with, and does not send duplicate warnings within the same day. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 1 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Update Compliance Document — New Version, Old Version Kept as Evidence** | | | | | | | | | | | | | | |
| MF04-TC-INT-008 | Test that replacing a compliance document creates a new versioned row while the previous one remains queryable and is excluded from the BR-047 scan, and that replacing an expired certificate lifts the partner's blocked status. | 1. `PUT /api/v1/supplier-documents/DOC-GF-04` (VietGAP, `URGENT`) with a new file and `expiryDate=2029-07-01`.<br>2. Query both the old and new document rows.<br>3. Confirm `MF04-TC-INT-009`'s scan does not pick up the superseded row.<br>4. Separately: a partner with an `EXPIRED` document is updated with a valid replacement — re-check its blocked status. | 1. HTTP 200, MSG-029.<br>2. New row: `expiry_date=2029-07-01`, derived status `VALID`; old row still present, `superseded_by_document_id` points to the new row (row count increases by 1, old row is not overwritten).<br>3. Excluded, as expected.<br>4. `hasValidFoodSafetyCert() == true` for that partner; `ComplianceDocumentUpdated` event published. | Testcontainers PostgreSQL 16, Flyway migrated. | Passed | 2026-08-05 | Manh | Pending | | | Pending | | | UC-091 Postconditions/AF1, TDS §4.2 ("documents are never physically deleted — inspection evidence") |
| **BR-047 Expiry Scan — Correct Recipients, No Duplicates** | | | | | | | | | | | | | | |
| MF04-TC-INT-009 | Test that the scheduled expiry scan sends MSG-053 only for documents inside the 180/30-day BR-047 windows, skips documents with no expiry date and superseded documents, notifies both the supplier and its partnered school(s), and does not duplicate warnings if run twice in one day. | 1. Run the scheduler once against FX-005 + FX-014 (`TODAY=2026-07-28`), collect all notifications sent.<br>2. Run the scheduler a second time on the same day. | 1. `DOC-GF-04` (expires `2026-07-28` → `URGENT`) receives a warning with the correct `[Number]` of days; `DOC-GF-01` (expires `2027-03-15`, >180 days) receives none; `DOC-GF-02` (`expiry_date=NULL`) receives none; any superseded document receives none; recipients include both the owning supplier and the School Administration of its partnered school(s) (BR-046 — "may affect the partnership status").<br>2. No duplicate notifications for the same document on the same day. | FX-005, FX-014; one document already marked `superseded_by_document_id`. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | MSG-053, BR-047, L4 (NULL is never scanned); if the NULL-expiry document throws instead of being skipped, the whole job dies mid-run and later documents go unscanned |
