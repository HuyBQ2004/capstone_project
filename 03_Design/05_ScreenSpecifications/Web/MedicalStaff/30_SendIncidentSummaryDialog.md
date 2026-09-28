# Screen Spec — Send Incident Summary Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-188 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-213 (báo cáo tổng hợp sự cố theo kỳ), BR-147 (phân loại mức độ), BR-181 (ẩn danh dữ liệu cá nhân), BR-178 (xuất báo cáo PDF/Excel), BR-158/BR-159 (thông báo trong hệ thống), BR-170 (audit log)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-096 (Error — không có sự cố trong kỳ), MSG-098 (Success — gửi thành công)

> **Bổ sung 13/07/2026**: hiện thực hóa **BR-213** — mở rộng cơ chế "gửi báo cáo chủ động" của UC-185 sang mảng dữ liệu sự cố ATTP. **Nguyên tắc cốt lõi**: đây là báo cáo **rollup số liệu**, KHÔNG lặp lại nội dung chi tiết đã có trong báo cáo chính thức từng sự cố (clinical notes, kết quả xét nghiệm mẫu, root cause, corrective action — đã tự động gửi School Administration ngay khi đóng sự cố, theo BR-154/UC-033 `20_GenerateIncidentReportDialog.md`). Mỗi mã sự cố trong bảng chỉ hiện trạng thái/mức độ + liên kết điều hướng, không hiện lại nội dung điều tra. Vào từ `29_ATTPReportCenter.md` (UC-187). Xem `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn kỳ báo cáo (Daily / Weekly) | Radio/Segmented control | Bắt buộc chọn 1 | BR-213 | Mặc định: Daily; cùng cơ chế với UC-185 |
| 2 | Khoảng thời gian áp dụng (auto-tính) | Text (đọc) | — | BR-213 | Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật) |
| 3 | Tổng số sự cố trong kỳ theo trạng thái | Card số liệu (đọc) | Đếm theo 3 nhóm: Chờ tiếp nhận / Đang xử lý / Đã đóng (khớp `25_IncidentList.md`) | BR-213 | |
| 4 | Phân bố theo mức độ nghiêm trọng | Card/biểu đồ nhỏ (đọc) | Chỉ tính sự cố đã qua Step 1 (Assign Severity) | BR-147 | Sự cố chưa đánh giá mức độ không tính vào biểu đồ này |
| 5 | Danh sách mã sự cố trong kỳ | Table rút gọn (đọc) | Cột: Mã sự cố, Trạng thái, Mức độ, nút "Xem chi tiết" | BR-213 | **Không hiện lại** clinical notes/root cause/corrective action; "Xem chi tiết" điều hướng `26_IncidentDetail.md` (mở tab/route mới, không rời dialog) |
| 6 | Ghi chú bổ sung | Textarea | Tùy chọn | BR-213 | Medical Staff tự nhập nhận định/khuyến nghị tổng quan |
| 7 | Nhãn "Confidential" | Badge tĩnh | Luôn hiện | BR-181 | Nhắc báo cáo chứa dữ liệu sự cố y tế, không phát tán ngoài phạm vi School Administration |
| 8 | Nút "Gửi báo cáo" (Send Report) | Button (primary) | Disabled nếu không có sự cố nào trong kỳ (MSG-096) | BR-213 | Xuất PDF/Excel (BR-178) + gửi thông báo (BR-158/159) |
| 9 | Nút "Hủy" (Cancel) | Button | — | — | Đóng dialog, không lưu |

### Trạng thái màn hình
- Loading: spinner khi hệ thống tổng hợp dữ liệu kỳ đã chọn và khi đang gửi.
- Empty: không có sự cố nào trong kỳ đã chọn → chặn gửi, hiện MSG-096, nút "Gửi báo cáo" disabled.
- Error: MSG-004, MSG-005 (lỗi hệ thống chung), MSG-096.
- Success: MSG-098 (Toast), đóng dialog, ghi audit log (BR-170), cập nhật "Lần gửi gần nhất" trên `29_ATTPReportCenter.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Card "Báo cáo Tổng hợp Sự cố"); thành công → đóng dialog, quay lại Report Center.
**Bố cục**: `Dialog` modal — chọn kỳ báo cáo trên cùng, card số liệu (tổng số theo trạng thái + phân bố mức độ) xếp lưới 2 cột, Table danh sách mã sự cố giữa dialog, Textarea ghi chú bên dưới, nhãn "Confidential" góc trên phải, 2 nút "Gửi báo cáo"/"Hủy" cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Soạn & Gửi" trên `29_ATTPReportCenter.md` (Mobile); thành công → đóng màn, quay lại Report Center.
**Bố cục**: Trang riêng full-screen — chọn kỳ báo cáo trên cùng, card số liệu xếp dọc, danh sách mã sự cố dạng `Card` cuộn dọc rút gọn, Textarea ghi chú, nút "Gửi báo cáo" cố định cuối màn, nút "Hủy" trên AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Send Incident Summary Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog modal — chọn kỳ Daily/Weekly, card tổng số sự cố theo trạng thái (Chờ tiếp nhận/Đang xử
lý/Đã đóng), card phân bố mức độ nghiêm trọng, Table danh sách mã sự cố (Mã/Trạng thái/Mức độ/nút Xem
chi tiết — KHÔNG hiện nội dung điều tra chi tiết), textarea ghi chú, nhãn "Confidential", nút Gửi báo
cáo (primary) + nút Hủy.
Áp dụng Design System: primary #0E7490, danger #B91C1C/warning #B45309 cho mức độ HIGH/MEDIUM.
Trạng thái cần thể hiện: loading, empty (MSG-096), success (MSG-098).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-213, mới 13/07/2026), §11 (BR-147, BR-154).
- `traceability_matrix.md` (UC-188, mới 13/07/2026).
- `error_message.md` (MSG-096, MSG-098).
- `25_IncidentList.md` (nguồn số liệu rollup), `26_IncidentDetail.md` (điều hướng chi tiết), `20_GenerateIncidentReportDialog.md` (UC-033, báo cáo chi tiết từng sự cố — không lặp lại ở đây).
- `29_ATTPReportCenter.md` (điểm vào từ nút "Soạn & Gửi").
