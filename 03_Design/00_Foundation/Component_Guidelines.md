# Component Guidelines — SFMS

**Trạng thái**: Confirmed (2026-07-01) — cập nhật 15/07/2026 (bổ sung `Collapsible`/`Accordion`, xem Mục 11.10.5 `08_FrontEnd/Web/review/medical_staff/review_medicalstaff_incidentmanagement.md`)
**Mục đích**: Ánh xạ nhu cầu UI của SFSMSS sang component cụ thể của shadcn/ui (Web) và Material 3 (Mobile) — đảm bảo Screen Specification chỉ dùng component đã được duyệt trong bảng dưới đây, không tự sáng tạo component mới ngoài danh sách.

---

## 1. Bảng ánh xạ Component theo nhu cầu UI

| Nhu cầu UI | shadcn/ui (Web) | Material 3 / Flutter (Mobile) | Ghi chú nghiệp vụ |
|---|---|---|---|
| Nút hành động chính/phụ | `Button` (variant: default/secondary/ghost/destructive) | `FilledButton` / `OutlinedButton` / `TextButton` | variant `destructive` (Web) / màu `danger` custom (Mobile) dùng cho hành động xoá/từ chối |
| Thẻ hiển thị 1 bản ghi | `Card` | `Card` (M3) | Card danh sách lô hàng/học sinh/sự cố — xem `Layout_Guidelines.md` mục 2.1 |
| Trạng thái/nhãn ngắn | `Badge` (custom variant theo `success/warning/danger/critical/info`) | `Chip` (M3) | Badge màu theo `Design_System.md` mục 3 — bắt buộc, không tự chọn màu khác |
| Bảng dữ liệu | `Table` (kèm `DataTable` pattern: sort/filter/pagination) | Không dùng bảng — chuyển thành `ListView` + `Card` (mục Layout 2.1) | Mobile không có bảng dữ liệu dày đặc — nguyên tắc "1 bản ghi = 1 card" |
| Form input | `Input`, `Textarea`, `Select`, `Checkbox`, `RadioGroup`, `DatePicker` | `TextField`, `DropdownMenu`, `Checkbox`, `RadioListTile`, `DatePicker` (M3) | Validation pattern chung — xem `UI_Design_Guidelines.md` mục 2 |
| Hộp thoại xác nhận | `AlertDialog` | `AlertDialog` (M3) | Dùng cho hành động không thể hoàn tác (xoá, duyệt/từ chối) |
| Form ngắn nổi | `Dialog` / `Sheet` | `showModalBottomSheet` (M3) | Web: Dialog giữa màn hình cho form ngắn desktop; Mobile: luôn trượt từ dưới lên (thumb-friendly) |
| Thông báo tạm thời | `Toast` (Sonner/shadcn toast) | `SnackBar` (M3) | Nội dung lấy từ MSG code — xem `UI_Design_Guidelines.md` mục 4 |
| Banner cảnh báo dài hạn | `Alert` (variant theo màu ngữ nghĩa) | `MaterialBanner` (M3) | Dùng cho Offline/Quarantine/Cache hết hạn — không tự ẩn |
| Điều hướng chính | `Sidebar` (shadcn sidebar block) + `Breadcrumb` | `NavigationBar` (Bottom Nav M3) | Xem `Navigation_Guidelines.md` |
| Tải ảnh lên | `Input type="file"` + preview thumbnail custom | `image_picker` package + preview | Giới hạn dung lượng theo BR-013 — xem `UI_Design_Guidelines.md` mục 2 |
| Quét mã QR | (không áp dụng — QR chỉ quét trên Mobile) | `mobile_scanner`/`qr_code_scanner` package | QR lưu mẫu thực phẩm — Report 1 Measure B |
| Biểu đồ/thống kê | `recharts` (tương thích shadcn theming) | `fl_chart` (Flutter) | Dùng cho dashboard System Admin/School Admin và báo cáo AI menu |
| Stepper nhiều bước | `Stepper` (custom dựng từ `Tabs`/progress) | `Stepper` (Flutter Material) | Web: ngang, cố định trên form dài; Mobile: từng trang riêng (xem `Layout_Guidelines.md`) |
| Khối nội dung thu gọn/mở được (nhiều khối độ dài không đều) | `Collapsible` / `Accordion` (shadcn) — **bổ sung 15/07/2026** | `ExpansionTile` / `ExpansionPanelList` (M3) | **Mới (Mục 11.10.5 review Incident)**: dùng khi 1 màn có nhiều khối nội dung dài ngắn khác nhau, tránh grid cố định gây khoảng trắng vô nghĩa. Cho phép **mở nhiều khối cùng lúc** (không ép single-open) khi các khối không khóa tuần tự cứng. Ứng dụng đầu tiên: `17_InvestigationWorkspace.md` (4 bước điều tra), `26_IncidentDetail.md` (bản đồ truy xuất thu gọn). Component dùng chung Foundation — không phải pattern riêng 1 màn |
| Trạng thái tải | `Skeleton` | `Shimmer` (package `shimmer`) hoặc `CircularProgressIndicator` | Xem `UI_Design_Guidelines.md` mục 6 |
| Empty state | Illustration + `Button` gợi ý hành động | Tương tự, dùng `Column` + `Icon` lớn | Không dùng câu chung chung "Không có dữ liệu" |
| Vai trò/quyền hiển thị khác nhau | Ẩn/hiện component theo RBAC (kiểm tra ở tầng route + component) | Tương tự ở tầng widget | Theo `ADR-001_User_Auth_Architecture.md` (RBAC) |

---

## 2. Quy tắc dùng component

1. **Không cài thêm thư viện UI thứ 3 ngoài shadcn/ui (Web) và Material widgets/first-party Flutter packages (Mobile)** đã liệt kê ở trên, trừ khi bổ sung vào bảng này trước (VD nếu cần thư viện chart khác `recharts`/`fl_chart`, phải cập nhật tài liệu này trước khi dùng).
2. **Mọi component tuỳ biến (custom variant) phải build trên component gốc của thư viện**, không viết CSS/Widget từ đầu cho nhu cầu đã có sẵn pattern chuẩn (VD `Badge` custom variant `critical` phải extend từ `Badge` gốc của shadcn, không viết `<div>` riêng).
3. **Component không được hardcode màu/font** — luôn tham chiếu token ở `Design_System.md` (`bg-danger`, `text-warning-foreground`... bên Web; `Theme.of(context).extension<SfmsStatusColors>()` bên Mobile).
4. **Table (Web) ↔ Card list (Mobile) là cặp tương đương bắt buộc** cho cùng 1 dữ liệu — khi viết Screen Spec Web mới (theo hướng "all web", `00_UI_Design_Documentation_Plan.md` mục 0), nếu bản Mobile dùng Card list, bản Web tương ứng nên ưu tiên `Table`/`DataTable` (khai thác lợi thế màn hình rộng) trừ khi dữ liệu vốn đã đơn giản (khi đó Card grid cũng chấp nhận được ở Web).

## Nguồn dữ liệu
- shadcn/ui component docs (ui.shadcn.com) — danh sách component chính thức.
- Material 3 component catalog (Flutter `material` library — `api.flutter.dev`).
- `class_diagram_description.md` (enum trạng thái cần Badge/Chip).
- `Report 1.md` Measure B (QR label lưu mẫu).
