# Tổng hợp Screen Specifications — ServiceCaterers (ServiceCaterers)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Service Caterers Dashboard

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng (màn hình điều hướng gốc) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp
**MSG liên quan**: Không áp dụng trực tiếp (trang tổng quan, không có thao tác ghi dữ liệu)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Lời chào + tên Service Caterers đang đăng nhập | Text | Lấy từ session người dùng | — | — |
| 2 | Grid Menu Items (8 mục điều hướng: Thực đơn, Nhận hàng, Kho, Kiểm tra 3 Bước, Lưu mẫu Thực phẩm, Phục vụ Bữa ăn, Sự cố & Hành động Khắc phục, Vi phạm) | Card/Button grid | Mỗi mục điều hướng tới đúng 1 Dashboard con | — | Cấu trúc lấy từ `04_Service_Caterers_Screen_Flow.md` (nhánh "Grid Menu Items") + `09_Service_Caterers_Screen_Flow_Web.md` (Sidebar 2 cấp) |
| 3 | Widget tóm tắt hoạt động hôm nay (số phiên nhận hàng, số bước kiểm tra đang chờ, số cảnh báo dị ứng đang hoạt động) | Card thống kê | Cập nhật theo BR-180 (dashboard làm mới trong 5 phút) nếu áp dụng | BR-180 | **[Giả định — cần team xác nhận]** nội dung widget cụ thể chưa có mô tả field-by-field gốc trong Report 3, chỉ suy ra từ vai trò tổng quan của 1 trang Dashboard |

### Trạng thái màn hình
- Loading: Skeleton cho grid + widget thống kê trong lúc tải dữ liệu tổng quan.
- Empty: Không áp dụng (grid điều hướng luôn hiển thị đủ 8 mục cố định).
- Error: `Alert variant="destructive"` nếu tải widget thống kê thất bại, kèm nút "Thử lại" — không chặn việc dùng Grid Menu Items.
- Success: Không áp dụng (không có thao tác ghi).

---

## Platform Layout — Web

