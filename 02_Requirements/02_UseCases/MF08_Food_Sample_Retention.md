### Food Sample Retention

#### Dispose Food Sample

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-030 - Dispose Food Sample | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | System generates a disposal alert when a food sample's mandatory 72-hour retention period expires with no active incident linked. | | |
| Description | Enables the Service Caterers to record and confirm the disposal of retained food samples after the mandatory 72-hour period in compliance with Decision 1246/QĐ-BYT, updating the disposal tracking columns of Mẫu số 5 when no active food safety incident is associated. | | |
| Preconditions | Service Caterers is authenticated and authorized. Sample's 72-hour retention period has elapsed (BR-111, BR-112). No active food safety incident is linked to this batch (BR-112). | | |
| Postconditions | Food sample marked as 'Disposed'. Disposal details (date, method, witness) logged (BR-113). Retention record permanently closed. | | |
| Normal Flow | 1. System sends a disposal alert when the 72-hour retention period expires.  2. Service Caterers opens the Food Sample Retention panel and selects the expired sample.  3. System verifies no active food safety incident is linked to this batch (BR-112).  4. Service Caterers enters disposal details corresponding to Sample No 5 columns 9 & 12: disposal date/time,disposal method, and signs off as the person disposing along with a witness name (BR-113).  5. System marks the sample as 'Disposed', logs all details, updates the Sample No 5 register, and closes the retention record. | | |
| Alternative Flows | AF1 - Disposal Suspended by Incident: A student reports food safety symptoms related to this batch. System immediately suspends disposal, locks the sample record (BR-112), and alerts Medical Staff to authorize sample retrieval for laboratory analysis (BR-113). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (after retention cycle expires) | | |
| Business Rules | BR-111, BR-112, BR-113 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-103 Dispose Food Sample Use Cases Specification

#### Manage Food Sample Retention

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-042 - Manage Food Sample Retention | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to manage food sample retention records or log refrigerator parameters. | | |
| Description | Enables the Service Caterers to log refrigerator temperatures twice daily, manage sample storage locations, and monitor cabinet statuses to ensure retention compliance. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | Refrigerator temperature log created or storage parameters updated in the database, with changes audited. | | |
| Normal Flow | 1. Service Caterers accesses the Food Sample Retention management panel.  2. System displays active refrigerators, their current registered temperature logs, and active samples stored in each refrigerator (BR-109).  3. Service Caterers selects 'Log Temperature' for a dedicated retention refrigerator.  4. Service Caterers enters the current temperature reading (°C) and selects the reading session (Morning or Evening) (BR-110). 5. Service Caterers submits the temperature log.  6. System validates that the temperature is within the safe range of 2°C to 8°C (BR-109).  7. System saves the temperature record, updates the refrigerator log, and displays a confirmation message.. | | |
| Alternative Flows | AF1 - Refrigerator Temperature Out of Range:  1. At step 6, the entered temperature is outside the 2°C to 8°C range (BR-109).  2. System displays warning message MSG-020 notifying the user that the temperature is out of the safe range.  3. System saves the temperature record with a warning flag, sends an immediate notification alert, and logs the deviation event.  4. If the temperature remains out of limits for >30 minutes, system triggers the temperature alarm alert (MSG-059).  AF2 - Update Refrigerator Assignment:  1. At step 2, Service Caterers chooses to reassign a food sample to a different refrigerator (e.g., due to maintenance).  2. Service Caterers selects the sample, selects the target refrigerator, and enters the reason for transfer.  3. System verifies the target refrigerator has active slots and is within safe temperature.  4. System updates the sample's storage record and logs the transfer details for audit | | |
| Exceptions | MSG-004, MSG-005, MSG-020, MSG-059 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (minimum twice per day for temperature logs) | | |
| Business Rules | BR-109, BR-110 | | |
| Other Information | Supports weekly audits conducted by Medical Staff (BR-110) by maintaining temperature logs. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-104 Manage Food Sample Retention Use Cases Specification

#### Register Food Sample

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-069 - Register Food Sample | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers selects 'Register Food Sample' after completing all three-step inspections for a meal batch. | | |
| Description | Enables the Service Caterers to register food samples for every cooked dish served in compliance with Decision 1246/QĐ-BYT, generating the sample label (Sample No 4) and initiating the sample retention tracking log (Sample No 5) with sample weight, labeling, photographic evidence, and GPS. | | |
| Preconditions | Service Caterers is authenticated and authorized. Meal batch has passed all three-step inspections. GPS enabled. | | |
| Postconditions | Sample retention records created and linked to inspection records. 72-hour cold storage timer initiated (BR-111). GPS validated within kitchen premises (BR-109). Photo evidence attached. MSG-044 displayed. | | |
| Normal Flow | 1. Service Caterers navigates to 'Register Food Sample' for the inspected meal batch.  2. System validates GPS location is within school kitchen premises (BR-109) and displays the registration form.  3. Service Caterers enters sample details corresponding to columns 1-8 of Sample No 5: meal slot, dish, total portion count, sample weight (minimum 100g for solid dishes / 150ml for liquid dishes per BR-203), storage container description, refrigerator storage temperature, and time of collection.  4. System generates and prints the sample container label conforming to Sample No 4, displaying Meal, Food sample name, Time of sampling, and Sampling person (Service Caterers staff).  5. Service Caterers pastes the printed label onto the container, uploads a photo of the labeled container (BR-107), signs the register digitally (Sample No 5 column 11), and confirms storage.  6. System saves the sample retention record, links it to the meal batch, starts the 72-hour storage timer (BR-111), and displays MSG-044. | | |
| Alternative Flows | AF1 - Insufficient Sample Weight:  1. At step 7, the entered sample weight is below the minimum threshold for its dish type (BR-203). 2. System blocks registration and displays error message MSG-019.  3. System prompts the Service Caterers to input the correct weight or add more sample.  AF2 - GPS Location Verification Fails:  1. At step 2, the system validates the GPS coordinates and finds they are outside the school kitchen premises (BR-109).  2. System blocks registration and displays error message MSG-015.  3. System logs the GPS deviation error for compliance audit. | | |
| Exceptions | MSG-004, MSG-005, MSG-015, MSG-019 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (for every cooked dish served) | | |
| Business Rules | BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-203 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and GPS-enabled device. | | |

