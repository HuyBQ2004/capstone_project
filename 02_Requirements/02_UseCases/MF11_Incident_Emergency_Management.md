### Incident & Emergency Management

#### Approval / Reject Corrective Action

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-005 - Approval / Reject Corrective Action | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Medical Staff submits a proposed Corrective and Preventive Action (CAPA) plan and requests School Admin review, prior to Service Caterers implementation. | | |
| Description | Enables the School Admin to review, and either approve or reject, a proposed Corrective and Preventive Action (CAPA) plan submitted by the Medical Staff, before the plan is sent to the Service Caterers for implementation (BR-216). | | |
| Preconditions | The school admin is authenticated and authorized. The Medical Staff has drafted and submitted a CAPA plan for review (BR-216). | | |
| Postconditions | The CAPA status is updated (Approved for Implementation or Draft). If approved, the CAPA is sent to the Service Caterers to view and accept (BR-216). If rejected, the Medical Staff is notified to revise and resubmit the plan. | | |
| Normal Flow | 1. School Admin navigates to the CAPA Pending Approval dashboard.  2. System displays a list of proposed CAPA plans awaiting review.  3. School Admin selects a CAPA record to review.  4. System displays the detailed incident description, the proposed root cause, and the proposed corrective actions.  5. School Admin verifies that the proposed actions reasonably address the root cause.  6. School Admin selects 'Approve' and confirms.  7. System updates the CAPA status to 'Approved for Implementation' (MSG-043), notifies the Service Caterers to view and accept the plan, and notifies the Medical Staff. | | |
| Alternative Flows | AF1 - Reject CAPA Plan: At step 5, School Admin finds the proposed plan unsatisfactory. School Admin clicks 'Reject', enters rejection reasons, and confirms. System updates CAPA status to 'Draft', logs the rejection, and notifies the Medical Staff to revise and resubmit the plan (BR-216). | | |
| Exceptions | EX1 - Missing Review Notes on Rejection: School Admin attempts to reject without entering a rejection reason. System displays validation error and blocks the action. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-153, BR-216 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-128 Approval / Reject Corrective Action Use Cases Specification

#### Assign Incident Severity

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-009 - Assign Incident Severity | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff opens an active food safety incident and selects 'Assign Severity Level'. | | |
| Description | Enables the Medical Staff to formally assess and assign a severity classification to a food safety incident based on reported symptoms, number of affected students, and potential health risk level. | | |
| Preconditions | Medical staff is authenticated and authorized. An active food safety incident record exists and is under Medical Staff review. | | |
| Postconditions | Incident severity assigned and recorded with initial action plan. If High, emergency alerts immediately sent to school board, caterer, and health department (BR-148). Suspected batches quarantined (BR-149). | | |
| Normal Flow | 1. Medical Staff opens the active incident record. 2. System displays reported symptoms, affected student count, suspected dish information, and linked traceability evidence. 3. Medical Staff applies severity criteria (BR-147): Low (mild, no clinic), Medium (clinic visit required), High (hospitalization/outbreak). 4. Medical Staff selects severity level and documents initial action plan. 5. System saves severity assignment, updates incident record, and dispatches notifications per severity level. | | |
| Alternative Flows | AF1 - High Severity Emergency: Severity = High. System immediately sends emergency alerts to school board, caterer, and local health department (BR-148) and activates emergency response protocol without waiting for administrator confirmation. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-129 Assign Incident Severity Use Cases Specification

#### Close Incident

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-013 - Close Incident | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration reviews a completed incident record and determines all closure criteria are met. | | |
| Description | Enables the School Administration to formally close a food safety incident after verifying all CAPAs are completed and all affected students have fully recovered. | | |
| Preconditions | School administration is authenticated and authorized. Incident is in 'Active' or 'Under Investigation' status. All CAPAs verified as completed. All affected students confirmed as fully recovered (BR-155). | | |
| Postconditions | Incident status changed to 'Closed'. Final incident report generated (BR-154). Closure logged with electronic signature. Quarantined batches reviewed for release or disposal. | | |
| Normal Flow | 1. School Administration opens the incident record and reviews the investigation summary and CAPA completion status. 2. System displays CAPA implementation verification and student recovery confirmation status. 3. School Administration verifies all CAPAs are implemented and verified, and all affected students are fully recovered (BR-155). 4. School Administration selects 'Close Incident' and enters a closing statement. 5. System changes status to 'Closed', generates the final report (BR-154), logs with electronic signature, and notifies all relevant stakeholders. | | |
| Alternative Flows | AF1 - Incomplete CAPAs: Any CAPA is still open or unverified. System blocks incident closure and highlights all pending actions that must be completed. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-130 Close Incident Use Cases Specification