**Entry/Exit points**: Vào ngay sau khi đăng nhập thành công qua Common Entry Flow (`13_Guest_Common_Screen_Flow.md`); là màn hình gốc của Sidebar 2 cấp (`09_Service_Caterers_Screen_Flow_Web.md`). Thoát: chọn 1 nhóm trong Sidebar để vào Dashboard con tương ứng (Menu Management, Receiving, Inventory, Three-Step Inspection, Food Sample Retention, Meal Service, Corrective Actions/Violation).
**Bố cục**: Theo `00_Foundation/Layout_Guidelines.md` mục 1.1 — Topbar (Logo/Breadcrumb/Notification/Avatar) + Sidebar cố định trái (menu theo `09_Service_Caterers_Screen_Flow_Web.md` mục "Sidebar 2 cấp") + Content Area dùng grid 12 cột: widget thống kê hàng trên (`col-span-4` mỗi thẻ), Grid Menu Items hàng dưới dạng `Card` lớn (`col-span-3`/`col-span-4` tuỳ số lượng, responsive theo `00_Foundation/Responsive_Guidelines.md`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Service Caterers Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Topbar (logo, breadcrumb, notification, avatar) + Sidebar trái 2 cấp (Thực đơn, Nhận hàng & Kho, Kiểm tra 3 Bước, Lưu mẫu Thực phẩm, Phục vụ Bữa ăn, Sự cố & Vi phạm, Cá nhân) + Content Area.
Thành phần bắt buộc: lời chào người dùng, widget thống kê hoạt động hôm nay, grid 8 mục điều hướng module chính.
Áp dụng Design System: màu primary #0E7490, font Be Vietnam Pro, card bo góc 8px, badge trạng thái theo token success/warning/danger/critical/info (xem 00_Foundation/Design_System.md).
Trạng thái cần thể hiện: loading (skeleton widget), error (alert khi widget lỗi).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` (Sidebar 2 cấp, sơ đồ điều hướng gốc)
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (Grid Menu Items)
- `02_Requirements/00_BusinessRules/business_rules.md` BR-180 (widget thống kê, giả định tham chiếu)

# Screen Spec — Menu Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-048 - Manage Menus (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-067
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách thực đơn theo tuần | Table (Web) / Card list (Mobile) | Mỗi dòng: Tuần áp dụng, Trạng thái, Ngày cập nhật cuối | — | UC-048 Normal Flow bước 2 |
| 2 | Trạng thái thực đơn | Badge | Draft / Pending Approval / Approved / Published / Rejected | BR-067, BR-068 | Màu theo `Design_System.md` mục 3 (`info`=Draft, `warning`=Pending, `success`=Approved/Published, `danger`=Rejected) |
| 3 | Bộ lọc theo khoảng ngày | DateRangePicker | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 4 | Bộ lọc theo trạng thái | Select | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 5 | Nút "Tạo Thực đơn Mới" | Button | Điều hướng tới Create Menu Workspace (UC-020) | — | — |
| 6 | Hành động "Edit" trên dòng Draft/Rejected | Button | Chỉ hiện khi status = Draft hoặc Rejected (BR-067); điều hướng Update Menu Screen (UC-096) | BR-067 | UC-048 Normal Flow bước 4 |
| 7 | Hành động "Submit" trên dòng Draft | Button | Chỉ hiện khi status = Draft; điều hướng Submit Menu For Approval Trigger (UC-087) | BR-067 | UC-048 Normal Flow bước 4 |
| 8 | Hành động "Delete" trên dòng Draft/Rejected | Button (destructive) + `AlertDialog` xác nhận | Chỉ hiện khi status = Draft hoặc Rejected; chặn nếu Approved/Published | BR-067 | UC-048 Exceptions: chặn thao tác + hiển thị cảnh báo nếu cố xoá/sửa thực đơn đã duyệt |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách khi tải.
- Empty: Chưa có thực đơn nào → hiển thị illustration + nút "Tạo Thực đơn Mới".
- Error: `Alert variant="destructive"` khi tải danh sách thất bại (MSG-004/MSG-005 nếu do session/quyền), kèm nút "Thử lại".
- Success: Toast xác nhận sau khi Delete thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Thực đơn" trong Sidebar (`09_Service_Caterers_Screen_Flow_Web.md`). Điều hướng tiếp: Create Menu Workspace, Update Menu, Submit Menu For Approval.
**Bố cục**: Page Header (tiêu đề "Quản lý Thực đơn" + nút "Tạo Thực đơn Mới" góc phải) + thanh filter (ngày, trạng thái) phía trên `Table` + `Table` dữ liệu (cột: Tuần, Trạng thái, Ngày cập nhật, Hành động) kèm phân trang dưới cùng, theo `00_Foundation/Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header (tiêu đề + nút Tạo Thực đơn Mới) + thanh filter (ngày, trạng thái) + Table dữ liệu (Tuần, Trạng thái, Ngày cập nhật, Hành động Edit/Submit/Delete) + phân trang.
Thành phần bắt buộc: badge trạng thái theo màu ngữ nghĩa, action buttons theo điều kiện trạng thái (BR-067).
Áp dụng Design System: xem 00_Foundation/Design_System.md, Component_Guidelines.md (Table, Badge, AlertDialog).
Trạng thái cần thể hiện: loading, empty, error, success (toast xoá thành công).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2867–2886 (UC-048 - Manage Menus)
- business_rules.md BR-067, BR-068
- traceability_matrix.md

# Screen Spec — Create Menu Workspace

**Actor**: Service Caterers | **UC liên quan**: UC-020 - Create Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-051 (Warning — xung đột dị ứng), MSG-071 (Success — lưu nháp), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tuần áp dụng / Ngày phục vụ | DatePicker | Bắt buộc; phải cách ngày nộp phê duyệt tối thiểu 3 ngày để kịp BR-061 | BR-061 | UC-020 Normal Flow bước 2 |
| 2 | Bữa ăn trong ngày (Meal Slot) | Select (Sáng/Trưa/Chiều) | Bắt buộc | — | UC-020 Normal Flow bước 2 |
| 3 | Chọn món ăn (Dish Selection) | Multi-select/Table chọn món | Bắt buộc ít nhất 1 món/bữa | BR-068 (giới hạn lặp món) | UC-020 Normal Flow bước 3 |
| 4 | Khai báo nguyên liệu + chất gây dị ứng từng món | Textarea/Tag input theo món | Bắt buộc đầy đủ trước khi lưu | BR-069 | UC-020 Postconditions |
| 5 | Mục tiêu dinh dưỡng (calo, nhóm chất) | Form nhập số + hiển thị % đạt chỉ tiêu | Phải đáp ứng chỉ tiêu theo nhóm tuổi | BR-062 | UC-020 Normal Flow bước 3 |
| 6 | Nút "AI Assist" (Gemini AI) | Button | Điều hướng/mở panel View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-020 Alternative Flow AF1 |
| 7 | Kết quả kiểm tra xung đột dị ứng | Alert banner | Tự động chạy khi lưu/submit; hiển thị MSG-051 nếu có xung đột | BR-063 | UC-020 Normal Flow bước 4 |
| 8 | Nút "Lưu nháp" | Button | Lưu trạng thái Draft, hiển thị MSG-071 | — | UC-020 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Skeleton` cho form khi tải danh sách món ăn/kho.
- Empty: Không áp dụng (form luôn hiển thị đủ field).
- Error: `Alert variant="destructive"` (MSG-004/MSG-005) nếu lưu thất bại.
- Success: `Toast` MSG-071 khi lưu nháp thành công; `Alert variant="warning"` (không tự ẩn) hiển thị MSG-051 nếu phát hiện xung đột dị ứng — không chặn lưu Draft nhưng chặn Submit sang phê duyệt (UC-087).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Menu Management Dashboard → nút "Tạo Thực đơn Mới" (`09_Service_Caterers_Screen_Flow_Web.md`). Thoát: "Lưu nháp" quay lại Menu Management Dashboard, hoặc mở AI Panel (không rời trang, dạng Dialog/Sheet).
**Bố cục**: Form dài chia `Stepper` ngang (Thông tin chung → Chọn món & dinh dưỡng → Xác nhận dị ứng) theo `00_Foundation/Layout_Guidelines.md` mục 1.2; panel AI Recommendations mở dạng `Sheet` trượt từ phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Menu Workspace, nền tảng Web, actor Service Caterers.
Bố cục: Stepper ngang 3 bước (Thông tin chung, Chọn món & dinh dưỡng, Xác nhận dị ứng), nút AI Assist nổi bật, nút Lưu nháp cố định cuối form.
Thành phần bắt buộc: DatePicker, Select bữa ăn, bảng chọn món, tag input nguyên liệu/dị ứng, banner cảnh báo dị ứng MSG-051.
Áp dụng Design System: warning banner dùng token `warning`, alert dị ứng dùng token `danger` (xem Design_System.md mục 3, BR-123 tinh thần màu đỏ dị ứng).
Trạng thái cần thể hiện: loading, error, success (toast lưu nháp), warning (banner xung đột dị ứng).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2823–2841 (UC-020 - Create Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
- error_message.md MSG-051, MSG-071

# Screen Spec — View Menu Recommendations Panel (AI Integration)

**Actor**: Service Caterers | **UC liên quan**: UC-075 - Request AI Menu Recommendations (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-064, BR-080, BR-156, BR-157 — **Lưu ý mâu thuẫn nguồn**: bảng Business Rules của UC-075 trong `Report 3.md` (dòng 2905) trích "BR-072", nhưng theo `business_rules.md` (nguồn BR chính thức đã chốt, xem `00_UI_Design_Documentation_Plan.md` mục 2.4) BR-072 là "Chụp ảnh bằng chứng bắt buộc khi nhận hàng" (Ingredient Receiving), không liên quan trần chi phí thực đơn. Quy tắc "trần chi phí" đúng trong `business_rules.md` là **BR-080**. Screen Spec này dùng BR-080 (nguồn đúng), không dùng BR-072 (trích dẫn lỗi thời của Report 3).
**MSG liên quan**: MSG-073 (Warning — AI không khả dụng), MSG-051 (Warning — cờ dị ứng trong đề xuất)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ngữ cảnh gửi AI (tồn kho, mục tiêu dinh dưỡng, hồ sơ dị ứng, trần chi phí) | Hiển thị tóm tắt read-only | Tự động lấy từ hệ thống, không cho sửa tay ở panel này | BR-062, BR-064 | UC-075 Normal Flow bước 2 |
| 2 | Danh sách Option Card đề xuất món ăn | Card list | Mỗi card: tên món, phân tích dinh dưỡng, danh sách nguyên liệu, cờ cảnh báo dị ứng | BR-156 | UC-075 Normal Flow bước 4; qua hard-filter dị ứng trước khi hiển thị (ghi chú ScreenFlow) |
| 3 | Cờ cảnh báo dị ứng trên từng Option Card | Badge màu `danger` | Hiện khi món chứa allergen trùng hồ sơ học sinh | BR-063, BR-156 | — |
| 4 | Nút "Áp dụng" trên từng Option Card | Button | Áp dụng gợi ý vào Create Menu Workspace đang mở | — | ScreenFlow: `AIPanel -->|"Apply Option"| CreateMenuWS` |
| 5 | Thông báo dịch vụ AI không khả dụng | Alert banner | Hiện khi Gemini AI Service không phản hồi | — | UC-075 Alternative Flow AF1, MSG-073 |
| 6 | Ghi chú lưu trữ hội thoại AI | Text nhỏ cuối panel | Thông báo "Cuộc trao đổi này được lưu trữ để phục vụ tuân thủ" | BR-157 | UC-075 Postconditions |

### Trạng thái màn hình
- Loading: `Skeleton` cho Option Card trong lúc chờ Gemini AI phản hồi.
- Empty: Không có gợi ý phù hợp → thông báo + gợi ý điều chỉnh mục tiêu dinh dưỡng/chi phí.
- Error: `Alert variant="destructive"` MSG-073 khi AI Service không khả dụng, kèm nút "Thử lại" và lối "Tạo thủ công" quay về Create Menu Workspace.
- Success: Không cần toast riêng — áp dụng option cập nhật trực tiếp form Create Menu Workspace.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Create Menu Workspace (nút "AI Assist") dạng `Sheet` trượt từ phải, không rời trang chính. Thoát: "Áp dụng" (đóng panel, cập nhật form) hoặc đóng panel không áp dụng gì.
**Bố cục**: `Sheet` chiều rộng ~480px, header tóm tắt ngữ cảnh gửi AI, danh sách Option Card cuộn dọc bên dưới, mỗi card có nút "Áp dụng" góc phải dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Menu Recommendations Panel (AI Integration), nền tảng Web, actor Service Caterers.
Bố cục: Sheet trượt từ phải, header tóm tắt ngữ cảnh AI, danh sách Option Card (tên món, dinh dưỡng, nguyên liệu, cờ dị ứng, nút Áp dụng).
Thành phần bắt buộc: badge cảnh báo dị ứng màu đỏ (token danger), ghi chú lưu trữ hội thoại AI.
Áp dụng Design System: xem 00_Foundation/Design_System.md (Alert, Badge), ADR-003 (pipeline AI + hard-filter dị ứng).
Trạng thái cần thể hiện: loading, empty, error (MSG-073), thành công khi áp dụng option.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2889–2907 (UC-075 - Request AI Menu Recommendations)
- business_rules.md BR-064, BR-156, BR-157, BR-063
- error_message.md MSG-073, MSG-051
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (ghi chú "kết quả AI luôn đi qua hard-filter dị ứng", ADR-003)

# Screen Spec — Submit Menu For Approval Trigger

**Actor**: Service Caterers | **UC liên quan**: UC-087 - Submit Menu For Approval (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-063, BR-069
**MSG liên quan**: MSG-051 (Warning/Error chặn — xung đột dị ứng), MSG-039 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt thực đơn (Menu Summary) | Read-only card | Hiển thị tuần, danh sách món, tổng dinh dưỡng | — | UC-087 Normal Flow bước 2 |
| 2 | Checklist kiểm tra trước khi nộp (Pre-submission validation checklist) | Checklist read-only tự động đánh dấu | Xác nhận đã khai báo đầy đủ nguyên liệu (BR-069), nộp tối thiểu 3 ngày trước tuần dùng (BR-061) | BR-061, BR-069 | UC-087 Normal Flow bước 2–3 |
| 3 | Kết quả kiểm tra xung đột dị ứng cuối cùng | Alert banner | Chạy lại lần cuối trước khi đổi trạng thái | BR-063 | UC-087 Normal Flow bước 4, MSG-051 |
| 4 | Nút "Xác nhận Nộp Phê duyệt" | Button (primary) | Bị khoá/disabled nếu còn xung đột dị ứng nghiêm trọng chưa xử lý | BR-063 | UC-087 Exceptions: chặn submit nếu phát hiện xung đột nghiêm trọng |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải tóm tắt thực đơn.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu submit thất bại; MSG-051 dạng chặn (blocking) nếu còn xung đột dị ứng nghiêm trọng — nút "Xác nhận Nộp" chuyển `disabled` kèm tooltip lý do.
- Success: `Toast` MSG-039, chuyển trạng thái thực đơn sang "Pending Approval", quay về Menu Management Dashboard.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Submit" trên dòng Draft) hoặc từ cuối Create Menu Workspace (`09_Service_Caterers_Screen_Flow_Web.md`: `SubmitMenu -.-> SchoolAdminMenu`). Thoát: sau khi xác nhận, chuyển giao cross-actor sang School Administration (Web) để phê duyệt.
**Bố cục**: `Dialog` xác nhận giữa màn hình (form ngắn, theo `Layout_Guidelines.md` mục 1.2) chứa tóm tắt + checklist + banner cảnh báo + nút hành động.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Menu For Approval Trigger, nền tảng Web, actor Service Caterers.
Bố cục: Dialog giữa màn hình gồm tóm tắt thực đơn, checklist tự động, banner cảnh báo dị ứng, nút Xác nhận Nộp Phê duyệt.
Thành phần bắt buộc: checklist trạng thái đạt/chưa đạt, banner MSG-051 nếu có xung đột, nút hành động disabled khi bị chặn.
Áp dụng Design System: banner cảnh báo dùng token `danger` khi chặn, `warning` khi chỉ cảnh báo không chặn (xem Design_System.md).
Trạng thái cần thể hiện: loading, error (chặn submit), success (MSG-039).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2911–2929 (UC-087 - Submit Menu For Approval)
- business_rules.md BR-061, BR-063, BR-069
- error_message.md MSG-051, MSG-039

# Screen Spec — Update Menu Screen

**Actor**: Service Caterers | **UC liên quan**: UC-096 - Update Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-067, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-074 (Success), MSG-051 (Warning), MSG-075 (Warning — giới hạn lặp món), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn (không tự suy diễn cách xử lý)**: `Report 3.md` (UC-096 Business Rules, dòng 2949) trích thêm **BR-072, BR-074**, nhưng theo `business_rules.md` hiện hành: BR-072 = "Chụp ảnh bằng chứng bắt buộc khi nhận hàng" và BR-074 = "Xác minh vị trí nhận hàng" — cả hai đều thuộc module Ingredient Receiving, không liên quan tới sửa thực đơn. Đây là trích dẫn lỗi thời của Report 3 (đã ghi nhận cơ chế tương tự ở `00_UI_Design_Documentation_Plan.md` mục 2.4) — **không đưa 2 mã này vào Screen Spec**. Riêng quy tắc "giới hạn món chính lặp lại tối đa 2 lần/tuần" mà UC-096 và `error_message.md` MSG-075 đều gọi là "BR-068" **không khớp** với nội dung BR-068 hiện tại trong `business_rules.md` (Quy trình phê duyệt thực đơn) — đây là mâu thuẫn đã biết nhưng **chưa có mã BR chính thức thay thế cho giới hạn dish rotation**. **[Giả định — cần team xác nhận]**: giữ MSG-075 làm nguồn cảnh báo hành vi (nội dung message không đổi), nhưng không gán số BR cụ thể cho đến khi team bổ sung/sửa `business_rules.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Form chỉnh sửa thực đơn (pre-filled) | Form | Chỉ cho phép khi status = Draft hoặc Rejected | BR-067 | UC-096 Preconditions |
| 2 | Bình luận từ chối (nếu status = Rejected) | Read-only text block | Hiển thị lý do từ chối gần nhất từ School Administration | — | UC-096 Normal Flow bước 2 — nguồn field cụ thể (lý do từ chối) hiện chưa có UC/BR riêng mô tả cấu trúc lưu trữ, **[Giả định — cần team xác nhận]** cho định dạng hiển thị |
| 3 | Ngày phục vụ / Chọn món | DatePicker + Multi-select | Cùng ràng buộc như Create Menu Workspace | BR-061, BR-062 | UC-096 Normal Flow bước 3 |
| 4 | Cảnh báo giới hạn lặp món chính | Alert banner | Hiện khi 1 món chính được xếp lịch quá 2 lần/tuần | *(xem lưu ý mâu thuẫn nguồn ở trên)* | MSG-075 |
| 5 | Kiểm tra khai báo nguyên liệu | Validation tự động | Bắt buộc đầy đủ trước khi lưu | BR-069 | UC-096 Normal Flow bước 4 |
| 6 | Kết quả kiểm tra xung đột dị ứng | Alert banner | Tự động chạy lại sau khi sửa | BR-063 | UC-096 Normal Flow bước 5, MSG-051 |
| 7 | Nút "AI Assist" thay món xung đột | Button | Mở View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-096 Alternative Flow AF1 |
| 8 | Nút "Lưu thay đổi" | Button | Lưu, hiển thị MSG-074 | — | UC-096 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải form pre-filled.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu sửa/lưu thất bại; chặn hoàn toàn nếu cố sửa menu đã Approved/Published (BR-067).
- Success: `Toast` MSG-074 khi lưu thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Edit" trên dòng Draft/Rejected — `09_Service_Caterers_Screen_Flow_Web.md`). Thoát: "Lưu thay đổi" quay lại Menu Management Dashboard.
**Bố cục**: Form dài dạng `Stepper` ngang giống Create Menu Workspace (tái dùng bố cục), pre-filled dữ liệu hiện có, banner bình luận từ chối hiển thị đầu trang nếu status = Rejected.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Menu Screen, nền tảng Web, actor Service Caterers.
Bố cục: giống Create Menu Workspace (Stepper ngang) nhưng pre-filled dữ liệu, thêm banner bình luận từ chối ở đầu trang nếu có.
Thành phần bắt buộc: banner cảnh báo giới hạn lặp món (MSG-075), banner xung đột dị ứng (MSG-051), nút AI Assist, nút Lưu thay đổi.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn sửa menu đã khoá), success (MSG-074).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2933–2949 (UC-096 - Update Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-067, BR-069, BR-156, BR-157
- error_message.md MSG-074, MSG-051, MSG-075

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

# Screen Spec — Receive Ingredients Workspace (Start Receiving Session)

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (khởi tạo phiên), UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071, BR-078, BR-079, BR-080
**MSG liên quan**: MSG-015 (Error — GPS ngoài khu vực), MSG-041 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin phiên nhận hàng (nhà cung cấp, giờ dự kiến) | Read-only header | Lấy từ Receiving Management Dashboard | BR-078 | UC-060 Trigger |
| 2 | Xác minh trạng thái Approved của nhà cung cấp | Kiểm tra tự động (block nếu không Approved) | **Bắt buộc kết nối mạng — không cho phép offline** | BR-070, BR-197 | UC-044 Postconditions ngầm định qua BR-070; UI Guidelines mục 5.2 |
| 3 | Xác minh GPS trong bán kính cho phép | Kiểm tra tự động | Trong 50m khu vực cổng trường; ngoài dung sai hiển thị cảnh báo `warning` cho phép xác nhận thủ công kèm lý do | BR-079 | UC-060 Normal Flow bước 2, MSG-015 |
| 4 | Xác thực khuôn mặt nhân viên giao hàng | Face scan trigger → điều hướng Verify Supplier Delivery Panel | **Bắt buộc online** | BR-050, BR-197 | UC-060 Normal Flow bước 3 |
| 5 | Danh sách hành động con của phiên (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) | Danh sách bước / Tab | Điều hướng tới các màn con tương ứng | BR-071, BR-080 | ScreenFlow: `RecvWS` branches |
| 6 | Nút "Hoàn tất Phiên Nhận hàng" | Button | Chỉ bật khi đã hoàn tất các bước bắt buộc | — | UC-060 Postconditions |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực) dạng `Alert variant="destructive"`; chặn hoàn toàn nếu nhà cung cấp không Approved (BR-070/BR-197, không cho qua bằng cache).
- Success: `Toast` MSG-041 sau khi hoàn tất phiên.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Receiving Management Dashboard (nút "Bắt đầu Nhận hàng"). Điều hướng tiếp: Verify Supplier Delivery Panel, Capture Receiving Evidence Screen, Reject Ingredient Batch Form, Record Ingredient Receiving.
**Bố cục**: Layout dạng Wizard theo `Tabs` ngang (Xác minh → Kiểm tra chất lượng → Bằng chứng → Xác nhận), mỗi tab là 1 khối nội dung trong cùng trang; GPS xác nhận qua Geolocation API trình duyệt + nhập/xác nhận tọa độ thủ công (theo `09_Service_Caterers_Screen_Flow_Web.md`: "Web thay bằng upload file + xác nhận tọa độ thủ công").

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receive Ingredients Workspace, nền tảng Web, actor Service Caterers.
Bố cục: Tabs ngang (Xác minh, Kiểm tra chất lượng, Bằng chứng, Xác nhận), header hiển thị thông tin phiên nhận hàng.
Thành phần bắt buộc: banner xác minh trạng thái Approved nhà cung cấp, banner GPS, khối xác thực khuôn mặt, nút Hoàn tất Phiên.
Áp dụng Design System: banner chặn dùng token `danger` (MSG-015/MSG-077 nếu mất kết nối), xem Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn do GPS/trạng thái NCC), success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044), dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071, BR-078, BR-079, BR-080, BR-197
- error_message.md MSG-015, MSG-041, MSG-077
- `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` (khác biệt GPS/camera Web vs Mobile)

