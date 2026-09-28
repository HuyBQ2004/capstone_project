# Phương án Đề xuất — Cho phép Phụ huynh Khai báo Bệnh lý nền của Học sinh

**Người soạn**: (điền tên) | **Ngày**: 15/07/2026 | **Trạng thái**: Draft đề xuất
**Phạm vi**: MF03 — Student & Parent Management (mở rộng tính năng Health & Allergy Declaration)
**UC liên quan**: UC-084, UC-093, UC-050, UC-094, UC-104, UC-145
**BR liên quan**: BR-030, BR-031, BR-066, BR-172, BR-173

---

## 1. Bối cảnh & Vấn đề

Hiện tại actor **Parent** đã khai báo được hai nhóm thông tin cho con qua UC-084 (Submit Health & Allergy Information) và UC-093 (Update):

- **Thông tin sức khỏe cơ bản**: Height, Weight, Blood Type, File đính kèm (màn `08_SubmitHealthInformation`).
- **Thông tin dị ứng**: Allergen Category, mức độ Severity, tài liệu chứng minh (màn `05_SubmitAllergyInformation`).

Tuy nhiên **bệnh lý nền** của học sinh (ví dụ: hen suyễn, tiểu đường type 1, động kinh, tim bẩm sinh, dị ứng thuốc mạn tính…) **chưa có kênh cho phụ huynh khai báo**. Trong data model, trường `medical_conditions` chỉ tồn tại trong entity `student_health_records` — vốn là hồ sơ **append-only do Medical Staff sở hữu** (UC-094, BR-172). Phụ huynh không có đường ghi vào đó.

Đây là khoảng trống nghiệp vụ có rủi ro an toàn: bếp ăn (Service Caterers) và giáo viên (Teacher) cần biết bệnh lý nền để xử lý bữa ăn và tình huống khẩn cấp, nhưng thông tin này phụ thuộc hoàn toàn vào việc Medical Staff tự nhập.

## 2. Mục tiêu

Bổ sung khả năng để **Phụ huynh tự khai báo bệnh lý nền của con**, theo đúng cơ chế kiểm duyệt hiện hành: dữ liệu phụ huynh khai ở trạng thái **chờ xác minh**, Medical Staff duyệt trước khi có hiệu lực và hiển thị cho các actor khác.

## 3. Cơ sở nghiệp vụ (bám UC & BR hiện có)

| Mã | Nội dung | Vai trò trong phương án |
|---|---|---|
| **BR-030** | Thông tin dị ứng và chế độ ăn đặc biệt của học sinh phải được khai báo và lưu trong hồ sơ học sinh. | Mở rộng phạm vi "thông tin sức khỏe được khai báo" để bao gồm bệnh lý nền. |
| **BR-031** | Thông tin sức khỏe và dị ứng do phụ huynh khai báo **phải được Medical Staff xem xét và xác nhận** trước khi có hiệu lực. | Nền tảng cho luồng duyệt bệnh lý nền — không cần tạo cơ chế mới, chỉ áp dụng đúng BR-031. |
| **BR-066** | Danh mục chế độ ăn đặc biệt (Halal, Vegetarian, Gluten-Free, Nut-Free…). | Tham chiếu để thiết kế danh mục bệnh lý nền dạng chọn sẵn (nhất quán style). |
| **BR-172** | Bảo vệ toàn vẹn nhật ký (append-only, chống sửa trái phép). | Lý do **không** cho phụ huynh ghi thẳng vào `student_health_records`; phải tách kênh dữ liệu khai báo. |
| **BR-173** | Tuân thủ PDPD; dữ liệu nhạy cảm của trẻ em. | Bệnh lý nền là dữ liệu sức khỏe nhạy cảm → cần cơ chế đồng ý (consent), phân quyền xem, và audit khi truy cập. |

