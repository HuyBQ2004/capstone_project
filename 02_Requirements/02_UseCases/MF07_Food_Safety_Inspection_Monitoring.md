### Food Safety Inspection & Monitoring

#### Conduct Food Safety Inspection

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-014 - Conduct Food Safety Inspection | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to conduct food safety inspection. | | |
| Description | Enables the Medical Staff to perform regular safety and sanitation audits of the school kitchen, verifying hygiene conditions, staff medical clearance certificates, pest control records, and submitting the audit checklist score. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. Checklist forms and templates are loaded. | | |
| Postconditions | Kitchen audit log saved, score calculated, compliance certificates verified. If the score is below 60%, a kitchen quarantine is triggered (BR-105) and emergency alerts are sent. | | |
| Normal Flow | 1. Medical Staff navigates to the Food Safety Inspection section and selects 'New Inspection'.  2. System displays the Kitchen Safety and Sanitation Checklist (BR-101, BR-104).  3. Medical Staff physically inspects the kitchen premises and logs compliance for each item: staff uniform compliance (glove, mask, hairnet), workspace cleanliness, and storage conditions.  4. Medical Staff verifies active quarterly pest control certificate (BR-102) and kitchen staff annual medical clearance certificates (BR-103) within the system.  5. Medical Staff uploads photographic evidence of kitchen hygiene conditions.  6. Medical Staff submits the checklist.  7. System calculates the audit score. If the score is >= 60%, system saves the audit report as 'Passed' and logs the result. | | |
| Alternative Flows | AF1 - Critical Safety Violations / Kitchen Quarantine:  1. At step 7, the calculated score is below 60% (BR-105).  2. System flags the audit as 'Failed', locks kitchen operational status, triggers an immediate kitchen quarantine, and blocks all meal distributions.  3. System displays error message MSG-024 (Kitchen quarantine is active).  4. System dispatches emergency alerts to the School Administration, Service Caterers, and School Board. | | |
| Exceptions | MSG-004, MSG-005, MSG-024 | | |
| Priority | High | | |
| Frequency of Use | Daily / Weekly | | |
| Business Rules | BR-101, BR-102, BR-103, BR-104, BR-105, BR-110 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-93 Conduct Food Safety Inspection Use Cases Specification

#### Daily Food Safety Monitoring

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-027 - Daily Food Safety Monitoring | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to monitor daily food safety logs. | | |
| Description | Enables the School Administration to monitor the daily three-step food safety inspection progress and review daily hygiene checklist scores submitted by the Service Caterers. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The daily food safety monitoring transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. School Administration navigates to the Daily Food Safety Monitoring dashboard.  2. System displays the real-time status of today's meal production batches and their three-step inspection steps (Step 1, Step 2, Step 3) (BR-091).  3. System displays the daily kitchen hygiene checklist score submitted by the Service Caterers (BR-100, BR-104).  4. School Administration reviews the compliance scores and verification photos.  5. School Administration signs off on the daily safety status, and the system logs the monitoring review event. | | |
| Alternative Flows | AF1 - Pending or Failed Inspection Alert:  1. System detects a failed step (BR-097) or an overdue inspection. 2. System highlights the warning on the dashboard and displays the active corrective action request.  3. School Administration selects the alert to view non-conformance details and contacts the Service Caterers. | | |
| Exceptions | MSG-004, MSG-005, MSG-024 | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-091, BR-097, BR-100, BR-104, BR-105 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-94 Daily Food Safety Monitoring Use Cases Specification

