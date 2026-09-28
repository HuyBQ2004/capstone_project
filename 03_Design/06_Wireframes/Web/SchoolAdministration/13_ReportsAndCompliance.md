# Wireframe — Reports & Compliance (School Administration)

**Nguồn**: `05_ScreenSpecifications/Web/SchoolAdministration/13_FoodSampleRetentionReports.md`, `14_ReceivingReports.md`, `15_InspectionReports.md`, `16_PartnershipReports.md`, `17_PartnershipApprovalHistory.md`, `18_ComplianceDashboard.md`, `35_SystemUsageMetrics.md`, `36_TraceabilityReports.md`, `39_AllergyReview.md`
**Công cụ sinh**: Thủ công (agilek/wireframer-skill — vanilla HTML/JS, hand-drawn Balsamiq style)
**File/Link**: `03_Design/06_Wireframes/Web/SchoolAdministration/13_ReportsAndCompliance.html` (single-file SPA, 10 `<div class="screen">`, điều hướng bằng `showScreen(id)`, không router)

> **Nguồn Draft**: Toàn bộ Screen Specification nguồn đang ở trạng thái Draft, CHƯA qua bước QA của Nguyễn Phương Thảo (mục 6, `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md`) — Wireframe này bám sát nguyên văn Draft hiện có theo quyết định của Nguyễn Văn Nam (leader), cần rà lại nếu Screen Spec đổi sau QA. Lưu ý thêm: nhiều field trong nhóm này đang đánh dấu [Giả định] trong Screen Spec gốc — Wireframe giữ nguyên các giả định đó, cần Thảo/team xác nhận trước khi lên Prototype chính thức.

## Danh sách màn hình

| STT | Màn hình | ID trong SPA | File nguồn (Screen Spec) | UC liên quan |
| --- | --- | --- | --- | --- |
| 1 | Compliance Dashboard | `compliance-dashboard` | `18_ComplianceDashboard.md` | UC-027 |
| 2 | Inspection Reports | `inspection-reports` | `15_InspectionReports.md` | UC-113 |
| 3 | Analytics Dashboard (hub báo cáo — không phải Screen Spec chính thức, chỉ card-list điều hướng) | `analytics-hub` | *(không có file spec riêng — dựng theo ghi chú điều hướng `02_School_Administration_Screen_Flow.md`)* | UC-171 |
| 4 | Food Sample Retention Reports | `food-sample-retention` | `13_FoodSampleRetentionReports.md` | UC-171/UC-172 |
| 5 | Receiving Reports | `receiving-reports` | `14_ReceivingReports.md` | UC-123 |
| 6 | Traceability Reports | `traceability-reports` | `36_TraceabilityReports.md` | UC-171/UC-172 |
| 7 | System Usage Metrics | `system-usage-metrics` | `35_SystemUsageMetrics.md` | UC-171 |
| 8 | Partnership Reports | `partnership-reports` | `16_PartnershipReports.md` | UC-171/UC-172 |
| 9 | Partnership Approval History | `partnership-approval-history` | `17_PartnershipApprovalHistory.md` | UC-106 |
| 10 | Allergy Review | `allergy-review` | `39_AllergyReview.md` | *(chưa có UC riêng — TODO)* |

**Ghi chú điều hướng**: Sidebar chỉ nhóm "Báo cáo & Thống kê" là clickable trong file này (theo `Navigation_Guidelines.md` — School Administration có nhóm menu "Báo cáo"). Các nhóm menu khác (Quản lý Lớp/Học sinh, Quản lý Thực đơn, Quản lý Nhà cung cấp, Kiểm tra & Sự cố) hiển thị dạng disabled, không chức năng thật vì ngoài phạm vi màn hình này. Nút "◀ Quay lại Dashboard chính" ở mỗi màn không có chức năng thật (theo yêu cầu). Analytics Dashboard là hub điều hướng nội bộ tới 5 view lọc (Food Sample/Receiving/Traceability/Usage/Partnership Reports).

