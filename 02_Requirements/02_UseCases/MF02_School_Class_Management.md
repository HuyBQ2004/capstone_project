### School & Class Management

#### Active/ Deactivate School

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-001 - Active/ Deactivate School | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin selects a school record and requests to change its operational status. | | |
| Description | Enables the System Admin to activate or deactivate a school record. Deactivation cascades to suspend all associated classes, staff accounts, and student accounts. | | |
| Preconditions | The system admin is authenticated and authorized. The target school record exists in the system. | | |
| Postconditions | School operational status is updated. If deactivated, all associated classes, staff, and student accounts are suspended (BR-021). Audit log records the action. | | |
| Normal Flow | 1. System Admin navigates to School Management and selects the target school.  2. System displays the school profile with current operational status.  3. System Admin triggers the status toggle (Activate/Deactivate).  4. System validates that cascading deactivation will not cause unresolvable data conflicts.  5. System updates school status, cascades suspension to all associated accounts (BR-021), records the action in the audit trail, and displays a success confirmation. | | |
| Alternative Flows | AF1 - Reassignment: Classroom already has a homeroom teacher. System prompts for reassignment confirmation. On confirm, system unlinks the current teacher and assigns the new teacher, logging both actions (BR-026). | | |
| Exceptions | MSG-004, MSG-005, MSG-011 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-021 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-20 Active/Deactivate School Use Cases Specification

#### Assign Homeroom Teacher

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-008 - Assign Homeroom Teacher | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects a classroom and initiates homeroom teacher assignment. | | |
| Description | Enables the School Administration to assign or reassign an active homeroom teacher to a classroom, with audit trail recording the change. | | |
| Preconditions | The school administration is authenticated and authorized. The target teacher is an active staff member with the Teacher role. Only one homeroom teacher per classroom is permitted (BR-020). | | |
| Postconditions | Teacher is assigned as homeroom teacher. Previous assignment (if any) is removed. Audit log records the assignment with details and date (BR-026). | | |
| Normal Flow | 1. School Administration navigates to Classroom Management and selects the target classroom.  2. System displays current homeroom teacher assignment (if any) and available teachers.  3. School Administration selects the new homeroom teacher from active staff.  4. System validates the selected teacher is not already assigned as homeroom teacher to another classroom (MSG-011 if conflict).  5. System validates the one-teacher-per-classroom rule (BR-020) and confirms no conflicts.  6. System saves the assignment, creates an audit log entry with assignment details and date (BR-026), and displays success message MSG-034. | | |
| Alternative Flows | AF1 - Reassignment: Classroom already has a homeroom teacher. System prompts for reassignment confirmation. On confirm, system unlinks the current teacher and assigns the new teacher, logging both actions (BR-026). | | |
| Exceptions | MSG-004, MSG-005, MSG-011 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-020, BR-026 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-21 Assign Homeroom Teacher Use Cases Specification

#### Create Classroom

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-018 - Create Classroom | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration navigates to Classroom Management and selects 'Create Classroom'. | | |
| Description | Enables the School Administration to create a new classroom record with name, valid grade level, and student capacity not exceeding 50. | | |
| Preconditions | The school administration is authenticated and authorized. The school record is active. | | |
| Postconditions | Classroom record is saved in the database. Classroom is available for student enrollment and teacher assignment. | | |
| Normal Flow | 1. School Administration navigates to Classroom Management and selects 'Create Classroom'.  2. System displays the classroom creation form.  3. School Administration enters: classroom name, grade level (1-12 per BR-024), and maximum student capacity (max 50 per BR-025).  4. System validates that the classroom name is unique within the school.  5. System validates grade level falls within 1-12 range and capacity does not exceed 50 (MSG-010 if capacity exceeded).  6. System saves the classroom record, logs the creation, and displays MSG-033. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-010. If grade level is invalid or name is duplicate, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-22 Create Classroom Use Cases Specification