#### Manage Three-Step Food Inspection

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-052 - Manage Three-Step Food Inspection | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers initiates a food safety inspection session for an active meal production batch. | | |
| Description | Enables the Service Caterers to conduct the mandatory three-step food safety inspection in compliance with Decision 1246/QĐ-BYT, recording checklist results, temperature measurements, GPS, timestamps, and photographic evidence for Step 1 (Sample No. 1), Step 2 (Sample No. 2), and Step 3 (Sample No. 3). | | |
| Preconditions | Service Caterers is authenticated and authorized. Meal production session is active. GPS enabled on device. | | |
| Postconditions | All three inspection steps completed and co-signed digitally (BR-099). Meals approved for serving if all steps pass. Corrective action triggered on failure (BR-097). | | |
| Normal Flow | 1. Service Caterers initiates a Three-Step Food Safety Inspection session.  2. Step 1 (Pre-processing inspection - Sample No. 1):System retrieves Step 1 data from UC-064. Service Caterers reviews and confirms that the raw ingredients and dry products pass inspection checklists, veterinary certificates, and sensory checks.  3. Step 2 (Pre-processing inspection - Sample No. 2):Service Caterers fills out the Sample 2 form: records meal slot, dish names, main ingredients/quantities, portion count, preprocessing and cooking end times, and checks sanitary conditions (staff, equipment, prep area) and sensory characteristics of cooking food. Verifies core temperature is >= 75°C for 15 seconds (BR-090). System locks Step 3 until Step 2 is marked 'Passed'.  4. Step 3 (Pre-processing inspection - Sample No. 3):Service Caterers fills out the Sample 3 form: records portion count, portion division end time, meal start time, description of containers/utensils (covered, hygienic), holding temperature >= 60°C (BR-091), and sensory check before eating.  5. System generates the consolidated digital three-step inspection record, prompts both the Catering Manager and the School Representative to sign digitally (BR-099), marks the meal batch as approved for serving, and displays MSG-043. | | |
| Alternative Flows | AF1 - Inspection Step Failure: Any step fails inspection. System blocks meal release (BR-097), logs non-conformance, creates corrective action request, displays MSG-056. Re-inspection required after corrective action (BR-098). | | |
| Exceptions | MSG-004, MSG-005, MSG-016, MSG-017, MSG-056 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-090, BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and GPS-enabled device. | | |

Table 3-95 Manage Three-Step Food Inspection Use Cases Specification

#### Monitor Food Safety

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-054 - Monitor Food Safety | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff requests to monitor general food safety metrics. | | |
| Description | Enables the Medical Staff to view the food safety dashboard, track pending compliance tasks, monitor temperature logs, review pest control schedules, and track active corrective action requests. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Food safety dashboard metrics retrieved and displayed. | | |
| Normal Flow | 1. Medical Staff navigates to the Food Safety Monitoring dashboard in the system portal.  2. System retrieves and displays real-time widgets: today's three-step inspection status for all meal batches (BR-091), storage temperature logs for ingredients and food sample retention refrigerators (BR-089, BR-110), compliance checklist scores (daily hygiene, weekly and monthly audits) (BR-100, BR-101, BR-110), active corrective action requests and re-inspection statuses (BR-097, BR-098), and refrigerator alarm alerts  3. Medical Staff reviews the dashboard, selects any active alert widget, and views the detailed non-compliance record. | | |
| Alternative Flows | AF1 - Re-inspection Review:  1. Medical Staff selects a batch that has failed inspection and undergoes re-inspection (BR-098).  2. Medical Staff reviews the uploaded corrective action results and schedules a physical follow-up inspection. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | Daily / Weekly | | |
| Business Rules | BR-089, BR-091, BR-097, BR-098, BR-101, BR-110 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-96 Monitor Food Safety Use Cases Specification

