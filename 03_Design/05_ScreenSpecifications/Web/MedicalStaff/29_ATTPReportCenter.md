# Screen Spec — ATTP Report Center

**Actor**: Medical Staff | **UC liên quan**: UC-187 (mới, bổ sung 13/07/2026 — không có trong Report 3.md gốc) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền xem báo cáo theo vai trò)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

> **Bổ sung 13/07/2026**: màn hub mới, gom 1 điểm vào chung cho 3 loại báo cáo ATTP mà Medical Staff có thể chủ động soạn và gửi cho School Administration — trước đây chỉ có 1 loại (UC-185, vào từ `23_ComplianceDashboard.md`). Phát sinh từ phản hồi người dùng (rà soát ảnh chụp màn hình, "Hình 7") khi nhận thấy: (1) dữ liệu sự cố và vi phạm cũng là mảng Medical Staff quản lý nhưng chưa có kênh báo cáo chủ động theo kỳ; (2) chưa có nơi xem lại các báo cáo đã gửi. Không thay thế UC-185 — chỉ là điểm vào bổ sung, giữ nguyên UC-185 vẫn có thể vào trực tiếp từ Compliance Dashboard như cũ. Xem `business_rules.md` BR-213/BR-214, Changelog v1.7; `review/medical_staff/review_screen_medical_staff.md` Mục 17.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card "Báo cáo Giám sát tuân thủ" | Card hành động | — | BR-209 | Mô tả ngắn: điểm kiểm thực 3 bước, checklist vệ sinh, côn trùng, sức khỏe NV. Nút "Soạn & Gửi" → `28_SendComplianceReportDialog.md` (UC-185) |
| 2 | Card "Báo cáo Tổng hợp Sự cố" | Card hành động | — | BR-213 | Mô tả ngắn: số sự cố theo trạng thái/mức độ trong kỳ. Nút "Soạn & Gửi" → `30_SendIncidentSummaryDialog.md` (UC-188) |
| 3 | Card "Báo cáo Tổng hợp Vi phạm" | Card hành động | — | BR-214 | Mô tả ngắn: số vi phạm theo loại/mức độ trong kỳ. Nút "Soạn & Gửi" → `31_SendViolationSummaryDialog.md` (UC-189) |
| 4 | Lần gửi gần nhất (mỗi card) | Text nhỏ (đọc) | "Chưa gửi lần nào" nếu chưa có | BR-177 | VD: "Gửi gần nhất: 12/07/2026 (Weekly)" |
| 5 | Nút "Xem lịch sử báo cáo đã gửi" | Button (link) | — | BR-177 | → `32_SentReportsHistory.md` (UC-190) |

### Trạng thái màn hình
- Loading: `Skeleton` cho 3 card.
- Empty: không áp dụng (3 card luôn hiển thị, chỉ khác trạng thái "Chưa gửi lần nào").
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn điều hướng).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ menu Sidebar "Báo cáo ATTP" (mới); mỗi card dẫn tới dialog soạn báo cáo tương ứng; nút "Xem lịch sử" dẫn tới `32_SentReportsHistory.md`.
**Bố cục**: Page Header đơn giản, Content Area 3 Card xếp ngang (`col-span-4` mỗi card) hàng trên, nút "Xem lịch sử báo cáo đã gửi" ở góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ menu điều hướng chung (Mobile) "Báo cáo ATTP"; chọn Card → mở dialog soạn báo cáo tương ứng.
**Bố cục**: 1 cột, 3 Card xếp dọc, nút "Xem lịch sử báo cáo đã gửi" cuối danh sách card (dạng link/button phụ).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình ATTP Report Center, nền tảng Web, actor Medical Staff.
Bố cục: Page Header, 3 Card hành động xếp ngang (Giám sát tuân thủ / Tổng hợp Sự cố / Tổng hợp Vi phạm),
mỗi card có mô tả ngắn + lần gửi gần nhất + nút "Soạn & Gửi", nút "Xem lịch sử báo cáo đã gửi" góc trên phải.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `business_rules.md` §16 (BR-175, BR-213, BR-214), §12 (BR-209).
- `error_message.md` (MSG-004, MSG-005).
- `traceability_matrix.md` (UC-187, mới 13/07/2026).
- `review/medical_staff/review_screen_medical_staff.md` Mục 17.

**Ghi chú màn hình dùng chung**: không dùng chung với School Administration — đây là màn hình soạn/gửi (viết), phía School Administration chỉ nhận (đọc) qua thông báo + Compliance Dashboard/Incident List/Violation Registry hiện có, không có "Report Center" tương ứng.
