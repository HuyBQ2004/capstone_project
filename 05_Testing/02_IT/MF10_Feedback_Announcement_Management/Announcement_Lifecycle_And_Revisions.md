# Integration Test Case — Announcement Revision History Immutability (MF10 Feedback & Announcement Management)

---

## Feature: Announcement Revision History — Append-Only at the Database Layer (BR-144)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Announcement Revision History — Append-Only at the Database Layer (BR-144) |
| Test requirement | Verify that `announcement_revisions` cannot be modified or deleted even by a direct SQL statement (RULE `r_annrev_no_update`/`r_annrev_no_delete`, ADR-M10-3, same immutability principle as the audit log BR-162), that every create/update appends a row, and that a rejected `PUBLISHED→DRAFT` transition does not leave a false trail. |
| Number of TCs | 1 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 1 | 0 |
| Round 2 | 0 | 0 | 1 | 0 |
| Round 3 | 0 | 0 | 1 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **announcement_revisions — Immutable History** | | | | | | | | | | | | | | |
| MF10-TC-INT-012 | Test that the revision history table is append-only end-to-end: creates/updates append rows, direct UPDATE/DELETE statements are silently rejected by DB rules, and a rejected status transition generates no revision. | 1. `POST /announcements {status:"PUBLISHED"}`.<br>2. Read `announcement_revisions`.<br>3. `PATCH` twice more.<br>4. Read `announcement_revisions` again.<br>5. `UPDATE announcement_revisions SET note='đã sửa'` directly via JDBC.<br>6. Re-read the row.<br>7. `DELETE FROM announcement_revisions` directly via JDBC.<br>8. Re-count rows.<br>9. `GET /announcements/{code}` as `school-admin`.<br>10. `PATCH {status:"DRAFT"}` on a `PUBLISHED` record. | 1. HTTP 201.<br>2. 1 row, `note='Công bố lần đầu'`.<br>3. Two `PATCH` calls succeed.<br>4. 3 rows total, `revised_at` strictly increasing.<br>5. 0 rows changed (RULE `DO INSTEAD NOTHING`), original `note` unchanged on re-read.<br>6. Confirmed unchanged.<br>7. 0 rows deleted.<br>8. Row count unchanged from step 4.<br>9. `revisions[]` has exactly 3 entries, ordered by `revised_at`.<br>10. HTTP 409 `FBA-STATE`, and no new revision row is created for the rejected attempt. | Testcontainers PostgreSQL 16, Flyway `V18`/`V19` applied. | Pending | 2026-08-05 | Manh | Pending | | | Pending | | | BR-144, ADR-M10-3, BR-162; RULE fails silently — assert by re-reading data, not by expecting an exception |
