### Meal Serving & Distribution

#### Confirm Meal Distribution

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-016 - Confirm Meal Distribution | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | Service Caterers |
| Trigger | Teacher receives a meal distribution notification and opens the classroom distribution confirmation screen. | | |
| Description | Enables the Teacher to confirm correct meal distribution to all students in their classroom via the mobile application, including confirmation of special meals. | | |
| Preconditions | Teacher is authenticated and authorized. Classroom distribution list has been generated. | | |
| Postconditions | Meal distribution confirmed and recorded with timestamp (MSG-045). | | |
| Normal Flow | 1. Teacher navigates to the Meal Distribution List (UC-134) and selects the current meal service session for their classroom.  2. System displays the distribution list with standard and special meal assignments. Special alerts highlighted in red (BR-123).  3. Teacher verifies each student received the correct meal.  4. Teacher confirms all distributions and marks the classroom as served.  5. System records confirmation with timestamp (MSG-045) | | |
| Alternative Flows | AF1 - Distribution Error: Teacher identifies an incorrect meal assignment and reports the error. The system notifies the Service Caterers for immediate correction (BR-065). Distribution completion is blocked until the issue is resolved. Once the meal has been corrected, the Teacher verifies the correction and resumes the process from Step 3 of the Normal Flow. | | |
| Exceptions | MSG-004, MSG-005, MSG-021 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-121, BR-065, BR-123, BR-125, BR-120, BR-130 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-108 Confirm Meal Distribution Use Cases Specification

#### Manage Meal Service

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-047 - Manage Meal Service | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to manage meal service. | | |
| Description | Enables the Service Caterers to manage details, modify parameters, or update configuration fields of Meal Service. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. A meal service session is active or planned for the day. | | |
| Postconditions | The manage meal service transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Service Caterers accesses Meal Service Management panel.  2. System displays active meal service sessions with status, classroom assignments, and meal counts.  3. Service Caterers selects a session to view details.  4. System displays serving schedule, meal allocation per classroom, and serving staff assignments.  5. Service Caterers updates serving parameters (e.g., timing, staff, portion adjustments).  6. System validates changes and saves updated configuration. | | |
| Alternative Flows | AF1 - Session Already Active: Service Caterers attempts to modify a session that is currently in serving status. System restricts modifications to non-critical fields only (e.g., staff reassignment allowed, portion reduction blocked) and displays a warning.  AF2 - Allocation Mismatch: System detects that updated portion counts do not match the total student count on distribution lists (BR-121). System displays a warning and requires Service Caterers to acknowledge the discrepancy before saving. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-121, BR-125, BR-100 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-109 Manage Meal Service Use Cases Specification

#### Monitor Child Meal History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-053 - Monitor Child Meal History | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent requests to monitor child meal history. | | |
| Description | Enables the Parent to safely query, view, or export the details of Child Meal History records. | | |
| Preconditions | The parent is authenticated and authorized to access this feature. | | |
| Postconditions | The monitor child meal history data is successfully retrieved and displayed to the parent. | | |
| Normal Flow | 1. Parent navigates to the Child Meal History section from the home screen.  2. System retrieves and displays the default child profile  3. System displays a chronological meal history list showing: date, meal session, served dishes, and consumption status (Fully/Partially/Not Consumed) (BR-121).  4. Parent selects a specific date entry to view detailed information including nutritional breakdown and any special meal arrangement applied.  5. System displays the detailed meal record with dish names, ingredients, calorie information, and serving timestamp. | | |
| Alternative Flows | AF1 - Switch Child Profile: Parent selects a different child from the child selector dropdown. System refreshes the meal history for the newly selected child.  AF2 - No Meal Records Found: System finds no meal records for the selected date range. System displays an informational message and suggests adjusting the date filter. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-201, BR-121, BR-133 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-110 Monitor Child Meal History Use Cases Specification

#### Monitor School Meals

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-055 - Monitor School Meals | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent navigates to 'School Meals' to view published menus and nutritional information for their child. | | |
| Description | Enables the Parent to view published daily and weekly school menus with complete nutritional content, allergen declarations, and allergy accommodation details specific to their child. | | |
| Preconditions | Parent is authenticated and authorized. Parent account is linked to at least one active student (BR-031). Published menus are available. | | |
| Postconditions | Published menus displayed with nutritional details and allergen declarations. Child-specific allergy-relevant information highlighted. | | |
| Normal Flow | 1. Parent navigates to 'School Meals' from the home screen. 2. System retrieves and displays approved, published menus for current and upcoming week (BR-070). 3. Parent browses meals viewing nutritional content, full ingredient lists, and allergen declarations (BR-069). 4. System highlights allergy-relevant information and special meal arrangements specific to the parent's child's verified allergy profile. 5. Parent can view detailed nutrition breakdown per dish and filter by meal date. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-069, BR-070, BR-075 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-111 Monitor School Meals Use Cases Specification

