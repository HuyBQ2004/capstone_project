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
- Loading: Skeleton cho banner + 4 card chức năng.
- Empty: Nếu supplier chưa có partnership nào Approved, Partner Welcome Banner hiển thị "Chưa có trường đối tác — Nộp đơn hợp tác ngay" kèm CTA tới màn 07.
- Error: MSG-004 (session expired) → chuyển Login; MSG-005 (access denied) → Alert destructive.
- Success: N/A (màn hình chỉ hiển thị, không có action ghi dữ liệu).

---

## Platform Layout — Web
**Entry/Exit points**: Vào sau khi Login thành công (Common Entry Flow, `13_Guest_Common_Screen_Flow.md`). Tương ứng node `Dash["Supplier Dashboard (Web)"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Partnership Detail, Purchase Request List, Delivery Management, Compliance Docs, Ingredient Batches.
**Bố cục**: Admin/Dashboard Shell (`Layout_Guidelines.md` §1.1) — Sidebar cố định trái (nhóm menu: Hợp tác với Trường, Đơn Mua hàng, Giao hàng, Tuân thủ & Chứng nhận, Lô Nguyên liệu, Cá nhân — theo `11_Raw_Food_Supplier_Screen_Flow_Web.md` Sidebar 2 cấp), Topbar có breadcrumb, Content Area dùng grid 12 cột với Partner Welcome Banner full-width phía trên, 4–5 Card chức năng dạng `col-span-4/6` bên dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Supplier Dashboard, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell — Sidebar 2 cấp (Hợp tác với Trường / Đơn Mua hàng / Giao hàng / Tuân thủ & Chứng nhận / Lô Nguyên liệu / Cá nhân), Topbar breadcrumb, Content grid 12 cột.
Component bắt buộc dùng: Sidebar (shadcn sidebar block), Breadcrumb, Card, Button, Badge.
Thành phần bắt buộc: Partner Welcome Banner, 4 Card điều hướng (Purchase Request/Delivery Management/Ingredient Batches/Food Safety Compliance Record), School Partnerships card.
Áp dụng Design System: primary #0E7490, Be Vietnam Pro, shadcn new-york style — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (skeleton), empty (chưa có partnership).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (§3.3.2.5.1 Supplier Dashboard).
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md`.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.

# Screen Spec — Select School Workspace

**Actor**: Raw Food Supplier | **UC liên quan**: Không tìm thấy UC riêng trong Report 3 §3.2 hay `14_Screen_Authorization_Matrix.md` khối 5 (cột UC để trống "—") | **Status**: Draft — [Giả định, cần team xác nhận]
**BR liên quan**: [Giả định] BR-041 (trạng thái Approved/Rejected/Pending theo từng trường) — suy luận, chưa có BR tường minh cho việc 1 supplier phục vụ nhiều trường cùng lúc
**MSG liên quan**: MSG-004, MSG-005

> **[Giả định — cần team xác nhận]**: Không có UC/BR nào trong `Report 3.md` hay `business_rules.md` mô tả tường minh việc 1 Raw Food Supplier có thể có quan hệ đối tác với **nhiều hơn 1 trường** cùng lúc. Report 3 §3.3.2.5.1 (Supplier Dashboard) chỉ mô tả "Partner Welcome Banner" hiển thị **1** trường đối tác duy nhất ("their currently linked institutional school partner" — số ít). Màn hình "Select School Workspace" xuất hiện như 1 dòng riêng trong `14_Screen_Authorization_Matrix.md` khối 5, ngụ ý mô hình multi-school có thể tồn tại, nhưng không có UC/BR nào xác nhận hành vi chi tiết (làm sao dữ liệu batch/delivery/purchase request được phân tách theo từng trường). Toàn bộ field bên dưới là suy luận tối thiểu hợp lý từ tên màn hình, **không suy diễn thêm nghiệp vụ phức tạp hơn** — cần Product Owner/BA xác nhận trước khi khoá spec.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Chọn trường đang làm việc" | — | [Giả định] |
| 2 | Danh sách trường đối tác (Approved) | List/Card | Mỗi mục: tên trường, trạng thái hợp tác | BR-041 | [Giả định] Chỉ hiển thị trường có partnership Approved |
| 3 | "Chọn" Button trên mỗi mục | Button | Chọn trường làm workspace hiện tại, lưu vào session/context | — | [Giả định] |
| 4 | "Nộp đơn hợp tác trường mới" Button | Navigation | Điều hướng tới Partnership Application (07) | — | [Giả định] |
| 5 | Workspace hiện tại (chip/badge trên Topbar sau khi chọn) | Badge | Hiển thị tên trường đang active | — | [Giả định] — dùng để lọc dữ liệu Batch/Delivery/Purchase Request theo đúng trường đang chọn |

### Trạng thái màn hình
- Loading: Skeleton danh sách trường.
- Empty: Chưa có trường đối tác nào Approved → hiển thị CTA "Nộp đơn hợp tác trường mới".
- Error: MSG-004, MSG-005.
- Success: Chọn xong → chuyển thẳng Supplier Dashboard (01) với context trường đã chọn.

---

## Platform Layout — Web
**Entry/Exit points**: Vào ngay sau Login nếu supplier có ≥2 trường Approved (nếu chỉ có 1 trường, bỏ qua màn này, vào thẳng Dashboard) — [Giả định, cần xác nhận logic điều kiện hiển thị]. Không xuất hiện trong `11_Raw_Food_Supplier_Screen_Flow_Web.md` gốc — bổ sung theo tên màn hình trong Authorization Matrix.
**Bố cục**: Layout căn giữa dạng danh sách card đơn giản (tương tự Guest/Common `Layout_Guidelines.md` §1.3 nhưng có sidebar tối giản), không cần Sidebar/Topbar đầy đủ vì đây là bước chọn ngữ cảnh trước khi vào Dashboard chính.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Select School Workspace, nền tảng Web, actor Raw Food Supplier.
Bố cục: layout căn giữa, danh sách Card trường đối tác, mỗi card có nút "Chọn".
Component bắt buộc dùng: Card, Button, Badge.
Thành phần bắt buộc: danh sách trường Approved, nút chọn, nút nộp đơn hợp tác trường mới.
Áp dụng Design System: primary #0E7490, Be Vietnam Pro — xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đây là màn hình còn [Giả định], giữ tối giản.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5 (dòng "Select School Workspace").
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (Supplier Dashboard — "currently linked institutional school partner", số ít).
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
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Partnership Application (07) khi chưa có đơn hoặc muốn nộp thêm trường mới | — | |
| 4 | School Partner Card | Card | Container hiển thị thông tin quản trị/liên hệ của trường | — | Report 3 §3.3.2.5.2 |
| 5 | School Name | Text | Tên trường đối tác | — | |
| 6 | Delivery address | Text | Địa chỉ giao hàng chỉ định | — | |
| 7 | Trạng thái hợp tác (Badge) | Badge | Pending / Approved / Rejected | BR-041 | Màu theo `Design_System.md` §3 (Pending=info, Approved=success, Rejected=danger) |
| 8 | Trạng thái tài liệu tuân thủ (tóm tắt) | Badge | Valid / Expiring Soon / Expired | BR-046, BR-047 | Điều hướng tới Compliance Documents (26) |
| 9 | "Xem lịch sử phê duyệt" Button | Navigation | Điều hướng tới Application Status (08), phần lịch sử (Partnership Approval History, chỉ đọc) | — | `06_Raw_Food_Supplier_Screen_Flow.md` node `PartnerHist` |

