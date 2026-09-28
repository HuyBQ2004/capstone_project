# Tổng hợp Screen Specifications — SchoolAdministration (SchoolAdministration)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — School Administration Dashboard

**Actor**: School Administration | **UC liên quan**: entry point (không phải 1 UC riêng — tổng hợp shortcut) | **Status**: Draft
**BR liên quan**: BR-176 (phạm vi báo cáo giới hạn theo trường), BR-180 (dashboard cập nhật trong 5 phút nếu tái sử dụng widget Analytics)
**MSG liên quan**: MSG-004, MSG-005

> Node gốc của sơ đồ điều hướng School Administration (`02_School_Administration_Screen_Flow.md`). Số liệu tóm tắt suy ra từ các UC nguồn liên quan (UC-114 Incident count, UC-131/127 Partnership pending count, UC-128 Menu pending, UC-027 Compliance hôm nay) — không bịa thêm chỉ số ngoài các UC này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card số sự cố đang mở (Active/Under Investigation) | Card + số liệu | Badge danger nếu > 0 | — | Nguồn: UC-114 View Food Poisoning Incident List |
| 2 | Card số hồ sơ đối tác đang chờ duyệt (Supplier + Caterer) | Card + số liệu | — | — | Nguồn: UC-131, UC-127 (lọc Pending) |
| 3 | Card số thực đơn chờ duyệt | Card + số liệu | — | — | Nguồn: UC-128 View List Menu (lọc Pending Approval) |
| 4 | Card trạng thái tuân thủ hôm nay (3-bước kiểm tra) | Card + badge | success/warning/danger | BR-091 | Nguồn: UC-027 Daily Food Safety Monitoring |
| 5 | Danh sách shortcut điều hướng theo module | Card link/menu tile | — | — | Ánh xạ tới các nhóm: Đối tác, Thực đơn, Lớp/Học sinh, Sự cố, Thông báo, Nhân viên, Báo cáo |
| 6 | Cảnh báo gần nhất (vi phạm/override abuse) | Alert list rút gọn | — | BR-189, BR-190 | Nguồn: UC-181 Violation Registry, UC-183 |

### Trạng thái màn hình
- Loading: Skeleton từng card, tải song song.
- Empty: Không áp dụng (dashboard luôn có số liệu, kể cả 0).
- Error: Card lỗi tải hiển thị trạng thái lỗi cục bộ, không chặn card khác.
- Success: Card hiển thị số liệu mới nhất trong phạm vi trường (BR-176).

---

## Platform Layout — Web
**Entry/Exit points**: Trang landing sau khi School Administration đăng nhập thành công. Điều hướng tới toàn bộ các nhóm màn hình còn lại qua Sidebar hoặc card shortcut.
**Bố cục**: Dashboard Shell — Content grid 12 cột: 4 card số liệu hàng trên (`col-span-3`), danh sách shortcut dạng card-grid theo module ở giữa trang, alert cảnh báo gần nhất cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Administration Dashboard, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Sidebar trái + Topbar + Content dạng grid (card số liệu hàng trên, shortcut tile giữa trang, alert cuối trang).
Thành phần bắt buộc: card sự cố đang mở, card đối tác chờ duyệt, card thực đơn chờ duyệt, card trạng thái tuân thủ hôm nay, lưới shortcut theo module, alert cảnh báo vi phạm gần nhất.
Áp dụng Design System: badge success/warning/danger theo trạng thái.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md (node gốc "Dash")
- Report 3.md (UC-114, UC-131, UC-127, UC-128, UC-027 — nguồn số liệu tóm tắt)
- traceability_matrix.md (BR-176, BR-180, BR-189, BR-190)

# Screen Spec — Catering Partnership Requests

**Actor**: School Administration | **UC liên quan**: UC-127 (Report 3 §3.2 "View List Service Caterers", lọc Pending) | **Status**: Draft
**BR liên quan**: — (Report 3 không gán BR trực tiếp cho UC-127)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên công ty) | Input (search) | — | — | |
| 2 | Filter theo Status | Select | Mặc định lọc "Pending" theo tên màn hình, nhưng cho phép xem cả Approved/Rejected/Suspended | — | |
| 3 | Bảng danh sách Service Caterers | Table | Cột: Tên công ty, Trạng thái hợp đồng, Ngày đăng ký | — | |
| 4 | Badge trạng thái | Badge (warning=Pending, success=Approved, danger=Rejected/Suspended) | — | — | |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Service Caterers Detail` |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: AF1 UC-127 — "Không tìm thấy công ty catering khớp bộ lọc", gợi ý xoá bớt filter.
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Nhà cung cấp". Điều hướng ra: `Service Caterers Detail`.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề), filter bar (search + status) trên bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Catering Partnership Requests, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (search + status), bảng dữ liệu (Tên công ty/Trạng thái/Ngày đăng ký).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, action xem chi tiết.
Áp dụng Design System: badge warning/success/danger theo trạng thái.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2577–2593 (Table 3-59 View List Service Caterers Use Cases Specification)
- traceability_matrix.md §1.4 (UC-127)

# Screen Spec — Supplier Partnership Requests

**Actor**: School Administration | **UC liên quan**: UC-131 (Report 3 §3.2 "View List Supplier", lọc Pending) | **Status**: Draft
**BR liên quan**: — (Report 3 không gán BR trực tiếp cho UC-131)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên công ty) | Input (search) | — | — | |
| 2 | Filter theo Status | Select | Mặc định lọc "Pending"; hỗ trợ Approved/Rejected/Flagged | — | |
| 3 | Bảng danh sách Supplier | Table | Cột: Tên công ty, Trạng thái duyệt, Hiệu lực chứng nhận, Chỉ số hiệu suất gần nhất | — | |
| 4 | Badge "Performance Warning" | Badge (warning) | Hiện khi supplier bị flag do hiệu suất | AF2 UC-131 | |
| 5 | Badge trạng thái | Badge (warning=Pending, success=Approved, danger=Rejected/Flagged) | — | — | |
| 6 | Action: xem chi tiết | Row click | — | — | Mở `Supplier Detail` |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: AF1 UC-131 — "Không tìm thấy nhà cung cấp khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, badge Performance Warning hiện khi có supplier bị flag (AF2).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Nhà cung cấp". Điều hướng ra: `Supplier Detail`.
**Bố cục**: Dashboard Shell — filter bar (search + status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Supplier Partnership Requests, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (search + status), bảng dữ liệu (Tên công ty/Trạng thái/Hiệu lực chứng nhận/Chỉ số hiệu suất).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, badge Performance Warning, action xem chi tiết.
Áp dụng Design System: badge warning/success/danger.
Trạng thái cần thể hiện: empty, success (kèm badge Performance Warning).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2595–2615 (Table 3-60 View List Supplier Use Cases Specification)
- traceability_matrix.md §1.4 (UC-131)

# Screen Spec — Service Caterers Detail

**Actor**: School Administration | **UC liên quan**: UC-108 (View Service Caterers Detail), UC-004 (Approval / Reject Service Caterers) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-046 (yêu cầu tài liệu tuân thủ), BR-048 (từ chối kèm lý do)
**MSG liên quan**: MSG-004, MSG-005, MSG-037 (phê duyệt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin công ty (tên, liên hệ) | Text hiển thị | — | — | |
| 2 | Trạng thái hợp đồng | Badge (warning/success/danger) | — | — | |
| 3 | Giấy phép kinh doanh + Chứng nhận ATTP | File preview + ngày hết hạn | Xem `Verification Documents` (bundled) | BR-046 | |
| 4 | Điểm site inspection | Text/số hiển thị | — | — | |
| 5 | Nút "Approve" | Button (primary) | Chỉ hiện khi trạng thái Pending | — | Cập nhật trạng thái Approved, tạo contract record, gửi email (Normal Flow bước 7) |
| 6 | Nút "Reject" + lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc nhập lý do trước khi xác nhận | BR-048 | |

### Trạng thái màn hình
- Loading: Skeleton card khi tải hồ sơ.
- Empty: Không áp dụng.
- Error: MSG-004/MSG-005.
- Success: MSG-037 khi Approve; toast xác nhận khi Reject (gửi email kèm lý do).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Catering Partnership Requests` (click 1 dòng). Không có màn hình con (Verification Documents hiển thị bundled ngay trong trang).
**Bố cục**: Dashboard Shell — Page Header (tên công ty + nút Approve/Reject góc phải), Content chia 2 khối: thông tin công ty + tài liệu (`col-span-8`), điểm inspection + trạng thái (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Service Caterers Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject góc phải), 2 cột: thông tin công ty + tài liệu, điểm inspection + badge trạng thái.
Thành phần bắt buộc: thông tin công ty, badge trạng thái hợp đồng, preview giấy phép/chứng nhận ATTP kèm ngày hết hạn, điểm site inspection, nút Approve, nút Reject + textarea lý do bắt buộc.
Áp dụng Design System: nút Reject dùng token danger.
Trạng thái cần thể hiện: success (approve/reject).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2269–2285 (Table 3-45 Approval/Reject Service Caterers), 2533–2549 (Table 3-57 View Service Caterers Detail)
- traceability_matrix.md §1.4 (UC-004, UC-108)

# Screen Spec — Supplier Detail

