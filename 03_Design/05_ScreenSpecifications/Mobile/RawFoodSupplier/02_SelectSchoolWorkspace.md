# Screen Spec — Select School Workspace

**Actor**: Raw Food Supplier | **UC liên quan**: Không tìm thấy UC riêng trong Report 3 §3.2 hay `14_Screen_Authorization_Matrix.md` khối 5 (cột UC để trống "—") | **Status**: Draft — [Giả định, cần team xác nhận]
**BR liên quan**: [Giả định] BR-041 (trạng thái Approved/Rejected/Pending theo từng trường) — suy luận, chưa có BR tường minh cho việc 1 supplier phục vụ nhiều trường cùng lúc
**MSG liên quan**: MSG-004, MSG-005

> **[Giả định — cần team xác nhận]**: Xem giải thích đầy đủ ở bản Web cùng tên (`Web/RawFoodSupplier/02_SelectSchoolWorkspace.md`) — không có UC/BR nào xác nhận tường minh mô hình 1 supplier phục vụ nhiều trường. Field bên dưới là suy luận tối thiểu từ tên màn hình trong Authorization Matrix.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Chọn trường đang làm việc" | — | [Giả định] |
| 2 | Danh sách trường đối tác (Approved) | List/Card | Mỗi mục: tên trường, trạng thái hợp tác | BR-041 | [Giả định] Chỉ hiển thị trường có partnership Approved |
| 3 | "Chọn" Button trên mỗi mục | Button | Chọn trường làm workspace hiện tại, lưu vào session/context | — | [Giả định] |
| 4 | "Nộp đơn hợp tác trường mới" Button | Navigation | Điều hướng tới Partnership Application (07) | — | [Giả định] |
| 5 | Workspace hiện tại (chip/badge trên AppBar sau khi chọn) | Badge | Hiển thị tên trường đang active | — | [Giả định] |

### Trạng thái màn hình
- Loading: Shimmer danh sách trường.
- Empty: Chưa có trường đối tác nào Approved → hiển thị CTA "Nộp đơn hợp tác trường mới".
- Error: MSG-004, MSG-005.
- Success: Chọn xong → chuyển thẳng Supplier Dashboard (01) với context trường đã chọn.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào ngay sau Login nếu supplier có ≥2 trường Approved (nếu chỉ có 1 trường, bỏ qua màn này) — [Giả định]. Không xuất hiện trong `06_Raw_Food_Supplier_Screen_Flow.md` gốc — bổ sung theo tên màn hình trong Authorization Matrix.
**Bố cục**: 1 cột — AppBar tối giản (không back nếu là màn đầu sau Login), danh sách Card trường đối tác cuộn dọc, mỗi Card có nút "Chọn"; không có BottomNavigationBar (chưa vào ngữ cảnh chính).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Select School Workspace, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, danh sách Card trường đối tác, mỗi card có nút "Chọn", không có BottomNavigationBar.
Component bắt buộc dùng: Card (M3), Button.
Thành phần bắt buộc: danh sách trường Approved, nút chọn, nút nộp đơn hợp tác trường mới.
Áp dụng Design System: seed color #0E7490, Be Vietnam Pro, Material Design 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đây là màn hình còn [Giả định], giữ tối giản.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5 (dòng "Select School Workspace").
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (Supplier Dashboard).
- Không có nguồn UC/BR trực tiếp — xem cảnh báo [Giả định] ở trên.
