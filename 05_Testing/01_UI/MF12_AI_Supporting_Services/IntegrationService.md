# Unit Test Case — IntegrationService / CredentialCipher (MF12 AI & Supporting Services)

> Group (B) — System Administration. Layer `com.sfms.mf12.integration.*`. Source: `TestSpec-MF12_AI_Supporting_Services.md` §4.1 `MF12-TC-006`…`MF12-TC-011`.

---

## Function: `IntegrationService.save()` — reject save before a passing test-connection

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-006` | | Function Name | `IntegrationService.save` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 40 | | Lack of test cases | 0 |
| Test requirement | Saving external-integration config (GPS/Gmail/Firebase/Gemini) must be blocked unless a connection test has passed first (BR-163); the server must ignore any `connected` flag the client sends and derive it only from `last_test_passed` (C11). (source: MF12-TC-006) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 0 | 2 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `FX-007` `code="gemini"`, `last_test_passed=false`, `last_tested_at=null` | O | O |
| Request | `save(code, {endpoint, apiKey})`, no prior test | O | |
| | Same, but client also sends `connected: true` in the payload | | O |
| **Confirm** | | | |
| Exception | `IntegrationNotTestedException`, `$.error.code == "AIS-163"`, HTTP 409 | O | O |
| Return | DB unchanged: `endpoint` stays old value, no credential saved | O | |
| | Still 409 — server ignores client-sent `connected` and derives it only from `last_test_passed` | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | A | A |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

---

## Function: `IntegrationService.save()` — 10-minute test-connection validity window (boundary)

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-007` | | Function Name | `IntegrationService.save` — `last_tested_at` age check |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | `save()` must accept a passing test result only while it is "not yet older than 10 minutes" (BR-163, `TDS §17.1 C4`) — exactly 10 minutes is still valid, 10m01s is not. (source: MF12-TC-007) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 5 | 0 | 1 | 4 | 5 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|---|
| **Condition** | | | | | | |
| `last_test_passed` | `true` | O | O | O | O | |
| | `false` | | | | | O |
| `last_tested_at` age | `now − 9m59s` | O | | | | |
| | `now − 10m` (on-point) | | O | | | |
| | `now − 10m01s` | | | O | | |
| | `now − 11m` | | | | O | O (`now − 1m`) |
| **Confirm** | | | | | | |
| Return | Save succeeds; `connected` set `true` (derived from `last_test_passed`, not client input) | O | O | | | |
| Exception | `$.error.code == "AIS-163"`, HTTP 409 | | | O | O | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 | UTCID04 | UTCID05 |
|---|---|---|---|---|---|
| Type (N/A/B) | B | B | B | B | A |
| Passed/Failed | Untested | Untested | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | | | |

Note (UTCID05): `last_test_passed=false` is rejected independently of age — even a 1-minute-old FAIL result must not allow a save.

---

## Function: `IntegrationService.testConnection()`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-008` | | Function Name | `IntegrationService.testConnection` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 35 | | Lack of test cases | 0 |
| Test requirement | `testConnection()` must perform a real handshake with a 5s timeout; on failure, `last_test_passed` is recorded as `false` and NO credential is persisted at all — persistence only ever happens at `save()` (BR-163, UC-040 AF1). (source: MF12-TC-008) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 1 | 1 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Fake result | `FX-023` fake 401 / connection error | O | | |
| | Fake delay > 5s (handshake timeout) | | O | |
| | Fake success | | | O |
| Payload | `FX-027` test `apiKey` | O | O | O |
| **Confirm** | | | | |
| Exception | `$.error.code == "AIS-163"`, HTTP 422 | O | O | |
| Return | `last_test_passed=false`, `last_tested_at` updated (a failed attempt is still recorded), no credential row written | O | O | |
| | `last_test_passed=true`, `last_tested_at=now`; credential is NOT yet persisted here (only at `save()`) | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | B | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

---

