# Screen Spec — School List

**Actor**: System Admin | **UC liên quan**: UC-140 (Report 3 §3.2 "View School List") | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường theo vai trò), BR-025 (sức chứa lớp — tham chiếu gián tiếp qua số liệu tổng hợp trường)
**MSG liên quan**: MSG-004, MSG-005

> Ghi chú: Report 3 gán "Business Rules: BR-022, BR-025" cho UC-140 — cả hai đều không mô tả trực tiếp nghiệp vụ "xem danh sách trường" (BR-022 là quyền xem chính sách, BR-025 là sức chứa lớp). Có khả năng đây cũng là boilerplate không khớp hoàn toàn — giữ lại trích dẫn theo đúng nguồn gốc UC nhưng đánh dấu **[Giả định — cần team xác nhận]** về mức độ liên quan thực tế.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên trường) | Input (search) | — | — | |
| 2 | Filter theo Status (Active/Inactive/Pending) | Select | — | — | |
| 3 | Bảng danh sách trường | Table | Cột: Name, Code, Tax ID, Status | — | |
| 4 | Badge trạng thái | Badge (success=Active, danger=Inactive, warning=Pending) | — | — | |
| 5 | Action: xem chi tiết | Row click / IconButton | — | — | Mở `School Detail` |
| 6 | Action: toggle trạng thái | IconButton | — | BR-021 | Mở `Active/Deactivate School Dialog` |
| 7 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có trường nào khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Trường học". Điều hướng ra: `School Detail`, `Active/Deactivate School Dialog`.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề, không có nút "Create School" vì trường được tạo qua luồng đăng ký UC-070 Register School, không phải do System Admin tạo trực tiếp theo Normal Flow hiện có), search + filter Status, bảng Table.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School List, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header, filter bar (search + status), bảng dữ liệu (Name/Code/Tax ID/Status).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, action xem chi tiết + toggle trạng thái, nút Export.
Áp dụng Design System: badge success/warning/danger theo trạng thái trường.
Trạng thái cần thể hiện: loading, empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1821–1841 (Table 3-29 View School List Use Cases Specification)
- traceability_matrix.md §1.2 (UC-140)
