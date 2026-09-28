# Use Case Descriptions — Bổ sung (Missing Use Cases)

**Tài liệu bổ sung cho Report 3 — Software Requirement Specification**
Mỗi thao tác (create, view list, view detail, update, delete, confirm…) được mô tả thành một UC riêng biệt, theo đúng phong cách Report 3.

---

## Mục lục

### MF07 — Food Safety Inspection & Monitoring
- [UC-176 — Submit Kitchen Inspection](#uc-176)
- [UC-177 — Confirm Kitchen Inspection](#uc-177)
- [UC-178 — Update Kitchen Inspection Record](#uc-178)
- [UC-179 — Upload Kitchen Inspection Evidence Photo](#uc-179)
- [UC-180 — View Kitchen Inspection List](#uc-180)
- [UC-181 — Create Daily Safety Signoff](#uc-181)
- [UC-182 — View Safety Signoff History](#uc-182)

### MF06 — Ingredient & Inventory Management
- [UC-183 — View Ingredient Traceability Map](#uc-183)
- [UC-184 — View Inventory Transaction Log](#uc-184)
- [UC-185 — Create Manual Inventory Adjustment](#uc-185)
- [UC-186 — Create Supplier Product](#uc-186)
- [UC-187 — Update Supplier Product](#uc-187)
- [UC-188 — View Supplier Product List](#uc-188)

### MF11 — Incident & Emergency Management
- [UC-189 — Submit Food Safety Concern Report](#uc-189)
- [UC-190 — Upload Evidence Photo to Source Report](#uc-190)
- [UC-191 — Close Source Report (Not Food Safety)](#uc-191)
- [UC-192 — View Source Report List](#uc-192)
- [UC-193 — Create Incident Investigation Record](#uc-193)
- [UC-194 — Update Incident Investigation Record](#uc-194)
- [UC-195 — Finalize Incident Investigation Record](#uc-195)
- [UC-196 — Open Investigation from Incident Record](#uc-196)
- [UC-197 — Close Incident Record (Not Food Safety)](#uc-197)
- [UC-198 — View Incident Record List](#uc-198)

### MF05 — Menu & Diet Management
- [UC-199 — View Diet Group List](#uc-199)
- [UC-200 — Create Menu Template](#uc-200)
- [UC-201 — Update Menu Template](#uc-201)
- [UC-202 — Delete Menu Template](#uc-202)
- [UC-203 — View Menu Template List](#uc-203)
- [UC-204 — View Menu Template Detail](#uc-204)

### MF02 — School & Class Management
- [UC-205 — Create Grade](#uc-205)
- [UC-206 — Update Grade](#uc-206)
- [UC-207 — Deactivate Grade](#uc-207)
- [UC-208 — View Grade List](#uc-208)
- [UC-209 — View Nutrition Target List](#uc-209)
- [UC-210 — Update Nutrition Target](#uc-210)

### MF13 — Reporting & Analytics
- [UC-211 — View Report Export History](#uc-211)
- [UC-212 — Download Report Export File](#uc-212)
- [UC-213 — Create Report Schedule](#uc-213)
- [UC-214 — Update Report Schedule](#uc-214)
- [UC-215 — Delete Report Schedule](#uc-215)
- [UC-216 — View Report Schedule List](#uc-216)
- [UC-217 — Run Report Schedule Now](#uc-217)

### MF03 — Student & Parent Management
- [UC-218 — View Student Health File](#uc-218)
- [UC-219 — Update Student Health File](#uc-219)

### MF10 — Feedback & Notification
- [UC-220 — Register Device Token](#uc-220)
- [UC-221 — Unregister Device Token](#uc-221)

---

## MF07 — Food Safety Inspection & Monitoring

---

### UC-176 — Submit Kitchen Inspection {#uc-176}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-176 — Submit Kitchen Inspection | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff navigates to the Kitchen Inspections section and selects "New Inspection". | | |
| Description | Enables the Medical Staff to record a daily kitchen hygiene inspection by completing a configurable checklist (temperature control, pest-free status, equipment cleanliness, staff hygiene) and submitting the result with evidence photos. The system calculates a hygiene score and issues a warning if the score falls below 60%. | | |
| Preconditions | Medical Staff is authenticated and authorized. A school kitchen facility exists for the affiliated caterer. The same kitchen has not already been submitted for inspection on the same date in the same session. | | |
| Postconditions | A kitchen inspection record is created with status SUBMITTED. The hygiene score is calculated (BR-100, BR-104). If score < 60%, system generates a warning notification (MSG-093). The caterer is notified for two-party confirmation. | | |
| Normal Flow | 1. Medical Staff opens the New Inspection form. 2. System displays the checklist grouped by category. 3. Medical Staff completes each checklist item (observed values, pass/fail). 4. Medical Staff uploads at least one evidence photo per item (BR-207). System records GPS and timestamp automatically. 5. Medical Staff reviews the form and clicks "Submit". 6. System calculates the hygiene score and saves the inspection record (MSG-043). 7. If score < 60%, system displays MSG-093 warning. 8. System notifies the affiliated Service Caterers for confirmation. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. INSP-016 (MSG-016): Submission blocked if mandatory evidence photos are missing (BR-207). | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-100, BR-101, BR-102, BR-103, BR-104, BR-207, BR-221 | | |
| Other Information | Linked to UC-041 (Manage Food Safety Incidents) when hygiene failure triggers escalation. | | |
| Assumptions | User has a running environment with internet connection. Camera access is available for photo capture. | | |

**Table Add-1:** Submit Kitchen Inspection Use Cases Specification

---

### UC-177 — Confirm Kitchen Inspection {#uc-177}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-177 — Confirm Kitchen Inspection | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers receives a notification that a kitchen inspection record awaits their two-party confirmation. | | |
| Description | Enables the Service Caterers to review and confirm a kitchen inspection record created by Medical Staff. Confirmation represents the caterer's formal acknowledgement of the inspection findings. Once confirmed, the inspection record is permanently locked and cannot be further edited by Medical Staff (BR-221). | | |
| Preconditions | Service Caterers is authenticated and authorized. An inspection record with status SUBMITTED exists for their affiliated kitchen and is awaiting caterer confirmation. | | |
| Postconditions | The inspection record status transitions to CONFIRMED. The record is locked (no further edits allowed). An audit log entry is created with the caterer actor and timestamp. | | |
| Normal Flow | 1. Service Caterers opens the notification or navigates to Kitchen Inspections. 2. System displays the inspection record details, checklist results, hygiene score, and evidence photos. 3. Service Caterers reviews the findings. 4. Service Caterers clicks "Confirm". 5. System locks the inspection record and transitions status to CONFIRMED. 6. System records the confirmation event in the audit log. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. INSP-STEP_LOCKED: Record has already been confirmed; duplicate confirmation is rejected. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-221 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-2:** Confirm Kitchen Inspection Use Cases Specification

---

### UC-178 — Update Kitchen Inspection Record {#uc-178}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-178 — Update Kitchen Inspection Record | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff selects an existing kitchen inspection record (status SUBMITTED) and chooses "Edit Record". | | |
| Description | Enables Medical Staff to correct or supplement the written content (record narrative) of a kitchen inspection that has been submitted but not yet confirmed by the Service Caterers. Once the caterer has confirmed the record, no further edits are permitted. | | |
| Preconditions | Medical Staff is authenticated and authorized. The target inspection record exists with status SUBMITTED (not yet confirmed by caterers). | | |
| Postconditions | The inspection record's written content is updated and saved. The hygiene score is not recalculated by this update (score is set at submission). | | |
| Normal Flow | 1. Medical Staff opens the submitted inspection record. 2. Medical Staff selects "Edit Record". 3. System displays the editable record narrative field. 4. Medical Staff updates the narrative content. 5. Medical Staff saves the changes. 6. System saves the updated record content and returns the updated inspection response. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. INSP-STEP_LOCKED: Update rejected if the record has already been confirmed by Service Caterers (BR-221). INSP-404 (MSG-137): Inspection record not found. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-221 | | |
| Other Information | Only the written narrative (record content) is editable via this UC. Checklist scores and evidence photos are fixed at submission time. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-3:** Update Kitchen Inspection Record Use Cases Specification

---

### UC-179 — Upload Kitchen Inspection Evidence Photo {#uc-179}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-179 — Upload Kitchen Inspection Evidence Photo | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff selects a checklist item within an open inspection session and taps the camera icon to capture a photo. | | |
| Description | Enables Medical Staff to upload photographic evidence directly linked to a specific checklist item within an active kitchen inspection. Each photo is attached to the item code (not to the overall session), ensuring traceability per inspection criterion. GPS metadata is automatically attached to each photo. | | |
| Preconditions | Medical Staff is authenticated and authorized. The kitchen inspection session exists. The target checklist item code exists within the session. The session has not yet been confirmed by caterers. | | |
| Postconditions | The evidence photo is uploaded and stored. The photo is linked to the specific checklist item of the inspection session. GPS coordinates and device timestamp are recorded. The photo appears in the evidence list for the inspection detail view. | | |
| Normal Flow | 1. Medical Staff opens an active inspection session. 2. Medical Staff selects a specific checklist item. 3. Medical Staff taps "Capture Photo" and takes a photo via the device camera. 4. Application sends the photo with GPS coordinates to the system. 5. System validates the file (type: image; size ≤ 10 MB). 6. System stores the photo and returns the evidence metadata (evidence ID, URL, item code, GPS, timestamp). 7. Photo appears as a thumbnail linked to the checklist item. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-008: File size exceeds 10 MB. MSG-009: Unsupported file type (only JPG and PNG allowed). INSP-404 (MSG-137): Inspection session or checklist item not found. | | |
| Priority | High | | |
| Frequency of Use | Daily (multiple times per inspection session) | | |
| Business Rules | BR-107, BR-207 | | |
| Other Information | Photos are attached per checklist item (D10-BIS), not to the overall session. This distinguishes kitchen inspection evidence from other evidence uploads in the system. | | |
| Assumptions | Device camera access is granted. GPS is enabled on the device. | | |

**Table Add-4:** Upload Kitchen Inspection Evidence Photo Use Cases Specification

---

### UC-180 — View Kitchen Inspection List {#uc-180}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-180 — View Kitchen Inspection List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | School Administration, Service Caterers, System Admin |
| Trigger | Actor navigates to the Kitchen Inspections section. | | |
| Description | Enables authorized users to view the paginated list of kitchen inspection records for their affiliated school, with filtering by date range, hygiene score range, or confirmation status. School Administration and System Admin have broader scoping access. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | Kitchen inspection records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Kitchen Inspections. 2. System displays the paginated list of inspection records: date, hygiene score, status (SUBMITTED / CONFIRMED), and Medical Staff name. 3. Actor can filter by date range, status, or hygiene score range. 4. Actor selects a record to view its full detail (checklist results, evidence photos, record narrative). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. INSP-404 (MSG-137): Requested record not found. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-100, BR-101 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-5:** View Kitchen Inspection List Use Cases Specification

---

### UC-181 — Create Daily Safety Signoff {#uc-181}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-181 — Create Daily Safety Signoff | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration reviews the day's inspection results and selects "Sign Off for Today". | | |
| Description | Enables School Administration to formally sign off on the daily food safety status of the kitchen after reviewing the completed inspection records. The signoff is the school's formal daily attestation that food operations on that date met safety requirements. The system enforces one signoff per school per calendar day. | | |
| Preconditions | School Administration is authenticated and authorized. At least one kitchen inspection record for the current day has been confirmed by caterers. No signoff has yet been created for this school on today's date. | | |
| Postconditions | A safety signoff record is immutably created, linked to the actor and the school with the current timestamp. | | |
| Normal Flow | 1. School Administration navigates to Safety Signoff. 2. System displays today's inspection summary (hygiene score, three-step inspection status, active alerts). 3. School Administration optionally adds remarks. 4. School Administration clicks "Sign Off". 5. System verifies no signoff exists for this school today. 6. System creates the signoff record and returns a success response. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. INSP-409: A signoff for this school today already exists; the second attempt is rejected. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-100, BR-101, BR-221 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-6:** Create Daily Safety Signoff Use Cases Specification

---

### UC-182 — View Safety Signoff History {#uc-182}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-182 — View Safety Signoff History | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration navigates to the Safety Signoff History section. | | |
| Description | Enables School Administration to view the historical list of daily safety signoffs for their school, filterable by date range, to verify compliance coverage. | | |
| Preconditions | School Administration is authenticated and authorized. | | |
| Postconditions | Signoff records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to Safety Signoff History. 2. System displays the paginated list of signoff records: date, actor name, and optional remarks. 3. Actor applies date range filter as needed. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-100 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-7:** View Safety Signoff History Use Cases Specification

---

## MF06 — Ingredient & Inventory Management

---

### UC-183 — View Ingredient Traceability Map {#uc-183}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-183 — View Ingredient Traceability Map | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | School Administration, Service Caterers |
| Trigger | Actor selects a dish on a specific service date and requests ingredient traceability. | | |
| Description | Enables authorized users to trace, for any given dish on a specific service date and meal type, the complete chain of ingredient supplier batches used in meal preparation. The result links the cooked dish back to each ingredient batch, receiving session, supplier, and compliance documents. Critical tool during food safety incident investigations (BR-151). | | |
| Preconditions | Actor is authenticated and authorized. Meal service records exist for the specified dish and service date. | | |
| Postconditions | The traceability map is returned. No data is modified. | | |
| Normal Flow | 1. Actor opens the Traceability query screen (or opens from inside an Incident record). 2. Actor enters: dish name, service date, meal type (Breakfast / Lunch / Dinner). 3. System queries the traceability chain: dish → ingredient list → issued batch IDs → receiving sessions → supplier → compliance certificates. 4. System displays the traceability map. 5. Actor can expand each batch to view delivery person, GPS coordinates, and receiving photos. 6. Actor exports the map to PDF for regulatory or incident documentation. | | |
| Alternative Flows | AF1 — From Incident: Medical Staff opens the traceability map from an active incident record. Dish and date are pre-populated. | | |
| Exceptions | MSG-004, MSG-005. If no batch is linked to the dish and date, system displays an informational message indicating incomplete traceability data. | | |
| Priority | High | | |
| Frequency of Use | On-demand (incident investigations), weekly (compliance monitoring) | | |
| Business Rules | BR-076, BR-151 | | |
| Other Information | BR-151 requires the system to automatically compile the traceability map within 4 hours of an incident report. This UC supports the manual on-demand query. | | |
| Assumptions | User has a running environment with internet connection. Ingredient batch IDs were linked at the time of meal production. | | |

**Table Add-8:** View Ingredient Traceability Map Use Cases Specification

---

### UC-184 — View Inventory Transaction Log {#uc-184}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-184 — View Inventory Transaction Log | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | School Administration, Medical Staff, System Admin |
| Trigger | Actor navigates to the Inventory Transaction Log section. | | |
| Description | Enables authorized users to query and review the append-only inventory transaction ledger, which records every stock movement: receiving, issue-to-kitchen, manual adjustment, and disposal. The ledger is immutable — PUT and DELETE operations are explicitly blocked by the system. | | |
| Preconditions | Actor is authenticated and authorized. The inventory system is active for the affiliated school. | | |
| Postconditions | Transaction records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to the Inventory Transaction Log. 2. System displays the paginated list: transaction type (RECEIVE / ISSUE / ADJUST / DISPOSE), ingredient name, batch ID, quantity change (±), actor, and timestamp. 3. Actor filters by date range, ingredient, transaction type, or batch ID as needed. 4. Actor can export the filtered log to Excel for audit purposes. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. ING-405 (405 Method Not Allowed): Any attempt to update or delete an existing transaction record is permanently blocked. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-077, BR-082, BR-083 | | |
| Other Information | ADR-M06-1 (append-only ledger): PUT and DELETE on existing transaction records are permanently blocked at the API level with an explicit 405 response. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-9:** View Inventory Transaction Log Use Cases Specification

---

### UC-185 — Create Manual Inventory Adjustment {#uc-185}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-185 — Create Manual Inventory Adjustment | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers discovers a discrepancy between the physical stock count and the system record and selects "Manual Adjustment". | | |
| Description | Enables Service Caterers to correct the inventory balance by appending a manual adjustment transaction to the ledger when the physical count differs from the system record. A mandatory reason must be provided. The adjustment is appended (never modifies existing records) in compliance with the append-only ledger principle. | | |
| Preconditions | Service Caterers is authenticated and authorized. The target ingredient batch exists. | | |
| Postconditions | A new ADJUST transaction record is appended to the inventory ledger with the actor identity, timestamp, ingredient, quantity delta, and reason. The effective stock level is updated accordingly. | | |
| Normal Flow | 1. Service Caterers opens "Manual Adjustment" within the Inventory Transaction section. 2. System displays the adjustment form: ingredient, quantity delta (positive for surplus, negative for deficit), and mandatory reason field. 3. Service Caterers fills in all fields and submits. 4. System validates: quantity delta would not result in a negative stock balance (BR-204). 5. System appends the ADJUST record to the ledger and returns MSG-042. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. MSG-018: Adjustment rejected because resulting stock would be negative. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand (stocktaking, physical count reconciliation) | | |
| Business Rules | BR-077, BR-204 | | |
| Other Information | Manual adjustment does not modify any existing transaction record. It appends a new row, preserving the full audit trail. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-10:** Create Manual Inventory Adjustment Use Cases Specification

---

### UC-186 — Create Supplier Product {#uc-186}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-186 — Create Supplier Product | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Supplier navigates to My Products and selects "Add Product". | | |
| Description | Enables the Raw Food Supplier to declare a new ingredient they supply, along with unit price and packaging specification. The ingredient must be selected from the system-wide ingredient catalog (BR-080). The product becomes visible to Service Caterers and School Administration for purchase request drafting. | | |
| Preconditions | Supplier is authenticated and has an approved partnership with at least one school. The ingredient exists in the system catalog (BR-080). | | |
| Postconditions | A supplier product record is created and visible in the shared product catalog for Service Caterers and School Administration. | | |
| Normal Flow | 1. Supplier selects "Add Product". 2. System displays the creation form: ingredient (from system catalog), unit of measure, price per unit, packaging specification. 3. Supplier completes all fields and submits. 4. System validates the ingredient exists (BR-080) and no duplicate exists (same supplier + ingredient). 5. System creates the product record and returns MSG-031. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Conflict (409): Duplicate product (same supplier, same ingredient) already exists. Validation error: Ingredient not found in the system catalog. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-080 | | |
| Other Information | Requires UC-080 (Submit Supplier Application) to have been approved first. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-11:** Create Supplier Product Use Cases Specification

---

### UC-187 — Update Supplier Product {#uc-187}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-187 — Update Supplier Product | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Supplier selects an existing product from My Products and chooses "Edit". | | |
| Description | Enables the Raw Food Supplier to update the unit price or packaging specification of an existing declared product. Changes do not retroactively affect purchase requests that have already been created. | | |
| Preconditions | Supplier is authenticated. The product exists and belongs to this supplier. | | |
| Postconditions | The product record is updated with the new price/specification. Existing purchase requests referencing this product are not changed. | | |
| Normal Flow | 1. Supplier selects a product from My Products. 2. Supplier edits the unit price and/or packaging specification. 3. Supplier saves the changes. 4. System validates and updates the record. System returns the updated product. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Not found (404): Product does not exist or does not belong to this supplier. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-12:** Update Supplier Product Use Cases Specification

---

### UC-188 — View Supplier Product List {#uc-188}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-188 — View Supplier Product List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Service Caterers, School Administration, System Admin, Parent |
| Trigger | Actor navigates to the Supplier Products section or the system invokes the product list during purchase request creation. | | |
| Description | Enables authorized users to view the list of declared supplier products. Suppliers see only their own products. Service Caterers, School Administration, and System Admin see all products across suppliers to support purchase request drafting. A separate sourcing endpoint allows Parents to view only the supplier name and ingredient name (no price) for nutritional transparency. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | Supplier product records are retrieved and displayed according to the actor's role-based scope. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Supplier Products. 2. System returns the paginated product list scoped by role: Supplier → own products only; Service Caterers / School Admin / System Admin → all products with unit prices. 3. Actor applies filters (ingredient, supplier, price range) as needed. 4. (Parent only) Parent accesses the read-only sourcing endpoint: system returns ingredient name + cheapest active supplier name only (no unit price). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-076, BR-080 | | |
| Other Information | The sourcing endpoint (`GET /supplier-products/sourcing`) is restricted to return only supplier name and ingredient — no commercial price data — for Parent role. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-13:** View Supplier Product List Use Cases Specification

---

## MF11 — Incident & Emergency Management

---

### UC-189 — Submit Food Safety Concern Report {#uc-189}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-189 — Submit Food Safety Concern Report | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Teacher, Parent | Secondary Actors | Medical Staff |
| Trigger | Teacher or Parent observes a student showing symptoms potentially linked to a school meal and selects "Report Concern" in the mobile app. | | |
| Description | Enables the Teacher or Parent to submit an initial food safety concern report (Source Report) when a student shows symptoms possibly linked to a school meal. The report captures student identity, observed symptoms, time of onset, and suspected meal. Medical Staff receives a triage notification. If 3 or more similar reports arrive within 24 hours, the system triggers a cluster alert (BR-146). | | |
| Preconditions | Actor is authenticated. The student being reported is in the actor's class (Teacher) or is linked to the actor's account (Parent). | | |
| Postconditions | The Source Report is saved with status PENDING_TRIAGE. Medical Staff is notified. If the cluster threshold is reached, a cluster signal alert is generated and Medical Staff is alerted. | | |
| Normal Flow | 1. Actor navigates to "Report Food Safety Concern". 2. System displays the report form: student (pre-filled for Parent; class-list for Teacher), symptoms (multi-select), time of onset, suspected meal/dish, and description. 3. Actor fills in required fields and submits. 4. System saves the report with status PENDING_TRIAGE and notifies Medical Staff. 5. System evaluates the 24-hour cluster threshold (BR-146). If ≥ 3 similar reports exist, system fires a cluster signal alert to Medical Staff. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. MSG-023: Required fields (symptoms, date, suspected dish) are incomplete. MSG-005 (Access denied): Student not linked to the actor. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-146, BR-147 | | |
| Other Information | This is the entry point for Workflow 11 (Food Safety Incident Handling). Corresponds to UC-073 (Parent) and UC-074 (Teacher) from Report 3 with combined description. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-14:** Submit Food Safety Concern Report Use Cases Specification

---

### UC-190 — Upload Evidence Photo to Source Report {#uc-190}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-190 — Upload Evidence Photo to Source Report | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Teacher, Parent | Secondary Actors | None |
| Trigger | Actor opens an existing source report they submitted and selects "Add Evidence Photo". | | |
| Description | Enables the Teacher or Parent who submitted a Source Report to attach one or more photographic evidence files (meal photos, packaging, visible symptoms) to support the report. Only the original submitter may upload evidence to their report. Photos are stored securely and accessible to Medical Staff during triage. | | |
| Preconditions | Actor is authenticated. The source report exists and was submitted by this actor. The report is not yet closed. | | |
| Postconditions | The evidence photos are uploaded and linked to the source report. Medical Staff can view the photos during triage. | | |
| Normal Flow | 1. Actor opens their submitted source report. 2. Actor taps "Add Evidence Photos" and selects or captures one or more photos. 3. Application sends the photos to the system. 4. System validates each file (type and size). 5. System stores the photos and links them to the report. System returns updated report with evidence list. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-008: File exceeds 10 MB. MSG-009: Unsupported file type. MSG-005 (Access denied): Actor is not the original submitter. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-146 | | |
| Other Information | Evidence photo download requires authentication (any role with access to the report) and is served through a secured endpoint. | | |
| Assumptions | User has a running environment with internet connection. Camera or file access is available. | | |

**Table Add-15:** Upload Evidence Photo to Source Report Use Cases Specification

---

### UC-191 — Close Source Report (Not Food Safety) {#uc-191}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-191 — Close Source Report (Not Food Safety Related) | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff reviews a PENDING_TRIAGE source report and determines it is unrelated to food safety. | | |
| Description | Enables Medical Staff to triage a Source Report and close it as not food-safety-related after clinical review. Closing removes it from the active triage queue. The report record is retained for audit purposes. | | |
| Preconditions | Medical Staff is authenticated. The source report exists with status PENDING_TRIAGE. | | |
| Postconditions | The source report status transitions to NOT_FOOD_SAFETY. The report is removed from the triage queue. The submitter (Teacher/Parent) is notified of the closure. | | |
| Normal Flow | 1. Medical Staff opens the source report from the triage queue. 2. Medical Staff reviews the reported symptoms, student information, and any attached evidence. 3. Medical Staff selects "Close — Not Food Safety Related". 4. System transitions the report status to NOT_FOOD_SAFETY. 5. System notifies the original submitter that the report has been reviewed and closed. | | |
| Alternative Flows | AF1 — Escalate Instead: Medical Staff determines the report does require investigation and creates an Incident Investigation Record (UC-193) from it, transitioning status to LINKED_TO_INCIDENT. | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-147 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-16:** Close Source Report (Not Food Safety) Use Cases Specification

---

### UC-192 — View Source Report List {#uc-192}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-192 — View Source Report List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | School Administration, Teacher, System Admin |
| Trigger | Actor navigates to the Source Reports (Food Safety Concern Reports) section. | | |
| Description | Enables authorized users to view the list of food safety concern reports. Medical Staff sees all reports for their affiliated school. Teachers see only reports they submitted or reports from their class. School Administration sees all reports for their school. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | Source report records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Source Reports. 2. System displays the paginated list: reporter name, student, reported symptoms summary, submission timestamp, and status (PENDING_TRIAGE / LINKED_TO_INCIDENT / NOT_FOOD_SAFETY). 3. Actor filters by status, date, or class. 4. Actor selects a report to view full details. | | |
| Alternative Flows | AF1 — View Cluster Signals: Medical Staff accesses the cluster signal summary to see symptom clusters that have crossed the 24-hour threshold (BR-146) but have not yet been manually reviewed. | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-17:** View Source Report List Use Cases Specification

---

### UC-193 — Create Incident Investigation Record {#uc-193}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-193 — Create Incident Investigation Record | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff selects one or more PENDING_TRIAGE Source Reports from the triage queue and clicks "Create Investigation Record". | | |
| Description | Enables Medical Staff to formally consolidate one or more food safety concern reports into a structured Incident Investigation Record (biên bản sự cố). The record pre-populates from the linked source reports (affected students, symptoms, onset time) and is supplemented with Medical Staff's clinical assessment. | | |
| Preconditions | Medical Staff is authenticated and authorized. At least one Source Report with status PENDING_TRIAGE exists. | | |
| Postconditions | A new Incident Investigation Record is created with status DRAFT, linked to the selected source reports. The source reports' status transitions to LINKED_TO_INCIDENT. | | |
| Normal Flow | 1. Medical Staff opens the triage queue and selects one or more PENDING_TRIAGE reports. 2. Medical Staff clicks "Create Investigation Record". 3. System creates a new Incident Record pre-populated from the reports: affected student list, symptoms, onset time, suspected dishes. 4. Medical Staff supplements the record with clinical notes and preliminary assessment. 5. Medical Staff saves the draft record (MSG-100). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. ADR-M11-1: A Source Report that is already LINKED_TO_INCIDENT cannot be selected again; the system returns a conflict error. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-146, BR-147, BR-150 | | |
| Other Information | The Incident Investigation Record is the clinical document. The Incident (UC-041) is the operational management case. Both are required for a complete food safety incident lifecycle. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-18:** Create Incident Investigation Record Use Cases Specification

---

### UC-194 — Update Incident Investigation Record {#uc-194}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-194 — Update Incident Investigation Record | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff opens an existing Incident Record in DRAFT status and selects "Edit". | | |
| Description | Enables Medical Staff to iteratively update an Incident Investigation Record while the clinical investigation is ongoing. Updates may include additional clinical notes, new affected students, updated suspected dishes, or lab result references. | | |
| Preconditions | Medical Staff is authenticated. The Incident Record exists with status DRAFT or IN_PROGRESS (not yet finalized). | | |
| Postconditions | The Incident Record is updated and saved. Status remains in the current non-final state. | | |
| Normal Flow | 1. Medical Staff opens the Incident Record. 2. Medical Staff edits the desired fields. 3. Medical Staff saves. System updates the record and returns the updated response. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. If the record has already been finalized (status COMPLETED), the update is rejected. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-150, BR-154 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-19:** Update Incident Investigation Record Use Cases Specification

---

### UC-195 — Finalize Incident Investigation Record {#uc-195}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-195 — Finalize Incident Investigation Record | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff determines that the clinical investigation is complete and clicks "Finalize Record". | | |
| Description | Enables Medical Staff to finalize an Incident Investigation Record, marking the clinical investigation as complete. A finalized record is a prerequisite for opening a formal Incident investigation (UC-196). Once finalized, the record transitions to COMPLETED status and can no longer be edited. | | |
| Preconditions | Medical Staff is authenticated. The Incident Record exists in DRAFT or IN_PROGRESS status and all mandatory fields are completed. | | |
| Postconditions | The Incident Record status transitions to COMPLETED (MSG-101). No further edits are permitted on the record. | | |
| Normal Flow | 1. Medical Staff opens the Incident Record and reviews all fields. 2. Medical Staff clicks "Finalize Record". 3. System validates all mandatory fields are present (symptoms, onset date, suspected dishes, clinical assessment). 4. System transitions status to COMPLETED and saves. System returns MSG-101. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Validation error: mandatory fields are missing. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-150, BR-154 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-20:** Finalize Incident Investigation Record Use Cases Specification

---

### UC-196 — Open Investigation from Incident Record {#uc-196}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-196 — Open Investigation from Incident Record | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | School Administration |
| Trigger | Medical Staff finalizes an Incident Record and selects "Open Full Investigation". | | |
| Description | Enables Medical Staff to escalate a finalized Incident Investigation Record into a full operational food safety Incident (UC-041). The system creates an Incident entity pre-populated from the record's data and returns the new incident code for continued management. | | |
| Preconditions | Medical Staff is authenticated. The Incident Investigation Record exists with status COMPLETED. | | |
| Postconditions | A new Incident entity is created and its code is returned. The Incident is now managed through UC-041 (Manage Food Safety Incidents). School Administration is notified of the new incident. | | |
| Normal Flow | 1. Medical Staff opens a COMPLETED Incident Record. 2. Medical Staff selects "Open Full Investigation". 3. Actor enters the severity classification and initial notes for the Incident. 4. System creates the Incident, linked to the Record, and returns the incident code. 5. System notifies School Administration and triggers the appropriate alert chain per severity (BR-148). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Record must be in COMPLETED status; otherwise the action is blocked. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-147, BR-148, BR-149, BR-150 | | |
| Other Information | After this step, the investigation continues via UC-041 (Manage Food Safety Incidents), UC-077 (Manage CAPA), and UC-154 (Generate Formal Incident Report). | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-21:** Open Investigation from Incident Record Use Cases Specification

---

### UC-197 — Close Incident Record (Not Food Safety) {#uc-197}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-197 — Close Incident Record (Not Food Safety Related) | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff determines during investigation that the Incident Record does not concern food safety and closes it. | | |
| Description | Enables Medical Staff to close an Incident Investigation Record as not food-safety-related, with an optional closing note. The record status transitions to CLOSED_NOT_FOOD_SAFETY and is removed from the active investigation queue. Linked Source Reports are updated accordingly. | | |
| Preconditions | Medical Staff is authenticated. The Incident Record exists and is not yet finalized or linked to a formal Incident. | | |
| Postconditions | The Incident Record status transitions to CLOSED_NOT_FOOD_SAFETY. Linked Source Reports are updated to NOT_FOOD_SAFETY. | | |
| Normal Flow | 1. Medical Staff opens the Incident Record. 2. Medical Staff selects "Close — Not Food Safety". 3. Medical Staff optionally enters a closing note. 4. System transitions record and linked source reports to CLOSED/NOT_FOOD_SAFETY. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-147 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-22:** Close Incident Record (Not Food Safety) Use Cases Specification

---

### UC-198 — View Incident Record List {#uc-198}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-198 — View Incident Record List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | School Administration |
| Trigger | Actor navigates to the Incident Records section. | | |
| Description | Enables Medical Staff and School Administration to view and filter the list of Incident Investigation Records for their school. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | Incident Records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Incident Records. 2. System displays the paginated list: record code, creation date, affected student count, status (DRAFT / COMPLETED / CLOSED_NOT_FOOD_SAFETY / LINKED_TO_INCIDENT), and Medical Staff name. 3. Actor filters by date, status, or student. 4. Actor selects a record to view full details. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-154 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-23:** View Incident Record List Use Cases Specification

---

## MF05 — Menu & Diet Management

---

### UC-199 — View Diet Group List {#uc-199}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-199 — View Diet Group List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Medical Staff, School Administration, Teacher, Parent |
| Trigger | Actor accesses a screen that requires diet group selection (e.g., Create Menu, Create Menu Template, student allergy profile). | | |
| Description | Enables authorized users to view the system-wide catalog of special dietary groups (e.g., Vegetarian, Halal, Dairy-Free, Low-Sugar, Gluten-Free). The catalog is a global reference maintained by the operations/medical team at database level. It is read-only for all application users. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | The diet group list is returned. No data is modified. | | |
| Normal Flow | 1. System automatically calls the diet group endpoint when a screen requiring diet group data is loaded. 2. System returns the list: code, display name, description. 3. Actor selects or references the applicable diet group in the operation at hand. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Low | | |
| Frequency of Use | Frequent (auto-invoked during menu, template, and health profile workflows) | | |
| Business Rules | BR-065 | | |
| Other Information | Teacher and Parent read access was added on 07/08/2026. New diet groups are added by operations staff at the database level, not through any UI UC. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-24:** View Diet Group List Use Cases Specification

---

### UC-200 — Create Menu Template {#uc-200}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-200 — Create Menu Template | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers navigates to Menu Templates and selects "Create New Template". | | |
| Description | Enables Service Caterers to create a reusable weekly menu template defining dish assignments for each day and meal session, optionally tagged with diet groups. Templates serve as starting points when drafting new menus (UC-020), reducing repetitive data entry. | | |
| Preconditions | Service Caterers is authenticated and authorized. Referenced dishes exist in the dish catalog. | | |
| Postconditions | A new menu template record is created with status active. The template appears in the template list for future menu drafting. | | |
| Normal Flow | 1. Service Caterers opens the Create Template form. 2. Service Caterers enters: template name, applicable grade(s), and a weekly grid (days × meal sessions) with dish assignments and optional diet group tags. 3. Service Caterers saves the template. 4. System validates dish references and uniqueness (template name within school). 5. System creates the template record and returns HTTP 201. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. MENU-409 (conflict): Template with the same name already exists for this school. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-061, BR-062, BR-065, BR-069 | | |
| Other Information | Applying a template to a menu draft is handled within UC-020 (Create Menu) / UC-096 (Update Menu), not in this UC. | | |
| Assumptions | User has a running environment with internet connection. Required dishes are already registered. | | |

**Table Add-25:** Create Menu Template Use Cases Specification

---

### UC-201 — Update Menu Template {#uc-201}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-201 — Update Menu Template | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers selects an existing template from the Menu Templates list and clicks "Edit". | | |
| Description | Enables Service Caterers to update an existing menu template — name, dish assignments, diet group tags, or applicable grades. Changes do not cascade to menus already created from this template. A template can also be deactivated by setting `isActive: false`, removing it from the active template list without deleting it. | | |
| Preconditions | Service Caterers is authenticated and authorized. The template exists and belongs to this school. | | |
| Postconditions | The template record is updated. Menus previously created from this template are not affected. | | |
| Normal Flow | 1. Service Caterers opens the template detail and selects "Edit". 2. Service Caterers modifies the desired fields (name, dishes, diet groups, isActive). 3. Service Caterers saves. System validates and updates the record. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. MENU-404: Template not found. MENU-409: Updated name conflicts with an existing template. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-061, BR-065 | | |
| Other Information | Setting `isActive: false` hides the template from the active list (soft deactivation). Preferred over Delete per system design guidance. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-26:** Update Menu Template Use Cases Specification

---

### UC-202 — Delete Menu Template {#uc-202}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-202 — Delete Menu Template | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers selects a template from the list and chooses "Delete". | | |
| Description | Enables Service Caterers to permanently delete a menu template that is no longer needed. Menus previously created from this template are not affected. The system recommends using deactivation (UC-201, isActive: false) instead of deletion for templates that may be referenced historically. | | |
| Preconditions | Service Caterers is authenticated. The template exists and belongs to this school. | | |
| Postconditions | The template record is permanently deleted. Menus created from this template are not modified. | | |
| Normal Flow | 1. Service Caterers selects the template and clicks "Delete". 2. System prompts for confirmation. 3. Service Caterers confirms. 4. System permanently deletes the template record and returns `{ "ok": true }`. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. MENU-404: Template not found or does not belong to this school. | | |
| Priority | Low | | |
| Frequency of Use | On-demand (infrequent) | | |
| Business Rules | None additional. | | |
| Other Information | System design recommends `isActive: false` (UC-201) over Delete. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-27:** Delete Menu Template Use Cases Specification

---

### UC-203 — View Menu Template List {#uc-203}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-203 — View Menu Template List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | School Administration, System Admin |
| Trigger | Actor navigates to Menu Templates or the system invokes the template list during menu draft creation. | | |
| Description | Enables authorized users to view the paginated list of menu templates scoped to the current school, with optional filters. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | Menu templates are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Menu Templates. 2. System returns the paginated list: template name, applicable grade(s), status (Active / Inactive), and creation date. 3. Actor filters by grade or status as needed. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None additional. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-28:** View Menu Template List Use Cases Specification

---

### UC-204 — View Menu Template Detail {#uc-204}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-204 — View Menu Template Detail | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Service Caterers | Secondary Actors | School Administration, System Admin |
| Trigger | Actor selects a template from the Menu Template List and opens its detail view. | | |
| Description | Enables authorized users to view the complete detail of a menu template, including all dish assignments per day/session, diet group tags, and applicable grades. | | |
| Preconditions | Actor is authenticated and authorized. The template exists. | | |
| Postconditions | Template detail is retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor selects a template from the list. 2. System returns the full template detail: name, grade(s), weekly grid of dish assignments with diet groups, active status, and creation metadata. 3. Actor reviews the detail. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. MENU-404: Template not found. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | None additional. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-29:** View Menu Template Detail Use Cases Specification

---

## MF02 — School & Class Management

---

### UC-205 — Create Grade {#uc-205}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-205 — Create Grade | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration navigates to School Configuration → Grades and selects "Add Grade". | | |
| Description | Enables School Administration to define a new grade level (khối lớp) for their school (e.g., Khối 1, Khối 2). Grades are school-scoped and serve as the linking key between classrooms, menus, and nutrition targets. | | |
| Preconditions | School Administration is authenticated and authorized. The school record is active. | | |
| Postconditions | A new grade record is created and associated with the school. The grade becomes available for assignment to classrooms and menus. | | |
| Normal Flow | 1. School Administration opens "Add Grade". 2. System displays the form: grade name and display order. 3. School Administration enters values and saves. 4. System validates: name unique within school. 5. System creates the grade record (MSG-031). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. SCH-409: Grade name already exists within this school. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand (school setup, start of academic year) | | |
| Business Rules | BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-30:** Create Grade Use Cases Specification

---

### UC-206 — Update Grade {#uc-206}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-206 — Update Grade | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects an existing grade and chooses "Edit". | | |
| Description | Enables School Administration to update the name or display order of an existing grade level. The change is propagated to the grade's display in all related screens (classrooms, menus). | | |
| Preconditions | School Administration is authenticated. The grade exists and belongs to this school. | | |
| Postconditions | The grade record is updated. Existing classrooms and menus linked to this grade retain their references with the updated name. | | |
| Normal Flow | 1. School Administration selects the grade and opens "Edit". 2. School Administration changes the name and/or display order. 3. System validates uniqueness and saves the update. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. SCH-404: Grade not found or does not belong to this school. SCH-409: Updated name conflicts with an existing grade. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-024 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-31:** Update Grade Use Cases Specification

---

### UC-207 — Deactivate Grade {#uc-207}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-207 — Deactivate Grade | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects a grade and chooses "Deactivate". | | |
| Description | Enables School Administration to soft-deactivate a grade that is no longer in use (e.g., end of academic year restructuring). Deactivation sets `active = false`; the grade record is retained for historical references. This is not a permanent deletion. | | |
| Preconditions | School Administration is authenticated. The grade exists and belongs to this school. | | |
| Postconditions | The grade `active` flag is set to false. The grade no longer appears in the active grade selector but remains in historical records. | | |
| Normal Flow | 1. School Administration selects the grade and clicks "Deactivate". 2. System confirms the action. 3. System sets `active = false` and returns HTTP 204. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. SCH-404: Grade not found. | | |
| Priority | Low | | |
| Frequency of Use | On-demand (annual cycle) | | |
| Business Rules | BR-024 | | |
| Other Information | Soft-delete pattern — no hard deletion. Historical menu and classroom records retain the grade reference. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-32:** Deactivate Grade Use Cases Specification

---

### UC-208 — View Grade List {#uc-208}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-208 — View Grade List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | Service Caterers, Medical Staff, Teacher, Parent, System Admin |
| Trigger | Actor navigates to a screen requiring grade data, or the system invokes the grade list to populate a selector. | | |
| Description | Enables all authenticated users to view the list of active grade levels for their school. Parents may optionally scope the query to a specific child to return grades for the school the child attends. System Admin views all grades across all schools. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | The grade list is returned and displayed. No data is modified. | | |
| Normal Flow | 1. System invokes the grade list endpoint automatically when a screen requires grade data (e.g., menu filter, classroom assignment). 2. System returns grades scoped to the actor's school (Parent: scoped to the child's school via studentCode parameter). 3. Actor selects the appropriate grade in the operation. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | Frequent (auto-invoked in multiple screens) | | |
| Business Rules | BR-024 | | |
| Other Information | Parent and Teacher read access added on 08/08/2026. Parents must supply `studentCode` when they have children in multiple schools, to scope the grade list correctly. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-33:** View Grade List Use Cases Specification

---

### UC-209 — View Nutrition Target List {#uc-209}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-209 — View Nutrition Target List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | Service Caterers, Medical Staff |
| Trigger | Actor navigates to School Configuration → Nutrition Targets. | | |
| Description | Enables authorized users to view the configured daily nutritional targets (calories, protein, fat, carbohydrates) for each grade level of the school. Grades not yet configured display default system values and are flagged as "not configured". | | |
| Preconditions | Actor is authenticated and authorized. The school has at least one grade defined. | | |
| Postconditions | Nutrition targets are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Nutrition Targets. 2. System returns the list of nutrition targets for all grades of the actor's school. Each entry shows: grade name, calories (kcal), protein (g), fat (g), carbohydrates (g), and configured status (true / false). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-062 | | |
| Other Information | Targets flagged `configured: false` use system-wide default values until explicitly set via UC-210. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-34:** View Nutrition Target List Use Cases Specification

---

### UC-210 — Update Nutrition Target {#uc-210}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-210 — Update Nutrition Target | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects a grade from the Nutrition Targets screen and enters new target values. | | |
| Description | Enables School Administration to set or update the daily nutritional targets for a specific grade level. If a target does not yet exist for the grade, the system creates one; otherwise it applies a partial merge. Updated targets are applied immediately to subsequent menu validation runs (BR-062). | | |
| Preconditions | School Administration is authenticated. The grade exists and belongs to this school. | | |
| Postconditions | The nutrition target for the specified grade is created or updated. New values are immediately used in menu approval validation. | | |
| Normal Flow | 1. School Administration selects a grade from the Nutrition Targets list. 2. System displays the current values (or defaults if not yet configured). 3. School Administration enters new values for any combination of: calories (kcal), protein (g), fat (g), carbohydrates (g). 4. School Administration saves. 5. System validates values (non-negative, within physiological ranges). 6. System creates or merges the target record and returns the updated target. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. SCH-404: Grade not found or does not belong to this school. Validation error: value is zero or negative. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand (school setup, start of academic year) | | |
| Business Rules | BR-062 | | |
| Other Information | Before 07/08/2026, targets were hardcoded constants in the codebase. This UC enables school-specific customization. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-35:** Update Nutrition Target Use Cases Specification

---

## MF13 — Reporting & Analytics

---

### UC-211 — View Report Export History {#uc-211}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-211 — View Report Export History | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor navigates to Report Center → Export History. | | |
| Description | Enables School Administration and System Admin to view the historical log of all report export operations within their authorized scope. Each record shows the report type, date range covered, output format, row count, generation timestamp, and file size. | | |
| Preconditions | Actor is authenticated and authorized. At least one report export has been performed. | | |
| Postconditions | Export history records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Export History. 2. System displays the paginated list: report type (slug), date range, format (PDF/Excel), row count, generated at, file size. 3. Actor filters by report type, date, or scope. 4. Actor selects a record to view detail or download the file. | | |
| Alternative Flows | AF1 — System Admin Cross-School View: System Admin queries with `scope=all` to review exports across all schools. | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-175, BR-176, BR-177, BR-181, BR-182 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-36:** View Report Export History Use Cases Specification

---

### UC-212 — Download Report Export File {#uc-212}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-212 — Download Report Export File | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor selects a record from the Export History and clicks "Download". | | |
| Description | Enables School Administration or System Admin to re-download a previously generated report export file. The system verifies the file checksum before serving to detect any tampering (ADR-M13-3). | | |
| Preconditions | Actor is authenticated and authorized. The export record exists and the file has not been purged beyond its retention period (BR-177). | | |
| Postconditions | The file is served to the actor as a download (PDF or Excel). The file is not re-generated; the previously stored file is returned. | | |
| Normal Flow | 1. Actor selects an export record from the Export History. 2. Actor clicks "Download". 3. System retrieves the file from secure cloud storage. 4. System verifies the file checksum against the stored value (ADR-M13-3). 5. System returns the file with the correct Content-Disposition header for download. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. If the file has been purged (beyond BR-177 retention), system informs the actor and disables the download. If checksum mismatch is detected (ADR-M13-3), download is blocked and a tamper warning is logged. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-177, BR-181 | | |
| Other Information | ADR-M13-3: Checksum verification before every file download. | | |
| Assumptions | User has a running environment with internet connection. Report files are stored in secure cloud storage. | | |

**Table Add-37:** Download Report Export File Use Cases Specification

---

### UC-213 — Create Report Schedule {#uc-213}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-213 — Create Report Schedule | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin, Email Service |
| Trigger | Actor navigates to Report Schedules and selects "Create New Schedule". | | |
| Description | Enables School Administration or System Admin to configure a recurring automated report schedule. On each scheduled trigger, the system generates the specified report type, uploads it to secure storage, and delivers it via email to the configured recipient list. | | |
| Preconditions | Actor is authenticated and authorized. The target report type (slug) is accessible to the actor's scope. | | |
| Postconditions | A report schedule record is created with status Active. The system calculates the first run timestamp. On each trigger, the system generates the report and delivers it to recipients. | | |
| Normal Flow | 1. Actor opens the Create Schedule form. 2. Actor enters: report type (slug), cadence (cron expression or daily/weekly/monthly selector), date range parameters, output format (PDF or Excel), and recipient email list. 3. Actor submits. 4. System validates the cron expression, scope, and checks for duplicates (same slug + scope). 5. System saves the schedule (status: Active) and calculates `nextRunAt`. 6. System returns the created schedule record. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Invalid cron expression: validation error. No recipients configured: submission blocked. Duplicate schedule (same slug + scope): conflict error (409). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand (configured once; runs automatically thereafter) | | |
| Business Rules | BR-175, BR-176, BR-177, BR-178, BR-179 | | |
| Other Information | CF-M13-2: Frontend UI for schedule management is under development. Currently verified via API only. | | |
| Assumptions | User has a running environment with internet connection. Recipient emails are valid. | | |

**Table Add-38:** Create Report Schedule Use Cases Specification

---

### UC-214 — Update Report Schedule {#uc-214}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-214 — Update Report Schedule | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor selects an existing report schedule and clicks "Edit". | | |
| Description | Enables School Administration or System Admin to modify the cadence, recipients, output format, or parameters of an existing report schedule. After saving, the system recalculates the next run timestamp. | | |
| Preconditions | Actor is authenticated. The schedule exists and belongs to the actor's scope. | | |
| Postconditions | The schedule record is updated. `nextRunAt` is recalculated. | | |
| Normal Flow | 1. Actor selects the schedule and opens "Edit". 2. Actor updates the desired fields. 3. Actor saves. 4. System validates, updates the record, and recalculates `nextRunAt`. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Invalid cron expression. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-179 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-39:** Update Report Schedule Use Cases Specification

---

### UC-215 — Delete Report Schedule {#uc-215}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-215 — Delete Report Schedule | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor selects a report schedule and chooses "Delete". | | |
| Description | Enables School Administration or System Admin to permanently delete a report schedule. Future runs are cancelled. Historical run records are retained. | | |
| Preconditions | Actor is authenticated. The schedule exists and belongs to the actor's scope. | | |
| Postconditions | The schedule record is deleted. No future runs will be triggered. Historical export records remain. | | |
| Normal Flow | 1. Actor selects the schedule. 2. Actor clicks "Delete". System prompts for confirmation. 3. Actor confirms. 4. System deletes the schedule record (HTTP 204). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Not found: Schedule does not exist. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-179 | | |
| Other Information | Historical report export records (`/report-exports`) are not deleted along with the schedule. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-40:** Delete Report Schedule Use Cases Specification

---

### UC-216 — View Report Schedule List {#uc-216}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-216 — View Report Schedule List | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor navigates to the Report Schedules section. | | |
| Description | Enables School Administration or System Admin to view the list of configured automated report schedules, with filtering by report type. | | |
| Preconditions | Actor is authenticated and authorized. | | |
| Postconditions | Schedule records are retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Actor navigates to Report Schedules. 2. System displays the paginated list: report type, cadence, output format, recipient count, last run status, and next run timestamp. 3. Actor filters by report type (slug) as needed. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-179 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-41:** View Report Schedule List Use Cases Specification

---

### UC-217 — Run Report Schedule Now {#uc-217}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-217 — Run Report Schedule Now | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor selects a report schedule with status FAILED or Active and clicks "Run Now". | | |
| Description | Enables School Administration or System Admin to immediately trigger one execution of a report schedule — generating and delivering the report right away — without modifying the recurring schedule or the calculated next run timestamp. | | |
| Preconditions | Actor is authenticated. The schedule exists and belongs to the actor's scope. | | |
| Postconditions | The report is generated, stored, and delivered to the configured recipients. The schedule's `nextRunAt` is unchanged. | | |
| Normal Flow | 1. Actor selects the schedule from the list. 2. Actor clicks "Run Now". 3. System immediately executes the report generation pipeline for the schedule's parameters. 4. System stores the generated file and delivers it to recipients. 5. System records the manual run in the schedule's run history and returns the run result. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Report generation failure: system records FAILED status for this run and returns an error detail. | | |
| Priority | Low | | |
| Frequency of Use | On-demand (troubleshooting / ad-hoc delivery) | | |
| Business Rules | BR-179 | | |
| Other Information | Commonly used to verify a schedule after editing, or to re-send a report that failed on its scheduled run. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-42:** Run Report Schedule Now Use Cases Specification

---

## MF03 — Student & Parent Management

---

### UC-218 — View Student Health File {#uc-218}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-218 — View Student Health File | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff opens a student's profile and selects the "Health File" tab. | | |
| Description | Enables Medical Staff to view a student's clinical Health File — a structured medical record containing clinically verified data: diagnosed medical conditions, current medications, emergency contact, and emergency instructions. This is distinct from the parent-submitted Health & Allergy Declaration (UC-070). | | |
| Preconditions | Medical Staff is authenticated and authorized. The student record exists. | | |
| Postconditions | The clinical Health File is retrieved and displayed. No data is modified. | | |
| Normal Flow | 1. Medical Staff opens the student profile. 2. Medical Staff selects the "Health File" tab. 3. System retrieves and displays the Health File: medical conditions, medications, emergency contact, and emergency instructions. 4. If no clinical record has been entered yet, system returns HTTP 204 No Content and the screen displays an empty form prompt. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-030, BR-031 | | |
| Other Information | Health File (Medical Staff–managed) and Health & Allergy Declaration (Parent–managed, reviewed by Medical Staff) are separate records with separate access control. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-43:** View Student Health File Use Cases Specification

---

### UC-219 — Update Student Health File {#uc-219}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-219 — Update Student Health File | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff views a student's Health File and selects "Edit" to update clinical information. | | |
| Description | Enables Medical Staff to update the clinically verified fields of a student's Health File: medical conditions, current medications, emergency contact, and emergency instructions. Attempts to write parent-declared fields (allergies, specialDiet, healthStatus, allergySeverity) via this endpoint are rejected with a validation error (C8 guard). | | |
| Preconditions | Medical Staff is authenticated and authorized. The student record exists. | | |
| Postconditions | The four permitted clinical fields are updated (or a new Health File record is created if none existed). Audit log records the change with actor identity and timestamp. | | |
| Normal Flow | 1. Medical Staff opens the student's Health File and selects "Edit". 2. Medical Staff updates any combination of: medicalConditions, medications, emergencyContact, emergencyInstructions. 3. Medical Staff saves the changes. 4. System validates that only the four permitted fields are being written (C8 guard). 5. System saves the update and returns the updated Health File. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. STU-094 (MSG-115): Attempt to write a disallowed field (e.g., allergies, specialDiet). System returns a 400 error identifying the specific disallowed field. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-030, BR-031 | | |
| Other Information | C8 guard: System reads raw JSON map and rejects any request containing fields outside `{medicalConditions, medications, emergencyContact, emergencyInstructions}` before binding to DTO. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table Add-44:** Update Student Health File Use Cases Specification

---

## MF10 — Feedback & Notification

---

### UC-220 — Register Device Token {#uc-220}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-220 — Register Device Token | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | User (all authenticated roles) | Secondary Actors | Firebase Cloud Messaging (FCM) |
| Trigger | User opens the SFMS mobile application or web portal (on each startup or on FCM token refresh). | | |
| Description | Enables any authenticated user to register a Firebase Cloud Messaging (FCM) device token for their current device or browser so the system can deliver push notifications to that device. The operation is an upsert — re-registering with the same token is safe. | | |
| Preconditions | User is authenticated and has an active session. A valid FCM token has been obtained from the FCM SDK by the mobile app or browser. | | |
| Postconditions | The device token is saved (or refreshed) in the system linked to the user's account. Future push notifications targeting this user will be delivered to this device. | | |
| Normal Flow | 1. On application startup (or token refresh), the app/browser obtains a fresh FCM token from the FCM SDK. 2. Application sends the token to the system (POST /api/v1/device-tokens) with the platform (iOS / Android / Web). 3. System performs an upsert: creates a new record or refreshes the existing one. 4. System returns HTTP 204 No Content. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. Validation error (422): Token format is invalid. | | |
| Priority | High | | |
| Frequency of Use | Every application startup and every FCM token refresh | | |
| Business Rules | BR-158, BR-159 | | |
| Other Information | Prerequisite for all push notification delivery. Without registration, the user receives no push notifications on the device. | | |
| Assumptions | The device or browser has granted notification permissions to the SFMS application. FCM service is reachable. | | |

**Table Add-45:** Register Device Token Use Cases Specification

---

### UC-221 — Unregister Device Token {#uc-221}

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-221 — Unregister Device Token | | |
| Created By | SEP490-G8 Team | Date Created | 12/08/2026 |
| Primary Actor | User (all authenticated roles) | Secondary Actors | None |
| Trigger | User logs out of the SFMS application on a specific device. | | |
| Description | Enables any authenticated user to remove a specific FCM device token when they log out from a device. Removal revokes push notification delivery to that device without affecting other devices or sessions associated with the same account. | | |
| Preconditions | User is authenticated. The token to be removed exists and is linked to the user's account. | | |
| Postconditions | The device token record is deleted. The system will no longer attempt to deliver push notifications to this device. Other registered tokens for the same user are unaffected. | | |
| Normal Flow | 1. User logs out from the application on a specific device. 2. Application sends the token value to the system (DELETE /api/v1/device-tokens/{token}). 3. System removes the token record. 4. System returns HTTP 204 No Content. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005. If the token does not exist, system returns 204 (idempotent — no error). | | |
| Priority | High | | |
| Frequency of Use | Every logout from a device | | |
| Business Rules | BR-158 | | |
| Other Information | Unregistration is essential for security: prevents push notifications from being delivered to devices after the user has logged out, especially on shared or lost devices. | | |
| Assumptions | User has a running environment with internet connection at the time of logout. | | |

**Table Add-46:** Unregister Device Token Use Cases Specification

---

*Tài liệu này bổ sung **46 Use Cases** vào Report 3 với UC ID từ UC-176 đến UC-221. Mỗi UC tương ứng một thao tác riêng biệt (create, view list, view detail, update, delete, confirm, finalize…) như phong cách của Report 3. Team cần review, xác nhận UC ID không trùng, và tích hợp vào tài liệu SRS chính thức.*
