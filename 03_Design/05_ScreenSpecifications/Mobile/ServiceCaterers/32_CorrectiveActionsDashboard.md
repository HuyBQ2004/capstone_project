# Screen Spec — Corrective Actions Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-038 - Manage Corrective Actions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-152, BR-153, BR-155
**MSG liên quan**: MSG-023 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách CAPA được gán cho Service Caterers | Card list | Mỗi card: Sự cố liên quan, Hạn chót, Trạng thái | BR-152 | UC-038 Normal Flow bước 2 |
| 2 | Trạng thái CAPA | Chip | Pending / In Progress / Pending Approval / Late Submission | BR-152, BR-153 | UC-038 Exceptions |
| 3 | Chi tiết hành động yêu cầu, nguyên nhân gốc rễ, hạn chót | Read-only detail (trang riêng) | — | BR-152 | UC-038 Normal Flow bước 4 |
| 4 | Nút "Cập nhật Tiến độ" | Button | Điều hướng Submit Corrective Action Results Form | — | UC-038 Normal Flow bước 5 |
| 5 | Cảnh báo nộp trễ hạn (quá 12 giờ) | Chip màu `warning` | Tự động gắn cờ "Late Submission" | BR-152 | MSG-023 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách CAPA.
- Empty: Không có CAPA nào đang chờ xử lý.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Sự cố & Hành động Khắc phục" trong Grid Menu Items. Điều hướng tiếp: Submit Corrective Action Results Form, View Incident Information Detail.
**Bố cục**: AppBar + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Corrective Actions Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list CAPA (Sự cố liên quan, Hạn chót, Trạng thái chip, nút Cập nhật Tiến độ).
Thành phần bắt buộc: chip "Late Submission" màu warning.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4419–4437 (UC-038 - Manage Corrective Actions)
- business_rules.md BR-152, BR-153, BR-155
- error_message.md MSG-023
