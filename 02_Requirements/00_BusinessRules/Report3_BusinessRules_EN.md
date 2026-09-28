# Appendix — Business Rules (EN, for Report 3)

> **Source of truth**: `02_Requirements/00_BusinessRules/business_rules.md` (v1.0, 25/06/2026) — the **only** canonical BR source.
> The Business Rules appendix currently embedded in `Report 3.md` (~line 6223) is **outdated and inaccurate** and must be **replaced** by this table, not merged with it.
> Paste the table below directly into Report 3's Business Rules appendix section.

---

### 1. User Management & Authentication

| BR Code | Rule | Description |
|---|---|---|
| BR-001 | Password encryption | All user passwords **must** be hashed with BCrypt before being stored. Plain-text passwords are never stored. |
| BR-002 | Account lockout on failed login | After **5** consecutive failed login attempts, the system **automatically locks** the account for **15 minutes**. The lockout event must be recorded in the audit log. |
| BR-003 | Password reset token expiry | A "Forgot Password" reset token is valid for **1 hour** from creation. After expiry, the user must request a new link. Maximum **1 reset request per 15 minutes** per email. |
| BR-004 | Session timeout | A login session automatically expires after **30 minutes** of inactivity. On timeout, the session token is invalidated and the user is redirected to the login screen. |
| BR-005 | Single-device session | Each account may be **logged in on only one device** at a time. Logging in from a new device invalidates the current session. |
| BR-006 | Password strength policy | Passwords must satisfy a minimum security policy (length, special characters, etc.). A new password **must not match any of the last 3 passwords** (BR-015). |
| BR-009 | Accept terms on first login | On the **first login**, the user must accept the **Privacy Policy** and **Terms of Use** before accessing any function. |
| BR-010 | Email verification before login | An account may log in only after its **email address has been verified**. Unverified accounts are blocked and prompted to verify. |
| BR-011 | Account activation token expiry | The account activation link is valid for **48 hours** from creation. If expired, the system allows resending a new link with another 48-hour validity. |
| BR-012 | User information format | Phone numbers must follow a valid format; the system warns if the format is invalid. Email must be a valid, system-unique address. |

### 2. Role Management & Access Control (RBAC)

| BR Code | Rule | Description |
|---|---|---|
| BR-007 | Role-Based Access Control (RBAC) | The system applies an RBAC model. Each user is assigned one or more roles; access rights are determined entirely by role. System roles: **System Admin, School Administration, Medical Staff, Teacher, Parent, Raw Food Supplier, Service Caterers**. |
| BR-008 | Default system roles are protected | Default system roles **cannot be deleted**. Only **custom** roles may be deleted, by System Admin, and only when **no user** (including deactivated users) is currently assigned that role. |
| BR-013 | Staff account creation authority | Only **School Administration** can create staff accounts for its own school. **System Admin** can create user accounts system-wide. |

### 3. School & Class Management

| BR Code | Rule | Description |
|---|---|---|
| BR-020 | One homeroom teacher per class | Each classroom may have only **one homeroom teacher** at a time. Assigning a new teacher automatically unlinks the previous one and logs the change. |
| BR-021 | Cascading school deactivation | Deactivating a school automatically **suspends** all of its classrooms, staff accounts, and student accounts. This action must be logged in the audit log. |
| BR-022 | Role-based visibility of school policies | Users can view policies only for **their own school** (per `schoolId` in JWT). **Teacher, Medical Staff, Service Caterers** may view their school's policies for daily operations. Cross-school access is always denied. |
| BR-024 | Valid grade level | Class grade level must be between **1 and 12** (Vietnamese general education system). |
| BR-025 | Maximum classroom capacity | Each classroom has a maximum capacity of **50 students**. |
| BR-026 | Teacher assignment audit logging | Every homeroom teacher assignment or replacement must be recorded in the **audit trail** with timestamp and actor. |
| BR-028 | School GPS coordinate verification & lock | School GPS coordinates must be valid: **latitude** in **-90.0 to 90.0**, **longitude** in **-180.0 to 180.0**; out-of-range values are rejected. Once coordinates are confirmed (school becomes **ACTIVE**), `coordinateLocked` is set to `true`; while locked, coordinate updates are rejected (other fields — address, phone, description — remain editable; all location verification relies on GPS, not the text address). **Unlock exception**: only **System Admin** may reset `coordinateLocked = false`, and must supply a written reason, logged in the audit log (actor, time, reason); after new coordinates are saved, `coordinateLocked` automatically returns to `true`. GPS coordinates are used by other modules (MF06 Ingredient Receiving, MF07 Three-Step Inspection, MF08 Food Sample Retention) as the central reference point for location verification. **Default geofence radius**: 200 meters from the school's center coordinate, configurable by School Administration between **50 m and 500 m**; any radius change must be logged in the audit log. |

