# Screen Spec — Inventory Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-075, BR-076, BR-077, BR-089, BR-205
**MSG liên quan**: MSG-018 (Warning/Error — thiếu kho), MSG-042 (Success), MSG-052 (Warning — sắp hết hạn), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn — ĐÃ XỬ LÝ (2026-07-02)**: `Report 3.md` UC-045 Business Rules trích "BR-076, BR-081, BR-082, BR-083, BR-089, BR-090" — các mã BR-081/082/083 đều không khớp nghĩa thật trong `business_rules.md` (BR-081 đã xoá; BR-082 = Chế biến riêng; BR-083 = Ghi nhận bằng chứng Three-Step). Đã cấp mã mới: **BR-202** (storage zone matching, UC-061), **BR-204** (reason code/sign-off, UC-064), **BR-205** (khóa nguyên liệu hết hạn, UC-045). Screen Spec này dùng **BR-075 (FEFO), BR-076 (truy xuất lô), BR-077 (cảnh báo thiếu kho), BR-089 (giám sát nhiệt độ kho), BR-205 (khóa hết hạn)** làm nguồn chính xác.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tồn kho theo nguyên liệu, khu vực lưu trữ, hạn dùng lô | Table (Web) / Card list (Mobile) | Nhóm theo ingredient/zone/expiration | BR-076 | UC-045 Normal Flow bước 2 |
| 2 | Nút "Xuất kho" (Issue) | Button | Điều hướng Issue Ingredients Workspace, áp dụng FEFO | BR-075 | UC-045 Normal Flow bước 3–4 |
| 3 | Nút "Điều chỉnh Kho" (Adjustment) | Button | Yêu cầu mã lý do bắt buộc + xác nhận quản lý (spoilage write-off) | — | UC-045 Alternative Flow AF1 |
| 4 | Cảnh báo thiếu kho | Alert banner | Hiện khi tồn không đủ nhu cầu kế hoạch | BR-077 | UC-045 Alternative Flow AF2, MSG-018 |
| 5 | Cảnh báo sắp hết hạn | Badge/Banner | Hiện theo ngưỡng cảnh báo hết hạn nguyên liệu kho | BR-205 | MSG-052 |
| 6 | Nút "Xem Cảnh báo Hết hạn" | Button | Điều hướng Monitor Expiration Dates Panel | — | ScreenFlow |
| 7 | Nút "Xem Nhật ký Giao dịch Kho" | Button | Điều hướng Record Inventory Transactions Log | — | ScreenFlow |
| 8 | Nút "Tạo Yêu cầu Mua hàng" (khi tồn thấp) | Button | Điều hướng Create Purchase Request | BR-077 | ScreenFlow: `InvDash -->|"low stock"| CreatePR` |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách tồn kho.
- Empty: Kho trống hoàn toàn (hiếm) → thông báo + gợi ý tạo yêu cầu mua hàng.
- Error: MSG-018 khi thiếu kho; `Alert variant="destructive"` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `Toast` MSG-042 sau khi ghi nhận giao dịch kho.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Nhận hàng & Kho" trong Sidebar. Điều hướng tiếp: Issue Ingredients Workspace, Monitor Expiration Dates Panel, Record Inventory Transactions Log, Create Purchase Request.
**Bố cục**: Page Header + `Table` tồn kho (cột: Nguyên liệu, Khu vực, Số lượng, Hạn dùng gần nhất, Trạng thái) + thanh filter/search + nút hành động (Xuất kho/Điều chỉnh) ở cột cuối.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inventory Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table tồn kho (Nguyên liệu, Khu vực, Số lượng, Hạn dùng, Trạng thái, Hành động Xuất kho/Điều chỉnh), banner cảnh báo thiếu kho/sắp hết hạn.
Thành phần bắt buộc: badge cảnh báo (MSG-018, MSG-052), nút Tạo Yêu cầu Mua hàng khi tồn thấp.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-075, BR-076, BR-077, BR-089, BR-205 (xem lưu ý mâu thuẫn nguồn đã giải quyết)
- error_message.md MSG-018, MSG-042, MSG-052
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Inventory → Create Purchase Request)
