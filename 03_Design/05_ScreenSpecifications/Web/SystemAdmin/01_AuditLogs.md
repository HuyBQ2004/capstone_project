# Screen Spec — Audit Logs

**Actor**: System Admin | **UC liên quan**: UC-106 (Report 3 §3.2 "View Audit Logs") | **Status**: Draft
**BR liên quan**: BR-162 (nhật ký bất biến), BR-170 (ghi log mọi hành động quan trọng), BR-171 (chính sách lưu trữ), BR-172 (bảo vệ toàn vẹn), BR-174 (truy vết lịch sử vận hành)
**MSG liên quan**: MSG-004 (Session expired), MSG-005 (Access denied)

> Nguồn UC: Report 3.md dòng 4883–4903 (Table 3-149). Ghi chú: trường "Business Rules" gốc trong bảng UC (BR-158–164) là boilerplate lặp lại giống hệt ở nhiều UC khác (UC-057/111/136/149/150/040/058/088) — không khớp nội dung nghiệp vụ của UC này; đã dùng bảng BR đối chiếu chính xác hơn ở `traceability_matrix.md` §1.12 (BR-162, BR-170, BR-171, BR-172, BR-174) làm nguồn chuẩn theo nguyên tắc đã chốt ở `00_UI_Design_Documentation_Plan.md` mục 2.4.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh filter: Date range | DatePicker (range) | Ngày bắt đầu ≤ ngày kết thúc | BR-171 | Mặc định 7 ngày gần nhất |
| 2 | Thanh filter: User ID / Tên | Input (search) | Tìm gần đúng | — | |
| 3 | Thanh filter: Action category | Select | Danh sách hành động cố định (Create/Update/Delete/Approve/Reject/Login...) | BR-170 | |
| 4 | Thanh filter: IP Address | Input | Định dạng IPv4/IPv6 | — | |
| 5 | Bảng Audit Log | Table (đọc-only, không sort/xoá dòng) | Không cho phép edit/xoá bất kỳ dòng nào | BR-162, BR-172 | Cột: Timestamp, User, IP Address, Action, Resource, Status |
| 6 | Nút "Export CSV/PDF" | Button | — | BR-171 | Không có trong Normal Flow gốc của UC-106 nhưng nhất quán với các màn báo cáo khác (BR-178) — **[Giả định — cần team xác nhận]** |
| 7 | Phân trang | Pagination | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table trong lúc truy vấn audit_log server (bảng có thể lớn — cần cảnh báo thời gian tải).
- Empty: "Không có bản ghi audit nào khớp bộ lọc hiện tại" kèm gợi ý nới bộ lọc ngày.
- Error: MSG-004 nếu phiên hết hạn giữa lúc filter; MSG-005 nếu không có quyền (route bị chặn RBAC trước khi vào màn hình).
- Success: Bảng hiển thị đầy đủ, không có trạng thái "success" dạng toast vì đây là màn hình chỉ đọc.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar "Audit Log" (nhóm menu Audit Log, `Navigation_Guidelines.md` mục 1.1). Không có màn hình con — đây là node lá trong sơ đồ điều hướng System Admin.
**Bố cục**: Dashboard Shell chuẩn (`Layout_Guidelines.md` mục 1.1) — Topbar + Sidebar + Content. Content Area: thanh filter ngang trên cùng (grid-cols-12, mỗi filter col-span-3), bảng `Table` chiếm toàn bộ chiều rộng bên dưới, phân trang cuối bảng. Dùng component `Table`/`DataTable` theo `Component_Guidelines.md` mục 1 (không dùng Card list vì đây là actor Web-only, dữ liệu dày đặc).

*(School Administration cũng có quyền xem Audit Logs theo cột "x" ở `14_Screen_Authorization_Matrix.md` khối 2 — School Admin chỉ xem phạm vi trường mình theo BR-176; ngoài phạm vi công việc actor "System Admin" của Nam, xem thêm ghi chú tại School Admin nếu cần màn tương ứng.)*

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Audit Logs, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Sidebar trái + Topbar + Content (filter bar ngang trên, bảng dữ liệu bên dưới, phân trang cuối).
Thành phần bắt buộc: filter Date range, filter User ID/tên, filter Action category, filter IP Address, bảng đọc-only (Timestamp/User/IP/Action/Resource/Status), nút Export.
Áp dụng Design System: 00_Foundation/Design_System.md (bảng dữ liệu dùng màu neutral, không dùng semantic color trừ cột Status).
Trạng thái cần thể hiện: loading (skeleton table), empty, success (bảng đầy đủ).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4883–4903 (Table 3-149 View Audit Logs Use Cases Specification)
- traceability_matrix.md §1.12 (UC-106 ↔ BR-162/170/171/172/174 ↔ MSG-004/005)
- 03_Design/02_ADR/ADR-004.md (audit_log bất biến, chỉ INSERT/SELECT)
- 03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md
