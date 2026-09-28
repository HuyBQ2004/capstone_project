# Screen Spec — Announcement Detail

**Actor**: School Administration | **UC liên quan**: UC-105 (Report 3 §3.2 "View Announcement Detail") | **Status**: Draft
**BR liên quan**: BR-144 (lịch sử sửa đổi — chỉ School Administration thấy đầy đủ)
**MSG liên quan**: — (Report 3 gán "Exceptions: None")

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Title, Content | Text hiển thị | — | — | |
| 2 | Target Group | Text/Badge hiển thị | — | — | |
| 3 | Publication Status | Badge (info=Draft, success=Published) | — | — | |
| 4 | Author, Publication Dates | Text hiển thị | — | — | |
| 5 | Lịch sử sửa đổi đầy đủ | Table (đọc-only) | **Chỉ hiện với actor School Administration** (khác User thường không thấy) | BR-144 | |
| 6 | Nút "Edit" | Button | — | — | Mở `Update Announcement` |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: Không có exception cụ thể — áp dụng MSG-004/MSG-005 mặc định.
- Success: Nội dung + lịch sử sửa đổi hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `School Announcement Management` (click 1 dòng). Điều hướng ra: `Update Announcement`.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề + nút Edit), Content: nội dung thông báo (`col-span-8`), lịch sử sửa đổi (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Announcement Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Edit), 2 cột: nội dung thông báo, lịch sử sửa đổi.
Thành phần bắt buộc: Title/Content, Target Group, badge trạng thái, Author/Publication Dates, bảng lịch sử sửa đổi, nút Edit.
Áp dụng Design System: badge info/success theo trạng thái.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4263–4283 (Table 3-125 View Announcement Detail Use Cases Specification)
- traceability_matrix.md §1.10 (UC-105)
