### Supplier & Partner Management

#### Approval / Reject Service Caterers

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-004 - Approval / Reject Service Caterers | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | Primary actor requests to approval / reject Service Caterers. | | |
| Description | Enables the authorized School Administration to review, evaluate, and approve or reject a pending Service Caterers registration based on submitted documentation and site inspection results. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. At least one Service Caterers registration is in 'Pending Approval' status. | | |
| Postconditions | Company status is updated to 'Approved' or 'Rejected'. The decision is saved to the database and audited. A notification email is sent to the Service Caterers. | | |
| Normal Flow | 1. School Administration navigates to the pending approvals dashboard for Service Caterers. 2. System displays a list of catering companies with 'Pending Approval' status. 3. School Administration selects a Service Caterers to review. 4. System displays the company's profile, business license, food safety certificates, and site inspection score. 5. School Administration reviews the documentation and site inspection score. 6. School Administration selects 'Approve' or 'Reject'. 7. If 'Approve': System updates the status to 'Approved', creates the supplier contract record, and sends an approval notification email. 8. If 'Reject': School Administration enters a rejection reason. System updates the status to 'Rejected' and sends a rejection notification email with the stated reason. 9. System logs the action in the audit trail. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-45 Approval / Reject Service Caterers Use Cases Specification

#### Approval / Reject Supplier

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-007 - Approval / Reject Supplier | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | Primary actor requests to approval / reject supplier. | | |
| Description | Enables the authorized School Administration to review, evaluate, and approve or reject a pending Raw Food Supplier registration based on submitted documentation and food safety certifications. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. At least one supplier registration is in 'Pending Approval' status. | | |
| Postconditions | The supplier status is updated to 'Approved' or 'Rejected'. The decision is saved to the database and audited. A notification email is sent to the supplier. | | |
| Normal Flow | 1. School Administration navigates to the pending approvals dashboard for Raw Food Suppliers.  2. System displays a list of suppliers with 'Pending Approval' status.  3. School Administration selects a supplier to review.  4. System displays the supplier's company profile, business license, tax ID, food safety certifications, and certificate expiration dates (BR-047).  5. School Administration reviews the documentation and verifies certificate validity (BR-046).  6. School Administration selects 'Approve' or 'Reject'.  7. If 'Approve': System updates the status to 'Approved', activates the supplier account, and sends an approval notification email.  8. If 'Reject': School Administration enters a rejection reason. System updates the status to 'Rejected' and sends a rejection notification email with the stated reason.  9. System logs the action in the audit trail. | | |
| Alternative Flows | AF1 - Expired Certificates: One or more uploaded food safety certificates have expired (BR-048). System displays MSG-013 and prevents approval until valid certificates are submitted.  AF2 - Incomplete Documentation: Required documents (business license, tax ID, food safety certifications) are missing. System highlights missing fields and prevents approval.  AF3 - Administrator Cancels: School Administration cancels the review before confirming. System returns to the pending approvals list without changing the supplier's status. | | |
| Exceptions | MSG-004, MSG-005, MSG-013 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-46 Approval / Reject Supplier Use Cases Specification

#### Create Info Delivery Person

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-019 - Create Info Delivery Person | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Face Recognition Service |
| Trigger | Primary actor requests to create info delivery person. | | |
| Description | Enables the Raw Food Supplier to input, validate, and save new Delivery Person Information details into the system database. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. The supplier's account is in 'Approved' status. | | |
| Postconditions | he delivery person profile is created and saved to the database. The delivery person is eligible for biometric face registration (UC-070). The creation event is audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Delivery Personnel Management panel and selects 'Add New Delivery Person'.  2. System displays the delivery person registration form.  3. Raw Food Supplier enters the delivery person's full name, national ID number, phone number, and vehicle license plate.  4. System validates that all required fields are complete and the national ID is not already registered.  5. System saves the delivery person profile with 'Pending Biometric Registration' status.  6. System displays a success confirmation and prompts the supplier to proceed to face biometric registration (UC-070). | | |
| Alternative Flows | AF1 - Duplicate National ID: The entered national ID is already registered under another delivery person. System displays an error and prompts for correction.  AF2 - Supplier Cancels: Raw Food Supplier cancels the form before submission. System discards all entered data and returns to the Delivery Personnel Management panel. | | |
| Exceptions | MSG-004, MSG-005, MSG-014 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-051, BR-060 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-47 Create Info Delivery Person Use Cases Specification

