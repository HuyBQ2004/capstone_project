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