#### Export Incident Reports

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-031 - Export Incident Reports | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration, Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to export incident reports. | | |
| Description | Enables the School Admin and Medical Staff to export compiled incident reports, symptom logs, and CAPA histories to Excel or PDF format for regulatory inspection or internal audit. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The selected incident reports are exported to a downloadable file (PDF/Excel) and logged in the audit trail. | | |
| Normal Flow | 1. School Admin navigates to the Incident Reports export panel.  2. System displays filter options (Date Range, Severity Level, Incident Status, Suspected Service Caterers).  3. School Admin applies filters and clicks 'Generate Preview'.  4. System compiles and displays a preview of matching incident records.  5. School Admin selects the export format (PDF or Excel) and clicks 'Export'.  6. System generates the file containing detailed logs (Incident ID, symptoms, affected counts, root cause, CAPA status) (BR-154), triggers the download (MSG-045), and logs the export transaction. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-131 Export Incident Reports Use Cases Specification

#### Generate Incident Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-033 - Generate Incident Report | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to generate incident report. | | |
| Description | Enables the Medical Staff to compile clinical symptoms, student counts, lab results, batch traceability, and root cause findings into a formal Incident Investigation Report. | | |
| Preconditions | Medical Staff is authenticated and authorized. An active incident investigation is underway. | | |
| Postconditions | A formal incident investigation report is generated in the system, saved, and made available for School Admin and external health authorities. | | |
| Normal Flow | 1. Medical Staff opens the target active incident record.  2. Medical Staff clicks 'Generate Incident Report'.  3. System auto-populates report fields: affected student list, reported symptoms, date of onset, suspected meals/dishes, and linked ingredient batches (BR-154).  4. Medical Staff inputs: clinical notes, laboratory sample test results (e.g. food poisoning pathogen details), and final root cause determinations.  5. Medical Staff clicks 'Save and Generate'.  6. System saves the report (MSG-046), changes incident status to 'Investigation Completed', and dispatches the PDF report to the School Admin. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-132 Generate Incident Report Use Cases Specification

#### Manage Corrective Actions

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-038 - Manage Corrective Actions | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers receives a CAPA plan that has been approved by the School Admin (BR-216). | | |
| Description | Enables the Service Caterers to view an approved Corrective and Preventive Action (CAPA) plan, accept it, update implementation progress, log evidence of completion, and submit the results for Medical Staff validation. | | |
| Preconditions | Service Caterers is authenticated and authorized. A CAPA plan has been approved by the School Admin and sent to the Service Caterers (BR-216). | | |
| Postconditions | CAPA status is updated to 'In Progress' upon acceptance, and to 'Submitted Evidence' once implementation results are submitted for Medical Staff validation. | | |
| Normal Flow | 1. Service Caterers accesses the Corrective Actions management panel.  2. System displays CAPA plans approved by the School Admin and awaiting acceptance.  3. Service Caterers reviews the plan and clicks 'Accept', moving the CAPA to 'In Progress'.  4. System displays the detailed actions required and root cause.  5. Service Caterers updates the implementation log: actions taken, date completed, and preventative measures implemented.  6. Service Caterers uploads live-captured evidence photos and clicks 'Submit for Review'.  7. System updates status to 'Submitted Evidence', records the submission timestamp, and notifies the Medical Staff. | | |
| Alternative Flows | None. | | |
| Exceptions | None. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-152, BR-216 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-133 Manage Corrective Actions Use Cases Specification