## Checklist đối chiếu Screen Specification

### 1. Compliance Dashboard (`18_ComplianceDashboard.md`)
- [x] Đủ 6 field: (1) trạng thái 3-bước realtime, (2) điểm vệ sinh hàng ngày, (3) alert cảnh báo bước thất bại/quá hạn (link CAPA), (4) gallery ảnh xác minh, (5) banner Kitchen Quarantine (cố định, không tự ẩn), (6) nút "Ký xác nhận trạng thái an toàn hôm nay"
- [x] Grayscale/mono — badge phân biệt bằng viền (double border = Fail/critical, dashed = pending)
- [x] Trạng thái Error (banner Quarantine + alert bước 2 Fail) và Success (đã hiển thị) thể hiện đồng thời
- [x] Không thêm field ngoài bảng Business Logic

### 2. Inspection Reports (`15_InspectionReports.md`)
- [x] Đủ field: Filter Date range, Filter Meal session, Filter Service Caterers, nút Generate Report, bảng tổng hợp (tổng lô, tỷ lệ pass/fail, lệch nhiệt độ, trạng thái CAPA), nút Export Report
- [x] Trạng thái Success thể hiện (bảng có dữ liệu); Empty/Loading chỉ mô tả trong spec, không dựng riêng màn phụ (giữ 1 view)
- [x] Không thêm field ngoài bảng Business Logic

### 3. Analytics Dashboard (hub — không chính thức)
- [x] Chỉ dựng card-list dẫn tới đúng 4 màn: Food Sample Retention / Receiving / Traceability / System Usage Metrics (+ Partnership Reports theo sơ đồ điều hướng), không thêm field riêng
- [x] Không có bảng Business Logic riêng vì không phải 1 trong 9 Screen Spec chính thức

### 4. Food Sample Retention Reports (`13_FoodSampleRetentionReports.md`)
- [x] Đủ field: Filter Date range **[Giả định]**, Bảng tổng hợp lưu mẫu **[Giả định]**, Cảnh báo nhiệt độ tủ lưu mẫu vi phạm **[Giả định]**, nút Export PDF/Excel
- [x] Trạng thái Success **[Giả định]** thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 5. Receiving Reports (`14_ReceivingReports.md`)
- [x] Đủ field: Filter Date range **[Giả định]**, Filter Supplier **[Giả định]**, Bảng tổng hợp nhận hàng (số lô nhận/từ chối, tỷ lệ đúng hạn/GPS hợp lệ), nút Export PDF/Excel
- [x] Trạng thái Success **[Giả định]** thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 6. Traceability Reports (`36_TraceabilityReports.md`)
- [x] Đủ field: Filter Date range **[Giả định]**, Filter theo Batch ID/Supplier **[Giả định]**, Bảng truy xuất lô nguyên liệu → menu → phiên sản xuất **[Giả định]**, nút Export PDF/Excel
- [x] Trạng thái Success **[Giả định]** thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 7. System Usage Metrics (`35_SystemUsageMetrics.md`)
- [x] Đủ field: Filter Date range **[Giả định]**, Chart Active users (staff/parent) theo ngày **[Giả định]**, Bảng Top tính năng dùng nhiều nhất **[Giả định]**, nút Export PDF/Excel
- [x] Trạng thái Success **[Giả định]** (ghi chú cập nhật trong 5 phút — BR-180) thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 8. Partnership Reports (`16_PartnershipReports.md`)
- [x] Đủ field: Filter Date range **[Giả định]** (BR-177), Filter loại đối tác (Supplier/Service Caterers) **[Giả định]**, Bảng tổng hợp (số đăng ký mới, tỷ lệ approve/reject, thời gian xử lý trung bình) **[Giả định]**, nút Export PDF/Excel
- [x] Trạng thái Success **[Giả định]** thể hiện
- [x] Không thêm field ngoài bảng Business Logic

