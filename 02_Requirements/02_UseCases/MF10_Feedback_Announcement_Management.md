### Feedback & Announcement Management

#### Create Announcement

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-017 - Create Announcement | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to create announcement. | | |
| Description | Enables the School Admin to create, validate, save as draft, and publish announcements to specific user groups associated with their school. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Announcement is saved in database. If published, target users strictly associated with the school (Parents, Teachers, Service Caterers) receive real-time notifications via Firebase Cloud Messaging (FCM) or email. | | |
| Normal Flow | 1. School Admin navigates to the Create Announcement panel.  2. System displays the announcement creation form with Publication Type defaulted to 'Draft'.  3. School Admin inputs announcement details and chooses either **Draft** or **Published** as the Publication Status.  4. School Admin clicks 'Save'.  5. System validates all required fields.  6. System saves the announcement according to the selected Publication Status:   * If Draft is selected, the announcement is saved with Draft status (MSG-071). * If Published is selected, the announcement is saved with Published status and notifications are sent to all targeted recipients (MSG-072). | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Missing Required Fields: School Admin attempts to publish with empty fields. System displays validation error and blocks submission. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-138, BR-144 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-120 Create Announcement Use Cases Specification

#### Manage Meal Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-046 - Manage Meal Feedback | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | Service Caterers |
| Trigger | Parent requests to manage meal feedback. | | |
| Description | Enables the Parent to view their feedback history, modify or delete feedback within the 24-hour submission window, and view responses/actions taken by the Service Caterers or School Admin regarding their feedback. | | |
| Preconditions | Parent is authenticated, authorized, and has previously submitted feedback. | | |
| Postconditions | The manage meal feedback transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Parent navigates to the 'My Feedback History' section.  2. System displays a list of all feedback submitted by the parent, including meal details, submission date, star ratings, and reply status.  3. Parent selects a feedback record.  4. System displays feedback details, including any response/reply submitted by the Service Caterers (BR-143). | | |
| Alternative Flows | AF1 - Edit Feedback: At step 4, if the feedback is within the 24-hour window from the meal serving time (BR-139), Parent clicks 'Edit'. Parent updates rating, comment, or quality tags, and clicks 'Update'. System validates and saves updates, notifying the Service Caterers of the modification.  AF2 - Retract/Delete Feedback: At step 4, if within the 24-hour window, Parent clicks 'Delete'. System prompts for confirmation. Parent confirms, and system deletes the feedback record. | | |
| Exceptions | EX1 - Outside 24-Hour Window: At AF1 or AF2, if 24 hours have passed since the meal serving time, the system disables the 'Edit' and 'Delete' buttons and displays MSG-022 if an edit is attempted via API. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-139, BR-140, BR-142, BR-143, BR-145 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-121 Manage Meal Feedback Use Cases Specification

#### Submit Meal & Food Quality Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-085 - Submit Meal & Food Quality Feedback | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent opens Meal Feedback to submit a quality and safety rating for a recently served meal. | | |
| Description | Enables the Parent to submit meal quality and food safety feedback within 24 hours of meal serving, including star rating, quality concern tags, and safety observations. | | |
| Preconditions | Parent is authenticated and authorized. Meal serving session completed within the past 24 hours (BR-139). Parent's child attended the meal session. | | |
| Postconditions | Feedback saved. If < 3 stars, quality concern tags required (BR-140). If illness keywords detected, emergency alert triggered (BR-142) and Medical Staff notified. Service Caterers notified. | | |
| Normal Flow | 1. Parent navigates to Meal Feedback and selects the served meal session.  2. System displays feedback form with star rating, quality concern tags, and comment field.  3. Parent gives star rating. If < 3 stars, system prompts for quality concern tag selection (BR-140).  4. System analyzes comment text for illness keywords (BR-142).  5. System saves feedback (MSG-046), notifies Service Caterers, and if illness keywords detected, triggers MSG-058 and alerts Medical Staff. | | |
| Alternative Flows | AF1 - Illness Keywords Detected: System detects illness keywords in feedback (e.g., 'stomach ache', 'vomit'). System flags emergency alert (MSG-058) and notifies Medical Staff for immediate review (BR-142). AF2 - Outside 24-Hour Submission Window: Parent attempts feedback after 24h. System blocks submission and displays MSG-022. | | |
| Exceptions | MSG-004, MSG-005, MSG-022, MSG-058 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-139, BR-140, BR-142, BR-143, BR-145 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-122 Submit Meal & Food Quality Feedback Use Cases Specification

