### Ingredient & Inventory Management

#### Confirm Delivery Schedule

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-015 - Confirm Delivery Schedule | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier requests to confirm delivery schedule. | | |
| Description | Enables the Raw Food Supplier to confirm and finalize the digital delivery schedule and assign delivery personnel for scheduled orders. | | |
| Preconditions | The raw food supplier is authenticated and authorized. Purchase requests are accepted and active. | | |
| Postconditions | Delivery schedule confirmed, delivery person face biometrics linked, and notified to the Service Caterers. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Confirm Delivery Schedule panel.  2. System displays accepted purchase requests awaiting dispatch scheduling.  3. Supplier selects an order, enters delivery date, estimated arrival time window, and assigns a registered delivery person.  4. Supplier verifies the delivery person's face biometrics profile exists (BR-050).  5. System validates that schedule is submitted at least 24 hours prior to dispatch (BR-071).  6. Supplier confirms schedule, system updates delivery status, logs the transaction, and displays success message. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If schedule is submitted <24 hours before dispatch (BR-071), system blocks confirmation and displays warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-72 Confirm Delivery Schedule Use Cases Specification

#### Create Purchase Request

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-021 - Create Purchase Request | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to create purchase request. | | |
| Description | Enables the Service Caterers to generate purchase requests for raw food ingredients based on menu requirements and current stock levels, routing requests exceeding $5,000 for dual authorization. | | |
| Preconditions | The Service Caterers is authenticated and authorized. Weekly menu is approved and published. | | |
| Postconditions | Purchase request saved. Under $5,000: Auto-dispatched to suppliers. Over $5,000: Marked 'Pending Approval' and routed for dual authorization. | | |
| Normal Flow | 1. Service Caterers selects a published weekly menu to start a Purchase Request.  2. System automatically retrieves ingredient requirements from the menu and subtracts current storage levels to calculate required quantities (BR-077).  3. Service Caterers reviews list, adjusts quantities, selects preferred supplier for each ingredient, and clicks 'Submit'.  4. System validates total request value. If total is under $5,000, system creates purchase order, emails supplier, and displays success message. | | |
| Alternative Flows | AF1 - Dual Authorization Required: If total request exceeds $5,000, system blocks automatic dispatch, flags request for dual authorization, changes status to 'Pending Approval', and notifies School Admin for review. | | |
| Exceptions | MSG-004, MSG-005, MSG-018 (Inventory shortage warning displayed during calculations). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-077 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-73 Create Purchase Request Use Cases Specification

#### Manage Deliveries

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-039 - Manage Deliveries | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier requests to manage deliveries. | | |
| Description | Enables the Raw Food Supplier to view, track, or modify parameters of active delivery runs. | | |
| Preconditions | The raw food supplier is authenticated and authorized. | | |
| Postconditions | Delivery details updated and status change logged in the audit trail. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Deliveries management panel.  2. System displays list of scheduled and active deliveries with details: Vehicle, Driver, Linked Batches (BR-071), and Schedule Status.  3. Supplier filters by date, status, or search by delivery ID.  4. Supplier selects a delivery to edit parameters (e.g. change driver or vehicle) or cancel if pending.  5. System saves changes, logs the action, and notifies the Service Caterers. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-74 Manage Deliveries Use Cases Specification

#### Manage Ingredient Batches

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-043 - Manage Ingredient Batches | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier requests to manage ingredient batches. | | |
| Description | Enables the Raw Food Supplier to view, edit, or register batch certificates and manufacturing details. | | |
| Preconditions | The manage ingredient batches transaction is completed, saved to the database, and audited. | | |
| Postconditions | Batch information verified and safety documents attached. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Ingredient Batches panel.  2. System displays a list of registered batches with: Product Name, Lot Number, Packaging Date, and Expiration Date (BR-071).  3. Supplier can edit manufacturing logs, upload certificate links, or register a new batch.  4. System validates dates, updates database, and updates audit records. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-75 Manage Ingredient Batches Use Cases Specification

#### Manage Ingredient Receiving

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-044 - Manage Ingredient Receiving | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Face Recognition Service |
| Trigger | Service Caterers opens the receiving dashboard. | | |
| Description | Enables the Service Caterers to monitor active delivery runs, initiate receiving sessions, and view historical receiving inspection logs. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The manage ingredient receiving transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Service Caterers accesses the Ingredient Receiving dashboard.  2. System displays today's scheduled deliveries with status: Scheduled, Dispatched, In Progress, or Completed (BR-078).  3. Service Caterers clicks on a dispatched delivery to initiate receiving when it arrives, triggers driver face verification (BR-050), and opens inspection checks.  4. Service Caterers can search or filter past receiving logs by date or supplier. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-050, BR-078, BR-079, BR-080 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-76 Manage Ingredient Receiving Use Cases Specification

