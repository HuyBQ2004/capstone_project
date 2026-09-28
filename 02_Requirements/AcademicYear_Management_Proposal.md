# Đề xuất bổ sung: Quản lý Năm học (Academic Year Management)

> **Trạng thái**: `PROPOSED` — toàn bộ nội dung trong file này là **đề xuất mới**, chưa phải trích xuất từ tài liệu chính thức hiện có. Mọi mã BR/UC dưới đây là **placeholder**, cần team duyệt trước khi merge vào `business_rules.md`, `traceability_matrix.md`, và `class_diagram_description.md` chính thức.
> **Lý do đề xuất**: Kiểm tra theo yêu cầu — "check xem đã có việc quản lý năm học chưa". Kết quả: **hoàn toàn không có**.

---

## 0. Bằng chứng đã kiểm tra (trước khi đề xuất)

| Đã tìm ở đâu | Kết quả |
|---|---|
| `class_diagram_description.md` — toàn bộ package "Organization" | Không có entity `AcademicYear`/`SchoolYear`/`Term`/`Semester` nào. `Classroom` không có field liên kết năm học (`academicYearId` không tồn tại). |
| `business_rules.md` (129 BR chuẩn) | Không có BR nào quản lý vòng đời năm học, lên lớp, hay tạo lớp cho năm học mới. |
| `traceability_matrix.md` | Không có UC nào tên "Manage Academic Year", "Promote Student", "Create Academic Year"... |
| `Report 3.md` (166+ mục UC Description) | Chỉ có **1 câu nhắc thoáng qua**, không phải tính năng thật: Alternative Flow của "Submit Health & Allergy Information" có "System automatically reminds parents at **academic year** start..." — gắn với BR-033 (đã xác nhận là rule bị loại bỏ khỏi bản chuẩn, xem `BR_Citation_Audit_Report.md` Bucket C). Không có entity, BR, hay UC thật sự nào đứng sau câu này. |

**Kết luận**: hệ thống hiện tại không thể phân biệt "Lớp 5A năm học 2026–2027" với "Lớp 5A năm học 2027–2028" — mỗi `Classroom` là một bản ghi tĩnh, không có khái niệm thời gian/niên khóa. Đây là khoảng trống kiến trúc thật sự, ảnh hưởng tới toàn bộ chu trình "kết thúc năm học → lên lớp → mở năm học mới" mà bất kỳ trường học nào cũng cần.

---

## 1. Business Rules đề xuất (đánh số tiếp theo BR-190 của đề xuất Violation Management — xem lưu ý đánh số ở mục 5)

| BR (đề xuất) | Nội dung |
| --- | --- |
| BR-191 | Mỗi trường chỉ được có **đúng 1 năm học ở trạng thái Active** tại một thời điểm. Tạo năm học Active mới bắt buộc phải đóng (Close) năm học Active hiện tại trước. |
| BR-192 | Mỗi `Classroom` phải thuộc về **đúng 1** `AcademicYear` — không thể tạo lớp học khi trường chưa có năm học ở trạng thái Active. |
| BR-193 | Khi đóng (Close) một năm học, toàn bộ `Classroom` thuộc năm học đó chuyển sang trạng thái **chỉ đọc** (không tạo học sinh mới, không đổi giáo viên chủ nhiệm) nhưng **toàn bộ dữ liệu lịch sử vẫn được giữ nguyên** để tra cứu/báo cáo (áp dụng theo pattern cascade đã có ở BR-021, không xóa dữ liệu). |
| BR-194 | Khi tạo năm học mới, hệ thống **cho phép sao chép cấu trúc lớp** (tên lớp, cấp lớp, sức chứa) từ năm học liền trước làm mẫu khởi tạo — **không tự động** chuyển học sinh sang lớp mới; việc "lên lớp" (promote) là hành động riêng, tường minh, do School Administration thực hiện. |
| BR-195 | Dữ liệu của năm học đã Archived (bữa ăn, kiểm tra, sự cố... gắn với `Classroom` của năm học đó) phải luôn **truy vấn được ở chế độ read-only** phục vụ audit/báo cáo dài hạn (áp dụng cùng nguyên tắc với BR-171 Log Retention Policy, BR-174 Truy vết lịch sử vận hành). |

⚠️ **Các con số này chỉ là placeholder** — số thật sẽ phụ thuộc vào việc đề xuất Violation Management (BR-183–190) có được duyệt trước hay không (xem mục 5).

