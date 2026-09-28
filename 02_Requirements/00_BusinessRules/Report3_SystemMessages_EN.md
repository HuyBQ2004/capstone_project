# Appendix — System Messages (EN, for Report 3)

> **Source of truth**: `02_Requirements/00_BusinessRules/error_message.md` (v1.0, 25/06/2026) — the **only** canonical Message source.
> The System Messages appendix currently embedded in `Report 3.md` (~line 6390) is **outdated** and must be **replaced** by this table, not merged with it.
> Paste the tables below directly into Report 3's System Messages appendix section.

---

## 1. Error Messages

Error messages block the user's action and require a corrective step.

| MSG Code | Message Content | BR Reference |
|---|---|---|
| MSG-001 | System error while saving school data. Please try again. | — |
| MSG-002 | Invalid credentials. Please check your email and password. | — |
| MSG-003 | Your account has been deactivated. Please contact your administrator. | — |
| MSG-004 | Session expired. Please log in again. | BR-004 |
| MSG-005 | Access denied: You do not have permission to access this resource. | — |
| MSG-006 | Registration failed: The email address is already in use. | BR-012 |
| MSG-007 | Invalid tax ID format. Please verify and try again. | — |
| MSG-008 | File upload failed: File size exceeds the maximum limit of 10MB. | ⚠ see note① |
| MSG-009 | File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed. | — |
| MSG-010 | Classroom capacity exceeded. Maximum allowed is 50 students. | BR-025 ⚠ see note② |
| MSG-011 | Homeroom teacher is already assigned to another classroom. | BR-020 |
| MSG-012 | Student admission code is invalid or has already been used. | — |
| MSG-013 | Supplier food safety certificate has expired. Onboarding blocked. | BR-046, BR-048 |
| MSG-014 | Face recognition failed: Biometric identity could not be verified. | BR-051 |
| MSG-015 | Receiving action blocked: GPS location verification failed (out of tolerance). | BR-074, BR-079 |
| MSG-016 | Checklist validation failed: Mandatory safety checks are missing. | BR-095 |
| MSG-017 | Cooking temperature too low: Meal core temperature must reach 75°C. | BR-090, BR-093 |
| MSG-018 | Inventory shortage: Insufficient stock of ingredient [Ingredient Name]. | BR-077 |
| MSG-019 | Food sample retention weight too low: Must be at least 100g. | BR-107 ⚠ see note③ |
| MSG-020 | Retention refrigerator temperature is out of safe range (2–8°C). | BR-110 |
| MSG-021 | Duplicate meal distribution: Student has already scanned for this session. | BR-130 |
| MSG-022 | Feedback submission blocked: Outside the 24-hour limit of meal serving. | BR-139 |
| MSG-023 | Incident report incomplete: Symptoms, date of ingestion, and dishes are required. | — |
| MSG-024 | Kitchen quarantine is active: Operation blocked due to safety violations. | BR-105 ⚠ see note④ |
| MSG-025 | Database transaction failed. Rollback executed. | — |
| MSG-075 | Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week. | BR-068 ⚠ see note⑤ |
| MSG-076 | Account temporarily locked due to too many failed login attempts. Please try again after [Minutes] minutes. | BR-002 |
| MSG-077 | Action blocked: Internet connection required to verify this safety check. Please reconnect and try again. | BR-197 |

**Notes on citations to verify with the team before pasting into Report 3** (found while cross-checking `error_message.md` against canonical `business_rules.md`):
- ① MSG-008: original source cites "BR-013", which is **Staff account creation authority** — unrelated to file size limits. No canonical BR currently governs upload size caps. Left blank pending a real BR; do not cite BR-013.
- ② MSG-010: original source cites "BR-025/BR-019". **BR-019 does not exist** in canonical `business_rules.md` (confirmed by direct search). BR-025 (max 50 students) is correct and sufficient; drop the BR-019 reference.
- ③ MSG-019: original source cites "BR-107" (mandatory sample photos), which does not actually specify a 100g minimum weight. No canonical BR currently defines a minimum sample weight — likely an old/dropped rule. Flag as TODO for the team rather than citing BR-107.
- ④ MSG-024: original source cites "BR-105" (mandatory retention after passed inspection), which does not define kitchen quarantine. Quarantine is actually governed by **BR-149** (automatic batch quarantine on incident) — corrected here.
- ⑤ MSG-075: "twice a week" dish-rotation limit is not stated in BR-068 (menu approval workflow) or anywhere else in canonical `business_rules.md`. No BR currently defines a rotation cap — flag as TODO for the team.

---

## 2. Success Messages

Confirmation messages indicating an action completed successfully.

| MSG Code | Message Content |
|---|---|
| MSG-026 | Login successful. Welcome back, [Name]! |
| MSG-027 | Logout successful. Session terminated safely. |
| MSG-028 | Registration request submitted! A verification email has been sent. |
| MSG-029 | Profile updated successfully. |
| MSG-030 | Password changed successfully. |
| MSG-031 | User account created successfully. |
| MSG-032 | School registered successfully. Pending System Admin approval. |
| MSG-033 | Classroom created successfully. |
| MSG-034 | Homeroom teacher assigned successfully. |
| MSG-035 | Student profiles and parent links imported successfully. |
| MSG-036 | Allergy profile updated successfully and alerts set. |
| MSG-037 | Supplier onboarding application approved successfully. |
| MSG-038 | Delivery staff face biometrics registered successfully. |
| MSG-039 | Menu draft submitted for approval successfully. |
| MSG-040 | Menu approved and published successfully. Notifications sent to parents. |
| MSG-041 | Ingredient delivery received. Inventory levels updated. |
| MSG-042 | Inventory transaction recorded and stock adjusted. |
| MSG-043 | Food safety inspection checklist submitted successfully. |
| MSG-044 | Food sample retention registered. Cold storage timer initiated. |
| MSG-045 | Meal distribution list confirmed for class [Class Name]. |
| MSG-046 | Meal feedback submitted successfully. Thank you for your feedback! |
| MSG-047 | Announcement published successfully to target audience. |
| MSG-048 | Suspected food safety incident report logged. Investigation map generated. |
| MSG-049 | Corrective actions plan submitted successfully for review. |
| MSG-050 | System database backup completed successfully and uploaded to secure cloud. |
| MSG-071 | Menu draft was saved successfully. |
| MSG-072 | Special meal plan saved successfully. |
| MSG-074 | Menu updated successfully. |
| MSG-078 | Saved offline. Will sync automatically once connection is restored. |

