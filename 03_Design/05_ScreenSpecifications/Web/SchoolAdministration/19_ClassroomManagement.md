# Screen Spec — Classroom Management

**Actor**: School Administration | **UC liên quan**: UC-037 (Manage Classrooms), UC-166 (View List Classrooms) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-024 (cấp lớp 1-12), BR-025 (sức chứa tối đa 50)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên lớp) | Input (search) | — | — | |
| 2 | Filter theo Grade Level | Select | 1-12 | BR-024 | |
| 3 | Bảng danh sách lớp | Table | Cột: Class Name, Grade Level, Student Capacity, Homeroom Teacher | BR-024, BR-025 | |
| 4 | Nút "Create Classroom" | Button (primary, góc trên phải) | — | — | Mở `Create Classroom` |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Classroom Detail` |
| 6 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có lớp học nào — tạo lớp đầu tiên" (kèm nút Create Classroom nổi bật hơn).
- Error: MSG-004/MSG-005; nếu trường chưa có năm học Active → MSG-081 khi cố tạo lớp (BR-192, xem `21_CreateClassroom.md`).
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Lớp/Học sinh". Điều hướng ra: `Create Classroom`, `Classroom Detail`.
**Bố cục**: Dashboard Shell — Page Header (nút Create Classroom góc phải), filter bar (search + grade), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Classroom Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Classroom), filter bar (search + grade level), bảng dữ liệu (Class Name/Grade/Capacity/Homeroom Teacher).
Thành phần bắt buộc: search, filter grade, bảng, action xem chi tiết, nút Export.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1667–1731 (Table 3-23 Manage Classrooms, View List Classrooms Use Cases Specification)
- traceability_matrix.md §1.2 (UC-037, UC-166 ↔ BR-024/BR-025)
