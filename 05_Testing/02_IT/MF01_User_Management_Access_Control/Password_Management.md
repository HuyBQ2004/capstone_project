# Integration Test Case — Password Management (MF01 User Management & Access Control)

---

## Feature: `Password Management & Recovery`

### 1. General Information

| Field | Value |
|---|---|
| Feature | Password Management & Recovery |
| Test requirement | Verify the forgot-password and reset-password endpoints end to end: the reset token is persisted hashed with a one-hour lifetime, a successful reset updates the password hash and revokes every active session, the token is single use, the reset path still enforces the last-3 reuse rule, and an unknown email produces an identical response so account existence is never revealed. |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 5 | 0 | 0 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **POST /api/v1/auth/forgot-password** | | | | | | | | | | | | | | |
| MF01-TC-INT-008a | Test that a reset request sends the email and persists a hashed token valid for one hour. | 1. Send POST to `/api/v1/auth/forgot-password` with `{"email":"toanpq@dtd.edu.vn"}`.<br>2. Read the message from the GreenMail SMTP stub.<br>3. Query `password_reset_tokens` for the user.<br>4. Check `expires_at - created_at`. | 1. HTTP 200 OK.<br>2. Exactly 1 reset email is delivered.<br>3. Exactly 1 token row exists and it stores the hash, not the raw token.<br>4. The token expires 1 hour after creation. | Seed user `U-0001` is ACTIVE; the GreenMail SMTP stub is running. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `PasswordResetIT.forgotPasswordSendsTokenAndPersists` |
| MF01-TC-INT-008b | Test that an unknown email produces exactly the same response as a registered one. | 1. Send a forgot-password request for a registered email.<br>2. Send a forgot-password request for an email that does not exist.<br>3. Compare status codes and bodies. | 1. Both return HTTP 200 with an identical body.<br>2. No token row and no email are produced for the unknown address.<br>3. Nothing reveals whether the account exists. | Seed data loaded. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `PasswordResetIT.unknownEmailSameResponse` |
| **POST /api/v1/auth/reset-password** | | | | | | | | | | | | | | |
| MF01-TC-INT-008c | Test the end-to-end reset happy path and session invalidation. | 1. Log in as `U-0001` to establish session R1.<br>2. Request a reset and extract the raw token from the stub mailbox.<br>3. Send POST to `/api/v1/auth/reset-password` with the token and `{"newPassword":"N3w@Pass456"}`.<br>4. Call `/refresh` with R1.<br>5. Log in with the new password. | 1. Step 3 returns HTTP 200 with MSG-030.<br>2. `password_hash` in the database is replaced with a new BCrypt hash.<br>3. Step 4 returns HTTP 401 because the reset revoked every active session.<br>4. Step 5 succeeds. | An active session for `U-0001` and a valid reset token. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `PasswordResetIT.resetPasswordHappyPath` |
| MF01-TC-INT-008d | Test that a reset token can only be used once. | 1. Complete one successful reset with the token.<br>2. Send the same token to `/reset-password` a second time. | 1. The second call returns HTTP 401 with `error.code = IAM-008`.<br>2. The password is not changed again. | A reset token that has already been consumed. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `PasswordResetIT.tokenOneTimeUse` |
| MF01-TC-INT-008e | Test that the reset path still enforces the last-3 password reuse rule. | 1. Request a reset token for `U-0001`.<br>2. Send POST to `/reset-password` with a new password equal to one of the last 3 passwords in `password_history`. | 1. HTTP 422 Unprocessable Content with `error.code = IAM-006reuse`.<br>2. `error.message` = MSG-105.<br>3. `password_hash` is unchanged. | `password_history` seeded with the 3 most recent hashes for `U-0001`. | Passed | 2026-08-14 | Nam | Pending | | | Pending | | | `PasswordResetIT.reusePasswordRejectedOnReset` |
