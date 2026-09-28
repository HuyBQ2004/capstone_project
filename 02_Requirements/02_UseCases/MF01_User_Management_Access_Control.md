# MF01 – User Management & Access Control

---

## Active/ Deactivate Staff

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-002 - Active/Deactivate Staff | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | School Administration selects a staff account and toggles its active/inactive status. | | |
| Description | Enables the School Administration to activate or deactivate a staff member's account. Deactivation immediately terminates all active sessions for that staff. | | |
| Preconditions | The school administration is authenticated and authorized. The target staff account exists in the school. | | |
| Postconditions | Staff account status is updated. If deactivated, all active sessions are immediately terminated. An email notification is sent to the staff member. Audit log records the action. | | |
| Normal Flow | 1. School Administration navigates Staff Account Management. 2. System displays all staff accounts with their current active/inactive status. 3. School Administration selects a target staff account and clicks the status toggle. 4. System checks that deactivating this account will not leave mandatory role assignments unfilled. 5. System updates the account status, terminates active sessions if deactivated (BR-007), sends a status update email to the staff member via the Email Service, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If deactivation leaves mandatory roles unfilled, system blocks the action and displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On demand | | |
| Business Rules | BR-007, BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ Active/Deactivate Staff Use Cases Specification

## Active/Deactivate User

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-003 - Active/Deactivate User | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | Email Service |
| Trigger | System Admin selects a user account and toggles its active/inactive status. | | |
| Description | Enables the System Admin to activate or deactivate any user account platform wide. Deactivation immediately terminates all active sessions and revokes all access. | | |
| Preconditions | The system admin is authenticated and authorized. The target user account exists and is not the default primary system admin account. | | |
| Postconditions | User account status is updated. If deactivated, all active sessions are immediately terminated (BR-007). An email notification is sent to the user. Audit log records the action. | | |
| Normal Flow | 1. System Admin navigates to User Account Management. 2. System displays all user accounts with statuses. 3. System Admin selects the target account and clicks the status toggle. 4. System verifies the account is not the protected primary default system admin account.  5. The system checks whether locking this user's account voids the unique required role at the affiliated institution/school. If so, a transaction blocking warning is displayed. 6. System updates the status, terminates all active sessions if deactivated (BR-007), sends a status update email to the user via the Email Service, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If trying to deactivate the primary system admin, system blocks the action and displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-007, BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ Active/Deactivate User Use Cases Specification

## Change Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-011 - Change Password | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user selects 'Change Password' from account settings. | | |
| Description | Enables the authenticated User to securely change their account password after verifying their current credentials, invalidating all other active sessions upon success. | | |
| Preconditions | The user is authenticated and has an active session. | | |
| Postconditions | The user password is updated. All existing sessions except the current one are invalidated. A password change confirmation notification is sent. | | |
| Normal Flow | 1. User navigates to Account Settings and selects 'Change Password'.  2. System displays the Change Password form with current password, new password, and confirmation fields.  3. User enters current password, new password, and confirms the new password.  4. System validates the current password and verifies the new password matches the confirmation.  5. System validates the new password against the password policy and password history (BR-006: must not match last 3 passwords).  6. System hashes the new password (BR-001), saves it to the database, terminates all other active sessions for this user, sends a confirmation notification, and displays MSG-030. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-002 (Invalid current password), MSG-004 (Session expired), MSG-005 (Access denied), MSG-063 (Weak password or policy violation). | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-001, BR-006, BR-007 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and access to their registered email. | | |

Table 3-4 Change Password Use Cases Specification

## Create Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-022 - Create Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin navigates to Role Management and selects 'Create New Role'. | | |
| Description | Enables the System Admin to define a new custom user role with a unique name, description, and specific permission assignments. | | |
| Preconditions | The system admin is authenticated and authorized. The proposed role name does not already exist in the system. | | |
| Postconditions | The new custom role is saved in the database and becomes available for assignment to user accounts. | | |
| Normal Flow | 1. System Admin navigates to Role Management and selects 'Create New Role'.  2. System displays the role creation form with name, description, and permission assignment fields.  3. System Admin enters a unique role name, description, and assigns specific permissions.  4. System validates the role name is unique and all assigned permissions are valid.  5. System saves the role, records the creation in the audit trail, and displays a success confirmation (MSG-031). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If the role name is already in use, system displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-5 Create Role Use Cases Specification