#### Manage Inventory

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-045 - Manage Inventory | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers navigates to Inventory Management to view stock, issue ingredients, or process adjustments. | | |
| Description | Enables the Service Caterers to manage inventory: view stock levels, issue ingredients following FEFO principles, record adjustments for spoilage, and monitor expiration and shortage alerts. | | |
| Preconditions | Service Caterers is authenticated and authorized. Inventory records exist in the system. | | |
| Postconditions | Inventory accurately updated. FEFO issuance maintained (BR-076). Adjustments logged with reason codes and sign-off (BR-204). Expiration alerts triggered (MSG-052) as applicable. | | |
| Normal Flow | 1. Service Caterers navigates to Inventory Management. 2. System displays stock levels grouped by ingredient, storage zone, and batch expiration date. 3. Service Caterers reviews stock and selects ingredients for issuance or adjustment. 4. System validates FEFO principle (BR-076), storage zone matching (BR-202), and blocks expired ingredient selection (BR-205). 5. System updates inventory balances, records the transaction (MSG-042), and triggers expiration alerts (MSG-052) as applicable. | | |
| Alternative Flows | AF1 - Spoilage Write-off: Service Caterers records spoilage adjustment. System requires mandatory reason code and manager sign-off (BR-204). AF2 - Inventory Shortage: System detects insufficient stock for required ingredient and displays MSG-018. | | |
| Exceptions | MSG-004, MSG-005, MSG-018, MSG-052 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-076, BR-202, BR-204, BR-205, BR-089, BR-090 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-77 Manage Inventory Use Cases Specification

#### Record Ingredient Receiving

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-060 - Record Ingredient Receiving | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Face Recognition Service |
| Trigger | Service Caterers starts a receiving session when a supplier delivery arrives at the school. | | |
| Description | Enables the Service Caterers to record a complete ingredient receiving session with batch verification, quality inspection, GPS validation, and photographic evidence capture. | | |
| Preconditions | Service Caterers is authenticated and authorized. Delivery is within the scheduled window (BR-078). GPS enabled on device. | | |
| Postconditions | Ingredient batch record created with full traceability (supplier, batch, GPS, timestamp, photos). Inventory updated. Rejected batches excluded and supplier notified. | | |
| Normal Flow | 1. Service Caterers initiates a new receiving session for an arriving delivery.  2. System verifies delivery is within the active schedule window (BR-078) and validates GPS within 50m of school gate (BR-079).  3. Service Caterers scans delivery person's face. Face Recognition Service validates driver identity against registered delivery personnel profile (BR-050).  4. Service Caterers reviews batch info (BR-071), conducts quality inspection, and records temperature checks of cold chain items (BR-080).  5. Service Caterers uploads mandatory evidence photos. System records GPS and timestamp automatically.  6. System creates batch traceability record, updates inventory, generates receiving record, and displays MSG-041. | | |
| Alternative Flows | AF1 - Batch Rejection: Batch fails inspection (e.g., cold chain temperature above 5°C per BR-080). Service Caterers selects 'Reject'. System logs rejection, notifies supplier, excludes batch from inventory. | | |
| Exceptions | MSG-004, MSG-005, MSG-015 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-050, BR-078, BR-079, BR-080, BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and GPS-enabled device. | | |

Table 3-78 Record Ingredient Receiving Use Cases Specification

#### Record Ingredient Storage

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-061 - Record Ingredient Storage | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers starts allocating received ingredients to storage. | | |
| Description | Enables the Service Caterers to log storage locations for ingredient batches, validating compatibility with temperature zone requirements (Dry, Chilled, Frozen). | | |
| Preconditions | The Service Caterers is authenticated and authorized. Ingredient batches are received but not stored. | | |
| Postconditions | Batch storage location saved, temperature compliance verified, and inventory logs updated. | | |
| Normal Flow | 1. Service Caterers selects received batches awaiting storage allocation.  2. Service Caterers scans the batch barcode (BR-090) to identify the ingredient.  3. Service Caterers selects target storage zone (Dry, Chilled, or Frozen).  4. System validates that the selected storage zone matches the category's required temperature zone (BR-202).  5. Service Caterers confirms storage location, system records the coordinates/location, and updates database. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If selected storage zone does not match required temperature zone (BR-202), system blocks saving and displays error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-202, BR-090 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-79 Record Ingredient Storage Use Cases Specification

