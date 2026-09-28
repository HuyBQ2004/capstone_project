# Screen Spec — System Administrator Dashboard

**Actor**: System Admin | **UC liên quan**: entry point (không phải 1 UC riêng — tổng hợp shortcut tới các UC khác) | **Status**: Draft
**BR liên quan**: BR-165 (báo cáo sức khỏe hệ thống hàng tuần), BR-180 (dashboard cập nhật trong 5 phút — nếu tái sử dụng widget Analytics)
**MSG liên quan**: MSG-004, MSG-005

> Đây là node gốc của sơ đồ điều hướng System Admin (`01_System_Admin_Screen_Flow.md`), không map trực tiếp UC nào — tổng hợp shortcut/summary card dẫn tới 19 màn hình còn lại. Nội dung số liệu tóm tắt suy ra từ các UC nguồn liên quan (UC-153 User count, UC-140 School count, UC-149 System Status, UC-057 Activity) — không tự bịa thêm chỉ số ngoài các UC này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card tổng số trường (Active/Inactive/Pending) | Card + số liệu | — | — | Nguồn: UC-140 View School List |
| 2 | Card tổng số user theo role | Card + số liệu | — | — | Nguồn: UC-153 View User Account List |
| 3 | Card trạng thái hệ thống (health-check tóm tắt) | Card + badge trạng thái (success/danger) | Badge màu theo `Design_System.md` mục 3 | — | Nguồn: UC-149 View System Status |
| 4 | Biểu đồ hoạt động hệ thống (CPU/RAM/session) rút gọn | Chart (recharts) | — | BR-165 | Nguồn: UC-057 Monitor System Activities, chỉ hiển thị 24h gần nhất |
| 5 | Danh sách shortcut điều hướng (Quản lý User, Trường, Role, Audit Log, Cấu hình...) | Card link / menu tile | — | — | Ánh xạ đúng 19 màn hình còn lại theo sơ đồ điều hướng |
| 6 | Cảnh báo hệ thống gần nhất (Error Logs tóm tắt) | Alert list rút gọn | — | BR-164 | Nguồn: UC-111 View Error Logs, chỉ 3-5 lỗi gần nhất mức Critical/High |

### Trạng thái màn hình
- Loading: Skeleton cho từng card số liệu (tải song song, không chặn nhau).
- Empty: Không áp dụng (dashboard luôn có ít nhất số liệu 0).
- Error: Nếu 1 card lỗi tải (VD server monitoring offline theo EX1 của UC-057), card đó hiển thị trạng thái lỗi cục bộ, không chặn các card khác.
- Success: Toàn bộ card hiển thị số liệu mới nhất, tự làm mới định kỳ (gợi ý theo BR-180 nếu áp dụng, không bắt buộc với dashboard này vì không phải Analytics Dashboard chính thức UC-171).

---

## Platform Layout — Web
**Entry/Exit points**: Trang landing sau khi System Admin đăng nhập thành công (UC-034, xem `13_Guest_Common_Screen_Flow.md`). Từ đây điều hướng tới toàn bộ 19 màn hình còn lại qua Sidebar hoặc card shortcut.
**Bố cục**: Dashboard Shell chuẩn — Content Area dùng grid 12 cột: 4 card số liệu hàng trên (`col-span-3` mỗi card), biểu đồ hoạt động hệ thống chiếm `col-span-8`, alert lỗi gần nhất `col-span-4` cạnh biểu đồ, danh sách shortcut dạng card-grid ở cuối trang (`col-span-4` mỗi tile, theo `Layout_Guidelines.md` mục 1.1).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Administrator Dashboard, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Sidebar trái + Topbar + Content dạng grid 12 cột (card số liệu hàng trên, biểu đồ + alert giữa trang, shortcut tile cuối trang).
Thành phần bắt buộc: card tổng số trường, card tổng số user, card trạng thái hệ thống, biểu đồ hoạt động 24h, danh sách cảnh báo lỗi gần nhất, lưới shortcut điều hướng tới 19 màn hình con.
Áp dụng Design System: 00_Foundation/Design_System.md (badge trạng thái theo success/warning/danger).
Trạng thái cần thể hiện: loading (skeleton từng card), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md (sơ đồ điều hướng, node gốc "Dash")
- Report 3.md dòng 1557–1577 (UC-153), 1821–1841 (UC-140), 4971–4991 (UC-149), 4795–4815 (UC-057), 4905–4925 (UC-111) — nguồn số liệu tóm tắt
- traceability_matrix.md (BR-165, BR-180)
