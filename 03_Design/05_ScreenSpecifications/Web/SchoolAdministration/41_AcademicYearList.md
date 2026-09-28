# Screen Spec — Academic Year List

**Actor**: School Administration | **UC liên quan**: UC-176 (Report 3 §3.2 "View Academic Year List") | **Status**: Draft
**BR liên quan**: BR-195 (dữ liệu năm học Archived luôn truy vấn được)
**MSG liên quan**: MSG-004, MSG-005

> Màn hình mới, chính thức hoá 2026-07-01 (`AcademicYear_Management_Proposal.md`). Cũng dùng chung với System Admin (xem cột "System Admin" trong Primary Actor của UC-176) nhưng phạm vi School Admin giới hạn theo trường mình.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Status (Draft/Active/Closed/Archived) | Select | — | BR-191, BR-195 | |
| 2 | Bảng danh sách năm học | Table | Cột: Year Label, Start/End Date, Status, Số lớp | BR-195 | Archived vẫn hiển thị, read-only |
| 3 | Badge trạng thái | Badge (info=Draft, success=Active, secondary=Closed, neutral=Archived) | — | BR-191 | |
| 4 | Nút "Create Academic Year" | Button (primary, góc trên phải) | — | — | Mở `Create Academic Year` |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Academic Year Detail` |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có năm học nào — tạo năm học đầu tiên".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, bao gồm cả năm học Archived (đọc-only, BR-195).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Lớp/Học sinh" (cạnh Classroom Management). Điều hướng ra: `Create Academic Year`, `Academic Year Detail`.
**Bố cục**: Dashboard Shell — Page Header (nút Create Academic Year), filter bar (status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Academic Year List, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Academic Year), filter bar (status), bảng dữ liệu (Year Label/Start-End Date/Status/Số lớp).
Thành phần bắt buộc: filter status, bảng, badge trạng thái (Draft/Active/Closed/Archived), action xem chi tiết.
Áp dụng Design System: badge info/success/secondary/neutral theo trạng thái năm học.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1887–1907 (Table 3-157 View Academic Year List Use Cases Specification)
- traceability_matrix.md §1.2 (UC-176 ↔ BR-195)
- business_rules.md §19 (BR-191–195)