### 4. Student & Parent Management

| BR Code | Rule | Description |
|---|---|---|
| BR-030 | Student–parent linkage | Each student must be linked to at least one parent account. Allergy information and special dietary requirements must be declared and stored in the student profile. |
| BR-031 | Health information review | Health and allergy information declared by parents must be **reviewed and confirmed by Medical Staff** before taking effect. |
| BR-032 | Bulk import of students & parents | The system supports bulk import of student and parent profiles via a predefined template with data validation rules. |

### 5. Supplier & Caterer Management

| BR Code | Rule | Description |
|---|---|---|
| BR-040 | Supplier registration process | A supplier must **submit a complete application**: company profile, business license, food safety certification, and product information. The application must be **reviewed and approved by School Administration** before the supplier may operate. |
| BR-041 | Supplier status lifecycle | Supplier status can be **Pending → Approved / Rejected**. Only suppliers in **Approved** status may deliver ingredients to a school. |
| BR-046 | Required compliance documents | Suppliers must maintain valid compliance documents: food safety certification, business license, product origin documents. Expired documents trigger a warning and may affect partnership status. |
| BR-047 | Document expiry warning | The system displays an **"Expiring Soon"** warning when a supplier document has **fewer than 30 days** remaining before expiry. |
| BR-048 | Supplier rejection | When rejecting a supplier, the **rejection reason** must be recorded in the system and a notification sent to the supplier. |
| BR-050 | Delivery staff registration | Each supplier delivery staff member must be registered with: full name, national ID number, phone number, license plate. The national ID number must be unique in the system. |
| BR-051 | Face biometric authentication | Delivery staff must register facial biometric data to be authenticated at the school gate. A supplier may delete a delivery staff member's biometric data, revoking gate access. **Note**: biometric authentication is **not applied to students** (children), for personal data protection (PDPD) reasons. |
| BR-060 | GPS location verification on delivery | The system verifies the delivery staff's GPS location to ensure delivery occurs within the permitted area. |

### 6. Ingredient & Inventory Management

| BR Code | Rule | Description |
|---|---|---|
| BR-070 | Supplier status check on receiving | At the start of a receiving session, the system **automatically verifies** that the supplier is in **Approved** status; if not, the receiving session is rejected. |
| BR-071 | Mandatory batch certification | Every ingredient batch received must include: batch number, production date, expiry date, quantity, and relevant certificates. |
| BR-072 | Mandatory evidence photos on receiving | Receiving staff **must photograph** the ingredients, labels, packaging, and supporting documents as evidence. |
| BR-073 | Automatic GPS & timestamp logging | The system **automatically records** GPS location, device information, and timestamp during the receiving process, as an anti-fraud measure. |
| BR-074 | Receiving location verification | The system verifies that the receiving activity is performed **within the school's permitted receiving area**. |
| BR-075 | FEFO principle (First Expired, First Out) | The system prioritizes ingredient usage by **FEFO** — the batch expiring soonest is issued from stock first. |
| BR-076 | Ingredient batch traceability | The system records the ingredient batch number linked to each menu and production run to ensure **full traceability**. |
| BR-077 | Low-stock alert | The system automatically raises a **low-stock alert** when inventory is insufficient to meet the planned meal requirements. |
| BR-078 | Rejecting a non-conforming ingredient batch | A batch that fails quality inspection may be **rejected**; the rejection reason must be recorded with supporting evidence photos. |

### 7. Menu & Diet Management

