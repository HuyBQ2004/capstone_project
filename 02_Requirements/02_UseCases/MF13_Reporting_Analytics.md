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

