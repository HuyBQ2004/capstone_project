# Tổng hợp Screen Specifications — SystemAdmin (SystemAdmin)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Audit Logs

**Actor**: System Admin | **UC liên quan**: UC-106 (Report 3 §3.2 "View Audit Logs") | **Status**: Draft
**BR liên quan**: BR-162 (nhật ký bất biến), BR-170 (ghi log mọi hành động quan trọng), BR-171 (chính sách lưu trữ), BR-172 (bảo vệ toàn vẹn), BR-174 (truy vết lịch sử vận hành)
**MSG liên quan**: MSG-004 (Session expired), MSG-005 (Access denied)

> Nguồn UC: Report 3.md dòng 4883–4903 (Table 3-149). Ghi chú: trường "Business Rules" gốc trong bảng UC (BR-158–164) là boilerplate lặp lại giống hệt ở nhiều UC khác (UC-057/111/136/149/150/040/058/088) — không khớp nội dung nghiệp vụ của UC này; đã dùng bảng BR đối chiếu chính xác hơn ở `traceability_matrix.md` §1.12 (BR-162, BR-170, BR-171, BR-172, BR-174) làm nguồn chuẩn theo nguyên tắc đã chốt ở `00_UI_Design_Documentation_Plan.md` mục 2.4.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh filter: Date range | DatePicker (range) | Ngày bắt đầu ≤ ngày kết thúc | BR-171 | Mặc định 7 ngày gần nhất |
| 2 | Thanh filter: User ID / Tên | Input (search) | Tìm gần đúng | — | |
| 3 | Thanh filter: Action category | Select | Danh sách hành động cố định (Create/Update/Delete/Approve/Reject/Login...) | BR-170 | |
| 4 | Thanh filter: IP Address | Input | Định dạng IPv4/IPv6 | — | |
| 5 | Bảng Audit Log | Table (đọc-only, không sort/xoá dòng) | Không cho phép edit/xoá bất kỳ dòng nào | BR-162, BR-172 | Cột: Timestamp, User, IP Address, Action, Resource, Status |
| 6 | Nút "Export CSV/PDF" | Button | — | BR-171 | Không có trong Normal Flow gốc của UC-106 nhưng nhất quán với các màn báo cáo khác (BR-178) — **[Giả định — cần team xác nhận]** |
| 7 | Phân trang | Pagination | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table trong lúc truy vấn audit_log server (bảng có thể lớn — cần cảnh báo thời gian tải).
- Empty: "Không có bản ghi audit nào khớp bộ lọc hiện tại" kèm gợi ý nới bộ lọc ngày.
- Error: MSG-004 nếu phiên hết hạn giữa lúc filter; MSG-005 nếu không có quyền (route bị chặn RBAC trước khi vào màn hình).
- Success: Bảng hiển thị đầy đủ, không có trạng thái "success" dạng toast vì đây là màn hình chỉ đọc.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar "Audit Log" (nhóm menu Audit Log, `Navigation_Guidelines.md` mục 1.1). Không có màn hình con — đây là node lá trong sơ đồ điều hướng System Admin.
**Bố cục**: Dashboard Shell chuẩn (`Layout_Guidelines.md` mục 1.1) — Topbar + Sidebar + Content. Content Area: thanh filter ngang trên cùng (grid-cols-12, mỗi filter col-span-3), bảng `Table` chiếm toàn bộ chiều rộng bên dưới, phân trang cuối bảng. Dùng component `Table`/`DataTable` theo `Component_Guidelines.md` mục 1 (không dùng Card list vì đây là actor Web-only, dữ liệu dày đặc).

*(School Administration cũng có quyền xem Audit Logs theo cột "x" ở `14_Screen_Authorization_Matrix.md` khối 2 — School Admin chỉ xem phạm vi trường mình theo BR-176; ngoài phạm vi công việc actor "System Admin" của Nam, xem thêm ghi chú tại School Admin nếu cần màn tương ứng.)*

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Audit Logs, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Sidebar trái + Topbar + Content (filter bar ngang trên, bảng dữ liệu bên dưới, phân trang cuối).
Thành phần bắt buộc: filter Date range, filter User ID/tên, filter Action category, filter IP Address, bảng đọc-only (Timestamp/User/IP/Action/Resource/Status), nút Export.
Áp dụng Design System: 00_Foundation/Design_System.md (bảng dữ liệu dùng màu neutral, không dùng semantic color trừ cột Status).
Trạng thái cần thể hiện: loading (skeleton table), empty, success (bảng đầy đủ).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4883–4903 (Table 3-149 View Audit Logs Use Cases Specification)
- traceability_matrix.md §1.12 (UC-106 ↔ BR-162/170/171/172/174 ↔ MSG-004/005)
- 03_Design/02_ADR/ADR-004.md (audit_log bất biến, chỉ INSERT/SELECT)
- 03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md

# Screen Spec — System Administrator Dashboard

**Actor**: System Admin | **UC liên quan**: entry point (không phải 1 UC riêng — tổng hợp shortcut tới các UC khác) | **Status**: Draft
**BR liên quan**: BR-165 (báo cáo sức khỏe hệ thống hàng tuần), BR-180 (dashboard cập nhật trong 5 phút — nếu tái sử dụng widget Analytics)
**MSG liên quan**: MSG-004, MSG-005

> Đây là node gốc của sơ đồ điều hướng System Admin (`01_System_Admin_Screen_Flow.md`), không map trực tiếp UC nào — tổng hợp shortcut/summary card dẫn tới 18 màn hình còn lại. Nội dung số liệu tóm tắt suy ra từ các UC nguồn liên quan (UC-153 User count, UC-140 School count, UC-149 System Status, UC-057 Activity) — không tự bịa thêm chỉ số ngoài các UC này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card tổng số trường (Active/Inactive/Pending) | Card + số liệu | — | — | Nguồn: UC-140 View School List |
| 2 | Card tổng số user theo role | Card + số liệu | — | — | Nguồn: UC-153 View User Account List |
| 3 | Card trạng thái hệ thống (health-check tóm tắt) | Card + badge trạng thái (success/danger) | Badge màu theo `Design_System.md` mục 3 | — | Nguồn: UC-149 View System Status |
| 4 | Biểu đồ hoạt động hệ thống (CPU/RAM/session) rút gọn | Chart (recharts) | — | BR-165 | Nguồn: UC-057 Monitor System Activities, chỉ hiển thị 24h gần nhất |
| 5 | Danh sách shortcut điều hướng (Quản lý User, Trường, Role, Audit Log, Cấu hình...) | Card link / menu tile | — | — | Ánh xạ đúng 18 màn hình còn lại theo sơ đồ điều hướng |
| 6 | Cảnh báo hệ thống gần nhất (Error Logs tóm tắt) | Alert list rút gọn | — | BR-164 | Nguồn: UC-111 View Error Logs, chỉ 3-5 lỗi gần nhất mức Critical/High |

### Trạng thái màn hình
- Loading: Skeleton cho từng card số liệu (tải song song, không chặn nhau).
- Empty: Không áp dụng (dashboard luôn có ít nhất số liệu 0).
- Error: Nếu 1 card lỗi tải (VD server monitoring offline theo EX1 của UC-057), card đó hiển thị trạng thái lỗi cục bộ, không chặn các card khác.
- Success: Toàn bộ card hiển thị số liệu mới nhất, tự làm mới định kỳ (gợi ý theo BR-180 nếu áp dụng, không bắt buộc với dashboard này vì không phải Analytics Dashboard chính thức UC-171).

