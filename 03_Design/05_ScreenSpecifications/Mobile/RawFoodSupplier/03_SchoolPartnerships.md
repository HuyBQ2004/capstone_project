# Screen Spec — School Partnerships

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình tổng hợp trạng thái hợp tác, điều hướng tới UC-152 (Submit), UC-169 (Track), UC-170 (Update/Resubmit) qua UC-155 (Manage Supplier Application) | **Status**: Draft
**BR liên quan**: BR-041 (trạng thái nhà cung cấp Pending/Approved/Rejected), BR-046, BR-047 (tài liệu tuân thủ + cảnh báo hết hạn)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Partner details" (theo Report 3 §3.3.2.5.2) | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Partnership Application (07) | — | |
| 4 | School Partner Card | Card | Container hiển thị thông tin quản trị/liên hệ của trường | — | Report 3 §3.3.2.5.2 |
| 5 | School Name | Text | Tên trường đối tác | — | |
| 6 | Delivery address | Text | Địa chỉ giao hàng chỉ định | — | |
| 7 | Trạng thái hợp tác (Chip) | Chip | Pending / Approved / Rejected | BR-041 | Màu theo `Design_System.md` §3 |
| 8 | Trạng thái tài liệu tuân thủ (tóm tắt) | Chip | Valid / Expiring Soon / Expired | BR-046, BR-047 | Điều hướng tới Compliance Documents (26) |
| 9 | "Xem lịch sử phê duyệt" Button | Navigation | Điều hướng tới Application Status (08) | — | `06_Raw_Food_Supplier_Screen_Flow.md` node `PartnerHist` |

### Trạng thái màn hình
- Loading: Shimmer School Partner Card.
- Empty: Chưa có partnership nào → CTA "Nộp đơn hợp tác" (điều hướng 07).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Partnership status card". Tương ứng node `PartnerD["Partnership Detail"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: `PartnerApp` (Partnership Application, 07), `PartnerHist` (Application Status, 08, chỉ đọc).
**Bố cục**: 1 cột — AppBar (Back + tiêu đề), School Partner Card full-width, Chip trạng thái ngay dưới Card, 2 nút hành động cuối màn hình ("Nộp đơn mới" / "Xem lịch sử phê duyệt").

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình School Partnerships (Partnership Detail), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, AppBar Back, School Partner Card full-width, Chip trạng thái, 2 nút hành động cuối màn hình.
Component bắt buộc dùng: Card (M3), Chip, Button.
Thành phần bắt buộc: School Name, Delivery address, Chip trạng thái hợp tác, Chip trạng thái tài liệu, nút xem lịch sử phê duyệt, nút Add/Plus.
Áp dụng Design System: seed color #0E7490, màu chip theo success/warning/danger/info.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6201–6218 (§3.3.2.5.2 Partnership Detail).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-041, BR-046, BR-047.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
