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
| 2 | Grid Menu Items (8 mục điều hướng: Thực đơn, Nhận hàng, Kho, Kiểm tra 3 Bước, Lưu mẫu Thực phẩm, Phục vụ Bữa ăn, Sự cố & Hành động Khắc phục, Vi phạm) | Card grid (2 cột) | Mỗi mục điều hướng tới đúng 1 Dashboard con | — | Cấu trúc lấy từ `04_Service_Caterers_Screen_Flow.md` (nhánh "Grid Menu Items") |
| 3 | Widget tóm tắt hoạt động hôm nay (số phiên nhận hàng, số bước kiểm tra đang chờ, số cảnh báo dị ứng đang hoạt động) | Card thống kê | Cập nhật theo BR-180 nếu áp dụng | BR-180 | **[Giả định — cần team xác nhận]** nội dung widget cụ thể chưa có mô tả field-by-field gốc trong Report 3 |
| 4 | Banner trạng thái mạng (Offline/Cần đồng bộ) | Banner cố định | Hiện khi offline hoặc cache quá hạn | BR-196–200 | Theo `00_Foundation/UI_Design_Guidelines.md` mục 5.5 |

### Trạng thái màn hình
- Loading: Shimmer cho grid + widget thống kê.
- Empty: Không áp dụng (grid điều hướng luôn hiển thị đủ 8 mục cố định).
- Error: `SnackBar` màu `danger` nếu tải widget thống kê thất bại, kèm nút "Thử lại".
- Success: Không áp dụng (không có thao tác ghi).

---

## Platform Layout — Mobile

**Entry/Exit points**: Vào ngay sau khi đăng nhập thành công qua Common Entry Flow (`13_Guest_Common_Screen_Flow.md`); là màn hình gốc (`04_Service_Caterers_Screen_Flow.md`). Thoát: chạm 1 thẻ trong Grid Menu Items để vào Dashboard con tương ứng.
**Bố cục**: Theo `00_Foundation/Layout_Guidelines.md` mục 2.1 — AppBar (tiêu đề "Service Caterers") + Content 1 cột cuộn dọc: widget thống kê trên cùng, Grid Menu Items dạng Card 2 cột bên dưới + `BottomNavigationBar` 4-5 mục (theo tần suất sử dụng: Kiểm tra 3 Bước, Nhận hàng, Kho, Thêm — theo `00_Foundation/Navigation_Guidelines.md` mục 2). Pull-to-refresh bắt buộc cho widget thống kê.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Service Caterers Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar (tiêu đề) + Content 1 cột cuộn dọc (widget thống kê + Grid Menu Items 2 cột dạng Card) + BottomNavigationBar 4-5 mục có icon + label chữ.
Thành phần bắt buộc: lời chào người dùng, widget thống kê hoạt động hôm nay, grid 8 mục điều hướng module chính, banner trạng thái mạng nếu offline.
Áp dụng Design System: seed color #0E7490 (ColorScheme.fromSeed), Material 3, font Be Vietnam Pro, touch target tối thiểu 48x48dp (xem 00_Foundation/Design_System.md, Accessibility.md).
Trạng thái cần thể hiện: loading (shimmer), error (snackbar khi widget lỗi), banner offline nếu áp dụng.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (Grid Menu Items, sơ đồ điều hướng gốc)
- `02_Requirements/00_BusinessRules/business_rules.md` BR-180, BR-196–200

# Screen Spec — Menu Management Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-048 - Manage Menus (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-067
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách thực đơn theo tuần | Card list (1 bản ghi = 1 Card) | Mỗi card: Tuần áp dụng, Trạng thái, Ngày cập nhật cuối | — | UC-048 Normal Flow bước 2 |
| 2 | Trạng thái thực đơn | Chip (Material 3) | Draft / Pending Approval / Approved / Published / Rejected | BR-067, BR-068 | Màu theo `Design_System.md` mục 3 |
| 3 | Bộ lọc theo khoảng ngày | DatePicker (M3) | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 4 | Bộ lọc theo trạng thái | DropdownMenu | Tuỳ chọn | — | UC-048 Normal Flow bước 3 |
| 5 | FAB "Tạo Thực đơn Mới" | FloatingActionButton | Điều hướng tới Create Menu Workspace (UC-020) | — | Góc dưới phải theo `Layout_Guidelines.md` mục 2.1 |
| 6 | Hành động "Edit" trên card Draft/Rejected | Button | Chỉ hiện khi status = Draft hoặc Rejected; điều hướng Update Menu Screen (UC-096) | BR-067 | — |
| 7 | Hành động "Submit" trên card Draft | Button | Chỉ hiện khi status = Draft; điều hướng Submit Menu For Approval Trigger (UC-087) | BR-067 | — |
| 8 | Hành động "Delete" trên card Draft/Rejected | Icon Button (danger) + `AlertDialog` xác nhận | Chỉ hiện khi status = Draft hoặc Rejected | BR-067 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho card list.
- Empty: Chưa có thực đơn nào → illustration + FAB nổi bật.
- Error: `SnackBar`/`AlertDialog` màu `danger` khi tải thất bại (MSG-004/MSG-005).
- Success: `SnackBar` màu `success` sau khi Delete thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Thực đơn" trong Grid Menu Items (`04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Create Menu Workspace, Update Menu Screen.
**Bố cục**: AppBar (tiêu đề "Quản lý Thực đơn") + Content 1 cột: thanh filter thu gọn trên cùng + Card list cuộn dọc (pull-to-refresh bắt buộc) + FAB "Tạo Thực đơn Mới" góc dưới phải, theo `00_Foundation/Layout_Guidelines.md` mục 2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter thu gọn + Card list (1 bản ghi/card: Tuần, Trạng thái, Ngày cập nhật, action Edit/Submit/Delete) + FAB Tạo Thực đơn Mới.
Thành phần bắt buộc: Chip trạng thái theo màu ngữ nghĩa, pull-to-refresh.
Áp dụng Design System: xem 00_Foundation/Design_System.md, Component_Guidelines.md (Card, Chip, FAB).
Trạng thái cần thể hiện: loading (shimmer), empty, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2867–2886 (UC-048 - Manage Menus)
- business_rules.md BR-067, BR-068

# Screen Spec — Create Menu Workspace