### 9. Partnership Approval History (`17_PartnershipApprovalHistory.md`)
- [x] Đủ field: Filter Date range **[Giả định]**, Filter loại đối tác **[Giả định]**, Filter kết quả (Approved/Rejected) **[Giả định]**, Bảng lịch sử duyệt đọc-only (Tên đối tác, Loại, Kết quả, Người duyệt, Ngày, Lý do nếu Reject) **[Giả định — cấu trúc cột dựa trên Normal Flow bước 7-9 của UC-004/UC-007]**
- [x] Trạng thái Success thể hiện (bảng đầy đủ, 1 dòng Approved + 1 dòng Rejected có lý do)
- [x] Không thêm field ngoài bảng Business Logic

### 10. Allergy Review (`39_AllergyReview.md`)
- [x] Đủ field: Filter theo Class/Grade **[Giả định]**, Filter theo loại dị ứng **[Giả định]**, Bảng danh sách hồ sơ dị ứng đã xác nhận đọc-only (Học sinh, Lớp, Loại dị ứng, Trạng thái xác nhận), nút Export PDF/Excel
- [x] Không có action Approve/Reject (đúng theo spec — quyền riêng Medical Staff UC-078)
- [x] Trạng thái Success thể hiện (chỉ xem)
- [x] Không thêm field ngoài bảng Business Logic

## Tổng hợp các trường hợp "[Giả định]" theo màn hình (để báo Nguyễn Phương Thảo)

- **Food Sample Retention Reports**: toàn bộ 3/4 field (Filter Date range, Bảng tổng hợp lưu mẫu, Cảnh báo nhiệt độ tủ lưu mẫu) + toàn bộ 4 trạng thái Loading/Empty/Success đều [Giả định].
- **Receiving Reports**: Filter Date range, Filter Supplier là [Giả định]; toàn bộ trạng thái Loading/Empty/Success [Giả định].
- **Partnership Reports**: toàn bộ 3/4 field (Filter Date range, Filter loại đối tác, Bảng tổng hợp) [Giả định]; toàn bộ trạng thái [Giả định]; Entry/Exit point cũng [Giả định] — team cần xác nhận có tách trang riêng hay không.
- **Partnership Approval History**: toàn bộ 3 filter [Giả định]; cấu trúc cột bảng lịch sử duyệt [Giả định — dựa trên Normal Flow bước 7-9 của UC-004/UC-007]; Entry/Exit point [Giả định].
- **System Usage Metrics**: toàn bộ 3/4 field (Filter Date range, Chart Active users, Bảng Top tính năng) [Giả định]; toàn bộ trạng thái [Giả định]; Entry/Exit point [Giả định].
- **Traceability Reports**: toàn bộ 3/4 field (Filter Date range, Filter Batch ID/Supplier, Bảng truy xuất) [Giả định]; toàn bộ trạng thái [Giả định]; Entry/Exit point [Giả định].
- **Allergy Review**: 2 filter (Class/Grade, loại dị ứng) [Giả định]; UC liên quan hoàn toàn chưa chính thức [Giả định — cần team xác nhận]; Loading/trạng thái Success [Giả định]; ghi chú thêm — nguồn BR cho quy tắc màu cảnh báo dị ứng trích dẫn "BR-123" trong Design_System.md/Accessibility.md nhưng mã này không tồn tại trong `business_rules.md` (đã grep toàn văn, không có kết quả) — do wireframe này tuân thủ grayscale tuyệt đối nên không áp dụng màu đỏ, không ảnh hưởng hiển thị, nhưng cần Thảo/team xác nhận lại nguồn BR thật.
- **Compliance Dashboard, Inspection Reports, Partnership Approval History** (phần trạng thái Loading/Error): không đánh dấu [Giả định] — đã có UC/BR tham chiếu rõ trong Screen Spec gốc.
