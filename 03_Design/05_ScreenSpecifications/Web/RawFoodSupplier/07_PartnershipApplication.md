# Screen Spec — Partnership Application

**Actor**: Raw Food Supplier | **UC liên quan**: UC-152 - Submit Supplier Application (Report 3 §3.2, dòng 2639–2658); liên quan UC-155 (Manage Supplier Application, dòng 2699–2718) và UC-170 (Update/Resubmit Supplier Application, dòng 2679–2697) khi nộp lại đơn bị từ chối | **Status**: Draft
**BR liên quan**: BR-040 (quy trình đăng ký nhà cung cấp), BR-046 (tài liệu tuân thủ), BR-047 (cảnh báo hết hạn), BR-048 (từ chối + lý do), BR-010 (xác minh email)
**MSG liên quan**: MSG-004, MSG-005, MSG-007 (sai định dạng mã số thuế), MSG-008 (vượt 10MB), MSG-009 (sai định dạng file), MSG-013 (chứng nhận hết hạn, chặn nộp — BR-048), MSG-025 (lỗi giao dịch DB), MSG-028 (Success — nộp đăng ký thành công)

> **Ghi chú đối chiếu nguồn**: `traceability_matrix.md` dòng 99 ánh xạ UC-152 với BR-040, BR-046, BR-050. BR-050 (đăng ký nhân viên giao hàng) không liên quan trực tiếp tới việc nộp đơn hợp tác công ty — nhiều khả năng là sai sót trong traceability matrix (nên là BR-047/BR-048 như Normal Flow/Alternative Flow của chính UC-152 mô tả). Bảng dưới đây dùng bộ BR theo đúng Normal Flow/Alternative Flow của UC-152 trong Report 3 (nguồn field/hành vi chính theo mục 3 của kế hoạch phân công), có ghi chú BR-050 riêng để team xác nhận lại traceability_matrix.md.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Apply for Onboarding" | — | UC-152 Normal Flow bước 1 |
| 2 | Company Name | Input text | Bắt buộc | BR-040 | |
| 3 | Tax ID | Input text | Bắt buộc, đúng định dạng mã số thuế (MSG-007 nếu sai) | BR-040 | |
| 4 | Business Address | Input text | Bắt buộc | BR-040 | |
| 5 | Business License (file) | File upload | Bắt buộc, PDF/JPG/PNG, ≤10MB (MSG-008/009) | BR-040 | |
| 6 | Food Safety Certificate(s) (file, có thể nhiều) | File upload (multi) | Bắt buộc ≥1, không được hết hạn (BR-048, MSG-013 nếu hết hạn) | BR-046, BR-047, BR-048 | |
| 7 | Ngày hết hạn từng chứng nhận | Date picker | Bắt buộc theo mỗi file chứng nhận | BR-047 | |
| 8 | "Submit" Button | Button | Validate toàn bộ field trước khi gửi | BR-040, BR-046, BR-047, BR-048 | Sau khi Submit: trạng thái 'Pending Approval', gửi email xác nhận (BR-010) |
| 9 | "Cancel" Button | Button | Huỷ, quay về School Partnerships (03) | — | |

### Trạng thái màn hình
- Loading: Skeleton form khi tải lại dữ liệu đơn cũ (trường hợp Update/Resubmit UC-170).
- Empty: N/A (form nhập mới).
- Error: MSG-007 (sai mã số thuế), MSG-008 (file >10MB), MSG-009 (sai định dạng file), MSG-013 (chứng nhận hết hạn — chặn Submit theo BR-048), MSG-025 (lỗi DB).
- Success: MSG-028 "Registration request submitted! A verification email has been sent." → điều hướng Application Status (08).

---

## Platform Layout — Web
**Entry/Exit points**: Từ School Partnerships (03) qua "Add / Plus" Icon, hoặc từ Supplier Dashboard nếu chưa có partnership. Tương ứng node `PartnerApp["Supplier Partnership Application Form"]` trong `11_Raw_Food_Supplier_Screen_Flow_Web.md`. Điều hướng đi: Application Status (08).
**Bố cục**: Form dài chia nhiều bước dùng Stepper ngang (`Layout_Guidelines.md` §1.2) — Bước 1: Thông tin công ty (Company Name, Tax ID, Address); Bước 2: Tải lên tài liệu (Business License, Food Safety Certificate + ngày hết hạn). Nút Submit cố định cuối bước 2.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Partnership Application, nền tảng Web, actor Raw Food Supplier.
Bố cục: Stepper ngang 2 bước — Bước 1 Thông tin công ty, Bước 2 Tải tài liệu tuân thủ.
Component bắt buộc dùng: Stepper, Input, DatePicker, file upload input + preview thumbnail, Button.
Thành phần bắt buộc: Company Name, Tax ID, Business Address, Business License upload, Food Safety Certificate upload (multi) + ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490, Be Vietnam Pro — xem Design_System.md.
Trạng thái cần thể hiện: error (MSG-007/008/009/013), success (MSG-028).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2639–2658 (UC-152 Submit Supplier Application).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-010, BR-040, BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-007, MSG-008, MSG-009, MSG-013, MSG-025, MSG-028.
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` dòng 99 (đối chiếu BR — xem ghi chú lệch ở trên).