**Actor**: Service Caterers | **UC liên quan**: UC-020 - Create Menu (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
**MSG liên quan**: MSG-051 (Warning — xung đột dị ứng), MSG-071 (Success — lưu nháp), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tuần áp dụng / Ngày phục vụ | DatePicker (M3) | Bắt buộc; tối thiểu 3 ngày trước tuần dùng | BR-061 | UC-020 Normal Flow bước 2 |
| 2 | Bữa ăn trong ngày (Meal Slot) | DropdownMenu | Bắt buộc | — | UC-020 Normal Flow bước 2 |
| 3 | Chọn món ăn (Dish Selection) | Danh sách chọn (Checkbox list) | Bắt buộc ít nhất 1 món/bữa | BR-068 | UC-020 Normal Flow bước 3 |
| 4 | Khai báo nguyên liệu + chất gây dị ứng từng món | TextField theo món (trang riêng trong stepper) | Bắt buộc đầy đủ | BR-069 | UC-020 Postconditions |
| 5 | Mục tiêu dinh dưỡng (calo, nhóm chất) | Form nhập số | Phải đáp ứng chỉ tiêu theo nhóm tuổi | BR-062 | UC-020 Normal Flow bước 3 |
| 6 | Nút "AI Assist" (Gemini AI) | FilledButton | Mở màn View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-020 Alternative Flow AF1 |
| 7 | Kết quả kiểm tra xung đột dị ứng | MaterialBanner | Tự động chạy khi lưu/submit; MSG-051 nếu có xung đột | BR-063 | UC-020 Normal Flow bước 4 |
| 8 | Nút "Lưu nháp" | FilledButton (cố định cuối màn) | Lưu trạng thái Draft, hiển thị MSG-071 | — | UC-020 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Shimmer`/`CircularProgressIndicator` khi tải danh sách món ăn/kho.
- Empty: Không áp dụng.
- Error: `AlertDialog` màu `danger` (MSG-004/MSG-005) nếu lưu thất bại.
- Success: `SnackBar` MSG-071 khi lưu nháp thành công; `MaterialBanner` màu `warning` (không tự ẩn) nếu MSG-051 xuất hiện.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Menu Management Dashboard → FAB "Tạo Thực đơn Mới" (`04_Service_Caterers_Screen_Flow.md`). Thoát: nút "Lưu nháp" quay lại Menu Management Dashboard.
**Bố cục**: Form nhiều trang riêng (không Stepper ngang do màn hình hẹp) — Trang 1: Thông tin chung; Trang 2: Chọn món & dinh dưỡng; Trang 3: Khai báo dị ứng — có nút "Tiếp theo/Quay lại" cố định dưới cùng theo `00_Foundation/Layout_Guidelines.md` mục 2.2; panel AI Recommendations mở dạng `showModalBottomSheet`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Menu Workspace, nền tảng Mobile, actor Service Caterers.
Bố cục: 3 trang tuần tự (Thông tin chung, Chọn món & dinh dưỡng, Khai báo dị ứng) với nút Tiếp theo/Quay lại cố định dưới cùng.
Thành phần bắt buộc: DatePicker, DropdownMenu bữa ăn, checkbox list chọn món, TextField nguyên liệu/dị ứng, banner cảnh báo dị ứng MSG-051.
Áp dụng Design System: ColorScheme.fromSeed #0E7490, SfmsStatusColors cho banner cảnh báo (xem Design_System.md).
Trạng thái cần thể hiện: loading, error, success (snackbar lưu nháp), warning (banner xung đột dị ứng).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2823–2841 (UC-020 - Create Menu)
- business_rules.md BR-061, BR-062, BR-063, BR-064, BR-068, BR-069, BR-156, BR-157
- error_message.md MSG-051, MSG-071

# Screen Spec — View Menu Recommendations Panel (AI Integration)

**Actor**: Service Caterers | **UC liên quan**: UC-075 - Request AI Menu Recommendations (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-064, BR-080, BR-156, BR-157 — **Lưu ý mâu thuẫn nguồn**: bảng Business Rules của UC-075 trong `Report 3.md` (dòng 2905) trích "BR-072", nhưng theo `business_rules.md` (nguồn BR chính thức đã chốt) BR-072 là "Chụp ảnh bằng chứng bắt buộc khi nhận hàng" (Ingredient Receiving), không liên quan trần chi phí thực đơn. Quy tắc "trần chi phí" đúng trong `business_rules.md` là **BR-080**. Screen Spec này dùng BR-080, không dùng BR-072.
**MSG liên quan**: MSG-073 (Warning — AI không khả dụng), MSG-051 (Warning — cờ dị ứng trong đề xuất)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ngữ cảnh gửi AI (tồn kho, mục tiêu dinh dưỡng, hồ sơ dị ứng, trần chi phí) | Hiển thị tóm tắt read-only | Tự động lấy từ hệ thống | BR-062, BR-064 | UC-075 Normal Flow bước 2 |
| 2 | Danh sách Option Card đề xuất món ăn | Card list (cuộn dọc) | Mỗi card: tên món, phân tích dinh dưỡng, danh sách nguyên liệu, cờ cảnh báo dị ứng | BR-156 | UC-075 Normal Flow bước 4; qua hard-filter dị ứng trước khi hiển thị |
| 3 | Cờ cảnh báo dị ứng trên từng Option Card | Chip màu `danger` | Hiện khi món chứa allergen trùng hồ sơ học sinh | BR-063, BR-156 | — |
| 4 | Nút "Áp dụng" trên từng Option Card | Button | Áp dụng gợi ý vào Create Menu Workspace đang mở | — | ScreenFlow: `AIPanel -->|"Apply Option"| CreateMenuWS` |
| 5 | Thông báo dịch vụ AI không khả dụng | MaterialBanner | Hiện khi Gemini AI Service không phản hồi | — | UC-075 Alternative Flow AF1, MSG-073 |
| 6 | Ghi chú lưu trữ hội thoại AI | Text nhỏ cuối panel | "Cuộc trao đổi này được lưu trữ để phục vụ tuân thủ" | BR-157 | UC-075 Postconditions |

### Trạng thái màn hình
- Loading: `Shimmer` cho Option Card trong lúc chờ Gemini AI phản hồi.
- Empty: Không có gợi ý phù hợp → thông báo + gợi ý điều chỉnh mục tiêu.
- Error: `AlertDialog`/`SnackBar` màu `danger` MSG-073 khi AI Service không khả dụng, kèm nút "Thử lại" và lối "Tạo thủ công".
- Success: Không cần snackbar riêng — áp dụng option cập nhật trực tiếp form Create Menu Workspace.

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Create Menu Workspace (nút "AI Assist") dạng `showModalBottomSheet` trượt từ dưới lên. Thoát: "Áp dụng" (đóng sheet, cập nhật form) hoặc vuốt xuống đóng không áp dụng.
**Bố cục**: Bottom sheet chiếm ~80% chiều cao màn hình, header tóm tắt ngữ cảnh AI, danh sách Option Card cuộn dọc, mỗi card có nút "Áp dụng" ở cuối card (thumb-friendly).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Menu Recommendations Panel (AI Integration), nền tảng Mobile, actor Service Caterers.
Bố cục: Bottom sheet trượt từ dưới lên, header tóm tắt ngữ cảnh AI, danh sách Option Card (tên món, dinh dưỡng, nguyên liệu, cờ dị ứng, nút Áp dụng).
Thành phần bắt buộc: chip cảnh báo dị ứng màu đỏ (SfmsStatusColors.danger), ghi chú lưu trữ hội thoại AI.
Áp dụng Design System: xem 00_Foundation/Design_System.md, ADR-003 (pipeline AI + hard-filter dị ứng).
Trạng thái cần thể hiện: loading, empty, error (MSG-073).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2889–2907 (UC-075 - Request AI Menu Recommendations)
- business_rules.md BR-064, BR-080, BR-156, BR-157, BR-063
- error_message.md MSG-073, MSG-051
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (hard-filter dị ứng, ADR-003)

# Screen Spec — Submit Menu For Approval Trigger

**Actor**: Service Caterers | **UC liên quan**: UC-087 - Submit Menu For Approval (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-061, BR-063, BR-069
**MSG liên quan**: MSG-051 (chặn — xung đột dị ứng), MSG-039 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt thực đơn (Menu Summary) | Read-only card | Hiển thị tuần, danh sách món, tổng dinh dưỡng | — | UC-087 Normal Flow bước 2 |
| 2 | Checklist kiểm tra trước khi nộp | Checklist read-only | Xác nhận đầy đủ nguyên liệu (BR-069), nộp tối thiểu 3 ngày trước tuần dùng (BR-061) | BR-061, BR-069 | UC-087 Normal Flow bước 2–3 |
| 3 | Kết quả kiểm tra xung đột dị ứng cuối cùng | MaterialBanner | Chạy lại lần cuối trước khi đổi trạng thái | BR-063 | UC-087 Normal Flow bước 4, MSG-051 |
| 4 | Nút "Xác nhận Nộp Phê duyệt" | FilledButton | Disabled nếu còn xung đột dị ứng nghiêm trọng chưa xử lý | BR-063 | UC-087 Exceptions |

### Trạng thái màn hình
- Loading: `CircularProgressIndicator` khi tải tóm tắt thực đơn.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005 nếu submit thất bại; MSG-051 chặn nếu còn xung đột nghiêm trọng — nút chuyển `disabled` kèm tooltip lý do.
- Success: `SnackBar` MSG-039, chuyển trạng thái thực đơn sang "Pending Approval".

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Submit" trên card Draft) hoặc từ cuối Create Menu Workspace (`04_Service_Caterers_Screen_Flow.md`: `SubmitTrigger -.-> SchoolAdminMenu`). Thoát: sau khi xác nhận, chuyển giao cross-actor sang School Administration (Web) để phê duyệt.
**Bố cục**: `showModalBottomSheet` chứa tóm tắt + checklist + banner cảnh báo + nút hành động, theo `00_Foundation/Layout_Guidelines.md` mục 2.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Menu For Approval Trigger, nền tảng Mobile, actor Service Caterers.
Bố cục: Bottom sheet gồm tóm tắt thực đơn, checklist tự động, banner cảnh báo dị ứng, nút Xác nhận Nộp Phê duyệt.
Thành phần bắt buộc: checklist trạng thái đạt/chưa đạt, banner MSG-051 nếu có xung đột, nút hành động disabled khi bị chặn.
Áp dụng Design System: banner cảnh báo dùng SfmsStatusColors.danger khi chặn, warning khi chỉ cảnh báo.
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

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — `Report 3.md` UC-096 trích thêm BR-072/BR-074 vốn thuộc module Ingredient Receiving, không áp dụng cho màn này (không đưa vào Screen Spec). "Giới hạn lặp món chính 2 lần/tuần" (MSG-075) hiện **chưa có mã BR chính thức khớp** trong `business_rules.md` (BR-068 hiện tại là quy trình phê duyệt, không phải dish rotation) — **[Giả định — cần team xác nhận]** giữ nội dung MSG-075 nhưng không gán số BR cho tới khi team xác nhận.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Form chỉnh sửa thực đơn (pre-filled) | Form nhiều trang | Chỉ cho phép khi status = Draft hoặc Rejected | BR-067 | UC-096 Preconditions |
| 2 | Bình luận từ chối (nếu status = Rejected) | Read-only text block | Hiển thị lý do từ chối gần nhất | — | **[Giả định — cần team xác nhận]** định dạng hiển thị |
| 3 | Ngày phục vụ / Chọn món | DatePicker + Checkbox list | Cùng ràng buộc như Create Menu Workspace | BR-061, BR-062 | UC-096 Normal Flow bước 3 |
| 4 | Cảnh báo giới hạn lặp món chính | MaterialBanner | Hiện khi 1 món chính được xếp lịch quá 2 lần/tuần | *(xem lưu ý mâu thuẫn nguồn)* | MSG-075 |
| 5 | Kiểm tra khai báo nguyên liệu | Validation tự động | Bắt buộc đầy đủ trước khi lưu | BR-069 | UC-096 Normal Flow bước 4 |
| 6 | Kết quả kiểm tra xung đột dị ứng | MaterialBanner | Tự động chạy lại sau khi sửa | BR-063 | UC-096 Normal Flow bước 5, MSG-051 |
| 7 | Nút "AI Assist" thay món xung đột | Button | Mở View Menu Recommendations Panel (UC-075) | BR-064, BR-156, BR-157 | UC-096 Alternative Flow AF1 |
| 8 | Nút "Lưu thay đổi" | FilledButton (cố định cuối màn) | Lưu, hiển thị MSG-074 | — | UC-096 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải form pre-filled.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005 nếu sửa/lưu thất bại; chặn hoàn toàn nếu cố sửa menu đã Approved/Published (BR-067).
- Success: `SnackBar` MSG-074 khi lưu thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Menu Management Dashboard (hành động "Edit" trên card Draft/Rejected — `04_Service_Caterers_Screen_Flow.md`). Thoát: "Lưu thay đổi" quay lại Menu Management Dashboard.
**Bố cục**: Form nhiều trang riêng giống Create Menu Workspace, pre-filled dữ liệu hiện có, banner bình luận từ chối ở đầu trang 1 nếu status = Rejected.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Menu Screen, nền tảng Mobile, actor Service Caterers.
Bố cục: giống Create Menu Workspace (nhiều trang tuần tự) nhưng pre-filled dữ liệu, thêm banner bình luận từ chối nếu có.
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
| 1 | Danh sách lịch giao hàng hôm nay | Card list | Mỗi card: Nhà cung cấp, Giờ dự kiến, Trạng thái | BR-078 | UC-044 Normal Flow bước 2 |
| 2 | Trạng thái giao hàng | Chip | Scheduled / Dispatched / In Progress / Completed | BR-078 | UC-044 Normal Flow bước 2 |
| 3 | Nút "Bắt đầu Nhận hàng" trên card Dispatched | Button | Chỉ hiện khi trạng thái = Dispatched; điều hướng Receive Ingredients Workspace | BR-050 | UC-044 Normal Flow bước 3 |
| 4 | Nút "Xem Lịch sử" | Button (AppBar action) | Điều hướng Receiving History & Reports | — | ScreenFlow: `RecvDash -->|"View History"| RecvHist` |
| 5 | Bộ lọc lịch sử theo ngày/nhà cung cấp | Filter chip row | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách lịch giao hàng.
- Empty: Không có lịch giao hàng hôm nay → illustration + gợi ý xem lịch sử.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Nhận hàng" trong Grid Menu Items (`04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Receive Ingredients Workspace, Receiving History & Reports.
**Bố cục**: AppBar (tiêu đề + icon "Lịch sử") + Card list cuộn dọc (pull-to-refresh), mỗi card có nút "Bắt đầu Nhận hàng" khi đủ điều kiện.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar (tiêu đề + icon Lịch sử) + Card list (Nhà cung cấp, Giờ dự kiến, Trạng thái chip, nút Bắt đầu Nhận hàng).
Thành phần bắt buộc: chip trạng thái theo 4 giá trị.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044 - Manage Ingredient Receiving)
- business_rules.md BR-050, BR-078
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md`

# Screen Spec — Receive Ingredients Workspace (Start Receiving Session)

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (khởi tạo phiên), UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071, BR-078, BR-079, BR-080
**MSG liên quan**: MSG-015 (Error — GPS ngoài khu vực), MSG-041 (Success), MSG-077 (Error — cần kết nối mạng), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin phiên nhận hàng (nhà cung cấp, giờ dự kiến) | Read-only header | Lấy từ Receiving Management Dashboard | BR-078 | UC-060 Trigger |
| 2 | Xác minh trạng thái Approved của nhà cung cấp | Kiểm tra tự động | **Bắt buộc kết nối mạng** — chặn hoàn toàn nếu offline, hiển thị MSG-077 | BR-070, BR-197 | UI_Design_Guidelines.md mục 5.2 |
| 3 | Xác minh GPS trong bán kính cho phép | Kiểm tra tự động (GPS thiết bị) | Trong 50m khu vực cổng trường; ngoài dung sai hiển thị cảnh báo `warning`, cho xác nhận thủ công kèm lý do (ghi audit log) | BR-079 | UC-060 Normal Flow bước 2, MSG-015 |
| 4 | Xác thực khuôn mặt nhân viên giao hàng | Camera scan (native) → Verify Supplier Delivery Panel | **Bắt buộc online** | BR-050, BR-197 | UC-060 Normal Flow bước 3 |
| 5 | Danh sách hành động con của phiên (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) | Danh sách bước dạng Card | Điều hướng tới màn con tương ứng | BR-071, BR-080 | ScreenFlow: `RecvWS` branches |
| 6 | Nút "Hoàn tất Phiên Nhận hàng" | FilledButton (cố định cuối màn) | Chỉ bật khi đã hoàn tất các bước bắt buộc | — | UC-060 Postconditions |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực) dạng `AlertDialog`; chặn hoàn toàn nếu nhà cung cấp không Approved hoặc mất kết nối (BR-070/BR-197, MSG-077) — nút hành động chuyển `disabled` kèm tooltip.
- Success: `SnackBar` MSG-041 sau khi hoàn tất phiên.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Receiving Management Dashboard (nút "Bắt đầu Nhận hàng" — `04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Verify Supplier Delivery Panel, Capture Receiving Evidence Screen, Reject Ingredient Batch Form, Record Ingredient Receiving.
**Bố cục**: 1 cột, mỗi bước là 1 `Card` có trạng thái (Chưa làm/Đang làm/Hoàn tất), thứ tự Verify → Inspect Quality → Capture Evidence → (Reject nếu cần) → Send Receipt; nút "Hoàn tất Phiên" cố định cuối màn. Trường cần camera/GPS đặt ưu tiên đầu luồng theo `Layout_Guidelines.md` mục 2.2. Cho phép ghi nhận ảnh bằng chứng offline theo BR-196 (badge "Chờ đồng bộ" nếu mất mạng khi ở bước Capture Evidence), nhưng bước xác minh Approved/GPS/khuôn mặt vẫn chặn nếu offline (BR-197).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receive Ingredients Workspace, nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột, danh sách bước dạng Card (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) với trạng thái từng bước, nút Hoàn tất Phiên cố định dưới cùng.
Thành phần bắt buộc: banner xác minh trạng thái Approved nhà cung cấp, banner GPS, khối xác thực khuôn mặt (camera), badge offline "Chờ đồng bộ" nếu áp dụng.
Áp dụng Design System: SfmsStatusColors.danger cho banner chặn (MSG-015/MSG-077), SfmsStatusColors.warning cho badge chờ đồng bộ (MSG-078).
Trạng thái cần thể hiện: loading, error (chặn do GPS/trạng thái NCC/offline), success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044), dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071, BR-078, BR-079, BR-080, BR-196, BR-197
- error_message.md MSG-015, MSG-041, MSG-077, MSG-078