**Actor**: School Administration | **UC liên quan**: UC-148 (View Supplier Detail), UC-007 (Approval / Reject Supplier) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-046 (tài liệu tuân thủ), BR-047 (cảnh báo hết hạn 30 ngày), BR-048 (từ chối kèm lý do)
**MSG liên quan**: MSG-004, MSG-005, MSG-013 (chứng nhận hết hạn — chặn duyệt), MSG-037 (phê duyệt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin công ty (tên, liên hệ, tax ID) | Text hiển thị | — | — | |
| 2 | Trạng thái duyệt | Badge (warning/success/danger) | — | — | |
| 3 | Giấy phép kinh doanh + Chứng nhận ATTP | File preview + ngày hết hạn | Badge "Expiring Soon" nếu <30 ngày | BR-047 | AF2 UC-148 |
| 4 | Lịch sử giao hàng (tóm tắt) | Table rút gọn | — | — | |
| 5 | Chỉ số hiệu suất hàng tháng | Text/số + badge cảnh báo nếu <70% 2 tháng liên tiếp | — | MSG-060 | |
| 6 | Nút "Approve" | Button (primary) | **Chặn nếu chứng nhận ATTP đã hết hạn** (MSG-013) | BR-048 | AF1 UC-007 |
| 7 | Nút "Reject" + lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc lý do | BR-048 | |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: MSG-013 nếu cố Approve khi chứng nhận hết hạn (AF1); AF2 thiếu tài liệu bắt buộc → highlight field thiếu, chặn Approve; MSG-004/MSG-005.
- Success: MSG-037 khi Approve; toast xác nhận khi Reject.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Supplier Partnership Requests` (click 1 dòng). Không có màn hình con.
**Bố cục**: Dashboard Shell — Page Header (tên công ty + nút Approve/Reject), Content 2 cột: thông tin + tài liệu (`col-span-8`), chỉ số hiệu suất + lịch sử giao hàng rút gọn (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Supplier Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), 2 cột: thông tin công ty + tài liệu, chỉ số hiệu suất + lịch sử giao hàng.
Thành phần bắt buộc: thông tin công ty, badge trạng thái, preview giấy phép/chứng nhận (badge Expiring Soon nếu <30 ngày), chỉ số hiệu suất (cảnh báo nếu <70%), nút Approve (chặn nếu hết hạn chứng nhận), nút Reject + lý do bắt buộc.
Áp dụng Design System: badge warning cho Expiring Soon, badge danger cho hiệu suất thấp.
Trạng thái cần thể hiện: error (chứng nhận hết hạn chặn Approve), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2287–2307 (Table 3-46 Approval/Reject Supplier), 2617–2637 (Table 3-61 View Supplier Detail)
- traceability_matrix.md §1.4 (UC-007, UC-148)

# Screen Spec — Menu Management

**Actor**: School Administration | **UC liên quan**: UC-128 (Report 3 §3.2 "View List Menu") | **Status**: Draft
**BR liên quan**: — (Report 3 gán "Business Rules: None" cho UC-128)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (theo tuần) | Input (search) | — | — | |
| 2 | Filter theo Status (Draft/Pending Approval/Approved/Published/Rejected) | Select | — | — | |
| 3 | Bảng danh sách Menu | Table | Cột: Tuần áp dụng, Trạng thái, Tên Service Caterers, Tóm tắt dinh dưỡng | — | |
| 4 | Badge trạng thái | Badge (info=Draft, warning=Pending, success=Approved/Published, danger=Rejected) | — | — | |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Menu Detail` |
| 6 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có thực đơn nào khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Thực đơn". Điều hướng ra: `Menu Detail`.
**Bố cục**: Dashboard Shell — filter bar (search + status) trên bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (search theo tuần + status), bảng dữ liệu (Tuần/Trạng thái/Service Caterers/Tóm tắt dinh dưỡng).
Thành phần bắt buộc: search, filter status, bảng, badge trạng thái, action xem chi tiết, nút Export.
Áp dụng Design System: badge info/warning/success/danger theo trạng thái menu.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2959–2975 (Table 3-69 View List Menu Use Cases Specification)
- traceability_matrix.md §1.6 (UC-128)

# Screen Spec — Menu Detail

**Actor**: School Administration | **UC liên quan**: UC-134 (View Menu Detail), UC-006 (Approval / Reject Menu) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-062 (mục tiêu dinh dưỡng), BR-063 (xung đột dị ứng — chặn duyệt nếu nghiêm trọng), BR-066 (electronic signature khi duyệt), BR-067 (khóa menu Approved/Published), BR-069 (khai báo nguyên liệu đầy đủ), BR-070 (khóa/publish sau approve — theo UC-006 gốc), BR-074 (ghi log lý do từ chối), BR-075 (thông báo sau approve)
**MSG liên quan**: MSG-004, MSG-005, MSG-040 (phê duyệt & công bố thành công), MSG-051 (cảnh báo xung đột dị ứng)

> Ghi chú đối chiếu BR: UC-006 trong Report 3 (dòng 2817) liệt kê "BR-063, BR-066, BR-067, BR-070, BR-074, BR-075" — trong đó BR-070/BR-074/BR-075 theo `business_rules.md` hiện hành lại mang nội dung khác (BR-070 = xác minh trạng thái Approved nhà cung cấp khi nhận hàng; BR-074 = xác minh vị trí nhận hàng; BR-075 = nguyên tắc FEFO) — không khớp ngữ nghĩa "khóa menu/ghi log rejection/thông báo" như mô tả trong Normal Flow. Khả năng cao đây là lệch số BR giữa 2 lần cập nhật `business_rules.md` (UC-006 có thể đã được viết khi BR còn đánh số khác). **[Giả định — cần team xác nhận]**: giữ nguyên trích dẫn theo đúng Report 3 gốc nhưng ghi chú rõ nghi vấn lệch số này để team đối chiếu lại, đồng thời bổ sung BR-067/BR-068 (khóa & quy trình phê duyệt) làm căn cứ chính xác hơn theo `business_rules.md` hiện hành.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ngày phục vụ + Meal slot | Text hiển thị | — | — | |
| 2 | Danh sách món ăn + nguyên liệu đầy đủ + trace allergen | Table/Card list | — | BR-069 | |
| 3 | Tóm tắt dinh dưỡng | Card số liệu | Theo nhóm tuổi | BR-062 | |
| 4 | Cảnh báo xung đột dị ứng (nếu có) | Alert (danger) | Chặn Approve nếu xung đột nghiêm trọng | BR-063 | AF2 UC-006, MSG-051 |
| 5 | Lịch sử duyệt/từ chối (timestamp, chữ ký điện tử, ghi chú) | Table lịch sử | — | BR-066 | |
| 6 | Nút "Approve" | Button (primary) | Disable nếu đang có cảnh báo dị ứng nghiêm trọng chưa xử lý | BR-063, BR-067 | |
| 7 | Nút "Reject" + ghi chú lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc ghi chú | — | AF1 UC-006 |

### Trạng thái màn hình
- Loading: Skeleton card khi tải chi tiết menu.
- Empty: Không áp dụng.
- Error: MSG-051 nếu phát hiện xung đột dị ứng — chặn Approve (AF2); MSG-004/MSG-005.
- Success: MSG-040 khi Approve (khóa + publish + thông báo phụ huynh/giáo viên); toast xác nhận khi Reject (trả về trạng thái Rejected, thông báo Service Caterers).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Menu Management` (UC-128) hoặc `Menu Approval Dashboard` — click 1 dòng. Không có màn hình con.
**Bố cục**: Dashboard Shell — Page Header (ngày phục vụ + nút Approve/Reject), Content 2 cột: danh sách món + dinh dưỡng (`col-span-8`), lịch sử duyệt (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), 2 cột: danh sách món + nguyên liệu + dinh dưỡng, lịch sử duyệt.
Thành phần bắt buộc: ngày phục vụ, danh sách món (nguyên liệu + allergen), card dinh dưỡng, alert cảnh báo xung đột dị ứng, bảng lịch sử duyệt, nút Approve, nút Reject + textarea lý do.
Áp dụng Design System: alert dị ứng dùng token danger.
Trạng thái cần thể hiện: error (xung đột dị ứng chặn duyệt), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2805–2821 (Table 3-62 Approval/Reject Menu), 2977–2997 (Table 3-70 View Menu Detail)
- traceability_matrix.md §1.6 (UC-006, UC-134)
- business_rules.md BR-062, BR-063, BR-067, BR-068, BR-069

# Screen Spec — Incident Dashboard

**Actor**: School Administration | **UC liên quan**: entry point tổng hợp (không phải 1 UC riêng — điểm vào của UC-114/116/121/005/013/031) | **Status**: Draft
**BR liên quan**: BR-146 (tự động kích hoạt điều tra), BR-148 (cảnh báo ngay cho sự cố nghiêm trọng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card tổng số sự cố đang mở theo severity | Card + badge (danger=High, warning=Medium, info=Low) | — | BR-147 | |
| 2 | Card số CAPA đang chờ duyệt | Card + số liệu | — | BR-152 | Nguồn: UC-005 |
| 3 | Danh sách sự cố gần nhất (rút gọn) | Table rút gọn | — | — | Link sang `Incident List` |
| 4 | Nút "Xem toàn bộ danh sách sự cố" | Button/Link | — | — | Mở `Incident List` |
| 5 | Nút "Export Incident Reports" | Button | — | BR-154 | Mở `Export Incident Reports` (UC-031) |

### Trạng thái màn hình
- Loading: Skeleton cho từng card.
- Empty: "Không có sự cố nào đang mở" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Card + bảng rút gọn hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố". Điều hướng ra: `Incident List`, `Export Incident Reports`.
**Bố cục**: Dashboard Shell — card số liệu hàng trên (`col-span-4` mỗi card), bảng rút gọn sự cố gần nhất bên dưới, nút Export góc phải Page Header.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Dashboard, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — card số liệu (sự cố theo severity, CAPA chờ duyệt) hàng trên, bảng rút gọn sự cố gần nhất bên dưới.
Thành phần bắt buộc: card severity, card CAPA chờ duyệt, bảng rút gọn, nút Xem toàn bộ, nút Export.
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md (node "ID" Incident Dashboard, entry point tổng hợp)
- business_rules.md BR-146, BR-147, BR-148, BR-152

# Screen Spec — Incident List

**Actor**: School Administration | **UC liên quan**: UC-114 (Report 3 §3.2 "View Food Poisoning Incident List") | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023 (báo cáo sự cố thiếu thông tin)

> **Đã đối chiếu hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)** với `Web/MedicalStaff/25_IncidentList.md`: 2 bản cùng phản ánh đủ dữ liệu cốt lõi (ngày/severity/status/số học sinh ảnh hưởng/export) — không phát hiện field dữ liệu bị thiếu ở bản nào, chỉ khác cách trình bày bộ lọc (School Administration dùng Select Status; Medical Staff dùng 2 Tab Processing/Resolved) — giữ nguyên vì đây là lựa chọn UI/layout, không phải khác biệt dữ liệu.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Severity Level | Select | — | BR-147 | |
| 2 | Filter theo Status | Select | Active/Under Investigation/Closed | — | |
| 3 | Thanh Search (từ khoá) | Input (search) | — | — | |
| 4 | Bảng danh sách sự cố | Table | Cột: Ngày báo cáo, Mô tả tóm tắt, Severity, Status, Số học sinh ảnh hưởng | — | |
| 5 | Badge Severity | Badge (danger=High, warning=Medium, info=Low) | — | BR-147 | |
| 6 | Action: xem chi tiết | Row click | — | — | Mở `Incident Detail` |
| 7 | Nút "Export" | Button | — | — | AF1 UC-114 |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có sự cố nào khớp bộ lọc".
- Error: MSG-004/MSG-005/MSG-023.
- Success: Bảng hiển thị đầy đủ, Export tải file CSV/PDF (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Dashboard`. Điều hướng ra: `Incident Detail`.
**Bố cục**: Dashboard Shell — filter bar (Severity, Status, search) trên bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident List, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Severity/Status/s
# Screen Spec — Incident Detail

**Actor**: School Administration | **UC liên quan**: UC-116 (Report 3 §3.2 "View Food Safety Incident Detail") | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023

> **Cập nhật sau review (mục 6, `00_Team_Work_Assignment_Plan.md`)**: `14_Screen_Authorization_Matrix.md` đánh dấu "Incident Detail" có cột `x` cho cả School Admin/Teacher/Medical Staff, nhưng đối chiếu thực tế phạm vi giao việc (mục 2 của Assignment Plan, Teacher cố định 13 màn/62 file) thì **Teacher không có file "Incident Detail" riêng** — Teacher tiếp cận thông tin sự cố qua `Mobile(Web)/Teacher/13_IncidentHistory.md` (chỉ đọc, giới hạn báo cáo do chính giáo viên đó tạo, không phải view quản trị đầy đủ này). Ghi nhận đây là điểm cần team xác nhận: hoặc bổ sung 1 file Incident Detail thu gọn cho Teacher sau này, hoặc coi Incident History là đủ đáp ứng cột `x` của Teacher trong ma trận. Màn hình này **thực tế chỉ dùng chung 2 actor**: School Administration (file này) + Medical Staff (`Web/MedicalStaff/26_IncidentDetail.md`) — đã hợp nhất phần field dữ liệu với bản Medical Staff, bổ sung "Suspected food item + meal time" và "First aid administered" (trước đây thiếu ở bản này).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Triệu chứng báo cáo | Text/List hiển thị | — | — | |
| 2 | Danh sách học sinh bị ảnh hưởng | Table | — | BR-146 (ngưỡng ≥3 học sinh/24h tự động kích hoạt điều tra) | |
| 3 | Suspected food item + meal time (món ăn nghi vấn + thời điểm ăn) | Text hiển thị | — | BR-146, `suspectedDishIds` (class diagram) | Hợp nhất từ bản Medical Staff |
| 4 | Tường trình ban đầu (trước đây "First aid administered", đổi 11/07/2026 — xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12) | Text hiển thị (read-only phía School Administration) | — | — | Hợp nhất từ bản Medical Staff — mô tả diễn biến ban đầu, không chỉ sơ cứu y tế; School Administration thấy ngay khi Medical Staff nhập ở trạng thái TREATMENT |
| 5 | Trạng thái điều tra | Badge (info=Investigating, danger=Active, success=Closed) | — | — | |
| 6 | Bản đồ truy xuất nguồn gốc (nếu có) | Panel/link | Biên soạn trong 4 giờ | BR-151 | |
| 7 | Danh sách lô hàng bị Quarantine | Table rút gọn | — | BR-149 | |
| 8 | Nút "Xem CAPA / Corrective Action Review" | Button/Link (hành động riêng School Administration) | — | BR-152, BR-153 | Mở `Corrective Action Review` |
| 9 | Nút "Export" | Button | — | — | AF1 UC-116 |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: MSG-004/MSG-005/MSG-023.
- Success: Chi tiết sự cố hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident List` (click 1 dòng). Điều hướng ra: `Corrective Action Review`.
**B
# Screen Spec — Corrective Action Review

**Actor**: School Administration | **UC liên quan**: UC-005 (Report 3 §3.2 "Approval / Reject Corrective Action") | **Status**: Draft
**BR liên quan**: BR-152 (quản lý CAPA), BR-153 (phê duyệt hành động khắc phục)
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (cập nhật thành công — Approve), MSG-049 (nộp CAPA thành công — tham chiếu vòng đời)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Mô tả sự cố liên quan | Text hiển thị | — | — | |
| 2 | Yêu cầu CAPA đã giao | Text/List hiển thị | — | BR-152 | |
| 3 | Báo cáo thực hiện của Service Caterers + bằng chứng (ảnh, checklist) | Text + gallery ảnh | — | — | |
| 4 | Ghi chú xác minh | Textarea | Bắt buộc khi Approve | — | |
| 5 | Nút "Approve" | Button (primary) | — | BR-153 | MSG-043 khi Approve theo Normal Flow |
| 6 | Nút "Reject" + lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc lý do (EX1 chặn nếu thiếu) | BR-153 | Deadline resubmit mới (VD 6 giờ, AF1) |

### Trạng thái màn hình
- Loading: Button Approve/Reject loading khi submit.
- Empty: Không áp dụng.
- Error: EX1 — thiếu ghi chú lý do khi Reject → validation error, chặn submit; MSG-004/MSG-005.
- Success: Approve → cập nhật CAPA "Approved", thông báo Service Caterers + Medical Staff; Reject → CAPA "Rejected", thông báo kèm deadline resubmit mới.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Detail` (nút "Xem CAPA") hoặc dashboard CAPA riêng. Sau khi xử lý quay về `Incident Detail`/`Resolution Report` nếu đủ điều kiện đóng sự cố.
**Bố cục**: Dashboard Shell — Page Header (nút Approve/Reject góc phải), Content: mô tả sự cố + yêu cầu CAPA (`col-span-6`), báo cáo thực hiện + gallery ảnh (`col-span-6`), ghi chú xác minh cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Corrective Action Review, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), 2 cột: mô tả sự cố + yêu cầu CAPA, báo cáo thực hiện + gallery ảnh bằng chứng, textarea ghi chú xác minh cuối trang.
Thành phần bắt buộc: mô tả sự cố, yêu cầu CAPA, báo cáo + ảnh bằng chứng, textarea ghi chú, nút Approve, nút Reject + lý do bắt buộc.
Áp dụng Design System: nút Reject dùng token danger.
Trạng thái cần thể hiện: error (thiếu lý do reject), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4307–4329 (Table 3-128 Approval/Reject Corrective Action Use Cases Specification)
- traceability_matrix.md §1.11 (UC-005 ↔ BR-152/BR-153)

# Screen Spec — Resolution Report