## Function: `IntegrationMapper` — credential exposure guard

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-009` | | Function Name | `IntegrationMapper` / `GET /integrations` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 25 | | Lack of test cases | 0 |
| Test requirement | External-service credentials must never leave the backend in plaintext — API responses expose only `credentialMasked`, the DB column stores ciphertext, and logs never contain the raw key (BR-164, CWE-522). (source: MF12-TC-009) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 2 | 2 | 0 | 0 | 2 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 |
|---|---|---|---|
| **Condition** | | | |
| Precondition | `save(code, {apiKey: FX-027})` completed | O | O |
| Check target | `GET /integrations` and `GET /integrations/{code}` JSON body (recursive key scan) | O | |
| | DB column value + application log output for the whole flow | | O |
| **Confirm** | | | |
| Return | JSON has `credentialMasked` (e.g. `"AIza••••0000"`); NO key named `apiKey`/`credential`/`secret`/`token` at any depth | O | |
| | DB column is ciphertext (≠ plaintext), stores `key_version` + a per-record IV/nonce | | O |
| | Logs contain neither `"AIzaTEST-not-a-real-key-000000000000000"` nor the `AIza` prefix (`grep -rniE "AIza|sk-[A-Za-z0-9]{20,}|password=" target/*.log` → no output) | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 |
|---|---|---|
| Type (N/A/B) | N | N |
| Passed/Failed | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 |
| Defect ID | |

---

## Function: `CredentialCipher.encrypt()/decrypt()` — AES-256-GCM round-trip

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-010` | | Function Name | `CredentialCipher` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 45 | | Lack of test cases | 0 |
| Test requirement | Credential encryption must round-trip correctly, use a fresh random IV per call (GCM safety), and reject tampered ciphertext instead of silently returning wrong plaintext (BR-164, CWE-327). (source: MF12-TC-010) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 2 | 1 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | `FX-026` `SFMS_CRED_MASTER_KEY` (32-byte base64 test key), `key_version=1` | O | O | O |
| Action | `decrypt(encrypt(x))` for ASCII, accented Unicode, and a 4KB string | O | | |
| | `encrypt(x)` called twice with the SAME plaintext | | O | |
| | 1 byte of a valid ciphertext flipped, then `decrypt()` | | | O |
| **Confirm** | | | | |
| Return | `decrypt(encrypt(x)) == x` for all 3 input shapes | O | | |
| | The two ciphertexts from step 2 are DIFFERENT (random IV/nonce per call) | | O | |
| Exception | `decrypt()` throws (GCM auth tag catches the tamper) — never returns a silently-wrong plaintext | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | N | N | A |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |

Note: this function also carries a static/source-scan check ("no `SFMS_CRED_MASTER_KEY = "..."` literal in `src/main/java/`") — not expressible as a runtime UTCID, run as part of the same test class.

---

## Function: `CredentialCipher.decrypt()` — wrong `key_version`

### 1. General Information

| Field | Value | | Field | Value |
|---|---|---|---|---|
| Function Code | `AIS-FUNC-011` | | Function Name | `CredentialCipher.decrypt` |
| Created By | Nguyen Phuong Thao | | Executed By | Thao |
| Lines of code | 30 | | Lack of test cases | 0 |
| Test requirement | Decryption with a missing/mismatched key version must fail loudly (`AIS-164`) rather than return garbage, the app must fail-fast at startup if the master key env var is absent, and key rotation must keep old versions readable (BR-164, §7 R4). (source: MF12-TC-011) | | | |

### 2. Result Summary

| Passed | Failed | Untested | N | A | B | Total Test Cases |
|---|---|---|---|---|---|---|
| 0 | 0 | 3 | 1 | 2 | 0 | 3 |

### 3. Condition / Confirmation Matrix

| | | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|---|
| **Condition** | | | | |
| Precondition | Ciphertext written with `key_version=1`, environment only has `version=2` key | O | | |
| | `SFMS_CRED_MASTER_KEY` env var entirely absent at service startup | | O | |
| | After rotation: v2 key added, v1 key KEPT | | | O |
| Action | `decrypt()` the v1 record | O | | O |
| **Confirm** | | | | |
| Exception | `$.error.code == "AIS-164"`, HTTP 500; no plaintext, no garbage string returned (⚠️ D7/L4: assert code only) | O | | |
| | App fails to start (fail-fast) — never runs silently with a default key | | O | |
| Return | v1 record decrypts successfully after rotation (old key retained) | | | O |

### 4. Execution Result

| | UTCID01 | UTCID02 | UTCID03 |
|---|---|---|---|
| Type (N/A/B) | A | A | N |
| Passed/Failed | Untested | Untested | Untested |
| Executed Date | 2026-08-05 | 2026-08-05 | 2026-08-05 |
| Defect ID | | | |
