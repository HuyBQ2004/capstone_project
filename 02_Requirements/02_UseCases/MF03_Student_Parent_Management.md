### Student & Parent Management

#### Bulk Import Student & Parent

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-010 - Bulk Import Student & Parent | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration navigates to Student Management and selects 'Bulk Import Students & Parents'. | | |
| Description | Enables the School Administration to bulk import student and parent records from a structured Excel or CSV file with automated validation and duplicate detection. | | |
| Preconditions | The school administration is authenticated and authorized. Import file is prepared using the system's template format. | | |
| Postconditions | Valid records are created. Duplicate and invalid records are reported in an error summary. Verification emails sent to new parent accounts. Notification channels configured. | | |
| Normal Flow | 1. School Administration navigates to Bulk Import and downloads the import template. 2. School Administration uploads the completed import file. 3. System validates each record: email format, phone format (BR-012, BR-032), duplicate student admission codes, and parent linkage. 4. System displays validation summary with valid, invalid, and duplicate record counts. 5. School Administration confirms. System imports valid records, sends verification emails to new parent accounts (BR-010), configures notification channels, and displays MSG-035. | | |
| Alternative Flows | AF1 - Validation Errors: System lists all invalid rows with specific error codes. School Administration corrects the file and re-uploads. | | |
| Exceptions | MSG-004, MSG-005, MSG-012 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-032 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-30 Bulk Import Student & Parent Use Cases Specification

#### Create Student

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-025 - Create Student | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to create student. | | |
| Description | Enables the School Administration to input, validate, and save new Student details into the system database. | | |
| Preconditions | The school administration is authenticated and authorized. The school is active. | | |
| Postconditions | The new student profile is created, saved to the database, and audited. Classroom assignment is validated. | | |
| Normal Flow | 1. School Administration navigates to Student Management and selects 'Create Student Profile'. 2. System displays the student creation form. 3. School Administration enters student details: full name, date of birth, grade level, and classroom assignment. 4. School Administration enters parent details: email address. 5. System searches for the parent email. - If parent email exists: system displays parent details and links the new student profile (BR-031, BR-201: max 5 students). - If parent email does not exist: School Administration inputs parent's name and phone number (BR-012) to create a new parent account. 6. System automatically generates a unique school registration number and a unique student admission code. 7. System validates that the classroom is active and has not reached its capacity limit of 50 students (BR-025). 8. System saves the student and parent profiles, links them, sends verification email for new parent accounts (BR-010), logs the creation in the audit trail, and displays MSG-031. | | |
| Alternative Flows | AF1 - Parent Limit Reached: The selected parent account already has 5 active students. System blocks linking and prompts School Administration to verify (BR-201). | | |
| Exceptions | MSG-004, MSG-005, MSG-010 (if classroom capacity is exceeded). If input details or phone format (BR-012) is invalid, system displays validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-012, BR-025, BR-031, BR-201 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-31 Create Student Use Cases Specification

#### View List Allergy & Special Meals

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-036 - View List Allergy & Special Meals | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff opens a pending student allergy declaration or navigates to update an existing verified allergy profile. | | |
| Description | Enables the Medical Staff to verify, approve, and maintain student allergy profiles, ensuring accurate allergy alerts are active and distributed to Service Caterers and Teachers. | | |
| Preconditions | The medical staff is authenticated and authorized. Student allergy declarations exist for review or existing profiles need updating. | | |
| Postconditions | Allergy profile is verified and active. Allergy alerts enabled. Service Caterers and Teachers notified. Teacher meal serving screen shows allergy alert in red (BR-123). MSG-036 displayed. | | |
| Normal Flow | 1. Medical Staff navigates to Allergy Management and selects a pending or existing allergy record.  2. System displays current allergen details, severity, supporting documentation, and dietary restrictions.  3. Medical Staff reviews, verifies, and updates allergen types, severity classification, and approved dietary category (BR-066).  4. System validates completeness and that dietary category is in the supported list (Halal, Vegetarian, Gluten-Free, Nut-Free per BR-066).  5. System activates the allergy profile, notifies Service Caterers and Teachers, and displays MSG-036. | | |
| Alternative Flows | AF1 - Insufficient Documentation: Medical Staff determines the uploaded documentation is incomplete. System sends a documentation request to the parent before approval. | | |
| Exceptions | MSG-004, MSG-005, MSG-036 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-031, BR-172, BR-066 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-32 View List Allergy & Special Meals Use Cases Specification

#### View List Student Health Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-050 - View List Student Health Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Primary actor requests to view list student health information. | | |
| Description | Enables the Parent to access and manage their children's health information hub, linking to new submissions or updates. | | |
| Preconditions | The parent is authenticated and authorized. The parent account is linked to at least one active student profile (BR-031). | | |
| Postconditions | Children's health profiles and status (Pending/Verified) are displayed. No modifications occur unless parent triggers submission or update actions. | | |
| Normal Flow | 1. Parent navigates to the 'My Children' Health section in the portal.  2. System retrieves and displays the list of linked student profiles for the parent (BR-031, max 5 students BR-201).  3. Parent selects a child's profile.  4. System displays the child's health information hub, showing current allergy profiles, medical records, and status (Pending/Verified).  5. Parent can choose to submit a new health declaration (UC-086) or update existing details (UC-095). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-201 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-33 View List Student Health Information Use Cases Specification

