### Menu & Diet Management

#### Approval / Reject Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-006 - Approval / Reject Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration opens a pending menu submission from the approval dashboard. | | |
| Description | Enables the authorized School Admin to review and approve or reject a menu submitted by the Service Caterers, ensuring nutritional and allergy compliance before publication. | | |
| Preconditions | School Admin is authenticated and authorized. A menu is in 'Pending Approval' status. System has completed the automatic allergy conflict check. | | |
| Postconditions | Approved: Menu locked and published (BR-067, BR-070). Parents and teachers notified (BR-075). Approval logged with electronic signature (BR-066).  Rejected: Menu returned to 'Rejected' status. Rejection reason logged (BR-074) and communicated to Service Caterers. | | |
| Normal Flow | 1. School Admin navigates to Menu Approval Dashboard.  2. System lists menus awaiting review with submission date and allergy check status.  3. School Admin opens a menu and reviews dish options, nutritional content, and allergy accommodation (BR-062, BR-063, BR-069).  4. School Admin selects 'Approve' or 'Reject', adding rejection comments if rejecting (BR-074).  5. System logs the decision with electronic signature metadata (BR-066), updates status, displays MSG-040 (if approved), and notifies Service Caterers and relevant parties (BR-075 if approved). | | |
| Alternative Flows | AF1 - Reject Menu: School Admin selects Reject and provides rejection comments. System records rejection reason (BR-074), updates status to 'Rejected', notifies Service Caterers, and prevents publication.  AF2 - Allergy Conflict During Review: System detects allergy conflict (MSG-051). System blocks approval and School Admin must request menu revision (returns menu to 'Rejected'). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-063, BR-066, BR-067, BR-070, BR-074, BR-075 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-62 Approval / Reject Menu Use Cases Specification

#### Create Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-020 - Create Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers navigates to Menu Management and selects 'Create Menu'. | | |
| Description | Enables the Service Caterers to create a new weekly menu draft, selecting dishes and verifying nutritional content, allergy compliance, and dish rotation rules. | | |
| Preconditions | Service Caterers is authenticated and authorized. Current inventory and student allergy profiles are available. | | |
| Postconditions | Menu draft saved in 'Draft' status with allergy check results attached. Ready for approval submission (UC-089). All dishes declare complete ingredient and allergen lists (BR-069). | | |
| Normal Flow | 1. Service Caterers navigates to Menu Management and selects 'Create Menu'.  2. System displays menu creation form with serving date, meal slots, and dish selection.  3. Service Caterers selects dishes, enters serving date, and confirms nutritional targets (BR-062) and rotation rules (BR-068).  4. System automatically runs allergy conflict check against active student profiles (BR-063) and displays MSG-051 if conflicts found.  5. System saves the draft menu with creation details, logs the audit trail, and displays MSG-071. | | |
| Alternative Flows | AF1 - AI Recommendation: Service Caterers selects 'AI Assist'. Gemini AI generates dish recommendations based on inventory, nutritional targets, and cost ceiling (BR-064, BR-072, BR-156). System archives the AI query and recommendations (BR-157). Service Caterers reviews and applies suggestions.  AF2 - Allergy Conflict Found: System displays MSG-051. Service Caterers must resolve conflicts before submitting for approval. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-072, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-63 Create Menu Use Cases Specification

#### Create Special Meal Plan

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-023 - Create Special Meal Plan | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects 'Create Special Meal Plan' or opens a student dietary request. | | |
| Description | Enables the Service Caterers to define, validate, and save custom special meal plans for students with verified dietary restrictions or severe allergies. | | |
| Preconditions | Service Caterers is authenticated and authorized. Target student has a verified allergy or special meal request (UC-036 or UC-080). | | |
| Postconditions | Special meal plan is saved in the database, linked to the student profile, and made available for printing or exporting by the Service Caterers. | | |
| Normal Flow | 1. Service Caterers navigates to Special Meal Plan Management.  2. System lists students with active verified dietary restrictions or severe allergies (BR-065).  3. Service Caterers selects a student and clicks 'Create Special Meal Plan'.  4. System displays special meal plan form, pre-populating the student's verified allergens and dietary category (Halal, Vegetarian, Gluten-Free, Nut-Free, etc. per BR-066).  5. Service Caterers designs the meal plan, specifying dedicated recipes and cooking instructions that ensure separate preparation and utensils (BR-071).  6. Service Caterers enters the raw ingredients and potential trace allergens for all dishes (BR-069) and validates that alternative dishes are configured for the top 5 common allergens if applicable (BR-073).  7. System runs validation to confirm zero allergen overlap with the student's allergy profile (BR-063).  8. System saves the special meal plan, logs the action in the audit trail, and displays MSG-072. | | |
| Alternative Flows | AF1 - AI Recipe Assistance: Service Caterers requests AI assistance to generate an allergy-safe recipe. Gemini AI Service reviews the student's allergens, recommends safe ingredients and separate preparation steps (BR-156). System archives the AI query and recommendations (BR-157). Service Caterers reviews and applies suggestions. | | |
| Exceptions | MSG-004, MSG-005. If allergen validation fails (overlap detected), system blocks saving and displays MSG-051. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-066, BR-063, BR-065, BR-069, BR-071, BR-073, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-64 Create Special Meal Plan Use Cases Specification

