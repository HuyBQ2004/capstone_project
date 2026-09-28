# Screen Spec — View Preparation Records Ledger

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình tổng hợp xuất ra sau khi hoàn tất Step 3 (`04_Service_Caterers_Screen_Flow.md`: `Step3 --> PrepLedger`), liên quan chặt tới UC-066 (Record Prepare Ingredient Information) và UC-052 (Manage Three-Step Food Inspection) | **Status**: Draft
**BR liên quan**: BR-076 (truy xuất lô nguyên liệu), BR-093, BR-099
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên sơ chế/sản xuất đã hoàn tất | Table (Web) / Card list (Mobile) | Cột: Mẻ, Món, Nhân viên chịu trách nhiệm, Ngày | — | Tổng hợp từ UC-066 + Step 1-3 |
| 2 | Liên kết số lô nguyên liệu đã dùng | Read-only link | Truy xuất nguồn gốc | BR-076 | — |
| 3 | Trạng thái đồng ký số của phiên | Badge | Đã ký / Chưa ký đủ | BR-099 | Kế thừa từ Step 3 |
| 4 | Bộ lọc theo ngày/món | Filter bar | Tuỳ chọn | — | — |
| 5 | Xem chi tiết 1 bản ghi | Điều hướng chi tiết (read-only, hiển thị lại nội dung 3 bước) | — | BR-093 | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách.
- Empty: Chưa có phiên sơ chế/sản xuất nào hoàn tất trong khoảng lọc.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem).

---

## Platform Layout — Web

**Entry/Exit points**: Từ cuối Step 3 (Inspect Meals Before Serving Screen) hoặc từ Three-Step Inspection Dashboard. Thoát: back về Dashboard.
**Bố cục**: Page Header + `Table` (Mẻ, Món, Nhân viên, Ngày, Trạng thái ký, Hành động Xem chi tiết) + filter bar.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Preparation Records Ledger, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + filter bar + Table (Mẻ, Món, Nhân viên, Ngày, Trạng thái ký, Xem chi tiết).
Thành phần bắt buộc: badge trạng thái đồng ký số.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Step3 --> PrepLedger)
- Report 3.md dòng 3221–3239 (UC-066), dòng 3487–3549 (UC-052/UC-062)
- business_rules.md BR-076, BR-093, BR-099