#### Delete Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-028 - Delete Face Data | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Face Recognition Service |
| Trigger | Primary actor requests to delete face data. | | |
| Description | Enables the Raw Food Supplier to permanently remove a delivery person's biometric face data from the system. Upon deletion, the delivery person will no longer be authorized for school gate biometric verification. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. The target delivery person has an existing face data record in the system. | | |
| Postconditions | The delivery person's biometric face data is permanently deleted from the system. The delivery person's gate access authorization is revoked. The deletion event is logged in the audit trail with timestamp and actor identity. | | |
| Normal Flow | 1.Raw Food Supplier navigates to the Delivery Personnel Management panel.  2. System displays the list of registered delivery persons with their biometric registration status.  3. Raw Food Supplier selects a delivery person and chooses 'Delete Face Data'.  4. System displays a confirmation dialog warning that this action is irreversible and will revoke the delivery person's school gate access.  5. Raw Food Supplier confirms the deletion.  6. Face Recognition Service removes the biometric data record. System updates the delivery person's status to 'Biometric Not Registered'.  7. System logs the deletion event in the audit trail. 8. System displays a deletion success confirmation | | |
| Alternative Flows | AF1 - Supplier Cancels Deletion: Raw Food Supplier selects 'Cancel' in the confirmation dialog. System closes the dialog and takes no action.  AF2 - Face Recognition Service Unavailable: The service cannot be reached. System displays MSG-014, aborts the deletion, and instructs the supplier to retry later. | | |
| Exceptions | MSG-004, MSG-005, MSG-014 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-051, BR-060 | | |
| Other Information | Deletion of biometric data is irreversible. A new face registration (UC-070) must be performed if gate access is required again. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-48 Delete Face Data Use Cases Specification

#### Manage Compliance Documents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-167 - Manage Compliance Documents | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to view and manage the list of their compliance documents. | | |
| Description | Enables the Raw Food Supplier to view, search, filter, and access the full list of their uploaded compliance documents including food safety certificates, business licenses, and product origin documents. From this panel, the supplier can initiate individual document upload or update actions. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The manage compliance documents transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Compliance Documents section.  2. System retrieves and displays the list of all compliance documents associated with the supplier's account.  3. System displays each document's name, type, upload date, expiration date, and status (Valid / Expiring Soon / Expired).  4. Raw Food Supplier can filter documents by type or status.  5. Raw Food Supplier selects a document to view its detail or initiate an update or replacement action. | | |
| Alternative Flows | AF1 - No Documents Uploaded: The supplier has no compliance documents on record. System displays an empty state message with a prompt to upload the first document (UC-103).  AF2 - Expiring Documents Alert: One or more documents expire within 180 days. System highlights those documents with an 'Expiring Soon' badge; if fewer than 30 days remain and the document is still unrenewed, the badge escalates to 'Urgent' (BR-047). | | |
| Exceptions | MSG-004, MSG-005, MSG-013 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-49 Manage Compliance Documents Use Cases Specification

#### Manage Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-049 - Manage Profile | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to view or update their company profile information. | | |
| Description | Enables the Raw Food Supplier to view, edit, and save updates to their company profile including contact information, business address, and company description. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The manage profile transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Profile Management section.  2. System displays the current company profile including company name, official email, phone number, business address, and company description.  3. Raw Food Supplier edits the desired fields.  4. System validates that all required fields are complete, the email format is correct, and the phone number format is valid.  5. Raw Food Supplier submits the updated profile.  6. System saves the changes and displays a success confirmation. | | |
| Alternative Flows | AF1 - No Changes Made: Raw Food Supplier views the profile but does not modify any fields. System does not trigger a save event.  AF2 - Email Already Registered: The new email is already associated with another account. System displays an error and prompts for a different email.  AF3 - Supplier Cancels: Raw Food Supplier cancels the edit. System discards unsaved changes and restores the previous profile data. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047 | | |
| Other Information | Company name changes may require re-verification by School Administration. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-50 Manage Profile Use Cases Specification

