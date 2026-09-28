# Integration Test Case — Security Policy Configuration (MF12 AI & Supporting Services)

---

## Feature: Security Policy Configuration (UC-184)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Security Policy Configuration |
| Test requirement | Verify that MF12's migration (`V22`/`V23`) does not recreate the `security_policies` table owned by MF01 (TDS §17.1 C8), and that updates made through `PUT /api/v1/security-policies/{code}` take effect immediately in MF01's login lockout logic via the `SecurityPolicyUpdated` event, with no restart required (BR-002). |
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
| **Migration Ownership — security_policies belongs to MF01** | | | | | | | | | | | | | | |
| MF12-TC-INT-013 | Test that MF12's Flyway migration does not re-declare the `security_policies` table, which is owned (created + seeded) by MF01. | 1. Grep `V22`/`V23` content for `CREATE TABLE`/`DROP TABLE` on `security_policies`.<br>2. Verify the table exists already after `V1` (before `V22`).<br>3. Verify seed `SEC-DEFAULT` exists after `V1`.<br>4. Confirm a partial-merge PUT (cross-ref `MF12-TC-016`) works on the MF01-created table. | 1. Step 1 → `grep -niE "CREATE TABLE .*security_policies|DROP TABLE .*security_policies" src/main/resources/db/migration/V22*.sql src/main/resources/db/migration/V23*.sql` returns no output (C8).<br>2. Step 2 → table exists after `V1`.<br>3. Step 3 → `SEC-DEFAULT` exists with the exact values of `FX-011`.<br>4. Step 4 → MF12's `PUT` succeeds against the MF01-owned table.<br>5. Positive control: `V22` DOES create MF12's own 4 tables (`ai_conversations`, `ai_requests`, `error_logs`, `external_integrations`, `sent_reports`) — an empty grep because `V22` is empty must not pass falsely. `audit_logs` and `checklist_groups` are explicitly NOT in this list (`M12-D01`/`M12-D02`) — `V22` only `ALTER`s `audit_logs`. | Full Flyway migration history (`V1`-`V23`) applied. | Pending | | | Pending | | | Pending | | | HIGH; TDS §17.1 C8; TDS-MF01_Backend.md §11.1; L10/CF-6; `00_` §4b |
| **Cross-Module Effect — PUT /api/v1/security-policies/{code} → MF01 Login Lockout** | | | | | | | | | | | | | | |
| MF12-TC-INT-014 | Test that updating `maxLoginAttempts` via MF12 takes effect on MF01's login lockout logic at the very next check, without a restart, via the `SecurityPolicyUpdated` event. | 1. `PUT /security-policies` with `maxLoginAttempts = 3` → assert 200 + event.<br>2. `POST /auth/login` with a wrong password 3 times.<br>3. Assert the 3rd attempt.<br>4. Change `maxLoginAttempts` back to 5, unlock, retry: 3rd attempt should NOT lock, 5th attempt should.<br>5. Inspect audit rows for both operations. | 1. Step 1 → event `SecurityPolicyUpdated` published exactly once.<br>2. Step 3 → account IS locked at the 3rd attempt (not the 5th) — the new policy takes effect without a restart.<br>3. Step 4 → after reverting to 5, lockout occurs at the 5th attempt.<br>4. Step 5 → every policy change has an audit row (BR-170); the lockout event also has an audit row (BR-002 — "record audit"), matching `FX-004` (`AUD-99087` with `result=BLOCKED`).<br>5. MF01 reads live from the table (or caches with event-based invalidation), never a permanently stale cache. | MF01 and MF12 running together; `SecurityPolicyUpdated` event bus wired. | Passed | 2026-08-12 | Thao | Pending | | | Pending | | | CRITICAL; BR-002; TDS §17.1 C8; cross-module MF12↔MF01 |
