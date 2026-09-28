# Screen Spec — Record Preparation Information Form (Cooking Record)

**Actor**: Service Caterers | **UC liên quan**: UC-066 - Record Prepare Ingredient Information (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: Không có BR trực tiếp (Report 3 UC-066 ghi "Business Rules: None")
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Phiên sản xuất đang hoạt động | Read-only (chọn từ danh sách) | Bắt buộc chọn đúng phiên | — | UC-066 Normal Flow bước 1 |
| 2 | Hành động sơ chế đã thực hiện (rửa, khử trùng, gọt, cắt) | Checklist/Multi-select | Bắt buộc chọn ít nhất 1 hành động | — | UC-066 Normal Flow bước 2 |
| 3 | Nồng độ dung dịch khử trùng (nếu áp dụng) | Input số | Tuỳ chọn (chỉ hiện khi chọn "khử trùng") | — | UC-066 Normal Flow bước 3 |
| 4 | Nhân viên chịu trách nhiệm | Select (danh sách nhân viên) | Bắt buộc | — | UC-066 Normal Flow bước 3 |
| 5 | Nút "Lưu Ghi chép Sơ chế" | Button | Lưu, cập nhật audit trail | — | UC-066 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên sản xuất.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu lưu thất bại.
- Success: `Toast` xác nhận lưu ghi chép sơ chế thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ nhánh "Prepare Ingredients → Record Cooking Information" trước khi vào Three-Step Inspection Dashboard (`04_Service_Caterers_Screen_Flow.md`). Thoát: lưu xong tiếp tục sang Three-Step Inspection Dashboard.
**Bố cục**: Form ngắn trong `Dialog`, danh sách checklist hành động sơ chế + input nhân viên chịu trách nhiệm.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Preparation Information Form (Cooking Record), nền tảng Web, actor Service Caterers.
Bố cục: Dialog form ngắn gồm checklist hành động sơ chế, input nồng độ khử trùng (điều kiện), select nhân viên, nút Lưu Ghi chép Sơ chế.
Thành phần bắt buộc: không có cảnh báo BR đặc thù (UC-066 không có BR).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3221–3239 (UC-066 - Record Prepare Ingredient Information)
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Prepare Ingredients → Record Cooking Information)
