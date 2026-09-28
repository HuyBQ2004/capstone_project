# Screen Spec — Retrieve Food Sample Records Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-076 (Report 3 §3.2, dòng 3757–3771) | **Status**: Draft
**BR liên quan**: BR-111 (thời gian lưu mẫu tối thiểu), BR-113 (thu hồi mẫu khi có sự cố), BR-114 (liên kết mẫu với thực đơn/kiểm tra, sửa 12/07/2026), BR-150 (điều tra nguyên nhân gốc rễ), **BR-212 (bổ sung thông tin bàn giao mẫu — mới 12/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 12/07/2026 (Nhóm E, căn cứ `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md` Phụ lục V "Biên bản bàn giao mẫu")**: (1) field "Tên người ủy quyền" đổi từ nhập tay sang **tự động = tài khoản Medical Staff đang đăng nhập, readonly** (BR-114); (2) bổ sung field "Đại diện phòng lab nhận mẫu" (tùy chọn) và "Chỉ tiêu yêu cầu kiểm nghiệm" (bắt buộc) (BR-212); (3) biên nhận in ra mở rộng nội dung để khớp cấu trúc Phụ lục V. **Không** thêm bước ký 2 bên/chụp ảnh/gate cứng như BR-211 — xem rationale ở BR-212.
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.6): bổ sung BIẾN THỂ ngữ cảnh Incident.** Khi mở từ `17_InvestigationWorkspace.md` (card "1. Thu hồi mẫu lưu") của 1 `INC-xxx` cụ thể, dùng **Dialog cỡ lớn / checklist multi-select** thay cho giao diện search/filter rộng — xem mục "Biến thể ngữ cảnh Incident" bên dưới. Bản search/filter hiện tại **giữ nguyên** cho các điểm vào UC-076 không gắn Incident (VD từ `13_FoodSampleRecords.md` độc lập).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tìm mẫu theo ngày/món/batch | Search/Filter | — | — | UC-076 bước 1 |
| 2 | Danh sách mẫu khớp kết quả | List | Hiển thị trạng thái + thời gian lưu còn lại | BR-111 | |
| 3 | Nút "Authorize Retrieval" (mỗi item) | Button | Chỉ khả dụng khi mẫu "Retained" | BR-113 | Nếu "Disposed" → chỉ hiện "Link Disposed Sample to Incident" (AF1) |
| 4 | Incident ID | Input (text)/Select | Bắt buộc; phải active | BR-114 | Option đã đóng bị disable ngay tại dropdown (không chờ Submit mới báo MSG-023) |
| 5 | Tên người ủy quyền (`authorizedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản Medical Staff đăng nhập | BR-114 | **Sửa 12/07/2026**: trước đây Input tự do, nay readonly — nhất quán với `reportedBy` (BR-183), "Đại diện Y tế trường học" (BR-211) |
| 6 | Phòng lab đích | Select | Bắt buộc | BR-114 | Giữ nguyên — danh sách phòng lab cố định |
| 7 | Đại diện phòng lab nhận mẫu | Input (text tự do) | Tùy chọn — có thể chưa xác định tại thời điểm thu hồi | BR-212 | Mới — khớp field "Đại diện là" (Bên nhận mẫu) trong Phụ lục V |
| 8 | Chỉ tiêu yêu cầu kiểm nghiệm | Textarea | Bắt buộc | BR-212 | Mới — khớp cột "Chỉ tiêu yêu cầu kiểm nghiệm" trong Phụ lục V; MSG-023 nếu thiếu |
| 9 | Nút "Submit" | Button (primary) | Disabled khi thiếu field bắt buộc (4, 6, 8) | BR-113, BR-114, BR-150, BR-212 | Khóa mẫu, sinh biên nhận in (nội dung mở rộng theo Phụ lục V) |

### Biến thể ngữ cảnh Incident (mới 15/07/2026, đợt 2 — Mục 11.6)

Khi mở từ `17_InvestigationWorkspace.md` của 1 `INC-xxx`:

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| I1 | Nút "← Quay lại Không gian điều tra (INC-xxx)" | Button (ghost) | — | — | Điều hướng chủ động (Mục 7.6 Loại A), quay đúng Investigation Workspace của `INC-xxx` đang mở |
| I2 | Checklist mẫu (multi-select) | Checklist | Mặc định = `suspectedDishIds[]` đã chọn ở trạng thái TREATMENT | BR-111, BR-150 | Mỗi dòng = 1 mẫu, kèm trạng thái + thời gian lưu còn lại (BR-111); tick nhiều dòng. Mẫu `Disposed` (quá 72h) hiển thị mờ + chỉ cho "Link Disposed Sample to Incident" (AF1) |
| I3 | Incident ID | Text (đọc, khóa sẵn) | Set sẵn theo context, không chọn tay | BR-114 | Khác bản search/filter (Incident ID là Select) — ở đây khóa theo `INC-xxx` đang điều tra |
| I4 | Tên người ủy quyền (`authorizedBy`) | Text (đọc) | Tự động = Medical Staff đăng nhập | BR-114 | Như field 5 bản gốc |
| I5 | Phòng lab đích (dùng chung cả lô gửi) | Select | Bắt buộc | BR-114 | **Quyết định 15/07/2026 (Mục 11.6, Q2)**: **1 phòng lab dùng chung** cho mọi mẫu đã tick trong 1 lần gửi — khớp thực tế Việt Nam: mẫu ngộ độc gửi về **1 phòng lab được chỉ định (ISO 17025/VILAS)** chạy trọn panel vi sinh + hóa (Salmonella, E. coli, S. aureus, B. cereus, Cl. perfringens…) trên nhiều nền mẫu cùng lúc |
| I6 | Chỉ tiêu yêu cầu kiểm nghiệm | Textarea (panel dùng chung, pre-fill mỗi món, cho phép sửa theo từng món) | Bắt buộc | BR-212 | **Quyết định 15/07/2026 (Mục 11.6, Q2)**: mặc định 1 panel chỉ tiêu **dùng chung** áp cho mọi món đã tick, nhưng **cho phép mở rộng chỉnh riêng theo từng món** — clinically món sữa/tráng miệng nghiêng về độc tố tụ cầu (S. aureus enterotoxin), món thịt nghiêng về Salmonella. Cân bằng đơn giản (1 submission) + linh hoạt lâm sàng |
| I7 | Nút "Submit" (gửi 1 lần cho cả lô) | Button (primary) | Disabled khi thiếu I5/I6 hoặc chưa tick mẫu nào | BR-113, BR-114, BR-150, BR-212 | Khóa tất cả mẫu đã tick, ghi `incidentId` cho từng mẫu, sinh biên nhận. Sau khi submit + quay lại, card "1. Thu hồi mẫu lưu" (`17_InvestigationWorkspace.md`) tự query & liệt kê mẫu có `incidentId = INC-xxx` hiện tại (Mục 9.10 Bug 2, 11.8) |

*(Câu hỏi Q2 do người dùng ủy quyền cho tôi nghiên cứu — đã chốt hướng "1 lab dùng chung + chỉ tiêu mặc định chung, cho phép override theo món". Vẫn là **đề xuất**, có thể điều chỉnh nếu đội dự án muốn khóa cứng chỉ tiêu chung tuyệt đối cho mọi món.)*

### Trạng thái màn hình
- Loading: spinner khi xác thực Incident ID + khóa bản ghi.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; MSG-023 (Incident ID không hợp lệ/đã đóng, hoặc thiếu Chỉ tiêu yêu cầu kiểm nghiệm); mẫu Disposed → chỉ liên kết metadata.
- Success: khóa bản ghi, hiển thị biên nhận thu hồi (nội dung mở rộng — xem Phụ lục V), quay lại `13_FoodSampleRecords.md`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (Web) hoặc từ Investigation Workspace (Web); thành công → quay lại màn nguồn.
**Bố cục**: `Dialog` — kết quả tìm kiếm mẫu trên cùng, form Incident ID/người ủy quyền (readonly)/phòng lab/đại diện phòng lab/chỉ tiêu kiểm nghiệm bên dưới, nút "Submit"/"Cancel" cuối Dialog.
**Biến thể ngữ cảnh Incident (Mục 11.6)**: `Dialog` cỡ lớn — nút "← Quay lại Không gian điều tra" trên cùng, checklist mẫu (mặc định lọc theo `suspectedDishIds[]`) chiếm phần chính, 1 bộ field dùng chung (Incident ID khóa sẵn | phòng lab đích | chỉ tiêu — pre-fill mỗi món, sửa được), nút "Submit" gửi 1 lần cho cả lô cuối Dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (Mobile) hoặc từ Investigation Workspace (Mobile); thành công → quay lại màn nguồn.
**Bố cục**: `showModalBottomSheet` — kết quả tìm kiếm trên cùng, form bên dưới, nút "Submit" cố định cuối sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Retrieve Food Sample Records Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog, tìm mẫu theo ngày/món/batch, danh sách mẫu khớp, nút Authorize Retrieval,
Incident ID, tên người ủy quyền (hiển thị tự động, không sửa được), phòng lab đích (Select),
đại diện phòng lab nhận mẫu (tùy chọn), chỉ tiêu yêu cầu kiểm nghiệm (Textarea, bắt buộc), nút Submit.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (MSG-023, mẫu Disposed), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm bước ký/chụp ảnh biên bản.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3757–3771 (UC-076).
- business_rules.md (BR-111, BR-113, BR-114, BR-150, BR-212).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md` Phụ lục V (Biên bản bàn giao mẫu) — căn cứ BR-212.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 11.6 (biến thể ngữ cảnh Incident, batch lab, giới hạn 72h/Disposed — đợt 2 15/07/2026).
