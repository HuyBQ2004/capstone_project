# Screen Spec — School Partnerships

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình tổng hợp trạng thái hợp tác, điều hướng tới UC-152 (Submit), UC-169 (Track), UC-170 (Update/Resubmit) qua UC-155 (Manage Supplier Application) | **Status**: Draft
**BR liên quan**: BR-041 (trạng thái nhà cung cấp Pending/Approved/Rejected), BR-046, BR-047 (tài liệu tuân thủ + cảnh báo hết hạn)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Partner details" (theo Report 3 §3.3.2.5.2) | — | |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Partnership Application (07) khi chưa có đơn hoặc muốn nộp thêm trường mới | — | |
| 4 | School Partner Card | Card | Container hiển thị thông tin quản trị/liên hệ của trường | — | Report 3 §3.3.2.5.2 |
| 5 | School Name | Text | Tên trường đối tác | — | |
| 6 | Delivery address | Text | Địa chỉ giao hàng chỉ định | — | |
| 7 | Trạng thái hợp tác (Badge) | Badge | Pending / Approved / Rejected | BR-041 | Màu theo `Design_System.md` §3 (Pending=info, Approved=success, Rejected=danger) |
| 8 | Trạng thái tài liệu tuân thủ (tóm tắt) | Badge | Valid / Expiring Soon / Expired | BR-046, BR-047 | Điều hướng tới Compliance Documents (26) |
| 9 | "Xem lịch sử phê duyệt" Button | Navigation | Điều hướng tới Application Status (08), phần lịch sử (Partnership Approval History, chỉ đọc) | — | `06_Raw_Food_Supplier_Screen_Flow.md` node `PartnerHist` |

### Trạng thái màn hình
- Loading: Skeleton School Partner Card.
- Empty: Chưa có partnership nào → CTA "Nộp đơn hợp tác" (điều hướng 07).
- Error: MSG-004, MSG-005.
- Success: N/A (màn hình hiển thị, không ghi dữ liệu trực tiếp).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "School Partnerships", hoặc từ Select School Workspace (02). Tương ứng node `PartnerD["Partnership Detail"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Partnership Application (07), Application Status (08).
**Bố cục**: Admin Shell — Sidebar nhóm "Hợp tác với Trường", Content Area 1 School Partner Card lớn (col-span-8) + panel trạng thái/tài liệu bên phải (col-span-4).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình School Partnerships, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, School Partner Card lớn bên trái, panel trạng thái hợp tác + tài liệu tuân thủ bên phải.
Component bắt buộc dùng: Card, Badge, Button, Breadcrumb.
Thành phần bắt buộc: School Name, Delivery address, Badge trạng thái hợp tác, Badge trạng thái tài liệu, nút xem lịch sử phê duyệt, nút Add/Plus.
Áp dụng Design System: primary #0E7490, badge màu theo success/warning/danger/info — xem Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty (chưa có partnership).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6201–6218 (§3.3.2.5.2 Partnership Detail).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-041, BR-046, BR-047.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md`.