---

## 3. Warning Messages

Warnings that do not block the action but require attention or preventive follow-up.

| MSG Code | Message Content | BR Reference |
|---|---|---|
| MSG-051 | Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students. | BR-063 |
| MSG-052 | Ingredient batch [Batch ID] is close to expiration ([Expiration Date]). | BR-047, BR-083 |
| MSG-053 | Supplier food safety certificate is expiring in [Number] days. | BR-047 |
| MSG-054 | Safety stock level reached for ingredient [Ingredient Name]. Reorder recommended. | BR-077 ⚠ see note⑥ |
| MSG-055 | High temperature alert in inventory storage area: [Temperature]°C. | BR-089 |
| MSG-056 | Re-inspection required: Failed safety check during inspection step [Step Name]. | BR-096, BR-098 |
| MSG-057 | Negative meal feedback rating (<3 stars) received from parent. | BR-140 |
| MSG-058 | Illness keywords detected in parent feedback. Potential incident flag. | BR-142 |
| MSG-059 | Retention refrigerator door left open or temperature fluctuation detected. | BR-110, BR-089 |
| MSG-060 | Supplier monthly performance index score has dropped below 70%. | — |
| MSG-061 | System load threshold reached. Auto-scaling initiated. | — |
| MSG-062 | Suspicious login attempt detected from a new location/device. Verification code required. | BR-002 |
| MSG-063 | Weak password rating. Recommend adding special characters and numbers. | BR-006 |
| MSG-064 | Incomplete student health profiles detected for class [Class Name]. | BR-031 |
| MSG-065 | Delivery delay alert: Supplier delivery time deviates from schedule. | BR-135 |
| MSG-066 | Weekly sanitation audit report is overdue. Please submit immediately. | — |
| MSG-067 | AI menu analysis: Nutritional diversity is below the required 3-group minimum. | BR-062 |
| MSG-068 | Push notification delivery queue is backed up. Retrying failed targets. | BR-159 |
| MSG-069 | External API response latency is high (exceeds 2000ms). | BR-163 |
| MSG-070 | Backup space alert: Backup storage utilization is at 92% capacity. | BR-160 |
| MSG-073 | Gemini AI Service is currently unavailable. Please try again later. | — |
| MSG-079 | Cached data is outdated ([Number] hours old). Please reconnect to sync before continuing. | BR-200 |

- ⑥ MSG-054: original source cites "BR-077/BR-084". **BR-084 does not exist** in canonical `business_rules.md`. BR-077 (low-stock alert) is correct and sufficient; drop the BR-084 reference.

---

## 4. Placeholder Legend

| Placeholder | Description |
|---|---|
| `[Name]` | Full name of the logged-in user |
| `[Dish Name]` | Specific dish name |
| `[Allergen]` | Name of the allergen |
| `[Number]` | Count of affected students / number of days |
| `[Batch ID]` | Ingredient batch number |
| `[Expiration Date]` | Specific expiry date |
| `[Ingredient Name]` | Specific ingredient name |
| `[Temperature]` | Measured temperature value |
| `[Step Name]` | Inspection step name (Step 1/2/3) |
| `[Class Name]` | Class name |
| `[Minutes]` | Remaining lockout time in minutes |

---

## 5. Summary by Module

| Module | MSG Codes |
|---|---|
| User Management & Authentication | MSG-002–006, MSG-026–031, MSG-062, MSG-063, MSG-076 |
| School & Class Management | MSG-001, MSG-007, MSG-010, MSG-011, MSG-032–034 |
| Student & Parent Management | MSG-012, MSG-035, MSG-036, MSG-064 |
| Supplier & Partner Management | MSG-013, MSG-014, MSG-037, MSG-038, MSG-053, MSG-060 |
| Ingredient & Inventory Management | MSG-008, MSG-009, MSG-015, MSG-018, MSG-041, MSG-042, MSG-052, MSG-054, MSG-055, MSG-065 |
| Menu & Diet Management | MSG-039, MSG-040, MSG-051, MSG-067, MSG-071–075 |
| Food Safety Inspection | MSG-016, MSG-017, MSG-043, MSG-056 |
| Food Sample Retention | MSG-019, MSG-020, MSG-044, MSG-059 |
| Meal Serving & Distribution | MSG-021, MSG-045 |
| Feedback & Announcement | MSG-022, MSG-046, MSG-047, MSG-057, MSG-058 |
| Incident Management | MSG-023, MSG-024, MSG-048, MSG-049 |
| AI & System Services | MSG-025, MSG-050, MSG-061, MSG-068–070, MSG-073 |
| Offline & Data Sync | MSG-077, MSG-078, MSG-079 |

**Total: 79 System Messages** (28 Error + 29 Success + 22 Warning, non-contiguous numbering by design — this matches Report 3's original numbering scheme, do not renumber sequentially).