#### Register Service Caterers Account

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-067 - Register Service Caterers Account | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Guest | Secondary Actors | Email Service |
| Trigger | Guest navigates to the platform and selects 'Register as Service Caterers'. | | |
| Description | Enables a Guest (prospective Service Caterers) to submit a registration application including company details and required compliance documentation for school approval. | | |
| Preconditions | Registration form is publicly accessible. Email address not previously registered. No authentication required. | | |
| Postconditions | Registration request submitted with 'Pending Approval' status. Verification email sent (BR-010). School Administration notified for review. Site inspection score must be >= 80% for approval. | | |
| Normal Flow | 1. Guest navigates to 'Register as Service Caterers' page. 2. System displays registration form with required company information fields. 3. Guest enters company name, business license, official email, address, and contact details. 4. System validates email uniqueness, required field completeness, and format constraints. 5. System creates the pending registration record, sends verification email (BR-010), and notifies School Administration for review. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-51 Register Service Caterers Account Use Cases Specification

#### Register Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-068 - Register Face Data | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Face Recognition Service |
| Trigger | Raw Food Supplier selects 'Register Face Data' for a delivery person in the Delivery Personnel Management panel. | | |
| Description | Enables the Raw Food Supplier to biometrically register a delivery person's face data for school gate identity verification during ingredient deliveries. | | |
| Preconditions | Raw food supplier is authenticated and authorized. Delivery person profile exists. Face Recognition Service is accessible. Device has camera access. | | |
| Postconditions | Delivery person's biometric face data registered, encrypted with AES-256 (BR-060), and stored. Delivery person authorized for gate biometric verification. | | |
| Normal Flow | 1. Raw Food Supplier navigates to Delivery Personnel Management and selects 'Register Face Data' for a delivery person.  2. System activates device camera and displays face capture instructions.  3. Raw Food Supplier positions delivery person and captures the face image.  4. Face Recognition Service validates biometric data quality and uniqueness.  5. System encrypts the biometric data, stores the registration, and displays MSG-038. | | |
| Alternative Flows | AF1 - Biometric Quality Failure: Face capture fails quality validation. System displays MSG-014 and prompts for retry with improved lighting/positioning.  AF2 - 6-Month Re-verification Prompt: After 6 months, system prompts mandatory biometric re-registration (BR-051). | | |
| Exceptions | MSG-004, MSG-005, MSG-014 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-051, BR-060 | | |
| Other Information | Upon successful registration, MSG-038 is displayed: "Delivery staff face biometrics registered successfully." | | |
| Assumptions | User has a running environment with internet connection and device camera access. | | |

Table 3-52 Register Face Data Use Cases Specification

#### Register Supplier Account

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-071 - Register Supplier Account | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Guest | Secondary Actors | Email Service |
| Trigger | Guest navigates to the platform and selects 'Register as Raw Food Supplier'. | | |
| Description | Enables a Guest (prospective supplier) to submit a supplier onboarding application with company profile and required food safety certifications for approval. | | |
| Preconditions | Registration form is publicly accessible. Email not previously registered. No authentication required. | | |
| Postconditions | Supplier registration submitted with 'Pending Approval' status. Verification email sent (BR-010). Certificate expiration tracked (BR-047). School Administration notified. | | |
| Normal Flow | 1. Guest navigates to 'Register as Supplier' page.  2. System displays the registration form.  3. Guest enters company name, business license, tax ID, official email, address, and uploads food safety certifications  4. System validates email uniqueness, document completeness, and certificate format. System tracks certificate expiration dates (BR-047).  5. System creates the pending record,sends verification email (BR-010), and notifies School Administration.. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006, MSG-013 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046, BR-047 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-53 Register Supplier Account Use Cases Specification