# Screen Spec — Verify Supplier Delivery Panel

**Actor**: Service Caterers | **UC liên quan**: UC-060 - Record Ingredient Receiving (Report 3 §3.2, sub-bước "Verify Invoice") | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071
**MSG liên quan**: MSG-013 (Error — chứng nhận ATTP hết hạn), MSG-014 (Error — xác thực khuôn mặt thất bại), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin nhà cung cấp (tên, trạng thái Approved) | Read-only card | Chặn nếu không Approved; **bắt buộc online** (BR-197) | BR-070 | UC-044/UC-060 |
| 2 | Danh sách chứng nhận lô hàng (số lô, ngày sản xuất, hạn dùng, số lượng, chứng chỉ) | Read-only list card | Bắt buộc đầy đủ | BR-071 | UC-060 Normal Flow bước 4 |
| 3 | Kết quả xác thực khuôn mặt nhân viên giao hàng (camera thiết bị) | Camera capture + trạng thái Passed/Failed | Nếu Failed → MSG-014, chuyển luồng "Manual Override" | BR-050 | UC-060 Normal Flow bước 3; `ADR-002.md` |
| 4 | Nút "Xác nhận Hồ sơ Hợp lệ" | FilledButton | Điều hướng tiếp sang Inspect Quality/Capture Evidence | — | — |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin chứng nhận.
- Empty: Không áp dụng.
- Error: MSG-013 nếu chứng nhận ATTP hết hạn (chặn xác nhận); MSG-014 nếu xác thực khuôn mặt thất bại → luồng Manual Override (nhập lý do + ảnh bằng chứng).
- Success: Chuyển sang bước tiếp theo trong Receive Ingredients Workspace.

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (bước "Verify Invoice"). Thoát: xác nhận hợp lệ → quay lại Workspace, chuyển bước kế tiếp.
**Bố cục**: Trang riêng trong luồng nhiều trang: card thông tin nhà cung cấp + list chứng nhận, khối camera xác thực khuôn mặt full-width (kênh chính cho thao tác hiện trường), nút "Xác nhận Hồ sơ Hợp lệ" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Verify Supplier Delivery Panel, nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột gồm card thông tin nhà cung cấp, list chứng nhận lô hàng, khối camera xác thực khuôn mặt, nút Xác nhận Hồ sơ Hợp lệ cố định dưới cùng.
Thành phần bắt buộc: badge trạng thái Approved, cảnh báo MSG-013/MSG-014 khi lỗi, luồng Manual Override khi xác thực khuôn mặt thất bại.
Áp dụng Design System: xem 00_Foundation/Design_System.md, ADR-002.md (Face Biometric).
Trạng thái cần thể hiện: loading, error (MSG-013/MSG-014 + Manual Override), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071, BR-197
- error_message.md MSG-013, MSG-014
- `03_Design/02_ADR/ADR-002.md` (Face Biometric, Manual Override)

# Screen Spec — Capture Receiving Evidence Screen

