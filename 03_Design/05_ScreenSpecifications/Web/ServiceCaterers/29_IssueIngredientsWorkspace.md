# Screen Spec — Issue Ingredients Workspace

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2, sub-flow "issue ingredients following FEFO") | **Status**: Draft
**BR liên quan**: BR-075, BR-076
**MSG liên quan**: MSG-018 (Error — thiếu kho), MSG-042 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn phiên sản xuất/thực đơn cần xuất nguyên liệu | Select | Bắt buộc | — | UC-045 Normal Flow bước 3 |
| 2 | Danh sách nguyên liệu cần xuất (theo thực đơn) + số lượng | Table (Web) / Card list (Mobile) | Tự động gợi ý lô theo FEFO (hết hạn sớm nhất trước) | BR-075 | UC-045 Normal Flow bước 4 |
| 3 | Xác nhận lô được chọn để xuất | Read-only + nút "Đổi lô khác" | Không cho chọn lô hết hạn | BR-076 | UC-045 Normal Flow bước 4 |
| 4 | Cảnh báo thiếu kho | Alert banner | Hiện nếu không đủ nguyên liệu cần thiết | BR-077 | UC-045 Alternative Flow AF2, MSG-018 |
| 5 | Nút "Xác nhận Xuất kho" | Button | Cập nhật tồn kho, ghi nhận giao dịch | — | UC-045 Normal Flow bước 5, MSG-042 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách nguyên liệu cần xuất.
- Empty: Không áp dụng.
- Error: MSG-018 nếu thiếu kho, dẫn tới đề xuất Create Purchase Request.
- Success: `Toast` MSG-042.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Inventory Management Dashboard (nút "Xuất kho"). Thoát: xác nhận xong quay lại Dashboard.
**Bố cục**: `Dialog`/trang riêng — `Table` danh sách nguyên liệu cần xuất kèm lô đề xuất theo FEFO, nút xác nhận cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Issue Ingredients Workspace, nền tảng Web, actor Service Caterers.
Bố cục: Table danh sách nguyên liệu cần xuất (lô đề xuất theo FEFO, số lượng), banner cảnh báo thiếu kho, nút Xác nhận Xuất kho.
Thành phần bắt buộc: cảnh báo MSG-018 khi thiếu kho.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-075, BR-076, BR-077
- error_message.md MSG-018, MSG-042