#### Manage Menus

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-048 - Manage Menus | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Primary actor requests to manage menus. | | |
| Description | Enables the Service Caterers to browse, search, and manage draft, submitted, and published menus, including submitting them for approval or deleting drafts. | | |
| Preconditions | Service Caterers is authenticated and authorized. | | |
| Postconditions | Menu list is displayed. Draft and rejected menus can be edited or deleted. Approved/Published menus are locked (BR-067). | | |
| Normal Flow | 1. Service Caterers navigates to Menu Management.  2. System queries the database and displays the list of weekly menus showing: Scheduled Week, Status (Draft, Pending Approval, Approved, Published, Rejected), and Last Updated Date.  3. Service Caterers can search menus by date range or filter by status.  4. Service Caterers selects a menu to perform management actions:  - If status is 'Draft' or 'Rejected': Service Caterers can select 'Edit' (redirecting to UC-098) or 'Delete'.  - If status is 'Draft': Service Caterers can select 'Submit' (redirecting to UC-089).  5. System processes the selected action, updates the display, and logs the operation in the audit trail. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If trying to delete or edit an approved/published menu, system blocks the action and displays a validation warning (BR-067). | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-067 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-65 Manage Menus Use Cases Specification

#### Request AI Menu Recommendations

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-075 - Request AI Menu Recommendations | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects 'Request AI Menu Recommendations' during menu planning. | | |
| Description | Enables the Service Caterers to obtain AI-generated menu suggestions from Gemini AI Service based on current inventory, nutritional targets, student health profiles, and budget constraints. | | |
| Preconditions | Service Caterers is authenticated and authorized. Gemini AI Service is operational. Current inventory and student allergy profiles are available. | | |
| Postconditions | AI recommendations displayed with nutritional breakdowns and allergen flags. AI query and recommendations archived for compliance (BR-157). | | |
| Normal Flow | 1. Service Caterers selects 'Request AI Recommendations' in Menu Management. 2. System sends inventory levels, nutritional targets (BR-062), student allergy profiles, and seasonal cost data to Gemini AI Service. 3. Gemini AI analyzes data and returns dish recommendations prioritizing in-stock ingredients and cost ceiling (BR-064, BR-072). 4. System displays recommendations with nutritional breakdown, ingredient lists, and allergen indicators (BR-156). 5. Service Caterers applies selected recommendations to the menu. AI exchange archived (BR-157). | | |
| Alternative Flows | AF1 - AI Service Unavailable: Gemini AI Service unreachable. System displays service unavailability message (MSG-073) and allows manual menu creation.  AF2 - Expiring Documents Alert: One or more documents expire within 180 days. System highlights those documents with an 'Expiring Soon' badge; if fewer than 30 days remain and the document is still unrenewed, the badge escalates to 'Urgent' (BR-047). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-064, BR-072, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-66 Request AI Menu Recommendations Use Cases Specification

#### Submit Menu For Approval

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-087 - Submit Menu For Approval | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects a completed menu draft and clicks 'Submit for Approval'. | | |
| Description | Enables the Service Caterers to formally submit a validated menu draft for School Admin review and approval. | | |
| Preconditions | Service Caterers is authenticated and authorized. Menu is in 'Draft' status. All required fields are complete. Submitted at least 3 days before the scheduled week (BR-061). | | |
| Postconditions | Menu status changed to 'Pending Approval'. School Admin notified. Allergy conflicts flagged (MSG-051) if present. | | |
| Normal Flow | 1. Service Caterers navigates to Menu Management and selects a draft menu.  2. System displays menu summary and pre-submission validation checklist.  3. Service Caterers reviews meal options, nutritional data, and declares all ingredients (BR-069).  4. System runs final allergy conflict check (BR-063), displaying MSG-051 warnings if found.  5. System changes menu status to 'Pending Approval', notifies School Admin, displays MSG-039. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005.  If critical allergy conflict is detected, system blocks submission, displays MSG-051 warning message, and prevents submission until conflict is resolved. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-061, BR-063, BR-069 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-67 Submit Menu For Approval Use Cases Specification