**Actor**: Service Caterers | **UC liên quan**: bundled UC-044 (theo `14_Screen_Authorization_Matrix.md`), chi tiết hoá từ UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-072, BR-073, BR-074, BR-079, BR-196
**MSG liên quan**: MSG-008 (Error), MSG-009 (Error), MSG-015 (Error), MSG-041 (Success), MSG-078 (Success — lưu offline)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ảnh bằng chứng nguyên liệu, nhãn, bao bì, tài liệu | Chụp ảnh trực tiếp (camera, `image_picker`) + preview | Bắt buộc tối thiểu 1 ảnh; giới hạn dung lượng theo `Component_Guidelines.md` (TODO #11: 5MB vs 10MB) | BR-072 | UC-060 Normal Flow bước 5 |
| 2 | Toạ độ GPS + timestamp | Tự động ghi nhận (GPS thiết bị), hiển thị read-only | Không cho sửa tay; xác minh trong khu vực nhận hàng cho phép | BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 2/5 |
| 3 | Thông tin thiết bị (device info) | Tự động ghi nhận (ẩn, chỉ log audit) | — | BR-073 | Biện pháp chống gian lận |
| 4 | Nút "Lưu Bằng chứng" | FilledButton | Lưu; nếu online hiển thị MSG-041, nếu offline hiển thị MSG-078 + badge "Chờ đồng bộ" | BR-196 | Cho phép offline (evidence capture) theo BR-196 |

### Trạng thái màn hình
- Loading: `Shimmer` cho vùng preview ảnh.
- Empty: Chưa chụp ảnh nào → placeholder khung ảnh có dấu "X" + hướng dẫn chụp.
- Error: MSG-008/MSG-009 khi ảnh không hợp lệ; MSG-015 nếu GPS ngoài khu vực cho phép (cho xác nhận thủ công kèm lý do, ghi audit log).
- Success: `SnackBar` MSG-041 (online) hoặc MSG-078 kèm badge "Chờ đồng bộ" màu vàng/cam (offline, theo `UI_Design_Guidelines.md` mục 5.1).

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (bước "Capture Evidence"). Thoát: "Lưu Bằng chứng" → quay lại Workspace.
**Bố cục**: Camera view chiếm phần lớn màn hình (kênh chính, độ tin cậy chống gian lận cao nhất), dải thumbnail ảnh đã chụp bên dưới, khối GPS/timestamp hiển thị góc trên, nút "Lưu Bằng chứng" cố định cuối màn. Cho phép ghi nhận offline (BR-196) — hiện badge "Chờ đồng bộ" (vàng/cam) nếu mất kết nối khi lưu, chuỗi trạng thái `Pending Sync → Syncing → Synced`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Capture Receiving Evidence Screen, nền tảng Mobile, actor Service Caterers.
Bố cục: Camera view lớn, dải thumbnail ảnh đã chụp, khối GPS/timestamp read-only, nút Lưu Bằng chứng cố định dưới cùng, badge trạng thái đồng bộ.
Thành phần bắt buộc: cảnh báo dung lượng/định dạng file (MSG-008/MSG-009), cảnh báo GPS ngoài khu vực (MSG-015), badge offline (MSG-078).
Áp dụng Design System: ảnh dùng aspect-ratio cố định 4:3 hoặc 1:1 theo Responsive_Guidelines.md mục 4; badge màu warning cho "Chờ đồng bộ".
Trạng thái cần thể hiện: loading, empty, error, success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-072, BR-073, BR-074, BR-079, BR-196
- error_message.md MSG-008, MSG-009, MSG-015, MSG-041, MSG-078
- `14_Screen_Authorization_Matrix.md` (ghi chú "bundled UC-044", "Mobile ưu tiên — GPS/camera")

# Screen Spec — Store Ingredients Form (Storage Assignment)

**Actor**: Service Caterers | **UC liên quan**: UC-061 - Record Ingredient Storage (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-202, BR-090
**MSG liên quan**: MSG-004/MSG-005 (Error), MSG-041 (Success)

> **✅ Đã xử lý (2026-07-01)**: rule "khu vực lưu trữ phải khớp nhiệt độ" trước đây trích nhầm là "BR-081" — số này đã bị chiếm bởi 1 rule khác (Menu & Diet Management, nay đã xoá). Đã chuyển sang mã mới **BR-202** trong `business_rules.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô hàng đã nhận, chờ xếp kho | Card list | Chỉ hiển thị batch đã Received nhưng chưa Stored | — | UC-061 Preconditions |
| 2 | Quét mã vạch lô hàng | `mobile_scanner`/`qr_code_scanner` | Bắt buộc | BR-090 | UC-061 Normal Flow bước 2 |
| 3 | Chọn khu vực lưu trữ (Dry/Chilled/Frozen) | RadioListTile | Phải khớp nhóm nhiệt độ yêu cầu | BR-202 | UC-061 Normal Flow bước 3–4 |
| 4 | Nút "Xác nhận Vị trí Lưu trữ" | FilledButton | Chặn nếu khu vực không khớp nhiệt độ yêu cầu | BR-202 | UC-061 Exceptions |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải danh sách lô hàng chờ xếp kho.
- Empty: Không còn lô hàng nào chờ xếp kho.
- Error: `AlertDialog` màu `danger` khi chọn sai khu vực nhiệt độ (BR-202) — chặn lưu.
- Success: `SnackBar` xác nhận lưu vị trí thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ cuối Record Ingredient Receiving (`04_Service_Caterers_Screen_Flow.md`: `RecordRecv --> StoreForm`). Thoát: xác nhận xong quay lại Receiving Management Dashboard.
**Bố cục**: Trang riêng — quét mã vạch ưu tiên đầu form (camera scanner full-width), danh sách khu vực lưu trữ dạng `RadioListTile` bên dưới, nút xác nhận cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Store Ingredients Form (Storage Assignment), nền tảng Mobile, actor Service Caterers.
Bố cục: khối quét mã vạch camera full-width, danh sách RadioListTile chọn khu vực lưu trữ, nút Xác nhận Vị trí Lưu trữ cố định dưới cùng.
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
**BR liên quan**: BR-078
**MSG liên quan**: MSG-015 (Error), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — Report 3 UC-072 không có mã MSG Success riêng cho reject; dùng thông báo xác nhận chung không gắn mã MSG. **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin lô hàng đang kiểm tra | Read-only card | Lấy từ phiên nhận hàng đang hoạt động | — | UC-072 Preconditions |
| 2 | Lý do từ chối (mã lý do bắt buộc) | Checkbox list | Bắt buộc chọn ít nhất 1 lý do | BR-078 | UC-072 Normal Flow bước 3–4 |
| 3 | Ảnh bằng chứng lô hàng bị từ chối | Camera capture | Khuyến nghị bắt buộc | BR-078 | UC-072 Postconditions |
| 4 | Tuỳ chọn "Từ chối một phần" (Partial Rejection) | Switch + TextField số lượng | Khi bật: tạo 2 bản ghi batch riêng | — | UC-072 Alternative Flow AF1 |
| 5 | Nút "Xác nhận Từ chối" | FilledButton màu `danger` + `AlertDialog` xác nhận | Không thể hoàn tác | — | UC-072 Normal Flow bước 4–5 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin lô hàng.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005/MSG-015 nếu thao tác thất bại.
- Success: `SnackBar` xác nhận batch đã chuyển trạng thái "Rejected".

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (bước "Log Rejections"). Thoát: xác nhận xong quay lại Workspace.
**Bố cục**: `showModalBottomSheet` form ngắn — thông tin lô hàng, checklist lý do, camera chụp ảnh, nút xác nhận màu `danger` ở cuối.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Reject Ingredient Batch Form, nền tảng Mobile, actor Service Caterers.
Bố cục: Bottom sheet gồm thông tin lô hàng, checklist lý do từ chối, camera chụp ảnh bằng chứng, tuỳ chọn từ chối một phần, nút Xác nhận Từ chối màu danger kèm AlertDialog xác nhận.
Thành phần bắt buộc: nút hành động màu danger theo SfmsStatusColors.
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
| 1 | Tóm tắt toàn bộ phiên nhận hàng (nhà cung cấp, chứng nhận, kết quả kiểm tra, ảnh bằng chứng, GPS/timestamp) | Read-only summary card | Tổng hợp từ các bước trước | BR-071, BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 6 |
| 2 | Kiểm tra nhiệt độ chuỗi lạnh (cold chain) | Read-only kết quả | Cảnh báo nếu > 5°C | BR-080 | UC-060 Alternative Flow AF1 |
| 3 | Số lô liên kết truy xuất nguồn gốc | Read-only text | Ghi nhận số lô gắn với thực đơn/phiên sản xuất | BR-076 | — |
| 4 | Nút "Xác nhận Hoàn tất Phiên Nhận hàng" | FilledButton (cố định cuối màn) | Tạo bản ghi batch traceability, cập nhật kho | — | UC-060 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tổng hợp dữ liệu phiên.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005 nếu lưu thất bại.
- Success: `SnackBar` MSG-041.

---

## Platform Layout — Mobile

**Entry/Exit points**: Bước cuối trong Receive Ingredients Workspace. Thoát: sau khi xác nhận, điều hướng tự động sang Store Ingredients Form.
**Bố cục**: Trang tóm tắt 1 cột, mỗi nhóm dữ liệu là 1 `Card` (Nhà cung cấp, Chứng nhận, Kết quả kiểm tra, Bằng chứng), nút xác nhận cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Ingredient Receiving, nền tảng Mobile, actor Service Caterers.
Bố cục: trang tóm tắt 1 cột nhiều Card (Nhà cung cấp, Chứng nhận, Kết quả kiểm tra, Bằng chứng, GPS/timestamp), nút Xác nhận Hoàn tất Phiên Nhận hàng cố định dưới cùng.
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
| 1 | Danh sách lịch sử nhận hàng | Card list | Mỗi card: Ngày, Nhà cung cấp, Số lô, Trạng thái | BR-076 | UC-044 Normal Flow bước 4 |
| 2 | Bộ lọc theo ngày/nhà cung cấp | Filter chip row | Tuỳ chọn | — | UC-044 Normal Flow bước 4 |
| 3 | Xem chi tiết 1 bản ghi lịch sử | Điều hướng chi tiết (read-only) | — | — | Truy xuất nguồn gốc BR-076 |
| 4 | Xuất báo cáo (PDF/Excel) | Button | Chỉ xem báo cáo liên quan hoạt động của chính Service Caterers | BR-175, BR-176, BR-178 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách lịch sử.
- Empty: Chưa có lịch sử nhận hàng nào trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Receiving Management Dashboard (icon "Lịch sử" trên AppBar). Thoát: back về Dashboard.
**Bố cục**: AppBar (tiêu đề + icon xuất báo cáo) + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving History & Reports, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list lịch sử nhận hàng.
Thành phần bắt buộc: chip trạng thái Received/Rejected.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
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
| 1 | Danh sách phiên sản xuất bữa ăn đang hoạt động | Card list | Mỗi card: Mẻ sản xuất, Bước hiện tại (1/2/3), Trạng thái | — | UC-052 Trigger + Normal Flow |
| 2 | Trạng thái từng bước kiểm tra | Chip (Chưa bắt đầu/Đang thực hiện/Passed/Failed) | Bước 3 bị khoá cho tới khi Bước 2 = Passed | BR-093 | UC-052 Normal Flow bước 3 |
| 3 | Nút "Bắt đầu/Tiếp tục Kiểm tra" | FilledButton | Điều hướng tới đúng Step 1/2/3 | — | UC-052 Normal Flow |
| 4 | Trạng thái đồng ký số (co-sign) Catering Manager + School Representative | Chip kép | Bắt buộc cả 2 bên ký trước khi phê duyệt phục vụ | BR-099 | UC-052 Normal Flow bước 5 |
| 5 | Cảnh báo thất bại kiểm tra + Corrective Action liên kết | MaterialBanner | Hiện khi có bước Failed | BR-097, BR-098 | UC-052 Alternative Flow AF1, MSG-056 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách phiên sản xuất.
- Empty: Chưa có phiên sản xuất nào trong ngày.
- Error: MSG-016, MSG-017, `AlertDialog`/`SnackBar` màu `danger`.
- Success: `SnackBar` MSG-043 khi hoàn tất nộp checklist.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Kiểm tra 3 Bước" trong Grid Menu Items (kênh chính tại bếp — `04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Step 1/2/3, View Preparation Records Ledger, View Inspection Records Log.
**Bố cục**: AppBar + Card list cuộn dọc (pull-to-refresh), mỗi card hiện Mẻ/Bước hiện tại/Trạng thái/nút hành động; FAB "Bắt đầu Kiểm tra Mới" nếu có phiên chưa khởi tạo.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Three-Step Inspection Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list (Mẻ, Bước hiện tại, Trạng thái chip, Co-sign, nút Bắt đầu/Tiếp tục Kiểm tra), FAB nếu cần.
Thành phần bắt buộc: chip trạng thái 4 giá trị, banner cảnh báo khi có bước Failed (MSG-056).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error (MSG-016/017), success (MSG-043).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3487–3505 (UC-052 - Manage Three-Step Food Inspection)
- business_rules.md BR-091–BR-099
- error_message.md MSG-016, MSG-017, MSG-043, MSG-056

# Screen Spec — Inspect Ingredients Before Processing Screen (Step 1)

**Actor**: Service Caterers | **UC liên quan**: UC-062 - Record Ingredients Before Processing (Report 3 §3.2, "Conduct Step 1 — Sample No. 1") | **Status**: Draft
**BR liên quan**: BR-076, BR-079, BR-083, BR-090, BR-091, BR-092, BR-196
**MSG liên quan**: MSG-015 (Error — GPS), MSG-078 (Success — lưu offline), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Quét mã vạch nguyên liệu/dry ingredient để xuất kho | Camera scanner | Bắt buộc | BR-090 | UC-062 Normal Flow bước 2 |
| 2 | Với nguyên liệu tươi/đông lạnh: giờ giao, khối lượng, nhà cung cấp, chứng nhận thú y/kiểm dịch | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 3 | Với nguyên liệu khô/đóng gói: nhà sản xuất, hạn dùng, điều kiện lưu trữ | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 4 | Kiểm tra cảm quan (màu sắc, mùi, hình thức) | Checklist (CheckboxListTile) | Bắt buộc đủ mục — có thể điền offline (BR-196) | BR-095, BR-196 | UC-062 Normal Flow bước 2 |
| 5 | Kiểm tra hạn sử dụng + FEFO | Validation tự động | Chặn nếu nguyên liệu hết hạn | BR-076, BR-083 | UC-062 Normal Flow bước 2 |
| 6 | Ảnh bằng chứng nguyên liệu + chứng nhận | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-083, BR-196 | UC-062 Normal Flow bước 2 |
| 7 | GPS + timestamp | Tự động ghi nhận | Trong khu vực hợp lệ của trường | BR-079 | UC-062 Preconditions |
| 8 | Nút "Nộp Bước 1" | FilledButton (cố định cuối màn) | Nếu đạt → mở khoá Step 2; nếu Failed → tạo Corrective Action, chặn tiến trình | BR-097 | UC-062 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên sản xuất.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực); chặn tiến trình + tạo Corrective Action Request nếu Failed (BR-097).
- Success: `SnackBar` MSG-043 (online) hoặc MSG-078 + badge "Chờ đồng bộ" (offline) — chuyển sang Step 2.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard (nút "Bắt đầu/Tiếp tục Kiểm tra" khi ở Bước 1) — kênh chính tại bếp. Thoát: nộp thành công → Step 2 (Upload Image Cooked Food Screen).
**Bố cục**: Trường cần camera/GPS đặt ưu tiên đầu form (quét mã vạch, chụp ảnh) theo `Layout_Guidelines.md` mục 2.2, checklist cảm quan và form nguyên liệu bên dưới, nút "Nộp Bước 1" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Ingredients Before Processing Screen (Step 1), nền tảng Mobile, actor Service Caterers.
Bố cục: quét mã vạch + chụp ảnh ưu tiên đầu form, form nguyên liệu tươi/khô, checklist cảm quan, khối GPS/timestamp, nút Nộp Bước 1 cố định dưới cùng.
Thành phần bắt buộc: cảnh báo hết hạn/FEFO, cảnh báo GPS (MSG-015), badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + tạo Corrective Action), success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3535–3549 (UC-062 - Record Ingredients Before Processing, mục "Conduct Step 1")
- business_rules.md BR-076, BR-079, BR-083, BR-090, BR-091, BR-092, BR-196
- error_message.md MSG-015, MSG-078

# Screen Spec — Upload Image Cooked Food Screen (Step 2)

**Actor**: Service Caterers | **UC liên quan**: UC-063 - Record Ingredients In Processing (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1070) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-093, BR-095, BR-097, BR-196
**MSG liên quan**: MSG-017 (Error — nhiệt độ nấu chưa đạt), MSG-078 (Success — lưu offline), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: giống bản Web — UC-063 không có bảng đặc tả UC đầy đủ riêng trong `Report 3.md`, chỉ có 1 dòng khai báo trong bảng tổng hợp UC (dòng 1070). Nội dung dưới đây suy ra từ **Step 2 trong Normal Flow của UC-062** (dòng 3542). **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên món / Meal Slot | Read-only | — | — | UC-062 Step 2 |
| 2 | Thời điểm bắt đầu/kết thúc sơ chế và nấu | Time picker (M3) | Bắt buộc | — | UC-062 Step 2 |
| 3 | Checklist điều kiện vệ sinh (nhân viên, thiết bị, khu vực sơ chế) | CheckboxListTile | Bắt buộc đủ mục — có thể điền offline | BR-095, BR-196 | UC-062 Step 2 |
| 4 | Nhiệt độ lõi thức ăn sau nấu | TextField số (°C) | Phải ≥ 75°C trong ≥ 15 giây | BR-093 | UC-062 Step 2, MSG-017 nếu không đạt |
| 5 | Ảnh món ăn sau khi nấu | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-090, BR-196 | Tên màn hình gốc "Upload Image Cooked Food" |
| 6 | GPS + timestamp | Tự động ghi nhận | — | BR-079 | UC-062 Step 2 |
| 7 | Nút "Nộp Bước 2" | FilledButton (cố định cuối màn) | Nếu nhiệt độ < 75°C → chặn, MSG-017, tạo Corrective Action; nếu đạt → mở khoá Step 3 | BR-097 | UC-052 Normal Flow bước 3 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-017 khi nhiệt độ chưa đạt — chặn, tạo Corrective Action Request.
- Success: `SnackBar` MSG-043 (online) hoặc MSG-078 + badge "Chờ đồng bộ" (offline) — chuyển sang Step 3.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 1 = Passed. Thoát: nộp thành công → Step 3 (Inspect Meals Before Serving Screen).
**Bố cục**: Camera chụp ảnh món ăn ưu tiên đầu form, input nhiệt độ nổi bật (số lớn, cảnh báo màu ngay khi < 75°C), checklist vệ sinh bên dưới, nút "Nộp Bước 2" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Image Cooked Food Screen (Step 2), nền tảng Mobile, actor Service Caterers.
Bố cục: camera chụp ảnh món ăn ưu tiên đầu form, input nhiệt độ lõi nổi bật, checklist vệ sinh, khối GPS/timestamp, nút Nộp Bước 2 cố định dưới cùng.
Thành phần bắt buộc: cảnh báo nhiệt độ chưa đạt 75°C (MSG-017) hiển thị màu danger ngay khi nhập, badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 2") — xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-093, BR-095, BR-097, BR-196
- error_message.md MSG-017, MSG-078