**Actor**: School Administration | **UC liên quan**: UC-013 (Close Incident), UC-121 (View Incident Report) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-155 (đóng sự cố), BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154
**MSG liên quan**: MSG-004, MSG-005, MSG-023, MSG-029 (cập nhật/đóng thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt điều tra (root cause) | Text hiển thị | — | BR-150 | |
| 2 | Trạng thái xác minh CAPA hoàn thành | Badge (success/warning) | Chặn đóng nếu còn CAPA chưa hoàn thành (AF1) | BR-152 | |
| 3 | Trạng thái hồi phục của học sinh bị ảnh hưởng | Table/checklist | Toàn bộ phải "Fully Recovered" trước khi đóng | BR-155 | |
| 4 | Closing statement | Textarea | Bắt buộc khi Close Incident | — | |
| 5 | Nút "Close Incident" | Button (primary) | Disable tới khi đủ điều kiện (CAPA hoàn thành + học sinh hồi phục) | BR-155 | |
| 6 | Báo cáo sự cố đầy đủ (clinical notes, lab results, root cause) | Panel/Accordion | — | BR-154 | Nguồn UC-121, chỉ xem |

### Trạng thái màn hình
- Loading: Button "Close Incident" loading khi xử lý.
- Empty: Không áp dụng.
- Error: AF1 UC-013 — còn CAPA chưa hoàn thành → chặn đóng, highlight các CAPA còn mở; MSG-004/MSG-005/MSG-023.
- Success: MSG-029, trạng thái sự cố chuyển "Closed", sinh báo cáo cuối cùng (BR-154), ghi chữ ký điện tử, thông báo các bên liên quan.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Detail`/`Corrective Action Review` khi đủ điều kiện đóng. Sau khi Close quay về `Incident List`.
**Bố cục**: Dashboard Shell — Page Header (nút Close Incident góc phải, disable tới khi đủ điều kiện), Content: checklist điều kiện đóng (`col-span-6`), panel báo cáo đầy đủ dạng accordion (`col-span-6`), textarea closing statement cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Resolution Report, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Close Incident, disable tới khi đủ điều kiện), checklist điều kiện đóng + panel báo cáo đầy đủ, textarea closing statement.
Thành phần bắt buộc: tóm tắt root cause, trạng thái CAPA, trạng thái hồi phục học sinh, textarea closing statement, nút Close Incident, panel báo cáo đầy đủ (clinical notes/lab results).
Áp dụng Design System: badge success/warning cho điều kiện đóng.
Trạng thái cần thể hiện: error (còn CAPA chưa hoàn thành, chặn đóng), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4357–4373 (Table 3-130 Close Incident), 4617–4635 (Table 3-142 View Incident Report)
- traceability_matrix.md §1.11 (UC-013 ↔ BR-155, UC-121 ↔ BR-154)

# Screen Spec — Food Sample Retention Reports

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard + UC-172 - Export Report (Report 3.md §3.2.13.1–3.2.13.2, lọc theo Food Sample Retention) | **Status**: Draft

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171/172 nay đã có Functional Specification đầy đủ trong Report 3.md (viết bổ sung qua audit A8). Màn hình này là 1 view lọc theo danh mục "Food Sample Retention" trong Analytics Dashboard (UC-171), có nút Export dùng UC-172. Cấu trúc field dưới đây (suy luận từ BR-105–114) vẫn giữ nguyên và nay được coi là chính thức.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | BR-177 | **[Giả định]** |
| 2 | Bảng tổng hợp lưu mẫu (số mẫu lưu, số mẫu hủy đúng hạn, số mẫu thu hồi điều tra) | Table | — | BR-105, BR-112, BR-113 | **[Giả định]** |
| 3 | Cảnh báo nhiệt độ tủ lưu mẫu vi phạm (nếu có trong kỳ báo cáo) | Alert/Table rút gọn | — | BR-110 | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê" hoặc view lọc trong `Analytics Dashboard`.
**Bố cục**: Dashboard Shell — filter bar + bảng tổng hợp + nút Export.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Retention Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range, bảng tổng hợp lưu mẫu, cảnh báo nhiệt độ (nếu có), nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1 (UC-171 View Analytics Dashboard), §3.2.13.2 (UC-172 Export Report) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-105–BR-114, BR-175, BR-176, BR-178
- 14_Screen_Authorization_Matrix.md khối 3

# Screen Spec — Receiving Reports

**Actor**: School Administration | **UC liên quan**: UC-123 - View Ingredient Receiving Records (Report 3.md §3.2.6.18, dòng 3397–3417 — Primary Actor School Administration, UC có sẵn đầy đủ) | **Status**: Draft
**BR liên quan**: BR-070–BR-078 (Ingredient & Inventory), BR-178
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: màn hình này KHÔNG cần UC-171/172 (Reporting & Analytics) — nó khớp trực tiếp với UC-123 (View Ingredient Receiving Records), một UC đã có Functional Specification đầy đủ từ trước, Primary Actor = School Administration. Đây là kết luận "gộp UC có sẵn" mạnh nhất trong nhóm A8 vì không cần suy luận hay viết UC mới.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | BR-177 | **[Giả định]** |
| 2 | Filter Supplier | Select | — | — | **[Giả định]** |
| 3 | Bảng tổng hợp nhận hàng (số lô nhận, số lô từ chối, tỷ lệ đúng hạn/GPS hợp lệ) | Table | — | BR-070, BR-074, BR-078 | Nguồn tham chiếu: UC-123 View Ingredient Receiving Records |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + bảng tổng hợp + nút Export.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + Supplier, bảng tổng hợp nhận hàng (số lô nhận/từ chối, tỷ lệ đúng hạn/GPS hợp lệ), nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.6.18, dòng 3397–3417 (UC-123 View Ingredient Receiving Records)
- traceability_matrix.md §1.5
- business_rules.md BR-070–BR-078, BR-178
- 14_Screen_Authorization_Matrix.md khối 3

# Screen Spec — Inspection Reports

**Actor**: School Administration | **UC liên quan**: UC-113 (Report 3 §3.2 "View Food Inspection Reports") | **Status**: Draft
**BR liên quan**: BR-091 (nhiệt độ giữ ấm), BR-097 (tích hợp quản lý sự cố), BR-098 (phê duyệt bữa ăn để phục vụ)
**MSG liên quan**: MSG-004, MSG-005, MSG-016, MSG-017

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | |
| 2 | Filter Meal session | Select | — | — | |
| 3 | Filter Service Caterers | Select | — | — | |
| 4 | Nút "Generate Report" | Button | — | — | |
| 5 | Bảng tổng hợp (tổng lô kiểm tra, tỷ lệ pass/fail, số lần lệch nhiệt độ nghiêm trọng, trạng thái CAPA) | Table | — | BR-091, BR-097, BR-098 | |
| 6 | Nút "Export Report" (PDF/Excel) | Button | — | — | AF1 UC-113 |

### Trạng thái màn hình
- Loading: Button "Generate Report" loading.
- Empty: "Không có dữ liệu kiểm tra trong khoảng thời gian đã chọn".
- Error: MSG-016 (checklist chưa đầy đủ), MSG-017 (nhiệt độ thấp) — hiển thị dưới dạng cảnh báo trong báo cáo tổng hợp; MSG-004/MSG-005.
- Success: Bảng tổng hợp hiển thị, Export tải file (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố" hoặc "Báo cáo & Thống kê". Không có màn hình con.
**Bố cục**: Dashboard Shell — filter bar + nút Generate Report, bảng tổng hợp giữa trang, nút Export cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspection Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Date/Meal session/Service Caterers), nút Generate Report, bảng tổng hợp, nút Export.
Thành phần bắt buộc: 3 filter, nút Generate Report, bảng tổng hợp (tổng lô/tỷ lệ pass-fail/lệch nhiệt độ/CAPA), nút Export.
Áp dụng Design System: không cần badge đặc biệt ngoài màu neutral cho bảng.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3579–3595 (Table 3-99 View Food Inspection Reports Use Cases Specification)
- traceability_matrix.md §1.7 (UC-113 ↔ BR-099)

# Screen Spec — Partnership Reports

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard + UC-172 - Export Report (Report 3.md §3.2.13.1–3.2.13.2, lọc theo Partnership) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền xem báo cáo), BR-176 (phạm vi theo trường), BR-178 (định dạng PDF/Excel)
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171/172 nay đã có Functional Specification đầy đủ trong Report 3.md. Màn hình này là 1 view lọc theo danh mục "Partnership" trong Analytics Dashboard (UC-171), có nút Export dùng UC-172. Cấu trúc field dưới đây vẫn giữ nguyên và nay được coi là chính thức.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | BR-177 (lưu trữ tối thiểu 12 tháng) | **[Giả định]** |
| 2 | Filter loại đối tác (Supplier/Service Caterers) | Select | — | — | **[Giả định]** |
| 3 | Bảng tổng hợp (số đăng ký mới, tỷ lệ approve/reject, thời gian xử lý trung bình) | Table | — | BR-176 | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]** — "Không có dữ liệu trong khoảng thời gian đã chọn".
- Error: MSG-004/MSG-005.
- Success: **[Giả định]** — bảng + export hoạt động.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê", hoặc là 1 view lọc trong `Analytics Dashboard` chung (UC-171) — team cần xác nhận có tách trang riêng hay không.
**Bố cục**: Dashboard Shell — filter bar + bảng tổng hợp + nút Export.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Partnership Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + loại đối tác, bảng tổng hợp (số đăng ký mới, tỷ lệ approve/reject, thời gian xử lý trung bình), nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1–3.2.13.2 (UC-171/172) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-175, BR-176, BR-177, BR-178
- 14_Screen_Authorization_Matrix.md khối 3

# Screen Spec — Partnership Approval History

**Actor**: School Administration | **UC liên quan**: UC-106 - View Audit Logs (Report 3.md dòng 4883–4903, lọc theo Resource = Partnership/Supplier/Caterer) | **Status**: Draft
**BR liên quan**: BR-170 (ghi log mọi hành động quan trọng — bao gồm approve/reject)
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: màn hình này KHÔNG cần UC mới. `14_Screen_Authorization_Matrix.md` khối "Audit Logs" xác nhận School Administration đã có quyền xem Audit Logs (`x` ở cả cột System Admin và School Admin). UC-106 (View Audit Logs) đã có Functional Specification đầy đủ, hiển thị đúng các cột cần thiết (Timestamp, User ID, Action Performed, Resource Accessed, Status) — Partnership Approval History là 1 view lọc theo Resource Accessed = "Partnership/Supplier/Caterer" trong màn hình Audit Logs dùng chung, không phải màn hình/UC riêng biệt.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | **[Giả định]** |
| 2 | Filter loại đối tác (Supplier/Service Caterers) | Select | — | — | **[Giả định]** |
| 3 | Filter kết quả (Approved/Rejected) | Select | — | — | **[Giả định]** |
| 4 | Bảng lịch sử duyệt | Table (đọc-only) | Cột: Tên đối tác, Loại, Kết quả, Người duyệt, Ngày, Lý do (nếu Reject) | BR-170 | **[Giả định — cấu trúc cột dựa trên Normal Flow bước 7-9 của UC-004/UC-007]** |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có lịch sử duyệt nào khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Quản lý Nhà cung cấp" (cạnh Catering/Supplier Partnership Requests).
**Bố cục**: Dashboard Shell — filter bar + bảng lịch sử đọc-only.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Partnership Approval History, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + loại đối tác + kết quả (Approved/Rejected), bảng lịch sử duyệt đọc-only (Tên đối tác, Loại, Kết quả, Người duyệt, Ngày, Lý do nếu Reject).
Đây là 1 view lọc trong màn hình Audit Logs dùng chung (UC-106), không phải màn hình độc lập — có thể tái dùng layout Audit Logs nếu team quyết định không tách trang riêng.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4883–4903 (UC-106 View Audit Logs) — xác nhận 2026-07-01
- 14_Screen_Authorization_Matrix.md khối "Audit Logs" (School Admin = x)
- Report 3.md, dòng 2269–2307 (Normal Flow UC-004/UC-007, bước ghi audit trail — nguồn gốc dữ liệu approve/reject)
- business_rules.md BR-170

# Screen Spec — Compliance Dashboard

**Actor**: School Administration | **UC liên quan**: UC-027 (Report 3 §3.2 "Daily Food Safety Monitoring") | **Status**: Draft
**BR liên quan**: BR-091 (nhiệt độ giữ ấm — theo dõi thời gian thực), BR-097 (tích hợp sự cố), BR-100 (điểm vệ sinh hàng ngày), BR-104 (điểm vệ sinh đầu bếp), BR-149 (tự động Quarantine **lô nguyên liệu** khi có sự cố ATTP — xem ghi chú phạm vi)
**MSG liên quan**: MSG-004, MSG-005, MSG-093 (cảnh báo điểm checklist thấp — Warning)

> **Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: phần field hiển thị dữ liệu đối chiếu với bản `Web/MedicalStaff/23_ComplianceDashboard.md`; giữ nguyên hành động riêng của School Administration (nút Ký xác nhận).
>
> **Cập nhật 05/07/2026 — bỏ banner Kitchen Quarantine gắn với điểm checklist**: field #5 cũ ("Banner Kitchen Quarantine... BR-105, BR-149... MSG-024") trộn lẫn 2 cơ chế khác nhau: (a) ngưỡng điểm checklist UC-014 <60% (trích `BR-105`, ghost-citation — canonical là quy tắc lưu mẫu thực phẩm) và (b) cách ly **lô nguyên liệu cụ thể** khi có sự cố ATTP thực sự (BR-149, đúng phạm vi Incident Management). Đội dự án quyết định: (a) **không còn khóa vận hành bếp** khi điểm checklist thấp — chỉ tự động cảnh báo School Administration (xem `business_rules.md` §12, ghi chú sau BR-208); (b) BR-149 **giữ nguyên, không đổi** — nếu có sự cố ATTP kích hoạt cách ly lô nguyên liệu, đó là thông tin thuộc UC-041 (Incident Management), không hiển thị ở Compliance Dashboard này nữa để tránh nhầm 2 cơ chế. `MSG-024` không còn dùng cho màn này (theo `error_message.md`, mã này thuộc module Incident Management). Mã thay thế đã cấp: **`MSG-093`** (Warning), xem `error_message.md` §3.
>
> **Cập nhật 05/07/2026 — bổ sung US-09 (BR-209, UC-185)**: Medical Staff hiện có thể chủ động soạn và gửi báo cáo tuân thủ ATTP (Daily/Weekly) cho School Administration, thay vì chỉ dựa vào School Administration tự vào xem màn này (pull). Đây là kênh bổ sung — School Administration vẫn có thể tự xem Compliance Dashboard khi màn hình này được triển khai thực tế (hiện Screen Spec đã có nhưng **chưa có route/trang thực thi phía frontend School Administration** — xem `03_Design/08_FrontEnd/Web/review/review_screen_medical_staff.md` mục 3.9). Báo cáo chủ động từ Medical Staff không thay thế màn hình này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trạng thái 3-bước kiểm tra hôm nay theo thời gian thực | Card/Progress theo lô sản xuất | Step 1/2/3, cập nhật realtime | BR-091 | |
| 2 | Điểm vệ sinh hàng ngày của bếp | Card số liệu | — | BR-100, BR-104 | |
| 3 | Cảnh báo bước kiểm tra thất bại/quá hạn | Alert (danger) | Hiện link tới yêu cầu CAPA đang mở | BR-097 | AF1 UC-027 |
| 4 | Ảnh xác minh (verification photos) | Gallery rút gọn | — | — | |
| 4b | Cảnh báo điểm checklist <60% *(thay thế field Quarantine cũ, 05/07/2026)* | Alert (warning, không phải banner cố định) | Hiện khi điểm gần nhất <60% | — | Không khóa vận hành bếp, không chặn phân phát bữa ăn |
| 5 | Nút "Ký xác nhận trạng thái an toàn hôm nay" | Button (primary, hành động riêng School Administration) | Ghi log sự kiện review | — | Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: Skeleton cho từng card.
- Empty: Không áp dụng (luôn có dữ liệu hôm nay, kể cả "chưa bắt đầu kiểm tra").
- Error: điểm checklist <60% → Alert cảnh báo (không phải banner cố định, MSG-093); AF1 — cảnh báo bước thất bại/quá hạn kèm link chi tiết non-conformance.
- Success: Ký xác nhận thành công, ghi log review.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố". Điều hướng ra: chi tiết non-conformance (nếu có cảnh báo AF1).
**Bố cục**: Dashboard Shell — Content: trạng thái 3-bước realtime (`col-span-8`) cạnh điểm vệ sinh hàng ngày (`col-span-4`), gallery ảnh xác minh + nút ký xác nhận cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Compliance Dashboard, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — trạng thái 3-bước kiểm tra realtime + card điểm vệ sinh hàng ngày, gallery ảnh xác minh, nút ký xác nhận cuối trang.
Thành phần bắt buộc: progress 3-bước theo lô, card điểm vệ sinh, alert cảnh báo bước thất bại/quá hạn,
alert cảnh báo điểm checklist <60% (không phải banner khóa), gallery ảnh, nút ký xác nhận.
Áp dụng Design System: alert dùng token danger/warning, progress dùng success/warning/danger theo từng bước.
Trạng thái cần thể hiện: error (bước thất bại/điểm thấp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3469–3485 (Table 3-94 Daily Food Safety Monitoring Use Cases Specification)
- traceability_matrix.md §1.7 (UC-027 ↔ BR-100/BR-104)
- `business_rules.md` §12, ghi chú sau BR-208 (quyết định 05/07/2026 descope khóa bếp, không còn dùng BR-105/MSG-024 cho màn này).

---

# Screen Spec — Classroom Management

**Actor**: School Administration | **UC liên quan**: UC-037 (Manage Classrooms), UC-166 (View List Classrooms) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-024 (cấp lớp 1-12), BR-025 (sức chứa tối đa 50)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên lớp) | Input (search) | — | — | |
| 2 | Filter theo Grade Level | Select | 1-12 | BR-024 | |
| 3 | Bảng danh sách lớp | Table | Cột: Class Name, Grade Level, Student Capacity, Homeroom Teacher | BR-024, BR-025 | |
| 4 | Nút "Create Classroom" | Button (primary, góc trên phải) | — | — | Mở `Create Classroom` |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Classroom Detail` |
| 6 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có lớp học nào — tạo lớp đầu tiên" (kèm nút Create Classroom nổi bật hơn).
- Error: MSG-004/MSG-005; nếu trường chưa có năm học Active → MSG-081 khi cố tạo lớp (BR-192, xem `21_CreateClassroom.md`).
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Lớp/Học sinh". Điều hướng ra: `Create Classroom`, `Classroom Detail`.
**Bố cục**: Dashboard Shell — Page Header (nút Create Classroom góc phải), filter bar (search + grade), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Classroom Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Classroom), filter bar (search + grade level), bảng dữ liệu (Class Name/Grade/Capacity/Homeroom Teacher).
Thành phần bắt buộc: search, filter grade, bảng, action xem chi tiết, nút Export.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1667–1731 (Table 3-23 Manage Classrooms, View List Classrooms Use Cases Specification)
- traceability_matrix.md §1.2 (UC-037, UC-166 ↔ BR-024/BR-025)

