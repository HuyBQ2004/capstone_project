**CAPSTONE PROJECT REPORT**

**Report 3 - Software Requirement Specification**

[1. Record of changes 9](#_Toc233639782)

[2. Definition 10](#_Toc233639783)

[2.1. Acronyms 10](#_Toc233639784)

[2.2. List of Figures and Tables 11](#_Toc233639785)

[2.2.1. List Figures 11](#_Toc233639786)

[2.2.2. List Table 12](#_Toc233639787)

[3. Software Requirement Specification 12](#_Toc233639788)

[3.1. Requirement Overview 12](#_Toc233639789)

[3.1.1. Context Diagram 13](#_Toc233639790)

[3.1.2. Main Workflows 15](#_Toc233639791)

[3.1.2.1. Supplier Registration & Approval Workflow 15](#_Toc233639792)

[3.1.2.2. Ingredient Receiving Workflow 18](#_Toc233639793)

[3.1.2.3. Ingredient Storage & Inventory Management Workflow 22](#_Toc233639794)

[3.1.2.4. Menu Planning & Approval Workflow 26](#_Toc233639795)

[3.1.2.5. Ingredient Issue to Kitchen Workflow 30](#_Toc233639796)

[3.1.2.6. Food Preparation Workflow 33](#_Toc233639797)

[3.1.2.7. Three-Step Food Inspection Workflow 36](#_Toc233639798)

[3.1.2.8. Food Sample Retention Workflow 40](#_Toc233639799)

[3.1.2.9. Allergy & Special Meal Management Workflow 44](#_Toc233639800)

[3.1.2.10. Meal Serving Workflow 48](#_Toc233639801)

[3.1.2.11. Food Safety Incident Handling Workflow 51](#_Toc233639802)

[3.1.2.12. Parent Meal Monitoring Workflow 55](#_Toc233639803)

[3.1.3. Generation – Specification 58](#_Toc233639804)

[3.1.3.1. Actors 58](#_Toc233639805)

[3.1.3.2. Use Case Diagrams 59](#_Toc233639806)

[3.1.3.2.1 UCs for Guest 59](#_Toc233639807)

[3.1.3.2.2 UCs for User 60](#_Toc233639808)

[3.1.3.2.3 UCs for System Admin 61](#_Toc233639809)

[3.1.3.2.4 UCs for School Administration 62](#_Toc233639810)

[3.1.3.2.5 UCs for Teacher 63](#_Toc233639811)

[3.1.3.2.6 UCs for Parent 64](#_Toc233639812)

[3.1.3.2.7 UCs for Medical Staff 65](#_Toc233639813)

[3.1.3.2.8 UCs for Raw Food Supplier 66](#_Toc233639814)

[3.1.3.2.9 UCs for Service Caterers 67](#_Toc233639815)

[3.1.3.3. Use Cases Description 68](#_Toc233639816)

[3.2. Functional Specifications 104](#_Toc233639817)

[3.2.1. User Management & Access Control 104](#_Toc233639818)

[3.2.1.1. Active/ Deactivate Staff 104](#_Toc233639819)

[3.2.1.2. Active/Deactivate User 105](#_Toc233639820)

[3.2.1.3. Change Password 107](#_Toc233639821)

[3.2.1.4. Create Role 109](#_Toc233639822)

[3.2.1.5. Create Staff 111](#_Toc233639823)

[3.2.1.6. Create User 113](#_Toc233639824)

[3.2.1.7. Delete Role 115](#_Toc233639825)

[3.2.1.8. Forgot Password 117](#_Toc233639826)

[3.2.1.9. Login 119](#_Toc233639827)

[3.2.1.10. Logout 120](#_Toc233639828)

[3.2.1.11. Profile Manage 122](#_Toc233639829)

[3.2.1.12. Update Profile 123](#_Toc233639830)

[3.2.1.13. Update Role 124](#_Toc233639831)

[3.2.1.14. View List Permission 125](#_Toc233639832)

[3.2.1.15. View List Role 126](#_Toc233639833)

[3.2.1.16. View Profile 127](#_Toc233639834)

[3.2.1.17. View Staff Account List 128](#_Toc233639835)

[3.2.1.18. View User Account List 129](#_Toc233639836)

[3.2.1.19. Activate Account & Set Password 130](#_Toc233639837)

[3.2.2. School & Class Management 132](#_Toc233639838)

[3.2.2.1. Active/ Deactivate School 132](#_Toc233639839)

[3.2.2.2. Assign Homeroom Teacher 134](#_Toc233639840)

[3.2.2.3. Create Classroom 135](#_Toc233639841)

[3.2.2.4. Manage Classrooms 137](#_Toc233639842)

[3.2.2.5. Register School 138](#_Toc233639843)

[3.2.2.6. Update Classroom 139](#_Toc233639844)

[3.2.2.7. Update Information School 141](#_Toc233639845)

[3.2.2.8. View Information School 142](#_Toc233639846)

[3.2.2.9. View School List 143](#_Toc233639847)

[3.2.3. Student & Parent Management 145](#_Toc233639849)

[3.2.3.1. Bulk Import Student & Parent 145](#_Toc233639850)

[3.2.3.2. Create Student 146](#_Toc233639851)

[3.2.3.3. View List Allergy & Special Meals 147](#_Toc233639852)

[3.2.3.4. View List Student Health Information 148](#_Toc233639853)

[3.2.3.5. View List Student Health Records 149](#_Toc233639854)

[3.2.3.6. Review Health Information 149](#_Toc233639855)

[3.2.3.7. Review Special Meal Requests 150](#_Toc233639856)

[3.2.3.8. Submit Health & Allergy Information 151](#_Toc233639857)

[3.2.3.9. Update Health & Allergy Information 152](#_Toc233639858)

[3.2.3.10. Update Health Records 153](#_Toc233639859)

[3.2.3.11. Update Student 153](#_Toc233639860)

[3.2.3.12. View Allergy Information 154](#_Toc233639861)

[3.2.3.13. View Student Health & Allergy Information 155](#_Toc233639862)

[3.2.3.14. View Student Health History 156](#_Toc233639863)

[3.2.3.15. View Student List 156](#_Toc233639864)

[3.2.4. Supplier & Partner Management 157](#_Toc233639865)

[3.2.4.1. Approval / Reject Service Caterers 157](#_Toc233639866)

[3.2.4.2. Approval / Reject Supplier 159](#_Toc233639867)

[3.2.4.3. Create Info Delivery Person 161](#_Toc233639868)

[3.2.4.4. Delete Face Data 162](#_Toc233639869)

[3.2.4.5. Manage Compliance Documents 164](#_Toc233639870)

[3.2.4.6. Manage Profile 166](#_Toc233639871)

[3.2.4.7. Register Service Caterers Account 168](#_Toc233639872)

[3.2.4.8. Register Face Data 169](#_Toc233639873)

[3.2.4.9. Register Supplier Account 171](#_Toc233639874)

[3.2.4.10. Update Compliance Documents 172](#_Toc233639875)

[3.2.4.11. Upload Food Safety Certificates 174](#_Toc233639876)

[3.2.4.12. Upload Product Origin Documents 176](#_Toc233639877)

[3.2.4.13. View Service Caterers Detail 177](#_Toc233639878)

[3.2.4.14. View Delivery Person List 178](#_Toc233639879)

[3.2.4.15. View List Service Caterers 180](#_Toc233639880)

[3.2.4.16. View List Supplier 181](#_Toc233639881)

[3.2.4.17. View Supplier Detail 183](#_Toc233639882)

[3.2.4.18 Submit Supplier Application 184](#_Toc233639883)

[3.2.4.19 Track Supplier Application Status 186](#_Toc233639884)

[3.2.4.20 Update/Resubmit Supplier Application 187](#_Toc233639885)

[3.2.4.21 Manage Supplier Application 189](#_Toc233639886)

[3.2.4.22. Submit Catering Application 190](#_Toc233639887)

[3.2.4.23. Track Catering Application Status 192](#_Toc233639888)

[3.2.4.24. Update/Resubmit Catering Application 193](#_Toc233639889)

[3.2.4.25. Manage Catering Application 194](#_Toc233639890)

[3.2.5. Menu & Diet Management 196](#_Toc233639891)

[3.2.5.1. Approval / Reject Menu 196](#_Toc233639892)

[3.2.5.2. Create Menu 197](#_Toc233639893)

[3.2.5.3. Create Special Meal Plan 199](#_Toc233639894)

[3.2.5.4. Manage Menus 201](#_Toc233639895)

[3.2.5.5. Request AI Menu Recommendations 203](#_Toc233639896)

[3.2.5.6. Submit Menu For Approval 204](#_Toc233639897)

[3.2.5.7. Update Menu 206](#_Toc233639898)

[3.2.5.8. View List Menu 207](#_Toc233639899)

[3.2.5.9. View Menu Detail 209](#_Toc233639900)

[3.2.5.10. View Menu Information 210](#_Toc233639901)

[3.2.6. Ingredient & Inventory Management 212](#_Toc233639902)

[3.2.6.1. Confirm Delivery Schedule 212](#_Toc233639903)

[3.2.6.2. Create Purchase Request 213](#_Toc233639904)

[3.2.6.3. Manage Deliveries 215](#_Toc233639905)

[3.2.6.4. Manage Ingredient Batches 216](#_Toc233639906)

[3.2.6.5. Manage Ingredient Receiving 217](#_Toc233639907)

[3.2.6.6. Manage Inventory 218](#_Toc233639908)

[3.2.6.7. Record Ingredient Receiving 219](#_Toc233639909)

[3.2.6.8. Record Ingredient Storage 221](#_Toc233639910)

[3.2.6.9. Record Inventory Transactions 222](#_Toc233639911)

[3.2.6.10. Record Prepare Ingredient Information 223](#_Toc233639912)

[3.2.6.11. Reject Ingredient Batch 224](#_Toc233639913)

[3.2.6.12. Submit Batch Information 226](#_Toc233639914)

[3.2.6.13. Submit Delivery Information 227](#_Toc233639915)

[3.2.6.14. Update Delivery Status 228](#_Toc233639916)

[3.2.6.15. Upload Batch Certificates 230](#_Toc233639917)

[3.2.6.16. View Batch History 231](#_Toc233639918)

[3.2.6.17. View Delivery Batch Records 232](#_Toc233639919)

[3.2.6.18. View Ingredient Receiving Records 233](#_Toc233639920)

[3.2.6.19. View Purchase Requests 233](#_Toc233639921)

[3.2.7. Food Safety Inspection & Monitoring 234](#_Toc233639922)

[3.2.7.1. Conduct Food Safety Inspection 234](#_Toc233639923)

[3.2.7.2. Daily Food Safety Monitoring 236](#_Toc233639924)

[3.2.7.3. Manage Three-Step Food Inspection 238](#_Toc233639925)

[3.2.7.4. Monitor Food Safety 239](#_Toc233639926)

[3.2.7.5. Conduct Three-Step Food Inspection 240](#_Toc233639927)

[3.2.7.6. View Food Inspection Records 242](#_Toc233639928)

[3.2.7.7. View Food Inspection Reports 244](#_Toc233639929)

[3.2.7.8. View Food Safety Compliance Report 245](#_Toc233639930)

[3.2.7.9. View Food Safety Reports 247](#_Toc233639931)

[3.2.7.10. View Inspection Records 248](#_Toc233639932)

[3.2.8. Food Sample Retention 249](#_Toc233639933)

[3.2.8.1. Dispose Food Sample 249](#_Toc233639934)

[3.2.8.2. Manage Food Sample Retention 251](#_Toc233639935)

[3.2.8.3. Register Food Sample 253](#_Toc233639936)

[3.2.8.4. Retrieve Food Sample Records 256](#_Toc233639937)

[3.2.8.5. View Food Sample Records 258](#_Toc233639938)

[3.2.9. Meal Serving & Distribution 260](#_Toc233639939)

[3.2.9.1. Confirm Meal Distribution 260](#_Toc233639940)

[3.2.9.2. Manage Meal Service 261](#_Toc233639941)

[3.2.9.3. Monitor Child Meal History 263](#_Toc233639942)

[3.2.9.4. Monitor School Meals 264](#_Toc233639943)

[3.2.9.5. Monitor Student Meal History 265](#_Toc233639944)

[3.2.9.6. Student Meal Consumption 267](#_Toc233639945)

[3.2.9.7. View Allergy Alerts 268](#_Toc233639946)

[3.2.9.8. View Meal Distribution List 270](#_Toc233639947)

[3.2.9.9. View Meal History 271](#_Toc233639948)

[3.2.9.10. View Special Meal Arrangements 273](#_Toc233639949)

[3.2.9.11. View Special Meal Records 274](#_Toc233639950)

[3.2.9.12. View Special Meal Requirements 275](#_Toc233639951)

[3.2.10. Feedback & Announcement Management 277](#_Toc233639952)

[3.2.10.1. Create Announcement 277](#_Toc233639953)

[3.2.10.2. Manage Meal Feedback 278](#_Toc233639954)

[3.2.10.3. Submit Meal & Food Quality Feedback 280](#_Toc233639955)

[3.2.10.4. Submit Meal Feedback 282](#_Toc233639956)

[3.2.10.5. Update Announcement 283](#_Toc233639957)

[3.2.10.6. View Announcement Detail 285](#_Toc233639958)

[3.2.10.7. View List Announcement 286](#_Toc233639959)

[3.2.11. Incident & Emergency Management 287](#_Toc233639960)

[3.2.11.1. Approval / Reject Corrective Action 287](#_Toc233639961)

[3.2.11.2. Assign Incident Severity 289](#_Toc233639962)

[3.2.11.3. Close Incident 290](#_Toc233639963)

[3.2.11.4. Export Incident Reports 292](#_Toc233639964)

[3.2.11.5. Generate Incident Report 294](#_Toc233639965)

[3.2.11.6. Manage Corrective Actions 295](#_Toc233639966)

[3.2.11.7. Manage Food Safety Incidents 297](#_Toc233639967)

[3.2.11.8. Record Corrective Actions Report 298](#_Toc233639968)

[3.2.11.9. Report Food Safety Concern 300](#_Toc233639969)

[3.2.11.10. Report Food Safety Incident 301](#_Toc233639970)

[3.2.11.11. Submit Corrective Action Results 303](#_Toc233639971)

[3.2.11.12. View Food Poisoning Incident List 304](#_Toc233639972)

[3.2.11.13. View Food Safety Incident Detail 306](#_Toc233639973)

[3.2.11.14. View Incident Information 307](#_Toc233639974)

[3.2.11.15. View Incident Report 308](#_Toc233639975)

[3.2.12. AI & Supporting Services 309](#_Toc233639976)

[3.2.12.1. Chat with AI 309](#_Toc233639977)

[3.2.12.2. Manage External Integrations 311](#_Toc233639978)

[3.2.12.3. Monitor System Activities 312](#_Toc233639979)

[3.2.12.4. Receive Notification 313](#_Toc233639980)

[3.2.12.5. Scan Face Data 315](#_Toc233639981)

[3.2.12.6. Turn off/Turn on Notification 316](#_Toc233639982)

[3.2.12.7. View Audit Logs 318](#_Toc233639983)

[3.2.12.8. View Error Logs 319](#_Toc233639984)

[3.2.12.9. View History Chat 320](#_Toc233639985)

[3.2.12.10. View Notification History 321](#_Toc233639986)

[3.2.12.11. View System Status 322](#_Toc233639987)

[3.2.12.12. View Usage Statistics 324](#_Toc233639988)

[3.3. Functional Requirements 326](#_Toc233639989)

[3.3.1. System Functional Overview 326](#_Toc233639990)

[3.3.1.1. Screens Flow 326](#_Toc233639991)

[3.3.1.2. Screen Authorization 334](#_Toc233639992)

[3.3.1.3. Non-screen Functions 352](#_Toc233639993)

[3.3.2. Mobile Screen 365](#_Toc233639994)

[3.3.2.1. Guest and Common Screen 365](#_Toc233639995)

[3.3.2.2. Service Caterers Screens 389](#_Toc233639996)

[3.3.2.3. Medical Staff Screens 414](#_Toc233639997)

[3.3.2.4. Parent Screens 446](#_Toc233639998)

[3.3.2.5. Raw Food Supplier Screen 474](#_Toc233639999)

[3.3.2.6. Teacher Screens 486](#_Toc233640000)

[3.4. Non-Functional Requirements 498](#_Toc233640001)

[3.4.1. Performance Requirements 498](#_Toc233640002)

[3.4.2. Reliability Requirements 498](#_Toc233640003)

[3.4.3. Security Requirements 498](#_Toc233640004)

[3.4.4. Usability Requirements 499](#_Toc233640005)

[3.4.5. Data Requirements 499](#_Toc233640006)

[3.4.6. Operational Constraints 499](#_Toc233640007)

[3.5 Requirement Appendix 500](#_Toc233640008)

[3.5.1 Business Rules 500](#_Toc233640009)

[3.5.2 System Messages 513](#_Toc233640010)

# Record of changes

|  |  |  |  |
| --- | --- | --- | --- |
| Date | A\*, M, D | In charge | Change Description |
| 05/06/2026 | A | NamNVHE180863 | Init Document |
| 14/06/2026 | M | HuyBQHE180599 | Add use case description |
| 14/06/2026 | M | MinhTTHE186258 | Modify use case description |
| 22/06/2026 | M | NamNVHE180863 | Fix document format. |
| 01/07/2026 | M | SEP490-G8 Team | Merged approved proposals: Academic Year Management (UC-174–177, §3.2.2 School & Class Management) and Violation Management (UC-178, §3.2.7 Food Safety Inspection & Monitoring; UC-179–183, §3.2.11 Incident & Emergency Management). Added corresponding entries to Non-screen Functions (#58–60), business_rules.md (BR-183–195), error_message.md (MSG-080–089), traceability_matrix.md, and class_diagram_description.md. |

Table ‑ Record of changes

# Definition

## Acronyms

| Acronym | Definition |
| --- | --- |
| SRS | Software Requirements Specification - A formal document describing the intended behavior, constraints, and requirements of the system. |
| SFMS | School Food Safety Management Support System - The digital platform developed to support food safety management activities in schools across Vietnam, in compliance with national food safety regulations. |
| UC | Use Case - A list of actions or event steps defining the interactions between an actor and a system to achieve a goal. |
| BR | Business Rule - A statement that defines or constrains some aspect of the business operations or logic. |
| MSG | System Message - A predefined code and text used by the system to communicate status or errors to users. |
| NFR | Non-Functional Requirement - A requirement that specifies criteria that can be used to judge the operation of a system, rather than specific behaviors. |
| UI/UX | User Interface / User Experience - The visual design and user interaction elements of the system. |
| API | Application Programming Interface - A set of protocols for building and integrating application software. |
| FEFO | First Expired, First Out - An inventory management methodology where products with the earliest expiration dates are used or sold first. |
| FIFO | First In, First Out - An inventory management methodology where the oldest inventory items are recorded as sold first. |
| GPS | Global Positioning System - A satellite-based radionavigation system used to verify delivery locations. |
| OTP | One-Time Password - A temporary, secure password that is valid for only one login session or transaction. |
| QR | Quick Response Code - A two-dimensional barcode used for scanning, ingredient traceability, and authentication. |
| FCM | Firebase Cloud Messaging - A cross-platform messaging solution that lets you reliably send messages and notifications. |
| SMS | Short Message Service - A text messaging service component of most telephone, Internet, and mobile device systems. |
| CSV | Comma-Separated Values - A text file format that uses commas to separate values, used for importing and exporting system data. |
| PDF | Portable Document Format - A file format used to present documents in a manner independent of application software, hardware, and operating systems. |
| BYT | Bộ Y tế - The Ministry of Health of Vietnam, the governmental authority responsible for health regulations and food safety guidelines. |
| CAPA | Corrective and Preventive Action - A systematic process to investigate, rectify, and prevent food safety incidents or system issues. |
| AI | Artificial Intelligence - Technology used in the system for menu recommendation and nutritional analysis. |
| AES | Advanced Encryption Standard - A symmetric-key block cipher algorithm used to secure sensitive data. |
| SMTP | Simple Mail Transfer Protocol - A communication protocol for electronic mail transmission. |
| TLS | Transport Layer Security - A cryptographic protocol designed to provide communications security over a computer network. |

Table ‑ Acronyms

## List of Figures and Tables

### List Figures

[Figure 3‑1 Context Diagram 15](#_Toc233697845)

[Figure 3‑2 Supplier Registration & Approval Workflow 17](#_Toc233697846)

[Figure 3‑3 Ingredient Receiving Workflow 20](#_Toc233697847)

[Figure 3‑4 Ingredient Storage & Inventory Management Workflow 24](#_Toc233697848)

[Figure 3‑5 UCs for Guest 61](#_Toc233697849)

[Figure 3‑6 UCs for User 62](#_Toc233697850)

[Figure 3‑7 UCs for System Admin 63](#_Toc233697851)

[Figure 3‑8 UCs for School Administration 64](#_Toc233697852)

[Figure 3‑9 UCs for Teacher 65](#_Toc233697853)

[Figure 3‑10 UCs for Parent 66](#_Toc233697854)

[Figure 3‑11 UCs for Medical Staff 67](#_Toc233697855)

[Figure 3‑12 UCs for Raw Food Supplier 68](#_Toc233697856)

[Figure 3‑13 UCs for Service Caterers 69](#_Toc233697857)

[Figure 3‑14 Screen for Register Service Caterers 379](#_Toc233697858)

[Figure 3‑15 Screen for Login 381](#_Toc233697859)

[Figure 3‑16 Screen for Forgot Password 383](#_Toc233697860)

[Figure 3‑17 Screen for Catering Dashboard 400](#_Toc233697861)

[Figure 3‑18 Screens Medical Staff Dashboard 425](#_Toc233697862)

[Figure 3‑19 Screens Health Record For Medical Staff 428](#_Toc233697863)

[Figure 3‑20: Parent Dashboard 456](#_Toc233697864)

[Figure 3‑21: Child Selection 458](#_Toc233697865)

[Figure 3‑22 Supplier Dashboard 484](#_Toc233697866)

[Figure 3‑23 Partnership Detail 486](#_Toc233697867)

[Figure 3‑24 Purchase Request Screen 488](#_Toc233697868)

[Figure 3‑25 Purchase Request Screen 489](#_Toc233697869)

[Figure 3‑26 Compliance Docs Screens 491](#_Toc233697870)

[Figure 3‑27 Ingredient Batches Screens 492](#_Toc233697871)

[Figure 3‑28 Ingredient Batches Screens 494](#_Toc233697872)

### List Table

[Table 1‑1 Record of changes 9](#_Toc233697873)

[Table 2‑1 Acronyms 11](#_Toc233697874)

[Table 3‑1 Context Diagram Description 15](#_Toc233697875)

[Table 3‑2 Supplier Registration & Approval Workflow 18](#_Toc233697876)

[Table 3‑3 Ingredient Receiving Workflow 22](#_Toc233697877)

[Table 3‑4 Ingredient Storage & Inventory Management Workflow 26](#_Toc233697878)

[Table 3‑5 Actors List 60](#_Toc233697879)

[Table 3‑6 Use Cases Description 105](#_Toc233697880)

[Table 3‑7 Active/Deactivate Staff Use Cases Specification 107](#_Toc233697881)

[Table 3‑8 Active/Deactivate User Use Cases Specification 108](#_Toc233697882)

[Table 3‑9 Screen for Login 381](#_Toc233697883)

[Table 3‑10 Screen for Forgot Password 383](#_Toc233697884)

[Table 3‑11 Screens Medical Staff Dashboard 426](#_Toc233697885)

[Table 3‑12 Screens Special Meal For Student 429](#_Toc233697886)

[Table 3‑13 Screens Special Meal For Student 431](#_Toc233697887)

[Table 3‑13 Screens Incidents Records 432](#_Toc233697888)

[Table 3‑14 Screens Incidents Records Detail 435](#_Toc233697889)

[Table 3‑15 Screens Evaluate the level Incidents 438](#_Toc233697890)

[Table 3‑16 Screens Investigation Space 440](#_Toc233697891)

[Table 3‑17 Screens Export To School Administration 442](#_Toc233697892)

[Table 3‑18 Screens Extraction of Stored Food Samples 445](#_Toc233697893)

[Table 3‑19 Screens Collect Evidence 448](#_Toc233697894)

[Table 3‑20 Screens Treatment Measures 453](#_Toc233697895)

[Table 3‑22 Supplier Dashboard 484](#_Toc233697896)

[Table 3‑24 Partnership Detail 486](#_Toc233697897)

[Table 3‑26 Purchase Request Screen 488](#_Toc233697898)

[Table 3‑28 Purchase Request Screen 489](#_Toc233697899)

[Table 3‑30 Compliance Docs Screens 491](#_Toc233697900)

[Table 3‑32 Ingredient Batches Screens 492](#_Toc233697901)

[Table 3‑28 Ingredient Batches Screens 494](#_Toc233697902)

# Software Requirement Specification

## Requirement Overview

The School Food Safety Management Support System (SFSMSS) is a digital platform developed to support food safety management activities in schools. The system enables schools to manage suppliers, ingredient receiving, inventory, menu planning, food processing, food sample retention, student health records, allergy information, and food safety incidents through a centralized role-based system.

The platform supports end-to-end food safety processes, from supplier qualification and ingredient inspection to meal preparation, health monitoring, and incident investigation. Parents can monitor menus and nutritional information, while school staff can ensure compliance with food safety regulations and operational procedures.

By integrating notification services, location verification, and Artificial Intelligence technologies, SFSMSS improves food safety compliance, enhances traceability, reduces operational risks, and strengthens communication between schools, suppliers, medical staff, teachers, and parents.

### Context Diagram

![](data:image/png;base64...)

Figure ‑ Context Diagram

**Description**

| External Entity | Input to System Data Flow | Output from System Data Flow |
| --- | --- | --- |
| System Admin | System Configuration, User Management Information | System Status, Audit Log Information |
| School Administration | Approval Decisions, School Policy Information, School Management Information | Dashboard Information, Food Safety Reports, Incident Reports |
| Service Caterers | Menu Information, Processing Information, Inspection Information | Approved Menu Information, Ingredient Information, Allergy Alerts |
| Medical Staff | Health Information, Allergy Verification Information, Incident Information | Health Alerts, Sample Information, Food Safety Reports |
| Teacher | Student Information, Health Update Information | Menu Information, Notification Information |
| Parent | Health Declaration Information, Allergy Information, Feedback Information | Menu Information, Nutrition Information, Notification Information |
| Raw Food Supplier | Supplier Information, Delivery Information, Certificate Information | Approval Information, Evaluation Information |
| GPS Service | Location Verification Result, Location Metadata | Location Verification Request |
| Gmail | Email Delivery Status | Email Information |
| Firebase Cloud Messaging | Notification Delivery Status | Notification Information |
| Gemini AI Service | Menu Recommendation Information, Nutrition Analysis Information | Menu Requirement Information, Student Health Information |

Table ‑ Context Diagram Description

### Main Workflows

#### Supplier Registration & Approval Workflow

![](data:image/png;base64...)

Figure ‑ Supplier Registration & Approval Workflow

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Submit Supplier Profile | Supplier submits company profile, business license, food safety certificates, product information, and supporting documents to the school. | Supplier | Start of the flow |
| 2 | Review Supplier Documents | School Administration reviews supplier qualifications, certificates, licenses, and compliance documents. | School Administration | Manual review |
| 3 | Documents Valid? | School Administration determines whether the submitted documents meet regulatory and school requirements. | School Administration | Decision point: Yes → Step 4; No → Step 8 |
| 4 | Evaluate Supplier | School Administration evaluates supplier capability, product quality, reputation, and service capacity. | School Administration | May involve additional verification |
| 5 | Approve Supplier? | School Administration decides whether the supplier is eligible to provide ingredients for the school. | School Administration | Decision point: Yes → Step 6; No → Step 8 |
| 6 | Create Supplier Record | System creates the supplier profile and assigns Approved status. | System | Executed after approval |
| 7 | Send Approval Notification | System sends approval notification and supplier onboarding information. | System | End of successful flow |
| 8 | Record Rejection Reason | School Administration records the rejection reason. | School Administration | Executed when supplier is not approved |
| 9 | Send Rejection Notification | System sends rejection notification to the supplier. | System | End of rejection flow |

Table ‑ Supplier Registration & Approval Workflow

#### Ingredient Receiving Workflow

![](data:image/png;base64...)

Figure ‑ Ingredient Receiving Workflow

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Deliver Ingredient Batch | Raw Food Supplier delivers an ingredient batch to the school according to the agreed schedule. | Raw Food Supplier | Start of workflow |
| 2 | Start Receiving Session | Catering Staff initiate a receiving session in the system for the delivered batch. | Service Caterers | Receiving process begins |
| 3 | Verify Supplier Status | System verifies that the supplier is approved and eligible to provide ingredients. | System | Decision point: Approved → Step 4; Rejected → Step 14 |
| 4 | Review Batch Information | Catering Staff reviews batch information including batch numbers, production date, expiration date, quantity, and certificates. | Service Caterers | Batch verification |
| 5 | Inspect Ingredient Quality | Catering Staff performs visual inspection of ingredient quality, packaging condition, freshness, and food safety indicators. | Service Caterers | Physical inspection |
| 6 | Capture Evidence Photos | Catering Staff captures photos of ingredients, labels, packaging, and supporting documents. | Service Caterers | Mandatory evidence |
| 7 | Capture GPS and Timestamp | System automatically records GPS coordinates, device information, and timestamp during receiving activities. | System | Anti-fraud control |
| 8 | Verify Receiving Location | System verifies that the receiving activity is performed within the authorized receiving area. | System | Decision point: Valid → Step 9; Invalid → Step 14 |
| 9 | Complete Inspection Checklist | Catering Staff completes the receiving checklist and records inspection results. | Service Caterers | Required before approval |
| 10 | Ingredient Qualified? | Catering Staff determines whether the ingredient batch satisfies food safety requirements. | Service Caterers | Decision point: Yes → Step 11; No → Step 14 |
| 11 | Create Batch Record | System creates a batch record containing supplier, batch, GPS, timestamp, and inspection information. | System | Traceability enabled |
| 12 | Update Inventory | System updates inventory quantities and expiration tracking information. | System | Inventory updated |
| 13 | Generate Receiving Record | System generates and stores the receiving record together with evidence and audit logs. | System | Successful completion |
| 14 | Reject Ingredient Batch | Catering Staff rejects the batch and records the rejection reason. System notifies relevant parties. | Service Caterers / System | End of rejection flow |

Table ‑ Ingredient Receiving Workflow

#### Ingredient Storage & Inventory Management Workflow

![](data:image/png;base64...)

Figure ‑ Ingredient Storage & Inventory Management Workflow

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Receive Approved Batch | The system receives approved ingredient batches from the Ingredient Receiving Workflow. | System | Start of workflow |
| 2 | Assign Storage Location | Catering Staff assigns the ingredient batch to an appropriate storage location such as freezer, refrigerator, or dry storage. | Service Caterers | Storage classification |
| 3 | Store Ingredient Batch | Catering Staff places the ingredient batch into the designated storage area. | Service Caterers | Physical storage |
| 4 | Record Storage Information | System records storage location, batch information, quantity, and expiration date. | System | Inventory tracking |
| 5 | Monitor Inventory Status | System continuously tracks inventory quantity and batch availability. | System | Automatic monitoring |
| 6 | Monitor Expiration Date | System monitors expiration dates of stored ingredients. | System | Automatic monitoring |
| 7 | Expiration Alert Required? | System determines whether any batch is approaching its expiration date. | System | Decision point: Yes → Step 8; No → Step 9 |
| 8 | Generate Expiration Alert | System sends expiration alerts to Service Caterers and School Administration. | System | Preventing food waste |
| 9 | Ingredient Request Received? | Service Caterers requests ingredients for food preparation. | Service Caterers | Decision point: Yes → Step 10; No → Continue monitoring |
| 10 | Verify Inventory Availability | System verifies that sufficient inventory is available for the requested ingredients. | System | Inventory validation |
| 11 | Inventory Available? | System determines whether inventory is sufficient. | System | Decision point: Yes → Step 12; No → Step 15 |
| 12 | Issue Ingredient Batch | Catering Staff issues ingredient batches from inventory according to FIFO/FEFO principles. | Service Caterers | Stock-out process |
| 13 | Update Inventory Quantity | System updates inventory balances and batch quantities after issuance. | System | Inventory adjustment |
| 14 | Record Inventory Transaction | System records inventory movement in the audit log and inventory history. | System | End of successful flow |
| 15 | Generate Inventory Shortage Alert | System generates inventory shortage alerts for Service Caterers. | System | End of shortage flow |

Table ‑ Ingredient Storage & Inventory Management Workflow

#### Menu Planning & Approval Workflow

![](data:image/png;base64...)

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Review Menu Requirements | Catering Manager reviews nutritional requirements, school policies, ingredient availability, and meal planning guidelines. | Service Caterers | Start of workflow |
| 2 | Review Allergy Statistics | System summarizes allergy categories and special dietary requirements of students. | System | Preparation step |
| 3 | Generate Menu Draft | Catering Manager creates a standard menu manually or requests AI assistance. | Service Caterers | Menu planning |
| 4 | Generate Menu Recommendation | Gemini AI analyzes nutritional requirements and suggests menu options. | Gemini AI Service | AI support |
| 5 | Analyze Nutrition | Gemini AI evaluates calorie balance, nutrients, and meal diversity. | Gemini AI Service | AI support |
| 6 | Detect Allergy Conflicts | System checks whether menu items contain allergens affecting registered students. | System | Automatic validation |
| 7 | Allergy Conflict Detected? | System determines whether allergy risks exist. | System | Decision point |
| 8 | Generate Alternative Meal Suggestions | Gemini AI recommends alternative dishes for common allergy categories (e.g., seafood allergies, milk allergy, egg allergy). | Gemini AI Service | Only executed if conflict exists |
| 9 | Review Alternative Meals | Catering Manager reviews and adjusts alternative meal suggestions. | Service Caterers | Human validation |
| 10 | Submit Menu for Approval | Catering Manager submits the standard menu and special meal options for approval. | Service Caterers | Approval request |
| 11 | Review Menu | School Administration reviews menu content, nutrition information, and allergy accommodations. | School Administration | Approval review |
| 12 | Menu Approved? | School Administration decides whether to approve or reject the menu. | School Administration | Decision point |
| 13 | Publish Menu | System publishes the approved menu and associated special meal options. | System | Approved flow |
| 14 | Notify Stakeholders | System sends menu notifications to parents, teachers, and medical staff. | System | Communication |
| 15 | Store Menu Records | System stores menu history, approval records, and audit logs. | System | End of successful flow |
| 16 | Reject Menu | School Administration rejects the menu and provides comments. | School Administration | Rejection flow |
| 17 | Revise Menu | Catering Manager revises the menu and resubmits it. | Service Caterers | Return to Step 3 |

#### Ingredient Issue to Kitchen Workflow

![](data:image/png;base64...)

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Review Approved Menu | Catering Manager reviews the approved menu and determines ingredient requirements. | Service Caterers | Start of workflow |
| 2 | Create Ingredient Request | Catering Manager creates an ingredient issue request based on menu requirements and expected meal quantity. | Service Caterers | Issue request |
| 3 | Verify Inventory Availability | System verifies whether sufficient ingredients are available in inventory. | System | Inventory validation |
| 4 | Inventory Available? | System determines whether inventory is sufficient. | System | Decision point |
| 5 | Select Ingredient Batches | System recommends ingredient batches following FEFO (First Expired First Out) principles. | System | Traceability support |
| 6 | Review Selected Batches | Catering Staff reviews selected ingredient batches and quantities. | Service Caterers | Human verification |
| 7 | Issue Ingredients | Catering Staff issues ingredients from inventory to the kitchen preparation area. | Service Caterers | Physical stock movement |
| 8 | Update Inventory Records | System deducts issued quantities from inventory and updates stock balances. | System | Inventory update |
| 9 | Record Batch Traceability | System records batch numbers associated with the menu and production session. | System | Traceability record |
| 10 | Generate Issue Record | System generates an ingredient issue record and stores it in the audit log. | System | Documentation |
| 11 | Ingredients Ready for Preparation | Ingredients become available for food preparation activities. | System | End of successful flow |
| 12 | Generate Shortage Alert | System generates a shortage alert when inventory is insufficient. | System | Alternative flow |
| 13 | Request Inventory Replenishment | Catering Manager initiates replenishment or procurement actions. | Service Caterers | End of shortage flow |

#### Food Preparation Workflow

![](data:image/png;base64...)

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Review Approved Menu | Catering Staff reviews the approved menu and meal production plan. | Service Caterers | Start of workflow |
| 2 | Retrieve Issued Ingredients | Catering Staff collects approved ingredients from the preparation area. | Service Caterers | Ingredient preparation |
| 3 | Verify Ingredient Information | Catering Staff verifies ingredient batches, quantities, and expiration dates before use. | Service Caterers | Batch verification |
| 4 | Prepare Ingredients | Catering Staff washes, cuts, and prepares ingredients according to food preparation procedures. | Service Caterers | Pre-processing stage |
| 5 | Record Pre-processing Information | System records pre-processing activities, responsible staff, and timestamps. | System | Traceability |
| 6 | Start Cooking Process | Catering Staff cooks meals according to the approved menu and recipe guidelines. | Service Caterers | Cooking stage |
| 7 | Record Cooking Information | System records cooking time, responsible staff, and production session information. | System | Traceability |
| 8 | Complete Meal Preparation | Catering Staff completes meal production and prepares meals for inspection. | Service Caterers | Meal ready |
| 9 | Generate Preparation Record | System generates a food preparation record linked to menu and ingredient batches. | System | Audit trail |
| 10 | Meals Ready for Inspection | Prepared meals are transferred to the Food Inspection Workflow. | System | End of workflow |

#### Three-Step Food Inspection Workflow

![](data:image/png;base64...)

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Start Inspection Session | Catering Staff initiates a food inspection session for the meal production batch. | Service Caterers | Start of workflow |
| 2 | Perform Inspection Step 1 | Catering Staff inspects ingredient quality, freshness, packaging condition, and batch information before food preparation. | Service Caterers | Inspection before processing |
| 3 | Record Inspection Step 1 | System records inspection results, evidence photos, GPS location, and timestamp. | System | Traceability |
| 4 | Step 1 Passed? | Determine whether ingredients satisfy food safety requirements. | Service Caterers | Decision point |
| 5 | Perform Inspection Step 2 | Catering Staff inspects cooked food after preparation, including appearance, odor, texture, and cooking completion. | Service Caterers | Inspection after cooking |
| 6 | Record Inspection Step 2 | System records inspection results, evidence photos, GPS location, and timestamp. | System | Traceability |
| 7 | Step 2 Passed? | Determine whether cooked food satisfies food safety requirements. | Service Caterers | Decision point |
| 8 | Perform Inspection Step 3 | Catering Staff inspects meals before serving, verifying food quality, meal portions, serving conditions, and hygiene compliance. | Service Caterers | Final inspection |
| 9 | Record Inspection Step 3 | System records inspection results, evidence photos, GPS location, and timestamp. | System | Traceability |
| 10 | Step 3 Passed? | Determine whether meals are safe for serving. | Service Caterers | Decision point |
| 11 | Generate Inspection Record | System generates a complete three-step inspection record linked to ingredient batches and meal production records. | System | Audit trail |
| 12 | Approve Meals for Serving | Meals are approved and released for serving activities. | System | Successful completion |
| 13 | Record Non-Conformance | System records food safety non-conformance details. | System | Failed inspection |
| 14 | Create Corrective Action Request | System creates a corrective action request and notifies responsible personnel. | System | Incident prevention |
| 15 | Re-inspection Required | Catering Staff performs corrective actions and repeats the failed inspection step. | Service Caterers | Return to failed step |

#### ![](data:image/png;base64...)Food Sample Retention Workflow

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Complete Food Inspection | The meal batch successfully passes all three food inspection steps. | System | Start of workflow |
| 2 | Identify Meals for Retention | Catering Staff identifies prepared food items that require sample retention according to operational procedures and food safety regulations. | Service Caterers | Sample selection |
| 3 | Collect Food Samples | Catering Staff collects representative samples from each prepared food item. | Service Caterers | Physical sample collection |
| 4 | Label Food Samples | Catering Staff labels each sample with meal information, production date, retention date, and batch reference. | Service Caterers | Sample identification |
| 5 | Capture Evidence Photos | Catering Staff captures photos of retained food samples and sample labels as operational evidence. | Service Caterers | Mandatory evidence |
| 6 | Capture GPS and Timestamp | System automatically records GPS location, device information, and timestamp during the sample retention process. | System | Anti-fraud control |
| 7 | Verify Retention Location | System verifies that sample retention is performed within the designated kitchen or storage area. | System | Location validation |
| 8 | Create Sample Record | System creates a food sample retention record linked to menu information, inspection records, and meal batch information. | System | Traceability |
| 9 | Store Food Samples | Catering Staff places food samples in the designated retention storage area. | Service Caterers | Physical storage |
| 10 | Record Storage Information | System records storage location and retention expiration date for retained samples. | System | Retention tracking |
| 11 | Monitor Retention Period | System continuously monitors the required food sample retention duration. | System | Automatic monitoring |
| 12 | Retention Period Expired? | System determines whether the mandatory retention period has elapsed. | System | Decision point |
| 13 | Dispose of Food Samples | Catering Staff disposes retained food samples according to food safety procedures after the retention period expires. | Service Caterers | Disposal process |
| 14 | Record Disposal Information | System records disposal details, responsible staff, and disposal timestamp. | System | End of normal flow |
| 15 | Retrieve Food Sample | Medical Staff retrieves retained samples for investigation when a food safety incident occurs. | Medical Staff | Alternative flow |
| 16 | Record Investigation Usage | System records sample retrieval and investigation activities for traceability purposes. | System | Incident support |

#### Allergy & Special Meal Management Workflow

![](data:image/png;base64...)

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Submit Health Information | Parent submits student health information, allergy details, and special dietary requirements. | Parent | Start of workflow |
| 2 | Create Health Declaration Record | System creates a health declaration record for review. | System | Record creation |
| 3 | Review Health Information | Medical Staff reviews the submitted health and allergy information. | Medical Staff | Verification process |
| 4 | Additional Verification Required? | Medical Staff determines whether supporting documents or clarification are required. | Medical Staff | Decision point |
| 5 | Request Additional Information | System requests additional information from the parent. | System | Alternative flow |
| 6 | Verify Allergy Information | Medical Staff verifies and approves allergy information. | Medical Staff | Validation |
| 7 | Create Allergy Profile | System creates or updates the student's allergy profile. | System | Health record update |
| 8 | Classify Special Meal Category | System classifies the student into predefined categories such as Seafood Allergy, Milk Allergy, Egg Allergy, Vegetarian, or Other Special Diets. | System | Categorization |
| 9 | Generate Allergy Alerts | System generates allergy alerts and distributes them to relevant stakeholders. | System | Risk prevention |
| 10 | Notify Service Caterers | System sends allergy and special meal information to the Service Caterers. | System | Operational notification |
| 11 | Review Special Meal Requirements | Catering Manager reviews special meal requirements and allergy categories. | Service Caterers | Meal planning support |
| 12 | Prepare Special Meal Plan | Catering Manager prepares alternative meal options for affected categories. | Service Caterers | Meal preparation planning |
| 13 | Monitor Allergy Compliance | System continuously validates approved menus against student allergy profiles. | System | Ongoing monitoring |
| 14 | Allergy Conflict Detected? | System detects whether an approved menu contains allergens affecting registered students. | System | Decision point |
| 15 | Generate Allergy Warning | System sends warnings to Service Caterers, Medical Staff, and Teachers. | System | Risk alert |
| 16 | Update Alternative Meal Plan | Service Caterers updates special meal arrangements to eliminate the risk. | Service Caterers | Corrective action |
| 17 | Store Allergy Management Records | System stores all declarations, verifications, alerts, and meal arrangements for audit purposes. | System | End of workflow |

#### Meal Serving Workflow

![C:\Users\Admin\AppData\Local\Microsoft\Windows\INetCache\Content.MSO\89D3720B.tmp](data:image/png;base64...)

| **Step** | **Step Name** | **Detailed Description** | **Role** | **Note** |
| --- | --- | --- | --- | --- |
| **1** | Prepare Meal Portions | Follow Allocation List from Work Flow 3.1.2.9 | Kitchen Manager | Start of workflow |
| **2** | Deliver Meals to Classrooms | Delivering meals to each classroom. | Kitchen Manager |  |
| **3** | Distribute Meals to Students | Distribute meals to each student follow the list of special meals | Teacher |  |
| **4** | Meal Distribution Correct? | Check if the meal distribution is correct. | Teacher | Decision point (Yes: Go to 6, No: Go to 5) |
| **5** | Correct Error | Staff replaces or corrects the meal assignment | Kitchen Manager | Corrective action (Go back to 3) |
| **6** | Complete Meal Distribution | Record that the distribution is complete, marking all students who have received the correct portion. | Teacher | Go to 7 |
| **7** | Record Serving Info | Record service time, staff, quantity | SFMS | Traceability |
| **8** | Monitor & Record Meal Consumption | Record whether students actually ate/didn't eat/ate only a portion. | Teacher |  |
| **9** | Submit Meal Feedback | Reports student feedback, observations or issues | Teacher | Go to 10 |
| **10** | Potential Safety Incident Reported? | Determine whether any food safety issue or adverse reaction is reported. | SFMS | Decision point (Yes: Go to 12, No: Go to 11) |
| **11** | Complete Service | Meal distribution is completed successfully | SFMS | End of normal flow |
| **12** | Create Incident Notification | Record reported incident and notify responsible parties | SFMS | Alternative flow (Go to 13) |
| **13** | Transfer to WF 3.1.2.12. Incident Handling Workflow | Transfer process to Food Safety Incident Handling Workflow. | SFMS | Transition to next workflow |
| **14** | Create Record | Creates a food safety incident log file. | SFMS | Alternative flow |
| **15** | Complete Service | Meal distribution is completed successfully. | SFMS | End of normal flow |

#### Food Safety Incident Handling Workflow

![C:\Users\Admin\AppData\Local\Microsoft\Windows\INetCache\Content.MSO\A0F6B43E.tmp](data:image/png;base64...)

| **Step** | **Step Name** | **Detailed Description** | **Role** | **Note** |
| --- | --- | --- | --- | --- |
| **1** | Review Incident Information | Review incident details and symptoms | Medical Staff | Start of workflow (triggered from WF 3.1.2.11) |
| **2** | Food Safety Related? | Determine if the incident is related to food safety. | Medical Staff | Decision point (Yes: Go to 4, No: Go to 3) |
| **3** | Close Non-Food-Safety Case | Close case or transfer to another process | Medical Staff | End of unconfirmed case |
| **4** | Assess Incident Severity | Evaluate severity and affected students | Medical Staff |  |
| **5** | Provide Medical Treatment | Provide immediate medical assistance | Medical Staff |  |
| **6** | High Severity Incident? | Determine whether the incident requires immediate emergency response. | Medical Staff | Decision point (Yes: Go to 7, No: Go to 8) |
| **7** | Activate Emergency Response | Initiate emergency response procedures | Medical Staff | Emergency protocol |
| **8** | Start Investigation | Begin formal incident investigation | Medical Staff |  |
| **9** | Retrieve Food Sample Logs | Retrieve retained food sample records | SFMS | Traceability |
| **10** | Retrieve Ingredient Batches | Retrieve ingredient batch information | SFMS | Traceability |
| **11** | Trace Supplier Information | Identify supplier and delivery records | Kitchen Manager | Source identification |
| **12** | Collect Supporting Evidence | Gather reports, photos and witness statements | Medical Staff | Evidence collection |
| **13** | Identify Root Cause | Determine the source of the incident | Medical Staff | Root cause analysis |
| **14** | Root Cause Confirmed? | Determine whether sufficient evidence exists to confirm the root cause. | Medical Staff | Decision point (Yes: Go to 16, No: Go to 15) |
| **15** | Additional Investigation | Perform further investigation activities | Medical Staff | Alternative flow (Go back to 12) |
| **16** | Define Corrective Actions | Define preventive and corrective measures | Medical Staff | Resolution planning (Go to 17) |
| **17** | Corrective Actions Approved? | Determine if proposed corrective actions are approved. | School Administration | Decision point (Yes: Go to 18, No: Go to 16) |
| **18** | Implement Corrective Actions | Execute approved corrective measures | Kitchen Manager | Corrective process |
| **19** | Monitor Corrective Actions | Monitor implementation effectiveness | School Administration | Compliance verification |
| **20** | Generate Incident Report | Generate formal incident report | SFMS | Documentation |
| **21** | Review Incident Report | Review and validate final report | School Administration | Validation step |
| **22** | Close Incident | Close incident and archive records | School Administration | End of workflow |

#### Parent Meal Monitoring Workflow

![C:\Users\Admin\AppData\Local\Microsoft\Windows\INetCache\Content.MSO\4E35388.tmp](data:image/png;base64...)

| **Step** | **Step Name** | **Detailed Description** | **Role** | **Note** |
| --- | --- | --- | --- | --- |
| **1** | Access Meal Monitoring Portal | Parent accesses the meal monitoring section of the system. | Parent | Start of workflow |
| **2** | View Published Menu | SFMS displays approved menus for upcoming meal periods. | SFMS | Information display |
| **3** | View Allergy Information | Displays allergy info and special meal arrangements for students. | SFMS | Information display |
| **4** | Review Special Meal Status | Reviews whether alternative meals are arranged for students. | Parent | Review step |
| **5** | View Food Safety Information | SFMS displays food safety inspection results, compliance status, and food safety announcements related to meal services. | SFMS | Information display |
| **6** | Receive Health Alerts | Sends alerts related to allergy risks or student health concerns. | SFMS | Notification |
| **7** | Submit Meal Feedback | Submits feedback on meal quality, nutrition, or food service. | Parent | Feedback submission |
| **8** | Create Feedback Record | SFMS records feedback and routes to relevant stakeholders. | SFMS | Record creation |
| **9** | Feedback Requires Investigation? | Determine whether the feedback requires further investigation as an incident. | SFMS | Decision point (Yes: Go to 10, No: Go to 11) |
| **10** | Create Incident Report | SFMS creates food safety incident reports for further investigation. | SFMS | Alternative flow |
| **11** | Store Monitoring Records | SFMS stores meal monitoring and feedback history records. | SFMS | End of workflow |

### Generation – Specification

#### Actors

| # | Actor | Description |
| --- | --- | --- |
| 1 | System Admin | Platform-level administrator responsible for managing system configuration, user accounts, roles, permissions, security settings, and audit logs across all schools using the platform. |
| 2 | School Administration | School-level administrator responsible for supervising food safety operations, approving suppliers and menus, monitoring compliance, reviewing reports, and managing food safety incidents within the school. |
| 3 | Service Caterers | Third-party food service provider responsible for ingredient receiving, inventory management, menu planning, food preparation, food inspection, food sample retention, meal serving, and implementation of corrective actions related to food safety. |
| 4 | Raw Food Supplier | Supplier responsible for providing raw food ingredients, maintaining supplier profiles, uploading compliance certificates, declaring delivery batches, and supporting food traceability requirements. |
| 5 | Medical Staff | School health officer responsible for managing student health records, verifying allergy information, monitoring food safety risks, investigating food safety incidents, and supervising food sample management activities. |
| 6 | Teacher | Homeroom teacher is responsible for monitoring students during meal service, viewing allergy alerts, receiving food safety notifications, updating student information, and assisting in food safety incident reporting. |
| 7 | Parent | Parents or guardians are responsible for providing student health information, declaring allergies and dietary requirements, monitoring menus and nutrition information, receiving notifications, and submitting feedback regarding school meals. |

Table ‑ Actors List

#### Use Case Diagrams

#### 3.1.3.2.1 UCs for Guest

![](data:image/png;base64...)

Figure ‑ UCs for Guest

#### 3.1.3.2.2 UCs for User

![](data:image/png;base64...)

Figure ‑ UCs for User

#### 3.1.3.2.3 UCs for System Admin

![](data:image/png;base64...)

Figure ‑ UCs for System Admin

#### 3.1.3.2.4 UCs for School Administration

![](data:image/png;base64...)

Figure ‑ UCs for School Administration

#### 3.1.3.2.5 UCs for Teacher

![](data:image/png;base64...)

Figure ‑ UCs for Teacher

#### 3.1.3.2.6 UCs for Parent

![](data:image/png;base64...)

Figure ‑ UCs for Parent

#### 3.1.3.2.7 UCs for Medical Staff

![](data:image/png;base64...)

Figure ‑ UCs for Medical Staff

#### 3.1.3.2.8 UCs for Raw Food Supplier

![](data:image/png;base64...)

Figure ‑ UCs for Raw Food Supplier

#### 3.1.3.2.9 UCs for Service Caterers

![](data:image/png;base64...)

Figure ‑ UCs for Service Caterers

#### Use Cases Description

| # | Actor | Use Case | Feature | Description |
| --- | --- | --- | --- | --- |
| UC-001 | System Admin | Active/Deactivate School | School & Class Management | Allows System Admin to activate or deactivate a school and control its operational status within the platform. |
| UC-002 | School Administration | Active/Deactivate Staff | User Management & Access Control | Allows School Administration to activate or deactivate staff accounts within the school and control their access to system functions. |
| UC-003 | System Admin | Active/Deactivate User | User Management & Access Control | Allows System Admin to activate or deactivate user accounts across the platform and enforce account access policies. |
| UC-004 | School Administration | Approval / Reject Service Caterers | Supplier & Partner Management | Enables the school administration to successfully approval / reject Service Caterers within the SFMS. |
| UC-005 | School Administration | Approval / Reject Corrective Action | Incident & Emergency Management | Enables the school administration to successfully approval / reject corrective action within the SFMS. |
| UC-006 | School Administration | Approval / Reject Menu | Menu & Diet Management | Enables the school administration to successfully approval / reject menu within the SFMS. |
| UC-007 | School Administration | Approval / Reject Supplier | Supplier & Partner Management | Enables the school administration to successfully approval / reject supplier within the SFMS. |
| UC-008 | School Administration | Assign Homeroom Teacher | School & Class Management | Allows School Administration to assign or reassign homeroom teachers to classrooms. |
| UC-009 | Medical Staff | Assign Incident Severity | Incident & Emergency Management | Enables the medical staff to successfully assign incident severity within the SFMS. |
| UC-010 | School Administration | Bulk Import Student & Parent | Student & Parent Management | Allows School Administration to import student and parent records in bulk using predefined templates and validation rules. |
| UC-011 | User | Change Password | User Management & Access Control | Allows authenticated users to change their account password and maintain account security. |
| UC-012 | User | Chat with AI | AI & Supporting Services | Enables the user to successfully chat with ai within the SFMS. |
| UC-013 | School Administration | Close Incident | Incident & Emergency Management | Enables the school administration to successfully close incident within the SFMS. |
| UC-014 | Medical Staff | Conduct Food Safety Inspection | Food Safety Inspection & Monitoring | Enables the medical staff to successfully conduct food safety inspection within the SFMS. |
| UC-015 | Raw Food Supplier | Confirm Delivery Schedule | Ingredient & Inventory Management | Enables the raw food supplier to successfully confirm delivery schedule within the SFMS. |
| UC-016 | Teacher | Confirm Meal Distribution | Meal Serving & Distribution | Enables the teacher to successfully confirm meal distribution within the SFMS. |
| UC-017 | School Administration | Create Announcement | Feedback & Announcement Management | Enables the school administration to successfully create announcement within the SFMS. |
| UC-018 | School Administration | Create Classroom | School & Class Management | Allows School Administration to create classrooms and define class information within the school. |
| UC-019 | Raw Food Supplier | Create Info Delivery Person | Supplier & Partner Management | Enables the raw food supplier to successfully create info delivery person within the SFMS. |
| UC-020 | Service Caterers | Create Menu | Menu & Diet Management | Enables the Service Caterers to successfully create menu within the SFMS. |
| UC-021 | Service Caterers | Create Purchase Request | Ingredient & Inventory Management | Enables the Service Caterers to successfully create purchase request within the SFMS. |
| UC-022 | System Admin | Create Role | User Management & Access Control | Allows System Admin to create new roles and define access permissions for system users. |
| UC-023 | Service Caterers | Create Special Meal Plan | Menu & Diet Management | Enables the Service Caterers to successfully create special meal plan within the SFMS. |
| UC-024 | School Administration | Create Staff | User Management & Access Control | Enables the school administration to successfully create staff within the SFMS. |
| UC-025 | School Administration | Create Student | Student & Parent Management | Allows School Administration to create new student records and associate them with parent accounts. |
| UC-026 | System Admin | Create User | User Management & Access Control | Enables the system admin to successfully create user within the SFMS. |
| UC-027 | School Administration | Daily Food Safety Monitoring | Food Safety Inspection & Monitoring | Enables the school administration to successfully daily food safety monitoring within the SFMS. |
| UC-028 | Raw Food Supplier | Delete Face Data | Supplier & Partner Management | Enables the raw food supplier to successfully delete face data within the SFMS. |
| UC-029 | System Admin | Delete Role | User Management & Access Control | Allows System Admin to remove unused roles while maintaining authorization consistency. |
| UC-030 | Service Caterers | Dispose Food Sample | Food Sample Retention | Enables the Service Caterers to successfully dispose food sample within the SFMS. |
| UC-031 | School Administration | Export Incident Reports | Incident & Emergency Management | Enables the school administration to successfully export incident reports within the SFMS. |
| UC-032 | User | Forgot Password | User Management & Access Control | Allows users to reset their password through a secure  account recovery process. |
| UC-033 | Medical Staff | Generate Incident Report | Incident & Emergency Management | Enables the medical staff to successfully generate incident report within the SFMS. |
| UC-034 | User | Login | User Management & Access Control | Allows users to authenticate with valid credentials and access authorized system functions. |
| UC-035 | User | Logout | User Management & Access Control | Allows users to securely terminate their current session and exit the system. |
| UC-036 | Medical Staff | View List Allergy & Special Meals | Student & Parent Management | Allows Medical Staff to manage allergy profiles and special meal requirements for students. |
| UC-037 | School Administration | Manage Classrooms | School & Class Management | Enables the School Administration to query, filter, and view the classrooms list, acting as the portal for create, edit, and teacher assignment actions. |
| UC-038 | Service Caterers | Manage Corrective Actions | Incident & Emergency Management | Enables the Service Caterers to successfully manage corrective actions within the SFMS. |
| UC-039 | Raw Food Supplier | Manage Deliveries | Ingredient & Inventory Management | Enables the raw food supplier to successfully manage deliveries within the SFMS. |
| UC-040 | System Admin | Manage External Integrations | AI & Supporting Services | Enables the system admin to successfully manage external integrations within the SFMS. |
| UC-041 | Medical Staff | Manage Food Safety Incidents | Incident & Emergency Management | Enables the medical staff to successfully manage food safety incidents within the SFMS. |
| UC-042 | Service Caterers | Manage Food Sample Retention | Food Sample Retention | Enables the Service Caterers to successfully manage food sample retention within the SFMS. |
| UC-043 | Raw Food Supplier | Manage Ingredient Batches | Ingredient & Inventory Management | Enables the raw food supplier to successfully manage ingredient batches within the SFMS. |
| UC-044 | Service Caterers | Manage Ingredient Receiving | Ingredient & Inventory Management | Enables the Service Caterers to successfully manage ingredient receiving within the SFMS. |
| UC-045 | Service Caterers | Manage Inventory | Ingredient & Inventory Management | Enables the Service Caterers to successfully manage inventory within the SFMS. |
| UC-046 | Parent | Manage Meal Feedback | Feedback & Announcement Management | Enables the parent to successfully manage meal feedback within the SFMS. |
| UC-047 | Service Caterers | Manage Meal Service | Meal Serving & Distribution | Enables the Service Caterers to successfully manage meal service within the SFMS. |
| UC-048 | Service Caterers | Manage Menus | Menu & Diet Management | Enables the Service Caterers to successfully manage menus within the SFMS. |
| UC-049 | Raw Food Supplier | Manage Profile | Supplier & Partner Management | Enables the raw food supplier to successfully manage profile within the SFMS. |
| UC-050 | Parent | View List Student Health Information | Student & Parent Management | Allows Parents to maintain and monitor health information associated with their children. |
| UC-051 | Medical Staff | View List Student Health Records | Student & Parent Management | Allows Medical Staff to manage verified health records and maintain student medical histories. |
| UC-052 | Service Caterers | Manage Three-Step Food Inspection | Food Safety Inspection & Monitoring | Enables the Service Caterers to successfully manage three-step food inspection within the SFMS. |
| UC-053 | Parent | Monitor Child Meal History | Meal Serving & Distribution | Enables the parent to successfully monitor child meal history within the SFMS. |
| UC-054 | Medical Staff | Monitor Food Safety | Food Safety Inspection & Monitoring | Enables the medical staff to successfully monitor food safety within the SFMS. |
| UC-055 | Parent | Monitor School Meals | Meal Serving & Distribution | Enables the parent to successfully monitor school meals within the SFMS. |
| UC-056 | Teacher | Monitor Student Meal History | Meal Serving & Distribution | Enables the teacher to successfully monitor student meal history within the SFMS. |
| UC-057 | System Admin | Monitor System Activities | AI & Supporting Services | Enables the system admin to successfully monitor system activities within the SFMS. |
| UC-058 | User | Receive Notification | AI & Supporting Services | Allows users to receive system notifications, alerts, and important operational updates. |
| UC-059 | User | Profile Manage | User Management & Access Control | Enables the authenticated User to access their personal profile hub containing details, role assignments, contact info, and account actions. |
| UC-060 | Service Caterers | Record Ingredient Receiving | Ingredient & Inventory Management | Enables the Service Caterers to successfully record ingredient receiving within the SFMS. |
| UC-061 | Service Caterers | Record Ingredient Storage | Ingredient & Inventory Management | Enables the Service Caterers to successfully record ingredient storage within the SFMS. |
| UC-062 | Service Caterers | Record Ingredients Before Processing | Ingredient & Inventory Management | Enables the Service Caterers to successfully record ingredients before processing within the SFMS. |
| UC-063 | Service Caterers | Record Ingredients In Processing | Ingredient & Inventory Management | Enables the Service Caterers to successfully record ingredients in processing within the SFMS. |
| UC-064 | Service Caterers | Record Inventory Transactions | Ingredient & Inventory Management | Enables the Service Caterers to successfully record inventory transactions within the SFMS. |
| UC-065 | Service Caterers | Record Meals Before Serving | Food Safety Inspection & Monitoring | Enables the Service Caterers to successfully record meals before serving within the SFMS. |
| UC-066 | Service Caterers | Record Prepare Ingredient Information | Ingredient & Inventory Management | Enables the Service Caterers to successfully record prepare ingredient information within the SFMS. |
| UC-067 | Guest | Register Service Caterers Account | Supplier & Partner Management | Enables the guest to successfully register Service Caterers account within the SFMS. |
| UC-068 | Raw Food Supplier | Register Face Data | Supplier & Partner Management | Enables the raw food supplier to successfully register face data within the SFMS. |
| UC-069 | Service Caterers | Register Food Sample | Food Sample Retention | Enables the Service Caterers to successfully register food sample within the SFMS. |
| UC-070 | Guest | Register School | School & Class Management | Allows a school representative to submit a registration request for joining the platform. |
| UC-071 | Guest | Register Supplier Account | Supplier & Partner Management | Enables the guest to successfully register supplier account within the SFMS. |
| UC-072 | Service Caterers | Reject Ingredient Batch | Ingredient & Inventory Management | Enables the Service Caterers to successfully reject ingredient batch within the SFMS. |
| UC-073 | Parent | Report Food Safety Concern | Incident & Emergency Management | Enables the parent to successfully report food safety concern within the SFMS. |
| UC-074 | Teacher | Report Food Safety Incident | Incident & Emergency Management | Enables the teacher to successfully report food safety incident within the SFMS. |
| UC-075 | Service Caterers | Request AI Menu Recommendations | Menu & Diet Management | Enables the Service Caterers to successfully request ai menu recommendations within the SFMS. |
| UC-076 | Medical Staff | Retrieve Food Sample Records | Food Sample Retention | Enables the medical staff to successfully retrieve food sample records within the SFMS. |
| UC-077 | Medical Staff | Review Health Information | Student & Parent Management | Allows Medical Staff to review and verify submitted student health information. |
| UC-078 | Medical Staff | Review Special Meal Requests | Student & Parent Management | Allows Medical Staff to review and validate requests for special meals and dietary accommodations. |
| UC-079 | Service Caterers | Scan Face Data | AI & Supporting Services | Enables the Service Caterers to successfully scan face data within the SFMS. |
| UC-080 | Teacher | Student Meal Consumption | Meal Serving & Distribution | Enables the teacher to successfully student meal consumption within the SFMS. |
| UC-081 | Raw Food Supplier | Submit Batch Information | Ingredient & Inventory Management | Enables the raw food supplier to successfully submit batch information within the SFMS. |
| UC-082 | Service Caterers | Submit Corrective Action Results | Incident & Emergency Management | Enables the Service Caterers to successfully submit corrective action results within the SFMS. |
| UC-083 | Raw Food Supplier | Submit Delivery Information | Ingredient & Inventory Management | Enables the raw food supplier to successfully submit delivery information within the SFMS. |
| UC-084 | Parent | Submit Health & Allergy Information | Student & Parent Management | Enables the parent to successfully submit health & allergy information within the SFMS. |
| UC-085 | Parent | Submit Meal & Food Quality Feedback | Feedback & Announcement Management | Enables the parent to successfully submit meal & food quality feedback within the SFMS. |
| UC-086 | Teacher | Submit Meal Feedback | Feedback & Announcement Management | Enables the teacher to successfully submit meal feedback within the SFMS. |
| UC-087 | Service Caterers | Submit Menu For Approval | Menu & Diet Management | Enables the Service Caterers to successfully submit menu for approval within the SFMS. |
| UC-088 | User | Manage Notification Preferences | AI & Supporting Services | Allows users to enable, disable, and customize notification preferences. |
| UC-089 | School Administration | Update Announcement | Feedback & Announcement Management | Enables the school administration to successfully update announcement within the SFMS. |
| UC-090 | School Administration | Update Classroom | School & Class Management | Allows School Administration to modify classroom information and maintain class records. |
| UC-091 | Raw Food Supplier | Update Compliance Documents | Supplier & Partner Management | Enables the raw food supplier to successfully update compliance documents within the SFMS. |
| UC-092 | Raw Food Supplier | Update Delivery Status | Ingredient & Inventory Management | Enables the raw food supplier to successfully update delivery status within the SFMS. |
| UC-093 | Parent | Update Health & Allergy Information | Student & Parent Management | Allows Parents to update previously submitted health and allergy information when changes occur. |
| UC-094 | Medical Staff | Update Health Records | Student & Parent Management | Allows Medical Staff to update verified health records based on assessments or new medical information. |
| UC-095 | System Admin | Update Information School | School & Class Management | Allows System Admin to modify school information and maintain accurate school records. |
| UC-096 | Service Caterers | Update Menu | Menu & Diet Management | Enables the Service Caterers to successfully update menu within the SFMS. |
| UC-097 | User | Update Profile | User Management & Access Control | Allows users to update their profile information and maintain accurate personal records. |
| UC-098 | System Admin | Update Role | User Management & Access Control | Allows System Admin to modify role definitions and associated permissions. |
| UC-099 | School Administration | Update Student | Student & Parent Management | Allows School Administration to update student information and maintain accurate enrollment records. |
| UC-100 | Raw Food Supplier | Upload Batch Certificates | Ingredient & Inventory Management | Enables the raw food supplier to successfully upload batch certificates within the SFMS. |
| UC-101 | Raw Food Supplier | Upload Food Safety Certificates | Supplier & Partner Management | Enables the raw food supplier to successfully upload food safety certificates within the SFMS. |
| UC-102 | Raw Food Supplier | Upload Product Origin Documents | Supplier & Partner Management | Enables the raw food supplier to successfully upload product origin documents within the SFMS. |
| UC-103 | Service Caterers, Teacher | View Allergy Alerts | Meal Serving & Distribution | Enables the Service Caterers, teacher to successfully view allergy alerts within the SFMS. |
| UC-104 | Medical Staff, Parent | View Allergy Information | Student & Parent Management | Allows authorized users to view verified allergy information and dietary restrictions. |
| UC-105 | School Administration | View Announcement Detail | Feedback & Announcement Management | Enables the school administration to successfully view announcement detail within the SFMS. |
| UC-106 | System Admin | View Audit Logs | AI & Supporting Services | Enables the system admin to successfully view audit logs within the SFMS. |
| UC-107 | Raw Food Supplier | View Batch History | Ingredient & Inventory Management | Enables the raw food supplier to successfully view batch history within the SFMS. |
| UC-108 | School Administration | View Service Caterers Detail | Supplier & Partner Management | Enables the school administration to successfully view Service Caterers detail within the SFMS. |
| UC-109 | Raw Food Supplier | View Delivery Batch Records | Ingredient & Inventory Management | Enables the raw food supplier to successfully view delivery batch records within the SFMS. |
| UC-110 | Raw Food Supplier | View Delivery Person List | Supplier & Partner Management | Enables the raw food supplier to successfully view delivery person list within the SFMS. |
| UC-111 | System Admin | View Error Logs | AI & Supporting Services | Enables the system admin to successfully view error logs within the SFMS. |
| UC-112 | Medical Staff | View Food Inspection Records | Food Safety Inspection & Monitoring | Enables the medical staff to successfully view food inspection records within the SFMS. |
| UC-113 | School Administration | View Food Inspection Reports | Food Safety Inspection & Monitoring | Enables the school administration to successfully view food inspection reports within the SFMS. |
| UC-114 | School Administration | View Food Poisoning Incident List | Incident & Emergency Management | Enables the school administration to successfully view food poisoning incident list within the SFMS. |
| UC-115 | Medical Staff | View Food Safety Compliance Report | Food Safety Inspection & Monitoring | Enables the medical staff to successfully view food safety compliance report within the SFMS. |
| UC-116 | School Administration | View Food Safety Incident Detail | Incident & Emergency Management | Enables the school administration to successfully view food safety incident detail within the SFMS. |
| UC-117 | Medical Staff | View Food Safety Reports | Food Safety Inspection & Monitoring | Enables the medical staff to successfully view food safety reports within the SFMS. |
| UC-118 | Medical Staff, School Administration | View Food Sample Records | Food Sample Retention | Enables the medical staff, school administration to successfully view food sample records within the SFMS. |
| UC-119 | User | View History Chat | AI & Supporting Services | Enables the user to successfully view history chat within the SFMS. |
| UC-120 | Service Caterers | View Incident Information | Incident & Emergency Management | Enables the Service Caterers to successfully view incident information within the SFMS. |
| UC-121 | School Administration | View Incident Report | Incident & Emergency Management | Enables the school administration to successfully view incident report within the SFMS. |
| UC-122 | System Admin | View Information School | School & Class Management | Allows System Admin to view detailed information and operational status of registered schools. |
| UC-123 | School Administration | View Ingredient Receiving Records | Ingredient & Inventory Management | Enables the school administration to successfully view ingredient receiving records within the SFMS. |
| UC-124 | Service Caterers | View Inspection Records | Food Safety Inspection & Monitoring | Enables the Service Caterers to successfully view inspection records within the SFMS. |
| UC-125 | School Administration | View List Announcement | Feedback & Announcement Management | Enables the school administration to successfully view list announcement within the SFMS. |
| UC-126 | Parent, Teacher | View List Announcements | Feedback & Announcement Management | Enables the parent, teacher to successfully view list announcements within the SFMS. |
| UC-127 | School Administration | View List Service Caterers | Supplier & Partner Management | Enables the school administration to successfully view list Service Caterers within the SFMS. |
| UC-128 | School Administration | View List Menu | Menu & Diet Management | Enables the school administration to successfully view list menu within the SFMS. |
| UC-129 | System Admin | View Permission List | User Management & Access Control | Allows System Admin to review permissions assigned to system roles. |
| UC-130 | System Admin | View Role List | User Management & Access Control | Allows System Admin to view and manage available roles in the system. |
| UC-131 | School Administration | View List Supplier | Supplier & Partner Management | Enables the school administration to successfully view list supplier within the SFMS. |
| UC-132 | Service Caterers | View Meal Distribution List | Meal Serving & Distribution | Enables the Service Caterers to successfully view meal distribution list within the SFMS. |
| UC-133 | Parent | View Meal History | Meal Serving & Distribution | Enables the parent to successfully view meal history within the SFMS. |
| UC-134 | School Administration | View Menu Detail | Menu & Diet Management | Enables the school administration to successfully view menu detail within the SFMS. |
| UC-135 | Parent | View Menu Information | Menu & Diet Management | Enables the parent to successfully view menu information within the SFMS. |
| UC-136 | User | View Notification History | AI & Supporting Services | Allows users to review previously received notifications and alerts. |
| UC-137 | Teacher, Medical Staff, Service Caterers | View Policy Of School | School & Class Management | Allows Medical Staff to review school food safety policies and operational regulations. |
| UC-138 | User | View Profile | User Management & Access Control | Allows users to view their personal profile and account information. |
| UC-139 | Raw Food Supplier | View Purchase Requests | Ingredient & Inventory Management | Enables the raw food supplier to successfully view purchase requests within the SFMS. |
| UC-140 | System Admin | View School List | School & Class Management | Allows System Admin to search, filter, and review registered schools across the platform. |
| UC-141 | Parent | View Special Meal Arrangements | Meal Serving & Distribution | Enables the parent to successfully view special meal arrangements within the SFMS. |
| UC-142 | Parent | View Special Meal Records | Meal Serving & Distribution | Enables the parent to successfully view special meal records within the SFMS. |
| UC-143 | Teacher | View Special Meal Requirements | Meal Serving & Distribution | Enables the teacher to successfully view special meal requirements within the SFMS. |
| UC-144 | School Administration | View Staff Account List | User Management & Access Control | Allows School Administration to search, filter, and review staff accounts within the school. |
| UC-145 | Teacher | View Student Health & Allergy Information | Student & Parent Management | Allows Teachers to review student health alerts and allergy information relevant to classroom meal activities. |
| UC-146 | Medical Staff | View Student Health History | Student & Parent Management | Enables the medical staff to successfully view student health history within the SFMS. |
| UC-147 | School Administration | View Student List | Student & Parent Management | Enables the school administration to successfully view student list within the SFMS. |
| UC-148 | School Administration | View Supplier Detail | Supplier & Partner Management | Enables the school administration to successfully view supplier detail within the SFMS. |
| UC-149 | System Admin | View System Status | AI & Supporting Services | Enables the system admin to successfully view system status within the SFMS. |
| UC-150 | System Admin | View Usage Statistics | AI & Supporting Services | Enables the system admin to successfully view usage statistics within the SFMS. |
| UC-152 | Raw Food Supplier | Submit Supplier Application | Supplier & Partner Management | Enables the raw food supplier to successfully submit supplier application within the SFMS. |
| UC-153 | System Admin | View User Account List | User Management & Access Control | Allows System Admin to search, filter, and review registered user accounts across the platform. |
| UC-154 | User | Activate Account & Set Password | User Management & Access Control | Enables a new staff member or user to activate their account, set their initial password, and transition their account status. |
| UC-155 | Raw Food Supplier | Manage Supplier Application | Supplier & Partner Management | Enables the raw food supplier to successfully manage supplier application within the SFMS. |
| UC-156 | Service Caterers | Submit Catering Application | Supplier & Partner Management | Enables the service caterers to successfully submit catering application within the SFMS. |
| UC-157 | Service Caterers | Track Catering Application Status | Supplier & Partner Management | Enables the service caterers to successfully track catering application status within the SFMS. |
| UC-158 | Service Caterers | Update/Resubmit Catering Application | Supplier & Partner Management | Enables the service caterers to successfully update/resubmit catering application within the SFMS. |
| UC-159 | Service Caterers | Manage Catering Application | Supplier & Partner Management | Enables the service caterers to successfully manage catering application within the SFMS. |
| UC-161 | Service Caterers, School Administration | View List Meal Feedback | Meal Serving & Distribution | Enables the Service Caterers to safely query, view List Meal Feedback records. |
| UC-162 | Service Caterers, School Administration | View Meal Feedback Detail | Meal Serving & Distribution | Enables the Service Caterers to view the detailed information of a specific meal feedback. |
| UC-163 | Service Caterers | Respond Meal Feedback | Meal Serving & Distribution | Enables the Service Caterers to submit a response to a parent's or teacher's meal feedback. |
| UC-164 | School Administration | View Meal Feedback Dashboard | Meal Serving & Distribution | Enables the School Administration to view aggregated and anonymized feedback charts |
| UC-165 | School Administration | View Classroom Detail | School & Class Management | Enables the School Administration to view the comprehensive details of a specific classroom, including its profile, capacity, assigned homeroom teacher, and student list. |
| UC-166 | School Administration | View List Classrooms | School & Class Management | Enables the School Administration to query, filter, and view the list of all classrooms in the school. |
| UC-167 | Raw Food Supplier | Manage Compliance Documents | Supplier & Partner Management | Enables the raw food supplier to successfully manage compliance documents within the SFMS. |
| UC-168 | Medical Staff | Record Corrective Actions Report | Incident & Emergency Management | Enables the medical staff to successfully record corrective actions report within the SFMS. |
| UC-169 | Raw Food Supplier | Track Supplier Application Status | Supplier & Partner Management | Enables the raw food supplier to successfully track supplier application status within the SFMS. |
| UC-170 | Raw Food Supplier | Update/Resubmit Supplier Application | Supplier & Partner Management | Enables the raw food supplier to successfully update/resubmit supplier application within the SFMS. |

Table ‑ Use Cases Description

## Functional Specifications

### User Management & Access Control

#### Active/ Deactivate Staff

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-002 - Active/Deactivate Staff | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | School Administration selects a staff account and toggles its active/inactive status. | | |
| Description | Enables the School Administration to activate or deactivate a staff member's account. Deactivation immediately terminates all active sessions for that staff. | | |
| Preconditions | The school administration is authenticated and authorized. The target staff account exists in the school. | | |
| Postconditions | Staff account status is updated. If deactivated, all active sessions are immediately terminated. An email notification is sent to the staff member. Audit log records the action. | | |
| Normal Flow | 1. School Administration navigates Staff Account Management. 2. System displays all staff accounts with their current active/inactive status. 3. School Administration selects a target staff account and clicks the status toggle. 4. System checks that deactivating this account will not leave mandatory role assignments unfilled. 5. System updates the account status, terminates active sessions if deactivated (BR-007), sends a status update email to the staff member via the Email Service, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If deactivation leaves mandatory roles unfilled, system blocks the action and displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On demand | | |
| Business Rules | BR-007, BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ Active/Deactivate Staff Use Cases Specification

#### Active/Deactivate User

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-003 - Active/Deactivate User | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | Email Service |
| Trigger | System Admin selects a user account and toggles its active/inactive status. | | |
| Description | Enables the System Admin to activate or deactivate any user account platform wide. Deactivation immediately terminates all active sessions and revokes all access. | | |
| Preconditions | The system admin is authenticated and authorized. The target user account exists and is not the default primary system admin account. | | |
| Postconditions | User account status is updated. If deactivated, all active sessions are immediately terminated (BR-007). An email notification is sent to the user. Audit log records the action. | | |
| Normal Flow | 1. System Admin navigates to User Account Management. 2. System displays all user accounts with statuses. 3. System Admin selects the target account and clicks the status toggle. 4. System verifies the account is not the protected primary default system admin account.  5. The system checks whether locking this user's account voids the unique required role at the affiliated institution/school. If so, a transaction blocking warning is displayed. 6. System updates the status, terminates all active sessions if deactivated (BR-007), sends a status update email to the user via the Email Service, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If trying to deactivate the primary system admin, system blocks the action and displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-007, BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table ‑ Active/Deactivate User Use Cases Specification

#### Change Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-011 - Change Password | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user selects 'Change Password' from account settings. | | |
| Description | Enables the authenticated User to securely change their account password after verifying their current credentials, invalidating all other active sessions upon success. | | |
| Preconditions | The user is authenticated and has an active session. | | |
| Postconditions | The user password is updated. All existing sessions except the current one are invalidated. A password change confirmation notification is sent. | | |
| Normal Flow | 1. User navigates to Account Settings and selects 'Change Password'.  2. System displays the Change Password form with current password, new password, and confirmation fields.  3. User enters current password, new password, and confirms the new password.  4. System validates the current password and verifies the new password matches the confirmation.  5. System validates the new password against the password policy and password history (BR-006: must not match last 3 passwords).  6. System hashes the new password (BR-001), saves it to the database, terminates all other active sessions for this user, sends a confirmation notification, and displays MSG-030. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-002 (Invalid current password), MSG-004 (Session expired), MSG-005 (Access denied), MSG-063 (Weak password or policy violation). | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-001, BR-006, BR-007 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and access to their registered email. | | |

Table 3-4 Change Password Use Cases Specification

#### Create Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-022 - Create Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin navigates to Role Management and selects 'Create New Role'. | | |
| Description | Enables the System Admin to define a new custom user role with a unique name, description, and specific permission assignments. | | |
| Preconditions | The system admin is authenticated and authorized. The proposed role name does not already exist in the system. | | |
| Postconditions | The new custom role is saved in the database and becomes available for assignment to user accounts. | | |
| Normal Flow | 1. System Admin navigates to Role Management and selects 'Create New Role'.  2. System displays the role creation form with name, description, and permission assignment fields.  3. System Admin enters a unique role name, description, and assigns specific permissions.  4. System validates the role name is unique and all assigned permissions are valid.  5. System saves the role, records the creation in the audit trail, and displays a success confirmation (MSG-031). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If the role name is already in use, system displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-5 Create Role Use Cases Specification

#### Create Staff

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-024 - Create Staff | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | School Administration navigates to Staff Management and selects 'Create Staff Account'. | | |
| Description | Enables the School Administration to create a new staff account, assigning role and contact details, triggering an email verification for account activation. | | |
| Preconditions | The school administration is authenticated and authorized. The target email address is not already registered in the system. | | |
| Postconditions | Staff account created with 'Pending Email Verification' status. Verification email sent to staff member (BR-010). Account inactive until email verified. | | |
| Normal Flow | 1. School Administration navigates to Staff Management and selects 'Create Staff Account'. 2. System displays the staff creation form. 3. School Administration enters full name, email address, assigned role, and phone number. 4. System validates that the assigned role is a school-level staff role (Teacher, Medical Staff, Catering staff) and not an administrator or external role. 5. System validates email uniqueness (MSG-006 if duplicate) and phone number format (BR-012: valid +84 or 0xx format). 6. System creates the account, sends a verification email (BR-010), logs the creation, and displays MSG-031. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006. If phone number format is invalid, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-6 Create Staff Use Cases Specification

#### Create User

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-026 - Create User | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | Email Service |
| Trigger | System Admin navigates to User Management and selects 'Create User Account'. | | |
| Description | Enables the System Admin to create a user account for any system actor, assigning credentials, roles, and triggering email verification. | | |
| Preconditions | The system admin is authenticated and authorized. The target email address is not already registered. | | |
| Postconditions | New user account created. Verification email sent (BR-010). Account inactive until verified. Creation logged in audit trail. | | |
| Normal Flow | 1. System Admin navigates to User Management and selects 'Create User Account'. 2. System displays the user creation form. 3. System Admin enters full name, email, assigned role, and phone number (If the selected role is at the school level, the System Admin is required to select the affiliated school from the list). 4. System validates email uniqueness (MSG-006 if duplicate) and phone format (BR-012: valid +84 or 0xx format). 5. System creates account, sends verification email (BR-010), logs creation in audit trail, and displays MSG-031. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006. If phone format is invalid, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008, BR-010, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-7 Create User Use Cases Specification

#### Delete Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-029 - Delete Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin selects a custom role and clicks 'Delete Role'. | | |
| Description | Enables the System Admin to permanently remove a custom role, provided it is not a default system role and has no active users assigned. | | |
| Preconditions | The system admin is authenticated and authorized. The role exists, is a custom (non-default) role (BR-008), and has no active users currently assigned. | | |
| Postconditions | Custom role is permanently deleted from the database. Audit log records the deletion. | | |
| Normal Flow | 1. System Admin navigates to Role Management and selects the target custom role.  2. System displays role details including assigned user count.  3. System Admin clicks 'Delete Role' and confirms the action in the confirmation dialog.  4. System validates the role is not a default system role (BR-008) and has zero users (active or inactive) assigned.  5. System deletes the role, records the deletion in the audit trail, and displays a success confirmation. | | |
| Alternative Flows | AF1 - Role Has Assigned Users: System blocks deletion and shows the count of assigned users. System Admin must reassign all users to other roles before deletion can proceed. | | |
| Exceptions | MSG-004, MSG-005. If target role is a default role, deletion is blocked with a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-8 Delete Role Use Cases Specification

#### Forgot Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-032 - Forgot Password | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Email Service |
| Trigger | User clicks 'Forgot Password' on the login screen and submits their registered email. | | |
| Description | Enables the User to reset their account password via a time-limited email link without requiring current password knowledge. | | |
| Preconditions | The user account exists and is active. The user has access to their registered email address. | | |
| Postconditions | A password reset link is sent to the registered email. Reset token expires in 1 hour (BR-003). Password is updated on successful token verification. | | |
| Normal Flow | 1. User clicks 'Forgot Password' on the Login screen.  2. System displays the email input form.  3. User enters their registered email address and submits.  4. System validates the email is active, generates a secure reset token (1-hour expiry per BR-003), and sends the reset link via Email Service.  5. User clicks the link, enters a new password and confirms it.  6. System validates token validity, matches password confirmation, enforces password policy (BR-006: must not match last 3 passwords), hashes the password (BR-001), saves it, invalidates all other active sessions, and displays MSG-030. | | |
| Alternative Flows | AF1 - Token Expired: User accesses an expired reset link. System displays token expiry error and prompts user to request a new reset link. | | |
| Exceptions | MSG-003 (Account deactivated). If email is invalid or rate-limit is exceeded, system displays appropriate validation warning. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-001, BR-003, BR-006 | | |
| Other Information | Rate-limiting applies: maximum 1 reset request per 15 minutes per email. | | |
| Assumptions | User has access to their registered email account. | | |

Table 3-9 Forget Password Use Cases Specification

#### Login

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-034 - Login | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | User opens the app and inputs credentials. | | |
| Description | Authenticates User credentials and establishes an authorized system session. | | |
| Preconditions | User has a registered account and a stable internet connection. | | |
| Postconditions | User is logged in and redirected to their role-based dashboard. | | |
| Normal Flow | 1. User enters their registered email and password on the login screen.  2. System verifies that the account exists and is active (MSG-003 if deactivated).  3. System checks that the account is not locked out due to excessive failed attempts (BR-002: max 5 attempts, locked for 15 mins).  4. System validates the email and password against stored hash values.  5. System checks for concurrent logins (BR-005) and enforces single-device session rules.  6. System checks if email verification is complete (BR-010). If pending, blocks login and prompts verification.  7. System checks if it is the user's initial login (BR-009). If yes, redirects to Privacy Policy and Terms of Service acceptance screen.  8. System generates a secure session token (BR-004 timeout 30 mins), resets failed login counter, records the login in the audit trail, displays MSG-026, and redirects to dashboard. | | |
| Alternative Flows | AF1 - First-time Login (BR-009): System redirects user to Terms of Service acceptance screen. User must accept to proceed. Once accepted, system updates account status and redirects to dashboard.  AF2 - Account Lockout (BR-002): If user fails login 5 times consecutively, system locks account for 15 minutes and logs the event. | | |
| Exceptions | MSG-002 (Invalid credentials), MSG-003 (Account deactivated), MSG-062 (Suspicious login/verification code required). | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-002, BR-004, BR-005, BR-009, BR-010 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-10 Login Use Cases Specification

#### Logout

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-035 - Logout | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user selects 'Logout' from the navigation menu. | | |
| Description | Enables the authenticated User to securely terminate their active system session, invalidating the session token and redirecting to the login screen. | | |
| Preconditions | The user is authenticated with an active session. | | |
| Postconditions | User session is terminated. Session token is invalidated. User is redirected to the Login screen. Session termination is logged. | | |
| Normal Flow | 1. User clicks 'Logout' from the navigation menu or header.  2. System displays a logout confirmation prompt.  3. User confirms the logout action.  4. System invalidates the session token (BR-004) and clears session data.  5. System logs the session termination, redirects user to the Login screen, and displays MSG-027. | | |
| Alternative Flows | AF1 - Auto-Logout on Session Timeout: If the session has expired (30 min inactivity per BR-004), system automatically logs out the user and redirects to Login screen with a session expiry notification. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-004 | | |
| Other Information | None. | | |
| Assumptions | User has an active session in the system. | | |

Table 3-11 Logout Use Cases Specification

#### Profile Manage

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-059 - Profile Manage | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user navigates to 'My Profile' from the navigation menu. | | |
| Description | Enables the authenticated User to access their personal profile hub containing details, role assignments, contact info, and account actions. | | |
| Preconditions | The user is authenticated and has an active session. | | |
| Postconditions | User profile information is displayed. No data modifications occur unless the user initiates an edit action. | | |
| Normal Flow | 1. User navigates to 'My Profile' from the navigation menu.  2. System retrieves and displays the user's current profile data hub (UC-140): name, email, phone, role, and status.  3. User reviews their profile information.  4. User may choose to edit profile details (redirecting to UC-099) or change password (redirecting to UC-011). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-012, BR-013 | | |
| Other Information | This is the parent management page linking to detailed profile view/update and password change functions. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-12 Profile Manage Use Cases Specification

#### Update Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-097 - Update Profile | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Authenticated user selects 'Edit Profile' from the Profile Management page. | | |
| Description | Enables the authenticated User to update their personal profile information including name, phone number, and profile photo. | | |
| Preconditions | The user is authenticated and has an active session. | | |
| Postconditions | Profile data is updated in the database. Audit log entry is created. A success confirmation notification is displayed (MSG-029). | | |
| Normal Flow | 1. User navigates to Profile Management and selects 'Edit Profile'.  2. System displays an editable form pre-filled with current profile data.  3. User modifies fields: name, phone number (BR-012: valid format +84 or 0xx), or uploads a new profile photo (BR-013: JPG/JPEG/PNG, max 5MB).  4. System validates phone number format. System also validates photo file type and size constraints.  5. System saves the updated profile, creates an audit log entry, displays MSG-029, and refreshes the profile view. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If photo exceeds 5MB, system displays MSG-008. If photo format is unsupported, system displays MSG-009. If phone number format is invalid, system displays validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-012, BR-013 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-13 Update Profile Use Cases Specification

#### Update Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-098 - Update Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin requests to update role. | | |
| Description | Enables the System Admin to manage details, modify permissions, or update configuration fields of a custom role. | | |
| Preconditions | The system admin is authenticated and authorized. The target role exists and is a custom role (BR-008). | | |
| Postconditions | The update role transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. System Admin navigates to Role Management and selects the target Role to update.  2. System displays the edit role form with current role name, description, and permissions.  3. System Admin modifies the role description or toggles permissions.  4. System Admin submits the modifications.  5. System validates that the role is a custom role (BR-008) and name (if modified) is unique.  6. System saves the updated role, records the action in the audit trail, and displays a success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If trying to modify default roles, system blocks the action and displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-14 Update Role Use Cases Specification

#### View List Permission

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-129 - View List Permission | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view list permission. | | |
| Description | Enables the System Admin to safely query, view, or export the details of List Permission records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view list permission data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the List Permission section in the system portal.  2. System queries the database for all available permissions in the system.  3. System displays the list of permissions in a tabular view with columns: Permission ID, Name, Module/Category, and Description.  4. System Admin can search permissions by keyword, filter by module, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-15 View List Permission Use Cases Specification

#### View List Role

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-130 - View List Role | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view list role. | | |
| Description | Enables the System Admin to safely query, view, or export the details of List Role records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view list role data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the List Role section in the system portal.  2. System queries the database for all user roles (both default and custom).  3. System displays the list of roles in a tabular view showing Role Name, Description, User Count, and Status.  4. System Admin can search, filter, or navigate to see details of a specific role. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-16 View List Role Use Cases Specification

#### View Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-138 - View Profile | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Primary actor requests to view profile. | | |
| Description | Enables the User to safely query, view, or export the details of Profile records. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | The view profile data is successfully retrieved and displayed to the user. | | |
| Normal Flow | 1. User navigates to the Profile section in the system portal (or UC-059 requests data).  2. System queries the database for the user's profile details: name, email, phone, role, profile photo, and registration details.  3. System returns and displays the profile details on the user interface.  4. User reviews their profile details. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-012, BR-013 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-17 View Profile Use Cases Specification

#### View Staff Account List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-144 - View Staff Account List | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service |
| Trigger | Primary actor requests to view staff account list. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Staff Account List records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view staff account list data is successfully retrieved and displayed to the school administration. | | |
| Normal Flow | 1. School Administration navigates to the Staff Account List section in the system portal.  2. System queries the database to retrieve staff accounts belonging to the school.  3. System displays the list of staff members (name, email, role, phone, active status) in a table view.  4. School Administration can filter by role, active status, search by name/email, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-18 View Staff Account List Use Cases Specification

#### View User Account List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-153 - View User Account List | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | Email Service |
| Trigger | Primary actor requests to view user account list. | | |
| Description | Enables the System Admin to safely query, view, or export the details of User Account List records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view user account list data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the User Account List section in the system portal.  2. System queries the database to retrieve all user accounts across the platform.  3. System displays the platform-wide user account list (name, email, role, school/company, active status) in a tabular view.  4. System Admin can search, filter by role/school/status, and export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-008 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-19 View User Account List Use Cases Specification

#### Activate Account & Set Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-154 - Activate Account & Set Password | | |
| Created By | NamNVHE180863 | Date Created | 23/06/2026 |
| Primary Actor | User | Secondary Actors | Email Service |
| Trigger | User clicks the account activation link in the received verification email. | | |
| Description | Enables a new staff member or user to activate their account, set their initial password, and transition their account status from PENDING\_VERIFICATION to ACTIVE. | | |
| Preconditions | The target user account exists with status 'PENDING\_VERIFICATION'. The user has a valid, unused activation token. | | |
| Postconditions | The user account status is updated to 'ACTIVE'. The initial password is set and encrypted. The verification token is invalidated. The activation is logged in the audit trail. | | |
| Normal Flow | 1. User clicks the activation link in their email. 2. System extracts and validates the activation token: checking its validity and verifying that it has not expired (must be within 48 hours of creation per BR-011). 3. System displays the activation form with "New Password" and "Confirm Password" fields. 4. User enters their new password, confirms it, and clicks the "Activate Account" button. 5. System verifies that the new password matches the confirmation and validates password strength against the security policy (BR-006). 6. System hashes the password using BCrypt (BR-001) and saves it to the database. 7. System updates the account status to 'ACTIVE', invalidates the activation token, records the activation in the audit trail, displays MSG-031, and redirects the user to the Login screen. | | |
| Alternative Flows | AF1 - Activation Token Expired (> 48 hours): 1. User accesses an expired activation link. 2. System detects token expiration, displays an expiration error message, and provides a "Resend Activation Link" button. 3. User clicks "Resend Activation Link". 4. System generates a new activation token, extends it for another 48 hours, sends a new activation email via Email Service, and displays a success notification. | | |
| Exceptions | - Invalid Token: System blocks access and displays a validation error message. - Account Already Active: System redirects the user to the Login screen with an information message. | | |
| Priority | High | | |
| Frequency of Use | On-demand (when a new staff/user is onboarded) | | |
| Business Rules | BR-001, BR-006, BR-010, BR-011, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with an internet connection and access to their registered email. | | |

Table 3-19 Activate Account & Set Password Use Cases Specification

#### Login with Google (OAuth2)

*(Bổ sung 2026-07-28 — theo yêu cầu người dùng (Nam): hỗ trợ đăng nhập nhanh qua Google, giảm phụ thuộc email/mật khẩu. UC mới, cấp BR-218 tương ứng — xem `business_rules.md` changelog v1.9. Chỉ liên kết với tài khoản đã tồn tại, không tự tạo tài khoản mới qua Google — BR-013 vẫn do School Administration/System Admin kiểm soát việc tạo tài khoản.)*

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-191 - Login with Google (OAuth2) | | |
| Created By | Claude (theo yêu cầu Nam) | Date Created | 28/07/2026 |
| Primary Actor | User | Secondary Actors | Google OAuth2 Provider |
| Trigger | User selects "Sign in with Google" on the Login screen instead of entering email/password. | | |
| Description | Enables an existing User to authenticate using their Google account (OAuth2 Authorization Code flow) instead of email/password, linking to their existing SFMS account by matching verified email. | | |
| Preconditions | A SFMS account already exists with an email matching the Google account's verified email (created via UC-024/UC-026). The account is not INACTIVE/SUSPENDED (BR-010). | | |
| Postconditions | User is authenticated and issued an access token + refresh token (BR-004). If this is the first Google login, the account is linked to the Google identity (`googleSub`) for future logins. Audit log records the login. | | |
| Normal Flow | 1. User clicks "Sign in with Google" on the Login screen. 2. System redirects to the Google OAuth2 consent screen. 3. User authenticates and approves with their Google account. 4. Google redirects back with an authorization code; system exchanges it for the user's verified Google email and profile. 5. System looks up an existing SFMS account by email. 6. If found and not yet linked, system links the account to the Google identity (stores `googleSub`). 7. System issues an access token + refresh token (BR-004), overwrites any prior session (BR-005), records the login in the audit trail, and redirects to the role-based dashboard. | | |
| Alternative Flows | AF1 - Already linked: skip the linking step (step 6), log in directly. | | |
| Exceptions | No matching SFMS account for the Google email: system blocks login and displays MSG-102. OAuth2 token exchange fails: system displays MSG-103. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-004, BR-005, BR-010, BR-218 | | |
| Other Information | Does not apply BR-001/BR-006/BR-015 (no local password for a Google-authenticated session). | | |
| Assumptions | User has a running environment with internet connection and a Google account whose email matches their SFMS account. | | |

Table 3-20 Login with Google Use Cases Specification

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

### Menu & Diet Management

#### Approval / Reject Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-006 - Approval / Reject Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration opens a pending menu submission from the approval dashboard. | | |
| Description | Enables the authorized School Admin to review and approve or reject a menu submitted by the Service Caterers, ensuring nutritional and allergy compliance before publication. | | |
| Preconditions | School Admin is authenticated and authorized. A menu is in 'Pending Approval' status. System has completed the automatic allergy conflict check. | | |
| Postconditions | Approved: Menu locked and published (BR-067, BR-070). Parents and teachers notified (BR-075). Approval logged with electronic signature (BR-066).  Rejected: Menu returned to 'Rejected' status. Rejection reason logged (BR-074) and communicated to Service Caterers. | | |
| Normal Flow | 1. School Admin navigates to Menu Approval Dashboard.  2. System lists menus awaiting review with submission date and allergy check status.  3. School Admin opens a menu and reviews dish options, nutritional content, and allergy accommodation (BR-062, BR-063, BR-069).  4. School Admin selects 'Approve' or 'Reject', adding rejection comments if rejecting (BR-074).  5. System logs the decision with electronic signature metadata (BR-066), updates status, displays MSG-040 (if approved), and notifies Service Caterers and relevant parties (BR-075 if approved). | | |
| Alternative Flows | AF1 - Reject Menu: School Admin selects Reject and provides rejection comments. System records rejection reason (BR-074), updates status to 'Rejected', notifies Service Caterers, and prevents publication.  AF2 - Allergy Conflict During Review: System detects allergy conflict (MSG-051). System blocks approval and School Admin must request menu revision (returns menu to 'Rejected'). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-063, BR-066, BR-067, BR-070, BR-074, BR-075 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-62 Approval / Reject Menu Use Cases Specification

#### Create Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-020 - Create Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers navigates to Menu Management and selects 'Create Menu'. | | |
| Description | Enables the Service Caterers to create a new weekly menu draft, selecting dishes and verifying nutritional content, allergy compliance, and dish rotation rules. | | |
| Preconditions | Service Caterers is authenticated and authorized. Current inventory and student allergy profiles are available. | | |
| Postconditions | Menu draft saved in 'Draft' status with allergy check results attached. Ready for approval submission (UC-089). All dishes declare complete ingredient and allergen lists (BR-069). | | |
| Normal Flow | 1. Service Caterers navigates to Menu Management and selects 'Create Menu'.  2. System displays menu creation form with serving date, meal slots, and dish selection.  3. Service Caterers selects dishes, enters serving date, and confirms nutritional targets (BR-062) and rotation rules (BR-068).  4. System automatically runs allergy conflict check against active student profiles (BR-063) and displays MSG-051 if conflicts found.  5. System saves the draft menu with creation details, logs the audit trail, and displays MSG-071. | | |
| Alternative Flows | AF1 - AI Recommendation: Service Caterers selects 'AI Assist'. Gemini AI generates dish recommendations based on inventory, nutritional targets, and cost ceiling (BR-064, BR-072, BR-156). System archives the AI query and recommendations (BR-157). Service Caterers reviews and applies suggestions.  AF2 - Allergy Conflict Found: System displays MSG-051. Service Caterers must resolve conflicts before submitting for approval. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-072, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-63 Create Menu Use Cases Specification

#### Create Special Meal Plan

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-023 - Create Special Meal Plan | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects 'Create Special Meal Plan' or opens a student dietary request. | | |
| Description | Enables the Service Caterers to define, validate, and save custom special meal plans for students with verified dietary restrictions or severe allergies. | | |
| Preconditions | Service Caterers is authenticated and authorized. Target student has a verified allergy or special meal request (UC-036 or UC-080). | | |
| Postconditions | Special meal plan is saved in the database, linked to the student profile, and made available for printing or exporting by the Service Caterers. | | |
| Normal Flow | 1. Service Caterers navigates to Special Meal Plan Management.  2. System lists students with active verified dietary restrictions or severe allergies (BR-065).  3. Service Caterers selects a student and clicks 'Create Special Meal Plan'.  4. System displays special meal plan form, pre-populating the student's verified allergens and dietary category (Halal, Vegetarian, Gluten-Free, Nut-Free, etc. per BR-066).  5. Service Caterers designs the meal plan, specifying dedicated recipes and cooking instructions that ensure separate preparation and utensils (BR-071).  6. Service Caterers enters the raw ingredients and potential trace allergens for all dishes (BR-069) and validates that alternative dishes are configured for the top 5 common allergens if applicable (BR-073).  7. System runs validation to confirm zero allergen overlap with the student's allergy profile (BR-063).  8. System saves the special meal plan, logs the action in the audit trail, and displays MSG-072. | | |
| Alternative Flows | AF1 - AI Recipe Assistance: Service Caterers requests AI assistance to generate an allergy-safe recipe. Gemini AI Service reviews the student's allergens, recommends safe ingredients and separate preparation steps (BR-156). System archives the AI query and recommendations (BR-157). Service Caterers reviews and applies suggestions. | | |
| Exceptions | MSG-004, MSG-005. If allergen validation fails (overlap detected), system blocks saving and displays MSG-051. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-066, BR-063, BR-065, BR-069, BR-071, BR-073, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-64 Create Special Meal Plan Use Cases Specification

#### Manage Menus

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-048 - Manage Menus | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Primary actor requests to manage menus. | | |
| Description | Enables the Service Caterers to browse, search, and manage draft, submitted, and published menus, including submitting them for approval or deleting drafts. | | |
| Preconditions | Service Caterers is authenticated and authorized. | | |
| Postconditions | Menu list is displayed. Draft and rejected menus can be edited or deleted. Approved/Published menus are locked (BR-067). | | |
| Normal Flow | 1. Service Caterers navigates to Menu Management.  2. System queries the database and displays the list of weekly menus showing: Scheduled Week, Status (Draft, Pending Approval, Approved, Published, Rejected), and Last Updated Date.  3. Service Caterers can search menus by date range or filter by status.  4. Service Caterers selects a menu to perform management actions:  - If status is 'Draft' or 'Rejected': Service Caterers can select 'Edit' (redirecting to UC-098) or 'Delete'.  - If status is 'Draft': Service Caterers can select 'Submit' (redirecting to UC-089).  5. System processes the selected action, updates the display, and logs the operation in the audit trail. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If trying to delete or edit an approved/published menu, system blocks the action and displays a validation warning (BR-067). | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-067 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-65 Manage Menus Use Cases Specification

#### Request AI Menu Recommendations

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-075 - Request AI Menu Recommendations | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects 'Request AI Menu Recommendations' during menu planning. | | |
| Description | Enables the Service Caterers to obtain AI-generated menu suggestions from Gemini AI Service based on current inventory, nutritional targets, student health profiles, and budget constraints. | | |
| Preconditions | Service Caterers is authenticated and authorized. Gemini AI Service is operational. Current inventory and student allergy profiles are available. | | |
| Postconditions | AI recommendations displayed with nutritional breakdowns and allergen flags. AI query and recommendations archived for compliance (BR-157). | | |
| Normal Flow | 1. Service Caterers selects 'Request AI Recommendations' in Menu Management. 2. System sends inventory levels, nutritional targets (BR-062), student allergy profiles, and seasonal cost data to Gemini AI Service. 3. Gemini AI analyzes data and returns dish recommendations prioritizing in-stock ingredients and cost ceiling (BR-064, BR-072). 4. System displays recommendations with nutritional breakdown, ingredient lists, and allergen indicators (BR-156). 5. Service Caterers applies selected recommendations to the menu. AI exchange archived (BR-157). | | |
| Alternative Flows | AF1 - AI Service Unavailable: Gemini AI Service unreachable. System displays service unavailability message (MSG-073) and allows manual menu creation.  AF2 - Expiring Documents Alert: One or more documents expire within 180 days. System highlights those documents with an 'Expiring Soon' badge; if fewer than 30 days remain and the document is still unrenewed, the badge escalates to 'Urgent' (BR-047). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-064, BR-072, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-66 Request AI Menu Recommendations Use Cases Specification

#### Submit Menu For Approval

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-087 - Submit Menu For Approval | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects a completed menu draft and clicks 'Submit for Approval'. | | |
| Description | Enables the Service Caterers to formally submit a validated menu draft for School Admin review and approval. | | |
| Preconditions | Service Caterers is authenticated and authorized. Menu is in 'Draft' status. All required fields are complete. Submitted at least 3 days before the scheduled week (BR-061). | | |
| Postconditions | Menu status changed to 'Pending Approval'. School Admin notified. Allergy conflicts flagged (MSG-051) if present. | | |
| Normal Flow | 1. Service Caterers navigates to Menu Management and selects a draft menu.  2. System displays menu summary and pre-submission validation checklist.  3. Service Caterers reviews meal options, nutritional data, and declares all ingredients (BR-069).  4. System runs final allergy conflict check (BR-063), displaying MSG-051 warnings if found.  5. System changes menu status to 'Pending Approval', notifies School Admin, displays MSG-039. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005.  If critical allergy conflict is detected, system blocks submission, displays MSG-051 warning message, and prevents submission until conflict is resolved. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-061, BR-063, BR-069 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-67 Submit Menu For Approval Use Cases Specification

#### Update Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-096 - Update Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Gemini AI Service |
| Trigger | Service Caterers selects a draft or rejected menu and clicks 'Edit Menu'. | | |
| Description | Enables the Service Caterers to modify details, dish selections, and serving dates of an existing menu draft, running automatic allergy and compliance checks. | | |
| Preconditions | Service Caterers is authenticated and authorized. The menu exists and is in 'Draft' or 'Rejected' status (BR-067). | | |
| Postconditions | The updated menu is saved as a draft. Allergy check results and nutrition evaluations are updated. | | |
| Normal Flow | 1. Service Caterers selects a draft or rejected menu and clicks 'Edit Menu'.  2. System displays the menu editing form pre-filled with current dish selections, nutritional breakdown, and active rejection comments (BR-074 if status is Rejected).  3. Service Caterers modifies serving dates, dish choices, or nutritional settings (BR-061, BR-062).  4. System runs automatic dish rotation check (BR-068: main dish max twice a week) and ingredient declaration validation (BR-069).  5. System runs allergy conflict check against student allergy profiles (BR-063).  6. System saves changes to database, updates audit logs, and displays MSG-074. | | |
| Alternative Flows | AF1 - AI Menu Recommendation: Service Caterers selects 'AI Assist' to replace a conflicting dish. Gemini AI Service analyzes current inventory, allergen profiles, and cost ceiling (BR-064, BR-072, BR-156) and recommends safe alternatives. System archives the AI query and recommendations (BR-157). | | |
| Exceptions | MSG-004, MSG-005. If trying to update an approved/locked menu (BR-067), action is blocked. If dish rotation limit is exceeded (BR-068), system blocks action and displays MSG-075. If allergy conflict is found, system displays MSG-051. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-061, BR-062, BR-063, BR-064, BR-067, BR-068, BR-069, BR-072, BR-074, BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-68 Update Menu Use Cases Specification

#### View List Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-128 - View List Menu | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Admin selects 'Menu List' from the dashboard. | | |
| Description | Enables the School Admin to query, view, filter, and export the list of weekly menus and their current statuses. | | |
| Preconditions | The School Admin is authenticated and authorized to access this feature. | | |
| Postconditions | List of menus is displayed. Exporting actions are enabled. | | |
| Normal Flow | 1. School Admin navigates to the Menu List section.  2. System queries the database for all weekly menus.  3. System displays the list showing: Scheduled Week range, status (Draft, Pending Approval, Approved, Published, Rejected), Service Caterers name, and nutritional summary.  4. School Admin can filter list by status, search by week range, or export the list to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-69 View List Menu Use Cases Specification

#### View Menu Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-134 - View Menu Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Admin selects a menu from the Menu List (UC-130) or the Menu Approval Dashboard (UC-006). | | |
| Description | Enables the School Admin to view detailed dish breakdowns, full raw ingredient declarations, potential trace allergens, nutritional targets, and approval histories for a specific menu. | | |
| Preconditions | School Admin is authenticated and authorized to access this feature. The target menu exists. | | |
| Postconditions | Detailed menu data, including raw ingredients, nutritional summaries, and approval logs, is displayed. | | |
| Normal Flow | 1. School Admin selects a menu and clicks 'View Details'.  2. System queries the database and retrieves the menu details: serving date, meal slots, dish lists, and nutritional breakdown (BR-062).  3. System displays the menu, showing each dish with its full ingredient list and trace allergens (BR-069).  4. System displays the history of approvals/rejections with timestamps, electronic signature metadata (BR-066), and comments.  5. School Admin reviews the information. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-062, BR-066, BR-069 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-70 View Menu Detail Use Cases Specification

#### View Menu Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-135 - View Menu Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent | Secondary Actors | None |
| Trigger | Parent navigates to 'School Menu' from the portal home screen. | | |
| Description | Enables Parents to view approved, published menus, including nutritional breakdowns, ingredient lists, and allergen highlights matched to their child's allergy profile. | | |
| Preconditions | Parent is authenticated and authorized. The menu is in 'Published' status (BR-070). At least one student profile is linked to the Parent (BR-031). | | |
| Postconditions | Published menu details, child-specific allergy warnings, and nutrition breakdowns are successfully retrieved and displayed on the user interface. | | |
| Normal Flow | 1. Parent navigates to the School Menu section.  2. System checks parent credentials and retrieves linked student profile(s) (BR-031).  3. System displays the published menus for the current and upcoming week (BR-070) conforming to the student's grade/age-specific nutritional targets (BR-062).  4. Parent browses dishes, viewing nutritional content, raw ingredients, and allergen declarations (BR-069).  5. System cross-references the menu dishes with the active child's verified allergy profile. If any dish contains active allergens, the system highlights the dish in red, highlights the specific allergen, and displays a warning alert (referencing MSG-051).  6. Parent can click 'Download PDF' to export a printable copy of the menu. | | |
| Alternative Flows | AF1 - Switch Child Profile (For Parents with multiple children):  1. Parent clicks the student dropdown selector.  2. System lists all linked children (max 5 per BR-201).  3. Parent selects a different child.  4. System refreshes the menu view, age-specific nutritional targets (BR-062), and child-specific allergy warnings.  AF2 - Access Special Meal Plan:  1. Parent selects a child who has an active special dietary plan (BR-065, BR-065).  2. Parent clicks 'View Special Meal' button.  3. System retrieves and displays the child's customized allergy-safe menu details (redirects to UC-144). | | |
| Exceptions | EX1 - No Menu Published: If no menu is published for the selected week, the system blocks access and displays MSG-090.  EX2 - Missing Student Link: If the Parent account has no linked student profiles (BR-031), the system displays warning and prompts the parent to enter a student admission code. | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-031, BR-201, BR-062, BR-069, BR-070, BR-075 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-71 View Menu Information Use Cases Specification

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

### 3.2.12. AI & Supporting Services

#### 3.2.12.1. Chat with AI

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-012 - Chat with AI | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Gemini AI Service |
| Trigger | User navigates to the AI Assistant section and types a question or request in the chat interface. | | |
| Description | Enables the User to interact with the Gemini AI Service through a conversational chat interface for food safety guidance, menu planning assistance, nutritional queries, and system support. | | |
| Preconditions | User is authenticated and authorized. Gemini AI Service is operational and configured (BR-157). | | |
| Postconditions | AI-generated response displayed in chat. Full conversation exchange archived for compliance audits (BR-157). | | |
| Normal Flow | 1. User navigates to Chat with AI section. 2. System displays chat interface with previous conversation history. 3. User types a question or request and submits. 4. System sends query to Gemini AI Service with relevant context (user role, active policies, nutritional data per BR-156). 5. Gemini AI returns response. System displays it in chat and archives the complete exchange (BR-157). | | |
| Alternative Flows | AF1 - AI Service Unavailable: Gemini AI Service unreachable. System displays service unavailability message and advises user to retry later. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-143 Chat with AI Use Cases Specification

#### 3.2.12.2. Manage External Integrations

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-040 - Manage External Integrations | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to manage external integrations. | | |
| Description | Enables the System Admin to manage details, modify parameters, or update configuration fields of External Integrations. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The manage external integrations transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. System Admin navigates to the 'External Integrations' panel.  2. System displays a list of connected third-party services: GPS Service, Gmail SMTP, Firebase Cloud Messaging, and Gemini AI.  3. System Admin selects a service and clicks 'Edit Configuration'.  4. System Admin updates the API endpoint, OAuth2 client credentials, or API secret key.  5. System Admin clicks 'Test Connection'.  6. System performs test handshake using OAuth2 or the new key (BR-163) and returns 'Success'.  7. System Admin clicks 'Save'.  8. System encrypts credentials, saves settings (MSG-043), logs action to the immutable audit trail (BR-162), and displays success message. | | |
| Alternative Flows | AF1 - Connection Test Failed: At step 6, connection test fails. System displays error message detailing failure (e.g. Invalid API key) and prevents saving. | | |
| Exceptions | EX1 - Missing Mandatory Fields: System Admin attempts to save with empty key or endpoint. System displays validation error (MSG-004) and blocks action. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-144 Manage External Integrations Use Cases Specification

#### 3.2.12.3. Monitor System Activities

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-057 - Monitor System Activities | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to monitor system activities. | | |
| Description | Enables the System Admin to safely query, view, or export the details of System Activities records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The monitor system activities data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the System Activities section.  2. System queries server performance APIs and retrieves active statistics (CPU load, memory load, disk IO, active user sessions, database query pool load).  3. System displays charts and indicators showing current metrics.  4. System compiles and displays weekly active user metrics and system health summary reports (BR-165). | | |
| Alternative Flows | None. | | |
| Exceptions | EX1 - Dashboard Load Failure: Server monitoring API is unreachable. System displays error message indicating service monitoring agent is offline. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-179 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-145 Monitor System Activities Use Cases Specification

#### 3.2.12.4. Receive Notification

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-058 - Receive Notification | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Firebase Cloud Message |
| Trigger | System detects a triggering event (e.g., menu published, incident reported, allergy alert) and initiates push notification delivery via Firebase Cloud Messaging. | | |
| Description | Enables the User to receive real-time push notifications for relevant system events including menu updates, food safety alerts, allergy warnings, and incident reports. | | |
| Preconditions | User is authenticated and has push notifications enabled. Firebase Cloud Messaging is operational. A triggering event has occurred in the system. | | |
| Postconditions | Notification delivered to user's device. Delivery status and timestamp logged. Retry initiated if delivery fails. | | |
| Normal Flow | 1. System detects a triggering event (menu published, incident reported, etc.). 2. System checks target user's notification preferences and quiet hours (default: 10PM-6AM per BR-158). 3. System constructs notification payload and dispatches via Firebase Cloud Messaging. 4. Firebase Cloud Messaging delivers push notification to user's registered device. 5. System logs delivery status, timestamp, and event type for audit purposes. | | |
| Alternative Flows | AF1 - Delivery Failure: FCM delivery fails. System automatically retries (MSG-068). After exhausting retries, system logs the failure and attempts email fallback delivery (BR-159). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and push notifications enabled on their device. | | |

Table 3-146 Receive Notification Use Cases Specification

#### 3.2.12.5. Scan Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-079 - Scan Face Data | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Face Recognition Service |
| Trigger | Primary actor requests to scan face data. | | |
| Description | Enables the Service Caterers to successfully perform the Scan Face Data operations in the system. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | The scan face data transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Service Caterers staff accesses the face scanning module.  2. System activates the camera interface and prompts the user (e.g. delivery driver) to look at the camera.  3. System captures video frames and detects face presence.  4. System extracts facial landmarks and generates the biometric face template.  5. System compares the template with the registered profile for validation (BR-050).  6. System displays 'Verification Successful' and logs the match with timestamp. | | |
| Alternative Flows | AF1 - Register Driver Biometrics: At step 1, supplier driver is onboarding. Service Caterers captures face scan, associates biometric template with driver profile, and saves (MSG-043) for future delivery validation. | | |
| Exceptions | EX1 - Face Recognition Failed: Biometric identity could not be verified due to lighting or mismatch. System displays error MSG-014 (Face recognition failed) and prompts to retry or enter passcode verification. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-050, BR-051, BR-060 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-147 Scan Face Data Use Cases Specification

#### 3.2.12.6. Turn off/Turn on Notification

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-088 - Turn off/Turn on Notification | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Firebase Cloud Message |
| Trigger | User navigates to Notification Settings and adjusts notification preferences. | | |
| Description | Enables the User to enable or disable specific push notification categories and configure quiet hours to control the timing of system notifications. | | |
| Preconditions | User is authenticated and has an active session. | | |
| Postconditions | Notification preferences saved. All subsequent notifications respect the updated configuration including quiet hours (BR-158). | | |
| Normal Flow | 1. User navigates to Settings and selects 'Notification Preferences'. 2. System displays current notification configuration: enabled categories and quiet hours (default: 10:00 PM - 6:00 AM per BR-158). 3. User toggles notification categories on/off or adjusts quiet hours settings. 4. System saves the updated configuration to the user profile. 5. System confirms the update. All future notifications will respect the new configuration. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-148 Turn off/Turn on Notification Use Cases Specification

#### 3.2.12.7. View Audit Logs

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-106 - View Audit Logs | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view audit logs. | | |
| Description | Enables the System Admin to safely query, view, or export the details of Audit Logs records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view audit logs data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the Audit Logs panel.  2. System queries the immutable audit log server (BR-162) and retrieves recent logs sorted by timestamp.  3. System displays log entries showing: Timestamp, User ID, IP Address, Action Performed, Resource Accessed, and Status.  4. System Admin applies filters: Date range, User ID, Action category, or IP address.  5. System retrieves filtered records and displays them in a read-only list. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-175 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-149 View Audit Logs Use Cases Specification

#### 3.2.12.8. View Error Logs

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-111 - View Error Logs | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view error logs. | | |
| Description | Enables the System Admin to safely query, view, or export the details of Error Logs records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view error logs data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the Error Logs section.  2. System retrieves error logs from the logging database sorted by severity and timestamp.  3. System displays a list showing: Timestamp, Error Message, Severity, Module/Class, and Path.  4. System Admin selects a specific error.  5. System displays the detailed stack trace, environment variables, request metadata (headers, query parameters), and affected user session info (BR-164). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-176 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-150 View Error Logs Use Cases Specification

#### 3.2.12.9. View History Chat

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-119 - View History Chat | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Primary actor requests to view history chat. | | |
| Description | Enables the User to safely query, view, or export the details of History Chat records. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | The view history chat data is successfully retrieved and displayed to the user. | | |
| Normal Flow | 1. User navigates to the 'AI Chat History' section.  2. System queries the archived AI interaction log (BR-157) filtering by the user's ID.  3. System displays a list of past chat sessions sorted by date.  4. User selects a chat session.  5. System retrieves and renders the complete dialogue history of questions and AI responses. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-156, BR-157 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-151 View History Chat Use Cases Specification

#### 3.2.12.10. View Notification History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-136 - View Notification History | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | None |
| Trigger | Primary actor requests to view notification history. | | |
| Description | Enables the User to safely query, view, or export the details of Notification History records. | | |
| Preconditions | The user is authenticated and authorized to access this feature. | | |
| Postconditions | The view notification history data is successfully retrieved and displayed to the user. | | |
| Normal Flow | 1. User navigates to the Notification History section.  2. System queries the notification database and retrieves received notifications for the user's ID sorted by dispatch timestamp.  3. System displays the list showing: Title, Body, Date, Type (Alert/Notification), and Read/Unread Status.  4. User can search by keyword or filter by Read/Unread status. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-152 View Notification History Use Cases Specification

#### 3.2.12.11. View System Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-149 - View System Status | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view system status. | | |
| Description | Enables the System Admin to safely query, view, or export the details of System Status records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view system status data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. Actor: System Admin navigates to access the System Status panel. 2. System: Authenticate access and immediately initiate online health checks for core services. 3. System: Returns results and displays them on the connection health dashboard. 4. System: Load and display the configuration and operating status of the weekly backup scheduler (BR-160). 5. Actor: Observe and monitor the parameters on the dashboard. | | |
| Alternative Flows | 4a. Export System Status Records:   1. Actor: In the System Status console, the System Admin clicks the "Export" function. 2. System: Summarizes the current status parameters (including status, latencies, and backup scheduler status). 3. System: Exports the data to a file (e.g.,.CSV or.PDF) and initiates the download process to the System Admin's device. 4. The event flow returns to step 5 of the Normal Flow. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-177 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-153 View System Status Use Cases Specification

#### 3.2.12.12. View Usage Statistics

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-150 - View Usage Statistics | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to view usage statistics. | | |
| Description | Enables the System Admin to safely query, view, or export the details of Usage Statistics records. | | |
| Preconditions | The system admin is authenticated and authorized to access this feature. | | |
| Postconditions | The view usage statistics data is successfully retrieved and displayed to the system admin. | | |
| Normal Flow | 1. System Admin navigates to the Usage Statistics panel.  2. System queries system log aggregation database.  3. System displays dashboards showing active daily/weekly users, top use cases called, storage capacity utilization, and API request count logs (BR-165).  4. System Admin filters metrics by date range or specific user role categories.  5. System refreshes the graphs and tables. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-158, BR-159, BR-160, BR-161, BR-162, BR-163, BR-164, BR-178 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-154 View Usage Statistics Use Cases Specification

#### 3.2.12.13. Configure Security Policies

*(Bổ sung 2026-07-01 — UC mới, phát hiện qua audit A8 của `00_TODO_GiaDinh_Master.md`: màn hình `Web/SystemAdmin/15_SecurityPolicies.md` mô tả cấu hình BR-002/BR-004/BR-006 nhưng không có UC nguồn nào trong Report 3/traceability_matrix. BR-002/004/006 trước đây chỉ được nêu là hằng số nghiệp vụ, chưa có UC cho phép System Admin xem/sửa. UC dưới đây được đánh số UC-184, tiếp theo UC-183 là mã UC lớn nhất đã cấp — xem traceability_matrix.md §1.13.)*

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-184 - Configure Security Policies | | |
| Created By | Claude (audit A8, xác nhận bởi team) | Date Created | 01/07/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | System Admin requests to view or update platform-wide security policy parameters. | | |
| Description | Enables the System Admin to view and adjust security-related configuration parameters governing account lockout, session timeout, and password strength requirements across the platform. | | |
| Preconditions | The System Admin is authenticated and authorized to access System Configuration. | | |
| Postconditions | Security policy parameters are updated, saved, and recorded in the audit trail. New values apply platform-wide on the next relevant security check (login attempt, session check, or password change). | | |
| Normal Flow | 1. System Admin navigates to the Security Policies panel (System Configuration group). 2. System displays current values: maximum failed login attempts before lockout (BR-002), lockout duration (BR-002), session timeout duration (BR-004), and password strength rules (BR-006). 3. System Admin edits one or more parameters. 4. System validates the new values are within allowed ranges. 5. System saves the updated parameters, records the change in the audit trail (BR-170), and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If a new value is outside the allowed range, system displays a validation error and does not save. | | |
| Priority | Low | | |
| Frequency of Use | Rare | | |
| Business Rules | BR-002, BR-004, BR-006, BR-170 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. Values shown are the platform's current hard-coded defaults (5 lần sai/15 phút lockout, 30 phút session timeout) until this UC is implemented; team cần xác nhận phạm vi cho phép chỉnh sửa (toàn bộ hay giới hạn khoảng cho phép) trước khi code hoá. | | |

Table 3-155 Configure Security Policies Use Cases Specification

### 3.2.13. Reporting & Analytics

*(Bổ sung 2026-07-01 — UC-171/172 trước đây chỉ tồn tại dạng stub trong traceability_matrix.md §1.13 [tên UC + actor + BR/MSG], chưa có Functional Specification chi tiết. Được viết đầy đủ tại đây qua audit A8 để làm nguồn chính thức cho các màn hình báo cáo dùng chung: Food Sample Retention Reports, Partnership Reports, System Usage Metrics [School Admin scope], Traceability Reports. UC-173 - Schedule Automated Report vẫn giữ nguyên dạng stub vì không có màn hình A8 nào cần đến, để team bổ sung sau khi cần.)*

#### 3.2.13.1. View Analytics Dashboard

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-171 - View Analytics Dashboard | | |
| Created By | Claude (audit A8, xác nhận bởi team) | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | Medical Staff, System Admin |
| Trigger | Primary actor requests to view an analytics dashboard summarizing operational metrics. | | |
| Description | Enables the primary actor to view an analytics dashboard summarizing key operational metrics scoped to their permission level — food sample retention, ingredient receiving, partnership performance, ingredient traceability, and system usage — with data refreshed near-real-time. | | |
| Preconditions | The actor is authenticated and authorized to access reporting features for their role/school. | | |
| Postconditions | The requested dashboard metrics are retrieved and displayed to the actor. | | |
| Normal Flow | 1. Actor navigates to the Analytics Dashboard / "Báo cáo & Thống kê" section. 2. System retrieves aggregated metrics scoped to the actor's role and school (BR-176). 3. System displays dashboard widgets/charts for the selected report category (Food Sample Retention, Receiving, Partnership, Traceability, or System Usage), refreshed within 5 minutes of underlying data changes (BR-180). 4. Actor applies date range or category filters. 5. System re-queries and displays the filtered data. | | |
| Alternative Flows | AF1 - Export: Actor selects "Export PDF/Excel" from within the dashboard → system proceeds to UC-172 Export Report using the current filter context. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-175, BR-176, BR-180, BR-181, BR-182 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-156 View Analytics Dashboard Use Cases Specification

#### 3.2.13.2. Export Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-172 - Export Report | | |
| Created By | Claude (audit A8, xác nhận bởi team) | Date Created | 01/07/2026 |
| Primary Actor | School Administration | Secondary Actors | System Admin |
| Trigger | Actor requests to export a report or dashboard view to a file. | | |
| Description | Enables the actor to export the currently viewed analytics report (Food Sample Retention, Receiving, Partnership, Traceability, System Usage, or Audit Logs) to PDF or Excel format for compliance retention and offline review. | | |
| Preconditions | The actor is authenticated and authorized to access the underlying report data. | | |
| Postconditions | An export file is generated and made available for download. The export event is retained for audit purposes. | | |
| Normal Flow | 1. Actor selects "Export PDF/Excel" from an Analytics Dashboard view (UC-171) or a standalone report screen. 2. System validates the actor's data scope (BR-175, BR-176). 3. System generates the export file honoring the currently applied filters (date range, category). 4. System returns a downloadable file in PDF or Excel format (BR-178). 5. The export event is retained for a minimum of 12 months for audit purposes (BR-177). | | |
| Alternative Flows | AF1 - Schedule instead of immediate download: Actor selects "Schedule" instead of "Export Now" → system proceeds to UC-173 Schedule Automated Report. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-175, BR-176, BR-177, BR-178, BR-181, BR-182 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

Table 3-157 Export Report Use Cases Specification

## Functional Requirements

### System Functional Overview

#### Screens Flow

##### Screens Flow for Guest and Common Screen

![](data:image/png;base64...)

##### Screens Flow for System Admin

![](data:image/png;base64...)

##### Screens Flow for School Admin

![](data:image/png;base64...)

##### Screens Flow for Medical Staff

![](data:image/png;base64...)

##### Screens Flow for Service Caterers

![](data:image/png;base64...)

##### Screens Flow for Parent

![](data:image/png;base64...)

##### Screens Flow for Raw Food Supplier

![](data:image/png;base64...)

##### Screens Flow for Teacher

![](data:image/png;base64...)

#### Screen Authorization

| Screen | System Admin | School Admin | Teacher | Raw Food Supplier | Medical Staff | Service Caterers | Parent |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Login | x | x | x | x | x | x | x |
| Forgot Password | x | x | x | x | x | x | x |
| Reset Password | x | x | x | x | x | x | x |
| Verify OTP | x | x | x | x | x | x | x |
| Profile Management | x | x |  | x | x | x |  |
| Change Password | x | x |  |  | x | x |  |
| Notification Center/List |  | x | x |  |  | x | x |
| Notification Detail |  |  | x |  |  |  | x |
| Notification Settings | x | x | x | x | x | x | x |
| Audit Logs | x | x |  |  |  |  |  |
| System Administrator Dashboard | x |  |  |  |  |  |  |
| Manage User Screen/Dialog | x |  |  |  |  |  |  |
| User Account List | x |  |  |  |  |  |  |
| Manage Role Screen | x |  |  |  |  |  |  |
| Role List | x |  |  |  |  |  |  |
| Manage Permission List Screen | x |  |  |  |  |  |  |
| Active/Deactivate User Dialog | x |  |  |  |  |  |  |
| Active/Deactivate School Dialog | x |  |  |  |  |  |  |
| School List | x |  |  | x |  |  |  |
| School Detail | x |  |  | x |  |  |  |
| Update School Screen/Dialog | x |  |  |  |  |  |  |
| Backup Management | x |  |  |  |  |  |  |
| Security Policies | x |  |  |  |  |  |  |
| AI Settings | x |  |  |  |  |  |  |
| System Activity Monitoring | x |  |  |  |  |  |  |
| System Status Screen | x |  |  |  |  |  |  |
| Error Logs Screen | x |  |  |  |  |  |  |
| Usage Statistics Screen | x |  |  |  |  |  |  |
| School Administration Dashboard |  | x |  |  |  |  |  |
| Catering Partnership Requests |  | x |  |  |  |  |  |
| Supplier Partnership Requests |  | x |  |  |  |  |  |
| Service Caterers Detail |  | x |  |  |  |  |  |
| Supplier Detail |  | x |  |  |  |  |  |
| Menu Management |  | x |  |  |  |  |  |
| Menu Detail |  | x |  |  |  |  | x |
| Incident Dashboard |  | x |  |  |  |  |  |
| Incident List |  | x |  |  | x |  |  |
| Incident Detail |  | x | x |  | x |  |  |
| Corrective Action Review |  | x |  |  |  |  |  |
| Resolution Report |  | x |  |  |  |  |  |
| Food Sample Retention Reports |  | x |  |  |  |  |  |
| Receiving Reports |  | x |  |  |  |  |  |
| Inspection Reports |  | x |  |  |  |  |  |
| Partnership Reports |  | x |  |  |  |  |  |
| Partnership Approval History |  | x |  |  |  |  |  |
| Compliance Dashboard |  | x |  |  | x |  |  |
| Classroom Management |  | x |  |  |  |  |  |
| Classroom Detail |  | x |  |  |  |  |  |
| Create Classroom |  | x |  |  |  |  |  |
| Student Profile Management |  | x |  |  |  |  |  |
| Student Profile Detail |  | x |  |  |  |  |  |
| Bulk Import Student Profiles |  | x |  |  |  |  |  |
| Parent Account Detail |  | x |  |  |  |  |  |
| Link Parent Account |  | x |  |  |  |  |  |
| Linked Students List |  | x |  |  |  |  |  |
| School Announcement Management |  | x |  |  |  |  |  |
| Create Announcement |  | x |  |  |  |  |  |
| Update Announcement |  | x |  |  |  |  |  |
| Announcement Detail |  | x |  |  |  |  |  |
| Staff Management |  | x |  |  |  |  |  |
| Upload Staff List |  | x |  |  |  |  |  |
| Invitation Status |  | x |  |  |  |  |  |
| System Usage Metrics |  | x |  |  |  |  |  |
| Traceability Reports |  | x |  |  |  |  |  |
| School Configuration |  | x |  |  |  |  |  |
| Verification Documents |  | x |  |  |  |  |  |
| Allergy Review |  | x |  |  |  |  |  |
| Teacher Home |  |  | x |  |  |  |  |
| Student List |  |  | x |  |  |  |  |
| Student Detail |  |  | x |  |  |  |  |
| Allergy Information |  |  | x |  | x |  | x |
| Health Information |  |  | x |  |  |  |  |
| Special Meal Requirements |  |  | x |  |  |  |  |
| Meal Records |  |  | x |  |  |  |  |
| Distribution List |  |  | x |  |  |  |  |
| Distribution Detail |  |  | x |  |  |  |  |
| Distribution Confirmation |  |  | x |  |  |  |  |
| Upload Evidence |  |  | x |  |  |  |  |
| Incident Form |  |  | x |  |  |  |  |
| Incident History |  |  | x |  |  |  |  |
| Supplier Dashboard |  |  |  | x |  |  |  |
| Select School Workspace |  |  |  | x |  |  |  |
| School Partnerships |  |  |  | x |  |  |  |
| Product Origin Documents |  |  |  | x |  |  |  |
| Food Safety Certificates |  |  |  | x |  |  |  |
| Upload Supplier Certificates |  |  |  | x |  |  |  |
| Partnership Application |  |  |  | x |  |  |  |
| Application Status |  |  |  | x |  |  |  |
| Purchase Requests |  |  |  | x |  |  |  |
| Purchase Request List |  |  |  | x |  |  |  |
| Purchase Request Detail |  |  |  | x |  |  |  |
| Ingredient Batches |  |  |  | x |  |  |  |
| Batch List |  |  |  | x |  |  |  |
| Batch Detail |  |  |  | x |  |  |  |
| Submit Batch Information |  |  |  | x |  |  |  |
| Upload Batch Certificates |  |  |  | x |  |  |  |
| View Batch History |  |  |  | x |  |  |  |
| Deliveries |  |  |  | x |  |  |  |
| Delivery List |  |  |  | x |  |  |  |
| Delivery Detail |  |  |  | x |  |  |  |
| Submit Delivery Information |  |  |  | x |  |  |  |
| Confirm Delivery Schedule |  |  |  | x |  |  |  |
| Update Delivery Status |  |  |  | x |  |  |  |
| View Delivery Batch Records |  |  |  | x |  |  |  |
| Update Documents |  |  |  | x |  |  |  |
| Compliance Documents |  |  |  | x |  |  |  |
| Medical Staff Dashboard |  |  |  |  | x |  |  |
| Review Health Information Screen |  |  |  |  | x |  |  |
| Student Health Record List |  |  |  |  | x |  |  |
| Student Health Record Detail |  |  |  |  | x |  |  |
| Update Health Record Screen |  |  |  |  | x |  |  |
| Health History Screen |  |  |  |  | x |  |  |
| Review Special Meal Request Screen |  |  |  |  | x |  |  |
| Special Meal Request List |  |  |  |  | x |  |  |
| Approve Special Meal Dialog |  |  |  |  | x |  |  |
| Reject Special Meal Dialog |  |  |  |  | x |  |  |
| Food Inspection Records |  |  |  |  | x |  |  |
| Conduct Food Safety Inspection Screen |  |  |  |  | x |  |  |
| Food Sample Records |  |  |  |  | x |  |  |
| Retrieve Food Sample Records Dialog |  |  |  |  | x |  |  |
| View Food Safety Reports Screen |  |  |  |  | x |  |  |
| View School Policy Screen |  |  |  |  | x |  |  |
| Investigation Workspace |  |  |  |  | x |  |  |
| Assign Incident Severity Dialog |  |  |  |  | x |  |  |
| Record Corrective Actions Report Screen |  |  |  |  | x |  |  |
| Generate Incident Report Dialog |  |  |  |  | x |  |  |
| Service Caterers Dashboard |  |  |  |  |  | x |  |
| Menu Management Dashboard |  |  |  |  |  | x |  |
| Create Menu Workspace |  |  |  |  |  | x |  |
| View Menu Recommendations Panel (AI Integration) |  |  |  |  |  | x |  |
| Submit Menu For Approval Trigger |  |  |  |  |  | x |  |
| Update Menu Screen |  |  |  |  |  | x |  |
| Receiving Management Dashboard |  |  |  |  |  | x |  |
| Receive Ingredients Workspace (Start Receiving Session) |  |  |  |  |  | x |  |
| Verify Supplier Delivery Panel |  |  |  |  |  | x |  |
| Capture Receiving Evidence Screen |  |  |  |  |  | x |  |
| Store Ingredients Form (Storage Assignment) |  |  |  |  |  | x |  |
| Reject Ingredient Batch Form |  |  |  |  |  | x |  |
| Record Ingredient Receiving |  |  |  |  |  | x |  |
| Receiving History & Reports |  |  |  |  |  | x |  |
| Three-Step Inspection Dashboard |  |  |  |  |  | x |  |
| Inspect Ingredients Before Processing Screen (Step 1) |  |  |  |  |  | x |  |
| Upload Image Cooked Food Screen (Step 2) |  |  |  |  |  | x |  |
| Inspect Meals Before Serving Screen (Step 3) |  |  |  |  |  | x |  |
| Record Preparation Information Form (Cooking Record) |  |  |  |  |  | x |  |
| View Preparation Records Ledger |  |  |  |  |  | x |  |
| Record Sample Information Form |  |  |  |  |  | x |  |
| Food Sample Detail |  |  |  |  |  | x |  |
| Food Sample Retention Dashboard |  |  |  |  |  | x |  |
| Meal Service Management Dashboard |  |  |  |  |  | x |  |
| View Meal Distribution List |  |  |  |  |  | x |  |
| View Allergy Alerts Panel (Special Meal Monitoring) |  |  |  |  |  | x |  |
| Manage Special Meals Configuration |  |  |  |  |  | x |  |
| Inventory Management Dashboard |  |  |  |  |  | x |  |
| Issue Ingredients Workspace |  |  |  |  |  | x |  |
| Monitor Expiration Dates Panel (Expiration Alerts) |  |  |  |  |  | x |  |
| Record Inventory Transactions Log |  |  |  |  |  | x |  |
| Corrective Actions Dashboard |  |  |  |  |  | x |  |
| Submit Corrective Action Results Form |  |  |  |  |  | x |  |
| View Inspection Records Log |  |  |  |  |  | x |  |
| View Incident Information Detail |  |  |  |  |  | x |  |
| Parent Dashboard |  |  |  |  |  |  | x |
| Weekly Menu |  |  |  |  |  |  | x |
| Nutrition Detail |  |  |  |  |  |  | x |
| Nutrition History |  |  |  |  |  |  | x |
| Submit Allergy Information |  |  |  |  |  |  | x |
| Update Allergy Information |  |  |  |  |  |  | x |
| Student Health Information |  |  |  |  |  |  | x |
| Submit Health Information |  |  |  |  |  |  | x |
| Update Health Information |  |  |  |  |  |  | x |
| Special Meal Arrangements |  |  |  |  |  |  | x |
| Special Meal Records |  |  |  |  |  |  | x |
| Meal History |  |  |  |  |  |  | x |
| Meal History Detail |  |  |  |  |  |  | x |
| School Meals Dashboard |  |  |  |  |  |  | x |
| Feedback Center |  |  |  |  |  |  | x |
| Submit Meal Feedback |  |  |  |  |  |  | x |
| Submit Food Quality Feedback |  |  |  |  |  |  | x |
| Report Food Safety Concern |  |  |  |  |  |  | x |

#### Non-screen Functions

| **#** | **Feature** | **System Function** | **Description** |
| --- | --- | --- | --- |
| 1 | User Management & Access Control | Session Auto-Timeout | System automatically terminates user sessions after 30 minutes of inactivity and redirects the user to the login screen. (BR-004) |
| 2 | User Management & Access Control | Account Lockout | System locks the user account for 15 minutes after 5 consecutive failed login attempts to prevent brute-force attacks. (BR-002) |
| 3 | User Management & Access Control | Password Reset Token Expiry | System automatically invalidates password reset tokens exactly 1 hour after generation. Expired tokens cannot be used. (BR-003) |
| 4 | User Management & Access Control | Concurrent Login Prevention | System blocks simultaneous logins from the same user account on multiple devices. Only the most recent session remains active. (BR-005) |
| 5 | User Management & Access Control | Session Cascade Termination | When a user account is deactivated, the system immediately terminates all active sessions associated with that account. (BR-007) |
| 6 | User Management & Access Control | Email Verification Dispatch | System automatically sends a verification email containing an activation link when a new user account is created. Account remains inactive until email is verified. (BR-010) |
| 7 | User Management & Access Control | Unverified Account Purge | System automatically deletes temporary or unverified accounts that remain inactive for more than 48 hours. (BR-011) |
| 8 | User Management & Access Control | Audit Log Recording | System automatically logs all user operational activities, status changes, and critical actions for auditing and traceability purposes. (BR-162) |
| 9 | School & Class Management | School Deactivation Cascade | When a school is deactivated, the system automatically suspends all associated classes, staff accounts, and student accounts. (BR-021) |
| 10 | School & Class Management | Homeroom Teacher Reassignment Audit | System automatically creates an audit log entry recording transfer details and date when a homeroom teacher is reassigned to a different classroom. (BR-026) |
| 11 | Student & Parent Management | Student Deactivation Cascade | When a student profile is deactivated, the system immediately removes the student from the active meal distribution list. (BR-041) |
| 12 | Student & Parent Management | Allergy Alert Distribution | After a student's allergy profile is verified and approved by Medical Staff, the system automatically generates and distributes allergy alerts to relevant stakeholders including Service Caterers, Teachers, and Medical Staff. (BR-123, WF 3.1.2.9 Step 9-10) |
| 13 | Student & Parent Management | Allergy Conflict Detection on Menu | System automatically checks whether approved or draft menu items contain allergens affecting registered students. If a conflict is detected, the system blocks menu approval and generates a warning notification. (BR-063, WF 3.1.2.4 Step 6-7) |
| 14 | Student & Parent Management | Health Alert Generation | System generates health alerts for relevant stakeholders when a student's verified health records indicate conditions requiring special attention during meal service. (WF 3.1.2.10 Step 9-10) |
| 15 | Supplier & Partner Management | Supplier Certificate Expiry Tracking | System monitors the expiration dates of supplier food safety certifications, sending an 'Expiring Soon' notification 180 days prior to expiry and escalating to an 'Urgent' notification at 30 days if still unrenewed. (BR-047) |
| 16 | Supplier & Partner Management | Expired Certificate Auto-Block | Suppliers with expired food safety certificates are automatically blocked from receiving new purchase requests until certificates are renewed. (BR-048) |
| 17 | Supplier & Partner Management | Supplier Performance Index Calculation | System calculates a monthly supplier performance index based on delivery timeline adherence and food inspection pass rates. Suppliers scoring below 70% for two consecutive months are flagged for suspension. |
| 18 | Supplier & Partner Management | Supplier Deactivation Cascade | When a supplier profile is deactivated, the system automatically cancels all pending purchase orders and delivery schedules associated with that supplier. *(No canonical BR currently defines this — flagged in BR_Citation_Audit_Report.md Bucket C)* |
| 19 | Supplier & Partner Management | Delivery Deviation Alert | System triggers an alert when a supplier's delivery time deviates more than 30 minutes from the agreed delivery schedule. (BR-135) |
| 20 | Supplier & Partner Management | Supplier Status Verification | During ingredient receiving, the system automatically verifies that the delivering supplier has an active and approved status before allowing the receiving session to proceed. (WF 3.1.2.2 Step 3) |
| 21 | Menu & Diet Management | Menu Publish Notification | Upon administrator approval, the system automatically publishes the menu to parent and teacher portals and sends push notifications to parents and class teachers. (BR-070, BR-075, WF 3.1.2.4 Step 13-14) |
| 22 | Menu & Diet Management | Alternative Meal Plan Generation | System automatically generates alternative meal suggestions for students with approved dietary restrictions when a new menu is created. (BR-065, WF 3.1.2.4 Step 8) |
| 23 | Menu & Diet Management | Dish Rotation Enforcement | System validates that the same main dish is not included in the menu more than twice in any calendar week and blocks submission if violated. (BR-068) |
| 24 | Ingredient & Inventory Management | Inventory Auto-Update on Receiving | System automatically updates inventory quantities and expiration tracking information when an ingredient batch passes inspection and is accepted. (WF 3.1.2.2 Step 12) |
| 25 | Ingredient & Inventory Management | Inventory Auto-Deduction on Issue | System automatically deducts issued ingredient quantities from inventory and updates stock balances when ingredients are issued to the kitchen. (WF 3.1.2.5 Step 8) |
| 26 | Ingredient & Inventory Management | Expiration Date Monitoring | System continuously monitors expiration dates of stored ingredients and generates expiration alerts to Service Caterers and School Administration when batches approach their expiration date. (WF 3.1.2.3 Step 6-8) |
| 27 | Ingredient & Inventory Management | Expired Ingredient Lock | Expired ingredients are automatically locked in the inventory system, preventing their selection for kitchen issue or meal preparation. (BR-205) |
| 28 | Ingredient & Inventory Management | Safety Stock Alert | System triggers automatic re-order email warnings to the catering manager when ingredient inventory reaches the configured safety stock threshold. (BR-077) |
| 29 | Ingredient & Inventory Management | Inventory Shortage Alert | System generates inventory shortage alerts when a kitchen ingredient request cannot be fulfilled due to insufficient stock levels. (WF 3.1.2.3 Step 15, WF 3.1.2.5 Step 12) |
| 30 | Ingredient & Inventory Management | FEFO Batch Recommendation | System automatically recommends ingredient batches following the First Expired First Out (FEFO) principle when issuing ingredients from inventory to the kitchen. (BR-076, WF 3.1.2.5 Step 5) |
| 31 | Ingredient & Inventory Management | GPS & Timestamp Capture on Receiving | System automatically records GPS coordinates, device information, and timestamp during ingredient receiving activities to support anti-fraud control and location verification. (WF 3.1.2.2 Step 7) |
| 32 | Ingredient & Inventory Management | Receiving Location Verification | System verifies that ingredient receiving activities are performed within the authorized receiving area by comparing GPS coordinates against the registered school location. (WF 3.1.2.2 Step 8, BR-079) |
| 33 | Food Safety Inspection & Monitoring | Inspection Failure Auto-Block | When any step of the three-step food inspection fails, the system automatically blocks meal release and generates a corrective action request to responsible personnel. (BR-097, WF 3.1.2.7 Step 13-14) |
| 34 | Food Safety Inspection & Monitoring | GPS & Timestamp Capture on Inspection | System automatically records GPS location, device information, and timestamp during each food inspection step to ensure traceability and prevent retrospective record creation. (WF 3.1.2.7 Step 3, 6, 9) |
| 35 | Food Safety Inspection & Monitoring | Inspection Record Generation | System automatically generates a complete three-step inspection record linked to ingredient batches and meal production records upon successful completion of all inspection steps. (WF 3.1.2.7 Step 11) |
| 36 | Food Safety Inspection & Monitoring | Kitchen Quarantine Trigger | System triggers immediate kitchen quarantine and blocks all meal distributions when critical safety violation scores fall below 60%. (BR-105) |
| 37 | Food Sample Retention | GPS & Timestamp Capture on Sample Registration | System automatically records GPS location, device information, and timestamp during food sample registration to verify the retention activity occurs at the school kitchen premises. (WF 3.1.2.8 Step 6-7, BR-109) |
| 38 | Food Sample Retention | Retention Period Monitoring | System continuously monitors the mandatory food sample retention duration (minimum 24 hours) and determines when the retention period has elapsed. (WF 3.1.2.8 Step 11-12, BR-111) |
| 39 | Food Sample Retention | Sample Disposal Lock on Incident | System immediately suspends food sample disposal if any student reports suspected food safety symptoms, preserving samples for potential investigation. (BR-120, BR-150) |
| 40 | Food Sample Retention | Temperature Deviation Alarm | System triggers an alarm if the retention refrigerator temperature deviates outside the 2°C–8°C range for more than 30 minutes. |
| 41 | Meal Serving & Distribution | Allergy Alert on Student Scan | When a student ID with a registered allergy is scanned during meal service, the system triggers a visual and audible alert on the serving screen to prevent incorrect meal distribution. (BR-123) |
| 42 | Meal Serving & Distribution | Duplicate Distribution Prevention | System prevents duplicate meal distribution by blocking a student ID that has already been scanned during the current meal service session. (BR-130) |
| 43 | Meal Serving & Distribution | Daily Meal Summary to Parents | System automatically compiles served meal menus and nutrient structures and sends a daily summary notification to parents. (BR-133) |
| 44 | Meal Serving & Distribution | Meal Service Delay Notification | System sends an automated delay notification to class teachers and parents when meal service is delayed by more than 20 minutes from the scheduled time. (BR-135) |
| 45 | Feedback & Announcement Management | Illness Keyword Detection | System automatically scans parent feedback for illness-triggering keywords (e.g., 'stomach ache', 'vomit') and flags an emergency alert when detected. (BR-142) |
| 46 | Feedback & Announcement Management | Low Rating Follow-up Prompt | When a parent submits a feedback rating below 3 stars, the system automatically prompts the parent to select from a list of predefined quality concern tags. (BR-140) |
| 47 | Feedback & Announcement Management | Critical Alert Bypass | Critical food alerts (e.g., food recall notices) bypass the standard administrator approval workflow and are broadcast immediately to all affected users. (BR-138) |
| 48 | Feedback & Announcement Management | Weekly Feedback Aggregation | System automatically compiles aggregated feedback charts on a weekly basis and delivers the report to the catering manager and school board. (BR-141) |
| 49 | Incident & Emergency Management | Auto-Incident Trigger | System automatically initiates a food safety incident investigation when 3 or more students report matching symptoms within a 24-hour period. (BR-146) |
| 50 | Incident & Emergency Management | Batch Quarantine on Incident | System automatically places suspected meal batches and associated raw ingredients on quarantine status in inventory when a food safety incident is reported. (BR-149) |
| 51 | Incident & Emergency Management | Traceability Map Compilation | System automatically compiles traceability maps showing all raw ingredient batches and associated suppliers within 4 hours of a reported food safety incident. (BR-151) |
| 52 | Incident & Emergency Management | High-Severity Incident Alert | System immediately sends alerts to the school board, catering company, and local health department when a food safety incident is classified as high severity. (BR-148) |
| 53 | AI & Supporting Services | AI Menu Recommendation | Gemini AI Service analyzes nutritional requirements, student allergy profiles, in-stock ingredients, and food cost ceilings to suggest optimized menu options. (BR-156, WF 3.1.2.4 Step 4-5) |
| 54 | AI & Supporting Services | AI Nutrition Analysis | Gemini AI Service evaluates calorie balance, nutrient diversity, and meal composition against student age-group nutritional targets. (WF 3.1.2.4 Step 5, BR-062) |
| 55 | AI & Supporting Services | Quiet Hours Enforcement | System respects user-configured quiet hours (default: 10:00 PM to 6:00 AM) and suppresses non-emergency push notifications during this period. (BR-158) |
| 56 | AI & Supporting Services | Automated Database Backup | System performs full database backups automatically every Sunday at 02:00 AM. Backup files are encrypted with AES-256 and replicated to an off-site cloud storage location. (BR-160, BR-161) |
| 57 | AI & Supporting Services | System Health Metrics Report | System automatically compiles weekly system health and active user metrics into dashboard reports for the System Administrator. (BR-165) |
| 58 | School & Class Management | Single Active Academic Year Enforcement | System blocks activating a new academic year while another academic year for the same school is already Active, requiring it to be closed first. (BR-191) |
| 59 | Incident & Emergency Management | Repeat Staff Violation Detection | System automatically checks a newly recorded staff violation's staff name against existing Medium+ severity violations within the default 90-day lookback window and raises a best-effort warning for manual verification if a match is found. (BR-187) |
| 60 | Incident & Emergency Management | Weekly Override Abuse Compilation | System automatically compiles a weekly staff violation record summarizing detected manual-override abuse cases for School Administration review. (BR-190) |

### Mobile Screen

#### Guest and Common Screen

##### 3.3.2.1.1 Screen for Guest Portal

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| SFMS Logo | Displays an application logo or banner image at the top of the screen. Shown as a placeholder image frame with an 'X' mark in wireframe. |
| Welcome Text | Welcome message and system introduction |
| "Register School" Button | Registration button for Schools |
| "Register Supplier" Button | Registration button for Suppliers |
| "Register Service Caterers" Button | Registration button for Service Caterers |
| "Login" Button | Button leading to the login page |

##### 3.3.2.1.2 Screen for Register School

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to guest portal |
| School Name | School Name |
| School Address | School Address |
| Representative Name | Representative's Name |
| Email Address | Representative's contact email |
| Phone Number | Representative's phone number |
| "Submit Request" Button | Submit registration request to the system |
| "Cancel" Button | Cancel registration |

##### 3.3.2.1.3 Screen for Register Supplier

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to guest portal |
| Company Name | Supplying Company Name |
| Business License Number | Business License / Tax Identification Number |
| Contact Person | Contact Person's Name |
| Email Address | Company contact email |
| Phone Number | Contact phone number |
| "Submit Request" Button | Submit registration request to the system |
| "Cancel" Button | Cancel registration |

##### 3.3.2.1.4 Screen for Register Service Caterers

![](data:image/png;base64...)

Figure ‑ Screen for Register Service Caterers

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to guest portal |
| Service Caterers Name | Service Caterers Name |
| Business Registration No | Business registration number |
| Office Address | Service Caterers office address |
| Contact Person | Contact person's name |
| Email Address | Company contact email |
| Phone Number | Contact phone number |
| "Submit Request" Button | Submit registration request to the system |
| "Cancel" Button | Cancel registration |

##### 3.3.2.1.5 Screen for Login

![](data:image/png;base64...)

Figure ‑ Screen for Login

| Field name | Description |
| --- | --- |
| Username / Email | Username or account email |
| Password | Masked password display |
| "Login" Button | Perform login |
| "Forgot Password?" | Recover password |
| "Register Options" | Go to registration options |

Table ‑ Screen for Login

##### 3.3.2.1.6 Screen for Forgot Password

![](data:image/png;base64...)

Figure ‑ Screen for Forgot Password

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to Login screen |
| Email Address | Registered account email |
| "Send OTP" Button | Submit OTP request |

Table ‑ Screen for Forgot Password

##### 3.3.2.1.7 Screen for Verify OTP

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to Forgot Password screen |
| OTP Input Fields | 6-digit verification code |
| "Verify Code" Button | Confirm entered OTP code |
| Countdown Timer | Remaining time to enter OTP |
| "Resend OTP" | Resend OTP code |

##### 3.3.2.1.8 Screen for Reset Password

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| New Password | New Password |
| Confirm New Password | Confirm New Password |
| "Update Password" Button | Confirm password update |

##### 3.3.2.1.9 Screen for Personal Profile

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Avatar | User profile picture |
| Name | User name |
| Role & School / Company | Current role and affiliated organization |
| "Settings" Button | Go to application settings page |
| Menu "Edit Profile" | Transition to edit profile information |
| Menu "Change Password" | Change password |
| Menu "Notification Settings" | Notification settings |
| Menu "AI Chatbot" | Informational support chatbot |
| "Logout" Button | Log out of the account |
| Bottom Tab Bar | Main navigation bar (Home, Meals, Health, Profile) |

##### 3.3.2.1.10 Screen for Edit Profile

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Cancel" Button | Cancel editing |
| "Save" Button | Save edited information |
| Avatar Upload | Change profile picture |
| Full Name | User's full name |
| Phone Number | Phone number |
| Email Address | User email (Read-only) |

##### 3.3.2.1.11 Screen for Change Password

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to profile |
| Current Password | Current password |
| New Password | New Password |
| Confirm New Password | Confirm New Password |
| "Save Password" Button | Save new password |

##### 3.3.2.1.12 Screen for Notification Settings

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to profile |
| Push Notifications | Toggle push notifications on device |
| Email Notifications | Toggle email notifications |
| Allergy Warnings | Receive food allergy warnings |
| Food Safety Alerts | Receive food safety alerts |
| "Save Settings" Button | Save notification settings |

##### 3.3.2.1.13 Screen for Notification History

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to settings/profile |
| Notification List | List of notification items |
| Warning Badge | Warning Badge |
| Allergy Warnings | Distinguish severity (Red: Allergy/Food Safety alert, Yellow: News, Grey: System) |
| Sent Time | Time notification was sent (Hour/Date) |

##### 3.3.2.1.14 Screen for AI Chatbot

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to profile |
| "Clear Chat" Button | Clear history of current conversation |
| Conversation Pane (Chat bubbles) | Displays bot messages (left) and user messages (right) |
| "Send" Button | Active (only enabled when input has text) |
| "View Chat History" | View list of past chat history |

##### 3.3.2.1.15 Screen for AI Chat History

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to Chat screen |
| Conversation List | List showing topic summaries and start date of chats |

#### Service Caterers Screens

##### 3.3.2.2.1 Screen for Catering Dashboard

![](data:image/png;base64...)

Figure ‑ Screen for Catering Dashboard

| Field name | Description |
| --- | --- |
| Avatar | User profile picture |
| Notif Icon | Quickly view the number of unread notifications |
| Grid Menu Items | List of modules (Receiving, Inventory, etc.) |
| Latest Alerts Panel | Displays urgent alerts (Expiration, shortage) |

##### 3.3.2.2.2 Screen for Receiving Dashboard

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Expected Delivery List | List of supplier invoices scheduled for today |
| "Start New Receiving" | Start a new ingredient receiving process |
| "View History" | View history of previous receiving batches |

##### 3.3.2.2.3 Screen for Receive Ingredients

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Delivery ID & Supplier | Delivery ID and Supplier name |
| "Verify Invoice" | Navigate to quantity checklist |
| "Inspect Quality" | Navigate to quality inspection metrics |
| "Capture Evidence" | Take photos for evidence storage |
| "Log Rejections" | Record returned/rejected ingredient batches |
| "Send the Receipt of Goods | Submit receiving report |

##### 3.3.2.2.4 Screen for Verify Supplier Delivery

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Verification Checkbox | Confirm that physical delivery matches the invoice |
| Received Qty | Physically received quantity |
| "Save Checklist" | Save draft of the quantity status |

##### 3.3.2.2.5 Screen for Inspect Ingredient Quality

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Measured Temperature | Temperature of the batch upon receipt |
| Freshness Level | Select 1 of 3: Good, Fair, Poor |
| Pest-free check | Confirm absence of pests, insects, or mold |
| Expiration Date | Expiry date labeled on packaging |
| "Save Inspection" | Save quality inspection report |

##### 3.3.2.2.6 Screen for Capture Receiving Evidence

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Capture Photo Button | Take photo of invoice or packaging |
| Thumbnail list | List of captured photos |
| Notes | Additional remarks |

##### 3.3.2.2.7 Screen for Reject Ingredient Batch

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Select Ingredient | Select the defective ingredient from the invoice |
| Reason for Rejection | Reason for Rejection |
| Qty Rejected | Quantity to reject and return |
| Capture Rejection Photo | Capture photo proof of defect (damaged, spoiled, moldy) |

##### 3.3.2.2.8 Screen for Store Ingredients

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Select Batch | Select the recently received ingredient batch |
| Storage Unit | Select storage unit location |
| Assigned Temp Target | Standard regulated temperature for the selected unit |
| Shelf Assignment | Specific shelf or row number |

##### 3.3.2.2.9 Screen for Monitor Expiration Dates

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Alert List | List of ingredients close to expiry |
| "Dispose" | Record the disposal of expired ingredients |

##### 3.3.2.2.10 Screen for Issue Ingredients

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Target Meal & Dish | Targeted meal and dish |
| Required Qty | Required ingredient quantity based on recipe |

##### 3.3.2.2.11 Screen for Create Purchase Request

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Ingredient Search | Search for ingredients needing restock |
| Current Stock | Current inventory stock level |
| Requested Qty | Quantity to purchase |
| Urgency Level | Urgency level |

##### 3.3.2.2.12 Screen for Update Menu

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Week Selection | Select the week to edit menu |
| Day Tabs | Days of the week (Monday - Friday) |
| Dish List | List of dishes for the selected day |
| "Add Custom Dish" | Add a new dish to the selected day |

##### 3.3.2.2.13 Screen for View Menu Recommendations

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Option Cards | Menu options proposed by Gemini AI |
| "Apply Option" | Apply the proposed option to the week |

##### 3.3.2.2.14 Screen for Special Meals Configuration

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Student Name | Select student with allergies |
| Substitute Recipe | Select a safe substitute recipe |
| Color Sticker Code | Select color sticker for portion identification |

##### 3.3.2.2.15 Screen for Prepare Ingredients

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Session Selection | Select preparation shift (Morning, Noon, Afternoon) |
| Prep Checklist | Hygiene and cross-contamination prevention checklist |
| Start/End Prep Time | Prep start and end times |

##### 3.3.2.2.16 Screen for Record Cooking Information

![](data:image/png;base64...)

| Field name | Description |
| --- | --- |
| Select Dish | Select dish currently being cooked |
| Required Core Temp | Minimum required core temperature |
| Measured Core Temp | Actual measured core temperature using a thermometer |
| Measurement Time | Time of temperature measurement |
| Visual Appearance check | Confirm dish visually appears fully cooked |

#### Medical Staff Screens

##### Screens Medical Staff Dashboard

![](data:image/png;base64...)

Figure ‑ Screens Medical Staff Dashboard

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title |
| "Add / Plus" Icon | Shortcut button to quickly create or add a new record |
| "Notification" Icon | Display current alert notifications with a red badge showing the count |
| Welcome Banner | Display a personalized greeting, the user's name, assigned office room, and current date |
| "Allergy pending approval" Card | Statistics metric showing the total number of pending student allergy requests |
| "Meal order needs approval" Card | Statistics metric showing the total number of special meal plans awaiting approval |
| "The issue is being resolved" Card | Statistics metric showing the total number of active or ongoing safety incidents being resolved |
| "Compliance Points" Card | Statistics metric displaying the overall food safety compliance score percentage |
| "Health Profile" Button | Navigate to the health profile management section |
| "Student Allergy Review" Button | Navigate to the processing interface for student allergy approvals |
| "Approve Special Meal" Button | Navigate to the management and approval interface for special meal requests |
| "City Safety Incident" Button | Navigate to the food safety incident management and reporting interface |
| "Food Compliance & Monitoring Board" Button | Open the compliance checklists and food monitoring log records |
| "View food inspection results in 3 steps" Button | Open the historical logs and results of the 3-step food inspection process |

Table ‑ Screens Medical Staff Dashboard

##### 3.3.2.3.2 Screens Health Record For Medical Staff

![](data:image/png;base64...)

Figure ‑ Screens Health Record For Medical Staff

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Health Record") |
| "Back" Button | Return to the previous dashboard screen |
| "Home" Button | Navigate to the main dashboard |
| "All students" Tab | Filter and display the comprehensive list of all student health profiles |
| "Waiting for confirmation" Tab | Filter and display only the profile requests awaiting verification, showing the pending count |
| Search Bar | Input field with placeholder text to search or filter the student list by name or class |
| Student Profile Card | Display summarized health declaration details for an individual student |
| Student Name & Class | Display the target student's full name and their respective class |
| Status Badge | Indicate the approval state of the health declaration (e.g., "Awaiting Approval" or "Confirmed") |
| Allergy Declaration Info | Display the specific food or substance allergies declared for the student |
| Log Footnote | Show submission details (sender and date) or approval history (reviewer name and date) depending on the card status |
| Screen Title | Display the current screen title ("Health Record") |
| "Back" Button | Return to the previous dashboard screen |

##### 3.3.2.3.3 Screens Special Meal For Student

![](data:image/png;base64...)

Table ‑ Screens Special Meal For Student

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Special Meal") |
| "Back" Button | Return to the previous dashboard screen |
| "Home" Button | Navigate to the main dashboard |
| "Awaiting approval" Tab | Filter and display the list of special meal requests currently pending approval |
| "Approved" Tab | Filter and display the historical list of special meal requests that have already been approved |
| Special Meal Card | Display summarized details of an individual student's special dietary request |
| Student Name & Class | Display the target student's full name and their respective class |
| Status Badge | Indicate the current review status of the request (e.g., "Needs Approval") |
| Meal Type | Specify the exact dietary constraints or menu requirements (e.g., Purely vegetarian menu) |
| Reason | State the underlying reason or justification for the special meal requirement (e.g., Religion / Voluntary) |
| Screen Title | Display the current screen title ("Special Meal") |
| "Back" Button | Return to the previous dashboard screen |
| "Home" Button | Navigate to the main dashboard |
| "Awaiting approval" Tab | Filter and display the list of special meal requests currently pending approval |
| "Approved" Tab | Filter and display the historical list of special meal requests that have already been approved |
| Special Meal Card | Display summarized details of an individual student's special dietary request |

Table ‑ Screens Special Meal For Student

##### 3.3.2.3.4 Screens Incidents Records

![](data:image/png;base64...)

Table ‑ Screens Incidents Records

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("City Safety Incident") |
| "Back" Button | Return to the previous dashboard screen |
| "Home" Button | Navigate to the main dashboard |
| "Processing" Tab | Filter and display the list of active safety incidents currently being handled, showing the count of ongoing cases |
| "Resolved" Tab | Filter and display the historical log of safety incidents that have been successfully settled |
| Incident Card | Display summarized details of a specific reported health or safety incident |
| Incident Code & Title | Display the unique identification code assigned to the incident along with its primary classification (e.g., Vomiting) |
| Status Badge | Indicate the current stage of the incident handling process (e.g., "Under investigation") |
| Symptoms | Detail the specific medical signs or health conditions reported by the affected individuals |
| Number of students | Specify the total headcount of affected students and their respective classroom/grade details |
| Reported Log Footnote | Show historical time tracking information and the role of the person who initially submitted the incident report |
| Screen Title | Display the current screen title ("City Safety Incident") |
| "Back" Button | Return to the previous dashboard screen |
| "Home" Button | Navigate to the main dashboard |
| "Processing" Tab | Filter and display the list of active safety incidents currently being handled, showing the count of ongoing cases |

##### 3.3.2.3.5. Screens Incidents Records Detail

![](data:image/png;base64...)

Table ‑ Screens Incidents Records Detail

|  |  |
| --- | --- |
| Field name | Description |
| "Back" Button (Top Bar) | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Return to list" Button | Navigate back to the incident logs list view |
| Section Header | Label indicating the current section ("Medical Incident Details") |
| Case code | Display the unique identification code assigned to this specific medical incident |
| Clinical symptoms | Detail the comprehensive medical symptoms observed in the affected individuals |
| Affected students | Specify the number of affected students, their class, and their full names |
| Suspected food item | State the specific food or beverage item suspected to have caused the issue, along with meal time details |
| First aid was administered | Describe the initial medical treatments given and emergency actions taken for the patients |
| "Step 1: Assess the severity" Button | Navigate to the protocol interface for evaluating the impact level of the incident |
| "Step 2: Incident Investigation Space" Button | Open the dedicated interface for tracking investigation progress and collecting evidence |
| "Step 3: Submit the incident report..." Button | Open the reporting interface to finalize and send the official case report to the school Principal |
| "Back" Button (Top Bar) | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Return to list" Button | Navigate back to the incident logs list view |

##### 3.3.2.3.6. Screens Evaluate the level Incidents

![](data:image/png;base64...)

Table ‑ Screens Evaluate the level Incidents

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Assess the level") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Cancel" Button | Abort the current evaluation and return to the incident details screen |
| Section Header | Label indicating the current section ("Severity Assessment") |
| Severity level | Display the evaluated seriousness classification of the medical incident (e.g., High) |
| Clinical Risk Assessment & Recommendations | Section header designated for medical analysis notes and advisory recommendations |
| "Save review & Activate process" Button | Save the finalized evaluation data and initiate the official protocol procedure |

##### 3.3.2.3.7 Screens Investigation Space

![](data:image/png;base64...)

Table ‑ Screens Investigation Space

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Investigation Center") |
| "Back" Button (Top Bar) | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Back to Details" Button | Navigate back to the detailed view of the current incident |
| Section Header | Label indicating the current workspace ("Incident Investigation Space") |
| "1. Extracting Stored Food Samples" Card | Open the workspace to log and verify information about inspecting and sealing food samples collected within the past 24 hours |
| "2. Gathering Evidence" Card | Open the interface to upload supporting evidence such as sample photographs, written student statements, and interview transcripts |
| "3. Identify the Root Cause" Card | Open the interface to document laboratory testing outcomes and official expert clinical conclusions |
| "4. Proposed Remedial Measures" Card | Open the interface to assign clear responsibilities and corrective actions designed to prevent incident recurrence |

##### 3.3.2.3.8 Screens Export To School Administration

![](data:image/png;base64...)

Table ‑ Screens Export To School Administration

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Incident Report") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Come back" Button | Abort the reporting preview and return to the incident investigation space |
| Section Header | Label indicating the reporting target ("Incident Report to the Principal") |
| Incident Report Card | Display a summarized overview of the emergency food safety incident investigation details |
| Incident code | Display the unique identification code tracking this specific case |
| Date created | Display the timestamp when this final report was generated |
| Severity level | State the finalized seriousness classification of the reported incident |
| Food sample retained | Display the tracking code and status of the food samples kept for analysis |
| Conclusion | Display the official medical or factual finding regarding the root cause of the incident |
| Remedial measures | Outline the corrective actions and preventative assignments established to address the problem |
| System Info Text | Informative prompt explaining that the system will compile logs and photographic evidence into an official PDF for school administration |
| "Export & Submit Incident Reports" Button | Finalize the data, generate the PDF document, and officially transmit the report to school authorities |

##### 3.3.2.3.9 Screens Extraction of Stored Food Samples

![](data:image/png;base64...)

Table ‑ Screens Extraction of Stored Food Samples

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Food sample") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Come back" Button | Cancel the current sample viewing and return to the investigation workspace |
| Section Header | Label indicating the current context ("Related Food Storage Samples") |
| Food Sample Card | Container displaying specific retention and identification details for a food item |
| Sample Name | Display the name of the retained food item (e.g., Vinamilk UHT Fresh Milk Sample) |
| Status Badge | Indicate the current storage or state of the sample (e.g., "Currently Saving") |
| Model number | Display the unique classification or identification code assigned to the sample |
| Storage location | Specify the exact physical appliance and compartment where the sample is kept |
| Prepared by | Display the full name and job role of the person responsible for securing the sample |
| Storage time | Display the precise date and timestamp when the food sample was retained |
| "Seal Sample" Button | Lock and verify the food sample container to ensure it remains untampered |
| "Submit for Testing" Button | Authorize and send the selected food sample to the laboratory for clinical analysis |

##### 3.3.2.3.10 Screens Collect Evidence

![](data:image/png;base64...)

Table ‑ Screens Collect Evidence

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Gathering evidence") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Come back" Button | Cancel the current evidence gathering process and return to the investigation workspace |
| Section Header | Label indicating the current context ("Gathering Evidence") |
| Type of evidence | Display the classification or category of the evidence being collected (e.g., Student/Teacher Statement Record) |
| Brief description | Input text field with placeholder text to provide a short summary or notes about the evidence |
| Attached documents | Label indicating the section for uploading or attaching supporting files |
| "Take a photo / Upload a file" Button | Trigger the device camera to take a picture or open the file picker to upload evidence documents |
| "Save Evidence" Button | Save the current evidence details, descriptions, and attachments to the system |
| Screen Title | Display the current screen title ("Gathering evidence") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Come back" Button | Cancel the current evidence gathering process and return to the investigation workspace |
| Section Header | Label indicating the current context ("Gathering Evidence") |
| Type of evidence | Display the classification or category of the evidence being collected (e.g., Student/Teacher Statement Record) |
| Brief description | Input text field with placeholder text to provide a short summary or notes about the evidence |
| Attached documents | Label indicating the section for uploading or attaching supporting files |
| "Take a photo / Upload a file" Button | Trigger the device camera to take a picture or open the file picker to upload evidence documents |
| "Save Evidence" Button | Save the current evidence details, descriptions, and attachments to the system |

##### 3.3.2.3.11 Screens Treatment Measures

![](data:image/png;base64...)

Table ‑ Screens Treatment Measures

|  |  |
| --- | --- |
| Field name | Description |
| Screen Title | Display the current screen title ("Troubleshooting") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Come back" Button | Cancel the current action and return to the investigation workspace |
| Section Header | Label indicating the current context ("Remedial Measures") |
| Medical treatment/correction measures | Label for the section to define specific medical treatments or corrective actions taken |
| Unit/Person in charge | Label for identifying the responsible party or department for handling the issue |
| Responsible Party Input Field | Text input field displaying the assigned units or individuals (e.g., Kitchen Manager & Milk Supplier) |
| "Establish Measures" Button | Confirm and formally execute the defined remedial and corrective measures within the system |
| Screen Title | Display the current screen title ("Troubleshooting") |
| "Back" Button | Return to the previous screen |
| "Home" Button | Navigate to the main dashboard |
| "Come back" Button | Cancel the current action and return to the investigation workspace |
| Section Header | Label indicating the current context ("Remedial Measures") |
| Medical treatment/correction measures | Label for the section to define specific medical treatments or corrective actions taken |
| Unit/Person in charge | Label for identifying the responsible party or department for handling the issue |
| Responsible Party Input Field | Text input field displaying the assigned units or individuals (e.g., Kitchen Manager & Milk Supplier) |

#### Parent Screens

##### Parent Dashboard

![](data:image/png;base64...)

Figure ‑: Parent Dashboard Screen

| Field name | Description |
| --- | --- |
| Child Selector Dropdown | Dropdown menu at the top left. Label: 'Child: Nguyễn Minh Hải (Grade...'. Used to switch between children profiles. |
| Notification Bell Icon | Icon button at the top right. Tapping opens the Notification Center. |
| Allergy Threat Alert Box | High-priority alert card with a red border. Title: 'Menu Allergy Threat Detected!'. Tapping opens safety substitute settings. |
| Today's Lunch Menu Card | Container displaying today's dishes. Includes a text link 'Tap to view weekly details >' to open the weekly menu. |
| Health Hub Navigation Tile | Button with a person icon. Label: 'Health Hub'. Navigates to health and allergy management. |
| School Meals Navigation Tile | Button with a '$' icon. Label: 'School Meals'. Navigates to meal plans and billing. |
| Meal Feedback Navigation Tile | Button with a chat bubble icon. Label: 'Meal Feedback'. Navigates to the feedback submission form. |
| Meal History Navigation Tile | Button with a calendar icon. Label: 'Meal History'. Navigates to past meal logs. |
| Report Urgent Safety Incident Button | Full-width button with a red dashed border. Label: 'Report Urgent Safety Incident'. Triggers urgent reporting workflow. |
| Bottom Navigation Bar | Global navigation menu with 4 tabs: 'Home' (Active), 'Meals', 'Health', and 'Profile'. |

Table ‑ Parent Dashboard

##### Child Selection

![](data:image/png;base64...)

Figure ‑: Child Selection Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping returns the user to the previous screen. |
| Screen Title | Text header: 'Child Selection', located at the top navigation area. |
| Instruction Text | Informative text label: 'Select an active child profile to track meals and health details:'. |
| Child Profile Radio Group | A bounded container enclosing radio button options for child selection. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home' (Active), 'Meals', 'Health', and 'Profile'. |

Table ‑ Child Selection

##### Student Health Hub

![](data:image/png;base64...)

Figure ‑ Student Health Hub Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping returns the user to the previous screen. |
| Screen Title | Text header: 'Child Selection', located at the top navigation area. |
| Back Button | Arrow icon button (<-) at the top-left corner. Tapping returns the user to the previous screen. |
| Screen Title | Text header: 'Student Health Hub', located at the top navigation area. |
| Student Summary Card | Header container displaying vital student identity information: Name ('Nguyễn Minh Hải') and subtext details ('Age: 9 | Class: 3A'). |
| Height Card | Data tile showing the student's height. Label: 'Height'. Value: '132 cm'. |
| Weight Card | Data tile showing the student's weight. Label: 'Weight'. Value: '29 kg'. |
| Blood Type Card | Data tile showing the student's blood type medical record. Label: 'Blood Type'. Value: 'O+'. |
| BMI Status Card | Data tile showing the calculated BMI status highlighted in green text. Label: 'BMI Status'. Value: '16.6 (Normal)'. |
| Allergy Profile Summary Card | Container detailing critical allergy information. Section Header: 'Allergy Profile Summary' in red text. Bullet point list displays current allergens: '• Peanuts - Severity: Severe (Danger)'. |
| Manage Health Details Button | Dark filled full-width action button. Label: 'Manage Health Details'. Tapping navigates to the health information editing form. |
| Manage Allergy List Button | Dark filled full-width action button. Label: 'Manage Allergy List'. Tapping navigates to the allergy list management section. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health' (Active state), and 'Profile'. |

Table ‑ Student Health Hub

##### Update Health

![](data:image/png;base64...)

Figure ‑ Update Health Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping discards changes and returns the user to the previous screen. |
| Screen Title | Text header: 'Update Health', located at the top navigation bar. |
| Height Input Field | Numeric text input field. Label: 'Height (cm) \*' (Asterisk indicates it is a required field). Displays an existing or pre-filled value (e.g., '132'). |
| Weight Input Field | Numeric text input field. Label: 'Weight (kg) \*' (Asterisk indicates it is a required field). Displays an existing or pre-filled value (e.g., '29'). |
| Blood Type Dropdown | Dropdown selection menu. Label: 'Blood Type'. Displays the currently selected option (e.g., 'O') with a down arrow icon on the right to expand available blood types. |
| File Upload Field | Upload component with a document icon. Label: 'Other Medical Conditions / Notes'. Subtext action link: 'Upload Health Certificate / growth chart (PDF, JPG)'. Tapping allows users to select and attach files from their device. |
| Save Health Info Button | Dark filled full-width action button. Label: 'Save Health Info'. Tapping validates fields and saves the updated health data to the system. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health' (Active state), and 'Profile'. |

Table ‑ Student Health Hub

##### Add/Edit Allergy

![](data:image/png;base64...)

Figure ‑ Add/Edit Allergy Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping discards inputs and returns the user to the previous screen. |
| Screen Title | Text header: 'Add/Edit Allergy', located at the top navigation bar. |
| Allergen Category Dropdown | Dropdown selection menu. Label: 'Allergen Category'. Displays the currently selected category (e.g., 'Peanuts') with a down arrow icon on the right to view more options. |
| Specific Allergen Name Input | Text input field. Label: 'Specific Allergen Name (if Other)'. Displays placeholder text 'e.g. Almonds'. |
| Severity Level Radio Group | Radio button group to choose a severity level. Label: 'Severity level'. Contains 3 choices:  - Mild: Unselected.  - Moderate: Unselected.  - Severe: Selected by default and highlighted in red text. |
| Upload Medical Proof Component | Camera icon upload action field. Label: 'Upload Medical Proof \*' (Asterisk indicates it is conditionally required). Subtext notes: '(Required for Severe allergy approval)'. Tapping allows users to upload an image or document as proof. |
| Save Allergy Button | Dark filled full-width action button. Label: 'Save Allergy'. Tapping validates fields and saves the allergy log to the system. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health' (Active state), and 'Profile'. |

Table ‑ Add/Edit Allergy

##### Weekly Menu

![](data:image/png;base64...)

Figure ‑ Weekly Menu Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Weekly Menu', located at the top navigation bar. |
| Weekly Days Tab Bar | Horizontal tab navigation row consisting of school weekdays: 'Mon', 'Tue', 'Wed', 'Thu', 'Fri'. The 'Mon' tab is currently active/selected by default. |
| Standard Meal Item Card (1) | Content container for the first dish. Title: '1. Braised Pork with Eggs'. Description: 'Ingredients: Pork, eggs, sugar, spices'. Features a dark grey status tag labeled 'Lunch' on the right side. |
| Allergy Alert Meal Card (2) | Bounded container highlighted with a red border for critical warning. Title: '2. Stir-fried Pork with Peanuts' in red text. Description: 'Contains: Peanuts. Tap to view Red Label replacement option for Nguyễn Minh Hải.'. Features a red status tag labeled 'Allergy Alert' on the right. Tapping redirects to substitution options. |
| Standard Meal Item Card (3) | Content container for the third dish. Title: '3. Tomato Tofu Soup'. Description: 'Ingredients: Tofu, tomatoes, green onions'. Features a dark grey status tag labeled 'Lunch' on the right side. |
| View Nutrition Details Button | Dark filled full-width action button. Label: 'View Nutrition Details'. Tapping redirects the user to the detailed nutritional breakdown screen for the selected day's menu. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals' (Active state), 'Health', and 'Profile'. |

Table ‑ Weekly Menu

##### Dish Details

![](data:image/png;base64...)

Figure ‑ Dish Details Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Dish Details', located at the top navigation bar. |
| Dish Information Card | Bounded main container displaying comprehensive details for the selected dish: Dish Title, Dish Category, Catering Supplier, Kitchen. |
| Full Ingredient Log Section | Sub-section within the information card. Header: 'Full Ingredient Log'. Description text: 'Pork shoulder meat (Local Farms), boiled quail eggs (Poultry farms), garlic, scallions, fish sauce, sugar, dark soy sauce.'. |
| Allergen Warnings Section | Safety sub-section highlighted with a red header: 'Allergen Warnings'. Warning text statement: 'Contains Soy & Fish sauce. (Shellfish/Peanut free).' displayed in red text. |
| Give Feedback Button | Dark filled full-width action button. Label: 'Give Feedback on this Dish'. Tapping redirects the user to the meal feedback submission screen for this specific dish. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals' (Active state), 'Health', and 'Profile'. |

Table ‑ Dish Detail

##### Allergy Conflict Alert

![](data:image/png;base64...)

Figure ‑ Allergy Conflict Alert Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Allergen Conflict!' displayed in red text at the top navigation bar. |
| Allergen Warning Card | Bounded alert container highlighted with a red border. Features a red section header: 'ALLERGEN WARNING' and details the safety conflict text regarding the student's registered allergy and the specific menu dish. |
| Substitute Meal Allocation Card | Content container displaying alternative meal details. Section Header: 'Substitute Meal Allocation'. |
| Parental Advisory Note | Italicized informational text note at the center: '\* Parents should advise their child to double check the Red Sticker on their tray before consuming the meal.' |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals' (Active state), 'Health', and 'Profile'. |

Table ‑ Allergy Conflict Alert

##### Notifications

![](data:image/png;base64...)

Figure ‑ Notifications Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Notifications', located at the top navigation bar. |
| Settings Icon | Gear icon button at the top-right corner. Tapping opens notification preference configurations. |
| Notification Category Tabs | Horizontal tab row to filter messages: 'All', 'Safety' (highlighted in red text), 'Allergy', and 'Menu'. The 'All' tab is currently selected by default. |
| Safety Incident Alert Card | Notification item container highlighted with a red indicator bar on the left. Features a red badge labeled 'Safety Incident Alert', a timestamp ('Today, 11:20 AM'), and description text regarding a suspicious pork batch and menu swap. |
| Allergy Warning Card | Notification item container highlighted with an orange indicator bar on the left. Features an orange badge labeled 'Allergy Warning', a timestamp ('Yesterday'), and description text regarding an allergy mismatch check for Nguyễn Minh Hải. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home' (Active state), 'Meals', 'Health', and 'Profile'. |

Table ‑ Notifications

##### Meal Feedback

![](data:image/png;base64...)

Figure ‑ Meal Feedback Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping discards inputs and returns the user to the previous screen. |
| Screen Title | Text header: 'Meal Feedback', located at the top navigation bar. |
| Dish Selection Dropdown | Dropdown selection menu. Label: 'Select Dish to Review'. Displays the currently selected item (e.g., 'Braised Pork with Eggs (Today's lunch)') with a down arrow icon on the right to view other eligible dishes. |
| Rating Score Component | Interactive star-rating selector. Label: 'Rating Score'. Displays a 5-star baseline with 4 stars filled (selected) and 1 star greyed out (unselected) by default. |
| Review Comments Section | Input region for text feedback. Label: 'Review Comments'. BA note: This area provides space for an optional or required multiline text area entry right beneath the label. |
| Photo Attachment Component | Camera icon upload action field. Label text link: 'Attach Actual Meal Tray Photo'. Tapping allows users to activate the device camera or choose an image from their gallery. |
| Send Feedback Button | Dark filled full-width action button. Label: 'Send Feedback'. Tapping validates inputs and submits the review log to the system. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health', and 'Profile'. |

Table ‑ Meal Feedback

##### Report Safety Issue

![](data:image/png;base64...)

Figure ‑ Report Safety Issue Screen

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping discards entries and returns the user to the previous screen. |
| Screen Title | Text header: 'Report Safety Issue' displayed in red text at the top navigation bar. |
| Concern Category Dropdown | Dropdown selection menu. Label: 'Concern Category \*' (Required). Displays the selected issue (e.g., 'Spoiled/Sour Food') in red text with a down arrow icon on the right to open more categories. |
| Symptoms Checkbox Group | Multiple-choice checkbox group. Label: 'Symptoms Shown by Child'. Contains 4 options (all unselected by default): Stomach Pain, Vomiting/Nausea, Diarrhea, Fever. |
| Incident Details Section | Input region for reporting details. Label: 'Incident Details \*' (Required). BA note: This area provides space for a required text area description input right below the label. |
| Photo Attachment Component | Camera icon upload action field highlighted in red. Label text link: 'Attach Food Error / Medical Report Photo \*' (Asterisk indicates it is a required upload). Tapping opens the device camera or gallery. |
| Send Urgent Report Button | Red filled, full-width action button. Label: 'Send Urgent Report' in white text. Tapping validates inputs and immediately submits the urgent safety report to the system. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health', and 'Profile'. |

Table ‑ Report Safety Issue

##### Meal History Log

![](data:image/png;base64...)

Figure ‑ Meal History Log

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping returns the user to the previous dashboard screen. |
| Screen Title | Text header: 'Meal History Log', located at the top navigation bar. |
| Instruction Text | Informative text label: 'Select calendar day to see child's historical consumption:'. |
| Calendar Strip Component | Interactive two-week calendar row display showing disabled past dates (1-4, 6-7, 13-14), selectable log dates (5, 8, 9) in dashed boxes, and the active date (10) in a dark filled square. |
| Meal History Summary Card | Container displaying selected day's details: Date (10 June 2026), Meal Intake (100% of serving), Portion Type (Standard), and Satisfaction status. |
| Weekly Charts Button | Bounded full-width navigation button. Label: 'Weekly Charts'. Tapping opens visual consumption reports. |
| Special Meals History Button | Bounded full-width navigation button. Label: 'Special Meals History'. Tapping opens logs for alternative diet adjustments. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home' (Active state), 'Meals', 'Health', and 'Profile'. |

Table ‑ Meal History Log

#### Raw Food Supplier Screen

##### 3.3.2.5.1 Supplier Dashboard

![](data:image/png;base64...)

Figure ‑ Supplier Dashboard

| Field name | Description |
| --- | --- |
| Screen Title / Breadcrumb | Display the current navigation path ("Homepage > Supply") |
| "Log Out" Icon | Terminate the active session and sign out of the account |
| Partner Welcome Banner | Display the supplier's company name and their currently linked institutional school partner |
| Section Header 1 | Label indicating logistics functionality ("Supply & Logistics Services") |
| "Purchase Request" Button | Navigate to the procurement and order request management section |
| "Delivery Management" Button | Open the tracking and management portal for ongoing food deliveries |
| "Raw Material Shipment" Button | Navigate to the inventory and tracking records for raw ingredient shipments |
| "Food Safety Compliance Record" Button | Access historical documentation and logs regarding food safety standards compliance |

Table ‑ Supplier Dashboard

##### 3.3.2.5.2 Partnership Detail

![](data:image/png;base64...)

Figure ‑ Partnership Detail

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Partner details") |
| "Back" Button (Top Bar) | Return to the previous screen |
| "Add / Plus" Icon | Shortcut button to create or add a new record within partner management |
| "Come back" Button | Abort the current details view and return to the partner directory list |
| Section Header | Label indicating the data category ("School Partner Details") |
| School Partner Card | Main container displaying administrative and contact information for the selected school |
| School Name | Display the name of the affiliated academic institution (e.g., Springfield Elementary School) |
| Delivery address | Specify the designated drop-off or shipping destination address for orders |

Table ‑ Partnership Detail

##### 3.3.2.5.3 Purchase Request Screen

![](data:image/png;base64...)

Figure ‑ Purchase Request Screen

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Material Order") |
| "Back" Button | Return to the previous dashboard screen |
| Section Header | Label indicating the current list category ("Purchase order list") |
| Purchase Order Card | Container displaying summary details for an individual material order |
| Order code | Display the unique tracking and identification code assigned to the purchase order |
| Status Badge | Indicate the current fulfillment state of the order (e.g., "Awaiting confirmation" or "Delivery completed.") |
| Source of request | Identify the job role or origin point initiating the material request (e.g., Springfield Head Chef) |
| Delivery date required | Specify the target deadline date and strict cutoff time expected for a pending shipment |

Table ‑ Purchase Request Screen

##### 3.3.2.5.4 Purchase Request Screen Detail

![](data:image/png;base64...)

Figure ‑ Purchase Request Screen Detail

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title |
| "Back" Button (Top Bar) | Return to the previous screen |
| "Come back" Button | Cancel the current detailed view and return to the purchase order directory list |
| Order number | Display the unique identification code assigned to track this specific purchase order |
| Recipient | Specify the designated institutional location or school canteen facility receiving the order |
| Delivery deadline | Display the target delivery date, cutoff time, and critical operational requirements for fulfillment |
| List of ingredients to order | Section header introducing the breakdown of raw materials requested |
| Ingredients List Container | Display the specific food items, requested weight quantities, and expected batch or lot tracking codes |

Table ‑ Purchase Request Screen Detail

##### 3.3.2.5.5 Compliance Docs Screens

![](data:image/png;base64...)

Figure ‑ Compliance Docs Screens

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Compliance Record") |
| "Back" Button | Return to the previous dashboard screen |
| "Add / Plus" Icon | Shortcut button to upload or add a new compliance document or certificate |
| "Food Safety Certificate" Card | Navigate to the management section for handling the farm's overall food safety and hygiene certification |
| "Product Origin Certification" Card | Navigate to the management section for tracking documentation related to VietGAP, GlobalGAP, Organic standards, and other provenance credentials |

Table ‑ Compliance Docs Screens

##### 3.3.2.5.6 Ingredient Batches Screens

![](data:image/png;base64...)

Figure ‑ Ingredient Batches Screens

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Raw Material Batch") |
| "Back" Button | Return to the previous dashboard screen |
| "Add / Plus" Icon | Shortcut button to create or add a new raw material batch record |
| Section Header | Label indicating the current list category ("List of outgoing shipments") |
| Material Batch Card | Container displaying summary logistics and compliance details for a specific batch |
| Batch Name & Code | Display the name of the raw material item alongside its unique batch identification code |
| Status Badge | Indicate the certified quality or health status achieved by the batch (e.g., "Quarantine passed" or "VietGAP achieved") |
| Packaging / Harvest Date | Display the specific date when the batch was packed or harvested |

Table ‑ Ingredient Batches Screens

##### 3.3.2.5.7 Supplier Upload Certificates

![](data:image/png;base64...)

Figure ‑ Ingredient Batches Screens

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Upload documents") |
| "Back" Button | Return to the previous screen |
| "Add / Plus" Icon | Shortcut button to initiate another related creation or upload workflow |
| "Cancel" Button | Abort the document submission process and return to the previous screen |
| Section Header | Label indicating the active action ("Upload New Profile") |
| Classification of compliance documents | Label indicating the category of the regulatory document being handled |
| Document Classification Text | Display the selected type of document compliance profile (e.g., "Farm Food Safety Certificate") |
| Expiration date of the document | Label indicating the field to specify when the document becomes legally invalid |

Table ‑ Ingredient Batches Screens

#### Teacher Screens

##### Teacher Dashboard

![](data:image/png;base64...)

Figure ‑ Teacher Dashboard Screen

| Field name | Description |
| --- | --- |
| Screen Title Header | Page title text block displaying 'Dashboard' and subtext configuration 'Class in charge: Class 1A · Ms. Lan'. |
| Greeting Banner | Highlighted box container with blue text displaying 'Hello, Ms. Lan!' and the current system date 'Today: 17/06/2026'. |
| Total Students Card | Metric summary tile showing the number 35 with the subtext label 'Students in Class 1A'. |
| Meals Pending Card | Metric summary tile showing the number 25 with the subtext label 'Meals pending'. |
| Dietary Notes Card | Metric summary tile showing the number 3 with the subtext label 'Dietary notes'. |
| Health Incidents Card | Metric summary tile showing the number 2 with the subtext label 'Health incidents sent'. |
| Distribute Meals Today Button | Dark filled full-width action button labeled 'Distribute Meals Today'. Tapping opens the meal tracking/distribution workflow. |
| Report New Health Incident Button | Bordered full-width secondary action button labeled 'Report New Health Incident'. Tapping opens the health incident reporting form. |
| Today's Notice Board Card | Bounded information list highlighted with a brown border. Displays urgent bullet points regarding a student's shrimp allergy, vegetarian meal distribution, and the latest student incident status. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home' (Active/shaded state), 'Students', 'Meals', and 'Incidents'. |

Table ‑ Teacher Dashboard

##### Meal Distribution

![](data:image/png;base64...)

Figure ‑ Meal Distribution Screen

| Field name | Description |
| --- | --- |
| Home Shortcut Button | Small bordered button labeled 'Home' at the top left. Tapping returns the user directly to the main dashboard. |
| Screen Title Header | Page title text block displaying 'Meal Distribution'. |
| Status Filter Tabs | Horizontal tab navigation row to filter the student list: 'All (35)', 'Pending', 'Served', and 'Special Meal'. |
| Student Meal Status Cards | Scrollable list of student containers displaying avatar initials, student names, dietary tags ('Vegetarian Meal', 'Normal Meal'), allergy alerts in red text ('Warning: Shrimp Allergy'), and actionable elements: Status Indicator, Details Link. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students', 'Meals' (Active/shaded state), and 'Incidents'. |

Table ‑ Meal Distribution

##### Student List in Class

![](data:image/png;base64...)

Figure ‑ Student List in Class Screen

| Field name | Description |
| --- | --- |
| Home Shortcut Button | Small bordered button labeled 'Home' at the top left. Tapping returns the user directly to the main dashboard. |
| Screen Title Header | Page title text block displaying 'Class 1A Student List'. |
| Profile Filter Tabs | Horizontal tab navigation row to filter students: 'All' (Active/dark filled state), 'Allergy Warning', 'Special Diet', and 'Normal'. |
| Student Record Cards | Scrollable list of student containers displaying avatar initials, student full names, conditional condition badges ('Allergy Warning' in red, 'Special Diet' in yellow, or 'Normal' in grey), and an actionable Profile text link on the right side to open the individual student profile log. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students' (Active/shaded state), 'Meals', and 'Incidents'. |

Table ‑ Student List in Class

##### Health Incident Report

![](data:image/png;base64...)

Figure ‑ Health Incident Report Screen

| Field name | Description |
| --- | --- |
| Screen Title Header | Page title text block displaying 'Health Incident Report' and subtext 'Record student condition'. |
| Student Input Field | Bounded text input box. Label: 'STUDENT'. Displays placeholder text 'Student's full name...'. |
| Class Input Field | Smaller text input box located to the right of the student field. Label: 'CLASS'. Pre-filled with the static value '1A'. |
| Detected Symptoms Checkboxes | Multiple-choice checkbox group. Label: 'DETECTED SYMPTOMS'. Contains options for 'Stomachache', 'Vomiting', 'Allergy', 'Diarrhea', and 'Fever'. Includes an 'Other:' option tied to a text input displaying placeholder text 'Specify symptom...'. |
| Severity Level Radio Group | Radio button row selection block. Label: 'SEVERITY LEVEL'. Offers three choices: 'Mild' (Selected by default), 'Medium', and 'Severe'. |
| Incident Description Input | Multiline text area block. Label: 'INCIDENT DESCRIPTION'. Displays placeholder text 'Enter incident details...'. |
| Attached Photos Component | File attachment container. Label: 'ATTACHED PHOTOS'. Features twin action buttons 'Take' and 'Choose' with status indicator text reading 'No files uploaded yet'. |
| Cancel Button | Red-bordered, full-width secondary action button labeled 'Cancel' in red text. Tapping discards inputs and returns to the dashboard. |
| Submit Incident Report Button | Dark filled full-width action button labeled 'Submit Incident Report'. Tapping validates entries and logs the medical report. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students', 'Meals', and 'Incidents' (Active/shaded state). |

Table ‑ Health Incident Report

##### Incident History

![](data:image/png;base64...)

Figure ‑ Incident History Screen

| Field name | Description |
| --- | --- |
| Home Shortcut Button | Small bordered shortcut button labeled 'Home' at the top left. Tapping redirects users directly to the main dashboard. |
| Screen Title Header | Page title text block displaying 'Incident History'. |
| Assessment Filter Tabs | Horizontally scrollable tab row to filter logs: 'All' (Active/dark filled state), 'Pending Assessment', and 'Under Assessment'. |
| Incident Log Cards | Scrollable list of recorded medical event containers displaying student identity details, reported symptoms, a severity indicator badge ('Mild', 'Medium'), the report date string, and a status label badge ('Pending Assessment' in yellow or 'Concluded' in green). |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students', 'Meals', and 'Incidents' (Active/shaded state). |

Table ‑ Incident History

## Non-Functional Requirements

### Performance Requirements

1. The system shall respond to operational and data requests within an average of ≤ 3 seconds.
2. When capturing verification photos, the system shall automatically validate the user's GPS coordinates against the designated school canteen or gate location within a radius of 50 meters (as defined by BR-079).
3. The system shall support rapid searching and history retrieval for ingredient inventory and processing logs.

### Reliability Requirements

1. The system shall support continuous operation to ensure uninterrupted tracking of the school kitchen workflow.
2. All operational data shall be stored centrally to minimize data loss and ensure informational consistency.
3. All user operational activities and status changes shall be automatically logged for auditing and inspection purposes.
4. The system shall achieve a minimum monthly uptime of 99%.
5. The database shall be automatically backed up on a daily basis.
6. For critical system failures (Priority 1 incidents), the recovery time shall not exceed 2 hours.

### Security Requirements

1. User passwords shall be hashed using BCrypt before being stored in the database.
2. The system shall use JSON Web Tokens (JWT) for authentication.
3. SQL injection attacks shall be prevented through parameterized queries using Spring Data JPA (Hibernate).

### Usability Requirements

1. The user interface shall be simple, intuitive, and easy to adopt for school management, kitchen staff, and medical personnel.
2. All system error messages shall be displayed in a clear, actionable, and understandable manner.
3. The system shall support responsive layouts for:
   * **Desktop devices:** Minimum width of 1280px
   * **Mobile/Tablet devices:** Minimum width of 360px

### Data Requirements

1. Uploaded images (e.g., verification photos, food sample photos, invoices) shall be in JPG or PNG format and must not exceed 5 MB.
2. The system shall ensure data integrity for food batch records, processing history, and sample storage logs.

### Operational Constraints

1. The system shall operate within the territory of Vietnam and comply with national food safety standards.
2. The system requires a stable Internet connection for full functionality.

## Requirement Appendix

### Business Rules

#### 1. User Management & Authentication

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-001 | Password encryption | All user passwords **must** be hashed with BCrypt before being stored. Plain-text passwords are never stored. |
| BR-002 | Account lockout on failed login | After **5** consecutive failed login attempts, the system **automatically locks** the account for **15 minutes**. The lockout event must be recorded in the audit log. |
| BR-003 | Password reset token expiry | A "Forgot Password" reset token is valid for **1 hour** from creation. After expiry, the user must request a new link. Maximum **1 reset request per 15 minutes** per email. |
| BR-004 | Session timeout | A login session automatically expires after **30 minutes** of inactivity. On timeout, the session token is invalidated and the user is redirected to the login screen. |
| BR-005 | Single-device session | Each account may be **logged in on only one device** at a time. Logging in from a new device invalidates the current session. |
| BR-006 | Password strength policy | Passwords must satisfy a minimum security policy (length, special characters, etc.). A new password **must not match any of the last 3 passwords** (BR-015). |
| BR-009 | Accept terms on first login | On the **first login**, the user must accept the **Privacy Policy** and **Terms of Use** before accessing any function. |
| BR-010 | Email verification before login | An account may log in only after its **email address has been verified**. Unverified accounts are blocked and prompted to verify. |
| BR-011 | Account activation token expiry | The account activation link is valid for **48 hours** from creation. If expired, the system allows resending a new link with another 48-hour validity. |
| BR-012 | User information format | Phone numbers must follow a valid format; the system warns if the format is invalid. Email must be a valid, system-unique address. |

#### 2. Role Management & Access Control (RBAC)

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-007 | Role-Based Access Control (RBAC) | The system applies an RBAC model. Each user is assigned one or more roles; access rights are determined entirely by role. System roles: **System Admin, School Administration, Medical Staff, Teacher, Parent, Raw Food Supplier, Service Caterers**. |
| BR-008 | Default system roles are protected | Default system roles **cannot be deleted**. Only **custom** roles may be deleted, by System Admin, and only when **no user** (including deactivated users) is currently assigned that role. |
| BR-013 | Staff account creation authority | Only **School Administration** can create staff accounts for its own school. **System Admin** can create user accounts system-wide. |

#### 3. School & Class Management

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-020 | One homeroom teacher per class | Each classroom may have only **one homeroom teacher** at a time. Assigning a new teacher automatically unlinks the previous one and logs the change. |
| BR-021 | Cascading school deactivation | Deactivating a school automatically **suspends** all of its classrooms, staff accounts, and student accounts. This action must be logged in the audit log. |
| BR-022 | Role-based visibility of school policies | Users can view policies only for **their own school** (per `schoolId` in JWT). **Teacher, Medical Staff, Service Caterers** may view their school's policies for daily operations. Cross-school access is always denied. |
| BR-024 | Valid grade level | Class grade level must be between **1 and 12** (Vietnamese general education system). |
| BR-025 | Maximum classroom capacity | Each classroom has a maximum capacity of **50 students**. |
| BR-026 | Teacher assignment audit logging | Every homeroom teacher assignment or replacement must be recorded in the **audit trail** with timestamp and actor. |
| BR-028 | School GPS coordinate verification & lock | School GPS coordinates must be valid: **latitude** in **-90.0 to 90.0**, **longitude** in **-180.0 to 180.0**; out-of-range values are rejected. Once coordinates are confirmed (school becomes **ACTIVE**), `coordinateLocked` is set to `true`; while locked, coordinate updates are rejected (other fields — address, phone, description — remain editable; all location verification relies on GPS, not the text address). **Unlock exception**: only **System Admin** may reset `coordinateLocked = false`, and must supply a written reason, logged in the audit log (actor, time, reason); after new coordinates are saved, `coordinateLocked` automatically returns to `true`. GPS coordinates are used by other modules (MF06 Ingredient Receiving, MF07 Three-Step Inspection, MF08 Food Sample Retention) as the central reference point for location verification. **Default geofence radius**: 200 meters from the school's center coordinate, configurable by School Administration between **50 m and 500 m**; any radius change must be logged in the audit log. |

#### 4. Student & Parent Management

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-030 | Student–parent linkage | Each student must be linked to at least one parent account. Allergy information and special dietary requirements must be declared and stored in the student profile. |
| BR-031 | Health information review | Health and allergy information declared by parents must be **reviewed and confirmed by Medical Staff** before taking effect. |
| BR-032 | Bulk import of students & parents | The system supports bulk import of student and parent profiles via a predefined template with data validation rules. |

#### 5. Supplier & Caterer Management

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-040 | Supplier registration process | A supplier must **submit a complete application**: company profile, business license, food safety certification, and product information. The application must be **reviewed and approved by School Administration** before the supplier may operate. |
| BR-041 | Supplier status lifecycle | Supplier status can be **Pending → Approved / Rejected**. Only suppliers in **Approved** status may deliver ingredients to a school. |
| BR-046 | Required compliance documents | Suppliers must maintain valid compliance documents: food safety certification, business license, product origin documents. Expired documents trigger a warning and may affect partnership status. |
| BR-047 | Document expiry warning | The system displays an **"Expiring Soon"** warning when a supplier document has **fewer than 30 days** remaining before expiry. |
| BR-048 | Supplier rejection | When rejecting a supplier, the **rejection reason** must be recorded in the system and a notification sent to the supplier. |
| BR-050 | Delivery staff registration | Each supplier delivery staff member must be registered with: full name, national ID number, phone number, license plate. The national ID number must be unique in the system. |
| BR-051 | Face biometric authentication | Delivery staff must register facial biometric data to be authenticated at the school gate. A supplier may delete a delivery staff member's biometric data, revoking gate access. **Note**: biometric authentication is **not applied to students** (children), for personal data protection (PDPD) reasons. |
| BR-060 | GPS location verification on delivery | The system verifies the delivery staff's GPS location to ensure delivery occurs within the permitted area. |

#### 6. Ingredient & Inventory Management

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-070 | Supplier status check on receiving | At the start of a receiving session, the system **automatically verifies** that the supplier is in **Approved** status; if not, the receiving session is rejected. |
| BR-071 | Mandatory batch certification | Every ingredient batch received must include: batch number, production date, expiry date, quantity, and relevant certificates. |
| BR-072 | Mandatory evidence photos on receiving | Receiving staff **must photograph** the ingredients, labels, packaging, and supporting documents as evidence. |
| BR-073 | Automatic GPS & timestamp logging | The system **automatically records** GPS location, device information, and timestamp during the receiving process, as an anti-fraud measure. |
| BR-074 | Receiving location verification | The system verifies that the receiving activity is performed **within the school's permitted receiving area**. |
| BR-075 | FEFO principle (First Expired, First Out) | The system prioritizes ingredient usage by **FEFO** — the batch expiring soonest is issued from stock first. |
| BR-076 | Ingredient batch traceability | The system records the ingredient batch number linked to each menu and production run to ensure **full traceability**. |
| BR-077 | Low-stock alert | The system automatically raises a **low-stock alert** when inventory is insufficient to meet the planned meal requirements. |
| BR-078 | Rejecting a non-conforming ingredient batch | A batch that fails quality inspection may be **rejected**; the rejection reason must be recorded with supporting evidence photos. |

#### 7. Menu & Diet Management

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-061 | Menu submission deadline | A menu must be submitted for approval **at least 3 days** before its intended week of use. |
| BR-062 | Age-group nutrition targets | A menu must meet **age-group nutrition targets** (calories, nutrient diversity, meal composition balance). |
| BR-063 | Allergy conflict check | The system **must check for allergy conflicts** between menu ingredients and student allergy profiles before menu approval. If a serious conflict is detected, submission is **blocked**. |
| BR-064 | Inventory-aware menu optimization | AI menu suggestions must prioritize ingredients **already available in stock** and comply with the configured **cost cap**. |
| BR-065 | Mandatory special meal plans | For students with verified allergies or special diets, the system **requires** Service Caterers to create a **separate meal plan**. |
| BR-066 | Special diet categories | The system supports special diet types: **Halal, Vegetarian, Gluten-Free, Nut-Free**, and others. |
| BR-067 | Locking an approved menu | A menu in **Approved** or **Published** status cannot be edited or deleted. Only menus in **Draft** or **Rejected** status may be edited. |
| BR-068 | Menu approval workflow | A menu must follow the workflow: **Draft → Pending Approval → Approved / Rejected → Published**. Only **School Administration** may approve or reject a menu. |
| BR-069 | Complete ingredient declaration | Service Caterers must declare **all ingredients and potential allergens** for every dish on the menu before submitting for approval. |
| BR-080 | Menu cost cap | A menu must comply with the **cost cap** configured by School Administration. |
| BR-081 | Substitute dish for common allergies | A menu must provide a **substitute dish** for the **5 most common food allergies**, where applicable. |
| BR-082 | Separate preparation for special meals | Special meals must be prepared **entirely separately**, using dedicated cooking equipment to avoid cross-contamination. |

#### 8. Three-Step Food Inspection

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-079 | Mandatory three-step inspection | Every food batch must go through **exactly 3 inspection steps**: **Step 1** — pre-cooking ingredient check (quality, freshness, packaging, batch info); **Step 2** — post-cooking food check (appearance, smell, texture, doneness); **Step 3** — pre-serving meal check (quality, portioning, serving conditions, hygiene). |
| BR-083 | Evidence recording at every step | Every inspection step must be accompanied by **evidence photos, GPS location, and timestamp**, recorded automatically. |
| BR-090 | Minimum post-cooking food temperature | Food after cooking must reach a **minimum core temperature of 75°C** (checked at Step 2). |
| BR-091 | Minimum holding temperature | Cooked food awaiting service must be held at a **minimum of 60°C** (checked at Step 3). |
| BR-092 | Ingredient expiry check | The system automatically **checks the expiry date** of ingredient batches used during the inspection step. |
| BR-093 | Complete inspection record | Every three-step inspection session must produce a **complete inspection record** linked to the ingredient batch and meal production record. |
| BR-094 | Block serving on failed inspection | A meal **may not be served** if any of the three inspection steps fails. |
| BR-095 | Non-conformance recording | When an inspection step fails, the system must record **non-conformance details** and automatically **create a Corrective Action Request**. |
| BR-096 | Re-inspection after corrective action | After a corrective action is completed, the failed inspection step must be **re-performed (re-inspection)**. |
| BR-097 | Integration with incident management | A seriously failed inspection result must be automatically linked to the **incident management** system. |
| BR-098 | Meal approval for serving | The system approves a meal for serving only after **all 3 steps** have **Passed**. |
| BR-099 | Inspection record access rights | **Medical Staff** and **School Administration** may view all inspection records. **Service Caterers** may view only records from inspection sessions they themselves performed. |

#### 9. Food Sample Retention

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-105 | Mandatory retention after passed inspection | After a food batch **passes the 3-step inspection**, Service Caterers **must** retain a food sample. |
| BR-106 | Mandatory sample labeling | Every retained sample must be labeled with: **dish name, production date, retention date, and batch reference number**. |
| BR-107 | Mandatory sample photos | Staff **must photograph** the retained samples and their labels as operational evidence. |
| BR-108 | Automatic GPS & timestamp on retention | The system **automatically records** GPS, device information, and timestamp during the retention process. |
| BR-109 | Retention location verification | The system verifies that sample retention is performed in the **designated kitchen or storage area**. |
| BR-110 | Sample storage temperature | The retention refrigerator must be kept between **2°C and 8°C**. The system **raises an alert** if the temperature deviates from this range for more than **30 minutes**. |
| BR-111 | Minimum retention period | The system **automatically tracks** the required retention period per food safety regulations. Samples must be kept for at least the minimum required duration. |
| BR-112 | Sample disposal after retention period | After the mandatory retention period elapses, staff dispose of the sample per procedure; the system records the disposal details, **the responsible staff member**, and the timestamp. |
| BR-113 | Sample recall on incident | When a food safety incident occurs, **Medical Staff** may recall a retained sample for investigation; the system records the recall, its investigative purpose, and the actor. |
| BR-114 | Linking samples to menu and inspection | A sample retention record must be **linked** to the related menu, inspection record, and meal production batch. |

#### 10. Meal Serving & Distribution

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-120 | Class-based meal distribution confirmation | The **Teacher** confirms meal distribution to students in their own class. |
| BR-121 | Student meal history tracking | The system records and tracks each student's **meal consumption history**. |
| BR-123 | Allergy alert on student ID scan | When a student with a **registered food allergy** is scanned during a serving session, the system **automatically triggers a visual and audible alert** on the serving screen to prevent incorrect meal distribution. |
| BR-125 | Viewing special meal requests during serving | Teachers and serving staff must be able to view each student's **special meal requirements** before and during the serving session. |
| BR-130 | Prevent duplicate distribution | The system **blocks duplicate meal distribution** — a student cannot be scanned twice within the same serving session. |
| BR-133 | Daily meal summary for parents | The system **automatically compiles** the served menu and nutrition breakdown, sending a **daily summary notification** to parents. |
| BR-135 | Meal serving delay notification | The system **automatically notifies** the homeroom teacher and parents when meal serving is delayed by more than **20 minutes** from the planned time. |

#### 11. Food Incident Management

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-146 | Automatic incident investigation trigger | The system **automatically initiates** a food safety incident investigation when **3 or more students** report similar symptoms within **24 hours**. |
| BR-147 | Incident severity classification | **Medical Staff** is responsible for classifying the **severity** of an incident. |
| BR-148 | Immediate alert for high-severity incidents | An incident classified as **High Severity** must **immediately** trigger alerts to: school management, the catering company, and local health authorities. |
| BR-149 | Automatic batch quarantine on incident | When a food safety incident is reported, the system **automatically sets Quarantine status** on all suspect food batches and related ingredients in stock. |
| BR-150 | Root cause investigation and analysis | The system must support **Root Cause Analysis** with the ability to attach evidence. |
| BR-151 | Traceability map generation | Within **4 hours** of an incident report, the system **automatically compiles a traceability map** for all related ingredient batches and suppliers. |
| BR-152 | CAPA management (Corrective and Preventive Action) | After every incident, a **CAPA plan** must be created, assigned, and tracked to completion. |
| BR-153 | Corrective action approval | **School Administration** may **approve or reject** proposed corrective actions. |
| BR-154 | Formal incident report | **Medical Staff** may generate a **formal incident report** for record-keeping and export. |
| BR-155 | Closing an incident | Only **School Administration** may **close** an incident, after confirming completion of corrective actions. |

#### 12. Food Safety Monitoring

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-089 | Refrigerator temperature monitoring | The system continuously monitors refrigerator temperature and raises an alert when the temperature **deviates from the allowed range** for more than 30 minutes. |
| BR-100 | Daily hygiene score | The system must record and track the kitchen's **daily hygiene score**. |
| BR-101 | Monthly safety & hygiene inspection | The system must store the results of **periodic monthly safety and hygiene inspections**. |
| BR-102 | Pest control log | The system must store a **pest control log** for the kitchen. |
| BR-103 | Kitchen staff health certificate validity | The system must track the validity of **kitchen staff health certificates** per regulation. |
| BR-104 | Chef hygiene score | Kitchen staff hygiene scores are aggregated and used in the **Food Safety Compliance Report**. |

#### 13. Feedback & Notification

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-138 | Critical alerts bypass approval workflow | Critical food safety alerts (e.g., a food recall) are **broadcast immediately** to all relevant users without going through an administrator approval workflow. |
| BR-139 | Feedback submission time window | Parents and teachers may submit meal feedback only within **24 hours** of the meal being served. |
| BR-140 | Mandatory quality-concern tag on low rating | When a parent rates below **3 stars**, the system **requires** selection of a **quality-concern tag** from a predefined list. |
| BR-141 | Weekly feedback aggregation | The system **automatically compiles** an aggregated feedback chart **weekly** and sends a report to catering management and school leadership. |
| BR-142 | Illness-keyword detection in feedback | The system **automatically analyzes** parent feedback content, detecting **illness-related keywords** (e.g., "stomachache", "vomiting"). When detected, the system **triggers an urgent alert** and immediately notifies **Medical Staff** for review. |
| BR-143 | Caterer responses to feedback | **Service Caterers** may respond to parent feedback. |
| BR-144 | Announcement revision history | Every announcement update must create a **revision history entry** with timestamp and actor ID. |
| BR-145 | Anonymized aggregate feedback data | Feedback data is **anonymized and aggregated** into the school's dashboard. |
| BR-158 | Default quiet hours | By default, the system **does not send non-urgent push notifications** during **22:00–06:00**. Urgent alerts are **not affected** by quiet hours. |
| BR-159 | Retry and fallback on notification failure | When a push notification fails to send, the system **automatically retries**; after exhausting retries, it falls back to sending a **fallback email**. |

#### 14. AI & Support Services

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-156 | AI-based menu suggestion | The **Gemini AI Service** analyzes nutrition requirements, student allergy profiles, available inventory, and cost caps to **suggest an optimized menu**. AI output includes **nutrition analysis, ingredient list, and allergy warning flags**. |
| BR-157 | Mandatory AI conversation archiving | All conversations between users and the AI (including AI menu suggestions) must be **archived** for compliance and audit purposes. |
| BR-158b | Quiet hours configuration | Users may **configure quiet hours** according to personal preference. |
| BR-160 | Automatic database backup | The system performs a **full database backup** every **Sunday at 02:00 AM**. The backup file is **AES-256 encrypted** and replicated to **off-site cloud storage**. |
| BR-161 | Backup file protection | The backup file must be encrypted and stored separately from the primary infrastructure. |
| BR-162 | Immutable audit log | The audit trail must be **immutable** — no one may edit or delete audit records. |
| BR-163 | External integration connectivity check | When updating configuration for an external integration (GPS, Gmail, Firebase, Gemini AI), the system must **verify a successful connection** before saving the new configuration. |
| BR-164 | Encryption of integration credentials | All external service credentials (API keys, OAuth credentials) must be **encrypted** at rest. |
| BR-165 | Weekly system health report | The system **automatically compiles** a weekly system health and active-user report for System Admin. |

#### 15. Audit Log & Data Security

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-170 | Logging all significant actions | All significant actions in the system (create, update, delete, approve, reject) must be recorded in the **audit trail** with actor, timestamp, and affected object. |
| BR-171 | Log retention policy | The system must have a **Log Retention Policy** defining the minimum log retention period. |
| BR-172 | Log integrity protection | The system must have a mechanism to **protect the integrity** of logs, preventing any unauthorized modification. |
| BR-173 | Personal data protection (PDPD) | The system must comply with Vietnam's **Personal Data Protection Decree (PDPD)**. Children's (students') biometric data **must not be collected**, for personal data protection reasons. |
| BR-174 | Operational history traceability | The system must support **querying operational history** and tracking the audit trail over time. |

#### 16. Reporting & Analytics

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-175 | Role-based report access | Access to each report type is controlled by role (RBAC): **School Administration** — all operational reports of its school; **Medical Staff** — inspection, medical incident, and safety compliance reports; **Service Caterers** — only reports related to their own activity (receiving, inspection, incidents); **System Admin** — all reports system-wide. |
| BR-176 | School-scoped report data | Report data must be **scoped to the user's own school**. Only **System Admin** may view cross-school aggregate reports. |
| BR-177 | Report history retention | Generated reports must be retained for at least **12 months**. Every export (who, when, report type) must be logged in the **audit trail**. |
| BR-178 | Report export formats | The system supports export in two formats: **PDF** and **Excel (XLSX)**. Each export file must include: report title, data period, school name, and export timestamp. |
| BR-179 | Scheduled automated report export | The system supports **scheduling periodic report generation and delivery** (daily, weekly, monthly), sent to the email list configured by School Administration. On send failure, the system retries up to **3 times**; if still unsuccessful, it logs the error and notifies System Admin. |
| BR-180 | Real-time dashboard data | The Analytics Dashboard must display data refreshed within **5 minutes** of an operational change. Dashboard content includes: food safety risk indicators, inspection compliance rate, daily meal summary. |
| BR-181 | Report data security & anonymization | Reports must **not disclose personally identifiable information** of students in aggregated data. Reports exported outside the system must be marked **"Confidential"** if they contain sensitive operational or incident information. |
| BR-182 | Report data integrity & accuracy | Report data must be computed directly from the **original data source** in the system; manual editing after report generation is not permitted. Reports must clearly display the **generation date/time** and the **data period** covered. |

#### 17. Offline & Data Sync

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-196 | Offline evidence capture allowed | Evidence-only actions (no server-side verification needed to decide pass/fail) may be performed offline: photo + GPS + timestamp capture during three-step inspection (BR-083), ingredient receiving (BR-072), food sample retention (BR-107), and filling inspection checklists. Data is stored in a **local sync queue** on the device and automatically uploaded once connectivity is restored. |
| BR-197 | Real-time safety verification requires connectivity | The following actions **may not** be performed offline, even with stale cached data on the device: allergy alert on student ID scan (BR-123); supplier Approved-status check before receiving (BR-070); batch Quarantine-status check (BR-149); face biometric authentication (BR-051). When offline, the system **blocks the action** and displays a connectivity-required message — cached data must never be used to silently allow the action through. |
| BR-198 | Automatic incident aggregation requires server connectivity | Automatically triggering an incident investigation when 3+ students report similar symptoms within 24 hours (BR-146) requires real-time server-side aggregation — it cannot be performed or simulated offline on-device. |
| BR-199 | Sync conflict resolution | When two devices edit the same record while offline, the system applies **last-write-wins by server timestamp** (not device clock). Both writes must be recorded in the **immutable audit log** (BR-162) so no write is silently lost, including the overwritten one. |
| BR-200 | Offline cache validity window | Cached data usable offline (menus, class lists, student lists, etc.) is valid for at most a defined number of hours since the last successful sync. Once expired, the relevant screens show a **"Needs re-sync"** state and restrict new write actions until sync succeeds. |

#### Business Rules by Module (Summary Index)

| Module | Business Rules |
| --- | --- |
| User Management & Authentication | BR-001–006, BR-009–012 |
| RBAC | BR-007, BR-008, BR-013 |
| School & Class Management | BR-020, BR-021, BR-022, BR-024–026, BR-028 |
| Student & Parent Management | BR-030–032 |
| Supplier & Caterer Management | BR-040, BR-041, BR-046–048, BR-050, BR-051, BR-060 |
| Ingredient & Inventory Management | BR-070–078 |
| Menu & Diet Management | BR-061–069, BR-080–082 |
| Three-Step Food Inspection | BR-079, BR-083, BR-090–099 |
| Food Sample Retention | BR-105–114 |
| Meal Serving & Distribution | BR-120, BR-121, BR-123, BR-125, BR-130, BR-133, BR-135 |
| Food Incident Management | BR-146–155 |
| Food Safety Monitoring | BR-089, BR-100–104 |
| Feedback & Notification | BR-138–145, BR-158, BR-159 |
| AI & Support Services | BR-156, BR-157, BR-158b, BR-160–165 |
| Audit Log & Data Security | BR-170–174 |
| Reporting & Analytics | BR-175–182 |
| Offline & Data Sync | BR-196–200 |

### System Messages

#### 1. Error Messages

Error messages block the user's action and require a corrective step.

| MSG Code | Message Type | BR Reference | Content |
| --- | --- | --- | --- |
| MSG-001 | Error | — | System error while saving school data. Please try again. |
| MSG-002 | Error | — | Invalid credentials. Please check your email and password. |
| MSG-003 | Error | — | Your account has been deactivated. Please contact your administrator. |
| MSG-004 | Error | BR-004 | Session expired. Please log in again. |
| MSG-005 | Error | — | Access denied: You do not have permission to access this resource. |
| MSG-006 | Error | BR-012 | Registration failed: The email address is already in use. |
| MSG-007 | Error | — | Invalid tax ID format. Please verify and try again. |
| MSG-008 | Error | — | File upload failed: File size exceeds the maximum limit of 10MB. |
| MSG-009 | Error | — | File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed. |
| MSG-010 | Error | BR-025 | Classroom capacity exceeded. Maximum allowed is 50 students. |
| MSG-011 | Error | BR-020 | Homeroom teacher is already assigned to another classroom. |
| MSG-012 | Error | — | Student admission code is invalid or has already been used. |
| MSG-013 | Error | BR-046, BR-048 | Supplier food safety certificate has expired. Onboarding blocked. |
| MSG-014 | Error | BR-051 | Face recognition failed: Biometric identity could not be verified. |
| MSG-015 | Error | BR-074, BR-079 | Receiving action blocked: GPS location verification failed (out of tolerance). |
| MSG-016 | Error | BR-095 | Checklist validation failed: Mandatory safety checks are missing. |
| MSG-017 | Error | BR-090, BR-093 | Cooking temperature too low: Meal core temperature must reach 75°C. |
| MSG-018 | Error | BR-077 | Inventory shortage: Insufficient stock of ingredient [Ingredient Name]. |
| MSG-019 | Error | BR-107 | Food sample retention weight too low: Must be at least 100g. |
| MSG-020 | Error | BR-110 | Retention refrigerator temperature is out of safe range (2–8°C). |
| MSG-021 | Error | BR-130 | Duplicate meal distribution: Student has already scanned for this session. |
| MSG-022 | Error | BR-139 | Feedback submission blocked: Outside the 24-hour limit of meal serving. |
| MSG-023 | Error | — | Incident report incomplete: Symptoms, date of ingestion, and dishes are required. |
| MSG-024 | Error | BR-149 | Kitchen quarantine is active: Operation blocked due to safety violations. |
| MSG-025 | Error | — | Database transaction failed. Rollback executed. |
| MSG-075 | Error | BR-068 | Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week. |
| MSG-076 | Error | BR-002 | Account temporarily locked due to too many failed login attempts. Please try again after [Minutes] minutes. |
| MSG-077 | Error | BR-197 | Action blocked: Internet connection required to verify this safety check. Please reconnect and try again. |

#### 2. Success Messages

Confirmation messages indicating an action completed successfully.

| MSG Code | Message Type | Content |
| --- | --- | --- |
| MSG-026 | Success | Login successful. Welcome back, [Name]! |
| MSG-027 | Success | Logout successful. Session terminated safely. |
| MSG-028 | Success | Registration request submitted! A verification email has been sent. |
| MSG-029 | Success | Profile updated successfully. |
| MSG-030 | Success | Password changed successfully. |
| MSG-031 | Success | User account created successfully. |
| MSG-032 | Success | School registered successfully. Pending System Admin approval. |
| MSG-033 | Success | Classroom created successfully. |
| MSG-034 | Success | Homeroom teacher assigned successfully. |
| MSG-035 | Success | Student profiles and parent links imported successfully. |
| MSG-036 | Success | Allergy profile updated successfully and alerts set. |
| MSG-037 | Success | Supplier onboarding application approved successfully. |
| MSG-038 | Success | Delivery staff face biometrics registered successfully. |
| MSG-039 | Success | Menu draft submitted for approval successfully. |
| MSG-040 | Success | Menu approved and published successfully. Notifications sent to parents. |
| MSG-041 | Success | Ingredient delivery received. Inventory levels updated. |
| MSG-042 | Success | Inventory transaction recorded and stock adjusted. |
| MSG-043 | Success | Food safety inspection checklist submitted successfully. |
| MSG-044 | Success | Food sample retention registered. Cold storage timer initiated. |
| MSG-045 | Success | Meal distribution list confirmed for class [Class Name]. |
| MSG-046 | Success | Meal feedback submitted successfully. Thank you for your feedback! |
| MSG-047 | Success | Announcement published successfully to target audience. |
| MSG-048 | Success | Suspected food safety incident report logged. Investigation map generated. |
| MSG-049 | Success | Corrective actions plan submitted successfully for review. |
| MSG-050 | Success | System database backup completed successfully and uploaded to secure cloud. |
| MSG-071 | Success | Menu draft was saved successfully. |
| MSG-072 | Success | Special meal plan saved successfully. |
| MSG-074 | Success | Menu updated successfully. |
| MSG-078 | Success | Saved offline. Will sync automatically once connection is restored. |

#### 3. Warning Messages

Warnings that do not block the action but require attention or preventive follow-up.

| MSG Code | Message Type | BR Reference | Content |
| --- | --- | --- | --- |
| MSG-051 | Warning | BR-063 | Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students. |
| MSG-052 | Warning | BR-047, BR-083 | Ingredient batch [Batch ID] is close to expiration ([Expiration Date]). |
| MSG-053 | Warning | BR-047 | Supplier food safety certificate is expiring in [Number] days. |
| MSG-054 | Warning | BR-077 | Safety stock level reached for ingredient [Ingredient Name]. Reorder recommended. |
| MSG-055 | Warning | BR-089 | High temperature alert in inventory storage area: [Temperature]°C. |
| MSG-056 | Warning | BR-096, BR-098 | Re-inspection required: Failed safety check during inspection step [Step Name]. |
| MSG-057 | Warning | BR-140 | Negative meal feedback rating (<3 stars) received from parent. |
| MSG-058 | Warning | BR-142 | Illness keywords detected in parent feedback. Potential incident flag. |
| MSG-059 | Warning | BR-110, BR-089 | Retention refrigerator door left open or temperature fluctuation detected. |
| MSG-060 | Warning | — | Supplier monthly performance index score has dropped below 70%. |
| MSG-061 | Warning | — | System load threshold reached. Auto-scaling initiated. |
| MSG-062 | Warning | BR-002 | Suspicious login attempt detected from a new location/device. Verification code required. |
| MSG-063 | Warning | BR-006 | Weak password rating. Recommend adding special characters and numbers. |
| MSG-064 | Warning | BR-031 | Incomplete student health profiles detected for class [Class Name]. |
| MSG-065 | Warning | BR-135 | Delivery delay alert: Supplier delivery time deviates from schedule. |
| MSG-066 | Warning | — | Weekly sanitation audit report is overdue. Please submit immediately. |
| MSG-067 | Warning | BR-062 | AI menu analysis: Nutritional diversity is below the required 3-group minimum. |
| MSG-068 | Warning | BR-159 | Push notification delivery queue is backed up. Retrying failed targets. |
| MSG-069 | Warning | BR-163 | External API response latency is high (exceeds 2000ms). |
| MSG-070 | Warning | BR-160 | Backup space alert: Backup storage utilization is at 92% capacity. |
| MSG-073 | Warning | — | Gemini AI Service is currently unavailable. Please try again later. |
| MSG-079 | Warning | BR-200 | Cached data is outdated ([Number] hours old). Please reconnect to sync before continuing. |

#### 4. Placeholder Legend

| Placeholder | Description |
| --- | --- |
| `[Name]` | Full name of the logged-in user |
| `[Dish Name]` | Specific dish name |
| `[Allergen]` | Name of the allergen |
| `[Number]` | Count of affected students / number of days |
| `[Batch ID]` | Ingredient batch number |
| `[Expiration Date]` | Specific expiry date |
| `[Ingredient Name]` | Specific ingredient name |
| `[Temperature]` | Measured temperature value |
| `[Step Name]` | Inspection step name (Step 1/2/3) |
| `[Class Name]` | Class name |
| `[Minutes]` | Remaining lockout time in minutes |

#### 5. System Messages by Module (Summary Index)

| Module | MSG Codes |
| --- | --- |
| User Management & Authentication | MSG-002–006, MSG-026–031, MSG-062, MSG-063, MSG-076 |
| School & Class Management | MSG-001, MSG-007, MSG-010, MSG-011, MSG-032–034 |
| Student & Parent Management | MSG-012, MSG-035, MSG-036, MSG-064 |
| Supplier & Partner Management | MSG-013, MSG-014, MSG-037, MSG-038, MSG-053, MSG-060 |
| Ingredient & Inventory Management | MSG-008, MSG-009, MSG-015, MSG-018, MSG-041, MSG-042, MSG-052, MSG-054, MSG-055, MSG-065 |
| Menu & Diet Management | MSG-039, MSG-040, MSG-051, MSG-067, MSG-071–075 |
| Food Safety Inspection | MSG-016, MSG-017, MSG-043, MSG-056 |
| Food Sample Retention | MSG-019, MSG-020, MSG-044, MSG-059 |
| Meal Serving & Distribution | MSG-021, MSG-045 |
| Feedback & Announcement | MSG-022, MSG-046, MSG-047, MSG-057, MSG-058 |
| Incident Management | MSG-023, MSG-024, MSG-048, MSG-049 |
| AI & System Services | MSG-025, MSG-050, MSG-061, MSG-068–070, MSG-073 |
| Offline & Data Sync | MSG-077, MSG-078, MSG-079 |

