# Báo cáo tổng hợp — Phiên làm việc 2026-07-01

> Tổng hợp 2 phần việc: (1) hoàn tất rà soát/sửa trích dẫn BR trong Report 3 + traceability_matrix theo BR chuẩn; (2) kiểm tra và bổ sung 2 tính năng được hỏi: quản lý năm học, biên bản xử phạt.

---

## 1. Rà soát trích dẫn BR — đã hoàn tất theo "BR mới"

Thực thi đầy đủ theo `BR_Citation_Audit_Report.md` (4 bucket, xem file đó để biết chi tiết từng mã):

- **Bucket A (12 mã)** + **Bucket B (9 mã)**: đổi số về BR chuẩn xuyên suốt Report 3.md (cả bảng "Business Rules" lẫn văn xuôi Normal Flow/Alternative Flows/Postconditions) và traceability_matrix.md (bảng UC↔BR và BR↔UC, có gộp/xoá dòng trùng lặp sau khi đổi số).
- **Bucket C (16 mã, có bổ sung BR-045 phát hiện thêm)**: xoá trích dẫn khỏi mọi vị trí vì không có căn cứ trong BR chuẩn; 1 trường hợp (BR-059, "Supplier Deactivation Cascade") được giữ lại mô tả hành vi nhưng thay trích dẫn bằng ghi chú trỏ về báo cáo thay vì xoá âm thầm.
- **Bucket D (BR-131)**: xoá trích dẫn khỏi UC-016; đã kiểm tra trực tiếp nội dung UC-016 và UC-047, xác nhận không có đoạn mô tả nào về sinh trắc học cho học sinh cần sửa thêm.
- **Kết quả kiểm chứng**: quét lại toàn bộ Report 3.md (trước phụ lục cũ dòng 6223) và toàn bộ traceability_matrix.md — **0 mã BR ma còn sót**, ngoại trừ các dòng changelog/footnote lịch sử tự mô tả việc sửa.

---

## 2. Quản lý Năm học (Academic Year Management) — **CHƯA CÓ**, đã tạo đề xuất

Kiểm tra `class_diagram_description.md`, `business_rules.md`, `traceability_matrix.md`, và toàn bộ Report 3.md: không có entity, Business Rule, hay Use Case nào quản lý vòng đời năm học. Duy nhất một câu nhắc thoáng qua ("reminds parents at academic year start") gắn với BR-033 — chính là rule đã bị xác nhận loại bỏ ở phần 1.

**Đã tạo**: `02_Requirements/AcademicYear_Management_Proposal.md` — đề xuất entity `AcademicYear` (vòng đời Draft→Active→Closed→Archived), quan hệ `School → AcademicYear → Classroom`, 5 Business Rule (BR-191–195, placeholder) và 4 Use Case (UC-174–177: Create/Activate-Close/View List/Promote Students). Thiết kế theo nguyên tắc "append-only theo năm học" — không sửa/xoá `Classroom` cũ, tạo bản ghi mới cho năm học mới, giữ nguyên truy xuất lịch sử. **Trạng thái: PROPOSED**, còn 5 vấn đề cần team xác nhận trước khi merge chính thức (xem mục 4 trong file đó — quan trọng nhất: cách xử lý "lên lớp" hàng loạt và hồ sơ sức khỏe học sinh có tách theo năm học hay không).

---

## 3. Biên bản xử phạt (Violation Management) — **ĐÃ CÓ ĐỀ XUẤT, CHƯA CHÍNH THỨC HOÁ**

Đây không phải khoảng trống mới — đã được đề xuất chi tiết trong phiên làm việc trước đó (`02_Requirements/Violation_Management_Proposal.md`, phản hồi yêu cầu của Hội đồng đồ án), gồm đầy đủ Biên bản Vi phạm (`StaffViolationRecord`, mở rộng `NonConformanceRecord`) và Biên bản Xử lý Sai phạm (`ViolationResolutionRecord`). **Nhưng vẫn ở trạng thái PROPOSED** — chưa merge vào `business_rules.md`, `class_diagram_description.md`, hay `traceability_matrix.md`.

**Đã làm hôm nay**: phát hiện và sửa lỗi đánh số UC-164–169 của đề xuất này đã bị chiếm chỗ bởi các UC thật (Meal Feedback, Classroom) sau đợt reconciliation ID — đổi thành UC-178–183 (tiếp theo ngay sau đề xuất Academic Year Management, UC-174–177), tránh chồng số giữa 2 đề xuất đang chờ duyệt song song.

**Chưa chính thức hoá vì còn 9 quyết định treo** (mục 6 trong file đề xuất), quan trọng nhất:
1. Số ngày/tháng tính "nghi ngờ tái diễn vi phạm" (BR-187) — chưa có con số.
2. Vi phạm mức LOW: Medical Staff tự phê duyệt được không, hay luôn cần School Administration?
3. `ViolationResolutionRecord` thay thế hay chạy song song với `CorrectiveActionRequest` hiện có?
4. Đặt tên module (MF13 hay mở rộng MF07/MF11)?
5. Áp dụng biên bản vi phạm nhân viên cho Teacher/Raw Food Supplier/Parent hay chỉ Service Caterers/Medical Staff?

---

## 4. Việc còn lại (chưa làm, cần bạn xác nhận hướng đi tiếp)

| Việc | Trạng thái | Cần gì để tiếp tục |
|---|---|---|
| Merge Violation Management vào canonical docs | Chờ | Trả lời 5 câu hỏi ở mục 3 trên |
| Merge Academic Year Management vào canonical docs | Chờ | Trả lời 5 câu hỏi ở mục 4 của `AcademicYear_Management_Proposal.md` |
| Dán 2 bảng EN (BR/MSG) vào Report 3, thay thế phụ lục cũ (dòng 6223+) | Chưa làm | Xác nhận có muốn thay thế phụ lục cũ trong Report 3.md luôn không |
| Đồng bộ ID sang các tài liệu implementation khác (nếu phát sinh thêm) | Đã xong cho MF01 | Không có, trừ khi phát hiện thêm module implementation khác dùng ID cũ |
