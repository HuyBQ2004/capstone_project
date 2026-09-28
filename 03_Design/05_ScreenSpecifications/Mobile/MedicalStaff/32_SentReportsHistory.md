# Screen Spec — Sent Reports History

**Actor**: Medical Staff | **UC liên quan**: UC-190 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-177 (lưu trữ lịch sử báo cáo tối thiểu 12 tháng), BR-175 (phân quyền xem báo cáo theo vai trò), BR-178 (định dạng xuất báo cáo)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Bổ sung 13/07/2026**: màn hình xem lại lịch sử các báo cáo Medical Staff đã gửi cho School Administration (cả 3 loại: BR-209/UC-185, BR-213/UC-188, BR-214/UC-189). **Không cấp BR mới** — hiện thực hóa **BR-177** (đã có sẵn) ở phạm vi Medical Staff. **Thiết kế theo mẫu UC-107 "View Batch History"** (Raw Food Supplier) — màn lịch sử riêng, KHÔNG dựa vào Audit Logs (UC-106, Medical Staff hiện chưa có quyền). Nội dung giống hệt bản Web, chỉ khác Platform Layout. Xem `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Lịch sử báo cáo đã gửi" | — | |
| 2 | Filter theo Loại báo cáo | Select/Chip | Giám sát tuân thủ / Tổng hợp Sự cố / Tổng hợp Vi phạm / Tất cả | — | Mặc định: Tất cả |
| 3 | Filter theo khoảng ngày gửi | Date range picker | — | — | |
| 4 | Danh sách báo cáo đã gửi | Card cuộn dọc (đọc-only) | Mỗi Card: Loại báo cáo (badge), Kỳ (Daily/Weekly), Khoảng thời gian dữ liệu, Ngày giờ gửi, Người gửi | BR-177, BR-182 | Sắp xếp mặc định: mới nhất trước |
| 5 | Nút "Tải lại PDF" (mỗi Card) | Button/Icon | — | BR-178 | Tải lại đúng file PDF đã sinh tại thời điểm gửi |

### Trạng thái màn hình
- Loading: `Shimmer`/`Skeleton` cho danh sách.
- Empty: "Chưa có báo cáo nào được gửi" + illustration (hoặc "Không có báo cáo nào khớp bộ lọc").
- Error: MSG-004, MSG-005.
- Success: danh sách hiển thị đầy đủ, không có MSG riêng (màn chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Xem lịch sử báo cáo đã gửi" trên `29_ATTPReportCenter.md` (Mobile).
**Bố cục**: 1 cột, filter dạng `Chip`/`DropdownButton` phía trên, danh sách `Card` cuộn dọc, mỗi Card có badge Loại báo cáo góc trên + nút "Tải lại PDF" góc dưới phải.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Xem lịch sử báo cáo đã gửi" trên `29_ATTPReportCenter.md` (Web).
**Bố cục**: filter bar (Loại báo cáo, khoảng ngày) phía trên `Table` đầy đủ, phân trang dưới bảng.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Sent Reports History, nền tảng Mobile, actor Medical Staff.
Bố cục: filter Chip/DropdownButton (Loại báo cáo, khoảng ngày gửi) phía trên, danh sách Card cuộn dọc
(badge Loại báo cáo, Kỳ, Khoảng thời gian dữ liệu, Ngày giờ gửi, Người gửi, nút Tải lại PDF).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-177, BR-175, BR-178, BR-182).
- `error_message.md` (MSG-004, MSG-005).
- `traceability_matrix.md` (UC-190, mới 13/07/2026).
- `17_ViewBatchHistory.md` (Raw Food Supplier, UC-107) — mẫu tham chiếu cho cấu trúc màn lịch sử riêng theo actor.
- `28_SendComplianceReportDialog.md` (UC-185), `30_SendIncidentSummaryDialog.md` (UC-188), `31_SendViolationSummaryDialog.md` (UC-189) — 3 nguồn dữ liệu ghi vào lịch sử này mỗi khi gửi thành công.
- `review/medical_staff/review_screen_medical_staff.md` Mục 17.
