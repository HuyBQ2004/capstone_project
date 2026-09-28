# Prototype — Mobile App — Medical Staff

**Link công cụ**: [Stitch Project Link](https://stitch.withgoogle.com/projects/1269773278099955812)
**Phạm vi màn hình**: 
- `01_MedicalStaffDashboard.md`
- `02_ReviewHealthInformationScreen.md`
- `03_StudentHealthRecordList.md`
- `04_StudentHealthRecordDetail.md`
- `05_UpdateHealthRecordScreen.md`
- `06_HealthHistoryScreen.md`
- `07_ReviewSpecialMealRequestScreen.md`
- `08_SpecialMealRequestList.md`
- `09_ApproveSpecialMealDialog.md`
- `10_RejectSpecialMealDialog.md`
- `11_FoodInspectionRecords.md`
- `12_ConductFoodSafetyInspectionScreen.md`
- `13_FoodSampleRecords.md`
- `14_RetrieveFoodSampleRecordsDialog.md`
- `15_ViewFoodSafetyReportsScreen.md`
- `16_ViewSchoolPolicyScreen.md`
- `17_InvestigationWorkspace.md`
- `18_AssignIncidentSeverityDialog.md`
- `19_RecordCorrectiveActionsReportScreen.md`
- `20_GenerateIncidentReportDialog.md`
- `21_RecordProcessViolation.md`
- `22_RecordStaffViolation.md`
- `23_ComplianceDashboard.md`
- `24_RecordViolationResolution.md`
- `25_IncidentList.md`
- `26_IncidentDetail.md`
- `27_ViolationRegistry.md`

**Ngày dựng**: 2026-07-02 | **Trạng thái**: Draft cho Review

## Luồng điều hướng đã hiện thực trong Prototype

```mermaid
flowchart TD
    Dash["Medical Staff Dashboard"]

    Dash -->|✅ ""Health Profile"" button| HR["Health Record\n(≈ Student Health Record List)"]
    HR -->|✅ ""All students / Waiting for confirmation tabs""| HRCard["Student Profile Card"]
    HRCard -->|✅| HRDetail["Student Health Record Detail"]
    HRDetail -->|✅| UpdateHR["Update Health Record Screen"]
    HRDetail -->|✅| HealthHistory["Health History Screen"]

    Dash -->|✅ ""Student Allergy Review"" button| AllergyReview["Review Health Information Screen\n(allergy verification)"]
    AllergyReview -->|✅| HRDetail

    Dash -->|✅ ""Approve Special Meal"" button| SM["Special Meal\n(≈ Special Meal Request List)"]
    SM -->|✅ ""Awaiting approval / Approved tabs""| SMCard["Special Meal Card"]
    SMCard -->|✅| ApproveSM["Approve Special Meal Dialog"]
    SMCard -->|✅| RejectSM["Reject Special Meal Dialog"]

    Dash -->|✅ ""City Safety Incident"" button| IR["Incidents Records\n(≈ Incident List)"]
    IR -->|✅ ""Processing / Resolved tabs""| IRCard["Incident Card"]
    IRCard -->|✅| IRDetail["Incidents Records Detail\n(≈ Incident Detail)"]

    IRDetail -->|✅ ""Step 1: Assess the severity""| EvalSev["Evaluate the level Incidents\n(≈ Assign Incident Severity Dialog)"]
    IRDetail -->|✅ ""Step 2: Incident Investigation Space""| InvSpace["Investigation Space\n(≈ Investigation Workspace)"]
    IRDetail -->|✅ ""Step 3: Submit the incident report...""| ExportReport["Export To School Administration\n(≈ Generate Incident Report Dialog)"]

    InvSpace -->|✅ ""1. Extracting Stored Food Samples""| ExtractSample["Extraction of Stored Food Samples\n(≈ Retrieve Food Sample Records Dialog)"]
    InvSpace -->|✅ ""2. Gathering Evidence""| CollectEv["Collect Evidence"]
    InvSpace -->|✅ ""3. Identify the Root Cause""| RootCause["Root Cause documentation\n(within Investigation Space)"]
    InvSpace -->|✅ ""4. Proposed Remedial Measures""| Treatment["Treatment Measures\n(≈ Record Corrective Actions Report Screen)"]

    ExtractSample -->|✅ ""Submit for Testing""| ExportReport
    Treatment -->|✅ ""Establish Measures""| IRDetail

    Dash -->|✅ ""Food Compliance & Monitoring Board"" button| Compliance["Compliance Dashboard"]
    Compliance -->|✅| PolicyScreen["View School Policy Screen"]
    Compliance -->|✅| FSReports["View Food Safety Reports Screen"]

    Dash -->|✅ ""Conduct Kitchen Inspection"" button| InspNew["Conduct Food Safety Inspection Screen\nchecklist vệ sinh + pest control + khám SK NV"]

    Dash -->|✅ ""View food inspection results in 3 steps"" button| InspRecords["Food Inspection Records\n(≈ View Inspection Records Log)"]

    IRDetail -->|✅ ""validate CAPA đã approved""| Validation["Incident Validation Screen"]
    IRDetail -->|| GenReport["Generate Incident Report Dialog"]
    InvSpace -->|✅| AssignSev["Assign Incident Severity Dialog"]

    HRDetail -->|✅| HealthHistory2["Health History Screen"]
    ExtractSample -->|✅| RetrieveDialog["Retrieve Food Sample Records Dialog"]
    Dash -->|✅ ""Food Sample Register"" button| SampleRecords["Food Sample Records — Mẫu số 5"]

    Dash -->|✅ ""Report Violation"" button| ViolForm["Record Violation Form (UC-179, dialog hợp nhất)\nstaffName TÙY CHỌN, violationType, severity"]
    ViolForm -->|✅ ""MSG-088 nếu nghi tái diễn""| ViolForm
    IRDetail -.->|✅ ""phát hiện qua audit độc lập (staffName để trống)""| ViolForm
    ViolForm -->|✅ ""Record Resolution""| ViolResolution["Record Violation Resolution"]
```

## Ghi chú review
*(Sẽ được điền sau khi hoàn thành UAT và rà soát chéo)*