#### 3.2.11.7. Manage Food Safety Incidents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-041 - Manage Food Safety Incidents | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff receives an incident notification and opens the active incident record to begin investigation. | | |
| Description | Enables the Medical Staff to manage the complete food safety incident investigation lifecycle: symptom assessment, severity classification, evidence collection, root cause analysis, and CAPA oversight. | | |
| Preconditions | Medical staff is authenticated and authorized. An active food safety incident record exists and has been assigned to Medical Staff. | | |
| Postconditions | Incident severity classified. Investigation evidence compiled. Root cause analyzed. CAPAs defined and submitted for School Admin approval (BR-216). Incident investigation report generated (BR-154). | | |
| Normal Flow | 1. Medical Staff opens the active incident record from the notification dashboard. 2. System displays symptoms, affected student count, suspected dishes, food sample records, and ingredient batch traceability. 3. Medical Staff assesses symptoms and classifies severity: Low, Medium, or High (BR-147). 4. Medical Staff documents root cause findings and defines corrective and preventive actions (CAPA). 5. System compiles investigation report (BR-154), sends the CAPA plan to School Admin for approval (BR-216), and logs all actions. | | |
| Alternative Flows | AF1 - High Severity Escalation: Medical Staff classifies as High. System triggers emergency protocol and notifies school board and health department (BR-148). AF2 - Additional Lab Testing Required: Insufficient evidence. Medical Staff initiates laboratory sample retrieval (UC-078) and documents additional testing requirements. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155, BR-216 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-134 Manage Food Safety Incidents Use Cases Specification

#### 3.2.11.8. Record Corrective Actions Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-168 - Record Corrective Actions Report | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to record corrective actions report. | | |
| Description | Enables the Medical Staff to record independent validation notes of the implemented CAPAs and certify clinical recovery of all affected students prior to incident closure. | | |
| Preconditions | Medical Staff is authenticated and authorized. Service Caterers has submitted CAPA implementation evidence (UC-082, BR-216). | | |
| Postconditions | Validation report recorded in the incident record. CAPA status updated to 'Completed' if satisfactory. Eligible for final closure (BR-155). | | |
| Normal Flow | 1. Medical Staff navigates to the Incident Validation section.  2. System displays a list of incidents with submitted CAPA evidence awaiting medical validation.  3. Medical Staff selects an incident record.  4. System displays the incident log, the approved CAPA plan, the submitted implementation evidence, and the student clinical tracking log.  5. Medical Staff inputs: verification comments on physical kitchen corrections, and updates student health status to confirm 100% recovery (BR-155).  6. Medical Staff clicks 'Submit Validation'.  7. System saves the validation report (MSG-046), attaches it to the incident file, updates the CAPA status to 'Completed' (BR-216), and updates the incident status to 'Validated - Ready for Closure'. | | |
| Alternative Flows | AF1 - Implementation Not Satisfactory: At step 5, Medical Staff finds the implementation does not adequately address the root cause. Medical Staff clicks 'Request Rework', enters a mandatory reason, and confirms. System reverts the CAPA status to 'In Progress' using the same approved plan (no re-approval by School Admin required) and notifies the Service Caterers. | | |
| Exceptions | EX1 - Active Symptoms Remaining: Medical Staff attempts to submit validation while student health records show one or more students still have active food poisoning symptoms. System blocks validation and displays MSG-004 (All students must be recovered, BR-155). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-153, BR-155, BR-216 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-135 Record Corrective Actions Report Use Cases Specification

