# Integration Test Case — End-to-End Workflow (MF05 Menu & Diet Management)

---

## Feature: Full Menu Lifecycle Trace

### 1. General Information

| Field | Value |
|---|---|
| Feature | Full Menu Lifecycle Trace |
| Test requirement | Verify the P1 backbone flow end-to-end — Caterer login → create menu for a grade → submit → School Admin login → approve → Parent login → sees the published menu with an allergy-conflict flag, with every hop joined strictly by ID (never by name) — and verify the FE/BE contract shape for the 4 MF05 resources (`menus`, `menus/{id}`, `dishes`, `menu-templates`) stays in sync with the FE mock, so swapping `baseURL` from mock to real backend requires no FE code change. |
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
| **Full Menu Lifecycle Trace** | | | | | | | | | | | | | | |
| MF05-TC-E2E-001 | Test the P1 backbone trace end-to-end, joined strictly by ID: Caterer creates a menu → submits → School Admin approves → Parent sees the published menu with an allergy-conflict flag. | 1. `POST /api/v1/auth/login` as Service Caterers → obtain JWT.<br>2. `POST /menus` for Grade 3, `menuDate = today + 5`, including a dish containing shrimp and its substitute.<br>3. `POST /menus/{id}/submit` → `PENDING_APPROVAL`.<br>4. Log in as School Admin → `GET /menus?status=PENDING_APPROVAL`, confirm the submitted menu appears.<br>5. `POST /menus/{id}/approve` with a signature.<br>6. Log in as the parent of `HS-3A-001` → `GET /parent/menus?studentId=HS-3A-001`. | 1. Step 4: the menu appears in the pending-approval list scoped to the correct school, with `allergyConflicts >= 1` visible to school leadership.<br>2. Step 6: the parent sees the exact same menu (compared by `id`, not by name), `status="PUBLISHED"`, `conflictsWithChild = true`, and the substitute dish is present.<br>3. Every join in the chain uses IDs: `menu.id` → `menu_dishes.dish_id` → `dishes.id`; `menu.grade` ↔ `classrooms.grade`; `parent.childIds` → `students.id`. No join uses a dish or class name.<br>4. The audit trail has the full 3 rows (create/submit/approve, see Menu_Lifecycle_Management.md MF05-TC-INT-010). | Clean DB plus seed data: MF01 (3 users), MF02 (`SCH-001`, classes 3A/3B), MF03 (`HS-3A-001` with VERIFIED shrimp allergy, parent linked), MF05 (`FX-008`, `FX-009`, `FX-011`, `FX-016`, `FX-017`). | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | P1 backbone (`00_Team_Work_Assignment_Plan.md` §3); all joins must use ID, never name |
| MF05-TC-E2E-002 | Test that the backend response shape for the 4 MF05 resources matches the FE mock server contract, so switching `baseURL` requires no FE changes. | 1. Call `GET /menus`, `GET /menus/{id}`, `GET /dishes`, `GET /menu-templates` (excluding `GET /special-meals`, which belongs to MF03).<br>2. Recursively extract the set of JSON keys from each response.<br>3. Compare against the key snapshot exported from `mock-server.ts` (`src/test/resources/contracts/mf05-mock-keys.json`). | 1. The backend's key set is a superset of the mock's key set — no missing keys.<br>2. All 12 field names required by C9 are present: `week`, `dishes`, `allergyConflicts`, `menuDateIso`, `createdBy`, `blocksData`, `servings`, `audience`, `dietGroupId`, `approvalSignature`, `rejectReason`, `reason`.<br>3. The 5 pagination keys are correct, with no `data`/`result` wrapper.<br>4. `submittedAt`/`approvedAt` match `^\d{2}/\d{2}/\d{4}`; `menuDateIso` matches `^\d{4}-\d{2}-\d{2}$`. | Snapshot file `src/test/resources/contracts/mf05-mock-keys.json` present and current. | Pending | 2026-08-05 | Thao | Pending | | | Pending | | | Contract/snapshot testing — keys only, not values; TDS §17.1 C7, C9, C11; must run in CI on every PR |
