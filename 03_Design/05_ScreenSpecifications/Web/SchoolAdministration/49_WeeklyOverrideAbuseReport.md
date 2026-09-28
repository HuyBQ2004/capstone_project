# Screen Spec — Weekly Override Abuse Report

**Actor**: School Administration | **UC liên quan**: UC-183 (Report 3 §3.2 "View Weekly Override Abuse Report") | **Status**: Draft
**BR liên quan**: BR-190 (báo cáo lạm dụng manual override hàng tuần)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo tuần | DatePicker (chọn tuần) | — | — | |
| 2 | Bảng báo cáo (Staff Name, Override Count, Context, Severity) | Table | Tự động biên soạn hàng tuần từ `StaffViolationRecord` (violationType = OVERRIDE_ABUSE) | BR-190 | |
| 3 | Nút "Export" | Button | — | — | AF1 UC-183 |
| 4 | Action: drill down | Row click | — | — | Mở `Violation Registry` (lọc theo staff tương ứng) |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có trường hợp lạm dụng override nào trong tuần này" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, Export tải file (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Violation Registry` hoặc Sidebar nhóm "Kiểm tra & Sự cố". Điều hướng ra: `Violation Registry` (drill down).
**Bố cục**: Dashboard Shell — filter bar (chọn tuần), bảng `Table`, nút Export góc phải Page Header.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Weekly Override Abuse Report, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Export), filter chọn tuần, bảng dữ liệu (Staff Name/Override Count/Context/Severity).
Thành phần bắt buộc: filter tuần, bảng, nút Export, action drill down sang Violation Registry.
Áp dụng Design System: badge severity nếu cần nhấn mạnh mức độ.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4727–4747 (Table 3-164 View Weekly Override Abuse Report Use Cases Specification)
- traceability_matrix.md §1.11 (UC-183 ↔ BR-190)
