# Screen Spec — Retrieve Food Sample Records Dialog

**Actor**: Medical Staff | **UC liên quan**: UC-076 (Report 3 §3.2, dòng 3757–3771) | **Status**: Draft
**BR liên quan**: BR-111 (thời gian lưu mẫu tối thiểu), BR-113 (thu hồi mẫu khi có sự cố), BR-114 (liên kết mẫu với thực đơn/kiểm tra, sửa 12/07/2026), BR-150 (điều tra nguyên nhân gốc rễ), **BR-212 (bổ sung thông tin bàn giao mẫu — mới 12/07/2026)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 12/07/2026 (Nhóm E, căn cứ `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md` Phụ lục V "Biên bản bàn giao mẫu")**: (1) field "Tên người ủy quyền" đổi từ nhập tay sang **tự động = tài khoản Medical Staff đang đăng nhập, readonly** (BR-114); (2) bổ sung field "Đại diện phòng lab nhận mẫu" (tùy chọn) và "Chỉ tiêu yêu cầu kiểm nghiệm" (bắt buộc) (BR-212); (3) biên nhận in ra mở rộng nội dung để khớp cấu trúc Phụ lục V. **Không** thêm bước ký 2 bên/chụp ảnh/gate cứng như BR-211 — xem rationale ở BR-212.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tìm mẫu theo ngày/món/batch | Search/Filter | — | — | UC-076 bước 1 |
| 2 | Danh sách mẫu khớp kết quả | List/Card | Hiển thị trạng thái + thời gian lưu còn lại | BR-111 | |
| 3 | Nút "Authorize Retrieval" (mỗi item) | Button | Chỉ khả dụng khi mẫu còn ở trạng thái "Retained" | BR-113 | Nếu mẫu đã "Disposed" → chỉ hiện nút "Link Disposed Sample to Incident" (AF1) |
| 4 | Incident ID | Input (text)/Select | Bắt buộc; phải là ID đang active | BR-114 | Option đã đóng bị disable ngay tại dropdown |
| 5 | Tên người ủy quyền (`authorizedBy`) | Text hiển thị (đọc-only) | Tự động = tài khoản Medical Staff đăng nhập | BR-114 | **Sửa 12/07/2026**: trước đây Input tự do, nay readonly — nhất quán với `reportedBy` (BR-183), "Đại diện Y tế trường học" (BR-211) |
| 6 | Phòng lab đích | Select | Bắt buộc | BR-114 | Giữ nguyên — danh sách phòng lab cố định |
| 7 | Đại diện phòng lab nhận mẫu | TextField tự do | Tùy chọn — có thể chưa xác định tại thời điểm thu hồi | BR-212 | Mới — khớp field "Đại diện là" (Bên nhận mẫu) trong Phụ lục V |
| 8 | Chỉ tiêu yêu cầu kiểm nghiệm | TextField multiline | Bắt buộc | BR-212 | Mới — khớp cột "Chỉ tiêu yêu cầu kiểm nghiệm" trong Phụ lục V; MSG-023 nếu thiếu |
| 9 | Nút "Submit" | Button (primary) | Disabled khi thiếu field bắt buộc (4, 6, 8) | BR-113, BR-114, BR-150, BR-212 | Khóa mẫu (chuyển "Under Investigation"/"Retrieved"), sinh biên nhận in (nội dung mở rộng theo Phụ lục V) |

### Trạng thái màn hình
- Loading: spinner khi xác thực Incident ID + khóa bản ghi mẫu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; MSG-023 (Incident ID không hợp lệ/đã đóng, AF2, hoặc thiếu Chỉ tiêu yêu cầu kiểm nghiệm) → yêu cầu nhập lại/bổ sung; mẫu đã Disposed (AF1) → chỉ cho phép liên kết metadata hủy mẫu vào sự cố, không cho thu hồi vật lý.
- Success: khóa bản ghi thành công, hiển thị biên nhận thu hồi có thể in (UC-076 Postcondition, nội dung mở rộng theo Phụ lục V), quay lại `13_FoodSampleRecords.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (hoặc từ `17_InvestigationWorkspace.md` nhánh "1. Extracting Stored Food Samples"); thành công → quay lại màn nguồn.
**Bố cục**: `showModalBottomSheet` — kết quả tìm kiếm mẫu trên cùng, form Incident ID/người ủy quyền (readonly)/phòng lab/đại diện phòng lab/chỉ tiêu kiểm nghiệm bên dưới, nút "Submit" cố định cuối sheet.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn 1 mẫu trên `13_FoodSampleRecords.md` (Web) hoặc từ Investigation Workspace (Web); thành công → quay lại màn nguồn.
**Bố cục**: `Dialog` — kết quả tìm kiếm mẫu trên cùng (list ngắn), form Incident ID/người ủy quyền (readonly)/phòng lab/đại diện phòng lab/chỉ tiêu kiểm nghiệm bên dưới, nút "Submit"/"Cancel" cuối Dialog.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Retrieve Food Sample Records Dialog, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: tìm mẫu theo ngày/món/batch, danh sách mẫu khớp, nút Authorize Retrieval,
Incident ID, tên người ủy quyền (hiển thị tự động, không sửa được), phòng lab đích (Select),
đại diện phòng lab nhận mẫu (tùy chọn), chỉ tiêu yêu cầu kiểm nghiệm (multiline, bắt buộc), nút Submit.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (MSG-023 Incident ID không hợp lệ, mẫu đã Disposed), success (biên nhận in).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm bước ký/chụp ảnh biên bản.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3757–3771 (UC-076).
- business_rules.md (BR-111, BR-113, BR-114, BR-150, BR-212).
- error_message.md (MSG-004, MSG-005, MSG-023).
- `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md` Phụ lục V (Biên bản bàn giao mẫu) — căn cứ BR-212.
