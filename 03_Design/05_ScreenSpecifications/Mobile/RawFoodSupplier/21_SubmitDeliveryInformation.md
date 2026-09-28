# Screen Spec — Submit Delivery Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-083 - Submit Delivery Information | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Delivery Information" | — | |
| 2 | Purchase Request đích | Select | Bắt buộc, chỉ hiện PR đã Accept | — | |
| 3 | Dispatch Date | Date picker | Bắt buộc, ≥24 giờ trước giờ giao dự kiến | BR-071 | |
| 4 | Vehicle License Plate | Input text | Bắt buộc | BR-071 | |
| 5 | Driver Details | Select | Bắt buộc, chọn từ Delivery Person đã đăng ký | BR-050 | |
| 6 | Liên kết lô nguyên liệu | Multi-select | Bắt buộc ≥1 | BR-071 | |
| 7 | "Submit" Button | Button | Validate ≥24h + đầy đủ field | BR-071 | |
| 8 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: Chặn nếu nộp <24 giờ trước giờ giao.
- Success: 'Scheduled' → điều hướng Delivery Detail (20).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Deliveries (18)/Delivery List (19) qua FAB "+". Tương ứng node `ShipForm["Raw Material Shipment Form"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`, dẫn tiếp `ShipDocs["Attach Delivery Documents"]`/`ShipTrack["Shipment Tracking"]`.
**Bố cục**: Form nhiều trang riêng (`Layout_Guidelines.md` §2.2) — Trang 1: Purchase Request/Dispatch Date; Trang 2: Vehicle/Driver; Trang 3: liên kết lô nguyên liệu. Nút Tiếp theo/Quay lại cố định dưới cùng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Delivery Information, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: form 3 trang riêng, nút Tiếp theo/Quay lại cố định dưới cùng.
Component bắt buộc dùng: DropdownMenu, DatePicker (M3), TextField, FilledButton.
Thành phần bắt buộc: Purchase Request đích, Dispatch Date, Vehicle License Plate, Driver, liên kết lô nguyên liệu.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3287–3307 (UC-083).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `ShipForm`).