### Trạng thái màn hình
- Loading: Skeleton School Partner Card.
- Empty: Chưa có partnership nào → CTA "Nộp đơn hợp tác" (điều hướng 07).
- Error: MSG-004, MSG-005.
- Success: N/A (màn hình hiển thị, không ghi dữ liệu trực tiếp).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "School Partnerships", hoặc từ Select School Workspace (02). Tương ứng node `PartnerD["Partnership Detail"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Partnership Application (07), Application Status (08).
**Bố cục**: Admin Shell — Sidebar nhóm "Hợp tác với Trường", Content Area 1 School Partner Card lớn (col-span-8) + panel trạng thái/tài liệu bên phải (col-span-4).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình School Partnerships, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, School Partner Card lớn bên trái, panel trạng thái hợp tác + tài liệu tuân thủ bên phải.
Component bắt buộc dùng: Card, Badge, Button, Breadcrumb.
Thành phần bắt buộc: School Name, Delivery address, Badge trạng thái hợp tác, Badge trạng thái tài liệu, nút xem lịch sử phê duyệt, nút Add/Plus.
Áp dụng Design System: primary #0E7490, badge màu theo success/warning/danger/info — xem Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty (chưa có partnership).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6201–6218 (§3.3.2.5.2 Partnership Detail).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-041, BR-046, BR-047.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md`.

# Screen Spec — Product Origin Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-102 - Upload Product Origin Documents (Report 3 §3.2, dòng 2507–2527) — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046 (yêu cầu tài liệu tuân thủ)
**MSG liên quan**: MSG-008 (vượt 10MB), MSG-009 (sai định dạng file) — theo `traceability_matrix.md` dòng 116

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Product Origin Documents" | — | |
| 2 | "Back" Button | Button | Về Supplier Dashboard / Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06), pre-select loại "Product Origin" | — | |
| 4 | Danh sách tài liệu xuất xứ (VietGAP/GlobalGAP/Organic...) | List/Card | Mỗi mục: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn (nếu có), trạng thái | BR-046 | Report 3 §3.3.2.5.5 "Product Origin Certification" Card, mở rộng thành danh sách đầy đủ |
| 5 | Trạng thái tài liệu (Badge) | Badge | Valid / Expiring Soon / Expired | BR-046 | |
| 6 | Chọn 1 tài liệu → xem chi tiết / cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Skeleton danh sách.
- Empty: Chưa có tài liệu xuất xứ nào → CTA "Tải lên tài liệu đầu tiên".
- Error: MSG-008, MSG-009 (khi upload lỗi từ màn 06).
- Success: N/A (xem/điều hướng).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Compliance Documents (26) qua card "Product Origin Certification". Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: Admin Shell — Content Area dùng Table (cột: Tên tài liệu, Loại, Ngày tải lên, Ngày hết hạn, Trạng thái, Hành động) kèm filter/search phía trên, phân trang phía dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Product Origin Documents, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table danh sách tài liệu xuất xứ với filter/search + phân trang.
Component bắt buộc dùng: Table, Badge, Button.
Thành phần bắt buộc: tên tài liệu, loại chứng nhận, ngày tải lên, ngày hết hạn, badge trạng thái, nút Add/Plus.
Áp dụng Design System: badge theo success/warning/danger — Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2258–2272 (Compliance Docs Screens §3.3.2.5.5), dòng 2507–2527 (UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 116.

# Screen Spec — Food Safety Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates (Report 3 §3.2, dòng 2485–2505) — màn hình này là danh sách/xem, hành động "Upload mới" điều hướng tới Upload Supplier Certificates (06) | **Status**: Draft
**BR liên quan**: BR-046, BR-047 (cảnh báo hết hạn 30 ngày)
**MSG liên quan**: MSG-008, MSG-009, MSG-053 (Warning — cảnh báo hết hạn) — theo `traceability_matrix.md` dòng 115

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Food Safety Certificates" | — | |
| 2 | "Back" Button | Button | Về Compliance Documents (26) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06), pre-select loại "Food Safety Certificate" | — | |
| 4 | Danh sách chứng nhận ATTP | List/Card | Mỗi mục: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, trạng thái | BR-046 | Report 3 §3.3.2.5.5 "Food Safety Certificate" Card, mở rộng thành danh sách đầy đủ |
| 5 | Trạng thái (Badge) | Badge | Valid / Expiring Soon (MSG-053, <30 ngày) / Expired | BR-047 | |
| 6 | Chọn 1 chứng nhận → xem chi tiết/cập nhật | Navigation | Điều hướng tới Update Documents (25) | — | |

### Trạng thái màn hình
- Loading: Skeleton danh sách.
- Empty: Chưa có chứng nhận ATTP nào → CTA "Tải lên chứng nhận đầu tiên" (bắt buộc trước khi được Approved theo BR-040).
- Error: MSG-008, MSG-009 (upload lỗi từ màn 06).
- Warning: MSG-053 hiển thị badge "Expiring Soon" cho chứng nhận còn <30 ngày.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Compliance Documents (26) qua card "Food Safety Certificate". Điều hướng đi: Upload Supplier Certificates (06), Update Documents (25).
**Bố cục**: Admin Shell — Table (cột: Tên chứng nhận, Cơ quan cấp, Ngày cấp, Ngày hết hạn, Trạng thái, Hành động), filter theo trạng thái ở đầu bảng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Food Safety Certificates, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table danh sách chứng nhận ATTP với filter trạng thái.
Component bắt buộc dùng: Table, Badge, Button.
Thành phần bắt buộc: tên chứng nhận, cơ quan cấp, ngày cấp, ngày hết hạn, badge trạng thái.
Áp dụng Design System: badge warning cho "Expiring Soon" (MSG-053).
Trạng thái cần thể hiện: loading, empty, warning (expiring soon).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272 (§3.3.2.5.5), dòng 2485–2505 (UC-101).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 115.

# Screen Spec — Upload Supplier Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-101 - Upload Food Safety Certificates, UC-102 - Upload Product Origin Documents (form dùng chung cho cả 2 UC, phân biệt bằng dropdown phân loại tài liệu) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-008, MSG-009, MSG-013 (chỉ áp dụng khi loại tài liệu = Food Safety Certificate và ngày hết hạn đã qua — theo UC-101 AF1)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload documents" | — | Report 3 §3.3.2.5.7 |
| 2 | "Back" Button | Button | Về màn danh sách gọi tới (04 hoặc 05) | — | |
| 3 | "Cancel" Button | Button | Huỷ, không lưu | — | |
| 4 | Section Header — "Upload New Profile" | Label | — | — | Report 3 §3.3.2.5.7 |
| 5 | Classification of compliance documents (dropdown) | Select | Bắt buộc — chọn "Food Safety Certificate" (UC-101) hoặc "Product Origin Document" (VietGAP/GlobalGAP/Organic...) (UC-102) | — | Quyết định luồng validation phía sau |
| 6 | Document Classification Text (hiển thị loại đã chọn) | Text | — | — | |
| 7 | Tên tài liệu / Cơ quan cấp | Input text | Bắt buộc | BR-046 | |
| 8 | File tài liệu | File upload | Bắt buộc, PDF/JPG/PNG, ≤10MB (MSG-008/009) | BR-046 | |
| 9 | Expiration date of the document | Date picker | Bắt buộc nếu loại = Food Safety Certificate; tuỳ chọn nếu Product Origin Document không có hạn | BR-047, BR-048 | Nếu ngày đã qua và loại = Food Safety Certificate → chặn submit, hiển thị MSG-013 (BR-048) |
| 10 | "Add / Plus" Icon | Button | Thêm 1 tài liệu khác trong cùng phiên upload | — | |
| 11 | "Submit"/"Save" Button | Button | Validate toàn bộ trước khi lưu | BR-046, BR-047, BR-048 | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: MSG-008 (file >10MB), MSG-009 (sai định dạng), MSG-013 (chứng nhận ATTP hết hạn, chặn upload theo BR-048).
- Warning: Nếu ngày hết hạn còn <30 ngày → hiển thị cảnh báo "sẽ sớm hết hạn" (BR-047) nhưng vẫn cho phép lưu.
- Success: Lưu thành công → quay về màn danh sách gọi tới (04/05/26), MSG-029 (cập nhật hồ sơ compliance thành công, dùng theo mẫu chung của UC-091/167).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26) qua "Add / Plus" Icon. Tương ứng node `UploadCert["Upload Certificates Form"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`.
**Bố cục**: Form ngắn hiện trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2, form <8 field) thay vì trang riêng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Supplier Certificates, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn, không phải trang riêng.
Component bắt buộc dùng: Dialog, Select, Input, DatePicker, file upload + preview, Button.
Thành phần bắt buộc: dropdown phân loại tài liệu, tên tài liệu/cơ quan cấp, file upload, ngày hết hạn, nút Submit/Cancel/Add.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-008/009/013), warning (sắp hết hạn), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6293–6309 (§3.3.2.5.7 Supplier Upload Certificates).
- `06_Document/01_Report/Report 3.md` dòng 2485–2527 (UC-101, UC-102).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-008, MSG-009, MSG-013.

