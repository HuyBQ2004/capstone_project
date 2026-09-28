# Prototype — Web App — Medical Staff

**Link công cụ**: [Stitch Project Link](https://stitch.withgoogle.com/projects/9673917523813382749)
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

## Luồng điều hiện thực trong Prototype

```mermaid
flowchart TD
    Dash["Medical Staff Dashboard (Web)"]

    Dash -->|✅| HRList["Student Health Record List"]
    HRList -->|✅| HRDetail["Student Health Record Detail"]
    HRDetail -->|✅| UpdateHR["Update Health Record"]
    HRDetail -->|✅| HealthHistory["Health History"]
    HRList -->|✅ ""tab Waiting for confirmation""| AllergyReview["Review Health/Allergy Information"]
    AllergyReview -->|✅| HRDetail

    Dash -->|✅| SMList["Special Meal Request List"]
    SMList -->|✅| SMApprove["Approve/Reject Special Meal Dialog"]

    Dash -->|✅| InspNew["Conduct Food Safety Inspection"]
    Dash -->|✅| MonitorDash["Monitor Food Safety Dashboard"]
    Dash -->|✅| InspRecords["Food Inspection Records"]
    Dash -->|✅| ComplianceReport["View Food Safety Compliance Report"]
    Dash -->|✅| FSReports["View Food Safety Reports"]
    Dash -->|✅| Policy["View School Policy"]

    Dash -->|✅| SampleRegister["Food Sample Records — Mẫu số 5"]
    SampleRegister -->|✅| RetrieveSample["Retrieve Food Sample Records Dialog"]

    Dash -->|✅| IncList["Incident List"]
    IncList -->|✅| IncDetail["Incident Detail"]
    IncDetail -->|✅| AssignSev["Assign Incident Severity"]
    IncDetail -->|✅| InvWorkspace["Investigation Workspace"]
    InvWorkspace -->|✅| RetrieveSample
    IncDetail -->|✅| Validation["Incident Validation"]
    IncDetail -->|✅| GenReport["Generate Incident Report"]

    Dash -->|✅| ViolForm["Record Staff/Process Violation"]
    ViolForm -->|✅| ViolResolution["Record Violation Resolution"]
```

## Ghi chú review
*(Sẽ được điền sau khi hoàn thành UAT và rà soát chéo)*