# Screen Spec — Classroom Detail

**Actor**: School Administration | **UC liên quan**: UC-165 (View Classroom Detail) + action UC-008 (Assign Homeroom Teacher), UC-090 (Update Classroom) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-020 (1 GVCN/lớp), BR-024 (cấp lớp), BR-025 (sức chứa), BR-026 (ghi log phân công GV)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa), MSG-011 (GV đã gán lớp khác), MSG-034 (phân công GV thành công)

> `Assign Homeroom Teacher` (UC-008) và `Update Classroom` (UC-090) **không phải màn hình riêng** — đây là 2 action (dialog/inline edit) thực hiện ngay tại màn hình này, theo đúng ghi chú nghiệp vụ ở `02_School_Administration_Screen_Flow.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Class Name, Grade Level, Max Capacity | Text hiển thị | — | BR-024, BR-025 | |
| 2 | Homeroom Teacher hiện tại | Text + nút "Assign/Reassign" | Chỉ 1 GVCN/lớp | BR-020 | Mở dialog chọn GV từ danh sách staff active role Teacher |
| 3 | Dialog Assign Homeroom Teacher | Select (danh sách teacher active) | Chặn nếu GV đã được gán lớp khác (MSG-011), yêu cầu xác nhận reassign (AF1) | BR-020, BR-026 | |
| 4 | Danh sách học sinh trong lớp | Table | — | BR-025 | |
| 5 | Nút "Edit" (Update Classroom) | Button | Mở form inline sửa Name/Grade/Capacity | BR-024, BR-025 | Capacity mới không được nhỏ hơn số học sinh hiện có |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Danh sách học sinh trống — "Chưa có học sinh trong lớp".
- Error: MSG-011 nếu GV đã gán lớp khác (không có AF1 confirm thì chặn); MSG-010 nếu sửa capacity nhỏ hơn số học sinh hiện có; MSG-004/MSG-005.
- Success: MSG-034 khi assign GV thành công; toast xác nhận khi Update Classroom thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Classroom Management` (click 1 dòng). Không có màn hình con — action Assign Teacher/Update Classroom mở Dialog/inline edit ngay tại trang.
**Bố cục**: Dashboard Shell — Page Header (tên lớp + nút Edit), Content: thông tin lớp + Homeroom Teacher (`col-span-4`), danh sách học sinh (`col-span-8`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Classroom Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Edit), thông tin lớp + Homeroom Teacher (cột trái), danh sách học sinh (cột phải), Dialog Assign Teacher khi bấm nút Assign/Reassign.
Thành phần bắt buộc: Class Name/Grade/Capacity, Homeroom Teacher + nút Assign/Reassign, dialog chọn GV, bảng học sinh, nút Edit (Update Classroom).
Áp dụng Design System: cảnh báo vượt sức chứa dùng token danger.
Trạng thái cần thể hiện: error (GV đã gán lớp khác, vượt sức chứa), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1623–1643 (UC-008), 1689–1709 (UC-165), 1755–1775 (UC-090)
- traceability_matrix.md §1.2 (UC-008/UC-090/UC-165)

# Screen Spec — Create Classroom

**Actor**: School Administration | **UC liên quan**: UC-018 (Report 3 §3.2 "Create Classroom") | **Status**: Draft
**BR liên quan**: BR-024 (cấp lớp 1-12), BR-025 (sức chứa tối đa 50), BR-192 (lớp phải thuộc năm học Active)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa), MSG-033 (tạo lớp thành công), MSG-081 (chưa có năm học Active)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên lớp | Input (text) | Bắt buộc, duy nhất trong trường | — | |
| 2 | Cấp lớp (Grade Level) | Select | 1-12 | BR-024 | |
| 3 | Sức chứa tối đa | Input (number) | ≤ 50 (MSG-010 nếu vượt) | BR-025 | |
| 4 | Năm học áp dụng | Select (read-only nếu chỉ có 1 năm Active) | **Bắt buộc phải có năm học Active** — nếu chưa có, chặn tạo lớp (MSG-081) | BR-192 | Ghi chú tích hợp: BR-192 chính thức hoá 2026-07-01, mở rộng Normal Flow gốc của UC-018 |
| 5 | Nút "Tạo lớp" | Button (primary) | Disable tới khi field hợp lệ | — | |

### Trạng thái màn hình
- Loading: Button "Tạo lớp" loading.
- Empty: Không áp dụng (form tạo mới).
- Error: MSG-010 nếu vượt sức chứa/cấp lớp sai; MSG-081 nếu trường chưa có năm học Active (chặn hoàn toàn, gợi ý điều hướng sang `Academic Year List` để kích hoạt năm học trước); MSG-004/MSG-005.
- Success: MSG-033, quay về `Classroom Management`.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create Classroom" trên `Classroom Management`. Sau khi tạo quay về `Classroom Management`.
**Bố cục**: Form ngắn (<8 field) hiện trong `Dialog` giữa màn hình theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Classroom, nền tảng Web, actor School Administration.
Bố cục: Dialog giữa màn hình, form 1 cột.
Thành phần bắt buộc: Tên lớp, Cấp lớp (select 1-12), Sức chứa tối đa (≤50), Năm học áp dụng (select/read-only), nút Tạo lớp.
Áp dụng Design System: lỗi field dùng token danger.
Trạng thái cần thể hiện: error (vượt sức chứa, chưa có năm học Active), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1645–1665 (Table 3-22 Create Classroom Use Cases Specification)
- traceability_matrix.md §1.2 (UC-018 ↔ BR-024/BR-025/BR-192)
- business_rules.md BR-192 (bổ sung 2026-07-01, mở rộng Normal Flow gốc)

# Screen Spec — Student Profile Management

**Actor**: School Administration | **UC liên quan**: UC-147 (View Student List) + action UC-025 (Create Student) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa lớp, hiển thị gián tiếp)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên học sinh) | Input (search) | — | — | |
| 2 | Filter theo Class/Grade | Select | — | — | |
| 3 | Bảng danh sách học sinh | Table | Cột: Tên, School Registration Number, Lớp, Liên hệ phụ huynh | — | |
| 4 | Nút "Create Student Profile" | Button (primary, góc trên phải) | — | — | Mở form Create Student (UC-025) |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Student Profile Detail` |
| 6 | Nút "Bulk Import" | Button | — | — | Mở `Bulk Import Student Profiles` |
| 7 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có học sinh nào — tạo hồ sơ đầu tiên hoặc nhập hàng loạt".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Lớp/Học sinh". Điều hướng ra: form Create Student (Dialog/Sheet), `Student Profile Detail`, `Bulk Import Student Profiles`.
**Bố cục**: Dashboard Shell — Page Header (nút Create + Bulk Import góc phải), filter bar, bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Profile Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Student Profile + Bulk Import), filter bar (search + class/grade), bảng dữ liệu.
Thành phần bắt buộc: search, filter, bảng (Tên/School Reg No/Lớp/Liên hệ PH), action xem chi tiết, nút Export.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2245–2261 (Table 3-44 View Student List Use Cases Specification)
- traceability_matrix.md §1.3 (UC-147)

# Screen Spec — Student Profile Detail

**Actor**: School Administration | **UC liên quan**: UC-099 (Report 3 §3.2 "Update Student") | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa lớp khi đổi lớp)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa)

> Ghi chú BR: Report 3 gán "Business Rules: BR-041" cho UC-099 (dòng 2169) — BR-041 theo `business_rules.md` hiện hành là "Trạng thái nhà cung cấp" (Supplier Pending/Approved/Rejected), không liên quan ngữ nghĩa tới cập nhật hồ sơ học sinh. Đây là lệch số rõ ràng — dùng BR-025 (sức chứa lớp, khớp đúng Normal Flow bước 4 "system validates capacity") làm căn cứ chính xác hơn, theo nguyên tắc ưu tiên nội dung thật của Normal Flow khi BR code bị lệch.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Họ tên | Input (text) | Bắt buộc | — | |
| 2 | Ngày sinh | DatePicker | Bắt buộc | — | |
| 3 | Lớp (Classroom) | Select | Nếu đổi lớp, lớp mới không được vượt sức chứa 50 (MSG-010) | BR-025 | |
| 4 | School Registration Number / Student Admission Code | Text hiển thị (read-only) | Không cho sửa (mã tự sinh) | — | |
| 5 | Thông tin phụ huynh liên kết | Text hiển thị + link | — | — | Link sang `Parent Account Detail` |
| 6 | Nút "Lưu" | Button (primary) | — | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading.
- Empty: Không áp dụng (form pre-filled).
- Error: MSG-010 nếu lớp mới vượt sức chứa; MSG-004/MSG-005.
- Success: Toast xác nhận cập nhật thành công (Report 3 không gán MSG code riêng cho Update Student thành công — dùng MSG-029 chung theo pattern `error_message.md`).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Student Profile Management` (click 1 dòng). Điều hướng ra: `Parent Account Detail`.
**Bố cục**: Dashboard Shell — Page Header (tên học sinh + nút Lưu), form 1 cột nhóm theo section (Thông tin học sinh / Lớp / Phụ huynh liên kết).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Profile Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Lưu), form dọc (Thông tin học sinh, Lớp, Phụ huynh liên kết).
Thành phần bắt buộc: Họ tên, Ngày sinh, Lớp (select), mã số read-only, thông tin phụ huynh + link, nút Lưu.
Áp dụng Design System: field read-only dùng màu neutral nhạt.
Trạng thái cần thể hiện: error (vượt sức chứa lớp mới), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2157–2173 (Table 3-40 Update Student Use Cases Specification)
- traceability_matrix.md §1.3 (UC-099)
- business_rules.md BR-025

# Screen Spec — Bulk Import Student Profiles

**Actor**: School Administration | **UC liên quan**: UC-010 (Report 3 §3.2 "Bulk Import Student & Parent") | **Status**: Draft
**BR liên quan**: BR-032 (nhập hàng loạt học sinh/phụ huynh), BR-012 (định dạng email/SĐT), BR-010 (xác minh email)
**MSG liên quan**: MSG-004, MSG-005, MSG-012 (mã học sinh không hợp lệ), MSG-035 (nhập hàng loạt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Tải template" | Button (link) | — | — | |
| 2 | Input upload file | Input type="file" + preview tên file | Định dạng Excel/CSV theo template | — | |
| 3 | Nút "Tải lên & Kiểm tra" | Button (primary) | — | BR-012, BR-032 | Chạy validate: định dạng email/SĐT, trùng mã học sinh, liên kết phụ huynh |
| 4 | Bảng tóm tắt validation (hợp lệ/không hợp lệ/trùng lặp) | Table + số liệu tổng | — | — | AF1 UC-010 |
| 5 | Danh sách dòng lỗi chi tiết (kèm mã lỗi) | Table (đọc-only) | — | MSG-012 | |
| 6 | Nút "Xác nhận nhập" | Button (primary) | Chỉ enable khi có ít nhất 1 dòng hợp lệ | — | |

### Trạng thái màn hình
- Loading: Progress bar khi đang validate/import file lớn.
- Empty: Chưa upload file — hiển thị hướng dẫn tải template trước.
- Error: MSG-012 (mã học sinh không hợp lệ/đã dùng) hiển thị theo từng dòng lỗi (AF1); MSG-004/MSG-005.
- Success: MSG-035, quay về `Student Profile Management` với danh sách mới được nhập; gửi email xác minh cho tài khoản phụ huynh mới (BR-010).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Bulk Import" trên `Student Profile Management`. Sau khi nhập xong quay về `Student Profile Management`.
**Bố cục**: Form theo Stepper ngang (Tải template → Upload → Xem kết quả validate → Xác nhận) theo `Layout_Guidelines.md` mục 1.2 (form dài chia bước).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Bulk Import Student Profiles, nền tảng Web, actor School Administration.
Bố cục: Trang riêng với Stepper ngang (Tải template → Upload → Validate → Xác nhận).
Thành phần bắt buộc: nút Tải template, input upload file, nút Tải lên & Kiểm tra, bảng tóm tắt validation, bảng chi tiết dòng lỗi, nút Xác nhận nhập.
Áp dụng Design System: dòng lỗi dùng token danger, dòng hợp lệ dùng token success.
Trạng thái cần thể hiện: error (dòng lỗi MSG-012), success (nhập hoàn tất).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1933–1953 (Table 3-30 Bulk Import Student & Parent Use Cases Specification)
- traceability_matrix.md §1.3 (UC-010 ↔ BR-032)

# Screen Spec — Parent Account Detail

**Actor**: School Administration | **UC liên quan**: bundled trong UC-025 (Report 3 §3.2 "Create Student") | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh-phụ huynh), BR-012 (định dạng thông tin)
**MSG liên quan**: MSG-004, MSG-005, MSG-029