| BR Code | Rule | Description |
|---|---|---|
| BR-061 | Menu submission deadline | A menu must be submitted for approval **at least 3 days** before its intended week of use. |
| BR-062 | Age-group nutrition targets | A menu must meet **age-group nutrition targets** (calories, nutrient diversity, meal composition balance). |
| BR-063 | Allergy conflict check | The system **must check for allergy conflicts** between menu ingredients and student allergy profiles before menu approval. If a serious conflict is detected, submission is **blocked**. |
| BR-064 | Inventory-aware menu optimization | AI menu suggestions must prioritize ingredients **already available in stock** and comply with the configured **cost cap**. |
| BR-065 | Mandatory special meal plans | For students with verified allergies or special diets, the system **requires** Service Caterers to create a **separate meal plan**. |
| BR-066 | Special diet categories | The system supports special diet types: **Halal, Vegetarian, Gluten-Free, Nut-Free**, and others. |
| BR-067 | Locking an approved menu | A menu in **Approved** or **Published** status cannot be edited or deleted. Only menus in **Draft** or **Rejected** status may be edited. |
| BR-068 | Menu approval workflow | A menu must follow the workflow: **Draft → Pending Approval → Approved / Rejected → Published**. Only **School Administration** may approve or reject a menu. |
| BR-069 | Complete ingredient declaration | Service Caterers must declare **all ingredients and potential allergens** for every dish on the menu before submitting for approval. |
| BR-080 | Menu cost cap | A menu must comply with the **cost cap** configured by School Administration. |
| BR-081 | Substitute dish for common allergies | A menu must provide a **substitute dish** for the **5 most common food allergies**, where applicable. |
| BR-082 | Separate preparation for special meals | Special meals must be prepared **entirely separately**, using dedicated cooking equipment to avoid cross-contamination. |

### 8. Three-Step Food Inspection

| BR Code | Rule | Description |
|---|---|---|
| BR-079 | Mandatory three-step inspection | Every food batch must go through **exactly 3 inspection steps**: **Step 1** — pre-cooking ingredient check (quality, freshness, packaging, batch info); **Step 2** — post-cooking food check (appearance, smell, texture, doneness); **Step 3** — pre-serving meal check (quality, portioning, serving conditions, hygiene). |
| BR-083 | Evidence recording at every step | Every inspection step must be accompanied by **evidence photos, GPS location, and timestamp**, recorded automatically. |
| BR-090 | Minimum post-cooking food temperature | Food after cooking must reach a **minimum core temperature of 75°C** (checked at Step 2). |
| BR-091 | Minimum holding temperature | Cooked food awaiting service must be held at a **minimum of 60°C** (checked at Step 3). |
| BR-092 | Ingredient expiry check | The system automatically **checks the expiry date** of ingredient batches used during the inspection step. |
| BR-093 | Complete inspection record | Every three-step inspection session must produce a **complete inspection record** linked to the ingredient batch and meal production record. |
| BR-094 | Block serving on failed inspection | A meal **may not be served** if any of the three inspection steps fails. |
| BR-095 | Non-conformance recording | When an inspection step fails, the system must record **non-conformance details** and automatically **create a Corrective Action Request**. |
| BR-096 | Re-inspection after corrective action | After a corrective action is completed, the failed inspection step must be **re-performed (re-inspection)**. |
| BR-097 | Integration with incident management | A seriously failed inspection result must be automatically linked to the **incident management** system. |
| BR-098 | Meal approval for serving | The system approves a meal for serving only after **all 3 steps** have **Passed**. |
| BR-099 | Inspection record access rights | **Medical Staff** and **School Administration** may view all inspection records. **Service Caterers** may view only records from inspection sessions they themselves performed. |

### 9. Food Sample Retention

| BR Code | Rule | Description |
|---|---|---|
| BR-105 | Mandatory retention after passed inspection | After a food batch **passes the 3-step inspection**, Service Caterers **must** retain a food sample. |
| BR-106 | Mandatory sample labeling | Every retained sample must be labeled with: **dish name, production date, retention date, and batch reference number**. |
| BR-107 | Mandatory sample photos | Staff **must photograph** the retained samples and their labels as operational evidence. |
| BR-108 | Automatic GPS & timestamp on retention | The system **automatically records** GPS, device information, and timestamp during the retention process. |
| BR-109 | Retention location verification | The system verifies that sample retention is performed in the **designated kitchen or storage area**. |
| BR-110 | Sample storage temperature | The retention refrigerator must be kept between **2°C and 8°C**. The system **raises an alert** if the temperature deviates from this range for more than **30 minutes**. |
| BR-111 | Minimum retention period | The system **automatically tracks** the required retention period per food safety regulations. Samples must be kept for at least the minimum required duration. |
| BR-112 | Sample disposal after retention period | After the mandatory retention period elapses, staff dispose of the sample per procedure; the system records the disposal details, **the responsible staff member**, and the timestamp. |
| BR-113 | Sample recall on incident | When a food safety incident occurs, **Medical Staff** may recall a retained sample for investigation; the system records the recall, its investigative purpose, and the actor. |
| BR-114 | Linking samples to menu and inspection | A sample retention record must be **linked** to the related menu, inspection record, and meal production batch. |