#### Manage Classrooms

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-037 - Manage Classrooms | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to manage classrooms. | | |
| Description | Enables the School Administration to query, filter, and view classrooms list, acting as the portal for create, edit, and teacher assignment actions. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Classroom records are retrieved and displayed. Entries for Create, Update, and Assign Teacher actions are enabled. | | |
| Normal Flow | 1. School Administration navigates to Classroom Management.  2. System queries the database and retrieves all classroom profiles for the school.  3. System displays the list of classrooms (Class Name, Grade Level, Student Capacity, Homeroom Teacher).  4. School Administration can filter by grade level, search by class name, or select a classroom to update details (UC-092) or assign teacher (UC-008). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-23 Manage Classrooms Use Cases Specification

#### View Classroom Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-165 - View Classroom Detail | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view classroom detail. | | |
| Description | Enables the School Administration to view the comprehensive details of a specific classroom, including its profile, capacity, assigned homeroom teacher, and student list. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view classroom detail data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Classroom List and selects a specific classroom.  2. System queries the database and retrieves the classroom's detailed profile.  3. System displays the classroom details including Class Name, Grade Level (BR-024), Max Capacity (BR-025), the single assigned Homeroom Teacher (BR-020), and the class student list (BR-025). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-020, BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ View Classroom Detail Use Cases Specification

#### View List Classrooms

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-166 - View List Classrooms | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to manage classrooms. | | |
| Description | Enables the School Administration to query, filter, and view classrooms list, acting as the portal for create, edit, and teacher assignment actions. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Classroom records are retrieved and displayed. Entries for Create, Update, and Assign Teacher actions are enabled. | | |
| Normal Flow | 1. School Administration navigates to Classroom Management.  2. System queries the database and retrieves all classroom profiles for the school.  3. System displays the list of classrooms (Class Name, Grade Level, Student Capacity, Homeroom Teacher).  4. School Administration can filter by grade level, search by class name, or select a classroom to update details (UC-092) or assign teacher (UC-008). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ View List Classrooms Use Cases Specification

#### Register School

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-070 - Register School | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Guest | Secondary Actors | None |
| Trigger | Guest requests to register a school. | | |
| Description | Enables a Guest (school representative) to input, validate, and submit new school details for platform onboarding. | | |
| Preconditions | The registration form is publicly accessible. No prior authentication is required. | | |
| Postconditions | School registration details are saved. School status is set to Pending. Verification email is sent. Audit log entry is created. | | |
| Normal Flow | 1. Guest navigates to the Register School panel. 2. System displays the school registration form. 3. Guest enters school details: Name, Tax ID, Operational Permit, Official Email, Address, and Geographical Coordinates (latitude/longitude). 4. System validates the email address format, Tax ID format, and geographical coordinates (BR-028). 5. System saves the school record in 'Pending' status, sends a confirmation email to the official school email, creates an audit trail, and displays MSG-032. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-007 (Invalid tax ID format). If coordinates or email is invalid, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-028 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-24 Register School Use Cases Specification

#### Update Classroom

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-090 - Update Classroom | | |
| Created By | ManhNDHE160692 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to update classroom. | | |
| Description | Enables the School Administration to modify parameters or update configuration fields of an existing Classroom record. | | |
| Preconditions | The school administration is authenticated and authorized. The classroom exists. | | |
| Postconditions | The classroom details are updated in the database and audited. | | |
| Normal Flow | 1. School Administration navigates to Classroom Management, searches for the target Classroom, and clicks 'Edit'.  2. System displays the classroom update form pre-filled with current details: Name, Grade Level, and Capacity.  3. School Administration modifies fields: Name, Grade Level (BR-024), or Max Capacity (BR-025).  4. System validates updated details: Grade Level (1-12), Max Capacity (max 50), and ensures new capacity is not less than the current count of enrolled students.  5. System saves the classroom record, creates an audit log entry, and displays a success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-010. If grade level is invalid or name is duplicate, or new capacity is less than the current number of students that the Admin wants to update. System displays validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-25 Update Classroom Use Cases Specification

