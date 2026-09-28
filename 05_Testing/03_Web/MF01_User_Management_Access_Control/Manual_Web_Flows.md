# Manual Web E2E Test — MF01 User Management & Access Control

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Login & Session (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Login & Session (Manual Web) |
| Test requirement | Login with email/password, Google, account lockout, block by linked school status, single-session, logout — all performed by hand on `/login`. |
| Number of TCs | 7 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 7 | 0 |
| Round 2 | 0 | 0 | 7 | 0 |
| Round 3 | 0 | 0 | 7 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Login (email/password)** | | | | | | | | | | | | | | |
| MF01-WEB-001 | Successful login with a correct email/password. | 1. Open `/login`.<br>2. Enter a valid email + password for an ACTIVE School Admin account.<br>3. Click "Login". | Redirected to the role-based dashboard (School Admin). No longer on `/login`. Avatar/name shown correctly in the topbar. | Account exists, status ACTIVE, email verified, linked school (if any) is ACTIVE. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-002 | 5 consecutive wrong passwords ⇒ account locked. | 1. Open `/login`.<br>2. Enter the correct email with a wrong password, click "Login" — repeat 5 times.<br>3. On the 6th attempt, enter the CORRECT password and click "Login". | After each failed attempt (1–4): generic error "incorrect email or password" (does not say which one is wrong). On the 5th: message that the account is temporarily locked (MSG-076), mentioning the wait time. On the 6th, still blocked even with the correct password (lockout wins over a correct password). | Account is ACTIVE, not previously locked (failedLoginCount = 0). | Pending | | | Pending | | | Pending | | | Must not leak whether the email exists |
| MF01-WEB-003 | Login with an account not yet activated (PENDING_VERIFICATION). | 1. Open `/login`.<br>2. Enter the email/password of an account just created, not yet activated. | Blocked with a "account disabled/not verified" message (MSG-003) — cannot reach the dashboard. | Account was just created by a School/System Admin, activation link in the email not clicked yet. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-004 | Login for an account whose linked school is not ACTIVE. *(new flow, 2026-08-11)* | 1. Prepare a school in a non-ACTIVE state (e.g. PENDING approval, or disabled).<br>2. Log in with a School Admin/Teacher/... account belonging to that school, correct password. | Blocked despite the correct password — message similar to "account disabled" (MSG-003). Cannot reach the dashboard. | Need Nam/Huy to confirm how to move a school to a non-ACTIVE state through the UI (MF02) to reproduce this precondition — unclear which screen allows this yet, needs confirmation. | Pending | | | Pending | | | Pending | | | Precondition needs confirmation from Nam/Huy — no known MF02 UI screen yet to set a school non-ACTIVE |
| **Login with Google** | | | | | | | | | | | | | | |
| MF01-WEB-005 | Google login for an existing account, matching email. | 1. Open `/login`.<br>2. Click "Login with Google".<br>3. Pick a Google account whose email matches an email already registered in the system. | Login succeeds, goes straight to the dashboard. On first use, the account is silently linked to Google (no extra prompt). | A test Google account exists whose email matches an ACTIVE SFMS account. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-006 | Google login with an email that matches no account in the system. | 1. Open `/login`.<br>2. Click "Login with Google".<br>3. Pick a Google account whose email has never been registered in the system. | Rejected, no account auto-created — a clear error message is shown, returns to `/login`. | A test Google account exists whose email is certainly not in the system. | Pending | | | Pending | | | Pending | | | Google login only links, never auto-creates an account (BR-218) |
| **Logout** | | | | | | | | | | | | | | |
| MF01-WEB-007 | Logout and confirm the session is fully revoked. | 1. Log in successfully.<br>2. Click the avatar in the topbar → "Logout".<br>3. Use the browser's Back button to return to the previous page (dashboard). | Returns to `/login`. Clicking Back does not re-enter the dashboard — bounced back to `/login` (session token already revoked server-side). | Successfully logged in at step 1. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Password Recovery & Account Activation (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Password Recovery & Account Activation (Manual Web) |
| Test requirement | Forgot password via `/forgot-password` → `/verify-otp` → `/reset-password`; activate a new account via `/activate-account`. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Forgot / Reset Password** | | | | | | | | | | | | | | |
| MF01-WEB-008 | Forgot password with a valid email, successful reset. | 1. On `/login`, click "Forgot password?".<br>2. Enter the registered email, click "Send OTP".<br>3. Open the email, get the token, paste it into `/reset-password?token=...` (or follow the link in the email).<br>4. Enter a valid new password + confirmation, click "Reset password". | After step 2: shows the `/verify-otp` screen (does not leak whether the email exists — same message for existing/non-existing email). After step 4: "Password changed successfully!" + a "Login" button. Logging in again with the new password succeeds; the old password no longer works. | An active account exists whose email inbox is reachable for testing. | Pending | | | Pending | | | Pending | | | Must not show different messages for existing vs. non-existing email |
| MF01-WEB-009 | Reuse an already-used reset token (or a token expired >1h). | 1. Complete one successful password reset (using token T1).<br>2. Go back to `/reset-password?token=T1`, enter a different new password, click "Reset password". | Rejected — reports the token is invalid/expired, password is not changed. | Already have one used reset token (from MF01-WEB-008). | Pending | | | Pending | | | Pending | | | One-time-use token |
| MF01-WEB-010 | Set a new password matching one of the last 3 passwords. | 1. Go to `/forgot-password`, request a new reset token.<br>2. On `/reset-password`, re-enter the exact current password (or one of the previous 2) as the new password. | Rejected with a password-policy violation message (must not match the last 3 passwords). | The account has changed its password at least once before (has password history). | Pending | | | Pending | | | Pending | | | BR-006 |
| **Account Activation** | | | | | | | | | | | | | | |
| MF01-WEB-011 | Activate a newly-created account from the invitation email (School Admin/System Admin just created). | 1. Go to `/activate-account?token=...` using the token from the activation email.<br>2. Enter a new password + confirmation, click "Activate Account & Set Password". | Shows "Account Activated Successfully!" with a "Login Now" button. Logging in with the email + newly-set password succeeds on the first try. | Account was just created via "Add staff" (school-admin) or "Add..." (system-admin/users), still in PENDING_VERIFICATION state. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Staff & User Account Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Staff & User Account Management (Manual Web) |
| Test requirement | Create/view/lock school staff accounts (`/school-admin/staff`) and platform-wide user accounts (`/system-admin/users`), ensuring status is always decided by the server. |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`/school-admin/staff`** | | | | | | | | | | | | | | |
| MF01-WEB-012 | School Admin creates a new staff member, may only pick a school-level role. | 1. Log in as School Admin, go to `/school-admin/staff`.<br>2. Click "Add staff".<br>3. Fill in Name, Email, Phone, pick a Role (check the role list only shows Teacher/Medical Staff/Catering, no System Admin/School Admin).<br>4. Click "Create account (MSG-031)". | The role dropdown does NOT contain any admin role. After creation, the new staff member appears in the table with "pending verification"/not-active status — never active immediately no matter what else is set on the form. | Logged in as a valid School Admin, manages at least one school. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-013 | The linked school is always the current School Admin's own school, no picker for another school. | 1. On the "Add staff" form, check there is no "School" selector anywhere on the form (unlike `/system-admin/users`). | The staff-creation form has NO school selector — new staff always belong to the currently logged-in School Admin's school. | Logged in as School Admin. | Pending | | | Pending | | | Pending | | | The server always derives schoolId from the JWT, never trusts the form |
| **`/system-admin/users`** | | | | | | | | | | | | | | |
| MF01-WEB-014 | System Admin creates a school-level user, linked school is required. | 1. Log in as System Admin, go to `/system-admin/users`.<br>2. Click "Add...".<br>3. Pick a school-level Role (Teacher/School Admin/...).<br>4. Check that the "Linked school *" field appears and is required.<br>5. Fill in the rest, click "Create account (MSG-031)". | The "Linked school" field is required when a school-level role is chosen. The new account is in pending-verification state, not active immediately. | Logged in as a valid System Admin. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-015 | Disable an active account, confirm the session is revoked. | 1. On `/system-admin/users`, find one active account, log in in parallel with that account in another browser/tab (already logged in).<br>2. In the System Admin tab, change that account's status to "Disable".<br>3. Go back to the already-logged-in tab, try any action (e.g. reload the page). | After step 2, the account's status changes immediately in the list. At step 3, the other tab is bounced to `/login` (session revoked immediately, no need to wait for JWT expiry). | A test account exists that is not the primary System Admin, is active, and already logged in on another browser/tab. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Role Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Role Management (Manual Web) |
| Test requirement | Create/edit/delete custom roles at `/system-admin/roles`, ensuring system roles cannot be renamed and cannot be deleted while still assigned to users. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF01-WEB-016 | Edit the description/permissions of a system role (not renaming it) — must be allowed. | 1. Go to `/system-admin/roles`.<br>2. Pick an existing system role (e.g. "Teacher"), click edit.<br>3. Only edit the Description or toggle permissions, KEEP the role Name unchanged, save. | Saves successfully — description/permissions are updated. This is the CORRECT behavior (system roles only lock the NAME, not the description/permissions). | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | Docs previously incorrectly stated system roles cannot be edited at all — corrected |
| MF01-WEB-017 | Try to change the NAME of a system role — must be blocked. | 1. On the system role edit form, try to change the Role Name to something else, save. | Blocked, shows an error message — a system role's name cannot be changed. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-018 | Delete a custom role that still has users assigned. | 1. Create a new custom role.<br>2. Assign that role to one account (via creating a new user with this role, if the UI allows it).<br>3. Go back to `/system-admin/roles`, try to delete that role. | Deletion is blocked, shows a warning with the count of users assigned to this role — all users must be moved to another role first. | A custom role exists with at least one account assigned. | Pending | | | Pending | | | Pending | | | BR-008 |

---

## Feature: Profile Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Profile Management (Manual Web) |
| Test requirement | View/edit personal profile and change password via the avatar menu (all roles). |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF01-WEB-019 | Change password from the avatar menu. | 1. Log in, click the avatar in the topbar → "Change password".<br>2. Enter the correct current password, a valid new password + confirmation, click "Save password (MSG-030)". | Saved successfully, dialog closes. Logging in again with the new password succeeds. | Already logged in. | Pending | | | Pending | | | Pending | | | |
| MF01-WEB-020 | Edit personal profile — name, phone, avatar photo. | 1. Click the avatar → "Edit Profile".<br>2. Edit Name, Phone number, upload a valid JPG/PNG photo.<br>3. Click "Save (MSG-029)". | Saved successfully, the new information shows immediately in the topbar/dialog. | Already logged in. | Pending | | | Pending | | | Pending | | | ⚠️ See the note at the end of the file — the UI says "≤5MB" but the real backend allows up to 10MB and also accepts PDF |
| MF01-WEB-021 | Try uploading an oversized or wrong-format avatar photo. | 1. In the "Edit Profile" dialog, pick a file larger than the size shown as allowed on the UI, or a `.gif`/`.exe` file renamed to `.jpg`.<br>2. Click "Save". | Rejected with a clear error message (wrong format or too large) — does not save a file whose extension was faked. | Already logged in. | Pending | | | Pending | | | Pending | | | The backend checks the file's real magic bytes, not just the extension |
