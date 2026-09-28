# Screen Spec — View Food Safety Reports Screen

**Actor**: Medical Staff | **UC liên quan**: UC-117 (Report 3 §3.2, dòng 3623–3637); gộp chung nội dung UC-115 "View Food Safety Compliance Report" (dòng 3601–3617) theo ghi chú tại `14_Screen_Authorization_Matrix.md` khối 6 ("không có dòng riêng, gộp chung với View Food Safety Reports Screen") | **Status**: Draft
**BR liên quan**: BR-100 (điểm vệ sinh hàng ngày), BR-101 (kiểm tra hàng tháng), BR-102 (kiểm soát côn trùng), BR-103 (khám sức khỏe NV bếp), BR-104 (điểm vệ sinh đầu bếp), BR-110 (nhiệt độ bảo quản mẫu, 2–8°C)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Sửa 12/07/2026**: (1) gỡ mọi tham chiếu BR-089 (Deprecated theo `business_rules.md` Changelog v1.6 — xem BR-110 thay thế, chỉ áp dụng riêng tủ lưu mẫu, không có cảnh báo IoT tự động); (2) đổi nhóm "Pest Control Log" từ badge hiệu lực nhị phân sang **nhật ký sự kiện** (đồng bộ quyết định `review_medicalstaff_compliance_kitcheninspection.md` Mục 1, đã áp code `compliance/page.tsx`). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn nhóm báo cáo | Tab/Select | 3 nhóm: Monthly Kitchen Audits, Pest Control Log, Refrigerator Temperature Logs (chỉ tủ lưu mẫu) | BR-101, BR-102, BR-110 | UC-117 bước 2 |
| 2 | Date range filter | Date range picker | — | — | UC-117 bước 3 |
| 3 | Refrigerator ID filter (chỉ áp dụng nhóm Temperature Logs, tủ lưu mẫu) | Select | — | BR-110 | |
| 4 | Kết quả báo cáo/xu hướng (Monthly Kitchen Audits, Temperature Logs) | Chart/Table | — | BR-101, BR-110 | UC-117 bước 4 |
| 4b | Nhật ký kiểm soát côn trùng (nhóm Pest Control Log) | Danh sách `Card`/Table — mỗi mục = 1 lần kiểm soát/xử lý | Trường: Ngày kiểm soát/xử lý, Đơn vị thực hiện, Loại côn trùng phát hiện (nếu có) + số lượng ước tính, Biện pháp xử lý, Ngày hẹn kiểm tra tiếp theo | BR-102 | Thay cho badge "Còn hiệu lực"/"Đã hết hạn" cũ |
| 5 | **Khối Compliance Report (từ UC-115)**: Date range selector riêng | Date range picker | — | — | UC-115 bước 2–3 |
| 6 | Export format (PDF/Excel) | Select | — | BR-178 | |
| 7 | Compliance summary dashboard | Dashboard/Card | Tổng hợp audit hàng tháng, pest control (bản ghi gần nhất + ngày hẹn tiếp theo), khám sức khỏe NV, điểm vệ sinh hàng ngày, nhiệt độ tủ lưu mẫu | BR-100–104, BR-110 | UC-115 bước 4–5; nêu rõ đánh giá quá hạn |
| 8 | Nút "Export" | Button | — | BR-178 | Xuất Excel (nhóm báo cáo thường)/PDF theo chuẩn cơ quan y tế (Compliance Report); xuất đúng theo khoảng ngày đang chọn ở field 2, không xuất toàn bộ dữ liệu (đồng bộ Nhóm D, xem bản Web) |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải dữ liệu báo cáo/biểu đồ.
- Empty: "Không có dữ liệu cho khoảng thời gian đã chọn" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem/xuất báo cáo, không có MSG riêng cho hành động Export).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard.
**Bố cục**: 1 cột, Tab chọn nhóm báo cáo trên cùng, filter (date range, refrigerator ID) ngay dưới, kết quả hiển thị dạng `fl_chart`/danh sách Card cuộn dọc, nút "Export" nổi góc trên phải AppBar. Khối Compliance Summary (UC-115) hiển thị dưới dạng section riêng có thể cuộn tới, không tách trang khác.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Tab chọn nhóm báo cáo trên Page Header, Content Area chia 2 khối: khối trên (báo cáo theo nhóm, dùng `recharts`), khối dưới (Compliance Summary Dashboard dạng Card thống kê), nút "Export" góc trên phải page header cho từng khối.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Food Safety Reports (gộp Compliance Report), nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Tab chọn nhóm báo cáo (Monthly Kitchen Audits/Pest Control Log/Refrigerator
Temperature Logs), filter ngày/refrigerator ID, chart/table kết quả (nhóm Pest Control Log hiển thị
Table/Card nhật ký sự kiện: Ngày, Đơn vị thực hiện, Loại côn trùng + số lượng ước tính, Biện pháp xử lý,
Ngày hẹn tiếp theo), Compliance Summary Dashboard, nút Export (PDF/Excel).
Áp dụng Design System: warning #B45309 cho đánh giá quá hạn/nhiệt độ vượt ngưỡng.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3623–3637 (UC-117), dòng 3601–3617 (UC-115).
- business_rules.md (BR-100, BR-101, BR-102, BR-103, BR-104, BR-110, BR-178; BR-089 Deprecated — Changelog v1.6).
- error_message.md (MSG-004, MSG-005).
- `14_Screen_Authorization_Matrix.md` khối 6 (ghi chú gộp UC-115 vào màn này).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 1 (nhật ký kiểm soát côn trùng).
