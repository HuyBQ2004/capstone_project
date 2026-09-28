# Screen Spec — Monitor Expiration Dates Panel (Expiration Alerts)

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2, sub-flow cảnh báo hạn dùng) | **Status**: Draft
**BR liên quan**: BR-076, BR-205 (Khóa/cảnh báo hết hạn nguyên liệu kho)
**MSG liên quan**: MSG-052 (Warning), MSG-004/MSG-005 (Error)

> **Lưu ý cập nhật (2026-07-02)**: Bản cũ trích **BR-047** (cảnh báo hết hạn **tài liệu nhà cung cấp**) cho ngữ cảnh cảnh báo lô nguyên liệu kho sắp hết hạn — đây là citation sai chủ đề. Mã đúng là **BR-205** (bổ sung 2026-07-02, xem `business_rules.md` §6).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô nguyên liệu sắp hết hạn | Card list | Mỗi card: Nguyên liệu, Số lô, Ngày hết hạn, Số lượng còn lại | BR-076 | UC-045 sub-flow |
| 2 | Chip "Expiring Soon" | Chip màu `warning` | Hiện khi còn dưới 30 ngày | BR-205 | MSG-052 |
| 3 | Sắp xếp theo mức độ khẩn cấp | Sort mặc định | — | BR-075 | — |
| 4 | Nút "Xuất kho ngay" trên từng card | Button | Điều hướng Issue Ingredients Workspace | — | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Không có lô nào sắp hết hạn — thông báo xác nhận tích cực.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Inventory Management Dashboard (icon "Xem Cảnh báo Hết hạn"). Thoát: back về Dashboard, hoặc điều hướng Issue Ingredients Workspace.
**Bố cục**: AppBar + Card list sắp xếp theo ngày hết hạn tăng dần (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Monitor Expiration Dates Panel (Expiration Alerts), nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list lô nguyên liệu sắp hết hạn (Nguyên liệu, Số lô, Ngày hết hạn, Số lượng, chip Expiring Soon, nút Xuất kho ngay).
Thành phần bắt buộc: chip màu warning cho "Expiring Soon".
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty (tích cực), error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-076, BR-205
- error_message.md MSG-052
