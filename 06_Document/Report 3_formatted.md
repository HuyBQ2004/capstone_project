**CAPSTONE PROJECT REPORT**

**Report 3 - Software Requirement Specification**

**School Food Safety Management Support System**

[1. Record of changes 11](#_Toc238240172)

[2. Definition 13](#_Toc238240173)

[2.1 Acronyms 13](#_Toc238240174)

[2.2 List of Figures and Tables 15](#_Toc238240175)

[2.2.1 List Figures 15](#_Toc238240176)

[2.2.2 List Table 18](#_Toc238240177)

[3. Software Requirement Specification 28](#_Toc238240178)

[3.1 Requirement Overview 28](#_Toc238240179)

[3.1.1 Context Diagram 28](#_Toc238240180)

[3.1.2 Main Workflows 30](#_Toc238240181)

[3.1.2.1 Supplier Registration & Approval Workflow 30](#_Toc238240182)

[3.1.2.2 Ingredient Receiving Workflow 33](#_Toc238240183)

[3.1.2.3 Ingredient Storage & Inventory Management Workflow 36](#_Toc238240184)

[3.1.2.4 Menu Planning & Approval Workflow 39](#_Toc238240185)

[3.1.2.5 Ingredient Issue to Kitchen Workflow 43](#_Toc238240186)

[3.1.2.6 Food Preparation Workflow 46](#_Toc238240187)

[3.1.2.7 Three-Step Food Inspection Workflow 49](#_Toc238240188)

[3.1.2.8 Food Sample Retention Workflow 51](#_Toc238240189)

[3.1.2.9 Allergy & Special Meal Management Workflow 54](#_Toc238240190)

[3.1.2.10 Meal Serving Workflow 57](#_Toc238240191)

[3.1.2.11 Food Safety Incident Handling Workflow 60](#_Toc238240192)

[3.1.2.12 Parent Meal Monitoring Workflow 64](#_Toc238240193)

[3.1.3 Generation Specification 67](#_Toc238240194)

[3.1.3.1 Actors 67](#_Toc238240195)

[3.1.3.2 Use Case Diagrams 68](#_Toc238240196)

[3.1.3.2.1 UCs for Guest 68](#_Toc238240197)

[3.1.3.2.2 UCs for User 69](#_Toc238240198)

[3.1.3.2.3 UCs for System Admin 70](#_Toc238240199)

[3.1.3.2.4 UCs for School Administration 71](#_Toc238240200)

[3.1.3.2.5 UCs for Teacher 72](#_Toc238240201)

[3.1.3.2.6 UCs for Parent 73](#_Toc238240202)

[3.1.3.2.7 UCs for Medical Staff 74](#_Toc238240203)

[3.1.3.2.8 UCs for Raw Food Supplier 75](#_Toc238240204)

[3.1.3.2.9 UCs for Service Caterers 76](#_Toc238240205)

[3.1.3.3 Use Cases Description 77](#_Toc238240206)

[3.2 Functional Specifications 106](#_Toc238240207)

[3.2.1 User Management & Access Control 106](#_Toc238240208)

[3.2.1.1 Activate/ Deactivate Staff 106](#_Toc238240209)

[3.2.1.2 Activate/Deactivate User 107](#_Toc238240210)

[3.2.1.3 Change Password 108](#_Toc238240211)

[3.2.1.4 Create Staff 110](#_Toc238240212)

[3.2.1.5 Create User 112](#_Toc238240213)

[3.2.1.6 Reset Password 114](#_Toc238240214)

[3.2.1.7 Login 116](#_Toc238240215)

[3.2.1.8 Logout 117](#_Toc238240216)

[3.2.1.9 Update Profile 118](#_Toc238240217)

[3.2.1.10 View Permission List 119](#_Toc238240218)

[3.2.1.11 View Role List 120](#_Toc238240219)

[3.2.1.12 View Profile 121](#_Toc238240220)

[3.2.1.13 View Staff Account List 122](#_Toc238240221)

[3.2.1.14 View User Account List 124](#_Toc238240222)

[3.2.1.15 Activate Account & Set Password 125](#_Toc238240223)

[3.2.2 School & Class Management 126](#_Toc238240224)

[3.2.2.1 Activate/Deactivate School 126](#_Toc238240225)

[3.2.2.2 Assign Homeroom Teacher 128](#_Toc238240226)

[3.2.2.3 Create Classroom 129](#_Toc238240227)

[3.2.2.4 View Classroom Detail 130](#_Toc238240228)

[3.2.2.5 View Classroom List 131](#_Toc238240229)

[3.2.2.6 Register School 133](#_Toc238240230)

[3.2.2.7 Update Classroom 134](#_Toc238240231)

[3.2.2.8 Update School Information 135](#_Toc238240232)

[3.2.2.9 View School Information 136](#_Toc238240233)

[3.2.2.10 View School List 137](#_Toc238240234)

[3.2.3 Student & Parent Management 138](#_Toc238240235)

[3.2.3.1 Bulk Import Student & Parent 138](#_Toc238240236)

[3.2.3.2 Create Student 140](#_Toc238240237)

[3.2.3.3 View Allergy & Special Meal List 141](#_Toc238240238)

[3.2.3.4 View Student Health Information 142](#_Toc238240239)

[3.2.3.5 View Student Health Record List 143](#_Toc238240240)

[3.2.3.6 Review Health Information 145](#_Toc238240241)

[3.2.3.7 Review Special Meal Requests 146](#_Toc238240242)

[3.2.3.8 Submit Health & Allergy Information 147](#_Toc238240243)

[3.2.3.9 Update Health & Allergy Information 148](#_Toc238240244)

[3.2.3.10 Update Health Records 150](#_Toc238240245)

[3.2.3.11 Update Student 151](#_Toc238240246)

[3.2.3.12 View Allergy Information 152](#_Toc238240247)

[3.2.3.13 View Student Health & Allergy Information 153](#_Toc238240248)

[3.2.3.14 View Student Health History 154](#_Toc238240249)

[3.2.3.15 View Student List 155](#_Toc238240250)

[3.2.4 Supplier & Partner Management 156](#_Toc238240251)

[3.2.4.1 Add Delivery Person 156](#_Toc238240252)

[3.2.4.2 Delete Face Data 158](#_Toc238240253)

[3.2.4.3 Manage Compliance Documents 159](#_Toc238240254)

[3.2.4.4 Manage Profile 161](#_Toc238240255)

[3.2.4.5 Register Service Caterer Account 162](#_Toc238240256)

[3.2.4.6 Register Face Data 163](#_Toc238240257)

[3.2.4.7 Register Supplier Account 164](#_Toc238240258)

[3.2.4.8 Update Compliance Documents 166](#_Toc238240259)

[3.2.4.9 Upload Food Safety Certificates 167](#_Toc238240260)

[3.2.4.10 Upload Product Origin Documents 168](#_Toc238240261)

[3.2.4.11 View Delivery Person List 170](#_Toc238240262)

[3.2.4.12 View Service Caterer List 171](#_Toc238240263)

[3.2.4.13 View Supplier List 172](#_Toc238240264)

[3.2.4.14 View Supplier Detail 173](#_Toc238240265)

[3.2.4.15 Submit Supplier Application 175](#_Toc238240266)

[3.2.4.16 Track Supplier Application Status 176](#_Toc238240267)

[3.2.4.17 Update/Resubmit Supplier Application 177](#_Toc238240268)

[3.2.4.18 Manage Supplier Application 178](#_Toc238240269)

[3.2.4.19 Submit Catering Application 180](#_Toc238240270)

[3.2.4.20 Track Catering Application Status 181](#_Toc238240271)

[3.2.4.21 Update/Resubmit Catering Application 182](#_Toc238240272)

[3.2.4.22 Manage Catering Application 183](#_Toc238240273)

[3.2.4.23 Add Supplier 184](#_Toc238240274)

[3.2.4.24 View Catering Company Detail 186](#_Toc238240275)

[3.2.4.25 Add Service Caterer 187](#_Toc238240276)

[3.2.5 Menu & Diet Management 188](#_Toc238240277)

[3.2.5.1 Approve/Reject Menu 188](#_Toc238240278)

[3.2.5.2 Create Menu 189](#_Toc238240279)

[3.2.5.3 Create Special Meal Plan 191](#_Toc238240280)

[3.2.5.4 Manage Menus 192](#_Toc238240281)

[3.2.5.5 Request AI Menu Recommendations 194](#_Toc238240282)

[3.2.5.6 Submit Menu For Approval 195](#_Toc238240283)

[3.2.5.7 Update Menu 196](#_Toc238240284)

[3.2.5.8 View Menu List 198](#_Toc238240285)

[3.2.5.9 View Menu Detail 199](#_Toc238240286)

[3.2.5.10 View Menu Information 200](#_Toc238240287)

[3.2.6 Ingredient & Inventory Management 202](#_Toc238240288)

[3.2.6.1 Confirm Delivery Schedule 202](#_Toc238240289)

[3.2.6.2 Create Purchase Request 203](#_Toc238240290)

[3.2.6.3 Manage Deliveries 204](#_Toc238240291)

[3.2.6.4 Manage Ingredient Batches 205](#_Toc238240292)

[3.2.6.5 Manage Ingredient Receiving 207](#_Toc238240293)

[3.2.6.6 Manage Inventory 208](#_Toc238240294)

[3.2.6.7 Record Ingredient Receiving 209](#_Toc238240295)

[3.2.6.8 Record Ingredient Storage 211](#_Toc238240296)

[3.2.6.9 Record Inventory Transactions 212](#_Toc238240297)

[3.2.6.10 Reject Ingredient Batch 213](#_Toc238240298)

[3.2.6.11 Submit Batch Information 214](#_Toc238240299)

[3.2.6.12 Submit Delivery Information 215](#_Toc238240300)

[3.2.6.13 Update Delivery Status 216](#_Toc238240301)

[3.2.6.14 Upload Batch Certificates 217](#_Toc238240302)

[3.2.6.15 View Batch History 218](#_Toc238240303)

[3.2.6.16 View Delivery Batch Records 219](#_Toc238240304)

[3.2.6.17 View Ingredient Receiving Records 220](#_Toc238240305)

[3.2.6.18 View Purchase Requests 221](#_Toc238240306)

[3.2.7 Food Safety Inspection & Monitoring 222](#_Toc238240307)

[3.2.7.1 Monitor Daily Food Safety 222](#_Toc238240308)

[3.2.7.2 Conduct Three-Step Food Inspection 224](#_Toc238240309)

[3.2.7.3 Monitor Food Safety 225](#_Toc238240310)

[3.2.7.4 View Food Inspection Records 227](#_Toc238240311)

[3.2.7.5 View Food Inspection Reports 228](#_Toc238240312)

[3.2.7.6 View Food Safety Compliance Report 229](#_Toc238240313)

[3.2.7.7 View Food Safety Reports 230](#_Toc238240314)

[3.2.7.8 View Inspection Records 232](#_Toc238240315)

[3.2.7.9 Record The Daily Inspection Checklist 233](#_Toc238240316)

[3.2.8 Food Sample Retention 234](#_Toc238240317)

[3.2.8.1 Dispose Food Sample 234](#_Toc238240318)

[3.2.8.2 Manage Food Sample Retention 235](#_Toc238240319)

[3.2.8.3 Register Food Sample 237](#_Toc238240320)

[3.2.8.4 Retrieve Food Sample Records 239](#_Toc238240321)

[3.2.8.5 View Food Sample Records 241](#_Toc238240322)

[3.2.9 Meal Serving & Distribution 242](#_Toc238240323)

[3.2.9.1 Confirm Meal Distribution 242](#_Toc238240324)

[3.2.9.2 Manage Meal Service 243](#_Toc238240325)

[3.2.9.3 Monitor Child Meal History 245](#_Toc238240326)

[3.2.9.4 Monitor School Meals 246](#_Toc238240327)

[3.2.9.5 Monitor Student Meal History 247](#_Toc238240328)

[3.2.9.6 Record Student Meal Consumption 248](#_Toc238240329)

[3.2.9.7 View Allergy Alerts 250](#_Toc238240330)

[3.2.9.8 View Meal Distribution List 251](#_Toc238240331)

[3.2.9.9 View Meal History 252](#_Toc238240332)

[3.2.9.10 View Special Meal Arrangements 253](#_Toc238240333)

[3.2.9.11 View Special Meal Records 255](#_Toc238240334)

[3.2.9.12 View Special Meal Requirements 256](#_Toc238240335)

[3.2.10 Feedback & Announcement Management 257](#_Toc238240336)

[3.2.10.1 Create Announcement 257](#_Toc238240337)

[3.2.10.2 Manage Meal Feedback 259](#_Toc238240338)

[3.2.10.3 Submit Meal Feedback 260](#_Toc238240339)

[3.2.10.4 View Meal Feedback List 261](#_Toc238240340)

[3.2.10.5 View Meal Feedback Dashboard 263](#_Toc238240341)

[3.2.10.6 Update Announcement 264](#_Toc238240342)

[3.2.10.7 View Announcement Detail 265](#_Toc238240343)

[3.2.10.8 View Announcement List 266](#_Toc238240344)

[3.2.10.9 View Announcement List 267](#_Toc238240345)

[3.2.11 Incident & Emergency Management 268](#_Toc238240346)

[3.2.11.1 Assign Incident Severity 268](#_Toc238240347)

[3.2.11.2 Export Incident Reports 270](#_Toc238240348)

[3.2.11.3 Generate Incident Report 271](#_Toc238240349)

[3.2.11.4 Manage Food Safety Incidents 272](#_Toc238240350)

[3.2.11.5 Report Food Safety Concern 273](#_Toc238240351)

[3.2.11.6 Report Food Safety Incident 275](#_Toc238240352)

[3.2.11.7 View Food Poisoning Incident List 276](#_Toc238240353)

[3.2.11.8 View Food Safety Incident Detail 277](#_Toc238240354)

[3.2.11.9 Record Violation 278](#_Toc238240355)

[3.2.12 AI & Supporting Services 280](#_Toc238240356)

[3.2.12.1 Chat with AI 280](#_Toc238240357)

[3.2.12.2 Manage External Integrations 281](#_Toc238240358)

[3.2.12.3 Monitor System Activities 282](#_Toc238240359)

[3.2.12.4 Receive Notification 283](#_Toc238240360)

[3.2.12.5 Scan Face Data 285](#_Toc238240361)

[3.2.12.6 View Audit Logs 286](#_Toc238240362)

[3.2.12.7 View Error Logs 287](#_Toc238240363)

[3.2.12.8 View Notification History 288](#_Toc238240364)

[3.2.12.9 View System Status 289](#_Toc238240365)

[3.2.12.10 View Usage Statistics 291](#_Toc238240366)

[3.3 Functional Requirements 292](#_Toc238240367)

[3.3.1 System Functional Overview 292](#_Toc238240368)

[3.3.1.1 Screens Flow for Guest and Common Screen 292](#_Toc238240369)

[3.3.1.2 Screens Flow for System Admin 293](#_Toc238240370)

[3.3.1.3 Screens Flow for School Admin 294](#_Toc238240371)

[3.3.1.4 Screens Flow for Medical Staff 295](#_Toc238240372)

[3.3.1.5 Screens Flow for Service Caterers 296](#_Toc238240373)

[3.3.1.6 Screens Flow for Parent 297](#_Toc238240374)

[3.3.1.7 Screens Flow for Raw Food Supplier 298](#_Toc238240375)

[3.3.1.8 Screens Flow for Teacher 299](#_Toc238240376)

[3.3.2 Screen Authorization 300](#_Toc238240377)

[3.3.3 Non-screen Functions 316](#_Toc238240378)

[Mobile Screen 326](#_Toc238240379)

[Guest and Common Screen 326](#_Toc238240380)

[3.3.1.1.1 Screen for Guest Portal 326](#_Toc238240381)

[3.3.1.1.2 Screen for Register School 328](#_Toc238240382)

[3.3.1.1.3 Screen for Register Supplier 330](#_Toc238240383)

[3.3.1.1.4 Screen for Register Service Caterers 332](#_Toc238240384)

[3.3.1.1.5 Screen for Login 334](#_Toc238240385)

[3.3.1.1.6 Screen for Reset Password 335](#_Toc238240386)

[3.3.1.1.7 Screen for Verify OTP 337](#_Toc238240387)

[3.3.1.1.8 Screen for Reset Password 339](#_Toc238240388)

[3.3.1.1.9 Screen for Personal Profile 340](#_Toc238240389)

[3.3.1.1.10 Screen for Edit Profile 342](#_Toc238240390)

[3.3.1.1.11 Screen for Change Password 343](#_Toc238240391)

[3.3.1.1.12 Screen for Notification Settings 345](#_Toc238240392)

[3.3.1.1.13 Screen for Notification History 346](#_Toc238240393)

[3.3.1.1.14 Screen for AI Chatbot 348](#_Toc238240394)

[3.3.1.1.15 Screen for AI Chat History 350](#_Toc238240395)

[Service Caterers Screens 351](#_Toc238240396)

[3.3.1.5.1 Screen for Catering Dashboard 351](#_Toc238240397)

[3.3.1.5.2 Screen for Receiving Dashboard 353](#_Toc238240398)

[3.3.1.5.3 Screen for Receive Ingredients 354](#_Toc238240399)

[3.3.1.5.4 Screen for Verify Supplier Delivery 356](#_Toc238240400)

[3.3.1.5.5 Screen for Inspect Ingredient Quality 357](#_Toc238240401)

[3.3.1.5.6 Screen for Capture Receiving Evidence 359](#_Toc238240402)

[3.3.1.5.7 Screen for Reject Ingredient Batch 360](#_Toc238240403)

[3.3.1.5.8 Screen for Store Ingredients 362](#_Toc238240404)

[3.3.1.5.9 Screen for Monitor Expiration Dates 364](#_Toc238240405)

[3.3.1.5.10 Screen for Issue Ingredients 365](#_Toc238240406)

[3.3.1.5.11 Screen for Create Purchase Request 367](#_Toc238240407)

[3.3.1.5.12 Screen for Update Menu 368](#_Toc238240408)

[3.3.1.5.13 Screen for View Menu Recommendations 370](#_Toc238240409)

[3.3.1.5.14 Screen for Special Meals Configuration 371](#_Toc238240410)

[3.3.1.5.15 Screen for Prepare Ingredients 373](#_Toc238240411)

[3.3.1.5.16 Screen for Record Cooking Information 374](#_Toc238240412)

[Medical Staff Screens 376](#_Toc238240413)

[Screens Medical Staff Dashboard 376](#_Toc238240414)

[3.3.1.4.2 Screens Health Record For Medical Staff 378](#_Toc238240415)

[3.3.1.4.3 Screens Special Meal For Student 380](#_Toc238240416)

[3.3.1.4.4 Screens Incidents Records 383](#_Toc238240417)

[3.3.1.4.5 Screens Incidents Records Detail 386](#_Toc238240418)

[3.3.1.4.6 Screens Evaluate the level Incidents 389](#_Toc238240419)

[3.3.1.4.7 Screens Investigation Space 391](#_Toc238240420)

[3.3.1.4.8 Screens Export To School Administration 393](#_Toc238240421)

[3.3.1.4.9 Screens Extraction of Stored Food Samples 396](#_Toc238240422)

[3.3.1.4.10 Screens Collect Evidence 399](#_Toc238240423)

[3.3.1.4.11 Screens Treatment Measures 402](#_Toc238240424)

[Parent Screens 405](#_Toc238240425)

[Parent Dashboard 405](#_Toc238240426)

[Child Selection 407](#_Toc238240427)

[Student Health Hub 409](#_Toc238240428)

[Update Health 412](#_Toc238240429)

[Add/Edit Allergy 414](#_Toc238240430)

[Weekly Menu 416](#_Toc238240431)

[Dish Details 418](#_Toc238240432)

[Allergy Conflict Alert 420](#_Toc238240433)

[Notifications 422](#_Toc238240434)

[Meal Feedback 424](#_Toc238240435)

[Report Safety Issue 426](#_Toc238240436)

[Meal History Log 428](#_Toc238240437)

[Raw Food Supplier Screen 430](#_Toc238240438)

[3.3.1.7.1 Supplier Dashboard 430](#_Toc238240439)

[3.3.1.7.2 Partnership Detail 432](#_Toc238240440)

[3.3.1.7.3 Purchase Request Screen 434](#_Toc238240441)

[3.3.1.7.4 Purchase Request Screen Detail 435](#_Toc238240442)

[3.3.1.7.5 Compliance Docs Screens 437](#_Toc238240443)

[3.3.1.7.6 Ingredient Batches Screens 438](#_Toc238240444)

[3.3.1.7.7 Supplier Upload Certificates 440](#_Toc238240445)

[Teacher Screens 442](#_Toc238240446)

[Teacher Dashboard 442](#_Toc238240447)

[Meal Distribution 444](#_Toc238240448)

[Student List in Class 446](#_Toc238240449)

[Health Incident Report 448](#_Toc238240450)

[Incident History 450](#_Toc238240451)

[3.4 Non-Functional Requirements 452](#_Toc238240452)

[3.4.1 Performance Requirements 452](#_Toc238240453)

[3.4.2 Reliability Requirements 452](#_Toc238240454)

[3.4.3 Security Requirements 452](#_Toc238240455)

[3.4.4 Usability Requirements 452](#_Toc238240456)

[3.4.5 Data Requirements 453](#_Toc238240457)

[3.4.6 Operational Constraints 453](#_Toc238240458)

[3.5 Requirement Appendix 454](#_Toc238240459)

[3.5.1 Business Rules 454](#_Toc238240460)

[3.5.2 System Messages 480](#_Toc238240461)

# 1. Record of changes

|  |  |  |  |
| --- | --- | --- | --- |
| Date | A\*, M, D | In charge | Change Description |
| 05/06/2026 | A | NamNVHE180863 | Init Document |
| 14/06/2026 | M | HuyBQHE180599 | Add use case description |
| 14/06/2026 | M | MinhTTHE186258 | Modify use case description |
| 22/06/2026 | M | NamNVHE180863 | Fix document format. |
| 02/08/2026 | M | NamNVHE180863 | Update description. |
| 22/08/2026 | M | MinhTTHE186258 | Update description |

**Table 1.1: Record of changes**

# 2. Definition

## 2.1 Acronyms

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
| BYT | B? Y t? - The Ministry of Health of Vietnam, the governmental authority responsible for health regulations and food safety guidelines. |
| CAPA | Corrective and Preventive Action - A systematic process to investigate, rectify, and prevent food safety incidents or system issues. |
| AI | Artificial Intelligence - Technology used in the system for menu recommendation and nutritional analysis. |
| AES | Advanced Encryption Standard - A symmetric-key block cipher algorithm used to secure sensitive data. |
| SMTP | Simple Mail Transfer Protocol - A communication protocol for electronic mail transmission. |
| TLS | Transport Layer Security - A cryptographic protocol designed to provide communications security over a computer network. |

**Table 2.1: Acronyms**

## 2.2 List of Figures and Tables

### 2.2.1 List Figures

[**Figure 3.1: Context Diagram** 18](#_Toc238240471)

[**Figure 3.2: Supplier Registration & Approval Workflow** 20](#_Toc238240472)

[**Figure 3.3: Ingredient Receiving Workflow** 23](#_Toc238240473)

[**Figure 3.4: Ingredient Storage & Inventory Management Workflow** 26](#_Toc238240474)

[**Figure 3.5: Menu Planning & Approval Workflow** 29](#_Toc238240475)

[**Figure 3.6: Ingredient Issue to Kitchen Workflow** 33](#_Toc238240476)

[**Figure 3.7: Food Preparation Workflow** 36](#_Toc238240477)

[**Figure 3.8: Three-Step Food Inspection Workflow** 39](#_Toc238240478)

[**Figure 3.9: Food Sample Retention Workflow** 41](#_Toc238240479)

[**Figure 3.10: Allergy & Special Meal Management Workflow** 44](#_Toc238240480)

[**Figure 3.11: Meal Serving Workflow** 47](#_Toc238240481)

[**Figure 3.12: Food Safety Incident Handling Workflow** 50](#_Toc238240482)

[**Figure 3.13: Parent Meal Monitoring Workflow** 54](#_Toc238240483)

[**Figure 3.14: UCs for Guest** 58](#_Toc238240484)

[**Figure 3.15: UCs for User** 59](#_Toc238240485)

[**Figure 3.16: UCs for System Admin** 60](#_Toc238240486)

[**Figure 3.17: UCs for School Administration** 62](#_Toc238240487)

[**Figure 3.18: UCs for Teacher** 62](#_Toc238240488)

[**Figure 3.19: UCs for Parent** 64](#_Toc238240489)

[**Figure 3.20: UCs for Medical Staff** 64](#_Toc238240490)

[**Figure 3.21: UCs for Raw Food Supplier** 65](#_Toc238240491)

[**Figure 3.22: UCs for Service Caterers** 67](#_Toc238240492)

[**Figure 3.23: Screens Flow for Guest and Common Screen** 282](#_Toc238240493)

[**Figure 3.24: Screens Flow for System Admin** 283](#_Toc238240494)

[**Figure 3.25: Screens Flow for School Admin** 284](#_Toc238240495)

[**Figure 3.26: Screens Flow for Medical Staff** 285](#_Toc238240496)

[**Figure 3.27: Screens Flow for Service Caterers** 286](#_Toc238240497)

[**Figure 3.28: Screens Flow for Parent** 287](#_Toc238240498)

[**Figure 3.29: Screens Flow for Raw Food Supplier** 288](#_Toc238240499)

[**Figure 3.30: Screens Flow for Teacher** 289](#_Toc238240500)

[**Figure 3.31: Screen for Guest Portal** 316](#_Toc238240501)

[**Figure 3.32: Screen for Register School** 318](#_Toc238240502)

[**Figure 3.33: Screen for Register Supplier** 320](#_Toc238240503)

[**Figure 3.34: Screen for Register Service Caterers** 322](#_Toc238240504)

[**Figure 3.35: Screen for Login** 324](#_Toc238240505)

[**Figure 3.36: Screen for Reset Password** 325](#_Toc238240506)

[**Figure 3.37: Screen for Verify OTP** 327](#_Toc238240507)

[**Figure 3.38: Screen for Reset Password** 329](#_Toc238240508)

[**Figure 3.39: Screen for Personal Profile** 330](#_Toc238240509)

[**Figure 3.40: Screen for Edit Profile** 332](#_Toc238240510)

[**Figure 3.41: Screen for Change Password** 334](#_Toc238240511)

[**Figure 3.42: Screen for Notification Settings** 335](#_Toc238240512)

[**Figure 3.43: Screen for Notification History** 337](#_Toc238240513)

[**Figure 3.44: Screen for AI Chatbot** 338](#_Toc238240514)

[**Figure 3.45: Screen for AI Chat History** 340](#_Toc238240515)

[**Figure 3.46: Screen for Catering Dashboard** 341](#_Toc238240516)

[**Figure 3.47: Screen for Receiving Dashboard** 343](#_Toc238240517)

[**Figure 3.48: Screen for Receive Ingredients** 344](#_Toc238240518)

[**Figure 3.49: Screen for Verify Supplier Delivery** 346](#_Toc238240519)

[**Figure 3.50: Screen for Inspect Ingredient Quality** 347](#_Toc238240520)

[**Figure 3.51: Screen for Capture Receiving Evidence** 349](#_Toc238240521)

[**Figure 3.52: Screen for Reject Ingredient Batch** 350](#_Toc238240522)

[**Figure 3.53: Screen for Store Ingredients** 352](#_Toc238240523)

[**Figure 3.54: Screen for Monitor Expiration Dates** 354](#_Toc238240524)

[**Figure 3.55: Screen for Issue Ingredients** 356](#_Toc238240525)

[**Figure 3.56: Screen for Create Purchase Request** 357](#_Toc238240526)

[**Figure 3.57: Screen for Update Menu** 359](#_Toc238240527)

[**Figure 3.58: Screen for View Menu Recommendations** 360](#_Toc238240528)

[**Figure 3.59: Screen for Special Meals Configuration** 361](#_Toc238240529)

[**Figure 3.60: Screen for Prepare Ingredients** 363](#_Toc238240530)

[**Figure 3.61: Screen for Record Cooking Information** 364](#_Toc238240531)

[**Figure 3.62: Screens Medical Staff Dashboard** 366](#_Toc238240532)

[**Figure 3.63: Screens Health Record For Medical Staff** 368](#_Toc238240533)

[**Figure 3.64: Screens Special Meal For Student** 370](#_Toc238240534)

[**Figure 3.65: Screens Incidents Records** 373](#_Toc238240535)

[**Figure 3.66: Screens Incidents Records Detail** 376](#_Toc238240536)

[**Figure 3.67: Screens Evaluate the level Incidents** 379](#_Toc238240537)

[**Figure 3.68: Screens Investigation Space** 381](#_Toc238240538)

[**Figure 3.69: Screens Export To School Administration** 383](#_Toc238240539)

[**Figure 3.70: Screens Extraction of Stored Food Samples** 386](#_Toc238240540)

[**Figure 3.71: Screens Collect Evidence** 389](#_Toc238240541)

[**Figure 3.72: Screens Treatment Measures** 392](#_Toc238240542)

[**Figure 3.73: Parent Dashboard Screen** 395](#_Toc238240543)

[**Figure 3.74: Child Selection Screen** 397](#_Toc238240544)

[**Figure 3.75: Student Health Hub Screen** 399](#_Toc238240545)

[**Figure 3.76: Update Health Screen** 402](#_Toc238240546)

[**Figure 3.77: Add/Edit Allergy Screen** 404](#_Toc238240547)

[**Figure 3.78: Weekly Menu Screen** 406](#_Toc238240548)

[**Figure 3.79: Dish Details Screen** 408](#_Toc238240549)

[**Figure 3.80: Allergy Conflict Alert Screen** 410](#_Toc238240550)

[**Figure 3.81: Notifications Screen** 412](#_Toc238240551)

[**Figure 3.82: Meal Feedback Screen** 414](#_Toc238240552)

[**Figure 3.83: Report Safety Issue Screen** 416](#_Toc238240553)

[**Figure 3.84: Meal History Log** 418](#_Toc238240554)

[**Figure 3.85: Supplier Dashboard** 420](#_Toc238240555)

[**Figure 3.86: Partnership Detail** 422](#_Toc238240556)

[**Figure 3.87: Purchase Request Screen** 424](#_Toc238240557)

[**Figure 3.88: Purchase Request Screen Detail** 425](#_Toc238240558)

[**Figure 3.89: Compliance Docs Screens** 427](#_Toc238240559)

[**Figure 3.90: Ingredient Batches Screens** 428](#_Toc238240560)

[**Figure 3.91: Ingredient Batches Screens** 430](#_Toc238240561)

[**Figure 3.92: Teacher Dashboard Screen** 432](#_Toc238240562)

[**Figure 3.93: Meal Distribution Screen** 434](#_Toc238240563)

[**Figure 3.94: Student List in Class Screen** 436](#_Toc238240564)

[**Figure 3.95: Health Incident Report Screen** 438](#_Toc238240565)

[**Figure 3.96: Incident History Screen** 440](#_Toc238240566)

### 2.2.2 List Table

[**Table 1.1: Record of changes** 11](#_Toc238240567)

[**Table 2.1: Acronyms** 14](#_Toc238240568)

[**Table 3.1: Context Diagram Description** 25](#_Toc238240569)

[**Table 3.2: Supplier Registration & Approval Workflow** 28](#_Toc238240570)

[**Table 3.3: Ingredient Receiving Workflow** 31](#_Toc238240571)

[**Table 3.4: Ingredient Storage & Inventory Management Workflow** 34](#_Toc238240572)

[**Table 3.5: Menu Planning & Approval Workflow** 38](#_Toc238240573)

[**Table 3.6: Ingredient Issue to Kitchen Workflow** 41](#_Toc238240574)

[**Table 3.7: Food Preparation Workflow** 44](#_Toc238240575)

[**Table 3.8: Three-Step Food Inspection Workflow** 47](#_Toc238240576)

[**Table 3.9: Food Sample Retention Workflow** 49](#_Toc238240577)

[**Table 3.10: Allergy & Special Meal Management Workflow** 52](#_Toc238240578)

[**Table 3.11: Meal Serving Workflow** 55](#_Toc238240579)

[**Table 3.12: Food Safety Incident Handling Workflow** 59](#_Toc238240580)

[**Table 3.13: Parent Meal Monitoring Workflow** 62](#_Toc238240581)

[**Table 3.14: Actors List** 63](#_Toc238240582)

[**Table 3.15: Use Cases Description** 101](#_Toc238240583)

[**Table 3.16: Activate/Deactivate Staff Use Cases Specification** 103](#_Toc238240584)

[**Table 3.17: Activate/Deactivate User Use Cases Specification** 104](#_Toc238240585)

[**Table 3.18: Change Password Use Cases Specification** 105](#_Toc238240586)

[**Table 3.19: Create Staff Use Cases Specification** 107](#_Toc238240587)

[**Table 3.20: Create User Use Cases Specification** 109](#_Toc238240588)

[**Table 3.21: Forget Password Use Cases Specification** 111](#_Toc238240589)

[**Table 3.22: Login Use Cases Specification** 113](#_Toc238240590)

[**Table 3.23: Logout Use Cases Specification** 114](#_Toc238240591)

[**Table 3.24: Update Profile Use Cases Specification** 115](#_Toc238240592)

[**Table 3.25: View List Permission Use Cases Specification** 116](#_Toc238240593)

[**Table 3.26: View List Role Use Cases Specification** 117](#_Toc238240594)

[**Table 3.27: View Profile Use Cases Specification** 118](#_Toc238240595)

[**Table 3.28: View Staff Account List Use Cases Specification** 119](#_Toc238240596)

[**Table 3.29: View User Account List Use Cases Specification** 121](#_Toc238240597)

[**Table 3.30: Activate Account & Set Password** 122](#_Toc238240598)

[**Table 3.31: Activate/Deactivate School Use Cases Specification** 123](#_Toc238240599)

[**Table 3.32: Assign Homeroom Teacher Use Cases Specification** 125](#_Toc238240600)

[**Table 3.33: Create Classroom Use Cases Specification** 126](#_Toc238240601)

[**Table 3.34: View Classroom List Use Cases Specification** 127](#_Toc238240602)

[**Table 3.35: View Classroom Detail Use Cases Specification** 128](#_Toc238240603)

[**Table 3.36: Register School Use Cases Specification** 130](#_Toc238240604)

[**Table 3.37: Update Classroom Use Cases Specification** 131](#_Toc238240605)

[**Table 3.38: Update School Information Use Cases Specification** 132](#_Toc238240606)

[**Table 3.39: View School Information Use Cases Specification** 133](#_Toc238240607)

[**Table 3.40: View School List Use Cases Specification** 134](#_Toc238240608)

[**Table 3.41: Bulk Import Student & Parent Use Cases Specification** 136](#_Toc238240609)

[**Table 3.42: Create Student Use Cases Specification** 137](#_Toc238240610)

[**Table 3.43: View Allergy & Special Meal List Use Cases Specification** 138](#_Toc238240611)

[**Table 3.44: View Student Health Information Use Cases Specification** 139](#_Toc238240612)

[**Table 3.45: View Student Health Record List Use Cases Specification** 141](#_Toc238240613)

[**Table 3.46: Review Health Information Use Cases Specification** 142](#_Toc238240614)

[**Table 3.47: Review Special Meal Requests Use Cases Specification** 143](#_Toc238240615)

[**Table 3.48: Submit Health & Allergy Information Use Cases Specification** 144](#_Toc238240616)

[**Table 3.49: Update Health & Allergy Information Use Cases Specification** 145](#_Toc238240617)

[**Table 3.50: Update Health Records Use Cases Specification** 147](#_Toc238240618)

[**Table 3.51: Update Student Use Cases Specification** 148](#_Toc238240619)

[**Table 3.52: View Allergy Information Use Cases Specification** 149](#_Toc238240620)

[**Table 3.53: View Student Health & Allergy Information Use Cases Specification** 150](#_Toc238240621)

[**Table 3.54: View Student Health History Use Cases Specification** 151](#_Toc238240622)

[**Table 3.55: View Student List Use Cases Specification** 152](#_Toc238240623)

[**Table 3.56: Add Delivery Person Use Cases Specification** 154](#_Toc238240624)

[**Table 3.57: Delete Face Data Use Cases Specification** 155](#_Toc238240625)

[**Table 3.58: Manage Compliance Documents Use Cases Specification** 156](#_Toc238240626)

[**Table 3.59: Manage Profile Use Cases Specification** 158](#_Toc238240627)

[**Table 3.60: Register Service Caterer Account Use Cases Specification** 159](#_Toc238240628)

[**Table 3.61: Register Face Data Use Cases Specification** 160](#_Toc238240629)

[**Table 3.62: Register Supplier Account Use Cases Specification** 162](#_Toc238240630)

[**Table 3.63: Update Compliance Documents Use Cases Specification** 163](#_Toc238240631)

[**Table 3.64: Upload Food Safety Certificates Use Cases Specification** 164](#_Toc238240632)

[**Table 3.65: Upload Product Origin Documents Use Cases Specification** 166](#_Toc238240633)

[**Table 3.66: View Delivery Person List Use Cases Specification** 167](#_Toc238240634)

[**Table 3.67: View Service Caterer List Use Cases Specification** 168](#_Toc238240635)

[**Table 3.68: View Supplier List Use Cases Specification** 169](#_Toc238240636)

[**Table 3.69: View Supplier Detail Use Cases Specification** 171](#_Toc238240637)

[**Table 3.70: Submit Supplier Application Use Cases Specification** 172](#_Toc238240638)

[**Table 3.71: Track Supplier Application Status** 173](#_Toc238240639)

[**Table 3.72: Update/Resubmit Supplier Application** 174](#_Toc238240640)

[**Table 3.73: Manage Supplier Application** 175](#_Toc238240641)

[**Table 3.74: Submit Catering Application** 177](#_Toc238240642)

[**Table 3.75: Track Catering Application Status** 178](#_Toc238240643)

[**Table 3.76: Update/Resubmit Catering Application** 179](#_Toc238240644)

[**Table 3.77: Manage Catering Application** 180](#_Toc238240645)

[**Table 3.78: Add Supplier** 182](#_Toc238240646)

[**Table 3.79: Table: Add Supplier Use Cases Specification** 182](#_Toc238240647)

[**Table 3.80: View Catering Company Detail** 183](#_Toc238240648)

[**Table 3.81: Table: View Catering Company Detail Use Cases Specification** 183](#_Toc238240649)

[**Table 3.82: Add Service Caterer** 184](#_Toc238240650)

[**Table 3.83: Table: Add Service Caterer Use Cases Specification** 184](#_Toc238240651)

[**Table 3.84: Approve/Reject Menu Use Cases Specification** 185](#_Toc238240652)

[**Table 3.85: Create Menu Use Cases Specification** 187](#_Toc238240653)

[**Table 3.86: Create Special Meal Plan Use Cases Specification** 188](#_Toc238240654)

[**Table 3.87: Manage Menus Use Cases Specification** 190](#_Toc238240655)

[**Table 3.88: Request AI Menu Recommendations Use Cases Specification** 191](#_Toc238240656)

[**Table 3.89: Submit Menu For Approval Use Cases Specification** 192](#_Toc238240657)

[**Table 3.90: Update Menu Use Cases Specification** 194](#_Toc238240658)

[**Table 3.91: View Menu List Use Cases Specification** 195](#_Toc238240659)

[**Table 3.92: View Menu Detail Use Cases Specification** 196](#_Toc238240660)

[**Table 3.93: View Menu Information Use Cases Specification** 198](#_Toc238240661)

[**Table 3.94: Confirm Delivery Schedule Use Cases Specification** 199](#_Toc238240662)

[**Table 3.95: Create Purchase Request Use Cases Specification** 200](#_Toc238240663)

[**Table 3.96: Manage Deliveries Use Cases Specification** 201](#_Toc238240664)

[**Table 3.97: Manage Ingredient Batches Use Cases Specification** 202](#_Toc238240665)

[**Table 3.98: Manage Ingredient Receiving Use Cases Specification** 204](#_Toc238240666)

[**Table 3.99: Manage Inventory Use Cases Specification** 205](#_Toc238240667)

[**Table 3.100: Record Ingredient Receiving Use Cases Specification** 206](#_Toc238240668)

[**Table 3.101: Record Ingredient Storage Use Cases Specification** 208](#_Toc238240669)

[**Table 3.102: Record Inventory Transactions Use Cases Specification** 209](#_Toc238240670)

[**Table 3.103: Reject Ingredient Batch Use Cases Specification** 210](#_Toc238240671)

[**Table 3.104: Submit Batch Information Use Cases Specification** 211](#_Toc238240672)

[**Table 3.105: Submit Delivery Information Use Cases Specification** 212](#_Toc238240673)

[**Table 3.106: Update Delivery Status Use Cases Specification** 213](#_Toc238240674)

[**Table 3.107: Upload Batch Certificates Use Cases Specification** 214](#_Toc238240675)

[**Table 3.108: View Batch History Use Cases Specification** 215](#_Toc238240676)

[**Table 3.109: View Delivery Batch Records Use Cases Specification** 216](#_Toc238240677)

[**Table 3.110: View Ingredient Receiving Records Use Cases Specification** 217](#_Toc238240678)

[**Table 3.111: View Purchase Requests Use Cases Specification** 218](#_Toc238240679)

[**Table 3.112: Monitor Daily Food Safety Use Cases Specification** 220](#_Toc238240680)

[**Table 3.113: Conduct Three-Step Food Inspection Use Cases Specification** 221](#_Toc238240681)

[**Table 3.114: Monitor Food Safety Use Cases Specification** 223](#_Toc238240682)

[**Table 3.115: View Food Inspection Records Use Cases Specification** 224](#_Toc238240683)

[**Table 3.116: View Food Inspection Reports Use Cases Specification** 225](#_Toc238240684)

[**Table 3.117: View Food Safety Compliance Report Use Cases Specification** 226](#_Toc238240685)

[**Table 3.118: View Food Safety Reports Use Cases Specification** 227](#_Toc238240686)

[**Table 3.119: View Inspection Records Use Cases Specification** 229](#_Toc238240687)

[**Table 3.120: Record The Daily Inspection Checklist Use Cases Specification** 230](#_Toc238240688)

[**Table 3.121: Dispose Food Sample Use Cases Specification** 231](#_Toc238240689)

[**Table 3.122: Manage Food Sample Retention Use Cases Specification** 233](#_Toc238240690)

[**Table 3.123: Register Food Sample Use Cases Specification** 235](#_Toc238240691)

[**Table 3.124: Retrieve Food Sample Records Use Cases Specification** 237](#_Toc238240692)

[**Table 3.125: View Food Sample Records Use Cases Specification** 238](#_Toc238240693)

[**Table 3.126: Confirm Meal Distribution Use Cases Specification** 239](#_Toc238240694)

[**Table 3.127: Manage Meal Service Use Cases Specification** 241](#_Toc238240695)

[**Table 3.128: Monitor Child Meal History Use Cases Specification** 242](#_Toc238240696)

[**Table 3.129: Monitor School Meals Use Cases Specification** 243](#_Toc238240697)

[**Table 3.130: Monitor Student Meal History Use Cases Specification** 244](#_Toc238240698)

[**Table 3.131: Record Student Meal Consumption Use Cases Specification** 246](#_Toc238240699)

[**Table 3.132: View Allergy Alerts Use Cases Specification** 247](#_Toc238240700)

[**Table 3.133: View Meal Distribution List Use Cases Specification** 248](#_Toc238240701)

[**Table 3.134: View Meal History Use Cases Specification** 249](#_Toc238240702)

[**Table 3.135: View Special Meal Arrangements Use Cases Specification** 251](#_Toc238240703)

[**Table 3.136: View Special Meal Records Use Cases Specification** 252](#_Toc238240704)

[**Table 3.137: View Special Meal Requirements Use Cases Specification** 253](#_Toc238240705)

[**Table 3.138: Create Announcement Use Cases Specification** 255](#_Toc238240706)

[**Table 3.139: Manage Meal Feedback Use Cases Specification** 256](#_Toc238240707)

[**Table 3.140: Submit Meal Feedback Use Cases Specification** 257](#_Toc238240708)

[**Table 3.141: View Meal Feedback List Use Cases Specification** 258](#_Toc238240709)

[**Table 3.142: View Meal Feedback Dashboard Use Cases Specification** 259](#_Toc238240710)

[**Table 3.143: Update Announcement Use Cases Specification** 261](#_Toc238240711)

[**Table 3.144: View Announcement Detail Use Cases Specification** 262](#_Toc238240712)

[**Table 3.145: View Announcement List Use Cases Specification** 263](#_Toc238240713)

[**Table 3.146: View Announcement List (Parent, Teacher) Use Cases Specification** 264](#_Toc238240714)

[**Table 3.147: Assign Incident Severity Use Cases Specification** 265](#_Toc238240715)

[**Table 3.148: Export Incident Reports Use Cases Specification** 267](#_Toc238240716)

[**Table 3.149: Generate Incident Report Use Cases Specification** 268](#_Toc238240717)

[**Table 3.150: Manage Food Safety Incidents Use Cases Specification** 269](#_Toc238240718)

[**Table 3.151: Report Food Safety Concern Use Cases Specification** 271](#_Toc238240719)

[**Table 3.152: Report Food Safety Incident Use Cases Specification** 272](#_Toc238240720)

[**Table 3.153: View Food Poisoning Incident List Use Cases Specification** 273](#_Toc238240721)

[**Table 3.154: View Food Safety Incident Detail Use Cases Specification** 274](#_Toc238240722)

[**Table 3.155: Record Violation Use Cases Specification** 275](#_Toc238240723)

[**Table 3.156: Chat with AI Use Cases Specification** 277](#_Toc238240724)

[**Table 3.157: Manage External Integrations Use Cases Specification** 278](#_Toc238240725)

[**Table 3.158: Monitor System Activities Use Cases Specification** 279](#_Toc238240726)

[**Table 3.159: Receive Notification Use Cases Specification** 280](#_Toc238240727)

[**Table 3.160: Scan Face Data Use Cases Specification** 282](#_Toc238240728)

[**Table 3.161: View Audit Logs Use Cases Specification** 283](#_Toc238240729)

[**Table 3.162: View Error Logs Use Cases Specification** 284](#_Toc238240730)

[**Table 3.163: View Notification History Use Cases Specification** 285](#_Toc238240731)

[**Table 3.164: View System Status Use Cases Specification** 286](#_Toc238240732)

[**Table 3.165: View Usage Statistics Use Cases Specification** 288](#_Toc238240733)

[**Table 3.166: Screen Authorization** 312](#_Toc238240734)

[**Table 3.167: Non-screen Functions** 321](#_Toc238240735)

[**Table 3.168: Screen for Guest Portal** 323](#_Toc238240736)

[**Table 3.169: Screen for Register School** 325](#_Toc238240737)

[**Table 3.170: Screen for Register Supplier** 327](#_Toc238240738)

[**Table 3.171: Screen for Register Service Caterers** 329](#_Toc238240739)

[**Table 3.172: Screen for Login** 331](#_Toc238240740)

[**Table 3.173: Screen for Reset Password** 332](#_Toc238240741)

[**Table 3.174: Screen for Verify OTP** 334](#_Toc238240742)

[**Table 3.175: Screen for Reset Password** 336](#_Toc238240743)

[**Table 3.176: Screen for Personal Profile** 337](#_Toc238240744)

[**Table 3.177: Screen for Edit Profile** 339](#_Toc238240745)

[**Table 3.178: Screen for Change Password** 340](#_Toc238240746)

[**Table 3.179: Screen for Notification Settings** 342](#_Toc238240747)

[**Table 3.180: Screen for Notification History** 343](#_Toc238240748)

[**Table 3.181: Screen for AI Chatbot** 345](#_Toc238240749)

[**Table 3.182: Screen for AI Chat History** 347](#_Toc238240750)

[**Table 3.183: Screen for Catering Dashboard** 348](#_Toc238240751)

[**Table 3.184: Screen for Receiving Dashboard** 350](#_Toc238240752)

[**Table 3.185: Screen for Receive Ingredients** 351](#_Toc238240753)

[**Table 3.186: Screen for Verify Supplier Delivery** 353](#_Toc238240754)

[**Table 3.187: Screen for Inspect Ingredient Quality** 354](#_Toc238240755)

[**Table 3.188: Screen for Capture Receiving Evidence** 356](#_Toc238240756)

[**Table 3.189: Screen for Reject Ingredient Batch** 357](#_Toc238240757)

[**Table 3.190: Screen for Store Ingredients** 359](#_Toc238240758)

[**Table 3.191: Screen for Monitor Expiration Dates** 361](#_Toc238240759)

[**Table 3.192: Screen for Issue Ingredients** 362](#_Toc238240760)

[**Table 3.193: Screen for Create Purchase Request** 364](#_Toc238240761)

[**Table 3.194: Screen for Update Menu** 365](#_Toc238240762)

[**Table 3.195: Screen for View Menu Recommendations** 367](#_Toc238240763)

[**Table 3.196: Screen for Special Meals Configuration** 368](#_Toc238240764)

[**Table 3.197: Screen for Prepare Ingredients** 370](#_Toc238240765)

[**Table 3.198: Screen for Record Cooking Information** 371](#_Toc238240766)

[**Table 3.199: Screens Medical Staff Dashboard** 374](#_Toc238240767)

[**Table 3.200: Screens Health Record For Medical Staff** 375](#_Toc238240768)

[**Table 3.201: Screens Special Meal For Student** 378](#_Toc238240769)

[**Table 3.202: Screens Incidents Records** 381](#_Toc238240770)

[**Table 3.203: Screens Incidents Records Detail** 384](#_Toc238240771)

[**Table 3.204: Screens Evaluate the level Incidents** 386](#_Toc238240772)

[**Table 3.205: Screens Investigation Space** 388](#_Toc238240773)

[**Table 3.206: Screens Export To School Administration** 391](#_Toc238240774)

[**Table 3.207: Screens Extraction of Stored Food Samples** 394](#_Toc238240775)

[**Table 3.208: Screens Collect Evidence** 397](#_Toc238240776)

[**Table 3.209: Screens Treatment Measures** 400](#_Toc238240777)

[**Table 3.210: Parent Dashboard** 402](#_Toc238240778)

[**Table 3.211: Child Selection** 404](#_Toc238240779)

[**Table 3.212: Student Health Hub** 407](#_Toc238240780)

[**Table 3.213: Student Health Hub** 409](#_Toc238240781)

[**Table 3.214: Add/Edit Allergy** 411](#_Toc238240782)

[**Table 3.215: Weekly Menu** 413](#_Toc238240783)

[**Table 3.216: Dish Detail** 415](#_Toc238240784)

[**Table 3.217: Allergy Conflict Alert** 417](#_Toc238240785)

[**Table 3.218: Notifications** 419](#_Toc238240786)

[**Table 3.219: Meal Feedback** 421](#_Toc238240787)

[**Table 3.220: Report Safety Issue** 423](#_Toc238240788)

[**Table 3.221: Meal History Log** 425](#_Toc238240789)

[**Table 3.222: Supplier Dashboard** 427](#_Toc238240790)

[**Table 3.223: Partnership Detail** 429](#_Toc238240791)

[**Table 3.224: Purchase Request Screen** 431](#_Toc238240792)

[**Table 3.225: Purchase Request Screen Detail** 432](#_Toc238240793)

[**Table 3.226: Compliance Docs Screens** 434](#_Toc238240794)

[**Table 3.227: Ingredient Batches Screens** 435](#_Toc238240795)

[**Table 3.228: Ingredient Batches Screens** 437](#_Toc238240796)

[**Table 3.229: Teacher Dashboard** 439](#_Toc238240797)

[**Table 3.230: Meal Distribution** 441](#_Toc238240798)

[**Table 3.231: Student List in Class** 443](#_Toc238240799)

[**Table 3.232: Health Incident Report** 445](#_Toc238240800)

[**Table 3.233: Incident History** 447](#_Toc238240801)

[**Table 3.234: 1. User Management & Authentication** 451](#_Toc238240802)

[**Table 3.235: 2. Role Management & Access Control (RBAC)** 452](#_Toc238240803)

[**Table 3.236: 3. School & Class Management** 454](#_Toc238240804)

[**Table 3.237: 4. Student & Parent Management** 455](#_Toc238240805)

[**Table 3.238: 5. Supplier & Caterer Management** 457](#_Toc238240806)

[**Table 3.239: 6. Ingredient & Inventory Management** 458](#_Toc238240807)

[**Table 3.240: 7. Menu & Diet Management** 460](#_Toc238240808)

[**Table 3.241: 8. Three-Step Food Inspection** 462](#_Toc238240809)

[**Table 3.242: 9. Food Sample Retention** 464](#_Toc238240810)

[**Table 3.243: 10. Meal Serving & Distribution** 465](#_Toc238240811)

[**Table 3.244: 11. Food Incident Management** 467](#_Toc238240812)

[**Table 3.245: 12. Food Safety Monitoring** 468](#_Toc238240813)

[**Table 3.246: 13. Feedback & Notification** 470](#_Toc238240814)

[**Table 3.247: 14. AI & Support Services** 471](#_Toc238240815)

[**Table 3.248: 15. Audit Log & Data Security** 472](#_Toc238240816)

[**Table 3.249: 16. Reporting & Analytics** 474](#_Toc238240817)

[**Table 3.250: 17. Offline & Data Sync** 476](#_Toc238240818)

[**Table 3.251: Error messages block the user's action and require a corrective step.** 480](#_Toc238240819)

[**Table 3.252: Confirmation messages indicating an action completed successfully.** 483](#_Toc238240820)

[**Table 3.253: Warnings that do not block the action but require attention or preventive follow-up.** 486](#_Toc238240821)

# 3. Software Requirement Specification

## 3.1 Requirement Overview

The School Food Safety Management Support System (SFSMSS) is a digital platform developed to support food safety management activities in schools. The system enables schools to manage suppliers, ingredient receiving, inventory, menu planning, food processing, food sample retention, student health records, allergy information, and food safety incidents through a centralized role-based system.

The platform supports end-to-end food safety processes, from supplier qualification and ingredient inspection to meal preparation, health monitoring, and incident investigation. Parents can monitor menus and nutritional information, while school staff can ensure compliance with food safety regulations and operational procedures.

By integrating notification services, location verification, and Artificial Intelligence technologies, SFSMSS improves food safety compliance, enhances traceability, reduces operational risks, and strengthens communication between schools, suppliers, medical staff, teachers, and parents.

### 3.1.1 Context Diagram

![](data:image/png;base64...)

**Figure 3.1: Context Diagram**

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

**Table 3.1: Context Diagram Description**

### 3.1.2 Main Workflows

#### 3.1.2.1 Supplier Registration & Approval Workflow

![](data:image/png;base64...)

**Figure 3.2: Supplier Registration & Approval Workflow**

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Submit Supplier Profile | Supplier submits company profile, business license, food safety certificates, product information, and supporting documents to the school. | Supplier | Start of the flow |
| 2 | Review Supplier Documents | School Administration reviews supplier qualifications, certificates, licenses, and compliance documents. | School Administration | Manual review |
| 3 | Documents Valid? | School Administration determines whether the submitted documents meet regulatory and school requirements. | School Administration | Decision point: Yes ? Step 4; No ? Step 8 |
| 4 | Evaluate Supplier | School Administration evaluates supplier capability, product quality, reputation, and service capacity. | School Administration | May involve additional verification |
| 5 | Approve Supplier? | School Administration decides whether the supplier is eligible to provide ingredients for the school. | School Administration | Decision point: Yes ? Step 6; No ? Step 8 |
| 6 | Create Supplier Record | System creates the supplier profile and assigns Approved status. | System | Executed after approval |
| 7 | Send Approval Notification | System sends approval notification and supplier onboarding information. | System | End of successful flow |
| 8 | Record Rejection Reason | School Administration records the rejection reason. | School Administration | Executed when supplier is not approved |
| 9 | Send Rejection Notification | System sends rejection notification to the supplier. | System | End of rejection flow |

**Table 3.2: Supplier Registration & Approval Workflow**

#### 3.1.2.2 Ingredient Receiving Workflow

![](data:image/png;base64...)

**Figure 3.3: Ingredient Receiving Workflow**

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Deliver Ingredient Batch | Raw Food Supplier delivers an ingredient batch to the school according to the agreed schedule. | Raw Food Supplier | Start of workflow |
| 2 | Start Receiving Session | Catering Staff initiate a receiving session in the system for the delivered batch. | Service Caterers | Receiving process begins |
| 3 | Verify Supplier Status | System verifies that the supplier is approved and eligible to provide ingredients. | System | Decision point: Approved ? Step 4; Rejected ? Step 14 |
| 4 | Review Batch Information | Catering Staff reviews batch information including batch numbers, production date, expiration date, quantity, and certificates. | Service Caterers | Batch verification |
| 5 | Inspect Ingredient Quality | Catering Staff performs visual inspection of ingredient quality, packaging condition, freshness, and food safety indicators. | Service Caterers | Physical inspection |
| 6 | Capture Evidence Photos | Catering Staff captures photos of ingredients, labels, packaging, and supporting documents. | Service Caterers | Mandatory evidence |
| 7 | Capture GPS and Timestamp | System automatically records GPS coordinates, device information, and timestamp during receiving activities. | System | Anti-fraud control |
| 8 | Verify Receiving Location | System verifies that the receiving activity is performed within the authorized receiving area. | System | Decision point: Valid ? Step 9; Invalid ? Step 14 |
| 9 | Complete Inspection Checklist | Catering Staff completes the receiving checklist and records inspection results. | Service Caterers | Required before approval |
| 10 | Ingredient Qualified? | Catering Staff determines whether the ingredient batch satisfies food safety requirements. | Service Caterers | Decision point: Yes ? Step 11; No ? Step 14 |
| 11 | Create Batch Record | System creates a batch record containing supplier, batch, GPS, timestamp, and inspection information. | System | Traceability enabled |
| 12 | Update Inventory | System updates inventory quantities and expiration tracking information. | System | Inventory updated |
| 13 | Generate Receiving Record | System generates and stores the receiving record together with evidence and audit logs. | System | Successful completion |
| 14 | Reject Ingredient Batch | Catering Staff rejects the batch and records the rejection reason. System notifies relevant parties. | Service Caterers / System | End of rejection flow |

**Table 3.3: Ingredient Receiving Workflow**

#### 3.1.2.3 Ingredient Storage & Inventory Management Workflow

![](data:image/png;base64...)

**Figure 3.4: Ingredient Storage & Inventory Management Workflow**

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Receive Approved Batch | The system receives approved ingredient batches from the Ingredient Receiving Workflow. | System | Start of workflow |
| 2 | Assign Storage Location | Catering Staff assigns the ingredient batch to an appropriate storage location such as freezer, refrigerator, or dry storage. | Service Caterers | Storage classification |
| 3 | Store Ingredient Batch | Catering Staff places the ingredient batch into the designated storage area. | Service Caterers | Physical storage |
| 4 | Record Storage Information | System records storage location, batch information, quantity, and expiration date. | System | Inventory tracking |
| 5 | Monitor Inventory Status | System continuously tracks inventory quantity and batch availability. | System | Automatic monitoring |
| 6 | Monitor Expiration Date | System monitors expiration dates of stored ingredients. | System | Automatic monitoring |
| 7 | Expiration Alert Required? | System determines whether any batch is approaching its expiration date. | System | Decision point: Yes ? Step 8; No ? Step 9 |
| 8 | Generate Expiration Alert | System sends expiration alerts to Service Caterers and School Administration. | System | Preventing food waste |
| 9 | Ingredient Request Received? | Service Caterers requests ingredients for food preparation. | Service Caterers | Decision point: Yes ? Step 10; No ? Continue monitoring |
| 10 | Verify Inventory Availability | System verifies that sufficient inventory is available for the requested ingredients. | System | Inventory validation |
| 11 | Inventory Available? | System determines whether inventory is sufficient. | System | Decision point: Yes ? Step 12; No ? Step 15 |
| 12 | Issue Ingredient Batch | Catering Staff issues ingredient batches from inventory according to FIFO/FEFO principles. | Service Caterers | Stock-out process |
| 13 | Update Inventory Quantity | System updates inventory balances and batch quantities after issuance. | System | Inventory adjustment |
| 14 | Record Inventory Transaction | System records inventory movement in the audit log and inventory history. | System | End of successful flow |
| 15 | Generate Inventory Shortage Alert | System generates inventory shortage alerts for Service Caterers. | System | End of shortage flow |

**Table 3.4: Ingredient Storage & Inventory Management Workflow**

#### 3.1.2.4 Menu Planning & Approval Workflow

![](data:image/png;base64...)

**Figure 3.5: Menu Planning & Approval Workflow**

| Step | Step Name | Detailed Description | Role | Note |
| --- | --- | --- | --- | --- |
| 1 | Review Menu Requirements | Catering Manager reviews nutritional requirements, school policies, ingredient availability, and meal planning guidelines. | Service Caterers | Start of workflow |
| 2 | Review Allergy Statistics | System summarizes allergy categories and special dietary requirements of students. | System | Preparation step |
| 3 | Generate Menu Draft | Catering Manager creates a standard menu manually. | Service Caterers | Menu planning |
| 6 | Detect Allergy Conflicts | System checks whether menu items contain allergens affecting registered students. | System | Automatic validation |
| 7 | Allergy Conflict Detected? | System determines whether allergy risks exist. | System | Decision point |
| 9 | Create Alternative Meals | Catering Manager manually creates alternative meals for the affected allergy categories (e.g., seafood allergies, milk allergy, egg allergy). | Service Caterers | Only executed if conflict exists |
| 10 | Submit Menu for Approval | Catering Manager submits the standard menu and special meal options for approval. | Service Caterers | Approval request |
| 11 | Review Menu | School Administration reviews menu content, nutrition information, and allergy accommodations. | School Administration | Approval review |
| 12 | Menu Approved? | School Administration decides whether to approve or reject the menu. | School Administration | Decision point |
| 13 | Publish Menu | System publishes the approved menu and associated special meal options. | System | Approved flow |
| 14 | Notify Stakeholders | System sends menu notifications to parents, teachers, and medical staff. | System | Communication |
| 15 | Store Menu Records | System stores menu history, approval records, and audit logs. | System | End of successful flow |
| 16 | Reject Menu | School Administration rejects the menu and provides comments. | School Administration | Rejection flow |
| 17 | Revise Menu | Catering Manager revises the menu and resubmits it. | Service Caterers | Return to Step 3 |

**Table 3.5: Menu Planning & Approval Workflow**

#### 3.1.2.5 Ingredient Issue to Kitchen Workflow

![](data:image/png;base64...)

**Figure 3.6: Ingredient Issue to Kitchen Workflow**

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

**Table 3.6: Ingredient Issue to Kitchen Workflow**

#### 3.1.2.6 Food Preparation Workflow

![](data:image/png;base64...)

**Figure 3.7: Food Preparation Workflow**

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

**Table 3.7: Food Preparation Workflow**

#### 3.1.2.7 Three-Step Food Inspection Workflow

![](data:image/png;base64...)

**Figure 3.8: Three-Step Food Inspection Workflow**

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

**Table 3.8: Three-Step Food Inspection Workflow**

#### 3.1.2.8 Food Sample Retention Workflow

**Figure 3.9: Food Sample Retention Workflow**

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

**Table 3.9: Food Sample Retention Workflow**

#### 3.1.2.9 Allergy & Special Meal Management Workflow

![](data:image/png;base64...)

**Figure 3.10: Allergy & Special Meal Management Workflow**

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

**Table 3.10: Allergy & Special Meal Management Workflow**

#### 3.1.2.10 Meal Serving Workflow

![C:\Users\Admin\AppData\Local\Microsoft\Windows\INetCache\Content.MSO\89D3720B.tmp](data:image/png;base64...)

**Figure 3.11: Meal Serving Workflow**

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

**Table 3.11: Meal Serving Workflow**

#### 3.1.2.11 Food Safety Incident Handling Workflow

![C:\Users\Admin\AppData\Local\Microsoft\Windows\INetCache\Content.MSO\A0F6B43E.tmp](data:image/png;base64...)

**Figure 3.12: Food Safety Incident Handling Workflow**

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

**Table 3.12: Food Safety Incident Handling Workflow**

#### 3.1.2.12 Parent Meal Monitoring Workflow

![C:\Users\Admin\AppData\Local\Microsoft\Windows\INetCache\Content.MSO\4E35388.tmp](data:image/png;base64...)

**Figure 3.13: Parent Meal Monitoring Workflow**

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

**Table 3.13: Parent Meal Monitoring Workflow**

### 3.1.3 Generation Specification

#### 3.1.3.1 Actors

| # | Actor | Description |
| --- | --- | --- |
| 1 | System Admin | Platform-level administrator responsible for managing system configuration, user accounts, roles, permissions, security settings, and audit logs across all schools using the platform. |
| 2 | School Administration | School-level administrator responsible for supervising food safety operations, approving suppliers and menus, monitoring compliance, reviewing reports, and managing food safety incidents within the school. |
| 3 | Service Caterers | Third-party food service provider responsible for ingredient receiving, inventory management, menu planning, food preparation, food inspection, food sample retention, meal serving, and implementation of corrective actions related to food safety. |
| 4 | Raw Food Supplier | Supplier responsible for providing raw food ingredients, maintaining supplier profiles, uploading compliance certificates, declaring delivery batches, and supporting food traceability requirements. |
| 5 | Medical Staff | School health officer responsible for managing student health records, verifying allergy information, monitoring food safety risks, investigating food safety incidents, and supervising food sample management activities. |
| 6 | Teacher | Homeroom teacher is responsible for monitoring students during meal service, viewing allergy alerts, receiving food safety notifications, updating student information, and assisting in food safety incident reporting. |
| 7 | Parent | Parents or guardians are responsible for providing student health information, declaring allergies and dietary requirements, monitoring menus and nutrition information, receiving notifications, and submitting feedback regarding school meals. |

**Table 3.14: Actors List**

#### 3.1.3.2 Use Case Diagrams

##### 3.1.3.2.1 UCs for Guest

![](data:image/png;base64...)

**Figure 3.14: UCs for Guest**

##### 3.1.3.2.2 UCs for User

![](data:image/png;base64...)

**Figure 3.15: UCs for User**

##### 3.1.3.2.3 UCs for System Admin

![](data:image/png;base64...)

**Figure 3.16: UCs for System Admin**

##### 3.1.3.2.4 UCs for School Administration

![](data:image/png;base64...)

**Figure 3.17: UCs for School Administration**

##### 3.1.3.2.5 UCs for Teacher

![](data:image/png;base64...)

**Figure 3.18: UCs for Teacher**

##### 3.1.3.2.6 UCs for Parent

![](data:image/png;base64...)

**Figure 3.19: UCs for Parent**

##### 3.1.3.2.7 UCs for Medical Staff

![](data:image/png;base64...)

**Figure 3.20: UCs for Medical Staff**

##### 3.1.3.2.8 UCs for Raw Food Supplier

![](data:image/png;base64...)

**Figure 3.21: UCs for Raw Food Supplier**

##### 3.1.3.2.9 UCs for Service Caterers

![](data:image/png;base64...)

**Figure 3.22: UCs for Service Caterers**

#### 3.1.3.3 Use Cases Description

| # | Actor | Use Case | Feature | Description |
| --- | --- | --- | --- | --- |
| UC-001 | System Admin | Activate/Deactivate School | School & Class Management | Allows System Admin to activate or deactivate a school and control its operational status within the platform. |
| UC-002 | School Administration | Activate/Deactivate Staff | User Management & Access Control | Allows School Administration to activate or deactivate staff accounts within the school and control their access to system functions. |
| UC-003 | System Admin | Activate/Deactivate User | User Management & Access Control | Allows System Admin to activate or deactivate user accounts across the platform and enforce account access policies. |
| UC-004 | School Administration | Approve/Reject Menu | Menu & Diet Management | Enables the school administration to successfully approval / reject menu within the SFMS. |
| UC-005 | School Administration | Assign Homeroom Teacher | School & Class Management | Allows School Administration to assign or reassign homeroom teachers to classrooms. |
| UC-006 | Medical Staff | Assign Incident Severity | Incident & Emergency Management | Enables the medical staff to successfully assign incident severity within the SFMS. |
| UC-007 | School Administration | Bulk Import Student & Parent | Student & Parent Management | Allows School Administration to import student and parent records in bulk using predefined templates and validation rules. |
| UC-008 | User | Change Password | User Management & Access Control | Allows authenticated users to change their account password and maintain account security. |
| UC-009 | User | Chat with AI | AI & Supporting Services | Enables the user to successfully chat with ai within the SFMS. |
| UC-010 | Raw Food Supplier | Confirm Delivery Schedule | Ingredient & Inventory Management | Enables the raw food supplier to successfully confirm delivery schedule within the SFMS. |
| UC-011 | Teacher | Confirm Meal Distribution | Meal Serving & Distribution | Enables the teacher to successfully confirm meal distribution within the SFMS. |
| UC-012 | School Administration | Create Announcement | Feedback & Announcement Management | Enables the school administration to successfully create announcement within the SFMS. |
| UC-013 | School Administration | Create Classroom | School & Class Management | Allows School Administration to create classrooms and define class information within the school. |
| UC-014 | Raw Food Supplier | Add Delivery Person | Supplier & Partner Management | Enables the raw food supplier to successfully create info delivery person within the SFMS. |
| UC-015 | Service Caterers | Create Menu | Menu & Diet Management | Enables the Service Caterers to successfully create menu within the SFMS. |
| UC-016 | Service Caterers | Create Purchase Request | Ingredient & Inventory Management | Enables the Service Caterers to successfully create purchase request within the SFMS. |
| UC-017 | Service Caterers | Create Special Meal Plan | Menu & Diet Management | Enables the Service Caterers to successfully create special meal plan within the SFMS. |
| UC-018 | School Administration | Create Staff | User Management & Access Control | Enables the school administration to successfully create staff within the SFMS. |
| UC-019 | School Administration | Create Student | Student & Parent Management | Allows School Administration to create new student records and associate them with parent accounts. |
| UC-020 | System Admin | Create User | User Management & Access Control | Enables the system admin to successfully create user within the SFMS. |
| UC-021 | School Administration | Monitor Daily Food Safety | Food Safety Inspection & Monitoring | Enables the school administration to successfully daily food safety monitoring within the SFMS. |
| UC-022 | Raw Food Supplier | Delete Face Data | Supplier & Partner Management | Enables the raw food supplier to successfully delete face data within the SFMS. |
| UC-023 | Service Caterers | Dispose Food Sample | Food Sample Retention | Enables the Service Caterers to successfully dispose food sample within the SFMS. |
| UC-024 | School Administration, Medical Staff | Export Incident Reports | Incident & Emergency Management | Enables the school administration to successfully export incident reports within the SFMS. |
| UC-025 | User | Reset Password | User Management & Access Control | Allows users to reset their password through a secure  account recovery process. |
| UC-026 | Medical Staff | Generate Incident Report | Incident & Emergency Management | Enables the medical staff to successfully generate incident report within the SFMS. |
| UC-027 | User | Login | User Management & Access Control | Allows users to authenticate with valid credentials and access authorized system functions. |
| UC-028 | User | Logout | User Management & Access Control | Allows users to securely terminate their current session and exit the system. |
| UC-029 | Medical Staff | View Allergy & Special Meal List | Student & Parent Management | Allows Medical Staff to manage allergy profiles and special meal requirements for students. |
| UC-030 | Raw Food Supplier | Manage Deliveries | Ingredient & Inventory Management | Enables the raw food supplier to successfully manage deliveries within the SFMS. |
| UC-031 | System Admin | Manage External Integrations | AI & Supporting Services | Enables the system admin to successfully manage external integrations within the SFMS. |
| UC-032 | Medical Staff | Manage Food Safety Incidents | Incident & Emergency Management | Enables the medical staff to successfully manage food safety incidents within the SFMS. |
| UC-033 | Service Caterers | Manage Food Sample Retention | Food Sample Retention | Enables the Service Caterers to successfully manage food sample retention within the SFMS. |
| UC-034 | Raw Food Supplier | Manage Ingredient Batches | Ingredient & Inventory Management | Enables the raw food supplier to successfully manage ingredient batches within the SFMS. |
| UC-035 | Service Caterers | Manage Ingredient Receiving | Ingredient & Inventory Management | Enables the Service Caterers to successfully manage ingredient receiving within the SFMS. |
| UC-036 | Service Caterers | Manage Inventory | Ingredient & Inventory Management | Enables the Service Caterers to successfully manage inventory within the SFMS. |
| UC-037 | Parent | Manage Meal Feedback | Feedback & Announcement Management | Enables the parent to successfully manage meal feedback within the SFMS. |
| UC-038 | Service Caterers | Manage Meal Service | Meal Serving & Distribution | Enables the Service Caterers to successfully manage meal service within the SFMS. |
| UC-039 | Service Caterers | Manage Menus | Menu & Diet Management | Enables the Service Caterers to successfully manage menus within the SFMS. |
| UC-040 | Raw Food Supplier | Manage Profile | Supplier & Partner Management | Enables the raw food supplier to successfully manage profile within the SFMS. |
| UC-041 | Parent | View Student Health Information | Student & Parent Management | Allows Parents to maintain and monitor health information associated with their children. |
| UC-042 | Medical Staff | View Student Health Record List | Student & Parent Management | Allows Medical Staff to manage verified health records and maintain student medical histories. |
| UC-043 | Service Caterers | Conduct Three-Step Food Inspection | Food Safety Inspection & Monitoring | Enables the Service Caterers to successfully manage three-step food inspection within the SFMS. |
| UC-044 | Parent | Monitor Child Meal History | Meal Serving & Distribution | Enables the parent to successfully monitor child meal history within the SFMS. |
| UC-045 | Medical Staff | Monitor Food Safety | Food Safety Inspection & Monitoring | Enables the medical staff to successfully monitor food safety within the SFMS. |
| UC-046 | Parent | Monitor School Meals | Meal Serving & Distribution | Enables the parent to successfully monitor school meals within the SFMS. |
| UC-047 | Teacher | Monitor Student Meal History | Meal Serving & Distribution | Enables the teacher to successfully monitor student meal history within the SFMS. |
| UC-048 | System Admin | Monitor System Activities | AI & Supporting Services | Enables the system admin to successfully monitor system activities within the SFMS. |
| UC-049 | User | Receive Notification | AI & Supporting Services | Allows users to receive system notifications, alerts, and important operational updates. |
| UC-050 | Service Caterers | Record Ingredient Receiving | Ingredient & Inventory Management | Enables the Service Caterers to successfully record ingredient receiving within the SFMS. |
| UC-051 | Service Caterers | Record Ingredient Storage | Ingredient & Inventory Management | Enables the Service Caterers to successfully record ingredient storage within the SFMS. |
| UC-052 | Service Caterers | Record Inventory Transactions | Ingredient & Inventory Management | Enables the Service Caterers to successfully record inventory transactions within the SFMS. |
| UC-053 | Guest | Register Service Caterer Account | Supplier & Partner Management | Enables the guest to successfully register Service Caterers account within the SFMS. |
| UC-054 | Raw Food Supplier | Register Face Data | Supplier & Partner Management | Enables the raw food supplier to successfully register face data within the SFMS. |
| UC-055 | Service Caterers | Register Food Sample | Food Sample Retention | Enables the Service Caterers to successfully register food sample within the SFMS. |
| UC-056 | Guest | Register School | School & Class Management | Allows a school representative to submit a registration request for joining the platform. |
| UC-057 | Guest | Register Supplier Account | Supplier & Partner Management | Enables the guest to successfully register supplier account within the SFMS. |
| UC-058 | Service Caterers | Reject Ingredient Batch | Ingredient & Inventory Management | Enables the Service Caterers to successfully reject ingredient batch within the SFMS. |
| UC-059 | Parent | Report Food Safety Concern | Incident & Emergency Management | Enables the parent to successfully report food safety concern within the SFMS. |
| UC-060 | Teacher | Report Food Safety Incident | Incident & Emergency Management | Enables the teacher to successfully report food safety incident within the SFMS. |
| UC-061 | Service Caterers | Request AI Menu Recommendations | Menu & Diet Management | Enables the Service Caterers to successfully request ai menu recommendations within the SFMS. |
| UC-062 | Medical Staff | Retrieve Food Sample Records | Food Sample Retention | Enables the medical staff to successfully retrieve food sample records within the SFMS. |
| UC-063 | Medical Staff | Review Health Information Detail | Student & Parent Management | Allows Medical Staff to review and verify submitted student health information. |
| UC-064 | Medical Staff | Review Special Meal Requests | Student & Parent Management | Allows Medical Staff to review and validate requests for special meals and dietary accommodations. |
| UC-065 | Service Caterers | Scan Face Data | AI & Supporting Services | Enables the Service Caterers to successfully scan face data within the SFMS. |
| UC-066 | Teacher | Record Student Meal Consumption | Meal Serving & Distribution | Enables the teacher to successfully student meal consumption within the SFMS. |
| UC-067 | Raw Food Supplier | Submit Batch Information | Ingredient & Inventory Management | Enables the raw food supplier to successfully submit batch information within the SFMS. |
| UC-068 | Raw Food Supplier | Submit Delivery Information | Ingredient & Inventory Management | Enables the raw food supplier to successfully submit delivery information within the SFMS. |
| UC-069 | Parent | Submit Health & Allergy Information | Student & Parent Management | Enables the parent to successfully submit health & allergy information within the SFMS. |
| UC-070 | Teacher | Submit Meal Feedback | Feedback & Announcement Management | Enables the teacher to successfully submit meal feedback within the SFMS. |
| UC-071 | Service Caterers | Submit Menu For Approval | Menu & Diet Management | Enables the Service Caterers to successfully submit menu for approval within the SFMS. |
| UC-072 | School Administration | Update Announcement | Feedback & Announcement Management | Enables the school administration to successfully update announcement within the SFMS. |
| UC-073 | School Administration | Update Classroom | School & Class Management | Allows School Administration to modify classroom information and maintain class records. |
| UC-074 | Raw Food Supplier | Update Compliance Documents | Supplier & Partner Management | Enables the raw food supplier to successfully update compliance documents within the SFMS. |
| UC-075 | Raw Food Supplier | Update Delivery Status | Ingredient & Inventory Management | Enables the raw food supplier to successfully update delivery status within the SFMS. |
| UC-076 | Parent | Update Health & Allergy Information | Student & Parent Management | Allows Parents to update previously submitted health and allergy information when changes occur. |
| UC-077 | Medical Staff | Update Health Records | Student & Parent Management | Allows Medical Staff to update verified health records based on assessments or new medical information. |
| UC-078 | System Admin | Update School Information | School & Class Management | Allows System Admin to modify school information and maintain accurate school records. |
| UC-079 | Service Caterers | Update Menu | Menu & Diet Management | Enables the Service Caterers to successfully update menu within the SFMS. |
| UC-080 | User | Update Profile | User Management & Access Control | Allows users to update their profile information and maintain accurate personal records. |
| UC-081 | School Administration | Update Student | Student & Parent Management | Allows School Administration to update student information and maintain accurate enrollment records. |
| UC-082 | Raw Food Supplier | Upload Batch Certificates | Ingredient & Inventory Management | Enables the raw food supplier to successfully upload batch certificates within the SFMS. |
| UC-083 | Raw Food Supplier | Upload Food Safety Certificates | Supplier & Partner Management | Enables the raw food supplier to successfully upload food safety certificates within the SFMS. |
| UC-084 | Raw Food Supplier | Upload Product Origin Documents | Supplier & Partner Management | Enables the raw food supplier to successfully upload product origin documents within the SFMS. |
| UC-085 | Service Caterers, Teacher | View Allergy Alerts | Meal Serving & Distribution | Enables the Service Caterers, teacher to successfully view allergy alerts within the SFMS. |
| UC-086 | Medical Staff, Parent | View Allergy Information | Student & Parent Management | Allows authorized users to view verified allergy information and dietary restrictions. |
| UC-087 | User, School Administration | View Announcement Detail | Feedback & Announcement Management | Enables the school administration to successfully view announcement detail within the SFMS. |
| UC-088 | System Admin | View Audit Logs | AI & Supporting Services | Enables the system admin to successfully view audit logs within the SFMS. |
| UC-089 | Raw Food Supplier | View Batch History | Ingredient & Inventory Management | Enables the raw food supplier to successfully view batch history within the SFMS. |
| UC-090 | Raw Food Supplier | View Delivery Batch Records | Ingredient & Inventory Management | Enables the raw food supplier to successfully view delivery batch records within the SFMS. |
| UC-091 | Raw Food Supplier | View Delivery Person List | Supplier & Partner Management | Enables the raw food supplier to successfully view delivery person list within the SFMS. |
| UC-092 | System Admin | View Error Logs | AI & Supporting Services | Enables the system admin to successfully view error logs within the SFMS. |
| UC-093 | Medical Staff | View Three-Step Food Inspection | Food Safety Inspection & Monitoring | Enables the medical staff to successfully view food inspection records within the SFMS. |
| UC-094 | School Administration | View Food Inspection Reports | Food Safety Inspection & Monitoring | Enables the school administration to successfully view food inspection reports within the SFMS. |
| UC-095 | School Administration | View Food Poisoning Incident List | Incident & Emergency Management | Enables the school administration to successfully view food poisoning incident list within the SFMS. |
| UC-096 | Medical Staff | View Food Safety Compliance Report | Food Safety Inspection & Monitoring | Enables the medical staff to successfully view food safety compliance report within the SFMS. |
| UC-097 | School Administration | View Food Safety Incident Detail | Incident & Emergency Management | Enables the school administration to successfully view food safety incident detail within the SFMS. |
| UC-098 | Medical Staff | View Food Safety Reports | Food Safety Inspection & Monitoring | Enables the medical staff to successfully view food safety reports within the SFMS. |
| UC-099 | Medical Staff, School Administration, Service Caterers | View Food Sample Records | Food Sample Retention | Enables the medical staff, school administration to successfully view food sample records within the SFMS. |
| UC-100 | System Admin | View School Information | School & Class Management | Allows System Admin to view detailed information and operational status of registered schools. |
| UC-101 | School Administration | View Ingredient Receiving Records | Ingredient & Inventory Management | Enables the school administration to successfully view ingredient receiving records within the SFMS. |
| UC-102 | Service Caterers | View Inspection Records | Food Safety Inspection & Monitoring | Enables the Service Caterers to successfully view inspection records within the SFMS. |
| UC-103 | School Administration, User | View Announcement List | Feedback & Announcement Management | Enables the school administration to successfully view list announcement within the SFMS. |
| UC-104 | Parent, Teacher | View Announcement List | Feedback & Announcement Management | Enables the parent, teacher to successfully view list announcements within the SFMS. |
| UC-105 | School Administration | View Service Caterer List | Supplier & Partner Management | Enables the school administration to successfully view list Service Caterers within the SFMS. |
| UC-106 | School Administration | View Menu List | Menu & Diet Management | Enables the school administration to successfully view list menu within the SFMS. |
| UC-107 | System Admin | View Permission List | User Management & Access Control | Allows System Admin to review permissions assigned to system roles. |
| UC-108 | System Admin | View Role List | User Management & Access Control | Allows System Admin to view and manage available roles in the system. |
| UC-109 | School Administration | View Supplier List | Supplier & Partner Management | Enables the school administration to successfully view list supplier within the SFMS. |
| UC-110 | Service Caterers, Teacher | View Meal Distribution List | Meal Serving & Distribution | Enables the Service Caterers to successfully view meal distribution list within the SFMS. |
| UC-111 | Parent | View Meal History | Meal Serving & Distribution | Enables the parent to successfully view meal history within the SFMS. |
| UC-112 | School Administration | View Menu Detail | Menu & Diet Management | Enables the school administration to successfully view menu detail within the SFMS. |
| UC-113 | Parent | View Menu Information | Menu & Diet Management | Enables the parent to successfully view menu information within the SFMS. |
| UC-114 | User | View Notification History | AI & Supporting Services | Allows users to review previously received notifications and alerts. |
| UC-115 | User | View Profile | User Management & Access Control | Allows users to view their personal profile and account information. |
| UC-116 | Raw Food Supplier | View Purchase Requests | Ingredient & Inventory Management | Enables the raw food supplier to successfully view purchase requests within the SFMS. |
| UC-117 | System Admin | View School List | School & Class Management | Allows System Admin to search, filter, and review registered schools across the platform. |
| UC-118 | Parent | View Special Meal Arrangements | Meal Serving & Distribution | Enables the parent to successfully view special meal arrangements within the SFMS. |
| UC-119 | Parent | View Special Meal Records | Meal Serving & Distribution | Enables the parent to successfully view special meal records within the SFMS. |
| UC-120 | Teacher | View Special Meal Requirements | Meal Serving & Distribution | Enables the teacher to successfully view special meal requirements within the SFMS. |
| UC-121 | School Administration | View Staff Account List | User Management & Access Control | Allows School Administration to search, filter, and review staff accounts within the school. |
| UC-122 | Teacher | View Student Health & Allergy Information | Student & Parent Management | Allows Teachers to review student health alerts and allergy information relevant to classroom meal activities. |
| UC-123 | Medical Staff | View Student Health History | Student & Parent Management | Enables the medical staff to successfully view student health history within the SFMS. |
| UC-124 | School Administration | View Student List | Student & Parent Management | Enables the school administration to successfully view student list within the SFMS. |
| UC-125 | School Administration | View Supplier Detail | Supplier & Partner Management | Enables the school administration to successfully view supplier detail within the SFMS. |
| UC-126 | System Admin | View System Status | AI & Supporting Services | Enables the system admin to successfully view system status within the SFMS. |
| UC-127 | System Admin | View Usage Statistics | AI & Supporting Services | Enables the system admin to successfully view usage statistics within the SFMS. |
| UC-128 | Raw Food Supplier | Submit Supplier Application | Supplier & Partner Management | Enables the raw food supplier to successfully submit supplier application within the SFMS. |
| UC-129 | System Admin | View User Account List | User Management & Access Control | Allows System Admin to search, filter, and review registered user accounts across the platform. |
| UC-130 | User | Activate Account & Set Password | User Management & Access Control | Enables a new staff member or user to activate their account, set their initial password, and transition their account status. |
| UC-131 | Raw Food Supplier | Manage Supplier Application | Supplier & Partner Management | Enables the raw food supplier to successfully manage supplier application within the SFMS. |
| UC-132 | Service Caterers | Submit Catering Application | Supplier & Partner Management | Enables the service caterers to successfully submit catering application within the SFMS. |
| UC-133 | Service Caterers | Track Catering Application Status | Supplier & Partner Management | Enables the service caterers to successfully track catering application status within the SFMS. |
| UC-134 | Service Caterers | Update/Resubmit Catering Application | Supplier & Partner Management | Enables the service caterers to successfully update/resubmit catering application within the SFMS. |
| UC-135 | Service Caterers | Manage Catering Application | Supplier & Partner Management | Enables the service caterers to successfully manage catering application within the SFMS. |
| UC-136 | Service Caterers, School Administration | View Meal Feedback List | Meal Serving & Distribution | Enables the Service Caterers to safely query, view List Meal Feedback records. |
| UC-137 | School Administration | View Meal Feedback Dashboard | Meal Serving & Distribution | Enables the School Administration to view aggregated and anonymized feedback charts |
| UC-138 | School Administration | View Classroom Detail | School & Class Management | Enables the School Administration to view the comprehensive details of a specific classroom, including its profile, capacity, assigned homeroom teacher, and student list. |
| UC-139 | School Administration | View Classroom List | School & Class Management | Enables the School Administration to query, filter, and view the list of all classrooms in the school. |
| UC-140 | Raw Food Supplier | Manage Compliance Documents | Supplier & Partner Management | Enables the raw food supplier to successfully manage compliance documents within the SFMS. |
| UC-141 | Raw Food Supplier | Track Supplier Application Status | Supplier & Partner Management | Enables the raw food supplier to successfully track supplier application status within the SFMS. |
| UC-142 | Raw Food Supplier | Update/Resubmit Supplier Application | Supplier & Partner Management | Enables the raw food supplier to successfully update/resubmit supplier application within the SFMS. |
| UC-143 | School Administration | Add Supplier | Supplier & Partner Management | Allows School Administration to directly add a new supplier record into the system. |
| UC-144 | School Administration | View Catering Company Detail | Supplier & Partner Management | Allows School Administration to view the detailed profile of a specific service caterer company. |
| UC-145 | School Administration | Add Service Caterer | Supplier & Partner Management | Allows School Administration to directly add a new service caterer record into the system. |
| UC-146 | Medical Staff | Record The Daily Inspection Checklist | Food Safety Inspection & Monitoring | Allows Medical Staff to record the results of the daily food safety inspection checklist. |
| UC-147 | Medical Staff | Record Violation | Incident & Emergency Management | Allows Medical Staff to record a food safety violation identified during inspection or monitoring. |
| UC-148 | School Administration | View Analytics Dashboard | Reporting & Analytics | Allows School Administration to view an analytics dashboard summarizing operational metrics such as food safety risk indicators, inspection compliance rate, and daily meal summary, scoped to their school |
| UC-149 | School Administration | Export Report | Reporting & Analytics | Allows School Administration to export the currently viewed report to PDF or Excel format for compliance retention and offline review. |
| UC-150 | School Administration | Schedule Automated Report | Reporting & Analytics | Allows School Administration to schedule periodic report generation and email delivery on a daily, weekly, or monthly basis. |

**Table 3.15: Use Cases Description**

## 3.2 Functional Specifications

### 3.2.1 User Management & Access Control

#### 3.2.1.1 Activate/ Deactivate Staff

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-002 - Activate/Deactivate Staff | | |
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

**Table 3.16: Activate/Deactivate Staff Use Cases Specification**

#### 3.2.1.2 Activate/Deactivate User

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-003 - Activate/Deactivate User | | |
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

**Table 3.17: Activate/Deactivate User Use Cases Specification**

#### 3.2.1.3 Change Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-008 - Change Password | | |
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

**Table 3.18: Change Password Use Cases Specification**

#### 3.2.1.4 Create Staff

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-018 - Create Staff | | |
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

**Table 3.19: Create Staff Use Cases Specification**

#### 3.2.1.5 Create User

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-020 - Create User | | |
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

**Table 3.20: Create User Use Cases Specification**

#### 3.2.1.6 Reset Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-025 - Reset Password | | |
| Created By | NamNVHE180863 | Date Created | 14/06/2026 |
| Primary Actor | User | Secondary Actors | Email Service |
| Trigger | User clicks 'Reset Password' on the login screen and submits their registered email. | | |
| Description | Enables the User to reset their account password via a time-limited email link without requiring current password knowledge. | | |
| Preconditions | The user account exists and is active. The user has access to their registered email address. | | |
| Postconditions | A password reset link is sent to the registered email. Reset token expires in 1 hour (BR-003). Password is updated on successful token verification. | | |
| Normal Flow | 1. User clicks 'Reset Password' on the Login screen.  2. System displays the email input form.  3. User enters their registered email address and submits.  4. System validates the email is active, generates a secure reset token (1-hour expiry per BR-003), and sends the reset link via Email Service.  5. User clicks the link, enters a new password and confirms it.  6. System validates token validity, matches password confirmation, enforces password policy (BR-006: must not match last 3 passwords), hashes the password (BR-001), saves it, invalidates all other active sessions, and displays MSG-030. | | |
| Alternative Flows | AF1 - Token Expired: User accesses an expired reset link. System displays token expiry error and prompts user to request a new reset link. | | |
| Exceptions | MSG-003 (Account deactivated). If email is invalid or rate-limit is exceeded, system displays appropriate validation warning. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-001, BR-003, BR-006 | | |
| Other Information | Rate-limiting applies: maximum 1 reset request per 15 minutes per email. | | |
| Assumptions | User has access to their registered email account. | | |

**Table 3.21: Forget Password Use Cases Specification**

#### 3.2.1.7 Login

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-027 - Login | | |
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

**Table 3.22: Login Use Cases Specification**

#### 3.2.1.8 Logout

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-028 - Logout | | |
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

**Table 3.23: Logout Use Cases Specification**

#### 3.2.1.9 Update Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-080 - Update Profile | | |
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

**Table 3.24: Update Profile Use Cases Specification**

#### 3.2.1.10 View Permission List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-107 - View Permission List | | |
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

**Table 3.25: View List Permission Use Cases Specification**

#### 3.2.1.11 View Role List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-108 - View Role List | | |
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

**Table 3.26: View List Role Use Cases Specification**

#### 3.2.1.12 View Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-115 - View Profile | | |
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

**Table 3.27: View Profile Use Cases Specification**

#### 3.2.1.13 View Staff Account List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-121 - View Staff Account List | | |
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

**Table 3.28: View Staff Account List Use Cases Specification**

#### 3.2.1.14 View User Account List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-129 - View User Account List | | |
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

**Table 3.29: View User Account List Use Cases Specification**

#### 3.2.1.15 Activate Account & Set Password

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-130 - Activate Account & Set Password | | |
| Created By | NamNVHE180863 | Date Created | 23/06/2026 |
| Primary Actor | User | Secondary Actors | Email Service |
| Trigger | User clicks the account activation link in the received verification email. | | |
| Description | Enables a new staff member or user to activate their account, set their initial password, and transition their account status from PENDING\_VERIFICATION to ACTIVE. | | |
| Preconditions | The target user account exists with status 'PENDING\_VERIFICATION'. The user has a valid, unused activation token. | | |
| Postconditions | The user account status is updated to 'ACTIVE'. The initial password is set and encrypted. The verification token is invalidated. The activation is logged in the audit trail. | | |
| Normal Flow | 1. User clicks the activation link in their email. 2. System extracts and validates the activation token: checking its validity and verifying that it has not expired (must be within 48 hours of creation per BR-011). 3. System displays the activation form with "New Password" and "Confirm Password" fields. 4. User enters their new password, confirms it, and clicks the "Activate Account" button. 5. System verifies that the new password matches the confirmation and validates password strength against the security policy (BR-006). 6. System hashes the password using BCrypt (BR-001) and saves it to the database. 7. System updates the account status to 'ACTIVE', invalidates the activation token, records the activation in the audit trail, displays MSG-031, and redirects the user to the Login screen. | | |
| Alternative Flows | AF1 - Activation Token Expired (> 48 hours): 1. User accesses an expired activation link. 2. System detects token expiration, displays an expiration error message, and provides a "Resend Activation Link" button. 3. User clicks "Resend Activation Link". 4. System generates a new activation token, extends it for another 48 hours, sends a new activation email via Email Service, and displays a success notification. | | |
| Exceptions | -�Invalid Token:�System blocks access and displays a validation error message. -�Account Already Active:�System redirects the user to the Login screen with an information message. | | |
| Priority | High | | |
| Frequency of Use | On-demand (when a new staff/user is onboarded) | | |
| Business Rules | BR-001, BR-006, BR-010, BR-011, BR-012 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with an internet connection and access to their registered email. | | |

**Table 3.30: Activate Account & Set Password**

### 3.2.2 School & Class Management

#### 3.2.2.1 Activate/Deactivate School

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-001 - Activate/Deactivate School | | |
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

**Table 3.31: Activate/Deactivate School Use Cases Specification**

#### 3.2.2.2 Assign Homeroom Teacher

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-005 - Assign Homeroom Teacher | | |
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

**Table 3.32: Assign Homeroom Teacher Use Cases Specification**

#### 3.2.2.3 Create Classroom

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-013 - Create Classroom | | |
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

**Table 3.33: Create Classroom Use Cases Specification**

#### 3.2.2.4 View Classroom Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-138 - View Classroom Detail | | |
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
| Business Rules | BR-020, BR-024, BR-025 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.34: View Classroom List Use Cases Specification**

#### 3.2.2.5 View Classroom List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-139 - View Classroom List | | |
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

**Table 3.35: View Classroom Detail Use Cases Specification**

#### 3.2.2.6 Register School

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-056 - Register School | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Guest | Secondary Actors | None |
| Trigger | Guest requests to register a school. | | |
| Description | Enables a Guest (school representative) to input, validate, and submit new school details for platform onboarding. | | |
| Preconditions | The registration form is publicly accessible. No prior authentication is required. | | |
| Postconditions | School registration details are saved. School status is set to Pending. Verification email is sent. Audit log entry is created. | | |
| Normal Flow | 1. Guest navigates to the Register School panel.  2. System displays the school registration form.  3. Guest enters school details: Name, Tax ID, Operational Permit, Official Email, Address, and Geographical Coordinates (latitude/longitude).  4. System validates the email address format, Tax ID format, and geographical coordinates (BR-028).  5. System saves the school record in 'Pending' status, sends a confirmation email to the official school email, creates an audit trail, and displays MSG-032. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-007 (Invalid tax ID format). If coordinates or email is invalid, system displays a validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-028 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.36: Register School Use Cases Specification**

#### 3.2.2.7 Update Classroom

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-073 - Update Classroom | | |
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

**Table 3.37: Update Classroom Use Cases Specification**

#### 3.2.2.8 Update School Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-078 - Update School Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | System Admin | Secondary Actors | None |
| Trigger | Primary actor requests to update information school. | | |
| Description | Enables the System Admin (platform-level) or School Administration (school-level) to modify school profile details and operational configuration fields. | | |
| Preconditions | The user is authenticated and authorized. The target school exists. | | |
| Postconditions | School profile details and operational configuration parameters are updated, saved, and audited. | | |
| Normal Flow | 1. User navigates to School Settings or Management panel and selects the target School to edit.  2. System displays the school details form: Name, Address, Coordinates, Tax ID, Official Email, and Operational Parameters.  3. User modifies details. If changing coordinates (BR-028), system validates and locks them for future receiving GPS checks.  4. If modifying school-level parameters, system verifies operational rules (BR-030). If edited by non-administrator staff, system flags for School Admin review.  5. System saves the updated details, records the changes in the audit trail, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If coordinate lock validation fails, system displays an error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-028, BR-030 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.38: Update School Information Use Cases Specification**

#### 3.2.2.9 View School Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-100 - View School Information | | |
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

**Table 3.39: View School Information Use Cases Specification**

#### 3.2.2.10 View School List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-117 - View School List | | |
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

**Table 3.40: View School List Use Cases Specification**

### 3.2.3 Student & Parent Management

#### 3.2.3.1 Bulk Import Student & Parent

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-007 - Bulk Import Student & Parent | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration navigates to Student Management and selects 'Bulk Import Students & Parents'. | | |
| Description | Enables the School Administration to bulk import student and parent records from a structured Excel or CSV file with automated validation and duplicate detection. | | |
| Preconditions | The school administration is authenticated and authorized. Import file is prepared using the system's template format. | | |
| Postconditions | Valid records are created. Duplicate and invalid records are reported in an error summary. Verification emails sent to new parent accounts. Notification channels configured. | | |
| Normal Flow | 1. School Administration navigates to Bulk Import and downloads the import template.  2. School Administration uploads the completed import file.  3. System validates each record: email format, phone format (BR-012, BR-032), duplicate student admission codes, and parent linkage.  4. System displays validation summary with valid, invalid, and duplicate record counts.  5. School Administration confirms. System imports valid records, sends verification emails to new parent accounts (BR-010), configures notification channels, and displays MSG-035. | | |
| Alternative Flows | AF1 - Validation Errors: System lists all invalid rows with specific error codes. School Administration corrects the file and re-uploads. | | |
| Exceptions | MSG-004, MSG-005, MSG-012 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-044 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.41: Bulk Import Student & Parent Use Cases Specification**

#### 3.2.3.2 Create Student

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-019 - Create Student | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to create student. | | |
| Description | Enables the School Administration to input, validate, and save new Student details into the system database. | | |
| Preconditions | The school administration is authenticated and authorized. The school is active. | | |
| Postconditions | The new student profile is created, saved to the database, and audited. Classroom assignment is validated. | | |
| Normal Flow | 1. School Administration navigates to Student Management and selects 'Create Student Profile'.  2. System displays the student creation form.  3. School Administration enters student details: full name, date of birth, grade level, and classroom assignment.  4. School Administration enters parent details: email address.  5. System searches for the parent email.  - If parent email exists: system displays parent details and links the new student profile (BR-031, BR-201: max 5 students).  - If parent email does not exist: School Administration inputs parent's name and phone number (BR-012) to create a new parent account.  6. System automatically generates a unique school registration number and a unique student admission code.  7. System validates that the classroom is active and has not reached its capacity limit of 50 students (BR-025).  8. System saves the student and parent profiles, links them, sends verification email for new parent accounts (BR-010), logs the creation in the audit trail, and displays MSG-031. | | |
| Alternative Flows | AF1 - Parent Limit Reached: The selected parent account already has 5 active students. System blocks linking and prompts School Administration to verify (BR-201). | | |
| Exceptions | MSG-004, MSG-005, MSG-010 (if classroom capacity is exceeded). If input details or phone format (BR-012) is invalid, system displays validation warning. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-012, BR-025, BR-031, BR-201 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.42: Create Student Use Cases Specification**

#### 3.2.3.3 View Allergy & Special Meal List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-029 - View Allergy & Special Meal List | | |
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

**Table 3.43: View Allergy & Special Meal List Use Cases Specification**

#### 3.2.3.4 View Student Health Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-041 - View Student Health Information | | |
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

**Table 3.44: View Student Health Information Use Cases Specification**

#### 3.2.3.5 View Student Health Record List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-042 - View Student Health Record List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to view list student health records. | | |
| Description | Enables the Medical Staff to search, retrieve, and append clinical records, medications, and health histories for students. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Student health records are retrieved and displayed. Append actions are enabled. Existing verified history logs are protected from modifications (BR-172). | | |
| Normal Flow | 1. Medical Staff navigates to Student Health Management.  2. System displays search filters (by school, grade, class, student name, status).  3. Medical Staff searches and selects a student's health record file.  4. System retrieves and displays the student's clinical file (medical conditions, medications, emergency contacts, health history).  5. Medical Staff can append annotations, record new clinical logs (which cannot be deleted BR-172), or configure emergency notes for offline access. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-172 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.45: View Student Health Record List Use Cases Specification**

#### 3.2.3.6 Review Health Information Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-063 - Review Health Information Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to review health information. | | |
| Description | Enables the Medical Staff to verify and approve pending student health and allergy declarations. | | |
| Preconditions | The medical staff is authenticated and authorized to access this feature. | | |
| Postconditions | Student health information status is updated. Approved profiles become active. | | |
| Normal Flow | 1. Medical Staff navigates to the Health Review panel.  2. System queries the database and displays a list of pending student health and allergy declarations.  3. Medical Staff selects a pending record to review.  4. System displays the declaration details (allergy type, severity, uploaded medical documentation).  5. Medical Staff verifies the medical documentation (BR-031). If documentation is valid and matches the declaration, Medical Staff clicks 'Approve'.  6. System updates the status of the declaration to 'Verified' (BR-031), activates the student's health profile, and displays MSG-036. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If documentation is missing or invalid, Medical Staff rejects the request and specifies the reason. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.46: Review Health Information Use Cases Specification**

#### 3.2.3.7 Review Special Meal Requests

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-064 - Review Special Meal Requests | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Primary actor requests to review special meal requests. | | |
| Description | Enables the Medical Staff to review, verify, and approve custom dietary requests submitted by parents, routing valid setups to the kitchen. | | |
| Preconditions | The medical staff is authenticated and authorized. Pending special meal requests exist. | | |
| Postconditions | Special meal requests are verified. Student's special meal status is updated. Requirements routed to Service Caterers. | | |
| Normal Flow | 1. Medical Staff navigate to the Special Meal Requests section.  2. System retrieves and displays the list of pending special meal requests submitted by parents.  3. Medical Staff selects a request to review and inspects the details (dietary type, duration, medical justification).  4. Medical Staff checks alignment with standard kitchen categories (Halal, Vegetarian, Gluten-Free, Nut-Free per BR-066).  5. Medical Staff approves the request. System updates the student's special meal status, routes the requirement to the kitchen/Service Caterers, and displays success confirmation. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If request details violate rules or standard categories, Medical Staff rejects request and specifies reason. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-031, BR-066 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.47: Review Special Meal Requests Use Cases Specification**

#### 3.2.3.8 Submit Health & Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-069 - Submit Health & Allergy Information | | |
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

**Table 3.48: Submit Health & Allergy Information Use Cases Specification**

#### 3.2.3.9 Update Health & Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-076 - Update Health & Allergy Information | | |
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

**Table 3.49: Update Health & Allergy Information Use Cases Specification**

#### 3.2.3.10 Update Health Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-077 - Update Health Records | | |
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

**Table 3.50: Update Health Records Use Cases Specification**

#### 3.2.3.11 Update Student

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-081 - Update Student | | |
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

**Table 3.51: Update Student Use Cases Specification**

#### 3.2.3.12 View Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-086 - View Allergy Information | | |
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

**Table 3.52: View Allergy Information Use Cases Specification**

#### 3.2.3.13 View Student Health & Allergy Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-122 - View Student Health & Allergy Information | | |
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

**Table 3.53: View Student Health & Allergy Information Use Cases Specification**

#### 3.2.3.14 View Student Health History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-123 - View Student Health History | | |
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

**Table 3.54: View Student Health History Use Cases Specification**

#### 3.2.3.15 View Student List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-124 - View Student List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view student list. | | |
| Description | Enables the School Administration to safely query, view, or export the details of Student List records. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | Student list is retrieved and displayed based on role permissions (BR-025). | | |
| Normal Flow | 1. School Administration or Teacher navigates to the Student List section in the system portal.  2. System queries about the database to retrieve student profiles  3. System displays the list of students showing: Name, school registration number, Class, and Parent Contact details.  4. User can search by name, filter by class/grade, or export the list of students to Excel/CSV. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-025 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.55: View Student List Use Cases Specification**

### 3.2.4 Supplier & Partner Management

#### 3.2.4.1 Add Delivery Person

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-014 - Add Delivery Person | | |
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

**Table 3.56: Add Delivery Person Use Cases Specification**

#### 3.2.4.2 Delete Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-022 - Delete Face Data | | |
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

**Table 3.57: Delete Face Data Use Cases Specification**

#### 3.2.4.3 Manage Compliance Documents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-140 - Manage Compliance Documents | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to view and manage the list of their compliance documents. | | |
| Description | Enables the Raw Food Supplier to view, search, filter, and access the full list of their uploaded compliance documents including food safety certificates, business licenses, and product origin documents. From this panel, the supplier can initiate individual document upload or update actions. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The manage compliance documents transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Compliance Documents section.  2. System retrieves and displays the list of all compliance documents associated with the supplier's account.  3. System displays each document's name, type, upload date, expiration date, and status (Valid / Expiring Soon / Expired).  4. Raw Food Supplier can filter documents by type or status.  5. Raw Food Supplier selects a document to view its detail or initiate an update or replacement action. | | |
| Alternative Flows | AF1 - No Documents Uploaded: The supplier has no compliance documents on record. System displays an empty state message with a prompt to upload the first document (UC-103).  AF2 - Expiring Documents Alert: One or more documents expire within 180 days. System highlights those documents with an 'Expiring Soon' badge; if fewer than 30 days remain and the document is still unrenewed, the badge escalates to 'Urgent' (BR-047). | | |
| Exceptions | MSG-004, MSG-005, MSG-009 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.58: Manage Compliance Documents Use Cases Specification**

#### 3.2.4.4 Manage Profile

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-040 - Manage Profile | | |
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

**Table 3.59: Manage Profile Use Cases Specification**

#### 3.2.4.5 Register Service Caterer Account

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-053 - Register Service Caterer Account | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Guest | Secondary Actors | Email Service |
| Trigger | Guest navigates to the platform and selects 'Register as Service Caterers'. | | |
| Description | Enables a Guest (prospective Service Caterers) to submit a registration application including company details and required compliance documentation for school approval. | | |
| Preconditions | Registration form is publicly accessible. Email address not previously registered. No authentication required. | | |
| Postconditions | Registration request submitted with 'Pending Approval' status. Verification email sent (BR-010). School Administration notified for review. Site inspection score must be >= 80% for approval. | | |
| Normal Flow | 1. Guest navigates to 'Register as Service Caterers' page.  2. System displays registration form with required company information fields.  3. Guest enters company name, business license, official email, address, and contact details.  4. System validates email uniqueness, required field completeness, and format constraints.  5. System creates the pending registration record, sends verification email (BR-010), and notifies School Administration for review. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-006 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.60: Register Service Caterer Account Use Cases Specification**

#### 3.2.4.6 Register Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-054 - Register Face Data | | |
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

**Table 3.61: Register Face Data Use Cases Specification**

#### 3.2.4.7 Register Supplier Account

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-057 - Register Supplier Account | | |
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

**Table 3.62: Register Supplier Account Use Cases Specification**

#### 3.2.4.8 Update Compliance Documents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-074 - Update Compliance Documents | | |
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

**Table 3.63: Update Compliance Documents Use Cases Specification**

#### 3.2.4.9 Upload Food Safety Certificates

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-083 - Upload Food Safety Certificates | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to upload food safety certificates. | | |
| Description | Enables the Raw Food Supplier to upload a new food safety certificate to the system. The system validates the document format and tracks the expiration date to ensure compliance�. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The food safety certificate is saved to the database with its expiration date tracked. The system evaluates whether the new certificate resolves any previous compliance block (BR-048). The upload event is audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Upload Food Safety Certificates panel.  2. System displays the upload form with fields for certificate name, issuing authority, issue date, and expiration date.  3. Raw Food Supplier selects the certificate file and fills in the required fields.  4. System validates the file format, required fields, and expiration date.  5. System saves the certificate record, tracks the expiration date (BR-047), and displays a success confirmation. | | |
| Alternative Flows | AF1 - Expired Certificate Uploaded: The expiration date entered is already past. System displays MSG-013 and blocks the upload. Supplier must upload a valid, non-expired certificate.  AF2 - Certificate Nearing Expiry: The expiration date is within 30 days from today. System accepts the upload but displays a warning that the certificate will expire soon and a renewal will be required (BR-047).  AF3 - Duplicate Certificate: A certificate of the same type and issuing authority already exists. System warns the supplier and asks whether to replace the existing record or add as a supplementary certificate. | | |
| Exceptions | MSG-004, MSG-005, MSG-009 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.64: Upload Food Safety Certificates Use Cases Specification**

#### 3.2.4.10 Upload Product Origin Documents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-084 - Upload Product Origin Documents | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to upload product origin documents. | | |
| Description | Enables the Raw Food Supplier to upload a new product origin document (such as a certificate of origin, VietGAP certification, or organic certification) to the system to support ingredient traceability requirements. | | |
| Preconditions | The raw food supplier is authenticated and authorized to access this feature. | | |
| Postconditions | The upload product origin documents transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to the Upload Product Origin Documents panel.  2. System displays the upload form with fields for document name, document type, issuing authority, and expiration date (if applicable).  3. Raw Food Supplier selects the document file and fills in the required fields.  4. System validates the file format and required fields.  5. System saves the product origin document record and displays a success confirmation. | | |
| Alternative Flows | AF1 - Invalid File Format: The uploaded file does not meet the accepted format requirements. System displays MSG-009 and prompts the supplier to upload a valid file.  AF2 - Supplier Cancels: Raw Food Supplier cancels before submitting. System discards the selected file and returns to the document list. | | |
| Exceptions | MSG-004, MSG-005, MSG-009 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.65: Upload Product Origin Documents Use Cases Specification**

#### 3.2.4.11 View Delivery Person List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-091 - View Delivery Person List | | |
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

**Table 3.66: View Delivery Person List Use Cases Specification**

#### 3.2.4.12 View Service Caterer List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-105 - View Service Caterer List | | |
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
| Business Rules | BR-052, BR-053 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.67: View Service Caterer List Use Cases Specification**

#### 3.2.4.13 View Supplier List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-109 - View Supplier List | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view the list of all registered raw food suppliers. | | |
| Description | Enables the School Administration to query and view the full list of raw food suppliers registered in the system, including their approval status, certificate validity, and performance index summary. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The supplier list data is successfully retrieved and displayed to the school administration. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to the Supplier management section.  2. System retrieves and displays the list of all registered suppliers with their company name, approval status, certificate validity, and latest performance index.  3. School Administration can filter the list by status (Pending, Approved, Rejected, Flagged) or search by company name.  4. School Administration can select a supplier to view full detail | | |
| Alternative Flows | AF1 - No Suppliers Found: No suppliers match the applied filter criteria. System displays an empty result message and prompts to clear filters.  AF2 - Flagged Suppliers Present: One or more suppliers are flagged for suspension. System highlights those entries with a 'Performance Warning' badge | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-052, BR-053 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.68: View Supplier List Use Cases Specification**

#### 3.2.4.14 View Supplier Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-125 - View Supplier Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | Primary actor requests to view supplier detail. | | |
| Description | Enables the School Administration to view the complete detail profile of a specific raw food supplier, including company information, approval status, compliance documents, certificate expiration dates, delivery history, and monthly performance index score | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. The target supplier record exists in the system. | | |
| Postconditions | The supplier detail data is successfully retrieved and displayed to the school administration. No data is modified. | | |
| Normal Flow | 1. School Administration navigates to the Supplier management section and selects a specific supplier.  2. System retrieves and displays the supplier's full detail profile including: company name, contact information, approval status, uploaded compliance documents with expiration dates, delivery history summary, and monthly performance index score.  3. School Administration reviews the supplier detail.  4. If the supplier's performance index is below 70% for two consecutive months, System displays a 'Performance Warning' alert indicating the supplier is flagged for review. | | |
| Alternative Flows | AF1 - Record Not Found: The requested supplier record cannot be found. System displays MSG-004.  AF2 - Certificates Expiring Soon: One or more certificates are expiring within 180 days. System highlights those certificates with an 'Expiring Soon' indicator; if fewer than 30 days remain and still unrenewed, the indicator escalates to 'Urgent' (BR-047). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-052, BR-053 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.69: View Supplier Detail Use Cases Specification**

#### 3.2.4.15 Submit Supplier Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-128 - Submit Supplier Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Email Service |
| Trigger | Raw Food Supplier requests to submit a supplier application. | | |
| Description | Enables the Raw Food Supplier to submit their company profile, tax information, business license, and food safety certificates to apply for school onboarding. | | |
| Preconditions | The Raw Food Supplier is authenticated, logged in, and does not have an active application. | | |
| Postconditions | The application is created and saved in the database with status 'Pending Approval'. The School Administration is notified and a confirmation email is sent to the supplier. | | |
| Normal Flow | 1. Supplier navigates to the "Apply for Onboarding" interface.  2. System displays the application form requesting company details (name, tax ID, address), business license, and food safety certifications.  3. Supplier inputs company details and uploads business license and certificates.  4. Supplier clicks "Submit".  5. System validates the inputs (e.g., non-empty fields, valid tax ID format per MSG-007, and non-expired certificates per BR-046).  6. System saves the application with status 'Pending Approval' and logs the event.  7. System sends a confirmation email to the supplier (BR-010) and notifies the School Administration.  8. System displays a success message (MSG-028). | | |
| Alternative Flows | AF1 - Expired/Invalid Certificates: One or more uploaded food safety certificates have expired (BR-048). System displays MSG-013 and blocks submission. | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has a stable internet connection and necessary company documents. | | |

**Table 3.70: Submit Supplier Application Use Cases Specification**

#### 3.2.4.16 Track Supplier Application Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-141 - Track Supplier Application Status | | |
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

**Table 3.71: Track Supplier Application Status**

#### 3.2.4.17 Update/Resubmit Supplier Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-142 - Update/Resubmit Supplier Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | Email Service |
| Trigger | Raw Food Supplier requests to update and resubmit a rejected or incomplete application. | | |
| Description | The Raw Food Supplier is authenticated and their application status is 'Rejected' or 'Action Required'. | | |
| Preconditions | The Raw Food Supplier is authenticated and has previously submitted an onboarding application. | | |
| Postconditions | The application is updated, status is reset to 'Pending Approval', and School Admin is notified. | | |
| Normal Flow | 1. Supplier navigates to the "Application Status" section and clicks "Edit/Update Application".  2. System displays the form pre-filled with previous inputs and displays comments from the School Administration explaining the issues.  3. Supplier updates the required fields or uploads new files.  4. Supplier clicks "Resubmit".  5. System validates the updated inputs (BR-046).  6. System updates the database record with the new details and resets status to 'Pending Approval'.  7. System sends a notification email to the supplier (BR-010) and notifies the School Admin.  8. System displays a success message (MSG-029). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046, BR-047, BR-048 | | |
| Other Information | None. | | |
| Assumptions | User has corrected the documents as requested by the School Administration. | | |

**Table 3.72: Update/Resubmit Supplier Application**

#### 3.2.4.18 Manage Supplier Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-131 - Manage Supplier Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Raw Food Supplier logs in and accesses their application dashboard. | | |
| Description | The main dashboard use case that allows the Raw Food Supplier to manage their onboarding application flow, directing them to submit, track, or update their application. | | |
| Preconditions | The Raw Food Supplier is authenticated and logged in | | |
| Postconditions | The system presents options corresponding to the current state of their application. | | |
| Normal Flow | 1. Supplier navigates to the "Onboarding Portal".  2. System checks the state of the supplier's application in the database.  3. System displays available actions based on application state: "Submit Application" (if no application exists), "View Application Status" (if an application is pending or approved), or "Edit and Resubmit Application" (if rejected or action required).  4. Supplier selects an option.  5. System routes the user to the corresponding use case (UC-152, UC-153, or UC-154). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | None | | |

**Table 3.73: Manage Supplier Application**

#### 3.2.4.19 Submit Catering Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-132 - Submit Catering Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Email Service |
| Trigger | Service Caterers actor requests to submit a catering application. | | |
| Description | Enables the Service Caterers to submit company profiles, safety certifications, and kitchen capability details to apply for school catering contracts. | | |
| Preconditions | The Service Caterers representative is authenticated, logged in, and does not have an active application. | | |
| Postconditions | The application is created and saved in the database with status 'Pending Approval'. The School Administration is notified and a confirmation email is sent. | | |
| Normal Flow | 1. Caterer navigates to the "Apply for Onboarding" interface.  2. System displays the application form requesting company details (name, tax ID, address), kitchen sanitation details, and food safety certifications.  3. Caterer inputs details and uploads food safety certificates.  4. Caterer clicks "Submit".  5. System validates the inputs (e.g., non-empty fields, valid tax ID format per MSG-007, and non-expired certificates per BR-046).  6. System saves the application with status 'Pending Approval' and logs the event.  7. System sends a confirmation email to the caterer (BR-010) and notifies the School Administration.  8. System displays a success message (MSG-028). | | |
| Alternative Flows | AF1 - Expired/Invalid Certificates: One or more uploaded food safety certificates have expired (BR-048). System displays MSG-013 and blocks submission. | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has a stable internet connection and necessary documents. Site inspection rating will be required later. | | |

**Table 3.74: Submit Catering Application**

#### 3.2.4.20 Track Catering Application Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-133 - Track Catering Application Status | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to check the status of their catering application. | | |
| Description | Allows the Service Caterers to view the current status of their onboarding application, comments from the School Admin, and site inspection rating. | | |
| Preconditions | The Service Caterers representative is authenticated and has previously submitted an onboarding application. | | |
| Postconditions | System retrieves and displays the current application status, comments, and site inspection score. | | |
| Normal Flow | 1. Caterer navigates to the "Application Status" section.  2. System retrieves the caterer's application record from the database.  3. System displays the current status (e.g., Pending, Approved, Rejected, Action Required), site inspection score (if conducted), and comments from the School Administration.  4. Caterer views the details. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-049 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.75: Track Catering Application Status**

#### 3.2.4.21 Update/Resubmit Catering Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-134 - Update/Resubmit Catering Application | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Email Service |
| Trigger | Service Caterers requests to update and resubmit a rejected or incomplete application. | | |
| Description | Enables the Service Caterers to update application details, fix missing documents, and resubmit the application for School Admin evaluation. | | |
| Preconditions | The Service Caterers representative is authenticated and their application status is 'Rejected' or 'Action Required'. | | |
| Postconditions | The application is updated, status is reset to 'Pending Approval', and School Admin is notified. | | |
| Normal Flow | 1. Caterer navigates to the "Application Status" section and clicks "Edit/Update Application".  2. System displays the form pre-filled with previous inputs and displays comments from the School Admin.  3. Caterer updates the required fields or uploads new files.  4. Caterer clicks "Resubmit".  5. System validates the updated inputs.  6. System updates the database record with the new details and resets status to 'Pending Approval'.  7. System sends a notification email to the caterer (BR-010) and notifies the School Admin.  8. System displays a success message (MSG-029). | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025 | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-010, BR-046 | | |
| Other Information | None. | | |
| Assumptions | User has corrected the documents as requested by the School Administration. | | |

**Table 3.76: Update/Resubmit Catering Application**

#### 3.2.4.22 Manage Catering Application

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-135 - Manage Catering Application | | |
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

**Table 3.77: Manage Catering Application**

#### 3.2.4.23 Add Supplier

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-143 - Add Supplier | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects "Add Supplier" from the Supplier List screen to register a new raw food supplier manually. | | |
| Description | Allows School Administration to manually create a supplier record for a raw food supplier that will work with the school, without requiring the supplier to self-register. | | |
| Preconditions | The school administration is authenticated and authorized. | | |
| Postconditions | A new supplier record is created and appears in the Supplier List. Audit log records the action. | | |
| Normal Flow | 1. School Administration navigates to the Supplier List and selects "Add Supplier". 2. System displays the supplier creation form. 3. School Administration enters the supplier's information (name, contact details, address). 4. School Administration submits the form. 5. System validates the input, creates the supplier record, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If required fields are missing or invalid, system displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.78: Add Supplier**

**Table 3.79: Table: Add Supplier Use Cases Specification**

#### 3.2.4.24 View Catering Company Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-144 - View Catering Company Detail | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects a service caterer from the Service Caterer List to view its details. | | |
| Description | Allows School Administration to view the comprehensive details of a specific service caterer, including its profile, contact information, and application/approval status. | | |
| Preconditions | The school administration is authenticated and authorized. The target service caterer exists. | | |
| Postconditions | The system displays the selected service caterer's detailed information. | | |
| Normal Flow | 1. School Administration navigates to the Service Caterer List. 2. School Administration selects a service caterer from the list. 3. System retrieves and displays the service caterer's detailed profile and status. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If the service caterer no longer exists, system displays an error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.80: View Catering Company Detail**

**Table 3.81: Table: View Catering Company Detail Use Cases Specification**

#### 3.2.4.25 Add Service Caterer

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-145 - Add Service Caterer | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration selects "Add Service Caterer" from the Service Caterer List screen to register a new service caterer manually. | | |
| Description | Allows School Administration to manually create a service caterer record for a catering company that will work with the school, without requiring the caterer to self-register. | | |
| Preconditions | The school administration is authenticated and authorized. | | |
| Postconditions | A new service caterer record is created and appears in the Service Caterer List. Audit log records the action. | | |
| Normal Flow | 1. School Administration navigates to the Service Caterer List and selects "Add Service Caterer". 2. System displays the service caterer creation form. 3. School Administration enters the caterer's information (company name, contact details, address). 4. School Administration submits the form. 5. System validates the input, creates the service caterer record, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If required fields are missing or invalid, system displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | None | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.82: Add Service Caterer**

**Table 3.83: Table: Add Service Caterer Use Cases Specification**

### 3.2.5 Menu & Diet Management

#### 3.2.5.1 Approve/Reject Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-004 - Approve/Reject Menu | | |
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

**Table 3.84: Approve/Reject Menu Use Cases Specification**

#### 3.2.5.2 Create Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-015 - Create Menu | | |
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

**Table 3.85: Create Menu Use Cases Specification**

#### 3.2.5.3 Create Special Meal Plan

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-017 - Create Special Meal Plan | | |
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

**Table 3.86: Create Special Meal Plan Use Cases Specification**

#### 3.2.5.4 Manage Menus

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-039 - Manage Menus | | |
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

**Table 3.87: Manage Menus Use Cases Specification**

#### 3.2.5.5 Request AI Menu Recommendations

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-061 - Request AI Menu Recommendations | | |
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

**Table 3.88: Request AI Menu Recommendations Use Cases Specification**

#### 3.2.5.6 Submit Menu For Approval

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-071 - Submit Menu For Approval | | |
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

**Table 3.89: Submit Menu For Approval Use Cases Specification**

#### 3.2.5.7 Update Menu

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-079 - Update Menu | | |
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

**Table 3.90: Update Menu Use Cases Specification**

#### 3.2.5.8 View Menu List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-106 - View Menu List | | |
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

**Table 3.91: View Menu List Use Cases Specification**

#### 3.2.5.9 View Menu Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-112 - View Menu Detail | | |
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

**Table 3.92: View Menu Detail Use Cases Specification**

#### 3.2.5.10 View Menu Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-113 - View Menu Information | | |
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

**Table 3.93: View Menu Information Use Cases Specification**

### 3.2.6 Ingredient & Inventory Management

#### 3.2.6.1 Confirm Delivery Schedule

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-010 - Confirm Delivery Schedule | | |
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

**Table 3.94: Confirm Delivery Schedule Use Cases Specification**

#### 3.2.6.2 Create Purchase Request

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-016 - Create Purchase Request | | |
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

**Table 3.95: Create Purchase Request Use Cases Specification**

#### 3.2.6.3 Manage Deliveries

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-030 - Manage Deliveries | | |
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

**Table 3.96: Manage Deliveries Use Cases Specification**

#### 3.2.6.4 Manage Ingredient Batches

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-034 - Manage Ingredient Batches | | |
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
| Business Rules | BR-087 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.97: Manage Ingredient Batches Use Cases Specification**

#### 3.2.6.5 Manage Ingredient Receiving

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-035 - Manage Ingredient Receiving | | |
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

**Table 3.98: Manage Ingredient Receiving Use Cases Specification**

#### 3.2.6.6 Manage Inventory

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-036 - Manage Inventory | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers navigates to Inventory Management to view stock, issue ingredients, or process adjustments. | | |
| Description | Enables the Service Caterers to manage inventory: view stock levels, issue ingredients following FEFO principles, record adjustments for spoilage, and monitor expiration and shortage alerts. | | |
| Preconditions | Service Caterers is authenticated and authorized. Inventory records exist in the system. | | |
| Postconditions | Inventory accurately updated. FEFO issuance maintained (BR-076). Adjustments logged with reason codes and sign-off (BR-204). Expiration alerts triggered (MSG-052) as applicable. | | |
| Normal Flow | 1. Service Caterers navigates to Inventory Management.  2. System displays stock levels grouped by ingredient, storage zone, and batch expiration date.  3. Service Caterers reviews stock and selects ingredients for issuance or adjustment.  4. System validates FEFO principle (BR-076), storage zone matching (BR-202), and blocks expired ingredient selection (BR-205).  5. System updates inventory balances, records the transaction (MSG-042), and triggers expiration alerts (MSG-052) as applicable. | | |
| Alternative Flows | AF1 - Spoilage Write-off: Service Caterers records spoilage adjustment. System requires mandatory reason code and manager sign-off (BR-204).  AF2 - Inventory Shortage: System detects insufficient stock for required ingredient and displays MSG-018. | | |
| Exceptions | MSG-004, MSG-005, MSG-018, MSG-052 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-076, BR-202, BR-204, BR-205, BR-089, BR-090 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.99: Manage Inventory Use Cases Specification**

#### 3.2.6.7 Record Ingredient Receiving

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-050 - Record Ingredient Receiving | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | Face Recognition Service |
| Trigger | Service Caterers starts a receiving session when a supplier delivery arrives at the school. | | |
| Description | Enables the Service Caterers to record a complete ingredient receiving session with batch verification, quality inspection, GPS validation, and photographic evidence capture. | | |
| Preconditions | Service Caterers is authenticated and authorized. Delivery is within the scheduled window (BR-078). GPS enabled on device. | | |
| Postconditions | Ingredient batch record created with full traceability (supplier, batch, GPS, timestamp, photos). Inventory updated. Rejected batches excluded and supplier notified. | | |
| Normal Flow | 1. Service Caterers initiates a new receiving session for an arriving delivery.  2. System verifies delivery is within the active schedule window (BR-078) and validates GPS within 50m of school gate (BR-079).  3. Service Caterers scans delivery person's face. Face Recognition Service validates driver identity against registered delivery personnel profile (BR-050).  4. Service Caterers reviews batch info (BR-071), conducts quality inspection, and records temperature checks of cold chain items (BR-080).  5. Service Caterers uploads mandatory evidence photos. System records GPS and timestamp automatically.  6. System creates batch traceability record, updates inventory, generates receiving record, and displays MSG-041. | | |
| Alternative Flows | AF1 - Batch Rejection: Batch fails inspection (e.g., cold chain temperature above 5�C per BR-080). Service Caterers selects 'Reject'. System logs rejection, notifies supplier, excludes batch from inventory. | | |
| Exceptions | MSG-004, MSG-005, MSG-015 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-050, BR-078, BR-079, BR-080, BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and GPS-enabled device. | | |

**Table 3.100: Record Ingredient Receiving Use Cases Specification**

#### 3.2.6.8 Record Ingredient Storage

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-051 - Record Ingredient Storage | | |
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

**Table 3.101: Record Ingredient Storage Use Cases Specification**

#### 3.2.6.9 Record Inventory Transactions

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-052 - Record Inventory Transactions | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers needs to record manual stock adjustments (spoilage, waste, audit write-off). | | |
| Description | Enables the Service Caterers to record manual stock transactions, requiring mandatory reason codes and manager sign-off for adjustments. | | |
| Preconditions | The Service Caterers is authenticated and authorized. | | |
| Postconditions | Inventory adjusted and transaction recorded in audit logs. | | |
| Normal Flow | 1. Service Caterers navigates to Inventory Adjustments.  2. Service Caterers selects ingredient batch, enters adjusted quantity, and selects a mandatory reason code (BR-204).  3. Service Caterers uploads manager sign-off authentication (BR-204).  4. System records the transaction (MSG-042) and updates the stock balances. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If reason code or sign-off is missing (BR-204), system blocks transaction | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-204 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.102: Record Inventory Transactions Use Cases Specification**

#### 3.2.6.10 Reject Ingredient Batch

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-058 - Reject Ingredient Batch | | |
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

**Table 3.103: Reject Ingredient Batch Use Cases Specification**

#### 3.2.6.11 Submit Batch Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-067 - Submit Batch Information | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Raw Food Supplier | Secondary Actors | None |
| Trigger | Primary actor requests to submit batch information. | | |
| Description | Enables the Raw Food Supplier to register batch details including lot number, manufacturing date, packaging date, and certificate links in the database. | | |
| Preconditions | The raw food supplier is authenticated and authorized. Supplier's certifications are active (BR-048). | | |
| Postconditions | The submit batch information transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. Raw Food Supplier navigates to Submit Batch Information.  2. Supplier enters product name, lot number, manufacturing date, packaging date, and expiration date (BR-071).  3. Supplier uploads links to safety certificates (BR-046).  4. System validates dates and registers the batch in the system. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-009 (blocked if supplier's certifications are expired per BR-048). | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-046, BR-047, BR-048, BR-071 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.104: Submit Batch Information Use Cases Specification**

#### 3.2.6.12 Submit Delivery Information

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-068 - Submit Delivery Information | | |
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

**Table 3.105: Submit Delivery Information Use Cases Specification**

#### 3.2.6.13 Update Delivery Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-075 - Update Delivery Status | | |
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

**Table 3.106: Update Delivery Status Use Cases Specification**

#### 3.2.6.14 Upload Batch Certificates

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-082 - Upload Batch Certificates | | |
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
| Business Rules | BR-087 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.107: Upload Batch Certificates Use Cases Specification**

#### 3.2.6.15 View Batch History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-089 - View Batch History | | |
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
| Business Rules | BR-087 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.108: View Batch History Use Cases Specification**

#### 3.2.6.16 View Delivery Batch Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-090 - View Delivery Batch Records | | |
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

**Table 3.109: View Delivery Batch Records Use Cases Specification**

#### 3.2.6.17 View Ingredient Receiving Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-101 - View Ingredient Receiving Records | | |
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

**Table 3.110: View Ingredient Receiving Records Use Cases Specification**

#### 3.2.6.18 View Purchase Requests

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-116 - View Purchase Requests | | |
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

**Table 3.111: View Purchase Requests Use Cases Specification**

### 3.2.7 Food Safety Inspection & Monitoring

#### 3.2.7.1 Monitor Daily Food Safety

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-021 - Monitor Daily Food Safety | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | School Administration | Secondary Actors | None |
| Trigger | School Administration requests to monitor daily food safety logs. | | |
| Description | Enables the School Administration to monitor the daily three-step food safety inspection progress and review daily hygiene checklist scores submitted by the Service Caterers. | | |
| Preconditions | The school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The daily food safety monitoring transaction is completed, saved to the database, and audited. | | |
| Normal Flow | 1. School Administration navigates to the Monitor Daily Food Safety dashboard.  2. System displays the real-time status of today's meal production batches and their three-step inspection steps (Step 1, Step 2, Step 3) (BR-091).  3. System displays the daily kitchen hygiene checklist score submitted by the Service Caterers (BR-100, BR-104).  4. School Administration reviews the compliance scores and verification photos.  5. School Administration signs off on the daily safety status, and the system logs the monitoring review event. | | |
| Alternative Flows | AF1 - Pending or Failed Inspection Alert:  1. System detects a failed step (BR-097) or an overdue inspection. 2. System highlights the warning on the dashboard and displays the active corrective action request.  3. School Administration selects the alert to view non-conformance details and contacts the Service Caterers. | | |
| Exceptions | MSG-004, MSG-005, MSG-024 | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-091, BR-097, BR-100, BR-104, BR-105 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.112: Monitor Daily Food Safety Use Cases Specification**

#### 3.2.7.2 Conduct Three-Step Food Inspection

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-043 - Conduct Three-Step Food Inspection | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers initiates a food safety inspection session for an active meal production batch. | | |
| Description | Enables the Service Caterers to conduct the mandatory three-step food safety inspection in compliance with Decision 1246/Q?-BYT, recording checklist results, temperature measurements, GPS, timestamps, and photographic evidence for Step 1 (Sample No. 1), Step 2 (Sample No. 2), and Step 3 (Sample No. 3). | | |
| Preconditions | Service Caterers is authenticated and authorized. Meal production session is active. GPS enabled on device. | | |
| Postconditions | All three inspection steps completed and co-signed digitally (BR-099). Meals approved for serving if all steps pass. Corrective action triggered on failure (BR-097). | | |
| Normal Flow | 1. Service Caterers initiates a Three-Step Food Safety Inspection session.  2. Step 1 (Pre-processing inspection - Sample No. 1):System retrieves Step 1 data from UC-064. Service Caterers reviews and confirms that the raw ingredients and dry products pass inspection checklists, veterinary certificates, and sensory checks.  3. Step 2 (Pre-processing inspection - Sample No. 2):Service Caterers fills out the Sample 2 form: records meal slot, dish names, main ingredients/quantities, portion count, preprocessing and cooking end times, and checks sanitary conditions (staff, equipment, prep area) and sensory characteristics of cooking food. Verifies core temperature is >= 75�C for 15 seconds (BR-090). System locks Step 3 until Step 2 is marked 'Passed'.  4. Step 3 (Pre-processing inspection - Sample No. 3):Service Caterers fills out the Sample 3 form: records portion count, portion division end time, meal start time, description of containers/utensils (covered, hygienic), holding temperature >= 60�C (BR-091), and sensory check before eating.  5. System generates the consolidated digital three-step inspection record, prompts both the Catering Manager and the School Representative to sign digitally (BR-099), marks the meal batch as approved for serving, and displays MSG-043. | | |
| Alternative Flows | AF1 - Inspection Step Failure: Any step fails inspection. System blocks meal release (BR-097), logs non-conformance, creates corrective action request, displays MSG-056. Re-inspection required after corrective action (BR-098). | | |
| Exceptions | MSG-004, MSG-005, MSG-016, MSG-017, MSG-056 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-090, BR-090, BR-090, BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and GPS-enabled device. | | |

**Table 3.113: Conduct Three-Step Food Inspection Use Cases Specification**

#### 3.2.7.3 Monitor Food Safety

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-045 - Monitor Food Safety | | |
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

**Table 3.114: Monitor Food Safety Use Cases Specification**

#### 3.2.7.4 View Three-Step Food Inspection

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-093 - View Three-Step Food Inspection | | |
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
| Business Rules | BR-090, BR-090, BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.115: View Food Inspection Records Use Cases Specification**

#### 3.2.7.5 View Food Inspection Reports

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-094 - View Food Inspection Reports | | |
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

**Table 3.116: View Food Inspection Reports Use Cases Specification**

#### 3.2.7.6 View Food Safety Compliance Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-096 - View Food Safety Compliance Report | | |
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

**Table 3.117: View Food Safety Compliance Report Use Cases Specification**

#### 3.2.7.7 View Food Safety Reports

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-098 - View Food Safety Reports | | |
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

**Table 3.118: View Food Safety Reports Use Cases Specification**

#### 3.2.7.8 View Inspection Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-102 - View Inspection Records | | |
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
| Business Rules | BR-090, BR-090, BR-091, BR-092, BR-093, BR-094, BR-096, BR-097, BR-098, BR-099 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.119: View Inspection Records Use Cases Specification**

#### 3.2.7.9 Execute Kitchen Checklist

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-146 - Execute Kitchen Checklist | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff opens the daily inspection checklist form at the start of the food safety inspection routine. | | |
| Description | Enables Medical Staff to record the results of the standard daily food safety inspection checklist covering kitchen hygiene, storage conditions, and staff compliance. | | |
| Preconditions | The medical staff is authenticated and authorized. | | |
| Postconditions | The completed checklist is saved and linked to the daily inspection record. Audit log records the action. | | |
| Normal Flow | 1. Medical Staff navigates to Daily Food Safety Monitoring and selects "Record Daily Inspection Checklist".  2. System displays the checklist form with predefined inspection items.  3. Medical Staff marks each item as compliant or non-compliant and adds notes where needed.  4. Medical Staff submits the checklist.  5. System validates completeness, saves the checklist, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If mandatory items are left unmarked, system blocks submission and displays a validation error. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | None. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.120: Record The Daily Inspection Checklist Use Cases Specification**

### 3.2.8 Food Sample Retention

#### 3.2.8.1 Dispose Food Sample

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-023 - Dispose Food Sample | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | System generates a disposal alert when a food sample's mandatory 72-hour retention period expires with no active incident linked. | | |
| Description | Enables the Service Caterers to record and confirm the disposal of retained food samples after the mandatory 72-hour period in compliance with Decision 1246/Q?-BYT, updating the disposal tracking columns of M?u s? 5 when no active food safety incident is associated. | | |
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

**Table 3.121: Dispose Food Sample Use Cases Specification**

#### 3.2.8.2 Manage Food Sample Retention

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-033 - Manage Food Sample Retention | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers requests to manage food sample retention records or log refrigerator parameters. | | |
| Description | Enables the Service Caterers to log refrigerator temperatures twice daily, manage sample storage locations, and monitor cabinet statuses to ensure retention compliance. | | |
| Preconditions | The Service Caterers is authenticated and authorized to access this feature. | | |
| Postconditions | Refrigerator temperature log created or storage parameters updated in the database, with changes audited. | | |
| Normal Flow | 1. Service Caterers accesses the Food Sample Retention management panel.  2. System displays active refrigerators, their current registered temperature logs, and active samples stored in each refrigerator (BR-109).  3. Service Caterers selects 'Log Temperature' for a dedicated retention refrigerator.  4. Service Caterers enters the current temperature reading (�C) and selects the reading session (Morning or Evening) (BR-110). 5. Service Caterers submits the temperature log.  6. System validates that the temperature is within the safe range of 2�C to 8�C (BR-109).  7. System saves the temperature record, updates the refrigerator log, and displays a confirmation message.. | | |
| Alternative Flows | AF1 - Refrigerator Temperature Out of Range:  1. At step 6, the entered temperature is outside the 2�C to 8�C range (BR-109).  2. System displays warning message MSG-020 notifying the user that the temperature is out of the safe range.  3. System saves the temperature record with a warning flag, sends an immediate notification alert, and logs the deviation event.  4. If the temperature remains out of limits for >30 minutes, system triggers the temperature alarm alert (MSG-059).  AF2 - Update Refrigerator Assignment:  1. At step 2, Service Caterers chooses to reassign a food sample to a different refrigerator (e.g., due to maintenance).  2. Service Caterers selects the sample, selects the target refrigerator, and enters the reason for transfer.  3. System verifies the target refrigerator has active slots and is within safe temperature.  4. System updates the sample's storage record and logs the transfer details for audit | | |
| Exceptions | MSG-004, MSG-005, MSG-020, MSG-059 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (minimum twice per day for temperature logs) | | |
| Business Rules | BR-109, BR-110 | | |
| Other Information | Supports weekly audits conducted by Medical Staff (BR-110) by maintaining temperature logs. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.122: Manage Food Sample Retention Use Cases Specification**

#### 3.2.8.3 Register Food Sample

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-055 - Register Food Sample | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Service Caterers | Secondary Actors | None |
| Trigger | Service Caterers selects 'Register Food Sample' after completing all three-step inspections for a meal batch. | | |
| Description | Enables the Service Caterers to register food samples for every cooked dish served in compliance with Decision 1246/Q?-BYT, generating the sample label (Sample No 4) and initiating the sample retention tracking log (Sample No 5) with sample weight, labeling, photographic evidence, and GPS. | | |
| Preconditions | Service Caterers is authenticated and authorized. Meal batch has passed all three-step inspections. GPS enabled. | | |
| Postconditions | Sample retention records created and linked to inspection records. 72-hour cold storage timer initiated (BR-111). GPS validated within kitchen premises (BR-109). Photo evidence attached. MSG-044 displayed. | | |
| Normal Flow | 1. Service Caterers navigates to 'Register Food Sample' for the inspected meal batch.  2. System validates GPS location is within school kitchen premises (BR-109) and displays the registration form.  3. Service Caterers enters sample details corresponding to columns 1-8 of Sample No 5: meal slot, dish, total portion count, sample weight (minimum 100g for solid dishes / 150ml for liquid dishes per BR-203), storage container description, refrigerator storage temperature, and time of collection.  4. System generates and prints the sample container label conforming to Sample No 4, displaying Meal, Food sample name, Time of sampling, and Sampling person (Service Caterers staff).  5. Service Caterers pastes the printed label onto the container, uploads a photo of the labeled container (BR-107), signs the register digitally (Sample No 5 column 11), and confirms storage.  6. System saves the sample retention record, links it to the meal batch, starts the 72-hour storage timer (BR-111), and displays MSG-044. | | |
| Alternative Flows | AF1 - Insufficient Sample Weight: 1. At step 7, the entered sample weight is below the minimum threshold for its dish type (BR-203).  2. System blocks registration and displays error message MSG-019.  3. System prompts the Service Caterers to input the correct weight or add more sample. AF2 - GPS Location Verification Fails: 1. At step 2, the system validates the GPS coordinates and finds they are outside the school kitchen premises (BR-109). 2. System blocks registration and displays error message MSG-015. 3. System logs the GPS deviation error for compliance audit. | | |
| Exceptions | MSG-004, MSG-005, MSG-015, MSG-019 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (for every cooked dish served) | | |
| Business Rules | BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-203 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection and GPS-enabled device. | | |

**Table 3.123: Register Food Sample Use Cases Specification**

#### 3.2.8.4 Retrieve Food Sample Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-062 - Retrieve Food Sample Records | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff requests to retrieve and authorize a food sample record for laboratory analysis. | | |
| Description | Enables the Medical Staff to authorize the physical retrieval and lock the system record of retained food samples for laboratory analysis during an active food safety incident investigation. | | |
| Preconditions | Medical staff is authenticated and authorized. An active food safety incident exists with a valid Incident ID. The selected food sample is currently retained (not yet disposed of). | | |
| Postconditions | Sample physical retrieval is authorized and logged. The system record of the food sample is locked to prevent accidental disposal (BR-114, BR-150). The retrieval record is linked to the incident report (BR-114). | | |
| Normal Flow | 1. Medical Staff navigates to the Food Sample Records list and searches for samples by suspected meal date, dish name, or batch ID.  2. System displays the matching retained food samples, showing their current status (e.g., 'Retained') and remaining retention time (BR-111).  3. Medical Staff selects the relevant food sample and clicks 'Authorize Retrieval'.  4. System prompts for authorization details: Incident ID, name of authorizing medical officer, and physical destination lab (BR-114).  5. Medical Staff enters the details and submits.  6. System validates that the Incident ID is valid and active in the database.  7. System locks the food sample record (changing status to 'Under Investigation' / 'Retrieved' and blocking disposal per BR-114, BR-150), logs the retrieval authorization, and links the sample to the incident report (BR-114).  8. System displays a success message and generates a printable retrieval receipt. | | |
| Alternative Flows | AF1 - Sample Already Disposed:  1. At step 2, if the matching sample has already been disposed of (exceeded 72 hours and was cleared).  2. System displays the sample record with a status of 'Disposed' along with its disposal log (date, method, witness per BR-113).  3. Medical Staff cannot authorize physical retrieval, but they can select 'Link Disposed Sample to Incident' to associate the disposal metadata with the active incident for record-keeping and investigation tracing.  AF2 - Invalid Incident ID:  1. At step 6, the system validates the Incident ID and finds it is invalid or closed.  2. System blocks the retrieval authorization and displays error message MSG-023.  3. System prompts the Medical Staff to enter a valid active Incident ID. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand (during active food safety incidents) | | |
| Business Rules | BR-111, BR-113, BR-114, BR-150 | | |
| Other Information | Linked with UC-042 - Manage Food Safety Incidents when lab results are required. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.124: Retrieve Food Sample Records Use Cases Specification**

#### 3.2.8.5 View Food Sample Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-099 - View Food Sample Records | | |
| Created By | MinhTTHE186258 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff, School Administration, Service Caterers | Secondary Actors | None |
| Trigger | Primary actor requests to view food sample records. | | |
| Description | Enables the Medical Staff and School Admin to query, view, and export the complete digital Food Sample Retention and Disposal log (M?u s? 5) for traceability and compliance monitoring. | | |
| Preconditions | The medical staff, school admin is authenticated and authorized to access this feature. | | |
| Postconditions | Requested food sample records and history are retrieved and displayed on the screen. | | |
| Normal Flow | 1. User (Medical Staff or School Admin) navigates to the Food Sample Records section in the system portal.  2. System queries the database and displays the digital Food Sample Retention and Disposal register conforming to M?u s? 5, displaying all columns (1 to 12) including sample names, collection times, storage details, and disposal/witness signatures.  3. User can filter the records by date range, meal slot, dish name, or status (Retained, Retrieved, Disposed).  4. User can click 'Export' to download the selected M?u s? 5 records as Excel or PDF for inspection by authorities. | | |
| Alternative Flows | None | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | Daily (for monitoring) / Weekly | | |
| Business Rules | BR-108, BR-111, BR-114, BR-107, BR-110 | | |
| Other Information | This UC supports the mandatory weekly food sample storage audits conducted by Medical Staff | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.125: View Food Sample Records Use Cases Specification**

### 3.2.9 Meal Serving & Distribution

#### 3.2.9.1 Confirm Meal Distribution

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-011 - Confirm Meal Distribution | | |
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

**Table 3.126: Confirm Meal Distribution Use Cases Specification**

#### 3.2.9.2 Manage Meal Service

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-038 - Manage Meal Service | | |
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

**Table 3.127: Manage Meal Service Use Cases Specification**

#### 3.2.9.3 Monitor Child Meal History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-044 - Monitor Child Meal History | | |
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

**Table 3.128: Monitor Child Meal History Use Cases Specification**

#### 3.2.9.4 Monitor School Meals

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-046 - Monitor School Meals | | |
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

**Table 3.129: Monitor School Meals Use Cases Specification**

#### 3.2.9.5 Monitor Student Meal History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-047 - Monitor Student Meal History | | |
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

**Table 3.130: Monitor Student Meal History Use Cases Specification**

#### 3.2.9.6 Record Student Meal Consumption

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-066 - Record Student Meal Consumption | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | Teacher | Secondary Actors | None |
| Trigger | Teacher initiates student meal consumption recording during or after the meal service session. | | |
| Description | Enables the Teacher to record meal consumption status for students on special dietary plans, supporting nutritional compliance tracking and parent daily reporting. | | |
| Preconditions | Teacher is authenticated and authorized. Meal service session is active (BR-125). Classroom distribution has been confirmed. | | |
| Postconditions | Consumption records saved for students on special dietary plans. Data included in daily parent summary notification (BR-133). | | |
| Normal Flow | 1. Teacher navigates to Record Student Meal Consumption for their classroom. 2. System displays students on special dietary plans for the current session. 3. Teacher records each student's consumption: 'Fully Consumed', 'Partially Consumed', or 'Not Consumed' (BR-121). 4. System validates all required consumption entries are recorded. 5. System saves records and includes data in the daily parent summary notification (BR-133). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005, MSG-021 | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-125, BR-121, BR-133 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.131: Record Student Meal Consumption Use Cases Specification**

#### 3.2.9.7 View Allergy Alerts

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-085 - View Allergy Alerts | | |
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

**Table 3.132: View Allergy Alerts Use Cases Specification**

#### 3.2.9.8 View Meal Distribution List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-110 - View Meal Distribution List | | |
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

**Table 3.133: View Meal Distribution List Use Cases Specification**

#### 3.2.9.9 View Meal History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-111 - View Meal History | | |
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

**Table 3.134: View Meal History Use Cases Specification**

#### 3.2.9.10 View Special Meal Arrangements

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-118 - View Special Meal Arrangements | | |
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

**Table 3.135: View Special Meal Arrangements Use Cases Specification**

#### 3.2.9.11 View Special Meal Records

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-119 - View Special Meal Records | | |
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

**Table 3.136: View Special Meal Records Use Cases Specification**

#### 3.2.9.12 View Special Meal Requirements

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-120 - View Special Meal Requirements | | |
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

**Table 3.137: View Special Meal Requirements Use Cases Specification**

### 3.2.10 Feedback & Announcement Management

#### 3.2.10.1 Create Announcement

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-012 - Create Announcement | | |
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

**Table 3.138: Create Announcement Use Cases Specification**

#### 3.2.10.2 Manage Meal Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-037 - Manage Meal Feedback | | |
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

**Table 3.139: Manage Meal Feedback Use Cases Specification**

#### 3.2.10.3 Submit Meal Feedback

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-070 - Submit Meal Feedback | | |
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

**Table 3.140: Submit Meal Feedback Use Cases Specification**

#### 3.2.10.4 View Meal Feedback List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-136 - View Meal Feedback List | | |
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

**Table 3.141: View Meal Feedback List Use Cases Specification**

#### 3.2.10.5 View Meal Feedback Dashboard

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-137 - View Meal Feedback Dashboard | | |
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

**Table 3.142: View Meal Feedback Dashboard Use Cases Specification**

#### 3.2.10.6 Update Announcement

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-072 - Update Announcement | | |
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

**Table 3.143: Update Announcement Use Cases Specification**

#### 3.2.10.7 View Announcement Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-087 - View Announcement Detail | | |
| Created By | ThaoNPHE182154 | Date Created | 14/06/2026 |
| Primary Actor | User, School Administration | Secondary Actors | None |
| Trigger | User, School Administration requests to view announcement detail. | | |
| Description | Enables the User, School Administration to view the full content of an announcement. | | |
| Preconditions | The user or school administration is authenticated and authorized to access this feature. | | |
| Postconditions | The view announcement detail data is successfully retrieved and displayed to the User or School Administration. | | |
| Normal Flow | 1. Actor navigates to the Announcement List page.  2. Actor selects an announcement.  3. System retrieves the announcement details.  4. System displays: Title, Content, Target Group, Publication Status, Author, Publication Dates  5. If the actor role is �School Administration�, the system additionally displays the complete revision history log (BR-144). | | |
| Alternative Flows | None. | | |
| Exceptions | None. | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | None. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.144: View Announcement Detail Use Cases Specification**

#### 3.2.10.8 View Announcement List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-103 - View Announcement List | | |
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

**Table 3.145: View Announcement List Use Cases Specification**

#### 3.2.10.9 View Announcement List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-104 - View Announcement List | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Parent, Teacher | Secondary Actors | None |
| Trigger | Parent or Teacher navigates to the Announcements section. | | |
| Description | Enables Parent and Teacher to view the list of announcements published by the School Administration. | | |
| Preconditions | The parent or teacher is authenticated and authorized. | | |
| Postconditions | The list of announcements relevant to the user is displayed. | | |
| Normal Flow | 1. Parent/Teacher navigates to Announcements.  2. System retrieves and displays the list of published announcements relevant to the user, sorted by publish date.  3. Parent/Teacher may select an announcement to view its detail (extends UC-105 View Announcement Detail). | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. | | |
| Priority | Low | | |
| Frequency of Use | On-demand | | |
| Business Rules | None. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.146: View Announcement List (Parent, Teacher) Use Cases Specification**

### 3.2.11 Incident & Emergency Management

#### 3.2.11.1 Assign Incident Severity

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-006 - Assign Incident Severity | | |
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

**Table 3.147: Assign Incident Severity Use Cases Specification**

#### 3.2.11.2 Export Incident Reports

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-024 - Export Incident Reports | | |
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

**Table 3.148: Export Incident Reports Use Cases Specification**

#### 3.2.11.3 Generate Incident Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-026 - Generate Incident Report | | |
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

**Table 3.149: Generate Incident Report Use Cases Specification**

#### 3.2.11.4 Manage Food Safety Incidents

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-032 - Manage Food Safety Incidents | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff receives an incident notification and opens the active incident record to begin investigation. | | |
| Description | Enables the Medical Staff to manage the complete food safety incident investigation lifecycle: symptom assessment, severity classification, evidence collection, root cause analysis, and CAPA oversight. | | |
| Preconditions | Medical staff is authenticated and authorized. An active food safety incident record exists and has been assigned to Medical Staff. | | |
| Postconditions | Incident severity classified. Investigation evidence compiled. Root cause analyzed. CAPAs defined and assigned to Service Caterers (BR-152). Incident investigation report generated (BR-154). | | |
| Normal Flow | 1. Medical Staff opens the active incident record from the notification dashboard. 2. System displays symptoms, affected student count, suspected dishes, food sample records, and ingredient batch traceability. 3. Medical Staff assesses symptoms and classifies severity: Low, Medium, or High (BR-147). 4. Medical Staff documents root cause findings and defines corrective and preventive actions (CAPA). 5. System compiles investigation report (BR-154), assigns CAPA to Service Caterers with 12-hour deadline (BR-152), and logs all actions. | | |
| Alternative Flows | AF1 - High Severity Escalation: Medical Staff classifies as High. System triggers emergency protocol and notifies school board and health department (BR-148). AF2 - Additional Lab Testing Required: Insufficient evidence. Medical Staff initiates laboratory sample retrieval (UC-078) and documents additional testing requirements. | | |
| Exceptions | MSG-004, MSG-005, MSG-023 | | |
| Priority | High | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.150: Manage Food Safety Incidents Use Cases Specification**

#### 3.2.11.5 Report Food Safety Concern

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-059 - Report Food Safety Concern | | |
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

**Table 3.151: Report Food Safety Concern Use Cases Specification**

#### 3.2.11.6 Report Food Safety Incident

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-060 - Report Food Safety Incident | | |
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

**Table 3.152: Report Food Safety Incident Use Cases Specification**

#### 3.2.11.7 View Food Poisoning Incident List

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-095 - View Food Poisoning Incident List | | |
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

**Table 3.153: View Food Poisoning Incident List Use Cases Specification**

#### 3.2.11.8 View Food Safety Incident Detail

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-097 - View Food Safety Incident Detail | | |
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

**Table 3.154: View Food Safety Incident Detail Use Cases Specification**

#### 3.2.11.9 Record Violation

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-147 - Record Violation | | |
| Created By | HuyBQHE180599 | Date Created | 14/06/2026 |
| Primary Actor | Medical Staff | Secondary Actors | None |
| Trigger | Medical Staff identifies a food safety violation during an inspection and opens the violation recording form. | | |
| Description | Enables Medical Staff to record a food safety violation observed during inspection, capturing the nature, location, and severity of the violation for follow-up action. | | |
| Preconditions | The medical staff is authenticated and authorized. An inspection or monitoring activity is in progress. | | |
| Postconditions | The violation record is saved and linked to the related incident or inspection. Audit log records the action. | | |
| Normal Flow | 1. Medical Staff navigates to Food Safety Incident Handling and selects "Record Violation".  2. System displays the violation recording form.  3. Medical Staff enters violation details (type, location, description, severity) and submits.  4. System validates the input, saves the violation record, records the action in the audit log, and displays MSG-029. | | |
| Alternative Flows | None. | | |
| Exceptions | MSG-004, MSG-005. If required fields are missing or invalid, system blocks submission and displays a validation error. | | |
| Priority | High | | |
| Frequency of Use | On-demand | | |
| Business Rules | None. | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

**Table 3.155: Record Violation Use Cases Specification**

### 3.2.12 AI & Supporting Services

#### 3.2.12.1 Chat with AI

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-009 - Chat with AI | | |
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

**Table 3.156: Chat with AI Use Cases Specification**

#### 3.2.12.2 Manage External Integrations

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-031 - Manage External Integrations | | |
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

**Table 3.157: Manage External Integrations Use Cases Specification**

#### 3.2.12.3 Monitor System Activities

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-048 - Monitor System Activities | | |
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

**Table 3.158: Monitor System Activities Use Cases Specification**

#### 3.2.12.4 Receive Notification

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-049 - Receive Notification | | |
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

**Table 3.159: Receive Notification Use Cases Specification**

#### 3.2.12.5 Scan Face Data

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-065 - Scan Face Data | | |
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

**Table 3.160: Scan Face Data Use Cases Specification**

#### 3.2.12.6 View Audit Logs

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-088 - View Audit Logs | | |
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

**Table 3.161: View Audit Logs Use Cases Specification**

#### 3.2.12.7 View Error Logs

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-092 - View Error Logs | | |
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

**Table 3.162: View Error Logs Use Cases Specification**

#### 3.2.12.8 View Notification History

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-114 - View Notification History | | |
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

**Table 3.163: View Notification History Use Cases Specification**

#### 3.2.12.9 View System Status

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-126 - View System Status | | |
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

**Table 3.164: View System Status Use Cases Specification**

#### 3.2.12.10 View Usage Statistics

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-127 - View Usage Statistics | | |
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

**Table 3.165: View Usage Statistics Use Cases Specification**

### 3.2.13 Reporting & Analytics

#### 3.2.13.1 View Analytics Dashboard

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-148 - View Analytics Dashboard | | |
| Created By | MinhTTHE186258 | Date Created | 22/08/2026 |
| Primary Actor | School Administration | Secondary Actors | Medical Staff, System Admin |
| Trigger | The actor requests to view an analytics dashboard summarizing operational metrics. | | |
| Description | Enables the actor to view an analytics dashboard summarizing key operational metrics scoped to their permission level - food safety risk indicators, inspection compliance rate, daily meal summary, ingredient receiving, partnership performance, and traceability - with data refreshed near real time. | | |
| Preconditions | The actor is authenticated and authorized to access reporting features for their role and school. | | |
| Postconditions | The requested dashboard metrics are retrieved and displayed to the actor. | | |
| Normal Flow | 1. Actor navigates to the Reporting & Analytics section.  2. System retrieves the report catalogue available to the actor's role  3. Actor selects a report category.  4. System aggregates the metrics scoped to the actor's school (BR-176), computed directly from the original data source (BR-182).  5. System displays the dashboard widgets and charts, refreshed within 5 minutes of the underlying operational change (BR-180), with student personal data excluded from aggregates (BR-181). 6. Actor applies a date range or category filter and the system re-queries and displays the filtered data. | | |
| Alternative Flows | AF1 - Export from dashboard: At step 6, the actor selects "Export PDF/Excel"; the system proceeds to UC-149 Export Report using the current filter context. AF2 - No data in range: If no records match the selected period, the system displays an empty-state message instead of the chart and keeps the filters available. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | Daily | | |
| Business Rules | BR-175, BR-176, BR-180, BR-181, BR-182 | | |
| Other Information | None. | | |
| Assumptions | User has a running environment with internet connection. | | |

#### 3.2.13.2 Export Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-149 - Export Report | | |
| Created By | MinhTTHE186258 | Date Created | 22/08/2026 |
| Primary Actor | School Administration | Secondary Actors | Medical Staff, System Admin |
| Trigger | The actor requests to export a report or dashboard view to a file. | | |
| Description | Enables the actor to export the currently viewed report to PDF or Excel format for compliance retention, regulatory submission, and offline review. | | |
| Preconditions | The actor is authenticated and authorized to access the underlying report data. | | |
| Postconditions | An export file is generated and made available for download. The export event is recorded in the audit trail. | | |
| Normal Flow | 1. Actor selects "Export" from an analytics dashboard view (UC-148) or a standalone report screen.  2. Actor chooses the output format, PDF or Excel.  3. System validates the actor's data scope (BR-175, BR-176).  4. System generates the export file honouring the currently applied filters, and includes the report title, data period, school name, and export timestamp in the file header (BR-178).  5. System applies the "Confidential" watermark if the report contains sensitive operational or incident information (BR-181). 6. System returns the downloadable file and records who exported what and when in the audit trail (BR-177). | | |
| Alternative Flows | AF1 - Schedule instead of immediate download: At step 2, the actor selects "Schedule" rather than "Export Now"; the system proceeds to UC-150 Schedule Automated Report. AF2 - Download a previous export: The actor opens the export history and downloads a file generated earlier, retained for at least 12 months (BR-177). | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Medium | | |
| Frequency of Use | On-demand | | |
| Business Rules | BR-175, BR-176, BR-177, BR-178, BR-181, BR-182 | | |
| Other Information | Supported formats: PDF (PDFBox renderer) and Excel XLSX (Apache POI renderer) | | |
| Assumptions | User has a running environment with internet connection. | | |

#### 3.2.13.3 Schedule Automated Report

|  |  |  |  |
| --- | --- | --- | --- |
| UC ID and Name | UC-150 - Schedule Automated Report | | |
| Created By | MinhTTHE186258 | Date Created | 22/08/2026 |
| Primary Actor | School Administration | Secondary Actors | Email Service, Firebase Cloud Messaging, System Admin |
| Trigger | The actor wants a report to be generated and delivered periodically without manual action. | | |
| Description | Enables the actor to create, update, and remove schedules that automatically generate a selected report on a daily, weekly, or monthly basis and deliver it to a configured email recipient list. | | |
| Preconditions | The actor is authenticated and authorized to access the selected report type. At least one recipient email address is available. | | |
| Postconditions | The schedule is saved with its next run time calculated. On each run, the report is generated and delivered to the recipient list, and the run outcome is recorded. | | |
| Normal Flow | 1. Actor navigates to Report Schedules and selects "Create Schedule".  2. System displays the schedule form.  3. Actor selects the report type, the frequency (Daily, Weekly, or Monthly), the output format, and the recipient email list (BR-179).  4. System validates the input and the actor's access to the selected report type (BR-175, BR-176).  5. System saves the schedule, calculates the next run time, and displays a confirmation.  6. At each scheduled run, the system generates the report and delivers it to the recipient list through the Email Service. | | |
| Alternative Flows | AF1 - Update schedule: Actor opens an existing schedule, modifies the frequency, format, or recipients, and the system saves the change and recalculates the next run time. AF2 - Delete schedule: Actor removes a schedule and the system stops all future runs. AF3 - Run now: Actor triggers an immediate run to verify the configuration; the system generates and delivers the report without altering the scheduled next run time. | | |
| Exceptions | MSG-004, MSG-005 | | |
| Priority | Low | | |
| Frequency of Use | Rare | | |
| Business Rules | BR-175, BR-176, BR-177, BR-178, BR-179, BR-181, BR-182 | | |
| Other Information | None | | |
| Assumptions | User has a running environment with internet connection. The mail server is reachable. | | |

## 3.3 Functional Requirements

### 3.3.1 System Functional Overview

Screens Flow

#### 3.3.1.1 Screens Flow for Guest and Common Screen

![](data:image/png;base64...)

**Figure 3.23: Screens Flow for Guest and Common Screen**

#### 3.3.1.2 Screens Flow for System Admin

![](data:image/png;base64...)

**Figure 3.24: Screens Flow for System Admin**

#### 3.3.1.3 Screens Flow for School Admin

![](data:image/png;base64...)

**Figure 3.25: Screens Flow for School Admin**

#### 3.3.1.4 Screens Flow for Medical Staff

![](data:image/png;base64...)

**Figure 3.26: Screens Flow for Medical Staff**

#### 3.3.1.5 Screens Flow for Service Caterers

![](data:image/png;base64...)

**Figure 3.27: Screens Flow for Service Caterers**

#### 3.3.1.6 Screens Flow for Parent

![](data:image/png;base64...)

**Figure 3.28: Screens Flow for Parent**

#### 3.3.1.7 Screens Flow for Raw Food Supplier

![](data:image/png;base64...)

**Figure 3.29: Screens Flow for Raw Food Supplier**

#### 3.3.1.8 Screens Flow for Teacher

![](data:image/png;base64...)

**Figure 3.30: Screens Flow for Teacher**

### 3.3.2 Screen Authorization

| Screen | System Admin | School Admin | Teacher | Raw Food Supplier | Medical Staff | Service Caterers | Parent |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Login | x | x | x | x | x | x | x |
| Reset Password | x | x | x | x | x | x | x |
| Reset Password | x | x | x | x | x | x | x |
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
| Activate/Deactivate User Dialog | x |  |  |  |  |  |  |
| Activate/Deactivate School Dialog | x |  |  |  |  |  |  |
| School List | x |  |  | x |  |  |  |
| School Detail | x |  |  | x |  |  |  |
| Update School Screen/Dialog | x |  |  |  |  |  |  |
| Campus Config | x |  |  |  |  |  |  |
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

**Table 3.166: Screen Authorization**

### 3.3.3 Non-screen Functions

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
| 18 | Supplier & Partner Management | Supplier Deactivation Cascade | When a supplier profile is deactivated, the system automatically cancels all pending purchase orders and delivery schedules associated with that supplier. (BR-059) |
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
| 40 | Food Sample Retention | Temperature Deviation Alarm | System triggers an alarm if the retention refrigerator temperature deviates outside the 2�C�8�C range for more than 30 minutes. |
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

**Table 3.167: Non-screen Functions**

#### Mobile Screen

#### Guest and Common Screen

##### 3.3.1.1.1 Screen for Guest Portal

![](data:image/png;base64...)

**Figure 3.31: Screen for Guest Portal**

| Field name | Description |
| --- | --- |
| SFMS Logo | Displays an application logo or banner image at the top of the screen. Shown as a placeholder image frame with an 'X' mark in wireframe. |
| Welcome Text | Welcome message and system introduction |
| "Register School" Button | Registration button for Schools |
| "Register Supplier" Button | Registration button for Suppliers |
| "Register Service Caterers" Button | Registration button for Service Caterers |
| "Login" Button | Button leading to the login page |

**Table 3.168: Screen for Guest Portal**

##### 3.3.1.1.2 Screen for Register School

![](data:image/png;base64...)

**Figure 3.32: Screen for Register School**

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

**Table 3.169: Screen for Register School**

##### 3.3.1.1.3 Screen for Register Supplier

![](data:image/png;base64...)

**Figure 3.33: Screen for Register Supplier**

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

**Table 3.170: Screen for Register Supplier**

##### 3.3.1.1.4 Screen for Register Service Caterers

![](data:image/png;base64...)

**Figure 3.34: Screen for Register Service Caterers**

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

**Table 3.171: Screen for Register Service Caterers**

##### 3.3.1.1.5 Screen for Login

![](data:image/png;base64...)

**Figure 3.35: Screen for Login**

| Field name | Description |
| --- | --- |
| Username / Email | Username or account email |
| Password | Masked password display |
| "Login" Button | Perform login |
| "Reset Password?" | Recover password |
| "Register Options" | Go to registration options |

**Table 3.172: Screen for Login**

##### 3.3.1.1.6 Screen for Reset Password

![](data:image/png;base64...)

**Figure 3.36: Screen for Reset Password**

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to Login screen |
| Email Address | Registered account email |
| "Send OTP" Button | Submit OTP request |

**Table 3.173: Screen for Reset Password**

##### 3.3.1.1.9 Screen for Personal Profile

![](data:image/png;base64...)

**Figure 3.39: Screen for Personal Profile**

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

**Table 3.176: Screen for Personal Profile**

##### 3.3.1.1.10 Screen for Edit Profile

![](data:image/png;base64...)

**Figure 3.40: Screen for Edit Profile**

| Field name | Description |
| --- | --- |
| "Cancel" Button | Cancel editing |
| "Save" Button | Save edited information |
| Avatar Upload | Change profile picture |
| Full Name | User's full name |
| Phone Number | Phone number |
| Email Address | User email (Read-only) |

**Table 3.177: Screen for Edit Profile**

##### 3.3.1.1.11 Screen for Change Password

![](data:image/png;base64...)

**Figure 3.41: Screen for Change Password**

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to profile |
| Current Password | Current password |
| New Password | New Password |
| Confirm New Password | Confirm New Password |
| "Save Password" Button | Save new password |

**Table 3.178: Screen for Change Password**

##### 3.3.1.1.12 Screen for Notification Settings

![](data:image/png;base64...)

**Figure 3.42: Screen for Notification Settings**

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to profile |
| Push Notifications | Toggle push notifications on device |
| Email Notifications | Toggle email notifications |
| Allergy Warnings | Receive food allergy warnings |
| Food Safety Alerts | Receive food safety alerts |
| "Save Settings" Button | Save notification settings |

**Table 3.179: Screen for Notification Settings**

##### 3.3.1.1.13 Screen for Notification History

![](data:image/png;base64...)

**Figure 3.43: Screen for Notification History**

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to settings/profile |
| Notification List | List of notification items |
| Warning Badge | Warning Badge |
| Allergy Warnings | Distinguish severity (Red: Allergy/Food Safety alert, Yellow: News, Grey: System) |
| Sent Time | Time notification was sent (Hour/Date) |

**Table 3.180: Screen for Notification History**

##### 3.3.1.1.14 Screen for AI Chatbot

![](data:image/png;base64...)

**Figure 3.44: Screen for AI Chatbot**

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to profile |
| "Clear Chat" Button | Clear history of current conversation |
| Conversation Pane (Chat bubbles) | Displays bot messages (left) and user messages (right) |
| "Send" Button | Active (only enabled when input has text) |
| "View Chat History" | View list of past chat history |

**Table 3.181: Screen for AI Chatbot**

##### 3.3.1.1.15 Screen for AI Chat History

![](data:image/png;base64...)

**Figure 3.45: Screen for AI Chat History**

| Field name | Description |
| --- | --- |
| "Back" Button | Go back to Chat screen |
| Conversation List | List showing topic summaries and start date of chats |

**Table 3.182: Screen for AI Chat History**

#### Service Caterers Screens

##### 3.3.1.5.1 Screen for Catering Dashboard

![](data:image/png;base64...)

**Figure 3.46: Screen for Catering Dashboard**

| Field name | Description |
| --- | --- |
| Avatar | User profile picture |
| Notif Icon | Quickly view the number of unread notifications |
| Grid Menu Items | List of modules (Receiving, Inventory, etc.) |
| Latest Alerts Panel | Displays urgent alerts (Expiration, shortage) |

**Table 3.183: Screen for Catering Dashboard**

##### 3.3.1.5.2 Screen for Receiving Dashboard

![](data:image/png;base64...)

**Figure 3.47: Screen for Receiving Dashboard**

| Field name | Description |
| --- | --- |
| Expected Delivery List | List of supplier invoices scheduled for today |
| "Start New Receiving" | Start a new ingredient receiving process |
| "View History" | View history of previous receiving batches |

**Table 3.184: Screen for Receiving Dashboard**

##### 3.3.1.5.3 Screen for Receive Ingredients

![](data:image/png;base64...)

**Figure 3.48: Screen for Receive Ingredients**

| Field name | Description |
| --- | --- |
| Delivery ID & Supplier | Delivery ID and Supplier name |
| "Verify Invoice" | Navigate to quantity checklist |
| "Inspect Quality" | Navigate to quality inspection metrics |
| "Capture Evidence" | Take photos for evidence storage |
| "Log Rejections" | Record returned/rejected ingredient batches |
| "Send the Receipt of Goods | Submit receiving report |

**Table 3.185: Screen for Receive Ingredients**

##### 3.3.1.5.4 Screen for Verify Supplier Delivery

![](data:image/png;base64...)

**Figure 3.49: Screen for Verify Supplier Delivery**

| Field name | Description |
| --- | --- |
| Verification Checkbox | Confirm that physical delivery matches the invoice |
| Received Qty | Physically received quantity |
| "Save Checklist" | Save draft of the quantity status |

**Table 3.186: Screen for Verify Supplier Delivery**

##### 3.3.1.5.5 Screen for Inspect Ingredient Quality

![](data:image/png;base64...)

**Figure 3.50: Screen for Inspect Ingredient Quality**

| Field name | Description |
| --- | --- |
| Measured Temperature | Temperature of the batch upon receipt |
| Freshness Level | Select 1 of 3: Good, Fair, Poor |
| Pest-free check | Confirm absence of pests, insects, or mold |
| Expiration Date | Expiry date labeled on packaging |
| "Save Inspection" | Save quality inspection report |

**Table 3.187: Screen for Inspect Ingredient Quality**

##### 3.3.1.5.6 Screen for Capture Receiving Evidence

![](data:image/png;base64...)

**Figure 3.51: Screen for Capture Receiving Evidence**

| Field name | Description |
| --- | --- |
| Capture Photo Button | Take photo of invoice or packaging |
| Thumbnail list | List of captured photos |
| Notes | Additional remarks |

**Table 3.188: Screen for Capture Receiving Evidence**

##### 3.3.1.5.7 Screen for Reject Ingredient Batch

![](data:image/png;base64...)

**Figure 3.52: Screen for Reject Ingredient Batch**

| Field name | Description |
| --- | --- |
| Select Ingredient | Select the defective ingredient from the invoice |
| Reason for Rejection | Reason for Rejection |
| Qty Rejected | Quantity to reject and return |
| Capture Rejection Photo | Capture photo proof of defect (damaged, spoiled, moldy) |

**Table 3.189: Screen for Reject Ingredient Batch**

##### 3.3.1.5.8 Screen for Store Ingredients

![](data:image/png;base64...)

**Figure 3.53: Screen for Store Ingredients**

| Field name | Description |
| --- | --- |
| Select Batch | Select the recently received ingredient batch |
| Storage Unit | Select storage unit location |
| Assigned Temp Target | Standard regulated temperature for the selected unit |
| Shelf Assignment | Specific shelf or row number |

**Table 3.190: Screen for Store Ingredients**

##### 3.3.1.5.9 Screen for Monitor Expiration Dates

![](data:image/png;base64...)

**Figure 3.54: Screen for Monitor Expiration Dates**

| Field name | Description |
| --- | --- |
| Alert List | List of ingredients close to expiry |
| "Dispose" | Record the disposal of expired ingredients |

**Table 3.191: Screen for Monitor Expiration Dates**

##### 3.3.1.5.10 Screen for Issue Ingredients

![](data:image/png;base64...)

**Figure 3.55: Screen for Issue Ingredients**

| Field name | Description |
| --- | --- |
| Target Meal & Dish | Targeted meal and dish |
| Required Qty | Required ingredient quantity based on recipe |

**Table 3.192: Screen for Issue Ingredients**

##### 3.3.1.5.11 Screen for Create Purchase Request

![](data:image/png;base64...)

**Figure 3.56: Screen for Create Purchase Request**

| Field name | Description |
| --- | --- |
| Ingredient Search | Search for ingredients needing restock |
| Current Stock | Current inventory stock level |
| Requested Qty | Quantity to purchase |
| Urgency Level | Urgency level |

**Table 3.193: Screen for Create Purchase Request**

##### 3.3.1.5.12 Screen for Update Menu

![](data:image/png;base64...)

**Figure 3.57: Screen for Update Menu**

| Field name | Description |
| --- | --- |
| Week Selection | Select the week to edit menu |
| Day Tabs | Days of the week (Monday - Friday) |
| Dish List | List of dishes for the selected day |
| "Add Custom Dish" | Add a new dish to the selected day |

**Table 3.194: Screen for Update Menu**

##### 3.3.1.5.13 Screen for View Menu Recommendations

![](data:image/png;base64...)

**Figure 3.58: Screen for View Menu Recommendations**

| Field name | Description |
| --- | --- |
| Option Cards | Menu options proposed by Gemini AI |
| "Apply Option" | Apply the proposed option to the week |

**Table 3.195: Screen for View Menu Recommendations**

##### 3.3.1.5.14 Screen for Special Meals Configuration

![](data:image/png;base64...)

**Figure 3.59: Screen for Special Meals Configuration**

| Field name | Description |
| --- | --- |
| Student Name | Select student with allergies |
| Substitute Recipe | Select a safe substitute recipe |
| Color Sticker Code | Select color sticker for portion identification |

**Table 3.196: Screen for Special Meals Configuration**

##### 3.3.1.5.15 Screen for Prepare Ingredients

![](data:image/png;base64...)

**Figure 3.60: Screen for Prepare Ingredients**

| Field name | Description |
| --- | --- |
| Session Selection | Select preparation shift (Morning, Noon, Afternoon) |
| Prep Checklist | Hygiene and cross-contamination prevention checklist |
| Start/End Prep Time | Prep start and end times |

**Table 3.197: Screen for Prepare Ingredients**

##### 3.3.1.5.16 Screen for Record Cooking Information

![](data:image/png;base64...)

**Figure 3.61: Screen for Record Cooking Information**

| Field name | Description |
| --- | --- |
| Select Dish | Select dish currently being cooked |
| Required Core Temp | Minimum required core temperature |
| Measured Core Temp | Actual measured core temperature using a thermometer |
| Measurement Time | Time of temperature measurement |
| Visual Appearance check | Confirm dish visually appears fully cooked |

**Table 3.198: Screen for Record Cooking Information**

#### Medical Staff Screens

##### Screens Medical Staff Dashboard

![](data:image/png;base64...)

**Figure 3.62: Screens Medical Staff Dashboard**

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

**Table 3.199: Screens Medical Staff Dashboard**

##### 3.3.1.4.2 Screens Health Record For Medical Staff

![](data:image/png;base64...)

**Figure 3.63: Screens Health Record For Medical Staff**

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

**Table 3.200: Screens Health Record For Medical Staff**

##### 3.3.1.4.3 Screens Special Meal For Student

![](data:image/png;base64...)

**Figure 3.64: Screens Special Meal For Student**

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

**Table 3.201: Screens Special Meal For Student**

##### 3.3.1.4.4 Screens Incidents Records

![](data:image/png;base64...)

**Figure 3.65: Screens Incidents Records**

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

**Table 3.202: Screens Incidents Records**

##### 3.3.1.4.5 Screens Incidents Records Detail

![](data:image/png;base64...)

**Figure 3.66: Screens Incidents Records Detail**

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

**Table 3.203: Screens Incidents Records Detail**

##### 3.3.1.4.6 Screens Evaluate the level Incidents

![](data:image/png;base64...)

**Figure 3.67: Screens Evaluate the level Incidents**

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

**Table 3.204: Screens Evaluate the level Incidents**

##### 3.3.1.4.7 Screens Investigation Space

![](data:image/png;base64...)

**Figure 3.68: Screens Investigation Space**

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

**Table 3.205: Screens Investigation Space**

##### 3.3.1.4.8 Screens Export To School Administration

![](data:image/png;base64...)

**Figure 3.69: Screens Export To School Administration**

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

**Table 3.206: Screens Export To School Administration**

##### 3.3.1.4.9 Screens Extraction of Stored Food Samples

![](data:image/png;base64...)

**Figure 3.70: Screens Extraction of Stored Food Samples**

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

**Table 3.207: Screens Extraction of Stored Food Samples**

##### 3.3.1.4.10 Screens Collect Evidence

![](data:image/png;base64...)

**Figure 3.71: Screens Collect Evidence**

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

**Table 3.208: Screens Collect Evidence**

##### 3.3.1.4.11 Screens Treatment Measures

![](data:image/png;base64...)

**Figure 3.72: Screens Treatment Measures**

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

**Table 3.209: Screens Treatment Measures**

#### Parent Screens

##### Parent Dashboard

![](data:image/png;base64...)

**Figure 3.73: Parent Dashboard Screen**

| Field name | Description |
| --- | --- |
| Child Selector Dropdown | Dropdown menu at the top left. Label: 'Child: Nguy?n Minh H?i (Grade...'. Used to switch between children profiles. |
| Notification Bell Icon | Icon button at the top right. Tapping opens the Notification Center. |
| Allergy Threat Alert Box | High-priority alert card with a red border. Title: 'Menu Allergy Threat Detected!'. Tapping opens safety substitute settings. |
| Today's Lunch Menu Card | Container displaying today's dishes. Includes a text link 'Tap to view weekly details >' to open the weekly menu. |
| Health Hub Navigation Tile | Button with a person icon. Label: 'Health Hub'. Navigates to health and allergy management. |
| School Meals Navigation Tile | Button with a '$' icon. Label: 'School Meals'. Navigates to meal plans and billing. |
| Meal Feedback Navigation Tile | Button with a chat bubble icon. Label: 'Meal Feedback'. Navigates to the feedback submission form. |
| Meal History Navigation Tile | Button with a calendar icon. Label: 'Meal History'. Navigates to past meal logs. |
| Report Urgent Safety Incident Button | Full-width button with a red dashed border. Label: 'Report Urgent Safety Incident'. Triggers urgent reporting workflow. |
| Bottom Navigation Bar | Global navigation menu with 4 tabs: 'Home' (Active), 'Meals', 'Health', and 'Profile'. |

**Table 3.210: Parent Dashboard**

##### Child Selection

![](data:image/png;base64...)

**Figure 3.74: Child Selection Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping returns the user to the previous screen. |
| Screen Title | Text header: 'Child Selection', located at the top navigation area. |
| Instruction Text | Informative text label: 'Select an active child profile to track meals and health details:'. |
| Child Profile Radio Group | A bounded container enclosing radio button options for child selection. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home' (Active), 'Meals', 'Health', and 'Profile'. |

**Table 3.211: Child Selection**

##### Student Health Hub

![](data:image/png;base64...)

**Figure 3.75: Student Health Hub Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping returns the user to the previous screen. |
| Screen Title | Text header: 'Child Selection', located at the top navigation area. |
| Back Button | Arrow icon button (<-) at the top-left corner. Tapping returns the user to the previous screen. |
| Screen Title | Text header: 'Student Health Hub', located at the top navigation area. |
| Student Summary Card | Header container displaying vital student identity information: Name ('Nguy?n Minh H?i') and subtext details ('Age: 9 | Class: 3A'). |
| Height Card | Data tile showing the student's height. Label: 'Height'. Value: '132 cm'. |
| Weight Card | Data tile showing the student's weight. Label: 'Weight'. Value: '29 kg'. |
| Blood Type Card | Data tile showing the student's blood type medical record. Label: 'Blood Type'. Value: 'O+'. |
| BMI Status Card | Data tile showing the calculated BMI status highlighted in green text. Label: 'BMI Status'. Value: '16.6 (Normal)'. |
| Allergy Profile Summary Card | Container detailing critical allergy information. Section Header: 'Allergy Profile Summary' in red text. Bullet point list displays current allergens: '� Peanuts - Severity: Severe (Danger)'. |
| Manage Health Details Button | Dark filled full-width action button. Label: 'Manage Health Details'. Tapping navigates to the health information editing form. |
| Manage Allergy List Button | Dark filled full-width action button. Label: 'Manage Allergy List'. Tapping navigates to the allergy list management section. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health' (Active state), and 'Profile'. |

**Table 3.212: Student Health Hub**

##### Update Health

![](data:image/png;base64...)

**Figure 3.76: Update Health Screen**

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

**Table 3.213: Student Health Hub**

##### Add/Edit Allergy

![](data:image/png;base64...)

**Figure 3.77: Add/Edit Allergy Screen**

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

**Table 3.214: Add/Edit Allergy**

##### Weekly Menu

![](data:image/png;base64...)

**Figure 3.78: Weekly Menu Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Weekly Menu', located at the top navigation bar. |
| Weekly Days Tab Bar | Horizontal tab navigation row consisting of school weekdays: 'Mon', 'Tue', 'Wed', 'Thu', 'Fri'. The 'Mon' tab is currently active/selected by default. |
| Standard Meal Item Card (1) | Content container for the first dish. Title: '1. Braised Pork with Eggs'. Description: 'Ingredients: Pork, eggs, sugar, spices'. Features a dark grey status tag labeled 'Lunch' on the right side. |
| Allergy Alert Meal Card (2) | Bounded container highlighted with a red border for critical warning. Title: '2. Stir-fried Pork with Peanuts' in red text. Description: 'Contains: Peanuts. Tap to view Red Label replacement option for Nguy?n Minh H?i.'. Features a red status tag labeled 'Allergy Alert' on the right. Tapping redirects to substitution options. |
| Standard Meal Item Card (3) | Content container for the third dish. Title: '3. Tomato Tofu Soup'. Description: 'Ingredients: Tofu, tomatoes, green onions'. Features a dark grey status tag labeled 'Lunch' on the right side. |
| View Nutrition Details Button | Dark filled full-width action button. Label: 'View Nutrition Details'. Tapping redirects the user to the detailed nutritional breakdown screen for the selected day's menu. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals' (Active state), 'Health', and 'Profile'. |

**Table 3.215: Weekly Menu**

##### Dish Details

![](data:image/png;base64...)

**Figure 3.79: Dish Details Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Dish Details', located at the top navigation bar. |
| Dish Information Card | Bounded main container displaying comprehensive details for the selected dish: Dish Title, Dish Category, Catering Supplier, Kitchen. |
| Full Ingredient Log Section | Sub-section within the information card. Header: 'Full Ingredient Log'. Description text: 'Pork shoulder meat (Local Farms), boiled quail eggs (Poultry farms), garlic, scallions, fish sauce, sugar, dark soy sauce.'. |
| Allergen Warnings Section | Safety sub-section highlighted with a red header: 'Allergen Warnings'. Warning text statement: 'Contains Soy & Fish sauce. (Shellfish/Peanut free).' displayed in red text. |
| Give Feedback Button | Dark filled full-width action button. Label: 'Give Feedback on this Dish'. Tapping redirects the user to the meal feedback submission screen for this specific dish. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals' (Active state), 'Health', and 'Profile'. |

**Table 3.216: Dish Detail**

##### Allergy Conflict Alert

![](data:image/png;base64...)

**Figure 3.80: Allergy Conflict Alert Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Allergen Conflict!' displayed in red text at the top navigation bar. |
| Allergen Warning Card | Bounded alert container highlighted with a red border. Features a red section header: 'ALLERGEN WARNING' and details the safety conflict text regarding the student's registered allergy and the specific menu dish. |
| Substitute Meal Allocation Card | Content container displaying alternative meal details. Section Header: 'Substitute Meal Allocation'. |
| Parental Advisory Note | Italicized informational text note at the center: '\* Parents should advise their child to double check the Red Sticker on their tray before consuming the meal.' |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals' (Active state), 'Health', and 'Profile'. |

**Table 3.217: Allergy Conflict Alert**

##### Notifications

![](data:image/png;base64...)

**Figure 3.81: Notifications Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping navigates the user back to the previous screen. |
| Screen Title | Text header: 'Notifications', located at the top navigation bar. |
| Settings Icon | Gear icon button at the top-right corner. Tapping opens notification preference configurations. |
| Notification Category Tabs | Horizontal tab row to filter messages: 'All', 'Safety' (highlighted in red text), 'Allergy', and 'Menu'. The 'All' tab is currently selected by default. |
| Safety Incident Alert Card | Notification item container highlighted with a red indicator bar on the left. Features a red badge labeled 'Safety Incident Alert', a timestamp ('Today, 11:20 AM'), and description text regarding a suspicious pork batch and menu swap. |
| Allergy Warning Card | Notification item container highlighted with an orange indicator bar on the left. Features an orange badge labeled 'Allergy Warning', a timestamp ('Yesterday'), and description text regarding an allergy mismatch check for Nguy?n Minh H?i. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home' (Active state), 'Meals', 'Health', and 'Profile'. |

**Table 3.218: Notifications**

##### Meal Feedback

![](data:image/png;base64...)

**Figure 3.82: Meal Feedback Screen**

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

**Table 3.219: Meal Feedback**

##### Report Safety Issue

![](data:image/png;base64...)

**Figure 3.83: Report Safety Issue Screen**

| Field name | Description |
| --- | --- |
| Back Button | Arrow icon button (<-) located at the top-left corner. Tapping discards entries and returns the user to the previous screen. |
| Screen Title | Text header: 'Report Safety Issue' displayed in red text at the top navigation bar. |
| Concern Category Dropdown | Dropdown selection menu. Label: 'Concern Category \*' (Required). Displays the selected issue (e.g., 'Spoiled/Sour Food') in red text with a down arrow icon on the right to open more categories. |
| Symptoms Checkbox Group | Multiple-choice checkbox group. Label: 'Symptoms Shown by Child'. Contains 4 options (all unselected by default): Stomach Pain , Vomiting/Nausea, Diarrhea, Fever. |
| Incident Details Section | Input region for reporting details. Label: 'Incident Details \*' (Required). BA note: This area provides space for a required text area description input right below the label. |
| Photo Attachment Component | Camera icon upload action field highlighted in red. Label text link: 'Attach Food Error / Medical Report Photo \*' (Asterisk indicates it is a required upload). Tapping opens the device camera or gallery. |
| Send Urgent Report Button | Red filled, full-width action button. Label: 'Send Urgent Report' in white text. Tapping validates inputs and immediately submits the urgent safety report to the system. |
| Bottom Navigation Bar | Persistent navigation menu at the bottom with 4 interactive tabs: 'Home', 'Meals', 'Health', and 'Profile'. |

**Table 3.220: Report Safety Issue**

##### Meal History Log

![](data:image/png;base64...)

**Figure 3.84: Meal History Log**

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

**Table 3.221: Meal History Log**

#### Raw Food Supplier Screen

##### 3.3.1.7.1 Supplier Dashboard

![](data:image/png;base64...)

**Figure 3.85: Supplier Dashboard**

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

**Table 3.222: Supplier Dashboard**

##### 3.3.1.7.2 Partnership Detail

![](data:image/png;base64...)

**Figure 3.86: Partnership Detail**

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

**Table 3.223: Partnership Detail**

##### 3.3.1.7.3 Purchase Request Screen

![](data:image/png;base64...)

**Figure 3.87: Purchase Request Screen**

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

**Table 3.224: Purchase Request Screen**

##### 3.3.1.7.4 Purchase Request Screen Detail

![](data:image/png;base64...)

**Figure 3.88: Purchase Request Screen Detail**

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

**Table 3.225: Purchase Request Screen Detail**

##### 3.3.1.7.5 Compliance Docs Screens

![](data:image/png;base64...)

**Figure 3.89: Compliance Docs Screens**

| Field name | Description |
| --- | --- |
| Screen Title | Display the current screen title ("Compliance Record") |
| "Back" Button | Return to the previous dashboard screen |
| "Add / Plus" Icon | Shortcut button to upload or add a new compliance document or certificate |
| "Food Safety Certificate" Card | Navigate to the management section for handling the farm's overall food safety and hygiene certification |
| "Product Origin Certification" Card | Navigate to the management section for tracking documentation related to VietGAP, GlobalGAP, Organic standards, and other provenance credentials |

**Table 3.226: Compliance Docs Screens**

##### 3.3.1.7.6 Ingredient Batches Screens

![](data:image/png;base64...)

**Figure 3.90: Ingredient Batches Screens**

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

**Table 3.227: Ingredient Batches Screens**

##### 3.3.1.7.7 Supplier Upload Certificates

![](data:image/png;base64...)

**Figure 3.91: Ingredient Batches Screens**

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

**Table 3.228: Ingredient Batches Screens**

#### Teacher Screens

##### Teacher Dashboard

![](data:image/png;base64...)

**Figure 3.92: Teacher Dashboard Screen**

| Field name | Description |
| --- | --- |
| Screen Title Header | Page title text block displaying 'Dashboard' and subtext configuration 'Class in charge: Class 1A � Ms. Lan'. |
| Greeting Banner | Highlighted box container with blue text displaying 'Hello, Ms. Lan!' and the current system date 'Today: 17/06/2026'. |
| Total Students Card | Metric summary tile showing the number 35 with the subtext label 'Students in Class 1A'. |
| Meals Pending Card | Metric summary tile showing the number 25 with the subtext label 'Meals pending'. |
| Dietary Notes Card | Metric summary tile showing the number 3 with the subtext label 'Dietary notes'. |
| Health Incidents Card | Metric summary tile showing the number 2 with the subtext label 'Health incidents sent'. |
| Distribute Meals Today Button | Dark filled full-width action button labeled 'Distribute Meals Today'. Tapping opens the meal tracking/distribution workflow. |
| Report New Health Incident Button | Bordered full-width secondary action button labeled 'Report New Health Incident'. Tapping opens the health incident reporting form. |
| Today's Notice Board Card | Bounded information list highlighted with a brown border. Displays urgent bullet points regarding a student's shrimp allergy, vegetarian meal distribution, and the latest student incident status. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home' (Active/shaded state), 'Students', 'Meals', and 'Incidents'. |

**Table 3.229: Teacher Dashboard**

##### Meal Distribution

![](data:image/png;base64...)

**Figure 3.93: Meal Distribution Screen**

| Field name | Description |
| --- | --- |
| Home Shortcut Button | Small bordered button labeled 'Home' at the top left. Tapping returns the user directly to the main dashboard. |
| Screen Title Header | Page title text block displaying 'Meal Distribution'. |
| Status Filter Tabs | Horizontal tab navigation row to filter the student list: 'All (35)', 'Pending', 'Served', and 'Special Meal'. |
| Student Meal Status Cards | Scrollable list of student containers displaying avatar initials, student names, dietary tags ('Vegetarian Meal', 'Normal Meal'), allergy alerts in red text ('Warning: Shrimp Allergy'), and actionable elements: Status Indicator, Details Link. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students', 'Meals' (Active/shaded state), and 'Incidents'. |

**Table 3.230: Meal Distribution**

##### Student List in Class

![](data:image/png;base64...)

**Figure 3.94: Student List in Class Screen**

| Field name | Description |
| --- | --- |
| Home Shortcut Button | Small bordered button labeled 'Home' at the top left. Tapping returns the user directly to the main dashboard. |
| Screen Title Header | Page title text block displaying 'Class 1A Student List'. |
| Profile Filter Tabs | Horizontal tab navigation row to filter students: 'All' (Active/dark filled state), 'Allergy Warning', 'Special Diet', and 'Normal'. |
| Student Record Cards | Scrollable list of student containers displaying avatar initials, student full names, conditional condition badges ('Allergy Warning' in red, 'Special Diet' in yellow, or 'Normal' in grey), and an actionable Profile text link on the right side to open the individual student profile log. |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students' (Active/shaded state), 'Meals', and 'Incidents'. |

**Table 3.231: Student List in Class**

##### Health Incident Report

![](data:image/png;base64...)

**Figure 3.95: Health Incident Report Screen**

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

**Table 3.232: Health Incident Report**

##### Incident History

![](data:image/png;base64...)

**Figure 3.96: Incident History Screen**

| Field name | Description |
| --- | --- |
| Home Shortcut Button | Small bordered shortcut button labeled 'Home' at the top left. Tapping redirects users directly to the main dashboard. |
| Screen Title Header | Page title text block displaying 'Incident History'. |
| Assessment Filter Tabs | Horizontally scrollable tab row to filter logs: 'All' (Active/dark filled state), 'Pending Assessment', and 'Under Assessment'. |
| Incident Log Cards | Scrollable list of recorded medical event containers displaying student identity details, reported symptoms, a severity indicator badge ('Mild', 'Medium'), the report date string, and a status label badge ('Pending Assessment' in yellow or 'Concluded' in green). |
| Bottom Tab Navigation Bar | Persistent global navigation menu featuring 4 equal-width tabs: 'Home', 'Students', 'Meals', and 'Incidents' (Active/shaded state). |

**Table 3.233: Incident History**

**3.4 Non-Functional Requirements**

### 3.4.1 Performance Requirements

1. The system shall respond to operational and data requests within an average of ? 3 seconds.
2. When capturing verification photos, the system shall automatically validate the user's GPS coordinates against the designated school canteen or gate location within a radius of 200 meters (as defined by BR-079).
3. The system shall support rapid searching and history retrieval for ingredient inventory and processing logs.

### 3.4.2 Reliability Requirements

1. The system shall support continuous operation to ensure uninterrupted tracking of the school kitchen workflow.
2. All operational data shall be stored centrally to minimize data loss and ensure informational consistency.
3. All user operational activities and status changes shall be automatically logged for auditing and inspection purposes.
4. The database shall be automatically backed up on a daily basis.
5. For critical system failures (Priority 1 incidents), the recovery time shall not exceed 2 hours.

### 3.4.3 Security Requirements

1. User passwords shall be hashed using BCrypt before being stored in the database.
2. The system shall use JSON Web Tokens (JWT) for authentication.
3. SQL injection attacks shall be prevented through parameterized queries using Spring Data JPA (Hibernate).

### 3.4.4 Usability Requirements

1. The user interface shall be simple, intuitive, and easy to adopt for school management, kitchen staff, and medical personnel.
2. All system error messages shall be displayed in a clear, actionable, and understandable manner.
3. The system shall support responsive layouts for:
   * **Desktop devices:** Minimum width of 1280px
   * **Mobile/Tablet devices:** Minimum width of 360px

### 3.4.5 Data Requirements

1. Uploaded images (e.g., verification photos, food sample photos, invoices) shall be in JPG or PNG format and must not exceed 10 MB.
2. The system shall ensure data integrity for food batch records, processing history, and sample storage logs.

### 3.4.6 Operational Constraints

1. The system shall operate within the territory of Vietnam and comply with national food safety standards.
2. The system requires a stable Internet connection for full functionality.

## 3.5 Requirement Appendix

### 3.5.1 Business Rules

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-001 | Password encryption | All user passwords must be hashed with BCrypt before being stored. Plain-text passwords are never stored. |
| BR-002 | Account lockout on failed login | After 5 consecutive failed login attempts, the system automatically locks the account for 15 minutes. The lockout event must be recorded in the audit log. |
| BR-003 | Password reset token expiry | A "Reset Password" reset token is valid for 1 hour from creation. After expiry, the user must request a new link. Maximum 1 reset request per 15 minutes per email. |
| BR-004 | Session timeout | A login session automatically expires after 30 minutes of inactivity. On timeout, the session token is invalidated and the user is redirected to the login screen. |
| BR-005 | Single-device session | Each account may be logged in on only one device at a time. Logging in from a new device invalidates the current session. |
| BR-006 | Password strength policy | Passwords must satisfy a minimum security policy (length, special characters, etc.). A new password must not match any of the last 3 passwords (BR-015). |
| BR-009 | Accept terms on first login | On the first login, the user must accept the Privacy Policy and Terms of Use before accessing any function. |
| BR-010 | Email verification before login | An account may log in only after its email address has been verified. Unverified accounts are blocked and prompted to verify. |
| BR-011 | Account activation token expiry | The account activation link is valid for 48 hours from creation. If expired, the system allows resending a new link with another 48-hour validity. |
| BR-012 | User information format | Phone numbers must follow a valid format; the system warns if the format is invalid. Email must be a valid, system-unique address. |

**Table 3.234: 1. User Management & Authentication**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-007 | Role-Based Access Control (RBAC) | The system applies an RBAC model. Each user is assigned one or more roles; access rights are determined entirely by role. System roles: System Admin, School Administration, Medical Staff, Teacher, Parent, Raw Food Supplier, Service Caterers. |
| BR-008 | Default system roles are protected | Default system roles cannot be deleted. Only custom roles may be deleted, by System Admin, and only when no user (including deactivated users) is currently assigned that role. |
| BR-013 | Staff account creation authority | Only School Administration can create staff accounts for its own school. System Admin can create user accounts system-wide. |

**Table 3.235: 2. Role Management & Access Control (RBAC)**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-020 | One homeroom teacher per class | Each classroom may have only one homeroom teacher at a time. Assigning a new teacher automatically unlinks the previous one and logs the change. |
| BR-021 | Cascading school deactivation | Deactivating a school automatically suspends all of its classrooms, staff accounts, and student accounts. This action must be logged in the audit log. |
| BR-022 | Role-based visibility of school policies | Users can view policies only for their own school (per schoolId in JWT). Teacher, Medical Staff, Service Caterers may view their school's policies for daily operations. Cross-school access is always denied. |
| BR-024 | Valid grade level | Class grade level must be between 1 and 12 (Vietnamese general education system). |
| BR-025 | Maximum classroom capacity | Each classroom has a maximum capacity of 50 students. |
| BR-026 | Teacher assignment audit logging | Every homeroom teacher assignment or replacement must be recorded in the audit trail with timestamp and actor. |
| BR-028 | School GPS coordinate verification & lock | School GPS coordinates must be valid: latitude in -90.0 to 90.0, longitude in -180.0 to 180.0; out-of-range values are rejected. Once coordinates are confirmed (school becomes ACTIVE), coordinateLocked is set to true; while locked, coordinate updates are rejected (other fields � address, phone, description � remain editable; all location verification relies on GPS, not the text address). Unlock exception: only System Admin may reset coordinateLocked = false, and must supply a written reason, logged in the audit log (actor, time, reason); after new coordinates are saved, coordinateLocked automatically returns to true. Default geofence radius: 200 meters from the school's center coordinate, configurable by School Administration between 50 m and 500 m; any radius change must be logged in the audit log. |

**Table 3.236: 3. School & Class Management**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-030 | Student�parent linkage | Each student must be linked to at least one parent account. Allergy information and special dietary requirements must be declared and stored in the student profile. |
| BR-031 | Health information review | Health and allergy information declared by parents must be reviewed and confirmed by Medical Staff before taking effect. |
| BR-032 | Bulk import of students & parents | The system supports bulk import of student and parent profiles via a predefined template with data validation rules. |

**Table 3.237: 4. Student & Parent Management**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-040 | Supplier registration process | A supplier must submit a complete application: company profile, business license, food safety certification, and product information. The application must be reviewed and approved by School Administration before the supplier may operate. |
| BR-041 | Supplier status lifecycle | Supplier status can be Pending ? Approved / Rejected. Only suppliers in Approved status may deliver ingredients to a school. |
| BR-046 | Required compliance documents | Suppliers must maintain valid compliance documents: food safety certification, business license, product origin documents. Expired documents trigger a warning and may affect partnership status. |
| BR-047 | Document expiry warning | The system displays an "Expiring Soon" warning when a supplier document has fewer than 30 days remaining before expiry. |
| BR-048 | Supplier rejection | When rejecting a supplier, the rejection reason must be recorded in the system and a notification sent to the supplier. |
| BR-050 | Delivery staff registration | Each supplier delivery staff member must be registered with: full name, national ID number, phone number, license plate. The national ID number must be unique in the system. |
| BR-051 | Face biometric authentication | Delivery staff must register facial biometric data to be authenticated at the school gate. A supplier may delete a delivery staff member's biometric data, revoking gate access. Note: biometric authentication is not applied to students (children), for personal data protection (PDPD) reasons. |
| BR-060 | GPS location verification on delivery | The system verifies the delivery staff's GPS location to ensure delivery occurs within the permitted area. |

**Table 3.238: 5. Supplier & Caterer Management**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-070 | Supplier status check on receiving | At the start of a receiving session, the system automatically verifies that the supplier is in Approved status; if not, the receiving session is rejected. |
| BR-071 | Mandatory batch certification | Every ingredient batch received must include: batch number, production date, expiry date, quantity, and relevant certificates. |
| BR-072 | Mandatory evidence photos on receiving | Receiving staff must photograph the ingredients, labels, packaging, and supporting documents as evidence. |
| BR-073 | Automatic GPS & timestamp logging | The system automatically records GPS location, device information, and timestamp during the receiving process, as an anti-fraud measure. |
| BR-074 | Receiving location verification | The system verifies that the receiving activity is performed within the school's permitted receiving area. |
| BR-075 | FEFO principle (First Expired, First Out) | The system prioritizes ingredient usage by FEFO � the batch expiring soonest is issued from stock first. |
| BR-076 | Ingredient batch traceability | The system records the ingredient batch number linked to each menu and production run to ensure full traceability. |
| BR-077 | Low-stock alert | The system automatically raises a low-stock alert when inventory is insufficient to meet the planned meal requirements. |
| BR-078 | Rejecting a non-conforming ingredient batch | A batch that fails quality inspection may be rejected; the rejection reason must be recorded with supporting evidence photos. |

**Table 3.239: 6. Ingredient & Inventory Management**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-061 | Menu submission deadline | A menu must be submitted for approval at least 3 days before its intended week of use. |
| BR-062 | Age-group nutrition targets | A menu must meet age-group nutrition targets (calories, nutrient diversity, meal composition balance). |
| BR-063 | Allergy conflict check | The system must check for allergy conflicts between menu ingredients and student allergy profiles before menu approval. If a serious conflict is detected, submission is blocked. |
| BR-064 | Inventory-aware menu optimization | AI menu suggestions must prioritize ingredients already available in stock and comply with the configured cost cap. |
| BR-065 | Mandatory special meal plans | For students with verified allergies or special diets, the system requires Service Caterers to create a separate meal plan. |
| BR-066 | Special diet categories | The system supports special diet types: Halal, Vegetarian, Gluten-Free, Nut-Free, and others. |
| BR-067 | Locking an approved menu | A menu in Approved or Published status cannot be edited or deleted. Only menus in Draft or Rejected status may be edited. |
| BR-068 | Menu approval workflow | A menu must follow the workflow: Draft ? Pending Approval ? Approved / Rejected ? Published. Only School Administration may approve or reject a menu. |
| BR-069 | Complete ingredient declaration | Service Caterers must declare all ingredients and potential allergens for every dish on the menu before submitting for approval. |
| BR-080 | Menu cost cap | A menu must comply with the cost cap configured by School Administration. |
| BR-081 | Substitute dish for common allergies | A menu must provide a substitute dish for the 5 most common food allergies, where applicable. |
| BR-082 | Separate preparation for special meals | Special meals must be prepared entirely separately, using dedicated cooking equipment to avoid cross-contamination. |

**Table 3.240: 7. Menu & Diet Management**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-079 | Mandatory three-step inspection | Every food batch must go through exactly 3 inspection steps: Step 1 � pre-cooking ingredient check (quality, freshness, packaging, batch info); Step 2 � post-cooking food check (appearance, smell, texture, doneness); Step 3 � pre-serving meal check (quality, portioning, serving conditions, hygiene). |
| BR-083 | Evidence recording at every step | Every inspection step must be accompanied by evidence photos, GPS location, and timestamp, recorded automatically. |
| BR-090 | Minimum post-cooking food temperature | Food after cooking must reach a minimum core temperature of 75�C (checked at Step 2). |
| BR-091 | Minimum holding temperature | Cooked food awaiting service must be held at a minimum of 60�C (checked at Step 3). |
| BR-092 | Ingredient expiry check | The system automatically checks the expiry date of ingredient batches used during the inspection step. |
| BR-093 | Complete inspection record | Every three-step inspection session must produce a complete inspection record linked to the ingredient batch and meal production record. |
| BR-094 | Block serving on failed inspection | A meal may not be served if any of the three inspection steps fails. |
| BR-095 | Non-conformance recording | When an inspection step fails, the system must record non-conformance details and automatically create a Corrective Action Request. |
| BR-096 | Re-inspection after corrective action | After a corrective action is completed, the failed inspection step must be re-performed (re-inspection). |
| BR-097 | Integration with incident management | A seriously failed inspection result must be automatically linked to the incident management system. |
| BR-098 | Meal approval for serving | The system approves a meal for serving only after all 3 steps have Passed. |
| BR-099 | Inspection record access rights | Medical Staff and School Administration may view all inspection records. Service Caterers may view only records from inspection sessions they themselves performed. |

**Table 3.241: 8. Three-Step Food Inspection**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-105 | Mandatory retention after passed inspection | After a food batch passes the 3-step inspection, Service Caterers must retain a food sample. |
| BR-106 | Mandatory sample labeling | Every retained sample must be labeled with: dish name, production date, retention date, and batch reference number. |
| BR-107 | Mandatory sample photos | Staff must photograph the retained samples and their labels as operational evidence. |
| BR-108 | Automatic GPS & timestamp on retention | The system automatically records GPS, device information, and timestamp during the retention process. |
| BR-109 | Retention location verification | The system verifies that sample retention is performed in the designated kitchen or storage area. |
| BR-110 | Sample storage temperature | The retention refrigerator must be kept between 2�C and 8�C. The system raises an alert if the temperature deviates from this range for more than 30 minutes. |
| BR-111 | Minimum retention period | The system automatically tracks the required retention period per food safety regulations. Samples must be kept for at least the minimum required duration. |
| BR-112 | Sample disposal after retention period | After the mandatory retention period elapses, staff dispose of the sample per procedure; the system records the disposal details, the responsible staff member, and the timestamp. |
| BR-113 | Sample recall on incident | When a food safety incident occurs, Medical Staff may recall a retained sample for investigation; the system records the recall, its investigative purpose, and the actor. |
| BR-114 | Linking samples to menu and inspection | A sample retention record must be linked to the related menu, inspection record, and meal production batch. |

**Table 3.242: 9. Food Sample Retention**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-120 | Class-based meal distribution confirmation | The Teacher confirms meal distribution to students in their own class. |
| BR-121 | Student meal history tracking | The system records and tracks each student's meal consumption history. |
| BR-123 | Allergy alert on student ID scan | When a student with a registered food allergy is scanned during a serving session, the system automatically triggers a visual and audible alert on the serving screen to prevent incorrect meal distribution. |
| BR-125 | Viewing special meal requests during serving | Teachers and serving staff must be able to view each student's special meal requirements before and during the serving session. |
| BR-130 | Prevent duplicate distribution | The system blocks duplicate meal distribution � a student cannot be scanned twice within the same serving session. |
| BR-133 | Daily meal summary for parents | The system automatically compiles the served menu and nutrition breakdown, sending a daily summary notification to parents. |
| BR-135 | Meal serving delay notification | The system automatically notifies the homeroom teacher and parents when meal serving is delayed by more than 20 minutes from the planned time. |

**Table 3.243: 10. Meal Serving & Distribution**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-146 | Automatic incident investigation trigger | The system automatically initiates a food safety incident investigation when 3 or more students report similar symptoms within 24 hours. |
| BR-147 | Incident severity classification | Medical Staff is responsible for classifying the severity of an incident. |
| BR-148 | Immediate alert for high-severity incidents | An incident classified as High Severity must immediately trigger alerts to: school management, the catering company, and local health authorities. |
| BR-149 | Automatic batch quarantine on incident | When a food safety incident is reported, the system automatically sets Quarantine status on all suspect food batches and related ingredients in stock. |
| BR-150 | Root cause investigation and analysis | The system must support Root Cause Analysis with the ability to attach evidence. |
| BR-151 | Traceability map generation | Within 4 hours of an incident report, the system automatically compiles a traceability map for all related ingredient batches and suppliers. |
| BR-152 | CAPA management (Corrective and Preventive Action) | After every incident, a CAPA plan must be created, assigned, and tracked to completion. |
| BR-153 | Corrective action approval | School Administration may approve or reject proposed corrective actions. |
| BR-154 | Formal incident report | Medical Staff may generate a formal incident report for record-keeping and export. |
| BR-155 | Closing an incident | Only School Administration may close an incident, after confirming completion of corrective actions. |

**Table 3.244: 11. Food Incident Management**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-089 | Refrigerator temperature monitoring | The system continuously monitors refrigerator temperature and raises an alert when the temperature deviates from the allowed range for more than 30 minutes. |
| BR-100 | Daily hygiene score | The system must record and track the kitchen's daily hygiene score. |
| BR-101 | Monthly safety & hygiene inspection | The system must store the results of periodic monthly safety and hygiene inspections. |
| BR-102 | Pest control log | The system must store a pest control log for the kitchen. |
| BR-103 | Kitchen staff health certificate validity | The system must track the validity of kitchen staff health certificates per regulation. |
| BR-104 | Chef hygiene score | Kitchen staff hygiene scores are aggregated and used in the Food Safety Compliance Report. |

**Table 3.245: 12. Food Safety Monitoring**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-138 | Critical alerts bypass approval workflow | Critical food safety alerts (e.g., a food recall) are broadcast immediately to all relevant users without going through an administrator approval workflow. |
| BR-139 | Feedback submission time window | Parents and teachers may submit meal feedback only within 24 hours of the meal being served. |
| BR-140 | Mandatory quality-concern tag on low rating | When a parent rates below 3 stars, the system requires selection of a quality-concern tag from a predefined list. |
| BR-141 | Weekly feedback aggregation | The system automatically compiles an aggregated feedback chart weekly and sends a report to catering management and school leadership. |
| BR-142 | Illness-keyword detection in feedback | The system automatically analyzes parent feedback content, detecting illness-related keywords (e.g., "stomachache", "vomiting"). When detected, the system triggers an urgent alert and immediately notifies Medical Staff for review. |
| BR-143 | Caterer responses to feedback | Service Caterers may respond to parent feedback. |
| BR-144 | Announcement revision history | Every announcement update must create a revision history entry with timestamp and actor ID. |
| BR-145 | Anonymized aggregate feedback data | Feedback data is anonymized and aggregated into the school's dashboard. |
| BR-158 | Default quiet hours | By default, the system does not send non-urgent push notifications during 22:00�06:00. Urgent alerts are not affected by quiet hours. |
| BR-159 | Retry and fallback on notification failure | When a push notification fails to send, the system automatically retries; after exhausting retries, it falls back to sending a fallback email. |

**Table 3.246: 13. Feedback & Notification**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-156 | AI-based menu suggestion | The Gemini AI Service analyzes nutrition requirements, student allergy profiles, available inventory, and cost caps to suggest an optimized menu. AI output includes nutrition analysis, ingredient list, and allergy warning flags. |
| BR-157 | Mandatory AI conversation archiving | All conversations between users and the AI (including AI menu suggestions) must be archived for compliance and audit purposes. |
| BR-160 | Automatic database backup | The system performs a full database backup every Sunday at 02:00 AM. The backup file is AES-256 encrypted and replicated to off-site cloud storage. |
| BR-161 | Backup file protection | The backup file must be encrypted and stored separately from the primary infrastructure. |
| BR-162 | Immutable audit log | The audit trail must be immutable � no one may edit or delete audit records. |
| BR-163 | External integration connectivity check | When updating configuration for an external integration (GPS, Gmail, Firebase, Gemini AI), the system must verify a successful connection before saving the new configuration. |
| BR-164 | Encryption of integration credentials | All external service credentials (API keys, OAuth credentials) must be encrypted at rest. |
| BR-165 | Weekly system health report | The system automatically compiles a weekly system health and active-user report for System Admin. |

**Table 3.247: 14. AI & Support Services**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-170 | Logging all significant actions | All significant actions in the system (create, update, delete, approve, reject) must be recorded in the audit trail with actor, timestamp, and affected object. |
| BR-171 | Log retention policy | The system must have a Log Retention Policy defining the minimum log retention period. |
| BR-172 | Log integrity protection | The system must have a mechanism to protect the integrity of logs, preventing any unauthorized modification. |
| BR-173 | Personal data protection (PDPD) | The system must comply with Vietnam's Personal Data Protection Decree (PDPD). Children's (students') biometric data must not be collected, for personal data protection reasons. |
| BR-174 | Operational history traceability | The system must support querying operational history and tracking the audit trail over time. |

**Table 3.248: 15. Audit Log & Data Security**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-175 | Role-based report access | Access to each report type is controlled by role (RBAC): School Administration � all operational reports of its school; Medical Staff inspection, medical incident, and safety compliance reports; Service Caterers only reports related to their own activity (receiving, inspection, incidents); System Admin all reports system-wide. |
| BR-176 | School-scoped report data | Report data must be scoped to the user's own school. Only System Admin may view cross-school aggregate reports. |
| BR-177 | Report history retention | Generated reports must be retained for at least 12 months. Every export (who, when, report type) must be logged in the audit trail. |
| BR-178 | Report export formats | The system supports export in two formats: PDF and Excel (XLSX). Each export file must include: report title, data period, school name, and export timestamp. |
| BR-179 | Scheduled automated report export | The system supports scheduling periodic report generation and delivery (daily, weekly, monthly), sent to the email list configured by School Administration. On send failure, the system retries up to 3 times; if still unsuccessful, it logs the error and notifies System Admin. |
| BR-180 | Real-time dashboard data | The Analytics Dashboard must display data refreshed within 5 minutes of an operational change. Dashboard content includes: food safety risk indicators, inspection compliance rate, daily meal summary. |
| BR-181 | Report data security & anonymization | Reports must not disclose personally identifiable information of students in aggregated data. Reports exported outside the system must be marked "Confidential" if they contain sensitive operational or incident information. |
| BR-182 | Report data integrity & accuracy | Report data must be computed directly from the original data source in the system; manual editing after report generation is not permitted. Reports must clearly display the generation date/time and the data period covered. |

**Table 3.249: 16. Reporting & Analytics**

| BR Code | Rule | Description |
| --- | --- | --- |
| BR-196 | Offline evidence capture allowed | Evidence-only actions (no server-side verification needed to decide pass/fail) may be performed offline: photo + GPS + timestamp capture during three-step inspection (BR-083), ingredient receiving (BR-072), food sample retention (BR-107), and filling inspection checklists. Data is stored in a local sync queue on the device and automatically uploaded once connectivity is restored. |
| BR-197 | Real-time safety verification requires connectivity | The following actions may not be performed offline, even with stale cached data on the device: allergy alert on student ID scan (BR-123); supplier Approved-status check before receiving (BR-070); batch Quarantine-status check (BR-149); face biometric authentication (BR-051). When offline, the system blocks the action and displays a connectivity-required message � cached data must never be used to silently allow the action through. |
| BR-198 | Automatic incident aggregation requires server connectivity | Automatically triggering an incident investigation when 3+ students report similar symptoms within 24 hours (BR-146) requires real-time server-side aggregation � it cannot be performed or simulated offline on-device. |
| BR-199 | Sync conflict resolution | When two devices edit the same record while offline, the system applies last-write-wins by server timestamp (not device clock). Both writes must be recorded in the immutable audit log (BR-162) so no write is silently lost, including the overwritten one. |
| BR-200 | Offline cache validity window | Cached data usable offline (menus, class lists, student lists, etc.) is valid for at most a defined number of hours since the last successful sync. Once expired, the relevant screens show a "Needs re-sync" state and restrict new write actions until sync succeeds. |

**Table 3.250: 17. Offline & Data Sync**

### 3.5.2 System Messages

**1. Error Messages**

| MSG Code | Message Type | BR Reference | Content |
| --- | --- | --- | --- |
| MSG-001 | Error | � | System error while saving school data. Please try again. |
| MSG-002 | Error | � | Invalid credentials. Please check your email and password. |
| MSG-003 | Error | � | Your account has been deactivated. Please contact your administrator. |
| MSG-004 | Error | BR-004 | Session expired. Please log in again. |
| MSG-005 | Error | � | Access denied: You do not have permission to access this resource. |
| MSG-006 | Error | BR-012 | Registration failed: The email address is already in use. |
| MSG-007 | Error | � | Invalid tax ID format. Please verify and try again. |
| MSG-008 | Error | � | File upload failed: File size exceeds the maximum limit of 10MB. |
| MSG-009 | Error | � | File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed. |
| MSG-010 | Error | BR-025 | Classroom capacity exceeded. Maximum allowed is 50 students. |
| MSG-011 | Error | BR-020 | Homeroom teacher is already assigned to another classroom. |
| MSG-012 | Error | � | Student admission code is invalid or has already been used. |
| MSG-013 | Error | BR-046, BR-048 | Supplier food safety certificate has expired. Onboarding blocked. |
| MSG-014 | Error | BR-051 | Face recognition failed: Biometric identity could not be verified. |
| MSG-015 | Error | BR-074, BR-079 | Receiving action blocked: GPS location verification failed (out of tolerance). |
| MSG-016 | Error | BR-095 | Checklist validation failed: Mandatory safety checks are missing. |
| MSG-017 | Error | BR-090, BR-093 | Cooking temperature too low: Meal core temperature must reach 75�C. |
| MSG-018 | Error | BR-077 | Inventory shortage: Insufficient stock of ingredient [Ingredient Name]. |
| MSG-019 | Error | BR-107 | Food sample retention weight too low: Must be at least 100g. |
| MSG-020 | Error | BR-110 | Retention refrigerator temperature is out of safe range (2�8�C). |
| MSG-021 | Error | BR-130 | Duplicate meal distribution: Student has already scanned for this session. |
| MSG-022 | Error | BR-139 | Feedback submission blocked: Outside the 24-hour limit of meal serving. |
| MSG-023 | Error | � | Incident report incomplete: Symptoms, date of ingestion, and dishes are required. |
| MSG-024 | Error | BR-149 | Kitchen quarantine is active: Operation blocked due to safety violations. |
| MSG-025 | Error | � | Database transaction failed. Rollback executed. |
| MSG-075 | Error | BR-068 | Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week. |
| MSG-076 | Error | BR-002 | Account temporarily locked due to too many failed login attempts. Please try again after [Minutes] minutes. |
| MSG-077 | Error | BR-197 | Action blocked: Internet connection required to verify this safety check. Please reconnect and try again. |

**Table 3.251: Error messages block the user's action and require a corrective step.**

**2. Success Messages**

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

**Table 3.252: Confirmation messages indicating an action completed successfully.**

**3. Warning Messages**

| MSG Code | Message Type | BR Reference | Content |
| --- | --- | --- | --- |
| MSG-051 | Warning | BR-063 | Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students. |
| MSG-052 | Warning | BR-047, BR-083 | Ingredient batch [Batch ID] is close to expiration ([Expiration Date]). |
| MSG-053 | Warning | BR-047 | Supplier food safety certificate is expiring in [Number] days. |
| MSG-054 | Warning | BR-077 | Safety stock level reached for ingredient [Ingredient Name]. Reorder recommended. |
| MSG-055 | Warning | BR-089 | High temperature alert in inventory storage area: [Temperature]�C. |
| MSG-056 | Warning | BR-096, BR-098 | Re-inspection required: Failed safety check during inspection step [Step Name]. |
| MSG-057 | Warning | BR-140 | Negative meal feedback rating (<3 stars) received from parent. |
| MSG-058 | Warning | BR-142 | Illness keywords detected in parent feedback. Potential incident flag. |
| MSG-059 | Warning | BR-110, BR-089 | Retention refrigerator door left open or temperature fluctuation detected. |
| MSG-060 | Warning | � | Supplier monthly performance index score has dropped below 70%. |
| MSG-061 | Warning | � | System load threshold reached. Auto-scaling initiated. |
| MSG-062 | Warning | BR-002 | Suspicious login attempt detected from a new location/device. Verification code required. |
| MSG-063 | Warning | BR-006 | Weak password rating. Recommend adding special characters and numbers. |
| MSG-064 | Warning | BR-031 | Incomplete student health profiles detected for class [Class Name]. |
| MSG-065 | Warning | BR-135 | Delivery delay alert: Supplier delivery time deviates from schedule. |
| MSG-066 | Warning | � | Weekly sanitation audit report is overdue. Please submit immediately. |
| MSG-067 | Warning | BR-062 | AI menu analysis: Nutritional diversity is below the required 3-group minimum. |
| MSG-068 | Warning | BR-159 | Push notification delivery queue is backed up. Retrying failed targets. |
| MSG-069 | Warning | BR-163 | External API response latency is high (exceeds 2000ms). |
| MSG-070 | Warning | BR-160 | Backup space alert: Backup storage utilization is at 92% capacity. |
| MSG-073 | Warning | � | Gemini AI Service is currently unavailable. Please try again later. |
| MSG-079 | Warning | BR-200 | Cached data is outdated ([Number] hours old). Please reconnect to sync before continuing. |

**Table 3.253: Warnings that do not block the action but require attention or preventive follow-up.**
