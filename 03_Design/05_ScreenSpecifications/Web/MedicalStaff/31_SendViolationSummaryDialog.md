# Screen Spec — Send Violation Summary Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-189 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-214 (báo cáo tổng hợp vi phạm theo kỳ), BR-186 (phân cấp mức độ), BR-189 (danh sách tổng hợp vi phạm), BR-211 (biên bản vi phạm PDF đã ký), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-097 (Error — không có vi phạm trong kỳ), MSG-099 (Success — gửi thành công)

> **Bổ sung 13/07/2026**: hiện thực hóa **BR-214** — mở rộng cơ chế "gửi báo cáo chủ động" của UC-185 sang mảng dữ liệu vi phạm. **Nguyên tắc cốt lõi**: đây là báo cáo **rollup số liệu**, KHÔNG lặp lại nội dung biên bản vi phạm đã ký 3 bên (đã có sẵn theo BR-211, xuất PDF ngay khi lập từng vi phạm). Mỗi vi phạm trong bảng chỉ hiện loại/đối tượng/mức độ/trạng thái + liên kết tới biên bản PDF đã có (nếu đã ký), không hiện lại toàn văn biên bản. Vào từ `29_ATTPReportCenter.md` (UC-187). Xem `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-214 | Mặc định: Daily; cùng cơ chế với UC-185 |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-214 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Tổng số vi phạm trong kỳ theo loại | Card số liệu (đọc) | Đếm theo 3 loại: quy trình/nhân viên/đối tác (BR-189) | BR-214 | |
| 4 | Phân bố theo mức độ | Card/biểu đồ nhỏ (đọc) | LOW/MEDIUM/HIGH (BR-186) | BR-214 | |
| 5 | Danh sách vi phạm trong kỳ | Table rút gọn (đọc) | Cột: Loại, đối tượng liên quan, mức độ, trạng thái, nút "Xem biên bản" (nếu đã có PDF ký, BR-211) | BR-214 | **Không hiện lại** toàn văn biên bản đã ký; "Xem biên bản" mở PDF đã lưu theo BR-211 |
| 6 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-214 | Medical Staff tự nhập nhận định/khuyến nghị tổng quan |
| 7 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có vi phạm nào trong kỳ (MSG-097) | BR-214 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 8 | Nút "Hủy" (Cancel) | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có vi phạm nào trong kỳ đã chọn → chặn gửi, hiện MSG-097, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-097.
- Success: MSG-099 (Toast), đóng dialog, ghi audit log (BR-170), cập nhật "Lần gửi gần nhất" trên `29_ATTPReportCenter.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Card "Báo cáo Tổng hợp Vi phạm"); thành công → đóng dialog, quay lại Report Center.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, card số liệu (tổng số theo loại + phân bố mức độ) xếp lưới 2 cột, Table danh sách vi phạm giữa dialog, Textarea ghi chú bên dưới, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Mobile); thành công → đóng màn, quay lại Report Center.
**Bố cục**: Trang riêng full-screen — chọn kỳ báo cáo trên cùng, card số liệu xếp dọc, danh sách vi phạm dạng `Card` cuộn dọc rút gọn, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Violation Summary Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog modal — chọn kỳ Daily/Weekly, card tổng số vi phạm theo loại (quy trình/nhân viên/đối
tác), card phân bố mức độ (LOW/MEDIUM/HIGH), Table danh sách vi phạm (Loại/Đối tượng/Mức độ/Trạng
thái/nút Xem biên bản — KHÔNG hiện toàn văn biên bản), textarea ghi chú, nút Gửi báo cáo (primary) +
nút Hủy.
Áp dụng Design System: danger #B91C1C (HIGH), warning #B45309 (MEDIUM), secondary #64748B (LOW).
Trạng thái cần thể hiện: loading, empty (MSG-097), success (MSG-099).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-214, mới 13/07/2026), §18 (BR-186, BR-189, BR-211).
- `traceability_matrix.md` (UC-189, mới 13/07/2026).
- `error_message.md` (MSG-097, MSG-099).
- `27_ViolationRegistry.md` (nguồn số liệu rollup), `24_RecordViolationResolution.md` (biên bản đã ký, BR-211 — không lặp lại ở đây).
- `29_ATTPReportCenter.md` (điểm vào từ nút "Soạn & Gửi").