#### View List Student Health Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-051 - View List Student Health Records | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to view list student health records. | | |
| Description | Enables the Medical Staff to search, retrieve, and append clinical records, medications, and health histories for students. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Student health records are retrieved and displayed. Append actions are enabled. Existing verified history logs are protected from modifications (BR-172). | | |
| Normal Flow | 1. Medical Staff navigates to Student Health Management. 2. System displays search filters (by school, grade, class, student name, status). 3. Medical Staff searches and selects a student's health record file. 4. System retrieves and displays the student's clinical file (medical conditions, medications, emergency contacts, health history). 5. Medical Staff can append annotations, record new clinical logs (which cannot be deleted BR-172), or configure emergency notes for offline access. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-172 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-34 View List Student Health Records Use Cases Specification

#### Review Health Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-077 - Review Health Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to review health information. | | |
| Description | Enables the Medical Staff to verify and approve pending student health and allergy declarations. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Student health information status is updated. Approved profiles become active. | | |
| Normal Flow | 1. Medical Staff navigates to the Health Review panel. 2. System queries the database and displays a list of pending student health and allergy declarations. 3. Medical Staff selects a pending record to review. 4. System displays the declaration details (allergy type, severity, uploaded medical documentation). 5. Medical Staff verifies the medical documentation (BR-031). If documentation is valid and matches the declaration, Medical Staff clicks 'Approve'. 6. System updates the status of the declaration to 'Verified' (BR-031), activates the student's health profile, and displays MSG-036. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If documentation is missing or invalid, Medical Staff rejects the request and specifies the reason. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-35 Review Health Information Use Cases Specification

#### Review Special Meal Requests

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-078 - Review Special Meal Requests | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to review special meal requests. | | |
| Description | Enables the Medical Staff to review, verify, and approve custom dietary requests submitted by parents, routing valid setups to the kitchen. | | |
| Preconditions | The medical staff is authenticated and authorized. Pending special meal requests exist. | | |
| Postconditions | Special meal requests are verified. Student's special meal status is updated. Requirements routed to Service Caterers. | | |
| Normal Flow | 1. Medical Staff navigate to the Special Meal Requests section. 2. System retrieves and displays the list of pending special meal requests submitted by parents. 3. Medical Staff selects a request to review and inspects the details (dietary type, duration, medical justification). 4. Medical Staff checks alignment with standard kitchen categories (Halal, Vegetarian, Gluten-Free, Nut-Free per BR-066). 5. Medical Staff approves the request. System updates the student's special meal status, routes the requirement to the kitchen/Service Caterers, and displays success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If request details violate rules or standard categories, Medical Staff rejects request and specifies reason. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-066 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-36 Review Special Meal Requests Use Cases Specification

#### Submit Health & Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-084 - Submit Health & Allergy Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent navigates to Health & Allergy Management and selects 'Submit Health & Allergy Information' for their child. | | |
| Description | Enables the Parent to submit their child's health and allergy information including allergen details, severity, dietary restrictions, and required medical documentation for Medical Staff verification. | | |
| Preconditions | Parent is authenticated and authorized. Parent account is linked to at least one active student (BR-031). Health declaration submission is required. | | |
| Postconditions | Health and allergy record saved as 'Pending Verification'. Medical Staff notified for review (BR-031). Severe allergy declarations require medical documentation upload (BR-031). | | |
| Normal Flow | 1. Parent navigates to Health & Allergy Management and selects their child's profile.  2. System displays the health submission form with allergen, severity, and dietary restriction fields.  3. Parent enters allergen details, severity level, dietary restrictions, and uploads medical documentation if severity is 'Severe' (BR-031).  4. System validates completeness and that 'Severe' severity has corresponding documentation.  5. System saves the record as 'Pending Verification', notifies Medical Staff (BR-031), and displays submission confirmation. | | |
| Alternative Flows | AF1 - Severe Allergy Without Documentation: Parent selects 'Severe' severity without uploading medical documentation. System blocks submission and prompts document upload requirement (BR-031). AF2 - Annual Reminder: System automatically reminds parents at academic year start to review and update their child's health declaration. | | |
| Exceptions | MSG-004, MSG-005, MSG-036 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-031, BR-172, BR-066 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-37 Submit Health & Allergy Information Use Cases Specification

