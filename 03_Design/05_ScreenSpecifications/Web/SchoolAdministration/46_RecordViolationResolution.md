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
Thành phần bắt buộc: vi phạm gốc + link, hành động xử lý (đọc-only), người xử lý, badge mức kỷ luật, badge trạng thái, nút Chuyển sang duyệt.
Áp dụng Design System: badge warning cho Pending, success cho Approved.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm form nhập liệu vì School Admin chỉ xem, không tự lập biên bản (đó là quyền Medical Staff).
```

## Nguồn dữ liệu
- Report 3.md, dòng 4665–4681 (Table 3-161 Record Violation Resolution Use Cases Specification)
- traceability_matrix.md §1.11 (UC-180 ↔ BR-185/BR-186/BR-211)
- 14_Screen_Authorization_Matrix.md khối 10
- `business_rules.md` §18 (BR-211 — mới 06/07/2026); `00_Policies/specialreport2173_1.md`
