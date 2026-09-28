# Screen Spec — View Incident Information Detail

**Actor**: Service Caterers | **UC liên quan**: UC-120 - View Incident Information (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-023 (Error), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách sự cố được gán cho Service Caterers | Table (Web) / Card list (Mobile) | Cột: Mã sự cố, Ngày, Mức độ nghiêm trọng, Trạng thái | BR-147 | UC-120 Normal Flow bước 1 |
| 2 | Mức độ nghiêm trọng | Badge | Low/Medium/High — High dùng màu `critical` | BR-147, BR-148 | UC-120 Normal Flow bước 2 |
| 3 | Chi tiết sự cố liên quan tới Service Caterers | Read-only detail | Chỉ hiển thị thông tin liên quan tới phạm vi Service Caterers (không toàn bộ hồ sơ y tế) | — | UC-120 Normal Flow bước 2 |
| 4 | Trạng thái Quarantine lô hàng liên quan | Badge màu `critical` | Hiện nếu lô hàng đang bị cách ly | BR-149 | — |
| 5 | Nút "Xuất Dữ liệu" (CSV/PDF) | Button | — | — | UC-120 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/chi tiết.
- Empty: Không có sự cố nào được gán.
- Error: `Alert variant="destructive"` MSG-023/MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/xuất báo cáo).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Corrective Actions Dashboard hoặc thông báo cảnh báo sự cố. Thoát: back về Dashboard.
**Bố cục**: Page Header + `Table` danh sách sự cố + panel chi tiết mở bên phải (Sheet) khi chọn 1 dòng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Incident Information Detail, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table danh sách sự cố (Mã sự cố, Ngày, Mức độ, Trạng thái), Sheet chi tiết bên phải, nút Xuất Dữ liệu.
Thành phần bắt buộc: badge mức độ nghiêm trọng (Low=info/warning, Medium=warning, High=critical), badge Quarantine.
Áp dụng Design System: xem 00_Foundation/Design_System.md (token critical riêng biệt với danger).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4595–4613 (UC-120 - View Incident Information)
- business_rules.md BR-146–BR-151, BR-154, BR-155
- error_message.md MSG-023