> **Nguồn**: Không phải UC độc lập — khi tạo học sinh (UC-025), hệ thống tìm email phụ huynh: có sẵn → hiển thị chi tiết + liên kết; chưa có → tạo mới. File này đặc tả riêng khối "Parent Account Detail" theo tên trong Screen Authorization Matrix, dùng khi School Admin xem lại thông tin phụ huynh đã liên kết (từ `Student Profile Detail`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Họ tên phụ huynh | Text hiển thị | — | — | |
| 2 | Email | Text hiển thị | Duy nhất trong hệ thống | BR-012 | |
| 3 | Số điện thoại | Text hiển thị | Định dạng hợp lệ | BR-012 | |
| 4 | Trạng thái xác minh email | Badge (warning=Pending, success=Verified) | — | BR-010 | |
| 5 | Danh sách học sinh liên kết | Table rút gọn (tối đa 5, BR-030/040) | — | BR-030 | Link sang `Linked Students List` để xem đầy đủ |
| 6 | Nút "Link thêm học sinh" | Button | Chặn nếu đã đạt giới hạn tối đa | BR-030 | Mở `Link Parent Account` |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng (phụ huynh luôn có ít nhất 1 học sinh liên kết khi vào màn hình này từ Student Profile Detail).
- Error: AF1 UC-025 — nếu phụ huynh đã đạt giới hạn liên kết tối đa → chặn thêm, hiển thị cảnh báo xác minh thủ công; MSG-004/MSG-005.
- Success: MSG-029 khi cập nhật thông tin.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Student Profile Detail` (link "Phụ huynh liên kết"). Điều hướng ra: `Linked Students List`, `Link Parent Account`.
**Bố cục**: Dashboard Shell — Page Header (tên phụ huynh + badge xác minh), Content: thông tin liên hệ (`col-span-6`), danh sách học sinh liên kết rút gọn (`col-span-6`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Parent Account Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (badge xác minh email), 2 cột: thông tin liên hệ, danh sách học sinh liên kết rút gọn.
Thành phần bắt buộc: họ tên, email, SĐT, badge xác minh, bảng học sinh liên kết rút gọn, nút Link thêm học sinh.
Áp dụng Design System: badge warning/success cho trạng thái xác minh.
Trạng thái cần thể hiện: error (đạt giới hạn liên kết), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1955–1975 (Table 3-31 Create Student Use Cases Specification, bước 5 + AF1)
- traceability_matrix.md §1.3 (UC-025 ↔ BR-030)

# Screen Spec — Link Parent Account

**Actor**: School Administration | **UC liên quan**: bundled trong UC-025 (Report 3 §3.2 "Create Student") | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh-phụ huynh), BR-012 (định dạng email/SĐT), BR-010 (xác minh email khi tạo mới)
**MSG liên quan**: MSG-004, MSG-005, MSG-012, MSG-031 (tạo tài khoản phụ huynh mới thành công)

> **Nguồn**: Bước 5 của Normal Flow UC-025 — "System searches for the parent email. If exists: link. If not: input parent's name/phone to create new parent account." Đây là 1 bước trong luồng tạo học sinh, không phải UC độc lập.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Email phụ huynh | Input (email) | Bắt buộc, hệ thống tự động tìm kiếm khi nhập | BR-012 | |
| 2 | Kết quả tìm kiếm: "Đã tìm thấy tài khoản" | Card hiển thị thông tin phụ huynh có sẵn | Hiển thị tên/SĐT hiện có, chỉ cần xác nhận link | BR-030 | Nhánh "email tồn tại" |
| 3 | Form tạo phụ huynh mới (nếu không tìm thấy) | Input Họ tên + Input SĐT | Bắt buộc, định dạng SĐT hợp lệ | BR-012 | Nhánh "email chưa tồn tại" |
| 4 | Cảnh báo giới hạn liên kết | Alert (warning) | Hiện khi phụ huynh đã có 5 học sinh liên kết (AF1) | BR-030 | Chặn link, yêu cầu xác minh thủ công |
| 5 | Nút "Xác nhận liên kết" | Button (primary) | Disable nếu đạt giới hạn (AF1) | BR-030 | |

### Trạng thái màn hình
- Loading: Loading khi tìm kiếm email / submit.
- Empty: Chưa nhập email — chưa hiển thị kết quả tìm kiếm.
- Error: AF1 — đạt giới hạn liên kết tối đa → chặn, hiển thị cảnh báo; MSG-012 nếu định dạng sai; MSG-004/MSG-005.
- Success: Nhánh email tồn tại → liên kết ngay; nhánh tạo mới → MSG-031 + gửi email xác minh (BR-010).

---

## Platform Layout — Web
**Entry/Exit points**: Là 1 bước trong luồng Create Student (không phải trang điều hướng độc lập) — cũng có thể mở từ `Parent Account Detail` (nút "Link thêm học sinh", theo chiều ngược: chọn học sinh để link vào phụ huynh đang xem).
**Bố cục**: Bước trong `Sheet`/Dialog của luồng Create Student — 1 cột, chuyển động (tìm kiếm → hiện kết quả/form tạo mới → xác nhận).

## AI Generation Prompt ⭐

```
Tạo wireframe cho bước Link Parent Account, nền tảng Web, actor School Administration.
Bố cục: 1 bước trong Sheet/Dialog của luồng Create Student — input email tìm kiếm, kết quả (card có sẵn hoặc form tạo mới), nút xác nhận.
Thành phần bắt buộc: input email, card kết quả tìm thấy HOẶC form tạo phụ huynh mới (Họ tên + SĐT), alert cảnh báo giới hạn liên kết, nút Xác nhận liên kết.
Áp dụng Design System: alert cảnh báo dùng token warning.
Trạng thái cần thể hiện: error (đạt giới hạn), success (2 nhánh: đã có tài khoản / tạo mới).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1955–1975 (Table 3-31 Create Student Use Cases Specification, bước 5 + AF1)
- traceability_matrix.md §1.3 (UC-025 ↔ BR-030)

# Screen Spec — Linked Students List

**Actor**: School Administration | **UC liên quan**: bundled trong UC-025 (Report 3 §3.2 "Create Student") | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh-phụ huynh)
**MSG liên quan**: MSG-004, MSG-005

> **Nguồn**: Không phải UC độc lập — danh sách đầy đủ học sinh liên kết với 1 phụ huynh, mở rộng từ khối rút gọn ở `Parent Account Detail`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Bảng danh sách học sinh liên kết đầy đủ | Table | Cột: Tên, School Reg No, Lớp, Trạng thái | BR-030 | Tối đa 5 học sinh/phụ huynh |
| 2 | Action: xem chi tiết học sinh | Row click | — | — | Mở `Student Profile Detail` |
| 3 | Action: gỡ liên kết | IconButton | Xác nhận trước khi gỡ (AlertDialog) | BR-030 | **[Giả định — cần team xác nhận]**: Normal Flow gốc UC-025 không mô tả rõ hành vi "gỡ liên kết", suy luận từ nhu cầu quản lý ngược |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Phụ huynh chưa liên kết học sinh nào" (trường hợp hiếm — thường luôn có ít nhất 1).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ tối đa 5 học sinh.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Parent Account Detail` (link "Xem đầy đủ"). Điều hướng ra: `Student Profile Detail`.
**Bố cục**: Dashboard Shell — Page Header (tên phụ huynh), bảng `Table` đầy đủ.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Linked Students List, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header, bảng dữ liệu (Tên/School Reg No/Lớp/Trạng thái).
Thành phần bắt buộc: bảng, action xem chi tiết, action gỡ liên kết (kèm AlertDialog xác nhận).
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt lưu ý action "gỡ liên kết" là suy luận, cần xác nhận với team.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1955–1975 (Table 3-31 Create Student Use Cases Specification)
- traceability_matrix.md §1.3 (UC-025 ↔ BR-030)

# Screen Spec — School Announcement Management

**Actor**: School Administration | **UC liên quan**: UC-125 (Report 3 §3.2 "View List Announcement") | **Status**: Draft
**BR liên quan**: — (Report 3 gán "Business Rules: None")
**MSG liên quan**: — (Report 3 gán "Exceptions: None")

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (từ khoá) | Input (search) | — | — | |
| 2 | Filter theo Status (Draft/Published) | Select | — | — | |
| 3 | Bảng danh sách thông báo | Table | Cột: Title, Target Group, Publication Status, Publication Date | — | Sắp xếp theo ngày tạo |
| 4 | Badge trạng thái | Badge (info=Draft, success=Published) | — | — | |
| 5 | Nút "Create Announcement" | Button (primary, góc trên phải) | — | — | Mở `Create Announcement` |
| 6 | Action: xem chi tiết | Row click | — | — | Mở `Announcement Detail` |
| 7 | Nút "Export CSV/PDF" | Button | — | — | Chỉ School Administration có quyền export theo Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có thông báo nào — tạo thông báo đầu tiên".
- Error: Không có exception cụ thể trong Normal Flow gốc (Report 3 ghi "None") — áp dụng MSG-004/MSG-005 mặc định cho lỗi phiên/quyền.
- Success: Bảng hiển thị đầy đủ, export tải file (bước 7 Normal Flow).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Thông báo". Điều hướng ra: `Create Announcement`, `Announcement Detail`.
**Bố cục**: Dashboard Shell — Page Header (nút Create Announcement), filter bar (search + status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Announcement Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Announcement), filter bar (search + status), bảng dữ liệu.
Thành phần bắt buộc: search, filter status, bảng (Title/Target Group/Status/Publication Date), badge trạng thái, action xem chi tiết, nút Export.
Áp dụng Design System: badge info/success theo trạng thái Draft/Published.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4285–4305 (Table 3-126 View List Announcement Use Cases Specification)
- traceability_matrix.md §1.10 (UC-125)

# Screen Spec — Create Announcement

**Actor**: School Administration | **UC liên quan**: UC-017 (Report 3 §3.2 "Create Announcement") | **Status**: Draft
**BR liên quan**: BR-138 (cảnh báo quan trọng bỏ qua phê duyệt), BR-144 (lịch sử sửa đổi thông báo)
**MSG liên quan**: MSG-071 (lưu draft thành công), MSG-072 (publish thành công — lưu ý MSG-072 gốc trong `error_message.md` là "Special meal plan saved successfully", KHÁC với mô tả trong Normal Flow UC-017 "MSG-072" cho publish thông báo — xem ghi chú)

> **Ghi chú lệch MSG**: Normal Flow UC-017 (Report 3 dòng 4076) ghi "If Published is selected... displays MSG-072" — nhưng theo `error_message.md` hiện hành, MSG-072 = "Special meal plan saved successfully" (không liên quan announcement). Khả năng đây là lệch số giữa 2 lần cập nhật tài liệu MSG. **[Giả định — cần team xác nhận]**: dùng MSG-047 ("Announcement published successfully to target audience" — khớp đúng ngữ nghĩa "publish announcement" trong `error_message.md`) thay cho MSG-072 khi hiển thị thông báo publish thành công.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề | Input (text) | Bắt buộc | — | |
| 2 | Nội dung | Textarea/Rich text | Bắt buộc | — | |
| 3 | Nhóm đối tượng nhận (Target Group) | Select/Checkbox group | Bắt buộc (Parents/Teachers/Service Caterers...) | — | |
| 4 | Publication Status | RadioGroup (Draft/Published) | Mặc định Draft | BR-138 | Nếu Published → gửi thông báo real-time qua FCM/email ngay |
| 5 | Nút "Lưu" | Button (primary) | Disable tới khi field bắt buộc hợp lệ | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading.
- Empty: Không áp dụng (form tạo mới).
- Error: EX1 — thiếu field bắt buộc khi Publish → validation error, chặn submit.
- Success: MSG-071 nếu Draft; nếu Published — dùng MSG-047 theo ghi chú lệch số ở trên (không phải MSG-072 như Report 3 gốc ghi).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create Announcement" trên `School Announcement Management`. Sau khi lưu quay về danh sách.
**Bố cục**: Form ngắn hiện trong `Dialog`/`Sheet` theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Announcement, nền tảng Web, actor School Administration.
Bố cục: Dialog/Sheet, form 1 cột.
Thành phần bắt buộc: Tiêu đề, Nội dung (rich text), Target Group (checkbox), Publication Status (radio Draft/Published), nút Lưu.
Áp dụng Design System: cảnh báo publish ngay dùng token info.
Trạng thái cần thể hiện: error (thiếu field khi publish), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4069–4085 (Table 3-120 Create Announcement Use Cases Specification)
- traceability_matrix.md §1.10 (UC-017 ↔ BR-138/BR-144)
- error_message.md MSG-047, MSG-071 (đối chiếu lệch số với MSG-072 gốc trong Report 3)

# Screen Spec — Update Announcement

**Actor**: School Administration | **UC liên quan**: UC-089 (Report 3 §3.2 "Update Announcement") | **Status**: Draft
**BR liên quan**: BR-144 (lịch sử sửa đổi thông báo)
**MSG liên quan**: MSG-029 (cập nhật thành công — theo Normal Flow bước 6 ghi "MSG-074", nhưng MSG-074 trong `error_message.md` là "Cập nhật thực đơn thành công", không khớp ngữ nghĩa announcement — xem ghi chú), MSG-047 (công bố thông báo)

> **Ghi chú lệch MSG**: tương tự UC-017, Normal Flow UC-089 (Report 3 dòng 4252) ghi "system saves the updated announcement (MSG-074)" — nhưng MSG-074 = "Menu updated successfully" theo `error_message.md`, sai domain. **[Giả định — cần team xác nhận]**: dùng MSG-047 khi Publish, hoặc thông báo chung "Cập nhật thành công" khi chỉ lưu Draft.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề | Input (text) | Bắt buộc | — | |
| 2 | Nội dung | Textarea/Rich text | Bắt buộc | — | |
| 3 | Publication Status | RadioGroup (Draft/Published) | — | — | |
| 4 | Lịch sử sửa đổi (revision history) | Table rút gọn | Ghi timestamp + School Admin ID mỗi lần sửa | BR-144 | |
| 5 | Nút "Lưu" | Button (primary) | — | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading.
- Empty: Không áp dụng (form pre-filled).
- Error: EX1 — thông báo không tồn tại (đã bị xoá) → "Announcement Not Found", quay về danh sách; EX2 — thiếu field bắt buộc → validation error.
- Success: Lưu thành công + ghi revision history (BR-144); nếu chuyển sang Published → gửi thông báo cho users liên quan trường.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ `Announcement Detail` (nút Edit). Sau khi lưu quay về `Announcement Detail`.
**Bố cục**: Form hiện trong `Dialog`/`Sheet`, có thêm khối lịch sử sửa đổi rút gọn bên dưới form.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Announcement, nền tảng Web, actor School Administration.
Bố cục: Dialog/Sheet, form 1 cột + bảng lịch sử sửa đổi rút gọn cuối form.
Thành phần bắt buộc: Tiêu đề, Nội dung, Publication Status, bảng lịch sử sửa đổi, nút Lưu.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: error (không tìm thấy, thiếu field), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4245–4261 (Table 3-124 Update Announcement Use Cases Specification)
- traceability_matrix.md §1.10 (UC-089 ↔ BR-144)

# Screen Spec — Announcement Detail