---

## Platform Layout — Web
**Entry/Exit points**: Trang landing sau khi System Admin đăng nhập thành công (UC-034, xem `13_Guest_Common_Screen_Flow.md`). Từ đây điều hướng tới toàn bộ 18 màn hình còn lại qua Sidebar hoặc card shortcut.
**Bố cục**: Dashboard Shell chuẩn — Content Area dùng grid 12 cột: 4 card số liệu hàng trên (`col-span-3` mỗi card), biểu đồ hoạt động hệ thống chiếm `col-span-8`, alert lỗi gần nhất `col-span-4` cạnh biểu đồ, danh sách shortcut dạng card-grid ở cuối trang (`col-span-4` mỗi tile, theo `Layout_Guidelines.md` mục 1.1).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Administrator Dashboard, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Sidebar trái + Topbar + Content dạng grid 12 cột (card số liệu hàng trên, biểu đồ + alert giữa trang, shortcut tile cuối trang).
Thành phần bắt buộc: card tổng số trường, card tổng số user, card trạng thái hệ thống, biểu đồ hoạt động 24h, danh sách cảnh báo lỗi gần nhất, lưới shortcut điều hướng tới 18 màn hình con.
Áp dụng Design System: 00_Foundation/Design_System.md (badge trạng thái theo success/warning/danger).
Trạng thái cần thể hiện: loading (skeleton từng card), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/01_System_Admin_Screen_Flow.md (sơ đồ điều hướng, node gốc "Dash")
- Report 3.md dòng 1557–1577 (UC-153), 1821–1841 (UC-140), 4971–4991 (UC-149), 4795–4815 (UC-057), 4905–4925 (UC-111) — nguồn số liệu tóm tắt
- traceability_matrix.md (BR-165, BR-180)

# Screen Spec — Manage User Screen/Dialog

**Actor**: System Admin | **UC liên quan**: UC-026 (Report 3 §3.2 "Create User") | **Status**: Draft
**BR liên quan**: BR-008 (bảo vệ role hệ thống mặc định khi gán), BR-010 (xác minh email trước đăng nhập), BR-012 (định dạng email/SĐT), BR-013 (phân quyền tạo — System Admin tạo user toàn hệ thống)
**MSG liên quan**: MSG-004, MSG-005, MSG-006 (email đã tồn tại), MSG-031 (tạo tài khoản thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Họ tên đầy đủ | Input (text) | Bắt buộc | — | |
| 2 | Email | Input (email) | Bắt buộc, đúng định dạng email, duy nhất trong hệ thống (MSG-006 nếu trùng) | BR-012 | |
| 3 | Số điện thoại | Input (tel) | Định dạng hợp lệ (+84/0xx) | BR-012 | |
| 4 | Vai trò (Role) | Select | Chọn 1 trong các role hệ thống/tuỳ chỉnh đã có | BR-008 | |
| 5 | Trường liên kết (School) | Select (điều kiện hiển thị) | **Bắt buộc nếu Role là role cấp trường** (VD School Administration, Teacher...) | — | Chỉ hiện khi Role thuộc nhóm cấp trường — theo đúng Normal Flow bước 3 |
| 6 | Nút "Tạo tài khoản" | Button (primary) | Disable tới khi field bắt buộc hợp lệ | — | |

### Trạng thái màn hình
- Loading: Button chuyển trạng thái loading khi submit, disable form.
- Empty: Không áp dụng (đây là form tạo mới).
- Error: MSG-006 dưới field Email nếu email đã tồn tại; MSG-004/MSG-005 nếu phiên hết hạn/không đủ quyền; cảnh báo validation nếu SĐT sai định dạng (theo Normal Flow bước 4, chưa có MSG code cụ thể cho lỗi SĐT — dùng banner cảnh báo chung).
- Success: MSG-031 (Toast) + đóng dialog/quay về User Account List, hệ thống gửi email xác minh (BR-010).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create User Account" trên màn `User Account List` (UC-153). Sau khi tạo thành công quay lại `User Account List`.
**Bố cục**: Form ngắn (<8 field) hiện trong `Dialog` giữa màn hình theo `Layout_Guidelines.md` mục 1.2 (không tạo trang riêng). Field xếp 1 cột, field điều kiện "Trường liên kết" hiện/ẩn động ngay dưới field Role.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage User Screen/Dialog (Create), nền tảng Web, actor System Admin.
Bố cục: Dialog giữa màn hình, form 1 cột, 6 field, nút Tạo tài khoản ở cuối dialog.
Thành phần bắt buộc: Họ tên, Email, SĐT, Role (select), School (select có điều kiện), nút Tạo tài khoản.
Áp dụng Design System: 00_Foundation/Design_System.md — lỗi field dùng token danger.
Trạng thái cần thể hiện: error (email trùng), loading (submit), success (toast MSG-031).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1293–1313 (Table 3-7 Create User Use Cases Specification)
- traceability_matrix.md §1.1 (UC-026 ↔ BR-008/010/012/013 ↔ MSG-004/005/006/031)

# Screen Spec — User Account List

**Actor**: System Admin | **UC liên quan**: UC-153 (Report 3 §3.2 "View User Account List") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên/email) | Input (search) | — | — | |
| 2 | Filter theo Role | Select | — | BR-007 | |
| 3 | Filter theo School/Company | Select | — | — | |
| 4 | Filter theo Status (Active/Inactive/Pending) | Select | — | — | |
| 5 | Bảng danh sách user | Table | Cột: Tên, Email, Role, School/Company, Trạng thái | — | |
| 6 | Badge trạng thái | Badge (success/warning/danger) | Active=success, Pending=warning, Inactive=danger | — | Theo `Design_System.md` mục 3 |
| 7 | Nút "Create User Account" | Button (primary, góc trên phải) | — | — | Mở `Manage User Screen/Dialog` |
| 8 | Action cột cuối: toggle trạng thái | IconButton | — | BR-007, BR-008 | Mở `Active/Deactivate User Dialog` |
| 9 | Nút "Export Excel/CSV" | Button | — | — | |
| 10 | Phân trang | Pagination | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có tài khoản nào khớp bộ lọc" + gợi ý xoá bớt filter.
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, filter/search hoạt động realtime (debounce).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Người dùng Hệ thống". Điều hướng ra: `Manage User Screen/Dialog` (Create), `Active/Deactivate User Dialog` (action theo dòng).
**Bố cục**: Dashboard Shell — Page Header có tiêu đề + nút "Create User Account" góc phải; Content: thanh filter/search ngang trên bảng, bảng `Table` chiếm toàn bộ chiều rộng, phân trang cuối trang (theo `Layout_Guidelines.md` mục 1.1, `Component_Guidelines.md` mục 1 — Table cho Web).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình User Account List, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (tiêu đề + nút Create User Account góc phải), filter bar, bảng dữ liệu, phân trang.
Thành phần bắt buộc: search, filter Role/School/Status, bảng (Tên/Email/Role/School/Trạng thái), badge trạng thái, action toggle, nút Export.
Áp dụng Design System: badge trạng thái theo success/warning/danger.
Trạng thái cần thể hiện: loading, empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1557–1577 (Table 3-19 View User Account List Use Cases Specification)
- traceability_matrix.md §1.1 (UC-153 ↔ BR-007 ↔ MSG-004/005)

