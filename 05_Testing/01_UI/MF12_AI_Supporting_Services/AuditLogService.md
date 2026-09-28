# Unit Test Case — AuditLogService / AuditLogPort (MF12 AI & Supporting Services)

> Group (B) — System Administration. Layer `com.sfms.mf12.audit.*`. `audit_logs` is physically owned by MF01 (`V1:278`) — MF12 only `ALTER ADD`s hash-chain columns (`M12-D01`); assertions use column `actor`, not `actor_label`. Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-012`, `MF12-TC-012b`, `MF12-TC-013`, `MF12-TC-014`, `MF12-TC-030`.

---

## Function: `AuditLogService.append()` — hash chain computation

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-012` | | Function Name | `AuditLogService.append` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Every audit row must carry `entry_hash(n) = SHA256(entry_hash(n-1) ‖ canonical(content))`, computed inside the same transaction as the INSERT, with a deterministic field order (BR-172, ADR-M12-02). (source: MF12-TC-012) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 0 | 1 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-001`, `FX-002`, `FX-003` written consecutively via `append()` | O | O | O |
| Row under check | Row 1 (first / genesis row) | O | | |
| | Row 2, hash re-computed independently in the test (not via production code) | | O | |
| | Row 3, plus DTO field-order reshuffled | | | O |
| **Confirm** | | | | |
| Return | `prev_hash` of row 1 = a fixed genesis value (e.g. 64 `'0'` chars), never `null` | O | | |
| | `entry_hash(2) == SHA256(entry_hash(1) ‖ canonical(row2))` | | O | |
| | `canonical(row)` output unchanged after reordering DTO fields — hash does not depend on map/JSON field order | | | O |
| | Hash computed in the SAME transaction as the INSERT (two concurrent inserts never share the same `prev_hash`) | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | B |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `AuditLogService.verifyChain()` — tamper detection

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-013` | | Function Name | `AuditLogService.verifyChain` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | Any single-row tamper (even in the middle of a 50-row chain, done directly by SQL) must be detected by `GET /audit-logs/integrity`, reporting the exact broken row (BR-172). (source: MF12-TC-012b) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 1 | 3 | 0 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | 50 rows written via `append()` | O | O | O | O |
| Tamper action | None — just verify | O | | | |
| | Row 25 (middle) content edited by raw SQL (owner role) | | O | | |
| | Row 25 (middle) DELETED by raw SQL | | | O | |
| | Last row (row 50) content edited by raw SQL | | | | O |
| **Confirm** | | | | | |
| Return | `{valid: true, checked: 50}` | O | | | |
| | `valid=false`, `brokenAt` = code of row 25 (not row 1, not the last row) | | O | | |
| | `valid=false`, `brokenAt` = row **26** (its `prev_hash` no longer matches) | | | O | |
| | `valid=false` (own-hash mismatch caught, not just `prev_hash`) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | A | A | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

---

## Function: `AuditLog` entity `@Immutable`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-014` | | Function Name | `AuditLog` (`@Immutable`) + repository |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 20 | | Lack of test cases | 0 |
| Test requirement | No one — including a business-layer bug that reloads-and-saves an entity — may generate an UPDATE/DELETE against `audit_logs` at the entity/Hibernate layer (BR-162; this is layer 1 of 3, alongside `AIS-FUNC-INT-*` DB and API layers). (source: MF12-TC-013) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-001` loaded, a field mutated, then `repository.save()` | O | | |
| Action | `repository.save()` after mutation, inspect generated SQL (`StatementInspector`) | O | | |
| | `repository.delete()` | | O | |
| | Inspect entity class annotations | | | O |
| **Confirm** | | | | |
| Return | NO `UPDATE audit_logs` statement is generated — Hibernate silently skips it because of `@Immutable` | O | | |
| Exception | `delete()` either throws or generates no `DELETE` | | O | |
| Return | Entity class carries `@Immutable`; no public setter exists for content fields | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note (UTCID01): `@Immutable` silently ignores UPDATEs rather than throwing — the test MUST assert on the generated SQL, not on "no exception thrown", otherwise it passes for the wrong reason.

---

## Function: `AuditLogPort` — audit write failure fails the business transaction

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-015` | | Function Name | `AuditLogPort` (in a business transaction) |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 25 | | Lack of test cases | 0 |
| Test requirement | If the audit INSERT fails, the whole business operation (e.g. `PUT /security-policies`) must roll back and return an error — an action without an audit trail is not considered complete (BR-170, `TDS §17.1 C2`). This must hold for every module that calls the shared `AuditLogPort`. (source: MF12-TC-014) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 0 | 2 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `AuditLogRepository` mocked to throw on INSERT | O | O |
| Business action under test | `PUT /security-policies` (module MF12) | O | |
| | An equivalent write operation in a different module that also uses `AuditLogPort` | | O |
| **Confirm** | | | |
| Return | Business change (`security_policies`) is rolled back — DB unchanged | O | O |
| | Client receives an error (never a silent success) | O | O |
| | No orphaned audit row exists | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | A |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

---

## Function: Audit retention policy (≥ 12 months)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-016` | | Function Name | `audit.retention-months` config + cleanup job |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 25 | | Lack of test cases | 0 |
| Test requirement | Retention default must be 12 months (BR-171, BR-177); rows older than the window may be archived but must NEVER be `DELETE`d from `audit_logs`, since BR-162 forbids deletion even of expired rows — a config below 12 months must fail fast at startup. (source: MF12-TC-030) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 4 | 2 | 1 | 1 | 4 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|---|
| **Condition** | | | | | |
| Precondition | Default configuration read | O | | | |
| | Audit row seeded at 11 months old | | O | | |
| | Audit row seeded at 13 months old | | | O | |
| | `audit.retention-months` overridden to `6` at startup | | | | O |
| **Confirm** | | | | | |
| Return | Default value is `12` (not 1, 3, or 24) | O | | | |
| | Row still present and unmodified after cleanup job runs | | O | | |
| | Row kept or archived — but NO `DELETE FROM audit_logs` executed against it (BR-162 wins over BR-171) | | | O | |
| Exception | App refuses to start with a retention value below the BR-177 minimum of 12 months (fail-fast) | | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 |
|---|---|---|---|---|
| Type (N/A/B) | N | N | B | A |
| Passed/Failed | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | |

Note: TestSpec §4.1 (`MF12-TC-030`) flags an unresolved conflict — BR-171 implies deletion after the retention window, while BR-162 forbids ANY deletion of audit rows. This matrix locks to BR-162 (no DELETE ever) per the TestSpec's explicit policy choice, pending Huy's sign-off on the archive mechanism.
