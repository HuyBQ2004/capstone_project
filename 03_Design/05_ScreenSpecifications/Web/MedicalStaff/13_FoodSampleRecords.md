# Screen Spec — Food Sample Records

**Actor**: Medical Staff (dùng chung với School Administration, Service Caterers theo UC-118 — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-118 (Report 3 §3.2, dòng 3779–3793) | **Status**: Draft
**BR liên quan**: BR-107 (chụp ảnh mẫu bắt buộc), BR-108 (GPS/timestamp tự động), BR-110 (nhiệt độ bảo quản 2–8°C), BR-111 (thời gian lưu mẫu tối thiểu), BR-114 (liên kết mẫu với thực đơn/kiểm tra)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo khoảng ngày/bữa/món/trạng thái | Filter bar | — | — | UC-118 bước 3 |
| 2 | Sổ lưu mẫu số hóa (Mẫu số 5) | Table | Đủ 12 cột theo Mẫu số 5 | BR-107, BR-108, BR-111, BR-114 | |
| 3 | Status Badge (mỗi dòng) | Badge màu | Info (Retained)/Warning (Retrieved)/Secondary (Disposed) | — | |
| 4 | Nút "Export" | Button | — | BR-178 | Xuất Excel/PDF. **Cập nhật 11/07/2026**: xuất đúng theo khoảng ngày đang chọn ở field 1 (Filter theo khoảng ngày) — không xuất toàn bộ dữ liệu bất kể filter đang áp dụng |
| 5 | Chọn 1 dòng → xem chi tiết/thao tác | Interaction | — | — | Điều hướng `14_RetrieveFoodSampleRecordsDialog.md` |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải sổ lưu mẫu.
- Empty: "Chưa có mẫu lưu nào phù hợp bộ lọc" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "Food Sample Records — Mẫu số 5" trên Sidebar (Web); chọn dòng → chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: thanh filter phía trên `Table` đầy đủ 12 cột theo Mẫu số 5, nút "Export" góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Sample Register" trên Dashboard (Mobile); chọn item → chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: 1 cột, filter `Chip`/`DropdownButton`, danh sách `Card` tóm tắt, nút "Export" nổi góc trên phải AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Sample Records (Mẫu số 5), nền tảng Web, actor Medical Staff.
Bố cục: thanh filter (ngày/bữa/món/trạng thái), Table 12 cột, Status Badge, nút Export.
Áp dụng Design System: info #1D4ED8 (Retained), warning #B45309 (Retrieved), secondary #64748B (Disposed).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3779–3793 (UC-118).
- business_rules.md (BR-107, BR-108, BR-110, BR-111, BR-114, BR-178).
- error_message.md (MSG-004, MSG-005).