# Screen Spec — Manage Role Screen

**Actor**: System Admin | **UC liên quan**: UC-022 (Create Role), UC-098 (Update Role), UC-029 (Delete Role) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-008 (bảo vệ role hệ thống mặc định — chỉ role custom được sửa/xoá, và chỉ khi 0 user gán)
**MSG liên quan**: MSG-004, MSG-005, MSG-031 (tạo thành công)

> Màn hình dùng chung 3 UC (Create/Update/Delete Role) qua 1 form + 1 dialog xác nhận xoá — theo đúng sơ đồ điều hướng `01_System_Admin_Screen_Flow.md` (`RL --> MRSCreate/MRSUpdate/MRSDelete`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên Role | Input (text) | Bắt buộc, duy nhất trong hệ thống | BR-008 | Disable khi Update role mặc định (không cho sửa tên role default) |
| 2 | Mô tả | Textarea | Tuỳ chọn | — | |
| 3 | Danh sách Permission (checkbox nhóm theo module) | Checkbox group | Ít nhất phải chọn hợp lệ theo module | — | Lấy danh mục cố định từ `Manage Permission List Screen` (UC-129) |
| 4 | Nút "Lưu" (Create/Update) | Button (primary) | Chỉ enable khi form hợp lệ | BR-008 | Với Update: chỉ hiển thị nếu role là custom |
| 5 | Nút "Xoá Role" (chỉ ở Update) | Button (destructive) | Chỉ hiện với role custom | BR-008 | Mở `AlertDialog` xác nhận — UC-029 |
| 6 | Dialog xác nhận xoá | AlertDialog | Chặn nếu còn user (kể cả đã vô hiệu hoá) đang gán role này | BR-008 | AF1 UC-029: hiển thị số lượng user đang gán, yêu cầu reassign trước |

### Trạng thái màn hình
- Loading: submit form / xoá role.
- Empty: Không áp dụng.
- Error: Validation lỗi tên trùng; nếu cố sửa/xoá role mặc định → chặn với thông báo lỗi rõ ràng (theo Exceptions của UC-098/UC-029); nếu xoá role còn user gán → AF1 hiển thị số lượng, chặn thao tác.
- Success: MSG-031 khi tạo; toast xác nhận chung khi update/xoá thành công (Report 3 không gán MSG code riêng cho Update/Delete Role — **[Giả định — cần team xác nhận]**: dùng MSG-029 "Cập nhật thành công" theo pattern chung ở `error_message.md`).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ `Role List` (nút "Create New Role" hoặc chọn role → "Edit"/"Delete"). Sau khi lưu/xoá quay về `Role List`.
**Bố cục**: Form trung bình (permission checkbox có thể dài) — hiện trong `Sheet` trượt từ phải theo `Layout_Guidelines.md` mục 1.2 thay vì Dialog nhỏ (do danh sách permission nhóm theo module có thể dài hơn 8 field). Checkbox group chia theo module (accordion hoặc section có tiêu đề), nút Lưu/Xoá cố định cuối Sheet.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Role Screen (Create/Update/Delete), nền tảng Web, actor System Admin.
Bố cục: Sheet trượt từ phải, form dọc gồm Tên Role, Mô tả, danh sách Permission theo nhóm module (checkbox), nút Lưu + nút Xoá (chỉ khi Update role custom).
Thành phần bắt buộc: Input Tên Role, Textarea Mô tả, Checkbox group Permission theo module, nút Lưu, nút Xoá + AlertDialog xác nhận xoá.
Áp dụng Design System: nút Xoá dùng variant destructive/token danger.
Trạng thái cần thể hiện: error (tên trùng, role mặc định bị khoá sửa), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1249–1269 (Table 3-5 Create Role), 1449–1467 (Table 3-14 Update Role), 1315–1335 (Table 3-8 Delete Role)
- traceability_matrix.md §1.1 (UC-022/UC-029/UC-098 ↔ BR-008)

# Screen Spec — Role List

**Actor**: System Admin | **UC liên quan**: UC-130 (Report 3 §3.2 "View List Role") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC), BR-008 (bảo vệ role mặc định)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên role) | Input (search) | — | — | |
| 2 | Bảng danh sách Role | Table | Cột: Role Name, Description, User Count, Status (Default/Custom) | BR-007, BR-008 | |
| 3 | Badge loại Role | Badge (info=Default, secondary=Custom) | — | BR-008 | |
| 4 | Nút "Create New Role" | Button (primary, góc trên phải) | — | — | Mở `Manage Role Screen` (Create) |
| 5 | Action: Edit | IconButton | Cho phép với mọi role (Update mô tả/permission chỉ áp dụng role custom — role default có thể xem read-only) | BR-008 | Mở `Manage Role Screen` (Update) |
| 6 | Action: Delete | IconButton | Chỉ hiện/enable với role custom | BR-008 | Mở `Manage Role Screen` (Delete confirm) |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: Không xảy ra thực tế (luôn có ít nhất các role mặc định).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ Role Name/Description/User Count/Status.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Người dùng Hệ thống". Điều hướng ra: `Manage Role Screen` (Create/Update/Delete), `Manage Permission List Screen` (xem danh mục quyền).
**Bố cục**: Dashboard Shell — Page Header (tiêu đề + nút Create New Role), Content: search bar + bảng Table, action cột cuối bảng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Role List, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (nút Create New Role góc phải), bảng dữ liệu (Role Name/Description/User Count/Status).
Thành phần bắt buộc: search, bảng, badge Default/Custom, action Edit/Delete theo dòng.
Áp dụng Design System: badge info cho Default, secondary cho Custom.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1491–1511 (Table 3-16 View List Role Use Cases Specification)
- traceability_matrix.md §1.1 (UC-130 ↔ BR-007/BR-008)

# Screen Spec — Manage Permission List Screen

**Actor**: System Admin | **UC liên quan**: UC-129 (Report 3 §3.2 "View List Permission") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC)
**MSG liên quan**: MSG-004, MSG-005

> Dù tên gọi trong Screen Authorization Matrix có chữ "Manage", Normal Flow thật của UC-129 chỉ mô tả hành vi xem/lọc/export — permission là danh mục cố định trong code (RBAC), không có UC tạo/sửa permission nào tồn tại (xem `01_System_Admin_Screen_Flow.md` ghi chú nghiệp vụ). Màn hình này **chỉ đọc**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (từ khoá) | Input (search) | — | — | |
| 2 | Filter theo Module | Select | — | — | |
| 3 | Bảng danh sách Permission | Table (đọc-only) | Cột: Permission ID, Name, Module/Category, Description | BR-007 | Không có action Create/Edit/Delete nào trên bảng này |
| 4 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: Không xảy ra thực tế (permission là danh mục cố định luôn có dữ liệu).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar (thường là tab con của "Quản lý Người dùng Hệ thống" cạnh Role List) hoặc từ `Manage Role Screen` khi cần tra cứu danh mục quyền trước khi gán. Không có màn hình con.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề, không có nút hành động tạo mới vì đây là màn read-only), Content: search + filter Module, bảng Table.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Permission List Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (không có nút Create), filter bar, bảng dữ liệu read-only.
Thành phần bắt buộc: search, filter Module, bảng (Permission ID/Name/Module/Description), nút Export.
Áp dụng Design System: không dùng badge trạng thái (permission không có trạng thái Active/Inactive).
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm nút Create/Edit/Delete vì đây là màn chỉ đọc.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1469–1489 (Table 3-15 View List Permission Use Cases Specification)
- traceability_matrix.md §1.1 (UC-129 ↔ BR-007)

