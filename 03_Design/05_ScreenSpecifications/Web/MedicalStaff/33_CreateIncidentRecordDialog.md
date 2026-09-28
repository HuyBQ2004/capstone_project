# Screen Spec — Create Incident Record Dialog 🆕

**Actor**: Medical Staff | **UC liên quan**: UC-114 (mở rộng, xem `Report 3.md` dòng 4555–4569 — không có trong bản gốc, bổ sung 15/07/2026) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng gộp), BR-154 (báo cáo sự cố chính thức — áp dụng sớm hơn ở giai đoạn triage)
**MSG liên quan**: MSG-023 (Error, tái sử dụng), MSG-100 (Success — lưu nháp), MSG-101 (Success — hoàn tất)

> **Mới 15/07/2026** — bổ sung theo phản hồi người dùng (3 ảnh chụp code thật, "Sự cố & Điều tra"). Đảo ngược quyết định 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.8, Câu hỏi 1 — khi đó chốt "gộp làm 1 bước, không tách 2 bước" vì cho rằng tách ra là dư thừa). Bước giữa mới này **không phải** xác nhận suông như bản gốc mà là **soạn thảo 1 văn bản chính thức** (biên bản), nên không còn là bước dư thừa. Xem Mục 10 cùng file review.
> Cấu trúc field rút ra từ mẫu thực tế `00_Policies/specialreport2173_1.md` ("Biên bản Sự việc") — cùng phương pháp đã dùng cho BR-211 (biên bản vi phạm).
> **Cập nhật 15/07/2026 (đợt 2 — Mục 11.3)**: (a) bổ sung field đọc "Ảnh minh chứng (kế thừa từ báo cáo nguồn)" — union `evidencePhotoUrls[]` từ các `RPT-xxx` đã chọn, chỉ để tham khảo khi viết (không upload thêm ở đây); (b) nâng field "Nội dung biên bản" từ `Textarea` trơn lên **rich text editor** (cùng toolbar `22_RecordStaffViolation.md` field 9) để nhất quán thuật ngữ "biên bản". **Không** áp cơ chế gate ký-ảnh BR-211 (in — ký tay — chụp ảnh) cho biên bản sự cố này: đây là tài liệu triage nội bộ, không phải văn bản pháp lý cuối cùng; báo cáo chính thức cuối cùng là UC-033 (BR-154) xuất PDF ở bước "Hoàn tất sự cố". Xem Mục 11.3 file review.

## Business Logic (dùng chung mọi nền tảng)

### Điểm vào
Từ tab "Chờ tiếp nhận" của `25_IncidentList.md`: tick chọn 1 hoặc nhiều dòng `RPT-xxx` (`status = PENDING_TRIAGE`) → bấm nút **"Tạo báo cáo sự cố"** (đổi tên từ "Xác nhận liên quan ATTP" — hành động cũ giờ tách thành 2 bước, xem ghi chú đảo ngược ở trên).

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tiêu đề biên bản + thời gian/địa điểm lập | Text (đọc, tự động) | — | — | "Biên bản sự cố an toàn thực phẩm" + timestamp server tại thời điểm mở dialog + tên trường (từ hồ sơ trường) — cùng cấu trúc phần đầu `specialreport2173_1.md`, không nhập tay |
| 2 | Người lập biên bản | Text (đọc, tự động) | — | — | = tài khoản Medical Staff đăng nhập (nhất quán `reportedBy`/BR-183, "Đại diện Y tế trường học"/BR-211) |
| 3 | Các báo cáo nguồn (`RPT-xxx`) | Danh sách chip/link (đọc) | ≥1 | — | Mã các báo cáo thô đã tick chọn, mỗi chip mở lại modal "Xem chi tiết" (`25_IncidentList.md`) khi bấm |
| 4 | Khối context tự động (union) | Card (đọc) | — | BR-146 | Học sinh liên quan (union), Triệu chứng (union), Món ăn/bữa ăn nghi vấn (union, tham khảo — không phải field chính thức dùng để Quarantine, xem Mục 7.2/7.7 review file) — giữ để Medical Staff đối chiếu khi viết |
| 4b 🆕 | Ảnh minh chứng (kế thừa từ báo cáo nguồn) | Gallery (đọc) | — | BR-146 | Union `evidencePhotoUrls[]` từ các `RPT-xxx` đã chọn (field 10 `Teacher/12_IncidentForm.md`), chỉ để **tham khảo** khi viết biên bản. **Không** phải chỗ upload ảnh mới — nơi Medical Staff tự thêm bằng chứng đúng vị trí là panel "Gathering Evidence" (`17_InvestigationWorkspace.md`, BR-150), tránh 2 nguồn bằng chứng rời rạc (Mục 11.3.1) |
| 5 | Nội dung biên bản | **Rich text editor** (cùng toolbar `22_RecordStaffViolation.md` field 9: đậm/nghiêng/gạch chân, cỡ chữ, màu chữ, bảng, căn lề, chèn ảnh/link), bắt buộc | Không được để trống | BR-154 | Medical Staff **tự viết** phần tường trình tổng hợp; pre-fill gợi ý ban đầu = ghép nối mô tả của các RPT đã chọn (cùng cơ chế pre-fill đã dùng ở "Tường trình ban đầu" → Clinical notes), toàn quyền sửa/viết lại. Nâng từ `Textarea` trơn lên rich text để nhất quán thuật ngữ "biên bản" — cùng nguồn cấu trúc `specialreport2173_1.md` với biên bản vi phạm (Mục 11.3.2). **Không** áp gate ký-ảnh BR-211 (Mục 11.3.3) |
| 6 | Nút "Lưu bản nháp" | Button (secondary) | Không validate nội dung rỗng | — | Lưu đúng nội dung đang có, đóng dialog, **không chuyển tab**; các `RPT-xxx` liên quan chuyển `status = DRAFTING`, hiện badge "Đang soạn biên bản (nháp)" ở tab "Chờ tiếp nhận"; mở lại nút "Tạo báo cáo sự cố"/"Tiếp tục soạn" trên đúng các RPT đó sẽ load lại dialog với nội dung đã lưu |
| 7 | Nút "Hoàn tất biên bản" | Button (primary) | Disabled khi field 5 rỗng (MSG-023) | BR-154 | Validate nội dung không rỗng → lưu, các `RPT-xxx` liên quan chuyển `status = DRAFTED`, biên bản xuất hiện ở tab mới "Biên bản chờ xử lý" (`25_IncidentList.md`) |
| 8 | Nút "Hủy" | Button (ghost) | — | — | Đóng dialog, không lưu gì (khác "Lưu bản nháp") |

