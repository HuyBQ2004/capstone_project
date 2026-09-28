# Tổng hợp Screen Specifications — RawFoodSupplier (RawFoodSupplier)

> File này là bản gộp để đọc nhanh/xuất báo cáo — nguồn thật nằm ở từng file lẻ trong cùng thư mục, sửa nội dung ở file lẻ rồi tái tạo lại file này, không sửa tay trực tiếp ở đây.

---


# Screen Spec — Supplier Dashboard

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry point tổng hợp, điều hướng tới các UC khác (UC-152/155/169/170, UC-139, UC-043, UC-039, UC-167) | **Status**: Draft
**BR liên quan**: Không có BR riêng cho bản thân màn hình (BR áp dụng ở từng màn con được điều hướng tới)
**MSG liên quan**: MSG-004 (Session expired), MSG-005 (Access denied) — dùng chung mọi màn hình có xác thực

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title / Breadcrumb | Text | "Homepage > Supply" | — | Report 3 §3.3.2.5.1 |
| 2 | "Log Out" Icon | Button | Terminate session | BR-004 | Điều hướng về Login |
| 3 | Partner Welcome Banner | Card | Hiển thị tên công ty + trường đối tác hiện đang liên kết (Approved) | BR-041 | Nếu chưa có partnership Approved nào, hiển thị Empty state gợi ý sang "Partnership Application" (màn 07) |
| 4 | Section Header — "Supply & Logistics Services" | Label | — | — | Report 3 §3.3.2.5.1 |
| 5 | "Purchase Request" Button/Card | Navigation | Điều hướng tới màn Purchase Requests (09) | — | UC-139 |
| 6 | "Delivery Management" Button/Card | Navigation | Điều hướng tới màn Deliveries (18) | — | UC-039 |
| 7 | "Ingredient Batches" Button/Card | Navigation | Điều hướng tới màn Ingredient Batches (12) | — | UC-043. Report 3 gọi là "Raw Material Shipment" — đổi tên hiển thị theo đúng tên màn chính thức trong `14_Screen_Authorization_Matrix.md` khối 5 |
| 8 | "Food Safety Compliance Record" Button/Card | Navigation | Điều hướng tới màn Compliance Documents (26) | — | UC-167 |
| 9 | "School Partnerships" Card | Navigation | Điều hướng tới màn School Partnerships (03), qua Select School Workspace (02) nếu có >1 trường | — | Bổ sung so với Report 3 gốc (chỉ có 4 nút) vì Screen Flow (`06_Raw_Food_Supplier_Screen_Flow.md`) có nhánh "Partnership status card" từ Dashboard — [Giả định — cần team xác nhận vị trí chính xác trên Dashboard] |
| 10 | Notification Bell Icon | Button | Điều hướng Notification Center | — | Common block, UC-088 |

