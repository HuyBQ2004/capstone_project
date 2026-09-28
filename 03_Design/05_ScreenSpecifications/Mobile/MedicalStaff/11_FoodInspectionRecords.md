# Screen Spec — Food Inspection Records

**Actor**: Medical Staff | **UC liên quan**: UC-112 (Report 3 §3.2, dòng 3553–3571) | **Status**: Draft
**BR liên quan**: BR-091 đến BR-099 (quy trình kiểm tra ba bước, nhiệt độ, tái kiểm tra)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-016 (Error), MSG-017 (Error)

> **Lịch sử sửa filter**: (1) **09/07/2026** — đổi filter chính "Bước kiểm tra" → "Kết quả" (`review_medicalstaff_compliance_kitcheninspection.md` Mục 5), vì Medical Staff quan tâm phiên Đạt/Không đạt hơn đang ở bước mấy; (2) **12/07/2026** (rà soát ảnh chụp màn hình thực tế, Hình 5) — bỏ hẳn filter "Bước" (kể cả bản phụ đã thêm ở code) và filter "Món", chỉ giữ Date range + Kết quả. Xem `review_screen_medical_staff.md` Mục 15.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo khoảng ngày | Date range picker | — | — | UC-112 bước 3 |
| 2 | Filter theo Kết quả | Select | Đạt / Không đạt / Đang thực hiện — khớp field `overall` của `InspectionSession` | BR-094, BR-098 | **Sửa 09/07/2026** — thay cho filter "Bước kiểm tra" cũ (đã bỏ hẳn) |
| 3 | Danh sách bản ghi kiểm tra | List/Table | — | — | Cột: ngày, trạng thái (Passed/Failed), nhân viên phụ trách |
| 4 | Status Badge (mỗi bản ghi) | Badge màu | Success (Passed)/Danger (Failed) | BR-094, BR-098 | |
| 5 | Chọn 1 bản ghi → xem chi tiết | Interaction | — | — | Hiện ảnh bằng chứng, timestamp, GPS, ghi chú nhân viên (UC-112 bước 5); modal hiển thị đầy đủ Mẫu 1/2/3 theo bước |

**Đã bỏ (12/07/2026)**: filter "Bước kiểm tra" (kể cả filter phụ + "Trạng thái bước" con) và filter "Món/thực đơn".

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải danh sách.
- Empty: AF1 "No Records Found" — hiển thị "Không tìm thấy bản ghi phù hợp" theo đúng tiêu chí filter.
- Error: MSG-004, MSG-005, MSG-016, MSG-017.
- Success: không áp dụng (màn hình xem).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "View food inspection results in 3 steps" trên Dashboard; chọn item → mở chi tiết dạng bottom sheet.
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` (date range, Kết quả) phía trên, danh sách `Card` cuộn dọc, mỗi Card có Status Badge góc phải.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "View food inspection results in 3 steps" trên Dashboard (Web); chọn dòng → mở `Sheet` chi tiết bên phải.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — thanh filter (date range, Kết quả) phía trên `Table`, `Table` hiển thị danh sách kèm phân trang.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Inspection Records, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: filter (date range, Kết quả: Đạt/Không đạt/Đang thực hiện), danh sách bản ghi
kiểm tra với Status Badge, panel chi tiết (ảnh bằng chứng, timestamp, GPS, ghi chú).
Áp dụng Design System: success #15803D cho Passed, danger #B91C1C cho Failed.
Trạng thái cần thể hiện: loading, empty (không có bản ghi phù hợp), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên (KHÔNG có filter Bước, KHÔNG có filter Món).
```

## Nguồn dữ liệu
- Report 3.md, dòng 3553–3571 (UC-112).
- business_rules.md (BR-091–BR-099).
- error_message.md (MSG-004, MSG-005, MSG-016, MSG-017).
- `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 5 (đổi filter Kết quả), Mục 6 (modal chi tiết đầy đủ).