# Screen Spec — Verify Supplier Delivery Panel

**Actor**: Service Caterers | **UC liên quan**: UC-060 - Record Ingredient Receiving (Report 3 §3.2, sub-bước "Verify Invoice") | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071
**MSG liên quan**: MSG-013 (Error — chứng nhận ATTP hết hạn), MSG-014 (Error — xác thực khuôn mặt thất bại), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin nhà cung cấp (tên, trạng thái Approved) | Read-only card | Chặn nếu không Approved | BR-070 | UC-044/UC-060 |
| 2 | Danh sách chứng nhận lô hàng (số lô, ngày sản xuất, hạn dùng, số lượng, chứng chỉ) | Read-only table/list | Bắt buộc đầy đủ | BR-071 | UC-060 Normal Flow bước 4 |
| 3 | Kết quả xác thực khuôn mặt nhân viên giao hàng | Trạng thái (Passed/Failed) | Nếu Failed → MSG-014, chuyển luồng "Manual Override" (nhập lý do + ảnh bằng chứng) | BR-050 | UC-060 Normal Flow bước 3; `UI_Design_Guidelines.md` mục 3.4, `ADR-002.md` |
| 4 | Nút "Xác nhận Hồ sơ Hợp lệ" | Button | Điều hướng tiếp sang Inspect Quality/Capture Evidence | — | — |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin chứng nhận.
- Empty: Không áp dụng.
- Error: MSG-013 nếu chứng nhận ATTP hết hạn (chặn xác nhận); MSG-014 nếu xác thực khuôn mặt thất bại → hiện luồng Manual Override bắt buộc nhập lý do + ảnh bằng chứng.
- Success: Chuyển sang bước tiếp theo trong Receive Ingredients Workspace.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (tab "Xác minh"). Thoát: xác nhận hợp lệ → quay lại Workspace, chuyển tab kế tiếp.
**Bố cục**: Panel/Dialog trong Workspace, 2 khối: (1) thông tin nhà cung cấp + chứng nhận dạng bảng, (2) khối xác thực khuôn mặt hiển thị kết quả webcam-based (thứ yếu, khuyến nghị dùng Mobile tại hiện trường theo `09_Service_Caterers_Screen_Flow_Web.md`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Verify Supplier Delivery Panel, nền tảng Web, actor Service Caterers.
Bố cục: Panel trong Workspace gồm thông tin nhà cung cấp, bảng chứng nhận lô hàng, khối kết quả xác thực khuôn mặt, nút Xác nhận Hồ sơ Hợp lệ.
Thành phần bắt buộc: badge trạng thái Approved, cảnh báo MSG-013/MSG-014 khi lỗi.
Áp dụng Design System: xem 00_Foundation/Design_System.md, ADR-002.md (Face Biometric).
Trạng thái cần thể hiện: loading, error (MSG-013/MSG-014 + Manual Override), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071
- error_message.md MSG-013, MSG-014
- `03_Design/02_ADR/ADR-002.md` (Face Biometric, Manual Override)

# Screen Spec — Capture Receiving Evidence Screen

**Actor**: Service Caterers | **UC liên quan**: bundled UC-044 (theo `14_Screen_Authorization_Matrix.md`), chi tiết hoá từ UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-072, BR-073, BR-074, BR-079
**MSG liên quan**: MSG-008 (Error — vượt dung lượng file), MSG-009 (Error — sai định dạng file), MSG-015 (Error — GPS ngoài khu vực), MSG-041 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ảnh bằng chứng nguyên liệu, nhãn, bao bì, tài liệu | Upload nhiều ảnh + preview thumbnail | Bắt buộc tối thiểu 1 ảnh; định dạng PDF/JPG/PNG, tối đa dung lượng theo `Component_Guidelines.md` (TODO #11: 5MB vs 10MB — dùng ngưỡng MSG-008 hiện hành 10MB cho tới khi team chốt) | BR-072 | UC-060 Normal Flow bước 5 |
| 2 | Toạ độ GPS + timestamp | Tự động ghi nhận, hiển thị read-only | Không cho sửa tay; xác minh trong khu vực nhận hàng cho phép | BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 2/5 |
| 3 | Thông tin thiết bị (device info) | Tự động ghi nhận (ẩn, chỉ log audit) | — | BR-073 | Biện pháp chống gian lận |
| 4 | Nút "Lưu Bằng chứng" | Button | Lưu, hiển thị MSG-041 | — | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho vùng preview ảnh khi đang tải lên.
- Empty: Chưa chụp/tải ảnh nào → placeholder khung ảnh + hướng dẫn.
- Error: MSG-008/MSG-009 khi ảnh không hợp lệ; MSG-015 nếu GPS ngoài khu vực cho phép (cho phép xác nhận thủ công kèm lý do, ghi audit log theo `UI_Design_Guidelines.md` mục 3.3).
- Success: `Toast` MSG-041.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (tab "Bằng chứng"). Thoát: "Lưu Bằng chứng" → quay lại Workspace.
**Bố cục**: Panel upload file (`Input type="file"` + preview thumbnail) thay cho chụp ảnh trực tiếp (không có camera), kèm ô nhập/xác nhận tọa độ GPS thủ công hoặc qua Geolocation API trình duyệt — đánh dấu rõ là **"secondary path", độ tin cậy chống gian lận thấp hơn Mobile**, theo `09_Service_Caterers_Screen_Flow_Web.md` mục "Khác biệt so với bản Mobile". Offline không áp dụng trên Web (BR-197 luôn yêu cầu kết nối).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Capture Receiving Evidence Screen, nền tảng Web, actor Service Caterers.
Bố cục: Panel upload nhiều ảnh (input file + preview thumbnail lưới), khối tọa độ GPS + timestamp read-only, nút Lưu Bằng chứng.
Thành phần bắt buộc: cảnh báo dung lượng/định dạng file (MSG-008/MSG-009), cảnh báo GPS ngoài khu vực (MSG-015).
Áp dụng Design System: ảnh dùng aspect-ratio cố định theo Responsive_Guidelines.md mục 4.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-072, BR-073, BR-074, BR-079
- error_message.md MSG-008, MSG-009, MSG-015, MSG-041
- `14_Screen_Authorization_Matrix.md` (ghi chú "bundled UC-044")

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

# Screen Spec — Reject Ingredient Batch Form

**Actor**: Service Caterers | **UC liên quan**: UC-072 - Reject Ingredient Batch (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-078 (lô nguyên liệu không đạt bị từ chối)
**MSG liên quan**: MSG-015 (Error — GPS, dùng chung ngữ cảnh phiên nhận hàng), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: `Report 3.md` UC-072 chỉ liệt kê `MSG-004, MSG-005, MSG-015` ở mục Exceptions, không có mã Success riêng cho việc từ chối lô hàng — Screen Spec này **không tự đặt tên MSG mới**, dùng thông báo xác nhận chung dạng Toast không gắn mã MSG cụ thể. **[Giả định — cần team xác nhận]** nếu cần bổ sung MSG riêng cho "Reject thành công".

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin lô hàng đang kiểm tra | Read-only card | Lấy từ phiên nhận hàng đang hoạt động | — | UC-072 Preconditions |
| 2 | Lý do từ chối (mã lý do bắt buộc) | Select (multi-choice) | Bắt buộc chọn ít nhất 1 lý do | BR-078 | UC-072 Normal Flow bước 3–4 |
| 3 | Ảnh bằng chứng lô hàng bị từ chối | Upload ảnh | Khuyến nghị bắt buộc (đồng bộ tinh thần BR-078 "ảnh bằng chứng") | BR-078 | UC-072 Postconditions |
| 4 | Tuỳ chọn "Từ chối một phần" (Partial Rejection) | Checkbox + input số lượng | Khi bật: tạo 2 bản ghi batch riêng (phần đạt/phần không đạt) | — | UC-072 Alternative Flow AF1 |
| 5 | Nút "Xác nhận Từ chối" | Button (destructive) + `AlertDialog` xác nhận | Không thể hoàn tác | — | UC-072 Normal Flow bước 4–5 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin lô hàng.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005/MSG-015 nếu thao tác thất bại.
- Success: `Toast` xác nhận batch đã chuyển trạng thái "Rejected", loại khỏi kho, thông báo đã gửi nhà cung cấp.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (tab/bước "Log Rejections") khi phát hiện lô hàng không đạt. Thoát: xác nhận xong quay lại Workspace.
**Bố cục**: `Dialog` form ngắn — thông tin lô hàng, checklist lý do từ chối, upload ảnh, nút xác nhận (destructive variant).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Reject Ingredient Batch Form, nền tảng Web, actor Service Caterers.
Bố cục: Dialog gồm thông tin lô hàng, danh sách lý do từ chối (multi-select), upload ảnh bằng chứng, tuỳ chọn từ chối một phần, nút Xác nhận Từ chối (destructive) kèm AlertDialog xác nhận.
Thành phần bắt buộc: nút hành động dùng variant destructive theo Component_Guidelines.md.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3243–3257 (UC-072 - Reject Ingredient Batch)
- business_rules.md BR-078
- error_message.md MSG-015

# Screen Spec — Record Ingredient Receiving

**Actor**: Service Caterers | **UC liên quan**: UC-060 - Record Ingredient Receiving (Report 3 §3.2, bước cuối "Send the Receipt of Goods") | **Status**: Draft
**BR liên quan**: BR-050, BR-071, BR-073, BR-074, BR-076, BR-079, BR-080
**MSG liên quan**: MSG-041 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt toàn bộ phiên nhận hàng (nhà cung cấp, chứng nhận, kết quả kiểm tra, ảnh bằng chứng, GPS/timestamp) | Read-only summary | Tổng hợp từ các bước trước trong Workspace | BR-071, BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 6 |
| 2 | Kiểm tra nhiệt độ chuỗi lạnh (cold chain) | Read-only kết quả | Cảnh báo nếu > 5°C tại bước kiểm tra chất lượng | BR-080 | UC-060 Alternative Flow AF1 |
| 3 | Số lô liên kết truy xuất nguồn gốc | Read-only text | Ghi nhận số lô gắn với thực đơn/phiên sản xuất | BR-076 | UI_Design_Guidelines nguồn BR-076 |
| 4 | Nút "Xác nhận Hoàn tất Phiên Nhận hàng" | Button (primary) | Tạo bản ghi batch traceability, cập nhật kho | — | UC-060 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tổng hợp dữ liệu phiên.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu lưu thất bại.
- Success: `Toast` MSG-041 — "Nhận hàng và cập nhật kho thành công".

---

## Platform Layout — Web

**Entry/Exit points**: Bước cuối trong Receive Ingredients Workspace (tab "Xác nhận"). Thoát: sau khi xác nhận, điều hướng tự động sang Store Ingredients Form.
**Bố cục**: Trang tóm tắt dạng `Card` lớn theo từng nhóm dữ liệu (Nhà cung cấp, Chứng nhận, Kết quả kiểm tra, Bằng chứng), nút xác nhận cố định cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Ingredient Receiving, nền tảng Web, actor Service Caterers.
Bố cục: trang tóm tắt nhiều Card (Nhà cung cấp, Chứng nhận, Kết quả kiểm tra, Bằng chứng, GPS/timestamp), nút Xác nhận Hoàn tất Phiên Nhận hàng.
Thành phần bắt buộc: cảnh báo cold chain nếu vượt ngưỡng nhiệt độ.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-071, BR-073, BR-074, BR-076, BR-079, BR-080
- error_message.md MSG-041

# Screen Spec — Receiving History & Reports

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (Report 3 §3.2, sub-flow "search/filter past receiving logs") | **Status**: Draft
**BR liên quan**: BR-076, BR-175, BR-176, BR-178
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lịch sử nhận hàng | Table (Web) / Card list (Mobile) | Cột: Ngày, Nhà cung cấp, Số lô, Trạng thái (Received/Rejected) | BR-076 | UC-044 Normal Flow bước 4 |
| 2 | Bộ lọc theo ngày/nhà cung cấp | Filter bar | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |
| 3 | Xem chi tiết 1 bản ghi lịch sử | Điều hướng chi tiết (mở lại Record Ingredient Receiving dạng read-only) | — | — | Truy xuất nguồn gốc BR-076 |
| 4 | Xuất báo cáo (PDF/Excel) | Button | Chỉ xem báo cáo liên quan hoạt động của chính Service Caterers | BR-175, BR-176, BR-178 | Phạm vi báo cáo giới hạn theo vai trò/trường |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách lịch sử.
- Empty: Chưa có lịch sử nhận hàng nào trong khoảng lọc.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/xuất báo cáo).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Receiving Management Dashboard (nút "Xem Lịch sử"). Thoát: back về Dashboard.
**Bố cục**: Page Header + thanh filter + `Table` dữ liệu (kèm phân trang) + nút "Xuất Báo cáo" góc trên phải, theo `Layout_Guidelines.md` mục 1.1.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving History & Reports, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + thanh filter (ngày, nhà cung cấp) + Table lịch sử nhận hàng + nút Xuất Báo cáo.
Thành phần bắt buộc: badge trạng thái Received/Rejected.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044 - Manage Ingredient Receiving)
- business_rules.md BR-076, BR-175, BR-176, BR-178

# Screen Spec — Three-Step Inspection Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-052 - Manage Three-Step Food Inspection (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099
**MSG liên quan**: MSG-016 (Error), MSG-017 (Error), MSG-043 (Success), MSG-056 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên sản xuất bữa ăn đang hoạt động | Table (Web) / Card list (Mobile) | Mỗi dòng: Mẻ sản xuất, Bước hiện tại (1/2/3), Trạng thái | — | UC-052 Trigger + Normal Flow |
| 2 | Trạng thái từng bước kiểm tra | Badge (Chưa bắt đầu/Đang thực hiện/Passed/Failed) | Bước 3 bị khoá cho tới khi Bước 2 = Passed | BR-093 | UC-052 Normal Flow bước 3 |
| 3 | Nút "Bắt đầu/Tiếp tục Kiểm tra" | Button | Điều hướng tới đúng Step 1/2/3 tương ứng | — | UC-052 Normal Flow |
| 4 | Trạng thái đồng ký số (co-sign) Catering Manager + School Representative | Badge kép | Bắt buộc cả 2 bên ký trước khi phê duyệt phục vụ | BR-099 | UC-052 Normal Flow bước 5 |
| 5 | Cảnh báo thất bại kiểm tra + Corrective Action liên kết | Alert banner | Hiện khi có bước Failed, liên kết CorrectiveActionRequest | BR-097, BR-098 | UC-052 Alternative Flow AF1, MSG-056 |

### Trạng thái màn hình
- Loading: `Skeleton` cho danh sách phiên sản xuất.
- Empty: Chưa có phiên sản xuất nào trong ngày.
- Error: MSG-016 (checklist thiếu bắt buộc), MSG-017 (nhiệt độ nấu chưa đạt), `Alert variant="destructive"`.
- Success: `Toast` MSG-043 khi hoàn tất nộp checklist.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Kiểm tra 3 Bước" trong Sidebar (khuyến nghị dùng Mobile tại bếp — `09_Service_Caterers_Screen_Flow_Web.md`). Điều hướng tiếp: Step 1/2/3, View Preparation Records Ledger, View Inspection Records Log.
**Bố cục**: Page Header + `Table` danh sách phiên sản xuất (cột: Mẻ, Bước hiện tại, Trạng thái, Co-sign, Hành động).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Three-Step Inspection Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table danh sách phiên sản xuất (Mẻ, Bước hiện tại, Trạng thái badge, Co-sign, nút Bắt đầu/Tiếp tục Kiểm tra).
Thành phần bắt buộc: badge trạng thái 4 giá trị, banner cảnh báo khi có bước Failed (MSG-056).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error (MSG-016/017), success (MSG-043).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3487–3505 (UC-052 - Manage Three-Step Food Inspection)
- business_rules.md BR-091–BR-099
- error_message.md MSG-016, MSG-017, MSG-043, MSG-056

# Screen Spec — Inspect Ingredients Before Processing Screen (Step 1)

**Actor**: Service Caterers | **UC liên quan**: UC-062 - Record Ingredients Before Processing (Report 3 §3.2, "Conduct Step 1 — Sample No. 1") | **Status**: Draft
**BR liên quan**: BR-076, BR-079, BR-083, BR-090, BR-091, BR-092
**MSG liên quan**: MSG-015 (Error — GPS), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Quét mã vạch nguyên liệu/dry ingredient để xuất kho | Input/Barcode scan | Bắt buộc | BR-090 | UC-062 Normal Flow bước 2 |
| 2 | Với nguyên liệu tươi/đông lạnh: giờ giao, khối lượng, nhà cung cấp, chứng nhận thú y/kiểm dịch | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 3 | Với nguyên liệu khô/đóng gói: nhà sản xuất, hạn dùng, điều kiện lưu trữ | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 4 | Kiểm tra cảm quan (màu sắc, mùi, hình thức) | Checklist | Bắt buộc đủ mục | BR-095 | UC-062 Normal Flow bước 2 |
| 5 | Kiểm tra hạn sử dụng + FEFO | Validation tự động | Chặn nếu nguyên liệu hết hạn | BR-076, BR-083 | UC-062 Normal Flow bước 2 |
| 6 | Ảnh bằng chứng nguyên liệu + chứng nhận | Upload/Chụp ảnh | Bắt buộc | BR-083 | UC-062 Normal Flow bước 2 |
| 7 | GPS + timestamp | Tự động ghi nhận | Trong khu vực hợp lệ của trường | BR-079 | UC-062 Preconditions, Normal Flow |
| 8 | Nút "Nộp Bước 1" | Button | Nếu đạt → mở khoá Step 2; nếu thất bại → tạo Corrective Action (UC-038), chặn tiến trình | BR-097 | UC-062 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên sản xuất.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực); chặn tiến trình + tạo Corrective Action Request nếu bất kỳ mục nào Failed (BR-097).
- Success: Chuyển trạng thái sang Step 2 (Upload Image Cooked Food Screen).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard (nút "Bắt đầu/Tiếp tục Kiểm tra" khi ở Bước 1). Thoát: nộp thành công → Step 2.
**Bố cục**: Form 1 trang dài trong `Tabs`/Stepper của Inspection Dashboard, ảnh bằng chứng dùng upload file thay chụp trực tiếp (secondary path, khuyến nghị dùng Mobile tại bếp theo `09_Service_Caterers_Screen_Flow_Web.md`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Ingredients Before Processing Screen (Step 1), nền tảng Web, actor Service Caterers.
Bố cục: form 1 trang trong Stepper, gồm quét mã vạch, form nguyên liệu tươi/khô, checklist cảm quan, upload ảnh, khối GPS/timestamp, nút Nộp Bước 1.
Thành phần bắt buộc: cảnh báo hết hạn/FEFO (BR-076/BR-083), cảnh báo GPS (MSG-015).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + tạo Corrective Action), success (mở khoá Step 2).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3535–3549 (UC-062 - Record Ingredients Before Processing, mục "Conduct Step 1")
- business_rules.md BR-076, BR-079, BR-083, BR-090, BR-091, BR-092
- error_message.md MSG-015