Table 3-105 Register Food Sample Use Cases Specification

#### Retrieve Food Sample Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-076 - Retrieve Food Sample Records | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | Service Caterers (lấy mẫu & bàn giao khi cơ quan tới nhận — BR-215) |
| Trigger | Medical Staff requests to retrieve and authorize a food sample record for laboratory analysis. | | |
| Description | Enables the Medical Staff to authorize the physical retrieval and lock the system record of retained food samples for laboratory analysis during an active food safety incident investigation. | | |
| Preconditions | Medical staff is authenticated and authorized. An active food safety incident exists with a valid Incident ID. The selected food sample is currently retained (not yet disposed of). | | |
| Postconditions | Sample physical retrieval is authorized and logged. The system record of the food sample is locked to prevent accidental disposal (BR-114, BR-150). The retrieval record is linked to the incident report (BR-114). | | |
| Normal Flow | 1. Medical Staff navigates to the Food Sample Records list and searches for samples by suspected meal date, dish name, or batch ID.  2. System displays the matching retained food samples, showing their current status (e.g., 'Retained') and remaining retention time (BR-111).  3. Medical Staff selects the relevant food sample and clicks 'Authorize Retrieval'.  4. System prompts for authorization details: Incident ID, name of authorizing medical officer, and physical destination lab (BR-114).  5. Medical Staff enters the details and submits.  6. System validates that the Incident ID is valid and active in the database.  7. System locks the food sample record (changing status to 'Under Investigation' / 'Retrieved' and blocking disposal per BR-114, BR-150), logs the retrieval authorization, and links the sample to the incident report (BR-114).  8. System gửi yêu cầu tới Service Caterers để lấy mẫu tại tủ lưu và bàn giao khi cán bộ của cơ quan tiếp nhận tới trường (BR-215) — bếp không tự vận chuyển mẫu; hệ thống KHÔNG tự sinh biên bản thu hồi — bằng chứng bàn giao do Service Caterers tải lên (BR-212). | | |
| Alternative Flows | AF1 - Sample Already Disposed:  1. At step 2, if the matching sample has already been disposed of (exceeded 72 hours and was cleared).  2. System displays the sample record with a status of 'Disposed' along with its disposal log (date, method, witness per BR-113).  3. Medical Staff cannot authorize physical retrieval, but they can select 'Link Disposed Sample to Incident' to associate the disposal metadata with the active incident for record-keeping and investigation tracing.  AF2 - Invalid Incident ID:  1. At step 6, the system validates the Incident ID and finds it is invalid or closed.  2. System blocks the retrieval authorization and displays error message MSG-023.  3. System prompts the Medical Staff to enter a valid active Incident ID.  AF3 - Huỷ yêu cầu thu hồi (bổ sung 22/07/2026):  1. Sau bước 8, khi Service Caterers CHƯA xác nhận đã gửi lab, Medical Staff huỷ được yêu cầu kèm lý do bắt buộc.  2. System đặt yêu cầu về CANCELLED, ghi audit (BR-170) và mở khoá huỷ cho toàn bộ mẫu thuộc yêu cầu.  3. Huỷ áp dụng cho CẢ yêu cầu, không huỷ từng mẫu — một yêu cầu là một lần bàn giao cho phòng lab. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand (during active food safety incidents) | | |
| Business Rules | BR-111, BR-113, BR-114, BR-150 | | |
| Other Information | Linked with UC-042 - Manage Food Safety Incidents when lab results are required. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-106 Retrieve Food Sample Records Use Cases Specification

#### View Food Sample Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-118 - View Food Sample Records | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff, School Administration, Service Caterers | Secondary Actors | None |
| Trigger | Primary actor requests to view food sample records. | | |
| Description | Enables the Medical Staff and School Admin to query, view, and export the complete digital Food Sample Retention and Disposal log (Mẫu số 5) for traceability and compliance monitoring. | | |
| Preconditions | The medical staff, school admin is authenticated and authorized to access this feature. | | |
| Postconditions | Requested food sample records and history are retrieved and displayed on the screen. | | |
| Normal Flow | 1. User (Medical Staff or School Admin) navigates to the Food Sample Records section in the system portal.  2. System queries the database and displays the digital Food Sample Retention and Disposal register conforming to Mẫu số 5, displaying all columns (1 to 12) including sample names, collection times, storage details, and disposal/witness signatures.  3. User can filter the records by date range, meal slot, dish name, or status (Retained, Retrieved, Disposed).  4. User can click 'Export' to download the selected Mẫu số 5 records as Excel or PDF for inspection by authorities. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (for monitoring) / Weekly | | |
| Business Rules | BR-108, BR-111, BR-114, BR-107, BR-110 | | |
| Other Information | This UC supports the mandatory weekly food sample storage audits conducted by Medical Staff | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-107 View Food Sample Records Use Cases Specification