**Nguyên tắc thiết kế rút ra**: bệnh lý nền phụ huynh khai phải đi theo **đúng mẫu của Allergy Profile** — có trạng thái `PENDING / VERIFIED / REJECTED`, do Medical Staff duyệt (BR-031), tách khỏi hồ sơ lâm sàng append-only của y tế (BR-172), và được bảo vệ theo PDPD (BR-173).

## 4. Các phương án

### Phương án A — Danh mục chọn sẵn + mức độ *(Khuyến nghị)*

Thêm khai báo bệnh lý nền như một danh mục có cấu trúc, song song với Allergy Profile:

- Field **Condition Category** (`Select`): danh mục chuẩn — Hen suyễn, Tiểu đường, Động kinh, Tim mạch bẩm sinh, Rối loạn đông máu, Dị ứng thuốc, Khác (nhập tay nếu "Khác").
- Field **Mức độ / Ghi chú** (`Textarea`, tuỳ chọn): mô tả tình trạng, thuốc đang dùng, lưu ý xử trí.
- Field **Tài liệu y tế** (`Input type="file"`, tuỳ chọn nhưng khuyến khích): giấy tờ chẩn đoán.
- Trạng thái bản ghi: `PENDING → VERIFIED / REJECTED` (Medical Staff duyệt theo BR-031).

**Ưu điểm**: nhất quán với thiết kế Allergy (BR-066 style); Teacher/Caterer lọc và cảnh báo được theo loại bệnh; dữ liệu sạch để báo cáo. **Nhược điểm**: cần định nghĩa và bảo trì danh mục.

### Phương án B — Ô văn bản tự do

Chỉ thêm một `Textarea` "Bệnh lý nền" vào form Submit/Update Health Information, lưu tự do, Medical Staff duyệt.

**Ưu điểm**: triển khai nhanh, ít thay đổi. **Nhược điểm**: dữ liệu không cấu trúc, khó lọc/cảnh báo tự động, khó thống kê; dễ nhập thiếu nhất quán.

### Phương án C — Không cho phụ huynh khai, giữ nguyên (chỉ Medical Staff nhập)

Giữ hiện trạng: bệnh lý nền chỉ do Medical Staff nhập vào `student_health_records`.

**Ưu điểm**: không thay đổi hệ thống. **Nhược điểm**: không giải quyết được nhu cầu; phụ thuộc thủ công vào y tế; đúng khoảng trống đang cần lấp.

**Đề xuất chọn Phương án A** — cân bằng giữa an toàn dữ liệu, khả năng cảnh báo và chi phí triển khai.

## 5. Tác động thay đổi (theo Phương án A)

### 5.1. Data model
Không tạo bảng mới. Bổ sung kênh lưu dữ liệu khai báo tách khỏi hồ sơ y tế append-only:

- Cách gọn nhất: mở rộng nhóm dữ liệu phụ huynh khai (cùng chỗ với Allergy Profile) thêm các trường: `condition_category`, `condition_notes : TEXT`, `supporting_docs : TEXT`, `status : ENUM('PENDING','VERIFIED','REJECTED')`, `verified_by`, `verified_at`.
- **Giữ nguyên** `student_health_records.medical_conditions` cho ghi chú lâm sàng của Medical Staff (BR-172). Khi Medical Staff duyệt, có thể đối chiếu / sao chép nội dung đã xác minh sang hồ sơ này.

### 5.2. Use Case
- **UC-084 / UC-093**: bổ sung field bệnh lý nền vào form; thêm bước nhập trong Normal Flow; postcondition "lưu ở trạng thái Pending Verification, notify Medical Staff" (đã có sẵn theo BR-031).
- **UC-094 (Medical Staff Update/Review)**: bổ sung thao tác duyệt/ từ chối phần bệnh lý nền phụ huynh khai.
- **UC-050 / UC-104 / UC-145**: bổ sung hiển thị bệnh lý nền đã xác minh cho Parent / Medical Staff / Teacher.