#### Submit Meal Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-086 - Submit Meal Feedback | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Primary actor requests to submit meal feedback. | | |
| Description | Enables the Teacher to input, validate, and save new Meal Feedback details into the system database. | | |
| Preconditions | The teacher is authenticated and authorized to access this feature. | | |
| Postconditions | Classroom feedback is saved and aggregated in the dashboard. If illness concerns are reported, alerts are dispatched. | | |
| Normal Flow | 1. Teacher navigates to the Submit Meal Feedback section.  2. System displays a feedback form showing the menu items served to their class today.  3. Teacher inputs: Overall class satisfaction rating (1-5 stars), portion size feedback (e.g., Too Small, Adequate, Too Large), student acceptance comments, and any food safety/quality concerns.  4. Teacher clicks 'Submit'.  5. System validates that the required fields are completed.  6. System saves the feedback record (MSG-046), aggregates the data anonymously into the school dashboard (BR-145), and notifies the Service Caterers and School Admin. | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Missing Required Fields: Teacher attempts to submit without rating or description. System displays validation error (MSG-004) and blocks submission. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-139, BR-140, BR-142, BR-143, BR-145 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-123 Submit Meal Feedback Use Cases Specification

#### View List Meal Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-161 - View List Meal Feedback | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration, Service Caterers | Secondary Actors | None |
| Trigger | Primary actor requests to view list meal feedback. | | |
| Description | Enables the Service Caterers to safely query, view List Meal Feedback records. | | |
| Preconditions | The service caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The view list meal feedback data is successfully retrieved and displayed to the service caterers. | | |
| Normal Flow | 1. Service Caterers navigates to the Meal Feedback section.  2. System queries the database and retrieves all meal feedback related to the meals they provided.  3. System displays the list of meal feedback sorted by date, including star ratings, quality concern tags, and comments.  4. Service Caterers can search feedback by keyword or apply filters (e.g., by date, rating, or class). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-140, BR-142 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ View List Meal Feedback Use Cases Specification

#### View Meal Feedback Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-162 - View Meal Feedback Detail | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration, Service Caterers | Secondary Actors | None |
| Trigger | Primary actor requests to view meal feedback detail. | | |
| Description | Enables the Service Caterers and School Administration to view the detailed information of a specific meal feedback, including comments, star ratings, and related meal information. | | |
| Preconditions | The Service Caterers and School Administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view meal feedback detail data is successfully retrieved and displayed to the service caterers. | | |
| Normal Flow | 1. Service Caterers and School Administration navigates to the Meal Feedback section and selects a specific feedback record.  2. System queries the database and retrieves the full feedback details.  3. System displays the meal feedback details (Star rating, Quality concern tags, Comments) while hiding the identity of the submitter to protect privacy. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-145 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ View Meal Feedback Detail Use Cases Specification

#### Respond Meal Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-163 - Respond Meal Feedback | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Primary actor requests to respond to a meal feedback. | | |
| Description | Enables the Service Caterers to submit a response to a parent's or teacher's meal feedback, addressing their concerns or acknowledging their feedback. | | |
| Preconditions | The service caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The response is saved and the original submitter (parent/teacher) can view the response. | | |
| Normal Flow | 1. Service Caterers views a specific meal feedback detail.  2. Service Caterers clicks 'Respond'.  3. System displays a response form.  4. Service Caterers inputs their response content and clicks 'Submit'.  5. System validates the input.  6. System saves the response and links it to the original feedback. If it was a negative review, the system logs that the response requirement has been fulfilled. | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Missing Required Fields: Service Caterers attempts to submit an empty response. System displays error and blocks submission.  EX2 - Late Response to Negative Review: If a response to a negative review is submitted after 48 hours, system logs the delay | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-143 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ Respond Meal Feedback Use Cases Specification

