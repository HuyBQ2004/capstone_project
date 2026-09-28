# Screen Spec — Monitor Expiration Dates Panel (Expiration Alerts)

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2, sub-flow cảnh báo hạn dùng) | **Status**: Draft
**BR liên quan**: BR-076, BR-205 (Khóa/cảnh báo hết hạn nguyên liệu kho)
**MSG liên quan**: MSG-052 (Warning), MSG-004/MSG-005 (Error)

> **Lưu ý cập nhật (2026-07-02)**: Bản cũ trích **BR-047** (cảnh báo hết hạn **tài liệu nhà cung cấp**) cho ngữ cảnh "badge Expiring Soon" trên lô nguyên liệu kho — đây là citation sai chủ đề (BR-047 thuộc §5 Supplier, không phải §6 Inventory). Mã đúng cho cảnh báo hết hạn nguyên liệu kho là **BR-205** (bổ sung 2026-07-02). MSG-052 cũng đã được cập nhật repoint từ BR-047/BR-083 sang BR-205.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô nguyên liệu sắp hết hạn | Table (Web) / Card list (Mobile) | Cột: Nguyên liệu, Số lô, Ngày hết hạn, Số lượng còn lại | BR-076 | UC-045 sub-flow |
| 2 | Badge "Expiring Soon" | Badge màu `warning` | Hiện khi lô nguyên liệu còn dưới 30 ngày (cảnh báo hết hạn nguyên liệu kho) | BR-205 | MSG-052 |
| 3 | Sắp xếp theo mức độ khẩn cấp (gần hết hạn nhất lên đầu) | Sort mặc định | — | BR-075 (FEFO) | — |
| 4 | Nút "Xuất kho ngay" trên từng dòng | Button | Điều hướng Issue Ingredients Workspace với lô đã chọn | — | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách.
- Empty: Không có lô nào sắp hết hạn — thông báo xác nhận tích cực.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/điều hướng).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Inventory Management Dashboard (nút "Xem Cảnh báo Hết hạn"). Thoát: back về Dashboard, hoặc điều hướng Issue Ingredients Workspace.
**Bố cục**: Page Header + `Table` (Nguyên liệu, Số lô, Ngày hết hạn, Số lượng, Badge, Hành động) sắp xếp theo ngày hết hạn tăng dần.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Monitor Expiration Dates Panel (Expiration Alerts), nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table lô nguyên liệu sắp hết hạn (Nguyên liệu, Số lô, Ngày hết hạn, Số lượng, badge Expiring Soon, nút Xuất kho ngay).
Thành phần bắt buộc: badge màu warning cho "Expiring Soon".
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty (tích cực), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-076, BR-205
- error_message.md MSG-052