#### Update Health & Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-093 - Update Health & Allergy Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Primary actor requests to update health & allergy information. | | |
| Description | Enables the Parent to modify current student health details, allergy parameters, or upload new medical files. | | |
| Preconditions | The parent is authenticated and authorized. Parent is linked to the student (BR-031). | | |
| Postconditions | Health and allergy details are updated in the database as 'Pending Verification'. Medical Staff are notified. | | |
| Normal Flow | 1. Parent navigates to the Health Information hub and selects 'Update Information' for their child.  2. System displays the current health details in an editable form.  3. Parent modifies fields: medical notes, emergency contacts, or allergens. If changing to 'Severe' allergy, parent must upload new medical documentation (BR-031).  4. System validates that the required documentation is attached for severe allergies (BR-031) and parent student limits are respected (BR-201).  5. System saves the updated declaration as 'Pending Verification', notifies Medical Staff (BR-031), logs the modification, and displays a success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If required documentation for severe allergy is missing, update is blocked. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-031, BR-201 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-38 Update Health & Allergy Information Use Cases Specification

#### Update Health Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-094 - Update Health Records | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to update health records. | | |
| Description | Enables the Medical Staff to append logs, add medication details, or edit clinical parameters on student health files. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Health records are updated. Existing verified history logs are protected from modifications (BR-172). | | |
| Normal Flow | 1. Medical Staff accesses the Student Health Records management panel, searches for the target student, and clicks 'Update Record'.  2. System displays the student's current health file in edit mode.  3. Medical Staff appends clinical annotations, edits medications, or updates emergency instructions.  4. System validates that existing verified health history logs are not deleted or modified (only appended per BR-172).  5. System saves the updated health record, updates the audit logs, and displays success message MSG-036. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-036 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-172 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-39 Update Health Records Use Cases Specification

#### Update Student

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-099 - Update Student | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to update student. | | |
| Description | Enables the School Administration to modify basic profile parameters or classroom assignments of a student. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The update student transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. School Administration navigates to Student Management, searches for the target Student, and clicks 'Edit Profile'.  2. System displays the student's profile form pre-filled with current details.  3. School Administration modifies fields: Name, Date of Birth, or Classroom assignment.  4. If classroom assignment is modified, system validates that the new classroom has not reached its capacity of 50 students (BR-025).  5. System saves the changes, updates class list distribution (if class is changed), logs the action, and displays a success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-010 (if classroom capacity is exceeded). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-041 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-40 Update Student Use Cases Specification

#### View Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-104 - View Allergy Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff, Parent | Secondary Actors | None |
| Trigger | Primary actor requests to view allergy information. | | |
| Description | Enables the Medical Staff, Parent to safely query, view, or export the details of Allergy Information records. | | |
| Preconditions | The medical staff, parent is authenticated and authorized to access this feature. | | |
| Postconditions | The view allergy information data is successfully retrieved and displayed to the medical staff, parent. | | |
| Normal Flow | 1. Medical Staff or Parent navigates to the Allergy Information section in the system portal.  2. System queries the database for active student allergy records.  - If Parent: displays allergy profiles for their linked children (BR-031).  - If Medical Staff: displays all student allergy profiles.  3. System renders the list showing: Student Name, Class, Allergen Name, Severity, and Verification Status (BR-031).  4. User can search, filter by severity, or download the details as a report. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-031 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-41 View Allergy Information Use Cases Specification

#### View Student Health & Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-145 - View Student Health & Allergy Information | | |
| Created By | ManhNDHE160692 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Primary actor requests to view student health & allergy information. | | |
| Description | Enables the homeroom Teacher to query and view allergy alerts and medical files for students in their class. | | |
| Preconditions | The teacher is authenticated and authorized to access this feature. | | |
| Postconditions | Class registry of health and allergy details is retrieved and displayed. Active allergy alerts are highlighted in red (BR-123). | | |
| Normal Flow | 1. Teacher navigates to the Student Health & Allergy Information section in the system portal.  2. System retrieves the class registry assigned to this Teacher (BR-025).  3. System displays the class health list showing: Student Name, medical alerts, and allergy declarations.  4. Allergies classified as 'Severe' are automatically highlighted in red (BR-123) on the teacher's screen.  5. Teacher reviews the alerts before meal serving to prevent safety hazards. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-025, BR-123 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-42 View Student Health & Allergy Information Use Cases Specification

#### View Student Health History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-146 - View Student Health History | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to view student health history. | | |
| Description | Enables the Medical Staff to safely query, view, or export the details of Student Health History records. | | |
| Preconditions | Student health history timeline is retrieved and displayed in read-only mode (BR-172). | | |
| Postconditions | The view student health history data is successfully retrieved and displayed to the medical staff. | | |
| Normal Flow | 1. Medical Staff navigates to the Student Health History section in the system portal.  2. System queries the database and displays the timeline of all health events, allergy incidents, clinical notes, and declarations for the selected student.  3. System renders the historical logs in a non-editable timeline format (BR-172).  4. Medical Staff reviews the history logs and can export the timeline as a report. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-123 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-43 View Student Health History Use Cases Specification

#### View Student List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-147 - View Student List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view student list. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Student List records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Student list is retrieved and displayed based on role permissions (BR-025). | | |
| Normal Flow | 1. School Administration or Teacher navigates to the Student List section in the system portal. 2. System queries about the database to retrieve student profiles 3. System displays the list of students showing: Name, school registration number, Class, and Parent Contact details. 4. User can search by name, filter by class/grade, or export the list of students to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-44 View Student List Use Cases Specification