#### Monitor Student Meal History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-056 - Monitor Student Meal History | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Primary actor requests to monitor student meal history. | | |
| Description | Enables the Teacher to safely query, view, or export the details of Student Meal History records. | | |
| Preconditions | The teacher is authenticated and authorized to access this feature. | | |
| Postconditions | The monitor student meal history data is successfully retrieved and displayed to the teacher. | | |
| Normal Flow | 1. Teacher navigates to the Student Meal History section in the system portal.  2. System retrieves the class registry assigned to this Teacher (BR-025) and displays the student list.  3. System displays a summary view showing each student's recent meal history: date, dishes served, and consumption status (BR-121).  4. Teacher selects a specific student to view their detailed meal history across multiple sessions.  5. System displays the selected student's full meal record including consumption trends, special meal arrangements, and allergy-safe meal compliance. | | |
| Alternative Flows | AF1 - Filter by Date Range: Teacher applies a date range filter. System refreshes the meal history display for the selected period. AF2 - Student with Special Dietary Plan: Teacher selects a student on a special dietary plan. System highlights the special meal details and allergy accommodations applied in each session. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-133, BR-121 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-112 Monitor Student Meal History Use Cases Specification

#### Student Meal Consumption

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-080 - Student Meal Consumption | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Teacher initiates student meal consumption recording during or after the meal service session. | | |
| Description | Enables the Teacher to record meal consumption status for students on special dietary plans, supporting nutritional compliance tracking and parent daily reporting. | | |
| Preconditions | Teacher is authenticated and authorized. Meal service session is active (BR-125). Classroom distribution has been confirmed. | | |
| Postconditions | Consumption records saved for students on special dietary plans. Data included in daily parent summary notification (BR-133). | | |
| Normal Flow | 1. Teacher navigates to Student Meal Consumption for their classroom. 2. System displays students on special dietary plans for the current session. 3. Teacher records each student's consumption: 'Fully Consumed', 'Partially Consumed', or 'Not Consumed' (BR-121). 4. System validates all required consumption entries are recorded. 5. System saves records and includes data in the daily parent summary notification (BR-133). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-021 | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-125, BR-121, BR-133 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-113 Student Meal Consumption Use Cases Specification

#### View Allergy Alerts

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-103 - View Allergy Alerts | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers, Teacher | Secondary Actors | None |
| Trigger | Primary actor requests to view allergy alerts. | | |
| Description | Enables the Service Caterers, Teacher to safely query, view, or export the details of Allergy Alerts records. | | |
| Preconditions | The Service Caterers, teacher is authenticated and authorized to access this feature. | | |
| Postconditions | The view allergy alerts data is successfully retrieved and displayed to the Service Caterers, teacher. | | |
| Normal Flow | 1. Service Caterers or Teacher navigates to the Allergy Alerts section in the system portal.  2. System retrieves and displays the list of students with active allergy profiles for the current or upcoming meal session.  3. System highlights allergies classified as 'Severe' in red (BR-123) and displays: student name, classroom, allergen type, severity level, and assigned special meal arrangement.  4. Actor selects a specific student to view detailed allergy information including declared allergens, medical notes, and historical allergy-safe meal records.  5. System displays the detailed allergy profile with linked special meal plan and dual-confirmation status (BR-123). | | |
| Alternative Flows | AF1 - Allergy Conflict with Today's Menu: System detects that today's approved menu contains ingredients matching a student's declared allergen. System displays an allergy conflict warning (MSG-051) and highlights the affected dish and student.  AF2 - No Active Allergy Alerts: System finds no students with active allergy profiles in the actor's scope. System displays an informational message confirming no allergy alerts. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-123, BR-065 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-114 View Allergy Alerts Use Cases Specification

#### View Meal Distribution List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-132 - View Meal Distribution List | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers, Teacher | Secondary Actors | None |
| Trigger | Primary actor requests to view meal distribution list. | | |
| Description | Enables the Service Caterers and Teacher to safely query, view, or export the details of Meal Distribution List records. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The view meal distribution list data is successfully retrieved and displayed to the Service Caterers. | | |
| Normal Flow | 1. Service Caterers navigates to the Meal Distribution List section.  2. System retrieves and displays the distribution list for active meal session, including classroom name, student count, special meal count, and serving status.  3. Service Caterers can filter by date, classroom, or meal type.  4. System supports export to PDF/Excel format. | | |
| Alternative Flows | AF1 - Filter by Session or Date: Service Caterers selects a different date or meal session. System refreshes the distribution list for the selected session.  AF2 - Distribution Incomplete: System highlights classrooms where distribution confirmation has not been received from the homeroom teacher (BR-120). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-123, BR-121, BR-065, BR-120 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-115 View Meal Distribution List Use Cases Specification

