# Screen Spec — Select School Workspace

**Actor**: Raw Food Supplier | **UC liên quan**: Không tìm thấy UC riêng trong Report 3 §3.2 hay `14_Screen_Authorization_Matrix.md` khối 5 (cột UC để trống "—") | **Status**: Draft — [Giả định, cần team xác nhận]
**BR liên quan**: [Giả định] BR-041 (trạng thái Approved/Rejected/Pending theo từng trường) — suy luận, chưa có BR tường minh cho việc 1 supplier phục vụ nhiều trường cùng lúc
**MSG liên quan**: MSG-004, MSG-005

> **[Giả định — cần team xác nhận]**: Không có UC/BR nào trong `Report 3.md` hay `business_rules.md` mô tả tường minh việc 1 Raw Food Supplier có thể có quan hệ đối tác với **nhiều hơn 1 trường** cùng lúc. Report 3 §3.3.2.5.1 (Supplier Dashboard) chỉ mô tả "Partner Welcome Banner" hiển thị **1** trường đối tác duy nhất ("their currently linked institutional school partner" — số ít). Màn hình "Select School Workspace" xuất hiện như 1 dòng riêng trong `14_Screen_Authorization_Matrix.md` khối 5, ngụ ý mô hình multi-school có thể tồn tại, nhưng không có UC/BR nào xác nhận hành vi chi tiết (làm sao dữ liệu batch/delivery/purchase request được phân tách theo từng trường). Toàn bộ field bên dưới là suy luận tối thiểu hợp lý từ tên màn hình, **không suy diễn thêm nghiệp vụ phức tạp hơn** — cần Product Owner/BA xác nhận trước khi khoá spec.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Chọn trường đang làm việc" | — | [Giả định] |
| 2 | Danh sách trường đối tác (Approved) | List/Card | Mỗi mục: tên trường, trạng thái hợp tác | BR-041 | [Giả định] Chỉ hiển thị trường có partnership Approved |
| 3 | "Chọn" Button trên mỗi mục | Button | Chọn trường làm workspace hiện tại, lưu vào session/context | — | [Giả định] |
| 4 | "Nộp đơn hợp tác trường mới" Button | Navigation | Điều hướng tới Partnership Application (07) | — | [Giả định] |
| 5 | Workspace hiện tại (chip/badge trên Topbar sau khi chọn) | Badge | Hiển thị tên trường đang active | — | [Giả định] — dùng để lọc dữ liệu Batch/Delivery/Purchase Request theo đúng trường đang chọn |

### Trạng thái màn hình
- Loading: Skeleton danh sách trường.
- Empty: Chưa có trường đối tác nào Approved → hiển thị CTA "Nộp đơn hợp tác trường mới".
- Error: MSG-004, MSG-005.
- Success: Chọn xong → chuyển thẳng Supplier Dashboard (01) với context trường đã chọn.

---

## Platform Layout — Web
**Entry/Exit points**: Vào ngay sau Login nếu supplier có ≥2 trường Approved (nếu chỉ có 1 trường, bỏ qua màn này, vào thẳng Dashboard) — [Giả định, cần xác nhận logic điều kiện hiển thị]. Không xuất hiện trong `11_Raw_Food_Supplier_Screen_Flow_Web.md` gốc — bổ sung theo tên màn hình trong Authorization Matrix.
**Bố cục**: Layout căn giữa dạng danh sách card đơn giản (tương tự Guest/Common `Layout_Guidelines.md` §1.3 nhưng có sidebar tối giản), không cần Sidebar/Topbar đầy đủ vì đây là bước chọn ngữ cảnh trước khi vào Dashboard chính.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Select School Workspace, nền tảng Web, actor Raw Food Supplier.
Bố cục: layout căn giữa, danh sách Card trường đối tác, mỗi card có nút "Chọn".
Component bắt buộc dùng: Card, Button, Badge.
Thành phần bắt buộc: danh sách trường Approved, nút chọn, nút nộp đơn hợp tác trường mới.
Áp dụng Design System: primary #0E7490, Be Vietnam Pro — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đây là màn hình còn [Giả định], giữ tối giản.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5 (dòng "Select School Workspace").
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (Supplier Dashboard — "currently linked institutional school partner", số ít).
- Không có nguồn UC/BR trực tiếp — xem cảnh báo [Giả định] ở trên.