#### 3.2.11.9. Report Food Safety Concern

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-073 - Report Food Safety Concern | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent navigates to 'Report Food Safety Concern' and submits a concern about their child's meal experience. | | |
| Description | Enables the Parent to formally report a food safety concern related to their child's meal, triggering Medical Staff review and potential incident investigation escalation. | | |
| Preconditions | Parent is authenticated and authorized and linked to at least one active student. | | |
| Postconditions | Food safety concern logged and associated with parent's child. Medical Staff notified. If >= 3 matching symptoms in 24h (BR-146), automatic incident investigation triggered. | | |
| Normal Flow | 1. Parent navigates to 'Report Food Safety Concern'. 2. System displays report form with symptom description, meal date, and suspected dish fields. 3. Parent enters: reported symptoms, meal date, and suspected dish. 4. System validates required fields and checks against automatic incident trigger criteria (BR-146). 5. System saves concern report, notifies Medical Staff, and if trigger threshold met (BR-146), automatically creates a food safety incident and quarantines suspected batches (BR-149). | | |
| Alternative Flows | AF1 - Incident Auto-Triggered: Parent's report contributes to >= 3 matching student symptom threshold (BR-146). System automatically creates incident record (MSG-048), quarantines suspected batches, and notifies School Administration. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-136 Report Food Safety Concern Use Cases Specification

#### 3.2.11.10. Report Food Safety Incident

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-074 - Report Food Safety Incident | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Teacher or other authorized actor observes a suspected food safety event, or system automatically detects that 3 or more students report matching symptoms within a 24-hour window (per BR-146). | | |
| Description | Enables the Teacher to formally report a suspected food safety incident, triggering an automated investigation including traceability map generation, batch quarantine, and stakeholder notification. | | |
| Preconditions | Teacher is authenticated and authorized. At least one student has reported food-related symptoms or teacher has directly observed a suspected food safety event. | | |
| Postconditions | Incident record created with unique Incident ID (MSG-048). Medical Staff and School Administration notified. If High severity, school board and health department alerted (BR-148). Suspected batches quarantined (BR-149). Traceability map generated (BR-151). | | |
| Normal Flow | 1. Teacher navigates to 'Report Food Safety Incident'. 2. System displays the incident report form. 3. Teacher enters: observed symptoms, number of affected students, suspected dishes, and ingestion date (BR-154). 4. System validates all required fields are complete. 5. System creates incident record (MSG-048), automatically quarantines suspected batches (BR-149), generates ingredient traceability map within 4 hours (BR-151), and notifies Medical Staff and School Administration. | | |
| Alternative Flows | AF1 - High Severity Auto-Escalation: Incident classified as High severity (hospitalization or outbreak). System immediately sends emergency alerts to school board and local health department (BR-148) without waiting for further confirmation. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-137 Report Food Safety Incident Use Cases Specification

#### 3.2.11.11. Submit Corrective Action Results

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-082 - Submit Corrective Action Results | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to submit corrective action results. | | |
| Description | Enables the Service Caterers to upload final verification data (laboratory clearance, sanitization certificates, live-captured evidence photos, or health department clearance) and formally submit corrective action results for Medical Staff validation (BR-216). | | |
| Preconditions | Service Caterers is authenticated and authorized. Service Caterers has accepted an approved CAPA plan and completed implementation (BR-216). | | |
| Postconditions | Corrective Action Results are submitted, with timestamp recorded, and a review notification is dispatched to the Medical Staff for validation. | | |
| Normal Flow | 1. Service Caterers opens the target CAPA worksheet.  2. Service Caterers inputs completion notes and uploads required evidence documents (e.g. laboratory disinfection swab clearance reports, live-captured photos).  3. Service Caterers uploads the Health Department Clearance Certificate (BR-153) if the kitchen was shut down.  4. Service Caterers clicks 'Submit Results'.  5. System validates that the required file uploads are attached.  6. System records the submission timestamp, saves the results (MSG-043), and triggers a notification to the Medical Staff for validation (BR-216). | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Missing Health Department Clearance: The incident was High severity requiring kitchen closure, but the Service Caterers attempts to submit results without uploading the mandatory Health Department Clearance Certificate. System blocks submission and displays validation error (MSG-004) pointing to the missing clearance file (BR-153). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-153, BR-155, BR-216 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-138 Submit Corrective Action Results Use Cases Specification

#### 3.2.11.12. View Food Poisoning Incident List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-114 - View Food Poisoning Incident List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to view food poisoning incident list. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Food Poisoning Incident List records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view food poisoning incident list data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Food Poisoning Incident List section in the system portal.  2. System retrieves and displays a list of all food poisoning incidents.  3. School Administration can search by keyword or apply filters (e.g., Severity Level, Status).  4. System updates the displayed list accordingly. | | |
| Alternative Flows | AF1 - Export Data: School Administration clicks 'Export'. System generates a file (CSV/PDF) of the filtered list and downloads it to the user's device. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-139 View Food Poisoning Incident List Use Cases Specification