# Screen Spec — Upload Image Cooked Food Screen (Step 2)

**Actor**: Service Caterers | **UC liên quan**: UC-063 - Record Ingredients In Processing (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1070) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-093, BR-095, BR-097
**MSG liên quan**: MSG-017 (Error — nhiệt độ nấu chưa đạt), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: `Report 3.md` chỉ có **1 dòng khai báo UC-063** trong bảng tổng hợp Use Case (dòng 1070 "Record Ingredients In Processing"), **không có bảng đặc tả UC đầy đủ riêng** (Normal Flow/Alternative Flow/Exceptions) cho UC-063 — khác với UC-062 (Step 1) đã có bảng chi tiết đầy đủ. Nội dung Business Logic dưới đây được suy ra từ **Step 2 trong Normal Flow của UC-062** (Report 3.md dòng 3542, đoạn "Conduct Step 2 (In-process inspection – Form No. 2)"), vì UC-052/UC-062 mô tả rõ cả 3 bước trong cùng 1 quy trình tổng. **[Giả định — cần team xác nhận]** nếu team muốn viết bảng UC-063 riêng đầy đủ trong Report 3/traceability_matrix.md.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên món / Meal Slot | Read-only (kế thừa từ phiên sản xuất) | — | — | UC-062 Step 2 |
| 2 | Thời điểm bắt đầu/kết thúc sơ chế và nấu | Time picker | Bắt buộc | — | UC-062 Step 2 |
| 3 | Checklist điều kiện vệ sinh (nhân viên, thiết bị, khu vực sơ chế) | Checklist | Bắt buộc đủ mục | BR-095 | UC-062 Step 2 |
| 4 | Nhiệt độ lõi thức ăn sau nấu | Input số (°C) | Phải ≥ 75°C trong ≥ 15 giây | BR-093 | UC-062 Step 2, MSG-017 nếu không đạt |
| 5 | Ảnh món ăn sau khi nấu | Upload ảnh | Bắt buộc | BR-090 (tinh thần ghi nhận bằng chứng, đồng bộ BR-083 Step 1) | Tên màn hình gốc "Upload Image Cooked Food" |
| 6 | GPS + timestamp | Tự động ghi nhận | — | BR-079 (kế thừa từ UC-062) | UC-062 Step 2 |
| 7 | Nút "Nộp Bước 2" | Button | Nếu nhiệt độ < 75°C → chặn, MSG-017, tạo Corrective Action; nếu đạt → mở khoá Step 3 | BR-097 | UC-052 Normal Flow bước 3 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-017 khi nhiệt độ chưa đạt — chặn, tạo Corrective Action Request.
- Success: Chuyển trạng thái sang Step 3 (mở khoá theo BR-093).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 1 = Passed. Thoát: nộp thành công → Step 3.
**Bố cục**: Form 1 trang trong Stepper của Inspection Dashboard, upload ảnh thay chụp trực tiếp (secondary path — khuyến nghị Mobile tại bếp).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Image Cooked Food Screen (Step 2), nền tảng Web, actor Service Caterers.
Bố cục: form 1 trang, checklist vệ sinh, input nhiệt độ lõi, upload ảnh món ăn, khối GPS/timestamp, nút Nộp Bước 2.
Thành phần bắt buộc: cảnh báo nhiệt độ chưa đạt 75°C (MSG-017).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (mở khoá Step 3).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 2") — UC-063 không có bảng đặc tả riêng trong Report 3, xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-093, BR-095, BR-097
- error_message.md MSG-017

