# Screen Spec — Store Ingredients Form (Storage Assignment)

**Actor**: Service Caterers | **UC liên quan**: UC-061 - Record Ingredient Storage (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-202, BR-090
**MSG liên quan**: MSG-004/MSG-005 (Error), MSG-041 (Success — dùng chung ngữ cảnh cập nhật kho)

> **✅ Đã xử lý (2026-07-01)**: rule "khu vực lưu trữ phải khớp nhiệt độ" trước đây trích nhầm là "BR-081" — số này đã bị chiếm bởi 1 rule khác (Menu & Diet Management "món thay thế dị ứng", nay đã xoá khỏi `business_rules.md`). Đã chuyển sang mã mới **BR-202**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô hàng đã nhận, chờ xếp kho | Table (Web) / Card list (Mobile) | Chỉ hiển thị batch đã Received nhưng chưa Stored | — | UC-061 Preconditions |
| 2 | Quét/nhập mã vạch lô hàng | Input/Barcode scan | Bắt buộc | BR-090 | UC-061 Normal Flow bước 2 |
| 3 | Chọn khu vực lưu trữ (Dry/Chilled/Frozen) | Select/RadioGroup | Phải khớp nhóm nhiệt độ yêu cầu của nguyên liệu | BR-202 | UC-061 Normal Flow bước 3–4 |
| 4 | Nút "Xác nhận Vị trí Lưu trữ" | Button | Chặn nếu khu vực không khớp nhiệt độ yêu cầu | BR-202 | UC-061 Exceptions |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách lô hàng chờ xếp kho.
- Empty: Không còn lô hàng nào chờ xếp kho.
- Error: `Alert variant="destructive"` khi chọn sai khu vực nhiệt độ (BR-202) — chặn lưu.
- Success: `Toast` xác nhận lưu vị trí thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ cuối Record Ingredient Receiving (`04_Service_Caterers_Screen_Flow.md`: `RecordRecv --> StoreForm`). Thoát: xác nhận xong quay lại Receiving Management Dashboard.
**Bố cục**: Form ngắn trong `Dialog`, danh sách batch chờ xếp kho dạng `Table` bên trái, form chọn khu vực lưu trữ bên phải theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Store Ingredients Form (Storage Assignment), nền tảng Web, actor Service Caterers.
Bố cục: Dialog 2 cột — Table batch chờ xếp kho bên trái, form chọn khu vực lưu trữ (Dry/Chilled/Frozen) bên phải, nút Xác nhận Vị trí Lưu trữ.
Thành phần bắt buộc: cảnh báo lỗi khi khu vực không khớp nhiệt độ yêu cầu.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3177–3195 (UC-061 - Record Ingredient Storage)
- business_rules.md BR-202, BR-090
