# Screen Spec — Send Compliance Report Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-185 (mới, bổ sung 05/07/2026 — không có trong Report 3.md gốc, chính thức hóa US-09) | **Status**: Draft
**BR liên quan**: BR-209 (gửi báo cáo tuân thủ ATTP định kỳ), BR-104 (điểm vệ sinh đầu bếp), BR-102 (chứng nhận kiểm soát côn trùng), BR-103 (khám sức khỏe nhân viên bếp), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-092 (Error — không có dữ liệu trong kỳ), MSG-091 (Success — gửi thành công)

> **Bổ sung 05/07/2026**: màn hình này hiện thực hóa **US-09** (`MedicalStaff_UserStory.md`) sau khi đội dự án phát hiện Compliance Dashboard (UC-027) — cơ chế hiện có duy nhất để School Administration nắm tình hình ATTP — vận hành theo mô hình **pull** (School Administration tự vào xem) và **chưa có route triển khai thực tế** phía frontend School Administration (xem `review_screen_medical_staff.md` mục 3.9). Màn hình này bổ sung kênh **push** song song, không thay thế UC-027. Không có trong `Report 3.md` gốc — mọi field đều tái sử dụng dữ liệu/cơ chế đã có (KitchenInspection từ UC-014, export BR-178, thông báo BR-158/159), không mở rộng phạm vi model dữ liệu.
>
> **Sửa 12/07/2026**: field 5 đổi từ badge "Còn hiệu lực/Hết hạn" sang tóm tắt bản ghi nhật ký kiểm soát côn trùng gần nhất — đồng bộ quyết định đổi model "nhật ký sự kiện" cho BR-102 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 1, đã áp code `compliance/page.tsx`). Xem `review_screen_medical_staff.md` Mục 14.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-209 | Mặc định: Daily |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-209 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Điểm vệ sinh trung bình trong kỳ | Card số liệu (đọc) | Tính từ các `KitchenInspection` trong kỳ | BR-104 | |
| 4 | Danh sách phiên kiểm tra Failed trong kỳ (nếu có) | List rút gọn (đọc) | Hiện mã phiên, ngày, lý do | BR-100, BR-104 | Trống nếu không có phiên Failed |
| 5 | Kiểm soát côn trùng — bản ghi gần nhất trong kỳ | Card tóm tắt (đọc) | Ngày xử lý gần nhất, Loại côn trùng phát hiện (nếu có), Ngày hẹn kiểm tra tiếp theo | BR-102 | **Sửa 12/07/2026** — thay cho badge "Còn hiệu lực/Hết hạn"; nếu không có bản ghi nào trong kỳ, hiện "Không có hoạt động kiểm soát côn trùng trong kỳ" |
| 6 | Tình trạng khám sức khỏe nhân viên bếp | Text/Badge (đọc) | Đạt / Có nhân viên quá hạn | BR-103 | |
| 7 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-209 | Medical Staff tự nhập nhận định/khuyến nghị |
| 8 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có phiên kiểm tra nào trong kỳ (MSG-092) | BR-209 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 9 | Nút "Hủy" (Cancel) | Button | — | — | Đóng màn, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có phiên kiểm tra nào trong kỳ đã chọn → chặn gửi, hiện MSG-092, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-092.
- Success: MSG-091 (Toast), đóng màn, ghi audit log (BR-170), lưu vào lịch sử báo cáo (BR-177, xem lại tại `32_SentReportsHistory.md`).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (Mobile) **hoặc** từ Card "Báo cáo Giám sát tuân thủ" trên `29_ATTPReportCenter.md` (Mobile, mới 13/07/2026 — UC-187); thành công → đóng màn, quay lại màn nguồn tương ứng.
**Bố cục**: Trang riêng full-screen (bottom sheet hoặc trang con) — chọn kỳ báo cáo trên cùng, các card số liệu/trạng thái xếp dọc, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (Web) **hoặc** từ Card "Báo cáo Giám sát tuân thủ" trên `29_ATTPReportCenter.md` (Web, mới 13/07/2026 — UC-187); thành công → đóng dialog, quay lại màn nguồn tương ứng.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, các card số liệu/trạng thái xếp lưới 2 cột giữa dialog, Textarea ghi chú bên dưới, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Compliance Report Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: chọn kỳ Daily/Weekly, card điểm vệ sinh trung bình, danh sách phiên Failed,
card tóm tắt kiểm soát côn trùng (bản ghi gần nhất: ngày xử lý, loại côn trùng nếu có, ngày hẹn tiếp theo),
trạng thái khám sức khỏe nhân viên, textarea ghi chú, nút Gửi báo cáo (primary) + nút Hủy.
Áp dụng Design System: primary #0E7490, disabled state khi không có dữ liệu trong kỳ.
Trạng thái cần thể hiện: loading, empty (MSG-092), success (MSG-091).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §12 (BR-209, ngay sau BR-208).
- `MedicalStaff_UserStory.md` (US-09, [DECIDED — duyệt 05/07/2026]).
- `traceability_matrix.md` (UC-185, hàng BR-209).
- `error_message.md` §1 (MSG-092), §2 (MSG-091).
- `23_ComplianceDashboard.md` (điểm vào từ nút "Gửi báo cáo tuân thủ").
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 1 (nhật ký kiểm soát côn trùng).
- `29_ATTPReportCenter.md` (UC-187, điểm vào bổ sung 13/07/2026), `32_SentReportsHistory.md` (UC-190, xem lại sau khi gửi).
