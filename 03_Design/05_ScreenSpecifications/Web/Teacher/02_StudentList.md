# Screen Spec — Student List

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History (bước 1–2, class registry) | **Status**: Draft
**BR liên quan**: BR-025, BR-030
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Class [Tên lớp] Student List" + filter/search phía trên bảng | BR-025 | |
| 2 | Profile Filter Tabs | Tabs/Filter | "All" / "Allergy Warning" / "Special Diet" / "Normal" | BR-030, BR-065 | |
| 3 | Student Table | `Table`/`DataTable` (sort/filter/pagination) | Cột: Avatar, Họ tên, Badge tình trạng, action "Xem hồ sơ" | BR-030 | Web ưu tiên Table thay Card list (`Component_Guidelines.md` mục 2.4) |

### Trạng thái màn hình
- Loading: `Skeleton` bảng.
- Empty: "Lớp chưa có học sinh nào" [Giả định].
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" (theo `12_Teacher_Screen_Flow_Web.md`); ra Student Detail (click hàng bảng).
**Bố cục**: Topbar + Sidebar + Content: Page Header + thanh filter/search + `Table` phân trang, cột action bên phải theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student List, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + filter tabs + Table (sort/filter/pagination).
Thành phần bắt buộc: Profile Filter Tabs, Student Table (avatar, họ tên, badge trạng thái, action xem hồ sơ).
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6351–6365 (bản Mobile gốc — dùng chung business logic).
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-025, BR-030.
