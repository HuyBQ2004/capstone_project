# Screen Spec — View Food Safety Reports Screen

**Actor**: Medical Staff | **UC liên quan**: UC-117 (Report 3 §3.2, dòng 3623–3637); gộp chung UC-115 "View Food Safety Compliance Report" (dòng 3601–3617) theo `14_Screen_Authorization_Matrix.md` khối 6 | **Status**: Draft
**BR liên quan**: BR-100–104 (điểm vệ sinh/kiểm tra/pest control/khám sức khỏe), BR-110 (nhiệt độ bảo quản mẫu, 2–8°C)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 12/07/2026**: (1) gỡ mọi tham chiếu BR-089 (Deprecated theo `business_rules.md` Changelog v1.6 — xem BR-110 thay thế, chỉ áp dụng riêng tủ lưu mẫu 2–8°C, không có cảnh báo IoT tự động); (2) đổi nhóm "Pest Control Log" từ badge hiệu lực nhị phân sang **nhật ký sự kiện** (đồng bộ quyết định `review_medicalstaff_compliance_kitcheninspection.md` Mục 1, đã áp code `compliance/page.tsx`). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn nhóm báo cáo | Tab | 3 nhóm: Monthly Kitchen Audits, Pest Control Log, Refrigerator Temperature Logs (chỉ tủ lưu mẫu) | BR-101, BR-102, BR-110 | UC-117 bước 2 |
| 2 | Date range filter | Date range picker | — | — | |
| 3 | Refrigerator ID filter | Select | Chỉ áp dụng nhóm Temperature Logs (tủ lưu mẫu, BR-110) | BR-110 | |
| 4 | Kết quả báo cáo/xu hướng (Monthly Kitchen Audits, Temperature Logs) | Chart (recharts)/Table | — | BR-101, BR-110 | |
| 4b | Nhật ký kiểm soát côn trùng (nhóm Pest Control Log) | Table — mỗi dòng = 1 lần kiểm soát/xử lý | Cột: Ngày kiểm soát/xử lý, Đơn vị thực hiện, Loại côn trùng phát hiện (nếu có) + số lượng ước tính, Biện pháp xử lý, Ngày hẹn kiểm tra tiếp theo | BR-102 | Thay cho badge "Còn hiệu lực"/"Đã hết hạn" cũ — không phải chứng chỉ có hạn pháp lý mà là chuỗi sự kiện xử lý liên tục |
| 5 | Compliance summary dashboard (từ UC-115) | Card thống kê | Tổng hợp audit/pest control (bản ghi gần nhất + ngày hẹn tiếp theo)/khám SK NV/điểm vệ sinh/nhiệt độ tủ lưu mẫu | BR-100–104, BR-110 | |
| 6 | Export format (PDF/Excel) | Select | — | BR-178 | |
| 7 | Nút "Export" | Button | — | BR-178 | **Cập nhật 11/07/2026**: xuất đúng theo khoảng ngày đang chọn ở field 2 (Date range filter) — không xuất toàn bộ dữ liệu bất kể filter đang áp dụng (làm rõ theo phản hồi người dùng, tránh nhầm lẫn Export và Filter là 2 khoảng ngày độc lập) |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải báo cáo/biểu đồ.
- Empty: "Không có dữ liệu cho khoảng thời gian đã chọn" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: Tab chọn nhóm báo cáo trên Page Header, Content Area 2 khối (chart theo nhóm + Compliance Summary Dashboard), nút "Export" ở page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard (Mobile).
**Bố cục**: 1 cột, Tab trên cùng, filter dưới Tab, kết quả dạng `fl_chart`/Card cuộn dọc, Compliance Summary là section cuộn tới, nút "Export" nổi AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Food Safety Reports (gộp Compliance Report), nền tảng Web, actor Medical Staff.
Bố cục: Tab chọn nhóm báo cáo (Monthly Kitchen Audits / Pest Control Log / Refrigerator Temperature Logs),
filter ngày/refrigerator ID, chart kết quả (2 nhóm đầu) hoặc Table nhật ký sự kiện (nhóm Pest Control Log:
Ngày, Đơn vị thực hiện, Loại côn trùng + số lượng ước tính, Biện pháp xử lý, Ngày hẹn tiếp theo),
Compliance Summary Dashboard, nút Export.
Áp dụng Design System: warning #B45309 cho đánh giá quá hạn/nhiệt độ vượt ngưỡng.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3623–3637 (UC-117), dòng 3601–3617 (UC-115).
- business_rules.md (BR-100, BR-101, BR-102, BR-103, BR-104, BR-110, BR-178; BR-089 Deprecated — Changelog v1.6).
- error_message.md (MSG-004, MSG-005).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 1 (nhật ký kiểm soát côn trùng).