# Screen Spec — Partnership Application

**Actor**: Raw Food Supplier | **UC liên quan**: UC-152 - Submit Supplier Application (Report 3 §3.2, dòng 2639–2658); liên quan UC-155 (Manage Supplier Application, dòng 2699–2718) và UC-170 (Update/Resubmit Supplier Application, dòng 2679–2697) khi nộp lại đơn bị từ chối | **Status**: Draft
**BR liên quan**: BR-040 (quy trình đăng ký nhà cung cấp), BR-046 (tài liệu tuân thủ), BR-047 (cảnh báo hết hạn), BR-048 (từ chối + lý do), BR-010 (xác minh email)
**MSG liên quan**: MSG-004, MSG-005, MSG-007 (sai định dạng mã số thuế), MSG-008 (vượt 10MB), MSG-009 (sai định dạng file), MSG-013 (chứng nhận hết hạn, chặn nộp — BR-048), MSG-025 (lỗi giao dịch DB), MSG-028 (Success — nộp đăng ký thành công)

> **Ghi chú đối chiếu nguồn**: `traceability_matrix.md` dòng 99 ánh xạ UC-152 với BR-040, BR-046, BR-050. BR-050 (đăng ký nhân viên giao hàng) không liên quan trực tiếp tới việc nộp đơn hợp tác công ty — nhiều khả năng là sai sót trong traceability matrix (nên là BR-047/BR-048 như Normal Flow/Alternative Flow của chính UC-152 mô tả). Bảng dưới đây dùng bộ BR theo đúng Normal Flow/Alternative Flow của UC-152 trong Report 3 (nguồn field/hành vi chính theo mục 3 của kế hoạch phân công), có ghi chú BR-050 riêng để team xác nhận lại traceability_matrix.md.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Apply for Onboarding" | — | UC-152 Normal Flow bước 1 |
| 2 | Company Name | Input text | Bắt buộc | BR-040 | |
| 3 | Tax ID | Input text | Bắt buộc, đúng định dạng mã số thuế (MSG-007 nếu sai) | BR-040 | |
| 4 | Business Address | Input text | Bắt buộc | BR-040 | |
| 5 | Business License (file) | File upload | Bắt buộc, PDF/JPG/PNG, ≤10MB (MSG-008/009) | BR-040 | |
| 6 | Food Safety Certificate(s) (file, có thể nhiều) | File upload (multi) | Bắt buộc ≥1, không được hết hạn (BR-048, MSG-013 nếu hết hạn) | BR-046, BR-047, BR-048 | |
| 7 | Ngày hết hạn từng chứng nhận | Date picker | Bắt buộc theo mỗi file chứng nhận | BR-047 | |
| 8 | "Submit" Button | Button | Validate toàn bộ field trước khi gửi | BR-040, BR-046, BR-047, BR-048 | Sau khi Submit: trạng thái 'Pending Approval', gửi email xác nhận (BR-010) |
| 9 | "Cancel" Button | Button | Huỷ, quay về School Partnerships (03) | — | |

### Trạng thái màn hình
- Loading: Skeleton form khi tải lại dữ liệu đơn cũ (trường hợp Update/Resubmit UC-170).
- Empty: N/A (form nhập mới).
- Error: MSG-007 (sai mã số thuế), MSG-008 (file >10MB), MSG-009 (sai định dạng file), MSG-013 (chứng nhận hết hạn — chặn Submit theo BR-048), MSG-025 (lỗi DB).
- Success: MSG-028 "Registration request submitted! A verification email has been sent." → điều hướng Application Status (08).

---