#### Update Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-096 - Update Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects a draft or rejected menu and clicks 'Edit Menu'. | | |
| Description | Enables the Service Caterers to modify details, dish selections, and serving dates of an existing menu draft, running automatic allergy and compliance checks. | | |
| Preconditions | Service Caterers is authenticated and authorized. The menu exists and is in 'Draft' or 'Rejected' status (BR-067). | | |
| Postconditions | The updated menu is saved as a draft. Allergy check results and nutrition evaluations are updated. | | |
| Normal Flow | 1. Service Caterers selects a draft or rejected menu and clicks 'Edit Menu'.  2. System displays the menu editing form pre-filled with current dish selections, nutritional breakdown, and active rejection comments (BR-074 if status is Rejected).  3. Service Caterers modifies serving dates, dish choices, or nutritional settings (BR-061, BR-062).  4. System runs automatic dish rotation check (BR-068: main dish max twice a week) and ingredient declaration validation (BR-069).  5. System runs allergy conflict check against student allergy profiles (BR-063).  6. System saves changes to database, updates audit logs, and displays MSG-074. | | |
| Alternative Flows | AF1 - AI Menu Recommendation: Service Caterers selects 'AI Assist' to replace a conflicting dish. Gemini AI Service analyzes current inventory, allergen profiles, and cost ceiling (BR-064, BR-072, BR-156) and recommends safe alternatives. System archives the AI query and recommendations (BR-157). | | |
| Exceptions | MSG-004, MSG-005. If trying to update an approved/locked menu (BR-067), action is blocked. If dish rotation limit is exceeded (BR-068), system blocks action and displays MSG-075. If allergy conflict is found, system displays MSG-051. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-061, BR-062, BR-063, BR-064, BR-067, BR-068, BR-069, BR-072, BR-074, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-68 Update Menu Use Cases Specification

#### View List Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-128 - View List Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Admin selects 'Menu List' from the dashboard. | | |
| Description | Enables the School Admin to query, view, filter, and export the list of weekly menus and their current statuses. | | |
| Preconditions | The School Admin is authenticated and authorized to access this feature. | | |
| Postconditions | List of menus is displayed. Exporting actions are enabled. | | |
| Normal Flow | 1. School Admin navigates to the Menu List section.  2. System queries the database for all weekly menus.  3. System displays the list showing: Scheduled Week range, status (Draft, Pending Approval, Approved, Published, Rejected), Service Caterers name, and nutritional summary.  4. School Admin can filter list by status, search by week range, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-69 View List Menu Use Cases Specification

#### View Menu Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-134 - View Menu Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Admin selects a menu from the Menu List (UC-130) or the Menu Approval Dashboard (UC-006). | | |
| Description | Enables the School Admin to view detailed dish breakdowns, full raw ingredient declarations, potential trace allergens, nutritional targets, and approval histories for a specific menu. | | |
| Preconditions | School Admin is authenticated and authorized to access this feature. The target menu exists. | | |
| Postconditions | Detailed menu data, including raw ingredients, nutritional summaries, and approval logs, is displayed. | | |
| Normal Flow | 1. School Admin selects a menu and clicks 'View Details'.  2. System queries the database and retrieves the menu details: serving date, meal slots, dish lists, and nutritional breakdown (BR-062).  3. System displays the menu, showing each dish with its full ingredient list and trace allergens (BR-069).  4. System displays the history of approvals/rejections with timestamps, electronic signature metadata (BR-066), and comments.  5. School Admin reviews the information. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-062, BR-066, BR-069 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-70 View Menu Detail Use Cases Specification

#### View Menu Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-135 - View Menu Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent navigates to 'School Menu' from the portal home screen. | | |
| Description | Enables Parents to view approved, published menus, including nutritional breakdowns, ingredient lists, and allergen highlights matched to their child's allergy profile. | | |
| Preconditions | Parent is authenticated and authorized. The menu is in 'Published' status (BR-070). At least one student profile is linked to the Parent (BR-031). | | |
| Postconditions | Published menu details, child-specific allergy warnings, and nutrition breakdowns are successfully retrieved and displayed on the user interface. | | |
| Normal Flow | 1. Parent navigates to the School Menu section.  2. System checks parent credentials and retrieves linked student profile(s) (BR-031).  3. System displays the published menus for the current and upcoming week (BR-070) conforming to the student's grade/age-specific nutritional targets (BR-062).  4. Parent browses dishes, viewing nutritional content, raw ingredients, and allergen declarations (BR-069).  5. System cross-references the menu dishes with the active child's verified allergy profile. If any dish contains active allergens, the system highlights the dish in red, highlights the specific allergen, and displays a warning alert (referencing MSG-051).  6. Parent can click 'Download PDF' to export a printable copy of the menu. | | |
| Alternative Flows | AF1 - Switch Child Profile (For Parents with multiple children):  1. Parent clicks the student dropdown selector.  2. System lists all linked children (max 5 per BR-201).  3. Parent selects a different child.  4. System refreshes the menu view, age-specific nutritional targets (BR-062), and child-specific allergy warnings.  AF2 - Access Special Meal Plan:  1. Parent selects a child who has an active special dietary plan (BR-065, BR-065).  2. Parent clicks 'View Special Meal' button.  3. System retrieves and displays the child's customized allergy-safe menu details (redirects to UC-144). | | |
| Exceptions | EX1 - No Menu Published: If no menu is published for the selected week, the system blocks access and displays MSG-090.  EX2 - Missing Student Link: If the Parent account has no linked student profiles (BR-031), the system displays warning and prompts the parent to enter a student admission code. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-031, BR-201, BR-062, BR-069, BR-070, BR-075 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-71 View Menu Information Use Cases Specification