#### Record Inventory Transactions

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-064 - Record Inventory Transactions | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers needs to record manual stock adjustments (spoilage, waste, audit write-off). | | |
| Description | Enables the Service Caterers to record manual stock transactions, requiring mandatory reason codes and manager sign-off for adjustments. | | |
| Preconditions | The Service Caterers is authenticated and authorized. | | |
| Postconditions | Inventory adjusted and transaction recorded in audit logs. | | |
| Normal Flow | 1. Service Caterers navigates to Inventory Adjustments.  2. Service Caterers selects ingredient batch, enters adjusted quantity, and selects a mandatory reason code (BR-204).  3. Service Caterers uploads manager sign-off authentication (BR-204).  4. System records the transaction (MSG-042) and updates the stock balances. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If reason code or sign-off is missing (BR-204), system blocks transaction. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-204 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-82 Record Inventory Transactions Use Cases Specification

#### Record Prepare Ingredient Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-066 - Record Prepare Ingredient Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers starts preprocessing ingredients for a meal session. | | |
| Description | Enables the Service Caterers to log preprocessing details (washing, sanitization, cutting) for raw ingredients prior to cooking. | | |
| Preconditions | The Service Caterers is authenticated and authorized. Issued raw ingredients are at the prep station. | | |
| Postconditions | Preprocessing logs and checklist saved, linked to production run. | | |
| Normal Flow | 1. Service Caterers selects the active production run.  2. Service Caterers records preprocessing actions completed (e.g. washing, sanitizing raw vegetables/meats, peeling, cutting).  3. Service Caterers records sanitizer concentration used (if applicable) and responsible staff.  4. System saves preprocessing record and updates audit trail. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-83 Record Prepare Ingredient Information Use Cases Specification

#### Reject Ingredient Batch

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-072 - Reject Ingredient Batch | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Face Recognition Service |
| Trigger | Service Caterers identifies a batch that fails quality inspection during the receiving session. | | |
| Description | Enables the Service Caterers to formally reject a non-conforming ingredient batch, recording rejection reason and notifying the supplier. | | |
| Preconditions | Service Caterers is authenticated and authorized. Receiving session is active. Batch has failed quality inspection criteria. | | |
| Postconditions | Batch marked as 'Rejected'. Rejection details logged with timestamp. Supplier notified. Batch excluded from inventory. | | |
| Normal Flow | 1. Service Caterers identifies the ingredient batch fails quality requirements during inspection. 2. Service Caterers selects 'Reject Batch' in the active receiving session. 3. System displays rejection form with mandatory reason codes. 4. Service Caterers selects rejection reason(s) and confirms the rejection. 5. System marks batch as 'Rejected', logs rejection details with timestamp, notifies supplier, and excludes the batch from inventory. | | |
| Alternative Flows | AF1 - Partial Rejection: Only a portion of the batch fails quality requirements. System allows partial acceptance with separate batch records created for accepted and rejected quantities. | | |
| Exceptions | MSG-004, MSG-005, MSG-015 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-078, BR-079, BR-080 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-84 Reject Ingredient Batch Use Cases Specification

