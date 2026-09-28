# Screen Spec — View Inspection Records Log

**Actor**: Service Caterers | **UC liên quan**: UC-124 - View Inspection Records (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-091, BR-092, BR-093, BR-094, BR-096, BR-097, BR-098, BR-099
**MSG liên quan**: MSG-016 (Error), MSG-017 (Error), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách bản ghi kiểm tra (chỉ của chính Service Caterers) | Table (Web) / Card list (Mobile) | Cột: Ngày, Món ăn, Trạng thái (Passed/Failed) | BR-099 | UC-124 Normal Flow bước 4 |
| 2 | Bộ lọc theo ngày/món/trạng thái | Filter bar | Tuỳ chọn | — | UC-124 Normal Flow bước 3 |
| 3 | Xem chi tiết checklist, nhiệt độ, ảnh, chữ ký số | Điều hướng chi tiết (read-only) | — | BR-091–BR-099 | UC-124 Normal Flow bước 5 |
| 4 | Xem lịch sử Corrective Action liên quan (nếu Failed) | Link điều hướng | — | BR-097, BR-098 | UC-124 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách.
- Empty: Chưa có bản ghi kiểm tra nào trong khoảng lọc.
- Error: `Alert variant="destructive"` MSG-004/MSG-005/MSG-016/MSG-017 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard hoặc Corrective Actions Dashboard. Thoát: back về màn gọi.
**Bố cục**: Page Header + filter bar + `Table` (Ngày, Món ăn, Trạng thái badge, Hành động Xem chi tiết/Xem Corrective Action).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Inspection Records Log, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + filter bar + Table (Ngày, Món ăn, Trạng thái badge Passed/Failed, Hành động).
Thành phần bắt buộc: badge trạng thái Passed (success)/Failed (danger).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3641–3659 (UC-124 - View Inspection Records)
- business_rules.md BR-091–BR-099
- error_message.md MSG-016, MSG-017