#### Update Compliance Documents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-091 - Update Compliance Documents | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to update compliance documents. | | |
| Description | Enables the Raw Food Supplier to update the content or replace an existing compliance document (such as a renewed certificate or revised business license) in the system. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The updated compliance document is saved to the database. Certificate expiration date is re-evaluated. If a previously expired certificate is replaced with a valid one, the supplier's blocked status is reviewed. The update event is audited. | | |
| Normal Flow | 1. Raw Food Supplier accesses the Compliance Documents panel (UC-038) and selects the target document to update.  2. System displays the current document details including name, upload date, expiration date, and status.  3. Raw Food Supplier uploads the replacement document file and updates the expiration date if applicable.  4. System validates the document format, completeness, and new expiration date.  5. System saves the updated document record and re-evaluates the certificate status.  6. System displays a success confirmation. | | |
| Alternative Flows | AF1 - Expired Certificate Replacement: Supplier uploads a new certificate to replace an expired one. System detects the new expiration date is valid, updates the certificate status to 'Valid', and re-activates the supplier's ability to receive purchase requests (BR-048).  AF2 - Invalid Document Format: The uploaded file does not meet format requirements. System displays MSG-009 and prompts the supplier to upload a valid file.  AF3 - Supplier Cancels: Raw Food Supplier cancels the update. System discards the new file and retains the original document. | | |
| Exceptions | MSG-004, MSG-005, MSG-009 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-54 Update Compliance Documents Use Cases Specification

#### Upload Food Safety Certificates

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-101 - Upload Food Safety Certificates | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to upload food safety certificates. | | |
| Description | Enables the Raw Food Supplier to upload a new food safety certificate to the system. The system validates the document format and tracks the expiration date to ensure compliance . | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The food safety certificate is saved to the database with its expiration date tracked. The system evaluates whether the new certificate resolves any previous compliance block (BR-048). The upload event is audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Upload Food Safety Certificates panel.  2. System displays the upload form with fields for certificate name, issuing authority, issue date, and expiration date.  3. Raw Food Supplier selects the certificate file and fills in the required fields.  4. System validates the file format, required fields, and expiration date.  5. System saves the certificate record, tracks the expiration date (BR-047), and displays a success confirmation. | | |
| Alternative Flows | AF1 - Expired Certificate Uploaded: The expiration date entered is already past. System displays MSG-013 and blocks the upload. Supplier must upload a valid, non-expired certificate.  AF2 - Certificate Nearing Expiry: The expiration date is within 30 days from today. System accepts the upload but displays a warning that the certificate will expire soon and a renewal will be required (BR-047).  AF3 - Duplicate Certificate: A certificate of the same type and issuing authority already exists. System warns the supplier and asks whether to replace the existing record or add as a supplementary certificate. | | |
| Exceptions | MSG-004, MSG-005, MSG-013 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-55 Upload Food Safety Certificates Use Cases Specification

#### Upload Product Origin Documents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-102 - Upload Product Origin Documents | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to upload product origin documents. | | |
| Description | Enables the Raw Food Supplier to upload a new product origin document (such as a certificate of origin, VietGAP certification, or organic certification) to the system to support ingredient traceability requirements. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The upload product origin documents transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Upload Product Origin Documents panel.  2. System displays the upload form with fields for document name, document type, issuing authority, and expiration date (if applicable).  3. Raw Food Supplier selects the document file and fills in the required fields.  4. System validates the file format and required fields.  5. System saves the product origin document record and displays a success confirmation. | | |
| Alternative Flows | AF1 - Invalid File Format: The uploaded file does not meet the accepted format requirements. System displays MSG-013 and prompts the supplier to upload a valid file.  AF2 - Supplier Cancels: Raw Food Supplier cancels before submitting. System discards the selected file and returns to the document list. | | |
| Exceptions | MSG-004, MSG-005, MSG-013 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-56 Upload Product Origin Documents Use Cases Specification

#### View Service Caterers Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-108 - View Service Caterers Detail | | |
| Created By | MinhTTHE186158 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view the detail information of a specific Service Caterers. | | |
| Description | Enables the School Administration to query and view the detailed profile of a specific Service Caterers, including company information, contract status, compliance documents, site inspection history, and performance records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. The target Service Caterers record exists in the system. | | |
| Postconditions | The Service Caterers detail data is successfully retrieved and displayed to the school administration. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to the Service Caterers management section.  2. System displays the list of catering companies.  3. School Administration selects a specific Service Caterers.  4. System retrieves and displays the Service Caterers's full detail profile including company name, contract status, compliance certificates, site inspection scores, and contact information. | | |
| Alternative Flows | AF1 - Record Not Found: The requested Service Caterers record cannot be found. System displays | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | — | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-57 View Service Caterers Detail Use Cases Specification