#### View Meal Feedback Dashboard

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-164 - View Meal Feedback Dashboard | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor navigates to the Meal Feedback Dashboard. | | |
| Description | Enables the School Administration to view aggregated and anonymized feedback charts, statistics. | | |
| Preconditions | The School Administration is authenticated and authorized to access this feature. | | |
| Postconditions | The aggregated feedback charts are displayed to the user. | | |
| Normal Flow | 1. School Administration navigates to the Feedback Dashboard section.  2. System aggregates meal feedback results anonymously to protect parent and student privacy.  3. System displays the weekly compiled aggregated feedback charts, average ratings, and trends. | | |
| Alternative Flows | None. | | |
| Exceptions | None. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-141, BR-145 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ View Meal Feedback Dashboard Use Cases Specification

#### Update Announcement

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-089 - Update Announcement | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to update announcement. | | |
| Description | Enables the School Admin to update details of an existing announcement (e.g., Title, Content, Publication Status), log revision history, and notify affected users if published. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The announcement is updated in the database. Revision history is logged (BR-144). Affected users are notified. | | |
| Normal Flow | 1. School Admin accesses the Announcement Management panel and searches for the target Announcement.  2. System displays an edit form with announcement details.  3. School Admin updates announcement details (Title, Content) and/or Publication Status (Draft/Published), then clicks 'Save'.  4. System validates that the required fields are not empty.  5. System logs the changes, creating a revision history entry with timestamps and School Admin ID (BR-144).  6. System saves the updated announcement (MSG-074). If Publication Status is set to Published, system dispatches update notifications/emails to the users have associated with their school. | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Announcement Not Found: The selected announcement does not exist. The system displays "Announcement Not Found" message and returns the School Admin to the announcement list.  EX2 - Missing Required Fields: The School Admin attempts to save an announcement with one or more required fields missing or containing invalid values. The system displays the corresponding validation message(s) and prevents the announcement from being saved. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-144 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-124 Update Announcement Use Cases Specification

#### View Announcement Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-105 - View Announcement Detail | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | User, School Administration | Secondary Actors | None |
| Trigger | User, School Administration requests to view announcement detail. | | |
| Description | Enables the User, School Administration to view the full content of an announcement. | | |
| Preconditions | The user or school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view announcement detail data is successfully retrieved and displayed to the User or School Administration. | | |
| Normal Flow | 1. Actor navigates to the Announcement List page.  2. Actor selects an announcement.  3. System retrieves the announcement details.  4. System displays: Title, Content, Target Group, Publication Status, Author, Publication Dates  5. If the actor role is “School Administration”, the system additionally displays the complete revision history log (BR-144). | | |
| Alternative Flows | None. | | |
| Exceptions | None. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | None. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-125 View Announcement Detail Use Cases Specification

#### View List Announcement

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-125 - View List Announcement | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | User, School Administration | Secondary Actors | None |
| Trigger | User, School Administration request to view list announcement. | | |
| Description | Enables the User, School Administration to safely query, view the details of List Announcement records. | | |
| Preconditions | The User, School Administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view list announcement data is successfully retrieved and displayed to the User, School Administration. | | |
| Normal Flow | 1. Actor navigates to the Announcement List page.  2. System retrieves all announcements accessible to the actor.  3. System displays the announcement list sorted by creation date.  4. Actor may search by keyword or apply filters.  5. System refreshes and displays the matching results.  6. If the actor role is School Administration, the actor may export the announcement list.  7. System generates and downloads the export file (CSV/PDF). | | |
| Alternative Flows | None. | | |
| Exceptions | None. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-126 View List Announcement Use Cases Specification

