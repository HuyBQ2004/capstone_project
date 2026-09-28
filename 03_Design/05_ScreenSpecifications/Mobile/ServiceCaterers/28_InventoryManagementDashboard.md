# Screen Spec — Inventory Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-075, BR-076, BR-077, BR-089, BR-205
**MSG liên quan**: MSG-018 (Warning/Error), MSG-042 (Success), MSG-052 (Warning), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn — ĐÃ XỬ LÝ (2026-07-02)**: giống bản Web — Report 3 UC-045 trích BR-081/082/083/089/090 với ngữ nghĩa kho không khớp nội dung thật của các mã này trong `business_rules.md`. Đã cấp mã mới: **BR-202** (storage zone matching), **BR-204** (reason code/sign-off), **BR-205** (khóa nguyên liệu hết hạn). Dùng **BR-075, BR-076, BR-077, BR-089, BR-205** đúng ngữ cảnh.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tồn kho theo nguyên liệu, khu vực lưu trữ, hạn dùng lô | Card list | Nhóm theo ingredient/zone/expiration | BR-076 | UC-045 Normal Flow bước 2 |
| 2 | Nút "Xuất kho" (Issue) | Button | Điều hướng Issue Ingredients Workspace, áp dụng FEFO | BR-075 | UC-045 Normal Flow bước 3–4 |
| 3 | Nút "Điều chỉnh Kho" (Adjustment) | Button | Yêu cầu mã lý do bắt buộc + xác nhận quản lý | — | UC-045 Alternative Flow AF1 |
| 4 | Cảnh báo thiếu kho | MaterialBanner | Hiện khi tồn không đủ nhu cầu kế hoạch | BR-077 | MSG-018 |
| 5 | Cảnh báo sắp hết hạn | Chip/Banner | Hiện theo ngưỡng cảnh báo hết hạn nguyên liệu kho | BR-205 | MSG-052 |
| 6 | Nút "Xem Cảnh báo Hết hạn" | Button (AppBar action) | Điều hướng Monitor Expiration Dates Panel | — | ScreenFlow |
| 7 | Nút "Xem Nhật ký Giao dịch Kho" | Button (AppBar action) | Điều hướng Record Inventory Transactions Log | — | ScreenFlow |
| 8 | Nút "Tạo Yêu cầu Mua hàng" (khi tồn thấp) | Button | Điều hướng Create Purchase Request | BR-077 | ScreenFlow: `InvDash -->|"low stock"| CreatePR` |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách tồn kho.
- Empty: Kho trống hoàn toàn → thông báo + gợi ý tạo yêu cầu mua hàng.
- Error: MSG-018 khi thiếu kho; `SnackBar` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `SnackBar` MSG-042 sau khi ghi nhận giao dịch kho.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Kho" trong Grid Menu Items. Điều hướng tiếp: Issue Ingredients Workspace, Monitor Expiration Dates Panel, Record Inventory Transactions Log, Create Purchase Request.
**Bố cục**: AppBar + Card list tồn kho (Nguyên liệu, Khu vực, Số lượng, Hạn dùng, badge cảnh báo), pull-to-refresh, FAB điều hướng nhanh tới Xuất kho.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inventory Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list tồn kho (Nguyên liệu, Khu vực, Số lượng, Hạn dùng, badge cảnh báo), FAB Xuất kho.
Thành phần bắt buộc: banner cảnh báo thiếu kho/sắp hết hạn (MSG-018/MSG-052).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-075, BR-076, BR-077, BR-089, BR-205
- error_message.md MSG-018, MSG-042, MSG-052