### 10. Meal Serving & Distribution

| BR Code | Rule | Description |
|---|---|---|
| BR-120 | Class-based meal distribution confirmation | The **Teacher** confirms meal distribution to students in their own class. |
| BR-121 | Student meal history tracking | The system records and tracks each student's **meal consumption history**. |
| BR-123 | Allergy alert on student ID scan | When a student with a **registered food allergy** is scanned during a serving session, the system **automatically triggers a visual and audible alert** on the serving screen to prevent incorrect meal distribution. |
| BR-125 | Viewing special meal requests during serving | Teachers and serving staff must be able to view each student's **special meal requirements** before and during the serving session. |
| BR-130 | Prevent duplicate distribution | The system **blocks duplicate meal distribution** — a student cannot be scanned twice within the same serving session. |
| BR-133 | Daily meal summary for parents | The system **automatically compiles** the served menu and nutrition breakdown, sending a **daily summary notification** to parents. |
| BR-135 | Meal serving delay notification | The system **automatically notifies** the homeroom teacher and parents when meal serving is delayed by more than **20 minutes** from the planned time. |

### 11. Food Incident Management

| BR Code | Rule | Description |
|---|---|---|
| BR-146 | Automatic incident investigation trigger | The system **automatically initiates** a food safety incident investigation when **3 or more students** report similar symptoms within **24 hours**. |
| BR-147 | Incident severity classification | **Medical Staff** is responsible for classifying the **severity** of an incident. |
| BR-148 | Immediate alert for high-severity incidents | An incident classified as **High Severity** must **immediately** trigger alerts to: school management, the catering company, and local health authorities. |
| BR-149 | Automatic batch quarantine on incident | When a food safety incident is reported, the system **automatically sets Quarantine status** on all suspect food batches and related ingredients in stock. |
| BR-150 | Root cause investigation and analysis | The system must support **Root Cause Analysis** with the ability to attach evidence. |
| BR-151 | Traceability map generation | Within **4 hours** of an incident report, the system **automatically compiles a traceability map** for all related ingredient batches and suppliers. |
| BR-152 | CAPA management (Corrective and Preventive Action) | After every incident, a **CAPA plan** must be created, assigned, and tracked to completion. |
| BR-153 | Corrective action approval | **School Administration** may **approve or reject** proposed corrective actions. |
| BR-154 | Formal incident report | **Medical Staff** may generate a **formal incident report** for record-keeping and export. |
| BR-155 | Closing an incident | Only **School Administration** may **close** an incident, after confirming completion of corrective actions. |

### 12. Food Safety Monitoring

| BR Code | Rule | Description |
|---|---|---|
| BR-089 | Refrigerator temperature monitoring | The system continuously monitors refrigerator temperature and raises an alert when the temperature **deviates from the allowed range** for more than 30 minutes. |
| BR-100 | Daily hygiene score | The system must record and track the kitchen's **daily hygiene score**. |
| BR-101 | Monthly safety & hygiene inspection | The system must store the results of **periodic monthly safety and hygiene inspections**. |
| BR-102 | Pest control log | The system must store a **pest control log** for the kitchen. |
| BR-103 | Kitchen staff health certificate validity | The system must track the validity of **kitchen staff health certificates** per regulation. |
| BR-104 | Chef hygiene score | Kitchen staff hygiene scores are aggregated and used in the **Food Safety Compliance Report**. |

### 13. Feedback & Notification

| BR Code | Rule | Description |
|---|---|---|
| BR-138 | Critical alerts bypass approval workflow | Critical food safety alerts (e.g., a food recall) are **broadcast immediately** to all relevant users without going through an administrator approval workflow. |
| BR-139 | Feedback submission time window | Parents and teachers may submit meal feedback only within **24 hours** of the meal being served. |
| BR-140 | Mandatory quality-concern tag on low rating | When a parent rates below **3 stars**, the system **requires** selection of a **quality-concern tag** from a predefined list. |
| BR-141 | Weekly feedback aggregation | The system **automatically compiles** an aggregated feedback chart **weekly** and sends a report to catering management and school leadership. |
| BR-142 | Illness-keyword detection in feedback | The system **automatically analyzes** parent feedback content, detecting **illness-related keywords** (e.g., "stomachache", "vomiting"). When detected, the system **triggers an urgent alert** and immediately notifies **Medical Staff** for review. |
| BR-143 | Caterer responses to feedback | **Service Caterers** may respond to parent feedback. |
| BR-144 | Announcement revision history | Every announcement update must create a **revision history entry** with timestamp and actor ID. |
| BR-145 | Anonymized aggregate feedback data | Feedback data is **anonymized and aggregated** into the school's dashboard. |
| BR-158 | Default quiet hours | By default, the system **does not send non-urgent push notifications** during **22:00–06:00**. Urgent alerts are **not affected** by quiet hours. |
| BR-159 | Retry and fallback on notification failure | When a push notification fails to send, the system **automatically retries**; after exhausting retries, it falls back to sending a **fallback email**. |