#### 3.2.11.13. View Food Safety Incident Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-116 - View Food Safety Incident Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to view food safety incident detail. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Food Safety Incident Detail records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view food safety incident detail data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Food Safety Incident Detail section and selects a specific incident.  2. System retrieves and displays the full details of the incident, including symptoms, affected students, and investigation status. | | |
| Alternative Flows | AF1 - Export Data: School Administration clicks 'Export'. System generates a file (CSV/PDF) of the incident details and downloads it to the user's device. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-140 View Food Safety Incident Detail Use Cases Specification

#### 3.2.11.14. View Incident Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-120 - View Incident Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to view incident information. | | |
| Description | Enables the Service Caterers to safely query, view, or export the details of Incident Information records. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The view incident information data is successfully retrieved and displayed to the Service Caterers. | | |
| Normal Flow | 1. Service Caterers navigate to the Incident Information section and select an assigned incident.  2. System retrieves and displays the incident information relevant to the Service Caterers. | | |
| Alternative Flows | AF1 - Export Data: Service Caterers click 'Export'. System generates a file (CSV/PDF) of the incident information and downloads it to the user's device. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-141 View Incident Information Use Cases Specification

#### 3.2.11.15. View Incident Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-121 - View Incident Report | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view incident report. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Incident Report records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view incident report data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Incident Report section and selects a specific report.  2. System retrieves and displays the full incident report, including clinical notes, lab results, and root cause analysis. | | |
| Alternative Flows | AF1 - Export Data: School Administration clicks 'Export'. System generates a file (CSV/PDF) of the incident report and downloads it to the user's device. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-142 View Incident Report Use Cases Specification

#### Record Staff Violation

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-179 - Record Staff Violation | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | Medical Staff | Secondary Actors | Service Caterers |
| Trigger | Medical Staff or Service Caterers observes a staff conduct violation (skipping mandatory procedure, personal hygiene violation, manual-override abuse, unauthorized action) not tied to a specific inspection step. | | |
| Description | Enables Medical Staff or Service Caterers to record a Staff Violation independently of the three-step inspection workflow, identifying the staff member by free-text name since kitchen/catering personnel do not hold system user accounts. | | |
| Preconditions | The user is authenticated and authorized. | | |
| Postconditions | A staff violation record is created with staff name, violation type, description, evidence, severity, and status Reported. If the staff name approximately matches another Medium+ severity violation within the school-configured lookback window (default 90 days), a repeat-violation warning is raised for manual verification, without auto-escalation. | | |
| Normal Flow | 1. Medical Staff or Service Caterers navigates to Violation Management and selects 'Record Staff Violation'.  2. System displays the violation form (staff name, staff position [optional], violation type, description, evidence upload, severity level, occurred date/time).  3. User fills in the required fields and submits.  4. System checks for a possible repeat violation by matching staff name (exact or approximate) against existing Medium+ severity records within the default 90-day lookback window; if found, displays MSG-088 as a warning without blocking submission.  5. System saves the staff violation record with status Reported, logs the action to the immutable audit trail, and displays MSG-086. | | |
| Alternative Flows | AF1 - Weekly Override Abuse Auto-Record: System automatically compiles a weekly staff violation record with violation type OVERRIDE_ABUSE summarizing detected manual-override abuse cases (BR-190), visible to School Administration via UC-183. | | |
| Exceptions | MSG-004, MSG-005, MSG-085, MSG-088. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-184, BR-187, BR-190 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-160 Record Staff Violation Use Cases Specification

