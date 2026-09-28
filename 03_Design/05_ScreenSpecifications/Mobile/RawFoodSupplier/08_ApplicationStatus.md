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
