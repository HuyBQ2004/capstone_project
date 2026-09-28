# Screen Spec — Food Sample Records

**Actor**: Medical Staff (dùng chung với School Administration, Service Caterers theo UC-118 — file này viết theo góc nhìn Medical Staff) | **UC liên quan**: UC-118 (Report 3 §3.2, dòng 3779–3793) | **Status**: Draft
**BR liên quan**: BR-107 (chụp ảnh mẫu bắt buộc), BR-108 (GPS/timestamp tự động), BR-110 (nhiệt độ bảo quản 2–8°C), BR-111 (thời gian lưu mẫu tối thiểu), BR-114 (liên kết mẫu với thực đơn/kiểm tra)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo khoảng ngày/bữa/món/trạng thái | Filter bar | — | — | UC-118 bước 3: Retained/Retrieved/Disposed |
| 2 | Sổ lưu mẫu số hóa (Mẫu số 5) | Table/List | Đủ 12 cột theo Mẫu số 5 | BR-107, BR-108, BR-111, BR-114 | Tên mẫu, thời gian thu, chi tiết bảo quản, chữ ký hủy/nhân chứng |
| 3 | Status Badge (mỗi bản ghi) | Badge màu | Info (Retained)/Warning (Retrieved)/Secondary (Disposed) | — | |
| 4 | Nút "Export" | Button | — | BR-178 | Xuất Excel/PDF cho thanh tra |
| 5 | Chọn 1 bản ghi → xem chi tiết/thao tác | Interaction | — | — | Điều hướng `14_RetrieveFoodSampleRecordsDialog.md` khi cần thu hồi mẫu để điều tra |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` khi tải sổ lưu mẫu.
- Empty: "Chưa có mẫu lưu nào phù hợp bộ lọc" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem, trừ hành động Export không có MSG riêng).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Sample Register" trên Dashboard; chọn item → mở chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` phía trên, danh sách `Card` (không dùng bảng dày đặc theo `Component_Guidelines.md` — mỗi bản ghi 1 Card tóm tắt 12 cột quan trọng nhất), nút "Export" nổi góc trên phải AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu "Food Sample Records — Mẫu số 5" trên Sidebar (Web); chọn dòng → chi tiết hoặc `14_RetrieveFoodSampleRecordsDialog.md`.
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — thanh filter phía trên `Table` đầy đủ 12 cột theo Mẫu số 5, nút "Export" góc trên phải page header.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Sample Records (Mẫu số 5), nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: filter (ngày/bữa/món/trạng thái), sổ lưu mẫu 12 cột, Status Badge, nút Export.
Áp dụng Design System: info #1D4ED8 (Retained), warning #B45309 (Retrieved), secondary #64748B (Disposed).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3779–3793 (UC-118).
- business_rules.md (BR-107, BR-108, BR-110, BR-111, BR-114, BR-178).
- error_message.md (MSG-004, MSG-005).
