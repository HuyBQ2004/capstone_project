# Screen Spec — Classroom Detail

**Actor**: School Administration | **UC liên quan**: UC-165 (View Classroom Detail) + action UC-008 (Assign Homeroom Teacher), UC-090 (Update Classroom) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-020 (1 GVCN/lớp), BR-024 (cấp lớp), BR-025 (sức chứa), BR-026 (ghi log phân công GV)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa), MSG-011 (GV đã gán lớp khác), MSG-034 (phân công GV thành công)

> `Assign Homeroom Teacher` (UC-008) và `Update Classroom` (UC-090) **không phải màn hình riêng** — đây là 2 action (dialog/inline edit) thực hiện ngay tại màn hình này, theo đúng ghi chú nghiệp vụ ở `02_School_Administration_Screen_Flow.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Class Name, Grade Level, Max Capacity | Text hiển thị | — | BR-024, BR-025 | |
| 2 | Homeroom Teacher hiện tại | Text + nút "Assign/Reassign" | Chỉ 1 GVCN/lớp | BR-020 | Mở dialog chọn GV từ danh sách staff active role Teacher |
| 3 | Dialog Assign Homeroom Teacher | Select (danh sách teacher active) | Chặn nếu GV đã được gán lớp khác (MSG-011), yêu cầu xác nhận reassign (AF1) | BR-020, BR-026 | |
| 4 | Danh sách học sinh trong lớp | Table | — | BR-025 | |
| 5 | Nút "Edit" (Update Classroom) | Button | Mở form inline sửa Name/Grade/Capacity | BR-024, BR-025 | Capacity mới không được nhỏ hơn số học sinh hiện có |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Danh sách học sinh trống — "Chưa có học sinh trong lớp".
- Error: MSG-011 nếu GV đã gán lớp khác (không có AF1 confirm thì chặn); MSG-010 nếu sửa capacity nhỏ hơn số học sinh hiện có; MSG-004/MSG-005.
- Success: MSG-034 khi assign GV thành công; toast xác nhận khi Update Classroom thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Classroom Management` (click 1 dòng). Không có màn hình con — action Assign Teacher/Update Classroom mở Dialog/inline edit ngay tại trang.
**Bố cục**: Dashboard Shell — Page Header (tên lớp + nút Edit), Content: thông tin lớp + Homeroom Teacher (`col-span-4`), danh sách học sinh (`col-span-8`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Classroom Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Edit), thông tin lớp + Homeroom Teacher (cột trái), danh sách học sinh (cột phải), Dialog Assign Teacher khi bấm nút Assign/Reassign.
Thành phần bắt buộc: Class Name/Grade/Capacity, Homeroom Teacher + nút Assign/Reassign, dialog chọn GV, bảng học sinh, nút Edit (Update Classroom).
Áp dụng Design System: cảnh báo vượt sức chứa dùng token danger.
Trạng thái cần thể hiện: error (GV đã gán lớp khác, vượt sức chứa), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1623–1643 (UC-008), 1689–1709 (UC-165), 1755–1775 (UC-090)
- traceability_matrix.md §1.2 (UC-008/UC-090/UC-165)