#### Update Information School

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-095 - Update Information School | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to update information school. | | |
| Description | Enables the System Admin (platform-level) or School Administration (school-level) to modify school profile details and operational configuration fields. | | |
| Preconditions | The user is authenticated and authorized. The target school exists. | | |
| Postconditions | School profile details and operational configuration parameters are updated, saved, and audited. | | |
| Normal Flow | 1. User navigates to School Settings or Management panel and selects the target School to edit. 2. System displays the school details form: Name, Address, Coordinates, Tax ID, Official Email, and Operational Parameters. 3. User modifies details. If changing coordinates (BR-028), system validates and locks them for future receiving GPS checks. 4. If modifying school-level parameters, system verifies operational rules (BR-030). If edited by non-administrator staff, system flags for School Admin review. 5. System saves the updated details, records the changes in the audit trail, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If coordinate lock validation fails, system displays an error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-028, BR-030 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-26 Update Information School Use Cases Specification

#### View Information School

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-122 - View Information School | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view information school. | | |
| Description | Enables the System Admin or School Administration to query and view the complete details and configuration of a School. | | |
| Preconditions | The user is authenticated and authorized. | | |
| Postconditions | School detailed information and operational stats are retrieved and displayed. | | |
| Normal Flow | 1. User navigates to the School Information section in the system portal.  2. System queries the database to retrieve the school's complete profile: Name, Address, Tax ID, Email, Coordinates, operational hours, emergency contact, and stats.  3. System renders and displays the school detailed profile on the screen.  4. User reviews the details and can choose to export the school profile to PDF. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-022, BR-025 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-27 View Information School Use Cases Specification

#### View School List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-140 - View School List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view school list. | | |
| Description | Enables the System Admin to query, view, or export the details of all registered schools. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view school list data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the School List section in the system portal.  2. System queries the database for all registered schools.  3. System displays the schools in a table view (Name, Code, Tax ID, Status: Active/Inactive/Pending).  4. System Admin can search by school name, filter by operational status, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-022, BR-025 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-29 View School List Use Cases Specification

#### Create Academic Year

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-174 - Create Academic Year | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration navigates to Academic Year Management and selects 'Create Academic Year'. | | |
| Description | Enables the School Administration to create a new academic year record for the school, optionally copying classroom structure from the previous academic year as a starting template. | | |
| Preconditions | The school administration is authenticated and authorized. The school record is active. | | |
| Postconditions | Academic year record is saved with status Draft. If classroom structure was copied, new classroom records are created linked to the new academic year; existing classroom records from prior years remain unchanged (BR-194). | | |
| Normal Flow | 1. School Administration navigates to Academic Year Management and selects 'Create Academic Year'.  2. System displays the creation form (year label, e.g. "2026-2027", start date, end date).  3. School Administration optionally selects 'Copy classroom structure from previous academic year'.  4. System validates the year label is unique for the school.  5. System saves the new academic year record with status Draft, creates copied classroom records if requested (linked to the new academic year), logs the creation, and displays MSG-082. | | |
| Alternative Flows | AF1 - Copy Classroom Structure: School Administration selects 'Copy from previous year'. System retrieves classroom names, grade levels, and capacities from the most recent academic year and creates matching classroom records under the new academic year, without transferring student enrollment (BR-194). | | |
| Exceptions | MSG-004, MSG-005. If the school already has an Active academic year and School Administration attempts to activate the new one directly, system displays MSG-080 (BR-191). | | |
| Priority | Medium | | |
| Frequency of Use | Annually | | |
| Business Rules | BR-191, BR-192, BR-194 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-155 Create Academic Year Use Cases Specification