**Actor**: School Administration | **UC liên quan**: UC-105 (Report 3 §3.2 "View Announcement Detail") | **Status**: Draft
**BR liên quan**: BR-144 (lịch sử sửa đổi — chỉ School Administration thấy đầy đủ)
**MSG liên quan**: — (Report 3 gán "Exceptions: None")

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Title, Content | Text hiển thị | — | — | |
| 2 | Target Group | Text/Badge hiển thị | — | — | |
| 3 | Publication Status | Badge (info=Draft, success=Published) | — | — | |
| 4 | Author, Publication Dates | Text hiển thị | — | — | |
| 5 | Lịch sử sửa đổi đầy đủ | Table (đọc-only) | **Chỉ hiện với actor School Administration** (khác User thường không thấy) | BR-144 | |
| 6 | Nút "Edit" | Button | — | — | Mở `Update Announcement` |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: Không có exception cụ thể — áp dụng MSG-004/MSG-005 mặc định.
- Success: Nội dung + lịch sử sửa đổi hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `School Announcement Management` (click 1 dòng). Điều hướng ra: `Update Announcement`.
**Bố cục**: Dashboard Shell — Page Header (tiêu đề + nút Edit), Content: nội dung thông báo (`col-span-8`), lịch sử sửa đổi (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Announcement Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Edit), 2 cột: nội dung thông báo, lịch sử sửa đổi.
Thành phần bắt buộc: Title/Content, Target Group, badge trạng thái, Author/Publication Dates, bảng lịch sử sửa đổi, nút Edit.
Áp dụng Design System: badge info/success theo trạng thái.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4263–4283 (Table 3-125 View Announcement Detail Use Cases Specification)
- traceability_matrix.md §1.10 (UC-105)

# Screen Spec — Staff Management

**Actor**: School Administration | **UC liên quan**: UC-144 (Report 3 §3.2 "View Staff Account List") | **Status**: Draft
**BR liên quan**: BR-008 (bảo vệ role mặc định — gián tiếp qua danh sách role staff)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thanh Search (tên/email) | Input (search) | — | — | |
| 2 | Filter theo Role | Select | Teacher/Medical Staff/Catering staff | BR-013 | |
| 3 | Filter theo Status | Select | Active/Inactive/Pending | — | |
| 4 | Bảng danh sách nhân viên | Table | Cột: Tên, Email, Role, SĐT, Trạng thái | — | |
| 5 | Badge trạng thái | Badge (success/warning/danger) | — | — | |
| 6 | Nút "Upload Staff List" | Button (primary, góc trên phải) | — | — | Mở `Upload Staff List` |
| 7 | Action: toggle trạng thái (Active/Inactive Staff) | IconButton | — | BR-007, BR-008 | UC-002 Active/Deactivate Staff |
| 8 | Nút "Export Excel/CSV" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có nhân viên nào — upload danh sách nhân viên đầu tiên".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Nhân sự". Điều hướng ra: `Upload Staff List`, `Invitation Status`.
**Bố cục**: Dashboard Shell — Page Header (nút Upload Staff List), filter bar (search + role + status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Staff Management, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Upload Staff List), filter bar (search + role + status), bảng dữ liệu.
Thành phần bắt buộc: search, filter role/status, bảng (Tên/Email/Role/SĐT/Trạng thái), badge trạng thái, action toggle, nút Export.
Áp dụng Design System: badge success/warning/danger.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1535–1555 (Table 3-18 View Staff Account List Use Cases Specification)
- traceability_matrix.md §1.1 (UC-144 ↔ BR-013)

# Screen Spec — Upload Staff List

**Actor**: School Administration | **UC liên quan**: UC-024 (Report 3 §3.2 "Create Staff", biến thể bulk) | **Status**: Draft
**BR liên quan**: BR-010 (xác minh email), BR-012 (định dạng email/SĐT), BR-013 (phân quyền tạo staff — chỉ role Teacher/Medical Staff/Catering)
**MSG liên quan**: MSG-004, MSG-005, MSG-006 (email đã tồn tại), MSG-031 (tạo thành công)

> **Nguồn**: Screen Authorization Matrix ghi "Upload Staff List | UC-024 (biến thể bulk)" — Normal Flow gốc của UC-024 (Report 3 dòng 1275–1291) mô tả tạo **1 staff đơn lẻ** qua form, không mô tả rõ luồng bulk upload file. Nội dung dưới đây áp dụng cùng validation của UC-024 (role phải là Teacher/Medical Staff/Catering, không phải admin/external) cho ngữ cảnh upload hàng loạt, tương tự cấu trúc `Bulk Import Student Profiles` — **[Giả định — cần team xác nhận]** cấu trúc file template bulk staff.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Nút "Tải template" | Button (link) | — | — | **[Giả định]** |
| 2 | Input upload file | Input type="file" | Định dạng Excel/CSV | — | |
| 3 | Nút "Tải lên & Kiểm tra" | Button (primary) | Validate: role hợp lệ (chỉ Teacher/Medical Staff/Catering — BR-013), email duy nhất (MSG-006), SĐT hợp lệ (BR-012) | BR-012, BR-013 | |
| 4 | Bảng tóm tắt validation | Table + số liệu tổng | — | — | |
| 5 | Nút "Xác nhận nhập" | Button (primary) | Chỉ enable khi có dòng hợp lệ | BR-010 | Gửi email xác minh cho từng tài khoản mới |

### Trạng thái màn hình
- Loading: Progress bar khi validate/import.
- Empty: Chưa upload file.
- Error: MSG-006 nếu email trùng; validation nếu role không hợp lệ (VD chọn "System Admin" — chặn theo BR-013); MSG-004/MSG-005.
- Success: MSG-031, quay về `Staff Management`, các tài khoản mới chuyển sang theo dõi tại `Invitation Status`.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Upload Staff List" trên `Staff Management`. Sau khi nhập xong quay về `Staff Management`/`Invitation Status`.
**Bố cục**: Stepper ngang (Tải template → Upload → Validate → Xác nhận), tương tự `Bulk Import Student Profiles`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Staff List, nền tảng Web, actor School Administration.
Bố cục: Stepper ngang (Tải template → Upload → Validate → Xác nhận).
Thành phần bắt buộc: nút Tải template, input upload file, nút Tải lên & Kiểm tra, bảng tóm tắt validation, nút Xác nhận nhập.
Áp dụng Design System: dòng lỗi dùng token danger.
Trạng thái cần thể hiện: error (email trùng, role không hợp lệ), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1271–1291 (Table 3-6 Create Staff Use Cases Specification)
- traceability_matrix.md §1.1 (UC-024 ↔ BR-010/BR-012/BR-013)

# Screen Spec — Invitation Status

**Actor**: School Administration | **UC liên quan**: bundled trong UC-024 (Create Staff)/UC-010 (Bulk Import) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-010 (xác minh email trước đăng nhập), BR-011 (thời hạn token kích hoạt 48h)
**MSG liên quan**: MSG-004, MSG-005

> **Nguồn**: Không phải UC riêng — theo dõi trạng thái xác minh email (BR-010) của tài khoản vừa tạo qua `Upload Staff List`/`Bulk Import Student Profiles`. Nội dung suy luận từ postcondition chung của UC-024/UC-010/UC-154 (Activate Account & Set Password).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Status (Pending Verification/Active/Expired) | Select | — | BR-010, BR-011 | |
| 2 | Bảng danh sách lời mời | Table | Cột: Tên, Email, Ngày gửi, Trạng thái, Hạn token (48h) | BR-011 | |
| 3 | Badge trạng thái | Badge (warning=Pending, success=Active, danger=Expired) | — | — | |
| 4 | Nút "Gửi lại email xác minh" | Button (secondary) | Chỉ enable khi Expired/Pending quá lâu | BR-011 | Gia hạn thêm 48h theo AF1 UC-154 |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có lời mời nào đang chờ xác minh".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ; gửi lại thành công → cập nhật trạng thái + hạn token mới.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Staff Management` (tab/link "Invitation Status") — cũng dùng chung logic cho `Bulk Import Student Profiles` (theo dõi tài khoản phụ huynh mới).
**Bố cục**: Dashboard Shell — filter bar (status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Invitation Status, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (status), bảng dữ liệu (Tên/Email/Ngày gửi/Trạng thái/Hạn token).
Thành phần bắt buộc: filter status, bảng, badge trạng thái, nút Gửi lại email xác minh.
Áp dụng Design System: badge warning/success/danger.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1271–1291 (UC-024), 1933–1953 (UC-010), 1579–1598 (UC-154 Activate Account & Set Password, AF1 resend)
- business_rules.md BR-010, BR-011

# Screen Spec — System Usage Metrics

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard (Report 3.md §3.2.13.1, lọc phạm vi 1 trường — School Administration được liệt kê là Primary Actor của UC-171) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền báo cáo), BR-176 (phạm vi theo trường), BR-180 (cập nhật trong 5 phút)
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171 nay đã có Functional Specification đầy đủ trong Report 3.md. Khác với `20_UsageStatisticsScreen.md` của System Admin (UC-150 View Usage Statistics riêng, phạm vi toàn hệ thống, đa trường), màn hình này dùng UC-171 với phạm vi **thu hẹp trong 1 trường** (BR-176) — 2 UC khác nhau phục vụ 2 actor khác nhau, không trùng lặp.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | **[Giả định]** |
| 2 | Chart Active users (staff/parent) theo ngày | Chart (recharts) | Chỉ trong phạm vi trường | BR-176 | **[Giả định]** |
| 3 | Bảng Top tính năng được dùng nhiều nhất | Table rút gọn | — | — | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]** — cập nhật trong 5 phút nếu áp dụng BR-180.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + chart + bảng top tính năng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Usage Metrics, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range, chart Active users theo ngày, bảng Top tính năng dùng nhiều nhất, nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1 (UC-171 View Analytics Dashboard) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-175, BR-176, BR-180
- 14_Screen_Authorization_Matrix.md khối 3

# Screen Spec — Traceability Reports

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard + UC-172 - Export Report (Report 3.md §3.2.13.1–3.2.13.2, lọc theo truy xuất nguồn gốc) | **Status**: Draft
**BR liên quan**: BR-076 (truy xuất lô nguyên liệu), BR-151 (bản đồ truy xuất trong 4 giờ khi có sự cố), BR-175, BR-176, BR-178
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171/172 nay đã có Functional Specification đầy đủ trong Report 3.md. Màn hình này là 1 view lọc theo danh mục "Traceability" trong Analytics Dashboard (UC-171), có nút Export dùng UC-172. Suy luận field từ BR-076 (truy xuất lô nguyên liệu qua menu/phiên sản xuất) và BR-151 (bản đồ truy xuất khi có sự cố, đã xuất hiện trong `10_IncidentDetail.md`) vẫn giữ nguyên và nay được coi là chính thức.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | **[Giả định]** |
| 2 | Filter theo Batch ID/Supplier | Input/Select | — | BR-076 | **[Giả định]** |
| 3 | Bảng truy xuất lô nguyên liệu → menu → phiên sản xuất | Table | — | BR-076 | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + bảng truy xuất.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Traceability Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + Batch ID/Supplier, bảng truy xuất lô nguyên liệu → menu → phiên sản xuất, nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1–3.2.13.2 (UC-171/172) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-076, BR-151, BR-175, BR-176, BR-178
- 14_Screen_Authorization_Matrix.md khối 3 (dòng "Traceability Reports | → UC-171/172")

# Screen Spec — School Configuration

**Actor**: School Administration | **UC liên quan**: UC-095 - Update Information School (Report 3.md dòng 1777–1797 — Description xác nhận rõ "Enables the System Admin (platform-level) OR School Administration (school-level) to modify school profile details and operational configuration fields") | **Status**: Draft
**BR liên quan**: BR-028 (geofence), BR-080 (trần chi phí thực đơn)
**MSG liên quan**: MSG-004, MSG-005, MSG-029

> **Đã xác nhận 2026-07-01 (audit A8)**: màn hình này KHÔNG cần UC mới — nó khớp với UC-095 (Update Information School), phần "School Administration (school-level)" theo Description. Đây là phần cấu hình vận hành (operational configuration) mà School Admin được sửa, khác với phần hồ sơ trường (Name/Address/Coordinates/Tax ID) do System Admin sở hữu — cùng 1 UC, 2 actor với phạm vi field khác nhau.
>
> **⚠️ Lưu ý phát hiện thêm (không thuộc phạm vi A8, cần team/Huy xác nhận riêng)**: UC-095 Normal Flow bước 4 trích dẫn "BR-030" cho "operational rules", nhưng BR-030 trong `business_rules.md` thực tế là "Liên kết học sinh với phụ huynh" (Student-Parent Mgmt) — không liên quan đến cấu hình trường. Đây có thể là 1 ghost-citation khác giống lỗi BR-038/BR-040 đã sửa ở mục A1/A2, hoặc UC-095 cần 1 BR mới về "operational rules" mà hiện chưa tồn tại. Trần chi phí thực đơn (BR-080) và bán kính geofence (BR-028) không có vấn đề trích dẫn.
>
> **✅ Đã xác nhận 2026-07-01 (loại bỏ Campus Config)**: chủ dự án xác nhận hệ thống không quản lý campus — màn `Campus Config` (System Admin) đã bị loại bỏ hoàn toàn. Đối chiếu lại nguyên văn BR-028: *"School Administration có thể cấu hình bán kính này trong khoảng 50m – 500m"* — xác nhận rõ **field bán kính geofence chính thức thuộc School Administration**, không phải System Admin. Field #2 dưới đây nay là chính thức, không còn là Giả định.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trần chi phí thực đơn (per suất ăn) | Input (number, VNĐ) | — | BR-080 | **[Giả định]** |
| 2 | Bán kính geofence | Input (number, mét) | 50-500m | BR-028 | Mặc định 200m — xác nhận 2026-07-01: BR-028 nêu rõ School Administration là chủ sở hữu cấu hình này (không phải System Admin) |
| 3 | Danh sách email nhận báo cáo định kỳ | Input (multi-email) | — | BR-179 | **[Giả định]** |
| 4 | Nút "Lưu" | Button (primary) | — | — | **[Giả định]** |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: Không áp dụng.
- Error: **[Giả định]** — chưa có MSG code chính thức.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Cấu hình" (nếu tồn tại như trang riêng).
**Bố cục**: **[Giả định]** — Dashboard Shell, form 1 cột.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Configuration, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell, form 1 cột — trần chi phí thực đơn, bán kính geofence (50-500m, mặc định 200m), danh sách email nhận báo cáo định kỳ, nút Lưu.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1777–1797 (UC-095 Update Information School) — xác nhận 2026-07-01
- business_rules.md BR-028 (xác nhận School Administration sở hữu cấu hình bán kính geofence), BR-080, BR-179
- 14_Screen_Authorization_Matrix.md khối 3
- `Web/SystemAdmin/13_CampusConfig.md` (đã loại bỏ — lý do và bối cảnh chuyển field)

# Screen Spec — Verification Documents

**Actor**: School Administration | **UC liên quan**: bundled trong UC-007 (Approval/Reject Supplier), UC-004 (Approval/Reject Service Caterers) | **Status**: Draft
**BR liên quan**: BR-046 (tài liệu tuân thủ), BR-047 (cảnh báo hết hạn 30 ngày)
**MSG liên quan**: MSG-004, MSG-005, MSG-013 (chứng nhận hết hạn)