# Screen Spec — Inspect Meals Before Serving Screen (Step 3)

**Actor**: Service Caterers | **UC liên quan**: UC-065 - Record Meals Before Serving (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1072) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099, BR-196
**MSG liên quan**: MSG-016 (Error), MSG-043 (Success), MSG-078 (Success — lưu offline), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: giống bản Web — UC-065 không có bảng đặc tả UC đầy đủ riêng trong `Report 3.md`. Nội dung dưới đây suy ra từ **Step 3 trong Normal Flow của UC-062** (dòng 3542). **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Số suất ăn (portion count) | TextField số | Bắt buộc | — | UC-062 Step 3 |
| 2 | Thời điểm chia suất / bắt đầu bữa ăn | Time picker (M3) | Bắt buộc | — | UC-062 Step 3 |
| 3 | Mô tả dụng cụ chứa (sạch, có nắp đậy) | CheckboxListTile | Bắt buộc | BR-095 | UC-062 Step 3 |
| 4 | Nhiệt độ giữ ấm | TextField số (°C) | Phải ≥ 60°C | BR-094 | UC-062 Step 3, chặn nếu không đạt |
| 5 | Kiểm tra cảm quan trước khi ăn | CheckboxListTile | Bắt buộc | BR-095 | UC-062 Step 3 |
| 6 | Ảnh bằng chứng bữa ăn hoàn thiện | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-090, BR-196 | UC-062 Step 3 |
| 7 | Đồng ký số (Catering Manager + School Representative) | Chữ ký số kép (Signature pad) | Bắt buộc cả 2 bên | BR-099 | UC-062 bước 5 "Co-sign & Approve" |
| 8 | Nút "Nộp Bước 3 & Phê duyệt Phục vụ" | FilledButton (cố định cuối màn) | Nếu đạt đủ 3 bước → chuyển "Approved for Serving" | BR-098 | UC-052 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-016 nếu checklist chưa đầy đủ; chặn phê duyệt nếu nhiệt độ giữ ấm < 60°C — tạo Corrective Action.
- Success: `SnackBar` MSG-043 khi bữa ăn được phê duyệt phục vụ.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 2 = Passed. Thoát: nộp thành công + đồng ký số → hoàn tất phiên kiểm tra.
**Bố cục**: Camera chụp ảnh + input nhiệt độ giữ ấm ưu tiên đầu form, checklist bên dưới, khối chữ ký số kép (2 signature pad tuần tự) cuối màn, nút "Nộp Bước 3 & Phê duyệt Phục vụ" cố định cuối.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Meals Before Serving Screen (Step 3), nền tảng Mobile, actor Service Caterers.
Bố cục: camera + input nhiệt độ giữ ấm ưu tiên đầu form, checklist dụng cụ + cảm quan, khối chữ ký số kép, nút Nộp Bước 3 & Phê duyệt Phục vụ cố định dưới cùng.
Thành phần bắt buộc: cảnh báo nhiệt độ giữ ấm chưa đạt 60°C, yêu cầu đủ 2 chữ ký số, badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 3" + "Co-sign & Approve") — xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099, BR-196
- error_message.md MSG-016, MSG-043, MSG-078

# Screen Spec — Record Preparation Information Form (Cooking Record)

**Actor**: Service Caterers | **UC liên quan**: UC-066 - Record Prepare Ingredient Information (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: Không có BR trực tiếp (Report 3 UC-066 ghi "Business Rules: None")
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Phiên sản xuất đang hoạt động | Read-only (chọn từ danh sách) | Bắt buộc chọn đúng phiên | — | UC-066 Normal Flow bước 1 |
| 2 | Hành động sơ chế đã thực hiện (rửa, khử trùng, gọt, cắt) | CheckboxListTile | Bắt buộc chọn ít nhất 1 hành động | — | UC-066 Normal Flow bước 2 |
| 3 | Nồng độ dung dịch khử trùng (nếu áp dụng) | TextField số | Tuỳ chọn (chỉ hiện khi chọn "khử trùng") | — | UC-066 Normal Flow bước 3 |
| 4 | Nhân viên chịu trách nhiệm | DropdownMenu | Bắt buộc | — | UC-066 Normal Flow bước 3 |
| 5 | Nút "Lưu Ghi chép Sơ chế" | FilledButton | Lưu, cập nhật audit trail | — | UC-066 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên sản xuất.
- Empty: Không áp dụng.
- Error: `AlertDialog`/`SnackBar` màu `danger` MSG-004/MSG-005 nếu lưu thất bại.
- Success: `SnackBar` xác nhận lưu ghi chép sơ chế thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ nhánh "Prepare Ingredients → Record Cooking Information" trước khi vào Three-Step Inspection Dashboard (`04_Service_Caterers_Screen_Flow.md`). Thoát: lưu xong tiếp tục sang Three-Step Inspection Dashboard.
**Bố cục**: Trang riêng, checklist hành động sơ chế đầu trang, input nồng độ khử trùng + select nhân viên bên dưới, nút "Lưu Ghi chép Sơ chế" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Preparation Information Form (Cooking Record), nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột gồm checklist hành động sơ chế, input nồng độ khử trùng (điều kiện), dropdown nhân viên, nút Lưu Ghi chép Sơ chế cố định dưới cùng.
Thành phần bắt buộc: không có cảnh báo BR đặc thù (UC-066 không có BR).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3221–3239 (UC-066 - Record Prepare Ingredient Information)
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Prepare Ingredients → Record Cooking Information)

# Screen Spec — View Preparation Records Ledger

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình tổng hợp xuất ra sau khi hoàn tất Step 3 (`04_Service_Caterers_Screen_Flow.md`: `Step3 --> PrepLedger`), liên quan chặt tới UC-066 và UC-052 | **Status**: Draft
**BR liên quan**: BR-076, BR-093, BR-099
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên sơ chế/sản xuất đã hoàn tất | Card list | Mỗi card: Mẻ, Món, Nhân viên chịu trách nhiệm, Ngày | — | Tổng hợp từ UC-066 + Step 1-3 |
| 2 | Liên kết số lô nguyên liệu đã dùng | Read-only link | Truy xuất nguồn gốc | BR-076 | — |
| 3 | Trạng thái đồng ký số của phiên | Chip | Đã ký / Chưa ký đủ | BR-099 | Kế thừa từ Step 3 |
| 4 | Bộ lọc theo ngày/món | Filter chip row | Tuỳ chọn | — | — |
| 5 | Xem chi tiết 1 bản ghi | Điều hướng chi tiết (read-only) | — | BR-093 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Chưa có phiên nào hoàn tất trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ cuối Step 3 hoặc từ Three-Step Inspection Dashboard. Thoát: back về Dashboard.
**Bố cục**: AppBar + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Preparation Records Ledger, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list (Mẻ, Món, Nhân viên, Ngày, Trạng thái ký).
Thành phần bắt buộc: chip trạng thái đồng ký số.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Step3 --> PrepLedger)
- Report 3.md dòng 3221–3239 (UC-066), dòng 3487–3549 (UC-052/UC-062)
- business_rules.md BR-076, BR-093, BR-099

# Screen Spec — Record Sample Information Form

