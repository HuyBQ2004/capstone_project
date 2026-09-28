# Screen Spec — Record Violation Resolution

**Actor**: Medical Staff (Primary Actor theo UC-180; màn hình dùng chung với School Administration — School Admin chỉ xem lại/duyệt qua UC-182, xem `Web/SchoolAdministration/46_RecordViolationResolution.md`) | **UC liên quan**: UC-180 - Record Violation Resolution (Report 3 §3.2, dòng 4661–4681 — **đã tìm thấy bảng đặc tả đầy đủ khi review chéo với bản School Administration, đính chính lại ghi chú "chưa có UC" trước đây**) | **Status**: Draft
**BR liên quan**: BR-185 (biên bản xử lý sai phạm), BR-186 (phân cấp phê duyệt), BR-188 (audit log bất biến), **BR-211 (xuất PDF biên bản chuẩn + lưu ảnh đã ký 3 bên — mới, 06/07/2026, xem ghi chú field 9)**
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-085 (Error — thiếu field bắt buộc), MSG-087 (Success), MSG-089 (Success — chỉ áp dụng khi School Admin duyệt, xem bản của họ), MSG-095 (Error — chặn đóng/gửi duyệt khi chưa có ảnh biên bản đã ký)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Vi phạm gốc liên kết (process hoặc staff conduct) | Text hiển thị + link | — | BR-183, BR-184 | Hợp nhất từ bản School Administration; link sang chi tiết `27_ViolationRegistry.md` |
| 2 | Tóm tắt biên bản vi phạm (context, đọc) | Text | — | — | Kế thừa từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` |
| 3 | Hành động xử lý (actionTaken) | Textarea | Bắt buộc | BR-185 | MSG-085 nếu thiếu |
| 4 | Người xử lý (handledBy) | Text (auto) | — | BR-185 | |
| 5 | Mức độ hình thức kỷ luật (disciplinaryLevel) | Select | Bắt buộc khi vi phạm nhân viên — Nhắc nhở/Cảnh cáo/Đình chỉ/Đề xuất chấm dứt | BR-185 | |
| 6 | Trạng thái (Pending Approval/Approved/Closed) | Badge (warning/success) | — | BR-186 | Hợp nhất từ bản School Administration, khớp field `status` (`ViolationResolutionRecord`) |
| 7 | Nút "Đóng biên bản" (LOW) | Button (primary) | Chỉ khả dụng khi LOW; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Hover/tap khi disabled → tooltip/toast MSG-095 |
| 8 | Nút "Gửi School Administration phê duyệt" (MEDIUM/HIGH) | Button (primary) | Bắt buộc khi ≥ MEDIUM; **disabled nếu chưa có `signedRecordPhotoUrl`** (BR-211, gate bắt buộc) | BR-186, BR-211 | Hover/tap khi disabled → tooltip/toast MSG-095 |
| 9 | Badge "Tình trạng biên bản đã ký" (dựa trên `signedRecordPhotoUrl` của biên bản gốc, BR-211) | Badge (danger/success) — "Chưa có ảnh ký — chưa thể đóng/gửi duyệt" / "Đã có ảnh ký xác nhận" | Hiển thị luôn để giải thích lý do nút 7/8 bị disable | BR-211 | Có link/nút phụ "Quay lại tải ảnh biên bản" điều hướng về `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (field ảnh biên bản đã ký) |

### Trạng thái màn hình
- Loading: spinner khi lưu.
- Empty: không áp dụng.
- Error: MSG-004, MSG-005; thiếu field bắt buộc → cảnh báo inline; **chưa có `signedRecordPhotoUrl` → nút 7/8 disabled, MSG-095 (Error) khi cố thao tác — chặn cứng theo BR-211 (quyết định người dùng 06/07/2026)**.
- Success: LOW → MSG-089; MEDIUM/HIGH → MSG-087, quay lại `27_ViolationRegistry.md`. Điều kiện tiên quyết cho cả 2 nhánh: đã có `signedRecordPhotoUrl`.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Web); thành công → `27_ViolationRegistry.md`.
**Bố cục**: `Dialog`/trang con — tóm tắt biên bản, form Hành động xử lý + Mức kỷ luật, nút hành động tương ứng.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Mobile); thành công → `27_ViolationRegistry.md`.
**Bố cục**: trang riêng full-screen, tóm tắt trên cùng, form bên dưới, nút cố định cuối màn.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Record Violation Resolution, nền tảng Web, actor Medical Staff.
Bố cục: Dialog, link vi phạm gốc, tóm tắt biên bản, Hành động xử lý, badge Trạng thái, Select mức kỷ luật, nút Đóng biên bản/Gửi phê duyệt.
Áp dụng Design System: success #15803D, warning #B45309.
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-087/MSG-089).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4661–4681 (Table 3-161, UC-180 Record Violation Resolution — Normal Flow đầy đủ).
- business_rules.md §18 (BR-185, BR-186, BR-188, BR-211 — mới 06/07/2026).
- error_message.md (MSG-004, MSG-005, MSG-085, MSG-087, MSG-089, MSG-095 — mới 06/07/2026).

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/SchoolAdministration/46_RecordViolationResolution.md` — cả 2 bản thống nhất: Medical Staff là actor **lập** biên bản xử lý (Normal Flow UC-180 bước 1-6), School Administration chỉ **xem lại/duyệt** (qua UC-182, không tự lập). Field dữ liệu 2 bản khớp nhau hợp lý.
