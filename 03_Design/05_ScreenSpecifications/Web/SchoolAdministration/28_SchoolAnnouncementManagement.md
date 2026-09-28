# Screen Spec — School Announcement Management

**Actor**: School Administration | **UC liên quan**: UC-125 (Report 3 §3.2 "View List Announcement") | **Status**: Draft
**BR liên quan**: — (Report 3 gán "Business Rules: None")
**MSG liên quan**: — (Report 3 gán "Exceptions: None")

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (từ khoá) | Input (search) | — | — | |
| 2 | Filter theo Status (Draft/Published) | Select | — | — | |
| 3 | Bảng danh sách thông báo | Table | Cột: Title, Target Group, Publication Status, Publication Date | — | Sắp xếp theo ngày tạo |
| 4 | Badge trạng thái | Badge (info=Draft, success=Published) | — | — | |
| 5 | Nút "Create Announcement" | Button (primary, góc trên phải) | — | — | Mở `Create Announcement` |
| 6 | Action: xem chi tiết | Row click | — | — | Mở `Announcement Detail` |
| 7 | Nút "Export CSV/PDF" | Button | — | — | Chỉ School Administration có quyền export theo Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có thông báo nào — tạo thông báo đầu tiên".
- Error: Không có exception cụ thể trong Normal Flow gốc (Report 3 ghi "None") — áp dụng MSG-004/MSG-005 mặc định cho lỗi phiên/quyền.
- Success: Bảng hiển thị đầy đủ, export tải file (bước 7 Normal Flow).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Thông báo". Điều hướng ra: `Create Announcement`, `Announcement Detail`.
**Bố cục**: Dashboard Shell — Page Header (nút Create Announcement), filter bar (search + status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Announcement Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Announcement), filter bar (search + status), bảng dữ liệu.
Thành phần bắt buộc: search, filter status, bảng (Title/Target Group/Status/Publication Date), badge trạng thái, action xem chi tiết, nút Export.
Áp dụng Design System: badge info/success theo trạng thái Draft/Published.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4285–4305 (Table 3-126 View List Announcement Use Cases Specification)
- traceability_matrix.md §1.10 (UC-125)