#### Conduct Three-Step Food Inspection

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-062 - Record Ingredients Before Processing | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers prepares to issue ingredients for a meal production session and starts the Step 1 inspection. | | |
| Description | Enables the Service Caterers to conduct and record the mandatory three-step food safety inspection process in compliance with Decision 1246/QĐ-BYT. This includes recording sensory checks, temperature logs, timestamps, GPS locations, and photographic evidence for Step 1 (Sample No 1 - Before Processing), Step 2 (Sample No 2 - In Processing), and Step 3 (Sample No 3 - Before Serving). | | |
| Preconditions | - Service Caterers is authenticated and authorized.  - Meal production session for the day is active and scheduled.  - Device GPS is enabled. | | |
| Postconditions | - All three inspection steps are completed, validated, and saved in the database.  - The consolidated inspection log is digitally signed by both the Catering Manager and School Representative.  - Meal batch is approved for serving. If any step fails, corrective actions are triggered. | | |
| Normal Flow | 1. Initiate Inspection: Service Caterers accesses the active meal production batch and initiates a new Three-Step Inspection session.  2. Conduct Step 1 (Pre-processing inspection - Sample No. 1):  - Service Caterers scans barcodes of raw/dry ingredients to issue them from stock.  - For fresh/frozen food: logs delivery time, weight, supplier info, veterinary/quarantine certificates (BR-092), and sensory checks (color, smell, appearance).  - For dry/packaged food: logs manufacturer, expiry date, storage conditions, and sensory checks.  - System validates that ingredients are not expired (BR-092) and complies with FEFO (BR-076).  - Service Caterers uploads photos of ingredients and certificates, then submits Step 1.  3. Conduct Step 2 (In-process inspection - Form No. 2):  - During cooking, Service Caterers completes the Sample No 2 checklist: records dish names, preprocessing and cooking end times, and verifies sanitary conditions (staff hygiene, equipment cleaniness, prep area).  - Service Caterers measures and logs the core cooking temperature (verifying core temp >= 75°C for 15 seconds per BR-090).  - System records GPS verification and timestamp.  4. Conduct Step 3 (Pre-meal inspection - Form No. 3):  - Before serving, Service Caterers completes the Sample No 3 checklist: records portion counts, portion division time, meal start time, description of containers (clean, covered), and holding temperature (verifying holding temp >= 60°C per BR-091).  - Service Caterers uploads photos of the finished dishes.  5. Co-sign & Approve: System compiles the consolidated 3-step inspection log, automatically records final GPS coordinates, and prompts the Catering Manager and School Representative to sign digitally (BR-099). Once co-signed, the meal batch status changes to 'Approved for Serving'. | | |
| Alternative Flows | AF1 - Inspection Step Failure:  1. At any step, if a checklist item or temperature check fails (e.g., core temp < 75°C in Step 2, holding temp < 60°C in Step 3, or expired batches in Step 1).  2. System blocks the progress of the inspection, logs non-conformance, and flags the batch status as 'Inspection Failed'.  3. System automatically triggers a Corrective Action Request (UC-038) and prompts for a re-inspection once corrective actions are submitted (BR-097, BR-098). | | |
| Exceptions | - MSG-004 (Error): Session expired. Please log in again.  - MSG-005 (Error): Access denied: You do not have permission.  - MSG-015 (Error): GPS verification failed (out of range tolerance). | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-076, BR-079, BR-083, BR-090, BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-97 Conduct Three-Step Food Inspection Use Cases Specification

#### View Food Inspection Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-112 - View Food Inspection Records | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to view food inspection records. | | |
| Description | Enables the Medical Staff to safely query, view, or export the details of Food Inspection Records records. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | The view food inspection records data is successfully retrieved and displayed to the medical staff. | | |
| Normal Flow | 1. Medical Staff navigates to the Food Inspection Records section in the system portal.  2. System displays a list of food inspection records (including Step 1, Step 2, and Step 3 inspections) with date, status, and responsible staff.  3. Medical Staff can filter records by date range, inspection step, or menu.  4. Medical Staff selects a specific inspection record to view its details.  5. System displays the detailed inspection results, including evidence photos, timestamps, GPS locations, and staff notes.  6. Medical Staff reviews the records to ensure food safety compliance. | | |
| Alternative Flows | AF1 - No Records Found: If there are no inspection records for the selected criteria, the system displays a "No records found" message. | | |
| Exceptions | MSG-004, MSG-005, MSG-016, MSG-017 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-98 View Food Inspection Records Use Cases Specification

#### View Food Inspection Reports

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-113 - View Food Inspection Reports | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to view food inspection reports. | | |
| Description | Enables the School Administration to search, view, and export daily or weekly food inspection summary reports and checklist histories. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Food inspection reports compiled and displayed. | | |
| Normal Flow | 1. School Administration navigates to the Food Inspection Reports section.  2. System displays search filters (date range, meal session, Service Caterers).  3. School Administration selects filter criteria and clicks 'Generate Report'.  4. System compiles and displays a summary report showing total inspected batches, pass/fail rates, critical temperature deviations, and corrective action statuses.  5. School Administration reviews the report. | | |
| Alternative Flows | AF1 - Export PDF/Excel:  1. School Administration selects 'Export Report'.  2. System generates and downloads the summary report as a PDF or Excel sheet. | | |
| Exceptions | MSG-004, MSG-005, MSG-016, MSG-017 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-091, BR-097, BR-098 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-99 View Food Inspection Reports Use Cases Specification