## Create Staff

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-024 - Create Staff | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | School Administration navigates to Staff Management and selects 'Create Staff Account'. | | |
| Description | Enables the School Administration to create a new staff account, assigning role and contact details, triggering an email verification for account activation. | | |
| Preconditions | The school administration is authenticated and authorized. The target email address is not already registered in the system. | | |
| Postconditions | Staff account created with 'Pending Email Verification' status. Verification email sent to staff member (BR-010). Account inactive until email verified. | | |
| Normal Flow | 1. School Administration navigates to Staff Management and selects 'Create Staff Account'. 2. System displays the staff creation form. 3. School Administration enters full name, email address, assigned role, and phone number. School Administration cannot choose which school the staff belongs to — System always assigns the acting School Administration's own school (taken from their session, not the request payload), so a School Admin can never create staff for another school. 4. System validates that the assigned role is a school-level staff role (Teacher, Medical Staff, Catering staff) and not an administrator or external role — this is enforced server-side even if bypassed via a direct API call. 5. System validates email uniqueness (MSG-006 if duplicate) and phone number format (BR-012: valid +84 or 0xx format). 6. System creates the account with status forced to 'PENDING_VERIFICATION' and no password set regardless of any status/password value sent by the client, sends a verification email (BR-010), logs the creation, and displays MSG-031. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006. If phone number format is invalid, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-6 Create Staff Use Cases Specification

## Create User

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-026 - Create User | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | Email Service |
| Trigger | System Admin navigates to User Management and selects 'Create User Account'. | | |
| Description | Enables the System Admin to create a user account for any system actor, assigning credentials, roles, and triggering email verification. | | |
| Preconditions | The system admin is authenticated and authorized. The target email address is not already registered. | | |
| Postconditions | New user account created. Verification email sent (BR-010). Account inactive until verified. Creation logged in audit trail. | | |
| Normal Flow | 1. System Admin navigates to User Management and selects 'Create User Account'. 2. System displays the user creation form. 3. System Admin enters full name, email, assigned role, and phone number (If the selected role is at the school level, the System Admin is required to select the affiliated school from the list). 4. System validates email uniqueness (MSG-006 if duplicate) and phone format (BR-012: valid +84 or 0xx format). 5. System creates the account with status forced to 'PENDING_VERIFICATION' and no password set regardless of any status/password value sent by the client, sends verification email (BR-010), logs creation in audit trail, and displays MSG-031. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006. If phone format is invalid, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008, BR-010, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-7 Create User Use Cases Specification

## Delete Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-029 - Delete Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin selects a custom role and clicks 'Delete Role'. | | |
| Description | Enables the System Admin to permanently remove a custom role, provided it is not a default system role and has no active users assigned. | | |
| Preconditions | The system admin is authenticated and authorized. The role exists, is a custom (non-default) role (BR-008), and has no active users currently assigned. | | |
| Postconditions | Custom role is permanently deleted from the database. Audit log records the deletion. | | |
| Normal Flow | 1. System Admin navigates to Role Management and selects the target custom role.  2. System displays role details including assigned user count.  3. System Admin clicks 'Delete Role' and confirms the action in the confirmation dialog.  4. System validates the role is not a default system role (BR-008) and has zero users (active or inactive) assigned.  5. System deletes the role, records the deletion in the audit trail, and displays a success confirmation. | | |
| Alternative Flows | AF1 - Role Has Assigned Users: System blocks deletion and shows the count of assigned users. System Admin must reassign all users to other roles before deletion can proceed. | | |
| Exceptions | MSG-004, MSG-005. If target role is a default role, deletion is blocked with a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-8 Delete Role Use Cases Specification

