# Screen Spec — Supplier Dashboard

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry point tổng hợp, điều hướng tới các UC khác (UC-152/155/169/170, UC-139, UC-043, UC-039, UC-167) | **Status**: Draft
**BR liên quan**: Không có BR riêng cho bản thân màn hình (BR áp dụng ở từng màn con được điều hướng tới)
**MSG liên quan**: MSG-004 (Session expired), MSG-005 (Access denied) — dùng chung mọi màn hình có xác thực

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title / Breadcrumb | Text | "Homepage > Supply" | — | Report 3 §3.3.2.5.1 |
| 2 | "Log Out" Icon | Button | Terminate session | BR-004 | Điều hướng về Login |
| 3 | Partner Welcome Banner | Card | Hiển thị tên công ty + trường đối tác hiện đang liên kết (Approved) | BR-041 | Nếu chưa có partnership Approved nào, hiển thị Empty state gợi ý sang "Partnership Application" (màn 07) |
| 4 | Section Header — "Supply & Logistics Services" | Label | — | — | Report 3 §3.3.2.5.1 |
| 5 | "Purchase Request" Button/Card | Navigation | Điều hướng tới màn Purchase Requests (09) | — | UC-139 |
| 6 | "Delivery Management" Button/Card | Navigation | Điều hướng tới màn Deliveries (18) | — | UC-039 |
| 7 | "Ingredient Batches" Button/Card | Navigation | Điều hướng tới màn Ingredient Batches (12) | — | UC-043. Report 3 gọi là "Raw Material Shipment" — đổi tên hiển thị theo đúng tên màn chính thức trong `14_Screen_Authorization_Matrix.md` khối 5 |
| 8 | "Food Safety Compliance Record" Button/Card | Navigation | Điều hướng tới màn Compliance Documents (26) | — | UC-167 |
| 9 | "School Partnerships" Card | Navigation | Điều hướng tới màn School Partnerships (03), qua Select School Workspace (02) nếu có >1 trường | — | Bổ sung so với Report 3 gốc (chỉ có 4 nút) vì Screen Flow (`06_Raw_Food_Supplier_Screen_Flow.md`) có nhánh "Partnership status card" từ Dashboard — [Giả định — cần team xác nhận vị trí chính xác trên Dashboard] |
| 10 | Notification Bell Icon | Button | Điều hướng Notification Center | — | Common block, UC-088 |

### Trạng thái màn hình
- Loading: Skeleton cho banner + 4 card chức năng.
- Empty: Nếu supplier chưa có partnership nào Approved, Partner Welcome Banner hiển thị "Chưa có trường đối tác — Nộp đơn hợp tác ngay" kèm CTA tới màn 07.
- Error: MSG-004 (session expired) → chuyển Login; MSG-005 (access denied) → Alert destructive.
- Success: N/A (màn hình chỉ hiển thị, không có action ghi dữ liệu).

---

## Platform Layout — Web
**Entry/Exit points**: Vào sau khi Login thành công (Common Entry Flow, `13_Guest_Common_Screen_Flow.md`). Tương ứng node `Dash["Supplier Dashboard (Web)"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Partnership Detail, Purchase Request List, Delivery Management, Compliance Docs, Ingredient Batches.
**Bố cục**: Admin/Dashboard Shell (`Layout_Guidelines.md` §1.1) — Sidebar cố định trái (nhóm menu: Hợp tác với Trường, Đơn Mua hàng, Giao hàng, Tuân thủ & Chứng nhận, Lô Nguyên liệu, Cá nhân — theo `11_Raw_Food_Supplier_Screen_Flow_Web.md` Sidebar 2 cấp), Topbar có breadcrumb, Content Area dùng grid 12 cột với Partner Welcome Banner full-width phía trên, 4–5 Card chức năng dạng `col-span-4/6` bên dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Supplier Dashboard, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell — Sidebar 2 cấp (Hợp tác với Trường / Đơn Mua hàng / Giao hàng / Tuân thủ & Chứng nhận / Lô Nguyên liệu / Cá nhân), Topbar breadcrumb, Content grid 12 cột.
Component bắt buộc dùng: Sidebar (shadcn sidebar block), Breadcrumb, Card, Button, Badge.
Thành phần bắt buộc: Partner Welcome Banner, 4 Card điều hướng (Purchase Request/Delivery Management/Ingredient Batches/Food Safety Compliance Record), School Partnerships card.
Áp dụng Design System: primary #0E7490, Be Vietnam Pro, shadcn new-york style — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (skeleton), empty (chưa có partnership).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (§3.3.2.5.1 Supplier Dashboard).
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md`.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.