> **Nguồn**: Không phải màn hình điều hướng riêng theo Normal Flow thật — license/chứng nhận ATTP/ngày hết hạn hiển thị ngay trong `Supplier Detail`/`Service Caterers Detail` (xem field #3 ở 2 file đó). File này đặc tả riêng khối "Verification Documents" theo đúng tên trong Screen Authorization Matrix, dùng khi cần 1 view phóng to/tách riêng (VD xem toàn màn hình 1 tài liệu PDF).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách tài liệu (giấy phép kinh doanh, chứng nhận ATTP, tài liệu xuất xứ) | List + file preview | — | BR-046 | |
| 2 | Ngày hết hạn từng tài liệu | Text + badge | Badge warning nếu <30 ngày (Expiring Soon) | BR-047 | |
| 3 | Nút "Xem tài liệu" (mở PDF/ảnh full-screen) | Button/Link | — | — | |
| 4 | Nút "Tải xuống" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton list.
- Empty: "Đối tác chưa upload tài liệu nào" (trường hợp hồ sơ chưa đầy đủ — AF2 UC-007).
- Error: MSG-004/MSG-005.
- Success: Danh sách tài liệu + trạng thái hết hạn hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Nhúng trong `Supplier Detail`/`Service Caterers Detail` (không phải trang điều hướng độc lập theo Normal Flow thật) — có thể mở rộng thành Dialog xem full tài liệu khi click "Xem tài liệu".
**Bố cục**: Card panel trong `Supplier Detail`/`Service Caterers Detail`, hoặc `Dialog` toàn màn hình khi xem chi tiết 1 tài liệu.

## AI Generation Prompt ⭐

```
Tạo wireframe cho panel Verification Documents, nền tảng Web, actor School Administration.
Bố cục: Card panel (nhúng trong Supplier Detail/Service Caterers Detail) — danh sách tài liệu kèm ngày hết hạn, nút Xem/Tải xuống.
Thành phần bắt buộc: danh sách tài liệu + preview, badge Expiring Soon, nút Xem tài liệu, nút Tải xuống.
Áp dụng Design System: badge warning cho Expiring Soon.
Trạng thái cần thể hiện: empty (chưa upload), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2287–2307 (UC-007 AF2), 2269–2285 (UC-004)
- business_rules.md BR-046, BR-047

# Screen Spec — Allergy Review

**Actor**: School Administration | **UC liên quan**: *(chưa có UC chính thức cho School Admin — TODO)* | **Status**: Draft — **[Giả định — cần team xác nhận]**
**BR liên quan**: BR-030 (liên kết dị ứng với hồ sơ học sinh), BR-031 (xét duyệt bởi Medical Staff), BR-175 (phân quyền xem báo cáo)
**MSG liên quan**: MSG-004, MSG-005

> **Nguồn**: Không có UC nền tảng chính thức cho School Administration. Theo `02_School_Administration_Screen_Flow.md`: "School Administration có quyền xem (không phải approver — đó là Medical Staff, UC-078/UC-036) theo BR-175 (phân quyền xem báo cáo)." Đây là màn hình **chỉ xem** (read-only), không có action Approve/Reject (khác với Medical Staff).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Class/Grade | Select | — | — | **[Giả định]** |
| 2 | Filter theo loại dị ứng | Select | — | BR-066 | **[Giả định]** |
| 3 | Bảng danh sách hồ sơ dị ứng đã xác nhận | Table (đọc-only) | Cột: Học sinh, Lớp, Loại dị ứng, Trạng thái xác nhận (Medical Staff) | BR-030, BR-031 | Không có action Approve/Reject — đây là quyền riêng của Medical Staff (UC-078) |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-175 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: "Chưa có hồ sơ dị ứng nào được xác nhận".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ (chỉ xem).

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Quản lý Lớp/Học sinh" hoặc "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + bảng đọc-only.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Allergy Review, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Class/Grade, loại dị ứng), bảng dữ liệu đọc-only.
Thành phần bắt buộc: filter, bảng (Học sinh/Lớp/Loại dị ứng/Trạng thái xác nhận), nút Export.
Áp dụng Design System: badge danger cho cảnh báo dị ứng theo `Design_System.md` mục 3 (màu đỏ bắt buộc cho cảnh báo dị ứng — lưu ý: `Design_System.md`/`Accessibility.md` trích dẫn nguồn gốc quy tắc này là "BR-123", nhưng mã này **không tồn tại** trong `business_rules.md` hiện hành — đã grep toàn văn, không có kết quả. Đây là tham chiếu sai/lỗi thời có sẵn trong tài liệu nền tảng, không phải do file này tạo ra — **[Giả định — cần team xác nhận lại nguồn BR thật cho quy tắc màu dị ứng, hoặc sửa lại `Design_System.md`]**).
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm action Approve/Reject vì đây không phải quyền của School Administration.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md (ghi chú TODO)
- business_rules.md BR-030, BR-031, BR-175 (lưu ý: BR-123 được `Design_System.md`/`Accessibility.md` trích dẫn cho quy tắc màu cảnh báo dị ứng nhưng không tồn tại trong `business_rules.md` — xem ghi chú ở AI Generation Prompt)
- 14_Screen_Authorization_Matrix.md khối 3 (dòng "Allergy Review | TODO — chưa có UC chính thức cho School Admin")

# Screen Spec — Export Incident Reports

**Actor**: School Administration | **UC liên quan**: UC-031 (Report 3 §3.2 "Export Incident Reports") | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155, BR-178 (định dạng PDF/Excel)
**MSG liên quan**: MSG-004, MSG-005, MSG-023

> **Ghi chú số thứ tự**: Màn hình này không có dòng riêng trong bảng gốc `14_Screen_Authorization_Matrix.md` khối 3 (chỉ có ghi chú cuối bảng: *"Export Incident Reports — UC-031 — không có dòng riêng trong Screen Authorization gốc nhưng có UC/Screen Spec chính thức"*). Theo `00_Team_Work_Assignment_Plan.md` §1 (đánh số theo đúng thứ tự xuất hiện trong ma trận), màn hình không có dòng gốc được đặt số thứ tự **40** — nối tiếp sau 39 màn hình có dòng chính thức, trước khối Academic Year/Violation Management. Cần team xác nhận cách đánh số này khi rà soát (mục 6).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date Range | DatePicker (range) | — | BR-177 | |
| 2 | Filter Severity Level | Select | — | BR-147 | |
| 3 | Filter Incident Status | Select | — | — | |
| 4 | Filter Suspected Service Caterers | Select | — | — | |
| 5 | Nút "Generate Preview" | Button | — | — | Hiển thị preview danh sách khớp filter trước khi export |
| 6 | Bảng Preview | Table | — | — | |
| 7 | Chọn định dạng export (PDF/Excel) | RadioGroup | — | BR-178 | |
| 8 | Nút "Export" | Button (primary) | — | BR-154 | Sinh file kèm Incident ID, symptoms, affected counts, root cause, CAPA status |

### Trạng thái màn hình
- Loading: Button "Generate Preview"/"Export" loading.
- Empty: "Không có sự cố nào khớp bộ lọc".
- Error: MSG-004/MSG-005/MSG-023.
- Success: MSG-045 (theo Normal Flow bước 6 — tải file, ghi log giao dịch export).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Dashboard` (nút "Export Incident Reports"). Không có màn hình con.
**Bố cục**: Dashboard Shell — filter bar trên cùng, nút "Generate Preview" cạnh filter, bảng preview giữa trang, radio chọn định dạng + nút Export cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Export Incident Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Date/Severity/Status/Caterer), nút Generate Preview, bảng preview, radio định dạng PDF/Excel, nút Export.
Thành phần bắt buộc: 4 filter, nút Generate Preview, bảng preview, radio định dạng, nút Export.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4375–4395 (Table 3-131 Export Incident Reports Use Cases Specification)
- traceability_matrix.md §1.11 (UC-031 ↔ BR-154)
- 14_Screen_Authorization_Matrix.md (ghi chú cuối khối 3 về UC-031)

# Screen Spec — Academic Year List

**Actor**: School Administration | **UC liên quan**: UC-176 (Report 3 §3.2 "View Academic Year List") | **Status**: Draft
**BR liên quan**: BR-195 (dữ liệu năm học Archived luôn truy vấn được)
**MSG liên quan**: MSG-004, MSG-005

> Màn hình mới, chính thức hoá 2026-07-01 (`AcademicYear_Management_Proposal.md`). Cũng dùng chung với System Admin (xem cột "System Admin" trong Primary Actor của UC-176) nhưng phạm vi School Admin giới hạn theo trường mình.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Status (Draft/Active/Closed/Archived) | Select | — | BR-191, BR-195 | |
| 2 | Bảng danh sách năm học | Table | Cột: Year Label, Start/End Date, Status, Số lớp | BR-195 | Archived vẫn hiển thị, read-only |
| 3 | Badge trạng thái | Badge (info=Draft, success=Active, secondary=Closed, neutral=Archived) | — | BR-191 | |
| 4 | Nút "Create Academic Year" | Button (primary, góc trên phải) | — | — | Mở `Create Academic Year` |
| 5 | Action: xem chi tiết | Row click | — | — | Mở `Academic Year Detail` |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có năm học nào — tạo năm học đầu tiên".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, bao gồm cả năm học Archived (đọc-only, BR-195).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Quản lý Lớp/Học sinh" (cạnh Classroom Management). Điều hướng ra: `Create Academic Year`, `Academic Year Detail`.
**Bố cục**: Dashboard Shell — Page Header (nút Create Academic Year), filter bar (status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Academic Year List, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Create Academic Year), filter bar (status), bảng dữ liệu (Year Label/Start-End Date/Status/Số lớp).
Thành phần bắt buộc: filter status, bảng, badge trạng thái (Draft/Active/Closed/Archived), action xem chi tiết.
Áp dụng Design System: badge info/success/secondary/neutral theo trạng thái năm học.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1887–1907 (Table 3-157 View Academic Year List Use Cases Specification)
- traceability_matrix.md §1.2 (UC-176 ↔ BR-195)
- business_rules.md §19 (BR-191–195)

# Screen Spec — Create Academic Year

