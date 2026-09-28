# Screen Spec — Service Caterers Dashboard

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng (màn hình điều hướng gốc) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp
**MSG liên quan**: Không áp dụng trực tiếp (trang tổng quan, không có thao tác ghi dữ liệu)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Lời chào + tên Service Caterers đang đăng nhập | Text | Lấy từ session người dùng | — | — |
| 2 | Grid Menu Items (8 mục điều hướng: Thực đơn, Nhận hàng, Kho, Kiểm tra 3 Bước, Lưu mẫu Thực phẩm, Phục vụ Bữa ăn, Sự cố & Hành động Khắc phục, Vi phạm) | Card grid (2 cột) | Mỗi mục điều hướng tới đúng 1 Dashboard con | — | Cấu trúc lấy từ `04_Service_Caterers_Screen_Flow.md` (nhánh "Grid Menu Items") |
| 3 | Widget tóm tắt hoạt động hôm nay (số phiên nhận hàng, số bước kiểm tra đang chờ, số cảnh báo dị ứng đang hoạt động) | Card thống kê | Cập nhật theo BR-180 nếu áp dụng | BR-180 | **[Giả định — cần team xác nhận]** nội dung widget cụ thể chưa có mô tả field-by-field gốc trong Report 3 |
| 4 | Banner trạng thái mạng (Offline/Cần đồng bộ) | Banner cố định | Hiện khi offline hoặc cache quá hạn | BR-196–200 | Theo `00_Foundation/UI_Design_Guidelines.md` mục 5.5 |

### Trạng thái màn hình
- Loading: Shimmer cho grid + widget thống kê.
- Empty: Không áp dụng (grid điều hướng luôn hiển thị đủ 8 mục cố định).
- Error: `SnackBar` màu `danger` nếu tải widget thống kê thất bại, kèm nút "Thử lại".
- Success: Không áp dụng (không có thao tác ghi).

---

## Platform Layout — Mobile

**Entry/Exit points**: Vào ngay sau khi đăng nhập thành công qua Common Entry Flow (`13_Guest_Common_Screen_Flow.md`); là màn hình gốc (`04_Service_Caterers_Screen_Flow.md`). Thoát: chạm 1 thẻ trong Grid Menu Items để vào Dashboard con tương ứng.
**Bố cục**: Theo `00_Foundation/Layout_Guidelines.md` mục 2.1 — AppBar (tiêu đề "Service Caterers") + Content 1 cột cuộn dọc: widget thống kê trên cùng, Grid Menu Items dạng Card 2 cột bên dưới + `BottomNavigationBar` 4-5 mục (theo tần suất sử dụng: Kiểm tra 3 Bước, Nhận hàng, Kho, Thêm — theo `00_Foundation/Navigation_Guidelines.md` mục 2). Pull-to-refresh bắt buộc cho widget thống kê.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Service Caterers Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar (tiêu đề) + Content 1 cột cuộn dọc (widget thống kê + Grid Menu Items 2 cột dạng Card) + BottomNavigationBar 4-5 mục có icon + label chữ.
Thành phần bắt buộc: lời chào người dùng, widget thống kê hoạt động hôm nay, grid 8 mục điều hướng module chính, banner trạng thái mạng nếu offline.
Áp dụng Design System: seed color #0E7490 (ColorScheme.fromSeed), Material 3, font Be Vietnam Pro, touch target tối thiểu 48x48dp (xem 00_Foundation/Design_System.md, Accessibility.md).
Trạng thái cần thể hiện: loading (shimmer), error (snackbar khi widget lỗi), banner offline nếu áp dụng.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (Grid Menu Items, sơ đồ điều hướng gốc)
- `02_Requirements/00_BusinessRules/business_rules.md` BR-180, BR-196–200