#### View Food Safety Compliance Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-115 - View Food Safety Compliance Report | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff requests to view the food safety compliance report. | | |
| Description | Enables the Medical Staff to generate and view monthly or quarterly kitchen safety compliance reports, summarizing hygiene scores, pest control statuses, staff clearances, and temperature log deviations. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Food safety compliance report generated and displayed. | | |
| Normal Flow | 1. Medical Staff navigates to the Food Safety Compliance Report section.  2. System displays date range selectors and export format options. 3. Medical Staff selects a date range and clicks 'Generate Compliance Report'.  4. System aggregates data from: monthly safety and sanitation audits (BR-101), pest control status (BR-102), staff medical clearance validity (BR-103), daily hygiene scores (BR-100, BR-104), and refrigerator temperature alarms (BR-089).  5. System displays the compliance summary dashboard highlighting any overdue certificates or critical safety deviations. | | |
| Alternative Flows | AF1 - Export for Health Authority:  1. Medical Staff selects 'Export PDF'.  2. System compiles a formal compliance report matching the local health department requirements and downloads the PDF file. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-089, BR-100, BR-101, BR-102, BR-103, BR-104 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-100 View Food Safety Compliance Report Use Cases Specification

#### View Food Safety Reports

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-117 - View Food Safety Reports | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff requests to view general food safety reports. | | |
| Description | Enables the Medical Staff to safely query, view, or export general food safety records and consolidated inspection histories. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Food safety logs and reports retrieved and displayed. | | |
| Normal Flow | 1. Medical Staff navigates to the Food Safety Reports portal.  2. System displays options to view different report categories: Monthly Kitchen Audits (BR-101), Pest Control Log (BR-102), and Refrigerator Temperature Logs (BR-089, BR-110).  3. Medical Staff selects a category, sets filters (date range, refrigerator ID), and submits.  4. System retrieves and displays the matching logs and trends. | | |
| Alternative Flows | AF1 - Export Data:  1. Medical Staff clicks 'Export'.  2. System compiles and downloads the selected logs as an Excel sheet. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-089, BR-101, BR-102, BR-103, BR-110 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-101 View Food Safety Reports Use Cases Specification

#### View Inspection Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-124 - View Inspection Records | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to view their inspection records. | | |
| Description | Enables the Service Caterers to query, view, or export their own daily three-step food safety inspection logs and corrective action history. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | Service Caterers's inspection records retrieved and displayed. | | |
| Normal Flow | 1. Service Caterers navigates to the My Inspection Records portal.  2. System displays search filters (date range, dish name, status: Passed/Failed).  3. Service Caterers inputs filter criteria and submits.  4. System retrieves and displays only the inspection records registered by this Service Caterers.  5. Service Caterers selects a record to view detailed checklists, temperatures, photos, and digital co-signatures | | |
| Alternative Flows | AF1 - View Corrective Action History:  1. While viewing a failed inspection record, Service Caterers clicks 'View Corrective Action'.  2. System displays the related corrective action request, submission status, and re-inspection logs | | |
| Exceptions | MSG-004, MSG-005, MSG-016, MSG-017 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-091, BR-092, BR-093, BR-094, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-102 View Inspection Records Use Cases Specification

#### Record Process Violation

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-178 - Record Process Violation | | |
| Created By | SEP490-G8 Team | Date Created | 01/07/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Medical Staff |
| Trigger | A three-step food inspection step fails, or Medical Staff/Service Caterers detects a process/product non-conformance during an independent audit. | | |
| Description | Enables Service Caterers or Medical Staff to formalize a detected process/product non-conformance into a complete Violation Record, capturing the reporter, description, evidence photos, and severity level, in addition to the existing corrective-action workflow. | | |
| Preconditions | The user is authenticated and authorized. A non-conformance record exists (from a failed inspection step) or the user is filing one directly from an independent audit finding (UC-014, UC-054). | | |
| Postconditions | The non-conformance record is enriched with reportedBy, description, evidence photos, and severity level. If severity is Medium or higher, a witness is recorded. The existing corrective-action workflow (UC-038, UC-082, UC-168) continues unaffected in parallel. | | |
| Normal Flow | 1. Service Caterers or Medical Staff opens the non-conformance record generated by a failed inspection step (or creates one from an independent audit finding).  2. System displays the violation record form pre-filled with step/session context.  3. User enters description, uploads evidence photos, and selects severity level (Low/Medium/High).  4. If severity is Medium or High, system requires a witness to be selected before submission.  5. System saves the completed violation record, links it to the existing non-conformance record, logs the action to the immutable audit trail, and displays MSG-086. | | |
| Alternative Flows | AF1 - Missing Required Fields: If description or evidence photo is missing, system blocks submission and displays MSG-085. | | |
| Exceptions | MSG-004, MSG-005, MSG-085. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-183 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-159 Record Process Violation Use Cases Specification