#### View Delivery Person List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-110 - View Delivery Person List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Face Recognition Service |
| Trigger | Primary actor requests to view the list of registered delivery persons. | | |
| Description | Enables the Raw Food Supplier to safely query, view, or export the details of Delivery Person List records. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The delivery person list data is successfully retrieved and displayed to the raw food supplier. No data is modified. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Delivery Personnel Management panel.  2. System retrieves and displays the list of all delivery persons registered under the supplier's account.  3. System displays each delivery person's name, national ID (masked), phone number, biometric registration status, and gate access status.  4. Raw Food Supplier can select a delivery person to view details or initiate management actions | | |
| Alternative Flows | F1 - No Delivery Persons Registered: The supplier has no delivery persons on record. System displays an empty state with a prompt to add the first delivery person (UC-019).  AF2 - Biometric Re-verification Required: One or more delivery persons have biometric data older than 6 months (BR-051). System highlights those records with a 'Re-verification Required' badge. | | |
| Exceptions | MSG-004, MSG-005, MSG-014 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-051, BR-060 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-58 View Delivery Person List Use Cases Specification

#### View List Service Caterers

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-127 - View List Service Caterers | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view list Service Caterers. | | |
| Description | Enables the School Administration to query and view the full list of catering companies registered in the system, with filtering and search capabilities by status, contract period, and company name. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The Service Caterers list data is successfully retrieved and displayed to the school administration. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to the Service Caterers management section.  2. System retrieves and displays the list of all catering companies with their names, contract status, and registration date.  3. School Administration can filter the list by status (Pending, Approved, Rejected, Suspended) or search by company name.  4. School Administration can select a Service Caterers to view its full detail (UC-110). | | |
| Alternative Flows | AF1 - No Catering Companies Found: No catering companies match the applied filter criteria. System displays an empty result message and prompts to clear filters. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | — | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-59 View List Service Caterers Use Cases Specification

#### View List Supplier

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-131 - View List Supplier | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view the list of all registered raw food suppliers. | | |
| Description | Enables the School Administration to query and view the full list of raw food suppliers registered in the system, including their approval status, certificate validity, and performance index summary. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The supplier list data is successfully retrieved and displayed to the school administration. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to the Supplier management section. 2. System retrieves and displays the list of all registered suppliers with their company name, approval status, certificate validity, and latest performance index. 3. School Administration can filter the list by status (Pending, Approved, Rejected, Flagged) or search by company name. 4. School Administration can select a supplier to view full detail | | |
| Alternative Flows | AF1 - No Suppliers Found: No suppliers match the applied filter criteria. System displays an empty result message and prompts to clear filters. AF2 - Flagged Suppliers Present: One or more suppliers are flagged for suspension. System highlights those entries with a 'Performance Warning' badge | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | — | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-60 View List Supplier Use Cases Specification

#### View Supplier Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-148 - View Supplier Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view supplier detail. | | |
| Description | Enables the School Administration to view the complete detail profile of a specific raw food supplier, including company information, approval status, compliance documents, certificate expiration dates, delivery history, and monthly performance index score | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. The target supplier record exists in the system. | | |
| Postconditions | The supplier detail data is successfully retrieved and displayed to the school administration. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to the Supplier management section and selects a specific supplier. 2. System retrieves and displays the supplier's full detail profile including: company name, contact information, approval status, uploaded compliance documents with expiration dates, delivery history summary, and monthly performance index score. 3. School Administration reviews the supplier detail. 4. If the supplier's performance index is below 70% for two consecutive months, System displays a 'Performance Warning' alert indicating the supplier is flagged for review. | | |
| Alternative Flows | AF1 - Record Not Found: The requested supplier record cannot be found. System displays MSG-004.  AF2 - Certificates Expiring Soon: One or more certificates are expiring within 180 days. System highlights those certificates with an 'Expiring Soon' indicator; if fewer than 30 days remain and still unrenewed, the indicator escalates to 'Urgent' (BR-047). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | — | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-61 View Supplier Detail Use Cases Specification