### Trạng thái màn hình
- Loading: Shimmer cho banner + 4 card chức năng.
- Empty: Nếu supplier chưa có partnership nào Approved, Partner Welcome Banner hiển thị "Chưa có trường đối tác — Nộp đơn hợp tác ngay" kèm CTA tới màn 07.
- Error: MSG-004 (session expired) → chuyển Login; MSG-005 (access denied) → SnackBar `danger`.
- Success: N/A (màn hình chỉ hiển thị, không có action ghi dữ liệu).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào sau khi Login thành công (Common Entry Flow, `13_Guest_Common_Screen_Flow.md`). Tương ứng node `Dash["Supplier Dashboard"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Partnership Detail (→ School Partnerships), Purchase Request Screen, Delivery Management, Food Safety Compliance Record, Ingredient Batches.
**Bố cục**: 1 cột (`Layout_Guidelines.md` §2.1) — AppBar (tiêu đề "Supply" + icon Log Out), Content scroll dọc: Partner Welcome Banner (Card M3) trên cùng, tiếp theo Card danh sách chức năng (Purchase Request/Delivery Management/Ingredient Batches/Food Safety Compliance Record/School Partnerships), BottomNavigationBar 3–5 mục theo actor (xem `Navigation_Guidelines.md` §2). Không có FAB (màn hình chỉ điều hướng, không có action tạo mới trực tiếp).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Supplier Dashboard, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, AppBar + Content scroll (Card-based) + BottomNavigationBar.
Component bắt buộc dùng: AppBar, Card (M3), BottomNavigationBar (Material 3).
Thành phần bắt buộc: Partner Welcome Banner, 4 Card điều hướng (Purchase Request/Delivery Management/Ingredient Batches/Food Safety Compliance Record), School Partnerships card.
Áp dụng Design System: seed color #0E7490, Be Vietnam Pro, Material Design 3 — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty (chưa có partnership).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (§3.3.2.5.1 Supplier Dashboard).
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.

# Screen Spec — Select School Workspace

**Actor**: Raw Food Supplier | **UC liên quan**: Không tìm thấy UC riêng trong Report 3 §3.2 hay `14_Screen_Authorization_Matrix.md` khối 5 (cột UC để trống "—") | **Status**: Draft — [Giả định, cần team xác nhận]
**BR liên quan**: [Giả định] BR-041 (trạng thái Approved/Rejected/Pending theo từng trường) — suy luận, chưa có BR tường minh cho việc 1 supplier phục vụ nhiều trường cùng lúc
**MSG liên quan**: MSG-004, MSG-005

> **[Giả định — cần team xác nhận]**: Xem giải thích đầy đủ ở bản Web cùng tên (`Web/RawFoodSupplier/02_SelectSchoolWorkspace.md`) — không có UC/BR nào xác nhận tường minh mô hình 1 supplier phục vụ nhiều trường. Field bên dưới là suy luận tối thiểu từ tên màn hình trong Authorization Matrix.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Chọn trường đang làm việc" | — | [Giả định] |
| 2 | Danh sách trường đối tác (Approved) | List/Card | Mỗi mục: tên trường, trạng thái hợp tác | BR-041 | [Giả định] Chỉ hiển thị trường có partnership Approved |
| 3 | "Chọn" Button trên mỗi mục | Button | Chọn trường làm workspace hiện tại, lưu vào session/context | — | [Giả định] |
| 4 | "Nộp đơn hợp tác trường mới" Button | Navigation | Điều hướng tới Partnership Application (07) | — | [Giả định] |
| 5 | Workspace hiện tại (chip/badge trên AppBar sau khi chọn) | Badge | Hiển thị tên trường đang active | — | [Giả định] |

### Trạng thái màn hình
- Loading: Shimmer danh sách trường.
- Empty: Chưa có trường đối tác nào Approved → hiển thị CTA "Nộp đơn hợp tác trường mới".
- Error: MSG-004, MSG-005.
- Success: Chọn xong → chuyển thẳng Supplier Dashboard (01) với context trường đã chọn.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào ngay sau Login nếu supplier có ≥2 trường Approved (nếu chỉ có 1 trường, bỏ qua màn này) — [Giả định]. Không xuất hiện trong `06_Raw_Food_Supplier_Screen_Flow.md` gốc — bổ sung theo tên màn hình trong Authorization Matrix.
**Bố cục**: 1 cột — AppBar tối giản (không back nếu là màn đầu sau Login), danh sách Card trường đối tác cuộn dọc, mỗi Card có nút "Chọn"; không có BottomNavigationBar (chưa vào ngữ cảnh chính).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Select School Workspace, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, danh sách Card trường đối tác, mỗi card có nút "Chọn", không có BottomNavigationBar.
Component bắt buộc dùng: Card (M3), Button.
Thành phần bắt buộc: danh sách trường Approved, nút chọn, nút nộp đơn hợp tác trường mới.
Áp dụng Design System: seed color #0E7490, Be Vietnam Pro, Material Design 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đây là màn hình còn [Giả định], giữ tối giản.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5 (dòng "Select School Workspace").
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (Supplier Dashboard).
- Không có nguồn UC/BR trực tiếp — xem cảnh báo [Giả định] ở trên.

# Screen Spec — School Partnerships

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình tổng hợp trạng thái hợp tác, điều hướng tới UC-152 (Submit), UC-169 (Track), UC-170 (Update/Resubmit) qua UC-155 (Manage Supplier Application) | **Status**: Draft
**BR liên quan**: BR-041 (trạng thái nhà cung cấp Pending/Approved/Rejected), BR-046, BR-047 (tài liệu tuân thủ + cảnh báo hết hạn)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Partner details" (theo Report 3 §3.3.2.5.2) | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Partnership Application (07) | — | |
| 4 | School Partner Card | Card | Container hiển thị thông tin quản trị/liên hệ của trường | — | Report 3 §3.3.2.5.2 |
| 5 | School Name | Text | Tên trường đối tác | — | |
| 6 | Delivery address | Text | Địa chỉ giao hàng chỉ định | — | |
| 7 | Trạng thái hợp tác (Chip) | Chip | Pending / Approved / Rejected | BR-041 | Màu theo `Design_System.md` §3 |
| 8 | Trạng thái tài liệu tuân thủ (tóm tắt) | Chip | Valid / Expiring Soon / Expired | BR-046, BR-047 | Điều hướng tới Compliance Documents (26) |
| 9 | "Xem lịch sử phê duyệt" Button | Navigation | Điều hướng tới Application Status (08) | — | `06_Raw_Food_Supplier_Screen_Flow.md` node `PartnerHist` |

### Trạng thái màn hình
- Loading: Shimmer School Partner Card.
- Empty: Chưa có partnership nào → CTA "Nộp đơn hợp tác" (điều hướng 07).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Partnership status card". Tương ứng node `PartnerD["Partnership Detail"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: `PartnerApp` (Partnership Application, 07), `PartnerHist` (Application Status, 08, chỉ đọc).
**Bố cục**: 1 cột — AppBar (Back + tiêu đề), School Partner Card full-width, Chip trạng thái ngay dưới Card, 2 nút hành động cuối màn hình ("Nộp đơn mới" / "Xem lịch sử phê duyệt").

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình School Partnerships (Partnership Detail), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, AppBar Back, School Partner Card full-width, Chip trạng thái, 2 nút hành động cuối màn hình.
Component bắt buộc dùng: Card (M3), Chip, Button.
Thành phần bắt buộc: School Name, Delivery address, Chip trạng thái hợp tác, Chip trạng thái tài liệu, nút xem lịch sử phê duyệt, nút Add/Plus.
Áp dụng Design System: seed color #0E7490, màu chip theo success/warning/danger/info.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6201–6218 (§3.3.2.5.2 Partnership Detail).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-041, BR-046, BR-047.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Product Origin Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-102 - Upload Product Origin Documents — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046
**MSG liên quan**: MSG-008, MSG-009

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Product Origin Documents" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | Danh sách tài liệu xuất xứ | Card list | Mỗi Card: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn, trạng thái | BR-046 | |
| 5 | Trạng thái tài liệu (Chip) | Chip | Valid / Expiring Soon / Expired | BR-046 | |
| 6 | Chọn 1 tài liệu → xem chi tiết/cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có tài liệu → CTA "Tải lên tài liệu đầu tiên".
- Error: MSG-008, MSG-009.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Compliance Documents (26) qua "Product Origin Certification" Card. Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: 1 cột — AppBar (Back + tiêu đề), danh sách Card cuộn dọc (1 tài liệu = 1 Card), FAB "+" góc dưới phải mở Upload Supplier Certificates (06).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Product Origin Documents, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, danh sách Card tài liệu, FAB "+" góc dưới phải.
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton.
Thành phần bắt buộc: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn, chip trạng thái.
Áp dụng Design System: chip theo success/warning/danger.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272 (§3.3.2.5.5), dòng 2507–2527 (UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 116.

# Screen Spec — Food Safety Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046, BR-047
**MSG liên quan**: MSG-008, MSG-009, MSG-053

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Food Safety Certificates" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | Danh sách chứng nhận ATTP | Card list | Mỗi Card: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, trạng thái | BR-046 | |
| 5 | Trạng thái (Chip) | Chip | Valid / Expiring Soon (MSG-053) / Expired | BR-047 | |
| 6 | Chọn 1 chứng nhận → xem chi tiết/cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có chứng nhận → CTA "Tải lên chứng nhận đầu tiên".
- Error: MSG-008, MSG-009.
- Warning: MSG-053 badge "Expiring Soon".
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Compliance Documents (26) qua "Food Safety Certificate" Card. Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: 1 cột — AppBar (Back + tiêu đề), danh sách Card cuộn dọc, FAB "+" mở Upload Supplier Certificates (06).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Safety Certificates, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, danh sách Card chứng nhận, FAB "+".
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton.
Thành phần bắt buộc: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, chip trạng thái.
Áp dụng Design System: chip warning cho Expiring Soon.
Trạng thái cần thể hiện: loading, empty, warning.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272, dòng 2485–2505 (UC-101).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 115.

# Screen Spec — Upload Supplier Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates, UC-102 - Upload Product Origin Documents (form dùng chung) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-008, MSG-009, MSG-013

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload documents" | — | Report 3 §3.3.2.5.7 |
| 2 | "Back" Button (AppBar) | Button | Về màn danh sách gọi tới (04 hoặc 05) | — | |
| 3 | "Cancel" Button | Button | Huỷ, không lưu | — | |
| 4 | Section Header — "Upload New Profile" | Label | — | — | |
| 5 | Classification of compliance documents (dropdown) | Select | Bắt buộc — Food Safety Certificate hoặc Product Origin Document | — | |
| 6 | Document Classification Text | Text | — | — | |
| 7 | Tên tài liệu / Cơ quan cấp | Input text | Bắt buộc | BR-046 | |
| 8 | File tài liệu | File upload (camera/thư viện) | Bắt buộc, PDF/JPG/PNG, ≤10MB | BR-046 | |
| 9 | Expiration date of the document | Date picker | Bắt buộc nếu Food Safety Certificate | BR-047, BR-048 | Ngày đã qua + loại Food Safety → chặn, MSG-013 |
| 10 | "Add / Plus" Icon | Button | Thêm 1 tài liệu khác | — | |
| 11 | "Submit"/"Save" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048 | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-008, MSG-009, MSG-013.
- Warning: Ngày hết hạn <30 ngày → cảnh báo nhưng vẫn lưu được (BR-047).
- Success: Lưu thành công → quay về màn danh sách gọi tới.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26). Tương ứng node `UploadCert["Supplier Upload Certificates Form"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`, cũng được gọi từ `BatchDetail["Ingredient Batch Detail"]` (upload certificate cho batch cụ thể — xem màn Upload Batch Certificates, 16).
**Bố cục**: Trượt từ dưới lên (`showModalBottomSheet`, `Layout_Guidelines.md` §1.2/`Component_Guidelines.md`) — form ngắn 1 trang, dùng `image_picker` cho file upload.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Supplier Certificates, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet trượt từ dưới lên, form 1 trang.
Component bắt buộc dùng: DropdownMenu (M3), TextField, DatePicker, image_picker upload, FilledButton.
Thành phần bắt buộc: dropdown phân loại tài liệu, tên tài liệu/cơ quan cấp, file upload, ngày hết hạn, nút Submit/Cancel/Add.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-008/009/013), warning, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6293–6309 (§3.3.2.5.7), dòng 2485–2527 (UC-101, UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-008, MSG-009, MSG-013.

# Screen Spec — Partnership Application

**Actor**: Raw Food Supplier | **UC liên quan**: UC-152 - Submit Supplier Application (Report 3 §3.2, dòng 2639–2658); liên quan UC-155, UC-170 | **Status**: Draft
**BR liên quan**: BR-040, BR-046, BR-047, BR-048, BR-010
**MSG liên quan**: MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025, MSG-028

> **Ghi chú đối chiếu nguồn**: xem giải thích lệch BR-050 ở bản Web cùng tên (`Web/RawFoodSupplier/07_PartnershipApplication.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Apply for Onboarding" | — | |
| 2 | Company Name | Input text | Bắt buộc | BR-040 | |
| 3 | Tax ID | Input text | Bắt buộc, đúng định dạng (MSG-007 nếu sai) | BR-040 | |
| 4 | Business Address | Input text | Bắt buộc | BR-040 | |
| 5 | Business License (file) | File upload (camera/thư viện) | Bắt buộc, PDF/JPG/PNG, ≤10MB (MSG-008/009) | BR-040 | |
| 6 | Food Safety Certificate(s) (file, có thể nhiều) | File upload (multi) | Bắt buộc ≥1, không hết hạn (BR-048, MSG-013) | BR-046, BR-047, BR-048 | |
| 7 | Ngày hết hạn từng chứng nhận | Date picker | Bắt buộc theo mỗi file | BR-047 | |
| 8 | "Submit" Button | Button | Validate toàn bộ trước khi gửi | BR-040, BR-046, BR-047, BR-048 | |
| 9 | "Cancel" Button | Button | Huỷ, quay về School Partnerships (03) | — | |

### Trạng thái màn hình
- Loading: Shimmer form khi tải lại dữ liệu đơn cũ (Update/Resubmit UC-170).
- Empty: N/A.
- Error: MSG-007, MSG-008, MSG-009, MSG-013, MSG-025.
- Success: MSG-028 → điều hướng Application Status (08).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ School Partnerships (03) qua nút "submit application". Tương ứng node `PartnerApp["Supplier Partnership Application Form"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`, dẫn tiếp tới `VD["Verification Documents Upload"]`. Điều hướng đi: Application Status (08).
**Bố cục**: Form nhiều trang riêng (không Stepper ngang, `Layout_Guidelines.md` §2.2) — Trang 1: Company Name/Tax ID/Business Address; Trang 2: Upload Business License + Food Safety Certificate + ngày hết hạn (dùng `image_picker`). Nút "Tiếp theo/Quay lại" cố định dưới cùng, nút Submit ở trang cuối.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Partnership Application, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: form 2 trang riêng, nút Tiếp theo/Quay lại cố định dưới cùng.
Component bắt buộc dùng: TextField, DatePicker (M3), image_picker upload + preview, FilledButton.
Thành phần bắt buộc: Company Name, Tax ID, Business Address, Business License upload, Food Safety Certificate upload (multi) + ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490, Material Design 3.
Trạng thái cần thể hiện: error (MSG-007/008/009/013), success (MSG-028).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2639–2658 (UC-152).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-010, BR-040, BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-007, MSG-008, MSG-009, MSG-013, MSG-025, MSG-028.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Application Status

**Actor**: Raw Food Supplier | **UC liên quan**: UC-169 - Track Supplier Application Status; liên quan UC-170 - Update/Resubmit Supplier Application, UC-155 - Manage Supplier Application | **Status**: Draft
**BR liên quan**: BR-041, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-029

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Application Status" | — | |
| 2 | Trạng thái đơn (Chip) | Chip | Pending / Approved / Rejected / Action Required | BR-041 | |
| 3 | Bình luận/Lý do từ School Administration | Text block | Hiển thị khi Rejected/Action Required | BR-048 | |
| 4 | "Edit/Update Application" Button | Button | Chỉ hiện khi Rejected hoặc Action Required | — | Điều hướng tới Partnership Application (07) ở chế độ Edit |
| 5 | Ngày nộp / Ngày cập nhật gần nhất | Text | — | — | |
| 6 | "Xem lịch sử phê duyệt" | List | Danh sách các lần nộp/duyệt/từ chối trước đó | — | Node `PartnerHist` |

### Trạng thái màn hình
- Loading: Shimmer chip + text block.
- Empty: Chưa từng nộp đơn → điều hướng về Partnership Application (07).
- Error: MSG-004, MSG-005.
- Success: MSG-029 sau khi resubmit thành công.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ School Partnerships (03) qua "Xem lịch sử phê duyệt" hoặc sau Submit ở Partnership Application (07). Tương ứng node `PartnerHist["Partnership Approval History"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Partnership Application (07) chế độ Edit.
**Bố cục**: 1 cột — AppBar (Back), khối trạng thái (Chip + bình luận) trên cùng, danh sách lịch sử phê duyệt dạng Card cuộn dọc bên dưới, nút "Edit/Update Application" cố định cuối màn hình khi điều kiện đúng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Application Status, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, khối trạng thái (Chip + bình luận) trên cùng, danh sách Card lịch sử phê duyệt bên dưới.
Component bắt buộc dùng: Chip, Card (M3), MaterialBanner (bình luận), FilledButton.
Thành phần bắt buộc: Chip trạng thái, bình luận, nút Edit/Update Application (điều kiện), lịch sử phê duyệt.
Áp dụng Design System: chip theo success/warning/danger/info.
Trạng thái cần thể hiện: error, success (MSG-029), empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2659–2718 (UC-169, UC-170, UC-155).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-041, BR-048.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Purchase Requests

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-139 (View Purchase Requests) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | Report 3 §3.3.2.5.3 |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái (Pending/Confirmed/Delivered) | Chip/Counter | Tổng hợp số lượng theo trạng thái | — | [Giả định — cần team xác nhận có cần hay không] |
| 4 | Điều hướng tới Purchase Request List | Navigation | Điều hướng tới màn 10 | — | |

### Trạng thái màn hình
- Loading: Shimmer bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Purchase Request" Button. Tương ứng node `PRList["Purchase Request Screen"]` trong `06_Raw_Food_Supplier_Screen_Flow.md` (dùng luôn làm màn danh sách chính — xem ghi chú ở màn 10 về khả năng gộp 2 màn 09/10 trên Mobile).
**Bố cục**: 1 cột — AppBar, dải Chip đếm nhanh theo trạng thái trên cùng, sau đó chuyển thẳng vào danh sách (xem màn 10).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Requests (entry), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, dải Chip đếm nhanh theo trạng thái.
Component bắt buộc dùng: Chip, Card (M3).
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237 (§3.3.2.5.3).
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.

# Screen Spec — Purchase Request List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-139 - View Purchase Requests | **Status**: Draft
**BR liên quan**: BR-077 (xem ghi chú đối chiếu nguồn ở bản Web cùng tên)
**MSG liên quan**: MSG-004, MSG-005; MSG-018 (chưa xác nhận áp dụng — xem ghi chú bản Web)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | Section Header — "Purchase order list" | Label | — | — | |
| 4 | Purchase Order Card (mỗi mục) | Card | Container tóm tắt 1 đơn | — | |
| 5 | Order code | Text | Mã đơn hàng duy nhất | — | |
| 6 | Status Badge (Chip) | Chip | "Awaiting confirmation" / "Delivery completed" / khác | — | |
| 7 | Source of request | Text | Nguồn gốc yêu cầu | — | |
| 8 | Delivery date required | Text | Ngày/giờ giao hàng yêu cầu | — | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã đơn | — | |
| 10 | Chọn 1 Card → xem chi tiết | Navigation | Điều hướng tới Purchase Request Detail (11) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có đơn mua hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Purchase Request" Button. Tương ứng trực tiếp node `PRList["Purchase Request Screen"]` trong `06_Raw_Food_Supplier_Screen_Flow.md` (trên Mobile, màn 09 "Purchase Requests" và màn 10 "Purchase Request List" thực chất là 1 màn hình duy nhất theo Screen Flow gốc — giữ 2 file riêng theo đúng tên trong Authorization Matrix, nhưng bố cục Mobile gộp phần đếm nhanh (09) vào đầu danh sách (10) để tránh 1 màn hình rỗng trung gian). Điều hướng đi: Purchase Request Detail (11).
**Bố cục**: 1 cột — AppBar, thanh filter/search cố định trên cùng, danh sách Card cuộn dọc (pull-to-refresh bắt buộc theo `Layout_Guidelines.md` §2.1).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request List, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, thanh filter/search trên cùng, danh sách Card cuộn dọc, pull-to-refresh.
Component bắt buộc dùng: Card (M3), Chip, TextField (search).
Thành phần bắt buộc: Order code, Status Chip, Source of request, Delivery date required.
Áp dụng Design System: chip theo trạng thái.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237, dòng 3419–3439 (UC-139).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-077.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Purchase Request Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-139 - View Purchase Requests; hành động Accept dẫn sang UC-015 - Confirm Delivery Schedule | **Status**: Draft
**BR liên quan**: BR-077, BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã đơn hàng | — | |
| 2 | "Back" Button (AppBar) | Button | Về Purchase Request List (10) | — | |
| 3 | "Come back" Button | Button | Huỷ xem chi tiết | — | |
| 4 | Order number | Text | Mã định danh đơn hàng | — | |
| 5 | Recipient | Text | Trường/canteen nhận hàng | — | |
| 6 | Delivery deadline | Text | Ngày giao hạn chót | — | |
| 7 | List of ingredients to order (Section Header) | Label | — | — | |
| 8 | Ingredients List Container | List (Card con) | Tên nguyên liệu, khối lượng, mã lô dự kiến | BR-071 | |
| 9 | "Accept" Button | Button | Chấp nhận → điều hướng Confirm Delivery Schedule (22) | — | |
| 10 | "Reject" Button | Button | Từ chối, yêu cầu nhập lý do | — | [Giả định — cần team xác nhận có bắt buộc lý do hay không] |

### Trạng thái màn hình
- Loading: Shimmer chi tiết đơn.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: Accept → Confirm Delivery Schedule (22); Reject → về danh sách (10).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Purchase Request List (10) khi chọn 1 Card. Tương ứng node `PRDetail["Purchase Request Detail"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Confirm Delivery Schedule (22, node `PRResponse`).
**Bố cục**: 1 cột — AppBar (Back), thông tin đơn (Order number/Recipient/Delivery deadline) trên cùng dạng Card, danh sách nguyên liệu Card con cuộn dọc bên dưới, 2 nút Accept/Reject cố định cuối màn hình (vùng ngón cái).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request Detail, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, Card thông tin đơn trên cùng, danh sách Card nguyên liệu cuộn dọc, 2 nút Accept/Reject cố định cuối màn hình.
Component bắt buộc dùng: Card (M3), FilledButton (Accept), OutlinedButton (Reject).
Thành phần bắt buộc: Order number, Recipient, Delivery deadline, danh sách nguyên liệu, nút Accept/Reject.
Áp dụng Design System: seed color #0E7490, màu danger cho Reject.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6239–6256 (§3.3.2.5.4).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).

# Screen Spec — Ingredient Batches

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-043 và các UC con | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | Report 3 §3.3.2.5.6 |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Bộ đếm nhanh theo trạng thái lô | Chip/Counter | — | BR-071 | [Giả định nhẹ] |
| 5 | Điều hướng tới Batch List | Navigation | Điều hướng tới màn 13 | — | |

### Trạng thái màn hình
- Loading: Shimmer bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Raw Material Shipment"/"Ingredient Batches" Button. Tương ứng node `BatchList["Ingredient Batches List"]` trong `06_Raw_Food_Supplier_Screen_Flow.md` (trên Mobile, màn 12 và 13 gộp thành 1 màn hình liên tục — dải Chip đếm nhanh nằm ở đầu danh sách Batch List). Điều hướng đi: Batch List (13), Submit Batch Information (15).
**Bố cục**: 1 cột — AppBar, dải Chip đếm nhanh trên cùng, FAB "+" mở Submit Batch Information (15).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Ingredient Batches (entry), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, dải Chip đếm nhanh theo trạng thái, FAB "+".
Component bắt buộc dùng: Chip, FloatingActionButton.
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: chip theo success/warning/danger/critical.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291.
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Batch List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-043 - Manage Ingredient Batches | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Ingredient Batches (12) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Section Header — "List of outgoing shipments" | Label | — | — | |
| 5 | Material Batch Card | Card | Container tóm tắt 1 lô | BR-071 | |
| 6 | Batch Name & Code | Text | Tên nguyên liệu + mã lô | BR-071 | |
| 7 | Status Badge (Chip) | Chip | "Quarantine passed" / "VietGAP achieved" / khác | BR-071 | |
| 8 | Packaging / Harvest Date | Text | Ngày đóng gói/thu hoạch | BR-071 | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã lô | — | |
| 10 | Chọn 1 Card → xem chi tiết | Navigation | Điều hướng tới Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có lô nào → CTA "Đăng ký lô đầu tiên".
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Ingredient Batches (12). Tương ứng node `BatchList["Ingredient Batches List"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Batch Detail (14), Submit Batch Information (15).
**Bố cục**: 1 cột — AppBar, thanh filter/search, danh sách Card cuộn dọc (pull-to-refresh), FAB "+" mở Submit Batch Information (15).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch List, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, thanh filter/search, danh sách Card, FAB "+", pull-to-refresh.
Component bắt buộc dùng: Card (M3), Chip, TextField, FloatingActionButton.
Thành phần bắt buộc: Batch Name & Code, Status Chip, Packaging/Harvest Date.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291, dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Batch Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-043 - Manage Ingredient Batches; điều hướng tới UC-100, UC-107 | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên/Mã lô | — | |
| 2 | "Back" Button (AppBar) | Button | Về Batch List (13) | — | |
| 3 | Product Name | Text | Tên nguyên liệu | BR-071 | |
| 4 | Lot Number | Text | Số lô | BR-071 | |
| 5 | Packaging Date | Text | Ngày đóng gói | BR-071 | |
| 6 | Expiration Date | Text | Hạn sử dụng | BR-071 | |
| 7 | Status Badge (Chip) | Chip | Quarantine passed / VietGAP achieved / khác | BR-071 | |
| 8 | Chứng chỉ liên kết (danh sách) | List | Liên kết chứng chỉ đã upload cho lô này | BR-071 | |
| 9 | "Upload Batch Certificates" Button | Navigation | Điều hướng tới màn 16 | — | |
| 10 | "View Batch History" Button | Navigation | Điều hướng tới màn 17 | — | |
| 11 | "Edit" Button (FAB) | Navigation | Điều hướng tới Submit Batch Information (15) chế độ chỉnh sửa | BR-071 | |

### Trạng thái màn hình
- Loading: Shimmer chi tiết lô.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Batch List (13) khi chọn 1 Card. Tương ứng node `BatchDetail["Ingredient Batch Detail"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: `UploadCert` (Upload Batch Certificates, 16), View Batch History (17), Submit Batch Information (15, edit).
**Bố cục**: 1 cột — AppBar (Back), Card thông tin lô trên cùng, danh sách chứng chỉ liên kết bên dưới, FAB "Edit" góc dưới phải, 2 nút hành động (Upload Batch Certificates / View Batch History) cuối màn hình.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch Detail, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, Card thông tin lô trên cùng, danh sách chứng chỉ bên dưới, FAB Edit, 2 nút hành động cuối màn hình.
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton, OutlinedButton.
Thành phần bắt buộc: Product Name, Lot Number, Packaging Date, Expiration Date, Status Chip, chứng chỉ liên kết.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291, dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `BatchDetail`).

# Screen Spec — Submit Batch Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-081 - Submit Batch Information | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048, BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-013

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Batch Information" | — | |
| 2 | Product Name | Input text | Bắt buộc | BR-071 | |
| 3 | Lot Number | Input text | Bắt buộc, duy nhất | BR-071 | |
| 4 | Manufacturing Date | Date picker | Bắt buộc | BR-071 | |
| 5 | Packaging Date | Date picker | Bắt buộc | BR-071 | |
| 6 | Expiration Date | Date picker | Bắt buộc, sau Packaging Date | BR-071 | |
| 7 | Liên kết chứng chỉ an toàn | Multi-select | Chọn từ Compliance Documents (26) | BR-046 | MSG-013 nếu chứng chỉ hết hạn |
| 8 | "Submit" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048, BR-071 | |
| 9 | "Cancel" Button | Button | Huỷ, về Batch List (13)/Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-013.
- Success: Đăng ký lô thành công → điều hướng Batch Detail (14).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Ingredient Batches (12)/Batch List (13) qua FAB "+", hoặc Batch Detail (14) qua FAB "Edit".
**Bố cục**: Form 1 trang trong `showModalBottomSheet` (form ngắn <8 field, `Component_Guidelines.md`).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Batch Information, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet, form 1 trang.
Component bắt buộc dùng: TextField, DatePicker (M3), DropdownMenu (multi), FilledButton.
Thành phần bắt buộc: Product Name, Lot Number, Manufacturing/Packaging/Expiration Date, liên kết chứng chỉ, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-013), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3265–3285 (UC-081).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048, BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-013.

# Screen Spec — Upload Batch Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-100 - Upload Batch Certificates — extends UC-043 | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-009

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload Batch Certificates" | — | |
| 2 | Lô hàng đang chọn (chỉ đọc) | Text/Chip | Tên/mã lô kế thừa từ Batch Detail (14) | BR-071 | |
| 3 | File chứng chỉ (PDF/image) | File upload (camera/thư viện) | Bắt buộc, ≤10MB | BR-071 | MSG-009 nếu sai định dạng |
| 4 | Ngày hết hạn chứng chỉ | Date picker | Bắt buộc | BR-071 | |
| 5 | "Submit"/"Save" Button | Button | Validate + liên kết vào lô | BR-071 | |
| 6 | "Cancel" Button | Button | Huỷ, về Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-009.
- Success: Về Batch Detail (14), cập nhật danh sách chứng chỉ.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Batch Detail (14) qua nút "Upload Batch Certificates" — tương ứng node `UploadCert` trong `06_Raw_Food_Supplier_Screen_Flow.md` (gọi từ `BatchDetail`). Điều hướng đi: quay lại Batch Detail (14).
**Bố cục**: Modal bottom sheet 1 trang, dùng `image_picker`.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Batch Certificates, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet form ngắn.
Component bắt buộc dùng: image_picker upload, DatePicker (M3), FilledButton.
Thành phần bắt buộc: lô hàng đang chọn (chỉ đọc), file chứng chỉ, ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-009), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3331–3351 (UC-100).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-009.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `UploadCert`, gọi từ `BatchDetail`).

# Screen Spec — View Batch History

**Actor**: Raw Food Supplier | **UC liên quan**: UC-107 - View Batch History — extends UC-043 | **Status**: Draft
**BR liên quan**: BR-071, BR-076 (xem ghi chú đối chiếu nguồn ở bản Web cùng tên)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Batch History" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Batch Detail (14) | — | |
| 3 | Danh sách toàn bộ lô đã đăng ký | Card list | Lot Number, Packaging Date, Expiration Date, Certificate Links | BR-071, BR-076 | |
| 4 | Search theo Lot Number | Input | — | — | |
| 5 | Filter theo Expiration Status | Dropdown | Valid / Expiring Soon / Expired | BR-047 | |

### Trạng thái màn hình
- Loading: Shimmer danh sách.
- Empty: Chưa có lô nào trong lịch sử.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Batch Detail (14) qua nút "View Batch History".
**Bố cục**: 1 cột — AppBar (Back), thanh search/filter trên cùng, danh sách Card cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Batch History, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, thanh search/filter, danh sách Card cuộn dọc, pull-to-refresh.
Component bắt buộc dùng: Card (M3), TextField (search), DropdownMenu (filter).
Thành phần bắt buộc: Lot Number, Packaging Date, Expiration Date, Certificate Links.
Áp dụng Design System: chip/badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3353–3373 (UC-107).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-076.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 145.

# Screen Spec — Deliveries

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-039 và các UC con | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Management" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái | Chip/Counter | — | BR-071 | [Giả định nhẹ] |
| 4 | Điều hướng tới Delivery List | Navigation | Điều hướng tới màn 19 | — | |

### Trạng thái màn hình
- Loading: Shimmer bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Delivery Management" Button. Tương ứng node `DelMgmt["Delivery Management"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Delivery List (19), Submit Delivery Information (21).
**Bố cục**: 1 cột — AppBar, dải Chip đếm nhanh theo trạng thái trên cùng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Deliveries (entry), nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, dải Chip đếm nhanh theo trạng thái.
Component bắt buộc dùng: Chip.
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199.
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Delivery List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-039 - Manage Deliveries | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Deliveries" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Deliveries (18) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Delivery Information (21) | — | |
| 4 | Delivery Card | Card | Vehicle, Driver, Linked Batches, Schedule Status | BR-071 | |
| 5 | Filter theo ngày/trạng thái | Dropdown | — | — | |
| 6 | Search theo Delivery ID | Input | — | — | |
| 7 | Chọn 1 Card → xem chi tiết | Navigation | Điều hướng tới Delivery Detail (20) | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách Card.
- Empty: Chưa có lịch giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Deliveries (18). Tương ứng node `DelMgmt["Delivery Management"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Delivery Detail (20), Submit Delivery Information (21).
**Bố cục**: 1 cột — AppBar, filter/search trên cùng, danh sách Card cuộn dọc (pull-to-refresh), FAB "+" mở Submit Delivery Information (21).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery List, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, filter/search trên cùng, danh sách Card, FAB "+", pull-to-refresh.
Component bắt buộc dùng: Card (M3), Chip, DropdownMenu, TextField, FloatingActionButton.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Delivery Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-039 - Manage Deliveries; điều hướng tới UC-092, UC-109 | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng | — | |
| 2 | "Back" Button (AppBar) | Button | Về Delivery List (19) | — | |
| 3 | Vehicle | Text | — | BR-071 | |
| 4 | Driver | Text | — | BR-050 | |
| 5 | Linked Batches | List (Card con) | Danh sách lô nguyên liệu gắn với chuyến | BR-071 | |
| 6 | Schedule Status (Chip) | Chip | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071, BR-135 | |
| 7 | "Edit" Button | Navigation | Điều hướng Submit Delivery Information (21, edit) | BR-071 | Chỉ khi Pending |
| 8 | "Cancel Delivery" Button | Button | Chỉ khả dụng nếu Pending | BR-071 | |
| 9 | "Update Status" Button | Navigation | Điều hướng Update Delivery Status (23) | — | |
| 10 | "View Batch Records" Button | Navigation | Điều hướng View Delivery Batch Records (24) | — | |

### Trạng thái màn hình
- Loading: Shimmer chi tiết.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Delivery List (19) khi chọn 1 Card. Điều hướng đi: Submit Delivery Information (21, edit), Update Delivery Status (23), View Delivery Batch Records (24).
**Bố cục**: 1 cột — AppBar (Back), Card thông tin chuyến giao trên cùng, danh sách Linked Batches Card con bên dưới, các nút hành động xếp dọc cuối màn hình.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery Detail, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, Card thông tin chuyến giao trên cùng, danh sách Card lô liên kết bên dưới, nút hành động xếp dọc cuối màn hình.
Component bắt buộc dùng: Card (M3), Chip, OutlinedButton, FilledButton.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status, nút Edit/Cancel/Update Status/View Batch Records.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071, BR-135.

# Screen Spec — Submit Delivery Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-083 - Submit Delivery Information | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Delivery Information" | — | |
| 2 | Purchase Request đích | Select | Bắt buộc, chỉ hiện PR đã Accept | — | |
| 3 | Dispatch Date | Date picker | Bắt buộc, ≥24 giờ trước giờ giao dự kiến | BR-071 | |
| 4 | Vehicle License Plate | Input text | Bắt buộc | BR-071 | |
| 5 | Driver Details | Select | Bắt buộc, chọn từ Delivery Person đã đăng ký | BR-050 | |
| 6 | Liên kết lô nguyên liệu | Multi-select | Bắt buộc ≥1 | BR-071 | |
| 7 | "Submit" Button | Button | Validate ≥24h + đầy đủ field | BR-071 | |
| 8 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: Chặn nếu nộp <24 giờ trước giờ giao.
- Success: 'Scheduled' → điều hướng Delivery Detail (20).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Deliveries (18)/Delivery List (19) qua FAB "+". Tương ứng node `ShipForm["Raw Material Shipment Form"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`, dẫn tiếp `ShipDocs["Attach Delivery Documents"]`/`ShipTrack["Shipment Tracking"]`.
**Bố cục**: Form nhiều trang riêng (`Layout_Guidelines.md` §2.2) — Trang 1: Purchase Request/Dispatch Date; Trang 2: Vehicle/Driver; Trang 3: liên kết lô nguyên liệu. Nút Tiếp theo/Quay lại cố định dưới cùng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Delivery Information, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: form 3 trang riêng, nút Tiếp theo/Quay lại cố định dưới cùng.
Component bắt buộc dùng: DropdownMenu, DatePicker (M3), TextField, FilledButton.
Thành phần bắt buộc: Purchase Request đích, Dispatch Date, Vehicle License Plate, Driver, liên kết lô nguyên liệu.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3287–3307 (UC-083).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `ShipForm`).

# Screen Spec — Confirm Delivery Schedule

**Actor**: Raw Food Supplier | **UC liên quan**: UC-015 - Confirm Delivery Schedule | **Status**: Draft
**BR liên quan**: BR-050, BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Confirm Delivery Schedule" | — | |
| 2 | Danh sách Purchase Request đã Accept, chờ lên lịch | List | — | — | |
| 3 | Delivery Date | Date picker | Bắt buộc | BR-071 | |
| 4 | Estimated Arrival Time Window | Input (time range) | Bắt buộc | BR-071 | |
| 5 | Delivery Person | Select | Bắt buộc, phải có hồ sơ sinh trắc học | BR-050 | |
| 6 | "Confirm Schedule" Button | Button | Validate ≥24 giờ trước dispatch | BR-050, BR-071 | |
| 7 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách.
- Empty: Không có PR nào chờ lên lịch.
- Error: Chặn nếu <24 giờ hoặc thiếu biometric.
- Success: → điều hướng Delivery Detail (20).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Purchase Request Detail (11) qua nút "Accept". Tương ứng node `PRResponse["Confirm Quantity & Delivery Date"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`.
**Bố cục**: Modal bottom sheet 1 trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Confirm Delivery Schedule, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet form ngắn.
Component bắt buộc dùng: DropdownMenu, DatePicker (M3), TextField (time range), FilledButton.
Thành phần bắt buộc: Delivery Date, Estimated Arrival Time Window, Delivery Person, nút Confirm Schedule/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3023–3043 (UC-015).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).

# Screen Spec — Update Delivery Status

**Actor**: Raw Food Supplier | **UC liên quan**: UC-092 - Update Delivery Status — extends UC-039 | **Status**: Draft
**BR liên quan**: BR-135, BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-029, MSG-065

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng | — | |
| 2 | Trạng thái hiện tại (chỉ đọc) | Chip | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071 | |
| 3 | Cập nhật trạng thái mới | Dropdown | Dispatched/In Transit/Delayed/Delivered | BR-071 | |
| 4 | "Save" Button | Button | Deviation >30 phút → gửi MSG-065 | BR-135 | |
| 5 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Warning: MSG-065.
- Success: MSG-029 → về Delivery Detail (20).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "Update Status".
**Bố cục**: Modal bottom sheet form ngắn — ưu tiên thao tác nhanh 1 tay ngoài hiện trường (tài xế cập nhật trạng thái khi đang di chuyển).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Delivery Status, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet, form ngắn, thao tác nhanh 1 tay.
Component bắt buộc dùng: Chip, DropdownMenu (M3), FilledButton.
Thành phần bắt buộc: trạng thái hiện tại, dropdown cập nhật trạng thái, nút Save/Cancel.
Áp dụng Design System: chip theo Design_System.md mục 3.
Trạng thái cần thể hiện: warning (MSG-065), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3309–3329 (UC-092).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-135.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-029, MSG-065.

# Screen Spec — View Delivery Batch Records

**Actor**: Raw Food Supplier | **UC liên quan**: UC-109 - View Delivery Batch Records — extends UC-043 | **Status**: Draft
**BR liên quan**: BR-071, BR-077 (xem ghi chú đối chiếu nguồn ở bản Web cùng tên)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Batch Records" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Delivery Detail (20) | — | |
| 3 | Danh sách chuyến giao hàng lịch sử | Card list | Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes | BR-071, BR-077 | |
| 4 | Filter theo khoảng ngày dispatch | Date range picker | — | — | |
| 5 | Filter theo trạng thái giao hàng | Dropdown | — | — | |

### Trạng thái màn hình
- Loading: Shimmer danh sách.
- Empty: Chưa có lịch sử giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "View Batch Records".
**Bố cục**: 1 cột — AppBar (Back), filter (date range + trạng thái) trên cùng, danh sách Card cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Delivery Batch Records, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, filter date range + trạng thái trên cùng, danh sách Card cuộn dọc, pull-to-refresh.
Component bắt buộc dùng: Card (M3), DatePicker (range), DropdownMenu.
Thành phần bắt buộc: Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes.
Áp dụng Design System: chip/badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3375–3395 (UC-109).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 146.

# Screen Spec — Update Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-091 - Update Compliance Documents | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-009, MSG-029, MSG-053

> **Ghi chú đối chiếu nguồn**: xem giải thích lệch MSG-013 vs MSG-009 ở bản Web cùng tên (`Web/RawFoodSupplier/25_UpdateDocuments.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên tài liệu đang cập nhật | — | |
| 2 | "Back" Button (AppBar) | Button | Về màn danh sách gọi tới | — | |
| 3 | Thông tin tài liệu hiện tại | Text/Chip | Chỉ đọc | BR-046 | |
| 4 | File thay thế | File upload (camera/thư viện) | Bắt buộc nếu thay tài liệu, ≤10MB | BR-046 | MSG-009 nếu sai định dạng |
| 5 | Ngày hết hạn mới | Date picker | Bắt buộc khi thay file | BR-047, BR-048 | |
| 6 | "Submit"/"Save" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048 | |
| 7 | "Cancel" Button | Button | Huỷ, giữ nguyên tài liệu cũ | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-009.
- Warning: MSG-053.
- Success: MSG-029.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26).
**Bố cục**: Modal bottom sheet — thông tin tài liệu cũ (chỉ đọc) trên cùng, form thay thế bên dưới, nút Submit/Cancel cố định cuối sheet.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Documents, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet, thông tin tài liệu cũ trên cùng, form thay thế bên dưới.
Component bắt buộc dùng: Chip, image_picker upload, DatePicker (M3), FilledButton.
Thành phần bắt buộc: thông tin tài liệu hiện tại, file thay thế, ngày hết hạn mới, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-009), warning (MSG-053), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2463–2483 (UC-091).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 114.

# Screen Spec — Compliance Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-167 - Manage Compliance Documents | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-053

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Compliance Record" | — | |
| 2 | "Back" Button (AppBar) | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | "Food Safety Certificate" Card | Navigation | Điều hướng tới Food Safety Certificates (05) | BR-046, BR-047 | |
| 5 | "Product Origin Certification" Card | Navigation | Điều hướng tới Product Origin Documents (04) | BR-046 | |
| 6 | Tóm tắt số lượng tài liệu theo trạng thái | Chip/Counter | Valid / Expiring Soon / Expired | BR-047 | |
| 7 | Danh sách đầy đủ tài liệu tuân thủ (filter) | Card list | Filter by type/status | BR-046, BR-047, BR-048 | |

### Trạng thái màn hình
- Loading: Shimmer 2 Card + danh sách.
- Empty: Chưa có tài liệu → CTA upload đầu tiên.
- Warning: MSG-053.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Supplier Dashboard (01) qua "Food Safety Compliance Record" Button. Tương ứng node `CompRec["Food Safety Compliance Record"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Food Safety Certificates (05), Product Origin Documents (04), Upload Supplier Certificates (06).
**Bố cục**: 1 cột — AppBar, 2 Card điều hướng full-width trên cùng, danh sách Card tổng hợp filter được cuộn dọc bên dưới, FAB "+" mở Upload Supplier Certificates (06).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Documents, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: 1 cột, 2 Card điều hướng trên cùng, danh sách Card tổng hợp bên dưới, FAB "+".
Component bắt buộc dùng: Card (M3), Chip, FloatingActionButton.
Thành phần bắt buộc: 2 Card điều hướng, tóm tắt trạng thái, danh sách filter được.
Áp dụng Design System: chip theo success/warning/danger.
Trạng thái cần thể hiện: loading, empty, warning.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272, dòng 2353–2373 (UC-167).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 113.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