**Actor**: School Administration | **UC liên quan**: UC-174 (Report 3 §3.2 "Create Academic Year") | **Status**: Draft
**BR liên quan**: BR-191 (1 năm học Active duy nhất/trường), BR-192 (Classroom phải thuộc năm học Active), BR-194 (sao chép cấu trúc lớp)
**MSG liên quan**: MSG-004, MSG-005, MSG-080 (đã có năm học Active, phải đóng trước), MSG-082 (tạo năm học thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Year Label (VD "2026-2027") | Input (text) | Bắt buộc, duy nhất trong trường | — | |
| 2 | Start Date, End Date | DatePicker x2 | Bắt buộc, Start < End | — | |
| 3 | Checkbox "Sao chép cấu trúc lớp từ năm học trước" | Checkbox | Tuỳ chọn | BR-194 | AF1 — chỉ sao chép tên/cấp/sức chứa, KHÔNG chuyển học sinh |
| 4 | Nút "Tạo năm học" | Button (primary) | — | BR-191, BR-192 | |

### Trạng thái màn hình
- Loading: Button "Tạo năm học" loading.
- Empty: Không áp dụng (form tạo mới).
- Error: MSG-080 nếu cố **kích hoạt ngay** năm học mới trong khi đã có năm học Active khác (năm học mới luôn tạo ở trạng thái Draft — chỉ lỗi nếu UI cho phép tick "Activate ngay"); MSG-004/MSG-005.
- Success: MSG-082, năm học được lưu ở trạng thái **Draft** (không tự Active); nếu tick sao chép cấu trúc lớp → tạo các Classroom mới liên kết năm học mới (BR-194), không chuyển học sinh.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create Academic Year" trên `Academic Year List`. Sau khi tạo quay về `Academic Year List` (hoặc `Academic Year Detail` của năm học vừa tạo).
**Bố cục**: Form ngắn hiện trong `Dialog` giữa màn hình theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Academic Year, nền tảng Web, actor School Administration.
Bố cục: Dialog giữa màn hình, form 1 cột.
Thành phần bắt buộc: Year Label, Start Date, End Date, checkbox Sao chép cấu trúc lớp, nút Tạo năm học.
Áp dụng Design System: lỗi field dùng token danger.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG có tuỳ chọn "Activate ngay" vì năm học mới luôn tạo ở trạng thái Draft theo Normal Flow.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1843–1863 (Table 3-155 Create Academic Year Use Cases Specification)
- traceability_matrix.md §1.2 (UC-174 ↔ BR-191/BR-192/BR-194)

# Screen Spec — Academic Year Detail

**Actor**: School Administration | **UC liên quan**: bundled (UC-175 Activate/Close, UC-177 Promote Students) | **Status**: Draft
**BR liên quan**: BR-191, BR-193 (đóng năm học → Classroom read-only), BR-195 (Archived vẫn truy vấn được)
**MSG liên quan**: MSG-004, MSG-005, MSG-080, MSG-083

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Year Label, Start/End Date | Text hiển thị | — | — | |
| 2 | Trạng thái hiện tại | Badge (info=Draft, success=Active, secondary=Closed, neutral=Archived) | — | BR-191 | |
| 3 | Danh sách lớp thuộc năm học này | Table | Read-only nếu năm học Closed/Archived | BR-193, BR-195 | |
| 4 | Nút "Activate" | Button (primary) | Chỉ hiện khi Draft; disable + MSG-080 nếu trường đã có năm học Active khác | BR-191 | Mở `Activate/Close Academic Year Dialog` |
| 5 | Nút "Close" | Button (destructive) | Chỉ hiện khi Active | BR-193 | Mở `Activate/Close Academic Year Dialog` |
| 6 | Nút "Promote Students" | Button (secondary) | Chỉ hiện khi năm học Active và có năm học kế tiếp (Draft) tồn tại | BR-194 | Mở `Promote Students Workspace` |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: "Chưa có lớp nào thuộc năm học này" (nếu chưa tạo/sao chép Classroom).
- Error: MSG-080 nếu cố Activate khi đã có năm học Active khác; MSG-004/MSG-005.
- Success: MSG-083 khi Activate/Close thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Academic Year List` (click 1 dòng). Điều hướng ra: `Activate/Close Academic Year Dialog`, `Promote Students Workspace`.
**Bố cục**: Dashboard Shell — Page Header (Year Label + badge trạng thái + nút Activate/Close/Promote góc phải), Content: danh sách lớp thuộc năm học (bảng `Table` toàn bộ chiều rộng).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Academic Year Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (badge trạng thái + nút Activate/Close/Promote Students), bảng danh sách lớp thuộc năm học.
Thành phần bắt buộc: Year Label/Start-End Date, badge trạng thái, bảng lớp học, nút Activate (điều kiện), nút Close (điều kiện), nút Promote Students (điều kiện).
Áp dụng Design System: badge info/success/secondary/neutral, nút Close dùng token danger (hành động rủi ro — khóa Classroom).
Trạng thái cần thể hiện: error (chặn Activate khi đã có năm Active khác), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1865–1929 (Table 3-156 Activate/Close Academic Year, Table 3-158 Promote Students)
- traceability_matrix.md §1.2 (UC-175 ↔ BR-191/BR-193, UC-177 ↔ BR-194)

# Screen Spec — Activate/Close Academic Year Dialog

**Actor**: School Administration | **UC liên quan**: UC-175 (Report 3 §3.2 "Activate/Close Academic Year") | **Status**: Draft
**BR liên quan**: BR-191 (1 năm học Active duy nhất/trường), BR-193 (đóng năm học → Classroom read-only)
**MSG liên quan**: MSG-004, MSG-005, MSG-080 (đã có năm học Active), MSG-083 (cập nhật trạng thái thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin năm học (Year Label, trạng thái hiện tại → mới) | Text + Badge | — | — | |
| 2 | Cảnh báo hệ quả (nhánh Activate) | Alert (info) | Hiển thị: sẽ chặn nếu còn năm học Active khác | BR-191 | |
| 3 | Cảnh báo hệ quả (nhánh Close) | Alert (warning) | Hiển thị: toàn bộ Classroom thuộc năm học sẽ chuyển read-only, dữ liệu lịch sử vẫn giữ nguyên | BR-193 | AF1 — cảnh báo nếu còn thao tác đang chờ xử lý (pending operations) |
| 4 | Nút "Xác nhận" | Button (primary cho Activate / destructive cho Close) | Chặn với MSG-080 nếu Activate khi đã có năm Active khác | BR-191 | |
| 5 | Nút "Huỷ" | Button (ghost) | — | — | |

### Trạng thái màn hình
- Loading: Button "Xác nhận" loading.
- Empty: Không áp dụng.
- Error: MSG-080 nếu Activate bị chặn; MSG-004/MSG-005.
- Success: MSG-083, cập nhật trạng thái năm học, ghi audit log.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Activate"/"Close" trên `Academic Year Detail`. Đóng lại trả về `Academic Year Detail`.
**Bố cục**: `AlertDialog` (hành động thay đổi trạng thái ảnh hưởng cascade tới Classroom — đặc biệt nhánh Close có rủi ro tương tự Active/Deactivate School), không điều hướng trang riêng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Activate/Close Academic Year Dialog, nền tảng Web, actor School Administration.
Bố cục: AlertDialog giữa màn hình, hiển thị thông tin năm học + cảnh báo hệ quả (khác nhau theo nhánh Activate/Close) + 2 nút Xác nhận/Huỷ.
Thành phần bắt buộc: Year Label, badge trạng thái trước/sau, alert cảnh báo (info cho Activate, warning cho Close), nút Xác nhận, nút Huỷ.
Áp dụng Design System: alert info cho Activate, alert warning + nút destructive cho Close.
Trạng thái cần thể hiện: error (chặn Activate khi đã có năm Active khác — MSG-080), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1865–1885 (Table 3-156 Activate/Close Academic Year Use Cases Specification)
- traceability_matrix.md §1.2 (UC-175 ↔ BR-191/BR-193)
- error_message.md MSG-080, MSG-083

# Screen Spec — Promote Students Workspace

**Actor**: School Administration | **UC liên quan**: UC-177 (Report 3 §3.2 "Promote Students to Next Academic Year") | **Status**: Draft
**BR liên quan**: BR-194 (lên lớp là hành động tường minh, không tự động, hỗ trợ loại trừ cá nhân)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa lớp đích), MSG-084 (lên lớp hàng loạt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chọn khối/lớp nguồn (năm học hiện tại) | Select (nhóm theo Grade Level) | — | — | |
| 2 | Chọn lớp đích (năm học mới) | Select (lớp tương ứng đã sao chép cấu trúc) | Sức chứa lớp đích không được vượt 50 (MSG-010) | BR-025, BR-194 | |
| 3 | Danh sách học sinh bị ảnh hưởng (bulk) | Table + Checkbox từng dòng | Mặc định tất cả được chọn để lên lớp | BR-194 | |
| 4 | Bỏ chọn học sinh lưu ban (loại trừ cá nhân) | Checkbox (uncheck để loại trừ) | Học sinh bị loại trừ giữ nguyên liên kết lớp cũ, cần xử lý thủ công sau (AF1, qua UC-090) | BR-194 | |
| 5 | Nút "Xác nhận lên lớp" | Button (primary) | Disable nếu lớp đích vượt sức chứa sau khi tính | BR-025 | |

### Trạng thái màn hình
- Loading: Button "Xác nhận lên lớp" loading khi xử lý hàng loạt.
- Empty: "Chưa chọn khối/lớp nguồn — chọn để xem danh sách học sinh".
- Error: MSG-010 nếu lớp đích vượt sức chứa; MSG-004/MSG-005.
- Success: MSG-084, học sinh được chọn chuyển sang lớp đích năm học mới; học sinh bị loại trừ giữ nguyên liên kết lớp cũ (xử lý thủ công sau qua `Classroom Detail`/`Update Classroom`); hồ sơ sức khỏe/dị ứng không bị ảnh hưởng (BR-194).

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Promote Students" trên `Academic Year Detail`. Sau khi xác nhận quay về `Academic Year Detail` (năm học mới).
**Bố cục**: Trang riêng (không phải Dialog, do có bảng học sinh dài) — Stepper ngang: Chọn khối/lớp nguồn-đích → Xem & loại trừ danh sách → Xác nhận, theo `Layout_Guidelines.md` mục 1.2 (form dài chia bước).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Promote Students Workspace, nền tảng Web, actor School Administration.
Bố cục: Trang riêng với Stepper ngang (Chọn khối nguồn-đích → Xem & loại trừ danh sách học sinh → Xác nhận).
Thành phần bắt buộc: select khối/lớp nguồn, select lớp đích, bảng học sinh + checkbox chọn/loại trừ, nút Xác nhận lên lớp.
Áp dụng Design System: dòng học sinh bị loại trừ hiển thị mờ/màu neutral nhạt.
Trạng thái cần thể hiện: error (vượt sức chứa lớp đích), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1909–1929 (Table 3-158 Promote Students to Next Academic Year Use Cases Specification)
- traceability_matrix.md §1.2 (UC-177 ↔ BR-194)

# Screen Spec — Record Violation Resolution (bản School Administration)

**Actor**: School Administration | **UC liên quan**: UC-180 (Report 3 §3.2 "Record Violation Resolution" — Primary Actor: Medical Staff, Secondary: School Administration) | **Status**: Draft

**BR liên quan**: BR-185 (biên bản xử lý sai phạm), BR-186 (phân cấp phê duyệt — Medium/High cần School Admin), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký 3 bên — mới, 06/07/2026, xem field 7)**
**MSG liên quan**: MSG-004, MSG-005, MSG-085, MSG-087

> **Ghi chú actor** (đã xác nhận qua review chéo mục 6, `00_Team_Work_Assignment_Plan.md`, 2026-07-01): Normal Flow gốc của UC-180 (Report 3 dòng 4661–4681) xác nhận **Medical Staff** là Primary Actor thực hiện bước "Record Resolution"; School Administration là Secondary Actor — vai trò thật là **xem lại (read-only) và phê duyệt** (tách thành UC-182, xem `Web/MedicalStaff/24_RecordViolationResolution.md` để biết action lập biên bản gốc). Phân tích ban đầu của file này (School Admin chỉ xem, không tự lập biên bản) đã được xác nhận đúng — gỡ nhãn "Giả định". Field dữ liệu đã đối chiếu khớp với `Web/MedicalStaff/24_RecordViolationResolution.md` (hợp nhất field "Vi phạm gốc liên kết" và "Trạng thái" sang bản Medical Staff).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Vi phạm gốc liên kết (process hoặc staff conduct) | Text hiển thị + link | — | BR-183, BR-184 | Link sang chi tiết `Violation Registry` |
| 2 | Hành động xử lý đề xuất (actionTaken) | Text hiển thị (đọc-only ở view School Admin) | — | BR-185 | |
| 3 | Người xử lý (handledBy) | Text hiển thị | — | — | |
| 4 | Mức độ hình thức kỷ luật (disciplinaryLevel) | Badge (Nhắc nhở/Cảnh cáo/Đình chỉ/Đề xuất chấm dứt) | Chỉ áp dụng vi phạm nhân viên | BR-185 | |
| 5 | Trạng thái (Pending Approval/Approved/Closed) | Badge (warning/success) | — | BR-186 | |
| 6 | Nút "Chuyển sang duyệt" | Button/Link | Chỉ hiện nếu Pending Approval (Medium/High) | BR-186 | Mở `Approve/Reject Violation Resolution` |
| 7 | Badge "Tình trạng biên bản đã ký" (dựa trên `signedRecordPhotoUrl` của biên bản gốc, BR-211) | Badge (đọc-only, luôn "Đã có ảnh ký xác nhận") | — | BR-211 | Vì `24_RecordViolationResolution.md` (Medical Staff) đã **chặn cứng** không cho gửi phê duyệt khi thiếu `signedRecordPhotoUrl` (gate bắt buộc, BR-211), badge ở màn này về lý thuyết luôn hiển thị "Đã có ảnh ký" — giữ lại để School Administration xem trực tiếp ảnh nếu cần, không phải để kiểm tra thiếu/đủ |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: MSG-085 nếu bản ghi thiếu field bắt buộc (hiển thị cảnh báo dữ liệu không đầy đủ khi School Admin xem); MSG-004/MSG-005.
- Success: Hiển thị đầy đủ biên bản xử lý sai phạm.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Violation Registry` (click 1 bản ghi có resolution). Điều hướng ra: `Approve/Reject Violation Resolution` (nếu Pending Approval).
**Bố cục**: Dashboard Shell — Page Header (badge trạng thái), Content: vi phạm gốc + hành động xử lý (`col-span-8`), badge kỷ luật + trạng thái (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Violation Resolution (bản xem của School Administration), nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (badge trạng thái Pending/Approved), 2 cột: vi phạm gốc + hành động xử lý, badge kỷ luật + nút chuyển sang duyệt.
Thành phần bắt buộc: vi phạm gốc + link, hành động xử lý (đọc-only), người xử lý, badge mức kỷ luật, badge trạng thái, nút Chuyển sang duyệt, badge tình trạng biên bản đã ký.
Áp dụng Design System: badge warning cho Pending, success cho Approved.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm form nhập liệu vì School Admin chỉ xem, không tự lập biên bản (đó là quyền Medical Staff).
```

## Nguồn dữ liệu
- Report 3.md, dòng 4665–4681 (Table 3-161 Record Violation Resolution Use Cases Specification)
- traceability_matrix.md §1.11 (UC-180 ↔ BR-185/BR-186/BR-211)
- 14_Screen_Authorization_Matrix.md khối 10
- `business_rules.md` §18 (BR-211 — mới 06/07/2026); `00_Policies/specialreport2173_1.md`

# Screen Spec — Violation Registry

**Actor**: School Administration | **UC liên quan**: UC-181 (Report 3 §3.2 "View Violation Registry") | **Status**: Draft
**BR liên quan**: BR-189 (danh sách tổng hợp vi phạm)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Loại (Process/Staff/Partner) | Select | — | BR-183, BR-184 | |
| 2 | Filter theo Severity | Select | — | — | |
| 3 | Filter theo Status | Select | Pending/Approved/Closed | — | |
| 4 | Bảng danh sách vi phạm hợp nhất | Table | Cột: Type, Actor/Staff Name, Severity, Status, Reported Date | BR-189 | Nguồn: `NonConformanceRecord` (BR-183) + `StaffViolationRecord` (BR-184) |
| 5 | Badge Severity | Badge (danger=High, warning=Medium, info=Low) | — | — | |
| 6 | Cảnh báo nghi ngờ tái diễn (nếu có) | Badge/Icon cảnh báo | Best-effort, không tự động escalate | BR-187 | MSG-088 |
| 7 | Action: xem lịch sử xử lý | Row click | — | — | Mở `Record Violation Resolution` (nếu đã có) |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có vi phạm nào khớp bộ lọc" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, cảnh báo tái diễn (MSG-088) hiện dưới dạng badge/icon cạnh dòng liên quan.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố" (cạnh Incident Dashboard). Điều hướng ra: `Record Violation Resolution`, `Weekly Override Abuse Report`.
**Bố cục**: Dashboard Shell — filter bar (Type/Severity/Status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Violation Registry, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Type/Severity/Status), bảng dữ liệu (Type/Actor-Staff Name/Severity/Status/Reported Date).
Thành phần bắt buộc: filter, bảng, badge severity, badge/icon cảnh báo tái diễn (best-effort), action xem chi tiết.
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty, success (kèm cảnh báo tái diễn).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4683–4703 (Table 3-162 View Violation Registry Use Cases Specification)
- traceability_matrix.md §1.11 (UC-181 ↔ BR-189)
- business_rules.md BR-187 (cảnh báo tái diễn, best-effort)

# Screen Spec — Approve/Reject Violation Resolution

**Actor**: School Administration | **UC liên quan**: UC-182 (Report 3 §3.2 "Approve/Reject Violation Resolution") | **Status**: Draft
**BR liên quan**: BR-186 (phân cấp phê duyệt — Medium/High cần School Admin), BR-211 (xem ghi chú)
**MSG liên quan**: MSG-004, MSG-005, MSG-089 (phê duyệt thành công, áp dụng kỷ luật)

> **Ghi chú BR-211 (06/07/2026)**: hồ sơ chỉ tới được màn hình này (Pending Approval) sau khi đã vượt qua gate bắt buộc "phải có ảnh biên bản đã ký 3 bên" ở `Web/MedicalStaff/24_RecordViolationResolution.md` — do đó `signedRecordPhotoUrl` **luôn đã tồn tại** khi School Administration xem/duyệt tại đây. Không cần thêm validation/gate riêng ở màn này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chi tiết vi phạm gốc | Text hiển thị | — | — | |
| 2 | Đề xuất xử lý + mức kỷ luật | Text hiển thị | — | BR-185 | |
| 3 | Nút "Approve" | Button (primary) | — | BR-186 | Đóng vi phạm gốc, áp dụng kỷ luật, ghi log |
| 4 | Nút "Reject" + lý do bắt buộc | Button (destructive) + Textarea | Bắt buộc lý do (AF1) | BR-186 | Trả về Medical Staff để sửa lại |

### Trạng thái màn hình
- Loading: Button Approve/Reject loading.
- Empty: Không áp dụng.
- Error: AF1 — thiếu lý do khi Reject → validation error, chặn submit; MSG-004/MSG-005.
- Success: MSG-089 khi Approve (đóng vi phạm gốc, áp dụng kỷ luật); toast xác nhận khi Reject (trả về Medical Staff, thông báo).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Violation Registry` (chọn bản ghi Pending Approval). Sau khi xử lý quay về `Violation Registry`.
**Bố cục**: Dashboard Shell — Page Header (nút Approve/Reject góc phải), Content: chi tiết vi phạm + đề xuất xử lý (1 cột, `col-span-8` căn giữa).

## AI G