### 5.3. Screen Specification (cả Web + Mobile)
- `Parent/08_SubmitHealthInformation.md` & `09_UpdateHealthInformation.md`: thêm dòng field "Chronic Condition" vào bảng Field và AI Generation Prompt.
- `Parent/07_StudentHealthInformation.md`: thêm mục hiển thị bệnh lý nền + Verification Status Badge.
- `MedicalStaff/02_ReviewHealthInformationScreen.md` & `05_UpdateHealthRecordScreen.md`: thêm khu vực review bệnh lý nền.
- `Teacher/05_HealthInformation.md`: hiển thị bệnh lý nền đã xác minh (cân nhắc highlight với bệnh nguy cấp, tương tự cảnh báo dị ứng Severe).

### 5.4. Business Rule
- Không cần BR mới nếu tái dùng BR-031 cho luồng duyệt. **Khuyến nghị** bổ sung diễn giải vào BR-030/BR-031 nêu rõ "bệnh lý nền" thuộc phạm vi thông tin sức khỏe phụ huynh khai và phải qua xác minh.
- Cân nhắc 1 BR PDPD-cụ thể (gắn BR-173): quy định quyền xem bệnh lý nền theo vai trò + ghi audit mỗi lần truy cập.

### 5.5. Traceability
Cập nhật `03_TraceabilityMatrix/traceability_matrix.md` cho các UC bị chỉnh (084, 093, 094, 050, 104, 145) và message liên quan (MSG-004, MSG-005, MSG-036).

## 6. Ràng buộc tuân thủ (PDPD — BR-173)
Bệnh lý nền là dữ liệu sức khỏe nhạy cảm của trẻ em:

- Chỉ phụ huynh liên kết với học sinh (BR-030) mới khai/sửa được; giới hạn tối đa 5 học sinh/tài khoản vẫn áp dụng (BR-201).
- Chỉ hiển thị cho các vai trò có nhu cầu chính đáng: Medical Staff (duyệt), Teacher chủ nhiệm & Service Caterers (xử lý bữa ăn/khẩn cấp), Parent của chính học sinh đó.
- Ghi **audit log** mỗi lần truy cập/sửa (bảo vệ toàn vẹn theo BR-172).
- Không thu thập dữ liệu sinh trắc học (BR-173 giữ nguyên).

## 7. Rủi ro & Lưu ý
- **Ranh giới trách nhiệm**: dữ liệu phụ huynh khai chỉ là *khai báo*, không phải chẩn đoán y tế — phải có nhãn "Chưa xác minh / Đã xác minh" rõ ràng để tránh Teacher/Caterer hiểu nhầm.
- **Không ghi đè hồ sơ y tế**: tuyệt đối tách khỏi `student_health_records` append-only (BR-172).
- **Danh mục bệnh**: cần thống nhất với chuyên môn y tế trước khi chốt danh sách category.

## 8. Đề xuất triển khai theo bước
1. Chốt Phương án A + danh mục bệnh lý nền với nghiệp vụ/y tế.
2. Cập nhật UC-084, UC-093, UC-094 và BR-030/031 (+ BR PDPD nếu thống nhất).
3. Cập nhật data model (ERD + Class Diagram) cho nhóm trường khai báo mới.
4. Cập nhật Screen Spec (Web + Mobile) và Traceability Matrix.
5. Review chéo + cập nhật wireframe.

---
*Nguồn tham chiếu: `02_Requirements/02_UseCases/MF03_Student_Parent_Management.md` (UC-084, UC-093, UC-094, UC-050, UC-104, UC-145); `02_Requirements/00_BusinessRules/business_rules.md` (BR-030, BR-031, BR-066, BR-172, BR-173, BR-201); `03_Design/01_Diagrams/02_ERD/erd_crowsfoot.puml` (`student_health_records`, `allergy_profiles`); các Screen Spec Parent/MedicalStaff/Teacher.*