#### View Meal History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-133 - View Meal History | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Primary actor requests to view meal history. | | |
| Description | Enables the Parent to safely query, view, or export the details of Meal History records. | | |
| Preconditions | The parent is authenticated and authorized to access this feature. | | |
| Postconditions | The view meal history data is successfully retrieved and displayed to the parent. | | |
| Normal Flow | 1. Parent navigates to the Meal History section from the home screen.  2. System retrieves the school associated with the parent's linked student(s) and displays a chronological list of past meal service sessions.  3. Each entry shows: date, meal session type (breakfast/lunch), menu summary, and total dishes served.  4. Parent selects a specific date to view the full menu details including dish names, ingredients, and nutritional breakdown.  5. System displays the detailed meal record with allergen declarations for each dish. | | |
| Alternative Flows | AF1 - Filter by Date Range: Parent applies a date range filter. System refreshes the list to show only meal sessions within the selected range.  AF2 - No Records Found: System finds no meal history for the selected filters. System displays an informational message. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-069, BR-070, BR-133 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-116 View Meal History Use Cases Specification

#### View Special Meal Arrangements

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-141 - View Special Meal Arrangements | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Primary actor requests to view special meal arrangements. | | |
| Description | Enables the Parent to safely query, view, or export the details of Special Meal Arrangements records. | | |
| Preconditions | The parent is authenticated and authorized to access this feature. | | |
| Postconditions | The view special meal arrangements data is successfully retrieved and displayed to the parent. | | |
| Normal Flow | 1. Parent navigates to the Special Meal Arrangements section from the child's profile or meal monitoring portal.  2. System retrieves the active child profile.  3. System displays the child's current special meal arrangements showing: allergen type, severity level, substitute dish assignments, and accommodation notes.  4. Parent reviews the upcoming meal sessions with special meal details, confirming that allergy-safe alternatives are arranged (BR-065).  5. System displays the dual-confirmation status for each special meal (Kitchen release and Teacher hand-off) (BR-123). | | |
| Alternative Flows | AF1 - No Special Meal Arrangements: Selected child has no active allergy or dietary restriction profile. System displays a message indicating no special arrangements are required and provides a link to submit allergy information (UC-086).  AF2 - Switch Child Profile: Parent selects a different child. System refreshes the special meal arrangements for the selected child. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-201, BR-065, BR-123 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-117 View Special Meal Arrangements Use Cases Specification

#### View Special Meal Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-142 - View Special Meal Records | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Primary actor requests to view special meal records. | | |
| Description | Enables the Parent to safely query, view, or export the details of Special Meal Records records. | | |
| Preconditions | The parent is authenticated and authorized to access this feature. | | |
| Postconditions | The view special meal records data is successfully retrieved and displayed to the parent. | | |
| Normal Flow | 1. Parent navigates to the Special Meal Records section from the child's meal history or special meal monitoring area.  2. System retrieves the active child profile and displays a chronological list of past special meal sessions.  3. Each entry shows: date, original menu dish, substitute dish served, allergen avoided, and dual-confirmation status (BR-123). 4. Parent selects a specific record to view details including preparation notes, Kitchen release timestamp, and Teacher hand-off confirmation.  5. System displays the full special meal record with consumption status (Fully/Partially/Not Consumed) (BR-121). | | |
| Alternative Flows | AF1 - No Special Meal Records: System finds no historical special meal records for the selected child. System displays a message indicating no past special meals are available.  AF2 - Switch Child Profile: Parent selects a different child. System refreshes the special meal records for the selected child. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-065, BR-123 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-118 View Special Meal Records Use Cases Specification

#### View Special Meal Requirements

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-143 - View Special Meal Requirements | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Primary actor requests to view special meal requirements. | | |
| Description | Enables the Teacher to safely query, view, or export the details of Special Meal Requirements records. | | |
| Preconditions | The teacher is authenticated and authorized to access this feature. | | |
| Postconditions | The view special meal requirements data is successfully retrieved and displayed to the teacher. | | |
| Normal Flow | 1. Teacher navigates to the Special Meal Requirements section in the system portal.  2. System retrieves the class registry assigned to this Teacher (BR-025) and filters students with active special dietary requirements. 3. System displays a list of students requiring special meals showing: student name, allergen type, severity level (highlighted in red for Severe per BR-123), and assigned special meal for the current session.  4. Teacher selects a specific student to view detailed dietary requirement information including medical notes, declared allergens, and special meal history.  5. Teacher reviews the requirements and confirms readiness for meal distribution verification. | | |
| Alternative Flows | AF1 - No Students with Special Requirements: No students in the teacher's classroom have active dietary restrictions. System displays a message confirming no special meal requirements for the current session.  AF2 - New Allergy Declaration: System indicates a recently updated allergy profile that the teacher has not yet reviewed. System highlights the new entry with a "New" badge. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025, BR-123, BR-065 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-119 View Special Meal Requirements Use Cases Specification