# Screen Spec — Active/Deactivate User Dialog

**Actor**: System Admin | **UC liên quan**: UC-003 (Report 3 §3.2 "Active/Deactivate User") | **Status**: Draft
**BR liên quan**: BR-007 (RBAC), BR-008 (bảo vệ tài khoản System Admin mặc định duy nhất)
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (cập nhật thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin tài khoản (tên, email, role) | Text hiển thị (read-only) | — | — | Hiển thị để xác nhận đúng tài khoản trước khi toggle |
| 2 | Trạng thái hiện tại → mới | Badge trước/sau | — | — | VD Active → Inactive |
| 3 | Cảnh báo hệ quả (nếu Deactivate) | Alert (warning) | Hiển thị: hủy toàn bộ session, gửi email thông báo | BR-007 | Theo Normal Flow bước 6 |
| 4 | Nút "Xác nhận" | Button (destructive nếu Deactivate) | Bị chặn (disable + thông báo) nếu target là tài khoản System Admin mặc định duy nhất | BR-008 | |
| 5 | Nút "Huỷ" | Button (ghost) | — | — | |

### Trạng thái màn hình
- Loading: Button "Xác nhận" chuyển loading khi xử lý.
- Empty: Không áp dụng.
- Error: Chặn hành động + validation error nếu target là tài khoản System Admin mặc định (Exceptions UC-003); MSG-004/MSG-005.
- Success: MSG-029, đóng dialog, cập nhật lại badge trạng thái trên `User Account List`; nếu deactivate → hệ thống tự hủy toàn bộ session của user đó (BR-007) và gửi email.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ action toggle trạng thái ở dòng tương ứng trên `User Account List`. Đóng lại trả về `User Account List`.
**Bố cục**: `AlertDialog` (hành động không thể hoàn tác ngay lập tức về mặt session) theo `Component_Guidelines.md` mục 1 — không điều hướng sang trang riêng, giữ ngữ cảnh danh sách phía sau.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Active/Deactivate User Dialog, nền tảng Web, actor System Admin.
Bố cục: AlertDialog giữa màn hình, hiển thị thông tin tài khoản + cảnh báo hệ quả + 2 nút Xác nhận/Huỷ.
Thành phần bắt buộc: tên/email/role tài khoản, badge trạng thái trước/sau, alert cảnh báo (hủy session + gửi email), nút Xác nhận (destructive khi deactivate), nút Huỷ.
Áp dụng Design System: nút destructive dùng token danger, alert dùng token warning.
Trạng thái cần thể hiện: error (chặn deactivate tài khoản System Admin mặc định), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1209–1224 (Table Active/Deactivate User Use Cases Specification)
- traceability_matrix.md §1.1 (UC-003 ↔ BR-007/BR-008)

# Screen Spec — Active/Deactivate School Dialog

**Actor**: System Admin | **UC liên quan**: UC-001 (Report 3 §3.2 "Active/Deactivate School") | **Status**: Draft
**BR liên quan**: BR-021 (vô hiệu hóa trường dạng cascade)
**MSG liên quan**: MSG-001 (lỗi hệ thống lưu dữ liệu trường), MSG-004, MSG-005, MSG-011 (theo Exceptions UC-001 gốc — xem ghi chú)

> Ghi chú đối chiếu: Exceptions trong Report 3.md (dòng 1614) liệt kê "MSG-004, MSG-005, MSG-011" — nhưng MSG-011 (`error_message.md`) thực chất là "Homeroom teacher is already assigned to another classroom", không liên quan ngữ nghĩa tới Active/Deactivate School. Nhiều khả năng đây là lỗi copy-paste giữa các UC trong Report 3 gốc (Alternative Flow của UC-001 trong cùng bảng cũng mô tả nhầm nội dung "Reassignment... homeroom teacher" — vốn là AF của UC khác). **[Giả định — cần team xác nhận]**: dùng MSG-001 làm exception chính cho lỗi lưu dữ liệu trường, bỏ qua MSG-011 do không khớp nghiệp vụ.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin trường (tên, mã trường) | Text hiển thị (read-only) | — | — | |
| 2 | Trạng thái hiện tại → mới | Badge trước/sau | — | — | |
| 3 | Cảnh báo hệ quả cascade | Alert (danger) | Liệt kê rõ: toàn bộ Classroom/Staff/Student thuộc trường sẽ bị tạm dừng | BR-021 | Rủi ro cao — cảnh báo rõ ràng trước khi xác nhận |
| 4 | Nút "Xác nhận" | Button (destructive) | Kiểm tra không gây xung đột dữ liệu trước khi cho phép | BR-021 | |
| 5 | Nút "Huỷ" | Button (ghost) | — | — | |

### Trạng thái màn hình
- Loading: Button "Xác nhận" loading khi xử lý cascade (có thể mất vài giây do cascade nhiều bảng).
- Empty: Không áp dụng.
- Error: MSG-001 nếu lưu thất bại; MSG-004/MSG-005.
- Success: Toast xác nhận (Report 3 không gán MSG success riêng cho UC-001 — **[Giả định]**: dùng thông báo chung "Cập nhật trạng thái trường thành công"), cascade tạm dừng Classroom/Staff/Student (BR-021), ghi audit log.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ action toggle trạng thái ở dòng trên `School List`. Đóng lại trả về `School List`.
**Bố cục**: `AlertDialog` (hành động rủi ro cao, cascade nhiều bảng — cần xác nhận hai bước theo ghi chú nghiệp vụ ở `01_System_Admin_Screen_Flow.md`), không điều hướng trang riêng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Active/Deactivate School Dialog, nền tảng Web, actor System Admin.
Bố cục: AlertDialog giữa màn hình, hiển thị thông tin trường + cảnh báo cascade rõ ràng + 2 nút Xác nhận/Huỷ.
Thành phần bắt buộc: tên/mã trường, badge trạng thái trước/sau, alert cảnh báo cascade (Classroom/Staff/Student bị tạm dừng), nút Xác nhận (destructive), nút Huỷ.
Áp dụng Design System: alert cảnh báo dùng token danger vì rủi ro cao (cascade).
Trạng thái cần thể hiện: error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1601–1621 (Table 3-20 Active/Deactivate School Use Cases Specification)
- traceability_matrix.md §1.2 (UC-001 ↔ BR-021)
- business_rules.md BR-021

# Screen Spec — School List

**Actor**: System Admin | **UC liên quan**: UC-140 (Report 3 §3.2 "View School List") | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường theo vai trò), BR-025 (sức chứa lớp — tham chiếu gián tiếp qua số liệu tổng hợp trường)
**MSG liên quan**: MSG-004, MSG-005