#### 3.2.4.18 Submit Supplier Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-152 - Submit Supplier Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Email Service |
| Trigger | Raw Food Supplier requests to submit a supplier application. | | |
| Description | Enables the Raw Food Supplier to submit their company profile, tax information, business license, and food safety certificates to apply for school onboarding. | | |
| Preconditions | The Raw Food Supplier is authenticated, logged in, and does not have an active application. | | |
| Postconditions | The application is created and saved in the database with status 'Pending Approval'. The School Administration is notified and a confirmation email is sent to the supplier. | | |
| Normal Flow | 1. Supplier navigates to the "Apply for Onboarding" interface. 2. System displays the application form requesting company details (name, tax ID, address), business license, and food safety certifications. 3. Supplier inputs company details and uploads business license and certificates. 4. Supplier clicks "Submit". 5. System validates the inputs (e.g., non-empty fields, valid tax ID format per MSG-007, and non-expired certificates per BR-046). 6. System saves the application with status 'Pending Approval' and logs the event. 7. System sends a confirmation email to the supplier (BR-010) and notifies the School Administration. 8. System displays a success message (MSG-028). | | |
| Alternative Flows | AF1 - Expired/Invalid Certificates: One or more uploaded food safety certificates have expired (BR-048). System displays MSG-013 and blocks submission. | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a stable internet connection and necessary company documents. | | |

#### 3.2.4.19 Track Supplier Application Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-169 - Track Supplier Application Status | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier requests to check the status of their supplier application. | | |
| Description | Allows the Raw Food Supplier to view the current progress, evaluation status, and comments from the School Administration regarding their onboarding application. | | |
| Preconditions | The Raw Food Supplier is authenticated and has previously submitted an onboarding application. | | |
| Postconditions | System retrieves and displays the current application status and notes. | | |
| Normal Flow | 1. Supplier navigates to the "Application Status" section.  2. System retrieves the supplier's application record from the database.  3. System displays the current status (e.g., Pending, Approved, Rejected, Action Required) and comments/reasons from the School Administration.  4. Supplier views the status. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a stable internet connection. | | |

#### 3.2.4.20 Update/Resubmit Supplier Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-170 - Update/Resubmit Supplier Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Email Service |
| Trigger | Raw Food Supplier requests to update and resubmit a rejected or incomplete application. | | |
| Description | The Raw Food Supplier is authenticated and their application status is 'Rejected' or 'Action Required'. | | |
| Preconditions | The Raw Food Supplier is authenticated and has previously submitted an onboarding application. | | |
| Postconditions | The application is updated, status is reset to 'Pending Approval', and School Admin is notified. | | |
| Normal Flow | 1. Supplier navigates to the "Application Status" section and clicks "Edit/Update Application". 2. System displays the form pre-filled with previous inputs and displays comments from the School Administration explaining the issues. 3. Supplier updates the required fields or uploads new files. 4. Supplier clicks "Resubmit". 5. System validates the updated inputs (BR-046). 6. System updates the database record with the new details and resets status to 'Pending Approval'. 7. System sends a notification email to the supplier (BR-010) and notifies the School Admin. 8. System displays a success message (MSG-029). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has corrected the documents as requested by the School Administration. | | |

#### 3.2.4.21 Manage Supplier Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-155 - Manage Supplier Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier logs in and accesses their application dashboard. | | |
| Description | The main dashboard use case that allows the Raw Food Supplier to manage their onboarding application flow, directing them to submit, track, or update their application. | | |
| Preconditions | The Raw Food Supplier is authenticated and logged in | | |
| Postconditions | The system presents options corresponding to the current state of their application. | | |
| Normal Flow | 1. Supplier navigates to the "Onboarding Portal".  2. System checks the state of the supplier's application in the database.  3. System displays available actions based on application state: "Submit Application" (if no application exists), "View Application Status" (if an application is pending or approved), or "Edit and Resubmit Application" (if rejected or action required).  4. Supplier selects an option.  5. System routes the user to the corresponding use case (UC-152, UC-169, or UC-170). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | None | | |