#### Submit Batch Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-081 - Submit Batch Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to submit batch information. | | |
| Description | Enables the Raw Food Supplier to register batch details including lot number, manufacturing date, packaging date, and certificate links in the database. | | |
| Preconditions | The raw food supplier is authenticated and authorized. Supplier's certifications are active (BR-048). | | |
| Postconditions | The submit batch information transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to Submit Batch Information.  2. Supplier enters product name, lot number, manufacturing date, packaging date, and expiration date (BR-071).  3. Supplier uploads links to safety certificates (BR-046).  4. System validates dates and registers the batch in the system. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-013 (blocked if supplier's certifications are expired per BR-048). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048, BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-85 Submit Batch Information Use Cases Specification

#### Submit Delivery Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-083 - Submit Delivery Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier schedules dispatch for an accepted purchase request. | | |
| Description | Enables the Raw Food Supplier to submit delivery details, linking active batches to the delivery schedule. | | |
| Preconditions | The raw food supplier is authenticated and authorized. Purchase request is accepted. | | |
| Postconditions | Delivery information saved, delivery status changed to 'Scheduled', and Service Caterers notified. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Deliveries management panel (UC-040) and selects the option to submit delivery information.  2. Supplier selects the target Purchase Request, enters dispatch date, vehicle license plate, driver details, and links the registered ingredient batches (BR-071).  3. System validates that delivery submission is at least 24 hours prior to arrival (BR-071).  4. Supplier submits the information, system changes status to 'Scheduled' and notifies the Service Caterers. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If submitted <24 hours prior (BR-071), system blocks submission. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-86 Submit Delivery Information Use Cases Specification

#### Update Delivery Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-092 - Update Delivery Status | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier accesses Manage Deliveries (UC-040) or transit conditions change. | | |
| Description | Enables the Raw Food Supplier to update delivery status in transit, triggering automated alerts if deviation exceeds limits, extending the Manage Deliveries (UC-040) use case. | | |
| Preconditions | The raw food supplier is authenticated and authorized. Delivery is scheduled. | | |
| Postconditions | Delivery status updated. Deviation alerts sent to Service Caterers if threshold exceeded (BR-135). | | |
| Normal Flow | 1. Raw Food Supplier accesses the Deliveries management panel (UC-040) and selects the active delivery.  2. Supplier updates status (e.g. Dispatched, In Transit, Delayed, Delivered).  3. System records update. If actual dispatch/transit time deviates by >30 minutes from the schedule, system triggers automated alert (MSG-065) to Service Caterers (BR-135).  4. System saves status change and logs audit trail. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-135, BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-87 Update Delivery Status Use Cases Specification

#### Upload Batch Certificates

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-100 - Upload Batch Certificates | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier accesses Manage Ingredient Batches (UC-044) and requests to upload batch certificates. | | |
| Description | Enables the Raw Food Supplier to upload certificate documents and link them to registered batches, extending the Manage Ingredient Batches (UC-044) use case. | | |
| Preconditions | The raw food supplier is authenticated and authorized. Target batch exists in the system. | | |
| Postconditions | The upload batch certificates transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Ingredient Batches panel (UC-044), selects the target batch, and selects the option to upload certificates.  2. Supplier uploads safety certificates (as PDF/image) and fills in the certificate expiration dates (BR-071).  3. System validates document format, links certificates to the batch record, and updates expiration tracking. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-009 (Unsupported file type). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-88 Upload Batch Certificates Use Cases Specification

#### View Batch History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-107 - View Batch History | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to view batch history. | | |
| Description | Enables the Raw Food Supplier to safely query, view, or export the details of Batch History records. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The view batch history data is successfully retrieved and displayed to the raw food supplier. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Ingredient Batches panel (UC-044) and selects the option to view batch history.  2. System retrieves and displays all registered batches including: Lot Number, Packaging Date, Expiration Date, and Certificate Links (BR-071).  3. Supplier can search by Lot Number or filter by Expiration Status. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-89 View Batch History Use Cases Specification

#### View Delivery Batch Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-109 - View Delivery Batch Records | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier accesses Manage Ingredient Batches (UC-044) and requests to view delivery batch records. | | |
| Description | Enables the Raw Food Supplier to query and view historical delivery schedules and linked ingredient batches, extending the Manage Ingredient Batches (UC-044) use case. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The view delivery batch records data is successfully retrieved and displayed to the raw food supplier. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Ingredient Batches panel (UC-044) and selects the option to view delivery batch records.  2. System retrieves and displays historical delivery runs with: Vehicle Plaque, Driver Name, Dispatch Timestamp (BR-071), and Linked Batch Codes (BR-071).  3. Supplier can search by dispatch date range or filter by delivery status. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-077 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-90 View Delivery Batch Records Use Cases Specification

#### View Ingredient Receiving Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-123 - View Ingredient Receiving Records | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view ingredient receiving records. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Ingredient Receiving Records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view ingredient receiving records data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Ingredient Receiving Records section in the system portal. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-015 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-078, BR-079, BR-080 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-91 View Ingredient Receiving Records Use Cases Specification

#### View Purchase Requests

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-139 - View Purchase Requests | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to view purchase requests. | | |
| Description | Enables the Raw Food Supplier to safely query, view, or export the details of Purchase Requests records. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The view purchase requests data is successfully retrieved and displayed to the raw food supplier. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Purchase Requests section in the system portal. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-018 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-077 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-92 View Purchase Requests Use Cases Specification

