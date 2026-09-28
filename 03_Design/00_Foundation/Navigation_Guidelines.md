# Navigation Guidelines — SFMS

**Trạng thái**: Confirmed (2026-07-01)
**Nguồn nền**: `03_Design/04_ScreenFlow/*.md` (đã dựng luồng điều hướng theo actor), `00_UI_Design_Documentation_Plan.md` mục 5 (ma trận nền tảng).

---

## 1. Web — Sidebar theo Actor + Breadcrumb

- **Sidebar cố định bên trái**, nội dung menu **khác nhau hoàn toàn theo actor** (không dùng 1 sidebar chung ẩn/hiện mục theo quyền — mỗi actor đăng nhập thấy đúng cấu trúc menu của mình, tránh rối mắt cho School Administration khi phải lướt qua mục không dùng tới).
- **Cấu trúc menu tối đa 2 cấp** (Nhóm chức năng → Màn hình cụ thể) — School Administration có ~38 màn hình nên bắt buộc nhóm theo module (VD "Quản lý Học sinh", "Quản lý Thực đơn", "Báo cáo & Thống kê") thay vì liệt kê phẳng.
- **Breadcrumb** luôn hiện ở Topbar, format: `[Nhóm chức năng] / [Tên màn hình hiện tại]` — bắt buộc với actor có menu sâu (System Admin, School Admin).
- **Active state**: mục menu đang chọn dùng `bg-primary/10` + `text-primary` + border trái 2px `primary` (theo token `Design_System.md`).

### 1.1. Ví dụ cấu trúc Sidebar theo actor

| Actor | Nhóm menu chính (ví dụ) |
|---|---|
| System Admin | Quản lý Trường học, Quản lý Người dùng Hệ thống, Cấu hình Hệ thống, Audit Log, Sao lưu |
| School Administration | Quản lý Lớp/Học sinh, Quản lý Thực đơn, Quản lý Nhà cung cấp, Kiểm tra & Sự cố, Báo cáo |
| Service Caterers / Medical Staff / Parent / Raw Food Supplier / Teacher (bản Web mới) | Menu rút gọn hơn, bám sát đúng các UC đã có ở bản Mobile — không thêm mục mới ngoài phạm vi UC hiện có |

## 2. Mobile — Bottom Navigation theo Actor

- **3–5 mục tối đa** trên `NavigationBar` (Material 3) — nếu actor có nhiều chức năng hơn, gom vào mục "Thêm"/"Cá nhân" chứa danh sách còn lại.
- **Icon kèm label chữ luôn hiển thị** (không dùng icon-only) — người dùng hiện trường thao tác nhanh, không có thời gian đoán icon.
- **Vị trí mục theo tần suất sử dụng thực tế**: mục dùng nhiều nhất (VD "Kiểm tra" với Service Caterers, "Thực đơn" với Parent) đặt gần giữa thanh nav (vùng ngón cái dễ với nhất), không mặc định đặt theo alphabet.
- Cấu trúc chi tiết theo từng actor: xem `03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md`… `07_Teacher_Screen_Flow.md` (đã tồn tại, không tạo trùng).

## 3. Điều hướng dùng chung 2 nền tảng

- **Back/Cancel luôn ở vị trí nhất quán**: Web = góc trên trái Dialog/trang con; Mobile = icon back trên AppBar (trái).
- **Deep link cho Notification**: khi bấm vào push notification (VD MSG cảnh báo dị ứng), điều hướng thẳng tới đúng màn hình chi tiết liên quan (không về màn hình chủ) — áp dụng cả Web (nếu có web push) và Mobile.
- **Không điều hướng vòng lặp không lối thoát**: mọi màn hình con luôn có đường quay lại rõ ràng (back button hoặc breadcrumb), đặc biệt với Wizard/Stepper nhiều bước.

## 4. Quan hệ với tài liệu khác
- Danh sách + thứ tự điều hướng chi tiết từng actor: `03_Design/04_ScreenFlow/*.md` (không tạo trùng nội dung ở đây).
- Component sidebar/bottom-nav cụ thể: `Component_Guidelines.md` mục 1.
- Bố cục tổng thể chứa nav: `Layout_Guidelines.md` mục 1–2.
