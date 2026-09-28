# Screen Spec — Investigation Workspace

**Actor**: Medical Staff | **UC liên quan**: UC-041 (Report 3 §3.2, dòng 4445–4460) | **Status**: Draft
**BR liên quan**: BR-146 (ngưỡng tự động kích hoạt điều tra), BR-147 (phân loại severity), BR-148 (cảnh báo tức thời High), BR-149 (cách ly lô hàng), BR-150 (điều tra nguyên nhân gốc rễ), BR-151 (bản đồ truy xuất trong 4 giờ), BR-152 (CAPA), BR-154 (báo cáo sự cố chính thức)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-023 (Error)

> **Cập nhật 15/07/2026 — banner "đang chờ ai"**: bổ sung field 0, áp dụng quyết định 09/07/2026 (`review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 7.6) chưa từng được đưa vào Screen Spec. Xem bản Web cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 0 | Banner "Đang chờ ai" 🆕 | Banner (đọc, điều kiện) | Chỉ hiện khi đang ở đúng điểm chờ | — | 1 trong các nhãn theo Mục 7.6: "Đang chờ Kitchen Manager xác minh nhà cung cấp" (E11), "Đang chờ School Administration duyệt CAPA" (E17), "Đang chờ Kitchen Manager/School Administration triển khai & giám sát CAPA" (E18-19), "Đang chờ School Administration xem xét báo cáo sự cố" (E21) |
| 1 | Card "1. Extracting Stored Food Samples" | Card điều hướng | — | BR-150 | Mở `14_RetrieveFoodSampleRecordsDialog.md` |
| 2 | Card "2. Gathering Evidence" | Card điều hướng | — | BR-150 | Mở khối "Collect Evidence" (xem bên dưới) |
| 3 | Card "3. Identify the Root Cause" | Card điều hướng | — | BR-150, BR-151 | Ghi nhận kết quả xét nghiệm lab + kết luận chuyên môn |
| 4 | Card "4. Proposed Remedial Measures" | Card điều hướng | — | BR-152 | Mở khối "Treatment Measures" (xem bên dưới) |
| **Khối "Collect Evidence" (§3.3.2.3.10)** | | | | | |
| 5 | Type of evidence | Select | — | — | VD "Student/Teacher Statement Record" |
| 6 | Brief description | Textarea | — | — | |
| 7 | Attached documents / "Take a photo/Upload a file" | Camera + File input | — | BR-150 | |
| 8 | Nút "Save Evidence" | Button (primary) | — | — | |
| **Khối "Identify the Root Cause"** | | | | | |
| 9 | Kết quả xét nghiệm lab | Textarea/File | — | BR-150 | [Giả định — cần team xác nhận cấu trúc field cụ thể ngoài mô tả tổng quát UC-041 bước 4] |
| 10 | Kết luận nguyên nhân gốc rễ | Textarea | — | BR-150, BR-151 | |
| **Khối "Treatment Measures" (§3.3.2.3.11)** | | | | | |
| 11 | Medical treatment/correction measures | Textarea | — | BR-152 | |
| 12 | Unit/Person in charge (Responsible Party) | Input (text) | — | BR-152 | VD "Kitchen Manager & Milk Supplier" |
| 13 | Nút "Establish Measures" | Button (primary) | — | BR-152 | Giao CAPA cho Service Caterers, hạn 12 giờ |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải dữ liệu điều tra hiện có.
- Empty: chưa có bằng chứng/kết luận nào → hiển thị trạng thái trống cho từng card tương ứng.
- Error: MSG-004, MSG-005, MSG-023 (Incident ID không hợp lệ khi liên kết mẫu).
- Success: lưu từng khối thành công → Toast xác nhận chung; hoàn tất "Establish Measures" → CAPA được giao, quay lại `26_IncidentDetail.md`.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (nút "Step 2: Incident Investigation Space"); "Establish Measures" → quay lại Incident Detail.
**Bố cục**: Report 3 §3.3.2.3.7 — AppBar (Back, Home, "Back to Details"), 4 Card xếp dọc (1 cột) dẫn tới 4 khối con, mỗi khối con mở trang riêng (Collect Evidence, Treatment Measures theo §3.3.2.3.10–11) có nút "Come back" quay lại Investigation Space.

## Platform Layout — Web
**Entry/Exit points**: Vào từ `26_IncidentDetail.md` (Web); "Establish Measures" → quay lại Incident Detail.
**Bố cục**: theo `08_Medical_Staff_Screen_Flow_Web.md` — trình bày dạng nhiều panel song song (bằng chứng | root cause | CAPA) tận dụng màn hình rộng thay vì 4 bước tuần tự như Mobile; mỗi panel tương ứng 1 khối (Extract Sample/Evidence/Root Cause/Treatment Measures).

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Investigation Workspace, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: 4 khối (Extracting Stored Food Samples, Gathering Evidence, Identify Root Cause,
Proposed Remedial Measures) với field tương ứng (Type of evidence, Brief description, Attached documents,
kết quả xét nghiệm, kết luận nguyên nhân, Medical treatment measures, Responsible Party), nút Save
Evidence/Establish Measures.
Áp dụng Design System: primary #0E7490, critical #7E22CE cho trạng thái Quarantine liên quan.
Trạng thái cần thể hiện: loading, empty (mỗi khối), error (MSG-023).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4445–4460 (UC-041), dòng 5826–5943 (§3.3.2.3.7, 3.3.2.3.10, 3.3.2.3.11).
- business_rules.md (BR-146–152, BR-154).
- error_message.md (MSG-004, MSG-005, MSG-023).
- 03_Design/04_ScreenFlow/03_Medical_Staff_Screen_Flow.md ("InvSpace" node).

**Ghi chú**: "Collect Evidence" và "Treatment Measures" không có dòng riêng trong `14_Screen_Authorization_Matrix.md` khối 6 (chỉ "Investigation Workspace" UC-041 có cột `x`) — gộp vào file này làm khối con, không tách file riêng.