# Screen Spec — Inspect Meals Before Serving Screen (Step 3)

**Actor**: Service Caterers | **UC liên quan**: UC-065 - Record Meals Before Serving (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1072) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099
**MSG liên quan**: MSG-016 (Error), MSG-043 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: giống UC-063, `Report 3.md` chỉ có 1 dòng khai báo UC-065 trong bảng tổng hợp UC (dòng 1072 "Record Meals Before Serving"), **không có bảng đặc tả UC đầy đủ riêng**. Nội dung dưới đây suy ra từ **Step 3 trong Normal Flow của UC-062** (Report 3.md dòng 3542, đoạn "Conduct Step 3 (Pre-meal inspection – Form No. 3)") và bước 5 "Co-sign & Approve". **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Số suất ăn (portion count) | Input số | Bắt buộc | — | UC-062 Step 3 |
| 2 | Thời điểm chia suất / bắt đầu bữa ăn | Time picker | Bắt buộc | — | UC-062 Step 3 |
| 3 | Mô tả dụng cụ chứa (sạch, có nắp đậy) | Checklist | Bắt buộc | BR-095 | UC-062 Step 3 |
| 4 | Nhiệt độ giữ ấm | Input số (°C) | Phải ≥ 60°C | BR-094 | UC-062 Step 3, chặn nếu không đạt |
| 5 | Kiểm tra cảm quan trước khi ăn | Checklist | Bắt buộc | BR-095 | UC-062 Step 3 |
| 6 | Ảnh bằng chứng bữa ăn hoàn thiện | Upload ảnh | Bắt buộc | BR-090 | UC-062 Step 3 |
| 7 | Đồng ký số (Catering Manager + School Representative) | Chữ ký số kép | Bắt buộc cả 2 bên trước khi phê duyệt phục vụ | BR-099 | UC-062 bước 5 "Co-sign & Approve" |
| 8 | Nút "Nộp Bước 3 & Phê duyệt Phục vụ" | Button | Nếu đạt đủ 3 bước → chuyển trạng thái "Approved for Serving" | BR-098 | UC-052 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-016 nếu checklist chưa đầy đủ; chặn phê duyệt nếu nhiệt độ giữ ấm < 60°C (BR-094) — tạo Corrective Action.
- Success: `Toast` MSG-043 khi bữa ăn được phê duyệt phục vụ (đủ cả 3 bước Passed + co-sign).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 2 = Passed. Thoát: nộp thành công + đồng ký số → hoàn tất phiên kiểm tra, dữ liệu xuất hiện ở View Preparation Records Ledger/View Inspection Records Log.
**Bố cục**: Form 1 trang trong Stepper, khối chữ ký số 2 bên ở cuối form, upload ảnh thay chụp trực tiếp (secondary path).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Meals Before Serving Screen (Step 3), nền tảng Web, actor Service Caterers.
Bố cục: form 1 trang, checklist dụng cụ + cảm quan, input nhiệt độ giữ ấm, upload ảnh, khối chữ ký số kép, nút Nộp Bước 3 & Phê duyệt Phục vụ.
Thành phần bắt buộc: cảnh báo nhiệt độ giữ ấm chưa đạt 60°C, yêu cầu đủ 2 chữ ký số trước khi bật nút phê duyệt.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (MSG-043, Approved for Serving).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 3" + "Co-sign & Approve") — xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099
- error_message.md MSG-016, MSG-043

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

# Screen Spec — Record Sample Information Form

**Actor**: Service Caterers | **UC liên quan**: UC-069 - Register Food Sample (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-203
**MSG liên quan**: MSG-019 (Error — trọng lượng mẫu dưới 100g), MSG-020 (Error — GPS/nhiệt độ ngoài phạm vi theo trích dẫn UC-069 AF2), MSG-044 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: `Report 3.md` UC-069 Alternative Flow AF2 ("GPS Location Verification Fails") ghi hiển thị **MSG-072**, nhưng theo `error_message.md`, MSG-072 là thông báo Success "Special meal plan saved successfully" — không liên quan lỗi GPS. Mã lỗi đúng ngữ cảnh "GPS ngoài khu vực cho phép" theo `error_message.md` là **MSG-015** hoặc **MSG-020** (nhiệt độ/GPS ngoài phạm vi tại retention). Screen Spec này dùng **MSG-020** cho lỗi GPS ngoài khu vực bếp trường (gần nghĩa nhất với ngữ cảnh retention), và ghi rõ **[Giả định — cần team xác nhận]** số MSG chính xác cho lỗi GPS tại bước đăng ký mẫu.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Xác minh GPS trong khu vực bếp trường | Kiểm tra tự động | Chặn đăng ký nếu ngoài khu vực | BR-109 | UC-069 Normal Flow bước 2, AF2 |
| 2 | Bữa ăn / Món / Tổng số suất | Read-only (kế thừa từ phiên đã qua 3 bước) | — | — | UC-069 Normal Flow bước 3 |
| 3 | Trọng lượng mẫu (gram) | Input số | Bắt buộc ≥ 100g (đặc) / ≥150ml (lỏng) | BR-203 | UC-069 Normal Flow bước 3, MSG-019 nếu dưới ngưỡng *(sửa 2026-07-02: trước đây gán nhầm BR-107, đã repoint sang BR-203)* |
| 4 | Mô tả hộp/túi đựng mẫu | Textarea | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 5 | Nhiệt độ tủ lưu mẫu tại thời điểm lưu | Input số (°C) | Trong khoảng 2–8°C | BR-109, BR-110 | UC-069 Normal Flow bước 3 |
| 6 | Thời điểm thu mẫu | Time picker | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 7 | In nhãn mẫu (Sample No 4) | Button "In nhãn" | Tự động sinh nội dung: Bữa ăn, Tên mẫu, Thời điểm lấy mẫu, Người lấy mẫu | BR-106 | UC-069 Normal Flow bước 4 |
| 8 | Ảnh hộp mẫu đã dán nhãn | Upload ảnh | Bắt buộc | BR-107 | UC-069 Normal Flow bước 5 |
| 9 | Chữ ký số người đăng ký (cột 11 Mẫu số 5) | Chữ ký số | Bắt buộc | — | UC-069 Normal Flow bước 5 |
| 10 | Nút "Xác nhận Lưu mẫu" | Button | Khởi động đồng hồ lưu mẫu 72 giờ (BR-111) | BR-111 | UC-069 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên đã qua kiểm tra.
- Empty: Không áp dụng.
- Error: MSG-019 (trọng lượng < 100g); MSG-020 (GPS/nhiệt độ ngoài phạm vi, xem lưu ý mâu thuẫn nguồn ở trên).
- Success: `Toast` MSG-044 — "Đăng ký lưu mẫu thực phẩm thành công, đồng hồ lưu trữ lạnh đã khởi động".

---

## Platform Layout — Web

**Entry/Exit points**: Từ Food Sample Retention Dashboard (nút "Đăng ký Mẫu mới") — sau khi hoàn tất Three-Step Inspection. Thoát: lưu xong → Food Sample Detail.
**Bố cục**: Form dài trong `Dialog`/trang riêng, khối in nhãn ở giữa form (xem trước nhãn trước khi in), upload ảnh + chữ ký số cuối form. GPS xác nhận qua Geolocation API trình duyệt (secondary path).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Sample Information Form, nền tảng Web, actor Service Caterers.
Bố cục: form dài gồm thông tin bữa ăn/món, input trọng lượng mẫu, mô tả hộp đựng, nhiệt độ tủ lưu, khối xem trước + in nhãn mẫu, upload ảnh, chữ ký số, nút Xác nhận Lưu mẫu.
Thành phần bắt buộc: cảnh báo MSG-019 nếu trọng lượng < 100g, cảnh báo GPS/nhiệt độ ngoài phạm vi.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (MSG-019/MSG-020), success (MSG-044).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3735–3749 (UC-069 - Register Food Sample)
- business_rules.md BR-106, BR-107, BR-108, BR-109, BR-111, BR-114
- error_message.md MSG-019, MSG-020, MSG-044

# Screen Spec — Food Sample Detail

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình chi tiết mở từ Record Sample Information Form (`04_Service_Caterers_Screen_Flow.md`: `SampleForm --> SampleDetail`), dữ liệu nguồn từ UC-069 (Register Food Sample) và UC-118 (View Food Sample Records) | **Status**: Draft
**BR liên quan**: BR-106, BR-108, BR-109, BR-111, BR-114
**MSG liên quan**: MSG-059 (Warning — tủ mở/nhiệt độ dao động), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin mẫu (tên món, ngày sản xuất, ngày lưu mẫu, số tham chiếu lô) | Read-only card | — | BR-106 | UC-069 Postconditions |
| 2 | Trọng lượng mẫu, nơi lưu (tủ số mấy) | Read-only | — | BR-107, BR-109 | — |
| 3 | Đồng hồ đếm ngược thời gian lưu mẫu (72 giờ) | Countdown/Progress | Tự động tính từ BR-111 | BR-111 | UC-069 Postconditions |
| 4 | Trạng thái mẫu | Badge | Retained / Retrieved / Disposed | BR-114 | UC-118 |
| 5 | Ảnh nhãn + hộp mẫu đã lưu | Ảnh xem lại (read-only) | — | BR-107 | — |
| 6 | Cảnh báo nếu tủ lưu mẫu mở/nhiệt độ dao động trong thời gian lưu | Alert banner | Hiện nếu có sự kiện bất thường ghi nhận | BR-110 | MSG-059 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết mẫu.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem).

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Record Sample Information Form sau khi lưu mẫu thành công, hoặc từ danh sách trong Food Sample Retention Dashboard. Thoát: back về Dashboard.
**Bố cục**: Trang chi tiết dạng 2 cột — thông tin mẫu bên trái, ảnh + đồng hồ đếm ngược bên phải, banner cảnh báo (nếu có) trên cùng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Detail, nền tảng Web, actor Service Caterers.
Bố cục: 2 cột — thông tin mẫu + badge trạng thái bên trái, ảnh nhãn/hộp mẫu + đồng hồ đếm ngược 72 giờ bên phải, banner cảnh báo nhiệt độ nếu có.
Thành phần bắt buộc: badge trạng thái 3 giá trị (Retained/Retrieved/Disposed), cảnh báo MSG-059.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh SampleForm --> SampleDetail)
- Report 3.md dòng 3735–3749 (UC-069), dòng 3775–3793 (UC-118 - View Food Sample Records)
- business_rules.md BR-106, BR-108, BR-109, BR-110, BR-111, BR-114
- error_message.md MSG-059