#### Activate/Close Academic Year

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-175 - Activate/Close Academic Year | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects an academic year record and requests to change its status. | | |
| Description | Enables the School Administration to activate a Draft academic year, making it the school's current academic year, or close the currently Active academic year, transitioning its classrooms to read-only. | | |
| Preconditions | The school administration is authenticated and authorized. The target academic year record exists. | | |
| Postconditions | Academic year status is updated (Draft to Active, or Active to Closed). If closed, all classroom records under that academic year become read-only (BR-193) while historical data remains queryable. Audit log records the action. | | |
| Normal Flow | 1. School Administration navigates to Academic Year Management and selects the target academic year.  2. System displays the current status and available transitions.  3. School Administration triggers the status change (Activate or Close).  4. If activating, system validates no other academic year for this school is currently Active (BR-191); if one exists, system blocks the action and displays MSG-080, prompting the admin to close it first.  5. If closing, system transitions all classroom records under this academic year to read-only (BR-193), preserving historical data.  6. System updates the academic year status, records the action in the audit trail, and displays MSG-083. | | |
| Alternative Flows | AF1 - Force Close Prompt: When closing, if some classrooms still have pending operations, system displays a confirmation warning before proceeding; data is never deleted. | | |
| Exceptions | MSG-004, MSG-005, MSG-080. | | |
| Priority | Medium | | |
| Frequency of Use | Annually | | |
| Business Rules | BR-191, BR-193 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-156 Activate/Close Academic Year Use Cases Specification

#### View Academic Year List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-176 - View Academic Year List | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Primary actor requests to view the academic year list. | | |
| Description | Enables the School Administration to query and view all academic years of their school, including Archived ones, and the System Admin to view academic years across schools. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | Academic year list is retrieved and displayed, including status and classroom count per year. | | |
| Normal Flow | 1. Primary actor navigates to Academic Year Management.  2. System queries the database and retrieves all academic year records for the school (or all schools, for System Admin).  3. System displays the list (Year Label, Start/End Date, Status, Number of Classrooms).  4. Primary actor can filter by status or select a year to view its classroom list; Archived years remain queryable in read-only mode (BR-195). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-195 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-157 View Academic Year List Use Cases Specification

#### Promote Students to Next Academic Year

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-177 - Promote Students to Next Academic Year | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration initiates the student promotion process for a new academic year. | | |
| Description | Enables the School Administration to move students in bulk from their current classroom to a classroom in the next academic year (grade-level promotion), with the ability to exclude or hold back individual students (grade retention). | | |
| Preconditions | The school administration is authenticated and authorized. The target academic year exists with a compatible classroom structure (typically copied via UC-174). | | |
| Postconditions | Selected students are enrolled in their new classroom under the new academic year. Excluded students remain linked to their prior classroom for manual placement. Student health and allergy records are not affected (BR-194). Audit log records the batch operation. | | |
| Normal Flow | 1. School Administration navigates to Academic Year Management and selects 'Promote Students'.  2. System displays the source academic year's classrooms grouped by grade level, with the target academic year's corresponding classrooms.  3. School Administration selects a source grade/classroom and a target classroom, then confirms bulk promotion for all enrolled students.  4. System displays the list of affected students with a checkbox to exclude specific students (e.g. for grade retention).  5. School Administration deselects any students to be held back and confirms.  6. System creates new enrollment links between the selected students and the target classroom, leaves excluded students unassigned for manual placement, logs the batch action, and displays MSG-084. | | |
| Alternative Flows | AF1 - Manual Placement: For excluded (held-back) students, School Administration manually assigns them to an appropriate classroom in the new academic year via UC-090 (Update Classroom). | | |
| Exceptions | MSG-004, MSG-005. If the target classroom's capacity would be exceeded, system displays MSG-010. | | |
| Priority | Medium | | |
| Frequency of Use | Annually | | |
| Business Rules | BR-194 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-158 Promote Students to Next Academic Year Use Cases Specification