> Ghi chú: Report 3 gán "Business Rules: BR-022, BR-025" cho UC-140 — cả hai đều không mô tả trực tiếp nghiệp vụ "xem danh sách trường" (BR-022 là quyền xem chính sách, BR-025 là sức chứa lớp). Có khả năng đây cũng là boilerplate không khớp hoàn toàn — giữ lại trích dẫn theo đúng nguồn gốc UC nhưng đánh dấu **[Giả định — cần team xác nhận]** về mức độ liên quan thực tế.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên trường) | Input (search) | — | — | |
| 2 | Filter theo Status (Active/Inactive/Pending) | Select | — | — | |
| 3 | Bảng danh sách trường | Table | Cột: Name, Code, Tax ID, Status | — | |
| 4 | Badge trạng thái | Badge (success=Active, danger=Inactive, warning=Pending) | — | — | |
| 5 | Action: xem chi tiết | Row click / IconButton | — | — | Mở `School Detail` |
| 6 | Action: toggle trạng thái | IconButton | — | BR-021 | Mở `Active/Deactivate School Dialog` |
| 7 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có trường nào khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Trường học". Điều hướng ra: `School Detail`, `Active/Deactivate School Dialog`.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề, không có nút "Create School" vì trường được tạo qua luồng đăng ký UC-070 Register School, không phải do System Admin tạo trực tiếp theo Normal Flow hiện có), search + filter Status, bảng Table.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School List, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header, filter bar (search + status), bảng dữ liệu (Name/Code/Tax ID/Status).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, action xem chi tiết + toggle trạng thái, nút Export.
Áp dụng Design System: badge success/warning/danger theo trạng thái trường.
Trạng thái cần thể hiện: loading, empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1821–1841 (Table 3-29 View School List Use Cases Specification)
- traceability_matrix.md §1.2 (UC-140)

# Screen Spec — School Detail

**Actor**: System Admin | **UC liên quan**: UC-122 (Report 3 §3.2 "View Information School") | **Status**: Draft
**BR liên quan**: BR-022 (quyền xem chính sách trường theo vai trò), BR-025 (sức chứa lớp — hiển thị trong thống kê tổng hợp)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên trường | Text hiển thị | — | — | |
| 2 | Địa chỉ | Text hiển thị | — | — | |
| 3 | Mã số thuế (Tax ID) | Text hiển thị | — | — | |
| 4 | Email liên hệ | Text hiển thị | — | — | |
| 5 | Toạ độ GPS (latitude/longitude) | Text hiển thị + mini-map | Khoá hiển thị `coordinateLocked` nếu trường đã Active | BR-028 | Badge "Đã khoá tọa độ" nếu `coordinateLocked = true` |
| 6 | Giờ hoạt động | Text hiển thị | — | — | |
| 7 | Liên hệ khẩn cấp | Text hiển thị | — | — | |
| 8 | Thống kê tổng hợp (số lớp, số học sinh, số nhân viên) | Card số liệu | — | BR-025 | |
| 9 | Nút "Edit" | Button | — | — | Mở `Update School Screen/Dialog` |
| 10 | Nút "Export PDF" | Button | — | — | Theo Normal Flow bước 4 |

### Trạng thái màn hình
- Loading: Skeleton card khi tải hồ sơ trường.
- Empty: Không áp dụng (trường luôn có hồ sơ tối thiểu khi đã đăng ký).
- Error: MSG-004/MSG-005.
- Success: Hiển thị đầy đủ hồ sơ trường.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `School List` (click 1 dòng). Điều hướng ra: `Update School Screen/Dialog` (Edit).
**Bố cục**: Dashboard Shell — Page Header (tên trường + nút Edit/Export góc phải), Content chia 2 khối: thông tin cơ bản (`col-span-8`) cạnh card thống kê tổng hợp (`col-span-4`), khối toạ độ GPS + mini-map bên dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Detail, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (tên trường + nút Edit/Export), 2 cột: thông tin cơ bản + card thống kê, khối toạ độ GPS/mini-map bên dưới.
Thành phần bắt buộc: tên/địa chỉ/tax ID/email/giờ hoạt động/liên hệ khẩn cấp, toạ độ GPS kèm badge khoá, card thống kê (lớp/học sinh/nhân viên), nút Edit, nút Export PDF.
Áp dụng Design System: badge khoá tọa độ dùng token info hoặc secondary.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1799–1819 (Table 3-27 View Information School Use Cases Specification)
- traceability_matrix.md §1.2 (UC-122)
- business_rules.md BR-028 (coordinateLocked)

# Screen Spec — Update School Screen/Dialog