# Screen Spec — Food Sample Retention Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-042 - Manage Food Sample Retention (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-109, BR-110
**MSG liên quan**: MSG-020 (Error — nhiệt độ ngoài phạm vi), MSG-059 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách tủ lưu mẫu đang hoạt động | Table (Web) / Card list (Mobile) | Mỗi dòng: Tủ số, Nhiệt độ hiện tại, Số mẫu đang lưu | BR-109 | UC-042 Normal Flow bước 2 |
| 2 | Nút "Ghi Nhiệt độ" (Log Temperature) | Button | Điều hướng form nhập nhiệt độ 2 lần/ngày (Sáng/Chiều) | BR-110 | UC-042 Normal Flow bước 3–5 |
| 3 | Cảnh báo nhiệt độ ngoài phạm vi 2–8°C | Alert banner | Nếu lệch > 30 phút → cảnh báo báo động | BR-110 | UC-042 Alternative Flow AF1, MSG-020/MSG-059 |
| 4 | Danh sách mẫu đang lưu trong từng tủ | Card list con | Điều hướng Food Sample Detail | BR-109 | UC-042 Normal Flow bước 2 |
| 5 | Chuyển tủ cho mẫu (Update Refrigerator Assignment) | Button + form | Yêu cầu lý do chuyển tủ; kiểm tra tủ đích còn chỗ và an toàn nhiệt độ | — | UC-042 Alternative Flow AF2 |
| 6 | Nút "Đăng ký Mẫu mới" | Button | Điều hướng Record Sample Information Form | — | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho danh sách tủ lưu mẫu.
- Empty: Chưa có tủ lưu mẫu nào được cấu hình (hiếm, thường do lỗi dữ liệu).
- Error: MSG-020 khi nhiệt độ ngoài phạm vi; `Alert variant="warning"` MSG-059 nếu kéo dài > 30 phút (không tự ẩn).
- Success: `Toast` xác nhận ghi nhiệt độ/chuyển tủ thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Lưu mẫu Thực phẩm" trong Sidebar (khuyến nghị dùng Mobile tại bếp). Điều hướng tiếp: Record Sample Information Form, Food Sample Detail.
**Bố cục**: Page Header + Card grid theo từng tủ lưu mẫu (nhiệt độ hiện tại nổi bật + badge cảnh báo), danh sách mẫu con dạng `Table` bên dưới mỗi card khi mở rộng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Retention Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Card grid theo tủ lưu mẫu (nhiệt độ, số mẫu, nút Ghi Nhiệt độ), danh sách mẫu con dạng bảng khi mở rộng, nút Đăng ký Mẫu mới.
Thành phần bắt buộc: banner cảnh báo nhiệt độ ngoài phạm vi (MSG-020/MSG-059).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3709–3727 (UC-042 - Manage Food Sample Retention)
- business_rules.md BR-109, BR-110
- error_message.md MSG-020, MSG-059

# Screen Spec — Meal Service Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-047 - Manage Meal Service (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-100, BR-121, BR-125
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên phục vụ bữa ăn đang hoạt động/dự kiến | Table (Web) / Card list (Mobile) | Cột: Lớp, Giờ phục vụ, Số suất, Trạng thái | — | UC-047 Normal Flow bước 2 |
| 2 | Chi tiết phân bổ suất ăn theo lớp | Read-only detail | Điều chỉnh timing/nhân sự/số suất | BR-121 | UC-047 Normal Flow bước 4–5 |
| 3 | Cảnh báo session đang phục vụ | Alert banner | Nếu session đang Active, chỉ cho sửa field không quan trọng (nhân sự), chặn giảm số suất | — | UC-047 Alternative Flow AF1 |
| 4 | Cảnh báo lệch số suất so với danh sách phân phát | Alert banner | Yêu cầu xác nhận trước khi lưu | BR-121 | UC-047 Alternative Flow AF2 |
| 5 | Nút "Xem Danh sách Phân phát" | Button | Điều hướng View Meal Distribution List | — | ScreenFlow |
| 6 | Nút "Xem Cảnh báo Dị ứng" | Button | Điều hướng View Allergy Alerts Panel | BR-125 | ScreenFlow |

### Trạng thái màn hình
- Loading: `Skeleton` cho danh sách phiên phục vụ.
- Empty: Chưa có phiên phục vụ nào được lên lịch hôm nay.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `Toast` xác nhận cập nhật cấu hình phiên phục vụ thành công.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Phục vụ Bữa ăn" trong Sidebar. Điều hướng tiếp: View Meal Distribution List, View Allergy Alerts Panel, Manage Special Meals Configuration.
**Bố cục**: Page Header + `Table` danh sách phiên phục vụ + panel chi tiết mở bên phải (Sheet) khi chọn 1 dòng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Service Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table danh sách phiên phục vụ (Lớp, Giờ, Số suất, Trạng thái), Sheet chi tiết bên phải khi chọn 1 dòng, link Xem Danh sách Phân phát/Xem Cảnh báo Dị ứng.
Thành phần bắt buộc: banner cảnh báo lệch số suất, banner session đang active hạn chế sửa.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3821–3839 (UC-047 - Manage Meal Service)
- business_rules.md BR-100, BR-121, BR-125

# Screen Spec — View Meal Distribution List

**Actor**: Service Caterers | **UC liên quan**: UC-132 - View Meal Distribution List (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-065, BR-120, BR-121, BR-123
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phân phát theo lớp | Table (Web) / Card list (Mobile) | Cột: Lớp, Số học sinh, Số suất đặc biệt, Trạng thái phục vụ | — | UC-132 Normal Flow bước 2 |
| 2 | Bộ lọc theo ngày/lớp/loại bữa ăn | Filter bar | Tuỳ chọn | — | UC-132 Normal Flow bước 3 |
| 3 | Cảnh báo dị ứng nổi bật | Badge màu `danger` | Hiện khi lớp có học sinh dị ứng đã đăng ký | BR-123 | UC-132 Normal Flow, đồng bộ BR-065 |
| 4 | Cảnh báo lớp chưa xác nhận phân phát | Highlight hàng | Hiện khi giáo viên chủ nhiệm chưa xác nhận | BR-120 | UC-132 Alternative Flow AF2 |
| 5 | Nút "Xuất PDF/Excel" | Button | — | — | UC-132 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách.
- Empty: Chưa có phiên phục vụ nào trong khoảng lọc.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/xuất báo cáo).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Meal Service Management Dashboard (nút "Xem Danh sách Phân phát"). Thoát: back về Dashboard.
**Bố cục**: Page Header + filter bar + `Table` (cột: Lớp, Số học sinh, Số suất đặc biệt, Trạng thái, Cảnh báo dị ứng) + nút xuất báo cáo.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Meal Distribution List, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + filter bar + Table (Lớp, Số học sinh, Số suất đặc biệt, Trạng thái, badge cảnh báo dị ứng), nút Xuất PDF/Excel.
Thành phần bắt buộc: badge cảnh báo dị ứng màu đỏ, highlight lớp chưa xác nhận phân phát.
Áp dụng Design System: xem 00_Foundation/Design_System.md (BR-123 tinh thần màu đỏ dị ứng).
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3953–3971 (UC-132 - View Meal Distribution List)
- business_rules.md BR-065, BR-120, BR-121, BR-123

# Screen Spec — View Allergy Alerts Panel (Special Meal Monitoring)

**Actor**: Service Caterers | **UC liên quan**: UC-103 - View Allergy Alerts (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-065, BR-123
**MSG liên quan**: MSG-051 (Warning — xung đột dị ứng với thực đơn), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có hồ sơ dị ứng đang hoạt động | Table (Web) / Card list (Mobile) | Cột: Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch bữa đặc biệt | BR-065 | UC-103 Normal Flow bước 2 |
| 2 | Dị ứng mức "Severe" | Highlight màu `danger` | Bắt buộc nổi bật | BR-123 | UC-103 Normal Flow bước 3 |
| 3 | Xem chi tiết dị ứng 1 học sinh | Điều hướng chi tiết | Hiển thị dị ứng đã khai báo, ghi chú y tế, lịch sử bữa ăn an toàn | — | UC-103 Normal Flow bước 4 |
| 4 | Trạng thái xác nhận kép (dual-confirmation) | Badge | Bếp phát hành + Giáo viên bàn giao | BR-123 | UC-103 Normal Flow bước 5 |
| 5 | Cảnh báo xung đột dị ứng với thực đơn hôm nay | Alert banner | Hiện khi thực đơn duyệt có nguyên liệu trùng allergen | — | UC-103 Alternative Flow AF1, MSG-051 |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách.
- Empty: Không có học sinh dị ứng nào trong phạm vi actor — thông báo xác nhận (không phải lỗi).
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Meal Service Management Dashboard (nút "Xem Cảnh báo Dị ứng"). Thoát: back về Dashboard.
**Bố cục**: Page Header + `Table` (cột: Học si**BR liên quan**: BR-076, BR-205 (Khóa/cảnh báo hết hạn nguyên liệu kho)
**MSG liên quan**: MSG-052 (Warning), MSG-004/MSG-005 (Error)

> **Lưu ý cập nhật (2026-07-02)**: Bản cũ trích **BR-047** (cảnh báo hết hạn **tài liệu nhà cung cấp**) cho ngữ cảnh "badge Expiring Soon" trên lô nguyên liệu kho — đây là citation sai chủ đề (BR-047 thuộc §5 Supplier). Mã đúng là **BR-205** (bổ sung 2026-07-02).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô nguyên liệu sắp hết hạn | Table (Web) / Card list (Mobile) | Cột: Nguyên liệu, Số lô, Ngày hết hạn, Số lượng còn lại | BR-076 | UC-045 sub-flow |
| 2 | Badge "Expiring Soon" | Badge màu `warning` | Hiện khi lô nguyên liệu còn dưới 30 ngày | BR-205 | MSG-052 |
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
- error_message.md MSG-052�ng định nghĩa rõ, không có data model hỗ trợ, trùng chức năng với chính màn hình Special Meal Plan này).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có dị ứng/chế độ ăn đặc biệt đã xác minh | Table (Web) / Card list (Mobile) | Chỉ hiển thị hồ sơ đã Medical Staff xác nhận | BR-065 | UC-023 Normal Flow bước 2 |
| 2 | Loại chế độ ăn đặc biệt | Badge/Select | Halal, Vegetarian, Gluten-Free, Nut-Free, khác | BR-066 | UC-023 Normal Flow bước 4 |
| 3 | Công thức + hướng dẫn nấu riêng biệt | Textarea | Bắt buộc mô tả dụng cụ nấu riêng, chống nhiễm chéo | BR-082 | UC-023 Normal Flow bước 5 |
| 4 | Khai báo nguyên liệu + dấu vết dị ứng tiềm ẩn | Tag input | Bắt buộc đầy đủ | BR-069 | UC-023 Normal Flow bước 6 |
| 5 | Kiểm tra không trùng allergen với hồ sơ học sinh | Validation tự động | Chặn lưu nếu phát hiện trùng | BR-063 | UC-023 Normal Flow bước 7, MSG-051 |
| 6 | Nút "AI Assist" gợi ý công thức an toàn | Button | Mở panel gợi ý (Gemini AI) | BR-156, BR-157 | UC-023 Alternative Flow AF1 |
| 7 | Nút "Lưu Kế hoạch Bữa ăn Đặc biệt" | Button | Lưu, hiển thị MSG-072 | — | UC-023 Normal Flow bước 8 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách học sinh.
- Empty: Chưa có học sinh nào cần kế hoạch bữa ăn đặc biệt.
- Error: `Alert variant="destructive"` MSG-051 nếu phát hiện trùng allergen (chặn lưu).
- Success: `Toast` MSG-072.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Update Menu Screen (`04_Service_Caterers_Screen_Flow.md`: `UpdateMenuScr --> SpecialMealCfg`) hoặc từ Meal Service Management Dashboard. Thoát: lưu xong quay lại màn gọi.
**Bố cục**: Trang 2 cột — danh sách học sinh bên trái (`Table`), form cấu hình kế hoạch bữa ăn đặc biệt bên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Special Meals Configuration, nền tảng Web, actor Service Caterers.
Bố cục: 2 cột — Table danh sách học sinh dị ứng/đặc biệt bên trái, form cấu hình (loại chế độ ăn, công thức riêng, khai báo nguyên liệu) bên phải, nút AI Assist, nút Lưu Kế hoạch.
Thành phần bắt buộc: cảnh báo trùng allergen (MSG-051).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-072).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2845–2863 (UC-023 - Create Special Meal Plan)
- business_rules.md BR-063, BR-065, BR-066, BR-069, BR-082, BR-156, BR-157
- error_message.md MSG-072, MSG-051