#### 3.2.4.22. Submit Catering Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-156 - Submit Catering Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Email Service |
| Trigger | Service Caterers actor requests to submit a catering application. | | |
| Description | Enables the Service Caterers to submit company profiles, safety certifications, and kitchen capability details to apply for school catering contracts. | | |
| Preconditions | The Service Caterers representative is authenticated, logged in, and does not have an active application. | | |
| Postconditions | The application is created and saved in the database with status 'Pending Approval'. The School Administration is notified and a confirmation email is sent. | | |
| Normal Flow | 1. Caterer navigates to the "Apply for Onboarding" interface. 2. System displays the application form requesting company details (name, tax ID, address), kitchen sanitation details, and food safety certifications. 3. Caterer inputs details and uploads food safety certificates. 4. Caterer clicks "Submit". 5. System validates the inputs (e.g., non-empty fields, valid tax ID format per MSG-007, and non-expired certificates per BR-046). 6. System saves the application with status 'Pending Approval' and logs the event. 7. System sends a confirmation email to the caterer (BR-010) and notifies the School Administration. 8. System displays a success message (MSG-028). | | |
| Alternative Flows | AF1 - Expired/Invalid Certificates: One or more uploaded food safety certificates have expired (BR-048). System displays MSG-013 and blocks submission. | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has a stable internet connection and necessary documents. Site inspection rating will be required later. | | |

#### 3.2.4.23. Track Catering Application Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-157 - Track Catering Application Status | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to check the status of their catering application. | | |
| Description | Allows the Service Caterers to view the current status of their onboarding application, comments from the School Admin, and site inspection rating. | | |
| Preconditions | The Service Caterers representative is authenticated and has previously submitted an onboarding application. | | |
| Postconditions | System retrieves and displays the current application status, comments, and site inspection score. | | |
| Normal Flow | 1. Caterer navigates to the "Application Status" section. 2. System retrieves the caterer's application record from the database. 3. System displays the current status (e.g., Pending, Approved, Rejected, Action Required), site inspection score (if conducted), and comments from the School Administration. 4. Caterer views the details. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | — | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

#### 3.2.4.24. Update/Resubmit Catering Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-158 - Update/Resubmit Catering Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Email Service |
| Trigger | Service Caterers requests to update and resubmit a rejected or incomplete application. | | |
| Description | Enables the Service Caterers to update application details, fix missing documents, and resubmit the application for School Admin evaluation. | | |
| Preconditions | The Service Caterers representative is authenticated and their application status is 'Rejected' or 'Action Required'. | | |
| Postconditions | The application is updated, status is reset to 'Pending Approval', and School Admin is notified. | | |
| Normal Flow | 1. Caterer navigates to the "Application Status" section and clicks "Edit/Update Application". 2. System displays the form pre-filled with previous inputs and displays comments from the School Admin. 3. Caterer updates the required fields or uploads new files. 4. Caterer clicks "Resubmit". 5. System validates the updated inputs. 6. System updates the database record with the new details and resets status to 'Pending Approval'. 7. System sends a notification email to the caterer (BR-010) and notifies the School Admin. 8. System displays a success message (MSG-029). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has corrected the documents as requested by the School Administration. | | |

#### 3.2.4.25. Manage Catering Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-159 - Manage Catering Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers logs in and accesses their application dashboard. | | |
| Description | The main dashboard use case that allows the Service Caterers to manage their onboarding application flow, directing them to submit, track, or update their application. | | |
| Preconditions | The Service Caterers representative is authenticated and logged in. | | |
| Postconditions | The system presents options corresponding to the current state of their application. | | |
| Normal Flow | 1. Caterer navigates to the "Catering Onboarding Portal".  2. System checks the state of the caterer's application in the database.  3. System displays available actions based on application state: "Submit Application" (if no application exists), "View Application Status" (if an application is pending or approved), or "Edit and Resubmit Application" (if rejected or action required).  4. Caterer selects an option.  5. System routes the user to the corresponding use case (UC-156, UC-157, or UC-158). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | None | | |