**Actor**: Service Caterers | **UC liên quan**: UC-069 - Register Food Sample (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-196, BR-203
**MSG liên quan**: MSG-019 (Error), MSG-020 (Error, xem lưu ý), MSG-044 (Success), MSG-078 (Success — offline), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — `Report 3.md` UC-069 AF2 ghi MSG-072 cho lỗi GPS, nhưng MSG-072 thực tế trong `error_message.md` là thông báo Success khác (lưu kế hoạch bữa ăn đặc biệt). Dùng **MSG-020** thay thế, **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Xác minh GPS trong khu vực bếp trường | Kiểm tra tự động (GPS thiết bị) | Chặn đăng ký nếu ngoài khu vực | BR-109 | UC-069 Normal Flow bước 2, AF2 |
| 2 | Bữa ăn / Món / Tổng số suất | Read-only | — | — | UC-069 Normal Flow bước 3 |
| 3 | Trọng lượng mẫu (gram) | TextField số | Bắt buộc ≥ 100g (đặc) / ≥150ml (lỏng) | BR-203 | MSG-019 nếu dưới ngưỡng *(sửa 2026-07-02: trước đây gán nhầm BR-107, đã repoint sang BR-203)* |
| 4 | Mô tả hộp/túi đựng mẫu | TextField multiline | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 5 | Nhiệt độ tủ lưu mẫu tại thời điểm lưu | TextField số (°C) | Trong khoảng 2–8°C | BR-109, BR-110 | UC-069 Normal Flow bước 3 |
| 6 | Thời điểm thu mẫu | Time picker (M3) | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 7 | In nhãn mẫu (Sample No 4) | Button "In nhãn" (kết nối máy in nhiệt Bluetooth nếu có) | Tự động sinh nội dung | BR-106 | UC-069 Normal Flow bước 4 |
| 8 | Ảnh hộp mẫu đã dán nhãn | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-107, BR-196 | UC-069 Normal Flow bước 5 |
| 9 | Chữ ký số người đăng ký | Signature pad | Bắt buộc | — | UC-069 Normal Flow bước 5 |
| 10 | Nút "Xác nhận Lưu mẫu" | FilledButton (cố định cuối màn) | Khởi động đồng hồ lưu mẫu 72 giờ | BR-111 | UC-069 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-019 (trọng lượng < 100g); MSG-020 (GPS/nhiệt độ ngoài phạm vi).
- Success: `SnackBar` MSG-044 (online) hoặc MSG-078 + badge "Chờ đồng bộ" (offline cho phần chụp ảnh/nhãn, xác minh GPS vẫn cần online theo BR-109/BR-197).

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Food Sample Retention Dashboard (FAB "Đăng ký Mẫu mới"). Thoát: lưu xong → Food Sample Detail.
**Bố cục**: Camera + GPS ưu tiên đầu form, input trọng lượng/nhiệt độ nổi bật, khối in nhãn (kết nối máy in Bluetooth nếu có) giữa form, chữ ký số cuối form, nút "Xác nhận Lưu mẫu" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Sample Information Form, nền tảng Mobile, actor Service Caterers.
Bố cục: camera + GPS ưu tiên đầu form, input trọng lượng/nhiệt độ, khối in nhãn mẫu, camera chụp ảnh hộp đã dán nhãn, chữ ký số, nút Xác nhận Lưu mẫu cố định dưới cùng.
Thành phần bắt buộc: cảnh báo MSG-019 nếu trọng lượng < 100g, cảnh báo GPS/nhiệt độ ngoài phạm vi, badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3735–3749 (UC-069 - Register Food Sample)
- business_rules.md BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-196
- error_message.md MSG-019, MSG-020, MSG-044, MSG-078

# Screen Spec — Food Sample Detail

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình chi tiết mở từ Record Sample Information Form (`04_Service_Caterers_Screen_Flow.md`: `SampleForm --> SampleDetail`), dữ liệu nguồn từ UC-069 và UC-118 | **Status**: Draft
**BR liên quan**: BR-106, BR-108, BR-109, BR-111, BR-114
**MSG liên quan**: MSG-059 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin mẫu (tên món, ngày sản xuất, ngày lưu mẫu, số tham chiếu lô) | Read-only card | — | BR-106 | UC-069 Postconditions |
| 2 | Trọng lượng mẫu, nơi lưu (tủ số mấy) | Read-only | — | BR-107, BR-109 | — |
| 3 | Đồng hồ đếm ngược thời gian lưu mẫu (72 giờ) | Countdown/Progress indicator | Tự động tính | BR-111 | UC-069 Postconditions |
| 4 | Trạng thái mẫu | Chip | Retained / Retrieved / Disposed | BR-114 | UC-118 |
| 5 | Ảnh nhãn + hộp mẫu đã lưu | Ảnh xem lại (read-only) | — | BR-107 | — |
| 6 | Cảnh báo nếu tủ lưu mẫu mở/nhiệt độ dao động | MaterialBanner | Hiện nếu có sự kiện bất thường | BR-110 | MSG-059 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải chi tiết mẫu.
- Empty: Không áp dụng.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Record Sample Information Form sau khi lưu mẫu, hoặc từ danh sách trong Food Sample Retention Dashboard. Thoát: back về Dashboard.
**Bố cục**: Trang chi tiết 1 cột — ảnh nhãn/hộp mẫu trên cùng, đồng hồ đếm ngược nổi bật, thông tin mẫu dạng list bên dưới, banner cảnh báo trên cùng nếu có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Detail, nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột — ảnh nhãn/hộp mẫu, đồng hồ đếm ngược 72 giờ nổi bật, thông tin mẫu dạng list, chip trạng thái, banner cảnh báo nếu có.
Thành phần bắt buộc: chip trạng thái 3 giá trị, cảnh báo MSG-059.
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
**MSG liên quan**: MSG-020 (Error), MSG-059 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách tủ lưu mẫu đang hoạt động | Card list | Mỗi card: Tủ số, Nhiệt độ hiện tại, Số mẫu đang lưu | BR-109 | UC-042 Normal Flow bước 2 |
| 2 | Nút "Ghi Nhiệt độ" (Log Temperature) | Button | Điều hướng form nhập nhiệt độ 2 lần/ngày | BR-110 | UC-042 Normal Flow bước 3–5 |
| 3 | Cảnh báo nhiệt độ ngoài phạm vi 2–8°C | MaterialBanner | Nếu lệch > 30 phút → cảnh báo báo động | BR-110 | MSG-020/MSG-059 |
| 4 | Danh sách mẫu đang lưu trong từng tủ | Card con (mở rộng) | Điều hướng Food Sample Detail | BR-109 | UC-042 Normal Flow bước 2 |
| 5 | Chuyển tủ cho mẫu | Button + form | Yêu cầu lý do chuyển tủ | — | UC-042 Alternative Flow AF2 |
| 6 | FAB "Đăng ký Mẫu mới" | FloatingActionButton | Điều hướng Record Sample Information Form | — | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách tủ lưu mẫu.
- Empty: Chưa có tủ lưu mẫu nào được cấu hình.
- Error: MSG-020 khi nhiệt độ ngoài phạm vi; `MaterialBanner` MSG-059 nếu kéo dài > 30 phút.
- Success: `SnackBar` xác nhận ghi nhiệt độ/chuyển tủ thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Lưu mẫu Thực phẩm" trong Grid Menu Items (kênh chính tại bếp). Điều hướng tiếp: Record Sample Information Form, Food Sample Detail.
**Bố cục**: AppBar + Card list theo từng tủ lưu mẫu (nhiệt độ hiện tại nổi bật), mở rộng để xem danh sách mẫu con, FAB "Đăng ký Mẫu mới" góc dưới phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Retention Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list theo tủ lưu mẫu (nhiệt độ, số mẫu, nút Ghi Nhiệt độ), FAB Đăng ký Mẫu mới.
Thành phần bắt buộc: banner cảnh báo nhiệt độ ngoài phạm vi (MSG-020/MSG-059).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error, success.
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
| 1 | Danh sách phiên phục vụ bữa ăn đang hoạt động/dự kiến | Card list | Mỗi card: Lớp, Giờ phục vụ, Số suất, Trạng thái | — | UC-047 Normal Flow bước 2 |
| 2 | Chi tiết phân bổ suất ăn theo lớp | Read-only detail (trang riêng) | Điều chỉnh timing/nhân sự/số suất | BR-121 | UC-047 Normal Flow bước 4–5 |
| 3 | Cảnh báo session đang phục vụ | MaterialBanner | Chỉ cho sửa field không quan trọng, chặn giảm số suất | — | UC-047 Alternative Flow AF1 |
| 4 | Cảnh báo lệch số suất so với danh sách phân phát | MaterialBanner | Yêu cầu xác nhận trước khi lưu | BR-121 | UC-047 Alternative Flow AF2 |
| 5 | Nút "Xem Danh sách Phân phát" | Button (AppBar action) | Điều hướng View Meal Distribution List | — | ScreenFlow |
| 6 | Nút "Xem Cảnh báo Dị ứng" | Button (AppBar action) | Điều hướng View Allergy Alerts Panel | BR-125 | ScreenFlow |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách phiên phục vụ.
- Empty: Chưa có phiên phục vụ nào được lên lịch hôm nay.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `SnackBar` xác nhận cập nhật cấu hình phiên phục vụ thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Phục vụ Bữa ăn" trong Grid Menu Items. Điều hướng tiếp: View Meal Distribution List, View Allergy Alerts Panel.
**Bố cục**: AppBar (icon Danh sách Phân phát + Cảnh báo Dị ứng) + Card list cuộn dọc (pull-to-refresh), chạm vào card mở trang chi tiết phiên phục vụ.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Service Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar (icon Danh sách Phân phát/Cảnh báo Dị ứng) + Card list phiên phục vụ (Lớp, Giờ, Số suất, Trạng thái).
Thành phần bắt buộc: banner cảnh báo lệch số suất, banner session đang active hạn chế sửa.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error, success.
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
| 1 | Danh sách phân phát theo lớp | Card list | Mỗi card: Lớp, Số học sinh, Số suất đặc biệt, Trạng thái phục vụ | — | UC-132 Normal Flow bước 2 |
| 2 | Bộ lọc theo ngày/lớp/loại bữa ăn | Filter chip row | Tuỳ chọn | — | UC-132 Normal Flow bước 3 |
| 3 | Cảnh báo dị ứng nổi bật | Chip màu `danger` | Hiện khi lớp có học sinh dị ứng đã đăng ký | BR-123 | — |
| 4 | Cảnh báo lớp chưa xác nhận phân phát | Highlight card (viền màu warning) | Hiện khi giáo viên chủ nhiệm chưa xác nhận | BR-120 | UC-132 Alternative Flow AF2 |
| 5 | Nút "Xuất PDF/Excel" | Button (AppBar action) | — | — | UC-132 Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Chưa có phiên phục vụ nào trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Meal Service Management Dashboard (icon "Xem Danh sách Phân phát"). Thoát: back về Dashboard.
**Bố cục**: AppBar (icon Xuất báo cáo) + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Meal Distribution List, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list (Lớp, Số học sinh, Số suất đặc biệt, Trạng thái, chip cảnh báo dị ứng).
Thành phần bắt buộc: chip cảnh báo dị ứng màu đỏ, highlight card lớp chưa xác nhận phân phát.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3953–3971 (UC-132 - View Meal Distribution List)
- business_rules.md BR-065, BR-120, BR-121, BR-123

# Screen Spec — View Allergy Alerts Panel (Special Meal Monitoring)

**Actor**: Service Caterers | **UC liên quan**: UC-103 - View Allergy Alerts (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-065, BR-123
**MSG liên quan**: MSG-051 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có hồ sơ dị ứng đang hoạt động | Card list | Mỗi card: Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch đặc biệt | BR-065 | UC-103 Normal Flow bước 2 |
| 2 | Dị ứng mức "Severe" | Highlight card màu `danger` | Bắt buộc nổi bật | BR-123 | UC-103 Normal Flow bước 3 |
| 3 | Xem chi tiết dị ứng 1 học sinh | Điều hướng chi tiết | Hiển thị dị ứng đã khai báo, ghi chú y tế, lịch sử bữa ăn an toàn | — | UC-103 Normal Flow bước 4 |
| 4 | Trạng thái xác nhận kép (dual-confirmation) | Chip kép | Bếp phát hành + Giáo viên bàn giao | BR-123 | UC-103 Normal Flow bước 5 |
| 5 | Cảnh báo xung đột dị ứng với thực đơn hôm nay | MaterialBanner | Hiện khi thực đơn duyệt có nguyên liệu trùng allergen | — | MSG-051 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Không có học sinh dị ứng nào — thông báo xác nhận (không phải lỗi).
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Meal Service Management Dashboard (icon "Xem Cảnh báo Dị ứng"). Thoát: back về Dashboard.
**Bố cục**: AppBar + banner cảnh báo xung đột thực đơn trên cùng (nếu có) + Card list cuộn dọc, card dị ứng Severe có viền/nền màu `danger` nổi bật.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Allergy Alerts Panel (Special Meal Monitoring), nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + banner cảnh báo xung đột thực đơn (nếu có) + Card list (Học sinh, Lớp, Loại dị ứng, Mức độ, Kế hoạch đặc biệt, chip xác nhận kép).
Thành phần bắt buộc: highlight màu đỏ cho dị ứng Severe, chip xác nhận kép.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3931–3949 (UC-103 - View Allergy Alerts)
- business_rules.md BR-065, BR-123
- error_message.md MSG-051

# Screen Spec — Manage Special Meals Configuration

**Actor**: Service Caterers | **UC liên quan**: UC-023 - Create Special Meal Plan (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-063, BR-065, BR-066, BR-069, BR-082, BR-156, BR-157
**MSG liên quan**: MSG-072 (Success), MSG-051 (Error), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — Report 3 UC-023 trích BR-071 cho "chế biến riêng", nhưng nội dung đúng trong `business_rules.md` là **BR-082**. Dùng BR-082.
>
> **✅ Đã xử lý (2026-07-01)**: field "Món thay thế cho 5 loại dị ứng phổ biến nhất" (trước đây trích BR-081) **đã bị gỡ bỏ** — team quyết định xoá hẳn BR-081 khỏi `business_rules.md` vì không thực tế (danh sách "5 loại phổ biến" không định nghĩa rõ, không có data model hỗ trợ, trùng chức năng với chính màn hình Special Meal Plan này).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có dị ứng/chế độ ăn đặc biệt đã xác minh | Card list | Chỉ hiển thị hồ sơ đã Medical Staff xác nhận | BR-065 | UC-023 Normal Flow bước 2 |
| 2 | Loại chế độ ăn đặc biệt | Chip/Dropdown | Halal, Vegetarian, Gluten-Free, Nut-Free, khác | BR-066 | UC-023 Normal Flow bước 4 |
| 3 | Công thức + hướng dẫn nấu riêng biệt | TextField multiline | Bắt buộc mô tả dụng cụ nấu riêng | BR-082 | UC-023 Normal Flow bước 5 |
| 4 | Khai báo nguyên liệu + dấu vết dị ứng tiềm ẩn | Tag input | Bắt buộc đầy đủ | BR-069 | UC-023 Normal Flow bước 6 |
| 5 | Kiểm tra không trùng allergen với hồ sơ học sinh | Validation tự động | Chặn lưu nếu phát hiện trùng | BR-063 | MSG-051 |
| 6 | Nút "AI Assist" gợi ý công thức an toàn | Button | Mở panel gợi ý (Gemini AI) | BR-156, BR-157 | UC-023 Alternative Flow AF1 |
| 7 | Nút "Lưu Kế hoạch Bữa ăn Đặc biệt" | FilledButton | Lưu, hiển thị MSG-072 | — | UC-023 Normal Flow bước 8 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải danh sách học sinh.
- Empty: Chưa có học sinh nào cần kế hoạch bữa ăn đặc biệt.
- Error: `AlertDialog` MSG-051 nếu phát hiện trùng allergen (chặn lưu).
- Success: `SnackBar` MSG-072.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Update Menu Screen (`04_Service_Caterers_Screen_Flow.md`: `UpdateMenuScr --> SpecialMealCfg`) hoặc Meal Service Management Dashboard. Thoát: lưu xong quay lại màn gọi.
**Bố cục**: Trang riêng — danh sách học sinh trên cùng (chọn 1 để cấu hình), form cấu hình bên dưới theo từng bước, nút "Lưu Kế hoạch" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Special Meals Configuration, nền tảng Mobile, actor Service Caterers.
Bố cục: danh sách học sinh dị ứng/đặc biệt, form cấu hình (loại chế độ ăn, công thức riêng, khai báo nguyên liệu), nút AI Assist, nút Lưu Kế hoạch cố định dưới cùng.
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
**BR liên quan**: BR-075, BR-076, BR-077, BR-089
**MSG liên quan**: MSG-018 (Warning/Error), MSG-042 (Success), MSG-052 (Warning), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — Report 3 UC-045 trích BR-081/082/083/089/090 với ngữ nghĩa "storage zone matching"/"block expired" không khớp nội dung thật của các mã này trong `business_rules.md`. Dùng BR-075/076/077/089 (đúng ngữ cảnh gần nhất). **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tồn kho theo nguyên liệu, khu vực lưu trữ, hạn dùng lô | Card list | Nhóm theo ingredient/zone/expiration | BR-076 | UC-045 Normal Flow bước 2 |
| 2 | Nút "Xuất kho" (Issue) | Button | Điều hướng Issue Ingredients Workspace, áp dụng FEFO | BR-075 | UC-045 Normal Flow bước 3–4 |
| 3 | Nút "Điều chỉnh Kho" (Adjustment) | Button | Yêu cầu mã lý do bắt buộc + xác nhận quản lý | — | UC-045 Alternative Flow AF1 |
| 4 | Cảnh báo thiếu kho | MaterialBanner | Hiện khi tồn không đủ nhu cầu kế hoạch | BR-077 | MSG-018 |
| 5 | Cảnh báo sắp hết hạn | Chip/Banner | Hiện theo ngưỡng cảnh báo hết hạn | BR-076 | MSG-052 |
| 6 | Nút "Xem Cảnh báo Hết hạn" | Button (AppBar action) | Điều hướng Monitor Expiration Dates Panel | — | ScreenFlow |
| 7 | Nút "Xem Nhật ký Giao dịch Kho" | Button (AppBar action) | Điều hướng Record Inventory Transactions Log | — | ScreenFlow |
| 8 | Nút "Tạo Yêu cầu Mua hàng" (khi tồn thấp) | Button | Điều hướng Create Purchase Request | BR-077 | ScreenFlow: `InvDash -->|"low stock"| CreatePR` |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách tồn kho.
- Empty: Kho trống hoàn toàn → thông báo + gợi ý tạo yêu cầu mua hàng.
- Error: MSG-018 khi thiếu kho; `SnackBar` MSG-004/MSG-005 nếu tải/lưu thất bại.
- Success: `SnackBar` MSG-042 sau khi ghi nhận giao dịch kho.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Kho" trong Grid Menu Items. Điều hướng tiếp: Issue Ingredients Workspace, Monitor Expiration Dates Panel, Record Inventory Transactions Log, Create Purchase Request.
**Bố cục**: AppBar + Card list tồn kho (Nguyên liệu, Khu vực, Số lượng, Hạn dùng, badge cảnh báo), pull-to-refresh, FAB điều hướng nhanh tới Xuất kho.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inventory Management Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list tồn kho (Nguyên liệu, Khu vực, Số lượng, Hạn dùng, badge cảnh báo), FAB Xuất kho.
Thành phần bắt buộc: banner cảnh báo thiếu kho/sắp hết hạn (MSG-018/MSG-052).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3133–3151 (UC-045 - Manage Inventory)
- business_rules.md BR-075, BR-076, BR-077, BR-089
- error_message.md MSG-018, MSG-042, MSG-052

# Screen Spec — Issue Ingredients Workspace

**Actor**: Service Caterers | **UC liên quan**: UC-045 - Manage Inventory (Report 3 §3.2, sub-flow "issue ingredients following FEFO") | **Status**: Draft
**BR liên quan**: BR-075, BR-076
**MSG liên quan**: MSG-018 (Error), MSG-042 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn phiên sản xuất/thực đơn cần xuất nguyên liệu | DropdownMenu | Bắt buộc | — | UC-045 Normal Flow bước 3 |
| 2 | Danh sách nguyên liệu cần xuất + số lượng | Card list | Tự động gợi ý lô theo FEFO | BR-075 | UC-045 Normal Flow bước 4 |
| 3 | Xác nhận lô được chọn để xuất | Read-only + nút "Đổi lô khác" | Không cho chọn lô hết hạn | BR-076 | UC-045 Normal Flow bước 4 |
| 4 | Cảnh báo thiếu kho | MaterialBanner | Hiện nếu không đủ nguyên liệu | BR-077 | MSG-018 |
| 5 | Nút "Xác nhận Xuất kho" | FilledButton (cố định cuối màn) | Cập nhật tồn kho, ghi nhận giao dịch | — | MSG-042 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải danh sách.
- Empty: Không áp dụng.
- Error: MSG-018 nếu thiếu kho.
- Success: `SnackBar` MSG-042.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Inventory Management Dashboard (nút "Xuất kho"). Thoát: xác nhận xong quay lại Dashboard.
**Bố cục**: Trang riêng — Card list nguyên liệu cần xuất kèm lô đề xuất FEFO, nút xác nhận cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Issue Ingredients Workspace, nền tảng Mobile, actor Service Caterers.
Bố cục: Card list nguyên liệu cần xuất (lô đề xuất FEFO, số lượng), banner cảnh báo thiếu kho, nút Xác nhận Xuất kho cố định dưới cùng.
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
**BR liên quan**: BR-076, BR-047
**MSG liên quan**: MSG-052 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách lô nguyên liệu sắp hết hạn | Card list | Mỗi card: Nguyên liệu, Số lô, Ngày hết hạn, Số lượng còn lại | BR-076 | UC-045 sub-flow |
| 2 | Chip "Expiring Soon" | Chip màu `warning` | Hiện khi còn dưới 30 ngày | BR-047 | MSG-052 |
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
- business_rules.md BR-076, BR-047
- error_message.md MSG-052

# Screen Spec — Record Inventory Transactions Log

**Actor**: Service Caterers | **UC liên quan**: UC-064 - Record Inventory Transactions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: Xem lưu ý mâu thuẫn nguồn (BR-082 trích dẫn sai chủ đề)
**MSG liên quan**: MSG-042 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn quan trọng**: giống bản Web — `Report 3.md` UC-064 trích BR-082 cho "mandatory reason code and manager sign-off", nhưng BR-082 thật trong `business_rules.md` là "Chế biến riêng cho bữa ăn đặc biệt" — không liên quan. `business_rules.md` hiện **không có mã BR** cho yêu cầu reason code/sign-off khi điều chỉnh kho. **[Giả định — cần team xác nhận, bổ sung mã BR mới nếu giữ yêu cầu này]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn lô nguyên liệu cần điều chỉnh | DropdownMenu | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 2 | Số lượng điều chỉnh | TextField số (+/-) | Bắt buộc | — | UC-064 Normal Flow bước 2 |
| 3 | Mã lý do điều chỉnh (spoilage, waste, audit write-off) | DropdownMenu | Bắt buộc | **[Giả định — chưa có mã BR]** | UC-064 Normal Flow bước 2 |
| 4 | Xác nhận/chữ ký quản lý | Signature pad hoặc xác thực quản lý | Bắt buộc | **[Giả định — chưa có mã BR]** | UC-064 Normal Flow bước 3 |
| 5 | Nút "Ghi nhận Giao dịch" | FilledButton | Chặn nếu thiếu mã lý do hoặc sign-off | **[Giả định]** | MSG-042 |
| 6 | Nhật ký giao dịch kho (lịch sử) | Card list | Mỗi card: Ngày, Nguyên liệu, Số lượng, Lý do, Người thực hiện | BR-170 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho form + nhật ký.
- Empty: Chưa có giao dịch điều chỉnh nào.
- Error: `AlertDialog` MSG-004/MSG-005 nếu thiếu mã lý do/sign-off.
- Success: `SnackBar` MSG-042.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Inventory Management Dashboard (icon "Xem Nhật ký Giao dịch Kho" hoặc "Điều chỉnh Kho"). Thoát: back về Dashboard.
**Bố cục**: Trang riêng — form điều chỉnh (bottom sheet) + Card list nhật ký giao dịch bên dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Inventory Transactions Log, nền tảng Mobile, actor Service Caterers.
Bố cục: Bottom sheet form điều chỉnh (chọn lô, số lượng, mã lý do, xác nhận quản lý) + Card list nhật ký giao dịch.
Thành phần bắt buộc: chặn submit nếu thiếu mã lý do/sign-off.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3199–3217 (UC-064 - Record Inventory Transactions)
- business_rules.md BR-170 — xem lưu ý mâu thuẫn nguồn BR-082 ở trên
- error_message.md MSG-042

# Screen Spec — Corrective Actions Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-038 - Manage Corrective Actions (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-152, BR-153, BR-155
**MSG liên quan**: MSG-023 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách CAPA được gán cho Service Caterers | Card list | Mỗi card: Sự cố liên quan, Hạn chót, Trạng thái | BR-152 | UC-038 Normal Flow bước 2 |
| 2 | Trạng thái CAPA | Chip | Pending / In Progress / Pending Approval / Late Submission | BR-152, BR-153 | UC-038 Exceptions |
| 3 | Chi tiết hành động yêu cầu, nguyên nhân gốc rễ, hạn chót | Read-only detail (trang riêng) | — | BR-152 | UC-038 Normal Flow bước 4 |
| 4 | Nút "Cập nhật Tiến độ" | Button | Điều hướng Submit Corrective Action Results Form | — | UC-038 Normal Flow bước 5 |
| 5 | Cảnh báo nộp trễ hạn (quá 12 giờ) | Chip màu `warning` | Tự động gắn cờ "Late Submission" | BR-152 | MSG-023 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách CAPA.
- Empty: Không có CAPA nào đang chờ xử lý.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Sự cố & Hành động Khắc phục" trong Grid Menu Items. Điều hướng tiếp: Submit Corrective Action Results Form, View Incident Information Detail.
**Bố cục**: AppBar + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Corrective Actions Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list CAPA (Sự cố liên quan, Hạn chót, Trạng thái chip, nút Cập nhật Tiến độ).
Thành phần bắt buộc: chip "Late Submission" màu warning.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4419–4437 (UC-038 - Manage Corrective Actions)
- business_rules.md BR-152, BR-153, BR-155
- error_message.md MSG-023

# Screen Spec — Submit Corrective Action Results Form

**Actor**: Service Caterers | **UC liên quan**: UC-082 - Submit Corrective Action Results (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-152, BR-153, BR-155
**MSG liên quan**: MSG-043 (Success), MSG-004 (Error — thiếu Health Department Clearance)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin CAPA đang xử lý | Read-only header | — | — | UC-082 Preconditions |
| 2 | Ghi chú hoàn thành | TextField multiline | Bắt buộc | — | UC-082 Normal Flow bước 2 |
| 3 | Upload tài liệu bằng chứng | Camera/file picker đa file | Bắt buộc ít nhất 1 file | — | UC-082 Normal Flow bước 2 |
| 4 | Giấy chứng nhận Cơ quan Y tế | Upload file | **Bắt buộc** nếu sự cố High Severity từng đóng cửa bếp | BR-153 | UC-082 Exceptions EX1 |
| 5 | Nút "Nộp Kết quả" | FilledButton (cố định cuối màn) | Chặn nếu thiếu Health Department Clearance khi bắt buộc | BR-153 | MSG-004 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin CAPA.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004 (chặn nộp nếu thiếu clearance bắt buộc).
- Success: `SnackBar` xác nhận nộp kết quả thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Corrective Actions Dashboard (nút "Cập nhật Tiến độ"). Thoát: nộp xong quay lại Dashboard.
**Bố cục**: Trang riêng — ghi chú hoàn thành, khối upload tài liệu đa file, khối upload Health Department Clearance (điều kiện), nút nộp cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Corrective Action Results Form, nền tảng Mobile, actor Service Caterers.
Bố cục: ghi chú hoàn thành, upload tài liệu bằng chứng đa file, khối upload Health Department Clearance (điều kiện), nút Nộp Kết quả cố định dưới cùng.
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
| 1 | Danh sách bản ghi kiểm tra (chỉ của chính Service Caterers) | Card list | Mỗi card: Ngày, Món ăn, Trạng thái | BR-099 | UC-124 Normal Flow bước 4 |
| 2 | Bộ lọc theo ngày/món/trạng thái | Filter chip row | Tuỳ chọn | — | UC-124 Normal Flow bước 3 |
| 3 | Xem chi tiết checklist, nhiệt độ, ảnh, chữ ký số | Điều hướng chi tiết (read-only) | — | BR-091–BR-099 | UC-124 Normal Flow bước 5 |
| 4 | Xem lịch sử Corrective Action liên quan (nếu Failed) | Link điều hướng | — | BR-097, BR-098 | UC-124 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Chưa có bản ghi kiểm tra nào trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005/MSG-016/MSG-017 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard hoặc Corrective Actions Dashboard. Thoát: back về màn gọi.
**Bố cục**: AppBar + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Inspection Records Log, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list (Ngày, Món ăn, chip trạng thái Passed/Failed).
Thành phần bắt buộc: chip trạng thái Passed (success)/Failed (danger).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
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
| 1 | Danh sách sự cố được gán cho Service Caterers | Card list | Mỗi card: Mã sự cố, Ngày, Mức độ nghiêm trọng, Trạng thái | BR-147 | UC-120 Normal Flow bước 1 |
| 2 | Mức độ nghiêm trọng | Chip | Low/Medium/High — High dùng màu `critical` | BR-147, BR-148 | UC-120 Normal Flow bước 2 |
| 3 | Chi tiết sự cố liên quan tới Service Caterers | Read-only detail (trang riêng) | Chỉ hiển thị thông tin liên quan tới phạm vi Service Caterers | — | UC-120 Normal Flow bước 2 |
| 4 | Trạng thái Quarantine lô hàng liên quan | Chip màu `critical` | Hiện nếu lô hàng đang bị cách ly | BR-149 | — |
| 5 | Nút "Xuất Dữ liệu" (CSV/PDF) | Button (AppBar action) | — | — | UC-120 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách/chi tiết.
- Empty: Không có sự cố nào được gán.
- Error: `SnackBar` MSG-023/MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Corrective Actions Dashboard hoặc thông báo cảnh báo sự cố (deep link push notification). Thoát: back về Dashboard.
**Bố cục**: AppBar + Card list danh sách sự cố, chạm vào card mở trang chi tiết.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Incident Information Detail, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list sự cố (Mã sự cố, Ngày, Mức độ, Trạng thái), trang chi tiết khi chạm vào.
Thành phần bắt buộc: chip mức độ nghiêm trọng (Low/Medium=warning, High=critical), chip Quarantine.
Áp dụng Design System: xem 00_Foundation/Design_System.md (token critical riêng biệt với danger).
Trạng thái cần thể hiện: loading (shimmer), empty, error.
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
| 1 | Địa điểm xảy ra sự việc (`locationOfIncident`) | TextField | Bắt buộc, không rỗng | BR-211 | Mẫu giấy: "Hôm nay, tại: ___" |
| 2 | Đại diện Y tế trường học (`reportedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản đăng nhập hiện tại | BR-183/184 | Mẫu giấy: "Đại diện P.VSATTP" |
| 3 | Đại diện Nhà thầu (`caterersRepresentativeId`) | DropdownMenu (tài khoản Service Caterers đang hoạt động) | Bắt buộc | BR-211 | Mẫu giấy: "Đại diện nhà thầu"; liên kết tài khoản có thật |
| 4 | Nhân viên liên quan (`staffName`) | TextField tự do (không phải khóa ngoại) | Tùy chọn — để trống nếu không quy trách nhiệm cho 1 cá nhân cụ thể | BR-184 | Trống = chỉ áp BR-183 (thường gặp khi tạo tự động từ `InspectionStep.fail()`); có giá trị = kích hoạt thêm BR-184/185/187 |
| 5 | Chức vụ nhân viên (`staffPosition`) | TextField | Tùy chọn; chỉ hiện khi field 4 đã có ký tự đầu tiên | BR-184 | Không có trên mẫu giấy |
| 6 | Mức độ nghiêm trọng (`severityLevel`) | RadioListTile (LOW/MEDIUM/HIGH) | Bắt buộc | BR-183/184, BR-186 | Điều kiện tự động phân luồng nút ở `24_RecordViolationResolution.md` |
| 7 | Loại vi phạm (`violationType`, gồm `OVERRIDE_ABUSE`) | DropdownMenu | Tùy chọn | BR-190 | 4 giá trị: "Bỏ qua bước quy trình bắt buộc", "Vi phạm vệ sinh cá nhân", "Lạm dụng quyền ghi đè hệ thống", "Hành động không được ủy quyền" |
| 8 | Thời điểm xảy ra sự việc (`occurredAt`) | DateTimePicker (M3) | Bắt buộc | BR-211 | Tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày]..." trong PDF |
| 9 | **Nội dung biên bản** (`violationContent`) | Rich text editor (toolbar rút gọn) | Bắt buộc, không rỗng | BR-183/184 | MSG-085 nếu thiếu |
| 10 | Ảnh bằng chứng | Camera capture, nhiều ảnh | Bắt buộc ít nhất 1 ảnh | BR-183/184 | MSG-085 nếu thiếu |
| 11 | Nút "Save Violation" | FilledButton (cố định cuối màn) | Disabled khi thiếu field bắt buộc (1, 3, 6, 8, 9, 10) | BR-183/184, BR-188 | Lưu dữ liệu số ngay — không chờ ảnh biên bản đã ký; liên kết `NonConformanceRecord` khi tạo từ `InspectionStep.fail()` — luồng `CorrectiveActionRequest` tiếp tục song song, không bị thay thế |
| 12 | Nút "Xuất Biên bản (PDF)" | FilledButton (secondary) | Chỉ hiện sau khi đã Save Violation | BR-211 | Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**; 3 khoảng chữ ký (cột 3 để trống tĩnh) |
| 13 | Ảnh biên bản đã ký (`signedRecordPhotoUrl`) | Camera capture | Không bắt buộc để Save; **bắt buộc** trước khi đóng/gửi duyệt ở `24_RecordViolationResolution.md` — gate chặn cứng | BR-211 | Tải lên sau khi in — ký tay — chụp ảnh |

### Cơ chế phân loại BR-183 vs BR-184 (không có toggle thủ công)
- Field 4 để trống → chỉ áp **BR-183** — field "Mức độ hình thức kỷ luật" ở `24_RecordViolationResolution.md` **không hiện**. Đây là trường hợp phổ biến khi Service Caterers tạo từ `InspectionStep.fail()`.
- Field 4 có giá trị → kích hoạt thêm **BR-184/185** và **BR-187** (best-effort, cảnh báo nghi ngờ tái diễn khi staffName trùng khớp với 1 bản ghi Medium+ trong 90 ngày gần nhất).
- Field 3, 1/8/9/10 luôn bắt buộc bất kể field 4.

### Trạng thái màn hình
- **Loading**: `Shimmer` khi tải ngữ cảnh (nếu mở từ `InspectionStep.fail()`) hoặc rà soát trùng tên (khi field 4 có giá trị); spinner trên "Save Violation" khi lưu.
- **Empty**: không áp dụng.
- **Error**: `AlertDialog` MSG-004, MSG-005, MSG-085 tại field thiếu.
- **Success**: `SnackBar` MSG-086, ghi vào audit log bất biến (BR-188); trùng tên ≥ MEDIUM trong 90 ngày → `MaterialBanner` MSG-088 (không chặn lưu); điều hướng `24_RecordViolationResolution.md`. Nếu chưa tải `signedRecordPhotoUrl`, màn `24` chặn cứng nút đóng/gửi duyệt (MSG-095).

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Grid Menu Items → "Vi phạm" (`04_Service_Caterers_Screen_Flow.md`: `ViolDash --> ProcessViol`) hoặc tự động điều hướng khi 1 bước Three-Step Inspection thất bại (field 4 để trống mặc định). Thoát: `ProcessViol -->|"MSG-086 success"| CorrDash`, sau đó → `24_RecordViolationResolution.md`.
**Bố cục**: Trang riêng full-screen, camera (field 10) ưu tiên đầu form, field 5 chỉ hiện khi field 4 có giá trị, nút "Save Violation" cố định cuối màn. **Lưu ý cần xác nhận thêm**: toolbar rich text đầy đủ không khả thi trên màn hình hẹp — rút gọn còn đậm/nghiêng/gạch chân + gạch đầu dòng, đội dự án/dev xác nhận trước khi code.

## Xuất & Lưu Biên bản đã ký (BR-211, sửa 11/07/2026)

> Mô tả đầy đủ tại `Web/MedicalStaff/22_RecordStaffViolation.md` — áp dụng chung mọi actor có quyền lập biên bản (Medical Staff, Service Caterers). Trên Mobile: nút "Xuất Biên bản (PDF)" mở trình xem PDF/trình chia sẻ hệ điều hành; field "Ảnh biên bản đã ký" dùng camera trực tiếp. Tiêu đề PDF **"BIÊN BẢN VI PHẠM"**, cột chữ ký thứ 3 "Người chứng kiến" để trống tĩnh.

## AI Generation Prompt ⭐

```
Tạo wireframe cho dialog "Lập Biên bản Vi phạm", nền tảng Mobile, actor Service Caterers.
Bố cục: trang riêng full-screen, camera ưu tiên đầu form, form dọc.
Thứ tự field: Địa điểm xảy ra sự việc, Đại diện Y tế trường học (hiển thị tự động, không sửa),
Đại diện Nhà thầu (Select) — KHÔNG có field Người chứng kiến trong form,
Nhân viên liên quan (tùy chọn, thường để trống khi tạo từ Three-Step Inspection thất bại) —
khi có giá trị thì hiện thêm Chức vụ nhân viên,
Mức độ nghiêm trọng (Select LOW/MEDIUM/HIGH), Loại vi phạm (Select, tùy chọn),
Thời điểm xảy ra sự việc (DateTimePicker), khối "Nội dung biên bản" là rich text editor rút gọn,
phía trên hiện dòng tự sinh "Vào hồi [giờ] ngày [ngày]...", ảnh bằng chứng (camera, bắt buộc ít nhất 1),
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
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Violation Management, InspectionStep.fail()).
- `review/medical_staff/review_screen_medical_staff.md` Mục 10.