### 14. AI & Support Services

| BR Code | Rule | Description |
|---|---|---|
| BR-156 | AI-based menu suggestion | The **Gemini AI Service** analyzes nutrition requirements, student allergy profiles, available inventory, and cost caps to **suggest an optimized menu**. AI output includes **nutrition analysis, ingredient list, and allergy warning flags**. |
| BR-157 | Mandatory AI conversation archiving | All conversations between users and the AI (including AI menu suggestions) must be **archived** for compliance and audit purposes. |
| BR-158b | Quiet hours configuration | Users may **configure quiet hours** according to personal preference. |
| BR-160 | Automatic database backup | The system performs a **full database backup** every **Sunday at 02:00 AM**. The backup file is **AES-256 encrypted** and replicated to **off-site cloud storage**. |
| BR-161 | Backup file protection | The backup file must be encrypted and stored separately from the primary infrastructure. |
| BR-162 | Immutable audit log | The audit trail must be **immutable** — no one may edit or delete audit records. |
| BR-163 | External integration connectivity check | When updating configuration for an external integration (GPS, Gmail, Firebase, Gemini AI), the system must **verify a successful connection** before saving the new configuration. |
| BR-164 | Encryption of integration credentials | All external service credentials (API keys, OAuth credentials) must be **encrypted** at rest. |
| BR-165 | Weekly system health report | The system **automatically compiles** a weekly system health and active-user report for System Admin. |

### 15. Audit Log & Data Security