**Actor**: System Admin | **UC liên quan**: UC-095 (Report 3 §3.2 "Update Information School") | **Status**: Draft
**BR liên quan**: BR-028 (xác minh & khóa tọa độ GPS), BR-030 (kiểm tra ràng buộc vận hành liên quan liên kết học sinh-phụ huynh, áp dụng gián tiếp nếu sửa tham số ảnh hưởng)
**MSG liên quan**: MSG-001 (lỗi lưu dữ liệu trường), MSG-004, MSG-005, MSG-029 (cập nhật thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên trường | Input (text) | Bắt buộc | — | |
| 2 | Địa chỉ | Textarea | Bắt buộc | — | |
| 3 | Toạ độ GPS (latitude/longitude) | Input (number) x2 | latitude ∈ [-90, 90], longitude ∈ [-180, 180]; **disable nếu `coordinateLocked = true`** | BR-028 | Chỉ System Admin có quyền mở khoá (đặt lại `coordinateLocked = false`) kèm lý do bằng văn bản bắt buộc — xem field 4 |
| 4 | Nút "Mở khoá toạ độ" + lý do (chỉ hiện khi đã khoá) | Button + Textarea (lý do bắt buộc) | Lý do bắt buộc, ghi vào audit log | BR-028 | Ngoại lệ đặc biệt chỉ System Admin |
| 5 | Mã số thuế | Input (text) | Định dạng mã số thuế hợp lệ (MSG-007 nếu sai) | — | |
| 6 | Email chính thức | Input (email) | Định dạng email hợp lệ | — | |
| 7 | Tham số vận hành (giờ hoạt động, bán kính geofence 50–500m...) | Input nhóm | Geofence trong khoảng 50–500m | BR-028 | Bán kính geofence mặc định 200m |
| 8 | Nút "Lưu" | Button (primary) | — | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading khi submit.
- Empty: Không áp dụng (form pre-filled từ School Detail).
- Error: MSG-001 (lỗi hệ thống lưu), MSG-007 (sai mã số thuế), MSG-004/MSG-005; validation riêng nếu tọa độ ngoài khoảng hợp lệ hoặc bị khoá.
- Success: MSG-029, quay về `School Detail` với dữ liệu mới; nếu vừa xác nhận tọa độ lần đầu (trường chuyển ACTIVE) → `coordinateLocked` tự động = true (BR-028).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Edit" trên `School Detail`. Sau khi lưu quay về `School Detail`.
**Bố cục**: Form trung bình (~8 field, có nhóm tham số vận hành) — hiện trong `Sheet` trượt từ phải theo `Layout_Guidelines.md` mục 1.2 (không phải Dialog nhỏ do có nhóm tham số geofence + toạ độ). Trường toạ độ hiển thị kèm mini-map preview khi thay đổi giá trị.

*(Cập nhật 2026-07-01: màn `Campus Config` đã bị loại bỏ hoàn toàn — hệ thống không quản lý campus. Bán kính geofence không còn ở form này, đã chuyển hẳn về `Web/SchoolAdministration/37_SchoolConfiguration.md` theo đúng BR-028.)*

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update School Screen/Dialog, nền tảng Web, actor System Admin.
Bố cục: Sheet trượt từ phải, form dọc nhóm theo section (Thông tin cơ bản / Toạ độ GPS / Tham số vận hành), nút Lưu cố định cuối Sheet.
Thành phần bắt buộc: tên trường, địa chỉ, toạ độ GPS (disable khi khoá) + nút mở khoá kèm lý do, mã số thuế, email, tham số vận hành (giờ hoạt động), nút Lưu.
KHÔNG thêm field bán kính geofence — đã chuyển về School Administration (37_SchoolConfiguration.md).
Áp dụng Design System: field disable dùng màu neutral nhạt, cảnh báo khoá toạ độ dùng token info.
Trạng thái cần thể hiện: error (toạ độ ngoài khoảng, mã số thuế sai), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1781–1797 (Table 3-26 Update Information School Use Cases Specification)
- traceability_matrix.md §1.2 (UC-095 ↔ BR-028)
- business_rules.md BR-028 (quy tắc toạ độ GPS/coordinateLocked — phần bán kính geofence đã chuyển về School Administration)

# Screen Spec — Campus Config — ❌ ĐÃ LOẠI BỎ (2026-07-01)

**Trạng thái**: **KHÔNG triển khai** — chủ dự án xác nhận hệ thống không quản lý campus. File giữ lại chỉ để lưu lịch sử quyết định, không phải nguồn cho Wireframe/Prototype. Field bán kính geofence (BR-028) nay CONFIRMED chính thức thuộc `Web/SchoolAdministration/37_SchoolConfiguration.md`. Chi tiết đầy đủ xem `13_CampusConfig.md`.

## AI Generation Prompt ⭐

```
[KHÔNG tạo wireframe cho màn hình này — đã loại bỏ 2026-07-01, không tồn tại trong hệ thống thật.]
```

# Screen Spec — Backup Management

**Actor**: System Admin | **UC liên quan**: bundled trong UC-149 (Report 3 §3.2 "View System Status", bước 4) | **Status**: Draft
**BR liên quan**: BR-160 (sao lưu CSDL tự động hàng tuần), BR-161 (bảo vệ file sao lưu)
**MSG liên quan**: MSG-050 (sao lưu thành công), MSG-070 (cảnh báo dung lượng backup gần đầy)

> **Nguồn**: Không phải UC riêng — Normal Flow thật của UC-149 (View System Status) bước 4 nêu rõ: "Load and display the configuration and operating status of the weekly backup scheduler (BR-160)". Đây là 1 panel hiển thị bên trong System Status, không có luồng "tạo backup thủ công" nào được đặc tả trong Report 3. Nội dung dưới đây tách riêng thành 1 file theo đúng tên trong Screen Authorization Matrix, nhưng bố cục logic vẫn là panel con của System Status.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Lịch backup (Chủ nhật 02:00 sáng) | Text hiển thị | — | BR-160 | Cố định theo cấu hình hệ thống, không cho sửa lịch trên UI (chưa có UC "Configure Backup Schedule") |
| 2 | Trạng thái lần backup gần nhất (thành công/thất bại + thời gian) | Badge + timestamp | success/danger | BR-160 | |
| 3 | Dung lượng backup đã dùng | Progress bar + % | Cảnh báo khi ≥ 92% (MSG-070) | BR-161 | |
| 4 | Vị trí lưu trữ (cloud off-site) | Text hiển thị | — | BR-161 | Không hiển thị chi tiết credential (bảo mật) |
| 5 | Lịch sử các lần backup gần đây | Table rút gọn (5-10 dòng) | — | BR-160 | **[Giả định — cần team xác nhận]**: Normal Flow gốc chỉ nói "display configuration and operating status", không xác nhận có lịch sử dạng bảng hay không |

### Trạng thái màn hình
- Loading: Skeleton panel.
- Empty: Không áp dụng (backup chạy tự động hàng tuần, luôn có ít nhất 1 bản ghi sau tuần đầu vận hành).
- Error: Badge danger nếu lần backup gần nhất thất bại; MSG-070 nếu dung lượng gần đầy.
- Success: MSG-050 hiển thị dạng log/badge (backup được thực hiện tự động, không phải hành động người dùng chủ động trigger).

---

## Platform Layout — Web
**Entry/Exit points**: Là 1 panel/tab bên trong `System Status Screen` (UC-149) — xem `18_SystemStatusScreen.md`. Có thể truy cập trực tiếp qua Sidebar nếu team quyết định tách trang riêng theo tên gọi trong Screen Authorization Matrix.
**Bố cục**: Card panel trong Dashboard Shell — nếu là tab con của System Status: nằm trong `Tabs` cùng health-check dashboard; nếu tách trang riêng: Page Header + Content đơn giản 1 cột (không cần bảng dữ liệu dày đặc).

## AI Generation Prompt ⭐

```
Tạo wireframe cho panel Backup Management, nền tảng Web, actor System Admin.
Bố cục: Card panel (trong System Status hoặc trang riêng) — lịch backup, trạng thái lần gần nhất, dung lượng đã dùng (progress bar), vị trí lưu trữ, lịch sử rút gọn.
Thành phần bắt buộc: text lịch backup cố định, badge trạng thái, progress bar dung lượng, bảng lịch sử rút gọn.
Áp dụng Design System: badge success/danger cho trạng thái, progress bar chuyển sang warning khi ≥92%.
Trạng thái cần thể hiện: success, warning (dung lượng gần đầy), error (backup thất bại).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm nút "Backup thủ công" vì không có UC nào mô tả hành vi này.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4971–4991 (Table 3-153 View System Status Use Cases Specification, bước 4 Normal Flow)
- business_rules.md BR-160, BR-161
- error_message.md MSG-050, MSG-070

# Screen Spec — Security Policies

**Actor**: System Admin | **UC liên quan**: UC-184 - Configure Security Policies (Report 3.md §3.2.12.13 — **UC mới**, thêm 2026-07-01 qua audit A8) | **Status**: Draft
**BR liên quan**: BR-006 (chính sách độ mạnh mật khẩu), BR-002 (khóa tài khoản sau 5 lần sai — lockout 15 phút), BR-004 (session timeout 30 phút), BR-170 (audit trail)
**MSG liên quan**: MSG-004, MSG-005, MSG-029

> **Đã xác nhận 2026-07-01 (audit A8)**: đây là 1 trong 8 màn A8 **bắt buộc phải thêm UC mới** — không có UC nào có sẵn (kể cả UC-171/172 Reporting & Analytics) phù hợp vì đây là màn hình cấu hình (configuration), không phải xem/xuất báo cáo. UC-184 - Configure Security Policies được viết mới tại Report 3.md §3.2.12.13, xác nhận System Admin được phép **xem và sửa** các tham số BR-002/BR-004/BR-006 (trước đây chỉ là hằng số cứng, chưa có UI cấu hình). **[Giả định còn lại — cần team xác nhận trước khi code hoá]**: phạm vi giá trị cho phép chỉnh sửa (khoảng min-max) chưa được xác định, xem Assumptions của UC-184.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Số lần đăng nhập sai tối đa trước khi khoá | Input (number, editable) | Mặc định 5 lần | BR-002 | UC-184 Normal Flow bước 3 xác nhận cho sửa |
| 2 | Thời gian khoá tài khoản | Input (number, phút) | Mặc định 15 phút | BR-002 | |
| 3 | Thời gian session timeout | Input (number, phút) | Mặc định 30 phút | BR-004 | |
| 4 | Chính sách độ mạnh mật khẩu (độ dài, ký tự đặc biệt, không trùng 3 mật khẩu gần nhất) | Checkbox group / Input | — | BR-006 | |
| 5 | Nút "Lưu" | Button (primary) | — | — | UC-184 Normal Flow bước 5: lưu + ghi audit trail (BR-170) + MSG-029 |

### Trạng thái màn hình
- Loading: `Skeleton` cho form.
- Empty: Không áp dụng.
- Error: MSG-004/MSG-005. Nếu giá trị ngoài khoảng cho phép: validation error tại field (UC-184 Exceptions — **[Giả định — chưa có MSG code riêng cho lỗi khoảng giá trị]**).
- Success: MSG-029 "Cập nhật hồ sơ thành công".

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Cấu hình Hệ thống" (cạnh AI Settings/External Integrations).
**Bố cục**: **[Giả định]** — Dashboard Shell, form 1 cột nhóm theo section (Đăng nhập/Khoá tài khoản, Session, Mật khẩu).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Security Policies, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell, form 1 cột nhóm theo section (Đăng nhập/Khoá tài khoản, Session, Mật khẩu), nút Lưu.
Thành phần bắt buộc: 4 field cấu hình (số lần sai tối đa, thời gian khoá, session timeout, chính sách mật khẩu) + nút Lưu.
Trạng thái cần thể hiện: loading, error (validation khoảng giá trị), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.12.13 (UC-184 Configure Security Policies — UC mới, bổ sung 2026-07-01)
- traceability_matrix.md §1.12 (UC-184)
- business_rules.md BR-002, BR-004, BR-006, BR-170
- 14_Screen_Authorization_Matrix.md khối 2

# Screen Spec — AI Settings

**Actor**: System Admin | **UC liên quan**: UC-040 (Report 3 §3.2 "Manage External Integrations" — 1 phần, tab Gemini AI) | **Status**: Draft
**BR liên quan**: BR-163 (kiểm tra kết nối tích hợp bên ngoài), BR-164 (mã hóa thông tin xác thực tích hợp)
**MSG liên quan**: MSG-004, MSG-005 (Exceptions gốc); MSG-069 (độ trễ API cao — tham chiếu warning liên quan)

> **Nguồn**: "AI Settings" **không phải** 1 UC độc lập. Normal Flow thật của UC-040 (Manage External Integrations) gộp chung cấu hình 4 dịch vụ: GPS Service, Gmail SMTP, Firebase Cloud Messaging, và Gemini AI trong cùng 1 màn hình "External Integrations" (dạng Tabs). "AI Settings" trong Screen Authorization Matrix nhiều khả năng là 1 tab/filter của màn hình đó — xem `01_System_Admin_Screen_Flow.md`. File này đặc tả riêng tab Gemini AI theo đúng tên gọi trong ma trận, nhưng khuyến nghị implement như 1 tab trong 1 màn "External Integrations" chung, không phải 4 trang riêng biệt.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tabs chọn dịch vụ (GPS/Gmail/Firebase/Gemini AI) | Tabs | — | — | Tab đang mở: "Gemini AI" |
| 2 | API Endpoint | Input (url) | Bắt buộc | — | |
| 3 | OAuth2 Client Credentials / API Key | Input (password, ẩn giá trị) | Bắt buộc, mã hóa khi lưu | BR-164 | |
| 4 | Nút "Test Connection" | Button (secondary) | Phải test thành công trước khi cho phép "Save" | BR-163 | AF1: nếu thất bại, hiển thị lỗi chi tiết và chặn lưu |
| 5 | Nút "Save" | Button (primary) | Disable tới khi Test Connection thành công | BR-163, BR-164 | |
| 6 | Trạng thái kết nối hiện tại | Badge (success/danger) | — | — | |

### Trạng thái màn hình
- Loading: Button "Test Connection"/"Save" chuyển loading.
- Empty: Chưa cấu hình → hiển thị placeholder "Chưa kết nối Gemini AI".
- Error: EX1 thiếu field bắt buộc (MSG-004 dạng validation chung); AF1 Test Connection thất bại → hiển thị lỗi chi tiết (VD "Invalid API key"), chặn Save.
- Success: Lưu cấu hình thành công (mã hóa credentials), ghi audit log bất biến (BR-162 tham chiếu chéo), hiển thị thông báo thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Cấu hình Hệ thống" → "External Integrations" → tab "Gemini AI". Không có màn hình con.
**Bố cục**: Dashboard Shell — Content dùng `Tabs` ngang trên cùng (4 tab GPS/Gmail/Firebase/Gemini AI), nội dung tab là form ngắn (`col-span-6`, căn giữa) gồm endpoint + credentials + nút Test/Save.

## AI Generation Prompt ⭐

```
Tạo wireframe cho tab AI Settings (Gemini AI) trong màn hình External Integrations, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Tabs ngang (GPS/Gmail/Firebase/Gemini AI đang mở), form ngắn giữa trang.
Thành phần bắt buộc: API Endpoint, API Key/OAuth2 credentials (ẩn giá trị), nút Test Connection, nút Save (disable tới khi test thành công), badge trạng thái kết nối.
Áp dụng Design System: badge success/danger cho trạng thái kết nối.
Trạng thái cần thể hiện: error (test connection thất bại), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4777–4793 (Table 3-144 Manage External Integrations Use Cases Specification)
- traceability_matrix.md §1.12 (UC-040 ↔ BR-163/BR-164)
- 03_Design/02_ADR/ADR-003.md (RAG/Gemini pipeline liên quan)

# Screen Spec — System Activity Monitoring

**Actor**: System Admin | **UC liên quan**: UC-057 (Report 3 §3.2 "Monitor System Activities") | **Status**: Draft
**BR liên quan**: BR-165 (báo cáo sức khỏe hệ thống hàng tuần), BR-170 (ghi nhật ký mọi hành động quan trọng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chart CPU load | Chart (recharts, line) | Cập nhật gần thời gian thực | — | |
| 2 | Chart RAM load | Chart (recharts, line) | — | — | |
| 3 | Chart Disk I/O | Chart (recharts, line) | — | — | |
| 4 | Chỉ số Active user sessions | Card số liệu | — | — | |
| 5 | Chỉ số Database query pool load | Card số liệu / gauge | — | — | |
| 6 | Báo cáo tổng hợp hàng tuần | Card/Link "Xem báo cáo tuần" | — | BR-165 | |

### Trạng thái màn hình
- Loading: Skeleton cho từng chart/card.
- Empty: Không áp dụng.
- Error: EX1 — "Server monitoring API is unreachable" → hiển thị banner lỗi "Dịch vụ giám sát đang offline" thay cho chart.
- Success: Chart/card cập nhật số liệu mới nhất.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Không có màn hình con (có thể link sang báo cáo tuần dạng export).
**Bố cục**: Dashboard Shell — Content grid 12 cột: 3 chart CPU/RAM/Disk I/O xếp `col-span-4` mỗi chart hàng trên, 2 card chỉ số (Active sessions, DB pool) `col-span-6` hàng dưới, card báo cáo tuần cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Activity Monitoring, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — 3 chart line (CPU/RAM/Disk I/O) hàng trên, 2 card chỉ số (Active sessions/DB pool) hàng dưới, card link báo cáo tuần.
Thành phần bắt buộc: 3 chart, 2 card số liệu, card báo cáo tuần.
Áp dụng Design System: dùng recharts với màu theo token Design_System.md.
Trạng thái cần thể hiện: loading, error (dịch vụ giám sát offline), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4795–4815 (Table 3-145 Monitor System Activities Use Cases Specification)
- traceability_matrix.md §1.12 (UC-057 ↔ BR-165, BR-170)

# Screen Spec — System Status Screen

**Actor**: System Admin | **UC liên quan**: UC-149 (Report 3 §3.2 "View System Status") | **Status**: Draft
**BR liên quan**: BR-160 (sao lưu tự động), BR-165 (báo cáo sức khỏe hệ thống hàng tuần)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Health-check dashboard các dịch vụ lõi | Card list (mỗi service 1 card, badge success/danger) | Health-check thực hiện ngay khi vào trang (online) | — | VD: Database, Email Service, Firebase, Gemini AI, GPS Service |
| 2 | Panel trạng thái backup scheduler | Panel (xem `14_BackupManagement.md`) | — | BR-160 | Bundled trong UC-149 bước 4 |
| 3 | Nút "Export" (CSV/PDF) | Button | — | — | AF1 Export System Status Records |

### Trạng thái màn hình
- Loading: Skeleton cho health-check cards trong lúc chạy online health check.
- Empty: Không áp dụng.
- Error: Card service nào offline hiển thị badge danger + thời gian mất kết nối gần nhất.
- Success: Tất cả card hiển thị trạng thái mới nhất; Export tải file CSV/PDF (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Chứa panel con `Backup Management`.
**Bố cục**: Dashboard Shell — Page Header (nút Export góc phải), Content: card-grid health-check dịch vụ lõi (`col-span-3` mỗi card), panel Backup Management bên dưới chiếm toàn bộ chiều rộng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Status Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (nút Export), card-grid health-check dịch vụ lõi, panel Backup Management bên dưới.
Thành phần bắt buộc: card health-check từng service (badge success/danger), panel backup scheduler, nút Export.
Áp dụng Design System: badge success/danger cho từng service.
Trạng thái cần thể hiện: error (service offline), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4971–4991 (Table 3-153 View System Status Use Cases Specification)
- traceability_matrix.md §1.12 (UC-149 ↔ BR-165)
- 14_BackupManagement.md (panel con)

# Screen Spec — Error Logs Screen

**Actor**: System Admin | **UC liên quan**: UC-111 (Report 3 §3.2 "View Error Logs") | **Status**: Draft
**BR liên quan**: BR-164 (mã hóa thông tin xác thực — áp dụng khi log chứa request metadata nhạy cảm)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Severity | Select | — | — | |
| 2 | Filter theo Date range | DatePicker (range) | — | — | |
| 3 | Bảng danh sách lỗi | Table | Cột: Timestamp, Error Message, Severity, Module/Class, Path | — | Sort theo severity/timestamp |
| 4 | Badge Severity | Badge (danger=Critical/High, warning=Medium, info=Low) | — | — | |
| 5 | Panel chi tiết lỗi (khi chọn 1 dòng) | Detail panel/Sheet | Hiển thị stack trace, request metadata, session ảnh hưởng | BR-164 | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có lỗi nào trong khoảng thời gian đã chọn" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Bảng + panel chi tiết hiển thị đầy đủ khi chọn 1 lỗi.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Không có màn hình con — chi tiết lỗi mở dưới dạng Sheet/panel trong cùng trang.
**Bố cục**: Dashboard Shell — filter bar trên bảng, bảng `Table` bên trái/toàn bộ, panel chi tiết mở dạng `Sheet` trượt từ phải khi chọn 1 dòng (theo `Layout_Guidelines.md` mục 1.2).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Error Logs Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — filter bar (Severity, Date range), bảng dữ liệu lỗi, Sheet chi tiết trượt từ phải khi chọn 1 dòng.
Thành phần bắt buộc: filter Severity/Date, bảng (Timestamp/Error Message/Severity/Module/Path), badge severity, panel chi tiết (stack trace, request metadata, session).
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty (không có lỗi), success (có lỗi + chi tiết mở).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4905–4925 (Table 3-150 View Error Logs Use Cases Specification)
- traceability_matrix.md §1.12 (UC-111 ↔ BR-164)

# Screen Spec — Usage Statistics Screen

**Actor**: System Admin | **UC liên quan**: UC-150 (Report 3 §3.2 "View Usage Statistics") | **Status**: Draft
**BR liên quan**: BR-165 (báo cáo sức khỏe hệ thống và số liệu người dùng hàng tuần)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Date range | DatePicker (range) | — | — | |
| 2 | Filter theo Role | Select | — | — | |
| 3 | Chart Active users theo ngày/tuần | Chart (recharts, bar/line) | — | BR-165 | |
| 4 | Bảng Top use case được gọi nhiều nhất | Table rút gọn | — | BR-165 | |
| 5 | Chart Dung lượng lưu trữ sử dụng | Chart (recharts, gauge/donut) | — | — | |
| 6 | Chart API request count | Chart (recharts, line) | — | BR-165 | |

### Trạng thái màn hình
- Loading: Skeleton cho từng chart/bảng.
- Empty: Không áp dụng (luôn có ít nhất số liệu 0 trong khoảng thời gian chọn).
- Error: MSG-004/MSG-005.
- Success: Chart/bảng cập nhật theo filter Date range/Role.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Không có màn hình con.
**Bố cục**: Dashboard Shell — filter bar (Date range, Role) trên cùng, Content grid 12 cột: chart Active users `col-span-8`, bảng Top use case `col-span-4` cạnh nhau, 2 chart Dung lượng/API request `col-span-6` mỗi chart hàng dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Usage Statistics Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — filter bar (Date range, Role), chart Active users + bảng Top use case hàng trên, 2 chart (Dung lượng, API request) hàng dưới.
Thành phần bắt buộc: filter Date/Role, chart Active users, bảng Top use case, chart dung lượng lưu trữ, chart API request count.
Áp dụng Design System: dùng recharts với token màu Design_System.md.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4993–5013 (Table 3-154 View Usage Statistics Use Cases Specification)
- traceability_matrix.md §1.12 (UC-150 ↔ BR-165)