### Trạng thái màn hình
- Loading: spinner trên nút đang bấm (Lưu bản nháp/Hoàn tất biên bản).
- Empty: không áp dụng.
- Error: MSG-023 (nội dung biên bản rỗng khi bấm "Hoàn tất biên bản").
- Success: MSG-100 (Toast, "Lưu bản nháp") / MSG-101 (Toast, "Hoàn tất biên bản") — đóng dialog, quay lại `25_IncidentList.md`, tab tương ứng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ tab "Chờ tiếp nhận" (`25_IncidentList.md`, Web); Lưu bản nháp/Hoàn tất/Hủy → quay lại `25_IncidentList.md`.
**Bố cục**: `Dialog` cỡ lớn — khối context đọc (field 1-4b, gồm gallery ảnh union) phía trên, rich text editor nội dung biên bản (field 5) với thanh toolbar định dạng chiếm phần lớn chiều cao, 3 nút (Hủy | Lưu bản nháp | Hoàn tất biên bản) cố định cuối dialog.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ tab "Chờ tiếp nhận" (`25_IncidentList.md`, Mobile); Lưu bản nháp/Hoàn tất/Hủy → quay lại danh sách.
**Bố cục**: trang riêng full-screen (không phải Dialog nổi, do nội dung dài) — context đọc xếp dọc trên cùng, Textarea nội dung biên bản, 3 nút xếp dọc cố định cuối màn (Hoàn tất biên bản trên cùng làm primary, Lưu bản nháp giữa, Hủy dưới cùng dạng text link).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Create Incident Record Dialog, nền tảng Web, actor Medical Staff.
Bố cục: Dialog lớn, khối context đọc (thời gian/địa điểm, người lập, các RPT nguồn, union học sinh/triệu chứng/món ăn,
gallery ảnh minh chứng kế thừa từ báo cáo nguồn — chỉ đọc), rich text editor "Nội dung biên bản" bắt buộc (toolbar định dạng
đậm/nghiêng/gạch chân, cỡ chữ, màu, bảng, căn lề, chèn ảnh/link), 3 nút cuối dialog: Hủy (ghost) | Lưu bản nháp (secondary) | Hoàn tất biên bản (primary).
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (viền đỏ Textarea khi rỗng), success (toast).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `00_Policies/specialreport2173_1.md` (cấu trúc biên bản tham khảo).
- `business_rules.md` BR-146, BR-154, Changelog v1.8.
- `error_message.md` MSG-023, MSG-100, MSG-101.
- `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.8 (quyết định gốc bị đảo ngược), Mục 10.3 (quyết định mới), Mục 11.3 (ảnh union + rich text editor).
- `22_RecordStaffViolation.md` field 9 (rich text editor tham chiếu toolbar).

**Ghi chú màn hình dùng chung**: giống bản Mobile — cùng field/logic, chỉ khác bố cục Dialog vs trang riêng.
