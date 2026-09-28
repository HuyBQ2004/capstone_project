# Integration Test Case — Health History Append-Only Integrity (MF03 Student & Parent Management)

---

## Feature: Health History Append-Only Log (BR-172)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Health History Append-Only Log |
| Test requirement | Verify `health_history_events` is genuinely append-only, enforced at three independent layers — API (`PUT`/`PATCH`/`DELETE` all rejected), ORM (`@Immutable` — no `UPDATE` statement generated even via reflection), and raw SQL under the least-privileged `sfms_app` role (`RULE hh_no_update`/`hh_no_delete` `DO INSTEAD NOTHING` + no `UPDATE`/`DELETE` grants) — and that corrections are made by appending a new row, never by editing history. |
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
| **PUT/PATCH/DELETE /api/v1/health-history/{id} and direct SQL (HealthHistoryAppendOnlyIT)** | | | | | | | | | | | | | | |
| MF03-TC-INT-005-01 | API layer rejects all 3 mutating verbs. | `PUT /health-history/{id}`; `PATCH /health-history/{id}`; `DELETE /health-history/{id}` (Medical Staff JWT). | All 3 return HTTP 405 `STU-172` + `MSG-116` verbatim ("Health history is append-only and cannot be modified or deleted."). | Seeded history event. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `HealthHistoryAppendOnlyIT.apiLayerRejectsMutation` |
| MF03-TC-INT-005-02 | ORM layer cannot mutate history even via reflection + `save()`. | Load managed entity in a transaction, overwrite the `event` field via reflection (no setter exists), `em.flush()`. | DB row content unchanged after commit, regardless of whether `flush()` throws (`@Immutable` disables Hibernate dirty-checking for this entity — observed behavior recorded in test Javadoc, not pre-assumed). | Seeded history event. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `HealthHistoryAppendOnlyIT.ormLayerCannotMutateHistory` |
| MF03-TC-INT-005-03 | SQL layer (role `sfms_app`) cannot mutate or delete. | Direct JDBC connection as `sfms_app`: `UPDATE health_history_events SET event=... WHERE id=?`; `DELETE FROM health_history_events WHERE id=?`. | `UPDATE` affects 0 rows (`RULE hh_no_update DO INSTEAD NOTHING`); `DELETE` either affects 0 rows or throws `SQLException`; row content/count unchanged; 0 `UPDATE`/`DELETE` grants for `sfms_app` in `information_schema.role_table_grants`. | Seeded history event; connection uses `sfms_app`, not superuser. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `HealthHistoryAppendOnlyIT.sqlLayerRejectsMutationForSfmsAppRole` |
| MF03-TC-INT-005-04 | Correction pattern appends a new row instead of mutating. | `repository.save(new HealthHistoryEvent(..., GHI_CHU_LAM_SANG, "Đính chính...", ...))`. | Row count increases by exactly 1 (correction is always an append, per ADR-M03-4). | Seeded history event. | Passed | 2026-08-14 | Huy | Pending | | | Pending | | | `HealthHistoryAppendOnlyIT.correctionAppendsNewRowInsteadOfMutating` |

Note: this is one of two `mandatory per TDS §17.2 TASK BLOCK` tests for MF03 (the other is `AllergyVerifyAlertIT`, in `Health_Allergy_Declaration_And_Review.md`). Test datasource must connect as `sfms_app`, never the Testcontainers superuser, or the `REVOKE` assertions in case 03 would pass vacuously (Green-from-Birth risk).