| BR Code | Rule | Description |
|---|---|---|
| BR-170 | Logging all significant actions | All significant actions in the system (create, update, delete, approve, reject) must be recorded in the **audit trail** with actor, timestamp, and affected object. |
| BR-171 | Log retention policy | The system must have a **Log Retention Policy** defining the minimum log retention period. |
| BR-172 | Log integrity protection | The system must have a mechanism to **protect the integrity** of logs, preventing any unauthorized modification. |
| BR-173 | Personal data protection (PDPD) | The system must comply with Vietnam's **Personal Data Protection Decree (PDPD)**. Children's (students') biometric data **must not be collected**, for personal data protection reasons. |
| BR-174 | Operational history traceability | The system must support **querying operational history** and tracking the audit trail over time. |

### 16. Reporting & Analytics

| BR Code | Rule | Description |
|---|---|---|
| BR-175 | Role-based report access | Access to each report type is controlled by role (RBAC): **School Administration** — all operational reports of its school; **Medical Staff** — inspection, medical incident, and safety compliance reports; **Service Caterers** — only reports related to their own activity (receiving, inspection, incidents); **System Admin** — all reports system-wide. |
| BR-176 | School-scoped report data | Report data must be **scoped to the user's own school**. Only **System Admin** may view cross-school aggregate reports. |
| BR-177 | Report history retention | Generated reports must be retained for at least **12 months**. Every export (who, when, report type) must be logged in the **audit trail**. |
| BR-178 | Report export formats | The system supports export in two formats: **PDF** and **Excel (XLSX)**. Each export file must include: report title, data period, school name, and export timestamp. |
| BR-179 | Scheduled automated report export | The system supports **scheduling periodic report generation and delivery** (daily, weekly, monthly), sent to the email list configured by School Administration. On send failure, the system retries up to **3 times**; if still unsuccessful, it logs the error and notifies System Admin. |
| BR-180 | Real-time dashboard data | The Analytics Dashboard must display data refreshed within **5 minutes** of an operational change. Dashboard content includes: food safety risk indicators, inspection compliance rate, daily meal summary. |
| BR-181 | Report data security & anonymization | Reports must **not disclose personally identifiable information** of students in aggregated data. Reports exported outside the system must be marked **"Confidential"** if they contain sensitive operational or incident information. |
| BR-182 | Report data integrity & accuracy | Report data must be computed directly from the **original data source** in the system; manual editing after report generation is not permitted. Reports must clearly display the **generation date/time** and the **data period** covered. |

### 17. Offline & Data Sync

> **Decision (2026-07-01)**, resolving the conflict between Report 1's "offline-capable" positioning and Report 3's NFR "requires stable Internet connection": the system supports **limited offline capability** — evidence capture is allowed offline, but real-time safety-verification actions always require connectivity. Applies to the Mobile app (Flutter); Web always requires a connection as before.

| BR Code | Rule | Description |
|---|---|---|
| BR-196 | Offline evidence capture allowed | Evidence-only actions (no server-side verification needed to decide pass/fail) may be performed offline: photo + GPS + timestamp capture during three-step inspection (BR-083), ingredient receiving (BR-072), food sample retention (BR-107), and filling inspection checklists. Data is stored in a **local sync queue** on the device and automatically uploaded once connectivity is restored. |
| BR-197 | Real-time safety verification requires connectivity | The following actions **may not** be performed offline, even with stale cached data on the device: allergy alert on student ID scan (BR-123); supplier Approved-status check before receiving (BR-070); batch Quarantine-status check (BR-149); face biometric authentication (BR-051). When offline, the system **blocks the action** and displays a connectivity-required message — cached data must never be used to silently allow the action through. |
| BR-198 | Automatic incident aggregation requires server connectivity | Automatically triggering an incident investigation when 3+ students report similar symptoms within 24 hours (BR-146) requires real-time server-side aggregation — it cannot be performed or simulated offline on-device. |
| BR-199 | Sync conflict resolution | When two devices edit the same record while offline, the system applies **last-write-wins by server timestamp** (not device clock). Both writes must be recorded in the **immutable audit log** (BR-162) so no write is silently lost, including the overwritten one. |
| BR-200 | Offline cache validity window | Cached data usable offline (menus, class lists, student lists, etc.) is valid for at most **[TODO — team to define number of hours]** since the last successful sync. Once expired, the relevant screens show a **"Needs re-sync"** state and restrict new write actions until sync succeeds. |

---

## Appendix — Business Rules by Module (summary index)

| Module | Business Rules |
|---|---|
| User Management & Authentication | BR-001–006, BR-009–012 |
| RBAC | BR-007, BR-008, BR-013 |
| School & Class Management | BR-020, BR-021, BR-022, BR-024–026, BR-028 |
| Student & Parent Management | BR-030–032 |
| Supplier & Caterer Management | BR-040, BR-041, BR-046–048, BR-050, BR-051, BR-060 |
| Ingredient & Inventory Management | BR-070–078 |
| Menu & Diet Management | BR-061–069, BR-080–082 |
| Three-Step Food Inspection | BR-079, BR-083, BR-090–099 |
| Food Sample Retention | BR-105–114 |
| Meal Serving & Distribution | BR-120, BR-121, BR-123, BR-125, BR-130, BR-133, BR-135 |
| Food Incident Management | BR-146–155 |
| Food Safety Monitoring | BR-089, BR-100–104 |
| Feedback & Notification | BR-138–145, BR-158, BR-159 |
| AI & Support Services | BR-156, BR-157, BR-158b, BR-160–165 |
| Audit Log & Data Security | BR-170–174 |
| Reporting & Analytics | BR-175–182 |
| Offline & Data Sync | BR-196–200 |

**Total: 134 Business Rules** (BR-001 through BR-182 plus BR-158b, and BR-196–200, non-contiguous numbering — gaps such as BR-014, BR-016–019, BR-023, BR-027, BR-029, BR-033–039, BR-042–045, BR-049, BR-052–059, BR-084–088, BR-115–119, BR-122, BR-124, BR-126–129, BR-131, BR-132, BR-134, BR-136, BR-137, BR-166–169 do not exist in the canonical source — **do not cite these codes anywhere in Report 3**. BR-183–195 are reserved but **not yet merged** — see `Violation_Management_Proposal.md` and `AcademicYear_Management_Proposal.md`, both still PROPOSED pending team decisions.).

---

> **Note on BR-183 onward**: If the Violation Management proposal (`02_Requirements/Violation_Management_Proposal.md`, BR-183–BR-190) is approved by the team, its rules should be appended here as a 17th module ("Violation Management") once finalized — they are currently PROPOSED, not yet canonical.
