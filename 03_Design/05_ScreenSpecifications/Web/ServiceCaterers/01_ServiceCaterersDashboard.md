# Screen Spec — Service Caterers Dashboard

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng (màn hình điều hướng gốc) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp
**MSG liên quan**: Không áp dụng trực tiếp (trang tổng quan, không có thao tác ghi dữ liệu)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Lời chào + tên Service Caterers đang đăng nhập | Text | Lấy từ session người dùng | — | — |
| 2 | Grid Menu Items (8 mục điều hướng: Thực đơn, Nhận hàng, Kho, Kiểm tra 3 Bước, Lưu mẫu Thực phẩm, Phục vụ Bữa ăn, Sự cố & Hành động Khắc phục, Vi phạm) | Card/Button grid | Mỗi mục điều hướng tới đúng 1 Dashboard con | — | Cấu trúc lấy từ `04_Service_Caterers_Screen_Flow.md` (nhánh "Grid Menu Items") + `09_Service_Caterers_Screen_Flow_Web.md` (Sidebar 2 cấp) |
| 3 | Widget tóm tắt hoạt động hôm nay (số phiên nhận hàng, số bước kiểm tra đang chờ, số cảnh báo dị ứng đang hoạt động) | Card thống kê | Cập nhật theo BR-180 (dashboard làm mới trong 5 phút) nếu áp dụng | BR-180 | **[Giả định — cần team xác nhận]** nội dung widget cụ thể chưa có mô tả field-by-field gốc trong Report 3, chỉ suy ra từ vai trò tổng quan của 1 trang Dashboard |

### Trạng thái màn hình
- Loading: Skeleton cho grid + widget thống kê trong lúc tải dữ liệu tổng quan.
- Empty: Không áp dụng (grid điều hướng luôn hiển thị đủ 8 mục cố định).
- Error: `Alert variant="destructive"` nếu tải widget thống kê thất bại, kèm nút "Thử lại" — không chặn việc dùng Grid Menu Items.
- Success: Không áp dụng (không có thao tác ghi).

---

## Platform Layout — Web

**Entry/Exit points**: Vào ngay sau khi đăng nhập thành công qua Common Entry Flow (`13_Guest_Common_Screen_Flow.md`); là màn hình gốc của Sidebar 2 cấp (`09_Service_Caterers_Screen_Flow_Web.md`). Thoát: chọn 1 nhóm trong Sidebar để vào Dashboard con tương ứng (Menu Management, Receiving, Inventory, Three-Step Inspection, Food Sample Retention, Meal Service, Corrective Actions/Violation).
**Bố cục**: Theo `00_Foundation/Layout_Guidelines.md` mục 1.1 — Topbar (Logo/Breadcrumb/Notification/Avatar) + Sidebar cố định trái (menu theo `09_Service_Caterers_Screen_Flow_Web.md` mục "Sidebar 2 cấp") + Content Area dùng grid 12 cột: widget thống kê hàng trên (`col-span-4` mỗi thẻ), Grid Menu Items hàng dưới dạng `Card` lớn (`col-span-3`/`col-span-4` tuỳ số lượng, responsive theo `00_Foundation/Responsive_Guidelines.md`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Service Caterers Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Topbar (logo, breadcrumb, notification, avatar) + Sidebar trái 2 cấp (Thực đơn, Nhận hàng & Kho, Kiểm tra 3 Bước, Lưu mẫu Thực phẩm, Phục vụ Bữa ăn, Sự cố & Vi phạm, Cá nhân) + Content Area.
Thành phần bắt buộc: lời chào người dùng, widget thống kê hoạt động hôm nay, grid 8 mục điều hướng module chính.
Áp dụng Design System: màu primary #0E7490, font Be Vietnam Pro, card bo góc 8px, badge trạng thái theo token success/warning/danger/critical/info (xem 00_Foundation/Design_System.md).
Trạng thái cần thể hiện: loading (skeleton widget), error (alert khi widget lỗi).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` (Sidebar 2 cấp, sơ đồ điều hướng gốc)
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (Grid Menu Items)
- `02_Requirements/00_BusinessRules/business_rules.md` BR-180 (widget thống kê, giả định tham chiếu)