#### Record Violation Resolution

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-180 - Record Violation Resolution | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | Medical Staff | Secondary Actors | School Administration |
| Trigger | Medical Staff proposes a resolution for an existing violation record (process or staff). | | |
| Description | Enables Medical Staff to propose the resolution and disciplinary action for a violation record, running in parallel with — not replacing — the existing Corrective Action Request/CAPA workflow, and route it to School Administration for approval when required. | | |
| Preconditions | The user is authenticated and authorized. A process violation (BR-183) or staff violation (BR-184) record exists. | | |
| Postconditions | A violation resolution record is created, linked to its source violation, with proposed action, handler, and disciplinary level (for staff violations). Status is set to Pending Approval for Medium/High severity, or directly closed for Low severity per BR-186. | | |
| Normal Flow | 1. Medical Staff opens the target violation record and selects 'Record Resolution'.  2. System displays the resolution form (action taken, disciplinary level if applicable: Verbal Warning / Written Warning / Suspension / Termination Recommendation).  3. Medical Staff fills in the proposed action and submits.  4. If violation severity is Low, system allows Medical Staff to directly approve and close the resolution without further routing (BR-186).  5. If severity is Medium or High, system sets status to Pending Approval and routes to School Administration (UC-182).  6. System saves the violation resolution record, logs the action to the immutable audit trail, and displays MSG-087. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-085. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-185, BR-186 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-161 Record Violation Resolution Use Cases Specification

#### View Violation Registry

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-181 - View Violation Registry | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | Medical Staff |
| Trigger | Primary actor requests to view the consolidated violation registry. | | |
| Description | Enables the School Administration and Medical Staff to view a unified, filterable list of all violations — process, staff conduct, and partner-related — by actor, severity, and status. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | Violation registry data is retrieved and displayed. | | |
| Normal Flow | 1. School Administration or Medical Staff navigates to the Violation Registry section.  2. System queries and aggregates non-conformance records (with BR-183 fields) and staff violation records.  3. System displays the consolidated list (Type, Actor/Staff Name, Severity, Status, Reported Date).  4. Primary actor filters by type, severity, or status, or selects a record to view its resolution history. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-189 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-162 View Violation Registry Use Cases Specification

#### Approve/Reject Violation Resolution

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-182 - Approve/Reject Violation Resolution | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration reviews a violation resolution pending approval (Medium/High severity). | | |
| Description | Enables the School Administration to approve or reject a proposed violation resolution record, applying the disciplinary action upon approval. | | |
| Preconditions | The school administration is authenticated and authorized. A violation resolution record exists with status Pending Approval. | | |
| Postconditions | Violation resolution record status is updated to Approved or Rejected. If approved, the disciplinary action is considered applied and the source violation is closed. If rejected, the record returns to Medical Staff for revision. | | |
| Normal Flow | 1. School Administration navigates to the Violation Registry and selects a resolution pending approval.  2. System displays the violation details and proposed resolution/disciplinary level.  3. School Administration reviews and selects Approve or Reject.  4. If approved, system marks the resolution Approved, closes the source violation record, logs the action, and displays MSG-089.  5. If rejected, system requires a rejection reason, sets status back for revision, and notifies Medical Staff. | | |
| Alternative Flows | AF1 - Reject with Reason: School Administration selects Reject. System requires a mandatory reason before saving. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-186 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-163 Approve/Reject Violation Resolution Use Cases Specification

#### View Weekly Override Abuse Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-183 - View Weekly Override Abuse Report | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view the weekly manual-override abuse report. | | |
| Description | Enables the School Administration to view the automatically compiled weekly report summarizing manual-override abuse cases (staff violation records with violation type OVERRIDE_ABUSE), fulfilling the reporting commitment made in ADR-002. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Weekly override abuse report data is retrieved and displayed. | | |
| Normal Flow | 1. School Administration navigates to the Weekly Override Abuse Report section.  2. System retrieves all staff violation records with violation type OVERRIDE_ABUSE for the selected week, auto-compiled per BR-190.  3. System displays the report (Staff Name, Override Count, Context, Severity).  4. School Administration can export the report or drill down into individual records via the Violation Registry (UC-181). | | |
| Alternative Flows | AF1 - Export Data: School Administration clicks 'Export'. System generates a file (CSV/PDF) of the report and downloads it. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Low | | |
| Frequency of Use | Weekly | | |
| Business Rules | BR-190 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-164 View Weekly Override Abuse Report Use Cases Specification