# Screen Spec — Inventory Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-076, BR-081, BR-082, BR-083, BR-089, BR-090
**MSG liên quan**: MSG-018 (Warning/Error — thiếu kho), MSG-042 (Success), MSG-052 (Warning — sắp hết hạn), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: `Report 3.md` UC-045 Business Rules trích "BR-076, BR-081, BR-082, BR-083, BR-089, BR-090" khớp đúng với `business_rules.md` hiện hành (FEFO BR-075/076, storage zone không có mã BR-081 riêng trong business_rules.md hiện tại — mục 6 chỉ có BR-070 đến BR-078). **[Giả định — cần team xác nhận]**: business_rules.md không có BR-081/082/083/089/090 trong mục "Quản lý Nguyên liệu & Kho" (BR-081/082 thuộc mục Menu & Diet — món thay thế dị ứng/chế biến riêng; BR-089 thuộc Food Safety Monitoring — giám sát nhiệt độ tủ lạnh; BR-090 thuộc Three-Step Inspection — nhiệt độ tối thiểu sau nấu). Report 3 UC-045 dùng các mã này với ngữ nghĩa khác (zone matching, block hết hạn) — đây là trích dẫn chéo module không khớp. Screen Spec này giữ **BR-075 (FEFO), BR-076 (truy xuất lô), BR-077 (cảnh báo thiếu kho), BR-089 (giám sát nhiệt độ, đúng nghĩa gần nhất cho cảnh báo nhiệt độ kho)** làm nguồn chính xác, đánh dấu phần "storage zone matching"/"block expired" là **[Giả định — cần team xác nhận mã BR]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tồn kho theo nguyên liệu, khu vực lưu trữ, hạn dùng lô | Table (Web) / Card list (Mobile) | Nhóm theo ingredient/zone/expiration | BR-076 | UC-045 Normal Flow bước 2 |
| 2 | Nút "Xuất kho" (Issue) | Button | Điều hướng Issue Ingredients Workspace, áp dụng FEFO | BR-075 | UC-045 Normal Flow bước 3–4 |
| 3 | Nút "Điều chỉnh Kho" (Adjustment) | Button | Yêu cầu mã lý do bắt buộc + xác nhận quản lý (spoilage write-off) | — | UC-045 Alternative Flow AF1 |
| 4 | Cảnh báo thiếu kho | Alert banner | Hiện khi tồn không đủ nhu cầu kế hoạch | BR-077 | UC-045 Alternative Flow AF2, MSG-018 |
| 5 | Cảnh báo sắp hết hạn | Badge/Banner | Hiện theo ngưỡng cảnh báo hết hạn nguyên liệu kho | BR-205 | MSG-052 |
| 6 | Nút "Xem Cảnh báo Hết hạn" | Button | Điều hướng Monitor Expiration Dates Panel | — | ScreenFlow |
| 7 | Nút "Xem Nhật ký Giao dịch Kho" | Button | Điều hướng Record Inventory Transactions Log | — | ScreenFlow |
| 8 | Nút "Tạo Yêu cầu Mua hàng" (khi tồn thấp) | Button | Điều hướng Create Purchase Request | BR-077 | ScreenFlow: `InvDash -->|"low stock"| CreatePR` |

### Trạng thái màn hình
- Loading: `Skeleton` cho bảng/danh sách tồn kho.
- Empty: Kho trống hoàn toàn (hiếm) → thông báo + gợi ý tạo yêu cầu mua hàng.
- Error: MSG-018 khi thiếu kho; `Alert variant="destructive"` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `Toast` MSG-042 sau khi ghi nhận giao dịch kho.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Nhận hàng & Kho" trong Sidebar. Điều hướng tiếp: Issue Ingredients Workspace, Monitor Expiration Dates Panel, Record Inventory Transactions Log, Create Purchase Request.
**Bố cục**: Page Header + `Table` tồn kho (cột: Nguyên liệu, Khu vực, Số lượng, Hạn dùng gần nhất, Trạng thái) + thanh filter/search + nút hành động (Xuất kho/Điều chỉnh) ở cột cuối.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inventory Management Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table tồn kho (Nguyên liệu, Khu vực, Số lượng, Hạn dùng, Trạng thái, Hành động Xuất kho/Điều chỉnh), banner cảnh báo thiếu kho/sắp hết hạn.
Thành phần bắt buộc: badge cảnh báo (MSG-018, MSG-052), nút Tạo Yêu cầu Mua hàng khi tồn thấp.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-075, BR-076, BR-077, BR-089, BR-205 (xem lưu ý mâu thuẫn nguồn đã giải quyết)
- error_message.md MSG-018, MSG-042, MSG-052
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Inventory → Create Purchase Request)

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

# Screen Spec — Monitor Expiration Dates Panel (Expiration Alerts)

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2, sub-flow cảnh báo hạn dùng) | **Status**: Draft
**BR liên quan**: BR-076, BR-205 (Khóa/cảnh báo hết hạn nguyên liệu kho)
**MSG liên quan**: MSG-052 (Warning), MSG-004/MSG-005 (Error)

> **Lưu ý cập nhật (2026-07-02)**: Bản cũ trích **BR-047** (cảnh báo hết hạn tài liệu nhà cung cấp) cho ngữ cảnh badge Expiring Soon lô nguyên liệu kho — citation sai chủ đề. Mã đúng là **BR-205** (bổ sung 2026-07-02).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô nguyên liệu sắp hết hạn | Table (Web) / Card list (Mobile) | Cột: Nguyên liệu, Số lô, Ngày hết hạn, Số lượng còn lại | BR-076 | UC-045 sub-flow |
| 2 | Badge "Expiring Soon" | Badge màu `warning` | Hiện khi lô nguyên liệu còn dưới 30 ngày | BR-205 | MSG-052 |
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

# Screen Spec — Record Inventory Transactions Log

**Actor**: Service Caterers | **UC liên quan**: UC-064 - Record Inventory Transactions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-082 (Report 3 gọi "mandatory reason code and manager sign-off" — đối chiếu `business_rules.md`, BR-082 hiện tại nghĩa là "Chế biến riêng cho bữa ăn đặc biệt", KHÔNG liên quan reason code/sign-off kho)
**MSG liên quan**: MSG-042 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn quan trọng**: `Report 3.md` UC-064 trích **BR-082** cho yêu cầu "mandatory reason code and manager sign-off" khi điều chỉnh kho thủ công, nhưng theo `business_rules.md` hiện hành, BR-082 = "Chế biến riêng cho bữa ăn đặc biệt" (Menu & Diet Management) — hoàn toàn khác chủ đề. **business_rules.md hiện KHÔNG có mã BR nào mô tả rõ "bắt buộc mã lý do + xác nhận quản lý khi điều chỉnh tồn kho thủ công"** — đây là khoảng trống thật giữa Report 3 và business_rules.md. Screen Spec này **giữ nguyên yêu cầu nghiệp vụ** (mã lý do + sign-off, vì đây là control nội bộ hợp lý và nhất quán với UC-045 AF1) nhưng **không gán BR-082**, đánh dấu **[Giả định — cần team xác nhận, bổ sung mã BR mới trong `business_rules.md` nếu team đồng ý giữ yêu cầu này]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn lô nguyên liệu cần điều chỉnh | Select | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 2 | Số lượng điều chỉnh | Input số (+/-) | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 3 | Mã lý do điều chỉnh (spoilage, waste, audit write-off) | Select | Bắt buộc | BR-204 | UC-064 Normal Flow bước 2 |
| 4 | Xác nhận/chữ ký quản lý | Upload chữ ký hoặc xác thực quản lý | Bắt buộc | BR-204 | UC-064 Normal Flow bước 3 |
| 5 | Nút "Ghi nhận Giao dịch" | Button | Chặn nếu thiếu mã lý do hoặc sign-off | BR-204 | UC-064 Exceptions, MSG-042 |
| 6 | Nhật ký giao dịch kho (lịch sử) | Table (Web) / Card list (Mobile) | Cột: Ngày, Nguyên liệu, Số lượng, Lý do, Người thực hiện | BR-170 (audit trail chung) | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho form + nhật ký.
- Empty: Chưa có giao dịch điều chỉnh nào.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu thiếu mã lý do/sign-off.
- Success: `Toast` MSG-042.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Inventory Management Dashboard (nút "Xem Nhật ký Giao dịch Kho" hoặc "Điều chỉnh Kho"). Thoát: back về Dashboard.
**Bố cục**: Page Header + form điều chỉnh dạng `Dialog` ngắn + `Table` nhật ký giao dịch bên dưới (phân trang).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Inventory Transactions Log, nền tảng Web, actor Service Caterers.
Bố cục: Dialog form điều chỉnh (chọn lô, số lượng, mã lý do, xác nhận quản lý) + Table nhật ký giao dịch bên dưới.
Thành phần bắt buộc: chặn submit nếu thiếu mã lý do/sign-off (BR-204).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3199–3217 (UC-064 - Record Inventory Transactions)
- business_rules.md BR-204 (bắt buộc mã lý do + sign-off khi điều chỉnh kho — bổ sung 2026-07-02)
- business_rules.md BR-170 (audit trail)
- error_message.md MSG-042

# Screen Spec — Corrective Actions Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-038 - Manage Corrective Actions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-152, BR-153, BR-155
**MSG liên quan**: MSG-023 (Warning — nộp trễ hạn), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách CAPA được gán cho Service Caterers | Table (Web) / Card list (Mobile) | Cột: Sự cố liên quan, Hạn chót, Trạng thái | BR-152 | UC-038 Normal Flow bước 2 |
| 2 | Trạng thái CAPA | Badge | Pending / In Progress / Pending Approval / Late Submission | BR-152, BR-153 | UC-038 Exceptions |
| 3 | Chi tiết hành động yêu cầu, nguyên nhân gốc rễ, hạn chót | Read-only detail | — | BR-152 | UC-038 Normal Flow bước 4 |
| 4 | Nút "Cập nhật Tiến độ" | Button | Điều hướng Submit Corrective Action Results Form | — | UC-038 Normal Flow bước 5 |
| 5 | Cảnh báo nộp trễ hạn (quá 12 giờ) | Badge màu `warning` | Tự động gắn cờ "Late Submission" | BR-152 | UC-038 Exceptions EX1, MSG-023 |

### Trạng thái màn hình
- Loading: `Skeleton` cho danh sách CAPA.
- Empty: Không có CAPA nào đang chờ xử lý.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem/điều hướng).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Service Caterers Dashboard → mục "Sự cố & Vi phạm" trong Sidebar. Điều hướng tiếp: Submit Corrective Action Results Form, View Incident Information Detail.
**Bố cục**: Page Header + `Table` (Sự cố, Hạn chót, Trạng thái, Hành động).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Corrective Actions Dashboard, nền tảng Web, actor Service Caterers.
Bố cục: Page Header + Table CAPA (Sự cố liên quan, Hạn chót, Trạng thái badge, nút Cập nhật Tiến độ).
Thành phần bắt buộc: badge "Late Submission" màu warning.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4419–4437 (UC-038 - Manage Corrective Actions)
- business_rules.md BR-152, BR-153, BR-155
- error_message.md MSG-023

# Screen Spec — Submit Corrective Action Results Form