## Platform Layout — Web
**Entry/Exit points**: Từ School Partnerships (03) qua "Add / Plus" Icon, hoặc từ Supplier Dashboard nếu chưa có partnership. Tương ứng node `PartnerApp["Supplier Partnership Application Form"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Application Status (08).
**Bố cục**: Form dài chia nhiều bước dùng Stepper ngang (`Layout_Guidelines.md` §1.2) — Bước 1: Thông tin công ty (Company Name, Tax ID, Address); Bước 2: Tải lên tài liệu (Business License, Food Safety Certificate + ngày hết hạn). Nút Submit cố định cuối bước 2.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Partnership Application, nền tảng Web, actor Raw Food Supplier.
Bố cục: Stepper ngang 2 bước — Bước 1 Thông tin công ty, Bước 2 Tải tài liệu tuân thủ.
Component bắt buộc dùng: Stepper, Input, DatePicker, file upload input + preview thumbnail, Button.
Thành phần bắt buộc: Company Name, Tax ID, Business Address, Business License upload, Food Safety Certificate upload (multi) + ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490, Be Vietnam Pro — xem Design_System.md.
Trạng thái cần thể hiện: error (MSG-007/008/009/013), success (MSG-028).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2639–2658 (UC-152 Submit Supplier Application).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-010, BR-040, BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-007, MSG-008, MSG-009, MSG-013, MSG-025, MSG-028.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 99 (đối chiếu BR — xem ghi chú lệch ở trên).

# Screen Spec — Application Status

**Actor**: Raw Food Supplier | **UC liên quan**: UC-169 - Track Supplier Application Status (Report 3 §3.2, dòng 2659–2677); liên quan UC-170 - Update/Resubmit Supplier Application (dòng 2679–2697) và UC-155 - Manage Supplier Application (dòng 2699–2718, dashboard điều phối) | **Status**: Draft
**BR liên quan**: BR-041 (trạng thái Pending/Approved/Rejected). Report 3 UC-169 ghi "Business Rules: None"; traceability_matrix.md dòng 100 bổ sung BR-041 — dùng BR-041 vì khớp trực tiếp với "trạng thái" hiển thị trên màn hình
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (Success — cập nhật/resubmit thành công, dùng cho UC-170)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Application Status" | — | UC-169 Normal Flow bước 1 |
| 2 | Trạng thái đơn (Badge) | Badge | Pending / Approved / Rejected / Action Required | BR-041 | Màu theo `Design_System.md` §3 |
| 3 | Bình luận/Lý do từ School Administration | Text block | Hiển thị khi Rejected/Action Required (BR-048 — lý do từ chối) | BR-048 | |
| 4 | "Edit/Update Application" Button | Button | Chỉ hiện khi trạng thái Rejected hoặc Action Required | — | Điều hướng tới form pre-filled (UC-170, dùng lại layout Partnership Application 07) |
| 5 | Ngày nộp / Ngày cập nhật gần nhất | Text | — | — | |
| 6 | "Xem lịch sử phê duyệt" (Partnership Approval History, read-only) | List | Danh sách các lần nộp/duyệt/từ chối trước đó | — | `06_Raw_Food_Supplier_Screen_Flow.md` node `PartnerHist` |

### Trạng thái màn hình
- Loading: Skeleton badge + text block.
- Empty: Chưa từng nộp đơn nào → điều hướng ngược về Partnership Application (07) (theo UC-155 Normal Flow bước 3: "Submit Application" nếu chưa có đơn).
- Error: MSG-004, MSG-005.
- Success: MSG-029 sau khi resubmit thành công (UC-170).

---

## Platform Layout — Web
**Entry/Exit points**: Từ School Partnerships (03) qua "Xem lịch sử phê duyệt", hoặc trực tiếp sau khi Submit ở Partnership Application (07). Điều hướng đi: Partnership Application (07) ở chế độ Edit/Resubmit (UC-170).
**Bố cục**: Admin Shell — Content Area 1 khối trạng thái lớn (Badge + bình luận) phía trên, danh sách lịch sử phê duyệt dạng Table bên dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Application Status, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, khối trạng thái (Badge lớn + bình luận) phía trên, Table lịch sử phê duyệt bên dưới.
Component bắt buộc dùng: Badge, Alert, Table, Button.
Thành phần bắt buộc: Badge trạng thái, bình luận từ School Admin, nút Edit/Update Application (điều kiện), lịch sử phê duyệt.
Áp dụng Design System: badge theo success/warning/danger/info — Design_System.md mục 3.
Trạng thái cần thể hiện: error, success (MSG-029), empty (chưa từng nộp đơn).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2659–2718 (UC-169, UC-170, UC-155).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-041, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 100–102.

# Screen Spec — Purchase Requests

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-139 (View Purchase Requests) | **Status**: Draft
**BR liên quan**: Không áp dụng trực tiếp (BR áp dụng ở màn con Purchase Request List/Detail)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | Report 3 §3.3.2.5.3 |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái (Pending/Confirmed/Delivered) | Badge/Counter | Tổng hợp số lượng theo trạng thái | — | Bổ sung nhẹ so với Report 3 gốc để tăng khả năng điều hướng nhanh — [Giả định — cần team xác nhận có cần hay không] |
| 4 | Điều hướng tới Purchase Request List | Navigation | Điều hướng tới màn 10 | — | |

### Trạng thái màn hình
- Loading: Skeleton bộ đếm.
- Empty: N/A (chuyển hẳn empty state cho màn 10).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Purchase Request". Điều hướng đi: Purchase Request List (10).
**Bố cục**: Admin Shell — Content Area hiển thị bộ đếm nhanh dạng Card nhỏ (col-span-3/4 mỗi card), bên dưới là bảng tóm tắt dẫn thẳng vào màn 10 (hoặc redirect thẳng nếu không cần màn trung gian riêng biệt về UI).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Requests (entry), nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, các Card đếm nhanh theo trạng thái đơn mua hàng.
Component bắt buộc dùng: Card, Badge, Button.
Thành phần bắt buộc: bộ đếm theo trạng thái, điều hướng tới Purchase Request List.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237 (§3.3.2.5.3 Purchase Request Screen).
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.

# Screen Spec — Purchase Request List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-139 - View Purchase Requests (Report 3 §3.2, dòng 3419–3439) | **Status**: Draft
**BR liên quan**: BR-077 (cảnh báo thiếu kho — theo Report 3 UC-139 "Business Rules: BR-077"; `traceability_matrix.md` dòng 148 để trống BR — giữ lại BR-077 vì logic hợp lý: đơn mua hàng phát sinh từ cảnh báo thiếu kho phía Service Caterers, hiển thị ngữ cảnh cho supplier)
**MSG liên quan**: MSG-004, MSG-005; MSG-018 (Report 3 Exceptions liệt kê nhưng khó khớp ngữ cảnh xem-danh-sách của Supplier — [cần team xác nhận], không hiển thị mặc định)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Material Order" | — | Report 3 §3.3.2.5.3 |
| 2 | "Back" Button | Button | Về Purchase Requests (09) | — | |
| 3 | Section Header — "Purchase order list" | Label | — | — | |
| 4 | Purchase Order Card (mỗi dòng) | Card/Row | Container tóm tắt 1 đơn | — | |
| 5 | Order code | Text | Mã đơn hàng duy nhất | — | |
| 6 | Status Badge | Badge | "Awaiting confirmation" / "Delivery completed" / khác theo vòng đời đơn | — | Màu theo `Design_System.md` §3 |
| 7 | Source of request | Text | Vai trò/nguồn gốc yêu cầu (VD "Springfield Head Chef") | — | |
| 8 | Delivery date required | Text | Ngày/giờ giao hàng yêu cầu | — | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã đơn | — | Chuẩn hoá theo `Component_Guidelines.md` DataTable pattern |
| 10 | Chọn 1 dòng → xem chi tiết | Navigation | Điều hướng tới Purchase Request Detail (11) | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có đơn mua hàng nào → empty state "Chưa có đơn đặt hàng nào từ trường đối tác".
- Error: MSG-004, MSG-005.
- Success: N/A (màn hình chỉ xem).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Purchase Requests (09) hoặc trực tiếp từ Supplier Dashboard (01). Tương ứng node `PRList["Purchase Request Screen"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Purchase Request Detail (11).
**Bố cục**: Admin Shell — `Table`/`DataTable` (cột: Mã đơn, Nguồn yêu cầu, Ngày giao yêu cầu, Trạng thái) kèm filter/search phía trên, phân trang phía dưới (theo `Component_Guidelines.md` §1 "Bảng dữ liệu").

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request List, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter/search phía trên, phân trang phía dưới.
Component bắt buộc dùng: Table, Badge, Input (search), Select (filter).
Thành phần bắt buộc: Order code, Status Badge, Source of request, Delivery date required.
Áp dụng Design System: badge theo trạng thái (Design_System.md mục 3).
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6220–6237 (§3.3.2.5.3), dòng 3419–3439 (UC-139).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-077.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 148.

# Screen Spec — Purchase Request Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-139 - View Purchase Requests; hành động Accept dẫn sang UC-015 - Confirm Delivery Schedule | **Status**: Draft
**BR liên quan**: BR-077 (bối cảnh thiếu kho), BR-071 (chứng nhận lô hàng — áp dụng khi Accept và điều hướng sang lập lịch giao hàng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã đơn hàng | — | Report 3 §3.3.2.5.4 |
| 2 | "Back" Button (Top Bar) | Button | Về Purchase Request List (10) | — | |
| 3 | "Come back" Button | Button | Huỷ xem chi tiết, về danh sách | — | |
| 4 | Order number | Text | Mã định danh đơn hàng | — | |
| 5 | Recipient | Text | Trường/canteen nhận hàng | — | |
| 6 | Delivery deadline | Text | Ngày giao hạn chót + yêu cầu vận hành quan trọng | — | |
| 7 | List of ingredients to order (Section Header) | Label | — | — | |
| 8 | Ingredients List Container | List | Tên nguyên liệu, khối lượng yêu cầu, mã lô/batch dự kiến | BR-071 | |
| 9 | "Accept" Button | Button | Chấp nhận đơn → điều hướng Confirm Delivery Schedule (22, UC-015) | — | ScreenFlow node `PRResponse["Confirm Quantity & Delivery Date"]` |
| 10 | "Reject" Button | Button | Từ chối đơn, yêu cầu nhập lý do | — | [Giả định — Report 3 không mô tả field lý do từ chối chi tiết cho hành động này, chỉ có trong ScreenFlow "Accept/Reject" — cần team xác nhận có bắt buộc nhập lý do hay không] |

### Trạng thái màn hình
- Loading: Skeleton chi tiết đơn.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: Sau khi Accept → điều hướng Confirm Delivery Schedule (22); sau khi Reject → quay về danh sách (10) với trạng thái cập nhật.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Purchase Request List (10) khi chọn 1 dòng. Tương ứng node `PRDetail["Purchase Request Detail"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Confirm Delivery Schedule (22).
**Bố cục**: Admin Shell — Content Area 2 cột: thông tin đơn (Order number, Recipient, Delivery deadline) bên trái (col-span-4), danh sách nguyên liệu dạng Table bên phải (col-span-8); nút Accept/Reject cố định cuối trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Purchase Request Detail, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 cột — thông tin đơn bên trái, Table nguyên liệu bên phải, nút Accept/Reject cố định cuối trang.
Component bắt buộc dùng: Table, Button, Card.
Thành phần bắt buộc: Order number, Recipient, Delivery deadline, danh sách nguyên liệu, nút Accept/Reject.
Áp dụng Design System: primary #0E7490, destructive variant cho Reject.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6239–6256 (§3.3.2.5.4 Purchase Request Screen Detail).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).

# Screen Spec — Ingredient Batches

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-043 (Manage Ingredient Batches) và các UC con | **Status**: Draft
**BR liên quan**: BR-071 (chứng nhận lô hàng bắt buộc)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | Report 3 §3.3.2.5.6 |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Bộ đếm nhanh theo trạng thái lô (Valid/Expiring/Expired/Quarantine) | Badge/Counter | — | BR-071 | [Giả định nhẹ — mở rộng so với Report 3 gốc để định hướng nhanh, không đổi field cốt lõi] |
| 5 | Điều hướng tới Batch List | Navigation | Điều hướng tới màn 13 | — | |

### Trạng thái màn hình
- Loading: Skeleton bộ đếm.
- Empty: N/A (chuyển cho màn 13).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Ingredient Batches". Điều hướng đi: Batch List (13), Submit Batch Information (15).
**Bố cục**: Admin Shell — Card đếm nhanh theo trạng thái phía trên, dẫn thẳng vào Batch List (13) bên dưới cùng trang hoặc qua điều hướng.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Ingredient Batches (entry), nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Card đếm nhanh theo trạng thái lô.
Component bắt buộc dùng: Card, Badge, Button.
Thành phần bắt buộc: bộ đếm theo trạng thái, nút Add/Plus.
Áp dụng Design System: badge theo success/warning/danger/critical.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291 (§3.3.2.5.6 Ingredient Batches Screens).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.

# Screen Spec — Batch List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-043 - Manage Ingredient Batches (Report 3 §3.2, dòng 3089–3109) | **Status**: Draft
**BR liên quan**: BR-071 (chứng nhận lô hàng bắt buộc: số lô, ngày sản xuất, hạn sử dụng, số lượng, chứng chỉ)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Raw Material Batch" | — | Report 3 §3.3.2.5.6 |
| 2 | "Back" Button | Button | Về Ingredient Batches (12) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Batch Information (15) | — | |
| 4 | Section Header — "List of outgoing shipments" | Label | — | — | |
| 5 | Material Batch Card (mỗi dòng) | Card/Row | Container tóm tắt 1 lô | BR-071 | |
| 6 | Batch Name & Code | Text | Tên nguyên liệu + mã lô | BR-071 | |
| 7 | Status Badge | Badge | "Quarantine passed" / "VietGAP achieved" / khác | BR-071 | Màu theo `Design_System.md` §3 |
| 8 | Packaging / Harvest Date | Text | Ngày đóng gói/thu hoạch | BR-071 | |
| 9 | Filter/Search | Input | Lọc theo trạng thái, tìm theo mã lô | — | |
| 10 | Chọn 1 dòng → xem chi tiết | Navigation | Điều hướng tới Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lô nào đăng ký → CTA "Đăng ký lô đầu tiên" (điều hướng 15).
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Ingredient Batches (12). Tương ứng node `BatchList["Ingredient Batches List"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Batch Detail (14), Submit Batch Information (15).
**Bố cục**: Admin Shell — `Table` (cột: Tên/Mã lô, Trạng thái, Ngày đóng gói) kèm filter/search, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch List, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter/search + phân trang.
Component bắt buộc dùng: Table, Badge, Input, Button.
Thành phần bắt buộc: Batch Name & Code, Status Badge, Packaging/Harvest Date.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291 (§3.3.2.5.6), dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`.

# Screen Spec — Batch Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-043 - Manage Ingredient Batches; điều hướng tới UC-100 (Upload Batch Certificates), UC-107 (View Batch History) | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên/Mã lô | — | Mở rộng từ Report 3 §3.3.2.5.6 (Material Batch Card) thành màn chi tiết đầy đủ |
| 2 | "Back" Button | Button | Về Batch List (13) | — | |
| 3 | Product Name | Text | Tên nguyên liệu | BR-071 | |
| 4 | Lot Number | Text | Số lô | BR-071 | |
| 5 | Packaging Date | Text | Ngày đóng gói | BR-071 | |
| 6 | Expiration Date | Text | Hạn sử dụng | BR-071 | |
| 7 | Status Badge | Badge | Quarantine passed / VietGAP achieved / khác | BR-071 | |
| 8 | Chứng chỉ liên kết (danh sách) | List | Liên kết tới các chứng chỉ đã upload cho lô này | BR-071 | Điều hướng tới Upload Batch Certificates (16) |
| 9 | "Upload Batch Certificates" Button | Navigation | Điều hướng tới màn 16 | — | UC-100 |
| 10 | "View Batch History" Button | Navigation | Điều hướng tới màn 17 | — | UC-107 |
| 11 | "Edit" Button | Navigation | Điều hướng tới Submit Batch Information (15) ở chế độ chỉnh sửa | BR-071 | UC-043 Normal Flow bước 3 "edit manufacturing logs" |

### Trạng thái màn hình
- Loading: Skeleton chi tiết lô.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A (màn xem, hành động ghi dữ liệu ở màn con).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Batch List (13) khi chọn 1 dòng. Tương ứng node `BatchDetail["Ingredient Batch Detail"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Upload Batch Certificates (16), View Batch History (17), Submit Batch Information (15, edit mode).
**Bố cục**: Admin Shell — Content Area 2 cột: thông tin lô (Product Name, Lot Number, Packaging/Expiration Date, Status) bên trái (col-span-5), danh sách chứng chỉ liên kết + action buttons bên phải (col-span-7).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Batch Detail, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 cột — thông tin lô bên trái, chứng chỉ liên kết + action bên phải.
Component bắt buộc dùng: Card, Badge, Table (chứng chỉ), Button.
Thành phần bắt buộc: Product Name, Lot Number, Packaging Date, Expiration Date, Status Badge, chứng chỉ liên kết, nút Upload Batch Certificates/View Batch History/Edit.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6274–6291 (§3.3.2.5.6), dòng 3089–3109 (UC-043).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (node `BatchDetail`).

# Screen Spec — Submit Batch Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-081 - Submit Batch Information (Report 3 §3.2, dòng 3265–3285) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048, BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-013 (chặn nếu chứng nhận supplier đã hết hạn theo BR-048)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Batch Information" | — | UC-081 Normal Flow bước 1 |
| 2 | Product Name | Input text | Bắt buộc | BR-071 | |
| 3 | Lot Number | Input text | Bắt buộc, duy nhất | BR-071 | |
| 4 | Manufacturing Date | Date picker | Bắt buộc | BR-071 | |
| 5 | Packaging Date | Date picker | Bắt buộc | BR-071 | |
| 6 | Expiration Date | Date picker | Bắt buộc, phải sau Packaging Date | BR-071 | |
| 7 | Liên kết chứng chỉ an toàn (safety certificate links) | Select/Multi-select | Chọn từ danh sách chứng chỉ đã có ở Compliance Documents (26) | BR-046 | Nếu supplier có chứng chỉ hết hạn (BR-048) → chặn submit, MSG-013 |
| 8 | "Submit" Button | Button | Validate ngày tháng + chứng chỉ hợp lệ | BR-046, BR-047, BR-048, BR-071 | |
| 9 | "Cancel" Button | Button | Huỷ, về Batch List (13)/Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Skeleton form (khi ở chế độ Edit từ Batch Detail 14).
- Empty: N/A.
- Error: MSG-013 (chứng nhận supplier hết hạn, chặn theo BR-048).
- Success: Đăng ký lô thành công → điều hướng Batch Detail (14) của lô vừa tạo.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Ingredient Batches (12) hoặc Batch List (13) qua "Add / Plus" Icon, hoặc từ Batch Detail (14) qua "Edit". Điều hướng đi: Batch Detail (14).
**Bố cục**: Form ngắn hiện trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2, <8 field).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Batch Information, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn.
Component bắt buộc dùng: Dialog, Input, DatePicker, Select (multi), Button.
Thành phần bắt buộc: Product Name, Lot Number, Manufacturing/Packaging/Expiration Date, liên kết chứng chỉ, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-013), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3265–3285 (UC-081).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048, BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-013.

# Screen Spec — Upload Batch Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-100 - Upload Batch Certificates (Report 3 §3.2, dòng 3331–3351) — extends UC-043 (Manage Ingredient Batches) | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-009 (Unsupported file type)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload Batch Certificates" | — | UC-100 Normal Flow bước 1 |
| 2 | Lô hàng đang chọn (chỉ đọc) | Text/Badge | Tên/mã lô kế thừa từ Batch Detail (14) | BR-071 | |
| 3 | File chứng chỉ (PDF/image) | File upload | Bắt buộc, PDF/JPG/PNG, ≤10MB | BR-071 | MSG-009 nếu sai định dạng |
| 4 | Ngày hết hạn chứng chỉ | Date picker | Bắt buộc | BR-071 | |
| 5 | "Submit"/"Save" Button | Button | Validate định dạng + liên kết chứng chỉ vào lô | BR-071 | |
| 6 | "Cancel" Button | Button | Huỷ, về Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: MSG-009.
- Success: Liên kết chứng chỉ vào lô thành công → về Batch Detail (14), cập nhật danh sách chứng chỉ liên kết.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Batch Detail (14) qua nút "Upload Batch Certificates". Điều hướng đi: quay lại Batch Detail (14).
**Bố cục**: Form ngắn trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Batch Certificates, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn.
Component bắt buộc dùng: Dialog, file upload + preview, DatePicker, Button.
Thành phần bắt buộc: lô hàng đang chọn (chỉ đọc), file chứng chỉ, ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-009), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3331–3351 (UC-100).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-009.

# Screen Spec — View Batch History

**Actor**: Raw Food Supplier | **UC liên quan**: UC-107 - View Batch History (Report 3 §3.2, dòng 3353–3373) — extends UC-043 (Manage Ingredient Batches) | **Status**: Draft
**BR liên quan**: BR-071 (theo Report 3 UC-107 "Business Rules: BR-071"); `traceability_matrix.md` dòng 145 ghi BR-076 (Truy xuất lô nguyên liệu) — cả 2 đều hợp lý ở góc độ khác nhau (BR-071 = nội dung chứng nhận lô, BR-076 = mục đích truy xuất nguồn gốc của màn xem lịch sử này), giữ cả 2
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Batch History" | — | UC-107 Normal Flow bước 1 |
| 2 | "Back" Button | Button | Về Batch Detail (14) | — | |
| 3 | Danh sách toàn bộ lô đã đăng ký | Table/List | Lot Number, Packaging Date, Expiration Date, Certificate Links | BR-071, BR-076 | |
| 4 | Search theo Lot Number | Input | — | — | |
| 5 | Filter theo Expiration Status | Select | Valid / Expiring Soon / Expired | BR-047 | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lô nào trong lịch sử.
- Error: MSG-004, MSG-005.
- Success: N/A (màn xem, chỉ đọc).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Batch Detail (14) qua nút "View Batch History". Điều hướng đi: N/A (màn cuối nhánh, chỉ đọc).
**Bố cục**: Admin Shell — `Table` đầy đủ (Lot Number, Packaging Date, Expiration Date, Certificate Links) kèm search/filter, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Batch History, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table lịch sử lô với search/filter + phân trang.
Component bắt buộc dùng: Table, Input (search), Select (filter), Badge.
Thành phần bắt buộc: Lot Number, Packaging Date, Expiration Date, Certificate Links.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3353–3373 (UC-107).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-076.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 145.

# Screen Spec — Deliveries

**Actor**: Raw Food Supplier | **UC liên quan**: Không có UC riêng — màn hình entry/nhóm chức năng, điều hướng tới UC-039 (Manage Deliveries) và các UC con | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Management" | — | Report 3 §3.3.2.5.1 (nút Dashboard) |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | Bộ đếm nhanh theo trạng thái (Scheduled/In Transit/Delivered/Delayed) | Badge/Counter | — | BR-071 | [Giả định nhẹ — mở rộng, không đổi field cốt lõi] |
| 4 | Điều hướng tới Delivery List | Navigation | Điều hướng tới màn 19 | — | |

### Trạng thái màn hình
- Loading: Skeleton bộ đếm.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Delivery Management". Điều hướng đi: Delivery List (19), Submit Delivery Information (21).
**Bố cục**: Admin Shell — Card đếm nhanh theo trạng thái phía trên, dẫn vào Delivery List (19).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Deliveries (entry), nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Card đếm nhanh theo trạng thái giao hàng.
Component bắt buộc dùng: Card, Badge, Button.
Thành phần bắt buộc: bộ đếm theo trạng thái.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6182–6199 (Supplier Dashboard, nút "Delivery Management").
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/14_Screen_Authorization_Matrix.md` khối 5.

# Screen Spec — Delivery List

**Actor**: Raw Food Supplier | **UC liên quan**: UC-039 - Manage Deliveries (Report 3 §3.2, dòng 3067–3087) | **Status**: Draft
**BR liên quan**: BR-071 (Linked Batches)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Deliveries" | — | UC-039 Normal Flow bước 1–2 |
| 2 | "Back" Button | Button | Về Deliveries (18) | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Submit Delivery Information (21) | — | |
| 4 | Delivery Card (mỗi dòng) | Card/Row | Vehicle, Driver, Linked Batches, Schedule Status | BR-071 | UC-039 Normal Flow bước 2 |
| 5 | Filter theo ngày/trạng thái | Input/Select | Filter by date, status | — | |
| 6 | Search theo Delivery ID | Input | — | — | |
| 7 | Chọn 1 dòng → xem chi tiết | Navigation | Điều hướng tới Delivery Detail (20) | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lịch giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Deliveries (18). Tương ứng node `DelMgmt["Delivery Management"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Delivery Detail (20), Submit Delivery Information (21).
**Bố cục**: Admin Shell — `Table` (cột: Vehicle, Driver, Linked Batches, Schedule Status) kèm filter theo ngày/trạng thái, search theo Delivery ID, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery List, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter ngày/trạng thái + search + phân trang.
Component bắt buộc dùng: Table, Badge, Input, Select, Button.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`.

# Screen Spec — Delivery Detail

**Actor**: Raw Food Supplier | **UC liên quan**: Bundled trong UC-039 - Manage Deliveries; điều hướng tới UC-092 (Update Delivery Status), UC-109 (View Delivery Batch Records) | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng (Delivery ID) | — | Mở rộng từ UC-039 Normal Flow bước 2 |
| 2 | "Back" Button | Button | Về Delivery List (19) | — | |
| 3 | Vehicle (biển số xe) | Text | — | BR-071 | |
| 4 | Driver (tên nhân viên giao hàng) | Text | — | BR-050 (đăng ký nhân viên giao hàng — dữ liệu tham chiếu, không chỉnh sửa ở màn này) | |
| 5 | Linked Batches | List | Danh sách lô nguyên liệu gắn với chuyến giao này | BR-071 | Điều hướng tới View Delivery Batch Records (24) |
| 6 | Schedule Status (Badge) | Badge | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071, BR-135 | |
| 7 | "Edit" Button | Navigation | Chỉnh sửa driver/vehicle nếu đang Pending — điều hướng Submit Delivery Information (21) chế độ Edit | BR-071 | UC-039 Normal Flow bước 4 |
| 8 | "Cancel Delivery" Button | Button | Chỉ khả dụng nếu trạng thái đang Pending | BR-071 | UC-039 Normal Flow bước 4 |
| 9 | "Update Status" Button | Navigation | Điều hướng tới Update Delivery Status (23) | — | UC-092 |
| 10 | "View Batch Records" Button | Navigation | Điều hướng tới View Delivery Batch Records (24) | — | UC-109 |

### Trạng thái màn hình
- Loading: Skeleton chi tiết chuyến giao hàng.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Success: N/A (hành động ghi dữ liệu ở màn con 21/23).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Delivery List (19) khi chọn 1 dòng. Điều hướng đi: Submit Delivery Information (21, edit), Update Delivery Status (23), View Delivery Batch Records (24).
**Bố cục**: Admin Shell — Content Area 2 cột: thông tin chuyến giao (Vehicle, Driver, Schedule Status) bên trái (col-span-5), danh sách Linked Batches bên phải (col-span-7); action buttons cố định cuối trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Delivery Detail, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 cột — thông tin chuyến giao bên trái, danh sách lô liên kết bên phải, action buttons cuối trang.
Component bắt buộc dùng: Card, Badge, Table (batch), Button.
Thành phần bắt buộc: Vehicle, Driver, Linked Batches, Schedule Status, nút Edit/Cancel/Update Status/View Batch Records.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3067–3087 (UC-039).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071, BR-135.

# Screen Spec — Submit Delivery Information

**Actor**: Raw Food Supplier | **UC liên quan**: UC-083 - Submit Delivery Information (Report 3 §3.2, dòng 3287–3307) | **Status**: Draft
**BR liên quan**: BR-071 (chứng nhận lô hàng bắt buộc — nộp trước tối thiểu 24 giờ)
**MSG liên quan**: MSG-004, MSG-005 (chặn nếu nộp <24 giờ trước giờ giao dự kiến, theo BR-071)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Submit Delivery Information" | — | UC-083 Normal Flow bước 1 |
| 2 | Purchase Request đích (chọn từ danh sách đã Accept) | Select | Bắt buộc, chỉ hiện các PR đã Accept | — | Liên kết tới Purchase Request Detail (11) |
| 3 | Dispatch Date | Date picker | Bắt buộc, phải ≥24 giờ trước thời điểm giao dự kiến | BR-071 | Nếu <24 giờ → chặn submit |
| 4 | Vehicle License Plate | Input text | Bắt buộc | BR-071 | |
| 5 | Driver Details (chọn từ Delivery Person đã đăng ký) | Select | Bắt buộc | BR-050 | Tham chiếu danh sách Delivery Person (ngoài phạm vi 26 màn RFS — quản lý ở UC-019/UC-068, không có màn riêng trong Authorization Matrix khối 5) |
| 6 | Liên kết lô nguyên liệu (Linked Batches) | Multi-select | Bắt buộc ≥1, chọn từ Batch List (13) | BR-071 | |
| 7 | "Submit" Button | Button | Validate thời gian ≥24h + đầy đủ field | BR-071 | |
| 8 | "Cancel" Button | Button | Huỷ, về Deliveries (18)/Delivery List (19) | — | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: Chặn submit nếu nộp <24 giờ trước giờ giao (BR-071) — hiển thị cảnh báo trực tiếp trên field Dispatch Date.
- Success: Trạng thái chuyển 'Scheduled', Service Caterers được thông báo → điều hướng Delivery Detail (20) của chuyến vừa tạo.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Deliveries (18)/Delivery List (19) qua "Add / Plus" Icon. Điều hướng đi: Delivery Detail (20).
**Bố cục**: Form dài chia bước dùng Stepper ngang (`Layout_Guidelines.md` §1.2) — Bước 1: chọn Purchase Request + Dispatch Date; Bước 2: Vehicle/Driver; Bước 3: Liên kết lô nguyên liệu.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Submit Delivery Information, nền tảng Web, actor Raw Food Supplier.
Bố cục: Stepper ngang 3 bước — chọn Purchase Request/Dispatch Date, Vehicle/Driver, liên kết lô nguyên liệu.
Component bắt buộc dùng: Stepper, Select, DatePicker, Input, Button.
Thành phần bắt buộc: Purchase Request đích, Dispatch Date, Vehicle License Plate, Driver, liên kết lô nguyên liệu, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (nộp <24h), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3287–3307 (UC-083).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.

# Screen Spec — Confirm Delivery Schedule

**Actor**: Raw Food Supplier | **UC liên quan**: UC-015 - Confirm Delivery Schedule (Report 3 §3.2, dòng 3023–3043) | **Status**: Draft
**BR liên quan**: BR-050 (xác thực sinh trắc học Delivery Person phải tồn tại), BR-071 (nộp lịch ≥24 giờ trước dispatch)
**MSG liên quan**: MSG-004, MSG-005 (chặn nếu <24 giờ theo BR-071)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Confirm Delivery Schedule" | — | UC-015 Normal Flow bước 1 |
| 2 | Danh sách Purchase Request đã Accept, chờ lên lịch | List | Đơn đã accept nhưng chưa dispatch | — | |
| 3 | Delivery Date | Date picker | Bắt buộc | BR-071 | |
| 4 | Estimated Arrival Time Window | Input (time range) | Bắt buộc | BR-071 | |
| 5 | Delivery Person (chọn từ danh sách đã đăng ký) | Select | Bắt buộc; hệ thống xác minh delivery person có hồ sơ sinh trắc học (Face Biometric) | BR-050 | Nếu chưa có face biometric → cảnh báo, chặn xác nhận (UC-015 Normal Flow bước 4) |
| 6 | "Confirm Schedule" Button | Button | Validate ≥24 giờ trước dispatch (BR-071) | BR-050, BR-071 | Nếu <24 giờ → chặn, hiển thị cảnh báo |
| 7 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Skeleton danh sách PR chờ lên lịch.
- Empty: Không có PR nào đang chờ lên lịch.
- Error: Chặn xác nhận nếu <24 giờ trước dispatch, hoặc delivery person chưa có face biometric.
- Success: Cập nhật trạng thái delivery, ghi log, thông báo Service Caterers → điều hướng Delivery Detail (20).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Purchase Request Detail (11) qua nút "Accept". Tương ứng node `PRResponse["Confirm Quantity & Delivery Date"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`/`06_Raw_Food_Supplier_Screen_Flow.md`. Điều hướng đi: Delivery Detail (20).
**Bố cục**: Form ngắn trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Confirm Delivery Schedule, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn.
Component bắt buộc dùng: Dialog, Select, DatePicker, Input (time range), Button.
Thành phần bắt buộc: Delivery Date, Estimated Arrival Time Window, Delivery Person, nút Confirm Schedule/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (thiếu biometric hoặc <24h), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3023–3043 (UC-015).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-050, BR-071.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md` (node `PRResponse`).

# Screen Spec — Update Delivery Status

**Actor**: Raw Food Supplier | **UC liên quan**: UC-092 - Update Delivery Status (Report 3 §3.2, dòng 3309–3329) — extends UC-039 (Manage Deliveries) | **Status**: Draft
**BR liên quan**: BR-135 (thông báo trễ >30 phút), BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (Success — cập nhật trạng thái), MSG-065 (Warning — giao hàng trễ so với lịch)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Mã chuyến giao hàng | — | UC-092 Normal Flow bước 1 |
| 2 | Trạng thái hiện tại (chỉ đọc) | Badge | Scheduled/Dispatched/In Transit/Delayed/Delivered | BR-071 | |
| 3 | Cập nhật trạng thái mới (dropdown) | Select | Dispatched / In Transit / Delayed / Delivered | BR-071 | |
| 4 | "Save" Button | Button | Nếu thời gian thực tế lệch >30 phút so với lịch → tự động gửi cảnh báo (MSG-065) tới Service Caterers | BR-135 | |
| 5 | "Cancel" Button | Button | Huỷ | — | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: MSG-004, MSG-005.
- Warning: MSG-065 tự động kích hoạt nếu deviation >30 phút (không chặn lưu, chỉ cảnh báo/thông báo).
- Success: MSG-029, ghi audit trail → về Delivery Detail (20).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "Update Status". Điều hướng đi: quay lại Delivery Detail (20).
**Bố cục**: Form ngắn trong `Dialog` (`Layout_Guidelines.md` §1.2).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Delivery Status, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog form ngắn.
Component bắt buộc dùng: Dialog, Badge, Select, Button.
Thành phần bắt buộc: trạng thái hiện tại, dropdown cập nhật trạng thái, nút Save/Cancel.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: warning (MSG-065), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3309–3329 (UC-092).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-135.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-029, MSG-065.

# Screen Spec — View Delivery Batch Records

**Actor**: Raw Food Supplier | **UC liên quan**: UC-109 - View Delivery Batch Records (Report 3 §3.2, dòng 3375–3395) — extends UC-043 (Manage Ingredient Batches) | **Status**: Draft
**BR liên quan**: BR-077 (theo Report 3 UC-109 "Business Rules: BR-077"); `traceability_matrix.md` dòng 146 ghi BR-071 — cả 2 đều xác nhận nội dung tương tự (lô hàng + số lượng), giữ cả 2 để đối chiếu
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Delivery Batch Records" | — | UC-109 Normal Flow bước 1 |
| 2 | "Back" Button | Button | Về Delivery Detail (20) | — | |
| 3 | Danh sách chuyến giao hàng lịch sử | Table/List | Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes | BR-071, BR-077 | |
| 4 | Filter theo khoảng ngày dispatch | Date range picker | — | — | |
| 5 | Filter theo trạng thái giao hàng | Select | — | — | |

### Trạng thái màn hình
- Loading: Skeleton bảng.
- Empty: Chưa có lịch sử giao hàng nào.
- Error: MSG-004, MSG-005.
- Success: N/A (màn xem, chỉ đọc).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Delivery Detail (20) qua nút "View Batch Records", hoặc từ Ingredient Batches (12). Điều hướng đi: N/A (màn cuối nhánh, chỉ đọc).
**Bố cục**: Admin Shell — `Table` đầy đủ (Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes) kèm filter theo khoảng ngày + trạng thái, phân trang.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình View Delivery Batch Records, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, Table với filter khoảng ngày + trạng thái, phân trang.
Component bắt buộc dùng: Table, DatePicker (range), Select, Badge.
Thành phần bắt buộc: Vehicle Plaque, Driver Name, Dispatch Timestamp, Linked Batch Codes.
Áp dụng Design System: badge theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3375–3395 (UC-109).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071, BR-077.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 146.

# Screen Spec — Update Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-091 - Update Compliance Documents (Report 3 §3.2, dòng 2463–2483) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048
**MSG liên quan**: MSG-004, MSG-005, MSG-009, MSG-029 (Success), MSG-053 (Warning — sắp hết hạn)

> **Ghi chú đối chiếu nguồn**: Report 3 UC-091, dòng Exceptions ghi "MSG-004, MSG-005, MSG-013", nhưng Alternative Flow AF2 mô tả hành vi là **"Invalid Document Format"** — về mặt nghiệp vụ khớp với **MSG-009** ("Unsupported file type") hơn là MSG-013 (dành cho chứng chỉ hết hạn, dùng ở UC-101/152/170). `traceability_matrix.md` dòng 114 cũng không liệt kê MSG-013 cho UC-091 (chỉ MSG-029, MSG-053). Khả năng cao đây là lỗi đánh máy trong bảng Exceptions gốc của Report 3 — dùng MSG-009 ở spec này, **[cần team xác nhận lại]** trước khi khoá.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | Tên tài liệu đang cập nhật | — | UC-091 Normal Flow bước 2 |
| 2 | "Back" Button | Button | Về màn danh sách gọi tới (04/05/26) | — | |
| 3 | Thông tin tài liệu hiện tại (tên, ngày tải lên, ngày hết hạn, trạng thái) | Text/Badge | Chỉ đọc | BR-046 | |
| 4 | File thay thế | File upload | Bắt buộc nếu muốn thay tài liệu, PDF/JPG/PNG ≤10MB | BR-046 | MSG-009 nếu sai định dạng |
| 5 | Ngày hết hạn mới | Date picker | Bắt buộc khi thay file | BR-047, BR-048 | Nếu chứng nhận hết hạn được thay bằng bản hợp lệ → trạng thái tự chuyển "Valid", supplier được nhận lại purchase request (BR-048 AF1) |
| 6 | "Submit"/"Save" Button | Button | Validate trước khi lưu | BR-046, BR-047, BR-048 | |
| 7 | "Cancel" Button | Button | Huỷ, giữ nguyên tài liệu cũ | — | AF3 |

### Trạng thái màn hình
- Loading: Skeleton form với dữ liệu tài liệu hiện tại.
- Empty: N/A.
- Error: MSG-009 (sai định dạng file) — xem ghi chú đối chiếu nguồn ở trên.
- Warning: MSG-053 nếu ngày hết hạn mới vẫn còn <30 ngày.
- Success: MSG-029 "Profile updated successfully" (dùng chung mẫu cập nhật hồ sơ).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Product Origin Documents (04), Food Safety Certificates (05), hoặc Compliance Documents (26) khi chọn 1 tài liệu hiện có. Điều hướng đi: về lại màn danh sách gọi tới.
**Bố cục**: `Dialog`/`Sheet` form ngắn (`Layout_Guidelines.md` §1.2), hiển thị thông tin tài liệu cũ phía trên, form thay thế phía dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Update Documents, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet, thông tin tài liệu cũ phía trên (chỉ đọc), form thay thế phía dưới.
Component bắt buộc dùng: Dialog, Badge, file upload + preview, DatePicker, Button.
Thành phần bắt buộc: thông tin tài liệu hiện tại, file thay thế, ngày hết hạn mới, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-009), warning (MSG-053), success (MSG-029).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2463–2483 (UC-091).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 114 (đối chiếu MSG — xem ghi chú lệch ở trên).

# Screen Spec — Compliance Documents

**Actor**: Raw Food Supplier | **UC liên quan**: UC-167 - Manage Compliance Documents (Report 3 §3.2, dòng 2353–2373) | **Status**: Draft
**BR liên quan**: BR-046, BR-047, BR-048 (Report 3); `traceability_matrix.md` dòng 113 xác nhận BR-046, BR-047
**MSG liên quan**: MSG-004, MSG-005, MSG-053 (Warning — expiring soon); MSG-013 áp dụng nếu supplier cố xem/dựa vào 1 chứng nhận Food Safety đã hết hạn (BR-048)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Compliance Record" | — | Report 3 §3.3.2.5.5 |
| 2 | "Back" Button | Button | Về Supplier Dashboard | — | |
| 3 | "Add / Plus" Icon | Button | Điều hướng tới Upload Supplier Certificates (06) | — | |
| 4 | "Food Safety Certificate" Card | Navigation | Điều hướng tới Food Safety Certificates (05) | BR-046, BR-047 | |
| 5 | "Product Origin Certification" Card | Navigation | Điều hướng tới Product Origin Documents (04) | BR-046 | |
| 6 | Tóm tắt số lượng tài liệu theo trạng thái | Badge/Counter | Valid / Expiring Soon / Expired | BR-047 | Tổng hợp cho cả 2 loại tài liệu |
| 7 | Danh sách đầy đủ mọi tài liệu tuân thủ (filter theo loại/trạng thái) | List/Table | Filter by type/status | BR-046, BR-047, BR-048 | UC-167 Normal Flow bước 2–4 |

### Trạng thái màn hình
- Loading: Skeleton 2 Card + danh sách.
- Empty: Chưa có tài liệu nào → empty state với CTA upload đầu tiên (UC-167 AF1).
- Warning: MSG-053 cho tài liệu sắp hết hạn (UC-167 AF2, <30 ngày theo BR-047).
- Error: MSG-004, MSG-005.
- Success: N/A (màn hình tổng hợp, không ghi dữ liệu trực tiếp).

---

## Platform Layout — Web
**Entry/Exit points**: Từ Supplier Dashboard (01) qua card "Food Safety Compliance Record". Tương ứng node `CompRec["Food Safety Compliance Record"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Food Safety Certificates (05), Product Origin Documents (04), Upload Supplier Certificates (06).
**Bố cục**: Admin Shell — 2 Card lớn (Food Safety Certificate / Product Origin Certification) phía trên dạng col-span-6, danh sách tổng hợp dạng Table filter được phía dưới.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Documents, nền tảng Web, actor Raw Food Supplier.
Bố cục: Admin Shell, 2 Card điều hướng (Food Safety Certificate / Product Origin Certification) phía trên, Table tổng hợp filter theo loại/trạng thái phía dưới.
Component bắt buộc dùng: Card, Table, Badge, Button.
Thành phần bắt buộc: 2 Card điều hướng, tóm tắt số lượng theo trạng thái, danh sách filter được.
Áp dụng Design System: badge theo success/warning/danger — Design_System.md mục 3.
Trạng thái cần thể hiện: loading, empty, warning (expiring soon).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 6258–6272 (§3.3.2.5.5), dòng 2353–2373 (UC-167).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-046, BR-047, BR-048.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 113.
- `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md`, `06_Raw_Food_Supplier_Screen_Flow.md`.