---

## 2. Bổ sung Class Diagram (PlantUML — đề xuất thêm vào package "Organization")

```plantuml
package "Organization" #FFF9E6 {

  ' ─── Entity mới ───
  class AcademicYear {
    - academicYearId : UUID
    - schoolId : UUID
    - yearLabel : String        <<VD: "2026-2027">>
    - startDate : Date
    - endDate : Date
    - status : AcademicYearStatus
    - createdAt : DateTime
    --
    + activate() : void
    + close() : void
    + archive() : void
  }

  enum AcademicYearStatus {
    DRAFT
    ACTIVE
    CLOSED
    ARCHIVED
  }

  ' ─── Quan hệ mới ───
  School "1" --> "0..*" AcademicYear : has
  AcademicYear "1" --> "0..*" Classroom : contains
}
```

**Ghi chú thiết kế quan trọng**: `Classroom` **không bị sửa/xóa** khi sang năm học mới — mỗi năm học tạo ra các bản ghi `Classroom` MỚI (gắn `academicYearId` mới), giữ nguyên các bản ghi `Classroom` cũ của năm học trước làm lịch sử. Đây là lựa chọn kiến trúc **append-only theo năm học** thay vì sửa tại chỗ (in-place mutation), để không phá vỡ truy xuất nguồn gốc (traceability) của các bản ghi bữa ăn/kiểm tra/sự cố vốn đã liên kết tới `Classroom` cụ thể của năm học cũ (nguyên tắc nhất quán với BR-076 "Truy xuất lô nguyên liệu" và toàn bộ triết lý audit trail bất biến của hệ thống — BR-162).

---

## 3. Use Case đề xuất (đánh số tiếp theo UC-173 — số UC chính thức mới nhất sau reconciliation 2026-07-01)

| UC (đề xuất) | Tên | Actor | BR liên quan | Nền tảng |
| --- | --- | --- | --- | --- |
| UC-174 | Create Academic Year | School Administration | BR-191, BR-192, BR-194 | Web |
| UC-175 | Activate/Close Academic Year | School Administration | BR-191, BR-193 | Web |
| UC-176 | View Academic Year List | School Administration, System Admin | — | Web |
| UC-177 | Promote Students to Next Academic Year | School Administration | BR-194 | Web |

Không phát sinh nền tảng mới — khớp với ma trận Web/Mobile hiện có (School Administration = Web only).

---

## 4. Vấn đề cần team xác nhận trước khi chính thức hoá

1. **Phạm vi "lên lớp" (UC-177)**: chuyển từng học sinh thủ công, hay bulk theo toàn khối (VD "cả khối 5 lên khối 6")? Trường hợp học sinh ở lại lớp (lưu ban) xử lý ra sao?
2. **Chuyển giao dữ liệu sức khỏe/dị ứng học sinh** khi lên năm học mới: hồ sơ `StudentProfile` có tách theo năm học không, hay là 1 hồ sơ xuyên suốt gắn nhiều `Classroom` (qua các năm)? Đề xuất hiện tại giả định hồ sơ học sinh xuyên suốt (không nhân bản theo năm), chỉ `Classroom` là theo năm — cần team xác nhận đây có đúng ý định nghiệp vụ không.
3. **Ai có quyền tạo năm học mới**: chỉ School Administration của từng trường, hay cần System Admin phê duyệt/khởi tạo hàng loạt cho tất cả trường cùng lúc vào đầu năm học?
4. **Thời điểm chuyển tiếp**: có cho phép 2 năm học "chồng lấn" tồn tại tạm thời (năm cũ chưa Close hẳn, năm mới đã Draft) để chuẩn bị trước, hay bắt buộc phải Close xong mới được tạo Draft mới?
5. Đánh số BR/UC ở đây là placeholder — cần xác nhận thứ tự merge so với đề xuất Violation Management (`02_Requirements/Violation_Management_Proposal.md`, hiện đang chờ 9 quyết định riêng, xem mục 5 file đó).

---

*File này là đề xuất (PROPOSED), tạo theo yêu cầu rà soát "quản lý năm học đã có chưa" — chưa merge vào `business_rules.md`, `traceability_matrix.md`, hay `class_diagram_description.md` chính thức. Cần review mục 4 trước khi chính thức hoá.*
