# Screen Spec — Receiving Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-050, BR-078
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lịch giao hàng hôm nay | Table (Web) / Card list (Mobile) | Mỗi dòng: Nhà cung cấp, Giờ dự kiến, Trạng thái | BR-078 | UC-044 Normal Flow bước 2 |
| 2 | Trạng thái giao hàng | Badge | Scheduled / Dispatched / In Progress / Completed | BR-078 | UC-044 Normal Flow bước 2 |
| 3 | Nút "Bắt đầu Nhận hàng" trên dòng Dispatched | Button | Chỉ hiện khi trạng thái = Dispatched; điều hướng Receive Ingredients Workspace | BR-050 | UC-044 Normal Flow bước 3 |
| 4 | Nút "Xem Lịch sử" | Button | Điều hướng Receiving History & Reports | — | ScreenFlow: `RecvDash -->|"View History"| RecvHist` |
| 5 | Bộ lọc lịch sử theo ngày/nhà cung cấp | Filter bar | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Skeleton` cho danh sách lịch giao hàng.
- Empty: Không có lịch giao hàng hôm nay → thông báo + gợi ý xem lịch sử.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/điều hướng).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Nhận hàng & Kho" trong Sidebar (`09_Service_Caterers_Screen_Flow_Web.md`). Điều hướng tiếp: Receive Ingredients Workspace, Receiving History & Reports.
**Bố cục**: Page Header + `Table` danh sách giao hàng hôm nay (cột: Nhà cung cấp, Giờ dự kiến, Trạng thái, Hành động) + link "Xem Lịch sử" góc trên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table danh sách giao hàng hôm nay (Nhà cung cấp, Giờ dự kiến, Trạng thái badge, nút Bắt đầu Nhận hàng) + link Xem Lịch sử.
Thành phần bắt buộc: badge trạng thái theo 4 giá trị (Scheduled/Dispatched/In Progress/Completed).
Áp dụng Design System: xem 00_Foundation/Design_System.md, Component_Guidelines.md (Table, Badge).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044 - Manage Ingredient Receiving)
- business_rules.md BR-050, BR-078
- `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md`