## Forgot Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-032 - Forgot Password | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Email Service |
| Trigger | User clicks 'Forgot Password' on the login screen and submits their registered email. | | |
| Description | Enables the User to reset their account password via a time-limited email link without requiring current password knowledge. | | |
| Preconditions | The user account exists and is active. The user has access to their registered email address. | | |
| Postconditions | A password reset link is sent to the registered email. Reset token expires in 1 hour (BR-003). Password is updated on successful token verification. | | |
| Normal Flow | 1. User clicks 'Forgot Password' on the Login screen.  2. System displays the email input form.  3. User enters their registered email address and submits.  4. System **always returns the same success response** regardless of whether the email exists, the account can log in, or the request is rate-limited — this is intentional (prevents attackers from discovering which emails are registered). If eligible, System generates a secure reset token (1-hour expiry per BR-003, hashed before storage, one-time-use) and sends the reset link via Email Service; rate-limited to 1 request per 15 minutes per email (measured from the previous token's creation time).  5. User clicks the link, enters a new password and confirms it.  6. System validates the token is still valid and unused, matches password confirmation, enforces password policy **and** the last-3-passwords reuse check (BR-006), hashes the password (BR-001), saves it, marks the token used, revokes all active sessions for the user, and displays MSG-030. | | |
| Alternative Flows | AF1 - Token Expired: User accesses an expired reset link. System displays token expiry error and prompts user to request a new reset link. | | |
| Exceptions | Deliberately **no** distinct "account deactivated" or "email not found" error at step 4 — the request always appears to succeed (non-enumeration). At step 6, invalid/expired/already-used token, mismatched confirmation, or policy/reuse violation each show their own validation error. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-001, BR-003, BR-006 | | |
| Other Information | Rate-limiting applies: maximum 1 reset request per 15 minutes per email. | | |
| Assumptions | User has access to their registered email account. | | |

Table 3-9 Forget Password Use Cases Specification

## Login

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-034 - Login | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | User opens the app and inputs credentials. | | |
| Description | Authenticates User credentials and establishes an authorized system session. | | |
| Preconditions | User has a registered account and a stable internet connection. | | |
| Postconditions | User is logged in and redirected to their role-based dashboard. | | |
| Normal Flow | 1. User enters their registered email and password on the login screen.  2. System looks up the account by email; if not found, System still runs a dummy password comparison (timing-attack resistance) before returning the same "invalid credentials" error as a wrong password — email existence is never revealed (MSG-002).  3. System checks the account is not locked out from excessive failed attempts (BR-002: threshold and lockout duration read from the active Security Policy, not hardcoded — default 5 attempts / 15 minutes). A locked account is rejected even if the password is later found correct.  4. System validates the email/password against the stored BCrypt hash; on mismatch, System increments the failed-attempt counter and, if the threshold is reached, locks the account and force-revokes all its active sessions (BR-002).  5. System checks the account can log in (BR-010: status must be ACTIVE and email verified); PENDING/INACTIVE/SUSPENDED accounts are rejected with MSG-003 before password state is touched.  6. If the account is linked to a school, System checks the school itself is ACTIVE; login is blocked if the school is not active (System Admin accounts, which have no school, are exempt).  7. System checks whether Terms of Service have been accepted yet (BR-009); if not, the response flags `needTermsAccept` so the client redirects to the acceptance screen before the dashboard.  8. System revokes any existing session for the user and issues a new access token + refresh token (BR-004/BR-005: exactly one active session per user — a new login always invalidates the previous one), resets the failed-login counter, updates last-login time, records the login in the audit trail, displays MSG-026, and redirects to the role-based dashboard. | | |
| Alternative Flows | AF1 - First-time Login (BR-009): Login succeeds but the response flags `needTermsAccept`; client redirects to the Terms of Service acceptance screen. User must accept (separate "Accept Terms" action) before continuing to use the app; System records the acceptance timestamp once and does not prompt again.  AF2 - Account Lockout (BR-002): If the user fails login enough consecutive times to reach the policy threshold, System locks the account for the configured duration and revokes all its sessions. | | |
| Exceptions | MSG-002 (Invalid credentials — used for both "email not found" and "wrong password", never distinguished). MSG-003 (Account not active, or account's school is not active). Lockout returns a dedicated "account locked" error with the remaining attempts/lock time, not MSG-002. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-002, BR-004, BR-005, BR-009, BR-010 | | |
| Other Information | *(Cập nhật 11/08/2026 theo code thật)* Step 6 (chặn đăng nhập nếu trường liên kết không ACTIVE) đã được triển khai trong `AuthService.completeLogin` nhưng **chưa có mã BR riêng** trong `business_rules.md` — cần Nam/Huy chốt bổ sung BR mới hoặc gộp vào BR-010. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-10 Login Use Cases Specification

## Logout

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-035 - Logout | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user selects 'Logout' from the navigation menu. | | |
| Description | Enables the authenticated User to securely terminate their active system session, invalidating the session token and redirecting to the login screen. | | |
| Preconditions | The user is authenticated with an active session. | | |
| Postconditions | User session is terminated. Session token is invalidated. User is redirected to the Login screen. Session termination is logged. | | |
| Normal Flow | 1. User clicks 'Logout' from the navigation menu or header.  2. System displays a logout confirmation prompt.  3. User confirms the logout action.  4. System invalidates the session token (BR-004) and clears session data.  5. System logs the session termination, redirects user to the Login screen, and displays MSG-027. | | |
| Alternative Flows | AF1 - Auto-Logout on Session Timeout: If the session has expired (30 min inactivity per BR-004), system automatically logs out the user and redirects to Login screen with a session expiry notification. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-004 | | |
| Other Information | None. | | |
| Assumptions | User has an active session in the system. | | |

Table 3-11 Logout Use Cases Specification

## Profile Manage

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-059 - Profile Manage | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user navigates to 'My Profile' from the navigation menu. | | |
| Description | Enables the authenticated User to access their personal profile hub containing details, role assignments, contact info, and account actions. | | |
| Preconditions | The user is authenticated and has an active session. | | |
| Postconditions | User profile information is displayed. No data modifications occur unless the user initiates an edit action. | | |
| Normal Flow | 1. User navigates to 'My Profile' from the navigation menu.  2. System retrieves and displays the user's current profile data hub (UC-140): name, email, phone, role, and status.  3. User reviews their profile information.  4. User may choose to edit profile details (redirecting to UC-099) or change password (redirecting to UC-011). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-012, BR-013 | | |
| Other Information | This is the parent management page linking to detailed profile view/update and password change functions. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-12 Profile Manage Use Cases Specification

## Update Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-097 - Update Profile | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user selects 'Edit Profile' from the Profile Management page. | | |
| Description | Enables the authenticated User to update their personal profile information including name, phone number, and profile photo. | | |
| Preconditions | The user is authenticated and has an active session. | | |
| Postconditions | Profile data is updated in the database. Audit log entry is created. A success confirmation notification is displayed (MSG-029). | | |
| Normal Flow | 1. User navigates to Profile Management and selects 'Edit Profile'.  2. System displays an editable form pre-filled with current profile data.  3. User modifies fields: name, phone number (BR-012: valid format +84 or 0xx), or uploads a new profile photo (BR-013: JPG/JPEG/PNG/PDF, max 10MB — System checks both the declared content-type and the file's actual magic bytes, so a renamed file cannot bypass the type check).  4. System validates phone number format. System also validates photo file type and size constraints.  5. System saves the updated profile, creates an audit log entry, displays MSG-029, and refreshes the profile view. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If photo exceeds 10MB, system displays MSG-008. If photo format is unsupported (or magic bytes don't match a declared type), system displays MSG-009. If phone number format is invalid, system displays validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-012, BR-013 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-13 Update Profile Use Cases Specification

## Update Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-098 - Update Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin requests to update role. | | |
| Description | Enables the System Admin to manage details, modify permissions, or update configuration fields of a custom role. | | |
| Preconditions | The system admin is authenticated and authorized. The target role exists. | | |
| Postconditions | The update role transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. System Admin navigates to Role Management and selects the target Role to update.  2. System displays the edit role form with current role name, description, and permissions.  3. System Admin modifies the role description, permissions, and — only for custom (non-default) roles — the role name.  4. System Admin submits the modifications.  5. System validates that, if the role is a **default system role**, only description/permissions changed (renaming a system role is blocked, BR-008); for custom roles, the new name (if changed) must be unique.  6. System saves the updated role, records the action in the audit trail, and displays a success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If trying to rename a default system role, system blocks the action and displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-14 Update Role Use Cases Specification

## View List Permission

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-129 - View List Permission | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view list permission. | | |
| Description | Enables the System Admin to safely query, view, or export the details of List Permission records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view list permission data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the List Permission section in the system portal.  2. System queries the database for all available permissions in the system.  3. System displays the list of permissions in a tabular view with columns: Permission ID, Name, Module/Category, and Description.  4. System Admin can search permissions by keyword, filter by module, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-15 View List Permission Use Cases Specification

## View List Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-130 - View List Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view list role. | | |
| Description | Enables the System Admin to safely query, view, or export the details of List Role records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view list role data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the List Role section in the system portal.  2. System queries the database for all user roles (both default and custom).  3. System displays the list of roles in a tabular view showing Role Name, Description, User Count, and Status.  4. System Admin can search, filter, or navigate to see details of a specific role. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-16 View List Role Use Cases Specification

## View Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-138 - View Profile | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Primary actor requests to view profile. | | |
| Description | Enables the User to safely query, view, or export the details of Profile records. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | The view profile data is successfully retrieved and displayed to the user. | | |
| Normal Flow | 1. User navigates to the Profile section in the system portal (or UC-059 requests data).  2. System queries the database for the user's profile details: name, email, phone, role, profile photo, and registration details.  3. System returns and displays the profile details on the user interface.  4. User reviews their profile details. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-012, BR-013 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-17 View Profile Use Cases Specification

## View Staff Account List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-144 - View Staff Account List | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | Primary actor requests to view staff account list. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Staff Account List records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view staff account list data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Staff Account List section in the system portal.  2. System queries the database to retrieve staff accounts belonging to the school.  3. System displays the list of staff members (name, email, role, phone, active status) in a table view.  4. School Administration can filter by role, active status, search by name/email, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-18 View Staff Account List Use Cases Specification

## View User Account List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-153 - View User Account List | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | Email Service |
| Trigger | Primary actor requests to view user account list. | | |
| Description | Enables the System Admin to safely query, view, or export the details of User Account List records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view user account list data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the User Account List section in the system portal.  2. System queries the database to retrieve all user accounts across the platform.  3. System displays the platform-wide user account list (name, email, role, school/company, active status) in a tabular view.  4. System Admin can search, filter by role/school/status, and export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-19 View User Account List Use Cases Specification

## Activate Account & Set Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-154 - Activate Account & Set Password | | |
| Created By | NamNVHE180863 | Date Created | 23/06/2026 |
| Primary Actor | User | Secondary Actors | Email Service |
| Trigger | User clicks the account activation link in the received verification email. | | |
| Description | Enables a new staff member or user to activate their account, set their initial password, and transition their account status from PENDING\_VERIFICATION to ACTIVE. | | |
| Preconditions | The target user account exists with status 'PENDING\_VERIFICATION'. The user has a valid, unused activation token. | | |
| Postconditions | The user account status is updated to 'ACTIVE'. The initial password is set and encrypted. The verification token is invalidated. The activation is logged in the audit trail. | | |
| Normal Flow | 1. User clicks the activation link in their email. 2. System extracts and validates the activation token: checking its validity and verifying that it has not expired (must be within 48 hours of creation per BR-011). 3. System displays the activation form with "New Password" and "Confirm Password" fields. 4. User enters their new password, confirms it, and clicks the "Activate Account" button. 5. System verifies that the new password matches the confirmation and validates password strength against the security policy (BR-006). 6. System hashes the password using BCrypt (BR-001) and saves it to the database. 7. System updates the account status to 'ACTIVE', invalidates the activation token, records the activation in the audit trail, displays MSG-031, and redirects the user to the Login screen. | | |
| Alternative Flows | AF1 - Activation Token Expired (> 48 hours): 1. User accesses an expired activation link. 2. System detects token expiration, displays an expiration error message, and provides a "Resend Activation Link" button. 3. User clicks "Resend Activation Link". 4. System generates a new activation token, extends it for another 48 hours, sends a new activation email via Email Service, and displays a success notification. | | |
| Exceptions | - Invalid Token: System blocks access and displays a validation error message. - Account Already Active: System redirects the user to the Login screen with an information message. | | |
| Priority | High | | |
| Frequency of Use | On-demand (when a new staff/user is onboarded) | | |
| Business Rules | BR-001, BR-006, BR-010, BR-011, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with an internet connection and access to their registered email. | | |

Table 3-19 Activate Account & Set Password Use Cases Specification

## Login with Google (OAuth2)

*(Bổ sung 2026-07-28 — theo yêu cầu người dùng (Nam): hỗ trợ đăng nhập nhanh qua Google, giảm phụ thuộc email/mật khẩu. UC mới, cấp BR-218 tương ứng — xem `business_rules.md` changelog v1.9. Chỉ liên kết với tài khoản đã tồn tại, không tự tạo tài khoản mới qua Google — BR-013 vẫn do School Administration/System Admin kiểm soát việc tạo tài khoản.)*

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-191 - Login with Google (OAuth2) | | |
| Created By | Claude (theo yêu cầu Nam) | Date Created | 28/07/2026 |
| Primary Actor | User | Secondary Actors | Google OAuth2 Provider |
| Trigger | User selects "Sign in with Google" on the Login screen instead of entering email/password. | | |
| Description | Enables an existing User to authenticate using their Google account (OAuth2 Authorization Code flow) instead of email/password, linking to their existing SFMS account by matching verified email. | | |
| Preconditions | A SFMS account already exists with an email matching the Google account's verified email (created via UC-024/UC-026). The account is not INACTIVE/SUSPENDED (BR-010). | | |
| Postconditions | User is authenticated and issued an access token + refresh token (BR-004). If this is the first Google login, the account is linked to the Google identity (`googleSub`) for future logins. Audit log records the login. | | |
| Normal Flow | **Web:** 1. User clicks "Sign in with Google" on the Login screen. 2. System (Spring Security OAuth2 client) redirects to the Google consent screen. 3. User authenticates and approves. 4. Google redirects back with an authorization code; System exchanges it for the user's verified Google profile (OIDC claims). **Mobile:** 1. User taps "Sign in with Google"; the Google SDK returns a raw Google `id_token` directly to the app. 2. App sends the `id_token` to `POST /auth/google`; System verifies it manually against Google's public token-info endpoint (checks issuer, audience matches the configured client ID, and that email/sub claims are present) since the mobile app does not go through the redirect flow. **Shared from here:** 5. System rejects the login if Google reports the email as unverified. 6. System looks up an existing SFMS account by email; if none exists, System rejects the login and **never creates a new account** (Google login is link-only, BR-218 — account creation stays with School Administration/System Admin via UC-024/UC-026). 7. If found and not yet linked to any Google identity, System links the account by storing `googleSub`. If already linked to a *different* `googleSub` than the one presented, System rejects the login (prevents a same-email, different-Google-account takeover). 8. System re-applies the same account-status (BR-010) and lockout (BR-002) checks as password login. 9. System revokes any existing session and issues a new access token + refresh token (BR-004/BR-005), records the login in the audit trail, and redirects to the role-based dashboard. | | |
| Alternative Flows | AF1 - Already linked to the same Google identity: skip the linking step (step 7), log in directly. | | |
| Exceptions | No matching SFMS account for the Google email, or Google email not verified: system blocks login and displays MSG-102. Google identity already linked to this email but the incoming `sub` doesn't match (possible account-takeover attempt): system blocks login. Account locked/inactive/school inactive: same errors as password login (MSG-002/MSG-003). Token exchange/verification fails: system displays MSG-103. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-004, BR-005, BR-010, BR-218 | | |
| Other Information | Does not apply BR-001/BR-006/BR-015 (no local password for a Google-authenticated session). | | |
| Assumptions | User has a running environment with internet connection and a Google account whose email matches their SFMS account. | | |

Table 3-20 Login with Google Use Cases Specification