**Actor**: Service Caterers | **UC liên quan**: UC-082 - Submit Corrective Action Results (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-152, BR-153, BR-155
**MSG liên quan**: MSG-043 (Success — dùng chung ngữ cảnh nộp kết quả), MSG-004 (Error — thiếu Health Department Clearance)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin CAPA đang xử lý | Read-only header | — | — | UC-082 Preconditions |
| 2 | Ghi chú hoàn thành | Textarea | Bắt buộc | — | UC-082 Normal Flow bước 2 |
| 3 | Upload tài liệu bằng chứng (báo cáo khử trùng, chứng nhận vệ sinh) | Upload nhiều file | Bắt buộc ít nhất 1 file | — | UC-082 Normal Flow bước 2 |
| 4 | Giấy chứng nhận của Cơ quan Y tế (Health Department Clearance) | Upload file | **Bắt buộc** nếu sự cố mức High Severity từng khiến bếp phải đóng cửa | BR-153 | UC-082 Normal Flow bước 3, Exceptions EX1 |
| 5 | Nút "Nộp Kết quả" | Button | Chặn nếu thiếu Health Department Clearance khi bắt buộc | BR-153 | UC-082 Exceptions EX1, MSG-004 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin CAPA.
- Empty: Không áp dụng.
- Error: MSG-004 (chặn nộp nếu thiếu Health Department Clearance bắt buộc).
- Success: `Toast` xác nhận nộp kết quả thành công, chuyển trạng thái CAPA sang "Pending Approval", thông báo School Admin.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Corrective Actions Dashboard (nút "Cập nhật Tiến độ"). Thoát: nộp xong quay lại Dashboard.
**Bố cục**: Form trong `Dialog`/trang riêng — ghi chú hoàn thành, khối upload tài liệu (đa file), khối upload Health Department Clearance (điều kiện hiện), nút nộp.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Corrective Action Results Form, nền tảng Web, actor Service Caterers.
Bố cục: form gồm ghi chú hoàn thành, upload tài liệu bằng chứng đa file, khối upload Health Department Clearance (điều kiện), nút Nộp Kết quả.
Thành phần bắt buộc: cảnh báo chặn nộp nếu thiếu Health Department Clearance bắt buộc (MSG-004).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn nộp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4533–4547 (UC-082 - Submit Corrective Action Results)
- business_rules.md BR-152, BR-153, BR-155

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

# Screen Spec — Record Process Violation (UC-178) — `(Deprecated 11/07/2026)`

> **TRẠNG THÁI: DEPRECATED.** UC-178 không còn là màn hình riêng biệt — dialog nhập liệu đã gộp vào dialog "Lập Biên bản Vi phạm" hợp nhất (mã UC sống sót = **UC-179**), xem đầy đủ ngay bên dưới. Giữ nguyên số hiệu UC-178 (không tái sử dụng). Chi tiết lịch sử: `36_RecordProcessViolation.md`, `review/medical_staff/review_screen_medical_staff.md` Mục 10.

# Screen Spec — Record Violation (Dialog hợp nhất — thay thế Record Process Violation UC-178 + Record Staff Violation UC-179)

**Actor**: Service Caterers (đồng thời Medical Staff — cả 2 vai trò đều có quyền lập biên bản theo BR-183/184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, dòng 4639–4659) — **mã UC sống sót sau khi gộp UI với UC-178, quyết định 11/07/2026**. UC-178 (Report 3 §3.2, dòng 3663–3683) — **Deprecated 11/07/2026** | **Status**: Draft
**BR liên quan**: BR-183 (vi phạm quy trình/sản phẩm — kích hoạt khi field "Nhân viên liên quan" để trống), BR-184 (vi phạm nhân viên — kích hoạt khi có điền), BR-185, BR-186, BR-187 (cảnh báo nghi ngờ tái diễn 90 ngày), BR-188 (audit log bất biến), BR-190 (báo cáo lạm dụng manual override), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký — sửa 11/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning)

> **Quyết định thiết kế 07/07/2026, chốt cuối 11/07/2026** (`review/medical_staff/review_screen_medical_staff.md` Mục 10): giữ tách BR-183/BR-184 ở tầng dữ liệu (nguồn gốc phát sinh khác nhau — Service Caterers thường tạo khi 1 bước Three-Step Inspection thất bại, Medical Staff thường lập tay khi phát hiện vi phạm nhân viên) nhưng **gộp UI thành 1 dialog duy nhất** — không có toggle chọn "loại vi phạm", BR-183 hay BR-184 được suy ra tự động từ việc có điền field "Nhân viên liên quan" (field 4) hay không.
> **Cập nhật 11/07/2026 — bỏ hẳn field nhân chứng**: hệ thống không track thông tin người chứng kiến dưới bất kỳ hình thức nào (bỏ field "Chọn Nhân chứng (Witness)" từng có trong UC-178 gốc). PDF vẫn giữ cột chữ ký thứ 3 "Người chứng kiến" (khớp mẫu giấy gốc) nhưng là khoảng trống tĩnh, không gắn field dữ liệu.
> **Cập nhật 11/07/2026 — đổi tên tiêu đề PDF**: từ "BIÊN BẢN SỰ VIỆC" → **"BIÊN BẢN VI PHẠM"**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | Input text | Bắt buộc, không rỗng | BR-211 | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản đăng nhập hiện tại | BR-183/184 | Mẫu giấy: "Đại diện P.VSATTP"; khi Service Caterers là người lập, vẫn hiển thị đúng tài khoản Y tế trường học đã được gán xử lý (không phải tài khoản Service Caterers) |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | Select (tài khoản Service Caterers đang hoạt động) | Bắt buộc | BR-211 | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật |
| 4 | Nhân viên liên quan (`staffName`) | Input text tự do (không phải khóa ngoại) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | BR-184 | Trống = chỉ áp BR-183 (thường gặp khi tạo tự động từ `InspectionStep.fail()`); có giá trị = kích hoạt thêm BR-184/185/187 |
| 5 | Chức vụ nhân viên (`staffPosition`) | Input text tự do | Tùy chọn; chỉ hiện khi field 4 đã có ký tự đầu tiên | BR-184 | Không có trên mẫu giấy |
| 6 | Mức độ nghiêm trọng (`severityLevel`) | Select (LOW/MEDIUM/HIGH) | Bắt buộc | BR-183/184, BR-186 | Điều kiện tự động phân luồng nút ở `24_RecordViolationResolution.md` |
| 7 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | Select | Tùy chọn | BR-190 | 4 giá trị: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống", "Hành động không được ủy quyền" |
| 8 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker | Bắt buộc | BR-211 | Tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong PDF |
| 9 | **Nội dung biên bản** (`violationContent`) | Rich text editor | Bắt buộc, không rỗng | BR-183/184 | MSG-085 nếu thiếu |
| 10 | Ảnh bằng chứng | Upload ảnh, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | BR-183/184 | MSG-085 nếu thiếu |
| 11 | Nút "Save Violation" | Button (primary) | Disabled khi thiếu field bắt buộc (1, 3, 6, 8, 9, 10) | BR-183/184, BR-188 | Lưu dữ liệu số ngay — không chờ ảnh biên bản đã ký; liên kết `NonConformanceRecord` khi tạo từ `InspectionStep.fail()` — luồng `CorrectiveActionRequest` (UC-038/UC-082) tiếp tục song song, không bị thay thế |
| 12 | Nút "Xuất Biên bản (PDF)" | Button (secondary) | Chỉ hiện sau khi đã Save Violation | BR-211 | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**; 3 khoảng chữ ký (cột 3 để trống tĩnh) |
| 13 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Upload ảnh | Không bắt buộc để Save; **bắt buộc** trước khi đóng/gửi duyệt ở `24_RecordViolationResolution.md` — gate chặn cứng | BR-211 | Tải lên sau khi in — ký tay — chụp ảnh |

### Cơ chế phân loại BR-183 vs BR-184 (không có toggle thủ công)
- Field 4 để trống → chỉ áp **BR-183** — field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**. Đây là trường hợp phổ biến khi Service Caterers tạo từ `InspectionStep.fail()`.
- Field 4 có giá trị → kích hoạt thêm **BR-184/185** và **BR-187** (best-effort, cảnh báo nghi ngờ tái diễn khi staffName trùng khớp với 1 bản ghi Medium+ trong 90 ngày gần nhất).
- Field 3, 1/8/9/10 luôn bắt buộc bất kể field 4.

### Trạng thái màn hình
- **Loading**: `Skeleton` khi tải ngữ cảnh (nếu mở từ `InspectionStep.fail()`) hoặc rà soát trùng tên (khi field 4 có giá trị); spinner trên "Save Violation" khi lưu.
- **Empty**: không áp dụng.
- **Error**: `Alert variant="destructive"` MSG-004, MSG-005, MSG-085 tại field thiếu.
- **Success**: `Toast` MSG-086, ghi vào audit log bất biến (BR-188); trùng tên ≥ MEDIUM trong 90 ngày → `Alert variant="warning"` MSG-088 (không chặn lưu); điều hướng `24_RecordViolationResolution.md`. Nếu chưa tải `signedRecordPhotoUrl`, màn `24` chặn cứng nút đóng/gửi duyệt (MSG-095).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Corrective Actions Dashboard (mục "Sự cố & Vi phạm" trong Sidebar → "Record Violation") hoặc tự động điều hướng khi 1 bước Three-Step Inspection thất bại (`InspectionStep.fail()`, theo `09_Service_Caterers_Screen_Flow_Web.md` — field 4 để trống mặc định). Thoát: lưu xong quay lại Corrective Actions Dashboard/`24_RecordViolationResolution.md`.
**Bố cục**: Form trong `Dialog`/trang riêng, 1 cột — xem đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md`.

## Xuất & Lưu Biên bản đã ký (BR-211, sửa 11/07/2026)

> Mô tả đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md` — áp dụng chung mọi actor có quyền lập biên bản (Medical Staff, Service Caterers). Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**, cột chữ ký thứ 3 "Người chứng kiến" để trống tĩnh.

## AI Generation Prompt ⭐

```
Tạo wireframe cho dialog "Lập Biên bản Vi phạm", nền tảng Web, actor Service Caterers.
Bố cục: Dialog/trang riêng, form 1 cột.
Thứ tự field: Địa điểm xảy ra sự việc, Đại diện Y tế trường học (hiển thị tự động, không sửa),
Đại diện Nhà thầu (Select) — KHÔNG có field Người chứng kiến trong form,
Nhân viên liên quan (tùy chọn, thường để trống khi tạo từ Three-Step Inspection thất bại) —
khi có giá trị thì hiện thêm Chức vụ nhân viên,
Mức độ nghiêm trọng (Select LOW/MEDIUM/HIGH), Loại vi phạm (Select, tùy chọn),
Thời điểm xảy ra sự việc (DateTimePicker), khối "Nội dung biên bản" là rich text editor,
phía trên hiện dòng tự sinh "Vào hồi [giờ] ngày [ngày]...", upload ảnh bằng chứng (bắt buộc ít nhất 1),
nút Save Violation, sau khi lưu hiện thêm nút "Xuất Biên bản (PDF)" (tiêu đề "BIÊN BẢN VI PHẠM").
Áp dụng Design System: xem 00_Foundation/Design_System.md; danger #B91C1C (HIGH), warning #B45309 (MEDIUM).
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-086/MSG-088).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm toggle chọn "loại vi phạm".
KHÔNG thêm field nhân chứng (witness) trong form nhập liệu.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4639–4659 (UC-179), dòng 3663–3683 (UC-178 — nay Deprecated).
- business_rules.md §18 (BR-183, BR-184, BR-185, BR-186, BR-187, BR-188, BR-190, BR-211).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-086, MSG-088, MSG-095).
- `02_Requirements/Violation_Management_Proposal.md`.
- `00_Policies/specialreport2173_1.md` (mẫu Biên bản Sự việc).
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Violation Management, InspectionStep.fail()), `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md`.
- `review/medical_staff/review_screen_medical_staff.md` Mục 10.
