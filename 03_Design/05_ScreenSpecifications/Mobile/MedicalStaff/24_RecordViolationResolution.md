# Screen Spec — Record Violation Resolution

**Actor**: Medical Staff (Primary Actor theo UC-180; màn hình dùng chung với School Administration — School Admin chỉ xem lại/duyệt qua UC-182, xem `Web/SchoolAdministration/46_RecordViolationResolution.md`) | **UC liên quan**: UC-180 - Record Violation Resolution (Report 3 §3.2, dòng 4661–4681 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo với bản School Administration, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-185 (biên bản xử lý sai phạm, `ViolationResolutionRecord`), BR-186 (phân cấp phê duyệt: LOW tự đóng, MEDIUM/HIGH chuyển School Administration), BR-188 (audit log bất biến), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký 3 bên — mới, 06/07/2026, xem ghi chú field 9)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error — thiếu field bắt buộc), MSG-086 (Success — kế thừa), MSG-087 (Success), MSG-089 (Success — chỉ áp dụng khi School Admin duyệt, xem bản của họ), MSG-095 (Error — chặn đóng/gửi duyệt khi chưa có ảnh biên bản đã ký)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Vi phạm gốc liên kết (process hoặc staff conduct) | Text hiển thị + link | — | BR-183, BR-184 | Hợp nhất từ bản School Administration; link sang chi tiết `27_ViolationRegistry.md` |
| 2 | Tóm tắt biên bản vi phạm (context, đọc) | Text | — | — | Kế thừa từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` |
| 3 | Hành động xử lý (actionTaken) | Textarea | Bắt buộc | BR-185 | MSG-085 nếu thiếu |
| 4 | Người xử lý (handledBy) | Text (auto, user hiện tại) | — | BR-185 | |
| 5 | Mức độ hình thức kỷ luật (disciplinaryLevel) | Select | Bắt buộc khi vi phạm nhân viên (UC-179) — Nhắc nhở/Cảnh cáo bằng văn bản/Đình chỉ tạm thời/Đề xuất chấm dứt hợp đồng | BR-185 | Không áp dụng cho vi phạm quy trình thuần túy (UC-178) |
| 6 | Trạng thái (Pending Approval/Approved/Closed) | Badge (warning/success) | — | BR-186 | Hợp nhất từ bản School Administration, khớp field `status` (`ViolationResolutionRecord`) |
| 7 | Nút "Đóng biên bản" (chỉ hiện khi mức LOW) | Button (primary) | Chỉ khả dụng khi severityLevel = LOW; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Medical Staff tự phê duyệt/đóng trực tiếp; tap khi disabled → toast MSG-095 |
| 8 | Nút "Gửi School Administration phê duyệt" (chỉ hiện khi MEDIUM/HIGH) | Button (primary) | Bắt buộc khi severityLevel ≥ MEDIUM; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Chuyển trạng thái "Pending Approval"; tap khi disabled → toast MSG-095 |
| 9 | Badge "Tình trạng biên bản đã ký" (dựa trên `signedRecordPhotoUrl` của biên bản gốc, BR-211) | Badge (danger/success) — "Chưa có ảnh ký — chưa thể đóng/gửi duyệt" / "Đã có ảnh ký xác nhận" | Hiển thị luôn để giải thích lý do nút 7/8 bị disable | BR-211 | Có nút phụ "Quay lại tải ảnh biên bản" điều hướng về `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` |

### Trạng thái màn hình
- Loading: spinner khi lưu biên bản xử lý.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; thiếu actionTaken/disciplinaryLevel bắt buộc → cảnh báo inline; **chưa có `signedRecordPhotoUrl` → nút 7/8 disabled, MSG-095 (Error) khi cố thao tác — chặn cứng theo BR-211 (quyết định người dùng 06/07/2026)**.
- Success: mức LOW → MSG-089 (đóng trực tiếp, áp dụng kỷ luật ngay); mức MEDIUM/HIGH → MSG-087 (nộp chờ phê duyệt), quay lại `27_ViolationRegistry.md`. Điều kiện tiên quyết cho cả 2 nhánh: đã có `signedRecordPhotoUrl`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` sau khi lập biên bản; thành công → `27_ViolationRegistry.md`.
**Bố cục**: trang riêng full-screen, tóm tắt biên bản trên cùng, form Hành động xử lý + Mức kỷ luật, 1 trong 2 nút (Đóng biên bản/Gửi phê duyệt) hiện tùy theo severityLevel, cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Web) sau khi lập biên bản; thành công → `27_ViolationRegistry.md`.
**Bố cục**: `Dialog`/trang con — tóm tắt biên bản, form Hành động xử lý + Mức kỷ luật, nút hành động tương ứng cuối form.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Violation Resolution, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: link vi phạm gốc, tóm tắt biên bản vi phạm, Hành động xử lý, người xử lý, badge Trạng thái, Select mức kỷ luật
(Nhắc nhở/Cảnh cáo/Đình chỉ/Đề xuất chấm dứt), nút Đóng biên bản (LOW) hoặc Gửi phê duyệt (MEDIUM/HIGH).
Áp dụng Design System: success #15803D cho đóng trực tiếp, warning #B45309 cho chờ phê duyệt.
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-087/MSG-089 tùy nhánh).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4661–4681 (Table 3-161, UC-180 Record Violation Resolution — Normal Flow đầy đủ).
- business_rules.md §18 (BR-185, BR-186, BR-188, BR-211 — mới 06/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-087, MSG-089, MSG-095 — mới 06/07/2026).
- `02_Requirements/Violation_Management_Proposal.md`.
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md (nhánh "ViolResolution").

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/46_RecordViolationResolution.md` — bổ sung field "Vi phạm gốc liên kết" (link) và "Trạng thái" (badge) còn thiếu so với bản School Administration; giữ nguyên 2 nút hành động riêng của Medical Staff (Đóng biên bản/Gửi phê duyệt) vì School Admin chỉ có nút "Chuyển sang duyệt" (xem qua UC-182).
