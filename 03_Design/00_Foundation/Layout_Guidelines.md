# Layout Guidelines — SFMS

**Trạng thái**: Confirmed (2026-07-01)
**Áp dụng cho**: bố cục trang/màn hình cấp cao (không đi vào chi tiết từng component — xem `Component_Guidelines.md`).

---

## 1. Web (Next.js + shadcn/ui) — Bố cục Dashboard

### 1.1. Khung tổng thể (Admin/Dashboard Shell)
Dùng cho System Admin, School Administration, và bản Web mới của 5 role field (theo hướng "all web + Mobile song song", `00_UI_Design_Documentation_Plan.md` mục 0):

```
┌─────────────────────────────────────────────┐
│ Topbar: Logo | Breadcrumb | Search | Notif | Avatar │
├───────────┬─────────────────────────────────┤
│           │  Page Header (Tiêu đề + Action) │
│ Sidebar   ├─────────────────────────────────┤
│ (nav theo │                                 │
│  role)    │  Content Area                   │
│           │  (Table / Form / Card grid)     │
│           │                                 │
└───────────┴─────────────────────────────────┘
```

- **Sidebar**: cố định bên trái (≥1280px), thu gọn thành icon-only ở tablet (768–1279px), chuyển thành Drawer (ẩn, mở bằng nút hamburger) dưới 768px. Nội dung sidebar khác nhau theo actor — xem `Navigation_Guidelines.md`.
- **Topbar**: cố định trên cùng, luôn hiển thị breadcrumb (giúp actor biết đang ở đâu trong cây menu nhiều cấp của School Administration/System Admin).
- **Content Area**: dùng grid 12 cột (Tailwind `grid-cols-12`), card chiếm `col-span-4/6/12` tuỳ độ phức tạp; bảng dữ liệu (danh sách UC dạng List) luôn kèm: thanh filter/search phía trên, phân trang phía dưới, action buttons (Edit/Delete/View) ở cột cuối bên phải.

### 1.2. Bố cục Form/Wizard
- Form dài (VD Đăng ký Supplier, tạo Menu) chia nhiều bước (Stepper component ngang phía trên) thay vì 1 form dài cuộn — giảm bỏ dở giữa chừng.
- Form ngắn (<8 field) hiện trong `Dialog`/`Sheet` (shadcn) thay vì trang riêng.

### 1.3. Bố cục Guest/Common (Login, OTP, Forgot Password)
Layout căn giữa (centered card, max-width 420px), nền `--background`, không sidebar/topbar — dùng chung cho mọi actor kể cả System Admin/School Admin trên Web (khoảng trống đã nêu ở mục 2.5 của `00_UI_Design_Documentation_Plan.md`).

---

## 2. Mobile (Flutter + Material 3) — Bố cục 1 cột

### 2.1. Khung tổng thể
```
┌─────────────────────────┐
│ AppBar (tiêu đề + icon)  │
├─────────────────────────┤
│                         │
│  Content (scroll dọc,   │
│  Card-based layout)     │
│                         │
├─────────────────────────┤
│ BottomNavigationBar     │
│ (3–5 mục theo role)     │
└─────────────────────────┘
```

- **1 cột duy nhất** (không chia sidebar) — phù hợp thao tác một tay ngoài hiện trường (bếp, cổng trường).
- **Card-based**: mỗi bản ghi (VD 1 lô hàng, 1 học sinh, 1 sự cố) là 1 `Card` Material 3 riêng biệt, không dùng bảng dày đặc như Web.
- **FAB (Floating Action Button)**: dùng cho hành động chính của màn hình (VD "Chụp ảnh kiểm tra", "Thêm mẫu mới") — vị trí góc dưới phải, luôn hiện trên `BottomNavigationBar`.
- **Pull-to-refresh**: bắt buộc cho mọi danh sách (đặc biệt quan trọng khi vừa đồng bộ lại sau offline — xem `UI_Design_Guidelines.md` mục 5).

### 2.2. Bố cục Form Mobile
- Form dài dùng nhiều `Step`/trang riêng (không dùng Stepper ngang như Web — màn hình hẹp), có nút "Tiếp theo/Quay lại" cố định dưới cùng.
- Trường cần camera/GPS đặt ưu tiên đầu form (thao tác trước khi pin/kết nối yếu do dùng lâu).

---

## 3. Nguyên tắc chung cả 2 nền tảng

- **Không quá 2 cấp lồng nhau** cho card/section trong cùng 1 màn hình — tránh rối mắt.
- **Khoảng trắng nhất quán** theo spacing token của `Design_System.md` mục 7 (bội số 4px) — không dùng giá trị margin/padding tuỳ ý.
- **Action quan trọng nhất luôn ở vị trí dễ bấm nhất**: Web = góc trên phải page header; Mobile = FAB hoặc cuối màn hình (vùng ngón cái với).

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/*.md` (thứ tự điều hướng đã dựng theo actor).
- `Report 3.md` mục Mobile Screen (mô tả wireframe gốc "placeholder image frame").
- `Design_System.md` (token màu/spacing), `Component_Guidelines.md` (chi tiết từng thành phần).
